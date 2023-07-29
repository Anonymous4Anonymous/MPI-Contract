package edu.udel.cis.vsl.sarl.util;

import java.util.Iterator;

public class JointIterator<E> implements Iterator<E> {

	private Iterator<E> iter1;

	private Iterator<E> iter2;

	public JointIterator(Iterator<E> iter1, Iterator<E> iter2) {
		this.iter1 = iter1;
		this.iter2 = iter2;
	}

	@Override
	public boolean hasNext() {
		return iter1.hasNext() || iter2.hasNext();
	}

	@Override
	public E next() {
		return iter1.hasNext() ? iter1.next() : iter2.next();
	}

}
