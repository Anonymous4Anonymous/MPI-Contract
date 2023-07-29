package edu.udel.cis.vsl.abc.ast.node.common.statement;

import edu.udel.cis.vsl.abc.ast.IF.ASTException;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.SequenceNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.ContractNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.ForLoopInitializerNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.ForLoopNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.StatementNode;
import edu.udel.cis.vsl.abc.token.IF.Source;

public class CommonForLoopNode extends CommonLoopNode implements ForLoopNode {

	public CommonForLoopNode(Source source, ExpressionNode condition,
			StatementNode statement, ForLoopInitializerNode initializer,
			ExpressionNode incrementer, SequenceNode<ContractNode> contracts) {
		super(source, LoopKind.FOR, condition, statement, contracts);
		addChild(initializer); // child 3
		addChild(incrementer); // child 4
	}

	@Override
	public ForLoopInitializerNode getInitializer() {
		return (ForLoopInitializerNode) child(3);
	}

	@Override
	public ExpressionNode getIncrementer() {
		return (ExpressionNode) child(4);
	}

	@Override
	public ForLoopNode copy() {
		return new CommonForLoopNode(getSource(), duplicate(getCondition()),
				duplicate(getBody()), duplicate(getInitializer()),
				duplicate(getIncrementer()), duplicate(loopContracts()));
	}

	@Override
	public StatementKind statementKind() {
		return StatementKind.LOOP;
	}

	@Override
	public void setInitializer(ForLoopInitializerNode initNode) {
		setChild(3, initNode);
	}

	@Override
	public void setIncrementer(ExpressionNode node) {
		setChild(4, node);
	}

	@Override
	public ASTNode setChild(int index, ASTNode child) {
		if (index >= 5)
			throw new ASTException(
					"CommonForLoopNode has five children, but saw index "
							+ index);
		switch (index) {
			case 3 :
				if (!(child == null || child instanceof ForLoopInitializerNode))
					throw new ASTException(
							"Child of CommonForLoopNode at index " + index
									+ " must be an ForLoopInitializerNode, but saw "
									+ child + " with type " + child.nodeKind());
				break;
			case 4 :
				if (!(child == null || child instanceof ExpressionNode))
					throw new ASTException(
							"Child of CommonForLoopNode at index " + index
									+ " must be an ExpressionNode, but saw "
									+ child + " with type " + child.nodeKind());
				break;
		}
		return super.setChild(index, child);
	}
}
