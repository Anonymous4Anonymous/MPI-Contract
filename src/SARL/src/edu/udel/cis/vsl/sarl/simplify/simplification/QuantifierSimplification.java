package edu.udel.cis.vsl.sarl.simplify.simplification;

import static edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator.EXISTS;
import static edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator.FORALL;

import edu.udel.cis.vsl.sarl.IF.CoreUniverse.ForallStructure;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.simplify.simplifier.Context;
import edu.udel.cis.vsl.sarl.simplify.simplifier.IdealSimplifierWorker;

public class QuantifierSimplification extends Simplification {

	public QuantifierSimplification(IdealSimplifierWorker worker) {
		super(worker);
	}

	@Override
	public SymbolicExpression apply(SymbolicExpression expr) {
		SymbolicOperator op = expr.operator();

		if (op == FORALL) {
			ForallStructure forall = universe()
					.getForallStructure((BooleanExpression) expr);

			if (forall != null) {
				NumericExpression incLow = forall.lowerBound, excUp = universe()
						.add(forall.upperBound, universe().oneInt()), excUp2;

				incLow = (NumericExpression) simplifyExpression(incLow);
				excUp2 = (NumericExpression) simplifyExpression(excUp);

				// restrict:
				BooleanExpression rstc = universe().lessThanEquals(incLow,
						forall.boundVariable);

				rstc = universe().and(rstc,
						universe().lessThan(forall.boundVariable, excUp2));
				rstc = (BooleanExpression) simplifyExpression(rstc);

				Context subContext = newSubContext(rstc);

				if (subContext.getFullAssumption().isFalse())
					return universe().trueExpression();

				BooleanExpression newBody = (BooleanExpression) subContext
						.simplify(forall.body);

				if (newBody == forall.body && incLow == forall.lowerBound
						&& excUp == excUp2)
					return expr;
				else
					return universe().forallInt(forall.boundVariable, incLow,
							excUp2, newBody);
			}
		}
		if (op == FORALL || op == EXISTS) {
			SymbolicConstant boundVar = (SymbolicConstant) expr.argument(0);
			BooleanExpression body = (BooleanExpression) expr.argument(1);
			Context subContext = newSubContext(body);
			BooleanExpression body2 = subContext.getFullAssumption();

			if (body == body2)
				return expr;
			return op == SymbolicOperator.FORALL
					? universe().forall(boundVar, body2)
					: universe().exists(boundVar, body2);
		}
		return expr;
	}

	@Override
	public SimplificationKind kind() {
		return SimplificationKind.QUANTIFIER;
	}

}
