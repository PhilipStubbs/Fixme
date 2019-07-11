package Client;

import BaseClient.BaseClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import Instruments.Instruments;


public class MarketClient extends BaseClient {
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
			id = split[0];
			index = Integer.parseInt(split[1]);

			messages.clear();
			logger.logMessage(1,"ID Assigned: "+id);
			logger.logMessage(1,"We Sell: "+ Instruments.instruments[index]);


			while (true) {
//					sendServerMessage("test");
				TimeUnit.SECONDS.sleep(1);
				getServerMessage();
				if (!messages.isEmpty()) {
					System.out.println("MESSAGE LIST SIZE = "+ messages.size());
					String message = messages.get(0);
					messages.remove(0);

					String msgArr[] = message.split("\\|");
					if (msgArr.length > 14) {
						int price = Integer.parseInt(getFixValue(13, msgArr));
						int quantity = Integer.parseInt(getFixValue(11, msgArr));
						String instrument = getFixValue(10, msgArr);
						switch (getFixValue(8, msgArr)) { //Could also use index 9 and adjust case values to 1 and 2 (Buy and Sell)
							case "D": {
								if (quantity <= stock) {
									stock -= quantity;
									sendServerMessage(String.join("|", setFixValue(14, "1", msgArr)) + "|");
								} else {
									sendServerMessage(String.join("|", setFixValue(14, "2", msgArr)) + "|");
								}
								break;
							}
							case "S": {
								stock += quantity;
								sendServerMessage(String.join("|", setFixValue(14, "1", msgArr)) + "|");
								break;
							}
							default:
								sendServerMessage(String.join("|", setFixValue(14, "2", msgArr)) + "|");
								break;
						}

					}
				}
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
