package edu.udel.cis.vsl.sarl.simplify.simplification;

import java.util.HashSet;
import java.util.Set;

import edu.udel.cis.vsl.sarl.IF.SARLConstants;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.expr.IF.BooleanExpressionFactory;
import edu.udel.cis.vsl.sarl.simplify.simplifier.IdealSimplifierWorker;

/**
 * Some things that should happen:
 * 
 * all clauses involving inequalities/equations on the same monic should be
 * combined and unified.
 * 
 * p||!p should be reduced to true.
 * 
 * 
 * 
 * @author xxxx
 *
 */
public class OrSimplification extends Simplification {

	public OrSimplification(IdealSimplifierWorker worker) {
		super(worker);
	}

	@Override
	public SymbolicExpression apply(SymbolicExpression x) {
		if (x.operator() != SymbolicOperator.OR)
			return x;

		BooleanExpressionFactory bf = info().getBooleanFactory();
		BooleanExpression expr = (BooleanExpression) x;
		BooleanExpression[] args = bf.getArgumentsAsArray(expr);
		int n = args.length;
		Set<BooleanExpression> nots = new HashSet<>();

		for (BooleanExpression arg : args)
			nots.add(bf.not(arg));
		for (int i = 0; i < n; i++) {
			BooleanExpression arg = args[i];

			if (nots.contains(arg)) {
				BooleanExpression result = info().trueExpr();

				for (int j = 0; j < i; j++)
					result = bf.or(result, args[j]);
				for (i++; i < n; i++)
					if (!nots.contains(args[i]))
						result = bf.or(result, args[i]);
				return result;
			}
		}
		if (SARLConstants.useDoubleOrNegation) {
			BooleanExpression result = universe()
					.not((BooleanExpression) simplifyExpression(
							universe().not(expr)));
			return result;
		} else {
			return expr;
		}
	}

	@Override
	public SimplificationKind kind() {
		return SimplificationKind.OR;
	}
}
