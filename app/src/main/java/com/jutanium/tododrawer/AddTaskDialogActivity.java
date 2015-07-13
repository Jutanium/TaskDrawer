package com.jutanium.tododrawer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

public class AddTaskDialogActivity extends Activity {

    private EditText titleEditText;
    private EditText detailsEditText;
    private Spinner dateSpinner;
    private Spinner timeSpinner;
    private Button addButton;
    private Button cancelButton;

    private LayoutInflater inflator;
    private boolean datePickerShown = false;
    private boolean timePickerShown = false;
    
    private int id = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        Log.i("AddTaskDialogActivity", "onCreate");

        titleEditText = (EditText) findViewById(R.id.name);
        detailsEditText = (EditText) findViewById(R.id.details);
        dateSpinner = (Spinner) findViewById(R.id.dateSpinner);
        timeSpinner = (Spinner) findViewById(R.id.timeSpinner);
        addButton = (Button) findViewById(R.id.add);
        cancelButton = (Button) findViewById(R.id.cancel);

        inflator =  (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);



        Intent i = getIntent();
        if (getIntent().hasExtra("title") || getIntent().hasExtra("details")) {
            setTitle(R.string.edit_task);
            addButton.setText(R.string.done);
            id = getIntent().getIntExtra("index", -1);
            Log.i("AddTaskDialogActivity", "Edit Mode");
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

        ArrayAdapter<CharSequence> dateAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new CharSequence[] { "Select Date"});

        dateSpinner.setAdapter(dateAdapter);
        ArrayAdapter<CharSequence> timeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new CharSequence[] { "Select Time"});
        timeSpinner.setAdapter(timeAdapter);

        dateSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    v.setPressed(true);
                if (event.getAction() == MotionEvent.ACTION_UP)
                    v.setPressed(false);

                toggleDatePicker();
                return true;
            }
        });

        timeSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    v.setPressed(true);
                if (event.getAction() == MotionEvent.ACTION_UP)
                    v.setPressed(false);

                toggleTimePicker();
                return true;
            }
        });

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

    private void toggleDatePicker() {
        if (!datePickerShown && !timePickerShown) {
            View v = inflator.inflate(R.layout.datepicker, null);
            ViewGroup insertPoint = (ViewGroup) findViewById(R.id.datePickerLayout);
            insertPoint.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
        else  {
            ViewGroup layout = (ViewGroup) findViewById(R.id.datePickerLayout);
            layout.removeAllViews();
        }
        datePickerShown = !datePickerShown;
    }

    private void toggleTimePicker() {
        if (!datePickerShown && !timePickerShown) {
            View v = inflator.inflate(R.layout.timepicker, null);
            ViewGroup insertPoint = (ViewGroup) findViewById(R.id.timePickerLayout);
            insertPoint.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
        else  {
            ViewGroup layout = (ViewGroup) findViewById(R.id.timePickerLayout);
            layout.removeAllViews();
        }
        timePickerShown = !timePickerShown;
    }

}
