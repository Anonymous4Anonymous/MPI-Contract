package edu.udel.cis.vsl.sarl.prove.z3;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import edu.udel.cis.vsl.sarl.IF.SARLConstants;
import edu.udel.cis.vsl.sarl.IF.SARLInternalException;
import edu.udel.cis.vsl.sarl.IF.TheoremProverException;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericSymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.IF.number.IntegerNumber;
import edu.udel.cis.vsl.sarl.IF.number.RationalNumber;
import edu.udel.cis.vsl.sarl.IF.object.BooleanObject;
import edu.udel.cis.vsl.sarl.IF.object.CharObject;
import edu.udel.cis.vsl.sarl.IF.object.IntObject;
import edu.udel.cis.vsl.sarl.IF.object.NumberObject;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicSequence;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicArrayType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicCompleteArrayType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicFunctionType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicTupleType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType.SymbolicTypeKind;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicTypeSequence;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicUninterpretedType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicUnionType;
import edu.udel.cis.vsl.sarl.preuniverse.IF.PreUniverse;
import edu.udel.cis.vsl.sarl.prove.IF.ProverFunctionInterpretation;
import edu.udel.cis.vsl.sarl.util.FastList;
import edu.udel.cis.vsl.sarl.util.Pair;

/**
 * <p>
 * Translates SARL {@link SymbolicExpression}s to the language of the automated
 * theorem prover Z3.
 * </p>
 * 
 * Notes on the Z3 language:
 * 
 * <pre>
 * // Invocation:
 * z3 -smt2 -in
 * 
 * // Commands:
 * (assert expr)
 * (check-sat).
 * 
 * To check if predicate is valid under context:
 * (assert context)
 * (assert (not predicate))
 * (check-sat)
 * if result is "sat": answer is NO (not valid)
 * if result is "unsat": answer is YES (valid)
 * otherwise, MAYBE
 * 
 * // Basic Types:
 * Int
 * Bool
 * Real
 * 
 * // Constants
 * true
 * false
 * integers, decimals, fractions can be written (/ a b)
 * 
 * // Boolean ops
 * and or not ite =>
 *  
 * // Symbolic constants:
 * (declare-const all1 (Array Int Int))
 * 
 * // Symbolic constants of functional type:
 * (declare-fun f (Int Bool) Int)
 * 
 * // Tuples:
 * (declare-datatypes () ((T1 (mk-T1 (proj_0 Int) (proj_1 Real)))))
 * (declare-datatypes () ((T2 (mk-T2 (proj_0 Real) (proj_1 Int)))))
 * (simplify (mk-T1 2 3.1))
 * (simplify (proj_0 (mk-T1 2 3.1)))
 * (simplify (= (mk-T1 2 3.1) (mk-T1 2 3.1)))
 * update(t, i, new_val) is equivalent to mk_tuple(proj_0(t), ..., new_val, ..., proj_n(t))
 * 
 * // Arrays:
 * (declare-const a1 (Array Int Int))
 * (select a1 index)
 * (store a index value)
 * 
 * // Constant arrays:
 * (declare-const all1 (Array Int Int))
 * (assert (= all1 ((as const (Array Int Int)) 1)))
 * 
 * // Lambdas: no lambda expressions; use macros:
 * (define-fun mydiv ((x Real) (y Real)) Real
 *   (if (not (= y 0.0))
 *       (/ x y)
 *       0.0))
 * 
 * declare them and then use the function name where the macro was called for
 * 
 * // Array lambdas:
 * just make an assertion that says the elements of the array are equal to the
 * evaluation of the function
 * 
 * // Union types
 * // union of Int and Real:
 * (declare-datatypes () ((U1 (inject_1_0 (extract_1_0 Int)) (inject_1_1 (extract_1_1 Real)))))
 * (simplify (inject_1_0 34))
 * (simplify (is-inject_1_0 (inject_1_0 34)))
 * 
 * // Integer division and modulus
 * There is both mod and remainder:
 * 
 * (assert (= r1 (div a 4))) ; integer division
 * (assert (= r2 (mod a 4))) ; mod
 * (assert (= r3 (rem a 4))) ; remainder
 * (assert (= r4 (div a (- 4)))) ; integer division
 * (assert (= r5 (mod a (- 4)))) ; mod
 * (assert (= r6 (rem a (- 4)))) ; remainder
 * 
 * (simplify (mod 10 -4))
 * 2
 * (simplify (rem 10 -4))
 * (- 2)
 * (simplify (div 10 -4))
 * (- 2)
 * 
 * First order quantifiers:
 * 
 * (forall ((x Int)) (p x))
 * 
 * </pre>
 * 
 * <p>
 * // Uninterpreted type:
 *
 * Translation of expressions of uninterpreted types: For an uninterpreted type
 * <code>t</code>, it will be translated to a type definition with a single key
 * of int type:
 * <code>(declare-datatypes (Int) (Unintpret_t (Cons-t (Select_key_t Int))))</code>
 * . Symbolic expressions of type t with {@link SymbolicOperator#CONCRETE}
 * operator will be translated using the constructor <code>Cons_t</code> as
 * <code>(Const_t key)</code>. Selector <code>Select_key_t</code> will never be
 * used for now.
 * </p>
 * 
 * @author Stephen F. Siegel
 */
public class Z3Translator {

	/**
	 * The length of bit-vector represents an integer;
	 */
	private String BITLEN_INT = "32";

	/**
	 * The symbolic universe used to create and manipulate SARL symbolic
	 * expressions.
	 */
	private PreUniverse universe;

	/**
	 * The number of auxiliary Z3 variables created. These are the variables
	 * that do not correspond to any SARL variable but are needed for some
	 * reason to translate an expression. Includes both ordinary and bound Z3
	 * variables.
	 */
	private int z3AuxVarCount;

	/**
	 * The number of auxiliary SARL variables created. These are used for
	 * integer index variables for expressing array equality.
	 */
	private int sarlAuxVarCount;

	/**
	 * Mapping of SARL symbolic expression to corresponding Z3 expression. Used
	 * to cache the results of translation.
	 */
	private Map<SymbolicExpression, FastList<String>> expressionMap;

	/**
	 * Mapping of pairs (t1,t2) of SARL types to the uninterpreted function
	 * symbol which represents casting from t1 to t2. The function has type
	 * "function from translate(t1) to translate(t2)".
	 */
	private Map<Pair<SymbolicType, SymbolicType>, String> castMap;

	/**
	 * Map from SARL symbolic constants to corresponding Z3 expressions. Entries
	 * are a subset of those of {@link #expressionMap}.
	 */
	private Map<SymbolicConstant, FastList<String>> variableMap;

	/**
	 * Mapping of SARL symbolic type to corresponding Z3 type. Used to cache
	 * results of translation.
	 */
	private Map<SymbolicType, FastList<String>> typeMap;

	/**
	 * A set of pairs of declared Z3 function names and their declaration texts.
	 * Used to avoid duplicated function declarations. Z3 allows functions of
	 * different types share the same function name.
	 */
	private Set<Pair<String, String>> functionSet;

	/**
	 * Has the "bigArray" type been defined?
	 */
	private boolean bigArrayDefined = false;

	/**
	 * The declarations section resulting from the translation. This contains
	 * all the declarations of symbols used in the resulting CVC input.
	 */
	private FastList<String> z3Declarations;

	/**
	 * The expression which is the result of translating the given symbolic
	 * expression.
	 */
	private FastList<String> z3Translation;

	/**
	 * A map that maps {@link SymbolicExpression}s to temporary binding names so
	 * that they can be reused. The translation is then processed in a
	 * compressed way. If this map is instantiated, this translator is working
	 * in this compressed way.
	 */
	private Map<SymbolicExpression, FastList<String>> subExpressionsBindingNames = null;

