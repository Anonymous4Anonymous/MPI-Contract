package edu.udel.cis.vsl.sarl.simplify.simplifier;

import java.io.PrintStream;

import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;

/**
 * A context is an abstract representation of a boolean expression suitable for
 * normalizing (simplifying) other expressions under the assumption that the
 * context holds. This interface for a context is under construction and is
 * currently not used.
 * 
 * @author xxxx
 *
 */
public interface ContextIF {

	/**
	 * Adds the given assumption to this context.
	 * 
	 * @param expr
	 *            a boolean expression (non-{@code null}) representing an
	 *            assumption to add to this context
	 */
	void assume(BooleanExpression expr);

	/**
	 * Places this context into a normal form.
	 * 
	 * Postcondition: the dirty set will be empty. The substitution map and
	 * range map will be in normal form.
	 */
	void normalize();

	/**
	 * Is this {@link Context} inconsistent, i.e., is the assumption it
	 * represents equivalent to "false"?
	 * 
	 * @return <code>true</code> if this context is known to be inconsistent. A
	 *         return value of <code>true</code> implies the context is
	 *         inconsistent; a return value of <code>false</code> means the
	 *         context may or may not be inconsistent.
	 */
	boolean isInconsistent();

	/**
	 * Returns the reduced assumption represented by this {@link Context}. That
	 * means it does not include the equations of the form x=e, where x is a
	 * solved symbolic constant. The related method {@link #getFullAssumption()}
	 * returns the conjunction of this reduced assumption with those equations.
	 * 
	 * @return the reduced assumption
	 */
	BooleanExpression getReducedAssumption();

	/**
	 * Returns the full assumption represented by this {@link Context}. This
	 * means the assumption will include the clauses which are equations of the
	 * form "x=e" where x is a solved {@link SymbolicConstant}.
	 * 
	 * If this is a sub-context, this assumption does not include the assumption
	 * of its super-context.
	 * 
	 * @return the full assumption
	 */
	BooleanExpression getFullAssumption();

	/**
	 * Prints this {#link Context} is a human-readable multi-line format.
	 * 
	 * @param out
	 *            the stream to which to print
	 */
	void print(PrintStream out);

	ContextIF clone();

}
