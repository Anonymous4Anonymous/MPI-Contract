package edu.udel.cis.vsl.abc.analysis.pointsTo.common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignAuxExprIF;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignExprIF;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignExprIF.AssignExprKind;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignFieldExprIF;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignOffsetExprIF;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignOffsetIF;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignStoreExprIF;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignSubscriptExprIF;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignmentIF;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignmentIF.AssignmentKind;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.InsensitiveFlow;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.InsensitiveFlowFactory;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.PointsToGraph;
import edu.udel.cis.vsl.abc.analysis.pointsTo.common.PointsToGraphComponentFactory.PointsToConstraint;
import edu.udel.cis.vsl.abc.ast.entity.IF.Variable;
import edu.udel.cis.vsl.abc.ast.type.IF.Field;
import edu.udel.cis.vsl.abc.ast.type.IF.Type.TypeKind;
import edu.udel.cis.vsl.abc.err.IF.ABCRuntimeException;
import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;

//TODO: since AssignExpIFs are now always canonicalized, no need to use symbolic expressions 
/**
 * <p>
 * An implementation of {@link PointsToGraph} which is based on the naive
 * algorithm presented in paper "The Ant and the Grasshopper: Fast and Accurate
 * Pointer Analysis for Millions of Lines of Code".
 * </p>
 * 
 * <p>
 * The naive algorithm in the paper does not take array elements and structs
 * into consideration. For array elements and struct fields, we do the following
 * extensions:
 * 
 * <ol>
 * <li>Generalization: The idea of generalization is from the paper
 * "Structure-Sensitive Points-To Analysis for C and C++". Briefly, it is based
 * on the core idea: When querying what a[*] points-to, what a[c] points to
 * shall be included. When querying what a[c] points-to, what a[*] points to
 * shall be included but not for a[c'] if <code>!c == c'</code></li>
 * 
 * <li>Offset Points-to Inference: Similar to the generation, for whatever U
 * points-to, what U + c or U + * point to shall be inferred during graph
 * building. see {@link #inferenceRules(Map)} and
 * {@link #partialOrderMerge(AssignOffsetExprIF)}</li>
 * 
 * <li>Array Points-to Inference: Offset points-to inference may generates new
 * abstract objects, for every new such object U, if it has array type, it is
 * known that U points to U[0]. see
 * {@link CommonPointsToGraph#subscriptIndexAddition(AssignSubscriptExprIF, AssignOffsetIF)}</li>
 * 
 * <li>Struct field Inference: for an arrow expression <code>p->id</code>, it is
 * presented as <code>let aux = *translate(p) in aux.id</code>. Struct field
 * Inference rule aims to build relation in between <code>aux</code> and
 * <code>aux.id</code>. Without such a rule, the points-to graph just take
 * <code>aux</code> and <code>aux.id</code> as two regular nodes.</li>
 * 
 * <li>De-Auxiliary abstract objects: the results of a query for what a pointer
 * points to shall contain no AUX kind abstract objects. AUX kind object can be
 * replaced by another abstract object U if U is reachable by AUX through the
 * edges. see {@link #deAux(AssignExprIF, Set)}.</li>
 * </ol>
 * </p>
 * 
 * 
 * @author xxxx
 *
 */
public class CommonPointsToGraph implements PointsToGraph {

	/**
	 * A set of FULL nodes in graph to the {@link AssignExprIF}s, from which
	 * nodes are created.
	 */
	private Map<SymbolicExpression, AssignExprIF> nodeToAssignExpr;

	/**
	 * The inverse map of {@link #node2assignExpr}
	 */
	private Map<AssignExprIF, SymbolicExpression> assignExprToNode;

	/**
	 * The points-to function that maps a node "n" to the nodes that "n" points
	 * to:
	 */
	private Map<SymbolicExpression, Set<SymbolicExpression>> pointsTo;

	/**
	 * all subset-of relations
	 */
	private Set<SymbolicExpression> allEdges;

	/**
	 * map for looking up subset-of relations by subsets:
	 */
	private Map<SymbolicExpression, List<SymbolicExpression>> subsetToEdge;

	/**
	 * a reference to the class providing nodes, edges and constraints:
	 */
	private PointsToGraphComponentFactory componentsFactory;

	/**
	 * a reference to {@link SymbolicUniverse}
	 */
	private SymbolicUniverse universe;

	/**
	 * the program fragment associated with this graph
	 */
	private InsensitiveFlow programAbstraction;

	/**
	 * true iff a re-computation is need before answering any
	 * {@link #mayPointsTo} queries
	 */
	private boolean dirty = false;

	CommonPointsToGraph(InsensitiveFlow programAbstraction,
			SymbolicUniverse universe) {
		this.universe = universe;
		this.componentsFactory = new PointsToGraphComponentFactory(universe);
		this.nodeToAssignExpr = new HashMap<>();
		this.assignExprToNode = new HashMap<>();
		this.pointsTo = new HashMap<>();
		this.allEdges = new HashSet<>();
		this.subsetToEdge = new HashMap<>();
		this.programAbstraction = programAbstraction;

		nodeToAssignExpr.put(componentsFactory.fullNode(),
				programAbstraction.insensitiveFlowfactory().full());
		assignExprToNode.put(programAbstraction.insensitiveFlowfactory().full(),
				componentsFactory.fullNode());
		build(programAbstraction, true);
	}

