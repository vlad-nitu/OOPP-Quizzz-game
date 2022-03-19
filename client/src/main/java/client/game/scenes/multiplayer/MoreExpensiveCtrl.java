package client.game.scenes.multiplayer;

import client.game.scenes.MainCtrl;
import client.utils.ServerUtils;
import commons.question.Answer;
import commons.question.QuestionMoreExpensive;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

import javax.inject.Inject;
import java.io.FileNotFoundException;
import java.util.Timer;
import java.util.TimerTask;

public class MoreExpensiveCtrl {

    @FXML
    private Text questionTitle, timer, score, points, answer, correct_guess, questionCount;

    @FXML
    private AnchorPane timerImage, emoji;

    @FXML
    private Button option1Button, option2Button, option3Button;


    @FXML
    private ImageView image1, image2, image3;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Pane confirmationExit;

    private final MainCtrl mainCtrl;
    private final GameCtrl gameCtrl;
    private final ServerUtils server;

    private QuestionMoreExpensive question;

    @Inject
    public MoreExpensiveCtrl(MainCtrl mainCtrl, GameCtrl gameCtrl, ServerUtils server) {
        this.mainCtrl = mainCtrl;
        this.gameCtrl = gameCtrl;
        this.server = server;
    }

    public void init(QuestionMoreExpensive question){
        this.question = question;
        disablePopUp(null);
        questionTitle.setText(question.getTitle());
        questionCount.setText("Question " + question.getNumber() + "/20");
        option1Button.setText(question.getActivities()[0].getTitle());
        option2Button.setText(question.getActivities()[1].getTitle());
        option3Button.setText(question.getActivities()[2].getTitle());
        progressBar.setProgress(question.getNumber() / 20.0d + 0.05);
        try {
            Image loadimage1 = new Image(server.getImage(question.getActivities()[0]));
            Image loadimage2 = new Image(server.getImage(question.getActivities()[1]));
            Image loadimage3 = new Image(server.getImage(question.getActivities()[2]));
            image1.setImage(loadimage1);
            image2.setImage(loadimage2);
            image3.setImage(loadimage3);
        } catch (FileNotFoundException e) {
            System.out.println("Couldn't find image");
        }
        Timer scheduler = new Timer();
        scheduler.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                int timeLeft = server.getTimeLeft(gameCtrl.getPlayer());
                Platform.runLater(() -> {
                    timer.setText(String.valueOf(timeLeft));
                });
            }
        }, 0, 100);
    }

    public void option3Selected(ActionEvent actionEvent) {
        gameCtrl.submitAnswer(new Answer((long) 3));
    }

    public void option2Selected(ActionEvent actionEvent) {
        gameCtrl.submitAnswer(new Answer((long) 2));
    }

    public void option1Selected(ActionEvent actionEvent) {
        gameCtrl.submitAnswer(new Answer((long) 1));
    }

    public void disablePopUp(ActionEvent actionEvent) {
        confirmationExit.setVisible(false);
        confirmationExit.setDisable(true);
    }

    public void leaveGame(ActionEvent actionEvent) {
        gameCtrl.disconnect();
        mainCtrl.showSplash();
    }

    public void enablePopUp(ActionEvent actionEvent) {
        confirmationExit.setVisible(true);
        confirmationExit.setDisable(false);
        confirmationExit.setStyle("-fx-background-color: #91e4fb; ");
    }
}
