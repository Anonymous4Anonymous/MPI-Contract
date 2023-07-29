package edu.udel.cis.vsl.civl.transform.common.contracts.translators;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode.NodeKind;
import edu.udel.cis.vsl.abc.ast.node.IF.IdentifierNode;
import edu.udel.cis.vsl.abc.ast.node.IF.NodeFactory;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.MPIContractExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.MPIContractExpressionNode.MPIContractExpressionKind;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.VariableDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode.ExpressionKind;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode.Operator;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.RegularRangeNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.BlockItemNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.StatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.TypeNode;
import edu.udel.cis.vsl.abc.ast.type.IF.PointerType;
import edu.udel.cis.vsl.abc.ast.type.IF.StandardBasicType.BasicTypeKind;
import edu.udel.cis.vsl.abc.ast.type.IF.Type.TypeKind;
import edu.udel.cis.vsl.abc.front.IF.CivlcTokenConstant;
import edu.udel.cis.vsl.abc.token.IF.CivlcToken;
import edu.udel.cis.vsl.abc.token.IF.CivlcToken.TokenVocabulary;
import edu.udel.cis.vsl.abc.token.IF.Formation;
import edu.udel.cis.vsl.abc.token.IF.Source;
import edu.udel.cis.vsl.abc.token.IF.TokenFactory;
import edu.udel.cis.vsl.civl.model.IF.CIVLSyntaxException;
import edu.udel.cis.vsl.civl.transform.common.BaseWorker;
import edu.udel.cis.vsl.civl.util.IF.Pair;

/**
 * This class is a collection of common methods that all contract translators
 * share.
 * 
 * @author xxxx
 */
public class ContractTranslatorCommonBase {

	protected NodeFactory nodeFactory;

	protected TokenFactory tokenFactory;

	ContractTranslatorCommonBase(NodeFactory nodeFactory,
			TokenFactory tokenFactory) {
		this.nodeFactory = nodeFactory;
		this.tokenFactory = tokenFactory;
	}

	/**
	 * <p>
	 * Given a variable name 'v' and a MPI_Datatype expression "datatype",
	 * returns <code>
	 *    int v;
	 *    v = sizeofDatatype(datatype);
	 * </code>
	 * </p>
	 */
	List<BlockItemNode> createDatatypeSizeHolderVariable(String varName,
			ExpressionNode datatype) {
		Source datatypeSource = datatype.getSource();
		IdentifierNode tmpVarIdent = nodeFactory
				.newIdentifierNode(datatypeSource, varName);
		TypeNode sizeType = nodeFactory.newBasicTypeNode(datatypeSource,
				BasicTypeKind.INT);
		VariableDeclarationNode tmpVarDecl = nodeFactory
				.newVariableDeclarationNode(datatypeSource, tmpVarIdent,
						sizeType);
		ExpressionNode datatypeSizeofFun = nodeFactory
				.newIdentifierExpressionNode(datatypeSource,
						nodeFactory.newIdentifierNode(datatypeSource,
								MPI_CONTRACT_CONSTS.MPI_SIZEOFDATATYPE_FUN));
		ExpressionNode datatypeSizeofCall, assignToTmpVar;
		List<ExpressionNode> args = new LinkedList<>();
		List<BlockItemNode> results = new LinkedList<>();

		args.add(datatype);
		datatypeSizeofCall = nodeFactory.newFunctionCallNode(datatypeSource,
				datatypeSizeofFun, args, null);
		assignToTmpVar = nodeFactory.newOperatorNode(datatypeSource,
				Operator.ASSIGN,
				nodeFactory.newIdentifierExpressionNode(datatypeSource,
						tmpVarIdent.copy()),
				datatypeSizeofCall);
		results.add(tmpVarDecl);
		results.add(nodeFactory.newExpressionStatementNode(assignToTmpVar));
		return results;
	}

