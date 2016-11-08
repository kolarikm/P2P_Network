/**
 * Created by bens on 10/2/2016.
 */

import org.omg.CORBA.*;

import java.io.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.*;

public class FTPClient {

    Socket clientSocket;
    Socket clientDataSocket;

    int myPort;
    private String myHost;
    private String username;
    private String hostname;
    private String connSpeed;

    /*Variables for Communicating with Central Server*/
    private DataInputStream centralControlIn;
    private DataOutputStream centralControlOut;
    private DataInputStream actualDataIn;
    private DataOutputStream actualDataOut;

    /* Variables for communicating with the FTP Server */
    private DataInputStream ftpControlIn;
    private DataOutputStream ftpControlOut;
    private DataInputStream ftpDataIn;
    private DataOutputStream ftpDataOut;

    private BufferedReader bufferedReader;


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
        centralControlIn = new DataInputStream(clientSocket.getInputStream());
        centralControlOut = new DataOutputStream(clientSocket.getOutputStream());
        uploadClientInfo();
    }


    public String search(String description) throws Exception {
        //used for data connections between the
        ServerSocket dataServerSocket = new ServerSocket(5013);
        centralControlOut.writeUTF("LIST");
        centralControlOut.writeUTF(description);
        centralControlOut.writeUTF(InetAddress.getLocalHost().getHostAddress());

        Socket socket = dataServerSocket.accept();
        actualDataIn = new DataInputStream(socket.getInputStream());
        int size = Integer.valueOf(centralControlIn.readUTF());
        String fileList = "";
        while (size > 0) {
            fileList = fileList + actualDataIn.readUTF() + "\n";
            size--;
        }
        centralControlOut.flush();
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

            centralControlOut.writeUTF("INIT");
            centralControlOut.writeUTF(username);
            centralControlOut.writeUTF(hostname);
            centralControlOut.writeUTF(connSpeed);
            centralControlOut.writeUTF(InetAddress.getLocalHost().getHostAddress());

            //Get number of lines first so we know when to stop reading data
            BufferedReader reader = new BufferedReader(new FileReader("/Users/ben/IdeaProjects/P2P_Network/src/fileList.txt"));
            int lines = 0;
            while (reader.readLine() != null) lines++;
            reader.close();

            centralControlOut.writeUTF("" + lines);

            try (BufferedReader br = new BufferedReader(new FileReader("/Users/ben/IdeaProjects/P2P_Network/src/fileList.txt"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    centralControlOut.writeUTF(line);
                }
            }
            centralControlOut.flush();
        }
    }

    public void retreiveFile(String details) throws Exception {
        String[] command = details.split(" ");
        centralControlOut.writeUTF(command[0]);
        centralControlOut.writeUTF(command[1]);
        centralControlOut.writeUTF(command[2]);

        String ip = centralControlIn.readUTF();
        String result = receiveFile(ip, command[1]);
        // call a function to set up a connection based on this ip
    }

    public String receiveFile(String ip, String filename) {
        //if the ip address is null or the file is null.
        if(ip == null || filename == null){
            return "The file you've requested does not exist";
        }
        try {
            //establish the connection to the remote ftp server.
            Socket socket = new Socket(ip, 5014);
            ftpControlIn = new DataInputStream(socket.getInputStream());
            ftpControlOut = new DataOutputStream(socket.getOutputStream());

            //make a request to the remote server for this file named whatever
            ftpControlOut.writeUTF(filename);

            //get the length of the file
            long fileSize = Long.valueOf(centralControlIn.readUTF());
            FileOutputStream fout = new FileOutputStream(".");
            byte[] bytes = new byte[16 * 1024];

            int count;
            while (fileSize > 0) {
                count = actualDataIn.read(bytes);
                fileSize -= count;
                fout.write(bytes, 0, count);
            }

            //release control of the connection once the file transfer is done.
            fout.close();
            ftpControlIn.close();
            ftpControlOut.flush();
            ftpControlOut.close();

        } catch (Exception e) {
            return "Something went wrong retrieving that file.";
        }
        return "File successfully retrieved. " + "file: " + filename;
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
                centralControlOut.writeUTF("RETR");
                centralControlOut.writeUTF(InetAddress.getLocalHost().getHostAddress());
                Socket socket = dataServerSocket.accept();

                actualDataIn = new DataInputStream(socket.getInputStream());

                centralControlOut.writeUTF(filename);
                long fileSize = Long.valueOf(centralControlIn.readUTF());
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
                centralControlOut.flush();
                return;
            } catch (Exception e) {
                System.out.println("An error occured receiving the file from the server");
            }
        }
    }

    public void disconnect() throws Exception {
        centralControlOut.writeUTF("QUIT");
        centralControlOut.close();
        centralControlIn.close();

    }
}
