package edu.udel.cis.vsl.abc.token.common;

import java.util.ArrayList;
import java.util.LinkedList;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;

import edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorUtils;
import edu.udel.cis.vsl.abc.token.IF.Macro;
import edu.udel.cis.vsl.abc.token.IF.SourceFile;

/**
 * Root class for representing preprocessor {@link Macro}s. This is an abstract
 * class. It has concrete subclasses {@link CommonObjectMacro} (for object-like
 * macros) and {@link CommonFunctionMacro} (for function-like macros).
 * 
 * @author xxxx
 * 
 */
public abstract class CommonMacro implements Macro {

	protected Tree definitionNode;

	protected SourceFile file;

	/**
	 * The replacement "units". Each includes one non-whitespace preprocessing
	 * token, and some possible whitespace after that token.
	 */
	protected ReplacementUnit[] replacements;

	protected CommonMacro(Tree definitionNode, SourceFile file) {
		this.definitionNode = definitionNode;
		this.file = file;

		Tree bodyNode = getBodyNode();
		int numChildren = bodyNode.getChildCount(); // includes ws
		ArrayList<ReplacementUnit> replacementVec = new ArrayList<>();
		int index = 0;

		// ignore any whitespace at beginning of body (should not exist):
		while (index < numChildren && PreprocessorUtils.isWhiteSpace(
				((CommonTree) bodyNode.getChild(index)).getToken()))
			index++;
		while (index < numChildren) {
			Token token = ((CommonTree) bodyNode.getChild(index)).getToken();
			LinkedList<Token> ws = new LinkedList<>();
			Token t;

			assert !PreprocessorUtils.isWhiteSpace(token);
			index++;
			while (index < numChildren) {
				t = ((CommonTree) bodyNode.getChild(index)).getToken();
				if (PreprocessorUtils.isWhiteSpace(t))
					ws.add(t);
				else
					break;
				index++;
			}

			ReplacementUnit unit = makeReplacement(replacementVec.size(), token,
					ws.toArray(new Token[ws.size()]));

			replacementVec.add(unit);
		}
		replacements = replacementVec
				.toArray(new ReplacementUnit[replacementVec.size()]);
	}

	protected abstract ReplacementUnit makeReplacement(int index, Token token,
			Token[] whitespace);

	@Override
	public SourceFile getFile() {
		return file;
	}

	@Override
	public Tree getDefinitionNode() {
		return definitionNode;
	}

	@Override
	public ReplacementUnit getReplacementUnit(int i) {
		return replacements[i];
	}

	@Override
	public String getName() {
		return definitionNode.getChild(0).getText();
	}

	@Override
	public int getNumReplacements() {
		return replacements.length;
	}

	protected boolean equals(ReplacementUnit unit1, ReplacementUnit unit2) {
		Token t1 = unit1.token, t2 = unit2.token;

		if (t1.getType() != t2.getType() || !t1.getText().equals(t2.getText()))
			return false;
		return (unit1.whitespace.length == 0) == (unit2.whitespace.length == 0);
	}

	/**
	 * 
	 * C11 6.10.3.1: "Two replacement lists are identical if and only if the
	 * preprocessing tokens in both have the same number, ordering, spelling,
	 * and white-space separation, where all white-space separations are
	 * considered identical."
	 * 
	 * @param object
	 * @return
	 */
	@Override
	public boolean equals(Object object) {
		if (object instanceof Macro) {
			Macro that = (Macro) object;

			if (!getName().equals(that.getName()))
				return false;

			int numReplacements = getNumReplacements();

			if (numReplacements != that.getNumReplacements())
				return false;
			for (int i = 0; i < numReplacements; i++) {
				if (!equals(this.getReplacementUnit(i),
						that.getReplacementUnit(i)))
					return false;
			}
			return true;
		}
		return false;
	}
}
