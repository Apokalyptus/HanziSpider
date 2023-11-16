package de.brainschweig.hanzispider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.fasterxml.classmate.AnnotationConfiguration;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
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
		if (connectionString == null || connectionString.isBlank()) {
			System.out.println("ERROR: Environement Variable DB_CONNECTION_STRING is empty.");
			logger.error("Environement Variable DB_CONNECTION_STRING is empty.");
			System.exit(-1);
		}
		this.connectionString = connectionString;
	}

	public String getConnectionString() {
		return this.connectionString;
	}

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

	private boolean doesMd5Exist(String md5sum) {
		String hql = "SELECT count(md5sum) as md5count FROM Url WHERE md5sum = :md5sum";
		Query q = session.createQuery(hql).setParameter("md5sum", md5sum);
		List<Long> list = q.getResultList();

		return list.get(0) > 0;

	}


	// synchronize.f
	public void fetchHyperLink(StringBuilder sUrlid, StringBuilder url) {

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
}
