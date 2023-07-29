/**
 * 
 */
package edu.udel.cis.vsl.abc.ast.node.IF.expression;

import edu.udel.cis.vsl.abc.ast.node.IF.declaration.VariableDeclarationNode;

/**
 * A lambda function, including two/three components, a bound variable
 * declaration (optional) and the lambda term. It has the following syntax:<br>
 * 
 * <pre>
 * lambda: 
 *   $lambda ( bound-variable-decl ? ) lambda-function ;
 * free-variable-decl:
 *   type ID
 * 
 * </pre>
 * 
 * e.g.,
 * 
 * <pre>
 * $lambda (int x) x*1.5
 * </pre>
 * 
 * @author Manchun Zheng (zxxxx)
 * 
 */
public interface LambdaNode extends ExpressionNode {

	/**
	 * 
	 * @return The bound variable declaration node
	 */
	VariableDeclarationNode freeVariable();

	/**
	 * 
	 * @return The boolean restriction on the bound variable
	 */
	ExpressionNode restriction();

	/**
	 *
	 * @return The lambda function expression
	 */
	ExpressionNode lambdaFunction();

}
