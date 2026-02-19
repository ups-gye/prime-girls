package basenaval.client;

import basenaval.common.Protocolo;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import javax.swing.*;
import javax.swing.border.*;

public class LoginView extends JFrame {

    // ── Paleta ──────────────────────────────────────────────
    private static final Color C_BG_DARK    = new Color(8,  15,  30);
    private static final Color C_BG_MID     = new Color(12, 28,  55);
    private static final Color C_CARD       = new Color(16, 36,  70, 240);
    private static final Color C_ACCENT     = new Color(0,  200, 255);
    private static final Color C_ACCENT2    = new Color(0,  120, 200);
    private static final Color C_TEXT       = new Color(220, 235, 255);
    private static final Color C_TEXT_DIM   = new Color(130, 160, 200);
    private static final Color C_BORDER     = new Color(0,  180, 230, 80);
    private static final Color C_FIELD_BG   = new Color(8,  20,  45);

    private JTextField     txtUsuario;
    private JPasswordField txtPassword;
    private JButton        btnIngresar;

    public LoginView() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("BATTLESHIP — Acceso");
        setSize(460, 620);
        setLocationRelativeTo(null);
        setResizable(false);
        setUndecorated(false);
        buildUI();
    }

    private void buildUI() {

        // ── Fondo con océano animado (capas) ───────────────
        JPanel bg = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                // Gradiente oscuro profundo
                GradientPaint grad = new GradientPaint(0, 0, C_BG_DARK, 0, h, C_BG_MID);
                g2.setPaint(grad);
                g2.fillRect(0, 0, w, h);

                // Líneas de radar / cuadrícula decorativa
                g2.setColor(new Color(0, 150, 200, 18));
                g2.setStroke(new BasicStroke(1f));
                for (int x = 0; x < w; x += 40) g2.drawLine(x, 0, x, h);
                for (int y = 0; y < h; y += 40) g2.drawLine(0, y, w, y);

                // Círculos de sonar
                g2.setColor(new Color(0, 200, 255, 12));
                for (int r = 60; r < 500; r += 80) {
                    g2.drawOval(w/2 - r, h - 60 - r, r*2, r*2);
                }

                // Brillo superior
                RadialGradientPaint glow = new RadialGradientPaint(
                    w / 2f, -40f, 320f,
                    new float[]{0f, 1f},
                    new Color[]{new Color(0, 160, 255, 60), new Color(0, 0, 0, 0)}
                );
                g2.setPaint(glow);
                g2.fillRect(0, 0, w, 260);
                g2.dispose();
            }
        };
        bg.setLayout(new GridBagLayout());
        setContentPane(bg);

        // ── Card central ───────────────────────────────────
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setColor(C_CARD);
                g2.fillRoundRect(0, 0, w, h, 20, 20);
                // Borde con brillo
                g2.setColor(C_BORDER);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, w-1, h-1, 20, 20);
                // Línea accent superior
                g2.setColor(C_ACCENT);
                g2.setStroke(new BasicStroke(3f));
                g2.drawLine(60, 0, w-60, 0);
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(45, 45, 45, 45));
        card.setPreferredSize(new Dimension(390, 520));

        // ── Logo / Ícono ────────────────────────────────────
        JLabel lblIcon = new JLabel("⚓") {{
            setFont(new Font("Segoe UI Emoji", Font.PLAIN, 52));
            setForeground(C_ACCENT);
            setAlignmentX(CENTER_ALIGNMENT);
        }};

        // ── Título ──────────────────────────────────────────
        JLabel lblTitle = new JLabel("BATTLESHIP") {{
            setFont(new Font("Courier New", Font.BOLD, 30));
            setForeground(C_TEXT);
            setAlignmentX(CENTER_ALIGNMENT);
        }};

        JLabel lblSub = new JLabel("COMANDO NAVAL") {{
            setFont(new Font("Courier New", Font.PLAIN, 12));
            setForeground(C_ACCENT);
            setAlignmentX(CENTER_ALIGNMENT);
            setBorder(new EmptyBorder(0, 0, 6, 0));
        }};

        // Separador
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(0, 180, 230, 60));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        // ── Campos ──────────────────────────────────────────
        JLabel lblUser = mkLabel("USUARIO");
        txtUsuario = new JTextField();
        styleField(txtUsuario, "Ingresa tu usuario...");

        JLabel lblPass = mkLabel("CONTRASEÑA");
        txtPassword = new JPasswordField();
        styleField(txtPassword, "••••••••");

        // ── Botón principal ─────────────────────────────────
        btnIngresar = mkButton("⮕  ENTRAR AL COMBATE");

        btnIngresar.addActionListener(e -> login());
        txtPassword.addActionListener(e -> login()); // Enter en password

        // ── Enlace Registro ─────────────────────────────────
        JButton btnReg = new JButton("<html><u>¿Nuevo recluta? Regístrate aquí</u></html>") {{
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
        btnReg.addActionListener(e -> { dispose(); new RegisterView().setVisible(true); });

        // ── Ensamblar ───────────────────────────────────────
        card.add(lblIcon);
        card.add(Box.createVerticalStrut(8));
        card.add(lblTitle);
        card.add(lblSub);
        card.add(Box.createVerticalStrut(12));
        card.add(sep);
        card.add(Box.createVerticalStrut(28));
        card.add(lblUser);
        card.add(Box.createVerticalStrut(6));
        card.add(txtUsuario);
        card.add(Box.createVerticalStrut(20));
        card.add(lblPass);
        card.add(Box.createVerticalStrut(6));
        card.add(txtPassword);
        card.add(Box.createVerticalStrut(35));
        card.add(btnIngresar);
        card.add(Box.createVerticalStrut(16));
        card.add(btnReg);

        bg.add(card);
    }

    // ── Helpers de estilo ────────────────────────────────────
    private JLabel mkLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Courier New", Font.BOLD, 11));
        l.setForeground(C_ACCENT);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private void styleField(JTextField f, String hint) {
        f.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        f.setForeground(C_TEXT);
        f.setBackground(C_FIELD_BG);
        f.setCaretColor(C_ACCENT);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        f.setAlignmentX(LEFT_ALIGNMENT);

        Color bOff = new Color(0, 120, 180, 80);
        Color bOn  = C_ACCENT;
        f.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 2, 0, bOff),
            new EmptyBorder(8, 10, 8, 10)
        ));
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                f.setBorder(new CompoundBorder(new MatteBorder(0, 0, 2, 0, bOn), new EmptyBorder(8, 10, 8, 10)));
            }
            public void focusLost(FocusEvent e) {
                f.setBorder(new CompoundBorder(new MatteBorder(0, 0, 2, 0, bOff), new EmptyBorder(8, 10, 8, 10)));
            }
        });
    }

    private JButton mkButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c1 = getModel().isPressed() ? C_ACCENT2 : new Color(0, 160, 220);
                Color c2 = getModel().isPressed() ? new Color(0,  80, 150) : C_ACCENT2;
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

    // ── Lógica de Login ──────────────────────────────────────
    private void login() {
        String u = txtUsuario.getText().trim();
        String p = new String(txtPassword.getPassword()).trim();

        if (u.isEmpty() || p.isEmpty()) {
            showMsg("Por favor, ingresa tus datos.", "Campos vacíos");
            return;
        }

        btnIngresar.setEnabled(false);
        btnIngresar.setText("  Conectando...");

        new Thread(() -> {
            try {
                Socket s = new Socket("localhost", 9090);
                DataOutputStream out = new DataOutputStream(s.getOutputStream());
                DataInputStream  in  = new DataInputStream(s.getInputStream());

                out.writeUTF(Protocolo.LOGIN + ":" + u + ":" + p);

                if (in.readUTF().equals(Protocolo.LOGIN_OK)) {
                    SwingUtilities.invokeLater(() -> {
                        dispose();
                        new LobbyView(u, p).setVisible(true);
                    });
                } else {
                    SwingUtilities.invokeLater(() -> {
                        showMsg("Usuario o contraseña incorrectos.", "Acceso denegado");
                        resetBtn();
                    });
                    s.close();
                }
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    showMsg("No se pudo conectar al servidor.\nVerifica que esté activo.", "Sin conexión");
                    resetBtn();
                });
            }
        }).start();
    }

    private void resetBtn() {
        btnIngresar.setEnabled(true);
        btnIngresar.setText("⮕  ENTRAR AL COMBATE");
    }

    private void showMsg(String msg, String title) {
        UIManager.put("OptionPane.background", C_BG_DARK);
        UIManager.put("Panel.background", C_BG_DARK);
        UIManager.put("OptionPane.messageForeground", C_TEXT);
        JOptionPane.showMessageDialog(this, msg, title, JOptionPane.WARNING_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginView().setVisible(true));
    }
}