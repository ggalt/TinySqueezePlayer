package com.georgegalt.tinysqueezeplayer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SetupActivity extends AppCompatActivity {
    private static final String TAG = "Setup-Activity";

    private ServerInfo serverInfo;

    private EditText serverIP;
    private EditText playerPort;
    private EditText cliPort;
    private EditText webPort;
    private EditText playerName;
    private EditText userName;
    private EditText passWord;

//    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        // setup fields
        serverIP = (EditText)findViewById(R.id.edtServerIP);
        playerPort = (EditText)findViewById(R.id.edtPort);
        cliPort = (EditText)findViewById(R.id.edtCliPort);
        webPort = (EditText)findViewById(R.id.edtWebPort);
        playerName = (EditText)findViewById(R.id.edtPlayer);
        userName = (EditText)findViewById(R.id.edtUser);
        passWord = (EditText)findViewById(R.id.edtPass);

        // setup connection to server info storage and read data from storage
        serverInfo = new ServerInfo(this);

        // set screen values
        SetScreenValues();

        // setup buttons and OnClick Listenters
        // Reset Button
        final Button resetButton = (Button) findViewById(R.id.btnReset);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serverInfo.resetValues();
                SetScreenValues();
                Toast.makeText(getApplicationContext(),R.string.settings_reset_msg,Toast.LENGTH_SHORT).show();
            }
        });

        final Button okButton = (Button) findViewById(R.id.btnOK);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetScreenValues();
                serverInfo.writeValues();
                onButtonPressed(true);
            }
        });

        final Button cancelButton = (Button) findViewById(R.id.btnCancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonPressed(false);
            }
        });
    }

    private void onButtonPressed(boolean result){
        Intent intent = new Intent();
        intent.putExtra("DataUpdated", result);
        setResult(MainActivity.RESULT_OK, intent);
        finish();
    }

    private void SetScreenValues(){
        serverIP.setText(serverInfo.getServerIP());
        playerPort.setText(serverInfo.getPlayerPort());
        cliPort.setText(serverInfo.getCliPort());
        webPort.setText(serverInfo.getWebPort());
        playerName.setText(serverInfo.getPlayerName());
        userName.setText(serverInfo.getUSERNAME());
        passWord.setText(serverInfo.getPASSWORD());
    }

    private void GetScreenValues() {
        serverInfo.setServerIP(serverIP.getText().toString());
        serverInfo.setPlayerPort(playerPort.getText().toString());
        serverInfo.setCliPort(cliPort.getText().toString());
        serverInfo.setWebPort(webPort.getText().toString());
        serverInfo.setPlayerName(playerName.getText().toString());
        serverInfo.setUSERNAME(userName.getText().toString());
        serverInfo.setPASSWORD(passWord.getText().toString());

    }
}
