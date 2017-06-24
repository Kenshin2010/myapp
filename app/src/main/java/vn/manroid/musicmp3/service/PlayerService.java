package vn.manroid.musicmp3.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.File;

import vn.manroid.musicmp3.R;

import static android.content.ContentValues.TAG;

public class PlayerService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private String url = "http://c34.vdc.nixcdn.com/2eb7126bc4bb413994fe18dbc8bb59ea/594ce38a/NhacCuaTui884/CamOnViTatCa-AnhQuanIdol-3754007.mp3";
    private MediaPlayer mediaPlayer;
    private int counter = 0;
    private boolean isPause;
    private Object pauseLock;
    private UpdateTimerRunable updateTimerRunable;
    private NotificationManager notificationManager;
    private BroadcastReceiver notificationReceiver;

    public PlayerService() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (mediaPlayer == null) {
            playAudio();
            showNotification();
            IntentFilter filter = new IntentFilter("ChangeStatusMedia");
            filter.addAction("SeekChange");
            LocalBroadcastManager.getInstance(this).registerReceiver(receiver(), filter);

            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnErrorListener(this);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void showNotification() {

        notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case "ChangeStatus":
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.pause();
                            updateTimerRunable.onPause();
//                            stopForeground(true);
//                            notificationManager.cancel(1);
                        } else {
                            mediaPlayer.start();
                            updateTimerRunable.onResume();
                        }

                        break;

                    case "stopAudio":
                        mediaPlayer.pause();
                        updateTimerRunable.onPause();
                        notificationManager.cancelAll();
                        stopSelf();
                        break;
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter("ChangeStatus");
        intentFilter.addAction("stopAudio");
        registerReceiver(notificationReceiver, intentFilter);

        PendingIntent changeStatusIntent =
                PendingIntent.getBroadcast(this, 0, new Intent("ChangeStatus"), PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent stopAudio =
                PendingIntent.getBroadcast(this, 0, new Intent("stopAudio"), PendingIntent.FLAG_UPDATE_CURRENT);


        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("Media Player")
                .setContentText("ECO Mobile")
                .setAutoCancel(true)
                .addAction(R.drawable.pause, "pause/resume", changeStatusIntent)
                .addAction(R.drawable.play, "stop", stopAudio)
                .setSmallIcon(R.drawable.backgroundzing).build();

//        notificationManager.notify(1, notification);

        notification.flags |= Notification.FLAG_ONGOING_EVENT;

        startForeground(1, notification);
    }

    private BroadcastReceiver receiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case "ChangeStatusMedia":
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.pause();
                            updateTimerRunable.onPause();

                        } else {
                            mediaPlayer.start();
                            updateTimerRunable.onResume();
                        }
                        break;
                    case "SeekChange":
                        int currentPosition = intent.getIntExtra("currentPosition", 0);
                        mediaPlayer.seekTo(currentPosition * mediaPlayer.getDuration() / 100);
                        break;
                }
            }
        };
    }

    private void playAudio() {
        File f = new File(url);// /
        if (f.exists() && f.isFile()) {
            return;
        } else {
            playAudioFromURL();
        }
    }

    private void playAudioFromURL() {
        try {

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            mediaPlayer.setDataSource(url);

            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();

            mediaPlayer.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mediaPlayer.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        sendDurationToActivity(mediaPlayer.getDuration());
        createdUpdateTimer();
    }

    @Override
    public void onDestroy() {
        stopSelf();
        super.onDestroy();
    }

    private void createdUpdateTimer() {
        updateTimerRunable = new UpdateTimerRunable();
        new Thread(updateTimerRunable).start();
    }

    class UpdateTimerRunable implements Runnable {

        private boolean isPause;
        private Object pauseLock;

        public UpdateTimerRunable() {
            isPause = false;
            pauseLock = new Object();
        }

        @Override
        public void run() {
            while (mediaPlayer.isPlaying()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                sendDurationToActivity(mediaPlayer.getDuration());

                sendProgressToActivity(mediaPlayer.getCurrentPosition()
                                * 100 / mediaPlayer.getDuration(),
                        mediaPlayer.getCurrentPosition());

                Log.d(TAG, "run: key log");

                synchronized (pauseLock) {
                    while (isPause) {
                        try {
                            pauseLock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        }

        public void onPause() {
            synchronized (pauseLock) {
                isPause = true;
            }
        }

        public void onResume() {
            synchronized (pauseLock) {
                isPause = false;
                pauseLock.notifyAll();
            }
        }
    }

    private void sendProgressToActivity(int progress, int currentPosition) {
        Intent intent = new Intent("SendProgress");
        intent.putExtra("progress", progress);
        intent.putExtra("currentPosition", currentPosition);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private void sendDurationToActivity(int duration) {
        Intent intent = new Intent("SendDuration");
        intent.putExtra("duration", duration);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

}
