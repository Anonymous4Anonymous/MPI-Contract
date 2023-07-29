package edu.udel.cis.vsl.abc.token.IF;

/**
 * <p>
 * A {@link Concatenation} represents the concatenation of a sequence of tokens.
 * </p>
 * 
 * <p>
 * There are two contexts in which concatenations occur. The first is with he
 * preprocessor '##' operator. When used in the replacement sequence (body) of a
 * macro definition, this is a binary operator that concatenates the tokens to
 * its left and right to form a new token.
 * </p>
 * 
 * <p>
 * The second is with adjacent string literals, which are concatenated to form
 * one large string literal in translation phase 7 (after preprocessing), as
 * specified in the C11 Standard.
 * </p>
 * 
 * @author xxxx
 */
public interface Concatenation extends Formation {

	/**
	 * Gets the number of string literal tokens which are begin concatenated.
	 * 
	 * @return the number of string literals
	 */
	int getNumConstituents();

	/**
	 * Returns the index-th string literal token in the concatenation.
	 * 
	 * @param index
	 *            an integer in the range [0, numConstitutents-1]
	 * @return
	 */
	CivlcToken getConstituent(int index);

}
