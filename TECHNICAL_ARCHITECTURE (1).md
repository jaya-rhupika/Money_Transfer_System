# Money Transfer System - Technical Architecture Explanation

> **Updated:** Reflects new features added to the project — Dashboard analytics chart, Rewards & Redeem module, and Change Password flow — along with explicit CORS config and custom 401 handling on the backend.

## System Overview

This is a **full-stack money transfer application** with Angular frontend and Spring Boot backend, using Basic HTTP Authentication for secure fund transfers.

---

## **Frontend Architecture (Angular 21)**

### **Technology Stack**
- **Framework**: Angular 21 (Standalone & Module-based components)
- **HTTP Client**: Angular HttpClient with interceptors
- **Authentication**: Basic Auth (Base64 encoded credentials)
- **State Management**: Component-level (no NgRx)
- **Styling**: CSS with responsive design
- **Storage**: SessionStorage for auth tokens

### **Component Structure**

```
src/app/
├── service/
│   ├── auth.ts                    # Authentication service
│   ├── httpinterceptor.ts         # HTTP interceptor for auth headers
│   ├── accountholderservice.ts    # API calls for accounts/transfers
│   └── transfer-service.ts        # Transfer-specific API calls
├── component/
│   ├── login/                     # Login page (redirects to dashboard)
│   ├── dashboard/                 # Main dashboard with balance & action grid
│   ├── transfer/                  # Money transfer form with success/error modals
│   ├── history/                   # Transaction history with floating nav
│   ├── profile/                   # Account details display
│   └── account-holder-interface.ts # Type definitions
├── models/
│   ├── transferModel.ts           # Transfer request/response DTOs
│   └── transaction-status.enum.ts # Enum for transaction status
└── app.ts                         # Root component with auto-login logic
```

### **Authentication Flow**

**Login Process:**
1. User enters username/password on login page
2. Credentials encoded as Base64: `btoa("username:password")`
3. Sent to `/auth` endpoint with header: `Authorization: Basic <encoded>`
4. Backend validates and returns 200 OK (or 401 if invalid)
5. `AuthService` stores credentials in `sessionStorage`:
   ```javascript
   sessionStorage.setItem('auth_user', username);
   sessionStorage.setItem('auth_token', base64EncodedCredentials);
   ```
6. User redirected to `/dashboard/:id`

**Auto-Login on Refresh:**
- `app.ts` `ngOnInit()` checks if token exists in sessionStorage
- If exists and user is on root/login page → auto-redirects to dashboard
- Session survives page refresh (not survived after closing browser tab)

### **HTTP Interceptor Pattern**

**Purpose**: Automatically attach Authorization header to all API requests

```typescript
intercept(req: HttpRequest<any>, next: HttpHandler):
  const token = this.authService.getAuthToken();
  if (token) {
    const request = req.clone({ 
      setHeaders: { Authorization: `Basic ${token}` } 
    });
    return next.handle(request);
  }
  return next.handle(req);
```

**Why `setHeaders` instead of replacing?** — Preserves existing headers instead of overwriting them (Content-Type, CORS, etc.)

### **Dev Proxy Configuration**

**File:** `proxy.conf.json`
```json
{
  "/api": { "target": "http://localhost:8080" },
  "/auth": { "target": "http://localhost:8080" }
}
```

**Why needed?**
- Avoids CORS preflight requests that can strip Authorization header
- All API calls made to `http://localhost:4200/api/...` proxied to backend
- Browser sees same origin (localhost:4200) → no CORS issues
- Interceptor runs *before* proxy → Authorization header attached before backend sees request

### **Change Detection**

Angular SSR + async data loading required manual change detection:

```typescript
loadUser() {
  this.service.getUserById(this.accId).subscribe({
    next: (data) => {
      this.user = data;
      this.cd.detectChanges();  // Force template re-render
    }
  });
}
```

**Why needed?** — Template has `*ngIf="user"` which initially false. After subscribe completes, `user` is set, but Angular doesn't re-check the template without explicit detection.

### **Key Pages**

| Page | Purpose | Key Features |
|------|---------|--------------|
| **Login** | Authentication | Form validation, error alerts |
| **Dashboard** | Home page | Account balance, action buttons, 4x2 grid, **Sent vs Received doughnut chart (Chart.js)** |
| **Transfer** | Send money | Form with recipient ID/amount, success/error modal |
| **History** | View transactions | Sorted by date (newest first), credit/debit badges |
| **Rewards** | Reward points | Lists reward-earning transactions, shows total points, info popup, links to Redeem |
| **Redeem** | Redeem reward points | Placeholder redemption page reached from Rewards |
| **Profile** | Account info | Account details with status, **Change Password popup/flow** |

