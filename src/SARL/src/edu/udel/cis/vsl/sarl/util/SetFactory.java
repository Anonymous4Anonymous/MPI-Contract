package edu.udel.cis.vsl.sarl.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * A {@link SetFactory} is used to manipulate arrays as if they were sets. An
 * array is used to represent a set by listing its element in increasing order.
 * In particular a {@link SetFactory} requires a {@link Comparator} on the
 * element type of the set, to specify the order.
 * 
 * A set is represented as an array of its elements. Each element occurs exactly
 * once. They occur in increasing order.
 * 
 * This kind of set is implemented as a "key set" in which the key and value are
 * identical.
 * 
 * @author xxxx
 *
 * @param <V>
 *            the type of elements of the set
 */
public abstract class SetFactory<V> extends KeySetFactory<V, V> {

	/**
	 * Projection onto first component.
	 */
	private BinaryOperator<V> project1 = new BinaryOperator<V>() {
		@Override
		public V apply(V x, V y) {
			return x;
		}
	};

	/**
	 * Constructs a new set factory based on the given comparator.
	 * 
	 * @param comparator
	 *            a comparator on the element type
	 */
	public SetFactory(Comparator<V> comparator) {
		super(comparator);
	}

	/**
	 * The key and value are identical.
	 */
	protected V key(V value) {
		return value;
	}

	/**
	 * Returns the set which is the union of the two given sets.
	 * 
	 * @param set1
	 *            the first set
	 * @param set2
	 *            the second set
	 * @return the union of the two sets
	 */
	public V[] union(V[] set1, V[] set2) {
		return combine(project1, set1, set2);
	}

	private V[][] setArray(int size) {
		@SuppressWarnings("unchecked")
		V[][] result = (V[][]) new Object[size][];

		return result;
	}

	/**
	 * Computes the intersection of a collection of sets.
	 * 
	 * @param sets
	 *            a collection of sets
	 * @return the intersection of those sets
	 */
	public V[] intersection(Collection<V[]> sets) {
		int n = sets.size();

		if (n == 0)
			return emptySet();
		if (n == 1)
			return sets.iterator().next();

		int[] positions = new int[n];
		V[][] theSets = setArray(n);
		Iterator<V[]> setIter = sets.iterator();

		for (int i = 0; i < n; i++) {
			V[] set = setIter.next();

			if (set.length == 0)
				return emptySet();
			theSets[i] = set;
			positions[i] = 0;
		}

		Comparator<V> c = this.keyComparator();
		V x = theSets[0][0]; // candidate for adding to intersection
		LinkedList<V> resultList = new LinkedList<>(); // the intersection
		int idx = 0; // index of set with current candidate for intersection

		// loop invariant 0<=idx,i<n
		// loop invariant forall j 0<=positions[j]<theSets[j].length
		// forall j in idx..i-1 (cyclic ordering) theSets[j][positions[j]]==x
		outer: for (int i = 1; true; i = (i + 1) % n) {
			V[] set = theSets[i];
			int pos = positions[i];
			int len = set.length;

			if (i == idx) {
				// cycle complete: at this point, for all i,
				// theSets[i][positions[i]] == x
				resultList.add(x);
				for (int j = 0; j < n; j++)
					if (++positions[j] == theSets[j].length)
						break outer;
				x = set[pos + 1];
				idx = i;
			} else {
				int comparison;
				V y = set[pos];

				while ((comparison = c.compare(y, x)) < 0) {
					pos++;
					if (pos >= len)
						break outer;
					y = set[pos];
				}
				// at this point, y==set[pos] and either y==x or y>x
				positions[i] = pos;
				if (comparison > 0) { // y>x
					for (; idx != i; idx = (idx + 1) % n)
						if (++positions[idx] == theSets[idx].length)
							break outer;
					x = y;
				}
			}
		}
		return resultList.toArray(newSet(resultList.size()));
	}

	/**
	 * Factors out commonality from an array of sets. Give an array A of sets
	 * [S1,S2,...,Sn], the method returns the intersection U of the Si, and also
	 * modifies A so that it becomes [T1,T2,...,Tn], where Ti is Si-U. The
	 * elements of A themselves are not modified.
	 * 
	 * @param sets
	 *            a list of sets of V, where each set is represented as an
	 *            ordered array (as produced by this factory)
	 * @return the pair (U, [T1,T2,...,Tn]), where U is the intersection of the
	 *         Si and Ti is Si-U
	 */
	public V[] factor(V[][] sets) {
		int n = sets.length;

		if (n == 0)
			return emptySet();
		if (n == 1) {
			V[] result = sets[0];

			sets[0] = emptySet();
			return result;
		}

		int[] positions = new int[n];
		@SuppressWarnings("unchecked")
		LinkedList<V>[] newSetLists = (LinkedList<V>[]) new LinkedList[n];

		for (int i = 0; i < n; i++) {
			V[] set = sets[i];

			if (set.length == 0)
				return emptySet();
			newSetLists[i] = new LinkedList<V>();
			positions[i] = 0;
		}

		Comparator<V> c = this.keyComparator();
		V x = sets[0][0]; // candidate for adding to intersection
		LinkedList<V> resultList = new LinkedList<>(); // the intersection
		int idx = 0; // index of set with current candidate for intersection

		// loop invariant 0<=idx,i<n
		// loop invariant forall j 0<=positions[j]<theSets[j].length
		// forall j in idx..i-1 (cyclic ordering) theSets[j][positions[j]]==x
		outer: for (int i = 1; true; i = (i + 1) % n) {
			V[] set = sets[i];
			int pos = positions[i];
			int len = set.length;

			if (i == idx) {
				// cycle complete: at this point, for all i,
				// sets[i][positions[i]] == x
				boolean done = false;

				resultList.add(x);
				for (int j = 0; j < n; j++) {
					positions[j]++;
					if (!done && positions[j] == sets[j].length)
						done = true;
				}
				if (done)
					break;
				x = set[pos + 1];
				idx = i;
			} else {
				int comparison;
				V y = set[pos];
				LinkedList<V> newSetList = newSetLists[i];

				while ((comparison = c.compare(y, x)) < 0) {
					pos++;
					newSetList.add(y);
					if (pos >= len) {
						positions[i] = pos;
						break outer;
					}
					y = set[pos];
				}
				positions[i] = pos;
				if (comparison > 0) { // y>x
					while (idx != i) {
						V[] aSet = sets[idx];
						int aPos = positions[idx];

						newSetLists[idx].add(aSet[aPos]);
						aPos++;
						positions[idx] = aPos;
						if (aPos == aSet.length)
							break outer;
						idx = (idx + 1) % n;
					}
					x = y;
				}
			}
		}

		int intersectionSize = resultList.size();

		if (intersectionSize == 0)
			return emptySet();
		for (int i = 0; i < n; i++) {
			V[] oldSet = sets[i];
			int oldLen = oldSet.length;
			int newLen = oldLen - intersectionSize;
			V[] newSet = newSet(newLen);
			int count = 0;

			for (V element : newSetLists[i])
				newSet[count++] = element;
			for (int j = positions[i]; j < oldLen; j++)
				newSet[count++] = oldSet[j];
			sets[i] = newSet;
		}
		return resultList.toArray(newSet(resultList.size()));
	}

	/**
	 * Does the given set contain the given element?
	 * 
	 * @param set
	 *            the set
	 * @param element
	 *            the object
	 * @return
	 */
	public boolean contains(V[] set, V element) {
		return find(set, element) >= 0;
	}
}
