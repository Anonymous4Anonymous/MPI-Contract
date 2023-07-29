package edu.udel.cis.vsl.abc.token.IF;

import java.io.File;

/**
 * Information object for a source file processed by ABC. Wraps a {@link File}
 * with a unique integer index that can be used to identify that file in this
 * ABC invocation, and possibly other information.
 * 
 * @author xxxx
 */
public class SourceFile implements Comparable<SourceFile> {
	/**
	 * The {@link File} object wrapped by this {@link SourceFile}. Always non-
	 * {@code null}.
	 */
	private File file;

	/**
	 * The index of this {@link SourceFile} in the ordered list of all
	 * {@link SourceFile}s managed by the {@link FileIndexer} that is managing
	 * this {@link SourceFile}. Will be negative if this {@link SourceFile} is
	 * not owned by an indexer.
	 */
	private int index;

	/**
	 * A short name that will be used to identify this file, typically the
	 * filename (with no path) with a possible suffix such as "<2>" to make the
	 * name unique.
	 */
	private String nickname;

	/**
	 * Constructs new indexer in which the nickname is just the name of the
	 * given file (without path).
	 * 
	 * @param file
	 *            the {@link File} object wrapped by this {@link SourceFile}
	 * @param index
	 *            the index of this {@link SourceFile} in the ordered list of
	 *            all {@link SourceFile}s managed by the {@link FileIndexer}
	 *            that is managing this {@link SourceFile}
	 */
	public SourceFile(File file, int index) {
		this(file, index, file.getName());
	}

	/**
	 * Constructs new {@link SourceFile} with given fields.
	 * 
	 * @param file
	 *            the {@link File} object wrapped by this {@link SourceFile}
	 * @param index
	 *            the index of this {@link SourceFile} in the ordered list of
	 *            all {@link SourceFile}s managed by the {@link FileIndexer}
	 *            that is managing this {@link SourceFile}
	 * @param nickname
	 *            a short name that will be used to identify this file,
	 *            typically the filename (with no path) with a possible suffix
	 *            such as "<2>" to make the name unique
	 */
	public SourceFile(File file, int index, String nickname) {
		this.file = file;
		this.index = index;
		this.nickname = nickname;
	}

	/**
	 * Returns the {@link File} wrapped by this object.
	 * 
	 * @return the file
	 */
	public File getFile() {
		return file;
	}

	/**
	 * Returns the index,the index of this {@link SourceFile} in the ordered
	 * list of all {@link SourceFile}s managed by the {@link FileIndexer} that
	 * is managing this {@link SourceFile}
	 * 
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Returns the "nickname" that has been associated to this file. A short
	 * name that will be used to identify this file, typically the filename
	 * (with no path) with a possible suffix such as "<2>" to make the name
	 * unique.
	 */
	public String getNickname() {
		return nickname;
	}

	/**
	 * Constructs a human-readable string representation of this object, showing
	 * values of all fields.
	 * 
	 * @return a human-readable string representation of this object, showing
	 *         values of all fields.
	 */
	public String toString() {
		return "SourceFile[" + index + "," + nickname + "," + file.getPath()
				+ "]";
	}

	/**
	 * Constructs a string of the form "f"+index, which can be used to
	 * distinguish this sourcefile from all sourcefiles controlled by an
	 * indexer.
	 * 
	 * @return "f"+index
	 */
	public String getIndexName() {
		return "f" + index;
	}

	/**
	 * Returns the filename (without the path prefix). This is the last element
	 * in the path sequence specifying the file.
	 * 
	 * @return the filename
	 */
	public String getName() {
		return file.getName();
	}

	/**
	 * Returns the complete path for the file, including the filename.
	 * 
	 * @return the complete path
	 */
	public String getPath() {
		return file.getPath();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object)
			return true;
		if (object instanceof SourceFile) {
			return file.equals(((SourceFile) object).file)
					&& index == ((SourceFile) object).index;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return file.hashCode() ^ index * 37;
	}

	@Override
	public int compareTo(SourceFile o) {
		int result = index - o.index;

		if (result != 0)
			return result;
		result = file.compareTo(o.file);
		return result;
	}

}
