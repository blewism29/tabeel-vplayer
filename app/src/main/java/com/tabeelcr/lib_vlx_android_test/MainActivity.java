package com.tabeelcr.lib_vlx_android_test;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.util.VLCVideoLayout;

import java.net.NetworkInterface;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.tabeelcr.lib_vlx_android_test.RestartService;

public class MainActivity extends AppCompatActivity {

    private boolean authorized;
    private static final boolean USE_TEXTURE_VIEW = false;
    private static final boolean ENABLE_SUBTITLES = false;

    private LibVLC mLibVLC;
    private MediaPlayer mMediaPlayer;

    private long timeChanged;
    private boolean playing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authorized = true; //checkAuthorization();

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
                        case MediaPlayer.Event.Vout:
                            Log.i("MediaPlayer.Event", "Vout");
                        case MediaPlayer.Event.PositionChanged:
                            Log.i("MediaPlayer.Event", "Paused");
                        case MediaPlayer.Event.Paused:
                            Log.i("MediaPlayer.Event", "Paused");
                            playing = false;
                        case MediaPlayer.Event.Playing:
                            Log.i("MediaPlayer.Event", "Playing");
                            playing = true;
                        case MediaPlayer.Event.TimeChanged:
                            if (timeChanged == event.getTimeChanged()) playing = false;
                            timeChanged = event.getTimeChanged() != 0 ? event.getTimeChanged() : timeChanged;
                            Log.i("MediaPlayer.Event", "TimeChanged " + event.getTimeChanged());
                        case MediaPlayer.Event.Buffering:
                            Log.i("MediaPlayer.Event", "Buffering");
                            break;
                        case MediaPlayer.Event.Stopped:
                            Log.i("MediaPlayer.Event", "Event Stopped ....... isPlaying: " + mMediaPlayer.isPlaying());
                            restartPlayerIn(30000);
                            break;
                        case MediaPlayer.Event.EncounteredError:
                            Log.i("MediaPlayer.Event", "Event EncounteredError ....... isPlaying: " + mMediaPlayer.isPlaying());
                            restartPlayerIn(5000);
                            break;
                        case MediaPlayer.Event.EndReached:
                            Log.i("MediaPlayer.Event", "Event EndReached ....... isPlaying: " + mMediaPlayer.isPlaying());
                            restartPlayerIn(1000);
                            break;
                    }
                }
            });

            checkPlayer(1000 * 90);
        } else {
            final String unauthorizedMessage = getString(R.string.unauthorized_message);
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, unauthorizedMessage, duration);
            toast.show();

            this.finishAffinity();
        }

        try {
            RestartService.getInstance().setTimer(this, MainActivity.class);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (authorized) {
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

    private void restartPlayerIn(final int milliseconds) {
        Log.e("TAVO -------->", "restartPlayerIn .......");
        if (!mMediaPlayer.isPlaying()) {
            Log.e("TAVO -------->", "NO PLaying restartPlayerIn .......");
            new CountDownTimer(milliseconds, milliseconds) {
                @Override
                public void onTick(long l) {

                }
                public void onFinish() {
                    try{
                        play();
                    } catch (Exception ex) {
                        Log.e("restartPlayerIn", "Error occured: " + ex.getMessage());
                        restartPlayerIn(milliseconds);
                    }

                }
            }.start();
        }
    }

    private void checkPlayer(final int milliseconds) {
        Log.i("checkPlayer", "Restaring in: " + milliseconds / 1000 + " seconds");

        final long timeChangedCopy = timeChanged;

        new CountDownTimer(milliseconds, milliseconds) {
            @Override
            public void onTick(long l) { }
            public void onFinish() {
                if (!playing || timeChangedCopy == timeChanged) {
                    Log.i("checkPlayer", "not playing, restarting stream");
                    try{
                        play();
                    } catch (Exception ex) {
                        Log.i("checkPlayer", "Error occured: " + ex.getMessage());
                    }
                }
                checkPlayer(milliseconds);
            }
        }.start();
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
}