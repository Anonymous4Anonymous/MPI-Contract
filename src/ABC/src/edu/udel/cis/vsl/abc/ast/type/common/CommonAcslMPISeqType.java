package edu.udel.cis.vsl.abc.ast.type.common;

import java.io.PrintStream;
import java.util.Map;

import edu.udel.cis.vsl.abc.ast.type.IF.AcslMPISeqType;
import edu.udel.cis.vsl.abc.ast.type.IF.Type;

public class CommonAcslMPISeqType extends CommonObjectType
		implements AcslMPISeqType {

	public CommonAcslMPISeqType() {
		super(TypeKind.ACSL_MPI_TYPE);
	}

	@Override
	public boolean isScalar() {
		return false;
	}

	@Override
	public void print(String prefix, PrintStream out, boolean abbrv) {
		out.print(prefix + "\\mpi_seq_t");
	}

	@Override
	public boolean isVariablyModified() {
		return false;
	}

	@Override
	protected boolean similar(Type other, boolean equivalent,
			Map<TypeKey, Type> seen) {
		return equivalent ? other instanceof CommonAcslMPISeqType
				: other instanceof AcslMPISeqType;
	}

	@Override
	public AcslMPITypeKind acslTypeKind() {
		return AcslMPITypeKind.ACSL_MPI_SEQ_TYPE;
	}

	@Override
	public boolean isComplete() {
		return true;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof AcslMPISeqType;
	}

	@Override
	public int hashCode() {
		return CommonAcslMPISeqType.class.hashCode();
	}
}
