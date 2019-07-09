package Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Server {
    static final int brokerPort = 5000;
    static final int marketPort = 5001;
    static private List<String> marketMessages = new ArrayList<String>();
    static private List<String> brokerMessages = new ArrayList<String>();
    static private  List<SocketHandlerAsync> marketClientList = new ArrayList<SocketHandlerAsync>();
    static private  List<SocketHandlerAsync> brokerClientList = new ArrayList<SocketHandlerAsync>();

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

                // TODO -- message parusing is required. And extracting UUID from it.
                TimeUnit.SECONDS.sleep(1);
                System.out.println("msg:"+marketMessages.size() +" client:"+marketClientList.size());
                if (marketMessages.size() > 0 && marketClientList.size() > 0) {
                    String tmpMessage = marketMessages.get(0);
                    routerMarketAsync.sendMessage(tmpMessage, routerMarketAsync.getClientList().get(0).getUuid());
                    marketMessages.remove(tmpMessage);
                }
//            brokerMessages = routerBrokerAsync.getMessages();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}


