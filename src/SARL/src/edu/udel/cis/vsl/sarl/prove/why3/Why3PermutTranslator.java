package edu.udel.cis.vsl.sarl.prove.why3;

import java.util.LinkedList;
import java.util.List;

import edu.udel.cis.vsl.sarl.IF.UnaryOperator;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericSymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicArrayType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;
import edu.udel.cis.vsl.sarl.object.common.SimpleSequence;
import edu.udel.cis.vsl.sarl.preuniverse.IF.PreUniverse;

/**
 * <p>
 * A translation of <code>permut(a, b, l, h)</code> predicate, where
 * <code>a, b</code> are arrays and <code>l, h</code> are integral bounds.
 * </p>
 * 
 * <p>
 * The interpretation of the permut predicate is based on the bag (a.k.a
 * multi-set) theory. This interpretation is built upon the axiom that for any
 * two arrays a and b such that a is a permutation of b, iff the bag(a) equals
 * bag(b), where bag(array a) is defined as the following:
 * </p>
 * <p>
 * FORALL element e in array a, the number of occurrence of e in a equals to the
 * number of occurrence of bag(a). AND The cardinality of bag(a) equals to the
 * length of array a.
 * </p>
 * 
 * <p>
 * A function bag(a) that maps an array to a unique bag is inductively defined
 * as the following ocaml-like pseudo code: <code>
 *  bag(a) =
 *   match operator(a) with 
 *   ARRAY -> adding all elements to an empty bag
 *   ARRAY_WRITE : a' idx val ->
 *                 if (l &lt= idx &lt h) then
 *                   if (a'[idx] = val) then a
 *                   else add(val, remove(a'[idx], bag(a')))
 *                 else a
 *                 // Note: there is a side-assumption: a'[idx] in bag(a') which is 
 *                 // required to prove the predicate.
 *   DENSE_ARRAY_WRITE : a' vals[] ->
 *               bag(ARRAY_WRITE(... ARRAY_WRITE(ARRAY_WRITE(a' 0 vals[0]), 1, vals[1]) ... ))
 *   OTHER: creating a unique bag type constant for a.
 * </code>
 * </p>
 * 
 * 
 * @author xxxx
 *
 */
public class Why3PermutTranslator {

	/**
	 * A reference to {@link Why3Translator}:
	 */
	private Why3Translator why3Translator;

	/**
	 * A reference to {@link PreUniverse}:
	 */
	private PreUniverse su;

	/**
	 * Lower and higher bounds:
	 */
	private NumericExpression low, high;

	/**
	 * Translation of lower and higher bounds:
	 */
	private String lowText, highText;

	/**
	 * Assumptions that assume array elements of array <code>a</code> are in the
	 * corresponding bag of <code>a</code>.
	 */
	private List<String> elementInArrayAssumptions = new LinkedList<>();

	/**
	 * The output:
	 */
	final String result;

	Why3PermutTranslator(Why3Translator mainTranslator,
			BooleanExpression permut) {
		this.why3Translator = mainTranslator;
		this.su = mainTranslator.universe;

		String results[] = new String[2]; // assumption and assert
		String[] assumptions;

		results[1] = translatePermutPredicate(permut);
		assumptions = new String[elementInArrayAssumptions.size()];
		elementInArrayAssumptions.toArray(assumptions);
		if (assumptions.length > 1)
			results[0] = mainTranslator.interpolateOperator(assumptions,
					Why3Primitives.land);
		else if (assumptions.length == 1)
			results[0] = assumptions[0];
		else {
			result = results[1];
			return;
		}
		result = mainTranslator.interpolateOperator(results,
				Why3Primitives.implies);
	}

	/**
	 * translate a permutation predicate by interpreting the array type
	 * arguments to bags.
	 * 
	 */
	private String translatePermutPredicate(BooleanExpression permut) {
		@SuppressWarnings("unchecked")
		SimpleSequence<SymbolicExpression> args = (SimpleSequence<SymbolicExpression>) permut
				.argument(1);
		SymbolicExpression arrayA = (SymbolicExpression) args.get(0);
		SymbolicExpression arrayB = (SymbolicExpression) args.get(1);
		NumericExpression low = (NumericExpression) args.get(2);
		NumericExpression high = (NumericExpression) args.get(3);

		this.low = low;
		this.high = high;
		lowText = why3Translator.translate(low);
		highText = why3Translator.translate(high);

		String bagA = arrayToBag(arrayA);
		String bagB = arrayToBag(arrayB);
		return Why3Primitives.bag_permut.call(bagA, bagB);
	}

