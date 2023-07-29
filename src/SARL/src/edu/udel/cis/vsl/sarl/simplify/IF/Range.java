package edu.udel.cis.vsl.sarl.simplify.IF;

import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.number.Interval;
import edu.udel.cis.vsl.sarl.IF.number.Number;
import edu.udel.cis.vsl.sarl.preuniverse.IF.PreUniverse;

/**
 * An abstract representation of a set of {@link Number}s. Each instance is
 * typed: it is either a set of integers, or a set of reals. The two types are
 * incompatible.
 * 
 * @author Stephen F. Siegel
 */
public interface Range {
	/**
	 * A categorization of ranges based on their relationship to 0. Every Range
	 * falls into exactly one of these categories.
	 */
	static enum RangeSign {
		/**
		 * Every element of the range is less than 0 and the range is not empty.
		 */
		LT0,
		/**
		 * Every element of the range is less than or equal to 0 and the range
		 * contains 0 and a negative number.
		 */
		LE0,
		/** The range consists exactly of 0 and nothing else. */
		EQ0,
		/**
		 * The range contains 0 and a positive number and every element of the
		 * range is greater than or equal to 0.
		 */
		GE0,
		/**
		 * Every element of the range is greater than 0 and the range is
		 * non-empty.
		 */
		GT0,
		/** The range is empty */
		EMPTY,
		/**
		 * The range contains at least a negative number and a positive number
		 * (0 is optionally contained)
		 */
		ALL
	};

	/**
	 * Is this an integer set?
	 * 
	 * @return <code>true</code> if this is a set of integers;
	 *         <code>false</code> if this is a set of reals
	 */
	boolean isIntegral();

	/**
	 * Is this an empty set?
	 * 
	 * @return <code>true</code> iff this is an empty set
	 */
	boolean isEmpty();

	/**
	 * Is this a universal set?
	 * 
	 * @return <code>true</code> iff this is a universal set
	 */
	boolean isUniversal();

	/**
	 * Does this set contain the given number as a member?
	 * 
	 * @param number
	 *            any non-<code>null</code> {@link Number} of the same type
	 *            (integer/real) as this set
	 * @return <code>true</code> iff this set contains the given number
	 */
	boolean containsNumber(Number number);

	/**
	 * Is this set a superset of the given one?
	 * 
	 * @param set
	 *            a number set of the same type (integer/real) as this one
	 * @return <code>true</code> iff this one contains the given one
	 */
	boolean contains(Range set);

	/**
	 * Is the intersection of this set with the given one nonempty?
	 * 
	 * @param set
	 *            a number set of the same type (integer/real) as this one
	 * @return <code>true</code> iff the intersection of the two sets is
	 *         nonempty
	 */
	boolean intersects(Range set);

	/**
	 * Given a {@link NumericExpression} <code>x</code>, returns a
	 * {@link BooleanExpression} which holds iff <code>x</code> is in
	 * <code>this</code> {@link Range}.
	 * 
	 * <p>
	 * Example: suppose <code>this</code> {@link Range} is the {@link Interval}
	 * (0,1]. Given <code>x</code>, this method will return a
	 * {@link BooleanExpression} <code>x>0 && x<=1</code>.
	 * </p>
	 * 
	 * <p>
	 * Example: (0,1] U [4,5]. Given <code>x</code>, returns
	 * <code>(x>0 && x<=1) || (x>=4 && x<=5)</code>.
	 * </p>
	 * 
	 * @param x
	 *            variable to use in the new expression
	 * @param universe
	 *            symbolic universe used to construct the symbolic expression
	 * @return a {@link BooleanExpression} involving <code>x</code> which holds
	 *         iff <code>x</code> is in this set
	 */
	BooleanExpression symbolicRepresentation(NumericExpression x,
			PreUniverse universe);

	/**
	 * If this range represents a singleton set (a set consisting of exactly one
	 * Number), this method returns the value of its sole element; otherwise,
	 * returns <code>null</code>.
	 * 
	 * @return The exact {@link Number} represented by <code>this
	 *         </code> {@link Range}, if <code>this</code> range represents a
	 *         singleton number;<br>
	 *         otherwise, it will return <code>null</code>.
	 */
	Number getSingletonValue();

	/**
	 * If this range is an interval, return the Interval representation,
	 * otherwise, returns <code>null</code>.
	 * 
	 * @return The exact {@link Interval} represented by <code>this
	 *         </code> {@link Range}, if <code>this</code> range represents a
	 *         singleton interval;<br>
	 *         otherwise, it will return <code>null</code>.
	 */
	Interval asInterval();

	/**
	 * Get the {@link RangeSign} of <code>this</code> range.
	 * 
	 * @return
	 */
	RangeSign sign();

	/**
	 * Get the over-approximated {@link Interval} of <code>this</code> range.
	 * (E.g., for an integral range consisting of [-1, 0), [3, 6) and [6, 10);
	 * the actual range should be [-1, -1] U [3, 9]; and its over-approximation
	 * range should be [-1, 9].)
	 * 
	 * @return the over-approximation {@link Interval} of <code>this</code>
	 */
	Interval intervalOverApproximation();
}
