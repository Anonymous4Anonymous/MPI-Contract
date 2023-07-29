package edu.udel.cis.vsl.civl.model.IF;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import edu.udel.cis.vsl.civl.model.IF.type.CIVLPrimitiveType;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLPrimitiveType.PrimitiveTypeKind;
import edu.udel.cis.vsl.civl.transform.IF.GeneralTransformer;
import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.object.StringObject;

/**
 * This file contains the constants used by the model builder/translator, which
 * reflects the translation strategy of CIVL. For example, for every scope, the
 * heap variable is added as the variable with index 0.
 * 
 * @author zxxxx
 * 
 */
public final class ModelConfiguration {

	/* scope id and pointer id */

	public final static int STATIC_CONSTANT_SCOPE = 0;
	public final static int STATIC_ROOT_SCOPE = 1;
	public final static int DYNAMIC_CONSTANT_SCOPE = -1;
	public final static int DYNAMIC_NULL_SCOPE = -2;
	public final static int DYNAMIC_UNDEFINED_SCOPE = -1;
	public final static int NULL_POINTER_DYSCOPE = -1;
	public final static int NULL_POINTER_VID = -1;
	public final static int UNDEFINED_PROC_ID = -1;

	/* Transformer names */

	public final static String FILESYSTEM = "_io_filesystem";

	public final static String GENERAL_ROOT = GeneralTransformer.PREFIX
			+ "root";

	/* Names of Symbolic Constants */

	/**
	 * The array of prefixes for symbolic constants.
	 */
	public static final String[] SYMBOL_PREFIXES = {"X", "Y", "H"};

	/**
	 * The prefix of symbolic constants representing input variables.
	 */
	public static final int INPUT_PREIFX_INDEX = 0;

	/**
	 * The prefix of symbolic constants created by havoc.
	 */
	public static final int HAVOC_PREFIX_INDEX = 1;

	/**
	 * The prefix of symbolic constants representing heap objects.
	 */
	public static final int HEAP_OBJECT_PREFIX_INDEX = 2;

	/* Reserved names of symbolic constants */
	/**
	 * Constant for the name of invalid heap objects.
	 */
	public static final String INVALID = "INVALID";

	/**
	 * the set of reserved names for symbolic constants
	 */
	public static Set<String> RESERVE_NAMES = new HashSet<>(
			Arrays.asList(INVALID));

	/**
	 * add new name to the reserved name set
	 * 
	 * @param name
	 *            name to be added to the reserved name set
	 */
	public static void addReservedName(String name) {
		RESERVE_NAMES.add(name);
	}

	/* Global symbolic constants: size_of primitive types */
	public static Set<SymbolicConstant> SIZEOF_VARS = new HashSet<>();

	public static void addSizeofSymbol(SymbolicConstant symbol) {
		SIZEOF_VARS.add(symbol);
	}

	/* Domain decomposition strategies */
	/**
	 * ALL, corresponds to the enumerator ALL of the enumeration type
	 * $domain_decomposition.
	 */
	public static final int DECOMP_ALL = 0;

	/**
	 * RANDOM, corresponds to the enumerator ALL of the enumeration type
	 * $domain_decomposition.
	 */
	public static final int DECOMP_RANDOM = 1;

	/**
	 * ROUND_ROBIN, corresponds to the enumerator ALL of the enumeration type
	 * $domain_decomposition.
	 */
	public static final int DECOMP_ROUND_ROBIN = 2;

	/* System variables */

	/**
	 * The name of the atomic lock variable of the system scope.
	 */
	public static final String ATOMIC_LOCK_VARIABLE_INDEX = "_atomic_lock_var";

	/**
	 * The name of the time count variable, which is incremented by the system
	 * function $next_time_count() of civlc.cvh.
	 */
	public static final String TIME_COUNT_VARIABLE = "_time_count_var";

	/**
	 * The variable to store broken time information for the time library. This
	 * variable is needed because some functions of time.h returns a pointer to
	 * it.
	 */
	public static final String BROKEN_TIME_VARIABLE = "_broken_time_var";

	/**
	 * The name of the heap variable of each scope.
	 */
	public static final String HEAP_VAR = "_heap";

	/**
	 * The index of the heap variable in the scope.
	 */
	public static final int HEAP_VARIABLE_INDEX = 0;

	/**
	 * The name of the file system variable, created when stdio transformation
	 * is performed.
	 */
	public static final String FILE_SYSTEM_TYPE = "CIVL_filesystem";

	/* Types */

	/**
	 * The name of the $range type.
	 */
	public static final String RANGE_TYPE = "$range";

	/**
	 * The name of the "pthread_t" type defined in pthread library
	 */
	public static final String PTHREAD_THREAD_TYPE = "pthread_t";
	/**
	 * The name of _pthread_gpool_t type, which is the object type of the handle
	 * $pthread_gpool, and is used by pthread.cvl.
	 */
	public static final String PTHREAD_GPOOL = "_pthread_gpool_t";

