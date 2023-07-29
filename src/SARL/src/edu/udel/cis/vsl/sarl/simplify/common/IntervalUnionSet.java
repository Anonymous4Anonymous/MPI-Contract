package edu.udel.cis.vsl.sarl.simplify.common;

import java.util.ArrayList;

import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.number.IntegerNumber;
import edu.udel.cis.vsl.sarl.IF.number.Interval;
import edu.udel.cis.vsl.sarl.IF.number.Number;
import edu.udel.cis.vsl.sarl.IF.number.NumberFactory;
import edu.udel.cis.vsl.sarl.number.IF.Numbers;
import edu.udel.cis.vsl.sarl.preuniverse.IF.PreUniverse;
import edu.udel.cis.vsl.sarl.simplify.IF.Range;

/**
 * <p>
 * Implementation of {@link Range} in which a set is represented as a finite
 * union of intervals. This class is immutable.
 * </p>
 * 
 * <p>
 * The following are invariants. They force there to be a unique representation
 * for each set of numbers:
 * 
 * <ol>
 * 
 * <li>All intervals in the array are non-<code>null</code> {@link Interval}s.
 * </li>
 * 
 * <li>An empty interval cannot occur in the array.</li>
 * 
 * <li>All of the intervals in the array are disjoint.</li>
 * 
 * <li>The intervals in the array are ordered from least to greatest.</li>
 * 
 * <li>If an interval has the form {a,+infty}, then it is open on the right. If
 * an interval has the form {-infty,a}, then it is open on the left.</li>
 * 
 * <li>If {a,b} and {b,c} are two consecutive intervals in the list, the first
 * one must be open on the right and the second one must be open on the left.
 * </li>
 * 
 * <li>If the range set has integer type, all of the intervals are integer
 * intervals. If it has real type, all of the intervals are real intervals. (All
 * integral intervals are actually )</li>
 * 
 * <li>If the range set has integer type, all of the intervals are closed,
 * except for +infty and -infty.</li>
 * 
 * </ol>
 * </p>
 * 
 *
 * @author Wenhao Wu
 *
 */
public class IntervalUnionSet implements Range {
	// TODO: Add pre-cond for necessary construcors or functions.
	private static NumberFactory numberFactory = Numbers.REAL_FACTORY;

	private static int GAP_THRESHOLD_VALUE = 2;

	private static Number GAP_THRESHOLD_NUMBER = numberFactory
			.integer(GAP_THRESHOLD_VALUE);

	/**
	 * {@link #rSign} stores the categorization of <code>this</code>
	 * {@link IntervalUnionSet} based on their relationship to <code>0</code>
	 */
	private RangeSign rSign;

	/**
	 * A boolean value to represent whether this {@link IntervalUnionSet} is
	 * integral or not: <code>true</code> - it is integral, or
	 * <code>false</code> - it is rational.
	 */
	private boolean isInt;

	/**
	 * An sorted array of {@link Interval}s; this {@link IntervalUnionSet} is
	 * the union of these {@link Interval}s.
	 */
	private Interval[] intervalArray;

	/**
	 * Constructs an {@link IntervalUnionSet} with defined type and size. It is
	 * used for in-class functions which
	 * 
	 * @param isIntegral
	 *            A boolean value to represent whether <code>this</code>
	 *            {@link IntervalUnionSet} is integral.
	 * @param size
	 *            A positive integer to represent the number of disjointed
	 *            {@link Interval}s in <code>this</code>
	 *            {@link IntervalUnionSet}. It would be 0, iff <code>this</code>
	 *            set is empty.
	 */
	IntervalUnionSet(boolean isIntegral, int size) {
		isInt = isIntegral;
		intervalArray = new Interval[size];
	}

	/**
	 * Constructs an {@link IntervalUnionSet} representing an empty set
	 * 
	 * @param isIntegral
	 *            A boolean value to represent whether <code>this</code>
	 *            {@link IntervalUnionSet} is integral.
	 */
	public IntervalUnionSet(boolean isIntegral) {
		isInt = isIntegral;
		intervalArray = new Interval[0];
	}

	/**
	 * Constructs an {@link IntervalUnionSet} with exactly one {@link Interval}
	 * containing exactly one {@link Number}.
	 * 
	 * @param number
	 *            a non-<code>null</code> finite {@link Number}
	 */
	public IntervalUnionSet(Number number) {
		assert number != null && !number.isInfinite();

		Interval interval = numberFactory.newInterval(
				number instanceof IntegerNumber, number, false, number, false);

		isInt = number instanceof IntegerNumber;
		intervalArray = new Interval[1];
		intervalArray[0] = interval;
	}

