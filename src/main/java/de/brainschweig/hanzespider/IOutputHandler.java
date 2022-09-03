package de.brainschweig.hanzespider;


public interface IOutputHandler extends Runnable {
	
	public void addToBuffer(String bodyContent);
	public String getBuffer();

}
