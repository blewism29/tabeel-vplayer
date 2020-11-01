package com.tabeelcr.lib_vlx_android_test;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.util.VLCVideoLayout;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private static final boolean USE_TEXTURE_VIEW = false;
    private static final boolean ENABLE_SUBTITLES = false;
    private VLCVideoLayout mVideoLayout = null;

    private LibVLC mLibVLC;
    private MediaPlayer mMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ArrayList<String> options = new ArrayList<>();
        options.add("--aout=opensles");
        options.add("--audio-time-stretch"); // time stretching
        options.add("-vvv"); // verbosity

        mLibVLC = new LibVLC(this, options);
        mMediaPlayer = new MediaPlayer(mLibVLC);
        // mMediaPlayer.setEventListener(getListener());

        mVideoLayout = findViewById(R.id.video_layout);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaPlayer.release();
        mLibVLC.release();
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

    private MediaPlayer.EventListener getListener () {
        return new MediaPlayer.EventListener() {
            @Override
            public void onEvent(MediaPlayer.Event event) {
                switch (event.type) {
                    case MediaPlayer.Event.Buffering:
                    case MediaPlayer.Event.EncounteredError:
                        // Replay the stream
                        // Logic to replay if buffering is taking too long.
                        cancel();
                        play();
                        break;
                    case MediaPlayer.Event.EndReached:
                        cancel();
                        break;
                }
            }
        };
    }

    private void cancel() {
        mMediaPlayer.stop();
        mMediaPlayer.release();
        mLibVLC.release();
        mMediaPlayer.detachViews();
    }

    private void play() {
        final String streamUrl = getString(R.string.stream_url);
        final Media media = new Media(mLibVLC, Uri.parse(streamUrl));

        mMediaPlayer.attachViews(mVideoLayout, null, ENABLE_SUBTITLES, USE_TEXTURE_VIEW);
        mMediaPlayer.setMedia(media);

        media.release();

        mMediaPlayer.play();
    }

}