	/**
	 * All binding translations. Eventually, these bindings will be added on the
	 * head of the translation as <code>(let (bindings) (translation))</code>
	 */
	private List<FastList<String>> subExpressionBindings = null;

	/**
	 * If the size of the context or a predicate exceends this threshold, this
	 * translator is working in a compressed way.
	 */
	private static final int FULL_EXPR_SIZE_THRESHOLD = 100;

	/**
	 * If the size of a single symbolic expression exceeds this threshold, it
	 * will be translated into a binding and keep being used in a compressed
	 * way.
	 */
	private static final int SINGLE_EXPR_SIZE_THRESHOLD = 10;

	/**
	 * A stack of bound variables. A new entry will be pushed onto this stack
	 * once the translation enters a quantified expression, and a top entry will
	 * gets popped out once the translation finishes a quantified expression.
	 */
	private Stack<SymbolicConstant> boundVariableStack = new Stack<>();

	/**
	 * The flag controls whether the translation should be done with a
	 * compressed form (if the size of the formula exceeds some threshold). By
	 * default it is on.
	 */
	private boolean enableCompression = true;

	// Constructors...

	public Z3Translator(PreUniverse universe, SymbolicExpression theExpression,
			boolean simplifyIntDivision) {
		this(universe, theExpression, simplifyIntDivision,
				new ProverFunctionInterpretation[0]);
	}

	public Z3Translator(PreUniverse universe, SymbolicExpression theExpression,
			boolean simplifyIntDivision,
			ProverFunctionInterpretation logicFunctions[]) {
		this.universe = universe;
		this.z3AuxVarCount = 0;
		this.sarlAuxVarCount = 0;
		this.expressionMap = new HashMap<>();
		this.castMap = new HashMap<>();
		this.variableMap = new HashMap<>();
		this.typeMap = new HashMap<>();
		this.functionSet = new HashSet<>();
		this.z3Declarations = new FastList<>();
		if (theExpression.size() >= FULL_EXPR_SIZE_THRESHOLD) {
			this.subExpressionsBindingNames = new HashMap<>();
			this.subExpressionBindings = new LinkedList<>();
		}
		// translate logic functions:
		for (ProverFunctionInterpretation logicFunction : logicFunctions)
			translateLogicFunction(logicFunction);
		this.z3Translation = translate(theExpression);
	}

	public Z3Translator(Z3Translator startingContext,
			SymbolicExpression theExpression) {
		this.universe = startingContext.universe;
		this.z3AuxVarCount = startingContext.z3AuxVarCount;
		this.sarlAuxVarCount = startingContext.sarlAuxVarCount;
		this.castMap = new HashMap<>(startingContext.castMap);
		this.typeMap = new HashMap<>(startingContext.typeMap);
		this.functionSet = new HashSet<>(startingContext.functionSet);
		this.expressionMap = new HashMap<>(startingContext.expressionMap);
		this.variableMap = new HashMap<>(startingContext.variableMap);
		if (theExpression.size() >= FULL_EXPR_SIZE_THRESHOLD
				|| startingContext.subExpressionBindings != null) {
			this.subExpressionsBindingNames = new HashMap<>();
			this.subExpressionBindings = new LinkedList<>();
			// add bindings from context to this translation since some binding
			// symbols created in context will be used again:
			if (startingContext.subExpressionBindings != null)
				this.subExpressionBindings
						.addAll(startingContext.subExpressionBindings);
		}
		this.bigArrayDefined = startingContext.bigArrayDefined;
		this.z3Declarations = new FastList<>();
		this.z3Translation = translate(theExpression);
	}

	// Private methods...
	private void requireBigArray() {
		assert SARLConstants.z3UseBigArray;
		if (!bigArrayDefined) {
			z3Declarations.add(
					"(declare-datatypes (T) ((BigArray (mk-BigArray (bigArray-len Int) (bigArray-val (Array Int T))))))\n");
			bigArrayDefined = true;
		}
	}

	private String tupleTypeName(SymbolicTupleType tupleType) {
		return "Tuple-" + tupleType.name().getString();
	}

	private String tupleConstructor(SymbolicTupleType tupleType) {
		return "mk-" + tupleTypeName(tupleType);
	}

	private String tupleProjector(SymbolicTupleType tupleType, int index) {
		return "proj-" + tupleTypeName(tupleType) + "_" + index;
	}

	private String unionTypeName(SymbolicUnionType unionType) {
		return "Union-" + unionType.name().getString();
	}

	private String uninterpretedTypeName(
			SymbolicUninterpretedType uninterpretedType) {
		return "Unintpret-" + uninterpretedType.name().getString();
	}

	private String uninterpretedTypeConstructor(
			SymbolicUninterpretedType uninterpretedType) {
		return "Cons-" + uninterpretedType.name().getString();
	}

	/**
	 * Computes the name of the index-th selector function into a union type.
	 * This is the function that takes an element of the union and returns an
	 * element of the index-th member type.
	 * 
	 * @param unionType
	 *            a union type
	 * @param index
	 *            integer in [0,n), where n is the number of member types of the
	 *            union type
	 * @return the name of the index-th selector function
	 */
	private String unionSelector(SymbolicUnionType unionType, int index) {
		return unionTypeName(unionType) + "_extract_" + index;
	}

	/**
	 * Computes the name of the index-th constructor function for a union type.
	 * This is the function which takes as input an element of the index-th
	 * member type and returns an element of the union type.
	 * 
	 * @param unionType
	 *            a union type
	 * @param index
	 *            an integer in [0,n), where n is the number of member types of
	 *            the union type
	 * @return the name of the index-th constructor function
	 */
	private String unionConstructor(SymbolicUnionType unionType, int index) {
		return unionTypeName(unionType) + "_inject_" + index;
	}

	private String unionTester(SymbolicUnionType unionType, int index) {
		return "is-" + unionConstructor(unionType, index);
	}

	/**
	 * Creates a new Z3 (ordinary) variable of given type with unique name;
	 * increments {@link #z3AuxVarCount}. CANNOT be used for a function type.
	 * 
	 * @param type
	 *            a Z3 type; it is consumed, so cannot be used after invoking
	 *            this method
	 * @return the new Z3 variable
	 */
	private String newZ3AuxVar(FastList<String> type) {
		String name = "t" + z3AuxVarCount;

		z3Declarations.addAll("(declare-const ", name);
		type.addFront(" ");
		z3Declarations.append(type);
		z3Declarations.add(")\n");
		z3AuxVarCount++;
		return name;
	}

	/**
	 * Returns a new SARL symbolic constant of integer type. Increments
	 * {@link #sarlAuxVarCount}.
	 * 
	 * @return new symbolic constant of integer type
	 */
	private NumericSymbolicConstant newSarlAuxVar() {
		NumericSymbolicConstant result = (NumericSymbolicConstant) universe
				.symbolicConstant(universe.stringObject("_i" + sarlAuxVarCount),
						universe.integerType());

		sarlAuxVarCount++;
		return result;
	}

	/**
	 * Creates "big array" expression: an ordered pair consisting of an integer
	 * expression which is the length of the array, and an expression of array
	 * type which is the contents.
	 * 
	 * @param length
	 *            Z3 expression yielding length of array; it is consumed (so
	 *            cannot be used after invoking this method)
	 * @param value
	 *            Z3 expression of type "array-of-T"; it is consumed (so cannot
	 *            be used after invoking this method)
	 * @return ordered pair (tuple), consisting of length and value
	 */
	private FastList<String> bigArray(FastList<String> length,
			FastList<String> value) {
		FastList<String> result = new FastList<String>();

		if (SARLConstants.z3UseBigArray) {
			requireBigArray();
			result.addAll("(mk-BigArray ");
			result.append(length);
			result.add(" ");
			result.append(value);
			result.add(")");
		} else
			result.append(value);
		return result;
	}

