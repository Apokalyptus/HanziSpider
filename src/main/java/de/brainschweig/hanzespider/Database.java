package de.brainschweig.hanzespider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Set;

class Database {

	private static final Logger logger = LogManager.getLogger(Database.class.getName());
	private static Connection conn = null;

	static void loadDriver() {
		try {
			// The newInstance() call is a work around for some
			// broken Java implementations

			//Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
		} catch (Exception ex) {
			logger.error("Could not load jdbc.Driver!!" + ex.getMessage());
			System.exit(-1);

		}

		logger.info("jdbc.Driver loaded sucessfully");
	}

	static void connect(String connectionString) {
		try {
			// jdbc:mysql://localhost/crawler?user=crawler&password=crawlerX
			conn = DriverManager.getConnection(connectionString);

		} catch (SQLException ex) {

			logger.error("Connect the Database went wrong: ", ex);
			System.exit(-1);
		}
	}

	static void insertHyperLinks(Set<String> hyperLinks) {
		PreparedStatement insertUrl = null;
		String insertStatement = "INSERT INTO `crawler`.`url` ( `url`, `md5sum`, `mtimestamp`, `mtime`) VALUES ( ?, ?, NOW(), NOW());";
		try {

			conn.setAutoCommit(false);

			insertUrl = conn.prepareStatement(insertStatement);
			MessageDigest m = MessageDigest.getInstance("MD5");

			for (String hyperLink : hyperLinks) {

				m.update(hyperLink.getBytes(), 0, hyperLink.length());
				String md5string = new BigInteger(1, m.digest()).toString(16);

				if (doesMd5Exist(md5string))
					continue;

				insertUrl.setString(1, hyperLink);
				insertUrl.setString(2, md5string);
				insertUrl.executeUpdate();

				conn.commit();

				logger.info("Inserted: Hyperlink: " + hyperLink + " MD5:" + md5string);
			}

		} catch (SQLException e) {
			logger.error("Executing Query went wrong: ", e);
			try {
				conn.rollback();
			} catch (SQLException e1) {
				logger.error("Rollback went wrong: ", e);
				e1.printStackTrace();
			} finally {
				try {
					if (insertUrl != null) {
						insertUrl.close();
					}
				} catch (SQLException e1) {
					logger.error("Close of PreparedStatement went wrong: ", e1);
				}
			}

		} catch (NoSuchAlgorithmException e) {
			logger.error("No Such Algorithm:", e);
		}
	}

	private static boolean doesMd5Exist(String md5sum) {

		PreparedStatement ps = null;
		ResultSet rs;
		int md5count = 0;
		try {

			String selectStatement = "SELECT count(md5sum) md5count FROM url WHERE md5sum = ?";
			ps = conn.prepareStatement(selectStatement);
			ps.setString(1, md5sum);

			rs = ps.executeQuery();
			rs.next();
			md5count = rs.getInt("md5count");

			rs.close();
			ps.close();

		} catch (SQLException e) {
			logger.error("Executing Query went wrong: ", e);
		}

		return md5count > 0;
	}

	// synchronized
	static boolean fetchHyperLink(StringBuilder sUrlid, StringBuilder url) {
		PreparedStatement insertStatus = null;
		Statement stmt;
		ResultSet rs;
		int urlid = -1;

		String selectStatement = "SELECT u.idurl, u.url, s.status FROM url u LEFT JOIN status s ON (u.idurl = s.url_idurl) WHERE s.status IS NULL LIMIT 1;";
		String insertStatement = "INSERT INTO `crawler`.`status` ( `url_idurl`, `status`, `mtimestamp`, `mtime`) VALUES (?, ?, NOW(), NOW());";

		try {
			conn.setAutoCommit(false);

			stmt = conn.createStatement();

			rs = stmt.executeQuery(selectStatement);
			if (!rs.next()) {
				logger.error("Got no ResultSet from Database - No HyperLinks without status available.");
				return false;
			} else {
				logger.debug("got Resultset from Database - Found Hyperlinks without status");
			}
		
			urlid = rs.getInt("idurl");
			url.append(rs.getString("url"));
			rs.close();

			logger.info("Fetch urlid: " + urlid + " url: " + url);

			insertStatus = conn.prepareStatement(insertStatement);
			insertStatus.setInt(1, urlid);
			insertStatus.setString(2, "check-out");
			insertStatus.executeUpdate();
			conn.commit();

			rs.close();
			insertStatus.close();

			logger.info("Insert Status urlid: " + urlid + " Status: check-out");
			sUrlid.append(String.valueOf(urlid));

		} catch (SQLException e) {
			logger.error("Executing Query went wrong:", e);

		}
		return true;

	}

	static void insertHyperLinkStatus(int urlid, String status) {
		PreparedStatement insertUrl = null;
		String insertStatement = "INSERT INTO `crawler`.`status` ( `url_idurl`, `status`, `mtimestamp`, `mtime`) VALUES (?, ?, NOW(), NOW());";
		try {

			conn.setAutoCommit(false);

			insertUrl = conn.prepareStatement(insertStatement);
			insertUrl.setInt(1, urlid);
			insertUrl.setString(2, status);
			insertUrl.executeUpdate();
			conn.commit();

			insertUrl.close();

			logger.info("Insert Status urlid: " + urlid + " Status: " + status);

		} catch (SQLException e) {
			logger.error("Executing Query went wrong:", e);
			try {
				conn.rollback();
			} catch (SQLException e1) {
				logger.error("Rollback went wrong", e1);

			}
		}
	}

	static void insertCrawlResult(String result) {
		PreparedStatement insertResult = null;
		String insertStatement = "INSERT INTO `crawler`.`results` ( `hanze` ) VALUES (?);";
		try {

			conn.setAutoCommit(false);

			insertResult = conn.prepareStatement(insertStatement);
		
			insertResult.setString(1, result);
			insertResult.executeUpdate();
			conn.commit();

			insertResult.close();

			logger.info("Insert Result: " + result);

		} catch (SQLException e) {
			logger.error("Executing Query went wrong:", e);
			try {
				conn.rollback();
			} catch (SQLException e1) {
				logger.error("Rollback went wrong", e1);

			}
		}
	}
}
