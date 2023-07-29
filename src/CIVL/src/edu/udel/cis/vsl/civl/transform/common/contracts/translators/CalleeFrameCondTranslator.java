package edu.udel.cis.vsl.civl.transform.common.contracts.translators;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode.NodeKind;
import edu.udel.cis.vsl.abc.ast.node.IF.IdentifierNode;
import edu.udel.cis.vsl.abc.ast.node.IF.NodeFactory;
import edu.udel.cis.vsl.abc.ast.node.IF.SequenceNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.CurlyBracedTermSetNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.VariableDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode.ExpressionKind;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.IdentifierExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode.Operator;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.BlockItemNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.StatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.TypeNode;
import edu.udel.cis.vsl.abc.ast.type.IF.StandardBasicType;
import edu.udel.cis.vsl.abc.ast.type.IF.StandardBasicType.BasicTypeKind;
import edu.udel.cis.vsl.abc.ast.type.IF.Type;
import edu.udel.cis.vsl.abc.token.IF.Source;
import edu.udel.cis.vsl.abc.token.IF.TokenFactory;
import edu.udel.cis.vsl.civl.model.IF.CIVLUnimplementedFeatureException;
import edu.udel.cis.vsl.civl.util.IF.Pair;

/**
 * This class translates assigns clauses for callee functions. In general, for a
 * memory location <code>e</code> listed in an assigns clause, we translate it
 * to <code>$havoc(&e)</code>. When <code>e</code> is a set-comprehension or an
 * <code>\mpi_seq</code> type expression, special handling is needed.
 * 
 * 
 * @author xxxx
 *
 */
public class CalleeFrameCondTranslator extends ContractTranslatorCommonBase {

	public CalleeFrameCondTranslator(NodeFactory nodeFactory,
			TokenFactory tokenFactory) {
		super(nodeFactory, tokenFactory);
	}

	public List<BlockItemNode> translateAssigns(List<ExpressionNode> conditions,
			List<ExpressionNode> assignsArgs) {
		List<BlockItemNode> results = new LinkedList<>();

		for (ExpressionNode assignsArg : assignsArgs)
			results.addAll(translateMemoryLocation(assignsArg));
		if (!conditions.isEmpty()) {
			ExpressionNode theCond = conjunct(conditions);
			Source source = theCond.getSource();
			List<BlockItemNode> newResults = new LinkedList<>();

			newResults.add(nodeFactory.newIfNode(source, theCond,
					nodeFactory.newCompoundStatementNode(source, results)));
			results = newResults;
		}

		List<BlockItemNode> newResults = new LinkedList<>();

		for (BlockItemNode result : results)
			newResults.add((BlockItemNode) applySubstitutions(
					mpiCommSizeOrRankSubstitutions(result),
					Arrays.asList(result)).get(0));
		return newResults;
	}

	private List<BlockItemNode> translateMemoryLocation(ExpressionNode memLoc) {
		List<BlockItemNode> results = new LinkedList<>();
		Source source = memLoc.getSource();

		if (memLoc.getType().kind() == Type.TypeKind.ACSL_MPI_TYPE) {
			// translate *\mpi_buf(p, c, d) to $mpi_assigns(p, c, d):
			OperatorNode deref = (OperatorNode) memLoc;
			ExpressionNode[] normedMpiBuf = normalizeMpiBufExpressionWorker(
					deref.getArgument(0));
			ExpressionNode ptr = normedMpiBuf[0], count = normedMpiBuf[1],
					datatype = normedMpiBuf[2];
			ExpressionNode mpiAssignsFun = nodeFactory
					.newIdentifierExpressionNode(source,
							nodeFactory.newIdentifierNode(source,
									MPI_CONTRACT_CONSTS.MPI_ASSIGNS_FUN));
			List<ExpressionNode> args = new LinkedList<>();

			args.add(ptr.copy());
			args.add(count.copy());
			args.add(datatype.copy());
			results.add(nodeFactory.newExpressionStatementNode(nodeFactory
					.newFunctionCallNode(source, mpiAssignsFun, args, null)));
		} else if (memLoc
				.expressionKind() == ExpressionKind.CURLY_BRACED_TERM_SET) {
			CurlyBracedTermSetNode termSet = (CurlyBracedTermSetNode) memLoc;

			if (termSet.isExplicit())
				for (ExpressionNode elt : termSet.getExplicitElements())
					results.addAll(translateMemoryLocation(elt));
			else
				results.addAll(translateSetComprehension(termSet));
		} else {
			// normal case, translate it to $mem_havoc:
			ExpressionNode ptrToMemLoc = nodeFactory.newOperatorNode(source,
					Operator.ADDRESSOF, memLoc.copy());
			ExpressionNode castToMem = nodeFactory.newCastNode(source,
					nodeFactory.newMemTypeNode(source), ptrToMemLoc);
			ExpressionNode memHavocFun = nodeFactory
					.newIdentifierExpressionNode(source,
							nodeFactory.newIdentifierNode(source,
									MPI_CONTRACT_CONSTS.MEM_HAVOC_FUN));
			List<ExpressionNode> args = new LinkedList<>();

			args.add(castToMem);
			results.add(nodeFactory.newExpressionStatementNode(nodeFactory
					.newFunctionCallNode(source, memHavocFun, args, null)));
		}
		return results;
	}

