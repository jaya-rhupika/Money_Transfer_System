# Money Transfer System — Complete Presentation Guide

---

## 1. DEMO SPEECH (6 Minutes)

> **Tip:** Practise with a timer. Each section is timed. Speak slowly and clearly.

---

### [0:00 – 0:30] Opening Hook

"Good \[morning/afternoon\], everyone. Imagine you need to send money to a friend right now — instantly, securely, and with a full record of every rupee. That's exactly what our **Money Transfer System** does.

We built a full-stack, production-ready banking application that handles user registration, secure login, real-time money transfers, transaction history, and even a rewards system — all within a clean, modern web interface."

---

### [0:30 – 1:15] Project Overview

"Our system has **two major layers**:

The **backend** is a Java Spring Boot REST API connected to a MySQL database. It handles all the business logic — account management, fund transfers, validation, and security.

The **frontend** is an Angular 21 Single Page Application. It talks to the backend through HTTP calls and provides the user interface.

Together they form a full-stack, decoupled architecture — a real-world pattern used in the financial industry."

---

### [1:15 – 2:00] Registration and Login Flow

"Let me walk you through the user journey.

A new user visits the **Register** page and enters their name and a password. The backend auto-generates a unique **account ID** — this ID serves as their username for all future logins. On successful registration, a **₹200 welcome bonus** is automatically credited, and the user is immediately taken to their dashboard.

On subsequent visits, they log in using their **account number and password**. The frontend encodes these credentials in **Base64** and sends them as an **HTTP Basic Auth** header. The Spring Security layer validates them on every request."

---

### [2:00 – 3:00] Core Feature — Money Transfer

"The heart of the application is the **Transfer** page.

The logged-in user enters a recipient's account ID and the amount they wish to send. Before any money moves, our backend runs a full **validation chain**:

- Is the sender trying to transfer to themselves? → **SelfTransferException**
- Is the sender's account active? → **AccountNotActiveException**
- Is the receiver's account active? → **AccountNotActiveException**
- Is the amount negative? → **NegativeAmountException**
- Does the sender have sufficient balance? → **InsufficientBalanceException**
- Is this a duplicate request? → **DuplicateTransferException** (via idempotency key)

Every failed attempt is still **logged to the database** with a FAILURE status and a reason — so there is always an audit trail.

On success, the sender's balance is debited, the receiver's is credited, and a **Transaction Log** entry is saved with a UUID. The whole transfer runs inside a **@Transactional** block to ensure atomicity."

---

### [3:00 – 3:45] Rewards System

"We also built a **Rewards and Points** feature.

Every successful transfer **above ₹100** earns the sender reward points. The formula is simple: for every ₹100 transferred, you earn 1 point. So a ₹500 transfer earns 5 points.

Users can view their accumulated points on the **Rewards** page and see which transactions earned them points. The **Redeem** page is the designated area where those points can be exchanged for benefits."

---

### [3:45 – 4:30] Transaction History and Dashboard

"The **History** page shows a complete log of all incoming and outgoing transactions for the user, sorted by most recent first. Users can filter by Sent, Received, or All, and search by amount or account number.

The **Dashboard** is the home screen — it shows the user's name, balance, reward points, and a **Chart.js doughnut chart** that visually breaks down the number of sent vs. received transactions at a glance."

---

### [4:30 – 5:15] Security and Architecture Highlights

"A few key engineering decisions worth highlighting:

**Security:** Spring Security enforces HTTP Basic Auth on all `/api/**` endpoints. Registration at `/api/v1/accounts/register` is the only public endpoint. The frontend's **HTTP Interceptor** automatically injects the stored Base64 token into every outgoing request, so the user never has to log in repeatedly within a session.

**Idempotency:** Every transfer request from the frontend generates a **UUID v4 idempotency key**. The backend checks for duplicate keys before executing — this prevents accidental double-payments if the user clicks submit twice or the network retries.

**Audit Logging:** Even failed transactions are persisted. This gives the system a complete, tamper-evident audit trail — critical for any financial application."

---

### [5:15 – 6:00] Closing

"To summarise, we built a real-world money transfer system that demonstrates:

- Full-stack development with Spring Boot and Angular
- RESTful API design with proper HTTP status codes
- Database persistence with JPA/Hibernate on MySQL
- Stateless authentication using HTTP Basic Auth + Spring Security
- Business logic with multi-layered validation
- Idempotency to prevent duplicate transactions
- Rewards logic tied to transaction business rules
- A clean, responsive UI with route-guarded navigation

This project reflects the kind of system you'd find in the financial services industry, and we're proud of what we built. Thank you — I'm happy to take questions."

---
---

## 2. PPT SLIDE CONTENT

---

