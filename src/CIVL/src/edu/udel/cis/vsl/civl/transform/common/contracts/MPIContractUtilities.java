package edu.udel.cis.vsl.civl.transform.common.contracts;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode.NodeKind;
import edu.udel.cis.vsl.abc.ast.node.IF.NodeFactory;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.MPIContractExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.MPIContractExpressionNode.MPIContractExpressionKind;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.FunctionDefinitionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.OrdinaryDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.OrdinaryDeclarationNode.OrdinaryDeclarationKind;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.VariableDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.CastNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode.ExpressionKind;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.FunctionCallNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.IdentifierExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode.Operator;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.BlockItemNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.BlockItemNode.BlockItemKind;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.CompoundStatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.ExpressionStatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.ForLoopInitializerNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.IfNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.StatementNode;
import edu.udel.cis.vsl.abc.token.IF.Source;
import edu.udel.cis.vsl.civl.transform.common.BaseWorker;
import edu.udel.cis.vsl.civl.util.IF.Pair;

public class MPIContractUtilities {

	static private MPIContractUtilities singleInstance = null;

	/* **************** Intermediate variable names: *****************/

	static final String CIVL_CONTRACT_PREFIX = "_cc";

	static final String COLLATE_STATE_PREFIX = CIVL_CONTRACT_PREFIX + "_cs";

	static final String PRE_COLLATE_STATE = COLLATE_STATE_PREFIX + "_pre";

	static final String POST_COLLATE_STATE = COLLATE_STATE_PREFIX + "_post";

	static final String ASSIGN_VAR_PREFIX = CIVL_CONTRACT_PREFIX + "_assign_";

	static final String HEAP_VAR_PREFIX = CIVL_CONTRACT_PREFIX + "_heap_";

	static final String EXTENT_VAR_PREFIX = CIVL_CONTRACT_PREFIX + "_extent_";

	static final String BOUND_VAR_PREFIX = CIVL_CONTRACT_PREFIX + "_bv_";

	static final String POINTER_VAR_PREFIX = CIVL_CONTRACT_PREFIX + "_ptr_";

	/* **************** Artificial identifier names: *****************/

	static final String MPI_COMM_RANK_CONST = "$mpi_comm_rank";

	static final String MPI_COMM_SIZE_CONST = "$mpi_comm_size";

	static final String MPI_COMM_RANK_CALL = "MPI_Comm_rank";

	static final String MPI_COMM_SIZE_CALL = "MPI_Comm_size";

	static final String MPI_BARRIER_CALL = "MPI_Barrier";

	static final String MPI_SNAPSHOT = "$mpi_snapshot";

	static final String MPI_UNSNAPSHOT = "$mpi_unsnapshot";

	static final String MPI_ASSIGNS = "$mpi_assigns";

	static final String MPI_SIZEOF_DATATYPE = "sizeofDatatype";

	static final String MPI_CHECK_EMPTY_COMM = "$mpi_comm_empty";

	final static String MPI_COMM_WORLD = "MPI_COMM_WORLD";
	
	final static String MPI_IN_PLACE_SPOT = "MPI_IN_PLACE_SPOT";

	static final String MEMCPY_CALL = "memcpy";

	static final String COLLATE_COMPLETE = "$collate_complete";

	static final String COLLATE_ARRIVED = "$collate_arrived";

	static final String COLLATE_PRE_STATE = "_cs_pre";

	static final String PRE_STATE = "_s_pre";

	static final String COLLATE_POST_STATE = "_cs_post";

	static final String STATE_NULL = "$state_null";

	static final String PROC_NULL = "$proc_null";

	static final String COLLATE_STATE_TYPE = "$collate_state";

	static final String COLLATE_GET_STATE_CALL = "$collate_get_state";

	static final String REGULAR_GET_STATE_CALL = "$get_state";

	static final String COLLATE_SNAPSHOT = "$collate_snapshot";

	static final String ACSL_RESULT_VAR = "$result";

	static final String HAVOC = "$havoc";

