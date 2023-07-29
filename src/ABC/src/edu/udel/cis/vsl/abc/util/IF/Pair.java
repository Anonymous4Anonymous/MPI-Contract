package edu.udel.cis.vsl.abc.util.IF;

/*
 * Enriched this type to make it more useful in collections.
 */
public class Pair<S, T> {

	public S left;

	public T right;

	public Pair(S left, T right) {
		this.left = left;
		this.right = right;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null) return false;
		
		if (o instanceof Pair) {
			@SuppressWarnings("unchecked")
			final Pair<S, T> other = (Pair<S, T>) o;
			
			// Partial nullity of pairs means inequality
			if (this.left == null && other.left != null) {
				return false;
			} else if (this.left != null && other.left == null) {
				return false;
			}
			
			if (this.right == null && other.right != null) {
				return false;
			} else if (this.right != null && other.right == null) {
				return false;
			}
			
			boolean leftEqual = (this.left == null && other.left == null) || this.left.equals(other.left);
			boolean rightEqual = (this.right == null && other.right == null) || this.right.equals(other.right);
			
			return leftEqual && rightEqual;
		}
		return false;
	}

	@Override
	public int hashCode() {
		int leftHash = (this.left == null) ? 0 : this.left.hashCode();
		int rightHash = (this.right == null) ? 0 : this.right.hashCode();
		return leftHash + rightHash;
	}

	@Override
	public String toString() {
		return "<" + this.left + ", " + this.right + ">";
	}

}
