package edu.udel.cis.vsl.civl.transform.analysis.common;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignExprIF;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignOffsetIF;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.type.IF.ArrayType;
import edu.udel.cis.vsl.abc.ast.type.IF.Field;
import edu.udel.cis.vsl.abc.ast.type.IF.ObjectType;
import edu.udel.cis.vsl.abc.ast.type.IF.Type;
import edu.udel.cis.vsl.civl.transform.analysisIF.ReadWriteDataStructures;

public class ReadWriteDataStructureImpl implements ReadWriteDataStructures {

	/* ******************* RWSetElement implementations **********************/

	public static abstract class CommonRWSetElement implements RWSetElement {
		protected Type type;

		private ASTNode source;

		CommonRWSetElement(ASTNode source, Type type) {
			this.source = source;
			this.type = type;
		}

		@Override
		public ASTNode source() {
			return source;
		}

		@Override
		public Type type() {
			return type;
		}
	}

	public static class CommonRWSetArbitraryElement extends CommonRWSetElement
			implements
				RWSetArbitraryElement {

		CommonRWSetArbitraryElement(ASTNode source, Type type) {
			super(source, type);
		}

		@Override
		public RWSetElementKind kind() {
			return RWSetElementKind.ARBITRARY;
		}

		@Override
		public String toString() {
			return "arbitrary(" + type + ")";
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof RWSetArbitraryElement) {
				return ((RWSetArbitraryElement) o).type().equals(type());
			}
			return false;
		}

		@Override
		public int hashCode() {
			return kind().hashCode() ^ 31 * type().hashCode();
		}

		@Override
		public AssignExprIF root() {
			return null;
		}

