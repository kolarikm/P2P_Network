import java.io.*;
import java.net.*;
import java.net.ServerSocket;
import java.net.Socket;

class ClientHelper {
    Socket clientSocket;
    Socket clientDataSocket;

    int myPort;
    String myHost;

    DataInputStream controlIn;
    DataOutputStream controlOut;
    DataInputStream actualDataIn;
    DataOutputStream actualDataOut;
    BufferedReader bufferedReader;

    public ClientHelper() {
        bufferedReader = new BufferedReader(new InputStreamReader(System.in));
    }

    public int getMyPort() {
        return myPort;
    }

    public String getMyHost() {
        return myHost;
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

    public void controlConnect (String host,int port) throws Exception {
        try {
            clientSocket = new Socket(host, port);
        } catch (Exception e) {
            System.out.println("The host you specified was unable to connect");
        }

        controlIn = new DataInputStream(clientSocket.getInputStream());
        controlOut = new DataOutputStream(clientSocket.getOutputStream());
    }

    private void listFiles () throws Exception {
        ServerSocket dataServerSocket = new ServerSocket(5013);
        controlOut.writeUTF("LIST");
        controlOut.writeUTF(InetAddress.getLocalHost().getHostAddress());
        Socket socket = dataServerSocket.accept();

        actualDataIn = new DataInputStream(socket.getInputStream());

        String files = null;
        int lengthOfMessage = Integer.valueOf(actualDataIn.readUTF());
        while (lengthOfMessage > 0) {
            files = actualDataIn.readUTF();
            System.out.print(files);
            lengthOfMessage -= 1;
        }
        dataServerSocket.close();
        socket.close();
        actualDataIn.close();
    }


    public void displayMenu () throws Exception {
        while (true) {
            System.out.println("\nPlease choose a command:\n");
            System.out.println("CONNECT <server name/IP address> <server port>");
            System.out.println("LIST");
            System.out.println("RETR <filename>");
            System.out.println("STOR <filename>");
            System.out.println("QUIT\n");

            String[] selection = bufferedReader.readLine().split("\\s");

            switch (selection[0]) {
                case "CONNECT":
                    myHost = selection[1];
                    myPort = Integer.valueOf(selection[2]);
                    controlConnect(myHost, myPort);
                    break;
                case "LIST": //list()
                    try {
                        listFiles();
                    } catch (Exception e) {
                        System.out.println("An error occurred while listing the files on the server");
                    }
                    break;
                case "RETR": //retr();
                    receiveFile();
                    break;
                case "STOR":
                    sendFile();
                    break;
                case "QUIT": //quit();
                    controlOut.writeUTF("QUIT");
                    controlIn.close();
                    controlOut.close();
                    actualDataIn.close();
                    break;
                default: //invalid selection
                    break;
            }
        }
    }
}
