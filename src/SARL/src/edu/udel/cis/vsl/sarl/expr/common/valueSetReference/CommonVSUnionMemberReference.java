package edu.udel.cis.vsl.sarl.expr.common.valueSetReference;

import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.valueSetReference.VSUnionMemberReference;
import edu.udel.cis.vsl.sarl.IF.number.IntegerNumber;
import edu.udel.cis.vsl.sarl.IF.object.IntObject;
import edu.udel.cis.vsl.sarl.IF.object.NumberObject;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicSequence;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;

public class CommonVSUnionMemberReference extends CommonNTValueSetReference
		implements VSUnionMemberReference {

	private IntObject memberIndex;

	public CommonVSUnionMemberReference(SymbolicType referenceType,
			SymbolicConstant function,
			SymbolicSequence<SymbolicExpression> parentIndexSequence,
			IntObject memberIndex) {
		super(referenceType, function, parentIndexSequence);
		SymbolicObject index = parentIndexSequence.get(1).argument(0);

		assert index.symbolicObjectKind() == SymbolicObjectKind.NUMBER
				&& ((IntegerNumber) ((NumberObject) index).getNumber())
						.intValue() == memberIndex.getInt();
		this.memberIndex = memberIndex;
	}

	@Override
	public VSReferenceKind valueSetReferenceKind() {
		return VSReferenceKind.UNION_MEMBER;
	}

	@Override
	public IntObject getIndex() {
		return memberIndex;
	}

	@Override
	public boolean isUnionMemberReference() {
		return true;
	}
}
