package edu.udel.cis.vsl.abc.front.fortran.preproc;

import java.util.ArrayList;

import org.antlr.runtime.BufferedTokenStream;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenSource;

import edu.udel.cis.vsl.abc.token.IF.CivlcToken;
import edu.udel.cis.vsl.abc.token.IF.CivlcToken.TokenVocabulary;
import edu.udel.cis.vsl.abc.token.IF.Formation;
import edu.udel.cis.vsl.abc.token.IF.TokenFactory;
import edu.udel.cis.vsl.abc.token.IF.Tokens;

public class MFortranTokenStream extends BufferedTokenStream {
	private final int ANTLR_LEX_IGNORED_CHANNEL = 99;
	private int curLineLen = 0;
	private TokenFactory tf = Tokens.newTokenFactory();
	private ArrayList<Token> rawLine = null;
	private ArrayList<Token> curLine = null;
	private ArrayList<Token> finalTokens = new ArrayList<Token>();

	MFortranTokenStream(CivlcToken token) {
		super(new MFortranTokenSource(token));
		super.setup();
		super.fill();
	}

	@Override
	public Token LT(int k) {
		if (index() + k - 1 >= this.size()) {
			assert (false);
			return null; // TODO: eofToken;
		}
		return super.LT(k);
	}

	void add(int index, Token token) {
		curLine.add(index, token);
	}

	void addToken(int index, int tokenType, String tokenText,
			Formation formation) {
		curLine.add(index, tf.newCivlcToken(tokenType, tokenText, formation,
				TokenVocabulary.FORTRAN));
	}

	void addToken(Token token) {
		curLine.add(token);
	}

	void addTokenToNewList(Token token) {
		if (!finalTokens.add(token))
			System.err.println("Couldn't add to finalTokens!");
	}

	boolean appendToken(int tokenType, String tokenText, Formation formation) {
		return curLine.add(tf.newCivlcToken(tokenType, tokenText, formation,
				TokenVocabulary.FORTRAN));
	}

	void clearTokensList() {
		curLine.clear();
	}

	CivlcToken createToken(int type, String text, int line, int col,
			Formation formation) {
		CivlcToken token = tf.newCivlcToken(type, text, formation,
				TokenVocabulary.FORTRAN);

		token.setLine(line);
		token.setCharPositionInLine(col);
		return token;
	}

	int currLineLA(int lookAhead) {
		if (lookAhead < curLine.size())
			return curLine.get(lookAhead - 1).getType();
		return -1;
	}

	void finalizeLine() {
		if (!finalTokens.addAll(curLine))
			System.err.println("Couldn't add to finalTokens!");
	}

	int findToken(int start, int desiredToken) {
		int numToken = curLine.size();
		Token t = null;

		while (start < numToken) {
			t = curLine.get(start);
			if (t.getType() == desiredToken)
				return start;
			start++;
		}
		return -1;
	}

	int findTokenInSuper(int lineStart, int desiredToken) {
		int lookAhead = 0;
		int tk, channel;

		/*****
		 * OBSOLETE NOTE: returning -1 is painful when looking for EOS // if
		 * this line is a comment, skip scanning it if (super.LA(1) ==
		 * MFortranLexer.LINE_COMMENT) { return -1; } OBSOLETE
		 *****/

		do {
			// lookAhead was initialized to 0
			lookAhead++;

			// get the token
			Token token = LT(lookAhead);
			tk = token.getType();
			channel = token.getChannel();

			// continue until find what looking for or reach end
		} while ((tk != MFortranLexer.EOF && tk != MFortranLexer.EOS
				&& tk != desiredToken) || channel == ANTLR_LEX_IGNORED_CHANNEL);

		if (tk == desiredToken) {
			// we found a what we wanted to
			return lookAhead;
		}

		return -1;
	}

	int getCurLineLength() {
		return curLine.size();
	}

	int getRawLineLength() {
		return rawLine.size();
	}

	Token getToken(int pos) {
		if (pos >= curLine.size() || pos < 0) {
			System.out.println("pos is out of range!");
			System.out.println(
					"pos: " + pos + " packedListSize: " + curLine.size());
			return null;
		} else
			return (Token) (curLine.get(pos));
	}

	ArrayList<Token> getTokensInCurLine() {
		return curLine;
	}