	/**
	 * Translates set comprehension expression of one of the followings forms:
	 * <code>
	 *   1. {f(x) | int x; low <= (or <) x <= (or <) high}
	 *   2. {term | int x; pred}, where term and pred involves no x.
	 * </code>
	 * 
	 * For case 1, we translate it to <code>
	 *  for (int tmp = low; tmp <= high; tmp++) {
	 *     translation(f(tmp));
	 *  }
	 * </code>. For case 2, we translate it to <code>
	 *  if (pred) 
	 *    translation(term);
	 * </code> The function <code>translation</code> refers to the general
	 * transaltion method
	 * {@link #translateMemoryLocation(ExpressionNode, ExpressionNode)}.
	 * 
	 * @return
	 */
	private List<BlockItemNode> translateSetComprehension(
			CurlyBracedTermSetNode setComp) {
		SequenceNode<ExpressionNode> terms = setComp.getSetComprehensionTerms();
		ExpressionNode predicate = setComp.getPredicate();
		SequenceNode<VariableDeclarationNode> binders = setComp.getBinders();

		if (terms.numChildren() != 1 || binders.numChildren() != 1)
			throw setComprehensionLimitationException(setComp);

		Source source = setComp.getSource();
		ExpressionNode term = terms.getSequenceChild(0);
		VariableDeclarationNode binder = binders.getSequenceChild(0);
		Pair<ExpressionNode, ExpressionNode> lowerAndUpper = getBounds(
				nodeFactory.newIdentifierExpressionNode(binder.getSource(),
						binder.getIdentifier().copy()),
				binder.getTypeNode(), predicate);
		List<BlockItemNode> termTranslation = new LinkedList<>();
		List<BlockItemNode> results = new LinkedList<>();

		if (lowerAndUpper == null) {
			// if both the term and the predicate are independent of the bound
			// variable, we simply translate it to:
			// "if (predicate) translation(term)"
			if (!involveIdentifier(term, binder.getIdentifier())
					&& !involveIdentifier(predicate, binder.getIdentifier())) {
				termTranslation.addAll(translateMemoryLocation(term));
				results.add(nodeFactory.newIfNode(source, predicate.copy(),
						nodeFactory.newCompoundStatementNode(source,
								termTranslation)));
				return results;
			}
			throw setComprehensionLimitationException(setComp);
		}

		IdentifierNode tmpVarIdent = nodeFactory.newIdentifierNode(source,
				MPI_CONTRACT_CONSTS.GENERIC_TEMP_VAR_NAME);

		term = substitute(term, binder.getIdentifier(), tmpVarIdent);
		termTranslation.addAll(translateMemoryLocation(term.copy()));

		// for (int tmpVar = lower; tmpVar <= upper; tmpVar+=1)
		// translation(term[tmpVar/bc])
		VariableDeclarationNode tmpVarDecl = nodeFactory
				.newVariableDeclarationNode(source, tmpVarIdent.copy(),
						nodeFactory.newBasicTypeNode(source, BasicTypeKind.INT),
						lowerAndUpper.left.copy());
		ExpressionNode loopCond = nodeFactory
				.newOperatorNode(source, Operator.LTE,
						nodeFactory.newIdentifierExpressionNode(source,
								tmpVarIdent.copy()),
						lowerAndUpper.right.copy());
		ExpressionNode loopIncr = nodeFactory.newOperatorNode(source,
				Operator.POSTINCREMENT, nodeFactory.newIdentifierExpressionNode(
						source, tmpVarIdent.copy()));
		List<VariableDeclarationNode> loopInitializer = new LinkedList<>();
		StatementNode loopBody = nodeFactory.newCompoundStatementNode(source,
				termTranslation);

		loopInitializer.add(tmpVarDecl);
		results.add(nodeFactory.newForLoopNode(source,
				nodeFactory.newForLoopInitializerNode(source, loopInitializer),
				loopCond, loopIncr, loopBody, null));
		// TODO: the other case where predicate is independent of the bound
		// variable
		return results;
	}

