parser grammar AcslParser;

/*
 * Grammar for ACSL: an ANSI/ISO C Specification Language,
 * with additional CIVL-C extensions.
 * Based on ACSL 1.12.
 * https://frama-c.com/acsl.html
 * 
 * Author: Ziqing Luo, University of Delaware
 * Author: Manchun Zheng, University of Delaware
 * Author: Stephen F. Siegel, University of Delaware
 * Last changed: June 2021
 */

//TODO: clear up the definitoins of "term" and "predicate"
//TODO: fix some definitons that were overly simplified by me so that token provider may be missing

options
{
	language=Java;
	tokenVocab=PreprocessorParser;
	output=AST;
}

tokens{
    ABSENT;
    ABSTRACT_DECLARATOR;
    ACCESS_ACSL;
    ALLOC;
    ANYACT;
    ARGUMENT_LIST;
    BITEQUIV_ACSL;
    COMMA_SEPARATED_LIST;
    ARRAY_SUFFIX;
    ASSUMES_ACSL;
    ASSIGNS_ACSL;
    ASSERT_ACSL;
    BEHAVIOR;
    BEHAVIOR_BODY;
    BEHAVIOR_COMPLETE;
    BEHAVIOR_DISJOINT;
    BIMPLIES_ACSL;
    BINDER;
    BINDER_LIST;
    BINDER_NEXT;
    BOOLEAN;
    BOTH;
    C_TYPE;
    C_TYPE_LIST;
    CALL_ACSL;
    CAST;
    CLAUSE_BEHAVIOR;
    CLAUSE_COMPLETE;
    COMM_CLAUSE;
    CONTRACT;
    DEPENDSON;
    DIRECT_ABSTRACT_DECLARATOR;
    DIRECT_ABSTRACT_DECLARATOR_ARRAY;
    DIRECT_ABSTRACT_DECLARATOR_PARAM_LIST;
    DIRECT_ABSTRACT_DECLARATOR_POINTER_TO;
    EMPTY_SET;
    EMPTY_BRACKET;
    ENSURES_ACSL;
    EVENT;
    EVENT_BASE;
    EVENT_PLUS;
    EVENT_SUB;
    EVENT_INTS;
    EVENT_LIST;
    EVENT_PARENTHESIZED;
    EXECUTES_WHEN;
    EXISTS_ACSL;
    EXPLICIT_SET_SUFFIX;
    FALSE_ACSL;
    FORALL_ACSL;
    FREES;
    FUNC_CALL;
    FUNC_CONTRACT;
    FUNC_CONTRACT_BLOCK;
    ID_LIST;
    INDEX;
    INTEGER;
    INTER;
    LAMBDA_ACSL;
    LOGIC_FUNCTIONS;
    LOGIC_FUNCTION_CLAUSE;
    LOGIC_TYPE;
    LOOP_ALLOC;
    LOOP_ASSIGNS;
    LOOP_BEHAVIOR;
    LOOP_CLAUSE;
    LOOP_CONTRACT_BLOCK;
    LOOP_FREE;
    LOOP_INVARIANT;
    LOOP_VARIANT;
    MAX;
    MIN;
    MPI_AGREE;
    MPI_BUF;
    MPI_BACK_MESSAGES;
    MPI_COMM_RANK;
    MPI_COMM_SIZE;
    MPI_CONSTANT;
    MPI_CONTRACT;
    MPI_COLLECTIVE_BLOCK;
    MPI_EXPRESSION;
    MPI_EXTENT;
    MPI_NONOVERLAPPING;
    MPI_ON;
    MPI_REDUCE;
    MPI_SIG;
    NOTHING;
    NULL_ACSL;
    NUMOF;
    OBJECT_OF;
    OLD;
    OPERATOR;
    POINTER;
    PROD;
    PURE;
    PREDICATE_CLAUSE;
    LOGIC_FUNCTION_BODY; /* shared by both predicate and logic function */
    QUANTIFIED;
    QUANTIFIED_EXT;
    READ_ACSL;
    READS_ACSL;
    REAL_ACSL;
    RELCHAIN;
    RELCHAIN_SEGMENT;
    RESULT_ACSL;
    REQUIRES_ACSL;
    SEPARATED;
    SET_BINDERS;
    CURLY_BRACED_EXPR;
    SIZEOF_EXPR;
    SIZEOF_TYPE;
    SPECIFIER_QUALIFIER_LIST;
    SUM;
    TERMINATES;
    TRUE_ACSL;
    TYPE_BUILTIN;
    TYPE_ID;
    UNION_ACSL;
    VALID;
    VALID_READ;
    VARIABLE_ID;
    WAITSFOR;
    WRITE_ACSL;
}

@header
{
package edu.udel.cis.vsl.abc.front.c.parse;
}

contract
    : loop_contract_block 
    | function_contract
    | logic_function_contract
    | assert_contract
    ;

/* Section 2.4.2 Loop Annotations */
loop_contract_block
    : lc+=loop_clause+ lb+=loop_behavior* 
        ->^(LOOP_CONTRACT_BLOCK $lc+ $lb*)
    ;

loop_clause
    : loop_key a=loop_clause_suffix SEMI
      -> ^(LOOP_CLAUSE $a)
    ; 

loop_clause_suffix
    : invariant_key term
        -> ^(LOOP_INVARIANT term)
    | assigns_key commaSeparatedList
        -> ^(LOOP_ASSIGNS commaSeparatedList)        
    | alloc_key commaSeparatedList (COMMA term)?
        -> ^(LOOP_ALLOC commaSeparatedList term?)
    | frees_key commaSeparatedList
        -> ^(LOOP_FREE commaSeparatedList)
    | variant_key term
        -> ^(LOOP_VARIANT term)
    ;

