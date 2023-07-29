package edu.udel.cis.vsl.civl.transform.common;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import edu.udel.cis.vsl.abc.ast.IF.AST;
import edu.udel.cis.vsl.abc.ast.IF.ASTFactory;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode.NodeKind;
import edu.udel.cis.vsl.abc.ast.node.IF.NodeFactory;
import edu.udel.cis.vsl.abc.ast.node.IF.SequenceNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.FunctionDefinitionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.VariableDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.FunctionCallNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode.Operator;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.StringLiteralNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.BlockItemNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.DeclarationListNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.ForLoopInitializerNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.ForLoopNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.LoopNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.LoopNode.LoopKind;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.StatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.StatementNode.StatementKind;
import edu.udel.cis.vsl.abc.ast.type.IF.StandardBasicType.BasicTypeKind;
import edu.udel.cis.vsl.abc.front.IF.CivlcTokenConstant;
import edu.udel.cis.vsl.abc.token.IF.CivlcToken;
import edu.udel.cis.vsl.abc.token.IF.CivlcToken.TokenVocabulary;
import edu.udel.cis.vsl.abc.token.IF.Formation;
import edu.udel.cis.vsl.abc.token.IF.Source;
import edu.udel.cis.vsl.abc.token.IF.StringLiteral;
import edu.udel.cis.vsl.abc.token.IF.SyntaxException;

public class LoopContractTransformerWorker extends BaseWorker {
	/**
	 * A reference to {@link NodeFactory}
	 */
	private NodeFactory nodeFactory;

	/* ************************ Static fields ****************************** */

	/* *** Function identifiers *** */
	private final static String ASSUME_PUSH = "$assume_push";

	private final static String ASSUME_POP = "$assume_pop";

	private final static String WRITE_SET_PUSH = "$write_set_push";

	private final static String WRITE_SET_POP = "$write_set_pop";

	private final static String WRITE_SET_PEEK = "$write_set_peek";

	private final static String MEM_EMPTY = "$mem_empty";

	private final static String MEM_CONTAINS = "$mem_contains";

	private final static String MEM_UNION = "$mem_union";

	private final static String MEM_UNION_WIDENING = "$mem_union_widening";

	private final static String MEM_UNARY_WIDENING = "$mem_unary_widening";

	private final static String MEM_HAVOC = "$mem_havoc";

	private final static String MEM_ASSIGN_FROM = "$mem_assign_from";

	private final static String GET_FULL_STATE = "$get_full_state";

	/* *** Generated identifier prefixes: *** */
	/**
	 * Names for miscellaneous temporary variables that do not need to be passed
	 * among different methods.
	 */
	private final static String LOOP_TMP_VAR_PREFIX = "_loop_tmp";

	/* *** counters for generating unique identifiers *** */

	private int loopTmpCounter = 0;

	/* ************** primitives for error reporting **************** */

	/**
	 * The token used to construct a {@link StringLiteral} which is used to
	 * report assertion violation.
	 */
	private final CivlcToken loopInvariantsViolationMessageToken;

	/**
	 * The static string literal of loop invariants violation messages
	 */
	private static final String violationMessage = "\"loop invariants violation\"";

	/**
	 * The token used to construct a {@link StringLiteral} which is used to
	 * report frame condition violation.
	 */
	private final CivlcToken frameConditionViolationMessageToken;

	/**
	 * The static string literal of frame condition violation messages
	 */
	private static final String frameConditionViolationMessage = "\"loop assigns violation\"";

	/**
	 * The token used to construct a {@link StringLiteral} which is used to
	 * report assertion establish violation.
	 */
	private final CivlcToken loopInvariantsEstablishViolationMessageToken;

	/**
	 * The static string literal of loop invariants establish violation messages
	 */
	private static final String establishViolationMessage = "\"loop invariants establish violation\"";

