package edu.udel.cis.vsl.sarl.simplify.norm;

import java.io.PrintStream;
import java.util.Set;

import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;
import edu.udel.cis.vsl.sarl.simplify.simplifier.InconsistentContextException;
import edu.udel.cis.vsl.sarl.simplify.simplifier.SimplifierUtility;

public class NormalizerChain implements Normalizer {

	// private static int globalID = 0; // for debugging
	//
	// private static int globalDepth = 0; // for debugging

	public static boolean debug = false;

	public final static PrintStream out = System.out;

	private Normalizer[] members;

	public NormalizerChain(Normalizer... members) {
		this.members = members;
	}

	@Override
	public void normalize(Set<SymbolicConstant> dirtyIn,
			Set<SymbolicConstant> dirtyOut)
			throws InconsistentContextException {
		// int id = globalID++; // make me atomic
		//
		// if (debug) {
		// globalDepth++;
		// out.println("Starting normalization " + id + " at depth "
		// + globalDepth);
		// }
		int n = members.length;
		@SuppressWarnings("unchecked")
		Set<SymbolicConstant>[] dirts = new Set[n];
		boolean hasDirt = true;
		Set<SymbolicConstant> tmp = SimplifierUtility.newDirtySet();
		// int cycleCount = 0; // for debugging

		for (int i = 0; i < n; i++)
			dirts[i] = SimplifierUtility.cloneDirtySet(dirtyIn);
		while (hasDirt) {
			// if (debug) {
			// out.println("Normalization " + id + " cycle " + cycleCount);
			// cycleCount++;
			// }
			hasDirt = false;
			for (int i = 0; i < n; i++) {
				if (!dirts[i].isEmpty()) {
					members[i].normalize(dirts[i], tmp);
					dirts[i].clear();
					if (!tmp.isEmpty()) {
						hasDirt = true;
						for (int j = 0; j < n; j++)
							if (j != i)
								dirts[j].addAll(tmp);
						dirtyOut.addAll(tmp);
						tmp.clear();
					}
				}
			}
		}
		// if (debug) {
		// out.println("Finishing normalization " + id);
		// globalDepth--;
		// }
	}

}
