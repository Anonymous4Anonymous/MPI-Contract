package edu.udel.cis.vsl.civl.transform.common;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.udel.cis.vsl.abc.ast.IF.AST;
import edu.udel.cis.vsl.abc.ast.IF.ASTFactory;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode.NodeKind;
import edu.udel.cis.vsl.abc.ast.node.IF.IdentifierNode;
import edu.udel.cis.vsl.abc.ast.node.IF.SequenceNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.FunctionDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.FunctionDefinitionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.OrdinaryDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.OrdinaryDeclarationNode.OrdinaryDeclarationKind;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.TypedefDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.VariableDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode.ExpressionKind;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.FunctionCallNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.IdentifierExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode.Operator;
import edu.udel.cis.vsl.abc.ast.node.IF.label.LabelNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.BlockItemNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.BlockItemNode.BlockItemKind;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.CompoundStatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.IfNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.LoopNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.LoopNode.LoopKind;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.StatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.StatementNode.StatementKind;
import edu.udel.cis.vsl.abc.ast.node.IF.type.StructureOrUnionTypeNode;
import edu.udel.cis.vsl.abc.ast.type.IF.StandardBasicType.BasicTypeKind;
import edu.udel.cis.vsl.abc.ast.type.IF.Type;
import edu.udel.cis.vsl.abc.token.IF.Source;
import edu.udel.cis.vsl.abc.token.IF.SyntaxException;

// TODO: it is suppose to be combined with side-effect remover but it reuses on
// many methods in the BaseWork, so I make it separate from the side-effect 
// remover.
/**
 * <p>
 * <strong>Pre-condition:</strong> This transformer should be applied after the
 * side-effect remover, so that non-error side effects are gone already.
 * </p>
 * <p>
 * This class is a program transformer for getting rid of short circuit (SC)
 * evaluations. i.e. If the evaluation of the first operand suffices to
 * determine the result of the operation, the second operand will not evaluate.
 * </p>
 *
 * <p>
 * However, there is no difference between the short circuit evaluation and the
 * evaluate-both-always evaluation IF there is no side-effects in the second
 * operand. CIVL prefers the latter one because short circuit evaluation may
 * exacerbate path explosion problem.
 * </p>
 * 
 * <p>
 * Thus, this transformer is responsible to transform away the following three
 * operators: logical AND, logical OR and logical IMPLIES when necessary, i.e
 * there is side effects in the second operand.
 * </p>
 *
 * <p>
 * The basic idea is:<code>a AND b</code> is transformed to <code>
 * _Bool _tmp_ = a;
 * 
 * if (_tmp_) _tmp_ = b;
 * </code>. <br>
 * <code>a OR b</code> is transformed to <code>
 * _Bool _tmp_ = a;
 * 
 * if (!_tmp_) _tmp_ = b;
 * </code>. <br>
 * <code>a IMPLIES b <==> !a OR b</code> is transformed to <code>
 * _Bool _tmp_ = !a;
 * 
 * if (!_tmp_) _tmp_ = b;
 * </code>.
 * </p>
 *
 * <p>
 * <strong>The algorithm:</strong> <code>
 * input : an expression e, a boolean variable v. 
 * output : a sequence of statements S.
 * 
 * transform (e, v) : S 
 *    requires: e is an expression, v has boolean type;
 *    ensures:  if e contains any short-circuit operator, S is non-empty;
 *              and after the execution of S, v will hold the value of e;
 *              otherwise S is empty;
 * {
 *   stmt-seq S;
 *   
 *   if e is short circuit operation "A op B" then {
 *     S += transformOperand(A, v);
 *     S += branch(op, v, transformOperand(B, v));  
 *   } else {
 *     stmt-seq S';
 *     var v';
 *            
 *     foreach(child : e) {
 *       stmt-seq S'' = transform(child, v');
 *       
 *       if (S'' is NOT empty) {
 *         S' += S'';
 *         S' += decl-v';
 *         S' += {v' = e[child/v'];} 
 *       }
 *     }
 *     if (S' is not empty) {
 *       S += S';
 *       S += {v = v';};
 *     }
 *   }
 *   return S;
 * }
 * 
 * transformOperand (operand, v) : S 
 *   requires: operand is a operand of a short-circuit operation. Both operand and v have boolean type;
 *   ensures:  S is non-empty; After execution of S, v holds the value of operand.
 * {
 *   stmt-seq S;
 *   
 *   S = transform(operand, v);
 *   if (S is empty) 
 *     S = {v = operand;}
 *   return S;
 * }
 *
 * </code>
 * </p>
 * 
 * @author xxxx
 *
 */
