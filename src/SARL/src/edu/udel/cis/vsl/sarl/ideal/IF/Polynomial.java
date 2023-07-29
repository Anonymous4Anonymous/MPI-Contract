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
package edu.udel.cis.vsl.sarl.ideal.IF;

import edu.udel.cis.vsl.sarl.IF.number.IntegerNumber;
import edu.udel.cis.vsl.sarl.IF.number.NumberFactory;

/**
 * A polynomial: an expression which is the sum of monomials. Use method
 * {@link #termMap(IdealFactory)} to get the term map for this
 * {@link Polynomial}. There must be at least two (non-0) {@link Monomial}s in
 * the term map.
 * 
 * A {@link Polynomial} is also a {@link Primitive}, so can be used as a factor
 * in a {@link Monic}.
 * 
 * @author xxxx
 */
public interface Polynomial extends Primitive {

	/**
	 * The constant term of this polynomial, which may be 0.
	 * 
	 * @param factory
	 *            the ideal factory owning this polynomial
	 * 
	 * @return the constant term of this polynomial
	 */
	Constant constantTerm(IdealFactory factory);

	/**
	 * The "polynomial degree" is the maximum monomial degree of the terms
	 * comprising this polynomial. Note that since this {@link Polynomial} is a
	 * {@link Primitive}, its "monomial degree" is 1.
	 * 
	 * @param factory
	 *            the {@link NumberFactory} used for this operation
	 * @return The {@link IntegerNumber} represents the degree of this
	 *         polynomial, i.e., the maximum degree of its terms
	 */
	IntegerNumber polynomialDegree(NumberFactory factory);

	/**
	 * Does this polynomial contain a term with a nontrivial factorization? This
	 * is stronger than asking if the polynomial has a nontrivial factorization.
	 * A polynomial with more than one term always has a nontrivial
	 * factorization.
	 * 
	 * @param factory
	 *            the ideal factory owning this polynomial
	 * @return <code>true</code> if at least one term has a nontrivial expansion
	 * 
	 * @see #hasNontrivialExpansion(IdealFactory)
	 */
	boolean hasTermWithNontrivialExpansion(IdealFactory factory);
}
