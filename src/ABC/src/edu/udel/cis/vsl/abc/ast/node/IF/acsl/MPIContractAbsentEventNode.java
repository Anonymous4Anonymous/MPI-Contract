package edu.udel.cis.vsl.abc.ast.node.IF.acsl;

import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;

/**
 * <p>
 * This {@link ContractNode} represents an "mpi event", which represents a set
 * of specific actions that a process performs during runtime.
 * </p>
 *
 * <p>There are four kinds of "mpi event": SENDTO, SENDFROM, ENTER and EXIT.
 *
 * <p>
 * The SENDTO(dests, tags) event represents the set of send actions which
 * sends messages to the given set of destinations (dests) with the given set of
 * message tags (tags),  performed by the running process.
 * </p>
 *
 * <p>
 * The SENDFROM(srcs, tags) event represents the set of send actions which sends
 * messages from the given set of source processes (srcs) to the running process
 * with the given message tags (tags).
 * </p>
 *
 * <p>
 * The ENTER(p) event is associated to a function "f".  It represents the action
 * that a process p enters the function "f".  If the argument p is absent, it
 * represents the action performed by the running process.
 * </p>
 *
 * <p>
 * The EXIT(p) event is associated to a function "f".  It represents the action
 * that a process p exits the function "f".  If the argument p is absent, it
 *  * represents the action performed by the running process.
 * </p>
 */
public interface MPIContractAbsentEventNode extends MPIContractExpressionNode {
    /**
     * <p>
     * The kinds of the events.
     * SENDTO is the kind for <code>\sendto(dests, tags)</code>
     * SENDFROM is the kind for <code>\sendfrom(srcs, tags)</code>
     * ENTER is the kind for <code>\enter(proc)</code>
     * EXIT is the kind for <code>\exit(proc)</code>
     * </p>
     */
    static public enum MPIAbsentEventKind {
        SENDTO, SENDFROM, ENTER, EXIT
    }

    /**
     * @return the {@link MPIAbsentEventKind} of this event.
     */
    MPIAbsentEventKind absentEventKind();

    /**
     * <p>for the meaning of the arguments, see {@link MPIContractAbsentEventNode}</p>
     *
     * @return the arguments of this event.
     * <ul>for
     * <li>SENDTO or SENDFROM kind, there must be two arguments: dests (or srcs)
     * and tags</li>
     * <li>ENTER kind, there is one or zero argument</li>
     * <li>EXIT kind, there is one or zero argument</li>
     * </ul>
     */
    ExpressionNode[] arguments();
}