	static final String MEM_HAVOC = "$mem_havoc";

	static ExpressionNode getStateNullExpression(Source source,
			NodeFactory nodeFactory) {
		return nodeFactory.newStatenullNode(source);
	}

	static ExpressionNode getProcNullExpression(Source source,
			NodeFactory nodeFactory) {
		return nodeFactory.newProcnullNode(source);
	}

	static String nextAssignName(int counter) {
		return ASSIGN_VAR_PREFIX + counter;
	}

	static String nextAllocationName(int count) {
		return HEAP_VAR_PREFIX + count;
	}

	static String nextExtentName(int count) {
		return EXTENT_VAR_PREFIX + count;
	}

	static String nextPointerName(int count) {
		return POINTER_VAR_PREFIX + count;
	}

	/**
	 * Return a new instance of {@link MemoryAllocation}.
	 * 
	 * @param freshNewValues
	 *            see
	 *            {@linkplain UnconstrainedMemoryAssignment#memoryDefinitions}
	 * @param refreshMemoryLocations
	 *            see
	 *            {@linkplain UnconstrainedMemoryAssignment#assignMemoryReferences}
	 * @return
	 */
	static MemoryAllocation newMemoryAllocation(
			List<BlockItemNode> memoryDefinitions,
			List<BlockItemNode> assignMemoryReferences) {
		if (singleInstance == null)
			singleInstance = new MPIContractUtilities();
		return singleInstance.new MemoryAllocation(memoryDefinitions,
				assignMemoryReferences);
	}

	/**
	 * Return a new instance of {@link MemoryAllocation}.
	 * 
	 * @param freshNewValues
	 *            see
	 *            {@linkplain UnconstrainedMemoryAssignment#memoryDefinitions}
	 * @param refreshMemoryLocations
	 *            see
	 *            {@linkplain UnconstrainedMemoryAssignment#assignMemoryReferences}
	 * @return
	 */
	static MemoryAllocation newMemoryAllocation(
			List<BlockItemNode> memoryDefinitions,
			StatementNode assignMemoryReferences) {
		if (singleInstance == null)
			singleInstance = new MPIContractUtilities();

		List<BlockItemNode> newList = new LinkedList<>();

		newList.add(assignMemoryReferences);
		return singleInstance.new MemoryAllocation(memoryDefinitions, newList);
	}

	/**
	 * <p>
	 * Expecting the inputs satisfy the following conditions:
	 * <li>1. if binder is null, predicate and stmts are independent of the
	 * binder</li>
	 * <li>2. if binder is non-null, predicate expresses lower and upper bounds
	 * on the binded variable.</li>
	 * </p>
	 * 
	 * If binder is null, then <code>
	 *  if (predicate) {
	 *    stmts;
	 *  }
	 * </code>, where all given nodes shall have no parent. Otherwise, $for
	 * (binder: lo .. hi) { stmts; }
	 * 
	 * @param predicate
	 * @param stmts
	 * @param stmtsSource
	 * @param nodeFactory
	 * @return
	 */
	static StatementNode makeGuardedByRestriction(
			VariableDeclarationNode binder, ExpressionNode predicate,
			List<BlockItemNode> stmts, Source stmtsSource, NodeFactory nf) {
		StatementNode compound = nf.newCompoundStatementNode(stmtsSource,
				stmts);
		if (binder == null)
			return nf.newIfNode(predicate.getSource(), predicate.copy(),
					compound);

		ExpressionNode inclLower = extractLower(predicate);
		ExpressionNode exclHigher = extractHigher(predicate);
		ExpressionNode incrementor = nf.newOperatorNode(binder.getSource(),
				Operator.POSTINCREMENT, nf.newIdentifierExpressionNode(
						binder.getSource(), binder.getIdentifier().copy()));
		List<VariableDeclarationNode> loopInitializers = new LinkedList<>();

		binder = binder.copy();
		binder.setInitializer(inclLower.copy());
		loopInitializers.add(binder);

		ForLoopInitializerNode forloopInitializer = nf
				.newForLoopInitializerNode(inclLower.getSource(),
						loopInitializers);
		ExpressionNode higherBoundClause = (ExpressionNode) exclHigher.parent()
				.copy();

		return nf.newForLoopNode(stmtsSource, forloopInitializer,
				higherBoundClause, incrementor, compound, null);
	}

