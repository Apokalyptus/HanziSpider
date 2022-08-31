package de.brainschweig.hanzespider;

import java.io.IOException;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class WebHandler {
	
	static final Logger logger = LogManager.getLogger(WebHandler.class.getName());

	public static void getWebContent(String url, StringBuilder bodyContent, Set<String> hyperLinks) throws IOException {

		Document doc = Jsoup.connect(url).timeout(15 * 1000)
				.userAgent(
						"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2")
				.followRedirects(true).get();

		Elements links = doc.select("a[href]");

		if(null != doc.body())
			bodyContent.append(doc.body().text());
		else
			logger.debug("Web-Document not valid!!!");

		// System.out.printf("\nLinks: (%d)", links.size());
		links.forEach((link) -> {
			hyperLinks.add(link.attr("abs:href").toString());
		});
	}

}
