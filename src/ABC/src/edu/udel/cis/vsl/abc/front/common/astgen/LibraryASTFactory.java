package edu.udel.cis.vsl.abc.front.common.astgen;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import edu.udel.cis.vsl.abc.ast.IF.AST;
import edu.udel.cis.vsl.abc.config.IF.Configurations.Language;
import edu.udel.cis.vsl.abc.err.IF.ABCException;
import edu.udel.cis.vsl.abc.front.IF.ASTBuilder;
import edu.udel.cis.vsl.abc.front.IF.ParseException;
import edu.udel.cis.vsl.abc.front.IF.ParseTree;
import edu.udel.cis.vsl.abc.front.IF.Parser;
import edu.udel.cis.vsl.abc.front.IF.Preprocessor;
import edu.udel.cis.vsl.abc.front.IF.PreprocessorException;
import edu.udel.cis.vsl.abc.main.ABCExecutor;
import edu.udel.cis.vsl.abc.main.TranslationTask;
import edu.udel.cis.vsl.abc.main.TranslationTask.TranslationStage;
import edu.udel.cis.vsl.abc.main.UnitTask;
import edu.udel.cis.vsl.abc.token.IF.CivlcTokenSource;
import edu.udel.cis.vsl.abc.token.IF.SyntaxException;

/**
 * A factory for producing the AST of any system library translation unit, given
 * a library file name.
 * 
 * @author xxxx
 *
 */
public class LibraryASTFactory {

	public final static String STDLIB = "stdlib.h";

	public final static String STDIO = "stdio.h";

	public final static String OMP = "omp.h";

	public final static String MATH = "math.h";
	
	public final static String CIVLC = "civlc.cvh";
	
	public final static String FORTRAN_ARRAY = "fortran_array.cvh";

	private final static Map<String, String> EMPTY_MACRO_MAP = new HashMap<>();

	private Preprocessor preprocessor;

	private Parser parser;

	private ASTBuilder astBuilder;

	/**
	 * Constructs new library AST factory from given front-end components.
	 * 
	 * @param preprocessor
	 *            the preprocessor that will be used to preprocess the library
	 *            file
	 * @param parser
	 *            the parser that will be used to parse the resulting token
	 *            stream
	 * @param astBuilder
	 *            the builder that will translate the parse tree into an ASt
	 */
	public LibraryASTFactory(Preprocessor preprocessor, Parser parser,
			ASTBuilder astBuilder) {
		this.preprocessor = preprocessor;
		this.parser = parser;
		this.astBuilder = astBuilder;
	}

	/**
	 * Constructs the raw (unanalyzed) AST for the translation unit specified by
	 * a standard library file name.
	 * 
	 * @param name
	 *            the file name of the system library file, including the suffix
	 *            (e.g., ".h" or ".cvh") but not including a directory; e.g.,
	 *            "stdlib.h"
	 * @return the raw AST for the specified translation unit
	 * @throws PreprocessorException
	 *             if something goes wrong preprocessing the file (opening and
	 *             reading the file, tokenizing the character stream, executing
	 *             preprocessor directives, and generating CIVLC tokens)
	 * @throws ParseException
	 *             if something goes wrong parsing the file (converting the
	 *             token stream to a parse tree)
	 * @throws SyntaxException
	 *             if something goes wrong translating the parse tree to an AST
	 */
	public AST getASTofLibrary(String name)
			throws PreprocessorException, ParseException, SyntaxException {
		CivlcTokenSource tokenSource = preprocessor
				.preprocessLibrary(EMPTY_MACRO_MAP, name);
		ParseTree parseTree = parser.parse(tokenSource);

		return astBuilder.getTranslationUnit(parseTree);
	}

	/**
	 * Constructs the raw (unanalyzed) AST for the translation unit specified by
	 * a standard library file name.
	 * 
	 * @param file
	 *            the file of the system library file, including the path to the
	 *            file but not including a directory; e.g.,
	 *            "/include/abc/stdlib.h"
	 * @param language
	 *            the language of the library
	 * @return the raw AST for the specified translation unit
	 * @throws ABCException
	 *             if something goes wrong while preprocessing, parsing and
	 *             translating the library file
	 */
	public AST getASTofLibrary(File file, Language language)
			throws ABCException {
		UnitTask task = new UnitTask(new File[]{file});

		task.setLanguage(language);

		TranslationTask translation = new TranslationTask(new UnitTask[]{task});

		translation.setStage(TranslationStage.GENERATE_ASTS);

		ABCExecutor executor = new ABCExecutor(translation);

		executor.execute();
		return executor.getAST(0);
	}
}
