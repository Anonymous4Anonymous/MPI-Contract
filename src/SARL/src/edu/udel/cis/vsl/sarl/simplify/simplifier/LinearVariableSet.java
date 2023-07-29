package edu.udel.cis.vsl.sarl.simplify.simplifier;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;
import edu.udel.cis.vsl.sarl.ideal.IF.IdealFactory;
import edu.udel.cis.vsl.sarl.ideal.IF.Monic;
import edu.udel.cis.vsl.sarl.ideal.IF.Monomial;
import edu.udel.cis.vsl.sarl.util.BinaryPredicate;
import edu.udel.cis.vsl.sarl.util.Pair;
import edu.udel.cis.vsl.sarl.util.TopologicalSorter;

/**
 * <p>
 * A structured representation of a set of {@link Monic}s. The {@link Monic}s
 * are divided into two types: integer and real. In each case, the {@link Monic}
 * s are ordered in an array, the order coming from a specified
 * {@link Comparator} of {@link Monic}s, or from the
 * {@link IdealFactory#monicComparator()}. Each monic is assigned an ID number,
 * unique among the monics of its type in this set. The numbers start from 0.
 * There are methods to go back and forth between the ID numbers and the
 * {@link Monic}s, and other methods.
 * </p>
 * 
 * <p>
 * This representation is built in a series of stages. First, it is instantiated
 * and the set of monics is empty. Then, the client invokes method
 * {@link #addKeys(Set)} some number of times to add {@link Monic}s to this set.
 * There may be repeated entries; entries after the first will simply be
 * ignored. When the client is finished, it must call {@link #finish()}. Then it
 * can use the other methods to get ID numbers, etc.
 * </p>
 * 
 * @author xxxx
 */
public class LinearVariableSet {

	/**
	 * The factory used to get the total order on the {@link Monic}s.
	 */
	private IdealFactory idealFactory;

	/**
	 * The comparator that will be used to order the keys in the maps. This
	 * basically specified the variable ordering for Gaussian elimination. The
	 * variables that are lower in the order (come first), will be expressed as
	 * linear combinations of variables that are higher in the order.
	 */
	Comparator<Monic> monicComparator;

	/**
	 * The set of integer "variables" in the system of linear equations.
	 */
	private Set<Monic> intMonicSet = new HashSet<Monic>();

	/**
	 * The set of real "variables" in the system of linear equations.
	 */
	private Set<Monic> realMonicSet = new HashSet<Monic>();

	/**
	 * The elements of {@link #intMonicSet}, ordered using the total order on
	 * {@link Monic}s provided by the {@link IdealFactory#monicComparator()}.
	 */
	private Monic[] intMonics;

	/**
	 * The elements of {@link #realMonicSet}, ordered using the total order on
	 * {@link Monic}s provided by the {@link IdealFactory#monicComparator()}.
	 */
	private Monic[] realMonics;

	/**
	 * Maps an integer {@link Monic} to its index in the array
	 * {@link #intMonics}.
	 */
	private Map<Monic, Integer> intIdMap;

	/**
	 * Maps a real {@link Monic} to its index in the array {@link #realMonics}.
	 */
	private Map<Monic, Integer> realIdMap;

	public LinearVariableSet(IdealFactory idealFactory,
			Comparator<Monic> monicComparator) {
		this.monicComparator = monicComparator;
		this.idealFactory = idealFactory;
	}

	public LinearVariableSet(IdealFactory idealFactory) {
		this.idealFactory = idealFactory;
		this.monicComparator = idealFactory.monicComparator();
	}

	/**
	 * Extracts the monics that are used in the map and initializes data
	 * structures. The following are initialized: {@link #intMonicSet},
	 * {@link #realMonicSet}, {@link #intMonics}, {@link #realMonics},
	 * {@link #intIdMap}, {@link #realIdMap}.
	 * 
	 * This method should be used when only the keys of the map have monics,
	 * e.g., in the case where the values of the map are constants.
	 * 
	 * @return a pair consisting of the number of integer constraints and the
	 *         number of real constraints
	 */
	public Pair<Integer, Integer> addKeys(Set<Monic> monicSet) {
		int numIntConstraints = 0, numRealConstraints = 0;

		for (Monic key : monicSet) {
			Set<Monic> monics;

			if (key.type().isInteger()) {
				numIntConstraints++;
				monics = intMonicSet;

			} else if (key.type().isReal()) {
				numRealConstraints++;
				monics = realMonicSet;
			} else
				continue;
			for (Monomial term : key.termMap(idealFactory)) {
				Monic monic = term.monic(idealFactory);

				// polynomials should not have constant term:
				assert !monic.isOne();
				monics.add(monic);
			}
		}
		return new Pair<>(numIntConstraints, numRealConstraints);
	}

