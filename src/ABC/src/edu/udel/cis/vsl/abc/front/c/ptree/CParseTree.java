package edu.udel.cis.vsl.abc.front.c.ptree;

import org.antlr.runtime.tree.CommonTree;

import edu.udel.cis.vsl.abc.config.IF.Configurations.Language;
import edu.udel.cis.vsl.abc.front.c.parse.CParser.RuleKind;
import edu.udel.cis.vsl.abc.front.common.ptree.CommonParseTree;
import edu.udel.cis.vsl.abc.token.IF.CivlcTokenSource;

public class CParseTree extends CommonParseTree {

	private RuleKind kind;

	public CParseTree(Language language, RuleKind kind,
			CivlcTokenSource tokenSource, CommonTree root) {
		super(language, tokenSource, root);
		this.kind = kind;
	}

	/**
	 * What kind of parse tree is this?
	 */
	public RuleKind getKind() {
		return kind;
	}
}
