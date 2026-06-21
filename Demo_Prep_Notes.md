# Money Transfer System — Demo Prep Notes

Use this as your single reference for the training presentation. It has three sections:
1. Flow Diagram (points to include)
2. Architecture Diagram (points to include)
3. PPT Outline (slide-by-slide points)

---

## 1. FLOW DIAGRAM — What to Include

Make this as a simple top-to-bottom swimlane or box-and-arrow diagram. Use 3 lanes: **User → Frontend (Angular) → Backend (Spring Boot)**.

### A. Login Flow
- User enters username/password on Login page
- Frontend encodes credentials as Base64 (`btoa("username:password")`)
- Frontend sends `GET /auth` with header `Authorization: Basic <encoded>`
- Request goes through Dev Proxy (localhost:4200 → localhost:8080) to avoid CORS
- Backend (Spring Security) validates credentials against DB
- If valid → 200 OK; if invalid → 401
- Frontend stores `auth_user` and `auth_token` in `sessionStorage`
- Router redirects to `/dashboard/:id`

### B. Transfer (Send Money) Flow
- User fills transfer form: recipient account ID + amount
- Frontend generates an **idempotency key** (format: `MMddHHmm_sender_receiver`)
- `POST /api/v1/transfers` sent with this payload
- HTTP Interceptor auto-attaches Authorization header to the request
- Backend runs validation rules (show as decision diamonds):
  - Is it a self-transfer? → reject
  - Is balance sufficient? → reject if not
  - Is this a duplicate request (same idempotency key)? → reject if so
  - Is account active? → reject if not
- If all checks pass → debit sender, credit receiver, log transaction
- Response sent back with `transactionId`
- Success → Success Modal shown with Transaction ID
- Failure → Error Modal shown with backend error message

### C. Auto-Login on Refresh
- User refreshes browser
- `app.ts ngOnInit()` checks sessionStorage for existing token
- Token exists → auto-redirect to dashboard
- Token absent → stay on login page

### D. New: Change Password Flow
- User opens Profile → clicks "Change Password"
- Popup with Current/New/Confirm Password fields
- Frontend validates fields filled + new matches confirm
- `PUT /auth/change-password` sent with Basic Auth header
- Backend checks current password against DB, validates new password rules
- Success → user is logged out and redirected to `/login` (must re-login with new password)
- Failure → inline error shown in popup

### E. New: Rewards & Redeem Flow
- User opens Rewards page
- Frontend fetches transaction history, filters transactions where `fromAccountId == accId AND points > 0`
- Total points summed and displayed, with an info popup explaining how points are earned
- "Redeem" button → navigates to Redeem page (currently a placeholder UI for future redemption logic)

### F. New: Dashboard Analytics
- Dashboard fetches transaction history on load
- Computes total sent/received amount + counts
- Renders a Sent vs Received doughnut chart (Chart.js) with legend and percentage tooltips

### G. Other flows worth a small box each
- View Transaction History (`GET /api/v1/accounts/{id}/transactions`)
- View Profile (`GET /api/v1/accounts/{id}`)

**Tip for diagram tool:** draw Login Flow, Transfer Flow, and Change Password Flow as separate flowcharts (these are the three most demo-worthy). Rewards/Redeem and Dashboard Analytics can be one combined small flowchart.

---

## 2. ARCHITECTURE DIAGRAM — What to Include

Draw as a layered/block diagram, left to right or top to bottom: **Client → Frontend → Proxy → Backend → Database**.

### Layer 1: Client (Browser)
- Runs Angular SPA
- Holds `sessionStorage` (auth_user, auth_token)

### Layer 2: Frontend — Angular 21
- Components: Login, Dashboard, Transfer, History, Profile, **Rewards, Redeem**
- Services: `AuthService` (now incl. `changePassword()`), `AccountHolderService`, `TransferService`
- `HttpInterceptor` — attaches Authorization header to every outgoing request
- Models/DTOs: `TransferModel`, `TransactionStatus enum`, `TransactionLogInterface` (now includes `points`)
- New: **Chart.js** integration on Dashboard for Sent vs Received doughnut chart
- No state management library (component-level state only, no NgRx)

### Layer 3: Dev Proxy
- `proxy.conf.json` routes `/api/*` and `/auth` → `http://localhost:8080`
- Purpose: avoids CORS preflight stripping the Authorization header; browser sees same-origin requests

### Layer 4: Backend — Spring Boot
- **Controllers**: `SecurityController` (now also handles `/auth/change-password`), `AccountController`, `TransactionController`
- **Services**: `AccountService`/`Impl`, `TransferService`/`Impl`
- **Security**: `SecurityConfig` + `SpringSecurityConfig` — HTTP Basic Auth, stateless, **explicit CORS config**, **custom JSON 401 responses**
- **Custom UserDetailsService**: `CustomUserDetailsService`
- **Repositories** (Spring Data JPA): `AccountRepository`, `TransactionLogRepository`, `UserCredentialsRepository`
- **Entities**: `Account`, `TransactionLog` (now carries `points`), `UserCredentials`
- **Enums**: `AccountStatus`, `TransactionStatus`
- **Custom Exceptions**: `AccountNotFoundException`, `InsufficientBalanceException`, `SelfTransferException`, `DuplicateTransferException`, `AccountNotActiveException`, `NegativeAmountException`
- **DTOs**: `TransferRequestDto`, `TransferResponseDto`, `AccountResponseDto`, `ErrorResponseDto`, `ChangePasswordRequestDto` (new flow)

