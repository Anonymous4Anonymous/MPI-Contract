package edu.udel.cis.vsl.civl.transform.analysisIF;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignExprIF;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignExprIF.AssignExprKind;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignFieldExprIF;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignOffsetIF;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignStoreExprIF;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignSubscriptExprIF;
import edu.udel.cis.vsl.abc.ast.entity.IF.Function;
import edu.udel.cis.vsl.abc.ast.entity.IF.Variable;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.type.IF.Field;
import edu.udel.cis.vsl.abc.ast.util.ExpressionEvaluator;
import edu.udel.cis.vsl.abc.util.IF.Pair;
import edu.udel.cis.vsl.civl.model.IF.CIVLInternalException;
import edu.udel.cis.vsl.civl.transform.analysisIF.ReadWriteDataStructures.RWSet;
import edu.udel.cis.vsl.civl.transform.analysisIF.ReadWriteDataStructures.RWSetBaseElement;
import edu.udel.cis.vsl.civl.transform.analysisIF.ReadWriteDataStructures.RWSetElement;
import edu.udel.cis.vsl.civl.transform.analysisIF.ReadWriteDataStructures.RWSetFieldElement;
import edu.udel.cis.vsl.civl.transform.analysisIF.ReadWriteDataStructures.RWSetSubscriptElement;
import edu.udel.cis.vsl.civl.transform.analysisIF.SimpleReadWriteAnalyzer.SimpleFullSetException;

//TODO: a[i][i] vs a[0][i + 1] shall be fine but currently considered error!
/**
 * An analyzer providing the
 * {@link #threadsArrayAccessIndependent(Function, List, Iterable, Iterable, Set, Set, Integer)}
 * method for checking if two sets of {@link RWSetElement} are independent.
 * 
 * @author xxxx
 */
public class ArrayReferenceDependencyAnalyzer {

	private static enum CompareResult {
		IDENTICAL, INDEPENDENT, UNKNOWN,
	}

	private boolean debug = false;

	private SimpleReadWriteAnalyzer analyzer;

	/**
	 * global variable that refers to the {@link Function} where the current
	 * analyzing reads/writes happen:
	 */
	private Function currentFunction;

	/**
	 * For any thread variable <code>i</code>, the (absolute) difference of the
	 * values of <code>i</code> on two concurrent threads must be less than
	 * <code>safeLength</code>.
	 */
	private Integer safeLength = null;

	/**
	 * A set of variables, each of which is guaranteed that will have different
	 * values on different concurrent threads.
	 */
	private Set<Variable> threadVars;

	/**
	 * The full write set. If a {@link RWSetElement} is not in this
	 * {@link #fullWriteSet}, it can be considered as read-only.
	 */
	private Set<RWSetElement> fullWriteSet;

	/**
	 * a set of conjunctive clauses of the assumption
	 */
	private List<ExpressionNode> assumptions;

	public ArrayReferenceDependencyAnalyzer(SimpleReadWriteAnalyzer analyzer) {
		this.analyzer = analyzer;
	}

	/**
	 * <p>
	 * Check whether the write set of a thread is independent with the read set
	 * (read set is a super-set of write set) of another concurrently running
	 * thread.
	 * </p>
	 * 
	 * <p>
	 * For an element "w" in the write set and an element "r" in the read set,
	 * whether "w" and "r" are independent is determined by
	 * {@link #checkIndependency(RWSetElement, RWSetElement, List)}.
	 * </p>
	 * 
	 * @param function
	 *            the {@link Function} where all the writes and reads happens
	 * @param assumptions
	 *            boolean assumptions
	 * @param arrWrites
	 *            a set of write to array elements
	 * @param arrReads
	 *            a set of read to array elements
	 * @param fulWrites
	 *            the set containing all the write accesses that may be done in
	 *            a specific statement
	 * @param threadVariables
	 *            a set of thread variables, thread variables are the variables
	 *            that are guaranteed to be different on different threads
	 * @param safeLength
	 *            for any thread variable <code>i</code>, differences of two
	 *            values of <code>i</code> of two concurrent iterations will be
	 *            less than (strictly) safeLength.
	 * @return true if and only if that threads read/write are independent
	 */
	public boolean threadsArrayAccessIndependent(Function function,
			List<ExpressionNode> assumptions, Iterable<RWSetElement> arrWrites,
			Iterable<RWSetElement> arrReads, Set<RWSetElement> fullWrites,
			Set<Variable> threadVariables, Integer safeLength) {
		boolean independent = true;

		assert assumptions != null;
		this.assumptions = assumptions;
		this.currentFunction = function;
		this.safeLength = safeLength;
		this.fullWriteSet = fullWrites;
		this.threadVars = threadVariables;

		for (RWSetElement w : arrWrites) {
			for (RWSetElement r : arrReads) {
				if (debug) {
					String safeLenStr = safeLength == null
							? ""
							: " with safeLen " + safeLength.intValue();
					System.out.println("Checking Array Refs for ");
					System.out.println(w + " and ");
					System.out.println(r + safeLenStr);
				}
				independent &= checkIndependency(w, r);
				if (!independent && debug) {
					System.out.println("may dependent!");
					return false;
				}
			}
		}
		return independent;
	}

