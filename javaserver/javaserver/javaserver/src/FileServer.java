import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class FileServer {

    public static void main(String[] args) throws IOException, ParseException {

        ServerSocket servsock = new ServerSocket(8000);
        System.out.println("Server is running and waiting for client connection...");

        while (true) {
            System.out.println("Waiting...");

            // Accept connection from client
            Socket sock = servsock.accept();
            System.out.println("Accepted connection from: " + sock);

            // Read message from client
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                String clientMessage;

                // Read each line of message from client until there is no more data
                while ((clientMessage = in.readLine()) != null) {
                    JSONParser parser = new JSONParser();
                    JSONObject json = (JSONObject) parser.parse(clientMessage);
                    
                    String type = (String) json.get("type");  
                    Integer value = (Integer) json.get("value");                  
                    
                    System.out.println("Message received from client: " + type);
                    System.out.println("Message received from client: " + value);

                    switch (type) {
                        case "turnonoff":
                            if (value == 1) {
                                sock.getOutputStream().write("Turn Light On\n".getBytes());
                            } else if (value == 0) {
                                sock.getOutputStream().write("Turn Light Off\n".getBytes());
                            } else {
                                sock.getOutputStream().write("invalid value for turnonoff\n".getBytes());
                            }
                            break;
            
                        case "sendnumber":
                            if (value >= 0 && value <= 90) {
                                sock.getOutputStream().write("Valid Number\n".getBytes());
                            } else {
                                sock.getOutputStream().write("No Data\n".getBytes());
                            }
                            break;
            
                        default:
                            sock.getOutputStream().write("Invalid type\n".getBytes());
                            break;
                    }
                }

                in.close();
            } catch (IOException e) {
                System.out.println("Error reading message from client");
                e.printStackTrace();
            }

            // Close the connection to the client after receiving data
            sock.close();
            System.out.println("Connection with client closed.");
        }
   }
}
