package edu.udel.cis.vsl.civl.model.IF.type;

import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicTupleType;

/**
 * Representing the ACSL-MPI <code>\mpi_buf_t</code> type.
 * @author xxxx
 *
 */
public interface CIVLMPIBufType extends CIVLType {

	@Override
	SymbolicTupleType  getDynamicType(SymbolicUniverse universe);
}
