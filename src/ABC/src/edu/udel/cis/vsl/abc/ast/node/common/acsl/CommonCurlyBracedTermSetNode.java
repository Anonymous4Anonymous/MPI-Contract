package edu.udel.cis.vsl.abc.ast.node.common.acsl;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.SequenceNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.CurlyBracedTermSetNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.VariableDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.common.expression.CommonExpressionNode;
import edu.udel.cis.vsl.abc.token.IF.Source;

public class CommonCurlyBracedTermSetNode extends CommonExpressionNode
		implements CurlyBracedTermSetNode {

	private final boolean isExplicit;

	public CommonCurlyBracedTermSetNode(Source source, List<ExpressionNode> explicitElements) {
		super(source, explicitElements);
		isExplicit = true;
	}

	public CommonCurlyBracedTermSetNode(Source source, SequenceNode<ExpressionNode> terms, SequenceNode<VariableDeclarationNode> binders, ExpressionNode predicate) {
		super(source, terms, binders, predicate);
		isExplicit = false;
	}

	@Override
	public ExpressionNode copy() {
		if (isExplicit) {
			List<ExpressionNode> copiedExplicitEles = new LinkedList<>();

			for (ExpressionNode ele : getExplicitElements())
				copiedExplicitEles.add(ele.copy());
			return new CommonCurlyBracedTermSetNode(getSource(),
					copiedExplicitEles);
		} else
			return new CommonCurlyBracedTermSetNode(getSource(),
					getSetComprehensionTerms().copy(), getBinders().copy(),
					getPredicate().copy());
	}

	@Override
	public ExpressionKind expressionKind() {
		return ExpressionKind.CURLY_BRACED_TERM_SET;
	}

	@Override
	public boolean isConstantExpression() {
		return false;
	}

	@Override
	public boolean isSideEffectFree(boolean errorsAreSideEffects) {
		if (isExplicit) {
			for (ExpressionNode element : getExplicitElements())
				if (!element.isSideEffectFree(errorsAreSideEffects))
					return false;
			return true;
		} else {
			for (ExpressionNode term : getSetComprehensionTerms())
				if (!term.isSideEffectFree(errorsAreSideEffects))
					return false;
			return getPredicate().isSideEffectFree(errorsAreSideEffects);
		}
	}

	@Override
	public boolean isExplicit() {
		return isExplicit;
	}

	@Override
	public List<ExpressionNode> getExplicitElements() {
		assert isExplicit;
		List<ExpressionNode> result = new LinkedList<>();

		for (ASTNode child : children())
			result.add((ExpressionNode) child);
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public SequenceNode<ExpressionNode> getSetComprehensionTerms() {
		assert !isExplicit;
		return (SequenceNode<ExpressionNode>) this.child(0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public SequenceNode<VariableDeclarationNode> getBinders() {
		assert !isExplicit;
		return (SequenceNode<VariableDeclarationNode>) this.child(1);
	}

	@Override
	public ExpressionNode getPredicate() {
		assert !isExplicit;
		return (ExpressionNode) this.child(2);
	}

	@Override
	public boolean isLvalue() {
		if (isExplicit) {
			for (ExpressionNode element : getExplicitElements())
				if (!element.isLvalue())
					return false;
		} else
			for (ExpressionNode term : getSetComprehensionTerms())
				if (!term.isLvalue())
					return false;
		return true;
	}

	@Override
	protected void printBody(PrintStream out) {
		out.print(prettyRepresentation());
	}

	@Override
	public StringBuffer prettyRepresentation() {
		return prettyRepresentation(-1);
	}

	@Override
	public StringBuffer prettyRepresentation(int maxLength) {
		StringBuffer result = new StringBuffer();
		int len = 0;
		StringBuffer childPretty;

		if (isExplicit) {
			result.append("{");
			len++;

			int numChildren = numChildren();

			for (int i = 0; i < numChildren - 1; i++) {
				childPretty = child(i).prettyRepresentation();
				result.append(childPretty);
				len += childPretty.length();
				result.append(", ");
				len++;
				len = testAndNewLine(result, len, maxLength);
			}
			childPretty = child(numChildren - 1).prettyRepresentation();
			result.append(childPretty);
			result.append("}");
			len += childPretty.length() + 1;
		} else {
			result.append("{");
			len++;
			childPretty = getSetComprehensionTerms().prettyRepresentation();
			childPretty.append(" | ");
			result.append(childPretty);
			len += childPretty.length();
			len = testAndNewLine(result, len, maxLength);
			childPretty = getBinders().prettyRepresentation();
			childPretty.append("; ");
			result.append(childPretty);
			len += childPretty.length();
			len = testAndNewLine(result, len, maxLength);
			childPretty = getPredicate().prettyRepresentation();
			result.append(childPretty);
			result.append("}");
			len += childPretty.length() + 1;
		}
		len = testAndNewLine(result, len, maxLength);
		return result;
	}

	private static int testAndNewLine(StringBuffer sb, int len, int maxLen) {
		if (maxLen >= 0 && len > maxLen) {
			sb.append("\n");
			return 0;
		}
		return len;
	}
}
