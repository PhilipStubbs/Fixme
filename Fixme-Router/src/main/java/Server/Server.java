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

    public static void main(String[] args) {

        RouterAsync routerBrokerAsync = new RouterAsync(brokerPort);
        RouterAsync routerMarketAsync = new RouterAsync(marketPort);
        routerBrokerAsync.start();
        routerMarketAsync.start();


        while (true){
            marketMessages = routerMarketAsync.getMessages();
            // TODO -- message parusing is required. And extracting UUID from it.
            if (marketMessages.size() > 0 )
                System.out.println(marketMessages.size());
            if (marketMessages.size() > 0 && routerMarketAsync.getClientList().size() > 0) {
                routerMarketAsync.sendMessage(marketMessages.get(0), routerMarketAsync.getClientList().get(0).getUuid());
//                marketMessages.clear();
            }
//            brokerMessages = routerBrokerAsync.getMessages();
//            // TODO -- message parusing is required. And extracting UUID from it.
//            if (brokerMessages.size() > 0 && routerBrokerAsync.getClientList().size() > 0) {
//                routerBrokerAsync.sendMessage(brokerMessages.get(0), routerBrokerAsync.getClientList().get(0).getUuid());
////                brokerMessages.clear();
//            }
        }
    }


//    public static void test(){
//        try (AsynchronousServerSocketChannel server =  AsynchronousServerSocketChannel.open()) {
//            server.bind(new InetSocketAddress("127.0.0.1",
//                    1234));
//
//            Future<AsynchronousSocketChannel> acceptCon = server.accept();
//            AsynchronousSocketChannel client = acceptCon.get(10, TimeUnit.SECONDS);
//
//            if ((client!= null) && (client.isOpen())) {
//                ByteBuffer buffer = ByteBuffer.allocate(1024);
//                Future<Integer> readval = client.read(buffer);
//                System.out.println("Received from client: "
//                        + new String(buffer.array()).trim());
//                readval.get();
//                buffer.flip();
//                String str= "I'm fine. Thank you!";
//                Future<Integer> writeVal = client.write(
//                        ByteBuffer.wrap(str.getBytes()));
//                System.out.println("Writing back to client: "
//                        +str);
//                writeVal.get();
//                buffer.clear();
//            }
//            client.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}


