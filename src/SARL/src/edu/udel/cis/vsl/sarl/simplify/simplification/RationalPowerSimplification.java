package edu.udel.cis.vsl.sarl.simplify.simplification;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.IF.number.IntegerNumber;
import edu.udel.cis.vsl.sarl.IF.number.NumberFactory;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject;
import edu.udel.cis.vsl.sarl.ideal.IF.Constant;
import edu.udel.cis.vsl.sarl.ideal.IF.IdealFactory;
import edu.udel.cis.vsl.sarl.ideal.IF.Monic;
import edu.udel.cis.vsl.sarl.ideal.IF.Monomial;
import edu.udel.cis.vsl.sarl.ideal.IF.Primitive;
import edu.udel.cis.vsl.sarl.ideal.IF.PrimitivePower;
import edu.udel.cis.vsl.sarl.ideal.IF.RationalExpression;
import edu.udel.cis.vsl.sarl.simplify.simplifier.IdealSimplifierWorker;

public class RationalPowerSimplification extends Simplification {

	public RationalPowerSimplification(IdealSimplifierWorker worker) {
		super(worker);
	}

	/**
	 * Build up entries in power map from the monic factors of a {@link Monic}.
	 * This method modifies two given {@link Map}s. The first map encodes a set
	 * of power expressions in which the base is a {@link Primitive} (in
	 * particular, is not concrete) and the second map encodes a set of power
	 * expressions in which the base is a concrete number. The final values of
	 * the maps is essentially the original value multiplied by the factors of
	 * the {@link Monic} (if <code>positive</code> is <code>true</code>)) or
	 * divided by the factors of the {@link Monic} (if <code>positive</code> is
	 * <code>false</code>). Simplifications are performed under the assumptions
	 * of {@link #theContext}).
	 * 
	 * @param powerMap1
	 *            map from the primitives to the exponent assigned to that
	 *            primitive; this is an input/output variable
	 * @param powerMap2
	 *            like <code>powerMap1</code>, but for expressions in which the
	 *            base is {@link Constant}; this is an input/output variable
	 * @param positive
	 *            if true, exponents should be added to the entries in the
	 *            powerMaps, otherwise they should be subtracted from entries;
	 *            an input
	 * @param monic
	 *            the {@link Monic} expression that is being simplified and
	 *            decomposed into a product of powers; this is an input
	 * 
	 * @return true iff change occurred
	 */
	private boolean simplifyPowers(Map<Monomial, RationalExpression> powerMap1,
			Map<Constant, RationalExpression> powerMap2, boolean positive,
			Monic monic) {
		IdealFactory idf = idealFactory();
		PrimitivePower[] factors = monic.monicFactors(idf);
		boolean isInteger = monic.type().isInteger();
		boolean change = false;
		NumberFactory nf = numberFactory();

		for (PrimitivePower pp : factors) {
			Primitive primitive = pp.primitive(idf);
			IntegerNumber outerExp = (IntegerNumber) pp
					.primitivePowerExponent(idf).getNumber();
			IntegerNumber signedOuterExp = positive ? outerExp
					: nf.negate(outerExp);
			RationalExpression realSignedOuterExp = idf.constant(isInteger
					? signedOuterExp : nf.integerToRational(signedOuterExp));
			RationalExpression newExp;
			SymbolicObject baseObj = primitive.argument(0);

			if (baseObj instanceof Constant) {
				Constant baseConst;

				if (primitive.operator() == SymbolicOperator.POWER) {
					baseConst = (Constant) primitive.argument(0);
					newExp = idf.multiply(realSignedOuterExp,
							(RationalExpression) primitive.argument(1));
					change = change || outerExp.numericalCompareTo(
							nf.abs(idf.getConcreteExponent(newExp))) != 0;

					NumericExpression oldExponent = powerMap2.get(baseConst);

					if (oldExponent == null) {
						powerMap2.put(baseConst, newExp);
						powerMap1.remove(primitive);
					} else {
						powerMap2.put(baseConst, idf.add(oldExponent, newExp));
						change = true;
					}
				}
			} else {
				Monomial baseMonomial;

				if (primitive.operator() == SymbolicOperator.POWER) {
					baseMonomial = (Monomial) primitive.argument(0);
					newExp = idf.multiply(realSignedOuterExp,
							(RationalExpression) primitive.argument(1));
					change = change || outerExp.numericalCompareTo(
							nf.abs(idf.getConcreteExponent(newExp))) != 0;
				} else {
					baseMonomial = primitive;
					newExp = realSignedOuterExp;
				}

				NumericExpression oldExponent = powerMap1.get(baseMonomial);

				if (oldExponent == null) {
					powerMap1.put(baseMonomial, newExp);
				} else {
					powerMap1.put(baseMonomial, idf.add(oldExponent, newExp));
					change = true;
				}
			}
		}
		return change;
	}

	/**
	 * Simplifies any {@link SymbolicOperator#POWER} operations occurring in a
	 * rational expression.
	 * 
	 * @param rational
	 *            a rational expression
	 * @return equivalent rational expression in which power operations that can
	 *         be combined are combined or simplified
	 */
	private RationalExpression simplifyPowersRational(
			RationalExpression rational) {
		IdealFactory idf = idealFactory();
		Monomial numerator = rational.numerator(idf),
				denominator = rational.denominator(idf);
		Monic m1 = numerator.monic(idf), m2 = denominator.monic(idf);
		Map<Monomial, RationalExpression> powerMap = new HashMap<>();
		Map<Constant, RationalExpression> powerMap2 = new HashMap<>();
		boolean change1 = simplifyPowers(powerMap, powerMap2, true, m1);
		boolean change2 = simplifyPowers(powerMap, powerMap2, false, m2);

		if (change1 || change2) {
			RationalExpression result = idf.one(rational.type());

			for (Entry<Constant, RationalExpression> entry : powerMap2
					.entrySet()) {
				result = idf.multiply(result,
						idf.power(entry.getKey(), entry.getValue()));
			}
			for (Entry<Monomial, RationalExpression> entry : powerMap
					.entrySet()) {
				result = idf.multiply(result,
						idf.power(entry.getKey(), entry.getValue()));
			}
			result = idf.divide(
					idf.multiply(result, numerator.monomialConstant(idf)),
					denominator.monomialConstant(idf));
			return result;
		}
		return rational;
	}

	@Override
	public SymbolicExpression apply(SymbolicExpression x) {
		if (x instanceof RationalExpression)
			return simplifyPowersRational((RationalExpression) x);
		return x;
	}

	@Override
	public SimplificationKind kind() {
		return SimplificationKind.RATIONAL;
	}

}
