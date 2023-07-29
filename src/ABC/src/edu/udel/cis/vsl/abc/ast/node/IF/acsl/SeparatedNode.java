package edu.udel.cis.vsl.abc.ast.node.IF.acsl;

import java.util.List;

import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;

/**
 * This class represents the <code>\separated</code> construct in the ACSL-MPI
 * contract language. A <code>\separated</code> takes a list of term sets and
 * asserts that all the sets are pair-wisely disjoint.
 * 
 * @author xxxx
 *
 */
public interface SeparatedNode extends ExpressionNode {
	/**
	 * 
	 * @return the listed, pair-wisely disjointed term sets.
	 */
	List<ExpressionNode> getDisjointTermSets();
}
