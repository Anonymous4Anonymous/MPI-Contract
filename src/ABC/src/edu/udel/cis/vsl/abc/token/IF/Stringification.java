package edu.udel.cis.vsl.abc.token.IF;

/**
 * Represents use of the '#' operator in a function-like macro application. The
 * hash must be followed immediately by a parameter in a function-like macro
 * replacement list. When the macro is expanded, the token sequence of the
 * corresponding actual argument is stringified into a single string token which
 * replaces the hash and parameter.
 * 
 * @author xxxx
 *
 */
public interface Stringification extends Formation {

	/**
	 * Returns the function macro that is being applied and whose replacement
	 * sequence contains the hash ('#') character.
	 * 
	 * @return the macro being applied
	 */
	FunctionMacro getMacro();

	/**
	 * Returns the index of the replacement token (which is an occurrence of a
	 * formal parameter) in the sequence of replacement tokens specified in the
	 * macro definition.
	 * 
	 * @return the replacement token index
	 */
	int getReplacementTokenIndex();

	/**
	 * Returns the number of non-whitespace tokens occurring in the actual
	 * argument. These tokens will be concatenated using a single space as the
	 * separation character to form the new token. Note this number may be 0, in
	 * which case this token will be the empty string "".
	 * 
	 * @return number of non-whitespace tokens in argument
	 */
	int getNumArgumentTokens();

	/**
	 * Returns the index-th non-whitespace token in the actual argument.
	 * 
	 * @param index
	 *            integer in range [0,n-1), where n is the number of
	 *            non-whitespace tokens in the actual argument
	 * @return the index-th non-whitespace token in argument
	 */
	CivlcToken getArgumentToken(int index);

}
