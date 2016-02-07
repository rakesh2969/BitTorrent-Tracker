import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Rakesh on 11/13/2015.
 */

/*
 Create a tracker class which listens to peers using sockets
 and accepts an request from peers and send out details of all the peers who have the particular file
*/
public class Tracker {
    private static ServerSocket serverSocket;
    private static int PORT = 8090;

    public static void main(String[] args) throws IOException {
        //create a server socket which listens to clients get requests
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Tracker is waiting to serve peers on :" + PORT);
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("Peer connection accepted");
            Thread subThread = new Thread(new TrackerService(socket));
            subThread.start();
        }
    }
}