### Slide 1 — Title Slide
**Money Transfer System**
*A Full-Stack Banking Application*

Built with: **Spring Boot 3 · Angular 21 · MySQL**
Team: \[Your Names\]
Date: \[Date\]

---

### Slide 2 — Problem Statement
**What problem does this solve?**
- Users need a secure, digital way to send money between accounts
- Every transaction must be recorded for transparency
- The system must prevent fraud: duplicate transfers, self-transfers, invalid amounts
- Users need incentive to use the platform → **Rewards points**

---

### Slide 3 — System Architecture
```
┌────────────────────────┐     HTTP/REST     ┌──────────────────────────┐
│   Angular Frontend     │ ◄─────────────► │  Spring Boot Backend     │
│   (Port 4200)          │  Basic Auth JWT  │  (Port 8080)             │
│                        │                  │                          │
│  Components:           │                  │  Controllers             │
│  - Login / Register    │                  │  Services                │
│  - Dashboard           │                  │  Repositories (JPA)      │
│  - Transfer            │                  │  Security Config         │
│  - History             │                  │                          │
│  - Rewards / Redeem    │                  │      MySQL DB            │
│  - Profile             │                  │   (mts_fidelity)         │
└────────────────────────┘                  └──────────────────────────┘
```

---

### Slide 4 — Tech Stack
| Layer | Technology |
|-------|-----------|
| Frontend Framework | Angular 21 |
| UI | Custom CSS + Chart.js |
| Backend Framework | Spring Boot 3 |
| Language | Java 17+ |
| Security | Spring Security (HTTP Basic Auth) |
| Database | MySQL 8 |
| ORM | Spring Data JPA + Hibernate |
| Build Tool | Maven |
| Package Manager | npm |

---

### Slide 5 — Database Schema
**Three Core Tables:**

`accounts` — id, holder_name, balance, status (ACTIVE/INACTIVE), total_reward_points, last_updated

`transaction_logs` — id (UUID), from_account_id, to_account_id, amount, status (SUCCESS/FAILURE), failure_reason, idempotency_key, created_on, points

`user_credentials` — account_id (PK/FK), username, password

---

### Slide 6 — Registration & Login Flow
```
User → Register Page
       ↓ POST /api/v1/accounts/register (public endpoint)
       ↓ Backend creates Account + saves UserCredentials
       ↓ Account ID auto-generated → used as username
       ↓ ₹200 welcome bonus credited
       ↓ Redirect to Dashboard

User → Login Page
       ↓ Enters account_id + password
       ↓ Frontend Base64-encodes → sends as Basic Auth header
       ↓ Spring Security validates via CustomUserDetailsService
       ↓ Session stored in sessionStorage
       ↓ HTTP Interceptor auto-injects token on all future requests
```

---

### Slide 7 — Transfer Flow & Validation
**POST /api/v1/transfers**

Validation Chain (each failure is logged):
1. ❌ Self-transfer → SelfTransferException
2. ❌ Sender inactive → AccountNotActiveException
3. ❌ Receiver inactive → AccountNotActiveException
4. ❌ Negative amount → NegativeAmountException
5. ❌ Insufficient balance → InsufficientBalanceException
6. ❌ Duplicate idempotency key → DuplicateTransferException

✅ Success → Debit sender, Credit receiver, Save log, Award points

All operations in a single `@Transactional` block

---

### Slide 8 — Rewards System
**How Points Are Earned:**
- Only on **successful** transfers
- Only when transfer amount **> ₹100**
- Formula: `points = Math.floor(amount / 100)`

**Example:**
- Transfer ₹500 → 5 points
- Transfer ₹1200 → 12 points
- Transfer ₹80 → 0 points

Points are stored on both the `accounts` table (cumulative) and the `transaction_logs` table (per transaction)

---

### Slide 9 — Security Design
**Spring Security (HTTP Basic Auth)**
- All `/api/**` and `/auth` endpoints require authentication
- `/api/v1/accounts/register` is publicly accessible
- Custom `PasswordEncoder` (plain-text for demo; BCrypt recommended for prod)
- `CustomUserDetailsService` loads user from DB by username (= account ID)
- `SpringSecurityConfig` disables CSRF (stateless REST API)
- CORS configured to allow `localhost:4200`

**Angular HTTP Interceptor**
- Automatically attaches `Authorization: Basic <token>` header to every request
- Token stored in `sessionStorage`

---

### Slide 10 — Idempotency
**Problem:** What if the user double-clicks "Submit Transfer"?

**Solution: UUID Idempotency Key**
1. Frontend generates a fresh `UUID v4` on each transfer attempt
2. Key is sent in the request body
3. Backend checks `transaction_logs` for existing key
4. If found → throws `DuplicateTransferException`
5. If not found → proceeds and stores the key (marked UNIQUE in DB)

