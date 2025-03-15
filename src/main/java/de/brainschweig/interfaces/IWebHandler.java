package de.brainschweig.interfaces;

import java.util.Set;
import java.io.IOException;

public interface IWebHandler {
    
    public void getWebContent(String url, StringBuilder bodyContent, Set<String> hyperLinks, String proxyAddr, String proxyPort) throws IOException ;
    public String getName();

}
