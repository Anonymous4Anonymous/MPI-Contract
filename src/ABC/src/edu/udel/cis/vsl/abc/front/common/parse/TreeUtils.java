package edu.udel.cis.vsl.abc.front.common.parse;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;

import edu.udel.cis.vsl.abc.token.IF.CivlcToken;

/**
 * General utility class for operations on ANTLR Trees.
 * 
 * @author xxxx
 *
 */
public class TreeUtils {

	/**
	 * <p>
	 * Sets some fields of the tokens that occur in the tree.
	 * </p>
	 * 
	 * <p>
	 * I know ANTLR is supposed to do this but I don't think it does it right.
	 * First, the tokenIndexes are not always what I expect. For some reason,
	 * ANTLR's CommonTokenStream sets the index of the last token (EOF) to be
	 * one higher than it should be, so there is a gap in the indexes between
	 * the penultimate token and the last token. I introduced my own "index"
	 * field to CToken (which extends CommonToken) and set it myself correctly.
	 * </p>
	 * 
	 * <p>
	 * Second, ANTLR is supposed to find the range of tokens spanned by each
	 * node in the tree (by examining all descendants of the node). However:
	 * first, the code that does this uses ANTLR's tokenIndex, and I want to do
	 * it using my index. Second, the ANTLR code is only correct under the
	 * assumption that the token indices are non-decreasing as child index
	 * increases, i.e., the token index of child i is less than or equal to that
	 * of child i+1, for all i, for all nodes. (Hence it only has to examine the
	 * first and last child.) There is no reason that assumption has to hold. So
	 * I compute this correctly (and using CToken indexes) and re-set the
	 * "tokenStartIndex" and "tokenStopIndex" fields of each tree node.
	 * </p>
	 * 
	 * @param tree
	 *            a tree resulting from executing an ANTLR parser
	 */
	public static void postProcessTree(CommonTree tree) {
		initPostProcess(tree);
		completePostProcess(tree);
	}

	/**
	 * Marks all nodes as "not yet visited"---indicating by the magic number
	 * -999 for tokenStartIndex and tokenStopIndex.
	 * 
	 * @param tree
	 *            root of tree to be explored
	 */
	private static void initPostProcess(CommonTree tree) {
		int numChildren = tree.getChildCount();

		tree.setTokenStartIndex(-999);
		tree.setTokenStopIndex(-999);
		for (int i = 0; i < numChildren; i++)
			initPostProcess((CommonTree) tree.getChild(i));
	}

	/**
	 * <p>
	 * Computes the actual start and stop index of each node in the tree.
	 * </p>
	 * 
	 * <p>
	 * Precondition: nodes reachable from <code>tree</code> have not yet been
	 * visited by this method iff their start and stop indexes are both -999.
	 * </p>
	 * 
	 * <p>
	 * If there is no CToken occurring in a node or any of its descendants, the
	 * start and stop index of that node will both be set to -1.
	 * </p>
	 * 
	 * @param tree
	 *            root of the ANTLR tree that is to be processed
	 */
	private static void completePostProcess(CommonTree tree) {
		if (tree.getTokenStartIndex() != -999)
			return;
		else {
			int numChildren = tree.getChildCount();
			Token token = tree.getToken();
			int min, max;

			if (token instanceof CivlcToken) {
				min = max = ((CivlcToken) token).getIndex();
			} else {
				min = max = -1;
			}
			for (int i = 0; i < numChildren; i++) {
				CommonTree child = (CommonTree) tree.getChild(i);
				int childMin, childMax;

				completePostProcess(child);
				childMin = getSubTreeStartIndex(child);
				childMax = getSubTreeStopIndex(child);
				if (childMin >= 0 && (min < 0 || childMin < min))
					min = childMin;
				if (childMax >= 0 && (max < 0 || childMax > max))
					max = childMax;
			}
			tree.setTokenStartIndex(min);
			tree.setTokenStopIndex(max);
		}
	}

	private static int getSubTreeStartIndex(CommonTree tree) {
		int index = tree.getTokenStartIndex();
		Token token = tree.getToken();

		if (token != null && index == token.getTokenIndex()
				&& !(token instanceof CivlcToken)) {
			index = -1;
		}
		return index;
	}

	private static int getSubTreeStopIndex(CommonTree tree) {
		int index = tree.getTokenStopIndex();
		Token token = tree.getToken();

		if (token != null && index == token.getTokenIndex()
				&& !(token instanceof CivlcToken)) {
			index = -1;
		}
		return index;
	}

}
