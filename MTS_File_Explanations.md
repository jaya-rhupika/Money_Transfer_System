# Money Transfer System — Every File Explained

---

## PROJECT STRUCTURE OVERVIEW

```
Money_Transfer_System/
├── backend/                          ← Spring Boot REST API (Java 17)
│   ├── pom.xml                       ← Maven build config & dependencies
│   ├── mvnw / mvnw.cmd               ← Maven wrapper scripts
│   └── src/
│       ├── main/
│       │   ├── java/com/fidelity/mts/
│       │   │   ├── MoneyTransferSystemApplication.java   ← Entry point
│       │   │   ├── config/
│       │   │   │   └── SecurityConfig.java               ← CORS + auth rules + password encoder
│       │   │   ├── security/
│       │   │   │   ├── SpringSecurityConfig.java         ← HTTP security filter chain
│       │   │   │   └── CustomUserDetailsService.java     ← Loads user from DB for auth
│       │   │   ├── controller/
│       │   │   │   ├── AccountController.java            ← Account CRUD endpoints
│       │   │   │   ├── TransactionController.java        ← Transfer endpoint
│       │   │   │   └── SecurityController.java           ← Login + change password
│       │   │   ├── service/
│       │   │   │   ├── AccountService.java               ← Account service interface
│       │   │   │   ├── AccountServiceImpl.java           ← Account service implementation
│       │   │   │   ├── TransferService.java              ← Transfer service interface
│       │   │   │   └── TransferServiceImpl.java          ← Transfer service implementation
│       │   │   ├── repository/
│       │   │   │   ├── AccountRepository.java            ← DB access for accounts
│       │   │   │   ├── TransactionLogRepository.java     ← DB access for transaction logs
│       │   │   │   └── UserCredentialsRepository.java    ← DB access for credentials
│       │   │   ├── entity/
│       │   │   │   ├── Account.java                      ← accounts table mapping
│       │   │   │   ├── TransactionLog.java               ← transaction_logs table mapping
│       │   │   │   └── UserCredentials.java              ← user_credentials table mapping
│       │   │   ├── dto/
│       │   │   │   ├── TransferRequestDto.java           ← Input for transfer API
│       │   │   │   ├── TransferResponseDto.java          ← Output from transfer API
│       │   │   │   ├── ErrorResponseDto.java             ← Structured error response
│       │   │   │   ├── AccountResponseDto.java           ← Account data response shape
│       │   │   │   └── ChangePasswordRequestDto.java     ← Change password request body
│       │   │   ├── enums/
│       │   │   │   ├── AccountStatus.java                ← ACTIVE / LOCKED / CLOSED
│       │   │   │   └── TransactionStatus.java            ← SUCCESS / FAILURE
│       │   │   └── exceptions/
│       │   │       ├── MtsGlobalNotFoundException.java   ← Global @ControllerAdvice
│       │   │       ├── AccountNotFoundException.java
│       │   │       ├── AccountNotActiveException.java
│       │   │       ├── InsufficientBalanceException.java
│       │   │       ├── SelfTransferException.java
│       │   │       ├── NegativeAmountException.java
│       │   │       └── DuplicateTransferException.java
│       │   └── resources/
│       │       └── application.properties                ← DB config, JPA settings
│       └── test/
│           └── java/com/fidelity/mts/
│               ├── MoneyTransferSystemApplicationTests.java ← Context load test
│               ├── AccountTest.java                         ← Entity unit tests
│               └── TransferRequestValidationTest.java       ← Service unit tests
│
└── frontend/                         ← Angular 21 SPA (TypeScript)
    ├── package.json                  ← npm dependencies & scripts
    ├── angular.json                  ← Angular CLI build configuration
    └── src/
        ├── index.html                ← Single HTML shell (CSP + fonts + app-root)
        ├── styles.css                ← Global CSS design system (CSS variables)
        ├── main.ts                   ← Browser bootstrap entry point
        ├── main.server.ts            ← SSR entry point
        ├── server.ts                 ← Express server for SSR
        └── app/
            ├── app.ts                ← Root component (auto-redirect logic)
            ├── app.html              ← Root template (<router-outlet>)
            ├── app.css               ← Root component styles
            ├── app-module.ts         ← NgModule root (declarations, imports, providers)
            ├── app-routing-module.ts ← Route definitions
            ├── app.module.server.ts  ← Server-side NgModule for SSR
            ├── app.routes.server.ts  ← SSR render mode config
            ├── transfer-service.ts   ← Transfer API service
            ├── models/
            │   ├── transferModel.ts            ← TransferRequestDto & TransferResponseDto interfaces
            │   └── transaction-status.enum.ts  ← TypeScript enum for transaction status
            ├── service/
            │   ├── auth.ts                     ← AuthService (login, token, logout)
            │   ├── accountholderservice.ts      ← Account & transaction API calls
            │   └── httpinterceptor.ts           ← HTTP Interceptor (injects auth header)
            └── component/
                ├── account-holder-interface.ts  ← TypeScript interface for Account shape
                ├── transaction-log-interface.ts ← TypeScript interface for TransactionLog shape
                ├── login/         ← Login page component
                ├── register/      ← Registration page component
                ├── dashboard/     ← Home dashboard with chart
                ├── transfer/      ← Money transfer page
                ├── history/       ← Transaction history page
                ├── rewards/       ← Rewards points page
                ├── redeem/        ← Points redemption page
                └── profile/       ← User profile & password change
```

---

# BACKEND FILES

---

## `pom.xml`
**Location:** `backend/pom.xml`
**What it is:** Maven Project Object Model — the build configuration file for the entire backend.

**Key contents:**
- **Parent:** `spring-boot-starter-parent 3.0.13` — inherits Spring Boot's dependency management (pre-configured versions for all Spring libraries)
- **Group/Artifact:** `com.fidelity.mts` / `money-transfer-system` — identifies the project uniquely in Maven
- **Java Version:** 17 — the minimum JDK required to compile and run the project

**Dependencies declared:**
| Dependency | Purpose |
|---|---|
| `spring-boot-starter-web` | Embedded Tomcat + Spring MVC for building REST controllers |
| `spring-boot-starter-data-jpa` | Spring Data JPA + Hibernate for ORM and DB access |
| `spring-boot-starter-security` | Spring Security for authentication and authorization |
| `spring-boot-starter-validation` | Bean Validation (`@NotNull`, `@NotBlank`, etc.) |
| `spring-boot-starter-aop` | Aspect-Oriented Programming (used internally by `@Transactional`) |
| `spring-boot-starter-test` | JUnit 5 + Mockito + Spring Test for unit/integration tests |
| `mysql-connector-java 8.0.29` | JDBC driver for MySQL 8 |
| `springdoc-openapi-starter-webmvc-ui 2.0.2` | Auto-generates Swagger UI at `/swagger-ui.html` |
| `lombok` | Reduces boilerplate (though not actively used via annotations in this project) |

**Build plugin:** `spring-boot-maven-plugin` — packages the app as a runnable fat JAR with embedded Tomcat.

---

## `application.properties`
**Location:** `backend/src/main/resources/application.properties`
**What it is:** The central configuration file for the Spring Boot application. Loaded automatically at startup.

**Every property explained:**
```properties
spring.application.name=money-transfer-system
```
Sets the app name — used in logging, Spring Cloud discovery, and actuator info.

