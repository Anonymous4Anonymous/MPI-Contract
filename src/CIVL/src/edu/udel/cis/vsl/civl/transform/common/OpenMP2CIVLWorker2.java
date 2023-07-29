package edu.udel.cis.vsl.civl.transform.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import edu.udel.cis.vsl.abc.ast.IF.AST;
import edu.udel.cis.vsl.abc.ast.IF.ASTFactory;
import edu.udel.cis.vsl.abc.ast.entity.IF.Entity;
import edu.udel.cis.vsl.abc.ast.entity.IF.Variable;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode.NodeKind;
import edu.udel.cis.vsl.abc.ast.node.IF.IdentifierNode;
import edu.udel.cis.vsl.abc.ast.node.IF.NodePredicate;
import edu.udel.cis.vsl.abc.ast.node.IF.PairNode;
import edu.udel.cis.vsl.abc.ast.node.IF.SequenceNode;
import edu.udel.cis.vsl.abc.ast.node.IF.compound.CompoundInitializerNode;
import edu.udel.cis.vsl.abc.ast.node.IF.compound.DesignationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.InitializerNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.VariableDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.FunctionCallNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.IdentifierExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.IntegerConstantNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode.Operator;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.RegularRangeNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpAtomicNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpDeclarativeNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpExecutableNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpForNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpParallelNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpReductionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpReductionNode.OmpReductionOperator;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpSymbolReductionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpSyncNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpWorksharingNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.BlockItemNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.CivlForNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.CompoundStatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.DeclarationListNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.ExpressionStatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.ForLoopInitializerNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.ForLoopNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.StatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.TypeNode;
import edu.udel.cis.vsl.abc.ast.type.IF.StandardBasicType.BasicTypeKind;
import edu.udel.cis.vsl.abc.front.IF.CivlcTokenConstant;
import edu.udel.cis.vsl.abc.token.IF.Source;
import edu.udel.cis.vsl.abc.token.IF.SyntaxException;
import edu.udel.cis.vsl.civl.config.IF.CIVLConfiguration;
import edu.udel.cis.vsl.civl.config.IF.CIVLConstants;
import edu.udel.cis.vsl.civl.model.IF.CIVLSyntaxException;
import edu.udel.cis.vsl.civl.model.IF.CIVLUnimplementedFeatureException;
import edu.udel.cis.vsl.civl.transform.IF.OpenMP2CIVLTransformer;
import edu.udel.cis.vsl.civl.transform.common.OmpRegion.OmpRgnKind;
import edu.udel.cis.vsl.civl.util.IF.Triple;

/**
 * See
 * 
 * https://vsl.cis.udel.edu/trac/civl/wiki/OpenMPTransformation
 * 
 * for documentation on this transformation.
 * 
 *
 * @author xxxx (xxxx@udel.edu)
 */
public class OpenMP2CIVLWorker2 extends BaseWorker {
	// Fields
	/**
	 * The source information used by all nodes created by this OpenMP-to-CIVL
	 * transformer.
	 */
	static private final String SRC_INFO = "OpenMP2CIVLWorker2";

	/* Specific numbers used in this transformer */
	static private final int INDEX_PVT_DECLS = 0;
	static private final int INDEX_TMP_DECLS = 1;
	static private final int INDEX_RDC_INITS = 0;
	static private final int INDEX_RDC_COMBS = 1;
	static private final int ID_MASTER_THREAD = 0;
	/* OpenMP variable identifiers used */
	// function/type identifier prefix
	static private final String SIGN_DOLLAR = "$";
	// variable identifier prefix
	static private final String _OMP_ = "_omp_";
	static private final String ATOMIC_ = _OMP_ + "atomic_";
	static private final String FIRSTPRIVATE_ = _OMP_ + "fstpvt_";
	static private final String REDUCTION_ = _OMP_ + "reduction_";
	static private final String CRITICAL_ = _OMP_ + "critical_";
	// variable identifier suffix
	static private final String _NEXT = "_next";
	// commonly used variable identifiers
	static private final String DOM = _OMP_ + "dom";
	static private final String GTEAM = _OMP_ + "gteam";
	static private final String RANGE = _OMP_ + "range";
	static private final String NTHREADS = _OMP_ + "nthreads";
	static private final String NUM_THREADS = _OMP_ + "num_threads";
	/** CIVL input variable for the maximum number of OpenMP threads */
	static private final String TEAM = _OMP_ + "team";
	static private final String THREAD_MAX = _OMP_ + "thread_max";
	static private final String THREAD_RANGE = _OMP_ + "thread_range";
	static private final String TID = _OMP_ + "tid";
	// Construct loops
	static private final String DOM_LOOP = _OMP_ + "loop_domain";
	static private final String LOOP_DIST = _OMP_ + "loop_dist";
	static private final String ORDERED = _OMP_ + "ordered";
	// Construct sections
	static private final String SECTIONS_DIST = _OMP_ + "sections_dist";
	/** The variable name representing the OpenMP section block id */
	static private final String SID = _OMP_ + "sid";
	// Construct single
	static private final String SINGLE_DIST = _OMP_ + "single_dist";
	// Construct critical
	static private final String NAME_CRITICAL_UNSPEC = "";
	// clauses

	/* OpenMP helper types */
	static private final String OMP_HELPER_SIGNAL = "$omp_helper_signal";
	/* OpenMP function identifier */
	static private final String OMP_GET_MAX_THREADS = "omp_get_max_threads";
	static private final String OMP_GET_NUM_PROCS = "omp_get_num_procs";
	static private final String OMP_GET_NUM_THREADS = "omp_get_num_threads";
	static private final String OMP_GET_THREAD_NUM = "omp_get_thread_num";
	static private final String OMP_SET_NUM_THREADS = "omp_set_num_threads";
	static private final String OMP_SET_LOCK = "omp_set_lock";
	static private final String OMP_SET_NEST_LOCK = "omp_set_nest_lock";
	static private final String OMP_TEST_LOCK = "omp_test_lock";
	static private final String OMP_TEST_NEST_LOCK = "omp_test_nest_lock";
	static private final String OMP_UNSET_LOCK = "omp_unset_lock";
	static private final String OMP_UNSET_NEST_LOCK = "omp_unset_nest_lock";
	/* CIVL OpenMP verification helper function identifiers */
	static private final String CHECK_DATA_RACE = "$check_data_race";
	static private final String LOCAL_START = "$local_start";
	static private final String LOCAL_END = "$local_end";
	static private final String OMP_ARRIVE_SECTIONS = "$omp_arrive_sections";
	static private final String OMP_ARRIVE_SINGLE = "$omp_arrive_single";
	static private final String OMP_BARRIER = "$omp_barrier";
	static private final String OMP_GTEAM_CREATE = "$omp_gteam_create";
	static private final String OMP_GTEAM_DESTROY = "$omp_gteam_destroy";
	static private final String OMP_HELPER_SIGNAL_CREATE = "$omp_helper_signal_create";
	static private final String OMP_HELPER_SIGNAL_WAIT = "$omp_helper_signal_wait";
	static private final String OMP_HELPER_SIGNAL_SEND = "$omp_helper_signal_send";
	static private final String OMP_REDUCTION_COMBINE = "$omp_reduction_combine";
	static private final String OMP_TEAM_CREATE = "$omp_team_create";
	static private final String OMP_TEAM_DESTROY = "$omp_team_destroy";
	static private final String READ_AND_WRITE_SET_UPDATE = "$read_and_write_set_update";
	static private final String READ_SET_POP = "$read_set_pop";
	static private final String READ_SET_PUSH = "$read_set_push";
	static private final String WRITE_SET_POP = "$write_set_pop";
	static private final String WRITE_SET_PUSH = "$write_set_push";
	static private final String YIELD = "$yield";

	static private final NodePredicate PREDICATE_BARRIER_AND_FLUSH = new NodePredicate() {
		@Override
		public boolean holds(ASTNode node) {
			if (node instanceof ExpressionStatementNode) {
				ExpressionNode expr = ((ExpressionStatementNode) node)
						.getExpression();

				if (expr instanceof FunctionCallNode) {
					ExpressionNode func = ((FunctionCallNode) expr)
							.getFunction();

					if (func instanceof IdentifierExpressionNode)
						return ((IdentifierExpressionNode) func).getIdentifier()
								.name().equals(OMP_BARRIER);
				}
			}
			return false;
		}
	};

	/**
	 * The kind of a OpenMp private variable specified by a
	 * private/firstprivate/lastprivate clause or threadprivate directive.
	 */
	private enum PrivateKind {
		/** OpenMP private clause */
		DEFAULT,
		/** OpenMP firstprivate clause */
		FIRST,
		/** OpenMP lastprivate clause */
		LAST, // Unsupported currently
		/** threadprivate directive */
		THREAD, // Unsupported currently
	}

	/**
	 * The command line configuration information for querying transformation
	 * conditions.
	 */
	private CIVLConfiguration config;

	/** A counter for $omp_arrive_loop functions */
	private int ctrOmpArriveLoop = 0;
	/** A counter for $omp_arrive_sections functions */
	private int ctrOmpArriveSections = 0;
	/** A counter for $omp_arrive_single functions */
	private int ctrOmpArriveSingle = 0;
	/** A counter for OpenMP reduction items */
	private int ctrOmpReductionItem = 0;
	/** A counter for OpenMP ordered construct */
	private int ctrOmpOrdered = 0;

//	private int levelParallel = 0;

	/** is the binding region specified with <code>ordered(concurrent)</code> */
	private boolean orderConcurrent = false;

	private boolean hasAtomicConstruct = false;

	/**
	 * The stack storing current omp region information.
	 */
	private Stack<OmpRegion> ompRgn = new Stack<>();

	private Stack<ArrayList<OmpLoopInfo>> bindingLoopInfosRecords = new Stack<>();

	/**
	 * The root node of the input AST.
	 */
	private SequenceNode<BlockItemNode> root;

	/**
	 * A list of critical variable name for critical sections encountered
	 */
	private Set<String> criticalNames = new HashSet<>();

	private List<BlockItemNode> globalVarDecls = new LinkedList<>();

	private List<BlockItemNode> signalCreates = new LinkedList<>();

	Map<String, VariableDeclarationNode> reductionId2TempDecls;

	// Constructors
	/**
	 * Constructs a new instance of {@link OpenMP2CIVLWorker2}
	 * 
	 * @param astFactory
	 *            the {@link ASTFactory} instance used for performing
	 *            transformation
	 * @param config
	 *            the {@link CIVLConfiguration} instance used for querying
	 *            transformation conditions
	 */
	public OpenMP2CIVLWorker2(ASTFactory astFactory, CIVLConfiguration config) {
		super(OpenMP2CIVLTransformer.LONG_NAME, astFactory);
		this.identifierPrefix = "$omp_";
		this.config = config;
	}

	// Helper Functions or methods

	/**
	 * Returns a list of {@link ExpressionNode}
	 * <p>
	 * 1. if (a) the given <code>expr</code> is an {@link OperatorNode}, <br>
	 * (b) its {@link Operator} is any of following ones:
	 * {@link Operator#POSTDECREMENT}, {@link Operator#POSTINCREMENT},
	 * {@link Operator#PREDECREMENT}, {@link Operator#PREINCREMENT} <br>
	 * and (c) the operand expression has a scalar type, <br>
	 * then returns a size-<strong>1</strong> list containing the exact operand
	 * expression.
	 * </p>
	 * <p>
	 * 2. if (a) the given <code>expr</code> is an {@link OperatorNode}, <br>
	 * (b) its {@link Operator} is {@link Operator#ASSIGN}, <br>
	 * (c) the left hand side expression is not in the right hand side,<br>
	 * and (d) both sides have scalar types, <br>
	 * then, returns a size-<strong>2</strong> list containing both left and
	 * right hand side expressions.
	 * </p>
	 * <p>
	 * 2. if (a) the given <code>expr</code> is an {@link OperatorNode}, <br>
	 * (b) its {@link Operator} is any of following binary-assign-operators:
	 * {@link Operator#BITANDEQ}, {@link Operator#BITOREQ},
	 * {@link Operator#BITXOREQ}, {@link Operator#DIVEQ},
	 * {@link Operator#MINUSEQ}, {@link Operator#PLUSEQ},
	 * {@link Operator#SHIFTLEFTEQ}, {@link Operator#SHIFTRIGHTEQ},
	 * {@link Operator#TIMESEQ} <br>
	 * and (c) both sides have scalar types, <br>
	 * then, returns a size-<strong>3</strong> list as {x, x.copy, expr}
	 * </p>
	 * <p>
	 * 4. if (a) the given <code>expr</code> is an {@link OperatorNode}, <br>
	 * (b) its {@link Operator} is {@link Operator#ASSIGN}, <br>
	 * (c) the left hand side expression immediately appears in the right hand
	 * side expression as: <code>x = x bin-op expr</code> or
	 * <code>x = expr bin-op x</code><br>
	 * and (d) both <code>x</code> and <code>expr</code> have scalar types, <br>
	 * then returns a size-<strong>3</strong> list as {x, x, expr}
	 * </p>
	 * <p>
	 * 4. if the given <code>expr</code> does <strong>NOT</strong> satisfy any
	 * conditions listed above, <br>
	 * then returns an empty list, whose size is <strong>0</strong>
	 * </p>
	 * 
	 * @param expr
	 *            the expression required to be analyzed.
	 * @return see above
	 */
	private List<ExpressionNode> analyzeExprAssignScalar(ExpressionNode expr) {
		List<ExpressionNode> args = new LinkedList<>();

		if (expr instanceof OperatorNode) {
			OperatorNode opExpr = (OperatorNode) expr;
			ExpressionNode lhs = null;
			ExpressionNode rhs = null;

			switch (opExpr.getOperator()) {
				case POSTDECREMENT :
				case POSTINCREMENT :
				case PREDECREMENT :
				case PREINCREMENT :
					lhs = opExpr.getArgument(0);
					if (lhs.getType().isScalar())
						// Cond.1, 'args' is: {x}
						args.add(lhs);
					return args;
				case ASSIGN :
					lhs = opExpr.getArgument(0);
					rhs = opExpr.getArgument(1);

					if (lhs.getType().isScalar() && rhs.getType().isScalar()) {
						if (rhs instanceof OperatorNode) {
							// x = x bin-op expr
							// x = expr bin-op x
							opExpr = (OperatorNode) rhs;

							switch (opExpr.getOperator()) {
								case BITAND :
								case BITOR :
								case BITXOR :
								case DIV :
								case MINUS :
								case PLUS :
								case SHIFTLEFT :
								case SHIFTRIGHT :
								case TIMES :
									ExpressionNode xExpr = lhs;

									lhs = opExpr.getArgument(0);
									rhs = opExpr.getArgument(1);
									if (verifyExprSameEntity(xExpr, lhs)) {
										// x = x bin-op expr
										args.add(xExpr); // x
										args.add(lhs); // x
										args.add(rhs); // expr
										return args;
									}
									if (verifyExprSameEntity(xExpr, rhs)) {
										// x = expr bin-op x
										args.add(xExpr); // x
										args.add(rhs); // x
										args.add(lhs); // expr
										return args;
									}
									// x = expr (and x is not in expr)
									args.add(xExpr);
									args.add(opExpr);
									return args;
								default :
							}
						}
						// v = x OR x = expr
						args.add(lhs); // v or x
						args.add(rhs); // x or expr
					}
					return args;
				case BITANDEQ :
				case BITOREQ :
				case BITXOREQ :
				case DIVEQ :
				case MINUSEQ :
				case PLUSEQ :
				case SHIFTLEFTEQ :
				case SHIFTRIGHTEQ :
				case TIMESEQ :
					lhs = opExpr.getArgument(0);
					rhs = opExpr.getArgument(1);

					if (lhs.getType().isScalar() && rhs.getType().isScalar()) {
						// x bin-op = expr
						args.add(lhs); // x
						args.add(lhs.copy()); // x.copy
						args.add(rhs); // expr
					}
					return args;
				default :
			}
		}
		return args;
	}

