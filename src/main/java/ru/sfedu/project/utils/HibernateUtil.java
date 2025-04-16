package ru.sfedu.project.utils;

import jakarta.persistence.Tuple;
import org.apache.logging.log4j.core.config.Configurator;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import ru.sfedu.project.Constants;
import ru.sfedu.project.entities.HistoryEntity;

import java.util.List;

import static ru.sfedu.project.Constants.log;

public class HibernateUtil {
    private static SessionFactory sessionFactory;

    static {
        HistoryEntity historyEntity = HistoryEntity.init("SqlDatabaseClient", "putMatchPlayers()", "Saved players for match");
        Configurator.setLevel("org.hibernate", org.apache.logging.log4j.Level.OFF);

        try {
            Configuration configuration = new Configuration().configure(Constants.HIBERNATE_DEFAULT_CONFIG_PATH)
                    .addAnnotatedClass(ru.sfedu.project.entities.TestEntity.class)
                    .addAnnotatedClass(ru.sfedu.project.entities.CustomComponent.class);
            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties()).build();

            sessionFactory = configuration.buildSessionFactory(serviceRegistry);
            DatabaseUtil.save(historyEntity);
        } catch (Exception e) {
            historyEntity.setStatus(HistoryEntity.Status.FAIL);
            historyEntity.setMessage(e.getMessage());

            log.debug("Error in HibernateUtil static: {}", e.getMessage());
            DatabaseUtil.save(historyEntity);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static List<String> getAllTables() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            NativeQuery<String> query = session.createNativeQuery(Constants.HIBERNATE_TABLES, String.class);
            return query.getResultList();
        }
    }

    public static String getDatabaseSize() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            NativeQuery<String> query = session.createNativeQuery(Constants.HIBERNATE_SIZE, String.class);
            return query.uniqueResult() + " bytes";
        }
    }

    public static List<String> getUsers() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            NativeQuery<String> query = session.createNativeQuery(Constants.HIBERNATE_USERS, String.class);
            return query.getResultList();
        }
    }

    public static List<Tuple> getColumnTypes() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createNativeQuery(Constants.HIBERNATE_COLS, Tuple.class).getResultList();
        }
    }
}

