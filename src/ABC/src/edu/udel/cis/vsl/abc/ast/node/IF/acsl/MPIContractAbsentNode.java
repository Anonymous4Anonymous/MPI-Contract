package edu.udel.cis.vsl.abc.ast.node.IF.acsl;

/**
 * <p>
 * This {@link MPIContractExpressionNode} denotes an MPI absent expression, which
 * states a path predicate for an MPI collective-style function.
 * <p>
 * An MPI absent expressions is parameterized by three events, each of which is
 * an instance of {@link MPIContractAbsentEventNode}.  The three events are called
 * respectively the "absentEvent", "fromEvent" and "untilEvent".  They encode
 * the meaning that
 * <b>
 * no "absentEvent" can happen since "fromEvent" until "untilEvent".
 * </b>
 * </p>
 */
public interface MPIContractAbsentNode extends MPIContractExpressionNode {
    /**
     * @return the "absentEvent". For the meaning of "absentEvent", see
     * {@link MPIContractAbsentNode}
     */
    MPIContractAbsentEventNode absentEvent();

    /**
     * @return the "fromEvent". For the meaning of "fromEvent", see
     * {@link MPIContractAbsentNode}
     */
    MPIContractAbsentEventNode fromEvent();

    /**
     * @return the "untilEvent". For the meaning of "untilEvent", see
     * {@link MPIContractAbsentNode}
     */
    MPIContractAbsentEventNode untilEvent();
}