**Result:** Even if the request is retried, the transfer executes only once.

---

### Slide 11 — API Endpoints Summary
| Method | Endpoint | Auth | Purpose |
|--------|----------|------|---------|
| POST | `/api/v1/accounts/register` | ❌ Public | Register new user |
| GET | `/auth` | ✅ | Authenticate & get user info |
| PUT | `/auth/change-password` | ✅ | Change password |
| GET | `/api/v1/accounts/{id}` | ✅ | Get account details |
| GET | `/api/v1/accounts/{id}/balance` | ✅ | Get account balance |
| GET | `/api/v1/accounts/{id}/transactions` | ✅ | Get transaction history |
| POST | `/api/v1/transfers` | ✅ | Execute a transfer |

---

### Slide 12 — Frontend Architecture
**Angular Module-based (non-standalone)**
- `AppModule` — root module, declares all components, registers interceptor
- `AppRoutingModule` — route definitions with `:id` path parameters

**8 Components:**
Login · Register · Dashboard · Transfer · History · Rewards · Redeem · Profile

**3 Services:**
- `AuthService` — login, logout, token storage, session management
- `AccountholderService` — fetch user & transaction data
- `TransferService` — execute transfer API call

**1 Interceptor:**
- `HttpInterceptorService` — injects auth token on every HTTP request

---

### Slide 13 — Key Design Decisions
| Decision | Reason |
|----------|--------|
| Account ID as username | Simplifies UX — user only needs one identifier |
| HTTP Basic Auth | Simple, stateless, no JWT complexity needed for this scale |
| SessionStorage for token | Auto-clears on browser close; more secure than localStorage |
| UUID for transaction ID | Globally unique, no sequential guessing |
| @Transactional on transfer | Ensures atomicity — both debit and credit succeed or both fail |
| Log even failed transfers | Complete audit trail for compliance and debugging |
| Idempotency key | Prevents double-spend on network retries |

---

### Slide 14 — Demo
*(Live or screenshots)*
- Registration → Auto-assigned account number
- Login → Dashboard with chart
- Transfer → Success modal with Transaction ID
- Transfer → Error modal (e.g., insufficient balance)
- History → Filtered transaction log
- Rewards → Points breakdown
- Profile → Change password

---

### Slide 15 — Future Enhancements
- BCrypt password hashing
- JWT tokens instead of Basic Auth
- Email/OTP verification during registration
- Admin dashboard for account management
- Spending limits and daily transfer caps
- Push notifications for transactions
- PDF statement download

---
---

## 3. ALL POSSIBLE TECHNICAL QUESTIONS & ANSWERS

---

### 🔷 SECTION A: PROJECT OVERVIEW

**Q1. What is the purpose of this project?**
A: It's a full-stack money transfer web application that allows registered users to securely send money between accounts, view transaction history, earn reward points, and manage their profile. It demonstrates backend REST API development with Spring Boot, frontend SPA development with Angular, and MySQL persistence.

**Q2. What is the overall architecture?**
A: A decoupled client-server architecture. The Angular frontend (port 4200) communicates with the Spring Boot REST API (port 8080) over HTTP. The backend connects to a MySQL database via Spring Data JPA. All API calls require HTTP Basic Auth except the registration endpoint.

**Q3. Why did you choose Spring Boot and Angular?**
A: Spring Boot provides rapid REST API development with built-in dependency injection, JPA, and security. Angular provides a structured, component-based frontend with strong TypeScript support, routing, and the HTTP interceptor pattern for global auth handling. Together they represent a common enterprise stack.

---

### 🔷 SECTION B: SPRING BOOT & JAVA

**Q4. What annotations are used in the controllers and what do they do?**
A:
- `@RestController` — marks the class as a controller where every method returns a response body (combines `@Controller` + `@ResponseBody`)
- `@RequestMapping("api/v1/")` — base URL for all routes in the controller
- `@GetMapping`, `@PostMapping`, `@PutMapping` — map HTTP methods to handler methods
- `@CrossOrigin(origins = "http://localhost:4200")` — enables CORS for the Angular frontend
- `@PathVariable` — extracts value from the URL path (e.g., `{id}`)
- `@RequestBody` — deserialises JSON request body into a Java object
- `@Autowired` — injects Spring-managed beans

**Q5. What is the difference between @Service, @Repository, and @Controller?**
A: All three are specialisations of `@Component`, so Spring will detect and register them. The difference is semantic and allows Spring to apply specific post-processing:
- `@Controller` — marks a class as a web request handler
- `@Service` — marks business logic layer (enables transactional features)
- `@Repository` — marks data access layer; translates DB exceptions to Spring's `DataAccessException`

