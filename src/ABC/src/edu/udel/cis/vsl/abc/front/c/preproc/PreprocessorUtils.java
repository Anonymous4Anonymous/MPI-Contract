package edu.udel.cis.vsl.abc.front.c.preproc;

import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.ABSTRACT;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.ALIGNAS;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.ALIGNOF;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.ASSIGNS;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.ATOMIC;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.AUTO;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.BIG_O;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.BOOL;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.BREAK;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.CALLS;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.CASE;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.CHAR;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.CHOOSE;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.CIVLATOMIC;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.CIVLFOR;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.COMPLEX;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.CONST;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.CONTIN;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.CONTINUE;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.DEFAULT;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.DEFINE;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.DEFINED;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.DEPENDS;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.DERIV;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.DEVICE;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.DO;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.DOMAIN;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.DOUBLE;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.ELIF;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.ELSE;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.ENDIF;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.ENSURES;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.ENUM;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.ERROR;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.EXISTS;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.EXTERN;
//import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.FALSE;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.FATOMIC;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.FLOAT;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.FOR;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.FORALL;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.GENERIC;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.GLOBAL;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.GOTO;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.GUARD;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.HERE;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.IDENTIFIER;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.IF;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.IFDEF;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.IFNDEF;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.IMAGINARY;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.INCLUDE;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.INLINE;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.INPUT;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.INT;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.INVARIANT;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.LAMBDA;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.LINE;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.LONG;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.NORETURN;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.OUTPUT;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.PARFOR;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.PRAGMA;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.PROCNULL;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.RANGE;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.READS;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.REAL;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.REGISTER;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.REQUIRES;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.RESTRICT;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.RESULT;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.RETURN;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.SCOPEOF;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.SELF;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.SHARED;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.SHORT;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.SIGNED;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.SIZEOF;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.SPAWN;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.STATIC;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.STATICASSERT;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.STRUCT;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.SWITCH;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.SYSTEM;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.THREADLOCAL;
//import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.TRUE;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.TYPEDEF;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.TYPEOF;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.UNDEF;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.UNIFORM;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.UNION;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.UNSIGNED;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.VOID;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.VOLATILE;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.WHEN;
import static edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser.WHILE;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.Map.Entry;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenSource;

import edu.udel.cis.vsl.abc.front.IF.PreprocessorException;
import edu.udel.cis.vsl.abc.front.IF.PreprocessorExpressionException;
import edu.udel.cis.vsl.abc.util.IF.ANTLRUtils;

public class PreprocessorUtils {

	private static int identifierMinIndex, identifierMaxIndex;
	private static boolean[] identifierPredicate = initIdentifierPredicate();

	private static boolean[] initIdentifierPredicate() {
		int[] identifierIndexes = new int[]{ABSTRACT, ALIGNAS, ALIGNOF, ASSIGNS,
				ATOMIC, AUTO, BIG_O, BOOL, BREAK, CALLS, CASE, CHAR, CHOOSE,
				CIVLATOMIC, CIVLFOR, COMPLEX, CONST, CONTIN, CONTINUE, DEFAULT,
				DEFINE, DEFINED, DEPENDS, DERIV, DEVICE, DO, DOMAIN, DOUBLE,
				ELIF, ELSE, ENDIF, ENSURES, ENUM, ERROR, EXISTS, EXTERN,
				FATOMIC, FLOAT, FOR, FORALL, GENERIC, GLOBAL, GOTO, GUARD, HERE,
				IF, IFDEF, IFNDEF, IMAGINARY, INCLUDE, INLINE, INPUT, INT,
				INVARIANT, LAMBDA, LINE, LONG, NORETURN, OUTPUT, PARFOR, PRAGMA,
				PROCNULL, RANGE, READS, REAL, REGISTER, REQUIRES, RESTRICT,
				RESULT, RETURN, SCOPEOF, SELF, SHARED, SHORT, SIGNED, SIZEOF,
				SPAWN, STATIC, STATICASSERT, STRUCT, SWITCH, SYSTEM,
				THREADLOCAL, TYPEDEF, TYPEOF, UNDEF, UNIFORM, UNION, UNSIGNED,
				VOID, VOLATILE, WHEN, WHILE};
		boolean[] result;
		int length;
		int min = identifierIndexes[0], max = identifierIndexes[0];

		for (int index : identifierIndexes) {
			if (index < min)
				min = index;
			if (index > max)
				max = index;
		}
		length = max - min + 1;
		result = new boolean[length];
		for (int index : identifierIndexes)
			result[index - min] = true;
		identifierMinIndex = min;
		identifierMaxIndex = max;
		return result;
	}

