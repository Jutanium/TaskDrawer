package com.jutanium.tododrawer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;

import java.util.Calendar;

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

    private final String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

    private int calendarYear;
    private int currentHour, currentMinute, currentYear, currentMonth, currentDay;

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

        Calendar c = Calendar.getInstance();
        currentHour = c.get(Calendar.HOUR);
        currentMinute = c.get(Calendar.MINUTE);
        currentYear = calendarYear = c.get(Calendar.YEAR);
        currentMonth = c.get(Calendar.MONTH);
        currentDay = c.get(Calendar.DATE);

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
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setPressed(true);
                    toggleDatePicker();
                }
                if (event.getAction() == MotionEvent.ACTION_UP)
                    v.setPressed(false);


                return true;
            }
        });

        timeSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setPressed(true);
                    toggleTimePicker();
                }
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
            titleEditText.setVisibility(View.GONE);
            detailsEditText.setVisibility(View.GONE);
            View v = inflator.inflate(R.layout.datepicker, null);
            ((DatePicker)v).init(currentYear, currentMonth, currentDay, new DatePicker.OnDateChangedListener() {
                @Override
                public void onDateChanged(DatePicker view, int year, int month, int day) {
                    String text = months[month] + " " + day;
                    if (year != calendarYear) text += " " + year;
                    ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(AddTaskDialogActivity.this,
                            android.R.layout.simple_spinner_dropdown_item,
                            new CharSequence[] { text } );
                    dateSpinner.setAdapter(adapter);

                    currentYear = year;
                    currentMonth = month;
                    currentDay = day;
                }
            });
            ViewGroup insertPoint = (ViewGroup) findViewById(R.id.datePickerLayout);
            insertPoint.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
        else  {
            titleEditText.setVisibility(View.VISIBLE);
            detailsEditText.setVisibility(View.VISIBLE);
            ViewGroup layout = (ViewGroup) findViewById(R.id.datePickerLayout);
            layout.removeAllViews();
        }
        datePickerShown = !datePickerShown;
    }

    private void toggleTimePicker() {
        if (!datePickerShown && !timePickerShown) {
            titleEditText.setVisibility(View.GONE);
            detailsEditText.setVisibility(View.GONE);
            TimePicker v = (TimePicker)inflator.inflate(R.layout.timepicker, null);
            v.setCurrentHour(currentHour);
            v.setCurrentMinute(currentMinute);
            v.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                @Override
                public void onTimeChanged(TimePicker view, int hour, int minute) {
                    String minuteString = minute < 10 ? "0" + minute : String.valueOf(minute);
                    String text = "";
                    if (hour == 0) text = "12:" + minuteString + "AM";
                    else text = (hour > 12 ? hour - 12 : hour) +
                            ":" + minuteString + (hour >= 12 ? "PM" : "AM");
                    ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(AddTaskDialogActivity.this,
                            android.R.layout.simple_spinner_dropdown_item,
                            new CharSequence[] { text } );
                    timeSpinner.setAdapter(adapter);

                    currentHour = hour;
                    currentMinute = minute;
                }
            });

            ViewGroup insertPoint = (ViewGroup) findViewById(R.id.timePickerLayout);
            insertPoint.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        }
        else  {
            titleEditText.setVisibility(View.VISIBLE);
            detailsEditText.setVisibility(View.VISIBLE);
            ViewGroup layout = (ViewGroup) findViewById(R.id.timePickerLayout);
            layout.removeAllViews();
        }
        timePickerShown = !timePickerShown;
    }

}
