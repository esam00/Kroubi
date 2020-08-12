package com.essam.chatapp.utils;

import android.content.Context;

import com.essam.chatapp.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/*
  Created by esammosbah1@gmail.com on 05/09/20.
 */

public class DateTimeUtils {

    public static String getDisplayableDateOfGivenTimeStamp(Context context, long timeStamp, boolean timeOnly ){
        boolean thisDay,yesterday;
        Date date = new Date(timeStamp);

        //String format of date only {03/04/2020}
        String dateString = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date);

        //String format of date only {12:53 AM}
        String timeString = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date);

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        Calendar rightNow = Calendar.getInstance();

        thisDay = rightNow.get(Calendar.DAY_OF_WEEK) == cal.get(Calendar.DAY_OF_WEEK);
        yesterday = rightNow.get(Calendar.DAY_OF_WEEK) - cal.get(Calendar.DAY_OF_WEEK) == 1;

        if (timeOnly || thisDay){
            return timeString;
        }
        if (yesterday){
            return context.getString(R.string.yesterday);
        }

        // if more than yesterday
        return dateString;
    }

}