```properties
spring.datasource.driverClassName=com.mysql.cj.jdbc.Driver
```
Tells Spring which JDBC driver class to use. `cj` is the MySQL Connector/J driver (modern version with timezone support).

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/mts_fidelity
```
Database connection URL — connects to MySQL on `localhost`, port `3306`, database named `mts_fidelity`.

```properties
spring.datasource.username=root
spring.datasource.password=Cloud@123$
```
MySQL credentials. (In production, use environment variables or a secrets manager — never hardcode passwords.)

```properties
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```
Tells Hibernate which SQL dialect to use when generating queries. MySQL 8 has specific features (JSON columns, window functions). The correct dialect ensures generated SQL is compatible.

```properties
spring.jpa.hibernate.ddl-auto=update
```
Hibernate schema management on startup:
- `update` — adds new tables/columns if they don't exist, never drops anything. Good for development.
- (Production should use `validate` or `none` with Flyway/Liquibase migrations)

```properties
spring.jpa.show-sql=true
```
Prints every SQL query Hibernate executes to the console. Useful for debugging but should be disabled in production.

---

## `MoneyTransferSystemApplication.java`
**Location:** `src/main/java/com/fidelity/mts/`
**What it is:** The application entry point — the `main()` method that starts everything.

```java
@SpringBootApplication
public class MoneyTransferSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(MoneyTransferSystemApplication.class, args);
    }
}
```

**`@SpringBootApplication` combines three annotations:**
1. `@Configuration` — this class can define Spring beans
2. `@EnableAutoConfiguration` — Spring Boot reads the classpath and auto-wires common beans (DataSource, EntityManager, etc.)
3. `@ComponentScan` — scans `com.fidelity.mts` and all sub-packages for `@Component`, `@Service`, `@Repository`, `@Controller` classes and registers them as beans

**`SpringApplication.run()`** starts the embedded Tomcat server, loads the ApplicationContext, connects to the database, and begins listening for HTTP requests on port 8080.

---

# SECURITY PACKAGE

---

## `SecurityConfig.java`
**Location:** `src/main/java/com/fidelity/mts/config/`
**What it is:** The primary security configuration. Defines CORS rules, URL access rules, session policy, and the password encoder.

**Key responsibilities:**

**1. `PasswordEncoder` bean:**
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new PasswordEncoder() {
        public String encode(CharSequence raw) { return raw.toString(); }
        public boolean matches(CharSequence raw, String encoded) {
            return raw.toString().equals(encoded);
        }
    };
}
```
A custom plain-text encoder — passwords are stored and compared as-is (no hashing). This is fine for a training project but **must** use BCrypt in production.

**2. `SecurityFilterChain` bean (`filterChain`):**
- Enables CORS using the `corsConfigurationSource()` bean
- Disables CSRF (safe for stateless REST APIs)
- Permits `/api/v1/accounts/register` publicly (no auth needed for registration)
- Requires authentication for `/auth` and all `/api/**` endpoints
- Everything else is permitted (static files, etc.)
- Sets session policy to `STATELESS` (no server-side sessions)
- Custom 401 JSON response instead of Spring's default HTML error

**3. `CorsConfigurationSource` bean:**
- Allows origins: `http://localhost:4200` (Angular dev) and `http://localhost:8080`
- Allows HTTP methods: `GET, POST, PUT, DELETE, OPTIONS`
- Allows all headers (`*`)
- `setAllowCredentials(true)` — required to allow the `Authorization` header in cross-origin requests
- `setMaxAge(3600L)` — browser caches the CORS preflight response for 1 hour

---

## `SpringSecurityConfig.java`
**Location:** `src/main/java/com/fidelity/mts/security/`
**What it is:** A second security filter chain configuration, focused on HTTP Basic Auth setup and request authorization rules.

**What it does:**
- Disables CSRF: `http.csrf(csrf -> csrf.disable())`
- Allows all OPTIONS requests (needed for CORS preflight): `.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()`
- Requires authentication for all other requests: `.anyRequest().authenticated()`
- Configures HTTP Basic Auth with a custom entry point that returns JSON `{"message": "Invalid username or password"}` with status 401 instead of the browser's login popup

**Note:** This project has two `SecurityFilterChain` beans (one here, one in `SecurityConfig`). In a single Spring Boot app, multiple filter chains are supported but must have different `@Order` values to avoid conflicts. Without ordering, behaviour depends on which one Spring loads last.

---

## `CustomUserDetailsService.java`
**Location:** `src/main/java/com/fidelity/mts/security/`
**What it is:** Implements Spring Security's `UserDetailsService` interface. Called by Spring Security during every authenticated request to load the user's credentials from the database.

```java
@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserCredentialsRepository userCredentialsRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userCredentialsRepository.findByUsername(username)
                .map(user -> User.builder()
                        .username(user.getUsername())
                        .password(user.getPassword())
                        .roles("USER")
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
```

**Flow:** When a request arrives with `Authorization: Basic <Base64>`, Spring decodes it → gets username → calls `loadUserByUsername(username)` → this method queries `user_credentials` table → builds a `UserDetails` object → Spring compares the submitted password against the stored one using the `PasswordEncoder` → grants or denies access.

`User.builder().roles("USER")` creates a `UserDetails` with authority `ROLE_USER`. The `.roles()` method automatically prepends `ROLE_` to the string.

---

# CONTROLLER LAYER

---

## `AccountController.java`
**Location:** `src/main/java/com/fidelity/mts/controller/`
**What it is:** Handles all account-related HTTP endpoints.

**Annotations on the class:**
- `@RestController` — all methods return JSON response bodies
- `@RequestMapping("api/v1/")` — base path prefix for all endpoints
- `@CrossOrigin(origins = "http://localhost:4200")` — allows Angular frontend to call these endpoints

**Endpoints:**

| Method | URL | Description |
|---|---|---|
| `POST` | `/api/v1/` | `addAccount()` — creates account directly from entity body (internal use) |
| `POST` | `/api/v1/accounts/register` | `register()` — public endpoint: create account + credentials + ₹200 bonus |
| `GET` | `/api/v1/accounts/{id}` | `getAccountsById()` — fetch full account details by ID |
| `GET` | `/api/v1/accounts/{id}/balance` | `getAccountBalanceById()` — fetch just the balance |
| `GET` | `/api/v1/accounts/{id}/transactions` | `getTransactionLogById()` — fetch all transactions for account |

**`register()` method in detail:**
1. Reads `holderName` and `password` from JSON body (as `Map<String, String>`)
2. Validates that neither is blank — returns `400 BAD_REQUEST` if so
3. Creates a new `Account` with `status = ACTIVE`, `balance = ₹200.00`, current timestamp
4. Calls `service.addAccount(account)` — saves to DB, gets auto-generated `accountId`
5. Uses `accountId` as the `username` for `UserCredentials` — `String.valueOf(accountId)`
6. Saves `UserCredentials` directly via `userCredentialsRepository`
7. Returns `201 CREATED` with `{ accountId, message }`

**Note:** `register()` bypasses the service layer for saving credentials — it directly uses `UserCredentialsRepository`. This is a slight violation of layered architecture; ideally a `UserService` would handle this.

---

## `TransactionController.java`
**Location:** `src/main/java/com/fidelity/mts/controller/`
**What it is:** Single-endpoint controller that handles money transfers.

**Endpoint:**
- `POST /api/v1/transfers` — accepts a `TransferRequestDto` in the request body, delegates to `TransferService.transfer()`, returns `TransferResponseDto`

**Note:** The method is named `getTransactionLogById` — which is misleading. It doesn't fetch a log by ID; it executes a transfer. This is a naming bug but doesn't affect functionality.

The controller is deliberately thin — all logic lives in the service layer. The controller only:
1. Receives the request body
2. Calls the service
3. Wraps the result in `ResponseEntity.status(HttpStatus.OK).body(e)`

---

## `SecurityController.java`
**Location:** `src/main/java/com/fidelity/mts/controller/`
**What it is:** Handles authentication-related endpoints that aren't about account data.

**Endpoints:**

**`GET /auth`** — `authenticate(Principal principal)`
- Spring Security injects `Principal` (the authenticated user) automatically
- If `principal == null` → returns 401
- Looks up `UserCredentials` by the principal's username
- Returns the full `UserCredentials` object (including `accountId`) as JSON
- The Angular frontend uses the returned `accountId` to navigate to `/dashboard/:id`
- This is essentially the "login" endpoint — after Basic Auth validation by Spring Security, this returns user info