	/** returns: <code>$check_data_race(_omp_team);</code> */
	private BlockItemNode callCheckDataRace(String srcMethod) {
		return nodeStmtCall(srcMethod, CHECK_DATA_RACE,
				nodeExprId(srcMethod, TEAM));
	}

	/** returns: <code>$omp_barrier(_omp_team);</code> */
	private BlockItemNode callOmpBarrier(String srcMethod) {
		return nodeStmtCall(srcMethod, OMP_BARRIER,
				nodeExprId(srcMethod, TEAM));
	}

	/** returns: <code>$omp_helper_signal_wait(&signalName, value);</code> */
	private StatementNode callSignalWait(String srcMethod, String signalName,
			int value) {
		return nodeStmtCall(srcMethod, OMP_HELPER_SIGNAL_WAIT,
				nodeExprAddrOf(srcMethod, nodeExprId(srcMethod, signalName)),
				nodeExprInt(srcMethod, value));
	}

	/** returns: <code>$omp_helper_signal_wait(&signalName, value);</code> */
	private StatementNode callSignalSend(String srcMethod, String signalName,
			int value) {
		return nodeStmtCall(srcMethod, OMP_HELPER_SIGNAL_SEND,
				nodeExprAddrOf(srcMethod, nodeExprId(srcMethod, signalName)),
				nodeExprInt(srcMethod, value));
	}

	/**
	 * Return {@link BlockItemNode}s representing:<br>
	 * <code>$read_set_pop();</code><br>
	 * <code>$write_set_pop();</code>
	 * 
	 * @param srcMethod
	 *            Dummy {@link Source} information based on caller name
	 * @return see above
	 */
	private List<BlockItemNode> callRWSetPop(String srcMethod) {
		return Arrays.asList(//
				nodeStmtCall(srcMethod, READ_SET_POP),
				nodeStmtCall(srcMethod, WRITE_SET_POP));
	}

	/**
	 * Return a list of {@link BlockItemNode} representing:<br>
	 * <code>$read_set_push();</code><br>
	 * <code>$write_set_push();</code>
	 * 
	 * @param srcMethod
	 *            Dummy {@link Source} information based on caller name
	 * @return see above
	 */
	private List<BlockItemNode> callRWSetPush(String srcMethod) {
		return Arrays.asList(//
				nodeStmtCall(srcMethod, READ_SET_PUSH),
				nodeStmtCall(srcMethod, WRITE_SET_PUSH));
	}

	/**
	 * Return a list of {@link BlockItemNode} representing:<br>
	 * <code>$read_and_write_set_update(team);</code><br>
	 * <code>$yield();</code>
	 * 
	 * @param srcMethod
	 *            Dummy {@link Source} information based on caller name
	 * @return see above
	 */
	private List<BlockItemNode> callYield(String srcMethod) {
		return Arrays.asList(//
				nodeStmtCall(srcMethod, READ_AND_WRITE_SET_UPDATE,
						nodeExprId(srcMethod, TEAM)),
				nodeStmtCall(srcMethod, YIELD));
	}

	/**
	 * Return {@link VariableDeclarationNode} representing:<br>
	 * <code>$domain(collapse) _omp_loop_dist = ($domain(collapse))
	 * $omp_arrive_loop(team, loop_id++, _omp_loop_domain, STRATEGY);</code>
	 * 
	 * @param srcMethod
	 *            Dummy {@link Source} information based on caller name
	 * @param collapse
	 *            the collapse value specified with the current OpenMP loop
	 *            construct
	 * @return see above
	 */
	private VariableDeclarationNode declOmpDistLoop(String srcMethod,
			int collapse) {
		// type: $domain(collapse)
		TypeNode typeDom = nodeTypeDom(srcMethod, collapse);
		// id: _omp_loop_dist
		IdentifierNode _omp_loop_dist = nodeIdent(srcMethod, LOOP_DIST);
		// init: ($domain(collapse))$omp_arrive_loop(
		// team, FOR_LOC++, _omp_loop_domain, STRATEGY);
		ExpressionNode init = nodeExprCast(srcMethod, typeDom.copy(),
				nodeExprCall(srcMethod, "$omp_arrive_loop",
						nodeExprId(srcMethod, TEAM),
						nodeExprInt(srcMethod, ctrOmpArriveLoop++),
						nodeExprCast(srcMethod, nodeTypeDom(srcMethod, 0),
								nodeExprId(srcMethod, DOM_LOOP)),
						nodeExprInt(srcMethod, config.ompLoopDecomp())));

		return nodeFactory.newVariableDeclarationNode(
				newSource(srcMethod, CivlcTokenConstant.DECLARATION),
				_omp_loop_dist, typeDom, init);
	}

	/**
	 * Return {@link VariableDeclarationNode} representing:<br>
	 * <code>$domain(1) _omp_sections_dist = ($domain(1)) 
	 * $omp_arrive_sections(_omp_team, section_id++, numSection);</code>
	 * 
	 * @param srcMethod
	 *            Dummy {@link Source} information based on caller name
	 * @param numSection
	 *            the number of OpenMP section block in related sections
	 *            construct
	 * @return see above
	 */
	private BlockItemNode declOmpDistSections(String srcMethod,
			int numSection) {
		// type: $domain(1)
		TypeNode typeDom = nodeTypeDom(srcMethod, 1);
		// id: _omp_sections_dist
		IdentifierNode _omp_sections_dist = nodeIdent(srcMethod, SECTIONS_DIST);
		// init: ($domain(collapse))$omp_arrive_loop(
		// team, FOR_LOC++, _omp_loop_domain, STRATEGY);
		ExpressionNode init = nodeExprCast(srcMethod, typeDom.copy(),
				nodeExprCall(srcMethod, OMP_ARRIVE_SECTIONS,
						nodeExprId(srcMethod, TEAM),
						nodeExprInt(srcMethod, ctrOmpArriveSections++),
						nodeExprInt(srcMethod, numSection)));

		return nodeFactory.newVariableDeclarationNode(
				newSource(srcMethod, CivlcTokenConstant.DECLARATION),
				_omp_sections_dist, typeDom, init);
	}

	/**
	 * Return {@link VariableDeclarationNode} representing:<br>
	 * <code>int _omp_single_dist = $omp_arrive_single(team, single_id++);</code>
	 * 
	 * @param srcMethod
	 *            Dummy {@link Source} information based on caller name
	 * @return see above
	 */
	private BlockItemNode declOmpDistSingle(String srcMethod) {
		// type: int
		TypeNode type = nodeTypeInt(srcMethod);
		// id: _omp_single_dist
		IdentifierNode _omp_single_dist = nodeIdent(srcMethod, SINGLE_DIST);
		// init: $omp_arrive_single(team, single_id++);
		ExpressionNode init = nodeExprCall(srcMethod, OMP_ARRIVE_SINGLE,
				nodeExprId(srcMethod, TEAM),
				nodeExprInt(srcMethod, ctrOmpArriveSingle++));

		return nodeFactory.newVariableDeclarationNode(
				newSource(srcMethod, CivlcTokenConstant.DECLARATION),
				_omp_single_dist, type, init);
	}

	/**
	 * Return {@link VariableDeclarationNode} representing:<br>
	 * <code>$domain(1) _omp_dom = ($domain){_omp_thread_range};</code> (if
	 * <code>numRanges</code> is 0) <strong>OR</strong><br>
	 * <code>$domain(1) _omp_loop_dom = ($domain){_omp_range1, ...};</code> (if
	 * <code>numRanges</code> is positive) <br>
	 * <strong>PRE-CONDITION</strong>: <code>numRanges</code> shall be
	 * non-negative.
	 * 
	 * @param srcMethod
	 *            Dummy {@link Source} information based on caller name
	 * @param numRanges
	 *            <code>0</code> for OpenMP parallel region; or a positive
	 *            number representing the number of associated for loops.
	 * @return See above
	 */
	private VariableDeclarationNode declOmpDomain(String srcMethod,
			int numRanges) {
		boolean isParallel = numRanges == 0;
		// type: $domain
		TypeNode typeDom = nodeTypeDom(srcMethod, 1);
		IdentifierNode idDom = null;
		List<PairNode<DesignationNode, InitializerNode>> initials = new ArrayList<>();

		if (isParallel) {
			// id: _omp_dom
			idDom = nodeIdent(srcMethod, DOM);
			// initials: _omp_thread_range
			initials.add(nodeFactory.newPairNode(
					/* src */ newSource(srcMethod, CivlcTokenConstant.STRUCT),
					/* dsgn */(DesignationNode) null, //
					/* init */ nodeExprId(srcMethod, THREAD_RANGE)));
		} else {
			// id: _omp_loop_domain
			idDom = nodeIdent(srcMethod, DOM_LOOP);
			// initials: _omp_range1, .. ,_omp_rangeX
			for (int i = 1; i <= numRanges; i++)
				initials.add(nodeFactory.newPairNode(
						/* src */ newSource(srcMethod,
								CivlcTokenConstant.STRUCT),
						/* dsgn */ (DesignationNode) null, //
						/* init */ nodeExprId(srcMethod,
								RANGE + Integer.toString(i))));
		}

		// initialList: {_omp_thread_range} OR {_omp_range1, .., _omp_rangeX}
		CompoundInitializerNode initialList = nodeFactory
				.newCompoundInitializerNode(newSource(srcMethod,
						CivlcTokenConstant.INITIALIZER_LIST), initials);
		// init: ($domain){..} or
		InitializerNode init = nodeFactory.newCompoundLiteralNode(
				/* src */ newSource(srcMethod,
						CivlcTokenConstant.COMPOUND_LITERAL),
				/* type */ nodeTypeDom(srcMethod, 0),
				/* initials */ initialList);

		return nodeFactory.newVariableDeclarationNode(
				newSource(srcMethod, CivlcTokenConstant.DECLARATION), //
				idDom, typeDom, init);
	}

	/**
	 * Return {@link VariableDeclarationNode} representing:<br>
	 * <code>$omp_gteam gteam = $omp_gteam_create($here, nthreads);</code>
	 * 
	 * @param srcMethod
	 *            Dummy {@link Source} information based on caller name
	 * @return See above
	 */
	private VariableDeclarationNode declOmpGteam(String srcMethod) {
		// type: $omp_gteam
		TypeNode typeGteam = nodeTypeNamed(srcMethod, "$omp_gteam");
		// id: _omp_gteam
		IdentifierNode _omp_gteam = nodeIdent(srcMethod, GTEAM);
		// init: $omp_gteam_create($here, nthreads)
		InitializerNode init = nodeExprCall(srcMethod, OMP_GTEAM_CREATE,
				nodeFactory.newHereNode(
						newSource(srcMethod, CivlcTokenConstant.HERE)),
				nodeExprId(srcMethod, NTHREADS));

		return nodeFactory.newVariableDeclarationNode(
				newSource(srcMethod, CivlcTokenConstant.DECLARATION), //
				_omp_gteam, typeGteam, init);
	}

	/**
	 * Return {@link VariableDeclarationNode} representing:<br>
	 * <code>$omp_helper_signal /signalName/ = $omp_helper_signal_create(/initExpr/);</code>
	 * 
	 * @param srcMethod
	 *            Dummy {@link Source} information based on caller name
	 * @param signalName
	 *            The name of a <code>critical</code> signal struct variable for
	 *            a critical section encountered.
	 * @param initExpr
	 *            the initialization expression used as the argument for the
	 *            function <code>$omp_helper_signal_create</code>.
	 * @return See above
	 */
	private VariableDeclarationNode declOmpHelperSignal(String srcMethod,
			String signalName, ExpressionNode initExpr) {
		// type: $omp_helper_signal
		TypeNode type = nodeTypeNamed(srcMethod, OMP_HELPER_SIGNAL);
		// id: /signalName/
		IdentifierNode id = nodeIdent(srcMethod, signalName);
		// init: $omp_helper_signal_create(/initExpr/);
		InitializerNode init = nodeExprCall(srcMethod, OMP_HELPER_SIGNAL_CREATE,
				initExpr);

		return nodeFactory.newVariableDeclarationNode(
				newSource(srcMethod, CivlcTokenConstant.DECLARATION), //
				id, type, init);
	}

	private List<BlockItemNode> declOmpLoopVarNext(String srcMethod,
			List<OmpLoopInfo> loopInfos) {
		List<BlockItemNode> loopVarNextDecls = new LinkedList<>();

		for (OmpLoopInfo info : loopInfos) {
			TypeNode type = nodeTypeInt(srcMethod);
			IdentifierNode loop_var_next = nodeIdent(srcMethod,
					info.loopVarName + _NEXT);
			InitializerNode init = nodeFactory.newOperatorNode(
					/* src */ newSource(srcMethod, CivlcTokenConstant.EXPR),
					/* op */ Operator.PLUS,
					/* arg0 */ nodeExprId(srcMethod, info.loopVarName),
					/* arg1 */ info.range.third.copy());

			loopVarNextDecls.add(nodeFactory.newVariableDeclarationNode(
					newSource(srcMethod, CivlcTokenConstant.DECLARATION),
					loop_var_next, type, init));
		}
		return loopVarNextDecls;
	}

