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
package edu.udel.cis.vsl.sarl.number.real;

import edu.udel.cis.vsl.sarl.IF.number.IntegerNumber;
import edu.udel.cis.vsl.sarl.IF.number.Number;

/**
 * An abstract class represents the infinite real number. <br>
 * According to the data-type (integer/rational), it has two derived sub-class:
 * <br>
 * {@link RealIntegerInfinity} and {@link RealRationalInfinity}
 * 
 * @author Wenhao Wu
 */
public abstract class RealInfinity extends RealNumber {

	/**
	 * The boolean representing whether this infinite number is positive or not.
	 * <br>
	 */
	private int signum = 1;

	/**
	 * Creates an <strong>infinite</strong> number. <br>
	 * Its signum is determined by the given boolean value
	 * <code>isPositive</code> .
	 * 
	 * @param isPositive
	 *            If <code>true</code>, <code>this</code> is the positive
	 *            infinity, else <code>this</code> is the negative infinity.
	 */
	RealInfinity(boolean isPositive) {
		signum = isPositive ? 1 : -1;
	}

	@Override
	public int signum() {
		return signum;
	}

	@Override
	public boolean isZero() {
		return false;
	}

	@Override
	public boolean isOne() {
		return false;
	}

	@Override
	public boolean isInfinite() {
		return true;
	}

	@Override
	public String toString() {
		return signum > 0 ? "+oo" : "-oo";
	}

	@Override
	public String atomString() {
		return signum > 0 ? "(+oo)" : "(-oo)";
	}

	@Override
	public int numericalCompareTo(Number other) {
		assert this instanceof IntegerNumber == other instanceof IntegerNumber;
		if (other.isInfinite())
			return signum - other.signum();
		else
			return signum();
	}

}
