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
package edu.udel.cis.vsl.sarl.object.common;

import edu.udel.cis.vsl.sarl.IF.number.RationalNumber;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject;
import edu.udel.cis.vsl.sarl.object.IF.ObjectFactory;

/**
 * A partial implementation of {@link SymbolicObject}.
 * 
 * @author xxxx
 */
public abstract class CommonSymbolicObject implements SymbolicObject {

	// static fields ...

	/**
	 * Used as an ID number, indicates this is a new object that has not even
	 * been hashed yet.
	 */
	private final static int NOT_HASHED = -3;

	/**
	 * Used as an ID number, indicates this object has been hashed (and its hash
	 * code cached in variable {@link #hashCode}, but not yet canonicalized.
	 */
	private final static int HASHED = -2;

	/**
	 * Used as an ID number, indicates that this object is in the midst of being
	 * canonicalized. Needed to avoid infinite recursion when canonicalizing
	 * children.
	 */
	private final static int IN_CANONIC = -1;

	/**
	 * If true, more detailed string representations of symbolic objects will be
	 * returned by the {@link #toString()} method.
	 */
	private final static boolean debug = false;

	// instance fields ...

	/**
	 * Cached hashCode, set upon first run of {@link #hashCode()}.
	 */
	private int hashCode;

	/**
	 * 
	 * The unique nonnegative canonic ID number OR a negative integer indicating
	 * the current status of a non-canonic object.
	 * 
	 * <p>
	 * If this object has not been hashed, {@link #id} will be
	 * {@link #NOT_HASHED}. If this object has been hashed, but is not canonic,
	 * {@link #id} will be {@link #HASHED}. If this object is canonic (which
	 * implies it has been hashed), {@link #id} will be nonnegative and will be
	 * the unique ID number among canonic objects. This means this object is the
	 * unique representative of its equivalence class.
	 * </p>
	 */
	private int id = NOT_HASHED;

	/**
	 * This number is typically used to place a total order on certain canonic
	 * objects, e.g., to "cache" comparisons. This class simply provides a
	 * getter and setter for this field; it is up to clients on how to use it.
	 */
	private RationalNumber order;

	// Constructors...

	/**
	 * Instantiates this symbolic object, with {@link #id} initialized to
	 * {@link #NOT_HASHED}.
	 */
	protected CommonSymbolicObject() {
	}

	// Abstract Methods...

	/**
	 * Is the given symbolic object equal to this one---assuming the given
	 * symbolic object is of the same kind as this one? Must be defined in any
	 * concrete subclass.
	 * 
	 * @param that
	 *            a symbolic object of the same kind as this one
	 * @return true iff they define the same type
	 */
	protected abstract boolean intrinsicEquals(SymbolicObject o);

	/**
	 * Canonizes the children of this symbolic object. Replaces each child with
	 * the canonic version of that child.
	 * 
	 * @param factory
	 *            the object factory that is responsible for this symbolic
	 *            object
	 */
	protected abstract void canonizeChildren(ObjectFactory factory);

	/**
	 * Computes the hash code to be returned by hashCode(). This is run the
	 * first time hashCode is run. The hash is cached for future calls to
	 * hashCode();
	 * 
	 * @return hash code
	 */
	protected abstract int computeHashCode();

	// private concrete methods...

	/**
	 * Has this object been hashed (and therefore had its hash code cached in
	 * {@link #hashCode}).
	 * 
	 * @return <code>true</code> iff this has been hashed
	 */
	private boolean hashed() {
		return id >= HASHED;
	}

	/**
	 * Sets the {@link #id} to {@link #HASHED} to indicate that this object has
	 * been hashed.
	 */
	private void setHashed() {
		id = HASHED;
	}

	// package-private concrete methods...

	/**
	 * Sets the id number of this object.
	 * 
	 * @param id
	 *            the new ID number; should be nonnegative
	 */
	void setId(int id) {
		this.id = id;
	}

	// protected concrete methods...

	/**
	 * Places parentheses around the string buffer.
	 * 
	 * @param buffer
	 *            a string buffer
	 */
	protected void atomize(StringBuffer buffer) {
		buffer.insert(0, '(');
		buffer.append(')');
	}

	// public methods: SymbolicObject

	@Override
	public boolean isCanonic() {
		return id >= IN_CANONIC;
	}

	@Override
	public void setInCanonic() {
		this.id = IN_CANONIC;
	}

	@Override
	public int id() {
		return id;
	}

	@Override
	public void setOrder(RationalNumber r) {
		this.order = r;
	}

	@Override
	public RationalNumber getOrder() {
		return order;
	}

	// public methods: Object

	@Override
	public int hashCode() {
		if (!hashed()) {
			hashCode = computeHashCode();
			setHashed();
		}
		return hashCode;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o instanceof CommonSymbolicObject) {
			CommonSymbolicObject that = (CommonSymbolicObject) o;

			if (id >= 0 && that.id >= 0) // both are canonic reps
				return false;
			if (hashCode() != that.hashCode())
				return false;
			if (this.symbolicObjectKind() != that.symbolicObjectKind())
				return false;
			return intrinsicEquals(that);
		}
		return false;
	}

	@Override
	public String toString() {
		if (debug)
			return toStringBufferLong().toString();
		else
			return toStringBuffer(false).toString();
	}

}
