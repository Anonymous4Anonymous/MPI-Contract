parser grammar MFortranParser2018;

options {
    language=Java;
    superClass=BaseMFortranParser;
    tokenVocab=MFortranLexer;
}

@header {
    package edu.udel.cis.vsl.abc.front.fortran.parse;

    import edu.udel.cis.vsl.abc.token.IF.CivlcToken;
	import edu.udel.cis.vsl.abc.front.fortran.ptree.MFPUtils;
}

@members {
    int gctr0;
    int gctr1;

    public void initialize() {
        MFPA.start_of_file(fileName, pathName);
    }

    public void finalize() {
        MFPA.end_of_file(fileName, pathName);
    }
}

/*
 * ISO IECJTC1 SC22 WG5 N2146 Fortran 2018 Draft
 */

/*
 * Rules omitted:
 * R501 program
 * R502 program_unit
 * R503 external_subprogram
 * 
 *  OFP:  top level rules will be handled by 
 *        an antlr parser instance.
 */
 
/*
 * R501:  program
 * R502:  program unit
 */
 
/*
 * R503:  external subprogram
 *          is  function subprogram
 *          or  subroutine-subprogram
 *
 *  OFP:  a dummy rule provides an entry that 
 *        an antlr Parser instance can call it to handle functions.
 */
ext_function_subprogram
@init{ 
  boolean hasPrefix=false; 
}
@after{
  MFPA.ext_function_subprogram(hasPrefix);
}
    : ( prefix {hasPrefix=true;} )? 
      function_subprogram
    ;

/*
 * R504:  specification part
 *
 *  OFP:  'implicit_part' made non-optional
 *        for fixing ambiguity
 */
specification_part
@init{
  int numUS=0; 
  int numIS=0; 
  gctr0=0;
  gctr1=0; 
}
@after{
  MFPA.specification_part(numUS, numIS, gctr0, gctr1);
}
    : ( use_stmt {numUS++;} )* 
      ( import_stmt {numIS++;} )*
      implicit_part_recursion
      ( declaration_construct {gctr1++;} )*
    ;

/*
 * R505:  implicit part
 * R506:  implicit part stmt
 */
implicit_part_recursion
    : ((label)? IMPLICIT)  => 
          implicit_stmt  {gctr0++;} implicit_part_recursion
    | ((label)? PARAMETER) => 
          parameter_stmt {gctr1++;} implicit_part_recursion
    | ((label)? FORMAT)    => 
          format_stmt    {gctr1++;} implicit_part_recursion
    | ((label)? ENTRY)     => 
          entry_stmt     {gctr1++;} implicit_part_recursion
    | // empty
    ;

/*
 * R507:  declaration construct
 * R508:  specification construct
 * R513:  other specification stmt
 * TODO:
 *        R1510 generic stmt
 */
declaration_construct
@after{
  MFPA.declaration_construct();
}
    : access_stmt
    | allocatable_stmt
    | asynchronous_stmt
    | bind_stmt
    | codimension_stmt
    | common_stmt
    | data_stmt
    | derived_type_def
    | dimension_stmt
    | entry_stmt
    | enum_def
    | equivalence_stmt
    | external_stmt
    | format_stmt
    | intent_stmt
    | interface_block
    | intrinsic_stmt
    | namelist_stmt
    | optional_stmt
    | parameter_stmt
    | pointer_stmt
    | procedure_declaration_stmt
    | protected_stmt
    | save_stmt
    | target_stmt
    | type_declaration_stmt
    | volatile_stmt
    | value_stmt
    | stmt_function_stmt
    | pragma_type_qualifier_stmt
    ;

/*
 * R509:  execution part
 */
execution_part
@init{
  int numExec = 1;
}
@after{
  MFPA.execution_part(numExec);
}
    : executable_construct
      ( execution_part_construct {numExec++;} )*
    ;

/*
 * R510:  execution part construct
 */
execution_part_construct
@after {
  MFPA.execution_part_construct();
}
    : executable_construct
    | data_stmt
    | entry_stmt
    | format_stmt
    ;

/*
 * R511:  internal subprogram part
 */
internal_subprogram_part
@init{
  int numIS = 0;
}
@after{
  MFPA.internal_subprogram_part(numIS);
}
    : contains_stmt
      ( internal_subprogram {numIS++;} )*
    ;
   
/*
 * R512:  internal subprogram
 */
internal_subprogram
options {backtrack=true;}
@after {
  MFPA.internal_subprogram();
}
    : ( prefix )? function_subprogram
    | subroutine_subprogram
    ;

/*
 * R514:  executable construct
 * UDEL:  A dummy rule 'pragma_stmt' is added for 
 *        OpenMP pragma directives 
 *        (xxxx@udel.edu)
 * TODO:
 *        R1111 change team construct
 *        R1148 select rank construct
 */
executable_construct
@after {
  MFPA.executable_construct();
}
    : action_stmt
    | associate_construct
    | block_construct
    | case_construct
    | critical_construct
    | do_construct
    | forall_construct
    | if_construct
    | select_type_construct
    | where_construct
    | pragma_stmt
    ;

/*
 * R515:  action stmt
 */
action_stmt
@after{
    MFPA.action_stmt();
    checkForInclude();
}
    : allocate_stmt
    | assignment_stmt
    | backspace_stmt
    | call_stmt
    | close_stmt
    | computed_goto_stmt
    | continue_stmt
    | cycle_stmt
    | deallocate_stmt
    | endfile_stmt
    | exit_stmt
    | flush_stmt
    | forall_stmt
    | goto_stmt
    | if_stmt
    | inquire_stmt
    | nullify_stmt
    | open_stmt
    | pointer_assignment_stmt
    | print_stmt
    | read_stmt
    | return_stmt
    | rewind_stmt
    | stop_stmt
    | sync_all_stmt
    | sync_images_stmt
    | sync_memory_stmt
    | wait_stmt
    | where_stmt
    | write_stmt
// New Features added in F2018
//    | lock_stmt
//    | unlock_stmt
//    | fail_image_stmt
//    | sync_team_stmt
//    | event_post_stmt
//    | event_wait_stmt
//    | form_team_stmt
// Deleted features ()
//    | arithmetic_if_stmt
//    | assign_stmt 
//    | assigned_goto_stmt
//    | errorstop_stmt
//    | pause_stmt
    ;

/*
 * R516:  keyword
 */
keyword returns [Token t]
@after{
  MFPA.keyword();
}
    : name {t = $name.t;}
    ;

/*
 * Rules omitted:
 * R601:  alphanumeric character 
 * R602:  underscore
 */

/*
 * R603:  name
 */
name returns [Token t]
@after{
  MFPA.name(t);
}
    : IDENT { t = $IDENT; }
    ;

/*
 * R604:  constant
 * R606:  named constant
 *  OFP:  named_constant replaced by IDENT
 */
constant
@after{
  MFPA.constant(t);
}
    : literal_constant
    | t=IDENT
    ;

/*
 * R605:  literal constant
 */
literal_constant
@after {
  MFPA.literal_constant();
}
    : int_literal_constant
    | real_literal_constant
    | complex_literal_constant
    | logical_literal_constant
    | char_literal_constant
    | boz_literal_constant
// Deleted Features (since F77)
//  | hollerith_literal_constant
    ;

/*
 * R607:  int constant
 */
int_constant
@after{
  MFPA.int_constant(c);
}
    : int_literal_constant
    | c=IDENT
    ;

/*
 * R608:  intrinsic operator
 */
intrinsic_operator returns [Token t]
@after{
  MFPA.intrinsic_operator();
}
    : power_op  { t = $power_op.t; }
    | mult_op   { t = $mult_op.t; }
    | add_op    { t = $add_op.t; }
    | concat_op { t = $concat_op.t; }
    | rel_op    { t = $rel_op.t; }
    | not_op    { t = $not_op.t; }
    | and_op    { t = $and_op.t; }
    | or_op     { t = $or_op.t; }
    | equiv_op  { t = $equiv_op.t; }
    ;

/*
 * R609 defined operator
 */
defined_operator
    : DEFINED_OP
        { MFPA.defined_operator($DEFINED_OP, false); }
    | extended_intrinsic_op
        { MFPA.defined_operator($extended_intrinsic_op.t, true); }
    ;

/*
 * R610:  extended intrinsic op
 */
extended_intrinsic_op returns [Token t]
@after {
  MFPA.extended_intrinsic_op();
}
    : intrinsic_operator { t = $intrinsic_operator.t; }
    ;

/*
 * R611:  label
 */
label returns [Token t]
    : DIGIT_STR { t = $DIGIT_STR; }
    ;

// MFPA.label called here to store label in action class
label_list
@init{
  int numLbl = 1;
}
@after{
  MFPA.label_list(numLbl);
}
    : lbl=label {  MFPA.label(lbl); } 
      ( COMMA lbl=label 
                  {  MFPA.label(lbl);numLbl++; } )*
    ;

/*
 * R690:  char constant
 * UDEL:  a DELETED feature
 */
 char_constant
    : char_literal_constant { MFPA.int_constant(null); }
    | IDENT { MFPA.int_constant($IDENT); }
    ;

/*
 * R701:  type param value
 */
type_param_value
    : expr 
        { MFPA.type_param_value(MFPUtils.TPV.EXPR); }
    | ASTERISK 
        { MFPA.type_param_value(MFPUtils.TPV.ASTERISK); }
    | COLON 
        { MFPA.type_param_value(MFPUtils.TPV.COLON); }
    ;

/*
 * R702:  type spec
 */
type_spec
@after {
  MFPA.type_spec();
}
    : intrinsic_type_spec
    | derived_type_spec
    ;

/*
 * R703:  declaration type spec
 */
declaration_type_spec
    : intrinsic_type_spec
        { MFPA.declaration_type_spec( null, null, 
            MFPUtils.F_INTRNSIC); }
    | TYPE  LPAREN intrinsic_type_spec RPAREN
        { MFPA.declaration_type_spec( $TYPE, null, 
            MFPUtils.TYPE_INTRN); }
    | TYPE  LPAREN derived_type_spec RPAREN
        { MFPA.declaration_type_spec( $TYPE, null, 
            MFPUtils.TYPE_DERIV); }
    | TYPE  LPAREN ASTERISK RPAREN
        { MFPA.declaration_type_spec( $TYPE, $ASTERISK,
            MFPUtils.TYPE_UNLMT); }
    | CLASS LPAREN derived_type_spec RPAREN
        { MFPA.declaration_type_spec( $CLASS, null, 
            MFPUtils.CLSS_DERIV); }
    | CLASS LPAREN ASTERISK RPAREN
        { MFPA.declaration_type_spec( $CLASS, $ASTERISK,
            MFPUtils.CLSS_UNLMT); }
    ;

/*
 * R704:  intrinsic type spec
 * R705:  integer type spec
 *  OFP:  Non-standard extionsion from BLAS
 *        'DOUBLE COMPLEX' and 'DOUBLECOMPLEX'
 */
intrinsic_type_spec
@init{
  boolean hasKS = false;
}
    : INTEGER (kind_selector {hasKS = true;})?
        { MFPA.intrinsic_type_spec(
            $INTEGER, null, 
            MFPUtils.TYPE_INT,  hasKS);}
    | REAL (kind_selector {hasKS = true;})?
        { MFPA.intrinsic_type_spec(
            $REAL, null, 
            MFPUtils.TYPE_REAL, hasKS);}
    | DOUBLE PRECISION
        { MFPA.intrinsic_type_spec(
            $DOUBLE, $PRECISION,
            MFPUtils.TYPE_DBL,  false);}
    | DOUBLEPRECISION
        { MFPA.intrinsic_type_spec(
            $DOUBLEPRECISION, null, 
            MFPUtils.TYPE_DBL,  false);}
    | COMPLEX (kind_selector {hasKS = true;})?
        { MFPA.intrinsic_type_spec(
            $COMPLEX, null,
            MFPUtils.TYPE_CPLX, hasKS);}
    | DOUBLE COMPLEX
        { MFPA.intrinsic_type_spec( 
            $DOUBLE, $COMPLEX, 
            MFPUtils.TYPE_DCPLX, false);}
    | DOUBLECOMPLEX
        { MFPA.intrinsic_type_spec(
            $DOUBLECOMPLEX, null, 
            MFPUtils.TYPE_DCPLX, false);}
    | CHARACTER (char_selector {hasKS = true;})?
        { MFPA.intrinsic_type_spec(
            $CHARACTER, null, 
            MFPUtils.TYPE_CHAR, hasKS);}
    | LOGICAL (kind_selector {hasKS = true;})?
        { MFPA.intrinsic_type_spec(
            $LOGICAL, null, 
            MFPUtils.TYPE_BOOL, hasKS);}
    ;

/*
 * R706:  kind selector
 */
kind_selector
@init{
  boolean hasKSExpr = false;
}
@after{
  MFPA.kind_selector(tk0, tk1, hasKSExpr);
}
    : LPAREN (tk0=KIND tk1=EQUALS)? expr RPAREN
        {hasKSExpr = true;}
    | tk0=ASTERISK tk1=DIGIT_STR	
    ;

/*
 * R707:  signed int literal constant
 * R710:  signed digit string
 * R712:  sign
 */
signed_int_literal_constant
@after{
  MFPA.signed_int_literal_constant(sign);
}
    : (sign=PLUS | sign=MINUS)?
      int_literal_constant
    ;

/*
 * R708:  int literal constant
 */
int_literal_constant
    : DIGIT_STR (UNDERSCORE kind=kind_param)?
        { MFPA.int_literal_constant($DIGIT_STR, kind); }
    ;

/*
 * R709:  kind param
 *  OFP:  IDENT inlined for scalar_int_constant_name
 */
kind_param returns [Token t]
@after{
  MFPA.kind_param(t);
}
    : DIGIT_STR { t = $DIGIT_STR; }
    | IDENT { t = $IDENT; }
    ;

/*
 * R711:  digit string
 *  OFP:  Used as a fragment in OFP FortranLexer
 * CIVL:  Converted from PP-tokens output by a C Preprocessor.
 */

/*
 * R713:  signed real literal constant
 */
signed_real_literal_constant
@after{
  MFPA.signed_real_literal_constant(sign);
} 
    : (sign=PLUS | sign=MINUS)?
      real_literal_constant
    ;

/*
 * R714:  real literal constant
 *  OFP:  Used as a terminal.
 *        Modified for handling a case like:
 *            ' if (1.and.1) then ...'
 *        Must be parsed in action implementation,
 *        so that exponent letter 'D'/'E' can be processed.
 */
real_literal_constant
    : M_REAL_CONST (UNDERSCORE kind=kind_param)? 
        { MFPA.real_literal_constant($M_REAL_CONST, kind); }
    ;

/*
 * R715:  significand
 *  OFP:  Used as a fragment in OFP FortranLexer
 * R716:  exponent letter
 * R717:  exponent
 *  OFP:  Inlined in the terminal involving it.
 * R715 -- 717:
 * CIVL:  Converted from PP-tokens output by a C Preprocessor. 
 */

/*
 * R718:  complex literal constant
 */
complex_literal_constant
@after {
    MFPA.complex_literal_constant();
}
    : LPAREN real_part COMMA imag_part RPAREN
    ;

/*
 * R719:  real part
 *  OFP:  'named_constant' replaced by IDENT
 */
real_part
    : signed_int_literal_constant
        { MFPA.real_part(null, 
            MFPUtils.CPLXP.INT); }
    | signed_real_literal_constant 
        { MFPA.real_part(null, 
            MFPUtils.CPLXP.REAL); }
    | IDENT
        { MFPA.real_part($IDENT, 
            MFPUtils.CPLXP.IDENT); }
    ;

/*
 * R720:  imag part
 *  OFP:  'named_constant' replaced by IDENT
 */
imag_part
    : signed_int_literal_constant
        { MFPA.imag_part(null, 
            MFPUtils.CPLXP.INT); }
    | signed_real_literal_constant
        { MFPA.imag_part(null, 
            MFPUtils.CPLXP.REAL); }
    | IDENT
        { MFPA.imag_part($IDENT, 
            MFPUtils.CPLXP.IDENT); }
    ;

/*
 * R721:  char selector
 *  OFP:  scalar_int_initialization_expr replaced by expr
 *        KIND, if type_param_value, must be a scalar_int_initialization_expr
 *        KIND and LEN cannot both be specified
 */
char_selector
@init {
  MFPUtils.CS pos0 = MFPUtils.CS.NONE;
  MFPUtils.CS pos1 = MFPUtils.CS.NONE;
}
@after{
  MFPA.char_selector(len, kind, pos0, pos1);
}
    : ASTERISK char_length (COMMA)?
        { pos0 = MFPUtils.CS.CHAR; }
    | LPAREN type_param_value
      ( COMMA (kind=KIND EQUALS)? expr
          { pos1 = MFPUtils.CS.KIND; }
      )?
      RPAREN
        { pos0 = MFPUtils.CS.TYPE; }
    | LPAREN len=LEN EQUALS type_param_value
      ( COMMA kind=KIND EQUALS expr
          { pos1 = MFPUtils.CS.KIND;}
      )?
      RPAREN
        { pos0 = MFPUtils.CS.TYPE;}
    | LPAREN kind=KIND EQUALS expr
      ( COMMA (len=LEN EQUALS)? type_param_value
          { pos1 = MFPUtils.CS.TYPE;}
      )?
      RPAREN
        { pos0 = MFPUtils.CS.KIND; }
    ;

/*
 * R722:  length selector
 */
length_selector
    : LPAREN ( len=LEN EQUALS )? type_param_value RPAREN
        { MFPA.length_selector(len, MFPUtils.CS.TYPE); }
    | ASTERISK char_length (COMMA)?
        { MFPA.length_selector(len, MFPUtils.CS.CHAR); }
    ; 
   
/*
 * R723:  char length
 */
char_length
    : LPAREN type_param_value RPAREN { MFPA.char_length(true); }
    | int_literal_constant { MFPA.char_length(false); }
    ;

/*
 * R724:  char literal constant
 *  OFP:  UNDERSCORE is removed because it will be 
 *        a part of the identifier token.
 */
char_literal_constant
    : DIGIT_STR UNDERSCORE CHAR_CONST
        { MFPA.char_literal_constant($DIGIT_STR, $CHAR_CONST); }
    | IDENT CHAR_CONST
        { MFPA.char_literal_constant($IDENT, $CHAR_CONST); }
    | CHAR_CONST
        { MFPA.char_literal_constant(null, $CHAR_CONST); }
    ;

/*
 * R725:  logical literal constant
 */
logical_literal_constant
    : TRUE  ( UNDERSCORE kind=kind_param)?
        { MFPA.logical_literal_constant($TRUE, kind);}
    | FALSE ( UNDERSCORE kind=kind_param)?
        { MFPA.logical_literal_constant($FALSE, kind);}
    ;

/*
 * R726:  derived type def
 *  OFP: ( component_part )? inlined as ( component_def_stmt )*
 * TODO: Incompleted 
 */
derived_type_def
@after{
  MFPA.derived_type_def();
}
    : derived_type_stmt
//  OFP: matches INTEGER possibilities in component_def_stmt
      ( type_param_or_comp_def_stmt_list )?
      ( private_or_sequence )*
          { /* OFP:
             * if private_or_sequence present, component_def_stmt in 
             * type_param_or_comp_def_stmt_list is an error
             */
          }
      ( component_def_stmt )*
      ( type_bound_procedure_part )?
      end_type_stmt
    ;

/*
 * R727:  derived type stmt
 *  OFP:  generic_name_list substituted for type_param_name_list
 */
derived_type_stmt
@init {
  boolean hasASList = false; 
  boolean hasPNList = false;
}
@after{
  checkForInclude();
}
    : (lbl=label)?  TYPE
      ( ( COMMA type_attr_spec_list {hasASList=true;} )? COLON_COLON )? 
      IDENT
      ( LPAREN generic_name_list RPAREN {hasPNList=true;} )?
      end_of_stmt
        { MFPA.derived_type_stmt(
              lbl, $TYPE, $IDENT, $end_of_stmt.t,
              hasASList, hasPNList);
        }
    ;

generic_name_list
@init{
  int numGN = 1;
}
@after{
  MFPA.generic_name_list(numGN);
}
    : ident=IDENT
        { MFPA.generic_name(ident); } 
      ( COMMA ident=IDENT 
          { numGN++;
            MFPA.generic_name(ident);
          } 
      )*
    ;

/*
 * R728:  type attr spec
 *  OFP:  IDENT inlined for parent_type_name
 */
type_attr_spec
    : access_spec
        { MFPA.type_attr_spec(null, null, 
            MFPUtils.ATTR_ACCESS);}
    | EXTENDS LPAREN IDENT RPAREN
        {MFPA.type_attr_spec($EXTENDS, $IDENT,
            MFPUtils.ATTR_EXTENDS);}
    | ABSTRACT
        {MFPA.type_attr_spec($ABSTRACT, null,
            MFPUtils.ATTR_ABSTRACT);} 
//    BIND (C)
    | BIND LPAREN IDENT RPAREN
        {MFPA.type_attr_spec($BIND, $IDENT, 
            MFPUtils.ATTR_BIND_C);}
    ;

type_attr_spec_list
@init{
  int numTAS = 1;
}
@after{
  MFPA.type_attr_spec_list(numTAS);
}
    : type_attr_spec ( COMMA type_attr_spec {numTAS++;} )*
    ;

/*
 * R729:  private or sequence
 */
private_or_sequence
@after {
  MFPA.private_or_sequence();
}
    : private_components_stmt
    | sequence_stmt
    ;

/*
 * R730:  end type stmt
 */
end_type_stmt
@after{
  checkForInclude();
}
    : (lbl=label)?  END TYPE 
      (id=IDENT)? end_of_stmt
        { MFPA.end_type_stmt(lbl, $END, $TYPE, id, $end_of_stmt.t);}
    ;

/*
 * R731:  sequence stmt
 */
sequence_stmt
@after{
  checkForInclude();
}
    : (lbl=label)?  SEQUENCE end_of_stmt
        { MFPA.sequence_stmt(lbl, $SEQUENCE, $end_of_stmt.t);}
    ;

/*
 * R732:  type param def stmt
 * R736:  component def stmt
 *  OFP:  type_param_def_stmt(s) must precede component_def_stmt(s)
 * TODO:  Test whether this is reachable and 
 *        type_param_attr_spec is tokenized KIND or LEN. (R435,440-F08)
 */
type_param_or_comp_def_stmt
    : type_param_attr_spec COLON_COLON type_param_decl_list end_of_stmt 
      { MFPA.type_param_or_comp_def_stmt( $end_of_stmt.t,
            MFPUtils.TPD_OR_CD.TYPE_PARAM_DEF);}
    | component_attr_spec_list COLON_COLON component_decl_list end_of_stmt 
      { MFPA.type_param_or_comp_def_stmt( $end_of_stmt.t,
            MFPUtils.TPD_OR_CD.COMP_DEF);}
    ;

type_param_or_comp_def_stmt_list
@init{
  int numTPCD = 1;
}
@after{
  MFPA.type_param_or_comp_def_stmt_list();
}
    : type_param_or_comp_def_stmt ( type_param_or_comp_def_stmt {numTPCD++;} )*
    ;

/*
 * R733:  type param decl
 *  OFP:  scalar_int_initialization_expr replaced by expr
 *        IDENT inlined for type_param_name
 */
type_param_decl
@init{
  boolean hasInit=false;
}
    : IDENT ( EQUALS expr {hasInit=true;} )?
      { MFPA.type_param_decl($IDENT, hasInit); }
    ;

type_param_decl_list
@init{
  int numTPD = 1;
}
@after{
  MFPA.type_param_decl_list(numTPD);
}
    : type_param_decl ( COMMA type_param_decl {numTPD++;} )*
    ;

/*
 * R734:  type param attr spec
 */
type_param_attr_spec
    : KIND 
        { MFPA.type_param_attr_spec($KIND, 
            MFPUtils.ATTR_KIND); }
    | LEN  
        { MFPA.type_param_attr_spec($LEN,  
            MFPUtils.ATTR_LEN); }
    ;

/*
 * R735:  component part
 *  OFP:  inlined as ( component_def_stmt )* in R726
 */

/*
 * R736:  component def stmt
 */
component_def_stmt
@after{
  checkForInclude();
}
    : data_component_def_stmt
    | proc_component_def_stmt
    ;

