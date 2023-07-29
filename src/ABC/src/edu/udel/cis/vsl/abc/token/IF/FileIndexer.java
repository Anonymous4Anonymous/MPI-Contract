package edu.udel.cis.vsl.abc.token.IF;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * A {@link FileIndexer} keeps track of all source files opened during an
 * invocation of ABC. It associates a unique integer ID to each file, starting
 * from 0.
 * 
 * @author xxxx
 */
public interface FileIndexer {

	/**
	 * Get the number of distinct source files managed by this indexer.
	 * 
	 * @return the number of distinct source files seen by this indexer
	 */
	int getNumSourceFiles();

	/**
	 * Returns an ordered list of all {@link SourceFile}s managed by this
	 * indexer that have the given file name. The file name should not contain a
	 * path separator. The order is consistent with the order used for all
	 * {@link SourceFile}s managed by this indexer.
	 * 
	 * @param name
	 *            a non-{@code null} {@link String}, a file name
	 * @return the ordered list of source files with file name {@code name}, or
	 *         <code>null</code> if there are no such source files
	 */
	ArrayList<SourceFile> getSourceFilesWithName(String name);

	/**
	 * Returns the set consisting of all filenames of {@link File}s controlled
	 * by this indexer. Note that a file name does not include the path
	 * "leading up to" the file. In other words, a file name is the last
	 * component in the path sequence. Hence there may be multiple distinct
	 * {@link File}s in this indexer with the same file name.
	 * 
	 * @return the set of all file names occurring in this indexers
	 */
	Set<String> getFilenames();

	/**
	 * Get the source file with the given index (ID number).
	 * 
	 * @param index
	 *            an integer in [0,n), where n is the number of source files
	 *            managed by this indexer
	 * @return the i-th source file
	 */
	SourceFile getSourceFile(int index);

	/**
	 * Gets the {@link SourceFile} object corresponding to the given
	 * {@link File} . The given {@code file} must be a file managed by this
	 * indexer. There is a 1-1 correspondence between such {@link File}s and the
	 * {@link SourceFile}s maintained by this indexer. The {@link SourceFile}
	 * wraps a reference to the {@link File} with some additional information,
	 * such as the index.
	 * 
	 * @param file
	 *            a {@link File} that is maintained by this indexer
	 * @return the {@link SourceFile} corresponding to {@code file}
	 */
	SourceFile get(File file);

	/**
	 * If {@code file} is already managed by this indexer, gets the
	 * corresponding {@link SourceFile}, else it creates a new
	 * {@link SourceFile} wrapping {@link File}, adds that {@link SourceFile} to
	 * this indexer, and returns it.
	 * 
	 * @param file
	 *            a non-{@code null} {@link File} that may or may not be managed
	 *            by this indexer when this method is called
	 * @return the corresponding {@link SourceFile}
	 */
	SourceFile getOrAdd(File file);

	/**
	 * Prints the list of {@link File}s managed by this indexer, and related
	 * information, in a human-readable form.
	 * 
	 * @param out
	 *            the {@link PrintStream} where the output shall be sent
	 */
	void print(PrintStream out);

	/**
	 * Prints the list of {@link Files}s managed by this indexer, leaving out
	 * any file for which the path begins with a string in
	 * {@code ignoredPrefixes}. For example, if {@code ignoredPrefixes} contains
	 * "/include", then no file with path starting with "/include" will be
	 * printed.
	 * 
	 * @param out
	 *            the {@link PrintStream} where the output shall be sent
	 * @param ignoredPrefixes
	 *            a collection of prefixes specifying the files that should not
	 *            be printed
	 */
	void printFiltered(PrintStream out, Collection<String> ignoredPrefixes);

}
