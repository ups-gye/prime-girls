/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package basenaval.client;

import basenaval.common.Protocolo;
import java.awt.*;
import java.awt.event.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import javax.swing.*;
import javax.swing.border.*;

public class LoginView extends javax.swing.JFrame {

    // --- 1. COLORES Y CONSTANTES VISUALES ---
    private final Color COLOR_FONDO_TOP = new Color(10, 30, 70);
    private final Color COLOR_FONDO_BOT = new Color(40, 80, 140);
    private final Color COLOR_TEXTO_AZUL = new Color(20, 50, 100);
    private final Color COLOR_ACCENTO = new Color(0, 120, 215);

    // --- 2. CONSTRUCTOR ---
    public LoginView() {
        initComponentsManual();
    }

    // --- 3. INICIALIZACIÓN DE COMPONENTES ---
    private void initComponentsManual() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("Battleship Command - Acceso");
        setSize(450, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        // A. Panel de Fondo (Degradado)
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth(), h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, COLOR_FONDO_TOP, 0, h, COLOR_FONDO_BOT);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        backgroundPanel.setLayout(new GridBagLayout());
        setContentPane(backgroundPanel);

        // B. Tarjeta Central (Panel blanco semitransparente)
        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBackground(new Color(255, 255, 255, 235));
        cardPanel.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 0, 0, 50), 1),
                new EmptyBorder(40, 40, 40, 40)
        ));
        cardPanel.setPreferredSize(new Dimension(380, 500));

        // C. Etiquetas de Título
        JLabel lblIcono = new JLabel("⚓");
        lblIcono.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        lblIcono.setForeground(COLOR_TEXTO_AZUL);
        lblIcono.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitulo = new JLabel("BATTLESHIP");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitulo.setForeground(COLOR_TEXTO_AZUL);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Iniciar Sesión");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblSub.setForeground(Color.GRAY);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        // D. Inicialización de Variables (LAS DEL FINAL)
        // IMPORTANTE: Aquí inicializamos las variables que NetBeans declara abajo
        
        // Campo Usuario
        JLabel lblUser = crearLabel("Usuario / Capitán");
        txtUsuario = new javax.swing.JTextField(); 
        estilarCampo(txtUsuario);

        // Campo Password
        JLabel lblPass = crearLabel("Contraseña");
        txtPassword = new javax.swing.JPasswordField(); 
        estilarCampo(txtPassword);

        // Botón Ingresar
        btnIngresar = new javax.swing.JButton("ENTRAR AL COMBATE");
        estilarBotonPrincipal(btnIngresar);
        
        // Acción del botón Ingresar
        btnIngresar.addActionListener(e -> login());

        // Botón Registro (Texto tipo enlace)
        JButton btnReg = new JButton("<html><u>¿Nuevo recluta? Regístrate aquí</u></html>");
        estilarBotonEnlace(btnReg);
        btnReg.addActionListener(e -> {
            this.dispose(); // Cerrar login
            new RegisterView().setVisible(true); // Abrir registro
        });

        // E. Agregar todo a la tarjeta (Layout)
        cardPanel.add(lblIcono);
        cardPanel.add(Box.createVerticalStrut(10));
        cardPanel.add(lblTitulo);
        cardPanel.add(lblSub);
        cardPanel.add(Box.createVerticalStrut(40));
        
        cardPanel.add(lblUser);
        cardPanel.add(Box.createVerticalStrut(5));
        cardPanel.add(txtUsuario);
        cardPanel.add(Box.createVerticalStrut(20));
        
        cardPanel.add(lblPass);
        cardPanel.add(Box.createVerticalStrut(5));
        cardPanel.add(txtPassword);
        cardPanel.add(Box.createVerticalStrut(40));
        
        cardPanel.add(btnIngresar);
        cardPanel.add(Box.createVerticalStrut(15));
        cardPanel.add(btnReg);

        backgroundPanel.add(cardPanel);
    }

    // --- 4. MÉTODOS DE ESTILO (DISEÑO) ---
    private JLabel crearLabel(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        l.setForeground(COLOR_TEXTO_AZUL);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private void estilarCampo(JTextField txt) {
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txt.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        txt.setBackground(Color.WHITE);
        // Borde inferior solamente
        Border off = new MatteBorder(0, 0, 2, 0, Color.LIGHT_GRAY);
        Border on = new MatteBorder(0, 0, 2, 0, COLOR_ACCENTO);
        txt.setBorder(new CompoundBorder(off, new EmptyBorder(5,5,5,5)));
        
        // Efecto al hacer clic
        txt.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { txt.setBorder(new CompoundBorder(on, new EmptyBorder(5,5,5,5))); }
            public void focusLost(FocusEvent e) { txt.setBorder(new CompoundBorder(off, new EmptyBorder(5,5,5,5))); }
        });
    }

    private void estilarBotonPrincipal(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setBackground(COLOR_ACCENTO);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void estilarBotonEnlace(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setForeground(COLOR_ACCENTO);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // --- 5. LÓGICA DE CONEXIÓN (LOGIN) ---
    private void login() {
        String u = txtUsuario.getText().trim();
        String p = new String(txtPassword.getPassword()).trim();
        
        if (u.isEmpty() || p.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, ingresa tus datos.");
            return;
        }

        // Efecto visual "Cargando..."
        btnIngresar.setEnabled(false);
        btnIngresar.setText("Conectando...");

        // Hilo secundario para no congelar la ventana
        new Thread(() -> {
            try {
                Socket s = new Socket("localhost", 9090);
                DataOutputStream out = new DataOutputStream(s.getOutputStream());
                DataInputStream in = new DataInputStream(s.getInputStream());
                
                // Enviar LOGIN:usuario:password
                out.writeUTF(Protocolo.LOGIN + ":" + u + ":" + p);
                
                if (in.readUTF().equals(Protocolo.LOGIN_OK)) {
                    // Si es correcto, abrimos el Lobby
                    SwingUtilities.invokeLater(() -> {
                        this.dispose();
                        // PASAMOS USUARIO Y CONTRASEÑA AL LOBBY
                        new LobbyView(u, p).setVisible(true);
                    });
                } else {
                    // Si falla
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "Datos incorrectos.");
                        btnIngresar.setEnabled(true);
                        btnIngresar.setText("ENTRAR AL COMBATE");
                    });
                    s.close();
                }
            } catch (Exception e) {
                // Si el servidor está apagado
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "No hay conexión con el Servidor.");
                    btnIngresar.setEnabled(true);
                    btnIngresar.setText("ENTRAR AL COMBATE");
                });
            }
        }).start();
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
       try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {}

        java.awt.EventQueue.invokeLater(() -> new LoginView().setVisible(true));
    }

    // Variables declaration - do not modify                     
    private javax.swing.JButton btnIngresar;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtUsuario;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
