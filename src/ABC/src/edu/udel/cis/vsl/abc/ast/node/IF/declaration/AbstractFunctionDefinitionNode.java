package edu.udel.cis.vsl.abc.ast.node.IF.declaration;

import edu.udel.cis.vsl.abc.ast.node.IF.PairNode;
import edu.udel.cis.vsl.abc.ast.node.IF.SequenceNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;

/**
 * <p>
 * An abstract function definition contains the information for an abstract
 * function (i.e. a function in the mathematical sense, treated as uninterpreted
 * in the code).
 * </p>
 * 
 * <p>
 * An abstract function has an identifier, return type, parameters, and an
 * integer specifying the number of partial derivatives that may be taken.
 * </p>
 * 
 * @author zirkel
 * 
 */
public interface AbstractFunctionDefinitionNode
		extends FunctionDeclarationNode {

	/**
	 * Returns the number of partial derivatives that exist and are continuous.
	 * 
	 * @return The total number of partial derivatives (of any parameter) that
	 *         may be taken.
	 */
	int continuity();

	/**
	 * Returns the sequence of interval whose Cartesian product defines the
	 * domain on which the function is differentiable.
	 * 
	 * @return the interval sequence or <code>null</code> if absent
	 */
	SequenceNode<PairNode<ExpressionNode, ExpressionNode>> getIntervals();

	@Override
	AbstractFunctionDefinitionNode copy();

}
