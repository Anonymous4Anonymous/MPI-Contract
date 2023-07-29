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

import edu.udel.cis.vsl.sarl.IF.object.IntObject;

/**
 * A reference to a (set-of) members of a (set-of) union value(s). This
 * reference includes a parent reference to a (set-of) union values and a
 * concrete index.
 * 
 * @author xxxx
 *
 */
public interface VSUnionMemberReference extends NTValueSetReference {

	/**
	 * Gets the member index.
	 * 
	 * @return the member index
	 */
	IntObject getIndex();
}