**Q6. What is @Transactional and why is it used on `executeTransfer`?**
A: `@Transactional` wraps a method in a database transaction. If any step inside the method throws an exception, the whole transaction rolls back. On `executeTransfer`, this ensures that if the receiver's credit succeeds but the sender's debit update fails (or vice versa), neither change is committed — protecting against partial updates that corrupt balances.

**Q7. Explain Spring Data JPA and `JpaRepository`.**
A: Spring Data JPA is an abstraction layer over JPA/Hibernate. By extending `JpaRepository<Entity, ID>`, you get CRUD methods (`save`, `findById`, `findAll`, `deleteById`) for free without writing SQL. You can also define query methods by naming conventions (e.g., `findByUsername`, `findAllByFromAccountId`) and Spring generates the SQL automatically.

**Q8. What is `saveAndFlush` vs `save`?**
A: `save` schedules the entity for persistence but may not immediately write to the DB within the current session (it batches). `saveAndFlush` forces an immediate flush to the database, ensuring the update is visible to subsequent queries in the same transaction. In `executeTransfer`, `saveAndFlush` is used to ensure balances are updated before the transaction log is written.

**Q9. Explain the entity relationships in the project.**
A:
- `Account` — standalone entity mapped to the `accounts` table
- `UserCredentials` — has a `@OneToOne` relationship with `Account` via `account_id` foreign key (insertable=false, updatable=false means the FK is managed by the `accountId` column, not the object reference)
- `TransactionLog` — has two `@ManyToOne` relationships with `Account` (fromAccount and toAccount), both with `insertable=false, updatable=false` so the `fromAccountId` / `toAccountId` columns drive the FK, not the object

**Q10. What is `@GeneratedValue(strategy = GenerationType.IDENTITY)` vs `AUTO`?**
A:
- `IDENTITY` — delegates ID generation to the database (uses `AUTO_INCREMENT` in MySQL). Best for MySQL.
- `AUTO` — lets Hibernate choose the strategy. In MySQL, this may use a `hibernate_sequence` table, which is why `TransactionLog` (which uses `AUTO` with UUID type) requires manually setting the UUID via `transactionLog.setId(UUID.randomUUID())`.

**Q11. What is `@Enumerated(EnumType.STRING)` and why use it?**
A: By default, JPA stores enums as integers (0, 1, 2…). `EnumType.STRING` stores the enum name as a string (e.g., "ACTIVE", "SUCCESS"). This makes the database readable, prevents bugs when enum values are reordered, and makes data migration safer.

**Q12. What is `@Column(insertable=false, updatable=false)`?**
A: When an entity has both a plain FK column (e.g., `fromAccountId`) and a `@ManyToOne` relationship on the same column, you need to tell JPA which one controls the column. `insertable=false, updatable=false` on the relationship means JPA ignores the relationship object when inserting/updating — only the primitive column is used for writes. The relationship is read-only for ORM purposes.

**Q13. Explain the exception handling strategy.**
A: Custom runtime exceptions (`AccountNotFoundException`, `InsufficientBalanceException`, etc.) extend `RuntimeException`. They are unchecked, so they don't need to be declared in method signatures. In `validateTransfer`, each failure case throws the appropriate exception. The `MtsGlobalNotFoundException` is a global exception class (visible in the exceptions package) — typically paired with `@ControllerAdvice` and `@ExceptionHandler` to convert exceptions into HTTP error responses.

**Q14. Why is `BigDecimal` used for balance and amount instead of `double`?**
A: `double` is a floating-point type that cannot represent many decimal values exactly (e.g., 0.1 + 0.2 ≠ 0.3 in IEEE 754). In financial applications, this causes rounding errors that compound over time. `BigDecimal` provides arbitrary-precision arithmetic and is the Java standard for monetary calculations.

**Q15. What does `@Column(precision=18, scale=2)` mean?**
A: It tells the DB that the numeric column can have up to 18 total digits with 2 decimal places. For a balance column, this means values up to `9,999,999,999,999,999.99` — sufficient for any realistic monetary amount.

---

### 🔷 SECTION C: SPRING SECURITY

**Q16. How does Spring Security work in this project?**
A: Two configuration classes are involved:
- `SpringSecurityConfig` — configures the security filter chain: disables CSRF, permits OPTIONS (CORS preflight), requires authentication for all other requests, and sets up HTTP Basic Auth with a custom 401 response
- `SecurityConfig` — configures CORS, defines which endpoints are public (`/api/v1/accounts/register`) vs authenticated, and creates a `PasswordEncoder` bean
- `CustomUserDetailsService` — loads user details from the `user_credentials` table when Spring Security needs to validate credentials

