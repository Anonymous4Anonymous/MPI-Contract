package edu.udel.cis.vsl.civl.transform.IF;

import edu.udel.cis.vsl.abc.ast.IF.AST;
import edu.udel.cis.vsl.abc.ast.IF.ASTFactory;
import edu.udel.cis.vsl.abc.token.IF.SyntaxException;
import edu.udel.cis.vsl.abc.transform.IF.BaseTransformer;
import edu.udel.cis.vsl.civl.transform.common.ShortCircuitTransformerWorker;

public class ShortCircuitTransformer extends BaseTransformer {

	public final static String CODE = "short-circuit";
	public final static String LONG_NAME = "shortCircuitTransformer";
	public final static String SHORT_DESCRIPTION = "transforms logical AND, logical OR and logical IMPLIES away to lift short circuit evaluation in CIVL backend";
	public final static String PREFIX = "_shcc_";

	public ShortCircuitTransformer(ASTFactory astFactory) {
		super(CODE, LONG_NAME, SHORT_DESCRIPTION, astFactory);
	}

	@Override
	public AST transform(AST ast) throws SyntaxException {
		return new ShortCircuitTransformerWorker(code, astFactory)
				.transform(ast);
	}
}
