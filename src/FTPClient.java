/**
 * Created by bens on 10/2/2016.
 */

import java.io.*;
import java.net.*;

public class FTPClient {

    Socket clientSocket;
    Socket clientDataSocket;

    int myPort;
    String myHost;

    String username;
    String hostname;
    String connSpeed;

    DataInputStream controlIn;
    DataOutputStream controlOut;
    DataInputStream actualDataIn;
    DataOutputStream actualDataOut;
    BufferedReader bufferedReader;


    public FTPClient(String serverIP, String serverPort, String username, String hostname, String connSpeed) throws Exception {
        this.myHost = serverIP;
        this.myPort = Integer.valueOf(serverPort);
        this.username = username;
        this.hostname = hostname;
        this.connSpeed = connSpeed;
        controlConnect(serverIP, Integer.valueOf(serverPort));
    }

    public void controlConnect(String host, int port) throws Exception {
        try {
            clientSocket = new Socket(host, port);
        } catch (Exception e) {
            System.out.println("The host you specified was unable to connect");
        }
        controlIn = new DataInputStream(clientSocket.getInputStream());
        controlOut = new DataOutputStream(clientSocket.getOutputStream());
    }

    public String search(String description) throws Exception {
        //used for data connections between the
        ServerSocket dataServerSocket = new ServerSocket(5013);
        controlOut.writeUTF("LIST " + description);
        controlOut.writeUTF(InetAddress.getLocalHost().getHostAddress());
        Socket socket = dataServerSocket.accept();

        actualDataIn = new DataInputStream(socket.getInputStream());

        String descriptions = null;
        int lengthOfMessage = Integer.valueOf(actualDataIn.readUTF());
        while (lengthOfMessage > 0) {
            descriptions += actualDataIn.readUTF() + "\n";

            lengthOfMessage -= 1;
        }
        dataServerSocket.close();
        socket.close();
        actualDataIn.close();

        return descriptions;
    }

    /*
    * Once the client connects to the server it then uploads all of it's current files
    * Connect speed, port, IP address, and hostname to the server itself.
     */
    private void uploadContent() throws Exception {
        if (clientSocket.isBound()) {
            File f = new File(".");
            controlOut.writeUTF("INIT");

            controlOut.writeUTF(username);
            controlOut.writeUTF(hostname);
            controlOut.writeUTF(connSpeed);

            File array[] = f.listFiles();
            controlOut.writeUTF(""+array.length);
            for (File file : array) {
                controlOut.writeUTF(file.getName());
            }

            controlOut.flush();
        }
    }

    public static void main(String args[]) throws Exception {
        return;
    }
}
