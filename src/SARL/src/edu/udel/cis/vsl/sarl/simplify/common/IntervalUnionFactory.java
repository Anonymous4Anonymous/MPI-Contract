package edu.udel.cis.vsl.sarl.simplify.common;

import java.util.ArrayList;

import edu.udel.cis.vsl.sarl.IF.number.IntegerNumber;
import edu.udel.cis.vsl.sarl.IF.number.Interval;
import edu.udel.cis.vsl.sarl.IF.number.Number;
import edu.udel.cis.vsl.sarl.IF.number.NumberFactory;
import edu.udel.cis.vsl.sarl.number.IF.Numbers;
import edu.udel.cis.vsl.sarl.simplify.IF.Range;
import edu.udel.cis.vsl.sarl.simplify.IF.RangeFactory;

public class IntervalUnionFactory implements RangeFactory {
	/**
	 * The {@link NumberFactory} used by <code>this</code>
	 * {@link IntervalUnionFactory}
	 */
	private static NumberFactory numberFactory = Numbers.REAL_FACTORY;

	/**
	 * The singleton of the integral universal {@link IntervalUnionSet}
	 */
	private static IntervalUnionSet UNIV_INT_SET = new IntervalUnionSet(
			numberFactory.universalIntegerInterval());

	/**
	 * The singleton of the rational universal {@link IntervalUnionSet}
	 */
	private static IntervalUnionSet UNIV_REAL_SET = new IntervalUnionSet(
			numberFactory.universalRealInterval());

	/**
	 * The singleton of the integral empty {@link IntervalUnionSet}
	 */
	private static IntervalUnionSet EMPTY_INT_SET = new IntervalUnionSet(true,
			0);

	/**
	 * The singleton of the rational empty {@link IntervalUnionSet}
	 */
	private static IntervalUnionSet EMPTY_REAL_SET = new IntervalUnionSet(false,
			0);

