package edu.udel.cis.vsl.sarl.simplify.IF;

import edu.udel.cis.vsl.sarl.IF.number.IntegerNumber;
import edu.udel.cis.vsl.sarl.IF.number.Interval;
import edu.udel.cis.vsl.sarl.IF.number.Number;

/**
 * Factory for producing instances of {@link Range}.
 * 
 * @author Stephen F. Siegel
 *
 */
public interface RangeFactory {

	/**
	 * Returns an empty {@link Range} of the specified type (integer/real) with
	 * no interval contained.
	 * 
	 * @param isIntegral
	 *            is this the integer type (not real type)?
	 * @return an empty {@link Range} as described.
	 * @author Wenhao Wu (xxxx@udel.edu)
	 */
	Range emptySet(boolean isIntegral);

	/**
	 * Returns a {@link Range} consisting of exactly one interval representing
	 * the single specified {@link Number} <code>number</code>.<br>
	 * The type of <code>number</code> determines the type of returned
	 * {@link Range}.<br>
	 * 
	 * <strong>Preconditions:</strong><br>
	 * > The <code>number</code> must be non-<code>null</code> and infinite.
	 * 
	 * @param number
	 *            a non-<code>null</code> finite {@link Number}
	 * @return a singleton {@link Range} as described.
	 * @author Wenhao Wu (xxxx@udel.edu)
	 */
	Range singletonSet(Number number);

	/**
	 * Returns a {@link Range} consisting of exactly one interval representing
	 * all x between the {@link Number} <code>lower</code> (exclusively iff
	 * <code>strictLower</code> is <code>true</code>, else inclusively) and the
	 * {@link Number} <code>upper</code> (exclusively iff
	 * <code>strictUpper</code> is <code>true</code>, else inclusively).<br>
	 *
	 * <strong>Preconditions:</strong><br>
	 * > All arguments should satisfy the preconditions of {@link Interval}.<br>
	 * 
	 * <strong>Postconditions:</strong><br>
	 * > The generated interval instance should satisfy the postconditions of
	 * {@link Interval}.
	 * 
	 * @param isIntegral
	 *            does the interval have integer type (as opposed to real type)?
	 * @param lower
	 *            the lower bound of the returned {@link Range} with the type
	 *            defined by <code>isIntegral</code>
	 * @param strictLower
	 *            is the lower bound strict? (i.e., "(" if exclusive, as opposed
	 *            to "[" if inclusive)
	 * @param upper
	 *            the upper bound of the returned {@link Range} with the type
	 *            defined by <code>isIntegral</code>
	 * @param strictUpper
	 *            is the upper bound strict? (i.e., ")" if exclusive, as opposed
	 *            to "]" if inclusive)
	 * @return an interval {@link Range} as described.
	 * @author Wenhao Wu (xxxx@udel.edu)
	 */
	Range interval(boolean isIntegral, Number lower, boolean strictLower,
			Number upper, boolean strictUpper);

	/**
	 * Return the complement {@link Range} of the input <code>range</code>. In
	 * other words, the returned {@link Range} contains all x, where x is not in
	 * the input <code>range</code>. The type of returned {@link Range} is same
	 * with the input <code>range</code>. <br>
	 * 
	 * <strong>Preconditions:</strong><br>
	 * > The input <code>range</code> must be non-<code>null</code>.<br>
	 * 
	 * @param range
	 *            the input {@link Range}
	 * @return a {@link Range} as described.
	 * @author Wenhao Wu (xxxx@udel.edu)
	 */
	Range complement(Range range);

	/**
	 * Return the union {@link Range} of <code>range0</code> and
	 * <code>range1</code>. In other words, the returned {@link Range} contains
	 * all x, where x is contained in either <code>range0</code> or
	 * <code>range1</code>.The type of returned {@link Range} is same with two
	 * input ranges.
	 * 
	 * <strong>Preconditions:</strong><br>
	 * > The <code>range0</code> and <code>range1</code> must be non-
	 * <code>null</code>.<br>
	 * > The <code>range0</code> and <code>range1</code> must have a same type
	 * (integer/real)<br>
	 * 
	 * @param range0
	 *            a non-<code>null</code> {@link Range}
	 * @param range1
	 *            a non-<code>null</code> {@link Range} of the same type
	 *            (integer/real) as <code>range0</code>.
	 * @return a {@link Range} as described.
	 * @author Wenhao Wu (xxxx@udel.edu)
	 */
	Range union(Range range0, Range range1);

