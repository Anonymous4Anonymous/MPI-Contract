package edu.udel.cis.vsl.abc.ast.node.common.statement;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.udel.cis.vsl.abc.ast.IF.ASTException;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.LabeledStatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.StatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.SwitchNode;
import edu.udel.cis.vsl.abc.token.IF.Source;

public class CommonSwitchNode extends CommonStatementNode
		implements
			SwitchNode {

	private List<LabeledStatementNode> cases = new LinkedList<LabeledStatementNode>();

	private LabeledStatementNode defaultCase = null;

	public CommonSwitchNode(Source source, ExpressionNode condition,
			StatementNode body) {
		super(source, condition, body);
	}

	@Override
	public ExpressionNode getCondition() {
		return (ExpressionNode) child(0);
	}

	@Override
	public StatementNode getBody() {
		return (StatementNode) child(1);
	}

	@Override
	public Iterator<LabeledStatementNode> getCases() {
		return cases.iterator();
	}

	@Override
	public LabeledStatementNode getDefaultCase() {
		return defaultCase;
	}

	@Override
	public void setDefaultCase(LabeledStatementNode statement) {
		defaultCase = statement;
	}

	@Override
	public void addCase(LabeledStatementNode statement) {
		cases.add(statement);
	}

	/**
	 * The number of cases in this switch statement, NOT including the "default"
	 * case.
	 * 
	 * @return number of cases not including default
	 */
	public int getNumCases() {
		return cases.size();
	}

	@Override
	protected void printBody(PrintStream out) {
		out.print("Switch");
	}

	@Override
	public SwitchNode copy() {
		return new CommonSwitchNode(getSource(), duplicate(getCondition()),
				duplicate(getBody()));
	}

	/**
	 * Removes cases and default case.
	 */
	@Override
	public void clear() {
		cases = new LinkedList<LabeledStatementNode>();
		defaultCase = null;
	}

	@Override
	public StatementKind statementKind() {
		return StatementKind.SWITCH;
	}

	@Override
	public ASTNode setChild(int index, ASTNode child) {
		if (!(index >= 0 && index < 2))
			throw new ASTException(
					"CommonSwitchNode has only two children, but saw index "
							+ index);
		if (index == 0 && !(child == null || child instanceof ExpressionNode))
			throw new ASTException("Child of CommonSwitchNode at index " + index
					+ "  must be a ExpressionNode, but saw " + child
					+ " with type " + child.nodeKind());
		if (index == 1 && !(child == null || child instanceof StatementNode)) {
			throw new ASTException("Child of CommonSwitchNode at index " + index
					+ "  must be a StatementNode, but saw " + child
					+ " with type " + child.nodeKind());
		}
		return super.setChild(index, child);
	}
}
