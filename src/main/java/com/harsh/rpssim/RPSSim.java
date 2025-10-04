/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.harsh.rpssim;

import java.util.Random;
import java.util.concurrent.*;

/**
 *
 * @author Harsh Noise
 */

interface ProgressListener {
    void onProgressUpdate(Integer[] progress); // Interface to send progress updates
}

public class RPSSim{ 
    static final int BOARDSIZE = 720;
    static final int BASEMOVEMENTSPEED = 100;
    static final int THREADS = 16;
    
    static double movementSpeed = 2;  //units per second
    static int creatureNumber = 99;
    static long simTime = 0;    //Milliseconds per turn
    
    static ExecutorService executor = Executors.newFixedThreadPool(THREADS);
    static Random rand = new Random();
    
    static volatile Creature[] players;

    RPSSim(){
        
    }

    public int getBOARDSIZE() {
        return (BOARDSIZE);
    }

    public int getCREATURENUMBER() {
        return (creatureNumber);
    }
    
    public void setCreatureNumber(int x){
        creatureNumber = x;
    }
    public double getMOVEMENTSPEED() {
        return (movementSpeed);
    }

    public int getTHREADS() {
        return (THREADS);
    }
    
    public long getSimTime(){
        return(simTime);
    }
    
    private static ProgressListener listener;

    public void setProgressListener(ProgressListener listener) {
        this.listener = listener;
    }
    
    public void initialize(){
        simTime = 0;    //Milliseconds per turn
        
        players = new Creature[creatureNumber];
        for(int x = 0; x < (creatureNumber); x++){
            players[x] = new Creature(rand.nextDouble() * BOARDSIZE, rand.nextDouble() * BOARDSIZE, x%3);
        }
    }
    
    public static void updateMoveSpeed(){
        movementSpeed = ((simTime / 1000.0) * BASEMOVEMENTSPEED); //speed (units/turn) = (ms/turn)(1s/1000ms)(units/second)
    }
    
    public String playerToString(int location){
        return("[" + players[location].getXCoords() + ", " + players[location].getYCoords() + ", " + players[location].getTeam() + "]");
    }
    
    public boolean isOver(){
        int winningTeam = players[0].getTeam();
        for(Creature candidate : players){
            if(candidate.getTeam() != winningTeam){
                return(false);
            }
        }
        return(true);
    }
    
    public Integer[] getNumTeams(){
        //System.out.println("In getNumTeams");
        //System.out.println(creatureNumber + ", " + players.length + ", " + playerToString(0));
        Integer[] returnArray = new Integer[3];
        returnArray[0] = 0;
        returnArray[1] = 0;
        returnArray[2] = 0;
        for (Creature candidate : players) {
            //System.out.println("Iterating once " + candidate.getTeam());
            returnArray[candidate.getTeam()]++;
            //System.out.println(returnArray[0] + ", " + returnArray[1] + ", " + returnArray[2]);
        }
        //System.out.println("Returning");

        return(returnArray);
    }
    
    public double getRelativeDistance(Creature source, Creature target) {
        //because we only need to know if a creature is closer to being capured or capturing, we can leave out the square root
        return (Math.pow(target.getXCoords() - source.getXCoords(), 2) + Math.pow(target.getYCoords() - source.getYCoords(), 2));
    }

    public double getAngle(Creature source, Creature target) {
        double xDif = target.getXCoords() - source.getXCoords();
        double yDif = target.getYCoords() - source.getYCoords();

        return ((Math.atan2(yDif, xDif)));
    }

    public Creature moveCreature(Creature source, Creature target, boolean isFlee) {
        double angle = getAngle(source, target);

        if (isFlee) {
            double newX = (source.getXCoords() - (Math.cos(angle) * movementSpeed));
            double newY = (source.getYCoords() - (Math.sin(angle) * movementSpeed));
            return(new Creature(Math.max(Math.min(newX, BOARDSIZE), 0), Math.max(Math.min(newY, BOARDSIZE), 0), source.getTeam()));
        }
        double newX = (source.getXCoords() + (Math.cos(angle) * movementSpeed));
        double newY = (source.getYCoords() + (Math.sin(angle) * movementSpeed));
        return (new Creature(Math.max(Math.min(newX, BOARDSIZE), 0), Math.max(Math.min(newY, BOARDSIZE), 0), source.getTeam()));
    }
    
