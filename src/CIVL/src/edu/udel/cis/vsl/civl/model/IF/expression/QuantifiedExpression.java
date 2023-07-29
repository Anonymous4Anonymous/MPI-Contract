package edu.udel.cis.vsl.civl.model.IF.expression;

import java.util.List;

import edu.udel.cis.vsl.civl.model.IF.variable.Variable;
import edu.udel.cis.vsl.civl.util.IF.Pair;

/**
 * A CIVL-C quantified expression, including three components, bound variable
 * declaration list, (optional) restriction and expression. It has the following
 * syntax:<br>
 * 
 * <pre>
 * quantified: 
 *   quantifier ( variable-decl-list | restrict? ) expression ;
 * 
 * variable-decl-list:
 *   variable-decl-sub-list (; variable-decl-sub-list)* ;
 *   
 * variable-decl-sub-list:
 *   type ID (, ID)* (: domain)?
 *   
 * quantifier: $forall | $exists | $uniform
 * </pre>
 * 
 * e.g.,
 * 
 * <pre>
 * $forall (int x, y: dom; double z | x > 0 && z<5.9) x*z > -1
 * </pre>
 * 
 * @author zirkel, Manchun Zheng (zxxxx)
 * 
 */
public interface QuantifiedExpression extends Expression {

	/**
	 * The different kinds of quantifiers which are possible. FORALL: the
	 * universal quantifier, EXISTS: the existential quantifier, UNIFORM: the
	 * uniform convergence quantifier.
	 * 
	 */
	enum Quantifier {
		FORALL, EXISTS, UNIFORM;
	}

	/**
	 * 
	 * @return The quantifier binding the variable.
	 */
	Quantifier quantifier();

	/**
	 * The list of bound variables.
	 * 
	 * @return
	 */
	List<Pair<List<Variable>, Expression>> boundVariableList();

	/**
	 * Boolean-valued expression assumed to hold when evaluating expression.
	 */
	Expression restriction();

	/** The expression e(x). */
	Expression expression();

	int numBoundVariables();

}
