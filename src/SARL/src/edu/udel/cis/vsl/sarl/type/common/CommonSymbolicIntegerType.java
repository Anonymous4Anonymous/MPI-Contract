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

import edu.udel.cis.vsl.sarl.IF.SARLInternalException;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicIntegerType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;
import edu.udel.cis.vsl.sarl.object.IF.ObjectFactory;

/**
 * an implementation of {@link SymbolicIntegerType}
 * 
 * @author mohammedalali
 *
 */
public class CommonSymbolicIntegerType extends CommonSymbolicType
		implements SymbolicIntegerType {

	/**
	 * a constant to store the hashCode of this object, so that it will be
	 * calculated once and saved.
	 */
	private final static int classCode = CommonSymbolicIntegerType.class
			.hashCode();
	/**
	 * the integerKind of this integerType
	 */
	private IntegerKind integerKind;

	/**
	 * stores the short name of this integerType
	 */
	private StringBuffer name;

	/**
	 * a constructor to create a new CommonSymbolicIntegerType
	 * 
	 * @param kind
	 *            the kind of this integerType
	 */
	public CommonSymbolicIntegerType(IntegerKind kind) {
		super(SymbolicTypeKind.INTEGER);
		this.integerKind = kind;
	}

	@Override
	public IntegerKind integerKind() {
		return integerKind;
	}

	@Override
	protected boolean typeEquals(CommonSymbolicType that) {
		return integerKind == ((CommonSymbolicIntegerType) that).integerKind;
	}

	@Override
	protected int computeHashCode() {
		return classCode ^ integerKind.hashCode();
	}

	/**
	 * this method is empty because CommonSymbolicIntegerType doesn't have
	 * children
	 */
	@Override
	public void canonizeChildren(ObjectFactory factory) {
	}

	@Override
	public StringBuffer toStringBuffer(boolean atomize) {
		if (name == null) {
			String shortName;

			switch (integerKind) {
			case IDEAL:
				shortName = "int";
				break;
			case HERBRAND:
				shortName = "hint";
				break;
			case BOUNDED:
				shortName = "bounded";
				break;
			default:
				throw new SARLInternalException("unreachable");
			}
			name = new StringBuffer(shortName);
		}
		return name;
	}

	@Override
	public boolean isHerbrand() {
		return integerKind == IntegerKind.HERBRAND;
	}

	@Override
	public boolean isIdeal() {
		return integerKind == IntegerKind.IDEAL;
	}

	/**
	 * @return the same object because no lengths are used in
	 *         CommonSymbolicIntegerType
	 */
	@Override
	public SymbolicType getPureType() {
		return this;
	}

	@Override
	public boolean containsQuantifier() {
		return false;
	}

	@Override
	public boolean containsSubobject(SymbolicObject obj) {
		return this == obj;
	}

}
