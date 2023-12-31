/**
 * Copyright (c) 2005, 2006 Los Alamos National Security, LLC.  This
 * material was produced under U.S. Government contract DE-
 * AC52-06NA25396 for Los Alamos National Laboratory (LANL), which is
 * operated by the Los Alamos National Security, LLC (LANS) for the
 * U.S. Department of Energy. The U.S. Government has rights to use,
 * reproduce, and distribute this software. NEITHER THE GOVERNMENT NOR
 * LANS MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR ASSUMES ANY
 * LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified to
 * produce derivative works, such modified software should be clearly
 * marked, so as not to confuse it with the version available from
 * LANL.
 *  
 * Additionally, this program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package edu.udel.cis.vsl.abc.front.fortran.preproc;

import java.util.ArrayList;
import java.util.Stack;

import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;

import edu.udel.cis.vsl.abc.token.IF.CivlcToken;
import edu.udel.cis.vsl.abc.token.IF.Formation;
import edu.udel.cis.vsl.abc.token.IF.Tokens;

//import fortran.ofp.FrontEnd;

public class MFortranLexicalPrepass {
	private MFortranTokenStream tokens;
	private Stack<Token> doLabels;
	private boolean isFixedForm;
	private Formation inclusion;

	public MFortranLexicalPrepass(TokenStream tokens) {
		assert (tokens.size() > 0);
		this.tokens = new MFortranTokenStream((CivlcToken) tokens.get(0));
		this.doLabels = new Stack<Token>();
		this.inclusion = Tokens.newTokenFactory()
				.newInclusion(((CivlcToken) tokens.get(0)).getSourceFile());
		this.isFixedForm = ((CivlcToken) tokens.get(0)).getSourceFile()
				.getName().toLowerCase().endsWith(".f");

	}

	/*******
	 * OBSOLETE private boolean isAssignment(int start, int end) { if
	 * (tokens.getToken(start).getType() == MFortranLexer.ASSIGNMENT && (start+3
	 * < end) && tokens.getToken(start+1).getType() == MFortranLexer.LPAREN &&
	 * tokens.getToken(start+2).getType() == MFortranLexer.EQUALS) { return
	 * true; } else { return false; } } // end isAssignment() END OBSOLETE
	 ********/

	/*******
	 * OBSOLETE private boolean isOperator(int start, int end) { if
	 * (tokens.getToken(start).getType() == MFortranLexer.OPERATOR && (start+3 <
	 * end) && tokens.getToken(start+1).getType() == MFortranLexer.LPAREN &&
	 * tokens.getToken(start+2).getType() == MFortranLexer.DEFINED_OP &&
	 * tokens.getToken(start+3).getType() == MFortranLexer.RPAREN) { return
	 * true; } else { return false; } } // end isOperator() END OBSOLETE
	 ********/

	/**
	 * Convert keyword tokens (except for generic-spec items) from start to end
	 * to identifiers. Tokens to convert often represent expressions which can
	 * be primaries which can be array constructors. So must look for occurrence
	 * of an array constructor of form [ type_spec_stuff :: ... ] and don't
	 * convert the intrisic-type-spec keyword.
	 */
	private int convertToIdents(int start, int end) {
		int i, tkType;
		int[] indices = new int[2];
		Token tk = null;

		for (i = start; i < end; i++) {
			// get the token
			tk = tokens.getToken(i);
			tkType = tk.getType();

			// This is often a list so skip commas (or EOS) to save time
			if (tkType == MFortranLexer.COMMA || tkType == MFortranLexer.EOS)
				continue;

			indices[0] = i;
			indices[1] = end;
			if (arrayConstructorIndices(indices) != -1) {
				// this could also match a coarray reference but that's ok
				i = fixupArrayConstructor(indices);
			}

			if (tokens.isKeyword(tk) == true) {
				// generic-spec items should not be converted (unless an id),
				// i.e.,
				// ASSIGNMENT(=) OPERATOR(DEFINED_OP), READ(UN/FORMATTED),
				// WRITE()
				int idOffset = matchGenericSpec(i, end);
				if (idOffset == i) {
					// an ident
					tk.setType(MFortranLexer.IDENT);
				} else if (idOffset != -1) {
					// skip over tokens in parens from matchGenericSpec
					i = idOffset - 1;
				}
			}
		} // end for (number of tokens in line)

		return end;
	} // end convertToIdents()

	/**
	 * TODO: Need to finish this to skip over anything in quotes and hollerith
	 * constants. Actually, the tokens already sucks up quotes (single and
	 * double) into the CHAR_CONSTANT tokens that it creates, so no need to
	 * consider here.
	 */
	public int salesScanForToken(int start, int desiredToken) {
		int lookAhead = 0;
		int tk;
		@SuppressWarnings("unused")
		int parenOffset;

		// if this line is a comment, skip scanning it
		if (tokens.currLineLA(1) == MFortranLexer.LINE_COMMENT) {
			return -1;
		}

		// start where the user says to
		lookAhead = start;
		do {
			// lookAhead was initialized to 0
			lookAhead++;

			// get the token and consume it (advances token index)
			tk = tokens.currLineLA(lookAhead);

			// if have a left paren, find the matching right paren. must
			// add one to lookAhead for starting index because
			// lookAhead is 0 based indexing and currLineLA() needs 1 based.
			if (tk == MFortranLexer.LPAREN || tk == MFortranLexer.LBRACKET) {
				parenOffset = tokens.findToken(lookAhead - 1,
						MFortranLexer.LPAREN);
				parenOffset++;
				// lookAhead should be the exact lookAhead of where we found
				// the LPAREN or LBRACKET.
				lookAhead = matchClosingParen(lookAhead);
				tk = tokens.currLineLA(lookAhead);
			}
		} while (tk != MFortranLexer.EOF && tk != MFortranLexer.EOS
				&& tk != desiredToken);

		if (tk == desiredToken) {
			// found what we wanted, need to subtract one because 0 based
			// indexing
			return lookAhead - 1;
		}

		return -1;
	} // end salesScanForToken()

	/**
	 * Returns true if the token at i is a name. Since at this stage a keyword
	 * can also be a name, a name is (id | keyword).
	 */
	private boolean matchName(int i) {
		Token tk = tokens.getToken(i);
		if (tokens.isKeyword(tk) || tk.getType() == MFortranLexer.IDENT) {
			return true;
		}
		return false;
	}

	/**
	 * Return the indices of a type-spec, or -1 if not a type-spec.
	 *
	 * A type-spec is a name|keyword followed by optional parens. Note: but
	 * could mistakenly match a function call so have to be careful with
	 * interpreting results.
	 */
	private int matchTypeSpec(int[] indices) {
		int offset = indices[0];

		indices[0] = indices[1] = -1;

		// must start with a name
		if (matchName(offset)) {
			indices[0] = offset++;
			// skip optional parens
			if (tokens.getToken(offset).getType() == MFortranLexer.LPAREN) {
				indices[1] = matchClosingParen(offset + 1);
			}
			if (indices[1] == -1)
				indices[0] = -1;
			else
				indices[1] -= 1;
		}
		return indices[0];
	}

	private boolean matchIfConstStmt(int lineStart, int lineEnd) {
		int rparenOffset = -1;
		int commaOffset = -1;

		// lineStart should be the physical index of the start (0, etc.)
		// currLinLA() is 1 based, so must add one to everything
		int tkType = tokens.currLineLA(lineStart + 1);
		if ((tkType == MFortranLexer.IF || tkType == MFortranLexer.ELSEIF)
				&& tokens.currLineLA(lineStart + 2) == MFortranLexer.LPAREN) {
			rparenOffset = matchClosingParen(lineStart + 2);
			commaOffset = salesScanForToken(rparenOffset + 1,
					MFortranLexer.COMMA);
			if (rparenOffset == -1) {
				System.err.println("Error in IF stmt at line: "
						+ tokens.getToken(0).getLine());
				return false;
			}

			// if we have a THEN token, everything between if and then are ids
			// this is an if_construct in the grammar
			if (tokens.currLineLA(rparenOffset + 1) == MFortranLexer.THEN) {
				convertToIdents(lineStart + 1, rparenOffset);
				// match an if_construct
				return true;
			} else if (commaOffset != -1 && tkType == MFortranLexer.IF && tokens
					.currLineLA(rparenOffset + 1) == MFortranLexer.DIGIT_STR) {
				// The arithmetic if requires a label COMMA label
				// COMMA label. We can distinguish between
				// arithmetic_if_stmt and if_stmt by verifying that the
				// first thing after the RPAREN is a label, and it is
				// immediately followed by a COMMA

				// (label)? IF LPAREN expr RPAREN label COMMA label
				// COMMA label EOS
				// convert everything after IF to ident if necessary
				convertToIdents(lineStart + 1, rparenOffset);
				// insert a token into the start of the line to signal that this
				// is an arithmetic if and not an if_stmt so the parser doesn't
				// have to backtrack for action_stmt.
				// 02.05.07
				// tokens.addToken(lineStart, MFortranLexer.ARITHMETIC_IF_STMT,
				// "__ARITHMETIC_IF_STMT__", inclusion);

				// matched an arithmetic if
				return false; // This feature is deleted
			} else {
				// TODO: must be an if_stmt, which is matched elsewhere (for
				// now..)
				return false;
			}
		}

		return false;
	}// end matchIfConstStmt()

	private boolean matchElseStmt(int lineStart, int lineEnd) {
		int tokenType;

		// lineStart should be physical index to start (0 based). add 1 to
		// make it one based.
		tokenType = tokens.currLineLA(lineStart + 1);
		if (tokenType == MFortranLexer.ELSE) {
			// see if there are any tokens following the else
			if (lineEnd >= 2) {
				if (tokens.currLineLA(lineStart + 2) == MFortranLexer.WHERE) {
					// ELSE WHERE stmt. anything after these two are idents
					convertToIdents(lineStart + 2, lineEnd);
				} else {
				}
			}

			return true;
		}
		return false;
	} // end matchElseStmt()

	private boolean matchDataDecl(int lineStart, int lineEnd) {
		int tokenType = tokens.currLineLA(lineStart + 1);
		if (isIntrinsicType(tokenType) == true || isPrefixToken(tokenType)
				|| ((tokenType == MFortranLexer.TYPE
						|| tokenType == MFortranLexer.CLASS)
						&& tokens.currLineLA(
								lineStart + 2) == MFortranLexer.LPAREN)) {

			// If a subroutine, then this is handled elsewhere.
			if (isSubDecl(lineStart, lineEnd)) {
				return false;
			}

			// Test to see if it's a function decl. If it is not, then
			// it has to be a data decl
			if (isFuncDecl(lineStart, lineEnd) == true) {
				fixupFuncDecl(lineStart, lineEnd);
			} else {
				// should have a variable declaration here
				fixupDataDecl(lineStart, lineEnd);
			}

			// We either matched a data decl or a function, but either way,
			// the line has been matched.
			return true;

		} else if (tokenType == MFortranLexer.FUNCTION) {
			// could be a function defn. that starts with the function keyword
			// instead of the type. fix it up.
			fixupFuncDecl(lineStart, lineEnd);
			return true;
		}

		// didn't match the line.
		return false;
	} // end matchDataDecl()

	/**
	 * Note: 'TYPE IS' part of a 'SELECT TYPE' statement is matched here because
	 * there isn't a way to know which one it is.
	 */
	private boolean matchDerivedTypeStmt(int lineStart, int lineEnd) {
		int colonOffset;
		Token identToken = null;
		int identOffset;

		// make sure it's a derived type defn, and not a declaration!
		if (tokens.currLineLA(lineStart + 1) == MFortranLexer.TYPE
				&& tokens.currLineLA(lineStart + 2) != MFortranLexer.LPAREN) {
			// we have a derived type defn.
			colonOffset = tokens.findToken(lineStart,
					MFortranLexer.COLON_COLON);
			if (colonOffset != -1) {
				// there was a double colon; ident immediately follows it
				identOffset = colonOffset + 1;
				// we know that it is not a 'TYPE IS' inside a 'SELECT TYPE'
				// convert everything after :: to idents
				convertToIdents(identOffset, lineEnd);
			} else {
				// offset lineStart+1 is the second token
				identToken = tokens.getToken(lineStart + 1);
				identOffset = lineStart + 1;
				// make sure the name is an identifier
				if (tokens.isKeyword(identToken) == true) {
					identToken.setType(MFortranLexer.IDENT);
				}

				// see if there are parens after the type name. if there
				// are, we're looking at a 'TYPE IS' and need to handle the
				// derived_type_spec or intrinsic_type_spec
				// note: we're guaranteed to have at least 3 tokens
				if (tokens.currLineLA(lineStart + 3) == MFortranLexer.LPAREN) {
					int rparenOffset;
					// matchClosingParen returns the lookAhead (1 based);
					// we want the offset (0 based), so subtract 1 from it.
					rparenOffset = matchClosingParen(lineStart + 3) - 1;
					// if the third token is a left paren, we have a 'type is'
					// and need to figure out what the type_spec is
					if (isIntrinsicType(
							tokens.currLineLA(lineStart + 4)) == true) {
						// we can't change the intrinsic type, but have to
						// handle the optional kind
						// selector, if given. Fixup the intrinsic_type_spec,
						// which is the third token
						fixupDeclTypeSpec(lineStart + 3, lineEnd);
					} else {
						// we have a 'type is' with a derived type name, so
						// convert everything on line to idents after '('
						convertToIdents(lineStart + 3, lineEnd);
					} // end else

					// have to see if a label is after the right paren and
					// convert it to an ident if necessary
					// lineEnd is 1 based; rparenOffset 0 based. convert
					// lineEnd to 0 based before testing
					if ((lineEnd - 1) > (rparenOffset + 1)) {
						// rparenOffset 0 based; convert to 1 based to get it's
						// lookAhead value, then lookAhead 1 more to see what
						// follows it (i.e., rparenOffset+2 is desired
						// lookAhead)
						if (tokens.isKeyword(
								tokens.currLineLA(rparenOffset + 2)) == true) {
							tokens.getToken(rparenOffset + 1)
									.setType(MFortranLexer.IDENT);
						}
					}
				} // end if(is a 'type is')
			} // end else(no :: is derived-type-stmt)

			return true;
		}

		return false;
	}// end matchDerivedTypeStmt()

	private boolean matchSub(int lineStart, int lineEnd) {
		int bindOffset;

		// Move past the pure, elemental, and recursive keywords.
		while (isPrefixToken(tokens.currLineLA(lineStart + 1))) {
			lineStart++;
		}

		// look for a bind statement
		bindOffset = tokens.findToken(lineStart, MFortranLexer.BIND);
		if (bindOffset != -1) {
			// use the BIND token as a marker for the end of the subroutine
			// name and any args.
			convertToIdents(lineStart + 1, bindOffset + lineStart - 1);
		} else {
			// convert any keyword in line after first token to ident
			convertToIdents(lineStart + 1, lineEnd);
		}

		return true;
	} // end matchSub()

	/**
	 * Match the various types of end statments. For example: END,
	 * ENDSUBROUTINE, ENDDO, etc.
	 */
	private boolean matchEnd(int lineStart, int lineEnd) {
		int tokenType;
		int identOffset;
		boolean matchedEnd = false;
		boolean isEndDo = false;

		// initialize to -1. if we find a END, this will be set to
		// the location of the identifier, if given.
		identOffset = -1;

		tokenType = tokens.currLineLA(lineStart + 1);
		if (tokenType == MFortranLexer.END) {
			if (lineEnd > 2) {
				// Laksono 2009.03.03: we will match the token with the keyword
				// "end with team"
				int nextToken = tokens.currLineLA(lineStart + 2);
				// the next token
				int nextNextToken = tokens.currLineLA(lineStart + 3);
				// the next of the next token

				if (nextToken == MFortranLexer.BLOCK && tokens
						.currLineLA(lineStart + 3) == MFortranLexer.DATA) {
					// end-block-data-stmt
					identOffset = lineStart + 3;
				} else if (nextToken == MFortranLexer.INTERFACE) {
					// have to accept a generic_spec
					identOffset = matchGenericSpec(lineStart + 2, lineEnd);
				} else if (nextNextToken == MFortranLexer.TEAM) {
					// Laksono 2009.03.03:
					// We have the perfect match of "end team", so push the
					// token 3 steps
					identOffset = lineStart + 3;

				} else {
					// identifier is after the END and <construct>
					identOffset = lineStart + 2;
				}
			}

			// we have to fixup the END DO if it's labeled
			if (tokens.currLineLA(lineStart + 2) == MFortranLexer.DO) {
				isEndDo = true;
			}
			matchedEnd = true;
		}
		// else if (tokenType == MFortranLexer.ENDBLOCK) {
		// if (tokens.currLineLA(lineStart + 2) == MFortranLexer.DATA) {
		// // end-block-data-stmt
		// identOffset = lineStart + 2;
		// } else {
		// // end-block-stmt
		// identOffset = lineStart + 1;
		// }
		// matchedEnd = true;
		// } else if (tokenType == MFortranLexer.ENDINTERFACE) {
		// identOffset = matchGenericSpec(lineStart + 1, lineEnd);
		// }
		else {
			if (lineEnd > 1)
				identOffset = lineStart + 1;
			matchedEnd = true;
		}

		if (identOffset != -1) {
			// only converting one thing, so not necessary to use a method..
			convertToIdents(identOffset, lineEnd);
		}

		// have to fixup a labeled END DO
		if (isEndDo == true /* || tokenType == MFortranLexer.ENDDO */ ) {
			fixupLabeledEndDo(lineStart, lineEnd);
		}

		return matchedEnd;
	} // end matchEnd()

	/**
	 * Note: This must occur after checking for a procedure declaration!
	 */
	private boolean matchModule(int lineStart, int lineEnd) {
		// convert everything after module to an identifier
		convertToIdents(lineStart + 1, lineEnd);
		return true;
	}// end matchModule()

	/**
	 * Attempt to match a submodule
	 */
	private boolean matchSubmodule(int lineStart, int lineEnd) {
		// convert everything after submodule to an identifier
		convertToIdents(lineStart + 1, lineEnd);
		return true;
	} // end matchSubmodule()

	/**
	 * Match block-stmt or block-data-stmt
	 */
	private boolean matchBlockOrBlockData(int lineStart, int lineEnd) {
		// there should be a minimum of 2 tokens so do a quick check
		// (IDENT COLON)? BLOCK
		// BLOCK DATA (IDENT)? EOS
		// BLOCKDATA (IDENT)? EOS
		//
		if (lineEnd < (lineStart + 2)) {
			return false;
		}

		if (tokens.currLineLA(lineStart + 1) == MFortranLexer.BLOCK) {
			if (tokens.currLineLA(lineStart + 2) == MFortranLexer.EOS) {
				// successfully matched a block-stmt
				return true;
			} else if (tokens.currLineLA(lineStart + 2) == MFortranLexer.DATA) {
				// BLOCK DATA (IDENT)? EOS
				if ((lineEnd >= (lineStart + 3)) && tokens
						.isKeyword(tokens.currLineLA(lineStart + 3)) == true) {
					// lookAhead 3 is index 2
					tokens.getToken(lineStart + 2).setType(MFortranLexer.IDENT);
				}
				// successfully matched a block data stmt
				return true;
			}

			// unsuccessfully matched a block data stmt
			return false;
		} else if (tokens.currLineLA(lineStart + 1) == MFortranLexer.BLOCK) {
			if (tokens.currLineLA(lineStart + 2) == MFortranLexer.DATA) {
				// BLOCK DATA (IDENT)? EOS
				if ((lineEnd >= (lineStart + 3)) && tokens
						.isKeyword(tokens.currLineLA(lineStart + 3)) == true) {
					// lookAhead 3 is index 2
					tokens.getToken(lineStart + 2).setType(MFortranLexer.IDENT);
				}
				// successfully matched a block data stmt
				return true;
			}

			// unsuccessfully matched a block data stmt
			return false;
		} else if (tokens
				.currLineLA(lineStart + 1) == MFortranLexer.BLOCKDATA) {
			if (tokens.isKeyword(tokens.currLineLA(lineStart + 2)) == true) {
				// lookAhead 2 is index 1
				tokens.getToken(lineStart + 1).setType(MFortranLexer.IDENT);
			}
			// successfully matched a block data stmt
			return true;
		} else {
			// unsuccessfully matched a block data stmt
			return false;
		}

	} // end matchBlockOrBlockData()

	private boolean matchUseStmt(int lineStart, int lineEnd) {
		int identPos;
		int colonOffset;

		// search for the only token, so we can reset it to a keyword
		// if it's there.
		colonOffset = tokens.findToken(lineStart, MFortranLexer.COLON_COLON);
		if (colonOffset != -1) {
			// everything after the double colons must be treated as ids
			identPos = colonOffset + 1;
		} else {
			// no double colon, so ident starts after the 'use' token
			identPos = lineStart + 1;
		}

		// convert what we need to to idents
		if (tokens.isKeyword(tokens.currLineLA(identPos + 1)))
			// the module name is a keyword so convert it.
			tokens.getToken(identPos).setType(MFortranLexer.IDENT);

		// Skip past the module name.
		identPos++;

		// See if anything follows the module name
		if (identPos < lineEnd) {
			// see if we have an only clause
			if (tokens.currLineLA(identPos + 1) == MFortranLexer.COMMA
					&& tokens.currLineLA(identPos + 2) == MFortranLexer.ONLY)
				// Skip the COMMA, ONLY, and COLON.
				identPos += 3;

			// Convert everything following the module name and optional
			// ONLY (if given) to an identifier if necessary.
			convertToIdents(identPos, lineEnd);
		}

		// matched a use stmt
		return true;
	}// end matchUseStmt()

	/**
	 * This depends on the handling of multi-line statements. This function
	 * assumes that the EOS tokens in a multi-line statement are removed for all
	 * lines except the last. This allows this function to simply test if the
	 * first token on the line is a digit string.
	 */
	private boolean matchLabel(int lineStart, int lineEnd) {
		// assume that if the line starts with a digit string, it
		// must be a label. this requires that the EOS is removed
		// in all lines of a multi-line statement, except for the last!
		if (tokens.currLineLA(1) == MFortranLexer.DIGIT_STR) {
			return true;
		} else {
			return false;
		}
	} // end matchLabel()

	/**
	 * An include line is INCLUDE M_INCLUDE_NAME followed by the tokens in the
	 * first line of the included file. So search for INCLUDE M_INCLUDE_NAME and
	 * return true if they are found. The CHAR_CONSTANT token following INCLUDE
	 * is assigned to channel 99 and is not seen here.
	 */
	private boolean matchInclude(int lineStart, int lineEnd) {
		if (tokens.currLineLA(lineStart + 1) == MFortranLexer.INCLUDE && tokens
				.currLineLA(lineStart + 2) == MFortranLexer.M_INCLUDE_NAME) {
			return true;
		} else {
			return false;
		}
	} // end matchInclude()

	private boolean matchIdentColon(int lineStart, int lineEnd) {
		int secondToken = tokens.currLineLA(lineStart + 2);
		if (secondToken == MFortranLexer.COLON) {
			// line starts with the optional IDENT and COLON
			if (tokens.isKeyword(tokens.currLineLA(lineStart + 1)) == true) {
				// convert keyword to IDENT
				tokens.getToken(lineStart).setType(MFortranLexer.IDENT);
			}
			return true;
		}

		return false;
	} // end matchIdentColon()

	/**
	 * Try matching a procedure statement. Note: This MUST be called BEFORE
	 * calling matchModule(). Also, procedure statements can only occur w/in an
	 * interface block.
	 */
	private boolean matchProcStmt(int lineStart, int lineEnd) {
		int identOffset = -1;

		// make sure we have enough tokens
		if (lineEnd < (lineStart + 2))
			return false;

		if (tokens.currLineLA(lineStart + 1) == MFortranLexer.PROCEDURE
				&& tokens.currLineLA(lineStart + 2) != MFortranLexer.LPAREN) {
			// PROCEDURE ...
			int colonOffset = -1;
			colonOffset = tokens.findToken(lineStart + 1,
					MFortranLexer.COLON_COLON);
			if (colonOffset != -1) {
				identOffset = colonOffset + 1;
			} else {
				identOffset = lineStart + 1;
			}
		} else if (tokens.currLineLA(lineStart + 1) == MFortranLexer.MODULE
				&& tokens
						.currLineLA(lineStart + 2) == MFortranLexer.PROCEDURE) {
			// a module stmt has at most 3 tokens after the optional label:
			// MODULE (IDENT)? EOS
			// but a procedure stmt must have at least 4:
			// MODULE PROCEDURE generic_name_list EOS
			if (lineEnd < (lineStart + 4))
				// it is a module stmt
				return false;
			identOffset = lineStart + 2;
		}

		if (identOffset != -1) {
			convertToIdents(identOffset, lineEnd);
			return true;
		} else {
			return false;
		}
	} // end matchProcStmt()

	/**
	 * Try matching a procedure declaration statement. Note: This is NOT for
	 * procedure statements, and MUST be called AFTER trying to match a
	 * procedure statement.
	 */
	private boolean matchProcDeclStmt(int lineStart, int lineEnd) {
		int lParenOffset;
		int rParenOffset;
		int colonOffset;

		if (tokens.currLineLA(lineStart + 1) == MFortranLexer.PROCEDURE) {
			// found a procedure decl. need to find the parens
			// The left paren should be the next token.
			lParenOffset = lineStart + 1;
			rParenOffset = matchClosingParen(lParenOffset + 1);

			// Don't convert proc-interface since it can be a
			// declaration-type-spec.

			// double colons, if there, must come after the RPAREN
			colonOffset = tokens.findToken(rParenOffset + 1,
					MFortranLexer.COLON_COLON);

			if (colonOffset != -1) {
				// idents start after the double colons
				convertToIdents(colonOffset + 1, lineEnd);
			} else {
				// idents start after the RPAREN
				convertToIdents(rParenOffset + 1, lineEnd);
			}

			return true;
		}

		return false;
	} // end matchProcDeclStmt()

	/**
	 * Try matching an access-stmt. This means we need to process an
	 * access-id-list. An access-id can be IDENT or KEYWORD ( stuff ) where
	 * KEYWORD is one of {OPERATOR, ASSIGNMENT, READ, WRITE}.
	 */
	private boolean matchAccessStmt(int lineStart, int lineEnd) {
		int tk = tokens.currLineLA(lineStart + 1);
		if (tk != MFortranLexer.PUBLIC && tk != MFortranLexer.PRIVATE) {
			return false;
		}

		convertToIdents(lineStart + 1, lineEnd);
		return true;
	}

	private boolean matchAttrStmt(int lineStart, int lineEnd) {
		int firstToken;
		int lParenOffset = -1;
		int rParenOffset = -1;
		int identOffset = -1;

		firstToken = tokens.currLineLA(lineStart + 1);

		switch (firstToken) {

			case MFortranLexer.INTENT :
				lParenOffset = tokens.findToken(lineStart + 1,
						MFortranLexer.LPAREN);
				identOffset = matchClosingParen(lParenOffset + 1);
				break;

			case MFortranLexer.BIND :
				// find the closing paren, starting at first location after the
				// left
				// paren.
				// What follows it is optional :: and the ident(s). The BIND
				// and
				// LPAREN
				// are the first two tokens, so lineStart+2 puts you on the
				// lookahead for LPAREN,
				// which is the starting point for the matching routine.
				identOffset = matchClosingParen(lineStart + 2);
				break;

			case MFortranLexer.PARAMETER :
				// match a parameter stmt
				lParenOffset = tokens.findToken(lineStart + 1,
						MFortranLexer.LPAREN);
				if (lParenOffset == -1) {
					System.err.println("Syntax error in PARAMETER statement");
					System.exit(1);
				}
				// idents start after the LPAREN and stop at the RPAREN
				identOffset = lParenOffset;
				lineEnd = matchClosingParen(lParenOffset + 1);
				break;

			case MFortranLexer.PRIVATE :
			case MFortranLexer.PUBLIC :
				// Match an access-stmt. This means we need to process an
				// access-id-list.
				// An access-id can be IDENT or KEYWORD ( stuff ) where
				// KEYWORD is
				// one of
				// {OPERATOR, ASSIGNMENT, READ, WRITE}.
				return matchAccessStmt(lineStart, lineEnd);

			case MFortranLexer.IMPLICIT :
				// TODO - does this really do anything as identOffset not
				// changed
				// Fixup an implicit statement. Search for the NONE,
				// if given, nothing needs updated because it's an IMPLICIT NONE
				if (tokens.currLineLA(lineStart + 2) != MFortranLexer.NONE) {
					do {
						lParenOffset = tokens.findToken(lineStart,
								MFortranLexer.LPAREN);
						if (lParenOffset != -1) {
							rParenOffset = matchClosingParen(lParenOffset + 1);
							// The first set of parens could be the optional
							// kind
							// selector, or it
							// is the letter designators for the implicit stmt.
							// either way, we can
							// convert anything that's not KIND or LEN to an
							// ident because KIND
							// and LEN can only appear in the kind selector.
							// then,
							// we don't need
							// to look for an optional second paren set.
							for (int i = lParenOffset; i < rParenOffset; i++) {
								if (tokens.isKeyword(tokens.currLineLA(i + 1))
										&& tokens.currLineLA(
												i + 1) != MFortranLexer.KIND
										&& tokens.currLineLA(
												i + 1) != MFortranLexer.LEN) {
									tokens.getToken(i)
											.setType(MFortranLexer.IDENT);
								}
							}

							// There could be another set of parens, if the
							// first
							// set (above) was for
							// the kind selector, then the second set would be
							// for
							// the letter
							// designator(s) (required).
							if (tokens.currLineLA(
									rParenOffset + 1) == MFortranLexer.LPAREN) {
								rParenOffset = matchClosingParen(
										rParenOffset + 1);
							}

							// reset the lineStart so we can accept an
							// implicit_spec_list
							lineStart = rParenOffset;
						} else
							break;
					} while (lineStart < lineEnd && tokens
							.currLineLA(lineStart + 1) != MFortranLexer.EOS);
				}
				break;

			default :
				identOffset = lineStart + 1;
				break;

		} // end switch

		if (identOffset != -1) {
			convertToIdents(identOffset, lineEnd);
			return true;
		} else {
			return false;
		}

	} // end matchAttrStmt()

	/**
	 * Test to see if a token is an opening paren or bracket.
	 */
	private boolean isOpenParen(int token) {
		return token == MFortranLexer.LPAREN || token == MFortranLexer.LBRACKET;
	} // end isOpenParen()

	/**
	 * Test to see if a token is a closing paren or bracket.
	 */
	private boolean isCloseParen(int token) {
		return token == MFortranLexer.RPAREN || token == MFortranLexer.RBRACKET;
	} // end isCloseParen()

	/**
	 * Return the offset of the beginning of an array constructor, ie, the
	 * offset of '[' or the offset of the '/' in the '(' '/' sequence, otherwise
	 * return -1.
	 */
	private int arrayConstructorIndices(int[] indices) {
		int start = indices[0];

		indices[0] = indices[1] = -1;

		// look for '['
		Token tk = tokens.getToken(start);
		if (tk.getType() == MFortranLexer.LBRACKET) {
			indices[0] = start;
			indices[1] = tokens.findToken(start + 1, MFortranLexer.RBRACKET);
		}
		// look for '(' '/'
		else if (tk.getType() == MFortranLexer.LPAREN && tokens
				.getToken(start + 1).getType() == MFortranLexer.SLASH) {
			indices[0] = start + 1;
			indices[1] = tokens.findToken(start + 2, MFortranLexer.SLASH);
			if (indices[1] != -1) {
				indices[1] += 1;
				if (tokens.getToken(indices[1])
						.getType() != MFortranLexer.RPAREN) {
					indices[0] = -1;
					indices[1] = -1;
				}
			}
		}

		// make sure end is found
		if (indices[1] == -1)
			indices[0] = -1;

		return indices[0];
	}

	private int fixupArrayConstructor(int[] indices) {
		int offset = indices[0] + 1;
		int end = indices[1];

		// see if there is a type-spec by looking for "::"
		indices[0] = offset;
		if (matchTypeSpec(indices) != -1) {
			if (tokens.getToken(indices[1] + 1)
					.getType() == MFortranLexer.COLON_COLON) {
				offset = fixupDeclTypeSpec(indices[0], indices[1]) + 1;
			}
		}

		return convertToIdents(offset, end);
	}

	/**
	 * This matches closing paren or bracket even if the match is the wrong
	 * type, i.e., '( ]'. This shouldn't really matter as the parser proper will
	 * work it out and give an error if the match is incorrect
	 * 
	 * WARNING return value is one-based indexing (as is offset, the location of
	 * the LPAREN).
	 */
	private int matchClosingParen(int offset) {
		int lookAhead = 0;
		int tmpTokenType;
		int nestingLevel = 0;

		// offset is the location of the LPAREN
		lookAhead = offset;
		// The parenLevel starts at one because we've matched the
		// left paren before calling this method.
		nestingLevel = 1;
		do {
			lookAhead++;
			tmpTokenType = tokens.currLineLA(lookAhead);
			if (isOpenParen(tmpTokenType)) {
				nestingLevel++;
			} else if (isCloseParen(tmpTokenType)) {
				nestingLevel--;
			}

			// handle the error condition of the user not giving the
			// closing paren(s)
			if ((tmpTokenType == MFortranLexer.EOS
					|| tmpTokenType == MFortranLexer.EOF)
					&& nestingLevel != 0) {
				System.err.println("Error: matchClosingParen(): Missing "
						+ "closing paren (or bracket) on line "
						+ tokens.getToken(lookAhead - 1).getLine() + ":");
				System.err.println("nestingLevel: " + nestingLevel);
				System.err.println("lookAhead is: " + lookAhead);
				tokens.printCurLine();
				System.exit(1);
			}

			// have to continue until we're no longer in a nested
			// paren, and find the matching closing paren
		} while (nestingLevel != 0 || (!isCloseParen(tmpTokenType)
				&& tmpTokenType != MFortranLexer.EOS
				&& tmpTokenType != MFortranLexer.EOF));

		if (isCloseParen(tmpTokenType)) {
			return lookAhead;
		}

		return -1;
	} // end matchClosingParen()

	/**
	 * The token at lineStart initiates an intrinsic or derived type-spec;
	 * convert kind/len expressions to identifiers.
	 */
	private int fixupDeclTypeSpec(int lineStart, int lineEnd) {
		// see if we have a derived type
		if (tokens.getToken(lineStart).getType() == MFortranLexer.TYPE || tokens
				.getToken(lineStart).getType() == MFortranLexer.CLASS) {
			int rparenOffset = -1;
			// left-paren is next token (or we're in trouble)
			if (tokens.getToken(lineStart + 1)
					.getType() != MFortranLexer.LPAREN) {
				System.err.println("Derived type or Class declaration error!");
				System.exit(1);
			}
			rparenOffset = matchClosingParen(lineStart + 2);
			// convert anything between the (..) to idents
			convertToIdents(lineStart + 1, rparenOffset);

			// change it to being 0 based indexing
			return rparenOffset - 1;
		}

		if (tokens.getToken(lineStart + 1).getType() == MFortranLexer.LPAREN) {
			int kindTokenOffset = -1;
			int lenTokenOffset = -1;
			int offsetEnd = matchClosingParen(lineStart + 2);
			kindTokenOffset = tokens.findToken(lineStart + 1,
					MFortranLexer.KIND);
			lenTokenOffset = tokens.findToken(lineStart + 1, MFortranLexer.LEN);

			convertToIdents(lineStart + 1, offsetEnd);

			if (kindTokenOffset != -1 && kindTokenOffset < offsetEnd
					&& tokens.getToken(kindTokenOffset + 1)
							.getType() == MFortranLexer.EQUALS) {
				tokens.getToken(kindTokenOffset).setType(MFortranLexer.KIND);
			}
			if (lenTokenOffset != -1 && lenTokenOffset < offsetEnd
					&& tokens.getToken(lenTokenOffset + 1)
							.getType() == MFortranLexer.EQUALS) {
				tokens.getToken(lenTokenOffset).setType(MFortranLexer.LEN);
			}

			return offsetEnd - 1;
		}

		if (tokens.getToken(lineStart).getType() == MFortranLexer.DOUBLE) {
			// return 0 based index of second token, which is lineStart+1
			lineStart = lineStart + 1;
		}

		return lineStart;
	} // end fixupDeclTypeSpec()

	/**
	 * Token at lineStart has been identified as coming from a
	 * declaration-type-spec so convert appropriate tokens to identifiers.
	 */
	private void fixupDataDecl(int lineStart, int lineEnd) {
		int[] indices = new int[2];
		int identOffset;

		// we know the line started with an intrinsic type-spec, so
		// now, we need to find the identifier(s) involved and convert
		// any of them that are keyword to identifiers.

		// fixup the decl type spec part (which handles any kind selector)
		lineStart = fixupDeclTypeSpec(lineStart, lineEnd);
		identOffset = tokens.findToken(lineStart, MFortranLexer.COLON_COLON);
		if (identOffset != -1) {
			// found the :: so the idents start at identOffset+1
			identOffset++;
		} else {
			// no kind selector and no attributes, so ident(s) should
			// be the next token (0 based indexing)
			identOffset = lineStart + 1;
		}

		// Look for a potential array constructor. This could have a type-spec
		// (which must be followed by "::") and the type-spec will have
		// keywords.
		int equalsOffset = tokens.findToken(identOffset, MFortranLexer.EQUALS);

		while (equalsOffset != -1) {
			convertToIdents(identOffset, equalsOffset); // convert kind/len
														// params in type
			identOffset = equalsOffset + 1;
			indices[0] = identOffset;
			indices[1] = lineEnd;
			if (arrayConstructorIndices(indices) != -1) {
				Token tk = tokens.getToken(indices[0] + 1);
				if (isIntrinsicType(tk.getType())) {
					identOffset = fixupDeclTypeSpec(indices[0] + 1, indices[1]);
					convertToIdents(identOffset, indices[1]);
					identOffset = indices[1] + 1;
				}
			}
			// look for additional array constructors
			equalsOffset = tokens.findToken(identOffset, MFortranLexer.EQUALS);
		}

		// now we have the location of the ident(s). simply loop across
		// any tokens left in this line and convert keywords to idents.
		convertToIdents(identOffset, lineEnd);

		return;
	} // end fixupDataDecl()

	/**
	 * TODO:: make this handle the result clause and bind(c) attribute!
	 */
	private void fixupFuncDecl(int lineStart, int lineEnd) {
		int identOffset;
		int resultOffset;
		int bindOffset;
		int newLineStart = 0;
		Token resultToken = null;
		Token bindToken = null;

		// fixup the kind selector, if given
		newLineStart = fixupDeclTypeSpec(lineStart, lineEnd);
		// bump lineStart to next token if it was modified above
		if (newLineStart != lineStart)
			lineStart = newLineStart + 1;

		// find location of FUNCTION; identifiers start one past it
		identOffset = tokens.findToken(lineStart, MFortranLexer.FUNCTION) + 1;
		// find locations of result clause and bind(c), if exist
		// use the scan function so that it will skip any tokens inside
		// of parens (which, in this case, would make them args)
		resultOffset = salesScanForToken(lineStart, MFortranLexer.RESULT);
		bindOffset = salesScanForToken(lineStart, MFortranLexer.BIND);

		// get the actual tokens for result and bind(c)
		if (resultOffset != -1) {
			resultToken = tokens.getToken(resultOffset);
		}
		if (bindOffset != -1) {
			bindToken = tokens.getToken(bindOffset);
		}

		// convert all keywords after the FUNCTION to identifers to
		// make it easier, and to make sure we catch the result clause id
		// then, afterwards, reset the type of the result and bind tokens
		convertToIdents(identOffset, lineEnd);
		if (resultToken != null) {
			resultToken.setType(MFortranLexer.RESULT);
		}
		if (bindToken != null) {
			// this one probably not necessary because i don't think it
			// is actually considered a keyword by tokens.isKeyword()
			// bindToken.setType(MFortranLexer.BIND_LPAREN_C);
			// TODO - fix for BIND token
			bindToken.setType(MFortranLexer.BIND);
		}

		return;
	}// end fixupFuncDecl()

	private boolean isIntrinsicType(int type) {
		if (type == MFortranLexer.INTEGER || type == MFortranLexer.REAL
				|| type == MFortranLexer.DOUBLE
				|| type == MFortranLexer.DOUBLEPRECISION
				|| type == MFortranLexer.COMPLEX
				|| type == MFortranLexer.CHARACTER
				|| type == MFortranLexer.LOGICAL)
			return true;
		else
			return false;
	}// end isIntrinsicType()

	/**
	 * Find the first index after the typespec (with or without the optional
	 * kind or character-length selector).
	 */
	private int skipTypeSpec(int lineStart) {
		int firstToken;
		int rparenOffset = -1;

		firstToken = tokens.currLineLA(lineStart + 1);
		if (isIntrinsicType(firstToken) == true
				|| firstToken == MFortranLexer.TYPE) {
			// if the first token is DOUBLE, we are expecting one more token
			// to finish the type, so bump the lineStart one more.
			if (firstToken == MFortranLexer.DOUBLE) {
				lineStart++;
			}

			// skip character-length (*char-length) selector if present
			if (firstToken == MFortranLexer.CHARACTER && tokens
					.currLineLA(lineStart + 2) == MFortranLexer.ASTERISK) {
				lineStart += 2;
			}

			// see if the next token is a left paren -- means either a kind
			// selector or a type declaration.
			if (tokens.currLineLA(lineStart + 2) == MFortranLexer.LPAREN) {
				// will return logical index of rparen. this is not zero
				// based! it is based on look ahead, which starts at 1!
				// therefore, if it is 4, it's really at offset 3 in the
				// packed list array, but is currLineLA(4)!
				rparenOffset = matchClosingParen(lineStart + 2);
			}

			if (rparenOffset != -1)
				// rparenOffset will be the logical index of the right paren.
				// if it's token 4 in packedList, which is 0 based, it's actual
				// index is 3, but 4 is returned because we need 1 based for
				// LA()
				lineStart = rparenOffset;
			else {
				lineStart = lineStart + 1;
			}
			return lineStart;
		} else {
			// it wasn't a typespec, so return original start. this should
			// not happen because this method should only be called if we're
			// looking at a typespec!
			return lineStart;
		}
	}// end skipTypeSpec()

	// Skip whole prefix, not just the type declaration.
	private int skipPrefix(int lineStart) {

		// First, skip over the pure, elemental, recursive tokens.
		// Then skip type spec.
		// Then skip over the pure, elemental, recursive tokens again
		while (isPrefixToken(tokens.currLineLA(lineStart + 1)))
			lineStart++;

		lineStart = skipTypeSpec(lineStart);

		while (isPrefixToken(tokens.currLineLA(lineStart + 1)))
			lineStart++;
		if (isKindSelector(tokens.currLineLA(lineStart + 1)))
			lineStart += 2;

		return lineStart;

	}// end skipPrefix()

	private boolean isKindSelector(int token) {
		if (token == MFortranLexer.ASTERISK)
			return true;
		else
			return false;
	}

	// Test to see if a token is one of pure, elemental, or recursive.
	private boolean isPrefixToken(int token) {
		if (token == MFortranLexer.PURE || token == MFortranLexer.ELEMENTAL
				|| token == MFortranLexer.RECURSIVE)
			return true;
		else
			return false;
	}// end isIntrinsicType()

	private boolean isFuncDecl(int lineStart, int lineEnd) {

		// have to skip over any kind selector
		lineStart = skipPrefix(lineStart);

		// Here, we know the first token is one of the intrinsic types.
		// Now, look at the second token to see if it is FUNCTION.
		// If it is, AND a keyword/identifier immediately follows it,
		// then this cannot be a data decl and must be a function decl.
		if (tokens.currLineLA(lineStart + 1) == MFortranLexer.FUNCTION) {
			if (tokens.currLineLA(lineStart + 2) == MFortranLexer.IDENT
					|| (tokens.isKeyword(tokens.currLineLA(3))))
				return true;
		}

		return false;
	}// end isFuncDecl()

	// True if this is a subroutine declaration.
	private boolean isSubDecl(int lineStart, int lineEnd) {

		// Skip the prefix.
		lineStart = skipPrefix(lineStart);

		// Look at the first token to see if it is SUBROUTINE.
		// If it is, AND a keyword/identifier immediately follows it,
		// then this is a subroutine declaration.
		if (tokens.currLineLA(lineStart + 1) == MFortranLexer.SUBROUTINE) {
			if (tokens.currLineLA(lineStart + 2) == MFortranLexer.IDENT
					|| tokens.isKeyword(tokens.currLineLA(lineStart + 2))) {
				return true;
			}
		}

		return false;

	} // end isSubDecl()

	private boolean isValidDataEditDesc(String line, int lineIndex) {
		char firstChar;
		char secondChar = '\0';

		// need the first char in the string
		firstChar = Character.toLowerCase(line.charAt(lineIndex));
		if (lineIndex < line.length() - 1)
			secondChar = Character.toLowerCase(line.charAt(lineIndex + 1));

		// TODO: there should be a more efficient way to do this!!
		if (firstChar == 'i'
				|| (firstChar == 'b' && secondChar != 'n' && secondChar != 'z')
				|| firstChar == 'o' || firstChar == 'z' || firstChar == 'f'
				|| firstChar == 'g' || firstChar == 'l' || firstChar == 'a'
				|| (firstChar == 'd'
						&& ((secondChar == 't') || isDigit(secondChar)))
				|| (firstChar == 'e' && (secondChar == 'n' || secondChar == 's'
						|| isDigit(secondChar)))) {
			// IDENT represents a valid data-edit-desc
			return true;
		}

		return false;
	} // end isValidDataEditDesc()

	private int findFormatItemEnd(String line, int lineIndex) {
		char currChar;
		int lineLength;

		lineLength = line.length();
		do {
			currChar = line.charAt(lineIndex);
			lineIndex++;
		} while (lineIndex < lineLength && currChar != ',' && currChar != ')'
				&& currChar != '/' && currChar != ':');

		// we went one past the line terminator, so move back to it's location
		return lineIndex - 1;
	} // end findFormatItemEnd()

	private int matchVList(String line, int lineIndex) {
		int tmpLineIndex;
		int lineLength;

		/* Skip the 'dt'. */
		tmpLineIndex = lineIndex + 2;

		lineLength = line.length();

		/* We could have a char-literal-constant here to skip. */
		if (line.charAt(tmpLineIndex) == '\''
				|| line.charAt(tmpLineIndex) == '"') {
			tmpLineIndex++;
			while (line.charAt(tmpLineIndex) != '\''
					&& line.charAt(tmpLineIndex) != '"'
					&& tmpLineIndex < lineLength)
				tmpLineIndex++;
		}

		/* If we hit the end, there's an error in the line so just return. */
		if (tmpLineIndex == lineLength)
			return lineIndex;

		/* Move off the closing quotation. */
		if (line.charAt(tmpLineIndex) == '\''
				|| line.charAt(tmpLineIndex) == '"')
			tmpLineIndex++;

		/* Check for optional v-list and skip if present. */
		if (line.charAt(tmpLineIndex) == '(') {
			tmpLineIndex++;

			while (tmpLineIndex < lineLength
					&& Character.isDigit(line.charAt(tmpLineIndex)))
				tmpLineIndex++;

			if (tmpLineIndex == lineLength)
				/* There is an error in the line! */
				return lineIndex;

			if (line.charAt(tmpLineIndex) == ')') {
				tmpLineIndex++;
				/* We successfully matched the v-list. Return new index. */
				return tmpLineIndex;
			} else {
				System.err.println("Error: Unable to match v-list in "
						+ "data-edit-desc!");
				return lineIndex;
			}
		}

		/* No v-list. Return where we started. */
		return lineIndex;
	}// end matchVList()

	private int getDataEditDesc(String line, int lineIndex, int lineEnd) {
		// see if we have a repeat specification (DIGIT_STR)
		while (lineIndex < lineEnd && isDigit(line.charAt(lineIndex))) {
			lineIndex++;
		}

		// data-edit-desc starts with a IDENT token, representing one of:
		// I, B, O, Z, F, E, EN, ES, G, L, A, D, or DT
		if (isValidDataEditDesc(line, lineIndex) == true) {
			/* For DT, there can be an optional v-list. */
			if (Character.toLowerCase(line.charAt(lineIndex)) == 'd'
					&& Character
							.toLowerCase(line.charAt(lineIndex + 1)) == 't') {
				/* Advance past any optional v-list. */
				lineIndex = matchVList(line, lineIndex);
			}
			return findFormatItemEnd(line, lineIndex);
		}

		return -1;
	} // end getDataEditDesc()

	private boolean isDigit(char tmpChar) {
		if (tmpChar >= '0' && tmpChar <= '9')
			return true;
		else
			return false;
	}// end isDigit()

	/********
	 * OBSOLETE private boolean isLetter(char tmpChar) { tmpChar =
	 * Character.toLowerCase(tmpChar); if (tmpChar >= 'a' && tmpChar <= 'z')
	 * return true; else return false; } END OBSOLETE
	 ********/

	private boolean isValidControlEditDesc(String line, int lineIndex) {
		char firstChar;
		char secondChar = '\0';

		firstChar = Character.toLowerCase(line.charAt(lineIndex));
		if (lineIndex < line.length() - 1) {
			secondChar = Character.toLowerCase(line.charAt(lineIndex + 1));
		}

		if (firstChar == ':' || firstChar == '/' || firstChar == 'p'
				|| firstChar == 't' || firstChar == 's' || firstChar == 'b'
				|| firstChar == 'r' || firstChar == 'd' || firstChar == 'x') {
			// more checking to do on the t, s, b, r, and d cases
			if (firstChar == 's')
				/* TODO: verify the following is true for sign-edit-desc. */
				/*
				 * If the first char is an 'S', then it can be followed by
				 * another S, a P, or nothing (the single 'S' is a
				 * sign-edit-desc). However, the single 'S' must not be
				 * immediately followed by another letter or number, I think.
				 */
				if (secondChar != 's' && secondChar != 'p'
						&& Character.isLetterOrDigit(secondChar) == true)
					return false;
				else if (firstChar == 't' && (isDigit(secondChar) != true
						&& secondChar != 'l' && secondChar != 'r'))
					return false;
				else if (firstChar == 'b'
						&& (secondChar != 'n' && secondChar != 'z'))
					return false;
				else if (firstChar == 'r'
						&& (secondChar != 'u' && secondChar != 'd'
								&& secondChar != 'z' && secondChar != 'n'
								&& secondChar != 'c' && secondChar != 'p'))
					return false;
				else if (firstChar == 'd'
						&& (secondChar != 'c' && secondChar != 'p'))
					return false;

			return true;
		}

		return false;
	}// end isValidControlEditDesc()

	private int getControlEditDesc(String line, int lineIndex, int lineLength) {
		// skip the possible number before X
		while (lineIndex < lineLength && (line.charAt(lineIndex) >= '0'
				&& line.charAt(lineIndex) <= '9')) {
			lineIndex++;
		}

		if (isValidControlEditDesc(line, lineIndex) == true) {
			// include the char we're on, in case it is a '/' or ':', which can
			// be the terminating char.
			return findFormatItemEnd(line, lineIndex);
		}

		return -1;
	} // end getControlEditDesc()

	// Return the index for the end of the char string. The tokens already
	// verified that
	// the string was valid (it should have, at least..), but we need the end so
	// we don't
	// consider anything within the string as a terminator.
	private int getCharString(String line, int lineIndex, char quoteChar) {
		char nextChar;
		// we know the first character matches the quoteChar, so look at
		// what the next char is
		lineIndex++;
		nextChar = line.charAt(lineIndex);

		// need to consider: 1. empty string; 2. escaped quotes, ie, """" is
		// equivalent to '"'

		// look for empty string
		if (nextChar == quoteChar && line.charAt(lineIndex + 1) != quoteChar) {
			// this is an empty string
			return lineIndex + 1;
		}

		do {
			if (nextChar == quoteChar) {
				// look for two consequtive quote chars
				if (line.charAt(lineIndex + 1) == quoteChar) {
					lineIndex += 1; // skip the consequtive quotes
				}
			}
			nextChar = line.charAt(++lineIndex);
		} while (nextChar != quoteChar);

		// OBSOLETE: replaced by fix to bug 3304566 19.5.2011
		// if ((nextChar == '\'' || nextChar == '"') && nextChar == quoteChar)
		// return getCharString(line, lineIndex, nextChar);
		//
		// do {
		// lineIndex++;
		// nextChar = line.charAt(lineIndex);
		// } while(nextChar != '\'' && nextChar != '"');

		return lineIndex;
	} // end getCharString()

	private int getCharStringEditDesc(String line, int lineIndex,
			int lineLength) {
		char quoteChar;
		int startIndex = lineIndex;

		// see if we have a repeat specification (DIGIT_STR)
		while (lineIndex < lineLength && isDigit(line.charAt(lineIndex))) {
			lineIndex++;
		}

		quoteChar = Character.toLowerCase(line.charAt(lineIndex));

		if (quoteChar == 'h') {
			// We have an H char-string-edit-desc, which is valid F90 but
			// deleted from F03.
			if (startIndex != lineIndex) {
				// there has to be a number before the H so we know how many
				// chars to read (skip).
				return lineIndex + 1 + (Integer
						.parseInt(line.substring(startIndex, lineIndex)));
			}
		}

		if (quoteChar != '\'' && quoteChar != '"') {
			return -1;
		}

		// find the end of the char string. the tokens already verified that
		// the string was valid (it should have, at least..), but we need the
		// end so we don't consider anything w/in the string as a terminator
		lineIndex = getCharString(line, lineIndex, quoteChar);

		return findFormatItemEnd(line, lineIndex + 1);
	}// end getCharStringEditDesc()

	/**
	*
	*/
	private int parseFormatString(String line, int lineIndex, int lineNum,
			int charPos) {
		int lineLength;
		int descIndex = 0;
		boolean foundClosingParen = false;

		lineLength = line.length();

		// stop before processing the closing RPAREN
		while (lineIndex < (lineLength - 1) && foundClosingParen == false) {
			descIndex = getCharStringEditDesc(line, lineIndex, lineLength);
			if (descIndex == -1) {
				descIndex = getDataEditDesc(line, lineIndex, lineLength);
				if (descIndex == -1) {
					descIndex = getControlEditDesc(line, lineIndex, lineLength);
					if (descIndex != -1) {
						// found a control-edit-desc
						// Don't create a token if we just have a / of : because
						// it
						// is handled below since it is also a terminating char
						// (no comma needed).
						if ((descIndex - lineIndex) > 0
								|| (line.charAt(descIndex) != '/'
										&& line.charAt(descIndex) != ':')) {
							// Don't understand how you can have a zero length
							// token so
							// print out a warning in case it occurs to see what
							// the problem
							// could be.
							if (descIndex == lineIndex) {
								System.out.println(
										"FortranLexicalPrepass::parseFormatString: WARNING creating 0 length token");
							}
							tokens.addToken(tokens.createToken(
									MFortranLexer.M_CTRL_EDIT_DESC,
									line.substring(lineIndex, descIndex),
									lineNum, charPos, inclusion));
							charPos += line.substring(lineIndex, descIndex)
									.length();
						}
					}
				} else {
					// found a data-edit-desc
					tokens.addToken(
							tokens.createToken(MFortranLexer.M_DATA_EDIT_DESC,
									line.substring(lineIndex, descIndex),
									lineNum, charPos, inclusion));
					charPos += line.substring(lineIndex, descIndex).length();
				}
			} else {
				// found a char-string-edit-desc
				tokens.addToken(
						tokens.createToken(MFortranLexer.M_CSTR_EDIT_DESC,
								line.substring(lineIndex, descIndex), lineNum,
								charPos, inclusion));
				charPos += line.substring(lineIndex, descIndex).length();
			}

			// need to see if we found a descriptor, or if we didn't, if we are
			// not on a LPAREN then we should be looking at a ',' or some other
			// terminating character. this can happen in a case where the format
			// string has something of the form: (i12, /, 'hello')
			// because the '/' is a control edit desc that terminates itself,
			// so we'd next look at the ','.
			if (descIndex != -1 || (descIndex == -1
					&& isDigit(line.charAt(lineIndex)) == false
					&& line.charAt(lineIndex) != '(')) {
				String termString = null;

				// if we started our search on a terminating character the
				// descIndex won't have been set, so set it here.
				if (descIndex == -1) {
					descIndex = lineIndex;
				}

				// add a token for the terminating character
				if (line.charAt(descIndex) == ',') {
					termString = new String(",");
					tokens.addToken(tokens.createToken(MFortranLexer.COMMA, ",",
							lineNum, charPos, inclusion));
				} else if (line.charAt(descIndex) == ')') {
					tokens.addToken(tokens.createToken(MFortranLexer.RPAREN,
							")", lineNum, charPos, inclusion));
				} else {
					if (line.charAt(descIndex) == ':') {
						termString = new String(":");
					} else if (line.charAt(descIndex) == '/') {
						termString = new String("/");
					} else {
						// we have no terminator (this is allowed, apparently).
						termString = null;
					}

					if (termString != null) {
						// we could be using a / or : as a terminator, and they
						// are
						// valid control-edit-descriptors themselves.
						tokens.addToken(tokens.createToken(
								MFortranLexer.M_CTRL_EDIT_DESC, termString,
								lineNum, charPos, inclusion));
					}
				}

				// we're on the terminating char so bump past it
				lineIndex = descIndex + 1;
			} else {
				int startIndex = lineIndex;
				// we may have a nested format stmt
				// skip over the optional DIGIT_STR, but if there, add a
				// token for it.
				while (lineIndex < lineLength
						&& isDigit(line.charAt(lineIndex))) {
					lineIndex++;
					charPos++;
				}

				if (startIndex != lineIndex) {
					// We have a DIGIT_STR in front of the nested format
					// stmt.
					// Put a token for it in the stream.
					tokens.addToken(tokens.createToken(MFortranLexer.DIGIT_STR,
							line.substring(startIndex, lineIndex), lineNum,
							charPos - (lineIndex - startIndex), inclusion));
				}

				// make sure we're on a left paren
				if (line.charAt(lineIndex) == '(') {
					tokens.addToken(tokens.createToken(MFortranLexer.LPAREN,
							"(", lineNum, charPos, inclusion));
					charPos++;
					// move past the left paren
					lineIndex++;
					descIndex = parseFormatString(line, lineIndex, lineNum,
							charPos);
					if (descIndex == -1) {
						System.err.println(
								"Could not parse the format string: " + line);
						return -1;
					} else {
						lineIndex = descIndex + 1;
					} // end else()
				} else {
					// couldn't match anything!
					return -1;
				}
			}

			charPos++;
		}

		/*
		 * this can happen in cases where a format item is terminated with a /
		 * or :, because these are also valid control-edit-descriptors. for
		 * example: 004 format(//) would create a M_CTRL_EDIT_DESCRIPTOR for the
		 * last /, and then advance the index to the ')'. however, the rparen is
		 * not a format item, and so is not considered in the above while loop.
		 * that is why a RPAREN is added here if necessary.
		 */
		if (lineIndex < lineLength && line.charAt(lineIndex) == ')') {
			tokens.addToken(tokens.createToken(MFortranLexer.RPAREN, ")",
					lineNum, charPos, inclusion));
			lineIndex++;
		}

		// return either the index of where we stopped parsing the format
		// sting, or a -1 if nothing was matched. the -1 case shouldn't reach
		// here because it should get handled above when looking for a nested
		// format stmt.
		return lineIndex;
	} // end parseFormatString()

	/**
	 * Convert items in a list of expressions to identifiers. The major
	 * difficulty is an array constructor because it can contain a type
	 * specifier with an intrisic type keyword that can't be converted to an
	 * identifier.
	 *
	 * Return the index following the expression list
	 */
	private int fixupExprList(int start, int end) {
		// Pulling apart expressions is difficult, for now just convert
		// everything
		// to identifiers except when encountering an array constructor which
		// has
		// the form [ type_stuff :: ... ]. So make convertToIdents look for
		// array constructors.

		convertToIdents(start, end);

		return end;

	} // end fixupExprList()

	/**
	 * Convert format-items in a format to identifiers.
	 *
	 * format is format-specification | label | '*'
	 *
	 * Return the index after the format, often ',' for an
	 * input/output-item-list.
	 *
	 * The reason this method is needed is because most the items in a
	 * format-specification are just converted by the tokens to identifiers so
	 * now the edit descriptors need to be parsed, primarily by going back to
	 * the original character representation of the input stream.
	 */
	private int fixupFormat(int start, int lineEnd) {
		// check for easy ones first
		int tk = tokens.getToken(start).getType();
		if (tk == MFortranLexer.ASTERISK || tk == MFortranLexer.DIGIT_STR) {
			return start + 1;
		}

		// TODO - Convert the format-specification? This is actually a string so
		// should ROSE pull it apart? It seems like OFP should really treat the
		// string
		// like a format-specification and pull out the edit descriptors.

		return start;

	} // end fixupFormat()

	/**
	 * Parse the format-specification in a format for edit descriptors. Although
	 * a Hollerith edit descriptor has been deprecated, deal with it anyway. All
	 * keywork tokens to the right of the paren have been converted to
	 * identifiers.
	 *
	 * The reason this method is needed is because most the items in a
	 * format-specification are just converted by the tokens to identifiers so
	 * now the edit descriptors need to be parsed, primarily by going back to
	 * the original character representation of the input stream.
	 */
	private int fixupFormatStmt(int start, int lineEnd) {
		String line;
		int lineIndex = 0;
		int i = 0;
		int lineNum = 0;
		int charPos = 0;
		ArrayList<Token> origLine = new ArrayList<Token>();

		/*
		 * NOTE: the COMMA to separate items in a format_item_list is not always
		 * required! See J3/04-007, pg. 221, lines 17-22
		 */
		// get the lineNum that the format stmt occurs on
		lineNum = tokens.getToken(start).getLine();
		start++; // move past the FORMAT
		charPos = tokens.getToken(start).getCharPositionInLine();

		if (tokens.currLineLA(start + 1) != MFortranLexer.LPAREN) {
			// error in the format stmt; missing paren
			return -1;
		}

		// get all the text left in the line as one String
		line = tokens.lineToString(start, lineEnd);

		// make a copy of the original packed line
		origLine.addAll(tokens.getTokensInCurLine());

		// now, delete the tokens in the curr line so we can rewrite them
		tokens.clearTokensList();
		// first, copy the starting tokens to the new line (label FORMAT,
		// etc.)
		for (i = 0; i < start; i++) {
			// adds to the end
			tokens.addToken(origLine.get(i));
		}

		lineIndex = 0;
		lineIndex = parseFormatString(line, lineIndex, lineNum, charPos);

		// terminate the newLine with a EOS
		tokens.addToken(tokens.createToken(MFortranLexer.EOS, "\n", lineNum,
				charPos + lineIndex, inclusion));

		// if there was an error, put the original line back
		if (lineIndex == -1) {
			System.err.println("Error in format statement " + line + " at line "
					+ lineNum);
			tokens.clearTokensList();
			for (i = 0; i < lineEnd; i++) {
				tokens.addToken(origLine.get(i));
			}
		}

		return lineIndex;
	} // end fixupFormatStmt()

	/**
	 * Match one of the following tokens for an IO statement: CLOSE, OPEN, READ,
	 * FLUSH, REWIND, WRITE, INQUIRE, FORMAT, PRINT
	 */
	private boolean matchIOStmt(int lineStart, int lineEnd) {
		int tokenType;
		int identOffset = -1;

		tokenType = tokens.currLineLA(lineStart + 1);

		if (tokenType == MFortranLexer.PRINT) {
			// Does this check for an assignment statement? What is it for?
			if (tokens.currLineLA(lineStart + 2) == MFortranLexer.EQUALS) {
				return false;
			} else {
				// fixup format and then the output-item-list
				identOffset = fixupFormat(lineStart + 1, lineEnd);
				if (tokens.getToken(identOffset)
						.getType() == MFortranLexer.COMMA) {
					identOffset = fixupExprList(identOffset + 1, lineEnd);
				}
			}
		} else {
			// TODO - READ/WRITE handled here? What about format and item-list?
			if (tokens.currLineLA(lineStart + 2) == MFortranLexer.LPAREN) {
				identOffset = lineStart + 2;

				// fixup the inquire statement to try and help the parser to not
				// have to backtrack. For an inquire_stmt, if something other
				// than EOS follows the closing RPAREN, it must try and match
				// alt2.
				if (tokenType == MFortranLexer.INQUIRE) {
					int rparenOffset = -1;
					rparenOffset = matchClosingParen(lineStart + 2);
					// should not be possible for it to be -1..
					if (rparenOffset != -1 && (rparenOffset < (lineEnd - 1))) {
						if (tokens.currLineLA(
								rparenOffset + 1) != MFortranLexer.EOS) {
							// add a token saying it must be alt2
							tokens.addToken(lineStart,
									MFortranLexer.M_INQUIRE_STMT_2,
									"__INQUIRE_STMT_2__", inclusion);
							// increment the identOffset because added token
							// before it
							identOffset++;
						}
					}
				} // end if(was INQUIRE)
			} else if ((tokenType == MFortranLexer.FLUSH
					|| tokenType == MFortranLexer.REWIND)
					&& tokens.currLineLA(
							lineStart + 2) != MFortranLexer.EQUALS) {
				// this is the case if you have a FLUSH/REWIND stmt w/ no parens
				identOffset = lineStart + 1;
			}
		}

		if (identOffset != -1) {
			convertToIdents(identOffset, lineEnd);

			// do the fixup after we've converted to identifiers because the
			// identOffset and lineEnd are based on the original line!
			if (tokenType == MFortranLexer.FORMAT) {
				fixupFormatStmt(lineStart, lineEnd);
			}

			// need to see if this has a label, and if so, see if it's needed
			// to terminate a do loop.
			if (lineStart > 0 && tokens
					.currLineLA(lineStart) == MFortranLexer.DIGIT_STR) {
				fixupLabeledEndDo(lineStart, lineEnd);
			}

			return true;
		} else {
			return false;
		}
	} // end matchIOStmt()

	private boolean matchProgramStmt(int lineStart, int lineEnd) {
		Token t_ident = tokens.getToken(lineStart + 1);

		// try to match PROGRAM IDENT EOS
		if (tokens.isKeyword(tokens.currLineLA(lineStart + 2))) {
			// getToken is 0 based indexing; currLineLA is 1 based
			t_ident.setType(MFortranLexer.IDENT);
		}
		return true;
	}// end matchProgramStmt()

	private boolean labelsMatch(String label1, String label2) {
		if (Integer.parseInt(label1) == Integer.parseInt(label2)) {
			return true;
		}
		return false;
	}// end labelsMatch()

	/**
	 * Fix up a DO loop that is terminated by an action statement. TODO:: There
	 * are a number of contraints on what action statements can be used to do
	 * this, but the parser will have to check them.
	 */
	private void fixupLabeledEndDo(int lineStart, int lineEnd) {
		// if we don't have a label, return
		if (tokens.currLineLA(1) != MFortranLexer.DIGIT_STR) {
			return;
		}

		if (doLabels.empty() == false) {
			String doLabelString = doLabels.peek().getText();
			Token firstToken = tokens.getToken(0);
			// the lineStart was advanced past the label, so the CONTINUE or
			// END is the first token in look ahead (lineStart+1)
			String labeledDoText = new String("LABELED_DO_TERM");

			if (labelsMatch(doLabelString, firstToken.getText()) == true) {
				// labels match up
				// try inserting a new token after the label. this will help
				// the parser recognize a do loop being terminated
				tokens.addToken(1, MFortranLexer.M_LBL_DO_TERMINAL,
						labeledDoText, inclusion);

				// need to pop off all occurrences of this label that
				// were pushed. this can happen if one labeled action stmt
				// terminates nested do stmts. start by popping the first one,
				// then checking if there are any more.
				doLabels.pop();
				while (doLabels.empty() == false
						&& (labelsMatch(doLabels.peek().getText(),
								firstToken.getText()) == true)) {
					// for each extra matching labeled do with this labeled end
					// do,
					// we need to add a M_LBL_DO_TERMINAL to the token stream.
					// also, append a new statement for each do loop we need to
					// terminate. the added stmt is:
					// label M_LBL_DO_TERMINAL CONTINUE EOS
					if (tokens.appendToken(MFortranLexer.DIGIT_STR,
							new String(firstToken.getText()),
							inclusion) == false
							|| tokens.appendToken(
									MFortranLexer.M_LBL_DO_TERMINAL,
									labeledDoText, inclusion) == false
							|| tokens.appendToken(MFortranLexer.CONTINUE,
									new String("CONTINUE"), inclusion) == false
							|| tokens.appendToken(MFortranLexer.EOS, null,
									inclusion) == false) {
						// should we exit here??
						System.err.println("Couldn't add tokens!");
						System.exit(1);
					}
					doLabels.pop();
				}
			}
		}
		return;
	} // end fixupLabeledEndDo()

	private boolean matchActionStmt(int lineStart, int lineEnd) {
		int tokenType;
		int identOffset = -1;

		tokenType = tokens.currLineLA(lineStart + 1);
		// these all start with a keyword, but after that, rest must
		// be idents, if applicable. this does not care about parens, if
		// the rule calls for them. they will be skipped, so can start
		// conversion on their location. this simplifies the logic.
		if (tokenType == MFortranLexer.GO) {
			if (tokens.currLineLA(lineStart + 2) != MFortranLexer.TO)
				return false;

			// there is a space between GO and TO. skip over the TO.
			identOffset = lineStart + 2;
		} else if (tokenType == MFortranLexer.ALLOCATE) {
			int colonOffset = -1;
			// allocate_stmt can have a type_spec if there is a double colon
			// search for the double colon, and if given, idents follow it.
			colonOffset = tokens.findToken(lineStart + 1,
					MFortranLexer.COLON_COLON);
			if (colonOffset != -1) {
				// insert a token for the parser to know whether this is alt 1
				// or alt 2 in allocate_stmt (depends on the ::)
				tokens.addToken(lineStart, MFortranLexer.M_ALLOCATE_STMT_1,
						"__ALLOCATE_STMT_1__", inclusion);
				lineStart++;
				// identifiers follow the ::
				// it's +2 instead of +1 because we just inserted a new token
				identOffset = colonOffset + 2;
			} else {
				identOffset = lineStart + 1;
			}
		} else {
			identOffset = lineStart + 1;
		}

		if (identOffset != -1) {
			convertToIdents(identOffset, lineEnd);

			// a labeled action stmt can terminate a do loop. see if we
			// have to fix it up (possibly insert extra tokens).
			// a number of things can't terminate a non-block DO, including
			// a goto.
			if ((lineStart > 0
					&& tokens.currLineLA(lineStart) == MFortranLexer.DIGIT_STR)
					&& tokenType != MFortranLexer.GOTO) {
				fixupLabeledEndDo(lineStart, lineEnd);
			}

			return true;
		} else {
			return false;
		}
	} // end matchActionStmt()

	private boolean matchSingleTokenStmt(int lineStart, int lineEnd) {
		int firstToken;

		firstToken = tokens.currLineLA(lineStart + 1);

		// if any of these tokens starts a line, any keywords that follow
		// must be idents.
		// ones i'm unsure about:
		// WHERE (assuming where_stmt is handled before this is called)
		if (firstToken == MFortranLexer.COMMON
				|| firstToken == MFortranLexer.EQUIVALENCE
				|| firstToken == MFortranLexer.NAMELIST
				|| firstToken == MFortranLexer.WHERE
				|| firstToken == MFortranLexer.ELSEWHERE
				|| firstToken == MFortranLexer.FORALL
				|| firstToken == MFortranLexer.SELECT
				|| firstToken == MFortranLexer.SELECTCASE
				|| firstToken == MFortranLexer.SELECTTYPE
				|| firstToken == MFortranLexer.CASE
				|| (firstToken == MFortranLexer.CLASS && tokens
						.currLineLA(lineStart + 2) != MFortranLexer.DEFAULT)
				|| firstToken == MFortranLexer.INTERFACE
				|| firstToken == MFortranLexer.ENTRY
				|| firstToken == MFortranLexer.IMPORT
				|| firstToken == MFortranLexer.DATA) {
			// if we have a CLASS, it must be used in a select-type because
			// we should have already tried to match the CLASS used in a
			// data declaration. there appears to be no overlap between a
			// data decl with CLASS and it's use here, unlike derived types..

			// if we have a SELECT, a CASE or TYPE must follow,
			// then ident(s). also, if have CASE DEFAULT, idents follow it
			if (firstToken == MFortranLexer.SELECT
					|| (firstToken == MFortranLexer.CASE && tokens.currLineLA(
							lineStart + 2) == MFortranLexer.DEFAULT)) {
				convertToIdents(lineStart + 2, lineEnd);
			} else if (firstToken == MFortranLexer.INTERFACE) {
				int identOffset;
				// need to match the generic spec and then convert to idents.
				identOffset = matchGenericSpec(lineStart + 1, lineEnd);

				// if matchGenericSpec fails, we won't convert anything because
				// there is an error on the line and we'll let the parser deal
				// with it.
				if (identOffset != -1) {
					convertToIdents(identOffset, lineEnd);
				}
			} else if (firstToken == MFortranLexer.ENTRY) {
				// an ENTRY stmt can have a result clause, so we need to
				// look for one. if it does, it must have the parens after the
				// entry-name, so look for them.
				// lineStart+3 is the first token after the required entry-name.
				if (lineStart + 3 < lineEnd) {
					if (tokens.currLineLA(
							lineStart + 3) == MFortranLexer.LPAREN) {
						int resultLA;
						resultLA = matchClosingParen(lineStart + 3);

						convertToIdents(lineStart + 1, resultLA - 1);

						// The resultLA is either the LA for RESULT or EOS.
						// If
						// it is a RESULT, we need to convert what follows it.
						if (tokens.currLineLA(resultLA) == MFortranLexer.RESULT)
							convertToIdents(resultLA, lineEnd);
					}
				} else {
					// No dummy-arg-list given.
					convertToIdents(lineStart + 1, lineEnd);
				}
			} else {
				// all other cases
				convertToIdents(lineStart + 1, lineEnd);
			}

			// insert token(s) to help disambiguate the grammar for the parser
			if (firstToken == MFortranLexer.WHERE)
				tokens.addToken(lineStart, MFortranLexer.M_WHERE_CONSTRUCT_STMT,
						"__WHERE_CONSTRUCT_STMT__", inclusion);
			else if (firstToken == MFortranLexer.FORALL)
				tokens.addToken(lineStart,
						MFortranLexer.M_FORALL_CONSTRUCT_STMT,
						"__FORALL_CONSTRUCT_STMT__", inclusion);

			// we matched the stmt successfully
			return true;
		}

		return false;
	}// end matchSingleTokenStmt()

	// ///////
	// Attempt to match a DO statement. The lineStart argument has already
	// advance
	// beyond the statement label if it exists.
	//
	private boolean matchDoStmt(int lineStart, int lineEnd) {
		// Default is to convert everything except DO to identifiers.
		//
		int identOffset = lineStart + 1;

		// The keyword may immediately follow the DO token
		//
		int keywordOffset = lineStart + 2;

		// If there is no loop-control then return immediately
		//
		if (tokens.currLineLA(lineStart + 2) == MFortranLexer.EOS
				|| tokens.currLineLA(lineStart + 3) == MFortranLexer.EOS) {
			return true;
		}

		// See if the next token is a label. If so, save it so we
		// can change the token type for the labeled continue.
		//
		if (tokens.currLineLA(lineStart + 2) == MFortranLexer.DIGIT_STR) {
			// Wwh
			Token t = tokens.getToken(lineStart + 1);

			doLabels.push(tokens.createToken(t.getType(), t.getText(),
					t.getLine(), t.getCharPositionInLine(), inclusion));
			//
			// doLabels.push(new FortranToken(tokens.getToken(lineStart+1)));
			identOffset += 1;
			keywordOffset += 1;
		}

		// At this point we know we have a loop-control.
		//

		// The previous (pre F2008) way of matching do statements no
		// longer works as a DO CONCURRENT construct has a forall-header
		// with EQUALS and COMMA. So we have to be more selective.

		// Skip over the comma preceeding the keyword if it exists
		//
		if (tokens.currLineLA(keywordOffset) == MFortranLexer.COMMA) {
			identOffset += 1;
			keywordOffset += 1;
		}

		// The next token must be a variable, WHILE, or CONCURRENT
		//
		if (tokens.currLineLA(keywordOffset) == MFortranLexer.WHILE || tokens
				.currLineLA(keywordOffset) == MFortranLexer.CONCURRENT) {
			// check for variable EQUALS ...
			if (tokens.currLineLA(keywordOffset + 1) != MFortranLexer.EQUALS) {
				identOffset += 1;
			}
		}

		// Convert keywords on the line to idents, starting at the identOffset.
		//
		convertToIdents(identOffset, lineEnd);

		return true;
	} // end matchDoStmt()

	/**
	 * A few stmts can be one liners (where-stmt, if-stmt, forall). These will
	 * fail Sale's because they will have an equal sign and no comma. Sale's
	 * says that it must not start w/ a keyword then, but these are exceptions.
	 * They could also have no equal and no comma, such as: if (result < 0.)
	 * cycle
	 */
	private boolean matchOneLineStmt(int lineStart, int lineEnd) {
		int tokenType;
		int identOffset = -1;
		int rparenOffset = -1;

		// get the token type and determine if we have an applicable stmt
		tokenType = tokens.currLineLA(lineStart + 1);
		if (tokenType == MFortranLexer.WHERE || tokenType == MFortranLexer.IF
				|| tokenType == MFortranLexer.FORALL) {
			// next token must be the required left paren!
			if (tokens.currLineLA(lineStart + 2) == MFortranLexer.LPAREN) {
				identOffset = lineStart + 2;
				// find the right paren (end of the expression)
				rparenOffset = matchClosingParen(lineStart + 2);
				// convert anything between the parens to idents
				convertToIdents(identOffset, rparenOffset);

				// match the rest of the line (action statements), the
				// matchLine() allows for
				// more than we should, but the parser should catch those
				// errors.
				// assignment-stmt should be checked first as variable may be a
				// keyword (CER 08.24.10)
				if (matchAssignStmt(rparenOffset, lineEnd) == false) {
					matchLine(rparenOffset, lineEnd);
				}

				// insert a token to signal that this a one-liner statement,
				// either a where_stmt, if_stmt, or forall_stmt. hopefully this
				// will allow the parser to do less backtracking.
				if (tokenType == MFortranLexer.WHERE) {
					tokens.addToken(lineStart, MFortranLexer.M_WHERE_STMT,
							"__WHERE_STMT__", inclusion);
				} else if (tokenType == MFortranLexer.IF) {
					tokens.addToken(lineStart, MFortranLexer.M_IF_STMT,
							"__IF_STMT__", inclusion);
				} else {
					tokens.addToken(lineStart, MFortranLexer.M_FORALL_STMT,
							"__FORALL_STMT__", inclusion);
				}

				// a labeled action stmt can terminate a do loop. see if we
				// have to fix it up (possibly insert extra tokens).
				if (lineStart > 0 && tokens
						.currLineLA(lineStart) == MFortranLexer.DIGIT_STR) {
					fixupLabeledEndDo(lineStart, lineEnd);
				}
				return true;
			} else {
				// didn't match the required left paren after the token
				return false;
			}
		}

		return false;
	} // end matchOneLineStmt()

	/**
	 * This matches an entire data_ref and returns the offset of the next token
	 * following it.
	 */
	private int matchDataRef(int lineStart, int lineEnd) {
		// SKW 2011-4-26: modified to accept CAF2 co-dereference operator ('[
		// ]') if present

		// skip any number of consecutive part refs separated by '%'
		// each begins with an identifier (or keyword)
		int nextOffset = lineStart + 1;
		int tk = tokens.currLineLA(nextOffset);
		while (tk == MFortranLexer.IDENT || tokens.isKeyword(tk)) {
			// skip the identifier if any
			nextOffset += 1;

			// skip up to three balanced '( )' or '[ ]' pairs (co-dereference
			// op, section subscript list, image selector)
			int numToSkip = 3;
			while (numToSkip > 0) {
				if (isOpenParen(tokens.currLineLA(nextOffset))) {
					nextOffset = 1 + matchClosingParen(nextOffset);
					numToSkip -= 1;
				} else {
					numToSkip = 0; // no more pairs available to skip
				}
			}

			// skip the separating '%' if any
			if (tokens.currLineLA(nextOffset) == MFortranLexer.PERCENT) {
				nextOffset += 1;
				tk = tokens.currLineLA(nextOffset);
			} else {
				break; // no '%' so no new part-ref to skip
			}
		}

		return nextOffset;
	} // end matchDataRef()

	private boolean matchAssignStmt(int lineStart, int lineEnd) {
		int identOffset, assignType;
		int nextOffset = lineStart;

		// can't be an assignment statement if not enough tokens
		if (lineEnd - lineStart < 3) {
			return false;
		}

		// advance past an initial data_ref
		nextOffset = matchDataRef(nextOffset, lineEnd);

		// look for an assignment token (including ptr assignment)
		if (tokens.currLineLA(nextOffset) == MFortranLexer.EQUALS
				|| tokens.currLineLA(nextOffset) == MFortranLexer.EQ_GT) {
			// will convert everything on line to identifier
			identOffset = lineStart;
			assignType = tokens.currLineLA(nextOffset);
		} else {
			identOffset = -1;
			assignType = 0; // javac complains without this
		}

		// if we found a valid ptr assignment fix up the line and return true
		// otherwise, change nothing and return false
		if (identOffset != -1) {
			convertToIdents(identOffset, lineEnd);

			// insert a special token to signify an assignment statement of
			// given kind
			if (assignType == MFortranLexer.EQUALS) {
				tokens.addToken(lineStart, MFortranLexer.M_ASSIGNMENT_STMT,
						"__ASSIGNMENT_STMT__", inclusion);
			} else if (assignType == MFortranLexer.EQ_GT) {
				tokens.addToken(lineStart, MFortranLexer.M_PTR_ASSIGNMENT_STMT,
						"__PTR_ASSIGNMENSTMT__", inclusion);
			}

			// see if we have to fix up a labeled action stmt terminating a do
			// loop
			if (lineStart > 0 && tokens
					.currLineLA(lineStart) == MFortranLexer.DIGIT_STR) {
				fixupLabeledEndDo(lineStart, lineEnd);
			}

			return true;
		} else {
			return false;
		}
	} // end matchAssignStmt()

	/**
	 * Try to match a generic-spec.
	 * 
	 * Returns the index of the id (if an identifier) or the index after the
	 * terminating RPAREN if keyword type of generic-spec. If a generic-spec is
	 * not found, -1 is returned.
	 */
	private int matchGenericSpec(int lineStart, int lineEnd) {
		int tk1 = tokens.currLineLA(lineStart + 1);
		int tk2 = tokens.currLineLA(lineStart + 2);

		if (tk1 == MFortranLexer.OPERATOR || tk1 == MFortranLexer.ASSIGNMENT
				|| tk1 == MFortranLexer.READ || tk1 == MFortranLexer.WRITE) {
			if (tk2 != MFortranLexer.LPAREN) {
				// not a generic-spec
				return -1;
			}
			// find end of parentheses
			int rparenOffset = matchClosingParen(lineStart + 2);
			// matchClosingParen is one based indexing so next token is at
			// rparenOffset
			return rparenOffset;
		} else {
			// generic spec is simply an identifier
			return lineStart;
		}
	}

	private boolean matchGenericBinding(int lineStart, int lineEnd) {
		if (tokens.currLineLA(lineStart + 1) == MFortranLexer.GENERIC) {
			int colonOffset;
			int nextToken;
			// search for the required ::
			colonOffset = salesScanForToken(lineStart + 1,
					MFortranLexer.COLON_COLON);
			if (colonOffset == -1) {
				return false;
			}

			// see what we may need to convert
			// if the next token is a OPERATOR, ASSIGNMENT, READ, or
			// WRITE
			// if so, then we have a dtio_generic_spec.
			// colonOffset is physical offset (0 based) of ::. add one to get
			// physical offset of next token, and 1 more for LA (1 based).
			nextToken = tokens.currLineLA(colonOffset + 2);
			if (nextToken == MFortranLexer.OPERATOR
					|| nextToken == MFortranLexer.ASSIGNMENT) {
				convertToIdents(colonOffset + 2, lineEnd);
			} else if (nextToken == MFortranLexer.READ
					|| nextToken == MFortranLexer.WRITE) {
				// find end of parentheses
				int nextTokenLA = colonOffset + 2;
				int rparenOffset;
				if (tokens.currLineLA(nextTokenLA + 1) != MFortranLexer.LPAREN)
					// syntax error in the spec. parser will report
					return false;

				// find the rparen
				rparenOffset = matchClosingParen(nextTokenLA + 1);
				convertToIdents(rparenOffset + 1, lineEnd);
			}
			return true;
		} else {
			return false;
		}
	} // end matchGenericBinding()

	/**
	 * Determine what this line should be, knowing that it MUST start with a
	 * keyword!
	 */
	private boolean matchLine(int lineStart, int lineEnd) {
		if (matchDataDecl(lineStart, lineEnd) == true) {
			return true;
		} else if (matchDerivedTypeStmt(lineStart, lineEnd) == true) {
			return true;
		}

		switch (tokens.currLineLA(lineStart + 1)) {

			// If there is a function, not a subroutine, then it should have
			// been
			// caught by matchDataDecl.
			case MFortranLexer.PURE :
			case MFortranLexer.RECURSIVE :
			case MFortranLexer.ELEMENTAL :
			case MFortranLexer.SUBROUTINE :
				return matchSub(lineStart, lineEnd);

			// End stuff.
			case MFortranLexer.END :
				// case MFortranLexer.ENDASSOCIATE :
				// case MFortranLexer.ENDBLOCK :
				// case MFortranLexer.ENDBLOCKDATA :
				// case MFortranLexer.ENDCRITICAL :
				// case MFortranLexer.ENDDO :
				// case MFortranLexer.ENDENUM :
				// case MFortranLexer.ENDFILE :
				// case MFortranLexer.ENDFORALL :
				// case MFortranLexer.ENDFUNCTION :
				// case MFortranLexer.ENDIF :
				// case MFortranLexer.ENDINTERFACE :
				// case MFortranLexer.ENDMODULE :
				// case MFortranLexer.ENDPROCEDURE :
				// case MFortranLexer.ENDPROGRAM :
				// case MFortranLexer.ENDSELECT :
				// case MFortranLexer.ENDSUBMODULE :
				// case MFortranLexer.ENDSUBROUTINE :
				// case MFortranLexer.ENDTYPE :
				// case MFortranLexer.ENDWHERE :
				return matchEnd(lineStart, lineEnd);

			case MFortranLexer.PROCEDURE :
				if (matchProcStmt(lineStart, lineEnd) == true) {
					return true;
				} else {
					return matchProcDeclStmt(lineStart, lineEnd);
				}

			case MFortranLexer.MODULE :
				// module procedure stmt.
				if (matchProcStmt(lineStart, lineEnd) == true) {
					return true;
				} else {
					return matchModule(lineStart, lineEnd);
				}

			case MFortranLexer.SUBMODULE :
				return matchSubmodule(lineStart, lineEnd);

			case MFortranLexer.BLOCK :
			case MFortranLexer.BLOCKDATA :
				return matchBlockOrBlockData(lineStart, lineEnd);

			case MFortranLexer.USE :
				return matchUseStmt(lineStart, lineEnd);

			case MFortranLexer.PROGRAM :
				return matchProgramStmt(lineStart, lineEnd);

			case MFortranLexer.STOP :
			case MFortranLexer.NULLIFY :
			case MFortranLexer.RETURN :
			case MFortranLexer.EXIT :
			case MFortranLexer.WAIT :
			case MFortranLexer.ALLOCATE :
			case MFortranLexer.DEALLOCATE :
			case MFortranLexer.CALL :
			case MFortranLexer.ASSOCIATE :
			case MFortranLexer.CYCLE :
			case MFortranLexer.CONTINUE :
			case MFortranLexer.GOTO :
			case MFortranLexer.GO : // Is this correct? second token must be
									// TO
				/*
				 * If this fails because we had a GO the was NOT followed by a
				 * TO, then there isn't anything else in this method that we
				 * could match, so we simply need to return the failure. The
				 * caller must handle this.
				 */
				return matchActionStmt(lineStart, lineEnd);

			case MFortranLexer.IF :
			case MFortranLexer.ELSEIF :
				if (matchIfConstStmt(lineStart, lineEnd) == true) {
					return true;
				} else {
					return matchOneLineStmt(lineStart, lineEnd);
				}

			case MFortranLexer.ELSE :
				// check to see if this is an elseif
				if (tokens.currLineLA(lineStart + 2) == MFortranLexer.IF) {
					if (matchIfConstStmt(lineStart + 1, lineEnd) == true) {
						return true;
					} else {
						return matchOneLineStmt(lineStart, lineEnd);
					}
				} else if (matchElseStmt(lineStart, lineEnd) == true) {
					return true;
				} else {
					return matchSingleTokenStmt(lineStart, lineEnd);
				}

			case MFortranLexer.DO :
				return matchDoStmt(lineStart, lineEnd);

			case MFortranLexer.CLOSE :
			case MFortranLexer.OPEN :
			case MFortranLexer.READ :
			case MFortranLexer.FLUSH :
			case MFortranLexer.REWIND :
			case MFortranLexer.WRITE :
			case MFortranLexer.INQUIRE :
			case MFortranLexer.FORMAT :
			case MFortranLexer.PRINT :
				return matchIOStmt(lineStart, lineEnd);

			case MFortranLexer.INTENT :
			case MFortranLexer.DIMENSION :
			case MFortranLexer.ASYNCHRONOUS :
			case MFortranLexer.ALLOCATABLE :
			case MFortranLexer.PUBLIC :
			case MFortranLexer.PRIVATE :
			case MFortranLexer.ENUMERATOR :
			case MFortranLexer.OPTIONAL :
			case MFortranLexer.POINTER :
			case MFortranLexer.PROTECTED :
			case MFortranLexer.SAVE :
			case MFortranLexer.TARGET :
			case MFortranLexer.VALUE :
			case MFortranLexer.VOLATILE :
			case MFortranLexer.EXTERNAL :
			case MFortranLexer.INTRINSIC :
				// case MFortranLexer.BIND_LPAREN_C:
				// TODO - fix to BIND token
			case MFortranLexer.BIND :
			case MFortranLexer.PARAMETER :
			case MFortranLexer.IMPLICIT :
				return matchAttrStmt(lineStart, lineEnd);

			default :
				/*
				 * What's left should either be a single token stmt or failure.
				 */
				return matchSingleTokenStmt(lineStart, lineEnd);
		}
	} // end matchLine()

	/******
	 * OBSOLETE private void fixupFixedFormatLine(int lineStart, int lineEnd,
	 * boolean startsWithKeyword) { StringBuffer buffer = new StringBuffer();
	 * Token token; int i = 0;
	 * 
	 * if(startsWithKeyword == true) { do { System.out.println( "fixed-format
	 * line must start with keyword"); tokens.printPackedList(); buffer =
	 * buffer.append(tokens.getToken(lineStart+i).getText());
	 * 
	 * ANTLRStringStream charStream = new
	 * ANTLRStringStream(buffer.toString().toUpperCase()); MFortranLexer myLexer
	 * = new MFortranLexer(charStream);
	 * 
	 * // System.out.println("trying to match the string: " + //
	 * buffer.toString().toUpperCase() + // " as keyword for fixed-format
	 * continuation"); token = myLexer.nextToken(); // System.out.println(
	 * "tokens said next token.getText() is: " + // token.getText()); //
	 * System.out.println("tokens said next token.getType() is: " + //
	 * token.getType()); i++; } while((lineStart + i) < lineEnd &&
	 * tokens.isKeyword(token.getType()) == false);
	 * 
	 * // make sure we found something that is a keyword if((lineStart + i) ==
	 * lineEnd) { System.err.println("Error: Expected keyword on line: " +
	 * token.getLine()); } else { // hide all tokens that we combined to make a
	 * keyword int j = 0; Token tmpToken;
	 * 
	 * for(j = lineStart; j < lineStart+i; j++) { tmpToken = tokens.getToken(j);
	 * tmpToken.setChannel(tokens.getIgnoreChannelNumber()); //
	 * System.out.println("hiding token: " + tmpToken.getText() + // " on line:
	 * " + tmpToken.getLine()); tokens.set(j, tmpToken); }
	 * 
	 * // add the newly created token tokens.add(j, token); } } else {
	 * System.out.println("fixed-format line must NOT start with keyword"); }
	 * 
	 * return; }// end fixupFixedFormatLine() END OBSOLETE
	 *******/

	private int scanForRealConsts(int lineStart, int lineEnd) {
		int i;

		for (i = lineStart; i < lineEnd; i++) {
			if (tokens.currLineLA(i + 1) == MFortranLexer.PERIOD_EXPONENT) {
				// this case happens if the real number starts with a period, so
				// there is one token. set the type of the PERIOD_EXPONENT
				// to M_REAL_CONST for the parser.
				tokens.getToken(i).setType(MFortranLexer.M_REAL_CONST);
			} else if (tokens.currLineLA(i + 1) == MFortranLexer.DIGIT_STR
					&& (i + 2) < lineEnd
					&& (tokens.currLineLA(i + 2) == MFortranLexer.PERIOD
							|| tokens.currLineLA(
									i + 2) == MFortranLexer.PERIOD_EXPONENT)) {
				StringBuffer newTokenText = new StringBuffer();
				int line = tokens.getToken(i).getLine();
				int col = tokens.getToken(i).getCharPositionInLine();
				newTokenText.append(tokens.getToken(i).getText());
				newTokenText.append(tokens.getToken(i + 1).getText());

				/********
				 * OBSOLETE if(this.sourceForm != FrontEnd.FIXED_FORM && END
				 * OBSOLETE
				 *******/
				if ((col + tokens.getToken(i).getText().length()) != (tokens
						.getToken(i + 1).getCharPositionInLine())) {
					System.err.println(
							"Error: Whitespace within real constant at "
									+ "{line:col}: " + line + ":" + (col + 1));
				}
				// remove the two tokens for DIGIT_STR and the PERIOD or
				// PERIOD_EXPONENT so we can replace them with our own token
				// for a real constant.
				tokens.removeToken(i);

				// since we just removed one token, location i now has the
				// PERIOD or PERIOD_EXPONENT.
				tokens.removeToken(i);

				// insert the new token for a real constant
				tokens.add(i, tokens.createToken(MFortranLexer.M_REAL_CONST,
						newTokenText.toString(), line, col, inclusion));

				// we just removed two tokens and replaced it with one, so the
				// line
				// is now one token shorter.
				lineEnd -= 1;
			}
		}

		return lineEnd;
	}

	private int scanForRelationalOp(int lineStart, int lineEnd) {

		for (int i = lineStart; i < lineEnd; i++) {
			// make sure there are 3 more tokens in the line
			if (i + 2 >= lineEnd) {
				return lineEnd;
			}
			if (tokens.currLineLA(i + 1) == MFortranLexer.PERIOD) {
				// check for an ident and trailing PERIOD
				if (tokens.currLineLA(i + 2) == MFortranLexer.IDENT
						&& tokens.currLineLA(i + 3) == MFortranLexer.PERIOD) {

					int type;
					int line = tokens.getToken(i).getLine();
					int col = tokens.getToken(i).getCharPositionInLine();
					String text = tokens.getToken(i + 1).getText();

					// intrinsic relational operators are:
					// .EQ., .NE., .GT., .GE., .LT., .LE.
					if (text.compareToIgnoreCase("EQ") == 0) {
						type = MFortranLexer.EQ;
					} else if (text.compareToIgnoreCase("NE") == 0) {
						type = MFortranLexer.NE;
					} else if (text.compareToIgnoreCase("GT") == 0) {
						type = MFortranLexer.GT;
					} else if (text.compareToIgnoreCase("GE") == 0) {
						type = MFortranLexer.GE;
					} else if (text.compareToIgnoreCase("LT") == 0) {
						type = MFortranLexer.LT;
					} else if (text.compareToIgnoreCase("LE") == 0) {
						type = MFortranLexer.LE;
					} else {
						continue;
					}

					// remove three old tokens, PERIOD, IDENT and PERIOD
					tokens.removeToken(i);
					tokens.removeToken(i);
					tokens.removeToken(i);

					// replace with new token so that character position in
					// buffer
					// reflects new token
					tokens.add(i, tokens.createToken(type, "." + text + ".",
							line, col, inclusion));

					// we've just removed two tokens (and replaced one) so line
					// is shorter
					lineEnd -= 2;
				}
			}
		}

		return lineEnd;
	} // end scanForRelationalOp(int, int)

	public MFortranTokenStream performPrepass() {
		int i, lineStart, rawLineStart = 0, rawLineEnd;
		int commaIndex = -1;
		int equalsIndex = -1;
		int lineLength = 0;
		int newLineLength = 0;

		/******
		 * OBSOLETE if (this.sourceForm == FrontEnd.FIXED_FORM) { tokensStart =
		 * tokens.mark(); tokens.fixupFixedFormat(); tokens.rewind(tokensStart);
		 * }
		 * 
		 * // the mark is the curr index into the tokens array and needs to
		 * start // at -1 (before the list). This used to be what it always was
		 * when // entering this method in antlr-3.0b*, but in antlr-3.0, the
		 * number // was no longer guaranteed to be -1. so, seek the index ptr
		 * to -1. if (tokensStart != -1) { tokens.seek(-1); tokensStart = -1; }
		 * END OBSOLETE
		 ******/

		while (tokens.LA(1) != MFortranLexer.EOF) {
			// initialize necessary variables
			commaIndex = -1;
			equalsIndex = -1;
			lineStart = 0;

			rawLineStart = tokens.mark();
			while (tokens.get(rawLineStart).getChannel() == 99)
				rawLineStart++;
			tokens.seek(rawLineStart);

			// mark the start of the line
			rawLineStart = tokens.mark();

			// call the routine that buffers the whole line, including WS
			tokens.setRawLine(rawLineStart);
			// get the line length (number of non-WS tokens)
			lineLength = tokens.getCurLineLength();

			// get the end of the line
			rawLineEnd = tokens.findTokenInSuper(rawLineStart,
					MFortranLexer.EOS);
			if (rawLineEnd == -1) {
				// EOF was reached so use EOF as EOS to break loop
				rawLineEnd = tokens.getRawLineLength();
			}
			// add offset of EOS from the start to lineStart to get end
			rawLineEnd += rawLineStart;

			// check for a generated M_EOF and skip it if it exists.
			if (tokens.currLineLA(1) == MFortranLexer.M_EOF) {
				lineStart++;
			}

			// Check for an include file and skip the include tokens if they
			// exist.
			// The include tokens are INCLUDE M_INCLUDE_NAME. These tokens are
			// followed by the first line in the included file.
			if (matchInclude(lineStart, lineLength) == true) {
				lineStart += 2;
				// Check for empty include file, see bug # 2983414
				if (tokens.getToken(lineStart).getType() == MFortranLexer.M_EOF)
					lineStart += 1;
			}

			// check for a label and consume it if exists
			if (matchLabel(lineStart, lineLength) == true) {
				// consume label by advancing lineStart to next nonWS char.
				lineStart++;
			}

			// check for the optional (IDENT COLON) that some
			// constructs can have and skip if it's there.
			if (matchIdentColon(lineStart, lineLength) == true) {
				lineStart += 2;
			}

			// scan for the real literal constant tokens created by the tokens.
			// this method could shorten the line if it combines tokens into
			// M_REAL_CONST tokens so we need to update our lineLength and
			// the stored size of tokens.packedListSize (probably safer to not
			// have the variable and simply use packedList.size() calls).
			newLineLength = scanForRealConsts(lineStart, lineLength);
			if (newLineLength != lineLength) {
				lineLength = newLineLength;
			}

			// Scan for relational operators, e.g., .EQ., this is done here
			// to let the PERIOD tokens in reals be fixed first.
			// This method could shorten the line if it combines tokens into
			// EQ, ..., tokens so we need to update our lineLength and
			// the stored size of tokens.packedListSize (probably safer to not
			// have the variable and simply use packedList.size() calls).
			if (isFixedForm) {
				newLineLength = scanForRelationalOp(lineStart, lineLength);
				if (newLineLength != lineLength) {
					lineLength = newLineLength;
				}
			}

			// see if there is a comma in the stmt
			commaIndex = salesScanForToken(lineStart, MFortranLexer.COMMA);
			if (commaIndex != -1) {
				// if there is a comma, the stmt must start with a keyword
				matchLine(lineStart, lineLength);
			} else {
				// see if there is an equal sign in the stmt
				equalsIndex = salesScanForToken(lineStart,
						MFortranLexer.EQUALS);
				if (equalsIndex == -1) {
					// see if it's a pointer assignment stmt
					equalsIndex = salesScanForToken(lineStart,
							MFortranLexer.EQ_GT);
				}
				if (equalsIndex != -1) {
					/********
					 * OBSOLETE // TODO: // have to figure out how to rearrange
					 * the case where we // can't start with a keyword (given
					 * the tests below fail) for // fixed format where we may
					 * have to combine tokens to get the // statement to be
					 * accepted. // if(this.sourceForm == FrontEnd.FIXED_FORM) {
					 * // fixupFixedFormatLine(lineStart, lineLength, false); //
					 * } END OBSOLETE
					 ******/

					// we have an equal but no comma, so stmt can not start with
					// a keyword.
					// try converting any keyword node found in this line to an
					// identifier
					// this is NOT true for data declarations that have an
					// initialization expression (e.g., integer :: i = 1 inside
					// a derived type). also, this does not work for one-liner
					// statements, such as a where-stmt, if-stmt, or procedure
					// stmts.
					// first, see if it's a one-liner
					if (matchOneLineStmt(lineStart, lineLength) == false) {
						if (matchProcStmt(lineStart, lineLength) == false) {
							// if not, see if it's an assignment stmt
							if (matchAssignStmt(lineStart,
									lineLength) == false) {
								// else, match it as a data declaration
								if (matchDataDecl(lineStart,
										lineLength) == false) {
									if (matchGenericBinding(lineStart,
											lineLength) == false) {
										System.err.println(
												"Couldn't match line!");
										tokens.printCurLine();
									}
								}
							}
						}
					}
				} else {
					/********
					 * OBSOLETE // TODO: // need to make sure that this can be
					 * here because it may // prevent something from matching
					 * below... // if(this.sourceForm == FrontEnd.FIXED_FORM) {
					 * // fixupFixedFormatLine(lineStart, lineLength, true); //
					 * } END OBSOLETE
					 *******/

					// no comma and no equal sign; must start with a keyword
					// can have a one-liner stmt w/ neither
					// if(matchOneLineStmt(lineStart, lineLength) == false) {
					// matchLine(lineStart, lineLength);
					// }
					// call matchLine() first because it will try and match an
					// if_construct, etc. if that fails, we may still have a
					// one-liner statement, such as if_stmt, where_stmt, etc.
					if (matchLine(lineStart, lineLength) == false) {
						matchOneLineStmt(lineStart, lineLength);
					}
				}
			} // end if(found comma)/else(found equals or neither)

			// consume the tokens we just processed
			for (i = rawLineStart; i < rawLineEnd; i++) {
				tokens.consume();
			}

			// need to finalize the line with the FortranTokenStream in case
			// we had to change any tokens in the line
			tokens.finalizeLine();
		} // end while(not EOF)

		// We need to include the EOF in the token stream so the parser can
		// signal when to pop the include stream stack.
		tokens.addTokenToNewList(tokens.LT(1));
		// Reset to the beginning of the tokens for the parser. Reset, rather
		// than rewinding to the original mark, because the newTokenList is
		// used so should rewind and reset mark at the beginning.
		tokens.reset();
		tokens.update();
		// Update indexes
		for (int index = 0; index < tokens.size(); index++)
			((CivlcToken) tokens.get(index)).setIndex(index);
		return tokens;
	} // end performPrepass()

} // end class FortranLexicalPrepass
