package edu.udel.cis.vsl.abc.front.fortran.preproc;

import org.antlr.runtime.Token;
import org.antlr.runtime.TokenSource;

import edu.udel.cis.vsl.abc.token.IF.CivlcToken;

class PP2CivlcTokenMFortranConverterTokenSource implements TokenSource {
	CivlcToken head;
	CivlcToken cur;

	PP2CivlcTokenMFortranConverterTokenSource(CivlcToken token) {
		head = token;
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
		return getClass().getName().toString();
	}

}
