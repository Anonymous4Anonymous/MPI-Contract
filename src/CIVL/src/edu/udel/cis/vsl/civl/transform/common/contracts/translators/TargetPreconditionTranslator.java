package edu.udel.cis.vsl.civl.transform.common.contracts.translators;

import static edu.udel.cis.vsl.civl.transform.common.contracts.translators.MPI_CONTRACT_CONSTS.MPI_IN_PLACE_SPOT_CONST;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.udel.cis.vsl.abc.ast.node.IF.IdentifierNode;
import edu.udel.cis.vsl.abc.ast.node.IF.NodeFactory;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.MPIContractExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.SeparatedNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.CastNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode.ExpressionKind;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.IdentifierExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode.Operator;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.BlockItemNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.StatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.TypeNode;
import edu.udel.cis.vsl.abc.ast.type.IF.AcslMPIType;
import edu.udel.cis.vsl.abc.ast.type.IF.AcslMPIType.AcslMPITypeKind;
import edu.udel.cis.vsl.abc.ast.type.IF.PointerType;
import edu.udel.cis.vsl.abc.ast.type.IF.QualifiedObjectType;
import edu.udel.cis.vsl.abc.ast.type.IF.StandardBasicType.BasicTypeKind;
import edu.udel.cis.vsl.abc.ast.type.IF.Type;
import edu.udel.cis.vsl.abc.ast.type.IF.Type.TypeKind;
import edu.udel.cis.vsl.abc.token.IF.Source;
import edu.udel.cis.vsl.abc.token.IF.SyntaxException;
import edu.udel.cis.vsl.abc.token.IF.TokenFactory;
import edu.udel.cis.vsl.civl.model.IF.CIVLSyntaxException;
import edu.udel.cis.vsl.civl.model.IF.CIVLUnimplementedFeatureException;
import edu.udel.cis.vsl.civl.transform.common.BaseWorker;
import edu.udel.cis.vsl.civl.util.IF.Pair;
import edu.udel.cis.vsl.civl.util.IF.Triple;

public class TargetPreconditionTranslator extends CommonPrePostCondTranslator {

	/**
	 * A cache for re-use temporary variables that hold translation of
	 * MPI_Datatype experssions in \mpi_buf expressions.
	 */
	private Map<String, IdentifierNode> datatypeToTempVar;

	/**
	 * collects all possible concrete pointer assignments to input pointers
	 */
	// TODO: this is some hack
	private Map<String, Triple<ExpressionNode, Boolean, List<StatementNode>>> nondetermChoices;

	/**
	 * name counter of temporary variables that mimic heaps
	 * 
	 * <p>
	 * heapVarCounter is local to this class as only pre-condition translation
	 * needs temporary heap var. So there will be no conflict between heap var
	 * names between the translation of pre- and post-conditions.
	 * </p>
	 */

	private int heapVarCounter = 0;

	private List<ExpressionNode> conditions = null;

	public TargetPreconditionTranslator(NodeFactory nodeFactory,
			TokenFactory tokenFactory) {
		super(nodeFactory, tokenFactory);
		this.datatypeToTempVar = new HashMap<>();
		this.nondetermChoices = new HashMap<>();
	}

	/**
	 * TODO: pass to postcondition translator. maybe not a good idea
	 */
	public Map<String, IdentifierNode> datatypeToTempVarMap() {
		return this.datatypeToTempVar;
	}

	public List<List<StatementNode>> nondetermChoices() {
		List<List<StatementNode>> results = new LinkedList<>();

		for (String key : nondetermChoices.keySet()) {
			Triple<ExpressionNode, Boolean, List<StatementNode>> triple = nondetermChoices
					.get(key);

			results.add(triple.third);
		}
		nondetermChoices.clear();
		return results;
	}