	/**
	 * Return the intersection {@link Range} of <code>range0</code> and
	 * <code>range1</code>. In other words, the returned {@link Range} contains
	 * all x, where x is contained in both input ranges.The type of returned
	 * {@link Range} is same with two input ranges.
	 * 
	 * <strong>Preconditions:</strong><br>
	 * > The <code>range0</code> and <code>range1</code> must be non-
	 * <code>null</code>.<br>
	 * > The <code>range0</code> and <code>range1</code> must have a same type
	 * (integer/real)<br>
	 * 
	 * @param range0
	 *            a non-<code>null</code> {@link Range}
	 * @param range1
	 *            a non-<code>null</code> {@link Range} of the same type
	 *            (integer/real) as <code>range0</code>.
	 * @return a {@link Range} as described.
	 * @author Wenhao Wu (xxxx@udel.edu)
	 */
	Range intersect(Range range0, Range range1);

	/**
	 * Return a {@link Range} containing all x+y, where x is in
	 * <code>range0</code> and y is in <code>range1</code>. The type of returned
	 * {@link Range} is same with two input ranges.
	 * 
	 * <strong>Preconditions:</strong><br>
	 * > The <code>range0</code> and <code>range1</code> must be non-
	 * <code>null</code>.<br>
	 * > The <code>range0</code> and <code>range1</code> must have a same type
	 * (integer/real)<br>
	 * 
	 * @param range0
	 *            a non-<code>null</code> {@link Range}
	 * @param range1
	 *            a non-<code>null</code> {@link Range} of the same type
	 *            (integer/real) as <code>range0</code>.
	 * @return a {@link Range} as described.
	 * @author Wenhao Wu (xxxx@udel.edu)
	 */
	Range add(Range range0, Range range1);

	/**
	 * Return the difference {@link Range} of <code>range0</code> and
	 * <code>range1</code>. In other words, the returned {@link Range} contains
	 * all x, where x is contained in <code>range0</code> but not in
	 * <code>range1</code>.The type of returned {@link Range} is same with two
	 * input ranges.
	 * 
	 * <strong>Preconditions:</strong><br>
	 * > The <code>range0</code> and <code>range1</code> must be non-
	 * <code>null</code>.<br>
	 * > The <code>range0</code> and <code>range1</code> must have a same type
	 * (integer/real)<br>
	 * 
	 * @param range0
	 *            a non-<code>null</code> {@link Range}
	 * @param range1
	 *            a non-<code>null</code> {@link Range} of the same type
	 *            (integer/real) as <code>range0</code>.
	 * @return a {@link Range} as described.
	 * @author Wenhao Wu (xxxx@udel.edu)
	 */
	Range setMinus(Range range0, Range range1);

	/**
	 * Return a {@link Range} containing all x*y, where x is in
	 * <code>range0</code> and y is in <code>range1</code>. The type of returned
	 * {@link Range} is same with two input ranges.
	 * 
	 * <strong>Preconditions:</strong><br>
	 * > The <code>range0</code> and <code>range1</code> must be non-
	 * <code>null</code>.<br>
	 * > The <code>range0</code> and <code>range1</code> must have a same type
	 * (integer/real)<br>
	 * 
	 * @param range0
	 *            a non-<code>null</code> {@link Range}
	 * @param range1
	 *            a non-<code>null</code> {@link Range} of the same type
	 *            (integer/real) as <code>range0</code>.
	 * @return a {@link Range} as described.
	 * @author Wenhao Wu (xxxx@udel.edu)
	 */
	Range multiply(Range range0, Range range1);

	/**
	 * Return a {@link Range} containing all x-y, where x is in
	 * <code>range0</code> and y is in <code>range1</code>. The type of returned
	 * {@link Range} is same with two input ranges.
	 * 
	 * <strong>Preconditions:</strong><br>
	 * > The <code>range0</code> and <code>range1</code> must be non-
	 * <code>null</code>.<br>
	 * > The <code>range0</code> and <code>range1</code> must have a same type
	 * (integer/real)<br>
	 * 
	 * @param range0
	 *            a non-<code>null</code> {@link Range}
	 * @param range1
	 *            a non-<code>null</code> {@link Range} of the same type
	 *            (integer/real) as <code>range0</code>.
	 * @return a {@link Range} as described.
	 * @author Wenhao Wu (xxxx@udel.edu)
	 */
	Range subtract(Range range0, Range range1);

