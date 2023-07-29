package edu.udel.cis.vsl.sarl.simplify.simplifier;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import edu.udel.cis.vsl.sarl.IF.SARLConstants;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.IF.number.IntegerNumber;
import edu.udel.cis.vsl.sarl.IF.number.Interval;
import edu.udel.cis.vsl.sarl.IF.number.Number;
import edu.udel.cis.vsl.sarl.IF.number.NumberFactory;
import edu.udel.cis.vsl.sarl.IF.number.RationalNumber;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;
import edu.udel.cis.vsl.sarl.ideal.IF.Constant;
import edu.udel.cis.vsl.sarl.ideal.IF.IdealFactory;
import edu.udel.cis.vsl.sarl.ideal.IF.Monic;
import edu.udel.cis.vsl.sarl.ideal.IF.Monomial;
import edu.udel.cis.vsl.sarl.ideal.IF.Polynomial;
import edu.udel.cis.vsl.sarl.ideal.IF.Primitive;
import edu.udel.cis.vsl.sarl.ideal.IF.PrimitivePower;
import edu.udel.cis.vsl.sarl.ideal.IF.RationalExpression;
import edu.udel.cis.vsl.sarl.preuniverse.IF.PreUniverse;
import edu.udel.cis.vsl.sarl.simplify.IF.Range;
import edu.udel.cis.vsl.sarl.simplify.IF.RangeFactory;
import edu.udel.cis.vsl.sarl.simplify.norm.GaussianNormalizer;
import edu.udel.cis.vsl.sarl.simplify.norm.MultiOrNormalizer;
import edu.udel.cis.vsl.sarl.simplify.norm.Normalizer;
import edu.udel.cis.vsl.sarl.simplify.norm.NormalizerChain;
import edu.udel.cis.vsl.sarl.simplify.norm.RangeNormalizer;
import edu.udel.cis.vsl.sarl.simplify.norm.SubstitutionNormalizer;
import edu.udel.cis.vsl.sarl.simplify.norm.TupleNormalizer;
import edu.udel.cis.vsl.sarl.util.Pair;
import edu.udel.cis.vsl.sarl.util.WorkMap;

/**
 * A structured representation of a boolean formula (assumption), used for
 * simplifying symbolic expressions. The two main data structures in this
 * representation are the substitution map and the range map. The substitution
 * map maps symbolic expressions to symbolic expressions and is used to replace
 * subexpressions with their simplified forms. The range map maps {@link Monic}s
 * to {@link Range}s and specifies a concrete range for each {@link Monic}.
 * 
 * @author Stephen F. Siegel (xxxx)
 */
public class Context implements ContextIF {

	// Static fields...

	public final static boolean debug = false;

	// Instance Fields ...

	/**
	 * A cache of all simplifications computed under this {@link Context}. For
	 * any entry (x,y), the following formula must be valid: context -> x=y.
	 * 
	 */
	private Map<SymbolicObject, SymbolicObject> simplificationCache = null;

	/**
	 * Should backwards substitution be used to solve for variables in terms of
	 * other variables?
	 */
	final boolean backwardsSub;

	/**
	 * An object that gathers together references to various tools that are
	 * needed for this class to do its work.
	 */
	SimplifierUtility util;

	/**
	 * <p>
	 * The essential substitution map. When simplifying an expression, any
	 * occurrence of the key of an entry in this map will be replaced by the
	 * value of the entry.
	 * </p>
	 *
	 * Invariants:
	 * 
	 * <ul>
	 * 
	 * <li>If the value of an entry is non-null, it will have a type compatible
	 * with that of the key.</li>
	 * 
	 * <li>No key occurs as a subexpression of any value or of any other key.
	 * </li>
	 * 
	 * <li>For each entry, the key is a {@link SymbolicConstant} or the value is
	 * concrete. [Note: this may change in the future.]</li>
	 * 
	 * <li>If the type of an entry is real, the key is a {@link Monic}. If that
	 * {@link Monic} is a {@link Polynomial}, its constant term is 0.</li>
	 * 
	 * <li>If the type of an entry is integer, the key and value are
	 * {@link Monomial}s, the coefficients of those two {@link Monomials} are
	 * relatively prime, and the key's coefficient is positive. WHY? Suppose
	 * 2X=3Y is an entry. Then X=(3*Y)/2. But the latter form loses information
	 * because the "/" is integer division. It loses the information that Y is
	 * even. However, if the key is a symbolic constant, it is a monic, and if
	 * the value is concrete, then you can always divide both side by the
	 * coefficient, so under the current assumption, the key will always be
	 * monic, and if a polynomial the constant term is 0, exactly as for reals.
	 * 
	 * TODO: this is very confusing. Doesn't it contradict the statement above
	 * that a key is a symbolic constant or the value is concrete????</li>
	 * 
	 * </ul>
	 * 
	 * Example: subMap: wx->1, y->x Applied to wy -> wx -> 1. Hence substitution
	 * is not necessarily idempotent, even with these assumptions
	 * 
	 * TODO: think about replacing all/some of these memory-inefficient maps
	 * with sorted arrays, at least after this Context has been initialized.
	 */
	Map<SymbolicExpression, SymbolicExpression> subMap;

	/**
	 * <p>
	 * Map giving precise range of {@link Monic}s. Associates to each
	 * {@link Monic} a {@link Range} such that the set of all possible values
	 * the monic can assume are contained in that range. Monics that have a
	 * single concrete value are removed from this map and placed in the
	 * {@link #subMap}. No solved variables can occur in this map.
	 * </p>
	 * 
	 * <p>
	 * This map is used to form the assumption (full and reduced).
	 * </p>
	 */
	WorkMap<Monic, Range> rangeMap;

