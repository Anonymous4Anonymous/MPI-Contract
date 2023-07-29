package edu.udel.cis.vsl.abc.analysis.pointsTo.common;

import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.FlowInsensePointsToAnalyzer;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.InvocationGraphNodeFactory;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.SimplePointsToAnalysisIF;
import edu.udel.cis.vsl.abc.ast.IF.AST;
import edu.udel.cis.vsl.abc.ast.type.IF.TypeFactory;

/**
 * The implementation that instantiates classes for
 * {@link SimplePointsToAnalysisIF}
 * 
 * @author xxxx
 *
 */
public class SimplePointsToAnalysis {

	static public FlowInsensePointsToAnalyzer flowInsensePointsToAnalyzer(
			AST program, TypeFactory typeFactory) {
		InvocationGraphNodeFactory igFactory = new CommonInvocationGraphFactory();
		return new CommonFlowInsensePointsToAnalyzer(program,
				new CommonInsensitiveFlowFactory(igFactory, typeFactory),
				igFactory);
	}
}
