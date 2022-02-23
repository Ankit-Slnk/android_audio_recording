package com.webbrains.audiorecording.screens;

import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.webbrains.audiorecording.R;
import com.webbrains.audiorecording.utility.Utility;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private boolean audioRecordingPermissionGranted = false;
    TextView tvFilename, tvRecordedAudio, tvStartTime, tvEndTime;
    File file;
    LinearLayout llMediaPlayer;
    SeekBar seekBar;
    private MediaPlayer mediaPlayer;
    public static int oneTimeOnly = 0;
    private Handler myHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mediaPlayer = new MediaPlayer();

        tvFilename = findViewById(R.id.tvFilename);
        tvRecordedAudio = findViewById(R.id.tvRecordedAudio);
        llMediaPlayer = findViewById(R.id.llMediaPlayer);
        tvStartTime = findViewById(R.id.tvStartTime);
        seekBar = findViewById(R.id.seekBar);
        tvEndTime = findViewById(R.id.tvEndTime);

        tvRecordedAudio.setVisibility(View.GONE);
        llMediaPlayer.setVisibility(View.GONE);
    }

    public void onTap(View view) {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) == PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent i = new Intent(MainActivity.this, RecordActivity.class);
            startActivityForResult(i, 1);
        } else {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                file = new File(data.getStringExtra("filename"));
                tvFilename.setText(file.getName().split("/")[file.getName().split("/").length - 1]);

                tvRecordedAudio.setVisibility(View.VISIBLE);
                llMediaPlayer.setVisibility(View.VISIBLE);

                tvRecordedAudio.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.pause();
                            tvRecordedAudio.setText("Play Recorded Audio");
                        } else {
                            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            try {
                                mediaPlayer.setDataSource(getApplicationContext(), Uri.fromFile(file));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                mediaPlayer.prepare();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            mediaPlayer.start();

                            Log.e("getDuration", mediaPlayer.getDuration() + "");
                            Log.e("getCurrentPosition", mediaPlayer.getCurrentPosition() + "");

                            if (oneTimeOnly == 0) {
                                seekBar.setMax(mediaPlayer.getDuration());
                                oneTimeOnly = 1;
                            }

                            tvEndTime.setText(Utility.milliSecondsToTimer(mediaPlayer.getDuration()));

                            tvStartTime.setText(Utility.milliSecondsToTimer(mediaPlayer.getCurrentPosition()));

                            seekBar.setProgress(mediaPlayer.getCurrentPosition());
                            myHandler.postDelayed(UpdateSongTime, 100);

                            tvRecordedAudio.setText("Pause Recorded Audio");
                        }
                    }
                });
            }
        }
    }

    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            tvStartTime.setText(Utility.milliSecondsToTimer(mediaPlayer.getCurrentPosition()));
            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            myHandler.postDelayed(this, 100);
            if (tvStartTime.getText().toString().equals(tvEndTime.getText().toString())) {
                tvRecordedAudio.setText("Play Recorded Audio");
            }
        }
    };
}