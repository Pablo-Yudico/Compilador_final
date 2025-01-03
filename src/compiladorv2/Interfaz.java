/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package compiladorv2;

import compiladorv2.Token;
import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Stack;

/**
 *
 * @author Omen
 */
public class Interfaz extends javax.swing.JFrame {

    /**
     * Creates new form Interfaz
     */
    public Interfaz() {
        resource = classLoader.getResource("compiladorv2/archivos");
        ubicacion = new File(resource.getPath());
        initComponents();

        NumeroLinea lineNumber = new NumeroLinea(Area_Edicion);
        lineNumber.setMinimumDisplayDigits(4);
        lineNumber.setDigitAlignment(NumeroLinea.CENTER);
        lineNumber.setCurrentLineForeground(Color.white);
        lineNumber.setBackground(Color.lightGray);
        jScrollPane1.setRowHeaderView(lineNumber);
        CargarTabla();
    }

    URL resource;
    File ubicacion;
    ImageIcon bien = new ImageIcon(getClass().getResource("/Compiladorv2/img/check.png"));
    ImageIcon mal = new ImageIcon(getClass().getResource("/Compiladorv2/img/error.png"));
    ClassLoader classLoader = getClass().getClassLoader();

    public static List<String> errores;
    List<String> columnas = new ArrayList<>();
    List<List<String>> matriz = new ArrayList<>();
    List<List<String>> producciones = new ArrayList<>();
    List<List<String>> matriz_semantica;
    String codigo_intermedio;
    List<Token> tokens;
    Stack<String> operadores;
    Stack<String> posfija;
    int numw = 0, nums = 0, numc = 1;
    int[] conta = {0, 0, 0, 0, 0};
    int[] contad = {0, 0, 0, 0, 0};
    int tab = 0;

    //Métodos agregados
    public void Vaciar() {
        URL resource = classLoader.getResource("compiladorv2/archivos");
        ubicacion = new File(resource.getPath());
        errores = null;

        Area_Edicion.setText("");
        Area_Lexico.setText("");
        Area_Sintactico.setText("");
        Area_Errores.setText("");
        Selector.setCurrentDirectory(ubicacion);
        Selector.setSelectedFile(null);
        codigo_intermedio = "";
        numw = 1;
        nums = 1;

    }

    public void Reiniciar() {
        //Arreglos
        operadores = new Stack<>();
        posfija = new Stack<>();
        codigo_intermedio = "";
        tokens = new ArrayList<>();
        errores = new ArrayList<>();
        matriz_semantica = new ArrayList<>();
        matriz_semantica.add(new ArrayList<>());
        matriz_semantica.add(new ArrayList<>());

        codigo_intermedio = "";
        numw = 0;
        nums = 0;
        tab = 0;

        for (int x = 0; x < 5; x++) {
            conta[x] = 0;
            contad[x] = 0;
        }

        //Ventana
        Area_Lexico.setText("Tipo\tLexema\tLinea\n");
        Area_Sintactico.setText("");
    }

