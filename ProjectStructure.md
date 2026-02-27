# E-Commerce Web API - Project Structure

This Spring Boot application follows **Domain-Driven Design (DDD)** principles with a modular architecture organized by bounded contexts.

## Overview

```
src/main/java/com/finalproject/ecommerce/ecommerce/
│
├── products/                        # PRODUCTS BOUNDED CONTEXT
├── orderspayments/                  # ORDERS & PAYMENTS BOUNDED CONTEXT
├── carts/                           # CARTS BOUNDED CONTEXT
├── iam/                             # IDENTITY & ACCESS MANAGEMENT CONTEXT
├── notifications/                   # NOTIFICATIONS BOUNDED CONTEXT
└── shared/                          # SHARED KERNEL (cross-cutting concerns)
```

---

## Bounded Contexts

### 1. Products Context (`products/`)

Manages the product catalog, inventory, categories, images, and user likes.

```
products/
│
├── domain/                          # DOMAIN LAYER (innermost)
│   ├── model/
│   │   ├── aggregates/              # Product (aggregate root)
│   │   ├── entities/                # ProductImage, ProductCategory, ProductLike
│   │   ├── commands/                # CreateProductCommand, UpdateProductCommand, etc.
│   │   ├── queries/                 # GetProductByIdQuery, GetProductsWithPaginationQuery
│   │   └── valueobjects/            # ProductName, Price, Stock
│   ├── services/                    # ProductCommandService, ProductQueryService
│   └── exceptions/                  # ProductNotFoundException, etc.
│
├── application/                     # APPLICATION LAYER
│   ├── internal/
│   │   ├── commandservices/         # ProductCommandServiceImpl
│   │   ├── queryservices/           # ProductQueryServiceImpl
│   │   └── eventhandlers/           # LowStockEventHandler, SalePriceEventHandler
│   ├── dto/                         # ProductResponse, ProductPageResponse, etc.
│   ├── ports/                       # Application service interfaces
│   └── acl/                         # Anti-Corruption Layer for external contexts
│
├── infrastructure/                  # INFRASTRUCTURE LAYER
│   ├── persistence/
│   │   └── jpa/
│   │       ├── repositories/        # ProductRepository, ProductSpecification
│   │       └── mappers/             # Entity-to-Domain mappers (if needed)
│   └── external/                    # External integrations (image storage, etc.)
│
└── interfaces/                      # PRESENTATION LAYER (outermost)
    ├── rest/                        # ProductsController, ProductRestMapper
    ├── graphql/                     # ProductQueryResolver, ProductMutationResolver
    └── acl/                         # ProductContextFacade (exposes services to other contexts)
```

**Key Entities:**
- `Product` (aggregate root)
- `ProductImage`, `ProductCategory`, `ProductLike` (entities)

---

### 2. Orders & Payments Context (`orderspayments/`)

Handles order creation, payment processing, delivery tracking, and discounts.

```
orderspayments/
│
├── domain/                          # DOMAIN LAYER
│   ├── model/
│   │   ├── aggregates/              # Order, PaymentIntent, Discount
│   │   ├── entities/                # OrderItem, OrderStatus, DeliveryStatus, etc.
│   │   ├── commands/                # CreateOrderCommand, UpdateDeliveryStatusCommand
│   │   └── queries/                 # GetOrderByIdQuery, GetUserOrdersQuery
│   ├── services/                    # OrderCommandService, OrderQueryService
│   └── exceptions/                  # OrderNotFoundException, InvalidOrderStateException
│
├── application/                     # APPLICATION LAYER
│   ├── internal/
│   │   ├── commandservices/         # OrderCommandServiceImpl, DiscountCommandServiceImpl
│   │   ├── queryservices/           # OrderQueryServiceImpl
│   │   └── eventhandlers/           # OrderStatusEventHandler
│   ├── ports/                       # PaymentService interface
│   └── acl/                         # Anti-Corruption Layer
│
├── infrastructure/                  # INFRASTRUCTURE LAYER
│   ├── payment/
│   │   └── stripe/                  # StripePaymentServiceImpl, StripeWebhookController
│   └── persistence/
│       └── jpa/
│           └── repositories/        # OrderRepository, DiscountRepository, etc.
│
└── interfaces/                      # PRESENTATION LAYER
    ├── rest/                        # OrdersController, DiscountsController
    ├── graphql/                     # OrderQueryResolver, OrderMutationResolver
    └── acl/                         # OrderContextFacade
```

