package edu.udel.cis.vsl.abc.front.c.preproc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenSource;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;

import edu.udel.cis.vsl.abc.front.IF.IllegalMacroArgumentException;
import edu.udel.cis.vsl.abc.front.IF.Preprocessor;
import edu.udel.cis.vsl.abc.front.IF.PreprocessorException;
import edu.udel.cis.vsl.abc.front.IF.PreprocessorRuntimeException;
import edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.file_return;
import edu.udel.cis.vsl.abc.front.common.preproc.CTokenIterator;
import edu.udel.cis.vsl.abc.token.IF.CivlcToken;
import edu.udel.cis.vsl.abc.token.IF.CivlcToken.TokenVocabulary;
import edu.udel.cis.vsl.abc.token.IF.CivlcTokenSource;
import edu.udel.cis.vsl.abc.token.IF.FileIndexer;
import edu.udel.cis.vsl.abc.token.IF.Formation;
import edu.udel.cis.vsl.abc.token.IF.FunctionMacro;
import edu.udel.cis.vsl.abc.token.IF.Macro;
import edu.udel.cis.vsl.abc.token.IF.ObjectMacro;
import edu.udel.cis.vsl.abc.token.IF.SourceFile;
import edu.udel.cis.vsl.abc.token.IF.StringToken;
import edu.udel.cis.vsl.abc.token.IF.SyntaxException;
import edu.udel.cis.vsl.abc.token.IF.TokenFactory;
import edu.udel.cis.vsl.abc.token.IF.TokenUtils;
import edu.udel.cis.vsl.abc.util.IF.Pair;
import edu.udel.cis.vsl.abc.util.IF.StringPredicate;

/**
 * <p>
 * A {@link PreprocessorTokenSource} is created by scanning a sequence of
 * character streams and executing the preprocessor directives in those streams
 * to produce a stream of (output) tokens. The directives may include
 * <code>#include</code> directives, which cause additional files to be opened
 * and scanned.
 * </p>
 * 
 * <p>
 * The tokens produced are instances of {@link CivlcToken}.
 * </p>
 * 
 * @author Stephen F. Siegel
 */
public class PreprocessorTokenSource implements CivlcTokenSource {

	// Fields...

	/** The list of character streams to be parsed and preprocessed */
	private CharStream[] theStreams;

	/**
	 * The list of formations corresponding to {@link #theStreams}. The
	 * {@link Formation} corresponding to a {@link CharStream} provides information
	 * on where the stream came from.
	 */
	private Formation[] theFormations;

	/**
	 * Object to track of all source files encountered by this preprocessing
	 * instance.
	 */
	private FileIndexer indexer;

	/**
	 * The index of the current stream being processed. Runs from 0 to
	 * <code>theStreams.length - 1</code>.
	 */
	private int currentSource = 0;

	/**
	 * The source files used to build this token stream only. These do not
	 * necessarily include all of the source files seen by the preprocessor creating
	 * this token source, because the preprocessor can be re-used multiple times to
	 * create many token sources.
	 */
	private Set<SourceFile> sourceFiles = new LinkedHashSet<>();

	/**
	 * Factory used to produce new {@link CivlcToken}s.
	 */
	private TokenFactory tokenFactory;

	/**
	 * Stack of included source file information objects. When a #include directive
	 * is executed, an element is pushed on to this stack. When the EOF of a file is
	 * reached, the stack is popped. The stack is initialized with the initial given
	 * source file.
	 */
	private Stack<PreprocessorSourceFileInfo> sourceStack = new Stack<PreprocessorSourceFileInfo>();

	/**
	 * The directories which should be searched for files that are included using
	 * 
	 * <pre>
	 * #include &lt;filename&gt;
	 * </pre>
	 * 
	 * syntax.
	 */
	private File[] systemIncludePaths;

	/**
	 * The directories which should be searched for files that are included using
	 * 
	 * <pre>
	 * #include "filename"
	 * </pre>
	 * 
	 * syntax. If the file is not found in one of these directories, the system
	 * paths are then searched.
	 */
	private File[] userIncludePaths;

	/**
	 * Should we save all output tokens in some kind of array?
	 */
	private boolean saveTokens = true;

	/**
	 * The output tokens, if we are saving them, else null.
	 */
	private ArrayList<CivlcToken> theTokens = null;

	/**
	 * Adjacent string literal tokens will be accumulated in a buffer before being
	 * added to the output stream because adjacent string literal must be
	 * concatenated to form one token.
	 */
	private LinkedList<CivlcToken> stringLiteralBuffer = new LinkedList<CivlcToken>();

	/**
	 * The last token, which will be the EOF token, once it is reached.
	 */
	private CivlcToken finalToken = null;

	/**
	 * The expression analyzer is used to analyze and evaluate integer expressions
	 * in preprocessor conditionals.
	 */
	private PreprocessorExpressionAnalyzer expressionAnalyzer;

	/**
	 * A mapping of macro names to the Macro object. An entry is created and added
	 * to this map whenever a '#define' directive is processed. An entry is removed
	 * by a '#undef'
	 */
	Map<String, Macro> macroMap;

	/** Is the current node inside a text block ? */
	private boolean inTextBlock = false;

	/**
	 * <p>
	 * A stack of "ACSL" pragmas (under the CIVL command-universe). An "ACSL" pragma
	 * denotes that all the annotations, in the same source file and coming after
	 * this pragma, will be parsed and be a part of the final AST if they are
	 * written in ACSL syntax.
	 * </p>
	 * 
	 * <p>
	 * This stack is used to keep track of when to parse ACSL annotations: A
	 * {@link #pushStream(CharStream, Formation)} will cause a push of a default
	 * PARSE_PRAGMA value (false); A "EOF" will cause a pop on the stack;
	 * Encountering a "#pragma CIVL ACSL" changes the value on the top frame to
	 * true.
	 * </p>
	 */
	private Stack<Boolean> parseACSLPragmaStack = new Stack<>();

	/**
	 * Is the current node inside a pragma line? We need to know this because then
	 * closing NEWLINE that terminates the pragma must be made VISIBLE (usually all
	 * white space is rendered invisible).
	 */
	private boolean inPragma = false;

	/**
	 * Is the current node inside an inline annotation, i.e., one that starts with
	 * "//@"? We need to know this for two reasons: (1) the NEWLINE that terminates
	 * the annotation must be made visible, (2) inside annotations, "@" tokens are
	 * rendered invisible.
	 */
	private boolean inInlineAnnotation = false;

	/**
	 * Is the current node inside a block annotation, i.e., one delimited by "/*@"
	 * and "*" "/"? We need to know this because inside annotations, "@" tokens are
	 * rendered invisible.
	 */
	private boolean inBlockAnnotation = false;

	/**
	 * First and last elements of the output buffer, which forms a linked list. This
	 * is where tokens stay temporarily until they are removed by an invocation of
	 * nextToken().
	 */
	private CivlcToken firstOutput, lastOutput;

	/**
	 * The number of tokens that have been output.
	 */
	private int outputTokenCount = 0;

	// Constructors...

	/**
	 * 
	 * Instantiates new CTokenSource object. The given source file is parsed, a file
	 * info object is created for it and pushed onto the stack. The output tokens
	 * are not generated; this does not begin until "getToken" is called.
	 * 
	 * @param config             the ABC configuration
	 * @param FileIndexer        indexer the file indexer that will be used to index
	 *                           all source files encountered by this preprocessing
	 *                           session
	 * @param streams            the input character streams that will be
	 *                           concatenated to form a single stream that will be
	 *                           the input to the preprocessing algorithm
	 * @param formations         an array specifying the formation history of each
	 *                           input stream, basically used to give a name to that
	 *                           stream
	 * @param systemIncludePaths the directories where files included with angle
	 *                           brackets are searched
	 * @param userIncludePaths   the directories where files included with double
	 *                           quotes are searched
	 * @param macroMap           the predefined macros; maps a macro name to its
	 *                           definition body
	 * @param tokenFactory       the token factory to be used for producing new
	 *                           {@link CivlcToken}s
	 * @throws PreprocessorException if and IOException or RecognitionException
	 *                               occurs while scanning and parsing the source
	 *                               file
	 */
	public PreprocessorTokenSource(FileIndexer indexer, CharStream[] streams, Formation[] formations,
			File[] systemIncludePaths, File[] userIncludePaths, Map<String, Macro> macroMap, TokenFactory tokenFactory)
			throws PreprocessorException {
		int numStreams = streams.length;

		assert systemIncludePaths != null;
		assert userIncludePaths != null;
		this.indexer = indexer;
		this.tokenFactory = tokenFactory;
		assert numStreams == formations.length;
		this.theStreams = streams;
		this.theFormations = formations;
		this.systemIncludePaths = systemIncludePaths;
		this.userIncludePaths = userIncludePaths;
		this.expressionAnalyzer = new PreprocessorExpressionAnalyzer(new MacroDefinedPredicate(macroMap));
		this.macroMap = macroMap;
		if (saveTokens)
			theTokens = new ArrayList<CivlcToken>();
		for (Formation formation : formations) {
			SourceFile sourceFile = formation.getLastFile();

			sourceFiles.add(sourceFile);
			indexer.getOrAdd(sourceFile.getFile());
		}
		pushStream(streams[0], formations[0]);
	}

