import java.io.*;
import java.net.*;

public class FTPServerThread extends Thread {
    Socket clientSocket;
    Socket dataSoc;
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

    public void listFiles(String myIP) throws Exception {
        dataSoc = new Socket(myIP, 5013);
        dataIn = new DataInputStream(dataSoc.getInputStream());
        dataOut = new DataOutputStream(dataSoc.getOutputStream());
        File f = null;
        String[] currentFilesInDirectory = null;
        try {
            f = new File("/home/bensonb/IdeaProjects/457_Project1/src/ServerFolder/");
            currentFilesInDirectory = f.list();
            dataOut.writeUTF("" + currentFilesInDirectory.length);
            for (String fileName : currentFilesInDirectory) {
                dataOut.writeUTF(fileName + "\n");
            }
            dataOut.flush();
            dataSoc.close();
        } catch (Exception e) {
            System.out.println("An error occured on the server while looking for the specified file.");
        }
    }

    /*
    * Receives a file from the client.
    * Receives through the data stream object the filename the client wishes to send over.
    *
     */
    public void receiveFile(String myIp) throws Exception {
        dataSoc = new Socket(myIp, 5013);
        dataIn = new DataInputStream(dataSoc.getInputStream());
        String fileName = null;
        try {
            fileName = controlIn.readUTF();
        } catch (Exception e) {
            System.out.println("Error reading the file name from client.");
        }
        //it was this little slash right here that fucked us up ............... here. under me.
        File f = new File("/home/bensonb/IdeaProjects/457_Project1/src/ServerFolder/" + fileName);
        if (!f.exists()) {
            try {
                //file does not exist send no back
                controlOut.writeUTF("NO");
                FileOutputStream fout = new FileOutputStream(f);
                byte[] bytes = new byte[16*1024];

                int count;
                long fileSize = Long.valueOf(controlIn.readUTF());
                while (fileSize > 0) {
                    count = dataIn.read(bytes);
                    fout.write(bytes, 0, count);
                    fileSize -= count;
                }
                dataIn.close();
                fout.close();
                dataSoc.close();
                return;
            } catch (Exception e) {
                System.out.println("Cannot communicate with client.");
            }
        } else {
            controlOut.writeUTF("YES");
            dataSoc.close();
            return;
        }
    }

    public void sendFile(String myIp) throws Exception {
        dataSoc = new Socket(myIp, 5013);
        dataOut = new DataOutputStream(dataSoc.getOutputStream());

        String fileName = controlIn.readUTF();
        File f = new File("/home/bensonb/IdeaProjects/457_Project1/src/ServerFolder/"+fileName);

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
                    receiveFile(controlIn.readUTF());
                    continue;
                } else if (Command.equals("LIST")) {
                    System.out.println("\tLIST Command Received ...");
                    listFiles(controlIn.readUTF());
                    continue;
                } else if (Command.compareTo("RETR") == 0) {
                    System.out.println("\tRetreive Command Received ...");
                    sendFile(controlIn.readUTF());
                    continue;
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