	public IntervalUnionFactory() {
		// Nothing
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
	 * To union a single non-<code>null</code> {@link Interval} into given list.
	 * 
	 * @param list
	 * @param interval
	 *            A non-<code>null</code> {@link Interval} with the same type of
	 *            list.
	 */
	private void addInterval(ArrayList<Interval> list, Interval interval) {
		// TODO: add the pre-cond: list should satisfy all invariants.
		// assert list != null;
		// assert interval != null;
		// assert isInt == interval.isIntegral();

		// TODO: add comments for magic numbers
		boolean isInt = interval.isIntegral();
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

			Interval firstInterval = list.get(left);

			if (firstInterval != null && firstInterval.lower().isInfinite()) {
				if (firstInterval.contains(upper)) {
					return;
				} else {
					list.remove(left);
					size--;
					right--;
				}
			}
		} else if (upper.isInfinite()) {
			end = size;

			Interval lastInterval = list.get(right);

			if (lastInterval != null && lastInterval.upper().isInfinite()) {
				if (lastInterval.contains(lower)) {
					return;
				} else {
					list.remove(right);
					size--;
					right--;
				}
			}
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

	@Override
	public Range newRange(Interval... intervals) {
		return new IntervalUnionSet(intervals);
	}

	@Override
	public Range emptySet(boolean isIntegral) {
		return isIntegral ? EMPTY_INT_SET : EMPTY_REAL_SET;
	}

	@Override
	public Range singletonSet(Number number) {
		assert number != null && !number.isInfinite();
		return newRange(numberFactory.newInterval(
				number instanceof IntegerNumber, number, false, number, false));
	}

	@Override
	public Range interval(boolean isIntegral, Number left, boolean strictLeft,
			Number right, boolean strictRight) {
		assert left != null && right != null;
		assert isIntegral == left instanceof IntegerNumber;
		assert isIntegral == right instanceof IntegerNumber;
		return newRange(numberFactory.newInterval(isIntegral, left, strictLeft,
				right, strictRight));
	}

	@Override
	public Range add(Range lRange, Range rRange) {
		IntervalUnionSet lSet = (IntervalUnionSet) lRange;
		IntervalUnionSet rSet = (IntervalUnionSet) rRange;
		Interval[] lIntervals = lSet.intervals();
		Interval[] rIntervals = rSet.intervals();
		int lSize = lIntervals.length;
		int rSize = rIntervals.length;
		Interval[] resultIntervals = new Interval[lSize * rSize];
		int resultIndex = 0;
		for (int i = 0; i < lSize; ++i) {
			for (int j = 0; j < rSize; ++j) {
				resultIntervals[resultIndex] = numberFactory.add(lIntervals[i],
						rIntervals[j]);
				resultIndex++;
			}
		}
		return new IntervalUnionSet(resultIntervals);
	}

	@Override
	public Range multiply(Range lRange, Range rRange) {
		IntervalUnionSet lSet = (IntervalUnionSet) lRange;
		IntervalUnionSet rSet = (IntervalUnionSet) rRange;
		Interval[] lIntervals = lSet.intervals();
		Interval[] rIntervals = rSet.intervals();
		int lSize = lIntervals.length;
		int rSize = rIntervals.length;
		Interval[] resultIntervals = new Interval[lSize * rSize];
		int resultIndex = 0;
		for (int i = 0; i < lSize; ++i) {
			for (int j = 0; j < rSize; ++j) {
				resultIntervals[resultIndex] = numberFactory
						.multiply(lIntervals[i], rIntervals[j]);
				resultIndex++;
			}
		}
		return new IntervalUnionSet(resultIntervals);
	}

	@Override
	public Range power(Range range, int exp) {
		IntervalUnionSet set = (IntervalUnionSet) range;
		Interval[] intervals = set.intervals();
		int size = intervals.length;
		Interval[] resultIntervals = new Interval[size];

		for (int i = 0; i < size; ++i) {
			resultIntervals[i] = numberFactory.power(intervals[i], exp);
		}
		return new IntervalUnionSet(resultIntervals);
	}

	@Override
	public Range affineTransform(Range range, Number a, Number b) {
		assert a != null && b != null;
		assert a instanceof IntegerNumber == range.isIntegral();
		assert b instanceof IntegerNumber == range.isIntegral();

		Boolean isInt = range.isIntegral();
		Interval[] thisIntervals = ((IntervalUnionSet) range).intervals();
		int index = 0;
		int size = thisIntervals.length;
		Interval[] intervals = new Interval[size];
		Interval newInterval = null, oldInterval = null;

		if (0 == size) { // IsEmpty
			return new IntervalUnionSet(isInt);
		}
		oldInterval = thisIntervals[index];
		if (oldInterval.isUniversal()) { // IsUniv
			return new IntervalUnionSet((IntervalUnionSet) range);
		}
		if (a.signum() == 0) { // a = 0
			return new IntervalUnionSet(
					numberFactory.newInterval(isInt, b, false, b, false));
		} else if (a.signum() > 0) { // a > 0
			while (index < size) {
				oldInterval = thisIntervals[index];
				newInterval = numberFactory.affineTransform(oldInterval, a, b);
				intervals[index] = newInterval;
				index++;
			}
		} else if (a.signum() < 0) { // a < 0
			while (index < size) {
				oldInterval = thisIntervals[index];
				newInterval = numberFactory.affineTransform(oldInterval, a, b);
				index++;
				intervals[size - index] = newInterval;
			}
		}
		return (IntervalUnionSet) newRange(intervals);
	}

	@Override
	public Range divide(Range range, Number constant) {
		assert range != null && constant != null;
		assert !constant.isZero() && !constant.isInfinite();
		assert range.isIntegral() == (constant instanceof IntegerNumber);

		IntervalUnionSet intervalSet = (IntervalUnionSet) range;
		Interval[] intervals = intervalSet.intervals();
		int size = intervals.length;
		Interval[] resultIntervals = new Interval[size];

		for (int i = 0; i < size; ++i) {
			resultIntervals[i] = numberFactory.divide(intervals[i], constant);
		}
		return new IntervalUnionSet(resultIntervals);
	}

	@Override
	public Range complement(Range range) {
		assert range != null;
		return setMinus(range.isIntegral() ? UNIV_INT_SET : UNIV_REAL_SET,
				range);
	}

	@Override
	public Range union(Range lRange, Range rRange) {
		assert lRange != null && rRange != null;
		assert lRange.isIntegral() == rRange.isIntegral();

		boolean isInt = lRange.isIntegral();
		IntervalUnionSet thisSet = (IntervalUnionSet) lRange;
		IntervalUnionSet otherSet = (IntervalUnionSet) rRange;
		Interval[] thisArray = thisSet.intervals();
		Interval[] otherArray = otherSet.intervals();
		int thisSize = thisArray.length;
		int otherSize = otherArray.length;

		if (thisSize <= 0) {
			return new IntervalUnionSet(otherSet);
		} else if (otherSize <= 0) {
			return new IntervalUnionSet(thisSet);
		}
		if (thisArray[0].isUniversal()) {
			return new IntervalUnionSet(thisSet);
		} else if (otherArray[0].isUniversal()) {
			return new IntervalUnionSet(otherSet);
		}
		// Using binary method to union a set with single interval
		if (thisSize == 1) {
			ArrayList<Interval> list = new ArrayList<Interval>();

			for (Interval i : otherArray) {
				list.add(i);
			}
			addInterval(list, thisArray[0]);

			IntervalUnionSet result = new IntervalUnionSet(isInt, list.size());

			list.toArray(result.intervals());
			return result;
		}
		if (otherSize == 1) {
			ArrayList<Interval> list = new ArrayList<Interval>();

			for (Interval i : thisArray) {
				list.add(i);
			}
			addInterval(list, otherArray[0]);

			IntervalUnionSet result = new IntervalUnionSet(isInt, list.size());

			list.toArray(result.intervals());
			return result;
		}

		int thisIndex = 0, otherIndex = 0;
		boolean isChanged = false;
		Interval thisInterval = thisArray[0];
		Interval otherInterval = otherArray[0];
		Interval temp = null;
		ArrayList<Interval> list = new ArrayList<Interval>();
		int compareLower = compareLo(thisInterval, otherInterval);

		// To find the left-most interval in two sets.
		if (compareLower > 0) {
			temp = otherInterval;
			otherIndex++;
		} else {
			temp = thisInterval;
			thisIndex++;
		}
		while (isChanged || thisIndex < thisSize || otherIndex < otherSize) {
			isChanged = false;
			while (thisIndex < thisSize) {
				Interval next = thisArray[thisIndex];
				int compareTempNext = compareJoint(temp, next);

				if (compareTempNext < 0) {
					// temp Left-disjoint next, then stop
					break;
				} else {
					int compareUpper = compareUp(temp, next);

					if (compareUpper < 0) {
						// Intersected, then union next with temp
						temp = numberFactory.newInterval(isInt, temp.lower(),
								temp.strictLower(), next.upper(),
								next.strictUpper());
						isChanged = true;
						thisIndex++;
						break;
					} // else temp Contains next, then skip
					thisIndex++;
				}
			}
			while (otherIndex < otherSize) {
				Interval next = otherArray[otherIndex];
				int compareTempNext = compareJoint(temp, next);

				if (compareTempNext < 0) {
					// Temp Left-disjoint next. then stop
					break;
				} else {
					int compareUpper = compareUp(temp, next);

					if (compareUpper < 0) {
						// Intersected, then union next with temp
						temp = numberFactory.newInterval(isInt, temp.lower(),
								temp.strictLower(), next.upper(),
								next.strictUpper());
						isChanged = true;
						otherIndex++;
						break;
					} // else temp Contains next, then skip
					otherIndex++;
				}
			}
			if (!isChanged) {
				list.add(temp);
				if (thisIndex < thisSize && otherIndex < otherSize) {
					// Get the new left-most interval
					thisInterval = thisArray[thisIndex];
					otherInterval = otherArray[otherIndex];
					compareLower = compareLo(thisInterval, otherInterval);
					if (compareLower > 0) {
						temp = otherInterval;
						otherIndex++;
					} else {
						temp = thisInterval;
						thisIndex++;
					}
				} else {
					// To add the remaining intervals
					while (thisIndex < thisSize) {
						list.add(thisArray[thisIndex]);
						thisIndex++;
					}
					while (otherIndex < otherSize) {
						list.add(otherArray[otherIndex]);
						otherIndex++;
					}
				}
			}
		}

		IntervalUnionSet result = new IntervalUnionSet(isInt, list.size());

		list.toArray(result.intervals());
		return result;
	}

	@Override
	public Range intersect(Range lRange, Range rRange) {
		assert lRange != null && rRange != null;
		assert lRange.isIntegral() == rRange.isIntegral();

		boolean isInt = lRange.isIntegral();
		ArrayList<Interval> list = new ArrayList<Interval>();
		Interval[] thisArray = ((IntervalUnionSet) lRange).intervals();
		Interval[] otherArray = ((IntervalUnionSet) rRange).intervals();
		Interval thisInterval = null;
		int thisSize = thisArray.length;
		int otherSize = otherArray.length;
		int thisIndex = 0;
		int otherIndex = 0;

		if (otherSize < 1 || thisSize < 1) {
			// Any set intersects an empty set is empty
			return new IntervalUnionSet(isInt);
		} else if (thisArray[0].isUniversal()) {
			return new IntervalUnionSet((IntervalUnionSet) rRange);
		} else if (otherArray[0].isUniversal()) {
			return new IntervalUnionSet((IntervalUnionSet) lRange);
		}

		while (thisIndex < thisSize && otherIndex < otherSize) {
			thisInterval = thisArray[thisIndex];
			Interval otherInterval = otherArray[otherIndex];
			int compareLower = compareLo(otherInterval, thisInterval);
			int compareUpper = compareUp(otherInterval, thisInterval);

			if (compareLower < 0) {
				if (compareUpper < 0) {
					int compareJoint = compareJoint(otherInterval,
							thisInterval);

					if (compareJoint > 0) {
						// Add the intersection into result set.
						list.add(numberFactory.newInterval(isInt,
								thisInterval.lower(),
								thisInterval.strictLower(),
								otherInterval.upper(),
								otherInterval.strictUpper()));
						thisInterval = numberFactory.newInterval(isInt,
								otherInterval.upper(),
								!otherInterval.strictUpper(),
								thisInterval.upper(),
								thisInterval.strictUpper());
					}
					// Else skip otherInterval
					otherIndex++;
				} else if (compareUpper > 0) {
					// thisInterval is contained
					list.add(thisInterval);
					thisIndex++;
				} else {
					// thisInterval is contained
					list.add(thisInterval);
					thisIndex++;
					otherIndex++;
				}
			} else if (compareLower > 0) {
				if (compareUpper < 0) {
					// otherInterval is contained
					// Cut thisInterval
					list.add(otherInterval);
					thisInterval = numberFactory.newInterval(isInt,
							otherInterval.upper(), !otherInterval.strictUpper(),
							thisInterval.upper(), thisInterval.strictUpper());
					otherIndex++;
				} else if (compareUpper > 0) {
					int compareJoint = compareJoint(thisInterval,
							otherInterval);

					if (compareJoint > 0) {
						// Add intersection into result set
						list.add(numberFactory.newInterval(isInt,
								otherInterval.lower(),
								otherInterval.strictLower(),
								thisInterval.upper(),
								thisInterval.strictUpper()));
					}
					thisIndex++;
				} else {
					// otherInterval is contained
					list.add(otherInterval);
					otherIndex++;
					thisIndex++;
				}
			} else {
				if (compareUpper < 0) {
					// otherInterval is contained
					// Cut thisInterval
					list.add(otherInterval);
					thisInterval = numberFactory.newInterval(isInt,
							otherInterval.upper(), !otherInterval.strictUpper(),
							thisInterval.upper(), thisInterval.strictUpper());
					otherIndex++;
				} else if (compareUpper > 0) {
					// thisInterval is contained
					list.add(thisInterval);
					thisIndex++;
				} else {
					// Both are equal
					list.add(thisInterval);
					thisIndex++;
					otherIndex++;
				}
			}
		}

		IntervalUnionSet result = new IntervalUnionSet(isInt, list.size());

		list.toArray(result.intervals());
		return result;
	}

	@Override
	public Range setMinus(Range lRange, Range rRange) {
		assert lRange != null && rRange != null;
		assert lRange.isIntegral() == rRange.isIntegral();

		boolean isInt = lRange.isIntegral();
		ArrayList<Interval> list = new ArrayList<Interval>();
		Interval[] thisArray = ((IntervalUnionSet) lRange).intervals();
		Interval[] otherArray = ((IntervalUnionSet) rRange).intervals();
		Interval thisInterval = null;
		int thisSize = thisArray.length;
		int otherSize = otherArray.length;
		int thisIndex = 0;
		int otherIndex = 0;

		if (otherSize < 1 || thisSize < 1) {
			// If this is empty, it returns "this" as an empty set.
			// If other is empty, it returns "this" as itself.
			return new IntervalUnionSet((IntervalUnionSet) lRange);
		} else if (otherArray[0].isUniversal()) {
			// If other is universal, it reuturns an empty set.
			return new IntervalUnionSet(isInt);
		}
		while (thisIndex < thisSize) {
			thisInterval = thisArray[thisIndex];
			while (otherIndex < otherSize) {
				Interval otherInterval = otherArray[otherIndex];
				int compareLower = compareLo(otherInterval, thisInterval);
				int compareUpper = compareUp(otherInterval, thisInterval);

				if (compareLower < 0) {
					if (compareUpper < 0) {
						int compareJoint = compareJoint(otherInterval,
								thisInterval);

						if (compareJoint > 0) {
							// Intersected, then shrink thisInterval
							thisInterval = numberFactory.newInterval(isInt,
									otherInterval.upper(),
									!otherInterval.strictUpper(),
									thisInterval.upper(),
									thisInterval.strictUpper());
						}
						// Else, skip otherInterval
						otherIndex++;
					} else if (compareUpper > 0) {
						// thisInterval is contained by otherInterval, skip
						// otherInterval may contain the next thisInterval
						thisIndex++;
						break;
					} else {
						// Skip both.
						thisIndex++;
						otherIndex++;
						break;
					}
				} else if (compareLower > 0) {
					if (compareUpper < 0) {
						// otherInterval is contained
						// Add the first piece of thisInterval into result set
						list.add(numberFactory.newInterval(isInt,
								thisInterval.lower(),
								thisInterval.strictLower(),
								otherInterval.lower(),
								!otherInterval.strictLower()));
						thisInterval = numberFactory.newInterval(isInt,
								otherInterval.upper(),
								!otherInterval.strictUpper(),
								thisInterval.upper(),
								thisInterval.strictUpper());
						otherIndex++;
					} else if (compareUpper > 0) {
						int compareJoint = compareJoint(thisInterval,
								otherInterval);

						if (compareJoint > 0) {
							// Cut the intersection
							list.add(numberFactory.newInterval(isInt,
									thisInterval.lower(),
									thisInterval.strictLower(),
									otherInterval.lower(),
									!otherInterval.strictLower()));
						} else {
							// thisInterval is safe to add into result set/
							list.add(thisInterval);
						}
						thisIndex++;
						break;
					} else {
						// Cut the intersection
						list.add(numberFactory.newInterval(isInt,
								thisInterval.lower(),
								thisInterval.strictLower(),
								otherInterval.lower(),
								!otherInterval.strictLower()));
						thisIndex++;
						otherIndex++;
						break;
					}
				} else {
					if (compareUpper < 0) {
						// Cut the intersection
						// Continue to check the next otherInterval
						thisInterval = numberFactory.newInterval(isInt,
								otherInterval.upper(),
								!otherInterval.strictUpper(),
								thisInterval.upper(),
								thisInterval.strictUpper());
						otherIndex++;
					} else if (compareUpper > 0) {
						// thisInterval is contained
						thisIndex++;
						break;
					} else {
						// Skip both
						thisIndex++;
						otherIndex++;
						break;
					}
				}
			}
			if (otherIndex == otherSize) {
				if (thisIndex < thisSize) {
					// Handling current remaining part of thisIntervals
					int compareUp = compareUp(thisInterval,
							thisArray[thisIndex]);

					if (compareUp == 0) {
						list.add(thisInterval);
						thisIndex++;
					}
				}
				// Handling remaining intervals of thisArray
				while (thisIndex < thisSize) {
					list.add(thisArray[thisIndex]);
					thisIndex++;
				}
				break;
			}
		}

		IntervalUnionSet result = new IntervalUnionSet(isInt, list.size());

		list.toArray(result.intervals());
		return result;
	}

	@Override
	public Range multiply(Range range, Number constant) {
		assert constant != null;
		assert !constant.isInfinite();
		assert constant instanceof IntegerNumber == range.isIntegral();

		Boolean isInt = range.isIntegral();
		Interval[] thisIntervals = ((IntervalUnionSet) range).intervals();
		int index = 0;
		int size = thisIntervals.length;
		Interval[] intervals = new Interval[size];
		Interval newInterval = null, oldInterval = null;

		if (0 == size) { // IsEmpty
			return new IntervalUnionSet(isInt);
		}
		oldInterval = thisIntervals[index];
		if (oldInterval.isUniversal()) { // IsUniv
			return new IntervalUnionSet((IntervalUnionSet) range);
		}
		if (constant.signum() == 0) { // a = 0
			return new IntervalUnionSet(numberFactory.newInterval(isInt,
					constant, false, constant, false));
		} else if (constant.signum() > 0) { // a > 0
			while (index < size) {
				oldInterval = thisIntervals[index];
				newInterval = numberFactory.multiply(constant, oldInterval);
				intervals[index] = newInterval;
				index++;
			}
		} else if (constant.signum() < 0) { // a < 0
			while (index < size) {
				oldInterval = thisIntervals[index];
				newInterval = numberFactory.multiply(constant, oldInterval);
				index++;
				intervals[size - index] = newInterval;
			}
		}
		return (IntervalUnionSet) newRange(intervals);
	}

	@Override
	public Range add(Range range, Number constant) {
		assert constant != null;
		assert !constant.isInfinite();
		assert constant instanceof IntegerNumber == range.isIntegral();

		Boolean isInt = range.isIntegral();
		Interval[] thisIntervals = ((IntervalUnionSet) range).intervals();
		int index = 0;
		int size = thisIntervals.length;
		Interval[] intervals = new Interval[size];
		Interval newInterval = null, oldInterval = null;

		if (0 == size) { // IsEmpty
			return new IntervalUnionSet(isInt);
		}
		oldInterval = thisIntervals[index];
		if (oldInterval.isUniversal()) { // IsUniv
			return new IntervalUnionSet((IntervalUnionSet) range);
		}
		while (index < size) {
			oldInterval = thisIntervals[index];
			newInterval = numberFactory.newInterval(isInt,
					numberFactory.add(oldInterval.lower(), constant),
					oldInterval.strictLower(),
					numberFactory.add(oldInterval.upper(), constant),
					oldInterval.strictUpper());
			intervals[index] = newInterval;
			index++;
		}
		return (IntervalUnionSet) newRange(intervals);
	}

	@Override
	public Range subtract(Range range, Number constant) {
		assert constant != null;
		assert !constant.isInfinite();
		return add(range, numberFactory.negate(constant));
	}

	@Override
	public Range subtract(Range lRange, Range rRange) {
		Interval[] lIntervals = ((IntervalUnionSet) lRange).intervals();
		Interval[] rIntervals = ((IntervalUnionSet) rRange).intervals();
		ArrayList<Interval> rawIntervals = new ArrayList<Interval>();

		for (int i = 0; i < lIntervals.length; i++)
			for (int j = 0; j < rIntervals.length; j++)
				rawIntervals.add(numberFactory.add(lIntervals[i],
						numberFactory.negate(rIntervals[j])));
		return new IntervalUnionSet(
				rawIntervals.toArray(new Interval[rawIntervals.size()]));
	}

	@Override
	public Range universalSet(boolean isIntegral) {
		return isIntegral ? UNIV_INT_SET : UNIV_REAL_SET;
	}

	@Override
	public Range power(Range range, IntegerNumber exp) {
		IntervalUnionSet set = (IntervalUnionSet) range;
		Interval[] intervals = set.intervals();
		int size = intervals.length;
		Interval[] resultIntervals = new Interval[size];

		for (int i = 0; i < size; ++i) {
			resultIntervals[i] = numberFactory.power(intervals[i], exp);
		}
		return new IntervalUnionSet(resultIntervals);
	}

	@Override
	public Range divide(Range range0, Range range1) {
		IntervalUnionSet set0 = (IntervalUnionSet) range0;
		IntervalUnionSet set1 = (IntervalUnionSet) range1;
		Interval[] intervals0 = set0.intervals();
		Interval[] intervals1 = set1.intervals();
		int size0 = intervals0.length;
		int size1 = intervals1.length;
		Interval[] resultIntervals = new Interval[size0 * size1];
		for (int i = 0; i < size0; ++i) {
			for (int j = 0; j < size1; ++i) {
				resultIntervals[i * size1 + j] = numberFactory
						.divide(intervals0[i], intervals1[j]);
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>
	 * If the first interval of ius = [a,b} and the first interval of context =
	 * [a,...) then replace the first interval of ius with (-infty,b}.
	 * </p>
	 * 
	 * <p>
	 * If the first interval of ius is (a,b} and the first interval of context
	 * is (a,...) then replace the first interval of ius with (-infty,b).
	 * </p>
	 * 
	 * Ditto for the last interval.
	 */
	@Override
	public Range expand(Range range, Range contextRange) {
		IntervalUnionSet ius = (IntervalUnionSet) range;
		IntervalUnionSet context = (IntervalUnionSet) contextRange;

		Interval[] ia1 = ius.intervals(), ia2 = context.intervals();
		int n1 = ia1.length;

		if (n1 == 0)
			return ius;

		Interval left1 = ia1[0], left2 = ia2[0], newLeft = null,
				newRight = null;
		boolean integral = ius.isIntegral();

		if (left1.strictLower() == left2.strictLower()) {
			Number lo = left1.lower(), hi = left1.upper();

			if (lo == left2.lower() && !lo.isInfinite() && lo != hi)
				newLeft = numberFactory.newInterval(integral,
						integral ? numberFactory.negativeInfinityInteger()
								: numberFactory.negativeInfinityRational(),
						true, hi, left1.strictUpper());
		}

		Interval right1 = n1 == 1 && newLeft != null ? newLeft : ia1[n1 - 1];
		Interval right2 = ia2[ia2.length - 1];

		if (right1.strictUpper() == right2.strictUpper()) {
			Number lo = right1.lower(), hi = right1.upper();

			if (hi == right2.upper() && !hi.isInfinite() && lo != hi)
				newRight = numberFactory.newInterval(integral, lo,
						right1.strictLower(),
						integral ? numberFactory.positiveInfinityInteger()
								: numberFactory.positiveInfinityRational(),
						true);
		}
		if (newLeft == null && newRight == null)
			return ius;
		if (n1 == 1)
			return newRange(newRight != null ? newRight : newLeft);

		Interval[] newIntervals = new Interval[n1];

		newIntervals[0] = newLeft == null ? ia1[0] : newLeft;
		newIntervals[n1 - 1] = newRight == null ? ia1[n1 - 1] : newRight;
		for (int i = 1; i < n1 - 1; i++)
			newIntervals[i] = ia1[i];
		return newRange(newIntervals);
	}

}
