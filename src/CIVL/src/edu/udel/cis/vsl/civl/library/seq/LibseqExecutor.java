package edu.udel.cis.vsl.civl.library.seq;

import java.util.LinkedList;
import java.util.List;

import edu.udel.cis.vsl.civl.config.IF.CIVLConfiguration;
import edu.udel.cis.vsl.civl.dynamic.IF.SymbolicUtility;
import edu.udel.cis.vsl.civl.library.common.BaseLibraryExecutor;
import edu.udel.cis.vsl.civl.model.IF.CIVLException.ErrorKind;
import edu.udel.cis.vsl.civl.model.IF.CIVLSource;
import edu.udel.cis.vsl.civl.model.IF.CIVLSyntaxException;
import edu.udel.cis.vsl.civl.model.IF.CIVLUnimplementedFeatureException;
import edu.udel.cis.vsl.civl.model.IF.ModelFactory;
import edu.udel.cis.vsl.civl.model.IF.expression.Expression;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLArrayType;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLType;
import edu.udel.cis.vsl.civl.semantics.IF.Evaluation;
import edu.udel.cis.vsl.civl.semantics.IF.Executor;
import edu.udel.cis.vsl.civl.semantics.IF.LibraryEvaluatorLoader;
import edu.udel.cis.vsl.civl.semantics.IF.LibraryExecutor;
import edu.udel.cis.vsl.civl.semantics.IF.LibraryExecutorLoader;
import edu.udel.cis.vsl.civl.semantics.IF.SymbolicAnalyzer;
import edu.udel.cis.vsl.civl.state.IF.State;
import edu.udel.cis.vsl.civl.state.IF.UnsatisfiablePathConditionException;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.IF.number.IntegerNumber;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicArrayType;

