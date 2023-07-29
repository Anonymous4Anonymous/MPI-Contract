package edu.udel.cis.vsl.civl.kripke.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.udel.cis.vsl.civl.kripke.IF.AtomicStep;
import edu.udel.cis.vsl.civl.kripke.IF.TraceStep;
import edu.udel.cis.vsl.civl.model.IF.Model;
import edu.udel.cis.vsl.civl.model.IF.location.Location;
import edu.udel.cis.vsl.civl.model.IF.statement.Statement;
import edu.udel.cis.vsl.civl.semantics.IF.Transition;
import edu.udel.cis.vsl.civl.state.IF.State;
import edu.udel.cis.vsl.civl.util.IF.Pair;
import edu.udel.cis.vsl.gmc.Trace;
import edu.udel.cis.vsl.gmc.TraceStepIF;

/**
 * SV-COMP requires a "witness" to be generated for a violation of any property.
 * 
 * The witness of a property violation found by CIVL is a way to tell other
 * program analyzers the path that CIVL took in finding that violation. The
 * common interface for path guidance is given by a control flow
 * automata---called an error automata---, which is the dual of a control flow
 * graph, i.e., the nodes represent program locations and the edges represent
 * program statements. Error automata nodes are annotated with the source code
 * line number, and the edges connecting the nodes are annotated with the source
 * code of the executed statement (in a brittle and underspecified way, see the
 * specs: https://sv-comp.sosy-lab.org/2016/witnesses/). The witness is
 * specified in the GraphML language, which is an extension of XML for
 * describing graphs.
 * 
 * The error automata is essentially CIVL's transition system, where edges are
 * annotated with CIVL {@link Statement}s, and nodes are CIVL {@link Location}s.
 * The only difference is that there is a specified "entry" node and a final
 * "violation" node, and we remove any edges which have no source code
 * associated with the original C file.
 * 
 * The beginning node must be of the form:
 * 
 * <node id="Location0"> <data key="entry">true</data> </node>
 * 
 * and the node leading to the error of the form:
 * 
 * <node id="Location24"> <data key="violation">true</data> </node>
 * 
 * We step through a CIVL {@link Trace} and name nodes according to their
 * {@link Location} string (with whitespace stripped), like:
 * 
 * <node id="Location1"/>
 * 
 * and the edges according to their {@link Statement} string, like:
 * 
 * <edge source="Location1" target="Location2"> <data key="sourcecode">int
 * x=0;</data> <data key="startline">10</data> </edge>
 * 
 * Any edge ({@link Statement}) of a conditional branch must also include the
 * one of the data elements:
 * 
 * <data key="control">condition-false</data>
 * 
 * or
 * 
 * <data key="control">condition-true</data>
 * 
 * @author mgerrard
 *
 */

public class WitnessGenerator {

	Set<String> declaredNodes = new HashSet<String>();
	BufferedWriter output = null;
	boolean debug = false;

	public WitnessGenerator(Model model, Trace<Transition, State> trace)
			throws IOException {

		List<TraceStepIF<State>> steps = trace.traceSteps();
		Iterator<TraceStepIF<State>> it = steps.iterator();
		List<Pair<Location, Statement>> traceStepLocStmt = traceLocStmtPairs(
				it);

		try {
			File file = new File("./witness.graphml");
			output = new BufferedWriter(new FileWriter(file));

			output.write(header());

			List<String> locationPath = new ArrayList<String>();
			for (Pair<Location, Statement> step : traceStepLocStmt) {
				/* We need to strip the whitespace from the Location strings */
				String compactLocationStr = step.left.toString()
						.replaceAll("\\s+", "");
				locationPath.add(compactLocationStr);
			}

			List<Pair<String, String>> locationStringPairs = new ArrayList<Pair<String, String>>();
			for (int i = 0; i < locationPath.size() - 1; i++) {
				Pair<String, String> pair = new Pair<String, String>(
						locationPath.get(i), locationPath.get(i + 1));
				locationStringPairs.add(pair);
				if (debug) {
					System.out.println("Pair " + i + pair);
				}
			}
			Pair<String, String> finalPair = new Pair<String, String>(
					locationPath.get(locationPath.size() - 1), "FinalLocation");
			locationStringPairs.add(finalPair);

			String entrySourceStr = locationStringPairs.get(0).left;
			writeEntryNode(entrySourceStr);
			int locationPairIndex = 0;
			String finalLocation = "";

			for (Pair<Location, Statement> step : traceStepLocStmt) {

				Location location = step.left;
				Statement statement = step.right;
				String statementStr = statement.toString();

				String compactSourceStr = locationStringPairs
						.get(locationPairIndex).left;
				if (debug) {
					System.out.println(
							location + "; about to look at target for: "
									+ statement.toString());
				}
				String compactTargetStr = locationStringPairs
						.get(locationPairIndex).right;

				if (!compactSourceStr
						.equals(entrySourceStr)) { /*
													 * The entry has already
													 * been declared
													 */
					if (statementStr.equals("__VERIFIER_error()")) {
						writeViolationNode(compactSourceStr);
						writeNode(compactTargetStr);
					} else {
						writeNode(compactSourceStr);
					}
				}
				String sourceLocationStr = step.left.getSource().getLocation()
						.toString();
				if (isStatementOrBranch(sourceLocationStr)) {
					String lineNumber = sourceLocationStr
							.replaceAll(".*:([0-9]+)..*", "$1");

					if (statementStr.equals("FALSE_BRANCH_IF")) {
						writeFalseEdge(compactSourceStr, compactTargetStr,
								statement, lineNumber);
					} else if (statementStr.equals("TRUE_BRANCH_IF")) {
						writeTrueEdge(compactSourceStr, compactTargetStr,
								statement, lineNumber);
					} else {
						writeStmtEdge(compactSourceStr, compactTargetStr,
								statement, lineNumber);
					}
				} else {
					assert false : "Location " + sourceLocationStr
							+ " does not lead to a source statement or branch";
				}

				finalLocation = compactTargetStr;
				locationPairIndex++;
			}
			writeNode(finalLocation);

			output.write(footer());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (output != null) {
				output.close();
			}
		}
	}

