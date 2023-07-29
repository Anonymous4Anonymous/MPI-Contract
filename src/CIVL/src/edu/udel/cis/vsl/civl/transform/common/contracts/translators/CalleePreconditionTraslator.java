package edu.udel.cis.vsl.civl.transform.common.contracts.translators;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

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
import edu.udel.cis.vsl.civl.model.IF.CIVLSyntaxException;

public class CalleePreconditionTraslator extends CommonPrePostCondTranslator {

	private Map<String, IdentifierNode> datatypeToTempVar;

	public CalleePreconditionTraslator(NodeFactory nodeFactory,
			TokenFactory tokenFactory) {
		super(nodeFactory, tokenFactory);
		datatypeToTempVar = new HashMap<>();
	}

	public Map<String, IdentifierNode> getDatatypeToTempVarMap() {
		return datatypeToTempVar; // TODO this is some hack
	}

	public Supplier<ContractClauseTranslation> translatePrecondition(
			List<ExpressionNode> conditions, List<ExpressionNode> preds,
			ExpressionNode preState, ExpressionNode theState, boolean isLocal)
			throws SyntaxException {
		List<BlockItemNode> prefix = new LinkedList<>();
		List<BlockItemNode> suffix = new LinkedList<>();
		List<ExpressionNode> newConds = new LinkedList<>();
		List<ExpressionNode> newPreds = new LinkedList<>();

		for (ExpressionNode cond : conditions)
			newConds.add(visitNodes(cond, preState.copy(), theState.copy(),
					prefix, suffix, isLocal));
		for (ExpressionNode pred : preds)
			newPreds.add(visitNodes(pred, preState.copy(), theState.copy(),
					prefix, suffix, isLocal));

		ExpressionNode thePredicate = conjunct(newPreds);

		newPreds.clear();
		if (!isLocal)
			// pred -> $value_at(_pre_state_, $mpi_comm_rank, pred);
			thePredicate = mkValueAtExpressionWithDefaultProc(thePredicate,
					theState.copy(), isLocal);
		newPreds.add(thePredicate);
		return new Supplier<ContractClauseTranslation>() {
			@Override
			public ContractClauseTranslation get() {
				return new ContractClauseTranslation(newConds, newPreds, prefix,
						suffix);
			}
		};
	}

	@Override
	ExpressionNode translateMpiOn(MPIContractExpressionNode mpiOn,
			ExpressionNode preState, ExpressionNode theState,
			List<BlockItemNode> prefix, List<BlockItemNode> suffix)
			throws SyntaxException {
		return commonTranslateMpiOn(mpiOn, preState, theState, prefix, suffix);
	}

	@Override
	ExpressionNode translateOld(OperatorNode oldExpr, ExpressionNode preState,
			ExpressionNode theState, List<BlockItemNode> prefix,
			List<BlockItemNode> suffix, boolean isLocal)
			throws SyntaxException {
		throw new CIVLSyntaxException(
				"\\old construct shall not appear in pre-conditions.",
				oldExpr.getSource());
	}

	@Override
	ExpressionNode translateResult(ExpressionNode resultExpr) {
		throw new CIVLSyntaxException(
				"\\result construct shall not appear in pre-conditions.",
				resultExpr.getSource());
	}

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

	@Override
	ExpressionNode translateMpiNonoverlapping(
			MPIContractExpressionNode mpiNonoverlapping,
			ExpressionNode preState, ExpressionNode theState,
			List<BlockItemNode> prefix, List<BlockItemNode> suffix)
			throws SyntaxException {
		return commonTranslateMpiNonoverlapping(mpiNonoverlapping, preState,
				theState, prefix, suffix);
	}

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
		return this.commonTranslateMpiReduce(mpiReduce, preState, theState,
				prefix, suffix);
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

	@Override
	ExpressionNode translateSeparated(SeparatedNode separatedNode,
			ExpressionNode preState, ExpressionNode theState,
			List<BlockItemNode> prefix, List<BlockItemNode> suffix,
			boolean isLocal) throws SyntaxException {
		return commonTranslateSeparated(separatedNode, preState, theState,
				prefix, suffix, isLocal);
	}

	@Override
	ExpressionNode translateMpiBufTypeExpression(ExpressionNode mpiBuf,
			ExpressionNode preState, ExpressionNode theState,
			List<BlockItemNode> prefix, List<BlockItemNode> suffix)
			throws SyntaxException {
		return commonTranslateMpiBufTypeExpression(mpiBuf, preState, theState,
				prefix, suffix);
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
				+ "_pre_" + +datatypeToTempVar.size();
		IdentifierNode tmpVarIdent = nodeFactory
				.newIdentifierNode(datatype.getSource(), tmpVarName);

		datatypeToTempVar.put(datatype.prettyRepresentation().toString(),
				tmpVarIdent);
		return tmpVarIdent;
	}
}