public class ShortCircuitTransformerWorker extends BaseWorker {

	static private final String SCTransformer_PREFIX = "_scc";

	static private final String HOLDER_PREFIX = SCTransformer_PREFIX + "_var";

	static private final String LABEL_PREFIX = SCTransformer_PREFIX + "_label";

	private int generatedVariableCounter = 0;

	private static String ASSUME_NAME = "$assume";

	private static String ASSUME_PUSH_NAME = "$assume_push";

	private static String CIVL_ASSERT_NAME = "$assert";

	private static String C_ASSERT_NAME = "assert";

	/**
	 * <p>
	 * This class is a short-circuit operation that will be transformed into a
	 * sequence of statements which contains no short circuit operations whose
	 * second operand ha side effects.
	 * </p>
	 * 
	 * @author xxxx
	 *
	 */
	private class ShortCircuitOperation {
		/**
		 * The original short circuit operation ON the old ASTree.
		 */
		ExpressionNode scOperationExpression;

		/**
		 * The {@link BlockItemNode} which represents the position the in
		 * program before where the transformed statements will be inserted.
		 */
		BlockItemNode scExpressionOwner;

		/**
		 * A list of transformed statements, execution of which delivers the
		 * evaluation (with short-circuit semantics) of the original expression.
		 * None of the node in this list is a part of the old ASTree.
		 */
		LinkedList<BlockItemNode> statements = null;

		/**
		 * The name of the identifier of a temporary variable which will hold
		 * the evaluation of the original expression after executing the
		 * transformed statements.
		 */
		String identifierName = null;

		ShortCircuitOperation(ExpressionNode expression,
				BlockItemNode location) {
			this.scOperationExpression = expression;
			this.scExpressionOwner = location;
		}

		/**
		 * Set the transformed statements and the identifier name of the
		 * variable which holds the evaluation.
		 * 
		 * @param statements
		 *            The transformed statements.
		 * @param identifierName
		 *            The identifier name of the holder.
		 */
		void complete(LinkedList<BlockItemNode> statements,
				String identifierName) {
			this.statements = statements;
			this.identifierName = identifierName;
		}

		/**
		 * 
		 * @return True iff the original short-circuit operation is a part of a
		 *         loop condition.
		 */
		boolean isInLoopCondition() {
			if (scExpressionOwner.blockItemKind() == BlockItemKind.STATEMENT) {
				return ((StatementNode) scExpressionOwner)
						.statementKind() == StatementKind.LOOP;
			}
			return false;
		}

		@Override
		public String toString() {
			return this.scOperationExpression.toString();
		}
	}

	/**
	 * @param oprt
	 *            An instance of a {@link Operator}
	 * @return True iff the given {@link Operator} is a short circuit operator :
	 *         logical AND, logical OR or IMPLIES.
	 */
	static private boolean isShortCircuitOperator(Operator oprt) {
		return oprt == Operator.LAND || oprt == Operator.LOR
				|| oprt == Operator.IMPLIES;
	}

	static private boolean isInErrorSEFreeContext(ExpressionNode expr) {
		return isBoundedExpression(expr) || isGuard(expr) || isAssumption(expr)
				|| isAssertion(expr);
	}

	/**
	 * @param expr
	 *            An instance of a {@link ExpressionNode}.
	 * @return True iff the given expression is either a quantified expression
	 *         (exist or forall) or a lambda expression (lambda or array
	 *         lambda).
	 */
	static private boolean isBoundedExpression(ExpressionNode expr) {
		ExpressionKind kind = expr.expressionKind();

		return kind == ExpressionKind.QUANTIFIED_EXPRESSION
				|| kind == ExpressionKind.ARRAY_LAMBDA
				|| kind == ExpressionKind.LAMBDA
				|| kind == ExpressionKind.ARRAY_LAMBDA;
	}

	/**
	 * @param expr
	 *            An instance of a {@link ExpressionNode}.
	 * @return True if and only if the expression is a part of a guard in the
	 *         program.
	 */
	static private boolean isGuard(ExpressionNode expr) {
		if (expr.parent().nodeKind() == NodeKind.STATEMENT) {
			StatementNode stmt = (StatementNode) expr.parent();

			return stmt.statementKind() == StatementKind.WHEN;
		}
		return false;
	}

