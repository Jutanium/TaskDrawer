package com.jutanium.tododrawer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class AddTaskDialogActivity extends Activity {

    private EditText titleEditText;
    private EditText detailsEditText;
    private Button addButton;
    private Button cancelButton;
    private int id = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        Log.i("AddTaskDialogActivity", "onCreate");
        addButton = (Button) findViewById(R.id.add);
        cancelButton = (Button) findViewById(R.id.cancel);
        titleEditText = (EditText) findViewById(R.id.name);
        detailsEditText = (EditText) findViewById(R.id.details);

        Intent i = getIntent();
        if (getIntent().hasExtra("title") || getIntent().hasExtra("details")) {
            setTitle(R.string.edit_task);
            addButton.setText(R.string.done);
            id = getIntent().getIntExtra("index", -1);
        }
        else {
            setTitle(R.string.add_task);
            addButton.setText(R.string.add);
        }
        if (getIntent().hasExtra("title")) {
            titleEditText.setText(getIntent().getStringExtra("title"));
        }

        if (getIntent().hasExtra("details")) {
            detailsEditText.setText(getIntent().getStringExtra("details"));
        }
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent broadcastIntent = new Intent(TaskDrawerService.DialogButtonPressed);
                broadcastIntent.putExtra("index", id);
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
