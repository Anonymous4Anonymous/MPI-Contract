package edu.udel.cis.vsl.sarl.expr.common;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import edu.udel.cis.vsl.sarl.IF.SARLException;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericSymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.IF.expr.valueSetReference.NTValueSetReference;
import edu.udel.cis.vsl.sarl.IF.expr.valueSetReference.VSArrayElementReference;
import edu.udel.cis.vsl.sarl.IF.expr.valueSetReference.VSArraySectionReference;
import edu.udel.cis.vsl.sarl.IF.expr.valueSetReference.VSIdentityReference;
import edu.udel.cis.vsl.sarl.IF.expr.valueSetReference.VSOffsetReference;
import edu.udel.cis.vsl.sarl.IF.expr.valueSetReference.VSTupleComponentReference;
import edu.udel.cis.vsl.sarl.IF.expr.valueSetReference.VSUnionMemberReference;
import edu.udel.cis.vsl.sarl.IF.expr.valueSetReference.ValueSetReference;
import edu.udel.cis.vsl.sarl.IF.expr.valueSetReference.ValueSetReference.VSReferenceKind;
import edu.udel.cis.vsl.sarl.IF.number.IntegerNumber;
import edu.udel.cis.vsl.sarl.IF.object.IntObject;
import edu.udel.cis.vsl.sarl.IF.object.StringObject;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicSequence;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicArrayType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicCompleteArrayType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicFunctionType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicTupleType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicTypeSequence;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicUnionType;
import edu.udel.cis.vsl.sarl.expr.IF.BooleanExpressionFactory;
import edu.udel.cis.vsl.sarl.expr.IF.NumericExpressionFactory;
import edu.udel.cis.vsl.sarl.expr.common.valueSetReference.CommonVSArrayElementReference;
import edu.udel.cis.vsl.sarl.expr.common.valueSetReference.CommonVSArraySectionReference;
import edu.udel.cis.vsl.sarl.expr.common.valueSetReference.CommonVSIdentityReference;
import edu.udel.cis.vsl.sarl.expr.common.valueSetReference.CommonVSOffsetReference;
import edu.udel.cis.vsl.sarl.expr.common.valueSetReference.CommonVSTupleComponentReference;
import edu.udel.cis.vsl.sarl.expr.common.valueSetReference.CommonVSUnionMemberReference;
import edu.udel.cis.vsl.sarl.ideal.common.NTConstant;
import edu.udel.cis.vsl.sarl.object.IF.ObjectFactory;
import edu.udel.cis.vsl.sarl.object.common.SimpleSequence;
import edu.udel.cis.vsl.sarl.type.IF.SymbolicTypeFactory;
import edu.udel.cis.vsl.sarl.util.Pair;

/**
 * The factory provides interfaces for
 * <ul>
 * <li>creating instances of {@link ValueSetReference}s</li>
 * <li>operations over {@link ValueSetReference}s</li>
 * </ul>
 * 
 * @author xxxx
 *
 */
public class VSReferenceFactory {

	/*
	 * References to factorys:
	 */
	private ObjectFactory objectFactory;

	private SymbolicTypeFactory typeFactory;

	private NumericExpressionFactory numericFactory;

	/**
	 * The symbolic type of {@link ValueSetReference} expressions
	 */
	private SymbolicType valueSetReferenceType;

	/*
	 * Functions that map arguments to value set references
	 */
	private SymbolicConstant arrayElementReferenceFunction;

	private SymbolicConstant arraySectionReferenceFunction;

	private SymbolicConstant tupleComponentReferenceFunction;

	private SymbolicConstant unionMemberReferenceFunction;

	private SymbolicConstant offsetReferenceFunction;

	/*
	 * constant:
	 */
	private VSIdentityReference identityReference;

	/* Constructor */
	VSReferenceFactory(NumericExpressionFactory numericFactory) {
		this.numericFactory = numericFactory;
		this.objectFactory = numericFactory.objectFactory();
		this.typeFactory = numericFactory.typeFactory();

		SymbolicType integerType = typeFactory.integerType();
		SymbolicTypeSequence refIdxSeq, refRangeSeq;
		SymbolicFunctionType refIdxFuncType, refRangeFuncType;

		valueSetReferenceType = objectFactory.canonic(typeFactory.tupleType(
				objectFactory.stringObject("VSRef"),
				typeFactory.sequence(new SymbolicType[] { integerType })));
		refIdxSeq = objectFactory.canonic(typeFactory.sequence(
				new SymbolicType[] { valueSetReferenceType, integerType }));
		refRangeSeq = objectFactory.canonic(
				typeFactory.sequence(new SymbolicType[] { valueSetReferenceType,
						integerType, integerType, integerType }));
		refIdxFuncType = objectFactory.canonic(
				typeFactory.functionType(refIdxSeq, valueSetReferenceType));
		refRangeFuncType = objectFactory.canonic(
				typeFactory.functionType(refRangeSeq, valueSetReferenceType));
		arrayElementReferenceFunction = symbolicConstant(
				objectFactory.stringObject("VSArrayElementRef"),
				refIdxFuncType);
		arraySectionReferenceFunction = symbolicConstant(
				objectFactory.stringObject("VSArraySectionRef"),
				refRangeFuncType);
		tupleComponentReferenceFunction = symbolicConstant(
				objectFactory.stringObject("VSTupleComponentRef"),
				refIdxFuncType);
		unionMemberReferenceFunction = symbolicConstant(
				objectFactory.stringObject("VSUnionMemberRef"), refIdxFuncType);
		offsetReferenceFunction = symbolicConstant(
				objectFactory.stringObject("VSOffsetRef"), refIdxFuncType);
		identityReference = objectFactory.canonic(new CommonVSIdentityReference(
				valueSetReferenceType, numericFactory.oneInt()));
	}

	/**
	 * 
	 * @return the symbolic type of {@link ValueSetReference}s
	 */
	SymbolicType valueSetReferenceType() {
		return valueSetReferenceType;
	}

