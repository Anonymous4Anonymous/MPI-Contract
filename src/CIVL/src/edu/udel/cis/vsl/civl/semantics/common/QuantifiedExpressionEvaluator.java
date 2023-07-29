package edu.udel.cis.vsl.civl.semantics.common;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

import edu.udel.cis.vsl.abc.ast.node.IF.acsl.ExtendedQuantifiedExpressionNode.ExtendedQuantifier;
import edu.udel.cis.vsl.civl.config.IF.CIVLConfiguration;
import edu.udel.cis.vsl.civl.dynamic.IF.SymbolicUtility;
import edu.udel.cis.vsl.civl.library.mpi.LibmpiEvaluator;
import edu.udel.cis.vsl.civl.log.IF.CIVLErrorLogger;
import edu.udel.cis.vsl.civl.model.IF.CIVLInternalException;
import edu.udel.cis.vsl.civl.model.IF.CIVLSource;
import edu.udel.cis.vsl.civl.model.IF.CIVLUnimplementedFeatureException;
import edu.udel.cis.vsl.civl.model.IF.ModelFactory;
import edu.udel.cis.vsl.civl.model.IF.expression.ArrayLambdaExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.BinaryExpression.BINARY_OPERATOR;
import edu.udel.cis.vsl.civl.model.IF.expression.BoundVariableExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.Expression;
import edu.udel.cis.vsl.civl.model.IF.expression.Expression.ExpressionKind;
import edu.udel.cis.vsl.civl.model.IF.expression.ExtendedQuantifiedExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.LambdaExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.QuantifiedExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.QuantifiedExpression.Quantifier;
import edu.udel.cis.vsl.civl.model.IF.expression.RegularRangeExpression;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLCompleteArrayType;
import edu.udel.cis.vsl.civl.model.IF.variable.Variable;
import edu.udel.cis.vsl.civl.semantics.IF.Evaluation;
import edu.udel.cis.vsl.civl.semantics.IF.LibraryEvaluatorLoader;
import edu.udel.cis.vsl.civl.semantics.IF.LibraryExecutorLoader;
import edu.udel.cis.vsl.civl.semantics.IF.SymbolicAnalyzer;
import edu.udel.cis.vsl.civl.semantics.IF.TypeEvaluation;
import edu.udel.cis.vsl.civl.state.IF.MemoryUnitFactory;
import edu.udel.cis.vsl.civl.state.IF.State;
import edu.udel.cis.vsl.civl.state.IF.StateFactory;
import edu.udel.cis.vsl.civl.state.IF.UnsatisfiablePathConditionException;
import edu.udel.cis.vsl.civl.util.IF.Pair;
import edu.udel.cis.vsl.sarl.IF.Reasoner;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericSymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.IF.number.IntegerNumber;
import edu.udel.cis.vsl.sarl.IF.number.Number;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicCompleteArrayType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicFunctionType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;

/**
 * <p>
 * This class is a stateful evaluator for a single expression. All stateful
 * informations kept by this evaluator are only live within the evaluation of
 * the expression period. (Thus, for LibarayEvaluator or LibraryExecutors, their
 * primary evaluator should refer to a instance of this class during such a
 * stateful evaluation period).
 * </p>
 * 
 * <p>
 * This is a stateful evaluator for different kinds of quantified expressions:
 * <ul>
 * <li>Ordinary quantified expression: FORALL or EXISTS</li>
 * <li>Extended quantified expression (fold expression)</li>
 * <li>Lambda expression</li>
 * <li>Array lambda expression</li>
 * <li>Uniform expression</li>
 * <li>Big-O expression</li>
 * </ul>
 * <strong>No array-out-of bound error will be thrown by CIVL during the
 * evaluation of quantified expressions</strong>
 * </p>
 * 
 * The reason of why the above expressions need a stateful evaluation is: All of
 * this expressions are allowed to contain bounded or free variables and
 * restrictions. They can affect the evaluation state on their sub-expressions.
 * 
 * @author xxxx (Ready for review)
 *
 */
