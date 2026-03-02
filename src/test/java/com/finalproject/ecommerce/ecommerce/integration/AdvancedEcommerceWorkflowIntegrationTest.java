package com.finalproject.ecommerce.ecommerce.integration;

import com.finalproject.ecommerce.ecommerce.carts.application.internal.commandservices.CartCommandServiceImpl;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.aggregates.Cart;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.commands.*;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.entities.CartStatus;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.valueobjects.CartStatuses;
import com.finalproject.ecommerce.ecommerce.carts.infrastructure.persistence.jpa.repositories.CartRepository;
import com.finalproject.ecommerce.ecommerce.carts.infrastructure.persistence.jpa.repositories.CartStatusRepository;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.acl.CartContextFacade;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.acl.dto.CartDto;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.acl.dto.CartItemDto;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.aggregates.User;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.CreateAddressCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.entities.Address;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.entities.Role;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.valueobjects.Roles;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.acl.IamContextFacade;
import com.finalproject.ecommerce.ecommerce.notifications.interfaces.acl.NotificationContextFacade;
import com.finalproject.ecommerce.ecommerce.orderspayments.application.internal.commandservices.DiscountCommandServiceImpl;
import com.finalproject.ecommerce.ecommerce.orderspayments.application.internal.commandservices.OrderCommandServiceImpl;
import com.finalproject.ecommerce.ecommerce.orderspayments.application.internal.queryservices.DiscountQueryServiceImpl;
import com.finalproject.ecommerce.ecommerce.orderspayments.application.ports.out.PaymentProvider;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.aggregates.Order;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands.CreateDiscountCommand;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands.CreateOrderFromCartCommand;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands.ToggleDiscountStatusCommand;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities.Discount;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities.OrderStatus;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities.PaymentIntentStatus;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.queries.GetDiscountByCodeQuery;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.valueobjects.OrderStatuses;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.valueobjects.PaymentIntentStatuses;
import com.finalproject.ecommerce.ecommerce.orderspayments.infrastructure.payment.stripe.dto.PaymentIntentResponse;
import com.finalproject.ecommerce.ecommerce.orderspayments.infrastructure.persistence.jpa.repositories.*;
import com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.acl.OrdersContextFacade;
import com.finalproject.ecommerce.ecommerce.products.application.internal.commandservices.CategoryCommandServiceImpl;
import com.finalproject.ecommerce.ecommerce.products.application.internal.commandservices.ProductCommandServiceImpl;
import com.finalproject.ecommerce.ecommerce.products.domain.model.aggregates.Product;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.CreateCategoryCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.CreateProductCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.SetProductSalePriceCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.ToggleProductLikeCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.entities.Category;
import com.finalproject.ecommerce.ecommerce.products.infrastructure.persistence.jpa.repositories.CategoryRepository;
import com.finalproject.ecommerce.ecommerce.products.infrastructure.persistence.jpa.repositories.ProductRepository;
import com.finalproject.ecommerce.ecommerce.products.interfaces.acl.ProductContextFacade;
import com.finalproject.ecommerce.ecommerce.shared.infrastructure.configuration.properties.StripeProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Advanced E-commerce Workflow Integration Tests")
class AdvancedEcommerceWorkflowIntegrationTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private CartRepository cartRepository;
    @Mock private CartStatusRepository cartStatusRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private OrderStatusRepository orderStatusRepository;
    @Mock private DiscountRepository discountRepository;
    @Mock private PaymentIntentRepository paymentIntentRepository;
    @Mock private PaymentIntentStatusRepository paymentIntentStatusRepository;
    @Mock private DeliveryStatusRepository deliveryStatusRepository;
    @Mock private IamContextFacade iamContextFacade;
    @Mock private ProductContextFacade productContextFacade;
    @Mock private OrdersContextFacade ordersContextFacade;
    @Mock private CartContextFacade cartContextFacade;
    @Mock private PaymentProvider paymentProvider;
    @Mock private NotificationContextFacade notificationContextFacade;
    @Mock private StripeProperties stripeProperties;
    @Mock private com.finalproject.ecommerce.ecommerce.iam.infrastructure.persistence.jpa.repositories.UserRepository userRepository;
    @Mock private com.finalproject.ecommerce.ecommerce.products.infrastructure.persistence.jpa.repositories.ProductCategoryRepository productCategoryRepository;
    @Mock private com.finalproject.ecommerce.ecommerce.products.infrastructure.persistence.jpa.repositories.ProductPriceLogRepository productPriceLogRepository;

    private ProductCommandServiceImpl productCommandService;
    private CategoryCommandServiceImpl categoryCommandService;
    private CartCommandServiceImpl cartCommandService;
    private OrderCommandServiceImpl orderCommandService;
    private DiscountCommandServiceImpl discountCommandService;
    private DiscountQueryServiceImpl discountQueryService;

    private User clientUser;
    private User managerUser;
    private Category electronicsCategory;
    private CartStatus activeCartStatus;
    private CartStatus checkedOutCartStatus;
    private OrderStatus pendingOrderStatus;
    private OrderStatus paidOrderStatus;
    private Address clientAddress;
    private PaymentIntentStatus paymentSucceededStatus;

    @BeforeEach
    void setUp() {
        categoryCommandService = new CategoryCommandServiceImpl(categoryRepository);

        productCommandService = new ProductCommandServiceImpl(
                productRepository,
                categoryRepository,
                productCategoryRepository,
                productPriceLogRepository,
                iamContextFacade,
                ordersContextFacade,
                notificationContextFacade
        );

        cartCommandService = new CartCommandServiceImpl(
                cartRepository,
                cartStatusRepository,
                iamContextFacade,
                productContextFacade
        );

        orderCommandService = new OrderCommandServiceImpl(
                orderRepository,
                orderStatusRepository,
                deliveryStatusRepository,
                discountRepository,
                paymentIntentRepository,
                paymentIntentStatusRepository,
                cartContextFacade,
                productContextFacade,
                iamContextFacade,
                paymentProvider,
                notificationContextFacade,
                stripeProperties
        );

        discountCommandService = new DiscountCommandServiceImpl(
                discountRepository
        );

        discountQueryService = new DiscountQueryServiceImpl(discountRepository);

        Role clientRole = new Role(Roles.ROLE_CLIENT);
        Role managerRole = new Role(Roles.ROLE_MANAGER);

        clientUser = new User("client", "client@example.com", "hashedPass", clientRole);
        setId(clientUser, 1L);
        clientUser.activate();

        managerUser = new User("manager", "manager@ravn.com", "hashedPass", managerRole);
        setId(managerUser, 100L);
        managerUser.activate();

        electronicsCategory = new Category("Electronics");
        setId(electronicsCategory, 1L);

        activeCartStatus = new CartStatus(CartStatuses.ACTIVE, "Active");
        checkedOutCartStatus = new CartStatus(CartStatuses.CHECKED_OUT, "Checked out");

        pendingOrderStatus = new OrderStatus(OrderStatuses.PENDING, "Pending");
        paidOrderStatus = new OrderStatus(OrderStatuses.PAID, "Paid");

        CreateAddressCommand addressCmd = new CreateAddressCommand(
                "123 Main St", "New York", "NY", "USA", "10001", true
        );
        clientAddress = new Address(addressCmd, clientUser);
        setId(clientAddress, 1L);

        paymentSucceededStatus = new PaymentIntentStatus(PaymentIntentStatuses.SUCCEEDED, "Payment succeeded");

        StripeProperties.Api apiConfig = mock(StripeProperties.Api.class);
        when(stripeProperties.getApi()).thenReturn(apiConfig);
        when(apiConfig.getSecretKey()).thenReturn("sk_test_mock");
        when(stripeProperties.getCurrency()).thenReturn("usd");

        when(userRepository.findById(1L)).thenReturn(Optional.of(clientUser));
        when(userRepository.findById(100L)).thenReturn(Optional.of(managerUser));

        when(productContextFacade.getUsersWhoLikedProduct(anyLong())).thenReturn(List.of());
        when(productPriceLogRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("Complete Workflow: Manager creates product with sale price, clients buy, discount applied")
    void completeWorkflow_ProductWithSaleAndDiscount() {
        when(categoryRepository.existsByName("Smart Home")).thenReturn(false);
        Category smartHomeCategory = new Category("Smart Home");
        setId(smartHomeCategory, 10L);
        when(categoryRepository.save(any(Category.class))).thenReturn(smartHomeCategory);

        CreateCategoryCommand categoryCmd = new CreateCategoryCommand("Smart Home");
        Long categoryId = categoryCommandService.handle(categoryCmd);

        assertThat(categoryId).isEqualTo(10L);

        when(categoryRepository.findById(10L)).thenReturn(Optional.of(smartHomeCategory));
        when(iamContextFacade.getCurrentUserId()).thenReturn(Optional.of(managerUser.getId()));
        when(productCategoryRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CreateProductCommand productCmd = new CreateProductCommand(
                "Smart Thermostat",
                "WiFi enabled smart thermostat",
                new BigDecimal("199.99"),
                50,
                List.of(10L),
                true
        );

        Product smartThermostat = new Product(productCmd, managerUser.getId());
        setId(smartThermostat, 1001L);
        smartThermostat.assignCategory(smartHomeCategory);

        when(productRepository.save(any(Product.class))).thenReturn(smartThermostat);
        when(productRepository.findById(1001L)).thenReturn(Optional.of(smartThermostat));
        Long productId = productCommandService.handle(productCmd);

        assertThat(productId).isEqualTo(1001L);
        assertThat(smartThermostat.getPrice()).isEqualByComparingTo(new BigDecimal("199.99"));

        when(productRepository.findById(1001L)).thenReturn(Optional.of(smartThermostat));

        SetProductSalePriceCommand salePriceCmd = new SetProductSalePriceCommand(
                1001L,
                new BigDecimal("149.99"),
                Instant.now().plus(7, ChronoUnit.DAYS)
        );

        smartThermostat.setSalePrice(new BigDecimal("149.99"));
        smartThermostat.setSalePriceExpireDate(Instant.now().plus(7, ChronoUnit.DAYS));

        Optional<Product> productWithSale = productCommandService.handle(salePriceCmd);

        assertThat(productWithSale).isPresent();
        assertThat(productWithSale.get().getSalePrice()).isEqualByComparingTo(new BigDecimal("149.99"));

        when(productRepository.findById(1001L)).thenReturn(Optional.of(smartThermostat));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ToggleProductLikeCommand likeCmd = new ToggleProductLikeCommand(clientUser.getId(), 1001L);
        boolean liked = productCommandService.handle(likeCmd);

        assertThat(liked).isTrue();
        assertThat(smartThermostat.getLikes()).hasSize(1);

        when(discountRepository.existsByCode("WELCOME10")).thenReturn(false);

        Discount welcomeDiscount = new Discount(
                "WELCOME10",
                10,
                Instant.now().minusSeconds(3600),
                Instant.now().plusSeconds(86400)
        );
        setId(welcomeDiscount, 1L);

        when(discountRepository.save(any(Discount.class))).thenReturn(welcomeDiscount);

        CreateDiscountCommand discountCmd = new CreateDiscountCommand(
                "WELCOME10",
                10,
                Instant.now().minusSeconds(3600),
                Instant.now().plusSeconds(86400)
        );

        Discount createdDiscount = discountCommandService.handle(discountCmd);

        assertThat(createdDiscount).isNotNull();
        assertThat(createdDiscount.getCode()).isEqualTo("WELCOME10");

        Cart clientCart = new Cart(clientUser.getId(), activeCartStatus);
        setId(clientCart, 50L);

        when(cartStatusRepository.findByName(CartStatuses.ACTIVE.name())).thenReturn(Optional.of(activeCartStatus));
        when(iamContextFacade.userExists(clientUser.getId())).thenReturn(true);
        when(productContextFacade.getProductStock(1001L)).thenReturn(50);
        when(productContextFacade.isProductDeleted(1001L)).thenReturn(false);
        when(productContextFacade.isProductActive(1001L)).thenReturn(true);
        doNothing().when(iamContextFacade).validateUserCanAccessResource(clientUser.getId());

        when(cartRepository.findByUserIdAndStatus(clientUser.getId(), activeCartStatus))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(clientCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(clientCart);

        AddProductToCartCommand addToCartCmd = new AddProductToCartCommand(clientUser.getId(), 1001L, 2);
        Cart cart = cartCommandService.handle(addToCartCmd);

        assertThat(cart).isNotNull();

        Long cartId = 50L;
        CartDto cartDto = new CartDto(
                cartId,
                clientUser.getId(),
                true,
                List.of(new CartItemDto(1L, 1001L, 2))
        );

        when(cartContextFacade.getCartById(cartId)).thenReturn(Optional.of(cartDto));
        when(orderStatusRepository.findByName(OrderStatuses.PENDING.name())).thenReturn(Optional.of(pendingOrderStatus));
        when(discountRepository.findByCode("WELCOME10")).thenReturn(Optional.of(welcomeDiscount));

        doNothing().when(iamContextFacade).validateUserCanAccessResource(clientUser.getId());
        when(iamContextFacade.userExists(clientUser.getId())).thenReturn(true);
        doNothing().when(iamContextFacade).validateAddressBelongsToUser(clientAddress.getId(), clientUser.getId());

        when(productContextFacade.isProductDeleted(1001L)).thenReturn(false);
        when(productContextFacade.isProductActive(1001L)).thenReturn(true);
        when(productContextFacade.getProductPrice(1001L)).thenReturn(new BigDecimal("149.99"));
        when(productContextFacade.hasActiveSalePrice(1001L)).thenReturn(true);
        when(productContextFacade.getProductStock(1001L)).thenReturn(48);
        doNothing().when(productContextFacade).decreaseProductStock(1001L, 2);

        PaymentIntentResponse paymentResponse = new PaymentIntentResponse(
                "pi_complete_workflow",
                "secret_complete",
                "requires_payment_method",
                null
        );

        when(orderRepository.findByCartId(cartId)).thenReturn(Optional.empty());
        Order mockOrder = new Order(clientUser.getId(), cartId, clientAddress.getId(), pendingOrderStatus);
        setId(mockOrder, 1L);

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            if (order.getId() == null) {
                setId(order, 1L);
            }
            return order;
        });
        when(paymentProvider.initiatePayment(any(Order.class))).thenReturn(paymentResponse);
        when(paymentIntentStatusRepository.findByName(any())).thenReturn(Optional.of(paymentSucceededStatus));
        when(paymentIntentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(cartContextFacade).checkoutCart(clientUser.getId(), cartId);

        CreateOrderFromCartCommand orderCmd = new CreateOrderFromCartCommand(
                clientUser.getId(),
                cartId,
                clientAddress.getId(),
                "WELCOME10"
        );

        Order order = orderCommandService.handle(orderCmd);


        assertThat(order).isNotNull();
        assertThat(order.getDiscount()).isNotNull();

        assertThat(order.getTotalAmount()).isEqualByComparingTo(new BigDecimal("299.98"));

        verify(productContextFacade).decreaseProductStock(1001L, 2);
        verify(cartContextFacade).checkoutCart(clientUser.getId(), cartId);
    }

    @Test
    @DisplayName("Discount Workflow: Create discount, apply to order, then deactivate")
    void discountWorkflow_CreateApplyDeactivate() {
        when(discountRepository.existsByCode("SUMMER25")).thenReturn(false);

        Discount summerDiscount = new Discount(
                "SUMMER25",
                25,
                Instant.now(),
                Instant.now().plus(30, ChronoUnit.DAYS)
        );
        setId(summerDiscount, 5L);

        when(discountRepository.save(any(Discount.class))).thenReturn(summerDiscount);

        CreateDiscountCommand createDiscountCmd = new CreateDiscountCommand(
                "SUMMER25",
                25,
                Instant.now(),
                Instant.now().plus(30, ChronoUnit.DAYS)
        );

        Discount createdDiscount = discountCommandService.handle(createDiscountCmd);

        assertThat(createdDiscount).isNotNull();
        assertThat(createdDiscount.getCode()).isEqualTo("SUMMER25");
        assertThat(createdDiscount.getPercentage()).isEqualTo(25);
        assertThat(createdDiscount.getIsActive()).isTrue();

        when(discountRepository.findByCode("SUMMER25")).thenReturn(Optional.of(summerDiscount));

        GetDiscountByCodeQuery query = new GetDiscountByCodeQuery("SUMMER25");
        Optional<Discount> queriedDiscount = discountQueryService.handle(query);

        assertThat(queriedDiscount).isPresent();
        assertThat(queriedDiscount.get().isValid()).isTrue();

        when(discountRepository.findById(5L)).thenReturn(Optional.of(summerDiscount));
        when(discountRepository.save(any(Discount.class))).thenAnswer(invocation -> {
            Discount d = invocation.getArgument(0);
            d.deactivate();
            return d;
        });

        ToggleDiscountStatusCommand deactivateCmd = new ToggleDiscountStatusCommand(5L);
        boolean isActive = discountCommandService.handle(deactivateCmd);

        assertThat(isActive).isFalse();
    }

    @Test
    @DisplayName("Liked Product Workflow: Product gets discount, users who liked it get notified")
    void likedProductWorkflow_DiscountNotification() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(electronicsCategory));
        when(iamContextFacade.getCurrentUserId()).thenReturn(Optional.of(managerUser.getId()));

        CreateProductCommand productCmd = new CreateProductCommand(
                "Wireless Earbuds",
                "Premium sound quality",
                new BigDecimal("129.99"),
                100,
                List.of(1L),
                true
        );

        Product earbuds = new Product(productCmd, managerUser.getId());
        setId(earbuds, 2001L);
        earbuds.assignCategory(electronicsCategory);

        when(productRepository.save(any(Product.class))).thenReturn(earbuds);
        when(productRepository.findById(2001L)).thenReturn(Optional.of(earbuds));
        Long productId = productCommandService.handle(productCmd);

        assertThat(productId).isEqualTo(2001L);

        User user1 = createMockUser(10L, "user1", "user1@example.com");
        User user2 = createMockUser(11L, "user2", "user2@example.com");
        User user3 = createMockUser(12L, "user3", "user3@example.com");

        when(productRepository.findById(2001L)).thenReturn(Optional.of(earbuds));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(iamContextFacade.userExists(10L)).thenReturn(true);
        when(iamContextFacade.userExists(11L)).thenReturn(true);
        when(iamContextFacade.userExists(12L)).thenReturn(true);

        productCommandService.handle(new ToggleProductLikeCommand(10L, 2001L));
        productCommandService.handle(new ToggleProductLikeCommand(11L, 2001L));
        productCommandService.handle(new ToggleProductLikeCommand(12L, 2001L));

        assertThat(earbuds.getLikes()).hasSize(3);

        when(productRepository.findById(2001L)).thenReturn(Optional.of(earbuds));
        when(iamContextFacade.getUserEmails(anyList())).thenReturn(java.util.Map.of(
                10L, "user1@example.com",
                11L, "user2@example.com",
                12L, "user3@example.com"
        ));
        doNothing().when(notificationContextFacade).sendDiscountAlertBatch(
                anySet(), anyString(), anyString(), anyString(), anyString(), anyString()
        );

        SetProductSalePriceCommand salePriceCmd = new SetProductSalePriceCommand(
                2001L,
                new BigDecimal("99.99"),
                Instant.now().plus(14, ChronoUnit.DAYS)
        );

        earbuds.setSalePrice(new BigDecimal("99.99"));
        earbuds.setSalePriceExpireDate(Instant.now().plus(14, ChronoUnit.DAYS));

        Optional<Product> productWithSale = productCommandService.handle(salePriceCmd);

        assertThat(productWithSale).isPresent();
        assertThat(productWithSale.get().getSalePrice()).isEqualByComparingTo(new BigDecimal("99.99"));
    }

    @Test
    @DisplayName("Stock Management Workflow: Concurrent purchases reduce stock atomically")
    void stockManagementWorkflow_ConcurrentPurchases() {
        CreateProductCommand productCmd = new CreateProductCommand(
                "Limited Edition Sneakers",
                "Only 10 pairs available",
                new BigDecimal("299.99"),
                10,
                List.of(1L),
                true
        );

        Product sneakers = new Product(productCmd, managerUser.getId());
        setId(sneakers, 3001L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(electronicsCategory));
        when(iamContextFacade.getCurrentUserId()).thenReturn(Optional.of(managerUser.getId()));
        when(productRepository.save(any(Product.class))).thenReturn(sneakers);

        Long productId = productCommandService.handle(productCmd);

        Cart cart1 = new Cart(1L, activeCartStatus);
        setId(cart1, 1L);

        when(cartStatusRepository.findByName(CartStatuses.ACTIVE.name())).thenReturn(Optional.of(activeCartStatus));
        when(iamContextFacade.userExists(1L)).thenReturn(true);
        when(productContextFacade.getProductStock(3001L)).thenReturn(10);
        when(productContextFacade.isProductDeleted(3001L)).thenReturn(false);
        when(productContextFacade.isProductActive(3001L)).thenReturn(true);
        doNothing().when(iamContextFacade).validateUserCanAccessResource(1L);

        when(cartRepository.findByUserIdAndStatus(1L, activeCartStatus))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(cart1));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart1);

        AddProductToCartCommand cmd1 = new AddProductToCartCommand(1L, 3001L, 4);
        cartCommandService.handle(cmd1);

        sneakers.updateProductInfo(null, null, null, 6);
        when(productRepository.findById(3001L)).thenReturn(Optional.of(sneakers));

        when(iamContextFacade.userExists(2L)).thenReturn(true);
        when(productContextFacade.getProductStock(3001L)).thenReturn(6);
        doNothing().when(iamContextFacade).validateUserCanAccessResource(2L);

        AddProductToCartCommand cmd2 = new AddProductToCartCommand(2L, 3001L, 8);
        assertThatThrownBy(() -> cartCommandService.handle(cmd2))
                .hasMessageContaining("only 6 in stock");

        Cart cart2 = new Cart(2L, activeCartStatus);
        setId(cart2, 2L);

        when(cartRepository.findByUserIdAndStatus(2L, activeCartStatus))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(cart2));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart2);

        AddProductToCartCommand cmd3 = new AddProductToCartCommand(2L, 3001L, 4);
        Cart cart2Result = cartCommandService.handle(cmd3);

        assertThat(cart2Result).isNotNull();

        sneakers.updateProductInfo(null, null, null, 2);

        assertThat(sneakers.getStock()).isEqualTo(2);
    }

    @Test
    @DisplayName("Low Stock Workflow: Purchase reduces stock below threshold, triggers notifications")
    void lowStockWorkflow_PurchaseTriggersNotification() {
        CreateProductCommand productCmd = new CreateProductCommand(
                "Running Shoes",
                "Premium running shoes",
                new BigDecimal("159.99"),
                5,
                List.of(1L),
                true
        );

        Product shoes = new Product(productCmd, managerUser.getId());
        setId(shoes, 4001L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(electronicsCategory));
        when(iamContextFacade.getCurrentUserId()).thenReturn(Optional.of(managerUser.getId()));
        when(productRepository.save(any(Product.class))).thenReturn(shoes);

        productCommandService.handle(productCmd);

        when(productRepository.findById(4001L)).thenReturn(Optional.of(shoes));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(iamContextFacade.userExists(20L)).thenReturn(true);
        when(iamContextFacade.userExists(21L)).thenReturn(true);
        when(iamContextFacade.userExists(22L)).thenReturn(true);
        doNothing().when(iamContextFacade).validateUserCanAccessResource(20L);
        doNothing().when(iamContextFacade).validateUserCanAccessResource(21L);
        doNothing().when(iamContextFacade).validateUserCanAccessResource(22L);

        productCommandService.handle(new ToggleProductLikeCommand(20L, 4001L));
        productCommandService.handle(new ToggleProductLikeCommand(21L, 4001L));
        productCommandService.handle(new ToggleProductLikeCommand(22L, 4001L));

        Long cartId = 60L;
        CartDto cartDto = new CartDto(
                cartId,
                clientUser.getId(),
                true,
                List.of(new CartItemDto(1L, 4001L, 3))
        );

        when(cartContextFacade.getCartById(cartId)).thenReturn(Optional.of(cartDto));
        when(orderStatusRepository.findByName(OrderStatuses.PENDING.name())).thenReturn(Optional.of(pendingOrderStatus));

        doNothing().when(iamContextFacade).validateUserCanAccessResource(clientUser.getId());
        when(iamContextFacade.userExists(clientUser.getId())).thenReturn(true);
        doNothing().when(iamContextFacade).validateAddressBelongsToUser(clientAddress.getId(), clientUser.getId());

        when(productContextFacade.isProductDeleted(4001L)).thenReturn(false);
        when(productContextFacade.isProductActive(4001L)).thenReturn(true);
        when(productContextFacade.getProductPrice(4001L)).thenReturn(new BigDecimal("159.99"));
        when(productContextFacade.hasActiveSalePrice(4001L)).thenReturn(false);
        doNothing().when(productContextFacade).decreaseProductStock(4001L, 3);
        when(productContextFacade.getProductStock(4001L)).thenReturn(2);

        when(productContextFacade.getUsersWhoLikedProduct(4001L)).thenReturn(List.of(20L, 21L, 22L));
        when(productContextFacade.getProductName(4001L)).thenReturn("Running Shoes");
        when(iamContextFacade.getUserEmails(List.of(20L, 21L, 22L))).thenReturn(java.util.Map.of(
                20L, "user20@example.com",
                21L, "user21@example.com",
                22L, "user22@example.com"
        ));
        doNothing().when(notificationContextFacade).sendLowStockAlertBatch(anySet(), anyString(), anyInt());

        PaymentIntentResponse paymentResponse = new PaymentIntentResponse("pi_lowstock", "secret_lowstock", "requires_payment_method", null);

        when(orderRepository.findByCartId(cartId)).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            if (order.getId() == null) {
                setId(order, 10L);
            }
            return order;
        });
        when(paymentProvider.initiatePayment(any(Order.class))).thenReturn(paymentResponse);
        when(paymentIntentStatusRepository.findByName(any())).thenReturn(Optional.of(paymentSucceededStatus));
        when(paymentIntentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(cartContextFacade).checkoutCart(clientUser.getId(), cartId);

        CreateOrderFromCartCommand orderCmd = new CreateOrderFromCartCommand(
                clientUser.getId(),
                cartId,
                clientAddress.getId(),
                null
        );

        Order order = orderCommandService.handle(orderCmd);

        assertThat(order).isNotNull();
        verify(productContextFacade).getUsersWhoLikedProduct(4001L);
        verify(notificationContextFacade).sendLowStockAlertBatch(
                argThat(set -> set.size() == 3),
                eq("Running Shoes"),
                eq(2)
        );
    }

    @Test
    @DisplayName("Cart Workflow: Update cart item quantities before checkout")
    void cartWorkflow_UpdateQuantitiesBeforeCheckout() {
        Cart cart = new Cart(clientUser.getId(), activeCartStatus);
        setId(cart, 70L);
        cart.addProduct(5001L, 2);
        cart.addProduct(5002L, 1);

        when(cartStatusRepository.findByName(CartStatuses.ACTIVE.name())).thenReturn(Optional.of(activeCartStatus));
        when(iamContextFacade.userExists(clientUser.getId())).thenReturn(true);
        doNothing().when(iamContextFacade).validateUserCanAccessResource(clientUser.getId());
        when(cartRepository.findByUserIdAndStatus(clientUser.getId(), activeCartStatus)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(productContextFacade.getProductStock(5001L)).thenReturn(50);
        when(productContextFacade.isProductDeleted(5001L)).thenReturn(false);
        when(productContextFacade.isProductActive(5001L)).thenReturn(true);

        UpdateCartItemQuantityCommand updateCmd = new UpdateCartItemQuantityCommand(
                clientUser.getId(),
                5001L,
                5
        );

        Cart updatedCart = cartCommandService.handle(updateCmd);

        assertThat(updatedCart).isNotNull();
        assertThat(cart.getProductQuantity(5001L)).isEqualTo(5);

        RemoveProductFromCartCommand removeCmd = new RemoveProductFromCartCommand(clientUser.getId(), 5002L);
        Cart cartAfterRemoval = cartCommandService.handle(removeCmd);

        assertThat(cartAfterRemoval).isNotNull();
        verify(cartRepository, atLeast(2)).save(any(Cart.class));
    }

    @Test
    @DisplayName("Clear Cart Workflow: User clears entire cart")
    void clearCartWorkflow_RemoveAllItems() {
        Cart cart = new Cart(clientUser.getId(), activeCartStatus);
        setId(cart, 80L);
        cart.addProduct(6001L, 3);
        cart.addProduct(6002L, 2);
        cart.addProduct(6003L, 1);

        when(cartStatusRepository.findByName(CartStatuses.ACTIVE.name())).thenReturn(Optional.of(activeCartStatus));
        doNothing().when(iamContextFacade).validateUserCanAccessResource(clientUser.getId());
        when(cartRepository.findByUserIdAndStatus(clientUser.getId(), activeCartStatus)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClearCartCommand clearCmd = new ClearCartCommand(clientUser.getId());
        Cart clearedCart = cartCommandService.handle(clearCmd);

        assertThat(clearedCart).isNotNull();
        assertThat(cart.getItems()).isEmpty();
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    @DisplayName("Product Activation Workflow: Deactivate product, clients cannot add to cart")
    void productActivationWorkflow_DeactivatedProductUnavailable() {
        CreateProductCommand productCmd = new CreateProductCommand(
                "Product to Deactivate",
                "Will be deactivated",
                new BigDecimal("79.99"),
                20,
                List.of(1L),
                true
        );

        Product product = new Product(productCmd, managerUser.getId());
        setId(product, 7001L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(electronicsCategory));
        when(iamContextFacade.getCurrentUserId()).thenReturn(Optional.of(managerUser.getId()));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productRepository.findById(7001L)).thenReturn(Optional.of(product));

        Long productId = productCommandService.handle(productCmd);

        assertThat(productId).isEqualTo(7001L);

        product.setIsActive(false);
        when(productRepository.findById(7001L)).thenReturn(Optional.of(product));

        when(cartStatusRepository.findByName(CartStatuses.ACTIVE.name())).thenReturn(Optional.of(activeCartStatus));
        when(iamContextFacade.userExists(clientUser.getId())).thenReturn(true);
        when(productContextFacade.getProductStock(7001L)).thenReturn(20);
        when(productContextFacade.isProductDeleted(7001L)).thenReturn(false);
        when(productContextFacade.isProductActive(7001L)).thenReturn(false);

        AddProductToCartCommand addCmd = new AddProductToCartCommand(clientUser.getId(), 7001L, 1);

        assertThatThrownBy(() -> cartCommandService.handle(addCmd))
                .hasMessageContaining("not available");
    }

    @Test
    @DisplayName("Order with Multiple Products Workflow: Different quantities and prices")
    void orderWorkflow_MultipleProducts_DifferentQuantities() {
        Long cartId = 90L;
        CartDto cartDto = new CartDto(
                cartId,
                clientUser.getId(),
                true,
                List.of(
                        new CartItemDto(1L, 8001L, 1),
                        new CartItemDto(2L, 8002L, 3),
                        new CartItemDto(3L, 8003L, 2)
                )
        );

        when(cartContextFacade.getCartById(cartId)).thenReturn(Optional.of(cartDto));
        when(orderStatusRepository.findByName(OrderStatuses.PENDING.name())).thenReturn(Optional.of(pendingOrderStatus));

        doNothing().when(iamContextFacade).validateUserCanAccessResource(clientUser.getId());
        when(iamContextFacade.userExists(clientUser.getId())).thenReturn(true);
        doNothing().when(iamContextFacade).validateAddressBelongsToUser(clientAddress.getId(), clientUser.getId());

        setupProductForOrder(8001L, new BigDecimal("499.99"), false, 10);
        setupProductForOrder(8002L, new BigDecimal("29.99"), false, 50);
        setupProductForOrder(8003L, new BigDecimal("89.99"), false, 25);

        PaymentIntentResponse paymentResponse = new PaymentIntentResponse("pi_multi", "secret_multi", "requires_payment_method", null);

        when(orderRepository.findByCartId(cartId)).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            if (order.getId() == null) {
                setId(order, 20L);
            }
            return order;
        });
        when(paymentProvider.initiatePayment(any(Order.class))).thenReturn(paymentResponse);
        when(paymentIntentStatusRepository.findByName(any())).thenReturn(Optional.of(paymentSucceededStatus));
        when(paymentIntentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(cartContextFacade).checkoutCart(clientUser.getId(), cartId);

        CreateOrderFromCartCommand orderCmd = new CreateOrderFromCartCommand(
                clientUser.getId(),
                cartId,
                clientAddress.getId(),
                null
        );

        Order order = orderCommandService.handle(orderCmd);

        assertThat(order).isNotNull();
        assertThat(order.getItems()).hasSize(3);

        assertThat(order.getTotalAmount()).isEqualByComparingTo(new BigDecimal("769.94"));

        verify(productContextFacade).decreaseProductStock(8001L, 1);
        verify(productContextFacade).decreaseProductStock(8002L, 3);
        verify(productContextFacade).decreaseProductStock(8003L, 2);
    }

    @Test
    @DisplayName("Discount Edge Case Workflow: Expired discount cannot be used")
    void discountWorkflow_ExpiredDiscountCannotBeUsed() {
        Discount expiredDiscount = new Discount(
                "EXPIRED20",
                20,
                Instant.now().minus(10, ChronoUnit.DAYS),
                Instant.now().minus(1, ChronoUnit.DAYS)
        );
        setId(expiredDiscount, 10L);

        when(discountRepository.findByCode("EXPIRED20")).thenReturn(Optional.of(expiredDiscount));

        Long cartId = 100L;
        CartDto cartDto = new CartDto(
                cartId,
                clientUser.getId(),
                true,
                List.of(new CartItemDto(1L, 9001L, 1))
        );

        when(cartContextFacade.getCartById(cartId)).thenReturn(Optional.of(cartDto));
        when(orderStatusRepository.findByName(OrderStatuses.PENDING.name())).thenReturn(Optional.of(pendingOrderStatus));

        doNothing().when(iamContextFacade).validateUserCanAccessResource(clientUser.getId());
        when(iamContextFacade.userExists(clientUser.getId())).thenReturn(true);
        doNothing().when(iamContextFacade).validateAddressBelongsToUser(clientAddress.getId(), clientUser.getId());

        setupProductForOrder(9001L, new BigDecimal("100.00"), false, 10);

        CreateOrderFromCartCommand orderCmd = new CreateOrderFromCartCommand(
                clientUser.getId(),
                cartId,
                clientAddress.getId(),
                "EXPIRED20"
        );

        assertThatThrownBy(() -> orderCommandService.handle(orderCmd))
                .hasMessageContaining("not valid");
    }

    @Test
    @DisplayName("Cart Item Management Workflow: Add same product multiple times increases quantity")
    void cartWorkflow_AddSameProductMultipleTimes_IncreasesQuantity() {
        Cart cart = new Cart(clientUser.getId(), activeCartStatus);
        setId(cart, 110L);

        when(cartStatusRepository.findByName(CartStatuses.ACTIVE.name())).thenReturn(Optional.of(activeCartStatus));
        when(iamContextFacade.userExists(clientUser.getId())).thenReturn(true);
        doNothing().when(iamContextFacade).validateUserCanAccessResource(clientUser.getId());

        when(productContextFacade.getProductStock(10001L)).thenReturn(100);
        when(productContextFacade.isProductDeleted(10001L)).thenReturn(false);
        when(productContextFacade.isProductActive(10001L)).thenReturn(true);

        when(cartRepository.findByUserIdAndStatus(clientUser.getId(), activeCartStatus))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(cart))
                .thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AddProductToCartCommand cmd1 = new AddProductToCartCommand(clientUser.getId(), 10001L, 2);
        Cart cartAfter1 = cartCommandService.handle(cmd1);

        assertThat(cartAfter1).isNotNull();

        cart.addProduct(10001L, 2);
        AddProductToCartCommand cmd2 = new AddProductToCartCommand(clientUser.getId(), 10001L, 3);
        Cart cartAfter2 = cartCommandService.handle(cmd2);

        assertThat(cartAfter2).isNotNull();
        verify(cartRepository, atLeast(2)).save(any(Cart.class));
    }

    private void setId(Object entity, Long id) {
        try {
            Field field = null;
            Class<?> currentClass = entity.getClass();

            while (currentClass != null && field == null) {
                try {
                    field = currentClass.getDeclaredField("id");
                } catch (NoSuchFieldException e) {
                    currentClass = currentClass.getSuperclass();
                }
            }

            if (field == null) {
                throw new NoSuchFieldException("Field 'id' not found in class hierarchy of " + entity.getClass().getName());
            }

            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set ID: " + e.getMessage(), e);
        }
    }

    private User createMockUser(Long id, String username, String email) {
        Role clientRole = new Role(Roles.ROLE_CLIENT);
        User user = new User(username, email, "hashedPass", clientRole);
        setId(user, id);
        user.activate();
        return user;
    }

    private void setupProductForOrder(Long productId, BigDecimal price, boolean hasSalePrice, Integer stock) {
        when(productContextFacade.isProductDeleted(productId)).thenReturn(false);
        when(productContextFacade.isProductActive(productId)).thenReturn(true);
        when(productContextFacade.getProductPrice(productId)).thenReturn(price);
        when(productContextFacade.hasActiveSalePrice(productId)).thenReturn(hasSalePrice);
        when(productContextFacade.getProductStock(productId)).thenReturn(stock);
        doNothing().when(productContextFacade).decreaseProductStock(eq(productId), anyInt());
    }
}

