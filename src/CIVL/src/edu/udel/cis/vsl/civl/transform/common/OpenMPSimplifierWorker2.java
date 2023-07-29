package edu.udel.cis.vsl.civl.transform.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignExprIF;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignExprIF.AssignExprKind;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignStoreExprIF;
import edu.udel.cis.vsl.abc.analysis.pointsTo.common.SimplePointsToAnalysis;
import edu.udel.cis.vsl.abc.ast.IF.AST;
import edu.udel.cis.vsl.abc.ast.IF.ASTFactory;
import edu.udel.cis.vsl.abc.ast.entity.IF.Entity;
import edu.udel.cis.vsl.abc.ast.entity.IF.Function;
import edu.udel.cis.vsl.abc.ast.entity.IF.Variable;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.AttributeKey;
import edu.udel.cis.vsl.abc.ast.node.IF.IdentifierNode;
import edu.udel.cis.vsl.abc.ast.node.IF.SequenceNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.FunctionDefinitionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.VariableDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.FunctionCallNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.IdentifierExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.IntegerConstantNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode.Operator;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpExecutableNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpExecutableNode.OmpExecutableKind;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpForNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpParallelNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpReductionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpReductionNode.OmpReductionOperator;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpSimdNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpSymbolReductionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpSyncNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpSyncNode.OmpSyncNodeKind;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpWorksharingNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpWorksharingNode.OmpWorksharingNodeKind;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.BlockItemNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.DeclarationListNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.ForLoopInitializerNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.ForLoopNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.StatementNode;
import edu.udel.cis.vsl.abc.token.IF.Source;
import edu.udel.cis.vsl.abc.token.IF.SyntaxException;
import edu.udel.cis.vsl.civl.config.IF.CIVLConfiguration;
import edu.udel.cis.vsl.civl.model.IF.CIVLException;
import edu.udel.cis.vsl.civl.model.IF.CIVLUnimplementedFeatureException;
import edu.udel.cis.vsl.civl.model.common.ABC_CIVLSource;
import edu.udel.cis.vsl.civl.transform.IF.OpenMPSimplifier;
import edu.udel.cis.vsl.civl.transform.analysisIF.ArrayReferenceDependencyAnalyzer;
import edu.udel.cis.vsl.civl.transform.analysisIF.ReadWriteDataStructures.RWSet;
import edu.udel.cis.vsl.civl.transform.analysisIF.ReadWriteDataStructures.RWSetBaseElement;
import edu.udel.cis.vsl.civl.transform.analysisIF.ReadWriteDataStructures.RWSetElement;
import edu.udel.cis.vsl.civl.transform.analysisIF.ReadWriteDataStructures.RWSetElement.RWSetElementKind;
import edu.udel.cis.vsl.civl.transform.analysisIF.SimpleReadWriteAnalyzer;
import edu.udel.cis.vsl.civl.transform.analysisIF.SimpleReadWriteAnalyzer.SimpleFullSetException;

/**
 * This transformer analyzes OpenMP constructs and converts them to simpler,
 * i.e., less concurrent, instances of constructs.
 * 
 * This transform operates in two phases:
 * 
 * 1) Analyze OpenMP workshares to determine those that are provably
 * thread-independent, i.e., execution of workshares in parallel is guaranteed
 * to compute the same result.
 * 
 * 2) Transform OpenMP constructs based on the analysis results.
 * 
 * TBD: a) support nowait clauses b) support collapse clauses (confirm whether
 * collapse uses variables or the first "k" row indices) c) what is the
 * semantics of a parallel region with no pragma, i.e., do we have to reason
 * about its independence to remove the parallel pragma d) intra-iteration
 * dependences, e.g., x[i] = x[i] + a; e) critical, barrier, master, single and
 * other workshares f) calling sensitive parallel workshare nesting, i.e.,
 * caller has parallel pragma, callee has workshare g) semantics of nowait for
 * that continues to method return h) treatment of omp_ calls, i.e., should we
 * preserve the parallelism since the calls likely depend on it i) detect
 * non-escaping heap data from within a omp pragma context, e.g.,
 * fig4.98-threadprivate.c j) default private/shared when there are explicit
 * shared/private clauses that don't mention the var
 * 
 * 
 * @author dwyer
 * 
 */
public class OpenMPSimplifierWorker2 extends BaseWorker {

	// TBD: clean this up
	private AttributeKey dependenceKey;

	// Visitor identifies scalars through their "defining" declaration
	// private Set<Entity> writeVars;
	// private Set<Entity> readVars;
	// private Set<OperatorNode> writeArrayRefs;
	// private Set<OperatorNode> readArrayRefs;

	// private Set<Entity> sharedWrites;
	// private Set<Entity> sharedReads;
	// private Set<OperatorNode> sharedArrayWrites;
	// private Set<OperatorNode> sharedArrayReads;

	private Set<Variable> writeVars;
	private Set<Variable> readVars;
	private Set<RWSetElement> writeArrayRefs;
	private Set<RWSetElement> readArrayRefs;

	private Set<Variable> sharedWrites;
	private Set<Variable> sharedReads;
	private Set<RWSetElement> sharedArrayWrites;
	private Set<RWSetElement> sharedArrayReads;

	private Set<Entity> ompMethods;
	private Set<FunctionDefinitionNode> ompMethodDefinitions;

	/**
	 * A stack keeps track of locally declared entities during the traverse and
	 * analysis of an OMP parallel block (including nested ones).
	 */
	private Stack<Set<Entity>> locallyDeclaredEntities;

