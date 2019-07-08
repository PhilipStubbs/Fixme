import Client.BrokerClient;

public class Broker {
	static final int port = 5000;

    public static void main(String[] args) {
		BrokerClient client = new BrokerClient(port);

		while(true);
    }

}
