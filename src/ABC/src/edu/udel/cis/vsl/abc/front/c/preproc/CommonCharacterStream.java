package edu.udel.cis.vsl.abc.front.c.preproc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.antlr.runtime.ANTLRStringStream;

/**
 * <p>
 * A general-purpose ANTLR character stream which can be formed from an
 * arbitrary Java {@link InputStream}. Since it has no idea how long the input
 * stream will be, it dynamically allocates memory in chunks as it reads the
 * stream, and only at the end stitches everything together into a big char
 * array.
 * </p>
 * 
 * @author xxxx
 */
public class CommonCharacterStream extends ANTLRStringStream {

	// Static constants...

	/**
	 * The default number of characters to read at one time.
	 */
	public final static int DEFAULT_CHUNK_SIZE = 8192;

	// Types...

	/**
	 * Helper class for recording chunk of data read from stream.
	 * 
	 * @author xxxx
	 *
	 */
	private class Chunk {
		/**
		 * Buffer to store the characters read in from file. Length is specified
		 * in constructor but is always {@link #chunkSize} for now.
		 */
		char buf[];

		/**
		 * Number of elements actually used: bounded above by buf.length.
		 */
		int size;

		/**
		 * Creates a new chunk with buffer the given size.
		 * 
		 * @param bufferSize
		 *            the number of characters to allocate for {@link #buf}
		 *            array.
		 */
		Chunk(int bufferSize) {
			buf = new char[bufferSize];
		}
	}

	/**
	 * Name of this stream, like file name, for example.
	 */
	private String name;

	/**
	 * The number of characters to read at one time.
	 */
	private int chunkSize = DEFAULT_CHUNK_SIZE;

	// Constructors...

	/**
	 * Creates new filtered ANTLR input stream from given stream.
	 * 
	 * @param name
	 *            the name to use for this stream; used mostly for reporting
	 *            errors
	 * @param stream
	 *            the underlying stream to read from
	 * @param encoding
	 *            the character encoding or null if default encoding is to be
	 *            used
	 * @param chunkSize
	 *            the number of characters to read at one time from the stream
	 * @throws IOException
	 *             if something goes wrong reading from the underlying stream
	 */
	public CommonCharacterStream(String name, InputStream stream,
			String encoding, int chunkSize) throws IOException {
		this.name = name;
		this.chunkSize = chunkSize;
		load(stream, encoding);
	}

	/**
	 * Creates new filtered ANTLR input stream from given stream using default
	 * chunk size.
	 * 
	 * @param name
	 *            the name to use for this stream; used mostly for reporting
	 *            errors
	 * @param stream
	 *            the underlying stream to read from
	 * @param encoding
	 *            the character encoding or null if default encoding is to be
	 *            used
	 * @throws IOException
	 *             if something goes wrong reading from the underlying stream
	 */
	public CommonCharacterStream(String name, InputStream stream,
			String encoding) throws IOException {
		this.name = name;
		load(stream, encoding);
	}

	/**
	 * Creates new filtered ANTLR input stream from given stream using default
	 * chunk size and default character encoding.
	 * 
	 * @param name
	 *            the name to use for this stream; used mostly for reporting
	 *            errors
	 * @param stream
	 *            the underlying stream to read from
	 * @throws IOException
	 *             if something goes wrong reading from the underlying stream
	 */
	public CommonCharacterStream(String name, InputStream stream)
			throws IOException {
		this(name, stream, null);
	}

	/**
	 * Creates new filtered ANTLR input stream from a string.
	 * 
	 * @param name
	 *            the name to use for this stream; used mostly for reporting
	 *            errors
	 * @param string
	 *            a string while will form the source for the stream
	 * @param chunkSize
	 *            the number of characters to read from the string at one time
	 * @throws IOException
	 *             should not happen
	 */
	public CommonCharacterStream(String name, String string, int chunkSize)
			throws IOException {
		this(name, new ByteArrayInputStream(string.getBytes()), null,
				chunkSize);
	}

