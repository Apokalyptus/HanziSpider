package de.brainschweig.hanzispider;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class HyperLinkProcessorTest {

    @Test
    public void testCleanUpHyperLinks() {
        Set<String> links = new HashSet<>();
        links.add("http://example.com");
        links.add("https://example.org");
        links.add("javascript:void(0)");
        links.add("mailto:user@example.com");
        links.add("");
        links.add("   ");
        links.add("HTTP://UPPERCASE.COM");

        HyperLinkProcessor.cleanUpHyperLinks(links);

        assertEquals(3, links.size());
        assertTrue(links.contains("http://example.com"));
        assertTrue(links.contains("https://example.org"));
        assertTrue(links.contains("HTTP://UPPERCASE.COM")); // Logic preserves case but checks lowercase start
        assertFalse(links.contains("javascript:void(0)"));
        assertFalse(links.contains("mailto:user@example.com"));
    }
}
