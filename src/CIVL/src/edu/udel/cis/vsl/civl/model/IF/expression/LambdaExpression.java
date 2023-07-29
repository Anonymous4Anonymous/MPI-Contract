package edu.udel.cis.vsl.civl.model.IF.expression;

import edu.udel.cis.vsl.civl.model.IF.type.CIVLFunctionType;
import edu.udel.cis.vsl.civl.model.IF.variable.Variable;

/**
 * A CIVL-C quanti, bound variable declaration list, (optional) restriction and
 * expression. It has the following syntax:<br>
 * 
 * <pre>
 * array-lambda: 
 *   $lambda ( variable-decl-list | restrict? ) expression ;
 * 
 * variable-decl-list:
 *   variable-decl-sub-list (; variable-decl-sub-list)* ;
 *   
 * variable-decl-sub-list:
 *   type ID (, ID)* (: domain)?
 * 
 * </pre>
 * 
 * e.g.,
 * 
 * <pre>
 * (int[n]) $lambda (int i) i*4
 * </pre>
 * 
 * @author Manchun Zheng (zxxxx)
 * 
 */
public interface LambdaExpression extends Expression {

	/**
	 * @return The free variable in this lambda expression
	 */
	Variable freeVariable();

	/** The expression e(x). */
	Expression lambdaFunction();

	@Override
	CIVLFunctionType getExpressionType();
}
