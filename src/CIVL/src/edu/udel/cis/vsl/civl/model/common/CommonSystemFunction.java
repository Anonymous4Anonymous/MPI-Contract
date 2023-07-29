/**
 * 
 */
package edu.udel.cis.vsl.civl.model.common;

import java.util.List;

import edu.udel.cis.vsl.civl.model.IF.CIVLSource;
import edu.udel.cis.vsl.civl.model.IF.Identifier;
import edu.udel.cis.vsl.civl.model.IF.ModelFactory;
import edu.udel.cis.vsl.civl.model.IF.Scope;
import edu.udel.cis.vsl.civl.model.IF.SystemFunction;
import edu.udel.cis.vsl.civl.model.IF.location.Location;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLType;
import edu.udel.cis.vsl.civl.model.IF.variable.Variable;

/**
 * @author zirkel
 * 
 */
public class CommonSystemFunction extends CommonFunction implements
		SystemFunction {

	private String library = null;
	private boolean needsEnabler = false;

	/**
	 * @param name
	 * @param parameters
	 * @param returnType
	 * @param containingScope
	 * @param startLocation
	 * @param factory
	 */
	public CommonSystemFunction(CIVLSource source, Identifier name,
			Scope parameterScope, List<Variable> parameters,
			CIVLType returnType, Scope containingScope, int fid,
			Location startLocation, String libraryName, boolean needsEnabler,
			ModelFactory factory) {
		super(source, true, name, parameterScope, parameters, returnType,
				containingScope, fid, startLocation, factory);
		this.isRoot = true;
		this.library = libraryName;
		this.needsEnabler = needsEnabler;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.udel.cis.vsl.civl.model.IF.SystemFunction#getLibrary()
	 */
	@Override
	public String getLibrary() {
		return library;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.udel.cis.vsl.civl.model.IF.SystemFunction#setLibrary(java.lang.String
	 * )
	 */
	@Override
	public void setLibrary(String libraryName) {
		this.library = libraryName;
	}

	@Override
	public String toString() {
		return this.name().name() + " : system function";
	}

	@Override
	public boolean isSystemFunction() {
		return true;
	}

	@Override
	public boolean isNormalFunction() {
		return false;
	}

	@Override
	public boolean needsEnabler() {
		return this.needsEnabler;
	}
}
