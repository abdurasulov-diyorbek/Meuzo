package com.example.aimusicplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SyncStatusObserver;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Locale;


public class MainActivity<implementation> extends AppCompatActivity {

    private ImageView previous, play_pause, next;
    private TextView songName, currentTime, totalTime;

    private ImageView imageView;
    private RelativeLayout lower;


    SeekBar seekBar;
    Handler handler;
    Runnable runnable;

    public static MediaPlayer myMediaPlayer;
    private int position;
    private ArrayList<File> mySongs;
    private String mSongName;
    float x1, x2, y1, y2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        handler = new Handler();

        seekBar = (SeekBar) findViewById(R.id.seekbar);
        play_pause = findViewById(R.id.play_pause);
        next = findViewById(R.id.next);
        previous = findViewById(R.id.previous);
        songName = findViewById(R.id.songName);
        currentTime = findViewById(R.id.currentTime);
        totalTime = findViewById(R.id.totalTime);


        receiveValuesAndStartPlaying();


        String duration = createTimerLabel(myMediaPlayer.getDuration());
        totalTime.setText(duration);


        play_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPauseSong();
            }
        });


        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myMediaPlayer.getCurrentPosition() > 0) {
                    previousSong();
                }
            }
        });


        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myMediaPlayer.getCurrentPosition() > 0) {
                    playNextSong();
                }
            }
        });
    }

    //Code for settings menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mymenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item1:
                Intent intent = new Intent(MainActivity.this, Settings.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //open another activity with swipe
    public boolean onTouchEvent(MotionEvent touchEvent) {
        switch (touchEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = touchEvent.getX();
                y1 = touchEvent.getY();
                break;
            case MotionEvent.ACTION_UP:
                x2 = touchEvent.getX();
                y2 = touchEvent.getY();
                if (x1 > x2) {
                    Intent i = new Intent(MainActivity.this, MainActivity2.class);
                    startActivity(i);
                }
                break;
        }
        return false;
    }


    @Override
    public void onBackPressed() {
        myMediaPlayer.stop();
        myMediaPlayer.release();
        super.onBackPressed();

    }


    private void receiveValuesAndStartPlaying() {
        if (myMediaPlayer != null && myMediaPlayer.isPlaying()) {
            myMediaPlayer.pause();
            myMediaPlayer.stop();
            myMediaPlayer.release();
        }
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        mySongs = (ArrayList) bundle.getParcelableArrayList("Song");
        mSongName = mySongs.get(position).getName();

        String songNames = intent.getStringExtra("name");
        songName.setText(songNames);
        songName.setSelected(true);

        position = bundle.getInt("position", 0);
        Uri uri = Uri.parse(mySongs.get(position).toString());

        myMediaPlayer = MediaPlayer.create(MainActivity.this, uri);
        myMediaPlayer.start();

        seekBar.setMax(myMediaPlayer.getDuration());
        playCycle();
        myMediaPlayer.start();


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean input) {
                if (input) {
                    myMediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (myMediaPlayer != null) {
                    if (myMediaPlayer.isPlaying()) {
                        try {
                            final double current = myMediaPlayer.getCurrentPosition();
                            final String elapsedTime = createTimerLabel((int) current);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    currentTime.setText(elapsedTime);

                                }
                            });
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                        }
                    }
                }

            }
        }).start();


    }

    public void playCycle() {
        seekBar.setProgress(myMediaPlayer.getCurrentPosition());

        if (myMediaPlayer.isPlaying()) {
            runnable = new Runnable() {
                @Override
                public void run() {
                    playCycle();
                }
            };
            handler.postDelayed(runnable, 1000);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        myMediaPlayer.start();
        playCycle();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        myMediaPlayer.release();
        handler.removeCallbacks(runnable);
    }

    private void playPauseSong() {
        if (myMediaPlayer.isPlaying()) {
            play_pause.setImageResource(R.drawable.play);
            myMediaPlayer.pause();
        } else {
            play_pause.setImageResource(R.drawable.pause);
            myMediaPlayer.start();
        }
    }


    private void playNextSong() {
        myMediaPlayer.pause();
        myMediaPlayer.stop();
        myMediaPlayer.release();

        position = ((position + 1) % mySongs.size());
        Uri uri = Uri.parse(mySongs.get(position).toString());
        myMediaPlayer = MediaPlayer.create(MainActivity.this, uri);
        mSongName = mySongs.get(position).toString();
        songName.setText(mSongName);
        myMediaPlayer.start();
        if (myMediaPlayer.isPlaying()) {
            play_pause.setImageResource(R.drawable.pause);
        } else {
            play_pause.setImageResource(R.drawable.play);
        }
    }


    private void previousSong() {
        myMediaPlayer.pause();
        myMediaPlayer.stop();
        myMediaPlayer.release();


        position = ((position - 1) < 0 ? (mySongs.size() - 1) : (position - 1));
        Uri uri = Uri.parse(mySongs.get(position).toString());
        myMediaPlayer = MediaPlayer.create(MainActivity.this, uri);
        mSongName = mySongs.get(position).toString();

        songName.setText(mSongName);
        myMediaPlayer.start();
        if (myMediaPlayer.isPlaying()) {
            play_pause.setImageResource(R.drawable.pause);
        } else {
            play_pause.setImageResource(R.drawable.play);
        }
    }

    public String createTimerLabel(int duration) {
        String timer_label = "";
        int min = duration / 1000 / 60;
        int sec = duration / 1000 % 60;
        timer_label += min + ":";
        if (sec < 10) {
            timer_label += "0";
        }
        timer_label += sec;
        return timer_label;
    }

}