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

import java.math.BigInteger;

import edu.udel.cis.vsl.sarl.IF.number.IntegerNumber;

/**
 * A representation of the integral infinity, it could be either postive or
 * negative.
 * 
 * @author Wenhao Wu
 *
 */
public class RealIntegerInfinity extends RealInfinity implements IntegerNumber {

	// Constructor...

	/**
	 * Creates an <strong>infinite</strong> integer. <br>
	 * Its signum is determined by the given <code>boolean</code> value.
	 * 
	 * @param isPositive
	 *            If <code>true</code>, this {@link RealIntegerInfinity} is the
	 *            positive infinity, else this {@link RealIntegerInfinity} is
	 *            the negative infinity.
	 */
	RealIntegerInfinity(boolean isPositive) {
		super(isPositive);
	}

	// Override Methods...

	@Override
	public int intValue() {
		return Integer.MAX_VALUE;
	}

	@Override
	public BigInteger bigIntegerValue() {
		return null;
	}

}
