package de.brainschweig.hanzispider;

import org.hibernate.Session;
import org.hibernate.Transaction;
import de.brainschweig.hanzispider.entities.Url;

public class InsertTestUrls {

	public static void main(String[] args) {
		System.out.println("Inserting test URLs into database...");
		
		String[] urls = {
			"http://www.toutiao.com/",
			"https://www.baidu.com/",
			"https://www.sina.com/",
			"https://www.qq.com/",
			"https://www.163.com/"
		};
		
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = null;
		
		try {
			for (String url : urls) {
				transaction = session.beginTransaction();
				Url urlObj = new Url();
				urlObj.setUrl(url);
				urlObj.setMd5Sum("");
				urlObj.setMTimeStamp(new java.sql.Timestamp(System.currentTimeMillis()));
				session.save(urlObj);
				transaction.commit();
				System.out.println("Inserted: " + url);
			}
			System.out.println("All test URLs inserted successfully!");
		} catch (Exception e) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			System.err.println("Error inserting URLs: " + e.getMessage());
			e.printStackTrace();
		} finally {
			session.close();
		}
	}
}