	// Helpers...

	/**
	 * Searches for an internal file, i.e., one which is stored in a directory in
	 * the ABC class path. Returns a new character stream obtained by reading the
	 * file, or <code>null</code> if the file cannot be found.
	 * 
	 * @param path     the path leading to the file; this path is relative to the
	 *                 class path and starts with "/", e.g., "/include/abc".
	 * @param filename the name of the file proper, e.g., "stdlib.h"
	 * @return a new character stream obtained from the file or <code>null</code>
	 */
	private Pair<File, CharStream> findInternalSystemFile(File path, String filename) {
		File file = new File(path, filename);
		String resource = file.getAbsolutePath();

		try {
			CharStream stream = PreprocessorUtils.newFilteredCharStreamFromResource(resource, resource);

			if (stream != null)
				return new Pair<File, CharStream>(file, stream);
		} catch (IOException e) {
		}
		return null;
	}

	/**
	 * Finds an internal system file in the default ABC include path,
	 * {@link Preprocessor#ABC_INCLUDE_PATH}.
	 * 
	 * @param filename name of file, e.g., "stdlib.h"
	 * @return new character stream obtained from the file or <code>null</code> if
	 *         no such file is found in the default include paths
	 */
	private Pair<File, CharStream> findInternalSystemFile(String filename) {
		for (File systemPath : systemIncludePaths) {
			Pair<File, CharStream> result = findInternalSystemFile(systemPath, filename);

			if (result != null)
				return result;
		}
		// look in directory "/include/abc" in the class path:
		return findInternalSystemFile(Preprocessor.ABC_INCLUDE_PATH, filename);
	}

	/**
	 * Pushes a new character stream onto the {@link #sourceStack}.
	 * 
	 * @param charStream the new character stream to push
	 * @param formation  a formation which specifies the origin of the character
	 *                   stream
	 * @throws PreprocessorException if something goes wrong parsing the character
	 *                               stream
	 */
	private void pushStream(CharStream charStream, Formation formation) throws PreprocessorException {
		String name = formation.getLastFile().getName();

		try {
			PreprocessorLexer lexer = new PreprocessorLexer(charStream);
			PreprocessorParser parser = new PreprocessorParser(new CommonTokenStream(lexer));
			file_return fileReturn = parser.file();
			int numErrors = parser.getNumberOfSyntaxErrors();

			if (numErrors != 0)
				throw new PreprocessorException(
						numErrors + " syntax errors occurred while scanning included file " + name);

			Tree tree = (Tree) fileReturn.getTree();

			sourceStack.push(new PreprocessorSourceFileInfo(formation, tree));
			parseACSLPragmaStack.push(false);
			incrementNextNode(); // skip root "FILE" node
		} catch (RecognitionException e) {
			throw new PreprocessorException("Preprocessing " + formation.toString() + " failed: " + e);
		} catch (RuntimeException e) {
			throw new PreprocessorException(e.getMessage());
		}
	}

	/**
	 * Returns current file being processed.
	 * 
	 * @return current file or <code>null</code> if the source stack is currently
	 *         empty
	 */
	private SourceFile getCurrentSource() {
		if (sourceStack.isEmpty())
			return theFormations[currentSource].getLastFile();
		else
			return sourceStack.peek().getFile();
	}

	/**
	 * Process the next node in the CPP AST. This may actually involve processing
	 * more nodes, but it will at least involve processing one node and incrementing
	 * the current position by at least one token.
	 * 
	 * @throws PreprocessorException
	 */
	private void processNextNode() throws PreprocessorException {
		CommonTree node = (CommonTree) getNextInputNode();

		if (inTextBlock) {
			// If you are in a TEXT_BLOCK, you don't have to check for any
			// directives.
			processText(node);
		} else {
			Token token = node.getToken();
			int type = token.getType();

			switch (type) {
			case PreprocessorParser.EOF:
				processEOF(node);
				break;
			case PreprocessorParser.TEXT_BLOCK:
				processTextBlock(node);
				break;
			case PreprocessorParser.DEFINE:
				processMacroDefinition(node);
				break;
			case PreprocessorParser.ERROR:
				processError(node);
				break;
			case PreprocessorParser.ELIF:
			case PreprocessorParser.PIF:
				processIf(node);
				break;
			case PreprocessorParser.IFDEF:
				processIfdef(node);
				break;
			case PreprocessorParser.IFNDEF:
				processIfndef(node);
				break;
			case PreprocessorParser.INCLUDE:
				processInclude(node);
				break;
			case PreprocessorParser.PPRAGMA:
				processPragma(node);
				break;
			case PreprocessorParser.UNDEF:
				processUndef(node);
				break;
			case PreprocessorParser.HASH:
				processNondirective(node);
				break;
			case PreprocessorParser.LINE:
				processLine(node);
				break;
			default:
				processText(node);
			}
		}
	}

	// processText...

	/**
	 * <p>
	 * Processes a text node, i.e., a node for a token which is not a preprocessor
	 * directive and does not have any special meaning to the preprocessor, though
	 * it may be a macro invocation. This node may or may not be in a text block.
	 * </p>
	 * 
	 * <p>
	 * Precondition: This method should be called only from the outermost scope of
	 * expansion, i.e., we are not currently in a macro expansion.
	 * </p>
	 * 
	 * <p>
	 * Postcondition: if the node is not an identifier for a macro, a
	 * {@link CivlcToken} for it is created and added to the output buffer. If it is
	 * a macro, macro expansion takes place. For a function macro, this involves
	 * consuming more input tokens (the tokens comprising the left parenthesis, the
	 * arguments, and the final right parenthesis). The current position in the
	 * input AST is moved to the point just after that last token. The tokens
	 * resulting from the expansion are added to the output buffer.
	 * </p>
	 * 
	 * @param textNode the text node to process
	 * @throws PreprocessorException if something goes wrong with macro expansion,
	 *                               or if a ppnumber token does not have the form
	 *                               of a CIVL-C range expression (a..b)
	 */
	private void processText(Tree textNode) throws PreprocessorException {
		Token token = ((CommonTree) textNode).getToken();

		if (PreprocessorUtils.isIdentifier(token)) {
			processIdentifier(textNode);
		} else {
			// Wether parse ACSL annotation or not:
			boolean parseACSL = parseACSLPragmaStack.peek();

			switch (token.getType()) {
			case PreprocessorLexer.INLINE_ANNOTATION_START:
				assert (inTextBlock);
				inInlineAnnotation = true;
				// will be set to false at next NEWLINE
				break;
			case PreprocessorLexer.NEWLINE:
				assert (inTextBlock);
				if (inInlineAnnotation) {
					// if parse ACSL, NEWLINE in an annotation line must be
					// added to output when "inInlineAnnotation" flag is
					// true: Otherwise, nothing in annotation line goes to
					// output
					if (parseACSL)
						shiftToOutput(textNode);
					inInlineAnnotation = false;
					incrementNextNode();
					return;
				}
				break;
			case PreprocessorLexer.ANNOTATION_START:
				assert (inTextBlock);
				inBlockAnnotation = true;
				break;
			case PreprocessorLexer.ANNOTATION_END:
				assert (inTextBlock && inBlockAnnotation);
				inBlockAnnotation = false;
				// if parse ACSL, ANNOTATION_END in an annotation block must
				// be added to output when "inBlockAnnotation" flag is true;
				// Otherwise, nothing in annotation block goes to output:
				if (parseACSL)
					shiftToOutput(textNode);
				incrementNextNode();
				return;
			case PreprocessorLexer.PP_NUMBER:
				String sourceName = token.getInputStream().getSourceName();
				Boolean isFortran = sourceName.toUpperCase().contains(".F");

				if (isFortran) {
					processIdentifier(textNode);
				} else
					processPPNumber(token);
				return;
			default:
			}
			// If current control is NOT in block annotation and line
			// annotation, or ACSL will be parsed anyway, put the node to
			// output:
			if ((!inBlockAnnotation && !inInlineAnnotation) || parseACSL)
				shiftToOutput(textNode);
			incrementNextNode();
		}
	}

