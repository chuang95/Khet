package edu.gatech.khet;

import edu.gatech.khet.Constants;


public class KhetError {
	
	/** A message describing this error. */
	public final String message;
	
	/** A throwable object that caused this error. */
	public final Throwable cause;
	
	/**
	 * A return code that indicates what this message relates to. This should
	 * be one of the &quot;ERR_&quot; constants defined in <code>Constants</code>,
	 * and it is never <code>Constants.ERR_NONE</code>.
	 */
	public final int returnCode;
	
	/** Type code. */
	public final int type;
	
	/** Indicates a warning type. */
	public static final int TYPE_WARNING = 0;
	
	/** Indicates an error type. */
	public static final int TYPE_ERROR = 1;
	
	public KhetError(String message, int returnCode, Throwable cause, int type)
			throws NullPointerException, IllegalArgumentException {
		
		// Check arguments
		if (message == null)
			throw new NullPointerException("Cannot create Khet warning: Message is null");
		
		message = message.trim();
		
		if (message.isEmpty())
			throw new IllegalArgumentException("Cannot create Khet warning: Message is empty");
		
		if (returnCode == Constants.ERR_NONE)
			throw new IllegalArgumentException("Cannot create Khet warning: Return code must not be " + Constants.ERR_NONE);
		
		if (type != TYPE_WARNING && type != TYPE_ERROR)
			throw new IllegalArgumentException("Cannot create Khet warning: Type code is invalid: " + type);
		
		this.message = message;
		this.cause = cause;
		this.returnCode = returnCode;
		this.type = type;
		
		return;
	}
	
	/**
	 * Get the return code associated with this error.
	 * 
	 * @return Return code associated with this error.
	 */
	public int getReturnCode() {
		return returnCode;
	}
	
	/**
	 * Get the type code associated with this error.
	 * 
	 * @return Type code associated with this error.
	 */
	public int getType() {
		return type;
	}
	
	/**
	 * Convenience method to determine if this error is a fatal error.
	 * 
	 * @return <code>true</code> if this error is a fatal error.
	 */
	public boolean isError() {
		return (type == TYPE_ERROR);
	}
	
	/**
	 * Convenience method to determine if this error is a warning.
	 * 
	 * @return <code>true</code> if this error is a warning.
	 */
	public boolean isWarning() {
		return (type == TYPE_WARNING);
	}
	
	/**
	 * Get a string version of this error.
	 * 
	 * @return String version of this error.
	 */
	@Override
	public String toString() {
		return "Error: " + message;
	}

	/**
	 * Fire an error message.
	 * 
	 * @param message Message text.
	 * @param returnCode Code to indicate what this message is related to. This
	 *   should be one of the &quot;ERR_&quot; constants defined in
	 *   <code>Constants</code>, and it must not be
	 *   <code>Constants.ERR_NONE</code>.
	 * @param cause Throwable that caused this error. If no throwable
	 *   is associated with this error, this argument is <code>null</code>.
	 * 
	 * @throws NullPointerException If <code>message</code> is <code>null</code>.
	 * @throws IllegalArgumentException If <code>message</code> is empty or
	 *   <code>returnCode</code> is <code>Constants.ERR_NONE</code>.
	 * 
	 * @see edu.gatech.Khet.Constants
	 */
	protected static void error(String message, int returnCode, int type)
			throws NullPointerException, IllegalArgumentException {
		Throwable cause = new IllegalArgumentException(message);
		KhetError errorobjet = new KhetError(message, returnCode, cause, type); // throws NullPointerException, IllegalArgumentException
		System.out.println(errorobjet.toString());
		return;
	}
	
	/**
	 * Fire an error message.
	 * 
	 * @param message Message text.
	 * @param returnCode Code to indicate what this message is related to. This
	 *   should be one of the &quot;ERR_&quot; constants defined in
	 *   <code>Constants</code>, and it must not be
	 *   <code>Constants.ERR_NONE</code>.
	 * 
	 * @throws NullPointerException If <code>message</code> is <code>null</code>.
	 * @throws IllegalArgumentException If <code>message</code> is empty or
	 *   <code>returnCode</code> is <code>Constants.ERR_NONE</code>.
	 * 
	 * @see edu.gatech.Khet.Constants
	 */
	protected void error(String message, int returnCode) {
		error(message, returnCode);	
		return;
	}
}
