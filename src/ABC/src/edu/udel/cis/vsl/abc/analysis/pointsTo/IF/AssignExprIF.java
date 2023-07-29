package edu.udel.cis.vsl.abc.analysis.pointsTo.IF;

import edu.udel.cis.vsl.abc.ast.type.IF.Type;

/**
 * <p>
 * An instance of this class represents an abstract object. We define an
 * abstract object U inductively: <code>
 * U := variable | allocation | string  
 *   | U.id      
 *   | U + (c|*)
 *   | U[c|*]
 *   | auxiliary
 *   | FULL
 * </code>, where "c|*" represents either a integral constant or an arbitrary
 * integer; "auxiliary" represents a temporary abstract object that DOES NOT
 * associated with any actual program object.; "FULL" represents a ponter that
 * may points to any possible object
 * </p>
 * 
 * <p>
 * An invariant: for <code>U + (c|*)</code>, <code>U</code> must NOT have the
 * form of <code>U + (c|*)</code>
 * </p>
 * 
 * @author xxxx
 *
 */
public interface AssignExprIF {

	static public enum AssignExprKind {
		/**
		 * represents an actual program objects: it is either a variable, an
		 * allocated object or a string literal
		 */
		STORE,
		/**
		 * represents the struct/union field of an abstract object
		 */
		FIELD,
		/**
		 * represents an array element of an abstract object
		 */
		SUBSCRIPT,
		/**
		 * represents a abstract object with an integral offset
		 */
		OFFSET,
		/**
		 * represents an auxiliary abstract object that is associated with no
		 * actual program objects.
		 */
		AUX,
	}

	/**
	 * 
	 * @return the kind of this {@link AssignExprIF} or null if this is instance
	 *         represents "FULL" (i.e., {@link #isFull()})
	 */
	AssignExprKind kind();

	/**
	 * 
	 * @return true iff this is an abstract object that represents a pointer may
	 *         points to any object
	 */
	boolean isFull();

	/**
	 * 
	 * @return the type of this AssignExprIF
	 */
	Type type();

	/**
	 * 
	 * @return the root of this abstract object, it is either a STORE kind, an
	 *         AUX kind instance or an instance representing FULL
	 */
	AssignExprIF root();

	/**
	 * 
	 * @param e
	 * @return true either e generalizes this instance or this instance
	 *         generalizes e
	 */
	boolean mayEquals(AssignExprIF e);
}