	/**
	 * The general interface for creating {@link ValueSetReference}s
	 * 
	 * @TODO Should this really be public? Nobody calling from outside class
	 *       could pass in the correct arg0 since it requires one of the private
	 *       reference functions
	 * 
	 * @param operator
	 *            The {@link SymbolicOperator} of the {@link ValueSetReference}
	 *            instance
	 * @param args
	 *            Arguments of the {@link ValueSetReference} instance
	 * @return a {@link ValueSetReference} expression, which has the given
	 *         operator and arguments
	 */
	ValueSetReference valueSetReference(SymbolicOperator operator,
			SymbolicObject... args) {
		if (operator == SymbolicOperator.TUPLE) {
			return vsIdentityReference();
		} else {
			assert operator == SymbolicOperator.APPLY;
			if (args[0] == arrayElementReferenceFunction) {
				@SuppressWarnings("unchecked")
				SimpleSequence<? extends SymbolicObject> seq = (SimpleSequence<? extends SymbolicObject>) args[1];
				return vsArrayElementReference((ValueSetReference) seq.get(0),
						(NumericExpression) seq.get(1));
			} else if (args[0] == arraySectionReferenceFunction) {
				@SuppressWarnings("unchecked")
				SimpleSequence<? extends SymbolicObject> seq = (SimpleSequence<? extends SymbolicObject>) args[1];
				return vsArraySectionReference((ValueSetReference) seq.get(0),
						(NumericExpression) seq.get(1),
						(NumericExpression) seq.get(2),
						(NumericExpression) seq.get(3));
			} else if (args[0] == this.tupleComponentReferenceFunction) {
				@SuppressWarnings("unchecked")
				SimpleSequence<? extends SymbolicObject> seq = (SimpleSequence<? extends SymbolicObject>) args[1];
				IntObject idx = objectFactory.intObject(
						((IntegerNumber) ((NTConstant) seq.get(1)).number())
								.intValue());

				return vsTupleComponentReference((ValueSetReference) seq.get(0),
						idx);
			} else if (args[0] == this.unionMemberReferenceFunction) {
				@SuppressWarnings("unchecked")
				SimpleSequence<? extends SymbolicObject> seq = (SimpleSequence<? extends SymbolicObject>) args[1];
				IntObject idx = objectFactory.intObject(
						((IntegerNumber) ((NTConstant) seq.get(1)).number())
								.intValue());

				return vsUnionMemberReference((ValueSetReference) seq.get(0),
						idx);
			} else {
				assert args[0] == this.offsetReferenceFunction;
				@SuppressWarnings("unchecked")
				SimpleSequence<? extends SymbolicObject> seq = (SimpleSequence<? extends SymbolicObject>) args[1];
				return vsOffsetReference((ValueSetReference) seq.get(0),
						(NumericExpression) seq.get(1));
			}
		}
	}

	/**
	 * Creating an instance of {@link VSArraySectionReference}
	 */
	VSArraySectionReference vsArraySectionReference(ValueSetReference parent,
			NumericExpression lower, NumericExpression upper,
			NumericExpression step) {
		return objectFactory.canonic(new CommonVSArraySectionReference(
				valueSetReferenceType, arraySectionReferenceFunction,
				makeSequence(parent, lower, upper, step)));
	}

	/**
	 * Creating an instance of {@link VSArrayElementReference}
	 */
	VSArrayElementReference vsArrayElementReference(ValueSetReference parent,
			NumericExpression index) {
		return objectFactory.canonic(new CommonVSArrayElementReference(
				valueSetReferenceType, arrayElementReferenceFunction,
				makeSequence(parent, index)));
	}

	/**
	 * Creating an instance of {@link VSOffsetReference}
	 */
	VSOffsetReference vsOffsetReference(ValueSetReference parent,
			NumericExpression offset) {
		return objectFactory
				.canonic(new CommonVSOffsetReference(valueSetReferenceType,
						offsetReferenceFunction, makeSequence(parent, offset)));
	}

	/**
	 * Creating an instance of {@link VSTupleComponentReference}
	 */
	VSTupleComponentReference vsTupleComponentReference(
			ValueSetReference parent, IntObject index) {
		NumericExpression idx = numericFactory
				.number(objectFactory.numberObject(numericFactory
						.numberFactory().integer(index.getInt())));
		return objectFactory.canonic(new CommonVSTupleComponentReference(
				valueSetReferenceType, tupleComponentReferenceFunction,
				makeSequence(parent, idx), index));
	}

	/**
	 * Creating an instance of {@link VSUnionMemberReference}
	 */
	VSUnionMemberReference vsUnionMemberReference(ValueSetReference parent,
			IntObject index) {
		NumericExpression idx = numericFactory
				.number(objectFactory.numberObject(numericFactory
						.numberFactory().integer(index.getInt())));
		return objectFactory.canonic(new CommonVSUnionMemberReference(
				valueSetReferenceType, unionMemberReferenceFunction,
				makeSequence(parent, idx), index));
	}

	/**
	 * Creating an instance of {@link VSIdentityReference}
	 */
	VSIdentityReference vsIdentityReference() {
		return identityReference;
	}

	/**
	 * Normalize a set of {@link ValueSetReference}s, which are associated with
	 * a {@link SymbolicType} "valueType":
	 * <ol>
	 * <li>all value set references will have the same depth as the one, which
	 * has the maximum depth, in them; reference:
	 * {@link #depth(ValueSetReference)}</li>
	 * <li>combine array element/slice references to array slice references in
	 * trivial cases</li>
	 * <li>get rid of duplicated value set references</li>
	 * <li>the same set of value set references will be in a canonicalized order
	 * from smallest to the greatest</li>
	 * </ol>
	 *
	 * @param valueType
	 *            the symbolic type of the value, subset of which are referred
	 *            by the given value set references
	 * @param vsRefs
	 *            an array of {@link ValueSetReference}s
	 * @return an array of normalized {@link ValueSetReference}s
	 */
	ValueSetReference[] normalize(SymbolicType valueType,
			ValueSetReference[] vsRefs) {
		vsRefs = toMaxDepth(deleteSubReferences(vsRefs), valueType);

		ValueSetReference[][] groups = grouping(vsRefs);
		int finalNumRefs = 0;

		for (int i = 0; i < groups.length; i++) {
			groups[i] = normalizeGroup(groups[i]);
			finalNumRefs += groups[i].length;
		}

		ValueSetReference[] results = new ValueSetReference[finalNumRefs];

		finalNumRefs = 0;
		for (ValueSetReference[] group : groups) {
			System.arraycopy(group, 0, results, finalNumRefs, group.length);
			finalNumRefs += group.length;
		}
		Arrays.sort(results, objectFactory.comparator());
		return results;
	}

	/* ******************** Operations ************************/
	/**
	 * <p>
	 * Tests if the given set of {@link ValueSetReference}s "superRefs" is a
	 * super set of "subRefs".
	 * </p>
	 * 
	 * @param valueType
	 *            the symbolic type that all elements in "superRefs" and
	 *            "subRefs" are referring to
	 * @param superRefs
	 *            the set of {@link ValueSetReference}s that will be tested if
	 *            it is a super set of another
	 * @param subRefs
	 *            the set of {@link ValueSetReference}s that will be tested if
	 *            it is a subset of another
	 * @return a boolean expression representing the test result
	 */
	BooleanExpression valueSetContains(SymbolicType valueType,
			ValueSetReference[] superRefs, ValueSetReference subRefs[]) {
		BooleanExpressionFactory boolFactory = numericFactory.booleanFactory();
		BooleanExpression result = boolFactory.trueExpr();
		ValueSetReference[][] refsToSameDepth = toMaxDepth(valueType, superRefs,
				subRefs);

		superRefs = refsToSameDepth[0];
		subRefs = refsToSameDepth[1];
		for (ValueSetReference subRef : subRefs) {
			ValueSetReference[] candidates = getSameConcreteStructureAs(
					superRefs, subRef, true).left;

			// no value set reference in "super" contains the "subRef"
			if (candidates.length == 0)
				return boolFactory.falseExpr();

			NumericExpression[][][] superDomain = getDomain(candidates);
			NumericExpression[][][] subDomain = getDomain(
					new ValueSetReference[] { subRef });
			NumericExpression[][] transformedSubDom = new NumericExpression[subDomain.length][];
			BooleanExpression contains;

			// transform subDomain : [numLevels][1][3] -> [numLevels][3] in
			// order
			// to call "contains"
			for (int i = 0; i < subDomain.length; i++) {
				assert subDomain[i].length == 1 : "single value set reference encodes up to one range";
				transformedSubDom[i] = subDomain[i][0];
			}
			contains = contains(superDomain, transformedSubDom);
			result = boolFactory.and(contains, result);
		}
		return result;
	}

