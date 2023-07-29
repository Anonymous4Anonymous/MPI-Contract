package edu.udel.cis.vsl.civl.library.civlc;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import edu.udel.cis.vsl.civl.library.civlc.Heuristics.Query;
import edu.udel.cis.vsl.civl.util.IF.Pair;
import edu.udel.cis.vsl.sarl.IF.CoreUniverse.ForallStructure;
import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;
import edu.udel.cis.vsl.sarl.IF.UnaryOperator;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject.SymbolicObjectKind;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicCompleteArrayType;

/**
 * a hack: for <code>
 * 
 *  FORALL int i; lo &lt= i &lt hi ==> A[i] == B[i]
 *  
 * </code>, generates a replacer for <code>
 * 
 * A = LAMBDA i; lo &lt= i &lt hi ? B[i] : A[i]
 * 
 * </code>. Finally, applies the replacer to both context and predicate.
 * 
 * @author xxxx
 */
public class SubArrayEquivalenceReplace {
	private SymbolicUniverse universe;
	
	private class ArrayElementEqualForalStructure {
		ForallStructure structure;
		NumericExpression arrayRead0;
		NumericExpression arrayRead1;

		ArrayElementEqualForalStructure(ForallStructure structure,
				NumericExpression arrayRead0, NumericExpression arrayRead1) {
			this.structure = structure;
			this.arrayRead0 = arrayRead0;
			this.arrayRead1 = arrayRead1;
		}
	}
	
	private class SubArrayReplacer {
		NumericExpression lo; // inclusive
		NumericExpression hi; // inclusive
		SymbolicConstant arrayReplacee;
		SymbolicConstant arrayReplacer;		
		
		SubArrayReplacer(NumericExpression lo, NumericExpression hi,
				SymbolicConstant arrayReplacee,
				SymbolicConstant arrayReplacer) {
			this.lo = lo;
			this.hi = hi;
			this.arrayReplacee = arrayReplacee;
			this.arrayReplacer = arrayReplacer;
		}
	}

	public SubArrayEquivalenceReplace(SymbolicUniverse universe) {
		this.universe = universe;
	}
	
	
	Query apply(BooleanExpression context, BooleanExpression pred) {
		for (SubArrayReplacer replacer : generateReplacer(
				getCandidateClause(context))) {
			pred = apply(replacer, pred);
			context = apply(replacer, context);
		}
		return new Query(context, pred);
	}
	
	private BooleanExpression apply(SubArrayReplacer replacer,
			BooleanExpression pred) {
		Stack<ForallStructure> contexts = new Stack<>();

		return (BooleanExpression) applyWorker(replacer, pred, contexts);
	}
	
	private SymbolicExpression applyWorker(SubArrayReplacer replacer,
			SymbolicExpression expr, Stack<ForallStructure> contexts) {
		// if expr is FORALL , push context
		ForallStructure newContext = applyWorkerContext(expr);
		
		if (newContext != null) {
			expr = newContext.body;
			contexts.push(newContext);
		}
		// if expr is array read, try replace:
		SymbolicExpression newExpr = applyWorkerArrayRead(replacer, expr,
				contexts);
		
		if (newExpr != expr) {
			if (newContext != null) {
				contexts.pop();
				newExpr = universe.forallInt(newContext.boundVariable,
						newContext.lowerBound,
						universe.add(newContext.upperBound, universe.oneInt()),
						(BooleanExpression) newExpr);
			}
			return newExpr;
		}
		
		boolean changed = false;
		int numArgs = expr.numArguments();
		SymbolicObject newArgs[] = new SymbolicObject[numArgs];

		for (int i = 0; i < numArgs; i++) {
			SymbolicObject arg = expr.argument(i);

			if (arg.symbolicObjectKind() == SymbolicObjectKind.EXPRESSION) {
				newArgs[i] = applyWorker(replacer, (SymbolicExpression) arg,
						contexts);
				if (newArgs[i] != arg)
					changed = true;
			} else
				newArgs[i] = arg;
		}
		if (changed)
			newExpr = universe.make(expr.operator(), expr.type(), newArgs);
		else
			newExpr = expr;
		if (newContext != null) {
			contexts.pop();
			newExpr = universe.forallInt(newContext.boundVariable,
					newContext.lowerBound,
					universe.add(newContext.upperBound, universe.oneInt()),
					(BooleanExpression) newExpr);
		}
		return newExpr;
	}
	
