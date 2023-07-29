package edu.udel.cis.vsl.civl.transform.common.contracts.translators;

public class MPI_CONTRACT_CONSTS {

	public static final String MPI_COMM_WORLD_CONST = "MPI_COMM_WORLD";

	public static final String MPI_COMM_RANK_CONST = "$mpi_comm_rank";

	public static final String MPI_COMM_SIZE_CONST = "$mpi_comm_size";

	public static final String MPI_IN_PLACE_SPOT_CONST = "MPI_IN_PLACE_SPOT";

	public static final String GENERIC_TEMP_VAR_NAME = "_tmp_";

	public static final String MPI_DATATYPE_TEMP_VAR_NAME = "_dt_tmp";

	public static final String MPI_HEAP_VAR_NAME = "_hp_tmp";

	public static final String ASSIGNS_TO_MEM_VAR_NAME = "_mem_assigns_";

	public static final String MPI_SIZEOFDATATYPE_FUN = "sizeofDatatype";

	public static final String MPI_EXTENT_ABSTRACT_FUN = "$mpi_extent";

	public static final String MPI_SIG_ABSTRACT_FUN = "$mpi_sig";

	public static final String MPI_NONOVERLAPPING_ABSTRACT_FUN = "$mpi_nonoverlapping";

	public static final String MPI_ASSIGNS_FUN = "$mpi_assigns";

	public static final String MPI_COMM_RANK_FUN = "MPI_Comm_rank";

	public static final String MPI_COMM_SIZE_FUN = "MPI_Comm_size";

	public static final String MPI_BARRIER_FUN = "MPI_Barrier";

	public static final String MPI_RESULT = "$result";

	public static final String MPI_SNAPSHOT = "$mpi_snapshot";

	public static final String MPI_UNSNAPSHOT = "$mpi_unsnapshot";
	
	public static final String MPI_POINTER_ADD_SYSTEM_FUN = "$mpi_pointer_add_sys";

	public static final String MEM_HAVOC_FUN = "$mem_havoc";

	public static final String SEPARATED_SYSTEM_FUN = "$separated";

	public static final String VALID_SYSTEM_FUN = "$valid";

	public static final String MEM_UNION_FUN = "$mem_union";

	public static final String MEM_EMPTY_FUN = "$mem_empty";

	public static final String WRITE_SET_PUSH_FUN = "$write_set_push";

	public static final String WRITE_SET_POP_FUN = "$write_set_pop";

	public static final String COLLATE_ARRIVED_FUN = "$collate_arrived";

	public static final String COLLATE_COMPLETE_FUN = "$collate_complete";

	public static final String COLLATE_STATE_TYPE = "$collate_state";

	public static final String COLLATE_GET_STATE_FUN = "$collate_get_state";

	public static final String GET_STATE_FUN = "$get_state";

	public static final String COLLATE_PRE_STATE_NAME = "_$collate_pre_";

	public static final String COLLATE_POST_STATE_NAME = "_$collate_post_";

	public static final String PRE_STATE_NAME = "_pre_state_";

	public static final String POST_STATE_NAME = "_post_state_";

	public static final String WAITSFOR_GUARD_VAR_NAME = "_waitsfor_guard_";
}
