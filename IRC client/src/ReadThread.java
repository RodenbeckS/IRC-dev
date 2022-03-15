import java.io.*;
import java.net.*;
 
/**
 * This thread is responsible for reading server's input and printing it
 * to the console.
 * It runs in an infinite loop until the client disconnects from the server.
 *
 * @author Stuart Rodenbeck
 */
public class ReadThread extends Thread {
    private BufferedReader reader;
    private Socket socket;
    private Client client;
    protected boolean quit;
 
    public ReadThread(Socket socket, Client client) {
        this.socket = socket;
        this.client = client;
        this.quit = false;
 
        try {
            InputStream input = this.socket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(input));
        } catch (IOException ex) {
            System.out.println("Error getting input stream: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
 
    public void run() {
        try {
            String response = reader.readLine();
            System.out.println("\n" + response);
        } catch (IOException ex) {
            System.out.println("Error reading from server: " + ex.getMessage());
            ex.printStackTrace();
        }
        while (!quit) {
            try {
                // prints the username after displaying the server's message
                if (client.getUserName() != null) {
                    System.out.print("[" + client.getUserName() + "]: ");
                }
                String response = reader.readLine();
                //Checks for system messages.
                if(response.startsWith("/")){
                    System.out.println("\n[Server]" + response.substring(1));
                }
                else{
                    System.out.println("\n" + response);
                }
 
            } catch (IOException ex) {
                //System.out.println("Error reading from server: " + ex.getMessage());
                //ex.printStackTrace();
                quit = true;
            }
        }
    }
}