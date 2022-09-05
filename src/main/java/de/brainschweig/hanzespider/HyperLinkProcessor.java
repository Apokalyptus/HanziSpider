package de.brainschweig.hanzespider;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class HyperLinkProcessor {

	public static void cleanUpHyperLinks(Set<String> hyperLinks) {

		Set<String> newHyperLinks = new HashSet<String>();

		for (String hyperLink : hyperLinks) {
			URL url = null;
			try {
				url = new URL(hyperLink);
			} catch (MalformedURLException mue) {
				continue;
			}

			newHyperLinks.add(url.toString());
		}

		hyperLinks.clear();
		hyperLinks.addAll(newHyperLinks);

	}

}