**Q17. What is HTTP Basic Authentication?**
A: Basic Auth sends credentials in the HTTP `Authorization` header as `Basic <Base64(username:password)>`. It is stateless — the credentials must be sent with every request. The server decodes and validates them on each call. It is simple but should only be used over HTTPS in production.

**Q18. Why is CSRF disabled?**
A: CSRF (Cross-Site Request Forgery) protection is needed for session-cookie-based authentication. Since this API uses stateless Basic Auth (no cookies, no session), CSRF attacks are not applicable. Disabling it prevents unnecessary token requirements on API calls.

**Q19. What is `SessionCreationPolicy.STATELESS`?**
A: It instructs Spring Security never to create an HTTP session. Each request must carry its own credentials. This is the correct policy for REST APIs — it scales horizontally and avoids session management complexity.

**Q20. What would you change about security for production?**
A: Several things: (1) Use BCrypt for password hashing — the current `PasswordEncoder` stores plain text. (2) Replace Basic Auth with JWT tokens — credentials would only be sent once at login, then a signed token is used for subsequent requests. (3) Add HTTPS. (4) Add rate limiting to prevent brute-force attacks. (5) Move passwords to a secrets manager.

**Q21. What does `UserDetailsService` do?**
A: It's a Spring Security interface with one method: `loadUserByUsername(String username)`. Spring calls this during authentication to retrieve user details from the data source. The returned `UserDetails` object (built with `User.builder()`) contains the username, password, and roles, which Spring Security uses to validate the submitted credentials.

---

### 🔷 SECTION D: ANGULAR & FRONTEND

**Q22. How does Angular handle routing in this project?**
A: `AppRoutingModule` defines all routes using `RouterModule.forRoot(routes)`. Routes include parameterised paths like `dashboard/:id`, `transfer/:id`. The component reads the parameter using `ActivatedRoute.snapshot.paramMap.get('id')`. The `**` wildcard route redirects unknown paths to login.

**Q23. What is an HTTP Interceptor in Angular and how is it used here?**
A: An interceptor implements `HttpInterceptor` and can intercept every outgoing HTTP request or incoming response. Here, `HttpInterceptorService` reads the stored Base64 auth token from `AuthService` and attaches it as an `Authorization: Basic <token>` header to every request. It's registered in `AppModule` via `HTTP_INTERCEPTORS` multi-provider — `multi: true` is essential so it doesn't replace other interceptors.

**Q24. What is `SessionStorage` and why is it used over `LocalStorage`?**
A: `sessionStorage` is a browser Web Storage API that stores data only for the duration of the browser session (cleared when the tab is closed). `localStorage` persists across sessions. For auth tokens, `sessionStorage` is safer because it auto-clears, reducing the risk of token theft if the user forgets to log out.

**Q25. What is `btoa()` and why is a fallback provided?**
A: `btoa()` is a browser built-in function that Base64-encodes a string. In Angular with Server-Side Rendering (SSR enabled via `@angular/ssr`), the code may run in Node.js where `btoa` doesn't exist. The fallback `Buffer.from(credentials).toString('base64')` is the Node.js equivalent. The `PLATFORM_ID` injection and `isPlatformBrowser()` check allow the service to detect the runtime environment.

**Q26. Why use `ChangeDetectorRef.detectChanges()` in several components?**
A: Angular's default change detection runs after every browser event or async operation. However, in some cases — especially with SSR, `OnPush` strategy, or when data changes inside a subscription — the view may not auto-update. Calling `cd.detectChanges()` manually forces Angular to re-check and re-render the component immediately.

**Q27. What is `uuid v4` used for in the transfer?**
A: `uuidv4()` generates a universally unique random ID. In `transfer.ts`, it creates a fresh `idempotencyKey` for each transfer submission. This key is sent to the backend, which checks if it has been used before — preventing the same transfer from executing twice if the user double-clicks or the network retries.

**Q28. How does `ng2-charts` / `Chart.js` work in the dashboard?**
A: A `<canvas>` element is referenced with `@ViewChild`. After the component and data load (`ngAfterViewInit` + a short `setTimeout`), `new Chart(ctx, config)` is called with a doughnut chart configuration. The `data` property contains `sentCount` and `receivedCount` values. `chart.destroy()` is called before re-creating to prevent memory leaks.

**Q29. Explain the `@Inject(PLATFORM_ID)` pattern.**
A: In Angular SSR, a single codebase runs both in the browser and on the server. `PLATFORM_ID` is an injection token that Angular provides, whose value is `'browser'` or `'server'`. `isPlatformBrowser(platformId)` returns `true` only in the browser. This guards browser-only APIs like `sessionStorage` from being called during server-side rendering.

