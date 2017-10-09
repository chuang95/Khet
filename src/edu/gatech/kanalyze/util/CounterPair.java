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

/**
 * A k-mer count key/value pair.
 */
public class CounterPair {
	
	/** K-mer. */
	public long kmer;
	
	/** K-mer count. */
	public int count;

	/**
	 * Create a new counter pair with kmer = 0 and count = 0.
	 */
	public CounterPair() {
		kmer = 0;
		count = 0;
	}
	
	/**
	 * Create a new counter pair with count = 0;
	 * 
	 * @param kmer K-mer.
	 */
	public CounterPair(long kmer) {
		this.kmer = kmer;
		
		return;
	}
	
	/**
	 * Create a new counter pair.
	 * 
	 * @param kmer K-mer.
	 * @param count Count.
	 */
	public CounterPair(long kmer, int count) {
		this.kmer = kmer;
		this.count = count;
		
		return;
	}
}
