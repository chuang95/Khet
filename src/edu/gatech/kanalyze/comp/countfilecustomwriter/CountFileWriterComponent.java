// Copyright (c) 2014 Peter A. Audano III
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 3 of the License or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with this program; see the file COPYING.LESSER.  If not, see
// <http://www.gnu.org/licenses/>

package edu.gatech.kanalyze.comp.countfilecustomwriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import edu.gatech.khet.Constants;
import edu.gatech.khet.merger.CountMergeComponent;
import edu.gatech.kanalyze.io.OutputFormat;
import edu.gatech.kanalyze.util.CounterPair;

/**
 * Writes k-mers to a file.
 */
public class CountFileWriterComponent implements Runnable {

	/** Queue k-mers are read from. */
	private LinkedBlockingQueue<CounterPair[]> countQueue;
	
	/** Name of the output file. */
	private final String fileName;
	
	/** K-mer size. */
	private final int kSize;
	
	/** K-mer write counter. */
	private int writeCount;

	/** Output format. */
	private OutputFormat outFmt;
	
	private CountMergeComponent mergeComponent;
	
	private int lowerThreshold;
	
	
	/**
	 * Create a new count file writer component.
	 * 
	 * @param countQueue Queue to read k-mer counts from.
	 * @param fileName Name of the file to output.
	 * @param kSize K-mer size.
	 * @param compProp Properties given to components
	 * @param wf 
	 * 
	 * @throws NullPointerException If <code>kmerQueue</code> or <code>fileName</code>
	 *   is <code>null</code>.
	 * @throws IllegalArgumentException If <code>kSize</code> is not a valid k-mer size or
	 *   if an invalid option is found in <code>compProp</code>.
	 */
	public CountFileWriterComponent(LinkedBlockingQueue<CounterPair[]> countQueue, CountMergeComponent mergeComponent, String fileName, int kSize, int lowerThreshold, Properties compProp)
			throws NullPointerException, IllegalArgumentException {
		
		String value; // Value buffer for processing properties

		// Check arguments
		if (countQueue == null)
			throw new NullPointerException("Cannot create count file writer with count queue: null");
		
		if (fileName == null)
			throw new NullPointerException("Cannot create count file writer with file name: null");
		
		if (kSize < 0 || kSize > Constants.LIMIT_KSIZE)
			throw new IllegalArgumentException("k-mer size is out of bounds (1 <= size <= " + Constants.LIMIT_KSIZE + "): " + kSize);
		
		// Set fields
		this.countQueue = countQueue;
		this.fileName = fileName;
		this.kSize = kSize;
		this.outFmt = OutputFormat.SEQ;
		this.mergeComponent = mergeComponent;
		this.lowerThreshold = lowerThreshold;
		
		// Process properties
		if ((value = compProp.getProperty(Constants.PROP_OUTFMT)) != null)
			outFmt = OutputFormat.getFormat(value);
		
		if (outFmt == null)
			throw new IllegalArgumentException("Unrecognized output format in properties (" + Constants.PROP_OUTFMT + "): " + value);
		
		writeCount = 0;
		
		return;
	}
	
	
	/**
	 * Run this component.
	 */
	@Override
	public void run() {
		
		File outFile;
		CountFormatWriter formatWriter = null;
		CounterPair[] countBatch;
		
		// Output file
		outFile = new File(fileName);

		// Read each k-mer and write
		try (FileOutputStream fos = new FileOutputStream(outFile)) {
			FileChannel fc = fos.getChannel();
			
			System.out.println("writer: "+outFmt);
			
			// Set writer
			switch(outFmt) {
			case SEQ:
				formatWriter = new SEQFormatWriter(fc, kSize, lowerThreshold);
				break;
				
			case INT:
				formatWriter = new INTFormatWriter(fc, kSize);
				break;
				
			case HEX:
				formatWriter = new HEXFormatWriter(fc, kSize);
				break;
				
			default:
				assert(false) :
					"Unrecognized output format: " + outFmt.toString();
				
				//error("Unrecognized output format: " + outFmt.toString(), Constants.ERR_SYSTEM);
				
				return;
			}
			
			//int index=0;
			//while (isActive) {
			while (!mergeComponent.getFlag() || !countQueue.isEmpty()) {
				// Get batch
				try {
					countBatch =countQueue.poll(10, TimeUnit.SECONDS);
					Thread.sleep(1);
					//index++;
					//System.out.println(index);
					
				} catch (InterruptedException ex) {
					continue;
				}
				
				// Termination signal
				if (countBatch == null)
					break;
				
				// Write
				formatWriter.write(countBatch);
			}
			
			formatWriter.flush();

			// Clean up
			try {
				formatWriter.postExec();
				
			} catch (Throwable ex) {
				// Ignore
			}
			
		} catch (IOException ex) {
			//error("IO error writing output file: " + fileName + ": " + ex.getMessage(), Constants.ERR_IO, ex);
			ex.printStackTrace();
		}
		
		System.out.println("writer done");
		return;
	}
	
	/**
	 * Get the number of k-mers written to the database.
	 * 
	 * @return Number of k-mers written to the database.
	 */
	public long getWriteCount() {
		return writeCount;
	}


}
