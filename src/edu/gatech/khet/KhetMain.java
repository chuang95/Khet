package edu.gatech.khet;

import java.io.File;
import java.io.PrintStream;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;

import edu.gatech.khet.merger.CountMergeComponent;
import edu.gatech.kanalyze.comp.countfilecustomwriter.CountFileWriterComponent;
import edu.gatech.kanalyze.util.CounterPair;
import edu.gatech.khet.worker.KhetWorkerCall;
import edu.gatech.khet.worker.KhetWorkerListen;
import edu.gatech.khet.worker.KhetWorkerSort;
import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;


public class KhetMain extends KhetError {
	

	public KhetMain(String message, int returnCode, Throwable cause, int type)
			throws NullPointerException, IllegalArgumentException {
		super(message, returnCode, cause, type);
	}

	/** Name of the file to write. */
	private static String inputFileName;
	
	/** Default output file name. */
	public static final String DEFAULT_OUTPUT_FILE_NAME = "output.het";
	
	/** Default number of threads. */
	public static final int DEFAULT_THREADS = 1;
	
	/** Name of the file to write. */
	private static String outputFileName = DEFAULT_OUTPUT_FILE_NAME;
	
	/** K-mer size. */
	private static int kSize = Constants.LIMIT_KSIZE;
	
	/** reverse flag. */
	private static Boolean reverseFlag = false;
	
	/** segment file size. */
	private static String segSize = Constants.DEFAULT_SEG_SIZE;
	
	/** number of threads. */
	private static int numberThreads = DEFAULT_THREADS;
	
	/** kmer counts lower threshold. */
	private static int lowerThreshold = Constants.DEFAULT_LOWER_THRESHOLD;
	
	/** kmer counts upper threshold. */
	private static int upperThreshold = Constants.DEFAULT_UPPER_THRESHOLD;
	
	/** Default input file format. */
	public static final String DEFAULT_FORMAT = "raw";
	
	/** Default input file format. */
	public static final String DEFAULT_OUTPUT_FORMAT = "SEQ";
	
	/** input file format. */
	private static String inputFormat = DEFAULT_FORMAT;
	
	/** input file format. */
	private static String outputFormat = DEFAULT_OUTPUT_FORMAT;
	
	/** Default k-mer size. */
	public static final int DEFAULT_KSIZE = Constants.DEFAULT_KSIZE;
	
	/** command for Kanalyze. */
	private static String command;
	
	/** Synchronized queue to hold all jobs for workers*/
	private static LinkedBlockingQueue<String> lbq = new LinkedBlockingQueue<String>();
	
	/** Synchronized queue to hold all jobs for merger*/
	private static LinkedBlockingQueue<SegmentFile> segmentQueue = new LinkedBlockingQueue<SegmentFile>();
	
	/** Synchronized queue to hold all jobs for writer*/
	private static LinkedBlockingQueue<CounterPair[]> countQueue = new LinkedBlockingQueue<CounterPair[]>();
	
	/** intermediate file format*/
	private static String inff = "bin";
	
	/** flag for kanalyze*/
	private static Boolean kf = false;
	
	/** Component properties. */
	private static Properties compProp = new Properties();
	
