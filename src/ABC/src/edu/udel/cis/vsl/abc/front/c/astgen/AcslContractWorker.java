package edu.udel.cis.vsl.abc.front.c.astgen;

import static edu.udel.cis.vsl.abc.front.IF.CivlcTokenConstant.EXPR;
import static edu.udel.cis.vsl.abc.front.IF.CivlcTokenConstant.TYPE;
import static edu.udel.cis.vsl.abc.front.c.parse.AcslParser.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonErrorNode;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;

import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.IdentifierNode;
import edu.udel.cis.vsl.abc.ast.node.IF.NodeFactory;
import edu.udel.cis.vsl.abc.ast.node.IF.PairNode;
import edu.udel.cis.vsl.abc.ast.node.IF.SequenceNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.AllocationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.AssignsOrReadsNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.AssumesNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.BehaviorNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.CompletenessNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.CompositeEventNode.EventOperator;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.ContractNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.DependsEventNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.DependsNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.EnsuresNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.ExtendedQuantifiedExpressionNode.ExtendedQuantifier;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.GuardsNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.MPICollectiveBlockNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.MPIContractExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.MPIContractExpressionNode.MPIContractExpressionKind;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.MemoryEventNode.MemoryEventNodeKind;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.RequiresNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.WaitsforNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.FunctionDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.VariableDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.CharacterConstantNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ConstantNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.EnumerationConstantNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.FloatingConstantNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.FunctionCallNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.IntegerConstantNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode.Operator;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.QuantifiedExpressionNode.Quantifier;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.SizeableNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.SizeofNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.StringLiteralNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.BlockItemNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.CompoundStatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.FunctionTypeNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.TypeNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.TypeNode.TypeNodeKind;
import edu.udel.cis.vsl.abc.ast.type.IF.StandardBasicType.BasicTypeKind;
import edu.udel.cis.vsl.abc.config.IF.Configuration;
import edu.udel.cis.vsl.abc.err.IF.ABCException;
import edu.udel.cis.vsl.abc.err.IF.ABCRuntimeException;
import edu.udel.cis.vsl.abc.front.IF.ParseException;
import edu.udel.cis.vsl.abc.front.c.parse.AcslParser;
import edu.udel.cis.vsl.abc.front.c.ptree.CParseTree;
import edu.udel.cis.vsl.abc.front.common.astgen.SimpleScope;
import edu.udel.cis.vsl.abc.token.IF.CharacterToken;
import edu.udel.cis.vsl.abc.token.IF.CivlcToken;
import edu.udel.cis.vsl.abc.token.IF.CivlcToken.TokenVocabulary;
import edu.udel.cis.vsl.abc.token.IF.Formation;
import edu.udel.cis.vsl.abc.token.IF.Source;
import edu.udel.cis.vsl.abc.token.IF.StringToken;
import edu.udel.cis.vsl.abc.token.IF.TokenFactory;

/**
 * This is responsible for translating a CParseTree that represents an ACSL
 * contract specification for a function or a loop into an ordered list of
 * Contract nodes. It serves as a helper for {@link AcslContractHandler}. <br>
 * Precondition: all tokens are preprocessed with the regular program
 * components. <br>
 * Note: there are no separated lexer for the ACSL parser. All keywords are
 * recognized as IDENTIFIER or EXTENDED_IDENTIFIER and semantic predicates are
 * used to match different keywords, like <code>requires</code>,
 * <code>ensures</code>, <code>assumes</code> as IDENTIFIER and
 * <code>\valid</code>, <code>\result</code>, <code>\valid</code> as
 * EXTENDED_IDENTIFIER.
 *
 * @author Manchun Zheng (zxxxx)
 * @author Ziqing Luo
 */
public class AcslContractWorker {

	/**
	 * A collection of translated nodes. The collection is the result of any
	 * ACSL contract translation. The collection includes two groups:
	 * <li>a set of {@link ContractNode}s</li>
	 * <li>a set of {@link BlockItemNode}s that can be directly put at the
	 * position of the translated ACSL contract.</li>
	 *
	 * @author xxxx
	 */
	public class ACSLSpecTranslation {
		/**
		 * A set of {@link ContractNode}s which are the translation results of
		 * the ACSL contract annotations.
		 */
		final SequenceNode<ContractNode> contractNodes;
		/**
		 * A set of {@link BlockItemNode}s which are the translation results of
		 * some ACSL contract annotations that can be directly mapped to
		 * existing ABC nodes.
		 */
		final List<BlockItemNode> blockItemNodes;

		ACSLSpecTranslation(Source acslSpecSource, List<ContractNode> contractNodes, List<BlockItemNode> blockItemNodes) {
			assert contractNodes != null;
			assert blockItemNodes != null;
			this.contractNodes = nodeFactory.newSequenceNode(acslSpecSource,
					"ACSL spec", contractNodes);
			this.blockItemNodes = blockItemNodes;
		}
	}

	/**
	 * the parse tree to be translated
	 */
	private CParseTree parseTree;

	/**
	 * the node factory to be used for creating AST nodes
	 */
	private NodeFactory nodeFactory;

	/**
	 * the token factory
	 */
	private TokenFactory tokenFactory;

	/**
	 * the formation to be used for transforming ANTLR tokens into CTokens
	 */
	private Formation formation;

	/**
	 * the configuration of this translation task
	 */
	private Configuration config;

	/* ******************** Constants ******************* */
	private final String MPI_COMM_RANK_NAME = "\\mpi_comm_rank";
	private final String MPI_COMM_SIZE_NAME = "\\mpi_comm_size";
	private final String MPI_BACK_MESSAGES_NAME = "\\mpi_back_messages";
	private final String CIVL_ASSERT = "$assert";

	/**
	 * creates a new instance of AcslContractWorker
	 *
	 * @param factory
	 *            the node factory to be used
	 * @param tokenFactory
	 *            the token factory to be used
	 * @param parseTree
	 *            the parse tree to be translated
	 */
	public AcslContractWorker(NodeFactory factory, TokenFactory tokenFactory, CParseTree parseTree, Configuration config) {
		this.nodeFactory = factory;
		this.tokenFactory = tokenFactory;
		this.parseTree = parseTree;
		this.config = config;
		formation = tokenFactory.newTransformFormation("ACSL", "contract");
	}

	/**
	 * translates the parse tree to a list of contract nodes. Currently, two
	 * kinds of contracts are supported, one is function contract, the other is
	 * loop annotation.
	 *
	 * @param scope
	 *            the scope of the contract
	 * @return the list of contract nodes which is the result of translating the
	 *         parse tree
	 * @throws ABCException
	 *             if there are syntax errors during the translation
	 * @throws ParseException
	 *             if parsing error happens
	 */
	public ACSLSpecTranslation generateContractNodes(SimpleScope scope)
			throws ABCException, ParseException {
		CommonTree contractTree = parseTree.getRoot();
		List<ContractNode> translatedContractNodes = new LinkedList<>();
		List<BlockItemNode> translatedBlockItems = new LinkedList<>();

		switch (contractTree.getType()) {
		case FUNC_CONTRACT:
			translatedContractNodes
					.addAll(translateFunctionContract(contractTree, scope));
			break;
		case LOOP_CONTRACT_BLOCK:
			translatedContractNodes
					.addAll(translateLoopContractBlock(contractTree, scope));
			break;
		case LOGIC_FUNCTIONS:
			translatedBlockItems
					.addAll(translateLogicFunctions(contractTree, scope));
			break;
		case ASSERT_ACSL:
			translatedBlockItems
					.add(translateACSLAssertion(contractTree, scope));
			break;
		case 0: // CommonErrorNode.getType() == 0 ...
			throw error("Syntax error: "
					+ ((CommonErrorNode) contractTree).getText(), null);
		default:
			throw new ABCRuntimeException(
					"unexpected error in translating ACSL-MPI contracts.");
		}
		return new ACSLSpecTranslation(parseTree.source(contractTree),
				translatedContractNodes, translatedBlockItems);
	}

	/**
	 * translates the contract for a loop.
	 *
	 * @param tree
	 *            the tree to be translated, which represented the contracts of
	 *            the loop
	 * @param scope
	 *            the current scope
	 * @return the list of contracts associated with a loop
	 * @throws ABCException
	 *             if there are syntax errors
	 */
	private List<ContractNode> translateLoopContractBlock(CommonTree tree,
			SimpleScope scope) throws ABCException {
		int numChildren = tree.getChildCount();
		List<ContractNode> result = new ArrayList<>();

		assert tree.getType() == LOOP_CONTRACT_BLOCK;
		for (int i = 0; i < numChildren; i++) {
			CommonTree loopIterm = (CommonTree) tree.getChild(i);
			int loopItemKind = loopIterm.getType();

			switch (loopItemKind) {
			case AcslParser.LOOP_CLAUSE:
				result.add(translateLoopClause(
						(CommonTree) loopIterm.getChild(0), scope));
				break;
			case AcslParser.LOOP_BEHAVIOR:
				throw error("unsupported kind of loop contract", loopIterm);
			default:
				throw error("unknown kind of loop contract", loopIterm);
			}
		}
		return result;
	}

	/**
	 * translate the LOGIC_FUNCTIONS list, which is a list of mixed items, each
	 * of which is either a LOGIC_FUNCTION or a PREDICATE_FUNCTION:
	 */
	private List<BlockItemNode> translateLogicFunctions(CommonTree logicFuncs,
			SimpleScope scope) throws ABCException {
		int numChildren = logicFuncs.getChildCount();
		List<BlockItemNode> results = new LinkedList<>();

		for (int i = 0; i < numChildren; i++) {
			CommonTree logicFun = (CommonTree) logicFuncs.getChild(i);
			int type = logicFun.getType();

			if (type == LOGIC_FUNCTION_CLAUSE)
				results.add(translatePureLogicFunction(logicFun, scope));
			else {
				assert type == PREDICATE_CLAUSE;
				results.add(translatePredicateFunction(logicFun, scope));
			}
		}
		return results;
	}

