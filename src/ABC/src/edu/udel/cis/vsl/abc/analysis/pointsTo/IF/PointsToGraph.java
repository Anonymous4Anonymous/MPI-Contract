package edu.udel.cis.vsl.abc.analysis.pointsTo.IF;

import java.util.List;

import edu.udel.cis.vsl.abc.ast.entity.IF.Variable;
import edu.udel.cis.vsl.abc.ast.type.IF.Field;

/**
 * <p>
 * A points-to graph G is a tuple (V, E, pt) where V are nodes, E are edges and
 * pt is a function from V to sets of V. A node represents a program object,
 * "pt(v)" represents the points-to set of a node "v" and an edge (v0, v1)
 * represents a relation "pt(v0) is a subset of pt(v1)".
 * </p>
 * 
 * 
 * <p>
 * This interface is an realization of the above graph where nodes are abstract
 * objects (instances of {@link AssignExprIF}s).
 * </p>
 * 
 * @author xxxx
 *
 */
public interface PointsToGraph {

	/**
	 * 
	 * @param root
	 *            a variable "v"
	 * @param fields
	 *            a sequence of accessing fields "{f0, f1, ...}"
	 * 
	 * @return the may points-to set of the given program object represented as
	 *         "v.f0.f1. ..."
	 */
	Iterable<AssignExprIF> mayPointsTo(Variable root, List<Field> fields);

	/**
	 * 
	 * @param expr
	 *            an abstract object
	 * 
	 * @return the may points-to set of the given abstract object
	 */
	Iterable<AssignExprIF> mayPointsTo(AssignExprIF expr);

	/**
	 * Adds additional point-to information to the graph. If it has any new
	 * information, the graph will be re-computed.
	 * 
	 * @param expr
	 *            the abstract object whose points-to set will be updated
	 * @param pointsTo
	 *            the points-to set that will be added
	 * @return true iff the graph will be re-computed.
	 */
	boolean addPointsTo(AssignExprIF expr, Iterable<AssignExprIF> pointsTo);

	/**
	 * Adds subset-of relation to the graph. If it has any new information, the
	 * graph will be re-computed.
	 * 
	 * @param superset
	 *            abstract object whose points-to set is the super-set in the
	 *            relation
	 * @param subset
	 *            abstract object whose points-to set is the sub-set in the
	 *            relation
	 * @return true iff the graph will be re-computed
	 */
	boolean addSubsetRelation(AssignExprIF superset, AssignExprIF subset);

	/**
	 * 
	 * @return a new instance that was deep copied from this instance
	 */
	PointsToGraph clone();
}