	/* ******************** static methods ********************** */
	/**
	 * @param node
	 *            An {@link ASTNode}.
	 * @return true iff the given {@link ASTNode} represents a function
	 *         definition.
	 */
	static private boolean isFunctionDefinition(ASTNode node) {
		return node.nodeKind() == NodeKind.FUNCTION_DEFINITION;
	}

	/**
	 * @param node
	 *            An {@link ASTNode}.
	 * @return true iff the given {@link ASTNode} represents a loop.
	 */
	static private boolean isLoopNode(ASTNode node) {
		if (node.nodeKind() == NodeKind.STATEMENT)
			return ((StatementNode) node).statementKind() == StatementKind.LOOP;
		return false;
	}

	/**
	 * @param node
	 *            An {@link LoopNode}.
	 * @return true iff the given {@link LoopNode} represents a loop.
	 */
	static private boolean isContractedLoop(LoopNode loop) {
		return loop.loopContracts() != null
				&& loop.loopContracts().numChildren() > 0;
	}

	/**
	 * @param loop
	 *            An {@link LoopNode}.
	 * @return true iff the given {@link LoopNode} represents a for-loop.
	 */
	static private boolean isForLoop(LoopNode loop) {
		return loop.getKind() == LoopKind.FOR;
	}

	/* ******************* Constructor ********************** */

	/**
	 * @return A unique identifier name for miscellaneous variables which are
	 *         only used by within one Java method.
	 */
	private String nextLoopTmpIdentifier() {
		return LOOP_TMP_VAR_PREFIX + loopTmpCounter++;
	}

	// TODO: deal with jump statements
	// /**
	// * @return A unique identifier name for a label which helps transforming
	// * 'continue's
	// */
	// private String nextLabelIdentifier() {
	// return LOOP_LABEL + loopContinueCounter++;
	// }

	/* ******************* Constructor ********************** */

	public LoopContractTransformerWorker(String transformerName,
			ASTFactory astFactory) throws SyntaxException {
		super(transformerName, astFactory);
		this.nodeFactory = astFactory.getNodeFactory();
		Formation feedBackformation = tokenFactory
				.newTransformFormation(transformerName, "violation report");

		frameConditionViolationMessageToken = tokenFactory.newCivlcToken(
				CivlcTokenConstant.STRING_LITERAL,
				frameConditionViolationMessage, feedBackformation,
				TokenVocabulary.DUMMY);
		loopInvariantsViolationMessageToken = tokenFactory.newCivlcToken(
				CivlcTokenConstant.STRING_LITERAL, violationMessage,
				feedBackformation, TokenVocabulary.DUMMY);
		loopInvariantsEstablishViolationMessageToken = tokenFactory
				.newCivlcToken(CivlcTokenConstant.STRING_LITERAL,
						establishViolationMessage, feedBackformation,
						TokenVocabulary.DUMMY);
	}

	/* ******************* The only public interface ********************** */
	@Override
	public AST transform(AST ast) throws SyntaxException {
		SequenceNode<BlockItemNode> root = ast.getRootNode();
		boolean hasContractedLoop = false;

		ast.release();
		for (BlockItemNode block : root) {
			if (isFunctionDefinition(block)) {
				FunctionDefinitionNode funcDefi = (FunctionDefinitionNode) block;

				if (transformLoopInFunction(funcDefi.getBody()))
					hasContractedLoop = true;
			}
		}

		if (!hasContractedLoop)
			return astFactory.newAST(root, ast.getSourceFiles(),
					ast.isWholeProgram());;

		completeSources(root);
		ast = astFactory.newAST(root, ast.getSourceFiles(),
				ast.isWholeProgram());
		// ast.prettyPrint(System.out, true);
		return ast;
	}

