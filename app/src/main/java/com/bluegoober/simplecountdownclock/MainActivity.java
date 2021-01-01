package com.bluegoober.simplecountdownclock;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    DatabaseHelper db;
    CountdownRecyclerAdapter recyclerAdapter, recyclerAdapterFavorite;
    private FloatingActionButton fab;
    private boolean isOpen;
    private ConstraintLayout layoutMain;
    private RelativeLayout layoutContent;
    private RelativeLayout layoutButtons;
    TextView timeTextView, dateTextView;
    RecyclerView cardRecycler, favoriteCardRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layoutMain = (ConstraintLayout) findViewById(R.id.layout_main);
        layoutContent = (RelativeLayout) findViewById(R.id.layoutContent);
        layoutButtons = (RelativeLayout) findViewById(R.id.layoutNewCountdown);

        fab = (FloatingActionButton) findViewById(R.id.floatingActionButtonNewCountdown);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewMenu();
            }
        });
        createCards();
        setTheme();


        //Update the countdown data every 5 seconds
        final Handler h = new Handler();
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                updateCountdownRecyclerData();
                h.postDelayed(this, 5000); //ms
            }
        };
        h.postDelayed(r, 5000);


        final Calendar calendar = Calendar.getInstance();
        timeTextView=(TextView) findViewById(R.id.editTextTime);
        timeTextView.setInputType(InputType.TYPE_NULL);
        timeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minutes = calendar.get(Calendar.MINUTE);
                TimePickerDialog picker = new TimePickerDialog(MainActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker tp, int timeHour, int timeMinute) {
                                timeTextView.setText(String.format("%02d:%02d", timeHour, timeMinute));
                            }
                        }, hour, minutes, false);
                picker.show();
            }
        });

        dateTextView=(TextView) findViewById(R.id.editTextDate);
        dateTextView.setInputType(InputType.TYPE_NULL);
        dateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);
                DatePickerDialog picker = new DatePickerDialog(MainActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                dateTextView.setText(String.format("%02d/%02d/%04d", dayOfMonth, (monthOfYear + 1), year));
                            }
                        }, year, month, day);
                picker.show();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        //Populate the card data upon resume
        createCards();
        setTheme();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Override the onBackPressed method to check if the menu is open when the user presses back, close the menu if it is
    @Override
    public void onBackPressed() {
        if(isOpen) {
            viewMenu();
        }
        else {
            super.onBackPressed();
        }
    }

    //Create the cards from
    public void createCards() {
        db = new DatabaseHelper(this);
        ArrayList<CountdownObject> countdownList = db.getNotFavoriteCountdowns();
        recyclerAdapter = new CountdownRecyclerAdapter(countdownList);
        cardRecycler = (RecyclerView) findViewById(R.id.card_recycler);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 1);
        cardRecycler.setLayoutManager(layoutManager);
        cardRecycler.setAdapter(recyclerAdapter);


        favoriteCardRecycler = findViewById(R.id.card_recycler_favorites);
        ArrayList<CountdownObject> countdownFavoritesList = db.getFavoriteCountdowns();
        recyclerAdapterFavorite = new CountdownRecyclerAdapter(countdownFavoritesList);
        RecyclerView.LayoutManager layoutManagerFavorite = new GridLayoutManager(this, 1);
        favoriteCardRecycler.setLayoutManager(layoutManagerFavorite);
        favoriteCardRecycler.setAdapter(recyclerAdapterFavorite);
        db.close();
    }

    //Update the recyclerAdapter content
    public void updateCountdownRecyclerData() {
        recyclerAdapter.notifyDataSetChanged();
        recyclerAdapterFavorite.notifyDataSetChanged();
    }

    //Method to show and hide the add countdown menu
    private void viewMenu() {
        //If the menu is not open
        if (!isOpen) {

            //Set the start position to the FAB, the radius starts at approximately the size of the FAB and ends when it cover the whole activity
            int y = Math.round(fab.getY());
            int x = Math.round(fab.getX());
            int startRadius = 15;
            int endRadius = (int) Math.hypot(layoutMain.getWidth(), layoutMain.getHeight());
            //Set the FAB image to the close icon
            fab.setImageResource(R.drawable.baseline_close_fullscreen_24);

            Animator animator = ViewAnimationUtils.createCircularReveal(layoutButtons, x, y, startRadius, endRadius);

            layoutButtons.setVisibility(View.VISIBLE);
            animator.start();

            //Wait 200ms to hide the countdown cards
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    cardRecycler.setVisibility(View.GONE);
                }
            }, 200);

            isOpen = true;

        } else {

            //Set the start position to the FAB and the start radius to the current layout size
            int y = Math.round(fab.getY());
            int x = Math.round(fab.getX());
            int startRadius = Math.max(layoutContent.getWidth(), layoutContent.getHeight());
            int endRadius = 0;

            //Set the FAB icon to the add icon
            fab.setImageResource(R.drawable.baseline_add);

            Animator animator = ViewAnimationUtils.createCircularReveal(layoutButtons, x, y, startRadius, endRadius);
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    cardRecycler.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    layoutButtons.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            animator.start();

            //Set the variable to track if the menu is open to false
            isOpen = false;
        }
    }

    //Create a countdown when the user clicks the submit button
    public void onClickSubmit(View view) {
        //Get the countdown name
        EditText countdownNameInput = findViewById(R.id.editTextName);
        String countdownName = countdownNameInput.getText().toString();

        //Instantiate variables used to create the countdown
        LocalDate date = null;
        LocalTime time = null;
        LocalDateTime dateTime = null;
        long millisecondTime = 0;

        //Get the countdown time and parse into into a LocalTime object
        TextView countdownTimeInput = (TextView) findViewById(R.id.editTextTime);
        String countdownTime = countdownTimeInput.getText().toString();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        if(countdownTime.length() > 0) {
            time = LocalTime.parse(countdownTime, timeFormatter);
        }

        //Get the countdown date and parse it into a LocalDate object
        TextView countdownDateInput = (TextView) findViewById(R.id.editTextDate);
        String countdownDate = countdownDateInput.getText().toString();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        if(countdownDate.length() > 0) {
            date = LocalDate.parse(countdownDate, formatter);
        }

        //If the date and time are not null create a LocalDateTime object from the time and date, also calculate the millisecond time
        if(date != null && time != null) {
            dateTime = LocalDateTime.of(date, time);
            Instant instant = dateTime.atZone(ZoneId.systemDefault()).toInstant();
            millisecondTime = instant.toEpochMilli();
        }


        //Create a new instance of the DatabaseHelper, insert the new countdown, and clear the countdown input fields
        DatabaseHelper db = new DatabaseHelper(this);
        CountdownObject countdownObject = new CountdownObject(0, millisecondTime, countdownName, "", dateTime.toString(), 0);
        db.insertCountdown(countdownObject);
        countdownNameInput.setText("");
        countdownDateInput.setText("");
        countdownTimeInput.setText("");
        db.close();
        viewMenu();
        createCards();
    }

    public void setTheme() {
        //Get the user settings preference for the app theme
        SharedPreferences sharedPref = getSharedPreferences(SettingsActivity.SETTINGS_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        String userTheme = sharedPref.getString("theme", "auto");

        //Set the app theme based on the users preference
        if(userTheme.equals("light")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        else if(userTheme.equals("dark")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }
}