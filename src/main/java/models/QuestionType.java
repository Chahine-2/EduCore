package models;

public enum QuestionType {
    QCM("qcm"),
    VRAI_FAUX("vrai_faux"),
    TEXTE_LIBRE("texte_libre"),
    CORRESPONDANCE("correspondance");

    private final String dbValue;

    QuestionType(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static QuestionType fromDbValue(String dbValue) {
        for (QuestionType type : values()) {
            if (type.dbValue.equalsIgnoreCase(dbValue)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown question type: " + dbValue);
    }
}