	/**
	 * 
	 * @param boundVar
	 *            an integer bound variable identifier expression. This method
	 *            will return null if the bound variable is not an integer.
	 * @param predicate
	 *            expected to be of the form: <code>
	 *           lower < (or <=) bv && bv < (or <=) upper
	 *           </code>. This method will return null if this predicate is not
	 *            in the expected form
	 * @return a pair of expressions (lower, upper) or null if fail to extact
	 *         bounds of the given bound variable
	 */
	private Pair<ExpressionNode, ExpressionNode> getBounds(
			IdentifierExpressionNode boundVar, TypeNode boundVarTypeNode,
			ExpressionNode predicate) {
		if (predicate.expressionKind() != ExpressionKind.OPERATOR)
			return null;

		Type boundVarType = boundVarTypeNode.getType();
		OperatorNode opNode = (OperatorNode) predicate;

		// bound var must have integer type:
		if (boundVarType.kind() != Type.TypeKind.BASIC)
			return null;
		if (((StandardBasicType) boundVarType)
				.getBasicTypeKind() != BasicTypeKind.INT)
			return null;
		if (opNode.getOperator() != Operator.LAND)
			return null;

		ExpressionNode low = getBound(boundVar, opNode.getArgument(0), true); // getLower
		ExpressionNode high = getBound(boundVar, opNode.getArgument(1), false); // getUpper

		if (low == null || high == null) {
			low = getBound(boundVar, opNode.getArgument(1), true); // getLower
			high = getBound(boundVar, opNode.getArgument(0), false); // getUpper
		}
		if (low == null || high == null)
			return null;
		return new Pair<>(low, high);
	}

	/**
	 * 
	 * @param boundVar
	 *            a bound variable identifier expression
	 * @param inequation
	 *            a boolean predicate that is expected to be one of the forms
	 *            <code>
	 *            1. e1 < e2
	 *            2. e1 <= e2
	 *            3. e1 > e2
	 *            4. e1 >= e2
	 *            </code> and for each form, exact one side of the inequation
	 *            shall be the given bound variable. This method will return
	 *            null when this inequation is not in the expected form.
	 * @param getLower
	 *            true to make this method extract lower bound of the bound
	 *            variable from the equation, otherwise extract the upper bound
	 * @return the lower bound of the bound variable expressed in the inequation
	 *         if getLower is true; or the upper bound of the bound variable
	 *         expressed in the inequation of getLower is false; or null if the
	 *         inequation is not in a desired form
	 */
	private ExpressionNode getBound(IdentifierExpressionNode boundVar,
			ExpressionNode inequation, boolean getLower) {
		if (inequation.expressionKind() != ExpressionKind.OPERATOR)
			return null;

		OperatorNode opNode = (OperatorNode) inequation;
		Source source = opNode.getSource();
		// the arg index of the bound var when it's a less than (equals)
		// inequation:
		int bvIdxWhenLess = getLower ? 1 : 0;
		int bvIdxWhenGreater = getLower ? 0 : 1;

		if (opNode.getOperator() == Operator.LT)
			if (opNode.getArgument(bvIdxWhenLess).equals(boundVar))
				return nodeFactory.newOperatorNode(source, Operator.PLUS,
						opNode.getArgument(1 - bvIdxWhenLess).copy(),
						nodeFactory.newIntConstantNode(source, 1));
		if (opNode.getOperator() == Operator.LTE)
			if (opNode.getArgument(bvIdxWhenLess).equals(boundVar))
				return opNode.getArgument(1 - bvIdxWhenLess);
		if (opNode.getOperator() == Operator.GT)
			if (opNode.getArgument(bvIdxWhenGreater).equals(boundVar))
				return nodeFactory.newOperatorNode(source, Operator.PLUS,
						opNode.getArgument(1 - bvIdxWhenGreater).copy(),
						nodeFactory.newIntConstantNode(source, 1));
		if (opNode.getOperator() == Operator.GTE)
			if (opNode.getArgument(bvIdxWhenGreater).equals(boundVar))
				return opNode.getArgument(1 - bvIdxWhenGreater);
		return null;
		// TODO: cound check that the returning bound contains no bound variable
	}