	private boolean allIndependent;

	private List<Entity> privateIDs;
	private List<Entity> loopPrivateIDs;

	private boolean debug = false;
	private CIVLConfiguration config;

	private SimpleReadWriteAnalyzer readWriteAnalyzer = null;

	private AST ast;

	private Function currentFunciton;

	public OpenMPSimplifierWorker2(ASTFactory astFactory,
			CIVLConfiguration config) {
		super(OpenMPSimplifier.LONG_NAME, astFactory);
		this.identifierPrefix = "$omp_sim_";
		this.config = config;
	}

	@Override
	public AST transform(AST unit) throws SyntaxException {
		if (config.ompNoSimplify())
			return unit;

		SequenceNode<BlockItemNode> rootNode = unit.getRootNode();

		assert this.astFactory == unit.getASTFactory();
		assert this.nodeFactory == astFactory.getNodeFactory();
		ast = unit;

		// OpenMPParallelRegions ompRegions = new
		// OpenMPParallelRegions(rootNode);

		/*
		 * TBD: We want a proper inter-procedural analysis. This is more of a
		 * stop-gap.
		 * 
		 * There are two challenges: 1) Detect orphaning "omp parallel"
		 * statements - since they should not be simplified without considering
		 * whether the methods they call are themselves simplified 2) Detecting
		 * orphaned "omp" statements - since they should be analyzed for
		 * simplification even though they might not be lexically nested within
		 * an "omp parallel"
		 * 
		 * We collect the set of methods that contain an omp construct,
		 * recording their entities to detect calls to such methods for case 1)
		 * and recording their definition to drive analysis into those methods
		 * to handle case 2)
		 */
		ompMethods = new HashSet<Entity>();
		ompMethodDefinitions = new HashSet<FunctionDefinitionNode>();
		collectOmpMethods(rootNode);

		unit.release();
		for (FunctionDefinitionNode fdn : ompMethodDefinitions) {
			this.currentFunciton = fdn.getEntity();
			transformOmp(fdn);
		}

		unit = astFactory.newAST(rootNode, unit.getSourceFiles(),
				unit.isWholeProgram());
		return unit;
	}

	AttributeKey getAttributeKey() {
		return this.dependenceKey;
	}

	private void addEntities(List<Entity> entityList,
			SequenceNode<IdentifierExpressionNode> clauseList) {
		if (clauseList != null) {
			for (IdentifierExpressionNode idExpression : clauseList) {
				Entity idEnt = idExpression.getIdentifier().getEntity();

				entityList.add(idEnt);
			}
		}
	}

	/*
	 * Scan program to collect methods involving omp constructs.
	 */
	private void collectOmpMethods(ASTNode node) {
		if (node instanceof FunctionDefinitionNode) {
			FunctionDefinitionNode fdn = (FunctionDefinitionNode) node;
			if (hasOmpConstruct(fdn.getBody())) {
				if (readWriteAnalyzer == null)
					readWriteAnalyzer = new SimpleReadWriteAnalyzer(
							SimplePointsToAnalysis.flowInsensePointsToAnalyzer(
									ast, astFactory.getTypeFactory()));
				ompMethods.add(fdn.getEntity());
				ompMethodDefinitions.add(fdn);
			}
		} else if (node != null) {
			Iterable<ASTNode> children = node.children();
			for (ASTNode child : children) {
				collectOmpMethods(child);
			}
		}
	}

	private boolean callsMethodWithOmpConstruct(ASTNode node) {
		boolean result = false;
		if (node instanceof FunctionCallNode) {
			ExpressionNode fun = ((FunctionCallNode) node).getFunction();
			if (fun instanceof IdentifierExpressionNode) {
				result = ompMethods.contains(((IdentifierExpressionNode) fun)
						.getIdentifier().getEntity());
			}
		} else if (node != null) {
			Iterable<ASTNode> children = node.children();
			for (ASTNode child : children) {
				result = callsMethodWithOmpConstruct(child);
				if (result)
					break;
			}
		}
		return result;
	}

