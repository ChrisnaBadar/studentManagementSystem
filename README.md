# Student Management System (JavaFX)

## Description
This JavaFX application is designed for managing student records. It allows users to add, update, delete, and save student data, including their name, ID, course, and grade. The application is tailored for small-scale use, ideal for managing up to 20 students, and features a user-friendly interface.

## Features
- **Add Student**: Add new student records.
- **Update Student**: Update existing student records.
- **Delete Student**: Remove student records.
- **Save Data**: Save the current state of student records to a CSV file.
- **Unsaved Changes Prompt**: Prompt to save unsaved changes when closing the application.

## Getting Started

### Prerequisites
- Java JDK 11 or higher
- JavaFX SDK

### Installation
1. Clone the repository:
   ```
   git clone https://github.com/your-username/student-management-system.git
   ```
2. Open the project in your favorite Java IDE.

3. Ensure JavaFX is properly configured in your development environment.

### Running the Application
Run `App.java` to start the application. The main window with a table view of student data will appear.

## Usage

- **Add a Student**: Click "Add Student", fill in the details in the form, and submit.
- **Update a Student**: Select a student, view details, modify as needed, and update.
- **Delete a Student**: Select a student, view details, and choose delete.
- **Save Data**: Click "Save Data" to manually save to `students.csv`. On closing, the app will prompt to save if there are unsaved changes.