**`PUT /auth/change-password`** — `changePassword(Principal principal, @RequestBody ChangePasswordRequestDto request)`
- Verifies current password matches stored password
- Validates new password is not blank
- Validates new password and confirm password match
- Updates password in DB via `userCredentialsRepository.save(user)`
- Returns `Map.of("message", "Password changed successfully")`

---

# SERVICE LAYER

---

## `AccountService.java` (Interface)
**Location:** `src/main/java/com/fidelity/mts/service/`
**What it is:** Java interface defining the contract for account operations. Controllers depend on this interface, not the implementation.

**Methods declared:**
- `Account getAccount(long id)` — fetch account by ID, throw if not found
- `BigDecimal getBalance(long id)` — fetch just the balance
- `List<TransactionLog> getTransactions(long id)` — fetch all transactions for an account
- `long addAccount(Account e)` — save a new account, return its generated ID

This interface enables the Dependency Inversion Principle: the controller is decoupled from `AccountServiceImpl`. In tests, a mock of this interface is used instead of the real implementation.

---

## `AccountServiceImpl.java`
**Location:** `src/main/java/com/fidelity/mts/service/`
**What it is:** Concrete implementation of `AccountService`. Contains the actual business logic for account operations.

**`getAccount(long id)`**
- Calls `accountRepo.findById(id)` which returns `Optional<Account>`
- If `!opt.isPresent()` → throws `AccountNotFoundException("Account Not Found!")`
- Otherwise returns `opt.get()`

**`getBalance(long id)`**
- Same as `getAccount` but returns `opt.get().getBalance()` — only the `BigDecimal` balance

**`getTransactions(long id)`**
- Fetches all sent transactions: `transactionLogRepo.findAllByFromAccountId(id)`
- Fetches all received transactions: `transactionLogRepo.findAllByToAccountId(id)`
- Combines both into one `ArrayList` using `addAll()`
- If combined list is empty → throws `AccountNotFoundException("No transactions found for Account ID: " + id)`
- Returns the combined list (no sorting here — sorting is done on the frontend in `history.ts`)

**`addAccount(Account e)`**
- Calls `accountRepo.save(e)` — Hibernate inserts the entity and populates `e.id` with the DB-generated auto-increment value
- Returns `e.getId()` — the newly assigned account ID

---

## `TransferService.java` (Interface)
**Location:** `src/main/java/com/fidelity/mts/service/`
**What it is:** Interface defining the three operations involved in a money transfer.

**Methods:**
- `TransferResponseDto transfer(TransferRequestDto transferRequest)` — orchestrates the full transfer
- `boolean validateTransfer(Account sender, Account receiver, BigDecimal amount, String key)` — validates business rules
- `TransferResponseDto executeTransfer(Account sender, Account receiver, BigDecimal amount, String key)` — executes the actual DB changes

Making all three public in the interface allows them to be individually tested and mocked.

---

## `TransferServiceImpl.java`
**Location:** `src/main/java/com/fidelity/mts/service/`
**What it is:** The most complex class in the project. Implements the full transfer flow including validation, execution, idempotency, and rewards.

**`transfer(TransferRequestDto)`**
- Fetches sender and receiver accounts from DB (throws if either not found)
- Calls `validateTransfer()` — if it returns `true`, calls `executeTransfer()`
- Returns the `TransferResponseDto` from `executeTransfer`, or `null` if validation fails (in practice, validation throws rather than returning false, so `null` is never returned)

**`validateTransfer()` — Full Validation Chain:**

Each check follows the same pattern: create a FAILURE log → save it → throw exception.

| Check | Exception Thrown |
|---|---|
| `sender.id == receiver.id` | `SelfTransferException` |
| `!sender.isActive()` | `AccountNotActiveException` (sender) |
| `!receiver.isActive()` | `AccountNotActiveException` (receiver) |
| `amount < 0` | `NegativeAmountException` |
| `amount > sender.balance` | `InsufficientBalanceException` |

All failed attempts are persisted to `transaction_logs` with `status=FAILURE` and a `failureReason` string. This creates a complete audit trail.

**`executeTransfer()` — Step by Step:**

Annotated with `@Transactional` — all DB operations are atomic.

1. **Idempotency check:** `findByIdempotencyKey(key).size() != 0` → throws `DuplicateTransferException` if the key was already used
2. **Debit sender:** `senderAcc.setBalance(senderAcc.debit(balance, amount))`
3. **Credit receiver:** `receiverAcc.setBalance(receiverAcc.credit(balance, amount))`
4. **Update timestamps** on both accounts
5. **Flush both accounts** to DB: `accountRepository.saveAndFlush()` — ensures DB reflects new balances before log is written
6. **Calculate points:** `Math.floor(amount / 100)` if amount > ₹100
7. **Create and save** `TransactionLog` with `status=SUCCESS`, the UUID, points
8. **Update sender's totalRewardPoints** if points > 0 (another `saveAndFlush`)
9. **Return** `TransferResponseDto` with `"TRX-" + transactionLog.getId()` as the transaction reference

---

# REPOSITORY LAYER

---

## `AccountRepository.java`
**Location:** `src/main/java/com/fidelity/mts/repository/`
**What it is:** Spring Data JPA repository for the `Account` entity.

```java
public interface AccountRepository extends JpaRepository<Account, Integer> {
    Optional<Account> findById(long id);
}
```

**`extends JpaRepository<Account, Integer>`** — provides inherited methods: `save()`, `saveAndFlush()`, `findById()`, `findAll()`, `deleteById()`, `count()`, etc. Spring creates a proxy implementation at runtime.

**`findById(long id)`** — overrides the inherited `findById(Integer id)` to accept a `long` primitive directly.

**⚠️ Bug:** The generic type is `JpaRepository<Account, Integer>` but `Account.id` is a `long`. This type mismatch is masked by the explicit override but could cause issues with other inherited methods that use the `Integer` type (e.g. `deleteById`).

---

## `TransactionLogRepository.java`
**Location:** `src/main/java/com/fidelity/mts/repository/`
**What it is:** Spring Data JPA repository for `TransactionLog`.

**Custom query methods:**

`findAllByFromAccountId(long id)` — Spring parses the method name and generates:
```sql
SELECT * FROM transaction_logs WHERE from_account_id = ?
```

`findAllByToAccountId(long id)` — generates:
```sql
SELECT * FROM transaction_logs WHERE to_account_id = ?
```

`findByIdempotencyKey(String key)` — generates:
```sql
SELECT * FROM transaction_logs WHERE idempotency_key = ?
```
Returns `List<TransactionLog>` (not `Optional`) even though the key is UNIQUE — so checking `.size() != 0` works but `.isEmpty()` would be cleaner.

---

## `UserCredentialsRepository.java`
**Location:** `src/main/java/com/fidelity/mts/repository/`
**What it is:** Spring Data JPA repository for `UserCredentials`.

```java
@Repository
public interface UserCredentialsRepository extends JpaRepository<UserCredentials, Long> {
    Optional<UserCredentials> findByUsername(String username);
}
```

Unlike the other two repositories, this one has the `@Repository` annotation explicitly. (The others work without it because Spring Data JPA auto-detects JpaRepository extensions — the annotation is optional but not harmful.)

`findByUsername(String username)` returns `Optional<UserCredentials>` — used both in `CustomUserDetailsService` (for authentication) and in `SecurityController` (to fetch user info after login).

---

# ENTITY LAYER

---

## `Account.java`
**Location:** `src/main/java/com/fidelity/mts/entity/`
**What it is:** JPA entity mapped to the `accounts` database table. Represents a bank account.

**Fields and their DB column definitions:**

