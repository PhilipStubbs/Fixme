package Client;

import javafx.concurrent.Task;

import java.util.Arrays;
import java.util.concurrent.*;

public class UpdateMarketListing extends Thread {
    BrokerClient client;
    public UpdateMarketListing(BrokerClient client){
        this.client = client;
    }

    @Override
    public void run() {
        while (isAlive()) {
            client.getServerMessage();
        }
    }
}