    public void Seleccionar() {
        Selector.setCurrentDirectory(ubicacion);
        Selector.setDialogTitle("Abrir archivo");
        Selector.setFileFilter(new FileNameExtensionFilter("Archivos de texto", "txt"));

        int result = Selector.showOpenDialog(this);
        if (result == Selector.APPROVE_OPTION) {
            File selectedFile = Selector.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
                Area_Edicion.read(reader, null);
            } catch (IOException ex) {
                Emergente.showMessageDialog(this, "Error al leer el archivo: " + ex.getMessage(), "Error", Emergente.ERROR_MESSAGE);
            }
        }
    }

    public void Guardar() {
        if (Selector.getSelectedFile() != null) {
            File selectedFile = Selector.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(selectedFile))) {
                Area_Edicion.write(writer);
                Emergente.showMessageDialog(null, "Se guardó correctamente", "Archivo guardado", Emergente.INFORMATION_MESSAGE, bien);
            } catch (IOException ex) {
                Emergente.showMessageDialog(this, "Error al guardar el archivo: " + ex.getMessage(), "Error", Emergente.ERROR_MESSAGE);
            }
        } else
            GuardarComo();
    }

    public void GuardarComo() {
        Selector.setCurrentDirectory(ubicacion);
        Selector.setDialogTitle("Guardar archivo de texto como...");
        Selector.setFileFilter(new FileNameExtensionFilter("Archivos de texto", "txt"));

        int result = Selector.showSaveDialog(this);
        if (result == Selector.APPROVE_OPTION) {
            File selectedFile = Selector.getSelectedFile();
            if (!selectedFile.getPath().endsWith(".txt")) {
                selectedFile = new File(selectedFile.getPath() + ".txt");
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(selectedFile))) {
                Area_Edicion.write(writer);
                Emergente.showMessageDialog(null, "Se guardó correctamente", "Archivo guardado", Emergente.INFORMATION_MESSAGE, bien);
            } catch (IOException ex) {
                Emergente.showMessageDialog(this, "Error al guardar el archivo: " + ex.getMessage(), "Error", Emergente.ERROR_MESSAGE);
            }
        }
    }

    public void Lexico() {

        Reiniciar();

        // Crear una instancia del lexer
        Lexer lexer = new Lexer(new StringReader(Area_Edicion.getText()));

        // Llamar a un método para procesar los tokens
        procesarTokens(lexer);

        if (!tokens.isEmpty()) {
            tokens.add(new Token(columnas.indexOf("$"), "$", tokens.getLast().Linea()));
            Area_Lexico.setText(Area_Lexico.getText() + columnas.indexOf("$") + "\t" + "$" + "\t" + tokens.getLast().Linea());
        }
        if (!errores.isEmpty()) {
            System.out.println("Hay errores");
            Area_Errores.setText("Errores léxicos encontrados");
            for (String error : errores) {
                Area_Errores.setText(Area_Errores.getText() + "\n" + error);
            }
        } else
            Sintactico();
    }

    private void procesarTokens(Lexer lexer) {
        Token token;
        try {
            // Extraer tokens hasta que no haya más
            while ((token = lexer.yylex()) != null) {
                // Mostrar el tipo y el texto del token
                Area_Lexico.setText(Area_Lexico.getText() + token.Tipo() + "\t" + token.Lexema() + "\t" + token.Linea() + "\n");
                tokens.add(token);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void CargarTabla() {

        System.out.println("Cargando tabla...");
        String linea, sep = ("\t");
        /*Notas
        La primera fila es la de los tokens
        La primera columna columna de cada fila es el estado
        La cadena vacia se representa por medio de un ?
         */
        resource = classLoader.getResource("compiladorv2/tablas/Tabla.csv");
        try (BufferedReader br = new BufferedReader(new FileReader(resource.getPath()))) {

            //Esta  es la parte en la que iniciamos los tokens a trabajar
            if ((linea = br.readLine()) != null) {
                String[] colums = linea.split(sep);
                for (String valor : colums) {
                    columnas.add(valor);
                }
                columnas.remove(0);
            }

            //Aquí leemos los estados y la matriz
            while ((linea = br.readLine()) != null) {
                String[] valores = linea.split(sep);
                ArrayList<String> fila = new ArrayList<>();
                for (int i = 1; i < valores.length; i++) {
                    fila.add(valores[i]);
                }
                matriz.add(fila);

            }

            //Mostramos los datos
            System.out.println("Tabla cargada\n");
            for (String elemento : columnas) {
                System.out.print(elemento + "\t");
            }
            for (List<String> fila : matriz) {
                System.out.println("");
                for (String elemento : fila) {
                    System.out.print(elemento + "\t");
                }
            }
        } catch (IOException e) {
            System.err.println("No se encontró el archivo: " + e.getMessage());
        }

        resource = classLoader.getResource("compiladorv2/tablas/Producciones.csv");
        try (BufferedReader br = new BufferedReader(new FileReader(resource.getPath()))) {

            //Aquí leemos los estados y la matriz
            while ((linea = br.readLine()) != null) {
                String[] valores = linea.split(sep);
                ArrayList<String> fila = new ArrayList<>();
                for (int i = 0; i < valores.length; i++) {
                    fila.add(valores[i]);
                }
                producciones.add(fila);

            }

            //Mostramos los datos
            System.out.println("Producciones cargadas\n");
            for (List<String> fila : producciones) {
                System.out.println("");
                for (String elemento : fila) {
                    System.out.print(elemento + "\t");
                }
            }
        } catch (IOException e) {
            System.err.println("No se encontró el archivo: " + e.getMessage());
        }

    }

    public void Sintactico() {

        //Inicializar
        Stack<String> pila = new Stack<>();
        int estado = 0;
        String accion;

        int tipo = -1;
        boolean asignacion = false; //Variable para identifiar si estamos en las asignaciones
        boolean valido = true;
        boolean exp = false;
        boolean primero = true;
        boolean ssc = false;
        Token buscar = new Token(42, "Error", 0);
        Token vswitch = new Token(7, "valSwitch", 0);
        List<Token> expresion = new ArrayList<>();

        pila.push("$"); // Estado inicial
        pila.push("0"); // Estado inicial

        MostrarPila(pila);

        Token tok;
        String produccion;
        int ppro;

        int numtoken = 0;
        // Recorrer toda la cadena de simbolos
        while (!pila.empty()) {

            tok = tokens.get(numtoken);

            //System.out.println("\nNueva acción\nPosicion: I" + estado + ", Simbolo(" + columnas.get(tok.Tipo()) + ") en columna: " + tok.Tipo());
            if (tok.Tipo() != -1) {

                if (tok.Tipo() >= matriz.get(estado).size()) {
                    Error("Error sintáctico - Linea " + tok.Linea() + " - Se esperaba: " + Validos(estado));
                    break;
                }
                accion = matriz.get(estado).get(tok.Tipo());

                if (accion.equals("")) {
                    Error("Error sintáctico - Linea " + tok.Linea() + " - Se esperaba: " + Validos(estado));
                    break;
                }
                //System.out.println("Accion: " + accion);

                //Checar si es desplazamiento(I) o reducción(P)
                if (accion.startsWith("P")) {
                    if (accion.equals("P0")) {

                        //Declaración de las variables
                        String intermedio = "Codigo intermedio\n\n#include <stdio.h>\n#include <stdbool.h>\n#include <string.h>\n\nint main()\n{\n";

                        matriz_semantica.get(0).add("whileCon");
                        matriz_semantica.get(1).add("4");
                        for (int x = 0; x <= 4; x++) {
                            while (contad[x] > 0) {
                                switch (x) {
                                    case 0:
                                        matriz_semantica.get(0).add("Vd" + contad[x]);
                                        matriz_semantica.get(1).add("" + x);
                                        break;
                                    case 1:
                                        matriz_semantica.get(0).add("Vf" + contad[x]);
                                        matriz_semantica.get(1).add("" + x);
                                        break;
                                    case 2:
                                        matriz_semantica.get(0).add("Vs" + contad[x]);
                                        matriz_semantica.get(1).add("" + x);
                                        break;
                                    case 3:
                                        matriz_semantica.get(0).add("Vc" + contad[x]);
                                        matriz_semantica.get(1).add("" + x);
                                        break;
                                    case 4:
                                        matriz_semantica.get(0).add("Vb" + contad[x]);
                                        matriz_semantica.get(1).add("" + x);
                                }
                                contad[x]--;
                            }
                        }
                        for (int x = 0; x < matriz_semantica.get(0).size(); x++) {
                            if (matriz_semantica.get(1).get(x).equals("-1"))
                                continue;
                            if (matriz_semantica.get(1).get(x).equals("2"))
                                intermedio += "\tchar " + matriz_semantica.get(0).get(x) + "[30];\n";
                            else
                                intermedio += "\t" + Tipo(Integer.parseInt(matriz_semantica.get(1).get(x))) + " " + matriz_semantica.get(0).get(x) + ";\n";
                        }

                        codigo_intermedio = intermedio + codigo_intermedio;
                        codigo_intermedio += "\n\treturn 0;\n}\n";
                        System.out.println(codigo_intermedio);

                        Error("Cadena aceptada."); //Solo aprovechamos el método, aunque no sea realmente un error
                        break;
                    }

                    ppro = Integer.parseInt(accion.substring(1)) - 1; //Le restamos 1 porque no ponemos la producción 0, así que se recorren los números
                    produccion = producciones.get(ppro).get(0);

                    if (!producciones.get(ppro).get(1).equals("?")) {
                        String pop;
                        for (int x = (producciones.get(ppro).size() - 1) * 2; x > 0; x--) {
                            pop = pila.pop();
                        }
                        MostrarPila(pila);

                    }
                    estado = Integer.parseInt(pila.peek());
                    pila.push(produccion);

                    //Buscar la ACCION en la tabla 
                    if (columnas.indexOf(produccion) != -1) {
                        accion = matriz.get(estado).get(columnas.indexOf(produccion));

                        if (accion.equals("")) {
                            Error("Error sintáctico - Linea " + tok.Linea() + " - Se esperaba: " + Validos(estado));
                            break;
                        }
                        pila.push(accion.substring(1));
                        estado = Integer.parseInt(accion.substring(1));
                    }

                } else if (accion.startsWith("I")) {

                    estado = Integer.parseInt(accion.substring(1));
                    pila.add(columnas.get(tok.Tipo()));
                    pila.add(estado + "");
                    numtoken++;

                    //INTERVENCIÓN DEL SEMÁNTICO
                    switch (tok.Tipo()) {

                        case 2: //int
                        case 3: //float
                        case 4: //String
                        case 5: //char
                        case 6: //boolean
                            tipo = 0;
                            valido = Semantico(tok, tipo);
                            break;
                        case 20: //default
                            Agregar("case" + nums + numc + ":");
                            tab++;
                            break;
                        case 21: //break
                            tab--;
                            Agregar("\tgoto finSwitch" + nums+";");
                            expresion = new ArrayList<>();
                            break;
                        case 8: //do
                            IncWhile();
                            Agregar("etqWhile" + numw + ":");
                            break;
                        case 19: //:
                            if (tipo == 2) {
                                System.out.println("\nSe obtendrá el valor del case " + numc);
                                Agregar("case" + nums + numc + ":");
                                tab++;

                                buscar = Evaluar(expresion, vswitch);

                                IncCase();

                                //Separar los casos donde valSwitch es cadena o no
                                if (buscar.Tipo() == 37)
                                    Agregar("if (strcmp(" + buscar.Lexema() + " , valSwitch" + nums + ") != 0)");
                                else
                                    Agregar("if (! (" + buscar.Lexema() + " == valSwitch" + nums + "))");

                                DecVa(Tipoinv(buscar.Tipo()));
                                Agregar("\tgoto case" + nums + numc + ";");
                                if (buscar.Lexema().equals("Error"))
                                    valido = false;
                                System.out.println("El resultado fue " + Tipo(Tipoinv(buscar.Tipo())));
                                tipo = 1;
                                break;
                            }
                            break;
                        case 15: //{
                            tab++;
                            if (tipo == 2) {
                                System.out.println("Se obtendrá el valor del switch");
                                vswitch = Evaluar(expresion, new Token(7, "valSwitch" + nums, tok.Linea()));
                                matriz_semantica.get(0).add("valSwitch" + nums);
                                matriz_semantica.get(1).add(Tipoinv(vswitch.Tipo()) + "");
                                if (vswitch.Lexema().equals("Error"))
                                    valido = false;
                                System.out.println("El resultado fue " + Tipo(Tipoinv(vswitch.Tipo())));
                                tipo = 1;
                                break;
                            }
                            break;

                        case 16: //}
                            tab--;
                            if (ssc) {
                                Agregar("finSwitch" + nums + ":");
                                ssc = false;
                            }
                            break;
                        case 11: //;
                            asignacion = true;
                            primero = true;
                            exp = true;

                            if (tipo == 3) {
                                System.out.println("Se evaluará la expresión de un while");
                                buscar = Evaluar(expresion, new Token(40, "whileCon", tok.Linea()));
                                if (buscar.Lexema().equals("Error"))
                                    valido = false;
                                System.out.println("El resultado fue " + buscar.Lexema());
                                Agregar("if (whileCon)");
                                Agregar("\tgoto etqWhile" + numw + ";");
                                IncWhile();
                                tipo = 1;
                                break;
                            }

                            if (tipo == 4) {
                                Agregar("printf(" + Parametro(expresion) + ");");
                                tipo = 1;
                                break;
                            }

                            tipo = 1;

                            if (!expresion.isEmpty()) {
                                if (Evaluar(expresion, buscar).Lexema().equals("Error"))
                                    valido = false;
                                break;
                            }

                            break;
                        case 7: //switch
                            ssc = true;
                            IncSwitch();
                            numc = 1;
                        case 18: //case
                            asignacion = false;
                            primero = false;
                            exp = true;
                            expresion = new ArrayList<>();
                            tipo = 2;
                            break;
                        case 9: //while
                            asignacion = false;
                            primero = true;
                            exp = true;
                            tipo = 3;
                            break;
                        case 17: // write
                            asignacion = false;
                            primero = true;
                            exp = true;
                            tipo = 4;
                            break;
                        default:

                            if (tok.Tipo() == 1) {
                                valido = Semantico(tok, tipo);
                                if (tipo == 0)
                                    break;
                                if (asignacion) {
                                    buscar = tok;
                                    asignacion = false;
                                    exp = true;
                                    break;
                                }
                            }
                            if (exp) {
                                if (primero) {
                                    expresion = new ArrayList<>();
                                    primero = false;
                                } else
                                    expresion.add(tok);
                            }
                    }

                    if (!valido) {
                        System.out.println("\nNo fue válido y se debe de enviar a error.\n");
                        break;
                    }

                }

            } else
                Error("Error sintáctico - Linea " + tok.Linea() + "Se esperaba: " + Validos(estado));
            MostrarPila(pila);

        }

    }

    public boolean Semantico(Token ob, int op) {

        switch (op) {
            case -1:
                if (ob.Tipo() == 1) {
                    matriz_semantica.get(0).add(ob.Lexema());
                    matriz_semantica.get(1).add("-1");
                }
                break;
            case 0: //Declaración
                switch (ob.Tipo()) {

                    case 2: //int
                        matriz_semantica.get(1).add("0");
                        break;

                    case 3: //float
                        matriz_semantica.get(1).add("1");
                        break;

                    case 5: //String
                        matriz_semantica.get(1).add("2");
                        break;

                    case 4: //char
                        matriz_semantica.get(1).add("3");
                        break;
                    case 6: //boolean
                        matriz_semantica.get(1).add("4");
                        break;

                    case 1: //id

                        if (Buscar(ob.Lexema()) != -1) {
                            Error("Error semántico - Linea " + ob.Linea() + " - Variable ya declarada: " + ob.Lexema());
                            return false;
                        }

                        if (matriz_semantica.get(0).size() < matriz_semantica.get(1).size()) //Es el caso en el que ya está el tipo pero no la variable
                            matriz_semantica.get(0).add(ob.Lexema());
                        else {
                            matriz_semantica.get(1).add(matriz_semantica.get(1).getLast());
                            matriz_semantica.get(0).add(ob.Lexema());
                        }
                        break;
                }
                break;

            case 1: //Una asginación
            case 2:
            case 3:
            case 4:
                //En este caso se va evaluando la valides de las operaciones pero no se declara nada

                if (ob.Tipo() == 1)
                    if (Buscar(ob.Lexema()) == -1) {
                        Error("Error semántico - Linea " + ob.Linea() + " - Variable no declarada: " + ob.Lexema());
                        return false;
                    }
                break;

        }
        return true;
    }

    public Token Evaluar(List<Token> expresion, Token buscar) {
        String cadena = "";
        String cadv1 = "";
        String cadv2 = "";
        String cadv = "";
        Token principal = buscar;
        boolean aux;
        int var1, var2 = -2, vart = -2, con = 0;

        List<Token> subexpresion;
        List<String> c = new ArrayList();
        c.add("(");
        c.add(")");
        c.add("+");
        c.add("-");
        c.add("/");
        c.add("*");
        c.add("<");
        c.add(">");
        c.add("<=");
        c.add(">=");
        c.add("==");
        c.add("!=");
        c.add("&");
        c.add("|");
        c.add("!");

        //Apuntador donde la primera posición será fila (lo que llega a la expresión) y la segunda será la columna (lo que sale del peek)
        int[] ap = {-1, -1}; //Apuntador para el autómata de la pila

        Stack<Token> pilao = new Stack<Token>(); //Pila de operadores
        Stack<Integer> pilae = new Stack<Integer>(); //Pila de operadores
        Stack<String> pilas = new Stack<String>(); //Pila de operadores
        List<Token> evaluar = new ArrayList(); //Es el arrreglo en el que vamos a recorrer las cosas con orden a evaluar

        //Autómata para manejar la pila
        /*
            0 - Meter a la pila
            1 - Sacar de la pila y luego meter
            2 - Sacar de la pila y no meter
         */
        int[][] autop = {{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // (
        {2, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, // )
        {0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, // +
        {0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, // -
        {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, // /
        {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, // *
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1}, // <
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1}, // >
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1}, // <=
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1}, // >=
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1}, // ==
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1}, // !=
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1}, // &
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1}, // |
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1}};// !

        //Autómata para manejar las operaciones
        //Operaciones aritmeticas
        int[][] autota = {{0, 1, 2, 2, -1},
        {1, 1, 2, 2, -1},
        {2, 2, 2, 2, 2},
        {2, 2, 2, 2, 2},
        {-1, -1, 2, 2, -1}};

        //Operaciones relacionales
        int[][] autotr = {{4, 4, -1, -1, -1},
        {4, 4, -1, -1, -1},
        {-1, -1, 4, 4, -1},
        {-1, -1, 4, 4, -1},
        {-1, -1, -1, -1, 4}};

        //Operaciones lógico
        int[][] autotl = {{-1, -1, -1, -1, -1},
        {-1, -1, -1, -1, -1},
        {-1, -1, -1, -1, -1},
        {-1, -1, -1, -1, -1},
        {-1, -1, -1, -1, 4}};

        //Obtener información de la expresión que se va a evaluar
        for (Token tt : expresion) {
            cadena += tt.Lexema();
        }

        System.out.println("Variable a evaluar: " + principal.Lexema());
        System.out.println("Expresion a evaluar: " + cadena);
        System.out.println("Elementos a evaluar: " + expresion.size());

        cadena = "";

        //EMPEZAMOS A TRABAJAR SOBRE LA EXPRESIÓN
        Token t = expresion.get(0);
        for (int y = 0; y < expresion.size(); y++) {
            t = expresion.get(y);

            switch (t.Tipo()) {
                case 1: //id
                case 35: //numero flotante
                case 36: //numero entero
                case 37: //literal String
                case 38: //literal char
                case 39: //true
                case 40: //false
                    evaluar.add(t);
                    cadena += t.Lexema() + " ";
                    break;
                case 41: //Es un read
                    int nump = 1;
                    subexpresion = new ArrayList<>();
                    expresion.remove(y);
                    t = expresion.get(y);
                    do {
                        switch (t.Tipo()) {
                            case 13:
                                nump++;
                                break;
                            case 14:
                                nump--;
                        }
                        System.out.println("Mandamos a la subexpresión: " + t.Lexema());
                        subexpresion.add(t);
                        expresion.remove(y);
                        if (expresion.size() == 0)
                            break;
                        t = expresion.get(y);
                    } while (nump != 1);

                    Agregar("printf(" + Parametro(subexpresion) + ");");

                    cadv = IncVa(2);
                    Agregar("scanf(\"%s\", " + cadv + ");");
                    evaluar.add(new Token(37, cadv, t.Linea()));

                    y--;
                    break;
                case 22: // !
                case 23: // &
                case 24: // |
                case 25: // <
                case 26: // >
                case 27: // <=
                case 28: // >=
                case 29: // ==
                case 30: // !=
                case 31: // +
                case 32: // -
                case 33: // *
                case 34: // /
                    if (pilao.empty())
                        pilao.push(t);
                    else {

                        aux = true;
                        while (aux) {
                            if (pilao.empty()) {
                                pilao.push(t);
                                break;
                            }
                            ap[1] = c.indexOf(pilao.peek().Lexema());
                            ap[0] = c.indexOf(t.Lexema());
                            switch (autop[ap[0]][ap[1]]) {
                                case 0: //Metemos a la pila
                                    pilao.push(t);
                                    aux = false;
                                    break;
                                case 1: //Sacamos
                                    evaluar.add(pilao.pop());
                                    cadena += evaluar.getLast().Lexema() + " ";
                                    break;
                                case 2: //Sacamos y luego terminamos
                                    pilao.pop();
                                    aux = false;
                            }
                        }
                    }
            }
        }

        while (!pilao.empty()) {
            evaluar.add(pilao.pop());
            cadena += evaluar.getLast().Lexema() + " ";
        }

        for (Token tt : evaluar) {
            System.out.println(tt.Lexema() + " de tipo " + tt.Tipo());
            switch (tt.Tipo()) {
                case 1: //Para variables
                    pilae.push(Integer.parseInt(matriz_semantica.get(1).get(Buscar(tt.Lexema())))); //Obtenemos el tipo de dato de la variable
                    pilas.push(tt.Lexema());
                    break;
                case 35: // float
                    pilae.push(1);
                    pilas.push(tt.Lexema());
                    break;
                case 36: // int
                    pilae.push(0);
                    pilas.push(tt.Lexema());
                    break;
                case 37: // String
                    pilae.push(2);
                    pilas.push(tt.Lexema());
                    break;
                case 38: // char
                    pilae.push(3);
                    pilas.push(tt.Lexema());
                    break;
                case 39: // boolean
                    pilae.push(4);
                    pilas.push(tt.Lexema());
                    break;
                case 40: // boolean
                    pilae.push(4);
                    pilas.push(tt.Lexema());
                    break;

                default: //Osea que debe ser un operador
                    con--;
                    var2 = pilae.pop();
                    var1 = pilae.pop();
                    cadv1 = IncVa(var1);
                    cadv2 = IncVa(var2);

                    if (var2 == 2)
                        Agregar("strcpy( " + cadv2 + ", " + pilas.pop() + ");");
                    else
                        Agregar(cadv2 + " = " + pilas.pop() + ";");

                    if (var1 == 2)
                        Agregar("strcpy( " + cadv1 + ", " + pilas.pop() + ");");
                    else
                        Agregar(cadv1 + " = " + pilas.pop() + ";");

                    //Dividir en las operaciones y los casos específicos
                    switch (tt.Tipo()) {
                        case 22: // !
                        case 23: // &
                        case 24: // |
                            vart = autotl[var2][var1];
                            break;
                        case 25: // <
                        case 26: // >
                        case 27: // <=
                        case 28: // >=
                            if (var1 > 1 | var2 > 1) {
                                Error("Error semántico - Linea " + tt.Linea() + " - Los tipos de datos no son compatibles para la operación: " + Tipo(var1) + " " + tt.Lexema() + " " + Tipo(var2));
                                return new Token(42, "Error", tt.Linea());
                            }
                        case 29: // == Aquí es cuando se usaría el strcmp
                        case 30: // !=
                            vart = autotr[var2][var1];
                            break;
                        case 32: // -
                        case 33: // *
                        case 34: // /
                            if (var1 > 1 | var2 > 1) {
                                Error("Error semántico - Linea " + tt.Linea() + " - Los tipos de datos no son compatibles para la operación: " + Tipo(var1) + " " + tt.Lexema() + " " + Tipo(var2));
                                return new Token(42, "Error", tt.Linea());
                            }
                        case 31: // + 

                            vart = autota[var2][var1];

                    }

                    if (vart == var1) {
                        cadv = cadv1;
                    } else {
                        if (vart == var2)
                            cadv = cadv2;
                        else
                            cadv = IncVa(vart);
                    }

                    pilas.push(cadv);
                    DecVa(var1);
                    DecVa(var2);

                    if (vart == -1) {
                        Error("Error semántico - Linea " + tt.Linea() + " - Los tipos de datos no son compatibles para la operación: " + Tipo(var1) + " " + tt.Lexema() + " " + Tipo(var2));
                        return new Token(42, "Error", tt.Linea());
                    }

                    //**********************************************************************************************************
                    //Esto es algo a modificar para usar las operaciones especiales de cadena
                    //**********************************************************************************************************
                    switch (tt.Lexema()) {
                        case "==":
                            if (vart == 2) {
                                Agregar(cadv + " = strcmp(" + cadv1 + " , " + cadv2 + ") == 0;");
                                break;
                            } else
                                Agregar(cadv + " = " + cadv1 + " " + tt.Lexema() + " " + cadv2 + ";");
                        case "!=":
                            if (vart == 2) {
                                Agregar(cadv + " = strcmp(" + cadv1 + " , " + cadv2 + ") != 0;");
                                break;
                            } else
                                Agregar(cadv + " = " + cadv1 + " " + tt.Lexema() + " " + cadv2 + ";");
                        case "+":

                            if (vart == 2) {
                                switch (cadv1.charAt(1)) {
                                    case 's':
                                        Agregar("strcat(" + cadv + " , " + cadv1 + ");");
                                        break;
                                    case 'b':
                                        Agregar("sprintf(" + cadv + " , \"%d\" ," + cadv1 + ");");
                                        break;
                                    default:
                                        Agregar("sprintf(" + cadv + " , \"%d\" ," + cadv1 + ");");
                                }

                                break;
                            }

                        default:
                            Agregar(cadv + " = " + cadv1 + " " + tt.Lexema() + " " + cadv2 + ";");
                    }

                    pilae.push(vart);
            }
        }
        System.out.println("Teminó la evaluación para buscar a :" + principal.Lexema() + " de tipo " + principal.Tipo());
        if (evaluar.size() == 1) {
            vart = pilae.pop();
            cadv = pilas.pop();

        }

        if (principal.Tipo() == 7) {
            if (vart == 2)
                Agregar("strcpy ( " + principal.Lexema() + ", " + cadv + ");");
            else
                Agregar(principal.Lexema() + " = " + cadv + ";");
            return new Token(TipoCod(vart), "valSwitch", principal.Linea());

        }

        if (principal.Tipo() != 1) {
            if (vart != Tipoinv(principal.Tipo())) {
                Error("Error semántico - Linea " + principal.Linea() + " - El resultado de tipo " + Tipo(vart) + " de la expresión no concuerda con el parametro de tipo: " + principal.Lexema());
                return new Token(42, "Error", principal.Linea());
            }
        } else if (vart != (Integer.parseInt(matriz_semantica.get(1).get(Buscar(principal.Lexema()))))) {
            Error("Error semántico - Linea " + principal.Linea() + " - El resultado de tipo " + Tipo(vart) + " de la expresión no concuerda con la variable " + principal.Lexema() + " de tipo: " + Tipo((Integer.parseInt(matriz_semantica.get(1).get(Buscar(principal.Lexema()))))));
            return new Token(42, "Error", principal.Linea());
        }

        if (principal.Lexema().equals("valSwitch")) {
            cadv1 = IncVa(vart);
            if (vart == 2)
                Agregar("strcpy ( " + cadv1 + ", " + cadv + ");");
            else
                Agregar(cadv1 + " = " + cadv + ";");
            return new Token(principal.Tipo(), cadv1 , t.Linea());
        }
        

        if (vart == 2)
            Agregar("strcpy ( " + principal.Lexema() + ", " + cadv + ");");
        else
            Agregar(principal.Lexema() + " = " + cadv + ";");

        return new Token(principal.Tipo(), Tipo(vart), t.Linea());
    }

    public void Error(String msj) {
        System.out.println(msj);
        Area_Errores.setText(msj);
    }

    public void Agregar(String msj) {
        for (int x = 0; x <= tab; x++) {
            codigo_intermedio += "\t";
        }
        codigo_intermedio += msj + "\n";
    }

    public String Parametro(List<Token> exp) {
        String p = "", cad = "\" ", va  = "";

        for (Token tt : exp) {
            System.out.println(tt.Lexema() + " de tipo " + tt.Tipo());
            switch (tt.Tipo()) {
                case 1: //Para variables
                case 35: // float
                case 36: // int
                case 37: // String
                case 38: // char
                case 39: // boolean
                case 40: // boolean
                    va  += ", " + tt.Lexema();
            }

            switch (tt.Tipo()) {
                case 1: //Para variables
                    switch (matriz_semantica.get(1).get(Buscar(tt.Lexema()))) {
                        case "0" ->
                            cad += "%d ";
                        case "1" ->
                            cad += "%f ";
                        case "2" ->
                            cad += "%s ";
                        case "3" ->
                            cad += "%c ";
                        case "4" ->
                            cad += "%d ";
                    }
                    break;
                case 35: // float
                    cad += "%f ";
                    break;
                case 36: // int
                    cad += "%d ";
                    break;
                case 37: // String
                    cad += "%s ";
                    break;
                case 38: // char
                    cad += "%c ";
                    break;
                case 39: // boolean
                case 40: // boolean
                    cad += "%d ";
            }
        }

        cad += "\"";

        return cad + va;
    }

    public String IncVa(int tipo) {
        String v = "V";
        conta[tipo]++;
        if (contad[tipo] < conta[tipo])
            contad[tipo]++;
        switch (tipo) {
            case 0 ->
                v += "d";
            case 1 ->
                v += "f";
            case 2 ->
                v += "s";
            case 3 ->
                v += "c";
            case 4 ->
                v += "b";

        }

        v += conta[tipo];

        return v;
    }

    public void DecVa(int v) {
        conta[v]--;
    }

    public void IncWhile() {
        numw++;
    }

    public void IncSwitch() {
        nums++;
    }

    public void IncCase() {
        numc++;
    }

    public int Buscar(String id) {
        int x = -1;
        for (x = matriz_semantica.get(0).size() - 1; x > -1; x--) {
            if (matriz_semantica.get(0).get(x).equals(id))
                return x;
        }
        return x;
    }

    public String Validos(int est) {
        String val = "";
        List<String> estado = matriz.get(est);
        for (int x = 0; x < estado.size(); x++) {
            if (x >= columnas.indexOf("$"))
                return val;
            if (!estado.get(x).isBlank())
                val += columnas.get(x) + " ";
        }
        return val;
    }

    public void MostrarPila(Stack<String> pp) {
        Stack<String> pa = new Stack<>();
        String texto = Area_Sintactico.getText();
        while (!pp.isEmpty()) {
            pa.push(pp.pop());
        }
        while (!pa.isEmpty()) {
            texto += pp.push(pa.pop());
        }
        texto += "\n";
        Area_Sintactico.setText(texto);
    }

    public String Tipo(int t) {
        switch (t) {
            case -1 -> {
                return "Clase";
            }
            case 0 -> {
                return "int";
            }
            case 1 -> {
                return "float";
            }
            case 2 -> {
                return "char [30]";
            }
            case 3 -> {
                return "char";
            }
            case 4 -> {
                return "bool";
            }
        }
        return "Tipo no identificado";
    }

    public int Tipoinv(int t) {
        switch (t) {
            case 35 -> {
                return 1;
            }
            case 36 -> {
                return 0;
            }
            case 37 -> {
                return 2;
            }
            case 38 -> {
                return 3;
            }
            case 39 -> {
                return 4;
            }
            case 40 -> {
                return 4;
            }
        }
        return -1;
    }

    public int TipoCod(int t) {
        switch (t) {
            case 1 -> {
                return 25;
            }
            case 0 -> {
                return 36;
            }
            case 2 -> {
                return 37;
            }
            case 3 -> {
                return 38;
            }
            case 4 -> {
                return 39;
            }
        }
        return -1;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        textLine2 = new org.jfree.text.TextLine();
        Selector = new javax.swing.JFileChooser();
        Emergente = new javax.swing.JOptionPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        Area_Edicion = new javax.swing.JTextArea();
        jToolBar1 = new javax.swing.JToolBar();
        btn_nuevo = new javax.swing.JButton();
        btn_abrir = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        btn_guardar = new javax.swing.JButton();
        btn_guardarcomo = new javax.swing.JButton();
        jSeparator5 = new javax.swing.JToolBar.Separator();
        btn_correr = new javax.swing.JButton();
        jSeparator6 = new javax.swing.JToolBar.Separator();
        jSeparator7 = new javax.swing.JToolBar.Separator();
        btn_cerrar = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        Area_Errores = new javax.swing.JTextArea();
        jScrollPane4 = new javax.swing.JScrollPane();
        Area_Sintactico = new javax.swing.JTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        Area_Lexico = new javax.swing.JTextArea();
        jMenuBar1 = new javax.swing.JMenuBar();
        menu_archivo = new javax.swing.JMenu();
        op_nuevo = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        op_abrir = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        op_guardar = new javax.swing.JMenuItem();
        op_guardarcomo = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        op_cerrar = new javax.swing.JMenuItem();
        menu_opciones = new javax.swing.JMenu();
        op_correr = new javax.swing.JMenuItem();

        Selector.setCurrentDirectory(new java.io.File("C:\\Users\\Omen\\Documents\\NetBeansProjects\\Compilador_final\\src\\compiladorv2\\archivos\\Ejemplo1.txt"));

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(31, 31, 31));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setMinimumSize(new java.awt.Dimension(1250, 800));
        setResizable(false);

        Area_Edicion.setBackground(new java.awt.Color(51, 51, 51));
        Area_Edicion.setColumns(20);
        Area_Edicion.setFont(new java.awt.Font("Courier New", 0, 14)); // NOI18N
        Area_Edicion.setForeground(new java.awt.Color(204, 204, 204));
        Area_Edicion.setRows(5);
        Area_Edicion.setMaximumSize(new java.awt.Dimension(192, 89));
        Area_Edicion.setMinimumSize(new java.awt.Dimension(10, 10));
        jScrollPane1.setViewportView(Area_Edicion);

        jToolBar1.setRollover(true);

        btn_nuevo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/compiladorv2/img/new.png"))); // NOI18N
        btn_nuevo.setText("Nuevo");
        btn_nuevo.setFocusable(false);
        btn_nuevo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btn_nuevo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btn_nuevo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_nuevoActionPerformed(evt);
            }
        });
        jToolBar1.add(btn_nuevo);

        btn_abrir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/compiladorv2/img/open.png"))); // NOI18N
        btn_abrir.setText("Abrir");
        btn_abrir.setFocusable(false);
        btn_abrir.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btn_abrir.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btn_abrir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_abrirActionPerformed(evt);
            }
        });
        jToolBar1.add(btn_abrir);
        jToolBar1.add(jSeparator4);

        btn_guardar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/compiladorv2/img/save.png"))); // NOI18N
        btn_guardar.setText("Guardar");
        btn_guardar.setFocusable(false);
        btn_guardar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btn_guardar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btn_guardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_guardarActionPerformed(evt);
            }
        });
        jToolBar1.add(btn_guardar);

        btn_guardarcomo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/compiladorv2/img/save as.png"))); // NOI18N
        btn_guardarcomo.setText("Guardar como");
        btn_guardarcomo.setFocusable(false);
        btn_guardarcomo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btn_guardarcomo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btn_guardarcomo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_guardarcomoActionPerformed(evt);
            }
        });
        jToolBar1.add(btn_guardarcomo);
        jToolBar1.add(jSeparator5);

        btn_correr.setIcon(new javax.swing.ImageIcon(getClass().getResource("/compiladorv2/img/play.png"))); // NOI18N
        btn_correr.setText("Correr");
        btn_correr.setFocusable(false);
        btn_correr.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btn_correr.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btn_correr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_correrActionPerformed(evt);
            }
        });
        jToolBar1.add(btn_correr);
        jToolBar1.add(jSeparator6);
        jToolBar1.add(jSeparator7);

        btn_cerrar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/compiladorv2/img/close.png"))); // NOI18N
        btn_cerrar.setText("Cerrar");
        btn_cerrar.setFocusable(false);
        btn_cerrar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btn_cerrar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btn_cerrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_cerrarActionPerformed(evt);
            }
        });
        jToolBar1.add(btn_cerrar);

        jLabel1.setFont(new java.awt.Font("Roboto Light", 1, 18)); // NOI18N
        jLabel1.setText("Análisis sintáctico");

        jLabel2.setFont(new java.awt.Font("Roboto Light", 1, 18)); // NOI18N
        jLabel2.setText("Análisis léxico");

        Area_Errores.setBackground(new java.awt.Color(51, 51, 51));
        Area_Errores.setColumns(20);
        Area_Errores.setFont(new java.awt.Font("Courier New", 0, 14)); // NOI18N
        Area_Errores.setForeground(new java.awt.Color(204, 204, 204));
        Area_Errores.setRows(5);
        jScrollPane2.setViewportView(Area_Errores);

        Area_Sintactico.setBackground(new java.awt.Color(51, 51, 51));
        Area_Sintactico.setColumns(20);
        Area_Sintactico.setFont(new java.awt.Font("Courier New", 0, 14)); // NOI18N
        Area_Sintactico.setForeground(new java.awt.Color(204, 204, 204));
        Area_Sintactico.setRows(5);
        jScrollPane4.setViewportView(Area_Sintactico);

        Area_Lexico.setEditable(false);
        Area_Lexico.setBackground(new java.awt.Color(51, 51, 51));
        Area_Lexico.setColumns(20);
        Area_Lexico.setFont(new java.awt.Font("Courier New", 0, 14)); // NOI18N
        Area_Lexico.setForeground(new java.awt.Color(204, 204, 204));
        Area_Lexico.setRows(5);
        jScrollPane3.setViewportView(Area_Lexico);

        jMenuBar1.setBackground(new java.awt.Color(38, 38, 38));
        jMenuBar1.setForeground(new java.awt.Color(51, 51, 51));

        menu_archivo.setText("Archivo");
        menu_archivo.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        op_nuevo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        op_nuevo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/compiladorv2/img/new.png"))); // NOI18N
        op_nuevo.setText("Nuevo");
        op_nuevo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                op_nuevoActionPerformed(evt);
            }
        });
        menu_archivo.add(op_nuevo);
        menu_archivo.add(jSeparator1);

        op_abrir.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        op_abrir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/compiladorv2/img/open.png"))); // NOI18N
        op_abrir.setText("Abrir");
        op_abrir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                op_abrirActionPerformed(evt);
            }
        });
        menu_archivo.add(op_abrir);
        menu_archivo.add(jSeparator2);

        op_guardar.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        op_guardar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/compiladorv2/img/save.png"))); // NOI18N
        op_guardar.setText("Guardar");
        op_guardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                op_guardarActionPerformed(evt);
            }
        });
        menu_archivo.add(op_guardar);

        op_guardarcomo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G, java.awt.event.InputEvent.ALT_DOWN_MASK | java.awt.event.InputEvent.CTRL_DOWN_MASK));
        op_guardarcomo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/compiladorv2/img/save as.png"))); // NOI18N
        op_guardarcomo.setText("Guardar como");
        op_guardarcomo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                op_guardarcomoActionPerformed(evt);
            }
        });
        menu_archivo.add(op_guardarcomo);
        menu_archivo.add(jSeparator3);

        op_cerrar.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0));
        op_cerrar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/compiladorv2/img/close.png"))); // NOI18N
        op_cerrar.setText("Cerrar");
        op_cerrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                op_cerrarActionPerformed(evt);
            }
        });
        menu_archivo.add(op_cerrar);

        jMenuBar1.add(menu_archivo);

        menu_opciones.setText("Opciones");
        menu_opciones.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        op_correr.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        op_correr.setIcon(new javax.swing.ImageIcon(getClass().getResource("/compiladorv2/img/play.png"))); // NOI18N
        op_correr.setText("Correr");
        op_correr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                op_correrActionPerformed(evt);
            }
        });
        menu_opciones.add(op_correr);

        jMenuBar1.add(menu_opciones);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 1250, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 644, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 576, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1))))
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addContainerGap(668, Short.MAX_VALUE)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 576, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap()))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 469, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(6, 6, 6)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(jLabel1)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(8, Short.MAX_VALUE))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addContainerGap(324, Short.MAX_VALUE)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(239, 239, 239)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void op_abrirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_op_abrirActionPerformed
        Seleccionar();
    }//GEN-LAST:event_op_abrirActionPerformed

    private void op_guardarcomoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_op_guardarcomoActionPerformed
        GuardarComo();
    }//GEN-LAST:event_op_guardarcomoActionPerformed

    private void op_correrActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_op_correrActionPerformed
        Area_Errores.setText("");
        Lexico();
    }//GEN-LAST:event_op_correrActionPerformed

    private void op_nuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_op_nuevoActionPerformed
        Vaciar();
    }//GEN-LAST:event_op_nuevoActionPerformed

    private void op_cerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_op_cerrarActionPerformed
        Vaciar();
    }//GEN-LAST:event_op_cerrarActionPerformed

    private void btn_abrirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_abrirActionPerformed
        Seleccionar();
    }//GEN-LAST:event_btn_abrirActionPerformed

    private void btn_cerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_cerrarActionPerformed
        Vaciar();
    }//GEN-LAST:event_btn_cerrarActionPerformed

    private void btn_guardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_guardarActionPerformed
        Guardar();
    }//GEN-LAST:event_btn_guardarActionPerformed

    private void btn_guardarcomoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_guardarcomoActionPerformed
        GuardarComo();
    }//GEN-LAST:event_btn_guardarcomoActionPerformed

    private void op_guardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_op_guardarActionPerformed
        Guardar();
    }//GEN-LAST:event_op_guardarActionPerformed

    private void btn_nuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_nuevoActionPerformed
        Vaciar();
    }//GEN-LAST:event_btn_nuevoActionPerformed

    private void btn_correrActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_correrActionPerformed
        Area_Errores.setText("");

        Lexico();
    }//GEN-LAST:event_btn_correrActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Interfaz.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Interfaz.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Interfaz.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Interfaz.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        /* Create and display the form */

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Interfaz().setVisible(true);

            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea Area_Edicion;
    private javax.swing.JTextArea Area_Errores;
    private javax.swing.JTextArea Area_Lexico;
    private javax.swing.JTextArea Area_Sintactico;
    private javax.swing.JOptionPane Emergente;
    private javax.swing.JFileChooser Selector;
    private javax.swing.JButton btn_abrir;
    private javax.swing.JButton btn_cerrar;
    private javax.swing.JButton btn_correr;
    private javax.swing.JButton btn_guardar;
    private javax.swing.JButton btn_guardarcomo;
    private javax.swing.JButton btn_nuevo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JToolBar.Separator jSeparator6;
    private javax.swing.JToolBar.Separator jSeparator7;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JMenu menu_archivo;
    private javax.swing.JMenu menu_opciones;
    private javax.swing.JMenuItem op_abrir;
    private javax.swing.JMenuItem op_cerrar;
    private javax.swing.JMenuItem op_correr;
    private javax.swing.JMenuItem op_guardar;
    private javax.swing.JMenuItem op_guardarcomo;
    private javax.swing.JMenuItem op_nuevo;
    private org.jfree.text.TextLine textLine2;
    // End of variables declaration//GEN-END:variables
}
