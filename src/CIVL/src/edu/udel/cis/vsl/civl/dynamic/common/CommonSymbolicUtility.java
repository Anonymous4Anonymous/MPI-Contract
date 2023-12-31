package edu.udel.cis.vsl.civl.dynamic.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import edu.udel.cis.vsl.civl.dynamic.IF.SymbolicUtility;
import edu.udel.cis.vsl.civl.dynamic.common.HeapAnalyzer.CIVLMemoryBlock;
import edu.udel.cis.vsl.civl.model.IF.CIVLInternalException;
import edu.udel.cis.vsl.civl.model.IF.CIVLSource;
import edu.udel.cis.vsl.civl.model.IF.CIVLTypeFactory;
import edu.udel.cis.vsl.civl.model.IF.CIVLUnimplementedFeatureException;
import edu.udel.cis.vsl.civl.model.IF.ModelFactory;
import edu.udel.cis.vsl.civl.state.IF.StateFactory;
import edu.udel.cis.vsl.civl.util.IF.Pair;
import edu.udel.cis.vsl.sarl.IF.Reasoner;
import edu.udel.cis.vsl.sarl.IF.SARLException;
import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;
import edu.udel.cis.vsl.sarl.IF.ValidityResult.ResultType;
import edu.udel.cis.vsl.sarl.IF.expr.ArrayElementReference;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NTReferenceExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericSymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.ReferenceExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.IF.expr.TupleComponentReference;
import edu.udel.cis.vsl.sarl.IF.expr.UnionMemberReference;
import edu.udel.cis.vsl.sarl.IF.number.IntegerNumber;
import edu.udel.cis.vsl.sarl.IF.object.IntObject;
import edu.udel.cis.vsl.sarl.IF.object.NumberObject;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject.SymbolicObjectKind;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicSequence;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicArrayType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicCompleteArrayType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicTupleType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType.SymbolicTypeKind;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicTypeSequence;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicUnionType;

public class CommonSymbolicUtility implements SymbolicUtility {

	/* *************************** Instance Fields ************************* */

	/**
	 * Symbolic universe for operations on symbolic expressions.
	 */
	private SymbolicUniverse universe;

	/**
	 * The type factory of the CIVL model.
	 */
	private CIVLTypeFactory typeFactory;

	/**
	 * Integer object 0.
	 */
	private IntObject zeroObj;

	/**
	 * Integer object 1.
	 */
	private IntObject oneObj;

	/**
	 * Integer object 2.
	 */
	private IntObject twoObj;

	/**
	 * Integer 0.
	 */
	private NumericExpression zero;

	/**
	 * Integer 1.
	 */
	private NumericExpression one;

	/**
	 * Symbolic dynamic type.
	 */
	private SymbolicTupleType dynamicType;

	/**
	 * The symbolic expression of boolean false.
	 */
	private BooleanExpression falseValue;

	/**
	 * The symbolic expression of boolean true.
	 */
	private BooleanExpression trueValue;

	/**
	 * The symbolic expression of NULL pointer.
	 */
	private SymbolicExpression nullPointer;

	/**
	 * The symbolic expression of undefined pointer, i.e., a pointer that has
	 * been deallocated.
	 */
	private SymbolicExpression undefinedPointer;

	/**
	 * The heap analyzer for heap related semantics.
	 */
	private HeapAnalyzer heapAnalyzer;

	/**
	 * The symbolic pointer type.
	 */
	private SymbolicTupleType pointerType;

	private final static String abstractGuard = "_guard_";

	private SymbolicType stringType;

	private SymbolicTupleType functionPointerType;

	private SymbolicExpression nullFunctionPointer;

	private StateFactory stateFactory;

	/* ***************************** Constructor *************************** */

	/**
	 * creates a new instance of symbolic utility.
	 * 
	 * @param universe
	 *            the symbolic universe to be used by the symbolic utility.
	 * @param modelFactory
	 *            the model factory to be used by the symbolic utility.
	 */
	public CommonSymbolicUtility(SymbolicUniverse universe,
			ModelFactory modelFactory, StateFactory stateFactory) {
		this.stateFactory = stateFactory;
		this.universe = universe;
		this.typeFactory = modelFactory.typeFactory();
		this.heapAnalyzer = new HeapAnalyzer(universe, this);
		dynamicType = typeFactory.dynamicSymbolicType();
		this.zeroObj = universe.intObject(0);
		this.oneObj = universe.intObject(1);
		this.twoObj = universe.intObject(2);
		zero = universe.integer(0);
		one = universe.integer(1);
		this.falseValue = universe.falseExpression();
		this.trueValue = universe.trueExpression();
		this.pointerType = this.typeFactory.pointerSymbolicType();
		this.functionPointerType = this.typeFactory
				.functionPointerSymbolicType();
		this.nullPointer = makePointer(-1, -1, universe.identityReference());
		this.nullFunctionPointer = makeFunctionPointer(-1, -1);
		this.undefinedPointer = modelFactory
				.undefinedValue(typeFactory.pointerSymbolicType());
		this.stringType = universe.arrayType(universe.characterType());
	}

	/* *********************** Package-Private Methods ********************* */

	/**
	 * Returns the list of ancestor references of a given reference expressions,
	 * in the bottom-up order, i.e., the first element of the list will be the
	 * parent of the reference.
	 * 
	 * @param ref
	 *            The reference expression whose ancestors are to be computed.
	 * @return the list ancestor references of the given reference.
	 */
	List<ReferenceExpression> ancestorsOfRef(ReferenceExpression ref) {
		if (ref.isIdentityReference())
			return new ArrayList<>();
		else {
			List<ReferenceExpression> result;

			result = ancestorsOfRef(((NTReferenceExpression) ref).getParent());
			result.add(ref);
			return result;
		}
	}

	/* ********************** Private Methods ************************** */
	/**
	 * Get the element in literal domain pointed by the given index.
	 * 
	 * @param domValue
	 *            The symbolic expression of the domain
	 * @param index
	 *            The index points to the position of the returned element
	 * @return The element in literal domain pointed by the given index
	 */
	private SymbolicExpression getEleInLiteralDomain(
			SymbolicExpression literalDom, int index) {
		SymbolicExpression element;

		try {
			element = universe.arrayRead(literalDom, universe.integer(index));
		} catch (SARLException e) {
			throw new CIVLInternalException("read literal domain out of bound",
					(CIVLSource) null);
		}

		return element;
	}