### **New: Dashboard Analytics (Chart.js)**
- `Dashboard` component now pulls transaction history and computes stats: `totalSent`, `totalReceived`, `sentCount`, `receivedCount`
- Renders a **doughnut chart** (Sent = red, Received = green) via Chart.js, with legend + percentage tooltips
- Chart initializes after view + data are both ready (`ngAfterViewInit` + `setTimeout` fallback)

### **New: Rewards & Redeem Module**
- `Rewards` component filters the account's transactions for those with `points > 0` and sums them into `totalPoints`
- Info popup explains how points are earned
- "Redeem" button navigates to a new `Redeem` page (`/redeem/:id`) — currently a placeholder screen for future redemption logic
- `TransactionLogInterface` now includes a `points` field

### **New: Change Password Flow**
- Profile page has a "Change Password" popup with Current/New/Confirm Password fields
- Client-side checks: all fields required, new password must match confirmation
- Calls `AuthService.changePassword()` → `PUT /auth/change-password`
- On success: user is logged out and redirected to `/login` (forces re-auth with new password)
- On failure: error message from backend shown inline in the popup

---

## **Backend Architecture (Spring Boot)**

### **Technology Stack**
- **Framework**: Spring Boot
- **Security**: Spring Security with HTTP Basic Auth
- **Database**: SQL (likely PostgreSQL/MySQL)
- **API**: RESTful with JSON
- **Authentication**: Stateless HTTP Basic

### **API Endpoints**

| Method | Endpoint | Auth | Purpose |
|--------|----------|------|---------|
| GET | `/auth` | Basic | Validate credentials, returns user credentials record |
| PUT | `/auth/change-password` | Basic | **New:** change account password (validates current password + confirmation match) |
| GET | `/api/v1/accounts/{id}` | Basic | Fetch account details |
| GET | `/api/v1/accounts/{id}/transactions` | Basic | Fetch transaction history (now includes `points` per transaction) |
| POST | `/api/v1/transfers` | Basic | Execute fund transfer |

### **Authentication & Authorization**

- **Mechanism**: HTTP Basic Auth (no JWT, no sessions)
- **Header Format**: `Authorization: Basic base64(username:password)`
- **Stateless**: Each request must include credentials (`SessionCreationPolicy.STATELESS` explicitly set)
- **Validation**: Backend extracts & validates username/password against database
- **New: Explicit CORS Configuration** (`SecurityConfig.corsConfigurationSource()`) — allowed origins `http://localhost:4200` and `http://localhost:8080`, allowed methods GET/POST/PUT/DELETE/OPTIONS, credentials allowed, max age 3600s
- **New: Custom 401 JSON response** — instead of default Spring Security HTML/blank 401, both `SecurityConfig` and `SpringSecurityConfig` now return `{"message": "Invalid username or password"}` via a custom `authenticationEntryPoint`
- **New: Path-based authorization rules** — static assets (`/`, `/index.html`, `/*.css`, `/*.js`, `/assets/**`) are public; `/auth` and `/api/**` require authentication; OPTIONS requests are permitted for CORS preflight
- **New: Password change capability** — `PasswordEncoder` bean currently uses plain-text matching (noted as a security gap — see Security Considerations)

### **Transfer Validation Rules**

Based on backend code:
- ✅ Transfer to different account allowed
- ❌ Self-transfer blocked (same from/to account)
- ✅ Insufficient balance checks
- ✅ Idempotency key: `MMddHHmm_sender_receiver` prevents duplicate processing
- ✅ Transaction logging for all transfers

---

## **Data Flow Diagrams**

### **Login Flow**
```
[Login Page]
    ↓ (username/password)
[AuthService.authenticate()]
    ↓ encode credentials
[HTTP GET /auth] (via proxy)
    ↓ interceptor attaches header
[Dev Proxy] (localhost:4200)
    ↓ forwards to backend
[Backend validates] (/auth endpoint)
    ↓ if valid → 200 OK
[AuthService stores token in sessionStorage]
    ↓
[Router navigates to /dashboard/:id]
    ↓
[Dashboard loads]
```

### **Transfer Flow**
```
[User fills form on Transfer page]
    ↓ (recipientId, amount)
[submitTransfer() called]
    ↓ generates idempotencyKey
[TransferService.executeTransfer()] 
    ↓ POST to /api/v1/transfers
[HTTP Interceptor] 
    ↓ attaches Authorization header
[Dev Proxy]
    ↓ forwards to backend
[Backend validates & executes]
    ↓ 
[If success → Response with transactionId]
    ↓
[Success Modal shows with Transaction ID]
    ↓ user clicks OK
[Navigate back to dashboard]
    ↓
[If error → Error Modal with message]
```

### **Auto-Login on Refresh**
```
[User refreshes page]
    ↓ app.ts ngOnInit()
[Check sessionStorage for token]
    ↓
[Token exists?]
    ├─ YES → auto-redirect to /dashboard/:id
    └─ NO → stay on login page
```

