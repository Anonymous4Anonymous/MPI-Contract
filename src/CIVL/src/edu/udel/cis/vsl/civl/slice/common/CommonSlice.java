package edu.udel.cis.vsl.civl.slice.common;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.udel.cis.vsl.civl.model.IF.Model;
import edu.udel.cis.vsl.civl.semantics.IF.Transition;
import edu.udel.cis.vsl.civl.slice.IF.Slice;
import edu.udel.cis.vsl.civl.state.IF.State;
import edu.udel.cis.vsl.gmc.Trace;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.preuniverse.IF.FactorySystem;
import edu.udel.cis.vsl.sarl.preuniverse.IF.PreUniverse;
import edu.udel.cis.vsl.sarl.preuniverse.IF.PreUniverses;
import edu.udel.cis.vsl.sarl.prove.z3.Z3Translator;
import edu.udel.cis.vsl.sarl.util.FastList;

public class CommonSlice implements Slice {

	private PrintStream out = System.out;
	private Set<ErrorCfaLoc> slice;
	private Map<SymbolicExpression,String> map;
	private int numberSlicedAway;
	
	public CommonSlice(Trace<Transition, State> trace, Model model) {
		SliceAnalysis sa = new SliceAnalysis(model, trace);
		this.slice = sa.getSlice(); 
		this.map = sa.getMapping();
		this.numberSlicedAway = sa.getNumberSliced();
	}
	
	@Override
	public void print() {
		out.println("=== Sliced PC ===");
		printConstraints(slice);
		out.println("=== END ===");
		out.println("=== ACF Mapping ===");
		printAcfMapping(map);
		out.println("=== END ===");
		out.println("=== Number sliced ===");
		out.println(numberSlicedAway);
		out.println("=== END ===");
	}
	
	private void printAcfMapping (Map<SymbolicExpression,String> map) {
		for (SymbolicExpression s : map.keySet()) {
			String isolatedSymVar = sanitizeSymbolicExpression(s);
			out.println(isolatedSymVar+" "+map.get(s));
		}
	}
	
	private String sanitizeSymbolicExpression(SymbolicExpression s) {
		Pattern p = Pattern.compile("Y\\d+");
		Matcher m = p.matcher(s.toString());
		if (m.find()) {
			return m.group(0);
		} else {
			return s.toString();
		}
	}
	
	private void printConstraints(Set<ErrorCfaLoc> locs) {
		FactorySystem factorySystem = PreUniverses.newIdealFactorySystem();
		PreUniverse universe = PreUniverses.newPreUniverse(factorySystem);
		BooleanExpression context = universe.trueExpression();
			
		Z3Translator startingContext = new Z3Translator(universe,context,true);
		
		List<String> mappings = new ArrayList<>();
		
		for (ErrorCfaLoc l : locs) {
			BooleanExpression expression = l.branchConstraint;
			Z3Translator translator = new Z3Translator(startingContext, expression);
			FastList<String> predicateText = translator.getTranslation();
			String mapping = "";
			if (!predicateText.toString().equals("true")) {
				mapping = expression.toString()
						+ " #Z3# "+predicateText.toString()
						+ " #TYPES# "+varTypePairs(expression.getFreeVars())
						+ "\n";
				mappings.add(mapping);
			}
		}
		java.util.Collections.sort(mappings);
		for (String s: mappings) out.print(s);
	}
	
	private String varTypePairs (Set<SymbolicConstant> symVars) {
		String str = "";
		for (SymbolicConstant s : symVars) {
			str += varTypeStr(s);
		}
		return str;
	}
	
	private String varTypeStr (SymbolicConstant s) {
		String var = s.toString();
		String type = s.type().toString();
		return (var + " " + type + " ");
	}
}