	static private boolean isAssumption(ExpressionNode expr) {
		if (expr.expressionKind() == ExpressionKind.FUNCTION_CALL) {
			FunctionCallNode callNode = (FunctionCallNode) expr;
			ExpressionNode functionIdentifier = callNode.getFunction();

			if (functionIdentifier
					.expressionKind() == ExpressionKind.IDENTIFIER_EXPRESSION) {
				String name = ((IdentifierExpressionNode) functionIdentifier)
						.getIdentifier().name();

				return name.equals(ASSUME_NAME)
						|| name.equals(ASSUME_PUSH_NAME);
			}
		}
		return false;
	}

	static private boolean isAssertion(ExpressionNode expr) {
		if (expr.expressionKind() == ExpressionKind.FUNCTION_CALL) {
			FunctionCallNode callNode = (FunctionCallNode) expr;
			ExpressionNode functionIdentifier = callNode.getFunction();

			if (functionIdentifier
					.expressionKind() == ExpressionKind.IDENTIFIER_EXPRESSION) {
				String name = ((IdentifierExpressionNode) functionIdentifier)
						.getIdentifier().name();

				return name.equals(CIVL_ASSERT_NAME)
						|| name.equals(C_ASSERT_NAME);
			}
		}
		return false;
	}

	/**
	 * @return The next unique name for an artificial variable.
	 */
	private String nextHolderName() {
		return HOLDER_PREFIX + generatedVariableCounter++;
	}

	/**
	 * @return The next unique name for an artificial label.
	 */
	private String nextLabelName() {
		return LABEL_PREFIX + generatedVariableCounter++;
	}

	public ShortCircuitTransformerWorker(String transformerName,
			ASTFactory astFactory) {
		super(transformerName, astFactory);
	}

	/* ***************** The public interface ******************* */
	@Override
	public AST transform(AST ast) throws SyntaxException {
		List<ShortCircuitOperation> scOperations = new LinkedList<>();
		SequenceNode<BlockItemNode> rootNode = ast.getRootNode();

		ast.release();
		// Find out all expressions containing short-circuit operators:
		for (BlockItemNode subTree : rootNode)
			scOperations.addAll(searchSCExpressionInSubTree(subTree));
		// Generating transformed statements to deliver the short-circuit
		// evaluation:
		for (ShortCircuitOperation remover : scOperations)
			transformShortCircuitExpression(remover);

		// Special transformation for loop condition:
		// a map caches all loops whose conditions are transformed already:
		Map<BlockItemNode, BlockItemNode> seenLoops = new HashMap<>();

		for (ShortCircuitOperation remover : scOperations)
			if (remover.isInLoopCondition())
				transformShortCircuitLoopCondition(remover, seenLoops);

		// Inserts transformed statements and replaces expressions with
		// temporary variables:
		for (ShortCircuitOperation remover : scOperations)
			mountTransformedSCOperation(remover);
		ast = astFactory.newAST(rootNode, ast.getSourceFiles(),
				ast.isWholeProgram());
		// ast.prettyPrint(System.out, true);
		return ast;
	}

	/**
	 * <p>
	 * Put the transformed statements of the given short circuit operation in
	 * the appropriate position in the ASTree.
	 * </p>
	 * 
	 * @param scOperation
	 *            An instance of {@link ShortCircuitOperation}
	 */
	void mountTransformedSCOperation(ShortCircuitOperation scOperation) {
		BlockItemNode location = scOperation.scExpressionOwner;
		ExpressionNode holderExpression = identifierExpression(
				scOperation.identifierName);
		ExpressionNode originalExpression = scOperation.scOperationExpression;
		ASTNode locationParent = location.parent();
		int locationChildIdx = location.childIndex();

		if (locationParent.nodeKind() == NodeKind.SEQUENCE) {
			@SuppressWarnings("unchecked")
			SequenceNode<BlockItemNode> seqNode = (SequenceNode<BlockItemNode>) locationParent;

			seqNode.insertChildren(locationChildIdx, scOperation.statements);
		} else if (locationParent instanceof CompoundStatementNode) {
			CompoundStatementNode compound = (CompoundStatementNode) locationParent;

			compound.insertChildren(locationChildIdx, scOperation.statements);
		} else {
			StatementNode locationReplacer;

			location.remove();
			scOperation.statements.add(location);
			locationReplacer = nodeFactory.newCompoundStatementNode(
					location.getSource(), scOperation.statements);
			locationParent.setChild(locationChildIdx, locationReplacer);
		}
		ASTNode oriExprParent = originalExpression.parent();
		int oriExprChildIdx = originalExpression.childIndex();

		originalExpression.remove();
		oriExprParent.setChild(oriExprChildIdx, holderExpression);
	}

