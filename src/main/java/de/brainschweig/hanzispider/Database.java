package de.brainschweig.hanzispider;

import java.math.BigInteger;
import java.util.Map;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.LockMode;
import org.hibernate.query.Query; // Corrected import for Query
import org.hibernate.Session;
import org.hibernate.Transaction;
import de.brainschweig.hanzispider.entities.Result;
import de.brainschweig.hanzispider.entities.Status;
import de.brainschweig.hanzispider.entities.Url;

public class Database {

	private static final Logger logger = LogManager.getLogger(Database.class.getName());

	private Database() {
		throw new IllegalStateException("Utility class");
	}

	public static void insertResult(String result) {
		Transaction transaction = null;
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			transaction = session.beginTransaction();
			Result cr = new Result();
			cr.setHanzi(result);
			session.save(cr);
			transaction.commit();
		} catch (Exception e) {
			if (transaction != null) {
				transaction.rollback();
			}
			logger.error("Failed to insert result", e);
		}
	}

	public static void storeHyperLinks(Set<String> hyperLinks) {
		if (hyperLinks == null || hyperLinks.isEmpty()) {
			return;
		}

		MessageDigest m;
		try {
			m = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException nsae) {
			logger.error("MD5 algorithm not found", nsae);
			return;
		}

		// Create a map of md5sum -> original URL
		Map<String, String> md5ToUrl = new java.util.HashMap<>();
		for (String hyperLink : hyperLinks) {
			m.update(hyperLink.getBytes(), 0, hyperLink.length());
			String md5string = new BigInteger(1, m.digest()).toString(16);
			md5ToUrl.put(md5string, hyperLink);
		}

		// Find which md5s already exist in the database in a single query
		Set<String> existingMd5s;
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			String hql = "SELECT u.md5sum FROM Url u WHERE u.md5sum IN (:md5s)";
			existingMd5s = new java.util.HashSet<>(
					session.createQuery(hql, String.class)
							.setParameterList("md5s", md5ToUrl.keySet())
							.list());
		}

		// Insert the new URLs in a single transaction with batching
		Transaction transaction = null;
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			transaction = session.beginTransaction();
			int i = 0;
			for (Map.Entry<String, String> entry : md5ToUrl.entrySet()) {
				String md5sum = entry.getKey();
				if (!existingMd5s.contains(md5sum)) {
					String hyperLink = entry.getValue();

					Url ul = new Url();
					ul.setUrl(hyperLink);
					ul.setMd5Sum(md5sum);
					ul.setMTimeStamp(java.sql.Timestamp.from(Instant.now()));

					session.save(ul);
					i++;
					logger.info("Queued for insert: Hyperlink: {} MD5: {}", hyperLink, md5sum);

					// Flush and clear the session periodically to manage memory
					if (i % 50 == 0) {
						session.flush();
						session.clear();
					}
				}
			}
			transaction.commit();
			logger.info("Committed {} new hyperlinks to the database.", i);
		} catch (Exception e) {
			if (transaction != null) {
				transaction.rollback();
			}
			logger.error("Failed to insert new hyperlinks batch.", e);
		}
	}

	static boolean doesMd5Exist(String md5sum) {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			String hql = "SELECT count(md5sum) as md5count FROM Url WHERE md5sum = :md5sum";
			Query q = session.createQuery(hql).setParameter("md5sum", md5sum);
			@SuppressWarnings("unchecked")
			List<Long> list = q.getResultList();
			return !list.isEmpty() && list.get(0) > 0;
		}
	}

	public static void insertHyperLinkStatus(Long urlid, String status) {
		Transaction transaction = null;
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			transaction = session.beginTransaction();
			Status st = new Status();
			st.setUrlId(urlid);
			st.setStatus(status);
			st.setMTimeStamp(java.sql.Timestamp.from(Instant.now()));
			session.save(st);
			transaction.commit();
		} catch (Exception e) {
			if (transaction != null) {
				transaction.rollback();
			}
			logger.error("Failed to insert hyperlink status for urlid: " + urlid, e);
		}
	}

	static boolean fetchHyperLink(StringBuilder sUrlid, StringBuilder url) {
		Transaction transaction = null;
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			transaction = session.beginTransaction();

			String hql = "FROM Url u WHERE u.idurl NOT IN (SELECT s.urlId FROM Status s)";

			List<Url> urls = session.createQuery(hql, Url.class)
					.setMaxResults(1)
					.setLockMode(LockMode.PESSIMISTIC_WRITE) // Using setLockMode with LockMode.PESSIMISTIC_WRITE
					.list();

			if (urls.isEmpty()) {
				// This is a normal condition when the queue is empty, so changing log level to debug
				logger.debug("No new hyperlinks available in the database to process.");
				transaction.commit();
				return false;
			}

			Url nextUrl = urls.get(0);
			sUrlid.append(String.valueOf(nextUrl.getIdUrl()));
			url.append(nextUrl.getUrl());

			logger.info("Fetched urlid: {} url: {}", nextUrl.getIdUrl(), nextUrl.getUrl());

			Status st = new Status();
			st.setUrlId(nextUrl.getIdUrl());
			st.setStatus("check-out");
			st.setMTimeStamp(java.sql.Timestamp.from(Instant.now()));

			session.save(st);
			transaction.commit();

			logger.info("Checked-out urlid: {}", nextUrl.getIdUrl());

			return true;
		} catch (Exception e) {
			if (transaction != null) {
				transaction.rollback();
			}
			logger.error("Failed to fetch hyperlink", e);
			return false;
		}
	}

	static void insertCrawlResult(String result) {
		insertResult(result);
	}
}
