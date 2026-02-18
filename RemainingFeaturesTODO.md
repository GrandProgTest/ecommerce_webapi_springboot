# Remaining Mandatory Features TODO

## Analysis Summary
Based on the README requirements, here's the status of mandatory features:

---

## ✅ COMPLETED FEATURES

### 1. Authentication System (REST) - ✅ DONE
- ✅ Sign up (with email activation)
- ✅ Sign in
- ✅ Sign out
- ✅ Forgot password
- ✅ Reset password
- ✅ Rate limiting on password reset endpoint
- ✅ JWT authentication with refresh tokens
- ✅ Secure password hashing

### 2. Product Catalog (REST) - ✅ DONE
- ✅ List products with pagination
- ✅ Search products by category
- ✅ Sorting options (price, name, date)
- ✅ Public access (no authentication required)

### 3. User Roles - ✅ DONE
- ✅ Manager role (ROLE_MANAGER) with full CRUD
- ✅ Client role (ROLE_CLIENT) for purchasing
- ✅ Role-based authorization (@PreAuthorize)
- ✅ Automatic role assignment based on email domain

### 4. Email Notification System - ✅ DONE
- ✅ Password change notification
- ✅ Order status updates
- ✅ Low stock alerts for liked products
- ✅ Welcome emails
- ✅ Password reset emails
- ✅ Async email processing
- ✅ HTML templates

### 5. Manager Capabilities - ✅ PARTIALLY DONE (REST only)
**REST - DONE:**
- ✅ Create products
- ✅ Update products
- ✅ Delete products (hard delete)
- ✅ Soft delete products
- ✅ Disable/Activate products
- ✅ View client orders with pagination
- ✅ Upload product images (multiple)
- ✅ Update delivery status

**GraphQL - MISSING:** See section below

### 6. Client Capabilities - ✅ PARTIALLY DONE
**REST - DONE:**
- ✅ View products
- ✅ View product details
- ✅ Buy products (create order)

**GraphQL - MISSING:**
- ✅ Add products to cart (GraphQL implemented)
- ❌ Like/Unlike products (GraphQL mutation missing)
- ❌ View my orders with pagination (GraphQL query missing)
- ❌ Track order status (GraphQL query missing)

### 7. Public Product Visibility - ✅ DONE
- ✅ Products accessible to authenticated and non-authenticated users
- ✅ Images publicly accessible

### 8. Stripe Payment Integration - ✅ DONE
- ✅ Create payment intent
- ✅ Handle successful payment webhook
- ✅ Handle failed payment webhook
- ✅ Webhook signature verification
- ✅ Idempotent webhook handling
- ✅ No card details stored locally

### 9. Security Configurations - ✅ PARTIALLY DONE
- ✅ CORS configuration
- ✅ Rate limiting (password reset)
- ✅ Global exception handling
- ✅ Bean validations
- ✅ Custom annotations
- ❌ **Schema validation for environment variables** - NOT IMPLEMENTED

---

## ❌ MISSING MANDATORY FEATURES

### 1. **Environment Variable Validation** (CRITICAL - Required)
**Status:** NOT IMPLEMENTED
**Effort:** 2-3 hours
**Description:** Need to implement schema validation for all environment variables at startup
**Implementation needed:**
- Create configuration classes with `@ConfigurationProperties`
- Add `@Validated` annotations
- Add validation constraints (`@NotBlank`, `@NotNull`, etc.)
- Validate: Database credentials, JWT secret, Stripe keys, Cloudinary keys, Email credentials

**Files to create/modify:**
- `DatabaseConfig.java` - PostgreSQL configuration
- `JwtConfig.java` - JWT configuration
- `StripeConfig.java` - Stripe configuration
- `CloudinaryConfig.java` - Already exists, needs validation
- `EmailConfig.java` - Email configuration
- Add `spring-boot-configuration-processor` dependency

---

### 2. **GraphQL Implementations for Products** (Required by README)
**Status:** NOT IMPLEMENTED
**Effort:** 6-8 hours
**Description:** README specifies GraphQL must be used for certain operations

**Missing implementations:**
- ❌ Delete products (GraphQL mutation)
- ❌ Disable products (GraphQL mutation)
- ❌ Activate products (GraphQL mutation)

