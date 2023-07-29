package edu.udel.cis.vsl.sarl.preuniverse.common;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import edu.udel.cis.vsl.sarl.IF.SARLException;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicSequence;
import edu.udel.cis.vsl.sarl.preuniverse.IF.PreUniverse;

public class CompressedPrinter {

	/**
	 * The set of all sub-expressions of the original expression that have been
	 * seen at the current time.
	 */
	Set<SymbolicObject> seen = new HashSet<>();

	/**
	 * Same as {@link #seen}, but in the order seen.
	 */
	List<SymbolicObject> seenList = new LinkedList<>();

	/**
	 * The set of all sub-expressions that have been seen more than once at the
	 * current time. This is a subset of {@link #seen}.
	 */
	Set<SymbolicObject> repeats = new HashSet<>();

	/**
	 * The symbolic universe used to do canonicalization
	 */
	PreUniverse universe;

	/**
	 * Where to print.
	 */
	PrintStream out;

	/**
	 * The original object to be printed, canonicalized.
	 */
	SymbolicObject theObject;

	public CompressedPrinter(PreUniverse universe, PrintStream out,
			SymbolicObject object) {
		this.universe = universe;
		this.out = out;
		analyze(theObject);
	}

	private void analyze(SymbolicObject obj) {
		switch (obj.symbolicObjectKind()) {
		case EXPRESSION: {
			SymbolicExpression symExpr = (SymbolicExpression) obj;
			SymbolicOperator op = symExpr.operator();

			if (op == SymbolicOperator.CONCRETE
					|| op == SymbolicOperator.SYMBOLIC_CONSTANT)
				break;
			if (seen.contains(symExpr)) {
				if (!repeats.contains(symExpr)) {
					repeats.add(symExpr);
				}
			} else {
				seen.add(symExpr);
				for (SymbolicObject arg : symExpr.getArguments())
					analyze(arg);
				seenList.add(symExpr);
			}
			break;
		}
		case SEQUENCE: {
			SymbolicSequence<?> symSeq = (SymbolicSequence<?>) obj;
			for (int i = 0; i < symSeq.size(); i++) {
				SymbolicObject seq = symSeq.get(i);

				analyze(seq);
			}
			break;
		}
		case INT:
		case CHAR:
		case BOOLEAN:
		case STRING:
		case NUMBER:
			// nothing to do
			break;
		case TYPE:
		case TYPE_SEQUENCE:
		default:
			throw new SARLException("Should be unreachable");
		}
	}

	public void print() {
		for (SymbolicObject obj : seenList) {
			if (repeats.contains(obj)) {
				out.print("e" + obj.id() + " = ");
				printTruncated(obj);
				out.println();
				out.flush();
			}
		}
		out.println();
		printTruncated(theObject);
		out.println();
		out.flush();
	}

	void printTruncated2(SymbolicObject obj) {
		if (repeats.contains(obj)) {
			out.print("e" + obj.id());
		} else {
			printTruncated(obj);
		}
	}

	void printTruncated(SymbolicObject obj) {
		switch (obj.symbolicObjectKind()) {
		case EXPRESSION: {
			SymbolicExpression symExpr = (SymbolicExpression) obj;
			SymbolicOperator op = symExpr.operator();

			if (op == SymbolicOperator.CONCRETE
					|| op == SymbolicOperator.SYMBOLIC_CONSTANT) {
				out.print(obj);
			} else {
				out.print("(");
				out.print(op);
				for (SymbolicObject arg : symExpr.getArguments()) {
					out.print(" ");
					printTruncated2(arg);
				}
				out.print(")");
			}
			break;
		}
		case SEQUENCE: {
			SymbolicSequence<?> symSeq = (SymbolicSequence<?>) obj;

			out.print("(SEQ ");
			for (int i = 0; i < symSeq.size(); i++) {
				SymbolicObject seq = symSeq.get(i);

				out.print(" ");
				printTruncated2(seq);
			}
			out.print(")");
			break;
		}
		case INT:
		case CHAR:
		case BOOLEAN:
		case STRING:
		case NUMBER:
			out.print(obj);
			break;
		case TYPE:
		case TYPE_SEQUENCE:
		default:
			out.println("Unkownn Symbolic Object: " + obj.symbolicObjectKind());
		}
	}
}
