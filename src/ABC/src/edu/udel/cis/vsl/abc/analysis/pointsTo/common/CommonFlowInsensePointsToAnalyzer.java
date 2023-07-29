package edu.udel.cis.vsl.abc.analysis.pointsTo.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignExprIF;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignExprIF.AssignExprKind;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignStoreExprIF;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.FlowInsensePointsToAnalyzer;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.InsensitiveFlow;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.InsensitiveFlowFactory;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.InvocationGraphNode;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.InvocationGraphNodeFactory;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.PointsToGraph;
import edu.udel.cis.vsl.abc.ast.IF.AST;
import edu.udel.cis.vsl.abc.ast.entity.IF.Entity;
import edu.udel.cis.vsl.abc.ast.entity.IF.Entity.EntityKind;
import edu.udel.cis.vsl.abc.ast.entity.IF.Function;
import edu.udel.cis.vsl.abc.ast.entity.IF.Scope;
import edu.udel.cis.vsl.abc.ast.entity.IF.Variable;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.DeclarationNode;
import edu.udel.cis.vsl.abc.ast.type.IF.Field;
import edu.udel.cis.vsl.abc.ast.type.IF.PointerType;
import edu.udel.cis.vsl.abc.ast.type.IF.Type;
import edu.udel.cis.vsl.abc.ast.type.IF.Type.TypeKind;
import edu.udel.cis.vsl.abc.err.IF.ABCRuntimeException;
import edu.udel.cis.vsl.abc.util.IF.Pair;
import edu.udel.cis.vsl.sarl.SARL;
import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;