	/**
	 * The symbolic constants that are involved in substitution map or range map
	 * entries that have been modified since the last normalization. An entry
	 * that does not contain any variables in this set should not need to be
	 * normalized.
	 */
	Set<SymbolicConstant> theDirtySet = new HashSet<>();

	// Constructors ...

	/**
	 * Constructs new {@link Context} with all empty maps. This represents the
	 * assumption "true". No initialization is done.
	 * 
	 * @param util
	 *            info structure with references to commonly-used factories and
	 *            other objects
	 * @param useBackwardSubstitution
	 *            should this {@link Context} use backwards substitution after
	 *            Gaussian elimination to remove additional symbolic constants
	 *            from the context?
	 */
	Context(SimplifierUtility util, boolean useBackwardSubstitution) {
		this.util = util;
		this.subMap = new TreeMap<>(util.universe.comparator());
		this.rangeMap = new WorkMap<>(util.idealFactory.monicComparator());
		this.backwardsSub = useBackwardSubstitution;
		this.simplificationCache = new HashMap<>();
	}

	/**
	 * Constructs new {@link Context} with given fields. Initialization is
	 * carried out.
	 * 
	 * @param util
	 *            info structure with references to commonly-used factories and
	 *            other objects
	 * @param subMap
	 *            substitution map; see {@link #subMap}
	 * @param rangeMap
	 *            range map; see {@link #rangeMap}
	 * @param useBackwardSubstitution
	 *            should this {@link Context} use backwards substitution after
	 *            Gaussian elimination to remove additional symbolic constants
	 *            from the context?
	 */
	Context(SimplifierUtility util,
			Map<SymbolicExpression, SymbolicExpression> subMap,
			WorkMap<Monic, Range> rangeMap, boolean useBackwardSubstitution) {
		this.util = util;
		this.subMap = subMap;
		this.rangeMap = rangeMap;
		this.backwardsSub = useBackwardSubstitution;
		initialize(util.trueExpr);
	}

	/**
	 * Create context from the given assumption. The assumption is parsed and
	 * processed to populate the fields of this context.
	 * 
	 * @param util
	 *            info structure with references to commonly-used factories and
	 *            other objects
	 * @param assumption
	 *            the assumption this context will represent
	 * @param useBackwardSubstitution
	 *            should this {@link Context} use backwards substitution after
	 *            Gaussian elimination to remove additional symbolic constants
	 *            from the context?
	 */
	Context(SimplifierUtility util, BooleanExpression assumption,
			boolean useBackwardSubstitution) {
		this(util, useBackwardSubstitution);
		initialize(assumption);
	}

	// Private methods ....

	/**
	 * <p>
	 * Transforms a pair of {@link Monomial} by dividing both elements by an
	 * appropriate constant so that (1) if the type is real, the coefficient for
	 * the left component is 1; (2) if the type is integer, the coefficient for
	 * the left component is positive and the GCD of the absolute values of the
	 * left and right coefficients is 1.
	 * </p>
	 * 
	 * <p>
	 * Example: x is an int, (3x,2). This should be an inconsistency. But
	 * (3x,2y) could be OK. It implies x=(2y) INTDIV 3, but is stronger than
	 * that formula. It is equivalent to (real)x = 2((real)y)/3, but it is
	 * debatable whether you should make this substitution.
	 * </p>
	 * 
	 * @param pair
	 *            a pair of non-<code>null</code> {@link Monomial}s of the same
	 *            type
	 * @throws InconsistentContextException
	 *             if an inconsistency is detected in the course of simplifying
	 *             the equality
	 */
	private void monicizeMonomialPair(Pair<Monomial, Monomial> pair)
			throws InconsistentContextException {
		Monomial x = pair.left;

		if (x instanceof Monic)
			return;

		Monomial y = pair.right;
		IdealFactory idf = util.idealFactory;
		NumberFactory nf = util.numberFactory;
		PreUniverse universe = util.universe;
		Constant constant = x.monomialConstant(idf);
		Monic xMonic = x.monic(idf);

		if (x.type().isReal()) {
			pair.left = xMonic;
			pair.right = (Monomial) universe.divide(y, constant);
		} else {
			IntegerNumber yCoefficient = (IntegerNumber) y.monomialConstant(idf)
					.number();
			IntegerNumber xCoefficient = (IntegerNumber) constant.number();
			boolean negate = xCoefficient.signum() < 0;
			IntegerNumber xCoefficientAbs = negate ? nf.negate(xCoefficient)
					: xCoefficient;
			IntegerNumber gcd = nf.gcd(xCoefficientAbs,
					(IntegerNumber) nf.abs(yCoefficient));
			Monic yMonic = y.monic(idf);

			if (gcd.isOne()) {
				if (negate) {
					pair.left = (Monomial) universe.minus(x);
					pair.right = (Monomial) universe.minus(y);
				}
			} else {
				if (yMonic.isOne() && gcd != xCoefficientAbs) {
					// something like 3x=2 can't hold, but 2x=4 is fine...
					throw new InconsistentContextException();
				}
				pair.left = idf.monomial(
						idf.constant(nf.divide(xCoefficientAbs, gcd)), xMonic);
				pair.right = idf
						.monomial(
								idf.constant(negate
										? nf.negate(
												nf.divide(yCoefficient, gcd))
										: nf.divide(yCoefficient, gcd)),
								yMonic);
			}
		}
	}

