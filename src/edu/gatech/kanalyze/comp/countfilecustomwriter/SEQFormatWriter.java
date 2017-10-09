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
import java.nio.channels.FileChannel;

import edu.gatech.kanalyze.util.CounterPair;

/**
 * Formats output as sequences and writes.
 */
public class SEQFormatWriter extends CountFormatWriter {
	
	/**
	 * Maximum buffer capacity before forcing <code>flush()</code>. This capacity
	 * prevents buffer overrun while writing records.
	 */
	private int maxCapacity;

	/** Tab character as a byte. */
	private static final byte tabByte = (byte) '\t';
	
	/** Newline character as a byte. */
	private static final byte nlByte = (byte) '\n';
	
	/** Newline character as a byte. */
	private static final byte slByte = (byte) '/';
	
	/** K-mer string as a byte array. */
	private byte[] kmerBytes;
	
	/** K-mer count as a byte array. */
	private byte[] countBytes;
	
	private int lowerThreshold;
	
	/**
	 * Create a new format writer.
	 * 
	 * @param fc File channel output is written to.
	 * @param kSize K-mer size.
	 * 
	 * @throws NullPointerException If <code>fc</code> is <code>null</code>.
	 * @throws IllegalArgumentException If <code>kSize</code> is invalid.
	 */
	public SEQFormatWriter(FileChannel fc, int kSize, int lowerThreshold)
			throws NullPointerException, IllegalArgumentException {
		
		super (fc, kSize); // throws NullPointerException, IllegalArgumentException
		
		maxCapacity = bufSize - (kSize + 10);
		this.lowerThreshold = lowerThreshold;
		
		kmerBytes = new byte[kSize+2];
		countBytes = new byte[((int) Math.log10(Integer.MAX_VALUE)) + 1];
		
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
		
		int countIndex;
		
		long preKmer = -1;
		//int[] ca = new int[4];
		
		for (int index = 0; index < kmerCount.length; ++index) {
			
			if (buf.position() >= maxCapacity)
				flush();
			
			//if(kmerCount[index].count<lowerThreshold || kmerCount[index].count>=1000)
			//{
			//	continue;
			//}
			
			if((kmerCount[index].kmer>>2) != preKmer)
			{
				preKmer=(kmerCount[index].kmer>>2);
				for (int baseIndex = kSize + 1; baseIndex >= 0; --baseIndex) {
					switch((int) kmerCount[index].kmer & 0x3) {
					case 0:
						kmerBytes[baseIndex] = (byte) 'A';
						break;
						
					case 1:
						kmerBytes[baseIndex] = (byte) 'C';
						break;
						
					case 2:
						kmerBytes[baseIndex] = (byte) 'G';
						break;
						
					default:
						kmerBytes[baseIndex] = (byte) 'T';
					}
					
					if(baseIndex == kSize + 1)
					{
						--baseIndex;
						kmerBytes[baseIndex] = tabByte;
					}
					
					if(baseIndex == kSize/2+1)
					{
						--baseIndex;
						kmerBytes[baseIndex] = tabByte;
					}
					
					kmerCount[index].kmer >>= 2;
				}
				
				countIndex = countBytes.length - 1;
				while (kmerCount[index].count > 0) {
					countBytes[countIndex--] = (byte) ('0' + kmerCount[index].count % 10);
					kmerCount[index].count /= 10;
				}
				
				if (countIndex == countBytes.length - 1) {
					countBytes[countIndex--] = '0';
				}
			
				buf.put(nlByte);
				buf.put(kmerBytes);
				buf.put(slByte);
				buf.put(countBytes, countIndex + 1, countBytes.length - countIndex - 1);
				buf.put(tabByte);
				
			}
			else
			{
				int baseIndex = kSize + 1;
				switch((int) kmerCount[index].kmer & 0x3) {
				case 0:
					kmerBytes[baseIndex] = (byte) 'A';
					break;
					
				case 1:
					kmerBytes[baseIndex] = (byte) 'C';
					break;
					
				case 2:
					kmerBytes[baseIndex] = (byte) 'G';
					break;
					
				default:
					kmerBytes[baseIndex] = (byte) 'T';
				}
				
				
				kmerCount[index].kmer >>= 2;

				
				countIndex = countBytes.length - 1;
				while (kmerCount[index].count > 0) {
					countBytes[countIndex--] = (byte) ('0' + kmerCount[index].count % 10);
					kmerCount[index].count /= 10;
				}
				
				if (countIndex == countBytes.length - 1) {
					countBytes[countIndex--] = '0';
				}
			
				buf.put(kmerBytes[baseIndex]);
				buf.put(slByte);
				buf.put(countBytes, countIndex + 1, countBytes.length - countIndex - 1);
				buf.put(tabByte);
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
		
		kmerBytes = null;
		countBytes = null;
		
		return;
	}
}
