package edu.udel.cis.vsl.abc.front.fortran.preproc;

import static edu.udel.cis.vsl.abc.token.IF.CivlcToken.TokenVocabulary.FORTRAN;
import static edu.udel.cis.vsl.abc.token.IF.CivlcToken.TokenVocabulary.PREPROC;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.TokenStream;

import edu.udel.cis.vsl.abc.config.IF.Configurations;
import edu.udel.cis.vsl.abc.config.IF.Configurations.Language;
import edu.udel.cis.vsl.abc.err.IF.ABCRuntimeException;
import edu.udel.cis.vsl.abc.front.IF.PP2CivlcTokenConversionException;
import edu.udel.cis.vsl.abc.front.IF.PP2CivlcTokenConverter;
import edu.udel.cis.vsl.abc.front.IF.Preprocessor;
import edu.udel.cis.vsl.abc.front.IF.PreprocessorException;
import edu.udel.cis.vsl.abc.front.c.preproc.CPreprocessor;
import edu.udel.cis.vsl.abc.front.c.preproc.PreprocessorParser;
import edu.udel.cis.vsl.abc.main.ABC;
import edu.udel.cis.vsl.abc.token.IF.CivlcToken;
import edu.udel.cis.vsl.abc.token.IF.TokenFactory;
import edu.udel.cis.vsl.abc.token.IF.Tokens;

/**
 * 
 * 
 * @author Wenhao Wu (xxxx@udel.edu)
 */
public class PP2CivlcTokenMFortranConverter implements PP2CivlcTokenConverter {

	private static final int FIXED_FORM_STATEMENT_START = 6; // Start from col 6
	private static final int ANTLR_IGNORED_CHANNEL = 99;
	static final String PATTERN_LBL_LOOPVAR = "^[0-9]+[a-zA-Z_]+$";
	static final String PATTERN_INTEGER = "^[0-9]+";
	private boolean isFixedForm = false;
	private HashMap<String, Integer> relOps = new HashMap<String, Integer>();
	private Set<String> endkeywords = new HashSet<String>();
	private HashMap<String, Integer> keywords = new HashMap<String, Integer>();
	private HashMap<Integer, Integer> symbols = new HashMap<Integer, Integer>();
	private TokenFactory tf = Tokens.newTokenFactory();

	public PP2CivlcTokenMFortranConverter() {
		initEndKeywordSet();
		initId2KeywordMap();
		initPP2MFokenTypedMap();
		initId2RelOpMap();
	}

	@Override
	public TokenStream convert(TokenStream stream) {
		CivlcToken cur = null;
		String srcFileName = null;
		TokenStream tstream = null;

		stream.toString();
		cur = (CivlcToken) stream.get(0);
		assert (cur != null);
		srcFileName = cur.getSourceFile().getName();
		try {
			if (srcFileName.toLowerCase().endsWith(".f"))
				tstream = convertFixedForm(stream);
			else
				tstream = convertFreeForm(stream);
		} catch (PP2CivlcTokenConversionException e) {
			e.printStackTrace();
		}
		return tstream;
	}

	// Initialization
	private void initPP2MFokenTypedMap() {
		symbols.put(PreprocessorParser.ASSIGN, MFortranLexer.EQUALS);
		symbols.put(PreprocessorParser.COMMA, MFortranLexer.COMMA);
		symbols.put(PreprocessorParser.COLON, MFortranLexer.COLON);
		symbols.put(PreprocessorParser.CHARACTER_CONSTANT,
				MFortranLexer.CHAR_CONST);
		symbols.put(PreprocessorParser.DIV, MFortranLexer.SLASH);
		symbols.put(PreprocessorParser.DIVEQ, MFortranLexer.NE);
		symbols.put(PreprocessorParser.ELSE, MFortranLexer.ELSE);
		symbols.put(PreprocessorParser.EQUALS, MFortranLexer.EQ_EQ);
		symbols.put(PreprocessorParser.GT, MFortranLexer.GT);
		symbols.put(PreprocessorParser.INTEGER_CONSTANT,
				MFortranLexer.DIGIT_STR);
		symbols.put(PreprocessorParser.IMPLIES, MFortranLexer.EQ_GT);
		symbols.put(PreprocessorParser.INPUT, MFortranLexer.CIVL_PRIMITIVE);
		symbols.put(PreprocessorParser.LPAREN, MFortranLexer.LPAREN);
		symbols.put(PreprocessorParser.LT, MFortranLexer.LT);
		symbols.put(PreprocessorParser.LTE, MFortranLexer.LE);
		symbols.put(PreprocessorParser.MOD, MFortranLexer.PERCENT);
		symbols.put(PreprocessorParser.OUTPUT, MFortranLexer.CIVL_PRIMITIVE);
		symbols.put(PreprocessorParser.PLUS, MFortranLexer.PLUS);
		symbols.put(PreprocessorParser.RPAREN, MFortranLexer.RPAREN);
		symbols.put(PreprocessorParser.SEMI, MFortranLexer.EOS);
		symbols.put(PreprocessorParser.STAR, MFortranLexer.ASTERISK);
		symbols.put(PreprocessorParser.STRING_LITERAL,
				MFortranLexer.CHAR_CONST);
		symbols.put(PreprocessorParser.CHARACTER_CONSTANT,
				MFortranLexer.CHAR_CONST);
		symbols.put(PreprocessorParser.SUB, MFortranLexer.MINUS);
		symbols.put(PreprocessorParser.WS, MFortranLexer.WS);
	}

	private void initId2RelOpMap() {
		relOps.put("AND", MFortranLexer.AND);
		relOps.put("EQ", MFortranLexer.EQ);
		relOps.put("EQV", MFortranLexer.EQV);
		relOps.put("FALSE", MFortranLexer.FALSE);
		relOps.put("GE", MFortranLexer.GE);
		relOps.put("GT", MFortranLexer.GT);
		relOps.put("LE", MFortranLexer.LE);
		relOps.put("LT", MFortranLexer.LT);
		relOps.put("NE", MFortranLexer.NE);
		relOps.put("NEQV", MFortranLexer.NEQV);
		relOps.put("NOT", MFortranLexer.NOT);
		relOps.put("OR", MFortranLexer.OR);
		relOps.put("TRUE", MFortranLexer.TRUE);
	}

	private void initEndKeywordSet() {
		endkeywords.add("ENDASSOCIATE");
		endkeywords.add("ENDCOTARGETBLOCK");
		endkeywords.add("ENDBLOCKDATA");
		endkeywords.add("ENDCRITICAL");
		endkeywords.add("ENDDO");
		endkeywords.add("ENDENUM");
		endkeywords.add("ENDFILE");
		endkeywords.add("ENDFORALL");
		endkeywords.add("ENDFUNCTION");
		endkeywords.add("ENDIF");
		endkeywords.add("ENDMODULE");
		endkeywords.add("ENDINTERFACE");
		endkeywords.add("ENDPROCEDURE");
		endkeywords.add("ENDPROGRAM");
		endkeywords.add("ENDSELECT");
		endkeywords.add("ENDSUBMODULE");
		endkeywords.add("ENDSUBROUTINE");
		endkeywords.add("ENDTEAM");
		endkeywords.add("ENDTYPE");
		endkeywords.add("ENDWHERE");
	}

