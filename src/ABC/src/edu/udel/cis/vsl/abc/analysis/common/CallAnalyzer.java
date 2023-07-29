package edu.udel.cis.vsl.abc.analysis.common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.udel.cis.vsl.abc.analysis.IF.Analyzer;
import edu.udel.cis.vsl.abc.ast.IF.AST;
import edu.udel.cis.vsl.abc.ast.entity.IF.Function;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.IdentifierNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.FunctionDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.FunctionDefinitionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.FunctionCallNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.IdentifierExpressionNode;
import edu.udel.cis.vsl.abc.ast.type.IF.FunctionType;
import edu.udel.cis.vsl.abc.ast.type.IF.PointerType;
import edu.udel.cis.vsl.abc.ast.type.IF.StandardSignedIntegerType;
import edu.udel.cis.vsl.abc.ast.type.IF.StandardSignedIntegerType.SignedIntKind;
import edu.udel.cis.vsl.abc.ast.type.IF.Type;
import edu.udel.cis.vsl.abc.token.IF.SyntaxException;

/**
 * Given an AST, determines caller/callee relationships among functions.
 * 
 * Calls through a function pointer are approximated by the set of functions
 * whose type matches the function pointer type.
 * 
 * Analysis is two-phase: First "collect" the set of functions declared for each
 * function type. Second "process" call nodes using the function-type relation
 * to resolve indirect calls.
 * 
 * In addition to the usual Analyzer interface, this class provides static access to an unordered
 * list of {@link Function}s computed for each {@link AST}.
 * 
 * @author dwyer
 * 
 */
public class CallAnalyzer implements Analyzer {
	Map<FunctionType, Set<Function>> functionsOfAType = new HashMap<FunctionType, Set<Function>>();
				
	private void addCall(Function caller, Function callee) {
		caller.getCallees().add(callee);
		callee.getCallers().add(caller);
	}

	private void collectProgram(ASTNode node) {
		if (node instanceof FunctionDefinitionNode) {
			collectFunctionDefinitionNode((FunctionDefinitionNode) node);
		} else if (node instanceof FunctionDeclarationNode) {
			// Will only reach this code if this is a prototype declaration
			collectFunctionDeclarationNode((FunctionDeclarationNode) node);
		}
		for (ASTNode child : node.children()) {
			if (child != null)
				collectProgram(child);
		}
	}

	private void collectFunctionDefinitionNode(FunctionDefinitionNode funNode) {
		Function fEntity = funNode.getEntity();

		FunctionType funType = (FunctionType) funNode.getTypeNode().getType();

		if (fEntity.getName().equals("main")) {
			// Return type of main is "int"
			Type rType = funType.getReturnType();
			if (rType instanceof StandardSignedIntegerType
					&& ((StandardSignedIntegerType) rType).getIntKind() == SignedIntKind.INT) {
				// Main has either 0 or 2 parameters
				if (funType.getNumParameters() == 0) {
					funNode.getOwner().setMain(fEntity);
				} else if (funType.getNumParameters() == 2) {
					// If it has parameters they are of type "int" and "char **"
					Type p0 = funType.getParameterType(0);
					if (p0 instanceof StandardSignedIntegerType
							&& ((StandardSignedIntegerType) p0).getIntKind() == SignedIntKind.INT) {
						Type p1 = funType.getParameterType(1);
						if (p1 instanceof PointerType) {
							Type derefP1 = ((PointerType) p1).referencedType();
							if (derefP1 instanceof PointerType) {
								Type deDerefP1 = ((PointerType) derefP1)
										.referencedType();
								if (deDerefP1 instanceof StandardSignedIntegerType
										&& ((StandardSignedIntegerType) deDerefP1)
												.getIntKind() == SignedIntKind.SIGNED_CHAR) {
									funNode.getOwner().setMain(fEntity);
								}
							}
						}
					}
				}
			}
		}

		collectFunctionType(funType);

		Set<Function> funsOfThisType = getFunctionsOfAType(funType);
		funsOfThisType.add(fEntity);
	}

	private void collectFunctionDeclarationNode(FunctionDeclarationNode funcNode) {
		collectFunctionType((FunctionType) (funcNode.getTypeNode().getType()));
	}

	private void collectFunctionType(FunctionType funType) {
		if (getFunctionsOfAType(funType) == null) {
			functionsOfAType.put(funType, new HashSet<Function>());
		}
	}

