import java.net.*;
import java.io.*;
 
/**
 * The chat client program.
 * Type '/logout' to terminte the program.
 *
 * @author Stuart Rodenbeck
 */
public class Client {
    private String hostname;
    private int port;
    private String userName;
 
    public Client(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }
 
    public void execute() {
        try {
            Socket socket = new Socket(hostname, port);
 
            System.out.println("Connected to the chat server");
 
            new ReadThread(socket, this).start();
            new WriteThread(socket, this).start();
 
        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O Error: " + ex.getMessage());
        }
 
    }

    void setUserName(String userName) {
        this.userName = userName;
    }
 
    String getUserName() {
        return this.userName;
    }

 
    public static void main(String[] args) {
        String hostname;
        int port;
        if (args.length < 2) {
            Console console = System.console();
            hostname = console.readLine("\nEnter the IP address of the destination server: ");
            String temp = console.readLine("\nEnter the port number to connect on: ");
            port = Integer.parseInt(temp);
        }
        else{
            hostname = args[0];
            port = Integer.parseInt(args[1]);
        }
        Client client = new Client(hostname, port);
        client.execute();
    }
}