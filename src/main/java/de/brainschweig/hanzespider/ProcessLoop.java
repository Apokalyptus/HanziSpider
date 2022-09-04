package de.brainschweig.hanzespider;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.brainschweig.interfaces.IWebHandler;

public class ProcessLoop implements Runnable {

	static final Logger logger = LogManager.getLogger(ProcessLoop.class.getName());
	private String webHandler = null;

	private String proxyAddr = null;

	private String proxyPort = null;

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

	@Override
	public void run() {

		boolean isRunning = true;

		while (isRunning) {
			try {
				// get hyperLink from database
				StringBuilder url = new StringBuilder();
				StringBuilder sUrlid = new StringBuilder();
				int urlid = 0;

				if (!Database.fetchHyperLink(sUrlid, url))
					continue;

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

				IWebHandler whjs = null;

				if (webHandler == null || webHandler.isBlank()) {
					whjs = new WebHandlerJsoup();
				} else if (webHandler.toLowerCase().equals("JSoup".toLowerCase())) {
					whjs = new WebHandlerJsoup();
				} else if (webHandler.toLowerCase().equals("Selenium".toLowerCase())) {
					whjs = new WebHandlerSelenium();
				}

				try {
					whjs.getWebContent(url.toString(), bodyContent, hyperLinks, proxyAddr, proxyPort);
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