	/* Extract Location-Statement pairs from TraceStep Iterator */
	private List<Pair<Location, Statement>> traceLocStmtPairs(
			Iterator<TraceStepIF<State>> it) {
		List<Pair<Location, Statement>> tracePairs = new ArrayList<Pair<Location, Statement>>();
		while (it.hasNext()) {
			TraceStep step = ((TraceStep) it.next());
			Iterable<AtomicStep> atomicSteps = step.getAtomicSteps();
			for (AtomicStep atom : atomicSteps) {
				Location l = atom.getTransition().statement().source();
				Statement s = atom.getTransition().statement();
				/*
				 * For witness optimization, only add locations leading to a
				 * statement which are not system source
				 */
				String str = s.toString();
				if (str != null && !str.startsWith("$")
						&& !str.startsWith("return temp (__VERIFIER_nondet")
						&& !str.startsWith("_svcomp_unsigned_bound")
						&& !str.startsWith("NO_OP")) {
					tracePairs.add(new Pair<Location, Statement>(l, s));
				}
			}
		}
		return tracePairs;
	}

	private boolean isStatementOrBranch(String locationString) {
		if (locationString.matches(".*.c:.*")) {
			return true;
		} else if (locationString.matches(".*.i:.*")) {
			return true;
		} else {
			return false;
		}
	}

	private void writeNode(String locationString) throws IOException {
		if (!declaredNodes.contains(locationString)) {
			output.write("<node id=\"" + locationString + "\"/>");
			output.newLine();
			declaredNodes.add(locationString);
		}
	}

	private void writeViolationNode(String locationString) throws IOException {
		output.write("<node id=\"" + locationString + "\">\n"
				+ "  <data key=\"violation\">true</data>\n" + "</node>");
		output.newLine();
		declaredNodes.add(locationString);
	}

	private void writeEntryNode(String entryLocation) throws IOException {
		output.write("<node id=\"" + entryLocation + "\">\n"
				+ "  <data key=\"entry\">true</data>\n" + "</node>\n");
		declaredNodes.add("Location0");
	}

	private void writeFalseEdge(String source, String target, Statement stmt,
			String lineNumber) throws IOException {
		String saneStmt = escapeXml(stmt.guard().toString());
		output.write(
				"<edge source=\"" + source + "\" target=\"" + target + "\">");
		output.newLine();
		output.write("  <data key=\"sourcecode\">[" + saneStmt + "]</data>");
		output.newLine();
		output.write("  <data key=\"startline\">" + lineNumber + "</data>");
		output.newLine();
		output.write("  <data key=\"control\">condition-false</data>");
		output.newLine();
		output.write("</edge>");
	}

	private void writeTrueEdge(String source, String target, Statement stmt,
			String lineNumber) throws IOException {
		String saneStmt = escapeXml(stmt.guard().toString());
		output.write(
				"<edge source=\"" + source + "\" target=\"" + target + "\">");
		output.newLine();
		output.write("  <data key=\"sourcecode\">[" + saneStmt + "]</data>");
		output.newLine();
		output.write("  <data key=\"startline\">" + lineNumber + "</data>");
		output.newLine();
		output.write("  <data key=\"control\">condition-true</data>");
		output.newLine();
		output.write("</edge>");
	}

	private void writeStmtEdge(String source, String target, Statement stmt,
			String lineNumber) throws IOException {
		String saneStmt = escapeXml(stmt.toString());
		output.write(
				"<edge source=\"" + source + "\" target=\"" + target + "\">");
		output.newLine();
		output.write("  <data key=\"sourcecode\">" + saneStmt + "</data>");
		output.newLine();
		output.write("  <data key=\"startline\">" + lineNumber + "</data>");
		output.newLine();
		output.write("</edge>");
		output.newLine();
	}

	private String escapeXml(String s) {
		/*
		 * Supports escaping only the 5 basic XML entities: gt,lt,quot,amp,apos
		 */
		s = s.replaceAll("&", "&amp;");
		s = s.replaceAll("\"", "&quot;");
		s = s.replaceAll("'", "&apos;");
		s = s.replaceAll("<", "&lt;");
		s = s.replaceAll(">", "&gt;");
		return s;
	}

