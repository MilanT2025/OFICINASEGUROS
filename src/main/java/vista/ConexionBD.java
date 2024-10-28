/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vista;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 *
 * @author ejmg3
 */
public class ConexionBD {
    // Conexion base de datos de servidor Galenos

    static Connection cn = null;

    static String usuario = "sa";
    static String contraseña = "123456";
    static String bd = "BD_ASISTENCIA";
    static String ip = "192.168.10.114";
    static String puerto = "1433;" + "encrypt=true;trustServerCertificate=true";

    String cadena = "jdbc:sqlserver://" + ip + ";" + puerto + "/" + bd;

    public static Connection establecerConexion() {
        try {
            String cadena = "jdbc:sqlserver://localhost\\SQLEXPRESS:" + puerto + ";" + "databaseName=" + bd;
            cn = DriverManager.getConnection(cadena, usuario, contraseña);
            JOptionPane.showMessageDialog(null, "Se conecto correctamente a la base de datos");
        } catch (SQLException e) {
//            JOptionPane.showMessageDialog(null, "Error al conectar a la base de datos, error: " + e.toString());
        }
        return cn;
    }

}