	/**
	 * <p>
	 * Test if two value set reference have no intersection, i.e., if they are
	 * used to refer the same object, their referred parts have no overlap.
	 * </p>
	 *
	 * <p>
	 * This method returns the condition that is true iff the two value set
	 * references have no intersection.
	 * </p>
	 *
	 * @param valueType
	 *            the type of the value that the given value set reference
	 *            associated with
	 * @param vsr0
	 *            a value set reference
	 * @param vsr1
	 *            a value set reference
	 * @return the boolean condition that is true iff the two value set
	 *         references have no intersection.
	 */
	BooleanExpression valueSetNoIntersect(SymbolicType valueType,
			ValueSetReference vsr0, ValueSetReference vsr1) {
		ValueSetReference[] vsr0arr = new ValueSetReference[] { vsr0 };
		ValueSetReference[] vsr1arr = new ValueSetReference[] { vsr1 };
		ValueSetReference[][] vsrsOfMaxDepth = toMaxDepth(valueType, vsr0arr,
				vsr1arr);
		BooleanExpression result = numericFactory.booleanFactory().trueExpr();

		assert vsrsOfMaxDepth.length == 2;
		for (ValueSetReference v0 : vsrsOfMaxDepth[0])
			for (ValueSetReference v1 : vsrsOfMaxDepth[1])
				if (sameConcreteStructure(v0, v1, false)) {
					NumericExpression[][][] dom0 = getDomain(
							new ValueSetReference[] { v0 });
					NumericExpression[][][] dom1 = getDomain(
							new ValueSetReference[] { v1 });
					NumericExpression[][] transformedDom0 = new NumericExpression[dom0.length][];
					NumericExpression[][] transformedDom1 = new NumericExpression[dom1.length][];

					assert dom0.length == dom1.length;
					if (dom0.length == 0) // same object
						return numericFactory.booleanFactory().falseExpr();
					/*
					 * the second dimensions must have size 1. Transform the
					 * array in order to call "noIntersect"
					 */
					for (int i = 0; i < dom0.length; i++) {
						assert dom0[i].length == 1;
						assert dom1[i].length == 1;
						transformedDom0[i] = dom0[i][0];
						transformedDom1[i] = dom1[i][0];
					}

					BooleanExpression cond = noIntersect(transformedDom0,
							transformedDom1);

					result = numericFactory.booleanFactory().and(result, cond);
				}
		return result;
	}

	/**
	 * <p>
	 * Applying a default widening operator to a value set template (in the form
	 * of a valueType and a set of value set references).
	 * </p>
	 * 
	 * <p>
	 * The default widening operator is defined by: <code>
	 * INTPUT refs : a set of value set references
	 * OUTPUT out  : a set of value set references
	 *   result : empty set
	 *   for-each (group : grouping(refs)) 
	 *     if (|group| > 1) 
	 *       result.add(widening(group))
	 *   return result
	 * </code> where "widening" by default is defined by
	 * {@link #defaultWidening(SymbolicType, ValueSetReference[])}. Also see
	 * {@link #grouping(ValueSetReference[])}.
	 * </p>
	 * 
	 * @param valueType
	 *            the symbolic type of the value where the given set of
	 *            {@link ValueSetReference}s refer to
	 * @param refs
	 *            a set of {@link ValueSetReference}s
	 * @return a java-array of value set references after the widening operation
	 */
	ValueSetReference[] valueSetWidening(SymbolicType valueType,
			ValueSetReference[] refs) {
		refs = toMaxDepth(refs, valueType);
		ValueSetReference[][] groups = grouping(refs);
		List<ValueSetReference> result = new LinkedList<>();

		for (ValueSetReference[] group : groups)
			if (group.length > 1)
				result.add(defaultWidening(valueType, group));
			else
				result.add(group[0]);

		ValueSetReference[] ret = new ValueSetReference[result.size()];

		result.toArray(ret);
		return ret;
	}

	/* ************************ private methods **************************/
	private SymbolicConstant symbolicConstant(StringObject name,
			SymbolicType type) {
		if (type.isNumeric())
			return numericFactory.symbolicConstant(name, type);
		return objectFactory.canonic(new CommonSymbolicConstant(name, type));
	}

	private SymbolicSequence<SymbolicExpression> makeSequence(
			ValueSetReference parent, NumericExpression... indices) {
		SymbolicExpression seqEles[] = new SymbolicExpression[indices.length
				+ 1];

		System.arraycopy(indices, 0, seqEles, 1, indices.length);
		seqEles[0] = parent;
		return objectFactory.sequence(seqEles);
	}

	/**
	 * <p>
	 * <b>pre-condition:</b> 1) the given "group" is returned by
	 * {@link #grouping(ValueSetReference[])}; 2) group.length > 1
	 * </p>
	 * 
	 * <p>
	 * Definition: <code>
	 * while (|group| > 1) {
	 *   for any pair (r0, r1 : group) {
	 *       r = apply(r0, r1)
	 *       group.remove(r0)
	 *       group.remove(r1)
	 *       group.add(r)
	 *   }
	 *   return group;
	 * }
	 * </code> where "apply" is defined by <code>
	 *  apply(r0, r1) = 
	 *    if (r0 is array element/section reference && r0 != r1)
	 *      return arraySectionRef(apply(parent(r0), parent(r1)), [0 .. extent : 1])
	 *    else if (r0 is non-trivial)
	 *      return r0[apply(parent(r0), parent(r1)) / parent(r0)] // replace parent
	 *    else 
	 *      return r0;
	 * </code>
	 * </p>
	 */
	private ValueSetReference defaultWidening(SymbolicType valueType,
			ValueSetReference[] group) {
		int length = group.length;

		while (length > 1) {
			ValueSetReference r0 = group[0];
			ValueSetReference r1 = group[length - 1];

			group[0] = defaultWideningWorker(valueType, r0, r1);
			length--;
		}
		return group[0];
	}

	/**
	 * Recursive helper method for
	 * {@link #defaultWidening(SymbolicType, ValueSetReference[])}
	 */
	private ValueSetReference defaultWideningWorker(SymbolicType valueType,
			ValueSetReference r0, ValueSetReference r1) {
		NumericExpression[] args0 = null;
		NumericExpression[] args1;
		boolean isArrayRef = false;

		if (r0.isIdentityReference()) {
			assert r1.isIdentityReference();
			return r0;
		}
		if (r0.isArrayElementReference()) {
			VSArrayElementReference r = (VSArrayElementReference) r0;

			args0 = new NumericExpression[] { r.getIndex() };
			isArrayRef = true;
		} else if (r0.isArraySectionReference()) {
			VSArraySectionReference r = (VSArraySectionReference) r0;

			args0 = new NumericExpression[] { r.lowerBound(), r.upperBound(),
					r.step() };
			isArrayRef = true;
		}
		if (isArrayRef) {
			if (r1.isArrayElementReference()) {
				VSArrayElementReference r = (VSArrayElementReference) r1;

				args1 = new NumericExpression[] { r.getIndex() };
			} else {
				VSArraySectionReference r = (VSArraySectionReference) r1;

				args1 = new NumericExpression[] { r.lowerBound(),
						r.upperBound(), r.step() };
			}
			if (!Arrays.equals(args0, args1)) {
				// widening:
				SymbolicCompleteArrayType arrType = (SymbolicCompleteArrayType) referredType(
						valueType, ((NTValueSetReference) r0).getParent());
				ValueSetReference parent = defaultWideningWorker(valueType,
						((NTValueSetReference) r0).getParent(),
						((NTValueSetReference) r1).getParent());

				return vsArraySectionReference(parent, numericFactory.zeroInt(),
						arrType.extent(), numericFactory.oneInt());
			}
		}

		@SuppressWarnings("unchecked")
		SimpleSequence<SymbolicExpression> parentIndices = (SimpleSequence<SymbolicExpression>) r0
				.argument(1);
		ValueSetReference parent = defaultWideningWorker(valueType,
				((NTValueSetReference) r0).getParent(),
				((NTValueSetReference) r1).getParent());

		parentIndices = (SimpleSequence<SymbolicExpression>) parentIndices
				.set(0, parent);
		return valueSetReference(SymbolicOperator.APPLY, r0.argument(0),
				parentIndices);
	}

