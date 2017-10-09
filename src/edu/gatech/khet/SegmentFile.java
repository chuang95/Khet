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

package edu.gatech.khet;

import java.io.File;

/**
 * Represents a segment file and attributes.
 */
public class SegmentFile {

	/** Segment file. */
	public final File file;
	
	/**
	 * <code>MergeComponent</code> will automatically delete the file
	 * if this flag is <code>true</code>.
	 */
	public boolean autoDelete;

	/**
	 * Create a new segment file.
	 * 
	 * @param file Segment file.
	 * @param autoDelete Auto delete flag.
	 * 
	 * @throws NullPointerException If <code>file</code> is <code>null</code>.
	 */
	public SegmentFile(File file, boolean autoDelete)
			throws NullPointerException {
		
		if (file == null)
			throw new NullPointerException("Cannot create segment file for file reference: null");
		
		this.file = file;
		this.autoDelete = autoDelete;
		
		return;
	}
	
	/**
	 * Create a new segment file. Set the auto-delete flag to <code>false</code>.
	 * 
	 * @param file Segment file.
	 * 
	 * @throws NullPointerException If <code>file</code> is <code>null</code>.
	 */
	public SegmentFile(File file)
			throws NullPointerException {
		
		this(file, false);
	}
}
