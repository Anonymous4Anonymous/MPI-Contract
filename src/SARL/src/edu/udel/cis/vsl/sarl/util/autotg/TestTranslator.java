package edu.udel.cis.vsl.sarl.util.autotg;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.udel.cis.vsl.sarl.IF.SARLException;
import edu.udel.cis.vsl.sarl.IF.ValidityResult.ResultType;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.IF.object.IntObject;
import edu.udel.cis.vsl.sarl.IF.object.NumberObject;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject.SymbolicObjectKind;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicArrayType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicCompleteArrayType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicFunctionType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicTupleType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType.SymbolicTypeKind;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicTypeSequence;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicUninterpretedType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicUnionType;
import edu.udel.cis.vsl.sarl.object.common.SimpleSequence;

public class TestTranslator {
	/**
	 * All text contents of Junit test methods:
	 */
	private List<String> tests;

	/**
	 * Number of tests in the generated test class:
	 */
	private int numTests;

	public TestTranslator() {
		this.tests = new LinkedList<>();
		this.numTests = 0;
	}

	public void generateValidCheckMethod(SymbolicExpression context,
			SymbolicExpression predicate, ResultType resultType,
			boolean useWhy3, String testName, String... comments) {
		TestTranslationState state = new TestTranslationState();
		if (useWhy3)
			checkValidWhy3((BooleanExpression) context,
					(BooleanExpression) predicate, state, resultType);
		else
			checkValid((BooleanExpression) context,
					(BooleanExpression) predicate, state, resultType);
		tests.addAll(state.printer.genUnitTest(numTests++, testName, comments));
	}

	public void generateTestClass(String className) {
		TestPrinter2.genTestClass(className, tests);
	}

	private String translate(SymbolicExpression expression,
			TestTranslationState state) {
		SymbolicOperator op = expression.operator();

		if (expression.type() != null)
			translateType(expression.type(), state);
		switch (op) {
		case ADD:
		case AND:
		case OR:
		case DIVIDE:
		case INT_DIVIDE:
		case MODULO:
		case MULTIPLY:
		case SUBTRACT:
		case EQUALS:
		case LESS_THAN:
		case LESS_THAN_EQUALS:
		case NEGATIVE:
		case NEQ:
		case NOT:
		case ARRAY_READ:
		case ARRAY_WRITE:
		case LENGTH:
		case TUPLE:
			return translateSimpleOperation(expression, op, state);
		case CONCRETE:
			return translateConcrete(expression);
		case SYMBOLIC_CONSTANT:
			return translateSYMBOLIC_CONSTANT((SymbolicConstant) expression,
					state);
		case APPLY:
			return translateAPPLY(expression, state);
		case ARRAY:
			return translateARRAY(expression, state);
		case ARRAY_LAMBDA:
			return translateARRAY_LAMBDA(expression, state);
		case CAST:
			return translateCAST(expression, state);
		case COND:
			return translateCOND(expression, state);
		case DENSE_ARRAY_WRITE:
			return translateDENSE_ARRAY_WRITE(expression, state);
		case EXISTS:
		case FORALL:
		case LAMBDA:
			return translateQuantifiedOrLambda(expression, state);
		case NULL:
			return TestPrinter2.UNIVERSE + ".nullExpression()";
		case TUPLE_READ:
			return translateTupleRead(expression, state);
		case TUPLE_WRITE:
			return translateTupleWrite(expression, state);
		case UNION_EXTRACT:
		case UNION_INJECT:
		case UNION_TEST:
			return translateUnionExtractOrInjectOrTest(expression, state);
		case POWER:
			return translatePOWER(expression, state);
		case BIT_AND:
		case BIT_NOT:
		case BIT_OR:
		case BIT_SHIFT_LEFT:
		case BIT_SHIFT_RIGHT:
		case BIT_XOR:
		case DERIV:
		case DIFFERENTIABLE:
		case DENSE_TUPLE_WRITE:
		default:
			throw new SARLException("unsupported operator " + op);
		}
	}

