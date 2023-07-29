package edu.udel.cis.vsl.civl.library.civlc;

import java.util.LinkedList;
import java.util.List;

import edu.udel.cis.vsl.civl.util.IF.Pair;
import edu.udel.cis.vsl.sarl.IF.CoreUniverse.ForallStructure;
import edu.udel.cis.vsl.sarl.IF.Reasoner;
import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericSymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.IF.number.Number;

/**
 * Collect universal clauses from a set of CNF clauses that matches such a
 * pattern: <code>
 *  FORALL int i. (lower &lt= i &lt= upper && i % step == remainder) ==> P(i);
 * </code>
 * 
 * @author xxxx
 */
class StepRestrictedForallStructureCollection {

	private List<StepRestrictedForall> foralls;

	private SymbolicUniverse universe;

	StepRestrictedForallStructureCollection(SymbolicUniverse universe,
			List<BooleanExpression> cnfClauses) {
		foralls = new LinkedList<>();
		this.universe = universe;
		filterForallStructuresSatisfyStepRestriction(
				filterForallStructures(cnfClauses));
	}

	private List<Pair<ForallStructure, BooleanExpression>> filterForallStructures(
			List<BooleanExpression> cnfClauses) {
		List<Pair<ForallStructure, BooleanExpression>> rawForalls = new LinkedList<>();

		for (BooleanExpression expr : cnfClauses)
			if (expr.operator() == SymbolicOperator.FORALL) {
				ForallStructure forall = universe.getForallStructure(expr);

				if (forall != null)
					rawForalls.add(new Pair<>(forall, expr));
			}
		return rawForalls;
	}

	// This method only looks at the outer most quantified expression
	private void filterForallStructuresSatisfyStepRestriction(
			List<Pair<ForallStructure, BooleanExpression>> rawForalls) {
		for (Pair<ForallStructure, BooleanExpression> pair : rawForalls) {
			ForallStructure rawForallStructure = pair.left;

			if (rawForallStructure.body.operator() == SymbolicOperator.OR
					&& rawForallStructure.body.numArguments() == 2) {
				BooleanExpression orClause0 = (BooleanExpression) rawForallStructure.body
						.argument(0);
				BooleanExpression orClause1 = (BooleanExpression) rawForallStructure.body
						.argument(1);
				StepRestrictedForall stepRestrictedForall = null;

				if (orClause0.operator() == SymbolicOperator.EQUALS)
					orClause0 = transformIfStepIsTwo(orClause0);
				if (orClause0.operator() == SymbolicOperator.NEQ)
					stepRestrictedForall = patternMatched(rawForallStructure,
							orClause1, orClause0,
							rawForallStructure.boundVariable, pair.right);
				if (stepRestrictedForall == null
						&& orClause1.operator() == SymbolicOperator.EQUALS)
					orClause1 = transformIfStepIsTwo(orClause1);
				if (orClause1 != null && stepRestrictedForall == null
						&& orClause1.operator() == SymbolicOperator.NEQ)
					stepRestrictedForall = patternMatched(rawForallStructure,
							orClause0, orClause1,
							rawForallStructure.boundVariable, pair.right);
				if (stepRestrictedForall != null)
					foralls.add(stepRestrictedForall);
			}
		}
	}

	/**
	 * @return {@link StepRestrictedForall} if the given equation is
	 *         <code>bv % step == step_offset </code>
	 */
	private StepRestrictedForall patternMatched(ForallStructure forall,
			BooleanExpression bodyWithoutStep, BooleanExpression nequals,
			NumericSymbolicConstant bv, BooleanExpression origin) {
		if (nequals.operator() != SymbolicOperator.NEQ)
			return null;

		SymbolicExpression op0 = (SymbolicExpression) nequals.argument(0);

		if (!op0.isZero())
			return null;

		NumericExpression op1 = (NumericExpression) nequals.argument(1);
		NumericExpression moduloExpr, offsetExpr;

		if (op1.operator() == SymbolicOperator.SUBTRACT) {
			moduloExpr = (NumericExpression) op1.argument(0);
			offsetExpr = (NumericExpression) op1.argument(1);
		} else if (op1.operator() == SymbolicOperator.ADD) {
			moduloExpr = (NumericExpression) op1.argument(0);
			offsetExpr = universe.minus((NumericExpression) op1.argument(1));
		} else
			return null;

		if (moduloExpr.operator() == SymbolicOperator.MODULO) {
			NumericExpression moduleOp0 = (NumericExpression) moduloExpr
					.argument(0);
			NumericExpression moduleOp1 = (NumericExpression) moduloExpr
					.argument(1);
			Number step = universe.extractNumber((NumericExpression) moduleOp1);

			if (moduleOp0.equals(bv)) {
				if (step != null)
					return new StepRestrictedForall(forall, bodyWithoutStep,
							step, offsetExpr, origin);
			}
		}
		return null;
	}