	/** flag for worker*/
	private static Boolean wf = false;
	
	
	/**
	 * Print command line usage help.
	 */
	private static void printHelp() 
	{
		
		PrintStream out = System.out;
		
		out.println("KhetMain [-d <k-mer threads>] [-f <format>] [-k <kmer size>] [-l <split threads>]");
		out.println("      [-o <output>] [-r <rev mode>] [-R] [-p <key=value>] -v -V");
		out.println("KhetMain -h");
		out.println();
		out.println("-f --format [default = " + DEFAULT_FORMAT + "]");
		out.println("\tSet the input sequence format type. This option determines how the");
		out.println("\tformat files are read. Valid examples include \"raw\", \"fasta\",");
		out.println("\t\"fastq\", \"fastagz\", and \"fastqgz\". This option may be set multiple");
		out.println("\ttimes when reading multiple files with different formats.");
		out.println();
		out.println("-g --segsize [default =  Kanalyze default size (65536000)]");
		out.println("\tSize of k-mer segments stored in memory. This many k-mers are accumulated into");
		out.println("\tan array in memory. When the array is full, it is sorted, written to disk, and");
		out.println("\tcleared for the next set of k-mers. This is a performance tuning parameter that");
		out.println("\tdoes not need to be set for the majority of data sets. If a value is chosen that");
		out.println("\tis higher than the default, then more than 2GB of memory will be required. See the");
		out.println("\tKAnalyze manual for more information on performance tuning. This number may be in");
		out.println("\tdecimal, hexadecimal, or octal. An optional multiplier (k, m, or g) may included");
		out.println("\tafter the number. For the maximum value, use keyword \"max\". See the KAnalyze");
		out.println("\tmanual usage documentation for more information on acceptable formats.");
		out.println();
		out.println("-h --help");
		out.println("\tPrint help and exit.");
		out.println();
		out.println("-k --ksize [default = " + DEFAULT_KSIZE + ", max = " + Constants.LIMIT_KSIZE + "]");
		out.println("\tK-mer size. This determines the size of k-mers extracted from");
		out.println("\tinput sequences.");
		out.println();
		out.println("-i --in");
		out.println("\tInput file name for the file reader.");
		out.println();
		out.println("-o --out [default = " + DEFAULT_OUTPUT_FILE_NAME + "]");
		out.println("\tOutput file name for the file writer.");
		out.println();
		out.println("-r --reverse");
		out.println("\tReverse complement k-mers as they are generated.");
		out.println();
		out.println("-t --threads [default = " + DEFAULT_THREADS + "]");
		out.println("\tThe number of threads to work");
		out.println();
		out.println("-l --lower");
		out.println("\tLower threshold of kmer counts to minimize sequence error.");
		out.println();
		out.println("-u --upper");
		out.println("\tUpper threshold of kmer counts to minimize sequence error.");
		out.println();
		out.println("-z --format [default = " + DEFAULT_OUTPUT_FORMAT + "]");
		out.println("\tSet the output format type. This option determines how the");
		out.println("\tformat files are output. Valid examples include \"SEQ\", \"INT\",");
		out.println("\tand \"HEX\". This option may be set multiple");
		out.println("\ttimes when reading multiple files with different formats.");
		out.println();
		
		return;
	}

