package edu.udel.cis.vsl.abc.config.IF;

import java.math.BigInteger;

import edu.udel.cis.vsl.abc.config.IF.Configurations.Language;

/**
 * Configuration constants.
 * 
 * numbers of bits in types, etc.
 * 
 * unsigned char unsigned short int unsigned int unsigned long int unsigned long
 * long int
 * 
 * signed versions of above, low and high
 * 
 * @author xxxx
 * 
 */
public interface Configuration {

	/**
	 * The different machine architectures that can be used to specialize
	 * translation and analysis.
	 * 
	 * @author xxxx
	 *
	 */
	public enum Architecture {
		_32_BIT, _64_BIT, UNKNOWN
	}

	BigInteger unsignedCharMax();

	BigInteger unsignedShortIntMax();

	BigInteger unsignedIntMax();

	BigInteger unsignedLongIntMax();

	BigInteger unsignedLongLongIntMax();

	BigInteger signedCharMin();

	BigInteger signedCharMax();

	BigInteger signedShortIntMin();

	BigInteger signedShortIntMax();

	BigInteger signedIntMin();

	BigInteger signedIntMax();

	BigInteger signedLongIntMin();

	BigInteger signedLongIntMax();

	BigInteger signedLongLongIntMin();

	BigInteger signedLongLongIntMax();

	BigInteger charMin();

	BigInteger charMax();

	boolean inRangeUnsignedChar(BigInteger value);

	boolean inRangeUnsignedShort(BigInteger value);

	boolean inRangeUnsignedInt(BigInteger value);

	boolean inRangeUnsignedLongInt(BigInteger value);

	boolean inRangeUnsignedLongLongInt(BigInteger value);

	boolean inRangeSignedChar(BigInteger value);

	boolean inRangeSignedShort(BigInteger value);

	boolean inRangeSignedInt(BigInteger value);

	boolean inRangeSignedLongInt(BigInteger value);

	boolean inRangeSignedLongLongInt(BigInteger value);

	/**
	 * Is this configuration being used to solve an SV-COMP problem?
	 * 
	 * @return <code>true</code> iff this is an SV-COMP problem
	 */
	boolean getSVCOMP();

	/**
	 * Sets the SVCOMP flag, which specifies whether this configuration is being
	 * used to solve an SV-COMP problem.
	 * 
	 * @param flag
	 *            <code>true</code> iff this is an SV-COMP problem
	 */
	void setSVCOMP(boolean value);

	/**
	 * Gets the architecture type for this translation task.
	 * 
	 * @return the architecture type
	 */
	Architecture getArchitecture();

	/**
	 * Sets the architecture type for this translation task. Default is
	 * {@link Architecture#UNKNOWN}.
	 * 
	 * @param architecture
	 *            the architecture type
	 */
	void setArchitecture(Architecture arch);

	/**
	 * Are the GNU extensions to the C language allowed?
	 * 
	 * @return value of the GNUC flag
	 */
	boolean getGNUC();

	/**
	 * Specifies whether the GNU extensions to the C language are allowed.
	 * Default is false. This flag is also automatically set to true when the
	 * SVCOMP flag is set to true
	 * 
	 * @param flag
	 *            value of GNUC flag
	 */
	void setGNUC(boolean flag);

	/**
	 * @return the language of the source code; null if this filed has not been
	 *         set
	 */
	Language getLanguage();

	/**
	 * set the the language of the source code
	 */
	void setLanguage(Language language);
}
