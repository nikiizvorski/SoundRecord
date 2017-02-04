package com.nikiizvorski.soundrecord;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.os.Messenger;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class PlayerService extends Service {
    private static final String TAG = PlayerService.class.getSimpleName();
    public Messenger mMessenger = new Messenger(new PlayerHandler(this));
    private MediaRecorder mRecorder;
    private String mFileName = null;
    private String mFilePath = null;
    private int mElapsedSeconds = 0;
    String pich = null;
    private long mElapsedMillis;
    private TimerTask mIncrementTimerTask = null;
    private static final SimpleDateFormat mTimerFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());
    private OnTimerChangedListener onTimerChangedListener = null;
    private boolean mRecorderPlaying;

    public interface OnTimerChangedListener {
        void onTimerChanged(int seconds);
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStart");
        return Service.START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return mMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        //mPlayer.release();

        if (mRecorder != null) {
            Log.e(TAG, "onDestroy Null");
            stopRecording();
        }
    }

    // Client Methods
    public boolean isPlaying() {
        Log.d(TAG, "isPlaying");
        return mRecorderPlaying;
    }

    public void play() {
        Log.d(TAG, "StartPlay");
//        mPlayer.start();
        startRecording();
    }

    public void pause() {
        Log.d(TAG, "PausePlay");
        //mPlayer.pause();
        stopRecording();
    }

    public void startRecording() {
        mRecorderPlaying = true;
        setFileNameAndPath();

        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setOutputFile(mFilePath);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setAudioChannels(1);

        try {
            mRecorder.prepare();
            mRecorder.start();

            startTimer();

        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }
    }

    public void setFileNameAndPath(){
        final String theData = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SoundRecorder/";

        int count = 0;
        File f;

        do{
            count++;

            mFileName = "Recording"
                    + " #" + count + ".mp4";
            mFilePath = theData + mFileName;

            Log.d(TAG, "TAGDATA " + mFilePath);
            f = new File(mFilePath);
        }while (f.exists() && !f.isDirectory());
    }

    public void stopRecording() {
        mRecorderPlaying = false;
        mRecorder.stop();
        mElapsedSeconds = 0;
        mRecorder.release();
        Toast.makeText(this, "Rec Finish" + " " + mFilePath, Toast.LENGTH_LONG).show();

        //remove notification
        if (mIncrementTimerTask != null) {
            mIncrementTimerTask.cancel();
            mIncrementTimerTask = null;
        }

        mRecorder = null;
        stopForeground(true);
    }

    public void startTimer() {
        Timer mTimer = new Timer();
        mIncrementTimerTask = new TimerTask() {
            @Override
            public void run() {
                mElapsedSeconds++;
                if (onTimerChangedListener != null)
                    onTimerChangedListener.onTimerChanged(mElapsedSeconds);
                startForeground(1, createNotification());
                sendMessage(getApplicationContext(), mElapsedSeconds);
            }
        };
        mTimer.scheduleAtFixedRate(mIncrementTimerTask, 1000, 1000);
    }

    public void cancelNotification(int notifyId) {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) getApplicationContext().getSystemService(ns);
        nMgr.cancel(notifyId);
    }

    public Notification createNotification() {
        NotificationCompat.Builder mBuilder;

        if (pich != null && pich.equals("da")) mBuilder = new NotificationCompat.Builder(getApplicationContext())
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Tap here to stop recording")
                .setContentText(mTimerFormat.format(mElapsedSeconds * 1000))
                .setAutoCancel(true)
                .setOngoing(true);

        else mBuilder = new NotificationCompat.Builder(getApplicationContext())
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Recording")
                .setContentText(mTimerFormat.format(mElapsedSeconds * 1000))
                .setAutoCancel(true)
                .setOngoing(true);



        mBuilder.setContentIntent(PendingIntent.getActivities(getApplicationContext(), 0,
                new Intent[]{new Intent(getApplicationContext(), MainActivity.class)}, 0));

        return mBuilder.build();
    }

    private void sendMessage(Context context, int i) {
        Intent intent = new Intent("com.example.keys.MESSAGE");
        intent.putExtra("time", i);
        // Send Broadcast to Broadcast receiver with message
        context.sendBroadcast(intent);
    }
}
