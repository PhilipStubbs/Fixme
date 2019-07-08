package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class SocketHandlerAsync extends Thread{
	private AsynchronousSocketChannel socket;
	private UUID uuid;
	private List<String> messages;

	public SocketHandlerAsync(AsynchronousSocketChannel socket, List<String> messages){
		this.socket = socket;
		this.uuid = UUID.randomUUID();
		this.messages = messages;
	}

	@Override
	public void run() {
		try {

			while(true){
				if ((socket!= null) && (socket.isOpen())) {

					ByteBuffer buffer = ByteBuffer.allocate(1024);
					Future<Integer> readval = socket.read(buffer);		// readers from client
					readval.get();

//					socket.read(buffer, null,
//					new CompletionHandler<Integer , Object>() {
//						@Override
//						public void completed(Integer result, Object attachment) {
//							if (result < 0) {
//								// handle unexpected connection close
//							}
//							else if (buffer.remaining() > 0) {
//								// repeat the call with the same CompletionHandler
//								socket.read(buffer, null, this);
//							}
//							else {
//								String clientMessage =  new String(buffer.array()).trim();
//								System.out.println("Received from client: "	+clientMessage);
//								messages.add(clientMessage);
//								// got all data, process the buffer
//							}
//						}
//						@Override
//						public void failed(Throwable e, Object attachment) {
//							// handle the failure
//						}
//					});


					String clientMessage =  new String(buffer.array()).trim();
					System.out.println("Received from client: "	+clientMessage);
					// TODO -- get messages out of thread and into RouterAsync.
					messages.add(clientMessage);
					buffer.flip();
					buffer.clear();
				}
			}

		} catch (Exception e){
			System.out.println("Error: " + e.getLocalizedMessage());
		} finally {
			try {
				socket.close();
			} catch (IOException e){
				// NO OP
			}
		}
	}

	public UUID getUuid() {
		return uuid;
	}

	public AsynchronousSocketChannel getSocket() {
		return socket;
	}
}