	@Override
	public PointsToGraph clone() {
		CommonPointsToGraph clone = new CommonPointsToGraph(
				this.programAbstraction, universe);

		clone.nodeToAssignExpr = new HashMap<>(nodeToAssignExpr);
		clone.assignExprToNode = new HashMap<>(assignExprToNode);
		clone.pointsTo = new HashMap<>();
		// deep copy:
		for (Entry<SymbolicExpression, Set<SymbolicExpression>> entry : pointsTo
				.entrySet()) {
			Set<SymbolicExpression> clonedPts = new TreeSet<>(
					universe.comparator());

			clonedPts.addAll(entry.getValue());
			clone.pointsTo.put(entry.getKey(), clonedPts);
		}

		clone.allEdges = new HashSet<>(allEdges);
		clone.subsetToEdge = new HashMap<>();
		// deep copy:
		for (Entry<SymbolicExpression, List<SymbolicExpression>> entry : subsetToEdge
				.entrySet())
			clone.subsetToEdge.put(entry.getKey(),
					new LinkedList<>(entry.getValue()));

		clone.dirty = this.dirty;
		return clone;
	}

	/*
	 * mayPointsTo invariant: any returned AssignExprIF must NOT contain any
	 * auxiliary AssignExprIF. see the {@link #deAux} method.
	 */

	@Override
	public Iterable<AssignExprIF> mayPointsTo(Variable root,
			List<Field> fields) {
		InsensitiveFlowFactory isf = programAbstraction
				.insensitiveFlowfactory();
		AssignExprIF key = isf.assignStoreExpr(root);

		for (Field field : fields)
			key = isf.assignFieldExpr(key, field);
		return mayPointsToWorker(key);
	}

	@Override
	public Iterable<AssignExprIF> mayPointsTo(AssignExprIF expr) {
		return mayPointsToWorker(expr);
	}

	@Override
	public boolean addPointsTo(AssignExprIF object,
			Iterable<AssignExprIF> objPointsTo) {
		SymbolicExpression node = getNodeByAssignExpr(object);
		Set<SymbolicExpression> pts = pointsTo.get(node);
		boolean changed = false;

		if (pts == null)
			pts = new HashSet<>();
		for (AssignExprIF ptsAbs : objPointsTo) {
			SymbolicExpression ptNode = getNodeByAssignExpr(ptsAbs);

			changed |= pts.add(ptNode);
		}
		savePointsTo(node, pts);
		dirty |= changed;
		return changed;
	}

	@Override
	public boolean addSubsetRelation(AssignExprIF superSet,
			AssignExprIF subSet) {
		SymbolicExpression supNode = getNodeByAssignExpr(superSet);
		SymbolicExpression subNode = getNodeByAssignExpr(subSet);
		SymbolicExpression edge = componentsFactory.edge(subNode, supNode);
		boolean changed = saveEdge(edge);

		dirty |= changed;
		return changed;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		for (AssignExprIF abs : nodeToAssignExpr.values()) {
			StringBuffer varSb = new StringBuffer();
			boolean hasFull = false;

			sb.append(abs + " points-to:\n");

			SymbolicExpression absNode = assignExprToNode.get(abs);

			if (pointsTo.get(absNode) == null)
				continue;
			for (SymbolicExpression ptNode : pointsTo.get(absNode)) {
				AssignExprIF pt = nodeToAssignExpr.get(ptNode);

				varSb.append("|| " + pt.toString() + "\n");
				if (pt.isFull()) {
					hasFull = true;
					break;
				}
			}
			if (hasFull)
				sb.append("|| FULL\n");
			else
				sb.append(varSb.toString());
			sb.append("\n");
		}
		return sb.toString();
	}

	/**
	 * <p>
	 * the common part of {@link #mayPointsTo(AssignExprIF)} and
	 * {@link #mayPointsTo(Variable, List)}.
	 * </p>
	 * 
	 * <p>
	 * This function shall not return any abstract objects that contains sub
	 * abstract objects of kind {@link AssignExprKind#AUX}.
	 * </p>
	 */
	private Iterable<AssignExprIF> mayPointsToWorker(AssignExprIF exprAbs) {
		if (dirty) {
			build(programAbstraction, false);
			dirty = false;
		}

		Set<AssignExprIF> results = new HashSet<>();

		if (exprAbs.kind() == AssignExprKind.OFFSET) {
			AssignOffsetExprIF oftExprAbs = (AssignOffsetExprIF) exprAbs;
			AssignExprIF base = oftExprAbs.base();
			AssignOffsetIF oft = oftExprAbs.offset();

			for (AssignExprIF basePt : mayPointsToWithGeneralization(base))
				if (basePt.kind() == AssignExprKind.SUBSCRIPT) {
					basePt = subscriptIndexAddition(
							(AssignSubscriptExprIF) basePt, oft);
					results.add(basePt);
				}
		}
		for (AssignExprIF pt : mayPointsToWithGeneralization(exprAbs))
			results.add(pt);
		return results;
	}

