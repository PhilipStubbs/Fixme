package Server;

import Responsibilty.AbstractLogger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static Responsibilty.Logger.getChainOfLoggers;

public class SocketHandlerAsync extends Thread{
	private AsynchronousSocketChannel socket;
	private List<String> messages;
	private String id;
	private AbstractLogger logger = getChainOfLoggers();


	public SocketHandlerAsync(AsynchronousSocketChannel socket, int clientListSize ,List<String> messages){
		this.socket = socket;
		this.messages = messages;
		String epochString = String.valueOf(Instant.now().toEpochMilli());
		this.id = epochString.substring(7);
		sendMessage(id + " " + clientListSize);
	}

	@Override
	public void run() {
		try {

			while(true){
				if ((socket!= null) && (socket.isOpen())) {

					ByteBuffer buffer = ByteBuffer.allocate(1024);
					Future<Integer> readval = socket.read(buffer);		// readers from client
					readval.get();


					String clientMessage =  new String(buffer.array()).trim();
					logger.logMessage(2, "Received from client: "	+clientMessage);
					// TODO -- get messages out of thread and into RouterAsync.
					messages.add(clientMessage);
					buffer.flip();
					buffer.clear();
				}
			}

		} catch (Exception e){
			logger.logMessage(3, "Error: " + e.getLocalizedMessage());
		} finally {
			try {
				socket.close();
			} catch (IOException e){
				// NO OP
			}
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
		}
	}


	public String getClientId() {
		return id;
	}

	public AsynchronousSocketChannel getSocket() {
		return socket;
	}
}