	/**
	 * <p>
	 * Given a SARL expression of array type, this method computes the Z3
	 * representation of the length of that array. This is a Z3 expression of
	 * integer type.
	 * </p>
	 * 
	 * <p>
	 * Convergence criterion: this method calls {@link #translate}, and
	 * {@link #translate} calls this method. In order for the recursion to
	 * terminate, the following protocol must be followed: {@link #translate}
	 * should never call this method on its entire argument; it should only call
	 * this method on a proper sub-tree of its argument.
	 * </p>
	 * 
	 * @param array
	 *            a SARL expression of array type
	 * @return translation into Z3 of length of that array
	 */
	private FastList<String> lengthOfArray(SymbolicExpression array) {
		SymbolicArrayType type = (SymbolicArrayType) array.type();

		// imagine translating "length(a)" for a symbolic constant a.
		// this calls lengthOfArray(a). This calls translate(a).
		// Since a is a symbolic constant, this yields a CVC symbolic
		// constant A. The result returned is "(A).0".

		if (type instanceof SymbolicCompleteArrayType)
			return translate(((SymbolicCompleteArrayType) type).extent());
		if (SARLConstants.z3UseBigArray) {
			// there are three kinds of array expressions for which translation
			// results in a literal ordered pair [int,array]: SEQUENCE,
			// ARRAY_WRITE, DENSE_ARRAY_WRITE. A concrete (SEQUENCE) array
			// always
			// has complete type.
			switch (array.operator()) {
			case ARRAY:
				throw new SARLInternalException("Unreachable");
			case ARRAY_WRITE:
			case DENSE_ARRAY_WRITE:
				return lengthOfArray((SymbolicExpression) array.argument(0));
			default:
				FastList<String> result = new FastList<>("(bigArray-len ");

				result.append(translate(array));
				result.add(")");
				return result;
			}
		} else
			throw new SARLInternalException(
					"Unimplemented feature: translate LENGTH when BigArray is not used");
	}

	private FastList<String> pretranslateConcreteArray(
			SymbolicExpression array) {
		SymbolicCompleteArrayType arrayType = (SymbolicCompleteArrayType) array
				.type();
		SymbolicType elementType = arrayType.elementType();
		NumericExpression extentExpression = arrayType.extent();
		IntegerNumber extentNumber = (IntegerNumber) universe
				.extractNumber(extentExpression);
		int size = array.numArguments();
		FastList<String> z3ArrayType = new FastList<>("(Array Int ");

		z3ArrayType.append(translateType(elementType));
		z3ArrayType.add(")");
		assert extentNumber != null && extentNumber.intValue() == size;

		FastList<String> result = new FastList<>(newZ3AuxVar(z3ArrayType));

		for (int i = 0; i < size; i++) {
			result.addFront("(store ");
			result.addAll(" ", Integer.toString(i), " ");
			result.append(translate((SymbolicExpression) array.argument(i)));
			result.add(")");
		}
		return result;
	}

	private FastList<String> pretranslateArrayWrite(
			SymbolicExpression arrayWrite) {
		// syntax: (store a index value)
		SymbolicExpression arrayExpression = (SymbolicExpression) arrayWrite
				.argument(0);
		NumericExpression indexExpression = (NumericExpression) arrayWrite
				.argument(1);
		SymbolicExpression valueExpression = (SymbolicExpression) arrayWrite
				.argument(2);
		FastList<String> result = new FastList<>("(store ");

		result.append(valueOfArray(arrayExpression));
		result.add(" ");
		result.append(translate(indexExpression));
		result.add(" ");
		result.append(translate(valueExpression));
		result.add(")");
		return result;
	}

	private FastList<String> pretranslateDenseArrayWrite(
			SymbolicExpression denseArrayWrite) {
		// syntax: lots of nested stores
		SymbolicExpression arrayExpression = (SymbolicExpression) denseArrayWrite
				.argument(0);
		SymbolicSequence<?> elements = (SymbolicSequence<?>) denseArrayWrite
				.argument(1);
		int n = elements.size();
		FastList<String> result = valueOfArray(arrayExpression);

		for (int i = 0; i < n; i++) {
			SymbolicExpression element = elements.get(i);

			if (!element.isNull()) {
				result.addFront("(store ");
				result.addAll(" ", Integer.toString(i), " ");
				result.append(translate(element));
				result.add(")");
			}
		}
		return result;
	}

	/**
	 * Given a SARL expression of array type, this method computes the Z3
	 * representation of array type corresponding to that array. The result will
	 * be a Z3 expression of type array-of-T, where T is the element type.
	 * 
	 * @param array
	 * @return
	 */
	private FastList<String> valueOfArray(SymbolicExpression array) {
		// the idea is to catch any expression which would be translated
		// as an explicit ordered pair [len,val] and return just the val.
		// for expressions that are not translated to an explicit
		// ordered pair, just apply bigArray-val to get the array value
		// component.
		switch (array.operator()) {
		case ARRAY:
			return pretranslateConcreteArray(array);
		case ARRAY_WRITE:
			return pretranslateArrayWrite(array);
		case DENSE_ARRAY_WRITE:
			return pretranslateDenseArrayWrite(array);
		default: {
			if (SARLConstants.z3UseBigArray) {
				FastList<String> result = new FastList<>("(bigArray-val ");

				result.append(translate(array));
				result.add(")");
				return result;
			} else
				return translate(array);
		}
		}
	}

	/**
	 * Translates a concrete SARL array into language of Z3.
	 * 
	 * @param arrayType
	 *            a SARL complete array type
	 * @param elements
	 *            a sequence of elements whose types are all the element type of
	 *            the arrayType
	 * @return Z3 translation of the concrete array
	 */
	private FastList<String> translateConcreteArray(SymbolicExpression array) {
		FastList<String> result = pretranslateConcreteArray(array);
		int size = array.numArguments();

		result = bigArray(new FastList<>(Integer.toString(size)), result);
		return result;
	}

	private FastList<String> translateConcreteTuple(SymbolicExpression tuple) {
		// syntax: (mk-T e0 e1 ...)
		FastList<String> result;
		SymbolicTupleType type = (SymbolicTupleType) tuple.type();
		int n = tuple.numArguments();

		// declare the tuple type if you haven't already
		translateType(type);
		result = new FastList<String>("(" + tupleConstructor(type));
		for (int i = 0; i < n; i++) {
			SymbolicExpression member = (SymbolicExpression) tuple.argument(i);
			result.add(" ");
			result.append(translate(member));
		}
		result.add(")");
		return result;
	}

	/**
	 * Translates any concrete SymbolicExpression with concrete type to
	 * equivalent Z3 expression using the ExprManager.
	 * 
	 * @param expr
	 *            any symbolic expression of kind CONCRETE
	 * @return the Z3 equivalent expression
	 */
	private FastList<String> translateConcrete(SymbolicExpression expr) {
		SymbolicType type = expr.type();
		SymbolicTypeKind kind = type.typeKind();
		SymbolicObject object = expr.argument(0);
		FastList<String> result;

		switch (kind) {
		case BOOLEAN:
			result = new FastList<>(
					((BooleanObject) object).getBoolean() ? "true" : "false");
			break;
		case CHAR:
			result = new FastList<>(
					Integer.toString((int) ((CharObject) object).getChar()));
			break;
		case INTEGER:
			result = new FastList<>(object.toString());
			break;
		case REAL: {
			RationalNumber number = (RationalNumber) ((NumberObject) object)
					.getNumber();
			String numerator = number.numerator().toString(),
					denominator = number.denominator().toString();

			if (denominator.equals("1"))
				result = new FastList<>(numerator);
			else
				result = new FastList<>("(/ ", numerator, " ", denominator,
						")");
			break;
		}
		case UNINTERPRETED: {
			SymbolicUninterpretedType uintType = (SymbolicUninterpretedType) type;
			int key = uintType.soleSelector().apply(expr).getInt();

			translateType(uintType);
			result = new FastList<>("(", uninterpretedTypeConstructor(uintType),
					" ", String.valueOf(key), " )");
			break;
		}
		default:
			throw new SARLInternalException("Unknown concrete object: " + expr);
		}
		return result;
	}

