lexer grammar MFortranLexer;

options {
    tokenVocab=MFortranLexer;
}

@header {
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

/**
 *
 * @author Craig E Rasmussen, Christopher D. Rickett, Jeffrey Overbey
 */
 
 
package edu.udel.cis.vsl.abc.front.fortran.preproc;

}

/*
 * Lexer rules
 */

/* 
 * Note: antlr sometimes has LL(*) failures with the grammars, but not always.
 * it seems that it may be the timeout issue that was mentioned..
 */

// Support for language extension points
NO_LANG_EXT
        :   {false}? 'no extension'                         ;   // can't be recognized

EOS     :   ';'
        |   ('\r')? ('\n')                                  ;

/* if this is a fragment, the generated code never seems to execute the
 * action.  the action needs to set the flag so EOS knows whether it should
 * be channel 99'ed or not (ignore EOS if continuation is true, which is the
 * case of the & at the end of a line).
 */
CONTINUE_CHAR
        :   '&'                                             ;

// R427 from char-literal-constant
CHAR_CONST
        :   ('\'' ( ~('\'') )* '\'')+ 
        |   ('\"' ( ~('\"')  )* '\"')+                   ;

DIGIT_STR
        :   Digit+                                          ;

// R412
BIN_CONST
        :   ('b'|'B') '\'' ('0'..'1')+ '\''
        |   ('b'|'B') '\"' ('0'..'1')+ '\"'                 ;

// R413
OCT_CONST
        :   ('o'|'O') '\'' ('0'..'7')+ '\''
        |   ('o'|'O') '\"' ('0'..'7')+ '\"'                 ;

// R414
HEX_CONST
        :   ('z'|'Z') '\'' (Digit|'a'..'f'|'A'..'F')+ '\''
        |   ('z'|'Z') '\"' (Digit|'a'..'f'|'A'..'F')+ '\"'  ;

WS      :   (' '|'\r'|'\t'|'\u000C')                        ;

/*
 * fragments
 */

// R409 digit_string
//fragment
//Digits      :   Digit+                  ;
//
// R302 alphanumeric_character
//fragment
//Gen_Char    :   Letter | Digit | '_'    ;
//
//fragment
//Rep_Char    :   ~('\'' | '\"')          ;
//
//fragment
//SQ_Rep_Char :   ~('\'')                 ;
//
//fragment
//DQ_Rep_Char :   ~('\"')                 ;

fragment
Sp_Char     :   ' ' .. '/' 
            |   ':' .. '@' 
            |   '[' .. '^' 
            |   '`' 
            |   '{' .. '~'              ;


fragment
Letter      :   ('a'..'z' | 'A'..'Z')   ;

fragment
Digit       :   '0'..'9'                ;


/*
 * from fortran03_lexer.g
 */

ASTERISK          :   '*'         ;
COLON             :   ':'         ;
COLON_COLON       :   '::'        ;
COMMA             :   ','         ;
EQUALS            :   '='         ;
EQ_EQ             :   '=='        ;
EQ_GT             :   '=>'        ;
GREATERTHAN       :   '>'         ;
GREATERTHAN_EQ    :   '>='        ;
LESSTHAN          :   '<'         ;
LESSTHAN_EQ       :   '<='        ;
LBRACKET          :   '['         ;
LPAREN            :   '('         ;
MINUS             :   '-'         ;
PERCENT           :   '%'         ;
PLUS              :   '+'         ;
POWER             :   '**'        ;
SLASH             :   '/'         ;
SLASH_EQ          :   '/='        ;
SLASH_SLASH       :   '//'        ;
RBRACKET          :   ']'         ;
RPAREN            :   ')'         ;
UNDERSCORE        :   '_'         ;
EQ                :   '.EQ.'      ;
NE                :   '.NE.'      ;
LT                :   '.LT.'      ;
LE                :   '.LE.'      ;
GT                :   '.GT.'      ;
GE                :   '.GE.'      ;
TRUE              :   '.TRUE.'    ;
FALSE             :   '.FALSE.'   ;
NOT               :   '.NOT.'     ;
AND               :   '.AND.'     ;
OR                :   '.OR.'      ;
EQV               :   '.EQV.'     ;
NEQV              :   '.NEQV.'    ;
PERIOD            :   '.'          ;
PERIOD_EXPONENT   : '.' ('0'..'9')+ 
                    ('E' | 'e' | 'd' | 'D') 
                    ('+' | '-')? ('0'..'9')+  
                  | '.' ('E' | 'e' | 'd' | 'D') 
                    ('+' | '-')? ('0'..'9')+  
                  | '.' ('0'..'9')+
                  | ('0'..'9')+ 
                    ('e' | 'E' | 'd' | 'D') 
                    ('+' | '-')? ('0'..'9')+  ;

