import java.io.*;
import java.net.*;
import java.util.*;
 
/**
 * The chat server.
 * 
 * @author Stuart Rodenbeck
 */
public class Server {
    private int port;
    private Set<String> userNames = new HashSet<>();
    private Set<ClientThread> ClientThreads = new HashSet<>();
 
    public Server(int port) {
        this.port = port;
    }
 
    public void execute() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
 
            System.out.println("Chat Server is listening on port " + port);
 
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New user connected");
 
                ClientThread newUser = new ClientThread(socket, this);
                ClientThreads.add(newUser);
                newUser.start();
 
            }
 
        } catch (IOException ex) {
            System.out.println("Error in the server: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
 
    public static void main(String[] args) {
        int port;
        if (args.length < 1) {
            Console console = System.console();
            String input = console.readLine("\nEnter the port number for the server: ");
            port = Integer.parseInt(input);

        }else{
            port = Integer.parseInt(args[0]);
        }
        Server server = new Server(port);
        server.execute();
        
    }
 
    /**
     * Delivers a message from one user to others (broadcasting)
     */
    void broadcast(String message, ClientThread excludeUser) {
        for (ClientThread aUser : ClientThreads) {
            if (aUser != excludeUser) {
                aUser.sendMessage(message);
            }
        }
    }
 
    /**
     * Stores username of the newly connected client.
     */
    void addUserName(String userName) {
        userNames.add(userName);
    }
 
    /**
     * When a client is disconneted, removes the associated username and ClientThread
     */
    void removeUser(String userName, ClientThread aUser) {
        boolean removed = userNames.remove(userName);
        if (removed) {
            ClientThreads.remove(aUser);
            System.out.println("The user " + userName + " quit");
        }
    }
 
    Set<String> getUserNames() {
        return this.userNames;
    }
 
    /**
     * Returns true if there are other users connected
     */
    boolean hasUsers() {
        return !this.userNames.isEmpty();
    }
}