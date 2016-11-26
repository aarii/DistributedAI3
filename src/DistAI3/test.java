package DistAI3;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by araxi on 2016-11-25.
 */
public class test {
    static int arraySize = 4;
    static Boolean[][] boardToSend;
    public static void main(String[] args) {
    boardToSend = new Boolean[arraySize][arraySize];
        for(int x = 0; x< arraySize; x++){
            for (int y = 0; y<arraySize; y++){

                boardToSend[x][y] = true;
            }
        }
        setBusySpots(boardToSend, 2,2);


    }


    private static void setBusySpots(Boolean [][] boardToSend, int xPos, int yPos){
        for(int x = 0; x <arraySize; x++){
            for(int y = 0; y <arraySize; y++){

                checkRow(xPos, x, y);
                checkColumn(yPos,x, y);
                //VI SKA ITERERA DIAGONALERNA OCH FÅ DET ATT FUNKA!!!! vi har skapat en algoritm där vi har 4 olika cases. Varje
                //case har x++ y-- osv osv vi vill få det att funka med false och true grejern ai vår Boolean [][]
                checkDiagonal(xPos, yPos, x , y);

                System.out.println("On xpos: " + x + " and ypos: " + y + " gives us value " + boardToSend[x][y]);
            }
        }

    }


    private static void checkRow(int xPos, int x, int y) {

        if(xPos == x){
           boardToSend[x][y] = false;
        }


    }
    private static void checkColumn(int yPos, int x, int y) {

        if(yPos == y){
            boardToSend[x][y] = false;

        }
    }


    private static void checkDiagonal(int xPos, int yPos, int x, int y) {
        int tempx1 = x;
        int tempy1 = y;
        int tempx2 = x;
        int tempy2 = y;
        int tempx3 = x;
        int tempy3 = y;
        int tempx4 = x;
        int tempy4 = y;
        for(int i = 0; i<arraySize; i++) {

       // System.out.println("tempx är " + tempx + " tempy är " + tempy);

            if ((tempx1+1) == xPos && (tempy1+1) == yPos) {
                boardToSend[x][y] = false;


            }
            if ((tempx2-1) == xPos && (tempy2-1) == yPos) {
                boardToSend[x][y] = false;

            }

            if ((tempx3-1) == xPos && (tempy3+1) == yPos) {
                boardToSend[x][y] = false;

            }
            if ((tempx4+1) == xPos && (tempy4-1) == yPos) {
                boardToSend[x][y] = false;

            }
        }
    }

}