	/**
	 * Returns the size of a given regular range.
	 * 
	 * @param range
	 *            The regular range whose size is to be computed.
	 * @return the size of the given regular range.
	 */
	private NumericExpression getRegRangeSize(SymbolicExpression range) {
		NumericExpression low = (NumericExpression) universe.tupleRead(range,
				this.zeroObj);
		NumericExpression high = (NumericExpression) universe.tupleRead(range,
				oneObj);
		NumericExpression step = (NumericExpression) universe.tupleRead(range,
				this.twoObj);
		NumericExpression size = universe.subtract(high, low);
		NumericExpression remainder;
		BooleanExpression claim = universe.lessThan(step, zero);
		ResultType resultType = universe.reasoner(this.trueValue).valid(claim)
				.getResultType();

		if (resultType == ResultType.YES) {
			step = universe.minus(step);
			size = universe.minus(size);
		}
		remainder = universe.modulo(size, step);
		size = universe.subtract(size, remainder);
		size = universe.divide(size, step);
		size = universe.add(size, this.one);
		return size;
	}

	/**
	 * Are the two given references disjoint?
	 * 
	 * @param ref1
	 *            The first reference expression.
	 * @param ref2
	 *            The second reference expression.
	 * @return True iff the two given references do NOT have any intersection.
	 */
	private boolean isDisjoint(ReferenceExpression ref1,
			ReferenceExpression ref2) {
		List<ReferenceExpression> ancestors1, ancestors2;
		int numAncestors1, numAncestors2, minNum;

		ancestors1 = this.ancestorsOfRef(ref1);
		ancestors2 = this.ancestorsOfRef(ref2);
		numAncestors1 = ancestors1.size();
		numAncestors2 = ancestors2.size();
		minNum = numAncestors1 <= numAncestors2 ? numAncestors1 : numAncestors2;
		for (int i = 0; i < minNum; i++) {
			ReferenceExpression ancestor1 = ancestors1.get(i),
					ancestor2 = ancestors2.get(i);

			if (!ancestor1.equals(ancestor2))
				return true;
		}
		return false;
	}

	/**
	 * Is the given reference applicable to the specified symbolic type?
	 * 
	 * @param ref
	 *            The reference expression to be checked.
	 * @param type
	 *            The symbolic type specified.
	 * @return True iff the given reference is applicable to the specified
	 *         symbolic type
	 */
	private boolean isValidRefOfType(ReferenceExpression ref,
			SymbolicType type) {
		// sub-references are ordered starting from the IdentityReference
		LinkedList<ReferenceExpression> subRefOrderedSet = new LinkedList<>();

		subRefOrderedSet.addFirst(ref);
		while (ref instanceof NTReferenceExpression) {
			ReferenceExpression parent = ((NTReferenceExpression) ref)
					.getParent();

			subRefOrderedSet.addFirst(parent);
			ref = parent;
		}

		ReferenceExpression subRef;
		SymbolicType subType = type;

		// Check type against references:
		while (!subRefOrderedSet.isEmpty()) {
			subRef = subRefOrderedSet.removeFirst();
			switch (subRef.referenceKind()) {
				case ARRAY_ELEMENT :
					if (subType.typeKind() == SymbolicTypeKind.ARRAY) {
						subType = ((SymbolicArrayType) subType).elementType();
						continue;
					} else
						return false;
				case IDENTITY :
					continue;
				case TUPLE_COMPONENT :
					if (subType.typeKind() == SymbolicTypeKind.TUPLE) {
						int fieldIdx = ((TupleComponentReference) subRef)
								.getIndex().getInt();
						SymbolicTypeSequence fieldTypes = ((SymbolicTupleType) subType)
								.sequence();

						if (fieldTypes.numTypes() <= fieldIdx)
							return false;
						subType = fieldTypes.getType(fieldIdx);
						continue;
					} else
						return false;
				case UNION_MEMBER :
					if (subType.typeKind() == SymbolicTypeKind.UNION) {
						int fieldIdx = ((UnionMemberReference) subRef)
								.getIndex().getInt();
						SymbolicTypeSequence fieldTypes = ((SymbolicUnionType) subType)
								.sequence();

						if (fieldTypes.numTypes() <= fieldIdx)
							return false;
						subType = fieldTypes.getType(fieldIdx);
						continue;
					} else
						return false;
				default :
					return false;
			}
		}
		return true;
	}

	/**
	 * Combines two references by using one as the parent of the other.
	 * 
	 * @param parent
	 *            The reference to be used as the parent.
	 * @param ref
	 *            The reference to be used as the base.
	 * @return A new reference which is the combination of the given two
	 *         references.
	 */
	private ReferenceExpression makeParentOf(ReferenceExpression parent,
			ReferenceExpression ref) {
		if (ref.isIdentityReference())
			return parent;
		else {
			ReferenceExpression myParent = makeParentOf(parent,
					((NTReferenceExpression) ref).getParent());

			if (ref.isArrayElementReference())
				return universe.arrayElementReference(myParent,
						((ArrayElementReference) ref).getIndex());
			else if (ref.isTupleComponentReference())
				return universe.tupleComponentReference(myParent,
						((TupleComponentReference) ref).getIndex());
			else
				return universe.unionMemberReference(myParent,
						((UnionMemberReference) ref).getIndex());
		}
	}

	/**
	 * Computes the components contained by a given reference expression.
	 * 
	 * @param ref
	 *            The reference expression whose components are to be computed.
	 * @return The components of the reference.
	 */
	private List<ReferenceExpression> referenceComponents(
			ReferenceExpression ref) {
		List<ReferenceExpression> components = new ArrayList<>();

		if (!ref.isIdentityReference()) {
			components.add(ref);
			components.addAll(referenceComponents(
					((NTReferenceExpression) ref).getParent()));
		}
		return components;
	}