	private FastList<String> functionDeclaration(String name,
			SymbolicFunctionType functionType) {
		FastList<String> result = new FastList<>("(declare-fun ", name, " (");
		boolean first = true;

		for (SymbolicType inputType : functionType.inputTypes()) {
			if (first)
				first = false;
			else
				result.add(" ");
			result.append(translateType(inputType));
		}
		result.add(") ");
		result.append(translateType(functionType.outputType()));
		result.add(")\n");

		Pair<String, String> key = new Pair<>(name, result.toString());

		if (functionSet.contains(key))
			return new FastList<>();
		functionSet.add(key);
		return result;
	}

	/**
	 * Translates a symbolic constant. It returns simply the name of the
	 * symbolic constant (in the form of a <code>FastList</code> of strings).
	 * For an ordinary (i.e., not quantified) symbolic constant, this method
	 * also adds to {@link #z3Declarations} a declaration of the symbolic
	 * constant.
	 * 
	 * @param symbolicConstant
	 *            a SARL symbolic constant
	 * @param isBoundVariable
	 *            is this a bound variable?
	 * @return the name of the symbolic constant as a fast string list
	 */
	private FastList<String> translateSymbolicConstant(
			SymbolicConstant symbolicConstant, boolean isBoundVariable) {
		String name = symbolicConstant.name().getString();
		FastList<String> result = new FastList<>(name);
		SymbolicType symbolicType = symbolicConstant.type();

		if (symbolicType.typeKind() == SymbolicTypeKind.FUNCTION) {
			z3Declarations.append(functionDeclaration(name,
					(SymbolicFunctionType) symbolicType));
		} else {
			if (!isBoundVariable) {
				FastList<String> z3Type = translateType(symbolicType);

				z3Declarations.addAll("(declare-const ", name, " ");
				z3Declarations.append(z3Type);
				z3Declarations.add(")\n");
			}
		}
		this.variableMap.put(symbolicConstant, result); // currently not used
		this.expressionMap.put(symbolicConstant, result);
		return result.clone();
	}

	/**
	 * There is no lambda expression in Z3, but you can use the macro facility
	 * 
	 * <pre>
	 * (define-fun funcName ((x1 T1) (x2 T2) ...) T expr)
	 * </pre>
	 * 
	 * where T1, T2, ... are the input types, T is the output type, x1, x2, ...,
	 * are the formal parameters, and expr is the function body. This method
	 * creates a fresh function symbol, and adds the macro to
	 * {@link #z3Declarations}.
	 * 
	 * @param lambdaExpression
	 *            symbolic expression of kind {@link SymbolicOperator#LAMBDA}
	 * @return new function macro symbol representing the lambda
	 */
	private FastList<String> translateLambda(
			SymbolicExpression lambdaExpression) {
		int argsNum = lambdaExpression.numArguments();
		SymbolicFunctionType functionType = (SymbolicFunctionType) lambdaExpression
				.type();
		SymbolicExpression body = (SymbolicExpression) lambdaExpression
				.argument(argsNum - 1);
		String name = "_lambda_" + z3AuxVarCount;
		FastList<String> z3SymbolicConstants = new FastList<>();

		for (int i = 0; i < argsNum - 1; i++) {
			SymbolicConstant inputVar = (SymbolicConstant) lambdaExpression
					.argument(i);

			z3SymbolicConstants.add("(");
			z3SymbolicConstants
					.append(translateSymbolicConstant(inputVar, true));
			z3SymbolicConstants.add(" ");
			z3SymbolicConstants.append(translateType(inputVar.type()));
			z3SymbolicConstants.add(")");
			if (i != argsNum - 2) {
				z3SymbolicConstants.add(" ");
			}
		}
		FastList<String> z3OutputType = translateType(
				functionType.outputType());
		FastList<String> z3Body = translate(body);

		z3Declarations.addAll("(define-fun ", name, "(");
		z3Declarations.append(z3SymbolicConstants);
		z3Declarations.add(") ");
		z3Declarations.append(z3OutputType);
		z3Declarations.add(" ");
		z3Declarations.append(z3Body);
		z3Declarations.add(")\n");
		z3AuxVarCount++;
		return new FastList<>(name);
	}

	/**
	 * Translates an array-read expression a[i] into equivalent Z3 expression.
	 * Syntax: <code>(select a index)</code>.
	 * 
	 * @param expr
	 *            a SARL symbolic expression of form a[i]
	 * @return an equivalent Z3 expression
	 */
	private FastList<String> translateArrayRead(SymbolicExpression expr) {
		SymbolicExpression arrayExpression = (SymbolicExpression) expr
				.argument(0);
		NumericExpression indexExpression = (NumericExpression) expr
				.argument(1);
		FastList<String> result = new FastList<>("(select ");

		result.append(valueOfArray(arrayExpression));
		result.add(" ");
		result.append(translate(indexExpression));
		result.add(")");
		return result;
	}

	/**
	 * Translates a tuple-read expression t.i into equivalent Z3 expression.
	 * 
	 * Recall: TUPLE_READ: 2 arguments: arg0 is the tuple expression. arg1 is an
	 * IntObject giving the index in the tuple.
	 * 
	 * @param expr
	 *            a SARL symbolic expression of kind
	 *            {@link SymbolicOperator#TUPLE_READ}
	 * @return an equivalent Z3 expression
	 */
	private FastList<String> translateTupleRead(SymbolicExpression expr) {
		// we can assume the tuple type has already been declared
		SymbolicExpression tupleExpression = (SymbolicExpression) expr
				.argument(0);
		int index = ((IntObject) expr.argument(1)).getInt();
		FastList<String> result = new FastList<>("(", tupleProjector(
				(SymbolicTupleType) tupleExpression.type(), index), " ");

		result.append(translate(tupleExpression));
		result.add(")");
		return result;
	}

	/**
	 * Translates an array-write (or array update) SARL symbolic expression to
	 * equivalent Z3 expression.
	 * 
	 * @param expr
	 *            a SARL array expression of kind
	 *            {@link SymbolicOperator#ARRAY_WRITE}
	 * @return the result of translating to Z3
	 */
	private FastList<String> translateArrayWrite(SymbolicExpression expr) {
		FastList<String> result = pretranslateArrayWrite(expr);

		result = bigArray(lengthOfArray(expr), result);
		return result;
	}

	/**
	 * <p>
	 * Translates a tuple-write (or tuple update) SARL symbolic expression to
	 * equivalent Z3 expression.
	 * </p>
	 * 
	 * <p>
	 * Recall: TUPLE_WRITE: 3 arguments: arg0 is the original tuple expression,
	 * arg1 is an IntObject giving the index, arg2 is the new value to write
	 * into the tuple.
	 * </p>
	 * 
	 * <pre>
	 * update(t, i, new_val) is equivalent to
	 * (mk-TupleName (proj_0 t) ... new_val ... (proj_n t))
	 * </pre>
	 * 
	 * @param expr
	 *            a SARL expression of kind {@link SymbolicOperator#TUPLE_WRITE}
	 * @return the result of translating to Z3
	 */
	private FastList<String> translateTupleWrite(SymbolicExpression expr) {
		SymbolicExpression tupleExpression = (SymbolicExpression) expr
				.argument(0);
		SymbolicTupleType tupleType = (SymbolicTupleType) tupleExpression
				.type();
		FastList<String> t = translate(tupleExpression);
		int index = ((IntObject) expr.argument(1)).getInt();
		SymbolicExpression valueExpression = (SymbolicExpression) expr
				.argument(2);
		int tupleLength = ((SymbolicTupleType) expr.type()).sequence()
				.numTypes();
		FastList<String> result = new FastList<>(tupleConstructor(tupleType));

		for (int i = 0; i < tupleLength; i++) {
			result.add(" ");
			if (i == index) {
				result.append(translate(valueExpression));
			} else {
				result.addAll("(", tupleProjector(tupleType, i), " ");
				result.append(t.clone());
				result.add(")");
			}
		}
		result.add(")");
		return result;
	}