**Files to create:**
- `ProductMutationResolver.java`
- `ProductQueryResolver.java`

**GraphQL schema already exists** in `products.graphqls` - just need Java resolvers

---

### 3. **GraphQL Implementations for Orders** (Required by README)
**Status:** PARTIALLY IMPLEMENTED
**Effort:** 8-10 hours
**Description:** README requires GraphQL for order management

**Missing implementations:**
- ❌ Order queries (GraphQL)
- ❌ Order mutations (GraphQL)
- ❌ Update delivery status (GraphQL mutation)

**Files to create:**
- `OrderQueryResolver.java`
- `OrderMutationResolver.java`

**GraphQL schema already exists** in `orderspayments.graphqls` - just need Java resolvers

---

### 4. **GraphQL Implementation for Product Likes** (Required by README)
**Status:** REST ONLY
**Effort:** 2-3 hours
**Description:** README specifies "Like products (mutation)" should be in GraphQL

**Currently implemented in REST:**
- Endpoint exists: `POST /api/v1/products/users/{userId}/products/{productId}/like`

**Need to add:**
- GraphQL mutation: `likeProduct(productId: ID!): Boolean!`
- GraphQL mutation: `unlikeProduct(productId: ID!): Boolean!`

**Files to modify:**
- Create `ProductMutationResolver.java` (or add to it if created for #2)

---

### 5. **Discount Alert Email for Liked Products** (Required by README)
**Status:** NOT IMPLEMENTED
**Effort:** 4-5 hours
**Description:** When a liked product gets a discount, notify users

**Implementation needed:**
- Monitor product price changes
- Track liked products per user
- Send discount alert emails when price drops
- Async batch email sending

**Files to create/modify:**
- Add discount tracking to Product entity
- Create event handler for product price changes
- Add email template for discount alerts (may already exist)
- Add `sendDiscountAlert` method to NotificationContextFacade

---

## 📊 TOTAL REMAINING EFFORT ESTIMATE

| Feature | Hours | Priority | Status |
|---------|-------|----------|--------|
| Environment Variable Validation | ~~2-3~~ | ~~CRITICAL~~ | ✅ COMPLETED |
| GraphQL Products (Delete/Disable/Activate) | 6-8 | HIGH | ⏳ PENDING |
| GraphQL Orders (All operations) | 8-10 | HIGH | ⏳ PENDING |
| GraphQL Product Likes | 2-3 | MEDIUM | ⏳ PENDING |
| Discount Alert Emails | 4-5 | MEDIUM | ⏳ PENDING |
| **COMPLETED** | **3** | | |
| **REMAINING** | **19-26 hours** | | |

---

## ⏰ CAN THIS BE DONE IN UNDER 20 HOURS?

**Answer: YES, definitely achievable**

### Completed (3 hours):
1. ✅ **Environment Variable Validation** (3 hours) - DONE

### Remaining Work (16-23 hours):
2. **GraphQL Products** (6 hours) - MUST DO (README requirement)
3. **GraphQL Orders** (8 hours) - MUST DO (README requirement)
4. **GraphQL Product Likes** (2 hours) - MUST DO (README requirement)
5. **Discount Alert** (3 hours minimum implementation) - Can be simplified

### What can be simplified:
- **Discount alerts**: Implement basic version without complex price monitoring
- **GraphQL**: Focus on core mutations/queries, skip advanced features
- **Validation**: Start with critical environment variables only

### Recommended approach:
1. Start with environment validation (quick win)
2. Implement GraphQL resolvers (most critical as per README)
3. Add discount alerts as time permits
4. Test thoroughly as you go

---

## 🔧 NEXT STEPS

1. **Confirm priorities with stakeholder**
2. **Set up environment variable validation first** (prevents runtime errors)
3. **Implement GraphQL resolvers** (biggest requirement)
4. **Add discount alerts** if time permits
5. **Comprehensive testing**

---

## 📝 NOTES

- Most infrastructure is already in place
- GraphQL schemas already defined, just need Java resolvers
- Email service is robust and ready for discount alerts
- The project is well-architected, making additions straightforward
- With focused effort, all mandatory features can be completed in 20-25 hours