	/**
	 * The name of _pthread_poo_t type, which is the object type of the handle
	 * $pthread_pool, and is used by pthread.cvl.
	 */
	public static final String PTHREAD_POOL = "_pthread_pool_t";

	/**
	 * The name of __barrier__ type, which is the object type of the handle
	 * $barrier.
	 */
	public static final String BARRIER_TYPE = "_barrier";

	/**
	 * the name of $bundle type
	 */
	public static final String BUNDLE_TYPE = "_bundle";

	/**
	 * the name of $dynamic type
	 */
	public static final String DYNAMIC_TYPE = "$dynamic";

	/**
	 * the name of the heap type
	 */
	public static final String HEAP_TYPE = "$heap";

	/**
	 * the name of message type
	 */
	public static final String MESSAGE_TYPE = "_message";

	/**
	 * the name of process reference type
	 */
	public static final String PROC_TYPE = "$proc";

	public static final String SCOPE_TYPE = "$scope";

	/**
	 * the name of queue type
	 */
	public static final String QUEUE_TYPE = "_queue";

	/**
	 * The name of $comm type, which is the object type of the handle $comm.
	 */
	public static final String COMM_TYPE = "_comm";

	/**
	 * The name of __gbarrier__ type, which is the object type of the handle
	 * $gbarrier.
	 */
	public static final String GBARRIER_TYPE = "_gbarrier";

	/**
	 * The name of __gcomm__ type, which is the object type of the handle
	 * $gcomm.
	 */
	public static final String GCOMM_TYPE = "$gcomm";

	/**
	 * The name of __int_iter__ type, which is the object type of the handle
	 * $int_iter.
	 */
	public static final String INT_ITER_TYPE = "$int_iter";

	/**
	 * The file type $file.
	 */
	public static final String REAL_FILE_TYPE = "$file";

	/**
	 * The file reference type FILE.
	 */
	public static final String FILE_STREAM_TYPE = "FILE";

	/**
	 * The tm type, used by time.h.
	 */
	public static final String TM_TYPE = "tm";

	/**
	 * The <code>$gcollator</code> type
	 */
	public static final String GCOLLATOR_TYPE = "$gcollator";

	/**
	 * The <code>$collator</code> type
	 */
	public static final String COLLATOR_TYPE = "$collator";

	/**
	 * The <code>$gcollate_state</code> type
	 */
	public static final String GCOLLATE_STATE = "$gcollate_state";

	/**
	 * The <code>$collate_state</code> type
	 */
	public static final String COLLATE_STATE = "$collate_state";

	/* libraries */

	/**
	 * The name of the time.h library.
	 */
	public static final String TIME_LIB = "time.h";

	/* Functions */

	/**
	 * the function to get the unique counter of time
	 */
	public static final String NEXT_TIME_COUNT = "$next_time_count";

	/**
	 * name of the symbolic type for mapping user-defined types to integers
	 */
	public static final String DYNAMIC_TYPE_NAME = "dynamicType";

	/**
	 * prefix for anonymous variables
	 */
	public static final String ANONYMOUS_VARIABLE_PREFIX = "_anon_";

	// TODO: following constant can replace ones in MPI2CIVLWorker
	/**
	 * The name of the identifier of the MPI_Comm variable in the final CIVL
	 * program.
	 */
	public final static String COMM_WORLD = "MPI_COMM_WORLD";

	/**
	 * The name of the identifier of the CMPI_Gcomm variable in the final CIVL
	 * program.
	 */
	public final static String GCOMM_WORLD = "_mpi_gcomm";

	/**
	 * The name of the identifier of the CMPI_Gcomm sequence variable in the
	 * final CIVL-MPI program
	 */
	public final static String GCOMMS = "_mpi_gcomms";

	/**
	 * The name of the variable representing the status of an MPI process, which
	 * is modified by MPI_Init() and MPI_Finalized().
	 */
	public final static String MPI_SYS_STATUS = "_mpi_status";

	/**
	 * The name of the input variable denoting the number of MPI processes in
	 * the final CIVL-C program.
	 */
	public final static String NPROCS = "_mpi_nprocs";

	/**
	 * The name of the input variable denoting the upper bound of the number of
	 * MPI processes in the final CIVL-C program.
	 */
	public final static String NPROCS_UPPER_BOUND = "_mpi_nprocs_hi";

	/**
	 * The name of the input variable denoting the lower bound of the number of
	 * MPI processes in the final CIVL-C program.
	 */
	public final static String NPROCS_LOWER_BOUND = "_mpi_nprocs_lo";

	/**
	 * The "\result" constant used in ACSL contracts which stands for the
	 * returned value.
	 */
	public static final String ContractResultName = "$result";

