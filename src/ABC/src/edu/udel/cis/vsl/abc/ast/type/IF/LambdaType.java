package edu.udel.cis.vsl.abc.ast.type.IF;

import edu.udel.cis.vsl.abc.ast.node.IF.expression.LambdaNode;

/**
 * A lambda type represents the type of an lambda expression (see
 * {@link LambdaNode} . A lambda type consists of at most one free variable type
 * v and a lambda term type t. Both v and t are {@link UnqualifiedObjectType}s
 * 
 * @author xxxx
 *
 */
public interface LambdaType extends UnqualifiedObjectType {
	/**
	 * 
	 * @return The type the free variable v. null if v is absent.
	 */
	UnqualifiedObjectType freeVariableType();

	/**
	 * 
	 * @return The return type of the lambda function
	 */
	UnqualifiedObjectType lambdaFunctionReturnType();
}
