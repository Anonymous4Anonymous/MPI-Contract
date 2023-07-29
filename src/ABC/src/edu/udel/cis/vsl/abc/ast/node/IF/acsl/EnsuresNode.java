package edu.udel.cis.vsl.abc.ast.node.IF.acsl;

import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;

/**
 * An "ensures" clause in a procedure contract represents a post-condition.
 *
 * @author xxxx
 */
public interface EnsuresNode extends ContractNode {

    /**
     * An expression of boolean type which is the post-condition
     *
     * @return the boolean expression post-condition
     */
    ExpressionNode getExpression();

    @Override
    EnsuresNode copy();

    /**
     * @return true iff this ensures clause specifies a "guarantee", i.e.
     * a set of absence assertions that is guaranteed to be satisfied by any execution
     * of the function specified by this clause.
     */
    boolean isGuarantee();

    /**
     * <p>see {@link #isGuarantee()}</p>
     * @param isGuarantee
     *         true to set this clause to be {@link #isGuarantee()}
     */
    void setIsGuarantee(boolean isGuarantee);
}
