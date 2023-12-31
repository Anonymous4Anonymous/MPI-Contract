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

import java.math.BigInteger;

import edu.udel.cis.vsl.sarl.IF.number.IntegerNumber;
import edu.udel.cis.vsl.sarl.IF.number.Number;
import edu.udel.cis.vsl.sarl.IF.number.RationalNumber;
import edu.udel.cis.vsl.sarl.IF.object.NumberObject;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject;
import edu.udel.cis.vsl.sarl.object.IF.ObjectFactory;

public class CommonNumberObject extends CommonSymbolicObject
		implements NumberObject {

	private Number value;

	CommonNumberObject(Number value) {
		// super(SymbolicObjectKind.NUMBER);
		this.value = value;
	}

	@Override
	public Number getNumber() {
		return value;
	}

	@Override
	public boolean intrinsicEquals(SymbolicObject o) {
		return value.equals(((NumberObject) o).getNumber());
	}

	@Override
	public int computeHashCode() {
		return symbolicObjectKind().hashCode() ^ value.hashCode();
	}

	@Override
	public String toString() {
		return value.toString();
	}

	@Override
	public int signum() {
		return value.signum();
	}

	@Override
	public boolean isZero() {
		return value.isZero();
	}

	@Override
	public boolean isOne() {
		return value.isOne();
	}

	@Override
	public boolean isInteger() {
		return value instanceof IntegerNumber;
	}

	@Override
	public boolean isReal() {
		return value instanceof RationalNumber;
	}

	/**
	 * Does nothing; Basic objects have no children, so there is nothing to do.
	 */
	@Override
	public void canonizeChildren(ObjectFactory factory) {
	}

	@Override
	public int compareTo(NumberObject o) {
		return value.compareTo(o.getNumber());
	}

	@Override
	public StringBuffer toStringBuffer(boolean atomize) {
		// fractions need to be enclosed in parentheses if they
		// are going to be used inside other expressions...
		if (atomize && value instanceof RationalNumber && !BigInteger.ONE
				.equals(((RationalNumber) value).denominator())) {
			StringBuffer buffer = new StringBuffer("(");

			buffer.append(value.toString());
			buffer.append(")");
			return buffer;
		} else {
			return new StringBuffer(value.toString());
		}
	}

	@Override
	public StringBuffer toStringBufferLong() {
		return new StringBuffer(value.toString());
	}

	@Override
	public final SymbolicObjectKind symbolicObjectKind() {
		return SymbolicObjectKind.NUMBER;
	}

	@Override
	public boolean containsQuantifier() {
		return false;
	}

	@Override
	public boolean containsSubobject(SymbolicObject obj) {
		return this == obj;
	}
}
