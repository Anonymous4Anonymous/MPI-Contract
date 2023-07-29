parser grammar MFortranOmpParser;

options
{
	language=Java;
	tokenVocab=MFortranOmpLexer;
	output=AST;
}

//import FortranParserExtras;

tokens
{
	T_IDENTIFIER_LIST;	
	T_PARALLEL_FOR;
	T_PARALLEL_SECTIONS;
	T_UNIQUE_FOR;
	T_UNIQUE_PARALLEL;
	T_DATA_CLAUSE;
	T_FOR_CLAUSE;
}

/* ANTLR 3.5 doesn't allow redefinition of headers in composite grammars.
Our solution for this is: add the header (package, imported package)
to the generated java file in ant.
@header
{
package edu.udel.cis.vsl.abc.front.fortran.parse;
}*/

@members {
	@Override
	public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
		String hdr = getErrorHeader(e);
		String msg = getErrorMessage(e, tokenNames);
		
		throw new RuntimeParseException(hdr+" "+msg, e.token);
	}

	@Override
	public void emitErrorMessage(String msg) { // don't try to recover!
	    throw new RuntimeParseException(msg);
	}
}

// openMP grammar :  a bit old
//  missing some things, e.g., collapse, block, ...
  
openmp_construct
  : 
    parallel_for_directive
  | parallel_sections_directive
  | parallel_directive
  | for_directive
  | sections_directive
  | single_directive
  | master_directive
  | critical_directive
  | ordered_directive
  | section_directive
  | ompatomic_directive
  | barrier_directive
  | flush_directive
  | threadprivate_directive
  | end_directive
  ;

parallel_directive
  : T_PARALLEL  (p+=parallel_clause)*
  -> ^(T_PARALLEL $p*)
  ;

parallel_clause
  : unique_parallel_clause
  | data_clause
  ;
  
master_directive
  : T_MASTER -> ^(T_MASTER)
  ;

critical_directive
  : T_CRITICAL  (T_LPAREN  id=T_IDENT  T_RPAREN)?
  -> ^(T_CRITICAL $id?)
  ;
  
sections_directive
  : T_SECTIONS  (s+=sections_clause)*
  -> ^(T_SECTIONS $s*)
  ;

sections_clause
  : data_clause
  | nowait_directive
  ;

section_directive
  : T_SECTION -> ^(T_SECTION)
  ;
  
parallel_for_directive
  : T_PARALLEL T_DO p+=parallel_for_clause*
    -> ^(T_PARALLEL_FOR $p*)
  ;

parallel_for_clause
  : unique_parallel_clause
  | unique_for_clause
  | data_clause
  ;

parallel_sections_directive
  : T_PARALLEL  T_SECTIONS  p+=parallel_sections_clause*
    -> ^(T_PARALLEL_SECTIONS $p*)
  ;

parallel_sections_clause
  : unique_parallel_clause
  | data_clause
  ;

single_directive
  : T_SINGLE  s+=single_clause*
    -> ^(T_SINGLE $s*)
  ;

single_clause
  : data_clause
  | nowait_directive
  ;

barrier_directive
  : T_BARRIER -> ^(T_BARRIER)
  ;
  
ompatomic_directive
  : T_OMPATOMIC c0=atomic_clasue? c1=seq_cst_clause? 
    -> ^(T_OMPATOMIC $c0? $c1?)
  ;
  
atomic_clasue
	: T_READ | T_WRITE | T_UPDATE | T_CAPTURE	
	;
	
seq_cst_clause
	: T_SEQ_CST	
	;

flush_directive
  : T_FLUSH  f=flush_vars?
    -> ^(T_FLUSH $f?)
  ;

flush_vars
  : T_LPAREN   i=identifier_list  T_RPAREN
    -> ^(T_IDENTIFIER_LIST $i)
  ;

ordered_directive
  : T_ORDERED -> ^(T_ORDERED)
  ;
  
nowait_directive
  : T_NOWAIT -> ^(T_NOWAIT)
  ;

threadprivate_directive
  : T_THD_PRIVATE  T_LPAREN  i=identifier_list  T_RPAREN
    -> ^(T_THD_PRIVATE $i)
  ;

for_directive
  : T_DO  (f+=for_clause)*
    -> ^(T_DO $f*)
  ;

for_clause
  : u=unique_for_clause -> ^(T_FOR_CLAUSE $u)
  | d=data_clause -> ^(T_FOR_CLAUSE $d)
  | n=nowait_directive -> ^(T_FOR_CLAUSE $n)
  ;

unique_for_clause
  : T_ORDERED ->^(T_UNIQUE_FOR T_ORDERED)
  | s1=schedule_clause -> ^(T_UNIQUE_FOR $s1)
  | c=collapse_clause -> ^(T_UNIQUE_FOR $c)
  ;
  
schedule_clause
	: T_SCHEDULE  T_LPAREN  s1=schedule_kind  T_COMMA  e=expression  T_RPAREN
      -> ^(T_SCHEDULE $s1 $e)
    |  T_SCHEDULE  T_LPAREN  s=schedule_kind  T_RPAREN
	  -> ^(T_SCHEDULE $s)
	;
	
