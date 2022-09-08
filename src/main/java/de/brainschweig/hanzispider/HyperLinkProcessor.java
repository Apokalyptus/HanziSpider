package de.brainschweig.hanzispider;

import java.util.HashSet;
import java.util.Set;

public class HyperLinkProcessor {

	public static void cleanUpHyperLinks(Set<String> hyperLinks) {
		
		Set<String> newHyperLinks = new HashSet<String>();

		for(String hl : hyperLinks){
			if(hl.trim().isEmpty()) continue;
			
			newHyperLinks.add(hl);
		}
		
		hyperLinks = newHyperLinks;
		

	}

}