	boolean isKeyword(int tokenType) {
		switch (tokenType) {
			case MFortranLexer.INTEGER :
			case MFortranLexer.REAL :
			case MFortranLexer.COMPLEX :
			case MFortranLexer.CHARACTER :
			case MFortranLexer.LOGICAL :
			case MFortranLexer.ABSTRACT :
			case MFortranLexer.ACQUIRED_LOCK :
			case MFortranLexer.ALL :
			case MFortranLexer.ALLOCATABLE :
			case MFortranLexer.ALLOCATE :
			case MFortranLexer.ASSIGNMENT :
			//case MFortranLexer.ASSIGN :
			case MFortranLexer.ASSOCIATE :
			case MFortranLexer.ASYNCHRONOUS :
			case MFortranLexer.BACKSPACE :
			case MFortranLexer.BLOCK :
			case MFortranLexer.BLOCKDATA :
			case MFortranLexer.CALL :
			case MFortranLexer.CASE :
			case MFortranLexer.CLASS :
			case MFortranLexer.CLOSE :
			case MFortranLexer.CODIMENSION :
			case MFortranLexer.COMMON :
			case MFortranLexer.CONCURRENT :
			case MFortranLexer.CONTAINS :
			case MFortranLexer.CONTIGUOUS :
			case MFortranLexer.CONTINUE :
			case MFortranLexer.CRITICAL :
			case MFortranLexer.CYCLE :
			case MFortranLexer.DATA :
			case MFortranLexer.DEFAULT :
			case MFortranLexer.DEALLOCATE :
			case MFortranLexer.DEFERRED :
			case MFortranLexer.DO :
			case MFortranLexer.DOUBLE :
			case MFortranLexer.DOUBLEPRECISION :
			case MFortranLexer.DOUBLECOMPLEX :
			case MFortranLexer.ELEMENTAL :
			case MFortranLexer.ELSE :
			case MFortranLexer.ELSEIF :
			case MFortranLexer.ELSEWHERE :
			case MFortranLexer.ENTRY :
			case MFortranLexer.ENUM :
			case MFortranLexer.ENUMERATOR :
			case MFortranLexer.ERROR :
			case MFortranLexer.EQUIVALENCE :
			case MFortranLexer.EXIT :
			case MFortranLexer.EXTENDS :
			case MFortranLexer.EXTERNAL :
			case MFortranLexer.FILE :
			case MFortranLexer.FINAL :
			case MFortranLexer.FLUSH :
			case MFortranLexer.FORALL :
			case MFortranLexer.FORMAT :
			case MFortranLexer.FORMATTED :
			case MFortranLexer.FUNCTION :
			case MFortranLexer.GENERIC :
			case MFortranLexer.GO :
			case MFortranLexer.GOTO :
			case MFortranLexer.IF :
			case MFortranLexer.IMAGES :
			case MFortranLexer.IMPLICIT :
			case MFortranLexer.IMPORT :
			case MFortranLexer.IN :
			case MFortranLexer.INOUT :
			case MFortranLexer.INTENT :
			case MFortranLexer.INTERFACE :
			case MFortranLexer.INTRINSIC :
			case MFortranLexer.INQUIRE :
			case MFortranLexer.LOCK :
			case MFortranLexer.MEMORY :
			case MFortranLexer.MODULE :
			case MFortranLexer.NAMELIST :
			case MFortranLexer.NONE :
			case MFortranLexer.NON_INTRINSIC :
			case MFortranLexer.NON_OVERRIDABLE :
			case MFortranLexer.NOPASS :
			case MFortranLexer.NULLIFY :
			case MFortranLexer.ONLY :
			case MFortranLexer.OPEN :
			case MFortranLexer.OPERATOR :
			case MFortranLexer.OPTIONAL :
			case MFortranLexer.OUT :
			case MFortranLexer.PARAMETER :
			case MFortranLexer.PASS :
			case MFortranLexer.PAUSE :
			case MFortranLexer.POINTER :
			case MFortranLexer.PRINT :
			case MFortranLexer.PRECISION :
			case MFortranLexer.PRIVATE :
			case MFortranLexer.PROCEDURE :
			case MFortranLexer.PROGRAM :
			case MFortranLexer.PROTECTED :
			case MFortranLexer.PUBLIC :
			case MFortranLexer.PURE :
			case MFortranLexer.READ :
			case MFortranLexer.RECURSIVE :
			case MFortranLexer.RESULT :
			case MFortranLexer.RETURN :
			case MFortranLexer.REWIND :
			case MFortranLexer.SAVE :
			case MFortranLexer.SELECT :
			case MFortranLexer.SELECTCASE :
			case MFortranLexer.SELECTTYPE :
			case MFortranLexer.SEQUENCE :
			case MFortranLexer.STOP :
			case MFortranLexer.SUBMODULE :
			case MFortranLexer.SUBROUTINE :
			case MFortranLexer.SYNC :
			case MFortranLexer.TARGET :
			case MFortranLexer.THEN :
			case MFortranLexer.TO :
			case MFortranLexer.TYPE :
			case MFortranLexer.UNFORMATTED :
			case MFortranLexer.UNLOCK :
			case MFortranLexer.USE :
			case MFortranLexer.VALUE :
			case MFortranLexer.VOLATILE :
			case MFortranLexer.WAIT :
			case MFortranLexer.WHERE :
			case MFortranLexer.WHILE :
			case MFortranLexer.WRITE :
			case MFortranLexer.EVENT :
				// case MFortranLexer.ENDASSOCIATE :
				// case MFortranLexer.ENDBLOCK :
				// case MFortranLexer.ENDBLOCKDATA :
				// case MFortranLexer.ENDCRITICAL :
				// case MFortranLexer.ENDDO :
				// case MFortranLexer.ENDENUM :
				// case MFortranLexer.ENDFILE :
				// case MFortranLexer.ENDFORALL :
				// case MFortranLexer.ENDFUNCTION :
				// case MFortranLexer.ENDIF :
				// case MFortranLexer.ENDMODULE :
				// case MFortranLexer.ENDINTERFACE :
				// case MFortranLexer.ENDPROCEDURE :
				// case MFortranLexer.ENDPROGRAM :
				// case MFortranLexer.ENDSELECT :
				// case MFortranLexer.ENDSUBMODULE :
				// case MFortranLexer.ENDSUBROUTINE :
				// case MFortranLexer.ENDTYPE :
				// case MFortranLexer.ENDWHERE :
			case MFortranLexer.END :
			case MFortranLexer.DIMENSION :
			case MFortranLexer.KIND :
			case MFortranLexer.LEN :
			case MFortranLexer.BIND :
			case MFortranLexer.PRAGMA :
				return true;
			default :
				return false;
		}
	}

