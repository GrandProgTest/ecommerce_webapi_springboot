package com.finalproject.ecommerce.ecommerce.orderspayments.application.internal.commandservices;

import com.finalproject.ecommerce.ecommerce.carts.interfaces.acl.CartContextFacade;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.acl.dto.CartDto;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.acl.IamContextFacade;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.exceptions.InvalidOrderOperationException;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.exceptions.OrderNotFoundException;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.aggregates.Order;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands.CancelOrderCommand;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands.CreateOrderFromCartCommand;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands.SeedOrderStatusCommand;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities.Discount;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities.OrderStatus;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.valueobjects.OrderStatuses;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.services.OrderCommandService;
import com.finalproject.ecommerce.ecommerce.orderspayments.repositories.DiscountRepository;
import com.finalproject.ecommerce.ecommerce.orderspayments.repositories.OrderRepository;
import com.finalproject.ecommerce.ecommerce.orderspayments.repositories.OrderStatusRepository;
import com.finalproject.ecommerce.ecommerce.products.interfaces.acl.ProductContextFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;

@Service
public class OrderCommandServiceImpl implements OrderCommandService {

    private final OrderRepository orderRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final DiscountRepository discountRepository;
    private final CartContextFacade cartContextFacade;
    private final ProductContextFacade productContextFacade;
    private final IamContextFacade iamContextFacade;


    public OrderCommandServiceImpl(OrderRepository orderRepository, OrderStatusRepository orderStatusRepository, DiscountRepository discountRepository, CartContextFacade cartContextFacade, ProductContextFacade productContextFacade, IamContextFacade iamContextFacade) {
        this.orderRepository = orderRepository;
        this.orderStatusRepository = orderStatusRepository;
        this.discountRepository = discountRepository;
        this.cartContextFacade = cartContextFacade;
        this.productContextFacade = productContextFacade;
        this.iamContextFacade = iamContextFacade;
    }

    @Override
    @Transactional
    public Order handle(CreateOrderFromCartCommand command) {
        iamContextFacade.validateUserCanAccessResource(command.userId());

        if (!iamContextFacade.userExists(command.userId())) {
            throw new InvalidOrderOperationException("User with ID " + command.userId() + " does not exist");
        }

        CartDto cartDto = cartContextFacade.getCartById(command.cartId()).orElseThrow(() -> new InvalidOrderOperationException("Cart with ID " + command.cartId() + " not found"));

        if (!cartDto.userId().equals(command.userId())) {
            throw new InvalidOrderOperationException("Cart does not belong to the user");
        }

        if (cartDto.items().isEmpty()) {
            throw new InvalidOrderOperationException("Cannot create order from empty cart");
        }

        if (!cartDto.isActive()) {
            throw new InvalidOrderOperationException("Cart is not active");
        }

        if (orderRepository.findByCartId(command.cartId()).isPresent()) {
            throw new InvalidOrderOperationException("Order already exists for this cart");
        }

        OrderStatus pendingStatus = orderStatusRepository.findByName(OrderStatuses.PENDING.name()).orElseThrow(() -> new IllegalStateException("Pending order status not found"));

        Order order = new Order(command.userId(), command.cartId(), command.addressId(), pendingStatus);

        cartDto.items().forEach(cartItem -> {
            BigDecimal price = productContextFacade.getProductPrice(cartItem.productId());
            order.addItem(cartItem.productId(), price, cartItem.quantity());
        });

        if (command.discountCode() != null && !command.discountCode().isBlank()) {
            Discount discount = discountRepository.findByCode(command.discountCode()).orElseThrow(() -> new InvalidOrderOperationException("Discount code not found"));
            order.applyDiscount(discount);
        }

        Order savedOrder = orderRepository.save(order);

        cartContextFacade.checkoutCart(command.userId(), command.cartId());

        return savedOrder;
    }

    @Override
    @Transactional
    public Order handle(CancelOrderCommand command) {
        Order order = orderRepository.findById(command.orderId()).orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + command.orderId()));

        iamContextFacade.validateUserCanAccessResource(order.getUserId());

        OrderStatus cancelledStatus = orderStatusRepository.findByName(OrderStatuses.CANCELLED.name()).orElseThrow(() -> new IllegalStateException("Cancelled order status not found"));

        order.cancel(cancelledStatus);
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public void handle(SeedOrderStatusCommand command) {
        Arrays.stream(OrderStatuses.values()).forEach(status -> {
            if (!orderStatusRepository.existsByName(status.name())) {
                String description = switch (status) {
                    case PENDING -> "Order is pending payment";
                    case PAID -> "Order has been paid";
                    case CANCELLED -> "Order has been cancelled";
                };
                orderStatusRepository.save(new OrderStatus(status, description));
            }
        });
    }
}