	/**
	 * <code>
	 * PREDICATE_KEY identifier ( binders )  function_body SEMI;
	 * </code>, where <code>
	 * function_body := ASSIGN term
	 * </code>
	 */
	private BlockItemNode translatePredicateFunction(CommonTree logicFunc,
			SimpleScope scope) throws ABCException {
		SimpleScope bindersScope = new SimpleScope(scope);
		SimpleScope bodyScope = new SimpleScope(bindersScope);
		CommonTree identTree = (CommonTree) logicFunc.getChild(0);
		CommonTree bindersTree = (CommonTree) logicFunc.getChild(1);
		CommonTree bodyTree = (CommonTree) logicFunc.getChild(2);
		// translate trees to AST nodes:
		IdentifierNode logicFuncIdent = translateIdentifier(identTree);
		SequenceNode<VariableDeclarationNode> binders = translateBinders(
				bindersTree, new SimpleScope(scope));
		ExpressionNode body = translateExpression(
				(CommonTree) bodyTree.getChild(0), bodyScope);
		TypeNode retType = nodeFactory.newBasicTypeNode(newSource(logicFunc),
				BasicTypeKind.BOOL);
		FunctionDeclarationNode result = logicFuncToFuncDefnNode(retType,
				logicFuncIdent, binders, body, bodyScope);

		result.setIsLogicFunction(true);
		return result;
	}

	/**
	 * <code>
	 * LOGIC_KEY type identifier ( binders )  function_body SEMI;
	 * </code>, where <code>
	 * function_body := ASSIGN term
	 * </code>
	 */
	private BlockItemNode translatePureLogicFunction(CommonTree logicFunc,
			SimpleScope scope) throws ABCException {
		SimpleScope bindersScope = new SimpleScope(scope);
		SimpleScope bodyScope = new SimpleScope(bindersScope);
		CommonTree typeTree = (CommonTree) logicFunc.getChild(0);
		CommonTree identTree = (CommonTree) logicFunc.getChild(1);
		CommonTree bindersTree = (CommonTree) logicFunc.getChild(2);
		CommonTree bodyTree = (CommonTree) logicFunc.getChild(3);
		// translate trees to AST nodes:
		TypeNode retType = translateTypeExpr(typeTree, scope);
		IdentifierNode logicFuncIdent = translateIdentifier(identTree);
		SequenceNode<VariableDeclarationNode> binders = translateBinders(
				bindersTree, new SimpleScope(scope));
		ExpressionNode body = null;

		if (bodyTree != null)
			body = translateExpression((CommonTree) bodyTree.getChild(0),
					bodyScope);

		FunctionDeclarationNode result = logicFuncToFuncDefnNode(retType,
				logicFuncIdent, binders, body, bodyScope);

		result.setIsLogicFunction(true);
		return result;
	}

	/**
	 * Translate ACSL assert to CIVL $assert.
	 */
	private BlockItemNode translateACSLAssertion(CommonTree assertTree,
			SimpleScope scope) throws ABCException {
		CommonTree predicate = (CommonTree) assertTree.getChild(0);
		Source source = parseTree.source(assertTree);
		ExpressionNode assertCall = nodeFactory.newFunctionCallNode(source,
				nodeFactory.newIdentifierExpressionNode(source,
						nodeFactory.newIdentifierNode(source, CIVL_ASSERT)),
				Arrays.asList(translateExpression(predicate, scope)), null);

		return nodeFactory.newExpressionStatementNode(assertCall);
	}

	/**
	 * creates a {@link FunctionDeclarationNode} for a logic function
	 */
	private FunctionDeclarationNode logicFuncToFuncDefnNode(TypeNode retType,
			IdentifierNode funcIdent,
			SequenceNode<VariableDeclarationNode> binders,
			ExpressionNode definition, SimpleScope scope) throws ABCException {
		Source funcTypeSource = tokenFactory.join(retType.getSource(),
				binders.getSource());
		FunctionTypeNode funcTypeNode = nodeFactory
				.newFunctionTypeNode(funcTypeSource, retType, binders, true);

		if (definition != null) {
			Source defiSource = definition.getSource();
			CompoundStatementNode body = nodeFactory
					.newCompoundStatementNode(defiSource, Arrays.asList(
							nodeFactory.newReturnNode(defiSource, definition)));

			return nodeFactory.newFunctionDefinitionNode(defiSource, funcIdent,
					funcTypeNode, null, body);
		} else
			return nodeFactory.newAbstractFunctionDefinitionNode(
					funcIdent.getSource(), funcIdent, funcTypeNode, null, 0,
					null);
	}

	/**
	 * translates a loop clause, which could be a loop invariant, an assigns
	 * clause, an allocate clause, or a free clause. Currently, only loop
	 * invariant is supported.
	 *
	 * @param tree
	 *            the tree represented a loop contract clause
	 * @param scope
	 *            the current scope
	 * @return the contract node represented a loop clause
	 * @throws ABCException
	 *             if there are some syntax errors
	 */
	private ContractNode translateLoopClause(CommonTree tree, SimpleScope scope)
			throws ABCException {
		int loopClauseKind = tree.getType();
		Source source = newSource(tree);
		CommonTree clauseArgTree = (CommonTree) tree.getChild(0);

		switch (loopClauseKind) {
		case AcslParser.LOOP_INVARIANT: {
			ExpressionNode expression = translateExpression(clauseArgTree,
					scope);

			return nodeFactory.newInvariantNode(source, true, expression);
		}
		case AcslParser.LOOP_ASSIGNS:
			return translateReadsOrAssigns(tree, scope, false);
		case AcslParser.LOOP_ALLOC:
		case AcslParser.LOOP_FREE:
		case AcslParser.LOOP_VARIANT:
			throw this.error("unsupported kind of loop contract clause", tree);
		case 0: // ANTLR error
			throw error("Syntax error: " + ((CommonErrorNode) tree).getText(),
					tree.parent);
		default:
			throw this.error("unknown kind of loop contract clause", tree);
		}
	}

	/**
	 * Input tree: <code>
	 *  function_contract_block 
	 *  mpi_contract* 
	 *  completeness_clause_block*
	 * </code>
	 * 
	 * @throws ABCException
	 */
	List<ContractNode> translateFunctionContract(CommonTree funcContract,
			SimpleScope scope) throws ABCException {
		List<ContractNode> results = new LinkedList<>();
		int numChildren = funcContract.getChildCount();

		for (int i = 0; i < numChildren; i++) {
			CommonTree child = (CommonTree) funcContract.getChild(i);

			switch (child.getType()) {
			case AcslParser.FUNC_CONTRACT_BLOCK:
				results.addAll(translateFunctionContractBlock(child, scope));
				break;
			case AcslParser.MPI_CONTRACT:
				results.add(translateMPIContract(child, scope));
				break;
			case 0:
				throw error(
						"Syntax error: " + ((CommonErrorNode) child).getText(),
						null);
			default:
				throw error("Unknown contract: " + child, child);
			}
		}
		return results;
	}

	/**
	 * Input tree <code>function_clause* 
	 * named_behavior_block* 
	 * completeness_clause_block*</code>
	 */
	private List<ContractNode> translateFunctionContractBlock(CommonTree tree,
			SimpleScope scope) throws ABCException {
		int numChildren = tree.getChildCount();
		List<ContractNode> result = new ArrayList<>();
		boolean processClause = true;

		assert tree.getType() == AcslParser.FUNC_CONTRACT_BLOCK;
		for (int i = 0; i < numChildren; i++) {
			CommonTree child = (CommonTree) tree.getChild(i);
			int childKind = child.getType();

			switch (childKind) {
			case AcslParser.BEHAVIOR:
				result.add(translateBehavior(child, scope));
				break;
			case AcslParser.CLAUSE_COMPLETE:
				result.add(translateCompleteness(child, scope));
				break;
			case 0: // ANTLR error
				throw error(
						"Syntax error: " + ((CommonErrorNode) child).getText(),
						null);
			default:
				// Process clauses:
				if (processClause)
					result.add(translateContractClause(child, scope));
				else
					throw this.error("Unknown contract kind", child);
			}
		}
		return result;
	}

	/**
	 * Input tree: <code>comm_clause | mpi_collective_block</code>
	 * 
	 * @throws ABCException
	 */
	private ContractNode translateMPIContract(CommonTree mpiContractTree,
			SimpleScope scope) throws ABCException {
		int numChildren = mpiContractTree.getChildCount();

		assert numChildren == 1;
		CommonTree child = (CommonTree) mpiContractTree.getChild(0);

		switch (child.getType()) {
		case AcslParser.COMM_CLAUSE:
			return nodeFactory.newMPIUsesNode(newSource(child),
					translateCommaSeparatedList((CommonTree) child.getChild(0),
							scope));
		case AcslParser.MPI_COLLECTIVE_BLOCK:
			return translateMPICollectiveBlock(child, scope);
		case 0: // ANTLR error
			throw error("Syntax error: " + ((CommonErrorNode) child).getText(),
					null);
		default:
			throw this.error("Unknown contract kind", child);
		}
	}

	/**
	 * translates the ACSL completeness clause for behavior, which could be
	 * COMPLETE or DISJOINT.
	 *
	 * @param tree
	 *            the CommonTree representing the completeness clause. It has a
	 *            child which is either BEHAVIOR_COMPLETE or BEHAVIOR_DISJOINT.
	 *            Either one has two children: the first child is a source
	 *            provider, simply ignore it; the second child is optional ID
	 *            list (see {@link #translateIdList(CommonTree, SimpleScope)}
	 * @param scope
	 *            the current scope
	 * @return the completeness node which is the result of translating the
	 *         given tree
	 * @throws ABCException
	 *             if there are some syntax errors
	 */
	private CompletenessNode translateCompleteness(CommonTree tree,
			SimpleScope scope) throws ABCException {
		CommonTree completenessTree = (CommonTree) tree.getChild(0);
		int kind = completenessTree.getType();
		boolean isComplete = false;
		SequenceNode<IdentifierNode> idList = null;

		if (completenessTree.getChildCount() > 1)
			idList = translateIdList((CommonTree) completenessTree.getChild(1),
					scope);
		switch (kind) {
		case AcslParser.BEHAVIOR_COMPLETE:
			isComplete = true;
			break;
		case AcslParser.BEHAVIOR_DISJOINT:
			break;
		default:
			throw this.error("Unknown kind of completeness clause", tree);
		}
		return this.nodeFactory.newCompletenessNode(newSource(tree), isComplete,
				idList);
	}