loop_behavior
    : FOR ilist=id_list COLON lc+=loop_clause*
        ->^(LOOP_BEHAVIOR $ilist $lc*)
    ;

/* sec. 2.3 Function contracts */
/* a full contract block non-terminal represents an ACSL contract
 * block for a function */
function_contract
    : (f=partial_contract_block) (m+=mpi_contract)*
        (c+=completeness_clause_block)* 
        -> ^(FUNC_CONTRACT $f $m* $c*) 
    ;

/* sec. 2.6.1 Predicate and (Logic) Function
 * definitions. Semantically, predicates are logic functions as
 * well. */
logic_function_contract
    : (a+=logic_function_clause+) -> ^(LOGIC_FUNCTIONS $a+)
    ;

logic_function_clause
    : logic_specifier_key type_expr a=IDENTIFIER LPAREN b=binders RPAREN 
                                    c=logic_function_body? SEMI
        -> ^(LOGIC_FUNCTION_CLAUSE type_expr $a $b $c?)
    | predicate_key a=IDENTIFIER LPAREN b=binders RPAREN c=logic_function_body? SEMI
        -> ^(PREDICATE_CLAUSE $a $b $c?) 
    ;

/* simple ACSL assertion */
assert_contract
    : assert_key term SEMI -> ^(ASSERT_ACSL term)
    ;

/* ACSL logic function (predicate) declaration, either binder is
 * absent or body is absent. They cannot be both absent. */
/* binders (optional) = function-body */
logic_function_body
    : ASSIGN term 
      -> ^(LOGIC_FUNCTION_BODY term)
    ;

pure_function
    : pure_key SEMI
    ;

/* a partial contract block non-terminal represents an ACSL contract
 * block inside an MPI collective block. There is no nested MPI
 * collective block allowed */
partial_contract_block
    : (f+=function_clause)* (b+=named_behavior_block)* 
        (c+=completeness_clause_block)* 
        -> ^(FUNC_CONTRACT_BLOCK $f* $b* $c*) 
    ;

mpi_contract
    : mpi_key ((a+=comm_clause SEMI) | (a+=mpi_collective_block))+
      -> ^(MPI_CONTRACT $a+)
    ;

/* the "mpi uses term (, term)*" clause */
comm_clause
    : uses_key term (COMMA term)* -> ^(COMM_CLAUSE term+)
    ;

/* ACSL-MPI extensions: constructors */
mpi_collective_block
    : collective_key LPAREN a=term RPAREN COLON b=partial_contract_block 
      -> ^(MPI_COLLECTIVE_BLOCK $a $b)
    ;

function_clause
    : requires_clause SEMI -> requires_clause
    | terminates_clause SEMI-> terminates_clause
    | simple_clause SEMI -> simple_clause
    ;

named_behavior_block
    : named_behavior -> named_behavior
    ;

completeness_clause_block
    : completeness_clause SEMI -> ^(CLAUSE_COMPLETE completeness_clause)
    ;

requires_clause
    : requires_key term -> ^(REQUIRES_ACSL requires_key term)
    ;

terminates_clause
    : terminates_key term -> ^(TERMINATES terminates_key term)
    ;

binders
    : a=binder b=binder_next*
        ->^(BINDER_LIST $a $b*)
    ;

binder
    : (a=logic_type_expr | a=c_specifierQualifierList) b=variable_ident
      -> ^(BINDER $a $b)
    ;

binder_next
    : COMMA (a=variable_ident | a=binder)
      -> ^(BINDER_NEXT $a)
    ;

variable_ident
    : IDENTIFIER (empty_brackets)*   -> ^(VARIABLE_ID IDENTIFIER empty_brackets*)
    | STAR variable_ident           -> ^(VARIABLE_ID STAR variable_ident)
    | LPAREN variable_ident RPAREN    -> variable_ident
    ;

empty_brackets
    : {input.LA(1) == LSQUARE && input.LA(2) == RSQUARE}?LSQUARE RSQUARE -> ^(EMPTY_BRACKET)
    ;

type_expr
    : logic_type_expr
    | a=c_specifierQualifierList b=c_abstractDeclarator
      -> ^(C_TYPE $a $b)
    ;

type_expr_list
    : a+=type_expr (COMMA a+=type_expr)* -> ^(C_TYPE_LIST $a*)
    ;

/* Start of C-like type name syntax */
c_specifierQualifierList
    : c_basic_type+
      -> ^(SPECIFIER_QUALIFIER_LIST c_basic_type+)
    ;

/* C (abstract) declarator */
c_abstractDeclarator
    : c_pointer (a=c_directAbstractDeclarator)? 
      -> ^(ABSTRACT_DECLARATOR c_pointer $a?)
    ;

c_directAbstractDeclarator
    : ( (LPAREN)=> a=c_directAbstractDeclarator_lparen b=c_directAbstractDeclarator?
        | (LSQUARE)=> a=c_directAbstractDeclarator_lsquare b=c_directAbstractDeclarator? )
      -> ^(DIRECT_ABSTRACT_DECLARATOR $a $b?)
    ;


/* C direct abstract declarator suffix starts from LPAREN */
// Note the hard-coded STAR here is to distinguish between pointer
// (the first case) and function (the second case)
c_directAbstractDeclarator_lparen
    : LPAREN 
      (  (STAR a=c_abstractDeclarator RPAREN   
            -> ^(DIRECT_ABSTRACT_DECLARATOR_POINTER_TO $a))
       | (a=type_expr_list? RPAREN
          -> ^(DIRECT_ABSTRACT_DECLARATOR_PARAM_LIST $a?))
      )
    ;

