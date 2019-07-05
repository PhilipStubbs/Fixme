package Server;

import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class RouterAsync extends Thread {
	private int port;
	private List<SocketHandler> clientList = new ArrayList<SocketHandler>();

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
					SocketHandler socketHandler = new SocketHandler(client);
					clientList.add(socketHandler);
					System.out.println("Added Client: " + socketHandler.getUuid().toString());
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

	public List<SocketHandler> getClientList() {
		return clientList;
	}

	public void setClientList(List<SocketHandler> clientList) {
		this.clientList = clientList;
	}
}
