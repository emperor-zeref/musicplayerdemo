package com.nirajprojects.audiofilesdemo;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    ArrayList<ModelAudio> songList;
    RecyclerView recyclerView;
    MediaPlayer mediaPlayer;
    TextView current, total, song_title;
    ImageView next, prev, play;
    SeekBar seekBar;
    double current_pos, total_duration;
    int audio_index = 0;
    public static final int PERMISSION_READ = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(checkPermission()){
            setAudio();
        }

    }

    public void setAudio(){
        recyclerView = (RecyclerView) findViewById(R.id.songListView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        //controls
        current = findViewById(R.id.current);
        total = findViewById(R.id.total);
        song_title = findViewById(R.id.song_title);
        prev = findViewById(R.id.prev);
        play = findViewById(R.id.play);
        next = findViewById(R.id.next);
        seekBar = findViewById(R.id.seekBar);

        //media file handle initialization
        songList = new ArrayList<>();
        mediaPlayer = new MediaPlayer();

        //get audio files to play
        getAudioFiles();

        //set seeking bar functionality
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                current_pos = seekBar.getProgress();
                mediaPlayer.seekTo((int) current_pos);
            }
        });

        mediaPlayer.setOnCompletionListener(mp -> {
            audio_index++;
            if (audio_index >= songList.size() - 1) {
                audio_index = 0;
            }
            playAudio(audio_index);
        });

        if (!songList.isEmpty()){
            // playAudio(audio_index);
            playPrevious();
            playNext();
            playPause();
        }
    }

    //play the song
    public void playAudio(int pos){
        try {
            mediaPlayer.reset();
            //set file path of the song
            mediaPlayer.setDataSource(this, songList.get(pos).getUri());
            mediaPlayer.prepare();
            mediaPlayer.start();
            play.setImageResource(R.drawable.ic_sharp_pause_24);
            song_title.setText(songList.get(pos).getSongTitle());
            audio_index = pos;

        }catch (Exception e){
            e.printStackTrace();
        }
        setAudioProgress();
    }

    //set audio progress
    public void setAudioProgress(){
        //get duration of audio
        current_pos = mediaPlayer.getCurrentPosition();
        total_duration = mediaPlayer.getDuration();

        //display the durations
        total.setText(timerConversion((long) total_duration));
        current.setText(timerConversion((long) current_pos));

        seekBar.setMax((int) total_duration);
        final Handler handler = new Handler();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                   current_pos = mediaPlayer.getCurrentPosition();
                   current.setText(timerConversion((long) current_pos));
                   seekBar.setProgress((int) current_pos);
                   handler.postDelayed(this,1000);

                }catch (IllegalStateException ed){
                    ed.printStackTrace();
                }
            }
        };
        handler.postDelayed(runnable, 1000);

    }

    //play next song
    public void playNext(){
        next.setOnClickListener(v -> {
            if(audio_index<songList.size() - 1){
                audio_index++;
            } else {
                audio_index = 0;
            }
            playAudio(audio_index);
        });
    }

    //play previous song
    public void playPrevious(){
        prev.setOnClickListener(v -> {
            if(audio_index>0){
                audio_index--;
            } else {
                audio_index = songList.size() -1;
            }
            playAudio(audio_index);
        });
    }

    //play and pause
    public void playPause(){
        play.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()){
                play.setImageResource(R.drawable.ic_sharp_pause_24);
                mediaPlayer.pause();
            } else {
                play.setImageResource(R.drawable.ic_sharp_play_arrow_24);
                mediaPlayer.start();
            }
        });
    }

    //time conversion
    public String timerConversion(long value) {
        String audioTime;
        int dur = (int) value;
        int hrs = (dur / 3600000);
        int mns = (dur / 60000) % 60000;
        int scs = dur % 60000 / 1000;

        if (hrs > 0) {
            audioTime = String.format(Locale.ENGLISH,"%02d:%02d:%02d",hrs,mns,scs);
        } else {
            audioTime = String.format(Locale.ENGLISH,"%02d:%02d", mns, scs);
        }
        return audioTime;
    }
    //


    //fetch audio files from storage  with looping and save the list of songs

    public void getAudioFiles(){
        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        Cursor cursor = contentResolver.query(uri,null,null,null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));

                ModelAudio modelAudio = new ModelAudio();
                modelAudio.setSongTitle(title);
                modelAudio.setSongArtist(artist);
                modelAudio.setSongDuration(duration);
                modelAudio.setUri(Uri.parse(url));
                songList.add(modelAudio);
            } while(cursor.moveToNext());
        }
        assert cursor != null;
        cursor.close();
        AudioAdapter adapter = new AudioAdapter(songList,this);
        recyclerView.setAdapter(adapter);

        adapter.setOnClickItemListener((pos, v) -> playAudio(pos));
    }
    //Runtime Permission Request
    public boolean checkPermission(){
        int READ_EXTERNAL_PERMISSION = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (READ_EXTERNAL_PERMISSION != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_READ);
            return false;
        }else{
            return true;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_READ) {
            if (grantResults.length > 0 && permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, "Please allow permission", Toast.LENGTH_SHORT).show();
                } else {
                    setAudio();
                }
            }
        }
    }
    //Release Media Player
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer!=null){
            mediaPlayer.release();
        }
    }
}