/**
 * Created by miloshzelembaba on 2017-11-26.
 */

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

/**
 * NOTE: the general structure of having a main thread that always sits on the accept() call and having
 * worker threads branch off from this thread to handle the actual tasks was found at:
 * http://tutorials.jenkov.com/java-multithreaded-servers/multithreaded-server.html
 * Despite that initial inspiration, all the code is my own
 */

public class Server {

    public static void main(String[] args) throws IOException{
        HashMap<String, WorkerInfo> connections = new HashMap<>(); // this will manage info on incomplete connections (i.e both downloader & uploader haven't connected yet)
        ServerSocket tcpSocket = establishTCPConnection(); // the socket clients will communicate through
        while(true){
            // accept any incoming connections
            Socket clientSocket = tcpSocket.accept();

            // read in the connection details (upload/download/terminate)
            DataInputStream inFromClient = new DataInputStream(clientSocket.getInputStream());
            int uploaderSize = inFromClient.readInt();
            byte[] data = null;
            if(uploaderSize > 0) {
                data = new byte[uploaderSize];
                inFromClient.readFully(data, 0, uploaderSize);
            }
            String clientString = new String(data, "UTF-8");
            String key = getKey(clientString); // extract key from what the client sent
            String command = getCommand(clientString); // extract command from the client

            if (command.equals("F")){ // terminate
                break;
            }


            if (connections.containsKey(key)){ // If the compliment has previously connected
                WorkerInfo compliment = connections.get(key); // get the compliment
                compliment.addConnection(clientSocket, command); // add in the details of the missing compliment (the client that just connected)
                connections.remove(key); // remove from incomplete connections

                // create a new thread to execute the data transfer so this thread can accept incoming requests
                new Thread(
                        new Worker(compliment)
                ).start();


            } else { // the first client to connect using that key
                // so we add it to the incomplete connections list
                WorkerInfo tmp = new WorkerInfo();
                tmp.addConnection(clientSocket,command);
                connections.put(key, tmp);
            }
        }
    }

    private static String getKey(String str){
        return str.substring(1);
    }

    private static String getCommand(String str){
        return str.charAt(0) + "";
    }

    // establishes a TCP connection
    private static ServerSocket establishTCPConnection() {
        ServerSocket tcpSocket;

        tcpSocket = createTCPSocket(); // open tcp connection
        String TCPPort = Integer.toString(tcpSocket.getLocalPort()); // get the port and convert to string
        System.out.println("SERVER_TCP_PORT=" + TCPPort);

        try {
            FileWriter fileWriter = new FileWriter("port");
//            String serverAddress = tcpSocket.getInetAddress().getLocalHost().toString();
//            System.out.println("host ip:" +  serverAddress);
            fileWriter.write(TCPPort);
            fileWriter.close();
        } catch (IOException e){
            System.out.println(e);
        }

        return tcpSocket;
    }

    // creates a TCP socket
    private static ServerSocket createTCPSocket(){
        int n_port;
        ServerSocket socket;

        while (true){
            n_port = 0;
            socket = tryTCPOnPort(n_port);

            if (socket != null){
                break;
            }
        }

        return socket;
    }

    // trys opening on a port specified by 'port'
    private static ServerSocket tryTCPOnPort(int port){
        ServerSocket socket;
        try {
            socket = new ServerSocket(port);
            return socket;
        } catch (IOException e) {
            System.out.println(e);
        }

        return null;
    }
}
