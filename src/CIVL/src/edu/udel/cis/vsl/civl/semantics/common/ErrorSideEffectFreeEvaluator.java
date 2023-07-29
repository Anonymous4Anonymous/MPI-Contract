package edu.udel.cis.vsl.civl.semantics.common;

import java.util.Arrays;

import edu.udel.cis.vsl.civl.config.IF.CIVLConfiguration;
import edu.udel.cis.vsl.civl.dynamic.IF.SymbolicUtility;
import edu.udel.cis.vsl.civl.library.mpi.LibmpiEvaluator;
import edu.udel.cis.vsl.civl.log.IF.CIVLErrorLogger;
import edu.udel.cis.vsl.civl.model.IF.CIVLSource;
import edu.udel.cis.vsl.civl.model.IF.CIVLUnimplementedFeatureException;
import edu.udel.cis.vsl.civl.model.IF.ModelFactory;
import edu.udel.cis.vsl.civl.model.IF.expression.BinaryExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.Expression;
import edu.udel.cis.vsl.civl.model.IF.expression.SubscriptExpression;
import edu.udel.cis.vsl.civl.semantics.IF.Evaluation;
import edu.udel.cis.vsl.civl.semantics.IF.Evaluator;
import edu.udel.cis.vsl.civl.semantics.IF.LibraryEvaluatorLoader;
import edu.udel.cis.vsl.civl.semantics.IF.LibraryExecutorLoader;
import edu.udel.cis.vsl.civl.semantics.IF.SymbolicAnalyzer;
import edu.udel.cis.vsl.civl.state.IF.MemoryUnitFactory;
import edu.udel.cis.vsl.civl.state.IF.State;
import edu.udel.cis.vsl.civl.state.IF.StateFactory;
import edu.udel.cis.vsl.civl.state.IF.UnsatisfiablePathConditionException;
import edu.udel.cis.vsl.civl.util.IF.Pair;
import edu.udel.cis.vsl.sarl.IF.ValidityResult.ResultType;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.ReferenceExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicFunctionType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;

/**
 * An error side-effects free alternative of {@link CommonEvaluator}. This
 * evaluator will not report errors for error side-effects during the
 * evaluation.
 * 
 * @author xxxx
 *
 */
