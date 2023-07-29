package edu.udel.cis.vsl.sarl.util.autotg;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import edu.udel.cis.vsl.sarl.IF.SARLException;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;

public class TestPrinter2 {
	/* Default Imports: */
	final static public String IMPORTS = "import edu.udel.cis.vsl.sarl.SARL;\n"
			+ "import java.io.PrintStream;\n" + "import java.util.Arrays;\n"
			+ "import org.junit.Test;\n" + "\n"
			+ "import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;\n"
			+ "import edu.udel.cis.vsl.sarl.IF.ValidityResult.ResultType;\n"
			+ "import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;\n"
			+ "import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;\n"
			+ "import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;\n"
			+ "import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;\n"
			+ "import edu.udel.cis.vsl.sarl.IF.type.SymbolicArrayType;\n"
			+ "import edu.udel.cis.vsl.sarl.IF.type.SymbolicCompleteArrayType;\n"
			+ "import edu.udel.cis.vsl.sarl.IF.type.SymbolicFunctionType;";
	/* Constants */
	final static public String UNIVERSE = "universe";
	final static public String DEBUG = "debug";
	final static public String OUT = "out";
	// Test File Constants
	final static private String CLASS_DIR = "test/dev/";
	final static private String CLASS_PACKAGE_DEFAULT = "edu/udel/cis/vsl/sarl/autogen/";
	final static private String CLASS_FILE_SUFFIX = ".java";
	// Test Class Constants
	final static private String PACKAGE_PREFIX = "package ";
	final static private String PACKAGE_SUFFIX = "; ";
	final static private String CLASS_HEADER_PREFIX = "public class ";
	final static private String CLASS_HEADER_SUFFIX = " {";
	final static private String CLASS_END = "} // Class End";
	// Test Initialization Constants
	final static private String CLASS_INIT_DEBUG = "final static private boolean "
			+ DEBUG + " = false;";
	final static private String CLASS_INIT_OUT = "final static private PrintStream "
			+ OUT + " = System.out;";
	final static private String CLASS_INIT_UNIV = "private static SymbolicUniverse "
			+ UNIVERSE + " = SARL.newStandardUniverse();";
	final static private String BLANK_LINE = "";
	// Test Unit Constants
	final static private String TEST_CONTENT_DEFAULT = "// This juint test is automatically generated.";
	final static private String TEST_ANNO = "@Test";
	final static private String TEST_PREFIX = "public void agt_";
	final static private String TEST_SUFFIX = "() {";
	final static private String TEST_END = "} // Test End";

	private LinkedList<String> testContent = new LinkedList<String>();

	private static File createTestFile(String packageName, String className) {
		String packagePath = CLASS_DIR + CLASS_PACKAGE_DEFAULT;
		File result = new File(packagePath + className + CLASS_FILE_SUFFIX);

		try {
			new File(packagePath).mkdirs();
			if (result.exists())
				result.delete();
			result.createNewFile();
			return result;
		} catch (IOException e) {
			throw new SARLException(
					"IOException during creating generated test class");
		}
	}

	private static LinkedList<String> genClassHeader(String packageName,
			String className) {
		LinkedList<String> header = new LinkedList<String>();

		header.add(PACKAGE_PREFIX + packageName.replace('/', '.').substring(0,
				packageName.length() - 1) + PACKAGE_SUFFIX);
		header.add(IMPORTS);
		header.add(BLANK_LINE);
		header.add(CLASS_HEADER_PREFIX + className + CLASS_HEADER_SUFFIX);
		header.add(BLANK_LINE);
		header.add(CLASS_INIT_DEBUG);
		header.add(CLASS_INIT_OUT);
		header.add(CLASS_INIT_UNIV);
		header.add(BLANK_LINE);
		return header;
	}