	/**
	 * <p>
	 * Given a substitution x->y in which both x and y are {@link Monomial}s,
	 * transforms that substitution into a standard form. In the standard form,
	 * x and y are the equivalent symbolic expressions, x is a {@link Monic},
	 * and if x is a {@link Polynomial} then its constant term is 0. Moreover,
	 * (1) if the type is real, the coefficient for the left component is 1; (2)
	 * if the type is integer, the coefficient for the left component is
	 * positive and the GCD of the absolute values of the left and right
	 * coefficients is 1.
	 * </p>
	 * <p>
	 * If in the process of transformation it is determined that x and y are
	 * equivalent, both fields of the pair will be set to <code>null</code>.
	 * </p>
	 * 
	 * @param pair
	 *            a substitution pair specifying a value x and the value y that
	 *            is to be substituted for x
	 * @throws InconsistentContextException
	 *             if the type is integer and an inconsistency is detected such
	 *             as 2x->3
	 */
	private void standardizeMonomialPair(Pair<Monomial, Monomial> pair)
			throws InconsistentContextException {
		IdealFactory idf = util.idealFactory;
		PreUniverse universe = util.universe;

		while (true) {
			monicizeMonomialPair(pair);
			if (pair.left instanceof Polynomial) {
				Polynomial poly = (Polynomial) pair.left;
				Constant c = poly.constantTerm(idf);

				if (c.isZero())
					break;
				else {
					pair.left = (Monomial) universe.subtract(poly, c);
					pair.right = (Monomial) universe.subtract(pair.right, c);
				}
			} else { // pair.left is a Monic and not a Polynomial
				break;
			}
		}
		if (pair.left.equals(pair.right)) {
			pair.left = null;
			pair.right = null;
		} else if (pair.left.isOne() && pair.right instanceof Constant) {
			throw new InconsistentContextException();
		}
	}

	/**
	 * Given a normalized {@link Monic} and a {@link Range} range, computes the
	 * intersection of {@code range} with the current known range of the monic
	 * based on the {@link #subMap} and {@link #rangeMap} of this
	 * {@link Context}.
	 * 
	 * @param monic
	 *            a normalized {@link Monic}
	 * @param range
	 *            a {@link Range} of the same type as the monic
	 * @return the intersection of {@code range} with the current known range of
	 *         {@link Monic} based on the entries of the {@link #subMap} and
	 *         {@link #rangeMap}
	 */
	private Range intersectWithRangeOf(Monic monic, Range range) {
		SymbolicExpression value = getSub(monic);

		if (value instanceof Constant) {
			Number number = ((Constant) value).number();

			if (range.containsNumber(number)) {
				range = util.rangeFactory.singletonSet(number);
			} else {
				range = util.rangeFactory.emptySet(range.isIntegral());
			}
		} else {
			Range oldRange = getRange(monic);

			if (oldRange != null)
				range = util.rangeFactory.intersect(range, oldRange);
		}
		return range;
	}

	/**
	 * Computes an (over-)estimate of the possible values of a
	 * {@link Polynomial} based on the current assumptions of this
	 * {@link Context}.
	 * 
	 * @param poly
	 *            a non-{@code null} {@link Polynomial}
	 * @return a {@link Range} of concrete values such that the result of
	 *         evaluating {@code poly} at any point satisfying the assumptions
	 *         of this context will lie in that range
	 */
	private Range computeRange(Polynomial poly) {
		IdealFactory idf = util.idealFactory;
		RangeFactory rf = util.rangeFactory;
		Constant constantTerm = poly.constantTerm(idf);
		Number constant = constantTerm.number();
		Range result;

		if (constant.isZero()) {
			result = rf.singletonSet(constant);
			for (Monomial term : poly.termMap(idf)) {
				result = rf.add(result, computeRange(term));
				if (result.isUniversal())
					break;
			}
			result = intersectWithRangeOf(poly, result);
		} else {
			result = rf.add(
					computeRange((Monomial) idf.subtract(poly, constantTerm)),
					constant);
		}
		return result;
	}

	/**
	 * Computes an (over-)estimate of the possible values of a power expression
	 * based on the current assumptions of this {@link Context}.
	 * 
	 * Currently, this is a very rough over-estimate that only tries to get
	 * right the signedness (greater than 0, greater than or equal to 0, etc.).
	 * 
	 * @param base
	 *            the base in the power expression (can have real or integer
	 *            type)
	 * @param exponent
	 *            the exponent in the power expression (must have same type as
	 *            {@code base})
	 * @return a {@link Range} of concrete values such that the result of
	 *         evaluating base raised to the exponent power at any point
	 *         satisfying the assumptions of this context will lie in that range
	 */
	private Range computeRangeOfPower(RationalExpression base,
			RationalExpression exponent) {
		IdealFactory idf = util.idealFactory;
		RangeFactory rf = util.rangeFactory;
		NumberFactory nf = util.numberFactory;
		boolean isIntegral = base.type().isInteger();
		Number zero = isIntegral ? util.numberFactory.zeroInteger()
				: util.numberFactory.zeroRational();

		// if base>0, then base^exponent>0:
		if (simplify(idf.isPositive(base)).isTrue())
			return rf.interval(isIntegral, zero, true,
					nf.infiniteNumber(isIntegral, true), true);

		// if base>=0, then base^exponent>=0:

		Range ge0 = rf.interval(isIntegral, zero, false,
				nf.infiniteNumber(isIntegral, true), true);

		if (simplify(idf.isNonnegative(base)).isTrue())
			return ge0;

		// if exponent is not integral or is even, base^exponent>=0:

		Number exponentNumber = idf.extractNumber(exponent);

		if (exponentNumber != null) {
			if (exponentNumber instanceof IntegerNumber) {
				IntegerNumber exponentInteger = (IntegerNumber) exponentNumber;

				if (nf.mod(exponentInteger, nf.integer(2)).isZero()) {
					return ge0;
				}
			} else {
				if (!nf.isIntegral((RationalNumber) exponentNumber))
					return ge0;
				else {
					IntegerNumber exponentInteger = nf
							.integerValue((RationalNumber) exponentNumber);

					if (nf.mod(exponentInteger, nf.integer(2)).isZero())
						return ge0;
				}
			}
		}
		return rf.universalSet(isIntegral);
	}

