grammar Simple;

options {output=AST;}

tokens{
	IF;
	BAR;
}

start 	:	foo{System.out.println($foo.tree);}
	;

foo	: b=bar WS* -> ^(IF {new CommonTree((CommonTree)((CommonTree)$b.tree).getChild(1))} {new CommonTree((CommonTree)((CommonTree)$b.tree).getChild(0))});

bar 	: ID WS+ INT -> ID INT;

//foo	: bar WS* -> ^(IF bar);
//bar	: ID WS+ ID -> ID ID ;
INT :   '0'..'9'+ ; 
ID	: ('a'..'z'|'A'..'Z')+ ;
NEWLINE:'\r'? '\n' ;
WS  :   (' '|'\t')+ ;