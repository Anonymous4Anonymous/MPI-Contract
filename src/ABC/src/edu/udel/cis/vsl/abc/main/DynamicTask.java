package edu.udel.cis.vsl.abc.main;

/**
 * A dynamic tasks specifies an action to be taken once all current unit tasks
 * have been executed. It specifies possible new unit tasks to add to the
 * executor's work list. Typically, the class implementing {@link DynamicTask}
 * will have a reference to the {@link ABCExecutor} executing the tasks. The
 * method {@link #generateTasks()} will be called once all current unit tasks
 * have been executed. This method will usually use the executor to inspect the
 * state of translation --- e.g., to see which header files were included by the
 * preprocessor. It may also keep some history. It can use this information
 * however it likes to generate new unit tasks. These will be added to the work
 * list and the executor will execute them. Repeat until no new tasks are
 * generated.
 * 
 * @author xxxx
 *
 */
public interface DynamicTask {

	/**
	 * Returns unit tasks to be added to the unit task list.
	 * 
	 * @return
	 */
	UnitTask[] generateTasks();
}
