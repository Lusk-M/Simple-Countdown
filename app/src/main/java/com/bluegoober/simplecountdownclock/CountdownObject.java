package com.bluegoober.simplecountdownclock;

import java.time.LocalDateTime;
import java.util.Date;

public class CountdownObject {

    private int id, isFavorite;
    private long milliTime, secondsLeft, seconds, daysLeft, hoursLeft, hours, minutesLeft, minutes;
    private String eventDate;
    private String name, desc;


    public CountdownObject(int id, long milliTime, String name, String desc, String eventDate, int isFavorite) {
        this.id = id;
        this.milliTime = milliTime;
        this.name = name;
        this.desc = desc;
        this.eventDate = eventDate;
        this.isFavorite = isFavorite;

        long currentTime = System.currentTimeMillis();
        long remainingTimeInMilli = milliTime - currentTime;
        this.seconds = remainingTimeInMilli / 1000;
        this.secondsLeft = seconds % 60;
        this.minutes = seconds / 60;
        this.minutesLeft = minutes % 60;
        this.hours = minutes / 60;
        this.hoursLeft = hours % 24;
        this.daysLeft = hours / 24;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public long getLongDate() {
        return milliTime;
    }

    public void setLongDate(long milliTime) {
        this.milliTime = milliTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public long getDaysLeft() {
        return daysLeft;
    }

    public long getHoursLeft() {
        return hoursLeft;
    }

    public long getMinutesLeft() {
        return minutesLeft;
    }

    public long getSecondsLeft() {
        return secondsLeft;
    }

    public long getHours() {
        return hours;
    }

    public long getMinutes() {
        return minutes;
    }

    public long getSeconds() {
        return seconds;
    }

    public int getIsFavorite() {return isFavorite;}

    public void setIsFavorite(int isFavorite) {this.isFavorite = isFavorite;}
}