**Q30. What is `provideClientHydration(withEventReplay())`?**
A: This is Angular's hydration feature. When using SSR, the server sends pre-rendered HTML to the client. Hydration makes Angular "attach" to the existing DOM instead of re-rendering from scratch. `withEventReplay()` replays any user events that occurred before Angular fully initialised, preventing missed interactions.

---

### 🔷 SECTION E: DATABASE & JPA

**Q31. What is JPA vs Hibernate vs Spring Data JPA?**
A:
- **JPA** (Jakarta Persistence API) — a Java specification for ORM. Defines annotations (`@Entity`, `@Table`, `@Column`, etc.) and `EntityManager`
- **Hibernate** — the most popular JPA implementation. Does the actual SQL generation and execution
- **Spring Data JPA** — a Spring abstraction on top of JPA. Provides `JpaRepository`, query derivation, and simplifies configuration

**Q32. What does `spring.jpa.hibernate.ddl-auto = update` do?**
A: Hibernate will automatically create or modify database tables to match the entity classes on startup. Specifically, `update` adds new columns/tables but never drops existing ones. This is convenient for development but dangerous in production (use `validate` or `none` in prod with proper migration tools like Flyway).

**Q33. What is `MySQL8Dialect` in application.properties?**
A: It tells Hibernate which SQL dialect to use when generating queries. MySQL 8 has specific SQL features (e.g., window functions, `JSON` type). Using the correct dialect ensures generated SQL is compatible with the MySQL version.

**Q34. What is an `idempotency_key` with `UNIQUE` constraint?**
A: The column is marked `UNIQUE` in the schema (`columnDefinition = "VARCHAR(100) UNIQUE"`). This means the database enforces that no two rows can have the same key. Even if the application-level check in `executeTransfer` fails (race condition), the database constraint acts as a final safety net, throwing a constraint violation exception.

**Q35. Why is UUID used as the transaction log ID?**
A: Auto-incremented integers are predictable and expose internal IDs to users ("your 1001st transaction"). UUID v4 is random and unpredictable, making it harder for attackers to enumerate transaction IDs. It also works for distributed systems where multiple servers need to generate unique IDs without coordination.

---

### 🔷 SECTION F: REST API DESIGN

**Q36. What HTTP status codes are returned and why?**
A:
- `200 OK` — successful GET or POST (transfer success)
- `201 CREATED` — registration created a new resource
- `400 BAD REQUEST` — invalid input (missing fields, password mismatch)
- `401 UNAUTHORIZED` — invalid credentials
- `500 INTERNAL SERVER ERROR` — unhandled server error

**Q37. What are DTOs and why use them?**
A: Data Transfer Objects are plain Java classes used to shape the data sent to/from the API. Instead of exposing the `Account` entity directly (which might include sensitive fields or internal FK references), a `TransferRequestDto` carries only `fromAccountId`, `toAccountId`, `amount`, and `idempotencyKey`. This decouples the API contract from the database model.

**Q38. What is `@CrossOrigin` and why is it needed?**
A: Browsers enforce the **Same-Origin Policy** — JavaScript can only make HTTP requests to the same domain/port/protocol as the page. Since the Angular app runs on port 4200 and the backend on port 8080, the origins differ. `@CrossOrigin` adds CORS headers (`Access-Control-Allow-Origin: http://localhost:4200`) that tell the browser it's safe to make cross-origin requests. Preflight OPTIONS requests are also handled.

**Q39. Why is the register endpoint kept public?**
A: New users can't have credentials before registering. If the endpoint were secured, new users would have no way to create an account. After registration, all subsequent requests (login verification, transfer, history) require valid credentials.

---

### 🔷 SECTION G: BUSINESS LOGIC

**Q40. Walk through what happens when an InsufficientBalanceException is thrown.**
A:
1. `validateTransfer` detects `amount > balance`
2. Creates a `TransactionLog` with `TransactionStatus.FAILURE` and reason "Balance is not sufficient"
3. Saves the log (with a new random UUID)
4. Throws `InsufficientBalanceException`
5. The exception propagates to the controller
6. Spring's exception handler (or `@ControllerAdvice`) converts it to an HTTP 400/500 response
7. The Angular frontend receives the error, parses `err.error.errorMessage`, and shows the error modal

**Q41. How is the rewards points calculation implemented?**
A: In `executeTransfer`:
```java
int points = 0;
if (amountToBeDebited.compareTo(new BigDecimal("100")) > 0) {
    points = amountToBeDebited.divideToIntegralValue(new BigDecimal("100")).intValue();
}
```
`divideToIntegralValue` performs integer division (no rounding). For ₹350, this gives 3 points. The points are stored on the `TransactionLog` and also added to `account.totalRewardPoints`.

