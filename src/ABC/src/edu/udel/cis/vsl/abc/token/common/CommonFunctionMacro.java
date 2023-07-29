package edu.udel.cis.vsl.abc.token.common;

import java.util.HashMap;
import java.util.Map;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;

import edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorLexer;
import edu.udel.cis.vsl.abc.token.IF.FunctionMacro;
import edu.udel.cis.vsl.abc.token.IF.SourceFile;

public class CommonFunctionMacro extends CommonMacro implements FunctionMacro {

	private boolean variadic = false;

	public CommonFunctionMacro(Tree definitionNode, SourceFile file) {
		super(definitionNode, file);
		initialize();
	}

	private void initialize() {
		int numFormals = getNumFormals();
		int numReplacements = getNumReplacements();
		Map<String, Integer> nameMap = new HashMap<String, Integer>(numFormals);

		for (int i = 0; i < numFormals; i++) {
			Token formal = getFormal(i);
			String formalName;

			if (i == numFormals - 1 && "...".equals(formal.getText())) {
				formalName = "__VA_ARGS__";
				variadic = true;
			} else
				formalName = formal.getText();
			nameMap.put(formalName, i);

		}
		for (int j = 0; j < numReplacements; j++) {
			FunctionReplacementUnit unit = getReplacementUnit(j);
			Token token = unit.token;

			if (token.getType() == PreprocessorLexer.IDENTIFIER) {
				String name = token.getText();
				Integer lookup = nameMap.get(name);

				if (lookup != null) {
					unit.formalIndex = lookup;
					continue;
				}
			}
			unit.formalIndex = -1;
		}
	}

	@Override
	public Tree getBodyNode() {
		return definitionNode.getChild(2);
	}

	public int getNumFormals() {
		return definitionNode.getChild(1).getChildCount();
	}

	public Token getFormal(int index) {
		return ((CommonTree) definitionNode.getChild(1).getChild(index))
				.getToken();
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer("FunctionMacro[" + getName() + "(");
		int numFormals = getNumFormals();
		int numReplacements = getNumReplacements();

		for (int j = 0; j < numFormals; j++) {
			if (j > 0)
				buf.append(',');
			buf.append(getFormal(j).getText());
		}
		buf.append(") =");
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
		if (this == object)
			return true;
		if (object instanceof CommonFunctionMacro) {
			CommonFunctionMacro that = (CommonFunctionMacro) object;

			if (!super.equals(that))
				return false;

			int numFormals = getNumFormals();

			if (numFormals != that.getNumFormals())
				return false;

			for (int i = 0; i < numFormals; i++) {
				Token t1 = getFormal(i);
				Token t2 = that.getFormal(i);

				if (t1.getType() != t2.getType()
						|| !t1.getText().equals(t2.getText()))
					return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public FunctionReplacementUnit getReplacementUnit(int index) {
		return (FunctionReplacementUnit) super.getReplacementUnit(index);
	}

	@Override
	protected ReplacementUnit makeReplacement(int index, Token token,
			Token[] whitespace) {
		return new FunctionReplacementUnit(index, token, whitespace);
	}

	@Override
	public boolean isVariadic() {
		return variadic;
	}

}
