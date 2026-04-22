package models;

public enum EvaluationStatut {
    BROUILLON("brouillon"),
    PUBLIE("publie"),
    FERME("ferme");

    private final String dbValue;

    EvaluationStatut(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static EvaluationStatut fromDbValue(String dbValue) {
        for (EvaluationStatut statut : values()) {
            if (statut.dbValue.equalsIgnoreCase(dbValue)) {
                return statut;
            }
        }
        throw new IllegalArgumentException("Unknown evaluation statut: " + dbValue);
    }
}