	/**
	 * translates a list of identifiers
	 *
	 * @param tree
	 *            a tree that represents a list of identifiers
	 * @param scope
	 *            the current scope
	 * @return a sequence of identifier node
	 */
	private SequenceNode<IdentifierNode> translateIdList(CommonTree tree,
			SimpleScope scope) {
		int numChildren = tree.getChildCount();
		List<IdentifierNode> list = new ArrayList<>();
		Source source = this.parseTree.source(tree);

		for (int i = 0; i < numChildren; i++) {
			CommonTree idTree = (CommonTree) tree.getChild(i);

			list.add(translateIdentifier(idTree));
		}
		return this.nodeFactory.newSequenceNode(source, "ID list", list);
	}

	/**
	 * translates an ACSL behavior block
	 *
	 * @param tree
	 *            the tree that represents a behavior block
	 * @param scope
	 *            the current scope
	 * @return the behavior node which is the result of translating the given
	 *         behavior block
	 * @throws ABCException
	 *             if there are any syntax errors.
	 */
	private BehaviorNode translateBehavior(CommonTree tree, SimpleScope scope)
			throws ABCException {
		CommonTree idTree = (CommonTree) tree.getChild(1);
		CommonTree bodyTree = (CommonTree) tree.getChild(2);
		IdentifierNode id = this.translateIdentifier(idTree);
		SequenceNode<ContractNode> body = this.translateBehaviorBody(bodyTree,
				scope);

		return this.nodeFactory.newBehaviorNode(this.parseTree.source(tree), id,
				body);
	}

	/**
	 * translates the body of a behavior block.
	 *
	 * @param tree
	 *            the tree that represents the body of a behavior block
	 * @param scope
	 *            the current scope
	 * @return a sequence of contract nodes which is the result of the
	 *         translation
	 * @throws ABCException
	 *             if there are any syntax errors
	 */
	private SequenceNode<ContractNode> translateBehaviorBody(CommonTree tree,
			SimpleScope scope) throws ABCException {
		Source source = this.parseTree.source(tree);
		int numChildren = tree.getChildCount();
		List<ContractNode> clauses = new ArrayList<>();

		for (int i = 0; i < numChildren; i++) {
			CommonTree clause = (CommonTree) tree.getChild(i);

			clauses.add(this.translateContractClause(clause, scope));
		}
		return this.nodeFactory.newSequenceNode(source, "behavior body",
				clauses);
	}

	/**
	 * The input tree must represent a clause
	 */
	private ContractNode translateContractClause(CommonTree tree,
			SimpleScope scope) throws ABCException {
		switch (tree.getType()) {
		case AcslParser.ALLOC:
			return this.translateAllocation(tree, scope, true);
		case AcslParser.FREES:
			return this.translateAllocation(tree, scope, false);
		case AcslParser.REQUIRES_ACSL:
			return this.translateRequires(tree, scope);
		case AcslParser.ENSURES_ACSL:
			return this.translateEnsures(tree, scope);
		case AcslParser.ASSIGNS_ACSL:
			return this.translateReadsOrAssigns(tree, scope, false);
		case AcslParser.ASSUMES_ACSL:
			return this.translateAssumes(tree, scope);
		case AcslParser.READS_ACSL:
			return this.translateReadsOrAssigns(tree, scope, true);
		case AcslParser.DEPENDSON:
			return this.translateDepends(tree, scope);
		case AcslParser.EXECUTES_WHEN:
			return this.translateGuards(tree, scope);
		case AcslParser.WAITSFOR:
			return this.translateWaitsfor(tree, scope);
		default:
			throw this.error("Unknown contract clause kind", tree);
		}
	}

	private AllocationNode translateAllocation(CommonTree tree,
			SimpleScope scope, boolean isAllocates) throws ABCException {
		SequenceNode<ExpressionNode> memoryList = translateCommaSeparatedList(
				(CommonTree) tree.getChild(0), scope);

		return this.nodeFactory.newAllocationNode(newSource(tree), isAllocates,
				memoryList);
	}

	/**
	 * translates a guard clause, which has the syntax
	 * <code>executes_when expr</code>.
	 *
	 * @param tree
	 *            the tree that represents the guard clause
	 * @param scope
	 *            the current scope
	 * @return the guard node that is the result of the translation
	 * @throws ABCException
	 *             if there are some syntax errors.
	 */
	private GuardsNode translateGuards(CommonTree tree, SimpleScope scope)
			throws ABCException {
		CommonTree expressionTree = (CommonTree) tree.getChild(1);

		return this.nodeFactory.newGuardNode(this.newSource(tree),
				this.translateExpression(expressionTree, scope));
	}

	private WaitsforNode translateWaitsfor(CommonTree tree, SimpleScope scope)
			throws ABCException {
		int numChildren = tree.getChildCount();
		SequenceNode<ExpressionNode> arguments;
		List<ExpressionNode> childrenList = new LinkedList<>();
		Source source = newSource(tree);

		for (int i = 0; i < numChildren; i++)
			childrenList.add(
					translateExpression((CommonTree) tree.getChild(i), scope));
		arguments = nodeFactory.newSequenceNode(source, "argument list",
				childrenList);
		return nodeFactory.newWaitsforNode(source, arguments);
	}

	/**
	 * translates an assume clause, which has the syntax
	 * <code>assumes expr</code>.
	 *
	 * @param tree
	 *            the tree that represents an assumes clause
	 * @param scope
	 *            the current scope
	 * @return the Assumes node
	 * @throws ABCException
	 *             if there are any syntax errors.
	 */
	private AssumesNode translateAssumes(CommonTree tree, SimpleScope scope)
			throws ABCException {
		CommonTree exprTree = (CommonTree) tree.getChild(1);
		ExpressionNode predicate = this.translateExpression(exprTree, scope);
		Source source = this.parseTree.source(tree);

		return this.nodeFactory.newAssumesNode(source, predicate);
	}

	private AssignsOrReadsNode translateReadsOrAssigns(CommonTree tree,
			SimpleScope scope, boolean isRead) throws ABCException {
		Source source = this.parseTree.source(tree);
		SequenceNode<ExpressionNode> memoryList = translateCommaSeparatedList(
				(CommonTree) tree.getChild(0), scope);

		if (isRead)
			return this.nodeFactory.newReadsNode(source, memoryList);
		else
			return this.nodeFactory.newAssignsNode(source, memoryList);
	}

	private DependsNode translateDepends(CommonTree tree, SimpleScope scope)
			throws ABCException {
		Source source = this.parseTree.source(tree);
		CommonTree argumentTree = (CommonTree) tree.getChild(1);
		SequenceNode<DependsEventNode> argumentList = this
				.translateDependsEventList(argumentTree, scope);

		return this.nodeFactory.newDependsNode(source, null, argumentList);
	}

	private SequenceNode<DependsEventNode> translateDependsEventList(
			CommonTree tree, SimpleScope scope) throws ABCException {
		int numChildren = tree.getChildCount();
		List<DependsEventNode> events = new ArrayList<>();
		Source source = this.parseTree.source(tree);

		for (int i = 0; i < numChildren; i++) {
			CommonTree event = (CommonTree) tree.getChild(i);

			events.add(this.translateDependsEvent(event, scope));
		}
		return this.nodeFactory.newSequenceNode(source, "depends event list",
				events);
	}

	/**
	 * <code>
	 *  event := event_base (event_suffix)?
	 * </code> see also
	 * {@link #translateDependsEventSuffix(DependsEventNode, CommonTree, SimpleScope)}
	 */
	private DependsEventNode translateDependsEvent(CommonTree tree,
			SimpleScope scope) throws ABCException {
		CommonTree eventBaseTree = (CommonTree) tree.getChild(0);
		CommonTree eventSuffixTree = (CommonTree) tree.getChild(1);
		DependsEventNode eventBase = translateDependsEventBase(eventBaseTree,
				scope);

		if (eventSuffixTree != null)
			return translateDependsEventSuffix(eventBase, eventSuffixTree,
					scope);
		else
			return eventBase;
	}

	/**
	 * <code>
	 * event_suffix := ABSENT
	 *  | PLUS       event_base
	 *  | SUB        event_base
	 *  | AMPERSAND  event_base
	 * </code>
	 */
	private DependsEventNode translateDependsEventSuffix(
			DependsEventNode eventBase, CommonTree suffixTree,
			SimpleScope scope) throws ABCException {
		int kind = suffixTree.getType();
		CommonTree rhsTree;
		EventOperator operator;

		switch (kind) {
		case EVENT_PLUS:
			operator = EventOperator.UNION;
			break;
		case EVENT_SUB:
			operator = EventOperator.DIFFERENCE;
			break;
		case EVENT_INTS:
			operator = EventOperator.INTERSECT;
			break;
		case ABSENT:
			return eventBase;
		default:
			throw this.error("unknown kind of operator for depends events",
					suffixTree);
		}
		rhsTree = (CommonTree) suffixTree.getChild(0);

		DependsEventNode rhs = this.translateDependsEventBase(rhsTree, scope);
		Source operationSource = tokenFactory.join(eventBase.getSource(),
				rhs.getSource());

		return nodeFactory.newOperatorEventNode(operationSource, operator,
				eventBase, rhs);
	}

