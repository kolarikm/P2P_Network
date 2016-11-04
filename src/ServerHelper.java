import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.ArrayList;

public class ServerHelper extends Thread {
    Socket clientSocket;
    Socket dataSoc;
    DataInputStream controlIn;
    DataOutputStream controlOut;
    DataInputStream dataIn;
    DataOutputStream dataOut;

    protected static ArrayList<User> userMap = new ArrayList<User>();
    protected static HashMap<String, ArrayList<String>> fileMap = new HashMap<String, ArrayList<String>>();

    public ServerHelper(Socket controlSoc) {
        try {
            clientSocket = controlSoc;

            controlIn = new DataInputStream(clientSocket.getInputStream());
            controlOut = new DataOutputStream(clientSocket.getOutputStream());

            System.out.println("Client Connected ...");
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
            f = new File("./");
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
                while ((count = dataIn.read(bytes)) > 0) {
                    fout.write(bytes, 0, count);
                }
                fout.close();
//                controlOut.writeUTF("File Send Successfully");
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

//    public void sendFile() throws Exception {
//        String filename = controlIn.readUTF();
//        File f = new File(filename);
//        if (!f.exists()) {
//            controlOut.writeUTF("File Not Found");
//            return;
//        } else {
//            controlOut.writeUTF("READY");
//            FileInputStream fin = new FileInputStream(f);
//            int ch;
//            do {
//                ch = fin.read();
//                controlOut.writeUTF(String.valueOf(ch));
//            }
//            while (ch != -1);
//            fin.close();
//            controlOut.writeUTF("File Receive Successfully");
//        }
//    }

    public void sendFile(String myIp) throws Exception {
        dataSoc = new Socket(myIp, 5013);
        dataOut = new DataOutputStream(dataSoc.getOutputStream());

        String fileName = controlIn.readUTF();
        File f = new File("/home/bensonb/IdeaProjects/457_Project1/src/ServerFolder/"+fileName);

        if (f.exists()) {
            FileInputStream fileIn = new FileInputStream(f);
            byte[] bytes = new byte[16 * 1024];

            int count;
            while ((count = fileIn.read(bytes)) > 0) {
                dataOut.write(bytes, 0, count);
            }
            fileIn.close();
        }
        return;
    }

    protected static void addUser(User user) {
        synchronized (userMap) {
            userMap.add(user);
        }
    }

    protected static void addFiles(String username, ArrayList<String> userFiles) {
        synchronized (fileMap) {
            fileMap.put(username, userFiles);
        }
    }

    public void init() throws Exception {
        String username = controlIn.readUTF();
        String hostname = controlIn.readUTF();
        String connSpeed = controlIn.readUTF();

        ArrayList<String> userFiles = new ArrayList<String>();

        User user = new User(username, hostname, connSpeed);

        addUser(user);

        int length = Integer.valueOf(controlIn.readUTF());

        while(length > 0) {
            userFiles.add(controlIn.readUTF());
            length--;
        }

        addFiles(username, userFiles);
    }

    public void run() {
        while(true) {
            try {
                System.out.println("Waiting for command...");
                String command = controlIn.readUTF();
                if (command.equals("INIT")) {
                    System.out.println("INIT client received");
                    init();
                }
            } catch (Exception e) {

            }
        }
    }
}