	private void initId2KeywordMap() {
		/*
		 * Keywords
		 */
		keywords.put("ABSTRACT", MFortranLexer.ABSTRACT);
		keywords.put("ACQUIRED_LOCK", MFortranLexer.ACQUIRED_LOCK);
		keywords.put("ALL", MFortranLexer.ALL);
		keywords.put("ALLOCATABLE", MFortranLexer.ALLOCATABLE);
		keywords.put("ALLOCATE", MFortranLexer.ALLOCATE);
		keywords.put("ASSIGNMENT", MFortranLexer.ASSIGNMENT);
		// keywords.put("ASSIGN", MFortranLexer.ASSIGN);
		keywords.put("ASSOCIATE", MFortranLexer.ASSOCIATE);
		keywords.put("ASYNCHRONOUS", MFortranLexer.ASYNCHRONOUS);
		keywords.put("BACKSPACE", MFortranLexer.BACKSPACE);
		keywords.put("BIND", MFortranLexer.BIND);
		keywords.put("BLOCK", MFortranLexer.BLOCK);
		keywords.put("BLOCKDATA", MFortranLexer.BLOCKDATA);
		keywords.put("CALL", MFortranLexer.CALL);
		keywords.put("CASE", MFortranLexer.CASE);
		keywords.put("CHARACTER", MFortranLexer.CHARACTER);
		keywords.put("CLASS", MFortranLexer.CLASS);
		keywords.put("CLOSE", MFortranLexer.CLOSE);
		keywords.put("CODIMENSION", MFortranLexer.CODIMENSION);
		keywords.put("COMMON", MFortranLexer.COMMON);
		keywords.put("COMPLEX", MFortranLexer.COMPLEX);
		keywords.put("CONCURRENT", MFortranLexer.CONCURRENT);
		keywords.put("CONTAINS", MFortranLexer.CONTAINS);
		keywords.put("CONTIGUOUS", MFortranLexer.CONTIGUOUS);
		keywords.put("CONTINUE", MFortranLexer.CONTINUE);
		keywords.put("CRITICAL", MFortranLexer.CRITICAL);
		keywords.put("CYCLE", MFortranLexer.CYCLE);
		keywords.put("DATA", MFortranLexer.DATA);
		keywords.put("DEFAULT", MFortranLexer.DEFAULT);
		keywords.put("DEALLOCATE", MFortranLexer.DEALLOCATE);
		keywords.put("DEFERRED", MFortranLexer.DEFERRED);
		keywords.put("DIMENSION", MFortranLexer.DIMENSION);
		keywords.put("DO", MFortranLexer.DO);
		keywords.put("DOUBLE", MFortranLexer.DOUBLE);
		keywords.put("DOUBLEPRECISION", MFortranLexer.DOUBLEPRECISION);
		keywords.put("DOUBLECOMPLEX", MFortranLexer.DOUBLECOMPLEX);
		keywords.put("ELEMENTAL", MFortranLexer.ELEMENTAL);
		keywords.put("ELSE", MFortranLexer.ELSE);
		keywords.put("ELSEIF", MFortranLexer.ELSEIF);
		keywords.put("ELSEWHERE", MFortranLexer.ELSEWHERE);
		keywords.put("END", MFortranLexer.END);
		// keywords.put("ENDASSOCIATE", MFortranLexer.ENDASSOCIATE);
		// keywords.put("ENDBLOCKDATA", MFortranLexer.ENDBLOCKDATA);
		// keywords.put("ENDCOTARGETBLOCK", MFortranLexer.ENDBLOCK);
		// keywords.put("ENDCRITICAL", MFortranLexer.ENDCRITICAL);
		// keywords.put("ENDDO", MFortranLexer.ENDDO);
		// keywords.put("ENDENUM", MFortranLexer.ENDENUM);
		// keywords.put("ENDFILE", MFortranLexer.ENDFILE);
		// keywords.put("ENDFORALL", MFortranLexer.ENDFORALL);
		// keywords.put("ENDFUNCTION", MFortranLexer.ENDFUNCTION);
		// keywords.put("ENDIF", MFortranLexer.ENDIF);
		// keywords.put("ENDINTERFACE", MFortranLexer.ENDINTERFACE);
		// keywords.put("ENDMODULE", MFortranLexer.ENDMODULE);
		// keywords.put("ENDPROCEDURE", MFortranLexer.ENDPROCEDURE);
		// keywords.put("ENDPROGRAM", MFortranLexer.ENDPROGRAM);
		// keywords.put("ENDSELECT", MFortranLexer.ENDSELECT);
		// keywords.put("ENDSUBMODULE", MFortranLexer.ENDSUBMODULE);
		// keywords.put("ENDSUBROUTINE", MFortranLexer.ENDSUBROUTINE);
		// keywords.put("ENDTEAM", MFortranLexer.ENDTEAM);
		// keywords.put("ENDTYPE", MFortranLexer.ENDTYPE);
		// keywords.put("ENDWHERE", MFortranLexer.ENDWHERE);
		keywords.put("ENTRY", MFortranLexer.ENTRY);
		keywords.put("ENUM", MFortranLexer.ENUM);
		keywords.put("ENUMERATOR", MFortranLexer.ENUMERATOR);
		keywords.put("ERROR", MFortranLexer.ERROR);
		keywords.put("EQUIVALENCE", MFortranLexer.EQUIVALENCE);
		keywords.put("EVENT", MFortranLexer.EVENT);
		keywords.put("EXIT", MFortranLexer.EXIT);
		keywords.put("EXTENDS", MFortranLexer.EXTENDS);
		keywords.put("EXTERNAL", MFortranLexer.EXTERNAL);
		keywords.put("FILE", MFortranLexer.FILE);
		keywords.put("FINAL", MFortranLexer.FINAL);
		keywords.put("FLUSH", MFortranLexer.FLUSH);
		keywords.put("FORALL", MFortranLexer.FORALL);
		keywords.put("FORMAT", MFortranLexer.FORMAT);
		keywords.put("FORMATTED", MFortranLexer.FORMATTED);
		keywords.put("FUNCTION", MFortranLexer.FUNCTION);
		keywords.put("GENERIC", MFortranLexer.GENERIC);
		keywords.put("GO", MFortranLexer.GO);
		keywords.put("GOTO", MFortranLexer.GOTO);
		keywords.put("IF", MFortranLexer.IF);
		keywords.put("IMAGES", MFortranLexer.IMAGES);
		keywords.put("IMPLICIT", MFortranLexer.IMPLICIT);
		keywords.put("IMPORT", MFortranLexer.IMPORT);
		keywords.put("IN", MFortranLexer.IN);
		keywords.put("INCLUDE", MFortranLexer.INCLUDE);
		keywords.put("INOUT", MFortranLexer.INOUT);
		keywords.put("INTEGER", MFortranLexer.INTEGER);
		keywords.put("INTENT", MFortranLexer.INTENT);
		keywords.put("INTERFACE", MFortranLexer.INTERFACE);
		keywords.put("INTRINSIC", MFortranLexer.INTRINSIC);
		keywords.put("INQUIRE", MFortranLexer.INQUIRE);
		keywords.put("KIND", MFortranLexer.KIND);
		keywords.put("LEN", MFortranLexer.LEN);
		keywords.put("LOCK", MFortranLexer.LOCK);
		keywords.put("LOGICAL", MFortranLexer.LOGICAL);
		keywords.put("MEMORY", MFortranLexer.MEMORY);
		keywords.put("MODULE", MFortranLexer.MODULE);
		keywords.put("NAMELIST", MFortranLexer.NAMELIST);
		keywords.put("NONE", MFortranLexer.NONE);
		keywords.put("NON_INTRINSIC", MFortranLexer.NON_INTRINSIC);
		keywords.put("NON_OVERRIDABLE", MFortranLexer.NON_OVERRIDABLE);
		keywords.put("NOPASS", MFortranLexer.NOPASS);
		keywords.put("NULLIFY", MFortranLexer.NULLIFY);
		keywords.put("ONLY", MFortranLexer.ONLY);
		keywords.put("OPEN", MFortranLexer.OPEN);
		keywords.put("OPERATOR", MFortranLexer.OPERATOR);
		keywords.put("OPTIONAL", MFortranLexer.OPTIONAL);
		keywords.put("OUT", MFortranLexer.OUT);
		keywords.put("PARAMETER", MFortranLexer.PARAMETER);
		keywords.put("PASS", MFortranLexer.PASS);
		keywords.put("PAUSE", MFortranLexer.PAUSE);
		keywords.put("POINTER", MFortranLexer.POINTER);
		keywords.put("PRINT", MFortranLexer.PRINT);
		keywords.put("PRECISION", MFortranLexer.PRECISION);
		keywords.put("PRIVATE", MFortranLexer.PRIVATE);
		keywords.put("PROCEDURE", MFortranLexer.PROCEDURE);
		keywords.put("PROGRAM", MFortranLexer.PROGRAM);
		keywords.put("PROTECTED", MFortranLexer.PROTECTED);
		keywords.put("PUBLIC", MFortranLexer.PUBLIC);
		keywords.put("PURE", MFortranLexer.PURE);
		keywords.put("READ", MFortranLexer.READ);
		keywords.put("REAL", MFortranLexer.REAL);
		keywords.put("RECURSIVE", MFortranLexer.RECURSIVE);
		keywords.put("RESULT", MFortranLexer.RESULT);
		keywords.put("RETURN", MFortranLexer.RETURN);
		keywords.put("REWIND", MFortranLexer.REWIND);
		keywords.put("SAVE", MFortranLexer.SAVE);
		keywords.put("SELECT", MFortranLexer.SELECT);
		keywords.put("SELECTCASE", MFortranLexer.SELECTCASE);
		keywords.put("SELECTTYPE", MFortranLexer.SELECTTYPE);
		keywords.put("SEQUENCE", MFortranLexer.SEQUENCE);
		keywords.put("STOP", MFortranLexer.STOP);
		keywords.put("SUBMODULE", MFortranLexer.SUBMODULE);
		keywords.put("SUBROUTINE", MFortranLexer.SUBROUTINE);
		keywords.put("SYNC", MFortranLexer.SYNC);
		keywords.put("TARGET", MFortranLexer.TARGET);
		keywords.put("TEAM", MFortranLexer.TEAM);
		keywords.put("THEN", MFortranLexer.THEN);
		keywords.put("TO", MFortranLexer.TO);
		keywords.put("TYPE", MFortranLexer.TYPE);
		keywords.put("UNFORMATTED", MFortranLexer.UNFORMATTED);
		keywords.put("UNLOCK", MFortranLexer.UNLOCK);
		keywords.put("USE", MFortranLexer.USE);
		keywords.put("VALUE", MFortranLexer.VALUE);
		keywords.put("VOLATILE", MFortranLexer.VOLATILE);
		keywords.put("WAIT", MFortranLexer.WAIT);
		keywords.put("WHERE", MFortranLexer.WHERE);
		keywords.put("WHILE", MFortranLexer.WHILE);
		keywords.put("WRITE", MFortranLexer.WRITE);

	}

