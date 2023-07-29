package edu.udel.cis.vsl.abc.token.IF;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.Tree;

/**
 * <p>
 * An abstract representation of a preprocessor macro. Used to represent both
 * object and function macros.
 * </p>
 * 
 * <p>
 * C11 5.1.1.2(3) states that in translation phase 3 (in which the source is
 * decomposed into preprocessing tokens): "Whether each nonempty sequence of
 * white-space characters other than new-line is retained or replaced by one
 * space character is implementation-defined." Note that in the vocabulary of
 * C11, white-space characters are not preprocessing tokens; they are used to
 * separate preprocessing tokens.
 * </p>
 * 
 * <p>
 * A {@link Macro} object maintains the sequence of all tokens in the macro
 * definition body, including the white space tokens. This is called the body
 * token sequence. The subsequence consisting of the non-whitespace tokens is
 * knows as the replacement token sequence (in accord with the vocabulary of
 * C11). To repeat: the replacement token sequence is a subsequence of the body
 * token sequence. This interface provides methods for navigating both
 * sequences.
 * </p>
 * 
 * <p>
 * The <strong>index</strong> of a token in the macro definition body is its
 * index in the body token sequence, counting from 0. Hence the body tokens have
 * indexes 0, 1, ..., numBodyTokens-1. The <strong>replacement ID</strong> of a
 * replacement token R is the number of replacement tokens that occur before R
 * in the body; hence the replacement tokens have IDs 0, 1, 2, ...,
 * numReplacementTokens-1.
 * </p>
 * 
 * @author xxxx
 * 
 */
public interface Macro {

	/**
	 * The body of a {@link Macro} definition consists of a sequence of
	 * {@link ReplacementUnit}s, each of which comprises a preprocessing token
	 * (a non-whitespace token known as the "replacement token" in C11) plus
	 * some possible whitespace.
	 * 
	 * @author xxxx
	 */
	class ReplacementUnit {
		
		/**
		 * Index of this replacement token in the sequence of replacement tokens
		 * that constitute the macro definition body, numbered from 0.
		 */
		public int index;

		/** The preprocessing (non-whitespace) replacement token itself. */
		public Token token;

		/**
		 * Possible 0 or more whitespace tokens following this replacement
		 * token.
		 */
		public Token[] whitespace;

		public ReplacementUnit(int index, Token token, Token[] whitespace) {
			assert token != null;
			assert index >= 0;
			assert whitespace != null;
			this.index = index;
			this.token = token;
			this.whitespace = whitespace;
		}

	}

	/**
	 * The node in the ANTLR parse tree for the preprocessor grammar which is
	 * the root of the macro definition for this macro.
	 * 
	 * @return ANTLR tree node for the macro definition
	 */
	Tree getDefinitionNode();

	/**
	 * The node in the ANTLR parse tree which is the root of the macro body,
	 * i.e., the sequence of replacement tokens. This is a child of the
	 * definition node.
	 * 
	 * @return the ANTLR tree node for the macro body
	 */
	Tree getBodyNode();

	/**
	 * Gets the number of replacement tokens in the macro definition body.
	 * 
	 * @return number of preprocessing tokens in macro definition body
	 */
	int getNumReplacements();

	/**
	 * Returns the macro name.
	 * 
	 * @return the macro name
	 */
	String getName();

	/**
	 * Returns the file in which this macro definition occurs.
	 * 
	 * @return file containing this macro definition
	 */
	SourceFile getFile();

	/**
	 * Returns the <code>index</code>-th {@link ReplacementUnit} object of the
	 * {@link Macro} body.
	 * 
	 * @param index
	 *            integer in range [0,numReplacements-1]
	 * @return the <code>index</code>-th replacement in the macro definition
	 *         body
	 * 
	 * @see {@link Macro}
	 */
	ReplacementUnit getReplacementUnit(int index);

}