| Field | Type | Column | Notes |
|---|---|---|---|
| `id` | `long` | `BIGINT` AUTO_INCREMENT | Primary key, auto-generated |
| `holderName` | `String` | `VARCHAR(255) NOT NULL` | Account holder's full name |
| `balance` | `BigDecimal` | `DECIMAL(18,2) NOT NULL` | Monetary balance — 2 decimal places |
| `status` | `AccountStatus` | `VARCHAR(20) NOT NULL` | `ACTIVE`, `LOCKED`, or `CLOSED` as string |
| `version` | `int` | `INT DEFAULT 0` | Intended for optimistic locking (but `@Version` is missing) |
| `lastUpdated` | `LocalDateTime` | `TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | Auto-updates on any row change |
| `totalRewardPoints` | `int` | `INT DEFAULT 0` | Cumulative reward points earned |

**Business methods on the entity:**
- `debit(currentBal, amount)` → `currentBal.subtract(amount)` — returns new balance after deduction
- `credit(currentBal, amount)` → `currentBal.add(amount)` — returns new balance after addition
- `isActive()` → `status == AccountStatus.ACTIVE` — returns true only for ACTIVE accounts

**Why `BigDecimal` for balance?** `double`/`float` use IEEE 754 floating-point, which cannot represent many decimals exactly (0.1 + 0.2 = 0.30000000000000004). `BigDecimal` gives exact arithmetic — critical for financial calculations.

**Why `@Enumerated(EnumType.STRING)`?** Stores `"ACTIVE"` in the DB instead of `0`. If `EnumType.ORDINAL` were used and a new status was inserted between ACTIVE and LOCKED, all ordinals would shift and corrupt existing data.

---

## `TransactionLog.java`
**Location:** `src/main/java/com/fidelity/mts/entity/`
**What it is:** JPA entity mapped to `transaction_logs`. Every transfer attempt — success or failure — creates one row here.

**Fields:**

| Field | Type | Column | Notes |
|---|---|---|---|
| `id` | `UUID` | `CHAR(36)` | Manually set via `UUID.randomUUID()` before save |
| `fromAccountId` | `long` | `BIGINT NOT NULL` | Sender's account ID (FK) |
| `toAccountId` | `long` | `BIGINT NOT NULL` | Receiver's account ID (FK) |
| `amount` | `BigDecimal` | `DECIMAL(18,2) NOT NULL` | Transfer amount |
| `status` | `TransactionStatus` | `VARCHAR(20) NOT NULL` | `SUCCESS` or `FAILURE` |
| `failureReason` | `String` | `VARCHAR(255)` | Reason if failed (e.g. "Balance is not sufficient") |
| `idempotencyKey` | `String` | `VARCHAR(100) UNIQUE` | UUID from frontend — prevents duplicate transfers |
| `createdOn` | `LocalDateTime` | `TIMESTAMP DEFAULT CURRENT_TIMESTAMP` | When the attempt happened |
| `points` | `int` | `INT DEFAULT 0` | Reward points earned on this transaction |

**Dual mapping pattern (why there are two references to Account):**
```java
@Column(name="from_account_id")
private long fromAccountId;  // ← used for writes (INSERT/UPDATE)

