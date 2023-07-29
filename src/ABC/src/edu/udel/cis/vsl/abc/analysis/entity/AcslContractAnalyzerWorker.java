package edu.udel.cis.vsl.abc.analysis.entity;

import java.util.HashMap;
import java.util.Map;

import edu.udel.cis.vsl.abc.ast.conversion.IF.ConversionFactory;
import edu.udel.cis.vsl.abc.ast.entity.IF.BehaviorEntity;
import edu.udel.cis.vsl.abc.ast.entity.IF.Function;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.IdentifierNode;
import edu.udel.cis.vsl.abc.ast.node.IF.SequenceNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.AllocationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.AssignsOrReadsNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.AssumesNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.BehaviorNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.CallEventNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.CompletenessNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.CompositeEventNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.ContractNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.ContractNode.ContractKind;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.DependsEventNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.DependsEventNode.DependsEventNodeKind;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.DependsNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.EnsuresNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.GuardsNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.InvariantNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.MPICollectiveBlockNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.MPIUsesNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.MemoryEventNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.RequiresNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.WaitsforNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode.ExpressionKind;
import edu.udel.cis.vsl.abc.ast.type.IF.AcslMPISeqType;
import edu.udel.cis.vsl.abc.ast.type.IF.SetType;
import edu.udel.cis.vsl.abc.ast.type.IF.StructureOrUnionType;
import edu.udel.cis.vsl.abc.ast.type.IF.Type;
import edu.udel.cis.vsl.abc.ast.type.IF.Type.TypeKind;
import edu.udel.cis.vsl.abc.token.IF.SyntaxException;
import edu.udel.cis.vsl.abc.token.IF.UnsourcedException;

public class AcslContractAnalyzerWorker {
	/**
	 * The entity analyzer controlling this declaration analyzer.
	 */
	private EntityAnalyzer entityAnalyzer;

	private Map<String, BehaviorEntity> definedBehaviors = new HashMap<>();

	private ExpressionAnalyzer expressionAnalyzer;

	private AbsentAssertionAnalyzer absentAssertionAnalyzer;
	
	private final String MPI_COMM_TYPE_NAME = "MPI_Comm";

	AcslContractAnalyzerWorker(EntityAnalyzer entityAnalyzer,
			ConversionFactory conversionFactory) {
		this.entityAnalyzer = entityAnalyzer;
		this.expressionAnalyzer = this.entityAnalyzer.expressionAnalyzer;
		this.absentAssertionAnalyzer = new AbsentAssertionAnalyzer();
	}

	/**
	 * Do entity analysis on a whole contract block of a function.
	 * 
	 * @param contract
	 * @param result
	 * @throws SyntaxException
	 */
	void processContractNodes(SequenceNode<ContractNode> contract,
			Function result) throws SyntaxException {
		for (ContractNode contractClause : contract) {
			processContractNode(contractClause);
			result.addContract(contractClause);
		}
	}

	/**
	 * <p>
	 * Check if the given expression is a (set of) lvalue expression(s). An
	 * expression that is a (set of) lvalue expression(s) iff:
	 * <ul>
	 * <li>{@link ExpressionNode#isLvalue()} returns true and the expression is
	 * of a (set-of) scalar type</li>
	 * <li>or is of (set-of) {@link AcslMPISeqType}.</li>
	 * <li>or is a NOTHING expression;</li>
	 * </ul>
	 * </p>
	 * 
	 * @param expr
	 *            an expression node
	 * @return true iff the given <code>expr</code> is an lvalue expression or a
	 *         set of lvalue expressions
	 * @throws SyntaxException
	 *             when the given expression has mixed mem type
	 */
	private boolean isLvalueOfMemorySet(ExpressionNode expr)
			throws SyntaxException {
		if (expr.isLvalue())
			if (expr.getType().kind() != TypeKind.ARRAY)
				if (isMemoryLocationSet(expr))
					return true;
		// both READS and ASSIGNS accepts \nothing and \mpi_region:
		if (expr.expressionKind() == ExpressionKind.NOTHING)
			// or the expression is \nothing:
			return true;
		return false;
	}
	
	/**
	 * <p>
	 * Check if the given expression is a "memory location set" expression. An
	 * expression is a "memory location set expression" iff it has (set-of)
	 * scalar type or (set-of) {@link AcslMPISeqType} and is a "lvalue"
	 * expression.
	 * </p>
	 * 
	 * @param expr
	 * @return true iff the given expression is a "memory location set"
	 *         expression; false, otherwise
	 * @throws SyntaxException
	 *             when the given expression has mixed mem type
	 */
	private boolean isMemoryLocationSet(ExpressionNode expr)
			throws SyntaxException {
		Type type = expr.getType();

		if (type.kind() == TypeKind.SET)
			type = ((SetType) type).elementType();
		if (!type.isScalar() && !(type instanceof AcslMPISeqType))
			return false;
		return expr.isLvalue();
	}

