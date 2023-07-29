/**
 * 
 */
package edu.udel.cis.vsl.civl.library.string;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import edu.udel.cis.vsl.civl.config.IF.CIVLConfiguration;
import edu.udel.cis.vsl.civl.dynamic.IF.SymbolicUtility;
import edu.udel.cis.vsl.civl.library.common.BaseLibraryExecutor;
import edu.udel.cis.vsl.civl.model.IF.CIVLException.ErrorKind;
import edu.udel.cis.vsl.civl.model.IF.CIVLInternalException;
import edu.udel.cis.vsl.civl.model.IF.CIVLSource;
import edu.udel.cis.vsl.civl.model.IF.CIVLUnimplementedFeatureException;
import edu.udel.cis.vsl.civl.model.IF.ModelFactory;
import edu.udel.cis.vsl.civl.model.IF.expression.Expression;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLType;
import edu.udel.cis.vsl.civl.semantics.IF.Evaluation;
import edu.udel.cis.vsl.civl.semantics.IF.Executor;
import edu.udel.cis.vsl.civl.semantics.IF.LibraryEvaluatorLoader;
import edu.udel.cis.vsl.civl.semantics.IF.LibraryExecutor;
import edu.udel.cis.vsl.civl.semantics.IF.LibraryExecutorLoader;
import edu.udel.cis.vsl.civl.semantics.IF.SymbolicAnalyzer;
import edu.udel.cis.vsl.civl.state.IF.State;
import edu.udel.cis.vsl.civl.state.IF.UnsatisfiablePathConditionException;
import edu.udel.cis.vsl.civl.util.IF.Pair;
import edu.udel.cis.vsl.civl.util.IF.Triple;
import edu.udel.cis.vsl.sarl.IF.Reasoner;
import edu.udel.cis.vsl.sarl.IF.ValidityResult.ResultType;
import edu.udel.cis.vsl.sarl.IF.expr.ArrayElementReference;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericSymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.ReferenceExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.IF.number.IntegerNumber;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicArrayType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicFunctionType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;

/**
 * Executor for stdlib function calls.
 * 
 * @author Manchun Zheng (zxxxx)
 * @author zirkel
 * @author Wenhao Wu (xxxx@udel.edu)
 * 
 */
