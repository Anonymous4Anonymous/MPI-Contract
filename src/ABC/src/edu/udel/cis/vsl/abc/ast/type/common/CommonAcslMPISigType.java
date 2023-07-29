package edu.udel.cis.vsl.abc.ast.type.common;

import java.io.PrintStream;
import java.util.Map;

import edu.udel.cis.vsl.abc.ast.type.IF.AcslMPISigType;
import edu.udel.cis.vsl.abc.ast.type.IF.Type;

public class CommonAcslMPISigType extends CommonObjectType
		implements AcslMPISigType {

	public CommonAcslMPISigType() {
		super(TypeKind.ACSL_MPI_TYPE);
	}

	@Override
	public boolean isScalar() {
		return false;
	}

	@Override
	public void print(String prefix, PrintStream out, boolean abbrv) {
		out.print(prefix + "\\mpi_sig_t");
	}

	@Override
	public boolean isVariablyModified() {
		return false;
	}

	@Override
	protected boolean similar(Type other, boolean equivalent,
			Map<TypeKey, Type> seen) {
		return equivalent ? other instanceof CommonAcslMPISigType
				: other instanceof AcslMPISigType;
	}

	@Override
	public AcslMPITypeKind acslTypeKind() {
		return AcslMPITypeKind.ACSL_MPI_SIG_TYPE;
	}

	@Override
	public boolean isComplete() {
		return true;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof AcslMPISigType;
	}

	@Override
	public int hashCode() {
		return CommonAcslMPISigType.class.hashCode();
	}
}
