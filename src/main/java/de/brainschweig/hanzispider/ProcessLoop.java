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

	public ProcessLoop() {
		this(null, null, null);
	}

	public ProcessLoop(String webHandler) {
		this(webHandler, null, null);
	}

	public ProcessLoop(String webHandler, String proxyAddr, String proxyPort) {
		this.webHandler = webHandler;
		this.proxyAddr = proxyAddr;
		this.proxyPort = proxyPort;
	}

	public void setOutputHandler(String outputHandler) {
		this.outputHandler = outputHandler;
	}

	public void setProxyAddr(String proxyAddr) {
		this.proxyAddr = proxyAddr;
	}

	public void setProxyPort(String proxyPort) {
		this.proxyPort = proxyPort;
	}

	public void setWebHandler(String webHandler) {
		this.webHandler = webHandler;
	}

	@Override
	public void run() {
		try {
			logger.debug("Worker thread {} checking for new URL.", Thread.currentThread().getName());
			// get hyperLink from database
			StringBuilder url = new StringBuilder();
			StringBuilder sUrlid = new StringBuilder();

			if (!Database.fetchHyperLink(sUrlid, url)) {
				logger.debug("No URL found to process. Will try again later.");
				return; // Exit the run method. The executor will schedule the next run.
			}

			long urlid;
			try {
				urlid = Long.parseLong(sUrlid.toString());
			} catch (NumberFormatException ex) {
				logger.warn("Invalid urlid retrieved: '{}'. Skipping.", sUrlid);
				return;
			}

			Set<String> hyperLinks = new HashSet<>();
			StringBuilder bodyContent = new StringBuilder();

			IWebHandler whjs;
			switch (webHandler == null ? "" : webHandler.toLowerCase()) {
				case "":
				case "jsoup":
					whjs = new WebHandlerJsoup();
					break;
				case "selenium":
					whjs = new WebHandlerSelenium();
					break;
				default:
					logger.error("Unknown web handler: {}. This task will not be rescheduled.", webHandler);
					// By throwing an exception, we can prevent the executor from rescheduling this task
					// if something is fundamentally broken.
					throw new IllegalArgumentException("Unknown web handler: " + webHandler);
			}

			try {
				whjs.getWebContent(url.toString(), bodyContent, hyperLinks, proxyAddr, proxyPort);
			} catch (IOException e) {
				logger.error("Fetching web content from {} went wrong: {}", url, e);
				Database.insertHyperLinkStatus(urlid, "visited-error");
				return;
			}
			
			HyperLinkProcessor.cleanUpHyperLinks(hyperLinks);
			Database.storeHyperLinks(hyperLinks);

			if (bodyContent.length() == 0) {
				logger.info("bodyContent length zero for urlid {}. Skip!", urlid);
				Database.insertHyperLinkStatus(urlid, "visited-empty");
				return;
			}
			
			bodyContent = TextProcessor.processText(bodyContent);
			Database.insertHyperLinkStatus(urlid, "visited-ok");
			logger.info("Successfully processed urlid: {}, hyperlinks found: {}, BodyContent length: {}", urlid, hyperLinks.size(), bodyContent.length());

			IOutputHandler oh;
			switch (outputHandler == null ? "" : outputHandler.toLowerCase()) {
				case "":
				case "mysqldatabase":
					oh = new OutputHandlerDatabase();
					break;
				case "file":
					oh = new OutputHandlerFile();
					break;
				default:
					logger.error("Unknown output handler: {}. This task will not be rescheduled.", outputHandler);
					throw new IllegalArgumentException("Unknown output handler: " + outputHandler);
			}

			oh.addToBuffer(bodyContent.toString());

		} catch (Exception ex) {
			// Log exceptions to prevent the ScheduledExecutorService from silently swallowing them
			logger.error("Unhandled exception in ProcessLoop, task for thread {} will terminate.", Thread.currentThread().getName(), ex);
			// Re-throwing the exception will prevent the ScheduledExecutorService from re-scheduling the task.
			throw new RuntimeException(ex);
		}
	}
}
