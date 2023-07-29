package edu.udel.cis.vsl.abc.transform.common;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Stack;

import edu.udel.cis.vsl.abc.ast.entity.IF.ProgramEntity;
import edu.udel.cis.vsl.abc.ast.entity.IF.Scope.ScopeKind;
import edu.udel.cis.vsl.abc.ast.entity.IF.Typedef;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.NodeFactory;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.DeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.FieldDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.TypedefDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.BlockItemNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.ArrayTypeNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.EnumerationTypeNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.PointerTypeNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.StructureOrUnionTypeNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.TypeNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.TypedefNameNode;

public class TypeDefinitionDependenciesFinder {

	/**
	 * A reference to the {@link NodeFactory}
	 */
	private NodeFactory nodeFactory;

	public TypeDefinitionDependenciesFinder(NodeFactory nodeFactory) {
		this.nodeFactory = nodeFactory;
	}

	/**
	 * <p>
	 * Computes all type declarations that the given type (node) depends on.
	 * This method assumes that only declarations but not definitions are needed
	 * as dependencies.
	 * </p>
	 * <p>
	 * One exception would be the type definitions referred by the type
	 * declarations that are in block scopes. These kind of definitions will be
	 * included as dependencies in case the returned declarations are used in
	 * outer scopes (it one does so, the declarations and the definition are no
	 * longer refers to the same entity).
	 * </p>
	 * 
	 * @param typeNode
	 * @return All type declarations that the given type (node) depends on.
	 */
	public LinkedHashSet<BlockItemNode> getDependentDeclarations(
			TypeNode typeNode) {
		return getAllDependentDeclarations(typeNode, true);
	}

	/**
	 * *
	 * <p>
	 * Computes all type definitions that the given type (node) depends on. This
	 * method assumes that all the dependencies can give a full definition of
	 * the given type node.
	 * </p>
	 * 
	 * @param typeNode
	 * @return
	 */
	public LinkedHashSet<BlockItemNode> getDependentDefinitions(
			TypeNode typeNode) {
		return getAllDependentDeclarations(typeNode, false);
	}

	/**
	 * <p>
	 * Returns a declaration node which contains definitions given by the giving
	 * parameter "type". Returns null if the giving "type" contains no
	 * definition.
	 * </p>
	 * 
	 * <p>
	 * Examples: 1) Giving a typedef name <code>T</code>, returns null; <br>
	 * 2) Giving type <code>struct _t</code>, returns null;<br>
	 * 3) Giving type <code>struct _t {int x;} *</code>, returns
	 * <code>struct _t {int x;};</code>
	 * </p>
	 * 
	 * 
	 * @param type
	 * @return
	 */
	public DeclarationNode getDefinition(TypeNode type) {
		switch (type.kind()) {
			case STRUCTURE_OR_UNION :
				StructureOrUnionTypeNode su = (StructureOrUnionTypeNode) type;

				if (su.getStructDeclList() != null)
					return su;
				else
					return null;
			case POINTER :
				PointerTypeNode ptr = (PointerTypeNode) type;

				return getDefinition(ptr.referencedType());
			case ARRAY :
				ArrayTypeNode arr = (ArrayTypeNode) type;

				return getDefinition(arr.getElementType());
			default :
		}
		return null;
	}

	/**
	 * 
	 * @param typeNode
	 *            the type node whose dependencies will be computed
	 * @param ignoreDefinitionUnlessNecessary
	 *            set true to use type delcarations instead of type definitions
	 *            for all types defined in file scopes.
	 * @return
	 */
	private LinkedHashSet<BlockItemNode> getAllDependentDeclarations(
			TypeNode typeNode, boolean ignoreDefinitionUnlessNecessary) {
		LinkedHashSet<BlockItemNode> dependencies = new LinkedHashSet<>();
		Stack<TypeNode> unprocessedTypes = new Stack<>();

		unprocessedTypes.push(typeNode);
		getAllDependentDeclarationsWorker(dependencies, new HashSet<>(),
				unprocessedTypes, ignoreDefinitionUnlessNecessary);
		return dependencies;
	}