	/**
	 * Return {@link VariableDeclarationNode} representing:<br>
	 * <code>int _omp_nthreads = 1+$choose_int(_omp_num_threads);</code> (if
	 * there is no explicit num_threads clause declared)<br>
	 * <code>int _omp_nthreads = _omp_num_threads;</code> (if an explicit
	 * num_threads clause is declared.)
	 * 
	 * @param srcMethod
	 *            Dummy {@link Source} information based on caller name
	 * @param numThds
	 *            The {@link ExpressionNode} for <code>_omp_num_threads</code>
	 * @param isDeclared
	 *            <code>true</code> iff an explicit num_threads clause is
	 *            declared with an exact constant value defining the number of
	 *            threads.
	 * @return see above
	 */
	private VariableDeclarationNode declOmpNthreads(String srcMethod,
			ExpressionNode numThds, Boolean isDeclared) {
		// type: int
		TypeNode typeInt = nodeTypeInt(srcMethod);
		// id: _omp_nthreads
		IdentifierNode _omp_nthreads = nodeIdent(srcMethod, NTHREADS);
		// init: _omp_num_threads
		// OR
		// init: 1+$choose_int(_omp_num_threads)
		InitializerNode init = isDeclared
				? numThds
				: nodeFactory.newOperatorNode(
						/* src */ newSource(srcMethod, CivlcTokenConstant.EXPR),
						/* op */ Operator.PLUS,
						/* arg0 */ nodeExprInt(srcMethod, 1),
						/* arg1 */ nodeExprCall(srcMethod, "$choose_int",
								numThds));

		return nodeFactory.newVariableDeclarationNode(
				newSource(srcMethod, CivlcTokenConstant.DECLARATION), //
				_omp_nthreads, typeInt, init);
	}

	/**
	 * Return {@link VariableDeclarationNode} representing:<br>
	 * <code>int _omp_num_threads = _omp_thread_max;</code>
	 * 
	 * @param srcMethod
	 *            Dummy {@link Source} information based on caller name
	 * @return see above
	 */
	private VariableDeclarationNode declOmpNumThreads(String srcMethod) {
		// type: int
		TypeNode typeInt = nodeTypeInt(srcMethod);
		// id: _omp_nthreads
		IdentifierNode _omp_num_threads = nodeIdent(srcMethod, NUM_THREADS);
		// init: _omp_thread_max
		InitializerNode init = nodeExprId(srcMethod, THREAD_MAX);

		return nodeFactory.newVariableDeclarationNode(
				newSource(srcMethod, CivlcTokenConstant.DECLARATION), //
				_omp_num_threads, typeInt, init);
	}

	/**
	 * Add signal struct variable declarations in global scope for each loop
	 * variables associated with an ordered clause
	 * 
	 * @param srcMethod
	 *            Dummy {@link Source} information based on caller name
	 * @param loopInfos
	 *            a list of {@link OmpLoopInfo}s, each of them contains both
	 *            lower and upper bounds and the incremental stride for a single
	 *            associated loop clause
	 */
	private void declOmpOrderedSignals(String srcMethod,
			List<OmpLoopInfo> loopInfos) {
		for (int i = 0; i < loopInfos.size(); i++) {
			signalCreates.add(
					declOmpHelperSignal(srcMethod, ORDERED + (ctrOmpOrdered++),
							loopInfos.get(i).range.first.copy()));
		}
	}

	/**
	 * Return {@link VariableDeclarationNode} representing:<br>
	 * <code>$range _omp_rangeX = {lo .. hi#step};</code>
	 * 
	 * @param srcMethod
	 *            Dummy {@link Source} information based on caller name
	 * @param num
	 *            The identifier number X of the declared $range type variable
	 * @return See above
	 */
	private VariableDeclarationNode declOmpRange(String srcMethod, int num,
			Triple<ExpressionNode, ExpressionNode, ExpressionNode> range) {
		// type: $range
		TypeNode typeInt = nodeTypeRange(srcMethod);
		// id: _omp_rangeX
		IdentifierNode _omp_rangeX = nodeIdent(srcMethod,
				RANGE + Integer.toString(num));
		// init: {lo .. hi#step}
		InitializerNode init = nodeExprRange(srcMethod, range.first,
				range.second, range.third);

		return nodeFactory.newVariableDeclarationNode(
				newSource(srcMethod, CivlcTokenConstant.DECLARATION), //
				_omp_rangeX, typeInt, init);
	}

	/**
	 * Return {@link VariableDeclarationNode} representing:<br>
	 * <code>$omp_team _omp_team = $omp_team_create($here, _omp_gteam, _omp_tid);</code>
	 * 
	 * @param srcMethod
	 *            Dummy {@link Source} information based on caller name
	 * @return See above
	 */
	private VariableDeclarationNode declOmpTeam(String srcMethod) {
		// type: "$omp_team"
		TypeNode typeTeam = nodeTypeNamed(srcMethod, "$omp_team");
		// id: _omp_team
		IdentifierNode _omp_team = nodeIdent(srcMethod, TEAM);
		// init: $omp_team_create($here, gteam, _tid);
		InitializerNode init = nodeExprCall(srcMethod, OMP_TEAM_CREATE,
				nodeExprHere(srcMethod), nodeExprId(srcMethod, GTEAM),
				nodeExprId(srcMethod, TID));

		return nodeFactory.newVariableDeclarationNode(
				newSource(srcMethod, CivlcTokenConstant.DECLARATION), //
				_omp_team, typeTeam, init);
	}

	/**
	 * Return {@link VariableDeclarationNode} representing:<br>
	 * <code>$input int _omp_thread_max;</code>
	 * 
	 * @param srcMethod
	 *            Dummy {@link Source} information based on caller name
	 * @return
	 */
	private VariableDeclarationNode declOmpThreadMax(String srcMethod) {
		// type: int
		TypeNode type = nodeTypeInt(srcMethod);
		// id: _omp_thread_max
		IdentifierNode _omp_thread_max = nodeIdent(srcMethod, THREAD_MAX);

		// set $input
		type.setInputQualified(true);
		return nodeFactory.newVariableDeclarationNode(
				newSource(srcMethod, CivlcTokenConstant.DECLARATION), //
				_omp_thread_max, type);
	}

	/**
	 * Return {@link VariableDeclarationNode} representing:<br>
	 * <code>$range _omp_thread_range = {0 .. _omp_nthreads-1};</code>
	 * 
	 * @param srcMethod
	 *            Dummy {@link Source} information based on caller name
	 * @return See above
	 */
	private VariableDeclarationNode declOmpThreadRange(String srcMethod) {
		// type: $range
		TypeNode typeInt = nodeTypeRange(srcMethod);
		// id: _omp_thread_range
		IdentifierNode _omp_thread_range = nodeIdent(srcMethod, THREAD_RANGE);
		// init: {0 .. _omp_nthreads-1}
		InitializerNode init = nodeExprRange(srcMethod,
				/* lb */ nodeExprInt(srcMethod, 0),
				/* ub */ nodeFactory.newOperatorNode(
						newSource(srcMethod, CivlcTokenConstant.SUB),
						Operator.MINUS, nodeExprId(srcMethod, NTHREADS),
						nodeExprInt(srcMethod, 1)),
				/* step */ null);

		return nodeFactory.newVariableDeclarationNode(
				newSource(srcMethod, CivlcTokenConstant.DECLARATION), //
				_omp_thread_range, typeInt, init);
	}

	/**
	 * Process a list of {@link OmpLoopInfo}s to retrieve all involved loop
	 * variables, so that CIVL <code>$for</code> can declare them correctly in
	 * its loop initial expression.
	 * 
	 * @param srcMethod
	 *            Dummy {@link Source} information based on caller name
	 * @param loopInfos
	 *            A list of {@link OmpLoopInfo}s, each of which contains a
	 *            single loop variable.
	 * @return A list of {@link VariableDeclarationNode} representing
	 *         declarations of all involved loop variables.
	 */
	private List<VariableDeclarationNode> declVarsLoopInit(String srcMethod,
			List<OmpLoopInfo> loopInfos) {
		List<VariableDeclarationNode> loopVarDecls = new LinkedList<>();

		for (OmpLoopInfo loopInfo : loopInfos)
			loopVarDecls.add(nodeDeclVarInt(srcMethod, loopInfo.loopVarName));
		return loopVarDecls;
	}

	/**
	 * Process dummy declarations for private variables in thread-local scope.
	 * 
	 * @param srcMethod
	 *            Dummy {@link Source} information based on caller name
	 * @param varIds
	 *            a sequence of {@link IdentifierExpressionNode} representing a
	 *            list of variables specified by a single privatization clause
	 *            or directive.
	 * @param kind
	 *            the kind of the related privatization clause or directive.
	 * @return a non-empty list containing at least one non-<code>null</code>
	 *         list of {@link VariableDeclarationNode}s for dummy declarations
	 *         of private variables. A second optional list for temporary
	 *         declarations that shall be inserted before the OpenMP region.
	 */
	private List<List<VariableDeclarationNode>> declVarsPrivate(
			String srcMethod, SequenceNode<IdentifierExpressionNode> varIds,
			PrivateKind kind) {
		Source declSrc = newSource(srcMethod, CivlcTokenConstant.DECLARATION);
		List<List<VariableDeclarationNode>> privateVarDecls = new LinkedList<List<VariableDeclarationNode>>();
		VariableDeclarationNode actualVarDecl = null;
		IdentifierNode actualVarId = null;
		VariableDeclarationNode pvtVarDecl = null;
		IdentifierNode pvtVarId = null;
		TypeNode pvtVarType = null;

		// The first list is for private variable declarations,
		// which is required for all situations
		privateVarDecls.add(new LinkedList<VariableDeclarationNode>());
		// The second list for temporary declarations that
		// shall be inserted before the parallel region
		privateVarDecls.add(new LinkedList<VariableDeclarationNode>());
		// If there is no private/firstprivate/lastprivate/threadprivate,
		// or no actual variable are specified with them.
		if (varIds == null || varIds.numChildren() == 0)
			// The first list for private variables is empty.
			return privateVarDecls;
		// else at least one private variable is specified
		switch (kind) {
			case DEFAULT :
				for (ASTNode varId : varIds.children()) {
					// get actual declaration for private variables
					actualVarId = ((IdentifierExpressionNode) varId)
							.getIdentifier();
					actualVarDecl = (VariableDeclarationNode) ((Variable) actualVarId
							.getEntity()).getFirstDeclaration();
					// create dummy declaration for private variables
					pvtVarId = nodeIdent(srcMethod, actualVarId.name());
					pvtVarType = actualVarDecl.getTypeNode().copy();
					pvtVarDecl = nodeFactory.newVariableDeclarationNode(//
							declSrc, pvtVarId, pvtVarType);
					// ADD: private variable declarations with same names
					privateVarDecls.get(0).add(pvtVarDecl);
				}
				break;
			case FIRST :
				// ADD: local variable declarations for each firstprivate ones
				// with a temporary variable transferring the value from its
				// original variable to the newly declared local one.
				// E.g.,
				// ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ====
				// int n = n_val;
				// int *p = p_val;
				// #pragma omp parallel firstprivate(n, p)
				// { ..
				// }
				// ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ====
				// int n = n_val;
				// int* p = p_val;
				// int _omp_fstpvt_n = n; // get the value of the outer 'n'
				// int* _omp_fstpvt_p = p; // get the value of the outer 'p'
				// $parfor ( .. )
				// { ..
				// int n = _omp_fstpvt_n; // assign the outer 'n' to inner 'n'
				// int* p = _omp_fstpvt_p; // assign the outer 'p' to inner 'p'
				// ..
				// }
				// ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ====
				VariableDeclarationNode tmpVarDecl = null;
				IdentifierNode tmpVarId = null;
				TypeNode tmpVarType = null;
				String pvtVarName, tmpVarName;

				for (ASTNode varId : varIds.children()) {
					// get actual declaration for private variables
					actualVarId = ((IdentifierExpressionNode) varId)
							.getIdentifier();
					actualVarDecl = (VariableDeclarationNode) ((Variable) actualVarId
							.getEntity()).getFirstDeclaration();
					// create dummy declaration for private variables
					pvtVarName = actualVarId.name();
					tmpVarName = FIRSTPRIVATE_ + pvtVarName;
					pvtVarId = nodeIdent(srcMethod, pvtVarName);
					pvtVarType = actualVarDecl.getTypeNode().copy();
					pvtVarDecl = nodeFactory.newVariableDeclarationNode(//
							declSrc, pvtVarId, pvtVarType,
							nodeExprId(srcMethod, tmpVarName));
					// create dummy declaration for temporary variables
					tmpVarId = nodeIdent(srcMethod, tmpVarName);
					tmpVarType = pvtVarType.copy();
					tmpVarDecl = nodeFactory.newVariableDeclarationNode(//
							declSrc, tmpVarId, tmpVarType,
							nodeExprId(srcMethod, pvtVarName));
					// ADD: private variable declarations with same names
					privateVarDecls.get(INDEX_PVT_DECLS).add(pvtVarDecl);
					privateVarDecls.get(INDEX_TMP_DECLS).add(tmpVarDecl);
				}
				break;
			case LAST :
			case THREAD :
				assert false;
		}
		return privateVarDecls;
	}

