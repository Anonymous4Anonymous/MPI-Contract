package edu.udel.cis.vsl.sarl.expr.common.valueSetReference;

import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.valueSetReference.NTValueSetReference;
import edu.udel.cis.vsl.sarl.IF.expr.valueSetReference.VSArrayElementReference;
import edu.udel.cis.vsl.sarl.IF.expr.valueSetReference.VSArraySectionReference;
import edu.udel.cis.vsl.sarl.IF.expr.valueSetReference.VSOffsetReference;
import edu.udel.cis.vsl.sarl.IF.expr.valueSetReference.VSTupleComponentReference;
import edu.udel.cis.vsl.sarl.IF.expr.valueSetReference.VSUnionMemberReference;
import edu.udel.cis.vsl.sarl.IF.expr.valueSetReference.ValueSetReference;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicSequence;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;

public abstract class CommonNTValueSetReference extends CommonValueSetReference
		implements NTValueSetReference {

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
	 * 4 in which element 0 is the parent reference (the reference to the array)
	 * , element 1 is the lower index bound of the array section, element 2 is
	 * the upper index bound of the array section and element 3 is the step of
	 * the range of the section, all elements 1, 2 and 3 are numeric symbolic
	 * expressions of integer type</li>
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
	CommonNTValueSetReference(SymbolicType referenceType,
			SymbolicConstant function,
			SymbolicSequence<SymbolicExpression> parentIndexSequence) {
		super(referenceType, function, parentIndexSequence);
		assert parentIndexSequence.get(0) instanceof ValueSetReference;
		assert parentIndexSequence.get(1).type().isInteger();
		assert parentIndexSequence.size() <= 2
				|| (parentIndexSequence.get(2).type().isInteger()
						&& parentIndexSequence.get(3).type().isInteger());

	}

	/**
	 * Method that returns parent ValueSetReference.
	 * 
	 * @return ValueSetReference
	 */
	@Override
	public ValueSetReference getParent() {
		return (ValueSetReference) ((SymbolicSequence<?>) this.argument(1))
				.get(0);
	}

	/**
	 * Protected method that returns NumericExpression.
	 * 
	 * @return NumericExpression
	 */
	protected NumericExpression getIndexExpression() {
		return (NumericExpression) ((SymbolicSequence<?>) this.argument(1))
				.get(1);
	}
}