public class LibstringExecutor extends BaseLibraryExecutor
		implements
			LibraryExecutor {

	/**
	 * The name of CIVL uninterpreted function:<br>
	 * char[] CIVL_dycast2ArrChar(DYNAMIC_TYPE arg0); <br>
	 * This function will convert the given <code>arg0</code> from its original
	 * type to the type of array-of-char. The function definition is dynamically
	 * generated according to the original type of <code>arg0</code>.
	 */
	static final private String CIVL_DYCAST_ARRCHAR = "CIVL_dycast2ArrChar";

	/**
	 * The name of CIVL uninterpreted function:<br>
	 * int CIVL_strlen(char[] arg0, refType arg1); <br>
	 * This function is used for handling the symbolic char array and returns a
	 * symbolic non-nagative value representing the length of the char array,
	 * which is the actual argument of the function 'strlen'.
	 */
	static final private String CIVL_STRLEN = "CIVL_strlen";

	/* **************************** Constructors *************************** */

	/**
	 * Create a new instance of library executor for "stdlib.h".
	 * 
	 * @param primaryExecutor
	 *            The main executor of the system.
	 * @param output
	 *            The output stream for printing.
	 * @param enablePrintf
	 *            True iff print is enabled, reflecting command line options.
	 */
	public LibstringExecutor(String name, Executor primaryExecutor,
			ModelFactory modelFactory, SymbolicUtility symbolicUtil,
			SymbolicAnalyzer symbolicAnalyzer, CIVLConfiguration civlConfig,
			LibraryExecutorLoader libExecutorLoader,
			LibraryEvaluatorLoader libEvaluatorLoader) {
		super(name, primaryExecutor, modelFactory, symbolicUtil,
				symbolicAnalyzer, civlConfig, libExecutorLoader,
				libEvaluatorLoader);
	}

	/*
	 * ******************** Methods from BaseLibraryExecutor *******************
	 */

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
	@Override
	protected Evaluation executeValue(State state, int pid, String process,
			CIVLSource source, String functionName, Expression[] arguments,
			SymbolicExpression[] argumentValues)
			throws UnsatisfiablePathConditionException {
		Evaluation callEval = null;

		switch (functionName) {
			case "strcpy" :
				callEval = execute_strcpy(state, pid, process, arguments,
						argumentValues, source);
				break;
			case "strlen" :
				callEval = execute_strlen(state, pid, process, arguments,
						argumentValues, source);
				break;
			case "strcmp" :
				callEval = execute_strcmp(state, pid, process, arguments,
						argumentValues, source);
				break;
			case "memset" :
				callEval = execute_memset(state, pid, process, arguments,
						argumentValues, source);
				break;
			default :
				throw new CIVLInternalException(
						"Unknown string function: " + functionName, source);
		}
		return callEval;
	}

	/* *************************** Private Methods ************************* */

	// TODO: this function assume the "lhsPointer" which is argument[0] is a
	// pointer to heap object element which needs being improved.
	private Evaluation execute_strcpy(State state, int pid, String process,
			Expression[] arguments, SymbolicExpression[] argumentValues,
			CIVLSource source) throws UnsatisfiablePathConditionException {
		Evaluation eval;
		SymbolicExpression charPointer = argumentValues[1];
		int startIndex;
		int lStartIndex;
		SymbolicExpression lhsPointer = argumentValues[0];
		SymbolicExpression originalArray = null;
		int numChars;
		int vid = symbolicUtil.getVariableId(source, lhsPointer);
		int scopeId = stateFactory
				.getDyscopeId(symbolicUtil.getScopeValue(lhsPointer));
		ReferenceExpression symRef = ((ArrayElementReference) symbolicUtil
				.getSymRef(lhsPointer)).getParent();

		if (charPointer.operator() == SymbolicOperator.TUPLE)
			startIndex = symbolicUtil.getArrayIndex(source, charPointer);
		else
			throw new CIVLUnimplementedFeatureException(
					"Do strcpy() on a non-concrete string", source);
		if (lhsPointer.operator() == SymbolicOperator.TUPLE)
			lStartIndex = symbolicUtil.getArrayIndex(source, lhsPointer);
		else
			throw new CIVLUnimplementedFeatureException(
					"Assign to a non-concrete string", source);
		if (charPointer.type() instanceof SymbolicArrayType) {
			originalArray = charPointer;
		} else {
			SymbolicExpression arrayPointer = symbolicUtil
					.parentPointer(charPointer);
			ArrayElementReference arrayRef = (ArrayElementReference) symbolicUtil
					.getSymRef(charPointer);
			NumericExpression arrayIndex = arrayRef.getIndex();

			eval = evaluator.dereference(source, state, process, arrayPointer,
					false, true);
			state = eval.state;
			// TODO: implement getStringConcrete() as an underneath
			// implementation of getString()
			originalArray = eval.value;
			startIndex = symbolicUtil.extractInt(source, arrayIndex);
		}
		numChars = originalArray.numArguments();
		assert originalArray.operator() == SymbolicOperator.ARRAY;
		for (int i = 0; i < numChars; i++) {
			SymbolicExpression charExpr = (SymbolicExpression) originalArray
					.argument(i + startIndex);
			Character theChar = universe.extractCharacter(charExpr);
			ReferenceExpression eleRef = universe.arrayElementReference(symRef,
					universe.integer(i + lStartIndex));
			SymbolicExpression pointer = symbolicUtil.makePointer(scopeId, vid,
					eleRef);

			state = primaryExecutor.assign(source, state, pid, pointer,
					charExpr);
			if (theChar == '\0')
				break;
		}
		return new Evaluation(state, argumentValues[0]);
	}

	/**
	 * <code>int strcmp(const char * s1, const char * s2)</code> Compare two
	 * strings s1 and s2. Return 0 if s1 = s2. If s1 > s2, return a value
	 * greater than 0 and if s1 < s2, return a value less than zero. <br>
	 * Comparison:<br>
	 * The comparison is determined by the sign of the difference between the
	 * values of the first pair of characters (both interpreted as unsigned
	 * char) that differ in the objects being compared.
	 * 
	 * @param state
	 *            the current state
	 * @param pid
	 *            the PID of the process
	 * @param process
	 *            the information of the process
	 * @param lhs
	 *            the expression of the left-hand side variable
	 * @param arguments
	 *            the expressions of arguments
	 * @param argumentValues
	 *            the symbolic expressions of arguments
	 * @param source
	 *            the CIVL source of the statement
	 * @return
	 * @throws UnsatisfiablePathConditionException
	 */
	private Evaluation execute_strcmp(State state, int pid, String process,
			Expression[] arguments, SymbolicExpression[] argumentValues,
			CIVLSource source) throws UnsatisfiablePathConditionException {
		int output = 0;
		NumericExpression result;
		SymbolicExpression charPointer1 = argumentValues[0];
		SymbolicExpression charPointer2 = argumentValues[1];
		Triple<State, StringBuffer, Boolean> strEval1 = null;
		Triple<State, StringBuffer, Boolean> strEval2 = null;
		StringBuffer str1, str2;

		if ((charPointer1.operator() != SymbolicOperator.TUPLE)) {
			errorLogger.logSimpleError(source, state, process,
					symbolicAnalyzer.stateInformation(state), ErrorKind.POINTER,
					"attempt to read/write from an invalid pointer");
			throw new UnsatisfiablePathConditionException();
		}
		if ((charPointer2.operator() != SymbolicOperator.TUPLE)) {
			errorLogger.logSimpleError(source, state, process,
					symbolicAnalyzer.stateInformation(state), ErrorKind.POINTER,
					"attempt to read/write from an invalid pointer");
			throw new UnsatisfiablePathConditionException();
		}
		// If two pointers are same, return 0.
		if (charPointer1.equals(charPointer2))
			result = zero;
		else {
			strEval1 = evaluator.getString(source, state, process, arguments[0],
					charPointer1);
			state = strEval1.first;
			strEval2 = evaluator.getString(source, state, process, arguments[1],
					charPointer2);
			state = strEval2.first;
			if (!strEval1.third || !strEval2.third) {
				// catch (CIVLUnimplementedFeatureException e) {
				// If the string pointed by either pointer1 or pointer2 is
				// non-concrete, try to compare two string objects, if
				// different, return abstract function.
				SymbolicExpression symResult;
				SymbolicExpression strObj1, strObj2;
				Evaluation eval;

				eval = evaluator.dereference(arguments[0].getSource(), state,
						process, charPointer1, true, true);
				state = eval.state;
				strObj1 = eval.value;
				eval = evaluator.dereference(arguments[1].getSource(), state,
						process, charPointer2, true, true);
				state = eval.state;
				strObj2 = eval.value;
				if (strObj1.equals(strObj2))
					symResult = zero;
				else {
					SymbolicFunctionType funcType;
					SymbolicExpression func;

					funcType = universe.functionType(
							Arrays.asList(charPointer1.type(),
									charPointer2.type()),
							universe.integerType());
					func = universe.symbolicConstant(
							universe.stringObject("strcmp"), funcType);
					symResult = universe.apply(func,
							Arrays.asList(charPointer1, charPointer2));
				}
				return new Evaluation(state, symResult);
			} else {
				assert (strEval1.second != null
						&& strEval2.second != null) : "Evaluating String failed";
				str1 = strEval1.second;
				str2 = strEval2.second;
				output = str1.toString().compareTo(str2.toString());
				result = universe.integer(output);
			}
		}
		return new Evaluation(state, result);
	}

	/**
	 * <code>int strlen(const char * s)</code> Returns the length of the string
	 * pointed by s. </br>
	 * If the given pointer s pointing to a symbolic value , then it will return
	 * an application of uninterpreted function, and the returned value of that
	 * function should be bounded. (e.g., For '$input char IN[10]', it will
	 * return strlen(&IN[0]), which is in range [0, 10].) <br>
	 * 
	 * @param state
	 *            the current state
	 * @param pid
	 *            the PID of the process
	 * @param process
	 *            the information of the process
	 * @param arguments
	 *            the expressions of arguments
	 * @param argumentValues
	 *            the symbolic expressions of arguments
	 * @param source
	 *            the CIVL source of the statement
	 * @return
	 * @throws UnsatisfiablePathConditionException
	 */
	private Evaluation execute_strlen(State state, int pid, String process,
			Expression[] arguments, SymbolicExpression[] argumentValues,
			CIVLSource source) throws UnsatisfiablePathConditionException {
		Evaluation eval;
		Evaluation result = null;
		SymbolicExpression charPointer = argumentValues[0];
		int startIndex;
		SymbolicExpression originalArray = null;
		int numChars;
		int length = 0;

		if (charPointer.operator() == SymbolicOperator.TUPLE)
			startIndex = symbolicUtil.getArrayIndex(source, charPointer);
		else
			throw new CIVLUnimplementedFeatureException(
					"Do strlen() with a non-concrete index", source);
		if (charPointer.type() instanceof SymbolicArrayType) {
			originalArray = charPointer;
		} else {
			SymbolicExpression arrayPointer = symbolicUtil
					.parentPointer(charPointer);
			ArrayElementReference arrayRef = (ArrayElementReference) symbolicUtil
					.getSymRef(charPointer);
			NumericExpression arrayIndex = arrayRef.getIndex();

			eval = evaluator.dereference(source, state, process, arrayPointer,
					false, true);
			eval = evaluator.dereference(source, state, process, arrayPointer,
					false, true);
			state = eval.state;
			originalArray = eval.value;
			startIndex = symbolicUtil.extractInt(source, arrayIndex);
		}
		numChars = originalArray.numArguments();
		if (originalArray.operator() == SymbolicOperator.ARRAY) {
			for (int i = 0; i < numChars - startIndex; i++) {
				SymbolicExpression charExpr = (SymbolicExpression) originalArray
						.argument(i + startIndex);
				Character theChar = universe.extractCharacter(charExpr);

				if (theChar == '\0')
					break;
				length++;
			}
			result = new Evaluation(state, universe.integer(length));
		} else {
			// If the given char-array is symbolic (e.g. $input char s[10])
			// Abstract function: int strlen(char* arg0, refType arg1)
			// Construct arg0
			// 1. get the variable referenced by the actual argument of strlen
			SymbolicExpression rootPointer = symbolicUtil
					.makePointer(charPointer, universe.identityReference());
			Evaluation evalRootPointer = evaluator.dereference(
					arguments[0].getSource(), state, process, rootPointer, true,
					true);
			SymbolicExpression rawRootVar = evalRootPointer.value;
			// 2. Cast the root variable to the type of array-of-char
			SymbolicType rawRootVarType = rawRootVar.type();
			SymbolicType ArrayOfCharType = universe
					.arrayType(universe.characterType());
			SymbolicFunctionType typeCastFuncType = universe.functionType(
					Arrays.asList(rawRootVarType), ArrayOfCharType);
			SymbolicExpression typeCastFunc = universe.symbolicConstant(
					universe.stringObject(CIVL_DYCAST_ARRCHAR),
					typeCastFuncType);
			SymbolicExpression castedRootVar = universe.apply(typeCastFunc,
					Arrays.asList(rawRootVar));
			// Construct arg1
			SymbolicExpression refExpr = symbolicUtil.getSymRef(charPointer);
			// Construct CIVL_strlen abstract function for symbolic string
			SymbolicFunctionType funcType = universe.functionType(
					Arrays.asList(ArrayOfCharType, universe.referenceType()),
					universe.integerType());
			SymbolicExpression func = universe.symbolicConstant(
					universe.stringObject(CIVL_STRLEN), funcType);
			SymbolicExpression symResult = universe.apply(func,
					Arrays.asList(castedRootVar, refExpr));
			// Add PC: 0 <= CIVL_strlen(arg0, arg1)
			BooleanExpression pc = universe.lessThanEquals(zero,
					(NumericExpression) symResult);

			state = stateFactory.addToPathcondition(evalRootPointer.state, pid,
					pc);
			result = new Evaluation(state, symResult);
		}
		assert (result != null);
		return result;
	}

	/**
	 * Execute memset() function. CIVL currently only support set memory units
	 * to zero (In other words, the second argument of the memset function can
	 * only be zero).
	 * 
	 * @param state
	 *            the current state
	 * @param pid
	 *            the PID of the process
	 * @param process
	 *            the identifier of the process
	 * @param lhs
	 *            the left hand size expression
	 * @param arguments
	 *            the expressions of arguments
	 * @param argumentValues
	 *            the symbolic expressions of arguments
	 * @param source
	 * @return
	 * @throws UnsatisfiablePathConditionException
	 * @author xxxx luo
	 */
	private Evaluation execute_memset(State state, int pid, String process,
			Expression[] arguments, SymbolicExpression[] argumentValues,
			CIVLSource source) throws UnsatisfiablePathConditionException {
		SymbolicExpression pointer, c;
		NumericExpression size, length, dataTypeSize;
		SymbolicExpression zerosArray;
		SymbolicExpression zeroVar;
		CIVLType objectElementTypeStatic;
		SymbolicType objectElementType;
		BooleanExpression claim;
		Reasoner reasoner = universe.reasoner(state.getPathCondition(universe));
		ResultType resultType;
		Evaluation eval;
		Pair<Evaluation, NumericExpression[]> ptrAddRet;
		Pair<Evaluation, SymbolicExpression> setDataRet;
		edu.udel.cis.vsl.sarl.IF.number.Number num_length;
		int int_length;
		boolean byteIsUnit = true;

		pointer = argumentValues[0];
		c = argumentValues[1];
		// check if pointer is valid first
		if (pointer.operator() != SymbolicOperator.TUPLE) {
			errorLogger.logSimpleError(source, state, process,
					symbolicAnalyzer.stateInformation(state), ErrorKind.POINTER,
					"attempt to read/write from an invalid pointer");
			throw new UnsatisfiablePathConditionException();
		}
		// check if c == 0, because that's the only case we support
		claim = universe.equals(c, zero);
		resultType = reasoner.valid(claim).getResultType();
		if (resultType != ResultType.YES) {
			throw new CIVLUnimplementedFeatureException(
					"memset() is not support copy a non-zero: " + c
							+ " value to the given address");
		}
		size = (NumericExpression) argumentValues[2];
		objectElementTypeStatic = symbolicAnalyzer.getArrayBaseType(state,
				arguments[0].getSource(), pointer);
		objectElementType = objectElementTypeStatic.getDynamicType(universe);
		dataTypeSize = typeFactory.sizeofDynamicType(objectElementType);

		// TODO: what is the point of looping over all arguments of size?
		// Shouldn't this only look for the case of one argument?

		int numArgs = size.numArguments();

		for (int i = 0; i < numArgs; i++) {
			SymbolicObject obj = size.argument(i);

			if (obj instanceof NumericSymbolicConstant) {
				/*
				 * size contains any "SIZEOF(CHAR) or SIZEOF(BOOLEAN)", never
				 * simplify SIZEOF(CHAR)(or SIZEOF(BOOLEAN) to one
				 */
				if (obj.equals(
						typeFactory.sizeofDynamicType(universe.characterType()))
						|| obj.equals(typeFactory
								.sizeofDynamicType(universe.booleanType())))
					byteIsUnit = false;
			}
		}
		switch (objectElementType.typeKind()) {
			case REAL :
				zeroVar = universe.rational(0);
				break;
			case INTEGER :
				zeroVar = universe.integer(0);
				break;
			case CHAR :
				zeroVar = universe.character('\0');
				if (byteIsUnit)
					dataTypeSize = one;
				break;
			case BOOLEAN :
				zeroVar = universe.bool(false);
				if (byteIsUnit)
					dataTypeSize = one;
				break;
			default :
				if (objectElementType == this.modelFactory.typeFactory()
						.pointerSymbolicType()) {
					zeroVar = this.symbolicUtil.nullPointer();
					if (byteIsUnit)
						dataTypeSize = one;
					break;
				}
				throw new CIVLUnimplementedFeatureException(
						"Any datatype other than REAL, INTEGER, CHAR and BOOLEAN is not supported yet");
		}
		length = universe.divide(size, dataTypeSize);
		ptrAddRet = evaluator.arrayElementReferenceAdd(state, pid, pointer,
				length, arguments[0].getSource());
		eval = ptrAddRet.left;
		state = eval.state;
		// create a set of zeros to set to the pointed object.
		num_length = universe.extractNumber(length);
		if (num_length != null) {
			List<SymbolicExpression> zeros = new LinkedList<>();

			int_length = ((IntegerNumber) num_length).intValue();
			for (int i = 0; i < int_length; i++)
				zeros.add(zeroVar);
			zerosArray = universe.array(objectElementType, zeros);
		} else {
			zerosArray = symbolicUtil.newArray(state.getPathCondition(universe),
					objectElementType, length, zeroVar);
		}
		// Calling setDataFrom to set the pointed object to zero
		setDataRet = setDataFrom(state, pid, process, arguments[0], pointer,
				length, zerosArray, false, source);
		eval = setDataRet.left;
		state = eval.state;
		state = this.primaryExecutor.assign(source, state, pid,
				setDataRet.right, eval.value);
		return new Evaluation(state, pointer);
	}
}
