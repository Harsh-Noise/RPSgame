/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.harsh.rpssim;

/**
 *
 * @author Harsh Noise
 */
public class Creature {

    double xCoords;
    double yCoords;
    int team;  //0=r, 1=p, 2=s

    Creature(double posX, double posY, int teamNum) {
        xCoords = posX;
        yCoords = posY;
        team = teamNum;
    }
    
    Creature(Creature copy) {
        xCoords = copy.getXCoords();
        yCoords = copy.getYCoords();
        team = copy.getTeam();
    }

    public void setXCoords(double x) {
        xCoords = x;
    }

    public double getXCoords() {
        return (xCoords);
    }

    public void setYCoords(double x) {
        yCoords = x;
    }

    public double getYCoords() {
        return (yCoords);
    }

    public void setTeam(int x) {
        team = x;
    }

    public int getTeam() {
        return (team);
    }
    
    public static void main(String[] args){
        Creature creat = new Creature(1.0, 2.0, 3);
        Creature copy = new Creature(creat);
        
        creat.setTeam(4);
        System.out.println(copy.getTeam());
    }
}