public class CommonFlowInsensePointsToAnalyzer
		implements
			FlowInsensePointsToAnalyzer {

	/**
	 * The program associated with this analyzer
	 */
	private final AST program;

	/**
	 * the root node of the invocation graph
	 */
	private InvocationGraphNode rootNode;

	/**
	 * a reference to {@link InsensitiveFlowFactory} for creating
	 * {@link InsensitiveFlow}s for analysis
	 */
	private InsensitiveFlowFactory factory;

	/**
	 * a reference to {@link InvocationGraphNodeFactory} for creating
	 * {@link InvocationGraphNode} for analysis
	 */
	private InvocationGraphNodeFactory igFactory;

	/**
	 * a table maps {@link Function}s to {@link InsensitiveFlow}s that represent
	 * the points-to abstraction of their function bodies.
	 */
	private Map<Function, InsensitiveFlow> flowTable;

	/**
	 * a table maps {@link InvocationGraphNode}s to their {@link PointsToGraph}s
	 */
	private Map<InvocationGraphNode, PointsToGraph> pointsToTable;

	/**
	 * a table maps functions to all the nodes representing lexical calls to
	 * them:
	 */
	private Map<Function, List<InvocationGraphNode>> funcCallsTable;

	private SymbolicUniverse universe;

	CommonFlowInsensePointsToAnalyzer(AST ast, InsensitiveFlowFactory factory,
			InvocationGraphNodeFactory igFactory) {
		this.program = ast;
		this.factory = factory;
		this.igFactory = igFactory;
		this.flowTable = new HashMap<>();
		this.pointsToTable = new HashMap<>();
		this.funcCallsTable = new HashMap<>();
		this.universe = SARL.newStandardUniverse();
		this.rootNode = abstractProgram();

		/*
		 * map function "f" to a points-to graph which can be used to initialize
		 * (by PointsToGraph#clone()) the points-to graphs of nodes that are
		 * associated with the function "f"
		 */
		Map<Function, PointsToGraph> basePtGraphs = new HashMap<>();

		intraProceduralAnalysis(basePtGraphs);
		interProceduralAnalysis(basePtGraphs);
		// build funcCallsTable:
		for (InvocationGraphNode node : pointsToTable.keySet()) {
			List<InvocationGraphNode> calls = funcCallsTable
					.get(node.function());

			if (calls == null)
				calls = new LinkedList<>();
			calls.add(node);
			funcCallsTable.put(node.function(), calls);
		}
		// System.out.println(pointsToTable.get(rootNode));
	}

	@Override
	public List<AssignExprIF> mayPointsTo(Function func,
			Entity[] designations) {
		assert designations[0].getEntityKind() == EntityKind.VARIABLE;
		List<Field> fields = new LinkedList<>();

		for (int i = 1; i < designations.length; i++)
			fields.add((Field) designations[i]);
		return mayPointsToWorker(func, designations[0], fields);
	}

	@Override
	public List<AssignExprIF> mayPointsTo(Function func, AssignExprIF ptr) {
		return this.mayPointsToWorker(func, ptr, null);
	}

	@Override
	public AST analyzedProgram() {
		return this.program;
	}

	@Override
	public InsensitiveFlowFactory insensitiveFlowFactory() {
		return factory;
	}

	@Override
	public InsensitiveFlow insensitiveFlow(Function func) {
		return flowTable.get(func);
	}

	private List<AssignExprIF> mayPointsToWorker(Function func, Object ptr,
			List<Field> fields) {
		Iterable<InvocationGraphNode> calls = funcCallsTable.get(func);
		Set<AssignExprIF> set = new HashSet<>();
		List<AssignExprIF> result = new LinkedList<>();

		assert calls != null;

		for (InvocationGraphNode node : calls) {
			PointsToGraph ptGraph = pointsToTable.get(node);
			Iterable<AssignExprIF> pts;

			if (ptr instanceof Variable)
				pts = ptGraph.mayPointsTo((Variable) ptr, fields);
			else
				pts = ptGraph.mayPointsTo((AssignExprIF) ptr);
			for (AssignExprIF pt : pts)
				set.add(pt);
		}
		result.addAll(set);
		return result;
	}

	/**
	 * build invocation graph, as well as insensitive flow of each function, for
	 * the given program
	 * 
	 * @param ast
	 * @return
	 */
	private InvocationGraphNode abstractProgram() {
		/*
		 * A function can corresponds to multiple invocation graph nodes since a
		 * function can be called multiple times. This map saves only one node
		 * for every processed function. We only save one node for a function
		 * because nodes associated with the same function will have same value
		 * before the points-to analysis.
		 */
		Map<Function, InvocationGraphNode> funcs2Nodes = new HashMap<>();
		// start from "main" function, builds an invocation graph of the
		// program:
		Function mainFunc = (Function) program
				.getInternalOrExternalEntity("main");

		if (mainFunc == null)
			throw new ABCRuntimeException(
					"Unimplemented: points-to analysis without main function");

		InvocationGraphNode mainNode = igFactory.newNode(mainFunc, null, null);
		InsensitiveFlow mainFlow = factory.InsensitiveFlow(mainFunc, mainNode);
		// System.out.println(mainFlow);
		funcs2Nodes.put(mainFunc, mainNode);
		flowTable.put(mainFunc, mainFlow);
		for (InvocationGraphNode child : mainNode.children())
			abstractInvocation(child, funcs2Nodes);
		return mainNode;
	}

	/**
	 * recursively build invocation graph:
	 */
	private void abstractInvocation(InvocationGraphNode node,
			Map<Function, InvocationGraphNode> funcs2Nodes) {
		InvocationGraphNode sameValNode = funcs2Nodes.get(node.function());

		if (sameValNode != null) {
			assert sameValNode.formalParams() != null;
			sameValNode.share(node);
		} else {
			InsensitiveFlow flow = factory.InsensitiveFlow(node.function(),
					node);

			// System.out.println(flow);
			flowTable.put(node.function(), flow);
			funcs2Nodes.put(node.function(), node);
		}
		for (InvocationGraphNode child : node.children())
			abstractInvocation(child, funcs2Nodes);
	}

	/**
	 * builds points-to graph for every function definition
	 * 
	 * @param basePtGraphs
	 */
	private void intraProceduralAnalysis(
			Map<Function, PointsToGraph> basePtGraphs) {
		for (Entry<Function, InsensitiveFlow> entry : flowTable.entrySet()) {
			PointsToGraph ptGraph = new CommonPointsToGraph(entry.getValue(),
					universe);

			basePtGraphs.put(entry.getKey(), ptGraph);
		}
	}

	/**
	 * builds points-to graph for every invocation graph node in the invocation
	 * graph
	 * 
	 * @param basePtGraphs
	 */
	private void interProceduralAnalysis(
			Map<Function, PointsToGraph> basePtGraphs) {
		Map<InvocationGraphNode, FunctionCallInputs> nodeInputsTable = new HashMap<>();

		// traverse the invocation graph :
		analyzeInvocationGraphNode(rootNode, nodeInputsTable, basePtGraphs);
	}

	/**
	 * recursively do inter-procedural analysis for each invocation graph node
	 * 
	 * @param node
	 *            an invocation graph node
	 * @param nodeInputsTable
	 *            a table maps RECURSIVE nodes to the inputs (points-to set for
	 *            every possible input) shared by the set of their associated
	 *            APPROXIMATE nodes
	 * @param funcDefTable
	 *            a table maps {@link Function}s to its {@link PointsToGraph}
	 *            obtained at the intra-procedural analysis phase
	 */
	private void analyzeInvocationGraphNode(InvocationGraphNode node,
			Map<InvocationGraphNode, FunctionCallInputs> nodeInputsTable,
			Map<Function, PointsToGraph> funcDefTable) {
		PointsToGraph nodePtGraph = pointsToTable.get(node);
		boolean undone;

		if (nodePtGraph == null) {
			// base points-to graph of the node:
			nodePtGraph = funcDefTable.get(node.function()).clone();
			pointsToTable.put(node, nodePtGraph);
		}
		// repeatedly do computation until a fix-point is reached:
		do {
			undone = false;
			// process children (calls in this function):
			for (InvocationGraphNode child : node.children()) {
				switch (child.kind()) {
					case APPROXIMATE :
						analyzeInvocationGraphNode(child, nodeInputsTable,
								funcDefTable);
						undone |= analyzeApproximateNode(child, nodePtGraph,
								nodeInputsTable);
						break;
					case ORDINARY :
						analyzeInvocationGraphNode(child, nodeInputsTable,
								funcDefTable);
						undone |= analyzeOrdinaryNode(child, nodePtGraph);
						break;
					case RECURSIVE :
						analyzeInvocationGraphNode(child, nodeInputsTable,
								funcDefTable);
						undone |= analyzeRecursiveNode(child, nodePtGraph,
								nodeInputsTable);
						break;
					default :
						throw new ABCRuntimeException(
								"Un implemented: unknown invocation graph node kind "
										+ child.kind());
				}
			}
			if (!undone)
				for (InvocationGraphNode child : node.children()) {
					PointsToGraph childPtGraph = pointsToTable.get(child);

					undone |= computesImpactToCaller(child, childPtGraph,
							nodePtGraph);
				}
		} while (undone);
	}

	/**
	 * <p>
	 * process an APPROXIMATE node: If the {@link FunctionCallInputs} shared by
	 * all peer APPROXIMATE nodes have covered the inputs (points-to set of the
	 * actual parameters and global accesses of this node), the effect of this
	 * node to its caller can be computed. Otherwise, merging the inputs of this
	 * node to the shared {@link FunctionCallInputs}, delay the processing.
	 * </p>
	 * 
	 * @param node
	 *            the processing APPROXIMATE invocation graph node
	 * @param callerPtGraph
	 *            the points-to graph of the parent node of the processing node
	 * @param inputsTable
	 *            a table maps RECURSIVE nodes to their
	 *            {@link FunctionCallInputs} which is shared by the set of their
	 *            associated APPROXIMATE nodes
	 * @return true IFF analyzing this nodes 1) affects the points-to
	 *         information of its caller or 2) affects its
	 *         {@link FunctionCallInputs} which is shared by its APPROXIMATE
	 *         peers. In general, returning true means a fix-point is definitely
	 *         not reached.
	 */
	private boolean analyzeApproximateNode(InvocationGraphNode node,
			PointsToGraph callerPtGraph,
			Map<InvocationGraphNode, FunctionCallInputs> inputsTable) {
		InvocationGraphNode recurNode = node.getRecursive();
		FunctionCallInputs inputs = inputsTable.get(recurNode);
		boolean covered = false;

		if (inputs == null) {
			inputs = new FunctionCallInputs(node);
			inputsTable.put(recurNode, inputs);
		} else
			covered = coveredByInput(node, callerPtGraph, inputs);
		if (covered) {
			PointsToGraph recurPtGraph = pointsToTable.get(recurNode);

			// since the analysis goes DFS, recursive node must be processed
			// before approximate node, so its pointsToGraph shall not be null
			assert recurPtGraph != null;
			// computes the constraints:
			return computesImpactToCaller(node, recurPtGraph, callerPtGraph);
		} else {
			// merge my inputs to the shared inputs:
			mergeInputs(node, callerPtGraph, inputs);
			inputsTable.put(recurNode, inputs);
			return true;
		}
	}

	/**
	 * <p>
	 * computes the impact of an call associated with ordinary invocation graph
	 * node to its caller
	 * </p>
	 * 
	 * @param node
	 *            the processing ORDINARY invocation graph node
	 * @param callerPtGraph
	 *            the points-to graph of the parent node of the processing node
	 * @return true IFF analyzing this nodes affects the points-to information
	 *         of its caller, i.e. a fix point is definitely not reached
	 */
	private boolean analyzeOrdinaryNode(InvocationGraphNode node,
			PointsToGraph callerPtGraph) {
		PointsToGraph nodePtGraph = pointsToTable.get(node);

		return computesImpactToCaller(node, nodePtGraph, callerPtGraph);
	}

	/**
	 * <p>
	 * process a RECURSIVE kind invocation graph node: 1) if there is NO pending
	 * APPROXIMATE node associated with this node, merge the inputs and computes
	 * the impact to its caller; 2) if there is at least one pending APPROXIMATE
	 * node, merge inputs of APPROXIMATE nodes and add inputs to the points-to
	 * graph of the RECURSIVE node
	 * </p>
	 * 
	 * @param node
	 *            the process RECURSIVE invocation graph node
	 * @param callerPtGraph
	 *            the points-to graph of the parent of the processing node
	 * @param inputsTable
	 *            a table maps RECURSIVE nodes to their
	 *            {@link FunctionCallInputs} which is shared by the set of their
	 *            associated APPROXIMATE nodes
	 * @return true IFF 1) true IFF analyzing this nodes affects the points-to
	 *         information of its caller or 2) there are delayed APPROXIMATE
	 *         nodes. In general, returning true means a fix point is definitely
	 *         not reached.
	 */
	private boolean analyzeRecursiveNode(InvocationGraphNode node,
			PointsToGraph callerPtGraph,
			Map<InvocationGraphNode, FunctionCallInputs> inputsTable) {
		FunctionCallInputs inputs = inputsTable.get(node);
		PointsToGraph nodePtGraph = pointsToTable.get(node);

		// approximate shall have been recursively processed hence inputs shall
		// not be null
		assert inputs != null;
		if (!inputs.pending) {
			// computes the constraints:
			return computesImpactToCaller(node, nodePtGraph, callerPtGraph);
		} else {
			mergeInputs(node, callerPtGraph, inputs);

			int i = 0;

			// add inputs to points-to graph:
			for (AssignExprIF formal : node.formalParams())
				nodePtGraph.addPointsTo(formal, inputs.getParamInputs(i++));
			for (AssignExprIF global : node.accessedGlobals())
				nodePtGraph.addPointsTo(global, inputs.getGlobalInputs(global));
			return true;
		}
	}

	/**
	 * <p>
	 * Update a points-to graph of a function f in three aspects:
	 * <ol>
	 * <li>Adding the equivalence relation for every formal and actual parameter
	 * pair</li>
	 * <li>For every object that is
	 * {@link #reachable(AssignExprIF, PointsToGraph)} by an actual parameter
	 * w.r.t. the given caller graph, add the points-to relation of the
	 * reachable object to this points-to graph.</li>
	 * <li>For every global object that is visible from function f, update the
	 * the object w.r.t the given caller graph</li>
	 * </ol>
	 * </p>
	 * 
	 * <p>
	 * Given an invocation graph node representing a call
	 * <code>f(x, y, ...)</code> and a points-to graph associated with the node
	 * containing the points-to info of the function body of <code>f</code>,
	 * this methods builds the relations in between the actual parameters
	 * contained in the call and the formal parameters contained in the
	 * points-to graph.
	 * </p>
	 * 
	 * 
	 * @param node
	 *            a invocation node
	 * @param nodeGraph
	 *            the points-to graph of the function associated with the given
	 *            node
	 */
	private void updateNodeGraphWRTCaller(InvocationGraphNode node,
			PointsToGraph nodeGraph, PointsToGraph callerGraph) {
		int i = 0;

		// formals equivalent to actual argument
		for (AssignExprIF formal : node.formalParams()) {
			nodeGraph.addSubsetRelation(formal, node.actualParams()[i]);
			nodeGraph.addSubsetRelation(node.actualParams()[i++], formal);
		}
		// update info for reachable objects
		for (AssignExprIF actualArg : node.actualParams())
			for (Pair<AssignExprIF, Iterable<AssignExprIF>> reachable : reachable(
					actualArg, callerGraph)) {
				nodeGraph.addPointsTo(reachable.left, reachable.right);
			}
		// update for global accesses:
		for (AssignExprIF global : node.accessedGlobals())
			nodeGraph.addPointsTo(global, callerGraph.mayPointsTo(global));
	}

	/**
	 * <p>
	 * Given an {@link AssignExprIF} of (multi-level) pointer type, returns all
	 * the {@link AssignExprIF}s in the given {@link PointsToGraph} that are
	 * reachable from the pointer through (multiple times) dereference(s).
	 * </p>
	 * 
	 * @return a list of pairs of a (multiple-times) dereferenced pointer and
	 *         the reachable set of it. For example, it the given pointer p has
	 *         type <code>T**</code>, this method will return a pair with
	 *         associated with p and a pair associated with *p (assuming T is
	 *         not pointer type).
	 */
	private List<Pair<AssignExprIF, Iterable<AssignExprIF>>> reachable(
			AssignExprIF ptr, PointsToGraph ptGraph) {
		Type type = ptr.type();
		List<Pair<AssignExprIF, Iterable<AssignExprIF>>> result = new LinkedList<>();
		List<AssignExprIF> curr = new LinkedList<>();

		curr.add(ptr);
		while (type.kind() == TypeKind.POINTER) {
			PointerType ptrType = (PointerType) type;
			List<AssignExprIF> next = new LinkedList<>();

			for (AssignExprIF currPtr : curr) {
				Iterable<AssignExprIF> currPtrPts = ptGraph
						.mayPointsTo(currPtr);

				result.add(new Pair<>(currPtr, currPtrPts));
				for (AssignExprIF pointsTo : currPtrPts)
					next.add(pointsTo);
			}
			curr = next;
			type = ptrType.referencedType();
		}
		return result;
	}

	/**
	 * Given a invocation graph node, a points-to graph of the node, computes
	 * the impact of callee to the caller and returns true if the caller graph
	 * is changed.
	 * 
	 * @param node
	 *            a invocation node
	 * @param nodeGraph
	 *            the points-to graph of the function associated with the given
	 *            node
	 * @param callerGraph
	 *            the points-to graph of the function that calls the function
	 *            associated with the given node
	 * @return true iff at least one of the computed constraints is new to the
	 *         caller's points-to graph
	 */
	private boolean computesImpactToCaller(InvocationGraphNode node,
			PointsToGraph nodeGraph, PointsToGraph callerGraph) {
		boolean changed = false;
		Iterable<AssignExprIF> pts;

		// update node graph since caller graph may has changed:
		updateNodeGraphWRTCaller(node, nodeGraph, callerGraph);
		// actual:
		for (AssignExprIF actual : node.actualParams())
			changed |= computesImpactToCallerWorker(actual, node, nodeGraph,
					callerGraph, new HashSet<>());
		// globals:
		for (AssignExprIF global : node.accessedGlobals()) {
			pts = filterOutLocal(nodeGraph.mayPointsTo(global), node);
			changed |= callerGraph.addPointsTo(global, pts);
		}
		// returnings
		for (AssignExprIF returning : node.returnings()) {
			pts = filterOutLocal(nodeGraph.mayPointsTo(returning), node);
			changed |= callerGraph.addPointsTo(node.returnTo(), pts);
		}
		return changed;
	}

	// TODO: doc
	private boolean computesImpactToCallerWorker(AssignExprIF actualArgOrGlobal,
			InvocationGraphNode node, PointsToGraph nodeGraph,
			PointsToGraph callerGraph, Set<AssignExprIF> seen) {
		boolean changed = false;

		if (seen.contains(actualArgOrGlobal))
			return changed;
		seen.add(actualArgOrGlobal);
		for (AssignExprIF pt : callerGraph.mayPointsTo(actualArgOrGlobal)) {
			Iterable<AssignExprIF> pts = filterOutLocal(
					nodeGraph.mayPointsTo(pt), node);

			changed |= callerGraph.addPointsTo(pt, pts);
			computesImpactToCallerWorker(pt, node, nodeGraph, callerGraph,
					seen);
		}
		return changed;
	}

	/**
	 * Test if the inputs (i.e., global objects and actual parameters) of a node
	 * are contained by the {@link FunctionCallInputs}.
	 * 
	 * @param node
	 *            a invocation node
	 * @param callerGraph
	 *            the points-to graph of the caller function which contains the
	 *            function call represented by the given node
	 * @param inputs
	 *            the saved inputs of the given node
	 * @return true iff the inputs (computed by the caller graph) of the given
	 *         node are covered by the saved inputs.
	 */
	private boolean coveredByInput(InvocationGraphNode node,
			PointsToGraph callerGraph, FunctionCallInputs inputs) {
		int i = 0;

		// test parameters:
		for (AssignExprIF actualParam : node.actualParams())
			if (!inputs.paramCovers(i++, callerGraph.mayPointsTo(actualParam)))
				return false;
		// test global objects:
		for (AssignExprIF global : node.accessedGlobals())
			if (!inputs.globalCovers(global, callerGraph.mayPointsTo(global)))
				return false;
		return true;
	}

	/**
	 * Add the inputs, computed by the points-to graph of the caller function,
	 * of a node to its saved input
	 * 
	 * @param node
	 *            a invocation node
	 * @param callerGraph
	 *            the points-to graph of the caller function which contains the
	 *            function call represented by the given node
	 * @param inputs
	 *            the saved inputs of the given node
	 */
	private void mergeInputs(InvocationGraphNode node,
			PointsToGraph callerGraph, FunctionCallInputs inputs) {
		int i = 0;

		for (AssignExprIF actualParam : node.actualParams())
			inputs.addParamInputs(i++, callerGraph.mayPointsTo(actualParam));
		for (AssignExprIF global : node.accessedGlobals())
			inputs.addGlabalInputs(global, callerGraph.mayPointsTo(global));
	}

	/**
	 * filter out local objects
	 *
	 * @return
	 */
	private Iterable<AssignExprIF> filterOutLocal(Iterable<AssignExprIF> pts,
			InvocationGraphNode node) {
		List<AssignExprIF> results = new LinkedList<>();

		for (AssignExprIF pt : pts) {
			AssignExprIF root = pt.root();

			if (root.isFull())
				results.add(pt);
			else {
				// points-to graph shall not return auxiliary AssignExprIFs:
				assert root.kind() != AssignExprKind.AUX;

				AssignStoreExprIF store = (AssignStoreExprIF) root;

				if (store.isAllocation()) {
					results.add(pt);
					continue;
				}

				Variable var = store.variable();
				Scope outerScope = node.function().getScope();

				if (!isLocal(var, outerScope))
					results.add(pt);
			}
		}
		return results;
	}

	/**
	 * 
	 * @param var
	 * @param scope
	 * @return true iff the given scope or any of its descendant scopes has
	 *         "var" declared
	 */
	private boolean isLocal(Variable var, Scope scope) {
		for (DeclarationNode varDecl : var.getDeclarations()) {
			Scope varDeclScope = varDecl.getScope();

			while (varDeclScope != null) {
				if (varDeclScope == scope)
					return true;
				varDeclScope = varDeclScope.getParentScope();
			}
		}
		return false;
	}

	/* ****************** sub-classes ******************* */
	/**
	 * Instances of this classes aggregates inputs, i.e. points-to objects of
	 * formal parameters and global variables, of a RECURSIVE node and its
	 * APPROXIMATE descendants
	 * 
	 * @author xxxx
	 *
	 */
	private class FunctionCallInputs {
		/**
		 * points-to set of each formal parameter of a function:
		 */
		Map<AssignExprIF, Set<AssignExprIF>> globalInputsMap;

		ArrayList<Set<AssignExprIF>> formalInputsMap;

		boolean pending = false;

		FunctionCallInputs(InvocationGraphNode node) {
			globalInputsMap = new HashMap<>();
			formalInputsMap = new ArrayList<>(node.formalParams().length);
			for (int i = 0; i < node.formalParams().length; i++)
				formalInputsMap.add(new HashSet<>());
			for (AssignExprIF global : node.accessedGlobals())
				globalInputsMap.put(global, new HashSet<>());
		}

		boolean globalCovers(AssignExprIF global,
				Iterable<AssignExprIF> ptSet) {
			Set<AssignExprIF> inputs = this.globalInputsMap.get(global);

			for (AssignExprIF pt : ptSet)
				if (!inputs.contains(pt))
					return false;
			return true;
		}

		boolean paramCovers(int index, Iterable<AssignExprIF> ptSet) {
			Set<AssignExprIF> inputs = this.formalInputsMap.get(index);

			for (AssignExprIF pt : ptSet)
				if (!inputs.contains(pt))
					return false;
			return true;
		}

		Set<AssignExprIF> getParamInputs(int idx) {
			return this.formalInputsMap.get(idx);
		}

		Set<AssignExprIF> getGlobalInputs(AssignExprIF global) {
			return this.globalInputsMap.get(global);
		}

		void addParamInputs(int idx, Iterable<AssignExprIF> inputs) {
			for (AssignExprIF input : inputs)
				this.formalInputsMap.get(idx).add(input);
		}

		void addGlabalInputs(AssignExprIF global,
				Iterable<AssignExprIF> inputs) {
			for (AssignExprIF input : inputs)
				this.globalInputsMap.get(global).add(input);
		}
	}
}
