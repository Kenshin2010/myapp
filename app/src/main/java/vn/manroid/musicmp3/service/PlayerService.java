package vn.manroid.musicmp3.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import java.io.File;

public class PlayerService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private String url = "http://c34.vdc.nixcdn.com/2eb7126bc4bb413994fe18dbc8bb59ea/594ce38a/NhacCuaTui884/CamOnViTatCa-AnhQuanIdol-3754007.mp3";
    private MediaPlayer mediaPlayer;

    public PlayerService() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        playAudio();
        IntentFilter filter = new IntentFilter("ChangeStatusMedia");
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver(), filter);

        return super.onStartCommand(intent, flags, startId);
    }

    private BroadcastReceiver receiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case "ChangeStatusMedia":
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.pause();
                        } else {
                            mediaPlayer.start();
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

    private void playAudio(){
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
        }catch (Exception e){
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
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {

    }

    @Override
    public void onDestroy()
    {
        stopSelf();
        super.onDestroy();
    }
}
