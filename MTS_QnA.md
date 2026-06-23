# Money Transfer System — Complete Q&A Guide

---

## SECTION 1: PROJECT OVERVIEW & ARCHITECTURE

**Q1. What is this project?**
A full-stack money transfer web app. Users can register, log in, send money between accounts, view transaction history, earn reward points, and manage their profile.
- Backend: Spring Boot 3 REST API + MySQL
- Frontend: Angular 21 SPA
- Auth: HTTP Basic Auth via Spring Security

**Q2. Explain the overall architecture.**
Decoupled client-server:
- Angular (port 4200) ↔ HTTP REST calls ↔ Spring Boot (port 8080) ↔ MySQL DB
- All API calls require HTTP Basic Auth header except `/api/v1/accounts/register`
- Frontend HTTP Interceptor auto-injects the auth token on every request

**Q3. How many layers are there in the backend?**
4 layers:
1. Controller layer — handles HTTP requests/responses
2. Service layer — business logic (interfaces + implementations)
3. Repository layer — DB access via Spring Data JPA
4. Entity/Model layer — JPA-mapped database tables

**Q4. What are all the controllers and what does each do?**
- `AccountController` — registration, get account by ID, get balance, get transactions
- `TransactionController` — execute money transfer
- `SecurityController` — authenticate (`/auth`), change password (`/auth/change-password`)

**Q5. What are the three database tables?**
- `accounts` — stores account info, balance, status, reward points
- `transaction_logs` — every transfer attempt (success or failure) with idempotency key
- `user_credentials` — username and password linked to account ID

**Q6. What design pattern separates `AccountService` from `AccountServiceImpl`?**
Interface-Implementation pattern (also called Strategy / Dependency Inversion). The controller depends on the `AccountService` interface, not the concrete class. This allows easy mocking in tests and swapping implementations without changing the controller.

**Q7. What are the two enums used?**
- `AccountStatus` — `ACTIVE`, `LOCKED`, `CLOSED`
- `TransactionStatus` — `SUCCESS`, `FAILURE`

**Q8. What are all the custom exceptions?**
`AccountNotFoundException`, `AccountNotActiveException`, `InsufficientBalanceException`, `SelfTransferException`, `NegativeAmountException`, `DuplicateTransferException`

**Q9. What are all the DTOs?**
- `TransferRequestDto` — fromAccountId, toAccountId, amount, idempotencyKey (sent to backend)
- `TransferResponseDto` — id (TRX-UUID), finalMessage, status, from/to IDs, amount (returned after transfer)
- `ErrorResponseDto` — errorCode (e.g. ACC-404), errorMessage (returned on failure)
- `ChangePasswordRequestDto` — currentPassword, newPassword, confirmPassword
- `AccountResponseDto` — account info returned to frontend

---

## SECTION 2: SPRING BOOT CORE

**Q10. What does `@SpringBootApplication` do?**
Combines three annotations:
- `@Configuration` — marks class as a source of bean definitions
- `@EnableAutoConfiguration` — tells Spring Boot to auto-configure beans based on classpath
- `@ComponentScan` — scans the current package and sub-packages for Spring components

**Q11. What is Spring's IoC container?**
IoC = Inversion of Control. The container creates, wires, and manages the lifecycle of beans. Instead of the developer doing `new Service()`, Spring creates and injects instances. `ApplicationContext` is the IoC container in Spring.

**Q12. What is dependency injection? How does Spring do it?**
DI = providing an object's dependencies from outside rather than creating them inside.
Spring uses `@Autowired` (field, setter, or constructor injection) to inject beans. Under the hood, Spring uses reflection or constructor calls to provide dependencies at startup.

**Q13. Difference between `@Component`, `@Service`, `@Repository`, `@Controller`?**
All are specialisations of `@Component` — Spring registers all of them as beans.
- `@Service` — semantic marker for business logic; enables transactional behaviour
- `@Repository` — wraps DB exceptions into Spring's `DataAccessException`
- `@Controller` — marks a web request handler
- `@Component` — generic bean

**Q14. What is `@Bean` and when do you use it?**
A method-level annotation inside a `@Configuration` class. The return value of the method is registered as a Spring bean. Used when you can't annotate a class directly (e.g. third-party classes like `PasswordEncoder`).

**Q15. What does `application.properties` configure?**
- `spring.datasource.url` — DB connection URL (MySQL on port 3306, DB name `mts_fidelity`)
- `spring.datasource.username/password` — DB credentials
- `spring.jpa.hibernate.ddl-auto=update` — auto-create/update tables on startup
- `spring.jpa.show-sql=true` — prints SQL to console
- `spring.jpa.properties.hibernate.dialect=MySQL8Dialect` — tells Hibernate which SQL dialect to use

**Q16. What is auto-configuration in Spring Boot?**
Spring Boot reads the classpath and automatically configures beans. E.g., if `spring-data-jpa` and a datasource URL are present, it auto-configures `EntityManagerFactory`, `TransactionManager`, etc. Without Spring Boot, you'd configure all these manually.

---

## SECTION 3: REST API & CONTROLLERS

**Q17. What does `@RestController` do?**
Combination of `@Controller` + `@ResponseBody`. Every method return value is automatically serialised to JSON and written to the HTTP response body. No need to annotate each method with `@ResponseBody`.

**Q18. What does `@RequestMapping("api/v1/")` on the class do?**
Sets a base URL prefix for all endpoints in that controller. So `@GetMapping("/accounts/{id}")` inside the class resolves to `/api/v1/accounts/{id}`.

**Q19. What does `@PathVariable` do? Give an example.**
Extracts a value from the URL path. Example: `@GetMapping("/accounts/{id}")` with `@PathVariable long id` — if the URL is `/api/v1/accounts/5`, then `id = 5`.

**Q20. What does `@RequestBody` do?**
Deserialises the incoming HTTP request body (JSON) into a Java object. Spring uses Jackson to convert JSON fields to the object's fields automatically.

**Q21. What is `ResponseEntity` and why use it?**
A wrapper that lets you control the HTTP status code, headers, and body in one object. Example: `ResponseEntity.status(HttpStatus.CREATED).body(res)` sends a 201 status with the body.