	/**
	 * Constructs an {@link IntervalUnionSet} with two {@link Number}s , two
	 * boolean representing whether the bound is closed or not, and one boolean
	 * showing whether the {@link IntervalUnionSet} is a integral one or not.
	 * 
	 * @param left
	 *            a non-<code>null</code> {@link Number}
	 * @param strictLeft
	 *            a boolean value
	 * @param right
	 *            a non-<code>null</code> {@link Number}
	 * @param rightstrict
	 *            a boolean value
	 * @param isIntegral
	 *            a boolean value
	 */
	public IntervalUnionSet(Number left, boolean strictLeft, Number right,
			boolean strictRight, boolean isIntegral) {
		assert left != null && right != null;

		Interval interval = numberFactory.newInterval(isIntegral, left,
				strictLeft, right, strictRight);

		isInt = isIntegral;
		intervalArray = new Interval[1];
		intervalArray[0] = interval;
	}

	/**
	 * Constructs an {@link IntervalUnionSet} with exactly one {@link Interval}.
	 * 
	 * @param interval
	 *            a non-<code>null</code> {@link Interval}.
	 */
	public IntervalUnionSet(Interval interval) {
		// intervals are immutable, so re-use:
		assert interval != null;

		isInt = interval.isIntegral();
		if (interval.isEmpty()) {
			intervalArray = new Interval[0];
		} else {
			intervalArray = new Interval[1];
			intervalArray[0] = interval;
		}
	}

	/**
	 * Constructs an {@link IntervalUnionSet} with an array of {@link Interval}
	 * s.
	 * 
	 * @param intervals
	 *            an array of {@link Interval}s (with at least one element) with
	 *            same type (real/integer).
	 */
	public IntervalUnionSet(Interval... intervals) {
		assert intervals != null;
		assert intervals.length >= 0;

		int inputIndex = 0, size = 0;
		int inputSize = intervals.length;
		ArrayList<Interval> list = new ArrayList<Interval>();

		while (inputIndex < inputSize) {
			Interval temp = intervals[inputIndex];

			if (temp != null) {
				isInt = temp.isIntegral();
				if (!temp.isEmpty()) {
					isInt = temp.isIntegral();
					list.add(temp);
					inputIndex++;
					break;
				}
			}
			inputIndex++;
		}
		while (inputIndex < inputSize) {
			Interval temp = intervals[inputIndex];

			if (list.get(0).isUniversal()) {
				break;
			}
			if (temp != null && !temp.isEmpty()) {
				assert isInt == temp.isIntegral();
				// TODO: Throws an Excpetion for mismatched type
				addInterval(list, temp);
			}
			inputIndex++;
		}
		size = list.size();
		intervalArray = new Interval[size];
		list.toArray(intervalArray);
	}

	/**
	 * Constructs an {@link IntervalnionSet} being same with <code>other</code>.
	 * 
	 * @param other
	 *            A non-<code>null</code> {@link IntervalnionSet} would be
	 *            copied.
	 */
	public IntervalUnionSet(IntervalUnionSet other) {
		assert other != null;

		int size = other.intervalArray.length;

		isInt = other.isInt;
		intervalArray = new Interval[size];
		System.arraycopy(other.intervalArray, 0, intervalArray, 0, size);
	}

	/**
	 * Get all {@link Interval}s contained in <code>this</code>
	 * {@link IntervalUnionSet};<br>
	 * If <code>this</code> is an empty set, an array with size of zero will be
	 * returned.
	 * 
	 * @return An array of {@link Interval}s
	 */
	public Interval[] intervals() {
		return this.intervalArray;
	}

	/**
	 * Generate the {@link RangeSign} for <code>this</code>
	 * {@link IntervalUnionSet}, which implements {@link Range}.
	 */
	private void updateRangeSign() {
		int numInterval = intervalArray.length;

		if (numInterval == 0)
			this.rSign = RangeSign.EMPTY;
		else {
			Interval leftMostInterval = intervalArray[0];
			Interval rightMostInterval = intervalArray[numInterval - 1];
			Number leftmost = leftMostInterval.lower();
			Number rightmost = rightMostInterval.upper();

			if (numInterval == 1 && leftmost.isZero())
				this.rSign = RangeSign.EQ0;
			else if (leftmost.signum() > 0)
				this.rSign = RangeSign.GT0;
			else if (leftmost.signum() == 0 && !leftMostInterval.strictLower())
				this.rSign = RangeSign.GE0;
			else if (rightmost.signum() < 0)
				this.rSign = RangeSign.LT0;
			else if (rightmost.signum() == 0
					&& !rightMostInterval.strictUpper())
				this.rSign = RangeSign.LE0;
			else
				this.rSign = RangeSign.ALL;
		}
	}

