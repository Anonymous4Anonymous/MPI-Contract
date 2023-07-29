package edu.udel.cis.vsl.civl.state.common.immutable;

import java.util.TreeMap;
import java.util.function.Function;

import edu.udel.cis.vsl.civl.dynamic.IF.SymbolicUtility;
import edu.udel.cis.vsl.civl.model.IF.CIVLTypeFactory;
import edu.udel.cis.vsl.civl.model.IF.ModelConfiguration;
import edu.udel.cis.vsl.civl.state.IF.State;
import edu.udel.cis.vsl.civl.state.IF.StateValueHelper;
import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;
import edu.udel.cis.vsl.sarl.IF.UnaryOperator;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;

public class ImmutableStateValueHelper implements StateValueHelper {

	private SymbolicUniverse universe;

	private CIVLTypeFactory typeFactory;

	private SymbolicUtility symbolicUtil;

	ImmutableStateValueHelper(SymbolicUniverse universe,
			CIVLTypeFactory typeFactory, SymbolicUtility symbolicUtil) {
		this.universe = universe;
		this.typeFactory = typeFactory;
		this.symbolicUtil = symbolicUtil;
	}

	@Override
	public UnaryOperator<SymbolicExpression> scopeSubstituterForReferredState(
			State currState, SymbolicExpression referredStateValue) {
		SymbolicExpression toCurrentScopes = typeFactory.stateType()
				.selectScopeValuesMap(universe, referredStateValue);
		SymbolicExpression toCurrentScopesArray[] = symbolicUtil
				.symbolicArrayToConcreteArray(toCurrentScopes);
		Function<Integer, SymbolicExpression> scopeIDToValueFunction = typeFactory
				.scopeType().scopeIdentityToValueOperator(universe);
		TreeMap<SymbolicExpression, SymbolicExpression> subMap = new TreeMap<>(
				universe.comparator());

		for (int i = 0; i < toCurrentScopesArray.length; i++)
			subMap.put(toCurrentScopesArray[i],
					scopeIDToValueFunction.apply(i));

		int numScopes = currState.numDyscopes();

		for (int i = 0; i < numScopes; i++) {
			SymbolicExpression scopeVal = scopeIDToValueFunction.apply(i);

			if (!subMap.containsKey(scopeVal))
				subMap.put(scopeVal, scopeIDToValueFunction
						.apply(ModelConfiguration.DYNAMIC_UNDEFINED_SCOPE));
		}
		return universe.mapSubstituter(subMap);
	}

	@Override
	public UnaryOperator<SymbolicExpression> scopeSubstituterForCurrentState(
			State currState, SymbolicExpression referredStateValue) {
		SymbolicExpression toCurrentScopes = typeFactory.stateType()
				.selectScopeValuesMap(universe, referredStateValue);
		SymbolicExpression toCurrentScopesArray[] = symbolicUtil
				.symbolicArrayToConcreteArray(toCurrentScopes);
		Function<Integer, SymbolicExpression> scopeIDToValueFunction = typeFactory
				.scopeType().scopeIdentityToValueOperator(universe);
		TreeMap<SymbolicExpression, SymbolicExpression> subMap = new TreeMap<>(
				universe.comparator());

		for (int i = 0; i < toCurrentScopesArray.length; i++) {
			SymbolicExpression keyScopeVal = scopeIDToValueFunction.apply(i);
			SymbolicExpression valueScopeVal = toCurrentScopesArray[i];

			subMap.put(keyScopeVal, valueScopeVal);
		}
		return universe.mapSubstituter(subMap);
	}
}
