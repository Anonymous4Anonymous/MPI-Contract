package edu.udel.cis.vsl.abc.ast.node.common;

import java.io.PrintStream;
import java.util.Arrays;

import edu.udel.cis.vsl.abc.ast.IF.ASTException;
import edu.udel.cis.vsl.abc.ast.IF.DifferenceObject;
import edu.udel.cis.vsl.abc.ast.IF.DifferenceObject.DiffKind;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.IdentifierNode;
import edu.udel.cis.vsl.abc.ast.node.IF.PragmaNode;
import edu.udel.cis.vsl.abc.token.IF.CivlcToken;
import edu.udel.cis.vsl.abc.token.IF.CivlcTokenSequence;
import edu.udel.cis.vsl.abc.token.IF.CivlcTokenSource;
import edu.udel.cis.vsl.abc.token.IF.Source;

public class CommonPragmaNode extends CommonASTNode implements PragmaNode {

	protected CivlcToken[] body;

	protected CivlcTokenSequence tokenSequence;

	protected CivlcToken newlineToken;

	public CommonPragmaNode(Source source, IdentifierNode identifier,
			CivlcTokenSequence tokenSequence, CivlcToken newlineToken) {
		super(source, identifier);
		this.tokenSequence = tokenSequence;
		this.newlineToken = newlineToken;
		body = tokenSequence.getTokens();
	}

	@Override
	protected void printBody(PrintStream out) {
		int numTokens = body.length;

		out.print("Pragma[");
		for (int i = 0; i < numTokens; i++) {
			CivlcToken token = body[i];

			if (i > 0)
				out.print(" ");
			out.print(token.getText());
		}
		out.print("]");
	}

	@Override
	public IdentifierNode getPragmaIdentifier() {
		return (IdentifierNode) child(0);
	}

	@Override
	public int getNumTokens() {
		return body.length;
	}

	@Override
	public CivlcToken getToken(int index) {
		return body[index];
	}

	@Override
	public Iterable<CivlcToken> getTokens() {
		return Arrays.asList(body);
	}

	@Override
	public PragmaNode copy() {
		return new CommonPragmaNode(getSource(),
				duplicate(getPragmaIdentifier()), tokenSequence, newlineToken);
	}

	@Override
	public NodeKind nodeKind() {
		return NodeKind.PRAGMA;
	}

	@Override
	public StatementKind statementKind() {
		return StatementKind.PRAGMA;
	}

	@Override
	public BlockItemKind blockItemKind() {
		return BlockItemKind.PRAGMA;
	}

	@Override
	protected DifferenceObject diffWork(ASTNode that) {
		if (that instanceof PragmaNode) {
			PragmaNode thatPragma = (PragmaNode) that;
			int numTokens = this.getNumTokens();

			if (numTokens != thatPragma.getNumTokens())
				return new DifferenceObject(this, that,
						DiffKind.PRAGMA_NUM_TOKENS);
			for (int i = 0; i < numTokens; i++) {
				String thisToken = this.getToken(i).getText(),
						thatToken = thatPragma.getToken(i).getText();

				if (!thisToken.equals(thatToken))
					return new DifferenceObject(this, that, DiffKind.OTHER,
							"the " + i + " token is different: " + thisToken
									+ " vs " + thatToken);
			}
			return null;
		}
		return new DifferenceObject(this, that);
	}

	@Override
	public CivlcTokenSource newTokenSource() {
		return tokenSequence.newSource();
	}

	@Override
	public CivlcToken getNewlineToken() {
		return newlineToken;
	}

	@Override
	public ASTNode setChild(int index, ASTNode child) {
		if (index != 0)
			throw new ASTException(
					"CommonPragmaNode has only one child, but saw index "
							+ index);
		if (!(child == null || child instanceof IdentifierNode))
			throw new ASTException(
					"Child of CommonPragmaNode must be a IdentifierNode, but saw "
							+ child + " with type " + child.nodeKind());
		return super.setChild(index, child);
	}
}