	// Converters:
	private TokenStream convertFixedForm(TokenStream stream)
			throws PP2CivlcTokenConversionException {
		CivlcToken cur = null, prv = null;

		this.isFixedForm = true;
		stream.toString(); // waiting for updating the stream
		cur = (CivlcToken) stream.get(0);
		while (cur != null && cur.getNext() != null) {
			switch (cur.getType()) {
				// Comments
				case PreprocessorParser.NOT :
					cur = matchCommentLineFixed(cur);
					break;
				// Identifiers and Keywords
				case PreprocessorParser.IDENTIFIER :
					String identText = cur.getText().toUpperCase();

					if (identText.startsWith("C")
							&& cur.getCharPositionInLine() == 0) // Comments
						cur = matchCommentLineFixed(cur);
					else if (identText.startsWith("END")
							&& identText.length() > 3) {
						if (endkeywords.contains(identText))
							cur = matchEndKeywords(cur);
						else
							matchIdentifier(cur);
					} else if (keywords.containsKey(identText)) // Keywords
						cur = matchKeywords(cur, identText);
					else if (identText.startsWith("$"))
						cur = matchCIVLPrimitives(cur);
					else // identifiers
						matchIdentifier(cur);
					break;
				// TODO: Symbols and Punctuators
				case PreprocessorParser.STAR :
					int pos = cur.getCharPositionInLine();

					if (pos == 0)
						// '*' on col 0 for a comment-line
						cur = matchCommentLineFixed(cur);
					else if (pos >= FIXED_FORM_STATEMENT_START) {
						if (cur.getNext().getType() != PreprocessorParser.STAR)
							matchNonIdentifiers(cur);
						else {
							// '**' for power
							cur.setText("**");
							cur.setStopIndex(cur.getStopIndex() + 1);
							cur.setType(MFortranLexer.POWER);
							cur.setTokenVocab(FORTRAN);
							cur.setNext(cur.getNext().getNext());
						}
					} else
						// '*' shall not appear in
						// col:1--5 in non-commentary
						// or non-coninuation line.
						assert (false);
					break;
				case PreprocessorParser.ASSIGN :
				case PreprocessorParser.CHARACTER_CONSTANT :
				case PreprocessorParser.COLON :
				case PreprocessorParser.COMMA :
				case PreprocessorParser.DIV :
				case PreprocessorParser.INTEGER_CONSTANT :
				case PreprocessorParser.LPAREN :
				case PreprocessorParser.LT :
				case PreprocessorParser.LTE :
				case PreprocessorParser.PLUS :
				case PreprocessorParser.RPAREN :
				case PreprocessorParser.STRING_LITERAL :
				case PreprocessorParser.SUB :
				case PreprocessorParser.WS :
				case PreprocessorParser.EQUALS :
				case PreprocessorParser.SEMI :
					matchNonIdentifiers(cur);
					break;
				case PreprocessorParser.DOT :
					if (cur.getNext().getNext()
							.getType() == PreprocessorParser.DOT) {
						cur = concatFortranRelOp(cur);
						prv.setNext(cur);
					} else if (cur.getNext().getNext()
							.getType() == PreprocessorParser.FLOATING_CONSTANT) {
						// e.g., '.', 'GT', '.2000'
						// reform to: '.GT.', '2000'
						splitPPDot(cur.getNext().getNext());
						cur = concatFortranRelOp(cur);
						prv.setNext(cur);
					} else
						assert (false);
					break;
				// Floating Constant
				case PreprocessorParser.FLOATING_CONSTANT :
				case PreprocessorParser.PP_NUMBER :
					cur = splitDotNumberConstant(cur);
					break;
				case PreprocessorParser.NEWLINE :
					cur = matchNewlineFixed(prv, cur);
					break;
				// Shared Keywords
				case PreprocessorParser.IF :
					cur.setType(MFortranLexer.IF);
					cur.setTokenVocab(FORTRAN);
					break;
				case PreprocessorParser.ELSE :
					cur.setType(MFortranLexer.ELSE);
					cur.setTokenVocab(FORTRAN);
					break;
				// Illegal tokens in Fortran
				case PreprocessorParser.IFDEF :
				case PreprocessorParser.IFNDEF :
					// All keywords only used in preprocessing
					// should be handled and not appear here.
					assert (false);
					break;
				// TODO: Unimplemented
				case PreprocessorParser.AMPERSAND :
				case PreprocessorParser.AND :
				case PreprocessorParser.ANNOTATION_END :
				case PreprocessorParser.ANNOTATION_START :
				case PreprocessorParser.ARROW :
				case PreprocessorParser.AT :
				case PreprocessorParser.BITANDEQ :
				case PreprocessorParser.BITOR :
				case PreprocessorParser.BITOREQ :
				case PreprocessorParser.BITXOR :
				case PreprocessorParser.BITXOREQ :
				case PreprocessorParser.BLOCK_COMMENT :
				case PreprocessorParser.BinaryExponentPart :
				case PreprocessorParser.CChar :
				case PreprocessorParser.COMMENT :
				case PreprocessorParser.DEFINE :
				case PreprocessorParser.DEFINED :
				case PreprocessorParser.DIVEQ :
				case PreprocessorParser.DOTDOT :
				case PreprocessorParser.DecimalConstant :
				case PreprocessorParser.DecimalFloatingConstant :
				case PreprocessorParser.Digit :
				case PreprocessorParser.ELIF :
				case PreprocessorParser.ELLIPSIS :
				case PreprocessorParser.ENDIF :
				case PreprocessorParser.EQUIV_ACSL :
				case PreprocessorParser.ERROR :
				case PreprocessorParser.EXTENDED_IDENTIFIER :
				case PreprocessorParser.EscapeSequence :
				case PreprocessorParser.ExponentPart :
				case PreprocessorParser.FloatingSuffix :
				case PreprocessorParser.FractionalConstant :
				case PreprocessorParser.GT :
				case PreprocessorParser.GTE :
				case PreprocessorParser.HASH :
				case PreprocessorParser.HASHHASH :
				case PreprocessorParser.HexEscape :
				case PreprocessorParser.HexFractionalConstant :
				case PreprocessorParser.HexPrefix :
				case PreprocessorParser.HexQuad :
				case PreprocessorParser.HexadecimalConstant :
				case PreprocessorParser.HexadecimalDigit :
				case PreprocessorParser.HexadecimalFloatingConstant :
				case PreprocessorParser.IMPLIES :
				case PreprocessorParser.IMPLIES_ACSL :
				case PreprocessorParser.INCLUDE :
				case PreprocessorParser.INLINE_ANNOTATION_START :
				case PreprocessorParser.INLINE_COMMENT :
				case PreprocessorParser.IdentifierNonDigit :
				case PreprocessorParser.IntegerSuffix :
				case PreprocessorParser.LCURLY :
				case PreprocessorParser.LEXCON :
				case PreprocessorParser.LINE :
				case PreprocessorParser.LSLIST :
				case PreprocessorParser.LSQUARE :
				case PreprocessorParser.LongLongSuffix :
				case PreprocessorParser.LongSuffix :
				case PreprocessorParser.MINUSMINUS :
				case PreprocessorParser.MOD :
				case PreprocessorParser.MODEQ :
				case PreprocessorParser.NEQ :
				case PreprocessorParser.NonDigit :
				case PreprocessorParser.NonZeroDigit :
				case PreprocessorParser.OR :
				case PreprocessorParser.OTHER :
				case PreprocessorParser.OctalConstant :
				case PreprocessorParser.OctalDigit :
				case PreprocessorParser.OctalEscape :
				case PreprocessorParser.PLUSEQ :
				case PreprocessorParser.PLUSPLUS :
				case PreprocessorParser.PRAGMA :
				case PreprocessorParser.QMARK :
				case PreprocessorParser.RCURLY :
				case PreprocessorParser.REXCON :
				case PreprocessorParser.RSLIST :
				case PreprocessorParser.RSQUARE :
				case PreprocessorParser.SChar :
				case PreprocessorParser.SHIFTLEFT :
				case PreprocessorParser.SHIFTLEFTEQ :
				case PreprocessorParser.SHIFTRIGHT :
				case PreprocessorParser.SHIFTRIGHTEQ :
				case PreprocessorParser.STAREQ :
				case PreprocessorParser.SUBEQ :
				case PreprocessorParser.TILDE :
				case PreprocessorParser.UNDEF :
				case PreprocessorParser.UniversalCharacterName :
				case PreprocessorParser.UnsignedSuffix :
				case PreprocessorParser.XOR_ACSL :
				case PreprocessorParser.Zero :
				default :
					System.err.println("Token: " + cur);
					assert (false);
					break;
			}

			// Update
			prv = cur;
			cur = prv.getNext();
		}

		TokenStream newTokenStream = new CommonTokenStream(
				new PP2CivlcTokenMFortranConverterTokenSource(
						(CivlcToken) stream.get(0)));

		newTokenStream.toString();
		return newTokenStream;
	}