**Q22. What does `@CrossOrigin(origins = "http://localhost:4200")` do?**
Adds CORS headers to the response: `Access-Control-Allow-Origin: http://localhost:4200`. Without this, the browser would block the Angular app's HTTP calls because they're on a different port (different origin).

**Q23. What is CORS and why does it exist?**
CORS = Cross-Origin Resource Sharing. Browsers enforce the Same-Origin Policy — JavaScript can only call its own origin by default. CORS headers from the server tell the browser it's safe to allow cross-origin calls. Server enforces it; browser respects it.

**Q24. What HTTP status codes are used and why?**
- `200 OK` — successful GET or transfer
- `201 CREATED` — successful registration (new resource created)
- `400 BAD_REQUEST` — invalid input, insufficient balance, negative amount, self-transfer
- `401 UNAUTHORIZED` — bad credentials
- `403 FORBIDDEN` — account not active
- `404 NOT_FOUND` — account doesn't exist
- `409 CONFLICT` — duplicate transfer

**Q25. Why is the `register` endpoint in `AccountController` and not `SecurityController`?**
Registration is an account creation operation (saves `Account` + `UserCredentials`), so it logically belongs in `AccountController`. `SecurityController` handles auth operations (login verification, password change) on existing users.

**Q26. What is `Principal` in `SecurityController`?**
`Principal` is a Spring Security interface representing the currently authenticated user. Spring automatically injects it into controller methods. `principal.getName()` returns the username (which is the account ID in this project).

---

## SECTION 4: SERVICE LAYER & BUSINESS LOGIC

**Q27. Walk through `transfer()` in `TransferServiceImpl` step by step.**
1. Fetch `fromAccount` and `toAccount` from DB using their IDs (throws `AccountNotFoundException` if either missing)
2. Extract `amount` and `idempotencyKey` from the DTO
3. Call `validateTransfer()` — runs all business rule checks
4. If validation passes, call `executeTransfer()` which does the actual debit/credit
5. Return `TransferResponseDto` with TRX-UUID, status SUCCESS, and amounts

**Q28. What are all the validations in `validateTransfer()`?**
In order:
1. `fromAccount.id == toAccount.id` → `SelfTransferException`
2. `!senderAcc.isActive()` → `AccountNotActiveException` (sender)
3. `!receiverAcc.isActive()` → `AccountNotActiveException` (receiver)
4. `amount < 0` → `NegativeAmountException`
5. `amount > sender balance` → `InsufficientBalanceException`

Each failure: creates a `TransactionLog` with `FAILURE` status → saves it → throws exception.

**Q29. Why is a `TransactionLog` saved even when validation fails?**
For audit trail. In banking systems, you must be able to explain every rejected attempt — for compliance, debugging, and fraud detection. The log stores the `failureReason` field.

**Q30. Why is `@Transactional` on `executeTransfer()` but not on `validateTransfer()`?**
`executeTransfer` modifies multiple records (sender balance, receiver balance, transaction log). If any step fails mid-way, `@Transactional` rolls back all changes — ensuring atomicity. `validateTransfer` only does reads and one log write — the log save is intentionally permanent even if an exception is thrown, so it's not inside the transaction scope.

**Q31. What happens if `transactionLogRepository.save()` inside `executeTransfer()` fails?**
Since `executeTransfer` is `@Transactional`, the whole transaction rolls back — both `saveAndFlush(senderAcc)` and `saveAndFlush(receiverAcc)` are undone. No partial state is committed. The balances return to what they were before.

**Q32. What is `saveAndFlush()` vs `save()`?**
- `save()` — marks entity as dirty; writes to DB at end of transaction or when Hibernate decides (lazy flush)
- `saveAndFlush()` — immediately writes to DB within the current transaction. Used in `executeTransfer` to ensure balance updates are visible before the transaction log is saved.

**Q33. How is reward points calculated?**
```java
int points = 0;
if (amount.compareTo(new BigDecimal("100")) > 0) {
    points = amount.divideToIntegralValue(new BigDecimal("100")).intValue();
}
```
Integer division: ₹350 → 3 points, ₹1200 → 12 points, ₹80 → 0 points. Points stored on both the `TransactionLog` and the sender's `Account.totalRewardPoints`.

**Q34. What does `BigDecimal.compareTo()` return?**
- Returns `-1` if less than, `0` if equal, `1` if greater than the compared value.
- Must be used instead of `==` or `equals()` for numeric comparison because `equals()` on BigDecimal includes scale (`5.0 != 5.00` with `equals`).

**Q35. What does `BigDecimal.divideToIntegralValue()` do?**
Integer division — returns only the whole-number part, no rounding. `new BigDecimal("350").divideToIntegralValue(new BigDecimal("100"))` = `3`.

**Q36. How does `addAccount()` return the auto-generated ID?**
`accountRepo.save(e)` — Spring Data JPA calls Hibernate, which executes the INSERT and populates the entity's `@Id` field via `GenerationType.IDENTITY` (MySQL `AUTO_INCREMENT`). After `save()`, `e.getId()` returns the DB-generated ID.

**Q37. What does `getTransactions()` do when a user has no transactions?**
It merges `fromTrans` and `toTrans` lists, and if the combined list size is 0, it throws `AccountNotFoundException("No transactions found for Account ID: " + id)`. So it treats "no transactions" as a not-found scenario.

---

## SECTION 5: JPA, HIBERNATE & DATABASE

**Q38. What is JPA vs Hibernate vs Spring Data JPA?**
- **JPA** — Java specification for ORM (defines annotations like `@Entity`, `@Id`, `@Column`)
- **Hibernate** — the most popular JPA implementation; generates and executes SQL
- **Spring Data JPA** — abstraction layer over JPA; provides `JpaRepository`, auto-query generation, removes boilerplate DAO code

**Q39. What does `@Entity` do?**
Marks the class as a JPA-managed entity. Hibernate maps it to a database table. Required for any class you want to persist.

**Q40. What does `@Table(name="accounts")` do?**
Specifies the exact database table name. Without it, Hibernate defaults to the class name (e.g. `Account` → `account` or `Account`). Here it maps to the `accounts` table.