	/* *********** Short-circuit expression searching methods **************/
	/**
	 * <p>
	 * Recursively searching the "biggest" short circuit operations, in a given
	 * sub-tree of the ASTree. Here "biggest" means, in terms of a tree
	 * structure, any returned short circuit operation is NOT a sub-tree of
	 * another short circuit operation. (Of course it can have short circuit
	 * operations as its descendant trees.)
	 * </p>
	 * 
	 * @param subTree
	 *            An instance of {@link BlockItemNode}, a sub-tree of the
	 *            ASTree.
	 * @return A list of short-circuit operations that needs to be transformed.
	 */
	private List<ShortCircuitOperation> searchSCExpressionInSubTree(
			BlockItemNode subTree) {
		if (subTree == null)
			return Arrays.asList();

		BlockItemKind kind = subTree.blockItemKind();
		List<ShortCircuitOperation> SCRemovers = new LinkedList<>();

		switch (kind) {
			case STATEMENT :
				searchSCExpressionInSubTreeWorker(subTree, subTree, SCRemovers);
				break;
			case STRUCT_OR_UNION :
				StructureOrUnionTypeNode typeNode = (StructureOrUnionTypeNode) subTree;

				searchSCExpressionInSubTreeWorker(typeNode, subTree,
						SCRemovers);
				break;
			case TYPEDEF :
				TypedefDeclarationNode typedefNode = (TypedefDeclarationNode) subTree;

				searchSCExpressionInSubTreeWorker(typedefNode.getTypeNode(),
						subTree, SCRemovers);
				break;
			case ORDINARY_DECLARATION :
				OrdinaryDeclarationNode declNode = (OrdinaryDeclarationNode) subTree;

				searchSCExpressionInSubTreeWorker(declNode.getTypeNode(),
						subTree, SCRemovers);
				if (declNode
						.ordinaryDeclarationKind() == OrdinaryDeclarationKind.FUNCTION_DEFINITION) {
					FunctionDefinitionNode funcDefiNode = (FunctionDefinitionNode) declNode;

					// No need to look at formal parameters because they will be
					// treated as if expressions were replaced by * (C11,
					// 6.7.6.2, semantics 5):
					searchSCExpressionInSubTreeWorker(funcDefiNode.getBody(),
							subTree, SCRemovers);
				}
				break;
			case PRAGMA :
				// TODO: when are pragma nodes translated away ?
				// TODO: following kinds of block item nodes haven't been
				// carefullt considered.
			case STATIC_ASSERTION :
				// no-op
			case ENUMERATION :
				// no-op
			case OMP_DECLARATIVE :
				// no-op
			default :
				break;
		}
		return SCRemovers;
	}

	/**
	 * <p>
	 * {@linkplain #searchSCExpressionInSubTree(BlockItemNode)}
	 * </p>
	 * 
	 * @param subTree
	 *            The root node of a sub ASTree.
	 * @param location
	 *            A {@link BlockItemNode} represents the current program
	 *            location
	 * @param output
	 *            The output collection of {@link ShortCircuitOperation}s.
	 */
	private void searchSCExpressionInSubTreeWorker(ASTNode subTree,
			BlockItemNode location, List<ShortCircuitOperation> output) {
		// no short-circut transformation for logic function definitions:
		if (location instanceof FunctionDeclarationNode)
			if (((FunctionDeclarationNode) location).isLogicFunction())
				return;

		for (ASTNode child : subTree.children()) {
			if (child == null)
				continue;
			if (child.nodeKind() == NodeKind.STATEMENT)
				searchSCExpressionInSubTreeWorker(child, (StatementNode) child,
						output);
			else if (child.nodeKind() == NodeKind.EXPRESSION) {
				if (!isInErrorSEFreeContext((ExpressionNode) child))
					searchSCInExpression((ExpressionNode) child, location,
							output);
			} else {
				if (child instanceof BlockItemNode) {
					searchSCExpressionInSubTreeWorker(child,
							(BlockItemNode) child, output);
				} else
					searchSCExpressionInSubTreeWorker(child, location, output);
			}
		}
	}

