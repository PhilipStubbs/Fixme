package Client;

import Server.RouterAsync;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Client {
	private int port;
	private AsynchronousSocketChannel client;

	public Client(int port){
		this.port = port;
		try (AsynchronousSocketChannel client = AsynchronousSocketChannel.open()) {
			this.client = client;
			Future<Void> result = client.connect(new InetSocketAddress("127.0.0.1", port));

			result.get();
			String str= "Hello! How are you?";

			TimeUnit.SECONDS.sleep(5);

			ByteBuffer buffer = ByteBuffer.wrap(str.getBytes());

			Future<Integer> writeval = client.write(buffer);			// sends to server
			System.out.println("Writing to server: "+str);

			writeval.get();
			buffer.flip();

			Future<Integer> readval = client.read(buffer);				// fetches from server


			System.out.println("Received from server: "	+new String(buffer.array()).trim());


			readval.get();
			buffer.clear();

		}
		catch (ExecutionException | IOException e) {
			e.printStackTrace();
		}
		catch (InterruptedException e) {
			System.out.println("Disconnected from the server.");
		} finally {
			try {
				client.close();
			} catch (IOException e){
				// NO OP
			}
		}
	}

	public void terminateConnection(RouterAsync router) {
		try {
			router.getClientList().remove(this);
		} finally {
			try {

				client.close();
			} catch (IOException e) {
				// NO OP
			}
		}
	}

	public AsynchronousSocketChannel getClient() {
		return client;
	}
}
