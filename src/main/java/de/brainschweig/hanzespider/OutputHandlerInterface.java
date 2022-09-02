package de.brainschweig.hanzespider;


public interface OutputHandlerInterface extends Runnable {
	
	public void addToBuffer(String bodyContent);
	public String getBuffer();

}
