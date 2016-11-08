import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class CentralServerThread extends Thread {
    Socket clientSocket;
    Socket dataSoc;
    DataInputStream controlIn;
    DataOutputStream controlOut;
    DataInputStream dataIn;
    DataOutputStream dataOut;

    protected static ArrayList<User> userMap = new ArrayList<User>();

    //A HashMap storing a user name as a key and a value pair of that user's
    //current files stored on the server.
    protected static ConcurrentHashMap<String, ArrayList<UserFile>> fileMap = new ConcurrentHashMap<String, ArrayList<UserFile>>();

    public CentralServerThread(Socket controlSoc) {
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
        File f = new File("/home/kolarikm/IdeaProjects/P2P_Network/src/ServerFolder/" + fileName);
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


    public void fileRequest(String fileName, String username) throws Exception {
        String userHoldingRequestedFileIP;
        for(String users: fileMap.keySet()){
            for(UserFile usersFile: fileMap.get(users)){
                if(fileName.equalsIgnoreCase(usersFile.getName()) && users.equalsIgnoreCase(username)){
                    for(User matchedUser: userMap) {
                        if(matchedUser.getUsername().equalsIgnoreCase(username)){
                            userHoldingRequestedFileIP = matchedUser.getClientIP();
                            System.out.println("Found the user requested File On IP: " + userHoldingRequestedFileIP);
                            return;
                        }
                    }
                }
            }
        }
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

    private void search(String fileDescription, String ipAddress) throws Exception {
        HashMap<String, FileSearchDTO> resultSet = new HashMap<String, FileSearchDTO>();
        //loop through every key in the hashMap
        for (String usersFiles : fileMap.keySet()) {
            //Look at all the current Users Files
            for (UserFile files : fileMap.get(usersFiles)) {
                if(files.getDescription().contains(fileDescription)){
                    resultSet.put(usersFiles + files.getName(), new FileSearchDTO());
                    resultSet.get(usersFiles + files.getName()).setFileName(files.getName());
                    resultSet.get(usersFiles + files.getName()).setUserName(usersFiles);
                }
            }
        }
        //Cross the previous results to get the connection speed from the usermap
        for(User user: userMap){
            for(String fileName: resultSet.keySet()){
                if(resultSet.get(fileName).getUserName().equalsIgnoreCase(user.getUsername())){
                    resultSet.get(fileName).setNetworkSpeed(user.getConnSpeed());
                }
            }
        }

        dataSoc = new Socket(ipAddress, 5013);
        controlOut.writeUTF(""+resultSet.keySet().size());
        dataOut = new DataOutputStream(dataSoc.getOutputStream());

        for (String result : resultSet.keySet()) {
            dataOut.writeUTF(resultSet.get(result).toString());
        }

        dataOut.flush();
        controlOut.flush();
        dataSoc.close();

        //This is a Debug Statement for seeing what the result set contains before
        //Returning it to the user
        System.out.println("\t The Result Set Contains");
        for (String usersFiles : resultSet.keySet()) {
            //The user who has the files
            System.out.println("\t User: " + usersFiles);
            System.out.println("\t\t " + resultSet.get(usersFiles).toString());
        }
    }

    public void init() throws Exception {
        String username = controlIn.readUTF();
        String hostname = controlIn.readUTF();
        String connSpeed = controlIn.readUTF();
        String clientIP = controlIn.readUTF();

        ArrayList<UserFile> userFiles = new ArrayList<UserFile>();

        User user = new User(username, hostname, connSpeed, clientIP);

        addUser(user);

        int lines = Integer.valueOf(controlIn.readUTF());

        while (lines > 0) {
            String line[] = controlIn.readUTF().split(":");
            userFiles.add(new UserFile(line[0], line[1]));
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
        System.out.println("\t Current File Table Holds...");
        for (String usersFiles : fileMap.keySet()) {
            //The user who has the files
            System.out.println("\t User: " + usersFiles);
            for (UserFile files : fileMap.get(usersFiles)) {
                System.out.println("\t\t F-Name: " + files.getName() + " F-Desc: " + files.getDescription());
            }
        }
    }

    private void debugUserMap() {
        System.out.println("Users connected");
        for (User u : userMap) {
            System.out.println(u.getUsername());
            System.out.println(u.getClientIP());
        }
    }

    public void run() {
        while (true) {
            try {
                System.out.println("Waiting for command...");
                String command = controlIn.readUTF();
                if (command.equals("INIT")) {
                    System.out.println("\tINIT command received...");
                    init();
                    //Prints out current contents of the File Storage table after ther user is initialized ABOVE.
                    //debugFileTable();
                    debugUserMap();
                //statement for listing the current files that match the description given
                }else if(command.equals("LIST")){
                    System.out.println("\tList command received...");
                    //Yes it's not very good practice to use a temp variable.
                    //However, I'm using it for debugging the functionality is still the same.
                    String fileDescription = controlIn.readUTF();
                    String ipAddress = controlIn.readUTF();
                    System.out.println("\t\t Search for files matching: " +fileDescription);
                    System.out.println("\t\t connect to the node on this IP: "+ipAddress);
                    search(fileDescription, ipAddress);
                } else if(command.equals("QUIT")){
                    System.out.println("Client disconnect command received");
                    break;
                } else if(command.equals("RETR")){
                    String fileName = controlIn.readUTF();
                    String username = controlIn.readUTF();
                    fileRequest(fileName, username);
                }
            } catch (Exception e) {
//                System.out.println("")
            }
        }
    }

}