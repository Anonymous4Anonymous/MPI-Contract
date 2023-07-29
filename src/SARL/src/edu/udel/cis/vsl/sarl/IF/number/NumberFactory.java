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
package edu.udel.cis.vsl.sarl.IF.number;

import java.math.BigInteger;

import edu.udel.cis.vsl.sarl.number.real.RealInfinity;

/**
 * A number factory is used to produce concrete rational and integer numbers.
 * The rational and integer numbers live in two separate worlds. Different
 * implementations of this interface may make different choices regarding
 * numerical precision, rounding policies, and other factors.
 * 
 */
public interface NumberFactory {

	/**
	 * Returns the integer number specified by the given string. The string
	 * could be something like "7583902" or "-1" or "0". There is no bound on
	 * the length of the string.
	 */
	IntegerNumber integer(String string);

	/** Returns an integer number corresponding to the given Java int. */
	IntegerNumber integer(int value);

	/**
	 * Returns an infinite {@link IntegerNumber}, its signum is determined by
	 * the given boolean value.
	 * 
	 * @param isPositiveInfinity
	 *            A boolean value determines whether this number is positive or
	 *            negative.
	 */
	IntegerNumber infiniteInteger(boolean isPositiveInfinity);

	/**
	 * Returns the rational number specified by the given string, where the
	 * string is a decimal representation of the number. The string may be an
	 * integer string, such as "394" or "-1" or "0". Or it may contain a decimal
	 * point, as in "-3.1415" or "2." or ".234" or "-.234". There is no limit on
	 * the number of digits.
	 * 
	 */
	RationalNumber rational(String string);

	/**
	 * Returns an infinite {@link RationalNumber}, its signum is determined by
	 * the given boolean value.
	 * 
	 * @param isPositiveInfinity
	 *            A boolean value determines whether this number is positive or
	 *            negative.
	 */
	RationalNumber infiniteRational(boolean isPositiveInfinity);

	/** Makes best guest on type of number based on string. */
	Number number(String string);

	/**
	 * Returns an infinite {@link Number}, its type is determined by the first
	 * boolean argument <code>isIntegeral</code> and its signum is determined by
	 * the second boolean argument <code>isPositiveInfinity</code>.
	 * 
	 * @param isIntegeral
	 *            A boolean value determines whether this number is an integer
	 *            number or a rational one.
	 * @param isPositiveInfinity
	 *            A boolean value determines whether this number is positive or
	 *            negative.
	 * @return
	 */
	Number infiniteNumber(boolean isIntegral, boolean isPositiveInfinity);

	/** Returns absolute value of number, preserving type. */
	Number abs(Number number);

	/**
	 * Returns true iff the rational number is an integer, e.g., "3.0", or
	 * "4/2". If this method returns true, it is then safe to invoke method
	 * integerValue() on the number.
	 */
	boolean isIntegral(RationalNumber arg0);

	/**
	 * Returns the rational number which is the quotient of the two integers.
	 */
	RationalNumber fraction(IntegerNumber numerator, IntegerNumber denominator);

	/** Casts an integer to a rational number. */
	RationalNumber integerToRational(IntegerNumber integer);

	/** The rational number zero. */
	RationalNumber zeroRational();

	/** The rational number one. */
	RationalNumber oneRational();

	/** The integer number zero. */
	IntegerNumber zeroInteger();

	/** The integer number zero. */
	IntegerNumber oneInteger();

	/**
	 * Return a {@link RationalNumber} representing the positive infinity.
	 * 
	 * @return
	 */
	RationalNumber positiveInfinityRational();

	/**
	 * Return a {@link IntegerNumber} representing the positive infinity.
	 * 
	 * @return
	 */
	IntegerNumber positiveInfinityInteger();

	/**
	 * Return a {@link RationalNumber} representing the negative infinity.
	 * 
	 * @return
	 */
	RationalNumber negativeInfinityRational();

	/**
	 * Return a {@link IntegerNumber} representing the negative infinity.
	 * 
	 * @return
	 */
	IntegerNumber negativeInfinityInteger();

	/**
	 * Adds two numbers and returns result. The numbers must be of same type
	 * (integer or real), which is also the type of the result.
	 */
	Number add(Number arg0, Number arg1);

	/**
	 * Subtracts two numbers and returns result. The numbers must be of same
	 * type (integer or real), which is also the type of the result.
	 */
	Number subtract(Number arg0, Number arg1);

