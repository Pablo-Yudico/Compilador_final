package compiladorv2;
%%
%class Lexer // El nombre que le pondré a la clase
%type Token // El tipo de objeto que quiero regresar
L=[a-zA-Z_]
D=[0-9]+
espacio=[ \t\r]
salto=[\n]
%{
    public int linea = 1;
%}
%%
{espacio} { /* ignora espacios en blanco */ }
{salto} {linea++;}

"program" { return new Token(Tokens.pro.ordinal(), yytext(), linea); }
"int" { return new Token(Tokens.tint.ordinal(), yytext(), linea); }
"float" { return new Token(Tokens.tfloat.ordinal(), yytext(), linea); }
"char" { return new Token(Tokens.tchar.ordinal(), yytext(), linea); }
"String" { return new Token(Tokens.tString.ordinal(), yytext(), linea); }
"boolean" { return new Token(Tokens.tboolean.ordinal(), yytext(), linea); }
"switch" { return new Token(Tokens.esw.ordinal(), yytext(), linea); }
"do" { return new Token(Tokens.edo.ordinal(), yytext(), linea); }
"while" { return new Token(Tokens.ewh.ordinal(), yytext(), linea); }
"write" { return new Token(Tokens.write.ordinal(), yytext(), linea); }
"case" { return new Token(Tokens.eca.ordinal(), yytext(), linea); }
"default" { return new Token(Tokens.ede.ordinal(), yytext(), linea); }
"break" { return new Token(Tokens.ebr.ordinal(), yytext(), linea); }
"true" { return new Token(Tokens.ver.ordinal(), yytext(), linea); }
"false" { return new Token(Tokens.fal.ordinal(), yytext(), linea); }
"read" { return new Token(Tokens.read.ordinal(), yytext(), linea); }

"=" { return new Token(Tokens.igual.ordinal(), yytext(), linea); }
"+" { return new Token(Tokens.sum.ordinal(), yytext(), linea); }
"-" { return new Token(Tokens.res.ordinal(), yytext(), linea); }
"*" { return new Token(Tokens.mul.ordinal(), yytext(), linea); }
"/" { return new Token(Tokens.div.ordinal(), yytext(), linea); }
"(" { return new Token(Tokens.p_a.ordinal(), yytext(), linea); }
")" { return new Token(Tokens.p_c.ordinal(), yytext(), linea); }
"{" { return new Token(Tokens.l_a.ordinal(), yytext(), linea); }
"}" { return new Token(Tokens.l_c.ordinal(), yytext(), linea); }
"," { return new Token(Tokens.coma.ordinal(), yytext(), linea); }
";" { return new Token(Tokens.pcoma.ordinal(), yytext(), linea); }
":" { return new Token(Tokens.puntos.ordinal(), yytext(), linea); }

{D}+ { return new Token(Tokens.nume.ordinal(), yytext(), linea); } // Para números enteros
[+-]?{D}+\.({D}+)? { return new Token(Tokens.numf.ordinal(), yytext(), linea); } // Para números flotantes

// Literales

"\"" ([^\"\\]|\\.)* "\""  { return new Token(Tokens.lits.ordinal(), yytext(), linea); } // String literals (allow escaping)
"'"  ([^'\\]|\\.) "'"     { return new Token(Tokens.litc.ordinal(), yytext(), linea); } // Character literals (allow escaping)




{L}({L}|{D})* { return new Token(Tokens.id.ordinal(), yytext(), linea); }

"!" { return new Token(Tokens.not.ordinal(), yytext(), linea); }
"&" { return new Token(Tokens.and.ordinal(), yytext(), linea); }
"|" { return new Token(Tokens.or.ordinal(), yytext(), linea); }
">" { return new Token(Tokens.ma.ordinal(), yytext(), linea); }
"<" { return new Token(Tokens.me.ordinal(), yytext(), linea); }
"<=" { return new Token(Tokens.mai.ordinal(), yytext(), linea); }
">=" { return new Token(Tokens.mei.ordinal(), yytext(), linea); }
"==" { return new Token(Tokens.exa.ordinal(), yytext(), linea); }
"!=" { return new Token(Tokens.dif.ordinal(), yytext(), linea); }

// Regla para manejar tokens desconocidos
. { 
    System.err.println("Error: Token desconocido '" + yytext() + "' en la línea " + linea); 
}