	/**
	 * Reads data from an input stream reader into chunks of memory. Goes
	 * through each chunk and removes backslash-newlines. Special handling if
	 * one chunk ends in backslash and the next begins with newline. Then
	 * allocates data array any copies the filtered data into it.
	 * 
	 * @param isr
	 *            the input stream reader
	 * @throws IOException
	 *             if something goes wrong reading the stream
	 */
	private void load(InputStreamReader isr) throws IOException {
		ArrayList<Chunk> chunks = new ArrayList<>();
		int totalSize = 0;
		// boolean previousBackslash = false; // does previous chunk end in \ ?
		// Chunk previousChunk = null; // the previous chunk
		int pos = 0;

		// first read in the whole file in chunks...
		while (true) {
			Chunk chunk = new Chunk(chunkSize);
			chunk.size = isr.read(chunk.buf);

			if (chunk.size <= 0)
				break;
			chunks.add(chunk);
			totalSize += chunk.size;
			if (chunk.size < chunkSize)
				break;
		}
		// // remove the backslash-newlines in-place...
		// for (Chunk chunk : chunks) {
		// char[] buf = chunk.buf;
		// int size = chunk.unfilteredLength;
		// int i = 0; // higher index
		// int j = 0; // lower index
		// char c = 0;
		//
		// assert size >= 1;
		// if (previousBackslash && buf[0] == '\n') {
		// // remove last char from previous chunk and skip the \n...
		// previousChunk.filteredLength--;
		// i = 1;
		// }
		// while (i < size) {
		// c = buf[i];
		// if (c == '\\' && i < size - 1 && buf[i + 1] == '\n') {
		// i += 2;
		// } else {
		// if (i != j)
		// buf[j] = c;
		// j++;
		// i++;
		// }
		// }
		// chunk.filteredLength = j;
		// previousBackslash = (c == '\\');
		// previousChunk = chunk;
		// }
		// count the filtered characters and allocate data...
		// for (Chunk chunk : chunks)
		// numFilteredChars += chunk.filteredLength;

		// int numChunks = chunks.size();
		// boolean addNewline = true;

		// for (int index = numChunks - 1; index >= 0; index--) {
		// Chunk chunk = chunks.get(index);
		// int len = chunk.filteredLength;
		//
		// if (len != 0) {
		// addNewline = chunk.buf[len - 1] != '\n';
		// break;
		// }
		// }
		// if (addNewline)
		// numFilteredChars++;
		super.data = new char[totalSize];
		super.n = totalSize;
		// copy chunk bufs into data...
		for (Chunk chunk : chunks) {
			System.arraycopy(chunk.buf, 0, super.data, pos, chunk.size);
			pos += chunk.size;
		}
		// if (addNewline)
		// super.data[pos] = '\n';
	}

	/**
	 * <p>
	 * Reads data from an input stream into chunks of memory. The character
	 * encoding is specified, which determines how to translate the bytes from
	 * the stream into characters. If null is given for encoding, the default
	 * encoding is used.
	 * </p>
	 * 
	 * <p>
	 * Goes through each chunk and removes backslash-newlines. Special handling
	 * if one chunk ends in backslash and the next begins with newline. Then
	 * allocates data array any copies the filtered data into it.
	 * </p>
	 * 
	 * @param stream
	 *            the input stream
	 * @param encoding
	 *            the character encoding
	 * @throws IOException
	 *             if something goes wrong reading the stream
	 */
	private void load(InputStream stream, String encoding) throws IOException {
		InputStreamReader isr;

		if (encoding != null) {
			isr = new InputStreamReader(stream, encoding);
		} else {
			isr = new InputStreamReader(stream);
		}
		try {
			load(isr);
		} finally {
			isr.close();
		}
	}

	@Override
	public String getSourceName() {
		return name;
	}
}