c_directAbstractDeclarator_lsquare
    : (LSQUARE a=constantExpression RSQUARE)
       -> ^(DIRECT_ABSTRACT_DECLARATOR_ARRAY $a)
    ;

c_pointer
    : a+=STAR* -> ^(POINTER $a*)
    ;

/* End of C-like type name syntax */


logic_type_expr
    : built_in_logic_type ->  ^(LOGIC_TYPE ^(TYPE_BUILTIN built_in_logic_type))
    ;

c_basic_type
    : CHAR | DOUBLE | FLOAT | INT | LONG | SHORT | VOID
    ;

built_in_logic_type
    : boolean_type | integer_type | real_type
    ;

guards_clause
    : executeswhen_key a=relationalExpression ->^(EXECUTES_WHEN executeswhen_key $a)
    ;

simple_clause
    : assigns_clause
    | ensures_clause 
    | allocation_clause
    | reads_clause
    | depends_clause
    | guards_clause
    | waitsfor_clause
    ;

assigns_clause
    : assigns_key commaSeparatedList ->^(ASSIGNS_ACSL commaSeparatedList)
    ;

ensures_clause
    : ensures_key term ->^(ENSURES_ACSL ensures_key term)
    ;

allocation_clause
    : alloc_key commaSeparatedList ->^(ALLOC alloc_key commaSeparatedList)
    | frees_key commaSeparatedList ->^(FREES frees_key commaSeparatedList)
    ;

reads_clause
    : reads_key commaSeparatedList ->^(READS_ACSL commaSeparatedList)
    ;

waitsfor_clause
    : waitsfor_key term (COMMA term)* -> ^(WAITSFOR term+)
    ;

depends_clause
    : dependson_key event_list ->^(DEPENDSON dependson_key event_list)
    ;

event_list
    : event (COMMA event)* -> ^(EVENT_LIST event+)
    ;

event
    : a=event_base b=event_suffix?
      ->  ^(EVENT $a $b?)
    ;

event_suffix
    : PLUS event_base
        -> ^(EVENT_PLUS event_base)
    | SUB event_base
        -> ^(EVENT_SUB event_base)
    | AMPERSAND event_base
        -> ^(EVENT_INTS event_base)
    ;

event_base
    : read_key LPAREN commaSeparatedList RPAREN
        -> ^(READ_ACSL commaSeparatedList)
    | write_key LPAREN commaSeparatedList RPAREN
        -> ^(WRITE_ACSL commaSeparatedList)
    | access_key LPAREN commaSeparatedList RPAREN
        -> ^(ACCESS_ACSL commaSeparatedList)
    | call_key LPAREN IDENTIFIER (COMMA commaSeparatedList)? RPAREN
        -> ^(CALL_ACSL IDENTIFIER commaSeparatedList?)
    | nothing_key
    | anyact_key
    | LPAREN event RPAREN
        -> ^(EVENT_PARENTHESIZED event)
    ;

/* sec. 2.3.3 contracts with named behaviors */
named_behavior
    : behavior_key IDENTIFIER COLON behavior_body
        -> ^(BEHAVIOR behavior_key IDENTIFIER behavior_body)
    ;

behavior_body
    : (b+=behavior_clause SEMI)+ -> ^(BEHAVIOR_BODY $b+)
    ;

behavior_clause
    : assumes_clause 
    | requires_clause
    | simple_clause
    ;

assumes_clause
    : assumes_key term ->^(ASSUMES_ACSL assumes_key term)
    ;

completeness_clause    //$a here provides the only token when id_list is absent
    : completes_key a=behaviors_key id_list?
        -> ^(BEHAVIOR_COMPLETE $a id_list?)
    | disjoint_key a=behaviors_key id_list?
        -> ^(BEHAVIOR_DISJOINT $a id_list?)
    ;

id_list
    : IDENTIFIER (COMMA IDENTIFIER)* -> ^(ID_LIST IDENTIFIER+)
    ;

/* C11 section 6.5 Expressions: Grammar here is organized with a
 * backwards order against the order of sub-sections in C11 standard,
 * because it's a more viewful way to illustrate how expressions will
 * be derived
 */
 
 /* ****************************** Expressions ******************************* */

// SFS: why is this called a "term"?  Why not "formula"?
term
    : quantifierExpression | assignmentExpression 
    ;
    
quantifierExpression
	: forall_key binders SEMI term
	   -> ^(QUANTIFIED forall_key binders term) 
    | exists_key binders SEMI term
       -> ^(QUANTIFIED exists_key binders term) 
	| lambda_key binders SEMI term
	   -> ^(LAMBDA_ACSL lambda_key binders term)
	;

/* SFS: Does ACSL have an assignment expression?
 * 6.5.16 
 * assignment-expression
 *   conditional-expression
 *   unary-expression assignment-operator assignment-expression
 * Tree:
 * Root: OPERATOR
 * Child 0: ASSIGN, in ACSL other side-effective assign operators 
 *          are not allowed
 * Child 1: ARGUMENT_LIST
 * Child 1.0: unaryExpression
 * Child 1.1: assignmentExpression
 */
assignmentExpression
	: (unaryExpression ASSIGN)=> unaryExpression ASSIGN assignmentExpression
	  -> ^(OPERATOR ASSIGN
            ^(ARGUMENT_LIST unaryExpression assignmentExpression))
	| conditionalExpression
	;

assignmentExpression_opt
    : -> ABSENT
    | assignmentExpression
    ;