	/**
	 * Checks if the given value in a range has a subsequence. e.g. range: from
	 * 0 to 10 step 2. Given a value 8 and it has a subsequence 10.
	 * 
	 * @param range
	 * @param value
	 * @return
	 */
	private BooleanExpression rangeHasNext(SymbolicExpression range,
			SymbolicExpression value) {
		NumericExpression step = this.getStepOfRegularRange(range);
		SymbolicExpression next = universe.add((NumericExpression) value, step);

		return this.isInRange(next, range);
	}

	/* ********************* Methods from SymbolicUtility ******************* */

	/**
	 * Given a symbolic expression of type array of char, returns a string
	 * representation. If it is a concrete array of char consisting of concrete
	 * characters, this will be the obvious string. Otherwise the result is
	 * something readable but unspecified.
	 */
	@Override
	public StringBuffer charArrayToString(CIVLSource source,
			SymbolicExpression charArray, int startIndex, boolean forPrint) {
		StringBuffer result = new StringBuffer();
		int numChars = charArray.numArguments();

		if (charArray.operator() != SymbolicOperator.ARRAY)
			throw new CIVLUnimplementedFeatureException(
					"extracting string from a non-concrete array of characters",
					source);
		// assert charArray.operator() == SymbolicOperator.ARRAY;
		// ignoring the '\0' at the end of the string.
		for (int j = startIndex; j < numChars; j++) {
			SymbolicExpression charExpr = (SymbolicExpression) charArray
					.argument(j);
			Character theChar = universe.extractCharacter(charExpr);

			if (theChar == null) {
				result.append(charArray);
				return result;
				// throw new CIVLUnimplementedFeatureException(
				// "non-concrete character in string at position " + j,
				// source);
			}
			// if (theChar != '\0') {
			if (forPrint) {
				String theCharToString;
				switch (theChar) {
					case '\0' :
						theCharToString = "\0";
						break;
					case '\u000C' :
						theCharToString = "\\f";
						break;
					case '\u0007' :
						theCharToString = "\\a";
						break;
					case '\b' :
						theCharToString = "\\b";
						break;
					case '\n' :
						theCharToString = "\\n";
						break;
					case '\t' :
						theCharToString = "\\t";
						break;
					case '\r' :
						theCharToString = "\\r";
						break;
					default :
						theCharToString = theChar.toString();
				}
				result.append(theCharToString);
			} else {
				result.append(theChar);
			}
			// }
		}
		return result;
	}

	/**
	 * A pointer can be only concrete for the current implementation of CIVL,
	 * because the only way to make one is through <code>$malloc</code> or
	 * <code>&</code>.
	 */
	@Override
	public BooleanExpression contains(SymbolicExpression pointer1,
			SymbolicExpression pointer2) {
		ReferenceExpression ref1 = (ReferenceExpression) universe
				.tupleRead(pointer1, twoObj);
		ReferenceExpression ref2 = (ReferenceExpression) universe
				.tupleRead(pointer2, twoObj);
		SymbolicExpression scope1 = universe.tupleRead(pointer1, zeroObj);
		SymbolicExpression scope2 = universe.tupleRead(pointer2, zeroObj);
		SymbolicExpression vid1 = universe.tupleRead(pointer1, oneObj);
		SymbolicExpression vid2 = universe.tupleRead(pointer2, oneObj);
		List<ReferenceExpression> refComps1 = new ArrayList<>();
		List<ReferenceExpression> refComps2 = new ArrayList<>();
		int numRefs1, numRefs2, offset;
		BooleanExpression result = this.trueValue;

		if (ref1.isIdentityReference() && ref2.isIdentityReference()) {
			return universe.equals(ref1, ref2);
		}
		if (ref2.isIdentityReference() // second contains first
				|| universe.equals(scope1, scope2).isFalse() // different scope
																// id
				|| universe.equals(vid1, vid2).isFalse()) // different vid
			return this.falseValue;
		if (ref1.isIdentityReference() && !ref2.isIdentityReference())
			return this.trueValue;
		refComps1 = referenceComponents(ref1);
		refComps2 = referenceComponents(ref2);
		numRefs1 = refComps1.size();
		numRefs2 = refComps2.size();
		if (numRefs1 > numRefs2)
			return this.falseValue;
		offset = numRefs2 - numRefs1;
		for (int i = offset; i < numRefs1; i++) {
			result = universe.and(result, universe.equals(refComps1.get(i),
					refComps2.get(i + offset)));
		}
		return result;
	}

	@Override
	public int extractInt(CIVLSource source, NumericExpression expression) {
		if (expression.operator() == SymbolicOperator.CONCRETE) {
			SymbolicObject object = expression.argument(0);

			if (object.symbolicObjectKind() == SymbolicObjectKind.NUMBER)
				return ((IntegerNumber) ((NumberObject) object).getNumber())
						.intValue();
		}
		throw new CIVLInternalException(
				"Unable to extract concrete int from " + expression, source);
	}

	@Override
	public int extractIntField(CIVLSource source, SymbolicExpression tuple,
			IntObject fieldIndex) {
		NumericExpression field = (NumericExpression) universe.tupleRead(tuple,
				fieldIndex);

		return this.extractInt(source, field);
	}

	@Override
	public int getArrayIndex(CIVLSource source, SymbolicExpression pointer)
			throws CIVLInternalException {
		int int_arrayIndex;

		if (pointer.type() instanceof SymbolicArrayType) {
			int_arrayIndex = 0;
		} else {
			ArrayElementReference arrayRef;
			NumericExpression arrayIndex;

			try {
				arrayRef = (ArrayElementReference) getSymRef(pointer);
			} catch (ClassCastException e) {
				throw new CIVLInternalException(
						"pointer is not a array element reference", source);
			}
			arrayIndex = arrayRef.getIndex();
			int_arrayIndex = extractInt(source, arrayIndex);
		}
		return int_arrayIndex;
	}

	@Override
	public SymbolicExpression getScopeValue(SymbolicExpression pointer) {
		return universe.tupleRead(pointer, zeroObj);
	}

	@Override
	public ReferenceExpression getSymRef(SymbolicExpression pointer) {
		SymbolicExpression result = universe.tupleRead(pointer, twoObj);

		assert result instanceof ReferenceExpression;
		return (ReferenceExpression) result;
	}

