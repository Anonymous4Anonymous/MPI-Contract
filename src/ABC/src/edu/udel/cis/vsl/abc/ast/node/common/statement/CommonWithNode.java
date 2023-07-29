package edu.udel.cis.vsl.abc.ast.node.common.statement;

import java.io.PrintStream;

import edu.udel.cis.vsl.abc.ast.IF.ASTException;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.StatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.WithNode;
import edu.udel.cis.vsl.abc.token.IF.Source;

/**
 * An implementation of {@Link WithNode}. Structures of a CommonWithNode:
 * <li>Children 0: stateRef, 2: statement</li>
 * 
 * @author xxxx
 *
 */
public class CommonWithNode extends CommonStatementNode implements WithNode {

	private boolean isParallel = false;

	public CommonWithNode(Source source, ExpressionNode stateRef,
			StatementNode statement) {
		super(source, stateRef, statement);
	}

	public CommonWithNode(Source source, ExpressionNode stateRef,
			StatementNode statement, boolean isParallel) {
		super(source, stateRef, statement);
		this.isParallel = true;

	}

	@Override
	public ExpressionNode getStateReference() {
		return (ExpressionNode) child(0);
	}

	@Override
	public StatementNode getBodyNode() {
		return (StatementNode) child(1);
	}

	@Override
	public WithNode copy() {
		return new CommonWithNode(getSource(), duplicate(getStateReference()),
				duplicate(getBodyNode()));
	}

	@Override
	protected void printBody(PrintStream out) {
		out.print("WithNode");
	}

	@Override
	public StatementKind statementKind() {
		return StatementKind.WITH;
	}

	@Override
	public boolean isParallelStatement() {
		return isParallel;
	}

	@Override
	public ASTNode setChild(int index, ASTNode child) {
		if (index >= 2)
			throw new ASTException(
					"CommonWithNode has only two children, but saw index "
							+ index);
		if (index == 0 && !(child == null || child instanceof ExpressionNode))
			throw new ASTException("Child of CommonWithNode at index " + index
					+ "  must be a ExpressionNode, but saw " + child
					+ " with type " + child.nodeKind());
		if (index == 1 && !(child == null || child instanceof StatementNode)) {
			throw new ASTException("Child of CommonWithNode at index " + index
					+ "  must be a StatementNode, but saw " + child
					+ " with type " + child.nodeKind());
		}
		return super.setChild(index, child);
	}
}
