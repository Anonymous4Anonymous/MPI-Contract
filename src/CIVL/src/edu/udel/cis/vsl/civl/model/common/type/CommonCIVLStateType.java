package edu.udel.cis.vsl.civl.model.common.type;

import java.util.function.Function;

import edu.udel.cis.vsl.civl.model.IF.type.CIVLStateType;
import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.object.IntObject;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicTupleType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicUninterpretedType;

public class CommonCIVLStateType extends CommonPrimitiveType
		implements
			CIVLStateType {

	private SymbolicUninterpretedType stateKeyType;

	private Function<SymbolicExpression, IntObject> selector;

	public CommonCIVLStateType(SymbolicType symbolicType,
			NumericExpression sizeofExpression, BooleanExpression facts) {
		super(PrimitiveTypeKind.STATE, symbolicType, sizeofExpression, facts);
		stateKeyType = (SymbolicUninterpretedType) ((SymbolicTupleType) symbolicType)
				.sequence().getType(0);
		this.selector = ((SymbolicUninterpretedType) stateKeyType)
				.soleSelector();
	}

	@Override
	public SymbolicExpression selectScopeValuesMap(
			SymbolicUniverse universe, SymbolicExpression stateValue) {
		return universe.tupleRead(stateValue, universe.intObject(1));
	}

	@Override
	public int selectStateKey(SymbolicUniverse universe,
			SymbolicExpression stateValue) {
		return selector
				.apply(universe.tupleRead(stateValue, universe.intObject(0)))
				.getInt();
	}

	@Override
	public boolean isNumericType() {
		return false;
	}

	@Override
	public boolean isIntegerType() {
		return false;
	}

	@Override
	public boolean isRealType() {
		return false;
	}

	@Override
	public boolean isPointerType() {
		return false;
	}

	@Override
	public boolean isProcessType() {
		return false;
	}

	@Override
	public boolean isStateType() {
		return true;
	}

	@Override
	public boolean isScopeType() {
		return false;
	}

	@Override
	public boolean isVoidType() {
		return false;
	}

	@Override
	public boolean isHeapType() {
		return false;
	}

	@Override
	public boolean isBundleType() {
		return false;
	}

	@Override
	public boolean isStructType() {
		return false;
	}

	@Override
	public boolean isUnionType() {
		return false;
	}

	@Override
	public boolean isArrayType() {
		return false;
	}

	@Override
	public boolean isIncompleteArrayType() {
		return false;
	}

	@Override
	public boolean isCharType() {
		return false;
	}

	@Override
	public boolean isEnumerationType() {
		return false;
	}

	@Override
	public boolean isBoolType() {
		return false;
	}

	@Override
	public boolean isDomainType() {
		return false;
	}

	@Override
	public String toString() {
		return "$state";
	}

	@Override
	public SymbolicExpression buildStateValue(SymbolicUniverse universe,
			int stateKey, SymbolicExpression scopeValuesToReal) {
		SymbolicExpression stateKeyValue = universe
				.concreteValueOfUninterpretedType(stateKeyType,
						universe.intObject(stateKey));

		return universe.tuple((SymbolicTupleType) getDynamicType(universe),
				new SymbolicExpression[]{stateKeyValue, scopeValuesToReal});
	}
}