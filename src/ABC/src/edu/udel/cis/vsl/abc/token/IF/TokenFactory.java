package edu.udel.cis.vsl.abc.token.IF;

import java.io.File;
import java.util.List;

import org.antlr.runtime.CharStream;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.Tree;

import edu.udel.cis.vsl.abc.token.IF.CivlcToken.TokenVocabulary;
import edu.udel.cis.vsl.abc.token.IF.ExecutionCharacter.CharacterKind;

/**
 * A factory for producing all the objects under the control of the token
 * module. These includes instances of the following types (and their subtypes):
 * <ul>
 * <li>{@link CivlcToken}</li>
 * <li>{@link Formation}</li>
 * <li>{@link ExecutionCharacter}</li>
 * <li>{@link Source}</li>
 * <li>{@link SyntaxException}</li>
 * <li>{@link UnsourcedException}</li>
 * <li>{@link Macro}</li>
 * </ul>
 * 
 * @author xxxx
 * 
 */
// TODO fix javadocs
public interface TokenFactory {

	// Formations (records of history of token creation)...

	/**
	 * Returns a standard macro expansion formation object. This is a formation
	 * that represents a token created through the process of macro expansion.
	 * 
	 * @param startToken
	 *            the token within the macro application expression that led to
	 *            the formation of the new token; this could be either the macro
	 *            name itself or a token from one of the arguments
	 * @param macro
	 *            the {@link Macro} object given an abstraction represnetation
	 *            of the macro
	 * @param index
	 *            the index of the replacement token (numbered from 0) in the
	 *            macro replacement list that led to the final token
	 * @return a new formation incorporating the specified values
	 */
	MacroExpansion newMacroExpansion(CivlcToken startToken, Macro macro,
			int index);

	/**
	 * Returns a new built-in macro expansion formation object. This represents
	 * a token formed by expanding one of the predefined object macros such as
	 * "__FILE__" or "__LINE__".
	 * 
	 * @param macroToken
	 *            the original token, such as "__FILE__" or "__LINE__" that is
	 *            being expanded
	 * @return a new Formation object representing the expansion of such a macro
	 */
	Formation newBuiltinMacroExpansion(CivlcToken macroToken);

	/**
	 * Formation of a string literal token through the use of the preprocessor
	 * "#" operator during the application of a function-like macro.
	 * 
	 * @param macro
	 *            the function-like macro being applied
	 * @param index
	 *            the index of the replacement token (numbered from 0) in the
	 *            macro replacement list involved in the formation of the new
	 *            token; this replacement token will necessarily be a parameter
	 *            immediately following a "#" token
	 * @param argument
	 *            the sequence of non-whitespace tokens comprising the argument
	 *            in the macro invocation
	 * @return a new formation incorporating specified values
	 */
	Stringification newStringification(FunctionMacro macro, int index,
			List<CivlcToken> argument);

	/**
	 * A formation of a token by either (1) concatenating 0 or more tokens using
	 * the preprocessor "##" operator, or (2) concatenating 1 or more string
	 * literal tokens immediately after preprocessing.
	 * 
	 * @param tokens
	 *            list of tokens to concatenate; should not include whitespace
	 */
	Concatenation newConcatenation(List<CivlcToken> tokens);

	/**
	 * Inclusion record for original source file.
	 * 
	 * @param file
	 *            the file which was included, which should be the original
	 *            source file (the root of the inclusion tree)
	 * @return a new inclusion record
	 */
	Inclusion newInclusion(SourceFile file);

	Inclusion newInclusion(SourceFile file, CivlcToken includeToken);

	/**
	 * Creates a new formation which represents some code added by the system
	 * itself, as opposed to code that emanated from an actual source file. The
	 * identifier should be a short string indicating what part of the system
	 * created the code. Examples: "The CIVL-MPI Transformer". The identifier
	 * will be used to form a "fake" {@link File}, which will be used to form a
	 * {@link SourceFile}, and that is what will be returned by the formation's
	 * {@link Formation#getLastFile()} method.
	 * 
	 * @param identifier
	 *            short string indicating what part of the system created this
	 *            code; used in messages
	 * @return a new system formation object
	 */
	Formation newSystemFormation(String identifier);

	Formation newTransformFormation(String transformerName, String method);

	// Basic token creation...

	CivlcToken newCivlcToken(Token token, Formation formation,
			TokenVocabulary tokenVocab);

	CivlcToken newCivlcToken(int type, String text, Formation formation,
			TokenVocabulary tokenVocab);

