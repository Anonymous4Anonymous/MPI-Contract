package edu.udel.cis.vsl.civl.semantics.IF;

import java.io.PrintStream;
import java.util.List;

import edu.udel.cis.vsl.civl.config.IF.CIVLConfiguration;
import edu.udel.cis.vsl.civl.dynamic.IF.SymbolicUtility;
import edu.udel.cis.vsl.civl.log.IF.CIVLErrorLogger;
import edu.udel.cis.vsl.civl.model.IF.CIVLSource;
import edu.udel.cis.vsl.civl.model.IF.expression.Expression;
import edu.udel.cis.vsl.civl.model.IF.expression.LHSExpression;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLType;
import edu.udel.cis.vsl.civl.model.IF.variable.Variable;
import edu.udel.cis.vsl.civl.state.IF.State;
import edu.udel.cis.vsl.civl.state.IF.StateFactory;
import edu.udel.cis.vsl.civl.state.IF.UnsatisfiablePathConditionException;
import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;

public interface Executor {

	/**
	 * Assigns a value to the referenced cell in the state. Returns a new state
	 * which is equivalent to the old state except that the memory specified by
	 * the given pointer value is assigned the given value.
	 * 
	 * @param source
	 *            the source code information for error report
	 * @param state
	 *            a CIVL model state
	 * @param pid
	 *            the PID of the calling process
	 * @param pointer
	 *            a pointer value
	 * @param value
	 *            a value to be assigned to the referenced memory location
	 * @return the new state
	 * @throws UnsatisfiablePathConditionException
	 */
	State assign(CIVLSource source, State state, int pid,
			SymbolicExpression pointer, SymbolicExpression value)
			throws UnsatisfiablePathConditionException;

	/**
	 * <p>
	 * Given a "pointer" to a variable ({@link Variable}) or a memory heap
	 * object (see {@link SymbolicUtility#isPointerToHeap(SymbolicExpression)}),
	 * a symbolic expression "newValue" that represents the new value of the
	 * variable or the memory heap object and a symbolic expression
	 * "valueSetTemplate" of {@link SymbolicUniverse#valueSetTemplateType()},
	 * which refers to a set of regions in the variable or the heap object, this
	 * method carves the part that is referred by the "valueSetTemplate" out of
	 * the "newValue" and assigns it to the counterpart in the variable
	 * or the heap object.
	 * </p>
	 * 
	 * @param source
	 *            the {@link CIVLSource} that is related to this assignment
	 * @param state
	 *            the state where the assignment happens
	 * @param pid
	 *            the PID of the running process
	 * @param pointerToVarOrHeapObject
	 *            a pointer to a variable or a memory heap object
	 * @param newValueOfVarOrHeapObject
	 *            a symbolic expression that represents the new value of the
	 *            referred variable or the memory heap object; the type of this
	 *            symbolic expression will be equal to the dynamic type of the
	 *            variable of the heap object
	 * @param valueSetTemplate
	 *            a symbolic expression of type
	 *            {@link SymbolicUniverse#valueSetTemplateType()} that
	 *            represents a specific (sub-)region that will be assigned.
	 * @return the state after assignment
	 * @throws UnsatisfiablePathConditionException
	 */
	State assign2(CIVLSource source, State state, int pid,
			SymbolicExpression pointerToVarOrHeapObject,
			SymbolicExpression newValueOfVarOrHeapObject,
			SymbolicExpression valueSetTemplate)
			throws UnsatisfiablePathConditionException;

	/**
	 * Assigns a value to the memory location specified by the given
	 * left-hand-side expression.
	 * 
	 * @param state
	 *            a CIVL model state
	 * @param pid
	 *            the PID of the process executing the assignment
	 * @param process
	 *            the process information (process name + PID) for error report
	 * @param lhs
	 *            a left-hand-side expression
	 * @param value
	 *            the value being assigned to the left-hand-side
	 * @param isInitializer
	 *            boolean value indicating if the given left-hand side
	 *            expression will be INITIALIZED by the given value
	 * @return the new state
	 * @throws UnsatisfiablePathConditionException
	 */
	State assign(State state, int pid, String process, LHSExpression lhs,
			SymbolicExpression value, boolean isInitializer)
			throws UnsatisfiablePathConditionException;

	/**
	 * @return The state factory associated with this executor.
	 */
	StateFactory stateFactory();

	/**
	 * @return The evaluator used by this executor.
	 */
	Evaluator evaluator();

	/**
	 * Returns the number of "steps" executed since this Executor was created.
	 * 
	 * @return the number of steps executed
	 */
	long getNumSteps();

	/**
	 * Adds a new object to the heap of a certain scope; returns the pointer of
	 * the object in the heap.
	 * 
	 * @param source
	 *            The source code element to be used to report errors.
	 * @param state
	 *            The current state.
	 * @param pid
	 *            The ID of the process where this computation happens.
	 * @param scopeExpression
	 *            The static expression of the scope value.
	 * @param scopeValue
	 *            The symbolic expression of the scope.
	 * @param objectType
	 *            The CIVL type of the object to be added, needed to decide the
	 *            field index in the heap.
	 * @param objectValue
	 *            The object to be added to the heap.
	 * @return The new state after allocating the specified object in the heap
	 *         and the pointer of the object in the heap.
	 * @throws UnsatisfiablePathConditionException
	 */
	Evaluation malloc(CIVLSource source, State state, int pid, String process,
			Expression scopeExpression, SymbolicExpression scopeValue,
			CIVLType objectType, SymbolicExpression objectValue)
			throws UnsatisfiablePathConditionException;

	/**
	 * Returns the state that results from executing the statement, or null if
	 * path condition becomes unsatisfiable.
	 * 
	 * @param state
	 *            the state that the transition emanates from
	 * @param pid
	 *            the PID of the process that the transition
	 * @param transition
	 *            a deterministic transition to be executed
	 * @return The state after the transition is executed.
	 * @throws UnsatisfiablePathConditionException
	 *             when an error is encountered during the execution
	 */
	State execute(State state, int pid, Transition transition)
			throws UnsatisfiablePathConditionException;

	/**
	 * Returns the error logger used by this executor.
	 * 
	 * @return The error logger used by this executor.
	 */
	CIVLErrorLogger errorLogger();

	Evaluation execute_printf(CIVLSource source, State state, int pid,
			String process, Expression[] arguments,
			SymbolicExpression[] argumentValues, boolean forcePrint)
			throws UnsatisfiablePathConditionException;

	List<Format> splitFormat(CIVLSource source, StringBuffer formatBuffer);

	/**
	 * If there are insufficient arguments for the format, the behavior is
	 * undefined. If the format is exhausted while arguments remain, the excess
	 * arguments are evaluated (as always) but are otherwise ignored.
	 * 
	 * @param printStream
	 * @param source
	 * @param formats
	 * @param arguments
	 */
	void printf(PrintStream printStream, CIVLSource source, String process,
			List<Format> formats, List<StringBuffer> arguments);

	void setConfiguration(CIVLConfiguration config);
}
