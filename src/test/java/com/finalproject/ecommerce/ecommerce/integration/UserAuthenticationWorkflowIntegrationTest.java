package com.finalproject.ecommerce.ecommerce.integration;

import com.finalproject.ecommerce.ecommerce.iam.application.internal.commandservices.AddressCommandServiceImpl;
import com.finalproject.ecommerce.ecommerce.iam.application.internal.commandservices.UserCommandServiceImpl;
import com.finalproject.ecommerce.ecommerce.iam.application.internal.outboundservices.hashing.HashingService;
import com.finalproject.ecommerce.ecommerce.iam.application.internal.outboundservices.tokens.TokenService;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.aggregates.User;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.*;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.entities.Address;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.entities.Role;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.entities.UserToken;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.valueobjects.Roles;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.valueobjects.TokenType;
import com.finalproject.ecommerce.ecommerce.iam.domain.services.PermissionValidationService;
import com.finalproject.ecommerce.ecommerce.iam.domain.services.RefreshTokenCommandService;
import com.finalproject.ecommerce.ecommerce.iam.infrastructure.persistence.jpa.repositories.AccountActivationTokenRepository;
import com.finalproject.ecommerce.ecommerce.iam.infrastructure.persistence.jpa.repositories.AddressRepository;
import com.finalproject.ecommerce.ecommerce.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.finalproject.ecommerce.ecommerce.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.acl.IamContextFacade;
import com.finalproject.ecommerce.ecommerce.notifications.interfaces.acl.NotificationContextFacade;
import com.finalproject.ecommerce.ecommerce.products.application.internal.commandservices.ProductCommandServiceImpl;
import com.finalproject.ecommerce.ecommerce.products.application.internal.queryservices.ProductQueryServiceImpl;
import com.finalproject.ecommerce.ecommerce.products.domain.model.aggregates.Product;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.CreateProductCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.ToggleProductLikeCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.queries.GetProductByIdQuery;
import com.finalproject.ecommerce.ecommerce.products.infrastructure.persistence.jpa.repositories.CategoryRepository;
import com.finalproject.ecommerce.ecommerce.products.infrastructure.persistence.jpa.repositories.ProductRepository;
import com.finalproject.ecommerce.ecommerce.shared.infrastructure.ratelimit.RateLimiterService;
import org.apache.commons.lang3.tuple.ImmutablePair;
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
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("User Authentication and Product Interaction Workflow Tests")
class UserAuthenticationWorkflowIntegrationTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private HashingService hashingService;
    @Mock private TokenService tokenService;
    @Mock private RefreshTokenCommandService refreshTokenCommandService;
    @Mock private AccountActivationTokenRepository accountActivationTokenRepository;
    @Mock private NotificationContextFacade notificationContextFacade;
    @Mock private RateLimiterService rateLimiterService;
    @Mock private AddressRepository addressRepository;
    @Mock private PermissionValidationService permissionValidationService;
    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private IamContextFacade iamContextFacade;

    private UserCommandServiceImpl userCommandService;
    private AddressCommandServiceImpl addressCommandService;
    private ProductCommandServiceImpl productCommandService;
    private ProductQueryServiceImpl productQueryService;

    private Role clientRole;
    private Role managerRole;
    private String appBaseUrl = "http://localhost:8080";

    @BeforeEach
    void setUp() {
        userCommandService = new UserCommandServiceImpl(
                userRepository,
                hashingService,
                tokenService,
                roleRepository,
                refreshTokenCommandService,
                accountActivationTokenRepository,
                notificationContextFacade,
                rateLimiterService,
                appBaseUrl
        );

        addressCommandService = new AddressCommandServiceImpl(
                addressRepository,
                userRepository,
                permissionValidationService
        );

        productCommandService = new ProductCommandServiceImpl(
                productRepository,
                categoryRepository,
                null,
                null,
                iamContextFacade,
                null,
                null
        );

        productQueryService = new ProductQueryServiceImpl(productRepository);

        clientRole = new Role(Roles.ROLE_CLIENT);
        managerRole = new Role(Roles.ROLE_MANAGER);

        doNothing().when(iamContextFacade).validateUserCanAccessResource(anyLong());
        when(rateLimiterService.isAllowed(anyString(), anyString())).thenReturn(true);
    }

    @Test
    @DisplayName("Authentication Workflow: User signs up and activates account")
    void authWorkflow_SignUp_ActivateAccount() {
        String username = "newuser";
        String email = "newuser@example.com";
        String rawPassword = "SecurePass123!";
        String hashedPassword = "hashedSecurePass123";

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(roleRepository.findByName(Roles.ROLE_CLIENT)).thenReturn(Optional.of(clientRole));
        when(hashingService.encode(rawPassword)).thenReturn(hashedPassword);
        when(hashingService.encode(anyString())).thenAnswer(invocation -> "hashed_" + invocation.getArgument(0));

        User newUser = new User(username, email, hashedPassword, clientRole);
        setId(newUser, 1L);

        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(newUser));

        String rawActivationToken = UUID.randomUUID().toString();
        when(accountActivationTokenRepository.save(any(UserToken.class))).thenAnswer(invocation -> {
            UserToken token = invocation.getArgument(0);
            setId(token, 1L);
            return token;
        });

        doNothing().when(notificationContextFacade).sendWelcomeEmail(anyString(), anyString(), anyString());

        SignUpCommand signUpCmd = new SignUpCommand(username, email, rawPassword);
        Optional<User> signedUpUser = userCommandService.handle(signUpCmd);

        assertThat(signedUpUser).isPresent();
        assertThat(signedUpUser.get().getUsername()).isEqualTo(username);
        assertThat(signedUpUser.get().getEmail()).isEqualTo(email);
        assertThat(signedUpUser.get().getIsActive()).isFalse();

        verify(userRepository).save(any(User.class));
        verify(accountActivationTokenRepository).save(any(UserToken.class));
        verify(notificationContextFacade).sendWelcomeEmail(eq(email), eq(username), contains("activate"));

        UserToken activationToken = new UserToken(
                newUser,
                "hashedToken123",
                Date.from(Instant.now().plus(24, ChronoUnit.HOURS)),
                TokenType.ACCOUNT_ACTIVATION
        );
        setId(activationToken, 1L);

        when(accountActivationTokenRepository.findByTokenTypeAndIsUsedFalse(TokenType.ACCOUNT_ACTIVATION))
                .thenReturn(List.of(activationToken));
        when(hashingService.matches(eq(rawActivationToken), eq("hashedToken123"))).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(newUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.activate();
            return user;
        });
        when(accountActivationTokenRepository.save(any(UserToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ActivateAccountCommand activateCmd = new ActivateAccountCommand(rawActivationToken);
        boolean activated = userCommandService.handle(activateCmd);

        assertThat(activated).isTrue();
        verify(accountActivationTokenRepository).save(argThat(token -> token.getIsUsed()));
    }

    @Test
    @DisplayName("Authentication Workflow: Complete sign up, activate, and sign in flow")
    void authWorkflow_SignUp_Activate_SignIn_Complete() {
        String username = "fullflowuser";
        String email = "fullflow@example.com";
        String rawPassword = "FullFlow123!";
        String hashedPassword = "hashedFullFlow123";

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(roleRepository.findByName(Roles.ROLE_CLIENT)).thenReturn(Optional.of(clientRole));
        when(hashingService.encode(rawPassword)).thenReturn(hashedPassword);
        when(hashingService.encode(anyString())).thenAnswer(invocation -> "hashed_" + invocation.getArgument(0));

        User user = new User(username, email, hashedPassword, clientRole);
        setId(user, 2L);

        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(accountActivationTokenRepository.save(any(UserToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(notificationContextFacade).sendWelcomeEmail(anyString(), anyString(), anyString());

        SignUpCommand signUpCmd = new SignUpCommand(username, email, rawPassword);
        Optional<User> signedUpUser = userCommandService.handle(signUpCmd);

        assertThat(signedUpUser).isPresent();

        String rawActivationToken = UUID.randomUUID().toString();
        UserToken activationToken = new UserToken(
                user,
                "hashedActivationToken",
                Date.from(Instant.now().plus(24, ChronoUnit.HOURS)),
                TokenType.ACCOUNT_ACTIVATION
        );

        when(accountActivationTokenRepository.findByTokenTypeAndIsUsedFalse(TokenType.ACCOUNT_ACTIVATION))
                .thenReturn(List.of(activationToken));
        when(hashingService.matches(eq(rawActivationToken), eq("hashedActivationToken"))).thenReturn(true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.activate();
            return u;
        });

        ActivateAccountCommand activateCmd = new ActivateAccountCommand(rawActivationToken);
        boolean activated = userCommandService.handle(activateCmd);

        assertThat(activated).isTrue();

        user.activate();
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(hashingService.matches(rawPassword, hashedPassword)).thenReturn(true);
        when(tokenService.generateToken(username)).thenReturn("jwt_access_token_123");
        when(refreshTokenCommandService.createRefreshToken(user)).thenReturn("refresh_token_123");

        SignInCommand signInCmd = new SignInCommand(username, rawPassword);
        Optional<ImmutablePair<ImmutablePair<User, String>, String>> signInResult = userCommandService.handle(signInCmd);

        assertThat(signInResult).isPresent();
        assertThat(signInResult.get().getLeft().getLeft().getUsername()).isEqualTo(username);
        assertThat(signInResult.get().getLeft().getRight()).isEqualTo("jwt_access_token_123");
        assertThat(signInResult.get().getRight()).isEqualTo("refresh_token_123");

        verify(tokenService).generateToken(username);
        verify(refreshTokenCommandService).createRefreshToken(user);
    }

    @Test
    @DisplayName("User Workflow: User creates address for delivery")
    void userWorkflow_CreateAddress_ForDelivery() {
        User user = new User("userwithdress", "address@example.com", "hashedPass", clientRole);
        setId(user, 3L);
        user.activate();

        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        doNothing().when(permissionValidationService).validateUserCanAccessResource(3L);

        CreateAddressCommand addressCmd = new CreateAddressCommand(
                "456 Oak Avenue",
                "Los Angeles",
                "CA",
                "USA",
                "90001",
                true
        );

        Address address = new Address(addressCmd, user);
        setId(address, 1L);

        when(addressRepository.findByUser(user)).thenReturn(List.of());
        when(addressRepository.save(any(Address.class))).thenReturn(address);

        Address createdAddress = addressCommandService.handle(addressCmd, user.getId());

        assertThat(createdAddress).isNotNull();
        assertThat(createdAddress.getStreet()).isEqualTo("456 Oak Avenue");
        assertThat(createdAddress.getCity()).isEqualTo("Los Angeles");
        assertThat(createdAddress.getIsDefault()).isTrue();

        verify(addressRepository).save(any(Address.class));
    }

    @Test
    @DisplayName("User Workflow: User creates multiple addresses, sets one as default")
    void userWorkflow_MultipleAddresses_SetDefault() {
        User user = new User("multiaddress", "multi@example.com", "hashedPass", clientRole);
        setId(user, 4L);
        user.activate();

        when(userRepository.findById(4L)).thenReturn(Optional.of(user));
        doNothing().when(permissionValidationService).validateUserCanAccessResource(4L);

        CreateAddressCommand address1Cmd = new CreateAddressCommand(
                "100 First St",
                "New York",
                "NY",
                "USA",
                "10001",
                true
        );

        Address address1 = new Address(address1Cmd, user);
        setId(address1, 1L);

        when(addressRepository.findByUser(user)).thenReturn(List.of());
        when(addressRepository.save(any(Address.class))).thenReturn(address1);

        Address firstAddress = addressCommandService.handle(address1Cmd, user.getId());

        assertThat(firstAddress.getIsDefault()).isTrue();

        CreateAddressCommand address2Cmd = new CreateAddressCommand(
                "200 Second Ave",
                "Boston",
                "MA",
                "USA",
                "02101",
                true
        );

        Address address2 = new Address(address2Cmd, user);
        setId(address2, 2L);

        when(addressRepository.findByUser(user)).thenReturn(List.of(address1));
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Address secondAddress = addressCommandService.handle(address2Cmd, user.getId());

        assertThat(secondAddress.getIsDefault()).isTrue();
        verify(addressRepository, atLeast(2)).save(any(Address.class));
    }

    @Test
    @DisplayName("Product Workflow: User likes and unlikes products")
    void productWorkflow_LikeAndUnlikeProduct() {
        User user = new User("likeuser", "like@example.com", "hashedPass", clientRole);
        setId(user, 5L);
        user.activate();

        CreateProductCommand productCmd = new CreateProductCommand(
                "Likeable Product",
                "A product users can like",
                new BigDecimal("99.99"),
                50,
                List.of(1L),
                true
        );

        Product product = new Product(productCmd, 10L);
        setId(product, 5001L);

        when(productRepository.findById(5001L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ToggleProductLikeCommand likeCmd = new ToggleProductLikeCommand(5L, 5001L);
        boolean isLikedAfterFirst = productCommandService.handle(likeCmd);

        assertThat(isLikedAfterFirst).isTrue();
        assertThat(product.getLikes()).hasSize(1);
        verify(productRepository).save(any(Product.class));

        boolean isLikedAfterSecond = productCommandService.handle(likeCmd);

        assertThat(isLikedAfterSecond).isFalse();
        verify(productRepository, times(2)).save(any(Product.class));
    }

    @Test
    @DisplayName("Product Workflow: Multiple users like same product")
    void productWorkflow_MultipleUsersLikeSameProduct() {
        User user1 = createMockUser(10L, "user1", "user1@example.com");
        User user2 = createMockUser(11L, "user2", "user2@example.com");
        User user3 = createMockUser(12L, "user3", "user3@example.com");

        CreateProductCommand productCmd = new CreateProductCommand(
                "Popular Product",
                "Many users like this",
                new BigDecimal("199.99"),
                100,
                List.of(1L),
                true
        );

        Product product = new Product(productCmd, 20L);
        setId(product, 6001L);

        when(productRepository.findById(6001L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ToggleProductLikeCommand like1 = new ToggleProductLikeCommand(10L, 6001L);
        ToggleProductLikeCommand like2 = new ToggleProductLikeCommand(11L, 6001L);
        ToggleProductLikeCommand like3 = new ToggleProductLikeCommand(12L, 6001L);

        boolean user1Liked = productCommandService.handle(like1);
        boolean user2Liked = productCommandService.handle(like2);
        boolean user3Liked = productCommandService.handle(like3);

        assertThat(user1Liked).isTrue();
        assertThat(user2Liked).isTrue();
        assertThat(user3Liked).isTrue();
        assertThat(product.getLikes()).hasSize(3);

        verify(productRepository, times(3)).save(any(Product.class));
    }

    @Test
    @DisplayName("Password Reset Workflow: Request reset and change password")
    void passwordResetWorkflow_RequestAndReset() {
        String email = "resetuser@example.com";
        User user = new User("resetuser", email, "oldHashedPass", clientRole);
        setId(user, 6L);
        user.activate();

        when(rateLimiterService.isAllowed(anyString(), anyString())).thenReturn(true);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(accountActivationTokenRepository.findByUser_IdAndTokenTypeAndIsUsedFalse(6L, TokenType.PASSWORD_RESET))
                .thenReturn(List.of());

        String rawResetToken = UUID.randomUUID().toString();
        String hashedResetToken = "hashedResetToken123";
        when(hashingService.encode(anyString())).thenAnswer(invocation -> "hashed_" + invocation.getArgument(0));

        UserToken resetToken = new UserToken(
                user,
                hashedResetToken,
                Date.from(Instant.now().plus(15, ChronoUnit.MINUTES)),
                TokenType.PASSWORD_RESET
        );

        when(accountActivationTokenRepository.save(any(UserToken.class))).thenReturn(resetToken);
        doNothing().when(notificationContextFacade).sendPasswordResetEmail(anyString(), anyString(), anyString(), anyInt());

        ForgotPasswordCommand forgotCmd = new ForgotPasswordCommand(email);
        boolean resetRequested = userCommandService.handle(forgotCmd);

        assertThat(resetRequested).isTrue();
        verify(accountActivationTokenRepository).save(any(UserToken.class));
        verify(notificationContextFacade).sendPasswordResetEmail(eq(email), eq(user.getUsername()), anyString(), anyInt());

        String newPassword = "NewSecurePass123!";
        String newHashedPassword = "hashedNewSecurePass123";


        when(accountActivationTokenRepository.findByTokenTypeAndIsUsedFalse(TokenType.PASSWORD_RESET))
                .thenReturn(List.of(resetToken));
        when(hashingService.matches(eq(rawResetToken), eq(hashedResetToken))).thenReturn(true);
        when(userRepository.findById(6L)).thenReturn(Optional.of(user));
        when(hashingService.encode(newPassword)).thenReturn(newHashedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(accountActivationTokenRepository.save(any(UserToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(notificationContextFacade).sendPasswordChangedEmail(anyString(), anyString(), anyString());

        ResetPasswordCommand resetCmd = new ResetPasswordCommand(rawResetToken, newPassword, newPassword);
        boolean passwordReset = userCommandService.handle(resetCmd);

        assertThat(passwordReset).isTrue();
        verify(hashingService).encode(newPassword);
        verify(notificationContextFacade).sendPasswordChangedEmail(eq(email), eq(user.getUsername()), anyString());
    }

    @Test
    @DisplayName("Complete E-commerce Workflow: Sign up, create address, like products, add to cart simulation")
    void completeEcommerceWorkflow_SignUpToLikingProducts() {
        String username = "completeclient";
        String email = "complete@example.com";
        String password = "Complete123!";
        String hashedPassword = "hashedComplete123";

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(roleRepository.findByName(Roles.ROLE_CLIENT)).thenReturn(Optional.of(clientRole));
        when(hashingService.encode(password)).thenReturn(hashedPassword);
        when(hashingService.encode(anyString())).thenAnswer(invocation -> "hashed_" + invocation.getArgument(0));

        User user = new User(username, email, hashedPassword, clientRole);
        setId(user, 7L);
        user.activate();

        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(accountActivationTokenRepository.save(any(UserToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(notificationContextFacade).sendWelcomeEmail(anyString(), anyString(), anyString());

        SignUpCommand signUpCmd = new SignUpCommand(username, email, password);
        Optional<User> signedUpUser = userCommandService.handle(signUpCmd);

        assertThat(signedUpUser).isPresent();

        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        doNothing().when(permissionValidationService).validateUserCanAccessResource(7L);

        CreateAddressCommand addressCmd = new CreateAddressCommand(
                "789 Commerce Blvd",
                "Chicago",
                "IL",
                "USA",
                "60601",
                true
        );

        Address address = new Address(addressCmd, user);
        setId(address, 10L);

        when(addressRepository.findByUser(user)).thenReturn(List.of());
        when(addressRepository.save(any(Address.class))).thenReturn(address);

        Address userAddress = addressCommandService.handle(addressCmd, user.getId());

        assertThat(userAddress).isNotNull();
        assertThat(userAddress.getCity()).isEqualTo("Chicago");

        CreateProductCommand product1Cmd = new CreateProductCommand(
                "Smart Watch",
                "Fitness tracker with GPS",
                new BigDecimal("299.99"),
                75,
                List.of(1L),
                true
        );

        Product product1 = new Product(product1Cmd, 20L);
        setId(product1, 7001L);

        when(productRepository.findById(7001L)).thenReturn(Optional.of(product1));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ToggleProductLikeCommand likeCmd = new ToggleProductLikeCommand(7L, 7001L);
        boolean liked = productCommandService.handle(likeCmd);

        assertThat(liked).isTrue();
        assertThat(product1.getLikes()).hasSize(1);

        verify(userRepository).save(any(User.class));
        verify(addressRepository).save(any(Address.class));
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Authentication Workflow: Cannot sign in with inactive account")
    void authWorkflow_CannotSignInWithInactiveAccount() {
        String username = "inactiveuser";
        String password = "Password123!";

        User inactiveUser = new User(username, "inactive@example.com", "hashedPass", clientRole);
        setId(inactiveUser, 8L);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(inactiveUser));

        SignInCommand signInCmd = new SignInCommand(username, password);
        assertThatThrownBy(() -> userCommandService.handle(signInCmd))
                .hasMessageContaining("not activated");
    }

    @Test
    @DisplayName("Authentication Workflow: Rate limiting on password reset")
    void authWorkflow_RateLimitingOnPasswordReset() {
        String email = "ratelimit@example.com";

        when(rateLimiterService.isAllowed(email, "forgot-password")).thenReturn(false);
        when(rateLimiterService.getSecondsUntilReset(email, "forgot-password")).thenReturn(30L);

        ForgotPasswordCommand forgotCmd = new ForgotPasswordCommand(email);
        assertThatThrownBy(() -> userCommandService.handle(forgotCmd))
                .hasMessageContaining("Too many");

        verify(rateLimiterService).isAllowed(email, "forgot-password");
    }

    @Test
    @DisplayName("User Workflow: Sign in with wrong password fails")
    void authWorkflow_SignInWithWrongPassword_Fails() {
        String username = "securuser";
        String correctPassword = "CorrectPass123!";
        String wrongPassword = "WrongPass123!";
        String hashedCorrectPassword = "hashedCorrectPass";

        User user = new User(username, "secure@example.com", hashedCorrectPassword, clientRole);
        setId(user, 9L);
        user.activate();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(hashingService.matches(wrongPassword, hashedCorrectPassword)).thenReturn(false);

        SignInCommand signInCmd = new SignInCommand(username, wrongPassword);
        assertThatThrownBy(() -> userCommandService.handle(signInCmd))
                .hasMessageContaining("Invalid password");
    }

    @Test
    @DisplayName("Address Workflow: Update existing address")
    void addressWorkflow_UpdateExistingAddress() {
        User user = new User("updateaddr", "update@example.com", "hashedPass", clientRole);
        setId(user, 15L);
        user.activate();

        CreateAddressCommand initialCmd = new CreateAddressCommand(
                "Old Street 123",
                "Old City",
                "OC",
                "USA",
                "12345",
                true
        );

        Address existingAddress = new Address(initialCmd, user);
        setId(existingAddress, 20L);

        when(userRepository.findById(15L)).thenReturn(Optional.of(user));
        doNothing().when(permissionValidationService).validateUserCanAccessResource(15L);
        when(addressRepository.findById(20L)).thenReturn(Optional.of(existingAddress));
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateAddressCommand updateCmd = new UpdateAddressCommand(
                20L,
                "New Street 456",
                "New City",
                "NC",
                "USA",
                "67890"
        );

        Optional<Address> updatedAddress = addressCommandService.handle(updateCmd, user.getId());

        assertThat(updatedAddress).isPresent();
        assertThat(updatedAddress.get().getStreet()).isEqualTo("New Street 456");
        assertThat(updatedAddress.get().getCity()).isEqualTo("New City");

        verify(addressRepository).save(any(Address.class));
    }

    @Test
    @DisplayName("Product Query Workflow: Retrieve product by ID after creation")
    void productQueryWorkflow_GetProductById() {
        CreateProductCommand productCmd = new CreateProductCommand(
                "Queryable Product",
                "Can be retrieved by ID",
                new BigDecimal("149.99"),
                30,
                List.of(1L),
                true
        );

        Product product = new Product(productCmd, 25L);
        setId(product, 8001L);

        when(productRepository.findById(8001L)).thenReturn(Optional.of(product));

        GetProductByIdQuery query = new GetProductByIdQuery(8001L);
        Optional<Product> retrievedProduct = productQueryService.handle(query);

        assertThat(retrievedProduct).isPresent();
        assertThat(retrievedProduct.get().getId()).isEqualTo(8001L);
        assertThat(retrievedProduct.get().getName()).isEqualTo("Queryable Product");
        assertThat(retrievedProduct.get().getPrice()).isEqualByComparingTo(new BigDecimal("149.99"));

        verify(productRepository).findById(8001L);
    }


    private User createMockUser(Long id, String username, String email) {
        User user = new User(username, email, "hashedPassword", clientRole);
        setId(user, id);
        user.activate();
        return user;
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
}