	/**
	 * check if the given pointer set is of the form <code>p + (l .. h)</code>
	 * where <code>p</code> is a base address.
	 * 
	 * @return a pair <code>(p, (l, h))</code>; null if the input is not in the
	 *         expected form
	 */
	Pair<ExpressionNode, Pair<ExpressionNode, ExpressionNode>> checkPointerSetLimitation(
			ExpressionNode ptrs) {
		if (ptrs.getType().kind() != TypeKind.SET)
			return new Pair<>(ptrs, null);
		if (ptrs.expressionKind() != ExpressionKind.OPERATOR)
			return null;

		OperatorNode opNode = (OperatorNode) ptrs;
		ExpressionNode ptr = opNode.getArgument(0);
		ExpressionNode range = opNode.getArgument(1);

		if (ptr.getType().kind() != TypeKind.POINTER)
			return null;
		if (opNode.getOperator() != Operator.PLUS)
			return null;
		if (range.expressionKind() != ExpressionKind.REGULAR_RANGE)
			return null;

		RegularRangeNode regularRange = (RegularRangeNode) range;

		return new Pair<>(ptr,
				new Pair<>(regularRange.getLow(), regularRange.getHigh()));
	}

	/**
	 * 
	 * @param mpiBufTypeExpr
	 *            an expression of type of <code>\mpi_buf_type</code>, which can
	 *            be either of the 3 forms <code>
	 *            1.  \mpi_buf(c, count, datatype)
	 *            2. e' + c, where e' has type \mpi_buf_type
	 *            3. e' * n, where e' has type \mpi_buf_type
	 *            </code> Note that when it comes to the cases where the
	 *            expression is of <code>\mpi_buf_type</code> and has the kinds
	 *            of <code>\mpi_on</code> or <code>\old</code>, it is
	 *            recursively handled.
	 * @return a normalized <code>\mpi_buf_type</code> expression of the form
	 *         <code>\mpi_buf(q, c, d)</code>
	 */
	MPIContractExpressionNode normalizeMpiBufExpression(
			ExpressionNode mpiBufTypeExpr) {
		if (mpiBufTypeExpr
				.expressionKind() == ExpressionKind.MPI_CONTRACT_EXPRESSION)
			return (MPIContractExpressionNode) mpiBufTypeExpr.copy();

		ExpressionNode[] triple = normalizeMpiBufExpressionWorker(
				mpiBufTypeExpr);
		List<ExpressionNode> args = new LinkedList<>();

		for (ExpressionNode arg : triple)
			args.add(arg.copy());
		return nodeFactory.newMPIExpressionNode(mpiBufTypeExpr.getSource(),
				args, MPIContractExpressionKind.MPI_BUF, "\\mpi_buf");
	}

	/**
	 * <p>
	 * worker method of {@link #normalizeMpiBufExpression(ExpressionNode)}. For
	 * any expression 'e' of type of \mpi_buf_type, applies the two rules
	 * recursively: <code>
	 * 
	 * 	 1. \mpi_buf(p, c, d) + x => \mpi_buf(p + x * \mpi_extent(d), c, d)
	 *   2. \mpi_buf(p, c, d) * n => \mpi_buf(p, c * n, d)
	 *   
	 * </code>
	 * </p>
	 * 
	 * @return the three components "{p, c, d}" of a normalized expression of
	 *         the form <code>\mpi_buf(p, c, d)</code> in a java array
	 * 
	 */
	ExpressionNode[] normalizeMpiBufExpressionWorker(
			ExpressionNode mpiBufTypeExpr) {
		if (mpiBufTypeExpr
				.expressionKind() == ExpressionKind.MPI_CONTRACT_EXPRESSION) {
			MPIContractExpressionNode mpiExpr = (MPIContractExpressionNode) mpiBufTypeExpr;

			if (mpiExpr
					.MPIContractExpressionKind() == MPIContractExpressionKind.MPI_BUF) {
				return new ExpressionNode[]{mpiExpr.getArgument(0),
						mpiExpr.getArgument(1), mpiExpr.getArgument(2)};
			}
		}
		if (mpiBufTypeExpr.expressionKind() != ExpressionKind.OPERATOR)
			throw new CIVLSyntaxException(
					"Supported \\mpi_buf type expression e must have of the following forms:\n"
							+ "          e := operation | remote\n"
							+ "  operation := \\mpi_buf(ptr, count, datatype) | operation * m + n\n"
							+ "     remote := operation | \\mpi_on(remote, proc)");

		OperatorNode opNode = (OperatorNode) mpiBufTypeExpr;

		if (opNode.getOperator() == Operator.PLUS) {
			// \mpi_buf(p, c, d) + x =>
			// \mpi_buf(p + x * \mpi_extent(d), c, d)
			ExpressionNode[] triple = normalizeMpiBufExpressionWorker(
					opNode.getArgument(0));
			ExpressionNode ptr = triple[0];
			// assume ABC guarantees the offset to be the second argument:
			ExpressionNode oft = opNode.getArgument(1);
			ExpressionNode datatype = triple[2];
//			Source ptrSource = ptr.getSource();

			triple[0] = mpiPointerAddSystemCall(ptr.copy(), oft.copy(),
					datatype.copy());
			// triple[0] = nodeFactory.newOperatorNode(ptrSource, Operator.PLUS,
			// ptr.copy(), nodeFactory.newOperatorNode(ptrSource,
			// Operator.TIMES, oft.copy(), datatype.copy()));
			return triple;
		}
		assert opNode.getOperator() == Operator.TIMES;
		// \mpi_buf(p, c, d) * n => \mpi_buf(p, c * n, d)
		ExpressionNode[] triple = normalizeMpiBufExpressionWorker(
				opNode.getArgument(0));
		ExpressionNode count = triple[1];
		// assume ABC guarantees the multiple 'n' to be the second argument:
		ExpressionNode n = opNode.getArgument(1);
		Source countSource = count.getSource();

		triple[1] = nodeFactory.newOperatorNode(countSource, Operator.TIMES,
				count.copy(), n.copy());
		return triple;
	}

