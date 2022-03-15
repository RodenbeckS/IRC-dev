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
                printHelp();
                break;
            case("create"):
                server.makeGroup(args[0], this);
                break;
            case("remove"):
                server.removeGroup(args[0], this);
                break;
            case("join"):
                server.joinGroup(args[0], this);
                break;
            case("leave"):
                server.leaveGroup(args[0], this);
                break;
            case("lg"):
                server.listGroups(this);
                break;
            case("gm"):
                //group message
                if(args.length < 2){
                    sendMessage("Insufficient arguments");
                    return;
                }
                String serverMessage = "";
                for(int i = 1; i<args.length; i++){
                    serverMessage.concat(args[i] + " ");
                }
                server.groupMessage(serverMessage, args[0], this);
                break;
            default:
                sendMessage("Invalid command.");
        }
        return;
    }

    void printHelp(){
        sendMessage("/create ___ create a new group. /join ___ to join an existing group.");
        sendMessage("/leave ___ to leave a group. /remove ___ to delete a group.");
        sendMessage("/lg to list all groups. /gm ___ followed by a message to send that message to the specified group.");
        sendMessage("/logout to exit.");
    }

    
 
    /**
     * Sends a message to the client.
     */
    void sendMessage(String message) {
        writer.println(message);
    }
}