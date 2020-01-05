import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

/**
 * @author Jonas
 * webhooktest
 */

public class ChatClient {
    private final int port;
    private final InetAddress ip;
    private Socket socket;
    private OutputStream outputStream;


    /**
     * Constructor
     * @param ip IP adress of serv
     * @param port Port of serv
     */
    public ChatClient(InetAddress ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    /**
     *
     * @param args main method
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        //Message to client
        System.out.println("Please enter IP and port in format: JOIN IP:PORT");
        //Scanner created
        Scanner sc = new Scanner(System.in);
        //Taking input
        String line = sc.nextLine();

        //Using .split method to seperate input by : and " " to get Ip and Port values
        String delimiter1 = " ";
        String delimiter2  = ":";
        line = line.replaceAll(delimiter1, delimiter2);
        String[] connect = line.split(":");
        InetAddress ip = InetAddress.getByName(String.valueOf(connect[1]));
        int port = Integer.parseInt(connect[2]);

        //Connecting with the assigned values from above
        //and running start method.
        ChatClient client = new ChatClient(ip,port);
        client.start();
    }

    /**
     * Start method
     * Reads input from client and sends it to the server
     * @throws IOException
     */
    private void start() throws IOException {
        //If statement to run if connected to server
        if (connect()) {
            Scanner sc = new Scanner(System.in);
            String line;
            //Continuously reads input line and sends to server
            //if quit or logoff, it closes socket.
            while (true) {
                line = sc.nextLine();
                if ("quit".equalsIgnoreCase(line) || "logoff".equalsIgnoreCase(line)) {
                   sendMsg(line);
                    socket.close();
                    break;
                }
                sendMsg(line);
            }
        }
        //Else statement if connection not made, will close outputstream and socket.
        else
            System.err.println("Connection failed!");
        outputStream.close();
        socket.close();
    }

    /**
     * Connect method
     * will establish connection
     * print your client port
     * Create Thread inputlistener
     * @return boolean
     */
    private boolean connect() {
        try {
            this.socket = new Socket(ip, port);
            System.out.println("Client port is " + socket.getLocalPort());
            outputStream = socket.getOutputStream();
            Thread inputListener = new Thread(new ResponseListener(socket));
            inputListener.start();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * sendMsg method, responsible for sending messages
     * also flushes outputstream to force any buffered output bytes to be written out
     * @param line a string input to be sent
     * @throws IOException
     */
    private void sendMsg(String line) throws IOException {
        outputStream.write((line + System.lineSeparator()).getBytes());
        outputStream.flush();
    }
}