	/**
	 * To determine if two {@link RWSetElement}s "e0" and "e1" are independent:
	 * <ol>
	 * <li>They have the same structure: let "s" be a sequence of operations,
	 * each of which is either a FIELD-ACCESS to a {@link Field} or an
	 * ARRAY-SUBSCRIPT (index is insignificant), "e0" and "e1" have the same
	 * structure if both of them can be obtained by applying "s" to a same root
	 * abstract object.</li>
	 * 
	 * <li>If "e0" and "e1" have the same structure, let "i0" and "i1" be two
	 * sequences of index expressions that are associated with the
	 * ARRAY-SUBSCRIPT operations in order on "e0" and "e1" separately.
	 * Recursively check <code>
	 * 
	 * length = i0.length; // i0 and i1 have same length
	 * 
	 * for (k : length) {
	 *   if (isIdentical(i0[k], i1[k])) 
	 *     continue;
	 *   if (isIndependent(i0[k], i1[k]))
	 *     return true;
	 *   return false;
	 * }
	 * </code> Whether two indices are independent is determined by
	 * {@link #compareIndicesWorker(Integer, ExpressionNode, Integer, ExpressionNode, List)}
	 * 
	 * </li>
	 * </ol>
	 * 
	 * @param e0
	 *            a {@link RWSetElement} instance
	 * @param r
	 *            a {@link RWSetElement} instance
	 * 
	 * @return true iff "e0" and "e1" have same structure
	 */
	private boolean checkIndependency(RWSetElement e0, RWSetElement e1) {
		Pair<AssignStoreExprIF, List<FieldOrSubscript>> norm0 = normalize(e0);
		Pair<AssignStoreExprIF, List<FieldOrSubscript>> norm1 = normalize(e1);

		if (norm0.left != norm1.left)
			return true;

		Iterator<FieldOrSubscript> iter0, iter1;
		CompareResult idxComparison = CompareResult.IDENTICAL;

		iter0 = norm0.right.iterator();
		iter1 = norm1.right.iterator();

		while (iter0.hasNext()) {
			if (!iter1.hasNext())
				return false;

			FieldOrSubscript op0 = iter0.next();
			FieldOrSubscript op1 = iter1.next();

			if (op0.isField != op1.isField)
				return false; // suppose to be unreachable
			if (op0.isField)
				if (op0.field != op1.field)
					return true;
				else
					continue;

			idxComparison = compareIndices(op0.indexOffset, op0.index,
					op1.indexOffset, op1.index);

			if (idxComparison == CompareResult.UNKNOWN)
				return false;
			if (idxComparison == CompareResult.INDEPENDENT)
				return true;
		}
		return idxComparison == CompareResult.INDEPENDENT;
	}

	/**
	 * 
	 * <p>
	 * Let "compare(idx, idx')" be a method compares two indices "idx" and
	 * "idx'". The result of comparison can be one of the three cases:
	 * <ol>
	 * <li>IDENTICAL: if both "idx" and "idx'" are read-only and they are
	 * lexically identical. IDENTICAL infers that they always have same values
	 * at runtime.</li>
	 * 
	 * <li>INDEPENDENT: if 1) both "idx" and "idx'" are math-functions "f" and
	 * "f'" over thread variables and 2) it can be proved
	 * <code>f(X) != f(X') iff X != X'</code> under some assumptions. Note that
	 * we say "idx" is a math-function over thread variables if "idx" only
	 * consists of read-only objects and thread variables.</li>
	 * 
	 * <li>UNKNOWN: nothing can be concluded</li>
	 * </ol>
	 * </p>
	 * 
	 * @param aOft
	 *            an instance of {@link AssignOffsetIF}, which is a part of an
	 *            index expression <code>aIdx + aOft</code>
	 * @param aIdx
	 *            a part of an index expression <code>aIdx + aOft</code>
	 * @param bOft
	 *            an instance of {@link AssignOffsetIF}, which is a part of an
	 *            index expression <code>bIdx + bOft</code>
	 * @param bIdx
	 *            a part of an index expression <code>bIdx + bOft</code>
	 * @return the {@link CompareResult} for the comparison of the two given
	 *         index expressions
	 */
	private CompareResult compareIndices(AssignOffsetIF aOft,
			ExpressionNode aIdx, AssignOffsetIF bOft, ExpressionNode bIdx) {
		if (!aOft.hasConstantValue() || !bOft.hasConstantValue())
			return CompareResult.UNKNOWN;

		Integer aOftInt = aOft.constantValue();
		Integer bOftInt = bOft.constantValue();

		return compareIndicesWorker(aOftInt, aIdx, bOftInt, bIdx);
	}

