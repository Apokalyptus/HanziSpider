package de.brainschweig.hanzispider;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import de.brainschweig.interfaces.IWebHandler;

public class WebHandlerJsoup implements IWebHandler {

	static final Logger logger = LogManager.getLogger(WebHandlerJsoup.class.getName());

	private static final String name = "JSoup";

	public String getName() {
		return name;
	}

	public void getWebContent(String url, StringBuilder bodyContent, Set<String> hyperLinks, String proxyAddr,
			String proxyPort) throws IOException {

		Document doc = null;

		if (proxyAddr == null || proxyAddr.isBlank() || proxyPort == null || proxyPort.isBlank()) {
			doc = Jsoup.connect(url).timeout(15 * 1000)
					.userAgent(
							"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2")
					.followRedirects(true)
					.sslSocketFactory(WebHandlerJsoup.socketFactory())
					.get();

		} else {
			final int pp = Integer.parseInt(proxyPort);
			doc = Jsoup.connect(url).timeout(15 * 1000)
					.userAgent(
							"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2")
					.followRedirects(true)
					.proxy(proxyAddr, pp)
					.sslSocketFactory(WebHandlerJsoup.socketFactory())
					.get();

		}

		Elements links = doc.select("a[href]");

		if (null != doc.body()) {
			bodyContent.append(doc.body().text());
			logger.debug("BODY:" + doc.body().text());
		} else {
			logger.debug("Web-Document not valid!!!");
		}

		links.forEach((link) -> {
			hyperLinks.add(link.attr("abs:href").toString());
		});
	}

	static private SSLSocketFactory socketFactory() {
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}

			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}
		} };

		try {
			SSLContext sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
			SSLSocketFactory result = sslContext.getSocketFactory();

			return result;
		} catch (NoSuchAlgorithmException | KeyManagementException e) {
			throw new RuntimeException("Failed to create a SSL socket factory", e);
		}
	}

}
