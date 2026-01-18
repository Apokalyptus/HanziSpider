package de.brainschweig.hanzispider;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

public class ResetDatabase {

	public static void main(String[] args) {
		System.out.println("Resetting database for testing...");
		
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = null;
		
		try {
			transaction = session.beginTransaction();
			
			// Delete all results
			Query<?> query = session.createNativeQuery("DELETE FROM results");
			int deletedResults = query.executeUpdate();
			System.out.println("Deleted " + deletedResults + " results");
			
			// Delete all status
			query = session.createNativeQuery("DELETE FROM status");
			int deletedStatus = query.executeUpdate();
			System.out.println("Deleted " + deletedStatus + " status entries");
			
			transaction.commit();
			System.out.println("Database reset complete!");
		} catch (Exception e) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			System.err.println("Error resetting database: " + e.getMessage());
			e.printStackTrace();
		} finally {
			session.close();
		}
	}
}
