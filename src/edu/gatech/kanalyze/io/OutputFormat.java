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

package edu.gatech.kanalyze.io;

/**
 * Format of the final output.
 */
public enum OutputFormat {
	
	/** K-mer sequence. */
	SEQ,
	
	/** K-mer integer. */
	INT,
	
	/** K-mer integer as hex string. */
	HEX;

	/**
	 * Get the output by name.
	 * 
	 * @param formatName Name of the output format.
	 * 
	 * @return The output format if found, and <code>null</code> if
	 *   not found or if <code>formatName</code> is <code>null</code>.
	 */
	public static OutputFormat getFormat(String formatName) {
		
		if (formatName == null || formatName.isEmpty())
			return null;
		
		formatName = formatName.toUpperCase();
		
		for (OutputFormat fmt : OutputFormat.values())
			if (fmt.toString().equals(formatName))
				return fmt;
		
		return null;
	}
}
