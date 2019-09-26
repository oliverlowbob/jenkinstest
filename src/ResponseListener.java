import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

/**
 * @author Jonas
 */
public class ResponseListener implements Runnable {
    private Socket socket;

    /**
     * Responselistener constructor
     * @param socket taking a socket as param
     */
    public ResponseListener(Socket socket) {
        this.socket = socket;
    }

    /**
     * Run method with scanner, socket, inputstream
     * Reading as long as socket is not closed.
     */
    @Override
    public void run() {
        Scanner sc;
        try {
            sc = new Scanner(socket.getInputStream());
                while (!socket.isClosed() && sc.hasNextLine())
                {
                System.out.println(sc.nextLine());
                }
            }
        catch (IOException e)
            {
            e.printStackTrace();
            }

                    }

}