/* 6.5.15
 * In C11 it is
 * conditional-expression:
 *   logical-OR-expression
 *   logical-OR-expression ? expression : conditional-expression
 * 
 * Note:  "a?b:c?d:e".  Is it (1) "(a?b:c)?d:e"  or (2) "a?b:(c?d:e)".
 * Answer is (2), it is "right associative".
 *
 * Note: the order matters in the two alternatives below.
 * The alternatives are tried in order from first to last.
 * Therefore it is necessary for the non-empty to appear first.
 * Else the empty will always be matched.
 */
conditionalExpression
	: a=logicalEquivExpression
        ( (QMARK)=> QMARK b=assignmentExpression COLON 
            (c=quantifierExpression | c=conditionalExpression)
            -> ^(OPERATOR QMARK ^(ARGUMENT_LIST $a $b $c))
        | -> $a
    	)
	;

/* ACSL Logical equivalence: a<==>b.
 * Left associative: a<==>b<==>c means (a<==>b)<==>c.
 */
logicalEquivExpression
	: (a=logicalImpliesExpression -> $a)
        ( (EQUIV_ACSL)=> EQUIV_ACSL (b=quantifierExpression | b=logicalImpliesExpression)
            -> ^(OPERATOR EQUIV_ACSL ^(ARGUMENT_LIST $logicalEquivExpression $b))
        )*
    ;

/* ACSL logical implies expression: a==>b.
 * NOTE: *RIGHT* associative: a==>b==>c is a==>(b==>c).
 */
logicalImpliesExpression
	: a=logicalOrExpression
        ( (IMPLIES_ACSL)=> op=(IMPLIES_ACSL) (b=quantifierExpression | b=logicalImpliesExpression)
            -> ^(OPERATOR $op ^(ARGUMENT_LIST $a $b))
        | -> $a
        )
    ;

/* logical-OR-expression: a||b.
 * Left associative: a||b||c is (a||b)||c.
 */
logicalOrExpression
	: (a=logicalXorExpression -> $a)
        ( (OR)=> OR (b=quantifierExpression | b=logicalXorExpression)
            -> ^(OPERATOR OR ^(ARGUMENT_LIST $logicalOrExpression $b))
        )*
	;
	
/* ACSL logical exclusive or: a^^b.
 * Left associative.
 */
logicalXorExpression
	: (a=logicalAndExpression -> $a)
        ( (XOR_ACSL)=> XOR_ACSL (b=quantifierExpression | b=logicalAndExpression)
            -> ^(OPERATOR XOR_ACSL ^(ARGUMENT_LIST $logicalXorExpression $b))
        )*
	;

/* 6.5.13, logical and: a && b.
 * Left associative.
 */
logicalAndExpression
	: (a=bitwiseEquivExpression -> $a)
        ( (AND)=> AND (b=quantifierExpression | b=bitwiseEquivExpression)
            -> ^(OPERATOR AND ^(ARGUMENT_LIST $logicalAndExpression $b))
        )*
	;

/* ACSL bitwise equivalence: a <--> b. 
 * Left associative.
 */
bitwiseEquivExpression
	: (a=bitwiseImpliesExpression -> $a)        
        ( {(input.LT(1).getType() == LT &&
            input.LT(2).getType() == MINUSMINUS &&
            input.LT(3).getType() == GT)
          }?LT MINUSMINUS GT  b=bitwiseImpliesExpression
            -> ^(OPERATOR BITEQUIV_ACSL ^(ARGUMENT_LIST $a $b)) 
        )*
	;

/* ACSL bitwise implies: a-->b.
 * RIGHT associative
 */
bitwiseImpliesExpression
	: a=inclusiveOrExpression
        ({(input.LT(1).getType() == MINUSMINUS &&
           input.LT(2).getType() == GT)
         }?MINUSMINUS GT b=bitwiseImpliesExpression
            -> ^(OPERATOR BIMPLIES_ACSL ^(ARGUMENT_LIST $a $b))
        | -> $a
        )
	;

// TODO: SFS: look at this, it doesn't make sense...
/* 6.5.12 *
 * Bitwise inclusive OR
 * inclusive-OR-expression:
 *   exclusive-OR-expression
 *   inclusive-OR-expression | exclusive-OR-expression
 *
 * Note: the syntatic predicate before BITOR is to solve the ambiguity
 *       with set expressions.  
 *
 *       For "expr | expr", here the | is an bit-or operator. 
 *
 *       For "{expr | t var ; ...}", where t is any of the built-in
 *       types: CHAR, DOUBLE, FLOAT, INT, LONG, SHORT, boolean, real,
 *       or integer, the | is the bar in set-comprehension
 *       expressions.
 * 
 */
inclusiveOrExpression
	: ( x=exclusiveOrExpression -> exclusiveOrExpression )
	  ( {        
        !(input.LT(2).getType() == CHAR  ||   
         input.LT(2).getType() == DOUBLE ||
         input.LT(2).getType() == FLOAT  ||
         input.LT(2).getType() == INT    ||
         input.LT(2).getType() == LONG   ||
         input.LT(2).getType() == SHORT  ||
         input.LT(2).getType() == VOID   ||
         input.LT(2).getText().equals("boolean")  ||
         input.LT(2).getText().equals("real")     ||
         input.LT(2).getText().equals("integer")
         )         
      }?BITOR y=exclusiveOrExpression
	    -> ^(OPERATOR BITOR ^(ARGUMENT_LIST $x $y))
	  )*
	;

/* 6.5.11 *
 * Bitwise exclusive OR
 * exclusive-OR-expression:
 *   AND-expression
 *   exclusive-OR-expression ^ AND-expression
 */
