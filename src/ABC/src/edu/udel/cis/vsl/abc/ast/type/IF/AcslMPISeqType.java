package edu.udel.cis.vsl.abc.ast.type.IF;

/**
 * This class represents the <code>\mpi_seq_t</code> type in the ACSL-MPI
 * contract langauge. The type represents a sequence of values extracted from an
 * MPI message buffer with respect to a MPI type map. (MPI Type map is defined
 * in the MPI standard).
 * 
 * @author xxxx
 */
// TODO: temporarily extending ObjectType since ABC only supports C function
// type, which requires the return type to be a C object type instead of any
// type.
public interface AcslMPISeqType extends AcslMPIType, ObjectType {

}
