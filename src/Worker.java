/**
 * Created by miloshzelembaba on 2017-11-26.
 */
import java.io.*;
import java.net.Socket;

/**
 This is the thread that handles the actual data transfer between a pair of uploader/downloader
 */
public class Worker implements Runnable{

    private Socket uploaderSocket = null;
    private Socket downloaderSocket = null;
    private int uploaderSize = 0;
    private int downloaderSize = 0;

    public Worker(WorkerInfo w){
        uploaderSocket = w.getUploaderSocket();
        downloaderSocket = w.getDownloaderSocket();
    }

    public void run(){
        if (downloaderSocket == null || uploaderSocket == null) {
            System.out.println("FATAL ERROR: called run without both clients being established");
            return;
        }
        byte[] data = null;

        // tell the uploader to start sending data since the downloader has connected
        try {
//            System.out.println("SERVER: telling the uploader to upload the data");
            String notify = "bloop bleep";
            DataOutputStream outToUploader = new DataOutputStream(uploaderSocket.getOutputStream());
            outToUploader.writeInt(notify.getBytes().length);
            outToUploader.write(notify.getBytes());
        } catch (IOException e){
            System.out.println(e);
        }

        // read in the byes that the uploader sends
        try {
//            System.out.println("SERVER: reading in the uploader bytes");
            DataInputStream inFromUploader = new DataInputStream(uploaderSocket.getInputStream());
            uploaderSize = inFromUploader.readInt();
            if(uploaderSize > 0) {
                data = new byte[uploaderSize];
                inFromUploader.readFully(data, 0, uploaderSize);
            }
        } catch (IOException e){
            System.out.println(e);
        }

        // send the bytes to the downloader
        try {
//            System.out.println("SERVER: sending bytes to the downloader");
            DataOutputStream outToDownloader = new DataOutputStream(downloaderSocket.getOutputStream());
            outToDownloader.writeInt(data.length);
            outToDownloader.write(data);
            downloaderSocket.close();
        } catch (IOException e){
            System.out.println(e);
        }


    }
}
