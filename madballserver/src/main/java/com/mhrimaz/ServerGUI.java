package com.mhrimaz;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Random;

public class ServerGUI extends Application {
    public ActorRef gameActor;
    private ActorSystem system;
    private Label label;
    private int idCounter;

    @Override
    public void init() throws Exception {
        idCounter = 1;
        system = ActorSystem.create("madballserver");
        gameActor = system.actorOf(ServerActor.props(this), "serveractor");
    }

    @Override
    public void stop() throws Exception {
        system.terminate();
    }

    public void setLabelText(String text){
        if(text == null||text.isEmpty()){
            return;
        }
        Platform.runLater(()->{
            label.setText(text);
        });
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        TextField position = new TextField("100,100,20,10");
        Random r = new Random();
        Button button = new Button("Send Bouncing Ball");
        button.setOnAction(event -> {
            gameActor.tell(position.getText()+","+(idCounter++)
                    +","+r.nextInt(255)+","+r.nextInt(255)+","+r.nextInt(255)
                    ,ActorRef.noSender());
        });
        label = new Label("Text");

        VBox pane = new VBox(button,position);
        pane.setSpacing(15);
        pane.setAlignment(Pos.CENTER);
        Scene scene = new Scene(pane,500,500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("SERVER");
        primaryStage.show();
    }

    public static void main(String[] args) {


        launch(args);
    }
}
