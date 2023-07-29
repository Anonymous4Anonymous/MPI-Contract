package edu.udel.cis.vsl.abc.front.c.parse;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenSource;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;

import edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorLexer;
import edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorUtils;
import edu.udel.cis.vsl.abc.front.common.parse.OmpPragmaParser;
import edu.udel.cis.vsl.abc.token.IF.Source;
import edu.udel.cis.vsl.abc.token.IF.SyntaxException;
import edu.udel.cis.vsl.abc.util.IF.ANTLRUtils;

public class CAcslParser implements OmpPragmaParser {

	@Override
	public CommonTree parse(Source source, TokenStream tokens)
			throws SyntaxException {
		AcslParser parser = new AcslParser(tokens);

		try {
			return (CommonTree) parser.contract().getTree();
		} catch (RecognitionException e) {
			throw new SyntaxException(e.getMessage(), null);
		}
	}

	/**
	 * A method to perform simple tests. Takes as input a filename. The file
	 * should contain only an ACSL contract. It will be parsed using the ACSL
	 * grammar (AcslParser.g) rule for contract.
	 * 
	 * @param args
	 *            array of length 1: the filename
	 */
	public final static void main(String[] args)
			throws IOException, RecognitionException {
		if (args.length != 1)
			throw new IllegalArgumentException("Takes one arg: the file name");

		PrintStream out = System.out;
		File file = new File(args[0]);
		CharStream charStream = PreprocessorUtils
				.newFilteredCharStreamFromFile(file);
		PreprocessorLexer lexer = new PreprocessorLexer(charStream);
		TokenSource source = PreprocessorUtils.filterWhiteSpace(lexer);

		out.println("Lexer output:");
		ANTLRUtils.print(out, source);
		out.println();
		out.println("Parser output:");
		charStream = PreprocessorUtils.newFilteredCharStreamFromFile(file);
		lexer = new PreprocessorLexer(charStream);
		source = PreprocessorUtils.filterWhiteSpace(lexer);

		TokenStream tokenStream = new CommonTokenStream(source);
		AcslParser parser = new AcslParser(tokenStream);
		Tree tree = (Tree) parser.contract().getTree();

		ANTLRUtils.printTree(System.out, tree);
	}
}
