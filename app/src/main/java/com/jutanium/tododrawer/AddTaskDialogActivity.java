package com.jutanium.tododrawer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class AddTaskDialogActivity extends Activity {

    private EditText titleEditText;
    private EditText detailsEditText;
    private Button addButton;
    private Button cancelButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        Log.i("AddTaskDialogActivity", "onCreate");
        addButton = (Button) findViewById(R.id.add);
        cancelButton = (Button) findViewById(R.id.cancel);
        titleEditText = (EditText) findViewById(R.id.name);
        detailsEditText = (EditText) findViewById(R.id.details);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent broadcastIntent = new Intent(TodoDrawerService.DialogButtonPressed);
                broadcastIntent.putExtra("title", titleEditText.getText().toString());
                broadcastIntent.putExtra("details", detailsEditText.getText().toString());
                sendBroadcast(broadcastIntent);
                finish();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
