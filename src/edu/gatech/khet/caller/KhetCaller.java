package edu.gatech.khet.caller;

import java.io.File;
import java.io.IOException;

import edu.gatech.khet.Constants;
import edu.gatech.khet.KhetError;


public class KhetCaller {
	
	
	private String command;
	
	public KhetCaller(String com)
	{
		command=com;
	}
	
	public void call()
	{
		try {
			File jarFile = new File("kanalyze.jar");
			if ((jarFile).exists()) {
		                //System.out.println(jarFile);
		                Process p = Runtime.getRuntime().exec(command);
		                p.waitFor();
			} else {
			    System.out.println("File is not available");
			}
		} catch(IOException ex){
			String message = "Try to run Kananlyze and throw IOException";
			Throwable cause = new IllegalArgumentException(message);
			KhetError KE = new KhetError(message, Constants.ERR_USAGE, cause, 1);
			System.out.println(KE.toString());
			return;
	
		} catch (InterruptedException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
		return;
	}
	
}
