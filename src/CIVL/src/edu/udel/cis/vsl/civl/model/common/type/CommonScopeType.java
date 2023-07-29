package edu.udel.cis.vsl.civl.model.common.type;

import java.util.function.Function;

import edu.udel.cis.vsl.civl.model.IF.type.CIVLScopeType;
import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.number.IntegerNumber;
import edu.udel.cis.vsl.sarl.IF.object.IntObject;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicUninterpretedType;

/**
 * An implementation of {@link CIVLScopeType}.
 * 
 * @author xxxx
 *
 */
public class CommonScopeType extends CommonPrimitiveType
		implements
			CIVLScopeType {

	/**
	 * An operator converts scope values to integral identities.
	 * 
	 * @author xxxx
	 */
	public class ScopeValueToIdentity
			implements
				Function<SymbolicExpression, IntegerNumber> {

		private Function<SymbolicExpression, IntObject> selector;

		private SymbolicUniverse universe;

		private ScopeValueToIdentity(SymbolicUniverse universe,
				Function<SymbolicExpression, IntObject> selector) {
			this.selector = selector;
			this.universe = universe;
		}

		@Override
		public IntegerNumber apply(SymbolicExpression t) {
			return universe.numberFactory().integer(selector.apply(t).getInt());
		}
	}

	/**
	 * The sole instance of {@link ScopeValueToIdentity}
	 */
	private ScopeValueToIdentity scopeValue2IdentityOperator = null;

	/**
	 * An operator converts integral identities to scope values.
	 * 
	 * @author xxxx
	 */
	public class ScopeIdentityToValue
			implements
				Function<Integer, SymbolicExpression> {
		private SymbolicUniverse universe;

		private ScopeIdentityToValue(SymbolicUniverse universe) {
			this.universe = universe;
		}

		@Override
		public SymbolicExpression apply(Integer t) {
			return universe.concreteValueOfUninterpretedType(
					(SymbolicUninterpretedType) getDynamicType(universe),
					universe.intObject(t));
		}
	}

	/**
	 * The sole instance of {@link ScopeIdentityToValue}
	 */
	private ScopeIdentityToValue scopeIdentity2ValueOperator = null;

	/**
	 * The name of the dynamic scope type.
	 */
	private static final String DYNAMIC_SCOPE_TYPE_NAME = "scope";

	public CommonScopeType(NumericExpression sizeofExpression,
			BooleanExpression facts) {
		super(PrimitiveTypeKind.SCOPE, null, sizeofExpression, facts);
	}

	@Override
	public TypeKind typeKind() {
		return TypeKind.PRIMITIVE;
	}

	@Override
	public SymbolicType getDynamicType(SymbolicUniverse universe) {
		if (dynamicType == null)
			dynamicType = universe
					.symbolicUninterpretedType(DYNAMIC_SCOPE_TYPE_NAME);
		return dynamicType;
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
		return false;
	}

	@Override
	public boolean isScopeType() {
		return true;
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
		return "$scope";
	}

	@Override
	public Function<SymbolicExpression, IntegerNumber> scopeValueToIdentityOperator(
			SymbolicUniverse universe) {
		if (this.scopeValue2IdentityOperator == null) {
			SymbolicUninterpretedType unintType = (SymbolicUninterpretedType) getDynamicType(
					universe);

			this.scopeValue2IdentityOperator = new ScopeValueToIdentity(
					universe, unintType.soleSelector());
		}
		return this.scopeValue2IdentityOperator;
	}

	@Override
	public Function<Integer, SymbolicExpression> scopeIdentityToValueOperator(
			SymbolicUniverse universe) {
		if (this.scopeIdentity2ValueOperator == null)
			this.scopeIdentity2ValueOperator = new ScopeIdentityToValue(
					universe);
		return this.scopeIdentity2ValueOperator;
	}
}
