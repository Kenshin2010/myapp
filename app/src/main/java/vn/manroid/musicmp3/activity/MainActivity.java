package vn.manroid.musicmp3.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import vn.manroid.musicmp3.R;
import vn.manroid.musicmp3.model.Song;
import vn.manroid.musicmp3.service.PlayerService;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener,SeekBar.OnSeekBarChangeListener{

    private Intent intent;
    private MediaPlayer mediaPlayer;
    private TextView txtSongName,txtSongSinger,txtTimerEnd,txtTimerStart;
    private ImageView btnPlay;
    private SeekBar seekBar;
    private Song song;
    private boolean isPlay = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        initView();

        intent = new Intent(this, PlayerService.class);
        startService(intent);

        IntentFilter filter = new IntentFilter("SendDuration");
        filter.addAction("SendProgress");
        LocalBroadcastManager.getInstance(this).registerReceiver(createMessageReceiver(), filter);

    }

    private void initView(){
        song = new Song("Eco Mobile songName","Eco Mobile songSinger","http://images.shulcloud.com/719/uploads/Icons/song.png","http://c34.vdc.nixcdn.com/2eb7126bc4bb413994fe18dbc8bb59ea/594ce38a/NhacCuaTui884/CamOnViTatCa-AnhQuanIdol-3754007.mp3");
        txtSongName = (TextView)findViewById(R.id.txtSongName);
        txtSongSinger = (TextView) findViewById(R.id.txtSongSinger);
        txtTimerStart = (TextView) findViewById(R.id.txtTimerStart);
        txtTimerEnd = (TextView) findViewById(R.id.txtTimerEnd);
        seekBar = (SeekBar) findViewById(R.id.seekbar);

        txtSongName.setText(song.getSongName());
        txtSongSinger.setText(song.getSongSinger());

        btnPlay = (ImageView) findViewById(R.id.btnPlay);


        btnPlay.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(this);
    }

    @Override
    public void onDestroy() {
//        stopService(intent);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        changeStatusMedia();
    }

    private void changeStatusMedia() {
        Intent intent = new Intent("ChangeStatusMedia");
        isPlay = !isPlay;
        btnPlay.setBackgroundResource(isPlay ? R.drawable.pause : R.drawable.play);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        sendCurrentPositionToService(seekBar.getProgress());
    }

    private BroadcastReceiver createMessageReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case "SendDuration":
                        int duration = intent.getIntExtra("duration", 0);
                        String endTime = String.format("%02d:%02d"
                                , TimeUnit.MILLISECONDS.toMinutes(duration)
                                , TimeUnit.MILLISECONDS.toSeconds(duration)
                                        - TimeUnit.MINUTES
                                        .toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));

                        txtTimerEnd.setText(endTime);
                        break;

                    case "SendProgress":
                        int progress = intent.getIntExtra("progress",0);
                        int currentPosition = intent.getIntExtra("currentPosition",0);

                        seekBar.setProgress(progress);

                        String startTime = String.format("%02d:%02d"
                                , TimeUnit.MILLISECONDS.toMinutes(currentPosition)
                                , TimeUnit.MILLISECONDS.toSeconds(currentPosition)
                                        - TimeUnit.MINUTES
                                        .toSeconds(TimeUnit.MILLISECONDS.toMinutes(currentPosition)));
                        txtTimerStart.setText(startTime);

                        break;
                }
            }
        };
    }

    private void sendCurrentPositionToService(int currentPosition){
        Intent intent = new Intent("SeekChange");
        intent.putExtra("currentPosition",currentPosition);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
