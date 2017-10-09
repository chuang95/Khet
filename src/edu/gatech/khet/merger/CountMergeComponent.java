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

package edu.gatech.khet.merger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import edu.gatech.kanalyze.util.CounterPair;
import edu.gatech.khet.Constants;
import edu.gatech.khet.SegmentFile;

/**
 * Merges files written by <code>MCountSplitComponent</code>.
 */
public class CountMergeComponent implements Runnable {
	
	/** Queue of segment files to read. */
	private LinkedBlockingQueue<SegmentFile> segmentQueue;
	
	/** Queue of output k-mer counts. */
	private LinkedBlockingQueue<CounterPair[]> countQueue;
	
	/** Size of write buffer. */
	private int bufRecordCount;
	
	/** Number of k-mers written. */
	private long kmerCount;
	
	/** Default number of records that are written to disk in a block. */
	public static final int DEFAULT_BUF_RECORD_COUNT = 5000;
	
	/** Property: Remove segment files. */
	public static final String PROP_DELETE_SEGMENT_FILES = "comp.count.deltemp";
	
	private Boolean wf;

	/**
	 * 
	 * @param segmentQueue Queue of files to read.
	 * @param countQueue Queue of k-mer counts.
	 * @param compProp Component properties. If <code>null</code>, properties are empty.
	 * 
	 * @throws NullPointerException If <code>segmentQueue</code> or <code>countQueue</code>
	 *   is <code>null</code>.
	 * @throws IllegalArgumentException If the <code>kSize</code> is not a valid k-mer size.
	 */
	public CountMergeComponent(LinkedBlockingQueue<SegmentFile> segmentQueue, LinkedBlockingQueue<CounterPair[]> countQueue, Properties compProp, Boolean wf)
			throws NullPointerException, IllegalArgumentException {
		
		if (segmentQueue == null)
			throw new NullPointerException("Cannot create merge component: Segment queue is null");

		if (countQueue == null)
			throw new NullPointerException("Cannot create merge component: Count queue is null");
	
		this.segmentQueue = segmentQueue;
		this.countQueue = countQueue;
		
		this.bufRecordCount = DEFAULT_BUF_RECORD_COUNT;
		
		this.wf=wf;

		kmerCount = 0;
		
		return;
	}
	
