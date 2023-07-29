package edu.udel.cis.vsl.civl.transform.analysisIF;

import java.util.Set;

import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignExprIF;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignOffsetIF;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.type.IF.Field;
import edu.udel.cis.vsl.abc.ast.type.IF.Type;

public interface ReadWriteDataStructures {

	// base interface for RWSet object:
	static interface RWSetElement {
		public static enum RWSetElementKind {
			ARBITRARY, BASE, FIELD, SUBSCRIPT, OFFSET,
		}

		RWSetElementKind kind();

		Type type();

		ASTNode source();

		AssignExprIF root();

		int depth();
	}

	static interface RWSetArbitraryElement extends RWSetElement {
		// represents an arbitrary value of the type of this element
	}

	static interface RWSetBaseElement extends RWSetElement {
		AssignExprIF base();
	}

	static interface RWSetFieldElement extends RWSetElement {
		RWSetElement struct();

		Field field();
	}

	static interface RWSetOffsetElement extends RWSetElement {
		RWSetElement base();

		ExpressionNode offset();

		boolean isPositive();
	}

	static interface RWSetSubscriptElement extends RWSetElement {
		/**
		 * @return an RWSetElement representing the array of which this instance
		 *         represents taking subscripts on the SUB-ARRAY (which is
		 *         obtained by "&array[0] + {@link #offset()}")
		 */
		RWSetElement array();

		/**
		 * 
		 * @return an offset on the {@link #array()}
		 */
		AssignOffsetIF offset();

		/**
		 * @return subscript indices
		 */
		ExpressionNode[] indices();
	}

	static interface RWSet {
		Set<RWSetElement> reads();

		Set<RWSetElement> writes();

		void addReads(RWSetElement... elements);

		void addWrites(RWSetElement... elements);

		void addReads(Iterable<RWSetElement> elements);

		void addWrites(Iterable<RWSetElement> elements);

		void add(RWSet rwset);
	}

	static interface ReadWriteDataStructureFactory {
		RWSet newRWSet();

		RWSetElement arbitraryElement(ASTNode source, Type type);

		RWSetElement baseElement(ASTNode source, AssignExprIF base);

		RWSetElement fieldElement(ASTNode source, RWSetElement struct,
				Field field);

		RWSetElement offsetElement(ASTNode source, RWSetElement base,
				ExpressionNode offset, boolean isPositive);

		RWSetElement subscriptElement(ASTNode source, RWSetElement array,
				AssignOffsetIF offset, ExpressionNode[] indices);
	}
}