	@Override
	public SymbolicExpression setSymRef(SymbolicExpression pointer,
			ReferenceExpression symRef) {
		return universe.tupleWrite(pointer, twoObj, symRef);
	}

	@Override
	public int getVariableId(CIVLSource source, SymbolicExpression pointer) {
		return extractIntField(source, pointer, oneObj);
	}

	@Override
	public SymbolicExpression heapMemUnit(SymbolicExpression pointer) {
		return this.heapAnalyzer.heapMemUnit(pointer);
	}

	@Override
	public SymbolicConstant invalidHeapObject(SymbolicType heapObjectType) {
		return heapAnalyzer.invalidHeapObject(heapObjectType);
	}

	@Override
	public boolean isDisjointWith(SymbolicExpression pointer1,
			SymbolicExpression pointer2) {
		if (pointer1.equals(pointer2))
			return false;
		{
			SymbolicExpression scope1 = universe.tupleRead(pointer1, zeroObj),
					var1 = universe.tupleRead(pointer1, oneObj);
			SymbolicExpression scope2 = universe.tupleRead(pointer2, zeroObj),
					var2 = universe.tupleRead(pointer2, oneObj);
			ReferenceExpression ref1 = (ReferenceExpression) universe
					.tupleRead(pointer1, twoObj);
			ReferenceExpression ref2 = (ReferenceExpression) universe
					.tupleRead(pointer2, twoObj);

			if (!scope1.equals(scope2))
				return true;
			if (!var1.equals(var2))
				return true;
			if (ref1.equals(ref2))
				return false;
			return isDisjoint(ref1, ref2);
		}
	}

	@Override
	public boolean isEmptyHeap(SymbolicExpression heapValue) {
		return heapAnalyzer.isEmptyHeap(heapValue);
	}

	@Override
	public boolean isMallocPointer(CIVLSource source,
			SymbolicExpression pointer) {
		return heapAnalyzer.isHeapAtomicObjectPointer(source, pointer);
	}

	@Override
	public boolean isInitialized(SymbolicExpression value) {
		if (value.isNull())
			return false;
		return true;
	}

	@Override
	public BooleanExpression isInRange(SymbolicExpression value,
			SymbolicExpression range) {
		SymbolicExpression high = universe.tupleRead(range, oneObj);
		SymbolicExpression step = universe.tupleRead(range, twoObj);
		BooleanExpression positiveStep = universe.lessThan(zero,
				(NumericExpression) step);
		BooleanExpression negativeStep = universe
				.lessThan((NumericExpression) step, zero);
		BooleanExpression positiveStepResult = universe.and(positiveStep,
				universe.lessThanEquals((NumericExpression) value,
						(NumericExpression) high));
		BooleanExpression negativeStepResult = universe.and(negativeStep,
				universe.lessThanEquals((NumericExpression) high,
						(NumericExpression) value));

		if (positiveStep.isTrue())
			return universe.lessThanEquals((NumericExpression) value,
					(NumericExpression) high);
		if (negativeStep.isTrue())
			return universe.lessThanEquals((NumericExpression) high,
					(NumericExpression) value);
		return universe.or(positiveStepResult, negativeStepResult);
	}

	@Override
	public BooleanExpression isInRange(NumericExpression value,
			NumericExpression low, NumericExpression upper,
			NumericExpression step) {
		return universe.and(Arrays.asList(universe.lessThanEquals(low, value),
				universe.lessThan(value, upper),
				universe.equals(
						universe.modulo(universe.subtract(value, low), step),
						zero)));
	}

	@Override
	public boolean isInvalidHeapObject(SymbolicExpression heapObject) {
		return heapAnalyzer.isInvalidHeapObject(heapObject);
	}

	@Override
	public boolean isNullPointer(SymbolicExpression pointer) {
		return universe.equals(this.nullPointer, pointer).isTrue();
	}

	@Override
	public boolean isPointerToHeap(SymbolicExpression pointer) {
		return heapAnalyzer.isPointerToHeap(pointer);
	}

	@Override
	public boolean isValidRefOf(ReferenceExpression ref,
			SymbolicType objectValueType) {
		return isValidRefOfType(ref, objectValueType);
	}

	@Override
	public SymbolicExpression makePointer(int scopeId, int varId,
			ReferenceExpression symRef) {
		SymbolicExpression scopeField = stateFactory.scopeValue(scopeId);
		SymbolicExpression varField = universe.integer(varId);
		SymbolicExpression result = universe.tuple(this.pointerType,
				Arrays.asList(new SymbolicExpression[]{scopeField, varField,
						symRef}));

		return result;
	}

	@Override
	public SymbolicExpression makePointer(SymbolicExpression oldPointer,
			ReferenceExpression symRef) {
		return universe.tupleWrite(oldPointer, this.twoObj, symRef);
	}

	@Override
	public SymbolicExpression extendPointer(SymbolicExpression objectPointer,
			ReferenceExpression reference) {
		ReferenceExpression objRef = (ReferenceExpression) universe
				.tupleRead(objectPointer, twoObj);
		SymbolicExpression scope = universe.tupleRead(objectPointer, zeroObj);
		SymbolicExpression vid = universe.tupleRead(objectPointer, oneObj);

		if (!objRef.isIdentityReference())
			reference = makeParentOf(objRef, reference);
		return universe.tuple(pointerType,
				Arrays.asList(scope, vid, reference));
	}

	@Override
	public SymbolicExpression newArray(BooleanExpression context,
			SymbolicType elementValueType, NumericExpression length,
			SymbolicExpression eleValue) {
		Reasoner reasoner = universe.reasoner(context);
		IntegerNumber length_number = (IntegerNumber) reasoner
				.extractNumber(length);

		if (length_number != null) {
			int length_int = length_number.intValue();
			List<SymbolicExpression> values = new ArrayList<>(length_int);

			for (int i = 0; i < length_int; i++)
				values.add(eleValue);
			return universe.array(elementValueType, values);
		} else {
			NumericSymbolicConstant index = (NumericSymbolicConstant) universe
					.symbolicConstant(universe.stringObject("i"),
							universe.integerType());
			SymbolicExpression arrayEleFunction = universe.lambda(index,
					eleValue);
			SymbolicCompleteArrayType arrayValueType = universe
					.arrayType(elementValueType, length);

			return universe.arrayLambda(arrayValueType, arrayEleFunction);
		}
	}