	/**
	 * Computes an (over-)estimate of the possible values of a {@link Primitive}
	 * based on the current assumptions of this {@link Context}.
	 * 
	 * @param primitive
	 *            a non-{@code null} {@link Primitive} expression
	 * @return a {@link Range} of concrete values such that the result of
	 *         evaluating {@code primitive} at any point satisfying the
	 *         assumptions of this context will lie in that range
	 */
	private Range computeRange(Primitive primitive) {
		if (primitive instanceof Polynomial)
			return computeRange((Polynomial) primitive);
		if (primitive.operator() == SymbolicOperator.POWER)
			return computeRangeOfPower(
					(RationalExpression) primitive.argument(0),
					(RationalExpression) primitive.argument(1));

		SymbolicExpression value = getSub(primitive);

		if (value instanceof Constant)
			return util.rangeFactory.singletonSet(((Constant) value).number());

		Range oldRange = getRange(primitive);

		if (oldRange != null)
			return oldRange;
		if (primitive.operator() == SymbolicOperator.MODULO)
			return computeDefaultModRange((Monomial) primitive.argument(0),
					(Monomial) primitive.argument(1));
		return util.rangeFactory.universalSet(primitive.type().isInteger());
	}

	/**
	 * Computes an (over-)estimate of the possible values of a
	 * {@link PrimitivePower} based on the current assumptions of this
	 * {@link Context}.
	 * 
	 * @param pp
	 *            a non-{@code null} {@link PrimitivePower}
	 * @return a {@link Range} of concrete values such that the result of
	 *         evaluating {@code pp} at any point satisfying the assumptions of
	 *         this context will lie in that range
	 */
	private Range computeRange(PrimitivePower pp) {
		if (pp instanceof Primitive)
			return computeRange((Primitive) pp);

		IntegerNumber exponent = pp.monomialDegree(util.numberFactory);
		Range result = util.rangeFactory
				.power(computeRange(pp.primitive(util.idealFactory)), exponent);

		result = intersectWithRangeOf(pp, result);
		return result;
	}

	/**
	 * Computes an (over-)estimate of the possible values of a {@link Monic}
	 * based on the current assumptions of this {@link Context}.
	 * 
	 * @param monic
	 *            a non-{@code null} {@link Monic}
	 * @return a {@link Range} of concrete values such that the result of
	 *         evaluating {@code monic} at any point satisfying the assumptions
	 *         of this context will lie in that range
	 */
	private Range computeRange(Monic monic) {
		if (monic instanceof PrimitivePower)
			return computeRange((PrimitivePower) monic);

		RangeFactory rf = util.rangeFactory;
		NumberFactory nf = util.numberFactory;
		Range result = rf.singletonSet(
				monic.type().isInteger() ? nf.oneInteger() : nf.oneRational());

		for (PrimitivePower pp : monic.monicFactors(util.idealFactory)) {
			result = rf.multiply(result, computeRange(pp));
			if (result.isUniversal())
				break;
		}
		result = intersectWithRangeOf(monic, result);
		return result;
	}

	/**
	 * Computes an (over-)estimate of the possible values of a {@link Monomial}
	 * based on the current assumptions of this {@link Context}.
	 * 
	 * @param monomial
	 *            a non-{@code null} {@link Monomial}
	 * @return a {@link Range} of concrete values such that the result of
	 *         evaluating {@code monomial} at any point satisfying the
	 *         assumptions of this context will lie in that range
	 */
	private Range computeRange(Monomial monomial) {
		if (monomial instanceof Monic)
			return computeRange((Monic) monomial);
		if (monomial instanceof Constant)
			return util.rangeFactory
					.singletonSet(((Constant) monomial).number());
		return util.rangeFactory.multiply(
				computeRange(monomial.monic(util.idealFactory)),
				monomial.monomialConstant(util.idealFactory).number());
	}

