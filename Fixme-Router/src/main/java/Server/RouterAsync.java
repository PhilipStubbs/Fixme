package Server;

import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;
import java.util.List;
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
				// TODO
				SocketHandlerAsync socketHandlerAsync = new SocketHandlerAsync(client, messages);
				System.out.println("Added Client: " + socketHandlerAsync.getClientId());
				clientList.add(socketHandlerAsync);
				socketHandlerAsync.start();
			}

		}  catch (Exception e){
			System.out.println("Server Exception "+ e.getLocalizedMessage());
		}
	}

	@Override
	public void run() {
		startServer();
	}

	public void sendMessage(String str, String id) {
		try {
			SocketHandlerAsync socketHandlerAsync = null;
			for (int i = 0; i < clientList.size(); i++)
			{
				if (clientList.get(i).getClientId().equals(id))
				{
					socketHandlerAsync = clientList.get(i);
				}
			}
			if (socketHandlerAsync != null) {
				String message = str + " " + id;												// message
				socketHandlerAsync.sendMessage(message);
				System.out.println("Writing back to client: " + message);
			} else {
				System.out.println(getClass().getSimpleName() + "> failed to send message to :"+ id);
			}

		} catch (Exception e){
			System.out.println(getClass().getSimpleName()+"> Server Exception "+ e.getLocalizedMessage());
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
