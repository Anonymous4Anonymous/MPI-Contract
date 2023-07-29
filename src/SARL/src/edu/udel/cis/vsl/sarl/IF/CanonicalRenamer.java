package edu.udel.cis.vsl.sarl.IF;

import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;

/**
 * <p>
 * A substituter used to assign new, canonical names to all symbolic constants
 * occurring in a sequence of expressions. This class is provided with a root
 * {@link String}, e.g., "X". Then, as it encounters symbolic constants, it
 * renames them X0, X1, X2, ...., in that order.
 * </p>
 * 
 * <p>
 * The {@link #apply(SymbolicExpression)} method consumes a symbolic expression
 * and produces the expression obtained by replacing symbolic constants whose
 * names begin with the root string with the new renumbered version.
 * </p>
 * 
 * @author Stephen F. Siegel
 */
public interface CanonicalRenamer extends UnaryOperator<SymbolicExpression> {

	/**
	 * Returns the number of new (post-canonicalized) symbolic constants at the
	 * current time. Initially, this will be 0, but as applied is repeatedly
	 * called and new symbolic constants are created, this number will grow.
	 * 
	 * @return current number of new symbolic constants
	 */
	int getNumNewNames();

}
