
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    public static void main(String[] args) {
        boolean client = false;
        int port = 5000;
        try  (ServerSocket serverSocket = new ServerSocket(port)){
            System.out.println("Server Created on port:" + port);
            Socket socket = serverSocket.accept();
            System.out.println("Client Connected");
            BufferedReader input = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
            client = true;

            while(client){
                String echoString = input.readLine();
                if (echoString.equals("exit")){
                    client = false;
                }
                output.println("Echo from server " + echoString);
            }

        } catch (IOException e){
            System.out.println("Server Exception "+ e.getLocalizedMessage());
        }
    }

}
