package edu.udel.cis.vsl.abc.ast.type.common;

import java.io.PrintStream;
import java.util.Map;

import edu.udel.cis.vsl.abc.ast.type.IF.MemType;
import edu.udel.cis.vsl.abc.ast.type.IF.ObjectType;
import edu.udel.cis.vsl.abc.ast.type.IF.PointerType;
import edu.udel.cis.vsl.abc.ast.type.IF.Type;

public class CommonMemType extends CommonObjectType implements MemType {

	private static int classCode = CommonMemType.class.hashCode();

	private PointerType elementType;

	public CommonMemType(PointerType elementType) {
		super(TypeKind.MEM);
		this.elementType = elementType;
	}

	@Override
	public boolean isComplete() {
		return true;
	}

	@Override
	public boolean isVariablyModified() {
		return false;
	}

	@Override
	public int hashCode() {
		return classCode ^ 65535;
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof CommonMemType)
			return elementType().equals(((CommonMemType) object).elementType());
		return false;
	}

	@Override
	public void print(String prefix, PrintStream out, boolean abbrv) {
		out.print(prefix + "$mem");
	}

	@Override
	public boolean isScalar() {
		return true;
	}

	@Override
	protected boolean similar(Type other, boolean equivalent,
			Map<TypeKey, Type> seen) {
		if (other.kind() == TypeKind.MEM) {
			if (equivalent)
				return equals(other);
			else
				return true;
		}
		return false;
	}

	@Override
	public ObjectType elementType() {
		return elementType;
	}
}
