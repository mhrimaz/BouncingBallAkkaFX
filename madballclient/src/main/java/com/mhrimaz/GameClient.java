package com.mhrimaz;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;


public class GameClient extends Application {

    private ActorRef gameActor;
    private ActorSystem system;
    private Pane canvas;
    private boolean leftPresent;
    private boolean rightPresent;
    private boolean upPresent;
    private boolean downPresent;
    private final double screenSize = 300;
    private Map<Integer, Circle> mapBall;
    private Map<Integer, Timeline> mapAnimation;

    @Override
    public void init() throws Exception {
        mapBall = new HashMap<>();
        mapAnimation = new HashMap<>();
        system = ActorSystem.create("madballclient");
        gameActor = system.actorOf(GameActor.props(this), "gameactor");
    }

    @Override
    public void stop() throws Exception {
        system.terminate();
    }

    public void setLeftPresent(boolean exist){
        leftPresent = exist;
    }
    public void setRightPresent(boolean exist){
        rightPresent = exist;
    }
    public void setDownPresent(boolean exist){
        downPresent = exist;
    }
    public void setUpPresent(boolean exist){
        upPresent = exist;
    }
//    private void toggle(int id){
//        Platform.runLater(()->{
//            mapBall.computeIfPresent(id,(integer, circle) -> {
//                Timeline timeline  = mapAnimation.get(id);
//
//                    if (circle.isVisible()) {
//                        if (timeline != null) {
//                            timeline.stop();
//                        }
//                    }
//
//                circle.setVisible(!circle.isVisible());
//                return circle;
//            });
//        });
//    }
    private void stopAnimation(int id){
//        Timeline anim = mapAnimation.get(id);
//        if(anim!=null && anim.getStatus() == Animation.Status.RUNNING){
//            anim.stop();
//        }
        Circle circle = mapBall.get(id);
        circle.setVisible(false);
    }
    public void setLabelText(String text){
        if(text == null||text.isEmpty()){
            return;
        }
        String[] split = text.split(",");
        int x = Integer.parseInt(split[0]);
        int y = Integer.parseInt(split[1]);
        int deg = Integer.parseInt(split[2]);
        int rd = Integer.parseInt(split[3]);
        int id = Integer.parseInt(split[4]);
        int r =Integer.parseInt(split[5]);
        int g =Integer.parseInt(split[6]);
        int b =Integer.parseInt(split[7]);
        Platform.runLater(()->{
            Circle ball;
            if(mapBall.containsKey(id)){
                ball = mapBall.get(id);
                if(ball.isVisible()){
                    System.out.println("Ball is already entered: "+text);
                    return;
                }else{
                    ball.setVisible(true);
                }
            }else{
                ball = new Circle(rd,Color.rgb(r, g, b));
                mapBall.put(id,ball);
                canvas.getChildren().add(ball);
            }
            ball.setCenterX(x);
            ball.setCenterY(y);
            Timeline timeline = mapAnimation.get(id);
            if((timeline != null) && (timeline.getStatus() == Animation.Status.RUNNING)){
                timeline.stop();
            }
            timeline = new Timeline(new KeyFrame(Duration.millis(24),
                    new EventHandler<ActionEvent>() {

                        double dx = Math.cos(Math.toRadians(deg))*3; //Step on x or velocity
                        double dy =  Math.sin(Math.toRadians(deg))*3; //Step on y

                        @Override
                        public void handle(ActionEvent t) {
                            //move the ball
                            moveTheBall();
                            ball.setCenterX(dx + ball.getCenterX());
                            ball.setCenterY(dy + ball.getCenterY());
                        }

                        private void moveTheBall() {
                            if (ball.getCenterX() > canvas.getWidth()
                                    - ball.getRadius() && dx>0) {
                                if(ball.getCenterX() > canvas.getWidth()
                                        +  ball.getRadius()){
                                    System.out.println("RIGHT OUT");
                                    stopAnimation(id);
                                    return;
                                }
                                gameActor.tell(
                                        new GameActor.BallStatus(GameActor.Pos.RIGHT,
                                                -ball.getRadius(),
                                                ball.getCenterY(),Math.toDegrees(Math.atan2(dy,dx))
                                                ,rd,id,r,g,b),
                                        ActorRef.noSender());
                                if(!rightPresent) {
                                    dx *= -1; // Change ball move direction
                                }
                            }
                            if (ball.getCenterX() < ball.getRadius()&&dx<0) {
                                if(ball.getCenterX() < -ball.getRadius()){
                                    System.out.println("LEFT OUT");
                                    stopAnimation(id);
                                    return;
                                }
                                gameActor.tell(
                                        new GameActor.BallStatus(GameActor.Pos.LEFT,
                                                screenSize + ball.getRadius(),
                                                ball.getCenterY(),
                                                Math.toDegrees(Math.atan2(dy,dx)),rd,id,r,g,b),
                                        ActorRef.noSender());
                                if(!leftPresent) {
                                    dx *= -1; // Change ball move direction
                                }
                            }

                            if (ball.getCenterY() > canvas.getHeight()
                                    - ball.getRadius()&&dy>0) {
                                if(ball.getCenterY() > canvas.getHeight()
                                        + ball.getRadius()){
                                    stopAnimation(id);
                                    return;
                                }
                                gameActor.tell(
                                        new GameActor.BallStatus(GameActor.Pos.DOWN,ball.getCenterX(),
                                                -ball.getRadius()
                                                ,Math.toDegrees(Math.atan2(dy,dx)),rd,id,r,g,b),
                                        ActorRef.noSender());
                                if(!downPresent) {
                                    dy *= -1; // Change ball move direction
                                }
                            }
                            if (ball.getCenterY() < ball.getRadius()&&dy<0 ) {
                                if(ball.getCenterY() < -ball.getRadius()){
                                    stopAnimation(id);
                                    return;
                                }
                                gameActor.tell(
                                        new GameActor.BallStatus(GameActor.Pos.UP,ball.getCenterX(),
                                                screenSize + ball.getRadius(),
                                                Math.toDegrees(Math.atan2(dy,dx)),rd,id,r,g,b),
                                        ActorRef.noSender());
                                if(!upPresent) {
                                    dy *= -1; // Change ball move direction
                                }
                            }
                        }
                    }));
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();
            mapAnimation.put(id,timeline);
        });
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        canvas = new Pane();
        Scene scene = new Scene(canvas, screenSize, screenSize, Color.ALICEBLUE);
        primaryStage.setScene(scene);
        primaryStage.setTitle("CLIENT");
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {


        launch(args);
    }
}
