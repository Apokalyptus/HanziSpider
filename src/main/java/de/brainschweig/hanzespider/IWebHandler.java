package de.brainschweig.hanzespider;

import java.util.Set;
import java.io.IOException;

public interface IWebHandler {
    
    public void getWebContent(String url, StringBuilder bodyContent, Set<String> hyperLinks) throws IOException ;
}