	/**
	 * 
	 * @return the "deaux"-ed points-to set of the given abstract object, as
	 *         well as the points-to sets of whatever is generalized from the
	 *         given abstract object. For generalization, see
	 *         {@link #generalize(AssignExprIF)}. For "deaux", see
	 *         {@link #deAux(AssignExprIF, Set)}
	 */
	private Iterable<AssignExprIF> mayPointsToWithGeneralization(
			AssignExprIF exprAbs) {
		Set<AssignExprIF> results = new HashSet<>();
		List<AssignExprIF> generalizeds = generalize(exprAbs);
		Set<SymbolicExpression> pts = new TreeSet<>(universe.comparator());

		// get all points-to set:
		for (AssignExprIF ptr : generalizeds)
			pts.addAll(allSubsetsPointsTo(ptr));
		if (pts.contains(componentsFactory.fullNode())) {
			results.add(programAbstraction.insensitiveFlowfactory().full());
			return results;
		}
		for (SymbolicExpression pt : pts)
			deAux(nodeToAssignExpr.get(pt), results);
		return results;
	}

	/**
	 * <p>
	 * For a given U, for every reachable U' from U (including U), return the
	 * union of the points-to sets of every abstract object that is generalized
	 * from U'.
	 * </p>
	 * 
	 * see {@link #allSubsets(SymbolicExpression, Set)} and
	 * {@link #generalize(AssignExprIF)}
	 */
	private Set<SymbolicExpression> allSubsetsPointsTo(AssignExprIF ptr) {
		SymbolicExpression ptrNode = assignExprToNode.get(ptr);
		Set<SymbolicExpression> subsetNodes = new TreeSet<>(
				universe.comparator());
		Set<SymbolicExpression> results = new TreeSet<>(universe.comparator());

		allSubsets(ptrNode, subsetNodes);
		subsetNodes.add(ptrNode);
		for (SymbolicExpression subsetNode : subsetNodes) {
			AssignExprIF subsetExpr = nodeToAssignExpr.get(subsetNode);
			List<AssignExprIF> generalizedExprs = generalize(subsetExpr);

			for (AssignExprIF generalizedExpr : generalizedExprs) {
				SymbolicExpression generalizedSubsetNode = assignExprToNode
						.get(generalizedExpr);
				Set<SymbolicExpression> tmp = pointsTo
						.get(generalizedSubsetNode);

				if (tmp != null)
					results.addAll(tmp);
			}
		}
		return results;
	}

	/* *********** methods for replacing AUX kind AssignExprIFs ***************/
	/**
	 * <p>
	 * For an abstract object that contains sub abstract objects of kind AUX,
	 * replace all AUX sub-objects with NON-AUX ones with the following basic
	 * idea: For a given abstract object U, recursively replace every AUX kind
	 * sub-object "aux" of U with every U' that is reachable from "aux" through
	 * edges.
	 * </p>
	 * 
	 * <p>
	 * An abstract object U is reachable from another one U' if the points-to
	 * set of U' is a subset of the points-to set of U. See
	 * {@link #allSubsets(SymbolicExpression, Set)}.
	 * </p>
	 * 
	 * @param expr
	 *            a abstract object whose sub-AUX-object will be replaced with
	 *            NON-AUX ones
	 * @param output
	 *            results of the replacements
	 */
	private void deAux(AssignExprIF expr, Set<AssignExprIF> output) {
		if (output.contains(expr))
			return;
		switch (expr.kind()) {
			case AUX :
				deAuxAux((AssignAuxExprIF) expr, output);
				break;
			case FIELD :
				deAuxField((AssignFieldExprIF) expr, output);
				break;
			case OFFSET :
				deAuxOffset((AssignOffsetExprIF) expr, output);
				break;
			case SUBSCRIPT :
				deAuxSubscript((AssignSubscriptExprIF) expr, output);
				break;
			case STORE : {
				output.add(expr);
				break;
			}
			default :
				throw new ABCRuntimeException("unreachable");
		}
	}

	/**
	 * For an AUX kind sub-objects: for every U that is reachable from it,
	 * recursively do replacement for U and return the result.
	 */
	private void deAuxAux(AssignAuxExprIF aux, Set<AssignExprIF> output) {
		Set<SymbolicExpression> subsets = new TreeSet<>(universe.comparator());

		allSubsets(getNodeByAssignExpr(aux), subsets);
		for (SymbolicExpression subNode : subsets) {
			AssignExprIF sub = nodeToAssignExpr.get(subNode);

			deAux(sub, output);
		}
	}

	/**
	 * For U.id, recursively do replacement for U.
	 */
	private void deAuxField(AssignFieldExprIF expr, Set<AssignExprIF> output) {
		InsensitiveFlowFactory isf = programAbstraction
				.insensitiveFlowfactory();
		AssignExprIF struct = expr.struct();
		Field field = expr.field();
		Set<AssignExprIF> deAuxStructs = new HashSet<>();

		deAux(struct, deAuxStructs);
		for (AssignExprIF deAuxStruct : deAuxStructs)
			output.add(isf.assignFieldExpr(deAuxStruct, field));
	}

