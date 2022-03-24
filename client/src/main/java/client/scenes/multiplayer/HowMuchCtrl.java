package client.scenes.multiplayer;

import client.scenes.MainCtrl;
import client.utils.ServerUtils;
import com.google.inject.Inject;
import commons.Answer;
import commons.QuestionHowMuch;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Timer;
import java.util.TimerTask;

public class HowMuchCtrl implements QuestionCtrl{

    @FXML
    private Text questionTitle, timer, score, points, answer, option4, correct_guess, questionCount;

    @FXML
    private AnchorPane emoji;

    @FXML
    private ImageView timerImage, heartPic, cryPic, laughPic, angryPic, glassesPic;

    @FXML
    private Button submit_guess, heart, cry, laugh, angry, glasses;

    @FXML
    private TextField player_answer;

    @FXML
    private ImageView image4;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Pane confirmationExit;

    private Image timerImageSource;

    private TimerTask scheduler;

    private ServerUtils server;
    private MainCtrl mainCtrl;
    private GameCtrl gameCtrl;

    private QuestionHowMuch question;

    @Inject
    public HowMuchCtrl(ServerUtils server, MainCtrl mainCtrl, GameCtrl gameCtrl) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.gameCtrl = gameCtrl;
        try {
            timerImageSource = new Image(new FileInputStream("client/src/main/resources/images/timer.png"));
        } catch (FileNotFoundException e) {
            System.out.println("Couldn't find timer image.");
        }
    }

    public void init(QuestionHowMuch question) {
        timerImage.setImage(timerImageSource);
        disablePopUp(null);
        player_answer.clear();
        this.question = question;
        questionTitle.setText(question.getTitle());
        questionCount.setText("Question " + question.getNumber() + "/20");
        option4.setText(question.getActivity().getTitle());
        progressBar.setProgress(question.getNumber() / 20.0d + 0.05);
        try {
            Image image = new Image(server.getImage(question.getActivity()));
            image4.setImage(image);
        } catch (FileNotFoundException e) {
            System.out.println("Couldn't find image");
        }
        scheduler = new TimerTask() {
            @Override
            public void run () {
                int timeLeft = server.getTimeLeft(gameCtrl.getPlayer());
                Platform.runLater(() -> {
                    timer.setText(String.valueOf(Math.round(timeLeft / 1000d)));
                });
            }
        };
        new Timer().scheduleAtFixedRate(scheduler, 0, 100);
    }

    public void disablePopUp(ActionEvent actionEvent) {
        confirmationExit.setVisible(false);
        confirmationExit.setDisable(true);
    }

    public void leaveGame(ActionEvent actionEvent) {
        scheduler.cancel();
        gameCtrl.disconnect();
        mainCtrl.showSplash();
    }

    public void enablePopUp(ActionEvent actionEvent) {
        confirmationExit.setVisible(true);
        confirmationExit.setDisable(false);
        confirmationExit.setStyle("-fx-background-color: #91e4fb; ");
    }

    public void submitAnswer(ActionEvent actionEvent) {
        gameCtrl.submitAnswer(new Answer(Long.valueOf(player_answer.getText())));
        //TODO ERROR HANDLING
    }

    @Override
    public void postQuestion(Answer answer) {
        correct_guess.setText("The correct answer is: " + answer.getAnswer());
        correct_guess.setVisible(true);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                scheduler.cancel();
                resetUI();
            }
        }, 5000);
    }

    @Override
    public void resetUI() {
        correct_guess.setVisible(false);
        player_answer.clear();
//        timer.setText("12000");
    }

    public void heartBold(){
        emojiBold(heart, heartPic);
    }

    public void glassesBold(){
        emojiBold(glasses, glassesPic);
    }

    public void angryBold(){
        emojiBold(angry, angryPic);
    }

    public void cryBold(){
        emojiBold(cry, cryPic);
    }

    public void laughBold(){
        emojiBold(laugh, laughPic);
    }


    public void emojiBold(Button emojiButton, ImageView emojiPic) {
        Thread thread = new Thread(() -> {

            try {

                emojiButton.setStyle("-fx-pref-height: 50; -fx-pref-width: 50; -fx-background-color: transparent; ");
                emojiButton.setLayoutX(emojiButton.getLayoutX() - 10.0);
                emojiButton.setLayoutY(emojiButton.getLayoutY() - 10.0);
                emojiButton.setMouseTransparent(true);
                emojiPic.setFitWidth(50);
                emojiPic.setFitHeight(50);
                Thread.sleep(3000);
                emojiButton.setStyle("-fx-pref-height: 30; -fx-pref-width: 30; -fx-background-color: transparent; ");
                emojiButton.setLayoutX(emojiButton.getLayoutX() + 10.0);
                emojiButton.setLayoutY(emojiButton.getLayoutY() + 10.0);
                emojiButton.setMouseTransparent(false);
                emojiPic.setFitWidth(30);
                emojiPic.setFitHeight(30);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        thread.start();
    }
}