	/**
	 * Extract lb, b, and incr values for OpenMP loop from the associated
	 * canonical {@link ForLoopNode} <br>
	 * <strong>PRE-CONDITION</strong>: the given node <code>forLoop</code> shall
	 * be a strict canonical <code>for</code> loop. (See OpenMP 5.0 Sec.
	 * 2.9.1)<br>
	 * <strong>POST-CONDITION</strong>: the lower bound (the first ) of the
	 * range triple is always less than the upper bound (the second). So, for
	 * decreasing loop variable, the range is from b to lb.
	 * 
	 * @param forLoop
	 *            A CIVL-AST node representing a canonical for loop.
	 * @return A triple of {@link ASTNode} representing a range's lower bound,
	 *         upper bound and step
	 */
	private OmpLoopInfo extractLoopInfo(ForLoopNode forLoop)
			throws SyntaxException {
		String srcMethod = SRC_INFO + ".extractLoopInfo";
		Source exprSrc = newSource(srcMethod, CivlcTokenConstant.EXPR);
		ExpressionNode one = nodeFactory.newIntegerConstantNode(exprSrc, "1");
		ForLoopInitializerNode initExpr = forLoop.getInitializer();
		ExpressionNode testExpr = forLoop.getCondition();
		ExpressionNode incrExpr = forLoop.getIncrementer();
		IdentifierNode var = null;
		ExpressionNode lb = null, b = null, incr = null;
		OperatorNode expr = null;
		ExpressionNode lhsExpr = null, rhsExpr = null;
		boolean isOpenBound = false;

		// GET: var (loop variable) and lb (the initial value of var)
		if (initExpr instanceof OperatorNode) {
			expr = (OperatorNode) initExpr;
			if (expr.getOperator() == Operator.ASSIGN) {
				// var = lb
				lhsExpr = expr.getArgument(0);
				rhsExpr = expr.getArgument(1);
				var = ((IdentifierExpressionNode) lhsExpr).getIdentifier();
				lb = rhsExpr;
			} // else non-canonical init-expr
		} else if (initExpr instanceof DeclarationListNode) {
			// type var = lb
			VariableDeclarationNode decl = (VariableDeclarationNode) initExpr
					.child(0);

			var = decl.getIdentifier();
			lb = (ExpressionNode) decl.getInitializer();
		}
		if (var == null || lb == null)
			throw new CIVLSyntaxException(
					"Non-canonical OpenMP loop init-expr.");

		// GET: b (the bound value of var)
		if (testExpr instanceof OperatorNode) {
			expr = (OperatorNode) testExpr;
			switch (expr.getOperator()) {
				case GT :
				case LT :
					isOpenBound = true;
				case LTE :
				case GTE :
				case NEQ :
					lhsExpr = expr.getArgument(0);
					rhsExpr = expr.getArgument(1);
					if (isSameVarEntity(lhsExpr, var))
						// var rel-op b
						b = rhsExpr;
					else if (isSameVarEntity(rhsExpr, var))
						// b rel-op var
						b = lhsExpr;
				default :
			}
		}
		if (b == null)
			throw new CIVLSyntaxException(
					"Non-canonical OpenMP loop test-expr.");

		// GET: incr (the step value of var)
		if (incrExpr instanceof OperatorNode) {
			expr = (OperatorNode) incrExpr;
			if (expr.getOperator() == Operator.ASSIGN) {
				// var = var + incr
				// var = incr + var
				// var = var - incr
				incrExpr = expr.getArgument(1);
				if (incrExpr instanceof OperatorNode)
					expr = (OperatorNode) incrExpr;
				else
					throw new CIVLSyntaxException(
							"Non-canonical OpenMP loop incr-expr.");
			}
			if (expr.getNumberOfArguments() == 2) {
				lhsExpr = expr.getArgument(0);
				rhsExpr = expr.getArgument(1);
				if (isSameVarEntity(lhsExpr, var))
					// var + incr | var - incr
					// var += incr | var -= incr
					incr = rhsExpr;
				else if (isSameVarEntity(rhsExpr, var))
					// incr + var
					incr = lhsExpr;
			} else if (expr.getNumberOfArguments() == 1)
				// ++va | var++ | --var | var--
				incr = one;
		}
		if (incr == null)
			throw new CIVLSyntaxException(
					"Non-canonical OpenMP loop incr-expr.");
		lb = lb.copy();
		b = b.copy();
		incr = incr.copy();
		// CREATE: triple<rangeLower, rangeUpper, rangeStep>
		switch (expr.getOperator()) {
			case PLUS : // var + incr | incr + var
			case PLUSEQ : // var += incr
			case PREINCREMENT : // ++var
			case POSTINCREMENT : // var++
				// open test bound b is decreased by 1
				if (isOpenBound)
					b = nodeFactory.newOperatorNode(b.getSource(),
							Operator.MINUS, b, one.copy());
				return new OmpLoopInfo(var.name(), new Triple<>(lb, b, incr));
			case MINUS : // var - incr
			case MINUSEQ : // var -= incr
			case PREDECREMENT : // --var
			case POSTDECREMENT : // var--
				// negate incr
				incr = nodeFactory.newOperatorNode(incr.getSource(),
						Operator.UNARYMINUS, incr);
				// open test bound b is increased by 1
				if (isOpenBound)
					b = nodeFactory.newOperatorNode(b.getSource(),
							Operator.PLUS, b, one.copy());
				// b < lb, so range should be [b, lb]
				return new OmpLoopInfo(var.name(), new Triple<>(b, lb, incr));
			default :
				throw new CIVLSyntaxException(
						"Non-canonical OpenMP loop incr-expr.");
		}
	}

	/**
	 * 
	 * @param srcMethod
	 * @param isParallel
	 * @param domainVarName
	 * @param loopVarDecls
	 * @param loopBodyItems
	 * @return
	 */
	private CivlForNode genCivlFor(String srcMethod, boolean isParallel,
			String domainVarName, List<VariableDeclarationNode> loopVarDecls,
			List<BlockItemNode> loopBodyItems) {
		// create CIVL loop var. decl.: int loopVar1, .. , loopVarX
		ForLoopInitializerNode loopInit = nodeFactory.newForLoopInitializerNode(
				newSource(srcMethod, CivlcTokenConstant.INITIALIZER_LIST),
				loopVarDecls);

		// if (isParallel): $parfor(int _omp_tid : _omp_domain) { .. }
		// else: $for(int loopVar1, .. loopVarX : _omp_loop_dist) { .. }
		return nodeFactory.newCivlForNode(
				newSource(srcMethod, CivlcTokenConstant.CIVLFOR), isParallel,
				(DeclarationListNode) loopInit,
				nodeExprId(srcMethod, domainVarName),
				nodeBlock(srcMethod, loopBodyItems), null);
	}

	/**
	 * Return <code>true</code> iff <code>sourceFile</code> indicating that the
	 * corresponding node is imported from library source files including: <br>
	 * *.cvh, *.h (except for stdio.h), civlc.cvl, concurrency.cvl, omp.cvl,
	 * pthread.cvl, stdio.cvl, string.cvl
	 * 
	 * @param sourceFileName
	 *            the name of a source file.
	 * @return see above.
	 */
	private boolean isImported(String sourceFileName) {
		return sourceFileName.endsWith(".cvh")
				|| sourceFileName.equals("civlc.cvl")
				|| sourceFileName.equals("concurrency.cvl")
				|| sourceFileName.equals("omp.cvl")
				|| sourceFileName.equals("pthread.cvl")
				|| sourceFileName.equals("stdio.cvl")
				|| sourceFileName.equals("string.cvl")
				|| (sourceFileName.endsWith(".h"));
	}

	/**
	 * Return <code>true</code> iff the {@link IdentifierNode} wrapped by
	 * <code>varExpr</code> refers to a same variable entity identified by
	 * <code>varId</code>.
	 * 
	 * @param varExpr
	 *            A variable expression holding a single variable
	 * @param varId
	 *            An identifier of a variable
	 * @return See above.
	 */
	private boolean isSameVarEntity(ExpressionNode varExpr,
			IdentifierNode varId) {
		return varExpr instanceof IdentifierExpressionNode
				&& ((IdentifierExpressionNode) varExpr).getIdentifier()
						.getEntity().equals(varId.getEntity());
	}

	/** @return {@link CompoundStatementNode} */
	private CompoundStatementNode nodeBlock(String srcMethod,
			BlockItemNode... blockItems) {
		return nodeBlock(srcMethod, Arrays.asList(blockItems));
	}

	/** @return {@link CompoundStatementNode} */
	private CompoundStatementNode nodeBlock(String srcMethod,
			List<BlockItemNode> blockItems) {
		return nodeFactory.newCompoundStatementNode(
				newSource(srcMethod, CivlcTokenConstant.COMPOUND_STATEMENT),
				blockItems);
	}

	/** @return {@link VariableDeclarationNode} with int type and no init */
	private VariableDeclarationNode nodeDeclVarInt(String srcMethod,
			String varName) {
		return nodeFactory.newVariableDeclarationNode(
				newSource(srcMethod, CivlcTokenConstant.DECLARATION),
				nodeIdent(srcMethod, varName), nodeTypeInt(srcMethod));
	}

	/** @return {@link ExpressionNode} for <code>&expr</code> */
	private ExpressionNode nodeExprAddrOf(String srcMethod,
			ExpressionNode expr) {
		return nodeFactory.newOperatorNode(
				newSource(srcMethod, CivlcTokenConstant.EXPR),
				Operator.ADDRESSOF, Arrays.asList(expr));
	}

	/** @return {@link FunctionCallNode} */
	private ExpressionNode nodeExprCall(String srcMethod, String funcName,
			ExpressionNode... argExprs) {
		return nodeFactory.newFunctionCallNode(
				newSource(srcMethod, CivlcTokenConstant.CALL),
				nodeExprId(srcMethod, funcName), Arrays.asList(argExprs), null);
	}

	/** @return {@link ExpressionNode}: <code>(type) expr</code> */
	private ExpressionNode nodeExprCast(String srcMethod, TypeNode type,
			ExpressionNode expr) {
		return nodeFactory.newCastNode(
				newSource(srcMethod, CivlcTokenConstant.CAST), type, expr);
	}

	/** @return {@link ExpressionNode}: <code>$here</code> */
	private ExpressionNode nodeExprHere(String srcMethod) {
		return nodeFactory
				.newHereNode(newSource(srcMethod, CivlcTokenConstant.HERE));
	}

	/** @return {@link IdentifierExpressionNode} */
	private ExpressionNode nodeExprId(String srcMethod, String idName) {
		IdentifierNode ident = nodeIdent(srcMethod, idName);

		return nodeFactory.newIdentifierExpressionNode(ident.getSource(),
				ident);
	}

	/** @return {@link IntegerConstantNode} */
	private ExpressionNode nodeExprInt(String srcMethod, int val) {
		return nodeFactory.newIntConstantNode(
				newSource(srcMethod, CivlcTokenConstant.INTEGER_CONSTANT), val);
	}

	/** @return {@link RegularRangeNode} */
	private ExpressionNode nodeExprRange(String srcMethod, ExpressionNode lo,
			ExpressionNode hi, ExpressionNode step) {
		if (step == null)
			return nodeFactory.newRegularRangeNode(
					newSource(srcMethod, CivlcTokenConstant.EXPR), lo, hi);
		else
			return nodeFactory.newRegularRangeNode(
					newSource(srcMethod, CivlcTokenConstant.EXPR), lo, hi,
					step);
	}

	/** @return {@link ExpressionNode} for omp reduction init val */
	private ExpressionNode nodeExprReductionInit(String srcMethod,
			OmpReductionOperator reductionOp, TypeNode type) {
		// TODO: an initial value shall comply with its bonding types
		switch (type.kind()) {
			case BASIC :
				switch (reductionOp) {
					case MAX : // TYPE.MIN_VALUE
						return nodeExprInt(srcMethod, Integer.MIN_VALUE);
					case MIN : // TYPE.MAX_VALUE
						return nodeExprInt(srcMethod, Integer.MAX_VALUE);
					case BAND : // ~0
						return nodeExprInt(srcMethod, ~0);
					case LAND : // true (or !0)
					case EQV : // true (or !0)
					case PROD : // 1
						return nodeExprInt(srcMethod, 1);
					case LOR : // false (or 0)
					case NEQ : // false (or 0)
					case SUM : // 0
					case MINUS :// 0
					case BOR : // 0
					case BXOR : // 0
						return nodeExprInt(srcMethod, 0);
					default :
						throw new CIVLUnimplementedFeatureException(
								"Unsupported OpenMP reduction operator: "
										+ reductionOp);
				}
			case ARRAY :
			case POINTER :
			default :
				throw new CIVLUnimplementedFeatureException("Unsupported type: "
						+ type + " for OpenMP reduction operator: "
						+ reductionOp);

		}
	}

	/** @return {@link IdentifierNode} */
	private IdentifierNode nodeIdent(String srcMethod, String idName) {
		return nodeFactory.newIdentifierNode(
				newSource(srcMethod, CivlcTokenConstant.IDENTIFIER), idName);
	}

	/** @return {@link StatementNode} for a function call */
	private StatementNode nodeStmtCall(String srcMethod, String funcName,
			ExpressionNode... argExprs) {
		return nodeFactory.newExpressionStatementNode(
				nodeExprCall(srcMethod, funcName, argExprs));
	}

	/**
	 * Returns a list of {@link BlockItemNode}s representing that the given
	 * <code>stmt</code> wrapped by lock/unlock with the lock named as
	 * <code>lockName</code> as follow:<br>
	 * <code>$read_and_write_set_update(team);</code><br>
	 * <code>$yield();</code><br>
	 * <code>$omp_helper_signal_wait(&signalName, 0);</code><br>
	 * <code>$check_data_race(team);</code><br>
	 * <code>$omp_helper_signal_send(&signalName, 1);</code><br>
	 * <code>{stmt;} // e.g., x += 1;</code><br>
	 * <code>$read_and_write_set_update(team);</code><br>
	 * <code>$yield();</code><br>
	 * <code>$omp_helper_signal_send(&signalName, 0);</code><br>
	 * <code>$check_data_race(team);</code><br>
	 * 
	 * @param srcMethod
	 * @param stmt
	 *            the {@link StatementNode} shall be wrapped.
	 * @param signalName
	 *            the CIVL's Omp helper signal name associated with given
	 *            <code>stmt</code>
	 * @return see above.
	 * @throws SyntaxException
	 */
	private List<BlockItemNode> nodeStmtsSignalProtected(String srcMethod,
			StatementNode stmt, String signalName) throws SyntaxException {
		LinkedList<BlockItemNode> block = new LinkedList<>();

		// ADD: $read_and_write_set_update(team);
		// ADD: $yield();
		block.addAll(callYield(srcMethod));
		// ADD: $omp_helper_signal_wait(&signalName, 0);
		block.add(callSignalWait(srcMethod, signalName, 0));
		// ADD: $check_data_race(team);
		block.add(callCheckDataRace(srcMethod));
		// ADD: $omp_helper_signal_send(&signalName, 1);
		block.add(callSignalSend(srcMethod, signalName, 1));
		// TRANS: stmt
		searchOmpInstructions(stmt);
		stmt.remove();
		// ADD: stmt
		block.add(stmt);
		// ADD: $read_and_write_set_update(team);
		// ADD: $yield();
		block.addAll(callYield(srcMethod));
		// ADD: $omp_helper_signal_send(&signalName, 0);
		block.add(callSignalSend(srcMethod, signalName, 0));
		// ADD: $check_data_race(team);
		block.add(callCheckDataRace(srcMethod));
		return block;
	}

	/** @return CIVL <code>$domain(dim)</code> type node: */
	private TypeNode nodeTypeDom(String srcMethod, int dim) {
		if (dim > 0) // $domain(dim)
			return nodeFactory.newDomainTypeNode(
					newSource(srcMethod, CivlcTokenConstant.DOMAIN),
					nodeExprInt(srcMethod, dim));
		else // $domain
			return nodeFactory.newDomainTypeNode(
					newSource(srcMethod, CivlcTokenConstant.DOMAIN));
	}

	/** @return <code>int</code> {@link TypeNode} */
	private TypeNode nodeTypeInt(String srcMethod) {
		return nodeFactory.newBasicTypeNode(
				newSource(srcMethod, CivlcTokenConstant.INT),
				BasicTypeKind.INT);
	}

	/** @return named_type_def {@link TypeNode} w/ given <code>name</code> */
	private TypeNode nodeTypeNamed(String srcMethod, String name) {
		return nodeFactory.newTypedefNameNode(nodeIdent(srcMethod, name), null);
	}

	/** @return CIVL <code>$range</code> {@link TypeNode} */
	private TypeNode nodeTypeRange(String srcMethod) {
		return nodeFactory.newRangeTypeNode(
				newSource(srcMethod, CivlcTokenConstant.RANGE));
	}

