package edu.udel.cis.vsl.civl.transform.IF;

import java.util.Map;

import edu.udel.cis.vsl.abc.ast.IF.AST;
import edu.udel.cis.vsl.abc.ast.IF.ASTFactory;
import edu.udel.cis.vsl.abc.token.IF.SyntaxException;
import edu.udel.cis.vsl.abc.transform.IF.BaseTransformer;
import edu.udel.cis.vsl.civl.config.IF.CIVLConfiguration;
import edu.udel.cis.vsl.civl.transform.common.IntOperationWorker;

/**
 * This transformer is used to
 * <ul>
 * <li>replace integer division ('/') and integer modulo ('%') in the program
 * with $int_div(int, int) and $int_mod(int, int) functions respectively</li>
 * <li>replace unsigned integer arithmetic operations with corresponding library
 * functions: int $unsigned_add, int $unsigned_subtract, int $unsigned_multiply
 * </li>
 * </ul>
 * 
 * @author xxxxxxxx
 *
 */
public class IntOperationTransformer extends BaseTransformer {
	/**
	 * The code (short name) of this transformer.
	 */
	public final static String CODE = "int_operation";
	/**
	 * The long name of the transformer.
	 */
	public final static String LONG_NAME = "IntOperationTransformer";
	/**
	 * The description of this transformer.
	 */
	public final static String SHORT_DESCRIPTION = "integer arithmetic operations to its corresponding library functions.";

	private Map<String, String> macros;

	private CIVLConfiguration civlConfig;

	public IntOperationTransformer(ASTFactory astFactory,
			Map<String, String> macros, CIVLConfiguration config) {
		super(CODE, LONG_NAME, SHORT_DESCRIPTION, astFactory);
		this.macros = macros;
		this.civlConfig = config;
	}

	@Override
	public AST transform(AST ast) throws SyntaxException {
		IntOperationWorker worker = new IntOperationWorker(astFactory, macros,
				civlConfig);

		return worker.transform(ast);
	}

}
