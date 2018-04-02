package com.example.androidthingssmartassistant.skills;

/**
 * Created by Administrator on 2018/3/16.
 */
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Metadata;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;


public class SpotifyMusic implements SpotifyPlayer.NotificationCallback,ConnectionStateCallback{

    private static final String TAG =SpotifyMusic.class.getSimpleName();
    private static final String   SPOTIFY_ID ="b5d08d5d0be74c1d948f853b891a7adc";
    private static final String   SPOTIFY_SECRET ="d1b1e38c810c4fddbbbcfe7a0bffd78b";
    private Player  player;
    private Context context;
    private String  ctoken;
    private PlaybackEventListener listener;
    private String token;


    public interface PlaybackEventListener{
        void onPlaybackEvent(PlayerEvent playerEvent, Metadata metadata);
    }

    public SpotifyMusic(Context context, PlaybackEventListener listener) {
        this.context = context;
        this.listener = listener;
      //  String token = getToken();
        if(token != null){
            init(token);
        }
    }

    /**
     * Spotify初始化
     * @param accessToken
     */
    private void init(final String accessToken) {
        Config playerConfig= new  Config(context,accessToken,SPOTIFY_ID);
        Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
            @Override
            public void onInitialized(SpotifyPlayer spotifyPlayer) {
                player = spotifyPlayer;
                player.addConnectionStateCallback(SpotifyMusic.this);
                player.addNotificationCallback(SpotifyMusic.this);
                token=accessToken;
            }

            @Override
            public void onError(Throwable throwable) {

                Log.w(TAG,"无法初始化Spotify："+ throwable.getMessage());


            }
        });

    }

/*    *//***
     * 网络请求
      * @return
     *//*
    @SuppressLint("StaticFieldLeak")
    public String getToken() {
       return new  AsyncTask<Void, Void ,String>(){
           @Override
           protected String doInBackground(Void... params) {
               try {
                   URL url = new URL("https://accounts.spotify.com/api/token");
                   HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                   connection.setRequestMethod("POST");
                   connection.setDoInput(true);
                   connection.setDoOutput(true);
                   //这个是告诉服务器 你的客户端的配置/需求
                   connection.setRequestProperty("Authorization", "Basic " + Base64.encodeToString((SPOTIFY_ID + ":" + SPOTIFY_SECRET).getBytes("utf-8"), Base64.NO_WRAP));
                   OutputStream os = connection.getOutputStream();
                   // os.write(("grant_type=refresh_token&refresh_token=" + Config.SPOTIFY_REFRESH_TOKEN).getBytes("UTF-8"));
                   try {
                       connection.connect();
                       int responseCode = connection.getResponseCode();
                       if (responseCode == 200) {

                           BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(connection.getInputStream())));
                           StringBuffer sbResponse = new StringBuffer();
                           String line;
                           while ((line = reader.readLine()) != null) {
                               sbResponse.append(line).append('\n');
                           }
                           // return new JSONObject(sbResponse.toString()).getString("access_token"));
                       } else {
                           Log.w(TAG, "Spotify getToken () response code! = 200 ");
                           return null;
                       }
                   } finally {
                       connection.disconnect();
                   }
               } catch (Exception e) {
                   e.printStackTrace();
                    return null;
                 }
              }
           }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
         } catch(InterruptedException | ExecutionException e) {
                   e.printStackTrace();
                   return null;
                }
    }*/


    /**
     * 已登录
     */
    @Override
    public void onLoggedIn() {
        Log.d(TAG,"User logged in");
    }

    /**
     * 登录已退出
     */
    @Override
    public void onLoggedOut() {
       Log.d(TAG,"User logged out");
    }

    /**
     * 登录失败
     * @param error
     */
    @Override
    public void onLoginFailed(Error error) {
      Log.d(TAG,"Login failed: " +error.name());
    //  init(getToken());
    }

    /**
     * 临时错误
     */
    @Override
    public void onTemporaryError() {
        Log.d(TAG,"Temporary Error occurred " );
    }

    /**
     * 连接消息
     * @param message
     */
    @Override
    public void onConnectionMessage(String message) {
        Log.d(TAG,"Received connection message: "  +message );
    }

    /**
     * 播放事件
     * @param playerEvent
     */
    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {

    Log.d(TAG,"Playback event received: " + playerEvent);
    listener.onPlaybackEvent(playerEvent,player.getMetadata());
    }

    /**
     *播放错误
     * @param error
     */
    @Override
    public void onPlaybackError(Error error) {
        Log.d(TAG,"Playback error received: " + error.name());
    }

    public void destroy(){
        Spotify.destroyPlayer(this);
    }

    public void control(String musicControl, Boolean... onOff){
        if(player !=null){
            switch (musicControl){
                //恢复
                case "resume":
                    player.resume(null);
                    break;
                case  "stop":
                    //暂停
                case "pause":
                    player.pause(null);
                    break;
                    //跳到下一步
                case "next":
                    player.skipToNext(null);
                    break;
                    //跳至上一页
                case "previous":
                    player.skipToPrevious(null);
                    break;
                    //重复
                case "repeat":
                    try {
                        player.setRepeat(null, onOff[0]);
                    }catch (IndexOutOfBoundsException e){
                        player.setRepeat(null,!player.getPlaybackState().isShuffling);
                    }break;
            }
        }
    }

    /***
     * Random:随机播放
     */
