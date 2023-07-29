package edu.udel.cis.vsl.civl.transform.common.contracts.translators;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode.NodeKind;
import edu.udel.cis.vsl.abc.ast.node.IF.IdentifierNode;
import edu.udel.cis.vsl.abc.ast.node.IF.NodeFactory;
import edu.udel.cis.vsl.abc.ast.node.IF.PairNode;
import edu.udel.cis.vsl.abc.ast.node.IF.SequenceNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.CurlyBracedTermSetNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.MPIContractExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.MPIContractExpressionNode.MPIContractExpressionKind;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.SeparatedNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.VariableDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode.ExpressionKind;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode.Operator;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.QuantifiedExpressionNode.Quantifier;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.BlockItemNode;
import edu.udel.cis.vsl.abc.ast.type.IF.AcslMPIType;
import edu.udel.cis.vsl.abc.ast.type.IF.AcslMPIType.AcslMPITypeKind;
import edu.udel.cis.vsl.abc.ast.type.IF.Type;
import edu.udel.cis.vsl.abc.ast.type.IF.Type.TypeKind;
import edu.udel.cis.vsl.abc.token.IF.Source;
import edu.udel.cis.vsl.abc.token.IF.SyntaxException;
import edu.udel.cis.vsl.abc.token.IF.TokenFactory;
import edu.udel.cis.vsl.civl.model.IF.CIVLUnimplementedFeatureException;
import edu.udel.cis.vsl.civl.util.IF.Pair;

/**
 * This class is the common part of the translators for pre- and post-conditions
 * of target and callee functions. Note that this class does not contain parts
 * for translating frame conditions for target or callee functions.
 * 
 * @author xxxx
 *
 */
