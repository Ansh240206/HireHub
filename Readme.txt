# HireHub

A full-stack Job Portal application built to demonstrate modern backend engineering practices using **Java, Spring Boot, PostgreSQL, and React**.

HireHub enables applicants to discover and apply for jobs, recruiters to manage companies and job postings, and administrators to moderate the platform through a secure, role-based system.

---

# Features

## Authentication

- User Registration
- User Login
- JWT Authentication
- Refresh Token Rotation
- BCrypt Password Hashing
- Role-Based Authorization
- Change Password
- Logout

---

## Applicant

- Browse Jobs
- Search Jobs
- Filter Jobs
- Sort Jobs
- Save Jobs
- Apply for Jobs
- Track Application Status
- Manage Profile
- Upload Resume (PDF)
- Manage Skills
- Manage Education
- Manage Experience

---

## Recruiter

- Create Company
- Manage Company
- Post Jobs
- Edit Jobs
- Delete Jobs
- View Applicants
- Update Application Status

---

## Admin

- View Users
- Disable Users
- Delete Users
- Manage Companies
- Manage Jobs
- Platform Dashboard

---

# Tech Stack

## Backend

- Java 21
- Spring Boot
- Spring Security
- JWT Authentication
- Spring Data JPA
- Hibernate
- PostgreSQL
- Bean Validation
- Swagger/OpenAPI
- Maven
- Docker

## Frontend

- React
- React Router
- Axios
- Context API
- Tailwind CSS

---

# Architecture

The application follows a layered monolithic architecture.

```
Client

↓

Controller

↓

Service

↓

Repository

↓

PostgreSQL
```

Responsibilities:

- Controllers handle HTTP requests and responses.
- Services contain business logic.
- Repositories communicate with the database.
- DTOs are used across API boundaries.
- Entities are never exposed directly.

---

# Project Structure

```
HireHub/

├── backend/
│   ├── src/
│   ├── Dockerfile
│   ├── pom.xml
│   └── README.md
│
├── frontend/
│   ├── src/
│   ├── package.json
│   ├── app.js
│   ├── styles.css
│   ├── index.html
│   └── README.md
│
├── docs/
│
├── docker-compose.yml
│
├── README.md
│
└── .gitignore
```

---

# User Roles

## Applicant

- Browse jobs
- Apply for jobs
- Upload resume
- Save jobs
- Track applications
- Manage profile

## Recruiter

- Manage company
- Create jobs
- Edit jobs
- Delete jobs
- Review applicants
- Update application status

## Admin

- Manage users
- Manage companies
- Moderate jobs
- View dashboard

---

# Business Rules

- Email must be unique.
- Users have exactly one role.
- Passwords are encrypted using BCrypt.
- Protected APIs require JWT authentication.
- Applicants can apply only once to a job.
- Applicants must complete their profile before applying.
- Recruiters can manage only their own jobs.
- Recruiters belong to exactly one company.
- Resume uploads must be PDF files.
- Resume size must not exceed 5 MB.
- Deleted or closed jobs cannot receive applications.

---

# REST APIs

## Authentication

```
POST   /api/auth/register
POST   /api/auth/login
POST   /api/auth/refresh
POST   /api/auth/logout
POST   /api/auth/change-password
```

## Jobs

```
GET    /api/jobs
POST   /api/jobs
PUT    /api/jobs/{id}
DELETE /api/jobs/{id}
```

## Applications

```
POST   /api/applications/jobs/{jobId}
GET    /api/applications/me
GET    /api/applications/recruiter
PATCH  /api/applications/{id}/status
```

## Applicant Profile

```
GET    /api/applicant-profile/me
PUT    /api/applicant-profile/me
POST   /api/applicant-profile/me/resume
```

## Saved Jobs

```
GET    /api/saved-jobs
POST   /api/saved-jobs/{jobId}
DELETE /api/saved-jobs/{jobId}
```

## Admin

```
GET    /api/users
PATCH  /api/users/{userId}/disable
DELETE /api/users/{userId}
GET    /api/admin/dashboard
```

---

# Database Overview

Main entities:

- User
- Role
- ApplicantProfile
- RecruiterProfile
- Company
- Job
- Application
- Resume
- Skill
- Education
- Experience
- SavedJob

---

# Security

- Spring Security
- JWT Authentication
- Stateless Authentication
- BCrypt Password Hashing
- Role-Based Authorization
- Bean Validation
- Global Exception Handling

---

# Pagination & Searching

Supported features:

- Pagination
- Sorting
- Keyword Search
- Company Filter
- Location Filter
- Employment Type Filter
- Salary Filter
- Experience Level Filter

---

# Resume Upload

- PDF only
- Maximum size: 5 MB
- File metadata stored in PostgreSQL
- Files stored separately from the database

---

# Running the Project

## Backend

Configure PostgreSQL credentials using `application.yml` or environment variables.

```bash
mvn spring-boot:run
```

Swagger UI:

```
http://localhost:8080/swagger-ui.html
```

---

## Frontend

```bash
npm install
npm run dev
```

---

## Docker

```bash
docker compose up --build
```

---

# Documentation

The `docs/` directory contains:

- Project Specification
- Functional Requirements
- Non-Functional Requirements
- API Design
- Database Design
- Architecture
- ER Diagram
- Wireframes

---

# Future Enhancements

- Email Verification
- Password Reset
- AWS S3 Resume Storage
- Recruiter Analytics Dashboard
- Interview Scheduling
- Company Reviews
- Job Recommendations
- Notifications

---

# License

This project is intended for educational and portfolio purposes.