	/**
	 * For some equations like <code> (bv + n) % step = m</code>, it can be
	 * transformed to the pattern we want <code>bv % step = abs(n - m)</code>,
	 * iff <code>
	 * ((bv + n) % step = m) -> (bv % step = abs(abs(n) - m))
	 * </code> is proved as a tautology.
	 * 
	 * @return
	 */
	@SuppressWarnings("unused") // could be used if such case happens
	private StepRestrictedForall equivalentStepEquationTransformation(
			NumericSymbolicConstant bv, NumericExpression moduloOperand,
			Number step, NumericExpression offset, ForallStructure forall,
			BooleanExpression bodyWithoutStep, BooleanExpression origin) {
		NumericExpression diff = universe.subtract(moduloOperand, bv);
		NumericExpression absDiff = (NumericExpression) universe.cond(
				universe.lessThanEquals(universe.zeroInt(), diff), diff,
				universe.minus(diff));
		NumericExpression absDiffOffset = universe.subtract(absDiff, offset);

		absDiffOffset = (NumericExpression) universe.cond(
				universe.lessThanEquals(universe.zeroInt(), absDiffOffset),
				absDiffOffset, universe.minus(absDiffOffset));

		BooleanExpression equivalent = universe.equals(
				universe.modulo(moduloOperand, universe.number(step)), offset);
		BooleanExpression expect = universe.equals(
				universe.modulo(bv, universe.number(step)), absDiffOffset);
		Reasoner noContextReasoner = universe
				.reasoner(universe.trueExpression());

		if (noContextReasoner
				.isValid(universe.and(universe.implies(equivalent, expect),
						universe.implies(expect, equivalent))))
			new StepRestrictedForall(forall, bodyWithoutStep, step, offset,
					origin);
		return null;
	}

	/**
	 * The algorithm is looking for <code>bv % step != n</code>, but when step
	 * is 2, <code>bv % 2 == n</code> can be converted to whats expected :
	 * <code>bv % 2 !=  2 - n </code>
	 * 
	 * @param equation
	 * @return
	 */
	private BooleanExpression transformIfStepIsTwo(BooleanExpression equation) {
		NumericExpression op0 = (NumericExpression) equation.argument(0);
		NumericExpression op1 = (NumericExpression) equation.argument(1);
		NumericExpression moduloFormula;

		if (op0.isZero())
			moduloFormula = op1;
		else if (op1.isZero())
			moduloFormula = op0;
		else
			return equation;

		NumericExpression step;

		if (moduloFormula.operator() == SymbolicOperator.ADD) {
			NumericExpression modulo = (NumericExpression) moduloFormula
					.argument(0);
			NumericExpression offset = (NumericExpression) moduloFormula
					.argument(1);

			if (modulo.operator() == SymbolicOperator.MODULO)
				step = (NumericExpression) modulo.argument(1);
			else if (offset.operator() == SymbolicOperator.MODULO)
				step = (NumericExpression) offset.argument(1);
			else
				return equation;
			if (universe.equals(step, universe.integer(2)).isTrue())
				return universe.neq(modulo,
						universe.add(universe.oneInt(), offset));
		} else if (moduloFormula.operator() == SymbolicOperator.MODULO) {
			step = (NumericExpression) moduloFormula.argument(1);
			if (universe.equals(step, universe.integer(2)).isTrue())
				return universe.neq(moduloFormula, universe.oneInt());
		}
		return equation;
	}

	/**
	 * 
	 * <p>
	 * A forall expression : <code>
	 *  forall int i. low <= i <= high && i % step == step_offset ==> P(i);
	 * </code> Note that step must be concrete.
	 * </p>
	 * 
	 * <p>
	 * <li>{@link #forall} : <code>
	 *  forall int i. low <= i <= high && i % step == step_offset ==> P(i);
	 * </code></li>
	 * <li>{@link #step} : step</li>
	 * <li>{@link #step_offset} : step_offset</li>
	 * <li>{@link #origin}</li> : the original clause appears in the cnf clause
	 * <li>{@link #bodyWithoutStep} : P(i)</li>
	 * </p>
	 * 
	 * @author xxxx
	 */
	public class StepRestrictedForall {
		final ForallStructure forall;
		final BooleanExpression bodyWithoutStep;
		final Number step;
		final NumericExpression step_offset;
		final BooleanExpression origin;

		StepRestrictedForall(ForallStructure forall,
				BooleanExpression bodyWithoutStep, Number step,
				NumericExpression step_offset, BooleanExpression origin) {
			this.forall = forall;
			this.bodyWithoutStep = bodyWithoutStep;
			this.step = step;
			this.step_offset = step_offset;
			this.origin = origin;
		}
	}

	public List<StepRestrictedForall> getStepRestrictedForalls() {
		return foralls;
	}
}
