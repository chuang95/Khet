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

package edu.gatech.kanalyze.util;

import edu.gatech.khet.Constants;

/**
 * A class of k-mer utilities.
 */
public class KmerUtil {
	
	/** Size of k-mers this utility works with. */
	public final int kSize;
	
	/**
	 * Create a new k-mer utility.
	 * 
	 * @param kSize Size of k-mers this utility will work on.
	 * 
	 * @throws IllegalArgumentException If <code>kmer</code> or <code>kSize</code>
	 *   is out of bounds (must be between 1 and <code>Constants.LIMIT_KSIZE</code>,
	 *   inclusive).
	 *   
	 * @see edu.gatech.kanalyze.Constants#LIMIT_KSIZE
	 */
	public KmerUtil(int kSize)
			throws IllegalArgumentException {
		
		if (kSize < 0 || kSize > Constants.LIMIT_KSIZE)
			throw new IllegalArgumentException("k-mer size is out of bounds (1 <= size <= " + Constants.LIMIT_KSIZE + "): " + kSize);
		
		this.kSize = kSize;
		
		return;
	}
	
	/**
	 * Determine if a k-mer size is valid. A k-mer size must be greater than
	 * 0, and less than or equal to <code>Constants.LIMIT_KSIZE</code>.
	 * 
	 * @param kSize K-mer size to check.
	 * 
	 * @return <code>true</code> if <code>kSize</code> is a valid k-mer size.
	 */
	public static boolean isValidSize(int kSize) {
		return kSize > 0 &&
			   kSize <= Constants.LIMIT_KSIZE;
	}

	/**
	 * Convert a k-mer represented as a long integer to a string. 
	 * 
	 * @param kmer K-mer to convert.
	 * 
	 * @return String representation of <code>kmer</code>
	 *  
	 * @throws IllegalArgumentException If <code>kmer</code> is negative.
	 */
	public String toString(long kmer)
			throws IllegalArgumentException {

		// A single base in binary format
		int base;
		
		// A string builder
		StringBuilder builder;
		
		// Verify arguments
		if (kmer < 0)
			throw new IllegalArgumentException("Cannot convert k-mer to string: k-mer is negative: " + kmer);
		
		// Initialize
		builder = new StringBuilder();
		
		// Step through each base and add to the sequence string (in builder)
		for (int count = kSize - 1; count >= 0; --count) {
			
			base = (int) ((kmer & (0x3L << (count * 2))) >>> (count * 2));
			
			switch (base) {
			case 0:
				builder.append('A');
				break;
				
			case 1:
				builder.append('C');
				break;
				
			case 2:
				builder.append('G');
				break;
				
			case 3:
				builder.append('T');
				break;
			}
		}
		
		return builder.toString();
	}
	
	/**
	 * Convert a string to a k-mer integer. Whitespace is removed from the
	 * beginning and the end of the string before converting. The case of the
	 * string does not matter.
	 * 
	 * @param str String to convert.
	 * 
	 * @return Integer representation of <code>str</code>
	 * 
	 * @throws NullPointerException If <code>str</code> is <code>null</code>.
	 * @throws IllegalArgumentException If <code>str</code> does not contain
	 *   exactly the same number of k-mers as the k-mer size this utility
	 *   object was initialized with, or if the string contains characters
	 *   other than A, C, G, T, or U.
	 */
	public long toKmer(String str)
			throws NullPointerException, IllegalArgumentException {
		
		// List of characters in the string
		char[] charList;
		
		// K-mer
		long kmer = 0;
		
		// Check arguments
		if (str == null)
			throw new NullPointerException("Cannot convert string to k-mer: null");
		
		// Get list and check
		charList = str.trim().toUpperCase().toCharArray();
		
		if (charList.length != kSize)
			throw new IllegalArgumentException("K-mer string does not match the k-mer size (" + kSize + "): " + str);
		
		// Convert
		for (int count = 0; count < kSize; ++count) {
			
			switch (charList[count]) {
			case 'A':
				// Nothing to do, A = 0
				break;
				
			case 'C':
				kmer |= (long) 0x1 << ((kSize - count - 1) * 2);
				break;
				
			case 'G':
				kmer |= (long) 0x2 << ((kSize - count - 1) * 2);
				break;
				
			case 'T':
			case 'U':
				kmer |= (long) 0x3 << ((kSize - count - 1) * 2);
				break;
				
			default:
				throw new IllegalArgumentException("K-mer string contains an unrecognized character: " + charList[count]);
			}
		}
		
		return kmer;
	}
	
	/**
	 * Reverse complements a k-mer. The order of the bases are reversed, and each
	 * is replaced with its complementary base (A with T, C with G).
	 * 
	 * @param kmer K-mer to complement.
	 * 
	 * @return Reverse complement of <code>kmer</code>.
	 */
	public long revComplement(long kmer) {
		
		long revCompl = 0; // Final reverse-complemented k-mer
		
		final long bitMask2 = 0x3; // Mask for lowest the two bits.
		
		revCompl = 0;
		
		kmer = ~kmer; // Complement
		
		// Reverse
		for (int count = 0; count < kSize; ++count) {
			revCompl = revCompl << 2 | kmer & bitMask2;
			
			kmer >>= 2;
		}
		
		return revCompl;
	}
	
	/**
	 * Reverse complements a k-mer and return the lesser of the k-mer or its
	 * complement. The lesser k-mer is defined as the one that comes first
	 * in ASCII sort order.
	 * <p/>
	 * The order of the bases are reversed, and each is replaced
	 * with its complementary base (A with T, C with G).
	 * 
	 * @param kmer K-mer to complement.
	 * 
	 * @return Reverse complement of <code>kmer</code>, or <code>kmer</code>, whichever
	 *   is less.
	 */
	public long revComplementLesser(long kmer) {
		
		long revCompl = 0;   // Final reverse-complemented k-mer
		long noCompl = kmer; // Store original
		
		final long bitMask2 = 0x3; // Mask for lowest the two bits.
		
		revCompl = 0;
		
		
		kmer = ~kmer; // Complement
		
		// Reverse
		for (int count = 0; count < kSize; ++count) {
			revCompl = revCompl << 2 | kmer & bitMask2;
			
			kmer >>= 2;
		}
		
		return (revCompl < noCompl) ? revCompl : noCompl;
	}
}
