package de.brainschweig.hanzispider;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.By;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import de.brainschweig.hanzispider.interfaces.IWebHandler;

public class WebHandlerSelenium implements IWebHandler {

	static final Logger logger = LogManager.getLogger(WebHandlerSelenium.class.getName());

	private static final String NAME = "Selenium";
	private static final String DEFAULT_IGNORE_SELECTORS = "header,footer,nav,aside,.sidebar,.side,.menu,.breadcrumb,.breadcrumbs,.pagination,.pager,.ads,.ad,.advertisement,.banner,.cookie,.cookie-banner,.cookie-consent,.consent,.modal,.popup,.newsletter,script,style,noscript";
	private WebDriver driver;

	public String getName() {
		return NAME;
	}

	public void getWebContent(String url, StringBuilder bodyContent, Set<String> hyperLinks, String proxyAddr,
			String proxyPort) throws IOException {
		if (url == null || url.isEmpty()) {
			throw new IllegalArgumentException("URL cannot be null or empty");
		}

		// Ensure driver is initialized
		if (driver == null) {
			initializeDriver(proxyAddr, proxyPort);
		}

		try {
			logger.debug("Starting to fetch URL: {}", url);
			try {
				driver.get(url);
			} catch (UnhandledAlertException f) {
				try {
					driver.switchTo().alert().accept();
					logger.info("Accepted unexpected alert for URL: {}", url);
				} catch (Exception ignore) {
					// Alert might have disappeared
				}
			}
			
			List<WebElement> links = driver.findElements(By.tagName("a"));
			links.forEach(link -> {
				String href = link.getAttribute("href");
				if (href != null) {
					hyperLinks.add(href);
				}
			});

			String ignoreSelectors = getEnv("IGNORE_SELECTORS", null);
			if ("default".equalsIgnoreCase(ignoreSelectors)) {
				ignoreSelectors = DEFAULT_IGNORE_SELECTORS;
			}
			if (ignoreSelectors != null && !ignoreSelectors.isEmpty() && driver instanceof JavascriptExecutor) {
				((JavascriptExecutor) driver).executeScript(
						"document.querySelectorAll(arguments[0]).forEach(function(e){e.remove();});",
						ignoreSelectors);
			}

			String contentSelector = getEnv("CONTENT_SELECTOR", null);
			boolean usedContentSelector = false;
			if (contentSelector != null && !contentSelector.isEmpty()) {
				List<WebElement> contentElements = driver.findElements(By.cssSelector(contentSelector));
				if (!contentElements.isEmpty()) {
					usedContentSelector = true;
					for (WebElement element : contentElements) {
						bodyContent.append(element.getText());
					}
				}
			}

			if (!usedContentSelector) {
				WebElement body = driver.findElement(By.tagName("body"));
				if (body != null) {
					bodyContent.append(body.getText());
					if (bodyContent.length() == 0) {
						logger.warn("Web-Document body is empty");
					}
				}
			}
			
		} catch (Exception e) {
			logger.error("Error while processing URL {}: {}", url, e.getMessage(), e);
			// If the driver crashes, we might want to reset it for the next run
			close(); 
			throw new IOException("Failed to fetch web content", e);
		}
	}

	private void initializeDriver(String proxyAddr, String proxyPort) {
		// Removed hardcoded path. Ensure geckodriver is in system PATH or set via -Dwebdriver.gecko.driver
		
		FirefoxOptions firefoxOptions = new FirefoxOptions();
		firefoxOptions.addArguments("--headless");

		if (proxyAddr != null && !proxyAddr.isEmpty() && proxyPort != null && !proxyPort.isEmpty()) {
			Proxy proxy = new Proxy();
			proxy.setHttpProxy(proxyAddr + ":" + proxyPort);
			proxy.setSslProxy(proxyAddr + ":" + proxyPort);
			firefoxOptions.setCapability("proxy", proxy);
		}

		driver = new FirefoxDriver(firefoxOptions);
	}

	@Override
	public void close() {
		if (driver != null) {
			try {
				driver.manage().deleteAllCookies(); // Clean session on close
				driver.quit();
			} catch (Exception e) {
				logger.warn("Error while closing WebDriver: {}", e.getMessage());
			} finally {
				driver = null;
			}
		}
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