	private DependsEventNode translateDependsEventBase(CommonTree tree,
			SimpleScope scope) throws ABCException {
		int kind = tree.getType();
		Source source = this.parseTree.source(tree);

		switch (kind) {
		case READ_ACSL: {
			SequenceNode<ExpressionNode> memList = translateCommaSeparatedList(
					(CommonTree) tree.getChild(0), scope);

			return nodeFactory.newMemoryEventNode(source,
					MemoryEventNodeKind.READ, memList);
		}
		case WRITE_ACSL: {
			SequenceNode<ExpressionNode> memList = translateCommaSeparatedList(
					(CommonTree) tree.getChild(0), scope);

			return nodeFactory.newMemoryEventNode(source,
					MemoryEventNodeKind.WRITE, memList);
		}
		case ACCESS_ACSL: {
			SequenceNode<ExpressionNode> memList = translateCommaSeparatedList(
					(CommonTree) tree.getChild(0), scope);

			return nodeFactory.newMemoryEventNode(source,
					MemoryEventNodeKind.REACH, memList);

		}
		case CALL_ACSL: {
			IdentifierNode function = translateIdentifier(
					(CommonTree) tree.getChild(0));
			SequenceNode<ExpressionNode> args = translateCommaSeparatedList(
					(CommonTree) tree.getChild(1), scope);

			return nodeFactory.newCallEventNode(source,
					this.nodeFactory.newIdentifierExpressionNode(
							function.getSource(), function),
					args);
		}
		case NOTHING:
			return nodeFactory.newNoactNode(source);
		case ANYACT:
			return nodeFactory.newAnyactNode(source);
		case EVENT_PARENTHESIZED:
			return translateDependsEvent((CommonTree) tree.getChild(0), scope);
		default:
			throw this.error("unknown kind of nodes for depends events", tree);
		}
	}

	private RequiresNode translateRequires(CommonTree tree, SimpleScope scope)
			throws ABCException {
		CommonTree expressionTree = (CommonTree) tree.getChild(1);
		Source source = this.newSource(tree);
		ExpressionNode expression = this.translateExpression(expressionTree,
				scope);

		return nodeFactory.newRequiresNode(source, expression);
	}

	private EnsuresNode translateEnsures(CommonTree tree, SimpleScope scope)
			throws ABCException {
		Source source = this.newSource(tree);
		CommonTree expressionTree = (CommonTree) tree.getChild(1);
		ExpressionNode expression = this.translateExpression(expressionTree,
				scope);

		return nodeFactory.newEnsuresNode(source, expression);
	}

	/**
	 * Translates an expression.
	 *
	 * @param expressionTree
	 *            any CommonTree node representing an expression
	 * @return an ExpressionNode
	 * @throws ABCException
	 */
	private ExpressionNode translateExpression(CommonTree expressionTree,
			SimpleScope scope) throws ABCException {
		Source source = this.newSource(expressionTree);
		int kind = expressionTree.getType();

		switch (kind) {
		case INTEGER_CONSTANT:
			return translateIntegerConstant(source, expressionTree);
		case FLOATING_CONSTANT:
			return translateFloatingConstant(source, expressionTree);
		case CHARACTER_CONSTANT:
			return translateCharacterConstant(source, expressionTree);
		case STRING_LITERAL:
			return translateStringLiteral(source, expressionTree);
		case IDENTIFIER: {
			IdentifierNode identifier = translateIdentifier(expressionTree);
			ExpressionNode enumerationConsant = translateEnumerationConstant(
					identifier, scope);

			return enumerationConsant != null ? enumerationConsant
					: nodeFactory.newIdentifierExpressionNode(source,
							identifier);
		}
		case DOT:
		case ARROW:
			return translateDotOrArrow(source, expressionTree, scope);
		case OPERATOR:
			return translateOperatorExpression(source, expressionTree, scope);
		case RELCHAIN:
			return translateRelationalChain(expressionTree, scope);
		case TRUE_ACSL:
			return translateTrue(source);
		case FALSE_ACSL:
			return translateFalse(source);
		case RESULT_ACSL:
			return nodeFactory.newResultNode(source);
		case SELF:
			return nodeFactory.newSelfNode(source);
		case DOTDOT:
			return translateRegularRange(source, expressionTree, scope);
		case WRITE_ACSL:
			return translateWriteEvent(source, expressionTree, scope);
		case NOTHING:
			return this.nodeFactory.newNothingNode(source);
		case ELLIPSIS:
			return this.nodeFactory.newWildcardNode(source);
		case MPI_BACK_MESSAGES:
		case MPI_COMM_RANK:
		case MPI_COMM_SIZE:
		case MPI_EXPRESSION:
			return translateMPIExpressionNode(expressionTree, source, scope);
		case VALID:
			return translateValidNode(expressionTree, source, scope, false);
		case AcslParser.VALID_READ:
			return translateValidNode(expressionTree, source, scope, true);
		case QUANTIFIED:
			return translateQuantifiedExpression(expressionTree, source, scope);
		case FUNC_CALL:
			return translateCall(source, expressionTree, scope);
		case AcslParser.OBJECT_OF:
			return translateObjectOf(source, expressionTree, scope);
		case AcslParser.QUANTIFIED_EXT:
			return translateExtendedQuantification(source,
					(CommonTree) expressionTree.getChild(0), scope);
		case AcslParser.LAMBDA_ACSL:
			return translateLambda(source, expressionTree, scope);
		case AcslParser.OLD:
			return translateOld(source, expressionTree, scope);
		case AcslParser.SEPARATED:
			return translateSeparated(source, expressionTree, scope);
		case SIZEOF:
			return translateSizeOf(source, expressionTree, scope);
		case CAST:
			return nodeFactory.newCastNode(source,
					translateTypeExpr((CommonTree) expressionTree.getChild(0),
							scope),
					translateExpression((CommonTree) expressionTree.getChild(1),
							scope));
		case CURLY_BRACED_EXPR:
			return processCurlyBracedSet(expressionTree, scope);
		default:
			throw error("Unknown expression kind", expressionTree);
		} // end switch
	}

	/**
	 * @param expressionTree
	 * @return
	 * @throws ABCException
	 */
	private SizeofNode translateSizeOf(Source source, CommonTree expressionTree,
			SimpleScope scope) throws ABCException {
		int kind = expressionTree.getChild(0).getType();
		CommonTree child = (CommonTree) expressionTree.getChild(1);
		SizeableNode sizeable;

		if (kind == EXPR)
			sizeable = translateExpression(child, scope);
		else if (kind == TYPE)
			sizeable = this.translateTypeExpr(child, scope);
		else
			throw error("Unexpected argument to sizeof", expressionTree);
		return nodeFactory.newSizeofNode(source, sizeable);
	}

	private ExpressionNode translateOld(Source source, CommonTree old,
			SimpleScope scope) throws ABCException {
		ExpressionNode expr = this
				.translateExpression((CommonTree) old.getChild(1), scope);

		return nodeFactory.newOperatorNode(source, Operator.OLD, expr);
	}

	private ExpressionNode translateSeparated(Source source, CommonTree node,
			SimpleScope scope) throws ABCException {
		int numChildren = node.getChildCount();
		List<ExpressionNode> arguments = new LinkedList<>();

		for (int i = 0; i < numChildren; i++) {
			CommonTree tsetTree = (CommonTree) node.getChild(i);
			ExpressionNode tset = translateExpression(tsetTree, scope);

			arguments.add(tset);
		}
		return this.nodeFactory.newSeparatedNode(source, arguments);
	}

	private ExpressionNode translateLambda(Source source, CommonTree lambda,
			SimpleScope scope) throws ABCException {
		SimpleScope newScope = new SimpleScope(scope);
		SequenceNode<VariableDeclarationNode> variableList = this
				.translateBinders((CommonTree) lambda.getChild(1), newScope);
		ExpressionNode expression = this
				.translateExpression((CommonTree) lambda.getChild(2), newScope);

		assert variableList.numChildren() == 1;
		return nodeFactory.newLambdaNode(source,
				variableList.getSequenceChild(0).copy(), expression);
	}

	private ExpressionNode translateExtendedQuantification(Source source,
			CommonTree extQuant, SimpleScope scope) throws ABCException {
		int quant = extQuant.getType();
		ExtendedQuantifier quantifier = null;
		ExpressionNode lo = this
				.translateExpression((CommonTree) extQuant.getChild(1), scope),
				hi = this.translateExpression((CommonTree) extQuant.getChild(2),
						scope),
				function = this.translateExpression(
						(CommonTree) extQuant.getChild(3), scope);

		switch (quant) {
		case AcslParser.MAX:
			quantifier = ExtendedQuantifier.MAX;
			break;
		case AcslParser.MIN:
			quantifier = ExtendedQuantifier.MIN;
			break;
		case AcslParser.SUM:
			quantifier = ExtendedQuantifier.SUM;
			break;
		case AcslParser.PROD:
			quantifier = ExtendedQuantifier.PROD;
			break;
		case AcslParser.NUMOF:
			quantifier = ExtendedQuantifier.NUMOF;
			break;
		default:
			throw this.error("unknown kind of extended quantifier ", extQuant);
		}
		return nodeFactory.newExtendedQuantifiedExpressionNode(source,
				quantifier, lo, hi, function);
	}

	private ExpressionNode translateObjectOf(Source source, CommonTree tree,
			SimpleScope scope) throws ABCException {
		CommonTree operandTree = (CommonTree) tree.getChild(2);
		ExpressionNode operand = this.translateExpression(operandTree, scope);

		return nodeFactory.newObjectofNode(source, operand);
	}

