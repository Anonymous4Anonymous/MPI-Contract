/*******************************************************************************
 * Copyright (c) 2013 Stephen F. Siegel, University of Delaware.
 * 
 * This file is part of SARL.
 * 
 * SARL is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * SARL is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with SARL. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package edu.udel.cis.vsl.sarl.reason.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import edu.udel.cis.vsl.sarl.IF.Reasoner;
import edu.udel.cis.vsl.sarl.IF.TheoremProverException;
import edu.udel.cis.vsl.sarl.IF.ValidityResult;
import edu.udel.cis.vsl.sarl.IF.ValidityResult.ResultType;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericSymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.number.Interval;
import edu.udel.cis.vsl.sarl.IF.number.Number;
import edu.udel.cis.vsl.sarl.preuniverse.IF.PreUniverse;
import edu.udel.cis.vsl.sarl.prove.IF.Prove;
import edu.udel.cis.vsl.sarl.simplify.IF.Simplifier;

/**
 * Very basic reasoner that does not use any theorem prover, only
 * simplification.
 * 
 * @author xxxx
 *
 */
public class SimpleReasoner implements Reasoner {

	private Simplifier simplifier;

	private Map<BooleanExpression, ValidityResult> validityCache = new ConcurrentHashMap<BooleanExpression, ValidityResult>();

	private Map<BooleanExpression, ValidityResult> unsatCache = new ConcurrentHashMap<BooleanExpression, ValidityResult>();

	public SimpleReasoner(Simplifier simplifier) {
		this.simplifier = simplifier;
	}

	public PreUniverse universe() {
		return simplifier.universe();
	}

	@Override
	public BooleanExpression getReducedContext() {
		return simplifier.getReducedContext();
	}

	@Override
	public BooleanExpression getFullContext() {
		return simplifier.getFullContext();
	}

	@Override
	public Interval assumptionAsInterval(SymbolicConstant symbolicConstant) {
		return simplifier.assumptionAsInterval(symbolicConstant);
	}

	@Override
	public SymbolicExpression simplify(SymbolicExpression expression) {
		return simplifier.apply(expression);
	}

	@Override
	public BooleanExpression simplify(BooleanExpression expression) {
		return (BooleanExpression) simplify((SymbolicExpression) expression);
	}

	@Override
	public NumericExpression simplify(NumericExpression expression) {
		return (NumericExpression) simplify((SymbolicExpression) expression);
	}

	@Override
	public ValidityResult valid(BooleanExpression predicate) {
		ValidityResult result = validityCache.get(predicate);

		universe().incrementProverValidCount();
		if (result == null) {
			BooleanExpression simple = (BooleanExpression) simplifier
					.apply(predicate);
			Boolean concrete = universe().extractBoolean(simple);

			if (concrete == null)
				result = Prove.RESULT_MAYBE;
			else if (concrete)
				result = Prove.RESULT_YES;
			else
				result = Prove.RESULT_NO;
			validityCache.putIfAbsent(predicate, result);
		}
		return result;
	}

	@Override
	public ValidityResult validOrModel(BooleanExpression predicate) {
		throw new TheoremProverException(
				"SimpleIdealProver cannot be used to find models");
	}

	@Override
	public Map<SymbolicConstant, SymbolicExpression> constantSubstitutionMap() {
		return simplifier.constantSubstitutionMap();
	}

	@Override
	public boolean isValid(BooleanExpression predicate) {
		return valid(predicate).getResultType() == ResultType.YES;
	}

	@Override
	public Number extractNumber(NumericExpression expression) {
		NumericExpression simple = (NumericExpression) simplify(expression);

		return universe().extractNumber(simple);
	}

	@Override
	public Interval intervalApproximation(NumericExpression expr) {
		return simplifier.intervalApproximation(expr);
	}

	@Override
	public boolean checkBigOClaim(BooleanExpression indexConstraint,
			NumericExpression lhs, NumericSymbolicConstant[] limitVars,
			int[] orders) {
		return false;
	}

	@Override
	public ValidityResult unsat(BooleanExpression predicate) {
		ValidityResult result = unsatCache.get(predicate);

		universe().incrementProverValidCount();
		if (result == null) {
			BooleanExpression simple = (BooleanExpression) simplifier
					.apply(predicate);
			Boolean concrete = universe().extractBoolean(simple);

			if (concrete == null)
				result = Prove.RESULT_MAYBE;
			else if (concrete)
				result = Prove.RESULT_NO;
			else
				result = Prove.RESULT_YES;
			unsatCache.putIfAbsent(predicate, result);
		}
		return result;
	}
}
