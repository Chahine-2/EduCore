# Fraud Detection Feature Documentation

## Overview
This document describes the fraud detection feature implementation in EDUCORE. When a student commits fraud during a quiz (detected by the anti-cheat module), their score is automatically set to **0** and marked as fraudulent, with a prominent **red "FRAUD"** indicator displayed in their quiz attempt history.

## Feature Components

### 1. Database Schema Changes

#### New Column: `fraude_detecte` in the `resultat` table
- **Type**: BOOLEAN
- **Default**: FALSE
- **Purpose**: Mark quiz attempts where fraud was detected

```sql
ALTER TABLE resultat 
ADD COLUMN fraude_detecte BOOLEAN DEFAULT FALSE;
```

### 2. Model Updates

#### Resultat Model (`models/Resultat.java`)
Added the following:
- Field: `fraudeDetecte: boolean`
- Getter: `isFraudeDetecte()`
- Setter: `setFraudeDetecte(boolean)`

```java
private boolean fraudeDetecte;

public boolean isFraudeDetecte() {
    return fraudeDetecte;
}

public void setFraudeDetecte(boolean fraudeDetecte) {
    this.fraudeDetecte = fraudeDetecte;
}
```

### 3. Database Access Layer

#### ResultatDAOImpl Updates
All methods that interact with the database have been updated to handle the `fraude_detecte` column:

- `add()` - Inserts fraud flag with new records
- `update()` - Updates fraud flag on existing records
- `getById()` - Reads fraud flag from database
- `getAll()` - Reads fraud flag for all results
- `insertAndGetId()` - Returns generated ID while saving fraud flag
- `mapRow()` - Helper method that maps fraud flag from ResultSet
- `findLatestByStudentAndEvaluation()` - Uses mapRow for consistency
- **`corrigerEvaluation()`** - Special logic: if `fraude_detecte = true`, sets score to 0

### 4. Quiz Controller Logic

#### StudentQuizController Updates
Enhanced the `detectFraude()` method to:

```java
private void detectFraude(String type, String description) {
    if (viewMode || submitted || fraudeDetected) {
        return;
    }
    fraudeDetected = true;
    int resultatId = ensureResultatRow();
    if (resultatId > 0) {
        // Mark the resultat as fraudulent
        Resultat resultat = resultatDAO.getById(resultatId);
        if (resultat != null) {
            resultat.setFraudeDetecte(true);
            resultatDAO.update(resultat);
        }
        
        // Log the fraud details
        fraudeLogDAO.logFraude(new FraudeLog(
                resultatId,
                studentId,
                type,
                description
        ));
    }
    
    // ... Alert and auto-submit logic ...
}
```

**Behavior**:
1. When fraud is detected by the anti-cheat system
2. The `resultat` record is immediately marked with `fraude_detecte = true`
3. An entry is logged in the `fraude_log` table
4. A warning alert is shown to the student
5. The quiz is auto-submitted
6. During score correction (`corrigerEvaluation`), the score is set to 0

### 5. Student Portal Display

#### StudentPortalController Updates
The quiz card display now shows fraud status:

**For fraudulent attempts**:
- A red badge labeled "FRAUDE" appears in the top-right
- The score area displays "FRAUDE" in red bold text instead of the numerical score
- The card is visually marked as completed but with fraud status

**Code snippet**:
```java
if (submitted != null && submitted.isFraudeDetecte()) {
    Label fraudBadge = new Label("FRAUDE");
    fraudBadge.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 4; -fx-font-weight: bold;");
    top.getChildren().add(fraudBadge);
}

if (submitted.isFraudeDetecte()) {
    titleScore = new Label("FRAUDE");
    titleScore.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-font-size: 14;");
}
```

## Fraud Detection Triggers

The anti-cheat system detects the following types of fraud:

1. **WINDOW_FOCUS_LOST** - Exam window lost focus
2. **FULLSCREEN_EXIT** - Fullscreen mode was exited
3. **CAMERA_OFF** - Camera disconnected or turned off
4. **CAMERA_BLOCKED** - Camera blocked or feed lost
5. **NO_FACE_DETECTED** - No face visible in the frame
6. **MULTIPLE_FACES** - Multiple faces detected
7. **MOTION_ANOMALY_NO_FACE** - Movement with no face visible
8. **OBJECT_NEAR_FACE** - Object movement near face (e.g., hands, phone)
9. **EXCESSIVE_HEAD_TURNS** - Too many head turns away from camera
10. **PHONE_IN_FRAME** - Phone or external device visible in frame

