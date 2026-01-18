package de.brainschweig.hanzispider;

import java.math.BigInteger;
import java.util.Map;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
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
	private static final String STATUS_CHECK_OUT = "check-out";

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
			if (transaction != null && transaction.isActive()) {
				transaction.commit();
			}
		} catch (Exception e) {
			if (transaction != null && transaction.isActive()) {
				try {
					transaction.rollback();
				} catch (Exception rollbackException) {
					logger.warn("Failed to rollback transaction after error", rollbackException);
				}
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
			if (transaction != null && transaction.isActive()) {
				transaction.commit();
			}
			logger.info("Committed {} new hyperlinks to the database.", i);
		} catch (Exception e) {
			if (transaction != null && transaction.isActive()) {
				try {
					transaction.rollback();
				} catch (Exception rollbackException) {
					logger.warn("Failed to rollback transaction after error", rollbackException);
				}
			}
			logger.error("Failed to insert new hyperlinks batch.", e);
		}
	}

	public static void storeHyperLinksAndUpdateStatus(Long urlid, Set<String> hyperLinks, String status) {
		Transaction transaction = null;
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			transaction = session.beginTransaction();
			storeHyperLinksInSession(session, hyperLinks);
			updateUrlStatus(session, urlid, status);
			if (transaction != null && transaction.isActive()) {
				transaction.commit();
			}
		} catch (Exception e) {
			if (transaction != null && transaction.isActive()) {
				try {
					transaction.rollback();
				} catch (Exception rollbackException) {
					logger.warn("Failed to rollback transaction after error", rollbackException);
				}
			}
			logger.error("Failed to store hyperlinks/update status for urlid: {}", urlid, e);
		}
	}

	static boolean doesMd5Exist(String md5sum) {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			String hql = "SELECT count(u.md5sum) FROM Url u WHERE u.md5sum = :md5sum";
			Query<Long> q = session.createQuery(hql, Long.class).setParameter("md5sum", md5sum);
			Long count = q.getSingleResult();
			return count != null && count > 0;
		}
	}

	public static void insertHyperLinkStatus(Long urlid, String status) {
		Transaction transaction = null;
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			transaction = session.beginTransaction();
			updateUrlStatus(session, urlid, status);
			if (transaction != null && transaction.isActive()) {
				transaction.commit();
			}
		} catch (Exception e) {
			if (transaction != null && transaction.isActive()) {
				try {
					transaction.rollback();
				} catch (Exception rollbackException) {
					logger.warn("Failed to rollback transaction after error", rollbackException);
				}
			}
			logger.error("Failed to insert hyperlink status for urlid: " + urlid, e);
		}
	}

	static boolean fetchHyperLink(StringBuilder sUrlid, StringBuilder url) {
		Transaction transaction = null;
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			transaction = session.beginTransaction();

			java.sql.Timestamp checkoutCutoff = new java.sql.Timestamp(
					System.currentTimeMillis() - getCheckoutTimeoutMs());
			Set<String> retryStatuses = new HashSet<>(Arrays.asList(
					"visited-error",
					"visited-empty",
					"visited-no-han",
					"visited-invalid-protocol",
					"visited-out-of-scope"));
			String hql = "FROM Url u WHERE (u.lastStatus IS NULL AND u.idurl NOT IN (SELECT s.urlId FROM Status s)) "
					+ "OR u.lastStatus IN (:retryStatuses) "
					+ "OR (u.lastStatus = :checkoutStatus AND (u.lastStatusTs IS NULL OR u.lastStatusTs < :checkoutCutoff))";

			List<Url> urls = session.createQuery(hql, Url.class)
					.setParameterList("retryStatuses", retryStatuses)
					.setParameter("checkoutStatus", STATUS_CHECK_OUT)
					.setParameter("checkoutCutoff", checkoutCutoff)
					.setMaxResults(1)
					.setLockMode("u", LockMode.PESSIMISTIC_WRITE) // Using setLockMode with LockMode.PESSIMISTIC_WRITE
					.list();

			if (urls.isEmpty()) {
				// This is a normal condition when the queue is empty, so changing log level to debug
				logger.debug("No new hyperlinks available in the database to process.");
				if (transaction != null && transaction.isActive()) {
					transaction.commit();
				}
				return false;
			}

			Url nextUrl = urls.get(0);
			sUrlid.append(String.valueOf(nextUrl.getIdUrl()));
			url.append(nextUrl.getUrl());

			logger.info("Fetched urlid: {} url: {}", nextUrl.getIdUrl(), nextUrl.getUrl());

			updateUrlStatus(session, nextUrl, STATUS_CHECK_OUT);
			if (transaction != null && transaction.isActive()) {
				transaction.commit();
			}

			logger.info("Checked-out urlid: {}", nextUrl.getIdUrl());

			return true;
		} catch (Exception e) {
			if (transaction != null && transaction.isActive()) {
				try {
					transaction.rollback();
				} catch (Exception rollbackException) {
					logger.warn("Failed to rollback transaction after error", rollbackException);
				}
			}
			logger.error("Failed to fetch hyperlink", e);
			return false;
		}
	}

	static void insertCrawlResult(String result) {
		insertResult(result);
	}

	private static void storeHyperLinksInSession(Session session, Set<String> hyperLinks) {
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

		Map<String, String> md5ToUrl = new java.util.HashMap<>();
		for (String hyperLink : hyperLinks) {
			m.update(hyperLink.getBytes(), 0, hyperLink.length());
			String md5string = new BigInteger(1, m.digest()).toString(16);
			md5ToUrl.put(md5string, hyperLink);
		}

		boolean isPostgres = isPostgres();
		String sql;
		if (isPostgres) {
			sql = "INSERT INTO url (md5sum, mtimestamp, mtime, url) "
					+ "VALUES (:md5sum, :ts, :ts, :url) "
					+ "ON CONFLICT (md5sum) DO NOTHING";
		} else {
			sql = "INSERT INTO url (md5sum, mtimestamp, mtime, url) "
					+ "VALUES (:md5sum, :ts, :ts, :url) "
					+ "ON DUPLICATE KEY UPDATE md5sum = md5sum";
		}

		int inserted = 0;
		java.sql.Timestamp now = java.sql.Timestamp.from(Instant.now());
		for (Map.Entry<String, String> entry : md5ToUrl.entrySet()) {
			int affected = session.createNativeQuery(sql)
					.setParameter("md5sum", entry.getKey())
					.setParameter("ts", now)
					.setParameter("url", entry.getValue())
					.executeUpdate();
			if (affected > 0) {
				inserted++;
			}
		}
		logger.info("Inserted {} new hyperlinks.", inserted);
	}

	private static void updateUrlStatus(Session session, Long urlid, String status) {
		Url url = session.get(Url.class, urlid);
		if (url == null) {
			logger.warn("Unable to update status. Url not found for urlid: {}", urlid);
			return;
		}
		updateUrlStatus(session, url, status);
	}

	private static void updateUrlStatus(Session session, Url url, String status) {
		java.sql.Timestamp now = java.sql.Timestamp.from(Instant.now());
		url.setLastStatus(status);
		url.setLastStatusTs(now);

		Status st = new Status();
		st.setUrlId(url.getIdUrl());
		st.setStatus(status);
		st.setMTimeStamp(now);
		session.save(st);
	}

	private static long getCheckoutTimeoutMs() {
		String value = System.getenv("CHECKOUT_TIMEOUT_MS");
		if (value == null || value.trim().isEmpty()) {
			return 10 * 60 * 1000L;
		}
		try {
			return Long.parseLong(value.trim());
		} catch (NumberFormatException e) {
			logger.warn("Invalid CHECKOUT_TIMEOUT_MS '{}', using default.", value);
			return 10 * 60 * 1000L;
		}
	}

	private static boolean isPostgres() {
		String conn = System.getenv("DB_CONNECTION_STRING");
		return conn != null && conn.toLowerCase().startsWith("jdbc:postgresql:");
	}
}