	// TODO: MPIContractExpression type checking!

	/**
	 * Recursively process entity analysis for a {@link ContractNode}.
	 *
	 * @param contractClause
	 *         a contract clause
	 * @throws SyntaxException
	 *         when a contract expression has syntax errors
	 */
	private void processContractNode(ContractNode contractClause)
			throws SyntaxException {
		ContractKind contractKind = contractClause.contractKind();

		switch (contractKind) {
			case ALLOCATES_OR_FREES : {
				AllocationNode allocation = (AllocationNode) contractClause;
				SequenceNode<ExpressionNode> memoryList = allocation
						.memoryList();

				for (ExpressionNode memory : memoryList) {
					expressionAnalyzer.processExpression(memory);
					if (!expressionAnalyzer.typeFactory
							.isPointerType(memory.getConvertedType()))
						throw this.error("The expression "
								+ memory.prettyRepresentation()
								+ " doesn't have pointer type and thus "
								+ "can't be used as the operand of allocates/frees",
								allocation);
				}
				break;
			}
			case ASSIGNS_READS : {
				AssignsOrReadsNode assignsOrReads = (AssignsOrReadsNode) contractClause;
				SequenceNode<ExpressionNode> expressionList = assignsOrReads
						.getMemoryList();
				int numExpressions = expressionList.numChildren();

				for (int i = 0; i < numExpressions; i++) {
					ExpressionNode expression = expressionList
							.getSequenceChild(i);

					expressionAnalyzer.processExpression(expression);
					if (!isLvalueOfMemorySet(expression)) {
						throw error("The expression "
								+ expression.prettyRepresentation()
								+ " doesn't designate an object or a set of memory locations and thus "
								+ "can't be used as the left argument of assigns/reads",
								assignsOrReads);
					}
				}
				break;
			}
			case DEPENDS : {
				DependsNode depends = (DependsNode) contractClause;
				// ExpressionNode condition = depends.getCondition();
				SequenceNode<DependsEventNode> eventList = depends
						.getEventList();

				for (DependsEventNode event : eventList) {
					processDependsEvent(event);
				}
				break;
				// int numEvents = eventList.numChildren();
				//
				// if (condition != null)
				// entityAnalyzer.expressionAnalyzer
				// .processExpression(condition);
				// for (int i = 0; i < numEvents; i++) {
				// ExpressionNode event = eventList.getSequenceChild(i);
				//
				// entityAnalyzer.expressionAnalyzer
				// .processExpression(event);
				// }
			}
			case GUARDS : {
				ExpressionNode expression = ((GuardsNode) contractClause)
						.getExpression();

				expressionAnalyzer.processExpression(expression);
				break;
			}
			/* *********************************************************** */
			/* ** Contracts stored in functions without categorization: ** */
			case ASSUMES : {
				AssumesNode assumesNode = (AssumesNode) contractClause;
				ExpressionNode expression = assumesNode.getPredicate();

				expressionAnalyzer.processExpression(expression);
				break;
			}
			case BEHAVIOR : {
				BehaviorNode behavior = (BehaviorNode) contractClause;
				String name = behavior.getName().name();
				SequenceNode<ContractNode> body = behavior.getBody();

				if (definedBehaviors.containsKey(name))
					throw this.error("re-definition of behavior named as "
							+ name + ": the previous definition was at "
							+ definedBehaviors.get(name).getBehavior()
									.getSource().getSummary(false, true),
							contractClause);
				else
					this.definedBehaviors.put(name, entityAnalyzer.entityFactory
							.newBehavior(name, behavior));
				for (ContractNode subClause : body) {
					processContractNode(subClause);
				}
				break;
			}
			case COMPLETENESS : {
				CompletenessNode completeNode = (CompletenessNode) contractClause;
				SequenceNode<IdentifierNode> idList = completeNode.getIDList();

				if (idList != null) {
					for (IdentifierNode id : idList) {
						BehaviorEntity behavior = this.definedBehaviors
								.get(id.name());

						if (behavior == null)
							throw this.error("undefined behavior " + id.name(),
									id);
						id.setEntity(behavior);
					}
				}
				break;
			}
			case REQUIRES : {
				ExpressionNode expression = ((RequiresNode) contractClause)
						.getExpression();
				boolean hasAbsentAssertion;

				expressionAnalyzer.processExpression(expression);
				hasAbsentAssertion = absentAssertionAnalyzer.
						processRequirementOrGuarantee(expression, true);
				((RequiresNode) contractClause).setIsRequirement(hasAbsentAssertion);
				break;
			}
			case ENSURES: {
				ExpressionNode expression = ((EnsuresNode) contractClause)
						.getExpression();
				boolean hasAbsentAssertion;

				expressionAnalyzer.processExpression(expression);
				hasAbsentAssertion = absentAssertionAnalyzer.
						processRequirementOrGuarantee(expression, false);
				((EnsuresNode) contractClause).setIsGuarantee(hasAbsentAssertion);
				break;
			}
			case PURE : {
				break;
			}
			case MPI_COLLECTIVE : {
				MPICollectiveBlockNode collective_block = (MPICollectiveBlockNode) contractClause;

				expressionAnalyzer
						.processExpression(collective_block.getMPIComm());
				for (ContractNode colClause : collective_block.getBody())
					processContractNode(colClause);
				break;
			}
			case MPI_USES: {
				MPIUsesNode mpiUsesNode = (MPIUsesNode) contractClause;

				for (ExpressionNode comm : mpiUsesNode.getArguments()) {
					expressionAnalyzer.processExpression(comm);
					if (!isMPICommType(comm.getType())) {
						throw error(comm.prettyRepresentation()
								+ "must be an MPI communicator", comm);
					}
				}
				break;
			}
			case WAITSFOR : {
				WaitsforNode waitsforNode = (WaitsforNode) contractClause;

				for (ExpressionNode arg : waitsforNode.getArguments())
					expressionAnalyzer.processExpression(arg);
				break;
			}
			default :
				throw error("Unknown kind of contract clause", contractClause);
		}
	}