// begin keyword section (all keywords must appear below
//ASSIGN            :   'ASSIGN'            ;  /* DELETED */

INTEGER           :   'INTEGER'           ;
REAL              :   'REAL'              ;
COMPLEX           :   'COMPLEX'           ;
CHARACTER         :   'CHARACTER'         ;
LOGICAL           :   'LOGICAL'           ;
ABSTRACT          :   'ABSTRACT'          ;
ACQUIRED_LOCK     :   'ACQUIRED_LOCK'     ;  /* F2008 */
ALL               :   'ALL'               ;  /* F2008 */
ALLOCATABLE       :   'ALLOCATABLE'       ;
ALLOCATE          :   'ALLOCATE'          ;
ASSIGNMENT        :   'ASSIGNMENT'        ;
ASSOCIATE         :   'ASSOCIATE'         ;
ASYNCHRONOUS      :   'ASYNCHRONOUS'      ;
BACKSPACE         :   'BACKSPACE'         ;
BIND              :   'BIND'              ;
BLOCK             :   'BLOCK'             ;
BLOCKDATA         :   'BLOCKDATA'         ;
CALL              :   'CALL'              ;
CASE              :   'CASE'              ;
CHANGE            :   'CHANGE'            ;
CLASS             :   'CLASS'             ;
CLOSE             :   'CLOSE'             ;
CODIMENSION       :   'CODIMENSION'       ;
COMMON            :   'COMMON'            ;
CONCURRENT        :   'CONCURRENT'        ;
CONTAINS          :   'CONTAINS'          ;
CONTIGUOUS        :   'CONTIGUOUS'        ;
CONTINUE          :   'CONTINUE'          ;
CRITICAL          :   'CRITICAL'          ;
CYCLE             :   'CYCLE'             ;
DATA              :   'DATA'              ;
DEFAULT           :   'DEFAULT'           ;
DEALLOCATE        :   'DEALLOCATE'        ;
DEFERRED          :   'DEFERRED'          ;
DIMENSION         :   'DIMENSION'         ;
DO                :   'DO'                ;
DOUBLE            :   'DOUBLE'            ;
DOUBLEPRECISION   :   'DOUBLEPRECISION'   ;
DOUBLECOMPLEX     :   'DOUBLECOMPLEX'     ;
ELEMENTAL         :   'ELEMENTAL'         ;
ELSE              :   'ELSE'              ;
ELSEIF            :   'ELSEIF'            ;
ELSEWHERE         :   'ELSEWHERE'         ;
ENTRY             :   'ENTRY'             ;
ENUM              :   'ENUM'              ;
ENUMERATOR        :   'ENUMERATOR'        ;
ERRMSG            :   'ERRMSG'            ;
ERROR             :   'ERROR'             ;
EQUIVALENCE       :   'EQUIVALENCE'       ;
EVENT             :   'EVENT'             ;  /* F2018 */
EVENTWAIT         :   'EVENTWAIT'         ;  /* F2018 */
EXIT              :   'EXIT'              ;
EXTENDS           :   'EXTENDS'           ;
EXTERNAL          :   'EXTERNAL'          ;
FAIL              :   'FAIL'              ;
FAILIMAGE         :   'FAILIMAGE'         ;
FILE              :   'FILE'              ;
FINAL             :   'FINAL'             ;
FLUSH             :   'FLUSH'             ;
FORALL            :   'FORALL'            ;
FORM              :   'FORM'              ;
FORMAT            :   'FORMAT'            ;
FORMATTED         :   'FORMATTED'         ;
FORMTEAM          :   'FORMTEAM'          ;
FUNCTION          :   'FUNCTION'          ;
GENERIC           :   'GENERIC'           ;
GO                :   'GO'                ;
GOTO              :   'GOTO'              ;
IF                :   'IF'                ;
IMAGE             :   'IMAGE'             ;
IMAGES            :   'IMAGES'            ;
IMPLICIT          :   'IMPLICIT'          ;
IMPORT            :   'IMPORT'            ;
IMPURE            :   'IMPURE'            ;
IN                :   'IN'                ;
INOUT             :   'INOUT'             ;
INTENT            :   'INTENT'            ;
INTERFACE         :   'INTERFACE'         ;
INTRINSIC         :   'INTRINSIC'         ;
INQUIRE           :   'INQUIRE'           ;
IS                :   'IS'                ;
KIND              :   'KIND'              ;
LEN               :   'LEN'               ;
LOCAL             :   'LOCAL'             ;
LOCAL_INT         :   'LOCAL_INT'         ;
LOCK              :   'LOCK'              ;  /* F2008 */
MEMORY            :   'MEMORY'            ;
MODULE            :   'MODULE'            ;
MOLD              :   'MOLD'              ;
NAMELIST          :   'NAMELIST'          ;
NONE              :   'NONE'              ;
NON_INTRINSIC     :   'NON_INTRINSIC'     ;
NON_OVERRIDABLE   :   'NON_OVERRIDABLE'   ;
NON_RECURSIVE     :   'NON_RECURSIVE'     ;
NOPASS            :   'NOPASS'            ;
NULLIFY           :   'NULLIFY'           ;
ONLY              :   'ONLY'              ;
OPEN              :   'OPEN'              ;
OPERATOR          :   'OPERATOR'          ;
OPTIONAL          :   'OPTIONAL'          ;
OUT               :   'OUT'               ;
PARAMETER         :   'PARAMETER'         ;
PASS              :   'PASS'              ;
PAUSE             :   'PAUSE'             ;
POINTER           :   'POINTER'           ;
POST              :   'POST'              ;
PRINT             :   'PRINT'             ;
PRECISION         :   'PRECISION'         ;
PRIVATE           :   'PRIVATE'           ;
PROCEDURE         :   'PROCEDURE'         ;
PROGRAM           :   'PROGRAM'           ;
PROTECTED         :   'PROTECTED'         ;
PUBLIC            :   'PUBLIC'            ;
PURE              :   'PURE'              ;
QUIET             :   'QUIET'             ;
RANK              :   'RANK'              ;
READ              :   'READ'              ;
RECURSIVE         :   'RECURSIVE'         ;
RESULT            :   'RESULT'            ;
RETURN            :   'RETURN'            ;
REWIND            :   'REWIND'            ;
SAVE              :   'SAVE'              ;
SELECT            :   'SELECT'            ;
SELECTCASE        :   'SELECTCASE'        ;
SELECTTYPE        :   'SELECTTYPE'        ;
SEQUENCE          :   'SEQUENCE'          ;
SHARED		      :   'SHARED'		      ;
STAT              :   'STAT'              ;
STOP              :   'STOP'              ;
SOURCE            :   'SOURCE'            ;
SUBMODULE         :   'SUBMODULE'         ;
SUBROUTINE        :   'SUBROUTINE'        ;
SYNC              :   'SYNC'              ;  /* F2008 */
SYNCTEAM          :   'SYNCTEAM'          ;
TARGET            :   'TARGET'            ;
TEAM              :   'TEAM'              ;  /* F2018 */
THEN              :   'THEN'              ;
TO                :   'TO'                ;
TYPE              :   'TYPE'              ;
UNFORMATTED       :   'UNFORMATTED'       ;
UNLOCK            :   'UNLOCK'            ;  /* F2008 */
USE               :   'USE'               ;
VALUE             :   'VALUE'             ;
VOLATILE          :   'VOLATILE'          ;
WAIT              :   'WAIT'              ;
WHERE             :   'WHERE'             ;
WHILE             :   'WHILE'             ;
WRITE             :   'WRITE'             ;