	/**
	 * <p>
	 * <b>pre-condition:</b> all group members have the same
	 * {@link #depth(ValueSetReference)}
	 * </p>
	 * <p>
	 * Normalize a group of value set references. For "group", see
	 * {@link #grouping(ValueSetReference[])}. For "normalize", see
	 * {@link #normalize(SymbolicType, ValueSetReference[])}
	 * </p>
	 * 
	 * @param group
	 *            a group of value set references
	 * @return normalized group fo value set references
	 */
	private ValueSetReference[] normalizeGroup(ValueSetReference[] group) {
		boolean hasCombined = false;
		ValueSetReference[] result = Arrays.copyOf(group, group.length);
		int length = result.length;

		Arrays.sort(result, objectFactory.comparator());
		do {
			int i, j;
			ValueSetReference combined = null;

			hasCombined = false;
			for (i = 0; i < length; i++) {
				for (j = 0; j < length; j++)
					if (i == j)
						continue;
					else {
						combined = combine(result[i], result[j]);
						if (combined != null) {
							hasCombined = true;
							break;
						}
					}
				if (hasCombined) {
					// replace the "result[i]" and "result[j]" with "combined":
					int min = i < j ? i : j;
					int max = min == i ? j : i;

					if (max < length - 1)
						System.arraycopy(result, max + 1, result, max,
								length - max - 1);
					length--;
					result[min] = combined;
					break;
				}
			}
		} while (hasCombined);
		return Arrays.copyOf(result, length);
	}

	/**
	 * <p>
	 * Attempt to combine two value set references: a and b, they can be
	 * combined if
	 * <ul>
	 * <li>there is at MOST one pair of ancestors a' and b' of a and b at some
	 * recursive level such that a' != b' and they can only be either array
	 * element or section references, then for a' and b':</li>
	 * <ul>
	 * <li>a' refers to an array section "l .. h : s" and b' refers to an array
	 * section "x .. y : z" and <code>s == z</code> and one of the followings
	 * holds: 1) <code>
	 * l == y
	 * </code> or 2) <code>h == x</code>
	 * </ul>
	 * </ul>
	 * , otherwise, returns null
	 * </p>
	 * 
	 * @param r0
	 *            a value set reference
	 * @param r1
	 *            a value set reference
	 * @return the combined value set reference, or null if a and b are not
	 *         combinable.
	 */
	private ValueSetReference combine(ValueSetReference r0,
			ValueSetReference r1) {
		NumericExpression[][][] d0 = getDomain(new ValueSetReference[] { r0 });
		NumericExpression[][][] d1 = getDomain(new ValueSetReference[] { r1 });
		NumericExpression[][] ranges0 = new NumericExpression[d0.length][];
		NumericExpression[][] ranges1 = new NumericExpression[d1.length][];

		// transform d0 -> ranges0 & d1 -> ranges1:
		assert d0.length == d1.length;
		for (int i = 0; i < d0.length; i++) {
			ranges0[i] = d0[i][0];
			ranges1[i] = d1[i][0];
		}

		int diffAt = -1;

		for (int i = 0; i < ranges0.length; i++) {
			boolean diff = !(ranges0[i][0] == ranges1[i][0]
					&& ranges0[i][1] == ranges1[i][1]
					&& ranges0[i][2] == ranges1[i][2]);

			if (diff && diffAt >= 0)
				return null;
			else if (diff)
				diffAt = i;
		}
		if (diffAt < 0)
			return r0;

		NumericExpression[] newRange = combineRanges(ranges0[diffAt],
				ranges1[diffAt]);

		if (newRange == null)
			return null;
		// Update ValueSetReference with newRange:
		// note the the only recursive level that needs update is the
		// "diffAt + 1"-th level (since 0-based), counting from outer to inner,
		// ignoring tuple component and union member references:
		return replaceWithArraySection(diffAt + 1, newRange, r0);
	}

	/**
	 * Attempts to combine two ranges, see
	 * {@link #combine(ValueSetReference, ValueSetReference)} for the cases
	 * under which they can be combined.
	 * 
	 * @param r0
	 *            range consists of 3 numeric expressions: [lower .. upper :
	 *            step]
	 * @param r1
	 *            range consists of 3 numeric expressions: [lower .. upper :
	 *            step]
	 * @return the combined range or null if the given two are not able to be
	 *         combined
	 */
	private NumericExpression[] combineRanges(NumericExpression[] r0,
			NumericExpression r1[]) {
		if (r0[2] != r1[2])
			return null;
		// connected:
		if (r0[0] == r1[1])
			return new NumericExpression[] { r1[0], r0[1], r0[2] };
		if (r0[1] == r1[0])
			return new NumericExpression[] { r0[0], r1[1], r1[2] };
		return null;
	}

	/**
	 * 
	 * <p>
	 * Given a depth counter "n" and a value set reference "origin", an ancestor
	 * <code>a</code> of "origin" is referred by: <code>
	 * a = origin;
	 * while(n > 1) {
	 *   if (a is array element/section reference) 
	 *     n--;
	 *    else if (a is offset reference) 
	 *     n--;
	 *   a = parent(a);
	 * }
	 * </code>. And, the result <code>a</code> must not be an
	 * {@link VSOffsetReference}.
	 * </p>
	 * 
	 * <p>
	 * Given a range "newRange", let
	 * <code>a' = arraySectionRef(parent(a), newRange)</code>, this method
	 * returns <code>origin[a' / a]</code>.
	 * </p>
	 * 
	 * <p>
	 * For example, <code>
	 *   replaceArraySection(2, [x .. y : z],  
	 *                       vsOffsetRef(TupleComponentRef(vsArraySectionRef(Identity(), a .. b : c) idx) offset) 
	 *                      )
	 *   = vsOffsetRef(TupleComponentRef(vsArraySectionRef(Identity(), x .. y : z) idx) offset) 
	 * 
	 * </code>
	 * </p>
	 * 
	 * @param n
	 *            the number of array element/section references or offset
	 *            reference that is counted from origin to the ancestor (the
	 *            ancestor itself is counted as well) that will be replaced.
	 * @param newRange
	 *            a range that refers to an array section that will be replaced
	 * @param origin
	 *            the value set reference before the replacement
	 * @return the replaced value set reference
	 */
	private ValueSetReference replaceWithArraySection(int n,
			NumericExpression[] newRange, ValueSetReference origin) {
		LinkedList<ValueSetReference> stack = new LinkedList<>();

		while (!origin.isIdentityReference() && n > 0) {
			stack.push(origin);
			if (origin.isArrayElementReference()
					|| origin.isArraySectionReference()
					|| origin.isOffsetReference())
				n--;
			origin = ((NTValueSetReference) origin).getParent();
		}
		assert !stack.peek().isOffsetReference();
		stack.pop();
		origin = vsArraySectionReference(origin, newRange[0], newRange[1],
				newRange[2]);
		while (!stack.isEmpty()) {
			ValueSetReference ancestor = stack.pop();
			SymbolicObject[] args = new SymbolicObject[ancestor.numArguments()];

			assert args.length == 2;
			args[0] = ancestor.argument(0);

			@SuppressWarnings("unchecked")
			SimpleSequence<SymbolicExpression> seqArg = (SimpleSequence<SymbolicExpression>) ancestor
					.argument(1);
			seqArg = (SimpleSequence<SymbolicExpression>) seqArg.set(0,
					(SymbolicExpression) origin);
			args[1] = seqArg;
			origin = valueSetReference(SymbolicOperator.APPLY, args);
		}
		return origin;
	}