	private TokenStream convertFreeForm(TokenStream stream)
			throws PP2CivlcTokenConversionException {
		CivlcToken cur = null, prv = null;

		this.isFixedForm = true;
		stream.toString(); // waiting for updating the stream
		cur = (CivlcToken) stream.get(0);
		while (cur != null && cur.getNext() != null) {
			switch (cur.getType()) {
				case PreprocessorParser.EOF :
					break;
				// Comments
				case PreprocessorParser.NOT :
					cur = matchCommentLineFree(cur);
					break;
				// Symbols
				case PreprocessorParser.COLON :
					if (cur.getNext().getType() != PreprocessorParser.COLON)
						matchNonIdentifiers(cur);
					else {
						// '::' for type decl delimiter
						cur.setText("::");
						cur.setStopIndex(cur.getStopIndex() + 1);
						cur.setType(MFortranLexer.COLON_COLON);
						cur.setTokenVocab(FORTRAN);
						cur.setNext(cur.getNext().getNext());
					}
					break;
				case PreprocessorParser.STAR :
					if (cur.getNext().getType() != PreprocessorParser.STAR)
						matchNonIdentifiers(cur);
					else {
						// '**' for power
						cur.setText("**");
						cur.setStopIndex(cur.getStopIndex() + 1);
						cur.setType(MFortranLexer.POWER);
						cur.setTokenVocab(FORTRAN);
						cur.setNext(cur.getNext().getNext());
					}
					break;
				case PreprocessorParser.ASSIGN :
				case PreprocessorParser.COMMA :
				case PreprocessorParser.DIV :
				case PreprocessorParser.DIVEQ :
				case PreprocessorParser.GT :
				case PreprocessorParser.IMPLIES :
				case PreprocessorParser.INTEGER_CONSTANT :
				case PreprocessorParser.LT :
				case PreprocessorParser.LTE :
				case PreprocessorParser.LPAREN :
				case PreprocessorParser.PLUS :
				case PreprocessorParser.RPAREN :
				case PreprocessorParser.STRING_LITERAL :
				case PreprocessorParser.CHARACTER_CONSTANT :
				case PreprocessorParser.SUB :
				case PreprocessorParser.WS :
				case PreprocessorParser.EQUALS :
				case PreprocessorParser.SEMI :
				case PreprocessorParser.MOD :
					matchNonIdentifiers(cur);
					break;
				case PreprocessorParser.AMPERSAND :
					// line continuation
					cur = processLineContinuationM(prv, cur);
					break;
				case PreprocessorParser.DOT :
					if (cur.getNext().getNext()
							.getType() == PreprocessorParser.DOT) {
						cur = concatFortranRelOp(cur);
						prv.setNext(cur);
					} else
						assert (false);
					break;
				// Constants
				// Floating Constant
				case PreprocessorParser.FLOATING_CONSTANT :
				case PreprocessorParser.PP_NUMBER :
					cur = splitDotNumberConstant(cur);
					break;
				case PreprocessorParser.NEWLINE :
					cur = matchNewlineFree(prv, cur);
					break;
				// Identifiers
				case PreprocessorParser.IDENTIFIER :
					String identText = cur.getText().toUpperCase();

					if (endkeywords.contains(identText)) // EndKeyw
						cur = matchEndKeywords(cur);
					else if (identText.startsWith("$"))
						cur = matchCIVLPrimitives(cur);
					else if (keywords.containsKey(identText)) // Keyw
						cur = matchKeywords(cur, identText);
					else // Ident
						matchIdentifier(cur);
					break;
				// Shared Keywords
				case PreprocessorParser.IF :
					cur.setType(MFortranLexer.IF);
					cur.setTokenVocab(FORTRAN);
					break;
				case PreprocessorParser.ELSE :
					cur.setType(MFortranLexer.ELSE);
					cur.setTokenVocab(FORTRAN);
					break;
				// Illegal tokens in Fortran
				case PreprocessorParser.IFDEF :
				case PreprocessorParser.IFNDEF :
					// All keywords only used in preprocessing
					// should be handled and not appear here.
					assert (false);
					break;
				// TODO: Unimplemented
				case PreprocessorParser.AND :
				case PreprocessorParser.ANNOTATION_END :
				case PreprocessorParser.ANNOTATION_START :
				case PreprocessorParser.ARROW :
				case PreprocessorParser.AT :
				case PreprocessorParser.BITANDEQ :
				case PreprocessorParser.BITOR :
				case PreprocessorParser.BITOREQ :
				case PreprocessorParser.BITXOR :
				case PreprocessorParser.BITXOREQ :
				case PreprocessorParser.BLOCK_COMMENT :
				case PreprocessorParser.BinaryExponentPart :
				case PreprocessorParser.CChar :
				case PreprocessorParser.COMMENT :
				case PreprocessorParser.DEFINE :
				case PreprocessorParser.DEFINED :
				case PreprocessorParser.DOTDOT :
				case PreprocessorParser.DecimalConstant :
				case PreprocessorParser.DecimalFloatingConstant :
				case PreprocessorParser.Digit :
				case PreprocessorParser.ELIF :
				case PreprocessorParser.ELLIPSIS :
				case PreprocessorParser.ENDIF :
				case PreprocessorParser.EQUIV_ACSL :
				case PreprocessorParser.ERROR :
				case PreprocessorParser.EXTENDED_IDENTIFIER :
				case PreprocessorParser.EscapeSequence :
				case PreprocessorParser.ExponentPart :
				case PreprocessorParser.FloatingSuffix :
				case PreprocessorParser.FractionalConstant :
				case PreprocessorParser.GTE :
				case PreprocessorParser.HASH :
				case PreprocessorParser.HASHHASH :
				case PreprocessorParser.HexEscape :
				case PreprocessorParser.HexFractionalConstant :
				case PreprocessorParser.HexPrefix :
				case PreprocessorParser.HexQuad :
				case PreprocessorParser.HexadecimalConstant :
				case PreprocessorParser.HexadecimalDigit :
				case PreprocessorParser.HexadecimalFloatingConstant :
				case PreprocessorParser.IMPLIES_ACSL :
				case PreprocessorParser.INCLUDE :
				case PreprocessorParser.INLINE_ANNOTATION_START :
				case PreprocessorParser.INLINE_COMMENT :
				case PreprocessorParser.IdentifierNonDigit :
				case PreprocessorParser.IntegerSuffix :
				case PreprocessorParser.LCURLY :
				case PreprocessorParser.LEXCON :
				case PreprocessorParser.LINE :
				case PreprocessorParser.LSLIST :
				case PreprocessorParser.LSQUARE :
				case PreprocessorParser.LongLongSuffix :
				case PreprocessorParser.LongSuffix :
				case PreprocessorParser.MINUSMINUS :
				case PreprocessorParser.MODEQ :
				case PreprocessorParser.NEQ :
				case PreprocessorParser.NonDigit :
				case PreprocessorParser.NonZeroDigit :
				case PreprocessorParser.OR :
				case PreprocessorParser.OTHER :
				case PreprocessorParser.OctalConstant :
				case PreprocessorParser.OctalDigit :
				case PreprocessorParser.OctalEscape :
				case PreprocessorParser.PLUSEQ :
				case PreprocessorParser.PLUSPLUS :
				case PreprocessorParser.PRAGMA :
				case PreprocessorParser.QMARK :
				case PreprocessorParser.RCURLY :
				case PreprocessorParser.REXCON :
				case PreprocessorParser.RSLIST :
				case PreprocessorParser.RSQUARE :
				case PreprocessorParser.SChar :
				case PreprocessorParser.SHIFTLEFT :
				case PreprocessorParser.SHIFTLEFTEQ :
				case PreprocessorParser.SHIFTRIGHT :
				case PreprocessorParser.SHIFTRIGHTEQ :
				case PreprocessorParser.STAREQ :
				case PreprocessorParser.SUBEQ :
				case PreprocessorParser.TILDE :
				case PreprocessorParser.UNDEF :
				case PreprocessorParser.UniversalCharacterName :
				case PreprocessorParser.UnsignedSuffix :
				case PreprocessorParser.XOR_ACSL :
				case PreprocessorParser.Zero :
				default :
					System.out
							.println("" + cur.getType() + ":" + cur.getText());
					assert false;
					break;
			}
			// Update
			prv = cur;
			cur = prv.getNext();
		}

		TokenStream newTokenStream = new CommonTokenStream(
				new PP2CivlcTokenMFortranConverterTokenSource(
						(CivlcToken) stream.get(0)));

		newTokenStream.toString();
		return newTokenStream;
	}

