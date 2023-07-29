/**
 * 
 */
package edu.udel.cis.vsl.abc.ast.node.IF.expression;

import edu.udel.cis.vsl.abc.ast.node.IF.PairNode;
import edu.udel.cis.vsl.abc.ast.node.IF.SequenceNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.VariableDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.TypeNode;

/**
 * A CIVL-C array lambda expression, including three components, bound variable
 * declaration list, (optional) restriction and expression. It has the following
 * syntax:<br>
 * 
 * <pre>
 * lambda: 
 *   (type) $lambda ( variable-decl-list | restrict? ) body-expression ;
 * 
 * variable-decl-list:
 *   variable-decl-sub-list (; variable-decl-sub-list)* ;
 *   
 * variable-decl-sub-list:
 *   type ID (, ID)* (: domain)?
 * 
 * </pre>
 * 
 * e.g.,
 * 
 * <pre>
 * (double[n]) $lambda (int x) x*1.5
 * </pre>
 * 
 * @author Manchun Zheng (zxxxx)
 * 
 */
public interface ArrayLambdaNode extends ExpressionNode {

	/**
	 * the type of this array lambda
	 * 
	 * @return
	 */
	TypeNode type();

	/**
	 * the bound variable declaration list, which is a sequence node of pairs of
	 * variable declaration list and an optional expression that has domain
	 * type. The dimension of the domain expression, if present, should agree
	 * with the number of variable declarations in the same pair. e.g.,
	 * <code>$lambda(int i,j: dom1;) i+j </code> This will have the bound
	 * variable list as: <code>{{{int i, int j}, dom1}}</code>.
	 * 
	 * @return the bound variable declaration list
	 */
	SequenceNode<PairNode<SequenceNode<VariableDeclarationNode>, ExpressionNode>> boundVariableList();

	/**
	 * Boolean-valued expression assumed to hold when evaluating expression.
	 */
	ExpressionNode restriction();

	/**
	 * The body expression.
	 * 
	 * @return The body expression.
	 */
	ExpressionNode expression();

}
