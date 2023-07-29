package edu.udel.cis.vsl.civl.library.civlc;

import java.util.Stack;

import edu.udel.cis.vsl.sarl.IF.Reasoner;
import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;
import edu.udel.cis.vsl.sarl.IF.UnaryOperator;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;

public class ConditionalSimplification extends ExpressionVisitor
		implements
			UnaryOperator<SymbolicExpression> {

	private Reasoner reasoner;

	private Stack<Condition> conditions;

	private class Condition {
		boolean negate;

		BooleanExpression condition;

		Condition(boolean negate, BooleanExpression condition) {
			this.negate = negate;
			this.condition = condition;
		}
	}

	ConditionalSimplification(SymbolicUniverse universe) {
		super(universe);
		this.conditions = new Stack<>();
		this.reasoner = universe.reasoner(universe.trueExpression());
	}

	@Override
	public SymbolicExpression apply(SymbolicExpression x) {
		return visitExpression(x);
	}

	@Override
	SymbolicExpression visitExpression(SymbolicExpression expr) {
		if (expr.operator() == SymbolicOperator.COND)
			return visitConditionalExpression(expr);
		return this.visitExpressionChildren(expr);
	}

	private SymbolicExpression visitConditionalExpression(
			SymbolicExpression condExpr) {
		BooleanExpression cond = (BooleanExpression) condExpr.argument(0);
		SymbolicExpression truB = (SymbolicExpression) condExpr.argument(1);
		SymbolicExpression flsB = (SymbolicExpression) condExpr.argument(2);
		BooleanExpression conjunc = universe.trueExpression();
		Condition sameCond = null;

		for (Condition c : conditions)
			if (c.condition == cond) {
				sameCond = c;
				break;
			} else
				conjunc = universe.and(conjunc,
						c.negate ? universe.not(c.condition) : c.condition);

		if (sameCond != null)
			if (sameCond.negate)
				return visitExpression(flsB);
			else
				return visitExpression(truB);

		BooleanExpression simplified;

		// attempt to simplify:
		simplified = universe.and(conjunc, cond);
		simplified = reasoner.simplify(simplified);
		if (simplified.isTrue())
			return visitExpression(truB);
		if (simplified.isFalse())
			return visitExpression(flsB);

		// cannot simpliy:
		SymbolicExpression newTruB, newFlsB;

		conditions.push(new Condition(false, cond));
		newTruB = visitExpression(truB);
		conditions.pop();

		conditions.push(new Condition(true, cond));
		newFlsB = visitExpression(flsB);
		conditions.pop();

		if (newTruB != truB || newFlsB != flsB)
			return universe.cond(cond, newTruB, newFlsB);
		return condExpr;
	}
}