	private ForallStructure applyWorkerContext(SymbolicExpression expr) {
		if (expr.operator() != SymbolicOperator.FORALL)
			return null;

		ForallStructure structure = universe
				.getForallStructure((BooleanExpression) expr);

		if (structure == null)
			return null;
		return structure;
	}
	
	private SymbolicExpression applyWorkerArrayRead(SubArrayReplacer replacer,
			SymbolicExpression expr, Stack<ForallStructure> contexts) {
		if (expr.operator() == SymbolicOperator.ARRAY_READ) {
			SymbolicExpression array = (SymbolicExpression) expr.argument(0);
			NumericExpression index = (NumericExpression) expr.argument(1);
			ForallStructure context;

			if (contexts.isEmpty())
				return expr;
			context = contexts.peek();
			if (context.boundVariable != index)
				return expr;
			if (replacer.lo != context.lowerBound
					|| replacer.hi != context.upperBound)
				return expr;
			if (array != replacer.arrayReplacee)
				return expr;
			return universe.arrayRead(replacer.arrayReplacer, index);
		}
		return expr;
	}
	/* ***********************************  ****************************** */	
	private List<SubArrayReplacer> generateReplacer(
			List<ArrayElementEqualForalStructure> candidates) {
		List<SubArrayReplacer> results = new LinkedList<>();

		for (ArrayElementEqualForalStructure candidate : candidates) {
			SymbolicExpression array0 = (SymbolicExpression) candidate.arrayRead0
					.argument(0);
			SymbolicExpression array1 = (SymbolicExpression) candidate.arrayRead1
					.argument(0);

			if (array0.operator() != SymbolicOperator.SYMBOLIC_CONSTANT)
				continue;
			if (array1.operator() != SymbolicOperator.SYMBOLIC_CONSTANT)
				continue;
			results.add(new SubArrayReplacer(candidate.structure.lowerBound,
					candidate.structure.upperBound, (SymbolicConstant) array0,
					(SymbolicConstant) array1));
		}
		return results;
	}
	
	private List<ArrayElementEqualForalStructure> getCandidateClause(
			BooleanExpression context) {
		List<ArrayElementEqualForalStructure> results = new LinkedList<>();

		if (context.operator() != SymbolicOperator.AND)
			results.addAll(getIfIsCandidiate(context));
		else {
			for (SymbolicObject cnfClause : context.getArguments()) {
				results.addAll(
						getIfIsCandidiate((BooleanExpression) cnfClause));
			}
		}
		return results;
	}
	
	private List<ArrayElementEqualForalStructure> getIfIsCandidiate(BooleanExpression clause) {
		if (clause.operator() != SymbolicOperator.FORALL)
			return new LinkedList<>();

		ForallStructure structure = universe.getForallStructure(clause);

		if (structure == null || structure.lowerBound != universe.zeroInt())
			return new LinkedList<>();

		List<ArrayElementEqualForalStructure> result = new LinkedList<>();
		ArrayElementEqualForalStructure candidate = isArrayElementEquals(
				structure);
		if (candidate != null)
			result.add(candidate);
		return result;
	}
	
	private ArrayElementEqualForalStructure isArrayElementEquals(ForallStructure structure) {
		BooleanExpression pred = structure.body;
		
		if (pred.operator() != SymbolicOperator.EQUALS)
			return null;
		
		SymbolicExpression left, right;

		left = (SymbolicExpression) pred.argument(0);
		right = (SymbolicExpression) pred.argument(1);
		if (!left.type().isNumeric())
			return null;
		if (!left.isZero())
			return null;
		if (right.operator() != SymbolicOperator.ADD) 
			return null;
		
		NumericExpression operand0 = (NumericExpression) right.argument(0);
		NumericExpression operand1 = universe
				.minus((NumericExpression) right.argument(1));
		
		if (operand0.operator() != SymbolicOperator.ARRAY_READ)
			return null;
		if (operand0.argument(1) != structure.boundVariable)
			return null;
		if (operand1.operator() != SymbolicOperator.ARRAY_READ)
			return null;
		if (operand1.argument(1) != structure.boundVariable)
			return null;
		return new ArrayElementEqualForalStructure(structure, operand0,
				operand1);
	}
}