	/* **************** Main transformation logic methods ****************** */
	/**
	 * Given a function body, transform all contracted loops in it into CIVL-C
	 * codes.
	 * 
	 * @param body
	 *            The root node of a sub-ASTree representing a function body
	 * @return true iff at least one loop in this function has been annotated
	 * @throws SyntaxException
	 */
	private boolean transformLoopInFunction(BlockItemNode body)
			throws SyntaxException {
		ASTNode node = body;
		LoopContractBlock annotatedLoop;
		ASTNode parent = body.parent();
		int bodyChildIdx = body.childIndex();
		boolean hasContractedLoop = false;

		// temporarily take off the body so that the DFS will only traverse the
		// body:
		body.remove();
		do {
			// transform nested function definitions:
			if (isFunctionDefinition(node)) {
				boolean innerHasContractedLoop = transformLoopInFunction(
						((FunctionDefinitionNode) node).getBody());

				if (innerHasContractedLoop)
					hasContractedLoop = true;
			}
			// transform loop:
			if (isLoopNode(node)) {
				LoopNode loop = (LoopNode) node;

				if (isContractedLoop(loop)) {
					// transform the loop
					annotatedLoop = new LoopContractBlock(loop);
					// skip the whole loop then continue. The loopNode will be
					// removed in transformLoopWorker method, so the Skip call
					// must happen before it.
					node = BaseWorker.nextDFSSkip(loop);
					transformLoopWorker(annotatedLoop);
					hasContractedLoop = true;
					continue;
				}
			}
			node = node.nextDFS();
		} while (node != null);
		parent.setChild(bodyChildIdx, body);
		return hasContractedLoop;
	}

	/**
	 * Transform a contracted loop including nested ones into pure CIVL-C codes.
	 * 
	 * @param loop
	 * @throws SyntaxException
	 */
	private void transformLoopWorker(LoopContractBlock loop)
			throws SyntaxException {
		// transform inner loops
		transformLoopInFunction(loop.getLoopNode().getBody());

		List<BlockItemNode> LISEComponents = new LinkedList<>(),
				newBodyComponents;
		ASTNode loopParent = loop.getLoopNode().parent();
		int childIdx = loop.getLoopNode().childIndex();
		String writeSetName = nextLoopTmpIdentifier();
		String preStateName = nextLoopTmpIdentifier();

		LISEComponents.addAll(
				transformLoopEntrance(loop, writeSetName, preStateName));
		// transforms loop body:
		newBodyComponents = transformLoopBody(loop, writeSetName, preStateName);
		// transforms the loop to a while(true|false) loop :
		LISEComponents.addAll(toWhileOrBranch(loop, newBodyComponents));

		Source source = loop.getLoopNode().getSource();
		BlockItemNode LISEBlock = nodeFactory.newCompoundStatementNode(source,
				LISEComponents);

		loop.getLoopNode().remove();
		loopParent.setChild(childIdx, LISEBlock);
	}

	/* **************** Loop transformation helper methods ****************** */
	/**
	 * <p>
	 * In general, before the loop, three things must be done: 1) establish the
	 * base case; 2) save a pre-loop state for havoc later:
	 * </p>
	 * <code>
	 * $assert(loop-inv);             // base case establish
	 * $state pre =$get_full_state(); // pre-loop state
	 * </code>
	 * <p>
	 * When the "loop assigns" are missing, write set must be accumulated, hence
	 * a $mem type variable is also needed to be declared before the loop:
	 * </p>
	 * <code>
	 * $mem ws = $mem_empty();
	 * </code>
	 * 
	 */
	private List<BlockItemNode> transformLoopEntrance(LoopContractBlock loop,
			String writeSetName, String preStateName) throws SyntaxException {
		List<BlockItemNode> results = new LinkedList<>();

		// loop initializer:
		if (isForLoop(loop.getLoopNode())) {
			ForLoopNode forLoop = (ForLoopNode) loop.getLoopNode();
			ForLoopInitializerNode initializer = forLoop.getInitializer();

			if (initializer != null) {
				initializer.remove();
				if (initializer instanceof ExpressionNode)
					results.add(nodeFactory.newExpressionStatementNode(
							(ExpressionNode) initializer));
				else {
					DeclarationListNode declList = (DeclarationListNode) initializer;

					for (VariableDeclarationNode decl : declList) {
						decl.remove();
						results.add(decl);
					}
				}
			}
		}
		// base case establishment
		results.add(createAssertion(loop.getLoopInvariants(nodeFactory), 0));
		// create a pre-state
		results.add(createPreState(preStateName));
		// declare a accumulated write set if the "loop assigns" is not given:
		if (loop.getLoopAssignSet().isEmpty()) {
			Source source = newSource("$mem ws",
					CivlcTokenConstant.DECLARATION);

			results.add(nodeFactory.newVariableDeclarationNode(source,
					identifier(writeSetName),
					nodeFactory.newMemTypeNode(source),
					nodeFactory.newFunctionCallNode(source,
							identifierExpression(MEM_EMPTY), Arrays.asList(),
							null)));
		}
		return results;
	}