	/**
	 * Computes a default range for a%b. Recall a = (a div b)*b + a%b. The sign
	 * of a%b is the sign of a. (a div b) is rounded towards 0.
	 * 
	 * Case 1: a>=0 and b>0. Then a%b is in [0,min(a,b-1)].
	 * 
	 * Case 2: a>=0 and b<0. Then a%b is in [0,min(a,-b-1)].
	 * 
	 * Case 3: a<=0 and b>0. Then a%b is in [max(a,1-b),0].
	 * 
	 * Case 4: a<=0 and b<0. Then a%b is in [max(a,1+b),0].
	 * 
	 * If ab>=0, a%b is in [0,b-1]. If ab<=0, a%b is in [1-b,0]. In any case,
	 * a%b is in [1-b,b-1].
	 * 
	 * The behavior is undefined if b could be 0.
	 * 
	 * @param a
	 *            the dividend, an integer expression
	 * @param b
	 *            the divisor, an integer expression
	 * @return a conservative concrete range on a%b under the assumptions of
	 *         this {@link Context}
	 */
	private Range computeDefaultModRange(Monomial a, Monomial b) {
		RangeFactory rf = util.rangeFactory;
		NumberFactory nf = util.numberFactory;
		Interval b_interval = computeRange(b).intervalOverApproximation();
		Interval a_interval = computeRange(a).intervalOverApproximation();
		Range result = null;

		if (a_interval.isEmpty() || b_interval.isEmpty())
			return rf.emptySet(true);
		if (a_interval.lower().signum() >= 0) {
			Number right;

			if (b_interval.lower().signum() >= 0) // [0,min(a,b-1)]
				right = nf.decrement(b_interval.upper());
			else if (b_interval.upper().signum() <= 0) // [0,min(a,-b-1)]
				right = nf.negate(nf.increment(b_interval.lower()));
			else
				right = util.max(nf.decrement(b_interval.upper()),
						nf.negate(nf.increment(b_interval.lower())));
			right = util.min(a_interval.upper(), right);
			result = rf.interval(true, nf.zeroInteger(), false, right,
					right.isInfinite());
		} else if (a_interval.upper().signum() <= 0) {
			Number left;

			if (b_interval.lower().signum() >= 0) // [max(a,1-b),0]
				left = nf.increment(nf.negate(b_interval.upper()));
			else if (b_interval.upper().signum() <= 0) // [max(a,1+b),0]
				left = nf.increment(b_interval.lower());
			else
				left = util.min(nf.increment(nf.negate(b_interval.upper())),
						nf.increment(b_interval.lower()));
			left = util.max(a_interval.lower(), left);
			result = rf.interval(true, left, left.isInfinite(),
					nf.zeroInteger(), false);
		}
		return result == null ? rf.universalSet(true) : result;
	}

	/**
	 * <p>
	 * Inserts an entry into the {@link #subMap} and also checks for an
	 * inconsistency between the old and new values, if an old value existed.
	 * </p>
	 * 
	 * <p>
	 * Preconditions: the {@code key} and {@code value} must satisfy the
	 * invariants described for {@link #subMap}.
	 * </p>
	 * 
	 * @param key
	 *            the expression on the left side of the substitution
	 * @param value
	 *            the expression on the right side which will replace the
	 *            {@code key}
	 * @param dirtySet
	 *            the free symbolic constants involved in the key and value will
	 *            be added to this set
	 * @return the old value associated to {@code key}, or {@code null} if there
	 *         was no entry for {@code key}
	 * @throws InconsistentContextException
	 *             if the old value was a concrete value and not equal to the
	 *             new value
	 */
	private SymbolicExpression updateSub(SymbolicExpression key,
			SymbolicExpression value, Set<SymbolicConstant> dirtySet)
			throws InconsistentContextException {
		SymbolicExpression old = subMap.put(key, value);

		if (old != value) {
			clearSimplifications();
			dirtySet.addAll(key.getFreeVars());
			dirtySet.addAll(value.getFreeVars());
			if (old != null) {
				switch (value.type().typeKind()) {
				case BOOLEAN:
				case CHAR:
				case INTEGER:
				case REAL:
				case UNINTERPRETED:
					if (value.operator() == SymbolicOperator.CONCRETE
							&& old.operator() == SymbolicOperator.CONCRETE) {
						throw new InconsistentContextException();
					}
				default:
				}
			}
		}
		return old;
	}

	// Package-private methods ...

	/**
	 * Computes the boolean formula represented by this context.
	 * 
	 * @param full
	 *            should the formula include the equalities giving the values of
	 *            the solved variables?
	 * @return the boolean formula as specified
	 */
	BooleanExpression getAssumption(boolean full) {
		BooleanExpression result = util.trueExpr;

		for (Entry<SymbolicExpression, SymbolicExpression> subEntry : subMap
				.entrySet()) {
			SymbolicExpression key = subEntry.getKey();

			if (full || !(key instanceof SymbolicConstant))
				result = util.universe.and(result,
						util.universe.equals(key, subEntry.getValue()));
		}
		for (Entry<Monic, Range> rangeEntry : rangeMap.entrySet())
			result = util.universe.and(result,
					rangeEntry.getValue().symbolicRepresentation(
							rangeEntry.getKey(), util.universe));
		// for (List<ArrayFact> list : arrayFacts.values())
		// for (ArrayFact fact : list)
		// result = info.universe.and(result, arrayFactToExpression(fact));
		return result;
	}

	/**
	 * Attempts to find, in the context, a clause which states the
	 * differentiability of the given <code>function</code>. This is a clause
	 * with operator {@link SymbolicOperator#DIFFERENTIABLE} and with the
	 * function argument (argument 0) equal to <code>function</code>.
	 * 
	 * @param function
	 *            the function for which a differentiability claim is sought
	 * @return a clause in the context dealing with the differentiability of
	 *         <code>function</code>, or <code>null</code> if no such clause is
	 *         found.
	 */
	BooleanExpression findDifferentiableClaim(SymbolicExpression function) {
		for (Entry<SymbolicExpression, SymbolicExpression> entry : getSubEntries()) {
			if (!entry.getValue().isTrue())
				continue;

			BooleanExpression clause = (BooleanExpression) entry.getKey();

			if (clause.operator() != SymbolicOperator.DIFFERENTIABLE)
				continue;
			if (clause.argument(0).equals(function))
				return clause;
		}
		return null;
	}