	private String translateSimpleOperation(SymbolicExpression operation,
			SymbolicOperator op, TestTranslationState state) {
		String result = state.cache.get(operation);

		if (result == null) {
			List<String> operandsList = new LinkedList<>();
			String[] operands;
			int numOperands = 0;

			for (SymbolicObject arg : operation.getArguments()) {
				String argText = translate((SymbolicExpression) arg, state);

				operandsList.add(argText);
				numOperands++;
			}
			operands = new String[numOperands];
			operandsList.toArray(operands);
			// declare temporary variable as LHS of this operation:
			String lhsName = state.nextVarName();
			SymbolicType lhsType = operation.type();
			String typeText = translateType(lhsType, state);

			state.printer.genVariableDeclaration(lhsType, lhsName, typeText);
			// insert text "lhs = universe.op(operand[0], operand[1], ...)"
			state.printer.genExpression(lhsName, lhsType, op, operands);
			state.cache.put(operation, lhsName);
			result = lhsName;
		}
		return result;
	}

	private String translateSYMBOLIC_CONSTANT(SymbolicConstant var,
			TestTranslationState state) {
		String result = state.varDecls.get(var);

		if (result == null) {
			String varName = symConstName(var);
			SymbolicType type = var.type();
			String typeText = translateType(type, state);

			state.printer.genVariableDeclaration(type, varName, typeText);
			state.varDecls.put(var, varName);
			result = varName;
		}
		return result;
	}

	private String translateAPPLY(SymbolicExpression expr,
			TestTranslationState state) {
		SymbolicExpression function = (SymbolicExpression) expr.argument(0);
		@SuppressWarnings("unchecked")
		SimpleSequence<SymbolicExpression> args = ((SimpleSequence<SymbolicExpression>) expr
				.argument(1));
		String argsText[];
		int counter = 1;

		argsText = new String[args.size() + 1];
		for (SymbolicExpression arg : args)
			argsText[counter++] = translate(arg, state);

		// special handling for $sigma:
		if (function.operator() == SymbolicOperator.SYMBOLIC_CONSTANT)
			if (((SymbolicConstant) function).name().getString()
					.equals("sigma")) {
				return translateWithTemporaryVariableForSigma(state,
						expr.type(),
						Arrays.copyOfRange(argsText, 1, argsText.length));
			}

		argsText[0] = translate(function, state);
		// temporary variable declaration:
		return translateWithTemporaryVariableForExpression(state, expr.type(),
				SymbolicOperator.APPLY, argsText);
	}

	private String translateARRAY(SymbolicExpression expr,
			TestTranslationState state) {
		List<String> elementTexts = new LinkedList<>();
		String elementTextArray[];

		elementTexts.add(translateType(
				((SymbolicArrayType) expr.type()).elementType(), state));
		for (SymbolicObject ele : expr.getArguments())
			elementTexts.add(translate((SymbolicExpression) ele, state));
		elementTextArray = new String[elementTexts.size()];
		elementTexts.toArray(elementTextArray);
		// temporary variable declaration:
		return translateWithTemporaryVariableForExpression(state, expr.type(),
				SymbolicOperator.ARRAY, elementTextArray);
	}

	private String translateARRAY_LAMBDA(SymbolicExpression expr,
			TestTranslationState state) {
		SymbolicExpression lambda = (SymbolicExpression) expr.argument(0);
		String lambdaTxt = translate(lambda, state);
		String arrayTypeTxt = translateType(expr.type(), state);

		return translateWithTemporaryVariableForExpression(state, expr.type(),
				SymbolicOperator.ARRAY_LAMBDA, arrayTypeTxt, lambdaTxt);
	}

	private String translateCAST(SymbolicExpression expr,
			TestTranslationState state) {
		SymbolicType castedType = (SymbolicType) expr.argument(0);
		String exprText, typeText;

		typeText = translateType(castedType, state);
		exprText = translate((SymbolicExpression) expr.argument(1), state);
		return translateWithTemporaryVariableForExpression(state, expr.type(),
				SymbolicOperator.CAST, typeText, exprText);
	}

