package com.bluegoober.simplecountdownclock;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;


import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class EditCountdown extends AppCompatActivity {

    TextView eTextTime, eTextDate;
    EditText eTextName;
    Button submitButton;
    int countdownId;
    TimePickerDialog timePicker;
    DatePickerDialog datePicker;

    public static final String EDIT_COUNTDOWN_ID = "editId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_countdown);
        eTextTime = findViewById(R.id.editTextTime);
        eTextDate =  findViewById(R.id.editTextDate);
        submitButton = findViewById(R.id.edit_countdown_button);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitEdit();
            }
        });

        setupDateTimePickers();


        eTextTime.setInputType(InputType.TYPE_NULL);
        eTextTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timePicker.show();
            }
        });

        eTextDate.setInputType(InputType.TYPE_NULL);
        eTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker.show();
            }
        });

        checkForEdit();

    }

    public void checkForEdit() {
        Intent intent = getIntent();
        if(intent.hasExtra(EDIT_COUNTDOWN_ID)) {
            countdownId = (Integer)intent.getExtras().get(EDIT_COUNTDOWN_ID);
            DatabaseHelper db = new DatabaseHelper(this);
            CountdownObject countdown = db.getCountdown(countdownId);

            int countdownId = countdown.getId();
            long milliTime = countdown.getLongDate();
            String name = countdown.getName();
            String description = countdown.getDesc();
            String dateString = countdown.getEventDate();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            LocalDateTime formattedDateTime = LocalDateTime.parse(dateString, formatter);
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            String formattedTime = formattedDateTime.format(timeFormatter);
            eTextTime.setText(formattedTime);
            timePicker.updateTime(formattedDateTime.getHour(), formattedDateTime.getMinute());
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String formattedDate = formattedDateTime.format(dateFormatter);
            eTextDate.setText(formattedDate);
            datePicker.updateDate(formattedDateTime.getYear(), formattedDateTime.getMonthValue() - 1, formattedDateTime.getDayOfMonth());

            eTextName = (EditText)findViewById(R.id.editTextName);
            eTextName.setText(name);

            Button editButton = (Button)findViewById(R.id.edit_countdown_button);
            editButton.setText("Edit Countdown");

            Toast toast = Toast.makeText(this, "test", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    public void submitEdit() {
        EditText countdownNameInput = (EditText) findViewById(R.id.editTextName);
        String countdownName = countdownNameInput.getText().toString();
        LocalDate date = null;
        LocalTime time = null;
        LocalDateTime dateTime = null;
        long millisecondTime = 0;

        TextView countdownTimeInput = (TextView) findViewById(R.id.editTextTime);
        String countdownTime = countdownTimeInput.getText().toString();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        if(countdownTime.length() > 0) {
            time = LocalTime.parse(countdownTime, timeFormatter);
        }

        TextView countdownDateInput = (TextView) findViewById(R.id.editTextDate);
        String countdownDate = countdownDateInput.getText().toString();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        if(countdownDate.length() > 0) {
            date = LocalDate.parse(countdownDate, formatter);
        }

        if(date != null && time != null) {
            dateTime = LocalDateTime.of(date, time);
            Instant instant = dateTime.atZone(ZoneId.systemDefault()).toInstant();
            millisecondTime = instant.toEpochMilli();
        }

        DatabaseHelper db = new DatabaseHelper(this);
        CountdownObject countdownObject = new CountdownObject(countdownId, millisecondTime, countdownName, "", dateTime.toString());
        db.updateCountdown(countdownObject);
        finish();
    }

    public void setupDateTimePickers() {
        Calendar cldr = Calendar.getInstance();
        int day = cldr.get(Calendar.DAY_OF_MONTH);
        int month = cldr.get(Calendar.MONTH);
        int year = cldr.get(Calendar.YEAR);
        int hour = cldr.get(Calendar.HOUR_OF_DAY);
        int minutes = cldr.get(Calendar.MINUTE);

        timePicker = new TimePickerDialog(EditCountdown.this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker tp, int sHour, int sMinute) {
                        eTextTime.setText(String.format("%02d:%02d", sHour, sMinute));
                    }
                }, hour, minutes, false);
        datePicker = new DatePickerDialog(EditCountdown.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        eTextDate.setText(String.format("%02d/%02d/%04d", dayOfMonth, (monthOfYear + 1), year));
                    }
                }, year, month, day);
    }

}