	/**
	 * Multiplies two numbers and returns result. The numbers must be of same
	 * type (integer or real), which is also the type of the result.
	 */
	Number multiply(Number arg0, Number arg1);

	/**
	 * Divides two numbers and returns result. The numbers must be of same type
	 * (integer or real), which is also the type of the result.
	 */
	Number divide(Number arg0, Number arg1);

	/**
	 * Negates the number, preserving the type (IntegerNumberIF or
	 * RationalNumberIF).
	 */
	Number negate(Number arg0);

	/** Adds two rational numbers and returns the result. */
	RationalNumber add(RationalNumber arg0, RationalNumber arg1);

	/** Subtracts two rational numbers and returns the result. */
	RationalNumber subtract(RationalNumber arg0, RationalNumber arg1);

	/** Multiplies two rational numbers and returns the result. */
	RationalNumber multiply(RationalNumber arg0, RationalNumber arg1);

	/**
	 * Divides two rational numbers and returns the result. An
	 * ArithmeticException is thrown if arg1 is zero.
	 */
	RationalNumber divide(RationalNumber arg0, RationalNumber arg1);

	/** Returns the negation of the given rational number, i.e., -x. */
	RationalNumber negate(RationalNumber arg0);

	/** Adds two integer numbers and returns the result. */
	IntegerNumber add(IntegerNumber arg0, IntegerNumber arg1);

	/** Subtracts two integer numbers and returns the result. */
	IntegerNumber subtract(IntegerNumber arg0, IntegerNumber arg1);

	/** Multiplies two integer numbers and returns the result. */
	IntegerNumber multiply(IntegerNumber arg0, IntegerNumber arg1);

	/**
	 * Divides two integer numbers and returns the result. Note that this is
	 * integer division. The result is obtained by taking the real quotient and
	 * rounding towards zero. An ArithmeticException is thrown if the
	 * denominator is zero.
	 */
	IntegerNumber divide(IntegerNumber arg0, IntegerNumber arg1);

	/**
	 * Modulo operations. Returns the result of arg0 % arg1.
	 */
	IntegerNumber mod(IntegerNumber arg0, IntegerNumber arg1);

	/** Returns the negation of the given integer number, i.e., -x. */
	IntegerNumber negate(IntegerNumber arg0);

	/** add(arg, 1.0) */
	RationalNumber increment(RationalNumber arg);

	/** add(arg, 1) */
	IntegerNumber increment(IntegerNumber arg);

	/** adds 1 of proper type */
	Number increment(Number arg);

	/** arg-1.0 */
	RationalNumber decrement(RationalNumber arg);

	/** arg - 1 */
	IntegerNumber decrement(IntegerNumber arg);

	/** subtracts 1 of proper type */
	Number decrement(Number arg);

	/**
	 * Returns the greatest common divisor of two integers. The two integers
	 * must be positive.
	 */
	IntegerNumber gcd(IntegerNumber arg0, IntegerNumber arg1);

	/** Returns the least common multiple of the two positive integers. */
	IntegerNumber lcm(IntegerNumber arg0, IntegerNumber arg1);

	/**
	 * Returns the numerator in a representation of the rational number as the
	 * quotient of two integers. This method is coordinated with method
	 * denominator so that the quotient of the numerator and denominator give
	 * the original rational number.
	 */
	IntegerNumber numerator(RationalNumber arg0);

	/**
	 * Returns the denominator in a representation of the rational number as the
	 * quotient of two integers. This method is coordinated with method
	 * numerator so that the quotient of the numerator and denominator give the
	 * original rational number.
	 */
	IntegerNumber denominator(RationalNumber arg0);

	/**
	 * Returns the value of the rational number as an integer number. Applies
	 * only to a rational number which is integral. I.e., the method
	 * isIntegral() must return true.
	 * 
	 * @exception ArithmeticException
	 *                if arg0 is not integral
	 */
	IntegerNumber integerValue(RationalNumber arg0);

	/**
	 * Returns the greatest integer less than or equal to the given rational
	 * number.
	 */
	IntegerNumber floor(RationalNumber arg0);

	/**
	 * Returns the least integer greater than or equal to the given rational
	 * number.
	 */
	IntegerNumber ceil(RationalNumber arg0);