	private boolean isCompatibleWithMemoryType(Type type) {
		if (type.kind() == TypeKind.MEM)
			return true;
		return expressionAnalyzer.typeFactory.isPointerType(type);
	}

	private void processDependsEvent(DependsEventNode event)
			throws SyntaxException {
		DependsEventNodeKind kind = event.getEventKind();

		switch (kind) {
			case MEMORY : {
				MemoryEventNode rwEvent = (MemoryEventNode) event;
				SequenceNode<ExpressionNode> memoryList = rwEvent
						.getMemoryList();

				for (ExpressionNode memory : memoryList) {
					this.expressionAnalyzer.processExpression(memory);
					if (!isCompatibleWithMemoryType(memory.getConvertedType()))
						throw this.error(
								"the operand of \\write/\\read/\\access doesn't have valid memory type",
								memory);
					// memory.addConversion(this.conversionFactory.memoryConversion(memory.getConvertedType()));
				}
				break;
			}
			case ANYACT :
			case NOACT :
				break;
			case CALL : {
				CallEventNode call = (CallEventNode) event;
				SequenceNode<ExpressionNode> arguments = call.arguments();

				this.expressionAnalyzer.processIdentifierExpression(
						call.getFunction(), true, true);

				if (arguments != null) {
					for (ExpressionNode arg : arguments) {
						this.expressionAnalyzer.processExpression(arg);
					}
				}
				break;
			}
			case COMPOSITE : {
				CompositeEventNode composite = (CompositeEventNode) event;

				this.processDependsEvent(composite.getLeft());
				this.processDependsEvent(composite.getRight());
				break;
			}
			default :
				throw error("Unknown kind of depends event", event);
		}

	}

	private SyntaxException error(String message, ASTNode node) {
		return entityAnalyzer.error(message, node);
	}

	@SuppressWarnings("unused")
	private SyntaxException error(UnsourcedException e, ASTNode node) {
		return entityAnalyzer.error(e, node);
	}

	void processLoopContractNodes(SequenceNode<ContractNode> loopContracts)
			throws SyntaxException {
		for (ContractNode clause : loopContracts) {
			switch (clause.contractKind()) {
				case INVARIANT :
					InvariantNode loopInvari = ((InvariantNode) clause);

					expressionAnalyzer
							.processExpression(loopInvari.getExpression());
					break;
				case ASSIGNS_READS :
					AssignsOrReadsNode assignsNode = (AssignsOrReadsNode) clause;

					if (assignsNode.isReads())
						throw error(
								"Unexpected loop contract clause: " + clause,
								clause);
					for (ExpressionNode mem : assignsNode.getMemoryList())
						expressionAnalyzer.processExpression(mem);
					break;
				default :
					throw error("Unknown kind of loop contracts: "
							+ clause.contractKind(), clause);
					// Check expression types
			}
		}
	}
	
	/**
	 * 
	 * @param type
	 * @return true iff the given type represents <code>MPI_Comm</code>
	 */
	private boolean isMPICommType(Type type) {
		if (type.kind() == TypeKind.STRUCTURE_OR_UNION) {
			StructureOrUnionType structType = (StructureOrUnionType) type;

			return structType.getName().equals(MPI_COMM_TYPE_NAME);
		}
		return false;
	}
}
