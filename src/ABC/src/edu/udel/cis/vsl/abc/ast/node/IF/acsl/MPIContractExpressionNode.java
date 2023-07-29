package edu.udel.cis.vsl.abc.ast.node.IF.acsl;

import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;

public interface MPIContractExpressionNode extends ExpressionNode {
	public enum MPIContractExpressionKind {
		MPI_AGREE, 
		MPI_BUF,
		MPI_BACK_MESSAGES,
		MPI_EXTENT, 
		MPI_COMM_SIZE,
		MPI_COMM_RANK,
		MPI_NONOVERLAPPING, 
		MPI_ON, 
		MPI_REDUCE, 
		MPI_SIG, 
		MPI_ABSENT, 
		MPI_ABSENT_EVENT,
	}

	MPIContractExpressionKind MPIContractExpressionKind();

	/**
	 * Return the number of arguments of this MPI expression
	 *
	 * @return
	 */
	int numArguments();

	/**
	 * Returns the index-th argument
	 *
	 * @param index
	 * @return
	 */
	ExpressionNode getArgument(int index);
}
