package edu.udel.cis.vsl.civl.model.common;

import edu.udel.cis.vsl.civl.model.IF.CIVLFunction;
import edu.udel.cis.vsl.civl.model.IF.CIVLSource;
import edu.udel.cis.vsl.civl.model.IF.Identifier;
import edu.udel.cis.vsl.civl.model.IF.ModelFactory;
import edu.udel.cis.vsl.civl.model.IF.Scope;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLType;

public class CommonNondetFunction extends CommonFunction
		implements
			CIVLFunction {

	public CommonNondetFunction(CIVLSource source, Identifier name,
			CIVLType returnType, Scope containingScope, int fid,
			ModelFactory factory) {
		super(source, false, name, containingScope, null, returnType,
				containingScope, fid, null, factory);
	}

	@Override
	public boolean isNondet() {
		return true;
	}

	@Override
	public boolean isNormalFunction() {
		return false;
	}
}
