package edu.udel.cis.vsl.sarl.prove.IF;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;

/**
 * <p>
 * A data structure that represents the interpretation of a
 * {@link LogicFunction}. An instance of {@link ProverFunctionInterpretation}
 * consists of a {@link String} type identifier which identifies a logic
 * function, a list of formal parameters which are {@link SymbolicConstant}s and
 * a definition which is a {@link SymbolicExpression}.
 * </p>
 * 
 * <p>
 * Provers can translate {@link ProverFunctionInterpretation} as function
 * definitions in their languages.
 * </p>
 * 
 * @author xxxx
 */
public class ProverFunctionInterpretation
		implements Comparable<ProverFunctionInterpretation> {

	public final SymbolicExpression definition;

	public final SymbolicConstant parameters[];

	public final SymbolicConstant function;

	public final String identifier;

	private ProverFunctionInterpretation(String identifier,
			SymbolicConstant parameters[], SymbolicExpression definition,
			SymbolicConstant function) {
		this.function = function;
		this.definition = definition;
		this.identifier = identifier;
		this.parameters = parameters;
	}

	public static ProverFunctionInterpretation newProverPredicate(
			SymbolicUniverse universe, String identifier,
			SymbolicConstant parameters[], SymbolicExpression definition) {
		SymbolicConstant function;
		List<SymbolicType> inputTypes = new LinkedList<>();

		for (SymbolicConstant param : parameters)
			inputTypes.add(param.type());
		function = universe.symbolicConstant(universe.stringObject(identifier),
				universe.functionType(inputTypes, definition.type()));
		return new ProverFunctionInterpretation(identifier, parameters,
				definition, function);
	}

	@Override
	public int hashCode() {
		return identifier.hashCode() + definition.hashCode()
				^ Arrays.hashCode(parameters);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		else if (obj instanceof ProverFunctionInterpretation) {
			ProverFunctionInterpretation ppd = (ProverFunctionInterpretation) obj;

			if (ppd.identifier.equals(ppd.identifier)
					&& ppd.definition == this.definition) {
				return Arrays.equals(ppd.parameters, this.parameters);
			}
		}
		return false;
	}

	@Override
	public int compareTo(ProverFunctionInterpretation o) {
		int ret = identifier.compareTo(o.identifier);

		if (ret == 0)
			ret = definition.id() - o.definition.id();
		if (ret == 0)
			ret = parameters.length - o.parameters.length;
		if (ret == 0) {
			for (int i = 0; i < parameters.length; i++) {
				ret = parameters[i].id() - o.parameters[i].id();
				if (ret != 0)
					break;
			}
		}
		return ret;
	}
}
