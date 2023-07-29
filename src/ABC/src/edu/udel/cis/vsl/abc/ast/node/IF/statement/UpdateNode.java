package edu.udel.cis.vsl.abc.ast.node.IF.statement;

import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.FunctionCallNode;

/**
 * This node represents an expression <code>$update($collator c) f()</code>. It
 * is a keyword <code>$update($collator c)</code> ahead of a function call
 * expresson.
 * 
 * An <code>$update($collator c) f()</code> means execute function f on all
 * $collate states that have not been arrived by the current process in c.
 * 
 * Parameters of f are evaluated at the real state.
 * 
 * @author xxxx
 *
 */
public interface UpdateNode extends StatementNode {
	/**
	 * Get the {@link FunctionCallNode} which represents the function call
	 * expression attached with this node.
	 * 
	 * @return
	 */
	FunctionCallNode getFunctionCall();

	/**
	 * Get the {@link ExpressionNode} which has the type $collator. The collator
	 * expression is attached with aN {@link UpdateNode}
	 * 
	 * @return
	 */
	ExpressionNode getCollator();
}