	static void genTestClass(String className, List<String> tests) {
		LinkedList<String> classContent = new LinkedList<String>();

		classContent.addAll(genClassHeader(CLASS_PACKAGE_DEFAULT, className));
		classContent.addAll(tests);
		classContent.add(CLASS_END);
		try {
			Files.write(
					createTestFile(CLASS_PACKAGE_DEFAULT, className).toPath(),
					classContent, Charset.defaultCharset());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected List<String> genUnitTest(int testNum, String testName,
			String... comments) {
		LinkedList<String> unitTest = new LinkedList<String>();
		String header = TEST_PREFIX + testNum + "_" + testName + TEST_SUFFIX;

		for (String comment : comments) {
			assert !comment.contains("/*") || !comment.contains("*/");
			unitTest.add("/*" + comment + "*/");
		}
		unitTest.add(TEST_ANNO);
		unitTest.add(header);
		unitTest.add(TEST_CONTENT_DEFAULT);
		unitTest.addAll(testContent);
		unitTest.add(TEST_END + "\n");
		return unitTest;
	}

	/* ****************** Symbolic Expression Translation ***********/

	void genExpression(String lhsName, SymbolicType lhsType,
			SymbolicOperator op, String... operands) {
		String lhs = lhsName + " = "
				+ (lhsType.isNumeric() ? "(NumericExpression)"
						: (lhsType.isBoolean() ? "(BooleanExpression)" : ""));

		switch (op) {
		case ADD:
			testContent.add(lhs + UNIVERSE + ".add(" + toList(operands) + ");");
			break;
		case AND:
			testContent.add(lhs + UNIVERSE + ".and(" + toList(operands) + ");");
			break;
		case EQUALS:
			testContent.add(lhs + UNIVERSE + ".equals(" + operands[0] + ", "
					+ operands[1] + ");");
			break;
		case SUBTRACT:
			testContent.add(lhs + UNIVERSE + ".subtract(" + operands[0] + ", "
					+ operands[1] + ");");
			break;
		case MULTIPLY:
			testContent.add(
					lhs + UNIVERSE + ".multiply(" + toList(operands) + ");");
			break;
		case APPLY:
			testContent
					.add(lhs + UNIVERSE + ".apply("
							+ operands[0] + ", " + toList(Arrays
									.copyOfRange(operands, 1, operands.length))
							+ ");");
			break;
		case ARRAY:
			testContent
					.add(lhs + UNIVERSE + ".array("
							+ operands[0] + ", " + toList(Arrays
									.copyOfRange(operands, 1, operands.length))
							+ ");");
			break;
		case ARRAY_LAMBDA:
			testContent.add(lhs + UNIVERSE + ".arrayLambda(" + operands[0]
					+ ", " + operands[1] + ");");
			break;
		case ARRAY_READ:
			testContent.add(lhs + UNIVERSE + ".arrayRead(" + operands[0] + ", "
					+ operands[1] + ");");
			break;
		case ARRAY_WRITE:
			testContent.add(lhs + UNIVERSE + ".arrayWrite(" + operands[0] + ", "
					+ operands[1] + ", " + operands[2] + ");");
			break;
		case CAST:
			testContent.add(lhs + UNIVERSE + ".cast(" + operands[0] + ", "
					+ operands[1] + ");");
			break;
		case COND:
			testContent.add(lhs + UNIVERSE + ".cond(" + operands[0] + ", "
					+ operands[1] + ", " + operands[2] + ");");
			break;
		case DENSE_ARRAY_WRITE:
			testContent
					.add(lhs + UNIVERSE + ".denseArrayWrite("
							+ operands[0] + ", " + toList(Arrays
									.copyOfRange(operands, 1, operands.length))
							+ ");");
			break;
		case DIVIDE:
		case INT_DIVIDE:
			testContent.add(lhs + UNIVERSE + ".divide(" + operands[0] + ", "
					+ operands[1] + ");");
			break;
		case EXISTS:
			testContent.add(lhs + UNIVERSE + ".exists(" + operands[0] + ", "
					+ operands[1] + ");");
			break;
		case FORALL:
			testContent.add(lhs + UNIVERSE + ".forall(" + operands[0] + ", "
					+ operands[1] + ");");
			break;
		case LAMBDA:
			testContent.add(lhs + UNIVERSE + ".lambda(" + operands[0] + ", "
					+ operands[1] + ");");
			break;
		case LENGTH:
			testContent.add(lhs + UNIVERSE + ".length(" + operands[0] + ");");
			break;
		case LESS_THAN:
			testContent.add(lhs + UNIVERSE + ".lessThan(" + operands[0] + ", "
					+ operands[1] + ");");
			break;
		case LESS_THAN_EQUALS:
			testContent.add(lhs + UNIVERSE + ".lessThanEquals(" + operands[0]
					+ ", " + operands[1] + ");");
			break;
		case MODULO:
			testContent.add(lhs + UNIVERSE + ".modulo(" + operands[0] + ", "
					+ operands[1] + ");");
			break;
		case NEGATIVE:
			testContent.add(lhs + UNIVERSE + ".minus(" + operands[0] + ");");
			break;
		case NEQ:
			testContent.add(lhs + UNIVERSE + ".neq(" + operands[0] + ", "
					+ operands[1] + ");");
			break;
		case NOT:
			testContent.add(lhs + UNIVERSE + ".not(" + operands[0] + ");");
			break;
		case NULL:
			testContent.add(lhs + UNIVERSE + ".nullExpression();");
			break;
		case OR:
			testContent.add(lhs + UNIVERSE + ".or(" + toList(operands) + ");");
			break;
		case POWER:
			testContent.add(lhs + UNIVERSE + ".power(" + operands[0] + ", "
					+ operands[1] + ");");
			break;
		case TUPLE:
			testContent
					.add(lhs + UNIVERSE + ".tuple("
							+ operands[0] + ", " + toList(Arrays
									.copyOfRange(operands, 1, operands.length))
							+ ");");
			break;
		case TUPLE_READ:
			testContent.add(lhs + UNIVERSE + ".tupleRead(" + operands[0] + ", "
					+ UNIVERSE + ".intObject(" + operands[1] + "));");
			break;
		case TUPLE_WRITE:
			testContent.add(lhs + UNIVERSE + ".tupleWrite(" + operands[0] + ", "
					+ UNIVERSE + ".intObject(" + operands[1] + "), "
					+ operands[2] + ");");
			break;
		case UNION_EXTRACT:
			testContent.add(lhs + UNIVERSE + ".unionExtract(" + UNIVERSE
					+ ".intObject(" + operands[0] + "), " + operands[1] + ");");
			break;
		case UNION_INJECT:
			testContent.add(lhs + UNIVERSE + ".unionInject(" + operands[0]
					+ ", " + UNIVERSE + ".intObject(" + operands[1] + "), "
					+ operands[2] + ");");
			break;
		case UNION_TEST:
			testContent.add(lhs + UNIVERSE + ".unionTest(" + UNIVERSE
					+ ".intObject(" + operands[0] + "), " + operands[1] + ");");
			break;
		case BIT_AND:
		case BIT_NOT:
		case BIT_OR:
		case BIT_SHIFT_LEFT:
		case BIT_SHIFT_RIGHT:
		case BIT_XOR:
		case CONCRETE:
		case DERIV:
		case DIFFERENTIABLE:
		case DENSE_TUPLE_WRITE:
		case SYMBOLIC_CONSTANT:
		default:
			throw new SARLException("unsupported operator " + op);
		}
	}

	void genSigma(String lhsName, SymbolicType lhsType, String... operands) {
		testContent.add(lhsName + " = " + UNIVERSE + ".sigma(" + operands[0]
				+ ", " + operands[1] + ", " + operands[2] + ");");
	}

	void genVariableDeclaration(SymbolicType type, String varName,
			String typeString) {
		String symType = "SymbolicExpression";

		if (type.isNumeric())
			symType = "NumericExpression";
		else if (type.isBoolean())
			symType = "BooleanExpression";

		String cast = "(" + symType + ")";
		String result = symType + " " + varName + " = " + cast + UNIVERSE
				+ ".symbolicConstant(" + UNIVERSE + ".stringObject(\"" + varName
				+ "\"), " + typeString + ");";

		testContent.add(result);
	}

	void genTypeDeclatation(String typeName, SymbolicType type,
			String... args) {
		String[] results = { "", typeName, "=", "" };
		int javaTypeIdx = 0, javaRhsIdx = 3;

		switch (type.typeKind()) {
		case REAL:
			results[javaTypeIdx] = "SymbolicType";
			results[javaRhsIdx] = UNIVERSE + ".realType()";
			break;
		case INTEGER:
			results[javaTypeIdx] = "SymbolicType";
			results[javaRhsIdx] = UNIVERSE + ".integerType()";
			break;
		case BOOLEAN:
			results[javaTypeIdx] = "SymbolicType";
			results[javaRhsIdx] = UNIVERSE + ".booleanType()";
			break;
		case CHAR:
			results[javaTypeIdx] = "SymbolicType";
			results[javaRhsIdx] = UNIVERSE + ".characterType()";
			break;
		case ARRAY:
			if (args.length <= 1) {
				results[javaTypeIdx] = "SymbolicArrayType";
				results[javaRhsIdx] = UNIVERSE + ".arrayType(" + args[0] + ")";
			} else {
				results[javaTypeIdx] = "SymbolicCompleteArrayType";
				results[javaRhsIdx] = UNIVERSE + ".arrayType(" + args[0] + ", "
						+ args[1] + ")";
			}
			break;
		case FUNCTION:
			results[javaTypeIdx] = "SymbolicFunctionType";
			results[javaRhsIdx] = UNIVERSE + ".functionType("
					+ toList(Arrays.copyOfRange(args, 0, args.length - 1))
					+ ", " + args[args.length - 1] + ")";
			break;
		case TUPLE:
			results[javaTypeIdx] = "SymbolicTupleType";
			results[javaRhsIdx] = UNIVERSE + ".tupleType(" + UNIVERSE
					+ ".stringObject(\"" + args[0] + "\"), "
					+ toList(Arrays.copyOfRange(args, 1, args.length)) + ")";
			break;
		case UNINTERPRETED:
			results[javaTypeIdx] = "SymbolicUninterpretedType";
			results[javaRhsIdx] = UNIVERSE + ".symbolicUninterpretedType("
					+ args[0] + ")";
			break;
		case UNION:
			results[javaTypeIdx] = "SymbolicUnionType";
			results[javaRhsIdx] = UNIVERSE + ".unionType(" + UNIVERSE
					+ ".stringObject(" + args[0] + "), "
					+ toList(Arrays.copyOfRange(args, 1, args.length)) + ")";
			break;
		case MAP:
		case SET:
		default:
			throw new SARLException("Unsupported type : " + type);
		}
		testContent.add(results[javaTypeIdx] + " " + results[1] + " "
				+ results[2] + " " + results[javaRhsIdx] + ";");
	}

	void genResultTypeDeclatation(String resultTypeVarName) {
		testContent.add("ResultType " + resultTypeVarName + ";");
	}

	void callValid(String resultTypeLHS, String context, String predicate) {
		String result = resultTypeLHS + " = " + UNIVERSE + ".reasoner("
				+ context + ").valid(" + predicate + ").getResultType();";

		testContent.add(result);
	}

	void callValidWhy3(String resultTypeLHS, String context, String predicate) {
		String result = resultTypeLHS + " = " + UNIVERSE + ".why3Reasoner("
				+ context + ", new ProverFunctionInterpretation[0]).valid("
				+ predicate + ").getResultType();";

		testContent.add(result);
	}

	void assertEquals(String expected, String actual) {
		String result = "org.junit.Assert.assertEquals(" + expected + ", "
				+ actual + ");";

		testContent.add(result);
	}

	private String toList(String[] operands) {
		String result = "Arrays.asList(";

		for (int i = 0; i < operands.length - 1; i++)
			result += operands[i] + ", ";
		result += operands.length > 0 ? operands[operands.length - 1] + ")"
				: ")";
		return result;
	}
}