	/**
	 * Processes a "PPNumber", which is any preprocessor number that is not a
	 * standard integer or floating constant. The only possibility currently is the
	 * CIVL-C range or fragment thereof, which has the form "a..b", or "a..", or
	 * "..b", where a and b are integer constants. Ideally we would like to have
	 * been parsed as 3 (or 2) separate tokens, but we just couldn't find any way to
	 * do that with ANTLR. So we fix it here.
	 * 
	 * @param token the ppnumber token which should be a CIVL-C range expression or
	 *              part thereof
	 * @throws PreprocessorException if the text of that token does not have the
	 *                               form of a CIVL-C range expression or part
	 *                               thereof
	 */
	private void processPPNumber(Token token) throws PreprocessorException {
		String text = token.getText();
		int line = token.getLine();
		int pos = token.getCharPositionInLine();
		int length = text.length();
		int index = text.indexOf("..");
		int startIndex = ((CommonToken) token).getStartIndex();
		int stopIndex = ((CommonToken) token).getStopIndex();
		int chan = token.getChannel();
		CharStream stream = token.getInputStream();
		Formation formation = getIncludeHistory();

		if (index < 0)
			throw new PreprocessorException("Unknown preprocessor number", token);
		if (index > 0) {
			CivlcToken leftToken = tokenFactory.newCivlcToken(stream, PreprocessorLexer.INTEGER_CONSTANT, chan,
					startIndex, startIndex + index - 1, formation, line, pos, TokenVocabulary.PREPROC);

			// should not be necessary since the text is obtained from the
			// stream by default, using the start and stop indexes:
			// leftToken.setText(text.substring(0, index));
			addOutput(leftToken);
		}

		CivlcToken dotdot = tokenFactory.newCivlcToken(stream, PreprocessorLexer.DOTDOT, chan, startIndex + index,
				startIndex + index + 1, formation, line, pos + index, TokenVocabulary.PREPROC);

		addOutput(dotdot);
		if (index + 2 < length) {
			CivlcToken rightToken = tokenFactory.newCivlcToken(stream, PreprocessorLexer.INTEGER_CONSTANT, chan,
					startIndex + index + 2, stopIndex, formation, line, pos + index + 2, TokenVocabulary.PREPROC);
			String rightText = rightToken.getText();
			char firstChar = rightText.charAt(0);

			// rightText should be either an integer literal or
			// identifier ... how to tell
			if (firstChar == '+' || firstChar == '-' || (firstChar >= '0' && firstChar <= '9')) {
				// OK: should be integer
			} else {
				rightToken.setType(PreprocessorLexer.IDENTIFIER);
			}
			addOutput(rightToken);
		}
		incrementNextNode();
	}

	/**
	 * Is this one of the special macros __LINE__ or __FILE__, predefined macros
	 * whose values change automatically.
	 * 
	 * @param name an identifier string
	 * @return <code>true</code> iff name is one of the special macro names
	 */
	private boolean isSpecialMacro(String name) {
		return "__LINE__".equals(name) || "__FILE__".equals(name);
	}

	/**
	 * If the identifier is a macro, do macro expansion. Else, it's just a regular
	 * token that gets shifted to output.
	 * 
	 * @throws PreprocessorException
	 */
	private void processIdentifier(Tree identifierNode) throws PreprocessorException {
		String name = identifierNode.getText();
		Macro macro = macroMap.get(name);

		// If the control is in block annotation or inline annotation but ACSL
		// shall not be parsed, skip:
		if ((inBlockAnnotation || inInlineAnnotation) && !parseACSLPragmaStack.peek()) {
			incrementNextNode();
		} else if (macro != null
				&& (macro instanceof ObjectMacro || peekAheadSkipWSHasType(PreprocessorLexer.LPAREN))) {
			processInvocation(macro, identifierNode);
		} else if (isSpecialMacro(name)) {
			addOutput(processSpecialInvocation(identifierNode));
			incrementNextNode();
		} else {
			shiftToOutput(identifierNode);
			incrementNextNode();
		}
	}

	/**
	 * <p>
	 * Processes an object or function-like macro invocation node. Continues walking
	 * the input tree to find the macro arguments (if the macro is a function
	 * macro). Then expands the macro using the macro's definition. The resulting
	 * token sequence is added to the end of the outputBuffer and the position in
	 * the input sequence is moved to the point just after the complete macro
	 * invocation.
	 * </p>
	 * 
	 * <p>
	 * This method is recursive: if any macro expansions occur within arguments,
	 * they are also expanded, following the rules laid out in the C11
	 * specification. See C11 6.10.3.
	 * </p>
	 * 
	 * <p>
	 * Implementation notes: for function-like macros: calls
	 * {@link #findInvocationArguments(FunctionMacro, Tree)}, then calls
	 * {@link #processInvocation(FunctionMacro, CivlcToken, CivlcToken[])}.
	 * </p>
	 * 
	 * @param macro          a Macro object
	 * @param invocationNode the node containing the identifier token whose string
	 *                       value is the name of the macro
	 * @throws PreprocessorException if something goes wrong expanding the macro,
	 *                               such as the wrong number of arguments is
	 *                               provided
	 */
	private void processInvocation(Macro macro, Tree invocationNode) throws PreprocessorException {
		Token token = ((CommonTree) invocationNode).getToken();
		CivlcToken cToken = tokenFactory.newCivlcToken(token, getIncludeHistory(), TokenVocabulary.PREPROC);
		Pair<CivlcToken, CivlcToken> result;

		if (macro instanceof ObjectMacro) {
			result = processInvocation((ObjectMacro) macro, cToken);
			incrementNextNode();
		} else {
			Iterator<CivlcToken> argumentIter = getArgumentIterator((FunctionMacro) macro);

			// note the call to findInvocationArguments updated
			// position correctly.
			result = processInvocation((FunctionMacro) macro, cToken, argumentIter);
		}
		addOutputList(result);
	}

	/**
	 * Processes the invocation of a "special" macro, one of the built-ins, such as
	 * "__LINE__" or "__FILE__".
	 * 
	 * @param invocationNode the parse tree node corresponding to the use of the
	 *                       macro name
	 * @throws PreprocessorException if something goes wrong adding the new string
	 *                               or integer constant token to the current output
	 *                               buffer
	 */
	private CivlcToken processSpecialInvocation(Tree invocationNode) throws PreprocessorException {
		Token token = ((CommonTree) invocationNode).getToken();
		CivlcToken cToken = tokenFactory.newCivlcToken(token, getIncludeHistory(), TokenVocabulary.PREPROC);

		return expandSpecial(cToken);
	}

	private CivlcToken expandSpecial(CivlcToken origin) {
		String name = origin.getText();
		Formation formation = tokenFactory.newBuiltinMacroExpansion(origin);
		CivlcToken newToken;

		switch (name) {
		case "__LINE__":
			newToken = tokenFactory.newCivlcToken(PreprocessorLexer.INTEGER_CONSTANT, "" + origin.getLine(), formation,
					TokenVocabulary.PREPROC);
			break;
		case "__FILE__":
			newToken = tokenFactory.newCivlcToken(PreprocessorLexer.STRING_LITERAL,
					'"' + getCurrentSource().getFile().getAbsolutePath() + '"', formation, TokenVocabulary.PREPROC);
			break;
		default:
			throw new PreprocessorRuntimeException("unreachable");
		}
		return newToken;
	}

	/**
	 * <p>
	 * Performs expansion of an object macro. Returns a list of tokens obtained from
	 * the macro's replacement list.
	 * </p>
	 * 
	 * <p>
	 * The list of tokens returned consists entirely of newly created tokens (i.e.,
	 * tokens which did not exist upon entry to this method). They will have correct
	 * include and expansion histories.
	 * </p>
	 * 
	 * <p>
	 * Following the C Standard, the list of replacement tokens are expanded a
	 * second time, after marking all tokens corresponding to the given macro
	 * identifier as "do not expand".
	 * </p>
	 * 
	 * @param macro  the object macro
	 * @param origin the expansion history of the identifier before getting to the
	 *               current expansion.
	 * @return a pair consisting of the first and last elements of the new list of
	 *         tokens
	 * @throws PreprocessorException if something goes wrong while expanding the
	 *                               list of replacement tokens (i.e., the second
	 *                               expansion)
	 */
	private Pair<CivlcToken, CivlcToken> processInvocation(ObjectMacro macro, CivlcToken origin)
			throws PreprocessorException {
		return performSecondExpansion(instantiate(macro, origin), macro);
	}

