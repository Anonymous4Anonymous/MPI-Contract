package edu.udel.cis.vsl.abc.ast.node.IF.acsl;

import edu.udel.cis.vsl.abc.ast.node.IF.SequenceNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;

/**
 * An contract block introduced by the
 * <code>mpi_collective(MPI_Comm, Kind):</code> contract constructor.
 * 
 * @author xxxx
 *
 */
public interface MPICollectiveBlockNode extends ContractNode {

	/**
	 * Returns the node corresponding to the specific MPI_Comm
	 * 
	 * @return
	 */
	ExpressionNode getMPIComm();

	/**
	 * Get the body of a MPI collective block
	 * 
	 * @return
	 */
	SequenceNode<ContractNode> getBody();

	@Override
	MPICollectiveBlockNode copy();
}