	/**
	 * 
	 * @param dependentDefis
	 *            a set of known dependencies
	 * @param processingTypedefName
	 *            the typedef names that are going through the analysis. It any
	 *            of them appears in the processing types, they can be ignored
	 *            (de-cycle).
	 * @param unprocessedDecls
	 *            a stack of unprocessed types
	 * @param ignoreDefinitionUnlessNecessary
	 *            set true to use type delcarations instead of type definitions
	 *            for all types defined in file scopes.
	 */
	@SuppressWarnings("unlikely-arg-type")
	private void getAllDependentDeclarationsWorker(
			LinkedHashSet<BlockItemNode> dependentDefis,
			Set<TypedefNameNode> processingTypedefName,
			Stack<TypeNode> unprocessedDecls,
			boolean ignoreDefinitionUnlessNecessary) {
		while (!unprocessedDecls.isEmpty()) {
			TypeNode typeNode = unprocessedDecls.pop();

			if (dependentDefis.contains(typeNode))
				continue;
			switch (typeNode.kind()) {
				case TYPEDEF_NAME :
					getAllDependenciesFromTypedefName(
							(TypedefNameNode) typeNode, processingTypedefName,
							dependentDefis, ignoreDefinitionUnlessNecessary);
					break;
				case STRUCTURE_OR_UNION :
					getAllDependenciesFromStructOrUnion(
							(StructureOrUnionTypeNode) typeNode,
							processingTypedefName, dependentDefis,
							ignoreDefinitionUnlessNecessary);
					break;
				case ENUMERATION :
					getAllDependenciesFromEnumeration(
							(EnumerationTypeNode) typeNode, dependentDefis);
					break;
				case POINTER :
					getAllDependenciesFromPointer((PointerTypeNode) typeNode,
							processingTypedefName, dependentDefis,
							ignoreDefinitionUnlessNecessary);
					break;
				case ARRAY :
					getAllDependenciesFromArray((ArrayTypeNode) typeNode,
							processingTypedefName, dependentDefis,
							ignoreDefinitionUnlessNecessary);
					break;
				default :
			}
		}
		return;
	}

	/**
	 * Add dependencies of a type node in a typedef declaration to the "output"
	 * argument.
	 */
	private void getAllDependenciesFromTypedefName(TypedefNameNode typedefName,
			Set<TypedefNameNode> processingTypedefName,
			LinkedHashSet<BlockItemNode> dependentDefis,
			boolean ignoreDefinitionUnlessNecessary) {
		// If the definition of this typedef name is in file scope, no need to
		// look into its definition, unless (ignoreDefinitionUnlessNecessary ==
		// false):
		ProgramEntity entity = (ProgramEntity) typedefName.getName()
				.getEntity();
		TypedefDeclarationNode definition = (TypedefDeclarationNode) entity
				.getDefinition();

		if (!ignoreDefinitionUnlessNecessary && definition != null
				&& definition.getScope().getScopeKind() == ScopeKind.FILE) {
			dependentDefis
					.add((TypedefDeclarationNode) entity.getFirstDeclaration());
			return;
		}

		// de-cycle:
		if (processingTypedefName.contains(typedefName)) {
			// cycle over a typedef declaration happens. A cycle
			// example would be:
			// typedef struct S T;
			// struct S {
			// T * next;
			// ..
			// }
			// Hence a pure declaration for the typedef is needed:
			TypedefDeclarationNode pureTypedefDecl = (TypedefDeclarationNode) entity
					.getFirstDeclaration();
			// Note that the first one must be a pure declaration
			// otherwise cycle will not happen.

			dependentDefis.add(pureTypedefDecl);
			return;
		}

		TypeNode typedefTypeNode;
		Stack<TypeNode> unprocessed = new Stack<>();
		Typedef typedef = (Typedef) entity;

		assert typedef != null;
		if (definition != null) {
			typedefTypeNode = definition.getTypeNode();
			unprocessed.push(typedefTypeNode);
			processingTypedefName.add(typedefName);
			getAllDependentDeclarationsWorker(dependentDefis,
					processingTypedefName, unprocessed,
					ignoreDefinitionUnlessNecessary);
			dependentDefis.add(definition);
			processingTypedefName.remove(typedefName);
		} else {
			definition = (TypedefDeclarationNode) typedef.getFirstDeclaration();
			dependentDefis.add(definition);
		}
	}