	/**
	 * Is the token a preprocessor identifier. That would be any C identifier
	 * (type IDENTIFIER) or any keyword.
	 * 
	 * @param token
	 *            any token
	 * @return true iff the token's type is either IDENTIFIER or any of the
	 *         types in the list of C keywords.
	 */
	public static boolean isIdentifier(Token token) {
		int type = token.getType();

		if (type == PreprocessorParser.IDENTIFIER)
			return true;
		return type >= identifierMinIndex && type <= identifierMaxIndex
				&& identifierPredicate[type - identifierMinIndex];
	}

	public static boolean isPpNumber(Token token) {
		int type = token.getType();

		return type == PreprocessorParser.INTEGER_CONSTANT
				|| type == PreprocessorParser.FLOATING_CONSTANT
				|| type == PreprocessorParser.PP_NUMBER;
	}

	/**
	 * Is the preprocessor token considered a white space token? Spaces, tabs,
	 * newlines, comments are all white space.
	 * 
	 * @param token
	 *            any token defined in the PreprocessorParser
	 * @return true iff token is a form of white space
	 */
	public static boolean isWhiteSpace(Token token) {
		int type = token.getType();

		return type == PreprocessorParser.WS
				|| type == PreprocessorParser.COMMENT
				|| type == PreprocessorParser.NEWLINE;
	}

	/**
	 * This convenience method transforms a TokenSource by filtering out the
	 * white space tokens. The TokenSource returned is equivalent to the given
	 * TokenSource, except that all white space tokens (spaces, tabs, newlines)
	 * have been removed.
	 * 
	 * @param oldSource
	 *            a token source that might have white space
	 * @return a new token source equivalent to old but with white space tokens
	 *         removed
	 */
	public static TokenSource filterWhiteSpace(final TokenSource oldSource) {
		TokenSource newSource = new TokenSource() {
			@Override
			public String getSourceName() {
				return oldSource.getSourceName();
			}

			@Override
			public Token nextToken() {
				while (true) {
					Token token = oldSource.nextToken();

					if (!isWhiteSpace(token))
						return token;
				}
			}
		};
		return newSource;
	}

	public static Integer convertStringToInt(String text)
			throws PreprocessorExpressionException {
		String stripped, root;
		// String suffix;
		int length = text.length();
		Integer result;

		while (length >= 1) {
			char c = text.charAt(length - 1);

			if (c != 'U' && c != 'u' && c != 'l' && c != 'L')
				break;
			length--;
		}
		stripped = text.substring(0, length);
		// TODO: do anything with suffix?
		// suffix = text.substring(length);
		try {
			if (stripped.startsWith("0")) {
				if (stripped.startsWith("0x") || stripped.startsWith("0X")) {
					// hexadecimal
					root = stripped.substring(2);
					result = Integer.parseInt(root, 16);
				} else {
					// octal
					result = Integer.parseInt(stripped, 8);
				}
			} else {
				// decimal
				result = Integer.valueOf(stripped);
			}
		} catch (NumberFormatException e) {
			throw new PreprocessorExpressionException(
					"Unable to extract integer value from " + text + ":\n" + e);
		}
		return result;
	}

	public static Double convertStringToDouble(String text)
			throws PreprocessorExpressionException {
		// TODO: fix
		return Double.valueOf(text);
	}

	/**
	 * If the token has type which is one of the preprocessor keywords (e.g.,
	 * DEFINE, which is the type of the token "define"), it is changed to the
	 * IDENTIFIER type. Otherwise, the token is unchanged.
	 * 
	 * @param token
	 *            any preprocessing token (including whitespace)
	 */
	public static void convertPreprocessorIdentifiers(Token token) {
		int tokenType = token.getType();

		switch (tokenType) {
			case DEFINE :
			case DEFINED :
			case ELIF :
			case ENDIF :
			case ERROR :
			case IFDEF :
			case IFNDEF :
			case INCLUDE :
			case LINE :
			case PRAGMA :
			case UNDEF :
				token.setType(IDENTIFIER);
			default :
		}
	}

