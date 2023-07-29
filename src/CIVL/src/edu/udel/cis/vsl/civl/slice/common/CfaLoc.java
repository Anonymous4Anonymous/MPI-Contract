package edu.udel.cis.vsl.civl.slice.common;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.udel.cis.vsl.civl.model.IF.location.Location;

public class CfaLoc {
	
	boolean isExit = false;
	boolean isAbort = false;
	boolean isBranching = false;
	boolean isIPD = false;
	boolean isDirective = false;
	
	Location location = null;
	public Set<CfaLoc> successors = new HashSet<CfaLoc>();

	public CfaLoc (String type) {
		
		if (type.equals("Virtual Exit")) {
			isExit = true;
		} else if (type.equals("Virtual Abort")) {
			isAbort = true;
		} else {
			assert false : "Invalid virtual node.";
		}
		
	}
	
	public CfaLoc (Location l) {
		location = l;
		if (location.getSource().toString().contains("DirectedSymEx"))
			isDirective = true;
	}
	
	public String toString () {
		if (isExit) {
			return "EXIT";
		} else if (isAbort) {
			return "ABORT";
		} else if (location.getSource().toString().contains("DirectedSymEx")) {
			return "DIRECTIVE";
		} else {
			String lineNumber = getSourceLine(location.getSource().toString());
			return "line "+lineNumber+" ("+location.toString()+")";
		}
	}
	
	private String getSourceLine(String locationString) {
		
		Pattern p = Pattern.compile(":(\\d+)\\.");
		Matcher m = p.matcher(locationString);
		String line = "";
		if (m.find()) line = m.group(1);
		assert !line.isEmpty() : locationString+" has no line.";
		
		return line;
	}
}
