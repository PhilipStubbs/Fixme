import Client.MarketClient;

public class Market {
    static final int port = 5001;

    public static void main(String[] args) {
        MarketClient client = new MarketClient(port);
        while(true);
    }

}