	/**
	 * To union a single non-<code>null</code> {@link Interval} into given list.
	 * 
	 * @param list
	 * @param interval
	 *            A non-<code>null</code> {@link Interval} with the same type of
	 *            list.
	 */
	private void addInterval(ArrayList<Interval> list, Interval interval) {
		// TODO: add the pre-cond: list should satisfy all invariants.
		assert list != null;
		assert interval != null;
		assert isInt == interval.isIntegral();

		// TODO: add comments for magic numbers
		int size = list.size();
		int start = -2;
		int end = -2;
		int left = 0;
		int right = size - 1;
		Number lower = interval.lower();
		Number upper = interval.upper();
		boolean strictLower = interval.strictLower();
		boolean strictUpper = interval.strictUpper();
		boolean noIntersection = true;

		// TODO: check the state of the list (isUniversal?)
		if (interval.isUniversal()) {
			list.clear();
			list.add(interval);
		} else if (lower.isInfinite()) {
			start = -1;
		} else if (upper.isInfinite()) {
			end = size;
		}
		while (left < right && start == -2) {
			// TODO: Check once for -2
			int mid = (left + right) / 2;
			Interval temp = list.get(mid);
			int compare = temp.compare(lower);

			if (lower == temp.lower() && strictLower && temp.strictLower()) {
				compare = 0; // For case: (1, ...) with (1, ...)
			}
			if (compare == 0) {
				start = mid;
				lower = temp.lower();
				strictLower = temp.strictLower();
				noIntersection = false;
				break;
			} else if (compare > 0) {
				right = mid - 1;
			} else {
				left = mid + 1;
			}
		}
		if (start == -2) {
			if (right < left) {
				if (right < 0) {
					start = -1;
				} else {
					int compareJoint = compareJoint(list.get(right), interval);

					if (compareJoint < 0) {
						start = left;
					} else {
						start = right;
						lower = list.get(start).lower();
						strictLower = list.get(start).strictLower();
						noIntersection = false;
					}
				}
			} else { // right == left
				Interval temp = list.get(right);
				int compareJoint = 0;
				int compare = temp.compare(lower);

				if (lower == temp.lower() && strictLower
						&& temp.strictLower()) {
					compare = 0;
				}
				if (compare > 0) {
					if (right < 1) {
						start = -1;
					} else {
						compareJoint = compareJoint(list.get(right - 1),
								interval);
						if (compareJoint < 0) {
							start = right;
						} else {
							start = right - 1;
							lower = list.get(start).lower();
							strictLower = list.get(start).strictLower();
							noIntersection = false;
						}
					}
				} else if (compare < 0) {
					compareJoint = compareJoint(list.get(right), interval);
					if (compareJoint < 0) {
						start = right + 1;
					} else {
						start = right;
						lower = list.get(start).lower();
						strictLower = list.get(start).strictLower();
						noIntersection = false;
					}
				} else {
					start = right;
					lower = list.get(start).lower();
					strictLower = list.get(start).strictLower();
					noIntersection = false;
				}
			}
		}
		if (start == size) {
			list.add(interval);
			return;
		}
		left = 0;
		right = size - 1;
		while (left < right && end == -2) {
			int mid = (left + right) / 2;
			Interval temp = list.get(mid);
			int compare = temp.compare(upper);

			if (upper == temp.upper() && strictUpper && temp.strictUpper()) {
				compare = 0;
			}
			if (compare == 0) {
				end = mid;
				upper = list.get(end).upper();
				strictUpper = list.get(end).strictUpper();
				noIntersection = false;
				break;
			} else if (compare > 0) {
				right = mid - 1;
			} else {
				left = mid + 1;
			}
		}
		if (end == -2) {
			if (right < left) {
				if (right < 0) {
					end = -1;
				} else {
					int compareJoint = compareJoint(interval, list.get(left));

					if (compareJoint < 0) {
						end = right;
					} else {
						end = left;
						upper = list.get(end).upper();
						strictUpper = list.get(end).strictUpper();
						noIntersection = false;
					}
				}
			} else { // right == left
				Interval temp = list.get(right);
				int compareJoint = 0;
				int compare = temp.compare(upper);

				if (upper == temp.upper() && strictUpper
						&& temp.strictUpper()) {
					compare = 0;
				}
				if (compare > 0) {
					compareJoint = compareJoint(interval, list.get(right));
					if (compareJoint < 0) {
						end = right - 1;
					} else {
						end = right;
						upper = list.get(end).upper();
						strictUpper = list.get(end).strictUpper();
						noIntersection = false;
					}
				} else if (compare < 0) {
					if (right >= size - 1) {
						end = size - 1;
					} else {
						compareJoint = compareJoint(interval,
								list.get(right + 1));
						if (compareJoint < 0) {
							end = right;
						} else {
							end = right + 1;
							upper = list.get(end).upper();
							strictUpper = list.get(end).strictUpper();
							noIntersection = false;
						}
					}
				} else {
					end = right;
					upper = list.get(end).upper();
					strictUpper = list.get(end).strictUpper();
					noIntersection = false;
				}
			}
		}
		if (noIntersection) {
			// assert start >= end;
			start = end == -1 ? 0 : start;
			start = start == -1 ? 0 : start;
			list.add(start, interval);
		} else {
			start = start < 0 ? 0 : start;
			end = end < size ? end : (size - 1);
			list.subList(start, end + 1).clear();
			list.add(start, numberFactory.newInterval(isInt, lower, strictLower,
					upper, strictUpper));

		}
	}

