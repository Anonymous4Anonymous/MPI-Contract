package edu.udel.cis.vsl.abc.front.fortran.parse;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;

import edu.udel.cis.vsl.abc.front.IF.ParseException;
import edu.udel.cis.vsl.abc.front.IF.ParseTree;
import edu.udel.cis.vsl.abc.front.IF.Parser;
import edu.udel.cis.vsl.abc.front.fortran.preproc.MFortranLexicalPrepass;
import edu.udel.cis.vsl.abc.front.fortran.preproc.PP2CivlcTokenMFortranConverter;
import edu.udel.cis.vsl.abc.token.IF.CivlcTokenSource;

public class MFParser implements Parser {

	/* **************** Constructor **************** */
	public MFParser() {
	}

	/* **************** Public Methods **************** */
	@Override
	public ParseTree parse(CivlcTokenSource tokenSource) throws ParseException {
		TokenStream stream = new CommonTokenStream(tokenSource);
		PP2CivlcTokenMFortranConverter converter = new PP2CivlcTokenMFortranConverter();

		stream = converter.convert(stream);

		MFortranLexicalPrepass prepass = new MFortranLexicalPrepass(stream);
		TokenStream fstream = prepass.performPrepass();
		MFortranParser2018 parser = new MFortranParser2018(fstream);

		parser.initialize();
		while (fstream.LA(1) != MFortranParser2018.EOF) {
			try {
				if (parseProgramUnit(fstream, parser))
					throw new ParseException(
							"encounter error when parsing the fortran token stream");
			} catch (RecognitionException e) {
				throw new ParseException(e.getMessage());
			}
		}
		return parser.getAction().getFortranParseTree();
	}

	/* **************** Private Helper Methods **************** */

	private static boolean parseMainProgram(TokenStream tokens,
			MFortranParser2018 parser, int start) throws RecognitionException {
		// try parsing the main program
		parser.main_program();

		return parser.hasError();
	} // end parseMainProgram()

	private static boolean parseModule(TokenStream tokens,
			MFortranParser2018 parser, int start) throws RecognitionException {
		parser.module();
		return parser.hasError();
	} // end parseModule()

	private static boolean parseSubmodule(TokenStream tokens,
			MFortranParser2018 parser, int start) throws RecognitionException {
		parser.submodule();
		return parser.hasError();
	} // end parseSubmodule()

	private static boolean parseBlockData(TokenStream tokens,
			MFortranParser2018 parser, int start) throws RecognitionException {
		parser.block_data();

		return parser.hasError();
	} // end parseBlockData()

	private static boolean parseSubroutine(TokenStream tokens,
			MFortranParser2018 parser, int start) throws RecognitionException {
		parser.subroutine_subprogram();

		return parser.hasError();
	} // end parserSubroutine()

	private static boolean parseFunction(TokenStream tokens,
			MFortranParser2018 parser, int start) throws RecognitionException {
		parser.ext_function_subprogram();
		return parser.hasError();
	} // end parseFunction()

	private static boolean parseProgramUnit(TokenStream tokens,
			MFortranParser2018 parser) throws RecognitionException {
		int firstToken;
		int lookAhead = 1;
		int start;
		boolean error = false;

		// check for opening with an include file
		parser.checkForInclude();

		// first token on the *line*. will check to see if it's
		// equal to module, block, etc. to determine what rule of
		// the grammar to start with.
		try {
			lookAhead = 1;
			do {
				firstToken = tokens.LA(lookAhead);
				lookAhead++;
			} while (firstToken == MFortranParser2018.LINE_COMMENT
					|| firstToken == MFortranParser2018.EOS);

			// mark the location of the first token we're looking at
			start = tokens.mark();

			// attempt to match the program unit
			// each of the parse routines called will first try and match
			// the unit they represent (function, block, etc.). if that
			// fails, they may or may not try and match it as a main
			// program; it depends on how it fails.
			//
			// due to Sale's algorithm, we know that if the token matches
			// then the parser should be able to successfully match.
			if (firstToken != MFortranParser2018.EOF) {
				if (firstToken == MFortranParser2018.MODULE && tokens
						.LA(lookAhead) != MFortranParser2018.PROCEDURE) {
					// try matching a module
					error = parseModule(tokens, parser, start);
				} else if (firstToken == MFortranParser2018.SUBMODULE) {
					// try matching a submodule
					error = parseSubmodule(tokens, parser, start);
				} else if (firstToken == MFortranParser2018.BLOCKDATA
						|| (firstToken == MFortranParser2018.BLOCK && tokens
								.LA(lookAhead) == MFortranParser2018.DATA)) {
					// try matching block data
					error = parseBlockData(tokens, parser, start);
				} else if (lookForToken(tokens,
						MFortranParser2018.SUBROUTINE) == true) {
					// try matching a subroutine
					error = parseSubroutine(tokens, parser, start);
				} else if (lookForToken(tokens,
						MFortranParser2018.FUNCTION) == true) {
					// try matching a function
					error = parseFunction(tokens, parser, start);
				} else {
					// what's left should be a main program
					error = parseMainProgram(tokens, parser, start);
				} // end else(unhandled token)
			} // end if(file had nothing but comments empty)
		} catch (RecognitionException e) {
			e.printStackTrace();
			error = true;
		} // end try/catch(parsing program unit)

		return error;
	} // end parseProgramUnit()

	private static boolean lookForToken(TokenStream stream, int tokenType) {
		int numLA = 1;
		int curType = -1;

		do {
			curType = stream.LA(numLA++);
		} while (curType != MFortranParser2018.EOS
				&& curType != MFortranParser2018.EOF && curType != tokenType);
		return curType == tokenType;
	}
}