	private String arrayToBag(SymbolicExpression array) {
		SymbolicOperator op = array.operator();
		String translateBag;

		switch (op) {
		case ARRAY:
			translateBag = concreteArrayToBag(array);
			break;
		case ARRAY_WRITE:
			translateBag = arrayWriteToBag(array);
			break;
		case DENSE_ARRAY_WRITE:
			translateBag = denseArrayWriteToBag(array);
			break;
		default: // other cases:
			translateBag = otherCasesToBag(array);
			break;
		}
		return translateBag;
	}

	/**
	 * Translate array type expression with {@link SymbolicOperator#ARRAY_WRITE}
	 * operator
	 */
	private String arrayWriteToBag(SymbolicExpression arrayWrite) {
		SymbolicExpression array = (SymbolicExpression) arrayWrite.argument(0);
		NumericExpression idx = (NumericExpression) arrayWrite.argument(1);
		SymbolicExpression val = (SymbolicExpression) arrayWrite.argument(2);
		SymbolicExpression oldVal = su.arrayRead(array, idx);
		String bagText = arrayToBag(array);
		String idxText = why3Translator.translate(idx);
		String valText = why3Translator.translate(val);
		String oldValText = why3Translator.translate(oldVal);
		String oldValUnchangedGuardText = why3Translator
				.translate(su.equals(su.arrayRead(array, idx), val));

		elementInArrayAssumptions.add(elementInBag(oldValText, bagText));
		return this.addNewRemoveOldWorker(bagText, idxText, oldValText, valText,
				rangeGuard(idx), oldValUnchangedGuardText);
	}

	/**
	 * Translate array type expression with {@link SymbolicOperator#ARRAY}
	 * operator, which is a concrete array.
	 */
	private String concreteArrayToBag(SymbolicExpression array) {
		String bagText = Why3Primitives.empty_bag;
		int idx = 0;

		for (SymbolicObject val : array.getArguments()) {
			NumericExpression idxExpr = su.integer(idx++);
			String idxText = why3Translator.translate(idxExpr);
			String valText = why3Translator.translate((SymbolicExpression) val);

			bagText = addNewRemoveOldWorker(bagText, idxText, null, valText,
					rangeGuard(idxExpr), null);
		}
		return bagText;
	}

	/**
	 * Generate why3 translation which adds a new element v and removes an old
	 * element a[idx] from bag(a) with 2 guards for different cases :
	 * <code>l &lt= idx &lt h</code> and <code>a[idx] == v</code>.
	 * 
	 * @param bag
	 *            the translated bag from an array
	 * @param idx
	 *            the index which refers to the removing element
	 * @param oldVal
	 *            the old removing element
	 * @param val
	 *            the new adding element
	 * @param inRangeGuard
	 *            the guard tests if the given <code>idx</code> is in the range
	 *            given in the permutation predicate call.
	 * @param oldValUnchangedGuard
	 *            the guard tests if the old value equals to the new value which
	 *            means nothing changed.
	 * @return
	 */
	private String addNewRemoveOldWorker(String bag, String idx, String oldVal,
			String val, String inRangeGuard, String oldValUnchangedGuard) {
		String trueBranch;

		if (oldValUnchangedGuard != null) {
			assert oldVal != null;
			// true branch : old == val ? array : aw_bag(array, idx, old, val)
			String awBag = Why3Primitives.bag_aw.call(bag, oldVal, val);

			trueBranch = Why3Primitives.why3IfThenElse(oldValUnchangedGuard,
					bag, awBag);
		} else
			trueBranch = Why3Primitives.bag_add.call(val, bag);
		if (inRangeGuard == String.valueOf(true))
			return trueBranch;
		else if (inRangeGuard == String.valueOf(false))
			return bag;
		else
			return Why3Primitives.why3IfThenElse(inRangeGuard, trueBranch, bag);
	}

