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
	private Long urlid = null;
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

	private volatile boolean isRunning = true;

	@Override
	public void run() {
		while (isRunning) {
			try {
				// get hyperLink from database
				StringBuilder url = new StringBuilder();
				StringBuilder sUrlid = new StringBuilder();
				Long urlid = 0L;

				Database db = new Database();
				Database.fetchHyperLink(sUrlid, url);

				try {
					urlid = Long.valueOf(sUrlid.toString());
				} catch (NumberFormatException ex) {
					continue;
				}

				if (url.length() == 0) {
					logger.debug("No URL found to process, waiting before next attempt");
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						logger.warn("Sleep interrupted, continuing with next iteration");
						Thread.currentThread().interrupt(); // Preserve interrupt status
					}
					continue;
				}

				Set<String> hyperLinks = new HashSet<>();
				StringBuilder bodyContent = new StringBuilder();

				// get webpage

				IWebHandler whjs;

				switch (webHandler == null ? "" : webHandler.toLowerCase()) {
					case "":
						whjs = new WebHandlerJsoup();
						break;
					case "jsoup":
						whjs = new WebHandlerJsoup();
						break;
					case "selenium":
						whjs = new WebHandlerSelenium();
						break;
					default:
						whjs = null;
						logger.error("Unknown web handler: {}", webHandler);
						throw new IllegalArgumentException("Unknown web handler: " + webHandler);
				}

				try {
					whjs.getWebContent(url.toString(), bodyContent, hyperLinks, proxyAddr, proxyPort);
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

				switch (outputHandler == null ? "" : outputHandler.toLowerCase()) {
					case "":
						oh = new OutputHandlerDatabase();
						break;
					case "mysqldatabase":
						oh = new OutputHandlerDatabase();
						break;
					case "file":
						oh = new OutputHandlerFile();
						break;
					default:
						logger.error("Unknown output handler: {}", outputHandler);
						throw new IllegalArgumentException("Unknown output handler: " + outputHandler);
				}

				oh.addToBuffer(bodyContent.toString());

			} catch (Exception ex) {
				logger.error("Found unhandled exception: ", ex);
			}

		}

	}

}