	/**
	 * Initializes this context by consuming and analyzing the given assumption
	 * and updating all data structures to represent the assumption in a
	 * structured way. After initialization, this context is basically immutable
	 * (an exception being the {@link #simplificationCache}).
	 * 
	 * @param assumption
	 *            the boolean expression which is to be represented by this
	 *            context
	 */
	void initialize(BooleanExpression assumption) {
		if (debug)
			System.out.println("Creating context : " + assumption);
		assume(assumption);
		normalize();
	}

	/**
	 * Adds all entries in the {@link #subMap} to the specified map.
	 * 
	 * @param map
	 *            a map to which the entries of the {@link #subMap} will be
	 *            added
	 */
	void addSubsToMap(Map<SymbolicExpression, SymbolicExpression> map) {
		map.putAll(subMap);
	}

	/**
	 * Gets the variables that have been "solved", i.e., have an expression in
	 * terms of other (unsolved) variables. These variables can be entirely
	 * eliminated from the state.
	 * 
	 * @return mapping from solved variables to their values
	 */
	Map<SymbolicConstant, SymbolicExpression> getSolvedVariables() {
		Map<SymbolicConstant, SymbolicExpression> solvedVariables = new TreeMap<>(
				util.variableComparator);

		for (Entry<SymbolicExpression, SymbolicExpression> entry : getSubEntries()) {
			SymbolicExpression key = entry.getKey();

			if (key instanceof SymbolicConstant)
				solvedVariables.put((SymbolicConstant) key, entry.getValue());
		}
		return solvedVariables;
	}

	/**
	 * If this assumption is exactly equivalent to the claim that the given
	 * symbolic constant lies in some interval, returns that interval.
	 * Otherwise, returns {@code null}.
	 * 
	 * @param symbolicConstant
	 *            the symbolic constant
	 * @return the interval or {@code null}
	 */
	Interval assumptionAsInterval(SymbolicConstant symbolicConstant) {
		if (!subMap.isEmpty()) {
			if (!rangeMap.isEmpty() || subMap.size() != 1)
				return null;

			Entry<SymbolicExpression, SymbolicExpression> entry = subMap
					.entrySet().iterator().next();

			if (!entry.getKey().equals(symbolicConstant))
				return null;

			SymbolicExpression value = entry.getValue();

			if (!(value instanceof Constant))
				return null;
			return util.numberFactory
					.singletonInterval(((Constant) value).number());
		}
		if (rangeMap.size() == 1) {
			Entry<Monic, Range> entry = rangeMap.entrySet().iterator().next();

			if (!entry.getKey().equals(symbolicConstant))
				return null;

			Range range = entry.getValue();

			return range.asInterval();
		}
		return null;
	}

	/**
	 * Returns a map consisting of all entries in the substitution map of this
	 * {@link Context} and all of its ancestors. An entry from a child overrides
	 * an entry with the same key from the parent.
	 * 
	 * @return a map consisting of all subMap entries from this context and its
	 *         ancestors
	 */
	Map<SymbolicExpression, SymbolicExpression> getFullSubMap() {
		return subMap;
	}

	/**
	 * <p>
	 * Returns the collapsed context. That is the context obtained by
	 * "collapsing" this context and all of its super-contexts into a single
	 * context.
	 * </p>
	 * 
	 * <p>
	 * In this case, since this has no super-context, this method just returns
	 * <code>this</code>.
	 * </p>
	 * 
	 * <p>
	 * This method may be overridden in subclasses.
	 * </p>
	 * 
	 * @return <code>this</code>
	 */
	Context collapse() {
		return this;
	}

	/**
	 * A combination of collapsing and cloning: the context returned will not be
	 * a sub-context, will be equivalent to this, but will not be this, not will
	 * it share the substitution map or range map of this.
	 * 
	 * @return a collapsed clone of this context
	 */
	Context collapsedClone() {
		return this.clone();
	}

	/**
	 * Looks up the given {@link Monic} in this context's range map. If not
	 * found there, look in parent. Override me to form a sub-context.
	 * 
	 * @param key
	 *            a {@link Monic}
	 * @return the value associated to that key in the range map of this context
	 *         or one of its ancestors, or {@code null} if that monic does not
	 *         occur as a key in this context or any of its ancestors
	 */
	Range getRange(Monic key) {
		return rangeMap.get(key);
	}

	/**
	 * Puts this {@link Context} into the "inconsistent" state. This occurs when
	 * the assumption is determined to be inconsistent, i.e., equivalent to
	 * <i>false</i>. All maps are cleared except for the single entry
	 * <code>false -> true</code> in the {@link #subMap}.
	 */
	void makeInconsistent() {
		rangeMap.clear();
		subMap.clear();
		putSub(util.falseExpr, util.trueExpr);
	}

	// Public methods ...

	/**
	 * Gets the range map of this context. Does not include anything from any
	 * ancestor.
	 * 
	 * @return this context's range map, {@link #rangeMap}
	 */
	public WorkMap<Monic, Range> getRangeMap() {
		return rangeMap;
	}

	/**
	 * Constructs an instance of {@link LinearSolver} that can be used to
	 * simplify the {@link #subMap} of this {@link Context}.
	 * 
	 * @return a linear solver for simplifying the {@link #subMap}, or
	 *         {@code null} if no simplifications are possible
	 */
	public LinearSolver getLinearSolver() {
		if (subMap.isEmpty())
			return null;
		return LinearSolver.reduce(util, subMap, util.monicComparator,
				backwardsSub);
	}

