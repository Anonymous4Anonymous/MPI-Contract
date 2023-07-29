package edu.udel.cis.vsl.sarl.simplify.eval;

/**
 * The parent of all {@link EvalNodeRat} nodes
 * 
 * @author xxxx
 *
 */
public abstract class EvalNodeRat extends EvalNode<Rat> {
	@Override
	abstract Rat evaluate();
}