	private CivlcToken processLineContinuationM(CivlcToken prv,
			CivlcToken cur) {
		CivlcToken nxt = cur.getNext();

		while (nxt != null && nxt.getType() != PreprocessorParser.NEWLINE) {
			cur = nxt;
			nxt = cur.getNext();
		} // Omit all tokens from '&' to the first '\n' encountered inclusively.
		assert nxt != null && nxt.getNext() != null;
		prv.setNext(nxt.getNext());
		return prv;
	}

	// Helpers

	private CivlcToken matchCIVLPrimitives(CivlcToken cur) {
		cur.setType(MFortranLexer.CIVL_PRIMITIVE);
		cur.setTokenVocab(FORTRAN);
		return cur;
	}

	private CivlcToken matchCommentLineFixed(CivlcToken cur) {
		CivlcToken tmp = cur;
		int commentStart = tmp.getCharPositionInLine();
		String text = "";

		if (cur.getNext().getText().contains("$")) {
			// '!$'/'c$'/'*$' for pragma
			return splitPragma(cur);
		}

		while (tmp.getType() != PreprocessorParser.NEWLINE) {
			text += tmp.getText();
			tmp = tmp.getNext();
		}
		// Now tmp is '\n'
		if (commentStart < FIXED_FORM_STATEMENT_START)
			tmp.setChannel(ANTLR_IGNORED_CHANNEL);
		else
			tmp.setChannel(0);
		tmp.setType(MFortranLexer.EOS);
		tmp.setTokenVocab(FORTRAN);
		cur.setStopIndex(tmp.getStartIndex() - 1);
		cur.setText(text);
		cur.setNext(tmp);
		cur.setType(MFortranLexer.LINE_COMMENT);
		cur.setChannel(ANTLR_IGNORED_CHANNEL);
		cur.setTokenVocab(FORTRAN);
		return tmp;
	}

