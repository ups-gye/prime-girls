/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package basenaval.client;

import basenaval.common.Protocolo;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import javax.swing.*;
import javax.swing.border.LineBorder;

public class GameView extends javax.swing.JFrame {
    private Socket socket;
    private DataInputStream entrada;
    private DataOutputStream salida;
    private String miUsuario;
    private boolean esMiTurno = false; 
    private JPanel panelPropio;
    private JPanel panelRival;
    private JLabel lblEstado;
    private JButton btnListo;
    private JLabel labelTituloPropio;
    private JLabel labelTituloRival;
    private JButton[][] misBarcos = new JButton[10][10]; 
    private JButton[][] miRadar = new JButton[10][10];   
    private int[][] logicaBarcos = new int[10][10];      
    private boolean faseColocacion = true;
    private final Color COLOR_AGUA = new Color(173, 216, 230); 
    private final Color COLOR_BARCO = new Color(70, 70, 70);   
    private final Color COLOR_HIT = new Color(200, 0, 0);      
    private final Color COLOR_MISS = new Color(255, 255, 255); 
    private final Color COLOR_FONDO = new Color(240, 240, 240); 
    public GameView(Socket socket, String usuario) {
        this.socket = socket;
        this.miUsuario = usuario;
        
        try {
            entrada = new DataInputStream(socket.getInputStream());
            salida = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        initComponentsManual();
        new Thread(new ListenerJuego()).start();
    }
    public GameView() {
        initComponentsManual();
    }
    private void initComponentsManual() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Batalla Naval - Jugador: " + (miUsuario != null ? miUsuario : "Invitado"));
        setSize(1100, 650);
        setLocationRelativeTo(null);
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(COLOR_FONDO);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        lblEstado = new JLabel("FASE 1: COLOCA TUS BARCOS EN TU FLOTA");
        lblEstado.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblEstado.setHorizontalAlignment(SwingConstants.CENTER);
        lblEstado.setForeground(new Color(0, 0, 100));
        mainPanel.add(lblEstado, BorderLayout.NORTH);
        JPanel panelCentral = new JPanel(new GridLayout(1, 2, 40, 0)); // Dividido en 2 columnas
        panelCentral.setOpaque(false);
        JPanel contenedorIzquierdo = new JPanel(new BorderLayout(0, 10));
        contenedorIzquierdo.setOpaque(false);
        labelTituloPropio = new JLabel("MI FLOTA (Tus Barcos)");
        labelTituloPropio.setHorizontalAlignment(SwingConstants.CENTER);
        labelTituloPropio.setFont(new Font("Segoe UI", Font.BOLD, 18));
        
        panelPropio = new JPanel(new GridLayout(10, 10)); // Rejilla 10x10
        panelPropio.setBorder(new LineBorder(Color.BLACK, 2));
        llenarTableroPropio();
        
        contenedorIzquierdo.add(labelTituloPropio, BorderLayout.NORTH);
        contenedorIzquierdo.add(panelPropio, BorderLayout.CENTER);
        JPanel contenedorDerecho = new JPanel(new BorderLayout(0, 10));
        contenedorDerecho.setOpaque(false);
        labelTituloRival = new JLabel("RADAR (Dispara Aquí)");
        labelTituloRival.setHorizontalAlignment(SwingConstants.CENTER);
        labelTituloRival.setFont(new Font("Segoe UI", Font.BOLD, 18));
        
        panelRival = new JPanel(new GridLayout(10, 10)); 
        panelRival.setBorder(new LineBorder(Color.RED, 2));
        llenarRadar(); 
        
        contenedorDerecho.add(labelTituloRival, BorderLayout.NORTH);
        contenedorDerecho.add(panelRival, BorderLayout.CENTER);
        panelCentral.add(contenedorIzquierdo);
        panelCentral.add(contenedorDerecho);
        mainPanel.add(panelCentral, BorderLayout.CENTER);
        btnListo = new JButton("¡ESTOY LISTO!");
        btnListo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnListo.setBackground(new Color(34, 139, 34)); // Verde
        btnListo.setForeground(Color.WHITE);
        btnListo.setPreferredSize(new Dimension(200, 50));
        btnListo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                accionBotonListo();
            }
        });
        
        JPanel panelBoton = new JPanel();
        panelBoton.setOpaque(false);
        panelBoton.add(btnListo);
        mainPanel.add(panelBoton, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }
    private void llenarTableroPropio() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                JButton btn = new JButton();
                btn.setBackground(COLOR_AGUA);
                final int f = i;
                final int c = j;
                btn.addActionListener(e -> {
                    if (faseColocacion) {
                        if (logicaBarcos[f][c] == 0) {
                            logicaBarcos[f][c] = 1; // Poner
                            btn.setBackground(COLOR_BARCO);
                        } else {
                            logicaBarcos[f][c] = 0; // Quitar
                            btn.setBackground(COLOR_AGUA);
                        }
                    }
                });
                misBarcos[i][j] = btn;
                panelPropio.add(btn);
            }
        }
    }

    private void llenarRadar() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                JButton btn = new JButton();
                btn.setBackground(Color.LIGHT_GRAY); 
                final int f = i;
                final int c = j;
                btn.addActionListener(e -> enviarDisparo(f, c));
                
                miRadar[i][j] = btn;
                panelRival.add(btn);
            }
        }
    }
    private void accionBotonListo() {
        if (!faseColocacion) return;
        
        int confirm = JOptionPane.showConfirmDialog(this, "¿Confirmar posición de barcos?", "Listo", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if(salida != null) salida.writeUTF(Protocolo.GAME_READY);
                
                faseColocacion = false;
                btnListo.setEnabled(false);
                btnListo.setText("ESPERANDO AL RIVAL...");
                lblEstado.setText("Esperando a que el otro jugador termine...");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void enviarDisparo(int f, int c) {
        if (faseColocacion) {
            JOptionPane.showMessageDialog(this, "Primero coloca tus barcos y presiona LISTO.");
            return;
        }
        if (!esMiTurno) {
            JOptionPane.showMessageDialog(this, "¡No es tu turno! Espera a que el enemigo dispare.");
            return;
        }
        Color colorActual = miRadar[f][c].getBackground();
        if (colorActual.equals(COLOR_HIT) || colorActual.equals(COLOR_MISS)) {
            return; 
        }

        try {
            salida.writeUTF(Protocolo.SHOOT + ":" + f + "," + c);
            esMiTurno = false; 
            lblEstado.setText("Disparando... Esperando confirmación...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    class ListenerJuego implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                     String mensaje = entrada.readUTF();
                    
                    // A. EL JUEGO EMPIEZA
                    if (mensaje.equals(Protocolo.GAME_START)) {
                        lblEstado.setText("¡COMBATE INICIADO! Dispara en el RADAR (Derecha).");
                        esMiTurno = true; 
                    }
                    else if (mensaje.startsWith(Protocolo.OPPONENT_SHOT)) {
                        String[] coords = mensaje.split(":")[1].split(",");
                        int f = Integer.parseInt(coords[0]);
                        int c = Integer.parseInt(coords[1]);
                        
                        String respuesta = Protocolo.RESULT_MISS;
                        if (logicaBarcos[f][c] == 1) {
                            respuesta = Protocolo.RESULT_HIT;
                            misBarcos[f][c].setBackground(COLOR_HIT); 
                        } else {
                            misBarcos[f][c].setBackground(COLOR_MISS); // Blanco en mi tablero
                        }
                     
                        salida.writeUTF(respuesta + ":" + f + "," + c);
                        

                        esMiTurno = true;
                        lblEstado.setText("¡TU TURNO! El enemigo disparó. Ahora ataca tú.");
                    }
                    

                    else if (mensaje.startsWith(Protocolo.RESULT_HIT)) {
                        String[] coords = mensaje.split(":")[1].split(",");
                        int f = Integer.parseInt(coords[0]);
                        int c = Integer.parseInt(coords[1]);
                        miRadar[f][c].setBackground(COLOR_HIT); 
                        esMiTurno = false;
                        lblEstado.setText("¡IMPACTO! Le diste. Espera al rival...");
                    }
                    else if (mensaje.startsWith(Protocolo.RESULT_MISS)) {
                        String[] coords = mensaje.split(":")[1].split(",");
                        int f = Integer.parseInt(coords[0]);
                        int c = Integer.parseInt(coords[1]);
                        miRadar[f][c].setBackground(COLOR_MISS); // Blanco en radar
                        esMiTurno = false;
                        lblEstado.setText("AGUA (Fallaste). Espera al rival...");
                    }
                }
            } catch (Exception e) {
                System.out.println("Desconectado.");
            }
        }
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
            java.util.logging.Logger.getLogger(GameView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(GameView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(GameView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GameView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new GameView().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
