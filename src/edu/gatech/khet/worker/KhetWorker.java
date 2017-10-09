package edu.gatech.khet.worker;

public interface KhetWorker extends Runnable{
	
	/**
	 * interface for workers 
	 * */
	
	public void checkCommand();
	public void work(String command);
	
}
