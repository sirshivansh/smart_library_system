# 🚀 Setup & Run Guide — Smart Library Management System

This document outlines the step-by-step instructions to set up, database-migrate, compile, and run the Smart Library Management System (Book Haven) project.

---

## 📋 Prerequisites

Before running the project, make sure you have the following installed on your system:

1.  **Java Development Kit (JDK) 11** or higher.
2.  **Apache Maven 3.6+** (Build tool).
3.  **MySQL Server 8.x** running locally on port `3306`.
    *   *Default DB Configuration (defined in [DbConnection.java](file:///c:/ANP-D6594/WORKSPACED6594/smart_library_system/src/main/java/com/shivansh/org/util/DbConnection.java)):*
        *   **Host:** `localhost:3306`
        *   **Username:** `root`
        *   **Password:** `root`

---

## 🛠️ Step-by-Step Execution Guide

### Step 1: Database Initialization
The application is built with an **automatic schema builder**. 
If MySQL is running with the default credentials (`root` / `root`), the application will automatically create the database `smart_library_system` and seed the tables with books and members upon startup.

However, you can also import the schema manually via the terminal:
```bash
mysql -u root -p < schema.sql
```

---

### Step 2: Compile the Project
Open your terminal at the root of the project directory (`smart_library_system`) and compile the Java source files:
```bash
mvn clean compile
```

---

### Step 3: Run the Application
Start the interactive Console User Interface:
```bash
mvn exec:java
```

---

### Step 4: Run Unit Tests
To run the expanded JUnit test suite (validating SHA-256 password hashing, validation rules, borrow limits, and late return fine logic):
```bash
mvn test
```

---

## 🔑 Default Credentials

Once the dashboard is running, use these accounts to sign in:

| Role | Username / Email | Password | Details |
|---|---|---|---|
| **Admin** | `admin` | `123` | Full CRUD operations for books & members, transaction audits, overdue reports, and library metrics. |
| **Member 1 (Student)** | `shivnsh01@gmail.com` | `password123` | Borrow limit: **5 books max**. |
| **Member 2 (Faculty)** | `jane.smith@example.com` | `password123` | Borrow limit: **10 books max**. |

---

## 📂 Project Architecture

*   **View Layer:** [Dashboard.java](file:///c:/ANP-D6594/WORKSPACED6594/smart_library_system/src/main/java/com/shivansh/org/view/Dashboard.java) (Interactive console UI)
*   **Controller Layer:** Book, Member, and Transaction controllers handling command delegation.
*   **Service Layer:** Implements library limits (membership checks) and dynamic fine computations.
*   **DAO (Data Access) Layer:** Pure JDBC MySQL database operations.
*   **DTO Layer:** Java models representing data structures (`Book`, `Member`, `Transaction`).
