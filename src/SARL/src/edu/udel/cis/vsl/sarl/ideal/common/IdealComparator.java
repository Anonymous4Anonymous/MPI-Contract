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
package edu.udel.cis.vsl.sarl.ideal.common;

import java.util.Comparator;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentSkipListSet;

import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.number.IntegerNumber;
import edu.udel.cis.vsl.sarl.IF.number.NumberFactory;
import edu.udel.cis.vsl.sarl.IF.number.RationalNumber;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;
import edu.udel.cis.vsl.sarl.ideal.IF.Constant;
import edu.udel.cis.vsl.sarl.ideal.IF.Monic;
import edu.udel.cis.vsl.sarl.ideal.IF.Monomial;
import edu.udel.cis.vsl.sarl.ideal.IF.Polynomial;
import edu.udel.cis.vsl.sarl.ideal.IF.Primitive;
import edu.udel.cis.vsl.sarl.ideal.IF.PrimitivePower;
import edu.udel.cis.vsl.sarl.ideal.IF.RationalExpression;
import edu.udel.cis.vsl.sarl.object.IF.ObjectFactory;

/**
 * <p>
 * Comparator for ideal numeric expressions. This comparator is very heavily
 * used in most numeric operations (e.g., adding and multiplying polynomials) so
 * performance is critical.
 * </p>
 * 
 * <p>
 * The order is defined as follows. First come all expressions of integer type,
 * then all of real type. Within a type, first all the NTRationalExpression,
 * then everything else. "Everything else" are instances of Polynomial.
 * Polynomials are sorted first by degree: larger degree comes first (since
 * that's the way you typically write them). Given two polynomials of the same
 * degree:
 * </p>
 * 
 * <ul>
 * 
 * <li>if the two polynomials are monomials of the same degree, compare monics,
 * then constants</li>
 * 
 * <li>to compare two monics of the same degree: use dictionary order on the
 * primitive powers</li>
 * 
 * <li>to compare two primitive powers of same degree: compare the bases</li>
 * 
 * <li>If the two polynomials of the same degree are not monomials, then compare
 * their leading terms. If those are equal, move to the next pair of terms. Etc.
 * </li>
 * 
 * </ul>
 * 
 * @author xxxx
 * 
 */
public class IdealComparator implements Comparator<NumericExpression> {

	/**
	 * The comparator to use for all {@link SymbolicObject}s other than
	 * {@link IdealExpression}s.
	 */
	private Comparator<SymbolicObject> objectComparator;

	/**
	 * The comparator to use for comparing two {@link SymbolicType}s.
	 */
	private Comparator<SymbolicType> typeComparator;

	/**
	 * The ideal factory associated with this ideal comparator. There is a 1-1
	 * correspondence between ideal factories and ideal comparators.
	 */
	private CommonIdealFactory idealFactory;

	private NumberFactory numberFactory;

	private ObjectFactory objectFactory;

	private Comparator<Monic> monicComparator;

	private NavigableSet<Monic> monicOrderSet;

	private RationalNumber two;

	/**
	 * Constructs a new {@link IdealComparator} associated with the given ideal
	 * factory.
	 * 
	 * @param idealFactory
	 *            the ideal factory associated with the new comparator
	 */
	public IdealComparator(CommonIdealFactory idealFactory) {
		this.idealFactory = idealFactory;
		this.objectFactory = idealFactory.objectFactory();
		this.objectComparator = objectFactory.comparator();
		this.typeComparator = idealFactory.typeFactory().typeComparator();
		this.numberFactory = idealFactory.numberFactory();
		// the comparator used to insert Monics into monicOrderedSet,
		// and to getHigher and getLower:
		this.monicComparator = new Comparator<Monic>() {

			@Override
			public int compare(Monic o1, Monic o2) {
				RationalNumber order1 = o1.getOrder(), order2 = o2.getOrder();

				if (order1 == null || order2 == null)
					return compareMonicsWork(o1, o2);
				else
					return order1.numericalCompareTo(order2);
			}

		};
		this.monicOrderSet = new ConcurrentSkipListSet<>(monicComparator);
		this.two = numberFactory.rational("2");
	}

