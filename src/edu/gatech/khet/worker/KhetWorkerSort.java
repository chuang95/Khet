package edu.gatech.khet.worker;

import java.io.File;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import edu.gatech.khet.SegmentFile;
import edu.gatech.khet.reader.KhetReaderBin;

public class KhetWorkerSort implements KhetWorker {
	
	private String command;
	private LinkedBlockingQueue<String> lbq;
	//private static Boolean kf;
	private KhetWorkerCall rc;
	private int kSize;
	private int numberofthread;
	private int upperThreshold;
	private LinkedBlockingQueue<SegmentFile> segmentQueue = new LinkedBlockingQueue<SegmentFile>();
	
	public KhetWorkerSort(String com, KhetWorkerCall rc, LinkedBlockingQueue<String> lbq, LinkedBlockingQueue<SegmentFile> segmentQueue, int kSize, int numberofthread, int upperThreshold)
	throws NullPointerException, IllegalArgumentException {
		this.command = com;
		//this.kf = rc;
		this.rc = rc;
		this.lbq = lbq;
		this.kSize = kSize;
		this.numberofthread = numberofthread;
		this.upperThreshold = upperThreshold;
		this.segmentQueue = segmentQueue;
		return;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("\nthread "+numberofthread+": sort bin files.");
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
		KhetReaderBin rb;
		while(!rc.getFlag() || !lbq.isEmpty()){
		  //removes the item
			try{
				String binfilename =lbq.poll(10, TimeUnit.SECONDS);
				Thread.sleep(1000);
				if(binfilename==null)
					continue;
				rb = new KhetReaderBin(binfilename,kSize,upperThreshold);
				rb.read(binfilename);
//				for(int i=0;i<100;i++)
//					System.out.print(data[0]+" ");
//				System.out.println("\n "+data.length);
				System.out.println(binfilename+" removed.");
				File fb = new File(binfilename+".sort");
				SegmentFile sf = new SegmentFile(fb);
				segmentQueue.put(sf);
				//Thread.sleep(1000);
			}catch(InterruptedException ex){
				ex.printStackTrace();
			}
			System.out.println(rc.getFlag());
		}
	}

}