	/**
	 * <p>
	 * Processes a function macro invocation, given the macro and the actual
	 * arguments. Each actual argument is given as a null-terminated list of tokens.
	 * This returns the list of tokens which is the result of expanding the macros.
	 * This procedure is recursive: if any macro invocations occur within the
	 * arguments, they are also expanded.
	 * </p>
	 * 
	 * <p>
	 * It is assumed that the input argument tokens will have the correct expansion
	 * histories, i.e., for all expansions up to but not including the current one.
	 * </p>
	 * 
	 * <p>
	 * The list of tokens returned consists entirely of newly created tokens (i.e.,
	 * tokens which did not exist upon entry to this method). They will have correct
	 * include and expansion histories.
	 * </p>
	 * 
	 * <p>
	 * NOTE: in a function-like macro replacement list, each '#' must be immediately
	 * followed by a parameter name, else error --- C11 6.10.3.2(1). The '#' may
	 * also occur in an object-like macro replacement list, but has no special
	 * meaning or restrictions.
	 * </p>
	 * 
	 * <p>
	 * NOTE: if "# A" occurs in the replacement sequence, where A is one of the
	 * parameter names, then the actual argument corresponding to A is NOT expanded.
	 * Instead, the exact actual argument is stringified, and the resulting string
	 * token replaces the "# A". Stringification involves escaping characters such
	 * as " and \. See C11 6.10.3.2.
	 * </p>
	 * 
	 * <p>
	 * NOTE: The "##" may be used in object and function-like macro replacement
	 * lists. Also, "[a] ## preprocessing token shall not occur at the beginning or
	 * at the end of a replacement list for either form of macro definition." C11
	 * 6.10.3.3(1).
	 * </p>
	 * 
	 * <p>
	 * NOTE: if "A ##" or "## A" occurs, then A is replaced by the non-expanded
	 * actual argument token list UNLESS actual is empty (no tokens), in which case
	 * A is replaced by PLACEMARKER, a special symbol. After that and before second
	 * expansion: each "X ## Y" --- if the ## is from the replacement list and NOT
	 * from an argument --- is replaced by concatenation of X and Y, where
	 * PLACEMARKER is the identity element for concatenation. Then all PLACEMARKERs
	 * are removed. Hence before second expansion all placemarkers and all of the
	 * original ## tokens should be gone. ISN'T THIS THE SAME as saying: if actual
	 * argument is empty then delete "A ##" or "## A" from the replacement list.
	 * What about "B ## A ## C"? It becomes "B ## C".
	 * </p>
	 * 
	 * 
	 * Formations: concatenation, stringification. Both part of macro expansion.
	 * 
	 * After all of this, second expansion.
	 * </p>
	 * 
	 * @param macro     the function-like macro which is being invoked
	 * @param arguments the actual arguments in the invocation of the macro
	 * @return a pair consisting of the first and last elements of the linked list
	 *         which is the new token sequence produced by the expansion
	 * @throws PreprocessorException
	 */
	private Pair<CivlcToken, CivlcToken> processInvocation(FunctionMacro macro, CivlcToken origin,
			Iterator<CivlcToken> argumentIter) throws PreprocessorException {
		return performSecondExpansion(instantiate(macro, origin, argumentIter), macro);
	}

	/**
	 * In the second expansion of a macro, the result of the first expansion is
	 * first scanned for instances of the macro being expanded. Any such instances
	 * are marked to not be re-expanded, to prevent an infinite recursion. See C11
	 * 6.10.3.4(2): "If the name of the macro being replaced is found during this
	 * scan of the replacement list (not including the rest of the source file’s
	 * preprocessing tokens), it is not replaced."
	 * 
	 * @param tokenList a list of tokens to be expanded given as pair consisting of
	 *                  first and last elements
	 * @param macro     the macro being expanded
	 * @return result of second expansion of tokenList (first and last elements of
	 *         the linked list of tokens)
	 * @throws PreprocessorException any improper macro invocations occur in the
	 *                               token list
	 */
	private Pair<CivlcToken, CivlcToken> performSecondExpansion(Pair<CivlcToken, CivlcToken> tokenList, Macro macro)
			throws PreprocessorException {
		String name = macro.getName();

		// mark all occurrences of identifier M as "do not expand"...
		for (CivlcToken token = tokenList.left; token != null; token = token.getNext()) {
			if (PreprocessorUtils.isIdentifier(token) && name.equals(token.getText()))
				token.makeNonExpandable();
		}
		return expandList(tokenList.left);
	}

	private Pair<CivlcToken, CivlcToken> instantiate(FunctionMacro macro, CivlcToken origin,
			Iterator<CivlcToken> argumentIter) throws PreprocessorException {
		MacroExpander expander = new MacroExpander(this, macro, origin, argumentIter);
		Pair<CivlcToken, CivlcToken> result = expander.expand();

		return result;
	}

	/**
	 * Creates a new instance of the object macro's body. Creates a sequence of
	 * CTokens corresponding to the sequence of input tokens in the macro's
	 * replacement list.
	 * 
	 * The result is a linked list of CToken using the "next" field of CToken. The
	 * elements of the Pair returned are the first and last element of the list.
	 * 
	 * @param macro  any object macro
	 * @param origin the original expansion history for the identifier which is the
	 *               macro's name and led to its expansion
	 * @return first and last element in a null-terminated linked list of fresh
	 *         CTokens obtained from the macro's body
	 */
	private Pair<CivlcToken, CivlcToken> instantiate(ObjectMacro macro, CivlcToken origin)
			throws PreprocessorException {
		MacroExpander expander = new MacroExpander(this, macro, origin);
		Pair<CivlcToken, CivlcToken> result = expander.expand();

		return result;
	}

	/**
	 * <p>
	 * Expands all macro invocations in a null-terminated linked list of
	 * {@link CivlcToken}, modifying the list in the process. Does not care about
	 * preprocessor directives, or anything else other than macro invocations. Even
	 * if this method comes across a directive, it does not treat the directive as a
	 * directive---it is treated as just another token.
	 * </p>
	 * 
	 * <p>
	 * Exception: do not expand macros if they occur within a "defined" operator!
	 * </p>
	 * 
	 * <p>
	 * Note that the left token in the pair returned by this method MAY be the first
	 * token in the given list. It also may NOT be the first token in the given
	 * list, because that token was a macro invocation and was replaced.
	 * </p>
	 * 
	 * <p>
	 * Implementation: iterate over the tokens looking for macro invocations. When
	 * you find one, call processInvocation and insert its output into list, in
	 * place of the the invocation.
	 * 
	 * <pre>
	 * ... X X X M ( A0 A0 A1 A2 A2 A2 ) X X X ... ->
	 * ... X X X R R R ... R X X X ...
	 * </pre>
	 * </p>
	 * 
	 * @param token the first element in a null-terminated list of tokens; may be
	 *              <code>null</code> for empty list
	 * @return pair consisting of the first and last elements of the expanded list;
	 *         the two components of this pair will be <code>null</node> if the
	 *         expanded list is empty
	 * @throws PreprocessorException if something goes wrong in a macro expansion
	 *                               (e.g., the wrong number of arguments)
	 */
	Pair<CivlcToken, CivlcToken> expandList(CivlcToken first) throws PreprocessorException {
		CivlcToken current = first, previous = null;

		while (current != null) {
			if (!current.isExpandable()) {
				previous = current;
				current = current.getNext();
				continue;
			}

			int type = current.getType();

			if (type == PreprocessorLexer.DEFINED) {
				// ignore it and next token
				previous = current.getNext();
				current = previous.getNext();
				continue;
			}

			Macro macro = null;
			boolean isInvocation = false;
			boolean isSpecial = false;

			if (PreprocessorUtils.isIdentifier(current)) {
				macro = macroMap.get(current.getText());

				if (macro == null) {
					isInvocation = isSpecialMacro(current.getText());
					isSpecial = isInvocation;
				} else if (macro instanceof ObjectMacro) {
					isInvocation = true;
				} else {
					CivlcToken next = current.getNext();

					// skip all white space
					while (next != null && PreprocessorUtils.isWhiteSpace(next))
						next = next.getNext();

					isInvocation = (next != null && next.getType() == PreprocessorLexer.LPAREN);
				}
			}
			if (!isInvocation) { // ignore it
				previous = current;
				current = current.getNext();
				continue;
			}
			if (isSpecial) {
				CivlcToken newToken = expandSpecial(current);

				newToken.setNext(current.getNext());
				if (previous == null)
					first = newToken;
				else
					previous.setNext(newToken);
				current = current.getNext();
				continue;
			}

			Pair<CivlcToken, CivlcToken> replacements;

			if (macro instanceof ObjectMacro) {
				replacements = processInvocation((ObjectMacro) macro, current);
				current = current.getNext();
			} else {
				Iterator<CivlcToken> iter = new CTokenIterator(current.getNext());
				// CivlcToken[] arguments = findInvocationArguments(
				// (FunctionMacro) macro, iter);

				replacements = processInvocation((FunctionMacro) macro, current, iter);
				// move current to the token just after the closing ')'
				// of the invocation, or null if the ')' was the last token:
				if (iter.hasNext())
					current = iter.next();
				else
					current = null;
			}
			if (replacements.left == null) {
				if (previous == null)
					first = current;
				else
					previous.setNext(current);
			} else {
				if (previous == null)
					first = replacements.left;
				else
					previous.setNext(replacements.left);
				previous = replacements.right;
				previous.setNext(current);
			}
		}
		return new Pair<>(first, previous);
	}