/*
 * R737:  data component def stmt
 */
data_component_def_stmt
@init {
  boolean hasSpec=false; 
}
@after{
  checkForInclude();
}
    : (lbl=label)?  
      declaration_type_spec 
      ( ( COMMA component_attr_spec_list {hasSpec=true;} )? 
        COLON_COLON 
      )? component_decl_list end_of_stmt
       { MFPA.data_component_def_stmt(lbl, $end_of_stmt.t, hasSpec); }
    ;

/*
 * R738:  component attr spec
 *  OFP:  component_attr_spec_extension
 */
component_attr_spec
    : access_spec
        { MFPA.component_attr_spec(null, 
            MFPUtils.ATTR_ACCESS);}
    | ALLOCATABLE
        { MFPA.component_attr_spec($ALLOCATABLE, 
            MFPUtils.ATTR_ALLOCATABLE);}
    | CODIMENSION LBRACKET coarray_spec RBRACKET
        {MFPA.component_attr_spec($CODIMENSION, 
            MFPUtils.ATTR_CODIMENSION);}
    | CONTIGUOUS
        {MFPA.component_attr_spec($CONTIGUOUS, 
            MFPUtils.ATTR_CONTIGUOUS);}
    | DIMENSION LPAREN component_array_spec RPAREN
        {MFPA.component_attr_spec($DIMENSION, 
            MFPUtils.ATTR_DIMENSION);}
    | POINTER
        {MFPA.component_attr_spec($POINTER, 
            MFPUtils.ATTR_POINTER);}
    | component_attr_spec_extension
    ;

component_attr_spec_extension
    : NO_LANG_EXT
    ;

component_attr_spec_list
@init{
  int numCAS = 1;
}
@after{
  MFPA.component_attr_spec_list(numCAS);
}
    : component_attr_spec ( COMMA component_attr_spec {numCAS++;} )*
    ;

/*
 * R739:  component decl
 */
component_decl
@init { 
  boolean hasCAS = false; 
  boolean hasCS = false;
  boolean hasCL = false;
  boolean hasCI = false;
}
    : IDENT 
      (LPAREN component_array_spec RPAREN {hasCAS=true;})?
      (LBRACKET coarray_spec RBRACKET {hasCS=true;})?
      (ASTERISK char_length {hasCL=true;})?
      (component_initialization {hasCI =true;})?
        { MFPA.component_decl($IDENT, 
              hasCAS, hasCS, hasCL, hasCI);}
    ;
   
component_decl_list
@init{
  int numCD = 1;
}
@after{
  MFPA.component_decl_list(numCD);
}
    : component_decl ( COMMA component_decl {numCD++;} )*
    ;

/*
 * R740:  component array spec
 */
component_array_spec
    : explicit_shape_spec_list
        { MFPA.component_array_spec(true);}
    | deferred_shape_spec_list
        { MFPA.component_array_spec(false);}
    ;

/*
 * R741:  proc component def stmt
 */
proc_component_def_stmt
@init{
  boolean hasItf = false;
}
@after{
  checkForInclude();
}
    : (lbl=label)?  
      PROCEDURE LPAREN ( proc_interface {hasItf=true;})? RPAREN COMMA
      proc_component_attr_spec_list COLON_COLON proc_decl_list end_of_stmt
        { MFPA.proc_component_def_stmt(lbl, $PROCEDURE, $end_of_stmt.t, hasItf);}
    ;

/*
 * R742:  proc component attr spec
 */
proc_component_attr_spec
    : POINTER
        {MFPA.proc_component_attr_spec($POINTER, id, 
            MFPUtils.ATTR_POINTER);}
    | PASS ( LPAREN id=IDENT RPAREN )?
        {MFPA.proc_component_attr_spec($PASS, id, 
            MFPUtils.ATTR_PASS);}
    | NOPASS
        {MFPA.proc_component_attr_spec($NOPASS, id, 
            MFPUtils.ATTR_NOPASS);}
    | access_spec
        {MFPA.proc_component_attr_spec(null, id, 
            MFPUtils.ATTR_ACCESS);}
    ;

proc_component_attr_spec_list
@init{
  int numCAS = 1;
}
@after{
  MFPA.proc_component_attr_spec_list(numCAS);
}
    : proc_component_attr_spec 
      ( COMMA proc_component_attr_spec {numCAS++;})*
    ;


/*
 * R743:  component initialization
 * R744:  initial data target
 *  OFP:  R447-F2008 can also be => initial_data_target, 
 *        (see NOTE 4.40 in J3/07-007)
 *        initialization_expr replaced by expr
 */
component_initialization
@after {
  MFPA.component_initialization();
}
    : EQUALS expr
    | EQ_GT null_init
    ;

/*
 * R745:  private components stmt
 */
private_components_stmt
@init {
}
@after{
  checkForInclude();
}
    : (lbl=label)?  
      PRIVATE end_of_stmt
        { MFPA.private_components_stmt(lbl, $PRIVATE, $end_of_stmt.t);}
    ;

/*
 * R746:  type bound procedure part
 */
type_bound_procedure_part
@init{
  int numTBPB = 1; 
  boolean hasBPS = false;
}
    : contains_stmt ( binding_private_stmt {hasBPS=true;})? 
      type_bound_proc_binding ( type_bound_proc_binding {numTBPB++;})*
            { MFPA.type_bound_procedure_part(numTBPB, hasBPS); }
    ;

/*
 * R747:  binding private stmt
 */
binding_private_stmt
@init{
}
@after{
  checkForInclude();
}
    : (lbl=label)?  
      PRIVATE end_of_stmt
        { MFPA.binding_private_stmt(lbl, $PRIVATE, $end_of_stmt.t);}
    ;

/*
 * R748:  type bound proc binding
 */
type_bound_proc_binding
@init{
}
@after{
  checkForInclude();
}
    : (lbl=label)?  type_bound_procedure_stmt end_of_stmt
        {MFPA.type_bound_proc_binding(lbl, 
           MFPUtils.TBPB.PROCEDURE, $end_of_stmt.t);}
    | (lbl=label)?  type_bound_generic_stmt end_of_stmt
        {MFPA.type_bound_proc_binding(lbl,
           MFPUtils.TBPB.GENERIC, $end_of_stmt.t);}
    | (lbl=label)?  final_procedure_stmt end_of_stmt
        {MFPA.type_bound_proc_binding(lbl,
           MFPUtils.TBPB.FINAL, $end_of_stmt.t);}
    ;

/*
 * R749:  type bound procedure stmt
 * CIVL:  type_bound_proc_decl_list substituted for binding_name_list
 *        If tIN is not null, then both hasBAL and hasCC are required to be true
 */
type_bound_procedure_stmt
@init{
  boolean hasBAL = false;
  boolean hasCC = false;
  boolean hasPN = false;
}
@after{
  MFPA.type_bound_procedure_stmt(
      proc, tIN, hasBAL, hasCC);
}
    : proc=PROCEDURE
      ( LPAREN tIN=IDENT RPAREN)?
      ( (COMMA binding_attr_list {hasBAL=true;})? 
        COLON_COLON {hasCC=true;}
      )?
      type_bound_proc_decl_list 
//  | proc=PROCEDURE
//    ( LPAREN tIN=IDENT RPAREN)?
//    COMMA binding_attr_list COLON_COLON {hasBAL=true; hasCC=true;}
//    type_bound_proc_decl_list
    ;

/*
 * R750:  type bound proc decl
 */
type_bound_proc_decl
@after{
  MFPA.type_bound_proc_decl(tBN, tPN);
}
    : tBN=IDENT
      ( EQ_GT tPN=IDENT)?
    ;
    
type_bound_proc_decl_list
@init{
  int numPD = 1;
}
@after{
  MFPA.type_bound_proc_decl_list(numPD);
}
    : type_bound_proc_decl 
      (COMMA type_bound_proc_decl {numPD++;})*
    ;

/*
 * R751:  type bound generic stmt
 *  OFP:  generic_name_list substituted for binding_name_list
 */
type_bound_generic_stmt
@init{
  boolean hasAS = false;
}
    : GENERIC ( COMMA access_spec {hasAS=true;})?
      COLON_COLON generic_spec EQ_GT generic_name_list
        { MFPA.type_bound_generic_stmt($GENERIC, hasAS);}
    ;

/*
 * R752:  binding attr
 *  OFP:  IDENT inlined for arg_name
 */
binding_attr
@init{
  Token id = null;
}
    : PASS ( LPAREN IDENT RPAREN {id=$IDENT;})?
        { MFPA.binding_attr($PASS, 
            MFPUtils.ATTR_PASS, id); }
    | NOPASS
        { MFPA.binding_attr($NOPASS, 
            MFPUtils.ATTR_NOPASS, id); }
    | NON_OVERRIDABLE
        { MFPA.binding_attr($NON_OVERRIDABLE, 
            MFPUtils.ATTR_NON_OVERRIDABLE, id); }
    | DEFERRED
        { MFPA.binding_attr($DEFERRED, 
            MFPUtils.ATTR_DEFERRED, id); }
    | access_spec
        { MFPA.binding_attr(null, 
            MFPUtils.ATTR_ACCESS, id); }
    ;

binding_attr_list
@init{
  int numBA = 1;
}
@after{
  MFPA.binding_attr_list(numBA);
}
    : binding_attr 
      ( COMMA binding_attr {numBA++;} )*
    ;

/*
 * R753:  final procedure stmt
 *  OFP:  generic_name_list substituted for final_subroutine_name_list
 */
final_procedure_stmt
    : FINAL ( COLON_COLON )? generic_name_list 
        { MFPA.final_procedure_stmt($FINAL); }
    ;

/*
 * R754:  derived type spec
 */
derived_type_spec
@init{
  boolean hasList = false;
}
    : IDENT ( LPAREN type_param_spec_list {hasList=true;} RPAREN )?
        { MFPA.derived_type_spec($IDENT, hasList); }
    ;

/*
 * R755:  type param spec
 */
type_param_spec
@init{
  Token keyWord=null;
}
    : ( keyword EQUALS {keyWord=$keyword.t;})? type_param_value
        { MFPA.type_param_spec(keyWord);}
    ;

type_param_spec_list
@init{
  int numTPS = 1;
}
@after{
  MFPA.type_param_spec_list(numTPS);
}
    : type_param_spec 
      ( COMMA type_param_spec {numTPS++;})*
    ;

/*
 * R756:  structure constructor
 *  OFP:  inlined derived_type_spec (R662) to remove ambiguity using backtracking
 *        If any of the type-param-specs in the list are an '*' or ':', the 
 *        component-spec-list is required.
 *        the second alternative to the original rule for structure_constructor is
 *        a subset of the first alternative because component_spec_list is a 
 *        subset of type_param_spec_list.  by combining these two alternatives we can
 *        remove the backtracking on this rule.
 */
structure_constructor
    : IDENT LPAREN type_param_spec_list RPAREN
      (LPAREN ( component_spec_list )? RPAREN)?
        { MFPA.structure_constructor($IDENT); }
    ;

/*
 * R757:  component spec
 */
component_spec
@init{
  Token keyWord = null;
}
    : ( keyword EQUALS { keyWord=$keyword.t; })? component_data_source
        { MFPA.component_spec(keyWord); }
    ;

component_spec_list
@init{
  int numCS = 1;
}
@after{
  MFPA.component_spec_list(numCS);
}
    : component_spec 
      ( COMMA component_spec {numCS++;})*
    ;

/*
 * R758:  component data source
 *  OFP:  All 'expr', 'data-target' and 'proc_target'
 *        are 'expr', so they are regarded as 'expr'
 */
component_data_source
    : expr { MFPA.component_data_source(); }
    ;

/*
 * R759:  enum def
 */
enum_def
@init{
 int numEDS = 1;
}
@after{
  MFPA.enum_def(numEDS);
}
    : enum_def_stmt 
      enumerator_def_stmt
      ( enumerator_def_stmt {numEDS++;} )*
      end_enum_stmt
    ;

/*
 * R760:  enum def stmt
 */
enum_def_stmt
@init {
}
@after{
  checkForInclude();
}
    : (lbl=label)?  
      // ENUM , BIND ( C )
      ENUM COMMA BIND LPAREN IDENT RPAREN end_of_stmt
        { MFPA.enum_def_stmt(
            lbl, $ENUM, $BIND, $IDENT, $end_of_stmt.t);}
    ;

/*
 * R761:  enumerator def stmt
 */
enumerator_def_stmt
@init {
}
@after{
  checkForInclude();
}
    : (lbl=label)?  
      ENUMERATOR ( COLON_COLON )? enumerator_list end_of_stmt
        { MFPA.enumerator_def_stmt(
            lbl, $ENUMERATOR, $end_of_stmt.t); }
    ;

/*
 * R762:  enumerator
 *  OFP:  scalar_int_initialization_expr replaced by expr
 *        named_constant replaced by IDENT
 */
enumerator
@init{
  boolean hasExpr = false;
}
    : IDENT ( EQUALS expr { hasExpr = true; })?
      { MFPA.enumerator($IDENT, hasExpr); }
    ;

enumerator_list
@init{
  int numE = 1;
}
@after{
  MFPA.enumerator_list(numE);
} 
    : enumerator
      ( COMMA enumerator {numE++;})*
    ;

/*
 * R763:  end enum stmt
 */
end_enum_stmt
@init{
}
@after{
  checkForInclude();
}
    : (lbl=label)?  
      END ENUM end_of_stmt 
      { MFPA.end_enum_stmt(lbl, $END, $ENUM, $end_of_stmt.t); }
    ;

/*
 * R764:  boz literal constant (bin,oct and hex)
 */
boz_literal_constant
    : BIN_CONST { MFPA.boz_literal_constant($BIN_CONST); }
    | OCT_CONST  { MFPA.boz_literal_constant($OCT_CONST); }
    | HEX_CONST    { MFPA.boz_literal_constant($HEX_CONST); }
    ;

/*
 * R765:  binary constant
 * R766:  octal constant
 * R767:  hex constant
 * R768:  hex digit
 *  OFP:  Used as a fragment in OFP FortranLexer
 * CIVL:  Converted from PP-tokens output by a C Preprocessor.
 */

/*
 * R769:  array constructor
 */
array_constructor
@after{
  MFPA.array_constructor();
}
    : LPAREN SLASH ac_spec SLASH RPAREN
    | LBRACKET ac_spec RBRACKET
    ;

/*
 * R770:  ac spec
 */
ac_spec
options {backtrack=true;}
@after {
    MFPA.ac_spec();
}
    : type_spec COLON_COLON (ac_value_list)?
    | ac_value_list
    ;

/*
 * CIVL: Listed rules are handled as terminals.
 * R771:  lbracket
 * R772:  rbracket
 */

/*
 * R773:  ac value
 */
ac_value
options {backtrack=true;}
@after {
    MFPA.ac_value();
}
    : expr
    | ac_implied_do
    ;

ac_value_list
@init{
  int numAV = 1;
}
@after{
  MFPA.ac_value_list(numAV);
}
    : ac_value 
      ( COMMA ac_value {numAV++;})*
    ;

/*
 * R774:  ac implied do
 */
ac_implied_do
    : LPAREN ac_value_list COMMA ac_implied_do_control RPAREN
        { MFPA.ac_implied_do();}
    ;

/*
 * R775:  ac implied do control
 * R776:  ac do variable
 *  OFP:  scalar_int_expr replaced by expr
 *  OFP:  ac_do_variable replaced by do_variable
 */
ac_implied_do_control
@init{
  boolean hasStrd=false;
}
    : IDENT EQUALS expr COMMA expr 
      ( COMMA expr {hasStrd=true;} )?
        { MFPA.ac_implied_do_control($IDENT, hasStrd); }
    ;

/*
 * R801:  type declaration stmt
 */
type_declaration_stmt
@init {
  int numAS = 0;
}
@after{
  checkForInclude();
}
    : (lbl=label)?  
      declaration_type_spec
      ( (COMMA attr_spec {numAS ++;})* COLON_COLON )?
      entity_decl_list end_of_stmt
        { MFPA.type_declaration_stmt(lbl, numAS, $end_of_stmt.t); }
    ;

/*
 * R802:  attr spec
 */
attr_spec
    : access_spec
        { MFPA.attr_spec(null, 
            MFPUtils.ATTR_ACCESS); }
    | ALLOCATABLE
        { MFPA.attr_spec($ALLOCATABLE, 
            MFPUtils.ATTR_ALLOCATABLE);}
    | ASYNCHRONOUS
        { MFPA.attr_spec($ASYNCHRONOUS, 
            MFPUtils.ATTR_ASYNCHRONOUS);}
    | CODIMENSION LBRACKET coarray_spec RBRACKET
        {MFPA.attr_spec($CODIMENSION, 
            MFPUtils.ATTR_CODIMENSION);}
    | CONTIGUOUS
        {MFPA.attr_spec($CONTIGUOUS, 
            MFPUtils.ATTR_CONTIGUOUS);}
    | DIMENSION LPAREN array_spec RPAREN
        {MFPA.attr_spec($DIMENSION, 
            MFPUtils.ATTR_DIMENSION);}
    | EXTERNAL
        {MFPA.attr_spec($EXTERNAL, 
            MFPUtils.ATTR_EXTERNAL);}
    | INTENT LPAREN intent_spec RPAREN
        {MFPA.attr_spec($INTENT, 
            MFPUtils.ATTR_INTENT);}
    | INTRINSIC
        {MFPA.attr_spec($INTRINSIC, 
            MFPUtils.ATTR_INTRINSIC);}
    | language_binding_spec
        {MFPA.attr_spec(null, 
            MFPUtils.ATTR_BIND);}
    | OPTIONAL
        {MFPA.attr_spec($OPTIONAL, 
            MFPUtils.ATTR_OPTIONAL);}
    | PARAMETER
        {MFPA.attr_spec($PARAMETER, 
            MFPUtils.ATTR_PARAMETER);}
    | POINTER
        {MFPA.attr_spec($POINTER, 
            MFPUtils.ATTR_POINTER);}
    | PROTECTED
        {MFPA.attr_spec($PROTECTED, 
            MFPUtils.ATTR_PROTECTED);}
    | SAVE
        {MFPA.attr_spec($SAVE, 
            MFPUtils.ATTR_SAVE);}
    | TARGET
        {MFPA.attr_spec($TARGET, 
            MFPUtils.ATTR_TARGET);}
    | VALUE
        {MFPA.attr_spec($VALUE, 
            MFPUtils.ATTR_VALUE);}
    | VOLATILE
        {MFPA.attr_spec($VOLATILE, 
            MFPUtils.ATTR_VOLATILE);}
    | attr_spec_extension
        {MFPA.attr_spec(null, 
            MFPUtils.ATTR_OTHER);}
    ;

attr_spec_extension
    : NO_LANG_EXT
        {MFPA.attr_spec_extension($NO_LANG_EXT, 
            MFPUtils.ATTR_EXT_NONE);}
    ;

/*
 * R803:  entity decl
 *  OFP:  IDENT inlined for object_name and function_name
 *        IDENT ( ASTERISK char_length )? takes character and function
 * TODO:  Pass more info to action
 */
entity_decl
@init{
   boolean hasAS=false;
   boolean hasCS=false;
   boolean hasCL=false;
   boolean hasInit=false;
}
    : IDENT 
      ( LPAREN array_spec RPAREN {hasAS=true;} )? 
      ( LBRACKET coarray_spec RBRACKET {hasCS=true;} )? 
      ( ASTERISK char_length {hasCL=true;} )? 
      ( initialization {hasInit=true;} )?
        { MFPA.entity_decl($IDENT, hasAS, hasCS, hasCL, hasInit); }
    ;

entity_decl_list
@init{
  int numED = 1;
}
@after{
  MFPA.entity_decl_list(numED);
}
    : entity_decl 
      ( COMMA entity_decl {numED ++;} )*
    ;

/*
 * R804:  object name
 */
object_name returns [Token t]
    : IDENT {t = $IDENT;}
    ;

/*
 * R805:  initialization
 *  OFP:  initialization_expr replaced by expr
 * CIVL:  null_init is combined in designator
 */
initialization
    : EQUALS expr 
      { MFPA.initialization(MFPUtils.INIT_VAL); }
//  | EQ_GT null_init 
//    { MFPA.initialization(MFPUtils.INIT_PTR); }
    | EQ_GT designator 
      { MFPA.initialization(MFPUtils.INIT_NUL); }
    ;

/*
 * R806:  null init
 *  OFP:  a reference to the NULL intrinsic function with no arguments
 */
null_init
//      NULL ( )
    : IDENT LPAREN RPAREN
      { MFPA.null_init($IDENT); }
    ;

/*
 * R807:  access spec
 */
access_spec
    : PUBLIC
      { MFPA.access_spec($PUBLIC, MFPUtils.ATTR_PUBLIC);}
    | PRIVATE
      { MFPA.access_spec($PRIVATE, MFPUtils.ATTR_PRIVATE);}
    ;

/*
 * R808:  language binding spec
 *  OFP:  scalar_char_initialization_expr replaced by expr
 */
language_binding_spec
@init{
  boolean hasName = false;
}
//    BIND ( C )
    : BIND LPAREN IDENT 
      (COMMA name EQUALS expr { hasName=true; })? RPAREN
        { MFPA.language_binding_spec($BIND, $IDENT, hasName); }
    ;

/*
 * R809:  coarray spec (replaced by array_spec)
 * R810:  deferred coshape spec (replaced by array_spec)
 * R811:  explicit coshape spec (replaced by array_spec)
 * R812:  lower cobound (see rule 817 lower bound)
 * R813:  upper cobound (see rule 818 upper bound)
 *  OFP:  deferred-coshape-spec-list and explicit-coshape-spec rules 
 *        are ambiguous, thus we use the same method as for array-spec.  
 *        Enough information is provided so that the coarray_spec can 
 *        be figured out by the actions.
 *        Note, that this means the parser can't determine all incorrect 
 *        syntax as many rules are combined into one.  
 *        It is the action's responsibility to enforce correct syntax.
 */
coarray_spec
@init{
  int numCS = 1;
}
   : array_spec_element 
     (COMMA array_spec_element {numCS++;})*
       { MFPA.coarray_spec(numCS); }
   ;

/*
 * R814:  dimension spec
 */
dimension_spec
    : DIMENSION LPAREN array_spec RPAREN
      { MFPA.dimension_spec($DIMENSION); }
    ;

/*
 * R815:  array spec
 * R819:  assumed shape spec
 * R821:  assumed implied spec
 * R822:  assumed size spec
 * R823:  implied shape or assumed size spec
 * R824:  implied shape spec
 * R825:  assumed rank spec
 * CIVL:  array_spec_element is shared by both array_spec and coarray_Spec
 *        R815 array-spec
 *         is R816 explicit_shape_spec is  [ expr : ] expr
 *         or R819 assumed-shape-spec is   [ expr ] :
 *         or R820 deferred-shape-spec is  :
 *         or R822 assumed-size-spec is    explicit_shape_spec, assumed-implied-spec
 *         or R824 implied-shape-spec is   assumed-implied-spec, assumed-implied-spec-list
 *         or R823 implied-shape-or-assumed-size-spec is assumed-implied-spec
 *         or R825 assumed-rank-spec is    ..
 *        Note: R821 assumed-implied-spec is    [ expr : ] *
 *              All bounds are replaced as expr
 *        Thus, array_spec_element should be:
 *            case 0:  expr
 *            case 1:  expr :
 *            case 2:  expr : expr
 *            case 3:  expr : *
 *            case 4:  *
 *            case 5:  :
 *            case 6:  ..
 */
array_spec
@init{
  int numAS = 1;
}
    : array_spec_element
      (COMMA array_spec_element {numAS++;})*
        { MFPA.array_spec(numAS);}
    ;

array_spec_element
@init{
  int type = MFPUtils.ASE_1U;
}
@after{
  MFPA.array_spec_element(type);
}
    : expr ( COLON {type=MFPUtils.ASE_LN;}
             ( expr {type=MFPUtils.ASE_LU;}
             | ASTERISK {type=MFPUtils.ASE_LX;} 
             )?
           )?
    | ASTERISK { type=MFPUtils.ASE_1X; }
    | COLON { type=MFPUtils.ASE_NN; }
    | DODOT { type=MFPUtils.ASE_RK; }
    ;


