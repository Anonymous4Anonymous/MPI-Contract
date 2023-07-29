package edu.udel.cis.vsl.abc.ast.node.IF.acsl;

import edu.udel.cis.vsl.abc.ast.node.IF.SequenceNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.VariableDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;

/**
 * <p>
 * This class represents two kinds of term set:
 * 1. explicit term set<code>{tset, tset, ...}</code>
 * 2. term set comprehension <code>{tset | binders (;pred)?}</code>
 * </p>
 * @author xxxx
 */
public interface CurlyBracedTermSetNode extends ExpressionNode {
	/**
	 * 
	 * @return true iff this node represents an explicit term set; otherwise, it
	 *         represents a term set comprehension.
	 */
	boolean isExplicit();
	
	/**
	 * This method is significt only if {@link #isExplicit()}
	 * 
	 * @return the set of explicit term set expressions listed in an explicit
	 *         set.
	 */
	Iterable<ExpressionNode> getExplicitElements();
	
	/**
	 * This method is significt only if not {@link #isExplicit()}
	 * 
	 * @return the "x" part of a set comprehension
	 *         <code>{x | binders; pred}</code>
	 */
	SequenceNode<ExpressionNode> getSetComprehensionTerms();

	/**
	 * This method is significt only if not {@link #isExplicit()}
	 * 
	 * @return the "binders" part of a set comprehension
	 *         <code>{x | binders; pred}</code>
	 */
	SequenceNode<VariableDeclarationNode> getBinders();

	/**
	 * This method is significt only if not {@link #isExplicit()}
	 * 
	 * @return the "pred" part of a set comprehension
	 *         <code>{x | binders; pred}</code>
	 */
	ExpressionNode getPredicate();
}
