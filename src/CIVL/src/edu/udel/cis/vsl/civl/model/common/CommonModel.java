/**
 * 
 */
package edu.udel.cis.vsl.civl.model.common;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import edu.udel.cis.vsl.abc.program.IF.Program;
import edu.udel.cis.vsl.civl.model.IF.CIVLFunction;
import edu.udel.cis.vsl.civl.model.IF.CIVLSource;
import edu.udel.cis.vsl.civl.model.IF.LogicFunction;
import edu.udel.cis.vsl.civl.model.IF.Model;
import edu.udel.cis.vsl.civl.model.IF.ModelFactory;
import edu.udel.cis.vsl.civl.model.IF.Scope;
import edu.udel.cis.vsl.civl.model.IF.location.Location;
import edu.udel.cis.vsl.civl.model.IF.statement.MallocStatement;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLBundleType;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLType;
import edu.udel.cis.vsl.civl.model.IF.variable.Variable;

/**
 * <p>
 * Implementation of {@link Model}.
 * </p>
 * 
 * @maintainer Stephen Siegel (xxxx)
 * 
 * @author Timothy K. Zirkel (zirkel)
 */
public class CommonModel extends CommonSourceable implements Model {

	private LinkedList<CIVLFunction> functions;
	private CIVLFunction rootFunction;
	private ModelFactory modelFactory;
	private String name = "";
	private Map<String, Variable> externVariables;
	private CIVLType queueType;
	private CIVLType messageType;
	private CIVLBundleType bundleType;
	private Program program;
	private List<MallocStatement> mallocStatements;
	private Scope staticConstantScope;
	private boolean hasFscanf;
	private Location sleep = null;
	private boolean hasStateRef = false;

	/**
	 * All translated {@link LogicFunction}s during model building:
	 */
	private List<LogicFunction> seenLogicFunctions = null;

	/**
	 * A model of a Chapel program.
	 * 
	 * @param source
	 *            The CIVL source of the model
	 * @param factory
	 *            The ModelFactory responsible for creating this model.
	 * @param root
	 *            The designated outermost function, called "System."
	 */
	public CommonModel(CIVLSource source, ModelFactory factory,
			CIVLFunction root, Program program) {
		super(source);
		this.modelFactory = factory;
		this.rootFunction = root;
		functions = new LinkedList<>();
		functions.add(root);
		this.program = program;
		this.staticConstantScope = factory.staticConstantScope();
	}

	/**
	 * @return The model factory that created this model.
	 */
	public ModelFactory factory() {
		return modelFactory;
	}

	/**
	 * @param name
	 *            The name of this model.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return The name of this model.
	 */
	public String name() {
		return name;
	}

	/**
	 * @return The set of all functions in the model.
	 */
	public Set<CIVLFunction> functions() {
		return new HashSet<CIVLFunction>(functions);
	}

	/**
	 * @return The designated outermost function "System."
	 */
	public CIVLFunction rootFunction() {
		return rootFunction;
	}

	/**
	 * @param functions
	 *            The set of all functions in the model.
	 */
	public void setFunctions(Set<CIVLFunction> functions) {
		this.functions = new LinkedList<CIVLFunction>(functions);
	}

	/**
	 * @param system
	 *            The designated outermost function "System."
	 */
	public void setRootFunction(CIVLFunction system) {
		this.rootFunction = system;
	}

	/**
	 * @param function
	 *            The function to be added to the model.
	 */
	public void addFunction(CIVLFunction function) {
		functions.add(function);
	}

	/**
	 * @param queueType
	 *            The queue type used by this model.
	 */
	public void setQueueType(CIVLType queueType) {
		this.queueType = queueType;
	}

	/**
	 * @param messageType
	 *            The message type used by this model.
	 */
	public void setMessageType(CIVLType messageType) {
		this.messageType = messageType;
	}

	/**
	 * Get a function based on its name.
	 * 
	 * @param name
	 *            The name of the function.
	 * @return The function with the given name. Null if not found.
	 */
	public CIVLFunction function(String name) {
		for (CIVLFunction f : functions) {
			if (f.name().name().equals(name)) {
				return f;
			}
		}
		return null;
	}

