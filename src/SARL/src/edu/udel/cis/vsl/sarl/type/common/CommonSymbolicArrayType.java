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
package edu.udel.cis.vsl.sarl.type.common;

import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicArrayType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;
import edu.udel.cis.vsl.sarl.object.IF.ObjectFactory;

public class CommonSymbolicArrayType extends CommonSymbolicType
		implements SymbolicArrayType {

	/**
	 * a constant to store the hashCode of this object, so that it will be
	 * calculated once and saved.
	 */
	private final static int classCode = CommonSymbolicArrayType.class
			.hashCode();

	private SymbolicType elementType;

	/**
	 * Cache of the "pure" version of this type: the version that is recursively
	 * incomplete.
	 */
	private SymbolicArrayType pureType = null;

	/**
	 * Creates new symbolic array type with given elementType. *
	 * 
	 * @param elementType
	 *            any non-null type
	 */
	CommonSymbolicArrayType(SymbolicType elementType) {
		super(SymbolicTypeKind.ARRAY);
		assert elementType != null;
		this.elementType = elementType;
	}

	/**
	 * Both this and that have kind ARRAY. However, neither, either or both may
	 * be complete.
	 */
	@Override
	protected boolean typeEquals(CommonSymbolicType that) {
		if (!elementType.equals(((CommonSymbolicArrayType) that).elementType))
			return false;
		if (isComplete()) {
			if (((CommonSymbolicArrayType) that).isComplete()) {
				return ((CommonSymbolicCompleteArrayType) this).extent().equals(
						((CommonSymbolicCompleteArrayType) that).extent());
			}
			return false;
		} else {
			return !((CommonSymbolicArrayType) that).isComplete();
		}
	}

	@Override
	protected int computeHashCode() {
		return classCode ^ elementType.hashCode();
	}

	@Override
	public SymbolicType elementType() {
		return elementType;
	}

	/**
	 * Used by toString() method. Complete array type subclass can override this
	 * by putting the extent in the brackets.
	 * 
	 * @return "[]"
	 */
	public String extentString() {
		return "[]";
	}

	/**
	 * Nice human-readable representation of the array type. Example
	 * "int[2][][4]". elementType: "int[][4]". extent: 2.
	 */
	@Override
	public StringBuffer toStringBuffer(boolean atomize) {
		StringBuffer result = new StringBuffer();
		SymbolicType type;

		for (type = this; type instanceof CommonSymbolicArrayType; type = ((CommonSymbolicArrayType) type)
				.elementType())
			result.append(((CommonSymbolicArrayType) type).extentString());
		result.insert(0, type.toStringBuffer(false));
		return result;
	}

	@Override
	public boolean isComplete() {
		return false;
	}

	@Override
	public void canonizeChildren(ObjectFactory factory) {
		if (!elementType.isCanonic())
			elementType = factory.canonic(elementType);
		if (pureType != null && !pureType.isCanonic())
			pureType = factory.canonic(pureType);
	}

	public SymbolicArrayType getPureType() {
		return pureType;
	}

	/**
	 * setting a new pureType to this ArrayType
	 * 
	 * @param pureType
	 */
	public void setPureType(SymbolicArrayType pureType) {
		this.pureType = pureType;
	}

	@Override
	public SymbolicType baseType() {
		if (elementType.typeKind() == SymbolicTypeKind.ARRAY)
			return ((SymbolicArrayType) elementType).baseType();
		else
			return elementType;
	}

	@Override
	public int dimensions() {
		int dimensions = 1;
		SymbolicType element = elementType;

		while (element.typeKind() == SymbolicTypeKind.ARRAY) {
			dimensions++;
			element = ((SymbolicArrayType) element).elementType();
		}
		return dimensions;
	}

	@Override
	public boolean containsQuantifier() {
		return false;
	}

	@Override
	public boolean containsSubobject(SymbolicObject obj) {
		return this == obj || elementType.containsSubobject(obj);
	}
}