public void playRandom(){
        List<String> artists = getArtistsList();
        if(artists != null){
            //Shuffle
            player.setShuffle(null,true);
            player.playUri(null,"spotify:artist:" +artists.get(new Random().nextInt(artists.size())),0,0);
            player.skipToNext(null);
        }else {
            Log.d(TAG,"No artists");
        }
    }

    /**
     * get Artists List:获取艺术家列表
     * @return
     */
    private List<String> getArtistsList() {
        try {
        return new AsyncTask<Void, Void, List<String>>() {
            @Override
            protected List doInBackground(Void... params) {

                try {
                    URL url = new URL("https://api.spotify.com/v1/me/following?type=artist&limit=50");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestProperty("Authorization", "Bearer " + token);

                    try {
                        connection.connect();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(connection.getInputStream())));
                        StringBuffer sbResponse = new StringBuffer();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            sbResponse.append(line).append('\n');
                        }
                        try {
                            JSONArray items = new JSONObject(sbResponse.toString()).getJSONObject("artists").getJSONArray("items");
                            List<String> artists = new ArrayList<>();
                            for (int i = 0; i < items.length(); i++) {
                                JSONObject item = items.getJSONObject(i);
                                artists.add(item.getString("id"));
                            }
                            return artists;
                        } catch (JSONException e) {
                            e.printStackTrace();
                            return null;
                        }
                    } finally {
                        connection.disconnect();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }.execute().get();
          } catch (InterruptedException e) {
                    e.printStackTrace();
                    return null;
            } catch (ExecutionException e) {
                    e.printStackTrace();
                    return null;
            }
    }
/**
 * playArtist:播放艺术家
 */
    public boolean playArtist(String artists){
        String artistsId =getArtistId(artists);
        if(artistsId !=null){
            player.setShuffle(null,true);
            player.playUri(null,"spotify:artist:" +artistsId,0,0);
            player.skipToNext(null);
            return true;
        }else {
            return false;
        }

}

    private String getArtistId(final String artist) {
        try {
        return new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    URL url = new URL("https://api.spotify.com/v1/search?q=" + artist + "&type=artist");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(connection.getInputStream())));
                        StringBuffer sbResponse = new StringBuffer();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            sbResponse.append(line).append('\n');
                        }
                        try {
                            return new JSONObject(sbResponse.toString()).getJSONObject("artists").getJSONArray("items").getJSONObject(0).getString("id");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            return null;
                        }
                    } finally {
                        connection.disconnect();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }.execute().get();
          } catch (InterruptedException e) {
                    e.printStackTrace();
                    return null;
            } catch (ExecutionException e) {
                    e.printStackTrace();
                    return null;
            }
    }


/**playPlaylist
 *播放列表
 */
    public boolean playPlaylist(String  playlist){
        //map是以键值对来存储数据的
        Map<String,String>ids = getPlaylistIds(playlist);
        if(ids!=null){
            player.setShuffle(null,true);
            player.playUri(null,"spotify:user:" +ids.get("userId") + ":playlist:"  + ids.get("playlistId"),0,0);
            player.skipToNext(null);
            return true;
        }else {
            return false;
        }
    }

private Map<String, String> getPlaylistIds(final String playlist) {
        try {
            return new AsyncTask<Void, Void, Map<String, String>>() {
                @Override
                protected Map<String, String> doInBackground(Void... params) {
                    try {
                        URL url = new URL("https://api.spotify.com/v1/search?q=" + playlist + "&type=playlist&limit=1");
                        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                        try {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(connection.getInputStream())));
                            StringBuilder sbResponse = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                sbResponse.append(line).append('\n');
                            }
                            try {
                                Map<String, String> ids = new HashMap<>();
                                ids.put("playlistId", new JSONObject(sbResponse.toString()).getJSONObject("playlists").getJSONArray("items").getJSONObject(0).getString("id"));
                                ids.put("userId", new JSONObject(sbResponse.toString()).getJSONObject("playlists").getJSONArray("items").getJSONObject(0).getJSONObject("owner").getString("id"));
                                return ids;
                            } catch (JSONException e) {
                                e.printStackTrace();
                                return null;
                            }

                        } finally {
                            connection.disconnect();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }.execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }



}