	/**
	 * For U + oft, recursively do replacement for U.
	 */
	private void deAuxOffset(AssignOffsetExprIF expr,
			Set<AssignExprIF> output) {
		InsensitiveFlowFactory isf = programAbstraction
				.insensitiveFlowfactory();
		AssignExprIF base = expr.base();
		AssignOffsetIF oft = expr.offset();
		Set<AssignExprIF> deAuxBases = new HashSet<>();

		deAux(base, deAuxBases);
		for (AssignExprIF deAuxBase : deAuxBases)
			output.add(isf.assignOffsetExpr(deAuxBase, oft));
	}

	/**
	 * For U[oft], recursively do replacement for U.
	 */
	private void deAuxSubscript(AssignSubscriptExprIF expr,
			Set<AssignExprIF> output) {
		InsensitiveFlowFactory isf = programAbstraction
				.insensitiveFlowfactory();
		AssignExprIF array = expr.array();
		AssignOffsetIF idx = expr.index();
		Set<AssignExprIF> deAuxArrays = new HashSet<>();

		deAux(array, deAuxArrays);
		for (AssignExprIF deAuxArray : deAuxArrays)
			output.add(isf.assignSubscriptExpr(deAuxArray, idx));
	}
	/* ****************** methods compute subsets **********************/

	/**
	 * For an abstract object U, an abstract object U' points-to a subset of
	 * what U points-to if U' is reachable by U through edges. This method
	 * returns all reachable abstract objects from the one represented by the
	 * given graph node.
	 * 
	 * <b>output argument:</b> all reachable abstract objects from the one
	 * represented by the given graph node.
	 */
	private void allSubsets(SymbolicExpression node,
			Set<SymbolicExpression> output) {
		Set<SymbolicExpression> directSubsets = subsets(node);

		for (SymbolicExpression directSubset : directSubsets)
			if (output.add(directSubset))
				allSubsets(directSubset, output);
	}

	/**
	 * @param node
	 *            a graph node representing an abstract object
	 * 
	 * @return the abstract objects that are reachable from the one represented
	 *         by the given node in ONE STEP.
	 */
	Set<SymbolicExpression> subsets(SymbolicExpression node) {
		Set<SymbolicExpression> result = new TreeSet<>(universe.comparator());

		for (SymbolicExpression edge : allEdges) {
			if (componentsFactory.getSuperset(edge) != node)
				continue;

			SymbolicExpression subset = componentsFactory.getSubset(edge);

			result.add(subset);
		}
		return result;
	}

	/* ****************** Dynamic Transitive Closure *********************/
	private void build(Iterable<AssignmentIF> assignments,
			boolean firstTimeBuild) {
		Set<SymbolicExpression> workSet = new TreeSet<>(universe.comparator());
		Map<SymbolicExpression, List<PointsToConstraint>> subIndexedConstraints = new HashMap<>(),
				superIndexedConstraints = new HashMap<>();

		// initialization with BASE and SIMPLE:
		for (AssignmentIF assign : assignments) {
			if (assign.kind() == AssignmentKind.BASE)
				initializeBASE(assign, workSet);
			else if (assign.kind() == AssignmentKind.SIMPLE)
				initializeSIMPLE(assign, workSet);
			else
				initializeCOMPLEX(assign, workSet, subIndexedConstraints,
						superIndexedConstraints);
		}
		// if first time build, no need to add edge ends to work set since they
		// are already in:
		if (!firstTimeBuild)
			for (SymbolicExpression edge : allEdges) {
				workSet.add(componentsFactory.getSubset(edge));
				workSet.add(componentsFactory.getSuperset(edge));
			}

		FirstRemovableSet<SymbolicExpression> workList = new FirstRemovableSet<>(
				workSet);

		// field-inference may results in new elements in work list.
		// compute until a fixed point is reached:
		while (!workList.isEmpty()) {
			transClosureBuild(workList, subIndexedConstraints,
					superIndexedConstraints);
			assert workList.isEmpty();
			for (SymbolicExpression work : inferenceRules(
					superIndexedConstraints))
				workList.add(work);
		}
	}

	private void initializeBASE(AssignmentIF base,
			Set<SymbolicExpression> workSet) {
		// a = &b -> {b} sub-of a -> a points-to b
		SymbolicExpression lhsNode = getNodeByAssignExpr(base.lhs());
		SymbolicExpression rhsNode = getNodeByAssignExpr(base.rhs());
		Set<SymbolicExpression> ptSet = this.pointsTo.get(lhsNode);

		if (ptSet == null)
			ptSet = new TreeSet<>(universe.comparator());
		ptSet.add(rhsNode);
		savePointsTo(lhsNode, ptSet);
		workSet.add(lhsNode);
		workSet.add(rhsNode);
	}

	private void initializeSIMPLE(AssignmentIF simple,
			Set<SymbolicExpression> workSet) {
		// a = b -> b subset-of a
		SymbolicExpression lhsNode = getNodeByAssignExpr(simple.lhs());
		SymbolicExpression rhsNode = getNodeByAssignExpr(simple.rhs());
		SymbolicExpression edge = componentsFactory.edge(rhsNode, lhsNode);

		saveEdge(edge);
		workSet.add(lhsNode);
		workSet.add(rhsNode);
	}

