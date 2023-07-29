package edu.udel.cis.vsl.abc.config.IF;

import java.util.Arrays;

import edu.udel.cis.vsl.abc.config.common.CommonConfiguration;

public class Configurations {

	/**
	 * The language of the program being processed. C is the default, but if the
	 * file suffix ends in ".cvl" the command line processor will change it to
	 * CIVL_C. As this is a public static variable, it can also be set manually.
	 */
	public static enum Language {
		/** The programming language C, as specified in the C11 Standard */
		C,
		/**
		 * The programming language CIVL-C, an extension of C for concurrency
		 * and verification. See
		 * <a href="http://vsl.cis.udel.edu/civl">http://vsl.cis.udel.edu/civl
		 * </a>.
		 */
		CIVL_C,
		/**
		 * The programming language FORTRAN, as specified in the FORTRAN
		 * Standard
		 */
		FORTRAN,
		/**
		 * Used for developing the new Fortran front.
		 */
		FORTRAN_OLD,
	};

	/**
	 * Returns new {@link Configuration} object in which all parameters have the
	 * lowest possible values allowed by the C Standard.
	 * 
	 * @return new minimal configuration object
	 */
	public static Configuration newMinimalConfiguration() {
		return new CommonConfiguration();
	}

	/**
	 * Finds best common language of mix of languages. If all the given
	 * languages are the same, that is the common language. Else the common
	 * language is {@link Language#CIVL_C}.
	 * 
	 * @param langs
	 *            a sequence of languages, none of which is <code>null</code>
	 * @return the common language: <code>null</code> if the sequence is empty,
	 *         the common language if all the languages are the same, else
	 *         {@link Language#CIVL_C}.
	 */
	public static Language commonLanguage(Iterable<Language> langs) {
		Language result = null;

		for (Language lang : langs) {
			if (lang == Language.CIVL_C) {
				result = lang;
				break;
			}
			if (result == null)
				result = lang;
			else if (result != lang) {
				result = Language.CIVL_C;
				break;
			}
		}
		return result;
	}

	/**
	 * Finds the best language to use for translation based on filename
	 * extensions.
	 * 
	 * @param filenames
	 *            sequence of non-<code>null</code> strings
	 * @return <code>null</code> if sequence is empty, else a {@link Language}
	 *         determined as follows: if all filenames have a C extension (.c.,
	 *         .h, or .i), then {@link Language#C}; else if all filenames have a
	 *         Fortran extension, {@link Language#FORTRAN}, else
	 *         {@link Language#CIVL_C}.
	 */
	public static Language bestLanguage(Iterable<String> filenames) {
		Language result = null;

		for (String name : filenames) {
			if (name.endsWith(".c") || name.endsWith(".h")
					|| name.endsWith(".i")) {
				// C code
				if (result == null)
					result = Language.C;
				else if (result != Language.C) {
					result = Language.CIVL_C;
					break;
				}
			} else {
				String lc = name.toLowerCase();

				if (lc.endsWith(".f") || lc.endsWith(".for")
						|| lc.endsWith(".f77") || lc.endsWith(".f90")
						|| lc.endsWith(".f95") || lc.endsWith(".f03")
						|| lc.lastIndexOf('.') < 0) {
					// Fortran code
					if (result == null)
						result = Language.FORTRAN;
					else if (result != Language.FORTRAN) {
						result = Language.CIVL_C;
						break;
					}
				} else {
					// something else: assume CIVL-C
					result = Language.CIVL_C;
					break;
				}
			}
		}
		return result;
	}

	/**
	 * Finds the best language to use for translation based on filename
	 * extensions.
	 * 
	 * @param filenames
	 *            array of non-<code>null</code> strings
	 * @return <code>null</code> if sequence is empty, else a {@link Language}
	 *         determined as follows: if all filenames have a C extension (.c.,
	 *         .h, or .i), then {@link Language#C}; else if all filenames have a
	 *         Fortran extension, {@link Language#FORTRAN}, else
	 *         {@link Language#CIVL_C}.
	 */
	public static Language bestLanguage(String[] filenames) {
		return bestLanguage(Arrays.asList(filenames));
	}

}
