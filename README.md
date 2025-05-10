# 📘 E-Attendance App

A modern and efficient attendance management system built using **Java**, **XML**, and **Firebase Realtime Database**. This project aims to simplify the process of tracking and managing attendance for educational and organizational institutions through an intuitive and role-based mobile application.

---

## 🚀 Features

### 🔐 User Authentication
- Secure login for **Admin**, **Teacher**, and **Student**.
- Role-based home screen navigation for a personalized experience.

### 📚 Subject Registration (Admin)
- Add or remove subjects for any class and semester.
- Support for registering multiple subjects (>5) per class-semester combination.

### 👩‍🏫 Teacher Management (Admin)
- Register and remove teachers.
- View all registered teachers in the system.

### 👨‍🎓 Student Management (Admin)
- Register and remove students for specific classes and semesters.
- Accurate placement of students in academic hierarchy.

### 🧾 Attendance Viewing
- **Admin and Teachers** can:
  - View subject-wise attendance.
  - Filter records by date.
- **Students** can:
  - View their own attendance with a single click.

### 📑 View Data Lists (Admin)
- Display student lists and subject lists for selected class and semester.

### 🗂️ Batch Management (Admin)
- Remove entire student batches while preserving attendance data.
- Shift students to the next semester to ensure academic continuity.
- Teachers can delete attendance records for specific subjects and semesters.

### 🔒 Change Password
- All users can securely update their passwords.

---

## 🛠️ Technologies Used

- **Java** - Application Logic
- **XML** - UI Design
- **Firebase Realtime Database** - Cloud-based data storage

---

## 📱 App Architecture

The app is structured with **role-based interfaces** to ensure that Admins, Teachers, and Students each have access to only the features they need. Firebase Realtime Database ensures live updates and fast data access with minimal latency.

---

## 📦 Setup Instructions

1. Clone the repository:
