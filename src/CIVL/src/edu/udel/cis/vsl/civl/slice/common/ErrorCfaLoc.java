package edu.udel.cis.vsl.civl.slice.common;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.udel.cis.vsl.civl.model.IF.location.Location;
import edu.udel.cis.vsl.civl.state.IF.State;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;

public class ErrorCfaLoc {
	
	private Location location = null;
	private CfaTransition nextTransition = null;
	private ErrorCfaLoc nextLocation = null;
	protected State state = null;
	protected Set<ErrorCfaLoc> successors = new HashSet<>();
	protected String sourceLine = null;
	protected BooleanExpression branchConstraint = null;
	
	private boolean isEntryLocation = false;
	private boolean isExitLocation = false;
	
	public int callingContext = 0;
	
	public ErrorCfaLoc (Location l, State s) { 
		location = l; 
		state = s; 
		callingContext = s.getProcessState(0).stackSize();
		sourceLine = getSourceLine(l.getSource().toString());
	}
	
	public ErrorCfaLoc () { isExitLocation  = true; }
	
	public Location getCIVLLocation () { return location; }
	
	protected CfaTransition nextTransition () {
		return nextTransition;
	}
	
	public void setNextTransition (CfaTransition t) {
		nextTransition = t;
	}
	
	protected ErrorCfaLoc nextLocation () {
		return nextLocation;
	}
	
	public void setNextLocation (ErrorCfaLoc l) {
		nextLocation = l;
	}
	
	boolean isEntryLocation () { return isEntryLocation; }
	
	public void setEntryLocation () { isEntryLocation = true; }
	
	boolean isExitLocation () { return isExitLocation; }
	
	public void setExitLocation () { isExitLocation = true; }
	
	@Override
	public boolean equals (Object o) {
		if (this == o) {
			return true;
		} else {
			if (o instanceof ErrorCfaLoc && this.isExitLocation) {
				return ((ErrorCfaLoc) o).isExitLocation;
			} else {
				return false;
			}
		}
	}
	
	@Override
	public int hashCode() {
		if (isExitLocation) {
			return 0;
		} else {
			return Objects.hash(state);
		}		
	    //return (isExitLocation ? 0 : location.hashCode());
	}

	@Override
	public String toString() {
		if (isExitLocation) {
			return "EXIT LOCATION";
		} else {
			return location.getSource().toString();
		}
	}
	
	private String getSourceLine(String locationString) {
		
		Pattern p = Pattern.compile(":(\\d+)\\.");
		Matcher m = p.matcher(locationString);
		String line = "";
		if (m.find()) line = m.group(1);
		//assert !line.isEmpty() : locationString+" has no line. Aborting.";
		if (line.isEmpty()) {
			return "0";
		} else {
			return line;
		}
		
		//return line;
	}
}
