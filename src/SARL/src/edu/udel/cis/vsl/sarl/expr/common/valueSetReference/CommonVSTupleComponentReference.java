package edu.udel.cis.vsl.sarl.expr.common.valueSetReference;

import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.valueSetReference.VSTupleComponentReference;
import edu.udel.cis.vsl.sarl.IF.number.IntegerNumber;
import edu.udel.cis.vsl.sarl.IF.object.IntObject;
import edu.udel.cis.vsl.sarl.IF.object.NumberObject;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicSequence;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;

public class CommonVSTupleComponentReference extends CommonNTValueSetReference
		implements VSTupleComponentReference {

	private IntObject fieldIndex;

	public CommonVSTupleComponentReference(SymbolicType referenceType,
			SymbolicConstant function,
			SymbolicSequence<SymbolicExpression> parentIndexSequence,
			IntObject fieldIndex) {
		super(referenceType, function, parentIndexSequence);
		SymbolicObject index = parentIndexSequence.get(1).argument(0);

		assert index.symbolicObjectKind() == SymbolicObjectKind.NUMBER
				&& ((IntegerNumber) ((NumberObject) index).getNumber())
						.intValue() == fieldIndex.getInt();
		this.fieldIndex = fieldIndex;
	}

	@Override
	public VSReferenceKind valueSetReferenceKind() {
		return VSReferenceKind.TUPLE_COMPONENT;
	}

	@Override
	public boolean isTupleComponentReference() {
		return true;
	}

	@Override
	public IntObject getIndex() {
		return fieldIndex;
	}
}