	/**
	 * Return a {@link Range} containing all x^<code>exp</code>, where x is in
	 * <code>range</code> and <code>exp</code> is a natural numbers. The type of
	 * returned {@link Range} is same with the input <code>range</code>.
	 * 
	 * <strong>Preconditions:</strong><br>
	 * > The <code>range</code> must be non- <code>null</code>.<br>
	 * > The <code>exp</code> must be a finite non-negative integer.<br>
	 * 
	 * @param range
	 *            a non-<code>null</code> {@link Range}
	 * @param exp
	 *            a natural number as the exponent
	 * @return a {@link Range} as described.
	 * @author Wenhao Wu (xxxx@udel.edu)
	 */
	Range power(Range range, int exp);

	/**
	 * Return a {@link Range} containing all x^<code>exp</code>, where x is in
	 * <code>range</code> and <code>exp</code> is an {@link IntegerNumber}. The
	 * type of returned {@link Range} is same with the input <code>range</code>.
	 * 
	 * <strong>Preconditions:</strong><br>
	 * > The <code>range</code> and <code>exps</code> must be non-
	 * <code>null</code>.<br>
	 * > The <code>exp</code> must be a finite non-negative
	 * {@link IntegerNumber}.<br>
	 * 
	 * @param range
	 *            a non-<code>null</code> {@link Range}
	 * @param exp
	 *            an {@link IntegerNumber} as the exponent
	 * @return a {@link Range} as described.
	 * @author Wenhao Wu (xxxx@udel.edu)
	 */
	Range power(Range range, IntegerNumber exp);

	/**
	 * Return a {@link Range} containing all x*<code>a</code>+<code>b</code>,
	 * where x is in <code>range</code>, <code>a</code> and <code>b</code> are
	 * {@link Number}s. The type of returned {@link Range} is same with the
	 * input <code>range</code>.
	 * 
	 * <strong>Preconditions:</strong><br>
	 * > The <code>range</code>, <code>a</code> and <code>b</code> must be non-
	 * <code>null</code>.<br>
	 * > The <code>range</code>, <code>a</code> and <code>b</code> must have a
	 * same type(integer/real).<br>
	 * > The <code>a</code> and <code>b</code> must be finite.<br>
	 * 
	 * @param range
	 *            a non-<code>null</code> {@link Range}
	 * @param a
	 *            a non-<code>null</code> finite {@link Number} of the same type
	 *            with <code>range</code>.
	 * @param b
	 *            a non-<code>null</code> finite {@link Number} of the same type
	 *            with <code>range</code>.
	 * @return a {@link Range} as described.
	 * @author Wenhao Wu (xxxx@udel.edu)
	 */
	Range affineTransform(Range range, Number a, Number b);

	/**
	 * Return a {@link Range} containing all x/<code>constant</code>, where x is
	 * in <code>range</code> and <code>constant</code> is a non-
	 * <code>null</code> and non-zero finite {@link Number}. The type of
	 * returned {@link Range} is same with the input <code>range</code>.
	 * 
	 * <strong>Preconditions:</strong><br>
	 * > The <code>range</code> and <code>constant</code> must be non-
	 * <code>null</code>.<br>
	 * > The <code>range</code> and <code>constant</code> must have a same type
	 * (integer/real)<br>
	 * > The <code>constant</code> must be non-<code>null</code>, non-zero and
	 * finite.<br>
	 * 
	 * @param range
	 *            a non-<code>null</code> {@link Range}
	 * @param constant
	 *            a non-<code>null</code>, non-zero and finite {@link Number} of
	 *            the same type with <code>range</code>.
	 * @return a {@link Range} as described.
	 * @author Wenhao Wu (xxxx@udel.edu)
	 */
	Range divide(Range range, Number constant);

	/**
	 * Return a {@link Range} containing all x/y, where x is in
	 * <code>range0</code> and y is is in <code>range1</code>. The type of
	 * returned {@link Range} is same with the both <code>range</code>.
	 * 
	 * <strong>Preconditions:</strong><br>
	 * > The <code>range0</code> and <code>range1</code> must be non-
	 * <code>null</code>.<br>
	 * > The <code>range0</code> and <code>range1</code> must have a same type
	 * (integer/real)<br>
	 * 
	 * @param range0
	 *            a non-<code>null</code> {@link Range}
	 * @param range1
	 *            a non-<code>null</code> {@link Range}
	 * @return a {@link Range} as described.
	 * @author Wenhao Wu (xxxx@udel.edu)
	 */
	Range divide(Range range0, Range range1);