**Q42. What happens if both `saveAndFlush(senderAcc)` and `saveAndFlush(recieverAcc)` succeed but the `transactionLogRepository.save(transactionLog)` fails?**
A: Because the method is annotated `@Transactional`, all three operations are part of the same database transaction. If the log save throws an exception, Spring rolls back the entire transaction — the balance updates are undone. This is the core value of `@Transactional` for financial operations.

**Q43. How does the system prevent self-transfer?**
A: In `validateTransfer`: `if (senderAcc.getId() == recieverAcc.getId())` — it compares the two account IDs. If equal, it logs a FAILURE entry and throws `SelfTransferException`. The frontend also validates this before submitting (though backend validation is the authoritative check).

---

### 🔷 SECTION H: TESTING

**Q44. What test files exist in the project?**
A: The backend has JUnit tests:
- `AccountTest.java` — tests `Account` entity methods (debit, credit)
- `TransferRequestValidationTest.java` — tests the validation logic in `TransferService`
- `MoneyTransferSystemApplicationTests.java` — Spring context load test

The frontend has `.spec.ts` files generated by Angular CLI for each service and component.

**Q45. What types of tests would you add to improve coverage?**
A:
- **Unit tests** — mock `AccountRepository` and test `TransferServiceImpl.validateTransfer` with all edge cases
- **Integration tests** — use `@SpringBootTest` with an in-memory H2 database to test the full API flow
- **API tests** — use MockMvc to test controllers with `@WebMvcTest`
- **Frontend** — use Jasmine/Karma for component tests, testing component logic and HTTP calls with `HttpClientTestingModule`

---

### 🔷 SECTION I: TRICKY & DEEP QUESTIONS

**Q46. What would happen if two users simultaneously transfer from the same account?**
A: Without a locking mechanism, a race condition could allow both transfers to proceed with the original balance, causing an overdraft. Solutions:
- **Optimistic locking** — add a `@Version` field to `Account`. If two transactions read the same version and both try to update, one will fail with `OptimisticLockException`
- **Pessimistic locking** — use `@Lock(LockModeType.PESSIMISTIC_WRITE)` in the repository to lock the row during the transaction

**Q47. The `Account` entity has a `version` field but it's not using `@Version`. Why?**
A: The `version` column exists in the schema but the `@Version` annotation from JPA was not applied. This means optimistic locking is not actually active — it's a schema artefact that was set up but not completed. Adding `@Version` above the `version` field would enable proper optimistic locking.

**Q48. Why is there a `UserCredentials` table separate from `Account`?**
A: Separation of concerns. Account data (balance, status, reward points) is financial data. Credentials (username, password) are security data. Keeping them in separate tables means: (1) you can query account data without touching credentials, (2) if you switch authentication systems (e.g., OAuth), the account table doesn't change, (3) it follows the principle of least privilege — services that need account info don't need to touch passwords.

**Q49. The `SecurityConfig` and `SpringSecurityConfig` both define `SecurityFilterChain` beans. Doesn't this cause a conflict?**
A: Yes, having two `SecurityFilterChain` beans with the same method names would cause a Spring conflict. In practice, Spring Boot would use both if they have different `@Order` values, applying them in order. If both are present without ordering, the last one loaded wins. This is a potential bug in the codebase — ideally, security configuration should be consolidated into a single class.

**Q50. What is the purpose of the `MtsGlobalNotFoundException` class?**
A: It's a base exception class (or global exception handler marker). Typically used with `@ControllerAdvice` and `@ExceptionHandler(Exception.class)` to catch all unhandled exceptions and return structured error responses (like `ErrorResponseDto`) instead of Spring's default HTML error page. The `ErrorResponseDto` class in the project confirms this pattern was intended.

---

### 🔷 SECTION J: QUICK-FIRE CONCEPTS

**Q51. What is `@Bean`?** → A method-level annotation that tells Spring to manage the return value of the method as a Spring bean (component). Used in `@Configuration` classes.

**Q52. What is dependency injection?** → A design pattern where objects receive their dependencies from an external source (Spring IoC container) instead of creating them (`new`). Promotes loose coupling.

**Q53. What is `Observable` in Angular?** → A lazy, async data stream from RxJS. HTTP calls return `Observable<T>`. You must `.subscribe()` to trigger the request and handle the response.

**Q54. What is `pipe(map(...))` in `AuthService`?** → `pipe` chains RxJS operators. `map` transforms the emitted value. Here it stores credentials in sessionStorage as a side effect while passing the server response downstream.

**Q55. What is the difference between `standalone: false` and `standalone: true` in Angular components?** → Standalone components (Angular 14+) don't need to be declared in an `NgModule`. Traditional components (`standalone: false`) must be declared in a module. This project uses module-based architecture (`AppModule`).