	private void procOmpBarrierNode(OmpSyncNode ompBarrierNode) {
		String srcMethod = SRC_INFO + ".procOmpBarrierNode";
		List<BlockItemNode> ompBlockItems = new LinkedList<>();

		// TRANS: $omp_barrier(team);
		ompBlockItems.add(callOmpBarrier(srcMethod));
		replaceOmpNode(srcMethod, ompBarrierNode, ompBlockItems);
	}

	/**
	 * yield => check => wait(0) => send(1) => block => send(0)
	 * 
	 * @param ompCriticalNode
	 * @throws SyntaxException
	 */
	private void procOmpCriticalNode(OmpSyncNode ompCriticalNode)
			throws SyntaxException {
		String srcMethod = SRC_INFO + ".procOmpCriticalNode";
		List<BlockItemNode> ompBlockItems = new LinkedList<>();
		IdentifierNode criticalNameId = ompCriticalNode.criticalName();
		StatementNode criticalBlock = ompCriticalNode.statementNode();
		String criticalName = null;

		if (criticalNameId == null) // Unspecified critical name
			criticalName = CRITICAL_ + NAME_CRITICAL_UNSPEC;
		else
			criticalName = CRITICAL_ + criticalNameId.name();
		// Store encountered critical names for declaring them
		if (!criticalNames.contains(criticalName))
			criticalNames.add(criticalName);

		// ADD: $read_and_write_set_update(team);
		// ADD: $yield();
		// ADD: $_omp_helper_signal_wait(&critical_X, 0);
		// ADD: $check_data_race(team);
		// ADD: $_omp_helper_signal_send(&critical_X, 1);
		// TRANS: criticalBlock
		// ADD: $read_and_write_set_update(team);
		// ADD: $yield();
		// ADD: $_omp_helper_signal_send(&critical_X, 0);
		// ADD: $check_data_race(team);
		ompBlockItems.addAll(nodeStmtsSignalProtected(srcMethod, criticalBlock,
				criticalName));

		// TRANS: 'ompCriticalNode'
		replaceOmpNode(srcMethod, ompCriticalNode, ompBlockItems);
	}

	/**
	 * Perform transformation on OpenMP Parallel Region (see:
	 * https://vsl.cis.udel.edu/trac/civl/wiki/OpenMPTransformation#Translatingfor
	 * )
	 * 
	 * @param ompForNode
	 * @throws SyntaxException
	 */
	private void procOmpForNode(OmpForNode ompForNode) throws SyntaxException {
		// TODO: lastprivate clause
		String srcMethod = SRC_INFO + ".procOmpForNode";
		List<BlockItemNode> ompBlockItems = new LinkedList<>();
		// PROC: collapse
		int collapse = ompForNode.collapse();
		int numLoopRanges = 0;
		ArrayList<OmpLoopInfo> loopInfos = new ArrayList<>();
		ForLoopNode curLoop = (ForLoopNode) ompForNode.statementNode();
		OmpLoopInfo curLoopInfo = extractLoopInfo(curLoop);

		// PRE: Record the current OpenMP region info
		ompRgn.push(new OmpRegion(OmpRgnKind.LOOP));
		loopInfos.add(curLoopInfo);
		for (int i = 1; i < collapse; i++) {
			curLoop = (ForLoopNode) curLoop.getBody();
			curLoopInfo = extractLoopInfo(curLoop);
			loopInfos.add(curLoopInfo);
		}
		bindingLoopInfosRecords.push(loopInfos);

		// ADD: $read_set_pop();
		// ADD: $write_set_pop();
		ompBlockItems.addAll(callRWSetPop(srcMethod));
		// ADD: $range _omp_rangeX = {lo .. hi, step};
		// * note that X is [1 .. numLoopRanges]
		for (OmpLoopInfo info : loopInfos)
			ompBlockItems
					.add(declOmpRange(srcMethod, ++numLoopRanges, info.range));
		assert numLoopRanges == loopInfos.size();
		// ADD: $domain(1) _omp_loop_domain = ($domain){_omp_range1, ...};
		ompBlockItems.add(declOmpDomain(srcMethod, numLoopRanges));
		// ADD: $domain(collapse) _omp_loop_dist = ($domain(collapse))
		// $omp_arrive_loop(_omp_team, loop_id++, _omp_loop_domain, STRATEGY);
		ompBlockItems.add(declOmpDistLoop(srcMethod, collapse));

		// PROC: shared, private and firstprivate variabe list.
		// NOTE: an item can appear in both firstprivate and last private.
		List<List<VariableDeclarationNode>> pvtDeclsList = declVarsPrivate(
				srcMethod, ompForNode.privateList(), PrivateKind.DEFAULT);
		List<List<VariableDeclarationNode>> fstpvtDeclsList = declVarsPrivate(
				srcMethod, ompForNode.firstprivateList(), PrivateKind.FIRST);
		List<List<BlockItemNode>> rdcItemsList = transOmpReduction(
				ompForNode.reductionList());

		// ADD: dummy decl. for pvt. var.
		ompBlockItems.addAll(pvtDeclsList.get(INDEX_PVT_DECLS));
		// ADD: temp. decl. for holding val. of pvt.1st var.
		ompBlockItems.addAll(fstpvtDeclsList.get(INDEX_TMP_DECLS));
		// ADD: dummy decl. for pvt.1st var.
		ompBlockItems.addAll(fstpvtDeclsList.get(INDEX_PVT_DECLS));

		// ADD: $read_set_push();
		// ADD: $write_set_push();
		ompBlockItems.addAll(callRWSetPush(srcMethod));
		// dummy decl. and init. for reduction items
		ompBlockItems.addAll(rdcItemsList.get(INDEX_RDC_INITS));

		// TRANS: OMP loop Region -> CIVL $for construct
		List<BlockItemNode> cvlForBodyItems = new LinkedList<>();
		StatementNode loopBody = null;

		int numOrderedLoops = ompForNode.ordered();
		// IF ordered, declare order counter variables in global scope

		if (numOrderedLoops > 1) // ordered(X), unsupported
			throw new CIVLUnimplementedFeatureException(
					"'ordered' clause with parameters for "
							+ "'doacross loop nest' feature (in OpenMP 4.5)");
		else if (numOrderedLoops == 1) {
			// ADD: int /loop_var/_next = /loop_var/ + /incr_expr/;
			cvlForBodyItems.addAll(declOmpLoopVarNext(srcMethod, loopInfos));
			// global decl:
			// $omp_helper_signal ordered_0 =
			// $omp_helper_signal_create(init_expr);
			declOmpOrderedSignals(srcMethod, loopInfos);
		}
		// else // NO ordered clause
		// process and transfer all other children
		searchOmpInstructions(curLoop);
		// get processed body
		loopBody = curLoop.getBody();
		// transfer into civl $for loop body
		if (loopBody instanceof CompoundStatementNode)
			for (ASTNode child : loopBody.children()) {
				child.remove();
				cvlForBodyItems.add((BlockItemNode) child);
			}
		else {
			loopBody.remove();
			cvlForBodyItems.add(loopBody);
		}
		ompBlockItems.add(genCivlFor(//
				srcMethod, false, /* domName */ LOOP_DIST,
				/* loopVarDecls */ declVarsLoopInit(srcMethod, loopInfos),
				/* loopBodyItems */ cvlForBodyItems));
		// dummy decl. and init. for reduction items
		ompBlockItems.addAll(rdcItemsList.get(INDEX_RDC_COMBS));
		if (!ompForNode.nowait())
			// ADD: $omp_barrier(team);
			ompBlockItems.add(callOmpBarrier(srcMethod));

		// TRANS: replace parallel region with transformed block
		replaceOmpNode(srcMethod, ompForNode, ompBlockItems);
		bindingLoopInfosRecords.pop();
		ompRgn.pop();
	}

	/**
	 * 
	 * @param ompMasterNode
	 * @throws SyntaxException
	 */
	private void procOmpMasterNode(OmpSyncNode ompMasterNode)
			throws SyntaxException {
		String srcMethod = SRC_INFO + ".procOmpMasterNode";
		List<BlockItemNode> ompBlockItems = new LinkedList<>();

		// PRE: Record the current OpenMP region info
		ompRgn.push(new OmpRegion(OmpRgnKind.MASTER));
		// TRANS: recursively transforms child nodes.
		searchOmpInstructions(ompMasterNode);

		// TRANS: adds the condition ' _omp_tid == 0 ' to
		// the associated block so that exactly one thread executes it.
		StatementNode masterBody = ompMasterNode.statementNode();
		// cond_expr: _omp_tid == 0
		ExpressionNode condExpr = nodeFactory.newOperatorNode(
				newSource(srcMethod, CivlcTokenConstant.EXPR), Operator.EQUALS,
				nodeExprId(srcMethod, TID),
				nodeExprInt(srcMethod, ID_MASTER_THREAD));

		masterBody.remove();
		ompBlockItems.add(nodeFactory.newIfNode(
				newSource(srcMethod, CivlcTokenConstant.STATEMENT), condExpr,
				masterBody));
		// TRANS: replace master construct with transformed block
		replaceOmpNode(srcMethod, ompMasterNode, ompBlockItems);
		ompRgn.pop();
	}

	private void procOmpOrderedNode(OmpSyncNode ompOrderedNode)
			throws SyntaxException {
		String srcMethod = SRC_INFO + ".procOmpOrderedNode";
		List<BlockItemNode> ompBlockItems = new LinkedList<>();

		// PRE: Record the current OpenMP region info
		ompRgn.push(new OmpRegion(OmpRgnKind.MASTER));
		// TRANS: recursively transforms child nodes.
		searchOmpInstructions(ompOrderedNode);

		StatementNode orderedBody = ompOrderedNode.statementNode();

		orderedBody.remove();
		if (orderConcurrent)
			// Omp Std. 2.9.2 Worksharing loop: Pg. 104 Ln. 10-12
			// If an order(concurrent) clause is present, ..
			// the iterations may be executed in any order,
			// including concurrently
			// Thus, pragma 'omp ordered' is omitted.
			// ADD: the block associated with ordered clause
			ompBlockItems.add(orderedBody);
		else {
			ompBlockItems.addAll(transOmpOrdered(srcMethod,
					bindingLoopInfosRecords.peek(), orderedBody));
		}
		// TRANS: replace master construct with transformed block
		replaceOmpNode(srcMethod, ompOrderedNode, ompBlockItems);
		ompRgn.pop();
	}

	/**
	 * 
	 * @param srcMethod
	 * @param loopInfos
	 * @param orderedBody
	 * @return
	 */
	private List<BlockItemNode> transOmpOrdered(String srcMethod,
			List<OmpLoopInfo> loopInfos, StatementNode orderedBody) {
		Source exprSrc = newSource(srcMethod, CivlcTokenConstant.EXPR);
		Source stmtSrc = newSource(srcMethod, CivlcTokenConstant.STATEMENT);
		List<BlockItemNode> orderedBodyItems = new LinkedList<>();
		int numOrderedCtr = loopInfos.size();
		int orderId = ctrOmpOrdered - numOrderedCtr;
		int infoIdx = 0;
		OmpLoopInfo info = loopInfos.get(infoIdx);
		ExpressionNode loopVarExpr = nodeExprId(srcMethod, info.loopVarName);
		ExpressionNode loopVarNextExpr = nodeExprId(srcMethod,
				info.loopVarName + _NEXT);
		ExpressionNode orderedCtrExpr = nodeExprId(srcMethod,
				ORDERED + orderId);
		StatementNode wait = nodeStmtCall(srcMethod, OMP_HELPER_SIGNAL_WAIT,
				nodeExprAddrOf(srcMethod, orderedCtrExpr.copy()), loopVarExpr);
		StatementNode sendNext = nodeStmtCall(srcMethod, OMP_HELPER_SIGNAL_SEND,
				nodeExprAddrOf(srcMethod, orderedCtrExpr), loopVarNextExpr);
		StatementNode sendInit = null;
		ExpressionNode condExpr = null;

		// ADD: $read_and_write_set_update(team);
		// ADD: $yield();
		orderedBodyItems.addAll(callYield(srcMethod));
		// ADD: $omp_helper_signal_wait(_omp_order_counter0, /loop_var/);
		orderedBodyItems.add(wait);
		for (infoIdx = 1; infoIdx < numOrderedCtr; infoIdx++) {
			orderId++;
			info = loopInfos.get(infoIdx);
			loopVarNextExpr = nodeExprId(srcMethod, info.loopVarName + _NEXT);
			loopVarExpr = nodeExprId(srcMethod, info.loopVarName);
			orderedCtrExpr = nodeExprId(srcMethod, ORDERED + orderId);
			wait = nodeStmtCall(srcMethod, OMP_HELPER_SIGNAL_WAIT,
					nodeExprAddrOf(srcMethod, orderedCtrExpr.copy()),
					loopVarExpr);
			// ADD: $omp_helper_signal_wait(_omp_order_counterX, /loop_var/);
			orderedBodyItems.add(wait);
			// GEN: $omp_helper_signal_send calls
			// $omp_helper_signal_send(_omp_order_counterX, init);
			sendInit = nodeStmtCall(srcMethod, OMP_HELPER_SIGNAL_SEND,
					nodeExprAddrOf(srcMethod, orderedCtrExpr.copy()),
					info.range.first.copy());
			// { $send(_omp_order_counter/X/, /inner_loop_var_init/);
			// $send(_omp_order_counter/X-1/, /outer_loop_var_next/);
			// }
			sendInit = nodeBlock(srcMethod, sendInit, sendNext);
			// $send(_omp_order_counter/X/, /inner_loop_var_next/);
			sendNext = nodeStmtCall(srcMethod, OMP_HELPER_SIGNAL_SEND,
					nodeExprAddrOf(srcMethod, orderedCtrExpr), loopVarNextExpr);
			// /inner_loop_var_next/ < /inner_loop_var_bound/
			condExpr = nodeFactory.newOperatorNode(exprSrc, Operator.LT,
					loopVarNextExpr.copy(), info.range.second.copy());
			// if (/inner_loop_var_next/ < /inner_loop_var_bound/)
			// $send(_omp_order_counter/X/, /inner_loop_var_next/);
			// else {
			// $send(_omp_order_counter/X/, /inner_loop_var_init/);
			// $send(_omp_order_counter/X-1/, /outer_loop_var_next/);
			// }
			// NOTE:
			// if the next iteration logical number 'j_next' of an inner
			// loop variable 'j' is less than its bound,
			// THEN: signal_send(signal_/X/, j_next) to issue the
			// execution of current iteration's sequentially next one.
			// ELSE: reset the current inner loop variable by
			// signal_send(signal_/X/, j_init) and increase the outer
			// loop variable 'i' by signal_send(signal_/X-1/, i_next).
			sendNext = nodeFactory.newIfNode(stmtSrc, condExpr, sendNext,
					sendInit);
		}
		orderedBodyItems.add(callCheckDataRace(srcMethod));
		orderedBodyItems.add(orderedBody);
		// ADD: $read_and_write_set_update(team);
		// ADD: $yield();
		orderedBodyItems.addAll(callYield(srcMethod));
		orderedBodyItems.add(sendNext);
		orderedBodyItems.add(callCheckDataRace(srcMethod));
		return orderedBodyItems;
	}

