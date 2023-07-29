package edu.udel.cis.vsl.civl.model.common.type;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import edu.udel.cis.vsl.civl.model.IF.type.CIVLMemType;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLPointerType;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLPrimitiveType;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLType;
import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;
import edu.udel.cis.vsl.sarl.IF.UnaryOperator;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.IF.number.IntegerNumber;
import edu.udel.cis.vsl.sarl.IF.object.NumberObject;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicArrayType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicTupleType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;

/**
 * <p>
 * Implementation of {@link CIVLMemType}. The dynamic type of a CIVLMemType is
 * <code>
 * Tuple {ARRAY{
 *                 Tuple{Integer, Integer, Integer, 
 *                       ScopeValue, 
 *                       ValueSetTemplate}
 *                )
 *       }
 * </code>
 * </p>
 * 
 * @author xxxx
 *
 */
public class CommonMemType extends CommonType implements CIVLMemType {

	/**
	 * A reference to its sub-type:
	 */
	private CIVLPointerType pointerType;

	public CommonMemType(CIVLPointerType pointerType,
			SymbolicType dynamicType) {
		super();
		this.dynamicType = dynamicType;
		this.pointerType = pointerType;
	}

	@Override
	public TypeKind typeKind() {
		return TypeKind.MEM;
	}

	@Override
	public boolean hasState() {
		return false;
	}

	@Override
	public boolean isSetType() {
		return true;
	}

	@Override
	public SymbolicType getDynamicType(SymbolicUniverse universe) {
		return dynamicType;
	}

	@Override
	public CIVLType copyAs(CIVLPrimitiveType type, SymbolicUniverse universe) {
		return type;
	}

