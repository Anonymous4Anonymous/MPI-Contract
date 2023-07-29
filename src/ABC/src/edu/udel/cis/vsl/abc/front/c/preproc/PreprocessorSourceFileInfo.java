package edu.udel.cis.vsl.abc.front.c.preproc;

import org.antlr.runtime.tree.Tree;

import edu.udel.cis.vsl.abc.token.IF.Formation;
import edu.udel.cis.vsl.abc.token.IF.SourceFile;

/**
 * A class which records information related to a source file that is parsed by
 * the preprocessor.
 * 
 * @author xxxx
 * 
 */
public class PreprocessorSourceFileInfo {

	private Formation history;

	private Tree position;

	public PreprocessorSourceFileInfo(Formation history, Tree position) {
		this.history = history;
		this.position = position;
	}

	public Formation getIncludeHistory() {
		return history;
	}

	public SourceFile getFile() {
		return history.getLastFile();
	}

	public Tree getPosition() {
		return position;
	}

	public void setPosition(Tree position) {
		this.position = position;
	}
}
