package com.tabeelcr.lib_vlx_android_test;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

public class RestartService {
    private static RestartService restartService;

    private Context context;
    private Intent intent;
    private CountDownTimer countDownTimer;
    private final int dayInMilli = 24 * 60 * 60 * 1000;
    private Timer timer;
    private boolean firstTime = false;


    public static RestartService getInstance () {
        if (restartService == null) {
            restartService = new RestartService();
        }
        return restartService;
    }

    public void setTimer (Context context, Class pClass) throws ParseException {
        this.context = context;
        intent = new Intent(context, pClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        String alarmTime = context.getString(R.string.restart_hour);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);

        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        String tomorrow = dateFormat.format(calendar.getTime());

        DateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        Log.i("setTimer", tomorrow + " " + alarmTime + ":00");
        Date date = dateFormatter.parse(tomorrow + " " + alarmTime + ":00");

        if (timer != null) timer.cancel();
        timer = new Timer();

        int period = dayInMilli;
        timer.schedule(new RestartTask(), date, period);

//        if (countDownTimer != null) countDownTimer.cancel();
//        countDownTimer = new CountDownTimer(dayInMilli, dayInMilli) {
//
//            public void onTick(long millisUntilFinished) {
//                Log.i("RestartService", "Tick");
//            }
//            public void onFinish() {
//                Log.i("RestartService", "Restarting!");
//                restart();
//            }
//        };
//        countDownTimer.start();
    }

    private void restart () {

//        if (!firstTime) {
//            firstTime = true;
//            return;
//        }

        context.startActivity(intent);
        if (context instanceof Activity) {
            ((Activity) context).finish();
        }

        Runtime.getRuntime().exit(0);
    }

    private RestartService () {}

    private static class RestartTask extends TimerTask
    {

        public void run()
        {
            Log.i("RestartTask", "running!!!... Restarting now!");
            RestartService.getInstance().restart();
        }
    }
}