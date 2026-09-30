package edu.isistan.util;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class JPAUtil {

    private static final EntityManagerFactory emf;

    static {
        emf = Persistence.createEntityManagerFactory("Example");
    }

    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public static void cerrar() {
        emf.close();
    }
}