	/**
	 * Returns the number of recursive levels of the value set reference
	 * 
	 * @param ref
	 *            a value set reference expression
	 * @return the number of recursive levels of the value set reference
	 */
	private int depth(ValueSetReference ref) {
		if (ref.isIdentityReference())
			return 1;
		else
			return depth(((NTValueSetReference) ref).getParent()) + 1;
	}

	/**
	 * <p>
	 * Given a set of "superDomain": <code>
	 * {superDomain[0][0] X superDomain[1][0] X ... X superDomain[n-1][0]},
	 * {superDomain[0][1] X superDomain[1][1] X ... X superDomain[n-1][1]},
	 * ...
	 * {superDomain[0][m-1] X superDomain[1][m-1] X ... X superDomain[n-1][m-1]}
	 * </code> , where <code>superDomain[i][j]</code> is a range of integers,
	 * and a "subDomain" : <code> 
	 * {subDomain[0] X ... X subDomain[1] ... X subDomain[n-1]}
	 * </code>, returns a boolean expression: <code>
	 * FORALL int e. e in subDomain s.t. (EXISTS int e'. e' in {superDomain[0][0], superDomain[1][0], ... } && e == e')  OR
	 *                                   (EXISTS int e'. e' in {superDomain[0][1], superDomain[1][1], ... } && e == e')  OR
	 *                                   ...
	 *                                   (EXISTS int e'. e' in {superDomain[0][m-1], superDomain[1][m-1], ... } && e == e')  OR
	 * </code>
	 * </p>
	 * 
	 * @param superDomain
	 * @param subDomain
	 * @return
	 */
	private BooleanExpression contains(NumericExpression[][][] superDomain,
			NumericExpression[][] subDomain) {
		assert superDomain.length == subDomain.length;
		BooleanExpressionFactory boolFactory = numericFactory.booleanFactory();

		if (subDomain.length == 0)
			return boolFactory.trueExpr();

		int numDims = superDomain.length;
		int numRangesPerDim = 0;
		BooleanExpression forallRestriction = boolFactory.trueExpr();
		String superEleName = "i", subEleName = "j";
		NumericSymbolicConstant[] superEles = new NumericSymbolicConstant[numDims];
		NumericSymbolicConstant[] subEles = new NumericSymbolicConstant[numDims];

		for (int i = 0; i < numDims; i++) {
			superEles[i] = (NumericSymbolicConstant) symbolicConstant(
					objectFactory.stringObject(superEleName + i),
					typeFactory.integerType());
			subEles[i] = (NumericSymbolicConstant) symbolicConstant(
					objectFactory.stringObject(subEleName + i),
					typeFactory.integerType());
			forallRestriction = boolFactory.and(forallRestriction,
					inRange(subEles[i], subDomain[i]));
			if (numRangesPerDim == 0)
				numRangesPerDim = superDomain[i].length;
			assert numRangesPerDim == superDomain[i].length;
		}

		BooleanExpression existsPred[] = new BooleanExpression[numRangesPerDim];
		BooleanExpression forallPred;

		for (int i = 0; i < numRangesPerDim; i++) {
			existsPred[i] = boolFactory.trueExpr();

			for (int j = 0; j < numDims; j++) {
				existsPred[i] = boolFactory.and(existsPred[i],
						inRange(superEles[j], superDomain[j][i]));
				existsPred[i] = boolFactory.and(existsPred[i],
						numericFactory.equals(superEles[j], subEles[j]));
			}
			for (int j = 0; j < numDims; j++)
				existsPred[i] = boolFactory.exists(superEles[j], existsPred[i]);
		}
		forallPred = boolFactory.or(Arrays.asList(existsPred));
		forallPred = boolFactory.or(boolFactory.not(forallRestriction),
				forallPred);
		for (int i = 0; i < numDims; i++)
			forallPred = boolFactory.forall(subEles[i], forallPred);
		return forallPred;
	}

	/**
	 * <p>
	 * Given two "domains": <code>
	 * {range[0] X range[1] X ... X range[n-1]}
	 * </code> and <code>
	 * {range2[0] X range2[1] X ... X range2[n-1]}
	 * </code>, returns the condition that is true iff the two domains have no
	 * intersection, i.e., <code>
	 *   rangeNoIntersect(range[0], range2[0])  OR
	 *   rangeNoIntersects(range[1], range2[1]) OR
	 *   ...                                    OR
	 *   rangeNoIntersects(range[n-1], range2[n-1])
	 * </code>
	 * </p>
	 *
	 * <p>
	 * pre-condition: two domains have the same positive number "n" of ranges
	 * </p>
	 *
	 * @param dom1
	 *            a domain which is represented by a sequence of ranges, each
	 *            range is an array of three NumericExpression
	 * @param dom2
	 *            a domain which is represented by a sequence of ranges, each
	 *            range is an array of three NumericExpression
	 * @return the condition that is true iff the two given domain have no
	 *         intersection
	 */
	private BooleanExpression noIntersect(NumericExpression[][] dom1,
			NumericExpression[][] dom2) {
		assert dom1.length == dom2.length;
		int len = dom1.length;
		assert len > 0;

		BooleanExpression result = rangeNoIntersect(dom1[0], dom2[0]);

		for (int i = 1; i < len; i++) {
			BooleanExpression tmp = rangeNoIntersect(dom1[i], dom2[i]);

			result = numericFactory.booleanFactory().or(result, tmp);
		}
		return result;
	}

	/**
	 * <p>
	 * return the condition that is true iff the two given ranges have no
	 * intersection.
	 * </p>
	 *
	 * @param r0
	 *            a range, consists of three NumericExpressions: low, high and
	 *            step.
	 * @param r1
	 *            a range, consists of three NumericExpressions: low, high and
	 *            step.
	 * @return the condition that is true iff the given two ranges have no
	 *         intersection
	 */
	private BooleanExpression rangeNoIntersect(NumericExpression[] r0,
			NumericExpression[] r1) {
		assert r0[2].isOne() && r1[2].isOne();
		// TODO: considering steps.
		NumericExpression low0 = r0[0], low1 = r1[0], hi0 = r0[1], hi1 = r1[1];
		// no intersect cond:
		BooleanExpression cond = numericFactory.lessThanEquals(hi0, low1);
		BooleanExpression tmp = numericFactory.lessThanEquals(hi1, low0);

		return numericFactory.booleanFactory().or(cond, tmp);
	}