	private String header() {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
				+ "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n "
				+ "<key attr.name=\"invariant\" attr.type=\"string\" for=\"node\" id=\"invariant\"/>\n "
				+ "<key attr.name=\"invariant.scope\" attr.type=\"string\" for=\"node\" id=\"invariant.scope\"/>\n "
				+ "<key attr.name=\"namedValue\" attr.type=\"string\" for=\"node\" id=\"named\"/>\n "
				+ "<key attr.name=\"nodeType\" attr.type=\"string\" for=\"node\" id=\"nodetype\">\n  "
				+ "<default>path</default>\n " + "</key>\n "
				+ "<key attr.name=\"isFrontierNode\" attr.type=\"boolean\" for=\"node\" id=\"frontier\">\n  "
				+ "<default>false</default>\n " + "</key>\n "
				+ "<key attr.name=\"isViolationNode\" attr.type=\"boolean\" for=\"node\" id=\"violation\">\n  "
				+ "<default>false</default>\n </key>\n "
				+ "<key attr.name=\"isEntryNode\" attr.type=\"boolean\" for=\"node\" id=\"entry\">\n  "
				+ "<default>false</default>\n " + "</key>\n "
				+ "<key attr.name=\"isSinkNode\" attr.type=\"boolean\" for=\"node\" id=\"sink\">\n  "
				+ "<default>false</default>\n " + "</key>\n "
				+ "<key attr.name=\"isLoopHead\" attr.type=\"boolean\" for=\"node\" id=\"loopHead\">\n  "
				+ "<default>false</default>\n " + "</key>\n "
				+ "<key attr.name=\"violatedProperty\" attr.type=\"string\" for=\"node\" id=\"violatedProperty\"/>\n "
				+ "<key attr.name=\"threadId\" attr.type=\"string\" for=\"edge\" id=\"threadId\"/>\n "
				+ "<key attr.name=\"sourcecodeLanguage\" attr.type=\"string\" for=\"graph\" id=\"sourcecodelang\"/>\n "
				+ "<key attr.name=\"programFile\" attr.type=\"string\" for=\"graph\" id=\"programfile\"/>\n "
				+ "<key attr.name=\"programHash\" attr.type=\"string\" for=\"graph\" id=\"programhash\"/>\n "
				+ "<key attr.name=\"specification\" attr.type=\"string\" for=\"graph\" id=\"specification\"/>\n "
				+ "<key attr.name=\"memoryModel\" attr.type=\"string\" for=\"graph\" id=\"memorymodel\"/>\n "
				+ "<key attr.name=\"architecture\" attr.type=\"string\" for=\"graph\" id=\"architecture\"/>\n "
				+ "<key attr.name=\"producer\" attr.type=\"string\" for=\"graph\" id=\"producer\"/>\n "
				+ "<key attr.name=\"sourcecode\" attr.type=\"string\" for=\"edge\" id=\"sourcecode\"/>\n "
				+ "<key attr.name=\"startline\" attr.type=\"int\" for=\"edge\" id=\"startline\"/>\n "
				+ "<key attr.name=\"startoffset\" attr.type=\"int\" for=\"edge\" id=\"startoffset\"/>\n "
				+ "<key attr.name=\"lineColSet\" attr.type=\"string\" for=\"edge\" id=\"lineCols\"/>\n "
				+ "<key attr.name=\"control\" attr.type=\"string\" for=\"edge\" id=\"control\"/>\n "
				+ "<key attr.name=\"assumption\" attr.type=\"string\" for=\"edge\" id=\"assumption\"/>\n "
				+ "<key attr.name=\"assumption.scope\" attr.type=\"string\" for=\"edge\" id=\"assumption.scope\"/>\n "
				+ "<key attr.name=\"enterFunction\" attr.type=\"string\" for=\"edge\" id=\"enterFunction\"/>\n "
				+ "<key attr.name=\"returnFromFunction\" attr.type=\"string\" for=\"edge\" id=\"returnFrom\"/>\n "
				+ "<key attr.name=\"predecessor\" attr.type=\"string\" for=\"edge\" id=\"predecessor\"/>\n "
				+ "<key attr.name=\"successor\" attr.type=\"string\" for=\"edge\" id=\"successor\"/>\n "
				+ "<key attr.name=\"witness-type\" attr.type=\"string\" for=\"graph\" id=\"witness-type\"/>\n "
				+ "<graph edgedefault=\"directed\">\n  <data key=\"witness-type\">violation_witness</data>\n  "
				+ "<data key=\"sourcecodelang\">C</data>\n  <data key=\"producer\">CIVL</data>\n "
				+ "<data key=\"specification\">CHECK( init(main()), LTL(G ! call(__VERIFIER_error())) )</data>\n "
				+ "<data key=\"memorymodel\">precise</data>\n  <data key=\"architecture\">32bit</data>\n";
	}

	private String footer() {
		return " </graph>\n" + "</graphml>";
	}

}
