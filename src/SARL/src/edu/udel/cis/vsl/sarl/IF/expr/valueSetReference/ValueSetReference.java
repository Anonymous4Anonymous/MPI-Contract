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

import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;

public interface ValueSetReference extends SymbolicExpression {
	/**
	 * The different kinds of reference set elements.
	 * 
	 * @author xxxx
	 */
	static public enum VSReferenceKind {
	/**
	 * The identity reference. This is the reference that when applied to any
	 * value v, returns a reference to v. A reference of this kind is an
	 * instance of {@link VSIdentityReference}.
	 */
	IDENTITY,
	/**
	 * An array element of reference. A reference of this kind is an instance of
	 * {@link VSArrayElementReference}. It includes a reference to the "parent"
	 * value set and an index.
	 */
	ARRAY_ELEMENT,
	/**
	 * An reference to a "section" of an array. A reference of this kind is an
	 * instance of {@link VSArraySectionReference}. It includes a reference to
	 * the "parent" value set, an inclusive lower index bound and an exclusive
	 * upper index bound of the section.
	 */
	ARRAY_SECTION,
	/**
	 * A tuple component reference. A reference of this kind is an instance of
	 * {@link VSTupleComponentReference}. It includes a reference to the
	 * "parent" value set and a concrete integer field index.
	 */
	TUPLE_COMPONENT,
	/**
	 * A union member reference. A reference of this kind is an instance of
	 * {@link VSUnionMemberReference}. It includes a reference to the "parent"
	 * value set and an integer member index.
	 */
	UNION_MEMBER,
	/**
	 * An offset reference. A reference of this kind is an instance of
	 * {@link VSOffsetReference}. It includes a reference to a "parent" value
	 * set and an integer "offset".
	 */
	OFFSET,
	}

	/**
	 * Gets the kind of this value set reference.
	 * 
	 * @return the kind of this value set reference
	 */
	VSReferenceKind valueSetReferenceKind();

	/**
	 * Is this the identity reference?
	 * 
	 * @return <code>true</code> iff the kind of this reference is
	 *         {@link VSReferenceKind#IDENTITY}.
	 */
	boolean isIdentityReference();

	/**
	 * Is this an array element reference?
	 * 
	 * @return <code>true</code> iff the kind of this reference is
	 *         {@link VSReferenceKind#ARRAY_ELEMENT}.
	 */
	boolean isArrayElementReference();

	/**
	 * Is this an array section reference?
	 * 
	 * @return <code>true</code> iff the kind of this reference is
	 *         {@link VSReferenceKind#ARRAY_SECTION}.
	 */
	boolean isArraySectionReference();

	/**
	 * Is this a tuple component reference?
	 * 
	 * @return <code>true</code> iff the kind of this reference is
	 *         {@link VSReferenceKind#TUPLE_COMPONENT}.
	 */
	boolean isTupleComponentReference();

	/**
	 * Is this a union member reference?
	 * 
	 * @return <code>true</code> iff the kind of this reference is
	 *         {@link VSReferenceKind#UNION_MEMBER}.
	 */
	boolean isUnionMemberReference();

	/**
	 * Is this an offset reference?
	 * 
	 * @return <code>true</code> iff the kind of this reference is
	 *         {@link VSReferenceKind#OFFSET}.
	 */
	boolean isOffsetReference();
}
