package com.example.androidthingssmartassistant.skills;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

/**
 * Created by Administrator on 2018/2/24.
 */

public class Openweather {

    private String  OPEN_WEATHER_KEY="d89360387c9ee95f661c1d8d8762106e";
    private String  OPEN_WEATHER_CITY="524901";


    public String getCurrentWeather(){

        try {
            return new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {

                try {
                    URL url = new URL("http://api.openweathermap.org/data/2.5/weather?APPID=" + OPEN_WEATHER_KEY + "&id=" + OPEN_WEATHER_CITY + "&lang=ru&units=metric");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(connection.getInputStream())));
                    StringBuffer sbResponse = new StringBuffer();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sbResponse.append(line).append("\n");
                    }

                    try {
                        JSONObject responce = new JSONObject(sbResponse.toString());
                        JSONArray weatherArry = responce.getJSONArray("weather");
                        String description = weatherArry.getJSONObject(0).getString("description");
                        int tempMin = responce.getJSONObject("main").getInt("temp_min");
                        int tempMax = responce.getJSONObject("main").getInt("temp_max");
                        int windSpeed = responce.getJSONObject("wind").getInt("speed");

                        StringBuffer sbResult = new StringBuffer();
                        sbResult.append(description);
                        sbResult.append(", ");

                        if (tempMin != tempMax) {
                            sbResult.append("temperature from");
                            sbResult.append("tempMin");
                            sbResult.append("to");

                            sbResult.append("tempMin");
                            sbResult.append(" degrees");
                        } else {
                            sbResult.append("temperature ");
                            sbResult.append(tempMin);
                            sbResult.append(" degrees, ");
                        }
                        sbResult.append("wind ");
                        sbResult.append(windSpeed);
                        sbResult.append(" meters per second.");

                        return sbResult.toString();
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