	private CivlcToken matchCommentLineFree(CivlcToken cur) {
		CivlcToken tmp = cur;
		String text = "";

		if (cur.getNext().getText().contains("$")) {
			// '!$'/'c$'/'*$' for pragma
			return splitPragma(cur);
		}
		while (tmp.getType() != PreprocessorParser.NEWLINE) {
			text += tmp.getText();
			tmp = tmp.getNext();
		}
		// Now tmp is '\n'
		tmp.setChannel(0);
		tmp.setType(MFortranLexer.EOS);
		tmp.setTokenVocab(FORTRAN);
		cur.setStopIndex(tmp.getStartIndex() - 1);
		cur.setText(text);
		cur.setNext(tmp);
		cur.setType(MFortranLexer.LINE_COMMENT);
		cur.setChannel(ANTLR_IGNORED_CHANNEL);
		cur.setTokenVocab(FORTRAN);
		return tmp;
	}

	private CivlcToken matchEndKeywords(CivlcToken cur) {
		String end = "END";
		String keywordStr = cur.getText().substring(end.length());
		CivlcToken next = cur.getNext();
		CivlcToken keyword = tf.newCivlcToken(cur.getInputStream(),
				keywords.get(keywordStr.toUpperCase()), cur.getChannel(),
				cur.getStartIndex() + end.length(), cur.getStopIndex(),
				cur.getFormation(), cur.getLine(),
				cur.getCharPositionInLine() + end.length(), FORTRAN);
		cur.setText(end);
		cur.setType(keywords.get(end));
		cur.setNext(keyword);
		keyword.setText(keywordStr);
		keyword.setNext(next);
		return keyword;
	}

	private CivlcToken concatFortranRelOp(CivlcToken cur) {
		CivlcToken relOp = cur.getNext();
		CivlcToken nxt = relOp.getNext().getNext();
		String relOpText = relOp.getText();
		int relOpType = relOps.get(relOpText.toUpperCase());
		int relOpInLine = cur.getCharPositionInLine();
		CivlcToken fRelOpToken = tf.newCivlcToken(cur.getInputStream(),
				relOpType, cur.getChannel(), relOpInLine,
				relOpInLine + relOpText.length() + 1, cur.getFormation(),
				cur.getLine(), relOpInLine, FORTRAN);

		fRelOpToken.setText("." + relOpText + ".");
		fRelOpToken.setNext(nxt);
		return fRelOpToken;
	}

	private void matchIdentifier(CivlcToken cur) {
		cur.setType(MFortranLexer.IDENT);
		cur.setText(cur.getText().toUpperCase());
		cur.setTokenVocab(FORTRAN);
	}

