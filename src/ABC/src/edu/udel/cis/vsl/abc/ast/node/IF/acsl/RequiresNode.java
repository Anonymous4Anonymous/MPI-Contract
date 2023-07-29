package edu.udel.cis.vsl.abc.ast.node.IF.acsl;

import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;

/**
 * A <code>requires</code> clause in a CIVL-C procedure contract. This clause
 * specifies a pre-condition: something that is expected to hold when the
 * function is called.
 *
 * @author xxxx
 * @see EnsuresNode
 * @see ContractNode
 */
public interface RequiresNode extends ContractNode {

    /**
     * Gets the boolean condition which is the pre-condition.
     *
     * @return the boolean expression which specified the pre-condition
     */
    ExpressionNode getExpression();

    @Override
    RequiresNode copy();

    /**
     * @return true iff this requires clause specifies a "requirement", i.e.
     * a set of absence assertions that is required to be satisfied by any execution
     * of the function specified by this clause.
     */
    boolean isRequirement();

    /**
     * see {@link #isRequirement()}
     *
     * @param isRequirement
     *         true to set this clause to be {@link #isRequirement()}
     */
    void setIsRequirement(boolean isRequirement);
}
