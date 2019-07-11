package BaseClient;

import Responsibilty.AbstractLogger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static Responsibilty.AbstractLogger.INFO;
import static Responsibilty.Logger.getChainOfLoggers;

public class BaseClient {
	protected int port;
	protected AsynchronousSocketChannel client;
	protected ArrayList<String> messages = new ArrayList<>();
	protected String id;
	protected AbstractLogger logger = getChainOfLoggers();

	public BaseClient(int port){
		this.port = port;
		try (AsynchronousSocketChannel client = AsynchronousSocketChannel.open()) {
			Future<Void> result = client.connect(new InetSocketAddress("127.0.0.1", port));
			this.client = client;
			result.get();

			/* Awaiting for ID from Router	*/
			logger.logMessage(1, "Awaiting ID");
			getServerMessage();
			id = messages.get(0);
			messages.clear();
			logger.logMessage(1,"ID Assigned :"+id);


			Random rn = new Random();

			// TODO -- proper logic for messaging.
			while (true) {
				sendServerMessage("test" + rn.nextInt());
				TimeUnit.SECONDS.sleep(1);
				getServerMessage();
			}

		}
		catch (ExecutionException | IOException e) {
			e.printStackTrace();
		}
		catch (InterruptedException e) {
			System.out.println("Disconnected from the server.");
		}
	}

	public BaseClient(){};

	public void getServerMessage() {
		try {
			logger.logMessage(2 ,"Is client open " + client.isOpen());
			ByteBuffer buffer = ByteBuffer.allocate(1024);				// TODO -- find better way to allocate buffersize
			Future<Integer> readval = client.read(buffer);                // fetches from server
			if (readval.get() == -1){
				this.terminateConnection();
			}

			String message = new String(buffer.array()).trim();
			logger.logMessage(2 ,"Received from server: " + message);
			messages.add(message);

		} catch (ExecutionException | InterruptedException e){
			e.printStackTrace();
		}
	}

	public void sendServerMessage(String message){
		try {
			ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
			Future<Integer> writeval = client.write(buffer);				//writes to server
			logger.logMessage(2 ,"Writing to server: "+message);
			writeval.get();
		} catch (ExecutionException | InterruptedException e){
			e.printStackTrace();
		}
	}

	public void terminateConnection() {
		try {
			logger.logMessage(INFO,"Connection Terminated");
			client.close();
		} catch (IOException e) {
			// NO OP
		} finally {
			System.exit(0);
		}
	}

	public String getFixValue(int index, String[] arr){
		return arr[index].substring(arr[index].lastIndexOf("=") + 1);
	}

	public String[] setFixValue(int index, String newValue, String[] arr){
		String orgValue = getFixValue(index, arr);
		arr[index] = arr[index].replace(orgValue, newValue);
		return arr;
	}

	public AsynchronousSocketChannel getClient() {
		return client;
	}

	public List<String> getMessages() {
		return messages;
	}
}