	/**
	 * <p>
	 * When current position in preprocessor parse tree is at a macro application
	 * node, this method will continue walking through the tree from that point in
	 * order to find the macro invocation arguments. It creates new
	 * {@link CivlcToken}s for the tokens occurring in those arguments. Returns an
	 * array (whose length is the number of inputs to the macro) in which element i
	 * is first element in the list of {@link CivlcToken}s for the tokens which
	 * comprise argument i.
	 * </p>
	 * 
	 * <p>
	 * The new tokens will have the correct include history. They will have the
	 * "trivial" expansion history of length 0.
	 * </p>
	 * 
	 * <p>
	 * From C11 Sec. 6.10.3.11: "If there are sequences of preprocessing tokens
	 * within the list of arguments that would otherwise act as preprocessing
	 * directives, the behavior is undefined." This method will throw a
	 * {@link PreprocessorException} if that occurs.
	 * </p>
	 * 
	 * <p>
	 * Updates position to point just after closing right parenthesis.
	 * </p>
	 * 
	 * <p>
	 * Implementation notes: creates an {@link Iterator} of {@link CivlcToken} by
	 * iterating over the {@link Tree} nodes starting from the node immediately
	 * following the <code>invocationNode</code> in DFS order and creating new
	 * tokens. Feeds this to method
	 * {@link #findInvocationArguments(FunctionMacro, Iterator)}.
	 * </p>
	 * 
	 * @param macro          the function-like macro which is being applied
	 * @param invocationNode the node in the preprocessor parse tree representing an
	 *                       invocation of the macro
	 * @return array of length number of formal parameters of macro; i-th element of
	 *         array is the first element of a list of tokens that comprise the i-th
	 *         argument
	 * @throws PreprocessorException if a sequence of tokens that would otherwise
	 *                               act as preprocessing directives occurs in the
	 *                               argument list
	 */
	private Iterator<CivlcToken> getArgumentIterator(FunctionMacro macro) throws PreprocessorException {
		Iterator<CivlcToken> iter;

		incrementNextNode();
		iter = new Iterator<CivlcToken>() {
			@Override
			public boolean hasNext() {
				return getNextInputNode() != null;
			}

			@Override
			public CivlcToken next() {
				Tree node = getNextInputNode();
				Token inputToken = ((CommonTree) node).getToken();
				CivlcToken result = tokenFactory.newCivlcToken(inputToken, getIncludeHistory(),
						TokenVocabulary.PREPROC);

				if (node.getChildCount() > 0)
					throw new IllegalMacroArgumentException(result);
				incrementNextNode();
				return result;
			}

			@Override
			public void remove() { // should never be called
				throw new UnsupportedOperationException();
			}
		};
		return iter;
	}

	/**
	 * Process and End-of-file token. If this is the root file, you are all done,
	 * and the EOF token is moved to the output buffer. Otherwise, you pop stack and
	 * throw away the EOF token---it does not get output.
	 * 
	 * @throws PreprocessorException
	 */
	private void processEOF(Tree node) throws PreprocessorException {
		Token eof = ((CommonTree) node).getToken();
		PreprocessorSourceFileInfo o = sourceStack.pop();

		parseACSLPragmaStack.pop();
		assert parseACSLPragmaStack.size() == sourceStack.size();
		if (sourceStack.isEmpty()) {
			if (currentSource == theStreams.length - 1) {
				CivlcToken myEof = tokenFactory.newCivlcToken(eof, o.getIncludeHistory(), TokenVocabulary.PREPROC);

				addOutput(myEof);
			} else {
				currentSource++;
				inTextBlock = false;
				inPragma = false;
				inInlineAnnotation = false;
				inBlockAnnotation = false;
				pushStream(theStreams[currentSource], theFormations[currentSource]);
				processNextNode();
			}
		} else {
			// you were at the include node. jump to next node ignoring
			// children of include node.
			jumpNextNode();
		}
	}

	/**
	 * <p>
	 * Processes a text block node by moving the current position to the first child
	 * (the first token in the text block).
	 * </p>
	 * 
	 * <p>
	 * A text block consists of a sequence of tokens that do not contain any
	 * preprocessor directives. The sequence may, however, contains macro
	 * invocations which need to be expanded.
	 * </p>
	 * 
	 * @param node a node in the tree with TEXT_BLOCK token
	 */
	private void processTextBlock(Tree textBlockNode) {
		int numChildren = textBlockNode.getChildCount();

		if (numChildren != 0)
			inTextBlock = true;
		incrementNextNode();
	}

	/**
	 * Processes a '#define' for an (object or function) macro. For an object macro,
	 * child 0 is the identifier being defined. Child 1 is the "body" node, whose
	 * children form a (possibly empty) list of tokens.
	 * 
	 * For a function macro. Child 0 is the identifier being defined. Child 1 is the
	 * formal parameter list node. Child 2 is the body.
	 * 
	 * @param node A node in the tree with token of type PDEFINE.
	 * @throws PreprocessorException if the macro has already been defined
	 *                               differently
	 */
	private void processMacroDefinition(Tree node) throws PreprocessorException {
		SourceFile sourceFile = getCurrentSource();

		if (node.getChildCount() == 3)
			processMacroDefinition(tokenFactory.newFunctionMacro(node, sourceFile));
		else
			processMacroDefinition(tokenFactory.newObjectMacro(node, sourceFile));
	}

	/**
	 * Takes a newly created Macro object and checks whether it has been defined
	 * previously. If it has, checks that the two definitions are equivalent. If the
	 * checks pass, an entry for the macro is added to the macroMap.
	 * 
	 * @param newMacro a new Macro object
	 * @throws PreprocessorException if a macro with the same name has been defined
	 *                               previously (and not undefined) in a different
	 *                               way
	 */
	private void processMacroDefinition(Macro newMacro) throws PreprocessorException {
		String name = newMacro.getName();
		Macro oldMacro = macroMap.get(name);

		if (oldMacro != null) {
			if (!oldMacro.equals(newMacro))
				throw new PreprocessorException("Attempt to redefine macro in new way: " + newMacro
						+ "\nOriginal defintion was at: " + oldMacro.getDefinitionNode());
		} else {
			macroMap.put(name, newMacro);
		}
		jumpNextNode();
	}

	/**
	 * Processes a "#error" node. Always throws an exception with a message formed
	 * from the error node's list of tokens.
	 * 
	 * @param errorNode a node of type "#error"
	 * @throws PreprocessorException always, with a message formed from the #error
	 *                               line's tokens
	 */
	private void processError(Tree errorNode) throws PreprocessorException {
		String message = "Preprocessor #error directive encountered:\n";
		int numChildren = errorNode.getChildCount();
		Token errorToken = ((CommonTree) errorNode).getToken();
		Token betterErrorToken = tokenFactory.newCivlcToken(errorToken, getIncludeHistory(), TokenVocabulary.PREPROC);
		PreprocessorException e = null;

		for (int i = 0; i < numChildren; i++) {
			Tree child = errorNode.getChild(i);
			String text = child.getText();

			message += text;
		}
		e = new PreprocessorException(message, betterErrorToken);
		e.setStackTrace(new StackTraceElement[0]);
		throw e;
	}

