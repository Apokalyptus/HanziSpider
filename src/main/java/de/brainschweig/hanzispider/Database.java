package de.brainschweig.hanzispider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Set;

import de.brainschweig.hanzispider.entities.Url;
import de.brainschweig.hanzispider.entities.Status;

public class Database {

	private static final Logger logger = LogManager.getLogger(Database.class.getName());
	private String connectionString = null;

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

	}

	public void insertCrawlResult(String resultChunk){

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

	}

	private boolean doesMd5Exist(String md5sum) {
		return true;

	}

	// synchronized
	public void fetchHyperLink(StringBuilder sUrlid, StringBuilder url) {

	}

	public void insertHyperLinkStatus(int urlid, String status) {

	}
}