	public static void main(String[] args) throws InterruptedException {
		
		//long startTime = System.currentTimeMillis();
		
		// Temporary variables
		String optarg; // Option argument
		int opt; // Option number getopt sets

		// Check arguments
		if (args == null)
			printHelp();
			
		if (args.length == 0)
			printHelp();
		
		// Long options
		LongOpt[] longOpts = new LongOpt[] {
				new LongOpt("format", LongOpt.REQUIRED_ARGUMENT, null, 'f'),
				new LongOpt("segsize", LongOpt.REQUIRED_ARGUMENT, null, 'g'),
				new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'),
				new LongOpt("ksize", LongOpt.REQUIRED_ARGUMENT, null, 'k'),
				new LongOpt("in", LongOpt.REQUIRED_ARGUMENT, null, 'i'),
				new LongOpt("out", LongOpt.REQUIRED_ARGUMENT, null, 'o'),
				new LongOpt("reverse", LongOpt.NO_ARGUMENT, null, 'r'),
				new LongOpt("threads", LongOpt.REQUIRED_ARGUMENT, null, 't'),
				new LongOpt("lower", LongOpt.REQUIRED_ARGUMENT, null, 'l'),
				new LongOpt("upper", LongOpt.REQUIRED_ARGUMENT, null, 'u'),
				new LongOpt("outputformat", LongOpt.REQUIRED_ARGUMENT, null, 'z'),
		};
		
		// Create getopt object
		Getopt g = new Getopt("KhetMain", args,
				"-f:g:hk:i:o:rt:l:u:z:", longOpts);
		
		g.setOpterr(false); // Getopt will not output error messages
		
		// Get options
		while ((opt = g.getopt()) != -1) {
			
			switch (opt) {
			
			case 'f': // Format type
				optarg = g.getOptarg();
				
				if (optarg.isEmpty()) {
					error("Cannot set format type (-f): Type name is empty", Constants.ERR_USAGE, 1);
					return;
				}
				
				if (! optarg.matches(Constants.FORMAT_TYPE_PATTERN)) {
					error("Cannot set format type (-f): Type name does not match regular expression pattern \"" + Constants.FORMAT_TYPE_PATTERN + "\": " + optarg, Constants.ERR_USAGE, 1);
					return;
				}
				inputFormat=optarg;
				break;
				
			case 'g': // Segment size
				
				optarg = g.getOptarg();
				segSize=optarg;
				break;
				
			case 'h': // Help
				printHelp();
				return;
				
			case 'k': // K-mer size
				optarg = g.getOptarg();
				
				if (optarg.isEmpty()) {
					error("Cannot set k-mer size (-k): Size is empty", Constants.ERR_USAGE, 1);
					return;
				}
				
				try {
					kSize = Integer.parseInt(optarg);
					
				} catch (NumberFormatException ex) {
					error("Cannot set k-mer size (-k): Size is not an integer: " + optarg, Constants.ERR_USAGE, 1);
					return;
				}
				
				if (!(kSize > 0 &&kSize <= Constants.LIMIT_KSIZE)) {
					error("Cannot set k-mer size (-k): Size is not between 1 and " + Constants.LIMIT_KSIZE + " (inclusive): " + kSize, Constants.ERR_USAGE, 1);
					return;
				}
				break;
				
			case 'i': // Output file name
				optarg = g.getOptarg();
				
				if (optarg.isEmpty()) {
					error("Cannot process empty file name argument from the command line", Constants.ERR_USAGE, 1);
					return;
				}
				inputFileName = g.getOptarg().trim();
				break;
				
			case 'o': // Output file name
				outputFileName = g.getOptarg().trim();
				break;
				
			case 'r': // reverse
				reverseFlag=true;
				break;
				
			case 't': // threads for worker
				optarg = g.getOptarg();
				
				if (optarg.isEmpty()) {
					error("Cannot set number of threads to empty", Constants.ERR_USAGE, 1);
					return;
				}
				
				try {
					numberThreads = Integer.parseInt(optarg);
					
				} catch (NumberFormatException ex) {
					error("Cannot set number of threads to not an integer: " + optarg, Constants.ERR_USAGE, 1);
					return;
				}
				
				if (!(numberThreads > 0 &&numberThreads <= Constants.LIMIT_NT)) {
					error("Cannot set number of threads more than " + Constants.LIMIT_NT + " and you tend to use" + numberThreads, Constants.ERR_USAGE, 1);
					return;
				}
				break;
				
			case 'l':
				optarg = g.getOptarg();
				
				if (optarg.isEmpty()) {
					error("Cannot set kmer lower threshold to empty", Constants.ERR_USAGE, 1);
					return;
				}
				
				try {
					lowerThreshold = Integer.parseInt(optarg);
					
				} catch (NumberFormatException ex) {
					error("Cannot set Cannot set kmer lower threshold  to not an integer: " + optarg, Constants.ERR_USAGE, 1);
					return;
				}
				break;
				
			case 'u':				
				optarg = g.getOptarg();
				
				if (optarg.isEmpty()) {
					error("Cannot set kmer upper threshold to empty", Constants.ERR_USAGE, 1);
					return;
				}
				
				try {
					upperThreshold = Integer.parseInt(optarg);
					
				} catch (NumberFormatException ex) {
					error("Cannot set Cannot set kmer upper threshold  to not an integer: " + optarg, Constants.ERR_USAGE, 1);
					return;
				}
				break;
				
			case 'z': // Format type
				optarg = g.getOptarg();
				
				if (optarg.isEmpty()) {
					error("Cannot set format type (-f): Type name is empty", Constants.ERR_USAGE, 1);
					return;
				}
				
				if (! optarg.matches(Constants.FORMAT_TYPE_PATTERN)) {
					error("Cannot set format type (-f): Type name does not match regular expression pattern \"" + Constants.FORMAT_TYPE_PATTERN + "\": " + optarg, Constants.ERR_USAGE, 1);
					return;
				}
				outputFormat=optarg;
				break;
				
			default: // Unexpected error
				assert(false) :
					"Reached default case processing command line options";
			}
		}	
		
		//System.out.println("upper: " + upperThreshold + " lower: " + lowerThreshold);
		//Thread.sleep(1000000);
		try{
			/**
			 * start one thread to call Kanalyze with arguments
			 */
			
			//construct command for Kanalyze
			if(reverseFlag)
			{
				command = "java -jar kanalyze.jar count "+"-k "+ kSize +" -f " + inputFormat + " -g " + segSize + " -r" + " -X " + "-s " + inputFileName;
			}
			else
				command = "java -jar kanalyze.jar count "+"-k "+ kSize +" -f " + inputFormat + " -g " + segSize + " -X " + "-s " + inputFileName;	
			System.out.println("Step 1: Call Kanalyze \nCheck command for Kanalyze.");
			KhetWorkerCall rc = new KhetWorkerCall(command,kf);
			Thread rc_t = new Thread(rc);
			rc_t.setDaemon(true);
			rc_t.setName("kanalyze_caller");
			rc_t.start();
			
			/**
			 * start one thread to listen for binary files from kanalyze
			 * */
			System.out.println("\nStep 2: Start listening for " + command + "files. \nCheck files for listener");
			KhetWorkerListen rl = new KhetWorkerListen(inff,lbq);
			//Runnable rl = new KhetWorkerListen(inff);
			Thread rl_t = new Thread(rl);
			rl_t.setDaemon(true);
			rl_t.setName("khet_listener");
			rl_t.start();		
			
			/**
			 * start threads to sort binary files
			 * */
			//kf = rc.getFlag();
			System.out.println("\nStep 3: sort bin files.");
			Thread[] workers = new Thread[numberThreads];
			for(int i=0;i<numberThreads;i++)
			{
				KhetWorkerSort rs = new KhetWorkerSort(command, rc, lbq,segmentQueue, kSize, i, upperThreshold);
				workers[i] = new Thread(rs);
				workers[i].setDaemon(true);
				workers[i].setName("Khet_sort");
				workers[i].start();
			}
						
			/**
			 * join threads
			 */
			try{
				rc_t.join();
				rl_t.join();
				//rs_t.join();
				for(int i=0;i<numberThreads;i++)
				{
					workers[i].join();
				}
			}catch(InterruptedException ex){
				error("Thread interrupted.", Constants.ERR_USAGE, 1);
				return;
			}
			
			/**
			 * start threads to merge binary files
			 * */
			compProp.setProperty(Constants.PROP_OUTFMT, outputFormat);
			System.out.println("\nStep 4: merge bin files.");
			CountMergeComponent mergeComponent = new CountMergeComponent(segmentQueue, countQueue, compProp, wf);
			//Runnable rl = new KhetWorkerListen(inff);
			Thread mergeComponent_t = new Thread(mergeComponent);
			mergeComponent_t.setDaemon(true);
			mergeComponent_t.setName("khet_merge");
			mergeComponent_t.start();
			
			/**
			 * start threads to write output files
			 * */
			System.out.println("\nStep 5: output files.");
			CountFileWriterComponent writerComponent = new CountFileWriterComponent(countQueue, mergeComponent, outputFileName, kSize, lowerThreshold, compProp); // throws IllegalArgumentException
			Thread writerComponent_t = new Thread(writerComponent);
			writerComponent_t.setDaemon(true);
			writerComponent_t.setName("khet_write");
			writerComponent_t.start();

			try{
				mergeComponent_t.join();
				writerComponent_t.join();
			}catch(InterruptedException ex){
				error("Thread interrupted.", Constants.ERR_USAGE, 1);
				return;
			}
			
			System.out.println("done");		
		} catch (IllegalThreadStateException ex) {
			error("Thread for calling kanalyze throw IllegalThreadStateException", Constants.ERR_USAGE, 1);
			return;
		}
		
		/**
		 * Delete all bin files
		 * */
		System.out.println("Final Step: Delete intermediate files");
		String parentDirectory="."+File.separator;
		String deleteExtension="."+inff;
		FileFilter fileFilter = new FileFilter(deleteExtension);
		File parentDir = new File(parentDirectory);

		// Put the names of all files ending with .txt in a String array
		String[] listOfTextFiles = parentDir.list(fileFilter);

		if (listOfTextFiles.length == 0) {
			System.out.println("There are no bin files in this direcotry!");
			return;
		}

		File fileToDelete;

		for (String file : listOfTextFiles) {

			//construct the absolute file paths...
			String absoluteFilePath = new StringBuffer(parentDirectory).append(File.separator).append(file).toString();

			//open the files using the absolute file path, and then delete them...
			fileToDelete = new File(absoluteFilePath);
			boolean isdeleted = fileToDelete.delete();
			System.out.println("File : " + absoluteFilePath + " was deleted : " + isdeleted);
		}
		
		
		
		
//		long endTime   = System.currentTimeMillis();
//		long totalTime = endTime - startTime;
//		System.out.println(totalTime);
		return;
	}
	


}
