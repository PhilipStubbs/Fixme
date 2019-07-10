package Server.RoutingTable;

import Instruments.Instruments;
import Server.SocketHandlerAsync;
import java.util.ArrayList;

public class RoutingTable {

    static private ArrayList<ArrayList<SocketHandlerAsync>> marketRoutingTable = new ArrayList<ArrayList<SocketHandlerAsync>>();
    static private ArrayList<SocketHandlerAsync> brokerRoutingTable = new ArrayList<SocketHandlerAsync>();

    public static void updateMarketRoutingTable(SocketHandlerAsync newMarket){
        try {
            marketRoutingTable.get(newMarket.getIndex()).add(newMarket);
        } catch (IndexOutOfBoundsException e){
            // If an error is thrown. We create the new list.
            marketRoutingTable.add(new ArrayList<SocketHandlerAsync>());
            marketRoutingTable.get(newMarket.getIndex()).add(newMarket);
        }
    }

    public static ArrayList<ArrayList<SocketHandlerAsync>> getMarketRoutingTable() {
        return marketRoutingTable;
    }

    public static void updateBrokerRoutingTable(SocketHandlerAsync newBroker){
        brokerRoutingTable.add(newBroker);
    }

    static public String serializedMarketString() {
        String serializedString = "MARKETUPDATE|";
        for (int i = 0 ; i < marketRoutingTable.size(); i++){
            serializedString += i+ "_";
            for (int x = 0; x < marketRoutingTable.get(i).size(); x++){
                serializedString += marketRoutingTable.get(i).get(x).getClientId() +" ";
            }
            serializedString += "\n";
        }
        return serializedString;
    }

    public static ArrayList<SocketHandlerAsync> getBrokerRoutingTable() {
        return brokerRoutingTable;
    }
}