	/**
	 * For an \mpi_buf type expression that in general has the form <code>
	 *   (\mpi_buf(p, c, d) * n + c) * m
	 * </code>, return the type of <code>p</code>
	 * 
	 * @param mpiBufTypeExpr
	 */
	PointerType extractMpiBufExprPointerType(ExpressionNode mpiBufTypeExpr) {
		if (mpiBufTypeExpr
				.expressionKind() == ExpressionKind.MPI_CONTRACT_EXPRESSION) {
			MPIContractExpressionNode mpiExpr = (MPIContractExpressionNode) mpiBufTypeExpr;

			return (PointerType) mpiExpr.getArgument(0).getType();
		}
		assert mpiBufTypeExpr.expressionKind() == ExpressionKind.OPERATOR;

		OperatorNode opNode = (OperatorNode) mpiBufTypeExpr;

		return extractMpiBufExprPointerType(opNode.getArgument(0));
	}

	/**
	 * @return <code>$mpi_extent(datatype)</code> where <code>$mpi_extent</code>
	 *         is an abstract function
	 */
	ExpressionNode mpiExtentCall(ExpressionNode datatype) {
		Source source = datatype.getSource();
		ExpressionNode funIdent = nodeFactory.newIdentifierExpressionNode(
				source, nodeFactory.newIdentifierNode(source,
						MPI_CONTRACT_CONSTS.MPI_EXTENT_ABSTRACT_FUN));
		List<ExpressionNode> args = new LinkedList<>();

		args.add(datatype);
		return nodeFactory.newFunctionCallNode(source, funIdent, args, null);
	}

	/**
	 * @param assertion
	 * @return <code>$assert(assertion)</code>
	 */
	StatementNode createAssertion(ExpressionNode assertion) {
		List<ExpressionNode> args = new LinkedList<>();
		Source source = assertion.getSource();
		ExpressionNode fun = nodeFactory.newIdentifierExpressionNode(source,
				nodeFactory.newIdentifierNode(source, BaseWorker.ASSERT));

		args.add(assertion);
		return nodeFactory.newExpressionStatementNode(
				nodeFactory.newFunctionCallNode(source, fun, args, null));
	}

	/**
	 * @param assertion
	 * @return <code>$assume(assertion)</code>
	 */
	StatementNode createAssumption(ExpressionNode assertion) {
		List<ExpressionNode> args = new LinkedList<>();
		Source source = assertion.getSource();
		ExpressionNode fun = nodeFactory.newIdentifierExpressionNode(source,
				nodeFactory.newIdentifierNode(source, BaseWorker.ASSUME));

		args.add(assertion);
		return nodeFactory.newExpressionStatementNode(
				nodeFactory.newFunctionCallNode(source, fun, args, null));
	}

	ExpressionNode conjunct(List<ExpressionNode> exprs) {
		Iterator<ExpressionNode> iter = exprs.iterator();
		ExpressionNode result = null;
		Source source = null;

		while (iter.hasNext()) {
			ExpressionNode expr = iter.next();

			source = source != null
					? tokenFactory.join(source, expr.getSource())
					: expr.getSource();
			result = result != null
					? nodeFactory.newOperatorNode(source, Operator.LAND,
							expr.copy(), result)
					: expr.copy();
		}
		if (result == null) {
			Formation formation = tokenFactory
					.newTransformFormation("contractTransformer", "$true");
			CivlcToken token = tokenFactory.newCivlcToken(
					CivlcTokenConstant.CONST, "inserted text", formation,
					TokenVocabulary.DUMMY);

			source = tokenFactory.newSource(token);
			result = nodeFactory.newBooleanConstantNode(source, true);
		}
		return result;
	}