	/**
	 * translate a quantified expression. e.g. \\forall | \\exists type_name
	 * identifier; predicate
	 *
	 * @param expressionTree
	 * @param source
	 * @param scope
	 * @return
	 * @throws ABCException
	 */
	private ExpressionNode translateQuantifiedExpression(
			CommonTree expressionTree, Source source, SimpleScope scope)
			throws ABCException {
		SimpleScope newScope = new SimpleScope(scope);
		CommonTree quantifierTree = (CommonTree) expressionTree.getChild(0);
		// The children of the quantifierTree are as follows:
		// arg0: the keyword "\forall", "\exists", or "\lambda"
		// arg1: the binders tree
		// arg2: the formula
		CommonTree bindersTree = (CommonTree) expressionTree.getChild(1);
		CommonTree predTree = (CommonTree) expressionTree.getChild(2);
		ExpressionNode restrict, pred;
		SequenceNode<VariableDeclarationNode> binders;
		Quantifier quantifier;
		// If the quantified expression has more than one binder, it will be
		// translated into several quantifiedExpressions each of which has exact
		// one binder:
		// boolean firstQuantifiedExpr = true;
		// ExpressionNode result = null;
		List<PairNode<SequenceNode<VariableDeclarationNode>, ExpressionNode>> boundVariableList = new LinkedList<>();

		if (quantifierTree.getType() == AcslParser.FORALL_ACSL) {
			quantifier = Quantifier.FORALL;
			restrict = null;
			// if the expression has the form "forall ... ; p==>q",
			// the type of predicate will be OPERATOR, arg0 will be IMPLIES
			// or IMPLIES_ACSL, and arg1 will be an ARGUMENT_LIST with 2
			// children, p and q.
			if (predTree.getType() == OPERATOR) {
				Tree predOperatorTree = predTree.getChild(0);
				int predOperatorType = predOperatorTree.getType();

				if (predOperatorType == IMPLIES_ACSL
						|| predOperatorType == IMPLIES) {
					Tree predArgTree = predTree.getChild(1);

					assert predArgTree.getChildCount() == 2;

					CommonTree restrictTree = (CommonTree) predArgTree
							.getChild(0);

					predTree = (CommonTree) predArgTree.getChild(1);
					restrict = translateExpression(restrictTree, newScope);
				}
			}
			pred = translateExpression(predTree, newScope);
		} else if (quantifierTree.getType() == AcslParser.EXISTS_ACSL) {
			quantifier = Quantifier.EXISTS;
			pred = translateExpression(predTree, newScope);
			restrict = null;
		} else {
			throw error("Unexpexted quantifier " + quantifierTree.getType(),
					quantifierTree);
		}
		binders = translateBinders(bindersTree, newScope);
		boundVariableList.add(nodeFactory.newPairNode(source, binders, null));
		return nodeFactory.newQuantifiedExpressionNode(source, quantifier,
				nodeFactory.newSequenceNode(source,
						"bound variable declaration list", boundVariableList),
				restrict, pred, null);
	}

	/**
	 * Translate a comma separated binder list
	 * 
	 * @param tree
	 *            a CommonTree node with n (n > 1) children: a BINDER and (n-1)
	 *            BINDER_NEXTs
	 * @param scope
	 * @return
	 * @throws ABCException
	 */
	private SequenceNode<VariableDeclarationNode> translateBinders(
			CommonTree tree, SimpleScope scope) throws ABCException {
		int count = tree.getChildCount();
		List<VariableDeclarationNode> vars = new LinkedList<>();
		Source source = newSource(tree);
		CommonTree binder = (CommonTree) tree.getChild(0);

		vars.add(translateBinder(binder, scope));

		TypeNode prevType = vars.get(0).getTypeNode();
		for (int i = 1; i < count; i++) {
			CommonTree binderNext = (CommonTree) tree.getChild(i);

			vars.addAll(translateBinderNext(binderNext, prevType, scope));
		}
		return this.nodeFactory.newSequenceNode(source, "Binder List", vars);
	}

	/**
	 * translate the binder component that follows a COMMA in a COMMA separated
	 * list
	 * 
	 * @param binderNext
	 *            a CommonTree node having one child that is either a
	 *            variable_ident (see
	 *            {@link #translateVariableIdent(CommonTree, SimpleScope, TypeNode)})
	 *            or a binder (see
	 *            {@link #translateBinder(CommonTree, SimpleScope)}).
	 * @return
	 * @throws ABCException
	 */
	private List<VariableDeclarationNode> translateBinderNext(
			CommonTree binderNext, TypeNode prevBinderType, SimpleScope scope)
			throws ABCException {
		CommonTree child = (CommonTree) binderNext.getChild(0);
		int type = child.getType();
		List<VariableDeclarationNode> result = new LinkedList<>();

		if (type == VARIABLE_ID) {
			VariableDeclarationNode binder = translateVariableIdent(child,
					scope, prevBinderType.copy());

			result.add(binder);
			return result;
		}
		assert type == BINDER;
		result.add(translateBinder(child, scope));
		return result;
	}

	/**
	 * A binder declares a variable with a type in ACSL
	 * 
	 * @param tree
	 *            the CommonTree node with two children: the first child is
	 *            either a logic type tree or a C specifier qualifier list tree
	 *            (see {@link #translateTypeExpr(CommonTree, SimpleScope)}); the
	 *            second is a variable_ident (see
	 *            {@link #translateVariableIdent(CommonTree, SimpleScope, TypeNode)}).
	 * @throws ABCException
	 */
	private VariableDeclarationNode translateBinder(CommonTree tree,
			SimpleScope scope) throws ABCException {
		CommonTree typeTree = (CommonTree) tree.getChild(0);
		TypeNode type = translateTypeExpr(typeTree, scope);

		return translateVariableIdent((CommonTree) tree.getChild(1), scope,
				type);
	}

	/**
	 * Translate ACSL variable identifier
	 * 
	 * @param tree
	 *            a CommonTree with two possible sets of children: 1. an
	 *            IDENTIFIER followed by n (n >= 0) brackets 2. an (optional
	 *            STAR) followed by a (recursive) variable_ident
	 * @param scope
	 *            the scope to which the translating variable identifier belongs
	 * @param baseType
	 *            the base type of the translating variable identifier
	 */
	private VariableDeclarationNode translateVariableIdent(CommonTree tree,
			SimpleScope scope, TypeNode baseType) throws ABCException {
		CommonTree firstChild = (CommonTree) tree.getChild(0);
		Source source = newSource(tree);

		if (firstChild.getType() == IDENTIFIER) {
			IdentifierNode ident = translateIdentifier(firstChild);
			int numBrackets = tree.getChildCount() - 1;

			for (int i = 0; i < numBrackets; i++)
				// check if brackets are correctly parsed:
				if (tree.getChild(i + 1).getType() == 0)
					throw error("invalid ACSL variable_ident syntax", tree);
			for (int i = 0; i < numBrackets; i++)
				baseType = nodeFactory.newArrayTypeNode(source, baseType, null);
			return nodeFactory.newVariableDeclarationNode(source, ident,
					baseType);
		}

		CommonTree varIdentTree = firstChild;

		if (firstChild.getType() == STAR) {
			baseType = nodeFactory.newPointerTypeNode(source, baseType);
			varIdentTree = (CommonTree) tree.getChild(1);
		}
		return translateVariableIdent(varIdentTree, scope, baseType);
	}

	// /**
	// * <code>
	// * variable_ident_base :=
	// * IDENTIFIER
	// * | STAR variable_ident
	// * | LPAREN variable_ident RPAREN
	// *
	// * </code>
	// */
	// private VariableDeclarationNode translateVariableIdentBase(CommonTree
	// tree,
	// Source source, SimpleScope scope, TypeNode baseType)
	// throws ABCException {
	// CommonTree firstChild = (CommonTree) tree.getChild(0);
	// int kind = firstChild.getType();
	//
	// if (kind == IDENTIFIER) {
	// IdentifierNode identifier = translateIdentifier(tree);
	//
	// return nodeFactory.newVariableDeclarationNode(
	// identifier.getSource(), identifier, baseType);
	// }
	//
	// CommonTree secondChild = (CommonTree) tree.getChild(1);
	//
	// if (kind == STAR) {
	// VariableDeclarationNode result = translateVariableIdent(secondChild,
	// scope, baseType);
	// TypeNode typeNode = result.getTypeNode();
	// Source typeSrc = typeNode.getSource();
	//
	// typeNode.remove();
	// typeSrc = tokenFactory.join(typeSrc, newSource(firstChild));
	// typeNode = nodeFactory.newPointerTypeNode(typeSrc, typeNode);
	// result.setTypeNode(typeNode);
	// return result;
	// } else {
	// assert tree.getChild(2).getType() == AcslParser.RPAREN;
	// return translateVariableIdent(secondChild, scope, baseType);
	// }
	// }

	/**
	 * Translates a tree of type LOGIC_TYPE, C_TYPE or
	 * C_SPECIFIER_QUALIFIER_LIST.
	 * 
	 * @return the translated type node
	 */
	private TypeNode translateTypeExpr(CommonTree tree, SimpleScope scope)
			throws ABCException {
		int kind = tree.getType();

		switch (kind) {
		case LOGIC_TYPE:
			return translateLogicType((CommonTree) tree.getChild(0), scope);
		case C_TYPE:
			return translateCType((CommonTree) tree.getChild(0),
					(CommonTree) tree.getChild(1), scope);
		case SPECIFIER_QUALIFIER_LIST:
			return translateSpecifierList(tree, scope);
		default:
			throw this.error("unkown kind of type expression", tree);
		}
	}

	/**
	 * ^(C_TYPE specifierList abstractDeclarator)
	 *
	 * @param specifierList
	 *            Type specifier tree
	 * @param abstractDeclarator
	 *            Abstract declarator tree
	 * @param scope
	 * @return
	 * @throws ABCException
	 */
	private TypeNode translateCType(CommonTree specifierList,
			CommonTree abstractDeclarator, SimpleScope scope)
			throws ABCException {
		TypeNode result;
		DeclaratorData declaratorData;

		result = translateSpecifierList(specifierList, scope);
		declaratorData = processDeclarator(abstractDeclarator, result, scope);
		result = declaratorData.type;
		return result;
	}

	/**
	 * Translate a list of specifiers (C basic types)
	 */
	private TypeNode translateSpecifierList(CommonTree specifierList,
			SimpleScope scope) throws ABCException {
		Source specifierSource = newSource(specifierList);
		SpecifierAnalysis specifierAnalyzer = new SpecifierAnalysis(
				specifierList, parseTree, config);
		TypeNode result;

		if (specifierAnalyzer.typeNameKind == TypeNodeKind.BASIC)
			result = nodeFactory.newBasicTypeNode(specifierSource,
					specifierAnalyzer.getBasicTypeKind());
		else if (specifierAnalyzer.typeNameKind == TypeNodeKind.VOID)
			result = nodeFactory.newVoidTypeNode(specifierSource);
		else
			throw new RuntimeException("Translation of C type of kind : "
					+ specifierAnalyzer.typeNameKind
					+ " has not been implemented.");
		return result;
	}

