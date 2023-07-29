package edu.udel.cis.vsl.civl.model.common.type;

import edu.udel.cis.vsl.civl.model.IF.CIVLUnimplementedFeatureException;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLPrimitiveType;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLSetType;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLType;
import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;

public class CommonSetType extends CommonType implements CIVLSetType {

	private CIVLType elementType;

	public CommonSetType(CIVLType elementType) {
		super();
		this.elementType = elementType;
		assert elementType.typeKind() != TypeKind.SET;
	}

	@Override
	public TypeKind typeKind() {
		return TypeKind.SET;
	}

	@Override
	public boolean hasState() {
		return false;
	}

	@Override
	public SymbolicType getDynamicType(SymbolicUniverse universe) {
		throw new CIVLUnimplementedFeatureException(
				"Dynamic type of CIVLSetType");
	}

	@Override
	public CIVLType copyAs(CIVLPrimitiveType type, SymbolicUniverse universe) {
		return type;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj instanceof CIVLSetType) {
			CIVLSetType that = (CIVLSetType) obj;

			return this.elementType().equals(that.elementType());
		}
		return false;
	}

	@Override
	public CIVLType elementType() {
		return elementType;
	}

	@Override
	public boolean isSetTypeOf(CIVLType element) {
		return elementType.equals(element);
	}

	@Override
	public boolean isSetType() {
		return true;
	}

	@Override
	public boolean isSuperTypeOf(CIVLType subtype) {
		return this.elementType.equals(subtype);
	}

	@Override
	public String toString() {
		return "set-of-(" + elementType.toString() + ")";
	}
}
