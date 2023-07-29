package edu.udel.cis.vsl.abc.ast.type.common;

import java.io.PrintStream;
import java.util.Map;

import edu.udel.cis.vsl.abc.ast.type.IF.AcslMPIBufType;
import edu.udel.cis.vsl.abc.ast.type.IF.Type;

public class CommonAcslMPIBufType extends CommonObjectType implements AcslMPIBufType {

	public CommonAcslMPIBufType() {
		super(TypeKind.ACSL_MPI_TYPE);
	}

	@Override
	public boolean isScalar() {
		return false;
	}

	@Override
	public void print(String prefix, PrintStream out, boolean abbrv) {
		out.print(prefix + "\\mpi_buf_t");
	}

	@Override
	public boolean isVariablyModified() {
		return false;
	}

	@Override
	protected boolean similar(Type other, boolean equivalent,
			Map<TypeKey, Type> seen) {
		return equivalent ? other instanceof CommonAcslMPIBufType
				: other instanceof AcslMPIBufType;
	}

	@Override
	public AcslMPITypeKind acslTypeKind() {
		return AcslMPITypeKind.ACSL_MPI_BUF_TYPE;
	}

	@Override
	public boolean isComplete() {
		return true;
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof AcslMPIBufType;
	}
	
	@Override
	public int hashCode() {
		return CommonAcslMPIBufType.class.hashCode();
	}
}
