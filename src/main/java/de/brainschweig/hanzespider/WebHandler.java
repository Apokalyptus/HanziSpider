package de.brainschweig.hanzespider;

import java.io.IOException;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.security.KeyManagementException;

import java.security.NoSuchAlgorithmException;

import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class WebHandler {
	
	static final Logger logger = LogManager.getLogger(WebHandler.class.getName());

	public static void getWebContent(String url, StringBuilder bodyContent, Set<String> hyperLinks) throws IOException {

		Document doc = Jsoup.connect(url).timeout(15 * 1000)
				.userAgent(
						"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2")
				.followRedirects(true)
				.proxy("10.0.2.15", 3128)
				.sslSocketFactory(WebHandler.socketFactory())
				.get();

		Elements links = doc.select("a[href]");

		if(null != doc.body()){
			bodyContent.append(doc.body().text());
			logger.debug("BODY:" + doc.body().text());
		}
		else {
			logger.debug("Web-Document not valid!!!");
		}

		// System.out.printf("\nLinks: (%d)", links.size());
		links.forEach((link) -> {
			hyperLinks.add(link.attr("abs:href").toString());
		});
	}

	static private SSLSocketFactory socketFactory() {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }};

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