	/**
	 * worker method for
	 * {@link #compareIndices(AssignOffsetIF, ExpressionNode, AssignOffsetIF, ExpressionNode)}
	 */
	private CompareResult compareIndicesWorker(Integer oft0,
			ExpressionNode idx0, Integer oft1, ExpressionNode idx1) {
		Set<Variable> mathFuncInputs = new HashSet<>();
		Set<Variable> tmp = null;

		if (idx0 != null) {
			tmp = getMathFuncInputs(idx0, fullWriteSet, threadVars);
			if (tmp == null)
				return CompareResult.UNKNOWN;
			mathFuncInputs.addAll(tmp);
		}
		if (idx1 != null) {
			tmp = getMathFuncInputs(idx1, fullWriteSet, threadVars);
			if (tmp == null)
				return CompareResult.UNKNOWN;
			mathFuncInputs.addAll(tmp);
		}
		if (mathFuncInputs.isEmpty()) {
			// IF both index expressions are pure read-only, check if they are
			// lexically identical:
			if (oft0.equals(oft1))
				return CompareResult.IDENTICAL;
			else
				return CompareResult.INDEPENDENT;
		}
		// TODO: idx0 and idx1 may be null, find a way to deal with them better.
		// At least, "ExpressionEvaluator.checkFunctionDisagrement" doesn't have
		// to deal with nulls.
		// IF both index expressions are math functions (including read-only)
		// over thread vars, check if they are independent:
		if (ExpressionEvaluator.checkFunctionDisagrement(oft0, idx0, oft1, idx1,
				mathFuncInputs, threadVars, safeLength, assumptions))
			return CompareResult.INDEPENDENT;
		return CompareResult.UNKNOWN;
	}

	/**
	 * <p>
	 * return true iff the given expression "expr" is a math function over the
	 * given set of thread-variables.
	 * </p>
	 *
	 * 
	 * @return the subset of the thread variables that the given expression
	 *         depends on if the expression is a math-function over the set of
	 *         thread variables. Otherwise, null;
	 */
	private Set<Variable> getMathFuncInputs(ExpressionNode expr,
			Set<RWSetElement> fullWrites, Set<Variable> threadVars) {
		RWSet exprRWSet;
		try {
			exprRWSet = analyzer.collectRWFromStmtDeclExpr(currentFunction,
					expr, new HashSet<>());
		} catch (SimpleFullSetException e) {
			return fullWrites.isEmpty() ? new HashSet<>() : null;
		}
		assert exprRWSet.writes().isEmpty();

		// fullWrites contains no thread variables, so
		// expr is considered a function over thread variables if
		// the intersection of "exprRWSet.reads" and "fullWrites" is empty
		List<RWSetElement> intersect = exprRWSet.reads().parallelStream()
				.filter(referToSameObject(fullWrites))
				.collect(Collectors.toList());

		// If there exist objects that are in the write set, this is not a
		// math-function:
		if (!intersect.isEmpty())
			return null;

		// get the input subset:
		Set<Variable> subset = new HashSet<>();

		for (RWSetElement e : exprRWSet.reads()) {
			AssignExprIF varAbs = e.root();

			assert varAbs != null && varAbs.kind() == AssignExprKind.STORE;
			AssignStoreExprIF store = (AssignStoreExprIF) varAbs;

			if (store.isAllocation())
				continue;
			if (threadVars.contains(store.variable()))
				subset.add(store.variable());
		}
		return subset;
	}

	/* ***************** normalization ******************* */

