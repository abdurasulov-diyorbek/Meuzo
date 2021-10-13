package com.example.aimusicplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;

public class SongsListActivity extends AppCompatActivity {

    private String[] allSongs;
    private ListView mSongList;
    float x1, x2, y1, y2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_songs_list);

        mSongList = findViewById(R.id.songList);

        appExternalStorageStoragePermission();

    }

    public boolean onTouchEvent(MotionEvent touchEvent) {
        switch (touchEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = touchEvent.getX();
                y1 = touchEvent.getY();
                break;
            case MotionEvent.ACTION_UP:
                x2 = touchEvent.getX();
                y2 = touchEvent.getY();
                if (x1 < x2) {
                    Intent i = new Intent(SongsListActivity.this, MainActivity.class);
                    startActivity(i);
                }
                break;
        }
        return false;
    }

    //For storage permission
    public void appExternalStorageStoragePermission() {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        displaySongName();

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    public ArrayList<File> readOnlyAudio(File file) {
        ArrayList<File> arrayList = new ArrayList<>();
        File[] allFiles = file.listFiles();
        for (File individualFile : allFiles) {
            if (individualFile.isDirectory() && !individualFile.isHidden()) {
                arrayList.addAll(readOnlyAudio(individualFile));
            } else {
                if (individualFile.getName().endsWith(".mp3") || individualFile.getName().endsWith(".aac") || individualFile.getName().endsWith(".wav") || individualFile.getName().endsWith(".wma")) {
                    arrayList.add(individualFile);
                }
            }
        }
        return arrayList;

    }


    private void displaySongName() {
        final ArrayList<File> audioSongs = readOnlyAudio(Environment.getExternalStorageDirectory());
        allSongs = new String[audioSongs.size()];
        for (int songCounter = 0; songCounter < audioSongs.size(); songCounter++) {
            allSongs[songCounter] = audioSongs.get(songCounter).getName();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (SongsListActivity.this, android.R.layout.simple_list_item_1, allSongs);
        mSongList.setAdapter(adapter);


        mSongList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                String songName = mSongList.getItemAtPosition(position).toString();
                Intent intent = new Intent(SongsListActivity.this, MainActivity.class);
                intent.putExtra("Song", audioSongs);
                intent.putExtra("name", songName);
                intent.putExtra("position", position);
                startActivity(intent);

            }
        });
    }

}