	/*
	 * Traverse the method declaration analyzing and simplifying omp constructs
	 */
	private void transformOmp(ASTNode node) {
		if (node instanceof OmpParallelNode) {
			OmpExecutableNode ompExec = (OmpExecutableNode) node;
			/*
			 * TODO: this code does not yet handle: - nested parallel blocks -
			 * sections workshares - collapse clauses - chunk clauses - omp_*
			 * calls which should be interpreted as being dependent
			 */

			/*
			 * Determine the private variables since they cannot generate
			 * dependences.
			 */
			privateIDs = new ArrayList<Entity>();
			addEntities(privateIDs, ompExec.privateList());
			addEntities(privateIDs, ompExec.copyinList());
			addEntities(privateIDs, ompExec.copyprivateList());
			addEntities(privateIDs, ompExec.firstprivateList());
			addEntities(privateIDs, ompExec.lastprivateList());
			SequenceNode<OmpReductionNode> reductionList = ompExec
					.reductionList();
			if (reductionList != null) {
				for (OmpReductionNode r : reductionList) {
					addEntities(privateIDs, r.variables());
				}
			}

			/*
			 * Initialize shared read/writes performed within parallel, but not
			 * in workshares
			 */
			sharedWrites = new HashSet<>();
			sharedReads = new HashSet<>();
			sharedArrayWrites = new HashSet<>();
			sharedArrayReads = new HashSet<>();
			locallyDeclaredEntities = new Stack<>();

			/*
			 * Analyze the workshares contained lexically within the parallel
			 * node. A parallel node is "orphaned" if it makes calls to methods
			 * that contain OpenMP statements or calls. An orphaned parallel
			 * should not be removed without determining if all called methods
			 * can be simplified to eliminate their OpenMP statements.
			 * 
			 * TBD: Currently the orphan analysis only checks a single level of
			 * call; the full solution would require construction of a call
			 * graph.
			 */
			allIndependent = true;

			// push an empty entry to the stack to keep track of locally
			// declared entities:
			locallyDeclaredEntities.push(new HashSet<>());
			// Visit the rest of this node
			transformOmpWorkshare(ompExec.statementNode());
			locallyDeclaredEntities.pop();
			/*
			 * Check for dependences between statements that are not within
			 * workshares
			 */
			// for (Entity entity : sharedWrites) {
			// // Currently I'm not sure if sharedWrites only contains
			// // scalar variables, so I have to go over it. As long as one
			// // scalar variable in it, there is possible a write-after-write
			// // data race (modified and commented by Ziqing):
			// if (entity.getEntityKind() == EntityKind.VARIABLE) {
			// Variable var = (Variable) entity;
			//
			// if (var.getType().isScalar()) {
			// allIndependent = false;
			// break;
			// }
			// }
			// }
			// sharedWrites.retainAll(sharedReads);

			Set<RWSetElement> fullWrites = new HashSet<>(sharedArrayWrites);

			for (Variable var : sharedWrites)
				fullWrites.add(readWriteAnalyzer.packVariable(var));
			allIndependent &= sharedWrites.isEmpty();
			if (debug && !sharedWrites.isEmpty()) {
				System.err.println(sharedWrites
						+ " are shared but will be written by multiple threads");
			}
			allIndependent &= new ArrayReferenceDependencyAnalyzer(
					readWriteAnalyzer).threadsArrayAccessIndependent(
							currentFunciton, new LinkedList<>(),
							sharedArrayWrites, sharedArrayReads, fullWrites,
							new HashSet<>(), null);

			boolean isOrphaned = callsMethodWithOmpConstruct(
					ompExec.statementNode());

			if (allIndependent && !isOrphaned) {
				/*
				 * Remove the nested omp constructs, e.g., workshares, calls to
				 * omp_*, ordered sync nodes, etc.
				 */
				removeOmpConstruct(ompExec.statementNode());

				/*
				 * NB: the call above can change its argument (by restructuring
				 * the AST), so we need to recompute the statementNode below.
				 */

				// Remove "statement" node from "omp parallel" node
				StatementNode stmt = ompExec.statementNode();
				int stmtIndex = getChildIndex(ompExec, stmt);
				assert stmtIndex != -1;
				ompExec.removeChild(stmtIndex);

				// Link "statement" into the "omp parallel" parent
				ASTNode parent = ompExec.parent();
				int parentIndex = getChildIndex(parent, ompExec);
				assert parentIndex != -1;
				parent.setChild(parentIndex, stmt);
			} else if (config.ompOnlySimplifier())
				throw new CIVLException(
						"openMP program possibly contains data race",
						new ABC_CIVLSource(node.getSource()));
		} else if (node instanceof OmpExecutableNode) {
			privateIDs = new ArrayList<Entity>();

			if (node instanceof OmpSimdNode) {
				OmpExecutableNode simdNode = (OmpExecutableNode) node;
				StatementNode forLoop = simdNode.statementNode();
				ASTNode parent = simdNode.parent();
				int childIdx = simdNode.childIndex();

				sharedWrites = new HashSet<>();
				sharedReads = new HashSet<>();
				sharedArrayWrites = new HashSet<>();
				sharedArrayReads = new HashSet<>();
				locallyDeclaredEntities = new Stack<>();
				locallyDeclaredEntities.push(new HashSet<>());
				allIndependent = true;
				transformOmpWorkshare(node);
				locallyDeclaredEntities.pop();
				if (allIndependent) {
					forLoop.remove();
					parent.setChild(childIdx, forLoop);
				} else if (config.ompOnlySimplifier())
					throw new CIVLException(
							"openMP program possibly contains data race",
							new ABC_CIVLSource(node.getSource()));
			} else
				transformOmpWorkshare(node);
		} else if (node != null) {
			// BUG: can get here with null values in parallelfor.c example

			/*
			 * Could match other types here that have no ForLoopNode below them
			 * and skip their traversal to speed things up.
			 */
			Iterable<ASTNode> children = node.children();
			for (ASTNode child : children) {
				transformOmp(child);
			}
		}
	}