	/**
	 * Run this component.
	 */
	@Override
	public void run() {
		
		kmerCount = 0;   // Run metrics
		
		CounterPair[] batch; // Batch of k-mer counts
		int batchIndex = 0;  // Current location of batch

		SegmentFile segmentFile; // Segment file buffer
		List<SegmentFile> segFileList;  // List of segment files
		
		long nextKmer; // Next k-mer to be counted (set while traversing - becomes curKmer in the next round)
		long curKmer;  // K-mer currently being counted
		int count;     // K-mer count
		
		SegmentContainer rootContainer; // Root of linked list
		SegmentContainer lastContainer; // Last container (for building list)
		SegmentContainer nextContainer; // Next container

		// Initialize
		rootContainer = lastContainer = null;
		batch = new CounterPair[Constants.DEFAULT_BATCH_SIZE];
		nextKmer = 0; // Compile error if not initialized - Actual value set in the segment load loop
		
		segFileList = new ArrayList<SegmentFile>();
		
		// Load all segments before counting
		if(segmentQueue.isEmpty()){System.out.println("sementqueue empty");}
		LOAD_LOOP:
		while (!segmentQueue.isEmpty()) {
			
			try {
				segmentFile = segmentQueue.poll(10, TimeUnit.SECONDS);
				
			} catch (InterruptedException ex) {
				continue;
			}
			
			if (segmentFile == null)
				break LOAD_LOOP;
			
			// Get next container
			try {
				nextContainer = new SegmentContainer(segmentFile);
				
			} catch (FileNotFoundException ex) {
				System.out.println("Segment file not found: " + segmentFile.file.getName() + ": " + ex.getMessage());
				ex.printStackTrace();
				return;
				
			} catch (IOException ex) {
				System.out.println("IO error opening segment file: " + segmentFile.file.getName() + ": " + ex.getMessage());
				ex.printStackTrace();
				return;
			}
			
			// Store file in list
			segFileList.add(segmentFile);
			
			// Do not add containers that contain no k-mers
			try {
				if (! nextContainer.load())
					continue LOAD_LOOP;
				
			} catch (IOException ex) {
				System.out.println("IO error loading data from segment file: " + segmentFile.file.getName() + ": " + ex.getMessage());
				ex.printStackTrace();
				return;
			}
			
			assert (nextContainer.length > 0) :
				"load() returned, but length is not greather than 0";
			
			// Add container to the linked list and find the lowest nextKmer of all containers
			if (rootContainer == null) {
				// First container
				rootContainer = lastContainer = nextContainer;
				nextKmer = nextContainer.kmer[nextContainer.index];
				
			} else {
				// Not first container: Update linked list
				lastContainer.fwdLink = nextContainer;
				nextContainer.revLink = lastContainer;
				lastContainer = nextContainer;
				
				if (nextContainer.kmer[nextContainer.index] < nextKmer)
					nextKmer = nextContainer.kmer[nextContainer.index];
			}
		}
		
		segmentQueue = null; // Free memory for GC
		
		if (rootContainer == null)
			return;
		
		// Iterate while there are at least two containers in the list. If rootContainer is set
		// to null while removing containers (which occurs when the last two containers are removed
		// in the same iteration), the loop terminates without checking the loop condition to avoid
		// null pointer exceptions without checking for (rootContainer == null) at ever iteration.
		COUNT_LOOP:
		while (rootContainer.fwdLink != null) {
			nextContainer = rootContainer;
			
			curKmer = nextKmer;
			nextKmer = Long.MAX_VALUE;
			count = 0;
			
			// Iterate through all containers
			while (nextContainer != null) {
				
				// If the next k-mer in the container is the next to be analyzed,
				// update count and find next k-mer in container
				if (nextContainer.kmer[nextContainer.index] == curKmer) {
					count += nextContainer.count[nextContainer.index];
					++nextContainer.index;
					
					// Check for end of buffer
					if (nextContainer.index == nextContainer.length) {
						
						// Reload buffer
						try {
							if (! nextContainer.load()) {
	
								// No k-mers loaded - remove container
								
								if (nextContainer.fwdLink != null)
									nextContainer.fwdLink.revLink = nextContainer.revLink;
								
								if (nextContainer.revLink != null)
									nextContainer.revLink.fwdLink = nextContainer.fwdLink;
								else
									rootContainer = nextContainer.fwdLink;
								
								// Terminate if root container is now null
								if (rootContainer == null) {
									
									batch[batchIndex++] = new CounterPair(curKmer, count);
									kmerCount += count;
									
									// Since there are no more k-mer counts to write, do not check for a full
									// batch and output. Let the logic at the end of this method check and write
									
									break COUNT_LOOP;
								}
								
							} else {
								if (nextContainer.kmer[nextContainer.index] < nextKmer)
									nextKmer = nextContainer.kmer[nextContainer.index];
							}
							
						} catch (IOException ex) {
							System.out.println("IO error loading data from segment file: " + nextContainer.file.getName() + ": " + ex.getMessage());
							ex.printStackTrace();
							return;
						}
						
					} else {
						if (nextContainer.kmer[nextContainer.index] < nextKmer)
							nextKmer = nextContainer.kmer[nextContainer.index];
					}
					
				} else {
					if (nextContainer.kmer[nextContainer.index] < nextKmer)
						nextKmer = nextContainer.kmer[nextContainer.index];
				}
				
				nextContainer = nextContainer.fwdLink;
			}
			
			// Write count to batch
			batch[batchIndex++] = new CounterPair(curKmer, count);
			kmerCount += count;
			
			// Send full batches to the queue
			if (batchIndex == batch.length) {
				putBatch(batch);
				
				batch = new CounterPair[Constants.DEFAULT_BATCH_SIZE];
				batchIndex = 0;
			}
		}
		
		// If there is one container left, iterate over it
		if (rootContainer != null) {
			nextContainer = rootContainer;
			
			FINAL_LOOP:
			while (true) {
				// Write count to batch
				batch[batchIndex++] = new CounterPair(nextContainer.kmer[nextContainer.index], nextContainer.count[nextContainer.index]);
				kmerCount += nextContainer.count[nextContainer.index];
				++nextContainer.index;
				
				// Send full batches to the queue
				if (batchIndex == batch.length) {
					putBatch(batch);
					
					batch = new CounterPair[Constants.DEFAULT_BATCH_SIZE];
					batchIndex = 0;
				}

				// Load
				if (nextContainer.index == nextContainer.length) {
					
					// Terminate loop when load fails
					try {
						if (! rootContainer.load())
							break FINAL_LOOP;
						
					} catch (IOException ex) {
						System.out.println("IO error loading data from segment file: " + rootContainer.file.getName() + ": " + ex.getMessage());
						ex.printStackTrace();
						return;
					}
				}
			}
		}
		
		// Free memory for GC
		rootContainer = null;
		
		// Write final batch.
		if (batchIndex > 0) {
			
			// Create a shorter array if batch is not full (do not write array with null counts)
			if (batchIndex < batch.length) {
				CounterPair[] newBatch = new CounterPair[batchIndex];
				
				for (int index = 0; index < batchIndex; ++index)
					newBatch[index] = batch[index];
				
				batch = newBatch;
			}
			
			putBatch(batch);
		}
		
		// Free memory for GC
		batch = null;
		countQueue = null;
		
		// Remove segment files
		for (SegmentFile segFile : segFileList) {
			
			if (segFile.autoDelete) {
				try {
					segFile.file.delete();
					
				} catch (SecurityException ex) {
					// Ignore - best effort delete
				}
			}
		}
		
		wf = true;
		return;
	}
	
