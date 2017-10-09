package edu.gatech.khet.worker;

import java.io.File;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

import edu.gatech.khet.listener.KhetListener;

public class KhetWorkerListen implements KhetWorker{
	
	private String command;
	private LinkedBlockingQueue<String> lbq;
	
	
	public KhetWorkerListen(String com, LinkedBlockingQueue<String> lbq)
	throws NullPointerException, IllegalArgumentException {
		command = com;
		this.lbq = lbq;
		return;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		checkCommand();
		//System.out.println("Start listening");
		work(command);
	}

	@Override
	public void checkCommand() {
		// TODO Auto-generated method stub
		System.out.println(command);
	}

	@Override
	public void work(String command) {
		// TODO Auto-generated method stub
	    TimerTask task = new KhetListener(".", command ) {
	        protected void onChange( File file, String action ) {
	          // here we code the action on a change
	          try {
				lbq.put(file.getName());
			} catch (InterruptedException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
	          System.out.println( "File "+ file.getName() +": " + action );
	        }
	      };

	      Timer timer = new Timer();
	      timer.schedule( task , new Date(), 1000 );
	}
	

}
