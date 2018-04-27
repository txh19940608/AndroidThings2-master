package com.example.androidthingssmartassistant;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.bumptech.glide.Glide;
import com.example.androidthingssmartassistant.skills.BingImageSearch;
import com.example.androidthingssmartassistant.skills.Openweather;
import com.example.androidthingssmartassistant.skills.SpotifyMusic;
import com.example.androidthingssmartassistant.skills.Todoist;
import com.example.androidthingssmartassistant.skills.YandexTranslator;
import com.google.gson.JsonElement;
import com.spotify.sdk.android.player.Metadata;
import com.spotify.sdk.android.player.PlayerEvent;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import ai.api.model.AIResponse;
import ai.api.model.Result;
import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;
import ru.yandex.speechkit.Error;
import ru.yandex.speechkit.SpeechKit;



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

    private static final String TAG = MainActivity.class.getSimpleName();
    private Openweather openweather = new Openweather();
    private BingImageSearch bingImageSearch =new BingImageSearch();
    private DateFormat utcDateFormat = new SimpleDateFormat("EEE dd MMM yyyy:mm:ss");
    private ImageView iv1, iv2, iv3, ivListen, ivAlbumCover, ivPlayPause, ivPreviousTrack, ivNextTrack, ivChangeTrack, ivRepeatTrack, ivShuffleTracks, ivVolume;
    private TextView tvSpeeck, tvTime, tvArtistTrack;
    private static AsyncTask<Void, Void, Void> atWake;

    private ProgressBar pbMain;
    private TextSwitcher tsMain;
    private SeekBar sbVolume;
    private YandexTranslator yandexTranslator = new YandexTranslator();
    private Todoist todoist = new Todoist();

    //Pocketsphinx:进行唤醒词识别
    private YandexRecognitionListener recognitionListener;
    private SpeechRecognizer wakeWordRecognizer;
    private ApiAi apiAi=new ApiAi();


    private AudioManager audioManager;
    private SpotifyMusic spotifyMusic;
    public static int volume;

    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private static final int PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 2;

    private Polly polly;
    private static final String YANDEX_SPEECHKIT_KEY="d6fdce9c-e191-40e0-97b7-2dbb6245eee6";
    private Assets assets;
    private File assetsDir;
