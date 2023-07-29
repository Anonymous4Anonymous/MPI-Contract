package edu.udel.cis.vsl.civl.transform.common.contracts.translators;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.udel.cis.vsl.abc.ast.node.IF.IdentifierNode;
import edu.udel.cis.vsl.abc.ast.node.IF.NodeFactory;
import edu.udel.cis.vsl.abc.ast.node.IF.PairNode;
import edu.udel.cis.vsl.abc.ast.node.IF.SequenceNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.CurlyBracedTermSetNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.MPIContractExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.VariableDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode.ExpressionKind;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode.Operator;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.QuantifiedExpressionNode.Quantifier;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.BlockItemNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.StatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.TypeNode;
import edu.udel.cis.vsl.abc.ast.type.IF.Type;
import edu.udel.cis.vsl.abc.ast.type.IF.Type.TypeKind;
import edu.udel.cis.vsl.abc.token.IF.Source;
import edu.udel.cis.vsl.abc.token.IF.SyntaxException;
import edu.udel.cis.vsl.abc.token.IF.TokenFactory;
import edu.udel.cis.vsl.civl.transform.common.BaseWorker;
import edu.udel.cis.vsl.civl.transform.common.contracts.FunctionContractBlock;
import edu.udel.cis.vsl.civl.transform.common.contracts.FunctionContractBlock.ContractBehavior;

/**
 * <p>
 * A frame condition in a contract is in the form of a list of memory locations
 * M. For the target function of verification, we translate frame conditions in
 * the contract in the following way.
 * </p>
 * 
 * <p>
 * Let (G0, M0), ..., (Gi, Mi), ... be the frame condition of the contract that
 * is in the form of pairs: a condition Gi under which the frame condition is
 * significant and a memory location set Mi. The translation layout is: <code>
 * 
 *   $mem m = $mem_empty();
 *   
 *   if (G0)
 *     m = union(m, M0)
 *   ...
 *   if (Gi)
 *     m = union(m, Mi)
 *   ...
 *   
 *   $mem ws = $capture [original function body]
 *   
 *   foreach(n : ws) {
 *     $assert(n in m);
 *   }
 * </code>
 * </p>
 * 
 * <p>
 * Expressions in the form of set comprehension needs some special handling, see
 * {@link #translateAssignsSetComprehension(CurlyBracedTermSetNode, IdentifierNode, ExpressionNode, boolean)}.
 * </p>
 * 
 * The result of this class is a list of {@link ContractClauseTranslation}s. In
 * a returning one, the {@link ContractClauseTranslation#predicate()} is a
 * disjoint clause of the assertion in the layout above.
 * 
 * @author xxxx
 *
 */
public class TargetFrameCondTranslator extends ContractTranslatorCommonBase {

	/**
	 * the map from datatype expressions to names of the temporary variables
	 * that hold size of the datatype. Translation of frame conditions can share
	 * this map with the translation of pre-conditions as both of them are
	 * interpreted at the pre-state.
	 */
	private Map<String, IdentifierNode> datatypeToTempVar;

	public TargetFrameCondTranslator(NodeFactory nodeFactory,
			TokenFactory tokenFactory) {
		super(nodeFactory, tokenFactory);
		this.datatypeToTempVar = new HashMap<>();
	}