	private BooleanExpression inRange(NumericSymbolicConstant e,
			NumericExpression range[]) {
		assert !range[0].getFreeVars().contains(e)
				&& !range[1].getFreeVars().contains(e)
				&& !range[2].getFreeVars().contains(
						e) : "bound variable 'i' has been used in expression";
		BooleanExpressionFactory boolFactory = numericFactory.booleanFactory();
		BooleanExpression result = numericFactory.lessThanEquals(range[0], e);

		result = boolFactory.and(result, numericFactory.lessThan(e, range[1]));
		if (!range[2].isOne()) {
			NumericExpression mod = numericFactory
					.modulo(numericFactory.subtract(e, range[0]), range[2]);

			result = boolFactory.and(result,
					numericFactory.equals(mod, numericFactory.zeroInt()));
		}
		return result;
	}

	/**
	 * <p>
	 * <b>pre-condition</b> the input value set references must 1) have same
	 * depth, 2) have same kind of structure, i.e. their ancestors at the same
	 * level have the same reference kind. 3) for their ancestors at the same
	 * level that have tuple component or union member reference kind, their
	 * field/member indices must be the same.
	 * </p>
	 *
	 * <p>
	 * This method helps obtaining the DOMAINs of some value set references.
	 * Returns a three dimensional array of
	 * <code>NumericExoression[H][num_ranges][3]</code>. H is the number of
	 * recursive levels, at each of which all candidates are array element/slice
	 * references or offset references. A DOMAIN at a recursive level
	 * <code>i</code> is a set of ranges, each of which is taken from the
	 * ancestor of a candidate at recursive level <code>i</code>. A range
	 * consists of an inclusive lower bound, an exclusive higher bound and a
	 * step. Note that for an array element reference or an offset reference who
	 * has a sole argument "index", this method gets range: [index, index+1)
	 * with default step 1.
	 * </p>
	 *
	 * <p>
	 * Note that "NON-array element/slice or offset reference" ancestors at some
	 * recursive levels are not represented in the returned domain. They are not
	 * needed actually.
	 * </p>
	 *
	 * @param refs
	 *            output of
	 *            {@link #getSameConcreteStructureAs(ValueSetReference[], ValueSetReference, boolean)}
	 * @return a 3 dimensional array: first dimension is the number of recursive
	 *         levels, (NOTE that indices in first dimensions are ORDERED from
	 *         the recursive level of higher depth to lower); the second
	 *         dimension is the number of ranges in a recursive level; the third
	 *         dimension is the range: low, high and step.
	 */
	private NumericExpression[][][] getDomain(ValueSetReference[] refs) {
		if (refs.length < 1)
			return new NumericExpression[0][0][0];

		ValueSetReference[] copy = Arrays.copyOf(refs, refs.length);
		NumericExpression one = numericFactory.oneInt();
		ValueSetReference delegate = copy[0];
		List<NumericExpression[][]> results = new LinkedList<>();

		while (!delegate.isIdentityReference()) {
			NumericExpression[][] resultAtLevel = new NumericExpression[copy.length][];
			boolean hasRange = false;

			switch (delegate.valueSetReferenceKind()) {
			case ARRAY_ELEMENT:
			case ARRAY_SECTION:
				for (int i = 0; i < copy.length; i++)
					if (copy[i].isArrayElementReference()) {
						NumericExpression idx = ((VSArrayElementReference) copy[i])
								.getIndex();
						resultAtLevel[i] = new NumericExpression[] { idx,
								numericFactory.add(idx, one), one };
					} else {
						VSArraySectionReference sectionRef = (VSArraySectionReference) copy[i];
						NumericExpression lo = sectionRef.lowerBound(),
								hi = sectionRef.upperBound(),
								step = sectionRef.step();

						resultAtLevel[i] = new NumericExpression[] { lo, hi,
								step };
					}
				hasRange = true;
				break;
			case OFFSET:
				for (int i = 0; i < copy.length; i++) {
					NumericExpression offset = ((VSOffsetReference) copy[i])
							.getOffset();

					resultAtLevel[i] = new NumericExpression[] { offset,
							numericFactory.add(offset, one), one };
				}
				hasRange = true;
				break;
			case TUPLE_COMPONENT:
			case UNION_MEMBER:
				// ignore:
				break;
			case IDENTITY:
			default:
				throw new SARLException("unreachable");
			}
			if (hasRange)
				results.add(resultAtLevel);
			for (int i = 0; i < copy.length; i++)
				copy[i] = ((NTValueSetReference) copy[i]).getParent();
			delegate = copy[0];
		}

		NumericExpression[][][] ret = new NumericExpression[results.size()][][];

		results.toArray(ret);
		return ret;
	}

	/**
	 * <p>
	 * Dividing a set of value set references into several groups. For value set
	 * references <code>R</code> that in a same group, they satisfy such a
	 * condition: <code>
	 *   forall r : R.  getSameConcreteStructureAs(R, r) == R
	 * </code> (see
	 * {@link #getSameConcreteStructureAs(ValueSetReference[], ValueSetReference, boolean)}
	 * ).
	 * </p>
	 * 
	 * <p>
	 * In other words, all elements in one group have the same concrete
	 * structures.
	 * </p>
	 * 
	 * @param refs
	 *            a set of value set references
	 * @return a set of groups of value set references
	 */
	private ValueSetReference[][] grouping(ValueSetReference refs[]) {
		int remaining = refs.length;
		List<ValueSetReference[]> groups = new LinkedList<>();

		while (remaining != 0) {
			ValueSetReference[] oldRefs = refs;
			Pair<ValueSetReference[], ValueSetReference[]> sames_remains;

			sames_remains = getSameConcreteStructureAs(oldRefs, oldRefs[0],
					false);
			refs = sames_remains.right;
			remaining = refs.length;
			groups.add(sames_remains.left);
		}

		ValueSetReference[][] ret = new ValueSetReference[groups.size()][];

		groups.toArray(ret);
		return ret;
	}