	/**
	 * Translates a multiple array-write (or array update) SARL symbolic
	 * expression to equivalent Z3 expression.
	 * 
	 * @param expr
	 *            a SARL expression of kind
	 *            {@link SymbolicOperator#DENSE_ARRAY_WRITE}
	 * @return the result of translating expr to Z3
	 */
	private FastList<String> translateDenseArrayWrite(SymbolicExpression expr) {
		FastList<String> result = pretranslateDenseArrayWrite(expr);

		result = bigArray(lengthOfArray(expr), result);
		return result;
	}

	/**
	 * <p>
	 * Translates a multiple tuple-write (or tuple update) SARL symbolic
	 * expression to equivalent Z3 expression.
	 * </p>
	 * 
	 * <p>
	 * Syntax: <code>(mk-T e0 e1 ...)</code>, where <code>e</code>i is
	 * <code>(proj_i tup)</code> if there is no i-th element in the sequence or
	 * the i-th element in the sequence is NULL, otherwise the i-th element of
	 * the sequence.
	 * </p>
	 * 
	 * @param expr
	 *            a SARL expression of kind
	 *            {@link SymbolicOperator#DENSE_TUPLE_WRITE}
	 * @return result of translating to Z3
	 */
	private FastList<String> translateDenseTupleWrite(SymbolicExpression expr) {
		SymbolicExpression tupleExpression = (SymbolicExpression) expr
				.argument(0);
		SymbolicTupleType tupleType = (SymbolicTupleType) tupleExpression
				.type();
		SymbolicSequence<?> values = (SymbolicSequence<?>) expr.argument(1);
		int numValues = values.size();
		FastList<String> origin = translate(tupleExpression);

		if (numValues == 0) {
			return origin;
		}

		FastList<String> result = new FastList<>("(",
				tupleConstructor(tupleType));

		result.append(translate(tupleExpression));
		for (int i = 0; i < numValues; i++) {
			SymbolicExpression value = values.get(i);

			result.add(" ");
			if (!value.isNull()) {
				result.append(translate(value));
			} else {
				result.addAll("(", tupleProjector(tupleType, i), " ");
				result.append(origin.clone());
				result.add(")");
			}
		}

		int tupleLength = tupleType.sequence().numTypes();

		for (int i = numValues; i < tupleLength; i++) {
			result.addAll("(", tupleProjector(tupleType, i), " ");
			result.append(origin.clone());
			result.add(")");
		}
		result.add(")");
		return result;
	}

	/**
	 * Translates SymbolicExpressions of the type "exists" and "forall" into the
	 * Z3 equivalent.
	 * 
	 * @param expr
	 *            a SARL "exists" or "forall" expression
	 * @return result of translating to Z3
	 */
	private FastList<String> translateQuantifier(SymbolicExpression expr) {
		// syntax: (forall ((x T)) expr)
		SymbolicOperator kind = expr.operator();
		SymbolicConstant boundVariable = (SymbolicConstant) expr.argument(0);
		BooleanExpression predicate = (BooleanExpression) expr.argument(1);
		FastList<String> result = new FastList<String>("(");

		boundVariableStack.push(boundVariable);
		switch (kind) {
		case FORALL:
			result.add("forall");
			break;
		case EXISTS:
			result.add("exists");
			break;
		default:
			throw new SARLInternalException("unreachable");
		}
		result.add(" ((");
		result.append(translateSymbolicConstant(boundVariable, true));
		result.add(" ");
		result.append(translateType(boundVariable.type()));
		result.add(")) ");
		result.append(translate(predicate));
		result.add(")");
		boundVariableStack.pop();
		return result;
	}

	/**
	 * Given two SARL symbolic expressions of compatible type, this returns the
	 * Z3 translation of the assertion that the two expressions are equal.
	 * Special handling is needed for arrays, to basically say:
	 * 
	 * <pre>
	 * lengths are equal and forall i: 0<=i<length -> expr1[i]=expr2[i].
	 * </pre>
	 * 
	 * @param expr1
	 *            a SARL symbolic expression
	 * @param expr2
	 *            a SARL symbolic expression of type compatible with that of
	 *            <code>expr1</code>
	 * @return result of translating into Z3 the assertion "expr1=expr2"
	 */
	private FastList<String> processEquality(SymbolicExpression expr1,
			SymbolicExpression expr2) {
		FastList<String> result;

		if (expr1.type().typeKind() == SymbolicTypeKind.ARRAY) {
			// lengths are equal and forall i (0<=i<length).a[i]=b[i].
			// syntax:
			// (and (= len1 len2)
			// (forall ((i Int)) (=> (and (<= 0 i) (< i len1)) <rec-call>)))
			FastList<String> extent1 = lengthOfArray(expr1);
			NumericSymbolicConstant index = newSarlAuxVar();
			String indexString = index.name().getString();
			SymbolicExpression read1 = universe.arrayRead(expr1, index);
			SymbolicExpression read2 = universe.arrayRead(expr2, index);

			result = new FastList<>("(and (= ");
			result.append(extent1.clone());
			result.add(" ");
			result.append(lengthOfArray(expr2));
			result.addAll(") (forall ((", indexString, " Int)) (=> (and (<= 0 ",
					indexString, ") (< ", indexString, " ");
			result.append(extent1);
			result.add(")) ");
			result.append(processEquality(read1, read2));
			result.add(")))");
		} else {
			result = new FastList<>("(= ");
			result.append(translate(expr1));
			result.add(" ");
			result.append(translate(expr2));
			result.add(")");
		}
		return result;
	}

	/**
	 * Translates a SymbolicExpression that represents a = b into the CVC
	 * equivalent.
	 * 
	 * @param expr
	 *            SARL symbolic expression with kind
	 *            {@link SymbolicOperator.EQUALS}
	 * @return the equivalent CVC
	 */
	private FastList<String> translateEquality(SymbolicExpression expr) {
		SymbolicExpression leftExpression = (SymbolicExpression) expr
				.argument(0);
		SymbolicExpression rightExpression = (SymbolicExpression) expr
				.argument(1);
		FastList<String> result = processEquality(leftExpression,
				rightExpression);

		return result;
	}

