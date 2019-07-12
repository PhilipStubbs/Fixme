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
import java.util.concurrent.TimeUnit;

import static Responsibilty.AbstractLogger.DEBUG;
import static Responsibilty.AbstractLogger.ERROR;
import static Responsibilty.AbstractLogger.INFO;
import static Responsibilty.ConsoleLogger.*;
import static Responsibilty.Logger.getChainOfLoggers;

public class RouterAsync extends Thread {
	private int port;
	private List<SocketHandlerAsync> clientList;
	private ArrayList<String> messages = new ArrayList<String>();
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

				SocketHandlerAsync socketHandlerAsync = new SocketHandlerAsync(client, clientList.size() ,messages, port);
				String clientType = port == 5000 ? ANSI_PURPLE+"Broker"+ANSI_RESET : ANSI_YELLOW+"Market"+ANSI_RESET ;
				logger.logMessage(INFO,"Added "+clientType+": " + socketHandlerAsync.getClientId());
				clientList.add(socketHandlerAsync);
				socketHandlerAsync.start();
				if (port == 5001) {
					RoutingTable.updateMarketRoutingTable(socketHandlerAsync);
				} else if (port == 5000){
					RoutingTable.updateBrokerRoutingTable(socketHandlerAsync);
					TimeUnit.SECONDS.sleep(1);							// TODO -- Look into not using a sleep.
					socketHandlerAsync.sendMessage(RoutingTable.serializedMarketString());
				}

			}

		}  catch (InterruptedException | ExecutionException | IOException e){
			logger.logMessage(3,getClass().getSimpleName() + "> Server Exception "+ e.getLocalizedMessage());
		}
	}

	@Override
	public void run() {
		startServer();
	}

	public void informBrokers() {
		ArrayList<SocketHandlerAsync> brokerRoutingTable = RoutingTable.getBrokerRoutingTable();
		String serialized = RoutingTable.serializedMarketString();

		for (int i = 0; i < brokerRoutingTable.size(); i++){
			brokerRoutingTable.get(i).sendMessage(serialized);
		}
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
				String message = str;												// message
				socketHandlerAsync.sendMessage(message);
				String clientType = port == 5000 ? ANSI_PURPLE+"Broker"+ANSI_RESET : ANSI_YELLOW+"Market"+ANSI_RESET ;
				logger.logMessage(DEBUG,BLUE_UNDERLINED+"Writing"+ANSI_RESET+" to "+clientType+" "+id+": " + message);
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
		/* checks for dead threads */
		for(int i = 0; i < this.clientList.size(); ++i){
			if (!this.clientList.get(i).isClientAlive() || !this.clientList.get(i).getSocket().isOpen()){
				logger.logMessage(1, getClass().getSimpleName()+"> Removing client:" + this.clientList.get(i).getClientId());
				if (port == 5001) {
					RoutingTable.removeMarket(this.clientList.get(i));
				}
				if (port == 5000) {
					RoutingTable.removeBroker(this.clientList.get(i));
				}
				this.clientList.remove(i);
			}
		}
		return this.clientList;
	}

	public void setClientList(List<SocketHandlerAsync> clientList) {
		this.clientList = clientList;
	}

	public ArrayList<String> getMessages() {
		return messages;
	}
}
