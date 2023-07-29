package edu.udel.cis.vsl.civl.library.civlc;

import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject.SymbolicObjectKind;

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
	 * A reference to a {@link SymbolicUniverse}.
	 */
	protected SymbolicUniverse universe;

	/**
	 * The action that can be performed on the expression when it is visited by
	 * the {@link ExpressionVisitor}. Two kinds of actions can be implemented:
	 * <code>pre:</code> the action should be performed before the children of
	 * the expression being visited and <code>post:</code> the action should be
	 * performed after the children of the expression being visited.
	 * 
	 * @author xxxx
	 *
	 */
	interface StatelessSimplificationAction {
		/**
		 * the action should be performed before the children of the expression
		 * being visited
		 * 
		 * @param expr
		 * @return
		 */
		SymbolicExpression pre(SymbolicExpression expr);

		/**
		 * the action should be performed after the children of the expression
		 * being visited
		 * 
		 * @param expr
		 * @return
		 */
		SymbolicExpression post(SymbolicExpression expr);
	}

	ExpressionVisitor(SymbolicUniverse universe) {
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

	public SymbolicExpression visitExpressionChildren(SymbolicExpression expr) {
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