	/**
	 * <p>
	 * Translates a union-extract expression. The result has the form
	 * 
	 * <pre>
	 * (UT_extract_i y)
	 * </pre>
	 * 
	 * where <code>UT</code> is the name of the union type, <code>y</code> is
	 * the argument belonging to the union type, and <code>i</code> is the index
	 * argument.
	 * </p>
	 * 
	 * <p>
	 * UNION_EXTRACT: 2 arguments: arg0 is an IntObject giving the index of a
	 * member type of a union type; arg1 is a symbolic expression whose type is
	 * the union type. The resulting expression has type the specified member
	 * type. This essentially pulls the expression out of the union and casts it
	 * to the member type. If arg1 does not belong to the member type (as
	 * determined by a UNION_TEST expression), the value of this expression is
	 * undefined.
	 * </p>
	 * 
	 * <p>
	 * Note that the union type will be declared as follows:
	 * 
	 * <pre>
	 * (declare-datatypes () ((UT
	 *     (UT-inject_0 (UT-extract_0 T0))
	 *     (UT-inject_1 (UT-extract_1 T1))
	 *     ...
	 *  )))
	 * </pre>
	 * 
	 * Usage:
	 * 
	 * <pre>
	 *   (UT-inject_i x)
	 *   (UT-extract_i y)
	 * </pre>
	 * 
	 * </p>
	 * 
	 * @param expr
	 *            a "union extract" expression, kind
	 *            {@link SymbolicOperator#UNION_EXTRACT}
	 * @return result of translating to Z3
	 */
	private FastList<String> translateUnionExtract(SymbolicExpression expr) {
		int index = ((IntObject) expr.argument(0)).getInt();
		SymbolicExpression arg = (SymbolicExpression) expr.argument(1);
		SymbolicUnionType unionType = (SymbolicUnionType) arg.type();
		FastList<String> result = new FastList<>("(",
				unionSelector(unionType, index), " ");

		result.append(translate(arg));
		result.add(")");
		return result;
	}

	/**
	 * <p>
	 * Translates a union-inject expression. The result has the form
	 * 
	 * <pre>
	 * (UT-inject_i x)
	 * </pre>
	 * 
	 * where <code>UT</code> is the name of the union type, <code>x</code> is
	 * the argument belonging to the member type, and <code>i</code> is the
	 * index argument.
	 * </p>
	 * 
	 * <p>
	 * UNION_INJECT: injects an element of a member type into a union type that
	 * includes that member type. 2 arguments: arg0 is an IntObject giving the
	 * index of the member type of the union type; arg1 is a symbolic expression
	 * whose type is the member type. The union type itself is the type of the
	 * UNION_INJECT expression.
	 * </p>
	 * 
	 * @param expr
	 *            a "union inject" expression
	 * @return the Z3 translation of that expression
	 */
	private FastList<String> translateUnionInject(SymbolicExpression expr) {
		int index = ((IntObject) expr.argument(0)).getInt();
		SymbolicExpression arg = (SymbolicExpression) expr.argument(1);
		SymbolicUnionType unionType = (SymbolicUnionType) expr.type();
		FastList<String> result = new FastList<>("(",
				unionConstructor(unionType, index), " ");

		result.append(translate(arg));
		result.add(")");
		return result;
	}

	/**
	 * <p>
	 * Translates a union-test expression. The result has the form
	 * 
	 * <pre>
	 * (is-UT-inject_i y)
	 * </pre>
	 * 
	 * where <code>UT</code> is the name of the union type, <code>y</code> is
	 * the argument belonging to the union type, and <code>i</code> is the index
	 * argument.
	 * </p>
	 * 
	 * <p>
	 * UNION_TEST: 2 arguments: arg0 is an IntObject giving the index of a
	 * member type of the union type; arg1 is a symbolic expression whose type
	 * is the union type. This is a boolean-valued expression whose value is
	 * true iff arg1 belongs to the specified member type of the union type.
	 * </p>
	 * 
	 * @param expr
	 *            a "union test" expression
	 * @return the Z3 translation of that expression
	 */
	private FastList<String> translateUnionTest(SymbolicExpression expr) {
		int index = ((IntObject) expr.argument(0)).getInt();
		SymbolicExpression arg = (SymbolicExpression) expr.argument(1);
		SymbolicUnionType unionType = (SymbolicUnionType) arg.type();
		FastList<String> result = new FastList<>("(",
				unionTester(unionType, index), " ");

		result.append(translate(arg));
		result.add(")");
		return result;
	}

	private FastList<String> translateCast(SymbolicExpression expression) {
		SymbolicExpression argument = (SymbolicExpression) expression
				.argument(0);
		SymbolicType originalType = argument.type();
		SymbolicType newType = expression.type();

		if (originalType.equals(newType)
				|| (originalType.isInteger() && newType.isReal()))
			return translate(argument);

		Pair<SymbolicType, SymbolicType> key = new Pair<>(originalType,
				newType);
		String castFunction = castMap.get(key);

		if (castFunction == null) {
			castFunction = "cast" + castMap.size();
			z3Declarations.append(functionDeclaration(castFunction, universe
					.functionType(Arrays.asList(originalType), newType)));
			castMap.put(key, castFunction);
		}

		FastList<String> result = new FastList<>("(", castFunction, " ");

		result.append(translate(argument));
		result.add(")");
		return result;
	}

	private FastList<String> translateApply(SymbolicExpression expression) {
		SymbolicExpression function = (SymbolicExpression) expression
				.argument(0);
		SymbolicSequence<?> arguments = (SymbolicSequence<?>) expression
				.argument(1);
		FastList<String> result = new FastList<String>("(");

		result.append(translate(function));
		for (SymbolicExpression arg : arguments) {
			result.add(" ");
			result.append(translate(arg));
		}
		result.add(")");
		return result;
	}

	private FastList<String> translateNegative(SymbolicExpression expression) {
		FastList<String> result = new FastList<>("(- ");

		result.append(translate((SymbolicExpression) expression.argument(0)));
		result.add(")");
		return result;
	}

	private FastList<String> translateNEQ(SymbolicExpression expression) {
		FastList<String> result = new FastList<>("(not ");

		result.append(
				processEquality((SymbolicExpression) expression.argument(0),
						(SymbolicExpression) expression.argument(1)));
		result.add(")");
		return result;
	}

	private FastList<String> translateNot(SymbolicExpression expression) {
		FastList<String> result = new FastList<>("(not ");

		result.append(translate((SymbolicExpression) expression.argument(0)));
		result.add(")");
		return result;
	}

	private FastList<String> translatePower(SymbolicExpression expression) {
		// apparently "^" but not documented
		SymbolicObject exponent = expression.argument(1);
		FastList<String> result = new FastList<>(), base, exp;

		base = translate((SymbolicExpression) expression.argument(0));
		if (exponent instanceof NumberObject) {
			NumberObject expNumObj = (NumberObject) exponent;

			if (SARLConstants.z3PowerToMultiply && expNumObj.isInteger()
					&& expNumObj.signum() > 0) {
				// optimize: avoid using POWER operator if exponent is concrete
				// positive integer:
				int expIntNum = ((IntegerNumber) ((NumberObject) exponent)
						.getNumber()).intValue();

				result.add("(* ");
				for (int i = 0; i < expIntNum; i++) {
					result.append(base.clone());
					result.add(" ");
				}
				result.add(")");
				return result;
			}
			exp = new FastList<>(expNumObj.toString());
		} else
			exp = translate((SymbolicExpression) exponent);

		result.add("(^ ");
		result.append(base);
		result.add(" ");
		result.append(exp);
		result.add(")");
		return result;
	}

	private FastList<String> translateCond(SymbolicExpression expression) {
		// syntax: (ite b x y)
		FastList<String> result = new FastList<>("(ite ");

		result.append(translate((SymbolicExpression) expression.argument(0)));
		result.add(" ");
		result.append(translate((SymbolicExpression) expression.argument(1)));
		result.add(" ");
		result.append(translate((SymbolicExpression) expression.argument(2)));
		result.add(")");
		return result;
	}

	private FastList<String> translateKeySet(String operator,
			String defaultValue, SymbolicExpression expression) {
		int size = expression.numArguments();

		if (size == 0) {
			return new FastList<>(defaultValue);
		} else if (size == 1) {
			return translate((SymbolicExpression) expression.argument(0));
		} else {
			FastList<String> result = new FastList<>("(", operator);

			for (int i = 0; i < size; i++) {
				SymbolicExpression term = (SymbolicExpression) expression
						.argument(i);

				result.add(" ");
				result.append(translate(term));
			}
			result.add(")");
			return result;
		}
	}

