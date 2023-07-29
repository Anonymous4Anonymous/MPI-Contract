package edu.udel.cis.vsl.abc.front.IF;

import edu.udel.cis.vsl.abc.token.IF.CivlcToken;
import edu.udel.cis.vsl.abc.token.IF.CivlcTokenSource;

/**
 * A {@link Parser} is used to parse a {@link CivlcTokenSource} --- a stream of
 * {@link CivlcToken}s --- and create a {@link ParseTree}.
 * 
 * @author xxxx
 */
public interface Parser {

	/**
	 * Returns the parse tree resulting from parsing the input, after some
	 * "post-processing" has been done to the tree to fill in some fields.
	 * 
	 * @return the parse tree resulting from parsing and clean up
	 * @throws ParseException
	 *             if there is a syntax exception
	 */
	ParseTree parse(CivlcTokenSource tokenSource) throws ParseException;

}