exclusiveOrExpression
    : ( andExpression -> andExpression )
	  ( (BITXOR)=> BITXOR y=andExpression
	    -> ^(OPERATOR BITXOR ^(ARGUMENT_LIST $exclusiveOrExpression $y))
	  )*
	;

/* 6.5.10 *
 * Bitwise AND
 * AND-expression
 *   equality-expression
 *   AND-expression & equality-expression
 */
andExpression
	: ( relationalExpression -> relationalExpression )
	  ( (AMPERSAND)=> AMPERSAND y=relationalExpression
	    -> ^(OPERATOR AMPERSAND ^(ARGUMENT_LIST $andExpression $y))
	  )*
	;


/*
  Note on ACSL relational expressions, from the ACSL manual:
  The construct t1 relop1 t2 relop2 t3 · · · tk
  with several consecutive comparison operators is a shortcut
  (t1 relop1 t2) && (t2 relop2 t3) && ···.
  It is required that the relopi operators must be in the same “direction”,
  i.e. they must all belong either to {<, <=, ==} or to {>,>=,==}.
  Expressions such as x < y > z or x != y != z are not allowed.

  Also, <,<=,>=,> have higher precedence than == and !=.  Though
  not sure what that means, so ignoring it.

  "a<b==c" means "a<b && b==c".

   a<b<c<d : (and (a<b) (and (b<c) (c<d)))

  Grammar:  The following works but doesn't check for illegal expressions.
  Better: create a new node RELCHAIN
  args: a < b <= c < d, in order, then check and assemble in Java code.

relationalExpression
    : x=shiftExpression
        ( r=relChain[(Tree)$x.tree] -> $r
        | -> $x
        )
    ;

// t is the tree of a single shiftExpression,  t < y (< ...) 
relChain[Tree t]
    : r=relOp y=shiftExpression
        ( z=relChain[(Tree)$y.tree]
            -> ^(OPERATOR AND ^(ARGUMENT_LIST
                    ^(OPERATOR $r ^(ARGUMENT_LIST {$t} $y))
            $z))
        | -> ^(OPERATOR $r ^(ARGUMENT_LIST {$t} $y))
        )
    ;
*/

/* A relational operator */
relOp: EQUALS | NEQ | LT | LTE | GT | GTE ;

/* A relational expression or chain of such expressions.
 * Returns a tree with root RELCHAIN and then the sequence
 * that alternates shiftExpression, relational operator,
 * and begins and ends with a shiftExpression.
 * 
 * Note the syntactic predicate below is similar to say "look ahead
 * for relOp" but explicitly to resolve the ambiguity of whether the
 * look-ahead token "<" belongs to "<" itself or "<--".
 */
relationalExpression
    : a=shiftExpression (
            {(input.LA(1) == EQUALS ||
              input.LA(1) == NEQ    ||
              (input.LA(1) == LT && input.LA(2) != MINUSMINUS) ||
              input.LA(1) == LTE    ||
              input.LA(1) == GT     ||
              input.LA(1) == GTE)
            }?
            b+=relationalChainSegments)* -> ^(RELCHAIN $a $b*)
    ;

relationalChainSegments
    : (a=relOp b=shiftExpression) -> ^(RELCHAIN_SEGMENT $a $b)
    ;

/* 6.5.7 *
 * In C11:
 * shift-expression:
 *   additive-expression
 *   shift-expression <</>> additive-expression
 *
 * CIVL-C extends C11 with a range-expression. see range-expression
 * shift-expression:
 *   range-expression:
 *   shift-expression <</>> range-expression
 */
shiftExpression
	: (rangeExpression -> rangeExpression)
        ( (SHIFTLEFT)=> SHIFTLEFT y=rangeExpression
          -> ^(OPERATOR SHIFTLEFT ^(ARGUMENT_LIST $shiftExpression $y))
        | (SHIFTRIGHT)=> SHIFTRIGHT y=rangeExpression
          -> ^(OPERATOR SHIFTRIGHT ^(ARGUMENT_LIST $shiftExpression $y))
        )*
	;

/* 6.5.6.5 *
 *
 * CIVL-C range expression "lo .. hi" or "lo .. hi # step" 
 * a + b .. c + d is equivalent to (a + b) .. (c + d) 
 * (*,/,%) > (+,-) > range > shift > ...
 */
rangeExpression
	: x=additiveExpression
        ( (DOTDOT)=> DOTDOT s=rangeSuffix -> ^(DOTDOT $x $s)
        | -> $x
        )
    ;

rangeSuffix
    : additiveExpression ((HASH)=> HASH! additiveExpression)?
    ;

/* 6.5.6 *
 * additive-expression:
 *   multiplicative-expression
 *   additive-expression +/- multiplicative-expression
 */
additiveExpression
	: (multiplicativeExpression -> multiplicativeExpression)
        ( (PLUS)=> PLUS y=multiplicativeExpression
          -> ^(OPERATOR PLUS ^(ARGUMENT_LIST $additiveExpression $y))
        | (SUB)=> SUB y=multiplicativeExpression
          -> ^(OPERATOR SUB ^(ARGUMENT_LIST $additiveExpression $y))
        )*
	;

/* 6.5.5 * 
 * In C11:
 * multiplicative-expression:
 *   cast-expression
 *   multiplicative-expression STAR/DIV/MOD cast-expression
 */
