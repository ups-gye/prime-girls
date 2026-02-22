package battleship.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Ship implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Type {
        PORTAAVIONES(5, "Portaaviones"),
        ACORAZADO(4, "Acorazado"),
        CRUCERO(3, "Crucero"),
        DESTRUCTOR(2, "Destructor");

        public final int size;
        public final String label;

        Type(int size, String label) {
            this.size = size;
            this.label = label;
        }
    }

    private Type type;
    private List<int[]> cells;   // cada int[] = {fila, col}
    private int hits;
    private boolean sunk;

    public Ship(Type type) {
        this.type = type;
        this.cells = new ArrayList<>();
        this.hits = 0;
        this.sunk = false;
    }

    public void addCell(int row, int col) {
        cells.add(new int[]{row, col});
    }

    /** Registra un impacto; retorna true si el barco fue hundido */
    public boolean hit() {
        hits++;
        if (hits >= type.size) {
            sunk = true;
        }
        return sunk;
    }

    public boolean occupies(int row, int col) {
        for (int[] c : cells) {
            if (c[0] == row && c[1] == col) return true;
        }
        return false;
    }

    public Type getType() { return type; }
    public List<int[]> getCells() { return cells; }
    public int getHits() { return hits; }
    public boolean isSunk() { return sunk; }
    public int getSize() { return type.size; }
    public String getName() { return type.label; }

    /**
     * Serializa: "PORTAAVIONES:r0,c0;r1,c1;..."
     */
    public String serialize() {
        StringBuilder sb = new StringBuilder(type.name()).append(":");
        for (int i = 0; i < cells.size(); i++) {
            if (i > 0) sb.append(";");
            sb.append(cells.get(i)[0]).append(",").append(cells.get(i)[1]);
        }
        return sb.toString();
    }

    public static Ship deserialize(String s) {
        String[] parts = s.split(":");
        Type type = Type.valueOf(parts[0]);
        Ship ship = new Ship(type);
        if (parts.length > 1 && !parts[1].isEmpty()) {
            for (String cell : parts[1].split(";")) {
                String[] rc = cell.split(",");
                ship.addCell(Integer.parseInt(rc[0]), Integer.parseInt(rc[1]));
            }
        }
        return ship;
    }
}