### **New: Change Password Flow**
```
[User opens Profile page → clicks "Change Password"]
    ↓
[Popup opens: Current / New / Confirm Password fields]
    ↓ client-side validation (all fields filled, new == confirm)
[AuthService.changePassword()]
    ↓ PUT /auth/change-password
[Backend SecurityController.changePassword()]
    ↓ validates current password matches DB record
    ↓ validates new password not blank, matches confirmation
    ↓ saves new password to UserCredentials
[Response: success message OR 400 with error]
    ↓ on success
[Frontend logs user out → redirects to /login]
    ↓ on failure
[Error shown inline in popup, popup stays open]
```

### **New: Rewards & Redeem Flow**
```
[User navigates to Rewards page]
    ↓
[Rewards.ngOnInit() fetches GET /api/v1/accounts/{id}/transactions]
    ↓
[Filter transactions: fromAccountId == accId AND points > 0]
    ↓
[Sum points → totalPoints displayed]
    ↓ user clicks "Redeem"
[Navigate to /redeem/:id]
    ↓
[Redeem page — placeholder UI for future redemption logic]
```

---

## **Security Considerations**

### **What's Secure** ✅
- Credentials sent via HTTPS (in production)
- Basic Auth encodes credentials in Authorization header
- SessionStorage not accessible via JavaScript (HttpOnly equivalent for session data)
- Interceptor ensures auth header on every request

### **What's NOT Secure** ❌
- **SessionStorage not HttpOnly**: Vulnerable to XSS attacks if malicious JS injected
- **Basic Auth stateless**: Credentials sent with every request (why HTTPS is required)
- **No token refresh**: Session expires when browser closed (acceptable for banking in practice)
- **New: Plain-text password storage/matching**: `SecurityConfig.passwordEncoder()` currently compares raw strings instead of hashing (e.g. BCrypt) — must be fixed before production, especially now that a self-service Change Password feature exists

### **Mitigations in Place**
- ✅ CSP headers restrict script execution
- ✅ Relative API URLs (no hardcoded localhost)
- ✅ Dev proxy hides backend URL from frontend

---

## **Key Design Patterns**

### **1. Dependency Injection (Angular)**
```typescript
private authService = inject(AuthService);
private router = inject(Router);
```
Loose coupling, testability

### **2. Observable-based Async (RxJS)**
```typescript
this.service.getUserById(id).subscribe({
  next: (data) => { },
  error: (err) => { }
});
```
Reactive programming, automatic unsubscribe in OnDestroy

### **3. HTTP Interceptor Pattern**
Centralized auth header management without repeating in every service call

### **4. Modal for User Feedback**
Success/error shown via modal instead of alerts (better UX)

### **5. Floating Navigation**
Fixed bottom nav persists across page navigation (mobile-friendly)

---

## **Error Handling**

| Error | Frontend Handling | User Sees |
|-------|------------------|-----------|
| **Invalid credentials** | 401 from `/auth` | Alert "Invalid username or password" |
| **Insufficient balance** | 400 from transfer API | Modal: "Error message from backend" |
| **Network timeout** | HttpErrorResponse | Modal: "Error message from backend" |
| **Invalid account ID** | 404 from /accounts API | Redirects to dashboard (with error logged) |

---

## **Performance Optimizations**

1. **Dev Proxy** — Reduces HTTP OPTIONS preflight requests
2. **SessionStorage** — No network roundtrip for auth on page load
3. **ChangeDetectionStrategy.OnPush** — Not implemented (would benefit large forms)
4. **Lazy loading** — Not implemented (app is small, not needed)
5. **Floating nav** — Fixed positioning avoids re-renders on scroll

---

## **Production Considerations**

To deploy this in production:

1. **HTTPS Required** — Basic Auth sends base64 (easily decoded)
2. **Backend CORS** — Remove `changeOrigin: true` from proxy (only for dev)
3. **Remove Debug Logs** — Console.error() statements for production build
4. **Error Messages** — Don't expose internal backend errors to users
5. **Session Timeout** — Implement token expiration (currently survives refresh)
6. **Security Headers** — CSP, X-Frame-Options, X-Content-Type-Options already set
7. **Type Safety** — Use stricter TypeScript configs

---

## **Summary Table**

| Layer | Technology | Purpose |
|-------|-----------|---------|
| **Frontend** | Angular 21 + TypeScript | UI, forms, navigation |
| **State** | SessionStorage | Auth token persistence |
| **HTTP** | Angular HttpClient | API communication |
| **Auth** | HTTP Basic (Base64) | Credential validation per request |
| **Proxy** | Dev proxy (dev-server) | CORS bypass, header preservation |
| **Backend** | Spring Boot | Business logic, security, data persistence |
| **DB** | SQL | Account & transaction storage |

This architecture prioritizes **simplicity** over complexity—perfect for a banking prototype or learning project! 🏦