	@Override
	public SymbolicExpression nullPointer() {
		return this.nullPointer;
	}

	@Override
	public SymbolicExpression parentPointer(SymbolicExpression pointer) {
		ReferenceExpression symRef = getSymRef(pointer);

		assert !symRef.isNullReference() && !symRef.isIdentityReference();
		return setSymRef(pointer, ((NTReferenceExpression) symRef).getParent());
	}

	@Override
	public ReferenceExpression referenceOfPointer(SymbolicExpression pointer) {
		ReferenceExpression ref = (ReferenceExpression) universe
				.tupleRead(pointer, twoObj);

		if (this.isPointerToHeap(pointer)) {
			Pair<ReferenceExpression, Integer> refResult = heapAnalyzer
					.heapReference(ref, true);

			if (refResult.right == 3)
				return universe.identityReference();
			else
				return refResult.left;
		} else
			return ref;
	}

	@Override
	public ReferenceExpression referenceToHeapMemUnit(
			SymbolicExpression pointer) {
		return this.heapAnalyzer.referenceToHeapMemUnit(pointer);
	}

	@Override
	public SymbolicExpression undefinedPointer() {
		return this.undefinedPointer;
	}

	@Override
	public ReferenceExpression makeArrayElementReference(
			ReferenceExpression arrayReference,
			NumericExpression[] newIndices) {
		int dimension = newIndices.length;
		ReferenceExpression newRef;

		newRef = arrayReference;
		for (int i = 0; i < dimension; i++) {
			newRef = universe.arrayElementReference(newRef, newIndices[i]);
		}
		return newRef;
	}

	/* *********************** Package-Private Methods ********************* */
	@Override
	public NumericExpression[] arraySlicesSizes(
			NumericExpression[] array_extents) {
		int dim = array_extents.length;
		NumericExpression[] sliceSizes = new NumericExpression[dim];
		NumericExpression sliceSize = one;

		for (int i = dim; --i >= 0;) {
			sliceSizes[i] = sliceSize;
			sliceSize = universe.multiply(sliceSize, array_extents[i]);
		}
		return sliceSizes;
	}

	@Override
	public NumericExpression[] arrayDimensionExtents(
			SymbolicCompleteArrayType arrayType) {
		int dimension, i = 0;
		NumericExpression[] extents;

		dimension = arrayType.dimensions();
		extents = new NumericExpression[dimension];
		do {
			extents[i++] = arrayType.extent();
			if (i < dimension)
				arrayType = (SymbolicCompleteArrayType) arrayType.elementType();
			else
				break;
		} while (true);
		return extents;
	}

	@Override
	public SymbolicExpression arrayRootPtr(SymbolicExpression arrayPtr) {
		SymbolicExpression arrayRootPtr = arrayPtr;

		if (heapAnalyzer.isPointerToHeap(arrayPtr)) {
			// Since the heap is modeled as a tuple of array of array of T, the
			// parent searching must stop once the pointer already points to the
			// first element of an heap memory block (i.e. the block of memory
			// allocated by one malloc call).
			while (getSymRef(arrayRootPtr).isArrayElementReference()
					&& !isPointer2MemoryBlock(arrayRootPtr))
				arrayRootPtr = parentPointer(arrayRootPtr);

			return arrayRootPtr;
		} else {
			while (getSymRef(arrayRootPtr).isArrayElementReference())
				arrayRootPtr = parentPointer(arrayRootPtr);

			return arrayRootPtr;
		}
	}

	@Override
	public NumericExpression[] extractArrayIndicesFrom(
			SymbolicExpression pointerToArrayElement) {
		Stack<NumericExpression> tmpStack = new Stack<>();
		NumericExpression[] indices;
		ReferenceExpression ref = getSymRef(pointerToArrayElement);
		int size;

		if (!heapAnalyzer.isPointerToHeap(pointerToArrayElement))
			while (ref.isArrayElementReference()) {
				ArrayElementReference tmpEleRef = (ArrayElementReference) ref;

				tmpStack.push(tmpEleRef.getIndex());
				ref = tmpEleRef.getParent();
			}
		else {
			SymbolicExpression tmpPointer = pointerToArrayElement;

			while (ref.isArrayElementReference()
					&& !isPointer2MemoryBlock(tmpPointer)) {
				ArrayElementReference tmpEleRef = (ArrayElementReference) ref;

				tmpStack.push(tmpEleRef.getIndex());
				ref = tmpEleRef.getParent();
				tmpPointer = setSymRef(tmpPointer, ref);
			}
		}
		size = tmpStack.size();
		indices = new NumericExpression[size];
		for (int i = 0; !tmpStack.isEmpty();)
			indices[i++] = tmpStack.pop();
		return indices;
	}