	/**
	 * Should debugging output be printed?
	 */
	public final static boolean debug = false;

	/**
	 * <p>
	 * Compares two {IdealExpression}s.
	 * </p>
	 * 
	 * <p>
	 * Implementation notes:
	 * </p>
	 * 
	 * <p>
	 * First compare types. Within a type, first all the
	 * {@link NTRationalExpression}, then everything else. "Everything else" are
	 * instances of {@link Polynomial}. {@link Polynomial}s are sorted first by
	 * degree: larger degree comes first (since that's the way you typically
	 * write them). Given two {@link Polynomial}s of the same degree:
	 * 
	 * <ul>
	 * 
	 * <li>if the two {@link Polynomial}s are {@link Monomial}s of the same
	 * degree, compare {@link Monic}s, then {@link Constant}s</li>
	 * 
	 * <li>to compare two {@link Monic}s of the same degree: use dictionary
	 * order on the {@link PrimitivePower}s</li>
	 * 
	 * <li>to compare two {@link PrimitivePower}s of same degree: compare the
	 * {@link Primitive} bases</li>
	 * 
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * Here is an example of why it is necessary to be able to compare
	 * expressions of two different types. If a has integer type and b has real
	 * type, the expression <code>a&gt;0 && b&gt;0</code> might be represented
	 * by a sorted list containing the two entries <code>a&gt;0</code> and
	 * <code>b&gt;0</code>. Hence these two entries will be compared. To compare
	 * <code>a&gt;0</code> and <code>b&gt;0</code>, the comparison first
	 * compares the operators, they are equal (both are <code>&gt</code>). Then
	 * it compares the arguments, so it will compare <code>a</code> and
	 * <code>b</code>. At this point it is comparing two expressions of
	 * different types. As an optimization, consider making a version of the
	 * comparator that assumes the two expressions have the same type.
	 * </p>
	 * 
	 * @param o1
	 *            a non-<code>null</code> {@link IdealExpression}
	 * 
	 * @param o2
	 *            a non-<code>null</code> {@link IdealExpression}
	 * @return a negative integer if <code>o1</code> occurs before
	 *         <code>o2</code> in the total order; 0 if <code>o1</code> equals
	 *         <code>o2</code>; otherwise a positive integer
	 */
	private int compareWork(NumericExpression o1, NumericExpression o2) {
		SymbolicType t1 = o1.type();
		SymbolicType t2 = o2.type();
		int result = typeComparator.compare(t1, t2);

		if (result != 0) {
			return result;
		}
		if (t1.isInteger()) {
			if (o1 instanceof Monic && o2 instanceof Monic)
				return compareMonics((Monic) o1, (Monic) o2);
			else
				return compareMonomials((Monomial) o1, (Monomial) o2);
		} else {
			return compareRationals((RationalExpression) o1,
					(RationalExpression) o2);
		}
	}

	/**
	 * Compares two {@link Polynomial}s of the same type.
	 * 
	 * @param p1
	 *            a non-<code>null</code> {@link Polynomial}
	 * @param p2
	 *            a non-<code>null</code> {@link Polynomial} of the same type as
	 *            <code>p1</code>
	 * @return a negative integer if <code>p1</code> occurs before
	 *         <code>p2</code> in the total order; 0 if they are equal;
	 *         otherwise a positive integer
	 */
	private int comparePolynomials(Polynomial p1, Polynomial p2) {
		NumberFactory nf = idealFactory.numberFactory();
		int result = nf
				.subtract(p2.polynomialDegree(nf), p1.polynomialDegree(nf))
				.signum();

		if (result != 0)
			return result;

		Monomial[] terms1 = p1.termMap(idealFactory),
				terms2 = p2.termMap(idealFactory);
		int i1 = 0, i2 = 0, n1 = terms1.length, n2 = terms2.length;

		while (i1 < n1) {
			Monomial monomial1 = terms1[i1];

			i1++;
			if (i2 < n2) {
				Monomial monomial2 = terms2[i2];

				i2++;
				result = compareMonomials(monomial1, monomial2);
				if (result != 0)
					return result;
			} else {
				return -1;
			}
		}
		if (i2 < n2)
			return 1;
		return 0;
	}

