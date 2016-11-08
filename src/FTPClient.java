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
        uploadClientInfo();
    }


    public String search(String description) throws Exception {
        //used for data connections between the
        ServerSocket dataServerSocket = new ServerSocket(5013);
        controlOut.writeUTF("LIST");
        controlOut.writeUTF(description);
        controlOut.writeUTF(InetAddress.getLocalHost().getHostAddress());

        Socket socket = dataServerSocket.accept();
        actualDataIn = new DataInputStream(socket.getInputStream());
        int size = Integer.valueOf(controlIn.readUTF());
        String fileList = "";
        while (size > 0) {
            fileList = fileList + actualDataIn.readUTF() + "\n";
            size--;
        }
        controlOut.flush();
        socket.close();
        dataServerSocket.close();
        return fileList;
    }

    /*
    * Once the client connects to the server it then uploads all of it's current files
    * Connect speed, port, IP address, and hostname to the server itself.
    */
    private void uploadClientInfo() throws Exception {
        if (clientSocket.isBound()) {

            controlOut.writeUTF("INIT");
            controlOut.writeUTF(username);
            controlOut.writeUTF(hostname);
            controlOut.writeUTF(connSpeed);
            controlOut.writeUTF(InetAddress.getLocalHost().getHostAddress());

            //Get number of lines first so we know when to stop reading data
            BufferedReader reader = new BufferedReader(new FileReader("/Users/Andromeda/IdeaProjects/P2P_Network/src/fileList.txt"));
            int lines = 0;
            while (reader.readLine() != null) lines++;
            reader.close();

            controlOut.writeUTF(""+lines);

            try (BufferedReader br = new BufferedReader(new FileReader("/Users/Andromeda/IdeaProjects/P2P_Network/src/fileList.txt"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    controlOut.writeUTF(line);
                }
            }
            controlOut.flush();
        }
    }

    private void sendFile() {
        String filename;
        System.out.print("Enter File Name : ");
        try {
            filename = bufferedReader.readLine();
        } catch (Exception e) {
            System.out.println("an error occured while getting the name of the file.");
            return;
        }
        File f = new File("/home/bensonb/IdeaProjects/457_Project1/src/ClientFolder/" + filename);
        if (f.exists()) {
            try {
                ServerSocket dataServerSocket = new ServerSocket(5013);
                controlOut.writeUTF("STOR");
                controlOut.writeUTF(InetAddress.getLocalHost().getHostAddress());
                Socket socket = dataServerSocket.accept();

                actualDataIn = new DataInputStream(socket.getInputStream());
                actualDataOut = new DataOutputStream(socket.getOutputStream());

                controlOut.writeUTF(filename);
                String response = controlIn.readUTF();
                long fileSize = f.length();
                //Yes if the file exists on the server
                if (response.equals("YES")) {
                    System.out.println("That file already exists on the server.");
                    return;
                    //Otherwise no, and transmit the data.
                } else {
                    FileInputStream fileIn = new FileInputStream(f);
                    byte[] bytes = new byte[16 * 1024];
                    controlOut.writeUTF(""+fileSize);
                    int count;
                    while ((count = fileIn.read(bytes)) > 0) {
                        actualDataOut.write(bytes, 0, count);
                    }
                    fileIn.close();
                    dataServerSocket.close();
                    controlOut.flush();
                }
            } catch (Exception e) {
                System.out.println("Something went wrong writing to the control.");
            }
        } else {
            System.out.println("File does not exist: ");
            return;
        }
    }

    public void receiveFile() throws Exception {
        String filename;
        System.out.print("Enter File Name : ");
        try {
            filename = bufferedReader.readLine();
        } catch (Exception e) {
            System.out.println("An error occured while getting the name of the file.");
            return;
        }
        File f = new File("/home/bensonb/IdeaProjects/457_Project1/src/ClientFolder/" + filename);
        if (!f.exists()) {
            try {
                ServerSocket dataServerSocket = new ServerSocket(5013);
                controlOut.writeUTF("RETR");
                controlOut.writeUTF(InetAddress.getLocalHost().getHostAddress());
                Socket socket = dataServerSocket.accept();

                actualDataIn = new DataInputStream(socket.getInputStream());

                controlOut.writeUTF(filename);
                long fileSize = Long.valueOf(controlIn.readUTF());
                FileOutputStream fout = new FileOutputStream(f);
                byte[] bytes = new byte[16 * 1024];

                int count;
                while (fileSize > 0) {
                    count = actualDataIn.read(bytes);
                    fileSize -= count;
                    fout.write(bytes, 0, count);
                }
                fout.close();
                actualDataIn.close();
                dataServerSocket.close();
                controlOut.flush();
                return;
            } catch (Exception e) {
                System.out.println("An error occured receiving the file from the server");
            }
        }
    }

    public void disconnect() throws Exception {
        controlOut.writeUTF("QUIT");
        controlOut.close();
        controlIn.close();

    }

    public static void main(String args[]) throws Exception {
        return;
    }
}
