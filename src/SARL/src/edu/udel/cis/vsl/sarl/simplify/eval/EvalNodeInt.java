package edu.udel.cis.vsl.sarl.simplify.eval;

import java.math.BigInteger;

/**
 * The parent of all {@link EvalNodeInt} nodes
 * 
 * @author xxxx
 *
 */
public abstract class EvalNodeInt extends EvalNode<BigInteger> {
	@Override
	abstract BigInteger evaluate();
}
