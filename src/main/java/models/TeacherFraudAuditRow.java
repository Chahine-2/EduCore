package models;

/**
 * Read-only row for the teacher fraud audit table (joined log + user + evaluation).
 */
public record TeacherFraudAuditRow(
        int logId,
        String detectedAt,
        int studentUserId,
        String student,
        String evaluation,
        String fraudType,
        String description,
        int resultatId
) {}
