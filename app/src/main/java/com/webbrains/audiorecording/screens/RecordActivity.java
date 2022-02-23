package com.webbrains.audiorecording.screens;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.webbrains.audiorecording.R;
import com.webbrains.audiorecording.utility.Utility;

import java.io.File;
import java.io.IOException;

public class RecordActivity extends AppCompatActivity {

    TextView tvTime, textView;
    String fileName = "";
    MediaRecorder recorder;

    private long startHTime = 0L;
    private Handler customHandler = new Handler();
    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;
    LinearLayout llStop;
    int step = 1;
    LinearLayout llUndoSave, llUndo, llResume, llSave;

    private Runnable updateTimerThread = new Runnable() {
        public void run() {

            timeInMilliseconds = SystemClock.uptimeMillis() - startHTime;
            updatedTime = timeSwapBuff + timeInMilliseconds;

            int secs = (int) (updatedTime / 1000);
            int mins = secs / 60;
            secs = secs % 60;
            if (tvTime != null)
                tvTime.setText("" + String.format("%02d", mins) + ":" + String.format("%02d", secs));
            customHandler.postDelayed(this, 0);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        tvTime = findViewById(R.id.tvTime);
        llStop = findViewById(R.id.llStop);
        textView = findViewById(R.id.textView);
        llUndoSave = findViewById(R.id.llUndoSave);
        llUndo = findViewById(R.id.llUndo);
        llResume = findViewById(R.id.llResume);
        llSave = findViewById(R.id.llSave);

        setToolbar();

        startRecording();

        setView();

        llStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // pause
                if (recorder != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        recorder.pause();
                        stopHandler();
                        step = 2;
                        setView();
                    }
                }
            }
        });

        llUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBack();
            }
        });

        llResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // resume
                if (recorder != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        recorder.resume();
                        startHandler();
                        step = 1;
                        setView();
                    }
                }
            }
        });

        llSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording();
                Intent i = new Intent();
                i.putExtra("filename", fileName);
                setResult(RESULT_OK, i);
                finish();
            }
        });
    }

    void onBack() {
        Utility.showDialogOK(RecordActivity.this, "Are you sure you want to delete this recording?",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                File f = new File(fileName);
                                if (f.exists()) {
                                    Log.e("file deleted", f.delete() + "");
                                }
                                finish();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        onBack();
    }

    private void startRecording() {
        String timestamp = System.currentTimeMillis() + "";
        fileName = getExternalCacheDir().getAbsolutePath() + "/" + timestamp + ".mp3";
        Log.e(MainActivity.class.getSimpleName(), fileName);

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(MainActivity.class.getSimpleName() + ":startRecording()", "prepare() failed");
        }

        recorder.start();
        startHandler();
    }

    private void startHandler() {
        startHTime = SystemClock.uptimeMillis();
        customHandler.postDelayed(updateTimerThread, 0);
    }

    private void stopHandler() {
        timeSwapBuff += timeInMilliseconds;
        customHandler.removeCallbacks(updateTimerThread);
    }

    private void stopRecording() {
        if (recorder != null) {
            recorder.release();
            recorder = null;
            stopHandler();
        }
    }

    void setView() {
        tvTime.setVisibility(View.INVISIBLE);
        textView.setVisibility(View.GONE);
        llStop.setVisibility(View.GONE);
        llUndoSave.setVisibility(View.GONE);
        if (step == 1) {
            // first time
            tvTime.setVisibility(View.VISIBLE);
            llStop.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.VISIBLE);
            llUndoSave.setVisibility(View.VISIBLE);
        }
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(step == 1 ? "Recording" : "Manage your audio");
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBack();
            }
        });
    }
}