package edu.udel.cis.vsl.abc.front.fortran.parse;

import java.util.HashMap;

import org.antlr.runtime.Parser;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;

import edu.udel.cis.vsl.abc.ast.IF.AST;

public class BaseMFortranParser extends Parser {
	public static enum ACITON_TYPE {
		NULL, F2018
	}

	protected FortranParserActionNew MFPA;

	protected ACITON_TYPE actionType = ACITON_TYPE.NULL;
	protected String fileName;
	protected String pathName;
	protected boolean hasError;

	@SuppressWarnings("unchecked")
	public BaseMFortranParser(TokenStream input, RecognizerSharedState rState) {
		super(input, rState);
		super.state.ruleMemo = new HashMap[500];
		actionType = ACITON_TYPE.F2018;
		MFPA = new FortranParserActionNew();
		fileName = input.getSourceName();
		pathName = input.getSourceName();
		hasError = false;
	}

	public boolean hasError() {
		return hasError;
	}

	public void reportError(RecognitionException re) {
		super.reportError(re);
		MFPA = null;
		actionType = ACITON_TYPE.NULL;
		hasError = true;
	}

	public FortranParserActionNew getAction() {
		return MFPA;
	}

	/**
	 * Check for include and end of file. INCLUDE is not in the grammar so this
	 * method must be called after every statement (and initially at the
	 * beginning of program unit file).
	 */
	public void checkForInclude() {
		// consume bare EOS
		while (input.LA(1) == MFortranParser2018.EOS) {
			input.consume();
		}
		if (input.LA(1) == MFortranParser2018.INCLUDE) {
			String files[];

			// consume INCLUDE
			input.consume();
			// get include filename from token stream
			files = input.LT(1).getText().split("=");
			MFPA.inclusion(files[1], fileName);
			MFPA.start_of_file(files[0], files[1]);
			// consume INCLUDE_NAME
			input.consume();
			// check for empty include file (no statements)
			if (input.LA(1) == MFortranParser2018.M_EOF) {
				Token tk = input.LT(1);

				input.consume();
				files = tk.getText().split(":");
				MFPA.end_of_file(files[0], files[1]);
			}
			// include acts like a statement so need to see if another include
			// follows
			checkForInclude();
		} else if (input.LA(1) == MFortranParser2018.M_EOF) {
			Token tk = input.LT(1);
			String[] files = tk.getText().split("=");

			input.consume();
			MFPA.end_of_file(files[0], files[1]);
			// unwind M_EOFs for include files containing includes
			checkForInclude();
		}
		// OFP: Done by the derived class
		// else if (input.LA(1) == FortranLexer.EOF) {
		// Token tk = input.LT(1);
		// action.end_of_file(tk.getText());
		// input.consume();
		// }
	}

	public AST getAST() {
		return MFPA.getAST();
	}
}
