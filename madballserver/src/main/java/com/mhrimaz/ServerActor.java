package com.mhrimaz;

import akka.actor.*;

public class ServerActor extends AbstractActor {
    private final ServerGUI gameClient;
    private final ActorRef[][] actorGridRef;
    private int xRow,yRow;
    public ServerActor(ServerGUI gameClient) {
        actorGridRef = new ActorRef[3][3];
        xRow = 0;
        yRow = 0;
        this.gameClient = gameClient;
    }

    public static Props props(ServerGUI gameClient){
        return Props.create(ServerActor.class,()->new ServerActor(gameClient));
    }

    private void handleNehigbors(){
        int leftX = xRow;
        int leftY = yRow - 1;
        int upX = xRow - 1;
        int upY =  yRow ;
        if(isValid(leftX,leftY)){
            if(actorGridRef[leftX][leftY]!=null){
                actorGridRef[leftX][leftY].tell("introduceRight",actorGridRef[xRow][yRow]);
                actorGridRef[xRow][yRow].tell("introduceLeft",actorGridRef[leftX][leftY]);
            }
        }
        if(isValid(upX,upY)){
            if(actorGridRef[upX][upY]!=null){
                actorGridRef[upX][upY].tell("introduceDown",actorGridRef[xRow][yRow]);
                actorGridRef[xRow][yRow].tell("introduceUp",actorGridRef[upX][upY]);
            }
        }
    }

    private boolean isValid(int x,int y){
        return x>=0 && x<=3 && y>=0 && y<=3;
    }
  
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class,s -> {
                    if(s.equals("join")){
                        if(xRow<3&&yRow<3) {
                            actorGridRef[xRow][yRow] = getSender();
                            handleNehigbors();
                            yRow = (yRow + 1);
                            if (yRow > 2) {
                                yRow = 0;
                                xRow++;
                            }
                        }
                    }
                    if(s.split(",").length==8){
                        actorGridRef[0][0].tell(s,getSelf());
                    }
                    System.out.println("s = " + s);
                }).matchAny(o -> {
                    System.out.println("o = " + o);
                }).build();
    }
}