**Q41. What is `GenerationType.IDENTITY` vs `AUTO`?**
- `IDENTITY` — uses DB's native auto-increment (`AUTO_INCREMENT` in MySQL). DB generates the ID on INSERT. Best for MySQL.
- `AUTO` — Hibernate picks a strategy. In MySQL with UUID fields, it may not use AUTO_INCREMENT. That's why `TransactionLog.id` (UUID type) uses `AUTO` but still requires manual `transactionLog.setId(UUID.randomUUID())` — Hibernate's AUTO for UUID on MySQL doesn't auto-generate it.

**Q42. What does `@Enumerated(EnumType.STRING)` do?**
Stores the enum constant's name as a string in the DB column (`"ACTIVE"`, `"SUCCESS"`). Without it, the default `EnumType.ORDINAL` stores the enum index (0, 1, 2). String is safer — adding a new enum value in the middle won't corrupt old data.

**Q43. What does `@Column(precision=18, scale=2)` mean?**
For `BigDecimal` columns: `precision=18` means 18 total digits, `scale=2` means 2 decimal places. Maps to `DECIMAL(18,2)` in MySQL. Supports values up to 9,999,999,999,999,999.99.

**Q44. What is `insertable=false, updatable=false` on `@JoinColumn`?**
When an entity has both a plain FK column (`fromAccountId`) and a `@ManyToOne` relationship pointing to the same column, you need to tell JPA which one controls writes. `insertable=false, updatable=false` means the relationship object is read-only — JPA uses only the primitive `fromAccountId` field for INSERT/UPDATE. The relationship is only used for reading the joined `Account` object.

**Q45. Why does `TransactionLog` have both `fromAccountId` (long) and `FromAccount` (`@ManyToOne`)?**
The primitive `fromAccountId` is used for writes (INSERT/UPDATE). The `@ManyToOne Account FromAccount` is used for reads (JOIN queries, accessing account details). Using `insertable=false, updatable=false` prevents a mapping conflict.

**Q46. What does `spring.jpa.hibernate.ddl-auto=update` do?**
On startup, Hibernate compares the entity classes with the existing DB schema and adds missing tables/columns. It never drops existing columns. Fine for development but dangerous in production — use `validate` or `none` with migration tools (Flyway/Liquibase) in prod.

**Q47. What is a derived query method? Give examples from this project.**
Spring Data JPA parses method names and generates SQL:
- `findById(long id)` → `SELECT * FROM accounts WHERE id = ?`
- `findAllByFromAccountId(long id)` → `SELECT * FROM transaction_logs WHERE from_account_id = ?`
- `findByIdempotencyKey(String key)` → `SELECT * FROM transaction_logs WHERE idempotency_key = ?`
- `findByUsername(String username)` → `SELECT * FROM user_credentials WHERE username = ?`

**Q48. What does `@Column(columnDefinition="VARCHAR(100) UNIQUE")` do on idempotency_key?**
Creates a UNIQUE constraint at the database level on that column. Even if the application-level check fails (race condition), the DB will reject a second INSERT with the same key — throwing a constraint violation exception.

**Q49. What is `Optional<T>` and why use it?**
Java 8 wrapper that represents a value that may or may not be present. Prevents `NullPointerException`. Instead of returning `null`, a repository returns `Optional<Account>`. You check `.isPresent()` before calling `.get()`.

**Q50. What is the `version` field on `Account` and what's wrong with it?**
It's intended for optimistic locking — JPA uses `@Version` to track concurrent modifications. However, in this project, `@Version` annotation is **missing**. The field exists in the DB but JPA doesn't use it for lock detection. It's an incomplete implementation. Adding `@Version` above the field would enable proper optimistic locking.

**Q51. What is optimistic locking?**
A concurrency strategy where each entity has a version number. When updating, JPA checks if the version in the DB still matches what was read. If another transaction already updated it (version changed), it throws `OptimisticLockException`. Prevents lost updates without holding a DB lock.

**Q52. Is there a type mismatch in `AccountRepository extends JpaRepository<Account, Integer>`?**
Yes. The `Account.id` field is `long` (primitive), but the repository declares `Integer` as the ID type. The explicit `findById(long id)` override masks the issue for reads, but `JpaRepository`'s built-in methods like `deleteById(Integer id)` could cause type confusion. It should be `JpaRepository<Account, Long>`.

---

## SECTION 6: SPRING SECURITY

**Q53. How does Spring Security work in this project?**
Two config classes:
- `SpringSecurityConfig` — disables CSRF, permits OPTIONS (CORS preflight), requires auth on all other requests, sets custom 401 JSON response
- `SecurityConfig` — configures CORS, explicitly permits `/api/v1/accounts/register`, sets `STATELESS` session policy, defines `PasswordEncoder` bean
- `CustomUserDetailsService` — loads user from DB by username for authentication

**Q54. What is HTTP Basic Authentication?**
Credentials (username:password) are Base64-encoded and sent in the `Authorization: Basic <encoded>` header on every request. Stateless — no session needed. Must be used over HTTPS in production because Base64 is trivially reversible.

**Q55. Why is CSRF disabled?**
CSRF attacks exploit session cookies — the browser auto-sends cookies, so a malicious site can forge requests. This API uses HTTP Basic Auth (no cookies, no session). With stateless auth, CSRF is not a risk, so disabling it removes unnecessary token requirements on API calls.

**Q56. What is `SessionCreationPolicy.STATELESS`?**
Tells Spring Security never to create an HTTP session. Every request must carry its own auth credentials. Correct for REST APIs — enables horizontal scaling and avoids server-side session management.

**Q57. What does `CustomUserDetailsService.loadUserByUsername()` do?**
Queries `user_credentials` table by username. Returns a `UserDetails` object built with `User.builder()` containing username, password (plain text), and role `"USER"`. Spring Security uses this to validate the submitted credentials.

**Q58. Why is there a `ROLE_` prefix behaviour with `.roles("USER")`?**
Spring Security's `.roles("USER")` automatically prefixes with `ROLE_`, storing it as `ROLE_USER` internally. When checking access with `.hasRole("USER")`, Spring adds the prefix automatically. If you use `.authorities("ROLE_USER")` you must include the prefix manually.

