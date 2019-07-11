package Server;

import Instruments.Instruments;
import Responsibilty.AbstractLogger;
import Server.RoutingTable.RoutingTable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


import static Responsibilty.Logger.getChainOfLoggers;

public class Server {
    static final int brokerPort = 5000;
    static final int marketPort = 5001;
    static private List<String> marketMessages = new ArrayList<>();
    static private List<String> brokerMessages = new ArrayList<>();
    static private  List<SocketHandlerAsync> marketClientList = new ArrayList<SocketHandlerAsync>();
    static private  List<SocketHandlerAsync> brokerClientList = new ArrayList<SocketHandlerAsync>();
    protected static AbstractLogger logger = getChainOfLoggers();
    static private ArrayList<ArrayList<SocketHandlerAsync>> routingTable;

    public static void main(String[] args) {
        RouterAsync routerBrokerAsync = new RouterAsync(brokerPort);
        RouterAsync routerMarketAsync = new RouterAsync(marketPort);
        routerBrokerAsync.start();
        routerMarketAsync.start();


        while (true) {
            try {
                marketClientList = routerMarketAsync.getClientList();
                brokerClientList = routerBrokerAsync.getClientList();
                marketMessages = routerMarketAsync.getMessages();
                brokerMessages = routerBrokerAsync.getMessages();
                routingTable = RoutingTable.getMarketRoutingTable();

                // TODO -- message parsing is required. And extracting UUID from it.
//                outputRoutingTable();
                TimeUnit.SECONDS.sleep(1);
                if (marketMessages.size() > 0 && marketClientList.size() > 0) {
                    logger.logMessage(2, "Market Server Port:5001 msg:"+marketMessages.size() +" client:"+marketClientList.size());
                    String tmpMessage = marketMessages.get(0);
                    String strArray[] = tmpMessage. split("\\|");

                    if (strArray.length > 14) {
                        String msgTo = strArray[14].substring(strArray[14].lastIndexOf("=") + 1);
                        if (msgTo.equals("0")){
                            //TODO is FIX is not working
                            String marketId = strArray[4].substring(strArray[4].lastIndexOf("=") + 1); //Get broker index
                            routerBrokerAsync.sendMessage(tmpMessage, marketId);
                        }
                        else{
                            String brokerId = strArray[3].substring(strArray[3].lastIndexOf("=") + 1); //Get broker index
                            routerBrokerAsync.sendMessage(tmpMessage, brokerId);
                        }
                    }
                    marketMessages.remove(0);
                }

                if (brokerMessages.size() > 0 && brokerClientList.size() > 0) {
                    logger.logMessage(2, "Broker Server Port:5000 msg:"+brokerMessages.size() +" client:"+brokerClientList.size());
                    String tmpMessage = brokerMessages.get(0);
                    String strArray[] = tmpMessage. split("\\|");

                    if (strArray.length > 14){
                        System.out.println("HELLOOOOOOOO");
                        String msgTo = strArray[14].substring(strArray[14].lastIndexOf("=") + 1);
                        if (msgTo.equals("0")) {
                            String marketId = strArray[4].substring(strArray[4].lastIndexOf("=") + 1);
                            routerMarketAsync.sendMessage(tmpMessage, marketId);
                        }
                    }
                    brokerMessages.remove(0);
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IndexOutOfBoundsException ibe){
                ibe.printStackTrace();
            }
        }
    }

    static private void outputRoutingTable(){
        String tmp = "";
        for (int i = 0 ; i < routingTable.size(); i++){
            tmp += Instruments.instruments[i]+ " -> ";
            for (int x = 0; x < routingTable.get(i).size(); x++){
                tmp += routingTable.get(i).get(x).getClientId() +" ";
            }
            logger.logMessage(2, tmp);
            tmp = "";
        }
    }


}


