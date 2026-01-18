package de.brainschweig.hanzispider;

import java.util.HashSet;
import java.util.Set;

public class HyperLinkProcessor {

	private HyperLinkProcessor() {
		throw new IllegalStateException("Utility class");
	}

	public static void cleanUpHyperLinks(Set<String> hyperLinks) {

		Set<String> newHyperLinks = new HashSet<>();

		for (String hl : hyperLinks) {
			String trimmedHl = hl.trim();
			if (trimmedHl.isEmpty()) {
				continue;
			}
			
			String lowerHl = trimmedHl.toLowerCase();
			if (!lowerHl.startsWith("http://") && !lowerHl.startsWith("https://")) {
				continue;
			}

			newHyperLinks.add(trimmedHl);
		}

		hyperLinks.clear();
		hyperLinks.addAll(newHyperLinks);

	}

}
