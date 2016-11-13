/**
 * Created by bens on 10/2/2016.
 */
import java.net.*;

public class FTPServer {
    public static void main(String args[]) throws Exception {
        ServerSocket controlSocket = new ServerSocket(5014);
        System.out.println("FTP Server Started on Port Number 5014");
        while (true) {
            System.out.println("Waiting for Connection ...");
            FTPServerThread t = new FTPServerThread(controlSocket.accept());
        }
    }
}
