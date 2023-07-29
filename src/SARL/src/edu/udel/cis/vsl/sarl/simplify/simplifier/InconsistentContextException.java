package edu.udel.cis.vsl.sarl.simplify.simplifier;

/**
 * Thrown when it has been determined that a @{link Context} is inconsistent,
 * i.e., equivalent to <code>false</code>.
 * 
 * @author xxxx
 *
 */
public class InconsistentContextException extends Exception {

	/**
	 * Randomly generated unique ID.
	 */
	private static final long serialVersionUID = -2910681509456717916L;

	/**
	 * Creates a new instance. There is no data associated to this exception.
	 */
	public InconsistentContextException() {
	}

}
