import java.io.*;
import java.net.*;
 
/**
 * This thread handles connection for each connected client, so the server
 * can handle multiple clients at the same time.
 *
 * @author Stuart Rodenbeck
 */
public class ClientThread extends Thread {
    private Socket socket;
    private Server server;
    private PrintWriter writer;
 
    public ClientThread(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }
 
    public void run() {
        try {
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
 
            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);
 
            printUsers();
 
            String userName = reader.readLine();
            server.addUserName(userName);
 
            String serverMessage = "New user connected: " + userName;
            server.broadcast(serverMessage, this);
 
            String clientMessage;
 
            do {
                clientMessage = reader.readLine();
                if(clientMessage.startsWith("/")){
                    parseMessage(clientMessage.substring(1));
                }
                else{
                    serverMessage = "[" + userName + "]: " + clientMessage;
                    server.broadcast(serverMessage, this);
                }
                
 
            } while (!clientMessage.equals("/logout"));
 
            server.removeUser(userName, this);
            socket.close();
 
            serverMessage = userName + " has quit.";
            server.broadcast(serverMessage, this);
 
        } catch (IOException ex) {
            System.out.println("Error in ClientThread: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
 
    /**
     * Sends a list of online users to the newly connected user.
     */
    void printUsers() {
        if (server.hasUsers()) {
            writer.println("Connected users: " + server.getUserNames());
        } else {
            writer.println("No other users connected");
        }
    }


    void parseMessage(String message){
        String[] args = message.split("\s");
        if(args.length == 0){
            sendMessage("Invalid arguments.");
        }
        switch(args[0]){
            case("help"):
                //print message list
                break;
            case("create"):
                //new group
                break;
            case("remove"):
                //remove group
                break;
            case("join"):
                //join group
                break;
            case("leave"):
                //leave group
                break;
            case("gm"):
                //group message
                break;
            default:
                sendMessage("Invalid command.");
        }
        return;
    }

    
 
    /**
     * Sends a message to the client.
     */
    void sendMessage(String message) {
        writer.println(message);
    }
}