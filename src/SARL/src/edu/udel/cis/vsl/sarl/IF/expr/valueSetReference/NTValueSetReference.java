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
package edu.udel.cis.vsl.sarl.IF.expr.valueSetReference;

/**
 * A non-trivial {@link ValueSetReference}, i.e., one which is not the identity
 * reference.
 * 
 * @author xxxx
 * 
 */
public interface NTValueSetReference extends ValueSetReference {

	/**
	 * As this is an {@link VSArrayElementReference},
	 * {@link VSArraySectionReference}, {@link VSOffsetReference},
	 * {@link VSTupleComponentReference} or {@link VSUnionMemberReference},
	 * returns the reference to the parent, i.e. the array value(s), tuple
	 * value(s), union value(s) or other {@link ValueSetReference}s, resp.
	 * 
	 * @returns the reference to the parent, i.e. the array value(s), tuple
	 *          value(s), union value(s) or other {@link ValueSetReference}s,
	 *          resp.
	 */
	ValueSetReference getParent();
}
