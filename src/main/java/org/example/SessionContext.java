package org.example;

public record SessionContext(Long userId, String username, Role role) {

    public enum Role {
        ADMIN,
        VENDEDOR;

        public static Role fromDbValue(String dbValue) {
            if (dbValue == null) {
                throw new IllegalArgumentException("Rol no puede ser null");
            }
            return Role.valueOf(dbValue.trim().toUpperCase());
        }
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
}

