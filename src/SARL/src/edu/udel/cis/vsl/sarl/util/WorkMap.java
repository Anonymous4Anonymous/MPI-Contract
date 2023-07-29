package edu.udel.cis.vsl.sarl.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import edu.udel.cis.vsl.sarl.IF.SARLInternalException;

/**
 * <p>
 * A special kind of map for use in "work list" algorithms. Each entry in the
 * map is either "dirty" or "clean". The dirty elements conceptually comprise
 * the work list. Once all elements are clean, the algorithm terminates.
 * </p>
 * 
 * The semantics of the operations:
 * 
 * <ol>
 * <li>If this map is modified in any way (e.g., through
 * {@link #put(Object, Object)}, {@link #putAll(Map)}, {@link #remove(Object)},
 * etc.) then every entry in this map becomes dirty.</li>
 * 
 * <li>The method {@link #hold()} chooses a dirty entry, makes it clean, stores
 * it in the "hold buffer" (a buffer of size 1), and returns it. This is used,
 * e.g., in algorithms that simplify a map by removing one entry at a time and
 * simplifying that entry using the remainder of the map. If {@link #hold()} is
 * called a second time, without an intervening {@link #release()}, the first
 * held element is simply lost and replaced by the new one.</li>
 * 
 * <li>the method {@link #release()} takes the entry in the hold buffer and puts
 * it back in the map (as a clean entry, since only clean entries are stored in
 * the hold buffer).</li>
 * </ol>
 * 
 * @author xxxx
 *
 * @param <K>
 *            the type of the keys in this map
 * @param <V>
 *            the type of the values in this map
 */
public class WorkMap<K, V> implements Map<K, V> {

	/**
	 * The dirty entries. Note that the key set of the {@link #dirtyMap} will
	 * always be disjoint from that of the {@link #cleanMap}.
	 */
	private TreeMap<K, V> dirtyMap;

	/**
	 * The clean entries. Note that the key set of the {@link #dirtyMap} will
	 * always be disjoint from that of the {@link #cleanMap}.
	 */
	private TreeMap<K, V> cleanMap;

	/**
	 * The hold buffer.
	 */
	private Entry<K, V> held;

	protected WorkMap(TreeMap<K, V> dirtyMap, TreeMap<K, V> cleanMap,
			Entry<K, V> held) {
		this.dirtyMap = dirtyMap;
		this.cleanMap = cleanMap;
		this.held = held;
	}

	public WorkMap() {
		this(new TreeMap<K, V>(), new TreeMap<K, V>(), null);
	}

	public WorkMap(Comparator<? super K> keyComparator) {
		this(new TreeMap<K, V>(keyComparator), new TreeMap<K, V>(keyComparator),
				null);
	}

	public void makeAllDirty() {
		dirtyMap.putAll(cleanMap);
		cleanMap.clear();
	}

	public boolean hasDirty() {
		return !dirtyMap.isEmpty();
	}

	/**
	 * Removes a dirty entry from this map and stores it in this map's hold
	 * buffer. Makes that entry clean. If there is no dirty entry, does nothing.
	 * 
	 * @return the removed entry, or {@code null} if there was no dirty entry in
	 *         this map
	 */
	public Entry<K, V> hold() {
		if (dirtyMap.isEmpty())
			return null;
		// TODO: not necessarily the best performance to keep starting
		// over from first element. Better might be to keep track
		// of the last element returned by this method, and then ask
		// for the next element greater than that.
		held = dirtyMap.pollFirstEntry();
		return held;
	}

	public void release() {
		if (held == null)
			throw new SARLInternalException(
					"Attempt to release when nothing held");
		cleanMap.put(held.getKey(), held.getValue());
	}

	@Override
	public int size() {
		return cleanMap.size() + dirtyMap.size();
	}

	@Override
	public boolean isEmpty() {
		return cleanMap.isEmpty() && dirtyMap.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return cleanMap.containsKey(key) || dirtyMap.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return cleanMap.containsValue(value) || dirtyMap.containsValue(value);
	}

	@Override
	public V get(Object key) {
		V result = cleanMap.get(key);

		if (result == null)
			result = dirtyMap.get(key);
		return result;
	}

	@Override
	public V put(K key, V value) {
		if (cleanMap.isEmpty())
			return dirtyMap.put(key, value);

		V old = cleanMap.put(key, value);

		if (old == null || !old.equals(value))
			makeAllDirty();
		return old;
	}

	@Override
	public V remove(Object key) {
		if (cleanMap.isEmpty())
			return dirtyMap.remove(key);

		V old = cleanMap.remove(key);

		if (old == null) {
			old = dirtyMap.remove(key);
			if (old == null)
				return null;
		}
		makeAllDirty();
		return old;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		for (Entry<? extends K, ? extends V> entry : m.entrySet())
			put(entry.getKey(), entry.getValue());
	}

	@Override
	public void clear() {
		cleanMap.clear();
		dirtyMap.clear();
	}

	@Override
	public Set<K> keySet() {
		return new JointSet<K>(dirtyMap.keySet(), cleanMap.keySet());
	}

	@Override
	public Collection<V> values() {
		return new JointCollection<V>(dirtyMap.values(), cleanMap.values());
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return new JointSet<Entry<K, V>>(dirtyMap.entrySet(),
				cleanMap.entrySet());
	}

	@Override
	public String toString() {
		boolean first = true;
		StringBuffer buf = new StringBuffer();

		buf.append("{");
		for (Entry<K, V> entry : this.entrySet()) {
			if (first)
				first = false;
			else
				buf.append(",");
			buf.append(entry.getKey().toString());
			buf.append("->");
			buf.append(entry.getValue().toString());
		}
		buf.append("}");
		return buf.toString();
	}

	@Override
	public WorkMap<K, V> clone() {
		@SuppressWarnings("unchecked")
		TreeMap<K, V> newDirtyMap = (TreeMap<K, V>) dirtyMap.clone();
		@SuppressWarnings("unchecked")
		TreeMap<K, V> newCleanMap = (TreeMap<K, V>) cleanMap.clone();
		Entry<K, V> newHeld = held == null ? null
				: new Pair<K, V>(held.getKey(), held.getValue());
		WorkMap<K, V> result = new WorkMap<K, V>(newDirtyMap, newCleanMap,
				newHeld);

		return result;
	}
}
