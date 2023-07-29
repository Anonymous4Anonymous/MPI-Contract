package edu.udel.cis.vsl.abc.analysis.pointsTo.IF;

import edu.udel.cis.vsl.abc.ast.entity.IF.Variable;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.IdentifierExpressionNode;

public interface AssignStoreExprIF extends AssignExprIF {

	boolean isAllocation();

	/**
	 * either {@link IdentifierExpressionNode} or an expression node represening
	 * string literal or allocation.
	 * 
	 * @return
	 */
	ExpressionNode store();

	Variable variable();
}