**Q59. What is the `PasswordEncoder` in `SecurityConfig` doing?**
It's a custom inline implementation that stores passwords in plain text — `encode()` returns the raw string, `matches()` does a plain string comparison. This is **not safe for production**. BCrypt should be used (`new BCryptPasswordEncoder()`), which hashes passwords with a salt.

**Q60. What is the risk of two `SecurityFilterChain` beans (`SpringSecurityConfig` + `SecurityConfig`)?**
Spring Boot applies multiple `SecurityFilterChain` beans if they have different `@Order` values. Without ordering, the last loaded wins. Here both try to configure security independently, which can cause conflicts or unexpected override of rules. Ideally, all security config should be in one class.

**Q61. What does the custom `authenticationEntryPoint` do?**
Customises the response when authentication fails. Instead of Spring's default HTML error page, it returns a JSON response: `{"message": "Invalid username or password"}` with HTTP 401. Needed because the frontend expects JSON, not HTML.

**Q62. What is `@ControllerAdvice` in `MtsGlobalNotFoundException`?**
`@ControllerAdvice` is a global exception handler. It intercepts exceptions thrown by any controller. Each `@ExceptionHandler(SomeException.class)` method handles a specific exception type and returns a structured `ResponseEntity<ErrorResponseDto>`.

**Q63. What HTTP status does each exception return?**
| Exception | Error Code | HTTP Status |
|---|---|---|
| `AccountNotActiveException` | ACC-403 | 403 FORBIDDEN |
| `AccountNotFoundException` | ACC-404 | 404 NOT_FOUND |
| `DuplicateTransferException` | TRX-409 | 409 CONFLICT |
| `InsufficientBalanceException` | TRX-400 | 400 BAD_REQUEST |
| `SelfTransferException` | VAL-422 | 400 BAD_REQUEST |
| `NegativeAmountException` | VAL-422 | 400 BAD_REQUEST |

---

## SECTION 7: ENTITIES & DATA MODEL

**Q64. What fields does `Account` have?**
`id` (BIGINT, auto-increment), `holderName` (VARCHAR 255), `balance` (DECIMAL 18,2), `status` (VARCHAR 20, enum), `version` (INT DEFAULT 0), `lastUpdated` (TIMESTAMP), `totalRewardPoints` (INT DEFAULT 0)

**Q65. What does `account.isActive()` check?**
`return status == AccountStatus.ACTIVE;`
Only `ACTIVE` status is considered active. `LOCKED` and `CLOSED` both return false.

**Q66. What are `debit()` and `credit()` on `Account`?**
- `debit(currentBal, amount)` → `currentBal.subtract(amount)` — returns new balance after deducting amount
- `credit(currentBal, amount)` → `currentBal.add(amount)` — returns new balance after adding amount
They don't modify the entity directly — they return the computed value; the service then calls `setBalance()`.

**Q67. Why is `UserCredentials.accountId` both the PK and FK?**
The account ID is used as the primary key for `UserCredentials` (one-to-one relationship with `Account`). This is a "shared primary key" pattern — no separate surrogate key needed because each account has exactly one credential record.

**Q68. Why does `TransactionLog.id` use `CHAR(36)` in the DB?**
UUID in standard string format is 36 characters (32 hex digits + 4 hyphens, e.g. `550e8400-e29b-41d4-a716-446655440000`). `CHAR(36)` is a fixed-length column — faster for indexed lookups on known-length strings.

**Q69. What does `@Column(name="last_updated", columnDefinition="TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")` do?**
MySQL automatically updates this column to the current timestamp whenever any other column in that row is updated. This means `lastUpdated` always reflects when the account was last modified without requiring manual code to set it (though the code also explicitly sets it with `LocalDateTime.now()`).

---

## SECTION 8: IDEMPOTENCY & CONCURRENCY

**Q70. What is idempotency and why does it matter here?**
An operation is idempotent if doing it multiple times produces the same result as doing it once. In money transfers, if a user double-clicks "Send" or the network retries, the same transfer must not execute twice. Idempotency prevents double-spending.

**Q71. How is idempotency implemented in this project?**
1. Frontend generates a fresh `UUID v4` (`uuidv4()`) for each transfer attempt
2. UUID is sent in `TransferRequestDto.idempotencyKey`
3. Backend checks `transactionLogRepository.findByIdempotencyKey(key).size() != 0` before executing
4. If found → throws `DuplicateTransferException` (409 CONFLICT)
5. DB has `UNIQUE` constraint on `idempotency_key` column as a final safety net

**Q72. Why is the UUID generated on the frontend, not the backend?**
If generated on the backend, a network failure after execution but before the response reaches the frontend means the frontend has no key to retry with. By generating it on the frontend before the request, the same key can be re-sent on retry — the backend will detect the duplicate and not execute again.

**Q73. Is the idempotency check race-condition safe?**
Not fully. Two simultaneous requests with the same key could both pass the `findByIdempotencyKey` check (both see empty result), then both proceed to `executeTransfer`. The DB `UNIQUE` constraint on `idempotency_key` would catch the second INSERT and throw a constraint violation. But balance could be deducted twice before that point. A proper fix: use `SELECT FOR UPDATE` or optimistic locking.

**Q74. What is a race condition in concurrent balance updates?**
Two transfers from Account A (balance ₹1000) arrive simultaneously. Both read balance = ₹1000. Both pass the `InsufficientBalanceException` check. Both debit ₹800. Both save balance = ₹200. Account ends up at ₹200 instead of being rejected for insufficient funds. Fix: pessimistic locking (`@Lock(PESSIMISTIC_WRITE)`) or optimistic locking (`@Version`).

---

## SECTION 9: TESTING

**Q75. What testing framework is used?**
JUnit 5 (`@Test`, `@BeforeEach`, `assertThrows`, `assertEquals`) + Mockito (`@Mock`, `@InjectMocks`, `when().thenReturn()`, `verify()`).

**Q76. What does `@Mock` do?**
Creates a mock (fake) object for the annotated type. The mock doesn't call real methods — you define what it returns using `when(...).thenReturn(...)`. Used to isolate the unit being tested.