public class ErrorSideEffectFreeEvaluator extends CommonEvaluator
		implements
			Evaluator {

	/**
	 * The {@link Exception} that will be thrown only by
	 * {@link ErrorSideEffectFreeEvaluator} when an erroneous side effect
	 * happens.
	 * 
	 * @author xxxx
	 */
	static public class ErroneousSideEffectException
			extends
				UnsatisfiablePathConditionException {
		/**
		 * The {@link SymbolicExpression} that really causes the side effect
		 * error, which will be used to as a key for generating a unique
		 * undefined value
		 */
		public final SymbolicExpression keyValue;
		/**
		 * generated serial ID
		 */
		private static final long serialVersionUID = -1237052183722755533L;

		/**
		 * @param keyValue
		 *            The {@link SymbolicExpression} that really causes the side
		 *            effect error, which will be used to as a key for
		 *            generating a unique undefined value
		 */
		public ErroneousSideEffectException(SymbolicExpression keyValue) {
			this.keyValue = keyValue;
		}
	}

	/**
	 * The name of an abstract function which will wrap a
	 * {@link ErroneousSideEffectException#keyValue}. A such function call
	 * represents a unique undefined value of some type.
	 */
	private static String SEError_ABSTRACT_FUNCTION_NAME = "SEError_undefined";

	public ErrorSideEffectFreeEvaluator(ModelFactory modelFactory,
			StateFactory stateFactory, LibraryEvaluatorLoader loader,
			LibraryExecutorLoader loaderExec, SymbolicUtility symbolicUtil,
			SymbolicAnalyzer symbolicAnalyzer, MemoryUnitFactory memUnitFactory,
			CIVLErrorLogger errorLogger, CIVLConfiguration config) {
		super(modelFactory, stateFactory, loader, loaderExec, symbolicUtil,
				symbolicAnalyzer, memUnitFactory, errorLogger, config);
	}

	@Override
	public Evaluation evaluate(State state, int pid, Expression expression)
			throws UnsatisfiablePathConditionException {
		try {
			return super.evaluate(state, pid, expression);
		} catch (ErroneousSideEffectException e) {
			SymbolicType exprType = expression.getExpressionType()
					.getDynamicType(universe);
			SymbolicFunctionType funcType = universe
					.functionType(Arrays.asList(e.keyValue.type()), exprType);

			return new Evaluation(state, universe.apply(
					universe.symbolicConstant(universe.stringObject(
							SEError_ABSTRACT_FUNCTION_NAME), funcType),
					Arrays.asList(e.keyValue)));
		}
	}

	@Override
	public Evaluation dereference(CIVLSource source, State state,
			String process, SymbolicExpression pointer, boolean checkOutput,
			boolean strict) throws UnsatisfiablePathConditionException {
		boolean muteErrorSideEffects = true; // mute error side effects

		return dereferenceWorker(source, state, process, pointer, checkOutput,
				false, strict, muteErrorSideEffects);
	}

	@Override
	protected Evaluation evaluateSubscript(State state, int pid, String process,
			SubscriptExpression expression)
			throws UnsatisfiablePathConditionException {
		return evaluateSubscriptWorker(state, pid, process, expression, true);
	}

	@Override
	protected Evaluation evaluateDivide(State state, int pid,
			BinaryExpression expression, NumericExpression numerator,
			NumericExpression denominator)
			throws UnsatisfiablePathConditionException {
		return evaluateDivideWorker(state, pid, expression, numerator,
				denominator, true);
	}

	@Override
	protected Evaluation evaluateModulo(State state, int pid,
			BinaryExpression expression, NumericExpression numerator,
			NumericExpression denominator)
			throws UnsatisfiablePathConditionException {
		return evaluateModuloWorker(state, pid, expression, numerator,
				denominator, true);
	}

	@Override
	public Pair<Evaluation, NumericExpression[]> arrayElementReferenceAdd(
			State state, int pid, SymbolicExpression ptr,
			NumericExpression offset, CIVLSource source)
			throws UnsatisfiablePathConditionException {
		SymbolicExpression newPtr = symbolicUtil.makePointer(ptr,
				symbolicAnalyzer.getLeafNodeReference(state, ptr, source));

		return arrayElementReferenceAddWorker(state, pid, newPtr, offset, true,
				source);
	}

	@Override
	public Evaluation evaluatePointerAdd(State state, int pid,
			BinaryExpression expression, SymbolicExpression pointer,
			SymbolicExpression offset)
			throws UnsatisfiablePathConditionException {
		NumericExpression count = null, datatype = null;
		/*
		 * wraps evaluateCIVLCPointerAdd with handling of \mpi_but_t arithmetic
		 * semantics.
		 */
		if (expression.getExpressionType() == typeFactory.mpiBufType()) {
			count = (NumericExpression) universe.tupleRead(pointer,
					universe.intObject(1));
			datatype = (NumericExpression) universe.tupleRead(pointer,
					universe.intObject(2));
			pointer = universe.tupleRead(pointer, universe.intObject(0));

			Pair<SymbolicExpression, NumericExpression> ptr_oft = LibmpiEvaluator
					.mpiBufAddition(state, pid, pointer, datatype,
							(NumericExpression) offset, symbolicAnalyzer,
							symbolicUtil, this, expression.getSource());

			pointer = ptr_oft.left;
			offset = ptr_oft.right;
		}

		Evaluation eval = evaluateCIVLCPointerAdd(state, pid, expression,
				pointer, offset);

		if (expression.getExpressionType() == typeFactory.mpiBufType()) {
			eval.value = universe.tuple(
					typeFactory.mpiBufType().getDynamicType(universe),
					Arrays.asList(eval.value, count, datatype));
		}
		return eval;
	}
	
	/**
	 * the original version of
	 * {@link #evaluatePointerAdd(State, int, BinaryExpression, SymbolicExpression, SymbolicExpression)}
	 * that ONLY deals with CIVL-C pointer arithmetic semantics.
	 */
	private Evaluation evaluateCIVLCPointerAdd(State state, int pid,
			BinaryExpression expression, SymbolicExpression pointer,
			SymbolicExpression offset)
			throws UnsatisfiablePathConditionException {
		Pair<BooleanExpression, ResultType> checkPointer = symbolicAnalyzer
				.isDefinedPointer(state, pointer, expression.getSource());

		if (checkPointer.right != ResultType.YES)
			return new Evaluation(state, symbolicUtil.undefinedPointer());
		else {
			ReferenceExpression symRef = symbolicUtil.getSymRef(pointer);

			if (symRef.isArrayElementReference()) {
				return arrayElementReferenceAddWorker(state, pid, pointer,
						(NumericExpression) offset, true,
						expression.left().getSource()).left;
			} else if (symRef.isOffsetReference()) {
				return offsetReferenceAddition(state, pid, pointer,
						(NumericExpression) offset, true,
						expression.getSource());
			} else if (symRef.isIdentityReference()) {
				return identityReferenceAddition(state, pid, pointer,
						(NumericExpression) offset, true,
						expression.getSource());
			} else
				throw new CIVLUnimplementedFeatureException(
						"Pointer addition for anything other than array elements or variables",
						expression);
		}
	}
	
	public QuantifiedExpressionEvaluator newQuantifiedExpressionEvaluator() {
		return new QuantifiedExpressionEvaluator(modelFactory, stateFactory,
				libLoader, libExeLoader, symbolicUtil, symbolicAnalyzer,
				memUnitFactory, errorLogger, civlConfig);
	}
}