	/**
	 * <p>
	 * BFSearch the first encountered,and should be transformed, short-circuit
	 * operation, in the given expression. Add it into the output collection if
	 * it exists.
	 * </p>
	 * 
	 * @param expression
	 *            The expression that will be searched.
	 * @param location
	 *            The program location where the expression belongs to.
	 * @param output
	 *            The output collection of {@link ShortCircuitOperation}s.
	 */
	private void searchSCInExpression(ExpressionNode expression,
			BlockItemNode location, List<ShortCircuitOperation> output) {
		// Cannot transform quantified expressions:
		if (isInErrorSEFreeContext(expression))
			return;

		if (expression.expressionKind() == ExpressionKind.OPERATOR) {
			OperatorNode oprtNode = (OperatorNode) expression;

			if (isShortCircuitOperator(oprtNode.getOperator())) {
				// Optimization: for a short-circuit operation 'arg0 op arg1',
				// if arg1 has no error side-effect, leave it there.
				if (hasErrorSideEffectApprox(oprtNode.getArgument(1))) {
					output.add(new ShortCircuitOperation(expression, location));
					// Never search sub-expressions of a saved operation
					return;
				} else {
					// If the right-hand side operand is error side-effect free,
					// keep searching in the left-hand side operand:
					expression = oprtNode.getArgument(0);
				}
			}
		}
		searchSCExpressionInSubTreeWorker(expression, location, output);
	}

	/* ******* Short circuit in loop conditions transformation method *********/
	/**
	 * <p>
	 * The transformed statements for short-circuit expressions in loop
	 * conditions are required be placed in appropriate locations so that it
	 * will be executed in each iteration. This method will transform a loop
	 * (referred by a short circuit operation) into another form so that
	 * eventually after the short-circuit transformation, it conforms the
	 * aforementioned requirements.
	 * </p>
	 * 
	 * <p>
	 * See:<br>
	 * {@link #transformConditionFirstLoop(LoopNode, ShortCircuitOperation)}<br>
	 * {@link #transformBodyFirstLoop(LoopNode, ShortCircuitOperation)}
	 * </p>
	 * 
	 * @param scOp
	 *            An instance of {@link ShortCircuitOperation}.
	 * @param seenLoops
	 *            A cache for transformed loops. A loop referred by multiple
	 *            short circuit operations shall only be transformed once.
	 */
	private void transformShortCircuitLoopCondition(ShortCircuitOperation scOp,
			Map<BlockItemNode, BlockItemNode> seenLoops) {
		LoopNode loop = (LoopNode) scOp.scExpressionOwner;
		BlockItemNode newOwner;

		if (seenLoops.containsKey(loop)) {
			newOwner = seenLoops.get(loop);

			scOp.scExpressionOwner = newOwner;
			return;
		}
		if (loop.getKind() != LoopKind.DO_WHILE)
			newOwner = transformConditionFirstLoop(loop, scOp);
		else
			newOwner = transformBodyFirstLoop(loop, scOp);
		seenLoops.put(loop, newOwner);
	}

	/**
	 * <p>
	 * For "for" and "while" loops, the transformation follows such an idea:
	 * <code>Before: 
	 * loop ( condition; increment ) stmt
	 * </code>
	 * 
	 * <code>After:
	 * loop ( true ) {
	 *   if (condition) {stmt increment}
	 *   else break;
	 * }
	 * </code> <br>
	 * After the transformation, the short circuit expression is a part of a
	 * branch condition instead of a loop condition.
	 * </p>
	 * 
	 * @param loop
	 *            The loop whose condition is referred by a
	 *            {@link ShortCircuitOperation}.
	 * @param scOp
	 *            An instance of {@link ShortCircuitOperation}.
	 * @return The if-else branch node that the short circuit operation refers
	 *         to.
	 */
	private BlockItemNode transformConditionFirstLoop(LoopNode loop,
			ShortCircuitOperation scOp) {
		ExpressionNode loopCondition = loop.getCondition();
		StatementNode body = loop.getBody();
		StatementNode ifElseNode;

		loopCondition.remove();
		body.remove();
		ifElseNode = nodeFactory.newIfNode(loopCondition.getSource(),
				loopCondition, body,
				nodeFactory.newBreakNode(loop.getSource()));
		loop.setBody(ifElseNode);
		loop.setCondition(nodeFactory
				.newBooleanConstantNode(loopCondition.getSource(), true));
		scOp.scExpressionOwner = ifElseNode;
		return ifElseNode;
	}