	/*
	 * This method assumes that all of the OMP statements that are encountered
	 * can be safely removed or transformed into non-OMP equivalents.
	 */
	private void removeOmpConstruct(ASTNode node) {
		if (node instanceof OmpExecutableNode) {
			// Remove "statement" node from "omp statement" node
			StatementNode stmt = ((OmpExecutableNode) node).statementNode();
			int stmtIndex = getChildIndex(node, stmt);
			assert stmtIndex != -1;
			node.removeChild(stmtIndex);

			// Link "statement" into the "omp workshare" parent
			ASTNode parent = node.parent();
			int parentIndex = getChildIndex(parent, node);
			assert parentIndex != -1;

			parent.setChild(parentIndex, stmt);
			removeOmpConstruct(stmt);

		} else if (node instanceof FunctionCallNode
				&& ((FunctionCallNode) node)
						.getFunction() instanceof IdentifierExpressionNode
				&& ((IdentifierExpressionNode) ((FunctionCallNode) node)
						.getFunction()).getIdentifier().name()
								.startsWith("omp_")) {
			// I replace node with a null-like node.
			Source nodeSource = node.getSource();
			String ompFunctionName = ((IdentifierExpressionNode) ((FunctionCallNode) node)
					.getFunction()).getIdentifier().name();
			ASTNode replacement = null;
			if (ompFunctionName.equals("omp_get_thread_num")) {
				replacement = nodeFactory.newIntConstantNode(nodeSource, 0);
			} else if (ompFunctionName.equals("omp_get_num_threads")
					|| ompFunctionName.equals("omp_get_max_threads")
					|| ompFunctionName.equals("omp_get_num_procs")
					|| ompFunctionName.equals("omp_get_thread_limit")) {
				replacement = nodeFactory.newIntConstantNode(nodeSource, 1);
			} else if (ompFunctionName.equals("omp_init_lock")
					|| ompFunctionName.equals("omp_set_lock")
					|| ompFunctionName.equals("omp_unset_lock")
					|| ompFunctionName.equals("omp_set_num_threads")) {
				// delete this node
				replacement = nodeFactory.newCastNode(nodeSource,
						nodeFactory.newVoidTypeNode(nodeSource),
						nodeFactory.newIntConstantNode(nodeSource, 0));
			} else if (ompFunctionName.equals("omp_get_wtime")) {
				// this will be transformed by the OMP transformer

			} else {
				assert false : "Unsupported omp function call "
						+ ompFunctionName
						+ " cannot be replaced by OpenMP simplifier";
			}

			// Link "replacement" into the omp call's parent
			ASTNode parent = node.parent();
			int parentIndex = getChildIndex(parent, node);
			assert parentIndex != -1;
			parent.setChild(parentIndex, replacement);
		} else if (node != null) {
			Iterable<ASTNode> children = node.children();
			for (ASTNode child : children) {
				removeOmpConstruct(child);
			}
		}
	}

	/*
	 * Scan workshares to drive dependence analysis.
	 */
	private void transformOmpWorkshare(ASTNode node) {
		if (node instanceof OmpForNode) {
			OmpForNode ompFor = (OmpForNode) node;
			/*
			 * Determine the private variables since they cannot generate
			 * dependences.
			 * 
			 * TBD: Factor this out to a method and pass it as a parameter to
			 * processFor
			 */
			loopPrivateIDs = new ArrayList<Entity>();
			addEntities(loopPrivateIDs, ompFor.privateList());
			addEntities(loopPrivateIDs, ompFor.copyinList());
			addEntities(loopPrivateIDs, ompFor.copyprivateList());
			addEntities(loopPrivateIDs, ompFor.firstprivateList());
			addEntities(loopPrivateIDs, ompFor.lastprivateList());
			SequenceNode<OmpReductionNode> reductionList = ompFor
					.reductionList();
			if (reductionList != null) {
				for (OmpReductionNode r : reductionList) {
					addEntities(loopPrivateIDs, r.variables());
				}
			}

			processForOrSimd(ompFor);

			// reset variable to avoid interference with processing of non-loop
			// parallel regions
			loopPrivateIDs = new ArrayList<Entity>();

		} else if (node instanceof OmpSyncNode) {
			OmpSyncNode syncNode = (OmpSyncNode) node;

			OmpSyncNodeKind kind = syncNode.ompSyncNodeKind();
			switch (kind) {
				case MASTER :
					allIndependent = false;
					break;
				case CRITICAL :
					allIndependent = false;
					break;
				case BARRIER :
					allIndependent = false;;
					break;
				case ORDERED :
					/*
					 * Should be able to optimize this case since semantics
					 * guarantee serial order
					 */
					allIndependent = false;;
					break;
				case FLUSH :
					allIndependent = false;;
					break;
				default :
					allIndependent = false;
					break;
			}

		} else if (node instanceof OmpWorksharingNode) {
			OmpWorksharingNode wsNode = (OmpWorksharingNode) node;

			OmpWorksharingNodeKind kind = wsNode.ompWorkshareNodeKind();
			switch (kind) {
				case SECTIONS :
					allIndependent = false;
					break;
				case SECTION :
					allIndependent = false;
					break;
				case SINGLE :
					allIndependent = false;
					break;
				default :
					allIndependent = false;
					break;
			}

		} else if (isAssignment(node)) {
			/*
			 * Collect up the set of assignment statements that are not within
			 * workshares
			 */

			/*
			 * This code is clunky because it uses globals, but we can
			 * accumulate the read/write sets for these statements within the
			 * parallel and then post process them.
			 */

			// Reset visitor globals
			writeVars = new HashSet<>();
			readVars = new HashSet<>();
			writeArrayRefs = new HashSet<>();
			readArrayRefs = new HashSet<>();
			loopPrivateIDs = new ArrayList<Entity>();

			collectAssignRefExprs(node);

			if (debug) {
				System.out.println(
						"Analyzed non-workshare assignment " + node + " with:");
				System.out.println("   Reads = " + readVars);
				System.out.println("   Writes = " + writeVars);
				System.out.println("   ArrayReads = " + readArrayRefs);
				System.out.println("   ArrayWrites = " + writeArrayRefs);
			}

			for (Variable read : readVars)
				if (!privateIDs.contains(read)
						&& !loopPrivateIDs.contains(read))
					sharedReads.add(read);

			for (Variable write : writeVars)
				if (!privateIDs.contains(write)
						&& !loopPrivateIDs.contains(write))
					sharedWrites.add(write);

			for (RWSetElement readArr : readArrayRefs)
				if (!privateIDs.contains(containedBy(readArr))
						&& !loopPrivateIDs.contains(containedBy(readArr)))
					sharedArrayReads.add(readArr);

			for (RWSetElement writeArr : writeArrayRefs)
				if (!privateIDs.contains(containedBy(writeArr))
						&& !loopPrivateIDs.contains(containedBy(writeArr)))
					sharedArrayWrites.add(writeArr);

			/*
			 * TBD: we are not collecting the reads from all of the appropriate
			 * statements. For example the reads in the conditions of
			 * if/while/for/...
			 */

		} else if (node instanceof OmpSimdNode) {
			processForOrSimd((OmpSimdNode) node);
		} else if (node != null) {

			// BUG: can get here with null values in parallelfor.c example

			/*
			 * Could match other types here that have no ForLoopNode below them
			 * and skip their traversal to speed things up.
			 */
			Iterable<ASTNode> children = node.children();
			for (ASTNode child : children) {
				transformOmpWorkshare(child);
			}
		}
	}

