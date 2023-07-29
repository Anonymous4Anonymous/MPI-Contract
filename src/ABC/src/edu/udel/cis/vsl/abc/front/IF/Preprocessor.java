package edu.udel.cis.vsl.abc.front.IF;

import java.io.File;
import java.util.Map;

import edu.udel.cis.vsl.abc.token.IF.CivlcTokenSource;
import edu.udel.cis.vsl.abc.token.IF.FileIndexer;

/**
 * <p>
 * A Preprocessor is used to preprocess source files. A single Preprocessor
 * object can be used to preprocess multiple files.
 * </p>
 * 
 * <p>
 * A Preprocessor does have some state: it keeps track of all the files it has
 * preprocessed. This includes files that were preprocessed because they were
 * (recursively) included by <code>#include</code> directives. It maintains an
 * ordered list in which each of these files occurs exactly once. This
 * essentially assigned a unique integer ID (numbered from 0) to all the files
 * that the preprocessor has encountered.
 * </p>
 * 
 * @author xxxx
 * 
 */

public interface Preprocessor {

	/**
	 * The path containing the ABC header files. This is internal to the
	 * project. The path is interpreted relative to the directories in the class
	 * path. The directory "/include" is in the class path. Therefore "abc" will
	 * be found in "/include/abc".
	 */
	final static File ABC_INCLUDE_PATH = new File(
			new File(File.separator + "include"), "abc");

	/**
	 * The name of the CIVL-C header file, which is included automatically at
	 * the beginning of any CIVL-C input file.
	 */
	final static String civlcHeaderName = "civlc.cvh";

	/**
	 * The name of the CIVL-C header file resource, which is needed to extract
	 * the contents of the file from the class path or jar.
	 */
	final static String civlcHeaderResource = new File(ABC_INCLUDE_PATH,
			civlcHeaderName).getAbsolutePath();

	/**
	 * Default value for system include path list.
	 */
	static File[] defaultSystemIncludes = new File[] {};

	/**
	 * Default value for user include path list. Currently, it consists of one
	 * directory, namely, the working directory.
	 */
	static File[] defaultUserIncludes = new File[] {
			new File(System.getProperty("user.dir")) };

	/**
	 * Given preprocessor source files, this returns a Token Source that emits
	 * the tokens resulting from preprocessing the file.
	 * 
	 * @param systemIncludePaths
	 *            the system include paths to search for included system headers
	 * @param userIncludePaths
	 *            the user include paths to search for included user headers
	 * @param predefinedMacros
	 *            the predefined macros, such as those specified in command line
	 * @param sourceFiles
	 *            the file sequence to be preprocessed as a single translation
	 *            unit
	 * @return the token source which is the output of the preprocessor, i.e.,
	 *         the result of tokenizing the files, applying the preprocessing
	 *         directives, etc.
	 * @throws PreprocessorException
	 *             if an I/O error occurs
	 */
	CivlcTokenSource preprocess(File[] systemIncludePaths,
			File[] userIncludePaths, Map<String, String> predefinedMacros,
			File[] sourceFiles) throws PreprocessorException;

	CivlcTokenSource preprocessLibrary(Map<String, String> predefinedMacros,
			String libraryFileName) throws PreprocessorException;

	FileIndexer getFileIndexer();

}
