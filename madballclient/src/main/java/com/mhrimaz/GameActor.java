package com.mhrimaz;

import akka.actor.*;


public class GameActor extends AbstractActor {
    private final GameClient gameClient;
    private ActorRef left;
    private ActorRef right;
    private ActorRef up;
    private ActorRef down;
    private ActorRef server;
    final String path = "akka.tcp://madballserver@127.0.0.1:2552/user/serveractor";

    public GameActor(GameClient gameClient) {
        this.gameClient = gameClient;
        getContext().actorSelection(path).tell(new Identify(path), self());
    }

    public static Props props(GameClient gameClient){
        return Props.create(GameActor.class,()->new GameActor(gameClient));
    }
    public enum Pos{
        LEFT,RIGHT,DOWN,UP
    }
    public static class BallStatus{
        private final Pos pos;
        private final double x;
        private final double y;
        private final double degree;
        private final int id;
        private final int r;
        private final int g;
        private final int b;
        private final double radius;

        public BallStatus(Pos pos, double x, double y, double degree,double radius,int id,
        int r, int g, int b) {
            this.pos = pos;
            this.x = x;
            this.y = y;
            this.degree = degree;
            this.id = id;
            this.r = r;
            this.g = g;
            this.b = b;
            this.radius = radius;
        }

        @Override
        public String toString() {
            return (int)x+","+(int)y+","+(int)degree+","+(int)radius+","+id+","+r+","+g+","+b;
        }
    }
    @Override
    public Receive createReceive() {
        return receiveBuilder().match(BallStatus.class,ballStatus -> {
            if (ballStatus.pos==Pos.RIGHT){

                if(right!=null) {
                    right.tell(ballStatus.toString(), getSelf());
                   // gameClient.toggle(ballStatus.id);
                }
            }else if (ballStatus.pos==Pos.UP){
                if(up!=null) {
                    up.tell(ballStatus.toString(), getSelf());
                    //gameClient.toggle(ballStatus.id);
                }
            }else if (ballStatus.pos==Pos.DOWN){
                if(down!=null) {
                    down.tell(ballStatus.toString(), getSelf());
                   // gameClient.toggle(ballStatus.id);
                }
            }else if (ballStatus.pos==Pos.LEFT){
                if(left!=null) {
                    left.tell(ballStatus.toString(), getSelf());
                  //  gameClient.toggle(ballStatus.id);
                }
            }
        }).match(String.class,s -> {
            String[] split = s.split(",");
            if(split.length == 8) {
                gameClient.setLabelText(s);
            }
            if(s.startsWith("introduce")){
                if(s.contains("Left")){
                    left = getSender();
                    getContext().watch(left);
                    gameClient.setLeftPresent(true);
                    System.out.println("LEFT JOINED");
                }
                if(s.contains("Right")){
                    right = getSender();
                    getContext().watch(right);
                    gameClient.setRightPresent(true);
                    System.out.println("RIGHT JOINED");
                }
                if(s.contains("Down")){
                    down = getSender();
                    getContext().watch(down);
                    gameClient.setDownPresent(true);
                    System.out.println("DOWN JOINED");
                }
                if(s.contains("Up")){
                    up = getSender();
                    getContext().watch(up);
                    gameClient.setUpPresent(true);
                    System.out.println("UP JOINED");
                }
            }
        }).match(ActorIdentity.class,actorIdentity -> {
            ActorRef ref = actorIdentity.getActorRef().get();
            if(ref!=null){
                server = ref;
                server.tell("join",getSelf());
            }
        }).match(Terminated.class,terminated -> {
            if(terminated.getActor().equals(left)){
                left = null;
                gameClient.setLeftPresent(false);
                System.out.println("Left Leaved");
            }
            if(terminated.getActor().equals(right)){
                right = null;
                gameClient.setRightPresent(false);
                System.out.println("Right Leaved");
            }
            if(terminated.getActor().equals(down)){
                down = null;
                gameClient.setDownPresent(false);
                System.out.println("Down Leaved");
            }
            if(terminated.getActor().equals(up)){
                up = null;
                gameClient.setUpPresent(false);
                System.out.println("Up Leaved");
            }
        }).matchAny(o -> {
            System.out.println("o = " + o);
        }).build();
    }
}