	/**
	 * Returns a positive value if arg0>arg1, 0 if arg0 equals arg1, -1 if
	 * arg0<arg1.
	 */
	int compare(RationalNumber arg0, RationalNumber arg1);

	/**
	 * Returns a positive value if arg0 is greater than arg1, 0 if arg0 equals
	 * arg1, -1 if arg0 is less than arg1.
	 */
	int compare(IntegerNumber arg0, IntegerNumber arg1);

	/* "Mixed" operations... */

	/**
	 * Returns a rational representation of the number. If the number already is
	 * rational, returns the number. Else casts from integer to rational.
	 */
	RationalNumber rational(Number number);

	/**
	 * Returns a positive value if arg0 is greater than arg1, 0 if arg0 equals
	 * arg1, a negative value if arg0 is less than arg1.
	 */
	int compare(Number arg0, Number arg1);

	/**
	 * Performs Gauss-Jordan Elimination on a matrix of rational numbers,
	 * transforming the matrix to reduced row echelon form.
	 * 
	 * @param matrix
	 * @return <code>true</code> iff a non-trivial modification was made to
	 *         {@code matrix}. A non-trivial modification is any modification
	 *         other than a permutation of the rows.
	 */
	boolean gaussianElimination(RationalNumber[][] matrix);

	/**
	 * <p>
	 * Performs a form of "relative Gauss-Jordan elimination". Given two
	 * matrices with the same number of columns. The first matrix is transformed
	 * to reduced row echelon form by the usual Gauss-Jordan elimination (method
	 * {@link #gaussianElimination(RationalNumber[][])}). Then suitable
	 * multiples of the rows in the first matrix are added to rows in the second
	 * matrix so as to make all entries in columns in the second matrix
	 * corresponding to pivot columns in the first matrix 0. Then the usual
	 * Gauss-Jordan elimination is performed on the second matrix.
	 * </p>
	 * 
	 * <p>
	 * Motivation: the idea is that a context specifies some set of constraints
	 * in mat1, and a sub-context specifies additional constraints in mat2. To
	 * simplify the sub-context, you can use any of the information in the
	 * original context.
	 * </p>
	 * 
	 * <p>
	 * Let A1=mat1 in reduced row echelon form. Let A2=mat2 and A2' result of
	 * performing one elementary row operation. Want:
	 * 
	 * <pre>
	 * A2x=b2 iff A2'x=b2' for all x s.t. A1x=b1.
	 * </pre>
	 * 
	 * This holds since A2' is obtained by adding the same number to both sides
	 * of an equation.
	 * </p>
	 * 
	 * @param mat1
	 *            the first matrix, which is the "background" context
	 * @param mat2
	 *            the second matrix, which is the one being reduced under the
	 *            context above
	 * @return <code>true</code> iff a non-trivial modification was made to
	 *         {@code matrix}. A non-trivial modification is any modification
	 *         other than a permutation of the rows.
	 */
	boolean relativeGaussianElimination(RationalNumber[][] mat1,
			RationalNumber[][] mat2);

	/**
	 * Returns the rational number which is the quotient of the two given
	 * integers. It can of course be simplified.
	 * 
	 * @param numerator
	 *            any BigInteger
	 * @param denominator
	 *            any BigInteger
	 * @exception ArithmeticException
	 *                if denominator is zero
	 * @return numerator/denominator
	 */
	RationalNumber rational(BigInteger numerator, BigInteger denominator);

	/**
	 * Returns the IntegerNumber with value specified by the BigInteger. No
	 * precision is lost
	 * 
	 * @param big
	 *            any BigInteger
	 * @return the corresponding IntegerNumber
	 */
	IntegerNumber integer(BigInteger big);

	/**
	 * Returns the IntegerNumber with value specified by the long. No precision
	 * is lost.
	 * 
	 * @param value
	 *            any long
	 * @return the corresponding IntegerNumber
	 */
	IntegerNumber integer(long value);

	// Intervals...

	/**
	 * Returns the empty integer interval: (0,0).
	 * 
	 * @return empty integer interval
	 */
	Interval emptyIntegerInterval();

	/**
	 * Returns the empty real interval: (0.0, 0.0).
	 * 
	 * @return empty real interval
	 */
	Interval emptyRealInterval();

	/**
	 * Returns the universal integer interval: (-&infin;, +&infin;).
	 * 
	 * @return universal integer interval
	 */
	Interval universalIntegerInterval();