**Q77. What does `@InjectMocks` do?**
Creates a real instance of the class and injects all `@Mock` fields into it. In `TransferRequestValidationTest`, `@InjectMocks TransferServiceImpl transferService` creates a real `TransferServiceImpl` but with mocked `accountService`, `accountRepository`, and `transactionLogRepository`.

**Q78. What does `MockitoAnnotations.openMocks(this)` do?**
Initialises all `@Mock` and `@InjectMocks` annotations in the test class. Must be called in `@BeforeEach` when not using `@ExtendWith(MockitoExtension.class)`.

**Q79. What does `when(accountService.getAccount(fromId)).thenReturn(fromAccount)` do?**
Stubs the mock: whenever `accountService.getAccount(fromId)` is called, return `fromAccount` instead of calling the real DB. This allows testing business logic without a real database.

**Q80. What does `verify(accountRepository, times(2)).saveAndFlush(any(Account.class))` test?**
Asserts that `accountRepository.saveAndFlush()` was called exactly 2 times with any `Account` object. This verifies that both sender and receiver accounts were updated/saved during the transfer.

**Q81. What is the bug in `testValidTransfer()`?**
`toAccount` has status `AccountStatus.CLOSED`. According to `validateTransfer()`, a closed receiver should throw `AccountNotActiveException`. But the test expects the transfer to **succeed** (`assertNotNull(result)`). This is a test bug — it passes only because the mock `transactionLogRepository.findByIdempotencyKey()` returns empty list (no duplicate), but the real validation should reject it. The test doesn't reflect actual business behaviour.

**Q82. What does `assertThrows(NegativeAmountException.class, () -> transferService.transfer(request))` test?**
Asserts that calling `transfer(request)` throws a `NegativeAmountException`. The lambda `() -> ...` is the code expected to throw the exception. If it doesn't throw, or throws a different exception, the test fails.

**Q83. What does `testNullFields()` verify?**
Tests the default values of a no-arg `TransferRequestDto`:
- `fromAccountId` and `toAccountId` default to `0L` (Java primitive `long` default)
- `amount` is `null` (object reference)
- `idempotencyKey` is `null` (object reference)

**Q84. What is the difference between a unit test and an integration test?**
- Unit test — tests one class in isolation; mocks all dependencies. Fast, no DB, no Spring context.
- Integration test — tests multiple layers together; uses real DB (or H2 in-memory), loads Spring context (`@SpringBootTest`). Slower but more realistic.

**Q85. What is `@BeforeEach`?**
JUnit 5 annotation. The method runs before each `@Test` method in the class. Used to reset state (re-create `Account`, re-init mocks) so tests are independent of each other.

---

## SECTION 10: ANGULAR & TYPESCRIPT

**Q86. What is `NgModule` and what does `@NgModule` configure?**
`NgModule` is Angular's module system. `@NgModule` configures:
- `declarations` — components, directives, pipes that belong to this module
- `imports` — other modules whose exports are needed here
- `providers` — services/interceptors for DI
- `bootstrap` — root component to start the app

**Q87. What are all the components in this project?**
`App`, `Login`, `Register`, `Dashboard`, `Transfer`, `History`, `Rewards`, `Redeem`, `Profile` — 9 components total, all declared in `AppModule`.

**Q88. What is `standalone: false` and why is it used?**
Means the component must be declared in an `NgModule` (traditional approach). Angular 14+ introduced standalone components that don't need `NgModule`. This project uses the module-based architecture, so all components have `standalone: false`.

**Q89. What does `RouterModule.forRoot(routes)` do?**
Registers the routes at the root level of the app. `forRoot` should only be called once — in the root module. It provides the `Router` service and `RouterOutlet`/`RouterLink` directives. Feature modules use `forChild()`.

**Q90. What is the `**` wildcard route?**
Catches any URL that doesn't match defined routes. Here it redirects to `/login`. Must be placed last in the routes array — Angular matches routes in order, and the wildcard would catch everything if placed first.

**Q91. What is `pathMatch: 'full'`?**
For the empty path (`''`), `pathMatch: 'full'` means the redirect only triggers when the URL is exactly `''` (empty). Without `'full'`, the empty path would prefix-match every URL (since every URL starts with an empty string), causing all routes to redirect to login.

**Q92. How does a component read a route parameter like `:id`?**
```typescript
this.accId = Number(this.route.snapshot.paramMap.get('id')) || 0;
```
`ActivatedRoute.snapshot.paramMap.get('id')` returns the current value of the `id` parameter as a string. `Number()` converts it to a number. `|| 0` provides a default if null/undefined.

**Q93. What is `OnInit` and `ngOnInit()`?**
`OnInit` is a lifecycle interface. `ngOnInit()` is called once after Angular initialises the component's data-bound properties. Used for initialisation logic like API calls. Preferred over the constructor for setup because inputs are available by the time `ngOnInit` runs.

**Q94. What is `ngAfterViewInit()`?**
Called after Angular initialises the component's view and child views. `@ViewChild` references (like the `<canvas>` for Chart.js) are available only after this lifecycle hook. Used in `Dashboard` to initialise the Chart.js doughnut chart.

**Q95. What is `@ViewChild('transactionChart')` in Dashboard?**
Gets a reference to the DOM element with template reference variable `#transactionChart` (the `<canvas>` element). Type `ElementRef<HTMLCanvasElement>` provides `.nativeElement` to access the raw canvas, needed for `canvas.getContext('2d')`.

**Q96. What is `ChangeDetectorRef.detectChanges()`?**
Manually triggers Angular's change detection for the component. Used in this project after async data arrives (subscriptions) — especially needed with SSR where Angular may not auto-detect changes. `cd.detectChanges()` ensures the view re-renders after data updates.

**Q97. What is `inject()` function vs constructor injection?**
Both are DI mechanisms:
- Constructor: `constructor(private service: Accountholderservice) {}`
- `inject()`: `private service = inject(Accountholderservice);` (at field level)

`inject()` is available since Angular 14. Works outside constructors, in functional guards, etc. This project mixes both patterns.