	static void replaceMPICommRankAndSizeForall(ASTNode root, NodeFactory nf) {
		while (root != null) {
			if (root.nodeKind() == NodeKind.EXPRESSION) {
				ExpressionNode expr = (ExpressionNode) root;

				if (expr.expressionKind() == ExpressionKind.MPI_CONTRACT_EXPRESSION) {
					MPIContractExpressionNode mpiExpr = (MPIContractExpressionNode) expr;

					if (mpiExpr
							.MPIContractExpressionKind() == MPIContractExpressionKind.MPI_COMM_RANK) {
						int childIdx = mpiExpr.childIndex();
						ASTNode parent = mpiExpr.parent();
						ExpressionNode replacee = nf
								.newIdentifierExpressionNode(
										mpiExpr.getSource(),
										nf.newIdentifierNode(
												mpiExpr.getSource(),
												MPI_COMM_RANK_CONST));

						root = mpiExpr.nextDFS();
						mpiExpr.remove();
						parent.setChild(childIdx, replacee);
						continue;
					} else if (mpiExpr
							.MPIContractExpressionKind() == MPIContractExpressionKind.MPI_COMM_SIZE) {
						int childIdx = mpiExpr.childIndex();
						ASTNode parent = mpiExpr.parent();
						ExpressionNode replacee = nf
								.newIdentifierExpressionNode(
										mpiExpr.getSource(),
										nf.newIdentifierNode(
												mpiExpr.getSource(),
												MPI_COMM_SIZE_CONST));

						root = mpiExpr.nextDFS();
						mpiExpr.remove();
						parent.setChild(childIdx, replacee);
						continue;
					}
				}
			}
			root = root.nextDFS();
		}
	}

	/**
	 * <p>
	 * a transformed <code>\valid(p + ...)</code> has the form <code>p ==
	 * _cc_heap_x<code>, where x is some name suffix.
	 * </p>
	 * 
	 * <p>
	 * For an $assume(expr) statement, if "p == _cc_heap_x" is a conjunctive
	 * clause of expr, transform it to an assignment "p = _cc_heap_x;" before
	 * the $assume(expr) statement.
	 * </p>
	 * 
	 * @param root
	 * @param nf
	 */
	static void workaroundForTransformedValid(
			ASTNode root, NodeFactory nf) {
		FunctionDefinitionNode driverFunDefn = null;

		while (root != null) {
			if (root.nodeKind() == NodeKind.FUNCTION_DEFINITION) {
				if (((FunctionDefinitionNode)root).getName().endsWith("$driver")) {
					driverFunDefn = (FunctionDefinitionNode) root;
					break;
				} else {
					root = BaseWorker.nextDFSSkip(root);
					continue;
				}
			}
			root=root.nextDFS();
		}
		if (driverFunDefn == null)
			return;

		CompoundStatementNode body = driverFunDefn.getBody();
		int size = body.numChildren();
		Map<String, Integer> insertAfterMap = new HashMap<>(); // _cc_heap_x name -> position
		
		for (int i = 0; i < size; i++) {
			BlockItemNode child = body.getSequenceChild(i);
			// System.out.println(child.prettyRepresentation());
			String heapVarName = isHeapVarDecl(child);
			
			if (heapVarName != null)
				insertAfterMap.put(heapVarName, i);
		}
		if (insertAfterMap.isEmpty())
			return;

		Map<String, BlockItemNode> transformdAssigns = new HashMap<>();

		findAndTransformValid(body, transformdAssigns, nf, insertAfterMap);

		PriorityQueue<Pair<String, Integer>> sortedByPos = new PriorityQueue<>(
				(o1, o2) -> Integer.compare(o2.right, o1.right));

		for (String name : transformdAssigns.keySet())
			sortedByPos.add(new Pair<>(name, insertAfterMap.get(name)));
		while (!sortedByPos.isEmpty()) {
			Pair<String, Integer> pair = sortedByPos.poll();
			List<BlockItemNode> list = new LinkedList<>();

			list.add(transformdAssigns.get(pair.left));
			body.insertChildren(pair.right + 1, list);
		}
	}
	