	/**
	 * Creates a new DeclaratorData based on given direct declarator tree node
	 * and base type. The direct declarator may be abstract.
	 *
	 * @param directDeclarator
	 *            A DIRECT_ABSTRACT_DECLARATOR type CommonTree node with 1 or 2
	 *            children: the first child is
	 *            DIRECT_ABSTRACT_DECLARATOR_POINTER_TO,
	 *            DIRECT_ABSTRACT_DECLARATOR_PARAM_LIST, or
	 *            DIRECT_ABSTRACT_DECLARATOR_ARRAY; the optional second child is
	 *            DIRECT_ABSTRACT_DECLARATOR again.
	 * @param type
	 *            base type
	 * @return new DeclaratorData with derived type and identifier
	 * @throws ABCException
	 */
	private DeclaratorData processDirectDeclarator(CommonTree directDeclarator,
			TypeNode baseType, SimpleScope scope) throws ABCException {
		int treeType = directDeclarator.getType();

		assert treeType == DIRECT_ABSTRACT_DECLARATOR;

		// the first child is DIRECT_ABSTRACT_DECLARATOR_POINTER_TO,
		// DIRECT_ABSTRACT_DECLARATOR_PARAM_LIST,
		// or DIRECT_ABSTRACT_DECLARATOR_ARRAY
		CommonTree firstChild = (CommonTree) directDeclarator.getChild(0);
		// the second child, if not null, is another DIRECT_ABSTRACT_DECLARATOR
		CommonTree secondChild = (CommonTree) directDeclarator.getChild(1);
		DeclaratorData result = secondChild != null
				? processDirectDeclarator(secondChild, baseType, scope)
				: new DeclaratorData(baseType, null);

		treeType = firstChild.getType();
		if (treeType == DIRECT_ABSTRACT_DECLARATOR_POINTER_TO) {
			result = processDeclarator((CommonTree) firstChild.getChild(0),
					result.type, scope);
			result.type = nodeFactory.newPointerTypeNode(
					newSource(directDeclarator), result.type);
		} else if (treeType == DIRECT_ABSTRACT_DECLARATOR_PARAM_LIST) {
			List<VariableDeclarationNode> params = new LinkedList<>();

			if (firstChild.getChildCount() != 0) {
				CommonTree typeList = (CommonTree) firstChild.getChild(0);
				int numTypes = typeList.getChildCount();

				for (int i = 0; i < numTypes; i++) {
					CommonTree paramTypeTree = (CommonTree) typeList
							.getChild(i);
					TypeNode paramType = translateTypeExpr(paramTypeTree,
							scope);
					Source paramSource = newSource(paramTypeTree);

					params.add(
							nodeFactory
									.newVariableDeclarationNode(
											newSource(paramTypeTree),
											nodeFactory.newIdentifierNode(
													paramSource, "$anon" + i),
											paramType));
				}
			}

			SequenceNode<VariableDeclarationNode> paramSeqNode = nodeFactory
					.newSequenceNode(newSource(directDeclarator), "param list",
							params);

			result.type = nodeFactory.newFunctionTypeNode(
					newSource(directDeclarator), result.type, paramSeqNode,
					false);
		} else if (treeType == DIRECT_ABSTRACT_DECLARATOR_ARRAY) {
			CommonTree arrLenTree = (CommonTree) firstChild.getChild(0);
			ExpressionNode arrLen = translateExpression(arrLenTree, scope);

			result.type = nodeFactory.newArrayTypeNode(newSource(arrLenTree),
					result.type, arrLen);
		} else
			throw error("unknown type of a directDeclarator tree",
					directDeclarator);
		return result;
	}

	/**
	 * Creates new DeclaratorData based on given declarator tree node and base
	 * type. The declarator may be abstract. The data gives the new type formed
	 * by applying the type derivation operations of the declarator to the base
	 * type. The data also gives the identifier being declared, though this may
	 * be null in the case of an abstract declarator.
	 *
	 * @param declarator
	 *            CommonTree node of type ABSTRACT_DECLARATOR. It has two
	 *            children: a CommonTree of type POINTER and a
	 *            directAbstractDeclarator non-terminal
	 * @param baseType
	 *            the start type before applying declarator operations
	 * @return new DeclaratorData with type derived from given type and
	 *         identifier
	 * @throws ABCException
	 */
	private DeclaratorData processDeclarator(CommonTree declarator,
			TypeNode baseType, SimpleScope scope) throws ABCException {
		CommonTree pointerTree = (CommonTree) declarator.getChild(0);
		CommonTree directDeclarator = (CommonTree) declarator.getChild(1);
		baseType = translatePointers(pointerTree, baseType, scope);

		if (directDeclarator != null)
			return processDirectDeclarator(directDeclarator, baseType, scope);
		return new DeclaratorData(baseType, null);
	}

	/**
	 * Returns the new type obtained by taking the given type and applying the
	 * pointer operations to it. For example, if the old type is "int" and the
	 * pointerTree is "*", the result is the type "pointer to int".
	 *
	 * @param pointerTree
	 *            CommonTree node of type POINTER or ABSENT
	 * @param type
	 *            base type
	 * @return modified type
	 * @throws ABCException
	 *             if an unknown kind of type qualifier appears
	 */
	private TypeNode translatePointers(CommonTree pointerTree, TypeNode type,
			SimpleScope scope) throws ABCException {
		int numChildren = pointerTree.getChildCount();
		Source source = type.getSource();

		for (int i = 0; i < numChildren; i++) {
			CommonTree starNode = (CommonTree) pointerTree.getChild(i);

			source = tokenFactory.join(source, newSource(starNode));
			type = nodeFactory.newPointerTypeNode(source, type);
		}
		return type;
	}

	private TypeNode translateLogicType(CommonTree tree, SimpleScope scope)
			throws ABCException {
		int kind = tree.getType();
		Source source = this.newSource(tree);

		switch (kind) {
		case TYPE_BUILTIN: {
			int typeKind = tree.getChild(0).getType();

			switch (typeKind) {
			case BOOLEAN:
				return this.nodeFactory.newBasicTypeNode(source,
						BasicTypeKind.BOOL);
			case INTEGER:
				return this.nodeFactory.newBasicTypeNode(source,
						BasicTypeKind.INT);
			case REAL_ACSL:
				return this.nodeFactory.newBasicTypeNode(source,
						BasicTypeKind.REAL);
			default:
				throw this.error("unknown built-in logic type", tree);
			}
		}
		default:
			throw this.error("unknown kind of logic type", tree);
		}
	}

	// ////////////////////////////////////
	private ExpressionNode translateValidNode(CommonTree tree, Source source,
			SimpleScope scope, boolean isRead) throws ABCException {
		CommonTree argumentTree = (CommonTree) tree.getChild(0);
		ExpressionNode argument;

		argument = translateExpression(argumentTree, scope);
		// TODO: support to distinguish VALID_READ and VALID
		return nodeFactory.newOperatorNode(source, Operator.VALID, argument);
	}

	private ExpressionNode translateWriteEvent(Source source,
			CommonTree expressionTree, SimpleScope scope) {
		// TODO Auto-generated method stub
		return null;
	}

	private IntegerConstantNode translateIntegerConstant(Source source,
			CommonTree integerConstant) throws ABCException {
		return nodeFactory.newIntegerConstantNode(source,
				integerConstant.getText());
	}

	private FloatingConstantNode translateFloatingConstant(Source source,
			CommonTree floatingConstant) throws ABCException {
		return nodeFactory.newFloatingConstantNode(source,
				floatingConstant.getText());
	}

	private CharacterConstantNode translateCharacterConstant(Source source,
			CommonTree characterConstant) throws ABCException {
		CharacterToken token = (CharacterToken) characterConstant.getToken();

		return nodeFactory.newCharacterConstantNode(source,
				characterConstant.getText(), token.getExecutionCharacter());
	}

	private ConstantNode translateTrue(Source source) {
		return nodeFactory.newBooleanConstantNode(source, true);
	}

	private ConstantNode translateFalse(Source source) {
		return nodeFactory.newBooleanConstantNode(source, false);
	}

	private StringLiteralNode translateStringLiteral(Source source,
			CommonTree stringLiteral) throws ABCException {
		StringToken token = (StringToken) stringLiteral.getToken();

		return nodeFactory.newStringLiteralNode(source, stringLiteral.getText(),
				token.getStringLiteral());
	}

	private ExpressionNode translateRegularRange(Source source,
			CommonTree expressionTree, SimpleScope scope) throws ABCException {
		// regular range expression lo..hi or lo..hi#step
		ExpressionNode loNode = translateExpression(
				(CommonTree) expressionTree.getChild(0), scope);
		ExpressionNode hiNode = translateExpression(
				(CommonTree) expressionTree.getChild(1), scope);
		if (expressionTree.getChildCount() > 2) {
			CommonTree stepTree = (CommonTree) expressionTree.getChild(2);

			if (stepTree != null /* && stepTree.getType() != ABSENT */) {
				ExpressionNode stepNode = translateExpression(stepTree, scope);

				return nodeFactory.newRegularRangeNode(source, loNode, hiNode,
						stepNode);
			}
		}
		return nodeFactory.newRegularRangeNode(source, loNode, hiNode);

	}

