# EDUCORE JDBC CRUD - Evaluation

This project contains a student-friendly JDBC CRUD application for the `evaluation` table in MySQL.

## Architecture

- `src/main/java/test.Main.java` - Console menu and app entry point
- `src/main/java/models/Evaluation.java` - Entity model
- `src/main/java/models/EvaluationType.java` - Enum for `type`
- `src/main/java/interfaces/EvaluationDAO.java` - DAO contract
- `src/main/java/services/EvaluationDAOImpl.java` - JDBC implementation with `PreparedStatement`
- `src/main/java/utils/DBConnection.java` - MySQL connection singleton

## Database setup

Run `setup.sql` in MySQL:

```sql
CREATE DATABASE IF NOT EXISTS educore;
USE educore;

CREATE TABLE IF NOT EXISTS evaluation (
    id INT AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    type ENUM('qcm', 'examen', 'devoir', 'projet', 'tp') NOT NULL,
    duree_minutes INT NOT NULL,
    note_max FLOAT NOT NULL,
    date_debut DATETIME NOT NULL,
    date_fin DATETIME NOT NULL
);
```

## Run

1. Build:

```powershell
mvn clean compile
```

2. Run the menu:

```powershell
mvn exec:java -Dexec.mainClass="test.Main"
```

Date input format in menu:

`yyyy-MM-dd HH:mm:ss`