	/**
	 * To compare the lower of two given {@link Interval}s
	 * 
	 * @param current
	 *            a non-<code>null</code> {@link Interval}.
	 * @param target
	 *            a non-<code>null</code> {@link Interval} has the same
	 *            type(real/integer) with <code>current</code>
	 * @return a negative integer iff <code>current</code> is left-most, a
	 *         positive integer iff <code>target</code> is left-most, or a zero
	 *         integer iff their lower and strictLower are exactly same.
	 */
	private int compareLo(Interval current, Interval target) {
		// assert current != null && target != null;
		// assert current.isIntegral() == target.isIntegral();

		boolean currentSL = current.strictLower();
		boolean targetSL = target.strictLower();
		Number currentLo = current.lower();
		Number targetLo = target.lower();

		if (currentLo.isInfinite() && targetLo.isInfinite()) {
			return 0; // Both are negative infinity
		} else if (currentLo.isInfinite()) {
			return -1;
		} else if (targetLo.isInfinite()) {
			return 1;
		} else {
			int compare = numberFactory.compare(currentLo, targetLo);

			if (compare == 0) {
				if (!currentSL && targetSL) {
					return -1;
				} else if (currentSL && !targetSL) {
					return 1;
				} else {
					return 0;
				}
			} else {
				return compare;
			}
		}
	}

	/**
	 * To compare the upper of two given {@link Interval}s
	 * 
	 * @param current
	 *            a non-<code>null</code> {@link Interval}.
	 * @param target
	 *            a non-<code>null</code> {@link Interval} has the same
	 *            type(real/integer) with <code>current</code>
	 * @return a negative integer iff <code>target</code> is right-most, a
	 *         positive integer iff <code>current</code> is right-most, or a
	 *         zero integer iff their upper and strictUpper are exactly same.
	 */
	private int compareUp(Interval current, Interval target) {
		// assert current != null && target != null;
		// assert current.isIntegral() == target.isIntegral();

		boolean currentSU = current.strictUpper();
		boolean targetSU = target.strictUpper();
		Number currentUp = current.upper();
		Number targetUp = target.upper();

		if (currentUp.isInfinite() && targetUp.isInfinite()) {
			return 0; // Both are positive infinity
		} else if (currentUp.isInfinite()) {
			return 1;
		} else if (targetUp.isInfinite()) {
			return -1;
		} else {
			int compare = numberFactory.compare(currentUp, targetUp);

			if (compare == 0) {
				if (!currentSU && targetSU) {
					return 1;
				} else if (currentSU && !targetSU) {
					return -1;
				} else {
					return 0;
				}
			} else {
				return compare;
			}
		}
	}

	/**
	 * To determine whether two given {@link Interval}s are jointed, it means
	 * that those two {@link Interval}s could be combined as a single one, by
	 * comparing <code>left</code>'s upper with <code>right</code>'s lower.
	 * 
	 * @param left
	 *            a non-<code>null</code> {@link Interval}.
	 * @param right
	 *            a non-<code>null</code> {@link Interval} has the same
	 *            type(real/integer) with <code>left</code>, and its lower
	 *            should greater than or equal to <code>left</code>'s lower.
	 * @return a negative integer iff they are NOT jointed, a zero integer iff
	 *         they are adjacent but no intersected, or a positive integer iff
	 *         they are intersected.
	 */
	private int compareJoint(Interval left, Interval right) {
		// assert left != null && right != null;
		// assert left.isIntegral() == right.isIntegral();

		boolean isIntegral = left.isIntegral();
		boolean leftSU = left.strictUpper();
		boolean rightSL = right.strictLower();
		Number leftUp = left.upper();
		Number rightLo = right.lower();

		if (leftUp.isInfinite() || rightLo.isInfinite()) {
			return 1;
		}

		Number difference = numberFactory.subtract(rightLo, leftUp);

		if (isIntegral) {
			/*
			 * For integral intervals even if the difference of their adjacent
			 * bound are 1, they are considered jointed.
			 */
			// e.g. [1, 1] U [2, 2] == [1, 2]
			if (difference.isOne()) {
				return 0;
			} else if (difference.signum() > 0) {
				return -1;
			} else {
				return 1;
			}
		} else {
			if (difference.signum() < 0) {
				return 1;
			} else if (difference.signum() > 0) {
				return -1;
			} else {
				if (leftSU && rightSL) {
					/*
					 * For rational intervals if the difference of their
					 * adjacent bound are 0 but both strict values are true,
					 * they are considered disjointed.
					 */
					// e.g. Both [0, 1) and (1, 2] excludes [1, 1]
					return -1;
				} else if (!leftSU && !rightSL) {
					return 1;
				} else {
					return 0;
				}
			}
		}
	}

