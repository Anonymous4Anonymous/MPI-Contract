package edu.udel.cis.vsl.sarl.expr.common.valueSetReference;

import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.valueSetReference.VSArraySectionReference;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicSequence;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;

public class CommonVSArraySectionReference extends CommonNTValueSetReference
		implements VSArraySectionReference {

	/**
	 * Constructor builds an instance of {@link VSArraySectionReference}
	 * 
	 * @param referenceType
	 * @param function
	 * @param parentLoHiStepSequence
	 */
	public CommonVSArraySectionReference(SymbolicType referenceType,
			SymbolicConstant function,
			SymbolicSequence<SymbolicExpression> parentLoHiStepSequence) {
		super(referenceType, function, parentLoHiStepSequence);
	}

	@Override
	public VSReferenceKind valueSetReferenceKind() {
		return VSReferenceKind.ARRAY_SECTION;
	}

	@Override
	public NumericExpression lowerBound() {
		return this.getIndexExpression();
	}

	@Override
	public NumericExpression upperBound() {
		return (NumericExpression) ((SymbolicSequence<?>) this.argument(1))
				.get(2);
	}

	@Override
	public NumericExpression step() {
		return (NumericExpression) ((SymbolicSequence<?>) this.argument(1))
				.get(3);
	}

	@Override
	public boolean isArraySectionReference() {
		return true;
	}
}