	/**
	 * <p>
	 * For "do-while" loops, the transformation follows such an idea:
	 * <code>Before: 
	 * do stmt while (cond);
	 * </code>
	 * 
	 * <code>After:
	 * goto L;
	 * while (true) 
	 *    if (cond) 
	 *     L: stmt
	 *    else break;  
	 * </code> <br>
	 * After the transformation, the short circuit expression is a part of a
	 * branch condition instead of a loop condition. Note that here we don't
	 * unroll the first iteration of the body in case there is any loop jumpers
	 * inside the body.
	 * </p>
	 * 
	 * @param loop
	 *            The loop whose condition is referred by a
	 *            {@link ShortCircuitOperation}.
	 * @param scOp
	 *            An instance of {@link ShortCircuitOperation}.
	 * @return The if-else branch node that the short circuit operation refers
	 *         to.
	 */
	private BlockItemNode transformBodyFirstLoop(LoopNode loop,
			ShortCircuitOperation remover) {
		StatementNode body = loop.getBody();
		ExpressionNode loopCondition = loop.getCondition();
		StatementNode ifElseNode;
		StatementNode skipConditionEvaluation;
		StatementNode newBlock;
		StatementNode newLoop;
		Source source = loop.getSource();
		IdentifierNode labelName = identifier(nextLabelName());
		LabelNode label;

		loopCondition.remove();
		body.remove();
		label = nodeFactory.newStandardLabelDeclarationNode(source, labelName,
				body);
		body = nodeFactory.newLabeledStatementNode(source, label, body);
		ifElseNode = nodeFactory.newIfNode(loopCondition.getSource(),
				loopCondition, body, nodeFactory.newBreakNode(source));
		skipConditionEvaluation = nodeFactory.newGotoNode(source,
				labelName.copy());
		newLoop = nodeFactory.newWhileLoopNode(source,
				nodeFactory.newBooleanConstantNode(source, true), ifElseNode,
				null);
		remover.scExpressionOwner = ifElseNode;

		ASTNode parent = loop.parent();
		int loopIdx = loop.childIndex();

		loop.remove();
		newBlock = nodeFactory.newCompoundStatementNode(source,
				Arrays.asList(skipConditionEvaluation, newLoop));
		parent.setChild(loopIdx, newBlock);
		return ifElseNode;
	}

	/* **************** Short circuit transformation methods ******************/
	/**
	 * <p>
	 * Transform a short-circuit operation into a sequence of statements which
	 * deliver the evaluation of it.
	 * 
	 * @param scOp
	 *            An instance of {@link ShortCircuitOperation}
	 */
	private void transformShortCircuitExpression(ShortCircuitOperation scOp) {
		String holderName = nextHolderName();
		LinkedList<BlockItemNode> transfromStatements = new LinkedList<>();
		VariableDeclarationNode holderDecl = nodeFactory
				.newVariableDeclarationNode(
						scOp.scOperationExpression.getSource(),
						identifier(holderName), basicType(BasicTypeKind.BOOL));
		List<BlockItemNode> evaluationStatements = transformShortCircuitExpressionWorker(
				scOp.scOperationExpression, holderName);

		transfromStatements.add(holderDecl);
		transfromStatements.addAll(evaluationStatements);
		scOp.complete(transfromStatements, holderName);
	}

	/**
	 * 
	 * {@linkplain #transformShortCircuitExpression(ShortCircuitOperation)}.
	 * 
	 * @param expression
	 *            An expression in the short circuit operation.
	 * @param holderName
	 *            The identifier name of the artificial variable which evetually
	 *            will hold the evaluation of the short circuit operation.
	 * @return A sequence of the transformed statements.
	 */
	private List<BlockItemNode> transformShortCircuitExpressionWorker(
			ExpressionNode expression, String holderName) {
		if (isBoundedExpression(expression))
			return Arrays.asList();
		if (expression.expressionKind() == ExpressionKind.OPERATOR) {
			OperatorNode oprtNode = (OperatorNode) expression;

			if (isShortCircuitOperator(oprtNode.getOperator())) {
				ExpressionNode left = oprtNode.getArgument(0);
				ExpressionNode right = oprtNode.getArgument(1);
				Source source = oprtNode.getSource();

				if (oprtNode.getOperator() == Operator.LAND)
					return transformShortCircuitExpressionWorker_LAND(left,
							right, holderName, source);
				else if (oprtNode.getOperator() == Operator.LOR)
					return transformShortCircuitExpressionWorker_LOR(left,
							right, holderName, source);
				else
					return transformShortCircuitExpressionWorker_IMPLIES(left,
							right, holderName, source);
			}
		}

		// If the expression is not a short circuit expression, a new artificial
		// variable is needed to hold the evaluation of it.
		Source source = expression.getSource();
		String subHolderName = nextHolderName();
		List<BlockItemNode> result = new LinkedList<>();

		for (ASTNode child : expression.children())
			if (child != null && child.nodeKind() == NodeKind.EXPRESSION) {
				List<BlockItemNode> subChildResult = transformShortCircuitExpressionWorker(
						(ExpressionNode) child, subHolderName);
				Type type = ((ExpressionNode) child).getType();

				if (!subChildResult.isEmpty()) {
					VariableDeclarationNode subHolderDecl = nodeFactory
							.newVariableDeclarationNode(source,
									identifier(subHolderName), typeNode(type));
					ExpressionNode newExpression = expression.copy();
					StatementNode assignHolder = nodeFactory
							.newExpressionStatementNode(nodeFactory
									.newOperatorNode(source, Operator.ASSIGN,
											Arrays.asList(
													identifierExpression(
															subHolderName),
													newExpression)));
					int childIdx = child.childIndex();

					// replace child with subHolder:
					newExpression.setChild(childIdx,
							identifierExpression(subHolderName));
					result.add(subHolderDecl);
					result.addAll(subChildResult);
					result.add(assignHolder);
				}
			}
		if (!result.isEmpty())
			result.add(nodeFactory.newExpressionStatementNode(
					nodeFactory.newOperatorNode(source, Operator.ASSIGN,
							Arrays.asList(identifierExpression(holderName),
									identifierExpression(subHolderName)))));
		return result;
	}

