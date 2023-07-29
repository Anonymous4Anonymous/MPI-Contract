package edu.udel.cis.vsl.abc.analysis.entity;

import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.MPIContractAbsentNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode;
import edu.udel.cis.vsl.abc.token.IF.SyntaxException;

import static edu.udel.cis.vsl.abc.ast.node.IF.acsl.MPIContractAbsentEventNode.MPIAbsentEventKind.*;
import static edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode.ExpressionKind.OPERATOR;
import static edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode.Operator.LAND;

/**
 * <p>
 * This analyzer performs analysis on the restrictions over \absent expressions.
 * </p>
 *
 *
 * <p>
 * For a requirement, the absence assertions are only allowed to have the
 * following form:
 * <code>\absentof \sendfrom(r, t) after \exit(r) until \exit</code>*
 * </p>
 *
 * <p>
 * For a guarantee, the absence assertions are only allowed to have one of the
 * following forms:
 * <code>\absentof \sendto(r, t) after \enter until \enter(r')</code> and
 * <code>\absentof \exit after \enter until \enter(r)</code>
 * </p>
 */
class AbsentAssertionAnalyzer {

    /**
     * <p>checks if absence assertions are used under restrictions.
     * see {@link AbsentAssertionAnalyzer} for details.</p>
     *
     * <p>note that if the given expression contains no {@link MPIContractAbsentNode},
     * this method is a no-op.
     * </p>
     *
     * @param clauseExpr
     *         a clause expression
     * @param isRequirement
     *         true iff the given expression belongs to a requires clause; otherwise,
     *         it belongs to an ensures clause
     * @return true iff the given expression contains MPI-Absent
     * @throws SyntaxException
     *         when the given expression contains MPI-Absent
     *         assertion but violates the restriction.
     */
    boolean processRequirementOrGuarantee(ExpressionNode clauseExpr,
            boolean isRequirement)
            throws SyntaxException {
        return processConjunctSet(clauseExpr, isRequirement);
    }

    /**
     * <p>
     * make sure the given expression is either a conjunctive set of
     * {@link MPIContractAbsentNode} or contains no {@link MPIContractAbsentNode}.
     * </p>
     *
     * @param expr
     *         an expression in contract clause
     * @param isRequirement
     *         true iff the given expression belongs to a requires clause; otherwise,
     *         it belongs to an ensures clause
     * @return true iff the given expression contains MPI-Absent
     * @throws SyntaxException
     *         when the expression is neither a conjunctive set of
     *         {@link MPIContractAbsentNode} nor contains no {@link MPIContractAbsentNode}.
     */
    private boolean processConjunctSet(ExpressionNode expr, boolean isRequirement) throws SyntaxException {
        if (expr.expressionKind() == OPERATOR) {
            OperatorNode opNode = (OperatorNode) expr;
            boolean hasMPIAbsent;

            if (opNode.getOperator() == LAND) {
                hasMPIAbsent = processConjunctSet(opNode.getArgument(0),
                        isRequirement);
                hasMPIAbsent |= processConjunctSet(opNode.getArgument(1),
                        isRequirement);
                return hasMPIAbsent;
            }
        } else if (expr instanceof MPIContractAbsentNode) {
            checkAbsenceAssertion((MPIContractAbsentNode) expr,
                    isRequirement);
            return true;
        }
        processNoMPIAbsent(expr);
        return false;
    }

    /**
     * <p>
     * make sure the given expression has no {@link MPIContractAbsentNode}
     * </p>
     *
     * @param expr
     *         an expression in contract clause
     * @throws SyntaxException
     *         if the expression contains an
     *         {@link MPIContractAbsentNode}
     */
    private void processNoMPIAbsent(ASTNode expr) throws SyntaxException {
        String restriction = "a requirement/guarantee clause must either be" +
                             "a conjunct of absence assertions or free of " +
                             "absence assertion";

        if (expr != null)
            for (ASTNode child : expr.children()) {

                if (child instanceof MPIContractAbsentNode)
                    throw new SyntaxException("The use of \\absentof construct " +
                                              "violates the restriction.\n" +
                                              restriction, child.getSource());
                else
                    processNoMPIAbsent(child);
            }
    }

    /**
     * <p>checks if the absent assertion has the restricted form according to
     * whether it belongs to a requirement or guarantee
     * </p>
     *
     * @param absent
     *         an absent assertion
     * @param isRequirement
     *         true iff the given absent assertion belongs to a requirement;
     *         otherwise, it belongs to a guarantee.
     */
    private void checkAbsenceAssertion(MPIContractAbsentNode absent,
            boolean isRequirement) throws SyntaxException {
        if (isRequirement) {
            // \absentof \sendfrom(r, t) after \exit(r) until \exit:
            if (absent.absentEvent().absentEventKind() == SENDFROM)
                if (absent.fromEvent().absentEventKind() == EXIT)
                    if (absent.untilEvent().absentEventKind() == EXIT)
                        if (absent.untilEvent().arguments().length == 0)
                            return;
        } else {
            // \absentof \sendto(r, t) after \enter until \enter(r'):
            if (absent.absentEvent().absentEventKind() == SENDTO)
                if (absent.fromEvent().absentEventKind() == ENTER)
                    if (absent.fromEvent().arguments().length == 0)
                        if (absent.untilEvent().absentEventKind() == ENTER)
                            return;
            // \absentof \exit after \enter until \enter(r)
            if (absent.absentEvent().absentEventKind() == EXIT)
                if (absent.absentEvent().arguments().length == 0)
                    if (absent.fromEvent().absentEventKind() == ENTER)
                        if (absent.fromEvent().arguments().length == 0)
                            if (absent.untilEvent().absentEventKind() == ENTER)
                                return;
        }

        String clause = isRequirement ? "requirement" : "guarantee";
        String msg = isRequirement ? "\\absentof \\sendfrom(r, t) after " +
                                     "\\exit(r) until \\exit" :
                "\\absentof \\exit after \\enter until \\enter(r)" +
                " or \\absentof \\sendto(r, t) after \\enter until \\enter(r')";

        msg = "For " + clause + ", an absent assertion can only has the form: "
              + msg;
        throw new SyntaxException("The absent assertion " + absent.
                prettyRepresentation() + " in " + clause + " has the " +
                                  "unsupported form.\n" + msg,
                absent.getSource());
    }
}
