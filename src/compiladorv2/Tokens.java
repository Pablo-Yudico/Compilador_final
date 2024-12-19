package compiladorv2;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */

/**
 *
 * @author Omen
 */
public enum Tokens {
    pro, //0 program
    id, //1 id de variable
    tint, //2 int
    tfloat, //3 float
    tchar, //4 char
    tString, //5 String
    tboolean, //6 boolean
    esw, //7 switch
    edo, //8 do
    ewh, //9 while
    coma, //10 ,
    pcoma, //11 ; 
    igual, //12 =
    p_a, //13 (
    p_c, //14 ) 
    l_a, //15 {
    l_c, //16 }
    write, //17 write
    eca, //18 case
    puntos, //19 :
    ede, //20 default
    ebr, //21 break
    not, //22 !
    and, //23 &
    or, //24 |
    ma, //25 > 
    me, //26 <
    mai, //27 <=
    mei, //28 >=
    exa, //29 ==
    dif, //30 !=
    sum, //31 +
    res, //32 -
    mul, //33 *
    div, //34 /
    numf, //35 numero flotante
    nume, //36 numero entero
    lits, //37 literal String debe estar delimitado por comillas dobles)
    litc, //38 literal char (debe estar delimitado por comillas simples y solo tener un caracter en su interior)
    ver, //39 true
    fal, //40 false
    read, //41 read
    terminador, //42 El inicio y fin marcado por el $
}
