package de.brainschweig.interfaces;


public interface IOutputHandler extends Runnable {
	
	public void addToBuffer(String bodyContent);
	public String getBuffer();
	public String getName();

}
