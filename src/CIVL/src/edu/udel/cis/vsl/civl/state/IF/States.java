package edu.udel.cis.vsl.civl.state.IF;

import edu.udel.cis.vsl.civl.config.IF.CIVLConfiguration;
import edu.udel.cis.vsl.civl.model.IF.ModelFactory;
import edu.udel.cis.vsl.civl.state.common.immutable.ImmutableMemoryUnitFactory;
import edu.udel.cis.vsl.civl.state.common.immutable.ImmutableStateFactory;
import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;

/**
 * Entry point for the state module: provides a static method to get a new state
 * factory. The usual way to get a state factory is to first create a
 * modelFactory and then invoke
 * <code>States.newStateFactory(modelFactory)</code>.
 * 
 * @author xxxx
 * 
 */
public class States {

	/**
	 * Returns a new immutable state factory based on the given model factory.
	 * This implementation of StateFactory uses the Immutable Pattern: all
	 * states (and components) are immutable.
	 * 
	 * @param modelFactory
	 *            a model factory
	 * @return a new immutable state factory
	 */
	public static StateFactory newImmutableStateFactory(
			ModelFactory modelFactory, MemoryUnitFactory memFactory,
			CIVLConfiguration config) {
		return new ImmutableStateFactory(modelFactory, memFactory, config);
	}

	public static MemoryUnitFactory newImmutableMemoryUnitFactory(
			SymbolicUniverse universe) {
		return new ImmutableMemoryUnitFactory(universe);
	}

}
