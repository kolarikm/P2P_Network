import java.io.*;
import java.net.*;

public class FTPServerThread extends Thread {
    Socket dataSoc;
    Socket clientSocket;


    DataInputStream controlIn;
    DataOutputStream controlOut;

    DataInputStream dataIn;
    DataOutputStream dataOut;

    public FTPServerThread(Socket controlSoc) {
        try {
            clientSocket = controlSoc;
            controlIn = new DataInputStream(clientSocket.getInputStream());
            controlOut = new DataOutputStream(clientSocket.getOutputStream());
            System.out.println("FTP Client Connected ...");
            start();

        } catch (Exception ex) {
            System.out.println("Could not establish control server connection.");
        }
    }

    /*
    * Receives a file from the client.
    * Receives through the data stream object the filename the client wishes to send over.
     */
    public void sendFile(String myIp) throws Exception {
        dataSoc = new Socket(myIp, 5015);
        dataOut = new DataOutputStream(dataSoc.getOutputStream());

        String fileName = controlIn.readUTF();
        File f = new File("C:\\Users\\bens\\IdeaProjects\\P2P_Network\\src\\"+fileName);

        if (f.exists()) {
            FileInputStream fileIn = new FileInputStream(f);
            byte[] bytes = new byte[16 * 1024];
            long fileSize = f.length();
            int count;
            controlOut.writeUTF(""+fileSize);
            while ((count = fileIn.read(bytes)) > 0) {
                dataOut.write(bytes, 0, count);
            }
            dataSoc.close();
            dataOut.flush();
            controlOut.flush();
            fileIn.close();
        }
        return;
    }

    public void run() {
        while (true) {
            try {
                System.out.println("Waiting for Command ...");
                String Command = controlIn.readUTF();
                if (Command.compareTo("STOR") == 0) {
                    System.out.println("\tGET Command Received ...");
                    continue;
                } else if (Command.compareTo("RETR") == 0) {
                    System.out.println("\tRetreive Command Received ...");
                    sendFile(controlIn.readUTF());
                    break;
                } else if (Command.compareTo("QUIT") == 0) {
                    System.out.println("\tDisconnect Command Received ...");
                    break;
                }
            } catch (Exception ex) {

            }
        }
        return;
    }
}