//    private Lights lights;

    /* Named searches allow to quickly reconfigure the decoder */
    private static final String WAKEUP = "wakeup";
    /* Keyword we are looking for to activate menu */
    private static final String KEYPHRASE = "oh mighty computer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvSpeeck = findViewById(R.id.tvSpeech);
        tvTime = findViewById(R.id.tvTime);
        tvArtistTrack = findViewById(R.id.tvArtistTrack);
        pbMain = findViewById(R.id.pbMain);
        tsMain = findViewById(R.id.tsMain);
        tsMain.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                //这里 用来创建内部的视图，这里创建TextView，用来显示文字
                TextView tv = new TextView(MainActivity.this);
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

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
        }
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        }

        //initialize();
        // spotifyMusic = new SpotifyMusic(){}


        spotifyMusic = new SpotifyMusic(this, new SpotifyMusic.PlaybackEventListener() {
            @Override
            public void onPlaybackEvent(PlayerEvent playerEvent, Metadata metadata) {
                //Handle event type as necessary
                //根据需要处理事件类型
                switch (playerEvent) {
                    //后退播放通知
                    case kSpPlaybackNotifyPlay:
                        //暂停播放
                        ivPlayPause.setImageDrawable(getDrawable(R.drawable.ic_action_pause));
                        //设置标签
                        ivPlayPause.setTag(1);
                        break;
                    case kSpPlaybackNotifyPause:
                        ivPlayPause.setImageDrawable(getDrawable(R.drawable.ic_action_play));
                        //设置标签
                        ivPlayPause.setTag(0);
                        break;
                        //重复(开)
                    case kSpPlaybackNotifyRepeatOn:
                        ivRepeatTrack.setTag(0);
                        ivRepeatTrack.setColorFilter(getColor(R.color.colorAccent));
                        break;
                        //重复(关)
                    case kSpPlaybackNotifyRepeatOff:
                        ivRepeatTrack.setTag(1);
                        ivRepeatTrack.setColorFilter(Color.parseColor("#9e9e9e"));
                        break;
                        //拖曳(开)
                    case kSpPlaybackNotifyShuffleOn:
                        ivShuffleTracks.setTag(1);
                        ivShuffleTracks.setColorFilter(getColor(R.color.colorAccent));
                        break;
                       //拖曳(关)
                    case kSpPlaybackNotifyShuffleOff:
                        ivShuffleTracks.setTag(0);
                        ivRepeatTrack.setColorFilter(Color.parseColor("#9e9e9e"));
                        break;
                        //下一页
                    case kSpPlaybackNotifyNext:
                        //上一页
                    case kSpPlaybackNotifyPrev:
                        //数据已更改
                    case kSpPlaybackNotifyMetadataChanged:
                        //艺术家跟踪
                        try {
                            tvArtistTrack.setText(metadata.currentTrack.artistName + "-" + metadata.currentTrack.name);
                            Glide.with(MainActivity.this).load(metadata.currentTrack.albumCoverWebUrl).into(ivAlbumCover);
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        break;
                }
            }
        });
        //该类提供访问控制音量和钤声模式的操作。
        audioManager= (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //声音类型（音乐）
        volume=audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        sbVolume=findViewById(R.id.sbVolume);
        //获得当前音乐最大声。
        sbVolume.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        //取得当前音乐的音量
        sbVolume.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        //
        sbVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                volume=i;
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,volume,0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        findViewById(R.id.btnExit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        polly=new Polly(this);
        //lights = new Lights(this);
        recognitionListener= new YandexRecognitionListener(this);

        //AlarmManager一种系统级别的提示服务
        AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        am.setTimeZone("GMT+08:00");
        //日历
        Calendar now = Calendar.getInstance();
        String hours = now.get(Calendar.HOUR_OF_DAY) >= 10 ? String.valueOf(now.get(Calendar.HOUR_OF_DAY)) : "0" +String.valueOf(now.get(Calendar.HOUR_OF_DAY));
        String minutes = now.get(Calendar.MINUTE) >= 10 ? String.valueOf(now.get(Calendar.MINUTE)) : "0" +String.valueOf(now.get(Calendar.MINUTE));
        tvTime.setText(hours + ":"+ minutes);

        final Handler timeHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                Calendar now = Calendar.getInstance();
                String hours = now.get(Calendar.HOUR_OF_DAY) >= 10 ? String.valueOf(now.get(Calendar.HOUR_OF_DAY)) : "0" +String.valueOf(now.get(Calendar.HOUR_OF_DAY));
                String minutes = now.get(Calendar.MINUTE) >= 10 ? String.valueOf(now.get(Calendar.MINUTE)) : "0" +String.valueOf(now.get(Calendar.MINUTE));
                tvTime.setText(hours + ":"+ minutes);
            }
        };
        //Timer类主要用于定时性、周期性任务 的触发
        //scheduleAtFixedRate
        // ；如果第一次执行时间被delay了，
        // 随后的执行时间按照 上一次开始的 时间点 进行计算，
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timeHandler.sendMessage(new Message());
            }
        },0,60*1000);

        iv1 =findViewById(R.id.iv1);
        iv2 =findViewById(R.id.iv2);
        iv3 =findViewById(R.id.iv3);

        //Album Cover:专辑封面
        ivAlbumCover=findViewById(R.id.ivAlbumCover);
        ivAlbumCover.setElevation(8);

        ivListen=findViewById(R.id.ivListen);
        //Volume:音量
        ivVolume=findViewById(R.id.ivVolume);

        //Previous Track:上一曲目
        ivPreviousTrack=findViewById(R.id.ivPreviousTrack);
        ivPreviousTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                spotifyMusic.control("previous");
            }
        });

        //ivNextTrack:下一曲目
        ivNextTrack=findViewById(R.id.ivNextTrack);
        ivNextTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                spotifyMusic.control("next");
            }
        });
        //ivPlayPause:播放暂停
        ivPlayPause=findViewById(R.id.ivPlayPause);
        ivPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch ((int)v.getTag()) {
                  //恢复
                    case 0:
                    spotifyMusic.control("resume");
                    break;
                    //暂停
                    case 1:
                        spotifyMusic.control("pause");
                        break;
                }
            }
        });
        //曲目更改跟踪
        ivChangeTrack=findViewById(R.id.ivChangeTrack);
        ivChangeTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                spotifyMusic.playRandom();
                if((int)ivPlayPause.getTag() ==0)
                    //暂停
                    spotifyMusic.control("pause");
            }
        });

        //Repeat Track:重复曲目
        ivRepeatTrack  = findViewById(R.id.ivRepeatTrack);
        ivRepeatTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //重复
                spotifyMusic.control("repeat",ivRepeatTrack.getTag()==null ||((int)ivRepeatTrack.getTag())==0);
            }
        });

        //随机曲目
        ivShuffleTracks = findViewById(R.id.ivShuffleTracks);
        ivShuffleTracks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            //拖曳
            spotifyMusic.control("shuffle",ivShuffleTracks.getTag()==null ||((int)ivShuffleTracks.getTag())==0);
            }
        });
    }

    /***
     * 从Android 6.0开始, 用户需要在运行时请求权限
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            //请求录制音频权限
            case PERMISSIONS_REQUEST_RECORD_AUDIO:
                //grantResults:授予结果
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
                     ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
                    else
                        initialize();
                }else
                    finish();
                break;
            case PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION:
                if(grantResults.length >0 &&  grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)
                      ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},PERMISSIONS_REQUEST_RECORD_AUDIO);
                    else
                        initialize();
                }else
                    finish();
                break;
        }
    }

    /**
     * 初始化
     */
    private void initialize(){

        new AsyncTask<Void, Void, Exception>() {

            //在后台任务即 doInBackground(Params…) 执行前被调用，
            // 一般用于初始化某些值，例如可以是在交互页面上显示一个 ProgressBar
            // 来提示将要进行后台任务
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pbMain.setVisibility(View.VISIBLE);
                tsMain.setText("Loading");


            }

            @Override
            protected Exception doInBackground(Void... voids) {
                // SpeechKit:语音识别
                SpeechKit.getInstance().configure(MainActivity.this,YANDEX_SPEECHKIT_KEY);
                try {
                    //资源文件:加载assets目录下音乐
                    assets =new Assets(MainActivity.this);
                    //同步资源文件
                    assetsDir = assets.syncAssets();
                    //wake Word Recognizer:访问解码器功能的主要类
                    wakeWordRecognizer = SpeechRecognizerSetup.defaultSetup()
                                        //声模式
                                         .setAcousticModel(new File(assetsDir,"en-us-ptm"))
                                         //字典
                                         .setDictionary(new File(assetsDir,"cmudict-en-us.dict"))
                                         .setRawLogDir(assetsDir)
                                         //Raw Log Dir:原始日志目录
                                         .getRecognizer();
                    wakeWordRecognizer.addListener(MainActivity.this);
                    wakeWordRecognizer.addKeyphraseSearch(WAKEUP, KEYPHRASE);

                } catch (IOException e) {
                    e.printStackTrace();
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception e) {
                super.onPostExecute(e);
               // 在doInBackground完成之后调用，一般是设置结果，取消第一个方法显示的进度条。
                if(e != null){
                    tsMain.setText("Initialization error");
                    tvSpeeck.setText(e.getMessage());
                }else {
                    //rfid.setRfidListener(MainActivity.this, MainActivity.this);
                    tsMain.setText("");
                    pbMain.setVisibility(View.GONE);
                    ivPreviousTrack.setVisibility(View.VISIBLE);
                    ivPlayPause.setVisibility(View.VISIBLE);
                    ivNextTrack.setVisibility(View.VISIBLE);
                    ivChangeTrack.setVisibility(View.VISIBLE);
                    ivRepeatTrack.setVisibility(View.VISIBLE);
                    ivShuffleTracks.setVisibility(View.VISIBLE);
                    ivVolume.setVisibility(View.VISIBLE);
                    sbVolume.setVisibility(View.VISIBLE);
                    findViewById(R.id.controlsDivider).setVisibility(View.VISIBLE);
                    spotifyMusic.playRandom();
                    spotifyMusic.control("next");
                    spotifyMusic.control("pause");
                    atWake=new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected void onPreExecute() {
                            wakeWordRecognizer.stop();
                            wakeWordRecognizer.startListening(WAKEUP);
                        }

                        @Override
                        protected Void doInBackground(Void... voids) {
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            //加载动画
                            tvSpeeck.startAnimation(AnimationUtils.loadAnimation(MainActivity.this,R.anim.fade_out));
                            tvSpeeck.setText("");
                        }
                    };
                    //听唤醒词
                    listenToWakeWord();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    //PocketSphinx wake word listener
    // PocketSphinx唤醒词收听者
    private void listenToWakeWord(){
        atWake.cancel(false);
        atWake.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    //Yandex speech listener
    private void listenToSpeech(YandexRecognitionListener.SpeechListener listener){
        wakeWordRecognizer.stop();
        recognitionListener.recognize(listener);
        ivListen.setVisibility(View.VISIBLE);
        ivListen.startAnimation(AnimationUtils.loadAnimation(MainActivity.this,R.anim.fade_in));
        tvSpeeck.setText("");
        tsMain.setText("");
        iv1.setVisibility(View.GONE);
        iv2.setVisibility(View.GONE);
        iv3.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        if(wakeWordRecognizer!=null){
            wakeWordRecognizer.cancel();
            //shutdown:关掉
            wakeWordRecognizer.shutdown();
        }
        spotifyMusic.destroy();
        super.onDestroy();
    }

    @Override
    public void onBeginningOfSpeech() {
        if(wakeWordRecognizer.getSearchName().equals(WAKEUP))
            listenToWakeWord();


    }

    @Override
    public void onEndOfSpeech() {

    }

    /**
     * onPartialResult
     * 部分结果
     * @param hypothesis
     */

    @Override
    public void onPartialResult(Hypothesis hypothesis) {

        if(hypothesis !=null && hypothesis.getHypstr().equals(KEYPHRASE)){
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) / 2,0);
            listenToSpeech(new YandexRecognitionListener.SpeechListener() {
                @Override
                public void onRecognitionDone(Error error, String speech) {
                    ivListen.startAnimation(AnimationUtils.loadAnimation(MainActivity.this,R.anim.fade_out));
                    ivListen.setVisibility(View.GONE);
                    if(error !=null){
                        tsMain.setText("Error  Yandex recognizer");
                        tvSpeeck.setText(error.getString());
                        return;
                    }
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,volume,0);
                    listenToWakeWord();
                    if(speech != null && speech.length()>0){
                        doAction(apiAi.makeRequest(speech));
                        //Character:字符,
                        // to Up per Case:以每个案例来增加
                        tvSpeeck.setText(Character.toUpperCase(speech.charAt(0)) + speech.substring(1));
                    }
                }
            });
        }
    }

    /**
     * 根据api.ai响应做一些动作
     * @param response
     */
    private void doAction(AIResponse response) {
        if(response !=null){
            final Result result =response.getResult();
            final String speech = result.getFulfillment().getSpeech();
            final HashMap<String,JsonElement> params=result.getParameters();

            if(params != null && !params.isEmpty()){
                //Entry:条目
                for(final Map.Entry<String,JsonElement> entry : params.entrySet()){
                    Log.d(TAG,entry.getKey() + " : " + entry.getValue().toString());
                }
            }
            //todo
            if(speech != null && !speech.equals("") && ((params.get("simplified") != null && !params.get("simplified").getAsString().equals("one-color")) || params.get("simplified") ==null)){
                Log.d(TAG,"oAction: SPEECH");
                tsMain.setText(speech);
                polly.say(speech, Polly.Lang.Ru, new Polly.SpeechEndListener() {
                    @Override
                    public void onSpeechEnd() {
                        tsMain.setText("");
                    }
                });
                return;
            }
            //AIResponse result
            if(result.getMetadata() !=null && result.getMetadata().getIntentName() !=null){
                final String intent = result.getMetadata().getIntentName();
                Log.d(TAG,"intent："  + intent);
                switch (intent){
                    case "light":
                        try {
                          //  lights.change(params.get("light").getAsString(), params.get("onoff") == null ? null : params.get("onoff").getAsString());
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                        break;


/*                    case "wiki":
                        try {
                            String answer = (wiki.getAnswer(params.get("query").getAsString()));
                            if (answer != null && !answer.equals("")) {
                                polly.say(answer, Polly.Lang.Ru);
                                tsMain.setText(Character.toUpperCase(answer.charAt(0)) + answer.substring(1));
                            } else {
                                answer = wiki.getAnswer(yandexTranslator.translate(params.get("query").getAsString(), "en"));
                                if (answer != null && !answer.equals("")) {
                                    polly.say(answer, Polly.Lang.Ru);
                                    tsMain.setText(Character.toUpperCase(answer.charAt(0)) + answer.substring(1));
                                } else {
                                    polly.say("I dont know", Polly.Lang.Ru);
                                    tsMain.setText("I dont know");
                                }
                            }
                        } catch (NullPointerException e) {
                            tsMain.setText("Ошибка wiki");
                            e.printStackTrace();
                        }
                        break;*/

                    case "translate":
                        String translated = yandexTranslator.translate(params.get("query").getAsString(), params.get("language").getAsString());
                        switch (params.get("language").getAsString()) {
                            case "ru":
                                polly.say(translated, Polly.Lang.Ru);
                                break;
                            case "en":
                                polly.say(translated, Polly.Lang.En);
                                break;
                            case "ja":
                                polly.say(translated, Polly.Lang.Ja);
                                break;
                        }
                        tsMain.setText(translated);
                        break;

                    case "time":
                        polly.say(tvTime.getText().toString(),Polly.Lang.Ru);
                        break;

                    case "weather":
                        String weatherResult = openweather.getCurrentWeather();
                        polly.say(weatherResult,Polly.Lang.Ru);
                        tsMain.setText(weatherResult);
                        break;
                    case "imageSearch":
                        if(params.get("query").getAsString() ==null)
                            return;
                        iv1.setVisibility(View.VISIBLE);
                        iv2.setVisibility(View.VISIBLE);
                        iv3.setVisibility(View.VISIBLE);
                        List<String> urls = bingImageSearch.getUrls(params.get("query").getAsString());
                        if(urls.size()>3){
                            int first = new Random().nextInt(urls.size());
                            int second,third;
                            do{
                                second = new Random().nextInt(urls.size());
                            }while (second == first);
                            do{
                                third =new Random().nextInt(urls.size());
                            }while (third ==first || third ==second);
                            Glide.with(MainActivity.this).load(urls.get(first)).into(iv1);
                            Glide.with(MainActivity.this).load(urls.get(new Random().nextInt(urls.size()))).into(iv2);
                            Glide.with(MainActivity.this).load(urls.get(new Random().nextInt(urls.size()))).into(iv3);
                        }else {
                            String err = "I do not know what it looks like";
                            polly.say(err,null);
                            tsMain.setText(err);
                        }break;
                    case "tasks.get":
                        //desire:期望
                        final String desiredDate=params.get("date") == null ? null:params.get("date").getAsString();
                        final String desiredTask=params.get("task") == null ? null:params.get("task").getAsString();

                        new AsyncTask<Void, Void, String>() {

                            @Override
                            protected void onPreExecute() {
                                super.onPreExecute();
                                tvSpeeck.setVisibility(View.GONE);
                                pbMain.setVisibility(View.VISIBLE);
                            }

                            @Override
                            protected String doInBackground(Void... Params) {
                                StringBuilder sb = new StringBuilder();
                                List<JSONObject> tasks = todoist.getTasks(desiredDate,desiredTask);
                                if(tasks == null)
                                return null;

                                String[] numerals ={"First", "Second", "Third", "Fourth", "Fifth", "Sixth", "Seventh", "Eighth", "Ninth", "Tenth"};
                              try {
                                if(desiredTask !=null && !desiredTask.equals("") && !desiredTask.equals(" ")) {
                                    if (tasks.size() == 1) {
                                        JSONObject task = tasks.get(0);
                                        Calendar itemDate = Calendar.getInstance();
                                        itemDate.setTime(utcDateFormat.parse(task.getString("due_date_utc").replace("  0000", "")));

                                        //没有检查错误的常态
                                        if (itemDate.get(Calendar.HOUR_OF_DAY) == 20 && itemDate.get(Calendar.MINUTE) == 59) {
                                            sb.append("Specific time not assigned");
                                        } else {

                                            sb.append("В ");
                                            sb.append(itemDate.get(Calendar.HOUR_OF_DAY) + 3 >= 10 ? itemDate.get(Calendar.HOUR_OF_DAY) + 3 : "0" + itemDate.get(Calendar.HOUR_OF_DAY) + 3);
                                            sb.append(":");
                                            sb.append(itemDate.get(Calendar.MINUTE) >= 10 ? itemDate.get(Calendar.MINUTE) : "0" + itemDate.get(Calendar.MINUTE));
                                        }
                                    } else if (tasks.size() > 1) {
                                        //Collections是一个工具类，sort是其中的静态方法，是用来对List类型进行排序的
                                        Collections.sort(tasks, new Comparator<JSONObject>() {
                                            Calendar c1 = Calendar.getInstance();
                                            Calendar c2 = Calendar.getInstance();


                                            @Override
                                            public int compare(JSONObject o1, JSONObject o2) {
                                                try {
                                                    c1.setTime(utcDateFormat.parse(o1.getString("due_date_utc").replace("  0000", "")));
                                                    c2.setTime(utcDateFormat.parse(o2.getString("due_date_utc").replace("  0000", "")));

                                                    if (c1.get(Calendar.DAY_OF_MONTH) > c2.get(Calendar.DAY_OF_MONTH))
                                                        return 1;
                                                    else if (c1.get(Calendar.DAY_OF_MONTH) < c2.get(Calendar.DAY_OF_MONTH))
                                                        return -1;
                                                    else
                                                        return 0;
                                                } catch (ParseException e) {
                                                    e.printStackTrace();
                                                    return 0;
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                    return 0;
                                                }
                                            }
                                        });

                                        int tasksWithoutTime = 0;
                                        String[] notime = {"but no specific time is assigned", "but time is not set again","there is no time again"};
                                        for (JSONObject task : tasks) {
                                            Calendar itemDate = Calendar.getInstance();
                                            Calendar now = Calendar.getInstance();
                                            itemDate.setTime(utcDateFormat.parse(task.getString("due_date_utc").replace("  0000", "")));

                                            //noinspection WrongConstant
                                            if (itemDate.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)) {
                                                sb.append("Today");
                                            } else if (itemDate.get(Calendar.DAY_OF_YEAR) - now.get(Calendar.DAY_OF_YEAR) == 1) {
                                                sb.append("Tomorrow");
                                            } else if (itemDate.get(Calendar.DAY_OF_YEAR) - now.get(Calendar.DAY_OF_YEAR) == 2) {
                                                sb.append("Day after tomorrow");
                                            } else {
                                                sb.append(itemDate.get(Calendar.DAY_OF_MONTH));
                                                sb.append(" ");
                                                sb.append(ruMothsFormatSymbols.getMonths()[itemDate.get(Calendar.MONTH)]);
                                            }

                                            //noinspection WrongConstant
                                            if (itemDate.get(Calendar.HOUR_OF_DAY) == 20 && itemDate.get(Calendar.MINUTE) == 59) {
                                                sb.append(", ");
                                                try {
                                                    sb.append(notime[tasksWithoutTime++]);
                                                } catch (IndexOutOfBoundsException e) {
                                                    sb.append("time is not set");
                                                    e.printStackTrace();
                                                } finally {
                                                    sb.append(". ");
                                                }
                                            } else {
                                                sb.append(" в ");
                                                sb.append(itemDate.get(Calendar.HOUR_OF_DAY) + 3 >= 10 ? itemDate.get(Calendar.HOUR_OF_DAY) + 3 : "0" + itemDate.get(Calendar.HOUR_OF_DAY) + 3);
                                                sb.append(":");
                                                sb.append(itemDate.get(Calendar.MINUTE) >= 10 ? itemDate.get(Calendar.MINUTE) : "0" + itemDate.get(Calendar.MINUTE));
                                                sb.append(". ");
                                            }
                                        }
                                    }
                                } else {
                                    if (tasks.size() == 1) {
                                        JSONObject task = tasks.get(0);
                                        Calendar itemDate = Calendar.getInstance();
                                        itemDate.setTime(utcDateFormat.parse(task.getString("due_date_utc").replace("  0000", "")));
                                        sb.append(task.getString("content"));

                                        //noinspection WrongConstant
                                        if (itemDate.get(Calendar.HOUR_OF_DAY) != 20 && itemDate.get(Calendar.MINUTE) != 59) {
                                            sb.append(" в ");
                                            sb.append(itemDate.get(Calendar.HOUR_OF_DAY) + 3 >= 10 ? itemDate.get(Calendar.HOUR_OF_DAY) + 3 : "0" + itemDate.get(Calendar.HOUR_OF_DAY) + 3);
                                            sb.append(":");
                                            sb.append(itemDate.get(Calendar.MINUTE) >= 10 ? itemDate.get(Calendar.MINUTE) : "0" + itemDate.get(Calendar.MINUTE));
                                        }
                                    } else {
                                        int i = 0;
                                        for (JSONObject task : tasks) {
                                            try {
                                                sb.append(numerals[i++]);
                                            } catch (NullPointerException e) {
                                                sb.append("Then");
                                                e.printStackTrace();
                                            }
                                            sb.append(" - ");
                                            String splittedTask[] = task.getString("content").split(".");
                                            if (splittedTask.length == 0) {
                                                sb.append(task.getString("content"));
                                            } else if (splittedTask[0].length() > 3) {
                                                sb.append(splittedTask[0]);
                                            } else {
                                                sb.append(splittedTask[0]);
                                                sb.append(".");
                                                sb.append(splittedTask[1]);
                                            }
                                            sb.append(". ");
                                        }
                                    }
                                }
                              } catch (JSONException | ParseException e) {
                                  e.printStackTrace();
                                  return null;
                              }
                                Log.d(TAG, sb.toString());
                                return sb.toString();
                            }

                            @Override
                            protected void onPostExecute(String tasks) {
                                super.onPostExecute(tasks);
                                pbMain.setVisibility(View.GONE);
                                tvSpeeck.setVisibility(View.VISIBLE);
                                if (tasks != null && !tasks.equals("") && !tasks.equals(" ")) {
                                    polly.say(tasks, Polly.Lang.Ru);
                                    tsMain.setText(tasks);
                                } else {
                                    tsMain.setText("Нет задач");
                                }
                            }

                        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        break;

                    case "tasks.add":

                        final String task = params.get("task") == null ? null : Character.toUpperCase(params.get("task").getAsString().charAt(0)) + params.get("task").getAsString().substring(1);
                        final String date = params.get("date") == null ? null : params.get("date").getAsString();
                        final String time = params.get("time") == null ? null : params.get("time").getAsString();
                        if (todoist.addTask(task, date, time)) {
                            String success = "The task has been added";
                            polly.say(success, Polly.Lang.Ru);
                            tsMain.setText(success);
                        } else {
                            String error = "Can not add task";
                            polly.say(error, Polly.Lang.Ru);
                            tsMain.setText(error);
                        }
                        break;

                    case "music.playlist":
                        //

                }
            }
        }
    }

    @Override
    public void onResult(Hypothesis hypothesis) {

    }

    @Override
    public void onError(Exception e) {

        tsMain.setText("Error PocketSphinx");
        //e.getMessage()会获得异常的名称。
        tvSpeeck.setText(e.getMessage());
        listenToWakeWord();

    }

    @Override
    public void onTimeout() {
        listenToWakeWord();
    }

    private static DateFormatSymbols ruMothsFormatSymbols = new DateFormatSymbols(){
        @Override
        public String[] getMonths() {
            return new String [] {"January", "February", "March", "April", "May", "June",
                    "July", "August", "September", "October", "November", "December"};
        }
    };
}
