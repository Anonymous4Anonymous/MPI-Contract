package edu.udel.cis.vsl.abc.analysis.dataflow.common;

import java.util.Arrays;
import java.util.Collections;

public class MyInterval {
	private final Long low;
	private final Long high;

	public MyInterval(Long value) {
		this.low  = value;
		this.high = value;

		assert isValidInterval(): "Not a valid interval";
	}

	public MyInterval(Long low, Long high) {
		this.low  = low;
		this.high = high;

		assert isValidInterval(): "Not a valid interval";
	}

	public boolean isValidInterval(){
		if ((low == null) != (high == null)) {
			return false;
		}
		if (low != null && low > high)
			return false;

		return true;
	}

	public Long getLow() {
		return low;
	}

	public Long getHigh() {
		return high;
	}

	public MyInterval union(MyInterval other) {
		if (isEmpty() || other.isEmpty()) {
			return createEmptyInterval();
		} else if (low >= other.low && high <= other.high) {
			return other;
		} else {
			return new MyInterval(Math.min(low, other.low), Math.max(high, other.high));
		}
	}

	public MyInterval intersect(MyInterval other) {
		if (this.intersects(other)) {
			return new MyInterval(Math.max(low, other.low), Math.min(high, other.high));
		} else {
			return createEmptyInterval();
		}
	}

	public boolean intersects(MyInterval other) {
		if (isEmpty() || other.isEmpty()) {
			return false;
		}

		return (low >= other.low && low <= other.high)
				|| (high >= other.low && high <= other.high)
				|| (low <= other.low && high >= other.high);
	}

	public boolean contains(int i){
		if(this.low <= i && this.high >= i)
			return true;
		return false;
	}
	
	public boolean contains(MyInterval other) {
		return (!isEmpty() && !other.isEmpty()
				&& low <= other.low && other.high <= high);
	}

	public boolean isEmpty() {
		return low == null && high == null;
	}

	private static MyInterval createEmptyInterval() {
		return new MyInterval(null,null);
	}

	public MyInterval plus(MyInterval i){
		return new MyInterval(this.low + i.low, this.high + i.high);
	}
	
	public MyInterval minus(MyInterval i){
		return new MyInterval(this.low - i.high, this.high -i.low);
	}
	
	public MyInterval multiply(MyInterval i){
	    Long[] values = {
	    				low* i.low,
	    				low, i.high,
                		high, i.low,
                high, i.high
              };

	    return new MyInterval(Collections.min(Arrays.asList(values)), Collections.max(Arrays.asList(values)));
	}
	
	public MyInterval divide(MyInterval i){
	    if (i.contains(0)) {
	        return new MyInterval(Long.MIN_VALUE, Long.MAX_VALUE);
	      } else {
	        Long[] values = {
	                          low / i.low,
	                          low / i.high,
	                          high / i.low,
	                          high / i.high
	                        };

	        return new MyInterval(Collections.min(Arrays.asList(values)), Collections.max(Arrays.asList(values)));
	      }
	}

	@Override
	public String toString() {
		return "[" + (low == null ? "" : low) + "; " + (high == null ? "" : high) + "]";
	}

	public MyInterval modulo(MyInterval rightValue) {
		return null;	
	}

}