	/**
	 * Compares two {@link Monomial}s of the same type.
	 * 
	 * @param m1
	 *            a non-<code>null</code> {@link Monomial}
	 * @param m2
	 *            a non-<code>null</code> {@link Monomial} of the same type as
	 *            <code>m1</code>
	 * @return a negative integer if <code>m1</code> occurs before
	 *         <code>m2</code> in the total order; 0 if they are equal;
	 *         otherwise a positive integer
	 */
	private int compareMonomials(Monomial m1, Monomial m2) {
		NumberFactory nf = numberFactory;
		int result = nf.subtract(m2.monomialDegree(nf), m1.monomialDegree(nf))
				.signum();

		if (result != 0)
			return result;
		result = compareMonics(m1.monic(idealFactory), m2.monic(idealFactory));
		if (result != 0)
			return result;
		return compareConstants(m1.monomialConstant(idealFactory),
				m2.monomialConstant(idealFactory));
	}

	/**
	 * Inserts the monic into the {@link #monicOrderSet}, if it is not already
	 * there. The monic is inserted maintaining the total order and its order
	 * field is set to be the mean of the monic immediately preceding and
	 * succeeding it.
	 * 
	 * @param monic
	 *            the monic to insert
	 * @return the canonicalized monic
	 */
	Monic insertMonic(Monic monic) {
		if (monic.getOrder() != null) // already inserted
			return monic;

		Monic lower = monicOrderSet.lower(monic); // slow comparisons
		Monic higher = (lower == null
				? (monicOrderSet.isEmpty() ? null : monicOrderSet.first())
				: monicOrderSet.higher(lower)); // fast comparisons
		RationalNumber low = (lower == null ? numberFactory.zeroRational()
				: lower.getOrder());
		RationalNumber high = (higher == null ? numberFactory.oneRational()
				: higher.getOrder());
		RationalNumber order = numberFactory
				.divide(numberFactory.add(low, high), two);

		monic.setOrder(order);
		monicOrderSet.add(monic); // fast comparisons
		return monic;
	}

	public int compareMonics(Monic m1, Monic m2) {
		RationalNumber o1 = m1.getOrder(), o2 = m2.getOrder();

		assert o1 != null && o2 != null;

		return o1.numericalCompareTo(o2);
		// TODO: try using doubles or longs instead.
		// if they ever get too close, just re-set all of the
		// orders appropriately. You have a list of all the
		// Monics, so you can do that.
	}

	/**
	 * Compares two {@link Monic}s of the same type. A {@link Monic} of higher
	 * degree always precedes any {@link Monic} of lower degree. If they have
	 * the same degree, the dictionary order on their {@link Primitive} factors
	 * is used.
	 * 
	 * @param m1
	 *            a non-<code>null</code> {@link Monic}
	 * @param m2
	 *            a non-<code>null</code> {@link Monic} of the same type as
	 *            <code>m1</code>
	 * @return a negative integer if m1 precedes m2, 0 if they are equal, else a
	 *         positive integer
	 */
	private int compareMonicsWork(Monic m1, Monic m2) {
		NumberFactory nf = numberFactory;
		IntegerNumber degree1 = m1.monomialDegree(nf);
		IntegerNumber degree2 = m2.monomialDegree(nf);

		if (degree1.isZero()) { // the only constant monic is 1
			return degree2.signum();
			// if 0, both are 1, else m2 has higher degree
		}

		int result = nf.subtract(degree2, degree1).signum();

		if (result != 0)
			return result;

		PrimitivePower[] factors1 = m1.monicFactors(idealFactory),
				factors2 = m2.monicFactors(idealFactory);
		int n = factors1.length;

		for (int i = 0; i < n; i++) {
			PrimitivePower ppower1 = factors1[i], ppower2 = factors2[i];

			result = comparePrimitives(ppower1.primitive(idealFactory),
					ppower2.primitive(idealFactory));
			if (result != 0)
				return result;
			result = nf.subtract(ppower2.monomialDegree(nf),
					ppower1.monomialDegree(nf)).signum();
			if (result != 0)
				return result;
		}
		return 0;
	}

