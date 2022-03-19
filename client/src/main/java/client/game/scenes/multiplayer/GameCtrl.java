package client.game.scenes.multiplayer;

import client.game.scenes.MainCtrl;
import client.game.scenes.pregame.LobbyCtrl;
import client.utils.ServerUtils;
import commons.player.SimpleUser;
import commons.question.Answer;
import commons.question.QuestionHowMuch;
import commons.question.QuestionMoreExpensive;
import commons.question.QuestionWhichOne;
import javafx.application.Platform;

import javax.inject.Inject;
import java.util.function.Consumer;

public class GameCtrl {

    private SimpleUser player;

    private final ServerUtils server;
    private final MainCtrl mainCtrl;
    private final LobbyCtrl lobbyCtrl;
    private final HowMuchCtrl howMuchCtrl;
    private final MoreExpensiveCtrl moreExpensiveCtrl;
    private final WhichOneCtrl whichOneCtrl;

    @Inject
    public GameCtrl(ServerUtils server, MainCtrl mainCtrl) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.lobbyCtrl = mainCtrl.getLobbyCtrl();
        this.howMuchCtrl = mainCtrl.getHowMuchCtrl();
        this.moreExpensiveCtrl = mainCtrl.getMoreExpensiveCtrl();
        this.whichOneCtrl = mainCtrl.getWhichOneCtrl();
    }

    public void start() {
        server.initWebsocket();
        subscribe("/topic/" + getPlayer().getGameInstanceId() + "/time", Integer.class, time -> {
            Platform.runLater(() -> {
                mainCtrl.getLobbyCtrl().setCountdown(time);
            });
        });

        //TODO FIND WAY TO DEAL WITH SUBCLASSES OF QUESTION
        subscribe("/topic/" + getPlayer().getGameInstanceId() + "/questionhowmuch", QuestionHowMuch.class, question -> {
            Platform.runLater(() -> {
                goToHowMuch(question);
            });
        });
        subscribe("/topic/" + getPlayer().getGameInstanceId() + "/questionmoreexpensive", QuestionMoreExpensive.class, question -> {
            Platform.runLater(() -> {
                goToMoreExpensive(question);
            });
        });
        subscribe("/topic/" + getPlayer().getGameInstanceId() + "/questionwhichone", QuestionWhichOne.class, question -> {
            Platform.runLater(() -> {
                goToWhichOne(question);
            });
        });
    }

    public <T> void subscribe(String destination, Class<T> type, Consumer<T> consumer) {
        server.registerForMessages(destination, type, consumer);
    }

    public void disconnect() {
        server.disconnectWebsocket();
        server.disconnect(player);
    }


    public void submitAnswer(Answer answer) {
        server.submitAnswer(player, answer);
    }

    public SimpleUser getPlayer() {
        return player;
    }

    public void setPlayer(SimpleUser player) {
        this.player = player;
    }

    private void goToHowMuch(QuestionHowMuch question) {
        mainCtrl.showHowMuch(question);
    }

    private void goToMoreExpensive(QuestionMoreExpensive question) {
        mainCtrl.showMoreExpensive(question);
    }

    private void goToWhichOne(QuestionWhichOne question) {
        mainCtrl.showWhichOne(question);
    }
}
