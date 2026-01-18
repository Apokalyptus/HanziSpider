package de.brainschweig.hanzispider;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import de.brainschweig.hanzispider.entities.Result;
import de.brainschweig.hanzispider.entities.Url;

class DatabaseTest {

    @BeforeEach
    void setUp() {
        cleanupDatabase();
    }

    @AfterEach
    void tearDown() {
        cleanupDatabase();
    }

    private void cleanupDatabase() {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            // Use DELETE FROM which is more portable and respects FKs if deleted in order
            // Assuming Status -> Url dependency
            session.createNativeQuery("DELETE FROM status").executeUpdate();
            session.createNativeQuery("DELETE FROM results").executeUpdate();
            session.createNativeQuery("DELETE FROM url").executeUpdate();
            
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Insert Crawl Result")
    void testInsertResult() {
        Database.insertResult("Ni Hao!");
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Result result = session.get(Result.class, "Ni Hao!");
            Assertions.assertNotNull(result);
        }
    }

    @Test
    @DisplayName("Insert a list of hyperlinks to Database")
    void testStoreHyperLinks() {
        Set<String> stringSet = new HashSet<>();
        stringSet.add("http://example.com/eins");
        stringSet.add("http://example.com/zwei");
        stringSet.add("http://example.com/drei");
        stringSet.add("http://example.com/vier");
        Database.storeHyperLinks(stringSet);

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Url WHERE url IN (:urls)";
            List<Url> urls = session.createQuery(hql, Url.class)
                    .setParameterList("urls", stringSet)
                    .list();
            Assertions.assertEquals(4, urls.size());
        }
    }
}