	/*
	 */
	private void processForOrSimd(OmpExecutableNode ompFor) {
		ForLoopNode fln = (ForLoopNode) ompFor.statementNode();

		/*
		 * If the "omp for" has a "nowait" clause then it can still be
		 * transformed as long as its parent is the "omp parallel", i.e., no
		 * additional statements follow it in the "omp parallel"
		 */
		if (ompFor.nowait())
			if (!(ompFor.parent() instanceof OmpParallelNode)) {
				this.allIndependent = false;
				return;
			}

		/*
		 * Need to handle collapse through the use of a sequence of iteration
		 * variables and associated constraints on them.
		 */

		/*
		 * The following block computes the loop appropriate constraints that
		 * bound the loop variable's range.
		 * 
		 * This code handles non-normalized loops, e.g., starting at non-zero
		 * indices, iterating down or up, etc.
		 * 
		 * TBD: record the increment to be used for more precise dependence
		 * constraints.
		 * 
		 * It does not check whether those bounding expressions are loop
		 * invariant, which is required by the OpenMP standard, but it does
		 * enforce a number of other canonical loop form constraints.
		 */
		IdentifierNode loopVariable = null;
		ExpressionNode initBound = null;
		Set<Variable> parForLoopVars = new HashSet<>();
		List<ExpressionNode> boundingConditions = new LinkedList<ExpressionNode>();

		{
			ForLoopInitializerNode initializer = fln.getInitializer();
			if (initializer instanceof OperatorNode) {
				OperatorNode assign = (OperatorNode) initializer;
				Operator op = assign.getOperator();
				if (op == Operator.ASSIGN) {
					ExpressionNode left = assign.getArgument(0);
					assert left instanceof IdentifierExpressionNode : "OpenMP Canonical Loop Form violated (identifier required on LHS of initializer)";
					IdentifierNode leftIdNode = ((IdentifierExpressionNode) left)
							.getIdentifier();

					parForLoopVars.add((Variable) leftIdNode.getEntity());
					loopVariable = leftIdNode.copy();
					initBound = assign.getArgument(1).copy();
				} else {
					assert false : "OpenMP Canonical Loop Form violated (initializer must be an assignment) :"
							+ assign;
				}

			} else if (initializer instanceof DeclarationListNode) {
				if (initializer instanceof SequenceNode<?>) {
					@SuppressWarnings("unchecked")
					SequenceNode<VariableDeclarationNode> decls = (SequenceNode<VariableDeclarationNode>) initializer;
					Iterator<VariableDeclarationNode> it = (Iterator<VariableDeclarationNode>) decls
							.iterator();
					VariableDeclarationNode vdn = it.next();
					if (it.hasNext()) {
						assert false : "OpenMP Canonical Loop Form violated (single initializer only) :"
								+ initializer;
					}
					parForLoopVars.add(vdn.getEntity());
					loopVariable = vdn.getEntity().getDefinition()
							.getIdentifier().copy();

					assert vdn
							.getInitializer() instanceof ExpressionNode : "OpenMP Canonical Loop Form violated (initializer must be simple expression)";

					// Record the initializer expression to build up the
					// initCondition below
					initBound = (ExpressionNode) vdn.getInitializer().copy();
				} else {
					assert false : "Expected SequenceNode<VariableDeclarationNode>: "
							+ initializer;
				}

			} else {
				assert false : "Expected OperatorNode or DeclarationListNode: "
						+ initializer;
			}

			ExpressionNode condition = fln.getCondition();

			if (condition instanceof OperatorNode) {
				OperatorNode relop = (OperatorNode) condition;

				/*
				 * The initial bound of the iteration space is established by an
				 * assignment statement. We need to convert that assignment into
				 * an appropriate inequality to appropriately constrain the loop
				 * variable values. The code assumes that the polarity of the
				 * loop exit condition and the increment operator are
				 * compatible, i.e., a "<" or "<=" test is coupled with an "++"
				 * and a ">" or ">=" with a "--"; this condition is not checked
				 * here. We reverse the polarity of the loop exit condition to
				 * establish the boundary condition associated with the
				 * initialization and make that condition non-strict to account
				 * for the equality implicit in the assignment.
				 * 
				 * This results in the following types of behavior: for (int
				 * i=0; i<N; i++) generates "i>=0" as an "initial" bound for
				 * (int i=0; i<=N-1; i++) generates "i>=0" as an "initial" bound
				 * for (int i=N-1; i>=0; i++) generates "i<=N-1" as an "initial"
				 * bound for (int i=N-1; i>-1; i++) generates "i<=N-1" as an
				 * "initial" bound
				 */
				List<ExpressionNode> arguments = new LinkedList<ExpressionNode>();
				ExpressionNode lvNode = nodeFactory.newIdentifierExpressionNode(
						ompFor.getSource(), loopVariable);
				arguments.add(lvNode);
				arguments.add(initBound);

				Operator op = relop.getOperator();
				if (op == Operator.LT || op == Operator.LTE) {
					OperatorNode newBoundExpr = nodeFactory.newOperatorNode(
							ompFor.getSource(), Operator.GTE, arguments);
					boundingConditions.add(newBoundExpr);

				} else if (op == Operator.GT || op == Operator.GTE) {
					OperatorNode newBoundExpr = nodeFactory.newOperatorNode(
							ompFor.getSource(), Operator.LTE, arguments);
					boundingConditions.add(newBoundExpr);
				} else {
					assert false : "OpenMP Canonical Loop Form violated (condition must be one of >, >=, <, or <=) :"
							+ relop;
				}

				ExpressionNode left = relop.getArgument(0);
				ExpressionNode right = relop.getArgument(1);

				/*
				 * variable must be either left or right, but not both
				 * 
				 * Currently these checks are based on the name of the variable.
				 * Perhaps it is better to use the symbol information, i.e.,
				 * getEntity()
				 */
				int loopVariableCount = 0;
				if (left instanceof IdentifierExpressionNode) {
					IdentifierNode id = ((IdentifierExpressionNode) left)
							.getIdentifier();
					if (id.name().equals(loopVariable.name())) {
						loopVariableCount++;
					}
				}

				if (right instanceof IdentifierExpressionNode) {
					IdentifierNode id = ((IdentifierExpressionNode) right)
							.getIdentifier();
					if (id.name().equals(loopVariable.name())) {
						loopVariableCount++;
					}
				}

				if (loopVariableCount == 1) {
					boundingConditions.add(condition);
				} else {
					assert false : "OpenMP Canonical Loop Form violated (requires variable condition operand) :"
							+ condition;
				}
			} else {
				assert false : "OpenMP Canonical Loop Form violated (condition malformed) :"
						+ condition;
			}

		}

		/*
		 * Accumulate the set of memory-referencing expressions, i.e., variable
		 * references, array index expressions, on the LHS and the RHS.
		 * 
		 * TBD: Need to generalize this to record the full alias-equivalence
		 * relation. - parameters of the same type are potential aliases unless
		 * the "restrict" keyword is used - assignments through pointers need to
		 * be handled as well
		 * 
		 * TBD: We need to know the dimensions of array parameters or else we
		 * cannot reason about independent memory regions, e.g., indexing of the
		 * end of one array may touch an element of an adjacent array.
		 */
		StatementNode body = fln.getBody();
		writeVars = new HashSet<>();
		readVars = new HashSet<>();
		writeArrayRefs = new HashSet<>();
		readArrayRefs = new HashSet<>();

		collectAssignRefExprs(body);

		/*
		 * Check for name-based dependences
		 */
		boolean independent = true;
		// For each entity in writeVars, if it is not in the local-declaration
		// collection, add it to sharedWrites (modified by Ziqing):
		for (Variable writeVar : writeVars) {
			boolean localContains = false;

			for (Set<Entity> stackEntry : locallyDeclaredEntities)
				if (stackEntry.contains(writeVar)) {
					localContains = true;
					break;
				}
			if (!localContains && !loopPrivateIDs.contains(writeVar)
					&& !privateIDs.contains(writeVar)) {
				sharedWrites.add(writeVar);
			}
		}
		// TODO: confirm the following code is wrong. If there exists write to
		// shared non-array object, there are dependencies among threads:
		// writeVars.retainAll(readVars);
		independent &= sharedWrites.isEmpty();

		if (independent) {
			Integer safelen = null;

			if (ompFor.ompExecutableKind() == OmpExecutableKind.SIMD) {
				OmpSimdNode simdNode = (OmpSimdNode) ompFor;

				if (simdNode.safeLen() != null)
					safelen = ((IntegerConstantNode) simdNode.safeLen())
							.getConstantValue().getIntegerValue()
							.intValueExact();
			}

			/*
			 * Check for array-based dependences.
			 */
			Set<RWSetElement> readWriteArrayRefs = new HashSet<>(readArrayRefs);
			Set<RWSetElement> fullWrites = new HashSet<>(writeArrayRefs);

			for (Variable writeVar : writeVars)
				fullWrites.add(readWriteAnalyzer.packVariable(writeVar));
			readWriteArrayRefs.addAll(writeArrayRefs);
			independent &= new ArrayReferenceDependencyAnalyzer(
					readWriteAnalyzer).threadsArrayAccessIndependent(
							currentFunciton, boundingConditions, writeArrayRefs,
							readWriteArrayRefs, fullWrites, parForLoopVars,
							safelen);
			if (debug) {
				// TODO: re-write these debug info
				System.out.println(
						"Found " + (independent ? "independent" : "dependent")
								+ " OpenMP for " + ompFor);
				System.out.println("  writeVars : " + writeVars);
				System.out.println("  readVars : " + readVars);
				System.out.println("  writeArrays : " + writeArrayRefs);
				System.out.println("  readArrays : " + readArrayRefs);
			}
		}
		if (independent) {
			/*
			 * At this point we can create a branch in the program based on the
			 * boundingConditions and array's reference expressions. Essentially
			 * we want to construct a runtime predicate that captures the
			 * assumptions about referenced memory locations that led to the
			 * judgement of independence.
			 * 
			 * For example if the program had for (int i=0; i<N; i++) a[i] =
			 * b[i]; then we want to generate a condition that says that: a != b
			 * && a+(N-1) < b && b+(N-1) < a which ensures that the array
			 * regions that are accessed are disjoint.
			 * 
			 * Note that this would approach could generalize to more
			 * interesting cases: for (int i=0; i<(N/2); i++) a[i] = a[N-1-i];
			 */

			/*
			 * Transform this "omp for" into a "omp single" workshare. To safely
			 * perform this when a reduction is present for an (op,var) pair all
			 * assignments to "var" in the "omp for" must be of the form
			 * "var = var op ...".
			 */
			SequenceNode<OmpReductionNode> reductionList = ompFor
					.reductionList();
			List<IdentifierNode> reductionVariables = null;
			boolean safeReduction = true;

			if (reductionList != null) {
				reductionVariables = new ArrayList<IdentifierNode>();

				// Build a map for scanning for reduction variable operator
				// assignment consistency
				Map<Entity, OmpReductionOperator> idOpMap = new HashMap<Entity, OmpReductionOperator>();
				for (OmpReductionNode r : reductionList) {
					if (r instanceof OmpSymbolReductionNode) {
						OmpReductionOperator op = ((OmpSymbolReductionNode) r)
								.operator();
						for (IdentifierExpressionNode id : r.variables()) {
							idOpMap.put(id.getIdentifier().getEntity(), op);
							reductionVariables.add(id.getIdentifier());
						}
						safeReduction &= checkReductionAssignments(fln,
								idOpMap);
					} else {
						safeReduction = false;
						break;
					}
				}
			}

			if (safeReduction) {
				ASTNode parent = ompFor.parent();
				int ompForIndex = getChildIndex(parent, ompFor);
				assert ompForIndex != -1;
				parent.removeChild(ompForIndex);

				fln.parent().removeChild(fln.childIndex());
				OmpWorksharingNode single = nodeFactory
						.newOmpSingleNode(ompFor.getSource(), fln);

				// Transfer private, firstprivate, copyprivate, and nowait
				// clauses to single

				SequenceNode<IdentifierExpressionNode> singlePrivateList = ompFor
						.privateList() != null
								? ompFor.privateList().copy()
								: null;
				SequenceNode<IdentifierExpressionNode> singleFirstPrivateList = ompFor
						.firstprivateList() != null
								? ompFor.firstprivateList().copy()
								: null;
				SequenceNode<IdentifierExpressionNode> singleCopyPrivateList = ompFor
						.copyprivateList() != null
								? ompFor.copyprivateList().copy()
								: null;

				// Add iteration variable to private list for single
				IdentifierExpressionNode privateLoopVariable = nodeFactory
						.newIdentifierExpressionNode(single.getSource(),
								nodeFactory.newIdentifierNode(
										single.getSource(),
										loopVariable.name()));
				if (ompFor.privateList() == null) {
					List<IdentifierExpressionNode> l = new ArrayList<IdentifierExpressionNode>();
					l.add(privateLoopVariable);
					singlePrivateList = nodeFactory
							.newSequenceNode(ompFor.getSource(), "private", l);
				} else {
					singlePrivateList.addSequenceChild(privateLoopVariable);
				}

				single.setPrivateList(singlePrivateList);
				single.setFirstprivateList(singleFirstPrivateList);

				// Any reduction variables should be added to copyprivate
				if (reductionVariables != null) {
					List<IdentifierExpressionNode> l = new ArrayList<IdentifierExpressionNode>();
					for (IdentifierNode id : reductionVariables) {
						l.add(nodeFactory
								.newIdentifierExpressionNode(single.getSource(),
										nodeFactory.newIdentifierNode(
												single.getSource(),
												id.name())));
					}
					if (ompFor.copyprivateList() == null) {
						singleCopyPrivateList = nodeFactory.newSequenceNode(
								ompFor.getSource(), "copyprivate", l);
					} else {
						for (IdentifierExpressionNode ide : l) {
							singleCopyPrivateList.addSequenceChild(ide);
						}
					}
				}

				single.setCopyprivateList(singleCopyPrivateList);
				single.setNowait(ompFor.nowait());

				parent.setChild(ompForIndex, single);
			}
		}

		allIndependent &= independent;

	}