	@Override
	public CIVLType elementType() {
		return pointerType;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj instanceof CIVLMemType) {
			CIVLMemType that = (CIVLMemType) obj;

			return this.elementType().equals(that.elementType());
		}
		return false;
	}

	@Override
	public String toString() {
		return "$mem";
	}

	@Override
	public Function<List<SymbolicExpression[]>, SymbolicExpression> memValueCreator(
			SymbolicUniverse universe) {
		return new Function<List<SymbolicExpression[]>, SymbolicExpression>() {

			private SymbolicUniverse u = universe;

			@Override
			public SymbolicExpression apply(List<SymbolicExpression[]> t) {
				SymbolicExpression memElements[] = new SymbolicExpression[t
						.size()];
				SymbolicTupleType elementType = (SymbolicTupleType) ((SymbolicArrayType) ((SymbolicTupleType) dynamicType)
						.sequence().getType(0)).elementType();
				int i = 0;

				for (SymbolicExpression five[] : t) {
					assert five.length == 5;
					if (!five[0].isZero()) {
						assert u.extractNumber(
								(NumericExpression) five[0]) != null : "non-concrete vid";
						// if it does not refer to memory heap, set heapID and
						// mallocID to -1
						five[1] = u.minus(u.oneInt());
						five[2] = five[1];
					}
					memElements[i++] = u.tuple(elementType,
							new SymbolicExpression[]{five[0], // vid
									five[1], // heapID
									five[2], // mallocID
									five[3], // scope value
									five[4] // value set template
					});
				}
				// canonicalize:
				Arrays.sort(memElements, u.comparator());

				SymbolicExpression result = u.array(elementType, memElements);

				return u.tuple((SymbolicTupleType) dynamicType,
						new SymbolicExpression[]{result});
			}
		};
	}

	@Override
	public Function<SymbolicExpression, Iterable<MemoryLocationReference>> memValueIterator() {
		return new Function<SymbolicExpression, Iterable<MemoryLocationReference>>() {
			@Override
			public Iterable<MemoryLocationReference> apply(
					SymbolicExpression t) {
				assert t.type() == dynamicType;
				assert t.operator() == SymbolicOperator.TUPLE;
				List<MemoryLocationReference> results = new LinkedList<>();
				SymbolicExpression memElements = (SymbolicExpression) t
						.argument(0);

				assert memElements.operator() == SymbolicOperator.ARRAY;
				for (SymbolicObject symObj : memElements.getArguments()) {
					SymbolicExpression memElement = (SymbolicExpression) symObj;
					assert memElement.operator() == SymbolicOperator.TUPLE;
					NumericExpression vid = (NumericExpression) memElement
							.argument(0);
					SymbolicExpression scopeVal = (SymbolicExpression) memElement
							.argument(3);
					SymbolicExpression vst = (SymbolicExpression) memElement
							.argument(4);

					assert vid.operator() == SymbolicOperator.CONCRETE;
					int vidInt = ((IntegerNumber) ((NumberObject) vid
							.argument(0)).getNumber()).intValue();
					MemoryLocationReference result;

					if (vidInt > 0)
						result = new CommonMemoryLocationReference(vidInt,
								scopeVal, vst);
					else {
						NumericExpression heapID = (NumericExpression) memElement
								.argument(1);
						NumericExpression mallocID = (NumericExpression) memElement
								.argument(2);
						int heapIDInt, mallocIDInt;

						assert heapID.operator() == SymbolicOperator.CONCRETE;
						assert mallocID.operator() == SymbolicOperator.CONCRETE;
						heapIDInt = ((IntegerNumber) ((NumberObject) heapID
								.argument(0)).getNumber()).intValue();
						mallocIDInt = ((IntegerNumber) ((NumberObject) mallocID
								.argument(0)).getNumber()).intValue();
						result = new CommonMemoryLocationReference(vidInt,
								heapIDInt, mallocIDInt, scopeVal, vst);
					}
					results.add(result);
				}
				return results;
			}
		};
	}

	@Override
	public UnaryOperator<SymbolicExpression> memValueCollector(
			SymbolicUniverse u, SymbolicExpression collectedScopeValue) {
		return new UnaryOperator<SymbolicExpression>() {
			@Override
			public SymbolicExpression apply(SymbolicExpression x) {
				assert x.type() == dynamicType;
				List<SymbolicExpression[]> results = new LinkedList<>();

				for (MemoryLocationReference ref : memValueIterator().apply(x))
					if (ref.scopeValue() != collectedScopeValue)
						results.add(new SymbolicExpression[]{
								u.integer(ref.vid()), u.integer(ref.heapID()),
								u.integer(ref.mallocID()), ref.scopeValue(),
								ref.valueSetTemplate()});
				return memValueCreator(u).apply(results);
			}
		};
	}

	/**
	 * Implementation of {@link MemoryLocationReference}
	 * 
	 * @author xxxx
	 *
	 */
	private class CommonMemoryLocationReference
			implements
				MemoryLocationReference {

		private final int vid;

		private final SymbolicExpression scopeVal;

		private final int heapID;

		private final int mallocID;

		private final SymbolicExpression valueSetTemplate;

		CommonMemoryLocationReference(int vid, SymbolicExpression scopeValue,
				SymbolicExpression valueSetTemplate) {
			this.vid = vid;
			this.scopeVal = scopeValue;
			this.valueSetTemplate = valueSetTemplate;
			assert vid > 0;
			this.heapID = -1;
			this.mallocID = -1;
		}

		CommonMemoryLocationReference(int vid, int heapID, int mallocID,
				SymbolicExpression scopeValue,
				SymbolicExpression valueSetTemplate) {
			this.vid = vid;
			this.scopeVal = scopeValue;
			this.valueSetTemplate = valueSetTemplate;
			assert vid == 0;
			this.heapID = heapID;
			this.mallocID = mallocID;
		}

		@Override
		public int vid() {
			return vid;
		}

		@Override
		public SymbolicExpression scopeValue() {
			return scopeVal;
		}

		@Override
		public SymbolicExpression valueSetTemplate() {
			return valueSetTemplate;
		}

		@Override
		public boolean isHeapObject() {
			return heapID >= 0;
		}

		@Override
		public int heapID() {
			return heapID;
		}

		@Override
		public int mallocID() {
			return mallocID;
		}
	}
}