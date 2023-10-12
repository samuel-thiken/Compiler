lexer grammar DecaLexer;

options {
	language = Java;
	// Tell ANTLR to make the generated lexer class extend the the named class, which is where any
	// supporting code and variables will be placed.
	superClass = AbstractDecaLexer;
}
@header { 
	import fr.ensimag.deca.DecacCompiler;
}

@lexer::members {
	{
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
		this.addResource(CharStreams.fromString("class Object { boolean equals(Object obj) { return this == obj; } }"));
		//this.addResource(CharStreams.fromFileName(DecacCompiler.class.getResource("include/Arrays.decah").getPath())); TODO si ça marche pas on copie la string dedans lol
	}
}
/*--- Deca booked words ---*/
PRINT: 'print';
PRINTLN: 'println';
PRINTX: 'printx';
PRINTLNX: 'printlnx';
WHILE: 'while';
RETURN: 'return';
IF: 'if';
ELSE: 'else';
INSTANCEOF: 'instanceof';
READINT: 'readInt';
READFLOAT: 'readFloat';
NEW: 'new';
TRUE: 'true';
FALSE: 'false';
THIS: 'this';
NULL: 'null';
EXTENDS: 'extends';
CLASS: 'class';
PROTECTED: 'protected';
ASM: 'asm';

/*--- special chars ---*/
OBRACKET: '[';
CBRACKET: ']';
OBRACE: '{';
CBRACE: '}';
OPARENT: '(';
CPARENT: ')';
SEMI: ';';
COMMA: ',';
EQUALS: '=';
AND: '&&';
OR: '||';
EQEQ: '==';
NEQ: '!=';
GEQ: '>=';
GT: '>';
LEQ: '<=';
LT: '<';
PLUS: '+';
MINUS: '-';
TIMES: '*';
SLASH: '/';
PERCENT: '%';
EXCLAM: '!';
DOT: '.';

/*--- letters, numbers ---*/
fragment LETTER: 'A' .. 'Z' | 'a' .. 'z';
fragment POSITIVE_DIGIT: '1' .. '9';
fragment DIGIT: '0' | POSITIVE_DIGIT;
fragment NUM: DIGIT+;
fragment SIGN: '+' | '-' | /* '' */;
fragment EXP: ('E' | 'e') SIGN NUM;
fragment DEC: NUM '.' NUM;
fragment FLOATDEC: (DEC | DEC EXP) ('F' | 'f' | /* '' */);
fragment DIGITHEX: DIGIT | 'A' .. 'F' | 'a' .. 'f';
fragment NUMHEX: DIGITHEX+;
fragment FLOATHEX: ('0x' | '0X') NUMHEX '.' NUMHEX ('P' | 'p') SIGN NUM (
		'F'
		| 'f'
		| /* '' */
	);
FLOAT: FLOATDEC | FLOATHEX;
INT: '0' | POSITIVE_DIGIT DIGIT*;
IDENT: (LETTER | '$' | '_') (LETTER | DIGIT | '$' | '_')*;

/*--- simple strings & multi-line strings ---*/
fragment STRING_CAR: ~('"' | '\\' | '\n');
STRING: '"' (STRING_CAR | '\\"' | '\\\\"' | '\\\\')* '"';
MULTI_LINE_STRING:
	'"' (STRING_CAR | '\n' | '\\"' | '\\\\')* '"';

/*--- Separators ---*/
EOL: '\n' { skip(); };
SPACE: ' ' { skip(); };
TAB: '\t' { skip(); };
CARRIAGE_RETURN: '\r' { skip();};
COMMENT: (('/*' .*? '*/') | ('//' .*? '\n')) { skip(); };

/*--- Inclusion of files ---*/
fragment FILENAME: (LETTER | DIGIT | '.' | '-' | '_')+;
INCLUDE: ('#include' (' ')* '"' FILENAME '"') { doInclude(getText()); };