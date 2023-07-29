package edu.udel.cis.vsl.sarl.simplify.simplification;

import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.IF.object.NumberObject;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject.SymbolicObjectKind;
import edu.udel.cis.vsl.sarl.ideal.IF.Constant;
import edu.udel.cis.vsl.sarl.ideal.IF.IdealFactory;
import edu.udel.cis.vsl.sarl.ideal.IF.Monic;
import edu.udel.cis.vsl.sarl.ideal.IF.Monomial;
import edu.udel.cis.vsl.sarl.ideal.IF.RationalExpression;
import edu.udel.cis.vsl.sarl.simplify.simplifier.IdealSimplifierWorker;

/**
 * A simplification that applies to expressions in which the operator is
 * {@link SymbolicOperator#POWER}.
 * 
 * @author xxxx
 */
public class PowerSimplification extends Simplification {

	public PowerSimplification(IdealSimplifierWorker worker) {
		super(worker);
	}

	/**
	 * <p>
	 * Attempts to decompose a power operation <code>base ^ exp</code> while the
	 * base is a monomial with a non-trivial (not one) constant.
	 * </p>
	 * 
	 * <p>
	 * Precondition and postcondition: {@code powerExpr} is generically
	 * simplified.
	 * </p>
	 * 
	 * <p>
	 * <code>base ^ exp</code> will be decomposed to
	 * <code>monomial_constant ^ exp * monomial_monic ^ exp</code> if both
	 * <code>monomial_constant & monomial_monic</code> are positive.
	 * </p>
	 * 
	 * @param powerExpr
	 *            the {@link SymbolicOperator#POWER} expression that might gets
	 *            decomposed (simplified).
	 * @param idf
	 *            A reference to the {@link IdealFactory}
	 * @return the simplified expression. If no simplification can be further
	 *         applied, the expression in unchanged.
	 */
	private RationalExpression simplifyPowerDecompose(
			RationalExpression powerExpr) {
		NumericExpression base = (NumericExpression) powerExpr.argument(0);
		SymbolicObject exp = powerExpr.argument(1);
		NumericExpression neExp;
		IdealFactory idf = idealFactory();

		if (exp.symbolicObjectKind() == SymbolicObjectKind.NUMBER) {
			NumberObject nobj = (NumberObject) exp;

			neExp = idf.number(nobj);
		} else
			neExp = (NumericExpression) exp;

		if (!(base instanceof Monomial))
			return powerExpr;

		Monomial monomialBase = (Monomial) base;
		Constant cons = monomialBase.monomialConstant(idf);
		Monic monic = monomialBase.monic(idf);

		if (cons.isOne())
			return powerExpr;

		// if exponent is an integer, both monic and constant are positive
		// this power expression can be decomposed:
		boolean decompose = false;

		if (neExp.type().isInteger())
			decompose = true;
		if (!decompose && intervalApproximation(cons).lower().signum() >= 0
				&& intervalApproximation(monic).lower().signum() >= 0)
			decompose = true;
		if (decompose) {
			RationalExpression result = idf.multiply(idf.power(monic, neExp),
					idf.power(cons, neExp));

			return result;
		}
		return powerExpr;
	}

	@Override
	public SymbolicExpression apply(SymbolicExpression x) {
		if (x.operator() == SymbolicOperator.POWER)
			return simplifyPowerDecompose((RationalExpression) x);
		return x;
	}

	@Override
	public SimplificationKind kind() {
		return SimplificationKind.POWER;
	}

}
