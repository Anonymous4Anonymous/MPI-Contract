package edu.udel.cis.vsl.abc.ast.node.common.acsl;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.SeparatedNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.common.expression.CommonExpressionNode;
import edu.udel.cis.vsl.abc.token.IF.Source;

public class CommonSeparatedNode extends CommonExpressionNode implements SeparatedNode {

	public CommonSeparatedNode(Source source, List<ExpressionNode> arguments) {
		super(source, arguments);
	}

	@Override
	public ExpressionNode copy() {
		List<ExpressionNode> disjointTermSetCopy = new LinkedList<>();

		for (ExpressionNode termSet : this.getDisjointTermSets())
			disjointTermSetCopy.add(termSet.copy());
		return new CommonSeparatedNode(this.getSource(), disjointTermSetCopy);
	}

	@Override
	public ExpressionKind expressionKind() {
		return ExpressionKind.SEPARATED;
	}

	@Override
	public boolean isConstantExpression() {
		return false;
	}

	@Override
	public boolean isSideEffectFree(boolean errorsAreSideEffects) {
		for (ExpressionNode tset : this.getDisjointTermSets())
			if (!tset.isSideEffectFree(errorsAreSideEffects))
				return false;
		return true;
	}

	@Override
	public List<ExpressionNode> getDisjointTermSets() {
		List<ExpressionNode> result = new LinkedList<>();

		for (ASTNode child : children())
			result.add((ExpressionNode) child);
		return result;
	}

	@Override
	protected void printBody(PrintStream out) {
		out.print(this.prettyRepresentation());
	}
	
	@Override
	public StringBuffer prettyRepresentation() {
		return prettyRepresentation(-1);
	}
	
	@Override
	public StringBuffer prettyRepresentation(int maxLength) {
		StringBuffer sb = new StringBuffer();
		int len = 0;
		String str = "\\separated(";

		sb.append(str);
		len += str.length();
		testAndNewLine(sb, len, maxLength);
		
		int numArgs = this.numChildren();
		ExpressionNode arg;
		
		for (int i = 0; i < numArgs - 1; i++) {
			arg = (ExpressionNode) child(i);
			str = arg.prettyRepresentation().toString();
			sb.append(str + ", ");
			len += str.length() + 2;
			testAndNewLine(sb, len, maxLength);
		}
		arg = (ExpressionNode) child(numArgs - 1);
		str = arg.prettyRepresentation().toString();
		sb.append(str + "}");
		return sb;
	}
	
	//TODO: make this method a common part
	private static int testAndNewLine(StringBuffer sb, int len, int maxLen) {
		if (maxLen >= 0 && len > maxLen) {
			sb.append("\n");
			return 0;
		}
		return len;
	}
}
