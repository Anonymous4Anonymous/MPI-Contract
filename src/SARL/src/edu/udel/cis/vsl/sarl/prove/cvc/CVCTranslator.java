package edu.udel.cis.vsl.sarl.prove.cvc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import edu.udel.cis.vsl.sarl.IF.SARLException;
import edu.udel.cis.vsl.sarl.IF.SARLInternalException;
import edu.udel.cis.vsl.sarl.IF.TheoremProverException;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericSymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.IF.number.IntegerNumber;
import edu.udel.cis.vsl.sarl.IF.object.BooleanObject;
import edu.udel.cis.vsl.sarl.IF.object.CharObject;
import edu.udel.cis.vsl.sarl.IF.object.IntObject;
import edu.udel.cis.vsl.sarl.IF.object.NumberObject;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject.SymbolicObjectKind;
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
 * A CVCTranslator object is used to translate a single symbolic expression to
 * the language of CVC. The expression is specified and the translation takes
 * place at construction time. The result is available in two parts: the
 * resulting declarations and the resulting translated expression. These are
 * obtained by two getter methods: {@link #getDeclarations()} and
 * {@link #getTranslation()}, respectively.
 * </p>
 * 
 * <p>
 * In SARL, a complete array type T[N] is considered a sub-type of the
 * incomplete type T[]. Therefore an expression of type T[N] may be used
 * wherever one of type T[] is expected. For example, a function that takes
 * something of type T[] as an argument may be called with something of type
 * T[N]. The translation must take this into account by using the same
 * representation for expressions of both types. This translator represents any
 * such expression as an ordered pair (len,val), where len is an expression of
 * integer type representing the length of the array object, and val is an
 * expression of array type (with domain the set of integers) specifying the
 * values of the array.
 * </p>
 * 
 * <p>
 * CVC does not like tuples of length 1. Therefore all SARL tuples (x) of length
 * 1 are translated to just x, and the SARL tuple type [T] is translated to just
 * T.
 * </p>
 * 
 * <p>
 * Since CVC currently has a bad performance on dealing with division and
 * modulo. During the translation, division and modulo will be transformed into
 * a value and a set of constraints which are polynomials (Note that we only
 * consider cases when numerator is greater or equal to 0 and denominator is
 * greater than 0. The rest are dealt with in CIVL). eg. a/b will be transformed
 * into: value: q (q is the result of a/b) constraints: b * q + r = a && r >= 0
 * && r < b value and constraint are encapsulated into an object
 * {@link Translation}.
 * </p>
 * 
 * <p>
 * // Uninterpreted type:
 *
 * Translation of expressions of uninterpreted types: For an uninterpreted type
 * <code>t</code>, it will be translated to a type definition with a single key
 * of int type:<code>
 * DATATYPE
 * Unintpret_t = Cons_t(Selector_t : INT)
 * END;
 * </code>. Symbolic expressions of type t with
 * {@link SymbolicOperator#CONCRETE} operator will be translated using the
 * constructor <code>Cons_t</code> as <code>Const_t(key)</code>. Selector
 * <code>Select_key_t</code> will never be used for now.
 * </p>
 * 
 * @author xxxx
 *
 */
public class CVCTranslator {

	/**
	 * If <code>true</code>, integer division and modulus expressions will be
	 * translated by introducing auxiliary variables for quotient and remainder,
	 * and new constraints (one of which will be quadratic). This seems to work
	 * well for CVC3, which does not support the integer division or modulus
	 * operations.
	 */
	private boolean simplifyIntDivision;

	/**
	 * Currently, CVC4 does not support the type conversion between
	 * <code>BITVECTOR(N)</code> and <code>INT</code>. Thus, for expressions
	 * containing bitwise operations with symbolic arguments, CVCTranslater will
	 * first process the expression in normal approach, and then set
	 * <code>hasBitOp</code> as <code>true</code> iff there exists at least one
	 * bit-wise operation in the expression.<br>
	 * If <code>hasBitOp</code> is true, CVCTranslater will try to generated a
	 * BITVECTOR(N)-based query. If all involved operators can be used with the
	 * type of BITVECTOR(N), a BITVECTOR(N)-based query can be used by CVC4
	 * prover.<br>
	 * 12-20-2016, W.Wu
	 */
	@SuppressWarnings("unused")
	private boolean hasBitOp;

	/**
	 * If the given expression containing operations can NOT be converted to a
	 * corresponding bit-wise operation in CVC4 native language,
	 * <code>supporBit</code> will be <code>false</code> so that CVCTranslator
	 * will not convert the expression into a BITVECTOR(N)-based query.
	 */
	@SuppressWarnings("unused")
	private boolean supportBit;

	/**
	 * The bound of the length of bit-vector, which represents an unsigned
	 * integer.
	 */
	private int intLen;

	/**
	 * The symbolic universe used to create and manipulate SARL symbolic
	 * expressions.
	 */
	private PreUniverse universe;

	/**
	 * The number of auxiliary CVC variables created. These are the variables
	 * that do not correspond to any SARL variable but are needed for some
	 * reason to translate an expression. Includes both ordinary and bound CVC
	 * variables.
	 */
	private int cvcAuxVarCount;

	/**
	 * The number of auxiliary SARL variables created. These are used for
	 * integer index variables for expressing array equality.
	 */
	private int sarlAuxVarCount;

	/**
	 * Mapping of SARL symbolic expression to corresponding CVC expression. Used
	 * to cache the results of translation.
	 */
	private Map<SymbolicExpression, Translation> expressionMap;

	/**
	 * Mapping of SARL symbolic expression to corresponding BITVECTOR(N)-based
	 * CVC expression. Used to cache the results of translation.
	 */
	private Map<SymbolicExpression, Translation> expressionBVMap;

	/**
	 * Mapping of pairs (t1,t2) of SARL types to the uninterpreted function
	 * symbol which represents casting from t1 to t2. The function has type
	 * "function from translate(t1) to translate(t2)".
	 */
	private Map<Pair<SymbolicType, SymbolicType>, String> castMap;

	/**
	 * Map from SARL symbolic constants to corresponding CVC expressions.
	 * Entries are a subset of those of {@link #expressionMap}.
	 * 
	 * NOTE: currently, this is not being used for anything.
	 */
	private Map<SymbolicConstant, FastList<String>> variableMap;

	/**
	 * Map from SARL symbolic constants to corresponding CVC expressions.
	 * Entries are a subset of those of {@link #expressionMap}.
	 * 
	 * NOTE: currently, this is not being used for anything.
	 */
	private Map<SymbolicConstant, FastList<String>> variableBVMap;

	/**
	 * Mapping of SARL symbolic type to corresponding CVC type. Used to cache
	 * results of translation.
	 */
	private Map<SymbolicType, FastList<String>> typeMap;

	/**
	 * Integer division map. Given a pair (a,b) of expressions of integer type,
	 * This returns the pair of CVC variables (q,r) representing the quotient
	 * and remainder resulting from the integer division a div b.
	 */
	private Map<Pair<NumericExpression, NumericExpression>, Pair<FastList<String>, FastList<String>>> intDivMap;

	/**
	 * The declarations section resulting from the translation. This contains
	 * all the declarations of symbols used in the resulting CVC input.
	 */
	private FastList<String> cvcDeclarations;

	/**
	 * The expression which is the result of translating the given symbolic
	 * expression.
	 */
	private FastList<String> cvcTranslation;

	/**
	 * <p>
	 * A stack indicating the state of translating theories. A theory (e.g.
	 * {@link CVCPowerReal}) will be associated with an uninterpreted function
	 * whose name will not be normalized (see
	 * {@link #normalizeSymbolicConstantName(SymbolicConstant)}).
	 * </p>
	 * <p>
	 * The name of the uninterpreted function will be pushed to this stack
	 * whenever translating a call to it.
	 * </p>
	 */
	private Stack<String> translatingTheoryStack = new Stack<>();

	/**
	 * A set of names of theories, which have been declared (i.e. axioms have
	 * been imported)
	 */
	private Set<String> declaredTheories;

	/**
	 * Power theory for real exponent:
	 */
	CVCPowerReal cvcPowerReal = null;

	// Constructors...

	CVCTranslator(PreUniverse universe, SymbolicExpression theExpression,
			boolean simplifyIntDivision,
			ProverFunctionInterpretation[] logicFunctions)
			throws TheoremProverException {
		this.hasBitOp = false;
		this.supportBit = true;
		this.simplifyIntDivision = simplifyIntDivision;
		this.universe = universe;
		this.intLen = universe.getIntegerLengthBound();
		this.cvcAuxVarCount = 0;
		this.sarlAuxVarCount = 0;
		this.expressionMap = new HashMap<>();
		this.expressionBVMap = new HashMap<>();
		this.castMap = new HashMap<>();
		this.variableMap = new HashMap<>();
		this.variableBVMap = new HashMap<>();
		this.typeMap = new HashMap<>();
		if (simplifyIntDivision)
			this.intDivMap = new HashMap<>();
		else
			this.intDivMap = null;
		this.cvcDeclarations = new FastList<>();
		this.declaredTheories = new HashSet<>();
		// translate logic function definitions:
		for (ProverFunctionInterpretation logicFunction : logicFunctions)
			translateLogicFunction(logicFunction);
		this.cvcTranslation = translate(theExpression).getResult();
		// if (hasBitOp && supportBit)
		// this.cvcTranslation = translate_BV(theExpression).getResult();
	}

	CVCTranslator(CVCTranslator startingContext,
			SymbolicExpression theExpression) throws TheoremProverException {
		this.hasBitOp = false;
		this.supportBit = true;
		this.simplifyIntDivision = startingContext.simplifyIntDivision;
		this.universe = startingContext.universe;
		this.intLen = universe.getIntegerLengthBound();
		this.cvcAuxVarCount = startingContext.cvcAuxVarCount;
		this.sarlAuxVarCount = startingContext.sarlAuxVarCount;
		this.expressionMap = new HashMap<>(startingContext.expressionMap);
		this.expressionBVMap = new HashMap<>();
		this.castMap = new HashMap<>(startingContext.castMap);
		this.variableMap = new HashMap<>(startingContext.variableMap);
		this.variableBVMap = new HashMap<>(startingContext.variableBVMap);
		this.typeMap = new HashMap<>(startingContext.typeMap);
		if (simplifyIntDivision)
			this.intDivMap = new HashMap<>(startingContext.intDivMap);
		else
			intDivMap = null;
		this.cvcDeclarations = new FastList<>();
		this.declaredTheories = new HashSet<>(startingContext.declaredTheories);
		this.cvcTranslation = translate(theExpression).getResult();
		// if (hasBitOp && supportBit)
		// this.cvcTranslation = translate_BV(theExpression).getResult();
	}

	// Private methods...

	/**
	 * <p>
	 * Normalize all symbolic constant names with a prefix "sc_" so that they
	 * will distinguish with CVC4 provided symbolic constants, e.g. "power"
	 * function.
	 * </p>
	 * 
	 * @param sc
	 *            a symbolic consant
	 * @return the normalized name for the pass-in symbolic constant
	 */
	private String normalizeSymbolicConstantName(SymbolicConstant sc) {
		String name = sc.name().getString();

		if (translatingTheoryStack.contains(name))
			return name;
		else
			return "sc_" + name;
	}

	/**
	 * Computes the name of the index-th selector function into a union type.
	 * This is the function that taken an element of the union and returns an
	 * element of the index-th member type.
	 * 
	 * @param unionType
	 *            a union type
	 * @param index
	 *            integer in [0,n), where n is the number of member types of the
	 *            union type
	 * @return the name of the index-th selector function
	 */
	private String selector(SymbolicUnionType unionType, int index) {
		return unionType.name().toString() + "_extract_" + index;
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
	private String constructor(SymbolicUnionType unionType, int index) {
		return unionType.name().toString() + "_inject_" + index;
	}

	/**
	 * Creates a new CVC (ordinary) variable of given type with unique name;
	 * increments {@link #cvcAuxVarCount}.
	 * 
	 * TODO: type is not used. Figure out why and correct this method.
	 * 
	 * @param type
	 *            a CVC type; it is consumed, so cannot be used after invoking
	 *            this method
	 * @return the new CVC variable
	 */
	private String newCvcAuxVar(FastList<String> type) {
		String name = "t" + cvcAuxVarCount;

		cvcAuxVarCount++;
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
	 *            CVC expression yielding length of array; it is consumed (so
	 *            cannot be used after invoking this method)
	 * @param value
	 *            CVC expression of type "array-of-T"; it is consumed (so cannot
	 *            be used after invoking this method)
	 * @return ordered pair (tuple), consisting of length and value
	 */
	private FastList<String> bigArray(FastList<String> length,
			FastList<String> value) {
		FastList<String> result = new FastList<String>();

		result.addAll("(");
		result.append(length);
		result.add(",");
		result.append(value);
		result.add(")");
		return result;
	}

	/**
	 * @return The name of an uninterpreted type
	 */
	private String uninterpretedTypeName(SymbolicUninterpretedType unitType) {
		return "Unintpre_" + unitType.name();
	}

	/**
	 * @return The name of the sole constructor of an uninterpreted type
	 */
	private String uninterpretedTypeConstructor(
			SymbolicUninterpretedType unitType) {
		return "Cons_" + unitType.name();
	}

	/**
	 * <p>
	 * Given a SARL expression of array type, this method computes the CVC
	 * representation of the length of that array. This is a CVC expression of
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
	 * @return translation into CVC of length of that array
	 */
	private Translation lengthOfArray(SymbolicExpression array) {
		SymbolicArrayType type = (SymbolicArrayType) array.type();

		// imagine translating "length(a)" for a symbolic constant a.
		// this calls lengthOfArray(a). This calls translate(a).
		// Since a is a symbolic constant, this yields a CVC symbolic
		// constant A. The result returned is "(A).0".

		if (type instanceof SymbolicCompleteArrayType)
			return translate(((SymbolicCompleteArrayType) type).extent());
		// there are three kinds of array expressions for which translation
		// results in a literal ordered pair [int,array]: SEQUENCE,
		// ARRAY_WRITE, DENSE_ARRAY_WRITE. A concrete (SEQUENCE) array always
		// has complete type.
		switch (array.operator()) {
		case ARRAY:
			throw new SARLInternalException(
					"Unreachable because the array would have a complete array type");
		case ARRAY_WRITE:
		case DENSE_ARRAY_WRITE:
			return lengthOfArray((SymbolicExpression) array.argument(0));
		default:
			FastList<String> result = new FastList<>("(");

			result.append(translate(array).getResult());
			result.add(").0");
			Translation res = new Translation(result);
			return res;
		}
	}

	/**
	 * Merge the auxiliary variables and auxiliary constraints of those
	 * translations in the list(translations) into the translation(t)
	 * 
	 * @param t
	 * @param translations
	 */
	private void combineTranslations(Translation t,
			List<Translation> translations) {
		t.setIsDivOrModulo(true);
		int size = translations.size();

		for (int i = 0; i < size; i++) {
			Translation tempTranslation = translations.get(i);

			t.getAuxVars().addAll(tempTranslation.getAuxVars());
			FastList<String> auxConstraints = t.getAuxConstraints();

			auxConstraints.add("AND (");
			auxConstraints.append(tempTranslation.getAuxConstraints().clone());
			auxConstraints.add(")");
		}
	}

	private Translation pretranslateConcreteArray(SymbolicExpression array) {
		Translation res;
		SymbolicCompleteArrayType arrayType = (SymbolicCompleteArrayType) array
				.type();
		SymbolicType elementType = arrayType.elementType();
		NumericExpression extentExpression = arrayType.extent();
		IntegerNumber extentNumber = (IntegerNumber) universe
				.extractNumber(extentExpression);
		int size = array.numArguments();
		FastList<String> cvcArrayType = new FastList<>("ARRAY INT OF (");

		cvcArrayType.append(translateType(elementType));
		cvcArrayType.add(")");
		assert extentNumber != null && extentNumber.intValue() == size;

		FastList<String> result = new FastList<>(
				newCvcAuxVar(cvcArrayType.clone()));

		cvcDeclarations.addAll(result.toString(), ":    ");
		cvcDeclarations.append(cvcArrayType.clone());
		cvcDeclarations.add(";\n");

		if (size > 0) {
			result.add(" WITH [0] := (");
			result.append(translate((SymbolicExpression) array.argument(0))
					.getResult());
			result.add(")");
			for (int i = 1; i < size; i++) {
				result.addAll(", [", Integer.toString(i), "] := (");
				result.append(translate((SymbolicExpression) array.argument(i))
						.getResult());
				result.add(")");
			}
		}
		res = new Translation(result);
		return res;
	}

	private Translation pretranslateArrayWrite(SymbolicExpression arrayWrite) {
		Translation res;
		Translation tempTranslation;
		Boolean involveDivOrModulo = false;
		List<Translation> translations = new ArrayList<Translation>();
		// syntax: a WITH [10] := 2/3
		SymbolicExpression arrayExpression = (SymbolicExpression) arrayWrite
				.argument(0);
		NumericExpression indexExpression = (NumericExpression) arrayWrite
				.argument(1);
		SymbolicExpression valueExpression = (SymbolicExpression) arrayWrite
				.argument(2);
		FastList<String> result = new FastList<>("(");

		result.append(valueOfArray(arrayExpression).getResult());
		result.add(") WITH [");
		tempTranslation = translate(indexExpression);
		if (tempTranslation.getIsDivOrModulo()) {
			translations.add(tempTranslation);
			involveDivOrModulo = true;
		}
		result.append(tempTranslation.getResult());
		result.add("] := (");
		tempTranslation = translate(valueExpression);
		if (tempTranslation.getIsDivOrModulo()) {
			translations.add(tempTranslation);
			involveDivOrModulo = true;
		}
		result.append(translate(valueExpression).getResult());
		result.add(")");
		res = new Translation(result);
		if (involveDivOrModulo) {
			combineTranslations(res, translations);
		}
		return res;
	}

	private Translation pretranslateDenseArrayWrite(
			SymbolicExpression denseArrayWrite) {
		Translation res;
		List<Translation> translations = new ArrayList<Translation>();
		Translation tempTranslation;
		Boolean involveDivOrModulo = false;
		// syntax: a WITH [10] := 2/3, [42] := 3/2;
		SymbolicExpression arrayExpression = (SymbolicExpression) denseArrayWrite
				.argument(0);
		SymbolicSequence<?> elements = (SymbolicSequence<?>) denseArrayWrite
				.argument(1);
		int n = elements.size();
		FastList<String> result = new FastList<>("(");
		boolean first = true;

		result.append(valueOfArray(arrayExpression).getResult());
		result.add(")");
		for (int i = 0; i < n; i++) {
			SymbolicExpression element = elements.get(i);

			if (!element.isNull()) {
				if (first) {
					result.add(" WITH ");
					first = false;
				} else {
					result.add(", ");
				}
				result.addAll("[", Integer.toString(i), "] := (");
				tempTranslation = translate(element);
				if (tempTranslation.getIsDivOrModulo()) {
					translations.add(tempTranslation);
					involveDivOrModulo = true;

				}
				result.append(tempTranslation.getResult());
				result.add(")");
			}
		}
		res = new Translation(result);
		if (involveDivOrModulo) {
			combineTranslations(res, translations);
		}
		return res;
	}

	/**
	 * Given a SARL expression of array type, this method computes the CVC
	 * representation of array type corresponding to that array. The result will
	 * be a CVC expression of type array-of-T, where T is the element type.
	 * 
	 * @param array
	 * @return
	 */
	private Translation valueOfArray(SymbolicExpression array) {
		// the idea is to catch any expression which would be translated
		// as an explicit ordered pair [len,val] and return just the val.
		// for expressions that are not translated to an explicit
		// ordered pair, just append ".1" to get the array value component.
		Translation res;
		List<Translation> translations = new ArrayList<Translation>();
		Translation tempTranslation;
		Boolean involveDivOrModulo = false;

		switch (array.operator()) {
		case ARRAY:
			return pretranslateConcreteArray(array);
		case ARRAY_WRITE:
			return pretranslateArrayWrite(array);
		case DENSE_ARRAY_WRITE:
			return pretranslateDenseArrayWrite(array);
		default: {
			FastList<String> result = new FastList<>("(");
			tempTranslation = translate(array);
			if (tempTranslation.getIsDivOrModulo()) {
				translations.add(tempTranslation);
				involveDivOrModulo = true;

			}
			result.append(tempTranslation.getResult());
			result.add(").1");
			res = new Translation(result);
			if (involveDivOrModulo) {
				combineTranslations(res, translations);
			}
			return res;
		}
		}
	}

	/**
	 * Translates a concrete SARL array into language of CVC.
	 * 
	 * @param arrayType
	 *            a SARL complete array type
	 * @param elements
	 *            a sequence of elements whose types are all the element type of
	 *            the arrayType
	 * @return CVC translation of the concrete array
	 */
	private Translation translateConcreteArray(SymbolicExpression array) {
		Translation res = pretranslateConcreteArray(array);

		int size = array.numArguments();
		FastList<String> r = bigArray(new FastList<>(Integer.toString(size)),
				res.getResult());
		res = new Translation(r);
		return res;
	}

	private Translation translateConcreteTuple(SymbolicExpression tuple) {
		Translation res;
		// syntax:(x,y,z)
		FastList<String> result;
		int n = tuple.numArguments();

		if (n == 1) { // no tuples of length 1 in CVC
			result = translate((SymbolicExpression) tuple.argument(0))
					.getResult();
		} else {
			result = new FastList<String>("(");
			if (n > 0) { // possible to have tuple with 0 components
				result.append(translate((SymbolicExpression) tuple.argument(0))
						.getResult());
				for (int i = 1; i < n; i++) {
					result.add(",");
					result.append(
							translate((SymbolicExpression) tuple.argument(i))
									.getResult());
				}
			}
			result.add(")");
		}
		res = new Translation(result);
		return res;
	}

	/**
	 * Translates any concrete SymbolicExpression with concrete type to
	 * equivalent CVC Expr using the ExprManager.
	 * 
	 * @param expr
	 * @return the CVC equivalent Expr
	 */
	private Translation translateConcrete(SymbolicExpression expr) {
		Translation translation;
		SymbolicType type = expr.type();
		SymbolicTypeKind kind = type.typeKind();
		SymbolicObject object = expr.argument(0);
		FastList<String> result;
		switch (kind) {
		case BOOLEAN:
			result = new FastList<>(
					((BooleanObject) object).getBoolean() ? "TRUE" : "FALSE");
			break;
		case CHAR:
			result = new FastList<>(
					Integer.toString((int) ((CharObject) object).getChar()));
			break;
		case INTEGER:
		case REAL:
			result = new FastList<>(object.toString());
			break;
		case UNINTERPRETED: {
			SymbolicUninterpretedType unintType = (SymbolicUninterpretedType) expr
					.type();
			String constructor = this.uninterpretedTypeConstructor(unintType);
			int key = unintType.soleSelector().apply(expr).getInt();

			translateType(unintType);
			result = new FastList<>(constructor, "(", String.valueOf(key), ")");
			break;
		}
		default:
			throw new SARLInternalException("Unknown concrete object: " + expr);
		}
		translation = new Translation(result);
		return translation;
	}

	/**
	 * Translates any concrete SymbolicExpression with concrete type to
	 * equivalent CVC Expr using the ExprManager.
	 * 
	 * @param expr
	 * @return the CVC equivalent Expr
	 */
	private Translation translateConcrete_BV(SymbolicExpression expr) {
		Translation translation;
		SymbolicType type = expr.type();
		SymbolicTypeKind kind = type.typeKind();
		SymbolicObject object = expr.argument(0);
		FastList<String> result;
		switch (kind) {
		case BOOLEAN:
			result = new FastList<>(
					((BooleanObject) object).getBoolean() ? "TRUE" : "FALSE");
			break;
		case CHAR:
			result = new FastList<>(
					Integer.toString((int) ((CharObject) object).getChar()));
			break;
		case INTEGER:
			String intStr = object.toString();
			long rawVal = Long.valueOf(intStr);

			result = new FastList<>(getBVString(rawVal));
			break;
		case REAL:
			result = new FastList<>(object.toString());
			break;
		case UNINTERPRETED: {
			SymbolicUninterpretedType unintType = (SymbolicUninterpretedType) expr
					.type();
			String constructor = this.uninterpretedTypeConstructor(unintType);
			int key = unintType.soleSelector().apply(expr).getInt();

			translateType(unintType);
			result = new FastList<>(constructor, "(", String.valueOf(key), ")");
			break;
		}
		default:
			throw new SARLInternalException("Unknown concrete object: " + expr);
		}
		translation = new Translation(result);
		return translation;
	}

	/**
	 * Translates a symbolic constant. It is assumed that this is the first time
	 * the symbolic constant has been seen. It returns simply the name of the
	 * symbolic constant (in the form of a <code>FastList</code> of strings).
	 * For an ordinary (i.e., not quantified) symbolic constant, this method
	 * also adds to {@link #cvcDeclarations} a declaration of the symbolic
	 * constant.
	 * 
	 * @param symbolicConstant
	 *            a SARL symbolic constant
	 * @param isBoundVariable
	 *            is this a bound variable?
	 * @return the name of the symbolic constant as a fast string list
	 */
	private Translation translateSymbolicConstant(
			SymbolicConstant symbolicConstant, boolean isBoundVariable) {
		Translation translation;
		String name = normalizeSymbolicConstantName(symbolicConstant);
		SymbolicType symbolicType = symbolicConstant.type();
		FastList<String> type = translateType(symbolicType);
		FastList<String> result = new FastList<>(name);

		if (!isBoundVariable) {
			cvcDeclarations.addAll(name, " : ");
			cvcDeclarations.append(type);
			cvcDeclarations.add(";\n");
		}
		this.variableMap.put(symbolicConstant, result);
		translation = new Translation(result.clone());
		return translation;
	}

	/**
	 * Translates a symbolic constant into the BIT-VECTOR. It is assumed that
	 * this is the first time the symbolic constant has been seen. It returns
	 * simply the name of the symbolic constant (in the form of a
	 * <code>FastList</code> of strings). For an ordinary (i.e., not quantified)
	 * symbolic constant, this method also adds to {@link #cvcDeclarations} a
	 * declaration of the symbolic constant.
	 * 
	 * @param symbolicConstant
	 *            a SARL symbolic constant
	 * @param isBoundVariable
	 *            is this a bound variable?
	 * @return the name of the symbolic constant as a fast string list
	 */
	private Translation translateSymbolicConstant_BV(
			SymbolicConstant symbolicConstant, boolean isBoundVariable) {
		Translation translation;
		String name = normalizeSymbolicConstantName(symbolicConstant);
		SymbolicType symbolicType = symbolicConstant.type();
		FastList<String> type = translateType(symbolicType);
		FastList<String> result = new FastList<>(name);

		if (!isBoundVariable) {
			cvcDeclarations.addAll(name, " : ");
			if (symbolicType.isInteger())
				cvcDeclarations
						.append(new FastList<>("BITVECTOR(" + intLen + ")"));
			else
				cvcDeclarations.append(type);
			cvcDeclarations.add(";\n");
		}
		this.variableBVMap.put(symbolicConstant, result);
		translation = new Translation(result.clone());
		return translation;
	}

	/**
	 * Syntax example:
	 * 
	 * <pre>
	 * LAMBDA (x: INT): x * x - 1
	 * </pre>
	 * 
	 * @param expr
	 *            The lambda expression that is being translated.
	 * @return The translation result.
	 */
	private Translation translateLambda(SymbolicExpression expr) {
		Translation res;
		List<Translation> translations = new ArrayList<Translation>();
		Translation tempTranslation;
		Boolean involveDivOrModulo = false;
		FastList<String> result = new FastList<>("LAMBDA (",
				((SymbolicConstant) expr.argument(0)).name().getString(), ":");

		result.append(translateType(expr.type()));
		result.add("):");
		tempTranslation = translate((SymbolicExpression) expr.argument(1));
		if (tempTranslation.getIsDivOrModulo()) {
			translations.add(tempTranslation);
			involveDivOrModulo = true;
		}
		result.append(tempTranslation.getResult());
		res = new Translation(result);
		if (involveDivOrModulo) {
			combineTranslations(res, translations);
		}
		return res;
	}

	/**
	 * Translates an array-read expression a[i] into equivalent CVC expression
	 * 
	 * @param expr
	 *            a SARL symbolic expression of form a[i]
	 * @return an equivalent CVC expression
	 */
	private Translation translateArrayRead(SymbolicExpression expr) {
		Translation res;
		SymbolicExpression arrayExpression = (SymbolicExpression) expr
				.argument(0);
		NumericExpression indexExpression = (NumericExpression) expr
				.argument(1);
		FastList<String> result = new FastList<>("(");
		Translation tempTranslation;
		Boolean involveDivOrModulo = false;
		Boolean tempBool;
		List<Translation> translations = new ArrayList<Translation>();

		result.append(valueOfArray(arrayExpression).getResult());
		result.add(")[");
		tempTranslation = translate(indexExpression);
		tempBool = tempTranslation.getIsDivOrModulo();
		if (tempBool) {
			translations.add(tempTranslation);
			involveDivOrModulo = true;
		}
		result.append(tempTranslation.getResult());
		result.add("]");
		res = new Translation(result);
		if (involveDivOrModulo) {
			combineTranslations(res, translations);
		}
		return res;
	}

	/**
	 * Translates an tuple-read expression t.i into equivalent CVC expression.
	 * 
	 * Recall: TUPLE_READ: 2 arguments: arg0 is the tuple expression. arg1 is an
	 * IntObject giving the index in the tuple.
	 * 
	 * @param expr
	 *            a SARL symbolic expression of form t.i
	 * @return an equivalent CVC expression
	 */
	private Translation translateTupleRead(SymbolicExpression expr) {
		SymbolicExpression tupleExpression = (SymbolicExpression) expr
				.argument(0);
		int tupleLength = ((SymbolicTupleType) tupleExpression.type())
				.sequence().numTypes();
		int index = ((IntObject) expr.argument(1)).getInt();
		Translation tempTranslation;
		Boolean involveDivOrModulo = false;
		List<Translation> translations = new ArrayList<Translation>();

		if (tupleLength == 1) {
			assert index == 0;
			return translate((SymbolicExpression) expr.argument(0));
		} else {
			Translation res;
			FastList<String> result = new FastList<>("(");

			tempTranslation = translate(tupleExpression);
			if (tempTranslation.getIsDivOrModulo()) {
				translations.add(tempTranslation);
				involveDivOrModulo = true;
			}
			result.append(translate(tupleExpression).getResult());
			result.add(")." + index);
			res = new Translation(result);
			if (involveDivOrModulo) {
				combineTranslations(res, translations);
			}
			return res;
		}
	}

	/**
	 * Translates an array-write (or array update) SARL symbolic expression to
	 * equivalent CVC expression.
	 * 
	 * @param expr
	 *            a SARL array update expression "a WITH [i] := v"
	 * @return the result of translating to CVC
	 */
	private Translation translateArrayWrite(SymbolicExpression expr) {
		Translation result = pretranslateArrayWrite(expr);

		FastList<String> res = bigArray(lengthOfArray(expr).getResult(),
				result.getResult());
		result = new Translation(res);
		return result;
	}

	/**
	 * Translates a tuple-write (or tuple update) SARL symbolic expression to
	 * equivalent CVC expression.
	 * 
	 * Recall: TUPLE_WRITE: 3 arguments: arg0 is the original tuple expression,
	 * arg1 is an IntObject giving the index, arg2 is the new value to write
	 * into the tuple.
	 * 
	 * @param expr
	 *            a SARL tuple update expression
	 * @return the result of translating to CVC
	 */
	private Translation translateTupleWrite(SymbolicExpression expr) {
		Translation tempTranslation;
		Boolean involveDivOrModulo = false;
		List<Translation> translations = new ArrayList<Translation>();
		SymbolicExpression tupleExpression = (SymbolicExpression) expr
				.argument(0);
		int index = ((IntObject) expr.argument(1)).getInt();
		SymbolicExpression valueExpression = (SymbolicExpression) expr
				.argument(2);
		int tupleLength = ((SymbolicTupleType) expr.type()).sequence()
				.numTypes();

		if (tupleLength == 1) {
			assert index == 0;
			return translate(valueExpression);
		} else {
			Translation res;
			FastList<String> result = new FastList<>("(");

			tempTranslation = translate(tupleExpression);
			if (tempTranslation.getIsDivOrModulo()) {
				translations.add(tempTranslation);
				involveDivOrModulo = true;
			}
			result.append(tempTranslation.getResult());
			result.addAll(") WITH .", Integer.toString(index), " := (");
			tempTranslation = translate(valueExpression);
			if (tempTranslation.getIsDivOrModulo()) {
				translations.add(tempTranslation);
				involveDivOrModulo = true;
			}
			result.append(translate(valueExpression).getResult());
			result.add(")");
			res = new Translation(result);
			if (involveDivOrModulo) {
				combineTranslations(res, translations);
			}
			return res;
		}
	}

	/**
	 * Translates a multiple array-write (or array update) SARL symbolic
	 * expression to equivalent CVC expression.
	 * 
	 * @param expr
	 *            a SARL expression of kind DENSE_ARRAY_WRITE
	 * @return the result of translating expr to CVC
	 */
	private Translation translateDenseArrayWrite(SymbolicExpression expr) {
		Translation res;
		FastList<String> result = pretranslateDenseArrayWrite(expr).getResult();

		result = bigArray(lengthOfArray(expr).getResult(), result);
		res = new Translation(result);
		return res;
	}

	/**
	 * Translate a multiple tuple-write (or tuple update) SARL symbolic
	 * expression to equivalent CVC expression.
	 * 
	 * @param expr
	 *            a SARL expression of kind
	 *            {@link SymbolicOperator#DENSE_TUPLE_WRITE}
	 * @return result of translating to CVC
	 */
	private Translation translateDenseTupleWrite(SymbolicExpression expr) {
		SymbolicExpression tupleExpression = (SymbolicExpression) expr
				.argument(0);
		int tupleLength = ((SymbolicTupleType) expr.type()).sequence()
				.numTypes();
		// syntax t WITH .0 := blah, .1 := blah, ...
		SymbolicSequence<?> values = (SymbolicSequence<?>) expr.argument(1);
		int numValues = values.size();
		Translation tempTranslation;
		Boolean involveDivOrModulo = false;
		List<Translation> translations = new ArrayList<Translation>();

		if (numValues == 0) {
			tempTranslation = translate(tupleExpression);
			if (tempTranslation.getIsDivOrModulo()) {
				translations.add(tempTranslation);
				involveDivOrModulo = true;
			}
			return tempTranslation;
		}
		if (tupleLength == 1) {
			assert numValues == 1;
			SymbolicExpression value = values.get(0);

			if (value.isNull()) {
				tempTranslation = translate(tupleExpression);
				if (tempTranslation.getIsDivOrModulo()) {
					translations.add(tempTranslation);
					involveDivOrModulo = true;
				}
				return tempTranslation;
			} else {
				tempTranslation = translate(value);
				if (tempTranslation.getIsDivOrModulo()) {
					translations.add(tempTranslation);
					involveDivOrModulo = true;
				}
				return tempTranslation;
			}
		} else {
			Translation res;
			FastList<String> result = new FastList<>("(");
			boolean first = true;

			tempTranslation = translate(tupleExpression);
			if (tempTranslation.getIsDivOrModulo()) {
				translations.add(tempTranslation);
				involveDivOrModulo = true;
			}
			result.append(tempTranslation.getResult());
			result.add(")");
			for (int i = 0; i < numValues; i++) {
				SymbolicExpression value = values.get(i);

				if (!value.isNull()) {
					if (first) {
						result.add(" WITH ");
						first = false;
					} else {
						result.add(", ");
					}
					result.addAll(".", Integer.toString(i), " := (");
					tempTranslation = translate(value);
					if (tempTranslation.getIsDivOrModulo()) {
						translations.add(tempTranslation);
						involveDivOrModulo = true;
					}
					result.append(tempTranslation.getResult());
					result.add(")");
				}
			}
			res = new Translation(result);
			if (involveDivOrModulo) {
				combineTranslations(res, translations);
			}
			return res;
		}
	}

	/**
	 * Translates SymbolicExpressions of the type "exists" and "forall" into the
	 * CVC equivalent.
	 * 
	 * @param expr
	 *            a SARL "exists" or "forall" expression
	 * @return result of translating to CVC
	 */
	private Translation translateQuantifier(SymbolicExpression expr) {
		// syntax: FORALL (x : T) : pred
		Translation res;
		SymbolicOperator kind = expr.operator();
		SymbolicConstant boundVariable = (SymbolicConstant) expr.argument(0);
		String name = boundVariable.name().getString();
		BooleanExpression predicate = (BooleanExpression) expr.argument(1);
		FastList<String> result = new FastList<>(kind.toString());

		expressionMap.put(boundVariable,
				new Translation(new FastList<String>(name)));
		result.addAll(" (", name, " : ");
		result.append(translateType(boundVariable.type()));
		result.add(") : (");
		result.append(translate(predicate).getResult());
		result.add(")");
		res = new Translation(result);
		return res;
	}

	/**
	 * Given two SARL symbolic expressions of compatible type, this returns the
	 * CVC translation of the assertion that the two expressions are equal.
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
	 * @return result of translating into CVC the assertion "expr1=expr2"
	 */
	private Translation processEquality(SymbolicExpression expr1,
			SymbolicExpression expr2) {
		FastList<String> result;
		Translation res;

		if (expr1.type().typeKind() == SymbolicTypeKind.ARRAY) {
			// lengths are equal and forall i (0<=i<length).a[i]=b[i].
			// syntax: ((len1) = (len2)) AND
			// FORALL (i : INT) : (( 0 <= i AND i < len1) => ( ... ))
			FastList<String> extent1 = lengthOfArray(expr1).getResult();
			NumericSymbolicConstant index = newSarlAuxVar();
			String indexString = index.name().getString();
			SymbolicExpression read1 = universe.arrayRead(expr1, index);
			SymbolicExpression read2 = universe.arrayRead(expr2, index);

			result = new FastList<>("((");
			result.append(extent1.clone());
			result.add(") = (");
			result.append(lengthOfArray(expr2).getResult());
			result.addAll(")) AND FORALL (", indexString, " : INT) : ((0 <= ",
					indexString, " AND ", indexString, " < ");
			result.append(extent1);
			result.add(") => (");
			result.append(processEquality(read1, read2).getResult());
			result.add(")");
		} else {
			Translation t1 = translate(expr1);
			Translation t2 = translate(expr2);
			result = new FastList<>("((");
			result.append(t1.getResult());
			result.add(") = (");
			result.append(t2.getResult());
			result.add("))");

			Boolean b1 = t1.getIsDivOrModulo();
			Boolean b2 = t2.getIsDivOrModulo();

			List<Translation> translations = new ArrayList<Translation>();
			if (b1 || b2) {
				if (b1)
					translations.add(t1);
				if (b2)
					translations.add(t2);
				result = postProcessForSideEffectsOfDivideOrModule(result,
						translations);
			}
		}
		res = new Translation(result);
		return res;
	}

	/**
	 * Construct a exists expression using auxiliary variables and auxiliary
	 * constraints from division and modulo.
	 * 
	 * @param currentResult
	 * @param translations
	 *            list of {@link Translation}s coming from division or modulo
	 * @return
	 */
	private FastList<String> postProcessForSideEffectsOfDivideOrModule(
			FastList<String> currentResult, List<Translation> translations) {
		String exist = SymbolicOperator.EXISTS.toString();
		FastList<String> result = new FastList<>("(");

		result.add(exist + " (");
		int translationNum = translations.size();
		for (int i = 0; i < translationNum; i++) {
			List<FastList<String>> auxVars = translations.get(i).getAuxVars();

			int auxVarNum = auxVars.size();
			for (int j = 0; j < auxVarNum; j++) {
				if (i != translationNum - 1 || j != auxVarNum - 1) {
					result.append(auxVars.get(j));
					result.add(" : INT, ");
				} else {
					result.append(auxVars.get(j));
					result.add(" : INT");
				}
			}
		}
		result.add(") : (");
		result.append(currentResult);
		for (int i = 0; i < translationNum; i++) {
			result.add(" AND ");
			result.append(translations.get(i).getAuxConstraints());
		}
		result.add("))");
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
	private Translation translateEquality(SymbolicExpression expr) {
		SymbolicExpression leftExpression = (SymbolicExpression) expr
				.argument(0);
		SymbolicExpression rightExpression = (SymbolicExpression) expr
				.argument(1);
		Translation result = processEquality(leftExpression, rightExpression);

		return result;
	}

	/**
	 * <p>
	 * Translates a union-extract expression. The result has the form
	 * 
	 * <pre>
	 * UT_extract_i(y)
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
	 * DATATYPE
	 *   UT = UT_inject_0(UT_extract_0 : T0) | UT_inject_1(UT_extract_1 : T1) | ...
	 * END;
	 * </pre>
	 * 
	 * Usage:
	 * 
	 * <pre>
	 *   UT_inject_i(x)
	 *   UT_extract_i(y)
	 * </pre>
	 * 
	 * </p>
	 * 
	 * @param expr
	 *            a "union extract" expression
	 * @return result of translating to CVC
	 */
	private Translation translateUnionExtract(SymbolicExpression expr) {
		Translation res;
		int index = ((IntObject) expr.argument(0)).getInt();
		SymbolicExpression arg = (SymbolicExpression) expr.argument(1);
		SymbolicUnionType unionType = (SymbolicUnionType) arg.type();
		FastList<String> result = new FastList<>(selector(unionType, index));

		result.add("(");
		result.append(translate(arg).getResult());
		result.add(")");
		res = new Translation(result);
		return res;
	}

	/**
	 * <p>
	 * Translates a union-inject expression. The result has the form
	 * 
	 * <pre>
	 * UT_inject_i(x)
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
	 * @return the CVC translation of that expression
	 */
	private Translation translateUnionInject(SymbolicExpression expr) {
		Translation res;
		int index = ((IntObject) expr.argument(0)).getInt();
		SymbolicExpression arg = (SymbolicExpression) expr.argument(1);
		SymbolicUnionType unionType = (SymbolicUnionType) expr.type();
		FastList<String> result = new FastList<>(constructor(unionType, index));

		result.add("(");
		result.append(translate(arg).getResult());
		result.add(")");
		res = new Translation(result);
		return res;
	}

	/**
	 * <p>
	 * Translates a union-test expression. The result has the form
	 * 
	 * <pre>
	 * is_UT_inject_i(y)
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
	 * @return the CVC translation of that expression
	 */
	private Translation translateUnionTest(SymbolicExpression expr) {
		Translation res;
		int index = ((IntObject) expr.argument(0)).getInt();
		SymbolicExpression arg = (SymbolicExpression) expr.argument(1);
		SymbolicUnionType unionType = (SymbolicUnionType) arg.type();
		FastList<String> result = new FastList<>(
				"is_" + constructor(unionType, index));

		result.add("(");
		result.append(translate(arg).getResult());
		result.add(")");
		res = new Translation(result);
		return res;
	}

	private Translation translateCast(SymbolicExpression expression) {
		Translation translation;
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
			cvcDeclarations.addAll(castFunction, " : (");
			cvcDeclarations.append(translateType(originalType));
			cvcDeclarations.add(") -> (");
			cvcDeclarations.append(translateType(newType));
			cvcDeclarations.add(");\n");
			castMap.put(key, castFunction);
		}

		FastList<String> result = new FastList<>(castFunction, "(");

		result.append(translate(argument).getResult());
		result.add(")");
		translation = new Translation(result);
		return translation;
	}

	private Translation translateApply(SymbolicExpression expression) {
		Translation translation;
		Translation tempTranslation;
		List<Translation> translations = new ArrayList<Translation>();
		Boolean involveDivOrModulo = false;
		SymbolicExpression function = (SymbolicExpression) expression
				.argument(0);
		SymbolicSequence<?> arguments = (SymbolicSequence<?>) expression
				.argument(1);
		boolean first = true;
		FastList<String> result = new FastList<String>("(");

		result.append(translate(function).getResult());
		result.add(")(");
		for (SymbolicExpression arg : arguments) {
			if (first)
				first = false;
			else
				result.add(", ");
			tempTranslation = translate(arg);
			if (tempTranslation.getIsDivOrModulo()) {
				translations.add(tempTranslation);
				involveDivOrModulo = true;
			}
			result.append(tempTranslation.getResult());
		}
		result.add(")");
		translation = new Translation(result);
		if (involveDivOrModulo) {
			combineTranslations(translation, translations);
		}
		return translation;
	}

	private Translation translateNegative(SymbolicExpression expression) {
		Translation translation;
		Translation tempTranslation;
		List<Translation> translations = new ArrayList<Translation>();
		Boolean involveDivOrModulo = false;
		FastList<String> result = new FastList<>("-(");

		tempTranslation = translate(
				(SymbolicExpression) expression.argument(0));
		if (tempTranslation.getIsDivOrModulo()) {
			translations.add(tempTranslation);
			involveDivOrModulo = true;
		}
		result.append(tempTranslation.getResult());
		result.add(")");
		translation = new Translation(result);
		if (involveDivOrModulo) {
			combineTranslations(translation, translations);
		}
		return translation;
	}

	private Translation translateNEQ(SymbolicExpression expression) {
		Translation translation;
		FastList<String> result = new FastList<>("NOT(");

		result.append(
				processEquality((SymbolicExpression) expression.argument(0),
						(SymbolicExpression) expression.argument(1))
								.getResult());
		result.add(")");
		translation = new Translation(result);
		return translation;
	}

	private Translation translateNot(SymbolicExpression expression) {
		Translation translation;
		FastList<String> result = new FastList<>("NOT(");

		result.append(translate((SymbolicExpression) expression.argument(0))
				.getResult());
		result.add(")");
		translation = new Translation(result);
		return translation;
	}

	private Translation translatePower(SymbolicExpression expression) {
		SymbolicObject exp = expression.argument(1);
		// if the exponent is real type, the power operation will be translated
		// to a uninterpreted function with axioms:
		if (exp.symbolicObjectKind() == SymbolicObjectKind.NUMBER) {
			if (((NumberObject) exp).isReal())
				return translatePowerRealExp(expression);
			else
				return translatePowerIntExp(expression);
		} else {
			if (((NumericExpression) exp).type().isReal())
				return translatePowerRealExp(expression);
			else
				return translatePowerIntExp(expression);
		}
	}

	/**
	 * Translating power operation where exponent is real type
	 * 
	 * @param expression
	 *            a power operation
	 * @return the {@link Translation} of the given power operation
	 */
	private Translation translatePowerRealExp(SymbolicExpression expression) {
		NumericExpression base = (NumericExpression) expression.argument(0);
		SymbolicObject arg1 = expression.argument(1);
		NumericExpression exp;

		if (arg1.symbolicObjectKind() == SymbolicObjectKind.NUMBER) {
			exp = universe.number(((NumberObject) arg1).getNumber());
		} else
			exp = (NumericExpression) arg1;

		NumericExpression expr = getPowerRealTheory().applyPow(base, exp);
		Translation result;

		// push theory into the specific stack so that the specific function
		// name for power will not be normalized (hence no naming conflict will
		// happen):
		translatingTheoryStack.push(CVCPowerReal.pow);
		result = translate(expr);
		if (!declaredTheories.contains(CVCPowerReal.TheoryName)) {
			// adding axioms into the context if they have not been added:
			BooleanExpression[] axioms = getPowerRealTheory().getAxioms();
			Translation axTranslation;

			declaredTheories.add(CVCPowerReal.TheoryName);
			for (BooleanExpression axiom : axioms) {
				axTranslation = translate(axiom);
				// axioms are all suppose to be quantified:
				cvcDeclarations.append(axTranslation.getResult());
			}
		}
		translatingTheoryStack.pop();
		return result;
	}

	/**
	 * Translating power operation where exponent is integer type
	 * 
	 * @param expression
	 *            a power operation
	 * @return the {@link Translation} of the given power operation
	 */
	private Translation translatePowerIntExp(SymbolicExpression expression) {
		// apparently "^" but not documented
		Translation translation;
		SymbolicObject exponent = expression.argument(1);
		FastList<String> result = new FastList<>("(");
		Translation tempTranslation;
		List<Translation> translations = new ArrayList<Translation>();
		Boolean involveDivOrModulo = false;

		tempTranslation = translate(
				(SymbolicExpression) expression.argument(0));
		if (tempTranslation.getIsDivOrModulo()) {
			translations.add(tempTranslation);
			involveDivOrModulo = true;
		}
		result.append(tempTranslation.getResult());
		result.add(")^");
		if (exponent instanceof NumberObject)
			result.add(exponent.toString());
		else {
			result.add("(");
			tempTranslation = translate((SymbolicExpression) exponent);
			if (tempTranslation.getIsDivOrModulo()) {
				translations.add(tempTranslation);
				involveDivOrModulo = true;
			}
			result.append(tempTranslation.getResult());
			result.add(")");
		}
		translation = new Translation(result);
		if (involveDivOrModulo) {
			combineTranslations(translation, translations);
		}
		return translation;
	}

	private Translation translateCond(SymbolicExpression expression) {
		// syntax: IF b THEN x ELSE y ENDIF
		Translation translation;
		FastList<String> result = new FastList<>("IF (");
		Translation tempTranslation;
		List<Translation> translations = new ArrayList<Translation>();
		Boolean involveDivOrModulo = false;

		tempTranslation = translate(
				(SymbolicExpression) expression.argument(0));
		if (tempTranslation.getIsDivOrModulo()) {
			translations.add(tempTranslation);
			involveDivOrModulo = true;
		}
		result.append(tempTranslation.getResult());
		result.add(") THEN (");
		tempTranslation = translate(
				(SymbolicExpression) expression.argument(1));
		if (tempTranslation.getIsDivOrModulo()) {
			translations.add(tempTranslation);
			involveDivOrModulo = true;
		}
		result.append(translate((SymbolicExpression) expression.argument(1))
				.getResult());
		result.add(") ELSE (");
		tempTranslation = translate(
				(SymbolicExpression) expression.argument(2));
		if (tempTranslation.getIsDivOrModulo()) {
			translations.add(tempTranslation);
			involveDivOrModulo = true;
		}
		result.append(translate((SymbolicExpression) expression.argument(2))
				.getResult());
		result.add(") ENDIF");
		translation = new Translation(result);
		if (involveDivOrModulo) {
			combineTranslations(translation, translations);
		}
		return translation;
	}

	/**
	 * 
	 * <p>
	 * Division or Modulo will be transformed into a struct consisting of the
	 * value of the expression, auxiliary variables and auxiliary constraints.
	 * </p>
	 * 
	 * <p>
	 * eg. a/b will be transformed into: value: q (q is the result of a/b)
	 * constraints: b * q + r = a && r >= 0 && r < b
	 * </p>
	 * 
	 * @param arg1
	 *            numerator
	 * @param arg2
	 *            denominator
	 * @param operator
	 *            either {@link SymbolicOperator.#INT_DIVIDE} or
	 *            {@link SymbolicOperator.#MODULO}
	 * 
	 * @return A struct {@link Translation} encapsules 1. the value of the
	 *         division or modulo 2. the generated auxiliary variables 3. the
	 *         generated constraints
	 */
	private Translation getIntDivInfo(NumericExpression arg1,
			NumericExpression arg2, SymbolicOperator operator) {
		Translation translation;
		FastList<String> quotient;
		FastList<String> remainder;
		FastList<String> numerator = translate(arg1).getResult();
		FastList<String> denominator = translate(arg2).getResult();
		// numerator-denominator pair:
		Pair<NumericExpression, NumericExpression> ndpair = new Pair<>(arg1,
				arg2);
		// quotient-remainder pair:
		Pair<FastList<String>, FastList<String>> qrpair = intDivMap.get(ndpair);
		if (qrpair == null) {
			quotient = new FastList<>(
					newCvcAuxVar(new FastList<String>("INT")));
			remainder = new FastList<>(
					newCvcAuxVar(new FastList<String>("INT")));
		} else {
			quotient = qrpair.left.clone();
			remainder = qrpair.right.clone();
		}

		/**
		 * constraint1: numerator = quotient*denominator + remainder
		 * 
		 * ((numerator) = (quotient) * (denominator) + (remainder))
		 */
		FastList<String> constraint1 = new FastList<>("((");
		constraint1.append(numerator.clone());
		constraint1.add(") = (");
		constraint1.append(quotient.clone());
		constraint1.add(")*(");
		constraint1.append(denominator.clone());
		constraint1.add(") + (");
		constraint1.append(remainder.clone());
		constraint1.add("))");

		/**
		 * constraint2: remainder >= 0 && remainder < denominator
		 * 
		 * (((remainder) >= 0) AND ((remainder) < (denominator)))
		 */
		FastList<String> constraint2 = new FastList<>("(((");

		constraint2.append(remainder.clone());
		constraint2.add(") >= 0) AND ((");
		constraint2.append(remainder.clone());
		constraint2.add(") < (");
		constraint2.append(denominator.clone());
		constraint2.add(")))");

		FastList<String> constraints = new FastList<>("(");

		constraints.append(constraint1);
		constraints.add(" AND ");
		constraints.append(constraint2);
		constraints.add(")");

		List<FastList<String>> auxVars = new ArrayList<FastList<String>>();

		auxVars.add(quotient.clone());
		auxVars.add(remainder.clone());
		if (operator == SymbolicOperator.INT_DIVIDE)
			translation = new Translation(quotient.clone(), true, constraints,
					auxVars);
		else if (operator == SymbolicOperator.MODULO)
			translation = new Translation(remainder.clone(), true, constraints,
					auxVars);
		else
			translation = new Translation(quotient.clone());

		return translation;
	}

	/**
	 * @param operator
	 *            the operator can be {@link SymbolicOperator.#DIVIDE} or
	 *            {@link SymbolicOperator.#SUBTRACT} or
	 *            {@link SymbolicOperator.#LESS_THAN} or
	 *            {@link SymbolicOperator.#LESS_THAN_EQUALS}
	 * @param arg0
	 * @param arg1
	 * @return
	 */
	private Translation translateBinary(String operator,
			SymbolicExpression arg0, SymbolicExpression arg1) {
		Translation translation;
		Translation t1 = translate(arg0);
		Translation t2 = translate(arg1);
		FastList<String> result = new FastList<>("(");

		result.append(t1.getResult());
		result.addAll(") ", operator, " (");
		result.append(t2.getResult());
		result.add(")");

		Boolean b1 = t1.getIsDivOrModulo();
		Boolean b2 = t2.getIsDivOrModulo();

		if (b1 || b2) {
			List<Translation> translations = new ArrayList<Translation>();
			int translationsNum;

			if (b1)
				translations.add(t1);
			if (b2)
				translations.add(t2);
			translationsNum = translations.size();
			// merge side effects.
			FastList<String> newConstraint = new FastList<>();
			List<FastList<String>> newAuxVars = new ArrayList<FastList<String>>();

			if (translationsNum == 1) {
				Translation tempTranslation = translations.get(0);

				newConstraint = tempTranslation.getAuxConstraints().clone();
				newAuxVars.addAll(tempTranslation.getAuxVars());
			} else {
				Translation tempTranslation1 = translations.get(0);
				Translation tempTranslation2 = translations.get(1);

				newAuxVars.addAll(tempTranslation1.getAuxVars());
				newAuxVars.addAll(tempTranslation2.getAuxVars());
				newConstraint.add("(");
				newConstraint.append(tempTranslation1.getAuxConstraints());
				newConstraint.add(" AND ");
				newConstraint.append(tempTranslation2.getAuxConstraints());
				newConstraint.add(")");
			}
			translation = new Translation(result, true, newConstraint,
					newAuxVars);

		} else {
			translation = new Translation(result);
		}
		return translation;
	}

	/**
	 * @param operator
	 *            the operator can be {@link SymbolicOperator.#DIVIDE} or
	 *            {@link SymbolicOperator.#SUBTRACT} or
	 *            {@link SymbolicOperator.#LESS_THAN} or
	 *            {@link SymbolicOperator.#LESS_THAN_EQUALS}
	 * @param arg0
	 * @param arg1
	 * @return
	 */
	private Translation translateBinary_BV(String operator,
			SymbolicExpression arg0, SymbolicExpression arg1) {
		hasBitOp = true;

		Translation translation;
		Translation t1 = translate_BV(arg0);
		Translation t2 = translate_BV(arg1);
		FastList<String> result = new FastList<>(operator + "(");

		result.append(t1.getResult());
		result.addAll(", ");
		result.append(t2.getResult());
		result.add(")");

		Boolean b1 = t1.getIsDivOrModulo();
		Boolean b2 = t2.getIsDivOrModulo();

		if (b1 || b2) {
			List<Translation> translations = new ArrayList<Translation>();
			int translationsNum;

			if (b1)
				translations.add(t1);
			if (b2)
				translations.add(t2);
			translationsNum = translations.size();
			// merge side effects.
			FastList<String> newConstraint = new FastList<>();
			List<FastList<String>> newAuxVars = new ArrayList<FastList<String>>();

			if (translationsNum == 1) {
				Translation tempTranslation = translations.get(0);

				newConstraint = tempTranslation.getAuxConstraints().clone();
				newAuxVars.addAll(tempTranslation.getAuxVars());
			} else {
				Translation tempTranslation1 = translations.get(0);
				Translation tempTranslation2 = translations.get(1);

				newAuxVars.addAll(tempTranslation1.getAuxVars());
				newAuxVars.addAll(tempTranslation2.getAuxVars());
				newConstraint.add("(");
				newConstraint.append(tempTranslation1.getAuxConstraints());
				newConstraint.add(" AND ");
				newConstraint.append(tempTranslation2.getAuxConstraints());
				newConstraint.add(")");
			}
			translation = new Translation(result, true, newConstraint,
					newAuxVars);

		} else {
			translation = new Translation(result);
		}
		return translation;
	}

	/**
	 * @param operator
	 *            The operator can be {@link SymbolicOperator.#MULTIPLY} or
	 *            {@link SymbolicOperator.#ADD} or {@link SymbolicOperator.#AND}
	 *            {@link SymbolicOperator.#OR}
	 * @param defaultValue
	 * @param expression
	 * @return
	 */
	private Translation translateKeySet(String operator, String defaultValue,
			SymbolicExpression expression) {
		Translation translation;
		int size = expression.numArguments();

		if (size == 0) {
			return new Translation(new FastList<>(defaultValue));
		} else if (size == 1) {
			return translate((SymbolicExpression) expression.argument(0));
		} else {
			Boolean divOrModole = false;
			List<Translation> translations = new ArrayList<>();
			FastList<String> result = new FastList<>();

			for (int i = 0; i < size; i++) {
				SymbolicExpression term = (SymbolicExpression) expression
						.argument(i);
				Translation t = translate(term);

				if (t.getIsDivOrModulo()) {
					translations.add(t.clone());
					divOrModole = true;
				}
				if (i > 0)
					result.add(operator);
				result.add("(");
				result.append(t.getResult());
				result.add(")");
			}
			if (divOrModole
					&& (operator.equals(" + ") || operator.equals(" * "))) {
				int translationsNum = translations.size();
				FastList<String> newConstraint = new FastList<>();
				List<FastList<String>> newAuxVars = new ArrayList<FastList<String>>();

				if (translationsNum == 1) {
					Translation tempTranslation = translations.get(0);

					newConstraint = tempTranslation.getAuxConstraints().clone();
					newAuxVars.addAll(tempTranslation.getAuxVars());
				} else {
					Translation tempTranslation1 = translations.get(0);
					Translation tempTranslation2 = translations.get(1);

					newAuxVars.addAll(tempTranslation1.getAuxVars());
					newAuxVars.addAll(tempTranslation2.getAuxVars());
					newConstraint.add("(");
					newConstraint.append(tempTranslation1.getAuxConstraints());
					newConstraint.add(" AND ");
					newConstraint.append(tempTranslation2.getAuxConstraints());
					newConstraint.add(")");
				}
				translation = new Translation(result, true, newConstraint,
						newAuxVars);
			} else {
				translation = new Translation(result);
			}
			return translation;
		}
	}

	/**
	 * Translates a SARL symbolic expression to the language of CVC.
	 * 
	 * @param expression
	 *            a non-null SymbolicExpression
	 * @return translation to CVC as a fast list of strings
	 */
	private Translation translateWork(SymbolicExpression expression)
			throws TheoremProverException {
		SymbolicOperator operator = expression.operator();
		Translation result;

		switch (operator) {
		case ADD:
			result = translateKeySet(" + ", "0", expression);
			break;
		case AND:
			result = translateKeySet(" AND ", "TRUE", expression);
			break;
		case APPLY:
			result = translateApply(expression);
			break;
		case ARRAY:
			result = translateConcreteArray(expression);
			break;
		case ARRAY_LAMBDA:
			// TODO: are they supported in CVC3?
			throw new TheoremProverException(
					"Array lambdas are not supported by CVC");
		case ARRAY_READ:
			result = translateArrayRead(expression);
			break;
		case ARRAY_WRITE:
			result = translateArrayWrite(expression);
			break;
		case BIT_AND:
			result = translateBitOperation(" & ",
					(SymbolicExpression) expression.argument(0),
					(SymbolicExpression) expression.argument(1));
			break;
		case BIT_NOT:
			result = translateBitOperation(" ~ ",
					(SymbolicExpression) expression.argument(0), null);
			break;
		case BIT_OR:
			result = translateBitOperation(" BVNOR ",
					(SymbolicExpression) expression.argument(0),
					(SymbolicExpression) expression.argument(1));
			break;
		case BIT_XOR:
			result = translateBitOperation(" BVXOR ",
					(SymbolicExpression) expression.argument(0),
					(SymbolicExpression) expression.argument(1));
			break;
		case BIT_SHIFT_LEFT:
			result = translateBitShift("bvshl",
					(SymbolicExpression) expression.argument(0),
					(SymbolicExpression) expression.argument(1));
			break;
		case BIT_SHIFT_RIGHT:
			result = translateBitShift("bvlshr",
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
			result = translateBinary(" / ",
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
			if (simplifyIntDivision) {
				result = getIntDivInfo(
						(NumericExpression) expression.argument(0),
						(NumericExpression) expression.argument(1),
						SymbolicOperator.INT_DIVIDE);
			} else {
				result = translateBinary(" DIV ",
						(SymbolicExpression) expression.argument(0),
						(SymbolicExpression) expression.argument(1));
			}
			break;
		case LENGTH:
			result = lengthOfArray((SymbolicExpression) expression.argument(0));
			break;
		case LESS_THAN:
			result = translateBinary(" < ",
					(SymbolicExpression) expression.argument(0),
					(SymbolicExpression) expression.argument(1));
			break;
		case LESS_THAN_EQUALS:
			result = translateBinary(" <= ",
					(SymbolicExpression) expression.argument(0),
					(SymbolicExpression) expression.argument(1));
			break;
		case MODULO:
			if (simplifyIntDivision) {
				result = getIntDivInfo(
						(NumericExpression) expression.argument(0),
						(NumericExpression) expression.argument(1),
						SymbolicOperator.MODULO);
			} else
				result = translateBinary(" MOD ",
						(SymbolicExpression) expression.argument(0),
						(SymbolicExpression) expression.argument(1));
			break;
		case MULTIPLY:
			result = translateKeySet(" * ", "1", expression);
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
			result = translateKeySet(" OR ", "FALSE", expression);
			break;
		case POWER:
			result = translatePower(expression);
			break;
		case SUBTRACT:
			result = translateBinary(" - ",
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
		case DERIV: {
			FastList<String> cvcType = translateType(expression.type());
			String name = newCvcAuxVar(cvcType.clone());

			cvcDeclarations.addAll(name, " : ");
			cvcDeclarations.append(cvcType);
			cvcDeclarations.add(";\n");
			result = new Translation(new FastList<String>(name));
			break;
		}
		case DIFFERENTIABLE: {
			// TODO: introduce uninterpreted functions
			// Need a different one for each n (dimension of domain)
			// DIFFERENTIABLE_0, DIFFERENTIABLE_1, ...
			// for now, just introduce a boolean variable
			FastList<String> cvcType = translateType(expression.type());
			String name = newCvcAuxVar(cvcType.clone());

			cvcDeclarations.addAll(name, " : ");
			cvcDeclarations.append(cvcType);
			cvcDeclarations.add(";\n");
			result = new Translation(new FastList<String>(name));
			break;
		}
		default:
			throw new SARLInternalException(
					"unreachable: unknown operator: " + operator);
		}
		return result;
	}

	private Translation translateBitShift(String operator,
			SymbolicExpression arg0, SymbolicExpression arg1) {
		hasBitOp = true;

		Translation translation;
		Translation t1 = translate(arg0);
		Translation t2 = translate(arg1); // Should be BV, but requires INT
		FastList<String> result = new FastList<>(operator + "(");

		result.append(t1.getResult());
		result.add(", ");
		result.append(t2.getResult());
		result.add(")");

		Boolean b1 = t1.getIsDivOrModulo();
		Boolean b2 = t2.getIsDivOrModulo();

		if (b1 || b2) {
			List<Translation> translations = new ArrayList<Translation>();
			int translationsNum;

			if (b1)
				translations.add(t1);
			if (b2)
				translations.add(t2);
			translationsNum = translations.size();
			// merge side effects.
			FastList<String> newConstraint = new FastList<>();
			List<FastList<String>> newAuxVars = new ArrayList<FastList<String>>();

			if (translationsNum == 1) {
				Translation tempTranslation = translations.get(0);

				newConstraint = tempTranslation.getAuxConstraints().clone();
				newAuxVars.addAll(tempTranslation.getAuxVars());
			} else {
				Translation tempTranslation1 = translations.get(0);
				Translation tempTranslation2 = translations.get(1);

				newAuxVars.addAll(tempTranslation1.getAuxVars());
				newAuxVars.addAll(tempTranslation2.getAuxVars());
				newConstraint.add("(");
				newConstraint.append(tempTranslation1.getAuxConstraints());
				newConstraint.add(" AND ");
				newConstraint.append(tempTranslation2.getAuxConstraints());
				newConstraint.add(")");
			}
			translation = new Translation(result, true, newConstraint,
					newAuxVars);

		} else {
			translation = new Translation(result);
		}
		return translation;
	}

	private Translation translateBitShift_BV(String operator,
			SymbolicExpression arg0, SymbolicExpression arg1) {
		hasBitOp = true;

		Translation translation;
		Translation t1 = translate_BV(arg0);
		Translation t2 = translate(arg1); // Should be BV, but requires INT
		FastList<String> result = new FastList<>(operator + "(");

		result.append(t1.getResult());
		result.add(", ");
		result.append(t2.getResult());
		result.add(")");

		Boolean b1 = t1.getIsDivOrModulo();
		Boolean b2 = t2.getIsDivOrModulo();

		if (b1 || b2) {
			List<Translation> translations = new ArrayList<Translation>();
			int translationsNum;

			if (b1)
				translations.add(t1);
			if (b2)
				translations.add(t2);
			translationsNum = translations.size();
			// merge side effects.
			FastList<String> newConstraint = new FastList<>();
			List<FastList<String>> newAuxVars = new ArrayList<FastList<String>>();

			if (translationsNum == 1) {
				Translation tempTranslation = translations.get(0);

				newConstraint = tempTranslation.getAuxConstraints().clone();
				newAuxVars.addAll(tempTranslation.getAuxVars());
			} else {
				Translation tempTranslation1 = translations.get(0);
				Translation tempTranslation2 = translations.get(1);

				newAuxVars.addAll(tempTranslation1.getAuxVars());
				newAuxVars.addAll(tempTranslation2.getAuxVars());
				newConstraint.add("(");
				newConstraint.append(tempTranslation1.getAuxConstraints());
				newConstraint.add(" AND ");
				newConstraint.append(tempTranslation2.getAuxConstraints());
				newConstraint.add(")");
			}
			translation = new Translation(result, true, newConstraint,
					newAuxVars);

		} else {
			translation = new Translation(result);
		}
		return translation;
	}

	private Translation translateBitOperation(String operator,
			SymbolicExpression arg0, SymbolicExpression arg1) {
		hasBitOp = true;

		Translation translation;

		if (arg1 == null) {
			Translation tempTranslation;
			List<Translation> translations = new ArrayList<Translation>();
			Boolean involveDivOrModulo = false;
			FastList<String> result = new FastList<>("~ (");

			tempTranslation = translate((SymbolicExpression) arg0.argument(0));
			if (tempTranslation.getIsDivOrModulo()) {
				translations.add(tempTranslation);
				involveDivOrModulo = true;
			}
			result.append(tempTranslation.getResult());
			result.add(")");
			translation = new Translation(result);
			if (involveDivOrModulo) {
				combineTranslations(translation, translations);
			}
			return translation;
		}

		Translation t1 = translate(arg0);
		Translation t2 = translate(arg1); // Should be BV, but requires INT
		FastList<String> result = new FastList<>(operator + "(");

		result.append(t1.getResult());
		result.add(", ");
		result.append(t2.getResult());
		result.add(")");
		if (operator.contains("BVNOR")) {
			result.addFront("~(");
			result.add(")");
		}

		Boolean b1 = t1.getIsDivOrModulo();
		Boolean b2 = t2.getIsDivOrModulo();

		if (b1 || b2) {
			List<Translation> translations = new ArrayList<Translation>();
			int translationsNum;

			if (b1)
				translations.add(t1);
			if (b2)
				translations.add(t2);
			translationsNum = translations.size();
			// merge side effects.
			FastList<String> newConstraint = new FastList<>();
			List<FastList<String>> newAuxVars = new ArrayList<FastList<String>>();

			if (translationsNum == 1) {
				Translation tempTranslation = translations.get(0);

				newConstraint = tempTranslation.getAuxConstraints().clone();
				newAuxVars.addAll(tempTranslation.getAuxVars());
			} else {
				Translation tempTranslation1 = translations.get(0);
				Translation tempTranslation2 = translations.get(1);

				newAuxVars.addAll(tempTranslation1.getAuxVars());
				newAuxVars.addAll(tempTranslation2.getAuxVars());
				newConstraint.add("(");
				newConstraint.append(tempTranslation1.getAuxConstraints());
				newConstraint.add(" AND ");
				newConstraint.append(tempTranslation2.getAuxConstraints());
				newConstraint.add(")");
			}
			translation = new Translation(result, true, newConstraint,
					newAuxVars);

		} else {
			translation = new Translation(result);
		}
		return translation;
	}

	private Translation translateBitOperation_BV(String operator,
			SymbolicExpression arg0, SymbolicExpression arg1) {
		hasBitOp = true;

		Translation translation;

		if (arg1 == null) {
			Translation tempTranslation;
			List<Translation> translations = new ArrayList<Translation>();
			Boolean involveDivOrModulo = false;
			FastList<String> result = new FastList<>("~ (");

			tempTranslation = translate_BV(
					(SymbolicExpression) arg0.argument(0));
			if (tempTranslation.getIsDivOrModulo()) {
				translations.add(tempTranslation);
				involveDivOrModulo = true;
			}
			result.append(tempTranslation.getResult());
			result.add(")");
			translation = new Translation(result);
			if (involveDivOrModulo) {
				combineTranslations(translation, translations);
			}
			return translation;
		}

		Translation t1 = translate_BV(arg0);
		Translation t2 = translate_BV(arg1); // Should be BV, but requires INT
		FastList<String> result = new FastList<>(operator + "(");

		result.append(t1.getResult());
		result.add(", ");
		result.append(t2.getResult());
		result.add(")");

		Boolean b1 = t1.getIsDivOrModulo();
		Boolean b2 = t2.getIsDivOrModulo();

		if (b1 || b2) {
			List<Translation> translations = new ArrayList<Translation>();
			int translationsNum;

			if (b1)
				translations.add(t1);
			if (b2)
				translations.add(t2);
			translationsNum = translations.size();
			// merge side effects.
			FastList<String> newConstraint = new FastList<>();
			List<FastList<String>> newAuxVars = new ArrayList<FastList<String>>();

			if (translationsNum == 1) {
				Translation tempTranslation = translations.get(0);

				newConstraint = tempTranslation.getAuxConstraints().clone();
				newAuxVars.addAll(tempTranslation.getAuxVars());
			} else {
				Translation tempTranslation1 = translations.get(0);
				Translation tempTranslation2 = translations.get(1);

				newAuxVars.addAll(tempTranslation1.getAuxVars());
				newAuxVars.addAll(tempTranslation2.getAuxVars());
				newConstraint.add("(");
				newConstraint.append(tempTranslation1.getAuxConstraints());
				newConstraint.add(" AND ");
				newConstraint.append(tempTranslation2.getAuxConstraints());
				newConstraint.add(")");
			}
			translation = new Translation(result, true, newConstraint,
					newAuxVars);

		} else {
			translation = new Translation(result);
		}
		return translation;
	}

	private FastList<String> translateType(SymbolicType type) {
		FastList<String> result = typeMap.get(type);

		if (result != null)
			return result.clone();

		SymbolicTypeKind kind = type.typeKind();

		switch (kind) {
		case BOOLEAN:
			result = new FastList<>("BOOLEAN");
			break;
		case INTEGER:
		case CHAR:
			result = new FastList<>("INT");
			break;
		case REAL:
			result = new FastList<>("REAL");
			break;
		case ARRAY: {
			SymbolicArrayType arrayType = (SymbolicArrayType) type;

			result = new FastList<>("ARRAY INT OF (");
			result.append(translateType(arrayType.elementType()));
			result.add(")");
			result.addFront("[INT, ");
			result.add("]");
			break;
		}
		case TUPLE: {
			SymbolicTupleType tupleType = (SymbolicTupleType) type;
			SymbolicTypeSequence sequence = tupleType.sequence();
			int numTypes = sequence.numTypes();

			if (numTypes == 1) {
				result = translateType(sequence.getType(0));
			} else {
				boolean first = true;

				result = new FastList<>("[");
				for (SymbolicType memberType : sequence) {
					if (first)
						first = false;
					else
						result.add(", ");
					result.append(translateType(memberType));
				}
				result.add("]");
			}
			break;
		}
		case FUNCTION: {
			SymbolicFunctionType funcType = (SymbolicFunctionType) type;
			SymbolicTypeSequence inputs = funcType.inputTypes();
			int numInputs = inputs.numTypes();
			boolean first = true;

			if (numInputs == 0)
				throw new SARLException(
						"CVC* requires a function type to have at least one input");
			result = new FastList<>("(");
			for (SymbolicType inputType : inputs) {
				if (first)
					first = false;
				else
					result.add(", ");
				result.append(translateType(inputType));
			}
			result.add(") -> (");
			result.append(translateType(funcType.outputType()));
			result.add(")");
			break;
		}
		case UNION: {
			// this is the first time this type has been encountered, so
			// it must be declared...
			//
			// Declaration of a union type UT, with member types T0, T1, ...:
			//
			// DATATYPE
			// UT = UT_inject_0(UT_extract_0 : T0) | UT_inject_1(UT_extract_1 :
			// T1) | ...
			// END;
			//
			// Usage:
			//
			// UT_inject_i(x)
			// UT_extract_i(y)
			SymbolicUnionType unionType = (SymbolicUnionType) type;
			SymbolicTypeSequence sequence = unionType.sequence();
			String name = unionType.name().getString();
			int n = sequence.numTypes();

			cvcDeclarations.addAll("DATATYPE\n", name, " = ");
			for (int i = 0; i < n; i++) {
				SymbolicType memberType = sequence.getType(i);

				if (i > 0)
					cvcDeclarations.add(" | ");
				cvcDeclarations.addAll(constructor(unionType, i), "(",
						selector(unionType, i), " : ");
				cvcDeclarations.append(translateType(memberType));
				cvcDeclarations.add(")");
			}
			cvcDeclarations.add("END;\n");
			result = new FastList<>(name);
			break;
		}
		case UNINTERPRETED: {
			SymbolicUninterpretedType unitType = (SymbolicUninterpretedType) type;
			String typeName = uninterpretedTypeName(unitType);
			String consName = uninterpretedTypeConstructor(unitType);

			cvcDeclarations.addAll("DATATYPE\n");
			cvcDeclarations.addAll(typeName, " = ", consName, "(selector_",
					unitType.name().getString(), " : INT)", "\n");
			cvcDeclarations.addAll("END;\n");
			result = new FastList<>(typeName);
			break;
		}
		default:
			throw new SARLInternalException("Unknown SARL type: " + type);
		}
		typeMap.put(type, result);
		return result.clone();
	}

	private Translation translate(SymbolicExpression expression)
			throws TheoremProverException {
		Translation res = expressionMap.get(expression);

		if (res == null) {
			res = translateWork(expression);
			this.expressionMap.put(expression, res);
		}
		return res.clone();
	}

	// BV-Based Query Translations
	// TODO: Re-factor this part like Z3Translator, when CVC allows the
	// conversion between BITVECTOR(N) and INT.

	private Translation translate_BV(SymbolicExpression expression)
			throws TheoremProverException {
		Translation res = expressionBVMap.get(expression);

		if (res == null) {
			res = translateWork_BV(expression);
			this.expressionBVMap.put(expression, res);
		}
		if (res == null)
			System.out.println(expression.operator());
		return res.clone();
	}

	private Translation translateWork_BV(SymbolicExpression expression) {
		SymbolicOperator operator = expression.operator();
		Translation result = null;

		// TODO: Implement all operators
		switch (operator) {
		case ADD:
			result = translateKeySet_BV(" BVPLUS ", getBVString(0), expression);
			break;
		case AND:
			result = translateKeySet(" AND ", "TRUE", expression);
			break;
		case APPLY:
			result = translateApply(expression);
			break;
		case ARRAY:
			// result = translateConcreteArray(expression);
			break;
		case ARRAY_LAMBDA:
			throw new TheoremProverException(
					"Array lambdas are not supported by CVC");
		case ARRAY_READ:
			result = translateArrayRead(expression);
			break;
		case ARRAY_WRITE:
			// result = translateArrayWrite(expression);
			break;
		case BIT_AND:
			result = translateBitOperation_BV(" & ",
					(SymbolicExpression) expression.argument(0),
					(SymbolicExpression) expression.argument(1));
			break;
		case BIT_NOT:
			result = translateBitOperation_BV(" ~ ",
					(SymbolicExpression) expression.argument(0), null);
			break;
		case BIT_OR:
			result = translateBitOperation_BV(" BVNOR ",
					(SymbolicExpression) expression.argument(0),
					(SymbolicExpression) expression.argument(1));
			break;
		case BIT_XOR:
			// result = translateBitOperation(" BVXOR ",
			// (SymbolicExpression) expression.argument(0),
			// (SymbolicExpression) expression.argument(1));
			break;
		case BIT_SHIFT_LEFT:
			result = translateBitShift_BV(" BVSHL ",
					(SymbolicExpression) expression.argument(0),
					(SymbolicExpression) expression.argument(1));
			break;
		case BIT_SHIFT_RIGHT:
			result = translateBitShift_BV(" BVLSHR ",
					(SymbolicExpression) expression.argument(0),
					(SymbolicExpression) expression.argument(1));
			break;
		case CAST:
			result = translateCast(expression);
			break;
		case CONCRETE:
			result = translateConcrete_BV(expression);
			break;
		case COND:
			// result = translateCond(expression);
			break;
		case DENSE_ARRAY_WRITE:
			// result = translateDenseArrayWrite(expression);
			break;
		case DENSE_TUPLE_WRITE:
			// result = translateDenseTupleWrite(expression);
			break;
		case DIVIDE: // real division
			// result = translateBinary(" / ",
			// (SymbolicExpression) expression.argument(0),
			// (SymbolicExpression) expression.argument(1));
			break;
		case EQUALS:
			// result = translateEquality(expression);
			break;
		case EXISTS:
		case FORALL:
			// result = translateQuantifier(expression);
			break;
		case INT_DIVIDE:
			// if (simplifyIntDivision) {
			// result = getIntDivInfo(
			// (NumericExpression) expression.argument(0),
			// (NumericExpression) expression.argument(1),
			// SymbolicOperator.INT_DIVIDE);
			// } else {
			// result = translateBinary(" DIV ",
			// (SymbolicExpression) expression.argument(0),
			// (SymbolicExpression) expression.argument(1));
			// }
			break;
		case LENGTH:
			// result = lengthOfArray((SymbolicExpression)
			// expression.argument(0));
			break;
		case LESS_THAN:
			result = translateBinary_BV(" BVLT ",
					(SymbolicExpression) expression.argument(0),
					(SymbolicExpression) expression.argument(1));
			break;
		case LESS_THAN_EQUALS:
			result = translateBinary_BV(" BVLE ",
					(SymbolicExpression) expression.argument(0),
					(SymbolicExpression) expression.argument(1));
			break;
		case MODULO:
			// if (simplifyIntDivision) {
			// result = getIntDivInfo(
			// (NumericExpression) expression.argument(0),
			// (NumericExpression) expression.argument(1),
			// SymbolicOperator.MODULO);
			// } else
			// result = translateBinary(" MOD ",
			// (SymbolicExpression) expression.argument(0),
			// (SymbolicExpression) expression.argument(1));
			break;
		case MULTIPLY:
			result = translateKeySet(" BVMULT ", getBVString(1), expression);
			break;
		case NEGATIVE:
			// result = translateNegative(expression);
			break;
		case NEQ:
			// result = translateNEQ(expression);
			break;
		case NOT:
			result = translateNot(expression);
			break;
		case OR:
			result = translateKeySet(" OR ", "FALSE", expression);
			break;
		case POWER:
			// result = translatePower(expression);
			break;
		case SUBTRACT:
			// result = translateBinary(" - ",
			// (SymbolicExpression) expression.argument(0),
			// (SymbolicExpression) expression.argument(1));
			break;
		case SYMBOLIC_CONSTANT:
			result = translateSymbolicConstant_BV((SymbolicConstant) expression,
					false);
			break;
		case TUPLE:
			// result = translateConcreteTuple(expression);
			break;
		case TUPLE_READ:
			// result = translateTupleRead(expression);
			break;
		case TUPLE_WRITE:
			// result = translateTupleWrite(expression);
			break;
		case UNION_EXTRACT:
			// result = translateUnionExtract(expression);
			break;
		case UNION_INJECT:
			// result = translateUnionInject(expression);
			break;
		case UNION_TEST:
			// result = translateUnionTest(expression);
			break;
		case LAMBDA:
			// result = translateLambda(expression);
			break;
		case NULL:
			result = null;
			break;
		default:
			throw new SARLInternalException(
					"unreachable: unknown operator: " + operator);
		}
		if (result == null)
			System.out.println(expression.toString());
		return result;
	}

	/**
	 * @param operator
	 *            The operator can be {@link SymbolicOperator.#MULTIPLY} or
	 *            {@link SymbolicOperator.#ADD}
	 * @param defaultValue
	 * @param expression
	 * @return
	 */
	private Translation translateKeySet_BV(String operator, String defaultValue,
			SymbolicExpression expression) {
		Translation translation;
		int size = expression.numArguments();

		if (size == 0) {
			return new Translation(new FastList<>(defaultValue));
		} else if (size == 1) {
			return translate_BV((SymbolicExpression) expression.argument(0));
		} else {
			Boolean divOrModole = false;
			List<Translation> translations = new ArrayList<>();
			FastList<String> result = new FastList<>();

			for (int i = 0; i < size; i++) {
				SymbolicExpression term = (SymbolicExpression) expression
						.argument(i);
				Translation t_bv = translate_BV(term);

				if (t_bv.getIsDivOrModulo()) {
					translations.add(t_bv.clone());
					divOrModole = true;
				}
				if (i == 0) {
					result.add(operator);
					result.add("(" + intLen + ", ");
					result.append(t_bv.getResult());
				} else if (i == 1) {
					result.add(", ");
					result.append(t_bv.getResult());
					result.add(")");
				} else {
					result.addFront("(" + intLen + ", ");
					result.addFront(operator);
					result.append(t_bv.getResult());
					result.add(")");
				}
			}
			if (divOrModole && (operator.equals(" BVPLUS ")
					|| operator.equals(" BVMULT "))) {
				int translationsNum = translations.size();
				FastList<String> newConstraint = new FastList<>();
				List<FastList<String>> newAuxVars = new ArrayList<FastList<String>>();

				if (translationsNum == 1) {
					Translation tempTranslation = translations.get(0);

					newConstraint = tempTranslation.getAuxConstraints().clone();
					newAuxVars.addAll(tempTranslation.getAuxVars());
				} else {
					Translation tempTranslation1 = translations.get(0);
					Translation tempTranslation2 = translations.get(1);

					newAuxVars.addAll(tempTranslation1.getAuxVars());
					newAuxVars.addAll(tempTranslation2.getAuxVars());
					newConstraint.add("(");
					newConstraint.append(tempTranslation1.getAuxConstraints());
					newConstraint.add(" AND ");
					newConstraint.append(tempTranslation2.getAuxConstraints());
					newConstraint.add(")");
				}
				translation = new Translation(result, true, newConstraint,
						newAuxVars);
			} else {
				translation = new Translation(result);
			}
			return translation;
		}
	}

	private String getBVString(long val) {
		char bitvector[] = new char[intLen];

		for (int i = intLen - 1; i >= 0; i--) {
			long bit = val % 2;

			if (bit == 1)
				bitvector[i] = '1';
			else
				bitvector[i] = '0';
			val = (val - bit) / 2;
		}

		return "0bin" + String.valueOf(bitvector);
	}

	/**
	 * Translate a logic function definition to this form : <code>
	 *   "function-name" : "function-type" = LAMBDA ("var-decls"): "function-body";
	 * </code>
	 * 
	 * @param logicFunction
	 */
	void translateLogicFunction(ProverFunctionInterpretation logicFunction) {
		String name = logicFunction.identifier;
		FastList<String> result = new FastList<>();

		// logic function name:
		result.add(name);
		result.add(": ");

		// builds the function type:
		List<SymbolicType> inputTypes = new LinkedList<>();
		SymbolicType outputType = logicFunction.definition.type();

		for (SymbolicConstant arg : logicFunction.parameters)
			inputTypes.add(arg.type());
		result.append(
				translateType(universe.functionType(inputTypes, outputType)));

		// translate function parameters:
		result.add(" = LAMBDA (");
		for (SymbolicConstant arg : logicFunction.parameters) {
			FastList<String> argType = translateType(arg.type());
			FastList<String> argName = new FastList<>(arg.name().getString());

			expressionMap.put(arg,
					new Translation(new FastList<>(arg.name().getString())));
			result.append(argName);
			result.add(" : ");
			result.append(argType);
			result.add(" ");
		}
		result.add(") : ");

		// disable this:
		simplifyIntDivision = false;

		Translation bodyTranslation = translate(logicFunction.definition);

		assert !bodyTranslation.getIsDivOrModulo()
				&& bodyTranslation.getAuxConstraints().isEmpty();
		assert bodyTranslation.getAuxVars()
				.isEmpty() : "logic function is not pure function";
		result.append(bodyTranslation.getResult());
		result.add(";\n");
		this.cvcDeclarations.append(result);
		// add the result to the expressionMap so that translating calls to this
		// function will not create function declarations again:
		this.expressionMap.put(logicFunction.function,
				new Translation(new FastList<>(name)));
	}

	// Exported methods...

	/**
	 * Returns the result of translating the symbolic expression specified at
	 * construction into the language of CVC. The result is returned as a
	 * {@link FastList}. The elements of that list are Strings, which,
	 * concatenated, yield the translation result. In most cases you never want
	 * to convert the result to a single string. Rather, you should iterate over
	 * this list, printing each element to the appropriate output stream.
	 * 
	 * @return result of translation of the specified symbolic expression
	 */
	FastList<String> getTranslation() {
		return cvcTranslation;
	}

	/**
	 * Returns the text of the declarations of the CVC symbols that occur in the
	 * translated expression. Typically, the declarations are submitted to CVC
	 * first, followed by a query or assertion of the translated expression.
	 * 
	 * @return the declarations of the CVC symbols
	 */
	FastList<String> getDeclarations() {
		return cvcDeclarations;
	}

	/**
	 * @return a unique instance, which is associated with this translator, of
	 *         {@link CVCPowerReal}
	 */
	CVCPowerReal getPowerRealTheory() {
		if (this.cvcPowerReal == null)
			this.cvcPowerReal = new CVCPowerReal(universe);
		return this.cvcPowerReal;
	}
}