	private String translateCOND(SymbolicExpression expr,
			TestTranslationState state) {
		BooleanExpression cond = (BooleanExpression) expr.argument(0);
		String argTexts[] = new String[3];

		argTexts[0] = "(BooleanExpression)" + translate(cond, state);
		argTexts[1] = translate((SymbolicExpression) expr.argument(1), state);
		argTexts[2] = translate((SymbolicExpression) expr.argument(2), state);
		return translateWithTemporaryVariableForExpression(state, expr.type(),
				SymbolicOperator.COND, argTexts);
	}

	private String translateDENSE_ARRAY_WRITE(SymbolicExpression expr,
			TestTranslationState state) {
		String argTexts[];
		@SuppressWarnings("unchecked")
		SimpleSequence<SymbolicExpression> args = (SimpleSequence<SymbolicExpression>) expr
				.argument(1);

		argTexts = new String[args.size() + 1];
		argTexts[0] = translate((SymbolicExpression) expr.argument(0), state);
		for (int i = 1; i < argTexts.length; i++)
			argTexts[i] = translate(args.get(i - 1), state);
		return translateWithTemporaryVariableForExpression(state, expr.type(),
				SymbolicOperator.DENSE_ARRAY_WRITE, argTexts);
	}

	private String translateQuantifiedOrLambda(SymbolicExpression expr,
			TestTranslationState state) {
		SymbolicConstant boundVar = (SymbolicConstant) expr.argument(0);
		SymbolicExpression pred = (SymbolicExpression) expr.argument(1);
		String boundVarText, predText;

		boundVarText = "(SymbolicConstant)" + translate(boundVar, state);
		predText = translate(pred, state);
		return this.translateWithTemporaryVariableForExpression(state,
				expr.type(), expr.operator(), boundVarText, predText);
	}

	private String translateTupleRead(SymbolicExpression expr,
			TestTranslationState state) {
		SymbolicExpression tuple = (SymbolicExpression) expr.argument(0);
		IntObject field = (IntObject) expr.argument(1);
		String tupleText = translate(tuple, state);
		String filedText = String.valueOf(field.getInt());

		return this.translateWithTemporaryVariableForExpression(state,
				expr.type(), expr.operator(), tupleText, filedText);
	}

	private String translateTupleWrite(SymbolicExpression expr,
			TestTranslationState state) {
		SymbolicExpression tuple = (SymbolicExpression) expr.argument(0);
		IntObject field = (IntObject) expr.argument(1);
		SymbolicExpression value = (SymbolicExpression) expr.argument(2);
		String tupleText = translate(tuple, state);
		String filedText = String.valueOf(field.getInt());
		String valueText = translate(value, state);

		return this.translateWithTemporaryVariableForExpression(state,
				expr.type(), expr.operator(), tupleText, filedText, valueText);
	}

	private String translateUnionExtractOrInjectOrTest(SymbolicExpression expr,
			TestTranslationState state) {
		SymbolicExpression union = (SymbolicExpression) expr.argument(0);
		IntObject field = (IntObject) expr.argument(1);
		String unionText = translate(union, state);
		String filedText = String.valueOf(field.getInt());

		return this.translateWithTemporaryVariableForExpression(state,
				expr.type(), expr.operator(), filedText, unionText);
	}

	private String translateConcrete(SymbolicExpression concrete) {
		if (concrete.type().isNumeric())
			return number("\"" + concrete.toString() + "\"",
					concrete.type().isReal());
		if (concrete.type().isBoolean())
			if (concrete.isTrue())
				return TestPrinter2.UNIVERSE + ".trueExpression()";
			else
				return TestPrinter2.UNIVERSE + ".falseExpression()";
		throw new SARLException("unsupported conrete expression : " + concrete);
	}

