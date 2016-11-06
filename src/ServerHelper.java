import java.io.*;
import java.lang.reflect.Array;
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

    //A HashMap storing a user name as a key and a value pair of that user's
    //current files stored on the server.
    protected static HashMap<String, ArrayList<UserFile>> fileMap = new HashMap<String, ArrayList<UserFile>>();

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
                byte[] bytes = new byte[16 * 1024];

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

    public void sendFile(String myIp) throws Exception {
        dataSoc = new Socket(myIp, 5013);
        dataOut = new DataOutputStream(dataSoc.getOutputStream());

        String fileName = controlIn.readUTF();
        File f = new File("/home/bensonb/IdeaProjects/457_Project1/src/ServerFolder/" + fileName);

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

    protected static void addFiles(String username, ArrayList<UserFile> userFiles) {
        synchronized (fileMap) {
            fileMap.put(username, userFiles);
        }
    }

    public void init() throws Exception {
        String username = controlIn.readUTF();
        String hostname = controlIn.readUTF();
        String connSpeed = controlIn.readUTF();

        ArrayList<UserFile> userFiles = new ArrayList<UserFile>();

        User user = new User(username, hostname, connSpeed);

        addUser(user);

        int lines = Integer.valueOf(controlIn.readUTF());

        String name;
        String desc;

        while (lines > 0) {
            String line[] = controlIn.readUTF().split(":");
            name = line[0];
            desc = line[1];
            UserFile file = new UserFile(name, desc);
            userFiles.add(file);
            lines--;
        }

        addFiles(username, userFiles);
    }

    private void debugFileTable() {
        /*
         * Debugging Loop for Seeing what files were uploaded to a server
         * When the client connected to this server class. And used for
         * determining if the files were synced across threads.
        */
        for (String usersFiles : fileMap.keySet()) {
            System.out.println("\t Current File Table Holds...");
            //The user who has the files
            System.out.println("\t User: " + usersFiles);
            for (UserFile files : fileMap.get(usersFiles)) {
                System.out.println("\t\t F-Name: " + files.getName() + " F-Desc: " + files.getDescription());
            }
        }
    }

    public void run() {
        while (true) {
            try {
                System.out.println("Waiting for command...");
                String command = controlIn.readUTF();
                if (command.equals("INIT")) {
                    System.out.println("\tINIT client received");

                    init();
                    debugFileTable();
                }
            } catch (Exception e) {

            }
        }
    }
}