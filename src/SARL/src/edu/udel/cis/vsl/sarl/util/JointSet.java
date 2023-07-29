package edu.udel.cis.vsl.sarl.util;

import java.util.Set;

/**
 * <p>
 * A set representing the union of two *disjoint* sets. This set is immutable:
 * the modification methods will all throw {@link UnsupportedOperationException}
 * s.
 * </p>
 * 
 * <p>
 * This set is backed by the two underlying sets. I.e., changes to the
 * underlying sets will be reflected in this set.
 * </p>
 * 
 * @author xxxx
 *
 * @param <E>
 *            the type of the elements of the sets
 */
public class JointSet<E> extends JointCollection<E> implements Set<E> {

	/**
	 * Constructs a new set representing the union of the two disjoint sets set1
	 * and set2. If set1 and set2 are not disjoint (i.e., if there is some x
	 * such that set1.contains(x) && set2.contains(x)) the behavior is
	 * completely undefined and probably wrong.
	 * 
	 * @param set1
	 *            the first set
	 * @param set2
	 *            the second set
	 */
	public JointSet(Set<E> set1, Set<E> set2) {
		super(set1, set2);
	}
}