	/**
	 * Perform transformation on OpenMP Parallel Region (see:
	 * https://vsl.cis.udel.edu/trac/civl/wiki/Next-GenOpenMPTransformation#Translatingparallel
	 * )
	 * 
	 * @param ompParallelNode
	 * @throws SyntaxException
	 */
	private void procOmpParallelNode(OmpParallelNode ompParallelNode)
			throws SyntaxException {
		// TODO: num_threads clause
		String srcMethod = SRC_INFO + ".procOmpParallelNode";
		Source declSrc = newSource(srcMethod, CivlcTokenConstant.DECLARATION);
		List<BlockItemNode> ompBlockItems = new LinkedList<>();

		// PRE: Record the current OpenMP region info
		ompRgn.push(new OmpRegion(OmpRgnKind.PARALLEL));
//		levelParallel += 1;

		// PROC: _omp_num_threads
		ExpressionNode _omp_num_threads = ompParallelNode.numThreads();
		// PROC: shared, private and firstprivate variabe list.
		// NOTE: an item can appear in both firstprivate and last private.
		List<List<VariableDeclarationNode>> pvtDeclsList = declVarsPrivate(
				srcMethod, ompParallelNode.privateList(), PrivateKind.DEFAULT);
		List<List<VariableDeclarationNode>> fstpvtDeclsList = declVarsPrivate(
				srcMethod, ompParallelNode.firstprivateList(),
				PrivateKind.FIRST);
		List<List<BlockItemNode>> rdcItemsList = transOmpReduction(
				ompParallelNode.reductionList());
		Boolean hasExplicitNumThreadsClause = _omp_num_threads != null;

		if (hasExplicitNumThreadsClause) 
			_omp_num_threads.remove();
		else // If absent
			_omp_num_threads = nodeExprId(srcMethod, _OMP_ + "num_threads");
		ompBlockItems.add(elaborateExpression(_omp_num_threads).copy());
		// ADD: int _omp_nthreads = 1+$choose_int(_omp_num_threads);
		ompBlockItems.add(declOmpNthreads(srcMethod, _omp_num_threads,
				hasExplicitNumThreadsClause));
		// ADD: temporary variable declarations for firstprivate variables
		ompBlockItems.addAll(fstpvtDeclsList.get(INDEX_TMP_DECLS));
		// ADD: $range _omp_thread_range = {0 .. _omp_nthreads-1};
		ompBlockItems.add(declOmpThreadRange(srcMethod));
		// ADD: $domain(1) dom = ($domain){thread_range};
		ompBlockItems.add(declOmpDomain(srcMethod, 0)); // 0 for parallel region
		// ADD: $omp_gteam gteam = $omp_gteam_create($here, nthreads);
		ompBlockItems.add(declOmpGteam(srcMethod));

		// TRANS: OMP Parallel Region -> CIVL $parfor construct
		List<BlockItemNode> parForBodyItems = new LinkedList<>();
		StatementNode bodyStatement = null;

		// ADD to $parfor:
		// $local_start();
		parForBodyItems.add(nodeStmtCall(srcMethod, LOCAL_START));
		// $omp_team _omp_team = $omp_team_create($here, _omp_gteam, _omp_tid);
		parForBodyItems.add(declOmpTeam(srcMethod));
		// $read_set_push();
		// $write_set_push();
		parForBodyItems.addAll(callRWSetPush(srcMethod));
		// reduction items: dummy decl. and init.
		parForBodyItems.addAll(rdcItemsList.get(INDEX_RDC_INITS));
		// private items: dummy decl. for pvt. var.
		parForBodyItems.addAll(pvtDeclsList.get(INDEX_PVT_DECLS));
		// first private items: dummy decl. and init. for pvt. var.
		parForBodyItems.addAll(fstpvtDeclsList.get(INDEX_PVT_DECLS));
		// DEPRECATED: transformation for shared variables, due to R/W set
		// process and transfer all other children
		searchOmpInstructions(ompParallelNode);
		bodyStatement = ompParallelNode.statementNode();
		bodyStatement.remove();
		parForBodyItems.add(bodyStatement);
		// TODO: reduction impl. in civl-omp.cvl should push-red-pop
		// dummy decl. and init. for reduction items
		parForBodyItems.addAll(rdcItemsList.get(INDEX_RDC_COMBS));
		// $omp_barrier(team);
		parForBodyItems.add(callOmpBarrier(srcMethod));
		// $read_set_pop();
		// $write_set_pop();
		parForBodyItems.addAll(callRWSetPop(srcMethod));
		// $omp_team_destroy(team);
		parForBodyItems.add(nodeStmtCall(srcMethod, OMP_TEAM_DESTROY,
				nodeExprId(srcMethod, TEAM)));
		// $local_end();
		parForBodyItems.add(nodeStmtCall(srcMethod, LOCAL_END));

		// ADD: creates of OpenMP helper signals used by this transformer
		ompBlockItems.addAll(signalCreates);
		// ADD: $parfor (int _omp_tid : _omp_dom) { .. }
		ompBlockItems.add(genCivlFor(srcMethod, true, /* domName */ DOM,
				/* loopVarDecls */ Arrays.asList(//
						nodeFactory.newVariableDeclarationNode(declSrc,
								nodeIdent(srcMethod, TID),
								nodeTypeInt(srcMethod))),
				parForBodyItems));
		// ADD: $omp_gteam_destroy(gteam);
		ompBlockItems.add(nodeStmtCall(srcMethod, OMP_GTEAM_DESTROY,
				nodeExprId(srcMethod, GTEAM)));
		// TRANS: replace parallel region with transformed block
		replaceOmpNode(srcMethod, ompParallelNode, ompBlockItems);
		ompRgn.pop();
	}

	private void procOmpSectionsNode(OmpWorksharingNode ompSectionsNode)
			throws SyntaxException {
		String srcMethod = SRC_INFO + ".procOmpSectionsNode";
		List<BlockItemNode> ompBlockItems = new LinkedList<>();

		// PRE: Record the current OpenMP region info
		ompRgn.push(new OmpRegion(OmpRgnKind.SECTIONS));

		// get each section block in the current sections construct
		List<StatementNode> sectionBlocks = new LinkedList<>();
		// PROC: shared, private and firstprivate variabe list.
		// NOTE: an item can appear in both firstprivate and last private.
		List<List<VariableDeclarationNode>> pvtDeclsList = declVarsPrivate(
				srcMethod, ompSectionsNode.privateList(), PrivateKind.DEFAULT);
		List<List<VariableDeclarationNode>> fstpvtDeclsList = declVarsPrivate(
				srcMethod, ompSectionsNode.firstprivateList(),
				PrivateKind.FIRST);
		// PROC: analysis the number of section constructs
		StatementNode sectionsItems = ompSectionsNode.statementNode();
		int numItems = sectionsItems.numChildren();
		int idx = 0;
		ASTNode item = null;

		// Check the first item
		item = sectionsItems.child(idx++);
		if (item instanceof OmpWorksharingNode)
			// explicit section block
			sectionBlocks.add(((OmpWorksharingNode) item).statementNode());
		else
			// implicit section block
			sectionBlocks.add((StatementNode) item);
		while (idx < numItems) {
			item = sectionsItems.child(idx++);
			if (item instanceof OmpWorksharingNode)
				sectionBlocks.add(((OmpWorksharingNode) item).statementNode());
			else
				throw new CIVLSyntaxException(
						"Non-section item in OpenMP sections construct.");
		}
		// ADD: $read_set_pop();
		// ADD: $write_set_pop();
		ompBlockItems.addAll(callRWSetPop(srcMethod));
		// ADD: $domain(1) _omp_sections_dist = ($domain(1))
		// $omp_arrive_sections(_omp_team, section_id++, numSection);
		ompBlockItems.add(declOmpDistSections(srcMethod, sectionBlocks.size()));
		// ADD: dummy decl. for pvt. var.
		ompBlockItems.addAll(pvtDeclsList.get(INDEX_PVT_DECLS));
		// ADD: temp. decl. for holding val. of pvt.1st var.
		ompBlockItems.addAll(fstpvtDeclsList.get(INDEX_TMP_DECLS));
		// ADD: dummy decl. for pvt.1st var.
		ompBlockItems.addAll(fstpvtDeclsList.get(INDEX_PVT_DECLS));
		// ADD: $read_set_push();
		// ADD: $write_set_push();
		ompBlockItems.addAll(callRWSetPush(srcMethod));

		// TRANS: OMP sections Region -> CIVL $for construct
		// ADD:
		// $for(int _omp_sid : _omp_sections_dist) {
		// if (_omp_sid == 1) { BLOCK1 }
		// ..
		// }
		// all section blocks are recursively processed in 'transOmpSection'
		ompBlockItems.add(genCivlFor(//
				srcMethod, false, /* domName */ SECTIONS_DIST,
				/* loopVarDecls */ Arrays.asList(//
						nodeDeclVarInt(srcMethod, SID)),
				/* loopBodyItems */ transOmpSection(sectionBlocks)));
		if (!ompSectionsNode.nowait())
			// ADD: $omp_barrier(team);
			ompBlockItems.add(callOmpBarrier(srcMethod));
		// TRANS: replace sections region with transformed block
		replaceOmpNode(srcMethod, ompSectionsNode, ompBlockItems);
		ompRgn.pop();
	}

	private void procOmpSingleNode(OmpWorksharingNode ompSingleNode)
			throws SyntaxException {
		String srcMethod = SRC_INFO + ".procOmpSingleNode";
		List<BlockItemNode> ompBlockItems = new LinkedList<>();

		// PRE: Record the current OpenMP region info
		ompRgn.push(new OmpRegion(OmpRgnKind.SINGLE));

		List<List<VariableDeclarationNode>> pvtDeclsList = declVarsPrivate(
				srcMethod, ompSingleNode.privateList(), PrivateKind.DEFAULT);
		List<List<VariableDeclarationNode>> fstpvtDeclsList = declVarsPrivate(
				srcMethod, ompSingleNode.firstprivateList(), PrivateKind.FIRST);

		// TODO: copypvt clause, copypvt
		assert ompSingleNode.copyprivateList() == null;
		assert ompSingleNode.copyinList() == null;

		// ADD: $read_set_pop();
		// ADD: $write_set_pop();
		ompBlockItems.addAll(callRWSetPop(srcMethod));
		// ADD: int _omp_single_dist = $omp_arrive_single(team, single_id++);
		ompBlockItems.add(declOmpDistSingle(srcMethod));
		// ADD: dummy decl. for pvt. var.
		ompBlockItems.addAll(pvtDeclsList.get(INDEX_PVT_DECLS));
		// ADD: temp. decl. for holding val. of pvt.1st var.
		ompBlockItems.addAll(fstpvtDeclsList.get(INDEX_TMP_DECLS));
		// ADD: dummy decl. for pvt.1st var.
		ompBlockItems.addAll(fstpvtDeclsList.get(INDEX_PVT_DECLS));
		// ADD: $read_set_push();
		// ADD: $write_set_push();
		ompBlockItems.addAll(callRWSetPush(srcMethod));
		// TRANS: recursively transforms child nodes.
		searchOmpInstructions(ompSingleNode);

		// TRANS: adds the condition ' _omp_tid == _omp_single_dist ' to
		// the associated block so that exactly one thread executes it.
		StatementNode singleBody = ompSingleNode.statementNode();
		// cond_expr: _omp_tid == _omp_single_dist
		ExpressionNode condExpr = nodeFactory.newOperatorNode(
				newSource(srcMethod, CivlcTokenConstant.EXPR), Operator.EQUALS,
				nodeExprId(srcMethod, TID), nodeExprId(srcMethod, SINGLE_DIST));

		singleBody.remove();
		ompBlockItems.add(nodeFactory.newIfNode(
				newSource(srcMethod, CivlcTokenConstant.STATEMENT), condExpr,
				singleBody));
		if (!ompSingleNode.nowait())
			// ADD: $omp_barrier(team);
			ompBlockItems.add(callOmpBarrier(srcMethod));
		// TRANS: replace single construct with transformed block
		replaceOmpNode(srcMethod, ompSingleNode, ompBlockItems);
		ompRgn.pop();
	}

	private void recognizeOmpFunctionCalls(FunctionCallNode ompFunctionCallNode)
			throws SyntaxException {
		String srcMethod = SRC_INFO + ".recognizeOmpFunctionCalls: ";
		ExpressionNode functionExpr = ompFunctionCallNode.getFunction();
		ExpressionNode transformedFunctionCall = null;

		if (functionExpr instanceof IdentifierExpressionNode) {
			boolean isSyncFunc = false;
			String funcName = ((IdentifierExpressionNode) functionExpr)
					.getIdentifier().name();

			switch (funcName) {
				case OMP_GET_MAX_THREADS :
					transformedFunctionCall = nodeExprId(
							srcMethod + OMP_GET_MAX_THREADS, THREAD_MAX);
					break;
				case OMP_GET_NUM_PROCS :
					transformedFunctionCall = nodeExprInt(
							srcMethod + OMP_GET_NUM_PROCS, 1);
					break;
				case OMP_GET_NUM_THREADS :
					transformedFunctionCall = nodeExprId(
							srcMethod + OMP_GET_MAX_THREADS, NTHREADS);
					break;
				case OMP_GET_THREAD_NUM :
					transformedFunctionCall = nodeExprId(
							srcMethod + OMP_GET_MAX_THREADS, TID);
					break;
				case OMP_SET_NUM_THREADS :
					transformedFunctionCall = nodeFactory.newOperatorNode(
							newSource(srcMethod, CivlcTokenConstant.EXPR),
							Operator.ASSIGN, nodeExprId(srcMethod, NUM_THREADS),
							ompFunctionCallNode.getArgument(0).copy());
					break;
				// The following four are directly implemented in omp.cvl
				// case OMP_INIT_LOCK :
				// case OMP_DESTROY_LOCK :
				// case OMP_DESTROY_NEST_LOCK :
				// case OMP_DESTROY_NEST_LOCK :
				// The following six OpenMP lock function is translated as:
				// 1. add '$' as a prefix
				// 2. add a second argument, which is '_omp_tid'
				case OMP_SET_LOCK :
				case OMP_SET_NEST_LOCK :
				case OMP_UNSET_LOCK :
				case OMP_UNSET_NEST_LOCK :
					isSyncFunc = true;
				case OMP_TEST_LOCK :
				case OMP_TEST_NEST_LOCK :
					transformedFunctionCall = nodeExprCall(srcMethod,
							SIGN_DOLLAR + funcName,
							ompFunctionCallNode.getArgument(0).copy(),
							nodeExprId(srcMethod, TID));
					break;
				default :
					// do nothing
			}
			if (transformedFunctionCall != null)
				ompFunctionCallNode.parent().setChild(
						ompFunctionCallNode.childIndex(),
						transformedFunctionCall);
			if (isSyncFunc)
				transOmpSyncLockRoutine(transformedFunctionCall);
			// Check args of function call statement
			searchOmpInstructions(ompFunctionCallNode);
		}
	}