	private FastList<String> translateBinary(String operator,
			SymbolicExpression arg0, SymbolicExpression arg1) {
		FastList<String> result = new FastList<>("(", operator, " ");

		result.append(translate(arg0));
		result.addAll(" ");
		result.append(translate(arg1));
		result.add(")");
		return result;
	}

	/**
	 * Translates a SARL symbolic expression to the language of CVC.
	 * 
	 * @param expression
	 *            a non-null SymbolicExpression
	 * @return translation to CVC as a fast list of strings
	 */
	private FastList<String> translateWork(SymbolicExpression expression)
			throws TheoremProverException {
		SymbolicOperator operator = expression.operator();
		FastList<String> result;

		switch (operator) {
		case ADD:
			result = translateKeySet("+", "0", expression);
			break;
		case AND:
			result = translateKeySet("and", "true", expression);
			break;
		case APPLY:
			result = translateApply(expression);
			break;
		case ARRAY:
			result = translateConcreteArray(expression);
			break;
		case ARRAY_LAMBDA:
			// throw new TheoremProverException(
			// "Z3 does not handle array lambdas");
			result = new FastList<>(
					newZ3AuxVar(translateType(expression.type())));
			break;
		case ARRAY_READ:
			result = translateArrayRead(expression);
			break;
		case ARRAY_WRITE:
			result = translateArrayWrite(expression);
			break;
		case BIT_AND:
			result = translateBitBinary("bvand",
					(SymbolicExpression) expression.argument(0),
					(SymbolicExpression) expression.argument(1));
			break;
		case BIT_NOT:
			result = translateBitUnary("bvnot",
					(SymbolicExpression) expression.argument(0));
			break;
		case BIT_OR:
			result = translateBitBinary("bvor",
					(SymbolicExpression) expression.argument(0),
					(SymbolicExpression) expression.argument(1));
			break;
		case BIT_XOR:
			result = translateBitBinary("bvxor",
					(SymbolicExpression) expression.argument(0),
					(SymbolicExpression) expression.argument(1));
			break;
		case BIT_SHIFT_LEFT:
			result = translateBitBinary("bvshl",
					(SymbolicExpression) expression.argument(0),
					(SymbolicExpression) expression.argument(1));
			break;
		case BIT_SHIFT_RIGHT:
			result = translateBitBinary("bvlshr",
					(SymbolicExpression) expression.argument(0),
					(SymbolicExpression) expression.argument(1));
			break;
		case CAST:
			result = translateCast(expression);
			break;
		case CONCRETE:
			result = translateConcrete(expression);
			break;
		case COND:
			result = translateCond(expression);
			break;
		case DENSE_ARRAY_WRITE:
			result = translateDenseArrayWrite(expression);
			break;
		case DENSE_TUPLE_WRITE:
			result = translateDenseTupleWrite(expression);
			break;
		case DIVIDE: // real division
			result = translateBinary("/",
					(SymbolicExpression) expression.argument(0),
					(SymbolicExpression) expression.argument(1));
			break;
		case EQUALS:
			result = translateEquality(expression);
			break;
		case EXISTS:
		case FORALL:
			result = translateQuantifier(expression);
			break;
		case INT_DIVIDE:
			result = translateBinary("div",
					(SymbolicExpression) expression.argument(0),
					(SymbolicExpression) expression.argument(1));
			break;
		case LENGTH:
			result = lengthOfArray((SymbolicExpression) expression.argument(0));
			break;
		case LESS_THAN:
			result = translateBinary("<",
					(SymbolicExpression) expression.argument(0),
					(SymbolicExpression) expression.argument(1));
			break;
		case LESS_THAN_EQUALS:
			result = translateBinary("<=",
					(SymbolicExpression) expression.argument(0),
					(SymbolicExpression) expression.argument(1));
			break;
		case MODULO:
			result = translateBinary("mod",
					(SymbolicExpression) expression.argument(0),
					(SymbolicExpression) expression.argument(1));
			break;
		case MULTIPLY:
			result = translateKeySet("*", "1", expression);
			break;
		case NEGATIVE:
			result = translateNegative(expression);
			break;
		case NEQ:
			result = translateNEQ(expression);
			break;
		case NOT:
			result = translateNot(expression);
			break;
		case OR:
			result = translateKeySet("or", "false", expression);
			break;
		case POWER:
			result = translatePower(expression);
			break;
		case SUBTRACT:
			result = translateBinary("-",
					(SymbolicExpression) expression.argument(0),
					(SymbolicExpression) expression.argument(1));
			break;
		case SYMBOLIC_CONSTANT:
			result = translateSymbolicConstant((SymbolicConstant) expression,
					false);
			break;
		case TUPLE:
			result = translateConcreteTuple(expression);
			break;
		case TUPLE_READ:
			result = translateTupleRead(expression);
			break;
		case TUPLE_WRITE:
			result = translateTupleWrite(expression);
			break;
		case UNION_EXTRACT:
			result = translateUnionExtract(expression);
			break;
		case UNION_INJECT:
			result = translateUnionInject(expression);
			break;
		case UNION_TEST:
			result = translateUnionTest(expression);
			break;
		case LAMBDA:
			result = translateLambda(expression);
			break;
		case NULL:
			result = null;
			break;
		case DERIV:
		case DIFFERENTIABLE: {
			// just create fresh symbolic constants
			FastList<String> z3Type = translateType(expression.type());
			String name = newZ3AuxVar(z3Type.clone());

			// the call to newZ3AuxVar added the declaration
			result = new FastList<String>(name);
			break;
		}
		default:
			throw new SARLInternalException(
					"unreachable: unknown operator: " + operator);
		}
		return result;
	}

	private FastList<String> translateBitUnary(String operator,
			SymbolicExpression arg0) {
		FastList<String> result = new FastList<>(
				"((_ bv2int " + BITLEN_INT + ") (", operator);

		result.add(" ((_ int2bv " + BITLEN_INT + ") ");
		result.append(translate(arg0));
		result.add(")))");
		return result;
	}

	private FastList<String> translateBitBinary(String operator,
			SymbolicExpression arg0, SymbolicExpression arg1) {
		FastList<String> result = new FastList<>(
				"((_ bv2int " + BITLEN_INT + ") (", operator);

		result.add(" ((_ int2bv " + BITLEN_INT + ") ");
		result.append(translate(arg0));
		result.add(") ((_ int2bv " + BITLEN_INT + ") ");
		result.append(translate(arg1));
		result.add(")))");
		return result;
	}