multiplicativeExpression
	: (castExpression -> castExpression)
	( (STAR)=> STAR y=castExpression
	  -> ^(OPERATOR STAR ^(ARGUMENT_LIST $multiplicativeExpression $y))
	| (DIV)=> y=castExpression
	  -> ^(OPERATOR DIV ^(ARGUMENT_LIST $multiplicativeExpression $y))
    | (MOD)=> MOD y=castExpression
	  -> ^(OPERATOR MOD ^(ARGUMENT_LIST $multiplicativeExpression $y))
    )*
	;

/* 6.5.4 *
 * cast-expression:
 *   unary-expression
 *   (type-name) cast-expression
 *
 */
// ambiguity 1: (expr) is a unary expression and looks like (typeName).
// ambiguity 2: (typeName){...} is a compound literal and looks like cast
castExpression
	: (LPAREN type_expr RPAREN)=> l=LPAREN type_expr RPAREN castExpression
	  -> ^(CAST type_expr castExpression)
	| unaryExpression
	;

/* 6.5.3 *
 * unary-expression:
 *   postfix-expression
 *   ++/--/sizeof unary-expression
 *   unary-operator cast-expression
 *   sizeof (type-name)
 */
unaryExpression
	: postfixExpression
	| unary_op (b=castExpression | b=quantifierExpression)
        -> ^(OPERATOR unary_op ^(ARGUMENT_LIST $b))
	| (SIZEOF LPAREN type_expr)=> SIZEOF LPAREN type_expr RPAREN
        -> ^(SIZEOF_TYPE type_expr)
	| SIZEOF unaryExpression
        -> ^(SIZEOF_EXPR unaryExpression)
    | union_key LPAREN commaSeparatedList RPAREN
        -> ^(UNION_ACSL union_key commaSeparatedList RPAREN)
    | inter_key LPAREN commaSeparatedList RPAREN
        -> ^(INTER inter_key commaSeparatedList RPAREN)
    | valid_key LPAREN term RPAREN
        -> ^(VALID term)
    | validread_key LPAREN term RPAREN
        -> ^(VALID_READ term)
    | extendedQuantification ->^(QUANTIFIED_EXT extendedQuantification)
    | object_of_key LPAREN term RPAREN -> ^(OBJECT_OF object_of_key LPAREN term RPAREN)
    | mpi_expression -> ^(MPI_EXPRESSION mpi_expression)
    | old_key LPAREN term RPAREN 
        -> ^(OLD old_key term RPAREN)
	;

extendedQuantification
	: sum_key LPAREN term COMMA term COMMA term RPAREN
        -> ^(SUM sum_key term+)
    | max_key LPAREN term COMMA term COMMA term RPAREN
        -> ^(MAX max_key term+)
    | min_key LPAREN term COMMA term COMMA term RPAREN
        -> ^(MIN min_key term+)
    | product_key LPAREN term COMMA term COMMA term RPAREN
        -> ^(PROD product_key term+)
    | numof_key LPAREN term COMMA term COMMA term RPAREN
        -> ^(NUMOF numof_key term+)
	;

/* 6.5.2 *
 * postfix-expression:
 *   primary-expression
 *   postfix-expression [expression]
 *   postfix-expression (argument-expression-list)
 *   postfix-expression . identifier
 *   postfix-expression -> identifier
 *   postfix-expression ++
 *   postfix-expression --
 *   (type-name) {initializer-list}
 *   (type-name) {initializer-list, }
 */
postfixExpression
	: (primaryExpression -> primaryExpression)
		// array index operator:
	  ( l=LSQUARE term RSQUARE
	    -> ^(OPERATOR
	           INDEX[$l]
	           ^(ARGUMENT_LIST $postfixExpression term)
	           RSQUARE)
	  |	// function call:
            (LPAREN)=> LPAREN a=commaSeparatedList? RPAREN
	    -> ^(FUNC_CALL $postfixExpression $a?
	    	 )
	  | DOT IDENTIFIER
	    -> ^(DOT $postfixExpression IDENTIFIER)
	  | ARROW IDENTIFIER
	    -> ^(ARROW $postfixExpression IDENTIFIER)
	  )*
	 ;

/* 6.5.2 */
commaSeparatedList
	: (a=assignmentExpression -> assignmentExpression)
      ((COMMA)=> COMMA b+=assignmentExpression)*
	    -> ^(COMMA_SEPARATED_LIST $a $b*)
	;
        
/* 6.5.1 */
primaryExpression
	: constant
    | IDENTIFIER
	| STRING_LITERAL
    | LPAREN a+=term (COMMA a+=term)* RPAREN
        -> $a*
    | curly_braced_expression 
    | empty_key
        -> ^(EMPTY_SET)
    | separated_key LPAREN term (COMMA term)* RPAREN
        -> ^(SEPARATED term+)
	;

/* An curly-braced expression is either an explicit-set or a
 * set-comprehension.
 *
 * explicit-set:       {term, (term)*} or { } 
 * set-comprehension:  {term | (binders (;predicate)?}
 */
curly_braced_expression
    : LCURLY ( a=assignmentExpression b=curly_braced_expression_suffix -> ^(CURLY_BRACED_EXPR $a $b)
             | RCURLY -> ^(CURLY_BRACED_EXPR LCURLY RCURLY) //TODO: this case misses at least a token provider
             )
    ;

curly_braced_expression_suffix
    : (COMMA assignmentExpression)* RCURLY -> ^(EXPLICIT_SET_SUFFIX assignmentExpression*)
    | BITOR  a=binders (SEMI b=assignmentExpression)? RCURLY 
      -> ^(SET_BINDERS $a $b?)
    ;


/* 6.6 */
constantExpression
	: conditionalExpression 
	;

