package edu.udel.cis.vsl.civl.library.civlc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.udel.cis.vsl.civl.library.civlc.StepRestrictedForallStructureCollection.StepRestrictedForall;
import edu.udel.cis.vsl.sarl.IF.CoreUniverse.ForallStructure;
import edu.udel.cis.vsl.sarl.IF.Reasoner;
import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;
import edu.udel.cis.vsl.sarl.IF.UnaryOperator;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.IF.number.IntegerNumber;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType.SymbolicTypeKind;

/**
 * <p>
 * Given a set of <code>n</code> universal quantified expressions:
 * <code>                                                                                                    
 * { FORALL i : low &lt= i &lt high && i % n == m ==> P<sub>j</sub>(i) | 0 &lt= j &lt n}                                                                                     
 * </code>, if one can prove that
 * <code>                                                                                                                                     
 * FORALL i, j : 0 &lt= j &lt n-1 ==> P<sub>j</sub>(i+1) == P<sub>j+1</sub>(i);                                                                                              
 * </code> The whole set of universal quantified expressions can be combined to
 * <code>                                                                                                                                                                    
 * FORALL i : low' &lt= i &lt high' ==> P<sub>0</sub>(i)                                                                                                                     
 * </code> where <br>
 * <code>low' == low % n == m ? low : 
 *                              (low % n &lt m ? low - low % n + m : low - low %n + n + m)</code>
 * and <code>high' == high % n == m ? (high + n - 1) : 
 *                                (high % n &lt m ? high - high % n + m - 1 : high - high % n + m + n - 1)</code>
 * .
 * </p>
 * 
 * <p>
 * Some description for the new lower and higher bounds <code>low'</code> and
 * <code>high'</code>: <code>low'</code> is suppose to be the minimum number
 * that is greater than or equal to <code>low</code> and
 * <code>low' % n == m</code>. So there are three cases: 1) if
 * <code>low % n == m</code>, <code>low' == low</code>; 2) else if
 * <code>low % n &lt m</code>, then the new lower can be obtained by adding an
 * offset <code>m - low % n </code> to <code>low</code>; 3) Otherwise, the new
 * lower is obtained by adding an offset <code>n - low % n + m</code> to
 * <code>low</code>
 * </p>
 * 
 * <p>
 * Smimilarly, <code>higher'</code> is suppose to be the number that is obtained
 * via adding <code>n - 1</code> to the maximum number <code>max</code> that is
 * less than or equal to <code>high</code> and <code>max % n == m</code>. So
 * there are three cases: 1) if <code>high % n == m</code>,
 * <code>high == max</code>, then <code>high' = max + n - 1</code>; 2) else if
 * <code>high % n &lt m</code>, then <code>max</code> can be obtained by adding
 * an offset <code>m - n - high % n </code> to <code>high</code>; 3) Otherwise,
 * the <code>max</code> is obtained by adding an offset
 * <code>m - high % n</code> to <code>high</code>
 * </p>
 * 
 * <p>
 * Such a simplification gets rid of modulo operations and reduces the number of
 * universal quantified clauses in CNF
 * </p>
 * 
 * @author xxxx
 *
 */
