package edu.udel.cis.vsl.sarl.simplify.simplification;

import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.simplify.simplifier.Context;
import edu.udel.cis.vsl.sarl.simplify.simplifier.IdealSimplifierWorker;

public class LambdaSimplification extends Simplification {

	public LambdaSimplification(IdealSimplifierWorker worker) {
		super(worker);
	}

	@Override
	public SymbolicExpression apply(SymbolicExpression expr) {
		if (expr.operator() == SymbolicOperator.LAMBDA) {
			// lambda x . e;
			SymbolicConstant boundVar = (SymbolicConstant) expr.argument(0);
			SymbolicExpression body = (SymbolicExpression) expr.argument(1);
			Context subContext = newSubContext();
			SymbolicExpression body2 = subContext.simplify(body);

			if (body2 == body)
				return expr;
			return universe().lambda(boundVar, body2);
		} else {
			return expr;
		}
	}

	@Override
	public SimplificationKind kind() {
		return SimplificationKind.LAMBDA;
	}

}