	/**
	 * Extracts all {@link Monic}s from the {@link Entry}s of a {@link Map}. A
	 * "usable" entry consists of a {@link Monic} key and a {@link Monomial}
	 * value. Entries of other types are ignored.
	 * 
	 * Preconditions: a {@link Monic} key should not have a constant term
	 * 
	 * @param entrySet
	 *            a collection of entries from a substitution map
	 * @return a pair in which the first component is the number of usable
	 *         {@link Entry}s of integer type, and the second component is the
	 *         number of usable {@link Entry}s of real type.
	 */
	public Pair<Integer, Integer> addEntries(
			Collection<Entry<SymbolicExpression, SymbolicExpression>> entrySet) {
		int numIntConstraints = 0, numRealConstraints = 0;

		for (Entry<SymbolicExpression, SymbolicExpression> entry : entrySet) {
			SymbolicExpression key = entry.getKey(), value = entry.getValue();

			if (!(key instanceof Monic) || !(value instanceof Monomial))
				continue;

			SymbolicType type = key.type();
			Set<Monic> monics;

			if (type.isInteger()) {
				numIntConstraints++;
				monics = intMonicSet;
			} else if (type.isReal()) {
				numRealConstraints++;
				monics = realMonicSet;
			} else
				continue;
			for (Monomial term : ((Monic) key).termMap(idealFactory)) {
				Monic monic = term.monic(idealFactory);

				// a key should not have a constant term:
				assert !monic.isOne();
				monics.add(monic);
			}
			for (Monomial term : ((Monomial) value).termMap(idealFactory)) {
				Monic monic = term.monic(idealFactory);

				if (!monic.isOne())
					monics.add(monic);
			}
		}
		return new Pair<>(numIntConstraints, numRealConstraints);
	}

	/**
	 * Sort and organize the monics that have been added to this set. Should be
	 * called only after all keys have been added using method
	 * {@link #addKeys(Set)}.
	 */
	public void finish(boolean backwardsSub) {
		int numIntMonics, numRealMonics, i;

		numIntMonics = intMonicSet.size();
		numRealMonics = realMonicSet.size();
		intMonics = new Monic[numIntMonics];
		realMonics = new Monic[numRealMonics];
		intIdMap = new HashMap<Monic, Integer>(numIntMonics);
		realIdMap = new HashMap<Monic, Integer>(numRealMonics);

		i = 0;
		for (Monic monic : intMonicSet)
			intMonics[i++] = monic;
		i = 0;
		for (Monic monic : realMonicSet)
			realMonics[i++] = monic;
		Arrays.sort(intMonics, monicComparator);
		Arrays.sort(realMonics, monicComparator);

		// if doing backwards sub, then need to
		// ensure that if m1 is a sub-object of m2, then
		// m1 does NOT occur before m2. Otherwise
		// you will end up solving for m1 in terms
		// of monics containing m1, and the substitution
		// map will have a cycle!

		if (backwardsSub) {
			BinaryPredicate<Monic> superObj = new BinaryPredicate<Monic>() {

				@Override
				public boolean apply(Monic x, Monic y) {
					return x.containsSubobject(y);
				}

			};
			TopologicalSorter<Monic> sorter = new TopologicalSorter<>(superObj);

			sorter.sort(intMonics);
			sorter.sort(realMonics);
		}
		for (i = 0; i < numIntMonics; i++)
			intIdMap.put(intMonics[i], i);
		for (i = 0; i < numRealMonics; i++)
			realIdMap.put(realMonics[i], i);
	}

	/**
	 * Computes the number of {@link Monic}s of real type in this set.
	 * 
	 * @return the number of {@link Monic}s of real type in this set
	 */
	public int numRealMonics() {
		return realMonics.length;
	}

	/**
	 * Computes the number of {@link Monic}s of integer type in this set.
	 * 
	 * @return the number of {@link Monic}s of integer type in this set
	 */
	public int numIntMonics() {
		return intMonics.length;
	}

	/**
	 * Given a {@link Monic} {@code key} of integer type in this set, returns
	 * its ID number.
	 * 
	 * @param key
	 *            a {@link Monic} of integer type that has been added to this
	 *            set
	 * @return the ID number of {@code key}
	 */
	public int getIntId(Monic key) {
		return intIdMap.get(key);
	}

	/**
	 * Given a {@link Monic} {@code key} of real type in this set, returns its
	 * ID number.
	 * 
	 * @param key
	 *            a {@link Monic} of real type that has been added to this set
	 * @return the ID number of {@code key}
	 */
	public int getRealId(Monic key) {
		return realIdMap.get(key);
	}

	/**
	 * Returns the array of all {@link Monic}s of integer type in this set,
	 * sorted by increasing order of {@link Monic}. This set is backed by the
	 * array, so don't modify the array.
	 * 
	 * @return the sorted array of integer {@link Monic}s in this set
	 */
	public Monic[] getIntMonics() {
		return intMonics;
	}

	/**
	 * Returns the array of all {@link Monic}s of real type in this set, sorted
	 * by increasing order of {@link Monic}. This set is backed by the array, so
	 * don't modify the array.
	 * 
	 * @return the sorted array of real {@link Monic}s in this set
	 */
	public Monic[] getRealMonics() {
		return realMonics;
	}
}
