package de.brainschweig.hanzespider;

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
				if (sUrlid.toString().isEmpty()) {
					isRunning = false;
					continue;
				}
				urlid = Integer.parseInt(sUrlid.toString());

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
					// WebHandlerJsoup whjs = new WebHandlerJsoup();
					// whjs.getWebContent(url.toString(), bodyContent, hyperLinks);
					WebHandlerSelenium whjs = new WebHandlerSelenium();
					whjs.getWebContent(url.toString(), bodyContent, hyperLinks);
				} catch (IOException e) {
					logger.error("Fetching web content from " + url + " went wrong: ", e);
					Database.insertHyperLinkStatus(urlid, "visited-error");
					continue;
				} catch (NullPointerException npe) {
					logger.error("NullPointerException:", npe);
				}
				// check hyperlinks against some rules
				for (String hyperLink : hyperLinks) {
					System.out.println("LINK2: " + (null == hyperLink ? "Nothing" : hyperLink));
				}

				HyperLinkProcessor.cleanUpHyperLinks(hyperLinks);

				for (String hyperLink : hyperLinks) {
					System.out.println("LINK3: " + (null == hyperLink ? "Nothing" : hyperLink));
				}

				// save hyperLinks to DB
				Database.insertHyperLinks(hyperLinks);

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
				OutputHandlerDatabase of = new OutputHandlerDatabase();
				of.addToBuffer(bodyContent.toString());

			} catch (Exception ex) {
				logger.error("Found unhandled exception: ", ex);
			}

		}

	}

}
