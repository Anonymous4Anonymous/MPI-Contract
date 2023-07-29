package edu.udel.cis.vsl.civl.transform.IF;

import edu.udel.cis.vsl.abc.ast.IF.AST;
import edu.udel.cis.vsl.abc.ast.IF.ASTFactory;
import edu.udel.cis.vsl.abc.token.IF.SyntaxException;
import edu.udel.cis.vsl.abc.transform.IF.BaseTransformer;
import edu.udel.cis.vsl.civl.config.IF.CIVLConfiguration;
import edu.udel.cis.vsl.civl.transform.common.DirectingWorker;

/**
 * This transformer instruments the model so as to direct
 * the symbolic execution down a subset of program paths.
 * 
 * @author dwyer
 * 
 */
public class DirectingTransformer extends BaseTransformer {

	public final static String CODE = "direct";
	public final static String LONG_NAME = "DirectedSymEx";
	public final static String SHORT_DESCRIPTION = "instruments model to direct sybolic execution";
	private CIVLConfiguration config;

	public DirectingTransformer(ASTFactory astFactory, CIVLConfiguration config) {
		super(CODE, LONG_NAME, SHORT_DESCRIPTION, astFactory);
		this.config = config;
	}

	@Override
	public AST transform(AST ast) throws SyntaxException {
		return new DirectingWorker(astFactory, config).transform(ast);
	}

}