@ManyToOne
@JoinColumn(name="from_account_id", insertable=false, updatable=false)
private Account FromAccount;  // ← used for reads (JOIN)
```
`insertable=false, updatable=false` tells JPA: the object reference doesn't control DB writes; only the primitive `fromAccountId` column does. The `@ManyToOne` is purely for reading related `Account` data.

**Why UUID for `id`?** Auto-increment integers are predictable — attackers can enumerate transaction IDs. UUIDs are random and globally unique. The ID is returned to the frontend as `"TRX-" + uuid`.

---

## `UserCredentials.java`
**Location:** `src/main/java/com/fidelity/mts/entity/`
**What it is:** JPA entity mapped to `user_credentials`. Stores login credentials separate from account financial data.

**Fields:**

| Field | Type | Notes |
|---|---|---|
| `accountId` | `long` | Primary key AND foreign key to `accounts.id` |
| `username` | `String` | Set to the account ID string (e.g. `"5"`) — must be UNIQUE |
| `password` | `String` | Stored in plain text (training project; BCrypt in production) |
| `account` | `Account` | `@OneToOne` read-only reference (`insertable=false, updatable=false`) |

**Design decision — why separate from Account?** Separation of concerns. Financial data (balance, status, points) and security data (passwords) are kept apart. This makes it easier to change authentication mechanisms without touching account data, and limits exposure if the credentials table is ever queried differently.

**Shared primary key pattern:** `accountId` is both `@Id` and the FK to `accounts`. One `Account` → exactly one `UserCredentials`.

---

# DTO LAYER

---

## `TransferRequestDto.java`
**Location:** `src/main/java/com/fidelity/mts/dto/`
**What it is:** The shape of the JSON body sent by the Angular frontend when initiating a transfer.

**Fields:**
- `fromAccountId` — `long` — the logged-in user's account ID
- `toAccountId` — `long` — the recipient's account ID
- `amount` — `BigDecimal` — the amount to transfer
- `idempotencyKey` — `String` — UUID v4 generated on the frontend to prevent duplicate transfers

Default no-arg constructor means `fromAccountId` and `toAccountId` default to `0L`, `amount` and `idempotencyKey` default to `null` — tested in `testNullFields()`.

---

## `TransferResponseDto.java`
**Location:** `src/main/java/com/fidelity/mts/dto/`
**What it is:** The JSON response sent back after a successful transfer.

**Fields:**
- `id` — `String` — the transaction reference in format `"TRX-<UUID>"`
- `finalMessage` — `String` — always `"Transfer completed"` on success
- `status` — `TransactionStatus` — always `SUCCESS` in this response
- `toAccountId` — `long`
- `fromAccountId` — `long`
- `amount` — `BigDecimal`

The Angular frontend reads `response.id` to show the transaction ID in the success modal.

---

## `ErrorResponseDto.java`
**Location:** `src/main/java/com/fidelity/mts/dto/`
**What it is:** Standard error response shape returned by `@ControllerAdvice` handlers.

**Fields:**
- `errorCode` — `String` — domain-specific code (e.g. `"ACC-404"`, `"TRX-409"`, `"VAL-422"`)
- `errorMessage` — `String` — human-readable description (the exception's message)

The Angular frontend reads `err.error.errorCode` and `err.error.errorMessage` to show the appropriate error modal text.

---

## `AccountResponseDto.java`
**Location:** `src/main/java/com/fidelity/mts/dto/`
**What it is:** A DTO representing account information for API responses. Contains: `id`, `holderName`, `balance`, `status`, `version`, `lastUpdated`.

**Note:** In the current implementation, `AccountController.getAccountsById()` returns the raw `Account` entity directly rather than this DTO. `AccountResponseDto` is defined but not actively used in controller responses — it may have been created for future use or was part of an earlier design.

---

## `ChangePasswordRequestDto.java`
**Location:** `src/main/java/com/fidelity/mts/dto/`
**What it is:** Request body for `PUT /auth/change-password`.

**Fields:**
- `currentPassword` — must match the user's existing stored password
- `newPassword` — the replacement password
- `confirmPassword` — must match `newPassword`

`SecurityController.changePassword()` validates all three fields before updating.

---

# ENUM LAYER

---

## `AccountStatus.java`
**Location:** `src/main/java/com/fidelity/mts/enums/`
**What it is:** Defines the three possible states of a bank account.

```java
public enum AccountStatus {
    ACTIVE, LOCKED, CLOSED
}
```

- `ACTIVE` — account can send and receive money
- `LOCKED` — account is temporarily restricted (frozen)
- `CLOSED` — account is permanently deactivated

`account.isActive()` returns `true` only when status is `ACTIVE`. Both `LOCKED` and `CLOSED` fail the `isActive()` check in `validateTransfer()`, causing `AccountNotActiveException`.

---

## `TransactionStatus.java`
**Location:** `src/main/java/com/fidelity/mts/enums/`
**What it is:** Defines the two possible outcomes of a transfer attempt.

```java
public enum TransactionStatus {
    SUCCESS, FAILURE
}
```

Every `TransactionLog` record gets one of these statuses. Even failed/rejected attempts get `FAILURE` status — they are still persisted. This ensures a complete, tamper-evident audit log.

---

# EXCEPTION LAYER

---

## `MtsGlobalNotFoundException.java`
**Location:** `src/main/java/com/fidelity/mts/exceptions/`
**What it is:** Global exception handler using Spring's `@ControllerAdvice`. Intercepts exceptions thrown from any controller in the application and converts them into structured JSON error responses.

**Despite the name "NotFound"**, it handles all domain exceptions — the name is misleading.

**Exception → HTTP Status mapping:**
```
AccountNotActiveException    → 403 FORBIDDEN      (error code: ACC-403)
AccountNotFoundException     → 404 NOT_FOUND      (error code: ACC-404)
DuplicateTransferException   → 409 CONFLICT       (error code: TRX-409)
InsufficientBalanceException → 400 BAD_REQUEST    (error code: TRX-400)
SelfTransferException        → 400 BAD_REQUEST    (error code: VAL-422)
NegativeAmountException      → 400 BAD_REQUEST    (error code: VAL-422)
```

Every handler creates `new ErrorResponseDto(errorCode, ex.getMessage())` and wraps it in `ResponseEntity` with the appropriate HTTP status. Without this class, Spring would return a generic HTML error page (or a default Spring Boot JSON error), and the frontend wouldn't be able to parse the domain-specific error codes.

---

## Custom Exception Classes
**Location:** `src/main/java/com/fidelity/mts/exceptions/`
**What they are:** Six domain-specific exception classes, each extending `RuntimeException`.

All six follow the same pattern:
```java
public class XxxException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public XxxException(String message) {
        super(message);
    }
}
```

| Class | When thrown | Message example |
|---|---|---|
| `AccountNotFoundException` | Account ID not found in DB | `"Account Not Found!"` |
| `AccountNotActiveException` | Sender/receiver is LOCKED or CLOSED | `"Sender Account Not Active !"` |
| `InsufficientBalanceException` | Transfer amount > sender's balance | `"Balance is not sufficient"` |
| `SelfTransferException` | `fromAccountId == toAccountId` | `"Self Transfer is not allowed!"` |
| `NegativeAmountException` | Transfer amount < 0 | `"Negative Amount cannot be transferred!"` |
| `DuplicateTransferException` | Idempotency key already in DB | `"Duplicate Transaction not allowed!!"` |

**Why `RuntimeException`?** Unchecked exceptions don't require `throws` declarations or `try-catch` in every calling method. They propagate naturally up the call stack to the `@ControllerAdvice` handler.

**`serialVersionUID = 1L`** — Java requires a version identifier for serialisable classes. Setting it manually prevents accidental changes when the class is modified.

**`@Serial`** — Java 14+ annotation marking serialisation-related fields for tooling/IDE support. No behaviour change.

---

# TEST FILES

---

## `MoneyTransferSystemApplicationTests.java`
**Location:** `src/test/java/com/fidelity/mts/`
**What it is:** Spring Boot context load test — the minimum test that verifies the entire application starts without errors.

```java
@SpringBootTest
class MoneyTransferSystemApplicationTests {
    @Test
    void contextLoads() { }
}
```

`@SpringBootTest` loads the full `ApplicationContext`. If any bean fails to initialise (missing dependency, DB config error, security misconfiguration), this test fails. An empty `contextLoads()` method is the convention — the test passes simply if the context starts up cleanly.

---

## `AccountTest.java`
**Location:** `src/test/java/com/fidelity/mts/`
**What it is:** Unit tests for the `Account` entity's business methods (`debit`, `credit`, `isActive`).

**Setup (`@BeforeEach`):** Creates a fresh `Account` with id=1, name="George", balance=₹1000, status=ACTIVE before each test.

**Tests:**

| Test | What it verifies |
|---|---|
| `testDebit_Success()` | `debit(1000, 600)` returns `400.00` |
| `testDebit_InsufficientBalance()` | `1000 - 1200 < 0` → `InsufficientBalanceException` is thrown |
| `testDebit_NegativeAmount()` | Amount `< 0` → `InsufficientBalanceException` is thrown |
| `testCredit_Success()` | `credit(1000, 500)` returns `1500.00` |
| `testIsActive_ActiveAccount()` | `ACTIVE` status → `isActive()` = `true` |
| `testIsActive_LockedAccount()` | `LOCKED` status → `isActive()` = `false` |
| `testIsActive_ClosedAccount()` | `CLOSED` status → `isActive()` = `false` |

Note: `@Mock TransferService transferService` is declared but never used in the tests — it's an artefact of earlier development.

---

## `TransferRequestValidationTest.java`
**Location:** `src/test/java/com/fidelity/mts/`
**What it is:** Unit tests for `TransferServiceImpl` using Mockito to mock all dependencies.

**Setup:**
```java
@Mock AccountService accountService;
@Mock AccountRepository accountRepository;
@Mock TransactionLogRepository transactionLogRepository;
@InjectMocks TransferServiceImpl transferService;
```
`@InjectMocks` creates a real `TransferServiceImpl` and injects the three mocks into it.

**Tests:**

**`testValidTransfer()`** — Sets up sender (ACTIVE, ₹2000) and receiver (CLOSED, ₹6570). Stubs account lookups and idempotency key check. Calls `transfer()` and asserts `result` is not null, has `"Transfer completed"` message and `SUCCESS` status. ⚠️ Bug: receiver is `CLOSED`, so `validateTransfer()` should throw `AccountNotActiveException`, but this test expects success. The test has a logical error.

**`testNegativeAmount()`** — Sets up a transfer with `amount = -10`. Asserts `NegativeAmountException` is thrown. Correct test.

**`testNullFields()`** — Creates a no-arg `TransferRequestDto` and asserts default field values: `fromAccountId = 0L`, `toAccountId = 0L`, `amount = null`, `idempotencyKey = null`. Verifies the DTO's default constructor behaviour.

---

# FRONTEND FILES

---

## `package.json`
**Location:** `frontend/package.json`
**What it is:** npm manifest file — defines the project name, scripts, and all JavaScript dependencies.

**Scripts:**
- `ng serve` — starts Angular dev server on port 4200 with hot reload
- `ng build` — compiles Angular for production
- `ng test` — runs unit tests
- `node dist/mts-frontend/server/server.mjs` — runs the SSR server

**Key dependencies:**

| Package | Version | Purpose |
|---|---|---|
| `@angular/core` | ^21.1.0 | Angular framework core |
| `@angular/router` | ^21.1.0 | Client-side routing |
| `@angular/forms` | ^21.1.0 | Template-driven and reactive forms |
| `@angular/ssr` | ^21.1.3 | Server-Side Rendering support |
| `@angular/platform-server` | ^21.1.0 | Angular running in Node.js for SSR |
| `chart.js` | ^4.4.0 | Chart rendering library |
| `ng2-charts` | ^4.1.1 | Angular wrapper for Chart.js |
| `rxjs` | ~7.8.0 | Reactive programming (Observables) |
| `uuid` | ^13.0.0 | UUID v4 generation for idempotency keys |
| `express` | ^5.1.0 | Node.js server for SSR |

**Dev dependencies:**
- `@angular/cli` — Angular CLI tooling
- `typescript ~5.9.2` — TypeScript compiler
- `@types/uuid` — TypeScript type definitions for the uuid package
- `vitest` — JavaScript unit test runner (alternative to Karma/Jasmine)

---

## `index.html`
**Location:** `frontend/src/`
**What it is:** The single HTML page that Angular's SPA loads into. The entire application lives inside this one file.

**Key elements:**
```html
<base href="/">
```
Tells the browser to resolve all relative URLs from the root. Required for Angular's client-side router to function correctly.

```html
<meta http-equiv="Content-Security-Policy" content="...">
```
CSP restricts what resources the page can load:
- `default-src 'self' 'unsafe-inline'` — only load from same origin; allow inline styles/scripts
- `connect-src 'self' http://localhost:4200 http://localhost:8080 ws://localhost:4200` — allows HTTP calls to both Angular dev server (4200) and Spring Boot backend (8080), and WebSocket for hot-reload

