package models;

public enum BaremeMention {
    EXCELLENT("Excellent"),
    BIEN("Bien"),
    PASSABLE("Passable"),
    INSUFFISANT("Insuffisant");

    private final String dbValue;

    BaremeMention(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static BaremeMention fromDbValue(String dbValue) {
        for (BaremeMention mention : values()) {
            if (mention.dbValue.equalsIgnoreCase(dbValue)) {
                return mention;
            }
        }
        throw new IllegalArgumentException("Unknown bareme mention: " + dbValue);
    }
}