    public Creature calculateCreature(Creature creature){
        switch(creature.getTeam()){
            case 0:
                Creature fightTarget = new Creature(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 2);
                Creature flightTarget = new Creature(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 1);
                double fightDistance = Double.POSITIVE_INFINITY;
                double flightDistance = Double.POSITIVE_INFINITY;
                
                for(Creature candidate : players){
                    if(candidate.getTeam() == 0){
                        continue;
                    }
                    
                    double testDistance = getRelativeDistance(creature, candidate);
                    
                    switch(candidate.getTeam()){
                        case 1:
                            if (testDistance < flightDistance) {
                                //System.out.println("New closest paper at " + testDistance);
                                flightTarget = candidate;
                                flightDistance = testDistance;
                            }
                            break;
                        case 2:
                            if (testDistance < fightDistance) {
                                //System.out.println("New closest scissor at " + testDistance);
                                fightTarget = candidate;
                                fightDistance = testDistance;
                            }
                            break;
                    }
                }
                
                if (flightDistance < Math.pow(movementSpeed, 2)) { //if capture
                    creature.setTeam(1);
                    return(moveCreature(creature, flightTarget, true));
                    //System.out.println("Captured!");
                } else if (fightDistance < flightDistance) {
                    return(moveCreature(creature, fightTarget, false));
                    //System.out.println("Fight!");
                } else {
                    return(moveCreature(creature, flightTarget, true));
                    //System.out.println("Flee!");
                }
            case 1:
                fightTarget = new Creature(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 2);
                flightTarget = new Creature(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 1);
                fightDistance = Double.POSITIVE_INFINITY;
                flightDistance = Double.POSITIVE_INFINITY;

                for (Creature candidate : players) {
                    if (candidate.getTeam() == 1) {
                        continue;
                    }

                    double testDistance = getRelativeDistance(creature, candidate);

                    switch (candidate.getTeam()) {
                        case 2:
                            if (testDistance < flightDistance) {
                                //System.out.println("New closest paper at " + testDistance);
                                flightTarget = candidate;
                                flightDistance = testDistance;
                            }
                            break;
                        case 0:
                            if (testDistance < fightDistance) {
                                //System.out.println("New closest scissor at " + testDistance);
                                fightTarget = candidate;
                                fightDistance = testDistance;
                            }
                            break;
                    }
                }

                if (flightDistance < Math.pow(movementSpeed, 2)) { //if capture
                    creature.setTeam(2);
                    return (moveCreature(creature, flightTarget, true));
                    //System.out.println("Captured!");
                } else if (fightDistance < flightDistance) {
                    return (moveCreature(creature, fightTarget, false));
                    //System.out.println("Fight!");
                } else {
                    return (moveCreature(creature, flightTarget, true));
                    //System.out.println("Flee!");
                }
            case 2:
                fightTarget = new Creature(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 2);
                flightTarget = new Creature(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 1);
                fightDistance = Double.POSITIVE_INFINITY;
                flightDistance = Double.POSITIVE_INFINITY;

                for (Creature candidate : players) {
                    if (candidate.getTeam() == 2) {
                        continue;
                    }

                    double testDistance = getRelativeDistance(creature, candidate);

                    switch (candidate.getTeam()) {
                        case 0:
                            if (testDistance < flightDistance) {
                                //System.out.println("New closest paper at " + testDistance);
                                flightTarget = candidate;
                                flightDistance = testDistance;
                            }
                            break;
                        case 1:
                            if (testDistance < fightDistance) {
                                //System.out.println("New closest scissor at " + testDistance);
                                fightTarget = candidate;
                                fightDistance = testDistance;
                            }
                            break;
                    }
                }

                if (flightDistance < Math.pow(movementSpeed, 2)) { //if capture
                    creature.setTeam(0);
                    return(moveCreature(creature, flightTarget, true));
                    //System.out.println("Captured!");
                } else if (fightDistance < flightDistance) {
                    return(moveCreature(creature, fightTarget, false));
                    //System.out.println("Fight!");
                } else {
                    return(moveCreature(creature, flightTarget, true));
                    //System.out.println("Flee!");
                }
        }
        return(creature);
    }
    
    public void singleTurn() {
        for (int x = 0; x < players.length; x++) {
            players[x] = calculateCreature(players[x]);
        }
    }
    
    public void singleTurn(int start, int end) {
        //System.out.println("Inside thread calculating from " + start + ", " + end);
        for (int x = start; x < end; x++) {
            //System.out.println("Calculating player " + x);
            players[x] = calculateCreature(players[x]);
        }
    }
    
    public Creature[] distributeThreads() {
        //System.out.println("In distribute threads");
        if (listener != null) {
            listener.onProgressUpdate(getNumTeams()); // Notify progress
        }
        
        long start =  System.currentTimeMillis();
        
        double threadLoad = (double) creatureNumber/THREADS; //cover 1/4 the img
        Future<?>[] futures = new Future<?>[THREADS];
        
        //Loop for starting threads
        //System.out.println("Starting " + THREADS + " threads");
        //System.out.println("For all in threads");
        for (int x = 0; x < THREADS; x++) {
            int startPoint = (int) (x * threadLoad);
            int endPoint = (int) ((x + 1) * threadLoad);
            if (creatureNumber - endPoint < threadLoad) {
                endPoint = creatureNumber;
            }
            //System.out.println("Thread working on " + startPoint + ", " + endPoint);
            //hacky final copy because executor gets cranky
            final int s = startPoint;
            final int e = endPoint;
            //System.out.println("Executing threads");
            futures[x] = executor.submit(() -> {
                singleTurn(s, e);
            });
            //System.out.println("All done starting threads!");
        }
        
        try{
            //System.out.println("Verifying if done");
            for (int i = 0; i < THREADS; i++) {
                futures[i].get();
            }
            //System.out.println("All threads done!");
        }catch(Exception e){
            
        }
        
        
        //System.out.println("updating speed");
        simTime = System.currentTimeMillis() - start;
        updateMoveSpeed();
        //System.out.println("New movement speed = " + movementSpeed);
        
        //System.out.println("Returning");
        return(players);
    }
    
    public static void main(String[] args){
        RPSSim sim = new RPSSim();
        
        System.out.println(sim.getCREATURENUMBER());
        sim.setCreatureNumber(5);
        System.out.println(sim.getCREATURENUMBER());
        sim.initialize();
        System.out.println(sim.getCREATURENUMBER());
        
    }
}
