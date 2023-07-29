package edu.udel.cis.vsl.civl.transform.common.contracts.translators;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import edu.udel.cis.vsl.abc.ast.node.IF.NodeFactory;
import edu.udel.cis.vsl.abc.ast.node.IF.PairNode;
import edu.udel.cis.vsl.abc.ast.node.IF.SequenceNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.CurlyBracedTermSetNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.VariableDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode.ExpressionKind;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode.Operator;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.QuantifiedExpressionNode.Quantifier;
import edu.udel.cis.vsl.abc.ast.type.IF.Type.TypeKind;
import edu.udel.cis.vsl.abc.token.IF.Source;
import edu.udel.cis.vsl.abc.token.IF.TokenFactory;

public class CalleeWaitsforTranslator extends ContractTranslatorCommonBase {

	public CalleeWaitsforTranslator(NodeFactory nodeFactory,
			TokenFactory tokenFactory) {
		super(nodeFactory, tokenFactory);
	}

	/**
	 * @param conditions
	 *            the boolean conditions under which this waitsfor clause is
	 *            significant
	 * @param collateAccess
	 *            an expression representing a collate
	 * @param waitsforArgs
	 *            an NON-EMPTY {@link Iterable} of waitsfor argument expressions
	 * @return the boolean guard translated from the given
	 *         <code>waitsforArgs</code>
	 */
	public ExpressionNode translateWaitsforExpressions(
			List<ExpressionNode> conditions, ExpressionNode collateAccess,
			Iterable<ExpressionNode> waitsforArgs) {
		ExpressionNode result = nodeFactory
				.newBooleanConstantNode(collateAccess.getSource(), true);

		for (ExpressionNode waitsforArg : waitsforArgs) {
			if (waitsforArg.expressionKind() == ExpressionKind.NOTHING)
				return nodeFactory
						.newBooleanConstantNode(waitsforArg.getSource(), true);

			List<ExpressionNode> trans = translateWaitsforExpression(
					collateAccess.copy(), waitsforArg);
			ExpressionNode conjunct = conjunct(trans);

			result = nodeFactory.newOperatorNode(waitsforArg.getSource(),
					Operator.LAND, result, conjunct);
		}

		ExpressionNode theCondition = conjunct(conditions);

		result = nodeFactory.newOperatorNode(
				tokenFactory.join(theCondition.getSource(), result.getSource()),
				Operator.IMPLIES, theCondition, result);
		return (ExpressionNode) applySubstitutions(
				mpiCommSizeOrRankSubstitutions(result), Arrays.asList(result))
						.get(0);
	}

	/**
	 * <p>
	 * If the waitsfor expression 'e' is a set-comprehension expression <code>
	 *   e = {f(x) | int x; pred(x)},
	 * </code> we translate it to <code>
	 *   FORALL int x; pred(x) => $collate_arrived(collate, f(x))) 
	 * </code>. Otherwise, the translation is simply
	 * <code>$collate_arrived(collate, e)</code>
	 * </p>
	 * 
	 * <p>
	 * Note that the second parameter of <code>$collate_arrived</code> has
	 * $range type. So a cast to $range is needed if the waitsfor argument is
	 * not range type.
	 * </p>
	 */
	private List<ExpressionNode> translateWaitsforExpression(
			ExpressionNode collateAccess, ExpressionNode waitsfor) {
		List<ExpressionNode> results = new LinkedList<>();
		Source source = waitsfor.getSource();
		ExpressionNode guard;

		if (waitsfor.expressionKind() == ExpressionKind.CURLY_BRACED_TERM_SET) {
			CurlyBracedTermSetNode termSetNode = (CurlyBracedTermSetNode) waitsfor;

			if (termSetNode.isExplicit()) {
				for (ExpressionNode term : termSetNode.getExplicitElements())
					results.addAll(translateWaitsforExpression(
							collateAccess.copy(), term));
				return results;
			} else {
				ExpressionNode term = termSetNode.getSetComprehensionTerms()
						.getSequenceChild(0);
				VariableDeclarationNode binder = termSetNode.getBinders()
						.getSequenceChild(0);
				ExpressionNode pred = termSetNode.getPredicate();
				SequenceNode<PairNode<SequenceNode<VariableDeclarationNode>, ExpressionNode>> boundVars;
				List<VariableDeclarationNode> tmp = new LinkedList<>();
				List<PairNode<SequenceNode<VariableDeclarationNode>, ExpressionNode>> tmp2 = new LinkedList<>();

				tmp.add(binder.copy());
				tmp2.add(nodeFactory.newPairNode(source, nodeFactory
						.newSequenceNode(source, "bound variable", tmp), null));
				boundVars = nodeFactory.newSequenceNode(source,
						"bound variable list", tmp2);
				guard = nodeFactory.newQuantifiedExpressionNode(source,
						Quantifier.FORALL, boundVars, pred.copy(),
						collateArrivedCall(collateAccess, makeItRange(term)),
						null);
			}
		} else
			guard = collateArrivedCall(collateAccess, makeItRange(waitsfor));
		results.add(guard);
		return results;
	}

	private ExpressionNode collateArrivedCall(ExpressionNode collateAccess,
			ExpressionNode proc) {
		Source source = proc.getSource();
		ExpressionNode fun = nodeFactory.newIdentifierExpressionNode(source,
				nodeFactory.newIdentifierNode(source,
						MPI_CONTRACT_CONSTS.COLLATE_ARRIVED_FUN));
		List<ExpressionNode> args = new LinkedList<>();

		args.add(collateAccess);
		args.add(proc);
		return nodeFactory.newFunctionCallNode(source, fun, args, null);
	}

	/**
	 * @param rangeOrInteger
	 *            Either an integer type expression or a regular range
	 *            expression.
	 * @return A regular range whose low and high are both equal to the given
	 *         expression 'rangeOrInteger' iff 'rangeOrInteger' is not a regular
	 *         range expression. Otherwise return 'rangeOrInteger' directly.
	 */
	private ExpressionNode makeItRange(ExpressionNode rangeOrInteger) {
		if (rangeOrInteger.getType().kind() == TypeKind.RANGE)
			return rangeOrInteger;
		assert rangeOrInteger.getType().kind() == TypeKind.BASIC;
		return nodeFactory.newRegularRangeNode(rangeOrInteger.getSource(),
				rangeOrInteger.copy(), rangeOrInteger.copy());
	}
}
