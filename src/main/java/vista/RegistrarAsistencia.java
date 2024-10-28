/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package vista;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

/**
 *
 * @author ejmg3
 */
public class RegistrarAsistencia extends javax.swing.JDialog {

    String hora, ampm, fecha, horac;
    Calendar calendario;
    Thread h1;
    Connection connection;
    PreparedStatement preparedStatement;
    ResultSet resultSet;

    public RegistrarAsistencia(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        setTitle("SISTEMA DE CONTROL DE ASISTENCIA");
        setSize(800, 800);
        setLocationRelativeTo(null);
        DefaultTableModel modelo = new DefaultTableModel();
        // Actualizar la fecha y la hora cada segundo
        Timer timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actualizarFechaYHora();
            }
        });
        timer.start();

        actualizarFechaYHora(); // Llamar una vez para mostrar la fecha y hora inmediatamente

        labelFechaHora = new JLabel();
        labelFechaHora.setFont(new Font("Arial", Font.BOLD, 24));
        labelFechaHora.setBounds(50, 50, 50, 50);

        // Personalizar el encabezado de la tabla
        JTableHeader header = tabla_asistencia.getTableHeader();
        header.setPreferredSize(new java.awt.Dimension(header.getWidth(), 50)); //establece la altura
        TableColumnModel columnModel = tabla_asistencia.getColumnModel();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setHorizontalAlignment(JLabel.CENTER); // Centrar el texto del encabezado
                //cambiar el color del encabezado
                label.setOpaque(true); //necesario para que el color de fondo se aplique
                label.setBackground(new Color(0, 57, 238)); //establece el color de fondo
                label.setForeground(new Color(255, 255, 255)); //establece el color de texto
                label.setFont(new java.awt.Font("Roboto", java.awt.Font.BOLD, 12));
                return label;
            }
        });
        //Establece la altura de las filas
        tabla_asistencia.setRowHeight(50);

        tabla_asistencia.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        ActualizarDatos();
    }

    private void actualizarFechaYHora() {
        // Obtener la fecha y hora actual
        String horaActual = new SimpleDateFormat("HH:mm:ss").format(new Date());
        String fechaActual = new SimpleDateFormat("dd/MM/yyyy").format(new Date());

        labelFechaHora.setText("Fecha: " + fechaActual + " - Hora: " + horaActual);

    }
    
    private void ActualizarDatos(){
        try {
            String[] data = new String[7];
            DefaultTableModel modelo = (DefaultTableModel) tabla_asistencia.getModel();
            modelo.setRowCount(0);
            Connection cn = ConexionBD.establecerConexion();
            Statement st = cn.createStatement();
            String sql = "SELECT "
                    + "	E.DNI, "
                    + "	E.Nombres + ' ' + E.Apellidos AS Personal, "
                    + "	CONVERT(VARCHAR(8), HoraIngreso, 108) AS HoraIngreso, "
                    + "	CONVERT(VARCHAR(8), BreakSalida, 108) AS BreakSalida, "
                    + "	CONVERT(VARCHAR(8), BreakIngreso, 108) AS BreakIngreso, "
                    + "	CONVERT(VARCHAR(8), HoraSalida, 108) AS HoraSalida, "
                    + "	FORMAT(A.Fecha, 'dd/MM/yyyy') AS Fecha FROM M_SisAsistencia A "
                    + "INNER JOIN Empleados E ON A.idEmpleado = E.idEmpleado "
                    + "WHERE Fecha = CONVERT(DATE, GETDATE()) "
                    + "ORDER BY Fecha, HoraIngreso";
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                for (int i = 0; i < data.length; i++) {
                    data[i] = rs.getString(i + 1);
                }
                modelo.addRow(data);
            }
        } catch (SQLException ex) {
            Logger.getLogger(RegistrarAsistencia.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void RegistrarEntrada() {
        try {
            Connection cn = ConexionBD.establecerConexion();
            Statement st = cn.createStatement();
            String sql = "SELECT idEmpleado FROM Empleados WHERE Usuario = '" + txtUsuario.getText().trim() + "' AND Contraseña = '" + passContraseña.getText().trim()+ "'";
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()) {
                sql = "INSERT INTO [dbo].[M_SisAsistencia] "
                        + "           ([idEmpleado] "
                        + "           ,[Fecha] "
                        + "           ,[HoraIngreso]) "
                        + "     VALUES "
                        + "           (? "
                        + "           ,GETDATE() "
                        + "           ,GETDATE())";
                PreparedStatement pst = cn.prepareStatement(sql);
                pst.setInt(1, rs.getInt(1));
                int n = pst.executeUpdate();
                if (n > 0) {
                    JOptionPane.showMessageDialog(null, "Entrada registrada correctamente");
                } else {
                    JOptionPane.showMessageDialog(null, "Error al registrar entrada");
                }
                pst.close();

            } else {
                JOptionPane.showMessageDialog(null, "Verifique el Usuario y/o Contraseña", "Error", JOptionPane.ERROR_MESSAGE);
                txtUsuario.requestFocus();
                txtUsuario.selectAll();
            }
            
            rs.close();
            st.close();
            cn.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

    }

    private void RegistroAlmuerzo1() {
         try {
            Connection cn = ConexionBD.establecerConexion();
            Statement st = cn.createStatement();
            String sql = "SELECT idEmpleado FROM Empleados WHERE Usuario = '" + txtUsuario.getText().trim() + "' AND Contraseña = '" + passContraseña.getText().trim()+ "'";
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()) {
                sql = "UPDATE M_SisAsistencia "
                        + "SET BreakSalida = GETDATE() "
                        + "WHERE idEmpleado = ? AND Fecha = CONVERT(DATE, GETDATE())";
                PreparedStatement pst = cn.prepareStatement(sql);
                pst.setInt(1, rs.getInt(1));
                int n = pst.executeUpdate();
                if (n > 0) {
                    JOptionPane.showMessageDialog(null, "Registro actualizado correctamente");
                } else {
                    JOptionPane.showMessageDialog(null, "Error al registrar salida");
                }
                pst.close();

            } else {
                JOptionPane.showMessageDialog(null, "Verifique el Usuario y/o Contraseña", "Error", JOptionPane.ERROR_MESSAGE);
                txtUsuario.requestFocus();
                txtUsuario.selectAll();
            }
            
            rs.close();
            st.close();
            cn.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        
    }

    private void RegistroAlmuerzo2() {
        try {
            Connection cn = ConexionBD.establecerConexion();
            Statement st = cn.createStatement();
            String sql = "SELECT idEmpleado FROM Empleados WHERE Usuario = '" + txtUsuario.getText().trim() + "' AND Contraseña = '" + passContraseña.getText().trim()+ "'";
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()) {
                sql = "UPDATE M_SisAsistencia "
                        + "SET BreakIngreso = GETDATE() "
                        + "WHERE idEmpleado = ? AND Fecha = CONVERT(DATE, GETDATE())";
                
                PreparedStatement pst = cn.prepareStatement(sql);
                pst.setInt(1, rs.getInt(1));
                int n = pst.executeUpdate();
                if (n > 0) {
                    JOptionPane.showMessageDialog(null, "Registro actualizado correctamente");
                } else {
                    JOptionPane.showMessageDialog(null, "Error al registrar entrada");
                }
                pst.close();

            } else {
                JOptionPane.showMessageDialog(null, "Verifique el Usuario y/o Contraseña", "Error", JOptionPane.ERROR_MESSAGE);
                txtUsuario.requestFocus();
                txtUsuario.selectAll();
            }
            
            rs.close();
            st.close();
            cn.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void RegistroSalida() {
        try {
            Connection cn = ConexionBD.establecerConexion();
            Statement st = cn.createStatement();
            String sql = "SELECT idEmpleado FROM Empleados WHERE Usuario = '" + txtUsuario.getText().trim() + "' AND Contraseña = '" + passContraseña.getText().trim()+ "'";
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()) {
                sql = "UPDATE M_SisAsistencia "
                        + "SET HoraSalida = GETDATE() "
                        + "WHERE idEmpleado = ? AND Fecha = CONVERT(DATE, GETDATE())";
                
                PreparedStatement pst = cn.prepareStatement(sql);
                pst.setInt(1, rs.getInt(1));
                int n = pst.executeUpdate();
                if (n > 0) {
                    JOptionPane.showMessageDialog(null, "Salida registrada correctamente");
                } else {
                    JOptionPane.showMessageDialog(null, "Error al registrar salida");
                }
                pst.close();

            } else {
                JOptionPane.showMessageDialog(null, "Verifique el Usuario y/o Contraseña", "Error", JOptionPane.ERROR_MESSAGE);
                txtUsuario.requestFocus();
                txtUsuario.selectAll();
            }
            
            rs.close();
            st.close();
            cn.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

    }

    
    private void calcula() {
        Calendar calendario = new GregorianCalendar();
        Date fechahora = new Date();
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat forhora = new SimpleDateFormat("hh:mm:ss");
        calendario.setTime(fechahora);
        ampm = calendario.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM";
        fecha = formato.format(fechahora);

        hora = forhora.format(fechahora);

    }

    
    private void LlenarTabla(String usuario, String contraseña, String es) {
        String sql = "SELECT * FROM M_SisAsistencia WHERE Usuario LIKE '" +usuario+ "' AND Contraseña = '"+contraseña+"'";

        try (Connection cn = ConexionBD.establecerConexion(); PreparedStatement pst = cn.prepareStatement(sql)) {

            if (cn == null) {
                JOptionPane.showMessageDialog(null, "No se pudo establecer la conexión con la base de datos.");
                return;
            }

            // Establecer parámetros
            pst.setString(1, usuario);
            pst.setString(2, contraseña);

            // Ejecutar la consulta
            ResultSet rs = pst.executeQuery();

            DefaultTableModel modelo = new DefaultTableModel();
            String[] datos = new String[12];

            if (!rs.isBeforeFirst()) { // Verifica si hay resultados
                JOptionPane.showMessageDialog(null, "No existe ningún registro con ese usuario y contraseña.");
                return;
            }

            while (rs.next()) {
                datos[0] = rs.getString(5);
                datos[1] = rs.getString(6);
                datos[2] = hora; // Asegúrate de que 'hora' esté definido
                if (es.equals("2")) {
                    datos[3] = hora;
                } else {
                    datos[3] = "hora sin registrar";
                }
                datos[4] = fecha; // Asegúrate de que 'fecha' esté definido

                modelo.addRow(datos);
            }
            tabla_asistencia.setModel(modelo);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error de SQL: " + ex.getMessage());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error inesperado: " + ex.getMessage());
        }
    }
    
    private void limpiarCampos(){
        txtUsuario.setText("");
        passContraseña.setText("");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        labelFechaHora = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        txtUsuario = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        cbxRegistro = new javax.swing.JComboBox<>();
        jScrollPane2 = new javax.swing.JScrollPane();
        tabla_asistencia = new javax.swing.JTable();
        btnNuevo = new javax.swing.JButton();
        btnRegistro = new javax.swing.JButton();
        passContraseña = new javax.swing.JPasswordField();

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(0, 57, 238));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("REGISTRO DE ASISTENCIA");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        labelFechaHora.setBackground(new java.awt.Color(255, 255, 255));
        labelFechaHora.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        labelFechaHora.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel2.setText("USUARIO:");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel3.setText("CONTRASEÑA:");

        txtUsuario.setMinimumSize(new java.awt.Dimension(64, 20));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel4.setText("REGISTRO MI:");

        cbxRegistro.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        cbxRegistro.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "<<Seleccione Uno>>", "Entrada", "Break Salida", "Break Ingreso", "Salida" }));
        cbxRegistro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxRegistroActionPerformed(evt);
            }
        });

        tabla_asistencia.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "DNI", "PERSONAL", "ENTRADA", "<html><center>BREAK<br>SALIDA</center></html>", "<html><center>BREAK<br>INGRESO</center></html>", "SALIDA", "FECHA"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tabla_asistencia.setShowHorizontalLines(true);
        jScrollPane2.setViewportView(tabla_asistencia);
        if (tabla_asistencia.getColumnModel().getColumnCount() > 0) {
            tabla_asistencia.getColumnModel().getColumn(0).setResizable(false);
            tabla_asistencia.getColumnModel().getColumn(0).setPreferredWidth(110);
            tabla_asistencia.getColumnModel().getColumn(1).setResizable(false);
            tabla_asistencia.getColumnModel().getColumn(1).setPreferredWidth(110);
            tabla_asistencia.getColumnModel().getColumn(2).setResizable(false);
            tabla_asistencia.getColumnModel().getColumn(2).setPreferredWidth(110);
            tabla_asistencia.getColumnModel().getColumn(3).setResizable(false);
            tabla_asistencia.getColumnModel().getColumn(3).setPreferredWidth(110);
            tabla_asistencia.getColumnModel().getColumn(4).setResizable(false);
            tabla_asistencia.getColumnModel().getColumn(4).setPreferredWidth(110);
            tabla_asistencia.getColumnModel().getColumn(5).setResizable(false);
            tabla_asistencia.getColumnModel().getColumn(5).setPreferredWidth(110);
            tabla_asistencia.getColumnModel().getColumn(6).setResizable(false);
            tabla_asistencia.getColumnModel().getColumn(6).setPreferredWidth(118);
        }

        btnNuevo.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnNuevo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/agregar.png"))); // NOI18N
        btnNuevo.setText("NUEVO");
        btnNuevo.setMaximumSize(new java.awt.Dimension(129, 37));
        btnNuevo.setMinimumSize(new java.awt.Dimension(129, 37));
        btnNuevo.setPreferredSize(new java.awt.Dimension(129, 37));
        btnNuevo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevoActionPerformed(evt);
            }
        });

        btnRegistro.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnRegistro.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/registro1.png"))); // NOI18N
        btnRegistro.setText("REGISTRAR");
        btnRegistro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegistroActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(40, 40, 40)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(50, 50, 50)
                        .addComponent(txtUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 307, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(37, 37, 37)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3))
                        .addGap(33, 33, 33)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(passContraseña, javax.swing.GroupLayout.PREFERRED_SIZE, 306, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cbxRegistro, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(228, Short.MAX_VALUE))
            .addComponent(jScrollPane2)
            .addComponent(labelFechaHora, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addComponent(btnRegistro, javax.swing.GroupLayout.PREFERRED_SIZE, 293, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnNuevo, javax.swing.GroupLayout.PREFERRED_SIZE, 293, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(37, 37, 37))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(labelFechaHora, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 42, Short.MAX_VALUE)
                    .addComponent(passContraseña))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbxRegistro, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(26, 26, 26)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 279, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnNuevo, javax.swing.GroupLayout.DEFAULT_SIZE, 82, Short.MAX_VALUE)
                    .addComponent(btnRegistro, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnRegistroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegistroActionPerformed
        if (cbxRegistro.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(null, "Seleccione una opcion de registro", "Error", JOptionPane.ERROR_MESSAGE);
            cbxRegistro.showPopup();
            return;
        }
        
        if (cbxRegistro.getSelectedItem().equals("Entrada")) {
            RegistrarEntrada();
            limpiarCampos();
            ActualizarDatos();

        }
        if (cbxRegistro.getSelectedItem().equals("Break Salida")) {
            RegistroAlmuerzo1();
            limpiarCampos();
            ActualizarDatos();

        }
        if (cbxRegistro.getSelectedItem().equals("Break Ingreso")) {
            RegistroAlmuerzo2();
            limpiarCampos();
            ActualizarDatos();

        }
        if (cbxRegistro.getSelectedItem().equals("Salida")) {
            RegistroSalida();
            limpiarCampos();
            ActualizarDatos();

        }
    }//GEN-LAST:event_btnRegistroActionPerformed

    private void btnNuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoActionPerformed
        limpiarCampos();
        cbxRegistro.setSelectedItem("<<Seleccione Uno>>");
    }//GEN-LAST:event_btnNuevoActionPerformed

    private void cbxRegistroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxRegistroActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbxRegistroActionPerformed

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
            java.util.logging.Logger.getLogger(RegistrarAsistencia.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(RegistrarAsistencia.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(RegistrarAsistencia.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(RegistrarAsistencia.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                RegistrarAsistencia dialog = new RegistrarAsistencia(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnNuevo;
    private javax.swing.JButton btnRegistro;
    private javax.swing.JComboBox<String> cbxRegistro;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel labelFechaHora;
    private javax.swing.JPasswordField passContraseña;
    private javax.swing.JTable tabla_asistencia;
    private javax.swing.JTextField txtUsuario;
    // End of variables declaration//GEN-END:variables

}
