import java.io.*;
import java.net.Socket;
import java.util.List;

public class ServerWorker extends Thread {

    private final Socket clientSocket;
    private final Server server;
    private String userName = null;
    private OutputStream outputStream;

    public ServerWorker(Server server, Socket clientSocket)
    {
        this.server = server;
        this.clientSocket = clientSocket;
    }

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
                //First token always going to be our command
                String cmd = tokens[0];
                //If message recieved is quit or logoff, break and will close connection.
                if ("logoff".equalsIgnoreCase(cmd) || "quit".equalsIgnoreCase(cmd)) {
                    handleLogOff();
                    break;
                    //if message recieved is login
                } else if ("login".equalsIgnoreCase(cmd))
                {
                    //Call handle login method
                    handleLogin(outputStream, tokens);
                    
                } else if ("msg".equalsIgnoreCase(cmd))
                {
                    //To make sure last token i.e. the message to be sent
                    //doesn't get split
                    String[] tokensMsg = line.split(" ", 3);
                    //Handles chatmessages between clients
                    handleMessage(tokensMsg);
                }
                else{
                    //If command not known/supported
                    String msg = "unknown command: " + cmd + "\n";
                    outputStream.write(msg.getBytes());
                }
            }
        }

        //Closing socket
        clientSocket.close();

    }

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
                String outMsg = userName + " : " + body + "\n";
                worker.send(outMsg);
            }
        }
    }

    private void handleLogOff() throws IOException {
        //Remove user from worker list when logoff
        server.removeWorker(this);
        //Access to workerlist.
        List<ServerWorker> workerList = server.getWorkerList();
        //Message to be sent
        String onlineMsg = "User: " + userName + " is offline!\n";
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
    public String getUserName(){
        return userName;
    }

    private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException {
        if (tokens.length == 2 )
        {
            //Username is going to be the 2nd token, hence [1]
            String userName = tokens[1];

            if (userName.equals("guest") || (userName.equals("jim")))
            {
                String msg = "Ok, logged in\n";
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
                                String msg2 = "online " + worker.getUserName() + "\n";
                                worker.send(msg2);
                            }

                    }
                }

                //Send other online users current user's status
                String onlineMsg = "User: " + userName + " is online!\n";
                for (ServerWorker worker: workerList)
                {
                    if (!userName.equals(worker.getUserName()))
                    {
                        //Message to be sent if a user is online
                        worker.send(onlineMsg);
                    }
                }
            } else {
                String msg = "Error on login\n";
                outputStream.write(msg.getBytes());
            }
        }

    }

    //Send message to access outputstream of current clientsocket
    //Send message to user.
    private void send(String Msg) throws IOException {
        if (userName!=null) {
            outputStream.write(Msg.getBytes());
        }
    }
}
