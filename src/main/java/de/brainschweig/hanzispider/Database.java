package de.brainschweig.hanzispider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import de.brainschweig.hanzispider.entities.Url;
import de.brainschweig.hanzispider.entities.Result;
import de.brainschweig.hanzispider.entities.Status;

public class Database {

	private static final Logger logger = LogManager.getLogger(Database.class.getName());
	private String connectionString = null;
	private Session session = null;

	public Database() {
		setConnectionString(System.getenv("DB_CONNECTION_STRING"));
		doConfiguration();
	}

	public Database(String connectionString) {
		setConnectionString(connectionString);
		doConfiguration();
	}

	private void doConfiguration() {
		Configuration configuration = new Configuration();
		// configuration.configure("hibernate.cfg.xml");
		configuration.addAnnotatedClass(Url.class);
		configuration.addAnnotatedClass(Status.class);
		configuration.addAnnotatedClass(Result.class);
		configuration.setProperty("hibernate.connection.url", getConnectionString());
		configuration.setProperty("hibernate.hbm2ddl.auto", "create");

		SessionFactory sessionFactory = configuration.buildSessionFactory();

		session = sessionFactory.openSession();

	}

	public void insertResult(String result){
		
		Result cr = new Result();
		cr.setHanzi(result);

		session.beginTransaction();
		session.save(cr);
		session.getTransaction().commit();

	}

	public void setConnectionString(String connectionString) {
		if (connectionString == null || connectionString.isEmpty()) {
			System.out.println("ERROR: Environement Variable DB_CONNECTION_STRING is empty.");
			logger.error("Environement Variable DB_CONNECTION_STRING is empty.");
			System.exit(-1);
		}
		this.connectionString = connectionString;
	}

	public String getConnectionString() {
		return this.connectionString;
	}

	@SuppressWarnings("null")
	public void storeHyperLinks(Set<String> hyperLinks) {
		MessageDigest m = null;
		try{
			m = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException nsae){
			logger.error("what the heck");
		}
		
		for (String hyperLink : hyperLinks) {
			m.update(hyperLink.getBytes(), 0, hyperLink.length());
			String md5string = new BigInteger(1, m.digest()).toString(16);

			if (doesMd5Exist(md5string))
					continue;

			Url ul = new Url();
			ul.setUrl(hyperLink);
			ul.setMd5Sum(md5string);
			ul.setMTimeStamp(java.sql.Timestamp.from(Instant.now()));

			session.beginTransaction();
			session.save(ul);
			session.getTransaction().commit();

			logger.info("Inserted: Hyperlink: {} MD%: {}", hyperLink, md5string);
		}

	}

	
	boolean doesMd5Exist(String md5sum) {
		String hql = "SELECT count(md5sum) as md5count FROM Url WHERE md5sum = :md5sum";
		Query q = session.createQuery(hql).setParameter("md5sum", md5sum);
		@SuppressWarnings("unchecked")
		List<Long> list = q.getResultList();


		return list.get(0) > 0;

	}


	public void insertHyperLinkStatus(int urlid, String status) {
		Status st = new Status();
		st.setUrlId(urlid);
		st.setStatus(status);
		st.setMTimeStamp(java.sql.Timestamp.from(Instant.now()));
		
		session.beginTransaction();
		session.save(st);
		session.getTransaction().commit();
	}


	// synchronized
	static synchronized boolean fetchHyperLink(StringBuilder sUrlid, StringBuilder url) {
		// PreparedStatement insertStatus = null;
		// Statement stmt;
		// ResultSet rs;
		// int urlid = -1;

		// String selectStatement = "SELECT u.idurl, u.url, s.status FROM url u LEFT JOIN status s ON (u.idurl = s.url_idurl) WHERE s.status IS NULL LIMIT 1;";
		// String insertStatement = "INSERT INTO `crawler`.`status` ( `url_idurl`, `status`, `mtimestamp`, `mtime`) VALUES (?, ?, NOW(), NOW());";

		// try {
		// 	conn.setAutoCommit(false);

		// 	stmt = conn.createStatement();

		// 	rs = stmt.executeQuery(selectStatement);
		// 	if (!rs.next()) {
		// 		logger.error("Got no ResultSet from Database - No HyperLinks without status available.");
		// 		return false;
		// 	} else {
		// 		logger.debug("got Resultset from Database - Found Hyperlinks without status");
		// 	}

		// 	urlid = rs.getInt("idurl");
		// 	url.append(rs.getString("url"));
		// 	rs.close();

		// 	logger.info("Fetch urlid: " + urlid + " url: " + url);

		// 	insertStatus = conn.prepareStatement(insertStatement);
		// 	insertStatus.setInt(1, urlid);
		// 	insertStatus.setString(2, "check-out");
		// 	insertStatus.executeUpdate();
		// 	conn.commit();

		// 	rs.close();
		// 	insertStatus.close();

		// 	logger.info("Insert Status urlid: " + urlid + " Status: check-out");
		// 	sUrlid.append(String.valueOf(urlid));

		// } catch (SQLException e) {
		// 	logger.error("Executing Query went wrong:", e);

		// }
		return true;

	}

	//static void insertHyperLinkStatus(int urlid, String status) {
		// PreparedStatement insertUrl = null;
		// String insertStatement = "INSERT INTO `crawler`.`status` ( `url_idurl`, `status`, `mtimestamp`, `mtime`) VALUES (?, ?, NOW(), NOW());";
		// try {

		// 	conn.setAutoCommit(false);

		// 	insertUrl = conn.prepareStatement(insertStatement);
		// 	insertUrl.setInt(1, urlid);
		// 	insertUrl.setString(2, status);
		// 	insertUrl.executeUpdate();
		// 	conn.commit();

		// 	insertUrl.close();

		// 	logger.info("Insert Status urlid: " + urlid + " Status: " + status);

		// } catch (SQLException e) {
		// 	logger.error("Executing Query went wrong:", e);
		// 	try {
		// 		conn.rollback();
		// 	} catch (SQLException e1) {
		// 		logger.error("Rollback went wrong", e1);

		// 	}
		// }
	//}

	static void insertCrawlResult(String result) {
		// PreparedStatement insertResult = null;
		// String insertStatement = "INSERT INTO `crawler`.`results` ( `Hanzi` ) VALUES (?);";
		// try {

		// 	conn.setAutoCommit(false);

		// 	insertResult = conn.prepareStatement(insertStatement);

		// 	insertResult.setString(1, result);
		// 	insertResult.executeUpdate();
		// 	conn.commit();

		// 	insertResult.close();

		// 	logger.info("Insert Result: " + result);

		// } catch (SQLException e) {
		// 	logger.error("Executing Query went wrong:", e);
		// 	try {
		// 		conn.rollback();
		// 	} catch (SQLException e1) {
		// 		logger.error("Rollback went wrong", e1);

		// 	}
		// }
	}
}
