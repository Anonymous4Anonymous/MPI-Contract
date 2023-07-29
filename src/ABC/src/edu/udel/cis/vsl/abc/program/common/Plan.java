package edu.udel.cis.vsl.abc.program.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import edu.udel.cis.vsl.abc.ast.entity.IF.ProgramEntity;
import edu.udel.cis.vsl.abc.ast.entity.IF.TaggedEntity;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.TypedefDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.TypeNode;

/**
 * A plan on how to transform an AST to prepare it for merging.
 * 
 * @author xxxx
 * 
 */
public class Plan {

	/**
	 * The set of entities in the translation unit whose definitions should be
	 * nullified.
	 */
	private Collection<TaggedEntity> defDeleteSet = new LinkedList<>();

	/**
	 * Mapping from entity that must be renamed to its new name.
	 */
	private Map<ProgramEntity, String> renameMap = new HashMap<>();

	/**
	 * Entities whose declarations should be deleted from the AST.
	 */
	private Collection<ProgramEntity> entityRemoveSet = new LinkedList<>();

	/**
	 * The set of {@link TypedefDeclarationNode} that need to be unwrap (Unwrap
	 * a {@link TypedefDeclarationNode} means converting the
	 * {@link TypedefDeclarationNode} into the {@link TypeNode} it wraps).
	 */
	private Collection<TypedefDeclarationNode> typedefUnwrapSet = new LinkedList<>();

	public Plan() {
	}

	public void addMakeIncompleteAction(TaggedEntity entity) {
		defDeleteSet.add(entity);
	}

	public void addRenameAction(ProgramEntity entity, String newName) {
		renameMap.put(entity, newName);
	}

	public void addEntityRemoveAction(ProgramEntity entity) {
		entityRemoveSet.add(entity);
	}

	public void addTypedefUnwrapAction(TypedefDeclarationNode declarationNode) {
		typedefUnwrapSet.add(declarationNode);
	}

	public Iterable<TaggedEntity> getMakeIncompleteActions() {
		return defDeleteSet;
	}

	public Map<ProgramEntity, String> getRenameMap() {
		return renameMap;
	}

	public Iterable<ProgramEntity> getEntityRemoveActions() {
		return entityRemoveSet;
	}

	public Iterable<TypedefDeclarationNode> getTypeDefUnwrapActions() {
		return typedefUnwrapSet;
	}
}
