package de.brainschweig.hanzispider;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProcessLoop implements Runnable {

	static final Logger logger = LogManager.getLogger(ProcessLoop.class.getName());

	public ProcessLoop() {

	}

	@Override
	public void run() {

		boolean isRunning = true;

		while (isRunning) {
			try {
				// get hyperLink from database
				StringBuilder url = new StringBuilder();
				StringBuilder sUrlid = new StringBuilder();
				int urlid = 0;

				Database.fetchHyperLink(sUrlid, url);
				try {
					urlid = Integer.parseInt(sUrlid.toString());
				} catch (NumberFormatException ex) {
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

				Set<String> hyperLinks = new HashSet<String>();
				StringBuilder bodyContent = new StringBuilder();

				// get webpage
				try {
					WebHandler.getWebContent(url.toString(), bodyContent, hyperLinks);
				} catch (IOException e) {
					logger.error("Fetching web content from " + url + " went wrong: ", e);
					Database.insertHyperLinkStatus(urlid, "visited-error");
					continue;
				}
				// check hyperlinks against some rules
				HyperLinkProcessor.cleanUpHyperLinks(hyperLinks);

				// save hyperLinks to DB
				Database.storeHyperLinks(hyperLinks);

				// process Text
				if (bodyContent.length() == 0) {
					logger.info("bodyContent length zero. Skip!");
					continue;
				}
				bodyContent = TextProcessor.processText(bodyContent);

				// Write Status to DB
				Database.insertHyperLinkStatus(urlid, "visited-ok");

				// write to file
				logger.info("hyperlinks: " + hyperLinks.size() + " BodyContent: " + bodyContent.length());
				OutputFileHandler.add(bodyContent.toString());

			} catch (Exception ex) {
				logger.error("Found unhandled exception: ", ex);
			}
		
			
		}

	}

}
