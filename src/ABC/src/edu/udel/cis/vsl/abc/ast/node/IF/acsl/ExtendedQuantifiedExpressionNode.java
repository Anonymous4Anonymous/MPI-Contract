package edu.udel.cis.vsl.abc.ast.node.IF.acsl;

import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;

/**
 * This represents an ACSL extended quantification expression.
 * 
 * Section 2.6.7 from ACSL standards
 * 
 * Extended quantifiers Terms <code>\quant(t1,t2,t3)</code> where quant is
 * <code>max</code>, <code>min</code>, <code>sum</code>, <code>product</code> or
 * <code>numof</code> are extended quantifications. <code>t1</code> and
 * <code>t2</code> must have type integer, and <code>t3</code> must be a unary
 * function with an integer argument, and a numeric value (integer or real)
 * except for <code>\numof</code> for which it should have a boolean value.
 * Their meanings are given as follows:
 * 
 * <pre>
 * \max(i,j,f) = max{f(i), f(i+1), ..., f(j)}
 * \min(i,j,f) = min{f(i), f(i+1), ..., f(j)}
 * \sum(i,j,f) = f(i) + f(i+1) + ... + f(j)
 * \product(i,j,f) = f(i) * f(i+1) * ... * f(j)
 * \numof(i,j,f) = #{k | i<=k<=j ^ f(k)} = \sum(i, j, \lambda integer k ; f(k) ? 1 : 0)
 * </pre>
 * 
 * If <code>i>j</code> then <code>\sum</code> and <code>\numof</code> above are
 * 0, <code>\product</code> is 1,and <code>\max</code> and <code>\min</code> are
 * unspecified.
 * 
 * @author Manchun Zheng
 *
 */
public interface ExtendedQuantifiedExpressionNode extends ExpressionNode {
	public enum ExtendedQuantifier {
		MAX, MIN, SUM, PROD, NUMOF;
		@Override
		public String toString() {
			switch (this) {
			case MAX:
				return "\\max";
			case MIN:
				return "\\min";
			case SUM:
				return "\\sum";
			case PROD:
				return "\\product";
			case NUMOF:
				return "\\numof";
			default:
				return super.toString();
			}
		}

		public String type() {
			switch (this) {
			case MAX:
			case MIN:
			case SUM:
			case PROD:
				return "integer-to-numeric type";
			case NUMOF:
				return "integer-to-boolean type";
			default:
				throw new IllegalArgumentException(
						"unknown extended quantifier " + toString());
			}
		}
	}

	/**
	 * return the lower bound
	 * 
	 * @return return the lower bound
	 */
	ExpressionNode lower();

	/**
	 * return the higher bound
	 * 
	 * @return return the higher bound
	 */
	ExpressionNode higher();

	/**
	 * return the function
	 * 
	 * @return return the function
	 */
	ExpressionNode function();

	/**
	 * returns the extended quantifier of this expression
	 * 
	 * @return the extended quantifier of this expression
	 */
	ExtendedQuantifier extQuantifier();
}