**Key Entities:**
- `Order` (aggregate root)
- `OrderItem`, `PaymentIntent`, `Discount` (entities/aggregates)
- `OrderStatus`, `DeliveryStatus`, `PaymentIntentStatus` (value objects)

---

### 3. Carts Context (`carts/`)

Manages shopping cart functionality for users.

```
carts/
│
├── domain/                          # DOMAIN LAYER
│   ├── model/
│   │   ├── aggregates/              # Cart (aggregate root)
│   │   ├── entities/                # CartItem, CartStatus
│   │   ├── commands/                # AddItemToCartCommand, RemoveItemCommand
│   │   └── queries/                 # GetCartByUserIdQuery
│   ├── services/                    # CartCommandService, CartQueryService
│   └── exceptions/                  # CartNotFoundException, InvalidCartOperationException
│
├── application/                     # APPLICATION LAYER
│   ├── internal/
│   │   ├── commandservices/         # CartCommandServiceImpl
│   │   ├── queryservices/           # CartQueryServiceImpl
│   │   └── eventhandlers/           # CartEventHandler
│   └── acl/                         # Anti-Corruption Layer
│
├── infrastructure/                  # INFRASTRUCTURE LAYER
│   └── persistence/
│       └── jpa/
│           └── repositories/        # CartRepository, CartItemRepository
│
└── interfaces/                      # PRESENTATION LAYER
    ├── rest/                        # CartsController
    ├── graphql/                     # CartQueryResolver, CartMutationResolver
    └── acl/                         # CartContextFacade
```

**Key Entities:**
- `Cart` (aggregate root)
- `CartItem` (entity)
- `CartStatus` (value object)

---

### 4. IAM Context (`iam/`)

Identity and Access Management - handles authentication, authorization, and user management.

```
iam/
│
├── domain/                          # DOMAIN LAYER
│   ├── model/
│   │   ├── aggregates/              # User, RefreshToken
│   │   ├── entities/                # Role, UserToken, Address
│   │   ├── commands/                # SignUpCommand, SignInCommand, ResetPasswordCommand
│   │   ├── queries/                 # GetUserByIdQuery, GetUserByUsernameQuery
│   │   └── validators/              # RavenEmailValidator
│   ├── services/                    # UserCommandService, UserQueryService
│   └── exceptions/                  # UserNotFoundException, InvalidCredentialsException
│
├── application/                     # APPLICATION LAYER
│   ├── internal/
│   │   ├── commandservices/         # UserCommandServiceImpl, RefreshTokenCommandServiceImpl
│   │   └── queryservices/           # UserQueryServiceImpl
│   └── acl/                         # IamContextFacade (validates permissions)
│
├── infrastructure/                  # INFRASTRUCTURE LAYER
│   ├── authorization/
│   │   └── sfs/                     # Spring Security configuration
│   │       ├── configuration/       # WebSecurityConfiguration
│   │       ├── model/               # UsernamePasswordAuthenticationTokenBuilder
│   │       └── pipeline/            # BearerAuthorizationRequestFilter
│   ├── hashing/                     # BCryptPasswordHashingService
│   ├── tokens/
│   │   └── jwt/                     # JwtTokenService implementation
│   └── persistence/
│       └── jpa/
│           └── repositories/        # UserRepository, RoleRepository, AddressRepository
│
└── interfaces/                      # PRESENTATION LAYER
    ├── rest/                        # AuthenticationController, UsersController, AddressesController
    └── acl/                         # IamContextFacade (exposed to other contexts)
```

**Key Entities:**
- `User` (aggregate root)
- `RefreshToken` (aggregate)
- `Role`, `UserToken`, `Address` (entities)

---

### 5. Notifications Context (`notifications/`)

Handles email notifications (order updates, password reset, low stock alerts, etc.)

```
notifications/
│
├── domain/                          # DOMAIN LAYER
│   ├── model/
│   │   ├── commands/                # SendEmailCommand
│   │   └── valueobjects/            # EmailAddress, EmailTemplate
│   ├── services/                    # EmailService interface
│   └── exceptions/                  # EmailSendException
│
├── application/                     # APPLICATION LAYER
│   ├── internal/
│   │   ├── commandservices/         # EmailCommandServiceImpl
│   │   └── eventhandlers/           # OrderStatusNotificationHandler, etc.
│   └── acl/                         # Anti-Corruption Layer
│
├── infrastructure/                  # INFRASTRUCTURE LAYER
│   ├── email/                       # JavaMailSenderEmailService
│   └── templates/                   # Thymeleaf template rendering
│
└── interfaces/                      # PRESENTATION LAYER
    └── acl/                         # NotificationContextFacade
```