	private String translatePOWER(SymbolicExpression power,
			TestTranslationState state) {
		if (power.argument(1)
				.symbolicObjectKind() == SymbolicObjectKind.EXPRESSION)
			return this.translateSimpleOperation(power, SymbolicOperator.POWER,
					state);

		NumericExpression base = (NumericExpression) power.argument(0);
		NumberObject exp = (NumberObject) power.argument(1);
		String baseTxt = translate(base, state);
		String expTxt = number("\"" + exp.toString() + "\"", false);

		return translateWithTemporaryVariableForExpression(state, power.type(),
				SymbolicOperator.POWER, baseTxt, expTxt);
	}

	private String number(String number, boolean isReal) {
		String numberFactory = TestPrinter.UNIVERSE + ".numberFactory()";
		String nfNumberOrRation = isReal ? "rational" : "number";

		return TestPrinter.UNIVERSE + ".number(" + numberFactory + "."
				+ nfNumberOrRation + "(" + number + "))";
	}

	private String translateType(SymbolicType type,
			TestTranslationState state) {
		String result = state.typeTexts.get(type);

		if (result == null) {
			switch (type.typeKind()) {
			case INTEGER:
				result = TestPrinter.UNIVERSE + ".integerType()";
				break;
			case REAL:
				result = TestPrinter.UNIVERSE + ".realType()";
				break;
			case BOOLEAN:
				result = TestPrinter.UNIVERSE + ".booleanType()";
				break;
			case CHAR:
				result = TestPrinter.UNIVERSE + ".characterType()";
				break;
			case ARRAY:
				result = translateArrayType((SymbolicArrayType) type, state);
				break;
			case FUNCTION:
				result = translateFunctionType((SymbolicFunctionType) type,
						state);
				break;
			case TUPLE:
			case UNION:
				result = translateTupleOrUnionType(type, state);
				break;
			case UNINTERPRETED:
				result = translateUninterpretedType(
						(SymbolicUninterpretedType) type, state);
				break;
			case MAP:
			case SET:
			default:
				throw new SARLException("unsupported type :" + type);
			}
			state.typeTexts.put(type, result);
		}
		return result;
	}

	private String translateArrayType(SymbolicArrayType arrayType,
			TestTranslationState state) {
		List<String> argTextList = new LinkedList<>();

		argTextList.add(translateType(arrayType.elementType(), state));
		if (arrayType.isComplete())
			argTextList.add(translate(
					((SymbolicCompleteArrayType) arrayType).extent(), state));

		String[] argsArray = new String[argTextList.size()];

		argTextList.toArray(argsArray);
		return translateWithTemporaryVariableForType(state, arrayType,
				argsArray);
	}

	private String translateFunctionType(SymbolicFunctionType functionType,
			TestTranslationState state) {
		List<String> argTextList = new LinkedList<>();

		for (SymbolicType inputType : functionType.inputTypes())
			argTextList.add(translateType(inputType, state));
		argTextList.add(translateType(functionType.outputType(), state));

		String[] argsArray = new String[argTextList.size()];

		argTextList.toArray(argsArray);
		return translateWithTemporaryVariableForType(state, functionType,
				argsArray);
	}

	private String translateTupleOrUnionType(SymbolicType tupleOrUnionType,
			TestTranslationState state) {
		List<String> argTextList = new LinkedList<>();
		SymbolicTypeSequence sequence = null;

		if (tupleOrUnionType.typeKind() == SymbolicTypeKind.TUPLE) {
			argTextList.add(
					((SymbolicTupleType) tupleOrUnionType).name().getString());
			sequence = ((SymbolicTupleType) tupleOrUnionType).sequence();
		} else {
			argTextList.add(
					((SymbolicUnionType) tupleOrUnionType).name().getString());
			sequence = ((SymbolicUnionType) tupleOrUnionType).sequence();
		}
		for (SymbolicType fieldType : sequence)
			argTextList.add(translateType(fieldType, state));

		String[] argsArray = new String[argTextList.size()];

		argTextList.toArray(argsArray);
		return translateWithTemporaryVariableForType(state, tupleOrUnionType,
				argsArray);
	}

