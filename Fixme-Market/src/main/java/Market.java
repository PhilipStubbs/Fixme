
public class Market {
    static final int port = 5001;

    public static void main(String[] args) {
        Client client = new Client(port);
        System.out.println(client.getClient().isOpen());


        while(true);
    }

}
