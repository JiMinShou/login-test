# Java + Vue Auth Demo

## Structure

- `backend/` Spring Boot 3 + JWT (access/refresh) + MySQL
- `frontend/` Vue 3 + Vite + Pinia + Vue Router

## Installed Toolchain (this machine)

- Java: `openjdk 17.0.18`
- Maven: `3.9.11` (installed under `tools/apache-maven-3.9.11`)
- Node.js: `v24.14.1`
- npm: `11.11.0`
- MySQL client: `8.4.8`

## MySQL Preparation

1. Ensure MySQL server service is initialized and running.
2. Create database:

```sql
CREATE DATABASE IF NOT EXISTS auth_demo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

3. Set backend env vars (see `backend/.env.example`) or export in shell.

## Run Backend

```powershell
cd "C:\Users\Administrator\Documents\New project 2\backend"
mvn spring-boot:run
```

## Run Frontend

```powershell
cd "C:\Users\Administrator\Documents\New project 2\frontend"
npm install
npm run dev
```

## API Summary

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`
- `GET /api/users/me`
- `GET /api/admin/users`

## Test and Build

```powershell
cd "C:\Users\Administrator\Documents\New project 2\backend"
mvn test

cd "C:\Users\Administrator\Documents\New project 2\frontend"
npm run build
```