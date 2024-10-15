package com.example.rgbmems_smartphoneapp;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.os.Handler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import org.json.JSONException;
import org.json.JSONObject;

public class ConnectToServer {
    private static Socket client;
    private PendingMessage pendingMessage = null; // Store the pending message to be sent

    private Handler handler = new Handler(); // Initialize the Handler
    private TextView responseTextView; // Declare TextView

    public void setResponseTextView(TextView responseTextView) {
        this.responseTextView = responseTextView; // Assign TextView from MainActivity
    }
    public void setPendingMessage(String messageType, int messageValue) {
        this.pendingMessage = new PendingMessage(messageType, messageValue);
        Log.d("ConnectServer", "Pending message stored: " + messageType + " - " + messageValue);

    }
    public void connectToServer(Context context) {
        new Thread(() -> {
            try {
                // Connect to socket
                if (client == null || client.isClosed()) {
                    client = new Socket("10.0.2.2", 8000);
                }

                if (client != null && client.isConnected()) {
                    // Connection successful
                    Log.d("ConnectServer", "Connected to server");

                    // Check if there is a pending message, send it immediately upon successful connection
                    if (pendingMessage != null) {
                        Log.d("ConnectServer", "Pending message found. Sending message...");
                        sendMessageToServer(context, pendingMessage.getName(), pendingMessage.getCheckNumber());
                        pendingMessage = null; // Clear the pending message after sending
                    }

                    // Read the response from the server
                    BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    String serverResponse;
                    while ((serverResponse = in.readLine()) != null) {
                        Log.d("ConnectServer", "Response from server: " + serverResponse);

                        // Update the user interface with the response from the server
                        String finalResponse = serverResponse;
                        ((MainActivity) context).runOnUiThread(() -> {
                            // Display the response from the server on the screen using TextView
                            if (responseTextView != null) {
                                updateResponseText(finalResponse); // Call the method to update and hide the TextView
                            }
                        });
                    }
                } else {
                    // Connection failed
                    Log.d("ClientThread", "Not connected to server!");
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("ConnectServer", "Error connecting to server", e);
            }
        }).start();
    }

    // Send message to the server
    public void sendMessageToServer(Context context, String type, int value) {
        new Thread(() -> {
            try {
                if (client != null && !client.isClosed()) {
                    // Create a JSON object from type and value
                    JSONObject jsonMessage = new JSONObject();
                    jsonMessage.put("type", type);
                    jsonMessage.put("value", value);

                    // Send message to the server
                    OutputStream outputStream = client.getOutputStream();
                    outputStream.write((jsonMessage.toString() + "\n").getBytes());
                    outputStream.flush();

                    Log.d("ClientThread", "Message sent to server: " + jsonMessage.toString());
                } else {
                    Log.e("ClientThread", "Connection not established. Message not sent.");
                }
            } catch (IOException | JSONException e) {
                Log.e("ConnectServer", "Error sending message to server", e);
            }
        }).start();
    }

    // Check if the client is connected to the server
    public boolean isConnected() {
        return client != null && client.isConnected() && !client.isClosed();
    }
    // Disconnect from the server
    public void disconnect(Context context) {
        if (client != null && !client.isClosed()) {
            try {
                client.close(); // Close the connection
                client = null;
                Log.d("ConnectServer", "Disconnected from server");
            } catch (IOException e) {
                Log.e("ConnectServer", "Error while disconnecting", e);
            }
        } else {
            Log.d("ConnectServer", "Already disconnected.");
        }
    }

    // Update the content of the TextView and hide it after a certain period of time
    public void updateResponseText(String response) {
        if (responseTextView != null) {
            responseTextView.setText(response);
            responseTextView.setVisibility(View.VISIBLE); // Show the TextView

            // Set a timer to hide the TextView
            handler.postDelayed(() -> responseTextView.setVisibility(View.GONE), 3000); // Hide after 3 seconds
        }
    }
}

