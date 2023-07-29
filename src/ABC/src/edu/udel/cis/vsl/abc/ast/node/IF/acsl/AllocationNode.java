package edu.udel.cis.vsl.abc.ast.node.IF.acsl;

import edu.udel.cis.vsl.abc.ast.node.IF.SequenceNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;

/**
 * This represents an ACSL allocation clause, which has the syntax <br>
 * <code>allocates p1, p2, p3;</code> <br>
 * or <br>
 * <code>frees p1, p2, p3;</code>
 * 
 * @author Manchun Zheng
 *
 */
public interface AllocationNode extends ContractNode {

	/**
	 * Is this node an <code>allocates</code> clause?
	 * 
	 * @return
	 */
	boolean isAllocates();

	/**
	 * Is this node a <code>frees</code> clause?
	 * 
	 * @return
	 */
	boolean isFrees();

	/**
	 * Returns the list of memory units associated with this allocation clause.
	 * 
	 * @return
	 */
	SequenceNode<ExpressionNode> memoryList();
}
