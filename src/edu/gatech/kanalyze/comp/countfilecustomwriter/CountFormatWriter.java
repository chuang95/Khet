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
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import edu.gatech.kanalyze.util.CounterPair;
import edu.gatech.kanalyze.util.KmerUtil;

/**
 * Parent class of all count format writers.
 */
public abstract class CountFormatWriter {

	/** Writer output it sent to. */
	protected FileChannel fc;
	
	/** K-mer size. */
	protected int kSize;
	
	/** Default buffer size. */
	public static final int DEFAULT_BUFFER_SIZE = 50 * 1048576; // 50 M
	
	/** Byte buffer. */
	protected ByteBuffer buf;

	/** Buffer size. */
	protected int bufSize;
	
	/**
	 * Create a new count format writer.
	 * 
	 * @param fc File channel output is written to.
	 * @param kSize K-mer size.
	 * 
	 * @throws NullPointerException If <code>fc</code> is <code>null</code>.
	 * @throws IllegalArgumentException If <code>kSize</code> is invalid.
	 */
	public CountFormatWriter(FileChannel fc, int kSize)
			throws NullPointerException, IllegalArgumentException {
		
		if (fc == null)
			throw new NullPointerException("Cannot create format writer with file channel: null");
		
		if (! KmerUtil.isValidSize(kSize))
			throw new IllegalArgumentException("Cannot create format writer: K-mer size is not valid: " + kSize);
		
		bufSize = DEFAULT_BUFFER_SIZE;
		buf = ByteBuffer.allocateDirect(bufSize);
		
		this.fc = fc;
		this.kSize = kSize;
		
		return;
	}

	/**
	 * Format and write k-mer.
	 * 
	 * @param kmerCount K-mer count.
	 * 
	 * @throws IOException If an IO error occurs while writing.
	 */
	public abstract void write(CounterPair[] kmerCount)
			throws IOException;
	
	/**
	 * Flushes the buffer and writes to disk.
	 * 
	 * @throws IOException If an IO error occurs flushing the buffer.
	 */
	public void flush()
			throws IOException {
		
		buf.flip();
		fc.write(buf);
		buf.clear();
	}
	
	/**
	 * Perform any post-run cleanup tasks.
	 * 
	 * @throws Throwable If any error occurs during cleanup.
	 */
	public void postExec()
			throws Throwable {
		
		return;
	}
}
