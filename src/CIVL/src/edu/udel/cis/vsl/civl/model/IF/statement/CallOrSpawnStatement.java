/**
 * 
 */
package edu.udel.cis.vsl.civl.model.IF.statement;

import java.util.List;

import edu.udel.cis.vsl.civl.model.IF.CIVLFunction;
import edu.udel.cis.vsl.civl.model.IF.expression.ConditionalExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.Expression;
import edu.udel.cis.vsl.civl.model.IF.expression.FunctionIdentifierExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.LHSExpression;

/**
 * A function call or spawn. Either of the form f(x) or else v=f(x). The
 * function expression is set only once if the function called is known
 * statically.
 * 
 * @author Timothy K. Zirkel (zirkel)
 * 
 */
public interface CallOrSpawnStatement extends Statement {

	/**
	 * Is this a call (not spawn)?
	 * 
	 * @return true iff this is a call
	 */
	boolean isCall();

	/**
	 * Is this a spawn (not call)?
	 * 
	 * @return true iff this is a spawn
	 */
	boolean isSpawn();

	/**
	 * <p>
	 * <b>Pre-condition:</b><code> {@link #isSpawn()} == true </code>
	 * </p>
	 * Is this a spawn for $run statement ?
	 * 
	 * @return true if and only if this is a spawn and it is translated from a
	 *         $run statement.
	 */
	boolean isRun();

	/**
	 * 
	 * @return true iff the {@link #lhs()} expression will be initialized by the
	 *         return value of this call statement, i.e. the function call is an
	 *         initializer of a declaration.
	 */
	boolean isInitializer();

	/**
	 * @return The left hand side expression if applicable. Else null.
	 */
	LHSExpression lhs();

	/**
	 * TODO: get rid of it
	 * 
	 * @return The function being called.
	 */
	CIVLFunction function();

	/**
	 * @return The arguments to the function.
	 */
	List<Expression> arguments();

	/**
	 * @param lhs
	 *            The left hand side expression if applicable. Else null.
	 */
	void setLhs(LHSExpression lhs);

	/**
	 * <p>
	 * <b>Pre-condition:</b><code> {@link #isSpawn()} == true </code>
	 * </p>
	 * Set the statement as a $spawn for $run. i.e. A $spawn statement which is
	 * translated from a $run statement.
	 * 
	 * @param isRun
	 */
	void setAsRun(boolean isRun);

	/**
	 * TODO: get rid of this, but updates the function expression instead.
	 * 
	 * @param function
	 *            The function being called.
	 */
	void setFunction(FunctionIdentifierExpression function);

	/**
	 * @param arguments
	 *            The arguments to the function.
	 */
	void setArguments(List<Expression> arguments);

	/**
	 * Check if the statement is calling a system function.
	 * 
	 * @return
	 */
	boolean isSystemCall();

	Expression functionExpression();

	@Override
	CallOrSpawnStatement replaceWith(ConditionalExpression oldExpression,
			Expression newExpression);
}