	/**
	 * <p>
	 * <b>pre-condition:</b> all input value set references are returned by
	 * {@link #toMaxDepth(ValueSetReference[], SymbolicType)}
	 * </p>
	 * 
	 * <p>
	 * Returns a subset of "refs" that have the same concrete structure as the
	 * given "model"
	 * </p>
	 * 
	 * @param refs
	 *            a set of value set references where ones that have
	 *            {@link #sameConcreteStructure(ValueSetReference, ValueSetReference, boolean)}
	 *            as the given "model" will be returned.
	 * @param model
	 *            a value set reference that the returned value set references
	 *            must have
	 *            {@link #sameConcreteStructure(ValueSetReference, ValueSetReference, boolean)}
	 *            as this one
	 * @param ignoreOffSet
	 *            set to true to ignore {@link VSOffsetReference}s, i.e. the
	 *            offsets of two value set references do not have to be the
	 *            same.
	 * @return a PAIR, LEFT: a subset of "refs" that have the same concrete
	 *         structure as the given "model"; RIGHT: a subset of "refs" that
	 *         have different concrete structure as the given "model".
	 */
	private Pair<ValueSetReference[], ValueSetReference[]> getSameConcreteStructureAs(
			ValueSetReference refs[], ValueSetReference model,
			boolean ignoreOffSet) {
		ValueSetReference sames[] = new ValueSetReference[refs.length];
		ValueSetReference diffs[] = new ValueSetReference[refs.length];
		int ctSame = 0, ctDiff = 0;

		for (int i = 0; i < refs.length; i++)
			if (sameConcreteStructure(refs[i], model, ignoreOffSet))
				sames[ctSame++] = refs[i];
			else
				diffs[ctDiff++] = refs[i];
		return new Pair<>(Arrays.copyOf(sames, ctSame),
				Arrays.copyOf(diffs, ctDiff));
	}

	/**
	 * Helper method for
	 * {@link #getSameConcreteStructureAs(ValueSetReference[], ValueSetReference, boolean)}.
	 * For the rules of determining if two value set references "a" and "b" have
	 * the same concrete structure, i.e. if ancestors of "a" and "b" at the same
	 * recursive level are exactly the same if they have tuple component kind,
	 * union member kind or (optional) offset kind.
	 *
	 * @param vs0
	 *            a value set reference
	 * @param vs1
	 *            a value set reference
	 * @param ignoreOffset
	 *            set to true to ignore {@link VSOffsetReference}s, i.e. the
	 *            offsets of two value set references do not have to be the same
	 *            in order to have same concrete structure.
	 * @return true iff the "vs0" and "vs1" have the same concrete structure
	 */
	private boolean sameConcreteStructure(ValueSetReference vs0,
			ValueSetReference vs1, boolean ignoreOffset) {
		if (diffConcreteStructureKind(vs0, vs1, ignoreOffset))
			return false;
		while (!vs0.isIdentityReference()) {
			switch (vs0.valueSetReferenceKind()) {
			case ARRAY_ELEMENT:
			case ARRAY_SECTION:
				break;
			case OFFSET:
				if (!ignoreOffset)
					if (((VSOffsetReference) vs0)
							.getOffset() != ((VSOffsetReference) vs1)
									.getOffset())
						return false;
				break;
			case TUPLE_COMPONENT:
				if (((VSTupleComponentReference) vs0)
						.getIndex() != ((VSTupleComponentReference) vs1)
								.getIndex())
					return false;
				break;
			case UNION_MEMBER:
				if (((VSUnionMemberReference) vs0)
						.getIndex() != ((VSUnionMemberReference) vs1)
								.getIndex())
					return false;
				break;
			case IDENTITY:
			default:
				throw new SARLException("unreachable");
			}
			vs0 = ((NTValueSetReference) vs0).getParent();
			vs1 = ((NTValueSetReference) vs1).getParent();
			if (diffConcreteStructureKind(vs0, vs1, ignoreOffset))
				return false;
		}
		return true;
	}

	/**
	 * @return true iff the given two value set reference have different kinds
	 *         and at least one of them does not have either ARRAY_ELEMENT,
	 *         ARRAY_SECTION or OFFSET (depends on "ignoreOffset") kind.
	 */
	private boolean diffConcreteStructureKind(ValueSetReference vs0,
			ValueSetReference vs1, boolean ignoreOffset) {
		if (vs0.valueSetReferenceKind() != vs1.valueSetReferenceKind()) {
			VSReferenceKind kind0 = vs0.valueSetReferenceKind(),
					kind1 = vs1.valueSetReferenceKind();
			boolean isStructureKind0 = kind0 != VSReferenceKind.ARRAY_ELEMENT;
			boolean isStructureKind1 = kind1 != VSReferenceKind.ARRAY_ELEMENT;

			isStructureKind0 &= kind0 != VSReferenceKind.ARRAY_SECTION;
			isStructureKind1 &= kind1 != VSReferenceKind.ARRAY_SECTION;
			if (ignoreOffset) {
				isStructureKind0 &= kind0 != VSReferenceKind.OFFSET;
				isStructureKind1 &= kind1 != VSReferenceKind.OFFSET;
			}
			if (isStructureKind0 || isStructureKind1)
				return true;
		}
		return false;
	}

	/**
	 * <p>
	 * Let <code>m</code> be the one, who has the maximum depth, in the given
	 * {@link ValueSetReference} set. Each {@link ValueSetReference} in the
	 * given set will be extended to have either
	 * <ul>
	 * <li>1) as great depth as it can have if its greatest depth is less than
	 * or equal to the depth of <code>m</code>,</li>
	 * <li>2) the same depth as <code>m<code> otherwise</li>
	 * </ul>
	 * </p>
	 * 
	 * <p> For example, given two value set references 
	 * <code>&a.x, &a.y[0][0]</code> over such a variable: <code>
	 * struct T {
	 *   int x[10];
	 *   int y[10][10];
	 * } a;
	 * </code>, <code>&a.y[0][0]</code> has the maximum depth among two of them.
	 * reference <code>&a.x</code> will be extended to have as great depth as it
	 * can <code>&a.x[0 .. 9]</code> and reference <code>&a.y[0][0]</code> will
	 * be extended to have the same depth as the maximum one which is itself.
	 * </p>
	 * 
	 * @param refs
	 *            a java-array of {@link ValueSetReference}s
	 * @param valueType
	 *            the symbolic type of the value where all value set references
	 *            refer to
	 * @return a set of extended value set references. It may contains more
	 *         elements than the input java-array references
	 */
	private ValueSetReference[] toMaxDepth(ValueSetReference[] refs,
			SymbolicType valueType) {
		return toMaxDepth(valueType, refs)[0];
	}

	/**
	 * <p>
	 * This method is a variant of
	 * {@link #toMaxDepth(ValueSetReference[], SymbolicType)}. This variant
	 * takes multiple (array of) sets of value set references, then processes
	 * them as a big set, finally returns them as multiple (array of) sets. The
	 * returned array of sets corresponds to the input array of sets.
	 * </p>
	 * 
	 * @param valueType
	 *            the symbolic type of the value where all value set references
	 *            refer to
	 * @param refSets
	 *            an array of {@link ValueSetReference} set
	 * @return an array of extended {@link ValueSetReference} set, each of which
	 *         may contains more elements than the input java-array references.
	 *         <p>
	 *         Elements in the returned array correspond to the ones in the
	 *         input (parameter "refSets") array.
	 *         </p>
	 * 
	 */
	private ValueSetReference[][] toMaxDepth(SymbolicType valueType,
			ValueSetReference[]... refSets) {
		int depths[][] = new int[refSets.length][];
		int maxDepth = -1;

		for (int i = 0; i < depths.length; i++) {
			depths[i] = new int[refSets[i].length];
			for (int j = 0; j < refSets[i].length; j++) {
				depths[i][j] = depth(refSets[i][j]);
				if (depths[i][j] > maxDepth)
					maxDepth = depths[i][j];
			}
		}

		List<ValueSetReference[]> results = new LinkedList<>();

		for (int i = 0; i < refSets.length; i++) {
			List<ValueSetReference> result = new LinkedList<>();

			for (int j = 0; j < refSets[i].length; j++)
				result.addAll(Arrays.asList(extend(refSets[i][j],
						referredType(valueType, refSets[i][j]),
						maxDepth - depths[i][j])));

			ValueSetReference[] resultArray = new ValueSetReference[result
					.size()];

			result.toArray(resultArray);
			results.add(resultArray);
		}

		ValueSetReference[][] ret = new ValueSetReference[results.size()][];

		results.toArray(ret);
		return ret;
	}

