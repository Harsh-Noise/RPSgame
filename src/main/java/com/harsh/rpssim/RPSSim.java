/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.harsh.rpssim;

import com.madgag.gif.fmsware.AnimatedGifEncoder;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import javax.imageio.ImageIO;
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
    static int CREATURENUMBER = 99;
    static final int BASEMOVEMENTSPEED = 100;
    static double MOVEMENTSPEED = 2;  //units per second
    static int THREADS = 16;
    
    static int simTimeCount = 0;
    static long simTime = 0;    //Milliseconds per turn
    
    static ExecutorService executor = Executors.newFixedThreadPool(THREADS);
    static Random rand = new Random();
    static AnimatedGifEncoder gif = new AnimatedGifEncoder();
    
    //Variables edited by the algorithm
    //Subimage size for each thread
    //static int startPoint = 0;
    //static int endPoint = 0;

    //Counters for threads
    static volatile double loadBarChunks = 0.0;
    
//    static ArrayList<double[]> rocks = new ArrayList<double[]>();
//    static ArrayList<double[]> papers = new ArrayList<double[]>();
//    static ArrayList<double[]> scissors = new ArrayList<double[]>();
    
    static volatile Creature[] players;

    RPSSim(){
        
    }

    public int getBOARDSIZE() {
        return (BOARDSIZE);
    }

    public int getCREATURENUMBER() {
        return (CREATURENUMBER);
    }
    
    public void setCreatureNumber(int x){
        CREATURENUMBER = x;
    }
    public double getMOVEMENTSPEED() {
        return (MOVEMENTSPEED);
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
        simTimeCount= 0;
        simTime = 0;    //Milliseconds per turn
        
        players = new Creature[CREATURENUMBER];
        for(int x = 0; x < (CREATURENUMBER); x++){
            players[x] = new Creature(rand.nextDouble() * BOARDSIZE, rand.nextDouble() * BOARDSIZE, x%3);
        }
    }
    
    public static void updateMoveSpeed(){
        //System.out.println("In updateMoveSpeed");
        //System.out.println(simTime + ", " + simTimeCount + ", " + 1000 + ", " + BASEMOVEMENTSPEED);
        //MOVEMENTSPEED = (((simTime/simTimeCount) / 1000.0) * BASEMOVEMENTSPEED); //speed (units/turn) = (avg ms/turn)(1s/1000ms)(units/second)
        MOVEMENTSPEED = ((simTime / 1000.0) * BASEMOVEMENTSPEED); //speed (units/turn) = (ms/turn)(1s/1000ms)(units/second)
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
        //System.out.println(CREATURENUMBER + ", " + players.length + ", " + playerToString(0));
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
            double newX = (source.getXCoords() - (Math.cos(angle) * MOVEMENTSPEED));
            double newY = (source.getYCoords() - (Math.sin(angle) * MOVEMENTSPEED));
            return(new Creature(Math.max(Math.min(newX, BOARDSIZE), 0), Math.max(Math.min(newY, BOARDSIZE), 0), source.getTeam()));
        }
        double newX = (source.getXCoords() + (Math.cos(angle) * MOVEMENTSPEED));
        double newY = (source.getYCoords() + (Math.sin(angle) * MOVEMENTSPEED));
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
                
                if (flightDistance < Math.pow(MOVEMENTSPEED, 2)) { //if capture
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

                if (flightDistance < Math.pow(MOVEMENTSPEED, 2)) { //if capture
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

                if (flightDistance < Math.pow(MOVEMENTSPEED, 2)) { //if capture
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
        
        /*
        if (THREADS > CREATURENUMBER) {
            THREADS = CREATURENUMBER;
        } else {
            THREADS = 16;
        }
        */
        
        long start =  System.currentTimeMillis();
        
        //double subImageSize = (double) imageSize / threads;
        double threadLoad = (double) CREATURENUMBER/THREADS; //cover 1/4 the img
        Future<?>[] futures = new Future<?>[THREADS];
        
        //Loop for starting threads
        //System.out.println("Starting " + THREADS + " threads");
        //System.out.println("For all in threads");
        for (int x = 0; x < THREADS; x++) {
            int startPoint = (int) (x * threadLoad);
            int endPoint = (int) ((x + 1) * threadLoad);
            if (CREATURENUMBER - endPoint < threadLoad) {
                endPoint = CREATURENUMBER;
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
        //simTime += System.currentTimeMillis() - start;
        simTime = System.currentTimeMillis() - start;
        simTimeCount++;
        updateMoveSpeed();
        //System.out.println("New movement speed = " + MOVEMENTSPEED);
        
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
        
        /*
        sim.initialize();
        
        gif.setBackground(Color.WHITE);
        gif.setQuality(1);
        gif.start("output.gif");
        gif.setDelay(100); // ms between frames
        gif.setRepeat(0);  // infinite loop

        int frames = 0;
        double start = (double) System.currentTimeMillis();

        //while(!sim.isOver()){
        for (int x = 0; x < 100; x++) {
            gif.addFrame(sim.distributeThreads());
            frames++;
            //System.out.println("FPS/B = " + ((double)frames)/(((double)System.currentTimeMillis()-start)/1000.0));
        }
        
        gif.addFrame(sim.getImage());
        
        System.out.println("FPS = " + ((double) frames) / ((((double) System.currentTimeMillis() - start) / 1000.0)));
        
        gif.finish();
        
        System.out.println("all done!");
        
        painter.shutDown();
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException ex) {
            System.getLogger(RPSSim.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
*/
    }
}
