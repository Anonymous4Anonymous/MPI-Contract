package edu.udel.cis.vsl.abc.ast.node.common.declaration;

import edu.udel.cis.vsl.abc.ast.IF.ASTException;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.IdentifierNode;
import edu.udel.cis.vsl.abc.ast.node.IF.PairNode;
import edu.udel.cis.vsl.abc.ast.node.IF.SequenceNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.ContractNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.AbstractFunctionDefinitionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.TypeNode;
import edu.udel.cis.vsl.abc.token.IF.Source;

/**
 * An abstract function definition contains the information for an abstract
 * function (i.e. a function in the mathematical sense, treated as uninterpreted
 * in the code).
 * 
 * An abstract function has an identifier, return type, parameters, and an
 * integer specifying the number of partial derivatives that may be taken.
 * 
 * @author zirkel
 *
 */
public class CommonAbstractFunctionDefinitionNode
		extends
			CommonFunctionDeclarationNode
		implements
			AbstractFunctionDefinitionNode {

	/**
	 * The number of partial derivatives (in total, of any parameters) that may
	 * be taken for this abstract function.
	 */
	private int continuity;

	/**
	 * Children: 0: identifier; 1: type; 2: contract; 3:intervals.
	 * 
	 * @param source
	 * @param identifier
	 * @param type
	 * @param contract
	 * @param continuity
	 * @param intervals
	 */
	public CommonAbstractFunctionDefinitionNode(Source source,
			IdentifierNode identifier, TypeNode type,
			SequenceNode<ContractNode> contract, int continuity,
			SequenceNode<PairNode<ExpressionNode, ExpressionNode>> intervals) {
		super(source, identifier, type, contract);
		this.continuity = continuity;
		addChild(intervals); // child 3
	}

	@Override
	public AbstractFunctionDefinitionNode copy() {
		CommonAbstractFunctionDefinitionNode result = new CommonAbstractFunctionDefinitionNode(
				getSource(), duplicate(getIdentifier()),
				duplicate(getTypeNode()), duplicate(getContract()), continuity,
				duplicate(getIntervals()));

		result.setInlineFunctionSpecifier(hasInlineFunctionSpecifier());
		result.setNoreturnFunctionSpecifier(hasNoreturnFunctionSpecifier());
		copyStorage(result);
		return result;
	}

	@Override
	public int continuity() {
		return continuity;
	}

	@Override
	public OrdinaryDeclarationKind ordinaryDeclarationKind() {
		return OrdinaryDeclarationKind.ABSTRACT_FUNCTION_DEFINITION;
	}

	@SuppressWarnings("unchecked")
	@Override
	public SequenceNode<PairNode<ExpressionNode, ExpressionNode>> getIntervals() {
		return (SequenceNode<PairNode<ExpressionNode, ExpressionNode>>) child(
				3);
	}

	@Override
	public ASTNode setChild(int index, ASTNode child) {
		if (index >= 4)
			throw new ASTException(
					"CommonAbstractFunctionDefinitionNode has only four children, but saw index "
							+ index);
		if (index == 3 && !(child == null || child instanceof SequenceNode))
			throw new ASTException(
					"Child of CommonAbstractFunctionDefinitionNode at index "
							+ index + " must be a SequenceNode, but saw "
							+ child + " with type " + child.nodeKind());

		return super.setChild(index, child);
	}
}
