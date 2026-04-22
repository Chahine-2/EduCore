package models;

public enum EvaluationType {
    QCM("qcm"),
    EXAMEN("examen"),
    DEVOIR("devoir"),
    PROJET("projet"),
    TP("tp");

    private final String dbValue;

    EvaluationType(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static EvaluationType fromDbValue(String dbValue) {
        for (EvaluationType type : values()) {
            if (type.dbValue.equalsIgnoreCase(dbValue)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown evaluation type: " + dbValue);
    }
}

