package edu.udel.cis.vsl.sarl.util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;

/**
 * <p>
 * A collection representing the disjoint union of two collections. This
 * collection is immutable: the modification methods will all throw
 * {@link UnsupportedOperationException} s.
 * </p>
 * 
 * <p>
 * This collection is backed by the two underlying collections. I.e., changes to
 * the underlying collections will be reflected in this collection.
 * </p>
 * 
 * @author xxxx
 *
 * @param <E>
 *            the type of the elements of the collections
 */
public class JointCollection<E> implements Collection<E> {

	/**
	 * The first collection
	 */
	protected Collection<E> col1;

	/**
	 * The second collection
	 */
	protected Collection<E> col2;

	/**
	 * Creates new joint collection from two underlying collections.
	 * 
	 * @param col1
	 *            the first collection
	 * @param col2
	 *            the second collection
	 */
	public JointCollection(Collection<E> col1, Collection<E> col2) {
		this.col1 = col1;
		this.col2 = col2;
	}

	@Override
	public int size() {
		return col1.size() + col2.size();
	}

	@Override
	public boolean isEmpty() {
		return col1.isEmpty() && col2.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return col1.contains(o) || col2.contains(o);
	}

	@Override
	public Iterator<E> iterator() {
		return new JointIterator<E>(col1.iterator(), col2.iterator());
	}

	@SuppressWarnings("unchecked")
	private <T> void fillArray(T[] a) {
		int count = 0;
		int length = a.length;

		for (E element : col1) {
			a[count] = (T) element;
			count++;
		}
		for (E element : col2) {
			a[count] = (T) element;
			count++;
		}
		while (count < length) {
			a[count] = null;
			count++;
		}
	}

	@Override
	public Object[] toArray() {
		int size = col1.size() + col2.size();
		Object[] result = new Object[size];

		fillArray(result);
		return result;
	}

	@Override
	public <T> T[] toArray(T[] a) {
		int size = col1.size() + col2.size();
		if (a.length >= size) {
			fillArray(a);
			return a;
		}
		Class<?> aClass = a.getClass();
		Class<?> tClass = aClass.getComponentType();
		@SuppressWarnings("unchecked")
		T[] newArray = (T[]) Array.newInstance(tClass, size);

		fillArray(newArray);
		return newArray;
	}

	@Override
	public boolean add(E e) {
		throw new UnsupportedOperationException(
				"Cannot modify an immutable collection");
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException(
				"Cannot modify an immutable collection");
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object o : c) {
			if (!contains(o))
				return false;
		}
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		throw new UnsupportedOperationException(
				"Cannot modify an immutable collection");
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException(
				"Cannot modify an immutable collection");
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException(
				"Cannot modify an immutable collection");
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException(
				"Cannot modify an immutable collection");
	}
}
