package edu.udel.cis.vsl.abc.analysis.pointsTo.IF;

import edu.udel.cis.vsl.abc.ast.entity.IF.Function;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.FunctionCallNode;

/**
 * creating invocation graph nodes
 * 
 * @author xxxx
 *
 */
public interface InvocationGraphNodeFactory {

	/**
	 * creates a new invocation graph node
	 * 
	 * @param function
	 *            the {@link Function} that is associated with the node
	 * @param parent
	 *            the parent node of the generated node, null if this node is
	 *            associated with the "main" function
	 * @param call
	 *            the {@link FunctionCallNode} representing the lexical call
	 *            that is associated with this node
	 * @param returnTo
	 *            the expression abstraction that will take the returned value
	 *            after returning from the associated function call
	 * @param actualParams
	 *            the sequence (as a java array) of actual parameter
	 *            abstractions
	 * @return
	 */
	InvocationGraphNode newNode(Function function, InvocationGraphNode parent,
			AssignExprIF returnTo, AssignExprIF... actualParams);
}
