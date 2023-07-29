package edu.udel.cis.vsl.abc.front.IF;

import org.antlr.runtime.TokenStream;

import edu.udel.cis.vsl.abc.config.IF.Configurations.Language;
import edu.udel.cis.vsl.abc.token.IF.CivlcToken.TokenVocabulary;

/**
 * A Converter rephrases a token stream output from a C Preprocessor. <br>
 * The major task is to recognize keywords from identifiers based on the grammar
 * specification of a given programming {@link Language}. (Note that the token
 * stream is processed from a C Preprocessor with its punctuator system, so that
 * a set of token concatenation and division will be performed for other
 * programming languages.)
 * 
 * @author Wenhao Wu (xxxx@udel.edu)
 */
public interface PP2CivlcTokenConverter {

	/**
	 * Process through an input {@link TokenStream} for recognizing all possible
	 * keyword tokens from identifier ones. Also, all processed tokens'
	 * {@link TokenVocabulary} will be changed from 'PREPROC' to a corresponding
	 * language.
	 * 
	 * @param stream
	 *            A {@link CivlcToken} stream from a C preprocessor
	 * @return A {@link CivlcToken} stream.
	 */
	TokenStream convert(TokenStream stream);
}
