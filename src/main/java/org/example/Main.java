package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.HashMap;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static Scanner sc = new Scanner(System.in);
    public static ConexionMySQL SQL = new ConexionMySQL();
    public static Connection conn = SQL.conectarMySQL();
    // Cola de espera la sala de roblox, inicia vacío
    public static Queue<Integer> colaRoblox = new LinkedList<>();
    // Cola de para índices libres en la sala de roblox, inicia vacío
    public static Queue<Integer> freeRoblox = new LinkedList<>();
    // Contador para marcar índices para los lugares en las salas
    public static Integer contaRoblox = 0;

    public static void main(String[] args) {
        iniConta();
        System.out.println("Roblox: " + contaRoblox);
        menu();
    }

    public static void menu() {
        int opcion;
        do {
            System.out.println("\n===== Menú Principal =====");
            System.out.println("1. Mostrar lugares de la sala");
            System.out.println("2. Mostrar lista de jugadores");
            System.out.println("3. Buscar a un jugador");
            System.out.println("4. Unirse a la sala");
            System.out.println("5. Salir de la sala");
            System.out.println("7. Salir");
            System.out.println("8. Ver contador");
            System.out.println("9. Ver cola de espera");
            System.out.print("Ingrese la opción deseada: ");
            opcion = sc.nextInt();
            switch (opcion) {
                case 1: // Lista todas las salas, sus lugares y estado
                    sala_lugares();
                    break;
                case 2: // Lista todos los jugadores y su estado
                    lista_jugadores();
                    break;
                case 3: // Busca un jugador en específico
                    String mensaje_jug_rastro = "Ingresa el nombre del jugador";
                    String jugsql_rastro = "SELECT id_jugador FROM jugador\n" +
                            "WHERE jugador = ?;";
                    String id_jug_rastro = "id_jugador";
                    sc.nextLine();
                    int id_jugador_rastro = lectura_teclado_id(mensaje_jug_rastro, jugsql_rastro, id_jug_rastro);

                    buscaJugador(id_jugador_rastro);
                    break;
                case 4: // Se une a una sala
                    String mensaje_jug = "Ingresa el nombre del jugador";
                    String jugsql = "SELECT id_jugador FROM jugador\n" +
                            "WHERE jugador = ?;";
                    String id_jug = "id_jugador";
                    sc.nextLine();
                    int id_jugador = lectura_teclado_id(mensaje_jug, jugsql, id_jug);

                    String mensaje_sala = "Ingresa el nombre de la sala";
                    String salasql = "SELECT id_sala FROM sala\n" +
                            "WHERE sala = ?;";
                    String id_sal = "id_sala";
                    int id_sala = lectura_teclado_id(mensaje_sala, salasql, id_sal);

                    unirse(id_jugador, id_sala);
                    break;
                case 5: // Se sale de una sala
                    String mensaje_jug_exit = "Ingresa el nombre del jugador";
                    String jugsql_exit = "SELECT id_jugador FROM jugador\n" +
                            "WHERE jugador = ?;";
                    String id_jug_exit = "id_jugador";
                    sc.nextLine();
                    int id_jugador_exit = lectura_teclado_id(mensaje_jug_exit, jugsql_exit, id_jug_exit);

                    int id_sala_exit = 0;
                    int id_lugar_exit = 0;
                    HashMap<String, Integer> hashJugador = buscaJugador(id_jugador_exit);
                    id_sala_exit = hashJugador.get("id_sala");
                    id_lugar_exit = hashJugador.get("id_lugar");
                    System.out.println("Sala: " + id_sala_exit + " - Lugar a desocupar: " + id_lugar_exit);
                    if (id_sala_exit != 0 && id_lugar_exit != 0) {
                        salirse(id_sala_exit, id_lugar_exit);
                    } else {
                        System.out.println("No pos no se pudo hacer lo de salir");
                    }
                    break;
                case 8:
                    System.out.println("Roblox: " + contaRoblox);
                    break;
                case 9:
                    mostrarCola();
                    break;
                default:
                    break;
            }
        } while (opcion != 7);
    }

    public static void unirse(int id_jugador, int id_sala){
        System.out.println("Sala: " + id_sala + ", Jugador: " + id_jugador);
        String sSQL =  "UPDATE sala_lugares SET id_jugador = ?"
                + " WHERE id_sala = ? AND id_lugar = ?";
        int lugar = contaRoblox;
        boolean flag = freeRoblox.isEmpty(); // Vacía = se está siguiendo el flujo normal
        System.out.println("El lugar es: " + lugar);
        if (lugar <= 8 || !flag){ // Si aún hay lugares disponibles
            try{
                PreparedStatement pstm = conn.prepareStatement(sSQL);
                pstm.setInt(1, id_jugador); // Id del jugador
                pstm.setInt(2, id_sala); // Id de la sala
                pstm.setInt(3, flag ? lugar : freeRoblox.poll()); // Lugar en la sala
                int rowsInserted = pstm.executeUpdate();
                if (rowsInserted > 0) {
                    System.out.println("¡Se unió exitosamente el jugador a la sala!");
                    if(flag) {
                        contaRoblox++;
                    }
                }
            } catch (SQLException e) {
                System.out.println("ERROR: " + e);
            }
        } else {
            System.out.println("No hay más lugares disponibles en esta sala, por favor, ingrese a la cola de espera.");
            colaRoblox.add(id_jugador);
        }
    }

    public static void salirse(int id_sala, int id_lugar){
        String sSQL =  "UPDATE sala_lugares SET id_jugador = null"
                + " WHERE id_sala = ? AND id_lugar = ?";
        int lugar = contaRoblox;
        try{
            PreparedStatement pstm = conn.prepareStatement(sSQL);
            pstm.setInt(1, id_sala); // Id de la sala
            pstm.setInt(2, id_lugar); // Lugar en la sala
            int rowsInserted = pstm.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Se salió exitosamente el jugador de la sala!");
                if(id_lugar == lugar-1) { // Significa que se está saliendo el último lugar del contador
                    // Resta al contador nomás
                    contaRoblox--; // Disminuye el valor del contador de sala correspondiente
                } else {
                    // No se resta al contador pero el lugar disponible se agrega a la colaLibres
                    freeRoblox.add(id_lugar);
                }
                if(!colaRoblox.isEmpty()) { // Si hay jugadores en la cola de espera, en seguida los mete
                    unirse(colaRoblox.poll(), 1);
                }
            }
        } catch (SQLException e) {
            System.out.println("ERROR: " + e);
        }
        if (lugar != 0) { // Si aún hay lugares disponibles
        }
    }

    public static void iniConta(){ // Inicializa el contador con los valores de la BD
        String query = "SELECT MAX(id_lugar) AS max_id_lugar FROM sala_lugares WHERE id_sala = ? AND id_jugador IS NOT NULL";
        try {
            PreparedStatement pstmRoblox = conn.prepareStatement(query);
            pstmRoblox.setInt(1, 1);
            ResultSet rsRoblox = pstmRoblox.executeQuery();
            if (rsRoblox.next()) {
                Integer robInt = rsRoblox.getInt("max_id_lugar");
                //robInt = robInt==3 ? robInt : robInt+1;
                contaRoblox = !rsRoblox.wasNull() ? robInt+1 : 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void sala_lugares(){ // Muestra la lista de lugares y su estado
        String sSQL =  "SELECT s.sala, sl.id_lugar, j.jugador FROM sala_lugares sl\n" +
                "JOIN sala s ON s.id_sala = sl.id_sala\n" +
                "LEFT JOIN jugador j ON j.id_jugador = sl.id_jugador;";
        try{
            PreparedStatement pstm = conn.prepareStatement(sSQL);
            ResultSet rs = pstm.executeQuery();

            String lastSala = ""; // Variable para almacenar el último nombre de la sala impreso
            while (rs.next()) {
                String currentSala = rs.getString("sala");
                if (!currentSala.equals(lastSala)) {
                    System.out.println("-- SALA: " + currentSala + " --");
                    lastSala = currentSala;
                }
                System.out.println("Lugar " + rs.getInt("id_lugar") + ": "
                        + (rs.getString("jugador") != null ? rs.getString("jugador") : "Disponible"));
            }
        } catch (SQLException e) {
            System.out.println("ERROR: " + e);
        }
    }

    public static void lista_jugadores(){ // Muestra la lista de los jugadores y si están en una sala
        String sSQL =  "SELECT j.id_jugador, j.jugador, s.sala, sl.id_lugar FROM jugador j\n" +
                "LEFT JOIN sala_lugares sl ON j.id_jugador = sl.id_jugador\n" +
                "LEFT JOIN sala s ON s.id_sala = sl.id_sala;";
        try{
            PreparedStatement pstm = conn.prepareStatement(sSQL);
            ResultSet rs = pstm.executeQuery();

            while (rs.next()) {
                System.out.println("Jugador: " + rs.getString("jugador")
                        + " - Sala: " + (rs.getString("sala") != null ? rs.getString("sala") : (buscarEnCola(rs.getInt("id_jugador")) ? "En cola de espera" : "En el lobby"))
                        + (rs.getInt("id_lugar") != 0 ? (" - Lugar: " + rs.getInt("id_lugar")) : ""));
            }
        } catch (SQLException e) {
            System.out.println("ERROR: " + e);
        }
    }

    public static HashMap<String, Integer> buscaJugador(int id_jugador){
        HashMap<String,Integer> jugador = new HashMap<>();
        String sSQL =  "SELECT j.id_jugador, j.jugador, s.id_sala, s.sala, sl.id_lugar FROM jugador j\n" +
                "LEFT JOIN sala_lugares sl ON j.id_jugador = sl.id_jugador\n" +
                "LEFT JOIN sala s ON s.id_sala = sl.id_sala\n" +
                "WHERE j.id_jugador = ?;";
        try {
            PreparedStatement pstm = conn.prepareStatement(sSQL);
            pstm.setInt(1, id_jugador); // Id del jugador
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                System.out.println("Jugador: " + rs.getString("jugador")
                        + " - Sala: " + (rs.getString("sala") != null ? rs.getString("sala") : (buscarEnCola(rs.getInt("id_jugador")) ? "En cola de espera" : "En el lobby"))
                        + (rs.getInt("id_lugar") != 0 ? (" - Lugar: " + rs.getInt("id_lugar")) : ""));
                jugador.put("id_jugador", rs.getInt("id_jugador"));
                jugador.put("id_sala", rs.getInt("id_sala"));
                jugador.put("id_lugar", rs.getInt("id_lugar"));
            }
            return jugador;
        } catch (SQLException e) {
            System.out.println("ERROR: " + e);
            return null;
        }
    }

    public static int lectura_teclado_id(String mensaje, String query, String param_name){
        int id = 0;
        System.out.println(mensaje);
        String value = sc.nextLine();
        try {
            PreparedStatement pstm = conn.prepareStatement(query);
            pstm.setString(1,value);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                id = rs.getInt(param_name);
            }
            rs.close();
            pstm.close();
        } catch (SQLException e) {
            System.out.println("Error: " + e);
            return 0;
        }
        return id;
    }

    public static boolean buscarEnCola(int value) {
        // Recorrer la cola para buscar el valor
        for (Integer element : colaRoblox) {
            if (element == value) {
                return true;
            }
        }
        return false;
    }

    public static void mostrarCola() {
        System.out.println("En cola de espera: ");
        for (Integer valor : colaRoblox) {
            System.out.println(valor);
        }
        System.out.println();
    }
}