// these tokens (without blank characters) are from 3.3.2.2
// All keywords commented out are separated as two tokens by converter 
//ENDASSOCIATE      :   'ENDASSOCIATE'      ;
//ENDBLOCK          :   'ENDCOTARGETBLOCK'  ;
//ENDBLOCKDATA      :   'ENDBLOCKDATA'      ;
//ENDCRITICAL       :   'ENDCRITICAL'       ;
//ENDDO             :   'ENDDO'             ;
//ENDENUM           :   'ENDENUM'           ;
//ENDFILE           :   'ENDFILE'           ;
//ENDFORALL         :   'ENDFORALL'         ;
//ENDFUNCTION       :   'ENDFUNCTION'       ;
//ENDIF             :   'ENDIF'             ;
//ENDMODULE         :   'ENDMODULE'         ;
//ENDINTERFACE      :   'ENDINTERFACE'      ;
//ENDPROCEDURE      :   'ENDPROCEDURE'      ;
//ENDPROGRAM        :   'ENDPROGRAM'        ;
//ENDSELECT         :   'ENDSELECT'         ;
//ENDSUBMODULE      :   'ENDSUBMODULE'      ;
//ENDSUBROUTINE     :   'ENDSUBROUTINE'     ;
//ENDTEAM           :   'ENDTEAM'           ;
//ENDTYPE           :   'ENDTYPE'           ;
//ENDWHERE          :   'ENDWHERE'          ;
END               :   'END'               ;

