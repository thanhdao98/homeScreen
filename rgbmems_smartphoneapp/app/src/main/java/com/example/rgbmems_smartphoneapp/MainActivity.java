package com.example.rgbmems_smartphoneapp;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Switch toggle_connect;
    private Switch toggle_onoff;
    private PendingMessage pendingMessage = null; // Store the pending message to be sent
    private ConnectToServer connectToServer;
    private Button sendButton;
    private String type = "";
    private int value;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);

        // Initialize the TextView
        TextView responseTextView = findViewById(R.id.responseTextView);

        // Connect to the server as soon as the application starts
        connectToServer = new ConnectToServer();
        connectToServer.setResponseTextView(responseTextView); // Assign the TextView to the ConnectToServer class
        connectToServer.connectToServer(MainActivity.this);

        // Set up the spinner
        setupSpinner();

        toggle_connect = findViewById(R.id.switch1);
        toggle_connect.setChecked(true); // Set switch1 to ON
        toggle_onoff = findViewById(R.id.switch2);

        // Handle the event when the state of toggle_connect changes
        toggle_connect.setOnCheckedChangeListener((buttonView, isChecked) -> handleToggleConnect(isChecked));

        // Handle the event when the state of toggle_onoff changes
        toggle_onoff.setOnCheckedChangeListener((buttonView, isChecked) -> handleToggleOnOff(isChecked));

        // Handle the event when the sendButton is clicked
        sendButton = findViewById(R.id.button3);
        sendButton.setOnClickListener(v -> handleSendButtonClick());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupSpinner() {
        Spinner dropdown = findViewById(R.id.spinner);
        List<String> numbersList = new ArrayList<>();
        for (int i = 0; i <= 99; i++) {
            numbersList.add(String.valueOf(i));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, numbersList);
        dropdown.setAdapter(adapter);

        ListPopupWindow listPopupWindow = new ListPopupWindow(this);
        listPopupWindow.setAnchorView(dropdown);
        listPopupWindow.setAdapter(adapter);
        listPopupWindow.setHeight(650);
        listPopupWindow.setModal(true);

        dropdown.setOnTouchListener((v, event) -> {
            listPopupWindow.show();
            return true;
        });

        listPopupWindow.setOnItemClickListener((parent, view, position, id) -> {
            dropdown.setSelection(position);
            listPopupWindow.dismiss();
        });
    }

    private void handleToggleConnect(boolean isChecked) {
        if (isChecked) {
            // When Switch 1 is turned on, establish a connection to the server
            connectToServer.connectToServer(MainActivity.this);
        } else {
            // When Switch 1 is turned off, disconnect from the server
            connectToServer.disconnect(MainActivity.this); // Call the disconnect method
        }
    }

    private void handleToggleOnOff(boolean isChecked) {
        type = "turnonoff";
        value = isChecked ? 1 : 0;

        if (toggle_connect.isChecked()) {
            // If Switch 1 is on, send the message immediately
            connectToServer.sendMessageToServer(MainActivity.this, type, value);
        } else {
            // If Switch 1 is off, store the pending message to be sent later
            connectToServer.setPendingMessage(type, value);
        }
    }

    private void handleSendButtonClick() {
        type = "sendnumber";
        Spinner dropdown = findViewById(R.id.spinner);
        int selectedNumber = Integer.parseInt(dropdown.getSelectedItem().toString());
        if (connectToServer.isConnected()) {
            connectToServer.sendMessageToServer(MainActivity.this, type, selectedNumber);
        } else {
            connectToServer.setPendingMessage(type, selectedNumber); // Store the pending message to be sent later
        }    }

    @Override
    protected void onResume() {
        super.onResume();
        if (toggle_connect.isChecked() && pendingMessage != null) {
            connectToServer.sendMessageToServer(MainActivity.this, pendingMessage.getName(), pendingMessage.getCheckNumber());
            pendingMessage = null; // Clear the pending message after sending
        }
    }
}
