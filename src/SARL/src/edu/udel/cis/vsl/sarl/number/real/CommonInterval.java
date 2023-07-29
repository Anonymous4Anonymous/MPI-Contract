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
package edu.udel.cis.vsl.sarl.number.real;

import java.util.Objects;

import edu.udel.cis.vsl.sarl.IF.number.IntegerNumber;
import edu.udel.cis.vsl.sarl.IF.number.Interval;
import edu.udel.cis.vsl.sarl.IF.number.Number;
import edu.udel.cis.vsl.sarl.IF.number.RationalNumber;

/**
 * Immutable implementation of {@link Interval}.
 */
public class CommonInterval implements Interval {
	// Private Fields ...

	/**
	 * The boolean variable indicating the type of <code>this</code> interval.
	 * (int/real).
	 */
	private boolean isIntegral;

	/**
	 * The lower bound of <code>this</code> interval.
	 */
	private Number lower;

	/**
	 * The lower strict of <code>this</code> interval.
	 */
	private boolean strictLower;

	/**
	 * The upper bound of <code>this</code> interval.
	 */
	private Number upper;

	/**
	 * The upper strict of <code>this</code> interval.
	 */
	private boolean strictUpper;

	// Constructors ...
	/**
	 * <p>
	 * Construct a new instance of {@link CommonInterval}, which is the general
	 * implementation of {@link Interval}.
	 * </p>
	 * 
	 * <strong>Preconditions</strong>:
	 * <ul>
	 * <li>1. Both <code>upper</code> and <code>lower</code> must be non-null
	 * instance.</li>
	 * <li>2. If the <code>isIntegral</code> is <code>true</code>, then both
	 * <code>upper</code> and <code>lower</code> must be instances of
	 * {@link IntegerNumber}; else they have to be instances of
	 * {@link RationalNumber}.</li>
	 * <li>3. If <code>this</code> is a non-empty integer interval, the strict
	 * of it must be <code>false</code> when the corresponding bound is finite.
	 * </li>
	 * <li>4. If <code>this</code> is an empty interval, which is represented as
	 * (0,0), then both its bounds should be <code>true</code>; or if
	 * <code>this</code> interval's bound is infinite, the corresponding strict
	 * must be <code>true</code></li>
	 * <li>5. The <code>lower</code> bound must be less than or equal to the
	 * <code>upper</code> bound. (1) Thus, if the lower bound is infinite, it
	 * must be the negative infinity, and if the upper bound is infinite, it
	 * must be the positive infinity; (2) if both bounds are the same number and
	 * <code>this</code> interval is not empty, both strict must be
	 * <code>false</code>; (3) if both bounds are zero and both stricts are
	 * <code>true</code>, then <code>this</code> interval is an empty interval
	 * </li>
	 * </ul>
	 * 
	 * @param isIntegral
	 *            the type (int/real) of <code>this</code> interval.
	 * @param lower
	 *            the lower bound of <code>this</code> interval.
	 * @param strictLower
	 *            the lower strict of <code>this</code> interval.
	 * @param upper
	 *            the upper bound of <code>this</code> interval.
	 * @param strictUpper
	 *            the upper strict of <code>this</code> interval.
	 */
	public CommonInterval(boolean isIntegral, Number lower, boolean strictLower,
			Number upper, boolean strictUpper) {
		// Precondition 1
		assert lower != null && upper != null;
		// Precondition 2
		assert isIntegral || ((lower instanceof RationalNumber)
				&& (upper instanceof RationalNumber));
		assert !isIntegral || ((lower instanceof IntegerNumber)
				&& (upper instanceof IntegerNumber));
		// Precondition 3
		assert !isIntegral || lower.isZero()
				|| (lower.isInfinite() == strictLower);
		assert !isIntegral || upper.isZero()
				|| (upper.isInfinite() == strictUpper);
		// Precondition 4
		assert !isIntegral || !strictLower || lower.isInfinite()
				|| (lower.isZero() && upper.isZero());
		assert !isIntegral || !strictUpper || upper.isInfinite()
				|| (lower.isZero() && upper.isZero());
		assert (!lower.isInfinite() || strictLower)
				&& (!upper.isInfinite() || strictUpper);

		int compare;

		// <a,b> with a>b is unacceptable
		// (0,0) is fine: the unique representation of the empty set
		// [a,a] is fine, but not (a,a), [a,a), or (a,a]
		assert (compare = lower.numericalCompareTo(upper)) < 0
				|| (compare == 0 && ((!strictLower && !strictUpper)
						|| (lower.isZero() && strictLower && strictUpper)));
		this.isIntegral = isIntegral;
		this.lower = lower;
		this.strictLower = strictLower;
		this.upper = upper;
		this.strictUpper = strictUpper;
	}

	// Methods specified by interface "Object"

	@Override
	public CommonInterval clone() {
		return new CommonInterval(isIntegral, lower, strictLower, upper,
				strictUpper);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof CommonInterval) {
			CommonInterval that = (CommonInterval) object;

			return isIntegral == that.isIntegral
					&& strictLower == that.strictLower
					&& strictUpper == that.strictUpper
					&& upper.equals(that.upper) && lower.equals(that.lower);
		} else
			return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(isIntegral, strictLower, strictUpper, lower, upper);
	}

	@Override
	public String toString() {
		String result;

		result = strictLower ? "(" : "[";
		result += lower.isInfinite() ? "-infty" : lower.toString();
		result += ",";
		result += upper.isInfinite() ? "+infty" : upper.toString();
		result += strictUpper ? ")" : "]";
		return result;
	}

	// Methods specified by interface "Interval"

	@Override
	public Number lower() {
		return lower;
	}

	@Override
	public Number upper() {
		return upper;
	}

	@Override
	public boolean strictLower() {
		return strictLower;
	}

	@Override
	public boolean strictUpper() {
		return strictUpper;
	}

	@Override
	public boolean isIntegral() {
		return isIntegral;
	}

	@Override
	public boolean isEmpty() {
		return strictLower && strictUpper && lower.isZero() && upper.isZero();
	}

	@Override
	public boolean isUniversal() {
		return lower.isInfinite() && upper.isInfinite();
	}

	@Override
	public boolean contains(Number number) {
		if (!lower.isInfinite()) {
			int compare = lower.numericalCompareTo(number);

			if (compare > 0 || (compare == 0 && strictLower))
				return false;
		}
		if (!upper.isInfinite()) {
			int compare = upper.numericalCompareTo(number);

			if (compare < 0 || (compare == 0 && strictUpper))
				return false;
		}
		return true;
	}

	@Override
	public int compare(Number number) {
		if (!lower.isInfinite()) {
			int compare = lower.numericalCompareTo(number);

			if (compare > 0 || (compare == 0 && strictLower))
				return 1;
		}
		if (!upper.isInfinite()) {
			int compare = upper.numericalCompareTo(number);

			if (compare < 0 || (compare == 0 && strictUpper))
				return -1;
		}
		return 0;
	}

	@Override
	public boolean isZero() {
		return lower != null && upper != null && lower.isZero()
				&& upper.isZero() && !strictLower;
	}
}