constant
	: INTEGER_CONSTANT
	| FLOATING_CONSTANT
	| CHARACTER_CONSTANT
	| true_key | false_key  | result_key | nothing_key | ELLIPSIS
    | SELF | null_key
	;

/* ACSL-MPI extensions Expressions and Constants  */
mpi_expression
    : mpiagree_key LPAREN a=term RPAREN 
        -> ^(MPI_AGREE $a) 
    | mpibuf_key LPAREN a=term COMMA b=term COMMA c=term RPAREN
        -> ^(MPI_BUF $a $b $c)
    | mpiextent_key LPAREN a=term RPAREN
        -> ^(MPI_EXTENT $a)
    | mpinonoverlapping_key LPAREN a=term RPAREN
        -> ^(MPI_NONOVERLAPPING $a)
    | mpion_key LPAREN a=term COMMA b=term RPAREN
        -> ^(MPI_ON $a $b)
    | mpireduce_key LPAREN a=term COMMA b=term COMMA c=term COMMA d=term COMMA e=term RPAREN
        -> ^(MPI_REDUCE $a $b $c $d $e)
    | mpisig_key LPAREN a=term RPAREN
        -> ^(MPI_SIG $a)
    | mpi_constant
    ;

mpi_constant
    : mpicommrank_key |  mpicommsize_key | mpibackmessages_key
    ;

unary_op
    : PLUS | SUB | NOT | TILDE | STAR | AMPERSAND
    ;
    
/* rules for ACSL types */
boolean_type
    : {input.LT(1).getText().equals("boolean")}? IDENTIFIER
        -> ^(BOOLEAN IDENTIFIER)
    ;

integer_type
    : {input.LT(1).getText().equals("integer")}? IDENTIFIER
        -> ^(INTEGER IDENTIFIER)
    ;

real_type
    : {input.LT(1).getText().equals("real")}? IDENTIFIER
        -> ^(REAL_ACSL IDENTIFIER)
    ;

/* rules for ACSL contract clause keywords */
alloc_key 
    : {input.LT(1).getText().equals("allocates")}? IDENTIFIER
    ; 

assigns_key 
    : {input.LT(1).getText().equals("assigns")}? IDENTIFIER
    ; 

assumes_key 
    : {input.LT(1).getText().equals("assumes")}? IDENTIFIER
    ; 

assert_key
    : {input.LT(1).getText().equals("assert")}? IDENTIFIER
    ;

behaviors_key 
    : {input.LT(1).getText().equals("behaviors")}? IDENTIFIER
    ; 

behavior_key 
    : {input.LT(1).getText().equals("behavior")}? IDENTIFIER
    ; 

completes_key 
    : {input.LT(1).getText().equals("complete")}? IDENTIFIER
    ; 

decreases_key
    : {input.LT(1).getText().equals("decreases")}? IDENTIFIER
    ; 

disjoint_key 
    : {input.LT(1).getText().equals("disjoint")}? IDENTIFIER
    ; 

ensures_key 
    : {input.LT(1).getText().equals("ensures")}? IDENTIFIER
    ;    
  
frees_key
    : {input.LT(1).getText().equals("frees")}? IDENTIFIER
    ; 
  
invariant_key
    : {input.LT(1).getText().equals("invariant")}? IDENTIFIER
    ;

loop_key 
	: {input.LT(1).getText().equals("loop")}? IDENTIFIER
    ; 

requires_key
    : {input.LT(1).getText().equals("requires")}? IDENTIFIER
    ;
    
terminates_key
    : {input.LT(1).getText().equals("terminates")}? IDENTIFIER
    ;
   
variant_key
    : {input.LT(1).getText().equals("variant")}? IDENTIFIER
    ;

waitsfor_key
    : {input.LT(1).getText().equals("waitsfor")}? IDENTIFIER
    ;

predicate_key
	: {input.LT(1).getText().equals("predicate")}? IDENTIFIER
	;

logic_specifier_key
	: {input.LT(1).getText().equals("logic")}? IDENTIFIER
	;

/* ACSL terms keywords */
/* keywords of terms */
empty_key
    : {input.LT(1).getText().equals("\\empty")}? EXTENDED_IDENTIFIER
    ;

exists_key
    : {input.LT(1).getText().equals("\\exists")}? EXTENDED_IDENTIFIER
    -> ^(EXISTS_ACSL EXTENDED_IDENTIFIER)
    ;

false_key
    : {input.LT(1).getText().equals("\\false")}? EXTENDED_IDENTIFIER
    -> ^(FALSE_ACSL EXTENDED_IDENTIFIER)
    ;

forall_key
    : {input.LT(1).getText().equals("\\forall")}? EXTENDED_IDENTIFIER
    -> ^(FORALL_ACSL EXTENDED_IDENTIFIER)
    ;

inter_key
    : {input.LT(1).getText().equals("\\inter")}? EXTENDED_IDENTIFIER
    ;

let_key
    : {input.LT(1).getText().equals("\\let")}? EXTENDED_IDENTIFIER
    ;

nothing_key
    : {input.LT(1).getText().equals("\\nothing")}? EXTENDED_IDENTIFIER
    -> ^(NOTHING EXTENDED_IDENTIFIER)
    ;

null_key
    : {input.LT(1).getText().equals("\\null")}? EXTENDED_IDENTIFIER
    -> ^(NULL_ACSL EXTENDED_IDENTIFIER)
    ;

old_key
    : {input.LT(1).getText().equals("\\old")}? EXTENDED_IDENTIFIER
    ;

result_key
    : {input.LT(1).getText().equals("\\result")}? EXTENDED_IDENTIFIER
    -> ^(RESULT_ACSL EXTENDED_IDENTIFIER)
    ;

