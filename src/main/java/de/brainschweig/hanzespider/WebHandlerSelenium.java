package de.brainschweig.hanzespider;

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

import de.brainschweig.interfaces.IWebHandler;

public class WebHandlerSelenium implements IWebHandler {

	static final Logger logger = LogManager.getLogger(WebHandlerSelenium.class.getName());
	// private static ChromeDriverService service;
	// private WebDriver driver = null;

	private static final String name = "Selenium";

	public String getName() {
		return name;
	}

	public void getWebContent(String url, StringBuilder bodyContent, Set<String> hyperLinks, String proxyAddr,
			String proxyPort) throws IOException {

		// service = new ChromeDriverService.Builder().usingDriverExecutable(new
		// File("/usr/bin/chromedriver"))
		// .usingAnyFreePort().build();
		// service.start();
		FirefoxBinary firefoxBinary = new FirefoxBinary();
		firefoxBinary.addCommandLineOptions("--headless");

		System.setProperty("webdriver.gecko.driver", "/usr/bin/geckodriver");

		FirefoxOptions firefoxOptions = new FirefoxOptions();

		Proxy proxy = new Proxy();

		if (proxyAddr != null && !proxyAddr.isBlank() && proxyPort != null && !proxyPort.isBlank()) {
			proxy.setHttpProxy(proxyAddr+":"+proxyPort);
			proxy.setSslProxy(proxyAddr+":"+proxyPort);
			firefoxOptions.setCapability("proxy", proxy);
		}

		firefoxOptions.setBinary(firefoxBinary);
		WebDriver driver = new FirefoxDriver(firefoxOptions);

		logger.error("Start calling the URL: " + url + "\n");

		try {
			driver.get(url);
		} catch (NullPointerException e) {
			logger.error("cought it", e);
		}

		System.out.println("BODY:" + driver.findElement(By.tagName("body")).getText());
		bodyContent.append(driver.findElement(By.tagName("body")).getText());
		if (null == bodyContent || bodyContent.length() == 0) {
			logger.error("Web-Document not valid!!!");
		}

		List<WebElement> links = driver.findElements(By.tagName("a"));
		System.out.println("amount of Links: " + links.size());

		links.forEach((link) -> {
			hyperLinks.add(link.getAttribute("href"));
			// System.out.println("Link: " + link.getAttribute("href"));
		});
		for (String hyperLink : hyperLinks) {
			System.out.println("LINK: " + (null == hyperLink ? "Nothing" : hyperLink));
		}

		driver.close();
	}

}