	/**
	 * Given a {@link RWSetElement} "e", normalize "e" to such a form:
	 * <code>(root, s)</code>, where <code>root</code> is a
	 * {@link AssignStoreExprIF} representing the root variable, memory heap or
	 * string where the object represented by "e" is at; <code>s</code> is a
	 * sequence of {@link FieldOrSubscript} that directs to the object
	 * represented by "e"
	 * 
	 * @param e
	 *            a {@link RWSetElement} representing a scalar object that can
	 *            be written to read
	 * @return the normalized <code>(root, s)</code> of the given
	 *         {@link RWSetElement}
	 */
	private Pair<AssignStoreExprIF, List<FieldOrSubscript>> normalize(
			RWSetElement e) {
		Pair<AssignStoreExprIF, List<FieldOrSubscript>> result;

		switch (e.kind()) {
			case BASE : {
				RWSetBaseElement base = (RWSetBaseElement) e;

				if (base.base().kind() == AssignExprKind.STORE)
					return new Pair<>((AssignStoreExprIF) base.base(),
							new LinkedList<>());
				else
					return normalizeAssignExprIF(base.base(), e.source());
			}
			case FIELD : {
				RWSetFieldElement fieldEle = (RWSetFieldElement) e;

				result = normalize(fieldEle.struct());
				result.right.add(new FieldOrSubscript(fieldEle.field()));
				return result;
			}
			case SUBSCRIPT : {
				RWSetSubscriptElement substEle = (RWSetSubscriptElement) e;

				result = normalize(substEle.array());
				result.right.add(new FieldOrSubscript(substEle.indices()[0],
						substEle.offset()));

				int numIndices = substEle.indices().length;

				for (int i = 1; i < numIndices; i++)
					result.right.add(
							new FieldOrSubscript(substEle.indices()[i], null));
				return result;
			}
			case OFFSET :
			case ARBITRARY :
			default :
				throw new CIVLInternalException("unexpected RWSetElement kind: "
						+ e.kind() + " of " + e, e.source().getSource());
		}
	}

	/**
	 * Worker method for {@link #normalize(RWSetElement)}: normalizes an
	 * abstract object {@link AssignExprIF} to the normalized form described in
	 * {@link #normalize(RWSetElement)}.
	 */
	private Pair<AssignStoreExprIF, List<FieldOrSubscript>> normalizeAssignExprIF(
			AssignExprIF absObj, ASTNode source) {
		Pair<AssignStoreExprIF, List<FieldOrSubscript>> result;

		switch (absObj.kind()) {
			case FIELD : {
				AssignFieldExprIF fieldAbsObj = (AssignFieldExprIF) absObj;

				result = normalizeAssignExprIF(fieldAbsObj.struct(), source);
				result.right.add(new FieldOrSubscript(fieldAbsObj.field()));
				return result;
			}
			case STORE :
				return new Pair<>((AssignStoreExprIF) absObj,
						new LinkedList<>());
			case SUBSCRIPT : {
				AssignSubscriptExprIF substAbsObj = (AssignSubscriptExprIF) absObj;

				result = normalizeAssignExprIF(substAbsObj.array(), source);
				result.right
						.add(new FieldOrSubscript(null, substAbsObj.index()));
				return result;
			}
			case AUX :
			case OFFSET :
			default :
				throw new CIVLInternalException("unexpected AssignExprIF kind: "
						+ absObj.kind() + " of " + absObj, source.getSource());
		}
	}

	/**
	 * A data-structure representing a union of 1) a FIELD-ACCESS to a Field and
	 * 2) a ARRAY-SUBSCRIPT with an index expression
	 * 
	 * @author xxxx
	 */
	private class FieldOrSubscript {
		/**
		 * the flag indicating if this instance represents FIELD-ACCESS or
		 * ARRAY-SUBSCRIPT
		 */
		final boolean isField;

		/**
		 * If {@link #isField}, this field stores the {@link Field} that is
		 * accessed
		 */
		final Field field;

		/**
		 * If NOT {@link #isField}, this field stores a part of the index
		 * expression <code>index + indexOffset</code>
		 */
		final ExpressionNode index;

		/**
		 * If NOT {@link #isField}, this field stores a part of the index
		 * expression <code>index + indexOffset</code>
		 */
		final AssignOffsetIF indexOffset;

		FieldOrSubscript(ExpressionNode index, AssignOffsetIF indexOffset) {
			this.isField = false;
			this.field = null;
			this.index = index;
			this.indexOffset = indexOffset;
		}

		FieldOrSubscript(Field field) {
			this.isField = true;
			this.field = field;
			this.index = null;
			this.indexOffset = null;
		}

		@Override
		public String toString() {
			if (isField)
				return "." + field.getName();
			return "[" + index.prettyRepresentation() + " + " + indexOffset
					+ "]";
		}
	}

	/* ************* java predicates ************/
	/**
	 * a predicate over a set of RWSetElement that each element t refers to the
	 * same variable or heap object as at least one element t' in the given
	 * "otherSet"
	 */
	private Predicate<RWSetElement> referToSameObject(
			Set<RWSetElement> otherSet) {
		return new Predicate<RWSetElement>() {
			Set<RWSetElement> compareTo = otherSet;

			@Override
			public boolean test(RWSetElement t) {
				for (RWSetElement e : compareTo)
					if (e.root().mayEquals(t.root()))
						return true;
				return false;
			}
		};
	}
}