	/**
	 * <p>
	 * Translates a contract clause under an contract assumption and returns a
	 * {@link ContractClauseTranslation}.
	 * </p>
	 * 
	 * @param behaviorCondition
	 *            the assumption of the behavior of the translating
	 *            pre-condition
	 * @param preCond
	 *            the pre-condition that will be translated
	 * @param preState
	 *            the (collective) pre-state
	 * @param isLocal
	 *            true iff the translating pre-condition is a part of sequential
	 *            contract
	 * @throws SyntaxException
	 */
	public ContractClauseTranslation translatePrecond4Target(
			List<ExpressionNode> conditions, List<ExpressionNode> predicates,
			ExpressionNode preState, ExpressionNode theState, boolean isLocal)
			throws SyntaxException {
		List<BlockItemNode> prefix = new LinkedList<>();
		List<BlockItemNode> suffix = new LinkedList<>();
		List<ExpressionNode> newConds = new LinkedList<>();
		List<ExpressionNode> newPreds = new LinkedList<>();

		for (ExpressionNode cond : conditions)
			newConds.add(this.visitNodes(cond, preState.copy(), theState.copy(),
					prefix, suffix, isLocal));
		this.conditions = newConds;
		for (ExpressionNode predicate : predicates)
			newPreds.add(visitNodes(predicate, preState.copy(), theState.copy(),
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
	ExpressionNode processNode(ExpressionNode node, ExpressionNode preState,
			ExpressionNode theState, List<BlockItemNode> prefix,
			List<BlockItemNode> suffix, boolean isLocal)
			throws SyntaxException {
		if (node.expressionKind() == ExpressionKind.OPERATOR) {
			OperatorNode opNode = (OperatorNode) node;

			if (opNode.getOperator() == Operator.EQUALS
					|| opNode.getOperator() == Operator.NEQ) {
				ExpressionNode arg0 = opNode.getArgument(0);
				ExpressionNode arg1 = opNode.getArgument(1);

				if (isMpiInPlace(arg0)) {
					addNondetermChoice(arg1.copy(),
							mpiInPlace(arg1.getSource()), true);
				} else if (isMpiInPlace(arg1))
					addNondetermChoice(arg0.copy(),
							mpiInPlace(arg0.getSource()), true);
			}
		}
		return super.processNode(node, preState, theState, prefix, suffix,
				isLocal);
	}

	private boolean isMpiInPlace(ExpressionNode expr) {
		if (expr.expressionKind() == ExpressionKind.CAST) {
			CastNode castNode = (CastNode) expr;

			expr = castNode.getArgument();
			if (expr.expressionKind() != ExpressionKind.OPERATOR)
				return false;

			OperatorNode opNode = (OperatorNode) expr;

			if (opNode.getOperator() != Operator.ADDRESSOF)
				return false;

			expr = opNode.getArgument(0);
			if (expr.expressionKind() != ExpressionKind.IDENTIFIER_EXPRESSION)
				return false;

			IdentifierExpressionNode identExpr = (IdentifierExpressionNode) expr;

			return identExpr.getIdentifier().name()
					.equals(MPI_IN_PLACE_SPOT_CONST);
		}
		return false;
	}

	/* ************* special constructs translation methods ************ */

	@Override
	ExpressionNode translateMpiOn(MPIContractExpressionNode mpiOn,
			ExpressionNode preState, ExpressionNode theState,
			List<BlockItemNode> prefix, List<BlockItemNode> suffix)
			throws SyntaxException {
		return commonTranslateMpiOn(mpiOn, preState, theState, prefix, suffix);
	}

	/**
	 * not applicable
	 */
	@Override
	ExpressionNode translateOld(OperatorNode oldExpr, ExpressionNode preState,
			ExpressionNode theState, List<BlockItemNode> prefix,
			List<BlockItemNode> suffix, boolean isLocal) {
		throw new CIVLSyntaxException(
				"\\old construct shall not appear in pre-conditions.",
				oldExpr.getSource());
	}

	/**
	 * not applicable
	 */
	@Override
	ExpressionNode translateResult(ExpressionNode resultExpr) {
		throw new CIVLSyntaxException(
				"\\result construct shall not appear in pre-conditions.",
				resultExpr.getSource());
	}

	/**
	 * <code>\mpi_sig(datatype) => $mpi_sig(datatype)</code>, where $mpi_sig is
	 * an abstract function
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

	/**
	 * no-op
	 * 
	 * @throws SyntaxException
	 */
	@Override
	ExpressionNode translateMpiAgree(MPIContractExpressionNode mpiAgree,
			ExpressionNode preState, ExpressionNode theState,
			List<BlockItemNode> prefix, List<BlockItemNode> suffix)
			throws SyntaxException {
		return this.commonTranslateMpiAgree(mpiAgree, preState, theState,
				prefix, suffix);
	}

	/**
	 * <code>\mpi_nonoverlapping(datatype) => $mpi_nonoverlapping(datatype)
	 * </code>, where $mpi_nonoverlapping is an abstract function
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
	 * <code>\mpi_buf(p, count, datatype) => \mpi_buf(p, count, tmp)</code>,
	 * where <code>tmp = sizeofDatatype(datatype)</code> will be added to
	 * prefix.
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

	/**
	 * no-op
	 * 
	 * @throws SyntaxException
	 */
	@Override
	ExpressionNode translateMpiReduce(MPIContractExpressionNode mpiReduce,
			ExpressionNode preState, ExpressionNode theState,
			List<BlockItemNode> prefix, List<BlockItemNode> suffix)
			throws SyntaxException {
		return this.commonTranslateMpiReduce(mpiReduce, preState, theState,
				prefix, suffix);
	}

	/**
	 * <p>
	 * There is a limitation that <code>\valid</code> can only take
	 * <ol>
	 * <li>pointer set of the form <code>p + (0 .. h)</code></li>,
	 * <li>2) <code>p</code> itself, or</li>
	 * <li>3) an <code>\mpi_buf(...)</code> expression.</li>
	 * </p>
	 * </ol>
	 * 
	 * 
	 * <ol>
	 * <li>For case 1: <code>\valid(p + (l .. h)) ==> p == &tmp[0]</code>, where
	 * <code>
	 *  T tmp[h];        // T is the pointed type of p or char.
	 *  $havoc(&tmp);
	 * </code> is added to the prefix. <code>
	 * $assert(l == 0 && h > l) is added to postfix.
	 * </code></li>
	 * 
	 * <li>case 2 is trivially a special case of case 1</li>
	 * 
	 * <li>For case 3, see
	 * {@link #translateValidMpiBuf(ExpressionNode, List, List, List)}</li>
	 * </ol>
	 * 
	 * @throws SyntaxException
	 */
	@Override
	ExpressionNode translateValid(OperatorNode valid, ExpressionNode preState,
			ExpressionNode theState, List<BlockItemNode> prefix,
			List<BlockItemNode> suffix, boolean isLocal)
			throws SyntaxException {
		assert valid.getOperator() == Operator.VALID;
		ExpressionNode ptrs = valid.getArgument(0);
		Source source = valid.getSource();
		// create a temporary variable represening the valid heap space:
		String tmpVarName = MPI_CONTRACT_CONSTS.MPI_HEAP_VAR_NAME
				+ heapVarCounter++;
		ExpressionNode tmpVarAccess = nodeFactory.newIdentifierExpressionNode(
				source, nodeFactory.newIdentifierNode(source, tmpVarName));
		Type pointedType; // type of the base pointer in the \valid expression
		ExpressionNode basePointer; // the base pointer in the \valid expression
		ExpressionNode ptrToHeap; // the proper address of the created heap var

		if (ptrs.getType().kind() == TypeKind.ACSL_MPI_TYPE) {
			return translateValidMpiBuf(valid, preState, theState, prefix,
					suffix);
		}
		if (ptrs.getType().kind() != TypeKind.SET) {
			// a single pointer:
			basePointer = ptrs;
			pointedType = ((PointerType) basePointer.getType())
					.referencedType();
			prefix.add(variableDeclaration(tmpVarName, pointedType, source));
			prefix.add(havocObject(tmpVarAccess.copy()));
			ptrToHeap = nodeFactory.newOperatorNode(source, Operator.ADDRESSOF,
					tmpVarAccess.copy());
			/* ** this segment is some hack, ideally it is not needed ** */
			if (isLocal) {
				basePointer = visitNodes(basePointer, preState, theState,
						prefix, suffix, isLocal);

				StatementNode validPtrAssignStmt = nodeFactory
						.newExpressionStatementNode(nodeFactory.newOperatorNode(
								basePointer.getSource(), Operator.ASSIGN,
								basePointer, ptrToHeap));

				if (this.conditions != null && !this.conditions.isEmpty())
					validPtrAssignStmt = nodeFactory.newIfNode(
							validPtrAssignStmt.getSource(),
							conjunct(conditions), validPtrAssignStmt);
				prefix.add(validPtrAssignStmt);
				return nodeFactory.newOperatorNode(source, Operator.EQUALS,
						basePointer.copy(), ptrToHeap.copy());
			}
			/* ** end of hack segment ** */
		} else {
			// check "ptrs" for limitations:
			Pair<ExpressionNode, Pair<ExpressionNode, ExpressionNode>> pair = checkPointerSetLimitation(
					ptrs);

			if (pair == null)
				throw new CIVLUnimplementedFeatureException(
						"pointer set expression in a \\valid construct that does not have the form \"ptr + (l .. h)\"");

			ExpressionNode low = visitNodes(pair.right.left, preState, theState,
					prefix, suffix, isLocal);
			ExpressionNode high = visitNodes(pair.right.right, preState,
					theState, prefix, suffix, isLocal);
			ExpressionNode extent = nodeFactory.newOperatorNode(source,
					Operator.PLUS,
					nodeFactory.newOperatorNode(source, Operator.MINUS,
							high.copy(), low.copy()),
					nodeFactory.newIntConstantNode(source, 1));

			basePointer = pair.left;
			pointedType = ((PointerType) basePointer.getType())
					.referencedType();
			if (pointedType.kind() == TypeKind.VOID)
				pointedType = nodeFactory.typeFactory()
						.basicType(BasicTypeKind.CHAR);

			// declare array: T tmp[h - l + 1];
			prefix.addAll(arrayVariableDeclaration(tmpVarName, pointedType,
					extent, source));
			// havoc array: $havoc(&tmp);
			prefix.add(havocObject(tmpVarAccess));
			// add side condition: $assert(high >= 0 && low == 0) to postfixes:
			suffix.add(sideConditionAssertionOfValid(low, high, source));
			// pointer to the first element of the tmp var: &tmp[0]:
			ptrToHeap = nodeFactory.newOperatorNode(source, Operator.ADDRESSOF,
					nodeFactory.newOperatorNode(source, Operator.SUBSCRIPT,
							tmpVarAccess.copy(),
							nodeFactory.newIntConstantNode(source, 0)));

			/* ** this segment is some hack, ideally it is not needed ** */
			basePointer = visitNodes(basePointer, preState, theState, prefix,
					suffix, isLocal);

			StatementNode validPtrAssignStmt = nodeFactory
					.newExpressionStatementNode(
							nodeFactory.newOperatorNode(basePointer.getSource(),
									Operator.ASSIGN, basePointer, ptrToHeap));

			if (this.conditions != null && !this.conditions.isEmpty()) {
				validPtrAssignStmt = nodeFactory.newIfNode(
						validPtrAssignStmt.getSource(),
						conjunct(this.conditions), validPtrAssignStmt);
			}
			prefix.add(validPtrAssignStmt);
			return nodeFactory.newOperatorNode(source, Operator.EQUALS,
					basePointer.copy(), ptrToHeap.copy());
			/* ** end of hack segment ** */
		}
		// the expression that will replace the \valid term: p == &tmp[0]
		basePointer = visitNodes(basePointer, preState, theState, prefix,
				suffix, isLocal);
		addNondetermChoice(basePointer, ptrToHeap, false);
		return nodeFactory.newOperatorNode(source, Operator.EQUALS,
				basePointer.copy(), ptrToHeap.copy());
	}

	/**
	 * <p>
	 * Translates <code>\mpi_buf(p, count, datatype)</code> to
	 * <code>p == &tmp2[0]</code>, where <code>
	 * 
	 * size_t tmp = datatypeSize(datatype);
	 * T tmp2[count * (tmp1 / sizeof(T))];
	 * 
	 * </code> and T is the referenced type of p or char if the type is void.
	 * </p>
	 * <p>
	 * In addition, there is a side condition <code>tmp1 % sizeof(T) == 0</code>
	 * </p>
	 * 
	 * @param mpiBufValid
	 *            the valid expression whose argument is an \mpi_buf expression.
	 *            Node the \mpi_buf expression must have been normalized. see
	 *            {@link #normalizeMpiBufExpression(ExpressionNode)}.
	 * @throws SyntaxException
	 */
	private ExpressionNode translateValidMpiBuf(OperatorNode mpiBufValid,
			ExpressionNode preState, ExpressionNode theState,
			List<BlockItemNode> prefix, List<BlockItemNode> suffix)
			throws SyntaxException {
		assert mpiBufValid.getType().kind() == TypeKind.ACSL_MPI_TYPE;
		assert ((AcslMPIType) mpiBufValid.getType())
				.acslTypeKind() == AcslMPITypeKind.ACSL_MPI_BUF_TYPE;
		PointerType ptrType = extractMpiBufExprPointerType(mpiBufValid);
		MPIContractExpressionNode normalizedMpiBuf;

		normalizedMpiBuf = visitNodes(
				(MPIContractExpressionNode) mpiBufValid.getArgument(0),
				preState, theState, prefix, suffix, false);
		normalizedMpiBuf = normalizeMpiBufExpression(normalizedMpiBuf);

		ExpressionNode ptr = normalizedMpiBuf.getArgument(0);
		ExpressionNode count = normalizedMpiBuf.getArgument(1);
		ExpressionNode datatype = normalizedMpiBuf.getArgument(2);
		Source datatypeSource = datatype.getSource();
		// creating the tmp var that mimics the heap:
		// T tmp2[count * (tmp1 / sizeof(T))];
		Type pointedType = ptrType.referencedType();
		TypeNode pointedTypeNode;
		ExpressionNode extent, heapTmpVarAccess, sizeofPointedType;

		if (pointedType.kind() == TypeKind.QUALIFIED)
			pointedType = ((QualifiedObjectType) pointedType).getBaseType();
		if (pointedType.kind() == TypeKind.VOID)
			pointedType = nodeFactory.typeFactory()
					.basicType(BasicTypeKind.CHAR);
		pointedTypeNode = BaseWorker.typeNode(datatypeSource, pointedType,
				nodeFactory);
		sizeofPointedType = nodeFactory.newSizeofNode(datatypeSource,
				pointedTypeNode);
		extent = nodeFactory.newOperatorNode(datatypeSource, Operator.TIMES,
				count.copy(), nodeFactory.newOperatorNode(datatypeSource,
						Operator.DIV, datatype.copy(), sizeofPointedType));

		Source source = mpiBufValid.getSource();
		String heapTmpVarName = MPI_CONTRACT_CONSTS.MPI_HEAP_VAR_NAME
				+ heapVarCounter++;

		heapTmpVarAccess = nodeFactory.newIdentifierExpressionNode(source,
				nodeFactory.newIdentifierNode(source, heapTmpVarName));
		prefix.addAll(arrayVariableDeclaration(heapTmpVarName, pointedType,
				extent, source));

		// create the side condition assertion: tmp1 % sizeof(T) == 0
		ExpressionNode sideCond = nodeFactory.newOperatorNode(source,
				Operator.EQUALS,
				nodeFactory.newOperatorNode(source, Operator.MOD,
						datatype.copy(), sizeofPointedType.copy()),
				nodeFactory.newIntConstantNode(source, 0));

		suffix.add(createAssertion(sideCond));
		addNondetermChoice(ptr.copy(), heapTmpVarAccess, false);
		return nodeFactory.newOperatorNode(source, Operator.EQUALS, ptr.copy(),
				heapTmpVarAccess.copy());
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
		return commonTranslateMpiBufTypeExpression(mpiBuf, preState, theState,
				prefix, suffix);
	}

	/**
	 * Translate <code>\mpi_comm_rank or \mpi_comm_size</code> to
	 * <code>$mpi_comm_rank or $mpi_comm_size</code>.
	 */
	@Override
	ExpressionNode translateMpiConstants(MPIContractExpressionNode mpiConst) {
		return commonTranslateMpiConstants(mpiConst);
	}

	/**
	 * Translate <code>\mpi_extent(datatype)</code> to
	 * <code>$mpi_extent(datatype)</code>, where $mpi_extent is an abstract
	 * function
	 * 
	 * @throws SyntaxException
	 */
	@Override
	ExpressionNode translateMpiExtent(MPIContractExpressionNode mpiExtent,
			ExpressionNode preState, ExpressionNode theState,
			List<BlockItemNode> prefix, List<BlockItemNode> suffix)
			throws SyntaxException {
		ExpressionNode arg = visitNodes(mpiExtent.getArgument(0), preState,
				theState, prefix, suffix, false);

		return mpiExtentCall(arg);
	}

	/* ******************* helper methods ***************** */

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

	/**
	 * @param object
	 * @return <code>$havoc(&object)</code>
	 */
	private StatementNode havocObject(ExpressionNode object) {
		Source source = object.getSource();
		IdentifierExpressionNode havocFun = nodeFactory
				.newIdentifierExpressionNode(source, nodeFactory
						.newIdentifierNode(source, BaseWorker.HAVOC));
		ExpressionNode addrofObject = nodeFactory.newOperatorNode(source,
				Operator.ADDRESSOF, object);
		List<ExpressionNode> args = new LinkedList<>();

		args.add(addrofObject);
		return nodeFactory.newExpressionStatementNode(
				nodeFactory.newFunctionCallNode(source, havocFun, args, null));
	}

	/**
	 * creates an array variable declaration node
	 * 
	 * @param varName
	 *            the name of the variable
	 * @param elementType
	 *            the element {@link Type} of the array
	 * @param arrayExtent
	 *            the extent expression of the array
	 * @param source
	 *            the {@link Source} associated to the creating node
	 * @return an array variable declaration node
	 * @throws SyntaxException
	 */
	private List<BlockItemNode> arrayVariableDeclaration(String varName,
			Type elementType, ExpressionNode arrayExtent, Source source)
			throws SyntaxException {
		TypeNode typeNode = BaseWorker.typeNode(source, elementType,
				nodeFactory);
		List<BlockItemNode> results = new LinkedList<>();

		typeNode = nodeFactory.newArrayTypeNode(source, typeNode, arrayExtent);
		results.add(nodeFactory.newVariableDeclarationNode(source,
				nodeFactory.newIdentifierNode(source, varName), typeNode));

		// assume: extent > 0:
		ExpressionNode extentGTZero = nodeFactory.newOperatorNode(source,
				Operator.GT, arrayExtent.copy(),
				nodeFactory.newIntConstantNode(source, 0));

		// TODO: need to add an assertion for this at the end:
		results.add(createAssumption(extentGTZero));
		return results;
	}

	/**
	 * creates a variable declaration
	 * 
	 * @param varName
	 *            variable name
	 * @param type
	 *            the {@link Type} of the variable
	 * @param source
	 *            the {@link Source} associated to the creating node
	 * @return a variable declaration
	 * @throws SyntaxException
	 */
	private BlockItemNode variableDeclaration(String varName, Type type,
			Source source) throws SyntaxException {
		TypeNode typeNode = BaseWorker.typeNode(source, type, nodeFactory);

		return nodeFactory.newVariableDeclarationNode(source,
				nodeFactory.newIdentifierNode(source, varName), typeNode);
	}

	/**
	 * @param lower
	 *            an expression
	 * @param upper
	 *            an expression
	 * @param source
	 * @return <code>$assert(upper > 0 && lower == 0)</code>
	 */
	private StatementNode sideConditionAssertionOfValid(ExpressionNode lower,
			ExpressionNode upper, Source source) {
		ExpressionNode upperGTZero = nodeFactory.newOperatorNode(source,
				Operator.GTE, upper, nodeFactory.newIntConstantNode(source, 0));
		ExpressionNode lowerEQZero = nodeFactory.newOperatorNode(source,
				Operator.EQUALS, lower,
				nodeFactory.newIntConstantNode(source, 0));
		ExpressionNode assertion = nodeFactory.newOperatorNode(source,
				Operator.LAND, upperGTZero, lowerEQZero);

		return createAssertion(assertion);
	}

	private void addNondetermChoice(ExpressionNode ptr, ExpressionNode addr,
			boolean addrIsMpiInPlace) {
		Triple<ExpressionNode, Boolean, List<StatementNode>> choices = nondetermChoices
				.getOrDefault(ptr.prettyRepresentation().toString(),
						new Triple<>(ptr, false, new LinkedList<>()));
		StatementNode pointerChoiceStmt = nodeFactory
				.newExpressionStatementNode(nodeFactory.newOperatorNode(
						ptr.getSource(), Operator.ASSIGN, ptr, addr));

		if (addrIsMpiInPlace && choices.second)
			return;
		// pointerChoiceStmt = nodeFactory.newWhenNode(
		// pointerChoiceStmt.getSource(), currentCondition.copy(),
		// pointerChoiceStmt);
		choices.third.add(pointerChoiceStmt);
		if (addrIsMpiInPlace)
			choices.second = true;
		nondetermChoices.put(ptr.prettyRepresentation().toString(), choices);
	}

	private ExpressionNode mpiInPlace(Source source) {
		ExpressionNode mpiInPlace = nodeFactory.newOperatorNode(source,
				Operator.ADDRESSOF,
				nodeFactory.newIdentifierExpressionNode(source, nodeFactory
						.newIdentifierNode(source, MPI_IN_PLACE_SPOT_CONST)));
		return nodeFactory.newCastNode(source, nodeFactory.newPointerTypeNode(
				source, nodeFactory.newVoidTypeNode(source)), mpiInPlace);
	}
}
