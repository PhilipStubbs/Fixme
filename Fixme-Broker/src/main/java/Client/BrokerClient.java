package Client;

import BaseClient.BaseClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class BrokerClient extends BaseClient {

	public BrokerClient(int port){
		this.port = port;
		try (AsynchronousSocketChannel client = AsynchronousSocketChannel.open()) {
			this.client = client;
			Future<Void> result = client.connect(new InetSocketAddress("127.0.0.1", port));

			result.get();

			/* Awaiting for ID from Router	*/
			logger.logMessage(1, "Awaiting ID");
			getServerMessage();
			String[] split = messages.get(0).split(" ");
			int tmpInt = Integer.parseInt(split[1]);
			id = split[0];

			messages.clear();
			logger.logMessage(1,"ID Assigned :"+id);

			Scanner scanner = new Scanner(System.in);
			try {
				while (true) {
					System.out.println("Please input a line");
					String line = scanner.nextLine();
					System.out.printf("User input was: %s%n", line);
					sendServerMessage(line);
					TimeUnit.SECONDS.sleep(1);

					getServerMessage();
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