/*
 * R816:  explicit shape spec
 *  OFP:  refactored to remove conditional from lhs and inlined lower_bound and upper_bound
 */
explicit_shape_spec
@init{
  boolean hasUB = false;
}
    : expr (COLON expr {hasUB=true;})?
        { MFPA.explicit_shape_spec(hasUB);}
    ;

explicit_shape_spec_list
@init{
  int numESS = 1;
}
    : explicit_shape_spec
      ( COMMA explicit_shape_spec {numESS++;})*
        { MFPA.explicit_shape_spec_list(numESS);}
    ;

/*
 *  OFP: specification_expr inlined as expr
 * R817:  lower bound
 * R818:  upper bound
 */

/*
 * R820:  deferred shape spec
 *  OFP:  inlined as COLON in deferred_shape_spec_list
 */
deferred_shape_spec_list
@init{
  int numDSS = 1;
}
@after{
  MFPA.deferred_shape_spec_list(numDSS);
}
    : COLON 
      ( COMMA COLON {numDSS++;} )*
    ;

/*
 * R826:  intent spec
 */
intent_spec
    : IN { MFPA.intent_spec(
                $IN, null); }
    | OUT { MFPA.intent_spec(
                null, $OUT); }
    | IN OUT { MFPA.intent_spec(
                $IN, $OUT); }
    | INOUT { MFPA.intent_spec(
                $INOUT, $INOUT); }
    ;

/*
 * R827:  access stmt
 */
access_stmt
@init {
  boolean hasList=false;
}
@after{
  checkForInclude();
}
    : (lbl=label)?  
      access_spec ( 
        ( COLON_COLON )? 
        access_id_list {hasList=true;}
      )? end_of_stmt
        { MFPA.access_stmt(lbl,$end_of_stmt.t,hasList); }
    ;

/*
 * R828:  access id
 *  OFP:  IDENT inlined for use_name
 *        generic_spec can be IDENT so IDENT deleted
 */
access_id
    : generic_spec { MFPA.access_id(); }
    ;

access_id_list
@init{ 
  int numAI = 1;
}
@after{
  MFPA.access_id_list(numAI);
}
    : access_id 
      ( COMMA access_id {numAI++;} )*
    ;

/*
 * R829:  allocatable stmt
 */
