package edu.udel.cis.vsl.abc.ast.type.common;

import java.io.PrintStream;
import java.util.Map;

import edu.udel.cis.vsl.abc.ast.type.IF.Type;
import edu.udel.cis.vsl.abc.ast.type.IF.UnqualifiedObjectType;

public class CommonStateType extends CommonObjectType implements UnqualifiedObjectType {

	private static int classCode = CommonStateType.class.hashCode();

	public CommonStateType() {
		super(TypeKind.STATE);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof CommonStateType;
	}

	@Override
	public boolean isComplete() {
		return true;
	}

	@Override
	public boolean isScalar() {
		return true;
	}

	@Override
	public void print(String prefix, PrintStream out, boolean abbrv) {
		out.print("$state");
	}

	@Override
	public String toString() {
		return "$state";
	}

	@Override
	public boolean isVariablyModified() {
		return false;
	}

	@Override
	public int hashCode() {
		return classCode;
	}

	@Override
	protected boolean similar(Type other, boolean equivalent, Map<TypeKey, Type> seen) {
		return other instanceof CommonStateType;
	}

}