	/**
	 * <p>
	 * the original loop body will be wrapped by $assume_push and $assume_pop
	 * and $write_set_push and $write_set_pop. In addition, after the original
	 * body, the frame condition and loop invariant preservation will be
	 * checked. Then modified objects will be refreshed.
	 * </p>
	 * 
	 * <p>
	 * If an object in a variable is modified by the loop, it will be refreshed.
	 * Non-modified part of the variable will remain its value as in the
	 * pre-loop state.
	 * </p>
	 * 
	 * <p>
	 * If "loop assigns" are missing, the modified objects must be added into
	 * the accumulated write set while no frame condition needs to check.
	 * </p>
	 * 
	 * <p>
	 * In general: <code>
	 * $mem_havoc(ws);                              // after arbitrary iterations
	 * $assume_push(loop-inv && loop-cond);
	 * $write_set_push();
	 * "original body"
	 * ws = $write_set_peek();
	 * $assert(loop-inv);                           // preservation
	 * $assert($mem_contains("loop assigns"), ws);  // check frame-condition
	 * $mem_havoc_with(preState, ws);               // refresh
	 * $write_set_pop();
	 * $assume_pop();
	 * </code>
	 * </p>
	 * <p>
	 * If "loop assigns" are missing, write set needs to be accumulated: <code>
	 * ...
	 * $mem tmp = $write_set_peek();
	 * $mem_union_widening(ws, tmp);
	 * ...
	 * </code>
	 * </p>
	 */
	private List<BlockItemNode> transformLoopBody(LoopContractBlock loop,
			String writeSetName, String preStateName) throws SyntaxException {
		List<BlockItemNode> results = new LinkedList<>();
		ExpressionNode inv = loop.getLoopInvariants(nodeFactory);
		ExpressionNode loopCond = loop.getLoopNode().getCondition();
		Source invAndCondSource = tokenFactory.join(inv.getSource(),
				loopCond.getSource());
		ExpressionNode invAndCond = nodeFactory.newOperatorNode(
				invAndCondSource, Operator.LAND, inv.copy(), loopCond.copy());
		String loopAssignsUnionName = nextLoopTmpIdentifier();

		// havoc, if loop assigns given ...
		if (!loop.getLoopAssignSet().isEmpty()) {
			results.addAll(unionLoopAssigns(loop.getLoopAssignSet(),
					loopAssignsUnionName));
			results.addAll(
					createMemHavoc(identifierExpression(loopAssignsUnionName)));
		}
		// assume_push ...
		results.add(createAssumptionPush(invAndCond));
		// write set push ...
		results.add(createWriteSetPush());

		// original body ...
		List<BlockItemNode> bodyAndIncrementor = new LinkedList<>();
		StatementNode originalBody = loop.getLoopNode().getBody();

		bodyAndIncrementor.add(originalBody);
		originalBody.remove();
		// for-loop incrementer ...
		if (isForLoop(loop.getLoopNode())) {
			ForLoopNode forLoop = (ForLoopNode) loop.getLoopNode();

			if (forLoop.getIncrementer() != null)
				bodyAndIncrementor.add(nodeFactory.newExpressionStatementNode(
						forLoop.getIncrementer().copy()));
		}
		// wrap the original body with a pair of curly braces so that loop-body
		// local variables won't be saved in the write set:
		results.add(nodeFactory.newCompoundStatementNode(
				originalBody.getSource(), bodyAndIncrementor));
		// assert (inv);
		results.add(createAssertion(inv.copy(), 2));

		String finalHavocWriteSetName;

		// check frame-condition and havoc:
		if (!loop.getLoopAssignSet().isEmpty()) {
			results.add(memTypeVariableDeclaration(writeSetName));
			results.addAll(createWriteSetPeek(writeSetName));
			results.addAll(
					checkFrameCondition(writeSetName, loopAssignsUnionName));
			results.add(backToPreState(
					identifierExpression(loopAssignsUnionName), preStateName));
			finalHavocWriteSetName = loopAssignsUnionName;
		} else {
			String tmpWsName = nextLoopTmpIdentifier();
			ExpressionNode memUnionWidening = identifierExpression(
					MEM_UNION_WIDENING);

			results.add(memTypeVariableDeclaration(tmpWsName));
			results.addAll(createWriteSetPeek(tmpWsName));
			// union & widening tmpWs with write set:
			results.add(createAssignment(identifierExpression(writeSetName),
					nodeFactory.newFunctionCallNode(
							memUnionWidening.getSource(), memUnionWidening,
							Arrays.asList(identifierExpression(tmpWsName),
									identifierExpression(writeSetName)),
							null)));
			// havoc the accumulated write set
			results.add(backToPreState(identifierExpression(writeSetName),
					preStateName));
			finalHavocWriteSetName = writeSetName;
		}
		// pop write set:
		results.add(createWriteSetPop());
		// $mem havoc
		results.addAll(
				createMemHavoc(identifierExpression(finalHavocWriteSetName)));
		// pop assume:
		results.add(createAssumptionPop());
		return results;
	}