	private void processIf(Tree ifNode) throws PreprocessorException {
		CommonTree expressionNode = (CommonTree) ifNode.getChild(0);
		CivlcToken first, expandedFirst;
		TokenSource source;
		int result;

		assert expressionNode.getType() == PreprocessorParser.EXPR;
		// form a list of new non-whitespace tokens from the expression node...
		first = nonWhiteSpaceTokenListFromChildren(expressionNode);
		// expand all macro invocations in the expression...
		expandedFirst = expandList(first).left;
		// form a TokenSource from this list...
		source = TokenUtils.makeTokenSourceFromList(expandedFirst);
		// evaluate to get integer result...
		result = expressionAnalyzer.evaluate(source);
		// move to the appropriate point based on result...
		processConditional(ifNode, result != 0);
	}

	private void processIfdef(Tree ifdefNode) {
		processDefOrNdefNode(ifdefNode, true);
	}

	private void processIfndef(Tree ifndefNode) {
		processDefOrNdefNode(ifndefNode, false);
	}

	private void processDefOrNdefNode(Tree node, boolean isIfDef) {
		String macroName = node.getChild(0).getText();
		Macro macro = macroMap.get(macroName);
		boolean trueBranch = (isIfDef ? macro != null : macro == null);

		processConditional(node, trueBranch);
	}

	private void processConditional(Tree node, boolean takeTrueBranch) {
		if (takeTrueBranch) {
			Tree next = node.getChild(1); // TRUE_BRANCH node

			if (next.getChildCount() > 0)
				setNextInputNode(next.getChild(0));
			else
				jumpNextNode();
		} else if (node.getChildCount() > 2) {
			Tree next = node.getChild(2);

			if (next.getChildCount() > 0)
				setNextInputNode(next.getChild(0));
			else
				jumpNextNode();
		} else
			jumpNextNode();
	}

	/**
	 * Given a node in the tree, this method creates a list of new CTokens formed
	 * from children of the given node. It returns the first element in the list.
	 * White space tokens (including new lines) are filtered out.
	 * 
	 * @param root a node in the AST of a preprocessor source file
	 * @return the first node in a list of new CTokens formed from the children of
	 *         root, or null if the root has 0 children
	 * @throws PreprocessorException if one of the children has a null token
	 */
	private CivlcToken nonWhiteSpaceTokenListFromChildren(CommonTree root) throws PreprocessorException {
		int numChildren = root.getChildCount();
		CivlcToken first = null, prev = null;

		for (int i = 0; i < numChildren; i++) {
			Token token = ((CommonTree) root.getChild(i)).getToken();

			if (token == null)
				throw new PreprocessorException("Encountered null token as child " + i + " of node " + root);
			if (!PreprocessorUtils.isWhiteSpace(token)) {
				CivlcToken newToken = tokenFactory.newCivlcToken(token, getIncludeHistory(), TokenVocabulary.PREPROC);

				if (prev == null)
					first = newToken;
				else
					prev.setNext(newToken);
				prev = newToken;
			}
		}
		return first;
	}

	/**
	 * Processes a #include directive. Locates the file by searching the appropriate
	 * paths, parses it, creates a source file info for it and pushes it onto the
	 * sourceStack.
	 * 
	 * @param includeNode node in source tree of type PINCLUDE
	 * @throws PreprocessorException if the file can't be found, or if it does not
	 *                               conform to the Preprocessor grammar
	 */
	private void processInclude(Tree includeNode) throws PreprocessorException {
		int numChildren = includeNode.getChildCount();
		CommonToken firstToken = (CommonToken) ((CommonTree) includeNode.getChild(0)).getToken();
		CommonToken lastToken = (CommonToken) ((CommonTree) includeNode.getChild(numChildren - 1)).getToken();
		CivlcToken filenameToken = tokenFactory.newCivlcToken(firstToken.getInputStream(),
				PreprocessorLexer.STRING_LITERAL, firstToken.getChannel(), firstToken.getStartIndex(),
				lastToken.getStopIndex(), getIncludeHistory(), firstToken.getLine(), firstToken.getCharPositionInLine(),
				TokenVocabulary.PREPROC);
		String fullName = filenameToken.getText();
		int numChars = fullName.length();
		String name;
		boolean system;

		if (numChars < 3)
			throw new PreprocessorException("Improper file name in #include: " + fullName, filenameToken);

		int firstChar = fullName.charAt(0), lastChar = fullName.charAt(numChars - 1);

		if (firstChar == '<') {
			if (lastChar != '>')
				throw new PreprocessorException("Improper file name in #include: " + fullName, filenameToken);
			name = fullName.substring(1, numChars - 1);
			system = true;
		} else if (firstChar == '"') {
			if (lastChar != '"')
				throw new PreprocessorException("Improper file name in #include: " + fullName, filenameToken);
			name = fullName.substring(1, numChars - 1);
			system = false;
		} else {
			throw new PreprocessorException("Improper file name in #include: " + fullName, filenameToken);
		}

		Pair<File, CharStream> pair;

		try {
			pair = findIncludeStream(name, system);
		} catch (IOException e) {
			throw new PreprocessorException("I/O error when attempting to include " + name + ":\n" + e, filenameToken);
		}
		if (pair == null)
			throw new PreprocessorException("Cannot find included file " + name, filenameToken);

		SourceFile sourceFile = indexer.getOrAdd(pair.left);

		sourceFiles.add(sourceFile);
		pushStream(pair.right, tokenFactory.newInclusion(sourceFile, filenameToken));
	}

	/**
	 * Locates an include file, opens it, and creates a {@link CharStream} from it.
	 * 
	 * @param filename the name of the file, as extracted from the token
	 * @param system   <code>true</code> if angular brackets were used around the
	 *                 filename in the <code>#include</code> directive,
	 *                 <code>false</code> if double quotes were used
	 * @return if the file can't be found, returns <code>null</code>, otherwise,
	 *         returns a pair consisting of the {@link File} object which wraps the
	 *         file name and the {@link CharStream} generated from the file
	 * @throws IOException if something goes wrong trying to read or open the file
	 */
	private Pair<File, CharStream> findIncludeStream(String filename, boolean system) throws IOException {
		File file = null;
		CharStream charStream = null;

		if (!system) {
			// first look in dir containing current file, then
			// user's include paths
			File currentDir = sourceStack.peek().getFile().getFile().getParentFile();

			file = new File(currentDir, filename);
			if (!file.isFile())
				file = PreprocessorUtils.findFile(userIncludePaths, filename);
		}
		if (file == null)
			file = PreprocessorUtils.findFile(systemIncludePaths, filename);
		if (file == null) {
			// last but not least: look internally in the class path:
			return findInternalSystemFile(filename);
		} else {
			charStream = PreprocessorUtils.newFilteredCharStreamFromFile(file);
		}
		if (charStream == null)
			return null;
		return new Pair<>(file, charStream);
	}

	/**
	 * <p>
	 * Processes a <code>#pragma</code> node. For now, the whole pragma is just
	 * going to be sent to the output stream. Macro replacement will take place in
	 * the pragma tokens as usual.
	 * </p>
	 * 
	 * <p>
	 * This method sends the <code>#pragma</code> node to the output list, and moves
	 * the current position to the first child (or the next token if there are no
	 * children).
	 * </p>
	 * 
	 * <p>
	 * This sets <code>inPragma</code> to <code>true</code>. This is so that when
	 * the newline is reached that terminates the pragma, that newline will not have
	 * its channel set to the hidden channel. Hence the newline will be visible to
	 * an ANTLR parser that consumes from this source. The newline is necessary to
	 * know where the pragma ends.
	 * </p>
	 * 
	 * <p>
	 * Also sets <code>inTextBlock</code> to <code>true</code>, to avoid the expense
	 * of checking for preprocessor directives in the body of the pragma. It will
	 * get set back to <code>false</code> after leaving the pragma body.
	 * </p>
	 * 
	 * @param pragmaNode A <code>#pragma</code> node in the preprocessor tree
	 * @throws PreprocessorException should not happen
	 */
	private void processPragma(Tree pragmaNode) throws PreprocessorException {
		Token token = ((CommonTree) pragmaNode).getToken();
		CivlcToken pragmaToken = tokenFactory.newCivlcToken(token, getIncludeHistory(), TokenVocabulary.PREPROC);
		Token pragmaUniverseName = ((CommonTree) pragmaNode.getChild(1)).getToken();

		// Assign the top ACSL parse mark to true:
		if (pragmaUniverseName.getText().equals("CIVL")) {
			Token pragmaCommandName = ((CommonTree) pragmaNode.getChild(3)).getToken();

			if (pragmaCommandName.getText().equals("ACSL")) {
				parseACSLPragmaStack.pop();
				parseACSLPragmaStack.push(true);
			}
		}
		addOutput(pragmaToken);
		inPragma = true;
		inTextBlock = true;
		incrementNextNode();
	}