	/**
	 * Does this {@link IntervalUnionSet} contain the given {@link Interval} as
	 * a sub-set?
	 * 
	 * @param interval
	 *            any non-<code>null</code> {@link Interval} of the same type
	 *            (integer/real) as this {@link IntervalUnionSet}
	 * @return <code>true</code> iff this {@link IntervalUnionSet} contains the
	 *         given {@link Interval}
	 */
	private boolean contains(Interval interval) {
		// assert interval != null;
		// assert interval.isIntegral() == isInt;

		int thisSize = intervalArray.length;
		int leftIdx = 0;
		int rightIdx = thisSize - 1;

		/*
		 * Currently used in contains(set), the interval could not be empty.
		 */
		// if (interval.isEmpty()) {
		// return true;
		// }// Any sets would contain an empty set
		while (leftIdx <= rightIdx) {
			int midIdx = (leftIdx + rightIdx) / 2;
			Interval midInterval = intervalArray[midIdx];
			int compareLower = compareLo(midInterval, interval);
			int compareUpper = compareUp(midInterval, interval);

			if (compareLower > 0) {
				if (compareUpper > 0) {
					int compareJoint = compareJoint(interval, midInterval);

					if (compareJoint <= 0) { // No intersection
						rightIdx = midIdx - 1;
						continue;
					}
				}
				return false;
			} else if (compareLower < 0) {
				if (compareUpper < 0) {
					int compareJoint = compareJoint(midInterval, interval);

					if (compareJoint > 0) {
						return false;
					} else { // No intersection
						leftIdx = midIdx + 1;
						continue;
					}
				} else { // compareUp >= 0
					return true;
				}
			} else {
				if (compareUpper < 0) {
					return false;
				} else {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Add a single {@link Number} to this {@link IntervalUnionSet}
	 * 
	 * @param number
	 *            a single non-<code>null</code> {@link Number} of the same type
	 *            (integer/real) with this {@link IntervalUnionSet}
	 */
	public Range addNumber(Number number) {
		assert number != null;
		assert (number instanceof IntegerNumber) == isInt;

		int size = intervalArray.length;
		int leftIndex = 0;
		int rightIndex = size - 1;

		while (leftIndex <= rightIndex) {
			int midIndex = (leftIndex + rightIndex) / 2;
			int compareNumber = intervalArray[midIndex].compare(number);

			if (compareNumber > 0 && leftIndex != rightIndex) {
				// The number is on the left part of the current set.
				rightIndex = midIndex - 1;
			} else if (compareNumber < 0 && leftIndex != rightIndex) {
				// The number is on the right part of the current set.
				leftIndex = midIndex + 1;
			} else if (compareNumber == 0) {
				// The set contains the number.
				return new IntervalUnionSet(this);
			} else {
				// The set does NOT contain the number
				leftIndex = compareNumber < 0 ? midIndex : midIndex - 1;
				rightIndex = leftIndex + 1;
				leftIndex = Math.max(leftIndex, 0);
				rightIndex = Math.min(rightIndex, size - 1);

				boolean leftSl = intervalArray[leftIndex].strictLower();
				boolean rightSu = intervalArray[rightIndex].strictUpper();
				Number leftLo = intervalArray[leftIndex].lower();
				Number leftUp = intervalArray[leftIndex].upper();
				Number rightLo = intervalArray[rightIndex].lower();
				Number rightUp = intervalArray[rightIndex].upper();
				Number leftDiff = numberFactory.subtract(number, leftUp);
				Number rightDiff = numberFactory.subtract(rightLo, number);
				boolean leftJoint = isInt ? leftDiff.isOne()
						: leftDiff.isZero();
				boolean rightJoint = isInt ? rightDiff.isOne()
						: rightDiff.isZero();

				if (leftJoint && rightJoint) {
					// The number connects two disjointed interval
					IntervalUnionSet result = new IntervalUnionSet(isInt,
							size - 1);

					System.arraycopy(intervalArray, 0, result.intervalArray, 0,
							leftIndex);
					result.intervalArray[leftIndex] = numberFactory.newInterval(
							isInt, leftLo, leftSl, rightUp, rightSu);
					System.arraycopy(intervalArray, rightIndex + 1,
							result.intervalArray, rightIndex,
							size - rightIndex - 1);
					result.updateRangeSign();
					return result;
				} else if (leftJoint) {
					// The number changes an interval's lower condition
					IntervalUnionSet result = new IntervalUnionSet(this);

					if (isInt) {
						result.intervalArray[leftIndex] = numberFactory
								.newInterval(true, leftLo, false, number,
										false);
					} else {
						result.intervalArray[leftIndex] = numberFactory
								.newInterval(false, leftLo, leftSl, number,
										false);
					}
					result.updateRangeSign();
					return result;
				} else if (rightJoint) {
					// The number changes an interval's upper condition
					IntervalUnionSet result = new IntervalUnionSet(this);

					if (isInt) {
						result.intervalArray[rightIndex] = numberFactory
								.newInterval(true, number, false, rightUp,
										false);
					} else {
						result.intervalArray[rightIndex] = numberFactory
								.newInterval(false, number, false, rightUp,
										rightSu);
					}
					result.updateRangeSign();
					return result;
				} else {
					// The number becomes a new point interval
					IntervalUnionSet result = new IntervalUnionSet(isInt,
							size + 1);

					if (leftIndex == rightIndex) {
						if (leftIndex == 0) {
							// To add the number to the head
							result.intervalArray[0] = numberFactory.newInterval(
									isInt, number, false, number, false);
							System.arraycopy(intervalArray, 0,
									result.intervalArray, 1, size);
						} else {
							// To add the number to the tail
							result.intervalArray[size] = numberFactory
									.newInterval(isInt, number, false, number,
											false);
							System.arraycopy(intervalArray, 0,
									result.intervalArray, 0, size);
						}
					} else {
						// To insert the number to the body
						System.arraycopy(intervalArray, 0, result.intervalArray,
								0, rightIndex);
						result.intervalArray[rightIndex] = numberFactory
								.newInterval(isInt, number, false, number,
										false);
						System.arraycopy(intervalArray, rightIndex,
								result.intervalArray, rightIndex + 1,
								size - rightIndex);
					}
					result.updateRangeSign();
					return result;
				}
			}
		} // Using binary searching to compare the number with intervals
		return new IntervalUnionSet(number);// To add a number to an empty set.
	}

	@Override
	public boolean isIntegral() {
		return isInt;
	}

	@Override
	public boolean isEmpty() {
		return intervalArray.length == 0;
	}

	@Override
	public boolean isUniversal() {
		return intervalArray.length == 1
				&& intervalArray[0].upper().isInfinite()
				&& intervalArray[0].lower().isInfinite();
	}

	@Override
	public boolean containsNumber(Number number) {
		assert number != null;
		assert (number instanceof IntegerNumber) == isInt;

		int size = intervalArray.length;
		int leftIdx = 0;
		int rightIdx = size - 1;

		while (leftIdx <= rightIdx) {
			int midIdx = (leftIdx + rightIdx) / 2;
			int compareNumber = intervalArray[midIdx].compare(number);

			if (compareNumber > 0) {
				rightIdx = midIdx - 1;
			} else if (compareNumber < 0) {
				leftIdx = midIdx + 1;
			} else if (compareNumber == 0) {
				return true;
			} else {
				break;
			}
		} // Using binary searching to compare the number with intervals
		return false;
	}

	@Override
	public boolean contains(Range set) {
		assert set != null;
		assert set.isIntegral() == isInt;

		IntervalUnionSet other = (IntervalUnionSet) set;
		Interval[] thisArray = intervalArray;
		Interval[] otherArray = other.intervalArray;
		int thisSize = thisArray.length;
		int otherSize = otherArray.length;
		int thisIndex = 0;
		int otherIndex = 0;

		if (otherSize < 1) {
			// Any set contains an empty set
			return true;
		} else if (thisSize < 1) {
			// An empty set does NOT contain any non-empty set.
			return false;
		} else if (thisArray[0].isUniversal()) {
			// A universal set contains any set.
			return true;
		} else if (otherArray[0].isUniversal()) {
			// Only a universal set could contain itself
			return false;
		} else if (otherSize == 1) {
			return contains(otherArray[0]);
		}
		while (thisIndex < thisSize) {
			Interval thisInterval = thisArray[thisIndex];

			while (otherIndex < otherSize) {
				Interval otherInterval = otherArray[otherIndex];
				int compareLower = compareLo(otherInterval, thisInterval);

				if (compareLower < 0) {
					return false;
				} else if (compareLower > 0) {
					int compareJoint = compareJoint(thisInterval,
							otherInterval);

					if (compareJoint > 0) {
						int compareUpper = compareUp(otherInterval,
								thisInterval);

						if (compareUpper < 0) {
							otherIndex++;
						} else if (compareUpper > 0) {
							return false;
						} else {
							otherIndex++;
							thisIndex++;
							break;
						}
					} else {
						if (thisIndex >= thisSize - 1) {
							return false;
						}
						thisIndex++;
						break;
					}
				} else {
					int compareUpper = compareUp(otherInterval, thisInterval);

					if (compareUpper > 0) {
						return false;
					} else if (compareUpper < 0) {
						otherIndex++;
					} else {
						otherIndex++;
						thisIndex++;
						break;
					}
				}
			}
			if (otherIndex == otherSize) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean intersects(Range set) {
		assert set != null;
		assert set.isIntegral() == isInt;

		IntervalUnionSet other = (IntervalUnionSet) set;
		Interval[] thisArray = intervalArray;
		Interval[] otherArray = other.intervalArray;
		int thisSize = thisArray.length;
		int otherSize = otherArray.length;
		int thisIndex = 0;
		int otherIndex = 0;

		if (thisSize == 0 || otherSize == 0) {
			return false;
		} // An empty set could not intersect with any sets.
		while (thisIndex < thisSize && otherIndex < otherSize) {
			Interval thisInterval = thisArray[thisIndex];
			Interval otherInterval = otherArray[otherIndex];
			int compareLower = compareLo(otherInterval, thisInterval);
			int compareUpper = compareUp(otherInterval, thisInterval);

			if (compareLower > 0) {
				if (compareUpper > 0) {
					int compareJoint = compareJoint(thisInterval,
							otherInterval);

					if (compareJoint <= 0) { // No intersection
						thisIndex++;
						break;
					}
				}
				return true;
			} else if (compareLower < 0) {
				if (compareUpper < 0) {
					int compareJoint = compareJoint(otherInterval,
							thisInterval);

					if (compareJoint <= 0) { // No intersection
						otherIndex++;
						continue;
					}
				}
				return true;
			} else {
				return true;
			}
		}
		return false;
	}

	
	@Override
	public BooleanExpression symbolicRepresentation(NumericExpression x,
			PreUniverse universe) {
		assert x != null;
		assert universe != null;

		BooleanExpression trueExpr = universe.trueExpression();

		if (this.isUniversal())
			return trueExpr;

		BooleanExpression result = universe.falseExpression();
		int size = intervalArray.length;
		int index = 0;

		if (isInt)
			while (index < size) {
				Interval interval = intervalArray[index];
				index++;

				IntegerNumber intOne = numberFactory.oneInteger();
				Number lower = interval.lower();
				Number upper = interval.upper();
				BooleanExpression orClause;

				if (lower.equals(upper)) {
					// This interval represents a single number.
					assert !interval.strictLower() && !interval.strictUpper();
					orClause = universe.equals(x, universe.number(lower));
				} else {
					orClause = trueExpr;
					while (index < size) {
						interval = intervalArray[index];
						if (numberFactory.subtract(interval.lower(), upper)
								.numericalCompareTo(GAP_THRESHOLD_NUMBER) > 0)
							break;
						// ... U (lower,upper) U (upper,c) U ...
						for (IntegerNumber i = intOne; i.numericalCompareTo(
								GAP_THRESHOLD_NUMBER) < 0; i = numberFactory
										.add(i, intOne)) {
							orClause = universe.and(orClause,
									(BooleanExpression) universe.neq(x,
											universe.number(numberFactory
													.add(upper, i))));
						}
						upper = interval.upper();
						index++;
					}
					if (!lower.isInfinite())
						orClause = universe.and(orClause, universe
								.lessThanEquals(universe.number(lower), x));
					if (!upper.isInfinite())
						orClause = universe.and(orClause, universe
								.lessThanEquals(x, universe.number(upper)));
				}
				result = universe.or(result, orClause);
			}
		else
			while (index < size) {
				Interval interval = intervalArray[index];
				index++;

				Number lower = interval.lower();
				Number upper = interval.upper();
				boolean strictLower = interval.strictLower();
				boolean strictUpper = interval.strictUpper();
				BooleanExpression orClause;

				if (lower.equals(upper)) {
					// This interval represents a single number.
					assert !strictLower && !strictUpper;
					orClause = universe.equals(x, universe.number(lower));
				} else {
					orClause = trueExpr;
					while (strictUpper && index < size) {
						interval = intervalArray[index];
						if (!interval.strictLower()
								|| !interval.lower().equals(upper))
							break;
						// ... U (lower,upper) U (upper,c) U ...
						orClause = universe.and(orClause,
								(BooleanExpression) universe.neq(x,
										universe.number(upper)));
						upper = interval.upper();
						strictUpper = interval.strictUpper();
						index++;
					}
					if (!lower.isInfinite()) {
						NumericExpression lowerExpression = universe
								.number(lower);

						orClause = universe.and(orClause, strictLower
								? universe.lessThan(lowerExpression, x)
								: universe.lessThanEquals(lowerExpression, x));
					}
					if (!upper.isInfinite()) {
						NumericExpression upperExpression = universe
								.number(upper);

						orClause = universe.and(orClause, strictUpper
								? universe.lessThan(x, upperExpression)
								: universe.lessThanEquals(x, upperExpression));
					}
				}
				result = universe.or(result, orClause);
			}
		return result;
	}

	public String toString() {
		StringBuilder result = new StringBuilder();
		int index = 0;
		int size = intervalArray.length;

		if (0 == size) {
			result.append("(0, 0)");
		}
		while (index < size) {
			Interval temp = intervalArray[index];
			Number lower = temp.lower();
			Number upper = temp.upper();
			boolean slower = temp.strictLower();
			boolean sUpper = temp.strictUpper();

			result.append(slower ? "(" : "[");
			result.append(lower.isInfinite() ? "-infi" : lower);
			result.append(", ");
			result.append(upper.isInfinite() ? "+infi" : upper);
			result.append(sUpper ? ")" : "]");
			index++;
			if (index < size) {
				result.append(" U ");
			}
		}
		return result.toString();
	}

	/**
	 * To check all invariants of <code>this</code> {@link IntervalUnionSet}
	 * Those invariants are:
	 * 
	 * 1. All of intervals in the array are non-<code>null</code> intervals.
	 * 
	 * 2. An empty interval cannot occur in the array.
	 * 
	 * 3. All of the intervals in the array are disjoint.
	 * 
	 * 4. The intervals in the array are ordered from least to greatest.
	 * 
	 * 5. If an interval has the form {a,+infty}, then it is open on the right.
	 * If an interval has the form {-infty,a}, then it is open on the left.
	 * 
	 * 6. If {a,b} and {b,c} are two consecutive intervals in the list, the the
	 * first one must be open on the right and the second one must be open on
	 * the left.
	 * 
	 * 7. If the range set has integer type, all of the intervals are integer
	 * intervals. If it has real type, all of the intervals are real intervals.
	 * 
	 * 8. If the range set has integer type, all of the intervals are closed,
	 * except for +infty and -infty.
	 * 
	 * @return <code>true</code> iff every {@link Interval} in <code>this</code>
	 *         set ensures invariants (Because all {@link IntervalUnionSet}
	 *         constructors would ensure those invariants, this function should
	 *         always return the value of </code>true</code>)
	 */
	public boolean checkInvariants() {
		assert intervalArray != null;

		int index = 0;
		int size = intervalArray.length;
		Interval temp, prev = null;
		Number tempLower = null;
		Number tempUpper = null;
		boolean tempSl, tempSu = true;

		if (0 == size) {
			// It is an empty set.
			return true;
		}
		temp = intervalArray[index];
		index++;
		// Check 1:
		if (temp == null) {
			return false;
		}
		tempLower = temp.lower();
		tempUpper = temp.upper();
		tempSl = temp.strictLower();
		tempSu = temp.strictUpper();
		// Check 2:
		if (temp.isEmpty()) {
			return false;
		}
		// Check 3, 4, 6: Skipped the 1st
		// Check 5:
		if ((tempLower.isInfinite() && !tempSl)
				|| (tempUpper.isInfinite() && !tempSu)) {
			return false;
		}
		// Check 7:
		if (isInt != temp.isIntegral()) {
			return false;
		}
		// Check 8:
		if (isInt) {
			if ((!tempLower.isInfinite() && tempSl)
					|| (!tempUpper.isInfinite() && tempSu)) {
				return false;
			}
		}
		if (1 < size) {
			while (index < size) {
				prev = temp;
				temp = intervalArray[index];
				index++;
				// Check 1:
				if (temp == null) {
					return false;
				}
				tempLower = temp.lower();
				tempUpper = temp.upper();
				tempSl = temp.strictLower();
				tempSu = temp.strictUpper();
				// Check 2:
				if (temp.isEmpty()) {
					return false;
				}
				// Check 3:
				if (compareJoint(prev, temp) >= 0) {
					return false;
				}
				// Check 4:
				if (compareLo(prev, temp) >= 0 || compareUp(prev, temp) >= 0) {
					return false;
				}
				// Check 5:
				if ((tempLower.isInfinite() && !tempSl)
						|| (tempUpper.isInfinite() && !tempSu)) {
					return false;
				}
				// Check 6:
				if (tempLower.compareTo(prev.upper()) == 0
						&& (!tempSl || !prev.strictUpper())) {
					return false;
				}
				// Check 7:
				if (isInt != temp.isIntegral()) {
					return false;
				}
				// Check 8:
				if (isInt) {
					if ((!tempLower.isInfinite() && tempSl)
							|| (!tempUpper.isInfinite() && tempSu)) {
						return false;
					}
				}
			}
		}
		return true;
	}

	@Override
	public Number getSingletonValue() {
		if (intervalArray.length == 1) {
			assert intervalArray[0] != null;

			Interval singletonInterval = intervalArray[0];
			Number upper = singletonInterval.upper();

			if ((singletonInterval.lower().numericalCompareTo(upper) == 0)
					&& !(singletonInterval.strictLower()
							|| singletonInterval.strictUpper())) {
				return singletonInterval.lower();
			}
		}
		return null;
	}

	@Override
	public Interval asInterval() {
		if (intervalArray.length == 0) {
			return isInt ? numberFactory.emptyIntegerInterval()
					: numberFactory.emptyRealInterval();
		} else if (intervalArray.length == 1) {
			assert intervalArray[0] != null;
			return intervalArray[0];
		}
		return null;
	}

	@Override
	public int hashCode() {
		int hashCode = 2147483647;

		for (Interval i : intervalArray) {
			hashCode ^= i.isIntegral() ? 127 : 131071;
			hashCode ^= i.lower().hashCode();
			hashCode ^= i.strictLower() ? 8191 : 8388607;
			hashCode ^= i.upper().hashCode();
			hashCode ^= i.strictUpper() ? 2047 : 536870911;
		}
		return hashCode;
	}

	@Override
	public RangeSign sign() {
		if (rSign == null)
			updateRangeSign();
		return rSign;
	}

	@Override
	public Interval intervalOverApproximation() {
		int startIndex = 0;
		int endIndex = intervalArray.length - 1;

		if (endIndex < 1)
			return this.asInterval();
		return numberFactory.newInterval(isInt,
				intervalArray[startIndex].lower(),
				intervalArray[startIndex].strictLower(),
				intervalArray[endIndex].upper(),
				intervalArray[endIndex].strictUpper());
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof IntervalUnionSet))
			return false;

		boolean isEqual = true;
		int numInterval = this.intervalArray.length;
		IntervalUnionSet other = (IntervalUnionSet) obj;

		if (numInterval != other.intervalArray.length)
			return false;
		for (int i = 0; i < numInterval; i++) {
			isEqual = isEqual && numberFactory.compare(intervalArray[i],
					other.intervalArray[i]) == 0;
		}
		return isEqual;
	}
}