	/*
	 * ************************ Domain Operations ****************************
	 */
	@Override
	public Iterator<List<SymbolicExpression>> getDomainIterator(
			SymbolicExpression domain) {
		Iterator<List<SymbolicExpression>> domIterator;
		SymbolicExpression domainUnionField = universe.tupleRead(domain,
				twoObj);
		NumericExpression dim = (NumericExpression) universe.tupleRead(domain,
				zeroObj);
		final int concreteDim = ((IntegerNumber) universe.extractNumber(dim))
				.intValue();

		if (isRectangularDomain(domain)) {
			final SymbolicExpression recDomainField = universe
					.unionExtract(zeroObj, domainUnionField);
			final List<SymbolicExpression> domStartPos = this
					.getDomainInit(domain);

			// anonymous class of iterator
			domIterator = new Iterator<List<SymbolicExpression>>() {
				private List<SymbolicExpression> startPos = domStartPos;
				private List<SymbolicExpression> currentPos = null;
				private SymbolicExpression recDom = recDomainField;
				private int dim = concreteDim;

				@Override
				public boolean hasNext() {
					BooleanExpression hasNext = universe.falseExpression();

					if (this.currentPos == null) {
						if (startPos == null)
							return false;
						else
							return true;
					} else {
						for (int i = 0; i < this.dim; i++) {
							SymbolicExpression range = universe.arrayRead(
									this.recDom, universe.integer(i));
							BooleanExpression rangeIHasNext = rangeHasNext(
									range, currentPos.get(i));

							hasNext = universe.or(hasNext, rangeIHasNext);
						}
						return universe.extractBoolean(hasNext);
					}
				}

				@Override
				public List<SymbolicExpression> next() {
					if (this.currentPos == null)
						return (this.currentPos = this.startPos);
					else {
						this.currentPos = getNextInRectangularDomain(
								this.recDom, this.currentPos, this.dim);
						return this.currentPos;
					}
				}

				@Override
				public void remove() {
					throw new CIVLInternalException(
							"Remove elements in domain is not support yet",
							(CIVLSource) null);
				}
			};
		} else if (this.isLiteralDomain(domain)) {
			final SymbolicExpression literalDomainField = universe
					.unionExtract(oneObj, domainUnionField);

			// anonymous class of iterator
			domIterator = new Iterator<List<SymbolicExpression>>() {
				private int currentPos = -1;
				private SymbolicExpression literalDom = literalDomainField;
				private int literalDomainSize = ((IntegerNumber) universe
						.extractNumber(universe.length(literalDom))).intValue();
				private int dim = concreteDim;

				@Override
				public boolean hasNext() {
					return ((literalDomainSize > (currentPos + 1)
							&& (currentPos + 1) >= 0));
				}

				@Override
				public List<SymbolicExpression> next() {
					SymbolicExpression element;
					List<SymbolicExpression> literalElement = new LinkedList<>();

					this.currentPos++;
					element = getEleInLiteralDomain(this.literalDom,
							this.currentPos);
					for (int i = 0; i < this.dim; i++) {
						literalElement.add(universe.arrayRead(element,
								universe.integer(i)));
					}
					return literalElement;
				}

				@Override
				public void remove() {
					throw new CIVLInternalException(
							"Remove elements in domain is not support yet",
							(CIVLSource) null);
				}
			};
		} else {
			throw new CIVLInternalException(
					"$domain type other than rectangular domain or literal domain is not supported",
					(CIVLSource) null);
		}

		return domIterator;
	}

	@Override
	public List<SymbolicExpression> getDomainInit(SymbolicExpression domValue) {
		SymbolicExpression domainUnionField = universe.tupleRead(domValue,
				twoObj);
		NumericExpression dim = (NumericExpression) universe.tupleRead(domValue,
				zeroObj);
		int concreteDim = ((IntegerNumber) universe.extractNumber(dim))
				.intValue();
		List<SymbolicExpression> varValues = new ArrayList<>(concreteDim);

		if (this.isRectangularDomain(domValue)) {
			SymbolicExpression recDomainField = universe.unionExtract(zeroObj,
					domainUnionField);
			SymbolicExpression range;

			if (universe.length(recDomainField).isZero())
				return null;
			for (int i = 0; i < concreteDim; i++) {
				range = universe.arrayRead(recDomainField, universe.integer(i));

				varValues.add(this.getLowOfRegularRange(range));
			}
			return varValues;
		} else {
			SymbolicExpression literalDomainField = universe
					.unionExtract(oneObj, domainUnionField);

			if (universe.length(literalDomainField).isZero())
				return null;
			else {
				SymbolicExpression firstElement = universe
						.arrayRead(literalDomainField, zero);

				for (int i = 0; i < concreteDim; i++)
					varValues.add(universe.arrayRead(firstElement,
							universe.integer(i)));
				return varValues;
			}
		}
	}

	@Override
	public boolean isLiteralDomain(SymbolicExpression domValue) {
		// Domain type is a tuple type{dim, field, union{...}}
		SymbolicType type = domValue.type();
		NumericExpression unionField; // Indicates weather rectangular or
										// literal
		int concreteUnionField;

		assert (type instanceof SymbolicTupleType);
		// The following 2 casts should be safe as long as domValue has $domian
		// type.
		unionField = (NumericExpression) universe.tupleRead(domValue, oneObj);
		concreteUnionField = ((IntegerNumber) universe
				.extractNumber(unionField)).intValue();
		if (concreteUnionField == 1)
			return true;
		return false;
	}

	@Override
	public NumericExpression getDomainSize(SymbolicExpression domain) {
		SymbolicExpression domainUnionField; // union{rec,literal}

		domainUnionField = universe.tupleRead(domain, twoObj);
		if (this.isRectangularDomain(domain)) {
			SymbolicExpression recDomainField; // array of ranges
			NumericExpression size = universe.oneInt();// Init size
			NumericExpression dim = (NumericExpression) universe
					.tupleRead(domain, zeroObj);
			int concreteDim;

			concreteDim = ((IntegerNumber) universe.extractNumber(dim))
					.intValue();
			recDomainField = universe.unionExtract(zeroObj, domainUnionField);
			for (int i = 0; i < concreteDim; i++) {
				SymbolicExpression range = universe.arrayRead(recDomainField,
						universe.integer(i));

				size = universe.multiply(size, this.getRegRangeSize(range));
			}
			return size;
		} else if (this.isLiteralDomain(domain)) {
			// literal domain is an array of array of integers. Also can be
			// explained as array of elements(elements are arrays of integers).
			SymbolicExpression literalDomainField = universe
					.unionExtract(oneObj, domainUnionField);

			return universe.length(literalDomainField);
		} else
			throw new CIVLInternalException(
					"The argument: 'domain' of the function getDomainSize() is incorrect",
					(CIVLSource) null);
	}

	@Override
	public SymbolicType getDomainElementType(SymbolicExpression domain) {
		NumericExpression dim;
		SymbolicArrayType domainElementType;

		dim = (NumericExpression) universe.tupleRead(domain, zeroObj);
		domainElementType = universe.arrayType(universe.integerType(), dim);
		return domainElementType;
	}

	@Override
	public boolean recDomainHasNext(SymbolicExpression recDomainUnion,
			int concreteDim, List<SymbolicExpression> domElement) {
		boolean hasNext = false;
		SymbolicExpression range;

		for (int i = 0; i < concreteDim; i++) {
			boolean rangeHasNext = false;

			range = universe.arrayRead(recDomainUnion, universe.integer(i));
			rangeHasNext = rangeHasNext(range, domElement.get(i)).isTrue();
			hasNext = (rangeHasNext | hasNext);
		}

		return hasNext;
	}