public class QuantifiedExpressionEvaluator
		extends
			ErrorSideEffectFreeEvaluator {

	/**
	 * A stack of map used to store bound variables during evaluation of
	 * (possibly nested) quantified expressions. LinkedList is used instead of
	 * Stack because of its more intuitive iteration order.
	 */
	private LinkedList<Map<String, SymbolicExpression>> boundVariableStack = new LinkedList<>();

	/**
	 * This object represents a stack of restrictions specified by
	 * {@link ExtendedQuantifiedExpression}s. One extended quantified expression
	 * specifies a restriction on the free variable of its lambda expression via
	 * defining the bounds on it. For example
	 * <code> \sum(0, 10, \lambda i; i+1); </code> The restriction on free
	 * variable i is <code>0&lt= i &lt=10</code>.
	 */
	private Stack<SymbolicExpression> extendedQuantifiedRestrictionsStack = new Stack<>();

	/**
	 * A Java function interface. An instance of this interface can be assigned
	 * by a Java method which has two {@link BooleanExpression}s as arguments
	 * and returns a {@link BooleanExpression}.
	 * 
	 * @author xxxx
	 *
	 */
	@FunctionalInterface
	private static interface LogicalOperation {
		BooleanExpression operation(BooleanExpression op0,
				BooleanExpression op1);
	}

	/**
	 * A Java function interface. An instance of this interface can be assigned
	 * by a Java method which has a {@link SymbolicConstant} argument and a
	 * {@link BooleanExpression} argument, returns a {@link BooleanExpression}.
	 * 
	 * @author xxxx
	 *
	 */
	@FunctionalInterface
	private static interface ApplyConstantOperation {
		BooleanExpression operation(SymbolicConstant boundVar,
				BooleanExpression pred);
	}

	@Override
	public Evaluation evaluate(State state, int pid, Expression expression)
			throws UnsatisfiablePathConditionException {
		if (expression.expressionKind() == ExpressionKind.BOUND_VARIABLE)
			return evaluateBoundVariable(state, pid,
					(BoundVariableExpression) expression);
		else
			return super.evaluate(state, pid, expression);
	}

	/**
	 * Constructor, parameters are similar to
	 * {@link CommonEvaluator#CommonEvaluator(ModelFactory, StateFactory, LibraryEvaluatorLoader, LibraryExecutorLoader, SymbolicUtility, SymbolicAnalyzer, MemoryUnitFactory, CIVLErrorLogger, CIVLConfiguration)}
	 */
	QuantifiedExpressionEvaluator(ModelFactory modelFactory,
			StateFactory stateFactory, LibraryEvaluatorLoader loader,
			LibraryExecutorLoader loaderExec, SymbolicUtility symbolicUtil,
			SymbolicAnalyzer symbolicAnalyzer, MemoryUnitFactory memUnitFactory,
			CIVLErrorLogger errorLogger, CIVLConfiguration config) {
		super(modelFactory, stateFactory, loader, loaderExec, symbolicUtil,
				symbolicAnalyzer, memUnitFactory, errorLogger, config);
	}

	/**
	 * <p>
	 * Evaluate an {@link ArrayLambdaExpression}
	 * </p>
	 * 
	 * @param state
	 *            The state where the evaluation happens
	 * @param pid
	 *            The PID of the process who invokes the evaluation
	 * @param arrayLambda
	 *            The expression that will be evaluated
	 * @return The evaluation result
	 * @throws UnsatisfiablePathConditionException
	 */
	@Override
	protected Evaluation evaluateArrayLambda(State state, int pid,
			ArrayLambdaExpression arrayLambda)
			throws UnsatisfiablePathConditionException {
		List<Pair<List<Variable>, Expression>> boundVariableList = arrayLambda
				.boundVariableList();
		CIVLCompleteArrayType exprType = arrayLambda.getExpressionType();
		NumericSymbolicConstant[] boundVariables;
		TypeEvaluation typeEval = getDynamicType(state, pid, exprType,
				arrayLambda.getSource(), false);
		SymbolicCompleteArrayType arrayType = (SymbolicCompleteArrayType) typeEval.type;
		Evaluation eval;
		int numBoundVars = 0;

		boundVariables = new NumericSymbolicConstant[exprType.dimension()];
		state = typeEval.state;
		boundVariableStack.push(new TreeMap<>());
		for (Pair<List<Variable>, Expression> boundVariableSubList : boundVariableList) {
			if (boundVariableSubList.right != null)
				throw new CIVLUnimplementedFeatureException(
						"declaring bound variables within a specific domain in array lambdas",
						arrayLambda.getSource());
			for (Variable variable : boundVariableSubList.left) {
				NumericSymbolicConstant boundVariable;

				assert variable.type().isIntegerType();
				boundVariable = (NumericSymbolicConstant) universe
						.symbolicConstant(variable.name().stringObject(),
								variable.type().getDynamicType(universe));
				boundVariables[numBoundVars++] = boundVariable;
				boundVariableStack.peek().put(boundVariable.name().getString(),
						boundVariable);
			}
		}
		assert exprType.dimension() == numBoundVars;
		if (arrayLambda.restriction() != null) {
			eval = evaluate(state, pid, arrayLambda.restriction());
			if (!eval.value.isTrue())
				throw new CIVLUnimplementedFeatureException(
						"non-trivial restriction expression in array lambdas",
						arrayLambda.getSource());
		}
		eval = new Evaluation(state, arrayLambda(state, pid, boundVariables, 0,
				arrayType, arrayLambda.expression()));
		boundVariableStack.pop();
		return eval;
	}

	/**
	 * Evaluates a bound variable expression.
	 * 
	 * @param state
	 *            The state where the evaluation happens.
	 * @param pid
	 *            The PID of the process who invokes the evaluation.
	 * @param expression
	 *            The bound variable expression to be evaluated.
	 * @return A possibly new state resulted from side effects during the
	 *         evaluation and the value of the bound variable expression.
	 * @throws UnsatisfiablePathConditionException
	 */
	Evaluation evaluateBoundVariable(State state, int pid,
			BoundVariableExpression expression) {
		SymbolicExpression value = null;
		String name = expression.name().name();

		for (Map<String, SymbolicExpression> boundVariableSet : boundVariableStack) {
			value = boundVariableSet.get(name);
			if (value != null)
				break;
		}
		if (value == null)
			throw new CIVLInternalException(
					"unreachable: unknown bound variable",
					expression.getSource());
		if (value.isNumeric()) {
			Reasoner reasoner = universe
					.reasoner(state.getPathCondition(universe));
			Number number = reasoner.extractNumber((NumericExpression) value);

			if (number != null)
				return new Evaluation(state, universe.number(number));
		}
		return new Evaluation(state, value);
	}

	@Override
	protected Evaluation evaluateQuantifiedExpression(State state, int pid,
			QuantifiedExpression expression)
			throws UnsatisfiablePathConditionException {
		List<Pair<List<Variable>, Expression>> boundVariableList = expression
				.boundVariableList();
		Evaluation eval;
		int numBoundVars = expression.numBoundVariables();
		SymbolicConstant[] boundVariables = new SymbolicConstant[numBoundVars];
		BooleanExpression restriction = processBoundVariableList(state, pid,
				boundVariableList, boundVariables, expression.getSource());

		eval = evaluate(state, pid, expression.restriction());
		state = eval.state;
		restriction = universe.and(restriction, (BooleanExpression) eval.value);

		/* optimization for FORALL when restriction can be elaborated */
		if (expression.quantifier() == Quantifier.FORALL
				&& boundVariables.length == 1) {
			Iterable<NumericExpression> elaboration = isRestrictionElaboratable(
					boundVariables[0], restriction);

			if (elaboration != null) {
				eval = elaborateForallRestriction(state, pid,
						expression.expression(), boundVariables[0],
						elaboration);
				boundVariableStack.pop();
				return eval;
			}
		}

		// Temporarily add restriction into path condition:
		State newState = stateFactory.addToPathcondition(state, pid,
				restriction);
		BooleanExpression predicate;

		try {
			predicate = (BooleanExpression) evaluate(newState, pid,
					expression.expression()).value;
		} catch (UnsatisfiablePathConditionException e) {
			// since the restriction is pushed into the context for evaluating
			// the predicate, if an unsatisfiable exception was caught, which
			// means the "restriction && context" is unsatisfiable hence this
			// expression evaluates to either true (forall) or false (exists).
			switch (expression.quantifier()) {
				case EXISTS :
					return new Evaluation(state, universe.falseExpression());
				case FORALL :
					return new Evaluation(state, universe.trueExpression());
				default :
					throw new CIVLInternalException(
							"Unknown quantifier: " + expression.quantifier(),
							expression.getSource());
			}
		}
		// function references:
		// Either "restriction AND predicate" or "restriction IMPLIES
		// predicate" ?
		LogicalOperation restirctionCombiner;
		// Either "exists" or "forall" ?
		ApplyConstantOperation quantifiedExpression;

		switch (expression.quantifier()) {
			case EXISTS :
				restirctionCombiner = universe::and;
				quantifiedExpression = universe::exists;
				break;
			case FORALL :
				restirctionCombiner = universe::implies;
				quantifiedExpression = universe::forall;
				break;
			default :
				throw new CIVLInternalException(
						"Unknown quantifier: " + expression.quantifier(),
						expression.getSource());
		}
		predicate = restirctionCombiner.operation(restriction, predicate);
		for (SymbolicConstant complexBoundVar : boundVariables)
			predicate = quantifiedExpression.operation(complexBoundVar,
					predicate);
		eval = new Evaluation(state, predicate);
		boundVariableStack.pop();
		return eval;
	}

	/**
	 * Evaluate an {@link ExtendedQuantifiedExpression}
	 * 
	 * @throws UnsatisfiablePathConditionException
	 */
	@Override
	protected Evaluation evaluateExtendedQuantifiedExpression(State state,
			int pid, ExtendedQuantifiedExpression extQuant)
			throws UnsatisfiablePathConditionException {
		Evaluation eval;
		Expression function = extQuant.function();
		NumericExpression low, high;
		ExtendedQuantifier quant = extQuant.extendedQuantifier();
		CIVLSource source = extQuant.getSource();
		Set<SymbolicConstant> symConsts;

		eval = evaluate(state, pid, extQuant.lower());
		state = eval.state;
		low = (NumericExpression) eval.value;
		eval = evaluate(state, pid, extQuant.higher());
		high = (NumericExpression) eval.value;
		state = eval.state;
		symConsts = low.getFreeVars();
		symConsts.addAll(high.getFreeVars());

		int nameCounter = 0;
		NumericSymbolicConstant idx = (NumericSymbolicConstant) universe
				.symbolicConstant(universe.stringObject("i" + nameCounter++),
						universe.integerType());

		while (symConsts.contains(idx))
			idx = (NumericSymbolicConstant) universe.symbolicConstant(
					universe.stringObject("i" + nameCounter++),
					universe.integerType());

		BooleanExpression restriction = universe.lessThanEquals(low, idx);
		Number lowNum, highNum;
		Reasoner reasoner = universe.reasoner(state.getPathCondition(universe));
		TypeEvaluation typeEval = this.getDynamicType(state, pid,
				extQuant.getExpressionType(), source, false);

		restriction = universe.and(restriction,
				universe.lessThanEquals(idx, high));
		// To deal with nested extended-quantified expressions:
		// Push a lambda function into the stack. During the evaluation of the
		// extended-quantified expression, applying the top stack entry to a
		// free variable in a lambda expression will return a boolean-value
		// restriction for the free variable:
		extendedQuantifiedRestrictionsStack
				.push(universe.lambda(idx, restriction));
		lowNum = reasoner.extractNumber(low);
		highNum = reasoner.extractNumber(high);
		// Using different helper methods for concrete and non-concrete cases:
		if (lowNum != null && highNum != null) {
			int lowInt, highInt;

			lowInt = ((IntegerNumber) lowNum).intValue();
			highInt = ((IntegerNumber) highNum).intValue();
			eval = computeConcreteFoldExpression(state, pid, lowInt, highInt,
					(LambdaExpression) function, quant, typeEval.type, source);
		} else {
			eval = evaluate(typeEval.state, pid, function);
			eval = computeNonconcreteFoldExpression(eval.state, pid, reasoner,
					low, high, eval.value, quant, source);
		}
		extendedQuantifiedRestrictionsStack.pop();
		return eval;
	}

	/**
	 * 
	 * <p>
	 * Evaluate {@link ExtendedQuantifiedExpression} e(i,j,f):
	 * <code>f(i) op f(i+1) op ... op f(j)</code> where i &lt= j and op stands
	 * for an {@link ExtendedQuantifier}.
	 * 
	 * This method requires both i and j have non-concrete values.
	 * 
	 * If j > i can be proved, an induction step will be added to the path
	 * condition: <code>e(i,j,f) == e(i,j-1,f) + f(j)</code>
	 * </p>
	 * 
	 * @param state
	 *            The current state when this method is called
	 * @param reasoner
	 *            A reference to a {@link Reasoner}
	 * @param low
	 *            The lower bound of the parameter.
	 * @param high
	 *            The higher bound of the parameter.
	 * @param lambda
	 *            The lambda expression which maps the parameter to an
	 *            expression
	 * @param quant
	 *            The {@link ExtendedQuantifier} which is a kind of a binary
	 *            operator
	 * @param source
	 *            The {@link CIVLSource} related to this method call
	 * @return
	 */
	private Evaluation computeNonconcreteFoldExpression(State state, int pid,
			Reasoner reasoner, NumericExpression low, NumericExpression high,
			SymbolicExpression lambda, ExtendedQuantifier quant,
			CIVLSource source) {
		NumericExpression result;

		if (reasoner.isValid(universe.lessThan(high, low))) {
			result = ((SymbolicFunctionType) lambda.type()).outputType()
					.isInteger() ? universe.zeroInt() : universe.zeroReal();
			return new Evaluation(state, result);
		}
		switch (quant) {
			case SUM :
				// Higher bound in SARL's sigma is exclusive:
				result = universe.sigma(low,
						universe.add(high, universe.oneInt()), lambda);
				break;
			default :
				throw new CIVLUnimplementedFeatureException(
						"evaluating non-concrete extended quantification "
								+ quant,
						source);
		}
		return new Evaluation(state, result);
	}

	/**
	 * <p>
	 * Evaluate {@link ExtendedQuantifiedExpression} :
	 * <code>f(i) op f(i+1) op ... op f(j)</code> where i &lt= j and op stands
	 * for an {@link ExtendedQuantifier}.
	 * 
	 * This method requires both i and j have concrete values.
	 * </p>
	 * 
	 * @param low
	 *            The lower bound of the parameter.
	 * @param high
	 *            The higher bound of the parameter.
	 * @param lambda
	 *            The lambda expression which maps the parameter to an
	 *            expression
	 * @param quant
	 *            The {@link ExtendedQuantifier} which is a kind of a binary
	 *            operator
	 * @param expressionType
	 *            The expression type of this
	 *            {@link ExtendedQuantifiedExpression}.
	 * @param source
	 *            The {@link CIVLSource} related to this method call
	 * @return
	 * @throws UnsatisfiablePathConditionException
	 */
	private Evaluation computeConcreteFoldExpression(State state, int pid,
			int low, int high, LambdaExpression function,
			ExtendedQuantifier quant, SymbolicType expressionType,
			CIVLSource source) throws UnsatisfiablePathConditionException {
		if (high < low)
			return new Evaluation(state,
					expressionType.isInteger()
							? universe.zeroInt()
							: universe.zeroReal());

		Map<String, SymbolicExpression> boundVarSet = new HashMap<>();
		NumericExpression result;
		Evaluation eval;

		boundVarSet.put(function.freeVariable().name().name(),
				universe.integer(low));
		boundVariableStack.push(boundVarSet);
		eval = evaluate(state, pid, function.lambdaFunction());
		state = eval.state;
		result = (NumericExpression) eval.value;
		boundVariableStack.pop();

		for (int i = low + 1; i <= high; i++) {
			NumericExpression index = universe.integer(i);
			NumericExpression current;

			boundVarSet.clear();
			boundVarSet.put(function.freeVariable().name().name(), index);
			boundVariableStack.push(boundVarSet);
			eval = evaluate(state, pid, function.lambdaFunction());
			state = eval.state;
			current = (NumericExpression) eval.value;
			boundVariableStack.pop();
			switch (quant) {
				case SUM :
					result = universe.add(result, current);
					break;
				case PROD :
					result = universe.multiply(result, current);
					break;
				default :
					throw new CIVLUnimplementedFeatureException(
							"evaluating concrete extended quantification "
									+ quant,
							source);
			}
		}
		return new Evaluation(state, result);
	}

	@Override
	protected Evaluation evaluateLambda(State state, int pid,
			LambdaExpression lambda)
			throws UnsatisfiablePathConditionException {
		Variable freeVariable = lambda.freeVariable();
		Evaluation eval = null;
		TypeEvaluation typeEval;
		SymbolicType varType;
		NumericSymbolicConstant freeVariableValue;

		typeEval = getDynamicType(state, pid, freeVariable.type(),
				freeVariable.getSource(), false);
		state = typeEval.state;
		varType = typeEval.type;
		boundVariableStack.push(new TreeMap<>());
		freeVariableValue = (NumericSymbolicConstant) universe
				.symbolicConstant(freeVariable.name().stringObject(), varType);
		boundVariableStack.peek().put(freeVariableValue.name().getString(),
				freeVariableValue);

		State oldState = state;

		if (!extendedQuantifiedRestrictionsStack.isEmpty()) {
			SymbolicExpression restrictFunction = extendedQuantifiedRestrictionsStack
					.peek();
			BooleanExpression restriction;

			restriction = (BooleanExpression) universe.apply(restrictFunction,
					Arrays.asList(freeVariableValue));
			assert restriction.type()
					.typeKind() == SymbolicType.SymbolicTypeKind.BOOLEAN;
			state = stateFactory.addToPathcondition(state, pid, restriction);
		}
		eval = evaluate(state, pid, lambda.lambdaFunction());
		eval.state = oldState;
		eval.value = universe.lambda(freeVariableValue, eval.value);
		boundVariableStack.pop();
		return eval;
	}

	@Override
	protected Evaluation evaluateValid(State state, int pid, Expression pointer,
			Expression offsets, CIVLSource source)
			throws UnsatisfiablePathConditionException {
		Evaluation eval;
		boolean ofMPIBufType = pointer.getExpressionType() == typeFactory
				.mpiBufType();

		if (offsets.getExpressionType().isIntegerType()) {
			if (!ofMPIBufType) {
				Expression singlePointer = modelFactory.binaryExpression(source,
						BINARY_OPERATOR.POINTER_ADD, pointer, offsets);

				eval = evaluate(state, pid, singlePointer);
				eval.value = symbolicAnalyzer.isDerefablePointer(state,
						eval.value).left;
			} else {
				eval = evaluate(state, pid, pointer);
				state = eval.state;
				// check \valid(\mpi_buf) and \valid(\mpi_buf + offset):
				eval = LibmpiEvaluator.isDerefableMPIBuf(state, pid, pointer,
						eval.value, symbolicAnalyzer, symbolicUtil, this);

				BooleanExpression isPointerValid = (BooleanExpression) eval.value;

				if (!isPointerValid.isFalse()) {
					Expression singlePointer = modelFactory.binaryExpression(
							source, BINARY_OPERATOR.POINTER_ADD, pointer,
							offsets);

					state = eval.state;
					eval = evaluate(state, pid, singlePointer);
					eval = LibmpiEvaluator.isDerefableMPIBuf(state, pid,
							singlePointer, eval.value, symbolicAnalyzer,
							symbolicUtil, this);
					eval.value = universe.and(isPointerValid,
							(BooleanExpression) eval.value);
				}
			}
			return eval;
		} else {
			if (ofMPIBufType)
				throw new CIVLUnimplementedFeatureException(
						"\\mpi_buf_t object plus a non-integer offset "
								+ offsets);

			// for \valid(p + range), it is evaluated as
			// forall int i. i in range -> dereferable(p + i)
			RegularRangeExpression range = (RegularRangeExpression) offsets;
			// step is always one ...
			NumericExpression lowVal, highVal;
			NumericSymbolicConstant offset = (NumericSymbolicConstant) universe
					.symbolicConstant(
							universe.stringObject(BOUNDED_OFFSET_IDENTIFIER),
							universe.integerType());

			eval = evaluate(state, pid, range);
			state = eval.state;
			lowVal = symbolicUtil.getLowOfRegularRange(eval.value);
			highVal = symbolicUtil.getHighOfRegularRange(eval.value);

			BooleanExpression offsetBounds = universe.and(
					universe.lessThanEquals(lowVal, offset),
					universe.lessThanEquals(offset, highVal));
			Expression boundVar = modelFactory.boundVariableExpression(
					offsets.getSource(),
					modelFactory.identifier(offsets.getSource(),
							BOUNDED_OFFSET_IDENTIFIER),
					typeFactory.integerType());
			Expression eachPointer = modelFactory.binaryExpression(source,
					BINARY_OPERATOR.POINTER_ADD, pointer, boundVar);

			state = stateFactory.pushAssumption(state, pid, offsetBounds);
			boundVariableStack.push(new TreeMap<>());
			boundVariableStack.peek().put(BOUNDED_OFFSET_IDENTIFIER, offset);
			eval = evaluate(state, pid, eachPointer);
			eval.value = symbolicAnalyzer.isDerefablePointer(state,
					eval.value).left;
			boundVariableStack.pop();
			eval.state = stateFactory.popAssumption(eval.state, pid);
			eval.value = universe.forallInt(offset, lowVal, highVal,
					(BooleanExpression) eval.value);
			return eval;
		}
	}

	/* ********************** Private helper methods ************************ */
	/**
	 * Evaluate a list of bound variables to a set of symbolic constants. For
	 * bound variables bounded by domains, their constraints will be returned as
	 * a boolean expression.
	 * 
	 * @param state
	 *            The current state
	 * @param pid
	 *            The PID of the calling process.
	 * @param boundVariableList
	 *            A list of bound variable groups. Each bound variable group
	 *            shares a domain constraint or has no domain constraint.
	 * @param boundVariables
	 *            Output argument. An array eventually will contain symbolic
	 *            constants which are values of bound variables.
	 * @param source
	 *            CIVLSource associates to those bounded variables.
	 * @return a constraint on some variables which are bounded by domains.
	 * @throws UnsatisfiablePathConditionException
	 */
	private BooleanExpression processBoundVariableList(State state, int pid,
			List<Pair<List<Variable>, Expression>> boundVariableList,
			SymbolicConstant[] boundVariables, CIVLSource source)
			throws UnsatisfiablePathConditionException {
		BooleanExpression restriction = universe.trueExpression();
		Evaluation eval;
		int index = 0;

		this.boundVariableStack.push(new HashMap<>());
		for (Pair<List<Variable>, Expression> boundVariableSubList : boundVariableList) {
			List<Variable> boundVariableDecls = boundVariableSubList.left;
			Expression domain = boundVariableSubList.right;
			SymbolicConstant boundValue;

			if (domain != null && boundVariableDecls.size() != 1)
				throw new CIVLUnimplementedFeatureException(
						"declaring bound variables within a specific domain in quantified expressions",
						source);
			if (domain != null) {
				// range
				Variable boundVar = boundVariableDecls.get(0);
				SymbolicExpression range;
				NumericExpression lower, upper;

				boundValue = universe.symbolicConstant(
						boundVar.name().stringObject(),
						boundVar.type().getDynamicType(universe));
				eval = this.evaluate(state, pid, domain);
				// TODO assert domain has dimension one
				boundVariables[index++] = boundValue;
				this.boundVariableStack.peek()
						.put(boundValue.name().getString(), boundValue);
				state = eval.state;
				range = eval.value;
				lower = this.symbolicUtil.getLowOfRegularRange(range);
				upper = this.symbolicUtil.getHighOfRegularRange(range);
				restriction = universe.and(restriction, universe.and(
						this.universe.lessThanEquals(lower,
								(NumericExpression) boundValue),
						this.universe.lessThanEquals(
								(NumericExpression) boundValue, upper)));
			} else {
				for (Variable boundVar : boundVariableDecls) {
					boundValue = universe.symbolicConstant(
							boundVar.name().stringObject(),
							boundVar.type().getDynamicType(universe));
					boundVariables[index++] = boundValue;
					this.boundVariableStack.peek()
							.put(boundValue.name().getString(), boundValue);
				}
			}
		}
		return restriction;
	}

	Iterable<NumericExpression> isRestrictionConstantOfBV(SymbolicConstant bv,
			BooleanExpression restriction) {
		if (restriction.operator() != SymbolicOperator.EQUALS)
			return null;

		NumericExpression op0, op1;
		List<NumericExpression> result = new LinkedList<>();

		op0 = (NumericExpression) restriction.argument(0);
		op1 = (NumericExpression) restriction.argument(1);

		if (op0 == bv) {
			result.add(op1);
			return result;
		}
		if (op1 == bv) {
			result.add(op0);
			return result;
		}
		if (op0 == universe.zeroInt()) {
			if (op1.operator() == SymbolicOperator.ADD
					&& op1.argument(0) == bv) {
				result.add(universe.minus((NumericExpression) op1.argument(1)));
				return result;
			}
		}
		return null;
	}

	private Iterable<NumericExpression> isRestrictionElaboratable(
			SymbolicConstant bv, BooleanExpression restriction) {
		if (!bv.type().isInteger())
			return null;
		if (restriction.operator() != SymbolicOperator.AND)
			return isRestrictionConstantOfBV(bv, restriction);

		BooleanExpression cond0 = (BooleanExpression) restriction.argument(0);
		BooleanExpression cond1 = (BooleanExpression) restriction.argument(1);
		NumericExpression lower = null, upper = null;
		NumericExpression[] bounds = isBounding(bv, cond0);

		lower = bounds[0];
		upper = bounds[1];
		bounds = isBounding(bv, cond1);
		if (lower == null && bounds[0] != null)
			lower = bounds[0];
		if (upper == null && bounds[1] != null)
			upper = bounds[1];
		if (lower == null || upper == null)
			return null;

		IntegerNumber lowerConc, upperConc;

		lowerConc = (IntegerNumber) universe.extractNumber(lower);
		upperConc = (IntegerNumber) universe.extractNumber(upper);
		if (lowerConc == null || upperConc == null)
			return null;

		int lowerInt = lowerConc.intValue();
		int upperInt = upperConc.intValue();
		List<NumericExpression> elaboratedResult = new LinkedList<>();

		for (int i = lowerInt; i <= upperInt; i++) {
			elaboratedResult.add(universe.integer(i));
		}
		return elaboratedResult;
	}

	private NumericExpression[] isBounding(SymbolicConstant bv,
			BooleanExpression cond) {
		NumericExpression[] result = new NumericExpression[]{null, null};

		if (cond.operator() != SymbolicOperator.LESS_THAN_EQUALS)
			return result;

		NumericExpression left = (NumericExpression) cond.argument(0);
		NumericExpression right = (NumericExpression) cond.argument(1);

		if (left.equals(zero)) {
			// 0 <= bv + x or 0 <= x + bv or 0 <= bv
			if (right.equals(bv))
				result[0] = zero;
			else if (right.operator() == SymbolicOperator.ADD) {
				if (right.argument(0).equals(bv))
					result[0] = universe
							.minus((NumericExpression) right.argument(1));
				else if (right.argument(1).equals(bv))
					result[0] = universe
							.minus((NumericExpression) right.argument(0));
			}
		}
		if (right.equals(zero)) {
			// bv + x <= 0 or x + bv <= 0 or bv <= 0
			if (left.equals(bv))
				result[1] = zero;
			else if (left.operator() == SymbolicOperator.ADD) {
				if (left.argument(0).equals(bv))
					result[1] = universe
							.minus((NumericExpression) left.argument(1));
				else if (right.argument(1).equals(bv))
					result[1] = universe
							.minus((NumericExpression) left.argument(0));
			}
		}
		return result;
	}

	private Evaluation elaborateForallRestriction(State state, int pid,
			Expression predicate, SymbolicConstant bv,
			Iterable<NumericExpression> elaboration)
			throws UnsatisfiablePathConditionException {
		BooleanExpression pred = universe.trueExpression();
		Evaluation eval;

		for (NumericExpression val : elaboration) {
			boundVariableStack.peek().put(bv.name().getString(), val);
			eval = evaluate(state, pid, predicate);
			state = eval.state;
			pred = universe.and(pred, (BooleanExpression) eval.value);
		}
		return new Evaluation(state, pred);
	}

	// hack:
	public Evaluation evaluateLambdaWithConcreteFreeVar(State state, int pid,
			LambdaExpression expression, int freeVarVal)
			throws UnsatisfiablePathConditionException {
		Map<String, SymbolicExpression> bvMap = new HashMap<>();
		Evaluation eval;

		bvMap.put(expression.freeVariable().name().name(),
				universe.integer(freeVarVal));
		this.boundVariableStack.push(bvMap);
		eval = evaluate(state, pid, expression.lambdaFunction());
		this.boundVariableStack.pop();
		return eval;
	}
}
