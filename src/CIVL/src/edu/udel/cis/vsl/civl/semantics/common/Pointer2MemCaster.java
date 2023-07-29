package edu.udel.cis.vsl.civl.semantics.common;

import java.util.Arrays;

import edu.udel.cis.vsl.civl.model.IF.type.CIVLType;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLType.TypeKind;
import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicArrayType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicTupleType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType.SymbolicTypeKind;

//char to int
public class Pointer2MemCaster
		implements
			CIVLUnaryOperator<SymbolicExpression> {
	private SymbolicUniverse universe;

	public Pointer2MemCaster(SymbolicUniverse universe) {
		this.universe = universe;
	}

	@Override
	public SymbolicExpression apply(BooleanExpression context,
			SymbolicExpression value, CIVLType type) {
		assert type.typeKind() == TypeKind.MEM;
		assert type.getDynamicType(universe)
				.typeKind() == SymbolicTypeKind.TUPLE : "unexpected dynmaic type of $mem type";

		SymbolicTupleType castedDyType = (SymbolicTupleType) type
				.getDynamicType(universe);

		// check the dynamic type of $mem type is what this method expects: tupe
		// {.1 = int; .2 = ptr[] }
		assert castedDyType.sequence().getType(0)
				.isInteger() : "unexpected dynmaic type of $mem type";
		assert castedDyType.sequence().getType(1)
				.typeKind() == SymbolicTypeKind.ARRAY : "unexpected dynmaic type of $mem type";

		SymbolicType ptrType = ((SymbolicArrayType) castedDyType.sequence()
				.getType(1)).elementType();
		NumericExpression extent = universe.integer(1);
		SymbolicExpression ptrArray = universe.array(ptrType,
				Arrays.asList(value));

		return universe.tuple(castedDyType, Arrays.asList(extent, ptrArray));
	}
}