	/**
	 * If "loop assigns" are given, the preservation step can be done by
	 * executing the body once: <code>
	 *   if ($choose_int(2)) {
	 *     "body";
	 *   }
	 *   $assume(inv && !loop-cond);
	 * </code> Else, the preservation step completes when the state converges,
	 * <code>
	 *   int tmp = 1;
	 *   while (tmp) {
	 *     "body";
	 *     tmp = $choose_int(2);
	 *   }
	 *   $assume(inv && !loop-cond);
	 * </code>
	 * 
	 * @throws SyntaxException
	 */
	private List<BlockItemNode> toWhileOrBranch(LoopContractBlock loop,
			List<BlockItemNode> body) throws SyntaxException {
		ExpressionNode bnd = createNDBinaryChoice();
		StatementNode compoundBody;
		List<BlockItemNode> results = new LinkedList<>();

		if (!loop.getLoopAssignSet().isEmpty()) {
			compoundBody = nodeFactory.newCompoundStatementNode(
					loop.getLoopNode().getSource(), body);
			results.add(
					nodeFactory.newIfNode(bnd.getSource(), bnd, compoundBody));
		} else {
			String bndInitName = nextLoopTmpIdentifier();
			BlockItemNode bndInit = nodeFactory.newVariableDeclarationNode(
					newSource("int " + bndInitName + " = 1",
							CivlcTokenConstant.DECLARATION),
					identifier(bndInitName), basicType(BasicTypeKind.INT),
					integerConstant(1));
			ExpressionNode bndIdent = identifierExpression(bndInitName);

			// put the ND choice at the end, so that it won't get side-effect
			// removed:
			body.add(createAssignment(bndIdent, bnd));
			compoundBody = nodeFactory.newCompoundStatementNode(
					loop.getLoopNode().getSource(), body);
			// initially, let control always enter the while loop:
			results.add(bndInit);
			results.add(nodeFactory.newWhileLoopNode(bndIdent.getSource(),
					bndIdent.copy(), compoundBody, null));
		}
		ExpressionNode negLoopCond = nodeFactory.newOperatorNode(
				loop.getLoopNode().getCondition().getSource(), Operator.NOT,
				loop.getLoopNode().getCondition().copy());
		ExpressionNode inv = loop.getLoopInvariants(nodeFactory);

		Source source = tokenFactory.join(inv.getSource(),
				negLoopCond.getSource());

		results.add(
				assumeNode(nodeFactory.newOperatorNode(source, Operator.LAND,
						loop.getLoopInvariants(nodeFactory), negLoopCond)));
		return results;
	}

