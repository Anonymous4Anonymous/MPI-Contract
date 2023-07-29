package edu.udel.cis.vsl.sarl.simplify.simplification;

import java.util.HashSet;

import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.simplify.simplifier.Context;
import edu.udel.cis.vsl.sarl.simplify.simplifier.ContextExtractor;
import edu.udel.cis.vsl.sarl.simplify.simplifier.IdealSimplifierWorker;
import edu.udel.cis.vsl.sarl.simplify.simplifier.InconsistentContextException;

public class NumericOrSimplification extends Simplification {

	public NumericOrSimplification(IdealSimplifierWorker worker) {
		super(worker);
	}

	@Override
	public SymbolicExpression apply(SymbolicExpression x) {
		if (x.operator() != SymbolicOperator.OR)
			return x;

		BooleanExpression expr = (BooleanExpression) x, result;
		Context subContext = newSubContext();
		ContextExtractor extractor = new ContextExtractor(subContext,
				new HashSet<>());
		boolean success;

		try {
			success = extractor.extractNumericOr(expr);
		} catch (InconsistentContextException e) {
			return info().falseExpr();
		}
		if (success)
			result = subContext.getFullAssumption();
		else
			result = expr;
		return result;
	}

	@Override
	public SimplificationKind kind() {
		return SimplificationKind.NUMERIC_OR;
	}

}
