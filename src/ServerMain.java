/**
 * @author Jonas
 * ServerMain class, responsible for setting the port the server will be running on
 * Creating an instance of server class
 * Starting server thread
 */
public class ServerMain {
    public static void main(String[] args) {


        //Define port
        int port = 2670;

        //Instance of server class
        Server server = new Server(port);

        //Starts server thread
        server.start();

    }


}