	/**
	 * Translate the frame condition for the contractof the target function
	 * 
	 * @param contractBlocks
	 *            the set of contract blocks in the contract of the target
	 *            function
	 * @param ptr2State
	 *            an expression representing a pointer to the pre-state
	 * @param memoryLocationVarIdent
	 *            the identifier of the $mem variable that will hold memory
	 *            locations listed in assigns clauses
	 * @param genericActualMemElementIdent
	 *            the identifier of a generic element in the set of written
	 *            memory locations collected from an execution of the original
	 *            function body
	 * @return
	 * @throws SyntaxException
	 */
	public ContractClauseTranslation translateFrameConditions(
			FunctionContractBlock contractBlock, ExpressionNode state,
			IdentifierNode memoryLocationVarIdent,
			IdentifierNode genericActualMemElementIdent)
			throws SyntaxException {
		List<BlockItemNode> prefix = new LinkedList<>();
		Source source = null;

		// generate a combined source:
		for (ContractBehavior bb : contractBlock.getBehaviorsInBlock())
			for (ExpressionNode arg : bb.getAssignsArgs())
				source = source == null
						? arg.getSource()
						: tokenFactory.join(source, arg.getSource());

		List<ExpressionNode> disjoinClauses = new LinkedList<>();

		for (ContractBehavior bb : contractBlock.getBehaviorsInBlock()) {
			ExpressionNode condition = conjunct(bb.getConditions());

			disjoinClauses.addAll(translateAssigsList(bb.getAssignsArgs(),
					memoryLocationVarIdent.copy(),
					genericActualMemElementIdent.copy(), condition,
					state.copy(), prefix, contractBlock.isSequentialBlock()));
		}

		ExpressionNode frameCondAssertion = null;
		List<ExpressionNode> frameCondAssertions = new LinkedList<>();

		for (ExpressionNode disjoinClause : disjoinClauses)
			frameCondAssertion = frameCondAssertion == null
					? disjoinClause
					: nodeFactory.newOperatorNode(source, Operator.LOR,
							frameCondAssertion, disjoinClause);

		List<BlockItemNode> newPrefix = new LinkedList<>();

		frameCondAssertion = (ExpressionNode) applySubstitutions(
				mpiCommSizeOrRankSubstitutions(frameCondAssertion),
				Arrays.asList(frameCondAssertion)).get(0);
		for (BlockItemNode prefixElt : prefix) {
			prefixElt = (BlockItemNode) applySubstitutions(
					mpiCommSizeOrRankSubstitutions(prefixElt),
					Arrays.asList(prefixElt)).get(0);
			newPrefix.add(prefixElt);
		}
		frameCondAssertions.add(frameCondAssertion);
		return new ContractClauseTranslation(new LinkedList<>(),
				frameCondAssertions, newPrefix, new LinkedList<>());
	}

	/**
	 * <p>
	 * Translate the frame conditon to an assertion, which asserts that the
	 * memory locations listed in all the assigns is the upper bound on the
	 * actual write set of the contracted function in any execution.
	 * </p>
	 * 
	 * @param assignsList
	 *            a list of memory locations listed in assigns clauses
	 * @param memVarIdent
	 *            the name of the variable of $mem type holding frame conditions
	 *            evaluated at the pre-state
	 * @param genericActualMemElementIdent
	 *            the identifier node of an variable holding the generic element
	 *            of the actual modified memory location set
	 * @param condition
	 *            the boolean condition under which the contract is significant
	 * @param state
	 *            the (collective) pre-state
	 * @param prefixes
	 *            a list of block item nodes that shall be placed before the
	 *            call to the original function body
	 * @param isLocal
	 *            true iff the translating post-condition is a part of
	 *            sequential contract
	 * @throws SyntaxException
	 */
	private List<ExpressionNode> translateAssigsList(
			List<ExpressionNode> assignsList, IdentifierNode memVarIdent,
			IdentifierNode genericActualMemElementIdent,
			ExpressionNode condition, ExpressionNode state,
			List<BlockItemNode> prefixes, boolean isLocal)
			throws SyntaxException {
		List<ExpressionNode> setComprehensionTranslation = new LinkedList<>();

		for (ExpressionNode assignsMemLoc : assignsList) {
			setComprehensionTranslation.addAll(translateAssignsMemoryLocation(
					assignsMemLoc, memVarIdent.copy(),
					genericActualMemElementIdent.copy(), condition.copy(),
					state.copy(), prefixes, isLocal));
		}
		return setComprehensionTranslation;
	}

