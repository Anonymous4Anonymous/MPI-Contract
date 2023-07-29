package edu.udel.cis.vsl.civl.transform.common.contracts.translators;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import edu.udel.cis.vsl.abc.ast.node.IF.IdentifierNode;
import edu.udel.cis.vsl.abc.ast.node.IF.NodeFactory;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.MPIContractExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.SeparatedNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.IdentifierExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.BlockItemNode;
import edu.udel.cis.vsl.abc.token.IF.Source;
import edu.udel.cis.vsl.abc.token.IF.SyntaxException;
import edu.udel.cis.vsl.abc.token.IF.TokenFactory;

public class TargetPostconditionTranslator extends CommonPrePostCondTranslator {

	/**
	 * A cache for re-use temporary variables that hold translation of
	 * MPI_Datatype experssions in \mpi_buf expressions.
	 */
	private Map<String, IdentifierNode> datatypeToTempVar;

	public TargetPostconditionTranslator(NodeFactory nodeFactory,
			TokenFactory tokenFactory,
			Map<String, IdentifierNode> datatypeToTempVar) {
		super(nodeFactory, tokenFactory);
		this.datatypeToTempVar = datatypeToTempVar;
	}

	/**
	 * <p>
	 * Translates a contract clause under an contract assumption and returns a
	 * {@link ContractClauseTranslation}.
	 * </p>
	 * 
	 * @param conditions
	 *            the assumption of the behavior of the translating
	 *            post-condition
	 * @param preidcates
	 *            the post-condition that will be translated
	 * @param preState
	 *            the (collective) pre-state
	 * @param postState
	 *            the (collective) post-state
	 * @param isLocal
	 *            true iff the translating post-condition is a part of
	 *            sequential contract
	 * @throws SyntaxException
	 */
	public ContractClauseTranslation translatePostcond4Target(
			List<ExpressionNode> conditions, List<ExpressionNode> predicates,
			ExpressionNode preState, ExpressionNode theState, boolean isLocal)
			throws SyntaxException {
		List<BlockItemNode> prefix = new LinkedList<>();
		List<BlockItemNode> suffix = new LinkedList<>();
		List<ExpressionNode> newConds = new LinkedList<>();
		List<ExpressionNode> newPreds = new LinkedList<>();

		for (ExpressionNode cond : conditions)
			newConds.add(visitNodes(cond, preState.copy(), theState.copy(),
					prefix, suffix, isLocal));
		for (ExpressionNode pred : predicates)
			newPreds.add(visitNodes(pred, preState.copy(), theState.copy(),
					prefix, suffix, isLocal));

		ExpressionNode thePredicate = conjunct(newPreds);

		newPreds.clear();
		if (!isLocal)
			thePredicate = mkValueAtExpressionWithDefaultProc(thePredicate,
					theState.copy(), isLocal);
		newPreds.add(thePredicate);
		return new ContractClauseTranslation(newConds, newPreds, prefix,
				suffix);
	}

	@Override
	ExpressionNode translateMpiOn(MPIContractExpressionNode mpiOn,
			ExpressionNode preState, ExpressionNode theState,
			List<BlockItemNode> prefix, List<BlockItemNode> suffix)
			throws SyntaxException {
		return commonTranslateMpiOn(mpiOn, preState, theState, prefix, suffix);
	}

	/**
	 * Translates <code>\old(e)</code> to <code>
	 *   $value_at(_co_pre_state, $mpi_comm_rank, e), if it is in an MPI contract; OR
	 *   $value_at(_pre_state, 0, e), if it is in a local contract.
	 * </code>
	 * 
	 * @throws SyntaxException
	 */
	@Override
	ExpressionNode translateOld(OperatorNode oldExpr, ExpressionNode preState,
			ExpressionNode theState, List<BlockItemNode> prefix,
			List<BlockItemNode> suffix, boolean isLocal)
			throws SyntaxException {
		return commonTranslateOld(oldExpr, preState, theState, prefix, suffix,
				isLocal);
	}

	/**
	 * Translate <code>\result</code> to <code>$result</code>
	 */
	@Override
	ExpressionNode translateResult(ExpressionNode resultExpr) {
		return this.commonTranslateResult(resultExpr);
	}

	/**
	 * see
	 * {@link #commonTranslateMpiSig(MPIContractExpressionNode, List, List, List)}
	 * 
	 * @throws SyntaxException
	 */
	@Override
	ExpressionNode translateMpiSig(MPIContractExpressionNode mpiSig,
			ExpressionNode preState, ExpressionNode theState,
			List<BlockItemNode> prefix, List<BlockItemNode> suffix)
			throws SyntaxException {
		return commonTranslateMpiSig(mpiSig, preState, theState, prefix,
				suffix);
	}

	@Override
	ExpressionNode translateMpiAgree(MPIContractExpressionNode mpiAgree,
			ExpressionNode preState, ExpressionNode theState,
			List<BlockItemNode> prefix, List<BlockItemNode> suffix)
			throws SyntaxException {
		return commonTranslateMpiAgree(mpiAgree, preState, theState, prefix,
				suffix);
	}