	/**
	 * Print the model.
	 * 
	 * @param out
	 *            The PrintStream used to print the model.
	 */
	@Override
	public void print(PrintStream out, boolean isDebug) {
		out.print("Model");
		if (name != null)
			out.print(" " + name);
		out.println();
		staticConstantScope.print(" | ", out, isDebug);
		for (CIVLFunction function : functions) {
			function.print(" | ", out, isDebug);
		}
		out.flush();
	}

	/**
	 * @param externVariables
	 *            Map of names to variables for all extern variables used in
	 *            this model.
	 */
	public void setExternVariables(Map<String, Variable> externVariables) {
		this.externVariables = externVariables;
	}

	/**
	 * @return Map of names to variables for all extern variables used in this
	 *         model.
	 */
	public Map<String, Variable> externVariables() {
		return externVariables;
	}

	/**
	 * Update the list of malloc statements
	 * 
	 * @param mallocStatements
	 *            the list of malloc statements
	 */
	public void setMallocStatements(List<MallocStatement> mallocStatements) {
		this.mallocStatements = mallocStatements;
	}

	@Override
	public int getNumMallocs() {
		return mallocStatements.size();
	}

	@Override
	public MallocStatement getMalloc(int index) {
		return mallocStatements.get(index);
	}

	@Override
	public CIVLType queueType() {
		return queueType;
	}

	@Override
	public CIVLType mesageType() {
		return messageType;
	}

	@Override
	public CIVLBundleType bundleType() {
		return this.bundleType;
	}

	@Override
	public void setBundleType(CIVLBundleType type) {
		this.bundleType = type;
	}

	@Override
	public void complete() {
		this.rootFunction.outerScope().complete();
		this.renumberLocations();
		containsStateReference();
	}

	private void containsStateReference() {
		Scope current;
		Stack<Scope> working = new Stack<>();
		Set<Integer> visited = new HashSet<>();

		working.push(rootFunction.outerScope());
		while (!working.isEmpty()) {
			int id;

			current = working.pop();
			id = current.id();
			if (visited.contains(id))
				continue;
			visited.add(id);
			if (!current.variablesWithStaterefs().isEmpty()) {
				this.hasStateRef = true;
				return;
			}
			working.addAll(current.children());
		}
	}

	private void renumberLocations() {
		int id = 0;

		for (CIVLFunction function : this.functions) {
			function.simplify();
			for (Location location : function.locations()) {
				location.setId(id++);
			}
		}
	}

	@Override
	public void setHasFscanf(boolean value) {
		this.hasFscanf = value;
	}

	@Override
	public boolean hasFscanf() {
		return this.hasFscanf;
	}

	@Override
	public Program program() {
		return this.program;
	}

	@Override
	public void printUnreachedCode(PrintStream out) {
		boolean noUnreachedCode = true;

		for (CIVLFunction function : functions) {
			StringBuffer unreached = function.unreachedCode();

			if (!unreached.toString().equals("")) {
				noUnreachedCode = false;
				out.print(unreached);
			}
		}
		if (noUnreachedCode)
			out.println("This program doesn't have any unreachable code.");
		out.println();
	}

	@Override
	public List<Variable> outputVariables() {
		Scope root = this.rootFunction.outerScope();
		List<Variable> result = new LinkedList<>();

		assert root.id() == 0;
		for (Variable variable : root.variables())
			if (variable.isOutput())
				result.add(variable);
		return result;
	}

	@Override
	public Scope staticConstantScope() {
		return this.staticConstantScope;
	}

	@Override
	public void setSleepLocation(Location sleep) {
		this.sleep = sleep;
	}

	@Override
	public Location sleepLocation() {
		return this.sleep;
	}

	@Override
	public boolean hasStateRefVariables() {
		return this.hasStateRef;
	}

	@Override
	public List<LogicFunction> getAllLogicFunctions() {
		if (seenLogicFunctions == null)
			seenLogicFunctions = new LinkedList<>();
		return seenLogicFunctions;
	}

	@Override
	public void setLogicFunctions(List<LogicFunction> logicFunctions) {
		seenLogicFunctions = logicFunctions;
	}
}
