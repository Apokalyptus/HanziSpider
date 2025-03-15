package de.brainschweig.hanzispider.interfaces;


public interface IOutputHandler extends Runnable {
	
	//public void addToBuffer(String bodyContent);
	public String getBuffer();
	public String getName();

}
