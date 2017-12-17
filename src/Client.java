import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by miloshzelembaba on 2017-11-26.
 */
public class Client {
    private static Socket serverSocket;

    public static void main(String[] args) throws IOException{
        if (args.length != 3 && args.length != 5 && args.length != 6) {
            throw new IOException("invalid number of arguments");
        }
        String command = getCommand(args[2]);

        if (command.equals("F") && args.length == 3){
            terminateServer(args);
        } else if (command.equals("G") && args.length == 5){
            downloadFromServer(args);
        } else if (command.equals("P") && args.length == 6){
            uploadToServer(args);
        } else {
            throw new IOException("invalid use of executables");
        }
    }

    private static String getCommand(String str){
        return str.charAt(0) + "";
    }

    private static void terminateServer(String[] args) throws IOException{
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String command = args[2];
        validateCommand(args[2]);

        if (command.charAt(0) != 'F'){
            System.out.println("If you're trying to terminate, please use F as the command");
        }

        // sends the server the "F" command to terminate it
        serverSocket = new Socket(host,port);
        DataOutputStream dos = new DataOutputStream(serverSocket.getOutputStream());

        dos.writeInt(command.getBytes().length);
        dos.write(command.getBytes());
    }

    private static void downloadFromServer(String[] args) throws IOException{
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String commandAndKey = args[2];
        String filePath = args[3];
        validateCommand(commandAndKey);
        int recvSize = Integer.parseInt(args[4]); // don't need?

        serverSocket = new Socket(host,port);
        // tell the server i'm an downloader
//        System.out.println("DOWNLOADER: telling the server i'm a downloader");
        DataOutputStream dos = new DataOutputStream(serverSocket.getOutputStream());
        dos.writeInt(commandAndKey.getBytes().length);
        dos.write(commandAndKey.getBytes());


        // download from the server
//        System.out.println("DOWNLOADER: download from the server");
        DataInputStream dis = new DataInputStream(serverSocket.getInputStream());
        int downloaderSize = dis.readInt();
        byte[] data = null;
        if(downloaderSize > 0) {
            data = new byte[downloaderSize];
            dis.readFully(data, 0, downloaderSize);
        }

//        System.out.println("DOWNLOADER: write to file");
        FileOutputStream fos = new FileOutputStream(filePath);
        fos.write(data);
        fos.close();
    }

    private static void uploadToServer(String[] args) throws IOException{
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String commandAndKey = args[2];
        String filePath = args[3];
        int sendSize = Integer.parseInt(args[4]); // don't need?
        int waitTime = Integer.parseInt(args[5]); // don't need?

        validateCommand(commandAndKey);

//        System.out.println("UPLOADER: telling the server i'm a uploader");
        serverSocket = new Socket(host,port);
        // tell the server i'm an uploader and here is my code
        DataOutputStream dos = new DataOutputStream(serverSocket.getOutputStream());
        dos.writeInt(commandAndKey.getBytes().length);
        dos.write(commandAndKey.getBytes());

        // wait for the server to be like okay cool send over your data
//        System.out.println("UPLOADER: waiting for server to tell me to upload");
        DataInputStream dis = new DataInputStream(serverSocket.getInputStream());
        int uploaderSize = dis.readInt();
        byte[] data;
        if(uploaderSize > 0) {
            data = new byte[uploaderSize];
            dis.readFully(data, 0, uploaderSize);
        }
        // don't really need to inspect what the sever said, its the fact that i got a response meaning i can continue


        // send the file data to the server
//        System.out.println("UPLOADER: sending bytes to server");
        dos = new DataOutputStream(serverSocket.getOutputStream());
        Path path = Paths.get(filePath);
        byte[] fileBytes = Files.readAllBytes(path);
        dos.writeInt(fileBytes.length);
        dos.write(fileBytes);

        serverSocket.close();
    }

    private static void validateCommand(String cmd) throws IOException{
        if (cmd.charAt(0) != 'F' && cmd.charAt(0) != 'G' && cmd.charAt(0) != 'P'){
            throw new IOException("invalid command prompt");
        }
    }
}