	/**
	 * <p>
	 * The general method for translating an expression listed in an assigns
	 * clause. In general, we translate the expression to a set of memory
	 * locations and save it in a $mem type variable 'm'. The identifier of 'm'
	 * is given by a paramemer. Eventually, 'm' can be used to compare with the
	 * set of memory locations that are actually get modified. In addition,
	 * there is also a $mem variable holding the actual set of memory locations
	 * get modified. The parameter "genericActualMemElementIdent" represents a
	 * generic element of the set.
	 * </p>
	 * 
	 * <p>
	 * There are two sepcial cases: 1) the expression has \mpi_seq type and 2)
	 * the expression is a set comprehension. For case 1, this method calls
	 * {@link #translateAssignsMpiSeq(ExpressionNode, IdentifierNode)}; for case
	 * 2, this method calls
	 * {@link #translateAssignsSetComprehension(CurlyBracedTermSetNode, IdentifierNode, ExpressionNode, boolean)}.
	 * </p>
	 * 
	 * <p>
	 * For any other expression e, we simply translate it to <code>
	 * m = $mem_union(m, ($mem)&e);
	 * </code>
	 * </p>
	 * 
	 * <p>
	 * Output of this method consists of two parts: 1) the output argument
	 * "prefixes" will contain a list of statements that saves translated memory
	 * locations into the $mem type variable identified by the parameter
	 * "memVarIdent"; and 2) the returned expressions, are the translated
	 * formulas from set-comprehensions that shall be asserted when comparing
	 * memory locations listed in assigns clauses with actual ones. See
	 * {@link #translateAssignsSetComprehension} for details of the translation.
	 * </p>
	 * 
	 * @param memoryLoc
	 *            an expression representing a (set of) memory location(s) that
	 *            is listed in an assigns clause
	 * @param memVarIdent
	 *            the name of the variable of $mem type holding frame conditions
	 *            evaluated at the pre-state
	 * @param genericActualMemElementIdent
	 *            the identifier node of an variable holding the generic element
	 *            of the actual modified memory location set
	 * @param condition
	 *            the boolean condition under which the contract is significant
	 * @param state
	 *            the (collective) state
	 * @param prefixes
	 *            a list of block item nodes that shall be placed before the
	 *            call to the original function body
	 * @param isLocal
	 *            true iff the translating post-condition is a part of
	 *            sequential contract
	 * @throws SyntaxException
	 */
	private List<ExpressionNode> translateAssignsMemoryLocation(
			ExpressionNode memoryLoc, IdentifierNode memVarIdent,
			IdentifierNode genericActualMemElementIdent,
			ExpressionNode condition, ExpressionNode state,
			List<BlockItemNode> prefix, boolean isLocal)
			throws SyntaxException {
		List<ExpressionNode> setComprehensionTranslation = new LinkedList<>();
		Type memoryLocType = memoryLoc.getType();

		if (memoryLocType.kind() == TypeKind.ACSL_MPI_TYPE) {
			prefix.addAll(translateAssignsMpiSeq(memoryLoc, condition, state,
					prefix, memVarIdent));
		} else if (memoryLoc
				.expressionKind() == ExpressionKind.CURLY_BRACED_TERM_SET) {
			CurlyBracedTermSetNode curlyBracedSet = (CurlyBracedTermSetNode) memoryLoc;

			if (curlyBracedSet.isExplicit())
				for (ExpressionNode ele : curlyBracedSet.getExplicitElements())
					setComprehensionTranslation
							.addAll(translateAssignsMemoryLocation(ele,
									memVarIdent.copy(),
									genericActualMemElementIdent.copy(),
									condition.copy(), state.copy(), prefix,
									isLocal));
			else {
				// TODO: curly braced set expr can be sub-expressions, e.g.
				// p[{i| 0 <= i < 3}]. It needs normalization
				setComprehensionTranslation
						.add(translateAssignsSetComprehension(curlyBracedSet,
								genericActualMemElementIdent, condition, state,
								prefix, isLocal));
			}
		} else {
			// normal case:
			Source source = memoryLoc.getSource();
			ExpressionNode addrofMemoryLoc = nodeFactory.newOperatorNode(source,
					Operator.ADDRESSOF, memoryLoc.copy());
			StatementNode unionPreMemVar = memUnion(memVarIdent,
					addrofMemoryLoc);

			if (condition != null)
				unionPreMemVar = nodeFactory.newIfNode(source, condition,
						unionPreMemVar);
			prefix.add(unionPreMemVar);
		}
		return setComprehensionTranslation;
	}

