package edu.udel.cis.vsl.civl.semantics.common;

import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;
import edu.udel.cis.vsl.sarl.IF.expr.ArrayElementReference;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.OffsetReference;
import edu.udel.cis.vsl.sarl.IF.expr.ReferenceExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.TupleComponentReference;
import edu.udel.cis.vsl.sarl.IF.expr.UnionMemberReference;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType.SymbolicTypeKind;

/**
 * <p>
 * This class is responsible for performing a dereference operation. Similar to
 * {@link #universe#dereference(SymbolicExpression, ReferenceExpression)} but
 * the {@link #dereference(SymbolicExpression, ReferenceExpression)} of this
 * class requires that the dereference operation must be valid, i.e. no array
 * out-of-bound error, no offset greater than 0 error, etc.
 * </p>
 * 
 * <p>
 * The result of the dereference operation is a {@link DereferencedResult}.
 * </p>
 * 
 * @author xxxx
 */
public class CIVLDereferenceOperator {

	private SymbolicUniverse universe;

	public CIVLDereferenceOperator(SymbolicUniverse universe) {
		this.universe = universe;
	}

	/**
	 * The result of dereferencing a pointer on a state. The result is either an
	 * error if and only if the the {@link #validCondition} does not hold in
	 * that state, or a {@link #value} of {@link SymbolicExpression} type.
	 * 
	 * @author xxxx
	 */
	public class DereferencedResult {
		/**
		 * The resulting value of the dereference operanion, which is
		 * significant if and only if {@link #validCondition} holds in a
		 * specific state.
		 */
		public SymbolicExpression value;
		/**
		 * The condition that must be satisified otherwise the dereference
		 * operation is never valid.
		 */
		public BooleanExpression validCondition;

		DereferencedResult(SymbolicExpression value,
				BooleanExpression validCondition) {
			this.value = value;
			this.validCondition = validCondition;
		}
	}

	public DereferencedResult dereference(SymbolicExpression value,
			ReferenceExpression reference) {
		switch (reference.referenceKind()) {
			case ARRAY_ELEMENT :
				return dereferenceArrayElementReference(value,
						(ArrayElementReference) reference);
			case IDENTITY :
				return dereferenceIdentityReference(value, reference);
			case OFFSET :
				return dereferenceOffsetReference(value,
						(OffsetReference) reference);
			case TUPLE_COMPONENT :
				return dereferenceTupleComponentReference(value,
						(TupleComponentReference) reference);
			case UNION_MEMBER :
				return dereferenceUnionMemberReference(value,
						(UnionMemberReference) reference);
			case NULL :
			default :
				return new DereferencedResult(universe.nullExpression(),
						universe.falseExpression());
		}
	}

	private DereferencedResult dereferenceArrayElementReference(
			SymbolicExpression value, ArrayElementReference reference) {
		NumericExpression index = reference.getIndex();
		DereferencedResult result = dereference(value, reference.getParent());
		SymbolicExpression array;

		array = result.value;
		if (!array.isNull())
			if (array.type().typeKind() == SymbolicTypeKind.ARRAY) {
				NumericExpression extent = universe.length(array);
				BooleanExpression condition = universe
						.lessThanEquals(universe.zeroInt(), index);

				condition = universe.and(condition,
						universe.lessThan(index, extent));
				result.validCondition = universe.and(condition,
						result.validCondition);
				result.value = universe.arrayRead(array, index);
				return result;
			}
		assert result.validCondition.isFalse();
		return result;
	}

	private DereferencedResult dereferenceOffsetReference(
			SymbolicExpression value, OffsetReference reference) {
		NumericExpression offset = reference.getOffset();
		DereferencedResult result = dereference(value, reference.getParent());

		result.validCondition = universe.and(result.validCondition,
				universe.equals(offset, universe.zeroInt()));
		return result;
	}

	private DereferencedResult dereferenceIdentityReference(
			SymbolicExpression value, ReferenceExpression reference) {
		if (!value.isNull())
			return new DereferencedResult(value, universe.trueExpression());
		return new DereferencedResult(value, universe.falseExpression());
	}

	private DereferencedResult dereferenceTupleComponentReference(
			SymbolicExpression value, TupleComponentReference reference) {
		DereferencedResult result = dereference(value, reference.getParent());
		SymbolicExpression tuple = result.value;

		if (!tuple.isNull())
			if (tuple.type().typeKind() == SymbolicTypeKind.TUPLE) {
				result.value = universe.tupleRead(tuple, reference.getIndex());
				return result;
			}
		assert result.validCondition.isFalse();
		return result;
	}

	private DereferencedResult dereferenceUnionMemberReference(
			SymbolicExpression value, UnionMemberReference reference) {
		DereferencedResult result = dereference(value, reference.getParent());
		SymbolicExpression union = result.value;

		if (!union.isNull())
			if (union.type().typeKind() == SymbolicTypeKind.UNION) {
				result.value = universe.unionExtract(reference.getIndex(),
						union);
				return result;
			}
		assert result.validCondition.isFalse();
		return result;
	}
}