	/**
	 * Translates a function call expression.
	 *
	 * @param callTree
	 *            CommonTree node of type CALL, representing a function call
	 * @return a FunctionCallNode corresponding to the ANTLR tree
	 * @throws ABCException
	 */
	private FunctionCallNode translateCall(Source source, CommonTree callTree,
			SimpleScope scope) throws ABCException {
		CommonTree functionTree = (CommonTree) callTree.getChild(0);
		// CommonTree contextArgumentListTree = (CommonTree)
		// callTree.getChild(2);
		CommonTree argumentListTree = (CommonTree) callTree.getChild(1);
		ExpressionNode functionNode = translateExpression(functionTree, scope);
		// int numContextArgs = contextArgumentListTree.getChildCount();
		int numArgs = argumentListTree.getChildCount();
		// List<ExpressionNode> contextArgumentList = new
		// LinkedList<ExpressionNode>();
		List<ExpressionNode> argumentList = new LinkedList<ExpressionNode>();

		for (int i = 0; i < numArgs; i++) {
			CommonTree argumentTree = (CommonTree) argumentListTree.getChild(i);
			ExpressionNode argumentNode = translateExpression(argumentTree,
					scope);

			argumentList.add(argumentNode);
		}
		return nodeFactory.newFunctionCallNode(source, functionNode,
				new LinkedList<ExpressionNode>(), argumentList, null);
	}

	/**
	 * @param expressionTree
	 * @return
	 * @throws ABCException
	 */
	private ExpressionNode translateDotOrArrow(Source source,
			CommonTree expressionTree, SimpleScope scope) throws ABCException {
		int kind = expressionTree.getType();
		CommonTree argumentNode = (CommonTree) expressionTree.getChild(0);
		CommonTree fieldNode = (CommonTree) expressionTree.getChild(1);
		ExpressionNode argument = translateExpression(argumentNode, scope);
		IdentifierNode fieldName = translateIdentifier(fieldNode);

		if (kind == DOT)
			return nodeFactory.newDotNode(source, argument, fieldName);
		else
			return nodeFactory.newArrowNode(source, argument, fieldName);
	}

	/**
	 * Translates an ACSL relational chain expression. An example is "x < y < z"
	 * , which is ACSL short-hand for "x<y && y<z". Here are some notes from the
	 * ACSL spec:
	 *
	 * <pre>
	 * The construct t1 relop1 t2 relop2 t3 · · · tk
	 * with several consecutive comparison operators is a shortcut
	 * (t1 relop1 t2) && (t2 relop2 t3) && ···.
	 * It is required that the relopi operators must be in the same "direction",
	 * i.e. they must all belong either to {<, <=, ==} or to {>,>=,==}.
	 * Expressions such as x < y > z or x != y != z are not allowed.
	 * </pre>
	 * 
	 * <p>
	 * Input tree: <code>
	 * ^( RELCHAIN expr ^(RELCHAIN_SEGMENT op expr)* )
	 * </code>
	 * </p>
	 * 
	 * @param source
	 *            source for the token sequence which makes up the expression
	 * @param expressionTree
	 *            the parse tree resulting from parsing the expression token
	 *            sequence
	 * @param scope
	 *            the scope in which the expression occurs
	 * @return the root of an AST tree representing this expression
	 * @throws ABCException
	 *             if the operators are not all in the same "direction"
	 */
	private ExpressionNode translateRelationalChain(CommonTree tree,
			SimpleScope scope) throws ABCException {
		int numSegments = tree.getChildCount() - 1;
		CommonTree leftTree = (CommonTree) tree.getChild(0);
		ExpressionNode leftExpr = translateExpression(leftTree, scope);
		ExpressionNode result = null;

		if (numSegments == 0)
			return leftExpr;

		// 0 for unknown or equal, 1 for increasing, -1 for decreasing and -2
		// for not equal.
		int direction = 0;

		for (int i = 0; i < numSegments; i++) {
			CommonTree segmentTree = (CommonTree) tree.getChild(i + 1);
			CommonTree opTree = (CommonTree) segmentTree.getChild(0);
			CommonTree operandTree = (CommonTree) segmentTree.getChild(1);
			ExpressionNode operand = translateExpression(operandTree, scope);
			ExpressionNode tmp;
			Operator op = null;

			switch (opTree.getType()) {
			case LT:
			case LTE:
				if (direction < 0)
					throw error(
							">, >=, or != is prohibited in a increasing relational chain",
							(CommonTree) tree.getChild(i - 1));
				op = opTree.getType() == LT ? Operator.LT : Operator.LTE;
				direction = 1;
				break;
			case GT:
			case GTE:
				if (direction > 0 || direction == -2)
					throw error(
							"<, <=, or != is prohibited in a decreasing relational chain",
							(CommonTree) tree.getChild(i - 1));
				op = opTree.getType() == GT ? Operator.GT : Operator.GTE;
				direction = -1;
				break;
			case EQUALS:
				if (direction == -2)
					throw error("!= is prohibited in a relational chain",
							opTree);
				op = Operator.EQUALS;
				direction = 0;
				break;
			case NEQ:
				if (direction != 0 || i != 0)
					throw error("!= is prohibited in a relational chain",
							opTree);
				op = Operator.NEQ;
				direction = -2;
				break;
			default:
				throw error("unexpected operator " + opTree
						+ " in a relational chain", opTree);
			}
			assert op != null;
			tmp = nodeFactory
					.newOperatorNode(
							tokenFactory.join(leftExpr.getSource(),
									newSource(segmentTree)),
							op, leftExpr, operand);
			result = result == null ? tmp
					: nodeFactory.newOperatorNode(
							tokenFactory.join(result.getSource(),
									tmp.getSource()),
							Operator.LAND, result, tmp);
			leftExpr = operand.copy();
		}
		return result;
	}

	/**
	 * @param expressionTree
	 * @return
	 * @throws ABCException
	 */
	private OperatorNode translateOperatorExpression(Source source,
			CommonTree expressionTree, SimpleScope scope) throws ABCException {
		CommonTree operatorTree = (CommonTree) expressionTree.getChild(0);
		int operatorKind = operatorTree.getType();
		CommonTree argumentList = (CommonTree) expressionTree.getChild(1);
		int numArgs = argumentList.getChildCount();
		List<ExpressionNode> arguments = new LinkedList<ExpressionNode>();
		Operator operator;

		for (int i = 0; i < numArgs; i++) {
			ExpressionNode argument = translateExpression(
					(CommonTree) argumentList.getChild(i), scope);

			arguments.add(argument);
		}
		switch (operatorKind) {
		case AMPERSAND:
			operator = numArgs == 1 ? Operator.ADDRESSOF : Operator.BITAND;
			break;
		case ASSIGN:
			operator = Operator.ASSIGN;
			break;
		case TILDE:
			operator = Operator.BITCOMPLEMENT;
			break;
		case BITOR:
			operator = Operator.BITOR;
			break;
		case BITXOR:
			operator = Operator.BITXOR;
			break;
		case COMMA:
			operator = Operator.COMMA;
			break;
		case QMARK:
			operator = Operator.CONDITIONAL;
			break;
		case STAR:
			operator = numArgs == 1 ? Operator.DEREFERENCE : Operator.TIMES;
			break;
		case DIV:
			operator = Operator.DIV;
			break;
		case EQUALS:
			operator = Operator.EQUALS;
			break;
		case GT:
			operator = Operator.GT;
			break;
		case GTE:
			operator = Operator.GTE;
			break;
		case HASH:
			operator = Operator.HASH;
			break;
		case AND:
			operator = Operator.LAND;
			break;
		case OR:
			operator = Operator.LOR;
			break;
		case IMPLIES_ACSL:
			operator = Operator.IMPLIES;
			break;
		case LT:
			operator = Operator.LT;
			break;
		case LTE:
			operator = Operator.LTE;
			break;
		case SUB:
			operator = numArgs == 1 ? Operator.UNARYMINUS : Operator.MINUS;
			break;
		case MOD:
			operator = Operator.MOD;
			break;
		case NEQ:
			operator = Operator.NEQ;
			break;
		case NOT:
			operator = Operator.NOT;
			break;
		case PLUS:
			operator = numArgs == 1 ? Operator.UNARYPLUS : Operator.PLUS;
			break;
		case SHIFTLEFT:
			operator = Operator.SHIFTLEFT;
			break;
		case SHIFTRIGHT:
			operator = Operator.SHIFTRIGHT;
			break;
		case INDEX:
			operator = Operator.SUBSCRIPT;
			break;
		case XOR_ACSL:
			operator = Operator.LXOR;
			break;
		case BITEQUIV_ACSL:
			operator = Operator.BITEQUIV;
			break;
		case BIMPLIES_ACSL:
			operator = Operator.BITIMPLIES;
			break;
		case EQUIV_ACSL: {
			// A <==> B: A ==> B && B ==> A
			return processACSLEquivalenceArguments(arguments);
		}
		default:
			throw error("Unknown operator : " + operatorTree.getText(),
					operatorTree);
		}
		return nodeFactory.newOperatorNode(source, operator, arguments);
	}

	/**
	 * Takes in <code>
	 * a <==> b <==> c <==> ... 
	 * </code> and returns <code>
	 * (((a => b) && (b => a)) => c) && (c => ((a => b) && (b => a))) ...
	 * </code>
	 * 
	 * @param args
	 *            arguments of (consecutive) ACSL equivalent operations
	 */
	private OperatorNode processACSLEquivalenceArguments(
			List<ExpressionNode> args) {
		assert args.size() > 1;
		// the safety of the cast below depends on the assertion above:
		return (OperatorNode) processACSLEquivalenceArgumentsWorker(null, args);
	}

	/**
	 * worker method of {@link #processACSLEquivalenceArguments(List)}
	 * 
	 * @param left
	 *            the "left" most operand of the ACSL equivalence operation
	 * @param args
	 *            the rest of the operands of the ACSL (consecutive) equivalence
	 *            operation
	 * @return translated ACSL (consecutive) equivalence operation.
	 */
	private ExpressionNode processACSLEquivalenceArgumentsWorker(
			ExpressionNode left, List<ExpressionNode> args) {
		if (args.size() == 0)
			return left;

		ExpressionNode operand = args.remove(0);

		if (left == null) {
			left = operand;
			operand = args.remove(0);
		}

		Source source = tokenFactory.join(left.getSource(),
				operand.getSource());
		OperatorNode translationL, translationR;

		translationL = nodeFactory.newOperatorNode(source, Operator.IMPLIES,
				left, operand);
		translationR = nodeFactory.newOperatorNode(source, Operator.IMPLIES,
				operand.copy(), left.copy());
		return processACSLEquivalenceArgumentsWorker(
				nodeFactory.newOperatorNode(source, Operator.LAND, translationL,
						translationR),
				args);
	}

