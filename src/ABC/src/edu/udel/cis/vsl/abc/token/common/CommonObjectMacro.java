package edu.udel.cis.vsl.abc.token.common;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.Tree;

import edu.udel.cis.vsl.abc.token.IF.ObjectMacro;
import edu.udel.cis.vsl.abc.token.IF.SourceFile;

public class CommonObjectMacro extends CommonMacro implements ObjectMacro {

	public CommonObjectMacro(Tree definitionNode, SourceFile file) {
		super(definitionNode, file);
	}

	@Override
	public Tree getBodyNode() {
		return definitionNode.getChild(1);
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer("ObjectMacro[" + getName() + "=");
		int numReplacements = getNumReplacements();

		for (int i = 0; i < numReplacements; i++) {
			ReplacementUnit unit = getReplacementUnit(i);

			buf.append(unit.token.getText());
			for (Token t : unit.whitespace)
				buf.append(t.getText());
		}
		buf.append("]");
		return buf.toString();
	}

	@Override
	public boolean equals(Object object) {
		return object instanceof CommonObjectMacro && super.equals(object);
	}

	@Override
	protected ReplacementUnit makeReplacement(int index, Token token,
			Token[] whitespace) {
		return new ReplacementUnit(index, token, whitespace);
	}
}