	/**
	 * Finds and transforms valid-pointer clause in <code>$assumes</code> in
	 * <code>
	 * if (cond)
	 *   $with(..) stmt;
	 * </code> or <code>
	 * $when(..) stmt; or
	 * <code>$assume</code>
	 * </code>
	 */
	private static void findAndTransformValid(CompoundStatementNode body,
			Map<String, BlockItemNode> assignsWtHeapVar, NodeFactory nf, 
			Map<String, Integer> table) {
		int size = body.numChildren();

		for (int i = 0; i < size; i++) {
			BlockItemNode blkItem = body.getSequenceChild(i);

			if (blkItem.blockItemKind() != BlockItemKind.STATEMENT)
				continue;

			StatementNode stmt = (StatementNode) blkItem;
			//System.out.println(stmt.prettyRepresentation());
			if (stmt.statementKind() == StatementNode.StatementKind.IF) {
				IfNode ifNode = (IfNode) stmt;
				StatementNode branch = ifNode.getTrueBranch();
				int branchIdx = branch.childIndex();
				Map<String, BlockItemNode> conditionalOut = new HashMap<>();

				branch.remove();
				transformValidInConditionalWithAssume(branch, conditionalOut,
						nf);
				ifNode.setChild(branchIdx, branch);
				if (conditionalOut.isEmpty())
					break;
				
				List<BlockItemNode> conditionalStmts = new LinkedList<>();
				
				conditionalStmts.addAll(conditionalOut.values());
				
				Source source = ifNode.getCondition().getSource();
				StatementNode conditionalAssigns = nf.newIfNode(source,
						ifNode.getCondition().copy(),
						nf.newCompoundStatementNode(source, conditionalStmts));
				int maxPos = -1;
				String maxPosName = null;
				
				for (String name : conditionalOut.keySet()) {
					int pos = table.get(name);
					if (pos > maxPos) {
						maxPos = pos;
						maxPosName = name;
					}
				}
				assignsWtHeapVar.put(maxPosName, conditionalAssigns);
			} else if (stmt.statementKind() == StatementNode.StatementKind.WHEN
					|| isAssumeCallStmt(stmt)) {
				int stmtIdx = stmt.childIndex();
				ASTNode parent = stmt.parent();

				stmt.remove();
				transformValidInConditionalWithAssume(stmt, assignsWtHeapVar, nf);
				parent.setChild(stmtIdx, stmt);
			}
		}
	}
	
	/**
	 * Given a statement, fill valid-pointer clause transformations to out, if
	 * they are conjunctive clauses of an assume call in the statement.
	 */
	private static void transformValidInConditionalWithAssume(
			StatementNode stmt, Map<String, BlockItemNode> assignsWtHeapVar,
			NodeFactory nf) {
		ASTNode node = stmt;

		for (; node != null; node = node.nextDFS()) {
			if (isCallOf(node, "$assume")) {
				FunctionCallNode call = (FunctionCallNode) node;

//				System.out.println(call.getArgument(0).prettyRepresentation());
				assignsWtHeapVar.putAll(transformedInPlaceChoice(
						findConjuntciveInPlaceChoice(call.getArgument(0)), nf));
				assignsWtHeapVar.putAll(transformedValidToAssignment(
						findConjuntciveValid(call.getArgument(0)), nf));
				break;
			}
		}
	}
 
	private static boolean isAssumeCallStmt(StatementNode stmt) {
		if (stmt.statementKind() == StatementNode.StatementKind.EXPRESSION) {
			ExpressionStatementNode exprStmtNode = (ExpressionStatementNode) stmt;

			return isCallOf(exprStmtNode.getExpression(), "$assume");
		}
		return false;
	}
	