	/**
	 * <p>
	 * Transform a logical AND expression <code>A && B</code> to <code>
	 * tmp = A;
	 * if (tmp)
	 *    tmp = B;
	 * </code>
	 * </p>
	 * 
	 * @param left
	 *            The left operand of the LAND operation
	 * @param right
	 *            The right operand of the LAND operation
	 * @param holderName
	 *            The identifier name of an artificial variable which will hold
	 *            the evaluation of the LAND operation
	 * @param source
	 *            The {@link Source} related to the LAND operation
	 * @return A sequence statements which deliver the evaluation of the LAND
	 *         operation.
	 */
	private List<BlockItemNode> transformShortCircuitExpressionWorker_LAND(
			ExpressionNode left, ExpressionNode right, String holderName,
			Source source) {
		List<BlockItemNode> results;
		ExpressionNode holderExpr = identifierExpression(holderName);
		StatementNode rightEvaluation;
		IfNode ifNode;

		results = transformSCLeftOperand(left, holderName);
		rightEvaluation = transformSCRightOperand(right, holderName);

		if (rightEvaluation == null) {
			// If there is no SC operator in right expression, holder =
			// rightExpression;
			ExpressionNode assign = nodeFactory.newOperatorNode(
					right.getSource(), Operator.ASSIGN,
					Arrays.asList(holderExpr, right.copy()));

			rightEvaluation = nodeFactory.newExpressionStatementNode(assign);
		}
		ifNode = nodeFactory.newIfNode(source, holderExpr.copy(),
				rightEvaluation);
		results.add(ifNode);
		return results;
	}

	/**
	 * <p>
	 * Transform a logical OR expression <code>A || B</code> to <code>
	 * tmp = A;
	 * if (!tmp)
	 *    tmp = B;
	 * </code>
	 * </p>
	 * 
	 * @param left
	 *            The left operand of the LOR operation
	 * @param right
	 *            The right operand of the LOR operation
	 * @param holderName
	 *            The identifier name of an artificial variable which will hold
	 *            the evaluation of the LOR operation
	 * @param source
	 *            The {@link Source} related to the LOR operation
	 * @return A sequence statements which deliver the evaluation of the LOR
	 *         operation.
	 */
	private List<BlockItemNode> transformShortCircuitExpressionWorker_LOR(
			ExpressionNode left, ExpressionNode right, String holderName,
			Source source) {
		List<BlockItemNode> result;
		StatementNode rightEvaluation;
		ExpressionNode holderExpr = identifierExpression(holderName);
		IfNode ifNode;

		result = transformSCLeftOperand(left, holderName);
		rightEvaluation = transformSCRightOperand(right, holderName);

		if (rightEvaluation == null) {
			// If there is no SC operator in right expression, holder =
			// rightExpression;
			ExpressionNode assign = nodeFactory.newOperatorNode(
					right.getSource(), Operator.ASSIGN,
					Arrays.asList(holderExpr, right.copy()));

			rightEvaluation = nodeFactory.newExpressionStatementNode(assign);
		}
		ifNode = nodeFactory.newIfNode(
				source, nodeFactory.newOperatorNode(left.getSource(),
						Operator.NOT, Arrays.asList(holderExpr.copy())),
				rightEvaluation);
		result.add(ifNode);
		return result;

	}

