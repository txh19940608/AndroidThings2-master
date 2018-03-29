package com.example.androidthingssmartassistant;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;

import ru.yandex.speechkit.Error;
import ru.yandex.speechkit.Recognition;
import ru.yandex.speechkit.Recognizer;
import ru.yandex.speechkit.RecognizerListener;

public class YandexRecognitionListener  implements RecognizerListener{

    private SpeechListener speechListener;
    private Recognizer recognizer;
    private MediaPlayer soundListenStart;
    private MediaPlayer soundListenStop;

    YandexRecognitionListener(Context context){
        soundListenStart=MediaPlayer.create(context,R.raw.listen_start);
        soundListenStart.setAudioStreamType(AudioManager.STREAM_MUSIC);
        soundListenStop=MediaPlayer.create(context,R.raw.listen_stop);
        soundListenStop.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    interface SpeechListener{
        void onRecognitionDone(Error error,String speech);
    }

    void recognize(SpeechListener listener){
        this.speechListener=listener;
        recognizer=Recognizer.create("ru-RU", "general", this);
        recognizer.start();
    }

    @Override
    public void onRecordingBegin(Recognizer recognizer) {
        soundListenStart.start();
    }

    @Override
    public void onSpeechDetected(Recognizer recognizer) {

    }

    @Override
    public void onSpeechEnds(Recognizer recognizer) {

    }

    @Override
    public void onRecordingDone(Recognizer recognizer) {
/*        soundListenStop.start();
      //  speechListener.onRecognitionDone(null);
        recognizer.start();*/
    }

    @Override
    public void onSoundDataRecorded(Recognizer recognizer, byte[] bytes) {

    }

    @Override
    public void onPowerUpdated(Recognizer recognizer, float v) {

    }

    @Override
    public void onPartialResults(Recognizer recognizer, Recognition recognition, boolean b) {

    }

    @Override
    public void onRecognitionDone(Recognizer recognizer, Recognition recognition) {
        soundListenStop.start();
      //  speechListener.onRecognitionDone(null);
        recognizer.start();
    }

    @Override
    public void onError(Recognizer recognizer, Error error) {
        soundListenStop.start();
        speechListener.onRecognitionDone(error,null);

    }
}