	/**
	 * 
	 * @param setComp
	 * @return a {@link CIVLUnimplementedFeatureException} explaining why the
	 *         given set comprehension expression is not in the limited form.
	 */
	private CIVLUnimplementedFeatureException setComprehensionLimitationException(
			ExpressionNode setComp) {
		return new CIVLUnimplementedFeatureException(
				"Set comprehensions in assigns clauses are expected to have the form\n \""
						+ "{f(bv) | int bv; lower <=/< bv && bv <=/< upper}"
						+ "\" or \""
						+ "{term | int bv; pred}, where term and pred contain no bv.\n But saw "
						+ setComp.prettyRepresentation());
	}

	/**
	 * @return expr[y/x]: replacing x in expr with y
	 */
	private ExpressionNode substitute(ExpressionNode expr, IdentifierNode x,
			IdentifierNode y) {
		if (expr.expressionKind() == ExpressionKind.IDENTIFIER_EXPRESSION) {
			IdentifierExpressionNode identExpr = (IdentifierExpressionNode) expr;

			if (identExpr.getIdentifier().equals(x))
				return nodeFactory.newIdentifierExpressionNode(y.getSource(),
						y.copy());
			return expr;
		}

		ExpressionNode exprClone = expr.copy();
		List<Pair<ExpressionNode, ExpressionNode>> substitutors = substituteWorker(
				exprClone, x, y);

		return (ExpressionNode) applySubstitutions(substitutors,
				Arrays.asList(exprClone));
	}

	/**
	 * worker method of
	 * {@link #substitute(ExpressionNode, IdentifierNode, IdentifierNode)}
	 */
	private List<Pair<ExpressionNode, ExpressionNode>> substituteWorker(
			ExpressionNode expr, IdentifierNode x, IdentifierNode y) {
		List<Pair<ExpressionNode, ExpressionNode>> result = new LinkedList<>();

		if (expr.expressionKind() == ExpressionKind.IDENTIFIER_EXPRESSION) {
			IdentifierExpressionNode identExpr = (IdentifierExpressionNode) expr;

			if (identExpr.getIdentifier().equals(x))
				result.add(new Pair<>(identExpr, nodeFactory
						.newIdentifierExpressionNode(y.getSource(), y)));
			return result;
		}
		for (ASTNode child : expr.children())
			if (child.nodeKind() == NodeKind.EXPRESSION)
				result.addAll(substituteWorker((ExpressionNode) child, x, y));
		return result;
	}

	private boolean involveIdentifier(ExpressionNode expr,
			IdentifierNode ident) {
		if (expr.expressionKind() == ExpressionKind.IDENTIFIER_EXPRESSION) {
			IdentifierExpressionNode identExpr = (IdentifierExpressionNode) expr;

			if (identExpr.getIdentifier().equals(ident))
				return true;
		}
		for (ASTNode child : expr.children()) {
			if (child.nodeKind() == NodeKind.EXPRESSION)
				if (involveIdentifier((ExpressionNode) child, ident))
					return true;
		}
		return false;
	}
}