	/**
	 * Returns the universal real interval: (-&infin;, +&infin;).
	 * 
	 * @return universal real interval
	 */
	Interval universalRealInterval();

	/**
	 * Returns the interval consisting of the single finite number x: [x,x].
	 * 
	 * @param x
	 *            a non-<code>null</code> {@link Number}
	 * @return closed interval of same type as <code>x</code> with both bounds
	 *         equal to <code>x</code> and non-strict
	 */
	Interval singletonInterval(Number x);

	/**
	 * Restricts the upper bound of an interval as specified. Returns the
	 * interval which is the intersection of the given <code>interval</code> and
	 * the interval (-&infin;,<code>bound</code>) (if <code>strict</code>) or
	 * (-&infin;,<code>bound</code>] (if not <code>strict</code>). This method
	 * could be implemented using {@link #intersection(Interval, Interval)} or
	 * it could be implemented more efficiently in an equivalent way.
	 * 
	 * @param interval
	 *            a non-<code>null</code> {@link Interval}
	 * @param bound
	 *            a {@link Number} of same type as <code>interval</code>
	 * @param strict
	 *            is the <code>bound</code> strict?
	 * @return interval obtained by intersecting the two intervals
	 */
	Interval restrictUpper(Interval interval, Number bound, boolean strict);

	/**
	 * Restricts the lower bound of an interval as specified. Returns the
	 * interval which is the intersection of the given <code>interval</code> and
	 * the interval (<code>bound</code>,&infin;) (if <code>strict</code>) or (
	 * <code>bound</code>,&infin;] (if not <code>strict</code>). This method
	 * could be implemented using {@link #intersection(Interval, Interval)} or
	 * it could be implemented more efficiently in an equivalent way.
	 * 
	 * @param interval
	 *            a non-<code>null</code> {@link Interval}
	 * @param bound
	 *            a {@link Number} of same type as <code>interval</code>
	 * @param strict
	 *            is the <code>bound</code> strict?
	 * @return interval obtained by intersecting the two intervals
	 */
	Interval restrictLower(Interval interval, Number bound, boolean strict);

	/**
	 * Returns a new {@link Interval} as specified. If the bound of
	 * <code>this</code> {@link Interval} is an implementation of
	 * {@link RealInfinity}, the bound represents an infinity. <br>
	 *
	 * Precondition:
	 * <ul>
	 * <li>if the type is integral, then the upper and lower bounds must be
	 * instances of {@link IntegerNumber}, else they must be instances of
	 * {@link RationalNumber}</li>
	 * </ul>
	 * 
	 * Postconditions: the parameters must specify an interval in "normal form",
	 * i.e., the following must all hold:
	 * 
	 * <ul>
	 * <li>if the type is integral, then the upper and lower bounds must be
	 * instances of {@link IntegerNumber}, else they must be instances of
	 * {@link RationalNumber}</li>
	 * 
	 * <li>if the bound is exclusive, the corresponding <code>strictLower</code>
	 * (or <code>strictUpper</code>) must be <code>true</code>.</li>
	 * 
	 * <li>the lower bound must be less than or equal to the upper bound
	 * (Otherwise, this method will return an empty interval.)</li>
	 * 
	 * <li>if the bounds are <strong>finite</strong> and equal: either (1) both
	 * <code>strictLower</code> and <code>strictUpper</code> will be
	 * <code>false</code>, or (2) <code>strictLower</code> and
	 * <code>strictUpper</code> will be <code>true</code> and the upper and
	 * lower bounds will be 0. The first case represents an interval consisting
	 * of a single point; the second case represents the empty interval.</li>
	 * 
	 * <li>if the <strong>lower bound</strong> <code>lower</code> is infinite,
	 * it must be a <strong>negative</strong> infinity. And if the <strong>upper
	 * bound</strong> <code>upper</code> is infinite, it must be a
	 * <strong>positive</strong> infinity.</li>
	 * 
	 * <li>if <code>isIntegral</code> is <strong><code>true</code></strong>: if
	 * the lower bound is finite then <code>strictLower</code> must be
	 * <code>false</code>; if the upper bound is finite then
	 * <code>strictUpper</code> must be <code>false</code>.</li>
	 * </ul>
	 * 
	 * @param isIntegral
	 *            does the interval have integer type (as opposed to real type)?
	 * @param lower
	 *            the lower bound of the interval with the type defined by
	 *            isIntegral
	 * @param strictLower
	 *            is the lower bound strict? (i.e., "(" if exclusive, as opposed
	 *            to "[" if inclusive)
	 * @param upper
	 *            the upper bound of the interval with the type defined by
	 *            isIntegral
	 * @param strictUpper
	 *            is the upper bound strict? (i.e., ")" if exclusive, as opposed
	 *            to "]" if inclusive)
	 * @return a new interval instance as specified
	 */
	Interval newInterval(boolean isIntegral, Number lower, boolean strictLower,
			Number upper, boolean strictUpper);