public class LibseqExecutor extends BaseLibraryExecutor
		implements
			LibraryExecutor {

	public LibseqExecutor(String name, Executor primaryExecutor,
			ModelFactory modelFactory, SymbolicUtility symbolicUtil,
			SymbolicAnalyzer symbolicAnalyzer, CIVLConfiguration civlConfig,
			LibraryExecutorLoader libExecutorLoader,
			LibraryEvaluatorLoader libEvaluatorLoader) {
		super(name, primaryExecutor, modelFactory, symbolicUtil,
				symbolicAnalyzer, civlConfig, libExecutorLoader,
				libEvaluatorLoader);
	}

	/**
	 * Executes a system function call, updating the left hand side expression
	 * with the returned value if any.
	 * 
	 * @param state
	 *            The current state.
	 * @param pid
	 *            The ID of the process that the function call belongs to.
	 * @param call
	 *            The function call statement to be executed.
	 * @return The new state after executing the function call.
	 * @throws UnsatisfiablePathConditionException
	 */
	protected Evaluation executeValue(State state, int pid, String process,
			CIVLSource source, String functionName, Expression[] arguments,
			SymbolicExpression[] argumentValues)
			throws UnsatisfiablePathConditionException {
		Evaluation callEval = null;

		switch (functionName) {
			case "$seq_init" :
				callEval = executeSeqInit(state, pid, process, arguments,
						argumentValues, source);
				break;
			case "$seq_insert" :
				callEval = executeSeqInsert(state, pid, process, arguments,
						argumentValues, source);
				break;
			case "$seq_length" :
				callEval = executeSeqLength(state, pid, process, arguments,
						argumentValues, source);
				break;
			case "$seq_remove" :
				callEval = executeSeqRemove(state, pid, process, arguments,
						argumentValues, source);
				break;
			default :
				throw new CIVLUnimplementedFeatureException(
						"the function " + name + " of library seq.cvh", source);
		}
		return callEval;
	}

	private Evaluation executeSeqInit(State state, int pid, String process,
			Expression[] arguments, SymbolicExpression[] argumentValues,
			CIVLSource source) throws UnsatisfiablePathConditionException {
		SymbolicExpression arrayPtr = argumentValues[0];
		NumericExpression count = (NumericExpression) argumentValues[1];
		SymbolicExpression elePointer = argumentValues[2];
		CIVLSource arrayPtrSource = arguments[0].getSource();
		CIVLSource elePtrSource = arguments[2].getSource();
		CIVLType objTypePointedByFirstArg = symbolicAnalyzer
				.civlTypeOfObjByPointer(arrayPtrSource, state, arrayPtr);
		CIVLArrayType arrayType;

		if (objTypePointedByFirstArg.isArrayType()
				&& !((CIVLArrayType) objTypePointedByFirstArg).isComplete())
			arrayType = (CIVLArrayType) objTypePointedByFirstArg;
		else
			throw new CIVLSyntaxException("The first argument: " + arguments[0]
					+ " should be a pointer to an imcomplete array"
					+ arguments[0].getSource());
		if (count.isZero()) {
			state = primaryExecutor.assign(source, state, pid, arrayPtr,
					universe.array(
							arrayType.elementType().getDynamicType(universe),
							new LinkedList<SymbolicExpression>()));
			return new Evaluation(state, null);
		}
		if (symbolicUtil.isNullPointer(arrayPtr)
				|| symbolicUtil.isNullPointer(elePointer)) {
			errorLogger.logSimpleError(source, state, process,
					symbolicAnalyzer.stateInformation(state),
					ErrorKind.DEREFERENCE,
					"both the first and the third argument of $seq_init() "
							+ "must be non-null pointers.\n"
							+ "actual value of first argument: "
							+ symbolicAnalyzer.symbolicExpressionToString(
									arrayPtrSource, state, null, arrayPtr)
							+ "\n" + "actual value of third argument: "
							+ symbolicAnalyzer.symbolicExpressionToString(
									elePtrSource, state, null, elePointer));
			throw new UnsatisfiablePathConditionException();
		} else {
			if (!arrayType.isIncompleteArrayType()) {
				String arrayPtrString = symbolicAnalyzer
						.symbolicExpressionToString(arrayPtrSource, state, null,
								arrayPtr);

				this.errorLogger.logSimpleError(source, state, process,
						symbolicAnalyzer.stateInformation(state),
						ErrorKind.SEQUENCE,
						"the first argument of $seq_init() must be "
								+ "a pointer to an incomplete array.\n"
								+ "actual first argument: " + arrayPtrString
								+ "\n" + "actual type of " + arrayPtrString
								+ ": pointer to " + arrayType);
				throw new UnsatisfiablePathConditionException();
			} else {
				CIVLType eleType = symbolicAnalyzer.civlTypeOfObjByPointer(
						elePtrSource, state, elePointer);
				CIVLType arrayEleType = ((CIVLArrayType) arrayType)
						.elementType();

				if (!arrayEleType.isSuperTypeOf(eleType)) {
					this.errorLogger.logSimpleError(elePtrSource, state,
							process, symbolicAnalyzer.stateInformation(state),
							ErrorKind.DEREFERENCE,
							"the element type of the array that the first argument "
									+ "points to of $seq_init() must be a super type"
									+ " or the same type of the object that the third "
									+ "argument points to.\n"
									+ "actual element type of the given array: "
									+ arrayEleType + "\n"
									+ "actual type of object pointed to by the third argument: "
									+ eleType);
					return new Evaluation(state, null);
				} else {
					SymbolicExpression eleValue, arrayValue;
					Evaluation eval = evaluator.dereference(elePtrSource, state,
							process, elePointer, false, true);

					state = eval.state;
					eleValue = eval.value;
					arrayValue = symbolicUtil.newArray(
							state.getPathCondition(universe),
							arrayType.elementType().getDynamicType(universe),
							count, eleValue);
					state = primaryExecutor.assign(source, state, pid, arrayPtr,
							arrayValue);
				}
			}
		}
		return new Evaluation(state, null);
	}

	/**
	 * <p>
	 * Given a pointer an object of type "incomplete-array-of-T", inserts count
	 * elements into the array starting at position index. The subsequence
	 * elements of the array are shifted up, and the final length of the array
	 * will be its original length plus count. The values to be inserted are
	 * taken from the region specified by parameters values.
	 * </p>
	 * 
	 * <p>
	 * Precondition: 0<=index<=length, where length is the length of the array
	 * in the pre-state. If index=length, this appends the elements to the end
	 * of the array. If index=0, this inserts the elements at the beginning of
	 * the array. If count=0, this is a no-op and values will not be evaluated
	 * (hence may be NULL).
	 * </p>
	 * 
	 * <p>
	 * Parameters: array: pointer-to-incomplete-array-of-T index: any integer
	 * type, 0<=index<=length values: pointer-to-T count: any integer type,
	 * count>=0
	 * </p>
	 * 
	 * @param state
	 *            The state where the function is called
	 * @param pid
	 *            The PID of the process that executes this function call
	 * @param process
	 *            The process information for error report
	 * @param arguments
	 *            The arguments of function call
	 * @param argumentValues
	 *            The values of the arguments of the function call
	 * @param source
	 *            The source information of the call statement for error report
	 * @return
	 * @throws UnsatisfiablePathConditionException
	 */
	private Evaluation executeSeqInsert(State state, int pid, String process,
			Expression[] arguments, SymbolicExpression[] argumentValues,
			CIVLSource source) throws UnsatisfiablePathConditionException {
		return executeSeqInsertOrRemove(state, pid, process, arguments,
				argumentValues, source, true);
	}

	/**
	 * 
	 * @param state
	 * @param pid
	 * @param process
	 * @param lhs
	 * @param arguments
	 * @param argumentValues
	 * @param source
	 * @return
	 * @throws UnsatisfiablePathConditionException
	 */
	private Evaluation executeSeqLength(State state, int pid, String process,
			Expression[] arguments, SymbolicExpression[] argumentValues,
			CIVLSource source) throws UnsatisfiablePathConditionException {
		SymbolicExpression seqPtr = argumentValues[0];
		CIVLSource seqSource = arguments[0].getSource();
		SymbolicExpression result = null;

		if (symbolicUtil.isNullPointer(seqPtr)) {
			this.errorLogger.logSimpleError(source, state, process,
					symbolicAnalyzer.stateInformation(state),
					ErrorKind.SEQUENCE,
					"the argument of $seq_length() must be a non-null pointer.\n"
							+ "actual argument: "
							+ symbolicAnalyzer.symbolicExpressionToString(
									seqSource, state, null, seqPtr));
			throw new UnsatisfiablePathConditionException();
		} else {
			Evaluation eval = evaluator.dereference(seqSource, state, process,
					seqPtr, false, true);
			SymbolicExpression seq;

			state = eval.state;
			seq = eval.value;
			if (!(seq.type() instanceof SymbolicArrayType)) {
				this.errorLogger.logSimpleError(source, state, process,
						symbolicAnalyzer.stateInformation(state),
						ErrorKind.SEQUENCE,
						"the argument of $seq_length() must be a sequence of "
								+ "objects of the same type.\n"
								+ "actual argument: "
								+ symbolicAnalyzer.symbolicExpressionToString(
										seqSource, state, null, seq));
				throw new UnsatisfiablePathConditionException();
			} else
				result = universe.length(seq);
		}
		return new Evaluation(state, result);
	}

	private Evaluation executeSeqRemove(State state, int pid, String process,
			Expression[] arguments, SymbolicExpression[] argumentValues,
			CIVLSource source) throws UnsatisfiablePathConditionException {
		return executeSeqInsertOrRemove(state, pid, process, arguments,
				argumentValues, source, false);
	}

	private Evaluation executeSeqInsertOrRemove(State state, int pid,
			String process, Expression[] arguments,
			SymbolicExpression[] argumentValues, CIVLSource source,
			boolean isInsert) throws UnsatisfiablePathConditionException {
		SymbolicExpression arrayPtr = argumentValues[0];
		NumericExpression index = (NumericExpression) argumentValues[1];
		SymbolicExpression valuesPtr = argumentValues[2];
		NumericExpression count = (NumericExpression) argumentValues[3];
		CIVLSource arrayPtrSource = arguments[0].getSource(),
				valuesPtrSource = arguments[2].getSource();
		CIVLType arrayType, arrayEleType, valueType;
		Evaluation eval;
		SymbolicExpression arrayValue;
		int countInt, indexInt, lengthInt;
		String functionName = isInsert ? "$seq_insert()" : "$seq_remove()";
		boolean isOldArrayEmpty = false;
		List<SymbolicExpression> elements = new LinkedList<>();
		boolean removeToNull = false;

		if (symbolicUtil.isNullPointer(arrayPtr)) {
			this.errorLogger.logSimpleError(source, state, process,
					symbolicAnalyzer.stateInformation(state),
					ErrorKind.DEREFERENCE,
					"the first argument of " + functionName
							+ " must be a non-null pointer.\n"
							+ "actual value of first argument: "
							+ symbolicAnalyzer.symbolicExpressionToString(
									arrayPtrSource, state, null, arrayPtr));
			throw new UnsatisfiablePathConditionException();
		}
		if (count.isZero())// no op
			return new Evaluation(state, null);
		if (isInsert && symbolicUtil.isNullPointer(valuesPtr)) {
			this.errorLogger.logSimpleError(source, state, process,
					symbolicAnalyzer.stateInformation(state),
					ErrorKind.DEREFERENCE,
					"the third argument of " + functionName
							+ " must be a non-null pointer when the forth "
							+ "argument is greater than zero.\n"
							+ "actual value of third argument: "
							+ symbolicAnalyzer.symbolicExpressionToString(
									valuesPtrSource, state, null, valuesPtr));
			throw new UnsatisfiablePathConditionException();
		}
		arrayType = symbolicAnalyzer.civlTypeOfObjByPointer(arrayPtrSource,
				state, arrayPtr);
		if (!arrayType.isIncompleteArrayType()) {
			this.errorLogger.logSimpleError(source, state, process,
					symbolicAnalyzer.stateInformation(state),
					ErrorKind.SEQUENCE,
					"the first argument of " + functionName
							+ " must be of a pointer to incomplete array of type T.\n"
							+ "actual type of the first argument: pointer to "
							+ arrayType);
			throw new UnsatisfiablePathConditionException();
		}
		arrayEleType = ((CIVLArrayType) arrayType).elementType();
		if (!symbolicUtil.isNullPointer(valuesPtr)) {
			valueType = symbolicAnalyzer.civlTypeOfObjByPointer(valuesPtrSource,
					state, valuesPtr);

			if (!arrayEleType.isSuperTypeOf(valueType)) {
				this.errorLogger.logSimpleError(source, state, process,
						symbolicAnalyzer.stateInformation(state),
						ErrorKind.SEQUENCE,
						"the first argument of " + functionName
								+ " must be a pointer to incomplete array of type T, and"
								+ " the third argument must be a pointer to type T. \n"
								+ "actual type of the first argument: pointer to "
								+ arrayEleType + "\n"
								+ "actual type of the third argument: pointer to "
								+ valueType);
				throw new UnsatisfiablePathConditionException();
			}
		}
		eval = evaluator.dereference(arrayPtrSource, state, process, arrayPtr,
				false, true);
		state = eval.state;
		arrayValue = eval.value;
		if (arrayValue.operator() != SymbolicOperator.ARRAY) {
			this.errorLogger.logSimpleError(source, state, process,
					symbolicAnalyzer.stateInformation(state),
					ErrorKind.SEQUENCE,
					"the first argument of " + functionName
							+ " must be a pointer to a concrete array.\n"
							+ "actual value of the array pointed to by the first argument: "
							+ symbolicAnalyzer.symbolicExpressionToString(
									arrayPtrSource, state, null, arrayValue));
			throw new UnsatisfiablePathConditionException();
		}
		countInt = ((IntegerNumber) universe.extractNumber(count)).intValue();
		indexInt = ((IntegerNumber) universe.extractNumber(index)).intValue();
		lengthInt = ((IntegerNumber) universe
				.extractNumber(universe.length(arrayValue))).intValue();
		isOldArrayEmpty = indexInt == 0 && lengthInt == 0;
		if (isInsert && !isOldArrayEmpty
				&& (indexInt < 0 || indexInt > lengthInt)) {
			this.errorLogger.logSimpleError(source, state, process,
					symbolicAnalyzer.stateInformation(state),
					ErrorKind.SEQUENCE,
					"the index for $seq_insert() is out of the range of the array index.\n"
							+ "array length: " + lengthInt + "\n" + "index: "
							+ indexInt);
			throw new UnsatisfiablePathConditionException();
		} else if (!isInsert && (countInt > lengthInt - indexInt)) {
			this.errorLogger.logSimpleError(source, state, process,
					symbolicAnalyzer.stateInformation(state),
					ErrorKind.SEQUENCE,
					"insufficient data to be removed for $seq_remove().\n"
							+ "array length: " + lengthInt + "\n"
							+ "start index: " + indexInt + "\n"
							+ "number of elements to be removed: " + countInt);
			throw new UnsatisfiablePathConditionException();
		}
		removeToNull = !isInsert && symbolicUtil.isNullPointer(valuesPtr);
		for (int i = 0; i < countInt; i++) {
			SymbolicExpression value, valuePtr;

			if (i == 0)
				valuePtr = valuesPtr;
			else if (!removeToNull) {
				eval = evaluator.arrayElementReferenceAdd(state, pid, valuesPtr,
						universe.integer(i), source).left;
				state = eval.state;
				valuePtr = eval.value;
			} else
				valuePtr = valuesPtr;
			if (isInsert) {
				eval = evaluator.dereference(source, state, process, valuePtr,
						false, true);
				state = eval.state;
				value = eval.value;

				if (isOldArrayEmpty) {
					elements.add(value);
				} else {
					arrayValue = universe.insertElementAt(arrayValue,
							indexInt + i, value);
				}
			} else {
				value = universe.arrayRead(arrayValue, index);
				if (!symbolicUtil.isNullPointer(valuePtr)) {
					state = primaryExecutor.assign(valuesPtrSource, state, pid,
							valuePtr, value);
				}
				arrayValue = universe.removeElementAt(arrayValue, indexInt);
			}
		}
		if (isOldArrayEmpty)
			arrayValue = universe.array(
					((SymbolicArrayType) arrayValue.type()).elementType(),
					elements);
		state = primaryExecutor.assign(source, state, pid, arrayPtr,
				arrayValue);
		return new Evaluation(state, null);
	}
}