	private String translateUninterpretedType(
			SymbolicUninterpretedType uninterpretedType,
			TestTranslationState state) {
		String name = uninterpretedType.name().getString();

		return translateWithTemporaryVariableForType(state, uninterpretedType,
				name);
	}

	private String symConstName(SymbolicConstant symConst) {
		return "var_" + symConst.name().getString();
	}

	private String translateWithTemporaryVariableForExpression(
			TestTranslationState state, SymbolicType tempVarType,
			SymbolicOperator op, String... args) {
		// temporary variable declaration:
		String tempVar = state.nextVarName();
		String typeText = translateType(tempVarType, state);

		state.printer.genVariableDeclaration(tempVarType, tempVar, typeText);
		state.printer.genExpression(tempVar, tempVarType, op, args);
		return tempVar;
	}

	private String translateWithTemporaryVariableForSigma(
			TestTranslationState state, SymbolicType tempVarType,
			String... args) {
		// temporary variable declaration:
		String tempVar = state.nextVarName();
		String typeText = translateType(tempVarType, state);

		state.printer.genVariableDeclaration(tempVarType, tempVar, typeText);
		state.printer.genSigma(tempVar, tempVarType, args);
		return tempVar;
	}

	private String translateWithTemporaryVariableForType(
			TestTranslationState state, SymbolicType type, String... args) {
		// temporary variable declaration:
		String typeName = state.nextTypeName();

		state.printer.genTypeDeclatation(typeName, type, args);
		return typeName;
	}

	private String checkValid(BooleanExpression context,
			BooleanExpression predicate, TestTranslationState state,
			ResultType expected) {
		String ctxText = translate(context, state);
		String predText = translate(predicate, state);
		String rtname = state.nextRTName();
		String expectedText = "ResultType." + expected;

		state.printer.genResultTypeDeclatation(rtname);
		state.printer.callValid(rtname, ctxText, predText);
		state.printer.assertEquals(expectedText, rtname);
		return rtname;
	}

	private String checkValidWhy3(BooleanExpression context,
			BooleanExpression predicate, TestTranslationState state,
			ResultType expected) {
		String ctxText = translate(context, state);
		String predText = translate(predicate, state);
		String rtname = state.nextRTName();
		String expectedText = "ResultType." + expected;

		state.printer.genResultTypeDeclatation(rtname);
		state.printer.callValidWhy3(rtname, ctxText, predText);
		state.printer.assertEquals(expectedText, rtname);
		return rtname;
	}

	/**
	 * Stateful objects for generating one Junit test:
	 * 
	 * @author xxxx
	 *
	 */
	static private class TestTranslationState {
		/**
		 * A map from {@link SymbolicType}s to their declaration texts
		 */
		Map<SymbolicType, String> typeTexts;

		/**
		 * Naming counter for variables holding unique {@link SymbolicType}s
		 */
		int typeNameCounter;

		/**
		 * Naming counter for variables of {@link ResultType} java type
		 */
		int rtNameCounter;

		/**
		 * A map from {@link SymbolicConstant}s to their delcaration texts
		 */
		Map<SymbolicConstant, String> varDecls;

		/**
		 * Naming counter for variables taking unique {@link SymbolicType}s
		 */
		int varNameCounter;

		/**
		 * Cache of translate expressions:
		 */
		Map<SymbolicExpression, String> cache;

		/**
		 * A instance of the {@link TestPrinter}:
		 */
		TestPrinter2 printer;

		TestTranslationState() {
			this.cache = new HashMap<>();
			this.typeTexts = new HashMap<>();
			this.varDecls = new HashMap<>();
			this.typeNameCounter = 0;
			this.varNameCounter = 0;
			this.rtNameCounter = 0;
			this.printer = new TestPrinter2();
		}

		String nextVarName() {
			return "tmpVar_" + varNameCounter++;
		}

		String nextRTName() {
			return "tmpRT_" + rtNameCounter++;
		}

		String nextTypeName() {
			return "tmpType_" + typeNameCounter++;
		}
	}
}
