package battleship.client;

import battleship.client.views.*;
import battleship.model.*;

import javax.swing.*;

public class BattleshipClient {

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(BattleshipClient::showConnect);
    }

    private static void showConnect() {
        ConnectView cv = new ConnectView((usuario, client) ->
            SwingUtilities.invokeLater(() -> showLobby(usuario, client)));
        cv.setVisible(true);
    }

    private static void showLobby(Usuario usuario, NetworkClient client) {
        LobbyView lv = new LobbyView(usuario, client, new LobbyView.LobbyCallback() {
            public void onJoinRoom(int roomId, String roomName, String opponentName,
                                   NetworkClient nc, Usuario u) {
                showSetup(roomId, roomName, opponentName, nc, u);
            }
            public void onLogout() {
                SwingUtilities.invokeLater(BattleshipClient::showConnect);
            }
        });
        lv.setVisible(true);
    }

    private static void showSetup(int roomId, String roomName, String opponentName,
                                   NetworkClient client, Usuario usuario) {
        BoardSetupView sv = new BoardSetupView(roomId, roomName, opponentName, client, usuario,
            (rId, rName, oName, board, nc, u, myTurn) ->
                showGame(rId, rName, oName, board, nc, u, myTurn));
        sv.setVisible(true);
    }

    private static void showGame(int roomId, String roomName, String opponentName,
                                  Board board, NetworkClient client, Usuario usuario, boolean myTurn) {
        GameView gv = new GameView(roomId, roomName, opponentName, board, client, usuario, myTurn);
        gv.setVisible(true);
    }
}
