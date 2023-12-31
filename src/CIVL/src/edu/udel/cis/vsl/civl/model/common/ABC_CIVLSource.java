package edu.udel.cis.vsl.civl.model.common;

import java.io.PrintStream;

import edu.udel.cis.vsl.abc.token.IF.Source;
import edu.udel.cis.vsl.civl.model.IF.CIVLSource;

/**
 * Implementation of CIVLSource formed by wrapping an ABC Source object.
 * 
 * @author xxxx
 * 
 */
public class ABC_CIVLSource implements CIVLSource {

	/* ************************** Instance Fields ************************** */

	private Source abcSource;

	/* **************************** Constructors *************************** */

	public ABC_CIVLSource(Source abcSource) {
		this.abcSource = abcSource;
	}

	/* *********************** Methods from CIVLSource ********************* */

	@Override
	public String getLocation() {
		return abcSource.getLocation(false);
	}

	@Override
	public String getSummary(boolean isException) {
		return abcSource.getSummary(false, isException);
	}

	@Override
	public String getContent() {
		return abcSource.getContent(false);
	}

	@Override
	public void print(PrintStream out) {
		abcSource.print(out);
	}

	/* ************************* Methods from Object *********************** */

	public String toString() {
		return abcSource.toString();
	}

	/* *************************** Public Methods ************************** */

	public Source getABCSource() {
		return abcSource;
	}

	@Override
	public boolean isSystemSource() {
		return false;
	}

	@Override
	public String getFileName() {
		return abcSource.getFirstToken().getFormation().getLastFile().getName();
	}
	
	public String getAbsoluteFilePath(){
		return abcSource.getFirstToken().getSourceFile().getFile().getAbsolutePath();
	}

}