### Layer 5: Database
- Relational DB (SQL — e.g. MySQL/PostgreSQL/H2)
- Tables corresponding to: Account, TransactionLog, UserCredentials

### Cross-cutting concerns to label on the diagram
- Authentication: HTTP Basic Auth (Base64), stateless, validated on every request
- API style: RESTful, JSON request/response
- Idempotency handling at the transfer-service layer
- Global exception handling (`MtsGlobalNotFoundException`) → maps to `ErrorResponseDto`

**Tip:** A clean version = 4 stacked boxes (Browser/Angular → Proxy → Spring Boot Controllers/Services → DB), with small labels listing the components inside each box, and a side annotation box for "Security: HTTP Basic Auth over every request."

---

## 3. PPT OUTLINE — Slide-by-Slide Points

### Slide 1: Title
- Project name: Money Transfer System
- Your name, batch/team, date
- One-line tagline: "Full-stack banking prototype — Angular + Spring Boot"

### Slide 2: Problem Statement / Objective
- Need: simple system to let account holders transfer money between accounts securely
- Goals: authentication, balance validation, transaction history, duplicate-transfer prevention

### Slide 3: Tech Stack
- Frontend: Angular 21, TypeScript, RxJS, Angular HttpClient
- Backend: Spring Boot, Spring Security, Spring Data JPA
- Auth: HTTP Basic Authentication (Base64)
- DB: SQL (relational)
- Dev tooling: Angular CLI dev proxy

### Slide 4: High-Level Architecture
- Insert the architecture diagram here
- One-liner per layer (Browser → Angular → Proxy → Spring Boot → DB)

### Slide 5: Authentication Flow
- Insert the Login Flow diagram
- Call out Base64 encoding + sessionStorage + auto-login on refresh

### Slide 6: Money Transfer Flow
- Insert the Transfer Flow diagram
- Highlight validation rules (self-transfer block, balance check, duplicate/idempotency check)

### Slide 7: Key Features
- Login & session persistence
- Dashboard with account balance + **Sent vs Received doughnut chart**
- Send money with success/error modal feedback
- Transaction history (sorted newest first, credit/debit badges)
- **Rewards module** — points earned per transaction, total points display
- **Redeem page** — entry point for redeeming reward points
- Profile view + **Change Password (self-service, forces re-login after change)**

### Slide 8: API Endpoints Summary (table slide)
| Method | Endpoint | Purpose |
|---|---|---|
| GET | /auth | Validate credentials |
| PUT | /auth/change-password | Change account password |
| GET | /api/v1/accounts/{id} | Fetch account details |
| GET | /api/v1/accounts/{id}/transactions | Fetch transaction history (includes points) |
| POST | /api/v1/transfers | Execute fund transfer |

### Slide 9: Design Patterns Used
- Dependency Injection (Angular `inject()`)
- HTTP Interceptor pattern (centralized auth header)
- Observable/RxJS reactive pattern
- DTO pattern for request/response separation
- Global exception handling on backend

### Slide 10: Security Considerations
- What's secure: Basic Auth over HTTPS (prod), interceptor enforces auth on all calls, explicit CORS policy, custom JSON 401 responses
- What's not production-grade yet: sessionStorage not HttpOnly (XSS risk), no token refresh/expiry, **passwords stored/compared as plain text (needs hashing, e.g. BCrypt)**
- Mitigations: CSP headers, relative API URLs, dev proxy hides backend URL

### Slide 11: Error Handling Summary (table slide)
| Scenario | Response | User sees |
|---|---|---|
| Invalid login | 401 | "Invalid username or password" |
| Insufficient balance | 400 | Error modal with message |
| Duplicate transfer | 400 | Error modal with message |
| Invalid account ID | 404 | Redirect to dashboard |
| Wrong current password | 400 | Inline error in Change Password popup |
| New/confirm password mismatch | client-side block | Inline error in popup |

### Slide 12: Live Demo
- Just a section-break slide: "Live Demo" — list what you'll show in order:
  1. Login
  2. Dashboard (balance view + Sent/Received chart)
  3. Transfer money (success case)
  4. Transfer money (failure case — e.g. insufficient balance)
  5. Transaction history
  6. Rewards page (points total) → Redeem page
  7. Profile → Change Password (success + re-login)

### Slide 13: Production Readiness / Future Scope
- Move to HTTPS + JWT/OAuth instead of Basic Auth
- **Hash passwords (BCrypt) instead of plain-text comparison**
- Token expiry/refresh mechanism
- **Build out real Redeem logic (currently a placeholder page)**
- Remove debug logs, hide internal error details
- Add NgRx if app grows
- Add pagination/lazy loading for history

### Slide 14: Summary
- Recap architecture in one line
- Recap key strengths: simplicity, clear separation of concerns, validation-first transfer logic

### Slide 15: Thank You / Q&A

---

## Quick Notes for Building the Actual Diagrams
- Flow diagrams: use swimlanes (User / Frontend / Backend) with arrows showing the sequence; mark validation checks as decision diamonds.
- Architecture diagram: use stacked horizontal layers (Client → Proxy → Backend → DB), each box listing its components in small text.
- Keep the PPT diagram slides to ONE diagram each — don't cram flow + architecture on the same slide.
