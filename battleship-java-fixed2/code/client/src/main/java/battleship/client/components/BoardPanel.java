package battleship.client.components;

import battleship.model.Board;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Componente Swing reutilizable para renderizar un tablero 10x10.
 * Soporta: mostrar barcos, disparos, hover de colocación.
 */
public class BoardPanel extends JPanel {

    public interface CellListener  { void onCell(int row, int col); }
    public interface HoverListener { void onHover(int row, int col); }

    private final int size;
    private final boolean interactive;

    private Board board;
    private boolean[][] hoverCells;
    private boolean hoverValid = true;
    private boolean hideShips = false; // true para tablero enemigo

    private CellListener cellListener;
    private HoverListener hoverListener;

    private static final int CELL = 34;
    private static final int MARGIN = 28;

    // Colores
    private static final Color C_EMPTY   = new Color(30, 70, 140);
    private static final Color C_SHIP    = new Color(60, 130, 220);
    private static final Color C_HIT     = new Color(220, 50, 50);
    private static final Color C_MISS    = new Color(120, 160, 200, 160);
    private static final Color C_HOVER_OK  = new Color(60, 200, 100, 180);
    private static final Color C_HOVER_BAD = new Color(220, 60, 60, 180);
    private static final Color C_GRID    = new Color(50, 100, 180);
    private static final Color C_HEADER  = new Color(150, 200, 255);

    private static final String[] COLS = {"A","B","C","D","E","F","G","H","I","J"};

    public BoardPanel(int size, boolean interactive) {
        this.size = size;
        this.interactive = interactive;
        this.hoverCells = new boolean[size][size];
        int dim = MARGIN + size * CELL + 2;
        setPreferredSize(new Dimension(dim, dim));
        setBackground(new Color(15, 30, 60));

        if (interactive) {
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    int[] rc = toRowCol(e.getX(), e.getY());
                    if (rc != null && cellListener != null) cellListener.onCell(rc[0], rc[1]);
                }
            });
            addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseMoved(MouseEvent e) {
                    int[] rc = toRowCol(e.getX(), e.getY());
                    if (rc != null && hoverListener != null) hoverListener.onHover(rc[0], rc[1]);
                }
            });
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Headers columna (A-J)
        g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        g2.setColor(C_HEADER);
        for (int c = 0; c < size; c++) {
            int x = MARGIN + c * CELL + CELL / 2 - 4;
            g2.drawString(COLS[c], x, 16);
        }
        // Headers fila (1-10)
        for (int r = 0; r < size; r++) {
            String label = String.valueOf(r + 1);
            int y = MARGIN + r * CELL + CELL / 2 + 4;
            g2.drawString(label, r < 9 ? 8 : 2, y);
        }

        // Celdas
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                int x = MARGIN + c * CELL;
                int y = MARGIN + r * CELL;

                Color bg = C_EMPTY;

                if (board != null) {
                    Board.Cell cell = board.getCell(r, c);
                    if (cell == Board.Cell.HIT)  bg = C_HIT;
                    else if (cell == Board.Cell.MISS) bg = C_MISS;
                    else if (cell == Board.Cell.SHIP && !hideShips) bg = C_SHIP;
                }

                // Hover
                if (hoverCells[r][c]) {
                    bg = hoverValid ? C_HOVER_OK : C_HOVER_BAD;
                }

                g2.setColor(bg);
                g2.fillRect(x + 1, y + 1, CELL - 2, CELL - 2);

                // Borde
                g2.setColor(C_GRID);
                g2.drawRect(x, y, CELL, CELL);

                // Símbolos
                if (board != null) {
                    Board.Cell cell = board.getCell(r, c);
                    if (cell == Board.Cell.HIT) {
                        g2.setColor(Color.WHITE);
                        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                        g2.drawString("✕", x + CELL / 2 - 5, y + CELL / 2 + 5);
                    } else if (cell == Board.Cell.MISS) {
                        g2.setColor(new Color(200, 230, 255));
                        g2.fillOval(x + CELL/2 - 3, y + CELL/2 - 3, 6, 6);
                    }
                }
            }
        }
    }

    private int[] toRowCol(int px, int py) {
        int col = (px - MARGIN) / CELL;
        int row = (py - MARGIN) / CELL;
        if (row < 0 || row >= size || col < 0 || col >= size) return null;
        return new int[]{row, col};
    }

    public void setBoard(Board board) {
        this.board = board;
        repaint();
    }

    public void setHoverCells(List<int[]> cells, boolean valid) {
        hoverCells = new boolean[size][size];
        hoverValid = valid;
        for (int[] c : cells) {
            if (c[0] >= 0 && c[0] < size && c[1] >= 0 && c[1] < size)
                hoverCells[c[0]][c[1]] = true;
        }
        repaint();
    }

    public void clearHover() {
        hoverCells = new boolean[size][size];
        repaint();
    }

    public void setHideShips(boolean hide) {
        this.hideShips = hide;
    }

    public void addCellListener(CellListener l)   { this.cellListener = l; }
    public void addHoverListener(HoverListener l) { this.hoverListener = l; }
}
