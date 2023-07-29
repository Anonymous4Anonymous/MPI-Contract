package edu.udel.cis.vsl.abc.analysis.pointsTo.IF;

import java.util.List;

import edu.udel.cis.vsl.abc.ast.IF.AST;
import edu.udel.cis.vsl.abc.ast.entity.IF.Entity;
import edu.udel.cis.vsl.abc.ast.entity.IF.Function;

/**
 * A flow-insensitive PointsToAnalyzer takes a program ({@link AST}), builds
 * points-to graph for the program and provides service for querying the "may
 * points-to" set of a lvalue
 * 
 * @author xxxx
 *
 */
public interface FlowInsensePointsToAnalyzer {

	/**
	 * Returns the points-to set of the given program object represented by a
	 * list of designations
	 * 
	 * @param func
	 *            the function where the entity appears
	 * @param designations
	 *            a list entities: <code>{var, field0, field1, ...}</code>
	 * @return the points-to set of the given variable entity
	 */
	List<AssignExprIF> mayPointsTo(Function func, Entity[] designations);

	/**
	 * Returns the points-to set of the given {@link AssignExprIF}, which is an
	 * abstraction of an object.
	 * 
	 * @param func
	 *            the function where the pointer expression appears
	 * @param ptr
	 *            an expression abstraction
	 * @return the points-to set of the given {@link AssignExprIF}
	 */
	List<AssignExprIF> mayPointsTo(Function func, AssignExprIF ptr);

	/**
	 * 
	 * @return the analyzed program associated with this analyzer
	 */
	AST analyzedProgram();

	/**
	 * 
	 * @return a reference to the {@link InsensitiveFlowFactory} used by this
	 *         analyzer
	 */
	InsensitiveFlowFactory insensitiveFlowFactory();

	/**
	 * 
	 * @return a reference to the {@link InsensitiveFlow} representing the body
	 *         of the given function that was analyzed by this instance.
	 */
	InsensitiveFlow insensitiveFlow(Function func);
}
