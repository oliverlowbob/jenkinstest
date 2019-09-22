

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
