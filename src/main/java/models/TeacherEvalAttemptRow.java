package models;

/**
 * One student attempt on an evaluation, formatted for the teacher results table.
 */
public record TeacherEvalAttemptRow(
        int resultatId,
        String student,
        String score,
        String outcome,
        String integrity,
        String completedAt
) {}