	/**
	 * Extends the given value set reference, as many as possible, to at most
	 * "toMax" more recursive levels, results in a set of value set references.
	 * 
	 * @param ref
	 *            the value set reference where extends from
	 * @param referredType
	 *            the type of an element in the value subset referred by the
	 *            given value set reference
	 * @param toMax
	 *            the extra (at most) more recursive levels to extend to
	 * @return a set of extended value set references
	 */
	private ValueSetReference[] extend(ValueSetReference ref,
			SymbolicType referredType, int toMax) {
		return extendWorker(new ValueSetReference[] { ref }, referredType,
				toMax);
	}

	/**
	 * the recursive helper method for
	 * {@link #extend(ValueSetReference, SymbolicType, int)}
	 */
	private ValueSetReference[] extendWorker(ValueSetReference refs[],
			SymbolicType referredType, int toMax) {
		if (toMax == 0)
			return refs;
		switch (referredType.typeKind()) {
		case ARRAY: {
			SymbolicCompleteArrayType arrType = (SymbolicCompleteArrayType) referredType;
			NumericExpression extent = arrType.extent();
			ValueSetReference[] results = new ValueSetReference[refs.length];

			for (int i = 0; i < results.length; i++)
				results[i] = vsArraySectionReference(refs[i],
						numericFactory.zeroInt(), extent,
						numericFactory.oneInt());
			return extendWorker(results, arrType.elementType(), toMax - 1);
		}
		case TUPLE: {
			SymbolicTupleType tupleType = (SymbolicTupleType) referredType;
			int numTypes = tupleType.sequence().numTypes();
			List<ValueSetReference> results = new LinkedList<>();

			for (int i = 0; i < numTypes; i++) {
				ValueSetReference[] expandedRefs = new ValueSetReference[refs.length];

				for (int j = 0; j < refs.length; j++)
					expandedRefs[j] = vsTupleComponentReference(refs[j],
							objectFactory.intObject(i));

				results.addAll(Arrays.asList(extendWorker(expandedRefs,
						tupleType.sequence().getType(i), toMax - 1)));
			}

			ValueSetReference[] ret = new ValueSetReference[results.size()];

			results.toArray(ret);
			return ret;
		}
		case UNION: {
			SymbolicUnionType unionType = (SymbolicUnionType) referredType;
			int numTypes = unionType.sequence().numTypes();
			List<ValueSetReference> results = new LinkedList<>();

			for (int i = 0; i < numTypes; i++) {
				ValueSetReference[] expandedRefs = new ValueSetReference[refs.length];

				for (int j = 0; j < refs.length; j++)
					expandedRefs[j] = vsUnionMemberReference(refs[j],
							objectFactory.intObject(i));

				results.addAll(Arrays.asList(extendWorker(expandedRefs,
						unionType.sequence().getType(i), toMax - 1)));
			}

			ValueSetReference[] ret = new ValueSetReference[results.size()];

			results.toArray(ret);
			return ret;
		}
		// unimplemented:
		case MAP:
		case SET:
			throw new SARLException(
					"Unimplemented symbolic types: " + referredType);
		// basics:
		case BOOLEAN:
		case CHAR:
		case FUNCTION:
		case INTEGER:
		case REAL:
		case UNINTERPRETED:
			assert toMax >= 0;
			return refs;
		default:
			throw new SARLException("Unreachable");
		}
	}

	/**
	 * <p>
	 * Deletes a set of value set references <code>D</code> from a given set
	 * <code>R</code>, results in <code>R' = R - D</code> and guarantees that
	 * for each <code>d in D</code>, there is exact one
	 * <code>ancestor(d) in R'</code>.
	 * </p>
	 * 
	 * @param refs
	 *            the given set of value set references <code>R</code>
	 * @return a subset of the given value set </ode>R'</code> references as
	 *         described above
	 */
	private ValueSetReference[] deleteSubReferences(ValueSetReference refs[]) {
		ValueSetReference[] copies = Arrays.copyOf(refs, refs.length);
		Set<Integer> deletingIndices = new HashSet<>();

		Arrays.sort(copies, new Comparator<ValueSetReference>() {
			@Override
			public int compare(ValueSetReference o1, ValueSetReference o2) {
				return depth(o1) - depth(o2);
			}
		});
		for (int i = 0; i < copies.length; i++) {
			// if any ref with shorter depth than me (copies[i]) is one of my
			// ancestor:
			for (int j = 0; j < i; j++) {
				ValueSetReference ancestor = copies[i];
				boolean isSubRef = false;

				// Note that if a deleting ref "r" is an ancestor of me, there
				// must be another non-deleting ref which is an ancestor for
				// both "r" and me.
				while (true) {
					if (ancestor == copies[j]) {
						deletingIndices.add(i);
						isSubRef = true;
						break;
					}
					if (ancestor.isIdentityReference())
						break;
					ancestor = ((NTValueSetReference) ancestor).getParent();
				}
				if (isSubRef)
					break;
			}
		}
		if (deletingIndices.size() > 0) {
			ValueSetReference[] results = new ValueSetReference[copies.length
					- deletingIndices.size()];
			int ct = 0;

			for (int i = 0; i < copies.length; i++)
				if (!deletingIndices.contains(i))
					results[ct++] = copies[i];
			assert ct == results.length;
			return results;
		}
		return copies;
	}

	/**
	 * Returns the type of an element in the value subset referred by the given
	 * value set reference.
	 * 
	 * @param valueType
	 *            the type of the value where the given value set reference
	 *            refers to
	 * @param ref
	 *            a value set reference
	 * @return the type of an element in the value subset referred by the given
	 *         value set reference
	 */
	private SymbolicType referredType(SymbolicType valueType,
			ValueSetReference ref) {
		switch (ref.valueSetReferenceKind()) {
		case ARRAY_ELEMENT:
		case ARRAY_SECTION:
			return ((SymbolicArrayType) referredType(valueType,
					((NTValueSetReference) ref).getParent())).elementType();
		case IDENTITY:
			return valueType;
		case TUPLE_COMPONENT: {
			VSTupleComponentReference tupleRef = (VSTupleComponentReference) ref;

			return ((SymbolicTupleType) referredType(valueType,
					tupleRef.getParent())).sequence()
							.getType(tupleRef.getIndex().getInt());
		}
		case UNION_MEMBER: {
			VSUnionMemberReference tupleRef = (VSUnionMemberReference) ref;

			return ((SymbolicUnionType) referredType(valueType,
					tupleRef.getParent())).sequence()
							.getType(tupleRef.getIndex().getInt());
		}
		case OFFSET:
			throw new SARLException("Unsupported value set reference kind: "
					+ ref.valueSetReferenceKind()
					+ " for getting the referred type from value type: "
					+ valueType);
		default:
			throw new SARLException("Unknown value set reference kind: "
					+ ref.valueSetReferenceKind());
		}
	}
}
