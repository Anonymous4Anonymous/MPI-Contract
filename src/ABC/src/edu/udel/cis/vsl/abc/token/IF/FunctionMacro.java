package edu.udel.cis.vsl.abc.token.IF;

import org.antlr.runtime.Token;

/**
 * <p>
 * A FunctionMacro represents a C preprocessor function-like macro, which has
 * the from <code>#define f(X1,X2,...) ...</code>. The name of the macro is
 * <code>f</code>, the <code>X1</code>,<code>X2</code>, etc., are the formal
 * parameters, and the <code>...</code> is a sequence of replacement tokens. The
 * replacement tokens may include the formal parameters.
 * </p>
 * 
 * <p>
 * The last formal parameter in the formal parameter list may actually be an
 * ELLIPSIS ("..."). In this case the number of formal parameters includes the
 * ELLIPSIS as one of the formal parameters.
 * </p>
 * 
 * @author xxxx
 * 
 */
public interface FunctionMacro extends Macro {

	class FunctionReplacementUnit extends ReplacementUnit {

		public FunctionReplacementUnit(int index, Token token,
				Token[] whitespace) {
			super(index, token, whitespace);
		}

		/**
		 * If the replacement token is an occurrence of a formal parameter, this
		 * is the formal index; otherwise -1. An occurrence of the identifier
		 * "__VA_ARGS__" is treated as an occurrence of the formal parameter
		 * ELLIPSIS ("...") if the ELLIPSIS occurs in the formal parameter list.
		 */
		public int formalIndex;
	}

	/**
	 * Returns the number of formal parameters, including the ELLIPSIS parameter
	 * if present.
	 * 
	 * @return the number of formal parameters
	 */
	int getNumFormals();

	/**
	 * Is this a variadic macro?
	 * 
	 * @return <code>true</code> if there is at least one formal, and the last
	 *         formal is an ellipsis ("...")
	 */
	boolean isVariadic();

	/**
	 * Gets the index-th formal parameter. This may be ELLIPSIS ("...").
	 * 
	 * @param index
	 *            an integer in the range [0,numFormals-1]
	 * @return the index-th formal parameter token
	 */
	Token getFormal(int index);

	@Override
	FunctionReplacementUnit getReplacementUnit(int index);
}
