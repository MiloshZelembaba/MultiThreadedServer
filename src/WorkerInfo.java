import java.net.Socket;

/**
 * Created by miloshzelembaba on 2017-11-27.
 * This class is used to store the socket information of a pair of clients (uploader,downloader)
 */

public class WorkerInfo {
    Socket uploaderSocket = null;
    Socket downloaderSocket = null;

    // adds a client's details to the object
    public void addConnection(Socket clientSocket, String job){
        if (job.equals("P")){
            if (uploaderSocket == null){
                uploaderSocket = clientSocket;
            } else {
                System.out.println("FATAL ERROR: two uploaders with the same code");
            }
        } else if (job.equals("G")){
            if (downloaderSocket == null){
                downloaderSocket = clientSocket;
            } else {
                System.out.println("FATAL ERROR: two downloaders with the same code");
            }
        } else {
            System.out.println("Incorrect job type, needs to be P or G");
        }
    }

    public Socket getUploaderSocket(){
        return uploaderSocket;
    }

    public Socket getDownloaderSocket(){
        return downloaderSocket;
    }
}