	private boolean checkReductionAssignments(ASTNode node,
			Map<Entity, OmpReductionOperator> idOpMap) {
		return checkReductionWorker(node, idOpMap, true);
	}

	private boolean checkReductionWorker(ASTNode node,
			Map<Entity, OmpReductionOperator> idOpMap, boolean consistent) {
		if (node instanceof OperatorNode) {
			/*
			 * Access the LHS to prepare for operator comparison
			 */
			ExpressionNode lhs = ((OperatorNode) node).getArgument(0);
			if (lhs instanceof IdentifierExpressionNode) {
				Entity idEnt = ((IdentifierExpressionNode) lhs).getIdentifier()
						.getEntity();
				if (idOpMap.keySet().contains(idEnt)) {
					Operator op = OmpReduceOp2CivlOp(idOpMap.get(idEnt));

					switch (((OperatorNode) node).getOperator()) {
						case ASSIGN :
							ExpressionNode rhs = ((OperatorNode) node)
									.getArgument(1);
							if (rhs instanceof OperatorNode) {
								consistent &= (eqOpToOp(
										op) == ((OperatorNode) rhs)
												.getOperator());
							} else {
								consistent = false;
							}
							break;
						case BITANDEQ :
						case BITOREQ :
						case BITXOREQ :
						case MINUSEQ :
						case PLUSEQ :
						case TIMESEQ :
						case LAND :
						case LOR :
							consistent &= (((OperatorNode) node)
									.getOperator() == op);
							break;
						case EQUALS :
						case NEQ :
						default :
					}
				}
			}

		} else if (node != null) {
			Iterable<ASTNode> children = node.children();
			for (ASTNode child : children) {
				consistent &= checkReductionWorker(child, idOpMap, consistent);
			}
		}
		return consistent;
	}

