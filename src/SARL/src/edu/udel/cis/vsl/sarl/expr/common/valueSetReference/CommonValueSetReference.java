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
package edu.udel.cis.vsl.sarl.expr.common.valueSetReference;

import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.valueSetReference.VSArrayElementReference;
import edu.udel.cis.vsl.sarl.IF.expr.valueSetReference.VSArraySectionReference;
import edu.udel.cis.vsl.sarl.IF.expr.valueSetReference.VSIdentityReference;
import edu.udel.cis.vsl.sarl.IF.expr.valueSetReference.VSOffsetReference;
import edu.udel.cis.vsl.sarl.IF.expr.valueSetReference.VSTupleComponentReference;
import edu.udel.cis.vsl.sarl.IF.expr.valueSetReference.VSUnionMemberReference;
import edu.udel.cis.vsl.sarl.IF.expr.valueSetReference.ValueSetReference;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicSequence;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;
import edu.udel.cis.vsl.sarl.expr.common.HomogeneousExpression;

/**
 * A general implementation of {@link ValueSetReference}.
 * 
 * @author xxxx
 *
 */
public abstract class CommonValueSetReference extends
		HomogeneousExpression<SymbolicObject> implements ValueSetReference {

	/**
	 * Construct an instance of a {@link VSIdentityReference}
	 * 
	 * @param type
	 *            the symbolic type of instances of {@link ValueSetReference}s
	 */
	CommonValueSetReference(SymbolicType type, SymbolicExpression... args) {
		super(SymbolicOperator.TUPLE, type, args);
	}

	/**
	 * Constructs a non-trivial value set reference. The cases are:
	 * <ul>
	 * <li>{@link VSArrayElementReference}: function is the
	 * arrayElementReferenceFunction, parentIndexSequence is sequence of length
	 * 2 in which element 0 is the parent reference (the reference to the array)
	 * and element 1 is the index of the array element, a numeric symbolic
	 * expression of integer type.</li>
	 * <li>{@link VSArraySectionReference}: function is the
	 * arraySectionReferenceFunction, parentIndexSequence is sequence of length
	 * 3 in which element 0 is the parent reference (the reference to the array)
	 * , element 1 is the lower index bound of the array section and element 2
	 * is the upper index bound of the array section, both elements 1 and 2 are
	 * numeric symbolic expressions of integer type</li>
	 * <li>{@link VSTupleComponentReference}: function is the
	 * tupleComponentReferenceFunction, parentIndexSequence is sequence of
	 * length 2 in which element 0 is the parent reference (the reference to the
	 * tuple) and element 1 is the field index, a concrete numeric symbolic
	 * expression of integer type.</li>
	 * <li>{@link VSUnionMemberReference}: function is the
	 * unionMemberReferenceFunction, parentIndexSequence is sequence of length 2
	 * in which element 0 is the parent reference (the reference to the
	 * expression of union type) and element 1 is the member index, a concrete
	 * numeric symbolic expression of intger type.</li>
	 * <li>{@link VSOffsetReference}: just like array element reference, but
	 * function is offsetReferenceFunction</li>
	 * </ul>
	 * 
	 * @param referenceType
	 *            the symbolic reference type
	 * @param function
	 *            one of the uninterpreted functions
	 * @param parentIndexSequence
	 *            sequence of length 2 in which first component is the parent
	 *            reference and second is as specified above
	 */
	CommonValueSetReference(SymbolicType referenceType,
			SymbolicConstant function,
			SymbolicSequence<SymbolicExpression> parentIndexSequence) {
		super(SymbolicOperator.APPLY, referenceType,
				new SymbolicObject[] { function, parentIndexSequence });
	}

	@Override
	public boolean isIdentityReference() {
		return false;
	}

	@Override
	public boolean isArrayElementReference() {
		return false;
	}

	@Override
	public boolean isArraySectionReference() {
		return false;
	}

	@Override
	public boolean isTupleComponentReference() {
		return false;
	}

	@Override
	public boolean isUnionMemberReference() {
		return false;
	}

	@Override
	public boolean isOffsetReference() {
		return false;
	}
}