	/**
	 * @return true iff the given node is a call expression to $assume.
	 */
	private static boolean isCallOf(ASTNode node, String name) {
		if (node.nodeKind() != NodeKind.EXPRESSION)
			return false;

		ExpressionNode expr = (ExpressionNode) node;

		if (expr.expressionKind() != ExpressionKind.FUNCTION_CALL)
			return false;

		FunctionCallNode call = (FunctionCallNode) expr;

		if (call.getFunction()
				.expressionKind() != ExpressionKind.IDENTIFIER_EXPRESSION)
			return false;

		IdentifierExpressionNode func = (IdentifierExpressionNode) call
				.getFunction();

		return func.getIdentifier().name().equals(name);
	}

	/**
	 * @param node
	 * @return the name of the declared variable if the given node is a variable
	 *         declaration node of a _cc_heap_x variable, otherwise, null.
	 */
	private static String isHeapVarDecl(BlockItemNode node) {
		if (node.blockItemKind() == BlockItemKind.ORDINARY_DECLARATION) {
			OrdinaryDeclarationNode decl = (OrdinaryDeclarationNode) node;

			if (decl.ordinaryDeclarationKind() == OrdinaryDeclarationKind.VARIABLE_DECLARATION) {
				VariableDeclarationNode varDecl = (VariableDeclarationNode) decl;

				if (varDecl.getName().startsWith("_cc_heap_"))
					return varDecl.getName();
			}
		}
		return null;
	}

	/**
	 * Given a formula, if it is in a conjunctive form <code>
	 *   clause && clause && ... && clause
	 * </code>, returns all the clauses of the form <code>p == _cc_heap_x</code>
	 * where x is a name suffix.
	 */
	private static List<ExpressionNode> findConjuntciveValid(
			ExpressionNode form) {
		if (form.expressionKind() != ExpressionKind.OPERATOR)
			return new LinkedList<>();

		OperatorNode opNode = (OperatorNode) form;
		List<ExpressionNode> results = new LinkedList<>();

		if (opNode.getOperator() != Operator.LAND)
			if (isTransformedValid(opNode)) {
				results.add(form);
				return results;
			} else
				return results;
		results.addAll(findConjuntciveValid(opNode.getArgument(0)));
		results.addAll(findConjuntciveValid(opNode.getArgument(1)));
		return results;
	}
	
	/**
	 * Given a formula, if it is in a conjunctive form <code>
	 *   clause && clause && ... && clause
	 * </code>, finds all the clauses of the form <code> 
	 *
	 * p == MPI_IN_PLACE || \valid(p), or
	 * MPI_IN_PLACE == p || \valid(p), or
	 * \valid(p) || p == MPI_IN_PLACE, or
	 * \valid(p) || MPI_IN_PLACE == p,
	 * 
	 * </code>
	 * 
	 * @returns the set of found ones and normalize them to the exact form
	 *          <code>p == MPI_IN_PLACE || \valid(p)</code>
	 */
	private static List<OperatorNode> findConjuntciveInPlaceChoice(
			ExpressionNode expr) {
//		System.out.println(expr.prettyRepresentation());
		if (expr.expressionKind() != ExpressionNode.ExpressionKind.OPERATOR)
			return new LinkedList<>();

		OperatorNode opNode = (OperatorNode) expr;
		List<OperatorNode> results = new LinkedList<>();

		if (opNode.getOperator() == Operator.LOR) {
			if (isInPlaceChoice(opNode))
				results.add(opNode);
		} else if (opNode.getOperator() == Operator.LAND) {
			int numArgs = opNode.getNumberOfArguments();

			for (int i = 0; i < numArgs; i++)
				results.addAll(
						findConjuntciveInPlaceChoice(opNode.getArgument(i)));
		}
		return results;
	}
	