	private Operator OmpReduceOp2CivlOp(
			OmpReductionOperator ompReductionOperator) {
		switch (ompReductionOperator) {
			case SUM :
				return Operator.PLUSEQ;
			case MINUS :
				return Operator.MINUSEQ;
			case PROD :
				return Operator.TIMESEQ;
			case BAND :
				return Operator.BITANDEQ;
			case BOR :
				return Operator.BITOREQ;
			case BXOR :
				return Operator.BITXOREQ;
			case LAND :
				return Operator.LAND;
			case LOR :
				return Operator.LOR;
			case EQV :
				return Operator.EQUALS;
			case NEQ :
				return Operator.NEQ;
			case MIN :
			case MAX :
			default :
				throw new CIVLUnimplementedFeatureException(
						"Unsupport OpenMP Reduce Operator: "
								+ ompReductionOperator);
		}
	}

	/*
	 * Returns the index of "child" in the children of "node"; -1 if "child" is
	 * not one of "node"'s children.
	 */
	private int getChildIndex(ASTNode node, ASTNode child) {
		for (int childIndex = 0; childIndex < node
				.numChildren(); childIndex++) {
			if (node.child(childIndex) == child)
				return childIndex;
		}
		return -1;
	}

	private Operator eqOpToOp(Operator op) {
		switch (op) {
			case BITANDEQ :
				return Operator.BITAND;
			case BITOREQ :
				return Operator.BITOR;
			case BITXOREQ :
				return Operator.BITXOR;
			case MINUSEQ :
				return Operator.MINUS;
			case PLUSEQ :
				return Operator.PLUS;
			case TIMESEQ :
				return Operator.TIMES;
			default :
		}
		return op;
	}

