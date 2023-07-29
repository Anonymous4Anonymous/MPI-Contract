package edu.udel.cis.vsl.abc.analysis.pointsTo.IF;

import edu.udel.cis.vsl.abc.ast.entity.IF.Function;

/**
 * <p>
 * The invocation graph node is described in detail in paper "Context-sensitive
 * inter-procedural points-to analysis in the presence of function pointers"
 * </p>
 * 
 * <p>
 * Briefly, a invocation graph node represents a lexical call to a function. A
 * program can be represented as a invocation graph where the root node is the
 * call to main function.
 * </p>
 * 
 * @author xxxx
 *
 */
public interface InvocationGraphNode {

	/**
	 * <p>
	 * Details these kinds can be found in paper "Context-sensitive
	 * inter-procedural points-to analysis in the presence of function
	 * pointers".
	 * </p>
	 * 
	 * @author xxxx
	 *
	 */
	public static enum IGNodeKind {
		/**
		 * represents a lexical call to a function where no recursion happens.
		 */
		ORDINARY,
		/**
		 * represents a recursive lexical call to a function f, i.e., there is
		 * an ancestor invocation graph node of this node that represents a
		 * lexical call to function f as well.
		 */
		APPROXIMATE,
		/**
		 * represents a lexical call to a function f which may contains a
		 * recursive call, i.e., there is an descendant invocation graph node of
		 * this node that represents a lexical call to function f as well.
		 */
		RECURSIVE
	}

	/* ********* read section ********** */
	/**
	 * 
	 * @return the InvocationGraphNodes that represents the function calls that
	 *         are lexically in the body of this function
	 */
	Iterable<InvocationGraphNode> children();

	/**
	 * 
	 * @return the parent node whose associated function all contains the
	 *         lexical call associated with this node
	 */
	InvocationGraphNode parent();

	/**
	 * @return the function this node refers to. Note that if the lexical call
	 *         associated with this node is not called through function pointer,
	 *         there is one exact {@link Function} this node refers to.
	 */
	Function function();

	/**
	 * 
	 * @return the set of accessed global objects by the calling function
	 */
	Iterable<AssignExprIF> accessedGlobals();

	/**
	 * @return an ordered array of actual parameters
	 */
	AssignExprIF[] actualParams();

	/**
	 * 
	 * @return an ordered array of formal parameters
	 */
	AssignExprIF[] formalParams();

	/**
	 * 
	 * @return the abstract object representing the expression that receives the
	 *         returning value from this call
	 */
	AssignExprIF returnTo();

	/**
	 * 
	 * @return the set of abstract objects that may be returned by this call
	 */
	Iterable<AssignExprIF> returnings();

	/**
	 * 
	 * @return the kind of the node
	 */
	IGNodeKind kind();

	/**
	 * 
	 * @return the RECURSIVE kind node that this APPROXIMATE node is associated
	 *         with. null if this node is NOT APPROXIMATE kind.
	 */
	InvocationGraphNode getRecursive();

	/**
	 * Share formal parameters, global accesses and returning values with the
	 * given node, which must represents a call to the same function as this
	 * instance.
	 * 
	 * @param node
	 */
	void share(InvocationGraphNode node);

	/* ********* write section ********** */

	/**
	 * Mark this node to be {@link IGNodeKind#RECURSIVE}
	 */
	void markRecursive();

	/**
	 * @param formals
	 *            the abstractions of the formal parameters of this node
	 */
	void setFormalParameters(AssignExprIF[] formals);

	/**
	 * save a returning expression in the associated function body in this node
	 * 
	 * @param returnValue
	 *            an return expression abstraction
	 */
	void addReturnValue(AssignExprIF returnValue);

	/**
	 * save an access to a global object by the function body of the function of
	 * this call
	 * 
	 * @param globalVar
	 *            a global object abstraction
	 */
	void addGlobalAccess(AssignExprIF globalAccess);

	/**
	 * Add a child to this node
	 * 
	 * @param child
	 */
	void addChild(InvocationGraphNode child);
}