	private void initializeCOMPLEX(AssignmentIF complex,
			Set<SymbolicExpression> workSet,
			Map<SymbolicExpression, List<PointsToConstraint>> subIndexedConstraints,
			Map<SymbolicExpression, List<PointsToConstraint>> superIndexedConstraints) {
		SymbolicExpression lhsNode = getNodeByAssignExpr(complex.lhs());
		SymbolicExpression rhsNode = getNodeByAssignExpr(complex.rhs());

		workSet.add(lhsNode);
		workSet.add(rhsNode);

		PointsToConstraint constraint;

		if (complex.kind() == AssignmentKind.COMPLEX_LD) {
			// *a = b -> b subset-of *a
			constraint = componentsFactory.newConstraint(true, rhsNode,
					lhsNode);
			updateEdgesWithConstraints(constraint);
		} else {
			// a = *b -> *b subset-of a:
			constraint = componentsFactory.newConstraint(false, rhsNode,
					lhsNode);
			updateEdgesWithConstraints(constraint);
		}

		List<PointsToConstraint> tmp = subIndexedConstraints
				.get(constraint.subset());

		if (tmp == null)
			tmp = new LinkedList<>();
		tmp.add(constraint);
		subIndexedConstraints.put(constraint.subset(), tmp);
		tmp = superIndexedConstraints.get(constraint.superset());
		if (tmp == null)
			tmp = new LinkedList<>();
		tmp.add(constraint);
		superIndexedConstraints.put(constraint.superset(), tmp);
	}

	/**
	 * algorithm from "The Ant and the Grasshopper: Fast and Accurate Pointer
	 * Analysis for Millions of Lines of Code", Figure 1
	 * 
	 * @param workList
	 */
	private void transClosureBuild(
			FirstRemovableSet<SymbolicExpression> workList,
			Map<SymbolicExpression, List<PointsToConstraint>> subIndexedConstraints,
			Map<SymbolicExpression, List<PointsToConstraint>> superIndexedConstraints) {
		while (!workList.isEmpty()) {
			SymbolicExpression node = workList.removeFirst();
			Set<SymbolicExpression> pts;

			pts = pointsTo.get(node);
			if (pts != null)
				for (SymbolicExpression pt : pts) {
					// process constraints for complex_rd:
					Iterable<PointsToConstraint> constraints = subIndexedConstraints
							.get(node);

					if (constraints != null)
						for (PointsToConstraint constraint : constraints) {
							// for every *b subset-of a
							if (constraint.isSuperDeref())
								continue;

							SymbolicExpression ptSubsetofSuper = componentsFactory
									.edge(pt, constraint.superset());

							if (!allEdges.contains(ptSubsetofSuper)) {
								workList.add(pt);
								saveEdge(ptSubsetofSuper);
							}
						}
					// process constraints for complex_ld:
					constraints = superIndexedConstraints.get(node);
					if (constraints != null)
						for (PointsToConstraint constraint : constraints) {
							// for every b subset-of *a
							if (!constraint.isSuperDeref())
								continue;

							SymbolicExpression subsetofPt = componentsFactory
									.edge(constraint.subset(), pt);

							if (!allEdges.contains(subsetofPt)) {
								workList.add(constraint.subset());
								saveEdge(subsetofPt);
							}
						}
				}

			// for every superset of "pt", add what "pt" points-to to the
			// superset:
			List<SymbolicExpression> edges = subsetToEdge.get(node);

			if (edges != null) {
				SymbolicExpression[] currEdges = new SymbolicExpression[edges
						.size()];

				// the following loop may modify "edges" hence use another array
				// for iteration:
				edges.toArray(currEdges);
				for (SymbolicExpression edge : currEdges) {
					SymbolicExpression superNode = componentsFactory
							.getSuperset(edge);
					Set<SymbolicExpression> superPts = pointsTo.get(superNode);
					Set<SymbolicExpression> nodePts = pointsTo.get(node);

					if (superPts == null)
						superPts = new TreeSet<>(universe.comparator());
					if (nodePts != null)
						if (superPts.addAll(nodePts))
							workList.add(superNode);
					savePointsTo(superNode, superPts);
					// update edges with constraints that involve the
					// "superNode" since its points-to function is updated:
					List<PointsToConstraint> tmp = subIndexedConstraints
							.get(superNode);

					if (tmp != null)
						for (PointsToConstraint cons : tmp)
							if (!cons.isSuperDeref())
								// only for *superNode subset-of a:
								for (SymbolicExpression toWorkset : updateEdgesWithConstraints(
										cons))
									workList.add(toWorkset);
					tmp = superIndexedConstraints.get(superNode);
					if (tmp != null)
						for (PointsToConstraint cons : tmp)
							if (cons.isSuperDeref())
								// only for a subset-of *superNode:
								for (SymbolicExpression toWorkset : updateEdgesWithConstraints(
										cons))
									workList.add(toWorkset);
				}
			}
		}
	}