	private FastList<String> translateType(SymbolicType type) {
		FastList<String> result = typeMap.get(type);

		if (result != null)
			return result.clone();

		SymbolicTypeKind kind = type.typeKind();

		switch (kind) {
		case BOOLEAN:
			result = new FastList<>("Bool");
			break;
		case INTEGER:
		case CHAR:
			result = new FastList<>("Int");
			break;
		case REAL:
			result = new FastList<>("Real");
			break;
		case ARRAY: {
			SymbolicArrayType arrayType = (SymbolicArrayType) type;

			if (SARLConstants.z3UseBigArray) {
				requireBigArray();
				result = new FastList<>("(BigArray ");
				result.append(translateType(arrayType.elementType()));
				result.add(")");
			} else {
				result = new FastList<>("(Array Int ");
				result.append(translateType(arrayType.elementType()));
				result.add(")");
			}
			break;
		}
		case TUPLE: {
			SymbolicTupleType tupleType = (SymbolicTupleType) type;
			SymbolicTypeSequence sequence = tupleType.sequence();
			int numTypes = sequence.numTypes();
			String typeName = tupleTypeName(tupleType);

			// before doing anything translate the member types,
			// because these could modify z3Declarations.
			// check if this happens in CVC translator?
			for (SymbolicType memberType : sequence)
				translateType(memberType);

			z3Declarations.add("(declare-datatypes () ((");
			z3Declarations.addAll(typeName, " (", tupleConstructor(tupleType));

			for (int i = 0; i < numTypes; i++) {
				SymbolicType memberType = sequence.getType(i);

				z3Declarations.addAll(" (", tupleProjector(tupleType, i), " ");
				z3Declarations.append(translateType(memberType));
				z3Declarations.add(")");
			}
			z3Declarations.add("))))\n");
			result = new FastList<>(typeName);
			break;
		}
		case FUNCTION: {
			throw new TheoremProverException(
					"Z3 does not have a function type");
		}
		case UNION: {
			/**
			 * <pre>
			 * 			 (declare-datatypes () ((UT
			 * 			     (UT-inject_0 (UT-extract_0 T0))
			 * 			     (UT-inject_1 (UT-extract_1 T1))
			 * 			     ...
			 * 			  )))
			 * </pre>
			 */
			SymbolicUnionType unionType = (SymbolicUnionType) type;
			String typeName = unionTypeName(unionType);
			SymbolicTypeSequence sequence = unionType.sequence();
			int n = sequence.numTypes();

			// before doing anything translate the member types,
			// because these could modify z3Declarations.
			// check if this happens in CVC translator?
			for (SymbolicType memberType : sequence)
				translateType(memberType);

			z3Declarations.addAll("(declare-datatypes () ((", typeName);
			for (int i = 0; i < n; i++) {
				SymbolicType memberType = sequence.getType(i);

				z3Declarations.addAll("\n    (", unionConstructor(unionType, i),
						" (", unionSelector(unionType, i), " ");
				z3Declarations.append(translateType(memberType));
				z3Declarations.add("))");
			}
			z3Declarations.add("\n)))\n");
			result = new FastList<>(typeName);
			break;
		}
		case UNINTERPRETED: {
			SymbolicUninterpretedType uninterpretedType = (SymbolicUninterpretedType) type;
			String typeName = uninterpretedTypeName(uninterpretedType);
			String consName = uninterpretedTypeConstructor(uninterpretedType);
			String typeDef = "() ((" + typeName + " (" + consName
					+ " (Selector-" + uninterpretedType.name() + " Int))))";

			z3Declarations.addAll("(declare-datatypes ", typeDef, ")\n");
			result = new FastList<>(typeName);
			break;
		}
		default:
			throw new SARLInternalException("Unknown SARL type: " + type);
		}
		typeMap.put(type, result);
		return result.clone();
	}

	private FastList<String> translate(SymbolicExpression expression)
			throws TheoremProverException {
		FastList<String> result = expressionMap.get(expression);

		if (result == null) {
			result = translateWork(expression);
			expressionMap.put(expression, result);
			if (useCompressedName(expression)) {
				// in compressed translation mode:
				result = translateExpression2binding(expression, result);
			}
		} else if (useCompressedName(expression)) {
			// expression has been translated but has no alias (when the context
			// translator is reused for translating predicate):
			result = subExpressionsBindingNames.get(expression);
			if (result == null)
				result = translateExpression2binding(expression,
						expressionMap.get(expression));
		}
		return result.clone();
	}

	private boolean useCompressedName(SymbolicExpression expression) {
		return subExpressionsBindingNames != null
				&& expression.size() > SINGLE_EXPR_SIZE_THRESHOLD
				&& boundVariableStack.isEmpty() && enableCompression;
	}

	/**
	 * For a translated sub-expression, creating an alias for it. The aliasing
	 * is implemented using <code>(let binding term)</code>.
	 */
	private FastList<String> translateExpression2binding(
			SymbolicExpression expression, FastList<String> translation) {
		// in compressed translation mode:
		FastList<String> tmpVarName = new FastList<>("t" + z3AuxVarCount++);
		FastList<String> binding = letTempVarRepresentExpression(
				tmpVarName.clone(), translation.clone());

		subExpressionBindings.add(binding);
		subExpressionsBindingNames.put(expression, tmpVarName.clone());
		return tmpVarName;
	}

	/**
	 * Add a alias binding for the sub-expression: <code>(symbol term)</code>
	 */
	private FastList<String> letTempVarRepresentExpression(FastList<String> var,
			FastList<String> subExpr) {
		FastList<String> result = new FastList<String>();

		result.add("(");
		result.append(var);
		result.add(" ");
		result.append(subExpr);
		result.add(") ");
		return result;
	}

	// Exported methods...

	/**
	 * Returns the result of translating the symbolic expression specified at
	 * construction into the language of Z3. The result is returned as a
	 * {@link FastList}. The elements of that list are Strings, which,
	 * concatenated, yield the translation result. In most cases you never want
	 * to convert the result to a single string. Rather, you should iterate over
	 * this list, printing each element to the appropriate output stream.
	 * 
	 * @return result of translation of the specified symbolic expression
	 */
	public FastList<String> getTranslation() {
		FastList<String> result = new FastList<>();
		FastList<String> suffixes = new FastList<>();

		if (subExpressionBindings != null) {
			// add compressed sub-expression bindings
			for (FastList<String> binding : subExpressionBindings) {
				result.add("(let (");
				result.append(binding.clone());
				result.add(") ");
				suffixes.add(")");
			}
			result.add(" ");
			result.append(z3Translation.clone());
			result.append(suffixes);
			return result;
		} else
			return z3Translation;
	}

	/**
	 * Returns the text of the declarations of the Z3 symbols that occur in the
	 * translated expression. Typically, the declarations are submitted to CVC
	 * first, followed by a query or assertion of the translated expression.
	 * 
	 * @return the declarations of the Z3 symbols
	 */
	public FastList<String> getDeclarations() {
		return z3Declarations;
	}

	/**
	 * The translation of logic function definitions is similar to
	 * {@link #translateLambda(SymbolicExpression)} but the translated functions
	 * are named after their corresponding logic functions.
	 * 
	 * @param logicFunction
	 *            a {@link LogicFunction} that will be translated into the
	 *            SMT2.0 function with body
	 */
	private void translateLogicFunction(
			ProverFunctionInterpretation logicFunction) {
		int argsNum = logicFunction.parameters.length;
		List<SymbolicType> inputTypes = new LinkedList<>();

		for (int i = 0; i < argsNum; i++)
			inputTypes.add(logicFunction.parameters[i].type());

		SymbolicFunctionType functionType = universe.functionType(inputTypes,
				logicFunction.definition.type());
		SymbolicExpression body = logicFunction.definition;

		String name = logicFunction.identifier;
		FastList<String> z3SymbolicConstants = new FastList<>();

		for (int i = 0; i < argsNum; i++) {
			SymbolicConstant inputVar = logicFunction.parameters[i];

			z3SymbolicConstants.add("(");
			z3SymbolicConstants
					.append(translateSymbolicConstant(inputVar, true));
			z3SymbolicConstants.add(" ");
			z3SymbolicConstants.append(translateType(inputVar.type()));
			z3SymbolicConstants.add(")");
		}
		FastList<String> z3OutputType = translateType(
				functionType.outputType());
		boolean oldCompressionOption = this.enableCompression;

		// no compression should be performed for the function body:
		enableCompression = false;

		FastList<String> z3Body = translate(body);

		enableCompression = oldCompressionOption;
		z3Declarations.addAll("(define-fun ", name, "(");
		z3Declarations.append(z3SymbolicConstants);
		z3Declarations.add(") ");
		z3Declarations.append(z3OutputType);
		z3Declarations.add(" ");
		z3Declarations.append(z3Body);
		z3Declarations.add(")\n");
		// add result into expression map so that translating calls to this
		// logic function will not create declarations again:
		this.expressionMap.put(logicFunction.function, new FastList<>(name));
	}
}
