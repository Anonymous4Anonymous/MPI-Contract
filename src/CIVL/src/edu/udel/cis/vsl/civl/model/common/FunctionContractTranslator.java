package edu.udel.cis.vsl.civl.model.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import edu.udel.cis.vsl.abc.ast.entity.IF.Function;
import edu.udel.cis.vsl.abc.ast.node.IF.SequenceNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.AssignsOrReadsNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.AssumesNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.BehaviorNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.CallEventNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.CompositeEventNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.ContractNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.DependsEventNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.DependsEventNode.DependsEventNodeKind;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.DependsNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.EnsuresNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.GuardsNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.MPICollectiveBlockNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.MemoryEventNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.RequiresNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.WaitsforNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.civl.config.IF.CIVLConfiguration;
import edu.udel.cis.vsl.civl.model.IF.CIVLFunction;
import edu.udel.cis.vsl.civl.model.IF.CIVLSource;
import edu.udel.cis.vsl.civl.model.IF.CIVLSyntaxException;
import edu.udel.cis.vsl.civl.model.IF.CIVLTypeFactory;
import edu.udel.cis.vsl.civl.model.IF.CIVLUnimplementedFeatureException;
import edu.udel.cis.vsl.civl.model.IF.ModelFactory;
import edu.udel.cis.vsl.civl.model.IF.Scope;
import edu.udel.cis.vsl.civl.model.IF.contract.CallEvent;
import edu.udel.cis.vsl.civl.model.IF.contract.CompositeEvent.CompositeEventOperator;
import edu.udel.cis.vsl.civl.model.IF.contract.ContractFactory;
import edu.udel.cis.vsl.civl.model.IF.contract.DependsEvent;
import edu.udel.cis.vsl.civl.model.IF.contract.DependsEvent.DependsEventKind;
import edu.udel.cis.vsl.civl.model.IF.contract.FunctionBehavior;
import edu.udel.cis.vsl.civl.model.IF.contract.FunctionContract;
import edu.udel.cis.vsl.civl.model.IF.contract.FunctionContract.ContractKind;
import edu.udel.cis.vsl.civl.model.IF.contract.MPICollectiveBehavior;
import edu.udel.cis.vsl.civl.model.IF.contract.MPICollectiveBehavior.MPICommunicationPattern;
import edu.udel.cis.vsl.civl.model.IF.contract.NamedFunctionBehavior;
import edu.udel.cis.vsl.civl.model.IF.expression.BinaryExpression.BINARY_OPERATOR;
import edu.udel.cis.vsl.civl.model.IF.expression.Expression;
import edu.udel.cis.vsl.civl.model.IF.expression.Nothing;
import edu.udel.cis.vsl.civl.model.IF.variable.Variable;
import edu.udel.cis.vsl.civl.model.common.contract.CommonContractFactory;
import edu.udel.cis.vsl.civl.util.IF.Pair;

public class FunctionContractTranslator extends FunctionTranslator {

	private CIVLFunction function;

	private ModelFactory modelFactory;

	private ModelBuilderWorker modelBuilder;

	private ContractFactory contractFactory = new CommonContractFactory();

	/**
	 * Current contract kind: {@link ContractKind} which informs the current
	 * contract kind during recursive parsing. {@link ContractKind} depends on a
	 * contract clause, which cannot be nested, so no need to use a stack.
	 */
	@SuppressWarnings("unused")
	private ContractKind currentContractKind;

	/**
	 * A set of variables that are required to be the exactly same at the
	 * beginning of a verifying function.
	 */
	private List<Variable> agreedVaraibles;

	/**
	 * This field informs title of the current parsing MPI collective behavior
	 * block. It consists of an MPI communicator expression and a
	 * {@link MPICommunicationPattern}. MPI collective behavior blocks cannot be
	 * nested, so no need to use a stack.
	 */
	private Pair<Expression, MPICommunicationPattern> currentMPICollectiveTitle;

	/******************** Constructor ********************/
	FunctionContractTranslator(ModelBuilderWorker modelBuilder,
			ModelFactory modelFactory, CIVLTypeFactory typeFactory,
			CIVLFunction function, CIVLConfiguration civlConfig) {
		super(modelBuilder, modelFactory, function, civlConfig);
		this.modelFactory = modelFactory;
		this.modelBuilder = modelBuilder;
		this.function = function;
		this.currentMPICollectiveTitle = new Pair<>(null, null);
	}

