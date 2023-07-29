package edu.udel.cis.vsl.sarl.util;

/**
 * Given a partial order lt on T. This means that for all x, y in T:
 * 
 * <pre>
 *   !lt(x,x)
 *   lt(x,y) && lt(y,z) ==> lt(x,z), and
 *   !(lt(x,y) && lt(y,x)).
 * </pre>
 * 
 * Sort a sequence of elements of T so that in the resulting sequence, if x
 * occurs before y, then !lt(y,x). Put another way, if lt(x,y) then x must occur
 * before y.
 * 
 * @author xxxx
 *
 * @param <T>
 */
public class TopologicalSorter<T> {

	BinaryPredicate<T> lt;

	public TopologicalSorter(BinaryPredicate<T> lt) {
		this.lt = lt;
	}

	/**
	 * Bubble-like sort. It is O(n^2). Can't see way around that: in the worst
	 * case lt(x,y) is false for all x,y. Then every element must be compared
	 * against every other element, in both directions, to discover that nothing
	 * needs to be changed.
	 * 
	 * @param an
	 *            array of T containing no {@code null}s
	 */
	public void sort(T[] a) {
		int n = a.length;
		
		// invariant: a[i+1..n-1] is sorted.
		for (int i = n - 2; i >= 0; i--) {
			// insert a[i] into a[i+1..n-1]...
			T x = a[i];
			int pos = i; // position where x should end up;
			
			for (int j = i + 1; j < n; j++) {
				T y = a[j];
				
				if (lt.apply(x, y)) {
					// stop: insert x at position pos
					break;
				} else if (lt.apply(y, x)) {
					// need to insert x after y
					pos = j;
				}
			}
			if (pos > i) {
				// shift everything in i+1..pos down...
				for (int j = i; j < pos; j++)
					a[j] = a[j + 1];
				a[pos] = x;
			}
		}
	}
}
