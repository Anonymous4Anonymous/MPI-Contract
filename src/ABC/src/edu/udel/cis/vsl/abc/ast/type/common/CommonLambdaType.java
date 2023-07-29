package edu.udel.cis.vsl.abc.ast.type.common;

import java.io.PrintStream;
import java.util.Map;

import edu.udel.cis.vsl.abc.ast.type.IF.LambdaType;
import edu.udel.cis.vsl.abc.ast.type.IF.Type;
import edu.udel.cis.vsl.abc.ast.type.IF.UnqualifiedObjectType;

public class CommonLambdaType extends CommonObjectType implements LambdaType {
	private Type freeVariableType;

	private Type lambdaFunctionType;

	public CommonLambdaType(Type freeVaraibleType, Type lambdaFunctionType) {
		super(TypeKind.LAMBDA);
		this.freeVariableType = freeVaraibleType;
		this.lambdaFunctionType = lambdaFunctionType;
	}

	@Override
	public boolean isComplete() {
		return true;
	}

	@Override
	public boolean isScalar() {
		return false;
	}

	@Override
	public void print(String prefix, PrintStream out, boolean abbrv) {
		out.print(prefix + "$lambda(");
		freeVariableType.print("", out, abbrv);
		out.print(":");
		lambdaFunctionType.print("", out, abbrv);
		out.print(")");
	}

	@Override
	public UnqualifiedObjectType freeVariableType() {
		return (UnqualifiedObjectType) freeVariableType;
	}

	@Override
	public UnqualifiedObjectType lambdaFunctionReturnType() {
		return (UnqualifiedObjectType) lambdaFunctionType;
	}

	@Override
	public boolean isVariablyModified() {
		return false;
	}

	@Override
	protected boolean similar(Type other, boolean equivalent,
			Map<TypeKey, Type> seen) {
		if (other instanceof LambdaType) {
			LambdaType otherType = (LambdaType) other;

			if (equivalent) {
				return freeVariableType
						.equivalentTo(otherType.freeVariableType())
						&& lambdaFunctionType.equivalentTo(
								otherType.lambdaFunctionReturnType());
			} else {
				return freeVariableType()
						.compatibleWith(otherType.freeVariableType())
						&& lambdaFunctionReturnType().compatibleWith(
								otherType.lambdaFunctionReturnType());
			}
		}
		return false;
	}
}
