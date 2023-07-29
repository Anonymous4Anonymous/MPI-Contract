package edu.udel.cis.vsl.abc.front.c.parse;

import java.util.Stack;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;

import edu.udel.cis.vsl.abc.config.IF.Configurations.Language;
import edu.udel.cis.vsl.abc.err.IF.ABCRuntimeException;
import edu.udel.cis.vsl.abc.front.IF.ParseException;
import edu.udel.cis.vsl.abc.front.IF.Parser;
import edu.udel.cis.vsl.abc.front.IF.PreprocessorRuntimeException;
import edu.udel.cis.vsl.abc.front.IF.RuntimeParseException;
import edu.udel.cis.vsl.abc.front.c.ptree.CParseTree;
import edu.udel.cis.vsl.abc.front.common.parse.TreeUtils;
import edu.udel.cis.vsl.abc.token.IF.CivlcTokenSource;

// why not make static methods of all these?
/**
 * <p>
 * Simple interface for a parser of C programs. It includes a bunch of integer
 * constants which are ID numbers of each kind of token (real or fake) that can
 * occur in a C parse tree.
 * </p>
 * 
 * 
 * @author xxxx
 * 
 */
public class CParser implements Parser {
	/**
	 * Kind of parsing rule
	 * 
	 * @author Manchun Zheng
	 *
	 */
	public static enum RuleKind {
		/**
		 * the rule for translating a translation unit
		 */
		TRANSLATION_UNIT,
		/**
		 * the rule for translating a block item
		 */
		BLOCK_ITEM,
		/**
		 * ACSL contracts
		 */
		CONTRACT
	}

	/* *************************** Constructors *************************** */

	public CParser() {

	}

	/* ************************* CParser Methods ************************** */

	/**
	 * Returns the ANTLR CommonTree resulting from parsing the input, after some
	 * "post-processing" has been done to the tree to fill in some fields.
	 * 
	 * @return the ANTLR tree that results from parsing
	 * @throws ParseException
	 *             if something goes wrong parsing the input
	 */
	public CParseTree parse(RuleKind rule, CivlcTokenSource tokenSource,
			Stack<ScopeSymbols> symbols) throws ParseException {
		TokenStream stream = new CommonTokenStream(tokenSource);
		CivlCParser parser = new CivlCParser(stream);
		PP2CivlcTokenCConverter pp2cConverter = new PP2CivlcTokenCConverter();
		CommonTree root;

		parser.setSymbols_stack(symbols);
		stream = pp2cConverter.convert(stream);
		try {
			switch (rule) {
				case TRANSLATION_UNIT :
					try {
						root = (CommonTree) parser.translationUnit().getTree();
					} catch (PreprocessorRuntimeException ex) {
						throw new ParseException(ex.getMessage(),
								ex.getToken());
					}
					break;
				case BLOCK_ITEM :
					root = (CommonTree) parser.blockItemWithScope().getTree();
					break;
				// TODO: confusing there is no case for CONTRACT.
				// That is because this method will not be used to parse
				// contracts.
				default :
					throw new ABCRuntimeException("Unreachable");
			}
			TreeUtils.postProcessTree(root);
		} catch (RecognitionException e) {
			throw new ParseException(e.getMessage(), e.token);
		} catch (RuntimeParseException e) {
			throw new ParseException(e.getMessage());
		}
		return new CParseTree(Language.CIVL_C, rule, tokenSource, root);
	}

	@Override
	public CParseTree parse(CivlcTokenSource tokenSource)
			throws ParseException {
		return parse(RuleKind.TRANSLATION_UNIT, tokenSource,
				new Stack<ScopeSymbols>());
	}

}