		@Override
		public int depth() {
			return 0; // unreachable
		}
	}

	public static class CommonRWSetBaseElement extends CommonRWSetElement
			implements
				RWSetBaseElement {

		private AssignExprIF base;

		CommonRWSetBaseElement(ASTNode source, Type type, AssignExprIF base) {
			super(source, type);
			this.base = base;
		}

		@Override
		public RWSetElementKind kind() {
			return RWSetElementKind.BASE;
		}

		@Override
		public AssignExprIF base() {
			return base;
		}

		@Override
		public String toString() {
			return base.toString();
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof RWSetBaseElement) {
				return ((RWSetBaseElement) o).base().equals(base);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return kind().hashCode()
					^ (base.hashCode() + 31 * type().hashCode());
		}

		@Override
		public AssignExprIF root() {
			return base;
		}

		@Override
		public int depth() {
			return 1;
		}
	}

	public static class CommonRWSetFieldElement extends CommonRWSetElement
			implements
				RWSetFieldElement {

		private Field field;

		private RWSetElement struct;

		CommonRWSetFieldElement(ASTNode source, Type type, RWSetElement struct,
				Field field) {
			super(source, type);
			this.struct = struct;
			this.field = field;
		}

		@Override
		public RWSetElementKind kind() {
			return RWSetElementKind.FIELD;
		}

		@Override
		public RWSetElement struct() {
			return struct;
		}

		@Override
		public Field field() {
			return field;
		}

		@Override
		public String toString() {
			return struct.toString() + "." + field.getName();
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof RWSetFieldElement) {
				RWSetFieldElement other = (RWSetFieldElement) o;

				return other.struct().equals(struct())
						&& other.field() == field();
			}
			return false;
		}

		@Override
		public int hashCode() {
			return kind().hashCode() ^ (struct.hashCode() + 7 * field.hashCode()
					+ 31 * type().hashCode());
		}

		@Override
		public AssignExprIF root() {
			return struct.root();
		}

		@Override
		public int depth() {
			return struct.depth() + 1;
		}
	}

	public static class CommonRWSetOffsetElement extends CommonRWSetElement
			implements
				RWSetOffsetElement {

		private ExpressionNode offset;

		private boolean isPositive;

		private RWSetElement base;

		CommonRWSetOffsetElement(ASTNode source, Type type, RWSetElement base,
				ExpressionNode offset, boolean isPositive) {
			super(source, type);
			this.base = base;
			this.isPositive = isPositive;
			this.offset = offset;
		}

		@Override
		public RWSetElementKind kind() {
			return RWSetElementKind.OFFSET;
		}

		@Override
		public RWSetElement base() {
			return base;
		}

		@Override
		public ExpressionNode offset() {
			return offset;
		}

		@Override
		public boolean isPositive() {
			return isPositive;
		}

		@Override
		public String toString() {
			String sign = isPositive ? " + " : " - ";

			return base.toString() + sign + offset.prettyRepresentation();
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof RWSetOffsetElement) {
				RWSetOffsetElement other = (RWSetOffsetElement) o;

				return other.base().equals(base())
						&& other.offset().equals(offset())
						&& other.isPositive() == isPositive;
			}
			return false;
		}

		@Override
		public int hashCode() {
			return kind().hashCode() ^ (base.hashCode() + 7 * offset.hashCode()
					+ 31 * type().hashCode() + (isPositive ? 1 : 0));
		}

		@Override
		public AssignExprIF root() {
			return this.base.root();
		}

		@Override
		public int depth() {
			return base.depth() + 1;
		}
	}

	public static class CommonRWSetSubscriptElement extends CommonRWSetElement
			implements
				RWSetSubscriptElement {

		private RWSetElement array;

		/**
		 * offset to the sub-array which is actually subscripted:
		 */
		private AssignOffsetIF offset;

		private ExpressionNode[] indices;

		CommonRWSetSubscriptElement(ASTNode source, Type type,
				RWSetElement array, AssignOffsetIF offset,
				ExpressionNode[] indices) {
			super(source, type);
			this.array = array;
			this.offset = offset;
			this.indices = indices;
		}

		@Override
		public RWSetElementKind kind() {
			return RWSetElementKind.SUBSCRIPT;
		}

		@Override
		public RWSetElement array() {
			return array;
		}

		@Override
		public AssignOffsetIF offset() {
			return offset;
		}

		@Override
		public ExpressionNode[] indices() {
			return indices;
		}

		@Override
		public String toString() {
			String str = array.toString();

			str += "[" + offset.toString() + " + "
					+ indices[0].prettyRepresentation() + "]";
			for (int i = 1; i < indices.length; i++)
				str += "[" + indices[i].prettyRepresentation() + "]";
			return str;
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof RWSetSubscriptElement) {
				RWSetSubscriptElement other = (RWSetSubscriptElement) o;

				return other.array().equals(array())
						&& other.offset().equals(offset())
						&& Arrays.equals(other.indices(), indices);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return kind().hashCode() ^ (array.hashCode() + 7 * offset.hashCode()
					+ 31 * type().hashCode() + 91 * Arrays.hashCode(indices));
		}

		@Override
		public AssignExprIF root() {
			return this.array.root();
		}

		@Override
		public int depth() {
			return array.depth() + 1;
		}
	}

	/* ******************* RWSet implementations **********************/
	public static class CommonRWSet implements RWSet {

		private Set<RWSetElement> reads;

		private Set<RWSetElement> writes;

		public CommonRWSet() {
			this.reads = new HashSet<>();
			this.writes = new HashSet<>();
		}

		@Override
		public Set<RWSetElement> reads() {
			return reads;
		}

		@Override
		public Set<RWSetElement> writes() {
			return writes;
		}

		@Override
		public void addReads(RWSetElement... elements) {
			for (RWSetElement e : elements)
				reads.add(e);
		}

		@Override
		public void addWrites(RWSetElement... elements) {
			for (RWSetElement e : elements)
				writes.add(e);
		}

		@Override
		public void addReads(Iterable<RWSetElement> elements) {
			for (RWSetElement e : elements)
				reads.add(e);
		}

		@Override
		public void addWrites(Iterable<RWSetElement> elements) {
			for (RWSetElement e : elements)
				writes.add(e);
		}

		@Override
		public void add(RWSet rwset) {
			this.reads.addAll(rwset.reads());
			this.writes.addAll(rwset.writes());
		}

		@Override
		public String toString() {
			return "read:\n" + reads + "\nwrites\n" + writes;
		}
	}

	/* ******************* factory implementations **********************/
	public static class CommonReadWriteDataStructureFactory
			implements
				ReadWriteDataStructureFactory {

		@Override
		public RWSet newRWSet() {
			return new CommonRWSet();
		}

		@Override
		public RWSetElement arbitraryElement(ASTNode source, Type type) {
			return new CommonRWSetArbitraryElement(source, type);
		}

		@Override
		public RWSetElement baseElement(ASTNode source, AssignExprIF base) {
			return new CommonRWSetBaseElement(source, base.type(), base);
		}

		@Override
		public RWSetElement fieldElement(ASTNode source, RWSetElement struct,
				Field field) {
			assert struct.kind() != RWSetElement.RWSetElementKind.ARBITRARY;
			return new CommonRWSetFieldElement(source, field.getType(), struct,
					field);
		}

		@Override
		public RWSetElement offsetElement(ASTNode source, RWSetElement base,
				ExpressionNode offset, boolean isPositive) {
			assert base.kind() != RWSetElement.RWSetElementKind.ARBITRARY;
			return new CommonRWSetOffsetElement(source, base.type(), base,
					offset, isPositive);
		}

		@Override
		public RWSetElement subscriptElement(ASTNode source, RWSetElement array,
				AssignOffsetIF offset, ExpressionNode[] indices) {
			assert array.kind() != RWSetElement.RWSetElementKind.ARBITRARY;
			Type type = (ObjectType) array.type();
			int i = 1;

			do {
				type = ((ArrayType) type).getElementType();
			} while (i++ < indices.length);
			return new CommonRWSetSubscriptElement(source, type, array, offset,
					indices);
		}
	}
}
