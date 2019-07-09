package Client;

import BaseClient.BaseClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


public class MarketClient extends BaseClient {
	String[] instruments = {"Gold", "Silver", "Bitcoin", "Red Sugar", "Morkite", "Apoca Bloom"};
	int index;
	int	stock;

	public MarketClient(int port) {
			this.port = port;
			stock = 100;
			try (AsynchronousSocketChannel client = AsynchronousSocketChannel.open()) {
				Future<Void> result = client.connect(new InetSocketAddress("127.0.0.1", port));
				this.client = client;
				result.get();

				/* Awaiting for ID from Router	*/
				logger.logMessage(1, "Awaiting ID");
				getServerMessage();
				String[] split = messages.get(0).split(" ");
				int tmpInt = Integer.parseInt(split[1]);
				id = split[0];
				index = tmpInt > 5 ? tmpInt % 6 : tmpInt;

				messages.clear();
				logger.logMessage(1,"ID Assigned :"+id);
				logger.logMessage(1,"We Sell :"+ instruments[index]);


				while (true) {
					sendServerMessage("test");
					TimeUnit.SECONDS.sleep(1);
					getServerMessage();
				}

			}
			catch (ExecutionException | IOException e) {
				e.printStackTrace();
			}
			catch (InterruptedException e) {
				System.out.println("Disconnected from the server.");
			} finally {
				System.out.println("finally"+client.isOpen());


		}
	}

}
