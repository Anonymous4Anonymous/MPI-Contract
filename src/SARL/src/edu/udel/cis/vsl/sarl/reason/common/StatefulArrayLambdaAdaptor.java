package edu.udel.cis.vsl.sarl.reason.common;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

import edu.udel.cis.vsl.sarl.IF.UnaryOperator;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericSymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicArrayType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicCompleteArrayType;
import edu.udel.cis.vsl.sarl.preuniverse.IF.PreUniverse;
import edu.udel.cis.vsl.sarl.util.Pair;

/**
 * This transformer transforms all appearances of ARRAY_LAMBDA expression to
 * symbolic constants of array types. The bodies of the ARRAY_LAMBDA expressions
 * will be transformed as assumptions over these array type symbolic constants.
 * 
 * @author xxxx
 */
class StatefulArrayLambdaAdaptor extends ExpressionVisitor
		implements UnaryOperator<SymbolicExpression> {

	private static final String arrayLambdaConstantName = "_arr_const_";

	private static final String boundVarName = "_arr_lambda_bv_";

	private Map<SymbolicExpression, SymbolicConstant> arrayLambdasToArrayConstants;

	private Map<SymbolicConstant, ArrayLambdaAxiom> arrayConstantsToAxioms;

	private Stack<Pair<SymbolicConstant, List<ArrayLambdaAxiom>>> contexualAxioms;

	private List<ArrayLambdaAxiom> independentAxioms;

	private int nameCounter = 0;

	private ArrayLambdaCanonicalization canonicalization;

	private PreUniverse universe;

	StatefulArrayLambdaAdaptor(PreUniverse universe) {
		super(universe);
		this.canonicalization = new ArrayLambdaCanonicalization(universe);
		this.universe = universe;
		this.arrayLambdasToArrayConstants = new TreeMap<>(
				universe.comparator());
		this.arrayConstantsToAxioms = new TreeMap<>(universe.comparator());
		this.contexualAxioms = new Stack<>();
		this.independentAxioms = new LinkedList<>();
	}

	@Override
	public SymbolicExpression apply(SymbolicExpression x) {
		x = canonicalization.apply(x);
		return visitExpression(x);
	}

	public BooleanExpression getIndependentArrayLambdaAxioms() {
		BooleanExpression ret = universe.trueExpression();

		for (ArrayLambdaAxiom axiom : independentAxioms)
			ret = universe.and(ret, arrayLambdaAxiom2BooleanExpression(axiom));
		return ret;
	}

	private SymbolicExpression preProcessArrayLambda(
			SymbolicExpression arrayLambda) {
		SymbolicConstant array = arrayLambdasToArrayConstants.get(arrayLambda);
		ArrayLambdaAxiom axiom;

		if (array == null) {
			array = universe.symbolicConstant(
					universe.stringObject(
							arrayLambdaConstantName + nameCounter++),
					arrayLambda.type());

			arrayLambdasToArrayConstants.put(arrayLambda, array);
			axiom = arrayLambda2Axiom(arrayLambda, array);
			arrayConstantsToAxioms.put(array, axiom);
		} else
			axiom = arrayConstantsToAxioms.get(array);
		if (!contexualAxioms.isEmpty()) {
			// find out the appropriate entry to insert the axiom in:
			Stack<Pair<SymbolicConstant, List<ArrayLambdaAxiom>>> tmpStack = new Stack<>();
			Set<SymbolicConstant> freeConstantsSet = universe
					.getFreeSymbolicConstants(axiom.predicate);
			boolean independent = true;

			while (!contexualAxioms.isEmpty())
				if (freeConstantsSet.contains(contexualAxioms.peek().left)) {
					contexualAxioms.peek().right.add(axiom);
					independent = false;
					break;
				} else
					tmpStack.push(contexualAxioms.pop());
			while (!tmpStack.isEmpty())
				contexualAxioms.push(tmpStack.pop());
			if (independent)
				independentAxioms.add(axiom);
		} else
			independentAxioms.add(axiom);
		return array;
	}

	private ArrayLambdaAxiom arrayLambda2Axiom(SymbolicExpression arrayLambda,
			SymbolicExpression arrayConstant) {
		SymbolicArrayType arrayType = (SymbolicArrayType) arrayLambda.type();
		int dimensions = arrayType.dimensions();
		NumericSymbolicConstant[] boundVars = symbolicConstants(dimensions);
		NumericExpression extents[] = new NumericExpression[dimensions];

		if (arrayType.isComplete())
			extents[0] = ((SymbolicCompleteArrayType) arrayType).extent();

		SymbolicExpression arrayLambdaElement = arrayLambda,
				arrayConstantElement = arrayConstant;

		for (int i = 0; i < dimensions; i++) {
			arrayLambdaElement = universe.arrayRead(arrayLambdaElement,
					boundVars[i]);
			arrayConstantElement = universe.arrayRead(arrayConstantElement,
					boundVars[i]);
			if (i < dimensions - 1) {
				arrayType = (SymbolicArrayType) arrayType.elementType();
				extents[i + 1] = arrayType.isComplete()
						? ((SymbolicCompleteArrayType) arrayType).extent()
						: null;
			}
		}
		assert arrayLambdaElement != null && arrayConstantElement != null;

		BooleanExpression axiom = universe.equals(arrayLambdaElement,
				arrayConstantElement);

		return new ArrayLambdaAxiom(boundVars, extents, axiom);
	}

	private BooleanExpression arrayLambdaAxiom2BooleanExpression(
			ArrayLambdaAxiom axiom) {
		BooleanExpression restriction = inRange(axiom.boundVariables[0],
				universe.zeroInt(), axiom.extents[0]);
		BooleanExpression axiomExpression;

		for (int i = 1; i < axiom.boundVariables.length; i++)
			restriction = universe.and(restriction,
					inRange(axiom.boundVariables[i], universe.zeroInt(),
							axiom.extents[i]));
		axiomExpression = universe.implies(restriction, axiom.predicate);
		for (int i = 0; i < axiom.boundVariables.length; i++)
			axiomExpression = universe.forall(axiom.boundVariables[i],
					axiomExpression);
		return axiomExpression;
	}

	private NumericSymbolicConstant[] symbolicConstants(int num) {
		NumericSymbolicConstant constants[] = new NumericSymbolicConstant[num];

		for (int i = 0; i < num; i++)
			constants[i] = (NumericSymbolicConstant) universe.symbolicConstant(
					universe.stringObject(boundVarName + i),
					universe.integerType());
		return constants;
	}

	private BooleanExpression inRange(NumericSymbolicConstant x,
			NumericExpression inclusiveLow, NumericExpression exclusiveHigh) {
		if (exclusiveHigh != null)
			return universe.and(universe.lessThanEquals(universe.zeroInt(), x),
					universe.lessThan(x, exclusiveHigh));
		else
			return universe.lessThanEquals(universe.zeroInt(), x);
	}

	private class ArrayLambdaAxiom {
		final NumericSymbolicConstant[] boundVariables;

		final NumericExpression[] extents;

		final BooleanExpression predicate;

		ArrayLambdaAxiom(NumericSymbolicConstant[] boundVariables,
				NumericExpression[] extents, BooleanExpression predicate) {
			this.boundVariables = boundVariables;
			this.predicate = predicate;
			this.extents = extents;
		}
	}

	/**
	 * transform from <code>
	 * forall int i. ... (ARRAY_LAMBDA int k. p(i, k))...
	 * </code> to <code>
	 * constant T c[]
	 * 
	 * forall int i.    
	 *   ( 
	 *     (forall int j. c[j] == p(i, j)) =>
	 *  ... c ...
	 *   )
	 * </code>
	 * 
	 * @param expr
	 * @return
	 */
	@Override
	SymbolicExpression visitExpression(SymbolicExpression expr) {
		SymbolicOperator operator = expr.operator();

		if (operator == SymbolicOperator.ARRAY_LAMBDA)
			return preProcessArrayLambda(expr);
		if (operator == SymbolicOperator.FORALL
				|| operator == SymbolicOperator.EXISTS) {
			List<ArrayLambdaAxiom> arrayLambdaAxioms = new LinkedList<>();
			SymbolicConstant boundVar = (SymbolicConstant) expr.argument(0);
			BooleanExpression predicate = (BooleanExpression) expr.argument(1);

			contexualAxioms.push(new Pair<>(boundVar, arrayLambdaAxioms));
			predicate = (BooleanExpression) visitExpressionChildren(predicate);

			List<ArrayLambdaAxiom> axioms = contexualAxioms.pop().right;
			BooleanExpression axiomsExpressions = universe.trueExpression();

			for (ArrayLambdaAxiom axiom : axioms)
				axiomsExpressions = universe.and(axiomsExpressions,
						arrayLambdaAxiom2BooleanExpression(axiom));
			if (operator == SymbolicOperator.FORALL)
				return universe.forall(boundVar,
						universe.implies(axiomsExpressions, predicate));
			else {
				assert operator == SymbolicOperator.EXISTS;
				return universe.exists(boundVar,
						universe.and(axiomsExpressions, predicate));
			}
		}
		return visitExpressionChildren(expr);
	}
}
