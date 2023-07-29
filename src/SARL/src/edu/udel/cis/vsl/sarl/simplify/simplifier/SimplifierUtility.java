/*******************************************************************************
 * Copyright (c) 2013 Stephen F. Siegel, University of Delaware.
 * 
 * This file is part of SARL.
 * 
 * SARL is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * SARL is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with SARL. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package edu.udel.cis.vsl.sarl.simplify.simplifier;

import java.io.PrintStream;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import edu.udel.cis.vsl.sarl.IF.SARLException;
import edu.udel.cis.vsl.sarl.IF.SARLInternalException;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericSymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.IF.number.IntegerNumber;
import edu.udel.cis.vsl.sarl.IF.number.Number;
import edu.udel.cis.vsl.sarl.IF.number.NumberFactory;
import edu.udel.cis.vsl.sarl.IF.number.RationalNumber;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject.SymbolicObjectKind;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;
import edu.udel.cis.vsl.sarl.expr.IF.BooleanExpressionFactory;
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
import edu.udel.cis.vsl.sarl.simplify.IF.Simplify;
import edu.udel.cis.vsl.sarl.util.Pair;

/**
 * An object that gathers together a variety of objects as fields needed to
 * perform simplification.
 * 
 * @author xxxx
 * 
 */
public class SimplifierUtility {

	/** Where debugging output should go. */
	PrintStream out;

	/** Print lots of stuff. */
	boolean verbose = false;

	/** The universe used to create new symbolic objects */
	PreUniverse universe;

	/**
	 * The instance of {@link IdealFactory} used to create new ideal
	 * expressions.
	 */
	IdealFactory idealFactory;

	/** The factory used to implement infinite precision numbers */
	NumberFactory numberFactory;

	/** The factory used to produce {@link Range}s. */
	RangeFactory rangeFactory;

	/** The factory used to produce new {@link BooleanSymbolicExpression}s. */
	BooleanExpressionFactory booleanFactory;

	/** The boolean constant "true" */
	BooleanExpression trueExpr;

	/** The boolean constant "false". */
	BooleanExpression falseExpr;

	/** An ordering on symbolic constants. */
	Comparator<SymbolicConstant> variableComparator;

	/** An ordering on {@link Monic}s. */
	Comparator<Monic> monicComparator;

	SimplifierUtility(PreUniverse universe, IdealFactory idealFactory) {
		this.idealFactory = idealFactory;
		this.universe = universe;
		this.booleanFactory = idealFactory.booleanFactory();
		this.falseExpr = (BooleanExpression) universe.bool(false);
		this.trueExpr = (BooleanExpression) universe.bool(true);
		this.numberFactory = universe.numberFactory();
		this.rangeFactory = Simplify.newIntervalUnionFactory();
		this.out = System.out;
		// do this once and put it in universe...
		this.variableComparator = new Comparator<SymbolicConstant>() {
			Comparator<SymbolicType> typeComparator = universe.typeFactory()
					.typeComparator();

			@Override
			public int compare(SymbolicConstant o1, SymbolicConstant o2) {
				int result = o1.name().compareTo(o2.name());

				if (result != 0)
					return result;
				return typeComparator.compare(o1.type(), o2.type());
			}
		};
		this.monicComparator = idealFactory.monicComparator();
	}

	// Static methods ...

