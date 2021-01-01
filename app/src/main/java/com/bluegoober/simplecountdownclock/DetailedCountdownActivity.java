package com.bluegoober.simplecountdownclock;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DetailedCountdownActivity extends AppCompatActivity {

    public static final String EXTRA_COUNTDOWN_ID = "countdownId";
    private int countdownId;
    private int isFavorite;
    DatabaseHelper db;
    CountdownObject countdownObject;
    MenuItem favoriteMenu;
    CollapsingToolbarLayout toolBarLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_countdown);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        toolBarLayout = findViewById(R.id.toolbar_layout);
        toolBarLayout.setTitle(getTitle());
        String countdownName = (String)getIntent().getExtras().get("countdown_name");
        toolBarLayout.setTitle(countdownName);


        //Call the method to populate the countdown data
        updateCountdownData();

        //Every second, call the method to update the countdown data
        final Handler h = new Handler();
        final Runnable r = new Runnable() {
            int count = 0;
            @Override
            public void run() {
                count++;
               updateCountdownData();
                h.postDelayed(this, 1000); //ms
            }
        };
        h.postDelayed(r, 1000);


    }

    //Method to update the countdown data
    protected void updateCountdownData() {
        //Get the countdown id from the intent and query the database for the corresponding countdown
        countdownId = getIntent().getExtras().getInt(EXTRA_COUNTDOWN_ID);
        db = new DatabaseHelper(this);
        countdownObject = db.getCountdown(countdownId);

        //If the countdown is not null, get the countdown data from the CountdownObject, format it, and update the UI views
        if(countdownObject != null) {
            int id = countdownObject.getId();
            isFavorite = countdownObject.getIsFavorite();
            long milliTime = countdownObject.getLongDate();
            String name = countdownObject.getName();
            String description = countdownObject.getDesc();
            String eventdate = countdownObject.getEventDate();

            final CountdownObject countdown = new CountdownObject(id, milliTime, name, description, eventdate, isFavorite);

            TextView dateTextView = (TextView) findViewById(R.id.countdown_date_view);
            DateFormat dateFormat = new SimpleDateFormat("h:mm aa EEE, dd MMM, yyyy");
            Date date = new Date(countdown.getLongDate());
            dateTextView.setText(dateFormat.format(date));

            //Instantiate the views for the data
            TextView daysTextView = (TextView) findViewById(R.id.days_remaining);
            TextView hoursTextView = (TextView) findViewById(R.id.hours_remaining);
            TextView minutesTextView = (TextView) findViewById(R.id.minutes_remaining);
            TextView secondsTextView = (TextView) findViewById(R.id.seconds_remaining);
            TextView totalHoursTextView = (TextView) findViewById(R.id.total_remaining_hours_view);
            TextView totalMinutesTextView = (TextView) findViewById(R.id.total_remaining_minutes_view);
            TextView totalSecondsTextView = (TextView) findViewById(R.id.total_remaining_seconds_view);
            NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);

            //Calc the various times remaining
            long countdownTime = countdown.getLongDate();
            long currentTime = System.currentTimeMillis();
            long remainingTimeInMilli = countdownTime - currentTime;
            long seconds = remainingTimeInMilli / 1000;
            long secondsFormatted = seconds % 60;
            long minutes = seconds / 60;
            long minutesFormatted = minutes % 60;
            long hours = minutes / 60;
            long hoursFormatted = hours % 24;
            long days = hours / 24;

            //Check the user preferences to see if they want to allow negative countdowns
            SharedPreferences sharedPref = getSharedPreferences(SettingsActivity.SETTINGS_SHARED_PREFERENCES, Context.MODE_PRIVATE);
            boolean canNegative = sharedPref.getBoolean("negative_countdown", false);

            //If the countdown date has already passed and the user does not want negative countdowns, set all of the fields to zero
            if (remainingTimeInMilli < 0 && !canNegative) {
                days = 0;
                hours = 0;
                minutes = 0;
                seconds = 0;
                hoursFormatted = 0;
                minutesFormatted = 0;
                secondsFormatted = 0;
            }

            //Formatted strings for the TextViews
            String daysString = days + "\nDays";
            String hoursString = hoursFormatted + "\nHours";
            String totalHoursString = numberFormat.format(hours) + " hours";
            String minutesString = minutesFormatted + "\nMinutes";
            String totalMinutesString = numberFormat.format(minutes) + " minutes";
            String secondsString = secondsFormatted + "\nSeconds";
            String totalSecondsString = numberFormat.format(seconds) + " seconds";

            //Set the TextViews with the countdown data
            daysTextView.setText(daysString);
            hoursTextView.setText(hoursString);
            totalHoursTextView.setText(totalHoursString);
            minutesTextView.setText(minutesString);
            totalMinutesTextView.setText(totalMinutesString);
            secondsTextView.setText(secondsString);
            totalSecondsTextView.setText(totalSecondsString);
            toolBarLayout.setTitle(countdown.getName());
        }
        db.close();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detailed_countdown, menu);
        favoriteMenu = menu.findItem(R.id.action_favorite_countdown);
        if(isFavorite == 1) {
            favoriteMenu.setIcon(R.drawable.baseline_favorite_24);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit_countdown:
                Intent intent = new Intent(getApplicationContext(), EditCountdown.class);
                intent.putExtra(EditCountdown.EDIT_COUNTDOWN_ID,countdownId);
                startActivity(intent);
                return true;
            case R.id.action_delete_countdown:
                deleteCountdown();
                return true;
            case R.id.action_favorite_countdown:
                toggleFavorite();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void deleteCountdown() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Countdown");
        builder.setMessage("Are you sure you want to delete this countdown?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DatabaseHelper db= new DatabaseHelper(DetailedCountdownActivity.this);
                db.deleteCountdown(countdownId);
                db.close();
                finish();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void toggleFavorite() {
        int isFavorite = countdownObject.getIsFavorite();
        DatabaseHelper db = new DatabaseHelper(this);

        if(isFavorite == 0) {
            db.setCountdownFavorite(countdownId);
            favoriteMenu.setIcon(R.drawable.baseline_favorite_24);
        }
        else {
            db.setCountdownNotFavorite(countdownId);
            favoriteMenu.setIcon(R.drawable.baseline_favorite_border_24);
        }
    }

}