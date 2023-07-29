package edu.udel.cis.vsl.abc.analysis.pointsTo.common;

import java.util.Arrays;

import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;
import edu.udel.cis.vsl.sarl.IF.UnaryOperator;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.object.IntObject;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicUninterpretedType;
import edu.udel.cis.vsl.sarl.object.common.SimpleSequence;

/**
 * Producing points-to graph components, including {@link PointsToConstraint}s,
 * edges and nodes.
 * 
 * @author xxxx
 *
 */
public class PointsToGraphComponentFactory {
	/* ************** constraints: ************* */
	/**
	 * A points to constraint is a subset-of relation, it has one of the
	 * following forms:
	 * <ol>
	 * <li>*a subset-of b</li>
	 * <li>a subset-of *b</li>
	 * </ol>
	 * 
	 * @author xxxx
	 *
	 */
	static class PointsToConstraint {

		/**
		 * superDeref: a subset-of *b <br>
		 * otherwise: *a subset-of b
		 */
		private boolean superDeref;

		/**
		 * a node on the pointsToGraph representing contents of a variable
		 */
		private SymbolicExpression subset;

		/**
		 * a node on the pointsToGraph representing contents of a variable
		 */
		private SymbolicExpression superset;

		private PointsToConstraint(boolean leftDeref, SymbolicExpression subset,
				SymbolicExpression superset) {
			assert subset != null;
			assert superset != null;
			this.subset = subset;
			this.superset = superset;
			this.superDeref = leftDeref;
		}

		SymbolicExpression subset() {
			return subset;
		}

		SymbolicExpression superset() {
			return superset;
		}

		boolean isSuperDeref() {
			return superDeref;
		}
	}

	/* *********** nodes and edges ********** */

	private int nodeCounter = 0;

	private static String nodeTypeName = "v";

	private static String edgeTypeName = "sub";

	/**
	 * a node can be represented by an uninterpreted type
	 */
	private SymbolicUninterpretedType nodeType;

	/**
	 * an edge is a function takes two arguments: subset and super
	 */
	private SymbolicConstant edgeType;

	/**
	 * The operator for getting subset of an edge:
	 */
	private UnaryOperator<SymbolicExpression> getSubset;

	/**
	 * The operator for getting superset of an edge:
	 */
	private UnaryOperator<SymbolicExpression> getSuperset;

	/**
	 * the content refers to every thing:
	 */
	private SymbolicExpression fullNode;

	/**
	 * a reference to symbolic universe:
	 */
	private SymbolicUniverse universe;

	PointsToGraphComponentFactory(SymbolicUniverse universe) {
		this.universe = universe;
		this.nodeType = universe.symbolicUninterpretedType(nodeTypeName);
		this.edgeType = universe.symbolicConstant(
				universe.stringObject(edgeTypeName),
				universe.functionType(Arrays.asList(nodeType, nodeType),
						universe.integerType()));
		this.fullNode = newNode();
		this.getSubset = new UnaryOperator<SymbolicExpression>() {
			@Override
			public SymbolicExpression apply(SymbolicExpression x) {
				assert x.type() == universe.integerType();
				@SuppressWarnings("unchecked")
				SimpleSequence<SymbolicExpression> argsSeq = (SimpleSequence<SymbolicExpression>) x
						.argument(1);

				return argsSeq.get(0);
			}
		};
		this.getSuperset = new UnaryOperator<SymbolicExpression>() {
			@Override
			public SymbolicExpression apply(SymbolicExpression x) {
				assert x.type() == universe.integerType();
				@SuppressWarnings("unchecked")
				SimpleSequence<SymbolicExpression> argsSeq = (SimpleSequence<SymbolicExpression>) x
						.argument(1);

				return argsSeq.get(1);
			}
		};
	}

	/**
	 * create a new {@link PointsToConstaint}: "*b subset-of a" or "b subset-of
	 * *a"
	 * 
	 * @param superDeref
	 *            true if it is "b subset-of *a" ; otherwise "*b subset-of a"
	 * @param subset
	 *            the subset node
	 * @param superset
	 *            the superset node
	 * @return
	 */
	PointsToConstraint newConstraint(boolean superDeref,
			SymbolicExpression subset, SymbolicExpression superset) {
		return new PointsToConstraint(superDeref, subset, superset);
	}

	/**
	 * 
	 * @return a fresh new node
	 */
	SymbolicExpression newNode() {
		IntObject key = universe.intObject(nodeCounter++);

		return universe.concreteValueOfUninterpretedType(nodeType, key);
	}

	/**
	 * @return the node representing the worst case
	 */
	SymbolicExpression fullNode() {
		return fullNode;
	}

	/**
	 * 
	 * @param subset
	 *            a node in the graph
	 * @param superset
	 *            a node in the graph
	 * @return an directed edge from the given node "subset" to node "superset"
	 */
	SymbolicExpression edge(SymbolicExpression subset,
			SymbolicExpression superset) {
		assert subset.type() == nodeType;
		assert superset.type() == nodeType;
		return universe.apply(edgeType, Arrays.asList(subset, superset));
	}

	/**
	 * 
	 * @param edge
	 *            an edge in the graph
	 * @return the subset node of the edge
	 */
	SymbolicExpression getSubset(SymbolicExpression edge) {
		return getSubset.apply(edge);
	}

	/**
	 * /**
	 * 
	 * @param edge
	 *            an edge in the graph
	 * @return the superset node of the edge
	 */
	SymbolicExpression getSuperset(SymbolicExpression edge) {
		return getSuperset.apply(edge);
	}
}
