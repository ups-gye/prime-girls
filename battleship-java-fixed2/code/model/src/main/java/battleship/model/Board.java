package battleship.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Tablero 10x10. Gestiona la ubicación de barcos y procesa disparos.
 */
public class Board implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int SIZE = 10;

    public enum Cell {
        EMPTY, SHIP, HIT, MISS
    }

    private Cell[][] grid;
    private List<Ship> ships;
    private int shipCellsTotal;
    private int shipCellsHit;

    public Board() {
        grid = new Cell[SIZE][SIZE];
        ships = new ArrayList<>();
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                grid[r][c] = Cell.EMPTY;
        shipCellsTotal = 0;
        shipCellsHit = 0;
    }

    /** Coloca un barco. Retorna false si hay colisión o está fuera de límites. */
    public boolean placeShip(Ship ship) {
        for (int[] cell : ship.getCells()) {
            int r = cell[0], c = cell[1];
            if (r < 0 || r >= SIZE || c < 0 || c >= SIZE) return false;
            if (grid[r][c] != Cell.EMPTY) return false;
        }
        for (int[] cell : ship.getCells()) {
            grid[cell[0]][cell[1]] = Cell.SHIP;
            shipCellsTotal++;
        }
        ships.add(ship);
        return true;
    }

    public enum ShotResult { MISS, HIT, SUNK, ALREADY_SHOT, INVALID }

    /** Procesa un disparo. Retorna el resultado. */
    public ShotResult shoot(int row, int col) {
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE)
            return ShotResult.INVALID;
        Cell current = grid[row][col];
        if (current == Cell.HIT || current == Cell.MISS)
            return ShotResult.ALREADY_SHOT;

        if (current == Cell.SHIP) {
            grid[row][col] = Cell.HIT;
            shipCellsHit++;
            // Buscar el barco y registrar el impacto
            for (Ship ship : ships) {
                if (ship.occupies(row, col)) {
                    boolean sunk = ship.hit();
                    return sunk ? ShotResult.SUNK : ShotResult.HIT;
                }
            }
            return ShotResult.HIT;
        } else {
            grid[row][col] = Cell.MISS;
            return ShotResult.MISS;
        }
    }

    /** ¿Todos los barcos fueron hundidos? */
    public boolean allSunk() {
        return shipCellsTotal > 0 && shipCellsHit >= shipCellsTotal;
    }

    public Cell getCell(int row, int col) { return grid[row][col]; }
    public List<Ship> getShips() { return ships; }
    public int getShipCellsTotal() { return shipCellsTotal; }
    public int getShipCellsHit() { return shipCellsHit; }
    public int getSunkCount() {
        int n = 0;
        for (Ship s : ships) if (s.isSunk()) n++;
        return n;
    }
    public int getTotalShots() {
        int n = 0;
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                if (grid[r][c] == Cell.HIT || grid[r][c] == Cell.MISS) n++;
        return n;
    }
    public int getHits() { return shipCellsHit; }

    /**
     * Serializa el tablero para enviar por red (solo barcos, sin revelar posición al enemigo).
     * Formato: "SHIP:r,c;r,c|SHIP:r,c"
     */
    public String serializeShips() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ships.size(); i++) {
            if (i > 0) sb.append("|");
            sb.append(ships.get(i).serialize());
        }
        return sb.toString();
    }

    /** Reconstruye el tablero desde la serialización */
    public static Board fromSerializedShips(String data) {
        Board board = new Board();
        if (data == null || data.isEmpty()) return board;
        for (String shipStr : data.split("\\|")) {
            Ship ship = Ship.deserialize(shipStr);
            board.placeShip(ship);
        }
        return board;
    }
}