allocatable_stmt
@init {
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      ALLOCATABLE ( COLON_COLON )? allocatable_decl_list end_of_stmt
        {  MFPA.allocatable_stmt(lbl, $ALLOCATABLE, $end_of_stmt.t);}
    ;
   
/*
 * R830:  allocatable decl
 */
allocatable_decl
@init{
  Token objName=null; 
  boolean hasAS=false; 
  boolean hasCS=false;
}
    : object_name {objName=$object_name.t;}
      ( LPAREN array_spec RPAREN {hasAS=true;} )?
      ( LBRACKET coarray_spec RBRACKET {hasCS=true;} )?
        { MFPA.allocatable_decl(objName, hasAS, hasCS);}
    ;

allocatable_decl_list
@init{
  int numAD = 1;
}
@after{
  MFPA.allocatable_decl_list(numAD);
}
    : allocatable_decl 
      ( COMMA allocatable_decl {numAD++;} )*
    ;

/*
 * R831:  asynchronous stmt
 *  OFP:  generic_name_list substituted for object_name_list
 */
asynchronous_stmt
@init {
}
@after{
  checkForInclude();
}
    : (lbl=label)?  
      ASYNCHRONOUS ( COLON_COLON )? generic_name_list 
      end_of_stmt
        { MFPA.asynchronous_stmt(lbl,$ASYNCHRONOUS,$end_of_stmt.t); }
    ;

/*
 * R832:  bind stmt
 */
bind_stmt
@init{
}
@after{
  checkForInclude();
}
    : (lbl=label)?  
      language_binding_spec ( COLON_COLON )? bind_entity_list 
      end_of_stmt
      { MFPA.bind_stmt(lbl, $end_of_stmt.t); }
    ;

/*
 * R833:  bind entity
 *  OFP:  2nd arg is 'isCommonBlockName'
 *        IDENT inlined for entity_name and common_block_name
 */
bind_entity
    : IDENT 
      { MFPA.bind_entity($IDENT, false); }
    | SLASH IDENT SLASH 
      { MFPA.bind_entity($IDENT, true); }
    ;

bind_entity_list
@init{
  int numBE = 1;
}
@after{
  MFPA.bind_entity_list(numBE);
}
    : bind_entity
      ( COMMA bind_entity {numBE++;} )*
    ;

/*
 * R834:  codimension stmt
 */
codimension_stmt
@init{
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      CODIMENSION ( COLON_COLON )? codimension_decl_list 
      end_of_stmt
      { MFPA.codimension_stmt(lbl, $CODIMENSION, $end_of_stmt.t); }
    ;

/*
 * R835:  codimension decl
 */
codimension_decl
    : IDENT LBRACKET coarray_spec RBRACKET
      { MFPA.codimension_decl($IDENT, $LBRACKET, $RBRACKET);}
    ;

codimension_decl_list
@init{
  int numCD = 1;
}
@after{
  MFPA.codimension_decl_list(numCD);
}
    : codimension_decl
      ( COMMA codimension_decl {numCD++;} )*
    ;

/*
 * R836:  contiguous stmt
 */
contiguous_stmt
@init{
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      CONTIGUOUS ( COLON_COLON )? generic_name_list 
      end_of_stmt
        { MFPA.contiguous_stmt(lbl, $CONTIGUOUS, $end_of_stmt.t); }
    ;

/*
 * R837:  data stmt
 */
data_stmt
@init{
  int numDSS = 1;
}
@after{
  checkForInclude();
}
    : (lbl=label)?  
      DATA data_stmt_set 
      ( ( COMMA )? data_stmt_set {numDSS++;})* 
      end_of_stmt
      { MFPA.data_stmt(lbl, $DATA, $end_of_stmt.t, numDSS); }
    ;

/*
 * R838:  data stmt set
 */
data_stmt_set
    : data_stmt_object_list
      SLASH data_stmt_value_list SLASH
      { MFPA.data_stmt_set(); }
    ;

/*
 * R839:  data stmt object
 */
data_stmt_object
@after {
    MFPA.data_stmt_object();
}
    : variable
    | data_implied_do
    ;

data_stmt_object_list
@init{
  int numDSO = 1;
}
@after{ 
  MFPA.data_stmt_object_list(numDSO);
}
    : data_stmt_object
      ( COMMA data_stmt_object {numDSO++;} )*
    ;

/*
 * R840:  data implied do
 * R842:  data i do variable
 *  OFP:  scalar_int_expr replaced by expr
 *        data_i_do_variable replaced by IDENT
 */
data_implied_do
@init{
  boolean hasStrd = false;
}
    : LPAREN data_i_do_object_list 
      COMMA IDENT EQUALS expr 
      COMMA expr 
      ( COMMA expr { hasStrd = true; })? RPAREN
        { MFPA.data_implied_do($IDENT, hasStrd); }
    ;

/*
 * R841:  data i do object
 *  OFP:  data_ref inlined for scalar_structure_component and array_element
 */
data_i_do_object
@after {
  MFPA.data_i_do_object();
}
    : data_ref
    | data_implied_do
    ;

data_i_do_object_list
@init{
  int numDIDO = 1;
}
@after{
  MFPA.data_i_do_object_list(numDIDO);
}
    : data_i_do_object 
      ( COMMA data_i_do_object {numDIDO++;} )*
    ;

/*
 * R843:  data stmt value
 * R844:  data stmt repeat
 * R846:  int constant subobject
 * R847:  constant subobject
 *  OFP:  Rule 844 inlined as: 
 *            designator (replacing Rule 847 in scalar-Rule 846)
 *          | int_literal_constant (replacing scalar-int-constant)
 *        And, if data stmt repeat does NOT appear,  
 *        then data-stmt-constant inlined as:
 *            a set of typed literal constants (replacing scalar-constant)
 *            designator (replacing scalar-constant-subobject and nitial-data-target)
 *            signed int/real constants
 *            structure constructor (also absorbs null-init)
 */
data_stmt_value
options {backtrack=true; k=3;}
@init {
  Token t = null;
}
@after{
  MFPA.data_stmt_value(t);
}
    : designator (ASTERISK data_stmt_constant {t=$ASTERISK;})?
    | int_literal_constant (ASTERISK data_stmt_constant {t=$ASTERISK;})?
    | signed_int_literal_constant
    | signed_real_literal_constant
    | complex_literal_constant
    | logical_literal_constant
    | char_literal_constant
    | boz_literal_constant
    | structure_constructor
    ;

data_stmt_value_list
@init{
  int numDSV = 1;
}
@after{
  MFPA.data_stmt_value_list(numDSV);
}
    : data_stmt_value
      ( COMMA data_stmt_value {numDSV++;} )*
    ;

/*
 * R845:  data stmt constant
 *  OFP:  items are inlined as:
 *            a set of typed literal constants (replacing scalar-constant)
 *            designator (replacing scalar-constant-subobject and nitial-data-target)
 *            signed int/real constants
 *            structure constructor (also absorbs null-init)
 */
data_stmt_constant
options {backtrack=true; k=3;}
@after {
  MFPA.data_stmt_constant();
}
    : designator
    | signed_int_literal_constant
    | signed_real_literal_constant
    | complex_literal_constant
    | logical_literal_constant
    | char_literal_constant
    | boz_literal_constant
    | structure_constructor
    ;

/*
 * R848:  dimension stmt
 */
dimension_stmt
@init {
  int numDD = 1;
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      DIMENSION ( COLON_COLON )? 
      dimension_decl ( COMMA dimension_decl {numDD++;})* 
      end_of_stmt
        { MFPA.dimension_stmt(lbl, $DIMENSION, $end_of_stmt.t, numDD); }
    ;

dimension_decl
    : IDENT LPAREN array_spec RPAREN
        { MFPA.dimension_decl($IDENT);}
    ;

/*
 * R849:  intent stmt
 *  OFP:  generic_name_list substituted for dummy_arg_name_list
 */
intent_stmt
@init{
}
@after{
  checkForInclude();
}
    : (lbl=label)?  
      INTENT LPAREN intent_spec RPAREN ( COLON_COLON )? generic_name_list 
      end_of_stmt
        { MFPA.intent_stmt(lbl,$INTENT,$end_of_stmt.t);}
    ;

/*
 * R850:  optional stmt
 *  OFP:  generic_name_list substituted for dummy_arg_name_list
 */
optional_stmt
@init {
}
@after{
  checkForInclude();
}
    : (lbl=label)?  
      OPTIONAL ( COLON_COLON )? generic_name_list 
      end_of_stmt
        { MFPA.optional_stmt(lbl, $OPTIONAL, $end_of_stmt.t); }
    ;

/*
 * R851:  parameter stmt
 */
parameter_stmt
@init{
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      PARAMETER LPAREN named_constant_def_list RPAREN
      end_of_stmt
        { MFPA.parameter_stmt(lbl,$PARAMETER,$end_of_stmt.t);}
    ;

/*
 * R852:  named constaant def
 *  OFP:  initialization_expr replaced by expr
 *        named_constant replaced by IDENT
 */
named_constant_def
    : IDENT EQUALS expr
        { MFPA.named_constant_def($IDENT);}
    ;

named_constant_def_list
@init{
  int numNCD = 1;
}
@after{
  MFPA.named_constant_def_list(numNCD);
}
    : named_constant_def
      ( COMMA named_constant_def {numNCD++;} )*
    ;

/*
 * R853:  pinter stmt
 */
pointer_stmt
@init{
  boolean isCrayPointer=false;
}
@after{
  checkForInclude();
}
    : (lbl=label)?  
      POINTER
      ( cray_pointer_assoc_list  {isCrayPointer = true;}
      | ( ( COLON_COLON )? pointer_decl_list )
      ) 
      end_of_stmt
        { if (isCrayPointer) {
            MFPA.cray_pointer_stmt(lbl,$POINTER,$end_of_stmt.t);
          } else {
            MFPA.pointer_stmt(lbl,$POINTER,$end_of_stmt.t);
          }
        }
    ;

/*
 * R854:  pointer decl
 *  OFP:  IDENT inlined as object_name and 
 *                           proc_entity_name (removing second alt)
 */
pointer_decl
@init{
  boolean hasSL = false;
}
    : IDENT 
      ( LPAREN deferred_shape_spec_list RPAREN {hasSL=true;})?
        { MFPA.pointer_decl($IDENT,hasSL);}
    ;

cray_pointer_assoc
    : LPAREN pointer=IDENT COMMA pointee=IDENT RPAREN
        { MFPA.cray_pointer_assoc(pointer, pointee);}
    ;

pointer_decl_list
@init{
  int numPD = 1;
}
@after{
  MFPA.pointer_decl_list(numPD);
}
    : pointer_decl
      ( COMMA pointer_decl {numPD++;} )*
    ;

cray_pointer_assoc_list
@init{
  int numCPA = 1;
}
@after{
  MFPA.cray_pointer_assoc_list(numCPA);
}
    : cray_pointer_assoc
      ( COMMA cray_pointer_assoc {numCPA++;} )*
    ;

/*
 * R855:  protected stmt
 *  OFP:  generic_name_list substituted for entity_name_list
 */
protected_stmt
@init{
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      PROTECTED ( COLON_COLON )? generic_name_list
      end_of_stmt
        { MFPA.protected_stmt(lbl,$PROTECTED,$end_of_stmt.t);}
    ;

/*
 * R856:  save stmt
 */
save_stmt
@init{
  boolean hasSEL = false;
}
@after{
  checkForInclude();
}
    : (lbl=label)?  
      SAVE ( ( COLON_COLON )? saved_entity_list {hasSEL=true;} )? 
      end_of_stmt
        { MFPA.save_stmt(lbl,$SAVE,$end_of_stmt.t,hasSEL);}
    ;

/*
 * R857:  saved entity
 * R858:  proc pointer name
 *  OFP:  IDENT inlined for object_name, 
 *                            proc_pointer_name (removing second alt),
 *                            common_block_name.
 */
saved_entity
    : id=IDENT
        { MFPA.saved_entity(id, false);}
    | SLASH id=IDENT SLASH
        {MFPA.saved_entity(id, true);} //Block Name
    ;

saved_entity_list
@init{
  int numSE = 1;
}
@after{
  MFPA.saved_entity_list(numSE);
}
    : saved_entity
      ( COMMA saved_entity {numSE++;} )*
    ;

/*
 * R859:  target stmt
 *  OFP:  IDENT inlined for object_name
 */
target_stmt
@init{
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      TARGET ( COLON_COLON )? target_decl_list
      end_of_stmt
        { MFPA.target_stmt(lbl,$TARGET,$end_of_stmt.t);}
    ;

/*
 * R860:  target decl
 */
target_decl
@init{
  boolean hasAS=false;
  boolean hasCS=false;
}
    : IDENT 
      (LPAREN array_spec RPAREN {hasAS=true;} )?
      (LBRACKET coarray_spec RBRACKET {hasCS=true;} )?
        { MFPA.target_decl($IDENT,hasAS,hasCS);}
    ;

target_decl_list
@init{
  int numTD = 1;
}
@after{
  MFPA.target_decl_list(numTD);
}
    : target_decl 
      ( COMMA target_decl {numTD++;} )*
    ;

/*
 * R861:  value stmt
 *  OFP:  generic_name_list substituted for dummy_arg_name_list
 */
value_stmt
@init{
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      VALUE ( COLON_COLON )? generic_name_list 
      end_of_stmt
        { MFPA.value_stmt(lbl,$VALUE,$end_of_stmt.t);}
    ;

/*
 * R862:  volatile stmt
 *  OFP:  generic_name_list substituted for object_name_list
 */
volatile_stmt
@init{
}
@after{
  checkForInclude();
}
    : (lbl=label)?  
      VOLATILE ( COLON_COLON )? generic_name_list 
      end_of_stmt
        { MFPA.volatile_stmt(lbl,$VOLATILE,$end_of_stmt.t);}
    ;

/*
 * R863:  implicit stmt
 */
implicit_stmt
@after{
  checkForInclude();
}
    : (lbl=label)?  
      IMPLICIT 
      implicit_spec_list 
      end_of_stmt
        { MFPA.implicit_stmt(
            lbl, $IMPLICIT, null, ins, $end_of_stmt.t);} 
    | (lbl=label)?  
      IMPLICIT NONE 
      (LPAREN (ins = implicit_none_spec)? RPAREN)? 
      end_of_stmt
        { MFPA.implicit_stmt(
            lbl, $IMPLICIT, $NONE, ins, $end_of_stmt.t);}
    ;

/*
 * R864:  implicit spec
 */
implicit_spec
    : declaration_type_spec LPAREN letter_spec_list RPAREN
        { MFPA.implicit_spec(); }
    ;

implicit_spec_list
@init{
  int numIS = 1;
}
@after{
  MFPA.implicit_spec_list(numIS);
}
    : implicit_spec
    ( COMMA implicit_spec {numIS++;} )*
    ;

/*
 * R865:  letter spec
 *  OFP:  action should check the token text
 *        which should be either a single letter
 *        or a letter range (e.g., A-Z).
 */
letter_spec 
    : id1=IDENT ( MINUS id2=IDENT )? 
        { MFPA.letter_spec(id1, id2); }
    ;

letter_spec_list
@init{
  int numLS = 1;
}
@after{
  MFPA.letter_spec_list(numLS);
}
    : letter_spec
      ( COMMA letter_spec {numLS++;} )*
    ;

/*
 * R866:  implicit none spec
 */
implicit_none_spec returns [Token t]
    : EXTERNAL { t = $EXTERNAL;}
    | TYPE { t = $TYPE;}
    ;

/*
 * R867:  import stmt
 *  OFP:  generic_name_list substituted for import_name_list
 */
import_stmt
@init{
  boolean hasINL = false;
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      IMPORT ( ( COLON_COLON )? generic_name_list {hasINL=true;})? 
      end_of_stmt
        { MFPA.import_stmt(lbl, $IMPORT, null, $end_of_stmt.t, hasINL);}
    | (lbl=label)? 
      IMPORT COMMA ONLY COLON generic_name_list 
      end_of_stmt
        { MFPA.import_stmt(lbl, $IMPORT, $ONLY, $end_of_stmt.t, true);}
    | (lbl=label)? 
      IMPORT COMMA NONE 
      end_of_stmt
        { MFPA.import_stmt(lbl, $IMPORT, $NONE, $end_of_stmt.t, false);}
    | (lbl=label)? 
      IMPORT COMMA ALL 
      end_of_stmt
        { MFPA.import_stmt(lbl, $IMPORT, $ALL, $end_of_stmt.t, false);}
    ;

/*
 * R868:  namelist stmt
 *  OFP:  IDENT inlined for namelist_group_name
 */
namelist_stmt
@init {
  int numNL =1;
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      NAMELIST SLASH nlName=IDENT SLASH
        { MFPA.namelist_group_name(nlName);}
      namelist_group_object_list
      ( ( COMMA )? SLASH nlName=IDENT SLASH
          { MFPA.namelist_group_name(nlName);}
            namelist_group_object_list {numNL++;}
      )* 
      end_of_stmt
        { MFPA.namelist_stmt(lbl,$NAMELIST,$end_of_stmt.t,numNL);}
    ;

/*
 * R869:  namelist group object
 *  OFP:  namelist_group_object was variable_name inlined as IDENT
 */
namelist_group_object_list
@init{
  int numNGO = 1;
}
@after{
  MFPA.namelist_group_object_list(numNGO);
}
    : goName=IDENT {MFPA.namelist_group_object(goName);}
      ( COMMA goName=IDENT 
          { MFPA.namelist_group_object(goName); numNGO++;} )*
    ;

/*
 * R870:  equivalence stmt
 */
equivalence_stmt
@init{
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      EQUIVALENCE equivalence_set_list
      end_of_stmt
        { MFPA.equivalence_stmt(lbl, $EQUIVALENCE, $end_of_stmt.t);}
    ;

/*
 * R871:  euivalence set
 */
equivalence_set
    : LPAREN equivalence_object COMMA equivalence_object_list RPAREN
        { MFPA.equivalence_set(); }
    ;

equivalence_set_list
@init{
  int numES = 1;
}
    : equivalence_set 
      ( COMMA equivalence_set {numES++;} )*
        { MFPA.equivalence_set_list(numES);}
    ;

/*
 * R872:  equivalence object
 *  OFP:  IDENT inlined for variable_name
 *        data_ref inlined for array_element
 *        data_ref is a IDENT so IDENT deleted (removing first alt)
 *        substring is a data_ref so data_ref deleted (removing second alt)
 */
equivalence_object
    : substring { MFPA.equivalence_object(); }
    ;

equivalence_object_list
@init{
  int numEO = 1;
}
@after{
  MFPA.equivalence_object_list(numEO);
}
    : equivalence_object 
      ( COMMA equivalence_object {numEO++;} )*
    ;

/*
 * R873:  common stmt
 *  OFP:  MFPA.common_block_name must be called in any case.
 */
common_stmt
@init{
  int numBlocks=1;
} 
@after{
  checkForInclude();
}
    : (lbl=label)? 
      COMMON ( cb_name=common_block_name )?
        { MFPA.common_block_name(cb_name); } 
      common_block_object_list
      ( ( COMMA )? cb_name=common_block_name
          { MFPA.common_block_name(cb_name); }
        common_block_object_list {numBlocks++;} 
      )* end_of_stmt
        { MFPA.common_stmt(
            lbl, $COMMON, $end_of_stmt.t, numBlocks);}
    ;

//  OFP: SLASH_SLASH required in case of no spaces slashes, '//'
common_block_name returns [Token id]
    : SLASH_SLASH {id=null;}
    | SLASH (IDENT)? SLASH {id=$IDENT;}
    ;

/*
 * R874:  common block object
 */
common_block_object
@init{
  boolean hasSSL=false;
}
    : IDENT
      ( LPAREN explicit_shape_spec_list RPAREN {hasSSL=true;})?
        { MFPA.common_block_object($IDENT,hasSSL);}
    ;

common_block_object_list
@init{
  int numCBO = 1;
}
@after{
  MFPA.common_block_object_list(numCBO);
}
    : common_block_object
      ( COMMA common_block_object {numCBO++;} )*
    ;

/* 
 * R896:  quantified_expr
 * CIVL:  CIVL extension
 */
quantified_expr
@init{
  boolean hasRestrict = false;
}
	: CIVL_PRIMITIVE LPAREN intrinsic_type_spec entity_decl_list 
			(COLON (quantified_expr|expr) {hasRestrict = true;} )? RPAREN expr
		{MFPA.quantified_expr($CIVL_PRIMITIVE, hasRestrict);}
	;

/*
 * R897:  civl_stmt
 * CIVL:  CIVL extension
 */
civl_stmt
@init{
  boolean isQuantified = false;
  int numArgs = 0;
}
	: CIVL_PRIMITIVE LPAREN 
			( expr {numArgs++;}
			| quantified_expr {isQuantified = true;numArgs++;}
			)? RPAREN
		{MFPA.civl_stmt($CIVL_PRIMITIVE, numArgs);}
	;
    
/*
 * R897:  pragma_type_qualifier_stmt
 * CIVL:  CIVL extension
 */
pragma_type_qualifier_stmt
@after{
  checkForInclude();
}
    : PRAGMA IDENT CIVL_PRIMITIVE end_of_stmt
        { MFPA.pragma_type_qualifier_stmt($IDENT, $CIVL_PRIMITIVE);}
    ;

/*
 * R898:  pragma_stmt
 * CIVL:  CIVL extension
 */
pragma_stmt
@init{
  boolean isCIVL = false;
}
@after{
  checkForInclude();
}
    : PRAGMA IDENT (pragma_tokens|civl_stmt {isCIVL = true;}) end_of_stmt
        { MFPA.pragma_stmt(isCIVL, $IDENT, $end_of_stmt.t);}
    ;

/*
 * R899: pragma_tokens
 * CIVL:  CIVL extension
 */
pragma_tokens
@init{
  int numPT=0;
}
@after{
  MFPA.pragma_token_list(numPT);
}
    : ( pt=(~ (EOS|EOF|CIVL_PRIMITIVE)) 
          { MFPA.pragma_token(pt); numPT++;} 
      )+
    ;

/*
 * R901:  designator
 *  OFP:  designator 
 *            is rule 804  (as IDENT
 *            or rule 913  (is data-ref)
 *            or rule 914  (is data-ref)
 *            or rule 917  (is data-ref)
 *            or rule 918  (is data-ref containing rule 910)
 *            or rule 915  (is data-ref with RE or IM)
 *            or substring
 *        (substring-range) may be matched in data-ref
 *        this rule is now identical to substring
 * CIVL:  TODO: Converter should recognize 'RE', 'IM' as RE, T_IM
 *              Then change hasSR to be designator kind
 */
designator
@init{
  boolean hasSR = false;
}
    : data_ref (LPAREN substring_range {hasSR=true;} RPAREN)?
        { MFPA.designator(hasSR); }
    | char_literal_constant LPAREN substring_range RPAREN
        { MFPA.substring(true); }
    ;

/*
 * R996:  designator_or_func_ref
 *  OFP:  OFP extension
 *        a function_reference is ambiguous with designator,
 *        which could be an array element. 
 *        data_ref may (or not) match 
 *            LPAREN ( actual_arg_spec_list )? RPAREN
 *        so is optional
 */
designator_or_func_ref
@init {
  int sType = MFPUtils.DOFR_NONE;
}
@after {
  MFPA.designator_or_func_ref(sType);
}
    : data_ref 
      ( LPAREN substring_range_or_arg_list RPAREN
          { sType = $substring_range_or_arg_list.sType; }
      )?
        { if(sType == MFPUtils.DOFR_SRNG) {
              MFPA.designator(true);
          }else if (sType == MFPUtils.DOFR_ARGS) {
              MFPA.function_reference();
          }
        }
    | char_literal_constant LPAREN substring_range RPAREN
       { sType = MFPUtils.DOFR_SSTR;
         MFPA.substring(true); 
       }
    ;

substring_range_or_arg_list returns [int sType]
@init{
  boolean hasUB = false;
  boolean hasLB = false;
  Token keyword = null;
  int numAAS = 1;
}
@after {
  MFPA.substring_range_or_arg_list();
}
    : COLON (expr {hasUB = true;})?
        { MFPA.substring_range(hasLB, hasUB);
          sType = MFPUtils.DOFR_SRNG;
        } // substring_range 
    | expr substr_range_or_arg_list_suffix
        { sType = $substr_range_or_arg_list_suffix.sType; }
    | IDENT EQUALS expr 
        { MFPA.actual_arg_spec($IDENT, null, null); } // hasExpr=false
      ( COMMA actual_arg_spec {numAAS++;} )*
        { MFPA.actual_arg_spec_list(numAAS);
          sType = MFPUtils.DOFR_ARGS;
        }
    | ( IDENT EQUALS {keyword=$IDENT;} )? ASTERISK lbl=label
        { MFPA.actual_arg_spec(keyword, $ASTERISK, lbl); } 
      ( COMMA actual_arg_spec {numAAS++;} )*
        { MFPA.actual_arg_spec_list(numAAS);
          sType = MFPUtils.DOFR_ARGS;
        }
    ;

substr_range_or_arg_list_suffix returns [int sType]
@init{
  boolean hasUB = false; 
  boolean hasLB = true; 
  int numAAS = 1;
  int error = -1;
}
@after{
  MFPA.substr_range_or_arg_list_suffix();
}
    :   { MFPA.actual_arg_spec_list(error); }
      COLON (expr {hasUB=true;})? 
        { MFPA.substring_range(hasLB, hasUB);
          sType = MFPUtils.DOFR_SRNG;
        } // substring_range 
     | { MFPA.actual_arg_spec(null, null, null); } // hasExpr=true
      ( COMMA actual_arg_spec {numAAS++;} )*
        { MFPA.actual_arg_spec_list(numAAS);
          sType = MFPUtils.DOFR_ARGS;
        } 
    ;

/*
 * R902:  variable
 */
variable
    : designator 
      { MFPA.variable(); }
    ;


/*
 * R903:  variable name
 *  OFP:  is name (inlined as IDENT)
 */

/*
 * R904:  logical variable
 */
logical_variable
    : variable 
      { MFPA.logical_variable(); }
    ;

/*
 * R905:  char variable
 */
char_variable
    : variable
      { MFPA.char_variable(); }
    ;

/*
 * R906:  default char variable
 */

default_char_variable
    : variable
        { MFPA.default_char_variable(); }
    ;

/*
 * R907:  int variable
 */
int_variable
    : variable
        { MFPA.int_variable(); }
    ;

/*
 * R908:  substring
 *  OFP:  C908 (rule 909) parent_string shall be of type character
 *        fix for ambiguity in data_ref allows it to match 
 *            LPAREN substring_range RPAREN
 *        so required LPAREN substring_range RPAREN made optional
 */
substring
@init{
  boolean hasSR = false;
}
@after{
  MFPA.substring(hasSR);
}
    : data_ref 
      (LPAREN substring_range RPAREN {hasSR=true;})?
    | char_literal_constant 
      LPAREN substring_range RPAREN {hasSR=true;}
    ;

/*
 * R909:  parent string
 *  OF:    is rule 903  (as IDENT in data-ref)
 *         or rule 913  (as data_ref)
 *         or rule 914  (as data_ref)
 *         or rule 917  (as data_ref)
 *         or rule 604  (as char_literal_constant)
 *                      (IDENT in data-ref, and must be char type)
 *        thus, inlined in rule 908 as (data_ref | char_literal_constant)
 */

/*
 * R910:  substring range
 *  OFP:  scalar_int_expr replaced by expr
 */
substring_range
@init{
  boolean hasUB = false;
  boolean hasLB = false;
}
    : (expr {hasLB = true;})? COLON (expr {hasUB = true;})?
        { MFPA.substring_range(hasLB, hasUB); }
    ;

/*
 * R911:  data ref
 */
data_ref
@init{
  int numPR = 1;
}
@after{
  MFPA.data_ref(numPR);
}
    : part_ref
      ( PERCENT part_ref {numPR++;})*
    ;

/*
 * R912:  part ref
 *  OFP:  IDENT inlined for part_name
 *        with k=2, this path is chosen over 
 *            LPAREN substring_range RPAREN
 * TODO:  error: if a function call, 
 *               should match id rather than 
 *               (section_subscript_list)
 */
part_ref
options{k=2;}
@init{
  boolean hasSSL = false; 
  boolean hasIS = false;
  Token id = null;
}
@after{
  MFPA.part_ref(id, hasSSL, hasIS);
}
    : (IDENT LPAREN) => 
          IDENT LPAREN section_subscript_list RPAREN
          (image_selector {hasIS=true;})?
        { hasSSL=true; id=$IDENT;}
    | (IDENT LBRACKET) => 
          IDENT image_selector
        { hasIS=true; id=$IDENT;}
    | IDENT
        { id=$IDENT;}
    ;

part_ref_no_image_selector
options{k=2;}
@init{
  boolean hasSSL = false; 
  boolean hasIS = false;
  Token id = null;
}
@after{
  MFPA.part_ref(id, hasSSL, hasIS);
}
    : (IDENT LPAREN) => 
          IDENT LPAREN section_subscript_list RPAREN
        { hasSSL=true; id=$IDENT;}
    | IDENT
        { id=$IDENT;}
    ;
   
/*
 * R913:  structure component
 *  OFP:  inlined as data_ref
 */

/*
 * R914:  coindexed named object
 * CIVL:  inlined as data_ref
 */

/*
 * R915:  complex part designator
 * CIVL:  inlined as data_ref
 */

/*
 * R916:  type param inquiry
 *  OFP:  inlined in rule 902 then deleted as can be designator
 *        IDENT inlined for type_param_name
 */

/*
 * R917:  array element
 *  OFP:  inlined as data_ref
 */

/*
 * R918:  array section
 *  OFP:  inlined in rule 901
 */

/*
 * R919:  subscript
 *  OFP:  inlined as expr
 *        scalar_int_expr replaced by expr
 */

/*
 * R920:  section subscript
 *  OFP:  expr inlined for subscript, vector_subscript, and stride (thus deleted option 3)
 *        refactored first optional expr from subscript_triplet modified to also match
 *        actual_arg_spec_list to reduce ambiguities and need for backtracking
 */
section_subscript returns [boolean isEmpty]
@init{
  boolean hasUB = false;
  boolean hasLB = false;
  boolean hasStrd = false;
  boolean isAmbiguous = false; 
}
    : expr section_subscript_ambiguous
    | COLON (expr {hasUB=true;})? (COLON expr {hasStrd=true;})?
        { MFPA.section_subscript(hasLB, hasUB, hasStrd, isAmbiguous); }
    | COLON_COLON expr
        { MFPA.section_subscript(hasLB, hasUB, true, isAmbiguous); }
    | IDENT EQUALS expr
        { MFPA.actual_arg_spec($IDENT, null, null); }     // may be actual-arg, see rule 1524
    | IDENT EQUALS ASTERISK lbl=label
        { MFPA.actual_arg_spec($IDENT, $ASTERISK, lbl); } // may be actual-arg, see rule 1524
    | ASTERISK lbl=label
        { MFPA.actual_arg_spec(null, $ASTERISK, lbl); }   // may be actual-arg, see rule 1524
    | { isEmpty = true; }                           // empty may be actual-arg, see rule 1524
    ;

section_subscript_ambiguous
@init {
  boolean hasUB = false;
  boolean hasLB = true;
  boolean hasStrd = false;
  boolean isAmbiguous = false; 
}
@after{
  MFPA.section_subscript(hasLB, hasUB, hasStrd, isAmbiguous);
}
    : COLON (expr {hasUB=true;})? (COLON expr {hasStrd=true;})?
      /* OFP: this alternative is necessary because 
       *      if alt1 above has no expr following the first COLON and 
       *      there is an optional second COLON with no WS between the two, 
       *      the lexer will make a COLON_COLON token but not two COLON tokens.
       *      in this case, the second expr is required. 
       *      (for an example, see J3/04-007, Note 7.44.)
       */
    | COLON_COLON expr { hasStrd=true; }
     | { isAmbiguous=true; } // empty could be an actual-arg, see rule 1524
    ;
   
section_subscript_list
@init{
  int numSS = 1;
}
@after{
  MFPA.section_subscript_list(numSS);
}
    : isEmpty=section_subscript { if (isEmpty) numSS--; }
      (COMMA section_subscript {numSS++;})*
    ;

/*
 * R921:  subscript triplet
 * R922:  stride
 * R923:  vector subscript
 *  OFP:  inlined in rule 920
 *        subscript, stride and vector subscript inlined as expr
 */

/*
 * R924:  image selector
 */
image_selector
@init{
  boolean hasISSL = false;
}
    : LBRACKET cosubscript_list (COMMA image_selector_spec_list {hasISSL=true;} ) RBRACKET
        { MFPA.image_selector(hasISSL);}
    ;

/*
 * R925:  cosubscript
 * CIVL:    is scalar-int-expr  (inlined as expr)
 */
cosubscript_list
@init{
  int numE = 1;
}
@after{
  MFPA.cosubscript_list(numE);
}
    : expr
      ( COMMA expr {numE++;} )*
    ;

/*
 * R926:  image selector spec
 * CIVL:   contains rule 942   (inlined as designator)
 *               or rule 1115  (inlined as scalar-expr)
 *               or rule 1026  (inlined is scalar-int-expr)
 */
image_selector_spec
    : STAT EQUALS designator
        { MFPA.image_selector_spec($STAT, 
              MFPUtils.ATTR_STAT); }
    | TEAM EQUALS expr
        { MFPA.image_selector_spec($TEAM, 
              MFPUtils.ATTR_TEAM); }
    | TEAM_NUMBER EQUALS expr
        { MFPA.image_selector_spec($TEAM_NUMBER, 
              MFPUtils.ATTR_TEAM_NUMBER); }
    ;


image_selector_spec_list
@init{
  int numISS = 1;
}
@after{
  MFPA.image_selector_spec_list(numISS);
}
    : image_selector_spec
      ( COMMA image_selector_spec {numISS++;} )*
    ;

/*
 * R927:  allocate stmt
 *  OFP:  modified to remove backtracking by looking for the token inserted 
 *        during the lexical prepass if a COLON_COLON was found 
 *        (which required alt1 below).
 */
allocate_stmt
@init{
  boolean hasTS = false;
  boolean hasAOL = false;
}
@after{
  checkForInclude();
}
    : (lbl=label)?  
      M_ALLOCATE_STMT_1 ALLOCATE LPAREN 
      type_spec COLON_COLON allocation_list
      ( COMMA alloc_opt_list {hasAOL=true;} )? RPAREN 
      end_of_stmt
        { MFPA.allocate_stmt(lbl, $ALLOCATE, 
              $end_of_stmt.t, true, hasAOL);
        }
    | (lbl=label)?  
      ALLOCATE LPAREN allocation_list
      ( COMMA alloc_opt_list {hasAOL=true;} )? RPAREN
      end_of_stmt
        { MFPA.allocate_stmt(lbl, $ALLOCATE, 
              $end_of_stmt.t, hasTS, hasAOL);
        }
    ;

/*
 * R928:  alloc opt
 * R929:  errmsg variable
 * R930:  source expr
 * CIVL:   contains rule 929  (inlined as int-variable)
 *               or rule 930  (inlined as expr)
 *               or rule 942  (inlined as designator)
 */
alloc_opt
    : ERRMSG EQUALS default_char_variable
        { MFPA.alloc_opt($ERRMSG, 
              MFPUtils.AO.ERRMSG); }
    | MOLD EQUALS expr
        { MFPA.alloc_opt($MOLD, 
              MFPUtils.AO.MOLD); }
    | SOURCE EQUALS expr
        { MFPA.alloc_opt($SOURCE, 
              MFPUtils.AO.SOURCE); }
    | STAT EQUALS designator
        { MFPA.alloc_opt($STAT, 
              MFPUtils.AO.STAT); }
    ;

alloc_opt_list
@init{
  int numAO = 1;
}
@after{
  MFPA.alloc_opt_list(numAO);
}
    : alloc_opt
      ( COMMA alloc_opt {numAO++;} )*
    ;

/*
 * R931:  allocation
 */
allocation
@init{
  boolean hasASSL = false; 
  boolean hasACS = false;
}
    : (allocate_object LBRACKET) => 
          allocate_object LBRACKET allocate_coarray_spec RBRACKET
        { MFPA.allocation(hasASSL, true); }
      /* OFP:  This option (with allocate_shape_spec_list) 
       *       is caught by the allocate object.
       *       If so, the section-subscript-list must 
       *       be changed into a allocate-shape-spec-list.
       */
//  | (allocate_object LPAREN) => 
//        allocate_object LPAREN allocate_shape_spec_list RPAREN
//        ( LBRACKET allocate_coarray_spec {hasACS=true;} RBRACKET )?
//      { MFPA.allocation(true, hasACS);}
    | (allocate_object) => 
          allocate_object
        { MFPA.allocation(hasASSL, hasACS);}
   ;

allocation_list
@init{
  int numAl = 1;
}
@after{
  MFPA.allocation_list(numAl);
}
    : allocation
      ( COMMA allocation {numAl++;} )*
    ;

/*
 * R932:  allocate object
 *  OFP:  C644 (R932) An allocate-object shall not be a coindexed object.
 *        IDENT inlined for variable_name
 *        data_ref inlined for structure_component
 *        data_ref is a IDENT so IDENT deleted
 *        data_ref inlined and part_ref_no_image_selector called directly
 */
allocate_object
@init{
  int numPR = 1;
}
@after{
  MFPA.data_ref(numPR); 
  MFPA.allocate_object();
}
    : part_ref_no_image_selector
      (PERCENT part_ref_no_image_selector {numPR++;})*
    ;

allocate_object_list
@init{
  int numAO = 1;
}
@after{
  MFPA.allocate_object_list(numAO);
}
    : allocate_object
      ( COMMA allocate_object {numAO++;} )*
    ;

/*
 * R933:  allocate shape spec
 *  OFP:  always has upper bound
 *        grammar was refactored to remove left recursion
 */
allocate_shape_spec
@init{
  boolean hasUB = true;
  boolean hasLB = false;
}
    : expr (COLON expr)?
        { MFPA.allocate_shape_spec(hasLB, hasUB); }
    ;

allocate_shape_spec_list
@init{
  int numASS = 1;
}
@after{
  MFPA.allocate_shape_spec_list(numASS);
}
    : allocate_shape_spec
      ( COMMA allocate_shape_spec {numASS++;} )*
    ;

/*
 * R934:  lower bound expr
 * R935:  upper bound expr
 *  OFP:  is scalar_int_expr inlined as expr 
 */

/*
 * R936:  allocate coarray spec
 *  OFP:  TODO: unfinished
 */
allocate_coarray_spec
options{k=3;}
@after{
  MFPA.allocate_coarray_spec();
}
    : (ASTERISK) => ASTERISK
    | (expr COLON ASTERISK) => expr COLON ASTERISK
//  | allocate_coshape_spec_list COMMA ( expr COLON )? ASTERISK
//  | ASTERISK // TESTING
    ;

/*
 * R937:  allocate coshape spec
 */
allocate_coshape_spec
@init{
  boolean hasLB = false;
}
    : expr ( COLON expr { hasLB = true; })?
        { MFPA.allocate_coshape_spec(hasLB); }
    ;

allocate_coshape_spec_list
@init{
  int numAOS = 1;
}
@after{
  MFPA.allocate_coshape_spec_list(numAOS);
}
   : allocate_coshape_spec
     ( COMMA allocate_coshape_spec {numAOS++;} )*
   ;

/*
 * R938:  nullify stmt
 */
nullify_stmt
@init{
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      NULLIFY LPAREN pointer_object_list RPAREN
      end_of_stmt
        { MFPA.nullify_stmt(lbl, $NULLIFY, $end_of_stmt.t); }
    ;

/*
 * R939:  pointer object
 *  OFP:  IDENT inlined for variable_name and proc_pointer_name
 *        data_ref inlined for structure_component
 *        data_ref can be a IDENT so IDENT deleted
 */
pointer_object
    : data_ref
      { MFPA.pointer_object(); }
    ;

pointer_object_list
@init{
  int numPO = 1;
}
@after{
  MFPA.pointer_object_list(numPO);
}
    : pointer_object
      ( COMMA pointer_object {numPO++;} )*
    ;

/*
 * R940:  deallocate stmt
 */
deallocate_stmt
@init{
  boolean hasDOL = false;
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      DEALLOCATE LPAREN allocate_object_list
      ( COMMA dealloc_opt_list {hasDOL=true;})? RPAREN
      end_of_stmt
        { MFPA.deallocate_stmt(lbl, 
              $DEALLOCATE, $end_of_stmt.t, hasDOL);}
    ;
    
/*
 * R941:  dealloc opt
 * R942:  stat variable
 *  OFP:  stat_variable and errmsg_variable replaced by designator
 * CIVL:   contains rule 929  (inlined as int-variable)
 *               or rule 942  (inlined as designator)
 */
dealloc_opt
    : STAT EQUALS designator
        { MFPA.dealloc_opt($STAT, 
              MFPUtils.AO.STAT); }
    | ERRMSG EQUALS default_char_variable
        { MFPA.dealloc_opt($ERRMSG, 
              MFPUtils.AO.ERRMSG); }
    ;

dealloc_opt_list
@init{
  int numDO = 1;
}
@after{
  MFPA.dealloc_opt_list(numDO);
}
    : dealloc_opt
      ( COMMA dealloc_opt {numDO++;} )*
    ;

/*
 * R1001: primary
 *   OFP: constant replaced by literal_constant as IDENT can be designator
 *        IDENT inlined for type_param_name
 *        data_ref in designator can be a IDENT so deleted
 *        type_param_inquiry is designator PERCENT IDENT 
 *          can be designator so deleted 
 *        function_reference integrated with designator (was ambiguous) 
 *          and deleted (to reduce backtracking)
 */
primary
options {backtrack=true;} // alt 1,4 ambiguous
@after{
  MFPA.primary();
}
    : designator_or_func_ref
    | literal_constant
    | array_constructor
    | structure_constructor
    | LPAREN expr RPAREN
    ;

/*
 * R1002: level 1 expr
 */
level_1_expr
@init{
  Token t = null;
}
    : (defined_unary_op {t = $defined_unary_op.t;})? 
      primary
        { MFPA.level_1_expr(t);}
    ;

/*
 * R1003: defined unary op . letter+ .
 */
defined_unary_op returns [Token t]
    : DEFINED_OP {t = $DEFINED_OP;}
    ;

/*
 * R1004: mult operand
 *   OFP: power_operand inserted as rule 1004 functionality
 */
power_operand
@init{
  Token t = null;
}
    : level_1_expr 
      ( power_op power_operand {t = $power_op.t;})?
          { MFPA.power_operand(t);}
    ;

mult_operand
@init{
  int numMO = 0;
}
    : power_operand 
      ( mult_op power_operand
          { numMO++; MFPA.mult_operand__mult_op($mult_op.t); }
      )*
        { MFPA.mult_operand(numMO); }
    ;

/*
 * R1005: add operand
 *   OFP: This rule has been added so the unary plus/minus has
 *        the correct precedence when actions are performed.
 *        moved leading optionals to mult_operand
 */
signed_operand
    : (t=add_op)? 
      mult_operand 
        { MFPA.signed_operand(t);}
    ;

add_operand
@init{
  int numAO = 0;
}
    : signed_operand
      ( t=add_op mult_operand 
          { numAO++; MFPA.add_operand__add_op(t); }
      )*
        { MFPA.add_operand(numAO);}
    ;

/*
 * R1006: level 2 expr
 *   OFP: ( ( level_2_expr )? add_op )? add_operand
 *        check notes on how to remove this left recursion  
 *        (WARNING something like the following)
 *        : (add_op)? ( add_operand add_op )* add_operand
 *
 *        moved leading optionals to add_operand
 */
level_2_expr
@init{
  int numCO = 0;
}
    : add_operand 
      ( concat_op add_operand 
          { numCO++;}
      )*
        { MFPA.level_2_expr(numCO); }
    ;

/*
 * R1007: power op  **
 */
power_op returns [Token t]
    : POWER {t = $POWER;}
    ;

/*
 * R1008: mult op  * or /
 */
mult_op returns [Token t]
    : ASTERISK { t = $ASTERISK; }
    | SLASH { t = $SLASH; }
    ;

/*
 * R1009: add op  + or -
 */
add_op returns [Token t]
    : PLUS {t = $PLUS;}
    | MINUS {t = $MINUS;}
    ;

/*
 * R1010: level 3 expr
 *   OFP: moved leading optional to level_2_expr
 */
level_3_expr
@init{Token t = null;}
    : level_2_expr 
      (rel_op level_2_expr {t = $rel_op.t;})?
        { MFPA.level_3_expr(t);}
    ;
    
/*
 * R1011: concat op  //
 */
concat_op returns [Token t]
    : SLASH_SLASH {t = $SLASH_SLASH;}
    ;

/*
 * R1012: level 4 expr
 *   OFP: moved leading optional to level_3_expr
 *        inlined level_3_expr for level_4_expr in rule 1014
 */

/*
 * R1013:  rel op  .EQ. (==) or .NE.(/=) or .LT. (<) or .LE. (<=) or .GT. (>) or .GE. (>=)
 */
rel_op returns [Token t]
    : EQ {t=$EQ;}
    | NE {t=$NE;}
    | LT {t=$LT;}
    | LE {t=$LE;}
    | GT {t=$GT;}
    | GE {t=$GE;}
    | EQ_EQ {t=$EQ_EQ;}
    | SLASH_EQ {t=$SLASH_EQ;}
    | LESSTHAN {t=$LESSTHAN;}
    | LESSTHAN_EQ {t=$LESSTHAN_EQ;}
    | GREATERTHAN {t=$GREATERTHAN;}
    | GREATERTHAN_EQ {t=$GREATERTHAN_EQ;}
    ;

/*
 * R1014: and operand
 *   OFP: level_4_expr inlined as level_3_expr
 */
and_operand
@init {
  int numAO = 0;
}
    : ( t0=not_op )? level_3_expr
      ( and_op ( t1=not_op )? level_3_expr
          { MFPA.and_operand__not_op(t1); 
            numAO++;
            t1=null;
          }
      )*
        { MFPA.and_operand(t0, numAO);}
    ;

/*
 * R1015: or operand
 *   OFP: moved leading optional to or_operand
 */
or_operand
@init{
  int numOO = 0;
}
    : and_operand (or_op and_operand {numOO++;})*
        { MFPA.or_operand(numOO); }
    ;

/*
 * R1016: equiv operand
 *   OFP: moved leading optional to or_operand
 */
equiv_operand
@init{
  int numEO = 0;
}
    : or_operand 
      ( equiv_op or_operand
          { MFPA.equiv_operand__equiv_op($equiv_op.t); 
            numEO++;
          }
      )*
        { MFPA.equiv_operand(numEO); }
    ;

/*
 * R1017: level 5 expr
 *   OFP: moved leading optional to equiv_operand
 */
level_5_expr
@init{
  int numDBO = 0;
}
    : equiv_operand 
      ( defined_binary_op equiv_operand
          { MFPA.level_5_expr__defined_binary_op($defined_binary_op.t); 
            numDBO++;
          }
      )*
        { MFPA.level_5_expr(numDBO); }
    ;

/*
 * R1018: not op  .NOT.
 */
not_op returns [Token t]
    : NOT {t = $NOT;} 
    ;

/*
 * R1019: and op  .AND.
 */
and_op returns [Token t]
    : AND {t = $AND;}
    ;

/*
 * R1020: or op  .OR.
 */
or_op returns [Token t]
    : OR {t = $OR;}
    ;

/*
 * R1021: equiv op  .EQV. or .NEQV.
 */
equiv_op returns [Token t]
    : EQV {t = $EQV;}
    | NEQV {t = $NEQV;}
    ;

/*
 * R1022: expr
 *   OFP: moved leading optional to level_5_expr
 */
expr
    : level_5_expr {MFPA.expr();}
    ;

/*
 * R1023: defined binary op . letter+ .
 */
defined_binary_op returns [Token t]
    : DEFINED_OP {t = $DEFINED_OP;}
    ;

/*
 * R1024: logical expr
 * R1025: default char expr
 * R1026: int expr
 * R1027: numeric expr
 * R1028: specification expr
 * R1029: constant expr
 * R1030: default char constant expr
 * R1031: int constant expr
 *   OFP: inlined as expr
 */

/*
 * R1032: assignment stmt
 */
assignment_stmt
@init{
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      M_ASSIGNMENT_STMT variable EQUALS expr 
      end_of_stmt
        { MFPA.assignment_stmt(lbl, $end_of_stmt.t);}
    ;

/*
 * R1033: pointer assignment stmt
 *   OFP: ensure that part_ref in data_ref doesn't capture the LPAREN
 *        data_pointer_object and proc_pointer_object replaced by designator
 *        data_target and proc_target replaced by expr
 *        third alt covered by first alt so proc_pointer_object assignment deleted
 *        designator (rule 901), minus the substring part is data_ref, so designator 
 *        replaced by data_ref.
 *
 *        TODO: alt1 and alt3 require the backtracking.  
 *              if find a way to disambiguate them, 
 *              should be able to remove backtracking.
 */
pointer_assignment_stmt
options{backtrack=true;}
@init{
}
@after{
  checkForInclude();
}
    : (lbl=label)?  
      M_PTR_ASSIGNMENT_STMT data_ref EQ_GT expr
      end_of_stmt
        { MFPA.pointer_assignment_stmt(lbl, 
              $end_of_stmt.t, MFPUtils.PAS_NONE);}
    | (lbl=label)? 
      M_PTR_ASSIGNMENT_STMT data_ref 
      LPAREN bounds_spec_list RPAREN EQ_GT expr 
      end_of_stmt
        { MFPA.pointer_assignment_stmt(lbl, 
              $end_of_stmt.t, MFPUtils.PAS_BOUND_SPEC);}
    | (lbl=label)?  
      M_PTR_ASSIGNMENT_STMT data_ref 
      LPAREN bounds_remapping_list RPAREN EQ_GT expr 
      end_of_stmt
        { MFPA.pointer_assignment_stmt(lbl, 
              $end_of_stmt.t, MFPUtils.PAS_BOUND_REMAP);}
    ;

/*
 * R1034: data pointer object
 *   OFP: ensure ( IDENT | designator ending in PERCENT IDENT)
 *        IDENT inlined for variable_name and data_pointer_component_name
 *        variable replaced by designator
 */
data_pointer_object
    : designator { MFPA.data_pointer_object(); }
    ;

/*
 * R1035: bounds spec
 *   OFP: lower_bound_expr replaced by expr
 */
bounds_spec
    : expr COLON { MFPA.bounds_spec(); }
    ;

bounds_spec_list
@init{
  int numBS = 1;
}
@after{
  MFPA.bounds_spec_list(numBS);
}
    : bounds_spec
      ( COMMA bounds_spec {numBS++;} )*
    ;

/*
 * R1036: bounds remapping
 *   OFP: lower_bound_expr replaced by expr
 *        upper_bound_expr replaced by expr
 */
bounds_remapping
    : expr COLON expr
        { MFPA.bounds_remapping(); }
    ;

bounds_remapping_list
@init{
  int numBR = 1;
}
@after{
  MFPA.bounds_remapping_list(numBR);
}
    : bounds_remapping
      ( COMMA bounds_remapping {numBR++;} )*
    ;

/*
 * R1037: data target
 *   OFP: inlined as expr in rule 758 and 1033
 *        expr can be designator (via primary) so variable deleted
 */

/*
 * R1038: proc pointer object
 *   OFP: ensure ( IDENT | ends in PERCENT IDENT )
 *        IDENT inlined for proc_pointer_name
 *        proc_component_ref replaced by designator PERCENT IDENT replaced
 *        by designator
 */
proc_pointer_object
    : designator { MFPA.proc_pointer_object(); }
    ;

/*
 * R1039: proc component ref
 *   OFP: inlined as designator PERCENT IDENT in 
 *          rule 1038, 1040, 1522, an 1524
 *        IDENT inlined for procedure_component_name
 *        designator inlined for variable
 */

/*
 * R1040: proc target
 *   OFP: inlined as expr in R459 and R735
 *        ensure ( expr | designator ending in PERCENT IDENT)
 *        IDENT inlined for procedure_name
 *        IDENT isa expr so IDENT deleted
 *        proc_component_ref is variable PERCENT IDENT 
 *          can be designator so deleted
 */

/*
 * R1041: where stmt
 *   OFP: mask_expr replaced by expr
 *        assignment_stmt inlined for where_assignment_stmt
 */
where_stmt
@init{
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      M_WHERE_STMT WHERE LPAREN expr RPAREN 
      assignment_stmt 
        { MFPA.where_stmt(lbl, $WHERE); }
    ;

/*
 * R1042: where construct
 */
where_construct
@init{
  int numC = 0;
  int numMC = 0;
  int numEwC = 0; 
  boolean hasMEw = false;
  boolean hasEw = false;
}
    : where_construct_stmt
      ( where_body_construct {numC++;} )* 
      ( masked_elsewhere_stmt ( where_body_construct { numMC++;} )* 
          { hasMEw = true;
            MFPA.masked_elsewhere_stmt__end(numMC);
          }
      )*
      ( elsewhere_stmt ( where_body_construct {numEwC++;} )*
          { hasEw = true;
            MFPA.elsewhere_stmt__end(numEwC);
          }
      )?
      end_where_stmt
        { MFPA.where_construct(numC, hasMEw, hasEw);}
    ;

/*
 * R1043: where construct stmt
 *   OFP: mask_expr replaced by expr
 */
where_construct_stmt
@init{
  Token id = null;
}
@after{
  checkForInclude();
}
    : ( IDENT COLON {id=$IDENT;})? 
      M_WHERE_CONSTRUCT_STMT WHERE LPAREN expr RPAREN 
      end_of_stmt 
        { MFPA.where_construct_stmt(id, $WHERE, $end_of_stmt.t);}
    ;

/*
 * R1044: where body construct
 *   OFP: assignment_stmt inlined for where_assignment_stmt
 */
where_body_construct
@after{
  MFPA.where_body_construct();
}
    : assignment_stmt
    | where_stmt
    | where_construct
    ;

/*
 * R1045: where assignment stmt
 *   OFP: inlined as assignment_stmt in rule 1041 and 1044
 */

/*
 * R1046: mask expr
 *   OFP: inlined mask_expr was logical_expr
 *        inlined scalar_mask_expr was scalar_logical_expr
 *        inlined scalar_logical_expr was logical_expr
 *  CIVL: finally replaced as expr.
 */

/*
 * R1047: masked elsewhere stmt
 *   OFP: mask_expr replaced by expr
 */
masked_elsewhere_stmt
@init{
  Token id = null;
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      ELSE WHERE LPAREN expr RPAREN ( IDENT {id=$IDENT;})? 
      end_of_stmt
        { MFPA.masked_elsewhere_stmt(lbl, 
              $ELSE, $WHERE, id, $end_of_stmt.t);}
    | (lbl=label)? 
      ELSEWHERE LPAREN expr RPAREN ( IDENT {id=$IDENT;})?
      end_of_stmt
        { MFPA.masked_elsewhere_stmt(lbl, 
              $ELSEWHERE, null, id, $end_of_stmt.t);}
    ;

/*
 * R1048: elsewhere stmt
 */
elsewhere_stmt
@init{
  Token id = null;
} 
@after{
  checkForInclude();
}
    : (lbl=label)?  
      ELSE WHERE (IDENT {id=$IDENT;})? 
      end_of_stmt
        { MFPA.elsewhere_stmt(lbl, 
              $ELSE, $WHERE, id, $end_of_stmt.t);}
    | (lbl=label)?  
      ELSEWHERE (IDENT {id=$IDENT;})?
      end_of_stmt
        { MFPA.elsewhere_stmt(lbl, 
              $ELSEWHERE, null, id, $end_of_stmt.t);}
    ;

/*
 * R1049: end where stmt
 */
end_where_stmt
@init{
  Token id=null;
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      END WHERE ( IDENT {id=$IDENT;} )?
      end_of_stmt
        { MFPA.end_where_stmt(lbl, 
              $END, $WHERE, id, $end_of_stmt.t);}
    ;

/*
 * R1050: forall construct
 */
forall_construct
@after{
  MFPA.forall_construct(); 
}
    : forall_construct_stmt
      ( forall_body_construct )*
      end_forall_stmt
    ;

/*
 * R1051: forall construct stmt
 *   OFP: forall-construct-name inlined as IDENT
 */
forall_construct_stmt
@init{
  Token id = null;
}
@after{
  checkForInclude();
}
    : (lbl=label)?  
      ( IDENT COLON {id=$IDENT;})? 
      M_FORALL_CONSTRUCT_STMT FORALL concurrent_header 
      end_of_stmt 
        { MFPA.forall_construct_stmt(lbl, 
              id, $FORALL, $end_of_stmt.t);}
    ;

/*
 * R1052: forall body construct
 */
forall_body_construct
@after {
  MFPA.forall_body_construct();
}
    : forall_assignment_stmt
    | where_stmt
    | where_construct
    | forall_construct
    | forall_stmt
    ;

/*
 * R1053: forall assignment stmt
 */
forall_assignment_stmt
@after{
  checkForInclude();
}
    : assignment_stmt
        { MFPA.forall_assignment_stmt(false); }
    | pointer_assignment_stmt
        { MFPA.forall_assignment_stmt(true); }
    ;

/*
 * R1054: end forall stmt
 */
end_forall_stmt
@init{
  Token id=null;
}
@after{
  checkForInclude();
}
    : (lbl=label)?  
      END FORALL ( IDENT {id=$IDENT;})?
      end_of_stmt
        { MFPA.end_forall_stmt(lbl, 
              $END, $FORALL, id, $end_of_stmt.t);}
    ;

/*
 * R1055: forall stmt
 *   OFP: M_FORALL_STMT token is inserted by scanner 
 *          to remove need for backtracking
 */
forall_stmt
@init{
}
@after{
  checkForInclude();
}
    : (lbl=label)?  
      M_FORALL_STMT FORALL concurrent_header forall_assignment_stmt
        { MFPA.forall_stmt(lbl, $FORALL);}
    ;

/*
 * R1101: block
 */
block
@init{
  int numExec = 0;
}
@after {
  MFPA.block(numExec);
}
    : ( execution_part_construct {numExec++;})*
    ;

/*
 * R1102:  associate construct
 */
associate_construct
    : associate_stmt
      block
      end_associate_stmt
        { MFPA.associate_construct(); }
    ;

/*
 * R1103: associate stmt
 */
associate_stmt
@init {
  Token id=null;
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      ( IDENT COLON {id=$IDENT;})? 
      ASSOCIATE LPAREN association_list RPAREN 
      end_of_stmt
        { MFPA.associate_stmt(lbl, 
              id, $ASSOCIATE, $end_of_stmt.t);}
    ;

/*
 * R1104: association
 *   OFP: IDENT inlined for associate_name
 */
association
    : IDENT EQ_GT selector
        { MFPA.association($IDENT); }
    ;

association_list
@init{
  int numA = 1;
}
@after{
  MFPA.association_list(numA);
}
    : association
      ( COMMA association {numA++;} )*
    ;

/*
 * R1105: selector
 *   OFP: expr can be designator (via primary) so variable deleted
 */
selector
    : expr { MFPA.selector(); }
    ;

/*
 * R1106:  end associate stmt
 */
end_associate_stmt
@init{
  Token id=null;
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      END ASSOCIATE (IDENT {id=$IDENT;})? 
      end_of_stmt
        { MFPA.end_associate_stmt(lbl, 
            $END, $ASSOCIATE, id, $end_of_stmt.t);}
    ;

/*
 * R1107: block construct
 *   OFP: C1107 A block-specification-part shall not contain a 
 *        COMMON, EQUIVALENCE, INTENT, NAMELIST, OPTIONAL,
 *        statement function, or VALUE statement.
 *        (implicit-part in specification-part can be removed)
 */
block_construct
@after{
  MFPA.block_construct();
}
    : block_stmt
      specification_part_and_block 
      end_block_stmt
    ;

/*
 * R1108: block stmt
 */
block_stmt
@init{
  Token name = null;
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      ( IDENT COLON {name=$IDENT;})? BLOCK 
      end_of_stmt 
        { MFPA.block_stmt(lbl, 
              name, $BLOCK, $end_of_stmt.t);}
    ;

/*
 * R1109: block specification part
 */
specification_part_and_block
@init{
  int numUS = 0; 
  int numIS = 0; 
  gctr0 = 0;
}
    : ( use_stmt {numUS++;} )* 
      ( import_stmt {numIS++;} )* 
      declaration_construct_and_block
        { MFPA.specification_part_and_block(
              numUS, numIS, gctr0);}
    ;

declaration_construct_and_block
@init{
  gctr0++;
}
    : ((label)? ENTRY) =>
          entry_stmt declaration_construct_and_block
    | ((label)? ENUM) =>
          enum_def declaration_construct_and_block
    | ((label)? FORMAT) =>
          format_stmt declaration_construct_and_block
    | ((label)? INTERFACE) =>
          interface_block declaration_construct_and_block
    | ((label)? PARAMETER) =>
          parameter_stmt declaration_construct_and_block
    | ((label)? PROCEDURE) => 
          procedure_declaration_stmt declaration_construct_and_block
    | (derived_type_stmt) =>
          derived_type_def declaration_construct_and_block
    | (type_declaration_stmt) =>
          type_declaration_stmt declaration_construct_and_block
    | ((label)? access_spec) =>
          access_stmt declaration_construct_and_block
    | ((label)? ALLOCATABLE) =>
          allocatable_stmt  declaration_construct_and_block
    | ((label)? ASYNCHRONOUS) => 
          asynchronous_stmt declaration_construct_and_block
    | ((label)? BIND) =>
          bind_stmt declaration_construct_and_block
    | ((label)? CODIMENSION) =>
          codimension_stmt declaration_construct_and_block
    | ((label)? DATA) => 
          data_stmt declaration_construct_and_block
    | ((label)? DIMENSION) =>
          dimension_stmt declaration_construct_and_block
    | ((label)? EXTERNAL) =>
          external_stmt declaration_construct_and_block
    | ((label)? INTRINSIC) =>
          intrinsic_stmt declaration_construct_and_block
    | ((label)? POINTER) => 
          pointer_stmt declaration_construct_and_block
    | ((label)? PROTECTED) =>
          protected_stmt declaration_construct_and_block
    | ((label)? SAVE) => 
          save_stmt declaration_construct_and_block
    | ((label)? TARGET) => 
          target_stmt declaration_construct_and_block
    | ((label)? VOLATILE) => 
          volatile_stmt declaration_construct_and_block
    | block {gctr0--;}  /* decrement extra count as this isn't a declConstruct */
    ;

/*
 * R1110: end block stmt
 */
end_block_stmt
@init{
  Token name = null;
}
@after{
  checkForInclude();
}
    : (lbl=label)?  
      END BLOCK (IDENT {name=$IDENT;})? 
      end_of_stmt
        { MFPA.end_block_stmt(lbl, 
              name, $END, $BLOCK, $end_of_stmt.t);}
    ;

/*
 * R1111: change team construct
 */
change_team_construct
@after{
  MFPA.change_team_construct();
}
    : change_team_stmt block end_change_team_stmt
    ;

/*
 * R1112: change team stmt
 */
change_team_stmt
@init{
  Token name = null;
  boolean hasCAL = false;
  boolean hasSSL = false;
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      ( IDENT COLON {name=$IDENT;})? CHANGE TEAM 
      LPAREN expr (COMMA coarray_association_list {hasCAL = true;})?
      ( COMMA sync_stat_list {hasSSL = true;} )? RPAREN
        { MFPA.change_team_stmt(lbl, 
              name, $CHANGE, $TEAM, hasCAL, hasSSL);}
    ;

/*
 * R1113: coarray association
 */
coarray_association
    : codimension_decl EQ_GT expr
        { MFPA.coarray_association(); }
    ;
    
coarray_association_list
@init{
  int numCA = 1;
}
@after{
  MFPA.coarray_association_list(numCA);
}
    : coarray_association
      ( COMMA coarray_association {numCA++;} )*
    ;

/*
 * R1114:  end change team stmt
 */
end_change_team_stmt
@init{
  Token name = null;
  boolean hasSSL = false;
}
    : (lbl=label)? 
      END TEAM 
      ( LPAREN (sync_stat_list {hasSSL = true;})? RPAREN)? 
      ( IDENT {name=$IDENT;})?
        { MFPA.end_change_team_stmt(lbl, 
              $END, $TEAM, name, hasSSL);}
    ;

/*
 * R1115: team value
 *  CIVL: inlined in rule 1112 as expr
 */

/*
 * R1116:  critical construct
 */
critical_construct
    : critical_stmt block end_critical_stmt
        { MFPA.critical_construct();}
    ;

/*
 * R1117:  critical stmt
 */
critical_stmt
@init{
  Token name = null;
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      (IDENT COLON {name=$IDENT;})? CRITICAL 
      end_of_stmt
        { MFPA.critical_stmt(lbl, 
              name, $CRITICAL, $end_of_stmt.t);}
    ;

/*
 * R1118: end critical stmt
 */
end_critical_stmt
@init{
  Token name = null;
}
@after{
  checkForInclude();
}
    : (lbl=label)?  
      END CRITICAL (IDENT {name=$IDENT;})? 
      end_of_stmt 
        { MFPA.end_critical_stmt(lbl, 
              name, $END, $CRITICAL, $end_of_stmt.t);}
    ;

/*
 * R1119: do construct
 *   OFP: deleted second alternative, nonblock_do_construct, to reduce backtracking,
 *        see comments for 'F08 rule 835 nonblock_do_construct' on how termination 
 *        of nested loops must be handled.
 *  CIVL: F2008 rule 835 is deleted in F2018
 */
do_construct
    : do_stmt
      block
      end_do
        { MFPA.do_construct(); }
    ;

/*
 * R1120: do stmt
 * R1121: label do stmt
 * R1122: nonlabel do stmt
 *   OFP: label_do_stmt and nonlabel_do_stmt inlined
 */
do_stmt
@init {
  Token id=null;
  Token doLbl =null;
  boolean hasLC = false;
}
@after{
  checkForInclude();
}
    : (lbl=label)?  
      ( IDENT COLON {id=$IDENT;})? DO 
      ( DIGIT_STR {doLbl=$DIGIT_STR;})? 
      ( loop_control {hasLC=true;})? 
      end_of_stmt
        { MFPA.do_stmt(lbl, 
              id, $DO, doLbl, $end_of_stmt.t, hasLC);}
    ;

/*
 * R1123: loop control
 * R1124: do variable
 *   OFP: scalar_int_expr replaced by expr
 *        scalar_logical_expr replaced by expr
 *  CIVL: do_variable replaced as IDENT
 */
loop_control
@init{
  boolean hasOE = false;
}
    : ( COMMA )? IDENT EQUALS expr COMMA expr 
      ( COMMA expr {hasOE=true;})? 
        { MFPA.loop_control($IDENT, hasOE);}
    | ( COMMA )? WHILE LPAREN expr RPAREN 
        { MFPA.loop_control($WHILE, hasOE);}
    | ( COMMA )? CONCURRENT concurrent_header
        { MFPA.loop_control($CONCURRENT, hasOE);}
    ;

/*
 * R1125: concurrent header
 *  CIVL: integer_type_spec (705) replaced by intrinsic_type_spec
 *        scalar_mask_expr replaced as expr
 */
concurrent_header
@init{
  boolean hasITS = false;
  boolean hasME = false;
}
    : LPAREN 
      (intrinsic_type_spec COLON_COLON {hasITS = true;})? 
      concurrent_control_list 
      ( COMMA expr {hasME = true;})? 
      RPAREN
        { MFPA.concurrent_header(hasITS, hasME); }
    ;

/*
 * R1126: concurrent control
 *   OFP: IDENT inlined for index_name
 *        expr inlined for concurrent_limit and concurrent_step
 *       
 */
concurrent_control
@init{
  boolean hasStrd = false;
}
    : IDENT EQUALS expr COLON expr 
      ( COLON expr {hasStrd=true;})?
        { MFPA.concurrent_control($IDENT, hasStrd);}
    ;

concurrent_control_list
@init{
  int numCC = 1;
}
@after{
  MFPA.concurrent_control_list(numCC);
}
    : concurrent_control
      ( COMMA concurrent_control {numCC++;} )*
    ;

/*
 * R1127: concurrent limit
 * R1128: concurrent step
 *  CIVL: is scalar_int_expr replaced as expr.
 */

/*
 * R1129:  concurrent locality
 */
concurrent_locality
@init{
  int numCL = 0;
}
@after{
  MFPA.concurrent_locality(numCL);
}
    : (locality_spec {numCL++;}) *
    ;

/*
 * R1130: locality spec
 */
locality_spec
    : LOCAL LPAREN generic_name_list RPAREN
        { MFPA.locality_spec($LOCAL, null); }
    | LOCAL_INT LPAREN generic_name_list RPAREN
        { MFPA.locality_spec($LOCAL_INT, null); }
    | SHARED LPAREN generic_name_list RPAREN
        { MFPA.locality_spec($SHARED, null); }
    | DEFAULT LPAREN NONE RPAREN
        { MFPA.locality_spec($DEFAULT, $NONE); }
    ;

/*
 * R1131: end do
 *   OFP: TODO continue-stmt is ambiguous with same in action statement,
 *        check there for label and if label matches do-stmt label, 
 *        then match end-do 
 *        do_term_action_stmt added to allow block_do_construct to cover
 *        nonblock_do_construct as well.
 */
end_do
@after{
  MFPA.end_do();
}
    : end_do_stmt
    | do_term_action_stmt
    ;

/*   OFP: try requiring an action_stmt and then we can simply insert 
 *        the new M_LBL_DO_TERMINAL during the Sale's prepass.  
 *        EOS is in action_stmt.
 *        added the END DO and ENDDO options to this rule 
 *        because of the token M_LBL_DO_TERMINAL that is inserted 
 *        if they end a labeled DO.
 *        
 */
do_term_action_stmt
@init{
  Token id=null;
  Token endToken = null;
  Token doToken = null;
}
@after{
  checkForInclude();
}
    : (lbl=label)? M_LBL_DO_TERMINAL
      ( action_stmt 
      | ( END DO {endToken=$END; doToken=$DO;} 
          (IDENT {id=$IDENT;})?
        ) 
        end_of_stmt
      )
        { MFPA.do_term_action_stmt(lbl, id, 
              endToken, doToken, $end_of_stmt.t);}
    ;

/*
 * R1132: end do stmt
 *   OFP: IDENT inlined for do_construct_name
 */
end_do_stmt
@init{
  Token id=null;
}
@after{
  checkForInclude();
}
    : (lbl=label)?  
      END DO ( IDENT {id=$IDENT;} )? 
      end_of_stmt
        { MFPA.end_do_stmt(lbl, id, 
              $END, $DO, $end_of_stmt.t);}
    ;

/*
 * R1133: cycle stmt
 *   OFP: IDENT inlined for do_construct_name
 */
cycle_stmt
@init{
  Token id = null;
}
@after{
  checkForInclude();
}
    : (lbl=label)?  
      CYCLE (IDENT {id=$IDENT;})? 
      end_of_stmt
        { MFPA.cycle_stmt(lbl, 
              $CYCLE, id, $end_of_stmt.t); }
    ;

/*
 * R1134: if construct
 */
if_construct
@init{
  int numB = 1;
}
@after{
  MFPA.if_construct(numB);
}
    : if_then_stmt block 
      ( else_if_stmt block {numB++;} )* 
      ( else_stmt block {numB++;} )?
      end_if_stmt
    ;

/*
 * R1135: if then stmt
 *   OFP: scalar_logical_expr replaced by expr
 */
if_then_stmt
@after{
  checkForInclude();
}
    : (lbl=label)? 
      ( id=IDENT COLON )?
      IF LPAREN expr RPAREN THEN 
      end_of_stmt
        { MFPA.if_then_stmt(lbl, id, 
            $IF, $THEN, $end_of_stmt.t);}
    ;

/*
 * R1136: else if stmt
 *   OFP: scalar_logical_expr replaced by expr
 */
else_if_stmt
@after{
  checkForInclude();
}
    : (lbl=label)?  
      ELSE IF LPAREN expr RPAREN THEN 
      ( id=IDENT )? 
      end_of_stmt
        { MFPA.else_if_stmt(lbl, id, 
              $ELSE, $IF, $THEN, $end_of_stmt.t);}
    | (lbl=label)? 
      ELSEIF LPAREN expr RPAREN THEN 
      ( id=IDENT )? 
      end_of_stmt
        { MFPA.else_if_stmt(lbl, id, 
              $ELSEIF, null, $THEN, $end_of_stmt.t);}
    ;

/*
 * R1137: else stmt
 */
else_stmt
@after{
  checkForInclude();
}
    : (lbl=label)? 
      ELSE ( id=IDENT )?
      end_of_stmt
        { MFPA.else_stmt(lbl, id, 
              $ELSE, $end_of_stmt.t); }
    ;

/*
 * R1138: end if stmt
 */
end_if_stmt
@after{
  checkForInclude();
}
    : (lbl=label)? 
      END IF ( id=IDENT )? 
      end_of_stmt
        { MFPA.end_if_stmt(lbl, id, $END, $IF, $end_of_stmt.t);}
    ;

/*
 * R1139: if stmt
 *   OFP: scalar_logical_expr replaced by expr
 *        M_IF_STMT inserted by scanner to remove need for backtracking
 */
if_stmt
@init{
}
@after{
  checkForInclude();
}
    : (lbl=label)?  
      M_IF_STMT IF LPAREN expr RPAREN 
      action_stmt
        { MFPA.if_stmt(lbl, $IF); }
    ;

/*
 * R1140: case construct
 */
case_construct
@after {
  MFPA.case_construct();
}
    : select_case_stmt ( case_stmt block )* end_select_stmt
    ;

/*
 * R1141: select case stmt
 *   OFP: case_expr replaced by expr
 */
select_case_stmt
@init{
  Token id = null;
  Token t0 = null;
  Token t1 = null;
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      ( IDENT COLON {id=$IDENT;})?
      ( SELECT CASE {t0=$SELECT; t1=$CASE;}
      | SELECTCASE {t0=$SELECTCASE; t1=null;} 
      ) LPAREN expr RPAREN 
      end_of_stmt
        { MFPA.select_case_stmt(lbl, 
              id, t0, t1, $end_of_stmt.t);}
    ;
    
/*
 * R1142: case stmt
 */
case_stmt
@init{
  Token id=null;
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      CASE case_selector ( IDENT {id=$IDENT;})? 
      end_of_stmt
        { MFPA.case_stmt(lbl, 
              $CASE, id, $end_of_stmt.t);}
    ;

/*
 * R1143: end select stmt
 */
end_select_stmt
@init{
  Token id=null;
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      END SELECT (IDENT {id=$IDENT;})? 
      end_of_stmt
        { MFPA.end_select_stmt(lbl, 
              $END, $SELECT, id, $end_of_stmt.t);}
    ;

/*
 * R1144: case expr
 *   OFP: inlined case_expr as expr
 */

/*
 * R1145:  case selector
 */
case_selector
    : LPAREN case_value_range_list RPAREN
        { MFPA.case_selector(null); }
    | DEFAULT
        { MFPA.case_selector($DEFAULT); }
    ;

/*
 * R1146: case value range
 */
case_value_range
@after{
  MFPA.case_value_range();
}
    : COLON case_value
    | case_value case_value_range_suffix
    ;

case_value_range_suffix
@after{
  MFPA.case_value_range_suffix();
}
    : COLON ( case_value )?
    | { /* empty */ }
    ;

case_value_range_list
@init{
  int numCVR = 1;
}
@after{
  MFPA.case_value_range_list(numCVR);
}
    : case_value_range {numCVR++;}
      ( COMMA case_value_range {numCVR++;} )*
    ;

/*
 * R1147: case value
 *  CIVL: constant-expr replaced as expr
 */
case_value
    : expr { MFPA.case_value(); }
    ;

/*
 * R1148:  select rank construct
 */
select_rank_construct
    : select_rank_stmt
      (select_rank_case_stmt block)*
      end_select_rank_stmt
        { MFPA.select_rank_construct(); }
    ;

/*
 * R1149: select rank stmt
 */
select_rank_stmt
@after{
  checkForInclude();
}
    : (lbl=label)? 
      (sname=IDENT COLON)?
      SELECT RANK LPAREN 
      (aname=IDENT EQ_GT)? selector RPAREN
      end_of_stmt
        { MFPA.select_rank_stmt(lbl, 
              sname, $SELECT, $RANK, aname, $end_of_stmt.t); }
    | (lbl=label)?  
      (sname=IDENT COLON)?
      SELECTRANK LPAREN 
      (aname=IDENT EQ_GT)? selector RPAREN
      end_of_stmt
        { MFPA.select_rank_stmt(lbl, 
              sname, $SELECTRANK, null, aname, $end_of_stmt.t); }
    ;

/*
 * R1150: select rank case stmt
 */
select_rank_case_stmt
@init{
  Token id = null;
}
    : (lbl=label)?  
      RANK LPAREN expr RPAREN ( IDENT {id=$IDENT;} )?
        { MFPA.select_rank_case_stmt(lbl, $RANK, null, id); }
    | (lbl=label)? 
      RANK LPAREN ASTERISK RPAREN ( IDENT {id=$IDENT;})?
        { MFPA.select_rank_case_stmt(lbl, $RANK, $ASTERISK, id); }
    | (lbl=label)? 
      RANK DEFAULT ( IDENT {id=$IDENT;})?
        { MFPA.select_rank_case_stmt(lbl, $RANK, $DEFAULT, id); }
    ;

/*
 * R1151: end select rank stmt
 */
end_select_rank_stmt
@init{
  Token id=null;
}
@after{
  checkForInclude();
}
    : (lbl=label)?  
      END SELECT ( IDENT {id=$IDENT;} )? 
      end_of_stmt
        { MFPA.end_select_rank_stmt(lbl, 
              $END, $SELECT, id, $end_of_stmt.t);}
    ;

/*
 * R1152: select type construct
 */
select_type_construct
    : select_type_stmt ( type_guard_stmt block )* 
      end_select_type_stmt 
        { MFPA.select_type_construct(); }
    ;

/*
 * R1153: select type stmt
 *   OFP: IDENT inlined for select_construct_name and associate_name
 */
select_type_stmt
@after{checkForInclude();}
    : (lbl=label)?
      (sname=IDENT COLON)? 
      SELECT TYPE LPAREN 
      (aname=IDENT EQ_GT)? selector RPAREN 
      end_of_stmt
        { MFPA.select_type_stmt(lbl, 
              sname, $SELECT, $TYPE, aname, $end_of_stmt.t);}
    | (lbl=label)? 
      (sname=IDENT COLON)? 
      SELECTTYPE LPAREN 
      (aname=IDENT EQ_GT)? selector RPAREN 
      end_of_stmt
        { MFPA.select_type_stmt(lbl, 
              sname, $SELECTTYPE, null, aname, $end_of_stmt.t);}
    ;

/*
 * R1154: type guard stmt
 *   OFP: IDENT inlined for select_construct_name
 *        
 */
type_guard_stmt
@init{
  Token sname = null;
}
@after{
  checkForInclude();
}
    : (lbl=label)?  
      TYPE IS LPAREN type_spec RPAREN
      ( IDENT {sname=$IDENT;} )? 
      end_of_stmt
        { MFPA.type_guard_stmt(lbl, 
              $TYPE, $IS, sname, $end_of_stmt.t);}
    | (lbl=label)? 
      CLASS IS LPAREN type_spec RPAREN
      ( IDENT {sname=$IDENT;} )? 
      end_of_stmt
        { MFPA.type_guard_stmt(lbl, 
              $CLASS, $IS, sname, $end_of_stmt.t);}
    | (lbl=label)?  
      CLASS DEFAULT 
      ( IDENT {sname=$IDENT;} )?
      end_of_stmt
        { MFPA.type_guard_stmt(lbl, 
              $CLASS, $DEFAULT, sname, $end_of_stmt.t);}
    ;

/*
 * R1155: end select type stmt
 *   OFP: IDENT inlined for select_construct_name
 */
end_select_type_stmt
@init{
  Token id = null;
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      END SELECT ( IDENT {id=$IDENT;})? 
      end_of_stmt
        { MFPA.end_select_type_stmt(lbl, 
              $END, $SELECT, id, $end_of_stmt.t);}
    ;

/*
 * R1156: exit stmt
 *   OFP: IDENT inlined for do_construct_name
 */
exit_stmt
@init{
  Token id = null;
}
@after{
  checkForInclude();
}
    : (lbl=label)?  
      EXIT (IDENT {id=$IDENT;})? 
      end_of_stmt
        { MFPA.exit_stmt(lbl, 
              $EXIT, id, $end_of_stmt.t); }
    ;

/*
 * R1157: goto stmt
 */
goto_stmt
@init{
  Token toLbl=null;
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      GO TO DIGIT_STR 
      end_of_stmt
        { MFPA.goto_stmt(lbl, 
              $GO, $TO, $DIGIT_STR, $end_of_stmt.t); }
    | (lbl=label)? 
      GOTO DIGIT_STR 
      end_of_stmt
        { MFPA.goto_stmt(lbl, 
              $GOTO, null, $DIGIT_STR, $end_of_stmt.t); }
    ;

/*
 * R1158: computed goto stmt
 */
computed_goto_stmt
@after{
  checkForInclude();
}
    : (lbl=label)? 
      GO TO LPAREN label_list RPAREN ( COMMA )? expr 
      end_of_stmt
        { MFPA.computed_goto_stmt(lbl, 
              $GO, $TO, $end_of_stmt.t); }
    | (lbl=label)? 
      GOTO LPAREN label_list RPAREN ( COMMA )? expr  
      end_of_stmt
        { MFPA.computed_goto_stmt(lbl, 
              $GOTO, null, $end_of_stmt.t); }
    ;

/*
 * R1159: continue stmt
 */
continue_stmt
@after{
  checkForInclude();
}
    : (lbl=label)? 
      CONTINUE 
      end_of_stmt
        { MFPA.continue_stmt(lbl, 
              $CONTINUE, $end_of_stmt.t); } 
    ;

/*
 * R1160: stop stmt
 *  CIVL: scalar-logical-expr replaced by expr
 *        rule 1162 stop code inlined as expr with type integer/char
 */
stop_stmt
@init{
  boolean hasSC = false;
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      STOP (stop_code {hasSC=true;})? 
      (COMMA quiet=QUIET EQUALS expr)?
      end_of_stmt
        { MFPA.stop_stmt(lbl, 
              $STOP, quiet, $end_of_stmt.t, hasSC); }
    ;

/*
 * R1161: error stop stmt
 *  CIVL: scalar-logical-expr replaced by expr
 *        rule 1162 stop code inlined as expr with type integer/char
 */
error_stop_stmt
@init{
  boolean hasSC = false;
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      ERROR STOP (stop_code {hasSC=true;})?
      (COMMA quiet=QUIET EQUALS expr)?
      end_of_stmt
        { MFPA.error_stop_stmt(lbl, 
              $ERROR, $STOP, quiet, $end_of_stmt.t, hasSC); }
    |  (lbl=label)? 
      ERRORSTOP (expr {hasSC=true;})?
      (COMMA quiet=QUIET EQUALS expr)?
      end_of_stmt
        { MFPA.error_stop_stmt(lbl, 
              $ERRORSTOP, null, quiet, $end_of_stmt.t, hasSC); }
    ;

/*
 * R1162: stop code
 *   OFP: DIGIT_STR must be 5 digits or less
 *  CIVL: scalar_char_constant replaced as char_constant
 */
stop_code
    : char_constant
        { MFPA.stop_code(null); }
    | DIGIT_STR
        { MFPA.stop_code($DIGIT_STR); } 
    ;

/*
 * R1163: fail image stmt
 */
/*
fail_image_stmt
    : (lbl=label)? 
      FAIL IMAGE
        { MFPA.fail_image_stmt(lbl, $FAIL, $IMAGE); }
    | (lbl=label)? 
      FAILIMAGE
        { MFPA.fail_image_stmt(lbl, $FAILIMAGE, null); }
    ;
*/

/*
 * R1164: sync all stmt
 */
sync_all_stmt
@init{
  boolean hasSSL = false;
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      SYNC ALL (LPAREN RPAREN)? 
      end_of_stmt
        { MFPA.sync_all_stmt(lbl, 
              $SYNC, $ALL, $end_of_stmt.t, hasSSL); }
    | (lbl=label)? 
      SYNC ALL LPAREN sync_stat_list RPAREN 
      end_of_stmt
        { MFPA.sync_all_stmt(lbl, 
              $SYNC, $ALL, $end_of_stmt.t, true); }
    ;

/*
 * R1165: sync stat
 *   OFP: expr is a stat-variable or an errmsg-variable
 *        {'STAT','ERRMSG'} exprs are variables
 */
sync_stat
    : IDENT EQUALS expr
        { MFPA.sync_stat($IDENT); }
    ;

sync_stat_list
@init{
  int numSS = 1;
}
@after{
  MFPA.sync_stat_list(numSS);
}
    : sync_stat 
      ( COMMA sync_stat {numSS++;} )*
    ;

/*
 * R1166: sync images stmt
 */
sync_images_stmt
@init{
  boolean hasSSL = false;
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      SYNC IMAGES LPAREN image_set 
      (COMMA sync_stat_list {hasSSL=true;})? RPAREN 
      end_of_stmt
        { MFPA.sync_images_stmt(lbl, 
              $SYNC, $IMAGES, $end_of_stmt.t, hasSSL); }
    ;

/*
 * R1167: image set
 */
image_set
    : expr {MFPA.image_set(null); }
    | ASTERISK { MFPA.image_set($ASTERISK); }
    ;

/*
 * R1168:  sync memory stmt
 */
sync_memory_stmt
@init{
  boolean hasSSL = false;
}
@after{
  checkForInclude();
}
    : (lbl=label)?  
      SYNC MEMORY (LPAREN RPAREN)? 
      end_of_stmt
        { MFPA.sync_memory_stmt(lbl, 
              $SYNC, $MEMORY, $end_of_stmt.t, hasSSL); }
    | (lbl=label)?  
      SYNC MEMORY LPAREN sync_stat_list RPAREN 
      end_of_stmt 
        { MFPA.sync_memory_stmt(lbl, 
              $SYNC, $MEMORY, $end_of_stmt.t, true); }
   ;

/*
 * R1169:  sync team stmt
 */
/*
sync_team_stmt
@init{
  boolean hasSSL = false;
}
@after{
  checkForInclude();
}
    : (lbl=label)?  
      SYNC TEAM LPAREN variable 
      (COMMA sync_stat_list {hasSSL = true;} )? RPAREN
      end_of_stmt
        { MFPA.sync_team_stmt(lbl, 
              $SYNC, $TEAM, hasSSL, $end_of_stmt.t); }
    | (lbl=label)?  
      SYNCTEAM LPAREN variable 
      (COMMA sync_stat_list {hasSSL = true;} )? RPAREN
      end_of_stmt
        { MFPA.sync_team_stmt(lbl, 
              $SYNCTEAM, null, hasSSL, $end_of_stmt.t); }
    ; 
*/

/*
 * R1170:  event post stmt
 */
/*
event_post_stmt
@init{
  boolean hasSSL = false;
}
@after{
  checkForInclude();
}
    : (lbl=label)?  
      EVENT POST LPAREN variable 
      (COMMA sync_stat_list {hasSSL = true;} )? RPAREN
      end_of_stmt
        { MFPA.event_post_stmt(lbl, 
              $EVENT, $POST, hasSSL, $end_of_stmt.t); }
    | (lbl=label)?  
      EVENTPOST LPAREN variable 
      (COMMA sync_stat_list {hasSSL = true;} )? RPAREN
      end_of_stmt
        { MFPA.event_post_stmt(lbl, 
              $EVENTPOST, null, hasSSL, $end_of_stmt.t); }
    ; 
*/

/*
 * R1171: event variable
 *  CIVL: inlined as variable
 */

/*
 * R1172:  event wait stmt
 */
/*
event_wait_stmt
@init{
  boolean hasEWSL = false;
}
@after{
  checkForInclude();
}
    : (lbl=label)?  
      EVENT WAIT LPAREN variable 
      (COMMA event_wait_spec_list {hasEWSL = true;} )? RPAREN
      end_of_stmt
        { MFPA.event_wait_stmt(lbl, 
              $EVENT, $WAIT, hasEWSL, $end_of_stmt.t); }
    | (lbl=label)?  
      EVENTWAIT LPAREN variable 
      (COMMA event_wait_spec_list {hasEWSL = true;} )? RPAREN
      end_of_stmt
        { MFPA.event_wait_stmt(lbl, 
              $EVENTWAIT, null, hasEWSL, $end_of_stmt.t); }
    ; 
*/

/*
 * R1173:  event wait spec
 */
/*
event_wait_spec
    : until_spec
        { MFPA.event_wait_spec(
              MFPUtils.EWS.UNTIL); }
    | sync_stat
        { MFPA.event_wait_spec(
              MFPUtils.EWS.SYNC); }
    ;

event_wait_spec_list
@init{
  int numEWS = 1;
}
@after{
  MFPA.event_wait_spec_list(numEWS);
}
    : event_wait_spec 
      ( COMMA event_wait_spec {numEWS++;} )*
    ;
*/

/*
 * R1174: until spec
 *  CIVL: scalar_int_expr replaced as expr
 */
/*
until_spec
    : UNTIL_COUNT EQUALS expr
        { MFPA.until_spec($UNTIL_COUNT); } 
    ;

*/

/*
 * R1175: form team stmt
 */
/*
form_team_stmt
@init{
  boolean hasTFSL = false;
}
@after{
  checkForInclude();
}
    : (lbl=label)?  
      FORM TEAM LPAREN expr COMMA variable 
      (COMMA form_team_spec_list {hasTFSL = true;} )? RPAREN
      end_of_stmt
        { MFPA.form_team_stmt(lbl, 
              $FORM, $TEAM, hasTFSL, $end_of_stmt.t); }
    | (lbl=label)?  
      FORMTEAM LPAREN expr COMMA variable 
      (COMMA form_team_spec_list {hasTFSL = true;} )? RPAREN
      end_of_stmt
        { MFPA.form_team_stmt(lbl, 
              $FORMTEAM, null, hasTFSL, $end_of_stmt.t); }
    ;
*/

/*
 * R1176: team number
 *  CIVL: inlined as expr
 */

/*
 * R1177: team variable
 *  CIVL: inlined as variable
 */

/*
 * R1178: form team spec
 *  CIVL: scalar_int_expr inlined as expr
 */
/*
form_team_spec
    : NEW_INDEX EQUALS expr
        { MFPA.form_team_spec($NEW_INDEX); }
    | sync_stat
        { MFPA.form_team_spec(null); }
    ;
    
form_team_spec_list
@init{
  int numFTS = 1;
}
@after{
  MFPA.form_team_spec_list(numFTS);
}
    : form_team_spec 
      ( COMMA form_team_spec {numFTS++;} )*
    ;
*/

/*
 * R1179: lock stmt
 *   OFP: lock_variable replaced by expr
 */
/*
lock_stmt
@init{
  boolean hasLSL = false;
}
@after{
  checkForInclude();
}
    : (lbl=label)?  
      LOCK LPAREN expr 
      (COMMA lock_stat_list {hasLSL=true;})? RPAREN
      end_of_stmt
        { MFPA.lock_stmt(lbl, 
              $LOCK, $end_of_stmt.t, hasLSL); }
    ;
*/

/*
 * R1180: lock stat
 *   OFP: expr is a scalar-logical-variable
 */
/*
lock_stat 
    : ACQUIRED_LOCK EQUALS expr
        { MFPA.lock_stat($ACQUIRED_LOCK); }
    | sync_stat
        { MFPA.lock_stat(null); }
    ;

lock_stat_list
@init{
  int numLS =0;
}
@after{
  MFPA.lock_stat_list(numLS);
}
    : lock_stat
      ( COMMA lock_stat {numLS++;} )*
    ;
*/

/*
 * R1181: unlcok stmt
 * R1182: lock variable
 *   OFP: lock_variable replaced by expr
 */
/*
unlock_stmt
@init{
  boolean hasSSL = false;
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      UNLOCK LPAREN expr 
      (COMMA sync_stat_list {hasSSL=true;})? RPAREN
      end_of_stmt
        { MFPA.unlock_stmt(lbl, 
              $UNLOCK, $end_of_stmt.t, hasSSL);}
    ;
*/

/*
 * R1201: io unit
 *   OFP: file_unit_number replaced by expr
 *        internal_file_variable is expr so deleted
 */
io_unit
@after {
  MFPA.io_unit(asterisk);
}
    : expr
    | asterisk=ASTERISK
    ;

/*
 * R1202: file until number
 *   OFP: scalar_int_expr replaced by expr
 */
file_unit_number
@after{
  MFPA.file_unit_number();
}
    : expr
    ;

/*
 * R1203: internal file variable
 *   OFP: is char_variable inlined (and then deleted in rule 1201)
 */

/*
 * R1204: open stmt
 */
open_stmt
@init{
}
@after{
  checkForInclude();
}
    : (lbl=label)?  
      OPEN LPAREN connect_spec_list RPAREN 
      end_of_stmt
        { MFPA.open_stmt(lbl, 
              $OPEN, $end_of_stmt.t);}
    ;

/*
 * R1205: connect spec
 *   OFP: check expr type with identifier
 *        {'UNIT','ACCESS','ACTION','ASYNCHRONOUS','BLANK',
 *         'DECIMAL', 'DELIM','ENCODING', 'FILE','FORM', 'PAD',
 *         'POSITION','RECL','ROUND','SIGN','STATUS' } are expr
 *        {'IOMSG','IOSTAT'} are variables
 *        {'ERR'} is DIGIT_STR
 */
connect_spec
    : expr
        { MFPA.connect_spec(null); }
    | IDENT EQUALS expr
        { MFPA.connect_spec($IDENT); }
    ;

connect_spec_list
@init{
  int numCS = 1;
}
@after{
  MFPA.connect_spec_list(numCS);
}
    : connect_spec
      ( COMMA connect_spec {numCS++;} )*
    ;

/*
 * R1206: file name expr
 *   OFP: was scalar_default_char_expr inlined as expr 
 */

/*
 * R1207: iomsg variable
 *   OFP: inlined as scalar_default_char_variable in 
 *        rule 1205, 1209, 1213, 1222, 1226, and 1228
 */

/*
 * R1208:  close stmt
 */
close_stmt
@init{
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      CLOSE LPAREN close_spec_list RPAREN 
      end_of_stmt 
        { MFPA.close_stmt(lbl, 
              $CLOSE, $end_of_stmt.t);}
    ;

/*
 * R1209: close spec
 *   OFP: IDENT is in {'UNIT','IOSTAT','IOMSG','ERR','STATUS'}
 *        file_unit_number, scalar_int_variable, iomsg_variable, 
 *        and label replaced by expr
 */
close_spec
    : expr
        { MFPA.close_spec(null); }
    | IDENT EQUALS expr
        { MFPA.close_spec($IDENT); }
    ;

close_spec_list
@init{
  int numCS = 1;
}
    : close_spec
      ( COMMA close_spec {numCS++;} )*
        { MFPA.close_spec_list(numCS); }
    ;

/*
 * R1210: read stmt
 */
read_stmt
options{k=3;}
@init{
  boolean hasIIL = false;
}
@after{
  checkForInclude();
}
    : ((label)? READ LPAREN) =>
          (lbl=label)?  
          READ LPAREN io_control_spec_list RPAREN
          ( input_item_list {hasIIL=true;})?
          end_of_stmt
            { MFPA.read_stmt(lbl, 
                  $READ, $end_of_stmt.t, hasIIL);}
    | ((label)? READ) =>
          (lbl=label)?  
          READ format 
          ( COMMA input_item_list {hasIIL=true;} )? 
          end_of_stmt
            { MFPA.read_stmt(lbl, 
                  $READ, $end_of_stmt.t, hasIIL);}
    ;

/*
 * R1211: write stmt
 */
write_stmt
@init{
  boolean hasOIL=false;
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      WRITE LPAREN io_control_spec_list RPAREN
      ( output_item_list {hasOIL=true;} )?
      end_of_stmt
        { MFPA.write_stmt(lbl, 
              $WRITE, $end_of_stmt.t, hasOIL); }
    ;

/*
 * R1212: print stmt
 */
print_stmt
@init{
  boolean hasOIL =false;
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      PRINT format 
      ( COMMA output_item_list {hasOIL=true;})? 
      end_of_stmt
        { MFPA.print_stmt(lbl, 
              $PRINT, $end_of_stmt.t, hasOIL); }
    ;

/*
 * R1213: io control spec
 * R1214: id variable
 *   OFP: check expr type with identifier
 *        io_unit and format are both (expr|'*') so combined
 *  CIVL: id variable was scalar int variable combined in expr
 */
io_control_spec
    : expr
        { MFPA.io_control_spec(null, null); 
        } /* Optional {'UNIT', 'FMT', 'NML'} */
    | ASTERISK
        { MFPA.io_control_spec(null, $ASTERISK); 
        } /* Optional {'UNIT', 'FMT', 'NML'} */
    | IDENT EQUALS ASTERISK
        { MFPA.io_control_spec($IDENT, $ASTERISK); 
        } /* {'UNIT','FMT'} */ 
    | IDENT EQUALS expr
        { MFPA.io_control_spec($IDENT, null); 
        } /* {'UNIT', 'FMT', 'ADVANCE','ASYNCHRONOUS',   */
          /*  'BLANK','DECIMAL','DELIM','PAD','POS',     */
          /*  'REC','ROUND','SIGN'} are expr             */
          /* {'ID','IOMSG',IOSTAT','SIZE'} are variables */
          /* {'END','EOR','ERR'} are labels              */
          /* {'NML'} is IDENT}                           */
    ;

io_control_spec_list
@init{
  int numICS = 1;
}
@after{
  MFPA.io_control_spec_list(numICS);
}
    : io_control_spec
      ( COMMA io_control_spec {numICS++;} )*
    ;

/*
 * R1215: format
 *   OFP: default_char_expr replaced by expr
 *        label replaced by DIGIT_STR is expr so deleted
 */
format
@after {
  MFPA.format(asterisk);
}
    : expr
    | asterisk=ASTERISK
    ;

/*
 * R1216: input item
 */
input_item
@after {
  MFPA.input_item();
}
    : variable
    | io_implied_do
    ;

input_item_list
@init{
  int numII = 1;
}
@after{
  MFPA.input_item_list(numII);
}
    : input_item
      ( COMMA input_item {numII++;} )*
    ;

/*
 * R1217: output item
 */
output_item
options{backtrack=true;}
@after{
  MFPA.output_item();
}
    : expr
    | io_implied_do
    ;

output_item_list
@init{
  int numOI = 1;
}
@after{
  MFPA.output_item_list(numOI);
}
    : output_item
      ( COMMA output_item {numOI++;} )*
    ;

/*
 * R1218:  io implied do
 */
io_implied_do
    : LPAREN io_implied_do_object io_implied_do_suffix RPAREN
        { MFPA.io_implied_do(); }
    ;

/*
 * R1219: io implied do object
 *   OFP: expr in output_item can be variable in input_item so input_item deleted
 */
io_implied_do_object
    : output_item 
      { MFPA.io_implied_do_object(); }
    ;

io_implied_do_suffix
options{backtrack=true;}
    : COMMA io_implied_do_object io_implied_do_suffix
    | COMMA io_implied_do_control
    ;

/*
 * R1220: io implied do control
 *   OFP: scalar_int_expr replaced by expr
 */
io_implied_do_control
@init{
  boolean hasStrd=false;
}
    : IDENT EQUALS expr COMMA expr 
      ( COMMA expr {hasStrd=true;})? 
        { MFPA.io_implied_do_control($IDENT, hasStrd); }
    ;

/*
 * R1221: dtv type spec
 *   OFP: Not used
 */
dtv_type_spec
    : TYPE LPAREN derived_type_spec RPAREN
        { MFPA.dtv_type_spec($TYPE); }
    | CLASS LPAREN derived_type_spec RPAREN
        { MFPA.dtv_type_spec($CLASS); }
    ;

/*
 * R1222: wait stmt
 */
wait_stmt
@init{
}
@after{
  checkForInclude();
}
    : (lbl=label)?  
      WAIT LPAREN wait_spec_list RPAREN
      end_of_stmt
        { MFPA.wait_stmt(lbl, 
              $WAIT, $end_of_stmt.t); }
    ;

/*
 * R1223: wait spec
 *   OFP: {'UNIT','END','EOR','ERR','ID','IOMSG','IOSTAT'}
 *        file_unit_number, scalar_int_variable, iomsg_variable, 
 *        and label replaced by expr
 */
wait_spec
    : expr
        { MFPA.wait_spec(null); }
    | IDENT EQUALS expr
        { MFPA.wait_spec($IDENT); }
    ;

wait_spec_list
@init{
  int numWS = 1;
}
@after{
  MFPA.wait_spec_list(numWS);
}
    : wait_spec
      ( COMMA wait_spec {numWS++;} )*
    ;

/*
 * R1224:  backspace stmt
 */
backspace_stmt
options {k=3;}
@init{
}
@after{
  checkForInclude();
}
    : ((label)? BACKSPACE LPAREN) =>
          (lbl=label)?  
          BACKSPACE LPAREN position_spec_list RPAREN
          end_of_stmt
            { MFPA.backspace_stmt(lbl, 
                  $BACKSPACE, $end_of_stmt.t, true);}
    | ((label)? BACKSPACE) =>
          (lbl=label)?  
          BACKSPACE file_unit_number 
          end_of_stmt
            { MFPA.backspace_stmt(lbl, 
                  $BACKSPACE, $end_of_stmt.t, false);}
    ;

/*
 * R1225:  endfile stmt
 */
endfile_stmt
options{k=3;}
@init{
}
@after{
  checkForInclude();
}
    : ((label)? END FILE LPAREN) =>
          (lbl=label)?  
          END FILE LPAREN position_spec_list RPAREN 
          end_of_stmt
            { MFPA.endfile_stmt(lbl, 
                  $END, $FILE, $end_of_stmt.t, true);}
    | ((label)? END FILE) => 
          (lbl=label)?  
          END FILE file_unit_number 
          end_of_stmt
            { MFPA.endfile_stmt(lbl, 
                  $END, $FILE, $end_of_stmt.t, false);}
    ;

/*
 * R1226:  rewind stmt
 */
rewind_stmt
options{k=3;}
@init{
}
@after{
  checkForInclude();
}
    : ((label)? REWIND LPAREN) => 
          (lbl=label)?  
          REWIND LPAREN position_spec_list RPAREN 
          end_of_stmt
            { MFPA.rewind_stmt(lbl, 
                  $REWIND, $end_of_stmt.t, true);}
    | ((label)? REWIND) =>
          (lbl=label)? 
          REWIND file_unit_number 
          end_of_stmt
            { MFPA.rewind_stmt(lbl,
                  $REWIND, $end_of_stmt.t, false);}
    ;

/*
 * R1227: position spec
 *   OFP: {'UNIT','IOSTAT','IOMSG','ERR'}
 *        file_unit_number, scalar_int_variable, iomsg_variable, 
 *        label replaced by expr
 */
position_spec
    : expr
        { MFPA.position_spec(null); }
    | IDENT EQUALS expr
        { MFPA.position_spec($IDENT); }
    ;

position_spec_list
@init{
  int numPS = 1;
}
@after{
  MFPA.position_spec_list(numPS);
}
    : position_spec 
      ( COMMA position_spec {numPS++;} )*
    ;

/*
 * R1228: flush stmt
 */
flush_stmt
options {k=3;}
@after{
  checkForInclude();
}
    : ((label)? FLUSH LPAREN) => 
          (lbl=label)?  
          FLUSH LPAREN flush_spec_list RPAREN 
          end_of_stmt 
            { MFPA.flush_stmt(lbl, 
                  $FLUSH, $end_of_stmt.t, true); }
    | ((label)? FLUSH) => 
         (lbl=label)? 
         FLUSH file_unit_number 
         end_of_stmt
           { MFPA.flush_stmt(lbl, 
                 $FLUSH, $end_of_stmt.t, false);}
    ;

/*
 * R1229: flush spec
 *   OFP: {'UNIT','IOSTAT','IOMSG','ERR'}
 *        file_unit_number, scalar_int_variable, iomsg_variable,
 *        and label replaced by expr
 */
flush_spec
    : expr
        { MFPA.flush_spec(null); }
    | IDENT EQUALS expr 
        { MFPA.flush_spec($IDENT); }
    ;

flush_spec_list
@init{
  int numFS = 1;
}
@after{
  MFPA.flush_spec_list(numFS);
}
    : flush_spec
      ( COMMA flush_spec {numFS++;} )*
    ;

/*
 * R1230: inquire stmt
 *  CIVL: scalar_int_variable replaced as expr
 */
inquire_stmt
@init{
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      INQUIRE LPAREN inquire_spec_list RPAREN 
      end_of_stmt
        { MFPA.inquire_stmt(lbl, 
              $INQUIRE, null, $end_of_stmt.t, false);}
    | (lbl=label)? 
      M_INQUIRE_STMT_2 INQUIRE LPAREN 
      IDENT /* 'IOLENGTH' */ EQUALS expr 
      RPAREN output_item_list 
      end_of_stmt
        { MFPA.inquire_stmt(lbl, 
              $INQUIRE, $IDENT, $end_of_stmt.t, true);}
    ;

/*
 * R1231: inquire spec
 *   OFP: {'UNIT','FILE'} are expr
 *        {'ACCESS', 'ACTION', 'ASYNCHRONOUS', 'BLANK', 'DECIMAL',
 *         'DELIM', 'DIRECT', 'ENCODING', 'ERR', 'EXIST', 'FORM', 
 *         'FORMATTED', 'ID', 'IOMSG', 'IOSTAT', 'NAME', 'NAMED', 
 *         'NEXTREC', 'NUMBER', 'OPENED', 'PAD', 'PENDING', 'POS', 
 *         'POSITION', 'READ', 'READWRITE', 'RECL', 'ROUND', 
 *         'SEQUENTIAL', 'SIGN', 'SIZE', 'STREAM', 'UNFORMATTED', 
 *         'WRITE'} are variable
 *        file_name_expr and file_unit_number replaced by expr
 *        scalar_default_char_variable replaced by designator
 */
inquire_spec
    : expr
        { MFPA.inquire_spec(null); }
    | IDENT EQUALS expr
        { MFPA.inquire_spec($IDENT); }
    ;

inquire_spec_list
@init{
  int numIS = 1;
}
@after{
  MFPA.inquire_spec_list(numIS);
}
    : inquire_spec
      ( COMMA inquire_spec {numIS++;} )*
    ;

/*
 * R1301: format stmt
 *   OFP: label is required. accept as optional for error report.
 */
format_stmt
@init{
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      FORMAT format_specification 
      end_of_stmt 
        { MFPA.format_stmt(lbl, 
              $FORMAT, $end_of_stmt.t); }
    ;

/*
 * R1302: format specification
 */
format_specification
@init{
  boolean hasFIL=false;
  boolean hasUFI=false;
}
    : LPAREN ( format_item_list {hasFIL=true;})? 
      (COMMA unlimited_format_item {hasUFI=true;})? RPAREN 
        { MFPA.format_specification(hasFIL, hasUFI); }
    ;

/*
 * R1303: format items
 *   OFP: r replaced by int_literal_constant replaced by  
 *          char_literal_constant replaced by CHAR_CONST
 *        char_string_edit_desc replaced by CHAR_CONST
 */
format_item
@init{
  Token descOrDigit=null; 
  boolean hasFIL = false;
}
    : M_DATA_EDIT_DESC 
        { MFPA.format_item($M_DATA_EDIT_DESC, hasFIL); }
    | M_CTRL_EDIT_DESC
        { MFPA.format_item($M_CTRL_EDIT_DESC, hasFIL);}
    | M_CSTR_EDIT_DESC
        { MFPA.format_item($M_CSTR_EDIT_DESC, hasFIL);}
    | (DIGIT_STR {descOrDigit=$DIGIT_STR;} )? 
      LPAREN format_item_list RPAREN
        { MFPA.format_item(descOrDigit, hasFIL);}
    ;

/*   OFP: the comma is not always required.  */
format_item_list
@init{
  int numFI = 1;
}
@after{
  MFPA.format_item_list(numFI);
}
    : format_item 
      ( (COMMA)? format_item {numFI++;} )*
    ;

/*
 * R1305: unlimited format item
 */
unlimited_format_item
    : ASTERISK LPAREN format_item_list RPAREN
        { MFPA.unlimited_format_item(); }
    ;

/*
 *   OFP: Rules below in the comment are combined into rule 1303
 * R1304: format item
 *        format_item_list
 *        char_string_edit_desc replaced by CHAR_CONST
 * R1306: r
 *        inlined in rule 1301 and 1313 as int_literal_constant 
 *        (then as DIGIT_STRING)
 * R1307: data edit spec
 *        
 * R1308: w
 * R1309: m
 * R1310: d
 * R1311: e
 *        w,m,d,e replaced by int_literal_constant replaced by DIGIT_STR
 * R1312: v
 *        inlined as signed_int_literal_constant in v_list replaced by (PLUS or MINUS) DIGIT_STR
 * R1313: control edit spec
 *        inlined/combined in rule 1307 and data_plus_control_edit_desc
 *        r replaced by int_literal_constant replaced by DIGIT_STR
 *        k replaced by signed_int_literal_constant replaced by (PLUS|MINUS)? DIGIT_STR
 *        position_edit_desc inlined
 *        sign_edit_desc replaced by T_ID_OR_OTHER was {'SS','SP','S'}
 *        blank_interp_edit_desc replaced by T_ID_OR_OTHER was {'BN','BZ'}
 *        round_edit_desc replaced by T_ID_OR_OTHER was {'RU','RD','RZ','RN','RC','RP'}
 *        decimal_edit_desc replaced by T_ID_OR_OTHER was {'DC','DP'}
 *        leading T_ID_OR_OTHER alternates combined with data_edit_desc in data_plus_control_edit_desc
 * R1314: k
 *        inlined in rule 1313 as signed_int_literal_constant
 *        n in rule 1314 was replaced by int_literal_constant replaced by DIGIT_STR
 * C1009: k shall not have a kind parameter specified for it
 * R1315: position edit spec
 *        inlined in rule 1313
 * R1316: n
 *        inlined in rule 1315 as int_literal_constant (is DIGIT_STR, see C1311)
 * C1311: a kind parameter shall not be specified for k.
 * R1317: sign edit desc
 *        inlined in rule 1313 as T_ID_OR_OTHER was {'SS','SP','S'}
 * R1318: blank interp edit desc
 *        inlined in rule 1313 as T_ID_OR_OTHER was {'BN','BZ'}
 * R1319: round edit desc
 *        inlined in rule 1313 as T_ID_OR_OTHER was {'RU','RD','RZ','RN','RC','RP'}
 * R1320: decimal edit desc
 *        inlined in rule 1313 as T_ID_OR_OTHER was {'DC','DP'}
 * R1321: char string edit spec
 *        was char_literal_constant inlined in rule 1313 as CHAR_CONST
 */

/*
 * R1401: main program
 *   OFP: A starting rule as the entry point.
 *        'program_stmt' (R1402) made to be non-optional, 
 *        then an empty program with a single 'end' will 
 *        not be ambiguous.
 */
main_program
@init{
  boolean hasEP = false;
  boolean hasISP = false;
}
@after{
  MFPA.main_program(hasEP, hasISP);
}
    : program_stmt
      specification_part
      ( execution_part {hasEP = true;} )?
      ( internal_subprogram_part {hasISP = true;} )?
      end_program_stmt
    ;

/*
 * R1402: program stmt
 *   OFP: IDENT inlined for program_name
 */
program_stmt			
@init{
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      PROGRAM IDENT 
      end_of_stmt
        { MFPA.program_stmt(lbl, 
              $PROGRAM, $IDENT, $end_of_stmt.t); }
    ;

/*
 * R1403: end program stmt
 *   OFP: IDENT inlined for program_name
 */
end_program_stmt
@init{
  Token id = null;
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      END PROGRAM (IDENT {id=$IDENT;})? 
      end_of_stmt
        { MFPA.end_program_stmt(lbl, 
              $END, $PROGRAM, id, $end_of_stmt.t); }
    | (lbl=label)? 
      END 
      end_of_stmt
        { MFPA.end_program_stmt(lbl, 
              $END, null, null, $end_of_stmt.t); }
    ;

/*
 * R1404: module
 * C1403: A module specification-part shall not contain a 
 *        stmt-function-stmt, an entry-stmt or a format-stmt
 *   OFP: specification_part made non-optional 
 *        to remove END ambiguity (as can be empty)
 */
module
@after {
  MFPA.module();
}
    : module_stmt
      specification_part
      ( module_subprogram_part )?
      end_module_stmt
    ;

/*
 * R1405: module stmt
 */
module_stmt
@init{
  Token id = null;
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      MODULE ( IDENT {id=$IDENT;} )?
      end_of_stmt
        { MFPA.module_stmt(lbl, 
              $MODULE, id, $end_of_stmt.t);}
    ;

/*
 * R1406: end module stmt
 */
end_module_stmt
@init{
  Token id = null;
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      END MODULE (IDENT {id=$IDENT;})?
      end_of_stmt
        { MFPA.end_module_stmt(lbl, 
              $END, $MODULE, id, $end_of_stmt.t);}
    | (lbl=label)?  END 
      end_of_stmt
        { MFPA.end_module_stmt(lbl, 
              $END, null, id, $end_of_stmt.t);}
    ;

/*
 * R1407: module subprogram part
 */
module_subprogram_part
@init{
  int numMS = 0;
}
@after{
  MFPA.module_subprogram_part(numMS);
}
    : contains_stmt
      ( module_subprogram {numMS++;} )*
    ;

/*
 * R1408: module subprogram
 */
module_subprogram
options {backtrack=true;}
@init{
  boolean hasPref = false;
}
@after{
  MFPA.module_subprogram(hasPref);
}
    : (prefix {hasPref=true;})? function_subprogram
    | subroutine_subprogram
    | separate_module_subprogram
    ;

/*
 * R1409: use stmt
 */
use_stmt
@init{
  boolean hasMN = false; 
  boolean hasRL = false;
  boolean hasOL = false;
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      USE ((COMMA module_nature {hasMN=true;})? COLON_COLON)? 
      IDENT (COMMA rename_list {hasRL=true;})? 
      end_of_stmt
        { MFPA.use_stmt(lbl, 
              $USE, $IDENT, null, $end_of_stmt.t, hasMN, hasRL, hasOL);}
    | (lbl=label)? 
      USE ((COMMA module_nature {hasMN=true;})? COLON_COLON)? 
      IDENT COMMA ONLY COLON (only_list {hasOL=true;})? 
      end_of_stmt
        { MFPA.use_stmt(lbl, 
              $USE, $IDENT, $ONLY, $end_of_stmt.t, hasMN, hasRL, hasOL);}
    ;

/*
 * R1410: module nature
 */
module_nature
    : INTRINSIC 
          { MFPA.module_nature($INTRINSIC); }
    | NON_INTRINSIC
          { MFPA.module_nature($NON_INTRINSIC); }
    ;

/*
 * R1411: rename
 * R1414: local defined operator
 * R1415: use defined operator
 *   OFP: DEFINED_OP inlined for local_defined_operator 
 *          and use_defined_operator
 *        IDENT inlined for local_name and use_name
 */
rename
    : id1=IDENT EQ_GT id2=IDENT
        { MFPA.rename(id1, id2, null, null, null, null); }
    | op1=OPERATOR LPAREN defOp1=DEFINED_OP RPAREN 
      EQ_GT op2=OPERATOR LPAREN defOp2=DEFINED_OP RPAREN
        { MFPA.rename(null, null, op1, defOp1, op2, defOp2); } 
    ;

rename_list
@init{
  int numRn = 1;
}
@after{
  MFPA.rename_list(numRn);
}
    : rename
      ( COMMA rename {numRn++;} )*
    ;

/*
 * R1412: only
 * R1413: only use stmt
 *   OFP: IDENT inlined for only_use_name
 *        generic_spec can be IDENT so IDENT deleted
 */
only
@init{
  boolean hasGS = false;
  boolean hasRn = false;
}
@after {
  MFPA.only(hasGS, hasRn);
}
    : generic_spec {hasGS=true;}
    | rename {hasRn=true;}
    ;

only_list
@init{
  int numO = 1;
}
@after{
  MFPA.only_list(numO);
}
    : only
      ( COMMA only {numO++;} )*
    ;

/*
 * R1416:  submodule
 *   OFP: specification_part made non-optional 
 *        to remove END ambiguity (as can be empty)
 */
submodule
@init{
  boolean hasMSP = false;
}
@after{
  MFPA.submodule(hasMSP);
}
    : submodule_stmt
      specification_part
      ( module_subprogram_part {hasMSP=true;} )?
      end_submodule_stmt
    ;

/*
 * R1417: submodule stmt
 *  CIVL: name replaced as IDENT
 */
submodule_stmt
@init{
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      SUBMODULE LPAREN parent_identifier RPAREN IDENT
      end_of_stmt
        { MFPA.submodule_stmt(lbl, 
              $SUBMODULE, $IDENT, $end_of_stmt.t);}
    ;

/*
 * R1418: parent identifier
 */
parent_identifier
    : ancestor=IDENT
      ( COLON parent=IDENT)?
        { MFPA.parent_identifier(ancestor, parent); }
    ;

/*
 * R1419: end submodule stmt
 */
end_submodule_stmt
@after{
  checkForInclude();
}
    : (lbl=label)?
      END (smod=SUBMODULE (sname=IDENT)?)?
      end_of_stmt
        { MFPA.end_submodule_stmt(lbl, 
              $END, smod, sname, $end_of_stmt.t);}
    ;

/*
 * R1420: block data
 *   OFP: specification_part made non-optional 
 *        to remove END ambiguity (as can be empty).
 */
block_data
@after {
  MFPA.block_data();
}
    : block_data_stmt
      specification_part
      end_block_data_stmt
    ;

/*
 * R1421:  block data stmt
 */
block_data_stmt
@init{
  Token id = null;
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      BLOCK DATA (IDENT {id=$IDENT;})? 
      end_of_stmt
        { MFPA.block_data_stmt(lbl, 
              $BLOCK, $DATA, id, $end_of_stmt.t);}
    | (lbl=label)? 
      BLOCKDATA (IDENT {id=$IDENT;})?
      end_of_stmt
        { MFPA.block_data_stmt(lbl, 
              $BLOCKDATA, null, id, $end_of_stmt.t);}
    ;

/*
 * R1422:  end block data stmt
 */
end_block_data_stmt
@init{
  Token id = null;
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      END BLOCK DATA (IDENT {id=$IDENT;})? 
      end_of_stmt
        { MFPA.end_block_data_stmt(lbl, 
              $END, $BLOCK, $DATA, id, $end_of_stmt.t);}
    | (lbl=label)? 
      END BLOCKDATA (IDENT {id=$IDENT;})?
      end_of_stmt
        { MFPA.end_block_data_stmt(lbl, 
              $END, $BLOCKDATA, null, id, $end_of_stmt.t);}
    | (lbl=label)? 
      END end_of_stmt
        { MFPA.end_block_data_stmt(lbl, 
              $END, null, null, id, $end_of_stmt.t);}
    ;

/*
 * R1501:  interface block
 */
interface_block
@after {
  MFPA.interface_block();
}
    : interface_stmt
      ( interface_specification )*
      end_interface_stmt
    ;

/*
 * R1502:  interface specification
 */
interface_specification
@after {
  MFPA.interface_specification();
}
    : interface_body
    | procedure_stmt
    ;

/*
 * R1503: interface stmt
 *   OFP: the last argument to the action specifies 
 *        whether this is an abstract interface or not.
 */
interface_stmt
@init{
  boolean hasGS = false;
}
@after{
  checkForInclude();
}
    : (lbl=label)?  
      INTERFACE (generic_spec {hasGS=true;})? 
      end_of_stmt
        { MFPA.interface_stmt(lbl, 
              null, $INTERFACE, $end_of_stmt.t, hasGS);}
    | (lbl=label)? 
      ABSTRACT INTERFACE 
      end_of_stmt
        { MFPA.interface_stmt(lbl, 
              $ABSTRACT, $INTERFACE, $end_of_stmt.t, hasGS);}
    ;

/*
 * R1504: end interface stmt
 */
end_interface_stmt
@init{
  boolean hasGS = false;
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      END INTERFACE ( generic_spec {hasGS=true;})? 
      end_of_stmt
        { MFPA.end_interface_stmt(lbl, 
              $END, $INTERFACE, $end_of_stmt.t, hasGS);}
    ;

/*
 * R1505: interface body
 *   OFP: the last argument to the action specifies 
 *        whether this is an abstract interface or not.
 *  CIVL: 1st arg for isFunc
 */
interface_body
options {backtrack=true;}
@init{
  boolean hasPref = false;
}
    : (prefix {hasPref=true;})? 
      function_stmt specification_part 
      end_function_stmt
        { MFPA.interface_body(true, hasPref); }
    | subroutine_stmt specification_part 
      end_subroutine_stmt
        { MFPA.interface_body(false, hasPref);}
    ;

/*
 * R1506: procedure stmt
 * R1507: specific procedure
 *   OFP: generic_name_list substituted for specific procedur list
 */
procedure_stmt
@init{
  Token mod = null;
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      ( MODULE {mod=$MODULE;})? 
      PROCEDURE generic_name_list 
      end_of_stmt
        { MFPA.procedure_stmt(lbl, 
              mod, $PROCEDURE, $end_of_stmt.t);}
    ;

/*
 * R1508: generic spec
 *   OFP: IDENT inlined for generic_name
 */
generic_spec
    : IDENT
        { MFPA.generic_spec(null, $IDENT, 
              MFPUtils.GS.NAME);}
    | OPERATOR LPAREN defined_operator RPAREN
        { MFPA.generic_spec($OPERATOR, null, 
              MFPUtils.GS.OPERATOR);}
    | ASSIGNMENT LPAREN EQUALS RPAREN
        { MFPA.generic_spec($ASSIGNMENT, null, 
              MFPUtils.GS.ASSIGNMENT);}
    | defined_io_generic_spec
        { MFPA.generic_spec(null, null,
              MFPUtils.GS.SPEC); }
    ;

/*
 * R1509: defined io generic spec
 */
defined_io_generic_spec
    : READ LPAREN FORMATTED RPAREN
        { MFPA.defined_io_generic_spec($READ, $FORMATTED,
              MFPUtils.DIGS.FMT_R);}
    | READ LPAREN UNFORMATTED RPAREN
        { MFPA.defined_io_generic_spec($READ, $UNFORMATTED, 
              MFPUtils.DIGS.UFMT_R);}
    | WRITE LPAREN FORMATTED RPAREN
        { MFPA.defined_io_generic_spec($WRITE, $FORMATTED,
              MFPUtils.DIGS.FMT_W);}
    | WRITE LPAREN UNFORMATTED RPAREN
        { MFPA.defined_io_generic_spec($WRITE, $UNFORMATTED,
              MFPUtils.DIGS.UFMT_W);}
    ;

/*
 * R1510: generic stmt
 *  OFP:  generic_name_list substituted for specific_procedure_list
 */
generic_stmt
@init{
  boolean hasAS = false;
}
    : GENERIC ( COMMA access_spec {hasAS=true;})?
      COLON_COLON generic_spec EQ_GT generic_name_list
        { MFPA.generic_stmt($GENERIC, hasAS);}
    ;

/*
 * R1511: external stmt
 */
external_stmt
@init{
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      EXTERNAL ( COLON_COLON )? generic_name_list 
      end_of_stmt
        { MFPA.external_stmt(lbl, 
              $EXTERNAL, $end_of_stmt.t);}
    ;

/*
 * R1512: procedure declaration stmt
 */
procedure_declaration_stmt
@init{
  boolean hasPI = false; 
  int numPD = 0;
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      PROCEDURE LPAREN ( proc_interface {hasPI=true;})? RPAREN
      ( ( COMMA proc_attr_spec {numPD++;})* COLON_COLON )?
      proc_decl_list 
      end_of_stmt
        { MFPA.procedure_declaration_stmt(lbl, 
              $PROCEDURE, $end_of_stmt.t, hasPI, numPD);}
    ;

/*
 * R1513: proc interface
 *   OFP: IDENT inlined for interface_name
 */
proc_interface
    : IDENT { MFPA.proc_interface($IDENT); }
    | declaration_type_spec { MFPA.proc_interface(null); }
    ;

/*
 * R1514:  proc attr spec
 */
proc_attr_spec
    : access_spec 
        { MFPA.proc_attr_spec(null, 
              MFPUtils.ATTR_ACCESS); }
    | language_binding_spec
        { MFPA.proc_attr_spec(null, 
              MFPUtils.ATTR_BIND_C); }
    | INTENT LPAREN intent_spec RPAREN
        { MFPA.proc_attr_spec($INTENT, 
              MFPUtils.ATTR_INTENT); }
    | OPTIONAL
        { MFPA.proc_attr_spec($OPTIONAL, 
              MFPUtils.ATTR_OPTIONAL); }
    | POINTER
        { MFPA.proc_attr_spec($POINTER, 
              MFPUtils.ATTR_POINTER); }
    | PROTECTED
        { MFPA.proc_attr_spec($PROTECTED,
              MFPUtils.ATTR_PROTECTED); }
    | SAVE
        { MFPA.proc_attr_spec($SAVE, 
              MFPUtils.ATTR_SAVE); }
/* features inherited from OFP */
    | proc_attr_spec_extension
        { MFPA.proc_attr_spec(null, 
              MFPUtils.ATTR_OTHER); }
    ;

proc_attr_spec_extension
    : NO_LANG_EXT
    ;

/*
 * R1515: proc decl
 *   OFP: IDENT inlined for procedure_entity_name
 */
proc_decl
@init{
  boolean hasPPI = false;
}
    : IDENT ( EQ_GT proc_pointer_init {hasPPI=true;} )?
        { MFPA.proc_decl($IDENT, hasPPI); }
    ;

proc_decl_list
@init{
  int numPd = 1;
}
@after{
  MFPA.proc_decl_list(numPd);
}
    : proc_decl
      ( COMMA proc_decl {numPd++;} )*
    ;

/*
 * R1516: interface name
 *   OFP: was name inlined as IDENT
 */

/*
 * R1517: proc pointer init
 * R1518: initial proc target
 *  CIVL: initial proc target inlined as IDENT
 */
proc_pointer_init
    : null_init
        { MFPA.proc_pointer_init(null); }
    | IDENT
        { MFPA.proc_pointer_init($IDENT); }
    ;

/*
 * R1519: intrinsic stmt
 *   OFP: generic_name_list substituted for 
 *          intrinsic_procedure_name_list
 */
intrinsic_stmt
@init{
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      INTRINSIC (COLON_COLON)? generic_name_list 
      end_of_stmt
        { MFPA.intrinsic_stmt(lbl, 
            $INTRINSIC, $end_of_stmt.t);}
    ;

/*
 * R1520: function reference
 *   OFP: replaced by designator_or_func_ref 
 *        to reduce backtracking
 */

/*
 * R1521: call stmt
 * C1525: The procedure-designator shall designate a subroutine.
 */
call_stmt
@init{
  boolean hasAASL = false;
} 
@after{
  checkForInclude();
}
    : (lbl=label)? 
      CALL procedure_designator 
      ( LPAREN 
        ( actual_arg_spec_list {hasAASL=true;})? RPAREN 
      )? 
      end_of_stmt
        { MFPA.call_stmt(lbl, 
              $CALL, $end_of_stmt.t, hasAASL);}
    ;

/*
 * R1522: procedure designator
 *   OFP: must be (IDENT | designator PERCENT IDENT)
 *        IDENT inlined for procedure_name and binding_name
 *          proc_component_ref is variable PERCENT IDENT 
 *          (variable is designator)
 *        data_ref subset of designator 
 *          so data_ref PERCENT IDENT deleted
 *        designator (R603), minus the substring part is data_ref, 
 *          so designator replaced by data_ref
 */
procedure_designator
    : data_ref { MFPA.procedure_designator();}
    ;

/*
 * R1523: actual arg spec
 *   OFP: TODO - delete greedy?
 * R1524: actual arg
 *   OFP: ensure ( expr | designator ending in PERCENT IDENT)
 *        IDENT inlined for procedure_name
 *        expr is a designator (via primary) so variable deleted
 *        designator is a IDENT so IDENT deleted
 *        proc_component_ref is variable PERCENT IDENT can be designator 
 *          so deleted
 * R1525: alt return spec
 *   OFP: inlined as ASTERISK label in rule 1524
 */
actual_arg_spec
@init{
  Token keyword = null;
}
    : (IDENT EQUALS {keyword=$IDENT;})? 
      ( expr 
          { MFPA.actual_arg_spec(keyword, null, null); }
      | ASTERISK lbl=label 
          { MFPA.actual_arg_spec(keyword, $ASTERISK, lbl); }) 
    ;

actual_arg_spec_list
options{greedy=false;}
@init{
  int numAAS = 1;
}
@after{
  MFPA.actual_arg_spec_list(numAAS);
}
    : actual_arg_spec
      ( COMMA actual_arg_spec {numAAS++;} )*
    ;

/*
 * R1526: prefix
 * C1544: shall not specify both PURE and IMPURE.
 * C1545: shall not specify both NON_RECURSIVE and RECURSIVE.
 */
prefix
@init{
  int numPref = 1;
}
@after{
  MFPA.prefix(numPref);
}
    : prefix_spec
      (prefix_spec {numPref++;})*
    ;

/*
 * R1527: prefix spec
 */
prefix_spec
    : declaration_type_spec
        { MFPA.prefix_spec(null, MFPUtils.PFX_TYPE); }
    | ELEMENTAL
        {MFPA.prefix_spec($ELEMENTAL, MFPUtils.PFX_ELEMENTAL);}
    | IMPURE
        {MFPA.prefix_spec($IMPURE, MFPUtils.PFX_IMPURE);}
    | MODULE
        {MFPA.prefix_spec($MODULE, MFPUtils.PFX_MODULE);}
    | NON_RECURSIVE
        {MFPA.prefix_spec($NON_RECURSIVE, MFPUtils.PFX_NON_RECURSIVE);}
    | PURE
        {MFPA.prefix_spec($PURE, MFPUtils.PFX_PURE);}
    | RECURSIVE
        {MFPA.prefix_spec($RECURSIVE, MFPUtils.PFX_RECURSIVE);}
    ;

/*
 * R1528: proc language binding spec
 *  CIVL: inlined as language binding spec in rule 1514
 */

/*
 * R1529: function subprogram
 *   OFP: left factored optional prefix in function_stmt 
 *          from function_subprogram
 *        specification_part made non-optional 
 *          to remove END ambiguity (as can be empty)
 */
function_subprogram
@init {
  boolean hasEP = false;
  boolean hasISP = false;
}
    : function_stmt
      specification_part
      ( execution_part { hasEP=true; })?
      ( internal_subprogram_part { hasISP=true; })?
      end_function_stmt
        { MFPA.function_subprogram(hasEP, hasISP); }
    ;

/*
 * R1530: function stmt
 *   OFP: left factored optional prefix from function_stmt
 *        generic_name_list substituted for dummy_arg_name_list
 */
function_stmt
@init {
  boolean hasGNL=false;
  boolean hasSffx=false;
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      FUNCTION IDENT LPAREN 
      ( generic_name_list {hasGNL=true;})? 
      RPAREN (suffix {hasSffx=true;})? 
      end_of_stmt
        { MFPA.function_stmt(lbl,  $FUNCTION, 
             $IDENT, $end_of_stmt.t, hasGNL, hasSffx);}
    ;

/*
 * R1531: dummy arg stmt
 *   OFP: was name inlined as IDENT
 */

/*
 * R1532: suffix
 *  CIVL: proc_language_binding_spec replaced as language_binding_spec
 *        result_name replaced as IDENT
 */
suffix
@init{
  Token rname = null;
  boolean hasPLBS = false;
}
    : language_binding_spec 
      ( RESULT LPAREN IDENT RPAREN {rname=$IDENT;} )?
        { MFPA.suffix(rname, true); }
    | RESULT LPAREN IDENT RPAREN 
      ( language_binding_spec {hasPLBS = true;} )?
        { MFPA.suffix($IDENT, hasPLBS); }
    ;

/*
 * R1533:  end function stmt
 */
end_function_stmt
@init{
  Token id = null;
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      END FUNCTION (IDENT {id=$IDENT;})?
      end_of_stmt
        { MFPA.end_function_stmt(lbl, 
              $END, $FUNCTION, id, $end_of_stmt.t);}
    | (lbl=label)? 
      END end_of_stmt
        { MFPA.end_function_stmt(lbl, 
              $END, null, id, $end_of_stmt.t);}
    ;

/*
 * R1534:  subroutine subprogram
 *   OFP: specification_part made non-optional 
 *          to remove END ambiguity (as can be empty)
 */
subroutine_subprogram
@init {
  boolean hasEP = false;
  boolean hasISP = false;
}
    : subroutine_stmt
      specification_part
      ( execution_part { hasEP=true; })?
      ( internal_subprogram_part { hasISP=true; })?
      end_subroutine_stmt
        { MFPA.subroutine_subprogram(hasEP, hasISP); }
    ;

/*
 * R1535: subroutine stmt
 *  CIVL: proc_language_binding_spec replaced as language_binding_spec
 *        result_name replaced as IDENT
 */
subroutine_stmt
@init{
  boolean hasPref=false;
  boolean hasDAL=false;
  boolean hasBS=false;
  boolean hasAS=false;
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      (prefix {hasPref=true;})? 
      SUBROUTINE IDENT 
      ( LPAREN (dummy_arg_list {hasDAL=true;})? 
        RPAREN {hasAS=true;}
        (language_binding_spec {hasBS=true;})? 
      )?
      end_of_stmt
        { MFPA.subroutine_stmt(lbl, 
              $SUBROUTINE, $IDENT, $end_of_stmt.t, 
              hasPref, hasDAL, hasBS, hasAS);}
    ;

/*
 * R1536: dummy arg
 *   OFP: IDENT inlined for dummy_arg_name
 */
dummy_arg
options{greedy=false; memoize=false;}
    : IDENT {MFPA.dummy_arg($IDENT);}
    | ASTERISK {MFPA.dummy_arg($ASTERISK);}
    ;

dummy_arg_list
@init{
  int numDA = 1;
}
@after{
  MFPA.dummy_arg_list(numDA);
}
    : dummy_arg
      ( COMMA dummy_arg {numDA++;} )*
    ;

/*
 * R1537: end subroutine stmt
 */
end_subroutine_stmt
@init{
  Token id = null;
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      END SUBROUTINE (IDENT {id=$IDENT;})?
      end_of_stmt
        { MFPA.end_subroutine_stmt(lbl, 
              $END, $SUBROUTINE, id, $end_of_stmt.t);}
    | (lbl=label)? 
      END 
      end_of_stmt
        { MFPA.end_subroutine_stmt(lbl, 
              $END, null, id, $end_of_stmt.t);}
    ;

/*
 * R1538: separate module subprogram
 *   OFP: specification_part made non-optional 
 *          to remove END ambiguity (as can be empty)
 */
separate_module_subprogram
@init{
  boolean hasEP = false;
  boolean hasISP = false;
}
@after{
  MFPA.separate_module_subprogram(hasEP, hasISP);
}
    : mp_subprogram_stmt
      specification_part
      ( execution_part {hasEP=true;} )?
      ( internal_subprogram_part {hasISP=true;} )?
      end_mp_subprogram_stmt
    ;

/*
 * R1539: mp subprogram stmt
 *  CIVL: procedure_name replaced as IDENT
 */
mp_subprogram_stmt
@init{
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      MODULE PROCEDURE IDENT 
      end_of_stmt
        { MFPA.mp_subprogram_stmt(lbl, 
              $MODULE, $PROCEDURE, $IDENT, $end_of_stmt.t);}
    ;

/*
 * R1540: end mp subprogram stmt
 *  CIVL: procedure_name replaced as IDENT
 */
end_mp_subprogram_stmt
@init{
  Token proc = null; 
  Token name = null;
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      END 
      ( PROCEDURE {proc=$PROCEDURE;} 
        ( IDENT {name=$IDENT;})? 
      )?
      end_of_stmt
        { MFPA.end_mp_subprogram_stmt(lbl, 
              $END, proc, name, $end_of_stmt.t);}
    ;

/*
 * R1541: entry stmt
 *   OFP: INDENT inlined for entry_name
 */
entry_stmt
@init {
  boolean hasDAL=false; 
  boolean hasSffx=false;
}
@after{
  checkForInclude();
}
    : (lbl=label)?  
      ENTRY IDENT 
      ( LPAREN (dummy_arg_list {hasDAL=true;})? 
        RPAREN (suffix {hasSffx=true;})? 
      )?
      end_of_stmt
        { MFPA.entry_stmt(lbl, 
              $ENTRY, $IDENT, $end_of_stmt.t, 
              hasDAL, hasSffx);}
    ;

/*
 * R1542: return stmt
 *   OFP: scalar_int_expr replaced by expr
 */
return_stmt
@init{
  boolean hasExpr = false;
} 
@after{
  checkForInclude();
}
    : (lbl=label)?  
      RETURN (expr {hasExpr=true;})? 
      end_of_stmt
        { MFPA.return_stmt(lbl, 
              $RETURN, $end_of_stmt.t, hasExpr);}
    ;

/*
 * R1543: contains stmt
 */
contains_stmt
@init{
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      CONTAINS
      end_of_stmt
        { MFPA.contains_stmt(lbl,
              $CONTAINS, $end_of_stmt.t); }
    ;

/*
 * R1544: stmt fucntion stmt
 *   OFP: scalar_expr replaced by expr 
 *        generic_name_list substituted for dummy_arg_name_list
 *        TODO Hopefully scanner and parser can help work together
 *             here to work around ambiguity. 
 *             why can't this be accepted as an assignment statement 
 *             and then the parser look up the symbol for the IDENT 
 *             to see if it is a function??
 *             Need scanner to send special token if it sees what?
 *        TODO - won't do a(b==3,c) = 2
 */
stmt_function_stmt
@init{
  boolean hasGNL = false;
}
@after{
  checkForInclude();
}
    : (lbl=label)? 
      STMT_FUNCTION IDENT LPAREN 
      ( generic_name_list {hasGNL=true;})? 
      RPAREN EQUALS expr 
      end_of_stmt
        { MFPA.stmt_function_stmt(lbl, 
              $IDENT, $end_of_stmt.t, hasGNL);}
    ;

/*
 * In total, there are 473 real rules defined in Fortran 2018 Std.
 * Rules below are deprecated rules for back-compatability or 
 * extended rules supporting parsing/verifying Fortran programs.
 */

/*
 * R-1: end of stmt
 * OFP: The first branch:
 *        added this to have a way to match the EOS and EOF combinations
 *        (EOF) => EOF is done with lookahead because if it's not there, 
 *        then antlr will crash with an internal error while trying to 
 *        generate the java code.  (as of 12.11.06)
 *      The second branch:
 *        don't call MFPA.end_of_file() here or the action will be called 
 *        before end_of_program action called 
 */
end_of_stmt returns [Token t]
    : EOS
        { t = $EOS;}
    | (EOF) => EOF
        { t = $EOF;}
    ;

/*
 * R-1101: assign stmt
 *    OFP: The ASSIGN statement is a deleted feature.
 */
//assign_stmt
//@after{
//  checkForInclude();
//}
//    : (lbl=label)?
//      ASSIGN tlbl=label TO IDENT 
//      end_of_stmt
//        { MFPA.assign_stmt(lbl, 
//              $ASSIGN, tlbl, $TO, $IDENT, $end_of_stmt.t);}
//    ;

/*
 * R-1102: assign stmt
 *    OFP: The assigned GOTO statement is a deleted feature.
 */
//assigned_goto_stmt
//@after{
//  checkForInclude();
//}
//    : (lbl=label)?
//      ( go=GOTO 
//      | go=GO to=TO 
//      ) 
//      IDENT (COMMA stmt_label_list)?
//      end_of_stmt
//        { MFPA.assigned_goto_stmt(lbl,
//              go, to, $IDENT, $end_of_stmt.t); }
//    ;

/*
 * R-1103: stmt label list
 *    OFP: Used with assigned_goto_stmt (deleted feature)
 */
//stmt_label_list
//@init{
//   int numSL = 1;
//}
//@after{
//  MFPA.stmt_label_list(numSL);
//}
//    : LPAREN label 
//      ( COMMA label {numSL ++;} )* RPAREN 
//    ;


/*
 * R-1104: pause stmt
 *    OFP: The PAUSE statement is a deleted feature.
 */
//pause_stmt
//@after{
//  checkForInclude();
//}
//    : (lbl=label)?
//      PAUSE 
//      ( tlbl=label
//      | char_literal_constant
//      )?
//      end_of_stmt
//        { MFPA.pause_stmt(lbl, 
//              $PAUSE, tlbl, $end_of_stmt.t); }
//    ;

/*
 * R-1105: arithmetic if stmt
 *    OFP: The arithmetic if statement is a deleted feature.
 *         scalar_numeric_expr replaced by expr
 */
//arithmetic_if_stmt
//@after{
//  checkForInclude();
//}
//    : (lbl=label)?
//      M_ARITHMETIC_IF_STMT IF LPAREN expr RPAREN 
//      lbl0=label COMMA lbl1=label COMMA lbl2=label 
//      end_of_stmt
//        { MFPA.arithmetic_if_stmt(lbl, 
//              $IF, lbl0, lbl1, lbl2, $end_of_stmt.t); }
//    ;

/*x
 * R-1106: errorstop-stmt
 *    OFP: rule 856 in Fortran 2008 std. 
 */
//errorstop_stmt
//@init{
//  boolean hasSC = false;
//}
//@after{
//  checkForInclude();
//}
//    : (lbl=label)? 
//      ERROR STOP (stop_code {hasSC=true;})? 
//      end_of_stmt
//        { MFPA.errorstop_stmt(lbl, 
//              $ERROR, $STOP, $end_of_stmt.t, hasSC); }
//    ;

/* 
 * R-601: hollerith literal constant
 *   OFP: Hollerith constants were deleted in F77;
 *         Hollerith edit descriptors deleted in F95.
 */
//hollerith_literal_constant
//    : HOLLERITH 
//        { MFPA.hollerith_literal_constant($HOLLERITH); }
//    ;
