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
package edu.udel.cis.vsl.sarl.util;

import java.util.Map.Entry;

/**
 * An ordered pair.
 * 
 * @author xxxx
 *
 * @param <S>
 *            the type of the left component
 * @param <T>
 *            the type of the right component
 */
public class Pair<S, T> implements Entry<S, T> {

	/**
	 * The left component.
	 */
	public S left;

	/**
	 * The right component.
	 */
	public T right;

	/**
	 * Constructs a new ordered pair from given components.
	 * 
	 * @param left
	 *            the left component
	 * @param right
	 *            the right component
	 */
	public Pair(S left, T right) {
		this.left = left;
		this.right = right;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * This hash code is the sum of the hash codes of the two components.
	 */
	public int hashCode() {
		return (left == null ? 0 : left.hashCode())
				+ (right == null ? 0 : right.hashCode());
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Two ordered pairs are equal iff the left components are equal and the
	 * right components are equal.
	 */
	public boolean equals(Object object) {
		if (object instanceof Pair<?, ?>) {
			Pair<?, ?> that = (Pair<?, ?>) object;

			if (left == null) {
				if (that.left != null)
					return false;
			} else if (that.left == null || !left.equals(that.left)) {
				return false;
			}
			if (right == null) {
				if (that.right != null)
					return false;
			} else if (that.right == null || !right.equals(that.right)) {
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @return string representation of this ordered pair using angular
	 *         brackets.
	 */
	@Override
	public String toString() {
		return "<" + left + "," + right + ">";
	}

	@Override
	public S getKey() {
		return left;
	}

	@Override
	public T getValue() {
		return right;
	}

	@Override
	public T setValue(T value) {
		T old = right;

		right = value;
		return old;
	}

}