	/**
	 * Given <code>p == MPI_IN_PLACE || \valid(p)</code>, transforms it to
	 * <code>
	 * if (p != MPI_IN_PLACE) {
	 *    transformValid(\valid(p))
	 * }
	 * </code>
	 * 
	 * @param inplaceChoices
	 * @param nf
	 * @return
	 */
	private static Map<String, BlockItemNode> transformedInPlaceChoice(
			List<OperatorNode> inplaceChoices, NodeFactory nf) {
		Map<String, BlockItemNode> result = new HashMap<>();

		for (OperatorNode inplaceChoice : inplaceChoices) {
			ExpressionNode inplace = inplaceChoice.getArgument(0);
			List<ExpressionNode> valid = new LinkedList<>();
			Source source = inplaceChoice.getSource();

			valid.add(inplaceChoice.getArgument(1));

			Map<String, BlockItemNode> subResult = transformedValidToAssignment(
					valid, nf);
			String key = subResult.keySet().iterator().next();
			StatementNode blkItem = nf.newIfNode(source,
					nf.newOperatorNode(source, Operator.NOT, inplace.copy()),
					(StatementNode) subResult.get(key));

			result.put(key, blkItem);
		}
		return result;
	}
	
	/**
	 * is <code>p == MPI_IN_PLACE || \valid(p)</code>
	 * @param orNode
	 * @return
	 */
	private static boolean isInPlaceChoice(OperatorNode orNode) {
		ExpressionNode left = orNode.getArgument(0);
		ExpressionNode right = orNode.getArgument(1);
		ExpressionNode valid, inplace;
		
		if (left.expressionKind() != ExpressionNode.ExpressionKind.OPERATOR)
			return false;
		if (right.expressionKind() != ExpressionNode.ExpressionKind.OPERATOR)
			return false;
		if (isTransformedValid((OperatorNode) left)) {
			valid = left;
			inplace = right;
		} else {
			valid = right;
			inplace = left;
		}
		OperatorNode opNode = (OperatorNode) inplace;
		
		if (opNode.getOperator() != Operator.EQUALS)
			return false;
		
		ExpressionNode inplaceLeft = opNode.getArgument(0);
		ExpressionNode inplaceRight = opNode.getArgument(1);

		if (!isMPI_IN_PLACE(inplaceLeft) && !isMPI_IN_PLACE(inplaceRight))
			return false;

		valid.remove();
		inplace.remove();
		orNode.setArgument(0, inplace);
		orNode.setArgument(1, valid);
		return true;
	}
	
	private static boolean isMPI_IN_PLACE(ExpressionNode expr) {
		if (expr.expressionKind() == ExpressionNode.ExpressionKind.CAST)
			expr = ((CastNode) expr).getArgument();
		if (expr.expressionKind() != ExpressionNode.ExpressionKind.OPERATOR)
			return false;

		OperatorNode opNode = (OperatorNode) expr;

		if (opNode.getOperator() != Operator.ADDRESSOF)
			return false;
		expr = opNode.getArgument(0);
		if (expr.expressionKind() == ExpressionNode.ExpressionKind.IDENTIFIER_EXPRESSION)
			if (((IdentifierExpressionNode) expr).getIdentifier().name()
					.equals(MPI_IN_PLACE_SPOT))
				return true;
		return false;
	}

	/**
	 * <p>
	 * Given <code>p == _cc_heap_x</code>s, returns
	 * <code>p = _cc_heap_x;</code>s.
	 * </p>
	 */
	private static Map<String, BlockItemNode> transformedValidToAssignment(
			List<ExpressionNode> pEqsHeaps, NodeFactory nf) {
		Map<String, BlockItemNode> results = new HashMap<>();

		for (ExpressionNode pEqsHeap : pEqsHeaps) {
			ExpressionNode p = (ExpressionNode) pEqsHeap.child(0);
			ExpressionNode heap = (ExpressionNode) pEqsHeap.child(1);
			ExpressionNode assign = nf.newOperatorNode(pEqsHeap.getSource(),
					Operator.ASSIGN, p.copy(), heap.copy());
			String heapName = ((IdentifierExpressionNode) heap).getIdentifier()
					.name();

			results.put(heapName, nf.newExpressionStatementNode(assign));
		}
		return results;
	}