	/**
	 * Returns the interval which is the intersection of the two given
	 * intervals. The two given intervals must have the same type.
	 * 
	 * @param i1
	 *            a non-<code>null</code> {@link Interval}
	 * @param i2
	 *            an non-<code>null</code> {@link Interval} with same type
	 *            (integral or rational) as "i1"
	 * @return an {@link Interval} representing the intersection of "i1" and
	 *         "i2"
	 */
	Interval intersection(Interval i1, Interval i2);

	/**
	 * Computes the union of two intervals or reports that the union is not an
	 * interval and why. The result is stored in the <code>result</code> object.
	 * If the union of the two intervals is an interval,
	 * <code>result.status</code> will be set to 0 and <code>result.union</code>
	 * will hold the union interval. Otherwise, the status will be set to either
	 * a negative or positive integer and <code>result.union</code> will be set
	 * to <code>null</code>. A positive status indicates that every element of
	 * i1 is greater than every element of i2; a negative status indicates every
	 * element of i1 is less than every element of i2.
	 *
	 * @param i1
	 *            an non-<code>null</code> {@link Interval}
	 * @param i2
	 *            an non-<code>null</code> {@link Interval} of same type
	 *            (integral or rational) as "i1"
	 * @param result
	 *            an non-<code>null</code> {@link IntervalUnion} used for
	 *            receiving the result union
	 * @return an {@link IntervalUnion} of "i1" and "i2" or <code>null</code>
	 */
	void union(Interval i1, Interval i2, IntervalUnion result);

	/**
	 * Returns the smallest interval containing both of the given intervals.
	 * 
	 * @param i1
	 *            a non-<code>null</code> {@link Interval}
	 * @param i2
	 *            a non-<code>null</code> {@link Interval} of same type as
	 *            <code>i1</code>
	 * @return the smallest {@link Interval} containing <code>i1</code> and
	 *         <code>i2</code>
	 */
	Interval join(Interval i1, Interval i2);

	/**
	 * Computes the affineTransform of the input interval <code>itv</code> with
	 * two numbers: <code>a</code> and <code>b</code> as parameters.
	 * 
	 * @param interval
	 *            an non-<code>null</code> {@link Interval}
	 * @param a
	 *            a non-<code>null</code> finite {@link Number} used to multiply
	 *            with both <code>upper</code> and <code>lower</code> of
	 *            "interval"
	 * @param b
	 *            a non-<code>null</code> finite {@link Number} used to add to
	 *            both <code>upper</code> and <code>lower</code> of "interval",
	 *            after multiplying "a".
	 * @return an {@link Interval} which is an affineTransform of "interval"
	 */
	Interval affineTransform(Interval interval, Number a, Number b);

	/**
	 * Computes the relationships of two {@link Interval}s. <br>
	 * <li>Return -3, if the first {@link Interval} is on the
	 * <strong>left</strong> side and <strong>disjointed</strong> with the
	 * second {@link Interval}</li>
	 * <li>Return -2, if the first {@link Interval} is on the
	 * <strong>left</strong> side and <strong>intersected</strong> with the
	 * second {@link Interval}</li>
	 * <li>Return -1, if the first {@link Interval} <strong>contains</strong>
	 * the second {@link Interval}</li>
	 * <li>Return 0, if the first {@link Interval} is exactly
	 * <strong>same</strong> with the second {@link Interval}</li>
	 * <li>Return 1, if the first {@link Interval} <strong>is contained
	 * in</strong> the second {@link Interval}</li>
	 * <li>Return 2, if the first {@link Interval} is on the
	 * <strong>right</strong> side and <strong>intersected</strong> with the
	 * second {@link Interval}</li>
	 * <li>Return 3, if the first {@link Interval} is on the
	 * <strong>right</strong> side and <strong>disjointed</strong> with the
	 * second {@link Interval}</li> </br>
	 * 
	 * @param i1
	 *            a non-<code>null</code> {@link Interval}
	 * @param i2
	 *            a non-<code>null</code> {@link Interval} of same type as
	 *            <code>i1</code>
	 * @return an integer representing the relationship of two given
	 *         {@link Interval}, the value is defined above.
	 */
	int compare(Interval i1, Interval i2);

