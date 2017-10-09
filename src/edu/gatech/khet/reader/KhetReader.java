package edu.gatech.khet.reader;

public interface KhetReader {

	/**
	 * interface for readers 
	 * */
	
	public boolean checkFile(String filename);
	public void read(String filename);
	public String getFilename();
	
}
