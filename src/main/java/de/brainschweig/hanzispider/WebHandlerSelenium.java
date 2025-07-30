package de.brainschweig.hanzispider;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import de.brainschweig.hanzispider.interfaces.IWebHandler;

public class WebHandlerSelenium implements IWebHandler {

	static final Logger logger = LogManager.getLogger(WebHandlerSelenium.class.getName());

	private static final String NAME = "Selenium";

	public String getName() {
		return NAME;
	}

	public void getWebContent(String url, StringBuilder bodyContent, Set<String> hyperLinks, String proxyAddr,
			String proxyPort) throws IOException {

		FirefoxBinary firefoxBinary = new FirefoxBinary();
		firefoxBinary.addCommandLineOptions("--headless");

		System.setProperty("webdriver.gecko.driver", "/usr/bin/geckodriver");

		FirefoxOptions firefoxOptions = new FirefoxOptions();

		Proxy proxy = new Proxy();

		if (proxyAddr != null && !proxyAddr.isEmpty() && proxyPort != null && !proxyPort.isEmpty()) {
			proxy.setHttpProxy(proxyAddr + ":" + proxyPort);
			proxy.setSslProxy(proxyAddr + ":" + proxyPort);
			firefoxOptions.setCapability("proxy", proxy);
		}

		firefoxOptions.setBinary(firefoxBinary);
		WebDriver driver = null;
		try {
			driver = new FirefoxDriver(firefoxOptions);
			logger.debug("Starting to fetch URL: {}", url);
			
			driver.get(url);
			
			WebElement body = driver.findElement(By.tagName("body"));
			if (body != null) {
				bodyContent.append(body.getText());
				if (bodyContent.length() == 0) {
					logger.warn("Web-Document body is empty");
				}
			} else {
				logger.error("No body element found in document");
			}
			
			List<WebElement> links = driver.findElements(By.tagName("a"));
			links.forEach(link -> {
				String href = link.getAttribute("href");
				if (href != null) {
					hyperLinks.add(href);
				}
			});
			
		} catch (Exception e) {
			logger.error("Error while processing URL {}: {}", url, e.getMessage(), e);
			throw new IOException("Failed to fetch web content", e);
		} finally {
			if (driver != null) {
				try {
					driver.quit();
				} catch (Exception e) {
					logger.warn("Error while closing WebDriver: {}", e.getMessage());
				}
			}
		}
	}

}