	/* *********************** Utility methods ****************************** */
	/**
	 * <code>
	 * $mem_assigns_from(preState, $mem_unary_widening(m));  
	 * $mem_havoc(m); 
	 * </code>
	 * 
	 * <p>
	 * The purpose of this two statements is to swipe out modification
	 * footprints on objects left by the loop body. For example:
	 * 
	 * <code>
	 * // loop assigns a[x .. y];
	 * { // loop body
	 *    a[i] = j;
	 * }
	 * $havoc(&a[x .. y]);  
	 * </code>
	 * 
	 * Suppose array a initially has value A. After the loop body, it has value
	 * A[i := j]. Then $havoc does not necessarily know that i belongs to the
	 * range [x .. y] hence array a will have a value like
	 * <code>array-lambda int. k : x <= k <= y ? A'[k] : A[i: = j][k]</code>.
	 * But since the frame-condition is always checked. It guarantees i belongs
	 * to the range [x .. y]. We'd like to have the value of a be simpler <code>
	 * array-lambda int. k : x <= k <= y ? A'[k] : A[k]
	 * </code>.
	 * 
	 * The two statements returned by this method delivers such a desire.
	 * </p>
	 * 
	 */
	private BlockItemNode backToPreState(ExpressionNode m, String preState) {
		String funcName;
		List<ExpressionNode> args;
		ExpressionNode call;

		funcName = MEM_UNARY_WIDENING;
		args = Arrays.asList(m);
		call = nodeFactory.newFunctionCallNode(m.getSource(),
				identifierExpression(funcName), args, null);
		funcName = MEM_ASSIGN_FROM;
		args = Arrays.asList(identifierExpression(preState), call);
		call = nodeFactory.newFunctionCallNode(m.getSource(),
				identifierExpression(funcName), args, null);
		return nodeFactory.newExpressionStatementNode(call);
	}

	/**
	 * $mem_havoc(m);
	 */
	private List<BlockItemNode> createMemHavoc(ExpressionNode m) {
		List<BlockItemNode> results = new LinkedList<>();
		String funcName;
		List<ExpressionNode> args;
		ExpressionNode call;

		funcName = MEM_HAVOC;
		args = Arrays.asList(m.copy());
		call = nodeFactory.newFunctionCallNode(m.getSource(),
				identifierExpression(funcName), args, null);
		results.add(nodeFactory.newExpressionStatementNode(call));
		return results;
	}

	private BlockItemNode memTypeVariableDeclaration(String name) {
		Source source = newSource("$mem " + name,
				CivlcTokenConstant.DECLARATION);

		return nodeFactory.newVariableDeclarationNode(source, identifier(name),
				nodeFactory.newMemTypeNode(source));
	}

	private StatementNode createAssignment(ExpressionNode lhs,
			ExpressionNode rhs) {
		Source source = newSource(
				lhs.prettyRepresentation() + " = " + rhs.prettyRepresentation(),
				CivlcTokenConstant.ASSIGN);

		return nodeFactory.newExpressionStatementNode(
				nodeFactory.newOperatorNode(source, Operator.ASSIGN, lhs, rhs));
	}

