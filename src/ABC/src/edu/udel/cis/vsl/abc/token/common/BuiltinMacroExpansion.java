package edu.udel.cis.vsl.abc.token.common;

import edu.udel.cis.vsl.abc.token.IF.CivlcToken;
import edu.udel.cis.vsl.abc.token.IF.Formation;
import edu.udel.cis.vsl.abc.token.IF.SourceFile;

public class BuiltinMacroExpansion implements Formation {

	private CivlcToken macroToken;

	public BuiltinMacroExpansion(CivlcToken macroToken) {
		assert macroToken != null;
		this.macroToken = macroToken;
	}

	@Override
	public String suffix() {
		return "expanded from " + macroToken;
	}

	@Override
	public SourceFile getLastFile() {
		return macroToken.getSourceFile();
	}

}