	private boolean isAssignment(ASTNode node) {
		return (node instanceof OperatorNode)
				&& (((OperatorNode) node).getOperator() == Operator.ASSIGN
						|| ((OperatorNode) node)
								.getOperator() == Operator.PLUSEQ
						|| ((OperatorNode) node)
								.getOperator() == Operator.POSTINCREMENT
						|| ((OperatorNode) node)
								.getOperator() == Operator.POSTDECREMENT);
	}

	private void collectAssignRefExprs(ASTNode node) {
		RWSet rwSet;

		try {
			rwSet = readWriteAnalyzer.collectRWFromStmtDeclExpr(currentFunciton,
					node, locallyDeclaredEntities.peek());
		} catch (SimpleFullSetException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
		// fill in writeVars, readVars, writeArrayRefs and readArrayRefs:
		for (RWSetElement e : rwSet.writes()) {
			Variable var = refersToVariable(e);

			if (var != null)
				writeVars.add(var);
			else
				this.writeArrayRefs.add(e);
		}
		for (RWSetElement e : rwSet.reads()) {
			Variable var = refersToVariable(e);

			if (var != null)
				readVars.add(var);
			else
				this.readArrayRefs.add(e);
		}
	}

	private Variable refersToVariable(RWSetElement e) {
		if (e.kind() == RWSetElementKind.BASE) {
			AssignExprIF ao = ((RWSetBaseElement) e).base();

			if (ao.kind() != AssignExprKind.STORE)
				return null;

			AssignStoreExprIF store = (AssignStoreExprIF) ao;

			return store.variable();
		}
		return null;
	}

	private Variable containedBy(RWSetElement e) {
		AssignExprIF ao = e.root();

		if (ao == null || ao.kind() != AssignExprKind.STORE)
			return null;

		AssignStoreExprIF store = (AssignStoreExprIF) ao;

		return store.variable();
	}

	/*
	 * Determine whether node lexically contains an omp construct
	 */
	private boolean hasOmpConstruct(ASTNode node) {
		boolean result = false;
		if (node instanceof OmpNode) {
			result = true;
		} else if (node instanceof FunctionCallNode
				&& ((FunctionCallNode) node)
						.getFunction() instanceof IdentifierExpressionNode
				&& ((IdentifierExpressionNode) ((FunctionCallNode) node)
						.getFunction()).getIdentifier().name()
								.startsWith("omp_")) {
			result = true;
		} else if (node != null) {
			Iterable<ASTNode> children = node.children();
			for (ASTNode child : children) {
				result |= hasOmpConstruct(child);
				if (result)
					break;
			}
		}
		return result;
	}
}
