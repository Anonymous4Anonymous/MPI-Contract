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
package edu.udel.cis.vsl.sarl.IF.expr;

import edu.udel.cis.vsl.sarl.IF.object.StringObject;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;

/**
 * <p>
 * A "symbolic constant" is a symbol used in symbolic execution to represent an
 * input value. It is "constant" in the sense that its value does not change in
 * the course of an execution of the program. A symbolic constant is determined
 * by two things: a name (which is a <code>String</code>), and a type (instance
 * of {@link SymbolicType}). Two distinct symbolic constants may have the same
 * name, but different types. Two symbolic constants are considered equal iff
 * their names are equal and their types are equal.
 * </p>
 * 
 * <p>
 * Symbolic constants are {@link SymbolicExpression}s, which are
 * {@link SymbolicObject}s, and like all symbolic objects are immutable.
 * </p>
 * 
 * @author xxxx
 */
public interface SymbolicConstant extends SymbolicExpression {

	/**
	 * Returns the name of this symbolic constant. This is a
	 * {@link StringObject}, which wraps a <code>String</code> in a
	 * {@link SymbolicObject} package.
	 * 
	 * @return the name of this symbolic constant
	 */
	StringObject name();

	/**
	 * Returns the type of this symbolic constant.
	 * 
	 * @return the type of this symbolic constant
	 */
	SymbolicType type();
}
