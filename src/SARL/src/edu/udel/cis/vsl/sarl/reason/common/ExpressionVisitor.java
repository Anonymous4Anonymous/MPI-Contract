package edu.udel.cis.vsl.sarl.reason.common;

import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject.SymbolicObjectKind;
import edu.udel.cis.vsl.sarl.preuniverse.IF.PreUniverse;

/**
 * This class visits all child expressions of the given expression in a
 * Depth-first order. For each visited expression e,
 * {@link StatelessSimplificationAction} will be performed before and after e's
 * children being visited.
 * 
 * @author xxxx
 *
 */
abstract class ExpressionVisitor {
	/**
	 * A reference to a {@link PreUniverse}.
	 */
	protected PreUniverse universe;

	ExpressionVisitor(PreUniverse universe) {
		this.universe = universe;
	}

	/**
	 * Visit the expression in a Depth-first order and performing
	 * {@link StatelessSimplificationAction}s on them.
	 * 
	 * @param expr
	 * @param action
	 * @return
	 */
	abstract SymbolicExpression visitExpression(SymbolicExpression expr);

	protected SymbolicExpression visitExpressionChildren(
			SymbolicExpression expr) {
		boolean changed = false;
		int numArgs = expr.numArguments();
		SymbolicObject newArgs[] = new SymbolicObject[numArgs];

		for (int i = 0; i < numArgs; i++) {
			SymbolicObject arg = expr.argument(i);

			if (arg.symbolicObjectKind() == SymbolicObjectKind.EXPRESSION) {
				newArgs[i] = visitExpression((SymbolicExpression) arg);
				if (newArgs[i] != arg)
					changed = true;
			} else
				newArgs[i] = arg;
		}
		if (changed)
			return universe.make(expr.operator(), expr.type(), newArgs);
		else
			return expr;
	}
}