// End keyword section (all keywords must appear above

PREPROCESS_LINE     :   '#' ~('\n'|'\r')*   ;
INCLUDE 
options {k=1;}      :   'INCLUDE'           ;


// Must come after .EQ. (for example) or will get matched first
// TODO:: this may have to be done in the parser w/ a rule such as:
// PERIOD IDENT PERIOD
DEFINED_OP        :   '.' Letter+ '.'    ;


// Note: Hollerith constants were deleted in F77; Hollerith edit descriptors
// deleted in F95.
//
// T_HOLLERITH      :   Digit+ 'H'   ;
//
// used to catch edit descriptors and other situations
// T_ID_OR_OTHER    :   'ID_OR_OTHER'      ;
//
//M_ARITHMETIC_IF_STMT    :   '__ARITHMETIC_IF_STMT__'    ;

STMT_FUNCTION     :   'STMT_FUNCTION'                 ;

// extra, context-sensitive terminals that require communication between parser and scanner
// added the underscores so there is no way this could overlap w/ any valid
// idents in Fortran.  we just need this token to be defined so we can 
// create one of them while we're fixing up labeled do stmts.
M_LBL_DO_TERMINAL       :   '__LABEL_DO_TERMINAL__'     ;
M_DATA_EDIT_DESC        :   '__DATA_EDIT_DESC__'        ;
M_CTRL_EDIT_DESC        :   '__CTRL_EDIT_DESC__'        ;
M_CSTR_EDIT_DESC        :   '__CSTR_EDIT_DESC__'        ;
M_ASSIGNMENT_STMT       :   '__ASSIGNMENT_STMT__'       ;
M_PTR_ASSIGNMENT_STMT   :   '__PTR_ASSIGNMENT_STMT__'   ;
M_ALLOCATE_STMT_1       :   '__ALLOCATE_STMT_1__'       ;
M_WHERE_STMT            :   '__WHERE_STMT__'            ;
M_IF_STMT               :   '__IF_STMT__'               ;
M_FORALL_STMT           :   '__FORALL_STMT__'           ;
M_WHERE_CONSTRUCT_STMT  :   '__WHERE_CONSTRUCT_STMT__'  ;
M_FORALL_CONSTRUCT_STMT :   '__FORALL_CONSTRUCT_STMT__' ;
M_INQUIRE_STMT_2        :   '__INQUIRE_STMT_2__'        ;
// text for the real constant will be set 
// when a token of this type is created by the prepass.
M_REAL_CONST            :   '__REAL_CONST__'            ; 
M_INCLUDE_NAME          :   '__INCLUDE_NAME__'          ;
M_EOF                   :   '__EOF__'                   ;


// R304
IDENT
options {k=1;}      :   Letter ( Letter | Digit | '_' )*  ;


// Used in format-item processing.  This token is replaced by an edit
// descriptor in the prepass (by FortranLexicalPrepass).  It doesn't really
// matter what this token contains because the format string is parsed
// as a string in the lexical prepass.  The goal is to keep the lexer from
// bombing on strings like 2es15.6 and also not interfer with real literal
// constants and Holleriths.

EDIT_DESC_MISC    :   Digit+
                        ( ('e'|'E') (('n'|'N') | ('s'|'S')) )
                        ( Letter | Digit | '_' )*        ;

LINE_COMMENT        :   '!'  ~('\n'|'\r'|'$')*             ;

/* Need a catch-all rule because of fixed-form being allowed to use any 
   character in column 6 to designate a continuation.  */
MISC_CHAR           :   ~('\n' | '\r')                     ;

PRAGMA            :   '!$'                               ;

CIVL_PRIMITIVE	  :   '$' Letter+						;