	/**
	 * tries to translate the given identifier node into an enumeration node
	 * according to the scope. If the identifer's name has NOT been declared as
	 * an enumeration constant in the scope, then return null.
	 *
	 * @param identifier
	 *            the identifier node to be translated
	 * @param scope
	 *            the current scope
	 * @return an enumeration constant node if the identifer's name has been
	 *         declared as an enumeration in the scope otherwise return null.
	 */
	private EnumerationConstantNode translateEnumerationConstant(
			IdentifierNode identifier, SimpleScope scope) {
		String name = identifier.name();

		if (scope.isEnumerationConstant(name))
			return this.nodeFactory.newEnumerationConstantNode(identifier);
		return null;
	}

	private IdentifierNode translateIdentifier(CommonTree identifier) {
		Token idToken = identifier.getToken();
		CivlcToken token;
		Source source;

		if (idToken instanceof CivlcToken)
			token = (CivlcToken) idToken;
		else
			token = tokenFactory.newCivlcToken(idToken, formation,
					TokenVocabulary.CIVLC);
		source = tokenFactory.newSource(token);
		return nodeFactory.newIdentifierNode(source, token.getText());
	}

	// ////////////////////////////////////

	private MPICollectiveBlockNode translateMPICollectiveBlock(
			CommonTree colBlock, SimpleScope scope) throws ABCException {
		CommonTree mpiComm = (CommonTree) colBlock.getChild(0);
		CommonTree body = (CommonTree) colBlock.getChild(1);
		List<ContractNode> bodyComponents = new LinkedList<>();
		SequenceNode<ContractNode> bodyNode;
		ExpressionNode mpiCommNode;
		Source source = newSource(colBlock);

		mpiCommNode = translateExpression(mpiComm, scope);
		bodyComponents.addAll(translateFunctionContractBlock(body, scope));
		bodyNode = nodeFactory.newSequenceNode(source, "mpi_collective body",
				bodyComponents);
		return nodeFactory.newMPICollectiveBlockNode(source, mpiCommNode,
				bodyNode);
	}

	private MPIContractExpressionNode translateMPIExpressionNode(
			CommonTree expressionTree, Source source, SimpleScope scope)
			throws ABCException {
		CommonTree expression = (CommonTree) expressionTree.getChild(0);
		int kind = expression.getType();
		List<ExpressionNode> args = new LinkedList<>();
		String exprName = expression.getText();
		MPIContractExpressionKind mpiExprKind;
		MPIContractExpressionNode result;
		int numChildren = expression.getChildCount();

		switch (kind) {
		case MPI_AGREE:
			mpiExprKind = MPIContractExpressionKind.MPI_AGREE;
			break;
		case MPI_BUF:
			mpiExprKind = MPIContractExpressionKind.MPI_BUF;
			break;
		case MPI_BACK_MESSAGES:
			return nodeFactory.newMPIExpressionNode(source, new LinkedList<>(),
					MPIContractExpressionKind.MPI_BACK_MESSAGES,
					MPI_BACK_MESSAGES_NAME);
		case MPI_COMM_RANK:
			return nodeFactory.newMPIExpressionNode(source, new LinkedList<>(),
					MPIContractExpressionKind.MPI_COMM_RANK,
					MPI_COMM_RANK_NAME);
		case MPI_COMM_SIZE:
			return nodeFactory.newMPIExpressionNode(source, new LinkedList<>(),
					MPIContractExpressionKind.MPI_COMM_SIZE,
					MPI_COMM_SIZE_NAME);
		case MPI_EXTENT:
			mpiExprKind = MPIContractExpressionKind.MPI_EXTENT;
			break;
		case MPI_NONOVERLAPPING:
			mpiExprKind = MPIContractExpressionKind.MPI_NONOVERLAPPING;
			break;
		case MPI_ON:
			mpiExprKind = MPIContractExpressionKind.MPI_ON;
			break;
		case MPI_REDUCE:
			mpiExprKind = MPIContractExpressionKind.MPI_REDUCE;
			break;
		case MPI_SIG:
			mpiExprKind = MPIContractExpressionKind.MPI_SIG;
			break;
		default:
			throw error("Unknown MPI expression " + exprName, expressionTree);
		}
		for (int i = 0; i < numChildren; i++) {
			ExpressionNode child = translateExpression(
					(CommonTree) expression.getChild(i), scope);

			args.add(child);
		}
		result = nodeFactory.newMPIExpressionNode(source, args, mpiExprKind,
				exprName);
		return result;
	}

	/**
	 * Translate ACSL curly-braced set expressions, i.e., explicit set or set
	 * comprehension.
	 * 
	 * @param a
	 *            <p>
	 *            CommonTree node has two possible sets of children: either an
	 *            expression tree and a curly_braced_set_suffix tree, or a
	 *            LCURLY and a RCURLY representing an empty set.
	 *            </p>
	 *            <p>
	 *            Note that a curly_braced_set_suffix tree has a child which is
	 *            either EXPLICIT_SET_SUFFIX (see
	 *            {@link #translateExplicitSet(CommonTree, CommonTree, SimpleScope, Source)})
	 *            or SET_BINDERS (see
	 *            {@link #translateSetComprehension(CommonTree, CommonTree, SimpleScope, Source)}).
	 *            </p>
	 */
	private ExpressionNode processCurlyBracedSet(CommonTree tree,
			SimpleScope scope) throws ABCException {
		CommonTree suffixTree, leadingExpr = null;

		leadingExpr = (CommonTree) tree.getChild(0);
		suffixTree = (CommonTree) tree.getChild(1);

		if (leadingExpr.getType() == LCURLY && suffixTree.getType() == RCURLY) {
			// empty explicit set:
			return nodeFactory.newNothingNode(newSource(tree));
		}
		if (suffixTree.getType() == SET_BINDERS) {
			return translateSetComprehension(leadingExpr, suffixTree, scope,
					newSource(tree));
		} else
			return translateExplicitSet(leadingExpr, suffixTree, scope,
					newSource(tree));
	}

	/**
	 * Translates set comprehension of the form:
	 * <code>LCURLY term | binders (;pred)? RCURLY</code>
	 */
	private ExpressionNode translateSetComprehension(CommonTree termsTree,
			CommonTree bindersPredTree, SimpleScope scope, Source source)
			throws ABCException {
		// set comprehension scope:
		SimpleScope newScope = new SimpleScope(scope);
		List<ExpressionNode> terms = new LinkedList<>();

		for (Object termTree : termsTree.getChildren()) {
			ExpressionNode term = translateExpression((CommonTree) termTree,
					newScope);

			terms.add(term);
		}

		CommonTree bindersTree = (CommonTree) bindersPredTree.getChild(0);
		SequenceNode<VariableDeclarationNode> binders = translateBinders(
				bindersTree, newScope);
		ExpressionNode pred = bindersPredTree.getChildCount() > 1
				? translateExpression((CommonTree) bindersPredTree.getChild(1),
						newScope)
				: nodeFactory.newBooleanConstantNode(source, true);

		return nodeFactory
				.newCurlyBracedTermSetNode(source,
						nodeFactory.newSequenceNode(source,
								"set comprehension terms", terms),
						binders, pred);
	}

	/**
	 * 
	 * Translates explicit set expression:
	 * <code>LCURLY firstExpr, (expr*) RCURLY</code>
	 * 
	 * @param firstExprTree
	 *            a CommonTree representing the first expression in a non-empty
	 *            explicit set
	 * @param suffixTree
	 *            a CommonTree with an optional child: COMMA_SEPARATED_LIST (see
	 *            {@link #translateCommaSeparatedList(CommonTree, SimpleScope)}
	 * @throws ABCException
	 */
	private ExpressionNode translateExplicitSet(CommonTree firstExprTree,
			CommonTree suffixTree, SimpleScope scope, Source source)
			throws ABCException {
		if (firstExprTree == null)
			return nodeFactory.newNothingNode(source);

		ExpressionNode firstExpr = translateExpression(firstExprTree, scope);
		List<ExpressionNode> tmp = new LinkedList<>();
		tmp.add(firstExpr);
		
		if (suffixTree != null) {
			int numSuffixTreeChildren = suffixTree.getChildCount();

			for (int i = 0; i < numSuffixTreeChildren; i++) {
				CommonTree commaSeparatedTermList = (CommonTree) suffixTree
						.getChild(i);

				if (commaSeparatedTermList != null) {
					SequenceNode<ExpressionNode> elements = translateCommaSeparatedList(
							commaSeparatedTermList, scope);

					for (ASTNode expr : elements.children()) {
						expr.remove();
						tmp.add((ExpressionNode) expr);
					}
				}
			}
		}
		return nodeFactory.newCurlyBracedTermSetNode(source, tmp);
	}

	/**
	 * Input tree: <code>
	 * ( expr | ^(COMMA_SEPARATED_LIST expr*) )
	 * </code>
	 */
	private SequenceNode<ExpressionNode> translateCommaSeparatedList(
			CommonTree tree, SimpleScope scope) throws ABCException {
		List<ExpressionNode> termList = new LinkedList<>();

		if (tree.getType() == COMMA_SEPARATED_LIST) {
			int numChildren = tree.getChildCount();

			for (int i = 0; i < numChildren; i++) {
				ExpressionNode term = translateExpression(
						(CommonTree) tree.getChild(i), scope);

				termList.add(term);
			}
		} else
			termList.add(translateExpression(tree, scope));
		return nodeFactory.newSequenceNode(newSource(tree),
				"comma separated term list", termList);
	}

	private ABCException error(String message, CommonTree tree) {
		if (tree != null)
			return parseTree.newSyntaxException(
					message + " at " + tree.getText(), tree);
		return tokenFactory.newUnsourcedException(message);
	}

	private Source newSource(CommonTree tree) {
		Source result = parseTree.source(tree);

		return result;
	}
}