	/**
	 * 
	 * <p>
	 * Translates an \mpi_seq type expression that must can be normalized to the
	 * form: <code>
	 *   *\mpi_buf(p, c, d).
	 * </code>
	 *
	 * Let <code>m</code> be the $mem type variable holding memory locations
	 * listed by all assigns. The translation will be <code>
	 *   int tmp = sizeofDatatype(d);
	 *   $assert(tmp % sizeof(T) == 0); // limitation side condition
	 *   m = $mem_union(m, ($mem)p);
	 *   m = $mem_union(m, ($mem)(p + (tmp/sizeof(T)) * (c-1)));
	 * </code>
	 * 
	 * This method returns the above statements. After these statements, the
	 * variable m includes the memory locations listed by the given \mpi_seq
	 * type expression.
	 * </p>
	 * 
	 * @throws SyntaxException
	 */
	private List<BlockItemNode> translateAssignsMpiSeq(ExpressionNode memoryLoc,
			ExpressionNode condition, ExpressionNode state,
			List<BlockItemNode> prefix, IdentifierNode memVarIdent)
			throws SyntaxException {
		assert memoryLoc.expressionKind() == ExpressionKind.OPERATOR;
		List<BlockItemNode> results = new LinkedList<>();
		OperatorNode opNode = (OperatorNode) memoryLoc;
		ExpressionNode mpiBuf = opNode.getArgument(0);
		Type pointedType = extractMpiBufExprPointerType(mpiBuf)
				.referencedType();
		MPIContractExpressionNode normedMpiBuf = normalizeMpiBufExpression(
				mpiBuf);
		ExpressionNode ptr = normedMpiBuf.getArgument(0);
		ExpressionNode count = normedMpiBuf.getArgument(1);
		ExpressionNode datatype = normedMpiBuf.getArgument(2);
		// create temporary variable holding datatype size:
		IdentifierNode tmpVarName = datatypeToTempVar
				.get(datatype.prettyRepresentation().toString());

		if (tmpVarName == null)
			tmpVarName = getNextTempVarNameForDatatype(datatype);
		results.addAll(
				createDatatypeSizeHolderVariable(tmpVarName.name(), datatype));

		// add limitation side condition checking: $assert(tmp % sizeof(T) == 0)
		Source source = memoryLoc.getSource();
		TypeNode pointedTypeNode = BaseWorker.typeNode(ptr.getSource(),
				pointedType, nodeFactory);
		ExpressionNode sizeofType = nodeFactory.newSizeofNode(ptr.getSource(),
				pointedTypeNode);
		ExpressionNode tmpVarAccess = nodeFactory.newIdentifierExpressionNode(
				datatype.getSource(), tmpVarName.copy());
		ExpressionNode sideCond = nodeFactory.newOperatorNode(source,
				Operator.EQUALS, nodeFactory.newOperatorNode(source,
						Operator.MOD, tmpVarAccess, sizeofType),
				nodeFactory.newIntConstantNode(source, 0));
		List<BlockItemNode> resultsUnderCond = new LinkedList<>();

		resultsUnderCond.add(createAssertion(sideCond));
		// union pointers, p & (p + (tmp/sizeof(T)) * (c-1))
		resultsUnderCond.add(memUnion(memVarIdent, ptr.copy()));

		ExpressionNode offset = nodeFactory.newOperatorNode(source,
				Operator.TIMES,
				nodeFactory.newOperatorNode(source, Operator.DIV,
						tmpVarAccess.copy(), sizeofType.copy()),
				nodeFactory.newOperatorNode(count.getSource(), Operator.MINUS,
						count.copy(),
						nodeFactory.newIntConstantNode(count.getSource(), 1)));
		ExpressionNode lastPtr = nodeFactory.newOperatorNode(source,
				Operator.PLUS, ptr.copy(), offset);

		resultsUnderCond.add(memUnion(memVarIdent.copy(), lastPtr));

		if (condition != null)
			results.add(nodeFactory.newIfNode(source, condition, nodeFactory
					.newCompoundStatementNode(source, resultsUnderCond)));
		else
			results.addAll(resultsUnderCond);
		return results;
	}

