package services;

import models.FraudeLog;

public interface FraudeLogDAO {
    void logFraude(FraudeLog log);
}