	/**
	 * <p>
	 * Returns true iff the given node is either <code>p == _cc_heap_x</code> or
	 * <code>_cc_heap_x == p</code>. Besides, if it is the latter case, the
	 * opNode will be modified to the former case.
	 * </p>
	 * 
	 * @param opNode
	 * @return
	 */
	private static boolean isTransformedValid(OperatorNode opNode) {
		if (opNode.getOperator() != Operator.EQUALS)
			return false;
		ExpressionNode cc_heap_x = opNode.getArgument(1);
		ExpressionNode p = opNode.getArgument(0);

		if (cc_heap_x
				.expressionKind() != ExpressionKind.IDENTIFIER_EXPRESSION) {
			ExpressionNode tmp = p;

			p = cc_heap_x;
			cc_heap_x = tmp;
		}
		if (cc_heap_x.expressionKind() != ExpressionKind.IDENTIFIER_EXPRESSION)
			return false;
		if (((IdentifierExpressionNode) cc_heap_x).getIdentifier().name()
				.startsWith("_cc_heap_")) {
			cc_heap_x.remove();
			p.remove();
			opNode.setArgument(0, p);
			opNode.setArgument(1, cc_heap_x);
			return true;
		}
		return false;
	}

	/**
	 * pre-condition: pred = lower &lt= x && x &lt higher
	 * 
	 * @param pred
	 * @return
	 */
	static ExpressionNode extractLower(ExpressionNode pred) {
		OperatorNode lte = (OperatorNode) pred.child(0);

		if (lte.getOperator() != Operator.LTE)
			lte = (OperatorNode) pred.child(1);
		return (ExpressionNode) lte.child(0);
	}

	/**
	 * pre-condition: pred = lower &lt= x && x &lt higher
	 * 
	 * @param pred
	 * @return
	 */
	static ExpressionNode extractHigher(ExpressionNode pred) {
		OperatorNode gt = (OperatorNode) pred.child(0);

		if (gt.getOperator() != Operator.LT)
			gt = (OperatorNode) pred.child(1);
		return (ExpressionNode) gt.child(1);
	}

	/* *************** package classes *********************/
	// Following classes are mainly used for a better readability of the
	// contract transformers.

	/**
	 * A grouped artificial {@link BlockItemNode}s which shall be used for the
	 * purpose of allocating memory spaces for valid pointers. Usually, memory
	 * spaces are simulated by variables, see {@link memoryDefinitions}, whose
	 * life time must be long enough.
	 * 
	 * @author xxxx
	 *
	 */
	class MemoryAllocation {
		/**
		 * <p>
		 * A list of {@link BlockItemNode}s which is an ordered group of object
		 * definitions. These objects have fresh new unconstrained values.
		 * </p>
		 * <p>
		 * Since these object definitions play roles of memory objects, their
		 * life time must be long enough. Thus, this nodes should be inserted in
		 * some scopes where those objects can always be referred until the
		 * verification is done.
		 * </p>
		 */
		List<BlockItemNode> memoryDefinitions;

		/**
		 * <p>
		 * A list of {@link BlockItemNode}s which is an ordered group of
		 * statements. These statements will assign memory references to valid
		 * pointers.
		 * </p>
		 * 
		 * <p>
		 * In contrast to {@link #memoryDefinitions}, this nodes must stick with
		 * their behaviors (if they are specified under some named behaviors).
		 * </p>
		 */
		List<BlockItemNode> assignMemoryReferences;

		MemoryAllocation(List<BlockItemNode> memoryDefinitions,
				List<BlockItemNode> assignMemoryReferences) {
			if (memoryDefinitions == null)
				this.memoryDefinitions = new LinkedList<>();
			else
				this.memoryDefinitions = memoryDefinitions;
			if (assignMemoryReferences == null)
				this.assignMemoryReferences = new LinkedList<>();
			else
				this.assignMemoryReferences = assignMemoryReferences;
		}
	}
}
