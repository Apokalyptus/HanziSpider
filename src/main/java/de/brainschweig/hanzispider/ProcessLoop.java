package de.brainschweig.hanzispider;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.brainschweig.hanzispider.interfaces.*;

public class ProcessLoop implements Runnable {

	static final Logger logger = LogManager.getLogger(ProcessLoop.class.getName());

	private String webHandler = null;

	private String outputHandler = null;

	private String proxyAddr = null;

	private String proxyPort = null;

	public String getOutputHandler() {
		return outputHandler;
	}

	public void setOutputHandler(String outputHandler) {
		this.outputHandler = outputHandler;
	}

	public String getProxyAddr() {
		return proxyAddr;
	}

	public void setProxyAddr(String proxyAddr) {
		this.proxyAddr = proxyAddr;
	}

	public String getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(String proxyPort) {
		this.proxyPort = proxyPort;
	}

	public String getWebHandler() {
		return webHandler;
	}

	public void setWebHandler(String webHandler) {
		this.webHandler = webHandler;
	}

	public ProcessLoop() {
	}

	public ProcessLoop(String webHandler) {
		this.webHandler = webHandler;
	}

	public ProcessLoop(String webHandler, String proxyAddr, String proxyPort) {
		this.webHandler = webHandler;
		this.proxyAddr = proxyAddr;
		this.proxyPort = proxyPort;
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

				Database db = new Database();
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

				Set<String> hyperLinks = new HashSet<>();
				StringBuilder bodyContent = new StringBuilder();

				// get webpage

				IWebHandler whjs = null;

				if (webHandler == null || webHandler.isEmpty()) {
					whjs = new WebHandlerJsoup();
				} else if (webHandler.toLowerCase().equals("JSoup".toLowerCase())) {
					whjs = new WebHandlerJsoup();
				} else if (webHandler.toLowerCase().equals("Selenium".toLowerCase())) {
					whjs = new WebHandlerSelenium();
				}

				try {
					WebHandler.getWebContent(url.toString(), bodyContent, hyperLinks);
				} catch (IOException e) {
					logger.error("Fetching web content from {} went wrong: {}", url, e);
					db.insertHyperLinkStatus(urlid, "visited-error");
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

			} catch (Exception ex) {
				logger.error("Found unhandled exception: ", ex);
			}

		}

	}

}