public class SteppedUniversalCombination extends ExpressionVisitor
		implements
			UnaryOperator<SymbolicExpression> {

	List<ForallStructure> structuredForalls;

	/**
	 * The applied expression:
	 */
	private BooleanExpression x;

	SteppedUniversalCombination(SymbolicUniverse universe) {
		super(universe);
		this.structuredForalls = new LinkedList<>();
	}

	@Override
	public BooleanExpression apply(SymbolicExpression x) {
		assert x.type().typeKind() == SymbolicTypeKind.BOOLEAN;
		this.x = (BooleanExpression) x;
		if (x.operator() == SymbolicOperator.AND) {
			List<BooleanExpression> cnfClauses = new LinkedList<>();

			for (SymbolicObject arg : x.getArguments())
				cnfClauses.add((BooleanExpression) arg);

			StepRestrictedForallStructureCollection stepedForalls;
			StepRestrictedForall[] stepedForallArray, tmp;

			stepedForalls = new StepRestrictedForallStructureCollection(
					universe, cnfClauses);
			stepedForallArray = new StepRestrictedForall[stepedForalls
					.getStepRestrictedForalls().size()];
			stepedForalls.getStepRestrictedForalls().toArray(stepedForallArray);

			List<StepRestrictedForall> groupCandidate = extractGroup(
					stepedForallArray);

			while (groupCandidate != null && stepedForallArray.length > 0) {
				int groupSize = groupCandidate.size();
				// TODO: let extract group return step value
				int j = 0, step = ((IntegerNumber) groupCandidate.get(0).step)
						.intValue();

				processGroupCandidate(groupCandidate, step);
				// resize array:
				tmp = new StepRestrictedForall[stepedForallArray.length
						- groupSize];
				for (int i = 0; i < stepedForallArray.length; i++)
					if (stepedForallArray[i] != null)
						tmp[j++] = stepedForallArray[i];
				stepedForallArray = tmp;
				groupCandidate = extractGroup(stepedForallArray);
			}
			return this.x;
		} else
			return this.x;
	}

	/**
	 * @param foralls
	 *            An array of {@link StepRestrictedForall}. Elements returned by
	 *            this method will be set to null in this array.
	 * @return A group of foralls :
	 *         <code>FORALL int i. low <= i <= high && i % step == offset ==> P(i) && P(i+1) && ... </code>
	 * 
	 */
	private List<StepRestrictedForall> extractGroup(
			StepRestrictedForall[] foralls) {
		for (int i = 0; i < foralls.length; i++) {
			List<Integer> groupMemCandidates = new LinkedList<>();
			int stepVal = ((IntegerNumber) foralls[i].step).intValue();

			for (int j = 0; j < foralls.length; j++) {
				if (i == j)
					continue;
				if (foralls[j].step.equals(foralls[i].step)
						&& foralls[j].step_offset
								.equals(foralls[i].step_offset))
					if (foralls[j].forall.lowerBound
							.equals(foralls[i].forall.lowerBound)
							&& foralls[j].forall.upperBound
									.equals(foralls[i].forall.upperBound))
						groupMemCandidates.add(j);
			}
			groupMemCandidates.add(i);
			if (stepVal <= groupMemCandidates.size()) {
				List<StepRestrictedForall> results = new LinkedList<>();

				for (int idx : groupMemCandidates) {
					results.add(foralls[idx]);
					foralls[idx] = null;
				}
				return results;
			}
		}
		return null;
	}

	@Override
	SymbolicExpression visitExpression(SymbolicExpression expr) {
		return expr;
	}

	/**
	 * <p>
	 * Given a set of N forall clauses (N >= step): <code>
	 * X<sub>0</sub>(k), X<sub>1</sub>(k), ..., X<sub>N-1</sub>(k)
	 * </code>, attempt to find out a "step" of them that satisfy such
	 * relations: <code>
	 * {X<sub>i</sub>(k) | X<sub>i</sub>(k+1) == X<sub>i+1</sub>(k+1), 0 &lt= i &lt step }.
	 * </code>
	 * </p>
	 * 
	 * 
	 * <p>
	 * update {@link #x} if find one.
	 * </p>
	 * 
	 * 
	 * @param group
	 * @param step
	 */
	private void processGroupCandidate(List<StepRestrictedForall> group,
			int step) {
		Map<SymbolicExpression, StepRestrictedForall> map = new HashMap<>();

		// group in map:
		for (StepRestrictedForall member : group)
			map.put(member.bodyWithoutStep, member);

		LinkedList<StepRestrictedForall> results = new LinkedList<>();

		// find a pair that Xi(k+1) == Xi+1(k) as the starter:
		for (StepRestrictedForall member : group) {
			BooleanExpression nextBody = (BooleanExpression) universe
					.simpleSubstituter(member.forall.boundVariable,
							universe.add(member.forall.boundVariable,
									universe.oneInt()))
					.apply(member.bodyWithoutStep);
			StepRestrictedForall next = map.get(nextBody);

			if (next != null) {
				results.add(member);
				results.add(next);
				break;
			}
		}

		if (results.isEmpty())
			return;

		// based on the starter pair, search in two directions:
		// find all clauses "precedes" the starter:
		StepRestrictedForall starter = results.getFirst();

		while (starter != null && results.size() < step) {
			BooleanExpression prevBody = (BooleanExpression) universe
					.simpleSubstituter(starter.forall.boundVariable,
							universe.subtract(starter.forall.boundVariable,
									universe.oneInt()))
					.apply(starter.bodyWithoutStep);
			StepRestrictedForall precede = map.get(prevBody);

			if (precede != null)
				results.addFirst(precede);
			starter = precede;
		}
		starter = results.getLast();
		while (starter != null && results.size() < step) {
			BooleanExpression nextBody = (BooleanExpression) universe
					.simpleSubstituter(starter.forall.boundVariable,
							universe.add(starter.forall.boundVariable,
									universe.oneInt()))
					.apply(starter.bodyWithoutStep);
			StepRestrictedForall next = map.get(nextBody);

			if (next != null)
				results.addLast(next);
			starter = next;
		}
		if (results.size() < step)
			return;

		// results to set for easy check existence:
		Set<SymbolicExpression> originSetOfResults = new HashSet<>();

		for (StepRestrictedForall result : results)
			originSetOfResults.add(result.origin);

		// update x:
		SymbolicObject xNewArgs[] = new SymbolicObject[x.numArguments() - step
				+ 1];
		int ct = 0;

		for (SymbolicObject arg : x.getArguments())
			if (!originSetOfResults.contains(arg))
				xNewArgs[ct++] = arg;
		if (ct != xNewArgs.length - 1)
			return; // if so , something wrong ?

		StepRestrictedForall theBaseForall = results.getFirst();

		updateBounds(theBaseForall);
		xNewArgs[ct] = universe
				.forallInt(theBaseForall.forall.boundVariable,
						theBaseForall.forall.lowerBound,
						universe.add(theBaseForall.forall.upperBound,
								universe.oneInt()),
						theBaseForall.bodyWithoutStep);
		x = (BooleanExpression) universe.make(SymbolicOperator.AND,
				universe.booleanType(), xNewArgs);
	}

	/**
	 * 
	 * Update lower bound of the given forall clause to
	 * <code>low' == low + (low % n == m ? 0 : n - m)</code> and
	 * <code>high' == high % n == m ? high : (high % n &lt m ? h - h % n + m : h - h % n + m + n)</code>
	 * 
	 * @param forall
	 */
	private void updateBounds(StepRestrictedForall forall) {
		NumericExpression newLow = newLow(forall.forall.lowerBound,
				universe.number(forall.step), forall.step_offset);
		NumericExpression newUp = newHigh(forall.forall.upperBound,
				universe.number(forall.step), forall.step_offset);

		forall.forall.lowerBound = newLow;
		forall.forall.upperBound = newUp;
	}

	/**
	 * n is step, m is offset: <code>low' == low % n == m ? low : 
	*                              (low % n &lt m ? low - low % n + m : low - low % n + n + m)</code>
	 * 
	 * @return
	 */
	private NumericExpression newLow(NumericExpression low,
			NumericExpression step, NumericExpression offset) {
		Reasoner reasoner = universe.reasoner(x);
		BooleanExpression cond0 = universe.equals(universe.modulo(low, step),
				offset);
		BooleanExpression cond1 = universe.lessThan(universe.modulo(low, step),
				offset);

		NumericExpression newLow0 = low;
		NumericExpression newLow1 = universe.add(Arrays.asList(low, offset,
				universe.minus(universe.modulo(low, step)), offset,
				universe.minus(universe.oneInt())));
		NumericExpression newLow2 = universe.add(Arrays.asList(low, step,
				offset, universe.minus(universe.modulo(low, step))));

		if (reasoner.isValid(cond0))
			return newLow0;
		else if (reasoner.isValid(universe.not(cond0)))
			if (reasoner.isValid(cond1))
				return newLow1;
			else if (reasoner.isValid(universe.not(cond1)))
				return newLow2;
		return (NumericExpression) universe.cond(cond0, newLow0,
				universe.cond(cond1, newLow1, newLow2));
	}

	/**
	 * n is step, m is offset: <br>
	 * <code>high' == high % n == m ? (high + n - 1) : 
	 *                                (high % n &lt m ? h - h % n + m - 1 : h - h % n + m + n - 1)</code>
	 * 
	 * @return
	 */
	private NumericExpression newHigh(NumericExpression high,
			NumericExpression step, NumericExpression offset) {
		Reasoner reasoner = universe.reasoner(x);
		BooleanExpression cond0 = universe.equals(universe.modulo(high, step),
				offset);
		BooleanExpression cond1 = universe.lessThan(universe.modulo(high, step),
				offset);

		NumericExpression newHigh0 = universe.add(
				Arrays.asList(high, step, universe.minus(universe.oneInt())));
		NumericExpression newHigh1 = universe.add(
				Arrays.asList(high, universe.minus(universe.modulo(high, step)),
						offset, universe.minus(universe.oneInt())));
		NumericExpression newHigh2 = universe.add(newHigh1, step);

		if (reasoner.isValid(cond0))
			return newHigh0;
		else if (reasoner.isValid(universe.not(cond0))) {
			if (reasoner.isValid(cond1))
				return newHigh1;
			else if (reasoner.isValid(universe.not(cond1)))
				return newHigh2;
		}
		return (NumericExpression) universe.cond(cond0, newHigh0,
				universe.cond(cond1, newHigh1, newHigh2));
	}
}
