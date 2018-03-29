package com.example.androidthingssmartassistant;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.example.androidthingssmartassistant.skills.SpotifyMusic;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;


/**
 * Skeleton of an Android Things activity.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 * <p>
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 */
public class MainActivity extends Activity implements
        RecognitionListener {

    private DateFormat utcDateFormat = new SimpleDateFormat("EEE dd MMM yyyy:mm:ss");
    private ImageView  iv1,iv2,iv3,ivListen,ivAlbumCover,ivPlayPause,ivPreviousTrack,ivNextTrack,ivChangeTrack,ivRepeatTrack,ivShuffleTracks,ivVolume;
    private TextView   tvSpeeck,tvTime,tvArtistTrack;
    private static AsyncTask<Void,Void,Void> atWake;

    private ProgressBar pbMain;
    private TextSwitcher tsMain;


    private SpotifyMusic spotifyMusic;
    public static int volume;
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO =1;
    private static final int PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION=2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvSpeeck = findViewById(R.id.tvSpeech);
        tvTime = findViewById(R.id.tvTime);
        tvArtistTrack=findViewById(R.id.tvArtistTrack);
        pbMain=findViewById(R.id.pbMain);
        tsMain=findViewById(R.id.tsMain);
        tsMain.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                //这里 用来创建内部的视图，这里创建TextView，用来显示文字
                TextView tv=new TextView(MainActivity.this);
                tv.setGravity(Gravity.CENTER);
                tv.setLayoutParams(new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER));
                tv.setTextSize(36);
                tv.setTextColor(Color.BLACK);
                return tv;
            }
        });

        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.RECORD_AUDIO},PERMISSIONS_REQUEST_RECORD_AUDIO);
        }
        if(ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        }

        //initialize();
       // spotifyMusic = new SpotifyMusic(){}

    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {

    }

    @Override
    public void onResult(Hypothesis hypothesis) {

    }

    @Override
    public void onError(Exception e) {

    }

    @Override
    public void onTimeout() {

    }
}