	@Override
	public int literalDomainSearcher(SymbolicExpression literalDomain,
			List<SymbolicExpression> literalDomElement, int dim) {
		NumericExpression literalDomLength = universe.length(literalDomain);
		int concreteLength;

		concreteLength = ((IntegerNumber) universe
				.extractNumber(literalDomLength)).intValue();
		for (int i = 0; i < concreteLength; i++) {
			SymbolicExpression symElement = universe.arrayRead(literalDomain,
					universe.integer(i));

			for (int j = 0; j < dim; j++) {
				SymbolicExpression elementComp = universe.arrayRead(symElement,
						universe.integer(j));

				if (elementComp.equals(literalDomElement.get(j)))
					if (j == dim - 1) {
						return i;
					} else
						continue;
				else
					break;
			}
		}
		return -1;
	}

	@Override
	public List<SymbolicExpression> getNextInRectangularDomain(
			SymbolicExpression recDom, List<SymbolicExpression> varValues,
			int concreteDim) {
		SymbolicExpression recDomainField = recDom;
		SymbolicExpression range;
		SymbolicExpression newValues[] = new SymbolicExpression[concreteDim];

		for (int i = concreteDim - 1; i >= 0; i--) {
			SymbolicExpression current = varValues.get(i);
			BooleanExpression rangeHasNext;

			range = universe.arrayRead(recDomainField, universe.integer(i));
			rangeHasNext = rangeHasNext(range, current);
			if (rangeHasNext.isTrue()) {
				newValues[i] = universe.add((NumericExpression) current,
						this.getStepOfRegularRange(range));
				for (int j = i - 1; j >= 0; j--) {
					newValues[j] = varValues.get(j);
				}
				break;
			} else {
				newValues[i] = this.getLowOfRegularRange(range);
			}
		}
		return Arrays.asList(newValues);
	}

	@Override
	public boolean isEmptyDomain(SymbolicExpression domain, int dim,
			CIVLSource source) {
		SymbolicExpression domUnion = universe.tupleRead(domain, twoObj);

		if (this.isLiteralDomain(domain)) {
			SymbolicExpression literalDom = universe.unionExtract(oneObj,
					domUnion);
			NumericExpression domLength = universe.length(literalDom);

			// array length can never be negative and here it also should always
			// be concrete.
			if (domLength.isZero())
				return true;
			else
				return false;
		} else if (this.isRectangularDomain(domain)) {
			SymbolicExpression recDom = universe.unionExtract(zeroObj,
					domUnion);

			for (int i = 0; i < dim; i++) {
				SymbolicExpression range = universe.arrayRead(recDom,
						universe.integer(i));

				if (!this.getRegRangeSize(range).isZero())
					return false;
			}
			return true;
		} else
			throw new CIVLInternalException(
					"A domain object is neither a rectangular domain nor a literal domain",
					source);
	}

	@Override
	public Pair<NumericExpression, NumericExpression> arithmeticIntDivide(
			NumericExpression dividend, NumericExpression denominator) {
		NumericExpression quotient, remainder;

		assert dividend.type().isInteger();
		assert denominator.type().isInteger();
		quotient = universe.divide(dividend, denominator);
		remainder = universe.subtract(dividend,
				universe.multiply(quotient, denominator));
		return new Pair<>(quotient, remainder);
	}

	@Override
	public SymbolicTupleType dynamicType() {
		return this.dynamicType;
	}

	@Override
	public boolean isRectangularDomain(SymbolicExpression domain) {
		// a domain is the tuple (dimension, type, value)
		return universe.tupleRead(domain, oneObj).isZero();
	}

	@Override
	public boolean isRegularRange(SymbolicExpression range) {
		if (range.type().toString().equals("$regular_range"))
			return true;
		return false;
	}

	@Override
	public SymbolicExpression getRangeOfRectangularDomain(
			SymbolicExpression domain, int index) {
		if (!isRectangularDomain(domain))
			return null;

		SymbolicExpression ranges = universe.unionExtract(zeroObj,
				universe.tupleRead(domain, twoObj));

		return universe.arrayRead(ranges, universe.integer(index));
	}

	@Override
	public NumericExpression getHighOfRegularRange(SymbolicExpression range) {
		return (NumericExpression) universe.tupleRead(range, oneObj);
	}

	@Override
	public NumericExpression getLowOfRegularRange(SymbolicExpression range) {
		return (NumericExpression) universe.tupleRead(range, zeroObj);
	}

	@Override
	public NumericExpression getStepOfRegularRange(SymbolicExpression range) {
		return (NumericExpression) universe.tupleRead(range, twoObj);

	}

	@Override
	public NumericExpression getDimensionOf(SymbolicExpression domain) {
		return (NumericExpression) universe.tupleRead(domain, zeroObj);
	}

	@Override
	public BooleanExpression[] getConjunctiveClauses(BooleanExpression clause) {
		SymbolicOperator operator = clause.operator();
		BooleanExpression[] result;

		if (operator != SymbolicOperator.AND) {
			result = new BooleanExpression[1];
			result[0] = clause;
		} else {
			int numArgs = clause.numArguments();

			result = new BooleanExpression[numArgs];
			for (int i = 0; i < numArgs; i++) {
				result[i] = (BooleanExpression) clause.argument(i);
			}
		}
		return result;
	}

	@Override
	public SymbolicExpression getAbstractGuardOfFunctionCall(String library,
			String function, SymbolicExpression[] argumentValues) {
		String functionName = abstractGuard + library + "_" + function;
		List<SymbolicType> argumentTypes = new ArrayList<>();
		SymbolicConstant abstractFunction;
		SymbolicType functionType;
		List<SymbolicExpression> argValues = new ArrayList<>(
				argumentValues.length + 1);

		argumentTypes.add(this.stringType);
		for (int i = 0; i < argumentValues.length; i++) {
			argumentTypes.add(argumentValues[i].type());
		}
		functionType = universe.functionType(argumentTypes,
				universe.booleanType());
		abstractFunction = universe.symbolicConstant(
				universe.stringObject(functionName), functionType);
		argValues.add(universe.stringExpression(functionName));
		for (int i = 0; i < argumentValues.length; i++) {
			argValues.add(argumentValues[i]);
		}
		return universe.apply(abstractFunction, argValues);
	}

