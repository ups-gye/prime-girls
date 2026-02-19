package basenaval.client;

import basenaval.common.Protocolo;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import javax.swing.*;
import javax.swing.border.*;

public class RegisterView extends JFrame {

    // ── Paleta (igual que LoginView para consistencia) ─────
    private static final Color C_BG_DARK  = new Color(8,  15, 30);
    private static final Color C_BG_MID   = new Color(12, 28, 55);
    private static final Color C_CARD     = new Color(16, 36, 70, 240);
    private static final Color C_ACCENT   = new Color(0, 200, 255);
    private static final Color C_ACCENT2  = new Color(0, 120, 200);
    private static final Color C_TEXT     = new Color(220, 235, 255);
    private static final Color C_TEXT_DIM = new Color(130, 160, 200);
    private static final Color C_BORDER   = new Color(0,  180, 230, 80);
    private static final Color C_FIELD_BG = new Color(8,  20,  45);

    private JTextField     txtNombre, txtApellido, txtUsuario;
    private JPasswordField txtPassword;
    private String         avatarSeleccionado = "boy1.png";
    private JLabel         lblAvatarPreview;

    private final String[] avatares = {
        "boy 1.png", "boy 2.png", "boy3.png",
        "gir11.png", "gir12.png", "gir13.png"
    };
    private final String[] avatarLabels = { "B1", "B2", "B3", "G1", "G2", "G3" };

