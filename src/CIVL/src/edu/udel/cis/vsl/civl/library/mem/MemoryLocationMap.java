package edu.udel.cis.vsl.civl.library.mem;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;

/**
 * <p>
 * A map specifically designed for values of $mem type:
 * </p>
 * 
 * <p>
 * This map maps four keys: variable ID, heap ID, malloc ID and scope value to a
 * value set template. The four keys will not all be significant at same time:
 * <ol>
 * <li>if variable ID is positive, only variable ID and scope value are
 * significant</li>
 * <li>otherwise, only heap ID, mallocID and scope value are significant.</li>
 * </ol>
 * The heap ID is the ID of a lexical malloc statement. The malloc ID is the ID
 * of a runtime instance of a lexical malloc statement.
 * 
 * </p>
 * 
 * @author xxxx
 *
 */
public class MemoryLocationMap {

	private class TwoInt {
		private int[] int2;

		private TwoInt(int i, int j) {
			int2 = new int[]{i, j};
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof TwoInt) {
				TwoInt other = (TwoInt) obj;

				return Arrays.equals(int2, other.int2);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(int2);
		}
	}

	/**
	 * The entry of this map:
	 * 
	 * @author xxxx
	 *
	 */
	public class MemLocMapEntry {

		private int vid, heapID, mallocID;

		private SymbolicExpression scope, vst;

		private MemLocMapEntry(int vid, int heapID, int mallocID,
				SymbolicExpression scopeVal,
				SymbolicExpression valueSetTemplate) {
			this.vid = vid;
			this.heapID = heapID;
			this.mallocID = mallocID;
			this.scope = scopeVal;
			this.vst = valueSetTemplate;
		}

		public int vid() {
			return vid;
		}
		public int heapID() {
			return heapID;
		}
		public int mallocID() {
			return mallocID;
		}
		public SymbolicExpression scopeValue() {
			return scope;
		}
		public SymbolicExpression valueSetTemplate() {
			return vst;
		}
	}

	Map<Integer, Map<SymbolicExpression, SymbolicExpression>> variableTable;

	Map<TwoInt, Map<SymbolicExpression, SymbolicExpression>> heapTable;

	public MemoryLocationMap() {
		this.variableTable = new HashMap<>();
		this.heapTable = new HashMap<>();
	}

	/**
	 * 
	 * the get operation. For meanings of parameters, see
	 * {@link MemoryLocationMap}
	 */
	public SymbolicExpression get(int vid, int heapID, int mallocID,
			SymbolicExpression scope) {
		Map<SymbolicExpression, SymbolicExpression> subMap;

		if (vid == 0)
			subMap = heapTable.get(new TwoInt(heapID, mallocID));
		else {
			assert vid > 0;
			subMap = variableTable.get(vid);
		}
		return subMap == null ? null : subMap.get(scope);
	}

	/**
	 * 
	 * the put operation. For meanings of parameters, see
	 * {@link MemoryLocationMap}
	 */
	public void put(int vid, int heapID, int mallocID, SymbolicExpression scope,
			SymbolicExpression value) {

		if (vid == 0) {
			TwoInt key = new TwoInt(heapID, mallocID);
			Map<SymbolicExpression, SymbolicExpression> subMap = heapTable
					.get(key);

			if (subMap == null)
				subMap = new HashMap<>();
			subMap.put(scope, value);
			heapTable.put(key, subMap);
		} else {
			assert vid > 0;
			Map<SymbolicExpression, SymbolicExpression> subMap = variableTable
					.get(vid);

			if (subMap == null)
				subMap = new HashMap<>();
			subMap.put(scope, value);
			variableTable.put(vid, subMap);
		}
	}

	/**
	 * 
	 * the entrySet method.
	 */
	public Iterable<MemLocMapEntry> entrySet() {
		List<MemLocMapEntry> results = new LinkedList<>();

		for (Entry<Integer, Map<SymbolicExpression, SymbolicExpression>> entry : variableTable
				.entrySet()) {
			int vid = entry.getKey();

			for (Entry<SymbolicExpression, SymbolicExpression> subEntry : entry
					.getValue().entrySet())
				results.add(new MemLocMapEntry(vid, -1, -1, subEntry.getKey(),
						subEntry.getValue()));
		}
		for (Entry<TwoInt, Map<SymbolicExpression, SymbolicExpression>> entry : heapTable
				.entrySet()) {
			TwoInt heap = entry.getKey();

			for (Entry<SymbolicExpression, SymbolicExpression> subEntry : entry
					.getValue().entrySet())
				results.add(new MemLocMapEntry(0, heap.int2[0], heap.int2[1],
						subEntry.getKey(), subEntry.getValue()));
		}
		return results;
	}

	public int size() {
		return this.heapTable.size() + this.variableTable.size();
	}
}