	public void translateFunctionContract(SequenceNode<ContractNode> contract) {
		FunctionContract result = contractFactory
				.newFunctionContract(modelFactory.sourceOf(contract));

		for (ContractNode clause : contract) {
			this.translateContractNode(clause, result);
		}
		this.function.setFunctionContract(result);
	}

	private void translateContractNode(ContractNode contractNode,
			FunctionContract functionContract) {
		this.translateContractNodeWork(contractNode, functionContract, null,
				null);
	}

	/**
	 * Translates a {@link ContractNode} to a component of a
	 * {@link FunctionContract}.
	 * 
	 * The function takes at most three main components:
	 * {@link FunctionContract}, {@link MPICollectiveBehavior} and
	 * {@link NamedFunctionBehavior}. According to the syntax:
	 * <p>
	 * <ol>
	 * <li>None of them can be nested.</li>
	 * <li>{@link NamedFunctionBehavior} can appear in {@link FunctionContract}
	 * or {@link MPICollectiveBehavior}</li>
	 * <li>{@link MPICollectiveBehavior} can only appear in
	 * {@link FunctionContract}</li>
	 * <li>{@link FunctionContract} denotes the whole group of function
	 * contracts for a function. For each function, it can has at most one
	 * {@link FunctionContract}.</li>
	 * </ol>
	 * </p>
	 * Thus, the specifications for different kind of contracts is as follows:
	 * <p>
	 * <ol>
	 * <li>A {@link NamedFunctionBehavior} will be added as a component of a
	 * {@link MPICollectiveBehavior} if it is non-null, else as of a
	 * {@link FunctionContract}.</li>
	 * <li>{@link ASSUMES} can only be added as a component of a
	 * {@link NamedFunctionBehavior}</li>
	 * <li>Other contract clauses will be added as a component of one of the
	 * three main blocks with such a precedence:
	 * <code>{@link NamedFunctionBehavior} higher than
	 * {@link MPICollectiveBehavior} high than {@link FunctionContract}<code>
	 * </li>
	 * </ol>
	 * </p>
	 * 
	 * @param contractNode
	 * @return
	 */
	private void translateContractNodeWork(ContractNode contractNode,
			FunctionContract functionContract,
			MPICollectiveBehavior collectiveBehavior,
			NamedFunctionBehavior behavior) {
		CIVLSource source = modelFactory.sourceOf(contractNode);
		Scope scope = function.outerScope();
		FunctionBehavior targetBehavior = behavior != null
				? behavior
				: collectiveBehavior != null
						? collectiveBehavior
						: functionContract.defaultBehavior();

		switch (contractNode.contractKind()) {
			case ASSIGNS_READS : {
				AssignsOrReadsNode assignsOrReads = (AssignsOrReadsNode) contractNode;
				boolean isAssigns = assignsOrReads.isAssigns();
				SequenceNode<ExpressionNode> muNodes = assignsOrReads
						.getMemoryList();

				for (ExpressionNode muNode : muNodes) {
					Expression mu = this.translateExpressionNode(muNode, scope,
							true);

					if (mu instanceof Nothing) {
						if (isAssigns) {
							if (targetBehavior.numAssignsMemoryUnits() == 0)
								targetBehavior.setAssingsNothing();
							else
								throw new CIVLSyntaxException(
										"assigns \\nothing conflicts with previous assigns clause",
										source);
						} else {
							if (targetBehavior.numReadsMemoryUnits() == 0)
								targetBehavior.setReadsNothing();
							else
								throw new CIVLSyntaxException(
										"reads \\nothing conflicts with previous reads clause",
										source);
						}
					} else {
						if (isAssigns) {
							if (targetBehavior.assignsNothing())
								throw new CIVLSyntaxException(
										"assigns clause conflicts with previous assigns \\nothing",
										source);
							targetBehavior.addAssignsMemoryUnit(mu);
						} else {
							if (targetBehavior.readsNothing())
								throw new CIVLSyntaxException(
										"reads clause conflicts with previous reads \\nothing",
										source);
							targetBehavior.addReadsMemoryUnit(mu);
						}
					}
				}
				break;
			}
			case ASSUMES : {
				assert targetBehavior instanceof NamedFunctionBehavior;
				Expression expression = translateExpressionNode(
						((AssumesNode) contractNode).getPredicate(), scope,
						true);
				Expression existedAssumptions;

				if ((existedAssumptions = behavior.assumptions()) != null) {
					CIVLSource spanedSource = modelFactory.sourceOfSpan(
							existedAssumptions.getSource(),
							expression.getSource());

					expression = modelFactory.binaryExpression(spanedSource,
							BINARY_OPERATOR.AND, existedAssumptions,
							expression);
				}
				behavior.setAssumption(expression);
				break;
			}
			case BEHAVIOR : {
				assert behavior == null;
				BehaviorNode behaviorNode = (BehaviorNode) contractNode;
				NamedFunctionBehavior namedBehavior = this.contractFactory
						.newNamedFunctionBehavior(source,
								behaviorNode.getName().name());
				SequenceNode<ContractNode> body = behaviorNode.getBody();

				for (ContractNode item : body) {
					this.translateContractNodeWork(item, functionContract,
							collectiveBehavior, namedBehavior);
				}
				if (collectiveBehavior != null)
					collectiveBehavior.addNamedBehaviors(namedBehavior);
				else
					functionContract.addNamedBehavior(namedBehavior);
				break;
			}

			case DEPENDS : {
				DependsNode dependsNode = (DependsNode) contractNode;
				SequenceNode<DependsEventNode> eventNodes = dependsNode
						.getEventList();

				for (DependsEventNode eventNode : eventNodes) {
					DependsEvent event = this.translateDependsEvent(eventNode,
							scope);

					if (event.dependsEventKind() == DependsEventKind.NOACT) {
						if (targetBehavior.numDependsEvents() > 0)
							throw new CIVLSyntaxException(
									"depends \\noact conflicts with previous depends clause",
									source);
						targetBehavior.setDependsNoact();
					} else if (event
							.dependsEventKind() == DependsEventKind.ANYACT) {
						if (targetBehavior.dependsNoact())
							throw new CIVLSyntaxException(
									"depends \\anyact conflicts with previous depends \\noact clause",
									source);
						targetBehavior.setDependsAnyact();
					} else
						targetBehavior.addDependsEvent(event);
				}
				if (targetBehavior.dependsAnyact())
					targetBehavior.clearDependsEvents();
				break;
			}
			case ENSURES : {
				currentContractKind = ContractKind.ENSURES;
				Expression expression = translateExpressionNode(
						((EnsuresNode) contractNode).getExpression(), scope,
						true);
				targetBehavior.addPostcondition(expression);
				currentContractKind = null;
				break;
			}
			case REQUIRES : {
				currentContractKind = ContractKind.REQUIRES;
				Expression expression = translateExpressionNode(
						((RequiresNode) contractNode).getExpression(), scope,
						true);
				targetBehavior.addPrecondition(expression);
				currentContractKind = null;
				break;
			}
			case GUARDS : {
				Expression guard = this.translateExpressionNode(
						((GuardsNode) contractNode).getExpression(), scope,
						true);

				functionContract
						.setGuard(modelFactory.booleanExpression(guard));
				break;
			}
			case MPI_COLLECTIVE :
				MPICollectiveBehavior newCollectiveBehavior;
				Variable[] agreedVariablesCopy;
				MPICollectiveBlockNode collectiveBlockNode = (MPICollectiveBlockNode) contractNode;

				currentMPICollectiveTitle.left = translateExpressionNode(
						collectiveBlockNode.getMPIComm(), scope, true);
				// Since MPI_Collective behavior cannot be nested, such a global
				// collection will be correct, other wise, it will be over
				// written:
				agreedVaraibles = new LinkedList<>();
				newCollectiveBehavior = translateMPICollectiveBehavior(
						(MPICollectiveBlockNode) contractNode, scope,
						functionContract);
				agreedVariablesCopy = new Variable[agreedVaraibles.size()];
				agreedVaraibles.toArray(agreedVariablesCopy);
				newCollectiveBehavior.setAgreedVariables(agreedVariablesCopy);
				currentMPICollectiveTitle.left = null;
				currentMPICollectiveTitle.right = null;
				agreedVaraibles = null;
				functionContract
						.addMPICollectiveBehavior(newCollectiveBehavior);
				break;
			case WAITSFOR :
				WaitsforNode waitsforNode = (WaitsforNode) contractNode;
				List<Expression> arguments = new LinkedList<>();

				for (ExpressionNode arg : waitsforNode.getArguments()) {
					Expression argExpr = translateExpressionNode(arg, scope,
							true);

					if (!argExpr.getExpressionType().isIntegerType())
						if (argExpr
								.expressionKind() != Expression.ExpressionKind.REGULAR_RANGE)
							throw new CIVLSyntaxException(
									"waitsfor clause only accepts arguments of integer type or regualr range type");
					arguments.add(argExpr);
				}
				targetBehavior.setWaitsforList(arguments);
				functionContract.setHasMPIWaitsfor(true);
				break;
			case PURE :
				functionContract.setPure(true);
				break;
			case COMPLETENESS :
			default :
				throw new CIVLUnimplementedFeatureException(
						"Translate Procedure ContractNode with "
								+ contractNode.contractKind());
		}
	}

