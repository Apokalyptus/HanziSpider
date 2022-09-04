package de.brainschweig.hanzespider;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class HyperLinkProcessor {

	public static void cleanUpHyperLinks(Set<String> hyperLinks) {
		
		Set<String> newHyperLinks = new HashSet<String>();
		System.out.println("NO LINKS:" + hyperLinks.size());

		for(String hyperLink : hyperLinks){
			URL url = null;
			try{
			url = new URL(hyperLink);
			} catch (MalformedURLException mue) {
				continue;
			}
		
			System.out.println("Hyperlink: " + (null==hyperLink?"none":hyperLink) + "\nhyperlink.trim: " + url.toString());

			newHyperLinks.add(url.toString());
		}
		
		hyperLinks.clear();
		hyperLinks.addAll(newHyperLinks);
		

	}

}
