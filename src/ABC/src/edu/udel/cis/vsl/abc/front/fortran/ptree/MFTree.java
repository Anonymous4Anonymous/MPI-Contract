/**
 * 
 */
package edu.udel.cis.vsl.abc.front.fortran.ptree;

import java.util.ArrayList;
import java.util.Collection;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;

import edu.udel.cis.vsl.abc.config.IF.Configurations.Language;
import edu.udel.cis.vsl.abc.front.IF.ParseTree;
import edu.udel.cis.vsl.abc.front.fortran.ptree.MFPUtils.PRPair;
import edu.udel.cis.vsl.abc.token.IF.CivlcToken;
import edu.udel.cis.vsl.abc.token.IF.CivlcTokenSequence;
import edu.udel.cis.vsl.abc.token.IF.CivlcTokenSource;
import edu.udel.cis.vsl.abc.token.IF.Source;
import edu.udel.cis.vsl.abc.token.IF.SourceFile;
import edu.udel.cis.vsl.abc.token.IF.SyntaxException;
import edu.udel.cis.vsl.abc.token.IF.TokenFactory;
import edu.udel.cis.vsl.abc.token.IF.Tokens;
import edu.udel.cis.vsl.abc.token.common.CommonCivlcTokenSource;

/**
 * @author Wenhao Wu
 *
 */
public class MFTree implements ParseTree {
	private static MFTree ABSENT_TREE = new MFTree(MFPUtils.ABSENT);

	private static boolean TEXT_ONLY = false;

	private static long NUM_NODE = 0;

	@SuppressWarnings("unused")
	private long index;

	private int childIndex;

	private int kind;

	private PRPair prp;

	private String suffix;

	private MFTree parent;

	private ArrayList<MFTree> children;

	private CivlcToken[] srcTokens;

	public MFTree(PRPair pair, CivlcToken... tokens) {
		this(pair, "", -1, tokens);
	}

	public MFTree(PRPair pair, String suffix, CivlcToken... tokens) {
		this(pair, suffix, -1, tokens);
	}

	public MFTree(PRPair pair, int kind, CivlcToken... tokens) {
		this(pair, "", kind, tokens);
	}

	public MFTree(PRPair pair, String suffix, int kind, CivlcToken... tokens) {
		this.index = NUM_NODE++;
		this.childIndex = -1;
		this.kind = kind;
		this.prp = pair;
		this.suffix = suffix;
		this.parent = null;
		this.children = new ArrayList<MFTree>();
		if (tokens.length > 0 && tokens[0] != null)
			this.srcTokens = (CivlcToken[]) tokens;
		else
			this.srcTokens = new CivlcToken[0];
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		// Append tree-structure prefix
		MFTree tmp = this;
		while (tmp.parent != null) {
			tmp = tmp.parent;
			sb.append("| ");
		}
		sb.append('{');
		if (parent == null)
			sb.append("ROOT");
		else
			sb.append("[" + childIndex + "]");
		sb.append(": [");
		if (prp.getRule() != Integer.MIN_VALUE)
			sb.append(prp.getRule() + " ");
		sb.append(prp.getName() + suffix + "]");
		if (srcTokens != null && srcTokens.length > 0) {
			sb.append('<');
			for (CivlcToken t : srcTokens)
				if (t != null) {
					if (TEXT_ONLY)
						sb.append(t.getText());
					else
						sb.append(t.toString());
				} else {
					sb.append("NULL");
				}
			sb.append('>');
		}
		sb.append("}\n");
		if (children.size() > 0)
			for (MFTree child : children)
				if (child != null)
					sb.append(child.toString());
		return sb.toString();
	}

	@Override
	public Language getLanguage() {
		return Language.FORTRAN;
	}

	@Override
	public CommonTree getRoot() {
		return null;
	}

	@Override
	public Source source(CommonTree tree) {
		assert false;
		return null;
	}

	@Override
	public Collection<SourceFile> getSourceFiles() {
		assert false;
		return null;
	}

	@Override
	public SyntaxException newSyntaxException(String message, CommonTree tree) {
		assert false;
		return null;
	}

	@Override
	public CivlcTokenSource getCivlcTokenSource() {
		assert false;
		return null;
	}

	@Override
	public TokenFactory getTokenFactory() {
		return Tokens.newTokenFactory();
	}

	@Override
	public CivlcTokenSequence getTokenSourceProducer(CommonTree tokenListNode) {
		assert false;
		return null;
	}

	// Deleted if not called.
	public CivlcTokenSequence getTokenSourceProducer(MFTree tokenListNode) {
		int numChildren = tokenListNode.numChildren();
		TokenFactory tF = getTokenFactory();

		if (numChildren == 0)
			return tF.getEmptyTokenSubsequence(getCivlcTokenSource());
		else {
			ArrayList<CivlcToken> tokens = new ArrayList<CivlcToken>();

			for (int i = 0; i < numChildren; i++)
				tokens.add(tokenListNode.getChildByIndex(i).srcTokens[0]);

			CivlcToken startToken = tokens.get(0);
			CivlcToken stopToken = tokens.get(numChildren - 1);

			startToken.setIndex(0);
			stopToken.setIndex(numChildren - 1);
			return tF.getTokenSubsequence(
					new CommonCivlcTokenSource(tokens, tF), startToken,
					stopToken);
		}
	}

