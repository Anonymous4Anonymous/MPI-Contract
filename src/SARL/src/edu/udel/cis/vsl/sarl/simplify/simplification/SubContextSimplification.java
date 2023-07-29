package edu.udel.cis.vsl.sarl.simplify.simplification;

import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.simplify.simplifier.Context;
import edu.udel.cis.vsl.sarl.simplify.simplifier.IdealSimplifierWorker;

/**
 * A {@link Simplification} that proceeds by creating a {@link SubContext} of
 * the current {@link Context} to process a {@link BooleanSymbolicExpression}.
 * Currently, this is used to process expressions with operator AND, LESS_THAN,
 * LESS_THAN_EQUALS, NEQ and also EQUALS in the case where the arguments are
 * numeric.
 */
public class SubContextSimplification extends Simplification {

	public SubContextSimplification(IdealSimplifierWorker worker) {
		super(worker);
	}

	@Override
	public SymbolicExpression apply(SymbolicExpression expression) {
		if (expression.type().isBoolean()) {
			Context c = newSubContext((BooleanExpression) expression);

			return c.getFullAssumption();
		}
		return expression;
	}

	@Override
	public SimplificationKind kind() {
		return SimplificationKind.SUBCONTEXT;
	}

}