	/**
	 * <p>
	 * For the memory location(s) expression of the form of set-comprehension
	 * <code>
	 *   {f(x) | T x; pred(f(x))}
	 * </code>, we are not always able to save the concrete memory locations in
	 * a $mem type variable. Thus we need to write the assertion in the
	 * following way. Let m be the $mem variable that holds rest of the memory
	 * locations listed in assigns clauses. Let m' be the $mem variable holding
	 * actual modified memory locations. The assertion shall be <code>
	 *    
	 *    foreach (void * n : m') {
	 *       $assert($mem_contains(m, n) ||
	 *               EXISTS. T x; \old(pred(f(x))) && \old(&f(x)) == n);
	 *    }
	 * </code>
	 * </p>
	 * <p>
	 * This method returns the assertion
	 * <code> EXISTS. T x; \old(pred(f(x))) && \old(&f(x)) == n</code> for the
	 * given identifier 'n'.
	 * </p>
	 * 
	 * @param curlyBracedSet
	 *            the set-comprehension expression representing a set of memory
	 *            locations
	 * @param genericActualMemElementIdent
	 *            the identifier node of an variable holding the generic element
	 *            of the actual modified memory location set
	 * @param condition
	 *            the boolean condition under which the contract is significant
	 * @param ptrToState
	 *            the expression representing a pointer to the pre-state
	 * @param isLocal
	 *            true iff this contract belongs to a sequential contract;
	 *            otherwise, this contract belongs to a collective contract
	 * @throws SyntaxException
	 */
	private ExpressionNode translateAssignsSetComprehension(
			CurlyBracedTermSetNode curlyBracedSet,
			IdentifierNode genericActualMemElementIdent,
			ExpressionNode condition, ExpressionNode state,
			List<BlockItemNode> prefix, boolean isLocal)
			throws SyntaxException {
		// process set comprehension:
		SequenceNode<ExpressionNode> terms = curlyBracedSet
				.getSetComprehensionTerms();
		ExpressionNode term, predicate = curlyBracedSet.getPredicate();
		SequenceNode<VariableDeclarationNode> binders = curlyBracedSet
				.getBinders();
		Source source = curlyBracedSet.getSource();

		term = terms.getSequenceChild(0);

		ExpressionNode trueExpr = nodeFactory.newBooleanConstantNode(source,
				true);
		SequenceNode<PairNode<SequenceNode<VariableDeclarationNode>, ExpressionNode>> boundVars;
		List<PairNode<SequenceNode<VariableDeclarationNode>, ExpressionNode>> boundVarComponents = new LinkedList<>();

		// create \old(pred(f(x))):
		predicate = mkValueAtExpressionWithDefaultProc(predicate.copy(), state,
				isLocal);

		// create \old(&(fx)):
		ExpressionNode addrofTerm = nodeFactory.newOperatorNode(
				term.getSource(), Operator.ADDRESSOF, term.copy());
		ExpressionNode genericActualMemElementIdentAccess = nodeFactory
				.newIdentifierExpressionNode(source,
						genericActualMemElementIdent);
		ExpressionNode ptrEquals = nodeFactory.newOperatorNode(source,
				Operator.EQUALS, addrofTerm,
				genericActualMemElementIdentAccess);

		addrofTerm = mkValueAtExpressionWithDefaultProc(addrofTerm,
				state.copy(), isLocal);
		// create EXIST. T x; \old(pred(f(x))) && \old(&f(x)) == n:
		boundVarComponents
				.add(nodeFactory.newPairNode(source, binders.copy(), trueExpr));
		boundVars = nodeFactory.newSequenceNode(source,
				"set-comprehension-bound-vars", boundVarComponents);

		ExpressionNode result = nodeFactory.newQuantifiedExpressionNode(source,
				Quantifier.EXISTS, boundVars, predicate, ptrEquals, null);

		if (condition != null)
			result = nodeFactory.newOperatorNode(source, Operator.LAND,
					condition, result);
		return result;
	}

	/* ******** helper method ******** */