	private CivlcToken matchNewlineFixed(CivlcToken prv, CivlcToken cur)
			throws PP2CivlcTokenConversionException {
		// If the col:5 is NOT ' ' or '0' in the next non-commentary line
		// Then the cur should be changed from EOS to WS.
		// and the char on col:6 should be changed as ' '.
		// Note that a token with text length greater than 1
		// should split to ensure the char on col:5 is a single token.
		boolean isComment = false; // Assume col:5 is not in a comment
		CivlcToken tmpPrv = cur;
		CivlcToken tmp = tmpPrv.getNext();
		CivlcToken commentTail = null;

		while (tmp != null) {
			if (tmp.getType() == PreprocessorParser.NEWLINE) {
				// Skip any NEWLINE token
				// Assume the next line is not commentary
				isComment = false;
				commentTail = tmp;
			} else if (!isComment) {
				String tmpText = tmp.getText();
				int tmpStart = tmp.getCharPositionInLine();
				int tmpLen = tmpText.length();
				int tmpStop = tmpStart + tmpLen - 1;
				int tmpToCol4 = 5 - tmpStart;

				// Check whether col:5 is in a comment.
				if (tmpStart == 0) {
					String col0 = tmpText.substring(0, 1).toUpperCase();

					// 'C'/'c'/'*'/'!' in col:0
					if (col0.equals("C") || col0.equals("*")
							|| col0.equals("!")) {
						isComment = true;
						continue;
					}
				} else if (tmpStart < 5) {
					int lenToCol4 = tmpLen < tmpToCol4 ? tmpLen : tmpToCol4;
					String colTo4 = tmpText.substring(0, lenToCol4)
							.toUpperCase();

					// At least 1 '!' in col:1--4
					if (colTo4.contains("!")) {
						isComment = true;
						continue;
					}
				}
				if (tmpStart <= 5 && 5 <= tmpStop) {
					// tmp contains col:5 and is not in a comment.
					if (tmpLen > 1) {
						// Split 'tmp' to get the single char on col:5
						// The C preprocessor may incorrectly output:
						// a PPNumber token for 3 cases:
						// C1: Label(i..4)-'0'(5)-Chars/Digits(6..j)
						// C2: '0'(5)-Chars/Digits(6..j)
						// C3: NonZeroDigit(5)-Chars/Digits(6..j)
						// or an Identifier token for 1 cases:
						// C4: NonZeroNonWSChar(5)-Chars/Digits(6..j)

						if (tmpStart < 5) {
							// Contents directly before col:5 is an int-constant
							String tmpPrefix = tmpText.substring(0, tmpToCol4);
							CivlcToken labelToken = tf.newCivlcToken(
									tmp.getInputStream(),
									PreprocessorParser.INTEGER_CONSTANT, 0,
									tmpStart, 4, tmp.getFormation(),
									tmp.getLine(), tmpStart, PREPROC);

							labelToken.setText(tmpPrefix);
							labelToken.setNext(tmp);
							prv.setNext(labelToken);
						}
						if (tmpStop > 5)
							// Contents directly before col:5 is not supported
							// Its type is determined by the code context.
							throw new PP2CivlcTokenConversionException(
									"Fortran converter can not split and lex this token",
									tmp);
					}

					// Set 'tmp' as a WS token with a single char on col:5
					String col5 = tmpText.substring(tmpToCol4, tmpToCol4 + 1);

					tmp.setChannel(ANTLR_IGNORED_CHANNEL);
					tmp.setCharPositionInLine(5);
					tmp.setText(col5);
					tmp.setType(PreprocessorParser.WS);
					if (!col5.equals(" ") && !col5.equals("0")) {
						CivlcToken continueStart = tmp.getNext();
						CivlcToken continueStop = null;
						CivlcToken continueEnd = continueStart;

						while (continueEnd != null && continueEnd
								.getType() != PreprocessorParser.NEWLINE) {
							continueStop = continueEnd;
							continueEnd = continueEnd.getNext();
						}
						// Insert continued line in to current line.
						prv.setNext(continueStart); // After prv
						continueStop.setNext(cur); // Before cur
						// Concatenate NEWLINE in the continued line.
						if (commentTail != null)
							commentTail.setNext(continueEnd);
						else
							cur.setNext(continueEnd);
						// Remove the header of the continued line.
						tmp.setNext(null);
						return prv;
					} else
						// a new non-commentary line
						break;
				}
			}
			// else skip tokens in comment except NEWLINE
			tmpPrv = tmp;
			tmp = tmpPrv.getNext();
		}

		// Fortran 2018 6.3.3.2 -- 6.3.3.3
		if (prv == null || prv.getType() != MFortranLexer.EOS)
			// An empty line will be ignored.
			cur.setChannel(0);
		cur.setType(MFortranLexer.EOS);
		cur.setTokenVocab(FORTRAN);
		return cur;
	}

	private CivlcToken matchNewlineFree(CivlcToken prv, CivlcToken cur)
			throws PP2CivlcTokenConversionException {
		// Fortran 2018 6.3.3.2 -- 6.3.3.3
		if (prv == null || prv.getType() != MFortranLexer.EOS)
			// An empty line will be ignored.
			cur.setChannel(0);
		cur.setType(MFortranLexer.EOS);
		cur.setTokenVocab(FORTRAN);
		return cur;
	}

	private CivlcToken matchKeywords(CivlcToken cur, String keyword) {
		cur.setType(keywords.get(keyword));
		cur.setTokenVocab(FORTRAN);
		return postKeywordProcessing(cur);
	}

	private void matchNonIdentifiers(CivlcToken cur) {
		cur.setType(symbols.get(cur.getType()));
		cur.setTokenVocab(FORTRAN);
	}

	private CivlcToken postKeywordProcessing(CivlcToken cur) {
		int fType = cur.getType();

		switch (fType) {
			case MFortranLexer.INCLUDE :
				CivlcToken includeFileToken = null;
				CivlcToken includeEndToken = null;
				CivlcToken inclusionBlockToken = null;
				CivlcToken inclusionEndToken = null;

				// Initially 'cur' is 'INCLUDE'
				cur = cur.getNext();
				while (cur != null
						&& cur.getType() != PreprocessorParser.STRING_LITERAL) {
					// Only WS between 'INCLUDE' and 'STRING_LITERAL' (filename)
					cur.setType(MFortranLexer.WS);
					cur.setChannel(ANTLR_IGNORED_CHANNEL);
					cur.setTokenVocab(FORTRAN);
					cur = cur.getNext();
				}
				// Now, 'cur' is 'STRING_LITERAL' (filename)
				includeFileToken = cur;
				cur.setType(MFortranLexer.CHAR_CONST);
				cur.setChannel(ANTLR_IGNORED_CHANNEL);
				cur.setTokenVocab(FORTRAN);
				// Generate the pre-processed token stream for
				// the included file based on 'STRING_LITERAL' (filename)
				inclusionBlockToken = processInclude(includeFileToken);
				cur = cur.getNext();
				// Then, find the NEWLINE in the current line
				while (cur != null
						&& cur.getType() != PreprocessorParser.NEWLINE) {
					// Only WS between 'STRING_LITERAL' (filename) and NEWLINE
					cur.setType(MFortranLexer.WS);
					cur.setChannel(ANTLR_IGNORED_CHANNEL);
					cur.setTokenVocab(FORTRAN);
					includeEndToken = cur;
					cur = cur.getNext();
				}
				// Now, 'inclusionEndToken' is the last WS before NEWLINE
				// Omit this NEWLINE ('cur')
				includeEndToken.setNext(inclusionBlockToken);
				// Find the 'EOF' token in the token stream of the included file
				inclusionEndToken = inclusionBlockToken;
				while (inclusionEndToken != null
						&& inclusionEndToken.getType() != MFortranLexer.EOF)
					inclusionEndToken = inclusionEndToken.getNext();
				// Concatenate the next line of the 'INCLUDE' line
				// after the EOF token in the stream for the included file
				inclusionEndToken
						.setText("EOF" + "=" + inclusionBlockToken.getText());
				inclusionEndToken.setType(MFortranLexer.M_EOF);
				inclusionEndToken.setTokenVocab(FORTRAN);
				inclusionEndToken.setNext(cur.getNext());
				// Return the start of the token stream for included file
				return inclusionEndToken;
			default :
		}
		return cur;
	}