	/**
	 * Clears the simplification cache, {@link #simplificationCache}.
	 */
	public void clearSimplifications() {
		if (simplificationCache != null)
			simplificationCache.clear();
	}

	/**
	 * <p>
	 * Places a substitution pair into a standard form. If the original pair is
	 * (x,y) and the new pair is (x',y') then the following formula will be
	 * valid: x=y -> x'=y'.
	 * </p>
	 * 
	 * <p>
	 * If in the new pair, x and y are the same symbolic expression, both
	 * components of the pair will be set to <code>null</code>.
	 * </p>
	 * 
	 * @param pair
	 *            a substitution pair
	 * @throws InconsistentContextException
	 *             if the type is integer and an inconsistency is detected such
	 *             as 2x->3
	 */
	public void standardizePair(
			Pair<SymbolicExpression, SymbolicExpression> pair)
			throws InconsistentContextException {
		if (pair.left == pair.right) {
			pair.left = null;
			pair.right = null;
			return;
		}
		if (pair.left.type().isIdeal()) {
			if (pair.left.operator() == SymbolicOperator.CAST) {
				// if x has type hint, (int)x->y should be changed to x->(hint)y
				SymbolicType type = pair.left.type();
				SymbolicExpression original = (SymbolicExpression) pair.left
						.argument(0);
				SymbolicType originalType = original.type();

				if (originalType.isHerbrand()
						&& originalType.isInteger() == type.isInteger()) {
					// problem: original expression might not be Monomial. Could
					// be RationalExpression like x/y. Questionable whether such
					// a thing should occur on right side of substitution. If it
					// does, should form the equality expression and process
					// from the beginning.
					pair.left = original;
					pair.right = util.universe.cast(originalType, pair.right);
					return;
				}
			}
			if (!(pair.left instanceof Monomial
					&& pair.right instanceof Monomial)) {
				BooleanExpression equation = util.universe.equals(pair.left,
						pair.right);

				pair.left = (Monomial) equation.argument(0);
				pair.right = (Monomial) equation.argument(1);
			}
			assert pair.left instanceof Monomial;
			assert pair.right instanceof Monomial;

			@SuppressWarnings("unchecked")
			Pair<Monomial, Monomial> monomialPair = (Pair<Monomial, Monomial>) (Pair<?, ?>) pair;

			standardizeMonomialPair(monomialPair);
		}
	}

	/**
	 * Looks up an entry in the substitution map of this context. This method is
	 * overridden in the SubContext class.
	 * 
	 * @param key
	 *            the key to look up
	 * @return the value associated to that key in the substitution map. This is
	 *         the value that will always be substituted for {@code key} when a
	 *         symbolic expression is simplified by this {@link Context}.
	 */
	public SymbolicExpression getSub(SymbolicExpression key) {
		return subMap.get(key);
	}

	/**
	 * Get subMap entries just from this context proper, not from super-context
	 * (if any).
	 * 
	 * @return the entries in {@link #subMap}
	 */
	public Set<Entry<SymbolicExpression, SymbolicExpression>> getSubEntries() {
		return subMap.entrySet();
	}

	/**
	 * Returns the simplifier utility used by this context. That object provides
	 * references to many different commonly used fields, and basic utility
	 * methods.
	 * 
	 * @return the simplifier utility
	 */
	public SimplifierUtility getInfo() {
		return util;
	}

	/**
	 * Enter the given data into this context's simplification cache. Future
	 * simplifications of the key will take place quickly by looking it up in
	 * the cache.
	 * 
	 * @param a
	 *            symbolic object
	 * @param value
	 *            the simplified version of that symbolic object
	 */
	public void cacheSimplification(SymbolicObject key, SymbolicObject value) {
		simplificationCache.put(key, value);
	}

	/**
	 * Retrieves the simplified version of an object from this context's
	 * simplification cache.
	 * 
	 * @param key
	 *            a symbolic object
	 * @return the cached result of simplification or {@code null} if there is
	 *         no such cached result
	 */
	public SymbolicObject getSimplification(SymbolicObject key) {
		return simplificationCache.get(key);
	}

	/**
	 * Computes an (over-)estimate of the possible values of a
	 * {@link RationalExpression} based on the current assumptions of this
	 * {@link Context}. Points at which this rational expression are undefined
	 * (because, e.g., the denominator is 0) are ignored.
	 * 
	 * @param expression
	 *            a non-{@code null} {@link RationalExpression}
	 * @return a {@link Range} of concrete values such that the result of
	 *         evaluating {@code rat} at any point satisfying the assumptions of
	 *         this context will lie in that range
	 */
	public Range computeRange(NumericExpression expression) {
		if (expression instanceof Monomial)
			return computeRange((Monomial) expression);

		IdealFactory idf = util.idealFactory;
		RationalExpression rat = (RationalExpression) expression;

		return util.rangeFactory.divide(computeRange(rat.numerator(idf)),
				computeRange(rat.denominator(idf)));
	}

	/**
	 * Enters a substitution into this context's substitution map, and clears
	 * the simplification cache.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value that will be substituted for {@code key} in future
	 *            simplifications
	 */
	public void putSub(SymbolicExpression key, SymbolicExpression value) {
		subMap.put(key, value);
		clearSimplifications();
	}