	/**
	 * Determines if the operator is one of the relation operators
	 * {@link SymbolicOperator#LESS_THAN},
	 * {@link SymbolicOperator#LESS_THAN_EQUALS},
	 * {@link SymbolicOperator#EQUALS}, or {@link SymbolicOperator#NEQ}.
	 * 
	 * @param operator
	 *            a non-<code>null</code> symbolic operator
	 * @return <code>true</code> iff <code>operator</code> is one of the four
	 *         relational operators
	 */
	public static boolean isRelational(SymbolicOperator operator) {
		switch (operator) {
		case LESS_THAN:
		case LESS_THAN_EQUALS:
		case EQUALS:
		case NEQ:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Determines whether the expression is a numeric relational expression,
	 * i.e., the operator is one of the four relation operators and argument 0
	 * has numeric type.
	 * 
	 * @param expression
	 *            any non-<code>null</code> symbolic expression
	 * @return <code>true</code> iff the expression is relational with numeric
	 *         arguments
	 */
	public static boolean isNumericRelational(SymbolicExpression expression) {
		return isRelational(expression.operator())
				&& ((SymbolicExpression) expression.argument(0)).isNumeric();
	}

	/**
	 * Searches for a "true" primitive (i.e., an instance of {@link Primitive}
	 * which is not a {@link Polynomial}) in the expression <code>expr</code>.
	 * The search is recursive on the structure but backtracks as soon as a node
	 * which is not a {@link RationalExpression} is encountered.
	 * 
	 * @param expr
	 * @return
	 */
	public static Primitive findATruePrimitive(Monomial m) {
		if (m instanceof Primitive && !(m instanceof Polynomial))
			return (Primitive) m;
		switch (m.operator()) {
		case ADD:
		case MULTIPLY:
			int n = m.numArguments();

			for (int i = 0; i < n; i++) {
				SymbolicObject arg = m.argument(i);
				Primitive p = findATruePrimitive((Monomial) arg);

				if (p != null)
					return p;
			}
			return null;
		case POWER:
			return findATruePrimitive((Monomial) m.argument(0));
		default:
			return null;
		}
	}

	/**
	 * Is the given expression a "simple constant": "NULL", a concrete boolean,
	 * int, number, or string? If so, there is nothing to do --- it is its own
	 * simplification.
	 * 
	 * @param x
	 *            any non-{@code null} symbolic expression
	 * @return {@code true} iff {@code x} is a simple constant
	 */
	public static boolean isSimpleConstant(SymbolicExpression x) {
		if (x.isNull())
			return true;

		SymbolicOperator operator = x.operator();

		if (operator == SymbolicOperator.CONCRETE) {
			SymbolicObject object = (SymbolicObject) x.argument(0);
			SymbolicObjectKind kind = object.symbolicObjectKind();

			switch (kind) {
			case BOOLEAN:
			case INT:
			case NUMBER:
			case STRING:
				return true;
			default:
			}
		}
		return false;
	}

	/**
	 * Is the object a "simple object", i.e., one which is its own
	 * simplification?
	 * 
	 * @param object
	 * @return if {@code true}, the object is a simple object
	 */
	public static boolean isSimpleObject(SymbolicObject object) {
		switch (object.symbolicObjectKind()) {
		case BOOLEAN:
		case INT:
		case NUMBER:
		case STRING:
		case CHAR:
			return true;
		case EXPRESSION:
			return SimplifierUtility
					.isSimpleConstant((SymbolicExpression) object);
		case SEQUENCE:
			return false;
		case TYPE:
			return isSimpleType((SymbolicType) object);
		case TYPE_SEQUENCE:
			return false;
		default:
			throw new SARLInternalException("unreachable");
		}
	}

	/**
	 * Is this a simple type --- i.e., one that is its own simplification.
	 * 
	 * @param type
	 *            a non-{@code null} type
	 * @return {@code true} iff {@code type} is a simple type
	 */
	public static boolean isSimpleType(SymbolicType type) {
		switch (type.typeKind()) {
		case BOOLEAN:
		case INTEGER:
		case REAL:
		case CHAR:
		case UNINTERPRETED:
			return true;
		default:
		}
		return false;
	}

	public static Set<SymbolicConstant> newDirtySet() {
		return new HashSet<>();
	}

	/**
	 * Clones the given set, assuming that the given set was produced by cloning
	 * a dirty set, or was a dirty set obtained from somewhere else.
	 * 
	 * @param set
	 *            a dirty set
	 * @return a shallow clone
	 */
	@SuppressWarnings("unchecked")
	public static Set<SymbolicConstant> cloneDirtySet(
			Set<SymbolicConstant> set) {
		return (Set<SymbolicConstant>) ((HashSet<SymbolicConstant>) set)
				.clone();
	}

	/**
	 * Makes a shallow copy of a {@link TreeMap} (the keys and values are not
	 * cloned). Provided as a convenience method because it consumes a
	 * {@link Map} which is assumed to be a {@link TreeMap}, and gets the type
	 * parameters right.
	 * 
	 * @param map
	 *            either {@code null} or an instance of {@link TreeMap}
	 * @return a shallow clone of {@code map}
	 */
	@SuppressWarnings("unchecked")
	public static <S, T> TreeMap<S, T> cloneTreeMap(Map<S, T> map) {
		if (map == null)
			return null;
		else
			return (TreeMap<S, T>) ((TreeMap<?, ?>) map).clone();
	}

	/**
	 * Prints the entries of a {@link Map}, putting one entry on each line for
	 * better readability of large {@link Map}s. Flushes the stream at the end.
	 * 
	 * @param out
	 *            the stream to which to print the map
	 * @param map
	 *            a non-<code>null</code> {@link Map} to print
	 */
	public static <S, T> void printMap(PrintStream out, Map<S, T> map) {
		for (Entry<S, T> entry : map.entrySet()) {
			out.println("  " + entry.getKey() + " : " + entry.getValue());
		}
		out.flush();
	}

	/**
	 * Determines whether any free variable occurring in {@code expr} is in
	 * {@code set}.
	 * 
	 * @param expr
	 *            a symbolic expression
	 * @param set
	 *            a set of variables
	 * @return {@code true} iff there is a free variable x occurring in
	 *         {@code expr} and {@code set} contains x
	 */
	public static boolean intersects(SymbolicExpression expr,
			Set<SymbolicConstant> set) {
		Set<SymbolicConstant> set1 = expr.getFreeVars();
		Set<SymbolicConstant> set2;

		// make set1 the bigger of the two sets
		if (set1.size() >= set.size()) {
			set2 = set;
		} else {
			set2 = set1;
			set1 = set;
		}
		for (SymbolicConstant x : set2) {
			if (set1.contains(x))
				return true;
		}
		return false;
	}

	// Instance methods ...

	// Private methods...

	/**
	 * Represents an expression of the form aX+b, where X is a
	 * "pseudo primitive polynomial", and a and b are concrete numbers.
	 * 
	 * <p>
	 * A {@link Polynomial} X is a pseudo-primitive polynomial if all of the
	 * following hold:
	 * 
	 * <ol>
	 * <li>X is a polynomial with no constant term</li>
	 * <li>the leading coefficient of X is positive</li>
	 * <li>if X is real: leading coefficient is 1</li>
	 * <li>if X is int: the gcd of the absolute values of the coefficients of X
	 * is 1</li>
	 * </ol>
	 * 
	 * If aX=0, then X is <code>null</code> and coefficient is 0.
	 * </p>
	 */
	private class AffineExpr {

		private Monic pseudo; /* maybe null */

		private Number coefficient; /* not null */

		private Number offset; /* not null */

		AffineExpr(Monic pseudo, Number coefficient, Number offset) {
			// assert coefficient != null;
			// assert offset != null;
			// assert iff(pseudo == null, coefficient.signum() == 0);
			this.pseudo = pseudo;
			this.coefficient = coefficient;
			this.offset = offset;
		}
	}

	/**
	 * Computes a representation of the given {@link Polynomial} as an
	 * {@link AffineExpression} aX+b, where X is in pseudo form.
	 * 
	 * Also guarantees that if a=1 and b=0, the pseudo X will ==
	 * <code>poly</code> (not just .equals). This provides an easy way to
	 * determine whether the affine expression is "trivial".
	 */
	private AffineExpr affine(Polynomial poly) {
		SymbolicType type = poly.type();
		IntegerNumber degree = poly.polynomialDegree(numberFactory);

		// any instance of Polynomial has nonnegative degree.
		// The term map must be non-empty.
		if (degree.isZero()) { // fp is constant
			return new AffineExpr(null,
					type.isInteger() ? numberFactory.zeroInteger()
							: numberFactory.zeroRational(),
					((Constant) poly).number());
		} else {
			// first, subtract off constant term (if it is non-0).
			// then factor out best you can:
			// if real: factor out leading coefficient (unless it is 1)
			// if int: take gcd of coefficients and factor that out (unless it
			// is 1)
			Number constantTerm = poly.constantTerm(idealFactory).number();
			Number coefficient;
			Monic pseudo;

			if (constantTerm.isZero()) {
				// the polynomial is already normal, so nothing to do
				coefficient = type.isInteger() ? numberFactory.oneInteger()
						: numberFactory.oneRational();
				pseudo = poly;
			} else {
				// better: one must be last, so remove last element
				// note: after removing one, resulting map might
				// have one entry
				Monomial difference = idealFactory
						.factorTermMap(idealFactory.polynomialFactory()
								.removeKey(poly.termMap(idealFactory),
										(Monic) idealFactory.one(type)));

				pseudo = difference.monic(idealFactory);
				coefficient = difference.monomialConstant(idealFactory)
						.number();
			}
			return new AffineExpr(pseudo, coefficient, constantTerm);
		}
	}

	// Package-private methods ...

	/**
	 * Converts a numeric relational expression to a constraint on a
	 * {@link Monic}. Returns <code>null</code> if this is not possible.
	 * 
	 * Precondition: <code>relationalExpr</code> is any non-<code>null</code>
	 * {@link BooleanExpression}
	 * 
	 * @param relationalExpr
	 * @return a pair consisting of a monic and a range such that the relational
	 *         expression is equivalent to the constraint that the monic lies in
	 *         that range, or <code>null</code>
	 */
	Pair<Monic, Range> comparisonToRange(BooleanExpression relationalExpr) {
		SymbolicOperator op = relationalExpr.operator();

		if (!isRelational(op))
			return null;

		SymbolicExpression left = (SymbolicExpression) relationalExpr
				.argument(0);
		SymbolicType type = left.type();

		if (!type.isNumeric())
			return null;

		boolean isInteger = type.isInteger();
		NumberFactory nf = numberFactory;
		RangeFactory rf = rangeFactory;
		boolean leftIsZero = left.isZero();
		Monic expr = leftIsZero ? (Monic) relationalExpr.argument(1)
				: (Monic) left;
		Number pos_inf = isInteger ? nf.positiveInfinityInteger()
				: nf.positiveInfinityRational();
		Number neg_inf = isInteger ? nf.negativeInfinityInteger()
				: nf.negativeInfinityRational();
		Range range;
		Monic monic;

		if (expr instanceof Polynomial) {
			// aX+b<0, aX+b<=0, aX+b==0, aX+b>=0, aX+b>0, aX+b!=0
			// convert to inequality on X
			Polynomial poly = (Polynomial) expr;
			AffineExpr affine = affine(poly);
			Number a = affine.coefficient;
			Number b = affine.offset;
			RationalNumber a_rat, b_rat;

			monic = affine.pseudo;
			if (isInteger) {
				a_rat = nf.integerToRational((IntegerNumber) a);
				b_rat = nf.integerToRational((IntegerNumber) b);
			} else {
				a_rat = (RationalNumber) a;
				b_rat = (RationalNumber) b;
			}

			RationalNumber c = nf.negate(nf.divide(b_rat, a_rat));
			boolean aIsNeg = a.signum() < 0;

			switch (op) {
			case LESS_THAN:
				assert !isInteger;
				if (leftIsZero == aIsNeg) {
					// 0<aX+b and a<0 => X<-b/a=c
					// aX+b<0 and a>0 => X<-b/a=c
					// if (isInteger)
					// range = rf.interval(null, true,
					// nf.ceil(nf.decrement(c)), false, true);
					// else
					range = rf.interval(false, neg_inf, true, c, true);
				} else { // X>c
					// if (isInteger)
					// range = rf.interval(nf.floor(nf.increment(c)), false,
					// null, true, true);
					// else
					range = rf.interval(false, c, true, pos_inf, true);
				}
				break;
			case LESS_THAN_EQUALS:
				if (leftIsZero == aIsNeg) // X<=c
					range = rf.interval(isInteger, neg_inf, true,
							isInteger ? nf.floor(c) : c, false);
				else // X>=c
					range = rf.interval(isInteger, isInteger ? nf.ceil(c) : c,
							false, pos_inf, true);
				break;
			case EQUALS: // aX+b=0, X=c.
				if (isInteger)
					range = nf.isIntegral(c) ? rf.singletonSet(nf.floor(c))
							: rf.emptySet(true);
				else
					range = rf.singletonSet(c);
				break;
			case NEQ: // aX+b!=0, X!=c
				if (isInteger) {
					if (nf.isIntegral(c)) {
						range = rf.complement(rf.singletonSet(nf.floor(c)));
					} else {
						range = rf.interval(true, neg_inf, true, pos_inf, true);
					}
				} else {
					range = rf.complement(rf.singletonSet(c));
				}
				break;
			default:
				throw new SARLException("unreachable");
			}
		} else { // expr is not a Polynomial, just a Monic
			// X<0, X<=0, X==0, X>=0, X>0, X!=0
			Number zero = isInteger ? nf.zeroInteger() : nf.zeroRational();

			monic = expr;
			switch (op) {
			case LESS_THAN:
				assert !isInteger;
				if (leftIsZero) { // X>0
					range = rf.interval(false, zero, true, pos_inf, true);
				} else { // X<0
					range = rf.interval(false, neg_inf, true, zero, true);
				}
				break;
			case LESS_THAN_EQUALS:
				if (leftIsZero) { // X>=0
					range = rf.interval(isInteger, zero, false, pos_inf, true);
				} else { // X<=0
					range = rf.interval(isInteger, neg_inf, true, zero, false);
				}
				break;
			case EQUALS: // X==0
				range = rf.singletonSet(zero);
				break;
			case NEQ: // X!=0
				range = rf.complement(rf.singletonSet(zero));
				break;
			default:
				throw new SARLException("unreachable");
			}
		}
		return new Pair<>(monic, range);
	}

	/**
	 * Determines whether <code>constraint</code> has the form a*X +b ? 0, where
	 * ? is one of less-than, less-than-or-equal-to, not-equal-to; X is
	 * <code>var</code>, and a and b are symbolic expressions that do not
	 * involve X.
	 * 
	 * @param var
	 *            the variable X on which to focus
	 * @param constraint
	 *            the boolean expression to analyze
	 * @return <code>true</code> iff <code>constraint</code> has the form
	 *         specified above
	 */
	boolean isLinearInequality(NumericSymbolicConstant var,
			BooleanExpression constraint) {
		SymbolicOperator op = constraint.operator();

		if (op != SymbolicOperator.LESS_THAN_EQUALS
				&& op != SymbolicOperator.LESS_THAN
				&& op != SymbolicOperator.NEQ)
			return false;

		NumericExpression arg0 = (NumericExpression) constraint.argument(0);
		NumericExpression arg1 = (NumericExpression) constraint.argument(1);
		Monic expr;

		if (arg0.isZero()) { // 0 <= arg1 = v+e
			expr = (Monic) arg1;
		} else {
			assert arg1.isZero();
			expr = (Monic) arg0;
		}

		IdealFactory idf = idealFactory;
		Monomial[] terms = expr.expand(idf);
		boolean degreeOneTermFound = false;

		// every term should have v-degree at most 1
		// and at least one term must have degree 1.
		for (Monomial term : terms) {
			boolean degOneFactorFound = false;

			for (PrimitivePower pp : term.monic(idf).monicFactors(idf)) {
				if (pp.equals(var)) {
					degOneFactorFound = true;
				} else if (universe.getFreeSymbolicConstants(pp)
						.contains(var)) {
					return false;
				}
			}
			degreeOneTermFound = degreeOneTermFound || degOneFactorFound;
		}
		return degreeOneTermFound;
	}

	// Public methods ...

	public PreUniverse getUniverse() {
		return universe;
	}

	public NumberFactory getNumberFactory() {
		return numberFactory;
	}

	public RangeFactory getRangeFactory() {
		return rangeFactory;
	}

	public BooleanExpressionFactory getBooleanFactory() {
		return booleanFactory;
	}

	public IdealFactory getIdealFactory() {
		return idealFactory;
	}

	public BooleanExpression trueExpr() {
		return trueExpr;
	}

	public BooleanExpression falseExpr() {
		return falseExpr;
	}

	/**
	 * Compute the minimum of two numbers. Infinities are allowed.
	 * 
	 * TODO: add this to NumberFactory.
	 * 
	 * @param a
	 *            any non-{@code null} SARL {@link Number}
	 * @param b
	 *            any non-{@code null} SARL {@link Number}
	 * @return the minimum of the {@code a} and {@code b}
	 */
	public Number min(Number a, Number b) {
		return numberFactory.compare(a, b) >= 0 ? b : a;
	}

	/**
	 * Compute the maximum of two numbers. Infinities are allowed.
	 * 
	 * TODO: add this to NumberFactory.
	 * 
	 * @param a
	 *            any non-{@code null} SARL {@link Number}
	 * @param b
	 *            any non-{@code null} SARL {@link Number}
	 * @return the maximum of the {@code a} and {@code b}
	 */
	public Number max(Number a, Number b) {
		return numberFactory.compare(a, b) >= 0 ? a : b;
	}

	/**
	 * Transforms a claim that a non-constant monomial lies in a range to an
	 * equivalent (normalized) form in which the monomial is a {@link Monic},
	 * and if that {@link Monic} is a {@link Polynomial}, its constant term is
	 * 0. It has the property that the original monomial is in the original
	 * range iff the new monic is in the new range. The new range may be empty.
	 * 
	 * @param monomial
	 *            a non-<code>null</code>, non-{@link Constant} {@link Monomial}
	 * @param range
	 *            a non-<code>null</code> {@link Range}, with the same type as
	 *            <code>monomial</code>
	 * @return a pair consisting of a {@link Monic} and a {@link Range}
	 */
	public Pair<Monic, Range> normalize(Monomial monomial, Range range) {
		assert !(monomial instanceof Constant);
		while (true) {
			if (!(monomial instanceof Monic)) {
				Constant c = monomial.monomialConstant(idealFactory);

				// cx in R -> x in R/c
				// Note that the "divide" method below is precise for integer.
				// ex: 2x in [3,5] -> x in [2,2].
				// ex: 2x in [3,3] -> x in emptyset.
				monomial = monomial.monic(idealFactory);
				range = rangeFactory.divide(range, c.number());
			}
			// now monomial is a Monic
			if (monomial instanceof Polynomial) {
				Polynomial poly = (Polynomial) monomial;
				Constant constantTerm = poly.constantTerm(idealFactory);
				Number constantTermNumber = constantTerm.number();

				if (constantTermNumber.isZero())
					break;
				range = rangeFactory.subtract(range, constantTermNumber);
				monomial = (Monomial) universe.subtract(poly, constantTerm);
			} else {
				break;
			}
		}
		return new Pair<>((Monic) monomial, range);
	}

	/**
	 * <p>
	 * Normalizes a constraint of the form <code>monomial = number</code>. This
	 * returns a {@link Pair} (m,a) in which the {@link Monic} m is normal,
	 * i.e., if m is a {@link Polynomial} then its constant term is 0. It has
	 * the property that m=a iff <code>monomial = number</code>. If it is
	 * determined that the equality is unsatisfiable (e.g., 2x=3, where x is an
	 * integer), then this method returns {@code null}.
	 * </p>
	 * 
	 * <p>
	 * Effect is similar to that of {@link #standardizeMonomialPair(Pair)},
	 * except this method is optimized for the case where the value is a
	 * concrete {@link Number}.
	 * </p>
	 * 
	 * @param monomial
	 *            a non-{@code null} {@link Monomial} m
	 * @param number
	 *            a SARL {@link Number} of the same type as {@code monomial}
	 * @return a pair (m,a) where m is normal and m=a iff monomial=number, or
	 *         {@code null} if the equality is unsatisfiable.
	 */
	public Pair<Monic, Number> normalize(Monomial monomial, Number number) {
		boolean isInt = monomial.type().isInteger();

		while (true) {
			if (!(monomial instanceof Monic)) {
				Number c = monomial.monomialConstant(idealFactory).number();

				if (isInt && !numberFactory
						.mod((IntegerNumber) number, (IntegerNumber) c)
						.isZero())
					return null;
				monomial = monomial.monic(idealFactory);
				number = numberFactory.divide(number, c);
			}
			// now monomial is a Monic
			if (monomial instanceof Polynomial) {
				Polynomial poly = (Polynomial) monomial;
				Constant constantTerm = poly.constantTerm(idealFactory);
				Number constantTermNumber = constantTerm.number();

				if (constantTermNumber.isZero())
					break;
				number = numberFactory.subtract(number, constantTermNumber);
				monomial = (Monomial) universe.subtract(poly, constantTerm);
			} else {
				break;
			}
		}
		return new Pair<>((Monic) monomial, number);
	}
}
