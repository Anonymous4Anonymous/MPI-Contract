package edu.udel.cis.vsl.abc.ast.type.common;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Map;

import edu.udel.cis.vsl.abc.ast.type.IF.TupleType;
import edu.udel.cis.vsl.abc.ast.type.IF.Type;

public class CommonTupleType extends CommonType implements TupleType {

	private Type[] componentTypes;

	public CommonTupleType(TypeKind kind, int numComponentTypes, Iterable<Type> componentTypes) {
		super(kind);
		this.componentTypes = new Type[numComponentTypes];

		int i = 0;

		for (Type componentType : componentTypes)
			this.componentTypes[i++] = componentType;

	}

	@Override
	public boolean isScalar() {
		return false;
	}

	@Override
	public void print(String prefix, PrintStream out, boolean abbrv) {
		out.append(prefix + "(");
		for (int i = 0; i < componentTypes.length - 1; i++)
			out.append(componentTypes[i] + ", ");
		out.append(componentTypes[componentTypes.length - 1] + ")");
	}

	@Override
	public boolean isVariablyModified() {
		return false;
	}

	@Override
	protected boolean similar(Type other, boolean equivalent,
			Map<TypeKey, Type> seen) {
		if (other instanceof TupleType) {
			TupleType otherTuple = (TupleType) other;
			int numComps = numComponentTypes();

			if (numComps != otherTuple.numComponentTypes())
				return false;

			for (int i = 0; i < numComps; i++)
				if (!((CommonType) getComponentType(i)).similar(
						otherTuple.getComponentType(i), equivalent, seen))
					return false;
			return true;
		}
		return false;
	}

	@Override
	public int numComponentTypes() {
		return componentTypes.length;
	}

	@Override
	public Type getComponentType(int idx) {
		return componentTypes[idx];
	}

	@Override
	public Iterable<Type> getComponentTypes() {
		return Arrays.asList(componentTypes);
	}
	
	@Override
	public String toString() {
		return "tuple" + Arrays.toString(componentTypes).replace('[', '(')
				.replace(']', ')');
	}
}
