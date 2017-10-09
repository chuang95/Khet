
package edu.gatech.khet;

/**
 * A class of constants used by the entire k-mer analyzer project.
 */
public class Constants {
	
	//
	// Version
	//
	
	/** Major version number. */
	public static final int VERSION_MAJOR = 0;
	
	/** Minor version number. */
	public static final int VERSION_MINOR = 9;
	
	/** Revision version numbers. */
	public static final int VERSION_REVISION = 5;
	
	/** Full version string. */
	public static final String VERSION = VERSION_MAJOR + "." + VERSION_MINOR + "." + VERSION_REVISION;

	// Return codes
	//
	// Constants starting with "ERR_" will be returned by main () when a
	// program exits. Non-zero return codes indicate an error, and scripts
	// or the command line can use that return code to determine if the
	// command completed successfully.

	/** Error: None. No error occurred. */
	public static final int ERR_NONE = 0;

	/** Indicates a usage error. An argument to the program is incorrect. */
	public static final int ERR_USAGE = 1;

	/** Indicates an I/O error. Reading or writing data failed. */
	public static final int ERR_IO = 2;
	
	/** Indicates a security or permissions error. */
	public static final int ERR_SECURITY = 3;

	/** Indicates that a specified file was not found. */
	public static final int ERR_FILENOTFOUND = 4;

	/**
	 * Indicates that data was malformed. For example, if a data file is corrupt
	 * or invalid, this error would be returned.
	 */
	public static final int ERR_DATAFORMAT = 5;

	/** Analysis was not able to complete for some reason. */
	public static final int ERR_ANALYSIS = 6;
	
	/** Indicats a thread was interrupted before it completed. */
	public static final int ERR_INTERRUPTED = 7;
	
	/** An unexpected system error occurred. These should be reported as a bug. */
	public static final int ERR_SYSTEM = 99;

	//
	// Limits
	//

	/**
	 * Kmer size limit. This software can process kmers between 1 and this
	 * number (inclusive).
	 */
	public static final int LIMIT_KSIZE = 31;
	
	/**
	 * number of thread limit. This software recommand number of threads between 1 and this
	 * number (inclusive).
	 */
	public static final int LIMIT_NT = 10;

	//
	// Global defaults
	//

	/** Default k-mer size an application should use if no size is specified. */
	public static final int DEFAULT_KSIZE = LIMIT_KSIZE;
	
	/** Default size of queues. */
	public static final int DEFAULT_QUEUE_SIZE = 100;
	
	/** Default batch size. Batches of elements are written to queues. */
	public static final int DEFAULT_BATCH_SIZE = 2000;
	
	/** Default segment size. */
	public static final String DEFAULT_SEG_SIZE = "65536000";
	
	/** Default lower threshold for kmer counts */
	public static final int DEFAULT_LOWER_THRESHOLD = 0;
	
	/** Default upper threshold for kmer counts */
	public static final int DEFAULT_UPPER_THRESHOLD = 50000000;
	
	/** Default database name. */
	public static final String DEFAULT_DB_NAME = "Khet";
	
	/** Default database host. */
	public static final String DEFAULT_DB_HOST = "localhost";


	//
	// Global properties. These properties control module and component behavior.
	//
	
	/**
	 * Output format of k-mers when they are written to a file. Values should correspond
	 * to values in enumeration <code>OutputFormat</code>.
	 */
	public static final String PROP_OUTFMT = "Khet.outfmt";
	
	
	//
	// Resource Locator Constants
	//
	
	/** Root of all resources. */
	public static final String RESOURCE_ROOT = "edu/gatech/Khet";
	
	/** Location of test resources and files. */
	public static final String RESOURCE_TEST = RESOURCE_ROOT + "/test";
	
	/** Resource for retrieving the Khet icon (16x16). The icon only has the K (no text). */
	public static final String RESOURCE_ICON_16 = "/edu/gatech/Khet/img/logo/kanlogo_nt_16.png";
	
	/** Resource for retrieving the Khet logo (128x128). */
	public static final String RESOURCE_LOGO_128 = "/edu/gatech/Khet/img/logo/kanlogo_128.png";
	
	
	//
	// Patterns
	//
	
	/** Pattern of valid nucelotide sequences. */
	public static final String NUCL_PATTERN = "[ACGTRYKMSWBDHVNUXacgtrykmswbdhvnux.-]+";
	
	/** Pattern of valid format types (e.g. "FASTA", "RAW"). */
	public static final String FORMAT_TYPE_PATTERN = "[A-Za-z0-9_]+";
	
	
	//
	// Known formats and types
	//
	
	/** Sequence formats shipped with Khet. */
	public static final String[] BUILTIN_SEQUENCE_FORMATS = new String[] {
		"raw",
		"fasta",
		"fastq",
		"fastagz",
		"fastqgz"
	};
	
	/** Library used to annotate snps. */
	public static final String LIBRARYA_FILE = "part.sdb";
	
	//
	// Copyright string
	//
	
	/** Copyright string. */
	public static final String COPYRIGHT =
		"Copyright (c) 2014 Peter A. Audano III\n" +
		"This program is free software; you can redistribute it and/or modify\n" +
		"it under the terms of the GNU Lesser General Public License as published\n" +
		"by the Free Software Foundation; either version 3 of the License or\n" +
		"(at your option) any later version.\n" +
		"\n" +
		"This program is distributed in the hope that it will be useful, but\n" +
		"WITHOUT ANY WARRANTY; without even the implied warranty of\n" +
		"MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n" +
		"GNU Library General Public License for more details.\n" +
		"You should have received a copy of the GNU Lesser General Public License\n" +
		"along with this program; see the file COPYING.LESSER.  If not, see\n" +
		"<http://www.gnu.org/licenses/>\n";
}
