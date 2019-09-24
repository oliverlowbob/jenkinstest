import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ClientWorker
{
    public ClientWorker(InetAddress ip, int port) throws IOException {

        Scanner scn = new Scanner(System.in);

        // establish the connection
        Socket s = new Socket(ip, port);

        // obtaining input and out streams
        DataInputStream dis = new DataInputStream(s.getInputStream());
        DataOutputStream dos = new DataOutputStream(s.getOutputStream());

    }

}