	/**
	 * <code>
	 *   preMemVarIdent = $mem_union(preMemVarIdent, ($mem)addrofMemLoc);
	 * </code>
	 * 
	 * @param preMemVarIdent
	 * @param addrofMemLoc
	 */
	private StatementNode memUnion(IdentifierNode preMemVarIdent,
			ExpressionNode addrofMemLoc) {
		Source source = addrofMemLoc.getSource();
		ExpressionNode preMemVarAccess = nodeFactory
				.newIdentifierExpressionNode(source, preMemVarIdent);
		TypeNode memType = nodeFactory.newMemTypeNode(source);
		ExpressionNode castedAddrofMemLoc = nodeFactory.newCastNode(source,
				memType, addrofMemLoc);
		ExpressionNode memUnionFun = nodeFactory.newIdentifierExpressionNode(
				source, nodeFactory.newIdentifierNode(source,
						MPI_CONTRACT_CONSTS.MEM_UNION_FUN));
		List<ExpressionNode> args = new LinkedList<>();
		ExpressionNode memUnionCall;

		args.add(preMemVarAccess);
		args.add(castedAddrofMemLoc);
		memUnionCall = nodeFactory.newFunctionCallNode(source, memUnionFun,
				args, null);
		memUnionCall = nodeFactory.newOperatorNode(source, Operator.ASSIGN,
				preMemVarAccess.copy(), memUnionCall);
		return nodeFactory.newExpressionStatementNode(memUnionCall);
	}

	/**
	 * <code>$mem_contains(superSet, subSet)</code>
	 */
	private ExpressionNode memContainsCall(ExpressionNode superSet,
			ExpressionNode subSet) {
		Source source = subSet.getSource();
		ExpressionNode memContainsFun = nodeFactory.newIdentifierExpressionNode(
				source, nodeFactory.newIdentifierNode(source, "$mem_contains"));
		List<ExpressionNode> args = new LinkedList<>();

		args.add(superSet);
		args.add(subSet);

		return nodeFactory.newFunctionCallNode(source, memContainsFun, args,
				null);
	}

	/**
	 * @param datatype
	 * @return a new indetifier node of a new unique temporary variable name for
	 *         holding datatype sizes
	 */
	private IdentifierNode getNextTempVarNameForDatatype(
			ExpressionNode datatype) {
		String tmpVarName = MPI_CONTRACT_CONSTS.MPI_DATATYPE_TEMP_VAR_NAME
				+ "_assigns_" + +datatypeToTempVar.size();
		IdentifierNode tmpVarIdent = nodeFactory
				.newIdentifierNode(datatype.getSource(), tmpVarName);

		datatypeToTempVar.put(datatype.prettyRepresentation().toString(),
				tmpVarIdent);
		return tmpVarIdent;
	}

	/**
	 * 
	 * @param source
	 * @return a function call <code>$mem_empty()</code>
	 */
	private ExpressionNode mkMemEmptyCall(Source source) {
		ExpressionNode memEmptyFun = nodeFactory.newIdentifierExpressionNode(
				source, nodeFactory.newIdentifierNode(source,
						MPI_CONTRACT_CONSTS.MEM_EMPTY_FUN));

		return nodeFactory.newFunctionCallNode(source, memEmptyFun,
				new LinkedList<>(), null);
	}

	/**
	 * 
	 * @param source
	 * @return <code>$write_set_push()</code>
	 */
	private StatementNode mkWriteSetPush(Source source) {
		ExpressionNode writeSetPushFun = nodeFactory
				.newIdentifierExpressionNode(source,
						nodeFactory.newIdentifierNode(source,
								MPI_CONTRACT_CONSTS.WRITE_SET_PUSH_FUN));

		return nodeFactory.newExpressionStatementNode(
				nodeFactory.newFunctionCallNode(source, writeSetPushFun,
						new LinkedList<>(), null));
	}

	/**
	 * 
	 * @param source
	 * @param returnValHolder
	 *            an expression that can appear at the left hand side of an
	 *            assignment
	 * @return <code>returnValHolder = $write_set_pop()</code>
	 */
	private StatementNode mkWriteSetPush(Source source,
			ExpressionNode returnValHolder) {
		ExpressionNode writeSetPopFun = nodeFactory.newIdentifierExpressionNode(
				source, nodeFactory.newIdentifierNode(source,
						MPI_CONTRACT_CONSTS.WRITE_SET_POP_FUN));
		ExpressionNode assign = nodeFactory.newOperatorNode(source,
				Operator.ASSIGN, returnValHolder, writeSetPopFun);

		return nodeFactory.newExpressionStatementNode(assign);
	}
}
