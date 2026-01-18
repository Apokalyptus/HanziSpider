package de.brainschweig.hanzispider;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.brainschweig.hanzispider.interfaces.*;

public class ProcessLoop implements Runnable {

	static final Logger logger = LogManager.getLogger(ProcessLoop.class.getName());

	private String webHandlerName = null;
	private String outputHandler = null;
	private String proxyAddr = null;
	private String proxyPort = null;
	private final String singleSpiderUrl;
	private final URI singleSpiderUri;
	private final String singleSpiderPath;
	private final String singleSpiderPathWithSlash;
	
	private IWebHandler webHandlerInstance;

	public ProcessLoop() {
		this(null, null, null, null);
	}

	public ProcessLoop(String webHandler) {
		this(webHandler, null, null, null);
	}

	public ProcessLoop(String webHandler, String proxyAddr, String proxyPort, String singleSpiderUrl) {
		this.webHandlerName = webHandler;
		this.proxyAddr = proxyAddr;
		this.proxyPort = proxyPort;
		this.singleSpiderUrl = normalizeSingleSpiderUrl(singleSpiderUrl);
		URI baseUri = null;
		String basePath = null;
		String basePathWithSlash = null;
		if (this.singleSpiderUrl != null) {
			try {
				baseUri = new URI(this.singleSpiderUrl);
				if (baseUri.getScheme() == null || baseUri.getHost() == null) {
					logger.warn("SINGLE_SPIDER_URL '{}' is missing scheme or host. Ignoring single-URL scope.",
							this.singleSpiderUrl);
					baseUri = null;
				}
			} catch (URISyntaxException e) {
				logger.warn("Invalid SINGLE_SPIDER_URL '{}'. Ignoring single-URL scope.", this.singleSpiderUrl);
				baseUri = null;
			}
			if (baseUri != null) {
				basePath = baseUri.getPath();
				if (basePath == null || basePath.isEmpty()) {
					basePath = "/";
				}
				basePathWithSlash = basePath.endsWith("/") ? basePath : basePath + "/";
			}
		}
		this.singleSpiderUri = baseUri;
		this.singleSpiderPath = basePath;
		this.singleSpiderPathWithSlash = basePathWithSlash;
		initWebHandler();
	}
	
	private void initWebHandler() {
		switch (webHandlerName == null ? "" : webHandlerName.toLowerCase()) {
			case "":
			case "jsoup":
				this.webHandlerInstance = new WebHandlerJsoup();
				break;
			case "selenium":
				this.webHandlerInstance = new WebHandlerSelenium();
				break;
			default:
				throw new IllegalArgumentException("Unknown web handler: " + webHandlerName);
		}
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
		this.webHandlerName = webHandler;
		// Re-init if changed at runtime (unlikely but safe)
		if (this.webHandlerInstance != null) {
			this.webHandlerInstance.close();
		}
		initWebHandler();
	}
	
	public void shutdown() {
		if (this.webHandlerInstance != null) {
			this.webHandlerInstance.close();
		}
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

			String urlStr = url.toString().trim();
			String lowerUrl = urlStr.toLowerCase();
			if (!lowerUrl.startsWith("http://") && !lowerUrl.startsWith("https://")) {
				logger.warn("Skipping URL with unsupported protocol: urlid={}, url={}", urlid, urlStr);
				Database.insertHyperLinkStatus(urlid, "visited-invalid-protocol");
				return;
			}
			if (!isUrlInScope(urlStr)) {
				logger.info("Skipping URL outside SINGLE_SPIDER_URL scope: urlid={}, url={}", urlid, urlStr);
				Database.insertHyperLinkStatus(urlid, "visited-out-of-scope");
				return;
			}

			try {
				webHandlerInstance.getWebContent(urlStr, bodyContent, hyperLinks, proxyAddr, proxyPort);
			} catch (IOException e) {
				logger.error("Fetching web content from {} went wrong: {}", url, e);
				Database.insertHyperLinkStatus(urlid, "visited-error");
				return;
			}
			
			HyperLinkProcessor.cleanUpHyperLinks(hyperLinks);
			filterHyperLinksInScope(hyperLinks);

			if (bodyContent.length() == 0) {
				logger.info("bodyContent length zero for urlid {}. Skip!", urlid);
				Database.storeHyperLinksAndUpdateStatus(urlid, hyperLinks, "visited-empty");
				return;
			}
			
			bodyContent = TextProcessor.processText(bodyContent);
			if (bodyContent == null) {
				logger.info("No HAN characters found for urlid {}. Skip!", urlid);
				Database.storeHyperLinksAndUpdateStatus(urlid, hyperLinks, "visited-no-han");
				return;
			}
			Database.storeHyperLinksAndUpdateStatus(urlid, hyperLinks, "visited-ok");
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

	private void filterHyperLinksInScope(Set<String> hyperLinks) {
		if (singleSpiderUri == null || hyperLinks == null || hyperLinks.isEmpty()) {
			return;
		}
		hyperLinks.removeIf(link -> !isUrlInScope(link));
	}

	private boolean isUrlInScope(String url) {
		if (singleSpiderUri == null) {
			return true;
		}
		if (url == null || url.isEmpty()) {
			return false;
		}
		URI candidateUri;
		try {
			candidateUri = new URI(url);
		} catch (URISyntaxException e) {
			return false;
		}
		if (!equalsIgnoreCase(singleSpiderUri.getScheme(), candidateUri.getScheme())) {
			return false;
		}
		if (!equalsIgnoreCase(singleSpiderUri.getHost(), candidateUri.getHost())) {
			return false;
		}
		if (resolvePort(singleSpiderUri) != resolvePort(candidateUri)) {
			return false;
		}
		String candidatePath = candidateUri.getPath();
		if (candidatePath == null || candidatePath.isEmpty()) {
			candidatePath = "/";
		}
		return candidatePath.equals(singleSpiderPath) || candidatePath.startsWith(singleSpiderPathWithSlash);
	}

	private static int resolvePort(URI uri) {
		if (uri == null) {
			return -1;
		}
		int port = uri.getPort();
		if (port != -1) {
			return port;
		}
		String scheme = uri.getScheme();
		if ("http".equalsIgnoreCase(scheme)) {
			return 80;
		}
		if ("https".equalsIgnoreCase(scheme)) {
			return 443;
		}
		return -1;
	}

	private static boolean equalsIgnoreCase(String a, String b) {
		if (a == null || b == null) {
			return false;
		}
		return a.equalsIgnoreCase(b);
	}

	private static String normalizeSingleSpiderUrl(String singleSpiderUrl) {
		if (singleSpiderUrl == null) {
			return null;
		}
		String trimmed = singleSpiderUrl.trim();
		if (trimmed.isEmpty()) {
			return null;
		}
		return trimmed;
	}
}
