package edu.udel.cis.vsl.civl.transform.IF;

import edu.udel.cis.vsl.abc.ast.IF.AST;
import edu.udel.cis.vsl.abc.ast.IF.ASTFactory;
import edu.udel.cis.vsl.abc.token.IF.SyntaxException;
import edu.udel.cis.vsl.abc.transform.IF.BaseTransformer;
import edu.udel.cis.vsl.civl.config.IF.CIVLConfiguration;
import edu.udel.cis.vsl.civl.transform.common.SvcompWorker;

/**
 * Transforms svcomp programs which are preprocessed by GCC
 * 
 * @author Manchun Zheng
 *
 */
public class SvcompTransformer extends BaseTransformer {

	// notes on deciding the upper bound of the scale parameter:
	// pthread-ext/25_stack_... require that the value is (2*pow(2)+1)
	// pthread-C-DAC/pthread-finding-k-matches_true-unreach-call.i requires it
	// to be even number
	public static final int UNPP_SCALE_ODD = 3;
	public static final int UNPP_SCALE_EVEN = 4;
	public static final int UNSIGNED_BOUND = 4;
	public static final int INT_BOUND_UP = 5;
	public static final String INT_BOUND_UP_NAME = "_svcomp_int_bound_up";
	// public static final String INT_BOUND_LO_NAME = "_svcomp_int_bound_lo";
	public static final String UNSIGNED_BOUND_NAME = "_svcomp_unsigned_bound";

	public static final int INT_BOUND_LO = -5;

	/**
	 * The code (short name) of this transformer.
	 */
	public final static String CODE = "svcomp";

	/**
	 * The long name of the transformer.
	 */
	public static String LONG_NAME = "SvcompTransformer";

	/**
	 * The description of this transformer.
	 */
	public static String SHORT_DESCRIPTION = "transforms (unpreprocessed) programs from SVCOMP benchmarks to CIVL-C";

	private CIVLConfiguration config;

	public SvcompTransformer(ASTFactory astFactory, CIVLConfiguration config) {
		super(CODE, LONG_NAME, SHORT_DESCRIPTION, astFactory);
		this.config = config;
	}

	@Override
	public AST transform(AST ast) throws SyntaxException {
		return new SvcompWorker(astFactory, config).transform(ast);
	}

}
