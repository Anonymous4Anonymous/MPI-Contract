package edu.udel.cis.vsl.civl.model.common;

import java.io.PrintStream;

import edu.udel.cis.vsl.civl.model.IF.CIVLSource;

public class ExpandedCIVLSource implements CIVLSource {

	private CIVLSource expandedSource;
	private CIVLSource baseSource;

	public ExpandedCIVLSource(CIVLSource expanded, CIVLSource base) {
		this.expandedSource = expanded;
		this.baseSource = base;
	}

	@Override
	public void print(PrintStream out) {
		out.print(this.getSummary(false));
	}

	@Override
	public String getLocation() {
		return expandedSource.getLocation();
	}

	@Override
	public String getSummary(boolean isException) {
		return expandedSource.getSummary(isException) + " from "
				+ baseSource.getSummary(isException);
	}

	@Override
	public boolean isSystemSource() {
		return false;
	}

	@Override
	public String getFileName() {
		return this.expandedSource.getFileName();
	}

	@Override
	public String getContent() {
		return expandedSource.getContent() + " from " + baseSource.getContent();
	}

	@Override
	public String getAbsoluteFilePath() {
		return this.expandedSource.getAbsoluteFilePath();
	}

}
