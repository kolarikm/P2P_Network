/**
 * Created by bens on 10/2/2016.
 */
import java.net.*;

public class FTPServer {
    public static void main(String args[]) throws Exception {
        ServerSocket controlSocket = new ServerSocket(5012);
        System.out.println("FTP Server Started on Port Number 5012");
        while (true) {
            System.out.println("Waiting for Connection ...");
            ServerHelper c = new ServerHelper(controlSocket.accept());
        }
    }
}
