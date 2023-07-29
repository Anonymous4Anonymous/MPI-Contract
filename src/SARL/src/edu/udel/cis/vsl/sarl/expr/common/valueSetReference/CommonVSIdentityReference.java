package edu.udel.cis.vsl.sarl.expr.common.valueSetReference;

import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.valueSetReference.VSIdentityReference;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;

public class CommonVSIdentityReference extends CommonValueSetReference
		implements VSIdentityReference {

	public CommonVSIdentityReference(SymbolicType type,
			SymbolicExpression... args) {
		super(type, args);
	}

	@Override
	public VSReferenceKind valueSetReferenceKind() {
		return VSReferenceKind.IDENTITY;
	}

	@Override
	public boolean isIdentityReference() {
		return true;
	}
}
