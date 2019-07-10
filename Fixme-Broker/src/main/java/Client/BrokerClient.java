package Client;

import BaseClient.BaseClient;
import Server.RoutingTable.RoutingTable;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class BrokerClient extends BaseClient {
	Scanner scanner;
	private int numOrders;
	private int numQuotes;

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
			int tmpInt = Integer.parseInt(split[1]);				// TODO client number -> maybe dont need for broker.
			id = split[0];

			messages.clear();
			logger.logMessage(1,"ID Assigned :"+id);

			scanner = new Scanner(System.in);
			try {
				while (true) {
					logger.logMessage(1,"---------Please input a command---------");
					logger.logMessage(1,"BUY  - buy an instrument from the markets");
					logger.logMessage(1,"SELL - sell an instrument to the markets");
					logger.logMessage(1,"EXIT - close connection to the server");

					String line = scanner.nextLine();
					if (line.equalsIgnoreCase("exit")){
						sendServerMessage("exit");
						this.terminateConnection();
						break;
					}
					else if (line.equalsIgnoreCase("buy")){
						this.buy();
					}
					else if (line.equalsIgnoreCase("sell")){
						this.sell();
					}
					logger.logMessage(2, "User input was: "+ line);
//					sendServerMessage(line);
					TimeUnit.SECONDS.sleep(1);

					getServerMessage();

					if (!messages.isEmpty()) {
						String msgArr[] = messages.get(0).split("\\|");
						messages.remove(0);
						if (msgArr.length > 14) {
							int price = Integer.parseInt(getFixValue(13, msgArr));
							int quantity = Integer.parseInt(getFixValue(11, msgArr));
							String msgType = getFixValue(8, msgArr);
							switch (getFixValue(14, msgArr)) {
								case "1": //Accepted
									if (msgType.equals("D")) {
									}
									//TODO Add to inventory;
									else if (msgType.equals("S")) {
									}
									//TODO Remove from Inventory
									break;
								case "2": //Refused
									break;
								default:
									//TODO no response
									break;
							}
						}
					}
				}
			} catch(IllegalStateException | NoSuchElementException e) {
				// System.in has been closed
				logger.logMessage(3,"System.in was closed; exiting");
			}
		}
		catch (ExecutionException | IOException e) {
			e.printStackTrace();
		}
		catch (InterruptedException e) {
			logger.logMessage(3 ,"Disconnected from the server.");
		} finally {
			try {
				client.close();
			} catch (IOException e){
				// NO OP
			}
		}
	}

	private void buy() {

		String instrument ="";
		String market = "";
		String quantity = "";
		String price = "";

		logger.logMessage(1,"-------Please fill in the following details--------");
		logger.logMessage(1, RoutingTable.getRoutingTable().toString());

		logger.logMessage(1,"Enter Instrument You Wish to buy:");
		while (scanner.hasNext()) {
			instrument = scanner.nextLine();
			break;
			//TODO Check if instrument is in list else get another input
		}

		logger.logMessage(1,"Which market would you like to buy from (Please choose their index):");
		while (scanner.hasNext()) {
			market = scanner.nextLine();
			break;
			//TODO Check if market is in list else get another input
		}

		logger.logMessage(1,"How many:");
		while (scanner.hasNext()) {
			quantity = scanner.nextLine();
			try
			{
				int val = Integer.parseInt(quantity);
				if (val > 0 && val < 100000)
					break;
			}
			catch (NumberFormatException nfe)
			{
				logger.logMessage(1, "Please enter an integer value between 0 and 100000");
			}
		}

		logger.logMessage(1, "At what price:");
		while (scanner.hasNext()) {
			price = scanner.nextLine();
			try
			{
				float val = Float.parseFloat(price);
				if (val > 0 && val < 99999.99)
					break;
			}
			catch (NumberFormatException nfe)
			{
				logger.logMessage(1, "Please enter a float value between 0.00 and 99999.99");
			}

		}

		Instant instant = Instant.now();
		ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

		String fix = String.format("35=D|49=%s|56=%s|52=%s|11=%d|21=1|55=D|54=1|60=%s|38=%s|40=1|44=%s|39=0|",id, market, now, ++this.numOrders,instrument, quantity, price);
		fix = "8=FIX.4|9="+fix.getBytes().length+"|"+fix+"10="+checksum(ByteBuffer.wrap(fix.getBytes()), fix.length()) + "|\n";
		logger.logMessage(2, fix);
		sendServerMessage(fix);
	}

	private void sell() {
		String instrument ="";
		String market = "";
		String quantity = "";
		String price = "";

		logger.logMessage(1,"-------Please fill in the following details--------");
		logger.logMessage(1, RoutingTable.getRoutingTable().toString());

		logger.logMessage(1, "Enter Instrument You Wish to sell:");
		while (scanner.hasNext()) {
			instrument = scanner.nextLine();
			break;
			//TODO Check if instrument is in brokers list else get another input
		}

		logger.logMessage(1,"Which market would you like to sell to (Please choose their index):");
		while (scanner.hasNext()) {
			market = scanner.nextLine();
			break;
			//TODO Check if market is in list else get another input
		}

		logger.logMessage(1,"How many:");
		while (scanner.hasNext()) {
			quantity = scanner.nextLine();
			try
			{
				int val = Integer.parseInt(quantity);
				if (val > 0 && val < 100000)
					break;
			}
			catch (NumberFormatException nfe)
			{
				logger.logMessage(1, "Please enter an integer value between 0 and 100000");
			}
		}

		logger.logMessage(1, "At what price:");
		while (scanner.hasNext()) {
			price = scanner.nextLine();
			try
			{
				float val = Float.parseFloat(price);
				if (val > 0 && val < 99999.99)
					break;
			}
			catch (NumberFormatException nfe)
			{
				logger.logMessage(1, "Please enter a float value between 0.00 and 99999.99");
			}

		}

		Instant instant = Instant.now();
		ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
		String fix = String.format("35=S|49=%s|56=%s|52=%s|117=%s|55=S|54=2|60=%s|38=%s|40=1|44=%s|39=0|",id,market, now, ++this.numQuotes, instrument, quantity, price);
		fix = "8=FIX.4|9="+fix.getBytes().length+"|"+fix+"10="+checksum(ByteBuffer.wrap(fix.getBytes()), fix.length()) + "|";
		logger.logMessage(2, fix);
		sendServerMessage(fix);
	}

	public AsynchronousSocketChannel getClient() {
		return client;
	}

	public static int checksum(ByteBuffer b, int end) {
		int checksum = 0;
		for (int i = 0; i < end; i++) {
			checksum += b.get(i);
		}
		return checksum % 256;
	}
}