	/**
	 * To negate the given non-<code>null</code> interval. <br>
	 * 
	 * @param interval
	 *            a non-<code>null</code> {@link Interval}
	 * @return the negated {@link Interval}
	 */
	Interval negate(Interval interval);

	/**
	 * To calculate the sum of two non-<code>null</code> and not empty
	 * {@link Interval} with same type (real/integer)
	 * 
	 * @param i1
	 *            a non-<code>null</code> {@link Interval} with same type of the
	 *            other one
	 * @param i2
	 *            a non-<code>null</code> {@link Interval} with same type of the
	 *            other one
	 * @return the sum of two {@link Interval}.
	 */
	Interval add(Interval i1, Interval i2);

	/**
	 * To calculate the product of two non-<code>null</code> and not empty
	 * {@link Interval} with same type (real/integer) <br>
	 * This is the smallest interval containing all x1*x2, where x1 is in i1 and
	 * x2 is in i2.
	 * 
	 * @param i1
	 *            a non-<code>null</code> {@link Interval} with same type of the
	 *            other one
	 * @param i2
	 *            a non-<code>null</code> {@link Interval} with same type of the
	 *            other one
	 * @return the product of two {@link Interval}.
	 */
	Interval multiply(Interval i1, Interval i2);

	/**
	 * To calculate the power-result of a given non-<code>null</code> and not
	 * empty {@link Interval} with a given natural number.
	 * 
	 * @param interval
	 *            a non-<code>null</code> {@link Interval}
	 * @param exp
	 *            an integer of natural number
	 * @return the power result of the {@link Interval} with the given integer
	 */
	Interval power(Interval interval, int exp);

	/**
	 * To calculate the power-result of a given non-<code>null</code> and not
	 * empty {@link Interval} with a given non-<code>null</code> non-negative
	 * {@link IntegerNumber}.
	 * 
	 * @param interval
	 *            a non-<code>null</code> {@link Interval}
	 * @param expr
	 *            an {@link IntegerNumber} of a natural number
	 * @return the power result described above.
	 */
	Interval power(Interval interval, IntegerNumber expr);

	/**
	 * Calculate the given number powering a given exponent and returns result.
	 * The exponent must be a natural number.
	 * 
	 * @param number
	 *            a non-<code>null</code> {@link IntegerNumber}
	 * @param exp
	 *            a non-<code>null</code> {@link IntegerNumber} representing an
	 *            natural number which is greater or equal to zero.
	 * @return the power result of the {@link Number} with the given integer
	 */
	Number power(Number number, int exp);

	/**
	 * Calculate the given integer number powering a given exponent and returns
	 * result. The exponent must be a natural number.
	 * 
	 * @param number
	 *            a non-<code>null</code> {@link IntegerNumber}
	 * @param exp
	 *            a non-<code>null</code> {@link IntegerNumber} representing an
	 *            natural number which is greater or equal to zero.
	 * @return the power result of the {@link IntegerNumber} with the given
	 *         integer
	 */
	IntegerNumber power(IntegerNumber number, int exp);

	/**
	 * Calculate the given rational number powering a given exponent and returns
	 * result. The exponent must be a natural number.
	 * 
	 * @return the power result of the {@link RationalNumber} with the given
	 *         integer
	 */
	RationalNumber power(RationalNumber number, int exp);

	/**
	 * Calculate the given {@link Number} base powering a given
	 * {@link IntegerNumber} exponent and returns result. The exponent must be a
	 * natural number.
	 * 
	 * @param number
	 *            a non-<code>null</code> {@link Number}
	 * @param exp
	 *            a non-<code>null</code> {@link IntegerNumber} representing an
	 *            natural number which is greater or equal to zero.
	 * @return
	 */
	Number power(Number number, IntegerNumber exp);

