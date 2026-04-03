package Prueba;

import jakarta.persistence.*;

// Utilidad manual de diagnóstico; se reubica fuera de src/main/java para no formar parte del build principal.
public class PersistenceTest {
    public static void main(String[] args) {
        // Crear el EntityManagerFactory usando el nombre de la unidad de persistencia definida en persistence.xml
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("miUnidadDePersistencia");
        EntityManager em = emf.createEntityManager();

        try {
            System.out.println("Conexión exitosa a la base de datos");
        } finally {
            em.close();
            emf.close();
        }
    }
}

