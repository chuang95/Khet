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

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.channels.FileChannel;

import edu.gatech.kanalyze.util.CounterPair;

/**
 * Formats output as sequences and writes.
 */
public class INTFormatWriter extends CountFormatWriter {
	
	/** Output format string. */
	private String fmtStr;
	
	/** String buffer. */
	private String strBuf;

	/**
	 * Create a new format writer.
	 * 
	 * @param fc File channel output is written to.
	 * @param kSize K-mer size.
	 * 
	 * @throws NullPointerException If <code>fc</code> is <code>null</code>.
	 * @throws IllegalArgumentException If <code>kSize</code> is invalid.
	 */
	public INTFormatWriter(FileChannel fc, int kSize)
			throws NullPointerException {
		
		super (fc, kSize); // throws NullPointerException, IllegalArgumentException
		
		// Calculate the number of digits required to store the largest k-mer
		int cellSize = (int) Math.floor(Math.log10(Math.pow(4, kSize))) + 1;
		fmtStr = "%0" + cellSize + "d\t%d\n";
		
		return;
	}
	
	/**
	 * Format and write k-mer.
	 * 
	 * @param kmerCount K-mer count.
	 * 
	 * @throws IOException If an IO error occurs while writing.
	 */
	@Override
	public void write(CounterPair[] kmerCount)
			throws IOException {
		
		for (CounterPair pair : kmerCount) {
			strBuf = String.format(fmtStr, pair.kmer, pair.count);
			
			try {
				buf.put(strBuf.getBytes());
				
			} catch (BufferOverflowException ex) {
				
				// Write to disk when buffer overflows
				flush();
				
				try {
					buf.put(strBuf.getBytes());
					
				} catch (BufferOverflowException ex2) {
					throw new IOException("Cannot write string after flushing buffer: " + ex2.getMessage(), ex2);
				}
			}
		}
		
		return;
	}
	
	/**
	 * Perform any post-run cleanup tasks.
	 * 
	 * @throws Throwable If any error occurs during cleanup.
	 */
	@Override
	public void postExec()
			throws Throwable {
		
		fmtStr = null;
		strBuf = null;
		
		return;
	}
}
