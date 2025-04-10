package ch.hoffmann.jan.warehouse.model;

public enum AuditAction {
    ADD,
    REMOVE,
    TRANSFER;

    public static boolean isValid(String action) {
        try {
            valueOf(action);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}