## Installation & Setup

### For New Installations
The `fraude_detecte` column is automatically created with the schema in `setup.sql`.

### For Existing Installations
Run the migration script:

```bash
mysql -u root -p educore < migration_add_fraud_flag.sql
```

Or manually execute:
```sql
USE educore;

ALTER TABLE resultat 
ADD COLUMN fraude_detecte BOOLEAN DEFAULT FALSE;
```

## User Experience

### Student View
- When a quiz attempt is detected as fraudulent:
  1. A warning dialog appears explaining the fraud type
  2. The quiz is automatically submitted
  3. In the student portal, the quiz card shows "FRAUDE" in red
  4. The detailed score is replaced with "FRAUDE"
  5. The attempt is still visible for review but marked as invalid

### Red Color Standards
- Badge color: `#ef4444` (red-500)
- Text color: white on badge, red text for scores
- Font-weight: bold
- Size: Regular text at 14pt for fraud indicators

## Database Queries

### View all fraudulent quiz attempts
```sql
SELECT r.id, r.student_id, r.evaluation_id, r.score, r.date_passage, r.fraude_detecte
FROM resultat r
WHERE r.fraude_detecte = TRUE
ORDER BY r.date_passage DESC;
```

### Count fraudulent attempts by evaluation
```sql
SELECT e.id, e.titre, COUNT(*) as fraud_count
FROM resultat r
JOIN evaluation e ON r.evaluation_id = e.id
WHERE r.fraude_detecte = TRUE
GROUP BY e.id, e.titre
ORDER BY fraud_count DESC;
```

### Check fraud logs for an attempt
```sql
SELECT fl.id, fl.resultat_id, fl.type_fraude, fl.description, fl.date_detection
FROM fraude_log fl
WHERE fl.resultat_id = ?
ORDER BY fl.date_detection DESC;
```

## Testing Checklist

- [ ] Compile project without errors: `mvn clean compile`
- [ ] Run migration script on test database
- [ ] Create a test quiz
- [ ] Simulate fraud detection (manually set `fraude_detecte = true` in DB)
- [ ] Verify score is set to 0 after correction
- [ ] Verify "FRAUDE" badge appears in red on student portal
- [ ] Test quiz review mode with fraud
- [ ] Verify fraud logs are properly recorded
- [ ] Check that normal (non-fraudulent) attempts still show scores correctly

## API Endpoints / Methods Modified

- `ResultatDAOImpl.corrigerEvaluation(int resultatId)` - Now checks fraud flag before scoring
- `StudentQuizController.detectFraude(String type, String description)` - Now marks resultat as fraudulent
- `StudentPortalController.buildCard(Evaluation ev, Resultat submitted)` - Now displays fraud status

## Files Changed

1. `setup.sql` - Added `fraude_detecte` column to schema
2. `migration_add_fraud_flag.sql` - Migration script for existing databases
3. `models/Resultat.java` - Added fraud flag field and accessors
4. `services/ResultatDAOImpl.java` - Updated all DB operations
5. `controllers/StudentQuizController.java` - Fraud detection logic
6. `controllers/StudentPortalController.java` - UI display of fraud status

## Future Enhancements

1. **Admin Dashboard** - Display fraud statistics by evaluation/student
2. **Fraud Review Interface** - Let instructors review and appeal fraudulent marks
3. **Fraud Severity Levels** - Different responses based on fraud type
4. **Automated Actions** - Notify instructors, lock student accounts, etc.
5. **Appeals Process** - Allow students to contest fraud decisions
6. **Analytics** - Track fraud patterns and improve detection

## Troubleshooting

### Issue: Score shows but "FRAUDE" badge still appears
**Solution**: Ensure `corrigerEvaluation` is called after fraud is detected. Check that the resultat is being updated with `fraude_detecte = true` before scoring.

### Issue: Migration script fails with "column already exists"
**Solution**: The column may already exist. This is normal for fresh installs. Verify the column with: 
```sql
DESCRIBE resultat;
```

### Issue: Fraud flag not persisting
**Solution**: Verify that `resultatDAO.update(resultat)` is being called. Check database logs for errors.

## Support

For issues or questions regarding this feature, refer to:
- Anti-cheat module documentation (FraudeService)
- StudentQuizController javadoc
- Database schema in setup.sql

