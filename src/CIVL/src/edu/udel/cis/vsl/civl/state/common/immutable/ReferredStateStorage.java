package edu.udel.cis.vsl.civl.state.common.immutable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import edu.udel.cis.vsl.civl.state.IF.State;

/**
 * <p>
 * This class saves all referred states and givens them unique identifiers. A
 * referred state is a {@link State} that is referred by some variables.
 * </p>
 * 
 * <p>
 * All methods of this class can be called concurrently.
 * </p>
 * 
 * @author xxxx
 *
 */
class ReferredStateStorage {
	/**
	 * The dictionary of all the saved collate states. It is used for looking up
	 * a collate state by its unique ID.
	 */
	private Map<Integer, ImmutableState> stateDictionay = new ConcurrentHashMap<>(
			1000000);

	/**
	 * The map saves all collate states.
	 */
	private Map<ImmutableState, Integer> collateStates = new ConcurrentHashMap<>(
			1000000);

	/**
	 * Save the given collate state
	 * 
	 * @param collateState
	 *            a collate state
	 * @return The unique ID of this collate state in this storage
	 */
	int saveCollateState(ImmutableState collateState) {
		Integer seenID = collateStates.get(collateState);

		if (seenID == null) {
			int newID = collateStates.size();

			// If there are multiple threads trying to save the same state,
			// only the first one (who always has the correct new ID) will put
			// it in the map ...
			seenID = collateStates.putIfAbsent(collateState, newID);
			// Same, only first thread will have seenID == null ...
			if (seenID == null) {
				stateDictionay.putIfAbsent(newID, collateState);
				seenID = newID;
			}
		}
		return seenID;
	}

	/**
	 * Loop up a saved collate state by giving its unique ID
	 * 
	 * @param stateReferenceID
	 *            The unique ID of a collate state in this storage
	 * @return The state associated to the given ID.
	 */
	ImmutableState getSavedState(int stateReferenceID) {
		return stateDictionay.get(stateReferenceID);
	}
}
