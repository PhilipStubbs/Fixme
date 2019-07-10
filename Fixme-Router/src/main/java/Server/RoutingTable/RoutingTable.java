package Server.RoutingTable;

import Server.SocketHandlerAsync;
import java.util.ArrayList;

public class RoutingTable {

    static private ArrayList<ArrayList<SocketHandlerAsync>> routingTable = new ArrayList<ArrayList<SocketHandlerAsync>>();;


    public static void updateRoutingTable(SocketHandlerAsync newMarket){
        try {
            routingTable.get(newMarket.getIndex()).add(newMarket);
        } catch (IndexOutOfBoundsException e){
            // If an error is thrown. We create the new list.
            routingTable.add(new ArrayList<SocketHandlerAsync>());
            routingTable.get(newMarket.getIndex()).add(newMarket);
        }
    }

    public static ArrayList<ArrayList<SocketHandlerAsync>> getRoutingTable() {
        return routingTable;
    }
}
