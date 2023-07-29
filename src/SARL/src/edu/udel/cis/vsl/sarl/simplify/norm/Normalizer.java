package edu.udel.cis.vsl.sarl.simplify.norm;

import java.util.Set;

import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;
import edu.udel.cis.vsl.sarl.simplify.simplifier.InconsistentContextException;

public interface Normalizer {

	void normalize(Set<SymbolicConstant> dirtyIn,
			Set<SymbolicConstant> dirtyOut) throws InconsistentContextException;
}
