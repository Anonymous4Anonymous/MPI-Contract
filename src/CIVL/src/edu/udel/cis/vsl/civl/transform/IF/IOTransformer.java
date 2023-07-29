package edu.udel.cis.vsl.civl.transform.IF;

import edu.udel.cis.vsl.abc.ast.IF.AST;
import edu.udel.cis.vsl.abc.ast.IF.ASTFactory;
import edu.udel.cis.vsl.abc.token.IF.SyntaxException;
import edu.udel.cis.vsl.abc.transform.IF.BaseTransformer;
import edu.udel.cis.vsl.civl.config.IF.CIVLConfiguration;
import edu.udel.cis.vsl.civl.transform.common.IOWorker;

/**
 * The IO transformer transforms<br>
 * <ul>
 * <li>all function calls printf(...) into frpintf(stdout, ...)</li>
 * <li>all function calls scanf(...) into fscanf(stdin, ...)</li>
 * <li>all function calls fopen(...) into $fopen(...)</li>
 * </ul>
 * 
 * @author zxxxx
 * 
 */
public class IOTransformer extends BaseTransformer {

	/*
	 * ************************** Public Static Fields ***********************
	 */
	/**
	 * The code (short name) of this transformer.
	 */
	public final static String CODE = "io";

	/**
	 * The long name of the transformer.
	 */
	public final static String LONG_NAME = "IOTransformer";

	/**
	 * The description of this transformer.
	 */
	public final static String SHORT_DESCRIPTION = "transforms C program with IO to CIVL-C";

	private CIVLConfiguration config;

	/**
	 * Creates a new instance of IO transformer.
	 * 
	 * @param astFactory
	 *            The AST factory to be used.
	 * @param inputVariables
	 *            The input variables specified from command line.
	 * @param config
	 *            The CIVL configuration.
	 */
	public IOTransformer(ASTFactory astFactory, CIVLConfiguration config) {
		super(IOTransformer.CODE, IOTransformer.LONG_NAME,
				IOTransformer.SHORT_DESCRIPTION, astFactory);
		this.config = config;
	}

	@Override
	public AST transform(AST ast) throws SyntaxException {
		return new IOWorker(astFactory, config).transform(ast);
	}

}