	private DependsEvent translateDependsEvent(DependsEventNode eventNode,
			Scope scope) {
		DependsEventNodeKind kind = eventNode.getEventKind();
		CIVLSource source = this.modelFactory.sourceOf(eventNode);

		switch (kind) {
			case MEMORY : {
				MemoryEventNode readWriteEvent = (MemoryEventNode) eventNode;
				Set<Expression> muSet = new HashSet<>();
				SequenceNode<ExpressionNode> muNodeSet = readWriteEvent
						.getMemoryList();
				DependsEventKind memoryKind;

				for (ExpressionNode muNode : muNodeSet) {
					muSet.add(
							this.translateExpressionNode(muNode, scope, true));
				}
				switch (readWriteEvent.memoryEventKind()) {
					case READ :
						memoryKind = DependsEventKind.READ;
						break;
					case WRITE :
						memoryKind = DependsEventKind.WRITE;
						break;
					default :// REACH
						memoryKind = DependsEventKind.REACH;
				}
				return this.contractFactory.newMemoryEvent(source, memoryKind,
						muSet);
			}
			case CALL : {
				CallEventNode callEvent = (CallEventNode) eventNode;
				Pair<Function, CIVLFunction> functionPair = this
						.getFunction(callEvent.getFunction());
				SequenceNode<ExpressionNode> argumentNodes = callEvent
						.arguments();
				List<Expression> arguments = new ArrayList<>();
				CallEvent call;

				for (ExpressionNode argNode : argumentNodes) {
					arguments.add(
							this.translateExpressionNode(argNode, scope, true));
				}
				call = this.contractFactory.newCallEvent(source,
						functionPair.right, arguments);
				if (functionPair.right == null)
					this.modelBuilder.callEvents.put(call, functionPair.left);
				return call;
			}
			case COMPOSITE : {
				CompositeEventNode compositeEvent = (CompositeEventNode) eventNode;
				CompositeEventOperator operator;
				DependsEvent left, right;

				switch (compositeEvent.eventOperator()) {
					case UNION :
						operator = CompositeEventOperator.UNION;
						break;
					case DIFFERENCE :
						operator = CompositeEventOperator.DIFFERENCE;
						break;
					case INTERSECT :
						operator = CompositeEventOperator.INTERSECT;
						break;
					default :
						throw new CIVLUnimplementedFeatureException(
								"unknown kind of composite event operatore: "
										+ compositeEvent.eventOperator(),
								source);
				}
				left = this.translateDependsEvent(compositeEvent.getLeft(),
						scope);
				right = this.translateDependsEvent(compositeEvent.getRight(),
						scope);
				return this.contractFactory.newCompositeEvent(source, operator,
						left, right);
			}
			case ANYACT :
				return this.contractFactory.newAnyactEvent(source);
			case NOACT :
				return this.contractFactory.newNoactEvent(source);
			default :
				throw new CIVLUnimplementedFeatureException(
						"unknown kind of depends event: " + kind, source);
		}

	}

	/**
	 * Translate a {@link MPICollectiveBlockNode} to a
	 * {@link MPICollectiveBehavior}.
	 * 
	 * @param node
	 * @param scope
	 * @return
	 */
	private MPICollectiveBehavior translateMPICollectiveBehavior(
			MPICollectiveBlockNode node, Scope scope,
			FunctionContract functionContract) {
		MPICollectiveBehavior result;
		CIVLSource source = modelFactory.sourceOf(node);
		Iterator<ContractNode> bodyNodesIter = node.getBody().iterator();
		Expression communicator = translateExpressionNode(node.getMPIComm(),
				scope, true);

		result = contractFactory.newMPICollectiveBehavior(source, communicator);
		while (bodyNodesIter.hasNext())
			translateContractNodeWork(bodyNodesIter.next(), functionContract,
					result, null);
		return result;
	}
}
