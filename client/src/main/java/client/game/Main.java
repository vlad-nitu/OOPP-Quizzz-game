/*
 * Copyright 2021 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package client.game;

import client.MyFXML;
import client.MyModule;
import client.game.scenes.LeaderBoardCtrl;
import client.game.scenes.MainCtrl;
import client.game.scenes.pregame.LobbyCtrl;
import client.game.scenes.pregame.MultiPlayerCtrl;
import client.game.scenes.pregame.SinglePlayerCtrl;
import client.game.scenes.pregame.SplashScreenCtrl;
import client.game.scenes.singleplayer.SinglePlayerGameCtrl;
import client.game.scenes.singleplayer.SinglePlayerGameOverCtrl;
import client.utils.ServerUtils;
import com.google.inject.Guice;
import com.google.inject.Injector;
import commons.player.SimpleUser;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

public class Main extends Application {

    private static final Injector INJECTOR = Guice.createInjector(new MyModule());
    private static final MyFXML FXML = new MyFXML(INJECTOR);

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) {
        var home = FXML.load(SplashScreenCtrl.class, "client", "client/game/scenes", "SplashScreen.fxml");
        var single = FXML.load(SinglePlayerCtrl.class, "client", "client/game/scenes", "SinglePlayer.fxml");
        var singleGame = FXML.load(SinglePlayerGameCtrl.class, "client", "client/game/scenes", "SinglePlayerGame.fxml");
        var singleGameOver = FXML.load(SinglePlayerGameOverCtrl.class, "client", "client/game/scenes", "SinglePlayerGameOver.fxml");
        var multi = FXML.load(MultiPlayerCtrl.class, "client", "client/game/scenes", "Multiplayer.fxml");
        var leaderboard = FXML.load(LeaderBoardCtrl.class, "client", "client/game/scenes", "LeaderBoard.fxml");
        var mainCtrl = INJECTOR.getInstance(MainCtrl.class);
        var lobby = FXML.load(LobbyCtrl.class, "client", "client/game/scenes", "Lobby.fxml");

        mainCtrl.initialize(primaryStage, home, single, singleGame, singleGameOver, multi, leaderboard, lobby);
        primaryStage.setOnCloseRequest(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Do you want to close the game?", ButtonType.YES, ButtonType.NO);
            ButtonType result = alert.showAndWait().orElse(ButtonType.NO);
            if (ButtonType.NO.equals(result))
                event.consume();
            else
                Platform.exit();
        });
    }

    /**
     * Method called whenever a client is closed (by pressing the 'x' button of the window).
     * TODO: POP-UP asking for confirmation of closing the client.
     */
    @Override
    public void stop() {
        var mainCtrl = INJECTOR.getInstance(MainCtrl.class);
        ServerUtils serverUtils = new ServerUtils();
        SimpleUser player = mainCtrl.getPlayer();
        if (player != null) {
            serverUtils.disconnect(mainCtrl.getPlayer());
            System.out.println(player.getName() + " disconnected!");
        }
    }
}