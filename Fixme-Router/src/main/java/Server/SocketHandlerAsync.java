package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.UUID;

public class SocketHandlerAsync extends Thread{
	private AsynchronousSocketChannel socket;
	private UUID uuid;

	public SocketHandlerAsync(AsynchronousSocketChannel socket){
		this.socket = socket;
		this.uuid = UUID.randomUUID();
	}

	@Override
	public void run() {
		try {
//			BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			while(true){
//				String echoString = input.readLine();
//				PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
//
//				if (echoString.equals("exit")){
//					break;
//				}
//				output.println(echoString);
			}

		} catch (Exception e){
			System.out.println("Error: " + e.getLocalizedMessage());
		} finally {
			try {
				socket.close();
			} catch (IOException e){
				// NO OP
			}
		}
	}

	public UUID getUuid() {
		return uuid;
	}

	public AsynchronousSocketChannel getSocket() {
		return socket;
	}
}

