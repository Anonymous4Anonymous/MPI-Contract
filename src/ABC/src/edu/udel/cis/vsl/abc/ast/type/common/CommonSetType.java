package edu.udel.cis.vsl.abc.ast.type.common;

import java.io.PrintStream;
import java.util.Map;

import edu.udel.cis.vsl.abc.ast.type.IF.SetType;
import edu.udel.cis.vsl.abc.ast.type.IF.Type;

public class CommonSetType extends CommonType implements SetType {

	Type elementType = null;

	public CommonSetType(Type elementType) {
		super(TypeKind.SET);
		this.elementType = elementType;
	}

	@Override
	public boolean isScalar() {
		// set type is not scalar type:
		return false;
	}

	@Override
	public void print(String prefix, PrintStream out, boolean abbrv) {
		out.print(prefix + "SET[");
		elementType.print("", out, abbrv);
		out.print("]");
	}

	@Override
	public boolean isVariablyModified() {
		return false;
	}

	@Override
	protected boolean similar(Type other, boolean equivalent,
			Map<TypeKey, Type> seen) {
		if (equivalent)
			equals(other);
		else if (other.kind() == TypeKind.SET)
			return ((CommonType) elementType())
					.similar(((SetType) other).elementType(), equivalent, seen);
		return false;
	}

	@Override
	public Type elementType() {
		return elementType;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof SetType) {
			return ((SetType) other).elementType().equals(elementType());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getClass().hashCode() ^ elementType().hashCode();
	}
}