**Q98. What is `PLATFORM_ID` and why inject it?**
Angular with SSR runs on both browser and Node.js server. `PLATFORM_ID` is a token injected by Angular with value `'browser'` or `'server'`. `isPlatformBrowser(platformId)` returns `true` only in the browser. Used to guard `sessionStorage` calls — `sessionStorage` doesn't exist in Node.js.

---

## SECTION 11: ANGULAR SERVICES & HTTP

**Q99. What does `AuthService.authenticate()` do?**
1. Combines username and password: `username:password`
2. Base64-encodes using `btoa()` (with Node.js fallback via `Buffer.from()`)
3. Makes `GET /auth` with `Authorization: Basic <encoded>` header
4. On success (via `pipe(map(...))`) — stores username in `SESSION_KEY` and token in `TOKEN_KEY` in `sessionStorage`
5. Returns the server response (UserCredentials object)

**Q100. What is `btoa()` and why is there a fallback?**
`btoa()` is a browser built-in that Base64-encodes a string. In Node.js (SSR), `btoa` doesn't exist. The fallback `Buffer.from(credentials).toString('base64')` is the Node.js equivalent. The code checks `typeof btoa !== 'undefined'` before using it.

**Q101. What is `sessionStorage` vs `localStorage`?**
- `sessionStorage` — data cleared when the browser tab closes. Scoped to the tab.
- `localStorage` — data persists across sessions until explicitly cleared.
For auth tokens, `sessionStorage` is safer — auto-expires when the user closes the tab, reducing token theft risk.

**Q102. What does the HTTP Interceptor (`HttpinterceptorService`) do?**
Implements `HttpInterceptor`. The `intercept()` method runs before every outgoing HTTP request. It reads the auth token from `AuthService.getAuthToken()`, and if present, clones the request with an added `Authorization: Basic <token>` header. This eliminates duplicating auth code in every service method.

**Q103. How is the interceptor registered in `AppModule`?**
```typescript
{
  provide: HTTP_INTERCEPTORS,
  useClass: HttpinterceptorService,
  multi: true
}
```
`HTTP_INTERCEPTORS` is a multi-provider token. `multi: true` means it adds to the list of interceptors rather than replacing any existing ones.

**Q104. What is `multi: true` in a provider?**
Angular normally allows only one provider per token. `multi: true` creates a collection — multiple providers for the same token are all registered and returned as an array. `HTTP_INTERCEPTORS` is a multi-token; all registered interceptors form a chain.

**Q105. Why does `TransferService` use an absolute URL `http://localhost:8080/...` while `Accountholderservice` uses a relative path `/api/v1/...`?**
`Accountholderservice` uses relative paths — these are proxied through the Angular dev server's proxy config (or the browser resolves relative to the current domain). `TransferService` uses a hardcoded absolute URL — this is less flexible (breaks in production or if the backend moves) and should ideally also be a relative path or an environment variable.

**Q106. What is an `Observable` and what does `.subscribe()` do?**
An `Observable` (from RxJS) represents a stream of async data. It's lazy — nothing happens until `.subscribe()` is called. `.subscribe({ next, error, complete })` activates the stream: `next` handles each emitted value, `error` handles errors, `complete` runs when the stream ends.

**Q107. What does `pipe(map(...))` do in `authenticate()`?**
`pipe()` chains RxJS operators. `map()` transforms each emitted value. Here, `map((res) => { sessionStorage.setItem(...); return res; })` — saves the token as a side effect and passes through the original response unchanged.

---

## SECTION 12: ANGULAR COMPONENTS IN DETAIL

**Q108. What does `Login.ngOnInit()` do?**
Checks if user is already logged in (`authService.isUserLoggedin()`). If yes, navigates directly to `/dashboard/:userId` using the stored session username. Prevents a logged-in user from seeing the login page again.

**Q109. What is the `isSubmitting` flag used for?**
Prevents double-submission. Set to `true` when a request is sent, reset to `false` when the response (success or error) arrives. If `isSubmitting` is already `true`, `submitTransfer()` / `doLogin()` returns early without firing another request.

**Q110. What does `submitTransfer()` in `Transfer` do?**
1. Returns early if `isSubmitting` is true
2. Builds `TransferRequestDto` with `fromAccountId`, `toAccountId`, `amount`, and a fresh `uuidv4()` as `idempotencyKey`
3. Calls `transferService.executeTransfer(request).subscribe(...)`
4. On success: sets `transactionId`, shows success modal
5. On error: extracts `errorCode` and `errorMessage` from `err.error`, shows error modal

**Q111. How does the error modal know what error to show?**
The backend returns `ErrorResponseDto` with `errorCode` and `errorMessage` fields. Angular's HTTP error wraps the response body in `err.error`. The component extracts: `this.errorCode = body?.errorCode` and `this.errorMessage = body?.errorMessage`. Multiple fallbacks (`body?.message`, `body?.finalMessage`) handle inconsistencies.

**Q112. What does the `filteredTransactions` getter in `History` do?**
A computed property that:
1. Starts with all transactions
2. If `filterMode === 'sent'` → filters out credit transactions (`toAccountId !== accId`)
3. If `filterMode === 'received'` → filters out debit transactions (`toAccountId === accId`)
4. Further filters by `searchQuery` matching amount, fromAccountId, or toAccountId as strings

**Q113. What does `isCredit(t)` check in `History`?**
`return t.toAccountId === this.accId` — if the current user is the receiver, it's a credit (money coming in). If `fromAccountId === accId`, it's a debit (money going out).

**Q114. How does `Dashboard.calculateStats()` work?**
Iterates all transactions. For each `SUCCESS` transaction:
- If `toAccountId === accId` → increment `receivedCount`, add to `totalReceived`
- If `fromAccountId === accId` → increment `sentCount`, add to `totalSent`

Only counts successful transactions, not failures.

**Q115. Why is `setTimeout(..., 100)` used before `initializeChart()`?**
Chart.js needs the `<canvas>` element to be fully rendered in the DOM before it can draw on it. Even inside `ngAfterViewInit`, there can be a brief delay when data arrives asynchronously. The 100ms timeout gives the view time to re-render with the new data before Chart.js tries to access the canvas context.