	/**
	 * <p>
	 * As described in the paper that new edges will be added when points-to
	 * sets, which are associated with constraints, are updated: "As we update
	 * the points-to sets, we must also add new edges to represent the complex
	 * constraints. "
	 * </p>
	 * 
	 * <p>
	 * Remark that for every new edge generated in this method, the subset
	 * component of the edge will be in the returned set. If there is anything
	 * new in the points-to set of the subset component, the points-to set of
	 * the superset component shall be updated. Hence, during graph building,
	 * the subset components shall be added back to work list.
	 * </p>
	 * 
	 * @param constraint
	 * @return a set of nodes that need to be added back to work set
	 */
	private Set<SymbolicExpression> updateEdgesWithConstraints(
			PointsToConstraint constraint) {
		Set<SymbolicExpression> nodesGoWorkSet = new TreeSet<>(
				universe.comparator());

		if (constraint.isSuperDeref()) {
			// if b subset-of *a, add edges b -> pts(a), and add b
			// to work set if b -> pt is a new edge:
			Set<SymbolicExpression> allSuperPointsTo = pointsTo
					.get(constraint.superset());

			if (allSuperPointsTo != null)
				for (SymbolicExpression superPointsTo : allSuperPointsTo) {
					SymbolicExpression edge = componentsFactory
							.edge(constraint.subset(), superPointsTo);

					if (saveEdge(edge))
						nodesGoWorkSet.add(constraint.subset());
				}
		} else {
			// if *b subset-of a, add edges pts(b) -> a, and add every pt in
			// pts(b) to work set if pt -> a is a new edge:
			Set<SymbolicExpression> allSubPointsTo = pointsTo
					.get(constraint.subset());

			if (allSubPointsTo != null)
				for (SymbolicExpression subPointsTo : allSubPointsTo) {
					SymbolicExpression edge = componentsFactory
							.edge(subPointsTo, constraint.superset());

					if (saveEdge(edge))
						nodesGoWorkSet.add(subPointsTo);
				}
		}
		return nodesGoWorkSet;
	}

	/**
	 * <p>
	 * updates the points-to info of the given "pointer" in {@link #pointsTo} to
	 * the given "pointsToSet"
	 * </p>
	 * 
	 * @param pointer
	 * @param pointsToSet
	 * 
	 * @return true if the update of the points-to info causes the update of
	 *         OTHER pointers' points-to info
	 */
	private void savePointsTo(SymbolicExpression pointer,
			Set<SymbolicExpression> pointsToSet) {
		pointsTo.put(pointer, pointsToSet);
	}

	/* ******************** methods for filed inference **********************/
	/**
	 * 
	 * <p>
	 * Two inference rules are applied during graph building until a fixed point
	 * is reached:
	 * 
	 * <b>field inference:</b> An arrow expression <code>expr->id</code> is
	 * represented in an insensitive flow with at least one auxiliary abstract
	 * object: <code>
	 * let aux = * translate(expr) in aux.id
	 * </code>. Field inference builds connections between <code>aux</code> and
	 * <code>aux.id</code> during the build of the points-to graph because in
	 * the perspective of the graph <code>aux</code> and <code>aux.id</code> are
	 * two regular nodes without any connection.<br>
	 * 
	 * 
	 * <b>offset inference:</b> see
	 * {@link #partialOrderMerge(AssignOffsetExprIF)} for more details
	 * </p>
	 * 
	 * <p>
	 * </p>
	 * 
	 * @param superIndexedConstraints
	 * @return
	 */
	private Set<SymbolicExpression> inferenceRules(
			Map<SymbolicExpression, List<PointsToConstraint>> superIndexedConstraints) {
		Set<SymbolicExpression> workList = new TreeSet<SymbolicExpression>(
				universe.comparator());
		Set<AssignExprIF> seenExprs = new HashSet<>(assignExprToNode.keySet());

		for (AssignExprIF expr : seenExprs) {
			if (expr.kind() == AssignExprKind.OFFSET) {
				if (partialOrderMerge((AssignOffsetExprIF) expr))
					workList.add(getNodeByAssignExpr(expr));
				continue;
			} else if (expr.kind() != AssignExprKind.FIELD)
				continue;

			AssignFieldExprIF field = (AssignFieldExprIF) expr;
			AssignExprIF struct = field.struct();
			SymbolicExpression structNode = getNodeByAssignExpr(struct);
			List<PointsToConstraint> constraints = superIndexedConstraints
					.get(structNode);

			if (constraints == null)
				continue;
			for (PointsToConstraint constraint : constraints) {
				if (constraint.isSuperDeref())
					continue;
				workList.addAll(fieldAccessInferenceWorker(constraint, field));
			}
		}
		return workList;
	}

	/**
	 * with constraint <code>a super-set-of *x</code>, for any v pointed by x,
	 * add <code>a.id super-set-of v.id</code> to the edge set
	 * 
	 * @param constraint
	 *            the constraint "struct superset-of * x"
	 * @param fieldAccess
	 *            the expression: struct.id
	 * @return
	 */
	private Set<SymbolicExpression> fieldAccessInferenceWorker(
			PointsToConstraint constraint, AssignFieldExprIF fieldAccess) {
		SymbolicExpression x = constraint.subset();
		Set<SymbolicExpression> ptsX = pointsTo.get(x);
		Set<SymbolicExpression> workList = new TreeSet<>(universe.comparator());

		if (ptsX == null)
			return new TreeSet<>(universe.comparator());
		for (SymbolicExpression xPt : ptsX) {
			// struct.id superset-of xPt.id
			fieldInferencedEdge(fieldAccess, xPt, workList);
		}
		return workList;
	}

