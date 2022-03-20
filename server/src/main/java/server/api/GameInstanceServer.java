package server.api;

import commons.Activity;
import commons.GameInstance;
import commons.GameState;
import commons.player.ServerAnswer;
import commons.player.SimpleUser;
import commons.question.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class GameInstanceServer extends GameInstance {

    GameController gameController;
    SimpMessagingTemplate msgs;
    int questionNumber = 1;
    int questionTime = 12000;
    private final List<ServerAnswer> answers;
    private long startingTime;
    Logger logger = LoggerFactory.getLogger(GameInstanceServer.class);
    private TimerTask questionTask;
    private final Timer questionTimer;

    public GameInstanceServer(int id, int type, GameController controller, SimpMessagingTemplate msgs) {
        super(id, type);
        this.gameController = controller;
        this.msgs = msgs;
        answers = new ArrayList<>();
        questionTimer = new Timer();
    }

    public void startCountdown() {
        setState(GameState.STARTING);

        TimerTask countdownTask = new TimerTask() {
            int time = 6;

            @Override
            public void run() {
                time--;
                msgs.convertAndSend("/topic/" + getId() + "/time", time);
                if (time == 0) {
                    cancel();
                    startGame(gameController.activityController.getRandom60().getBody());
                }
            }
        };

        new Timer().scheduleAtFixedRate(countdownTask, 0, 1000);
    }

    @Async
    public void startGame(List<Activity> activities) {
        gameController.createNewMultiplayerLobby();
        setState(GameState.INQUESTION);
        generateQuestions(activities);
        nextQuestion();
    }

    private void sendQuestion(int questionNumber) {
        Question currentQuestion = getQuestions().get(questionNumber);
        logger.info("Question " + questionNumber + " sent" + currentQuestion);
        if (currentQuestion instanceof QuestionHowMuch) {
            msgs.convertAndSend("/topic/" + getId() + "/questionhowmuch", getQuestions().get(questionNumber));
        } else if (currentQuestion instanceof QuestionMoreExpensive) {
            msgs.convertAndSend("/topic/" + getId() + "/questionmoreexpensive", getQuestions().get(questionNumber));
        } else if (currentQuestion instanceof QuestionWhichOne) {
            msgs.convertAndSend("/topic/" + getId() + "/questionwhichone", getQuestions().get(questionNumber));
        } else throw new IllegalStateException();
    }

    private void nextQuestion() {
        if(questionTask != null) questionTask.cancel();
        sendQuestion(questionNumber);
        startingTime = System.currentTimeMillis();
        questionNumber++;
        if(questionNumber > 20){
            //TODO ADD POST-GAME SCREEN AND FUNCTIONALITY
        }
        answers.clear();
        questionTask = new TimerTask() {
            @Override
            public void run() {
                //TODO ADD POST-QUESTION SCREEN
                nextQuestion();
            }
        };
        questionTimer.schedule(questionTask, 12500);
    }

    public int getTimeLeft() {
        int timeSpent = (int) (System.currentTimeMillis() - startingTime);
        return questionTime - timeSpent;
    }

    public Question getCurrentQuestion() {
        return getQuestions().get(questionNumber);
    }


    public long getCorrectAnswer() {
        return getQuestions().get(questionNumber).getAnswer();
    }


    public void updatePlayerList() {
        msgs.convertAndSend("/topic/" + getId() + "/players", getPlayers().size());
    }

    public boolean answerQuestion(SimpleUser player, Answer answer) {
        if(answers.stream()
                .map(x -> x.getPlayer().getName())
                .noneMatch(x-> x.equals(player.getName()))){
            answers.add(new ServerAnswer(answer.getAnswer(), player));
            if(answers.size() == getPlayers().size()) {
                //TODO POST QUESTION
                nextQuestion();
            }
            logger.info("Answer received from " + player.getName() + " = " + answer.getAnswer());
            return true;
        }
        return false;
    }

    public boolean disconnectPlayer(SimpleUser player){
        boolean status = getPlayers().remove(player);
        updatePlayerList();
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        GameInstanceServer that = (GameInstanceServer) o;

        return new EqualsBuilder().appendSuper(super.equals(o)).append(questionNumber, that.questionNumber)
                .append(questionTime, that.questionTime).append(gameController, that.gameController)
                .append(msgs, that.msgs).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).appendSuper(super.hashCode())
                .append(gameController).append(msgs).append(questionNumber)
                .append(questionTime).toHashCode();
    }
}