	/**
	 * Prints the stream of tokens emanating from a token source. Used mainly
	 * for debugging. Uses the tokens' "toString" method.
	 * 
	 * @param out
	 *            a print stream to which the output is sent
	 * @param source
	 *            any instance of TokenSource
	 * @throws PreprocessorException
	 *             if any exception is thrown while printing a token or getting
	 *             the next token. CommonToken's toString method can throw all
	 *             manner of exceptions
	 */
	public static void printTokenSource(PrintStream out, TokenSource source)
			throws PreprocessorException {
		try {
			CommonToken token;

			do {
				token = (CommonToken) source.nextToken();
				out.println(token.toString());
				out.flush();
			} while (token.getType() != PreprocessorParser.EOF);
		} catch (RuntimeException e) {
			e.printStackTrace(System.err);
			throw new PreprocessorException(e.toString());
		}
	}

	public static void sourceTokenSource(PrintStream out, TokenSource source)
			throws PreprocessorException {
		try {
			CommonToken token;
			int type;

			while (true) {
				token = (CommonToken) source.nextToken();
				type = token.getType();
				if (type == PreprocessorParser.EOF)
					break;
				if (type == PreprocessorParser.COMMENT)
					out.print(" ");
				else
					out.print(token.getText());
				out.flush();
			}
		} catch (RuntimeException e) {
			e.printStackTrace(out);
			throw new PreprocessorException(e.getMessage());
		}
	}

	public static void source(PrintStream out, File file)
			throws PreprocessorException {
		try {
			ANTLRUtils.source(out, file);
		} catch (IOException e) {
			e.printStackTrace(out);
			throw new PreprocessorException(e.toString());
		}
	}

	public static CharStream newFilteredCharStreamFromFile(File file)
			throws IOException {
		InputStream inputStream = new FileInputStream(file);
		CharStream charStream = new CommonCharacterStream(
				file.getAbsolutePath(), inputStream);
		CharStream filteredStream = new FilteredStream(charStream);

		return filteredStream;
	}

	/**
	 * Creates new filtered character stream from the specified internal
	 * resource. Used to read files that are stored inside the class path
	 * (including inside a jar file).
	 * 
	 * @param name
	 *            a name to assign to the stream; used only for reporting
	 *            errors, referring to the stream, etc.
	 * @param resource
	 *            the actual name of the resource, which is absolute path
	 *            relative to the class path
	 * @return the character stream or <code>null</code> if the resource could
	 *         not be found
	 * @throws IOException
	 *             if something goes wrong reading from the stream
	 */
	public static CharStream newFilteredCharStreamFromResource(String name,
			String resource) throws IOException {
		InputStream inputStream = PreprocessorUtils.class
				.getResourceAsStream(resource);

		if (inputStream == null)
			return null;

		CharStream charStream = new CommonCharacterStream(name, inputStream);
		CharStream filteredStream = new FilteredStream(charStream);

		return filteredStream;
	}

	/**
	 * Find the file with the given name by looking through the directories in
	 * the given list. Go through list from first to last. Returns first
	 * instance found.
	 * 
	 * Note: the filename may itself containing directory structure, e.g.,
	 * "sys/stdio.h".
	 * 
	 * @param paths
	 *            list of directories to search
	 * @param filename
	 *            name of file
	 * @return file named filename, or null if not found
	 */
	public static File findFile(File[] paths, String filename) {
		for (File path : paths) {
			File result = new File(path, filename);

			if (result.isFile())
				return result;
		}
		return null;
	}

	/**
	 * Converts a macro map to an ANTLR character stream. The macro map
	 * specifies macros as key-value pairs, where the key is the name of the
	 * macro (and possible formal parameter list, if the macro is a
	 * function-like macro) and the value is the body. The character stream
	 * return follows the C preprocessor format: a sequence of
	 * newline-terminated lines of the form "#define NAME BODY"
	 * 
	 * @param macroMap
	 *            map from macro names to bodies
	 * @return character stream defining macros in the C preprocessor format
	 */
	public static CharStream macroMapToCharStream(
			Map<String, String> macroMap) {
		StringBuffer sb = new StringBuffer();

		for (Entry<String, String> entry : macroMap.entrySet()) {
			sb.append("#define ");
			sb.append(entry.getKey());
			sb.append(" ");
			sb.append(entry.getValue());
			sb.append(System.lineSeparator());
		}

		int n = sb.length();
		char[] charArray = new char[n];

		sb.getChars(0, n, charArray, 0);
		return new ANTLRStringStream(charArray, n);
	}

}