	/**
	 * <code>
	 * $assert($mem_contains(loopAssignsUnionName, write_set));
	 * </code>
	 * 
	 * @throws SyntaxException
	 * 
	 */
	private List<BlockItemNode> checkFrameCondition(String writeSetName,
			String loopAssignsUnionName) throws SyntaxException {
		Source mcSource = newSource(MEM_CONTAINS, CivlcTokenConstant.CALL);
		List<BlockItemNode> results = new LinkedList<>();
		// assert
		ExpressionNode call = nodeFactory.newFunctionCallNode(mcSource,
				identifierExpression(MEM_CONTAINS),
				Arrays.asList(identifierExpression(loopAssignsUnionName),
						identifierExpression(writeSetName)),
				null);

		results.add(createAssertion(call, 1));
		return results;
	}

	/**
	 * <code>
	 * $mem loopAssignsUnionName = $mem_union(loopAssigns<sub>0</sub>, 
	 *                                        loopAssigns<sub>1</sub>, 
	 *                                        ...,
	 *                                        loopAssigns<sub>n-1</sub>);
	 * </code>
	 */
	private List<BlockItemNode> unionLoopAssigns(
			List<ExpressionNode> loopAssigns, String loopAssignsUnionName) {
		Source muSource = newSource(MEM_UNION, CivlcTokenConstant.CALL);
		Source meSource = newSource(MEM_EMPTY, CivlcTokenConstant.CALL);
		List<BlockItemNode> results = new LinkedList<>();

		// temp var "union"
		results.add(memTypeVariableDeclaration(loopAssignsUnionName));
		results.add(createAssignment(identifierExpression(loopAssignsUnionName),
				nodeFactory.newFunctionCallNode(meSource,
						identifierExpression(MEM_EMPTY), Arrays.asList(),
						null)));

		// executes union:
		for (ExpressionNode loopAssignsArg : loopAssigns) {
			ExpressionNode addrOf = nodeFactory.newOperatorNode(
					loopAssignsArg.getSource(), Operator.ADDRESSOF,
					loopAssignsArg.copy());
			ExpressionNode unionCall = nodeFactory.newFunctionCallNode(muSource,
					identifierExpression(MEM_UNION),
					Arrays.asList(identifierExpression(loopAssignsUnionName),
							addrOf),
					null);

			results.add(createAssignment(
					identifierExpression(loopAssignsUnionName), unionCall));
		}
		return results;
	}

	private BlockItemNode createPreState(String preStateName) {
		Source srcDecl = newSource("$state " + preStateName,
				CivlcTokenConstant.DECLARATION);
		Source srcGetFullState = newSource(GET_FULL_STATE,
				CivlcTokenConstant.CALL);
		ExpressionNode getFullState = nodeFactory.newFunctionCallNode(
				srcGetFullState, identifierExpression(GET_FULL_STATE),
				Arrays.asList(), null);
		BlockItemNode decl = nodeFactory.newVariableDeclarationNode(srcDecl,
				identifier(preStateName), nodeFactory.newStateTypeNode(srcDecl),
				getFullState);

		return decl;
	}

	/**
	 * Creates an assertion function call with an argument "predicate".
	 * 
	 * @param predicate
	 *            The {@link ExpressionNode} which represents a predicate. It is
	 *            the only argument of an assertion call.
	 * @param kind
	 *            an integer indicating the kind of error message, there are 3
	 *            options: 0: establishment violation; 1: frame-condition
	 *            violation; 2: loop invariant preservation violation
	 * @return A created assert call statement node;
	 * @throws SyntaxException
	 */
	private StatementNode createAssertion(ExpressionNode predicate, int kind)
			throws SyntaxException {
		ExpressionNode assertIdentifier = identifierExpression(
				BaseWorker.ASSERT);
		Source source = newSource("$assert", CivlcTokenConstant.CALL);
		String errMsg = kind == 0
				? establishViolationMessage
				: kind == 1 ? frameConditionViolationMessage : violationMessage;
		CivlcToken errMsgToken = kind == 0
				? loopInvariantsEstablishViolationMessageToken
				: kind == 1
						? frameConditionViolationMessageToken
						: loopInvariantsViolationMessageToken;

		StringLiteralNode messageNode = nodeFactory.newStringLiteralNode(source,
				errMsg, astFactory.getTokenFactory().newStringToken(errMsgToken)
						.getStringLiteral());
		FunctionCallNode assertCall = nodeFactory.newFunctionCallNode(source,
				assertIdentifier, Arrays.asList(predicate.copy(), messageNode),
				null);

		return nodeFactory.newExpressionStatementNode(assertCall);
	}