**Q116. What does `chart.destroy()` do and why is it called?**
Destroys the existing Chart.js instance and removes it from the canvas. If you call `new Chart(ctx, config)` without destroying the previous one, Chart.js throws an error ("Canvas is already in use"). Must be called before re-creating the chart.

**Q117. What does `uuidv4()` generate in the Transfer component?**
A random UUID v4 — 128-bit random number formatted as `xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx`. Imported as `import { v4 as uuidv4 } from 'uuid'`. Called fresh on every transfer submission to generate a unique idempotency key.

---

## SECTION 13: SECURITY DEEP DIVE

**Q118. What is the difference between authentication and authorisation?**
- Authentication — verifying who you are (login with username/password)
- Authorisation — verifying what you're allowed to do (can this user access this resource?)
This project uses Basic Auth for authentication and `@authenticated()` rules for authorisation (any authenticated user can access all endpoints).

**Q119. What would happen if a request is sent without an Authorization header?**
Spring Security intercepts the request before it reaches the controller. Since the endpoint requires authentication, the custom `authenticationEntryPoint` returns HTTP 401 with `{"message": "Invalid username or password"}`.

**Q120. Is Base64 encoding the same as encryption?**
No. Base64 is an encoding, not encryption. It can be trivially decoded. HTTP Basic Auth over plain HTTP is insecure — credentials are visible to anyone who can intercept the traffic. Must use HTTPS in production to encrypt the transport layer.

**Q121. How does Spring Security know which `PasswordEncoder` to use?**
Spring Security auto-discovers the `PasswordEncoder` bean. Since `SecurityConfig` defines a `@Bean PasswordEncoder`, Spring uses it to call `encoder.matches(rawPassword, storedPassword)` during authentication.

**Q122. What does `setAllowCredentials(true)` in CORS config do?**
Allows the browser to send cookies/auth headers in cross-origin requests. Necessary when using `Authorization` headers from `http://localhost:4200` to `http://localhost:8080`. When `allowCredentials=true`, `allowedOrigins` must be specific URLs (not wildcard `*`).

**Q123. What does `setMaxAge(3600L)` do in CORS?**
Tells the browser to cache the CORS preflight (`OPTIONS`) response for 3600 seconds (1 hour). Without this, the browser sends a preflight OPTIONS request before every cross-origin API call. Caching reduces unnecessary round trips.

**Q124. What is SQL injection and is this project vulnerable?**
SQL injection is inserting malicious SQL in user input to manipulate DB queries. This project uses Spring Data JPA with parameterised queries (Hibernate generates prepared statements). Parameterised queries automatically escape input — SQL injection is not possible through the ORM layer.

---

## SECTION 14: DESIGN & CODE QUALITY

**Q125. Why use DTOs instead of returning entities directly?**
- Security: hides internal DB structure and sensitive fields
- Decoupling: API contract is independent of the DB schema — you can change the entity without breaking the API
- Control: you choose exactly what fields are exposed
- Versioning: easier to evolve the API independently of the data model

**Q126. Does the project follow Single Responsibility Principle?**
Mostly yes:
- `AccountController` — HTTP handling
- `AccountServiceImpl` — account business logic
- `AccountRepository` — DB access
- `TransferServiceImpl` — transfer business logic
- `CustomUserDetailsService` — security user loading

One violation: `AccountController` directly uses `UserCredentialsRepository` (bypassing the service layer) in the `register` endpoint. Ideally, credential saving should be in a service.

**Q127. What would you improve in `validateTransfer()`?**
The method saves a `TransactionLog` before throwing each exception — this code is duplicated 5 times with only the `failureReason` string different. This should be extracted into a private helper:
```java
private void logAndThrow(Account from, Account to, BigDecimal amount, String key, String reason, RuntimeException ex) {
    saveFailureLog(from.getId(), to.getId(), amount, key, reason);
    throw ex;
}
```

**Q128. Why do custom exceptions extend `RuntimeException` and not `Exception`?**
`RuntimeException` is unchecked — doesn't need to be declared in `throws` clauses or caught explicitly. Checked exceptions (`Exception` subclasses) require `try-catch` or `throws` in every method in the call stack, adding boilerplate. For business rule violations in Spring apps, unchecked exceptions are preferred.

**Q129. What is `serialVersionUID = 1L` and `@Serial`?**
`serialVersionUID` is a version identifier for Java serialisation. If a class is serialised and the `serialVersionUID` changes (e.g. by adding a field), deserialisation fails. Setting `1L` manually avoids auto-generated IDs that change when the class changes. `@Serial` (Java 14+) is a documentation annotation marking serialisation-related fields — doesn't change behaviour, just improves tooling support.

**Q130. What naming issues exist in this project?**
- `MtsGlobalNotFoundException` — actually handles all exceptions, not just "not found"
- `getTransactionLogById` in `TransactionController` — actually executes a transfer, not fetching by ID
- `recieverAcc` — typo, should be `receiverAcc`
- `TransferRequestValidationTest.testValidTransfer()` — tests a transfer to a CLOSED account but expects success (logic bug)

---

## SECTION 15: MISCELLANEOUS JAVA

**Q131. Why use `BigDecimal` instead of `double` for money?**
`double` is IEEE 754 floating-point — cannot represent many decimal fractions exactly (e.g. `0.1 + 0.2 = 0.30000000000000004`). For financial calculations, rounding errors accumulate and cause incorrect results. `BigDecimal` provides arbitrary-precision arithmetic — exact representation and controlled rounding.

**Q132. What is `BigDecimal.valueOf(double)` vs `new BigDecimal(double)`?**
- `new BigDecimal(0.1)` → `0.1000000000000000055511151231257827021181583404541015625` (exact representation of the float)
- `BigDecimal.valueOf(0.1)` → `0.1` (uses the string representation)
Always use `BigDecimal.valueOf()` or `new BigDecimal("0.1")` (string) for monetary values.

**Q133. What does `LocalDateTime.now()` return?**
The current date and time without timezone info (system default). Returns a `LocalDateTime` object. For timezone-aware applications, `ZonedDateTime.now()` or `Instant.now()` are preferred. This project uses `LocalDateTime` — fine for a single-timezone app.