	private void recognizeOmpInstructions(OmpNode ompNode)
			throws SyntaxException {
		switch (ompNode.ompNodeKind()) {
			case EXECUTABLE :
				OmpExecutableNode ompExecNode = (OmpExecutableNode) ompNode;

				switch (ompExecNode.ompExecutableKind()) {
					case PARALLEL :
						procOmpParallelNode((OmpParallelNode) ompExecNode);
						return;
					case SIMD :
						assert false;
					case SYNCHRONIZATION :
						OmpSyncNode ompSyncNode = (OmpSyncNode) ompExecNode;

						switch (ompSyncNode.ompSyncNodeKind()) {
							case MASTER :
								procOmpMasterNode(ompSyncNode);
								return;
							case CRITICAL :
								procOmpCriticalNode(ompSyncNode);
								return;
							case BARRIER :
								procOmpBarrierNode(ompSyncNode);
								return;
							case FLUSH : // Omitted
								ompSyncNode.remove();
								return;
							case ORDERED :
								procOmpOrderedNode(ompSyncNode);
								return;
							case OMPATOMIC :
								procOmpAtomicNode((OmpAtomicNode) ompSyncNode);
								return;
						}
					case WORKSHARING :
						OmpWorksharingNode ompWorkSNode = (OmpWorksharingNode) ompExecNode;

						switch (ompWorkSNode.ompWorkshareNodeKind()) {
							case SECTIONS :
								procOmpSectionsNode(ompWorkSNode);
								return;
							// case SECTION : processed in 'procOmpSectionsNode'
							case SINGLE :
								procOmpSingleNode(ompWorkSNode);
								return;
							case FOR :
								procOmpForNode((OmpForNode) ompWorkSNode);
								return;
							default :
								assert false;
						}
				}
			case DECLARATIVE :
				OmpDeclarativeNode ompDeclNode = (OmpDeclarativeNode) ompNode;

				assert false;
				switch (ompDeclNode.ompDeclarativeNodeKind()) {
					case REDUCTION :
					case SIMD :
					case TARGET :
					case THREADPRIVATE :
				}
		}
		// DFS: recursively search for OmpNode among successors of this OmpNode
		searchOmpInstructions(ompNode);
	}

	private void procOmpAtomicNode(OmpAtomicNode ompAtomicNode)
			throws SyntaxException {
		String srcMethod = SRC_INFO + ".procOmpAtomicNode";
		StatementNode atomicStmt = ompAtomicNode.statementNode();
		ExpressionNode atomicExpr[] = new ExpressionNode[2];

		assert atomicExpr[1] == null;
		if (atomicStmt instanceof ExpressionStatementNode)
			atomicExpr[0] = ((ExpressionStatementNode) atomicStmt)
					.getExpression();
		else if (atomicStmt instanceof CompoundStatementNode
				&& atomicStmt.numChildren() <= 2) {
			CompoundStatementNode block = ((CompoundStatementNode) atomicStmt);

			for (int i = 0; i < block.numChildren(); i++) {
				ASTNode stmt = block.child(i);

				if (stmt instanceof ExpressionStatementNode)
					atomicExpr[i] = ((ExpressionStatementNode) stmt)
							.getExpression();
			}
		}
		switch (ompAtomicNode.atomicClause()) {
			case READ :
				if (!verifyOmpAtomicRead(atomicExpr[0]))
					throw new CIVLSyntaxException(
							"Illegal pattern for the body statement associated "
									+ "with OpenMP atomic READ construct: "
									+ atomicStmt.prettyRepresentation());
				break;
			case WRITE :
				if (!verifyOmpAtomicWrite(atomicExpr[0]))
					throw new CIVLSyntaxException(
							"Illegal pattern for the body statement associated "
									+ "with OpenMP atomic WRITE construct: "
									+ atomicStmt.prettyRepresentation());
				break;
			case CAPTURE :
				if (!verifyOmpAtomicCapture(atomicExpr))
					throw new CIVLSyntaxException(
							"Illegal pattern for the body statement associated "
									+ "with OpenMP atomic CAPTURE construct: "
									+ atomicStmt.prettyRepresentation());
				break;
			case UPDATE :
				if (!verifyOmpAtomicUpdate(atomicExpr[0]))
					throw new CIVLSyntaxException(
							"Illegal pattern for the body statement associated "
									+ "with OpenMP atomic UPDATE construct: "
									+ atomicStmt.prettyRepresentation());
				break;
		}
		hasAtomicConstruct = true;
		// All atomic section are protected by a single signal struct
		// It gives a stronger condition that enforces exclusive access
		// between atomic regions, which may access different storage locations.
		// (see: OpenMP std. 4.5: 2.17.7 atomic Construct: Description [Pg.240])
		replaceOmpNode(srcMethod, ompAtomicNode,
				nodeStmtsSignalProtected(srcMethod, atomicStmt, ATOMIC_));

	}

	/**
	 * Replace the processed {@link OmpNode} <code>ompNode</code> with a list of
	 * {@link BlockItemNode} transformed.
	 * 
	 * @param srcMethod
	 *            Dummy {@link Source} information based on caller name
	 * @param ompNode
	 *            The processed {@link OmpNode}
	 * @param blockItems
	 *            A list of {@link BlockItemNode} representing behaviors
	 *            peformed by the processed OpenMP pragma and its associated
	 *            block constructs.
	 */
	private void replaceOmpNode(String srcMethod, OmpNode ompNode,
			List<BlockItemNode> blockItems) {
		ASTNode parent = ompNode.parent();
		int indexOld = ompNode.childIndex();

		parent.setChild(indexOld, nodeBlock(srcMethod, blockItems));
	}

	/**
	 * Perform recursive DFS for searching {@link OmpNode}s among successors of
	 * the given {@link ASTNode} <code>root</code>. If an {@link OmpNode} is
	 * found, then it is processed and this function is applied for continuing
	 * exploring its successors
	 * 
	 * @param root
	 *            a {@link ASTNode}, if it is an {@link OmpNode} then it must be
	 *            processed by <code>this</code> transformer.
	 * @throws SyntaxException
	 */
	private void searchOmpInstructions(ASTNode root) throws SyntaxException {
		// DFS: recursively search for OmpNode
		for (ASTNode child : root.children()) {
			if (child instanceof OmpNode) // recognize and process OmpNode
				recognizeOmpInstructions((OmpNode) child);
			else if (child instanceof FunctionCallNode)
				recognizeOmpFunctionCalls((FunctionCallNode) child);
			else if (child != null) // Explore non-OmpNode
				searchOmpInstructions(child);
		}
	}

	private List<List<BlockItemNode>> transOmpReduction(
			SequenceNode<OmpReductionNode> reductionClauses) {
		String srcMethod = SRC_INFO + ".transOmpReduction";
		Source ptrSrc = newSource(srcMethod, CivlcTokenConstant.POINTER);
		Source declSrc = newSource(srcMethod, CivlcTokenConstant.DECLARATION);
		Source exprSrc = newSource(srcMethod, CivlcTokenConstant.EXPR);
		List<BlockItemNode> varPtrDecls = new LinkedList<>();
		List<BlockItemNode> varTmpDecls = new LinkedList<>();
		List<BlockItemNode> varRdcInits = new LinkedList<>();
		List<BlockItemNode> initialItems = new LinkedList<>();
		List<BlockItemNode> combineItems = new LinkedList<>();
		Set<String> rcVarDeclName = new HashSet<>();
		OmpSymbolReductionNode symbRc = null;
		OmpReductionOperator rcOp = null;

		if (reductionClauses == null)
			return Arrays.asList(initialItems, combineItems);
		for (OmpReductionNode rc : reductionClauses) {
			// Trans each reduction clause
			symbRc = (OmpSymbolReductionNode) rc;
			rcOp = symbRc.operator();

			for (IdentifierExpressionNode rcVar : symbRc.variables()) {
				String vName = rcVar.getIdentifier().name();
				String vpName = REDUCTION_ + ctrOmpReductionItem + "_" + vName;
				TypeNode vType = ((VariableDeclarationNode) ( //
				(Variable) rcVar.getIdentifier().getEntity())
						.getFirstDeclaration()).getTypeNode().copy();
				TypeNode vpType = nodeFactory.newPointerTypeNode( //
						ptrSrc, vType.copy());
				ExpressionNode vpInit = nodeFactory.newOperatorNode(exprSrc,
						Operator.ADDRESSOF, nodeExprId(srcMethod, vName));
				// type *_omp_reduction_x_var = &var;
				VariableDeclarationNode vpDecl = nodeFactory
						.newVariableDeclarationNode(declSrc, //
								nodeIdent(srcMethod, vpName), vpType, vpInit);
				// type var;
				VariableDeclarationNode vDecl = nodeFactory
						.newVariableDeclarationNode(declSrc, //
								nodeIdent(srcMethod, vName), vType);
				// var = [omp_priv];
				ExpressionNode varInitExpr = nodeFactory.newOperatorNode(
						exprSrc, Operator.ASSIGN, nodeExprId(srcMethod, vName),
						nodeExprReductionInit(srcMethod, rcOp, vType));
				ExpressionStatementNode varInitStmt = nodeFactory
						.newExpressionStatementNode(varInitExpr);
				// $omp_rdc(OPERATOR, VAR_PTR, VAR_TMP);
				StatementNode combineItem = nodeStmtCall(srcMethod,
						OMP_REDUCTION_COMBINE,
						nodeExprInt(srcMethod, rcOp.civlOp()),
						nodeExprId(srcMethod, vpName), nodeExprAddrOf(srcMethod,
								nodeExprId(srcMethod, vName)));

				varPtrDecls.add(vpDecl);
				if (!rcVarDeclName.contains(vName)) {
					varTmpDecls.add(vDecl);
					rcVarDeclName.add(vName);
				} else
					throw new CIVLSyntaxException("Reduction item identifier "
							+ vName + "shall appear only once in data clause.");
				varRdcInits.add(varInitStmt);
				combineItems.add(combineItem);
			}
			ctrOmpReductionItem++;
		}
		initialItems.addAll(varPtrDecls);
		initialItems.addAll(varTmpDecls);
		initialItems.addAll(varRdcInits);
		return Arrays.asList(initialItems, combineItems);
	}

	/**
	 * Return a list of {@link BlockItemNode} transformed from each of
	 * structured-blocks<br>
	 * (see:
	 * https://vsl.cis.udel.edu/trac/civl/wiki/Next-GenOpenMPTransformation )
	 * 
	 * @param sectionBlocks
	 *            a list of {@link StatementNode} representing each associated
	 *            section block
	 * @return see above
	 * @throws SyntaxException
	 */
	private List<BlockItemNode> transOmpSection(
			List<StatementNode> sectionBlocks) throws SyntaxException {
		// sections {
		// [section] { BLOCK0 }
		// [section { BLCOK1 }] ..
		// }
		/* **** **** is transformed to **** **** */
		// $for(int _omp_sid : _omp_sec_dist) {
		// if (_omp_sid==0) { BLOCK0 }
		// if (_omp_sid==1) { BLCOK1 } ..
		// }
		String srcMethod = SRC_INFO + ".transOmpSection";
		Source exprSrc = newSource(srcMethod, CivlcTokenConstant.EXPR);
		Source ifSrc = newSource(srcMethod, CivlcTokenConstant.STATEMENT);
		List<BlockItemNode> sections = new LinkedList<>();
		ExpressionNode condExpr = null;
		int sectionId = 0;

		for (StatementNode block : sectionBlocks) {
			// _omp_sid == N
			condExpr = nodeFactory.newOperatorNode(exprSrc, Operator.EQUALS,
					nodeExprId(srcMethod, SID),
					nodeExprInt(srcMethod, sectionId++));
			searchOmpInstructions(block);
			block.remove();
			// if (_omp_sid == N) { BLOCK_N }
			sections.add(nodeFactory.newIfNode(ifSrc, condExpr, block));
		}
		return sections;
	}

	/**
	 * Transform a OpenMP lock set/unset routine (which requires
	 * synchronization) by wrapping it with CIVL's helper function calls. (e.g.,
	 * <br>
	 * <code>$omp_set_lock(.., ..);</code><br>
	 * is trans formed to <br>
	 * <code>{</code><br>
	 * <code>  $read_and_write_set_update(team);</code><br>
	 * <code>  $yield();</code><br>
	 * <code>  $omp_set_lock(.., ..);</code><br>
	 * <code>  $check_data_race(_omp_team);</code><br>
	 * <code>}</code><br>
	 * 
	 * @param ompLockFuncCallExpr
	 *            an {@link ExpressionNode} for an OpenMP lock routine, which
	 *            requires synchronization. (either a set or an unset routine)
	 */
	private void transOmpSyncLockRoutine(ExpressionNode ompLockFuncCallExpr) {
		String srcMethod = SRC_INFO + ".transOmpSyncLockRoutine: ";
		List<BlockItemNode> transformedCall = new LinkedList<>();
		StatementNode funcCall = (StatementNode) ompLockFuncCallExpr.parent();

		transformedCall.addAll(callYield(srcMethod));
		transformedCall.add(funcCall.copy());
		transformedCall.add(callCheckDataRace(srcMethod));
		funcCall.parent().setChild(funcCall.childIndex(),
				nodeBlock(srcMethod, transformedCall));
	}

	/**
	 * Returns <code>true</code> iff the storage location designated by both
	 * <code>expr0</code> and <code>expr1</code> is a same location. (i.e.,
	 * <code>expr1</code> accesses the exact storage location (entity)
	 * designated by <code>expr0</code>); else returns <code>false</code>
	 * 
	 * @param expr0
	 *            an expression designating a storage location
	 * @param expr1
	 *            an expression designating a storage location
	 * @return
	 */
	private boolean verifyExprSameEntity(ExpressionNode expr0,
			ExpressionNode expr1) {
		boolean areBothLValues = expr0.isLvalue() && expr1.isLvalue();
		// TODO: Implement the verification as described:
		// TBD: verifies that for both expr0 and expr1
		// 1. they designate a exactly same storage location (i.e. Entity).
		Entity entity0 = null;
		Entity entity1 = null;

		// Both exor0 and expr1 are l-values.
		if (!areBothLValues)
			return false;
		if (expr0 instanceof IdentifierExpressionNode)
			entity0 = ((IdentifierExpressionNode) expr0).getIdentifier()
					.getEntity();
		if (expr1 instanceof IdentifierExpressionNode)
			entity1 = ((IdentifierExpressionNode) expr1).getIdentifier()
					.getEntity();
		if (entity0 != null && entity1 != null)
			return entity0.equals(entity1);
		return true;
	}

