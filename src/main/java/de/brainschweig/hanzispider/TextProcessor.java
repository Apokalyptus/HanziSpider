package de.brainschweig.hanzispider;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TextProcessor {

	static final Logger logger = LogManager.getLogger(TextProcessor.class.getName());

	private TextProcessor() {
		throw new IllegalStateException("Utility class");
	}

	public static StringBuilder processText(StringBuilder bodyContent) {
		String carriageReturn = "%n";
		carriageReturn = String.format(carriageReturn);
		Set<String> newStringList = null;

		logger.info("length: {}", bodyContent.length());
		logger.info("before: {}", bodyContent);

		if (bodyContent.length() == 0) {
			logger.info("bodyContent length zero. Skip!");
			return null;
		}

		for (int a = 0; a < bodyContent.length(); a++) {
			// Replace everything which is not HAN Character by carriage Return
			int codepoint = bodyContent.codePointAt(a);
			if (Character.UnicodeScript.of(codepoint) != Character.UnicodeScript.HAN) {
				bodyContent.replace(a, a + 1, carriageReturn);
			}
		}

		String[] lines = bodyContent.toString().split("\\r?\\n");

		newStringList = new HashSet<>(Arrays.asList(lines));
		Iterator<String> newStringListIterator = newStringList.iterator();
		while (newStringListIterator.hasNext()) {
			if (newStringListIterator.next().trim().isEmpty()) {
				newStringListIterator.remove();
			}
		}

		// put all elements into a string.
		StringBuilder newBodyContent = new StringBuilder();
		int i = newStringList.size();
		for (String s : newStringList) {
			i--;
			newBodyContent.append(s);
			if (i > 0)
				newBodyContent.append(carriageReturn);

		}

		bodyContent = newBodyContent;
		logger.info("after: {}", bodyContent);
		return bodyContent;

	}
}