	/**
	 * Compares two {@link Primitive}s of the same type. All polynomials come
	 * before everything else.
	 * 
	 * @param p1
	 *            a non-<code>null</code> {@link Primitive}
	 * @param p2
	 *            a non-<code>null</code> {@link Primitive} of the same type as
	 *            <code>p1</code>
	 * @return a negative integer if p1 precedes p2, 0 if they are equals, else
	 *         a positive integer
	 */
	public int comparePrimitives(Primitive p1, Primitive p2) {
		if (p1 instanceof Polynomial) {
			if (p2 instanceof Polynomial) {
				return comparePolynomials((Polynomial) p1, (Polynomial) p2);
			} else {
				return -1;
			}
		} else {
			if (p2 instanceof Polynomial) {
				return 1;
			} else {
				int result = p1.operator().compareTo(p2.operator());

				if (result != 0)
					return result;
				else {
					int numArgs = p1.numArguments();

					result = numArgs - p2.numArguments();
					if (result != 0)
						return result;
					for (int i = 0; i < numArgs; i++) {
						result = objectComparator.compare(p1.argument(i),
								p2.argument(i));
						if (result != 0)
							return result;
					}
					return 0;
				}
			}
		}
	}

	/**
	 * Compares two {@link Constant}s. Uses the comparator in the underlying
	 * {@link NumberFactory}.
	 * 
	 * @param c1
	 *            a non-<code>null</code> {@link Constant}
	 * @param c2
	 *            a non-<code>null</code> {@link Constant}
	 * @return a negative integer if <code>c1</code> precedes <code>c2</code> in
	 *         the total order; 0 if they are equal; otherwise a positive
	 *         integer.
	 */
	private int compareConstants(Constant c1, Constant c2) {
		return idealFactory.numberFactory().compare(c1.number(), c2.number());
	}

	/**
	 * Compares two {@link RationalExpression}s of the same type.
	 * 
	 * @param e1
	 *            a non-<code>null</code> {@link RationalExpression}
	 * @param e2
	 *            a non-<code>null</code> {@link RationalExpression} of same
	 *            type as <code>e1</code>
	 * @return a negative integer if <code>e1</code> precedes <code>e2</code> in
	 *         the total order; 0 if they are equal; otherwise a positive
	 *         integer.
	 */
	private int compareRationals(RationalExpression e1, RationalExpression e2) {
		int result = compareMonomials(e1.numerator(idealFactory),
				e2.numerator(idealFactory));

		if (result != 0)
			return result;
		return compareMonomials(e1.denominator(idealFactory),
				e2.denominator(idealFactory));
	}

	/**
	 * Compares two {IdealExpression}s, printing debugging output if the
	 * {@link #debug} flag is <code>true</code>
	 * 
	 * @param o1
	 *            a non-<code>null</code> {@link IdealExpression}
	 * @param o2
	 *            a non-<code>null</code> {@link IdealExpression}
	 * @return a negative integer if <code>o1</code> occurs before
	 *         <code>o2</code> in the total order; 0 if <code>o1</code> equals
	 *         <code>o2</code>; otherwise a positive integer
	 */
	@Override
	public int compare(NumericExpression o1, NumericExpression o2) {
		return compareWork(o1, o2);
	}
}
