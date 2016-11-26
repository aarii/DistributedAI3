package DistAI3;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;


/**
 * Created by araxi on 2016-11-24.
 */

//false är upptagen
//true är ledig
public class Queen extends Agent{

    AID nextQueen = new AID();
    Boolean[][] myBoard;
    Boolean[][] boardToSend;
    ArrayList<Double> availablePos = new ArrayList<Double>();
    int arraySize;

    @Override
    protected void setup() {

        Object[] args = getArguments();

        if(args.length == 3) {
            arraySize = Integer.parseInt((String) args[1]);
            myBoard = new Boolean[arraySize][arraySize];

            for(int x = 0; x< arraySize; x++){
                for (int y = 0; y<arraySize; y++){
                    double x1 = x;
                    double y1 = y;
                    availablePos.add((x1 + y1 /10));
                    myBoard[x][y] = true;
                    boardToSend[x][y] = true;
                }
            }
        }

        createServiceInDF();
        if(args.length != 2) {
            searchForNextQueenInDF();
        }

        placeQueenOnBoard();


    }
    protected void createServiceInDF(){
        Object[] args = getArguments();
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd;
        System.out.println("get aid är " + getAID().getName());
        dfd.setName(getAID());
        sd = new ServiceDescription();
        sd.setType((String)args[0]);
        sd.setName("Queen");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }

    }
    protected void searchForNextQueenInDF(){
        addBehaviour(new TickerBehaviour(this, 1000) {
            @Override
            protected void onTick() {
                Object[] args = getArguments();
                try {
                    //System.out.println("hee");

               /* Subscribe Artifacts service by Curator agent */
                    DFAgentDescription dfd = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();

                    int arg1 = Integer.parseInt((String) args[0]) + 1;
                    String arg2 = String.valueOf(arg1);
                    sd.setType(arg2);
                    dfd.addServices(sd);
                    SearchConstraints sc = new SearchConstraints();
                    sc.setMaxResults(new Long(1));
                    send(DFService.createSubscriptionMessage(myAgent, getDefaultDF(), dfd, sc));
                    DFAgentDescription[] result = DFService.search(myAgent, dfd);
                    if(result.length == 1){

                        for (int i = 0; i < result.length; i++) {
                            nextQueen = result[i].getName();
                           // System.out.println("I am " + getLocalName() + " and my nextQueen is " + nextQueen.getName());
                            removeBehaviour(this);
                        }
                    }
                } catch (FIPAException e) {
                    e.printStackTrace();
                }

            }
        });
    }
    private void placeQueenOnBoard(){

        Object[] args = getArguments();
        String firstQueen = "";
        String lastQueen = "";
        if(args.length == 3) {
            firstQueen = (String) args[2];
        }
        if(args.length==2) {
            lastQueen = (String) args[1];
        }

    if (firstQueen.equalsIgnoreCase("first")) {
        double firstPos = randomPos(arraySize);

        String[] arr=String.valueOf(firstPos).split("\\.");
        int[] intArr=new int[2];
        intArr[0]=Integer.parseInt(arr[0]);
        intArr[1]=Integer.parseInt(arr[1]);
        int x = intArr[0];
        int y = intArr[1];
        boardToSend[x][y] = false;
        setBusySpots(boardToSend, x, y);




    } else if (lastQueen.equalsIgnoreCase("last")) {

    } else {

    }




    }
    private void setBusySpots(Boolean [][] boardToSend, int xPos, int yPos){
        for(int x = 0; x <arraySize; x++){
            for(int y = 0; y <arraySize; y++){

                boolean result1 = checkRow(xPos, x);
                boolean result2 = checkColumn(yPos, y);

                boolean result3 = checkDiagonal(xPos, yPos, x , y);

            }
        }

    }


    private boolean checkRow(int xPos, int x) {

        if(xPos == x){
            return false;
        }else{
            return true;
        }
    }
    private boolean checkColumn(int yPos, int y) {

        if(yPos == y){
            return false;
        }else{
            return true;
        }

    }

    private boolean checkDiagonal(int xPos, int yPos, int x, int y) {
    for(int i = 0; i<arraySize; i++) {

    if (x++ == xPos && y++ == yPos) {
        return false;
    }
    if (x-- == xPos && y-- == yPos) {
        return false;
    }

    if (x-- == xPos && y++ == yPos) {
        return false;
    }
    if (x++ == xPos && y-- == yPos) {
        return false;
    }
}
   return true;
    }







    private double randomPos(int arraySize){
        int x = ThreadLocalRandom.current().nextInt(0 + arraySize + 1);
        double y = (Math.floor(ThreadLocalRandom.current().nextDouble(0 + arraySize + 1)) / 10);
        double result = x + y;
        return  result;
    }


}