	List<Pair<ASTNode, ASTNode>> mpiCommSizeOrRankSubstitutions(ASTNode node) {
		List<Pair<ASTNode, ASTNode>> results = new LinkedList<>();

		if (node.nodeKind() == NodeKind.EXPRESSION) {
			ExpressionNode expr = (ExpressionNode) node;

			if (expr.expressionKind() == ExpressionKind.MPI_CONTRACT_EXPRESSION) {
				MPIContractExpressionNode mpiContractExpr = (MPIContractExpressionNode) expr;

				if (mpiContractExpr
						.MPIContractExpressionKind() == MPIContractExpressionKind.MPI_COMM_RANK)
					results.add(new Pair<>(mpiContractExpr,
							nodeFactory.newIdentifierExpressionNode(
									mpiContractExpr.getSource(),
									nodeFactory.newIdentifierNode(
											mpiContractExpr.getSource(),
											MPI_CONTRACT_CONSTS.MPI_COMM_RANK_CONST))));
				if (mpiContractExpr
						.MPIContractExpressionKind() == MPIContractExpressionKind.MPI_COMM_SIZE)
					results.add(new Pair<>(mpiContractExpr,
							nodeFactory.newIdentifierExpressionNode(
									mpiContractExpr.getSource(),
									nodeFactory.newIdentifierNode(
											mpiContractExpr.getSource(),
											MPI_CONTRACT_CONSTS.MPI_COMM_SIZE_CONST))));
			}
		}
		for (ASTNode child : node.children())
			if (child != null)
				results.addAll(mpiCommSizeOrRankSubstitutions(child));
		return results;
	}

	<T extends ASTNode> List<T> applySubstitutions(
			List<Pair<T, T>> substitutions, Iterable<T> nodes) {
		List<T> results = new LinkedList<>();
		Map<T, T> substitutionMap = new IdentityHashMap<>();

		for (Pair<T, T> substPair : substitutions) {
			ASTNode prev = substitutionMap.put(substPair.left, substPair.right);

			assert prev == null;
		}
		for (T node : nodes)
			results.add(applySubstitutionsWorker(substitutionMap, node));
		return results;
	}

	@SuppressWarnings("unchecked")
	private <T extends ASTNode> T applySubstitutionsWorker(
			Map<T, T> substitutions, T node) {
		T subst = substitutions.get(node);

		if (subst != null)
			return subst;

		T copy = (T) node.copy();

		for (ASTNode child : node.children())
			if (child != null) {
				int childIdx = child.childIndex();

				child = applySubstitutionsWorker(substitutions, (T) child);
				child.remove();
				copy.setChild(childIdx, child);
			}
		return copy;
	}

	/**
	 * @return <code>$value_at(*ptr2State, $mpi_comm_rank, expr)</code> if
	 *         "isLocal" is false; <code>$value_at(*ptr2State, 0, expr)</code>
	 *         otherwise.
	 */
	ExpressionNode mkValueAtExpressionWithDefaultProc(ExpressionNode expr,
			ExpressionNode state, boolean isLocal) {
		Source source = expr.getSource();
		ExpressionNode proc = isLocal
				? nodeFactory.newIntConstantNode(source, 0)
				: nodeFactory.newIdentifierExpressionNode(source,
						nodeFactory.newIdentifierNode(source,
								MPI_CONTRACT_CONSTS.MPI_COMM_RANK_CONST));

		return nodeFactory.newValueAtNode(source, state, proc, expr);
	}

	ExpressionNode mpiPointerAddSystemCall(ExpressionNode ptr,
			ExpressionNode count, ExpressionNode datatypeSize) {
		Source source = ptr.getSource();
		ExpressionNode fun = nodeFactory.newIdentifierExpressionNode(source,
				nodeFactory.newIdentifierNode(source,
						MPI_CONTRACT_CONSTS.MPI_POINTER_ADD_SYSTEM_FUN));
		List<ExpressionNode> args = new LinkedList<>();

		args.add(ptr);
		args.add(count);
		args.add(datatypeSize);
		return nodeFactory.newFunctionCallNode(source, fun, args, null);
	}
}
