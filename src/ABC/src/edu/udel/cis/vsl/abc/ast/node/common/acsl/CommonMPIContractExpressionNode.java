package edu.udel.cis.vsl.abc.ast.node.common.acsl;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

import edu.udel.cis.vsl.abc.ast.IF.ASTException;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.MPIContractAbsentEventNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.MPIContractExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.common.expression.CommonExpressionNode;
import edu.udel.cis.vsl.abc.token.IF.Source;

public class CommonMPIContractExpressionNode extends CommonExpressionNode
		implements
			MPIContractExpressionNode {

	private MPIContractExpressionKind kind;

	private int numArgs = -1;

	protected String exprName;

	public CommonMPIContractExpressionNode(Source source,
			List<ExpressionNode> arguments, MPIContractExpressionKind kind,
			String exprName) {
		super(source, arguments);
		this.kind = kind;
		this.exprName = exprName;
		this.numArgs = arguments.size();
	}

	@Override
	public ExpressionNode copy() {
		List<ExpressionNode> argCopy = new LinkedList<>();
		int numArgs = this.numArgs;

		for (int i = 0; i < numArgs; i++)
			argCopy.add(duplicate(getArgument(i)));
		return new CommonMPIContractExpressionNode(this.getSource(), argCopy,
				kind, exprName);
	}

	@Override
	public MPIContractExpressionKind MPIContractExpressionKind() {
		return kind;
	}

	@Override
	public ExpressionKind expressionKind() {
		return ExpressionKind.MPI_CONTRACT_EXPRESSION;
	}

	@Override
	public boolean isConstantExpression() {
		return false;
	}

	@Override
	public boolean isSideEffectFree(boolean errorsAreSideEffects) {
		return true;
	}

	@Override
	public int numArguments() {
		if (numArgs > 0)
			return numArgs;
		else {
			switch (kind) {
			case MPI_AGREE:
				numArgs = 1;
				break;
			case MPI_ABSENT:
				numArgs = 3;
				break;
			case MPI_ABSENT_EVENT:
				numArgs = 1;
				break;
			case MPI_BUF:
				numArgs = 3;
				break;
			case MPI_BACK_MESSAGES:
				numArgs = 0;
				break;
			case MPI_COMM_RANK:
				numArgs = 0;
				break;
			case MPI_COMM_SIZE:
				numArgs = 0;
				break;
			case MPI_EXTENT:
				numArgs = 1;
				break;
			case MPI_NONOVERLAPPING:
				numArgs = 1;
				break;
			case MPI_ON:
				numArgs = 2;
				break;
			case MPI_REDUCE:
				numArgs = 5;
				break;
			case MPI_SIG:
				numArgs = 1;
				break;
			default:
				assert false : "unreachable";
			}
		}
		return numArgs;
	}

	@Override
	public ExpressionNode getArgument(int index) {
		return (ExpressionNode) child(index);
	}

	@Override
	protected void printBody(PrintStream out) {
		out.print(kind);
	}

	@Override
	public ASTNode setChild(int index, ASTNode child) {
		if (kind == MPIContractExpressionKind.MPI_ABSENT) {
			if (!(child == null || child instanceof MPIContractAbsentEventNode))
				throw new ASTException("Child of MPIContractAbsentNode must be" +
									   " an instance of MPIContractAbsentEventNode.");
		} else if (!(child == null || child instanceof ExpressionNode))
			throw new ASTException(
					"Child of CommonMPIContractExpressionNode must be a ExpressionNode, but saw "
					+ child + " with type " + child.nodeKind());
		return super.setChild(index, child);
	}
}
