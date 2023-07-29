package edu.udel.cis.vsl.civl.model.common.contract;

import java.util.List;

import edu.udel.cis.vsl.civl.model.IF.CIVLSource;
import edu.udel.cis.vsl.civl.model.IF.contract.LoopContract;
import edu.udel.cis.vsl.civl.model.IF.expression.Expression;
import edu.udel.cis.vsl.civl.model.IF.expression.LHSExpression;
import edu.udel.cis.vsl.civl.model.IF.location.Location;

public class CommonLoopContract implements LoopContract {
	/**
	 * A {@link CIVLSource} attached to the loop contract
	 */
	private CIVLSource civlSource;

	/**
	 * The location identifies a loop statement. This should be the location
	 * where the loop statement begins.
	 */
	private Location loopLocation;

	private Expression[] loopInvariants;

	private LHSExpression[] loopAssigns;

	private Expression[] loopVariants;

	public CommonLoopContract(CIVLSource civlSource, Location loopLocation,
			List<Expression> loopInvariants, List<LHSExpression> loopAssigns,
			List<Expression> loopVariants) {
		this.loopInvariants = loopInvariants == null ? new Expression[0]
				: new Expression[loopInvariants.size()];
		this.loopAssigns = loopAssigns == null ? new LHSExpression[0]
				: new LHSExpression[loopAssigns.size()];
		this.loopVariants = loopVariants == null ? new Expression[0]
				: new Expression[loopVariants.size()];

		int count;

		count = 0;
		for (Expression item : loopInvariants)
			this.loopInvariants[count++] = item;
		count = 0;
		for (LHSExpression item : loopAssigns)
			this.loopAssigns[count++] = item;
		count = 0;
		for (Expression item : loopVariants)
			this.loopVariants[count++] = item;
	}

	@Override
	public CIVLSource getSource() {
		return civlSource;
	}

	@Override
	public void setCIVLSource(CIVLSource source) {
		this.civlSource = source;
	}

	@Override
	public Expression[] loopInvariants() {
		return loopInvariants;
	}

	@Override
	public LHSExpression[] loopAssigns() {
		return loopAssigns;
	}

	@Override
	public Expression[] loopVariants() {
		return loopVariants;
	}

	@Override
	public Location loopLocation() {
		return loopLocation;
	}

	@Override
	public void setLocation(Location loopLocation) {
		this.loopLocation = loopLocation;
	}
}