	/**
	 * The extended "\mpi_comm_rank" constant used in CIVL-ACSL contracts which
	 * stands for the rank in a specific communicator of an MPI process.
	 */
	public static final String ContractMPICommRankName = "$mpi_comm_rank";

	/**
	 * The extended "\mpi_comm_size" constant used in CIVL-ACSL contracts which
	 * stands for the number of MPI processes in a specific communicator.
	 */
	public static final String ContractMPICommSizeName = "$mpi_comm_size";

	/************* Methods and fields for naming convention *************/
	/**
	 * For all abstract functions declared in the source programs, their values,
	 * i.e. function type symbolic constants, should have names that starts with
	 * this prefix. This is the naming convention for values of abstract
	 * functions.
	 */
	private static final String CIVL_ABSTRACT_FUNCTION_PREFIX = "AF_";

	/**
	 * For all function type symbolic constants created by CIVL, their names all
	 * start with this prefix. This is the naming convention for function type
	 * symbolic constants that are generated by CIVL and are not values of
	 * abstract functions or not bound variables.
	 */
	private static final String CIVL_FUNCTION_CONSTANT_PREFIX = "CIVL_";

	/**
	 * The name prefix of symbolic constants that represent the values of sizes
	 * of primitive types:
	 */
	private static String SIZEOF_PRIMITIVE_TYPE_PREFIX = "SIZEOF_";

	/**
	 * <p>
	 * Returns a {@link StringObject} which is the name of the value an abstract
	 * function. The value of an abstract function is a function type symbolic
	 * constant.
	 * </p>
	 * 
	 * <p>
	 * The returned name follows CIVL's naming convention for abstract function
	 * values and is uniquely associated with the given <code>identifier</code>.
	 * </p>
	 * 
	 * <p>
	 * Note that for function type symbolic constants that are not values of
	 * abstract functions, their names should be what
	 * {@link #getFunctionConstantName(SymbolicUniverse, String)} decides to be.
	 * </p>
	 * 
	 * @param universe
	 *            a reference to the {@link SymbolicUniverse}
	 * @param identifier
	 *            an identifier that belongs to an abstract function
	 * @return a {@link StringObject} which is the name of an abstract function
	 */
	public static StringObject getAbstractFunctionName(
			SymbolicUniverse universe, String identifier) {
		return universe
				.stringObject(CIVL_ABSTRACT_FUNCTION_PREFIX + identifier);
	}

	/**
	 * <p>
	 * Returns a {@link StringObject} which is the name of a function type
	 * symbolic constant. This symbolic constant is not the value of an abstract
	 * function.
	 * </p>
	 * 
	 * <p>
	 * The returned name follows CIVL's naming convention for function type
	 * symbolic constants that are not values of abstract functions and is
	 * uniquely associated with the given <code>identifier</code>.
	 * </p>
	 * 
	 * @param universe
	 *            a reference to the {@link SymbolicUniverse}
	 * @param identifier
	 *            an identifier that belongs to a symbolic constant
	 * @return a {@link StringObject} which is the name of a symbolic constant
	 */
	public static StringObject getFunctionConstantName(
			SymbolicUniverse universe, String identifier) {
		return universe
				.stringObject(CIVL_FUNCTION_CONSTANT_PREFIX + identifier);
	}

	/**
	 * 
	 * @param universe
	 *            a reference to the {@link SymbolicUniverse}
	 * @return the {@link StringObject} which is the name of the symbolic
	 *         constant representing invalid heap object values.
	 */
	public static StringObject getInvalidName(SymbolicUniverse universe) {
		return universe.stringObject(INVALID);
	}

	/**
	 * <p>
	 * Returns the name of the unique symbolic constant which represents the
	 * value of the size of a specific {@link CIVLPrimitiveType}.
	 * </p>
	 * 
	 * @param universe
	 *            a reference to the {@link SymbolicUniverse}
	 * @param kind
	 *            the kind of the given {@link CIVLPrimitiveType}
	 * @return the name of the unique symbolic constant which represents the
	 *         value of the size of a specific {@link CIVLPrimitiveType}
	 */
	public static StringObject getSizeofPrimitiveTypeName(
			SymbolicUniverse universe, PrimitiveTypeKind kind) {
		return universe.stringObject(SIZEOF_PRIMITIVE_TYPE_PREFIX + kind);
	}

	/**
	 * <p>
	 * Returns the name of the unique function type symbolic constant which maps
	 * non-primitive types to their size values.
	 * </p>
	 * 
	 * @param universe
	 *            a reference to the {@link SymbolicUniverse}
	 * @return the name of the unique function type symbolic constant which maps
	 *         non-primitive types to their size values.
	 */
	public static StringObject getSizeofNonPrimitiveTypeFunctionName(
			SymbolicUniverse universe) {
		return getFunctionConstantName(universe, "SIZEOF");
	}
}
