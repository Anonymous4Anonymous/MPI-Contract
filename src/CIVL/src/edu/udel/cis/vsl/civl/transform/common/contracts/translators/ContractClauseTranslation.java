package edu.udel.cis.vsl.civl.transform.common.contracts.translators;

import java.util.List;

import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.BlockItemNode;

/**
 * 
 * An instance of this class represents a translation of a contract clause. A
 * translation consists of several components, which have different meanings for
 * different contract clauses.
 * 
 * <p>
 * For a translation of a requires/ensures clause:
 * <ul>
 * <li>{@link #condition()} the condition under which the contract clause is
 * significant</li>
 * <li>{@link #predicate()} is the expression specified by the clause</li>
 * <li>{@link #substitutions()} is a set of substitutions in the
 * {@link #predicate()} that shall be applied</li>
 * <li>{@link #prefix()} is a list of statements that shall be placed before
 * snapshot taking.</li>
 * <li>{@link #postfix()} is a list of statements that shall be placed after
 * {@link #predicate()} evaluation</li>
 * </ul>
 * </p>
 * 
 * <p>
 * For a translation of an assigns clause:
 * <ul>
 * <li>{@link #condition()} the condition under which the contract clause is
 * significant</li>
 * <li>{@link #predicate()} is a disjoint clause of the frame condition
 * assertion that is shared by all assigns clauses in the contract of a
 * function. For the translation layout of assigns, see
 * {@link TargetFrameCondTranslator} and {@link CalleeFrameCondTranslator}.</li>
 * <li>{@link #substitutions()} shall be null for frame conditions</li>
 * <li>
 * <li>{@link #prefix()} is a list of statements that shall be placed before the
 * translation of the function body</li>
 * <li>{@link #postfixes()()} is a list of statements that shall be placed after
 * the translation of the function body</li>
 * </ul>
 * </p>
 * 
 * @author xxxx
 *
 */
public class ContractClauseTranslation {

	private List<BlockItemNode> prefix;

	private List<BlockItemNode> suffix;

	private final List<ExpressionNode> conditions;

	private final List<ExpressionNode> predicates;

	ContractClauseTranslation(List<ExpressionNode> conditions,
			List<ExpressionNode> predicates, List<BlockItemNode> prefix,
			List<BlockItemNode> suffix) {
		this.prefix = prefix;
		this.suffix = suffix;
		this.conditions = conditions;
		this.predicates = predicates;
	}

	/**
	 * @return a list of statements. For how to use, see
	 *         {@link ContractClauseTranslation}.
	 */
	public List<BlockItemNode> prefix() {
		return prefix;
	}

	/**
	 * @return a list of statements. For how to use, see
	 *         {@link ContractClauseTranslation}.
	 */
	public List<BlockItemNode> postfix() {
		return suffix;
	}

	// /**
	// * @return a set of {@link ExpressionSubstitutor}s for {@link
	// #predicate()}
	// */
	// public List<ExpressionSubstitutor> substitutions() {
	// return substitutions;
	// }

	/**
	 * @return the boolean condition under which the contract of this
	 *         translation is significant; null if the condition is trivially
	 *         true.
	 */
	public List<ExpressionNode> conditions() {
		return conditions;
	}

	/**
	 * @return a predicate, which is a part of this translation. For how to use,
	 *         see {@link ContractClauseTranslation}.
	 */
	public List<ExpressionNode> predicates() {
		return predicates;
	}
}
