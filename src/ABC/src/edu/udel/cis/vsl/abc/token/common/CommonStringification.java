package edu.udel.cis.vsl.abc.token.common;

import java.util.ArrayList;

import edu.udel.cis.vsl.abc.token.IF.CivlcToken;
import edu.udel.cis.vsl.abc.token.IF.FunctionMacro;
import edu.udel.cis.vsl.abc.token.IF.SourceFile;
import edu.udel.cis.vsl.abc.token.IF.Stringification;

public class CommonStringification implements Stringification {

	private FunctionMacro macro;

	private int index;

	private ArrayList<CivlcToken> argument;

	public CommonStringification(FunctionMacro macro, int index,
			ArrayList<CivlcToken> argument) {
		this.macro = macro;
		this.index = index;
		this.argument = argument;
	}

	@Override
	public String suffix() {
		return "from " + argument;
	}

	@Override
	public SourceFile getLastFile() {
		return macro.getFile();
	}

	@Override
	public FunctionMacro getMacro() {
		return macro;
	}

	@Override
	public int getReplacementTokenIndex() {
		return index;
	}

	@Override
	public int getNumArgumentTokens() {
		return argument.size();
	}

	@Override
	public CivlcToken getArgumentToken(int index) {
		return argument.get(index);
	}

	@Override
	public String toString() {
		return "Stringification[" + macro + ", " + index + ", " + argument
				+ "]";
	}

}
