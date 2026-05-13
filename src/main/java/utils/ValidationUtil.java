package utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ValidationUtil {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Validate that a string is not empty
     */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Validate that a string can be parsed as a positive integer
     */
    public static boolean isValidPositiveInteger(String value) {
        try {
            int num = Integer.parseInt(value);
            return num > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validate that a string can be parsed as a non-negative integer
     */
    public static boolean isValidNonNegativeInteger(String value) {
        try {
            int num = Integer.parseInt(value);
            return num >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validate that a string can be parsed as a positive float
     */
    public static boolean isValidPositiveFloat(String value) {
        try {
            float num = Float.parseFloat(value);
            return num > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validate that a string can be parsed as a non-negative float
     */
    public static boolean isValidNonNegativeFloat(String value) {
        try {
            float num = Float.parseFloat(value);
            return num >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validate that a string is a valid date-time format (yyyy-MM-dd HH:mm:ss)
     */
    public static boolean isValidDateTime(String value) {
        try {
            LocalDateTime.parse(value, DATE_TIME_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Validate that notePassage <= noteMax
     */
    public static boolean isValidNoteRange(float notePassage, float noteMax) {
        return notePassage <= noteMax;
    }

    /**
     * Validate that noteMin < noteMax
     */
    public static boolean isValidBaremeRange(float noteMin, float noteMax) {
        return noteMin < noteMax;
    }

    /**
     * Get user-friendly error message
     */
    public static String getErrorMessage(String fieldName, String validationType) {
        return switch (validationType) {
            case "EMPTY" -> fieldName + " ne peut pas être vide";
            case "POSITIVE_INT" -> fieldName + " doit être un nombre entier positif";
            case "NON_NEGATIVE_INT" -> fieldName + " doit être un nombre entier positif ou zéro";
            case "POSITIVE_FLOAT" -> fieldName + " doit être un nombre positif";
            case "NON_NEGATIVE_FLOAT" -> fieldName + " doit être un nombre positif ou zéro";
            case "DATETIME" -> fieldName + " doit être au format: yyyy-MM-dd HH:mm:ss";
            case "NOTE_RANGE" -> "Note passage ne peut pas dépasser Note max";
            case "BAREME_RANGE" -> "Note min doit être inférieure à Note max";
            default -> "Valeur invalide pour " + fieldName;
        };
    }
}

