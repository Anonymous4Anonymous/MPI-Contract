package edu.udel.cis.vsl.sarl.prove.cvc;

import java.util.ArrayList;
import java.util.List;

import edu.udel.cis.vsl.sarl.util.FastList;

/**
 * <p>
 * This class is used to deal with integer division or modulo during the CVC
 * translation. Since CVC4 currently can not deal with integer division or
 * modulo, integer division and modulo operations will be translated into an
 * "exists" expression. Below is an example:
 * </p>
 * 
 * <p>
 * z = x%y will become exists t__0, t__1 : z = t__1 && (y*t__0 + t__1 = x) &&
 * (t__1 >= 0 && t__1 < y)
 * </p>
 * 
 * <p>
 * z = x/y will become exists t__0, t__1 : z = t__0 && (y*t__0 + t__1 = x) &&
 * (t__1 >= 0 && t__1 < y)
 * </p>
 * 
 * <p>
 * Note that SARL assumes that all operands in integer division and modulo
 * operations are non-negative.
 * </p>
 * 
 * @author xxxxxxxx
 * 
 */
public class Translation {
	/**
	 * Stores the translation result.
	 */
	private FastList<String> result;

	/**
	 * Is the translation coming from division or modulo?
	 */
	private Boolean isDivOrModulo;

	/**
	 * If the translation comes from integer division or modulo, auxiliary
	 * constraints must be added to the translation. Those constraints are
	 * stored here as a single {@link FastList}. E.g., the auxiliary constraints
	 * from x%y will be: (y*t__0 + t__1 = x) && (t__1 >= 0 && t__1 < y).
	 */
	private FastList<String> auxConstraints;

	/**
	 * <p>
	 * Store all the auxiliary vars generated and used in the result and side
	 * effects.
	 * </p>
	 * 
	 * <p>
	 * e.g. z = x/y becomes exist t__0, t__1 : z = t__0 && (y*t__0 + t__1 = x)
	 * && (t__1 >= 0 && t__1 < y). t__0, t__1 will be two generated auxiliary
	 * variables.
	 * </p>
	 */
	private List<FastList<String>> auxVars;

	public Translation() {
		result = new FastList<String>();
		isDivOrModulo = false;
		auxConstraints = new FastList<>();
		auxVars = new ArrayList<>();
	}

	public Translation(FastList<String> res) {
		result = res;
		isDivOrModulo = false;
		auxConstraints = new FastList<>();
		auxVars = new ArrayList<>();
	}

	public Translation(FastList<String> res, Boolean isDivOrModulo,
			FastList<String> auxConstraints, List<FastList<String>> auxVars) {
		result = res;
		this.isDivOrModulo = isDivOrModulo;
		this.auxConstraints = auxConstraints;
		this.auxVars = auxVars;
	}

	public FastList<String> getResult() {
		return result;
	}

	public Boolean getIsDivOrModulo() {
		return isDivOrModulo;
	}

	public void setIsDivOrModulo(Boolean isDivOrModulo) {
		this.isDivOrModulo = isDivOrModulo;
	}

	public FastList<String> getAuxConstraints() {
		return auxConstraints;
	}

	public void setAuxConstraints(FastList<String> auxConstraints) {
		this.auxConstraints = auxConstraints;
	}

	public List<FastList<String>> getAuxVars() {
		return auxVars;
	}

	public void setAuxVars(List<FastList<String>> auxVars) {
		this.auxVars = auxVars;
	}

	public Translation clone() {
		FastList<String> constraints = this.auxConstraints == null ? null
				: this.auxConstraints.clone();
		Translation translation = new Translation(this.result.clone(),
				this.isDivOrModulo, constraints, this.auxVars);

		return translation;
	}

	@Override
	public String toString() {
		return result.toString();
	}
}
