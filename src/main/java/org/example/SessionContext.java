package org.example;

public record SessionContext(Long userId, String username, Role role, Tienda tienda) {

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

    public enum Tienda {
        MARIHEL("Marihel"),
        QUEENS("Queens");

        private final String displayName;


        Tienda(String displayName) {
            this.displayName = displayName;
        }

        public String displayName() {
            return displayName;
        }
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public boolean isQueens() {
        return tienda == Tienda.QUEENS;
    }
}

