package edu.udel.cis.vsl.sarl.simplify.simplification;

import edu.udel.cis.vsl.sarl.IF.CoreUniverse;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;
import edu.udel.cis.vsl.sarl.ideal.IF.Constant;
import edu.udel.cis.vsl.sarl.ideal.IF.IdealFactory;
import edu.udel.cis.vsl.sarl.ideal.IF.Monomial;
import edu.udel.cis.vsl.sarl.ideal.IF.Polynomial;
import edu.udel.cis.vsl.sarl.ideal.IF.RationalExpression;
import edu.udel.cis.vsl.sarl.simplify.simplifier.IdealSimplifierWorker;

public class PolynomialSimplification extends Simplification {

	public PolynomialSimplification(IdealSimplifierWorker worker) {
		super(worker);
	}

	/**
	 * <p>
	 * Simplifies a {@link Polynomial}. Note that method
	 * {@link #simplifyGenericExpression(SymbolicExpression)} cannot be used,
	 * since that method will invoke
	 * {@link CoreUniverse#make(SymbolicOperator, SymbolicType, SymbolicObject[])}
	 * , which will apply binary addition repeatedly on the terms of a
	 * {@link Polynomial}, which will not result in the correct form.
	 * </p>
	 * 
	 * <p>
	 * The following strategies are used:
	 * <ul>
	 * <li>look up the polynomial in the {@link #constantMap()}</li>
	 * <li>convert to an {@link AffineExpression} and look for a constant value
	 * of the pseudo</li>
	 * <li>simplify the individual terms and sum the results</li>
	 * <li>full expand the polynomial</li>
	 * </ul>
	 * The process is repeated until it stabilizes or a constant value is
	 * determined.
	 * </p>
	 * 
	 * Precondition: {@code poly} is generically simplified
	 * 
	 * Postcondition: result is generically simplified and equivalent under
	 * {@link #theContext} to {@code poly}
	 * 
	 * @param poly
	 *            a {@link Polynomial} with at least 2 terms
	 * @return a simplified version of <code>poly</code> equivalent to
	 *         <code>poly</code> under the existing assumptions
	 */
	private RationalExpression simplifyPolynomial(Polynomial poly) {
		IdealFactory idf = idealFactory();
		Constant constantTerm = poly.constantTerm(idf);

		if (!constantTerm.isZero()) {
			RationalExpression result = idf.subtract(poly, constantTerm);

			result = (RationalExpression) simplifyExpression(result);
			result = idf.add(result, constantTerm);
			return result;
		}

		// try simplifying the terms of this polynomial...

		Monomial[] termMap = poly.termMap(idf);
		int size = termMap.length;
		Monomial[] terms = null;

		assert size >= 2;
		for (int i = 0; i < size; i++) {
			Monomial term = termMap[i];
			Monomial simplifiedTerm = (Monomial) simplifyExpression(term);

			if (term != simplifiedTerm) { // a simplification
				terms = new Monomial[size];
				for (int j = 0; j < i; j++)
					terms[j] = termMap[j];
				terms[i] = simplifiedTerm;
				for (int j = i + 1; j < size; j++)
					terms[j] = (Monomial) simplifyExpression(termMap[j]);
				return (RationalExpression) idf.addMonomials(terms);
			}
		}
		return poly;
	}

	@Override
	public SymbolicExpression apply(SymbolicExpression x) {
		if (x instanceof Polynomial) {
			return simplifyPolynomial((Polynomial) x);
		}
		return x;
	}

	@Override
	public SimplificationKind kind() {
		return SimplificationKind.POLYNOMIAL;
	}

}
