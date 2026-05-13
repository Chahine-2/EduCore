package services;

import models.FraudeLog;
import models.TeacherFraudAuditRow;

import java.util.List;

public interface FraudeLogDAO {
    void logFraude(FraudeLog log);

    /** Anti-cheat audit trail, newest first; empty if table missing or no rows. */
    List<TeacherFraudAuditRow> findAllAuditRows();
}
