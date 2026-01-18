package de.brainschweig.hanzispider;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

public class FixDatabase {

	public static void main(String[] args) {
		System.out.println("Fixing database schema...");
		
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = null;
		
		try {
			transaction = session.beginTransaction();
			
			// Drop and recreate results table
			try {
				Query<?> dropQuery = session.createNativeQuery("DROP TABLE results");
				dropQuery.executeUpdate();
				System.out.println("Dropped old results table");
			} catch (Exception e) {
				System.out.println("Results table doesn't exist or couldn't be dropped: " + e.getMessage());
			}
			
			// Create new results table with correct schema
			Query<?> createQuery = session.createNativeQuery(
				"CREATE TABLE results (" +
				"  id BIGSERIAL PRIMARY KEY," +
				"  hanzi VARCHAR(2048) NOT NULL," +
				"  redundant VARCHAR(255)" +
				")"
			);
			createQuery.executeUpdate();
			System.out.println("Created new results table with id column");
			
			transaction.commit();
			System.out.println("Database schema fixed!");
		} catch (Exception e) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			System.err.println("Error fixing database: " + e.getMessage());
			e.printStackTrace();
		} finally {
			session.close();
		}
	}
}
