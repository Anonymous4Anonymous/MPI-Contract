package edu.udel.cis.vsl.abc.front.IF;

import org.antlr.runtime.Token;

import edu.udel.cis.vsl.abc.err.IF.ABCException;
import edu.udel.cis.vsl.abc.token.IF.TokenUtils;

public class PP2CivlcTokenConversionException extends ABCException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Token token;

	public PP2CivlcTokenConversionException(String message, Token t) {
		super(message);
		token = t;
	}

	@Override
	public String toString() {
		String result = "PP2CivlcTokenConverter error: " + super.getMessage();

		if (token != null)
			result += "\nat " + TokenUtils.location(token, false) + ": "
					+ TokenUtils.quotedText(token);
		return result;
	}
}
