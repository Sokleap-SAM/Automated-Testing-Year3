# Lab01 - Automated API Testing

This project contains automated tests for the `online-shop` backend using Junit Jupiter Test.

## Prerequisites

- **Java 17** or higher
- **Maven 3.6+** (for Java tests)
- **Node.js 16+** and **npm**

## Project Structure

```
lab01/
├── online-shop/                     # NestJS Backend Application
│   ├── src/                        # Source code
│   │   ├── auth/                   # Authentication module
│   │   └── users/                  # Users module
│   ├── test/                       #
│   ├── package.json                # Dependencies & scripts
│   └── tsconfig.json               # TypeScript config
├── test-lab01/                     # Java Test Suite
│   ├── src/                        # Java source files
│   │   └── User.java               # User model
│   └── test/                       # Java test files
│       ├── Authenticationtest.java # API integration tests
│       └── UserTest.java           # Unit tests
└── README.md                       # This file
```

---

## 🚀 Quick Start

### Option 1: Running Tests Against Render

#### Step 1: Navigate to backend url
https://automated-testing-year3.onrender.com

#### Step 2: Verify if it display `Hello World!` on that screen URL

#### Step 3: Run Java Tests

```powershell
# Navigate to the test-lab01 directory
cd test-lab01

# Run JUnit Jupiter Test
```

---

---

### Option 2: Running Tests in Localhost

#### Step 1: Run Backend Server

```powershell
# Navigate to the online-shop directory
cd online-shop

# Install dependencies (first time only)
npm install

# Start the development server
npm run start:dev
```

The server will start at `http://localhost:3000`

**Note:** Make sure the backend server is running at `http://localhost:3000` before running Java tests.

Open [test-lab01/test/Authenticationtest.java](test-lab01/test/Authenticationtest.java) and update the URL constant:

```java
// Change from:
private static final String LOGIN_URL = "https://automated-testing-year3.onrender.com/auth/login";

// To your Render URL:
private static final String LOGIN_URL = "http://localhost:3000/auth/login";
```

#### Step 2: Run Java Tests

```powershell
# Navigate to the test-lab01 directory
cd test-lab01

# Run JUnit Jupiter Test
```

---

## ✅ Test Coverage=

### Java Tests (test-lab01/test)

**Authenticationtest.java** - API Integration Tests:
- ✅ Login with valid credentials (200)
- ✅ Login with wrong password (401)
- ✅ Login with wrong email (401)
- ✅ Login with empty fields (401)

**UserTest.java** - Unit Tests:
- ✅ User email change test

---