	/**
	 * Write batch reliably.
	 * 
	 * @param batch Batch to write.
	 */
	private void putBatch(CounterPair[] batch) {
		
		while (true) {
			try {
				countQueue.put(batch);
				break;
				
			} catch (InterruptedException ex) {
				if (!segmentQueue.isEmpty())
					return;
			}
		}
		
		return;
	}
	
	/**
	 * Get the number of k-mers written.
	 * 
	 * @return Number of k-mers written.
	 */
	public long getKmerCount() {
		return kmerCount;
	}
	
	public synchronized Boolean getFlag() {	
		return this.wf;
	}
	
	/**
	 * A container class that manages buffers from each on-disk segment of sorted
	 * k-mers. This class has a public buffer and buffer index which is refreshed
	 * with the next set of k-mers from the <code>load()</code> method.
	 */
	private class SegmentContainer {
		
		/** k-mer buffer this segment container loads. */
		public long[] kmer;
		
		/** k-mer counts where each count[n] is the count of kmer[n]. */
		public int[] count;
		
		/** Index of the current index of <code>buffer</code>. */
		public int index;
		
		/** Number of elements loaded in kmer[] and count[]. */
		public int length;
		
		/** File this container loads. */
		public final File file;
		
		/** Points to the next segment container in a queue. */
		public SegmentContainer fwdLink;
		
		/** Points to the last segment container in a queue. */
		public SegmentContainer revLink;
		
		/** Input stream of the on-disk segment this container loads. */
		private FileInputStream fis;
		
		/** File channel for block I/O on <code>fis</code>. */
		private FileChannel fcin;
		
		/** Buffer for file I/O. */
		private ByteBuffer buf;


		/**
		 * Create a new segment container.
		 * <p/>
		 * The initial buffers will be empty. To load the first set of k-mers, call <code>load()</code>
		 * before reading from the buffers. <code>length</code> will be set to 0 when this constructor
		 * returns.
		 * 
		 * @param segmentFile File to load.
		 * 
		 * @throws NullPointerException If <code>segmentFile</code> is <code>null</code>.
		 * @throws FileNotFoundException If <code>segmentFile</code> cannot be found.
		 * @throws SecurityException If a security error occurs while opening <code>segmentFile</code>.
		 * @throws IOException If an IO error occurs while reading <code>segmentFile</code>.
		 */
		public SegmentContainer(SegmentFile segmentFile)
				throws NullPointerException, FileNotFoundException, SecurityException, IOException {
			
			// Check arguments
			if (segmentFile == null)
				throw new NullPointerException("Cannot create segment container for input file: null");
			
			// Open input stream
			fis = new FileInputStream(segmentFile.file); // throws FileNotFoundException, SecurityException
			fcin = fis.getChannel();
			this.file = segmentFile.file;
			
			// Allocate buffer
			buf = ByteBuffer.allocateDirect(bufRecordCount * (Long.SIZE + Integer.SIZE) / 8);
			buf.clear();
			buf.flip();
			
			// Create structures
			kmer = new long[Constants.DEFAULT_BATCH_SIZE];
			count = new int[Constants.DEFAULT_BATCH_SIZE];
			index = 0;
			length = 0;
			
			fwdLink = null;
			revLink = null;
			
			return;
		}

		/**
		 * Loads the next set of k-mers into the buffer. If the segment is already
		 * depleted, this method has no effect (<code>false</code> is automatically
		 * returned).
		 * 
		 * @return <code>true</code> if at least one k-mer and count was written to the
		 *   buffers kmer[] and count[]. If <code>false</code>, no k-mers were loaded.
		 * 
		 * @throws IOException If an IO error occurs while reading k-mers.
		 */
		public boolean load()
				throws IOException {
			
			length = 0; // Length is used as the index for loading kmer[] and count[] (loading sets length)
			index = 0;  // Reset object index

			if (fis == null) // fis == null is the flag for a depleted segment
				return false;

			// Read k-mer counts until EOF or buffer is full
			while (length < kmer.length) {

				// Check buffer
				if (! buf.hasRemaining()) {

					// Buffer is empty: Read from file
					buf.clear();
					
					if (fcin.read(buf) == -1) {
						
						// Nothing left to read from file.
						close();
						return (length > 0);
					}
					
					buf.flip();
				}
				
				kmer[length] = buf.getLong();
				count[length] = buf.getInt();
				
				++length;
			}

			return true;
		}
		
		/**
		 * Close the input file.
		 * 
		 * @throws IOException If an IO error occurs while closing.
		 */
		private void close()
				throws IOException {
			
			if (fis != null) {
				fis.close(); // throws IOException

				buf = null;
				fis = null;
			}
			
			return;
		}
		
		/**
		 * Close the input file.
		 */
		@Override
		public void finalize() {
			try {
				close();
				
			} catch (Exception ex) {
				; // Ignore
			}
		}
	}


}
