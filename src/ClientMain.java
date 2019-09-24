import java.io.IOException;
import java.net.InetAddress;
import java.util.Scanner;

public class ClientMain {
    public static void main(String[] args) throws IOException {

        Scanner sc = new Scanner(System.in);
        String line = sc.nextLine();
        String[] tokens = line.split(":");

        InetAddress ip = InetAddress.getByName(String.valueOf(tokens[0]));

        int port = Integer.parseInt(tokens[1]);

       //ClientWorker clientWorker = new ClientWorker(ip,port);
        ServerWorker serverWorker = new ServerWorker(ip,port);

    }
}