	private CivlcToken processInclude(CivlcToken cur) {
		CivlcToken inclusionHeaderToken = cur;

		try {
			String includedLiteral = cur.getText();
			String includedFileName = includedLiteral.substring(1,
					includedLiteral.length() - 1);
			String curAbsPath = cur.getSourceFile().getFile().getAbsolutePath();
			String curPath = curAbsPath.substring(0,
					curAbsPath.lastIndexOf('/') + 1);
			File[] sysPaths = ABC.DEFAULT_SYSTEM_INCLUDE_PATHS;
			File[] usrPaths = ABC.DEFAULT_USER_INCLUDE_PATHS;
			File includeFile = new File(curPath + "/" + includedFileName);
			int index = 0;

			while (!includeFile.exists() && index < sysPaths.length)
				includeFile = new File(sysPaths[index++].getAbsolutePath() + "/"
						+ includedFileName);
			index = 0;
			while (!includeFile.exists() && index < usrPaths.length)
				includeFile = new File(usrPaths[index++].getAbsolutePath() + "/"
						+ includedFileName);
			if (!includeFile.exists())
				throw new ABCRuntimeException(
						"Included file not found: " + includedFileName);

			Preprocessor cPreprocessor = new CPreprocessor(
					Configurations.newMinimalConfiguration(), Language.C,
					tf.newFileIndexer(), tf);
			TokenStream inclusionTokenStream = new CommonTokenStream(
					cPreprocessor.preprocess(sysPaths, usrPaths,
							ABC.DEFAULT_IMPLICIT_MACROS,
							new File[]{includeFile}));
			CivlcToken tmp = null;

			// Pre-process the included part based on
			// the form of its parent source file
			if (isFixedForm)
				inclusionTokenStream = convertFixedForm(inclusionTokenStream);
			else
				inclusionTokenStream = convertFreeForm(inclusionTokenStream);
			//
			tmp = (CivlcToken) inclusionTokenStream.get(0);
			inclusionHeaderToken = tf.newCivlcToken(tmp.getInputStream(),
					MFortranLexer.M_INCLUDE_NAME, 0, tmp.getStartIndex(),
					tmp.getStartIndex(), tmp.getFormation(), 0, 0, FORTRAN);
			inclusionHeaderToken.setText("=" + includeFile.getAbsolutePath());
			inclusionHeaderToken.setNext(tmp);
		} catch (PreprocessorException | PP2CivlcTokenConversionException e) {
			e.printStackTrace();
		}
		return inclusionHeaderToken;
	}

	private CivlcToken splitPPDot(CivlcToken cur) {
		CivlcToken fracToken = null;
		CivlcToken tmp = cur.getNext();
		String realText = cur.getText();
		String dotText = ".";
		String fracText = realText.substring(1).toUpperCase();

		assert '.' == realText.charAt(0);
		assert Pattern.matches(PATTERN_INTEGER, fracText);

		fracToken = tf.newCivlcToken(cur.getInputStream(),
				PreprocessorParser.INTEGER_CONSTANT, cur.getChannel(),
				cur.getStartIndex() + 1, cur.getStopIndex(), cur.getFormation(),
				cur.getLine(), cur.getCharPositionInLine() + 1, PREPROC);
		fracToken.setText(fracText);
		fracToken.setNext(tmp);
		cur.setText(dotText);
		cur.setStopIndex(cur.getStartIndex() + 1);
		cur.setType(PreprocessorParser.DOT);
		cur.setNext(fracToken);
		return fracToken;
	}

	private CivlcToken splitDotNumberConstant(CivlcToken cur) {
		CivlcToken fracToken = null;
		CivlcToken tmp = cur.getNext();
		String realText = cur.getText();
		String suffix = realText.substring(realText.length() - 1).toUpperCase();
		int intLen = realText.indexOf('.');
		int fracStart = cur.getStartIndex();

		if (intLen < 0) {
			String fracText = "";
			if (Pattern.matches(PATTERN_LBL_LOOPVAR, realText)) {
				// Number concatenated with Letters
				// e.g., DO 100I=1,N
				// DO end lbl. concatenated with loop var. I
				fracText = Pattern.compile(PATTERN_INTEGER).split(realText,
						2)[1];
				intLen = realText.length() - fracText.length();
				fracStart += intLen;
			}
			fracToken = tf.newCivlcToken(cur.getInputStream(),
					MFortranLexer.IDENT, cur.getChannel(), fracStart,
					tmp.getStartIndex() - 1, cur.getFormation(), cur.getLine(),
					cur.getCharPositionInLine() + intLen, FORTRAN);
			fracToken.setText(fracText);
		} else {
			// DotNumber
			// e.g., 100.1D
			fracStart += intLen;
			if (suffix.equals("E") || suffix.equals("D")) {
				if (tmp != null && (tmp.getType() == PreprocessorParser.PLUS
						|| tmp.getType() == PreprocessorParser.SUB))
					// Add a sign: '+' or '-'
					realText += tmp.getText();
				tmp = tmp.getNext();
				// Concatenate following INTEGER_CONSTANT
				// which may be separated by WS.
				while (tmp != null) {
					int tmpType = tmp.getType();

					if (tmpType == PreprocessorParser.INTEGER_CONSTANT)
						realText += tmp.getText();
					else if (tmpType != PreprocessorParser.WS)
						// Omit WS
						break;
					tmp = tmp.getNext();
				}
			}
			fracToken = tf.newCivlcToken(cur.getInputStream(),
					MFortranLexer.PERIOD_EXPONENT, cur.getChannel(), fracStart,
					tmp.getStartIndex() - 1, cur.getFormation(), cur.getLine(),
					cur.getCharPositionInLine() + intLen, FORTRAN);
			fracToken.setText(realText.substring(intLen));
		}
		fracToken.setNext(tmp);
		cur.setText(realText.substring(0, intLen));
		cur.setStopIndex(fracStart - 1);
		cur.setType(MFortranLexer.DIGIT_STR);
		cur.setNext(fracToken);
		cur.setTokenVocab(FORTRAN);
		return fracToken;
	}

	private CivlcToken splitPragma(CivlcToken cur) {
		// Starts with '!'
		// dollarSign is '$PRAGMA_ID' (e.g., $OMP, $CVL)
		CivlcToken dollarSignAndId = cur.getNext();

		cur.setText("!$");
		cur.setStopIndex(cur.getStopIndex() + 1);
		cur.setType(MFortranLexer.PRAGMA);
		dollarSignAndId.setText(dollarSignAndId.getText().substring(1));
		dollarSignAndId.setStartIndex(dollarSignAndId.getStartIndex() + 1);
		return cur;
	}

}
