package de.brainschweig.hanzispider;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProcessLoop implements Runnable {

	static final Logger logger = LogManager.getLogger(ProcessLoop.class.getName());

	@Override
	public void run() {

		boolean isRunning = true;

		while (isRunning) {
			try {
				// get hyperLink from database
				StringBuilder url = new StringBuilder();
				StringBuilder sUrlid = new StringBuilder();
				int urlid = 0;

				Database db = new Database();
				db.fetchHyperLink(sUrlid, url);

				try {
					urlid = Integer.parseInt(sUrlid.toString());
				} catch (NumberFormatException ex) {
<<<<<<< HEAD
					continue;
				}

				if (url.length() == 0) {
					Thread.sleep(5000);
					continue;
				}

=======
					continue;
				}

				if (url.length() == 0) {
					logger.debug("Found URL with length == 0");
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						logger.error("Sleeping 1000ms went wrong", e);
					}
					continue;
				}

>>>>>>> c18b80e (Merged Hibernate and Modular)
				Set<String> hyperLinks = new HashSet<>();
				StringBuilder bodyContent = new StringBuilder();

				// get webpage
<<<<<<< HEAD
=======

				IWebHandler whjs = null;

				if (webHandler == null || webHandler.isEmpty()) {
					whjs = new WebHandlerJsoup();
				} else if (webHandler.toLowerCase().equals("JSoup".toLowerCase())) {
					whjs = new WebHandlerJsoup();
				} else if (webHandler.toLowerCase().equals("Selenium".toLowerCase())) {
					whjs = new WebHandlerSelenium();
				}

>>>>>>> c18b80e (Merged Hibernate and Modular)
				try {
					WebHandler.getWebContent(url.toString(), bodyContent, hyperLinks);
				} catch (IOException e) {
<<<<<<< HEAD
					logger.error("Fetching web content from {} }went wrong: {}", url, e);
					db.insertHyperLinkStatus(urlid, "visited-error");
=======
					logger.error("Fetching web content from {} went wrong: {}", url, e);
					Database.insertHyperLinkStatus(urlid, "visited-error");
>>>>>>> c18b80e (Merged Hibernate and Modular)
					continue;
				}
				// check hyperlinks against some rules
				HyperLinkProcessor.cleanUpHyperLinks(hyperLinks);

				// save hyperLinks to DB
				db.storeHyperLinks(hyperLinks);

				// process Text
				if (bodyContent.length() == 0) {
					logger.info("bodyContent length zero. Skip!");
					continue;
				}
				bodyContent = TextProcessor.processText(bodyContent);

				// Write Status to DB
				db.insertHyperLinkStatus(urlid, "visited-ok");

				// write to file
<<<<<<< HEAD
				logger.info("hyperlinks: {}} BodyContent: {}", hyperLinks.size(), bodyContent.length());
				OutputFileHandler.add(bodyContent.toString());
=======
				logger.info("hyperlinks: {} BodyContent: {}", hyperLinks.size(), bodyContent.length());

				IOutputHandler oh = null;

				if (outputHandler == null || outputHandler.isEmpty()) {
					oh = new OutputHandlerDatabase();
				} else if (outputHandler.toLowerCase().equals("MySQLDatabase".toLowerCase())) {
					oh = new OutputHandlerDatabase();
				} else if (outputHandler.toLowerCase().equals("File".toLowerCase())) {
					oh = new OutputHandlerFile();
				}

				oh.addToBuffer(bodyContent.toString());
>>>>>>> c18b80e (Merged Hibernate and Modular)

			} catch (Exception ex) {
				logger.error("Found unhandled exception: ", ex);
			}

		}

	}

}
