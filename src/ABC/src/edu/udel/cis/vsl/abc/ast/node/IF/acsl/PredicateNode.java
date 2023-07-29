package edu.udel.cis.vsl.abc.ast.node.IF.acsl;

import edu.udel.cis.vsl.abc.ast.node.IF.IdentifierNode;
import edu.udel.cis.vsl.abc.ast.node.IF.SequenceNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.FunctionDefinitionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.VariableDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;

/**
 * The ACSL predicate node, which in the view of ABC, is just a function with a
 * boolean return type. An ACSL predicate annotation will become a function
 * which get inserted in the AST tree at the location of the annotation.
 * 
 * @author xxxx
 *
 */
public interface PredicateNode extends ContractNode, FunctionDefinitionNode {
	/**
	 * The name of the predicate
	 * 
	 * @return
	 */
	IdentifierNode getPredicateName();

	/**
	 * The parameters of the predicate
	 * 
	 * @return
	 */
	SequenceNode<VariableDeclarationNode> getParameters();

	/**
	 * the body expression of the predicate
	 * 
	 * @return
	 */
	ExpressionNode getExpressionBody();

	@Override
	PredicateNode copy();
}
