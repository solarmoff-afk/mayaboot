import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.IOException;

public class Main {
    private static final int PORT = 05117;
    private static final String HOST = "127.0.0.1";

    public static void main(String[] args) {
        System.out.println("Guest JAR: Launching...");

        try (Socket socket = new Socket(HOST, PORT)) {
            
            System.out.println("Guest JAR: Successfully connected to " + HOST + ":" + PORT);

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            while (true) {
                String command = in.readLine();
                
                if (command == null) {
                    System.out.println("Guest JAR: Host closed the connection. Exiting.");
                    break;
                }
                
                System.out.println("Guest JAR: Received command: '" + command + "'");

                if ("ping".equals(command)) {
                    System.out.println("Guest JAR: Sending response: 'pong'");
                    out.println("pong");
                } else {
                    System.out.println("Guest JAR: Unknown command received. Exiting.");
                    break;
                }
            }

        } catch (IOException e) {
            System.err.println("Guest JAR: CRITICAL ERROR!");
            e.printStackTrace(System.err);
        }
        
        System.out.println("Guest JAR: Terminating.");
    }
}