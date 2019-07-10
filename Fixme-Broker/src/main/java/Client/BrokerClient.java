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
			int tmpInt = Integer.parseInt(split[1]);
			id = split[0];

			messages.clear();
			logger.logMessage(1,"ID Assigned :"+id);

			scanner = new Scanner(System.in);
			try {
				while (true) {
					System.out.println("---------Please input a command---------");
					System.out.println("BUY  - buy an instrument from the markets");
					System.out.println("SELL - sell an instrument to the markets");
					System.out.println("EXIT - close connection to the server");

					String line = scanner.nextLine();
					if (line.equalsIgnoreCase("exit")){
						this.terminateConnection();
						break;
					}
					else if (line.equalsIgnoreCase("buy")){
						this.buy();
					}
					else if (line.equalsIgnoreCase("sell")){
						this.sell();
					}
//					System.out.printf("User input was: %s%n", line);
//					sendServerMessage(line);
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

	private void buy() {

		String instrument ="";
		String market = "";
		String quantity = "";
		String price = "";

		System.out.println("-------Please fill in the following details--------");
		System.out.println( RoutingTable.getRoutingTable());

		System.out.println("Enter Instrument You Wish to buy:");
		while (scanner.hasNext()) {
			instrument = scanner.nextLine();
			break;
			//TODO Check if instrument is in list else get another input
		}

		System.out.println("Which market would you like to buy from (Please choose their index):");
		while (scanner.hasNext()) {
			market = scanner.nextLine();
			break;
			//TODO Check if market is in list else get another input
		}

		System.out.println("How many:");
		while (scanner.hasNext()) {
			quantity = scanner.nextLine();
			break;
			//TODO Check if quantity is a proper value between 0 - 1000000
		}

		System.out.println("At what price:");
		while (scanner.hasNext()) {
			price = scanner.nextLine();
			break;
			//TODO Check if price is a proper value between 0 - 999999.99
		}

		Instant instant = Instant.now();
		ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

		String fix = String.format("35=D|49=%s|56=%s|52=%s|11=%d|21=1|55=D|54=1|60=%s|38=%s|40=1|44=%s|39=0|",id, market, now, ++this.numOrders,instrument, quantity, price);
		fix = "8=FIX.4|9="+fix.getBytes().length+"|"+fix+"10="+checksum(ByteBuffer.wrap(fix.getBytes()), fix.length()) + "|\n";
		logger.logMessage(2, fix);
		sendServerMessage(fix);
			//TODO Send FIX message to server
	}

	private void sell() {
		String instrument ="";
		String market = "";
		String quantity = "";
		String price = "";

		System.out.println("-------Please fill in the following details--------");
		System.out.println( RoutingTable.getRoutingTable());

		System.out.println("Enter Instrument You Wish to sell:");
		while (scanner.hasNext()) {
			instrument = scanner.nextLine();
			break;
			//TODO Check if instrument is in brokers list else get another input
		}

		System.out.println("Which market would you like to sell to (Please choose their index):");
		while (scanner.hasNext()) {
			market = scanner.nextLine();
			break;
			//TODO Check if market is in list else get another input
		}

		System.out.println("How many:");
		while (scanner.hasNext()) {
			quantity = scanner.nextLine();
			break;
			//TODO Check if quantity is a proper value between 0 - 1000000
		}

		System.out.println("At what price:");
		while (scanner.hasNext()) {
			price = scanner.nextLine();
			break;
			//TODO Check if price is a proper value between 0 - 999999.99
		}

		Instant instant = Instant.now();
		ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
		String fix = String.format("35=S|49=%s|56=%s|52=%s|117=%s|55=S|54=2|60=%s|38=%s|40=1|44=%s|39=0|",id,market, now, ++this.numQuotes, instrument, quantity, price);
		fix = "8=FIX.4|9="+fix.getBytes().length+"|"+fix+"10="+checksum(ByteBuffer.wrap(fix.getBytes()), fix.length()) + "|";
		logger.logMessage(2, fix);
		sendServerMessage(fix);
		//TODO Send FIX message to server
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