**Key Services:**
- `EmailService` (sends transactional emails)
- Event handlers for order status, low stock, password reset, etc.

---

### 6. Shared Kernel (`shared/`)

Cross-cutting concerns shared across all bounded contexts.

```
shared/
│
├── domain/                          # DOMAIN LAYER
│   ├── model/                       # Base domain entities, value objects
│   │   ├── aggregates/              # AuditableAbstractAggregateRoot
│   │   └── entities/                # AuditableModel
│   └── events/                      # DomainEvent, EventPublisher
│
├── infrastructure/                  # INFRASTRUCTURE LAYER
│   ├── cache/                       # Redis cache configuration
│   ├── configuration/               # AppConfig, CorsConfig, AsyncConfig
│   ├── documentation/               # OpenAPI/Swagger configuration
│   ├── exception/                   # GlobalExceptionHandler, ErrorResponse
│   ├── persistence/                 # JPA auditing, base repositories
│   └── ratelimit/                   # Redis-based rate limiting
│
└── interfaces/                      # PRESENTATION LAYER
    └── rest/                        # Base REST controllers, common filters
```

**Key Components:**
- `AuditableAbstractAggregateRoot` (base class for all aggregates)
- `GlobalExceptionHandler` (centralized error handling)
- `RedisConfig` (caching configuration)
- `OpenApiConfiguration` (Swagger documentation)

---

## Resources Structure

```
src/main/resources/
│
├── application.properties          # Application configuration
│   - Database connection
│   - Redis configuration
│   - JWT settings
│   - Email SMTP settings
│   - Stripe API keys
│
├── graphql/                         # GraphQL schema definitions
│   ├── schema.graphqls              # Main schema
│   ├── carts/
│   │   └── carts.graphqls
│   ├── orderspayments/
│   │   └── orderspayments.graphqls
│   ├── products/
│   │   └── products.graphqls
│   └── shared/
│       └── shared.graphqls
│
└── templates/                       # Email templates (Thymeleaf)
    └── emails/
        ├── discount-alert.html
        ├── low-stock-alert.html
        ├── order-status-update.html
        ├── password-changed.html
        ├── password-reset.html
        └── welcome.html
```

---

## Architecture Layers (per Bounded Context)

Each bounded context follows a **4-layer architecture** based on DDD principles:

### 1. **Domain Layer** (innermost)
- **Pure business logic** - no dependencies on frameworks
- Contains:
  - **Aggregates**: Root entities managing consistency boundaries
  - **Entities**: Domain objects with identity
  - **Value Objects**: Immutable objects without identity
  - **Commands**: Intent to change state
  - **Queries**: Intent to retrieve data
  - **Domain Services**: Business logic that doesn't fit in entities
  - **Domain Exceptions**: Business rule violations

### 2. **Application Layer**
- **Orchestrates** domain logic
- Contains:
  - **Command Handlers**: Execute commands, update aggregates
  - **Query Handlers**: Retrieve and transform data
  - **Event Handlers**: React to domain events
  - **DTOs**: Data Transfer Objects for API contracts
  - **Ports**: Interfaces for external dependencies (repositories, services)
  - **ACL**: Anti-Corruption Layer to protect domain from external changes

### 3. **Infrastructure Layer**
- **Technical implementations** of ports
- Contains:
  - **Repository Implementations**: JPA/database access
  - **External Service Integrations**: Stripe, email services
  - **Framework Configurations**: Spring Security, Redis, etc.

### 4. **Interfaces Layer** (outermost)
- **Exposes** the application to the outside world
- Contains:
  - **REST Controllers**: HTTP API endpoints
  - **GraphQL Resolvers**: GraphQL queries and mutations
  - **Mappers**: Transform DTOs to/from domain models
  - **Facades**: Expose services to other bounded contexts (ACL)

---

## Communication Between Contexts

Bounded contexts communicate through:

1. **ACL Facades** (`interfaces/acl/`)
   - `IamContextFacade` - used by other contexts to validate permissions
   - `ProductContextFacade` - exposes product data
   - `OrderContextFacade` - exposes order operations

2. **Domain Events** (async)
   - `LowStockEvent` → triggers email notification
   - `OrderStatusChangedEvent` → triggers email notification
   - `SalePriceExpiringEvent` → triggers price update

---

