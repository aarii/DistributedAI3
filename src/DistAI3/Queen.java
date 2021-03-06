package DistAI3;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;


/**
 * Created by araxi on 2016-11-24.
 */

//false är upptagen
//true är ledig
public class Queen extends Agent{

    AID nextQueen;
    Boolean[][] myBoard;
    Boolean[][] boardToSend;
    ArrayList<Double> availableSpots = new ArrayList<Double>();
    int arraySize;
    AID prevQueen = new AID();
    boolean onlyOnce = false;
    boolean onlyOnce1 = false;

    @Override
    protected void setup() {

        Object[] args = getArguments();

        if(args.length == 3) {
            arraySize = Integer.parseInt((String) args[1]);
            myBoard = new Boolean[arraySize][arraySize];
            boardToSend = new Boolean[arraySize][arraySize];
            for(int x = 0; x< arraySize; x++){
                for (int y = 0; y<arraySize; y++){
                    double x1 = x;
                    double y1 = y;
                    //  availableSpots.add((x1 + y1 /10));
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
                            //System.out.println("I am " + getLocalName() + " and my nextQueen is " + nextQueen.getName());
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

        String firstQueen = "";
        String lastQueen = "";
        Object[] args = getArguments();

        if(args.length == 3) {
            firstQueen = (String) args[2];
        }
        if(args.length==2) {
            lastQueen = (String) args[1];
        }

        if (firstQueen.equalsIgnoreCase("first")) {

            for(int i = 0; i<boardToSend.length; i++){
                for(int j = 0; j < boardToSend.length; j++){
                    boardToSend[i][j] = myBoard[i][j];
                }
            }
            double firstPos = randomPos(arraySize);
            String[] arr = String.valueOf(firstPos).split("\\.");
            int[] intArr = new int[2];
            intArr[0] = Integer.parseInt(arr[0]);
            intArr[1] = Integer.parseInt(arr[1]);
            int xPos = intArr[0];
            int yPos = intArr[1];
            System.out.println(getLocalName()+ "'s chosen pos is  " + xPos + " , " + yPos);
            boardToSend[xPos][yPos] = false;
            setBusySpots(xPos, yPos);
            // removeFromAvailablePos(xPos, yPos);
            System.out.println("Board to send from " + getLocalName() + " is: ");
            for (int x = 0; x < arraySize; x++) {
                for (int y = 0; y < arraySize; y++) {

                    if(boardToSend[x][y] == true) {

                        System.out.print(" a ");
                    }else{
                        if(xPos == x && yPos == y){
                            System.out.print(" "+ getLocalName());
                        }else{
                        System.out.print(" o ");
                    }}
                }
                System.out.println();
            }


            addBehaviour(new TickerBehaviour(this, 1000) {
                @Override
                protected void onTick() {
                    if (nextQueen != null) {
                        ACLMessage sendToNextQueen = new ACLMessage(ACLMessage.INFORM);
                        try {

                            sendToNextQueen.setContentObject(boardToSend);
                            sendToNextQueen.addReceiver(nextQueen);
                            send(sendToNextQueen);
                            removeBehaviour(this);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });


            addBehaviour(new TickerBehaviour(this, 1000) {
                @Override
                protected void onTick() {
                    ACLMessage receiveFromNextQueen = blockingReceive();

                    if(receiveFromNextQueen.getPerformative() == ACLMessage.REQUEST) {
                        placeQueenOnBoard();
                        removeBehaviour(this);
                    }

                }
            });


        }else {


            ACLMessage receivedFromPreviousQueen = blockingReceive();
            try {
                myBoard = (Boolean[][]) receivedFromPreviousQueen.getContentObject();
            } catch (UnreadableException e) {
                e.printStackTrace();
            }

            boardToSend = new Boolean[myBoard.length][myBoard.length];
            prevQueen = receivedFromPreviousQueen.getSender();
           // System.out.println("I am queen " + getLocalName()+ " and got a message from " + prevQueen.getLocalName());

            findAvailableSpots();


            if(receivedFromPreviousQueen.getPerformative() == ACLMessage.INFORM) {
                logic(lastQueen);
            }

        }


    }


    private void logic(String lastQueen){

        for(int i = 0; i<boardToSend.length; i++){
            for(int j = 0; j < boardToSend.length; j++){
                boardToSend[i][j] = myBoard[i][j];
            }
        }


        if(!availableSpots.isEmpty()) {
            String[] arr = String.valueOf(availableSpots.get(0)).split("\\.");
            int[] intArr = new int[2];
            intArr[0] = Integer.parseInt(arr[0]);
            intArr[1] = Integer.parseInt(arr[1]);
            int xPos = intArr[0];
            int yPos = intArr[1];

            System.out.print("Available positions for " + getLocalName() + " are: ");
            for (int i = 0; i< availableSpots.size(); i++){
                System.out.print(availableSpots.get(i) + " ");
            }
            System.out.println();
            System.out.println(getLocalName() + "'s chosen position is: " + xPos + "," + yPos);
            boardToSend[xPos][yPos] = false;
            setBusySpots(xPos, yPos);
            removeFromAvailablePos(xPos, yPos);

            System.out.println("Board to send from " + getLocalName() + " is: ");
            for (int x = 0; x < boardToSend.length; x++) {
                for (int y = 0; y < boardToSend.length; y++) {

                    if(boardToSend[x][y] == true) {

                        System.out.print(" a ");
                    }else{
                        if(xPos == x && yPos == y){
                            System.out.print(" "+ getLocalName());
                        }else{
                            System.out.print(" o ");
                        }}
                }
                System.out.println();
            }



            if(!lastQueen.equalsIgnoreCase("last")){

                addBehaviour(new TickerBehaviour(this, 1000) {
                    @Override
                    protected void onTick() {
                        if(nextQueen != null) {
                            ACLMessage sendToNextQueen = new ACLMessage(ACLMessage.INFORM);
                            try {

                                sendToNextQueen.setContentObject(boardToSend);
                                sendToNextQueen.addReceiver(nextQueen);
                                send(sendToNextQueen);
                               // System.out.println("I am " + getLocalName() + " and just sent a message to " + nextQueen);
                                removeBehaviour(this);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

                addBehaviour(new TickerBehaviour(this, 1000) {
                    @Override
                    protected void onTick() {
                        ACLMessage receivedFromNextQueen = blockingReceive();


                        if (receivedFromNextQueen.getPerformative() == ACLMessage.REQUEST) {
                            System.out.println(receivedFromNextQueen.getSender().getLocalName() + " requested " + getLocalName()  + " to move");
                            logic(lastQueen);
                            removeBehaviour(this);
                        }
                    }
                });

            }


        }else{
            ACLMessage sendToPrevQueen = new ACLMessage(ACLMessage.REQUEST);

            sendToPrevQueen.setPerformative(ACLMessage.REQUEST);


            sendToPrevQueen.addReceiver(prevQueen);
            send(sendToPrevQueen);
            placeQueenOnBoard();
           // System.out.println("I am " + getLocalName() + " and just sent a message to " + prevQueen);

        }
    }

    private void removeFromAvailablePos(int xPos, int yPos) {

        for (int i = 0; i< availableSpots.size(); i++){
            String[] arr=String.valueOf(availableSpots.get(i)).split("\\.");
            int[] intArr=new int[2];
            intArr[0]=Integer.parseInt(arr[0]);
            intArr[1]=Integer.parseInt(arr[1]);
            int x = intArr[0];
            int y = intArr[1];

            if (x == xPos && y == yPos){
                availableSpots.remove(i);
            }
        }

    }
    private void findAvailableSpots(){
        for(int x = 0; x<myBoard.length; x++){
            for(int y=0; y<myBoard.length; y++){
                if(myBoard[x][y] == true){
                    double x1 = x;
                    double y1 = y;
                    availableSpots.add((x1 + y1 /10));
                }
            }
        }
    }
    private void setBusySpots(int xPos, int yPos){
        for(int x = 0; x <boardToSend.length; x++){
            for(int y = 0; y <boardToSend.length; y++){
                checkRow(xPos, x,y);
                checkColumn(yPos,x, y);
                checkDiagonal(xPos, yPos, x , y);

            }
        }

    }
    private  void checkRow(int xPos, int x, int y) {

        if(xPos == x){
            boardToSend[x][y] = false;
        }

    }
    private  void checkColumn(int yPos, int x, int y) {
        if(yPos == y){
            boardToSend[x][y] = false;
        }
    }
    private  void checkDiagonal(int xPos, int yPos, int x, int y) {
        int tempx1 = x;
        int tempy1 = y;
        int tempx2 = x;
        int tempy2 = y;
        int tempx3 = x;
        int tempy3 = y;
        int tempx4 = x;
        int tempy4 = y;
        for(int i = 0; i<boardToSend.length; i++) {
            tempx1++;
            tempy1++;
            tempx2--;
            tempy2++;
            tempx3++;
            tempy3--;
            tempx4--;
            tempy4--;
            if (tempx1  == xPos && tempy1 == yPos){
                boardToSend[x][y] = false;
            }

            if (tempx2 == xPos && tempy2 == yPos){
                boardToSend[x][y] = false;
            }

            if (tempx3 == xPos && tempy3 == yPos){
                boardToSend[x][y] = false;
            }

            if (tempx4 == xPos && tempy4 == yPos){
                boardToSend[x][y] = false;
            }
        }

    }
    private double randomPos(int arraySize){
        int x = ThreadLocalRandom.current().nextInt(0 + arraySize);
        double y = (Math.floor(ThreadLocalRandom.current().nextDouble(0 + arraySize)) / 10);
        double result = x + y;
        return  result;
    }




}

