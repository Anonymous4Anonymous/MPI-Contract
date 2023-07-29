package edu.udel.cis.vsl.abc.ast.node.IF.type;

/**
 * A {@link TypeNode} representing a $lambda(free-var-type:func-type) type
 * 
 * @author xxxx
 *
 */
public interface LambdaTypeNode extends TypeNode {
	/**
	 * 
	 * @return The {@link TypeNode} representing the type of the free variable
	 */
	TypeNode freeVariableType();

	/**
	 * 
	 * @return The {@link TypeNode} representing the type of the lambda
	 *         function.
	 */
	TypeNode lambdaFunctionType();
}
