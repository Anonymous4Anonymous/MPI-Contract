package edu.udel.cis.vsl.abc.front.c.preproc;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.CharStream;

/**
 * <p>
 * An ANTLR stream which is built from an existing stream by filtering out
 * occurrences of backslash-newline. Furthermore, if the last character of the
 * input stream is not a newline, it adds a newline to the end.
 * </p>
 * 
 * <p>
 * Implementation nodes: meta-data: array of int of length number of lines.
 * Entry in position i is total number of characters preceding first character
 * in that line (after excluding backslash-newlines). This is the character
 * index of the first character in that line.
 * </p>
 * 
 * <p>
 * A lot of the code is copied from ANTLR's ANTLRStringStream.
 * </p>
 * 
 * @author xxxx
 */
public class FilteredStream implements CharStream {

	/**
	 * Source name obtained from old source.
	 */
	private String sourceName;

	/**
	 * The text, after all backslash-newline sequences removed. The final
	 * character will be newline: if it wasn't already present in the original
	 * stream, it will be added at construction. The EOF character does not
	 * occur here. The total "size" of this stream is the length of this array.
	 * Note that the size does NOT include any EOFs (despite what the misleading
	 * ANTLR documentation says).
	 */
	private char[] data;

	/**
	 * The index of the first character in each line. The entry in position i is
	 * the total number of characters preceding the first character in line i.
	 * (Excluding backslash-newlines, which have been removed.) This is a
	 * nonempty, non-decreasing array starting at 0. It is possible to have
	 * consecutive equal entries because the original text could contain lines
	 * that consisted solely of backslash-newline. The length of this array is
	 * one more than the number of lines. The final entry will be the length of
	 * {@link #data}.
	 */
	private int firsts[];

	/**
	 * The line of the current cursor position. Lines are numbered from 0.
	 * Initial value is 0. Once the last NEWLINE has been consumed, this will be
	 * the number of lines.
	 */
	private int currentLine = 0;

	/**
	 * The global character index of the current cursor position. This is the
	 * total number of characters consumed at this point.
	 */
	private int currentIndex = 0;

	/**
	 * Tracks how deep mark() calls are nested.
	 */
	private int markDepth = 0;

	/**
	 * The data stored with a "mark".
	 * 
	 * @author xxxx
	 *
	 */
	class StreamState {
		int line;
		int index;
	}

	/**
	 * A list of StreamState objects that tracks the stream state values line,
	 * charPositionInLine, and p that can change as you move through the input
	 * stream. Indexed from 1..markDepth. A null is kept @ index 0. Create upon
	 * first call to mark().
	 */
	private List<StreamState> markers;

	/** Track the last mark() call result value for use in rewind(). */
	private int lastMarker;

	/**
	 * Constructs new filtered stream from any given character stream by reading
	 * the entire stream and filtering out backslash-newlines, and adding a
	 * final newline if not already present.
	 * 
	 * @param stream
	 *            the original character stream
	 */
	public FilteredStream(CharStream stream) {
		int numLines = 0;
		int originalLength = stream.size();
		int dataLength = originalLength; // will be length of data

		this.sourceName = stream.getSourceName();
		for (int i = 0; i < originalLength; i++) {
			if (stream.LA(i + 1) == '\n') {
				numLines++;
				if (stream.LA(i) == '\\')
					dataLength -= 2;
			}
		}
		assert stream.LA(originalLength + 1) == CharStream.EOF;

		boolean addNewline = stream.LA(originalLength) != '\n'
				|| stream.LA(originalLength - 1) == '\\';

		if (addNewline) {
			numLines++;
			dataLength++;
		}
		data = new char[dataLength];
		firsts = new int[numLines + 1];
		firsts[0] = 0;

		int index = 0;
		int line = 0;

		for (int i = 0; i < originalLength; i++) {
			char c = (char) stream.LA(i + 1); // the index-th char

			if (c == '\\' && stream.LA(i + 2) == '\n') {
				line++;
				firsts[line] = index;
				i++; // skip the \n
			} else {
				data[index] = c;
				index++;
				if (c == '\n') {
					line++;
					firsts[line] = index;
				}
			}
		}
		if (addNewline) {
			line++;
			data[index] = '\n';
			index++;
			firsts[line] = index;
		}
	}

	/**
	 * Apparently this should consume one character, moving the "cursor" forward
	 * one position. The position is specified by {@link #currentLine} and
	 * {@link #currentCharacterInLine} and {@link #currentIndex}.
	 */
	@Override
	public void consume() {
		if (currentIndex == data.length)
			return;
		currentIndex++;
		// skip blank lines...
		if (firsts[currentLine + 1] == currentIndex) {
			do {
				currentLine++;
			} while (currentLine + 1 < firsts.length
					&& firsts[currentLine + 1] == currentIndex);
		}
	}

	@Override
	public int LA(int i) {
		if (i > 0) {
			// want to read the element with this index:
			int destination = i + currentIndex - 1;

			if (destination >= data.length)
				return EOF;
			else
				return data[destination];
		} else if (i < 0) {
			int destination = currentIndex + i;

			if (destination < 0)
				return -1; // undefined
			else
				return data[destination];
		}
		return -1; // undefined
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Copying ANTLRStringStream.
	 */
	@Override
	public int mark() {
		if (markers == null) {
			markers = new ArrayList<StreamState>();
			markers.add(null); // depth 0 means no backtracking, leave blank
		}
		markDepth++;

		StreamState state;

		if (markDepth >= markers.size()) {
			state = new StreamState();
			markers.add(state);
		} else {
			state = markers.get(markDepth);
		}
		state.index = currentIndex;
		state.line = currentLine;
		lastMarker = markDepth;
		return markDepth;
	}

	@Override
	public int index() {
		return currentIndex;
	}

	@Override
	public void rewind(int marker) {
		StreamState state = markers.get(marker);

		// restore stream state
		currentIndex = state.index;
		currentLine = state.line;
		release(marker);
	}

	@Override
	public void rewind() {
		// do I believe the javadoc for this method or what the
		// code in ANTLRStringStream actually does? Going with latter.
		rewind(lastMarker);
	}

	@Override
	public void release(int marker) {
		// unwind any other markers made after m and release m
		markDepth = marker;
		// release this marker
		markDepth--;
	}

	@Override
	public void seek(int index) {
		if (index > currentIndex) {
			if (index > data.length)
				index = data.length;
			currentIndex = index;
			while (currentLine + 1 < firsts.length
					&& firsts[currentLine + 1] <= index)
				currentLine++;
		} else if (index < currentIndex) {
			if (index < 0)
				index = 0;
			currentIndex = index;
			while (firsts[currentLine] > index)
				currentLine--;
		}
	}

	@Override
	public int size() {
		return data.length;
	}

	@Override
	public String getSourceName() {
		return sourceName;
	}

	@Override
	public String substring(int start, int stop) {
		return new String(data, start, stop - start + 1);
	}

	@Override
	public int LT(int i) {
		return LA(i);
	}

	@Override
	public int getLine() {
		return currentLine + 1;
	}

	@Override
	public void setLine(int line) {
		throw new UnsupportedOperationException(
				"Do not try to set the line number of a stream!");
	}

	@Override
	public void setCharPositionInLine(int pos) {
		throw new UnsupportedOperationException(
				"Do not try to set the char position in line of a stream!");
	}

	@Override
	public int getCharPositionInLine() {
		return currentIndex - firsts[currentLine];
	}

}