	boolean isKeyword(Token t) {
		return isKeyword(t.getType());
	}

	String lineToString(int lineStart, int lineEnd) {
		StringBuilder sb = new StringBuilder();

		for (int i = lineStart; i < curLine.size() - 1; i++)
			sb.append(curLine.get(i).getText());
		return sb.toString();
	}

	boolean lookForToken(int desiredToken) {
		int lookAhead = 1;
		int tk;

		do {
			// get the next token
			tk = this.LA(lookAhead);
			// update lookAhead in case we look again
			lookAhead++;
		} while (tk != MFortranLexer.EOS && tk != MFortranLexer.EOF
				&& tk != desiredToken);

		if (tk == desiredToken) {
			return true;
		} else {
			return false;
		}
	}

	void printCurLine() {
		System.out.println("*********************************");
		System.out.println("curLine Size is: " + curLine.size());
		System.out.println(curLine.toString());
		System.out.println("*********************************");
		return;
	}

	void removeToken(int index) {
		curLine.remove(index);
	}

	@SuppressWarnings("unchecked")
	void setRawLine(int lineStart) {
		curLineLen = this.getLineLength(lineStart);

		// this will get the tokens [lineStart->((lineStart+lineLength)-1)]
		rawLine = (ArrayList<Token>) getTokens(lineStart,
				(lineStart + curLineLen) - 1);
		if (rawLine == null) {
			System.err.println("currLine is null!!!!");
			System.exit(1);
		}

		// pack all non-ws tokens
		curLine = createCurLine();

	}

	void update() {
		super.tokens = finalTokens;
		super.setup();
		super.fill();
	}

	private ArrayList<Token> createCurLine() {
		int i = 0;
		Token tk = null;
		Formation formation = tf.newTransformFormation(getClass().getName(),
				"createCurLine");
		ArrayList<Token> pList = new ArrayList<Token>(curLineLen + 1);

		for (i = 0; i < rawLine.size(); i++) {
			tk = getTokenFromRawLine(i);
			try {
				if (tk.getChannel() != ANTLR_LEX_IGNORED_CHANNEL) {
					pList.add(tk);
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		} // end for(each item in buffered line)

		// need to make sure the line was terminated with a EOS. this may
		// not happen if we're working on a file that ended w/o a newline
		if (pList.get(pList.size() - 1).getType() != MFortranLexer.EOS) {
			pList.add(tf.newCivlcToken(MFortranLexer.EOS, "\n", formation,
					TokenVocabulary.FORTRAN));
		}

		return pList;
	}

	private int getLineLength(int start) {
		int idx = start;
		Token t;

		while (idx < tokens.size()) {
			t = super.get(idx++);
			if (t.getChannel() != ANTLR_LEX_IGNORED_CHANNEL
					&& (t.getType() == MFortranLexer.EOS
							|| t.getType() == MFortranLexer.EOF)) {
				return idx - start;
			}
		}
		return idx - start;
	}

	private Token getTokenFromRawLine(int pos) {
		if (pos >= rawLine.size() || pos < 0) {
			return null;
		} else {
			return ((Token) (rawLine.get(pos)));
		}
	}
}

class MFortranTokenSource implements TokenSource {
	// private CivlcToken head;
	private CivlcToken cur;

	protected MFortranTokenSource(CivlcToken token) {
		// head = token;
		cur = token;
	}

	@Override
	public Token nextToken() {
		CivlcToken tmp = cur;

		cur = cur.getNext();
		return tmp;
	}

	@Override
	public String getSourceName() {
		return getClass().getName();
	}
}