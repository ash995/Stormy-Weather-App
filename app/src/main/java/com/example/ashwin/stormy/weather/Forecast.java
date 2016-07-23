package com.example.ashwin.stormy.weather;

import com.example.ashwin.stormy.weather.Current;
import com.example.ashwin.stormy.weather.Day;
import com.example.ashwin.stormy.weather.Hour;

/**
 * Created by ashwin on 23/7/16.
 */
public class Forecast {

    private Current mCurrent;
    private Hour[] mHours;
    private Day[] mDays;

    public Current getCurrent() {
        return mCurrent;
    }

    public void setCurrent(Current current) {
        mCurrent = current;
    }

    public Hour[] getHours() {
        return mHours;
    }

    public void setHours(Hour[] hours) {
        mHours = hours;
    }

    public Day[] getDays() {
        return mDays;
    }

    public void setDays(Day[] days) {
        mDays = days;
    }
}
