import java.io.*;
import java.net.Socket;
import java.util.List;

/**
 * @author Jonas
 */

public class ServerWorker extends Thread {

    private final Socket clientSocket;
    private final Server server;
    private String userName = null;
    private OutputStream outputStream;

    /**
     * Constructor
     * @param server server object
     * @param clientSocket the client socket
     */
    public ServerWorker(Server server, Socket clientSocket)
    {
        this.server = server;
        this.clientSocket = clientSocket;
    }

    /**
     * Run method which will call the handleClientSocket method
     */
    //Run method for ServerWorker thread
    @Override
    public void run() {
        try {
            //Runs handleClientSocket
            handleClientSocket();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * Method for handling client socket
     * Access to date from client in- and output
     * Responsible for reading lines and executing methods based on these lines/commands
     * such as logoff, users, msg, ect.
     * @throws IOException
     * @throws InterruptedException
     */
    private void handleClientSocket() throws IOException, InterruptedException {
        //Access to data from client input
        InputStream inputStream = clientSocket.getInputStream();
        //Access to data from client output
        this.outputStream = clientSocket.getOutputStream();

        //Buffered reader to read lines
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        //String to keep input
        String line;
        //While loop to read from client
        while ((line = reader.readLine()) != null)
        {
            //Will split line into different tokens
            String[] tokens = line.split(" ");

            //Avoid nullpointer exceptions
            if(tokens != null && tokens.length > 0) {
                //First token always going to be the command
                String cmd = tokens[0];
                //If message recieved is quit or logoff, break and will close connection.
                if ("logoff".equalsIgnoreCase(cmd) || "quit".equalsIgnoreCase(cmd)) {
                    handleLogOff();
                    break;
                    //if message recieved is login
                } else if ("login".equalsIgnoreCase(cmd))
                {
                    //Call handle login method
                    handleLogin(outputStream,tokens);
                    
                } else if ("DATA".equalsIgnoreCase(cmd))
                {
                    //To make sure last token i.e. the message to be sent
                    //doesn't get split
                    String delimiter1 = ":";
                    String delimiter2  = " ";
                    line = line.replaceAll(delimiter1, delimiter2);
                    String[] tokensMsg = line.split(" ", 3);
                    //Handles chatmessages between clients
                    handleMessage(tokensMsg);
                } else if ("list".equalsIgnoreCase(cmd))
                {
                    showOnlineList();
                } else if ("help".equalsIgnoreCase(cmd))
                {
                  printHelpCommands();
                }
                else{
                    //If command not known/supported
                    String msg = "unknown command: " + cmd + "\r\n";
                    outputStream.write(msg.getBytes());
                }
            }
        }

        //Closing socket
        clientSocket.close();

    }

    /**
     * Method to print string to client/user with instructions
     * @throws IOException
     */
    private void printHelpCommands() throws IOException {
        String msg =
                "To message others type: data <user> : message \n" +
                "<List> will show who is online\n" +
                "<Quit> or <Logoff> will sign you out!\n";
        outputStream.write(msg.getBytes());
    }

    /**
     * Method to show who is online when user logs in
     * @throws IOException
     */
    private void showOnlineList() throws IOException {
        List<ServerWorker> workerList = server.getWorkerList();
        for (ServerWorker worker : workerList)
        {
            send("user: " + worker.getUserName() + " is online!\r\n");

        }

    }

    /**
     * Handles messages
     * breaks messages up to get recieving user and the messagebody
     * then calls send method to send the message to correct reciever
     * @param tokens
     * @throws IOException
     */
    //For handling chat messages between clients
    //Format: "msg" "login" message..
    private void handleMessage(String[] tokens) throws IOException {
        String sendTo = tokens[1];
        String body   = tokens[2];
        //Get the list of workers
        List<ServerWorker> workerList = server.getWorkerList();
        //Iterate through the workers
        //See if username matches sendTo username
        for (ServerWorker worker : workerList)
        {
            if (sendTo.equalsIgnoreCase(worker.userName))
            {
                //If username matches, send message to username/client
                String outMsg = userName + " : " + body + "\r\n";
                worker.send(outMsg);
            }
        }
    }

    /**
     * Handles logoff
     * called when user writes quit or logoff
     * Will call method removeWorker to remove user when logged off
     * closing socket when logoff occurs
     * @throws IOException
     */
    private void handleLogOff() throws IOException {
        //Remove user from worker list when logoff
        server.removeWorker(this);
        //Access to workerlist.
        List<ServerWorker> workerList = server.getWorkerList();
        //Message to be sent
        String onlineMsg = "User: " + userName + " is offline!\r\n";
        //Going through worker in workerList
        for (ServerWorker worker: workerList)
        {
            if (!userName.equals(worker.getUserName()))
            {
                //Message to be sent if a user is offline
                worker.send(onlineMsg);
            }
        }
        //Closing socket if logged off.
        clientSocket.close();
    }

    //Get username

    /**
     * Getter for username
     * @return userName
     */
    public String getUserName(){
        return userName;
    }

    /**
     * Handles login
     * Will send online user notifications
     * @param outputStream to write message to client
     * @param tokens to get the username from client
     * @throws IOException
     */
    private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException {
        if (tokens.length == 2 )
        {
            //Username is going to be the 2nd token, hence [1]
            //Username is chosen by user by typing: login <username>
            String userName = tokens[1];

            //Assuring username is less or max 12 chars
            if (userName.length() <= 12 && !equals(getUserName()))
            {
                String msg = "Ok, logged in\r\n";
                outputStream.write(msg.getBytes());
                this.userName = userName;
                System.out.println("User logged in as: " + userName);


                List<ServerWorker> workerList = server.getWorkerList();
                //Send current usernames of all other users online
                for (ServerWorker worker: workerList)
                {
                    //Avoid sending message to yourself.
                    if (!userName.equals(worker.getUserName()))
                    {
                            if (userName.equals(worker.getUserName()))
                            {
                                String msg2 = "online " + worker.getUserName() + "\r\n";
                                worker.send(msg2);
                            }

                    }
                }

                //Send other online users current user's status
                String onlineMsg = "User: " + userName + " is online!\r\n";
                for (ServerWorker worker: workerList)
                {
                    if (!userName.equals(worker.getUserName()))
                    {
                        //Message to be sent if a user is online
                        worker.send(onlineMsg);
                    }
                }
            } else {
                String msg = "Error on login(Remember max 12 char!)\r\n";
                outputStream.write(msg.getBytes());
                //Server error code print if login is not successful by a client
                System.err.println("Login not successful for " + userName);
            }
        }

    }

    /**
     * Send method responsible for writing messages on outputstream
     * @param msg takes a String Msg that is to be sent
     * @throws IOException
     */
    //Send message to access outputstream of current clientsocket
    //Send message to user.
    private void send(String msg) throws IOException
    {
        if (userName!=null)
        {
            outputStream.write(msg.getBytes());
        }
    }
}