**Q134. What is `UUID.randomUUID()`?**
Generates a random UUID v4 — a 128-bit number formatted as `xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx`. Cryptographically random. Essentially impossible to predict or collide in practice (2^122 combinations).

**Q135. What is the difference between `isBlank()` and `isEmpty()`?**
- `isEmpty()` → true only if length is 0 (empty string `""`)
- `isBlank()` → true if empty OR contains only whitespace (spaces, tabs, newlines). `"  ".isEmpty()` = false, `"  ".isBlank()` = true. `isBlank()` is safer for user input validation.

**Q136. What does `allTrans.addAll(fromTrans)` do?**
Adds all elements of `fromTrans` list to `allTrans` list. Equivalent to `allTrans.addAll(fromTrans)`. After this and `allTrans.addAll(toTrans)`, `allTrans` contains all sent and received transactions.

**Q137. What does `Optional.orElseThrow()` do vs `.get()`?**
- `.get()` — throws `NoSuchElementException` if empty (generic error)
- `.orElseThrow(() -> new CustomException(...))` — throws a specific exception of your choice
This project uses `if (!opt.isPresent()) throw new AccountNotFoundException(...)` — equivalent to `orElseThrow` but more verbose. Using `orElseThrow` is more concise and idiomatic in modern Java.

---

## SECTION 16: BONUS / TRICKY QUESTIONS

**Q138. If two users simultaneously transfer from the same account, what happens?**
Without locking, both transactions read the same balance, both pass the `InsufficientBalanceException` check, and both debit the account. This causes the balance to go negative or below the expected minimum. Fix: add `@Version` for optimistic locking or use `@Lock(PESSIMISTIC_WRITE)` on the repository query.

**Q139. What would happen if `@Transactional` is removed from `executeTransfer`?**
If the `transactionLogRepository.save()` fails after both balance updates, the balances are already committed (because `saveAndFlush` forces a DB write). Money is debited from sender and credited to receiver, but no transaction log exists. The system is in an inconsistent state with no audit trail.

**Q140. Can a `@Transactional` method catch its own exception and prevent rollback?**
By default, `@Transactional` rolls back on unchecked exceptions (`RuntimeException`). If you catch the exception inside the method and don't rethrow it, the transaction does NOT roll back. You can also use `@Transactional(noRollbackFor = SomeException.class)` to prevent rollback for specific exceptions.

**Q141. The test `testValidTransfer()` passes even though `toAccount` is `CLOSED`. Why?**
Because `transactionLogRepository.findByIdempotencyKey(key)` is mocked to return `Collections.emptyList()`, and the test skips past `validateTransfer()` going straight to `executeTransfer()`. But wait — `transfer()` calls `validateTransfer()` first, which should throw `AccountNotActiveException` for a `CLOSED` receiver. The test may actually be broken — it would likely fail when run against the real `TransferServiceImpl.validateTransfer()`. This is a test quality issue.

**Q142. What is the full URL for the register endpoint?**
`POST http://localhost:8080/api/v1/accounts/register`
— `8080` is the Spring Boot port
— `api/v1/` is the class-level `@RequestMapping`
— `accounts/register` is the `@PostMapping` value

**Q143. What would happen if CORS is configured with `allowedOrigins("*")` and `allowCredentials(true)` together?**
This combination is invalid and causes a browser error. When `allowCredentials=true`, the spec requires a specific origin (not wildcard `*`). Browsers reject responses with `Access-Control-Allow-Origin: *` alongside `Access-Control-Allow-Credentials: true`. Spring also throws an error for this combination.

**Q144. Why does `AccountRepository` redeclare `findById(long id)` when `JpaRepository` already has it?**
`JpaRepository<Account, Integer>` declares `findById(Integer id)` — the ID type is `Integer`. But the `Account.id` field is `long`. To accept a `long` parameter without boxing/unboxing issues, `findById(long id)` is explicitly declared. It overrides the inherited method signature.

**Q145. What is `withFetch()` in `provideHttpClient(withFetch())`?**
Configures Angular's `HttpClient` to use the browser's Fetch API instead of `XMLHttpRequest` (XHR) internally. Fetch is the modern browser HTTP API — required for SSR compatibility (Fetch works in Node.js too, while XHR is browser-only).

**Q146. What is `provideClientHydration(withEventReplay())`?**
Angular SSR sends pre-rendered HTML from the server. `provideClientHydration()` makes Angular attach to the existing DOM (hydration) instead of re-rendering from scratch. `withEventReplay()` replays user interactions (clicks, inputs) that happened before Angular fully initialised — prevents lost events during the hydration window.

**Q147. What would happen in production if the backend runs on a different domain (not localhost)?**
The hardcoded `http://localhost:8080` in `TransferService` would break. The Angular app wouldn't be able to reach the backend. It should use Angular's `environment.ts` with `environment.apiUrl` and configure a proper CORS `allowedOrigins` for the production domain.

**Q148. What does `response.getWriter().write("{\"message\": \"Invalid username or password\"}")` do in the security config?**
Manually writes a JSON string to the HTTP response body. This is the custom `AuthenticationEntryPoint` — instead of Spring's default HTML 401 error page, it returns a JSON error that Angular's error handlers can parse (`err.error.message`).

**Q149. If `executeTransfer` throws `DuplicateTransferException` (a `RuntimeException`), does `@Transactional` roll back?**
Yes. By default, `@Transactional` rolls back on any `RuntimeException`. So even though `saveAndFlush(senderAcc)` and `saveAndFlush(receiverAcc)` may have already executed, they are rolled back. The duplicate check is done before any balance changes, so rollback is not needed in practice, but the `@Transactional` boundary still covers it.

**Q150. What makes a good idempotency key?**
- Unique per request attempt (UUID v4 satisfies this)
- Generated client-side before the request (so retries can re-use the same key)
- Stored with a UNIQUE DB constraint as a backup safety net
- Should expire/be garbage-collected after a reasonable period in production (this project doesn't implement expiry)

---

*Sourced from full code analysis of Money_Transfer_System.*
*Backend: Spring Boot 3 · Spring Security · JPA/Hibernate · MySQL 8*
*Frontend: Angular 21 · RxJS · Chart.js · ng2-charts · TypeScript*
