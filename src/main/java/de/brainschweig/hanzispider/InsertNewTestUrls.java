package de.brainschweig.hanzispider;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.xml.bind.DatatypeConverter; // Or use a custom hex converter if strictly no extra deps desired, but let's stick to standard Java if possible. 
// Actually, let's use a simple hex helper to avoid javax.xml.bind in newer Java versions.

import org.hibernate.Session;
import org.hibernate.Transaction;
import de.brainschweig.hanzispider.entities.Url;

public class InsertNewTestUrls {

	public static void main(String[] args) {
		System.out.println("Inserting new test URLs into database...");
		
		String[] urls = {
			"https://www.sina.com/",
			"https://www.qq.com/",
			"https://www.163.com/",
			"https://www.sohu.com/",
			"https://www.ifeng.com/"
		};
		
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = null;
		
		try {
			for (String url : urls) {
				try {
					transaction = session.beginTransaction();
					Url urlObj = new Url();
					urlObj.setUrl(url);
					urlObj.setMd5Sum(getMD5(url));
					urlObj.setMTimeStamp(new java.sql.Timestamp(System.currentTimeMillis()));
					session.save(urlObj);
					transaction.commit();
					System.out.println("Inserted: " + url);
				} catch (Exception e) {
					if (transaction != null) transaction.rollback();
					System.out.println("Skipping " + url + ": " + e.getMessage());
				}
			}
			System.out.println("Done processing URLs.");
		} finally {
			session.close();
			HibernateUtil.shutdown();
		}
	}

	private static String getMD5(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] messageDigest = md.digest(input.getBytes());
			StringBuilder sb = new StringBuilder();
			for (byte b : messageDigest) {
				sb.append(String.format("%02x", b));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
}
