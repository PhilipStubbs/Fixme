package Server;

import Responsibilty.AbstractLogger;
import Server.RoutingTable.RoutingTable;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static Responsibilty.AbstractLogger.DEBUG;
import static Responsibilty.AbstractLogger.ERROR;
import static Responsibilty.AbstractLogger.INFO;
import static Responsibilty.Logger.getChainOfLoggers;

public class RouterAsync extends Thread {
	private int port;
	private List<SocketHandlerAsync> clientList;
	private List<String> messages = new ArrayList<String>();
	private AbstractLogger logger = getChainOfLoggers();



	public RouterAsync(int port){
		this.port = port;
		clientList = new ArrayList<SocketHandlerAsync>();
	}

	private void startServer(){
		try {
			AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open().bind(new InetSocketAddress("127.0.0.1", port));

			logger.logMessage(1,"Server listening on :"+port);

			while(true){
				Future<AsynchronousSocketChannel> acceptCon = server.accept();
				AsynchronousSocketChannel client = acceptCon.get();

				SocketHandlerAsync socketHandlerAsync = new SocketHandlerAsync(client, clientList.size() ,messages);
				logger.logMessage(INFO,"Added Client: " + socketHandlerAsync.getClientId());
				clientList.add(socketHandlerAsync);
				if (port == 5001)
					RoutingTable.updateRoutingTable(socketHandlerAsync);

				socketHandlerAsync.start();
			}

		}  catch (InterruptedException | ExecutionException | IOException e){
			logger.logMessage(3,getClass().getSimpleName() + "> Server Exception "+ e.getLocalizedMessage());
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
				if (clientList.get(i).getClientId().contains(id))
				{
					socketHandlerAsync = clientList.get(i);
				}
			}
			if (socketHandlerAsync != null) {
				String message = str + " " + id;												// message
				socketHandlerAsync.sendMessage(message);
				logger.logMessage(DEBUG,"Writing back to client: " + message);
			} else {
				logger.logMessage(ERROR,getClass().getSimpleName() + "> failed to send message to :"+ id);
			}

		} catch (Exception e){
			logger.logMessage(ERROR,getClass().getSimpleName()+"> Server Exception "+ e.getLocalizedMessage());
		}
		finally {
			messages.remove(str);
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
