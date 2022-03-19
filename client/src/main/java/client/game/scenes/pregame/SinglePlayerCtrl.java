package client.game.scenes.pregame;

import client.game.scenes.MainCtrl;
import client.utils.ServerUtils;
import com.google.inject.Inject;
import commons.GameInstance;
import commons.communication.RequestToJoin;
import commons.player.SimpleUser;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;


public class SinglePlayerCtrl {

    private final ServerUtils server;
    private final MainCtrl mainCtrl;
    private String playerName;

    @FXML
    private TextField textField;

    @Inject
    public SinglePlayerCtrl(ServerUtils server, MainCtrl mainCtrl) {
        this.server = server;
        this.mainCtrl = mainCtrl;
    }

    public void back() {
        this.textField.clear();
        mainCtrl.showSplash();
    }

    /**
     * To be added when making the main game scene, in order for the player to play
     */
    public void play() {
        if (!getTextField().equals("")) {
            SimpleUser player = server.addPlayer(new RequestToJoin(getTextField(), GameInstance.SINGLE_PLAYER));
            mainCtrl.setPlayer(player);
            playerName = player.getName();
            this.textField.clear();
            mainCtrl.showSinglePlayerGame();
            // mainCtrl.showLobby(); -> Disabled, since we won't have a lobby for SinglePlayer and it messed the counter of players implementation
            //TODO Make it so that player goes directly into game instead of going to lobby
        }
    }

    public String getTextField() {
        return textField.getText();
    }

    public void setTextField(String string) {
        this.textField.setText(string);
    }

    public String getPlayerName() {
        return playerName;
    }
}