	/**
	 * Processes a <code>#undef</code> node. This node has 1 child, which is the
	 * identifier of the macro which is to be undefined. This removes the macro from
	 * the {@link #macroMap}. According to C11 Sec. 6.10.3.5.2, the operation is
	 * just ignored if the macro is not already defined, so this cannot lead to an
	 * error.
	 * 
	 * @param undefNode the <code>#undef</code> node
	 */
	private void processUndef(Tree undefNode) {
		Tree identifierNode = undefNode.getChild(0);
		String name = identifierNode.getText();

		macroMap.remove(name);
		jumpNextNode();
	}

	/**
	 * Processes a preprocessor "nondirective" (which, yes, is a kind of directive).
	 * For now, a no-op.
	 * 
	 * @param nondirectiveNode a preprocessor nondirective node
	 */
	private void processNondirective(Tree nondirectiveNode) {
		jumpNextNode();
	}

	/**
	 * Process a <code>#line</code> directive.
	 * 
	 * Ignored, for now.
	 * 
	 * @param lineNode preprocessor parse tree node for <code>#line</code> directive
	 */
	private void processLine(Tree lineNode) {
		// TODO

		// strategy: get the real line number from the "line" token.
		// let delta = new line number - real line number
		// when creating new CivlcTokens: add delta to line number.
		// this delta goes on the include stack: each entry has its own.
		// setting the file also affects the entry on include stack
		//
		// macro definition: when processing the macro definition, common
		// tokens from the preprocessor parse tree are used. These will
		// have original (unadjusted) line numbers. And filenames.
		// could pass arguments to CommonMacro to adjust these things.
		// or do it before calling CommonMacro.

		// macro expansion: creates new tokens by copying replacement
		// tokens and argument tokens. The replacement token copy already
		// has the right line number and should not be further modified.
		// The argument tokens should be adjusted when they are created
		// the first time and not further changed.

		// Moral: tokens should have the correct (adjusted) line/file
		// information when they are created, not just before
		// being shifted to output stream.

		// alternatively: if all line/file info ultimately comes from
		// common tokens, adjust them first before using them
		// to create CivlcTokens.

		jumpNextNode();
	}

	// Utility methods...

	/**
	 * Determines whether a node in the preprocessor parse tree is one of the
	 * conditional nodes "#if", "#ifdef", "#ifndef", "#elif".
	 * 
	 * @param node a non-<code>null</code> node in a preprocessor parse tree
	 * @return <code>true</code> iff the node is one of the preprocessor conditional
	 *         nodes, else <code>false</code>
	 */
	private static boolean isConditional(CommonTree node) {
		Token token = node.getToken();

		if (token == null)
			return false;
		else {
			int type = token.getType();

			return type == PreprocessorParser.PIF || type == PreprocessorParser.IFDEF
					|| type == PreprocessorParser.IFNDEF || type == PreprocessorParser.ELIF;
		}
	}

	/**
	 * Returns next input tree node, or <code>null</code> if end of file has been
	 * reached. Does not modify anything.
	 * 
	 * @return tree position for top entry of <code>sourceStack</code> or
	 *         <code>null</code> if <code>sourceStack</code> is empty
	 */
	private Tree getNextInputNode() {
		if (sourceStack.isEmpty())
			return null;
		return sourceStack.peek().getPosition();
	}

	private void setNextInputNode(Tree node) {
		sourceStack.peek().setPosition(node);
	}

	/**
	 * Move next node to next node in Tree in DFS order, or null if at last node.
	 * 
	 * When backtracking through a conditional node, this method will skip over the
	 * alternative branch. In other words, if we start in the "true" branch and then
	 * backtrack, we will NOT go down the false branch. This is because we only want
	 * to traverse one of the two branches when we preprocess a file.
	 * 
	 * The inTextBlock flag is turned off when we exit a block.
	 * 
	 * If given null this method is a no-op. If we are at the last node, it returns
	 * null. It should never throw an exception.
	 */
	private void incrementNextNode() {
		Tree node = getNextInputNode();

		if (node != null) {
			if (node.getChildCount() > 0)
				setNextInputNode(node.getChild(0));
			else
				jumpNextNode();
		}
	}

	/**
	 * <p>
	 * Move next node to the next sibling. If there is no next sibling, proceed as
	 * in DFS. In other words, this is the same as DFS after removing all children
	 * of the current next node.
	 * </p>
	 * 
	 * <p>
	 * When backtracking through a conditional node, this method will skip over the
	 * alternative branch. In other words, if we start in the "true" branch and then
	 * backtrack, we will NOT go down the false branch. This is because we only want
	 * to traverse one of the two branches when we preprocess a file.
	 * </p>
	 * 
	 * <p>
	 * The {@link #inTextBlock}, {@link #inInlineAnnotation}, and
	 * {@link #inBlockAnnotation} flags are turned off when we exit a block.
	 * </p>
	 * 
	 * <p>
	 * If given null this method is a no-op. If there is no node to jump to, it
	 * returns null. It should never throw an exception.
	 * </p>
	 */
	private void jumpNextNode() {
		CommonTree node = (CommonTree) getNextInputNode();

		if (node != null) {
			while (true) {
				int index = node.getChildIndex() + 1;

				node = (CommonTree) node.getParent();
				if (node == null)
					break;
				if (!isConditional(node) && index < node.getChildCount()) {
					// move to next sibling
					node = (CommonTree) node.getChild(index);
					break;
				}
				// a nontrivial backtrack is taking place
				// if you were in a TEXT_BLOCK (including in
				// an annotation) or PRAGMA, now you're not...
				inTextBlock = false;
				inInlineAnnotation = false;
				inBlockAnnotation = false;
			}
			setNextInputNode(node);
		}
	}

	private boolean peekAheadSkipWSHasType(int tokenType) {
		CommonTree node = (CommonTree) getSuccessorNode(getNextInputNode());

		while (node != null) {
			Token token = node.getToken();

			if (!PreprocessorUtils.isWhiteSpace(token)) {
				return token != null && token.getType() == tokenType;
			}
			node = (CommonTree) getSuccessorNode(node);
		}
		return false;
	}

	/**
	 * Returns the node that follows the given node in DFS order without modifying
	 * the state. Alternative conditional branches are skipped, as usual. It is
	 * useful for peeking ahead.
	 * 
	 * @param node any node in the tree.
	 * @return the next node in the DFS traversal
	 */
	private Tree getSuccessorNode(Tree node) {
		if (node == null)
			return null;
		else {
			int numChildren = node.getChildCount();

			if (numChildren > 0)
				return node.getChild(0);
			while (true) {
				int index = node.getChildIndex() + 1;

				node = node.getParent();
				if (node == null)
					return null;
				if (!isConditional((CommonTree) node) && index < node.getChildCount()) {
					return node.getChild(index);
				}
			}
		}
	}

	private Formation getIncludeHistory() {
		return sourceStack.peek().getIncludeHistory();
	}

	// Methods modifying the output list...

	/**
	 * <p>
	 * Empties the string literal buffer. The buffer contains a sequence of string
	 * literal tokens and possibly some white space tokens at the end. The string
	 * literals are concatenated to from a single string token which is inserted
	 * into the output buffer by method {@link #addOutputHelper(CivlcToken)}. Then
	 * the white space tokens are all added to the output buffer as well. Note that
	 * any white space between two string literals was already removed from this
	 * buffer, so only the white space after the last string literal in the sequence
	 * is actually sent to the output stream.
	 * </p>
	 * 
	 * <p>
	 * Precondition: <code>stringLiteralBuffer</code> is not empty.
	 * </p>
	 * 
	 * @throws PreprocessorException if the strings cannot be concatenated for some
	 *                               reason
	 */
	private void emptyStringLiteralBuffer() throws PreprocessorException {
		assert !stringLiteralBuffer.isEmpty();

		List<CivlcToken> pureStringTokens = new LinkedList<>();
		List<CivlcToken> extraWhiteSpaces = new LinkedList<>();

		for (CivlcToken stringToken : stringLiteralBuffer) {
			if (PreprocessorUtils.isWhiteSpace(stringToken))
				extraWhiteSpaces.add(stringToken);
			else
				pureStringTokens.add(stringToken);
		}
		stringLiteralBuffer.clear();
		try {
			StringToken result = pureStringTokens.size() != 1 ? tokenFactory.newStringToken(pureStringTokens)
					: tokenFactory.newStringToken(pureStringTokens.get(0));

			addOutputHelper(result);
		} catch (SyntaxException e) {
			throw new PreprocessorException(e.getMessage(), pureStringTokens.get(0));
		}
		for (CivlcToken ws : extraWhiteSpaces)
			addOutputHelper(ws);
	}

