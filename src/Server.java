import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 */
public class Server extends Thread{


    private final int serverPort;

    //List of server workers
    private ArrayList<ServerWorker> workerList = new ArrayList<>();

    /**
     * Server constructor
     * @param serverPort has a port as param.
     */
    public Server(int serverPort) {
        this.serverPort = serverPort;
    }

    //For workers to be able to access all other server workers
    public List<ServerWorker> getWorkerList()
    {
        return workerList;
    }

    /**
     * Run method to continuously accept connections from client
     * Main thread running this process
     *
     */
    @Override
    public void run() {
        //Create server socket
        try (ServerSocket serverSocket = new ServerSocket(serverPort))
        {
            //In loop to continuously accept connections from client
            //main thread doing this
            while (true)
            {
                System.out.println("Waiting for client(s) to connect..");
                //Socket represents connection to client
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket);
                //Message to write to client when connected via outputstream
                OutputStream outputStream = clientSocket.getOutputStream();
                outputStream.write(("You have connected. Please write: login <username> to login!\n" + "Write <help> for list of additional commands!\n").getBytes());
                //Server worker, handles communication with client socket
                ServerWorker worker = new ServerWorker(this, clientSocket);
                //Adding worker to workerlist
                workerList.add(worker);
                //Starting thread worker
                worker.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    /**
     * Method responsible for removing clients when logging off.
     * @param serverWorker serverWorker as param, to be removed when a logoff occurs.
     */
    //Remove worker from list, used when logoff
    public void removeWorker(ServerWorker serverWorker) {
        workerList.remove(serverWorker);
    }
}
