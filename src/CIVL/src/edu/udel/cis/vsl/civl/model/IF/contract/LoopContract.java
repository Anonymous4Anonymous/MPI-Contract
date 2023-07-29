package edu.udel.cis.vsl.civl.model.IF.contract;

import edu.udel.cis.vsl.civl.model.IF.Sourceable;
import edu.udel.cis.vsl.civl.model.IF.expression.Expression;
import edu.udel.cis.vsl.civl.model.IF.expression.LHSExpression;
import edu.udel.cis.vsl.civl.model.IF.location.Location;

/**
 * This class represents a group of loop annotations for a loop, including loop
 * invariants, loop assigns and loop variants.
 * 
 * @author xxxx
 *
 */
public interface LoopContract extends Sourceable {
	/**
	 * Returns the location which identifies the corresponding loop.
	 * 
	 * @return the location which identifies the corresponding loop.
	 */
	Location loopLocation();

	/**
	 * Returns an array of loop invariants specified for this loop.
	 * 
	 * @return An array of expression whose type must be bool; Empty array if no
	 *         loop invariants specified.
	 */
	Expression[] loopInvariants();

	/**
	 * Returns an array of loop assigns specified for this loop.
	 * 
	 * @return An array of left-hand side expressions;Empty array if no loop
	 *         assigns specified.
	 */
	LHSExpression[] loopAssigns();

	/**
	 * Returns an array of loop variants specified for this loop.
	 * 
	 * @return An array of loop variants; Empty array if no loop variants
	 *         specified.
	 */
	Expression[] loopVariants();

	/**
	 * Set the location which identifies a loop statement.
	 * 
	 * @param loopLocation
	 */
	void setLocation(Location loopLocation);
}