**Q56. What is `@ViewChild`?** → A decorator that gets a reference to a child element in the template (by selector or template variable). Used here to get the `<canvas>` element for Chart.js initialisation.

**Q57. Why `inject()` function instead of constructor injection?** → Both work. The `inject()` function is a newer Angular pattern that allows dependency injection outside constructor parameters. It makes it easier to use in derived classes and reduces constructor boilerplate.

**Q58. What is `BigDecimal.compareTo()` vs `==` or `equals()`?** → `compareTo` compares numeric value only (5.0 == 5.00). `equals` would return false for 5.0 vs 5.00 because it includes scale. For monetary comparisons, always use `compareTo`.

---
---

## 4. REASONING FOR EVERY MAJOR DECISION

---

### Backend Decisions

| Decision | Reason |
|----------|--------|
| **Spring Boot** | Auto-configuration, embedded Tomcat, starter POMs. Fastest way to build production-ready REST APIs in Java |
| **MySQL** | ACID-compliant relational database. Perfect for financial data requiring strong consistency and referential integrity |
| **Spring Data JPA** | Eliminates boilerplate DAO code. Auto-generates SQL from method names. Provides transaction management |
| **`@Transactional` on executeTransfer** | Money transfers must be atomic. Either both balance changes succeed, or neither does. Without this, a crash mid-transfer would leave balances in an inconsistent state |
| **Idempotency key (UUID)** | Financial systems must prevent duplicate transactions. UUID ensures each transfer attempt has a globally unique ID, detectable on retry |
| **Separate `UserCredentials` table** | Separation of concerns between financial data and authentication data. Makes it easier to change auth mechanism without touching account logic |
| **Account ID as username** | Simplifies the login experience — the user only needs to remember their account number (the system's ID), not a separate username |
| **Custom exception classes** | Provides meaningful, specific error types instead of generic exceptions. Enables targeted `@ExceptionHandler` mappings in `@ControllerAdvice` |
| **Log every failed transfer** | Regulatory compliance and debugging. In banking, you must be able to explain why any transaction failed |
| **`BigDecimal` for amounts** | IEEE 754 floating-point arithmetic introduces rounding errors (0.1 + 0.2 ≠ 0.3). `BigDecimal` is the only safe type for financial calculations |
| **HTTP Basic Auth** | Simplest stateless auth for a REST API. No session state needed on the server. Suitable for the scale of this demo project |
| **`EnumType.STRING` for status** | Readable database values. Resilient to enum reordering. Critical for financial audit logs |
| **Welcome bonus of ₹200** | Incentivises registration and gives users something to transfer immediately, allowing demo functionality without additional setup |
| **Reward points system** | Encourages higher-value transfers. Common in banking apps (credit card reward points, UPI cashback) |

---

### Frontend Decisions

| Decision | Reason |
|----------|--------|
| **Angular (module-based, v21)** | Mature, structured framework with built-in DI, routing, and HTTP client. Strong TypeScript support |
| **HTTP Interceptor** | Avoids duplicating auth header code in every service method. Single point of control for auth |
| **SessionStorage for token** | Auto-clears on tab close. More secure than localStorage for auth tokens |
| **Base64 in client** | HTTP Basic Auth specification requires credentials encoded as Base64. `btoa()` is the browser-native function for this |
| **SSR check with PLATFORM_ID** | The project uses `@angular/ssr`. `sessionStorage` doesn't exist in Node.js. Guards prevent errors during server-side rendering |
| **UUID on frontend (not backend)** | The frontend generates the idempotency key so that if the network fails before the server responds, the same key can be re-sent on retry, allowing the server to detect the duplicate |
| **Chart.js + ng2-charts** | Popular, lightweight charting library. ng2-charts provides Angular-friendly wrapper. Doughnut chart gives at-a-glance spending insight |
| **ChangeDetectorRef.detectChanges()** | Needed in async contexts with SSR to ensure the view updates after data arrives |
| **Separate service files** | Single Responsibility Principle. AuthService handles auth only. AccountholderService handles account API calls. TransferService handles transfers |
| **Component-per-route design** | Each page is its own component with its own template and logic. Clean separation, easier to test and maintain |
| **Error modal + success modal** | Better UX than alert() or inline text. Shows a structured, dismissable popup with the transaction ID or error message |
| **`isSubmitting` flag** | Prevents double-submit while a request is in-flight. Disables the button after first click |
| **`RouterModule.forRoot` with `**` wildcard** | Ensures unknown routes redirect to login instead of showing a blank page |

---

*Document generated from full source code analysis of Money_Transfer_System project.*
*Stack: Spring Boot 3 · Angular 21 · MySQL 8 · Spring Security · JPA/Hibernate · Chart.js*