public abstract class CommonPrePostCondTranslator
		extends
			ContractTranslatorCommonBase {

	CommonPrePostCondTranslator(NodeFactory nodeFactory,
			TokenFactory tokenFactory) {
		super(nodeFactory, tokenFactory);
	}

	/* **************** ASTNode traversal **************** */
	<T extends ASTNode> T visitNodes(T node, ExpressionNode preState,
			ExpressionNode theState, List<BlockItemNode> prefix,
			List<BlockItemNode> suffix, boolean isLocal)
			throws SyntaxException {
		if (node.nodeKind() != NodeKind.EXPRESSION)
			return visitNodeChildren(node, preState, theState, prefix, suffix,
					isLocal);
		else {
			ASTNode newNode = processNode((ExpressionNode) node, preState,
					theState, prefix, suffix, isLocal);

			if (newNode == node)
				newNode = newNode.copy();

			@SuppressWarnings("unchecked")
			T result = (T) newNode;

			return result;
		}
	}

	private <T extends ASTNode> T visitNodeChildren(T node,
			ExpressionNode preState, ExpressionNode theState,
			List<BlockItemNode> prefix, List<BlockItemNode> suffix,
			boolean isLocal) throws SyntaxException {
		@SuppressWarnings("unchecked")
		T copy = (T) node.copy();

		for (ASTNode child : node.children()) {
			if (child == null)
				continue;

			int childIdx = child.childIndex();
			child = visitNodes(child, preState, theState, prefix, suffix,
					isLocal);

			copy.removeChild(childIdx);
			copy.setChild(childIdx, child.copy());
		}
		return copy;
	}

	ExpressionNode processNode(ExpressionNode node, ExpressionNode preState,
			ExpressionNode theState, List<BlockItemNode> prefix,
			List<BlockItemNode> suffix, boolean isLocal)
			throws SyntaxException {
		ExpressionNode expr = node;
		Type exprType = expr.getType();

		switch (expr.expressionKind()) {
			case MPI_CONTRACT_EXPRESSION : {
				MPIContractExpressionNode contractNode = (MPIContractExpressionNode) expr;
				switch (contractNode.MPIContractExpressionKind()) {
					case MPI_AGREE :
						return translateMpiAgree(contractNode, preState,
								theState, prefix, suffix);
					case MPI_BUF :
						return translateMpiBuf(contractNode, preState, theState,
								prefix, suffix);
					case MPI_COMM_RANK :
					case MPI_COMM_SIZE :
						return translateMpiConstants(contractNode);
					case MPI_EXTENT :
						return translateMpiExtent(contractNode, preState,
								theState, prefix, suffix);

					case MPI_NONOVERLAPPING :
						return translateMpiNonoverlapping(contractNode,
								preState, theState, prefix, suffix);
					case MPI_ON :
						return translateMpiOn(contractNode, preState, theState,
								prefix, suffix);
					case MPI_REDUCE :
						return translateMpiReduce(contractNode, preState,
								theState, prefix, suffix);
					case MPI_SIG :
						return translateMpiSig(contractNode, preState, theState,
								prefix, suffix);
					default :
						assert false : "unreachable";
						return null;
				}
			}
			case OPERATOR : {
				OperatorNode opNode = (OperatorNode) expr;

				switch (opNode.getOperator()) {
					case OLD :
						return translateOld(opNode, preState, theState, prefix,
								suffix, isLocal);
					case VALID :
						return translateValid(opNode, preState, theState,
								prefix, suffix, isLocal);
					default :
						opNode = visitNodeChildren(opNode, preState, theState,
								prefix, suffix, isLocal);
						if (exprType.kind() == TypeKind.ACSL_MPI_TYPE) {
							AcslMPIType acslType = (AcslMPIType) exprType;

							if (acslType
									.acslTypeKind() == AcslMPITypeKind.ACSL_MPI_BUF_TYPE)
								return normalizeMpiBufExpression(opNode);
						}
						return opNode;
				}
			}
			case RESULT :
				return translateResult(expr);
			case SEPARATED :
				return translateSeparated((SeparatedNode) expr, preState,
						theState, prefix, suffix, isLocal);
			default :
				return visitNodeChildren(expr, preState, theState, prefix,
						suffix, isLocal);
		}
	}

	/* ************* special constructs translation methods ************ */

	abstract ExpressionNode translateMpiOn(MPIContractExpressionNode mpiOn,
			ExpressionNode preState, ExpressionNode theState,
			List<BlockItemNode> prefix, List<BlockItemNode> suffix)
			throws SyntaxException;

	abstract ExpressionNode translateOld(OperatorNode oldExpr,
			ExpressionNode preState, ExpressionNode theState,
			List<BlockItemNode> prefix, List<BlockItemNode> suffix,
			boolean isLocal) throws SyntaxException;

	abstract ExpressionNode translateResult(ExpressionNode resultExpr);

	abstract ExpressionNode translateMpiSig(MPIContractExpressionNode mpiSig,
			ExpressionNode preState, ExpressionNode theState,
			List<BlockItemNode> prefix, List<BlockItemNode> suffix)
			throws SyntaxException;

	abstract ExpressionNode translateMpiAgree(
			MPIContractExpressionNode mpiAgree, ExpressionNode preState,
			ExpressionNode theState, List<BlockItemNode> prefix,
			List<BlockItemNode> suffix) throws SyntaxException;

	abstract ExpressionNode translateMpiNonoverlapping(
			MPIContractExpressionNode mpiNonoverlapping,
			ExpressionNode preState, ExpressionNode theState,
			List<BlockItemNode> prefix, List<BlockItemNode> suffix)
			throws SyntaxException;

	abstract ExpressionNode translateMpiDatatypeInMpiBuf(
			ExpressionNode datatype, ExpressionNode preState,
			ExpressionNode theState, List<BlockItemNode> prefix,
			List<BlockItemNode> suffix) throws SyntaxException;

	abstract ExpressionNode translateMpiReduce(
			MPIContractExpressionNode mpiReduce, ExpressionNode preState,
			ExpressionNode theState, List<BlockItemNode> prefix,
			List<BlockItemNode> suffix) throws SyntaxException;

	abstract ExpressionNode translateValid(OperatorNode valid,
			ExpressionNode preState, ExpressionNode theState,
			List<BlockItemNode> prefix, List<BlockItemNode> suffix,
			boolean isLocal) throws SyntaxException;

	abstract ExpressionNode translateSeparated(SeparatedNode separatedNode,
			ExpressionNode preState, ExpressionNode theState,
			List<BlockItemNode> prefix, List<BlockItemNode> suffix,
			boolean isLocal) throws SyntaxException;

	abstract ExpressionNode translateMpiConstants(
			MPIContractExpressionNode mpiConst);

	abstract ExpressionNode translateMpiExtent(
			MPIContractExpressionNode mpiExtent, ExpressionNode preState,
			ExpressionNode theState, List<BlockItemNode> prefix,
			List<BlockItemNode> suffix) throws SyntaxException;

	ExpressionNode translateMpiBuf(MPIContractExpressionNode mpiBuf,
			ExpressionNode preState, ExpressionNode theState,
			List<BlockItemNode> prefix, List<BlockItemNode> suffix)
			throws SyntaxException {
		ExpressionNode ptr = mpiBuf.getArgument(0);
		ExpressionNode count = mpiBuf.getArgument(1);
		ExpressionNode datatype = mpiBuf.getArgument(2);
		ExpressionNode copy = mpiBuf.copy();
		int ptrIdx, countIdx, datatypeIdx;

		ptrIdx = ptr.childIndex();
		countIdx = count.childIndex();
		datatypeIdx = datatype.childIndex();
		ptr = visitNodes(ptr, preState, theState, prefix, suffix, false);
		count = visitNodes(count, preState, theState, prefix, suffix, false);
		datatype = translateMpiDatatypeInMpiBuf(datatype, preState, theState,
				prefix, suffix);
		copy.removeChild(ptrIdx);
		copy.removeChild(countIdx);
		copy.removeChild(datatypeIdx);
		copy.setChild(ptrIdx, ptr);
		copy.setChild(countIdx, count);
		copy.setChild(datatypeIdx, datatype);
		return copy;
	}

	ExpressionNode translateMpiBufTypeExpression(ExpressionNode mpiBuf,
			ExpressionNode preState, ExpressionNode theState,
			List<BlockItemNode> prefix, List<BlockItemNode> suffix)
			throws SyntaxException {
		return commonTranslateMpiBufTypeExpression(mpiBuf, preState, theState,
				prefix, suffix);
	}

	/* ********* common helper methods ********** */

	/**
	 * <code>\mpi_on(e, proc) => $value_at(*pre_state, proc, e)</code>
	 * 
	 * @param mpiOn
	 *            the \mpi_on expression
	 * @param state
	 *            a collective state
	 * @throws SyntaxException
	 */
	ExpressionNode commonTranslateMpiOn(MPIContractExpressionNode mpiOn,
			ExpressionNode preState, ExpressionNode theState,
			List<BlockItemNode> prefix, List<BlockItemNode> suffix)
			throws SyntaxException {
		assert mpiOn
				.MPIContractExpressionKind() == MPIContractExpressionKind.MPI_ON;
		ExpressionNode expr = visitNodes(mpiOn.getArgument(0), preState,
				theState, prefix, suffix, false);
		ExpressionNode proc = visitNodes(mpiOn.getArgument(1), preState,
				theState, prefix, suffix, false);

		return nodeFactory.newValueAtNode(mpiOn.getSource(),
				nodeFactory.newStatenullNode(theState.getSource()), proc, expr);
	}

	ExpressionNode commonTranslateMpiBufTypeExpression(
			ExpressionNode mpiBufTypeExpr, ExpressionNode preState,
			ExpressionNode theState, List<BlockItemNode> prefix,
			List<BlockItemNode> suffix) throws SyntaxException {
		if (mpiBufTypeExpr
				.expressionKind() == ExpressionKind.MPI_CONTRACT_EXPRESSION) {
			// This expression could be kinds of \mpi_buf or \mpi_on. The two
			// cases shall already be handle in their own methods. So no op
			// needed here:
			return mpiBufTypeExpr;
		}
		return normalizeMpiBufExpression(mpiBufTypeExpr);
	}

	/**
	 * <code>\mpi_sig(datatype) => $mpi_sig(datatype)</code>, where $mpi_sig is
	 * an abstract function
	 * 
	 * @throws SyntaxException
	 */
	ExpressionNode commonTranslateMpiSig(MPIContractExpressionNode mpiSig,
			ExpressionNode preState, ExpressionNode theState,
			List<BlockItemNode> prefix, List<BlockItemNode> suffix)
			throws SyntaxException {
		assert mpiSig
				.MPIContractExpressionKind() == MPIContractExpressionKind.MPI_SIG;
		Source source = mpiSig.getSource();
		ExpressionNode datatypeExpr = mpiSig.getArgument(0);
		List<ExpressionNode> args = new LinkedList<>();
		ExpressionNode mpiSigAbstractFunction = nodeFactory
				.newIdentifierExpressionNode(source,
						nodeFactory.newIdentifierNode(source,
								MPI_CONTRACT_CONSTS.MPI_SIG_ABSTRACT_FUN));

		datatypeExpr = visitNodes(datatypeExpr, preState, theState, prefix,
				suffix, false);
		args.add(datatypeExpr);
		return nodeFactory.newFunctionCallNode(source, mpiSigAbstractFunction,
				args, null);
	}

	/**
	 * <code>\mpi_nonoverlapping(datatype) => $mpi_nonoverlapping(datatype)
	 * </code>, where $mpi_nonoverlapping is an abstract function
	 * 
	 * @throws SyntaxException
	 */
	ExpressionNode commonTranslateMpiNonoverlapping(
			MPIContractExpressionNode mpiNonoverlapping,
			ExpressionNode preState, ExpressionNode theState,
			List<BlockItemNode> prefix, List<BlockItemNode> suffix)
			throws SyntaxException {
		assert mpiNonoverlapping
				.MPIContractExpressionKind() == MPIContractExpressionKind.MPI_NONOVERLAPPING;
		Source source = mpiNonoverlapping.getSource();
		ExpressionNode datatypeExpr = mpiNonoverlapping.getArgument(0);
		List<ExpressionNode> args = new LinkedList<>();
		ExpressionNode abstractFunction = nodeFactory
				.newIdentifierExpressionNode(source,
						nodeFactory.newIdentifierNode(source,
								MPI_CONTRACT_CONSTS.MPI_NONOVERLAPPING_ABSTRACT_FUN));

		datatypeExpr = visitNodes(datatypeExpr, preState, theState, prefix,
				suffix, false);
		args.add(datatypeExpr);
		return nodeFactory.newFunctionCallNode(source, abstractFunction, args,
				null);
	}

	/**
	 * <code>\separated(a, b, ...) => $separated(a, b, ...)</code>, where
	 * <code>$separated</code> is a system state_f function.
	 * 
	 * @throws SyntaxException
	 * 
	 */
	ExpressionNode commonTranslateSeparated(SeparatedNode separatedNode,
			ExpressionNode preState, ExpressionNode theState,
			List<BlockItemNode> prefix, List<BlockItemNode> suffix,
			boolean isLocal) throws SyntaxException {
		Source source = separatedNode.getSource();
		IdentifierNode separatedFunName = nodeFactory.newIdentifierNode(source,
				"$separated");
		ExpressionNode separatedFun = nodeFactory
				.newIdentifierExpressionNode(source, separatedFunName);
		List<ExpressionNode> args = new LinkedList<>();
		List<Pair<SequenceNode<VariableDeclarationNode>, ExpressionNode>> setCompreContexts = new LinkedList<>();

		for (ExpressionNode arg : separatedNode.getDisjointTermSets())
			args.addAll(translateArgumentForSeparated(arg, setCompreContexts,
					preState, theState, prefix, suffix, isLocal));

		ExpressionNode systemCall = nodeFactory.newFunctionCallNode(source,
				separatedFun, args, null);

		if (!setCompreContexts.isEmpty()) {
			List<PairNode<SequenceNode<VariableDeclarationNode>, ExpressionNode>> boundVars = new LinkedList<>();
			SequenceNode<PairNode<SequenceNode<VariableDeclarationNode>, ExpressionNode>> boundVarsNode;
			List<ExpressionNode> restrictions = new LinkedList<>();

			for (Pair<SequenceNode<VariableDeclarationNode>, ExpressionNode> ctxPair : setCompreContexts) {
				Source ctxSource = ctxPair.left.getSource();

				restrictions.add(ctxPair.right);
				boundVars.add(
						nodeFactory.newPairNode(ctxSource, ctxPair.left, null));
			}
			boundVarsNode = nodeFactory.newSequenceNode(source,
					"set-comprehension translation bound variables", boundVars);
			systemCall = nodeFactory.newQuantifiedExpressionNode(source,
					Quantifier.FORALL, boundVarsNode, conjunct(restrictions),
					systemCall, null);
		}
		return systemCall;
	}

	/**
	 * translate each argument of a <code>\separated</code> expression. Special
	 * handing for \mpi_buf and curly-braced term set expressions.
	 * 
	 * @throws SyntaxException
	 */
	private List<ExpressionNode> translateArgumentForSeparated(
			ExpressionNode arg,
			List<Pair<SequenceNode<VariableDeclarationNode>, ExpressionNode>> outputSetCompreContexts,
			ExpressionNode preState, ExpressionNode theState,
			List<BlockItemNode> prefix, List<BlockItemNode> suffix,
			boolean isLocal) throws SyntaxException {
		List<ExpressionNode> args = new LinkedList<>();
		Type argType = arg.getType();

		if (argType.kind() == Type.TypeKind.ACSL_MPI_TYPE) {
			// for "\mpi_buf(ptr, count, datatype)", we conservatively only
			// compare "ptr" with other pointers specified in this separated
			// function. TODO: this is conservative but not yet accurate.
			arg = visitNodes(arg, preState, theState, prefix, suffix, isLocal);
			arg = normalizeMpiBufExpressionWorker(arg)[0];
			args.add(arg.copy());
		} else if (arg.expressionKind() == ExpressionKind.CURLY_BRACED_TERM_SET)
			args.addAll(translateTermSetForSeparated(
					(CurlyBracedTermSetNode) arg, outputSetCompreContexts,
					preState, theState, prefix, suffix, isLocal));
		else {
			if (argType.kind() != TypeKind.SET)
				args.add(visitNodes(arg, preState, theState, prefix, suffix,
						isLocal));
			else {
				Pair<ExpressionNode, Pair<ExpressionNode, ExpressionNode>> ptrSet = checkPointerSetLimitation(
						arg);

				if (ptrSet == null)
					throw new CIVLUnimplementedFeatureException(
							"unsupported pointer set form "
									+ arg.prettyRepresentation(),
							arg.getSource());
				args.add(visitNodes(ptrSet.left, preState, theState, prefix,
						suffix, isLocal));
			}
		}
		return args;
	}

	/**
	 * translate a curly-braced term set expression for a
	 * <code>\separated</code> expression
	 * 
	 * @throws SyntaxException
	 */
	private List<ExpressionNode> translateTermSetForSeparated(
			CurlyBracedTermSetNode termSet,
			List<Pair<SequenceNode<VariableDeclarationNode>, ExpressionNode>> outputContexts,
			ExpressionNode preState, ExpressionNode theState,
			List<BlockItemNode> prefix, List<BlockItemNode> suffix,
			boolean isLocal) throws SyntaxException {
		List<ExpressionNode> args = new LinkedList<>();

		if (termSet.isExplicit()) {
			for (ExpressionNode elt : termSet.getExplicitElements())
				args.addAll(translateArgumentForSeparated(elt, outputContexts,
						preState, theState, prefix, suffix, isLocal));
			return args;
		}

		ExpressionNode ptrTerm = termSet.getSetComprehensionTerms()
				.getSequenceChild(0);
		SequenceNode<VariableDeclarationNode> binders = termSet.getBinders();
		ExpressionNode pred = termSet.getPredicate();

		args.addAll(translateArgumentForSeparated(ptrTerm, outputContexts,
				preState, theState, prefix, suffix, isLocal));
		pred = visitNodes(pred, preState, theState, prefix, suffix, isLocal);

		Pair<SequenceNode<VariableDeclarationNode>, ExpressionNode> contextPair = new Pair<>(
				binders.copy(), pred);

		outputContexts.add(contextPair);
		return args;
	}

	/**
	 * Translate <code>\mpi_comm_rank or \mpi_comm_size</code> to
	 * <code>$mpi_comm_rank or $mpi_comm_size</code>.
	 */
	ExpressionNode commonTranslateMpiConstants(
			MPIContractExpressionNode mpiConst) {
		Source source = mpiConst.getSource();
		if (mpiConst
				.MPIContractExpressionKind() == MPIContractExpressionKind.MPI_COMM_RANK) {
			return nodeFactory.newIdentifierExpressionNode(source,
					nodeFactory.newIdentifierNode(source,
							MPI_CONTRACT_CONSTS.MPI_COMM_RANK_CONST));

		} else {
			return nodeFactory.newIdentifierExpressionNode(source,
					nodeFactory.newIdentifierNode(source,
							MPI_CONTRACT_CONSTS.MPI_COMM_SIZE_CONST));
		}
	}

	/**
	 * Translates <code>\old(e)</code> to <code>
	 *   $value_at(_co_pre_state, $mpi_comm_rank, e), if it is in an MPI contract; OR
	 *   $value_at(_pre_state, 0, e), if it is in a local contract.
	 * </code>
	 * 
	 * @throws SyntaxException
	 */
	ExpressionNode commonTranslateOld(OperatorNode oldExpr,
			ExpressionNode preState, ExpressionNode theState,
			List<BlockItemNode> prefix, List<BlockItemNode> suffix,
			boolean isLocal) throws SyntaxException {
		ExpressionNode expr = visitNodes(oldExpr.getArgument(0), preState,
				theState, prefix, suffix, isLocal);

		return mkValueAtExpressionWithDefaultProc(expr, preState, isLocal);
	}

	/**
	 * Translate <code>\result</code> to <code>$result</code>
	 */
	ExpressionNode commonTranslateResult(ExpressionNode resultExpr) {
		Source source = resultExpr.getSource();
		return nodeFactory.newIdentifierExpressionNode(source, nodeFactory
				.newIdentifierNode(source, MPI_CONTRACT_CONSTS.MPI_RESULT));
	}

	/**
	 * just translates sub-expressions of the \mpi_agree expression:
	 */
	ExpressionNode commonTranslateMpiAgree(MPIContractExpressionNode mpiAgree,
			ExpressionNode preState, ExpressionNode theState,
			List<BlockItemNode> prefix, List<BlockItemNode> suffix)
			throws SyntaxException {
		ExpressionNode expr = mpiAgree.getArgument(0);
		int childIdx = expr.childIndex();

		expr = visitNodes(expr, preState, theState, prefix, suffix, false);
		mpiAgree = (MPIContractExpressionNode) mpiAgree.copy();
		mpiAgree.removeChild(childIdx);
		mpiAgree.setChild(childIdx, expr);
		return mpiAgree;
	}

	/**
	 * just translates sub-expressions of the \mpi_agree expression:
	 */
	ExpressionNode commonTranslateMpiReduce(MPIContractExpressionNode mpiReduce,
			ExpressionNode preState, ExpressionNode theState,
			List<BlockItemNode> prefix, List<BlockItemNode> suffix)
			throws SyntaxException {
		int numArgs = mpiReduce.numArguments();
		ExpressionNode mpiReduceCopy = mpiReduce.copy();

		for (int i = 0; i < numArgs; i++) {
			ExpressionNode arg = mpiReduce.getArgument(i);
			int childIdx = arg.childIndex();

			arg = visitNodes(arg, preState, theState, prefix, suffix, false);
			mpiReduceCopy.removeChild(childIdx);
			mpiReduceCopy.setChild(childIdx, arg);
		}
		return mpiReduceCopy;
	}

	/**
	 * Translates <code>
	 *   1. \valid(p) to $valid(p)
	 *   
	 *   2. \valid(p + (l .. h)) to $valid(p[l]) && $valid(p[h])
	 *   
	 *   3. \valid(\mpi_buf(p, c, d)) to $valid(p[0]) && $valid(p[c * (tmp/sizeof(T)) - 1]), where
	 *      tmp = sizeofDatatype(d) and T is the referenced type of p. There is a side condition for
	 *      case 3:
	 *        tmp % sizeof(T) == 0
	 * </code>
	 * 
	 * @param valid
	 *            the <code>\valid</code> expression node
	 * @param datatypeTmpVarCreator
	 *            the {@link Function} that can create a new temporary variable
	 *            {@link IdentifierNode} for holding the size of an MPI_Datatype
	 *            when necessary
	 * @param preState
	 *            the pre (collective) state
	 * @param theState
	 *            the current (collective) state
	 * @param prefix
	 *            output argument, a list of {@link BlockItemNode}s that will be
	 *            placed before snapshot taking
	 * @param suffix
	 *            output argument, a list of {@link BlockItemNode}s that will be
	 *            placed after
	 * @param isLocal
	 *            true iff the expression is in a purely sequential contract
	 * @throws SyntaxException
	 */
	ExpressionNode translateValidAsPureAssertion(OperatorNode valid,
			Function<ExpressionNode, IdentifierNode> datatypeTmpVarCreator,
			ExpressionNode preState, ExpressionNode theState,
			List<BlockItemNode> prefix, List<BlockItemNode> suffix,
			boolean isLocal) throws SyntaxException {
		ExpressionNode ptrs = valid.getArgument(0);
		Source source = valid.getSource();

		if (ptrs.getType().kind() == TypeKind.ACSL_MPI_TYPE) {
			return translateValidMpiBuf(valid, datatypeTmpVarCreator, preState,
					theState, prefix, suffix);
		}

		ExpressionNode validSystemFun = nodeFactory.newIdentifierExpressionNode(
				source, nodeFactory.newIdentifierNode(source,
						MPI_CONTRACT_CONSTS.VALID_SYSTEM_FUN));
		List<ExpressionNode> args = new LinkedList<>();
		ExpressionNode validSystemFunCall;

		if (ptrs.getType().kind() != TypeKind.SET) {
			// a single pointer:
			ExpressionNode ptr = visitNodes(valid.getArgument(0), preState,
					theState, prefix, suffix, isLocal);

			args.add(ptr);
			validSystemFunCall = nodeFactory.newFunctionCallNode(source,
					validSystemFun, args, null);
		} else {
			// check "ptrs" for limitations:
			Pair<ExpressionNode, Pair<ExpressionNode, ExpressionNode>> pair = checkPointerSetLimitation(
					ptrs);
			ExpressionNode ptr = visitNodes(pair.left, preState, theState,
					prefix, suffix, isLocal);
			ExpressionNode low = visitNodes(pair.right.left, preState, theState,
					prefix, suffix, isLocal);
			ExpressionNode high = visitNodes(pair.right.right, preState,
					theState, prefix, suffix, isLocal);
			ExpressionNode accessLow = nodeFactory.newOperatorNode(source,
					Operator.SUBSCRIPT, ptr, low);
			ExpressionNode accessHigh = nodeFactory.newOperatorNode(source,
					Operator.SUBSCRIPT, ptr.copy(), high);
			ExpressionNode accessLowAddr = nodeFactory.newOperatorNode(source,
					Operator.ADDRESSOF, accessLow);
			ExpressionNode accessHighAddr = nodeFactory.newOperatorNode(source,
					Operator.ADDRESSOF, accessHigh);

			args.add(accessLowAddr);
			validSystemFunCall = nodeFactory.newFunctionCallNode(source,
					validSystemFun, args, null);

			List<ExpressionNode> args2 = new LinkedList<>();

			args2.add(accessHighAddr);
			validSystemFunCall = nodeFactory.newOperatorNode(source,
					Operator.LAND, validSystemFunCall,
					nodeFactory.newFunctionCallNode(source,
							validSystemFun.copy(), args2, null));
		}
		return validSystemFunCall;
	}

	/**
	 * helper method of {@link #translateValid(OperatorNode, List, List, List)}
	 * for the case where the argument is an <code>\mpi_buf</code> expression.
	 * 
	 * @throws SyntaxException
	 */
	private ExpressionNode translateValidMpiBuf(OperatorNode mpiBufValid,
			Function<ExpressionNode, IdentifierNode> datatypeTmpVarCreation,
			ExpressionNode preState, ExpressionNode theState,
			List<BlockItemNode> prefix, List<BlockItemNode> suffix)
			throws SyntaxException {		
		ExpressionNode mpiBufExpr = mpiBufValid.getArgument(0);

		mpiBufExpr = visitNodes(mpiBufValid.getArgument(0), preState, theState,
				prefix, suffix, false);

		MPIContractExpressionNode normalizedMpiBuf = normalizeMpiBufExpression(
				mpiBufExpr);
		ExpressionNode ptr = normalizedMpiBuf.getArgument(0);
		ExpressionNode count = normalizedMpiBuf.getArgument(1);
		ExpressionNode datatype = normalizedMpiBuf.getArgument(2);
		Source source = mpiBufValid.getSource();
		// create $valid(p) && $valid($mpi_pointer_add_sys(p, c-1,
		// datatype_size))
		// note that datatype has been handled by visitNodes
		ExpressionNode ptrLowAccess = ptr.copy();
		ExpressionNode countMinusOne = nodeFactory.newOperatorNode(
				count.getSource(), Operator.MINUS, count.copy(),
				nodeFactory.newIntConstantNode(count.getSource(), 1));
		ExpressionNode ptrHighAccess = mpiPointerAddSystemCall(ptr.copy(),
				countMinusOne, datatype.copy());
		ExpressionNode validSystemFun = nodeFactory.newIdentifierExpressionNode(
				source, nodeFactory.newIdentifierNode(source,
						MPI_CONTRACT_CONSTS.VALID_SYSTEM_FUN));
		ExpressionNode validSystemFunCall;
		List<ExpressionNode> args = new LinkedList<>();
		List<ExpressionNode> args2 = new LinkedList<>();

		args.add(ptrLowAccess);
		validSystemFunCall = nodeFactory.newFunctionCallNode(source,
				validSystemFun, args, null);
		args2.add(ptrHighAccess);
		validSystemFunCall = nodeFactory.newOperatorNode(source, Operator.LAND,
				validSystemFunCall, nodeFactory.newFunctionCallNode(source,
						validSystemFun.copy(), args2, null));
		return validSystemFunCall;

		/*
		 * 
		 * 
		 * 
		 * // create $valid(&p[0]) && $valid(&p[c * (tmp/sizeof(T)) - 1]) //
		 * note that datatype has been handled by visitNodes Type pointedType =
		 * ptrType.referencedType(); TypeNode pointedTypeNode; ExpressionNode
		 * extent, sizeofPointedType, validSystemFun, validSystemFunCall,
		 * ptrLowAccess, ptrHighAccess; List<ExpressionNode> args, args2;
		 * 
		 * if (pointedType.kind() == TypeKind.QUALIFIED) pointedType =
		 * ((QualifiedObjectType) pointedType).getBaseType(); if
		 * (pointedType.kind() == TypeKind.VOID) { pointedType =
		 * nodeFactory.typeFactory() .basicType(BasicTypeKind.CHAR);
		 * 
		 * TypeNode castedToTypeNode = nodeFactory.newPointerTypeNode(
		 * ptr.getSource(), BaseWorker.typeNode(ptr.getSource(), pointedType,
		 * nodeFactory));
		 * 
		 * ptr = nodeFactory.newCastNode(ptr.getSource(), castedToTypeNode,
		 * ptr.copy()); }
		 * 
		 * pointedTypeNode = BaseWorker.typeNode(datatypeSource, pointedType,
		 * nodeFactory); sizeofPointedType =
		 * nodeFactory.newSizeofNode(datatypeSource, pointedTypeNode); extent =
		 * nodeFactory.newOperatorNode(datatypeSource, Operator.TIMES,
		 * count.copy(), nodeFactory.newOperatorNode(datatypeSource,
		 * Operator.DIV, datatype.copy(), sizeofPointedType)); ptrLowAccess =
		 * nodeFactory.newOperatorNode(source, Operator.SUBSCRIPT, ptr.copy(),
		 * nodeFactory.newIntConstantNode(source, 0)); ptrLowAccess =
		 * nodeFactory.newOperatorNode(ptrLowAccess.getSource(),
		 * Operator.ADDRESSOF, ptrLowAccess); ptrHighAccess =
		 * nodeFactory.newOperatorNode(source, Operator.SUBSCRIPT, ptr.copy(),
		 * nodeFactory.newOperatorNode(source, Operator.MINUS, extent,
		 * nodeFactory.newIntConstantNode(source, 1))); ptrHighAccess =
		 * nodeFactory.newOperatorNode(ptrHighAccess.getSource(),
		 * Operator.ADDRESSOF, ptrHighAccess); validSystemFun =
		 * nodeFactory.newIdentifierExpressionNode(source,
		 * nodeFactory.newIdentifierNode(source,
		 * MPI_CONTRACT_CONSTS.VALID_SYSTEM_FUN)); args = new LinkedList<>();
		 * args.add(ptrLowAccess); validSystemFunCall =
		 * nodeFactory.newFunctionCallNode(source, validSystemFun, args, null);
		 * args2 = new LinkedList<>(); args2.add(ptrHighAccess);
		 * validSystemFunCall = nodeFactory.newOperatorNode(source,
		 * Operator.LAND, validSystemFunCall,
		 * nodeFactory.newFunctionCallNode(source, validSystemFun.copy(), args2,
		 * null));
		 * 
		 * // create assertion for side condition: tmp % sizeof(T) == 0
		 * ExpressionNode sideCond = nodeFactory.newOperatorNode(source,
		 * Operator.EQUALS, nodeFactory.newOperatorNode(source, Operator.MOD,
		 * datatype.copy(), sizeofPointedType.copy()),
		 * nodeFactory.newIntConstantNode(source, 0));
		 * 
		 * suffix.add(createAssertion(sideCond)); return validSystemFunCall;
		 */
	}
}
