package ru.sfedu.project.db;

import org.hibernate.Session;
import org.hibernate.Transaction;
import ru.sfedu.project.entities.TestEntity;
import ru.sfedu.project.utils.HibernateUtil;

public class HibernateMysqlClient {
    public static Long createTestEntity(TestEntity entity) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(entity);
            transaction.commit();
            return entity.getId();
        }
    }

    public static TestEntity readTestEntity(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(TestEntity.class, id);
        }
    }

    public static void updateTestEntity(TestEntity entity) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.merge(entity);
            transaction.commit();
        }
    }

    public static void deleteTestEntity(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            TestEntity entity = session.get(TestEntity.class, id);
            if (entity != null)
                session.remove(entity);
            transaction.commit();
        }
    }
}

