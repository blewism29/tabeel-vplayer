package com.tabeelcr.lib_vlx_android_test;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.util.VLCVideoLayout;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private boolean authorized;
    private static final boolean USE_TEXTURE_VIEW = false;
    private static final boolean ENABLE_SUBTITLES = false;

    private LibVLC mLibVLC;
    private MediaPlayer mMediaPlayer;

    private boolean playing = false;
    private Timer timer;

    private boolean internetAvailable = true;
    private boolean restartPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authorized = checkAuthorization();

        if (authorized) {
            final ArrayList<String> options = new ArrayList<>();
            options.add("--aout=opensles");
            options.add("--audio-time-stretch"); // time stretching
            options.add("-vvv"); // verbosity

            mLibVLC = new LibVLC(this, options);
            mMediaPlayer = new MediaPlayer(mLibVLC);

            VLCVideoLayout mVideoLayout = findViewById(R.id.video_layout);
            mMediaPlayer.attachViews(mVideoLayout, null, ENABLE_SUBTITLES, USE_TEXTURE_VIEW);

            mMediaPlayer.setEventListener(new MediaPlayer.EventListener() {
                @Override
                public void onEvent(MediaPlayer.Event event) {
                    switch (event.type) {
                        case MediaPlayer.Event.Paused:
                            Log.i("MediaPlayer.Event", "Paused");
                            playing = false;
                        case MediaPlayer.Event.Playing:
                            Log.i("MediaPlayer.Event", "Playing");
                            playing = true;
                        case MediaPlayer.Event.Buffering:
                            Log.i("MediaPlayer.Event", "Buffering");
                            break;
                        case MediaPlayer.Event.Stopped:
                            Log.i("MediaPlayer.Event", "Event Stopped ....... isPlaying: " + mMediaPlayer.isPlaying());
                            playing = false;
                            break;
                        case MediaPlayer.Event.EncounteredError:
                            Log.i("MediaPlayer.Event", "Event EncounteredError ....... isPlaying: " + mMediaPlayer.isPlaying());
                            playing = false;
                            break;
                        case MediaPlayer.Event.EndReached:
                            Log.i("MediaPlayer.Event", "Event EndReached ....... isPlaying: " + mMediaPlayer.isPlaying());
                            playing = false;
                            break;
                    }
                }
            });

            TimerTask task =  new TimerTask() {
                public void run() {
                    Log.i("TimerTask network", "Runnning check network");
                    if (!checkInternetConnection()) {
                        Log.i("TimerTask network", "No network");
                        restartPlayer = true;
                        playing = false;
                    }
                }
            };

            timer = new Timer();
            timer.schedule(task, 0, 1000 * 60); // each min

            task = new TimerTask() {
                public void run() {
                    Log.i("TimerTask", "Runnning recurrent task: " + !mMediaPlayer.isPlaying() + " | " + !playing);

                    if (!checkAuthorization()) restartPlayer = true;

                    if ((restartPlayer && checkInternetConnection()) || !mMediaPlayer.isPlaying() || !playing) {
                        Log.i("task", "not playing, restarting stream "  + !mMediaPlayer.isPlaying() + " | " + !playing + " | " + restartPlayer + " | " + checkInternetConnection());
                        try{
                            restartPlayer = false;
                            play();
                        } catch (Exception ex) {
                            Log.i("task", "Error occured: " + ex.getMessage());
                        }
                    }
                }
            };

            timer = new Timer();
            timer.schedule(task, 0, 1000 * 60 * 10); // each 10 min

            try {
                RestartService.getInstance().setTimer(this, MainActivity.class);
            } catch (ParseException e) {
                e.printStackTrace();
            }

        } else {
            final String unauthorizedMessage = getString(R.string.unauthorized_message);
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, unauthorizedMessage, duration);
            toast.show();

            this.finishAffinity();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (authorized) {
            timer.cancel();
            mMediaPlayer.release();
            mLibVLC.release();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        play();
    }

    @Override
    protected void onStop() {
        super.onStop();
        cancel();
    }

    private void cancel() {
        mMediaPlayer.stop();
        mMediaPlayer.release();
        mLibVLC.release();
        mMediaPlayer.detachViews();
    }

    public void stop() {
        mMediaPlayer.stop();
    }

    private void play() {
        if (authorized) {
            final String streamUrl = getString(R.string.stream_url);
            final Media media = new Media(mLibVLC, Uri.parse(streamUrl));

            mMediaPlayer.setMedia(media);
            media.release();

            mMediaPlayer.play();
        }
    }

    private boolean checkAuthorization () {
        String vMacAddress = getMacAddr();
        final String macAddress = getString(R.string.mac_address);
        boolean flag = false;

        for (String temp : vMacAddress.split(",")) {
            if (temp.equals(macAddress)) flag = true;
        }

        return flag;
    }

    private String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            String result = "";
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0") && !nif.getName().equalsIgnoreCase("eth0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    // res1.append(Integer.toHexString(b & 0xFF) + ":");
                    res1.append(String.format("%02X:",b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                result = result + res1.toString() + ",";
            }
            return result;
        } catch (Exception ex) {
            //handle exception
        }
        return "";
    }

    private boolean checkInternetConnection() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com");
            //You can replace it with your name
            return !ipAddr.equals("");
        } catch (Exception e) {
            return false;
        }
    }
}