package Server;

import Responsibilty.AbstractLogger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static Responsibilty.AbstractLogger.INFO;
import static Responsibilty.Logger.getChainOfLoggers;

public class SocketHandlerAsync extends Thread{
	private AsynchronousSocketChannel socket;
	private List<String> messages;
	private String id;
	private AbstractLogger logger = getChainOfLoggers();
	private int index;
	private boolean isAlive;


	public SocketHandlerAsync(AsynchronousSocketChannel socket, int clientListSize ,List<String> messages){
		this.socket = socket;
		this.messages = messages;
		String epochString = String.valueOf(Instant.now().toEpochMilli());
		this.id = epochString.substring(7);
		this.isAlive = true;
		int tmpInt = clientListSize;
		index = tmpInt > 5 ? tmpInt % 6 : tmpInt;
		sendMessage(id + " " + index);
	}

	@Override
	public void run() {
		try {
			while(this.isAlive){
				if ((socket!= null) && (socket.isOpen()) && this.isAlive) {

					ByteBuffer buffer = ByteBuffer.allocate(1024);
					Future<Integer> readval = socket.read(buffer);		// readers from client
					readval.get();


					String clientMessage =  new String(buffer.array()).trim();
//					logger.logMessage(2, "Received from client: "	+clientMessage);

					messages.add(clientMessage);
					buffer.flip();
					buffer.clear();
				}
			}

		} catch (Exception e){
			logger.logMessage(3, getClass().getSimpleName()+"> Error: " + e.getLocalizedMessage());
		} finally {
			terminateConnection();
		}
	}

	public void sendMessage(String message){
		try {
				ByteBuffer messageByteBuffer = ByteBuffer.allocate(message.length());
				messageByteBuffer.wrap(message.getBytes());

				Future<Integer> writeVal = socket.write(messageByteBuffer.wrap(message.getBytes()));        // writes to client
				writeVal.get();
		}
		 catch (InterruptedException | ExecutionException e){
			 logger.logMessage(3, getClass().getSimpleName()+"> Server Exception "+ e.getLocalizedMessage());
			 terminateConnection();
		}
	}

	public void terminateConnection() {
		try {
			logger.logMessage(INFO,"Connection Terminated");
			socket.close();
		} catch (IOException e) {
			// NO OP
		} finally {
			this.isAlive = false;
			Thread.interrupted();
		}
	}


	public String getClientId() {
		return id;
	}

	public int getIndex() {
		return index;
	}

	public AsynchronousSocketChannel getSocket() {
		return socket;
	}
}