	/**
	 * 
	 * @param input
	 *            the character stream from which this token was formed
	 * @param type
	 *            the type of the new token, which is a unique integer ID
	 *            assigned to each category of token by the lexer, e.g.,
	 *            IDENTIFIER
	 * @param channel
	 *            all tokens go to the parser (unless skip() is called in that
	 *            rule) on a particular "channel". The parser tunes to a
	 *            particular channel so that whitespace etc... can go to the
	 *            parser on a "hidden" channel.
	 * @param start
	 *            the char position into the input buffer where this token
	 *            starts
	 * @param stop
	 *            the char position into the input buffer where this token stops
	 * @param formation
	 *            the object specifying the complete history on how this token
	 *            came to exist
	 * @param line
	 *            the line number on which the token occurs
	 * @param charPositionInLine
	 *            the index of the first character of the token in the line in
	 *            which it occurs. Indexes start at 0 for first character in
	 *            line.
	 * @param tokenVocab
	 * 			  the lexical vocabulary used by <code>this<
	 * @return a new instance of {@link CivlcToken} with fields as specified
	 *         above
	 */
	CivlcToken newCivlcToken(CharStream input, int type, int channel, int start,
			int stop, Formation formation, int line, int charPositionInLine,
			TokenVocabulary tokenVocab);

	// Characters and Strings...

	ExecutionCharacter executionCharacter(CharacterKind kind, int codePoint,
			char[] characters);

	CharacterToken characterToken(CivlcToken token) throws SyntaxException;

	StringToken newStringToken(CivlcToken token) throws SyntaxException;

	/**
	 * Constructs a new string literal token (instance of {@link StringToken})
	 * by concatenating a list of string literal tokens. According to the C
	 * Standard, after preprocessing, adjacent string literal tokens are
	 * concatenated. (They may be separated by white space, which is ignored.)
	 * 
	 * @param tokens
	 *            sequence of non-null string literal tokens
	 * @return a single token obtained by concatenating the given ones
	 * @throws SyntaxException
	 *             if the new string cannot be formed for some reason
	 */
	StringToken newStringToken(List<CivlcToken> tokens) throws SyntaxException;

	// Source objects...

	/**
	 * Returns a {@link Source} consisting of a single {@link Token}.
	 * 
	 * @param token
	 *            a non-{@code null} {@link Token}
	 * @return a {@link Source} consisting of the single given token
	 */
	Source newSource(CivlcToken token);

	/**
	 * Computes a {@link Source} comprising all {@link Token}s from
	 * {@code first} to {@code last}, inclusive.
	 * 
	 * @param first
	 *            a non-{@code null} {@link Token}
	 * @param last
	 *            a non-{@code null} {@link Token} from the same token sequence
	 *            as {@code first}
	 * @return a {@link Source} comprising all {@link Token}s from {@code first}
	 *         to {@code last}, inclusive
	 */
	Source newSource(CivlcToken first, CivlcToken last);

	/**
	 * Computes a minimal {@link Source} containing the given source and token.
	 * The given source and token must come from the same token sequence. This
	 * is useful when you want to increase a source by one token.
	 * 
	 * @param source
	 *            a non-{@code null} {@link Source}
	 * @param token
	 *            a non-{@code null} {@link Token} from the same token sequence
	 *            as {@code source}
	 * @return a minimal {@code Source} containing the given source and token
	 */
	Source join(Source source, CivlcToken token);

	/**
	 * Computes a minimal {@link Source} containing the two given sources.
	 * 
	 * @param source1
	 *            a non-{@code null} {@link Source}
	 * @param source2
	 *            a non-{@code null} {@link Source} from the same token sequence
	 *            as {@code source1}
	 * @return a minimal source containing the two given ones
	 */
	Source join(Source source1, Source source2);

	// Exceptions...

	SyntaxException newSyntaxException(String message, Source source);

	SyntaxException newSyntaxException(String message, CivlcToken token);

	SyntaxException newSyntaxException(UnsourcedException e, Source source);

	SyntaxException newSyntaxException(UnsourcedException e, CivlcToken token);

	UnsourcedException newUnsourcedException(String message);

	// Macros...

	ObjectMacro newObjectMacro(Tree definitionNode, SourceFile file);

	FunctionMacro newFunctionMacro(Tree definitionNode, SourceFile file);

	// TokenSources...

	CivlcTokenSequence getTokenSubsequence(CivlcTokenSource fullSource,
			CivlcToken startToken, CivlcToken stopToken);

	CivlcTokenSequence getEmptyTokenSubsequence(
			CivlcTokenSource originalSource);

	/**
	 * creates a CivlC Token Source based on a give list of tokens (not
	 * necessarily CivlC token). All given tokens will be transformed to CivlC
	 * tokens if they are not CivlC tokens, using the given formation.
	 * 
	 * @param tokens
	 *            the list of tokens
	 * @param formation
	 *            the formation to be used when transforming the given tokens
	 * @return a CivlC token source wrapping the given list of tokens
	 */
	CivlcTokenSource getCivlcTokenSourceByTokens(List<? extends Token> tokens,
			Formation formation);

	// FileIndexers....

	FileIndexer newFileIndexer();
}
