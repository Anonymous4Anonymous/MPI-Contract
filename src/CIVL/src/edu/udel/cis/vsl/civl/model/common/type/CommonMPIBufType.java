package edu.udel.cis.vsl.civl.model.common.type;

import edu.udel.cis.vsl.civl.model.IF.CIVLUnimplementedFeatureException;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLMPIBufType;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLPrimitiveType;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLType;
import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicTupleType;

public class CommonMPIBufType extends CommonType implements CIVLMPIBufType {

	public CommonMPIBufType(SymbolicTupleType dyType) {
		this.dynamicType = dyType;
	}
	
	@Override
	public TypeKind typeKind() {
		return TypeKind.MPI_BUF;
	}

	@Override
	public boolean hasState() {
		return false;
	}

	@Override
	public SymbolicTupleType getDynamicType(SymbolicUniverse universe) {
		return (SymbolicTupleType) this.dynamicType;
	}

	@Override
	public CIVLType copyAs(CIVLPrimitiveType type, SymbolicUniverse universe) {
		throw new CIVLUnimplementedFeatureException(
				"copyAs method for CIVLMPIBufType");
	}
}