	/**
	 * {@link #commonTranslateMpiNonoverlapping(MPIContractExpressionNode, List, List, List)}
	 * 
	 * @throws SyntaxException
	 */
	@Override
	ExpressionNode translateMpiNonoverlapping(
			MPIContractExpressionNode mpiNonoverlapping,
			ExpressionNode preState, ExpressionNode theState,
			List<BlockItemNode> prefix, List<BlockItemNode> suffix)
			throws SyntaxException {
		return commonTranslateMpiNonoverlapping(mpiNonoverlapping, preState,
				theState, prefix, suffix);
	}

	/**
	 * Use temporary variables to hold the side of the MPI_Datatype
	 * 
	 * @throws SyntaxException
	 */
	@Override
	ExpressionNode translateMpiDatatypeInMpiBuf(ExpressionNode datatype,
			ExpressionNode preState, ExpressionNode theState,
			List<BlockItemNode> prefix, List<BlockItemNode> suffix)
			throws SyntaxException {
		datatype = visitNodes(datatype, preState, theState, prefix, suffix,
				false);

		IdentifierNode tempVarIdent = datatypeToTempVar
				.get(datatype.prettyRepresentation().toString());
		IdentifierExpressionNode tempVarAccess;
		Source source = datatype.getSource();

		if (tempVarIdent == null) {
			tempVarIdent = getNextTempVarNameForDatatype(datatype);
			tempVarAccess = nodeFactory.newIdentifierExpressionNode(source,
					tempVarIdent);
			prefix.addAll(createDatatypeSizeHolderVariable(tempVarIdent.name(),
					datatype.copy()));
		} else
			tempVarAccess = nodeFactory.newIdentifierExpressionNode(source,
					tempVarIdent.copy());
		return tempVarAccess;
	}

	@Override
	ExpressionNode translateMpiReduce(MPIContractExpressionNode mpiReduce,
			ExpressionNode preState, ExpressionNode theState,
			List<BlockItemNode> prefix, List<BlockItemNode> suffix)
			throws SyntaxException {
		return commonTranslateMpiReduce(mpiReduce, preState, theState, prefix,
				suffix);
	}

	@Override
	ExpressionNode translateValid(OperatorNode valid, ExpressionNode preState,
			ExpressionNode theState, List<BlockItemNode> prefix,
			List<BlockItemNode> suffix, boolean isLocal)
			throws SyntaxException {
		Function<ExpressionNode, IdentifierNode> datatypeTmpVarCreator = (
				dt) -> {
			IdentifierNode tmpVarIdent = datatypeToTempVar
					.get(dt.prettyRepresentation().toString());

			if (tmpVarIdent == null) {
				tmpVarIdent = getNextTempVarNameForDatatype(dt);
				prefix.addAll(createDatatypeSizeHolderVariable(
						tmpVarIdent.name(), dt.copy()));
			}
			return tmpVarIdent;
		};
		return translateValidAsPureAssertion(valid, datatypeTmpVarCreator,
				preState, theState, prefix, suffix, isLocal);
	}

	/**
	 * <code>\separated(a, b, ...) => $separated(a, b, ...)</code>, where
	 * <code>$separated</code> is a system state_f function.
	 * 
	 * @throws SyntaxException
	 * 
	 */
	@Override
	ExpressionNode translateSeparated(SeparatedNode separatedNode,
			ExpressionNode preState, ExpressionNode theState,
			List<BlockItemNode> prefix, List<BlockItemNode> suffix,
			boolean isLocal) throws SyntaxException {
		return commonTranslateSeparated(separatedNode, preState, theState,
				prefix, suffix, isLocal);
	}

	/**
	 * normalize <code>\mpi_buf_type</code> expressions
	 * 
	 * @throws SyntaxException
	 */
	@Override
	ExpressionNode translateMpiBufTypeExpression(ExpressionNode mpiBuf,
			ExpressionNode preState, ExpressionNode theState,
			List<BlockItemNode> prefix, List<BlockItemNode> suffix)
			throws SyntaxException {
		return this.commonTranslateMpiBufTypeExpression(mpiBuf, preState,
				theState, prefix, suffix);
	}

	@Override
	ExpressionNode translateMpiConstants(MPIContractExpressionNode mpiConst) {
		return commonTranslateMpiConstants(mpiConst);
	}

	@Override
	ExpressionNode translateMpiExtent(MPIContractExpressionNode mpiExtent,
			ExpressionNode preState, ExpressionNode theState,
			List<BlockItemNode> prefix, List<BlockItemNode> suffix)
			throws SyntaxException {
		ExpressionNode arg = visitNodes(mpiExtent.getArgument(0), preState,
				theState, prefix, suffix, false);

		return mpiExtentCall(arg);
	}

	/* ******************* helper methods ******************* */
	/**
	 * @param datatype
	 * @return a new indetifier node of a new unique temporary variable name for
	 *         holding datatype sizes
	 */
	private IdentifierNode getNextTempVarNameForDatatype(
			ExpressionNode datatype) {
		String tmpVarName = MPI_CONTRACT_CONSTS.MPI_DATATYPE_TEMP_VAR_NAME
				+ "_post_" + +datatypeToTempVar.size();
		IdentifierNode tmpVarIdent = nodeFactory
				.newIdentifierNode(datatype.getSource(), tmpVarName);

		datatypeToTempVar.put(datatype.prettyRepresentation().toString(),
				tmpVarIdent);
		return tmpVarIdent;
	}
}
