/*******************************************************************************
 * 
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
package edu.udel.cis.vsl.sarl.IF.expr.valueSetReference;

import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;

/**
 * A reference to a (set-of) section(s) of a (set-of) array(s). This reference
 * includes a parent reference to a (set-of) array value(s), a lower index bound
 * of the section, a upper index bound of the section and a step of the range of
 * the section.
 * 
 * @author xxxx
 *
 */
public interface VSArraySectionReference extends NTValueSetReference {
	/**
	 * @return the inclusive lower index bound of the array section
	 */
	NumericExpression lowerBound();

	/**
	 * @return the exclusive upper index bound of the array section
	 */
	NumericExpression upperBound();

	/**
	 * @return the step of the range of the array section, by default, it is
	 *         one.
	 */
	NumericExpression step();
}