collapse_clause
	:
	T_COLLAPSE  T_LPAREN  i=T_DIGIT_STRING  T_RPAREN
    -> ^(T_COLLAPSE $i)
	;

schedule_kind
  : T_STATIC -> ^(T_STATIC)
  | T_DYNAMIC -> ^(T_DYNAMIC)
  | T_GUIDED -> ^(T_GUIDED)
  | T_RUNTIME -> ^(T_RUNTIME)
  ;

unique_parallel_clause
  : i=if_clause 
    -> ^(T_UNIQUE_PARALLEL $i)
  | n=num_threads_clause 
    -> ^(T_UNIQUE_PARALLEL $n)
  ;
  
if_clause
  : T_IF  T_LPAREN  e1=expression  T_RPAREN
    -> ^(T_IF $e1)
  ;
  
num_threads_clause
  : T_NUM_THREADS  T_LPAREN  e2=expression  T_RPAREN
    -> ^(T_NUM_THREADS $e2)
  ;

data_clause
  : d1=private_clause
    -> ^(T_DATA_CLAUSE $d1)
  | d2=firstprivate_clause
    -> ^(T_DATA_CLAUSE $d2)
  | d3=lastprivate_clause
    -> ^(T_DATA_CLAUSE $d3)
  | d4=shared_clause
    -> ^(T_DATA_CLAUSE $d4)
  | d5=default_clause
    -> ^(T_DATA_CLAUSE $d5)
  | d6=reduction_clause
    -> ^(T_DATA_CLAUSE $d6)
  | d7=copyin_clause
    -> ^(T_DATA_CLAUSE $d7)
  | d8=copyprivate_clause
    -> ^(T_DATA_CLAUSE $d8)
  ;
  
private_clause
  : T_PRIVATE  T_LPAREN  i1=identifier_list  T_RPAREN 
    -> ^(T_PRIVATE $i1)
  ;
  
firstprivate_clause
  : T_FST_PRIVATE  T_LPAREN  i2=identifier_list  T_RPAREN
    -> ^(T_FST_PRIVATE $i2)
  ;
  
lastprivate_clause
  : T_LST_PRIVATE  T_LPAREN  i3=identifier_list  T_RPAREN
    -> ^(T_LST_PRIVATE $i3)
  ;
  
shared_clause
  : T_SHARED  T_LPAREN  i4=identifier_list  T_RPAREN
    -> ^(T_SHARED $i4)
  ;
  
default_clause
  : T_DEFAULT  T_LPAREN  T_SHARED  T_RPAREN
    -> ^(T_DEFAULT T_SHARED)
  | T_DEFAULT  T_LPAREN  T_PRIVATE  T_RPAREN
    -> ^(T_DEFAULT T_NONE)
  | T_DEFAULT  T_LPAREN  T_NONE  T_RPAREN
    -> ^(T_DEFAULT T_NONE)
  ;
  
reduction_clause
  : T_REDUCTION T_LPAREN r=reduction_operator T_COLON i5=identifier_list T_RPAREN
    -> ^(T_REDUCTION $r $i5)
  ;
  
copyin_clause
  : T_COPYIN  T_LPAREN  i6=identifier_list  T_RPAREN
    -> ^(T_COPYIN $i6)
  ;
  
copyprivate_clause
  : T_COPYPRIVATE  T_LPAREN  i7=identifier_list  T_RPAREN
    -> ^(T_COPYPRIVATE $i7)
  ;

reduction_operator
  : T_PLUS -> ^(T_PLUS)
  | T_ASTERISK -> ^(T_ASTERISK)
  | T_MINUS -> ^(T_MINUS)
  | T_AMPERSAND -> ^(T_AMPERSAND)
  | T_BITXOR -> ^(T_BITXOR)
  | T_BITOR -> ^(T_BITOR)
  | T_AND -> ^(T_AND)
  | T_OR -> ^(T_OR)
  | T_IDENT -> ^(T_IDENT)
  ;

identifier_list
  :
  i1=T_IDENT ( T_COMMA  i2+=T_IDENT)* 
  -> ^(T_IDENTIFIER_LIST $i1 $i2*)
  ;
  
//EXPRESSION:
expression
  	:	
  	(i1=T_IDENT)
  	-> ^($i1)
  	;
  	
end_directive
	:
	T_END kl=keyword_list
	-> ^(T_END $kl)
	;
	
keyword_list
	:
	T_PARALLEL -> ^(T_PARALLEL)
	|T_DO -> ^(T_DO)
	|T_SECTIONS -> ^(T_SECTIONS)
	|T_SINGLE -> ^(T_SINGLE)
	|T_MASTER -> ^(T_MASTER)
	|T_CRITICAL -> ^(T_CRITICAL)
	|T_ORDERED -> ^(T_ORDERED)
	|T_SECTION -> ^(T_SECTION)
	|T_OMPATOMIC -> ^(T_OMPATOMIC)
	|T_BARRIER -> ^(T_BARRIER)
	|T_FLUSH -> ^(T_FLUSH)
	|T_THD_PRIVATE -> ^(T_THD_PRIVATE)
	;
  