	public PRPair prp() {
		return prp;
	}

	public int rule() {
		return prp.getRule();
	}

	public int kind() {
		return kind;
	}

	public int numChildren() {
		return children.size();
	}

	public MFTree getChildByIndex(int i) {
		return children.get(i);
	}

	public MFTree getParent() {
		return parent;
	}

	public int addChild(MFTree newChild) {
		assert newChild != null;
		assert newChild.parent == null;

		int index = children.size();

		newChild.childIndex = index;
		newChild.parent = this;
		children.add(newChild);

		return index;
	}

	public void addChild(int index, MFTree newChild) {
		assert newChild != null;
		assert newChild.parent == null;
		assert index >= 0 && index <= children.size();

		newChild.parent = this;
		newChild.childIndex = index;
		children.add(index, newChild);
		index++;
		while (index < children.size()) {
			children.get(index).childIndex++;
			index++;
		}
	}

	public void addChildren(MFTree... newChildren) {
		assert newChildren != null;
		assert newChildren.length > 0;
		for (MFTree child : newChildren) {
			assert child.parent == null;
			child.childIndex = children.size();
			child.parent = this;
			children.add(child);
		}
	}

	public MFTree setChild(int index, MFTree newChild) {
		assert newChild.parent == null;
		assert children.size() > index;

		MFTree oldChild = children.get(index);

		oldChild.release();
		newChild.parent = this;
		newChild.childIndex = index;
		children.set(index, newChild);
		return oldChild;
	}

	public void release() {
		assert parent != null;
		parent.children.set(childIndex, ABSENT_TREE);
		parent = null;
		childIndex = -1;
	}

	public CivlcToken[] cTokens() {
		return srcTokens;
	}

	public boolean isNullToken(int index) {
		return srcTokens.length < 1 || srcTokens[index] == null;
	}

	/* * * * Deprecated * * * */

	public MFTree(String name, Token... cTokens) {
		assert false;
	}

	public MFTree(int rule, String name, Token... cTokens) {
		assert false;
	}

	public MFTree(int rule, String name, int type, Token... cTokens) {
		assert false;
	}

	public void setNodeName(String nodeName) {
		srcTokens[0].setText(nodeName);
	}

	public void setRule(int rule) {
		assert false;
	}

	/*
	 * 
	 * public int childIndex() { return childIndex; }
	 * 
	 * public void setChildIndex(int newIndex) { childIndex = newIndex; }
	 * 
	 * public void setTokens(CivlcToken... cTokens) { this.cTokens = cTokens; }
	 * 
	 * 
	 * public String nodeName() { return nodeName; }
	 * 
	 * 
	 * public MFTree parent() { return parent; }
	 * 
	 * public void setParent(MFTree parent) { this.parent = parent; }
	 * 
	 * public Iterable<MFTree> children() { return children; }
	 * 
	 * public MFTree setChild(int index, MFTree newChild) { assert newChild !=
	 * null; assert newChild.parent == null;
	 * 
	 * MFTree oldChild = null; int numChildren = children.size();
	 * 
	 * assert index >= 0 && index < numChildren; index = Math.min(index,
	 * numChildren - 1); index = Math.max(index, 0); oldChild =
	 * children.get(index); oldChild.parent = null; oldChild.childIndex = -1;
	 * newChild.parent = this; newChild.childIndex = index; children.set(index,
	 * newChild); return oldChild; }
	 * 
	 * public void insertChildrenAt(int start, List<? extends MFTree>
	 * newChildren) { int i = 0; int oldSize = children.size(); int
	 * numNewChildren = newChildren.size(); int newSize = oldSize +
	 * numNewChildren;
	 * 
	 * assert start >= 0 && start <= oldSize; children.addAll(start,
	 * newChildren); for (i = start; i < start + numNewChildren; i++) { MFTree
	 * newChild = children.get(i);
	 * 
	 * assert newChild != null; assert newChild.parent == null; newChild.parent
	 * = this; newChild.childIndex = i; } for (; i < newSize; i++) { MFTree
	 * child = children.get(i);
	 * 
	 * assert child != null; child.childIndex = i; } }
	 * 
	 * public void remove() { if (children != null)
	 * parent.removeChild(childIndex); }
	 * 
	 * public MFTree removeChild(int index) { int numChildren = children.size();
	 * MFTree oldChild = null;
	 * 
	 * assert index >= 0 && index < numChildren; oldChild = children.get(index);
	 * assert oldChild != null; oldChild.parent = null; oldChild.childIndex =
	 * -1; children.remove(index); return oldChild; }
	 * 
	 * 
	 * public void printNode() { System.out.print(this.toString()); }
	 * 
	 * 
	 */
}
