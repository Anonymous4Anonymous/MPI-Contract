package edu.udel.cis.vsl.abc.util.IF;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenSource;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;

public class ANTLRUtils {

	/**
	 * Pretty-prints a parse tree.
	 * 
	 * @param out
	 *            the stream to which output should be sent
	 * @param tree
	 *            the tree to print. May be null.
	 */
	public static void printTree(PrintStream out, Tree tree) {
		if (tree == null) {
			out.println("null");
		} else {
			printNode(out, tree, "");
		}
	}

	/**
	 * Pretty-prints tree using one line per node and nice indentation.
	 * 
	 * @param out
	 *            stream to which output should be sent
	 * @param node
	 *            a non-null instance of CommonTree
	 * @param prefix
	 *            any text you wish to precede output
	 */
	private static void printNode(PrintStream out, Tree node, String prefix) {
		int numChildren;
		Token token = ((CommonTree) node).getToken();
		String nodeString = (token == null ? "null" : token.toString());

		out.print(prefix);
		out.println(nodeString);
		out.flush();
		numChildren = node.getChildCount();
		if (numChildren > 0) {
			String newPrefix = prefix + "| ";

			for (int i = 0; i < numChildren; i++) {
				Tree child = node.getChild(i);

				printNode(out, child, newPrefix);
			}
		}
	}

	/**
	 * Applies method source to the file with the given filename.
	 * 
	 * @param out
	 *            a PrintStream to which the output is sent
	 * @param filename
	 *            name of a file
	 * @throws IOException
	 */
	public static void source(PrintStream out, String filename)
			throws IOException {
		source(out, new File(filename));
	}

	/**
	 * Looks for file in file system and as resource located under class path;
	 * returns buffered reader in either case. Returns <code>null</code> if file
	 * cannot be found in either location.
	 * 
	 * @param file
	 *            the file which could be name of resource
	 * @return buffered reader or <code>null</code> i
	 */
	private static BufferedReader readFileOrResource(File file) {
		Reader reader;

		try {
			reader = new FileReader(file);
		} catch (FileNotFoundException e) {
			reader = null;
		}
		if (reader == null) {
			InputStream stream = ANTLRUtils.class
					.getResourceAsStream(file.getAbsolutePath());

			if (stream == null)
				reader = null;
			else
				reader = new InputStreamReader(stream);
		}
		if (reader == null)
			return null;
		else
			return new BufferedReader(reader);
	}

	/**
	 * Prints the original text file to the give output stream, unaltered.
	 * 
	 * @param out
	 *            a PrintStream to which the output is sent
	 * @param file
	 *            the file to read
	 * @throws IOException
	 *             if an I/O exception occurs while reading the file
	 */
	public static void source(PrintStream out, File file) throws IOException {
		out.println("Contents of file " + file + ":\n");
		out.println("----------------------------------->");

		BufferedReader bufferedReader = readFileOrResource(file);

		if (bufferedReader == null) {
			throw new IOException("Cannot find file " + file.getAbsolutePath());
		}

		String line;

		while ((line = bufferedReader.readLine()) != null)
			out.println(line);
		out.println("<-----------------------------------");
		out.flush();
		bufferedReader.close();
		// I _think_ that closes all the underlying readers/streams
	}

	/**
	 * Prints all tokens from the token source.
	 * 
	 * @param out
	 *            where to print
	 * @param ts
	 *            token source
	 */
	public static void print(PrintStream out, TokenSource ts) {
		while (true) {
			Token token = ts.nextToken();

			out.println(token);
			if (token.getType() == Token.EOF)
				break;
		}
		out.flush();
	}

	public interface LexerFactory {
		Lexer makeLexer(CharStream stream);
	}

	public static void lex(PrintStream out, LexerFactory lf, String filename)
			throws IOException {
		ANTLRFileStream stream = new ANTLRFileStream(filename);
		Lexer lexer = lf.makeLexer(stream);

		while (true) {
			Token t = lexer.nextToken();

			if (t == null)
				break;
			if (t.getType() == 0) {
				out.println("TOKEN[0]");
				out.flush();
				break;
			} else
				out.println(t);
			out.flush();
			if (t.getType() == Token.EOF)
				break;
		}
	}

}