	private void fieldInferencedEdge(AssignFieldExprIF fieldAccess,
			SymbolicExpression ptNode, Set<SymbolicExpression> workList) {
		// struct.id superset-of xPt.id
		AssignExprIF pt = nodeToAssignExpr.get(ptNode);
		AssignFieldExprIF ptId = programAbstraction.insensitiveFlowfactory()
				.assignFieldExpr(pt, fieldAccess.field());
		SymbolicExpression ptIdNode = getNodeByAssignExpr(ptId);
		SymbolicExpression fieldAccessNode = getNodeByAssignExpr(fieldAccess);

		// if the edge is new, add xPt.id into work-set
		if (saveEdge(componentsFactory.edge(ptIdNode, fieldAccessNode)))
			workList.add(ptIdNode);
		if (saveEdge(componentsFactory.edge(fieldAccessNode, ptIdNode)))
			workList.add(fieldAccessNode);
	}

	/* ******************** Partial Order Merge *****************************/
	/**
	 * <p>
	 * Merge points-to sets of different nodes with the following idea: <code>
	 * update every pts(U + c) with
	 * ||for v[c'] in pts(U)
	 * ||||v[c' + c] in pts(U + c)
	 *
	 * update every pts(U + c) with
	 * ||for v[*] in pts(U)
	 * ||||v[*] in pts(U + c)
	 *
	 * update every pts(U + *) with
	 * ||for v[*|c] in pts(U)
	 * ||||v[*] in pts(U + *)
	 * </code>
	 * </p>
	 * 
	 * <p>
	 * No need to do the way in the opposite direction, i.e., compute pts(U)
	 * from pts(U + [c|*]) according to my hypothesis : pts(U + C) can never
	 * contain more elements than pts(U) because U + c can never be left-hand
	 * side expression.
	 * </p>
	 */
	private boolean partialOrderMerge(AssignOffsetExprIF offsetExpr) {
		AssignExprIF base = offsetExpr.base();
		SymbolicExpression baseNode = getNodeByAssignExpr(base);
		SymbolicExpression offsetNode = getNodeByAssignExpr(offsetExpr);
		Set<SymbolicExpression> ptsBase = pointsTo.get(baseNode);
		Set<SymbolicExpression> ptsOffset = pointsTo.get(offsetNode);
		boolean changed = false;

		if (ptsBase == null)
			return false;
		if (ptsOffset == null)
			ptsOffset = new TreeSet<>(universe.comparator());
		for (SymbolicExpression basePtNode : ptsBase) {
			AssignExprIF basePt = nodeToAssignExpr.get(basePtNode);

			if (basePt.kind() == AssignExprKind.SUBSCRIPT) {
				AssignExprIF basePtOfs = subscriptIndexAddition(
						(AssignSubscriptExprIF) basePt, offsetExpr.offset());

				changed |= ptsOffset.add(getNodeByAssignExpr(basePtOfs));
			}
		}
		if (changed)
			savePointsTo(offsetNode, ptsOffset);
		return changed;
	}

	/**
	 * <p>
	 * Given a abstract object <code>U[c|*]</code> and an offset
	 * <code>[c'|*]</code>,
	 * 
	 * <ol>
	 * <li>return <code>U[x], where x = c + c' or *</code></li>
	 * <li>if U[x] is a new node and has array type, inferencing what
	 * <code>U[x]</code> points-to. see
	 * {@link #subscriptInferredPointsTo(AssignSubscriptExprIF, InsensitiveFlowFactory)}</li>
	 * </ol>
	 * </p>
	 */
	private AssignSubscriptExprIF subscriptIndexAddition(
			AssignSubscriptExprIF subscript, AssignOffsetIF oft) {
		if (!subscript.index().hasConstantValue())
			return subscript;
		AssignSubscriptExprIF result;
		InsensitiveFlowFactory isf = programAbstraction
				.insensitiveFlowfactory();

		if (!oft.hasConstantValue())
			result = isf.assignSubscriptExpr(subscript.array(), oft);
		else {
			Integer val = subscript.index().constantValue()
					+ oft.constantValue();

			result = isf.assignSubscriptExpr(subscript.array(),
					isf.assignOffset(val));
		}
		subscriptInferredPointsTo(result, isf);
		return result;
	}

	/**
	 * For an abstract object <code>U[x]</code>, if it has array type,
	 * recursively inferencing what <code>U[x]</code> points to, i.e.,
	 * <code>U[x]</code> points to <code>U[x][0]</code>
	 */
	private void subscriptInferredPointsTo(AssignSubscriptExprIF subscript,
			InsensitiveFlowFactory isf) {
		if (subscript.type().kind() == TypeKind.ARRAY) {
			AssignSubscriptExprIF subscriptPt = isf
					.assignSubscriptExpr(subscript, isf.assignOffsetZero());
			SymbolicExpression ptNode = getNodeByAssignExpr(subscriptPt);
			SymbolicExpression node = getNodeByAssignExpr(subscript);
			Set<SymbolicExpression> nodePts = pointsTo.get(node);

			if (nodePts == null)
				nodePts = new TreeSet<>(universe.comparator());
			nodePts.add(ptNode);
			this.savePointsTo(node, nodePts);
			this.subscriptInferredPointsTo(subscriptPt, isf);
		}
	}

