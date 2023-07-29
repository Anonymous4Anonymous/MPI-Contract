package edu.udel.cis.vsl.sarl.util.autotg;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;

import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType.SymbolicTypeKind;

public class TestPrinter {
	/* Constants */
	final static public String UNIVERSE = "universe";
	final static public String DEBUG = "debug";
	final static public String OUT = "out";
	// Test File Constants
	final static private String CLASS_DIR = "test/autogen/";
	final static private String CLASS_PACKAGE_DEFAULT = "edu/udel/cis/vsl/sarl/autogen/";
	final static private String CLASS_NAME_DEFAULT = "AutoGenTest";
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
	final static private String TEST_NAME_DEFAULT = "";
	final static private String TEST_CONTENT_DEFAULT = "// This uint test is automatically generated.";
	final static private String TEST_ANNO = "@Test";
	final static private String TEST_PREFIX = "public void AutoGenTest";
	final static private String TEST_SUFFIX = "() {";
	final static private String TEST_END = "} // Test End";

	private int unitTestCounter = 0;

	private LinkedList<String> testContent = new LinkedList<String>();
	private File testFile = new File(CLASS_DIR + CLASS_PACKAGE_DEFAULT
			+ CLASS_NAME_DEFAULT + CLASS_FILE_SUFFIX);

	private void createTestFile(String packageName, String className) {
		String packagePath = CLASS_DIR + CLASS_PACKAGE_DEFAULT;

		this.testFile = new File(packagePath + className + CLASS_FILE_SUFFIX);
		try {
			new File(packagePath).mkdirs();
			if (testFile.exists())
				testFile.delete();
			testFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private LinkedList<String> genClassHeader(String packageName,
			String className) {
		LinkedList<String> header = new LinkedList<String>();

		header.add(PACKAGE_PREFIX + packageName.replace('/', '.').substring(0,
				packageName.length() - 1) + PACKAGE_SUFFIX);
		header.add(BLANK_LINE);
		header.add(CLASS_HEADER_PREFIX + className + CLASS_HEADER_SUFFIX);
		header.add(BLANK_LINE);
		header.add(CLASS_INIT_DEBUG);
		header.add(CLASS_INIT_OUT);
		header.add(CLASS_INIT_UNIV);
		header.add(BLANK_LINE);
		return header;
	}

	private String genTestNumber() {
		if (unitTestCounter < 10)
			return "___" + unitTestCounter + "_";
		else if (unitTestCounter < 100)
			return "__" + unitTestCounter + "_";
		else
			return "_" + unitTestCounter + "_";
	}

	protected void genTestClass(String className, List<String> tests) {
		LinkedList<String> classContent = new LinkedList<String>();

		if (className.length() < 1)
			className = CLASS_NAME_DEFAULT;
		createTestFile(CLASS_PACKAGE_DEFAULT, className);
		classContent.addAll(genClassHeader(CLASS_PACKAGE_DEFAULT, className));
		classContent.addAll(tests);
		classContent.add(CLASS_END);
		try {
			Files.write(testFile.toPath(), classContent,
					Charset.defaultCharset());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected List<String> genUnitTest(String testName) {
		LinkedList<String> unitTest = new LinkedList<String>();
		String header = TEST_PREFIX + genTestNumber() + testName + TEST_SUFFIX;

		unitTest.add(TEST_ANNO);
		unitTest.add(header);
		unitTest.add(TEST_CONTENT_DEFAULT);
		unitTest.addAll(testContent);
		unitTest.add(TEST_END);
		unitTestCounter++;
		return unitTest;
	}

	@SuppressWarnings("incomplete-switch")
	protected void genExpression(String resName, SymbolicOperator op,
			String... operands) {
		String lhs = resName + " = ";

		switch (op) {
		case ADD:
			testContent.add(lhs + UNIVERSE + ".add(" + operands[0] + ", "
					+ operands[1] + ");");
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
			testContent.add(lhs + UNIVERSE + ".multiply(" + operands[0] + ", "
					+ operands[1] + ");");
			break;
		}
	}

	protected void genVariableDeclaration(SymbolicType type, String varName,
			String typeString) {
		String symConstText = "SymbolicExpression";
		String numericSymConstText = "NumericExpression";
		String symType = symConstText;

		if (type.typeKind() == SymbolicTypeKind.INTEGER
				|| type.typeKind() == SymbolicTypeKind.REAL)
			symType = numericSymConstText;

		String cast = "(" + symType + ")";
		String result = symType + " " + varName + " = " + cast + UNIVERSE
				+ ".symbolicConstant(" + UNIVERSE + ".stringObject(\"" + varName
				+ "\"), " + typeString + ");";

		testContent.add(result);
	}

	protected void genTypeDeclatation(String type, String typeName) {

	}

	protected void callValid(String resultTypeLHS, String context,
			String predicate) {
		String result = resultTypeLHS + " = " + UNIVERSE + ".reasoner("
				+ context + ").valid(" + predicate + ").getResultType();";

		testContent.add(result);
	}

	static public void main(String args[]) {
		TestPrinter tp = new TestPrinter();

		tp.genTestClass(CLASS_NAME_DEFAULT, tp.genUnitTest(TEST_NAME_DEFAULT));
		System.out.println("Terminated");
	}
}
