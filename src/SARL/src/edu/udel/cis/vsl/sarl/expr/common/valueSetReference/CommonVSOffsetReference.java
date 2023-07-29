package edu.udel.cis.vsl.sarl.expr.common.valueSetReference;

import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.valueSetReference.VSOffsetReference;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicSequence;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;

public class CommonVSOffsetReference extends CommonNTValueSetReference
		implements VSOffsetReference {

	public CommonVSOffsetReference(SymbolicType referenceType,
			SymbolicConstant function,
			SymbolicSequence<SymbolicExpression> parentIndexSequence) {
		super(referenceType, function, parentIndexSequence);
	}

	@Override
	public VSReferenceKind valueSetReferenceKind() {
		return VSReferenceKind.OFFSET;
	}

	@Override
	public NumericExpression getOffset() {
		return this.getIndexExpression();
	}

	@Override
	public boolean isOffsetReference() {
		return true;
	}
}