	/**
	 * Normalizes an entry for the {@link #subMap} and adds it if it results in
	 * a change to the map. Adds an entry to the {@link #subWorklist}.
	 * 
	 * @param x
	 *            the key for the new substitution
	 * @param y
	 *            the value for the new substitution
	 * @param dirtySet
	 *            symbolic constants in any entries created or modified in this
	 *            operation will be added to this set
	 * @throws InconsistentContextException
	 *             if the addition of this fact results in a contradiction in
	 *             this context
	 */
	public void addSub(SymbolicExpression x, SymbolicExpression y,
			Set<SymbolicConstant> dirtySet)
			throws InconsistentContextException {
		Pair<SymbolicExpression, SymbolicExpression> pair = new Pair<>(x, y);

		standardizePair(pair);

		SymbolicExpression newKey = pair.left;

		if (newKey == null)
			return; // a trivial substitution

		SymbolicExpression newValue = pair.right;
		SymbolicExpression oldValue = getSub(newKey);

		if (oldValue != null && oldValue.equals(newValue))
			return; // this sub is already in the subMap
		updateSub(newKey, newValue, dirtySet);
	}

	public SymbolicExpression removeSubkey(SymbolicExpression key) {
		clearSimplifications();
		return subMap.remove(key);
	}

	/**
	 * Updates the state of this {@link Context} by restricting the range of a
	 * normal {@link Monic}. This may result in changes to the {@link #rangeMap}
	 * , {@link #subMap}, or both.
	 * 
	 * @param key
	 *            a normal {@link Monic}
	 * @param range
	 *            a {@link Range} for {@code key}, with the same type
	 * @throws InconsistentContextException
	 *             if the restriction results in the {@code key} having an empty
	 *             range
	 */
	public void restrictRange(Monic key, Range range,
			Set<SymbolicConstant> dirtySet)
			throws InconsistentContextException {
		Range original = getRange(key);

		if (original == null) {
			SymbolicExpression value = getSub(key);

			if (value instanceof Constant) {
				Number number = ((Constant) value).number();

				if (range.containsNumber(number)) {
					return;
				} else {
					throw new InconsistentContextException();
				}
			}
			if (key.operator() == SymbolicOperator.MODULO) {
				Range modRange = computeDefaultModRange(
						(Monomial) key.argument(0), (Monomial) key.argument(1));

				range = util.rangeFactory.intersect(range, modRange);
			}
		} else {
			range = util.rangeFactory.intersect(original, range);
			if (range.equals(original))
				return;
		}
		if (range.isEmpty())
			throw new InconsistentContextException();

		Number value = range.getSingletonValue();

		if (value == null) {
			rangeMap.put(key, range);
			dirtySet.addAll(key.getFreeVars());
			clearSimplifications();
		} else {
			addSub(key, util.universe.number(value), dirtySet);
			if (original != null) {
				rangeMap.remove(key);
				clearSimplifications();
			}
		}
	}

	/**
	 * Simplifies a symbolic expression using the current state of this
	 * {@link Context}.
	 * 
	 * @param expr
	 *            the expression to simplify
	 * @return the simplified expression
	 */
	public SymbolicExpression simplify(SymbolicExpression expr) {
		Set<SymbolicExpression> simplificationStack = new HashSet<>();

		return new IdealSimplifierWorker(this, simplificationStack)
				.simplifyExpression(expr);
	}

	public Context getGlobalContext() {
		return this;
	}

	// Public methods implementing methods specified in ContextIF...

	@Override
	public void print(PrintStream out) {
		out.println("subMap:");
		SimplifierUtility.printMap(out, subMap);
		out.println("rangeMap:");
		SimplifierUtility.printMap(out, rangeMap);
		out.flush();
	}

	@Override
	public boolean isInconsistent() {
		SymbolicExpression result = subMap.get(util.falseExpr);

		return result != null && result.isTrue();
	}

	@Override
	public void assume(BooleanExpression expr) {
		ContextExtractor extractor = new ContextExtractor(this, theDirtySet);

		try {
			extractor.extractCNF(expr);
		} catch (InconsistentContextException e) {
			makeInconsistent();
		}
	}

	@Override
	public BooleanExpression getReducedAssumption() {
		return getAssumption(false);
	}

	@Override
	public BooleanExpression getFullAssumption() {
		return getAssumption(true);
	}

	@Override
	public void normalize() {
		try {
			Normalizer rangeNormalizer = new RangeNormalizer(this);
			Normalizer substitutionNormalizer = new SubstitutionNormalizer(
					this);
			Normalizer tupleNormalizer = new TupleNormalizer(this);
			Normalizer gaussianNormalizer = new GaussianNormalizer(this);
			Normalizer multiOrNormalizer = new MultiOrNormalizer(this);
			Normalizer subMapNormalizer;

			if (SARLConstants.useMultiOrReduction)
				subMapNormalizer = new NormalizerChain(substitutionNormalizer,
						tupleNormalizer, gaussianNormalizer, multiOrNormalizer);
			else
				subMapNormalizer = new NormalizerChain(substitutionNormalizer,
						tupleNormalizer, gaussianNormalizer);

			Normalizer normalizer = new NormalizerChain(rangeNormalizer,
					subMapNormalizer);
			Set<SymbolicConstant> dirtOut = SimplifierUtility.newDirtySet();

			normalizer.normalize(theDirtySet, dirtOut);
		} catch (InconsistentContextException e) {
			makeInconsistent();
		}
	}

	// Public methods overriding implemented methods ...

	@Override
	public Context clone() {
		return new Context(util, SimplifierUtility.cloneTreeMap(subMap),
				rangeMap.clone(), backwardsSub);
	}

	@Override
	public String toString() {
		return "Context[" + subMap + ", " + rangeMap + "]";
	}

}
