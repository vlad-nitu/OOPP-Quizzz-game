package server.api;

import commons.Activity;
import commons.GameInstance;
import commons.GameState;
import commons.player.Player;
import commons.player.ServerAnswer;
import commons.player.SimpleUser;
import commons.question.Answer;
import commons.question.Question;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

public class ServerGameInstance extends GameInstance{

    GameController gameController;
    SimpMessagingTemplate msgs;
    int questionNumber = 1;
    StopWatch stopWatch;
    int questionTime = 8000;
    private List<ServerAnswer> answers;

    public ServerGameInstance(int id, int type, GameController controller, SimpMessagingTemplate msgs) {
        super(id, type);
        this.gameController = controller;
        this.msgs = msgs;
        stopWatch = new StopWatch();
        answers = new ArrayList<>();
    }

    public void startCountdown(){
        setState(GameState.STARTING);
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            int time = 6;
            @Override
            public void run() {
                time--;
                msgs.convertAndSend("/topic/time", time);
                if(time == 0) {
                    timer.cancel();
                    //TODO START GAME
                    startGame(gameController.activityController.getRandom60().getBody());
                }
            }
        }, 0, 1000);
    }

    @Async
    public void startGame(List<Activity> activities){
        gameController.createNewMultiplayerLobby();
        setState(GameState.INQUESTION);
        generateQuestions(activities);
        nextQuestion();
    }

    private void goToQuestion(int questionNumber){
        msgs.convertAndSend("/topic/" + getId() + "/question", getQuestions().get(questionNumber));
    }

    private void nextQuestion(){
        goToQuestion(questionNumber);
        questionNumber++;
        stopWatch.start();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(questionNumber > 20) {
                    /*TODO CREATE POSTGAME STUFF*/
                    return;
                }
                //TODO ADD POST-QUESTION SCREEN
                answers.clear();
                nextQuestion();
            }
        }, 8000);
    }

    public int getTimeLeft(){
        int timeSpent = (int) stopWatch.getLastTaskTimeMillis();
        return questionTime - timeSpent;
    }

    public Question getCurrentQuestion(){
        return getQuestions().get(questionNumber);
    }


    public long getCorrectAnswer(){
        return getQuestions().get(questionNumber).getAnswer();
    }

    @SendTo("/topic/players")
    public List<SimpleUser> updatePlayerList(){
        return getPlayers().stream().map(SimpleUser.class::cast).collect(Collectors.toList());
    }

    public boolean answerQuestion(Player player, Answer answer){
        answers.add(new ServerAnswer(answer.getAnswer(), player));
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ServerGameInstance that = (ServerGameInstance) o;

        return new EqualsBuilder().appendSuper(super.equals(o)).append(questionNumber, that.questionNumber).append(questionTime, that.questionTime).append(gameController, that.gameController).append(msgs, that.msgs).append(stopWatch, that.stopWatch).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).appendSuper(super.hashCode()).append(gameController).append(msgs).append(questionNumber).append(stopWatch).append(questionTime).toHashCode();
    }
}