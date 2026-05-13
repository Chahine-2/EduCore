# Gestion de la Présence des Étudiants (Attendance Management)

## Overview

This feature allows administrators to easily track and manage student attendance by class/course. The system provides:

- 📋 **Class Selection**: Select a specific course to manage attendance
- 📅 **Date Management**: Choose the date for attendance records  
- ✅ **Attendance Marking**: Mark students as present or absent
- 📝 **Notes**: Add notes for each student (e.g., reasons for absence)
- 📊 **Statistics**: View real-time attendance statistics and rates
- 💾 **Data Persistence**: Save and load attendance records from the database

## Database Structure

### Presence Table
```sql
CREATE TABLE presence (
    id INT PRIMARY KEY AUTO_INCREMENT,
    etudiant_id INT NOT NULL,
    cours_id INT NOT NULL,
    date_presence DATE NOT NULL,
    est_present BOOLEAN DEFAULT TRUE,
    notes TEXT,
    date_enregistrement DATETIME DEFAULT NOW(),
    UNIQUE KEY unique_presence (etudiant_id, cours_id, date_presence),
    FOREIGN KEY (etudiant_id) REFERENCES utilisateurs(id),
    FOREIGN KEY (cours_id) REFERENCES cours(id)
);
```

## Installation Instructions

### Step 1: Apply Database Migration
Run the migration script in MySQL:
```sql
USE educore;
SOURCE migration_add_presence.sql;
```

Or manually execute the SQL commands from `migration_add_presence.sql` file.

### Step 2: Verify Files are in Place
Ensure these files exist in your project:

**Java Classes:**
- `src/main/java/models/Presence.java` (Model)
- `src/main/java/services/ServicePresence.java` (Service)
- `src/main/java/controllers/GestionPresenceController.java` (Controller)

**UI Files:**
- `src/main/resources/views/GestionPresence.fxml` (Layout)
- `src/main/resources/css/gestion-presence.css` (Styling)

### Step 3: Integrate into Admin Dashboard
To add this feature to the admin dashboard menu, modify `AdminDashboardController.java`:

```java
// Add a button or menu item to navigate to GestionPresence
private void ouvrirGestionPresence(ActionEvent event) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/GestionPresence.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Gestion de la Présence");
        stage.setWidth(1000);
        stage.setHeight(700);
        stage.show();
    } catch (IOException e) {
        e.printStackTrace();
    }
}
```

## Usage Guide

### 1. **Select a Course**
   - Use the dropdown menu to select the course/class you want to manage
   - The list of all students will automatically load

### 2. **Choose a Date**
   - Click on the date picker to select the date for attendance
   - Defaults to today's date

### 3. **Load Previous Records**
   - Click "📂 Charger" (Load) to retrieve existing attendance records for that date
   - Current attendance status will be loaded into the table

### 4. **Mark Attendance**
   - Check the "Présent(e)" column for each student to mark them as present
   - Uncheck to mark as absent
   - Table is editable - click on cells to modify

### 5. **Add Notes (Optional)**
   - Click on the "Notes" column to add remarks
   - Useful for recording reasons for absence or special notes

### 6. **Save Records**
   - Click "💾 Sauvegarder" (Save) to persist all changes to the database
   - System shows a success message when done

### 7. **View Statistics**
   - Attendance statistics display automatically
   - Shows: Total sessions, Presences, Absences, and Attendance Rate

## Features in Detail

### ServicePresence Service Methods

- `add(Presence)` - Add a new attendance record
- `update(Presence)` - Update an existing record
- `getPresenceByCoursId(coursId)` - Get all attendance for a course
- `getPresenceByEtudiantId(etudiantId)` - Get attendance history of a student
- `getPresenceByCoursIdAndDate(coursId, date)` - Get attendance for specific date
- `getPresenceStatsForCours(coursId)` - Get attendance statistics for a course
- `getTauxPresenceEtudiant(etudiantId, coursId)` - Calculate attendance rate for a student
- `delete(presenceId)` - Remove an attendance record

## Data Model

### Presence Object Properties
```java
- id: int (Primary key)
- etudiantId: int (Student ID, FK)
- coursId: int (Course ID, FK)
- datePresence: LocalDate (Attendance date)
- estPresent: boolean (Present/Absent)
- notes: String (Optional notes)
- dateEnregistrement: LocalDateTime (Record timestamp)
```

## SQL Queries Available

### Attendance Statistics View
```sql
-- See all attendance statistics per course and student
SELECT * FROM v_presence_stats;
```

### Get Attendance Rate for a Student in a Course
```sql
CALL calculate_attendance_rate(student_id, course_id);
```

## Error Handling

The system includes comprehensive error handling:
- Database connection errors
- Invalid selections
- Data validation
- User-friendly error messages in pop-up dialogs

## Best Practices

1. **Regular Updates**: Update attendance regularly after each class
2. **Notes Documentation**: Use notes to document absences for administrative purposes
3. **Backup**: Keep regular backups of your database
4. **Verification**: Double-check before saving large batches
5. **Statistics Review**: Regularly review the statistics to identify trends

## Troubleshooting

### Database Connection Error
- Ensure MySQL/WAMP is running
- Check database credentials in `config.properties`
- Verify the presence table was created successfully

### Table Not Loading
- Verify the course selection
- Check that students exist in the system
- Ensure database connection is active

### Changes Not Saving
- Click "Sauvegarder" button explicitly
- Check for validation errors in status bar
- Verify database permissions

## Future Enhancements

Potential improvements for this feature:
- Bulk attendance import from CSV
- Email notifications for low attendance
- Attendance reports export (PDF/Excel)
- Automatic warning system for chronic absenteeism
- QR code scanning for attendance
- Mobile app integration

## Conclusion

The Gestion de Présence module provides a professional, user-friendly interface for managing student attendance. It integrates seamlessly with the EduCore platform and provides administrators with real-time visibility into student attendance patterns.

