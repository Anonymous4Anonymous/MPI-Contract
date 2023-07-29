package edu.udel.cis.vsl.abc.front.fortran.parse;

import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;

import edu.udel.cis.vsl.abc.front.common.parse.OmpPragmaParser;
import edu.udel.cis.vsl.abc.front.fortran.ptree.MFTree;
import edu.udel.cis.vsl.abc.token.IF.Source;
import edu.udel.cis.vsl.abc.token.IF.SyntaxException;

public class MFOmpParser implements OmpPragmaParser {
	public static final int AMPERSAND = MFortranOmpParser.T_AMPERSAND;
	public static final int ATOMIC = MFortranOmpParser.T_OMPATOMIC;
	public static final int BARRIER = MFortranOmpParser.T_BARRIER;
	public static final int BITOR = MFortranOmpParser.T_BITOR;
	public static final int BITXOR = MFortranOmpParser.T_BITXOR;
	public static final int CAPTURE = MFortranOmpParser.T_CAPTURE;
	public static final int COLLAPSE = MFortranOmpParser.T_COLLAPSE;
	public static final int COPYIN = MFortranOmpParser.T_COPYIN;
	public static final int COPYPRIVATE = MFortranOmpParser.T_COPYPRIVATE;
	public static final int CRITICAL = MFortranOmpParser.T_CRITICAL;
	public static final int DATA_CLAUSE = MFortranOmpParser.T_DATA_CLAUSE;
	public static final int DEFAULT = MFortranOmpParser.T_DEFAULT;
	public static final int DYNAMIC = MFortranOmpParser.T_DYNAMIC;
	public static final int EQ = MFortranOmpParser.EQ;
	public static final int EQV = MFortranOmpParser.EQV;
	public static final int FLUSH = MFortranOmpParser.T_FLUSH;
	public static final int FOR = MFortranOmpParser.T_DO;
	public static final int FST_PRIVATE = MFortranOmpParser.T_FST_PRIVATE;
	public static final int GUIDED = MFortranOmpParser.T_GUIDED;
	public static final int IDENTIFIER = MFortranOmpParser.T_IDENT;
	public static final int IF = MFortranOmpParser.T_IF;
	public static final int LAND = MFortranOmpParser.T_AND;
	public static final int LOR = MFortranOmpParser.T_OR;
	public static final int LST_PRIVATE = MFortranOmpParser.T_LST_PRIVATE;
	public static final int MASTER = MFortranOmpParser.T_MASTER;
	public static final int NONE = MFortranOmpParser.T_NONE;
	public static final int NE = MFortranOmpParser.NE;
	public static final int NEQV = MFortranOmpParser.NEQV;
	public static final int NOWAIT = MFortranOmpParser.T_NOWAIT;
	public static final int NUM_THREADS = MFortranOmpParser.T_NUM_THREADS;
	public static final int ORDERED = MFortranOmpParser.T_ORDERED;
	public static final int PARALLEL = MFortranOmpParser.T_PARALLEL;
	public static final int PARALLEL_FOR = MFortranOmpParser.T_PARALLEL_FOR;
	public static final int PARALLEL_SECTIONS = MFortranOmpParser.T_PARALLEL_SECTIONS;
	public static final int PLUS = MFortranOmpParser.T_PLUS;
	public static final int PRIVATE = MFortranOmpParser.T_PRIVATE;
	public static final int READ = MFortranOmpParser.T_READ;
	public static final int REDUCTION = MFortranOmpParser.T_REDUCTION;
	public static final int RUNTIME = MFortranOmpParser.T_RUNTIME;
	public static final int SCHEDULE = MFortranOmpParser.T_SCHEDULE;
	public static final int SECTION = MFortranOmpParser.T_SECTION;
	public static final int SECTIONS = MFortranOmpParser.T_SECTIONS;
	public static final int SEQ_CST = MFortranOmpParser.T_SEQ_CST;
	public static final int SHARED = MFortranOmpParser.T_SHARED;
	public static final int SINGLE = MFortranOmpParser.T_SINGLE;
	public static final int STAR = MFortranOmpParser.T_ASTERISK;
	public static final int STATIC = MFortranOmpParser.T_STATIC;
	public static final int SUB = MFortranOmpParser.T_MINUS;
	public static final int THD_PRIVATE = MFortranOmpParser.T_THD_PRIVATE;
	public static final int UNIQUE_FOR = MFortranOmpParser.T_UNIQUE_FOR;
	public static final int UNIQUE_PARALLEL = MFortranOmpParser.T_UNIQUE_PARALLEL;
	public static final int UPDATE = MFortranOmpParser.T_UPDATE;
	public static final int WRITE = MFortranOmpParser.T_WRITE;
	public static final int END = MFortranOmpParser.T_END;

	@Override
	public CommonTree parse(Source source, TokenStream tokens)
			throws SyntaxException {
		MFortranOmpParser parser = new MFortranOmpParser(tokens);

		try {
			return (CommonTree) parser.openmp_construct().getTree();
		} catch (RecognitionException e) {
			throw new SyntaxException(e.getMessage(), null);
		}
	}

	// TODO: Transformation from CommonTree to MFTree for
	// parsing the involved expression.
	public MFTree parseFortran(Source source, TokenStream tokens)
			throws SyntaxException {
		MFortranOmpParser parser = new MFortranOmpParser(tokens);

		try {
			CommonTree tempTree = (CommonTree) parser.openmp_construct()
					.getTree();

			return commonTree2MFTree(tempTree);
		} catch (RecognitionException e) {
			throw new SyntaxException(e.getMessage(), null);
		}
	}

	private MFTree commonTree2MFTree(CommonTree tree) {
		// TODO Auto-generated method stub
		return null;
	}

}
