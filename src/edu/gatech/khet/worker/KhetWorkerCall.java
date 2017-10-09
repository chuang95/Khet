package edu.gatech.khet.worker;

import edu.gatech.khet.caller.*;

public class KhetWorkerCall implements KhetWorker{
	
	private String command;
	private Boolean kf;
	
	public KhetWorkerCall(String com, Boolean kf)
	throws NullPointerException, IllegalArgumentException {
		this.command = com;
		this.kf = kf;
		return;
	}

	@Override
	public void checkCommand() {
		// TODO Auto-generated method stub
		System.out.println(command);
	}

	@Override
	public void work(String command) {	
		KhetCaller kc = new KhetCaller(command);
		kc.call();
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		checkCommand();
		System.out.println("Start kmer with Kanalyze");
		work(command);
		kf = true;
		System.out.println("Kanalyze done, flag: " + kf);
	}
	
	public synchronized Boolean getFlag() {	
		return this.kf;
	}

}
