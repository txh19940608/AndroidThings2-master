package com.example.androidthingssmartassistant;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.polly.AmazonPollyPresigningClient;
import com.amazonaws.services.polly.model.OutputFormat;
import com.amazonaws.services.polly.model.SynthesizeSpeechPresignRequest;

import java.io.IOException;
import java.net.URL;

/**
 * Created by Administrator on 2018/2/13.
 */

public class Polly {

    // Cognito pool ID. Pool needs to be unauthenticated pool with
// Amazon Polly permissions.
    String COGNITO_POOL_ID = "YourCognitoIdentityPoolId";

    private AudioManager audioManager;

    enum Lang{
        Ru("Maxim"),
        En("Joey"),
        Ja("Mizuki");

        private String language;

        Lang(String language) {
            this.language = language;
        }

        public String toString() {
            return language;
        }

    }

    private static final Regions MY_Region = Regions.US_WEST_2;
    private AmazonPollyPresigningClient  client;


    //初始化Amazon Cognito证书提供程序。
    Polly(Context context) {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
       client =new AmazonPollyPresigningClient(new CognitoCachingCredentialsProvider(
                context,
                COGNITO_POOL_ID,
                MY_Region

        ));
    }

//    获取音频流的URL
void say(final String text, final Lang voiceId, final SpeechEndListener... listener) {
        new  Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        //合成语音预定请求
                        //Amazon PollyAmazon Polly=Android Example
                        //Create speech synthesis request.
                        SynthesizeSpeechPresignRequest synthesizeSpeechPresignRequest = new SynthesizeSpeechPresignRequest()
                                .withText(text)
                                .withVoiceId(voiceId.toString())
                                .withOutputFormat(OutputFormat.Mp3);

                        // Get the presigned URL for synthesized speech audio stre
                        URL presignedSynthesizeSpeechUrl=client.getPresignedSynthesizeSpeechUrl(synthesizeSpeechPresignRequest);

                        // Create a media player to play the synthesized audio stream.
                        MediaPlayer mediaPlayer=new MediaPlayer();
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);

                        try {
                            // Set media player's data source to previously obtained URL.
                            mediaPlayer.setDataSource(presignedSynthesizeSpeechUrl.toString());
                        } catch (IOException e) {
                            Log.e("11","Unable to set data source for the media player!"+e.getMessage());
                        }
                        // Prepare the MediaPlayer asynchronously (since the data source is a network stream).
                        mediaPlayer.prepareAsync();

                        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer  mp) {

                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)/2,0);
                                mp.start();
                            }
                        });
                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                mp.release();
                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,MainActivity.volume,0);
                                if(listener.length > 0)
                                   listener[0].onSpeechEnd();
                            }
                        });
                    }
                }).start();
    }

  interface   SpeechEndListener{
      void onSpeechEnd();
  }

}
