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
    protected String username;
 
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
            this.username = userName;
 
            String serverMessage = "New user connected: " + userName;
            server.broadcast(serverMessage, this);
 
            String clientMessage;
 
            do {
                clientMessage = reader.readLine();
                if(clientMessage.startsWith("/")){
                    System.out.println("Command: " + clientMessage.substring(1));
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
            sendSystemMessage("Invalid arguments.");
        }
        switch(args[0]){
            case("help"):
                printHelp();
                break;
            case("create"):
                if(args.length < 2){
                    sendSystemMessage("Insufficient arguments");
                    return;
                }
                server.makeGroup(args[1], this);
                break;
            case("remove"):
                if(args.length < 2){
                    sendSystemMessage("Insufficient arguments");
                    return;
                }
                server.removeGroup(args[1], this);
                break;
            case("join"):
                if(args.length < 2){
                    sendSystemMessage("Insufficient arguments");
                    return;
                }
                server.joinGroup(args[1], this);
                break;
            case("leave"):
                if(args.length < 2){
                    sendSystemMessage("Insufficient arguments");
                    return;
                }
                server.leaveGroup(args[1], this);
                break;
            case("lg"):
                server.listGroups(this);
                break;
            case("gm"):
                //group message
                if(args.length < 3){
                    sendSystemMessage("Insufficient arguments");
                    return;
                }
                String serverMessage = "";
                for(int i = 1; i<args.length; i++){
                    serverMessage.concat(args[i] + " ");
                }
                server.groupMessage(serverMessage, args[1], this);
                break;
            case("lm"):
                //list group members
                if(args.length < 2){
                    sendSystemMessage("Insufficient arguments");
                    return;
                }
                server.listGroupMembers(args[1], this);
                break;
            default:
                sendSystemMessage("Invalid command.");
        }
        return;
    }

    void printHelp(){
        sendSystemMessage("Type /create ___ to create a new group. /join ___ to join an existing group.");
        sendSystemMessage("Type /leave ___ to leave a group. /remove ___ to delete a group.");
        sendSystemMessage("Type /lg to list all groups. /gm ___ followed by a message to send that message to the specified group.");
        sendSystemMessage("Type /lm ___ to list members of a group. /logout to exit.");
    }

    
 
    /**
     * Sends a message to the client.
     */
    void sendMessage(String message) {
        writer.println(message);
    }

    /**
     * Sends a message prepended with the system flag.
     */
    void sendSystemMessage(String message) {
        writer.println("/" + message);
    }

}