	/**
	 * Calculate the given {@link RationalNumber} base powering a given
	 * {@link IntegerNumber} exponent and returns result. The exponent must be a
	 * natural number.
	 * 
	 * @param number
	 *            a non-<code>null</code> {@link RationalNumber}
	 * @param exp
	 *            a non-<code>null</code> {@link IntegerNumber} representing an
	 *            natural number which is greater or equal to zero.
	 * @return
	 */
	RationalNumber power(RationalNumber number, IntegerNumber exp);

	/**
	 * Calculate the given {@link IntegerNumber} base powering a given
	 * {@link IntegerNumber} exponent and returns result. The exponent must be a
	 * natural number.
	 * 
	 * @param number
	 *            a non-<code>null</code> {@link IntegerNumber}
	 * @param exp
	 *            a non-<code>null</code> {@link IntegerNumber} representing an
	 *            natural number which is greater or equal to zero.
	 * @return
	 */
	IntegerNumber power(IntegerNumber number, IntegerNumber exp);

	/**
	 * Calculate the nth root of the given number and n. The inputs and output
	 * are {@link IntegerNumbers}
	 * 
	 * @param number
	 *            a non-<code>null</code> finite {@link IntegerNumbers};
	 * @param n
	 *            a non-<code>null</code> positive finite {@link IntegerNumbers}
	 *            representing the n;
	 * @return
	 */
	IntegerNumber nthRootInt(IntegerNumber number, IntegerNumber n);

	/**
	 * Return the {@link Interval} consisting of all {@link Number}s that each
	 * {@link Number} <code>x</code> in the {@link Interval} multiplies with the
	 * given <code>num</code> is in the given <code>interval</code>. <br>
	 * (i.e., for all <code>x</code> in the returned result, <code>x*num</code>
	 * is in <code>interval</code>.)<br>
	 * E.g., for integer intervals: [1,3]/2 = [1,1]; [1,4]/3 = [1,1]; [1,5]/6 =
	 * empty, for rational intervals: [1,3]/2 = [0.5,1.5].
	 * 
	 * @param interval
	 *            A non-<code>null</code> {@link Interval}
	 * @param num
	 *            A non-<code>null</code> {@link Number} (which means it is a
	 *            finite number) with the same type of the given
	 *            {@link Interval} <code>interval</code>.
	 * @return the result
	 */
	Interval divide(Interval interval, Number num);

	/**
	 * Divides two given {@link Interval}s and returns result. They must be of
	 * same type (integer or real), which is also the type of the result. <br>
	 * This is the smallest interval containing all x1/x2, where x1 is in i1 and
	 * x2 is in i2.
	 * 
	 * @param i1
	 *            A non-<code>null</code> {@link Interval}
	 * @param i2
	 *            A non-<code>null</code> {@link Interval} with the same type of
	 *            the given {@link Interval} <code>interval</code>.
	 * @return the result
	 */
	Interval divide(Interval i1, Interval i2);

	/**
	 * Multiply the given {@link Interval} with a given {@link Number} and
	 * returns result. The numbers must be of same type (integer or real), which
	 * is also the type of the result.
	 * 
	 * @param num
	 *            A non-<code>null</code> finite {@link Number}
	 * @param interval
	 *            A non-<code>null</code> {@link Interval} with the same type of
	 *            the given {@link Number} <code>num</code>.
	 * @return the result
	 */
	Interval multiply(Number num, Interval interval);

	/**
	 * A simple type for recording the result of attempting to take the union of
	 * two intervals i1 and i2. There are three possibilities:
	 * <ol>
	 * <li>the union of the two intervals is an interval. In this case,
	 * <code>status=0</code> and <code>union</code> is the union of the two
	 * intervals.</li>
	 * <li>i1 is strictly less than i2 and the union is not an interval. In this
	 * case, <code>status&lt;0</code> and <code>union</code> is
	 * <code>null</code>.</li>
	 * <li>i1 is strictly greater than i2 and the union is not an interval. In
	 * this case, <code>status&gt;0</code> and <code>union</code> is
	 * <code>null</code>.</li>
	 * </ol>
	 * 
	 * @author xxxx
	 *
	 */
	public class IntervalUnion {
		public int status;
		public Interval union;
	}

	/**
	 * Constructs a string representation of the given rational number in
	 * scientific notation: x.xxxx * 10^{-yyy}.
	 * 
	 * @param num
	 *            the rational number
	 * @param numSig
	 *            the number of significant digits
	 * @return string representation in scientific notation
	 */
	String scientificString(RationalNumber num, int numSig);
}
