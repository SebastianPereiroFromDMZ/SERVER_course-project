import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Server implements Runnable {

    private ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;
    private int clientCount = 0;
    Logger logger;


    public Server() {
        connections = new ArrayList<>();
        done = false;
    }

    public static int getPort() throws IOException {
        String filePath = "settings.txt";
        BufferedReader reader = new BufferedReader(new FileReader(new File(filePath)));
        String content = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        String[] splitString = content.split(" ");
        return Integer.parseInt(splitString[2]);
    }


    @Override
    public void run() {
        try {
            server = new ServerSocket(getPort());
            pool = Executors.newCachedThreadPool();
            while (!done) {
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                pool.execute(handler);
            }
        } catch (IOException e) {
            shutdown();
        }
    }

    public void broadcast(String message) {
        for (ConnectionHandler handler : connections) {
            if (handler != null) {
                handler.sendMessage(message);
            }
        }
    }

    public void shutdown() {
        try {
            done = true;
            pool.shutdown();
            if (!server.isClosed()) {
                server.close();
            }
            for (ConnectionHandler handler : connections) {
                handler.shutdown();
            }
        } catch (IOException e) {

        }
    }


    class ConnectionHandler implements Runnable {

        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String nickname;


        public ConnectionHandler(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                logger = new Logger();
                out.println("Please enter a nickname: ");
                nickname = in.readLine();
                System.out.println(nickname + " connected!");
                logger.log(nickname + " connected!");
                clientCount++;
                broadcast(nickname + " joined to chat!");
                broadcast("Number of people in the chat " + clientCount);
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("/nick")) {
                        String[] messageSplit = message.split(" ", 2);
                        if (messageSplit.length == 2) {
                            broadcast(nickname + " renamed themselves to " + messageSplit[1]);
                            System.out.println(nickname + " renamed themselves to " + messageSplit[1]);
                            logger.log(nickname + " renamed themselves to " + messageSplit[1]);
                            nickname = messageSplit[1];
                            out.println("Successfully changed nickname to " + messageSplit[1]);
                        } else {
                            out.println("No nickname provided!");
                        }
                    } else if (message.startsWith("/exit")) {
                        broadcast(nickname + " left the chat");
                        logger.log(nickname + " left the chat");
                        clientCount--;
                        broadcast("Number of people in the chat " + clientCount);
                        shutdown();
                    } else {
                        broadcast(nickname + ": " + message);
                        logger.log(nickname + ": " + message);
                    }
                }
            } catch (Exception e) {
                shutdown();
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }

        public void shutdown() {
            try {


                in.close();
                out.close();
                if (!client.isClosed()) {
                    client.close();
                }
            } catch (IOException e) {

            }
        }


    }

    public static void main(String[] args) {

        Server server = new Server();
        server.run();
    }
}