    public RegisterView() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle("BATTLESHIP — Registro");
        setSize(480, 820);
        setLocationRelativeTo(null);
        setResizable(false);
        buildUI();
    }

    private void buildUI() {

        // ── Fondo ──────────────────────────────────────────
        JPanel bg = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setPaint(new GradientPaint(0, 0, C_BG_DARK, 0, h, C_BG_MID));
                g2.fillRect(0, 0, w, h);
                g2.setColor(new Color(0, 150, 200, 15));
                g2.setStroke(new BasicStroke(1));
                for (int x = 0; x < w; x += 40) g2.drawLine(x, 0, x, h);
                for (int y = 0; y < h; y += 40) g2.drawLine(0, y, w, y);
                g2.dispose();
            }
        };
        bg.setLayout(new GridBagLayout());
        setContentPane(bg);

        // ── Card ───────────────────────────────────────────
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setColor(C_CARD);
                g2.fillRoundRect(0, 0, w, h, 20, 20);
                g2.setColor(C_BORDER);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, w-1, h-1, 20, 20);
                g2.setColor(C_ACCENT);
                g2.setStroke(new BasicStroke(3f));
                g2.drawLine(60, 0, w-60, 0);
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(35, 45, 35, 45));
        card.setPreferredSize(new Dimension(420, 740));

        // ── Encabezado ─────────────────────────────────────
        JLabel lblIcon  = mkCenterLabel("⚓", 44, C_ACCENT, new Font("Segoe UI Emoji", Font.PLAIN, 44));
        JLabel lblTitle = mkCenterLabel("REGISTRO DE CAPITÁN", 14, C_TEXT, new Font("Courier New", Font.BOLD, 22));
        JLabel lblSub   = mkCenterLabel("Crea tu cuenta para unirte a la batalla", 0, C_TEXT_DIM, new Font("Segoe UI", Font.PLAIN, 12));

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(0, 180, 230, 60));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        // ── Campos ─────────────────────────────────────────
        txtNombre   = new JTextField(); styleField(txtNombre);
        txtApellido = new JTextField(); styleField(txtApellido);
        txtUsuario  = new JTextField(); styleField(txtUsuario);
        txtPassword = new JPasswordField(); styleField(txtPassword);

        // ── Selección Avatar ────────────────────────────────
        JLabel lblAvTitle = mkFieldLabel("ELIGE TU AVATAR");

        // Preview del avatar
        lblAvatarPreview = new JLabel(avatarLabels[0]) {{
            setFont(new Font("Segoe UI Emoji", Font.PLAIN, 52));
            setAlignmentX(CENTER_ALIGNMENT);
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_ACCENT, 2),
                new EmptyBorder(8, 12, 8, 12)
            ));
            setBackground(C_FIELD_BG);
            setOpaque(true);
        }};

        JPanel panelAvatares = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        panelAvatares.setOpaque(false);
        panelAvatares.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        for (int i = 0; i < avatares.length; i++) {
            final String av  = avatares[i];
            final String emoji = avatarLabels[i];
            JButton btn = new JButton(emoji) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    boolean sel = av.equals(avatarSeleccionado);
                    g2.setColor(sel ? new Color(0, 160, 220, 120) : C_FIELD_BG);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.setColor(sel ? C_ACCENT : new Color(0, 100, 160, 80));
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
            btn.setPreferredSize(new Dimension(52, 52));
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> {
                avatarSeleccionado = av;
                lblAvatarPreview.setText(emoji);
                panelAvatares.repaint();
            });
            panelAvatares.add(btn);
        }

        // ── Botón Registrar ─────────────────────────────────
        JButton btnRegistrar = mkButton("✔  ALISTARME");
        btnRegistrar.addActionListener(e -> registrar(btnRegistrar));

        JButton btnVolver = new JButton("<html><u>← Volver al login</u></html>") {{
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setForeground(C_TEXT_DIM);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setAlignmentX(CENTER_ALIGNMENT);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { setForeground(C_ACCENT); }
                public void mouseExited (MouseEvent e) { setForeground(C_TEXT_DIM); }
            });
        }};
        btnVolver.addActionListener(e -> { dispose(); new LoginView().setVisible(true); });

        // ── Ensamblar ───────────────────────────────────────
        card.add(lblIcon);
        card.add(Box.createVerticalStrut(4));
        card.add(lblTitle);
        card.add(lblSub);
        card.add(Box.createVerticalStrut(12));
        card.add(sep);
        card.add(Box.createVerticalStrut(18));

        card.add(mkFieldLabel("NOMBRE"));
        card.add(Box.createVerticalStrut(5));
        card.add(txtNombre);
        card.add(Box.createVerticalStrut(14));

        card.add(mkFieldLabel("APELLIDO"));
        card.add(Box.createVerticalStrut(5));
        card.add(txtApellido);
        card.add(Box.createVerticalStrut(14));

        card.add(mkFieldLabel("USUARIO"));
        card.add(Box.createVerticalStrut(5));
        card.add(txtUsuario);
        card.add(Box.createVerticalStrut(14));

        card.add(mkFieldLabel("CONTRASEÑA"));
        card.add(Box.createVerticalStrut(5));
        card.add(txtPassword);
        card.add(Box.createVerticalStrut(18));

        card.add(lblAvTitle);
        card.add(Box.createVerticalStrut(8));
        card.add(lblAvatarPreview);
        card.add(Box.createVerticalStrut(8));
        card.add(panelAvatares);
        card.add(Box.createVerticalStrut(22));

        card.add(btnRegistrar);
        card.add(Box.createVerticalStrut(12));
        card.add(btnVolver);

        bg.add(card);
    }

    // ── Helpers ──────────────────────────────────────────────
    private JLabel mkCenterLabel(String text, int vgap, Color color, Font font) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        l.setAlignmentX(CENTER_ALIGNMENT);
        if (vgap > 0) l.setBorder(new EmptyBorder(0, 0, vgap, 0));
        return l;
    }

    private JLabel mkFieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Courier New", Font.BOLD, 11));
        l.setForeground(C_ACCENT);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private void styleField(JTextField f) {
        f.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        f.setForeground(C_TEXT);
        f.setBackground(C_FIELD_BG);
        f.setCaretColor(C_ACCENT);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        f.setAlignmentX(LEFT_ALIGNMENT);
        Color bOff = new Color(0, 120, 180, 80);
        Color bOn  = C_ACCENT;
        f.setBorder(new CompoundBorder(new MatteBorder(0,0,2,0, bOff), new EmptyBorder(8,10,8,10)));
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                f.setBorder(new CompoundBorder(new MatteBorder(0,0,2,0,bOn), new EmptyBorder(8,10,8,10)));
            }
            public void focusLost(FocusEvent e) {
                f.setBorder(new CompoundBorder(new MatteBorder(0,0,2,0,bOff), new EmptyBorder(8,10,8,10)));
            }
        });
    }

    private JButton mkButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c1 = getModel().isPressed() ? C_ACCENT2 : new Color(0, 160, 220);
                Color c2 = getModel().isPressed() ? new Color(0, 80, 150) : C_ACCENT2;
                g2.setPaint(new GradientPaint(0, 0, c1, 0, getHeight(), c2));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(C_ACCENT);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Courier New", Font.BOLD, 15));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        btn.setAlignmentX(CENTER_ALIGNMENT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── Lógica ───────────────────────────────────────────────
    private void registrar(JButton btn) {
        String nombre   = txtNombre.getText().trim();
        String apellido = txtApellido.getText().trim();
        String usuario  = txtUsuario.getText().trim();
        String pass     = new String(txtPassword.getPassword()).trim();

        if (nombre.isEmpty() || apellido.isEmpty() || usuario.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Campos vacíos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        btn.setEnabled(false);
        btn.setText("  Registrando...");

        new Thread(() -> {
            try {
                Socket s   = new Socket("localhost", 9090);
                DataOutputStream out = new DataOutputStream(s.getOutputStream());
                DataInputStream  in  = new DataInputStream(s.getInputStream());

                out.writeUTF(Protocolo.REGISTER + ":" + usuario + ":" + pass
                             + ":" + nombre + ":" + apellido + ":" + avatarSeleccionado);

                String resp = in.readUTF();
                s.close();

                SwingUtilities.invokeLater(() -> {
                    if (resp.equals(Protocolo.REGISTER_OK)) {
                        JOptionPane.showMessageDialog(this,
                            "¡Bienvenido, Capitán " + nombre + "!\nYa puedes iniciar sesión.",
                            "Registro exitoso", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                        new LoginView().setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "El usuario ya existe o hubo un error.", "Registro fallido",
                            JOptionPane.ERROR_MESSAGE);
                        btn.setEnabled(true);
                        btn.setText("✔  ALISTARME");
                    }
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                        "No se pudo conectar al servidor.", "Sin conexión",
                        JOptionPane.ERROR_MESSAGE);
                    btn.setEnabled(true);
                    btn.setText("✔  ALISTARME");
                });
            }
        }).start();
    }
}