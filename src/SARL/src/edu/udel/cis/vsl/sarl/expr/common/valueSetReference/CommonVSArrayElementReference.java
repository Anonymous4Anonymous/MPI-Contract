package edu.udel.cis.vsl.sarl.expr.common.valueSetReference;

import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.valueSetReference.VSArrayElementReference;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicSequence;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;

public class CommonVSArrayElementReference extends CommonNTValueSetReference
		implements VSArrayElementReference {

	/**
	 * Constructor that builds a CommonVSArrayElementReference.
	 * 
	 * @param referenceType
	 * @param arrayElementReferenceFunction
	 * @param parentIndexSequence
	 * 
	 * @return CommonArrayElementReference
	 */
	public CommonVSArrayElementReference(SymbolicType referenceType,
			SymbolicConstant arrayElementReferenceFunction,
			SymbolicSequence<SymbolicExpression> parentIndexSequence) {
		super(referenceType, arrayElementReferenceFunction,
				parentIndexSequence);
	}

	@Override
	public VSReferenceKind valueSetReferenceKind() {
		return VSReferenceKind.ARRAY_ELEMENT;
	}

	@Override
	public NumericExpression getIndex() {
		return getIndexExpression();
	}

	@Override
	public boolean isArrayElementReference() {
		return true;
	}
}
