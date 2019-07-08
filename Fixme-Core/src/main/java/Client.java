import Server.RouterAsync;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Client {
    private int port;
    private AsynchronousSocketChannel client;
    private List<String> messages = new ArrayList<String>();

    public Client(int port){
        this.port = port;
        try (AsynchronousSocketChannel client = AsynchronousSocketChannel.open()) {
            Future<Void> result = client.connect(new InetSocketAddress("127.0.0.1", port));
            this.client = client;

            result.get();

            sendServerMessage("test");
            getServerMessage();

            // TODO -- seems like we might have to put market code in here. Else the connection closes.

        }
        catch (ExecutionException | IOException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            System.out.println("Disconnected from the server.");
        } finally {
            System.out.println("finally"+client.isOpen());

        }
    }

    public void getServerMessage() {
        try {
            System.out.println(client.isOpen());
            ByteBuffer buffer = ByteBuffer.allocate(1024);				// TODO -- find better way to allocate buffersize
            Future<Integer> readval = client.read(buffer);                // fetches from server
            readval.get();
            String message = new String(buffer.array()).trim();
            System.out.println("Received from server: " + message);
            messages.add(message);

        } catch (ExecutionException | InterruptedException e){
            e.printStackTrace();
        }
    }

    public void sendServerMessage(String message){
        try {
            ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
            Future<Integer> writeval = client.write(buffer);				//writes to server
            System.out.println("Writing to server: "+message);
            writeval.get();
        } catch (ExecutionException | InterruptedException e){
            e.printStackTrace();
        }
    }

    public void terminateConnection(RouterAsync router) {
        try {
            router.getClientList().remove(this);
        } finally {
            try {
                System.out.println("Connection Terminated");
                client.close();
            } catch (IOException e) {
                // NO OP
            }
        }
    }

    public AsynchronousSocketChannel getClient() {
        return client;
    }

    public List<String> getMessages() {
        return messages;
    }
}

