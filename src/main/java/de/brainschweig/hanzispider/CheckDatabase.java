package de.brainschweig.hanzispider;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import de.brainschweig.hanzispider.entities.Url;
import de.brainschweig.hanzispider.entities.Status;

import java.util.List;

public class CheckDatabase {

	public static void main(String[] args) {
		System.out.println("Checking database contents...");
		
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = null;
		
		try {
			transaction = session.beginTransaction();
			
			// Count URLs
			String urlCountQuery = "SELECT COUNT(*) FROM Url";
			Query<?> query = session.createQuery(urlCountQuery);
			Long urlCount = (Long) query.uniqueResult();
			System.out.println("Total URLs in database: " + urlCount);
			
			// Count Status entries
			String statusCountQuery = "SELECT COUNT(*) FROM Status";
			query = session.createQuery(statusCountQuery);
			Long statusCount = (Long) query.uniqueResult();
			System.out.println("Total Status entries in database: " + statusCount);
			
			// Count Results
			String resultsCountQuery = "SELECT COUNT(*) FROM Result";
			query = session.createQuery(resultsCountQuery);
			Long resultsCount = (Long) query.uniqueResult();
			System.out.println("Total Results in database: " + resultsCount);
			
			// Show all URLs
			System.out.println("\nAll URLs:");
			List<Url> urls = session.createQuery("FROM Url", Url.class).list();
			for (Url url : urls) {
				System.out.println("  ID: " + url.getIdUrl() + ", URL: " + url.getUrl());
			}
			
			// Show all Status entries
			System.out.println("\nAll Status entries:");
			List<Status> statuses = session.createQuery("FROM Status", Status.class).list();
			for (Status status : statuses) {
				System.out.println("  URL ID: " + status.getUrlId() + ", Status: " + status.getStatus());
			}
			
			transaction.commit();
		} catch (Exception e) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			System.err.println("Error checking database: " + e.getMessage());
			e.printStackTrace();
		} finally {
			session.close();
		}
	}
}