```html
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined">
```
Loads Material Icons font — used throughout the app for nav icons (home, send, history, etc.).

```html
<app-root></app-root>
```
Angular bootstraps into this tag. The `App` root component replaces this with the routed view.

---

## `styles.css`
**Location:** `frontend/src/`
**What it is:** Global stylesheet. Defines the entire design system as CSS custom properties (variables) and shared utility classes used across all components.

**CSS Variables defined:**
```css
:root {
  --clr-primary: #1e3f00;         /* dark green brand color */
  --clr-primary-light: #45a049;   /* lighter green for buttons */
  --clr-bg: #d4dbd6;              /* sage-green page background */
  --clr-success: #067647;         /* green for success states */
  --clr-error: #b91c1c;           /* red for error states */
  --shadow-card: 0 12px 32px ...; /* card drop shadow */
  --radius-md: 14px;              /* border radius for cards */
  --ease: 0.2s ease;              /* transition timing */
}
```

**Shared components defined globally:**
- `.bottom-nav` — the fixed bottom navigation bar (Home, Send, History icons). Shared by Dashboard, Transfer, History, Rewards, Profile
- `.nav-item` — individual nav button with hover and active states
- `.modal-overlay` — semi-transparent backdrop for modals (success/error popups)

All component-level CSS files inherit these variables via `var(--clr-primary)` etc., ensuring visual consistency across pages.

---

## `main.ts`
**Location:** `frontend/src/`
**What it is:** The browser-side entry point. Bootstraps the Angular application in the browser.

```typescript
import { platformBrowser } from '@angular/platform-browser';
import { AppModule } from './app/app-module';

platformBrowser().bootstrapModule(AppModule)
  .catch(err => console.error(err));
```

`platformBrowser()` creates the Angular platform for the browser environment. `bootstrapModule(AppModule)` loads the root module, processes all `declarations` and `providers`, and mounts the root component (`App`) into the `<app-root>` tag in `index.html`.

---

## `main.server.ts`
**Location:** `frontend/src/`
**What it is:** The server-side rendering (SSR) entry point. Re-exports `AppServerModule` as the default export for Angular's SSR build.

```typescript
export { AppServerModule as default } from './app/app.module.server';
```

When the Angular CLI builds the SSR bundle, it looks for this file's default export as the module to render on the server. The server build uses this file while the browser build uses `main.ts`.

---

## `server.ts`
**Location:** `frontend/src/`
**What it is:** An Express.js server that handles SSR (Server-Side Rendering) for the Angular app.

**What it does:**
1. Creates an Express app
2. Serves static files (JS, CSS, images) from the built `../browser` directory with 1-year cache headers
3. For all other requests, passes them to `AngularNodeAppEngine` which pre-renders the Angular route on the server
4. Sends the pre-rendered HTML to the client (faster first paint, better SEO)
5. Listens on `PORT` environment variable or defaults to port 4000

This is the file that runs when you execute `npm run serve:ssr:mts-frontend`. It powers the production SSR mode.

---

## `app.ts` (Root Component)
**Location:** `frontend/src/app/`
**What it is:** The root Angular component — the top-level component that Angular bootstraps into `<app-root>`.

**What it does:**
- On `ngOnInit()`, checks if the user is already logged in (`authService.isUserLoggedin()`)
- If logged in AND currently at `/`, `""`, or `/login` — navigates to `/dashboard/:userId`
- This handles the browser refresh case: if the user has a valid session in `sessionStorage` and refreshes the page at `/`, they're automatically redirected to their dashboard instead of the login screen

The template (`app.html`) contains only `<router-outlet />` — a placeholder where Angular renders the currently active route's component.

---

## `app-module.ts`
**Location:** `frontend/src/app/`
**What it is:** The root `NgModule` — the central registry of the entire Angular application. Everything must be declared or imported here to be used.

**`declarations`** — all 9 components that belong to this module:
`App`, `Login`, `Register`, `Dashboard`, `Transfer`, `History`, `Rewards`, `Redeem`, `Profile`

**`imports`** — Angular modules whose exported directives/pipes are used:
- `BrowserModule` — required for browser-specific functionality, `*ngIf`, `*ngFor` base functionality
- `AppRoutingModule` — provides `RouterOutlet`, `RouterLink`, `RouterModule`
- `FormsModule` — enables two-way data binding with `[(ngModel)]`
- `ReactiveFormsModule` — enables reactive forms (FormGroup, FormControl)
- `CommonModule` — provides `NgIf`, `NgFor`, `AsyncPipe`, `DatePipe`, etc.
- `RouterOutlet`, `RouterLink` — imported individually (Angular 14+ style)
- `NgChartsModule` — provides `<canvas baseChart>` from ng2-charts

**`providers`**:
- `HTTP_INTERCEPTORS` with `HttpinterceptorService` and `multi: true` — registers the auth interceptor
- `provideHttpClient(withFetch(), withInterceptorsFromDi())` — sets up HttpClient to use Fetch API and honour DI-registered interceptors
- `provideBrowserGlobalErrorListeners()` — registers global error listeners for the browser
- `provideClientHydration(withEventReplay())` — enables SSR hydration with user event replay

---

## `app-routing-module.ts`
**Location:** `frontend/src/app/`
**What it is:** Defines all client-side routes. When the URL changes, Angular renders the corresponding component inside `<router-outlet>`.

**Route table:**
| Path | Component | Notes |
|---|---|---|
| `login` | `Login` | Login page |
| `register` | `Register` | Registration page |
| `dashboard/:id` | `Dashboard` | Home screen; `:id` is the account ID |
| `transfer/:id` | `Transfer` | Send money page |
| `history/:id` | `History` | Transaction history |
| `rewards/:id` | `Rewards` | Rewards points breakdown |
| `redeem/:id` | `Redeem` | Points redemption page |
| `profile/:id` | `Profile` | Profile + change password |
| `` (empty) | — | Redirects to `login` (pathMatch: 'full') |
| `**` (wildcard) | — | Redirects to `login` |

`RouterModule.forRoot(routes)` registers routes at the root level. Should only be called once.

---

## `app.module.server.ts`
**Location:** `frontend/src/app/`
**What it is:** Server-side NgModule for SSR. Extends `AppModule` with server-specific providers.

```typescript
@NgModule({
  imports: [AppModule],
  providers: [provideServerRendering(withRoutes(serverRoutes))],
  bootstrap: [App],
})
export class AppServerModule {}
```

`provideServerRendering(withRoutes(serverRoutes))` activates Angular Universal's server rendering engine and specifies how each route should be rendered on the server (see `app.routes.server.ts`).

---

## `app.routes.server.ts`
**Location:** `frontend/src/app/`
**What it is:** Configures how each route is rendered in SSR mode.

```typescript
export const serverRoutes: ServerRoute[] = [
  {
    path: '**',
    renderMode: RenderMode.Prerender
  }
];
```

`RenderMode.Prerender` means every route is pre-rendered at build time to static HTML. This gives the fastest possible page load but means dynamic data isn't included in the pre-rendered HTML (it's fetched client-side after hydration).

---

# FRONTEND SERVICES

---

## `auth.ts` (AuthService)
**Location:** `frontend/src/app/service/`
**What it is:** The authentication service. Manages login, logout, token storage, and session state.

**Key constants:**
- `SESSION_KEY = 'auth_user'` — key in `sessionStorage` storing the username (account ID)
- `TOKEN_KEY = 'auth_token'` — key in `sessionStorage` storing the Base64-encoded auth token

**`authenticate(username, password)`:**
1. Combines: `"username:password"`
2. Base64-encodes using `btoa()` (browser) or `Buffer.from().toString('base64')` (Node.js/SSR fallback)
3. Makes `GET /auth` with explicit `Authorization: Basic <encoded>` header
4. On success via `pipe(map(...))` — stores username in `SESSION_KEY` and token in `TOKEN_KEY` in sessionStorage
5. Returns the server response Observable (the `UserCredentials` object)

