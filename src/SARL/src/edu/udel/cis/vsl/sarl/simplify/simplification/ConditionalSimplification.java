package edu.udel.cis.vsl.sarl.simplify.simplification;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import edu.udel.cis.vsl.sarl.IF.SARLInternalException;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.simplify.simplifier.IdealSimplifierWorker;

/**
 * Used to simplify a conditional symbolic expression p?a:b.
 * 
 * NEED BETTER WAY?  p?(q?a:b):c ==> p&&q ? a : (p&&!q) ? b : c
 * 
 * Algorithm:
 * 
 * <pre>
Simplification of conditional expressions will be carried out using an
intermediate form:

p1 : a1
p2 : a2
...
pn : an

where the pi partition true. I.e., in the given context, pi&&pj is
equivalent to false, and p1||p2||...||pn is equivalent to true. In the
intermediate form, the entries pi:ai are unordered (could be a map).

Conversion of a nested conditional expression to intermediate form is
carried out as follows:

Rule 0: p?a:b ==>
p  : a
!p : b


Canonicalization of the intermediate form is carried out as follows:
Carry out the following rules until they no longer apply:

Rule 1:
p : q?a:b
==>
p&&q   : a
p&&!q  : b

Rule 2: simplify sub-expressions:
p : a
==>
simplify(p) : simplify(a)

Rule 3: combine all entries which share a common value:
p     : a
q     : a
==>
p||q  : a

Rule 4: Remove any entry of the form false : a.

In the end, no entry right side will be a conditional expression.
No value will occur more than once on the right side.
All keys and values will be simplified.

Algorithm:
  start(p?a:b) : enter(p,a); enter(!p,b);
  enter(p,v):
    if p is false, return;
    if v=q?a:b: enter(p&&q,a); enter(p&&!q,b); return;
    p'=simplify(p), v'=simplify(v); if (p!=p' || v!=v') enter(p',v'); return
    if some (q,v) in map: remove (q,v); enter(p||q,v);
    else: add(p,v)


Conversion of intermediate form to an expression is carried
out as follows:

1. Order the entries in some canonical way (e.g., by key).
2. if there is one entry of the form true:a, result is a
3. otherwise, result is
p1?a1:p2?a2 ... : pn-1?an-1:an

(note that pn does not occur)
 * </pre>
 * 
 * @author xxxx
 */
public class ConditionalSimplification extends Simplification {

	public ConditionalSimplification(IdealSimplifierWorker worker) {
		super(worker);
	}

	private class ConditionalSimplifier {

		Map<BooleanExpression, SymbolicExpression> map1;

		Map<SymbolicExpression, BooleanExpression> map2;

		public ConditionalSimplifier() {
			this.map1 = new TreeMap<>(universe().comparator());
			this.map2 = new HashMap<>();
		}

		void init(SymbolicExpression condExpr) {
			BooleanExpression p = (BooleanExpression) condExpr.argument(0);

			enter(p, (SymbolicExpression) condExpr.argument(1));
			enter(universe().not(p), (SymbolicExpression) condExpr.argument(2));
		}

		private SymbolicExpression buildResult(
				Iterator<Entry<BooleanExpression, SymbolicExpression>> iter) {
			if (!iter.hasNext()) {
				throw new SARLInternalException("unreachable");
			}

			SymbolicExpression result = iter.next().getValue();

			while (iter.hasNext()) {
				Entry<BooleanExpression, SymbolicExpression> entry = iter
						.next();

				result = universe().cond(entry.getKey(), entry.getValue(),
						result);
			}
			return result;
		}

		SymbolicExpression getResult() {
			return buildResult(map1.entrySet().iterator());
		}

		private void enter(BooleanExpression p, SymbolicExpression v) {
			if (p.isFalse()) { // nothing to do
			} else if (v.operator() == SymbolicOperator.COND) {
				BooleanExpression q = (BooleanExpression) v.argument(0);
				SymbolicExpression a = (SymbolicExpression) v.argument(1),
						b = (SymbolicExpression) v.argument(2);

				enter(universe().and(p, q), a);
				enter(universe().and(p, universe().not(q)), b);
			} else {
				BooleanExpression p2 = (BooleanExpression) simplifyExpression(
						p);
				SymbolicExpression v2 = simplifyExpression(v);

				if (p != p2 || v != v2)
					enter(p2, v2);
				else {
					BooleanExpression q = map2.remove(v);

					if (q != null) {
						map1.remove(q);
						enter(universe().or(p, q), v);
					} else {
						map1.put(p, v);
						map2.put(v, p);
					}
				}
			}
		}

	}

	@Override
	public SymbolicExpression apply(SymbolicExpression x) {
		ConditionalSimplifier cs = new ConditionalSimplifier();

		cs.init(x);
		return cs.getResult();
	}

	@Override
	public SimplificationKind kind() {
		return SimplificationKind.COND;
	}
}
