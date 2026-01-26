/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package basenaval.client;

import basenaval.common.Protocolo;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.net.URL;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class RegisterView extends javax.swing.JFrame {

    private JTextField txtNombre, txtApellido, txtUsuario;
    private JPasswordField txtPassword;
    private String avatarSeleccionado = "boy1.png"; // Default
    private JLabel lblAvatarPreview;
    
    // Lista de avatares
    private final String[] avatares = {
        "boy1.png", "boy2.png", "boy3.png", 
        "gir11.png", "gir12.png", "gir13.png"
    };

    public RegisterView() {
        initComponentsManual();
    }

    private void initComponentsManual() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle("Registro de Capitán");
        setSize(450, 680);
        setLocationRelativeTo(null);
        setResizable(false);

        // BORRA LAS LÍNEAS VIEJAS DE mainPanel Y PEGA ESTO AQUÍ:
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                int width = getWidth();
                int height = getHeight();
                Color color1 = new Color(173, 216, 230); // Celeste
                Color color2 = new Color(70, 130, 180);  // Azul suave
                GradientPaint gp = new GradientPaint(0, 0, color1, 0, height, color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, width, height);
            }
        };
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(30, 40, 30, 40));
        

        // Título
        JLabel title = new JLabel("REGISTRO DE CAPITÁN");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(25, 25, 112));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(title);
        mainPanel.add(Box.createVerticalStrut(20));

        // Campos
        mainPanel.add(crearLabel("Nombre:"));
        txtNombre = crearField();
        mainPanel.add(txtNombre);
        
        mainPanel.add(crearLabel("Apellido:"));
        txtApellido = crearField();
        mainPanel.add(txtApellido);
        
        mainPanel.add(crearLabel("Usuario (Login):"));
        txtUsuario = crearField();
        mainPanel.add(txtUsuario);
        
        mainPanel.add(crearLabel("Contraseña:"));
        txtPassword = new JPasswordField();
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        mainPanel.add(txtPassword);

        mainPanel.add(Box.createVerticalStrut(20));

        // Selección de Avatar
        mainPanel.add(crearLabel("Selecciona tu Avatar:"));
        
        JPanel panelAvatares = new JPanel(new FlowLayout());
        panelAvatares.setOpaque(false);
        
        for (String avt : avatares) {
            JButton btn = new JButton();
            btn.setPreferredSize(new Dimension(50, 50));
            btn.setIcon(obtenerIcono(avt, 40));
            btn.addActionListener(e -> {
                avatarSeleccionado = avt;
                lblAvatarPreview.setIcon(obtenerIcono(avt, 80)); 
            });
            panelAvatares.add(btn);
        }
        mainPanel.add(panelAvatares);
        
        // Preview grande
        lblAvatarPreview = new JLabel();
        lblAvatarPreview.setIcon(obtenerIcono(avatarSeleccionado, 80));
        lblAvatarPreview.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lblAvatarPreview);

        mainPanel.add(Box.createVerticalStrut(30));

        // Botón Registrar
        JButton btnRegistrar = new JButton("GUARDAR Y SALIR");
        btnRegistrar.setBackground(new Color(34, 139, 34));
        btnRegistrar.setForeground(Color.WHITE);
        btnRegistrar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnRegistrar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRegistrar.addActionListener(e -> accionRegistrar());
        
        mainPanel.add(btnRegistrar);

        add(mainPanel);
    }
    
    private void accionRegistrar() {
        String n = txtNombre.getText();
        String a = txtApellido.getText();
        String u = txtUsuario.getText();
        String p = new String(txtPassword.getPassword());
        
        if(u.isEmpty() || p.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Usuario y Contraseña requeridos.");
            return;
        }
        
        try {
            Socket s = new Socket("localhost", 9090);
            DataOutputStream out = new DataOutputStream(s.getOutputStream());
            DataInputStream in = new DataInputStream(s.getInputStream());
            
            // ENVÍO: REGISTER:user:pass:nombre:apellido:avatar
            out.writeUTF(Protocolo.REGISTER + ":" + u + ":" + p + ":" + n + ":" + a + ":" + avatarSeleccionado);
            
            if (in.readUTF().equals(Protocolo.REGISTER_OK)) {
                JOptionPane.showMessageDialog(this, "¡Registrado! Ahora inicia sesión.");
                this.dispose();
                // AQUÍ ESTABA EL ERROR: Ahora funcionará si LoginView está arreglado
                new LoginView().setVisible(true); 
            } else {
                JOptionPane.showMessageDialog(this, "Error: El usuario ya existe.");
            }
            s.close();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error de conexión: " + e.getMessage());
        }
    }
    private JLabel crearLabel(String t) {
        JLabel l = new JLabel(t);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }
    private JTextField crearField() {
        JTextField t = new JTextField();
        t.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        return t;
    }
    private ImageIcon obtenerIcono(String n, int s) {
        URL url = getClass().getResource(n);
        if (url != null) return new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(s, s, Image.SCALE_SMOOTH));
        return null;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

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
            java.util.logging.Logger.getLogger(RegisterView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(RegisterView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(RegisterView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(RegisterView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new RegisterView().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
