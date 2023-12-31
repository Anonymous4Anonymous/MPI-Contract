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
package edu.udel.cis.vsl.sarl.type.IF;

import edu.udel.cis.vsl.sarl.object.IF.ObjectFactory;
import edu.udel.cis.vsl.sarl.type.common.CommonSymbolicTypeFactory;

/**
 * This class provides a static method for the creation of a new
 * {@link SymbolicTypeFactory}.
 * 
 * @author xxxx
 *
 */
public class Types {

	public static SymbolicTypeFactory newTypeFactory(
			ObjectFactory objectFactory) {
		return new CommonSymbolicTypeFactory(objectFactory);
	}

	// public static Comparator<SymbolicType> newTypeComparator() {
	// Comparator<SymbolicType> result = new TypeComparator();
	// Comparator<SymbolicTypeSequence>
	// }
}