	/**
	 * <p>
	 * Adds a single token to output buffer. Special handling is needed for string
	 * literals and some other cases.
	 * </p>
	 * 
	 * <p>
	 * The string literals may be separated by some white space tokens. All of these
	 * consecutive literals (ignoring the possible white space) must be concatenated
	 * into a single string literal token. Therefore they are inserted into a
	 * separate buffer, the string literal buffer, until a token is reached that is
	 * not a string literal or white space and the buffer should be processed to
	 * form a single token which is inserted into the output stream.
	 * </p>
	 * 
	 * <p>
	 * White space is made invisible, with the following exception: a NEWLINE
	 * terminating an inline annotation or a pragma. Such a NEWLINE is part of the
	 * grammar of the CIVL-C language and is needed to delineate the end of the
	 * annotation/pragma.
	 * </p>
	 * 
	 * @param token a token to add to output buffer
	 * @throws PreprocessorException if something goes wrong concatenating strings
	 *                               or forming a character
	 */
	private void addOutput(CivlcToken token) throws PreprocessorException {
		int type = token.getType();

		if (type == PreprocessorParser.STRING_LITERAL) {
			// first remove any white space tokens at the end of the list, as
			// we throw away any white space between two adjacent string
			// literals. Then add the literal to the SLB.
			while (!stringLiteralBuffer.isEmpty() && PreprocessorUtils.isWhiteSpace(stringLiteralBuffer.getLast()))
				stringLiteralBuffer.removeLast();
			stringLiteralBuffer.add(token);
		} else if (type == PreprocessorParser.NEWLINE && (inInlineAnnotation || inPragma)) {
			// a NEWLINE while in an inlineAnnotation or pragma always
			// ends that annotation or pragma and clears the string literal
			// buffer...
			if (!stringLiteralBuffer.isEmpty())
				emptyStringLiteralBuffer();
			addOutputHelper(token); // keep that NEWLINE visible
			inInlineAnnotation = false;
			inPragma = false;
		} else if (type == PreprocessorParser.AT && (inInlineAnnotation || inBlockAnnotation)) {
			// ignore @s in annotations (that is the ACSL way)
			token.setChannel(Token.HIDDEN_CHANNEL);
			addOutputHelper(token);
		} else { // cases:
			// 1. white, empty : invisible, output
			// 2. white, nonempty : invisible, SLB
			// 3. not white: emptySLB, output
			if (PreprocessorUtils.isWhiteSpace(token)) {
				token.setChannel(Token.HIDDEN_CHANNEL);
				if (stringLiteralBuffer.isEmpty())
					addOutputHelper(token);
				else
					stringLiteralBuffer.add(token);
			} else {
				if (!stringLiteralBuffer.isEmpty())
					emptyStringLiteralBuffer();
				addOutputHelper(token);
			}
		}
	}

	/**
	 * Actually adds the token to the linked list which forms the output buffer.
	 * Makes adjustments to character tokens. Sets the index of the token to the
	 * current outputTokenCount. Increments outputTokenCount.
	 * 
	 * @param token
	 * @throws PreprocessorException
	 */
	private void addOutputHelper(CivlcToken token) throws PreprocessorException {
		int type = token.getType();

		if (type == PreprocessorParser.CHARACTER_CONSTANT) {
			try {
				token = tokenFactory.characterToken(token);
			} catch (SyntaxException e) {
				throw new PreprocessorException(e.getMessage(), e.getSource().getFirstToken());
			}
		}
		token.setIndex(outputTokenCount);
		if (saveTokens)
			theTokens.add(token);
		outputTokenCount++;
		if (firstOutput == null) {
			firstOutput = lastOutput = token;
		} else {
			lastOutput.setNext(token);
			token.setNext(null);
			lastOutput = token;
		}
	}

	private void addOutputList(Pair<CivlcToken, CivlcToken> list) throws PreprocessorException {
		CivlcToken previous = null, current = list.left;

		while (current != null) {
			previous = current;
			current = current.getNext();
			addOutput(previous);
		}
	}

	/**
	 * Creates a new {@link CivlcToken} from the given {@link Tree} node and adds
	 * the token to the end of the output list. Tokens for preprocessor keywords
	 * (e.g., "define") have their types changed to IDENTIFIER.
	 * 
	 * @param node a {@link CommonTree} node
	 * @throws PreprocessorException if something goes wrong concatenating strings
	 *                               when the token is shifted to output buffer
	 */
	private void shiftToOutput(Tree node) throws PreprocessorException {
		Token token = ((CommonTree) node).getToken();

		PreprocessorUtils.convertPreprocessorIdentifiers(token);

		CivlcToken output = tokenFactory.newCivlcToken(token, getIncludeHistory(), TokenVocabulary.PREPROC);

		addOutput(output);
	}

	/**
	 * Removes a token from the front of the output list.
	 * 
	 * @return the first token, i.e., the one removed
	 */
	private CivlcToken removeOutput() {
		CivlcToken result = firstOutput;

		if (result == null)
			throw new PreprocessorRuntimeException("Internal error: no output to remove");
		firstOutput = result.getNext();
		if (firstOutput == null)
			lastOutput = null;
		return result;
	}

	// Public methods...

	@Override
	public String toString() {
		return "PreprocessorTokenSource[" + getSourceName() + "]";
	}

	@Override
	public TokenFactory getTokenFactory() {
		return tokenFactory;
	}

	@Override
	public FileIndexer getIndexer() {
		return indexer;
	}

	/**
	 * Returns name of current file being scanned. I.e., the name returned by this
	 * method will change dynamically as new files are included.
	 */
	@Override
	public String getSourceName() {
		SourceFile sourceFile = getCurrentSource();

		if (sourceFile != null)
			return sourceFile.getPath();
		return "<unknown source>";
	}

	@Override
	public int getNumTokens() {
		return theTokens.size();
	}

	@Override
	public CivlcToken getToken(int index) {
		return theTokens.get(index);
	}

	@Override
	public Collection<SourceFile> getSourceFiles() {
		return sourceFiles;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>
	 * Returns the next token in the post-preprocessing stream. This is the main
	 * method that must be implemented in order to implement ANTLR's
	 * {@link TokenSource} interface.
	 * </p>
	 * 
	 * <p>
	 * EOF will be the last token returned, and if subsequent calls to this method
	 * are made, it will continue to return EOF forever. This seems to be what
	 * ANTLR's parsers expect.
	 * </p>
	 * 
	 * @exception PreprocessorRuntimeException if anything goes wrong in trying to
	 *                                         find the next token. Note that this
	 *                                         method cannot throw a
	 *                                         {@link PreprocessorException} because
	 *                                         the interface that it implements does
	 *                                         not specify any exception being
	 *                                         thrown
	 */
	@Override
	public CivlcToken nextToken() {
		// nextToken_calls++;
		if (finalToken != null)
			return finalToken;
		while (firstOutput == null || firstOutput.getType() != PreprocessorLexer.EOF && firstOutput == lastOutput)
			try {
				processNextNode();
			} catch (PreprocessorException e) {
				PreprocessorRuntimeException pre = new PreprocessorRuntimeException(e);
				
				pre.setStackTrace(e.getStackTrace());
				throw pre;
			} catch (PreprocessorRuntimeException e) {
				throw e;
			} catch (RuntimeException e) {
				PreprocessorRuntimeException pre = new PreprocessorRuntimeException(e.toString(), firstOutput);

				pre.setStackTrace(e.getStackTrace());
				throw pre;
			}
		if (firstOutput.getType() == PreprocessorLexer.EOF)
			finalToken = firstOutput;
		return removeOutput();
	}

}

class MacroDefinedPredicate implements StringPredicate {
	private Map<String, Macro> macroMap;

	MacroDefinedPredicate(Map<String, Macro> macroMap) {
		this.macroMap = macroMap;
	}

	@Override
	public boolean holds(String string) {
		return macroMap.containsKey(string);
	}
}