	/**
	 * Return a {@link Range} containing all x*<code>constant</code>, where x is
	 * in <code>range</code> and <code>constant</code> is a non-
	 * <code>null</code> finite {@link Number}. The type of returned
	 * {@link Range} is same with the input <code>range</code>.
	 * 
	 * <strong>Preconditions:</strong><br>
	 * > The <code>range</code> and <code>constant</code> must be non-
	 * <code>null</code>.<br>
	 * > The <code>range</code> and <code>constant</code> must have a same type
	 * (integer/real)<br>
	 * > The <code>constant</code> must be non-<code>null</code> and finite.<br>
	 * 
	 * @param range
	 *            a non-<code>null</code> {@link Range}
	 * @param constant
	 *            a non-<code>null</code> and finite {@link Number} of the same
	 *            type with <code>range</code>.
	 * @return a {@link Range} as described.
	 * @author Wenhao Wu (xxxx@udel.edu)
	 */
	Range multiply(Range range, Number constant);

	/**
	 * Return a {@link Range} containing all x+<code>constant</code>, where x is
	 * in <code>range</code> and <code>constant</code> is a non-
	 * <code>null</code> finite {@link Number}. The type of returned
	 * {@link Range} is same with the input <code>range</code>.
	 * 
	 * <strong>Preconditions:</strong><br>
	 * > The <code>range</code> and <code>constant</code> must be non-
	 * <code>null</code>.<br>
	 * > The <code>range</code> and <code>constant</code> must have a same type
	 * (integer/real)<br>
	 * > The <code>constant</code> must be non-<code>null</code> and finite.<br>
	 * 
	 * @param range
	 *            a non-<code>null</code> {@link Range}
	 * @param constant
	 *            a non-<code>null</code> and finite {@link Number} of the same
	 *            type with <code>range</code>.
	 * @return a {@link Range} as described.
	 * @author Wenhao Wu (xxxx@udel.edu)
	 */
	Range add(Range range, Number constant);

	/**
	 * Return a {@link Range} containing all x-<code>constant</code>, where x is
	 * in <code>range</code> and <code>constant</code> is a non-
	 * <code>null</code> finite {@link Number}. The type of returned
	 * {@link Range} is same with the input <code>range</code>.
	 * 
	 * <strong>Preconditions:</strong><br>
	 * > The <code>range</code> and <code>constant</code> must be non-
	 * <code>null</code>.<br>
	 * > The <code>range</code> and <code>constant</code> must have a same type
	 * (integer/real)<br>
	 * > The <code>constant</code> must be non-<code>null</code> and finite.<br>
	 * 
	 * @param range
	 *            a non-<code>null</code> {@link Range}
	 * @param constant
	 *            a non-<code>null</code> and finite {@link Number} of the same
	 *            type with <code>range</code>.
	 * @return a {@link Range} as described.
	 * @author Wenhao Wu (xxxx@udel.edu)
	 */
	Range subtract(Range range, Number constant);

	/**
	 * Create a new {@link Range} based on given non-<code>null</code>
	 * {@link Interval}s.<br>
	 * If the given intervals represents an empty interval array, this function
	 * will return an empty {@link Range} with 0 interval. (i.e.,
	 * <strong>zero</strong> would be the size of the interval array stored in
	 * <code>this</code> {@link Range})
	 * 
	 * <strong>Preconditions:</strong><br>
	 * > All given <code>intervals</code> must be non-<code>null</code>.<br>
	 * > All given <code>intervals</code> must have a same type.<br>
	 * 
	 * <strong>Postconditions:</strong><br>
	 * > All intervals stored in the returned result will be simplified.<br>
	 * 
	 * @param intervals
	 *            a set of {@link Interval}s of a same type (integer/real).
	 * @return a non-<code>null</code> {@link Range} representing the range
	 *         represented by given <code>intervals</code>.
	 * @author Wenhao Wu (xxxx@udel.edu)
	 */
	Range newRange(Interval... intervals);

	/**
	 * Returns a range of either integer or real type. That is, the set
	 * consisting of all real numbers, or the set consisting of all integers.
	 * 
	 * @param isIntegral
	 *            is this the integer type (not real type)?
	 * 
	 */
	Range universalSet(boolean isIntegral);

	/**
	 * Returns a {@link Range} R such that the intersection of R and C=
	 * {@code contextRange} is I={@code range} and R is "as simple as possible".
	 * 
	 * Precondition: I is contained in C. I and C have the same type.
	 * 
	 * Postcondition: R intersect C = I.
	 * 
	 * @param range
	 *            a range to be expanded
	 * @param contextRange
	 *            the background context assumed to be true
	 * @return a range R satisfying R intersect C = I
	 */
	Range expand(Range range, Range contextRange);
}
