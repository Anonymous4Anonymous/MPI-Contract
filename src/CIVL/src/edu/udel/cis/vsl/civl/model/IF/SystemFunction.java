package edu.udel.cis.vsl.civl.model.IF;

/**
 * A system function is a function that is implemented in a library executor,
 * not in source code.
 * 
 * @author zirkel
 * 
 */
public interface SystemFunction extends CIVLFunction {

	/**
	 * 
	 * @return The name of the library containing this system function.
	 */
	String getLibrary();

	/**
	 * 
	 * @param libraryName
	 *            The name of the library containing this system function.
	 */
	void setLibrary(String libraryName);

	/**
	 * returns true iff this system function needs special handling from the
	 * library enabler.
	 * 
	 * @return true iff this system function needs special handling from the
	 *         library enabler.
	 */
	boolean needsEnabler();

}
