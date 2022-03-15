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
    private Set<Group> groups = new HashSet<>();
 
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

    private Group getGroup(String group){
        for (Group aGroup : groups){
            if(aGroup.name == group){
                return aGroup;
            }
        }
        return null;
    }

    /**
     * Takes a group name, adds it to the list of groups, and adds the user to that group.
     */
    void makeGroup(String group, ClientThread user){
        this.groups.add(new Group(group, user));
    }

    /**
     * Adds the user to the selected group, if it exists.
     */
    void joinGroup(String group, ClientThread user){
        Group target = getGroup(group);
        if(target == null){
            user.sendMessage("Group does not exist.");
            return;
        }
        target.members.add(user);
    }

    /**
     * Removes the selected user from the selected group, if it exists.
     */
    void leaveGroup(String group, ClientThread user){
        Group target = getGroup(group);
        if(target == null){
            user.sendMessage("Group does not exist.");
            return;
        }
        target.members.remove(user);
    }

    /**
     * Removes the group from the server entirely.
     */
    void removeGroup(String group, ClientThread user){
        Group target = getGroup(group);
        if(target == null){
            user.sendMessage("Group does not exist.");
            return;
        }
        groups.remove(target);
    }
    /**
     * Broadcasts the message to all users in that group.
     */
    void groupMessage(String message, String group, ClientThread excludeUser) {
        Group target = getGroup(group);
        if(target == null){
            excludeUser.sendMessage("Group does not exist.");
            return;
        }
        for(ClientThread aUser : target.members){
            if(aUser != excludeUser){
                aUser.sendMessage("<" + group + "> " + message);
            }
        }
    }

    /**
     * Sends a list of all current groups to the selected user.
     * @param user
     */
    void listGroups(ClientThread user){
        if(groups.isEmpty()){
            user.sendMessage("No groups available.");
        }
        String grouplist = "";
        for(Group aGroup : groups){
            grouplist.concat(aGroup.name + ", ");
        }
        user.sendMessage(grouplist);
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


class Group {
    protected Set<ClientThread> members = new HashSet<>();
    public String name;

    Group(String name, ClientThread user){
        this.name = name;
        this.members.add(user);
    }
}