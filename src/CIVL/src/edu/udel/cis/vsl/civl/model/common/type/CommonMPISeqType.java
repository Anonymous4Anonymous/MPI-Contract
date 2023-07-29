package edu.udel.cis.vsl.civl.model.common.type;

import edu.udel.cis.vsl.civl.model.IF.CIVLUnimplementedFeatureException;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLMPISeqType;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLPrimitiveType;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLType;
import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;

public class CommonMPISeqType extends CommonType implements CIVLMPISeqType {

	public CommonMPISeqType() {
	}

	@Override
	public TypeKind typeKind() {
		return TypeKind.MPI_SEQ;
	}

	@Override
	public boolean hasState() {
		return false;
	}

	@Override
	public SymbolicType getDynamicType(SymbolicUniverse universe) {
		return universe.symbolicUninterpretedType("\\mpi_seq");
	}

	@Override
	public CIVLType copyAs(CIVLPrimitiveType type, SymbolicUniverse universe) {
		throw new CIVLUnimplementedFeatureException(
				"copyAs method of CommonMPISeqType");
	}
}
