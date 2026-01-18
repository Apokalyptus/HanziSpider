package de.brainschweig.hanzispider;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import de.brainschweig.hanzispider.interfaces.IWebHandler;

public class WebHandlerJsoup implements IWebHandler {

	static final Logger logger = LogManager.getLogger(WebHandlerJsoup.class.getName());

	private static final String NAME = "JSoup";
	private static final String DEFAULT_IGNORE_SELECTORS = "header,footer,nav,aside,.sidebar,.side,.menu,.breadcrumb,.breadcrumbs,.pagination,.pager,.ads,.ad,.advertisement,.banner,.cookie,.cookie-banner,.cookie-consent,.consent,.modal,.popup,.newsletter,script,style,noscript";
	private Map<String, String> cookies = new HashMap<>();

	public String getName() {
		return NAME;
	}

	public void getWebContent(String url, StringBuilder bodyContent, Set<String> hyperLinks, String proxyAddr,
			String proxyPort) throws IOException {
		if (url == null || url.isEmpty()) {
			throw new IllegalArgumentException("URL cannot be null or empty");
		}

		String lowerUrl = url.toLowerCase().trim();
		if (!lowerUrl.startsWith("http://") && !lowerUrl.startsWith("https://")) {
			logger.warn("Skipping invalid URL protocol: {}", url);
			throw new IOException("Unsupported protocol in URL: " + url);
		}

		try {
			Connection connection = Jsoup.connect(url)
					.timeout(15 * 1000)
					.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2")
					.followRedirects(true)
					.cookies(cookies); // Pass existing cookies

			if (proxyAddr != null && !proxyAddr.isEmpty() && proxyPort != null && !proxyPort.isEmpty()) {
				try {
					int pp = Integer.parseInt(proxyPort);
					connection.proxy(proxyAddr, pp);
				} catch (NumberFormatException e) {
					logger.warn("Invalid proxy port: {}", proxyPort);
				}
			}

			Connection.Response response = connection.execute();
			
			// Update cookies with those received in the response
			cookies.putAll(response.cookies());
			
			Document doc = response.parse();

			Elements links = doc.select("a[href]");

			Document contentDoc = doc.clone();
			String ignoreSelectors = getEnv("IGNORE_SELECTORS", null);
			if ("default".equalsIgnoreCase(ignoreSelectors)) {
				ignoreSelectors = DEFAULT_IGNORE_SELECTORS;
			}
			if (ignoreSelectors != null && !ignoreSelectors.isEmpty()) {
				contentDoc.select(ignoreSelectors).remove();
			}

			String contentSelector = getEnv("CONTENT_SELECTOR", null);
			Elements contentElements = null;
			if (contentSelector != null && !contentSelector.isEmpty()) {
				contentElements = contentDoc.select(contentSelector);
			}

			if (contentElements != null && !contentElements.isEmpty()) {
				bodyContent.append(contentElements.text());
			} else if (contentDoc.body() != null) {
				bodyContent.append(contentDoc.body().text());
				logger.debug("BODY:" + contentDoc.body().text());
			}

			links.forEach((link) -> {
				hyperLinks.add(link.attr("abs:href"));
			});
		} catch (IllegalArgumentException e) {
			logger.error("Malformed URL {}: {}", url, e.getMessage());
			throw new IOException("Malformed URL: " + url, e);
		} catch (IOException e) {
			logger.error("Error fetching URL {}: {}", url, e.getMessage());
			throw e;
		} catch (Exception e) {
			logger.error("Unexpected error in Jsoup handler for URL {}: {}", url, e.getMessage());
			throw new IOException("Unexpected error", e);
		}
	}

	@Override
	public void close() {
		cookies.clear();
	}

	private static String getEnv(String name, String defaultValue) {
		String value = System.getenv(name);
		if (value == null || value.trim().isEmpty()) {
			return defaultValue;
		}
		value = value.trim();
		if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
			value = value.substring(1, value.length() - 1);
		} else if (value.startsWith("'") && value.endsWith("'") && value.length() >= 2) {
			value = value.substring(1, value.length() - 1);
		}
		return value;
	}

}