	/**
	 * Returns <code>true</code> iff the storage location designated by
	 * <code>expr0</code> is isolated from <code>expr1</code> (i.e.,
	 * <code>expr1</code> does not access the storage location (entity)
	 * designated by <code>expr0</code>); else returns <code>false</code>
	 * <p>
	 * <strong>NOTE</strong>: this function is <strong>NOT implemented
	 * yet</strong>, and will always return <code>true</code>
	 * </p>
	 * 
	 * @param expr0
	 *            an l-value expression designating a storage location
	 * @param expr1
	 *            any valid expression
	 * @return see above.
	 */
	private boolean verifyExprSeparatedEntity(ExpressionNode expr0,
			ExpressionNode expr1) {
		// TODO: Implement the verification as described:
		// TBD: verifies that for all OpenMP atomic constructs:
		// 1. Neither of 'v' and 'expr' (as applicable) may access the storage
		// location designated by 'x'. (OpenMP Std. 4.5.0: Sec. 2.17.7)
		// 2. Neither of 'x' and 'expr' (as applicable) may access the storage
		// location designated by 'v'. (OpenMP Std. 4.5.0: Sec. 2.17.7)
		// *. any two atomic constructs associated with a same storage location
		// shall have a same type on their 'x'. (OpenMP Eg. 4.5.0: Sec. 6.5)
		return expr0.isLvalue();
	}

	/**
	 * <p>
	 * Verify the pattern of <code>atomicCaptureExprs</code>, which is/are
	 * associated with an OpenMP <code>atomic</code> construct with
	 * <code>update</code> clause.
	 * </p>
	 * <p>
	 * The pattern shall be one of following:<br>
	 * <code>v = update_expr;</code><br>
	 * <code>v = x; update_expr;</code><br>
	 * <code>update_expr; v = x;</code><br>
	 * 1. <code>update_expr</code> can be verified as a 'legal' update
	 * expression. (i.e. <code>true</code> is returned as result by
	 * {@link #verifyOmpAtomicUpdate(update_expr)})<br>
	 * 2. Both <code>v</code> and <code>x</code> (including ones involved in
	 * <code>update_expr</code>) are l-values<br>
	 * 3. They designate separated storage locations (i.e., <code>v</code> and
	 * <code>x</code> shall not access a same storage location.) <br>
	 * 4. The storage location designated by either of <code>x</code> and
	 * <code>v</code> shall not be accessed by <code>expr</code> involved in
	 * <code>update_expr</code> (if it exists).
	 * </p>
	 * 
	 * @param atomicCaptureExprs
	 *            expression(s) associated with an OpenMP atomic capture
	 *            construct
	 * @return <code>true</code> iff the pattern of
	 *         <code>atomicCaptureExprs</code> is legal.
	 */
	private boolean verifyOmpAtomicCapture(
			ExpressionNode... atomicCaptureExprs) {
		// v = update_expr;
		// {v = x; update_expr;}
		// {update_expr; v = x;}
		ExpressionNode expr0 = atomicCaptureExprs[0];
		ExpressionNode expr1 = atomicCaptureExprs[1];
		boolean isSingleExpr = expr1 == null;

		// 1. a single expr
		if (isSingleExpr) {
			List<ExpressionNode> args = analyzeExprAssignScalar(expr0);
			boolean isScalarAssignmentExpr = args.size() == 2;

			if (isScalarAssignmentExpr) {
				ExpressionNode v = args.get(0);
				ExpressionNode update = args.get(1);

				return verifyOmpAtomicUpdate(update) && v.isLvalue()
						&& verifyExprSeparatedEntity(v, update);
			} else
				return false;
		}

		// 2. a block with two expr
		List<ExpressionNode> args0 = analyzeExprAssignScalar(expr0);
		List<ExpressionNode> args1 = analyzeExprAssignScalar(expr1);
		boolean isScalarAssignmentExpr0 = args0.size() == 2;
		boolean isScalarAssignmentExpr1 = args1.size() == 2;
		boolean isBinaryUpdate = false;
		ExpressionNode v = null;
		ExpressionNode x = null; // x in v = x
		ExpressionNode xLHS = null; // LHS x in update_expr
		ExpressionNode xRHS = null; // RHS x in update_Expr (may be null)
		ExpressionNode expr = null; // expr in update_expr (may be null)

		// Get 'v', 'x', and 'expr' (if it exists).
		if (isScalarAssignmentExpr0) {
			v = args0.get(0);
			x = args0.get(1);
			isBinaryUpdate = args1.size() == 3;
			if (isBinaryUpdate) {
				xLHS = args1.get(0);
				xRHS = args1.get(1);
				expr = args1.get(2);
			} else
				xLHS = args1.get(0);
		} else if (isScalarAssignmentExpr1) {
			v = args1.get(0);
			x = args1.get(1);
			isBinaryUpdate = args0.size() == 3;
			if (isBinaryUpdate) {
				xLHS = args0.get(0);
				xRHS = args0.get(1);
				expr = args0.get(2);
			} else
				xLHS = args1.get(0);
		} else // illegal: at least one scalar assignment
			return false;
		// Verify
		if (isBinaryUpdate)
			return v.isLvalue() && x.isLvalue() //
					&& verifyExprSameEntity(x, xLHS)
					&& verifyExprSameEntity(xLHS, xRHS)
					&& verifyExprSeparatedEntity(v, x)
					&& verifyExprSeparatedEntity(v, expr)
					&& verifyExprSeparatedEntity(x, expr);
		else
			return v.isLvalue() && x.isLvalue() //
					&& verifyExprSameEntity(x, xLHS)
					&& verifyExprSeparatedEntity(v, x);
	}

	/**
	 * <p>
	 * Verify the pattern of <code>atomicReadExpr</code>, which is associated
	 * with an OpenMP <code>atomic</code> construct with <code>read</code>
	 * clause.
	 * </p>
	 * <p>
	 * The pattern shall be exactly: <code>v = x;</code><br>
	 * 1. Both <code>v</code> and <code>x</code> are l-values. <br>
	 * 2. They designate separated storage locations (i.e., <code>v</code> and
	 * <code>x</code> shall not access a same storage location.)
	 * </p>
	 * 
	 * @param atomicReadExpr
	 *            the expression associated with an OpenMP atomic read construct
	 * @return <code>true</code> iff the pattern of <code>atomicReadExpr</code>
	 *         is legal.
	 */
	private boolean verifyOmpAtomicRead(ExpressionNode atomicReadExpr) {
		// v = x;
		List<ExpressionNode> args = analyzeExprAssignScalar(atomicReadExpr);
		boolean isScalarAssignmentExpr = args.size() == 2;

		if (isScalarAssignmentExpr) {// args = {v, x}
			ExpressionNode v = args.get(0);
			ExpressionNode x = args.get(1);

			return v.isLvalue() && x.isLvalue()
					&& verifyExprSeparatedEntity(x, v);
		}
		return false;
	}

	/**
	 * <p>
	 * Verify the pattern of <code>atomicUpdateExpr</code>, which is associated
	 * with an OpenMP <code>atomic</code> construct with <code>update</code>
	 * clause.
	 * </p>
	 * <p>
	 * The pattern shall be one of following:<br>
	 * <code>x++;</code><br>
	 * <code>x--;</code><br>
	 * <code>++x;</code><br>
	 * <code>--x;</code><br>
	 * <code>x bin-op = expr;</code><br>
	 * <code>x = x bin-op expr;</code><br>
	 * <code>x = expr bin-op x;</code><br>
	 * 1. <code>x</code> is a l-value<br>
	 * 2. The storage location designated by <code>x</code> shall not be
	 * accessed by <code>expr</code> (if it exists).
	 * </p>
	 * 
	 * @param atomicUpdateExpr
	 *            the expression associated with an OpenMP atomic update
	 *            construct
	 * @return <code>true</code> iff the pattern of
	 *         <code>atomicUpdateExpr</code> is legal.
	 */
	private boolean verifyOmpAtomicUpdate(ExpressionNode atomicUpdateExpr) {
		// x++
		// x--
		// ++x
		// --x
		// x bin-op = expr
		// x = x binop expr
		// x = expr binop x
		List<ExpressionNode> args = analyzeExprAssignScalar(atomicUpdateExpr);
		boolean isUnaryScalarUpdate = args.size() == 1;
		boolean isBinaryScalarUpdate = args.size() == 3;

		if (isUnaryScalarUpdate) // args = {x}
			return args.get(0).isLvalue();
		else if (isBinaryScalarUpdate) {
			// args = {x, x, expr} or {x, x.copy, expr}
			ExpressionNode xLHS = args.get(0);
			ExpressionNode xRHS = args.get(1);

			return xLHS.isLvalue() && xRHS.isLvalue()
					&& verifyExprSameEntity(xLHS, xRHS)
					&& verifyExprSeparatedEntity(xLHS, args.get(2));
		}
		return false;
	}

	/**
	 * <p>
	 * Verify the pattern of <code>atomicWriteExpr</code>, which is associated
	 * with an OpenMP <code>atomic</code> construct with <code>write</code>
	 * clause.
	 * </p>
	 * <p>
	 * The pattern shall be exactly: <code>x = expr;</code><br>
	 * <code>x</code> is a l-value and the storage location designated by
	 * <code>x</code> shall not be accessed by <code>expr</code>.
	 * </p>
	 * 
	 * @param atomicWriteExpr
	 *            the expression associated with an OpenMP atomic write
	 *            construct
	 * @return <code>true</code> iff the pattern of <code>atomicWriteExpr</code>
	 *         is legal.
	 */
	private boolean verifyOmpAtomicWrite(ExpressionNode atomicWriteExpr) {
		// x = expr;
		List<ExpressionNode> args = analyzeExprAssignScalar(atomicWriteExpr);
		boolean isScalarAssignmentExpr = args.size() == 2;

		if (isScalarAssignmentExpr) {// args = {x, expr}
			ExpressionNode x = args.get(0);

			return x.isLvalue() && verifyExprSeparatedEntity(x, args.get(1));
		}
		return false;
	}

	// Public functions or interfaces

	/**
	 * Transform an AST of a OpenMP program in C into an equivalent AST of
	 * CIVL-C program.<br>
	 * 
	 * @param oldAst
	 *            The AST of the original OpenMP program in C.
	 * @return An AST of CIVL-C program equivalent to the original OpenMP
	 *         program.
	 * @throws SyntaxException
	 */
	@Override
	public AST transform(AST oldAst) throws SyntaxException {
		assert super.astFactory == oldAst.getASTFactory();
		assert super.nodeFactory == astFactory.getNodeFactory();
		// Check the inclusion of CIVL's OpenMP Implementation file.
		if (!super.hasHeader(oldAst, CIVLConstants.CIVL_OMP_IMP))
			return oldAst;
		root = oldAst.getRootNode();
		oldAst.release();

		String srcMethod = SRC_INFO;
		// A list holding all processed block items for building new AST.
		List<BlockItemNode> newItems = new LinkedList<>();
		List<BlockItemNode> oldItems = new LinkedList<>();
		List<BlockItemNode> importedItems = new LinkedList<>();
		List<BlockItemNode> declaredItems = new LinkedList<>();
		List<String> ioVarNames = new LinkedList<>();
		String srcFile = null;

		// Search and Transform all OpenMP Nodes
		searchOmpInstructions((ASTNode) root);
		reduceDuplicateNode(root, PREDICATE_BARRIER_AND_FLUSH);
		for (BlockItemNode item : root) {
			if (item == null)
				continue;
			item.remove();
			srcFile = item.getSource().getFirstToken().getSourceFile()
					.getName();
			if (srcFile.equals("stdio.h")) {
				if (item.nodeKind() == NodeKind.VARIABLE_DECLARATION)
					oldItems.add(item);
				else
					importedItems.add(item);
			} else if (isImported(srcFile))
				// Extract items imported from library files.
				importedItems.add(item);
			else if (isRelatedAssumptionNode(item, ioVarNames))
				// Extract assumptions related with input/output var. decl.
				declaredItems.add(item);
			else if (item.nodeKind() == NodeKind.VARIABLE_DECLARATION) {
				VariableDeclarationNode varDecl = (VariableDeclarationNode) item;
				TypeNode varType = varDecl.getTypeNode();

				if (varType.isInputQualified() || varType.isOutputQualified()) {
					// Extract input/output var. decl.
					ioVarNames.add(varDecl.getName());
					declaredItems.add(item);
				} else
					oldItems.add(item);
			} else
				oldItems.add(item);
		}

		/* **** **** **** **** Build the new AST **** **** **** **** */
		// ADD: Nodes from header files
		newItems.addAll(importedItems);
		// ADD: Nodes of input/output var. dec. and their assumptions
		newItems.addAll(declaredItems);
		// ADD: $input int _omp_thread_max
		newItems.add(declOmpThreadMax(srcMethod));
		// ADD: generated global variables for verification
		newItems.addAll(globalVarDecls);
		// ADD: decls for OpenMP critical variables
		for (String signalNameCritical : criticalNames)
			newItems.add(declOmpHelperSignal(srcMethod, signalNameCritical,
					nodeExprInt(srcMethod, 0)));
		if (hasAtomicConstruct) {
			newItems.add(declOmpHelperSignal(srcMethod, ATOMIC_,
					nodeExprInt(srcMethod, 0)));
		}
		// ADD: int omp_num_threads = _omp_thread_max;
		newItems.add(declOmpNumThreads(srcMethod));
		// ADD: transformed Civl AST nodes from the old AST.
		newItems.addAll(oldItems);

		// CREATE: a new AST from the old one by transforming all OpenMP nodes
		SequenceNode<BlockItemNode> newRoot = nodeFactory
				.newSequenceNode(root.getSource(), "Omp2CivlProgram", newItems);
		AST newAst = astFactory.newAST(newRoot, oldAst.getSourceFiles(),
				oldAst.isWholeProgram());
		completeSources(newRoot);
		return newAst;
	}
}

class OmpRegion {
	protected enum OmpRgnKind {
		PARALLEL, // parallel
		SECTIONS, SECTION, SINGLE, WORKSHARE, // workshare
		FOR, SIMD, SIMD_DECL, LOOP, // loop
		BARRIER, CRITICAL, ATOMIC, MASTER, ORDERED, // sync
	}

	OmpRgnKind ompRgns[];

	OmpRegion(OmpRgnKind... OmpRegions) {
		assert OmpRegions.length > 0;
		this.ompRgns = OmpRegions;
	}

	OmpRgnKind[] getOmpRegions() {
		return this.ompRgns;
	}
}

class OmpLoopInfo {
	String loopVarName;
	Triple<ExpressionNode, ExpressionNode, ExpressionNode> range;

	OmpLoopInfo(String varName,
			Triple<ExpressionNode, ExpressionNode, ExpressionNode> range) {
		this.loopVarName = varName;
		this.range = range;
	}
}
