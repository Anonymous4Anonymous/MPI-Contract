package edu.udel.cis.vsl.sarl.simplify.simplification;

import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.simplify.simplifier.IdealSimplifierWorker;
import edu.udel.cis.vsl.sarl.simplify.simplifier.SubContext;

/**
 * Used to simplify a conditional symbolic expression p?a:b.
 * 
 * First, p is simplified. Then a is simplified under the context p. Then b is
 * simplified under the context !p.
 * 
 * @author xxxx
 */
public class ConditionalSimplification2 extends Simplification {

	public ConditionalSimplification2(IdealSimplifierWorker worker) {
		super(worker);
	}

	@Override
	public SymbolicExpression apply(SymbolicExpression x) {
		if (x.operator() != SymbolicOperator.COND)
			return x;

		BooleanExpression condition = (BooleanExpression) x.argument(0);
		SymbolicExpression trueValue = (SymbolicExpression) x.argument(1),
				falseValue = (SymbolicExpression) x.argument(2);

		BooleanExpression condition2 = (BooleanExpression) simplifyExpression(
				condition);

		if (condition2.isTrue())
			return simplifyExpression(trueValue);
		if (condition2.isFalse())
			return simplifyExpression(falseValue);

		SubContext trueContext = this.newSubContext(condition2);
		SymbolicExpression trueValue2 = trueContext.simplify(trueValue);
		SubContext falseContext = this
				.newSubContext(universe().not(condition2));
		SymbolicExpression falseValue2 = falseContext.simplify(falseValue);

		if (condition2 == condition && trueValue2 == trueValue
				&& falseValue2 == falseValue)
			return x;

		SymbolicExpression result = universe().cond(condition2, trueValue2,
				falseValue2);

		return result;
	}

	@Override
	public SimplificationKind kind() {
		return SimplificationKind.COND;
	}
}
