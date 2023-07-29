package edu.udel.cis.vsl.abc.analysis.pointsTo.IF;

import edu.udel.cis.vsl.abc.analysis.pointsTo.common.SimplePointsToAnalysis;
import edu.udel.cis.vsl.abc.ast.IF.AST;
import edu.udel.cis.vsl.abc.ast.type.IF.TypeFactory;

/**
 * The interface for create a points-to analyzer
 * 
 * @author xxxx
 *
 */
public class SimplePointsToAnalysisIF {
	static public FlowInsensePointsToAnalyzer flowInsensePointsToAnalyzer(
			AST program, TypeFactory typeFactory) {
		return SimplePointsToAnalysis.flowInsensePointsToAnalyzer(program,
				typeFactory);
	}
}
