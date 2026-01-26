/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package basenaval.client;
import basenaval.common.Protocolo;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.net.URL;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class LobbyView extends javax.swing.JFrame {

    String usuario;
    String password;
    
    private JPanel panelContenedorSalas; 
    private String idSalaSeleccionada = null;
    private JPanel panelSeleccionadoVisualmente = null;

    // Tus imágenes de avatares
    private final String[] misAvatares = {
        "boy1.png", "boy2.png", "boy3.png", 
        "gir11.png", "gir12.png", "gir13.png"
    };

    public LobbyView(String nombreJugador, String passReal) {
        this.usuario = nombreJugador;
        this.password = passReal;
        this.setTitle("Batalla Naval - Lobby - " + usuario);
        initComponentsManual();
    }
    
    private void initComponentsManual() {
        this.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        this.setSize(1100, 750);
        this.setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(240, 245, 250)); 
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(10, 25, 60)); 
        header.setBorder(new EmptyBorder(20, 40, 20, 40));
        
        JLabel title = new JLabel("ESTACIÓN DE MANDO: SELECCIONAR SALA");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(Color.CYAN); 
        
        JLabel userLbl = new JLabel("Capitán: " + this.usuario); 
        userLbl.setForeground(Color.ORANGE);
        userLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        userLbl.setIcon(obtenerIcono(this.usuario, 50)); 
        
        header.add(title, BorderLayout.WEST);
        header.add(userLbl, BorderLayout.EAST);
        mainPanel.add(header, BorderLayout.NORTH);

 
        panelContenedorSalas = new JPanel(new GridLayout(0, 2, 20, 20)); 
        panelContenedorSalas.setBackground(new Color(220, 230, 240)); 
        panelContenedorSalas.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        JScrollPane scroll = new JScrollPane(panelContenedorSalas);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        mainPanel.add(scroll, BorderLayout.CENTER);

     
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 25));
        footer.setBackground(new Color(10, 25, 60));
        footer.setPreferredSize(new Dimension(1000, 100)); 
        

        JButton btnRefrescar = crearBotonGrande("REFRESCAR LISTA", new Color(70, 130, 180));
        btnRefrescar.addActionListener(e -> accionRefrescar());
        
        JButton btnCrear = crearBotonGrande("CREAR SALA NUEVA", new Color(255, 69, 0));
        btnCrear.addActionListener(e -> accionCrear());
        
        JButton btnUnirse = crearBotonGrande("UNIRSE A BATALLA", new Color(34, 139, 34));
        btnUnirse.addActionListener(e -> accionUnirse());
        
        footer.add(btnRefrescar);
        footer.add(btnCrear);
        footer.add(btnUnirse);
        mainPanel.add(footer, BorderLayout.SOUTH);

        this.setContentPane(mainPanel);
    }
    
    
    private JButton crearBotonGrande(String texto, Color color) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 18)); 
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(300, 60)); 
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        return btn;
    }
    

    private JPanel crearTarjeta(String id, String creador) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(255, 255, 255));
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(30, 60, 100), 2),
            new EmptyBorder(10, 10, 10, 10)
        ));
        card.setPreferredSize(new Dimension(400, 160));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        JPanel headerCard = new JPanel(new BorderLayout());
        headerCard.setBackground(new Color(30, 60, 100));
        headerCard.setBorder(new EmptyBorder(5, 10, 5, 10));
        
        JLabel lblSala = new JLabel("SALA DE BATALLA #" + id);
        lblSala.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblSala.setForeground(Color.WHITE);
        headerCard.add(lblSala, BorderLayout.WEST);
        
        JLabel lblEstado = new JLabel("● ESPERANDO RIVAL");
        lblEstado.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblEstado.setForeground(Color.GREEN);
        headerCard.add(lblEstado, BorderLayout.EAST);
        
        card.add(headerCard, BorderLayout.NORTH);
        JPanel bodyCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        bodyCard.setOpaque(false);
        JLabel iconLbl = new JLabel();
        iconLbl.setIcon(obtenerIcono(creador, 80));
        iconLbl.setBorder(new LineBorder(Color.GRAY, 1));
        bodyCard.add(iconLbl);
        JPanel infoPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        infoPanel.setOpaque(false);
        
        JLabel lblCapitan = new JLabel(creador.toUpperCase());
        lblCapitan.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblCapitan.setForeground(new Color(0, 0, 50));
        
        JLabel lblSub = new JLabel("Rango: Capitán");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSub.setForeground(Color.GRAY);
        JLabel lblWins = new JLabel("Victorias: ???  |  Derrotas: ???"); 
        lblWins.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblWins.setForeground(new Color(100, 100, 100));
        
        infoPanel.add(lblCapitan);
        infoPanel.add(lblSub);
        infoPanel.add(lblWins);
        
        bodyCard.add(infoPanel);
        card.add(bodyCard, BorderLayout.CENTER);
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(panelSeleccionadoVisualmente != null) {
                    panelSeleccionadoVisualmente.setBackground(Color.WHITE);
                    panelSeleccionadoVisualmente.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(30, 60, 100), 2),
                        new EmptyBorder(10, 10, 10, 10)
                    ));
                }
                idSalaSeleccionada = id;
                panelSeleccionadoVisualmente = card;
                card.setBackground(new Color(220, 255, 220));
                card.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(0, 200, 0), 4), 
                    new EmptyBorder(8, 8, 8, 8)
                ));
            }
        });

        return card;
    }
    private ImageIcon obtenerIcono(String nombre, int size) {
        try {
            if (nombre == null) nombre = "guest";
            int idx = Math.abs(nombre.hashCode()) % misAvatares.length;
            URL url = getClass().getResource(misAvatares[idx]);
            if (url != null) {
                return new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH));
            }
        } catch (Exception e) {}
        return null; 
    }
    private void accionCrear() {
        try {
            Socket s = new Socket("localhost", 9090);
            DataOutputStream out = new DataOutputStream(s.getOutputStream());
            DataInputStream in = new DataInputStream(s.getInputStream());
            
            out.writeUTF(Protocolo.LOGIN + ":" + this.usuario + ":" + this.password);
            String login = in.readUTF();
            
            if(!login.equals(Protocolo.LOGIN_OK)) {
                JOptionPane.showMessageDialog(this, "Error de autenticación.");
                return;
            }

            out.writeUTF(Protocolo.CREATE_ROOM + ":" + this.usuario);
            String resp = in.readUTF();
            
            if (resp.startsWith(Protocolo.OPPONENT_TURN)) {
                 this.dispose();
                 new GameView(s, this.usuario).setVisible(true);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void accionRefrescar() {
        panelContenedorSalas.removeAll();
        try {
            Socket s = new Socket("localhost", 9090);
            DataOutputStream out = new DataOutputStream(s.getOutputStream());
            DataInputStream in = new DataInputStream(s.getInputStream());
            
            out.writeUTF(Protocolo.LOGIN + ":" + this.usuario + ":" + this.password);
            in.readUTF(); 
            
            out.writeUTF(Protocolo.GET_ROOMS);
            String resp = in.readUTF();
            
            if(resp.startsWith(Protocolo.LIST_ROOMS)) {
                String[] partes = resp.split(":");
                if(partes.length > 1) {
                    for(String sala : partes[1].split(";")) {
                        if(!sala.isEmpty()) {
                            String[] d = sala.split("-");
                            panelContenedorSalas.add(crearTarjeta(d[0], d[1]));
                        }
                    }
                }
            }
            s.close();
            panelContenedorSalas.revalidate();
            panelContenedorSalas.repaint();
        } catch (Exception e) {}
    }

    private void accionUnirse() {
        if(idSalaSeleccionada == null) {
            JOptionPane.showMessageDialog(this, "¡Haz clic en una sala para seleccionarla!");
            return;
        }
        try {
            Socket s = new Socket("localhost", 9090);
            DataOutputStream out = new DataOutputStream(s.getOutputStream());
            DataInputStream in = new DataInputStream(s.getInputStream());
            
            out.writeUTF(Protocolo.LOGIN + ":" + this.usuario + ":" + this.password);
            in.readUTF(); 
            
            out.writeUTF(Protocolo.JOIN_ROOM + ":" + idSalaSeleccionada);
            if(in.readUTF().equals(Protocolo.JOIN_OK)) {
                this.dispose();
                new GameView(s, this.usuario).setVisible(true);
            } else {
                 JOptionPane.showMessageDialog(this, "No se pudo unir. Quizás ya está llena.");
            }
        } catch (Exception e) {}
    }
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

   
    public static void main(String args[]) {
try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {}
        java.awt.EventQueue.invokeLater(() -> new LobbyView("Prueba", "1234").setVisible(true));
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
