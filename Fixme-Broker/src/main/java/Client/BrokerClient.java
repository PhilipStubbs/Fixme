package Client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class BrokerClient {
	private int port;
	private AsynchronousSocketChannel client;

	public BrokerClient(int port){
		this.port = port;
		try (AsynchronousSocketChannel client = AsynchronousSocketChannel.open()) {
			this.client = client;
			Future<Void> result = client.connect(new InetSocketAddress("127.0.0.1", port));

			result.get();

			Scanner scanner = new Scanner(System.in);
			try {
				while (true) {
					System.out.println("Please input a line");
					String line = scanner.nextLine();
					System.out.printf("User input was: %s%n", line);

					ByteBuffer buffer = ByteBuffer.wrap(line.getBytes());

					Future<Integer> writeval = client.write(buffer);			// sends to server
					System.out.println("Writing to server: "+line);

					writeval.get();
					buffer.flip();

					Future<Integer> readval = client.read(buffer);
					readval.get();
					System.out.println("Received from server: "	+ new String(buffer.array()).trim());
					System.out.println("After Test");
					buffer.clear();
				}
			} catch(IllegalStateException | NoSuchElementException e) {
				// System.in has been closed
				System.out.println("System.in was closed; exiting");
			}
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

	public AsynchronousSocketChannel getClient() {
		return client;
	}
}
