package edu.udel.cis.vsl.abc.main;

import java.io.PrintStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import edu.udel.cis.vsl.abc.ast.IF.AST;
import edu.udel.cis.vsl.abc.ast.entity.IF.Entity;
import edu.udel.cis.vsl.abc.ast.entity.IF.ProgramEntity;
import edu.udel.cis.vsl.abc.ast.node.IF.IdentifierNode;
import edu.udel.cis.vsl.abc.ast.node.IF.SequenceNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.DeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.BlockItemNode;
import edu.udel.cis.vsl.abc.token.IF.CivlcToken;
import edu.udel.cis.vsl.abc.token.IF.Formation;
import edu.udel.cis.vsl.abc.token.IF.Inclusion;
import edu.udel.cis.vsl.abc.token.IF.Source;
import edu.udel.cis.vsl.abc.token.IF.SourceFile;

/**
 * Summarizes the source files and entities defined in an AST.
 * 
 * Iterate over all source files, and for each, the entities declared or defined
 * in that file that are needed.
 * 
 * Iterate over all external defns in the AST. For each, record sourcefile of
 * the decl or defn.
 * 
 * Map: sourceFile -> Set of entity
 * 
 * If A includes B and entity is defined in B...?
 * 
 * Iterate over all tokens and accumulate the last source file (i.e., B, the
 * file immediately containing the token) and the external defn in which that
 * token occurs, if any
 * 
 * Alternative: iterate over leaf AST nodes only, get the source, iterate over
 * tokens
 * 
 * for each token : get file and containing external def
 * 
 * alternative: if A includes B includes C and token occurs in C with source
 * (A->B->C) then add A, B, and C to set
 * 
 * print the map.
 * 
 * Iterate over all external defs. For each: get the Entity it is declaring or
 * defining (if any). Look at source of identifier. process as above
 * 
 * @author xxxx
 *
 */
public class Summarizer {

	private Map<SourceFile, Set<Entity>> fileMap = new TreeMap<>();

	private AST ast;

	Set<Entity> allEntities = new HashSet<>();

	public Summarizer(AST ast) {
		this.ast = ast;
		analyze();
	}

	private void getFilesOf(CivlcToken token, Collection<SourceFile> files) {
		Formation formation = token.getFormation();
		SourceFile sourceFile = formation.getLastFile();

		files.add(sourceFile);
		if (formation instanceof Inclusion) {
			CivlcToken includeToken = ((Inclusion) formation).getIncludeToken();

			if (includeToken != null)
				getFilesOf(includeToken, files);
		}
	}

	@SuppressWarnings("unused")
	private Collection<SourceFile> getFilesOf(CivlcToken token) {
		HashSet<SourceFile> sourceFiles = new HashSet<>();

		getFilesOf(token, sourceFiles);
		return sourceFiles;
	}

	private void add(SourceFile sourceFile, Entity entity) {
		Set<Entity> set = fileMap.get(sourceFile);

		if (set == null) {
			set = new HashSet<Entity>();
			fileMap.put(sourceFile, set);
		}
		set.add(entity);
	}

	private void analyze() {
		SequenceNode<BlockItemNode> root = ast.getRootNode();

		for (BlockItemNode itemNode : root) {
			if (itemNode instanceof DeclarationNode) {
				DeclarationNode decl = (DeclarationNode) itemNode;
				Entity entity = decl.getEntity();

				if (entity instanceof ProgramEntity) {
					DeclarationNode defnNode = ((ProgramEntity) entity)
							.getDefinition();

					// don't list declarations that are not defns
					if (defnNode != null && defnNode != itemNode)
						continue;
				}

				IdentifierNode idNode = decl.getIdentifier();

				allEntities.add(entity);
				if (idNode != null) {
					Source source = idNode.getSource();
					CivlcToken token = source.getFirstToken();

					if (token != null) {
						SourceFile sf = token.getSourceFile();

						add(sf, entity);
						/*
						 * Collection<SourceFile> sourceFiles =
						 * getFilesOf(token); for (SourceFile sf : sourceFiles)
						 * { add(sf, entity); }
						 */
					}
				}
			}
		}
	}

	public void print(PrintStream out) {
		out.println("Required source files:");
		for (SourceFile sourceFile : fileMap.keySet()) {
			out.println(
					sourceFile.getIndexName() + "\t: " + sourceFile.getPath());
		}
		for (Entry<SourceFile, Set<Entity>> entry : fileMap.entrySet()) {
			SourceFile sourceFile = entry.getKey();

			out.println();
			out.println("Entities used from " + sourceFile.getIndexName() + ": "
					+ sourceFile.getPath() + " :");
			for (Entity entity : entry.getValue()) {
				out.println("  " + entity.getName());
			}
		}
		out.flush();
	}

}