	/**
	 * Creates an assume_push function call with an argument "predicate".
	 * 
	 * @param predicate
	 *            The {@link ExpressionNode} which represents a predicate. It is
	 *            the only argument of an assumption call.
	 * @return A created assumption call statement node;
	 */
	private StatementNode createAssumptionPush(ExpressionNode predicate) {
		ExpressionNode assumeIdentifier = identifierExpression(ASSUME_PUSH);
		FunctionCallNode assumeCall = nodeFactory.newFunctionCallNode(
				predicate.getSource(), assumeIdentifier,
				Arrays.asList(predicate.copy()), null);

		return nodeFactory.newExpressionStatementNode(assumeCall);
	}

	/**
	 * Creates an assume_pop function call.
	 * 
	 * @return A created assumption call statement node;
	 */
	private StatementNode createAssumptionPop() {
		Source source = newSource(ASSUME_POP, CivlcTokenConstant.CALL);
		ExpressionNode assumeIdentifier = identifierExpression(ASSUME_POP);

		FunctionCallNode assumeCall = nodeFactory.newFunctionCallNode(source,
				assumeIdentifier, Arrays.asList(), null);
		return nodeFactory.newExpressionStatementNode(assumeCall);
	}

	/**
	 * 
	 * @return a <code>$write_set_push()</code> statement node;
	 */
	private StatementNode createWriteSetPush() {
		Source source = this.newSource("$write_set_push",
				CivlcTokenConstant.CALL);
		ExpressionNode wsPushIdentifier = identifierExpression(WRITE_SET_PUSH);
		FunctionCallNode wsPushCall = nodeFactory.newFunctionCallNode(source,
				wsPushIdentifier, Arrays.asList(), null);
		return nodeFactory.newExpressionStatementNode(wsPushCall);
	}

	/**
	 * <code>
	 * $mem writeSetName = $write_set_peek();
	 * </code>
	 */
	private List<BlockItemNode> createWriteSetPeek(String writeSetName) {
		Source writeSetPop = newSource(WRITE_SET_PEEK, CivlcTokenConstant.CALL);
		Source writeSetAssign = newSource(
				writeSetName + "= " + WRITE_SET_PEEK + ";",
				CivlcTokenConstant.ASSIGN);
		List<BlockItemNode> results = new LinkedList<>();

		results.add(nodeFactory.newExpressionStatementNode(
				nodeFactory.newOperatorNode(writeSetAssign, Operator.ASSIGN,
						identifierExpression(writeSetName),
						nodeFactory.newFunctionCallNode(writeSetPop,
								identifierExpression(WRITE_SET_PEEK),
								Arrays.asList(), null))));
		return results;
	}

	/**
	 * <code>
	 * $write_set_pop();
	 * </code>
	 */
	private BlockItemNode createWriteSetPop() {
		Source writeSetPop = newSource(WRITE_SET_POP, CivlcTokenConstant.CALL);

		return nodeFactory.newExpressionStatementNode(
				nodeFactory.newFunctionCallNode(writeSetPop,
						identifierExpression(WRITE_SET_POP), Arrays.asList(),
						null));
	}

	/**
	 * @return <code>$choose_int(2)</code> call.
	 * @throws SyntaxException
	 */
	private ExpressionNode createNDBinaryChoice() throws SyntaxException {
		Source source = newSource("$choose_int(2)", CivlcTokenConstant.CALL);
		return nodeFactory.newFunctionCallNode(source,
				identifierExpression(CHOOSE_INT),
				Arrays.asList(nodeFactory.newIntegerConstantNode(source, "2")),
				null);
	}
}