	private Set<Function> getFunctionsOfAType(FunctionType funType) {
		for (FunctionType fType : functionsOfAType.keySet()) {
			if (funType.compatibleWith(fType)) {
				return functionsOfAType.get(fType);
			}
		}
		return null;
	}

	private void processFunctionDefinitionNode(FunctionDefinitionNode funcNode) {
		Function fEntity = funcNode.getEntity();
		processFunctionBody(funcNode.getBody(), fEntity);
	}

	private void processFunctionBody(ASTNode node, Function caller) {
		if (node instanceof FunctionCallNode) {
			FunctionCallNode fcn = (FunctionCallNode) node;

			if (fcn.getFunction() instanceof IdentifierExpressionNode) {
				IdentifierNode calledFunId = ((IdentifierExpressionNode) fcn
						.getFunction()).getIdentifier();

				// Call directly to a function
				if (calledFunId.getEntity() instanceof Function) {
					Function callee = (Function) calledFunId.getEntity();
					addCall(caller, callee);
				} else {
					// Call through an expression (an identifier)
					PointerType pFunType = (PointerType) fcn.getFunction()
							.getConvertedType();
					FunctionType funType = (FunctionType) pFunType
							.referencedType();

					Set<Function> callees = getFunctionsOfAType(funType);

					if (callees != null)
						for (Function callee : callees) {
							addCall(caller, callee);
						}
				}
			} else {
				Type funcExpressionType = fcn.getFunction().getConvertedType();
				FunctionType funType;

				// the type of the function expression in a function call could
				// be either function type or pointer to function type
				if (funcExpressionType instanceof FunctionType) {
					funType = (FunctionType) funcExpressionType;
				} else {
					assert (funcExpressionType instanceof PointerType);
					funType = (FunctionType) ((PointerType) funcExpressionType)
							.referencedType();
				}

				Set<Function> callees = functionsOfAType.get(funType);
				if (callees != null) {
					for (Function callee : callees) {
						addCall(caller, callee);
					}
				}
			}

			// Check arguments for nested calls
			for (ExpressionNode arg : fcn.getArguments()) {
				processFunctionBody(arg, caller);
			}
		} else if (node != null) {
			for (ASTNode child : node.children()) {
				processFunctionBody(child, caller);
			}
		}
	}

	private void processProgram(ASTNode node) {
		if (node instanceof FunctionDefinitionNode) {
			processFunctionDefinitionNode((FunctionDefinitionNode) node);
		} else if (node != null) {
			for (ASTNode child : node.children()) {
				processProgram(child);
			}
		}
	}
	
	@Override
	public void clear(AST unit) {
		functionsOfAType.clear();
		clearNode(unit.getRootNode());
	}

	private void clearNode(ASTNode node) {
		if (node != null) {
			if (node instanceof FunctionDefinitionNode) {
				Function f = ((FunctionDefinitionNode) node).getEntity();
				if (f != null) {
					Set<Function> callers = f.getCallers();
					if (callers != null)
						callers.clear();
					Set<Function> callees = f.getCallees();
					if (callees != null)
						callees.clear();
				}
			}
			for (ASTNode child : node.children()) {
				clearNode(child);
			}
		}
	}

	@Override
	public void analyze(AST unit) throws SyntaxException {
		// functions of a type is temporary map used during analysis of an AST
		functionsOfAType.clear();
		
		ASTNode root = unit.getRootNode();

		collectProgram(root);
		processProgram(root);
		
		functionsOfAType.clear();
	}
	
	static private void collectReachableCalls(Function f, Set<Function> funs) {
		if (!funs.contains(f)) {
			funs.add(f);
			for (Function callee : f.getCallees()) {
				collectReachableCalls(callee, funs);
			}
		}
	}
	
	static public Set<Function> functions(AST unit) {
		Set<Function> functionsAnalyzed = new HashSet<Function>();
		Function mainFun = unit.getMain();
		if (mainFun != null) {
			collectReachableCalls(mainFun, functionsAnalyzed);
		}
		return functionsAnalyzed;
	}
	
	static public void printCallGraph(AST unit) {
		System.out.println("Functions in call graph:");
		for (Function f : functions(unit)) {
			System.out.println("   "+f);
		}
		System.out.println();
	}

}