	private void getAllDependenciesFromStructOrUnion(
			StructureOrUnionTypeNode structOrUnion,
			Set<TypedefNameNode> processingTypedefName,
			LinkedHashSet<BlockItemNode> dependentDefis,
			boolean ignoreDefinitionUnlessNecessary) {
		Stack<TypeNode> unprocessed = new Stack<>();

		if (structOrUnion.getStructDeclList() == null) {
			ProgramEntity entity = (ProgramEntity) structOrUnion.getEntity();
			StructureOrUnionTypeNode definition = (StructureOrUnionTypeNode) entity
					.getDefinition();

			// If the definition of this struct is in a block scope, move it to
			// top and recursively find more dependencies unless
			// (ignoreDefinitionUnlessNecessary == true)
			if (!ignoreDefinitionUnlessNecessary && definition != null
					&& definition.getScope()
							.getScopeKind() == ScopeKind.BLOCK) {
				StructureOrUnionTypeNode castedDefinition = (StructureOrUnionTypeNode) definition;

				for (FieldDeclarationNode field : castedDefinition
						.getStructDeclList())
					unprocessed.push(field.getTypeNode());
				getAllDependentDeclarationsWorker(dependentDefis,
						processingTypedefName, unprocessed,
						ignoreDefinitionUnlessNecessary);
				dependentDefis.add(castedDefinition);

				// replace definition occurence with a pure declaration:
				ASTNode parent = definition.parent();
				int childIdx = definition.childIndex();

				if (parent == null)
					return;
				definition.remove();
				parent.setChild(childIdx, nodeFactory.newStructOrUnionTypeNode(
						definition.getSource(), castedDefinition.isStruct(),
						castedDefinition.getTag().copy(), null));
			}
		} else {
			// this node is definition already, so it is not a dependency:
			for (FieldDeclarationNode field : structOrUnion.getStructDeclList())
				unprocessed.push(field.getTypeNode());
			getAllDependentDeclarationsWorker(dependentDefis,
					processingTypedefName, unprocessed,
					ignoreDefinitionUnlessNecessary);
		}
	}

	private void getAllDependenciesFromEnumeration(EnumerationTypeNode enumType,
			LinkedHashSet<BlockItemNode> dependentDefis) {
		// Enumeration definition must come before declaration.
		// If the node is already a definition, no further action.
		if (enumType.enumerators() == null) {
			// If the node is not a definition, find out the definition and add
			// it to the dependencies list.
			ProgramEntity entity = (ProgramEntity) enumType.getEntity();
			EnumerationTypeNode definition = (EnumerationTypeNode) entity
					.getDefinition();

			// replace definition occurence with a pure declaration:
			dependentDefis.add(definition);

			ASTNode parent = definition.parent();
			int childIdx = definition.childIndex();

			definition.remove();
			parent.setChild(childIdx, nodeFactory.newEnumerationTypeNode(
					definition.getSource(), definition.getTag().copy(), null));
		}
	}

	private void getAllDependenciesFromPointer(PointerTypeNode pointerType,
			Set<TypedefNameNode> processingTypedefName,
			LinkedHashSet<BlockItemNode> dependentDefis,
			boolean ignoreDefinitionUnlessNecessary) {
		TypeNode referredTypeNode = pointerType.referencedType();
		Stack<TypeNode> unprocessed = new Stack<>();

		unprocessed.push(referredTypeNode);
		getAllDependentDeclarationsWorker(dependentDefis, processingTypedefName,
				unprocessed, ignoreDefinitionUnlessNecessary);
	}

	private void getAllDependenciesFromArray(ArrayTypeNode arrayType,
			Set<TypedefNameNode> processingTypedefName,
			LinkedHashSet<BlockItemNode> dependentDefis,
			boolean ignoreDefinitionUnlessNecessary) {
		TypeNode elementTypeNode = arrayType.getElementType();
		Stack<TypeNode> unprocessed = new Stack<>();

		unprocessed.push(elementTypeNode);
		getAllDependentDeclarationsWorker(dependentDefis, processingTypedefName,
				unprocessed, ignoreDefinitionUnlessNecessary);
	}
}