separated_key 
    : {input.LT(1).getText().equals("\\separated")}? EXTENDED_IDENTIFIER
    ;         

true_key
    : {input.LT(1).getText().equals("\\true")}? EXTENDED_IDENTIFIER
    -> ^(TRUE_ACSL EXTENDED_IDENTIFIER)
    ;

union_key
    : {input.LT(1).getText().equals("\\union")}? EXTENDED_IDENTIFIER
    ;

valid_key
    : {input.LT(1).getText().equals("\\valid")}? EXTENDED_IDENTIFIER
    ;

validread_key
    : {input.LT(1).getText().equals("\\valid_read")}? EXTENDED_IDENTIFIER
    ;

with_key
    : {input.LT(1).getText().equals("\\with")}? EXTENDED_IDENTIFIER
    ;

/* ACSL CIVL extension */
executeswhen_key
    : {input.LT(1).getText().equals("executes_when")}? IDENTIFIER
    ; 

pure_key
    : {input.LT(1).getText().equals("pure")}? IDENTIFIER
    -> ^(PURE IDENTIFIER)
    ;

reads_key
    : {input.LT(1).getText().equals("reads")}? IDENTIFIER
    ;   

/* ACSL dependence-specification extension */

access_key
    : {input.LT(1).getText().equals("\\access")}? EXTENDED_IDENTIFIER
//    -> ^(ACCESS_ACSL EXTENDED_IDENTIFIER)
    ;

anyact_key
    : {input.LT(1).getText().equals("\\anyact")}? EXTENDED_IDENTIFIER
    -> ^(ANYACT EXTENDED_IDENTIFIER)
    ;


call_key
    : {input.LT(1).getText().equals("\\call")}? EXTENDED_IDENTIFIER
    ;

dependson_key
    : {input.LT(1).getText().equals("depends_on")}? IDENTIFIER
    ;
    
object_of_key
    : {input.LT(1).getText().equals("\\object_of")}? EXTENDED_IDENTIFIER
    ; 

read_key
    : {input.LT(1).getText().equals("\\read")}? EXTENDED_IDENTIFIER
//    -> ^(READ_ACSL EXTENDED_IDENTIFIER)
    ;
    
region_of_key
    : {input.LT(1).getText().equals("\\region_of")}? EXTENDED_IDENTIFIER
    ;

write_key
    : {input.LT(1).getText().equals("\\write")}? EXTENDED_IDENTIFIER
//    -> ^(WRITE_ACSL EXTENDED_IDENTIFIER)
    ;
    
/* ACSL MPI-extension keywords */

mpi_key
    : {input.LT(1).getText().equals("mpi")}? IDENTIFIER
    ;

uses_key
    : {input.LT(1).getText().equals("uses")}? IDENTIFIER
    ;

collective_key
    : {input.LT(1).getText().equals("collective")}? IDENTIFIER
    ;

mpiagree_key
    : {input.LT(1).getText().equals("\\mpi_agree")}? EXTENDED_IDENTIFIER
    ;

mpibackmessages_key
	: {input.LT(1).getText().equals("\\mpi_back_messages")}? EXTENDED_IDENTIFIER
        -> ^(MPI_BACK_MESSAGES EXTENDED_IDENTIFIER)
	;

mpibuf_key
    : {input.LT(1).getText().equals("\\mpi_buf")}? EXTENDED_IDENTIFIER
    ;

mpicommsize_key
    : {input.LT(1).getText().equals("\\mpi_comm_size")}? EXTENDED_IDENTIFIER
    -> ^(MPI_COMM_SIZE EXTENDED_IDENTIFIER)
    ;

mpicommrank_key
    : {input.LT(1).getText().equals("\\mpi_comm_rank")}? EXTENDED_IDENTIFIER
    -> ^(MPI_COMM_RANK EXTENDED_IDENTIFIER)
    ;

mpiextent_key
    : {input.LT(1).getText().equals("\\mpi_extent")}? EXTENDED_IDENTIFIER
    ;

mpinonoverlapping_key
    : {input.LT(1).getText().equals("\\mpi_nonoverlapping")}? EXTENDED_IDENTIFIER
    ;

mpion_key
    : {input.LT(1).getText().equals("\\mpi_on")}? EXTENDED_IDENTIFIER
    ;

mpisig_key
    : {input.LT(1).getText().equals("\\mpi_sig")}? EXTENDED_IDENTIFIER
    ;

mpireduce_key
	: {input.LT(1).getText().equals("\\mpi_reduce")}? EXTENDED_IDENTIFIER
	;

after_key
        : {input.LT(1).getText().equals("\\after")}? EXTENDED_IDENTIFIER
	;

until_key
        : {input.LT(1).getText().equals("\\until")}? EXTENDED_IDENTIFIER
	;

/** ACSL higher-order keywords */
lambda_key
	: {input.LT(1).getText().equals("\\lambda")}? EXTENDED_IDENTIFIER
	;
	
sum_key
	: {input.LT(1).getText().equals("\\sum")}? EXTENDED_IDENTIFIER
	;

max_key
	: {input.LT(1).getText().equals("\\max")}? EXTENDED_IDENTIFIER
	;
	
min_key
	: {input.LT(1).getText().equals("\\min")}? EXTENDED_IDENTIFIER
	;

product_key
	: {input.LT(1).getText().equals("\\product")}? EXTENDED_IDENTIFIER
	;
	
numof_key
	: {input.LT(1).getText().equals("\\numof")}? EXTENDED_IDENTIFIER
	;

