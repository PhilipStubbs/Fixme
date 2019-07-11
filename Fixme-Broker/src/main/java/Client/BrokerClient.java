package Client;

import BaseClient.BaseClient;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.*;

import static Instruments.Instruments.*;

public class BrokerClient extends BaseClient {
	Scanner scanner;
	private int numOrders;
	private int numQuotes;
	private ArrayList<ArrayList<String>> marketListing;

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
			logger.logMessage(1,"ID Assigned :"+id);

			messages.clear();
			getServerMessage();
			processMarketListUpdate(messages.get(0));
			messages.clear();
			scanner = new Scanner(System.in);
			try {
				while (true) {
					brokerInstructions();

					String line = scanner.nextLine();
					if (line.equalsIgnoreCase("exit")){
						sendServerMessage("exit");
						this.terminateConnection();
						break;
					}
					else if (line.equalsIgnoreCase("buy")){
						this.buy();
						getServerMessage();
					}
					else if (line.equalsIgnoreCase("sell")){
						this.sell();
						getServerMessage();
					}
					else if (line.equalsIgnoreCase("update")) {
						sendServerMessage("update");
						getServerMessage();
						processMarketListUpdate(messages.get(0));
					} else if (line.equalsIgnoreCase("list")){
						outputMarketListing();
					}

					logger.logMessage(2, "User input was: "+ line);
					TimeUnit.SECONDS.sleep(1);

				}
			} catch(IllegalStateException | NoSuchElementException e) {
				// System.in has been closed
				logger.logMessage(3, getClass().getSimpleName() + "> System.in was closed; exiting");
			}
		}
		catch (ExecutionException | IOException e) {
			e.printStackTrace();
		}
		catch (InterruptedException e) {
			logger.logMessage(3 ,getClass().getSimpleName() + "> Disconnected from the server.");
		} finally {
			try {
				client.close();
			} catch (IOException e){
				// NO OP
			}
		}
	}


	private void processMarketListUpdate(String serialized){
		ArrayList<ArrayList<String>> updatedMarketListing = new ArrayList<ArrayList<String>>();
		int tmpIndex = serialized.indexOf("|") + 1;

		String[] marketListing = serialized.substring(tmpIndex).split("\n");
		String[] markets;

		for (int i = 0; i < marketListing.length; i++){
			markets = marketListing[i].split("_");
			for (int x = 1; x < markets.length; ++x){
				try {
					updatedMarketListing.get(i).add(markets[x]);
				} catch (IndexOutOfBoundsException e){
					// If an error is thrown. We create the new list.
					updatedMarketListing.add(new ArrayList<String>());
					updatedMarketListing.get(i).add(markets[x]);
				}
			}
		}
		this.marketListing = updatedMarketListing;
	}

	private void brokerInstructions(){
		logger.logMessage(1,"---------Please input a command---------\n" +
		"		BUY  - buy an instrument from the markets\n" +
		"		SELL - sell an instrument to the markets\n" +
		"		UPDATE - update your market listing\n" +
		"		LIST - display market listings\n" +
		"		EXIT - close connection to the server\n");
	}

	private void buy() {

		String instrument ="";
		int market = -1;
		String rawMarket = "";
		String quantity = "";
		String price = "";
		int marketIndex = -1;

		logger.logMessage(1,"-------Please fill in the following details--------");
		outputAvailableInstruments();

		logger.logMessage(1,"Enter Instrument You Wish to buy:");
		while (scanner.hasNext()) {
			instrument = scanner.nextLine();
			marketIndex = instrumentToIndex(instrument);
			if (marketIndex > -1 && marketIndex < marketListing.size()) {
				break;
			} else {
				logger.logMessage(3, "Invalid Instrument type: "+instrument);
				outputAvailableInstruments();
			}
		}

		logger.logMessage(1,"Which market would you like to buy from (Please choose their index):");
		outputMarkets(marketIndex);

		while (scanner.hasNext()) {
			rawMarket = scanner.nextLine();
			try {
				market = Integer.parseInt(rawMarket);
				if ( market > -1 && market < marketListing.get(marketIndex).size() ) {
					break;
				} else {
					logger.logMessage(3, "Invalid Market Index: "+rawMarket);
				}
			} catch (NumberFormatException e){
				logger.logMessage(3, "Invalid Market Index: "+rawMarket);
			}
			outputMarkets(marketIndex);

			//TODO Check if market is in list else get another input
		}

		logger.logMessage(1, "How many:");
		while (scanner.hasNext()) {
			quantity = scanner.nextLine();
			break;
			//TODO Check if quantity is a proper value between 0 - 1000000
		}

		logger.logMessage(1,"At what price:");
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

		logger.logMessage(1,"-------Please fill in the following details--------");

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
			break;
			//TODO Check if quantity is a proper value between 0 - 1000000
		}

		logger.logMessage(1, "At what price:");
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


	public void outputMarketListing() {
		for (int i = 0; i < marketListing.size(); ++i) {
			switch (i) {
				case GOLD:
					logger.logMessage(1, "Gold:");
					break;

				case SILVER:
					logger.logMessage(1, "Silver:");
					break;

				case BITCOIN:
					logger.logMessage(1, "Bitcoin:");
					break;

				case RED_SUGAR:
					logger.logMessage(1, "Red Sugar:");
					break;

				case MORKITE:
					logger.logMessage(1, "Morkite:");
					break;

				case APOCA_BLOOM:
					logger.logMessage(1, "Apoca Bloom:");
					break;

			}

			for (int x = 0; x < marketListing.get(i).size(); x++){
				logger.logMessage(1, "	Index:"+ i + "-" + x + " market id:" + marketListing.get(i).get(x));
			}
		}
	}

	public void outputAvailableInstruments() {
		logger.logMessage(1, "Available Instruments are as followed.");
		for (int i = 0; i < marketListing.size(); i++) {
			switch (i) {
				case GOLD:
					logger.logMessage(1, "	Gold");
					break;

				case SILVER:
					logger.logMessage(1, "	Silver");
					break;

				case BITCOIN:
					logger.logMessage(1, "	Bitcoin");
					break;

				case RED_SUGAR:
					logger.logMessage(1, "	Red Sugar");
					break;

				case MORKITE:
					logger.logMessage(1, "	Morkite");
					break;

				case APOCA_BLOOM:
					logger.logMessage(1, "	Apoca Bloom");
					break;
			}
		}
	}

	public void outputMarkets(int marketIndex) {
			for (int x = 0; x < marketListing.get(marketIndex).size(); x++){
				logger.logMessage(1, "	Index:"+ x + " market id:" + marketListing.get(marketIndex).get(x));
			}
	}

	public int instrumentToIndex(String input){
		switch (input.toLowerCase()) {
			case "gold":
				return GOLD;

			case "silver":
				return SILVER;

			case "bitcoin":
				return BITCOIN;

			case "red sugar":
				return RED_SUGAR;

			case "morkite":
				return MORKITE;

			case "apoca bloom":
				return APOCA_BLOOM;

			default:
				return -1;
		}
	}
}
