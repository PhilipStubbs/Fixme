package Server;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

public class RouterAsync extends Thread {
	private int port;
	private List<SocketHandlerAsync> clientList = new ArrayList<SocketHandlerAsync>();
	private List<String> messages = new ArrayList<String>();


	public RouterAsync(int port){
		this.port = port;
	}

	private void startServer(){
		try {
			AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open().bind(new InetSocketAddress("127.0.0.1", port));

			System.out.println("Server listening on :"+port);

			while(true){
				Future<AsynchronousSocketChannel> acceptCon = server.accept();
				AsynchronousSocketChannel client = acceptCon.get();
				if ((client!= null) && (client.isOpen())) {

					SocketHandlerAsync socketHandlerAsync = new SocketHandlerAsync(client);
					clientList.add(socketHandlerAsync);
					System.out.println("Server "+ this.port + " Added New Client: " + socketHandlerAsync.getUuid().toString());


					ByteBuffer buffer = ByteBuffer.allocate(1024);
					Future<Integer> readval = client.read(buffer);		// readers from client

					String clientMessage =  new String(buffer.array()).trim();
					System.out.println("Server "+ this.port + " Received Message from Client: "	+ clientMessage);
					messages.add(clientMessage);
					readval.get();
					buffer.flip();


					buffer.clear();

				}
			}

		}  catch (Exception e){
			System.out.println("Server Exception "+ e.getLocalizedMessage());
		}
	}

	@Override
	public void run() {
		startServer();
	}

	public void sendMessage(String str, UUID uuid){
		try {
			SocketHandlerAsync socketHandlerAsync = null;
			for (int i = 0; i < clientList.size(); i++)
			{
				if (clientList.get(i).getUuid() == uuid)
				{
					socketHandlerAsync = clientList.get(i);
				}
			}
			if (socketHandlerAsync != null) {
				AsynchronousSocketChannel client = socketHandlerAsync.getSocket();
				String message = str +  uuid.toString();
				ByteBuffer messageByteBuffer = ByteBuffer.allocate(message.length());
				messageByteBuffer.wrap(message.getBytes());

				Future<Integer> writeVal = client.write(messageByteBuffer.wrap(message.getBytes()));        // writes to client

				System.out.println("Writing back to client: " + message);
				writeVal.get();
			} else {
				System.out.println(getClass().getSimpleName() + "> failed to send message to :"+ uuid);
			}

		} catch (Exception e){
			System.out.println("Server Exception "+ e.getLocalizedMessage());
		}
	}

	public List<SocketHandlerAsync> getClientList() {
		return clientList;
	}

	public void setClientList(List<SocketHandlerAsync> clientList) {
		this.clientList = clientList;
	}

	public List<String> getMessages() {
		return messages;
	}
}
