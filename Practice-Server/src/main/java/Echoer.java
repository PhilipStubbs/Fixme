import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Echoer extends Thread {
	private Socket socket;

	public Echoer(Socket socket){
		this.socket = socket;
	}

	@Override
	public void run() {
		try {
			BufferedReader input = new BufferedReader(
			new InputStreamReader(socket.getInputStream()));

			while(true){
				String echoString = input.readLine();
				PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

				if (echoString.equals("exit")){
					break;
				}
				output.println("server says:" +echoString);
			}

		} catch (IOException e){
			System.out.println("Error: " + e.getLocalizedMessage());
		} finally {
			try {
				socket.close();
			} catch (IOException e){
				// NO OP
			}
		}
	}
}
