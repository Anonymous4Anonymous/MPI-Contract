package edu.udel.cis.vsl.abc.ast.node.IF.acsl;

import edu.udel.cis.vsl.abc.ast.node.IF.SequenceNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;

/**
 * This node represents the ACSL-MPI contract clause:
 * <code>mpi uses expr (, expr)*</code>. The sole child node of this node is a
 * sequence of expression nodes representing the upper bound on the MPI_Comm
 * objects used by the function who owns the contract.
 * 
 * @author xxxx
 *
 */
public interface MPIUsesNode extends ContractNode {
	/**
	 * 
	 * @return a sequence of expression nodes representing the upper bound on
	 *         the MPI_Comm objects used by the function who owns the contract.
	 */
	SequenceNode<ExpressionNode> getArguments();
}