	@Override
	public SymbolicExpression applyReverseFunction(String originalFunction,
			SymbolicExpression argument) {
		if (argument.operator() == SymbolicOperator.APPLY) {
			SymbolicConstant function = (SymbolicConstant) argument.argument(0);
			@SuppressWarnings("unchecked")
			SymbolicSequence<? extends SymbolicExpression> args = (SymbolicSequence<? extends SymbolicExpression>) argument
					.argument(1);

			if (args.size() == 1) {
				if (function.name().getString().equals(originalFunction)) {
					return args.get(0);
				}
			}
		}
		return null;
	}

	@Override
	public BitSet range2BitSet(SymbolicExpression range, Reasoner reasoner) {
		BitSet results = new BitSet();

		if (range.isNumeric()) {
			results.set(((IntegerNumber) reasoner
					.extractNumber((NumericExpression) range)).intValue());
		} else {
			// else it's range
			NumericExpression high = getHighOfRegularRange(range);
			NumericExpression low = getLowOfRegularRange(range);
			NumericExpression step = getStepOfRegularRange(range);
			int highInt, lowInt, stepInt;
			IntegerNumber tmp;

			tmp = (IntegerNumber) reasoner.extractNumber(high);
			if (tmp == null)
				return null;
			highInt = tmp.intValue();
			tmp = (IntegerNumber) reasoner.extractNumber(low);
			if (tmp == null)
				return null;
			lowInt = tmp.intValue();
			tmp = (IntegerNumber) reasoner.extractNumber(step);
			if (tmp == null)
				return null;
			stepInt = tmp.intValue();
			for (int i = lowInt; i <= highInt; i += stepInt)
				results.set(i);
		}
		return results;
	}

	public IntObject getMallocID(SymbolicExpression heapPointer) {
		// As long as the argument "heapPointer" is a valid result of function
		// #heapMemUnit, it must have the form
		// ArrayELementReference<TupleComponentReference, a>, b>:
		ReferenceExpression symref = getSymRef(heapPointer);
		TupleComponentReference tupleRef = (TupleComponentReference) ((ArrayElementReference) symref)
				.getParent();

		return tupleRef.getIndex();
	}

	@Override
	public SymbolicExpression nullFunctionPointer() {
		return this.nullFunctionPointer;
	}

	@Override
	public SymbolicExpression makeFunctionPointer(int dyscopeID, int fid) {
		return universe.tuple(this.functionPointerType, Arrays.asList(
				stateFactory.scopeValue(dyscopeID), universe.integer(fid)));
	}

	/* *************** methods for specific heap structures ************* */
	@Override
	public SymbolicExpression getPointer2MemoryBlock(
			SymbolicExpression heapPointer) {
		while (!isPointer2MemoryBlock(heapPointer))
			heapPointer = parentPointer(heapPointer);
		return heapPointer;
	}

	@Override
	public boolean isPointer2MemoryBlock(SymbolicExpression pointer) {
		return heapAnalyzer.isPointer2MemoryBlock(pointer);
	}

	@Override
	public boolean arePoint2SameMemoryBlock(SymbolicExpression ptr0,
			SymbolicExpression ptr1) {
		assert isPointerToHeap(ptr0) && isPointerToHeap(ptr1);
		CIVLMemoryBlock blk0 = heapAnalyzer.memoryBlock(ptr0);
		CIVLMemoryBlock blk1 = heapAnalyzer.memoryBlock(ptr1);

		return blk0.compare(blk1);
	}
	/* ********** end-of methods for specific heap structures ******** */

	/* ********** end-of methods for specific heap structures ******** */

	@Override
	public SymbolicConstant freshBoundVariableFor(SymbolicType type,
			SymbolicExpression... expressions) {
		int nameSuffix = 0;
		SymbolicConstant bv = universe.symbolicConstant(
				universe.stringObject("i" + nameSuffix++), type);
		Set<SymbolicConstant> freeVars;

		if (expressions.length < 1)
			return bv;
		freeVars = expressions[0].getFreeVars();
		for (int i = 1; i < expressions.length; i++)
			freeVars.addAll(expressions[i].getFreeVars());
		do {
			bv = universe.symbolicConstant(
					universe.stringObject("i" + nameSuffix++), type);
		} while (freeVars.contains(bv));
		return bv;
	}

	@Override
	public SymbolicExpression[] symbolicArrayToConcreteArray(
			SymbolicExpression array) {
		NumericExpression numElements = universe.length(array);
		IntegerNumber numElementsNumber = (IntegerNumber) universe
				.extractNumber(numElements);

		assert array.operator() == SymbolicOperator.ARRAY : "A symbolic "
				+ "expression without ARRAY operator "
				+ "cannot be converted to a concrete array.";

		int numElementsInt = numElementsNumber.intValue();
		SymbolicExpression results[] = new SymbolicExpression[numElementsInt];

		for (int i = 0; i < numElementsInt; i++)
			results[i] = (SymbolicExpression) array.argument(i);
		return results;
	}

	@Override
	public boolean isConcretePointer(SymbolicExpression pointer) {
		if (pointer.type() != typeFactory.pointerSymbolicType())
			return false;
		if (pointer.operator() != SymbolicOperator.TUPLE)
			return false;
		if (pointer.argument(0)
				.symbolicObjectKind() != SymbolicObjectKind.EXPRESSION)
			return false;
		if (((SymbolicExpression) pointer.argument(0)).type() != typeFactory
				.scopeSymbolicType())
			return false;
		if (pointer.argument(1)
				.symbolicObjectKind() != SymbolicObjectKind.EXPRESSION)
			return false;
		if (((SymbolicExpression) pointer.argument(1)).type() != universe
				.integerType())
			return false;
		if (pointer.argument(2)
				.symbolicObjectKind() != SymbolicObjectKind.EXPRESSION)
			return false;
		if (((SymbolicExpression) pointer.argument(2)).type() != universe
				.referenceType())
			return false;
		return true;

	}
}