	/**
	 * Translate array type expression with
	 * {@link SymbolicOperator#DENSE_ARRAY_WRITE} operator, which is a concrete
	 * array.
	 */
	private String denseArrayWriteToBag(SymbolicExpression denseArrayWrite) {
		@SuppressWarnings("unchecked")
		Iterable<SymbolicExpression> vals = (Iterable<SymbolicExpression>) denseArrayWrite
				.argument(1);
		SymbolicExpression array = (SymbolicExpression) denseArrayWrite
				.argument(0);
		String bagText = arrayToBag(array);
		String dawBagText = bagText;
		int idx = 0;

		for (SymbolicExpression val : vals) {
			if (!val.isNull()) {
				NumericExpression idxExpr = su.integer(idx);
				SymbolicExpression oldVal = su.arrayRead(array, idxExpr);
				String idxText = why3Translator.translate(idxExpr);
				String oldValText = why3Translator.translate(oldVal);
				String oldValUnchangedGuard = why3Translator.translate(
						su.equals(su.arrayRead(array, idxExpr), val));
				String valText = why3Translator.translate(val);

				elementInArrayAssumptions
						.add(elementInBag(oldValText, bagText));
				dawBagText = addNewRemoveOldWorker(dawBagText, idxText,
						oldValText, valText, rangeGuard(idxExpr),
						oldValUnchangedGuard);
			}
			idx++;
		}
		return dawBagText;
	}

	private String otherCasesToBag(SymbolicExpression array) {
		String translateBag = why3Translator.state
				.getBagName(arraySlice(array));
		SymbolicType element = ((SymbolicArrayType) array.type()).elementType();

		why3Translator.state.addDeclaration(translateBag,
				Why3Primitives.constantDecl(translateBag, Why3Primitives
						.why3BagType(why3Translator.translateType(element))));

		return translateBag;
	}

	/**
	 * @return an assertion which asserts that the given element is in the bag.
	 */
	private String elementInBag(String element, String bag) {
		String operands[] = new String[2];

		// number of occ
		operands[1] = Why3Primitives.bag_occ.call(element, bag);
		// zero
		operands[0] = why3Translator.translate(su.zeroInt());
		// zero < number of occ
		return why3Translator.interpolateOperator(operands, Why3Primitives.lt);
	}

	private String rangeGuard(NumericExpression idx) {
		BooleanExpression simplified = su.and(su.lessThanEquals(low, idx),
				su.lessThan(idx, high));

		if (simplified.isTrue())
			return String.valueOf(true);
		else if (simplified.isFalse())
			return String.valueOf(false);
		// range guard : low <= idx < high:
		return wrap(lowText) + Why3Primitives.lte.text
				+ wrap(why3Translator.translate(idx)) + Why3Primitives.lt.text
				+ wrap(highText);
	}

	/**
	 * @param array
	 * @return return the array slice <code>array[low .. high-1]</code>
	 */
	private SymbolicExpression arraySlice(SymbolicExpression array) {
		SymbolicType elementType = ((SymbolicArrayType) array.type())
				.elementType();
		NumericExpression extent = su.length(array);
		UnaryOperator<SymbolicExpression> boundCleaner = su
				.newMinimalBoundCleaner();

		if (!this.low.isZero()) {
			NumericSymbolicConstant lambdaVar = (NumericSymbolicConstant) su
					.symbolicConstant(su.stringObject("i"), su.integerType());

			array = boundCleaner.apply(array);
			lambdaVar = (NumericSymbolicConstant) boundCleaner.apply(lambdaVar);

			SymbolicExpression function = su.lambda(lambdaVar,
					su.arrayRead(array, su.add(lambdaVar, low)));

			array = su.arrayLambda(
					su.arrayType(elementType, su.subtract(extent, low)),
					function);
		}
		if (!this.high.equals(extent)) {
			NumericSymbolicConstant lambdaVar = (NumericSymbolicConstant) su
					.symbolicConstant(su.stringObject("i"), su.integerType());

			array = boundCleaner.apply(array);
			lambdaVar = (NumericSymbolicConstant) boundCleaner.apply(lambdaVar);

			SymbolicExpression function = su.lambda(lambdaVar,
					su.arrayRead(array, lambdaVar));
			NumericExpression newExtent = su.subtract(su.length(array),
					su.subtract(extent, high));

			array = su.arrayLambda(su.arrayType(elementType, newExtent),
					function);
		}
		return array;
	}

	static private String wrap(String str) {
		return "(" + str + ")";
	}
}