	/* ******************** Generalization **********************/
	/**
	 * <p>
	 * the generalization is based on the rules in paper "Structure-Sensitive
	 * Points-To Analysis for C and C++". See the doc of this class above for
	 * more details.
	 * </p>
	 * 
	 */
	private List<AssignExprIF> generalize(AssignExprIF expr) {
		Generalizer pred = new Generalizer(expr);
		List<AssignExprIF> candidates = new LinkedList<>(
				nodeToAssignExpr.values());

		return candidates.parallelStream().filter(pred)
				.collect(Collectors.toList());
	}

	private class Generalizer implements Predicate<AssignExprIF> {
		private AssignExprIF generalizer;

		Generalizer(AssignExprIF generalizer) {
			this.generalizer = generalizer;
		}

		@Override
		public boolean test(AssignExprIF t) {
			return generalize(generalizer, t);
		}

		private boolean generalize(AssignExprIF er, AssignExprIF ee) {
			switch (er.kind()) {
				case AUX :
					return er == ee;
				case FIELD : {
					if (er.kind() != ee.kind())
						return false;
					AssignFieldExprIF fieldEr = (AssignFieldExprIF) er;
					AssignFieldExprIF fieldEe = (AssignFieldExprIF) ee;

					if (fieldEr.field() == fieldEe.field())
						return generalize(fieldEr.struct(), fieldEe.struct());
					return false;
				}
				case OFFSET : {
					AssignOffsetExprIF offsetEr = (AssignOffsetExprIF) er;
					return generalizeByOffset(offsetEr, ee);
				}
				case STORE : {
					if (er.kind() != ee.kind()) // a cannot generalize a + *
						return false;
					AssignStoreExprIF storeEr = (AssignStoreExprIF) er;
					AssignStoreExprIF storeEe = (AssignStoreExprIF) ee;

					if (storeEr.isAllocation())
						return storeEr.store() == storeEe.store();
					else
						return storeEr.variable() == storeEe.variable();
				}
				case SUBSCRIPT : {
					if (er.kind() != ee.kind())
						return false;
					AssignSubscriptExprIF subscriptEr = (AssignSubscriptExprIF) er;
					AssignSubscriptExprIF subscriptEe = (AssignSubscriptExprIF) ee;
					if (generalize(subscriptEr.array(), subscriptEe.array()))
						return offsetGeneralize(subscriptEr.index(),
								subscriptEe.index());
					return false;
				}
				default :
					throw new ABCRuntimeException("unreachable");
			}
		}

		private boolean generalizeByOffset(AssignOffsetExprIF er,
				AssignExprIF ee) {
			if (ee.kind() == AssignExprKind.OFFSET) {
				AssignOffsetExprIF offsetEe = (AssignOffsetExprIF) ee;

				return generalize(er.base(), offsetEe.base()) && this
						.offsetGeneralize(er.offset(), offsetEe.offset());
			} else if (!er.offset().hasConstantValue())
				return generalize(er.base(), ee);
			else
				return false;
		}

		private boolean offsetGeneralize(AssignOffsetIF er, AssignOffsetIF ee) {
			if (er.hasConstantValue())
				return ee.hasConstantValue()
						&& er.constantValue().equals(ee.constantValue());
			return true;
		}
	}

	/* ************ utils *********** */

	/**
	 * 
	 * @param expr
	 *            an {@link AssignExprIF}
	 * @return the node associated with the given AssignExprIF
	 */
	private SymbolicExpression getNodeByAssignExpr(AssignExprIF expr) {
		if (expr.isFull())
			return componentsFactory.fullNode();

		SymbolicExpression node = assignExprToNode.get(expr);

		if (node != null)
			return node;

		node = this.componentsFactory.newNode();
		this.assignExprToNode.put(expr, node);
		this.nodeToAssignExpr.put(node, expr);
		return node;
	}

	/**
	 * Add an edge to graph
	 * 
	 * @param edge
	 * @return true iff the edge was not saved before
	 */
	private boolean saveEdge(SymbolicExpression edge) {
		SymbolicExpression sub = componentsFactory.getSubset(edge);
		List<SymbolicExpression> edgesSharingSub = subsetToEdge.get(sub);
		boolean notExist = allEdges.add(edge);

		if (edgesSharingSub == null)
			edgesSharingSub = new LinkedList<>();
		edgesSharingSub.add(edge);
		subsetToEdge.put(sub, edgesSharingSub);
		return notExist;
	}

	private class FirstRemovableSet<T> {

		private Set<T> set;

		private LinkedList<T> list;
		FirstRemovableSet(Set<T> ts) {
			set = new HashSet<>(ts);
			list = new LinkedList<>(ts);
		}

		boolean isEmpty() {
			return list.isEmpty();
		}

		void add(T t) {
			if (set.add(t))
				list.addLast(t);
		}

		T removeFirst() {
			T t = list.removeFirst();
			boolean mustBeTrue = set.remove(t);

			assert mustBeTrue;
			return t;
		}

		@Override
		public String toString() {
			return list.toString();
		}
	}
}
