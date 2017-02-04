package com.nikiizvorski.soundrecord;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private TextView textView;
    private boolean mBound = false;
    private Button mPlayButton;
    private Chronometer chronometer;
    private Messenger mServiceMessenger;
    private Messenger mActivityMessenger = new Messenger(new ActivityHandler(this));
    long timeRunning = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate");
        mPlayButton = (Button) findViewById(R.id.playButton);
        textView = (TextView) findViewById(R.id.textView);
        textView.setText("0");
        chronometer = (Chronometer) findViewById(R.id.chronometer);

        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isStoragePermissionGranted())
                if (mBound) {
                    Log.d(TAG, "BOUND TRUE");
                    Intent intent = new Intent(MainActivity.this, PlayerService.class);
                    startService(intent);
                    Log.d(TAG, "STARTED");
                    Message message = Message.obtain();
                    message.arg1 = 2;
                    message.replyTo = mActivityMessenger;
                    try {
                        mServiceMessenger.send(message);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void changePlayButtonText(String text) {
        mPlayButton.setText(text);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onStartReceiver");
        registerReceiver(mHandleMessageReceiver, new IntentFilter("com.example.keys.MESSAGE"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onStopReceiver");
        unregisterReceiver(mHandleMessageReceiver);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        Intent intent = new Intent(this, PlayerService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        if (mBound) {
            unbindService(mServiceConnection);
            mBound = false;
        }
    }

    public void startChronometer() {
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
    }

    public void startChronometera(int i) {
        chronometer.setBase(SystemClock.elapsedRealtime() - i * 1000);
        chronometer.start();
    }

    public void stopChronometer() {
        timeRunning = 0;
        chronometer.stop();
        chronometer.setBase(SystemClock.elapsedRealtime() - timeRunning * 1000);
        textView.setText("0");
    }

    private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int message = intent.getExtras().getInt("time");
            startChronometera(message);
            textView.setText(String.valueOf(message));
        }
    };


    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Log.d(TAG, "onService Connected");
            mBound = true;
            mServiceMessenger = new Messenger(binder);
            Message message = Message.obtain();
            message.arg1 = 2;
            message.arg2 = 1;
            message.replyTo = mActivityMessenger;
            try {
                mServiceMessenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onService DC");
            mBound = false;
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
            else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, 1);
                return false;
            }
        } else return true;
    }
}