	/**
	 * <p>
	 * Transform a logical IMPLIES expression <code>A => B</code> to <code>
	 * tmp = !A;
	 * if (!tmp)
	 *    tmp = B;
	 * </code>
	 * </p>
	 * 
	 * @param left
	 *            The left operand of the IMPLIES operation
	 * @param right
	 *            The right operand of the IMPLIES operation
	 * @param holderName
	 *            The identifier name of an artificial variable which will hold
	 *            the evaluation of the IMPLIES operation
	 * @param source
	 *            The {@link Source} related to the IMPLIES operation
	 * @return A sequence statements which deliver the evaluation of the IMPLIES
	 *         operation.
	 */
	private List<BlockItemNode> transformShortCircuitExpressionWorker_IMPLIES(
			ExpressionNode left, ExpressionNode right, String holderName,
			Source source) {
		List<BlockItemNode> result;
		StatementNode rightEvaluation;
		ExpressionNode holderExpr = identifierExpression(holderName);
		ExpressionNode notHolder = nodeFactory.newOperatorNode(
				holderExpr.getSource(), Operator.NOT,
				Arrays.asList(holderExpr));
		IfNode ifNode;

		result = transformSCLeftOperand(left, holderName);
		rightEvaluation = transformSCRightOperand(right, holderName);

		result.add(nodeFactory.newExpressionStatementNode(
				nodeFactory.newOperatorNode(left.getSource(), Operator.ASSIGN,
						Arrays.asList(holderExpr.copy(), notHolder))));
		if (rightEvaluation == null) {
			// If there is no SC operator in right expression, holder =
			// rightExpression;
			ExpressionNode assign = nodeFactory.newOperatorNode(
					right.getSource(), Operator.ASSIGN,
					Arrays.asList(holderExpr.copy(), right.copy()));

			rightEvaluation = nodeFactory.newExpressionStatementNode(assign);
		}
		ifNode = nodeFactory.newIfNode(
				source, nodeFactory.newOperatorNode(left.getSource(),
						Operator.NOT, Arrays.asList(holderExpr.copy())),
				rightEvaluation);
		result.add(ifNode);
		return result;
	}

	/**
	 * <p>
	 * Transform the right operand of a short circuit operation. Wraps the
	 * translated statements with a pair of curly braces iff there are more than
	 * one statements. Return one {@link StatementNode}.
	 * </p>
	 * 
	 * @param operand
	 *            The right operand of a short circuit operation.
	 * @param holderName
	 *            The identifier name of an artificial variable which holds the
	 *            evaluation of the operand.
	 * @return A statement which delivers the evaluation of the operand.
	 */
	private StatementNode transformSCRightOperand(ExpressionNode operand,
			String holderName) {
		Source source = operand.getSource();
		List<BlockItemNode> result = transformShortCircuitExpressionWorker(
				operand, holderName);
		StatementNode evaluation;

		if (result.isEmpty())
			return null;
		else if (result.size() == 1)
			evaluation = (StatementNode) result.get(0);
		else {
			List<BlockItemNode> cast = new LinkedList<>();

			cast.addAll(result);
			evaluation = nodeFactory.newCompoundStatementNode(source, cast);
		}
		assert evaluation.blockItemKind() == BlockItemKind.STATEMENT;
		return evaluation;
	}

	/**
	 * <p>
	 * Transform a left operand of a short circuit operation. If the left
	 * operand contains no short circuit operations, assign it to the holder
	 * variable.
	 * </p>
	 * 
	 * @param operand
	 *            The left operand of a short circuit operation.
	 * @param holderName
	 *            The identifier name of an artificial variable which holds the
	 *            evaluation of the operand.
	 * @return A statement which delivers the evaluation of the operand.
	 */
	private List<BlockItemNode> transformSCLeftOperand(ExpressionNode operand,
			String holderName) {
		Source source = operand.getSource();
		List<BlockItemNode> results = new LinkedList<>();

		results.addAll(
				transformShortCircuitExpressionWorker(operand, holderName));
		if (results.isEmpty()) {
			ExpressionNode holder = this.identifierExpression(holderName);
			ExpressionNode assignment = nodeFactory.newOperatorNode(source,
					Operator.ASSIGN, Arrays.asList(holder, operand.copy()));
			StatementNode initHolder = nodeFactory
					.newExpressionStatementNode(assignment);

			results.add(initHolder);
		}
		return results;
	}

	/* **************** Error side-effect over-approximation ******************/
	/**
	 * <p>
	 * Over approximates if the given expression contains error side effects.
	 * </p>
	 * 
	 * @param expression
	 *            The expression will be tested if it contains error
	 *            side-effects
	 * @return True iff the given expression contains error side-effects.
	 */
	private boolean hasErrorSideEffectApprox(ExpressionNode expression) {
		return !expression.isSideEffectFree(true);
	}
}