**`logout()`:** Removes both session keys from `sessionStorage`, clears in-memory `username`/`password` fields.

**`isUserLoggedin()`:** Returns `true` if `SESSION_KEY` exists in `sessionStorage`. Used by `Login.ngOnInit()` and `App.ngOnInit()` for auto-redirect.

**`getLoggedinUser()`:** Returns the stored username (= account ID string) from `sessionStorage`.

**`getAuthToken()`:** Returns the Base64 token string for use by the HTTP interceptor.

**`changePassword(current, new, confirm)`:** Makes `PUT /auth/change-password` with the three password fields. Called from `Profile` component.

**Platform guard:** Constructor injects `PLATFORM_ID` and sets `this.isBrowser = isPlatformBrowser(platformId)`. All `sessionStorage` access is wrapped in `if (this.isBrowser)` to prevent errors during SSR when Node.js runs the code (Node.js doesn't have `sessionStorage`).

---

## `accountholderservice.ts` (Accountholderservice)
**Location:** `frontend/src/app/service/`
**What it is:** Service for fetching account data and transaction history from the backend.

```typescript
@Injectable({ providedIn: 'root' })
export class Accountholderservice {
  private uri = '/api/v1/accounts/';
  private http = inject(HttpClient);

  getUserById(id: number): Observable<AccountHolderInterface> {
    return this.http.get<AccountHolderInterface>(`${this.uri}${id}`);
  }

  getTransactionsById(id: number): Observable<TransactionLogInterface[]> {
    return this.http.get<TransactionLogInterface[]>(`${this.uri}${id}/transactions`);
  }
}
```

Uses **relative URLs** (`/api/v1/accounts/`) — requests resolve relative to the Angular dev server, which proxies them to Spring Boot port 8080 (or they work directly when served from the same origin in production).

`inject(HttpClient)` is used instead of constructor injection — the modern Angular 14+ pattern.

`providedIn: 'root'` means Angular creates a single instance of this service for the entire app (singleton).

---

## `httpinterceptor.ts` (HttpinterceptorService)
**Location:** `frontend/src/app/service/`
**What it is:** HTTP Interceptor — intercepts every outgoing HTTP request and injects the auth token automatically.

```typescript
intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
  const token = this.authService.getAuthToken();
  if (token) {
    const request = req.clone({ setHeaders: { Authorization: `Basic ${token}` } });
    return next.handle(request);
  }
  return next.handle(req);
}
```

**Flow:**
1. Gets the Base64 token from `AuthService.getAuthToken()`
2. If a token exists — clones the request (HTTP requests are immutable) and adds `Authorization: Basic <token>` header
3. Passes the modified request to `next.handle()` — the next interceptor or the actual HTTP call
4. If no token — passes the original request unchanged

This eliminates duplicating auth headers in every service method. The interceptor is registered once in `AppModule` with `multi: true` and automatically applies to all `HttpClient` calls, including `AccountholderService` and `TransferService`.

---

## `transfer-service.ts` (TransferService)
**Location:** `frontend/src/app/`
**What it is:** Service for calling the transfer API endpoint.

```typescript
private apiUrl = 'http://localhost:8080/api/v1/transfers';

executeTransfer(request: TransferRequestDto): Observable<TransferResponseDto> {
  return this.http.post<TransferResponseDto>(this.apiUrl, request);
}
```

Uses an **absolute URL** (`http://localhost:8080/...`) unlike `Accountholderservice` which uses relative paths. This is a consistency issue — in production, this would need to be updated or use Angular's environment files. The HTTP interceptor still applies to this call and adds the auth header.

---

# MODELS & INTERFACES

---

## `account-holder-interface.ts`
**Location:** `frontend/src/app/component/`
**What it is:** TypeScript interface defining the shape of account data received from the backend.

```typescript
export interface AccountHolderInterface {
  id: number;
  holderName: string;
  balance: number;
  status: string;
  version: number;
  lastUpdated: Date;
  active: boolean;
  totalRewardPoints?: number;  // optional
}
```

This mirrors the `Account` Java entity's JSON serialisation. `active` is the serialised form of `account.isActive()` — Jackson serialises getter methods that follow the `isXxx()` pattern as a field named `xxx`.

`totalRewardPoints?` has the `?` optional modifier — it may not always be present in the response.

---

## `transaction-log-interface.ts`
**Location:** `frontend/src/app/component/`
**What it is:** TypeScript interface defining the shape of transaction log data received from the backend.

```typescript
export interface TransactionLogInterface {
  id: string;
  fromAccountId: number;
  toAccountId: number;
  amount: number;
  status: 'SUCCESS' | 'FAILED' | string;
  failureReason: string;
  idempotencyKey: string;
  createdOn: string;
  points?: number;  // optional
}
```

Note: `status` uses a union type `'SUCCESS' | 'FAILED' | string` — accepts the two known values or any other string for flexibility.

`createdOn` is typed as `string` (not `Date`) because JSON doesn't have a native date type — dates come as ISO strings from the backend.

`points?` is optional because older transaction logs may not have this field (it was added later).

---

## `transferModel.ts`
**Location:** `frontend/src/app/models/`
**What it is:** TypeScript interfaces for transfer request and response DTOs — the frontend equivalents of the Java DTOs.

```typescript
export interface TransferRequestDto {
  fromAccountId: number;
  toAccountId: number;
  amount: number;
  idempotencyKey: string;
}

export interface TransferResponseDto {
  id: string;
  finalMessage: string;
  status: TransactionStatus;
  toAccountId: number;
  fromAccountId: number;
  amount: number;
}
```

These are `interface` types (not classes) — they define shape only, with no runtime overhead.

---

## `transaction-status.enum.ts`
**Location:** `frontend/src/app/models/`
**What it is:** TypeScript enum mirroring the Java `TransactionStatus` enum.

```typescript
export enum TransactionStatus {
    SUCCESS = 'SUCCESS',
    FAILURE = 'FAILURE'
}
```

String enums (values explicitly set to string) are used here — when comparing with API response strings (`t.status === 'SUCCESS'`), the enum values match directly without mapping.

---

# FRONTEND COMPONENTS

---

## `Login` Component
**Files:** `login.ts`, `login.html`, `login.css`
**Route:** `/login`

**What it does:**
- Displays account number and password input fields
- `ngOnInit()` — checks `authService.isUserLoggedin()`. If already logged in, redirects to dashboard (prevents logged-in users from seeing the login page)
- `doLogin()` — validates fields, sets `isSubmitting = true`, calls `authService.authenticate(username, password).subscribe()`
- On success: extracts `accountId` from the response and navigates to `/dashboard/:accountId`
- On error: displays error message (`err?.error?.message || 'Invalid...'`)
- `isSubmitting` flag prevents double-click submissions

**Key fields:** `username`, `password`, `isLoggedin`, `error`, `isSubmitting`

---

## `Register` Component
**Files:** `register.ts`, `register.html`, `register.css`
**Route:** `/register`

**What it does:**
- Form with Name, Password, Confirm Password fields
- `doRegister()` — validates fields and password match, then posts `{ holderName, password }` to `POST /api/v1/accounts/register`
- On success: stores the new `accountId`, sets `showWelcomePopup = true` — shows a welcome popup displaying the new account number
- `goToDashboard()` — closes popup and navigates to `/dashboard/:newAccountId`
- Uses `HttpClient` directly (no service) — calls the registration endpoint without auth (it's a public endpoint)
- `cd.detectChanges()` is called after data arrives to force view re-render in SSR context

**Key fields:** `name`, `password`, `confirmPassword`, `showWelcomePopup`, `newAccountId`, `isSubmitting`, `error`

---

## `Dashboard` Component
**Files:** `dashboard.ts`, `dashboard.html`, `dashboard.css`
**Route:** `/dashboard/:id`

**What it does:**
The main home screen after login. Shows account info, balance, reward points, and a Chart.js doughnut chart of sent vs. received transaction counts.

**`ngOnInit()`:**
- Reads `:id` from route params
- If invalid (≤ 0) → redirects to `/`
- Calls `loadUser()` and `loadTransactionStats()`

**`loadUser()`:** Calls `accountholderservice.getUserById(accId)` → stores result in `this.user`

**`loadTransactionStats()`:**
- Calls `getTransactionsById(accId)` → calls `calculateStats(data)`
- After loading, waits 100ms (`setTimeout`) then calls `initializeChart()`
- The 100ms delay ensures the Angular view has re-rendered the `<canvas>` element before Chart.js tries to use it

**`calculateStats(transactions)`:** Iterates SUCCESS transactions and counts/sums sent and received amounts and counts.

**`initializeChart()`:**
- Gets canvas context: `transactionChart.nativeElement.getContext('2d')`
- Calls `chart.destroy()` if a chart already exists (prevents "canvas already in use" error)
- Creates a `new Chart(ctx, { type: 'doughnut', data: { labels: ['Sent', 'Received'], datasets: [sentCount, receivedCount] } })`
- `cutout: '58%'` makes it a ring/donut shape (hole in the middle)

**Navigation methods:** `goHome()`, `goToTransfer()`, `goToHistory()`, `goToRewards()`, `goToProfile()`, `logout()`

**Key fields:** `user`, `accId`, `chart`, `stats` (totalSent, totalReceived, sentCount, receivedCount), `statsLoading`

---

## `Transfer` Component
**Files:** `transfer.ts`, `transfer.html`, `transfer.css`
**Route:** `/transfer/:id`

**What it does:**
The money transfer page. User enters recipient account ID and amount.

**`submitTransfer()`:**
1. Returns if `isSubmitting` is true
2. Sets `isSubmitting = true`
3. Builds `TransferRequestDto` with `fromAccountId`, `toAccountId`, `amount`, and a fresh `uuidv4()` as `idempotencyKey`
4. Calls `transferService.executeTransfer(request).subscribe(...)`
5. On success: sets `transactionId = response.id`, shows `showSuccessModal = true`
6. On error: extracts `errorCode` and `errorMessage` from `err.error`, shows `showErrorModal = true`
7. Resets `isSubmitting = false` in both cases

**Modals:** `showSuccessModal` shows the transaction ID and a button to go back to dashboard. `showErrorModal` shows the error code and message with a "Try Again" button.

**Key fields:** `recipientId`, `transferAmount`, `showSuccessModal`, `showErrorModal`, `transactionId`, `errorMessage`, `errorCode`, `isSubmitting`

---

## `History` Component
**Files:** `history.ts`, `history.html`, `history.css`
**Route:** `/history/:id`

**What it does:**
Displays all transactions for the account with filtering and search.

**`ngOnInit()`:** Fetches all transactions via `getTransactionsById(accId)`, sorts them by `createdOn` descending (newest first) using `.localeCompare()`.

**`filteredTransactions` getter (computed property):**
1. Applies filter mode: `'all'`, `'sent'` (only debits), `'received'` (only credits)
2. Further filters by `searchQuery` matching amount, fromAccountId, or toAccountId

**`isCredit(t)`:** `return t.toAccountId === this.accId` — determines if a transaction is incoming (true) or outgoing (false)

**`getPoints(t)`:**
- Returns 0 for credit transactions (you don't earn points for receiving money)
- Returns `t.points` if it's > 0
- Falls back to `Math.floor(t.amount / 100)` if the points field is missing/0 on a successful sent transaction

**`searchMatches(t)`:** Searches amount, fromAccountId, and toAccountId as strings for the query

**Key fields:** `transactions`, `filterMode`, `searchQuery`, `loading`

---

## `Rewards` Component
**Files:** `rewards.ts`, `rewards.html`, `rewards.css`
**Route:** `/rewards/:id`

**What it does:**
Shows a breakdown of all transactions that earned reward points.

**`ngOnInit()`:**
- Fetches all transactions
- Filters to only transactions where `fromAccountId === accId` (sent by this user) AND `points > 0`
- Calculates `totalPoints` by summing all `t.points` values across filtered transactions

**`showInfo()` / `closeInfo()`:** Toggles a `showPopup` boolean — shows/hides an info popup explaining how points are earned.

**`goRedeem()`:** Navigates to `/redeem/:id`

**Key fields:** `rewardTransactions`, `totalPoints`, `loading`, `showPopup`

---

## `Redeem` Component
**Files:** `redeem.ts`, `redeem.html`, `redeem.css`
**Route:** `/redeem/:id`

**What it does:**
The designated page for redeeming reward points. Currently a placeholder page.

**`ngOnInit()`:** Reads `:id` from route params. If invalid (≤ 0), redirects to root.

**`goDashboard()`:** Navigates back to `/dashboard/:id`

This component is the simplest in the project — no API calls, just route protection and navigation. The actual redemption logic would be implemented here in a future iteration.

---

## `Profile` Component
**Files:** `profile.ts`, `profile.html`, `profile.css`
**Route:** `/profile/:id`

**What it does:**
Shows user account details and provides a password change feature.

**`ngOnInit()`:** Loads user details via `accountholderservice.getUserById(accId)`.

**Password change flow:**
1. `openPasswordPopup()` — sets `passwordPopup = true`, clears previous messages
2. In the popup: user enters `currentPassword`, `newPassword`, `confirmPassword`
3. `changePassword()` — validates fields locally, then calls `authService.changePassword(current, new, confirm).subscribe()`
4. On success: calls `authService.logout()` (clears session) → navigates to `/login` (forces re-login with new password)
5. On error: shows `passwordError` message

**Key fields:** `user`, `loading`, `passwordPopup`, `currentPassword`, `newPassword`, `confirmPassword`, `passwordMessage`, `passwordError`, `savingPassword`

---

## `App` Component
**Files:** `app.ts`, `app.html`, `app.css`
**Selector:** `<app-root>`

**What it does:**
The root component. Its template (`app.html`) contains only `<router-outlet />`, which is the placeholder where Angular inserts the active route's component.

`ngOnInit()` handles auto-login: if a session token exists in `sessionStorage` and the user is at `/` or `/login`, they're redirected to `/dashboard/:id`. This handles page refresh — Angular re-reads the session and routes accordingly.

---

## `app.module.server.ts`
**Files:** `app.module.server.ts`

**What it does:**
Extends `AppModule` for server-side rendering. Adds `provideServerRendering(withRoutes(serverRoutes))` which tells Angular SSR which routes to pre-render and how.

---

# SUMMARY TABLE

## Backend File Count by Layer
| Layer | Files |
|---|---|
| Entry point | 1 |
| Configuration + Security | 3 |
| Controllers | 3 |
| Service interfaces + implementations | 4 |
| Repositories | 3 |
| Entities | 3 |
| DTOs | 5 |
| Enums | 2 |
| Exceptions | 7 |
| Config | 1 (application.properties) |
| Build | 1 (pom.xml) |
| Tests | 3 |
| **Total** | **36** |

## Frontend File Count by Type
| Type | Files |
|---|---|
| Entry points + server | 3 (main.ts, main.server.ts, server.ts) |
| Root module + routing + SSR config | 4 |
| Root component | 3 (app.ts, app.html, app.css) |
| Services | 3 (auth, accountholder, interceptor) + 1 (transfer-service) |
| Models + Interfaces | 4 |
| Components (ts + html + css each) | 9 × 3 = 27 |
| Global styles | 1 (styles.css) |
| Shell | 1 (index.html) |
| Config | 2 (package.json, angular.json) |
| **Total** | **~49** |

---

*Document generated from full source code analysis of the Money Transfer System project.*
*Backend: Spring Boot 3.0.13 · Java 17 · MySQL 8 · Spring Security · JPA/Hibernate*
*Frontend: Angular 21 · TypeScript 5.9 · Chart.js 4 · RxJS 7.8 · ng2-charts*
