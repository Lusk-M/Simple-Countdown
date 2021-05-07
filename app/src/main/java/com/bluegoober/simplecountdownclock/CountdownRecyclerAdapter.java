package com.bluegoober.simplecountdownclock;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CountdownRecyclerAdapter extends RecyclerView.Adapter<CountdownRecyclerAdapter.ViewHolder> {


    private List<CountdownObject> countdowns;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;

        public ViewHolder(CardView v) {
            super(v);
            cardView = v;
        }
    }

    public CountdownRecyclerAdapter(List<CountdownObject> countdowns) { this.countdowns = countdowns; }

    @Override
    public int getItemCount() {
        return countdowns.size();
    }

    @Override
    public CountdownRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView cv = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.card_countdown, parent, false);
        return new ViewHolder(cv);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final CardView cardView = holder.cardView;
        final CountdownObject countdownObject = countdowns.get(position);

        //Calculate the time remaining
        long countdownTime = countdowns.get(position).getLongDate();
        long currentTime = System.currentTimeMillis();
        long remainingTimeInMilli = countdownTime - currentTime;
        long seconds = remainingTimeInMilli / 1000;
        long secondsFormatted = seconds % 60;
        long minutes = seconds / 60;
        long minutesFormatted = minutes % 60;
        long hours = minutes / 60;
        long hoursFormatted = hours % 24;
        long days = hours / 24;
        long daysLeft = countdownObject.getDaysLeft();

        //Check if the user has chosen to view negative countdowns
        SharedPreferences sharedPref = cardView.getContext().getSharedPreferences(SettingsActivity.SETTINGS_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        boolean canNegative = sharedPref.getBoolean("negative_countdown", false);

        //If the countdown is negative and the user does not want negative countdowns, set all of the values to zero
        if( remainingTimeInMilli < 0 && !canNegative) {
            hoursFormatted = 0;
            minutesFormatted = 0;
            daysLeft = 0;
        }

        TextView textNameView = cardView.findViewById(R.id.countdown_name);
        String countdownName = countdownObject.getName();
        textNameView.setText(countdownName);

        TextView textDaysTimeView = cardView.findViewById(R.id.countdown_time_days);
        String daysLeftString = daysLeft +"\n days";
        textDaysTimeView.setText(daysLeftString);

        TextView textHoursTimeView = cardView.findViewById(R.id.countdown_time_hours);
        String hoursString = hoursFormatted +"\n hours";
        textHoursTimeView.setText(hoursString);

        TextView textMinutesTimeView = cardView.findViewById(R.id.countdown_time_minutes);
        String minutesString = minutesFormatted +"\n minutes";
        textMinutesTimeView.setText(minutesString);

        ImageView favoriteIndicator = cardView.findViewById(R.id.favorite_card_indicator);
        if(countdowns.get(position).getIsFavorite() == 1) {
            favoriteIndicator.setVisibility(View.VISIBLE);
            favoriteIndicator.setColorFilter(ContextCompat.getColor(cardView.getContext(), R.color.colorAccent));
        }
        else {
            cardView.setCardBackgroundColor(cardView.getResources().getColor(R.color.off_white));
        }

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
                    public void onClick(View view) {
                Intent intent = new Intent(cardView.getContext(), DetailedCountdownActivity.class);
                intent.putExtra(DetailedCountdownActivity.EXTRA_COUNTDOWN_ID, countdownObject.getId());
                intent.putExtra("countdown_name", countdownObject.getName());
                cardView.getContext().startActivity(intent);
            }
        });
    }
}
