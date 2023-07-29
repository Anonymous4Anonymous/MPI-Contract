package edu.udel.cis.vsl.abc.analysis.dataflow.common;

//import edu.udel.cis.vsl.sarl.number.real.RealNumberFactory;
import edu.udel.cis.vsl.sarl.IF.number.IntegerNumber;
import edu.udel.cis.vsl.sarl.IF.number.Interval;

import edu.udel.cis.vsl.sarl.IF.number.NumberFactory;
import edu.udel.cis.vsl.sarl.IF.number.NumberFactory.IntervalUnion;
import edu.udel.cis.vsl.sarl.number.IF.Numbers;

/**
 * Another implementation of Interval using SARL to do the calculation;
 * 
 * @author dxu
 */

public class IntervalSARL{
	private static NumberFactory numFactory = Numbers.REAL_FACTORY;
	Interval value;
	
	public IntervalSARL(){
//		numFactory = new RealNumberFactory();
	}
	
	public Interval createEmptyInterval(){
		return numFactory.emptyIntegerInterval();
	}

	public Interval createUniversalInterval(){
		return numFactory.universalIntegerInterval();
	}
	
	public Interval createInterval(long num){
		IntegerNumber intNum = numFactory.integer(num);
		Interval a = numFactory.newInterval(true, intNum, false, intNum, false);
		return a;
	}
	
	public Interval intersect(Interval i1, Interval i2){
		return numFactory.intersection(i1, i2);
	}
	
	public Interval union(Interval i1, Interval i2){
		IntervalUnion result = new IntervalUnion();
		numFactory.union(i1, i2, result);
		return result.union;
	}
	
	public Interval plus(Interval i1, Interval i2){
		return numFactory.add(i1, i2);
	}
	
	public Interval minus(Interval i1, Interval i2){
		return numFactory.add(i1, i2);
	}
	
	public Interval multiply(Interval i1, Interval i2){
		return numFactory.multiply(i1, i2);
	}
	
	public Interval divide(Interval i1, Interval i2){
		return numFactory.divide(i1, i2);
	}
}
