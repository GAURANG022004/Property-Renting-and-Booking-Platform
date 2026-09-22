# Property Rental & Booking Platform - Manual Runbook

This runbook verifies the full-stack flow for a college final-year/placement project:
Angular frontend + Spring Boot backend + MySQL DB, with JWT auth and role-based access.

---

## 1. Prerequisites

1. MySQL Server running
2. Java 17 installed
3. Node.js + npm installed

---

## 2. Backend Setup (Spring Boot)

1. Go to backend folder:
   - `property-rental/`
2. Ensure `application.properties` points to your MySQL:
   - `spring.datasource.url=jdbc:mysql://localhost:3306/property_db`
   - `spring.datasource.username=root`
3. Start backend:
   - `./mvnw.cmd spring-boot:run`
4. On first run, Spring Boot will auto-create tables using:
   - `spring.jpa.hibernate.ddl-auto=update`

### Default ADMIN user (Seeder)
The backend seeds an admin user at startup:
- Email: `admin@example.com`
- Password: `Admin@12345`

If you change `app.adminEmail` / `app.adminPassword`, use the updated values.

### Image upload folder
Uploaded images are stored on disk under the configured upload directory:
- Default: `uploads/`
The backend serves them under:
- `/images/**`

---

## 3. Frontend Setup (Angular)

1. Go to frontend folder:
   - `property-rental-frontend/`
2. Start Angular:
   - `npm run start`
3. Confirm the UI is running at:
   - `http://localhost:4200`

---

## 4. Verification Checklist (User Flows)

### A) Register + Login (JWT)
1. Open `http://localhost:4200/register`
2. Register a new user (email + password)
3. Ensure it redirects to `/login`
4. Login with the same user
5. Expected results:
   - Backend returns a JWT token
   - Angular stores token in `localStorage` (key: `auth_token`)

---

### B) View Properties (Public)
1. Open `http://localhost:4200/`
2. Ensure property cards are shown
3. Click a property card to open property details:
   - `/properties/:id`
4. Expected results:
   - `GET /properties` works without login
   - `GET /properties/:id` works without login
   - Property images render (from `/images/**`)

---

### C) Search & Filter (Public)
1. In home page, filter by:
   - `location` (partial match)
   - `minPrice`
   - `maxPrice`
   - `minRating`
2. Click `Search`
3. Expected results:
   - Results list updates according to filters

---

### D) Owner Add Property + Image Upload
1. Login as a user (or use the default admin)
2. Open:
   - `/owner/add-property`
3. Fill:
   - Title, Description, Location, Price/Night, Rating
4. Select one or more images
5. Click “Create & Upload”
6. Expected results:
   - Calls `POST /properties` to create the property
   - Then calls `POST /properties/{propertyId}/images` (multipart)
   - Upload succeeds and backend returns image URLs
   - New property appears on the home listings with images

---

### E) Booking a Property (No Double Booking)
1. Open property details page:
   - `/properties/:id`
2. Click “Book this property”
3. Submit a valid booking range:
   - Enter `check-in` and `check-out` dates
4. Expected results:
   - Calls `POST /bookings`
   - Booking created successfully
   - Redirects to `/my-bookings`

#### Double booking prevention
1. Try to create another booking for the same property with overlapping dates:
   - Any overlap should fail (based on check-in inclusive, check-out exclusive)
2. Expected results:
   - Backend returns `409 Conflict`
   - UI shows an error message (“already booked for the selected dates”)

---

### F) Booking History
1. While logged in, open:
   - `/my-bookings`
2. Expected results:
   - UI calls `GET /bookings`
   - Booking history displays only the logged-in user’s bookings
3. Logout, login with a different user, repeat step 1
4. Expected results:
   - The booking list changes accordingly

---

## 5. Security/Access Checks (Quick)

1. Without login, open:
   - `/owner/add-property`
   - `/my-bookings`
   - `/properties/:id/book`
2. Expected results:
   - Frontend redirects to `/login` due to guards
   - Backend endpoints also remain protected (401/403)

---

## 6. Submission Notes (What to mention in viva)

- Controller → Service → Repository → Model structure
- JWT authentication with `Authorization: Bearer <token>`
- Role-based route protection in Angular
- Double booking prevention via repository overlap query
- Image upload stored on filesystem and served via `/images/**`

