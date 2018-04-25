package com.example.androidthingssmartassistant.skills;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Administrator on 2018/4/22.
 */

public class Todoist {

    private static final String TAG = Todoist.class.getSimpleName();
    private  static final String TODOIST_KEY ="605c4e40ea7c1b24fb50cbde3fe99ceaaa9a7599";

    DateFormat utcDateFormat = new SimpleDateFormat("EEE dd MMM yyyy HH:mm:ss");

    public List<JSONObject> getTasks(final String desiredDate,final String desiredTask){

        //Asynctask + JSON (how to get some values)
        //Asynctask + JSON（如何获得一些值）
        //如果你需要在任务完成时触发某些东西,广播,每个感兴趣的人（包括其他活动）都需要注册一个广播接收器。
    try {
        return new AsyncTask<Void,Void,List<JSONObject>>(){
            @Override
            protected List<JSONObject> doInBackground(Void... params) {
             try {
                    boolean isDateSet =false;
                    URL url = new URL("https://todoist.com/API/v7/sync?token=" + TODOIST_KEY + "&resource_types=[\"items\"]");
                    HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(connection.getInputStream())));
                    StringBuilder sbResponse = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sbResponse.append(line).append('\n');
                    }

                    JSONObject response = new JSONObject(URLDecoder.decode(sbResponse.toString(),"UTF-8"));
                    JSONArray  itemsArray = response.getJSONArray("items");
                    List<JSONObject> itemsList = new LinkedList<>();
                    for(int i=0;i<itemsArray.length();i++){
                        itemsList.add(itemsArray.getJSONObject(i));
                    }
                    //Calendar:日历
                    Calendar desiredCalendar=Calendar.getInstance();
                    if(desiredDate !=null && !desiredDate.equals("") && !desiredDate.equals(" ")){
                        isDateSet=true;
                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

                        try {
                            desiredCalendar.setTime(df.parse(desiredDate));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    if(desiredTask !=null && !desiredTask.equals("") && !desiredTask.equals(" ")){
                        //应用迭代器Iterator 获取所有的key值
                        for(Iterator<JSONObject>  iterator = itemsList.iterator();iterator.hasNext();){
                            JSONObject task = iterator.next();


                            try {
                                //Calendar:日历
                                Calendar itemDate=Calendar.getInstance();
                                itemDate.setTime(utcDateFormat.parse(task.getString("due_date_utc").replace("  0000","")));
                                if(isDateSet){
                                    if(itemDate.get(Calendar.HOUR_OF_DAY) !=desiredCalendar.get(Calendar.HOUR_OF_DAY) || !task.getString("content").toLowerCase().replaceAll("[-+.^:,]","").equals(desiredTask.toLowerCase())){

                                        iterator.remove();
                                    }
                                }else {
                                    if(!task.getString("content").toLowerCase().replaceAll("[-+.^:,]","").equals(desiredTask.toLowerCase())){
                                        iterator.remove();
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                return null;
                            }
                            catch (ParseException e) {
                                e.printStackTrace();
                                return null;
                            }
                        }
                    }else {
                        for(Iterator<JSONObject> iterator = itemsList.iterator();iterator.hasNext();){
                            JSONObject task = iterator.next();

                            try {
                                Calendar itemDate = Calendar.getInstance();
                                itemDate.setTime(utcDateFormat.parse(task.getString("due_date_utc").replace("   0000","")));

                                if(itemDate.get(Calendar.DAY_OF_YEAR) !=desiredCalendar.get(Calendar.DAY_OF_YEAR)){
                                    iterator.remove();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                return null;
                            } catch (ParseException e) {
                                e.printStackTrace();
                                return null;
                            }
                        }
                    }
                      return itemsList;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                } finally {
                    connection.disconnect();
                   }
                }catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }.execute().get();
    }catch (InterruptedException e) {
        e.printStackTrace();
        return null;
    }catch (ExecutionException e) {
        e.printStackTrace();
        return null;
        }
   }
   public boolean addTask(final String task,final String date,final String time) {
       try {
           return new AsyncTask<Void, Void, Boolean>() {
               @Override
               protected Boolean doInBackground(Void... params) {
                   try {
                       StringBuilder sb = new StringBuilder();
                       sb.append(task);
                       if (date != null && !date.equals(" ") && !date.equals(" ")) {
                           sb.append(" ");
                           sb.append(date);
                       }
                       if (time != null && !time.equals(" ") && !time.equals(" ")) {
                           sb.append(" ");
                           sb.append(time);
                       }
                       URL url = new URL("https://todoist.com/API/v7/quick/add?token=" + TODOIST_KEY + "&text=" + sb.toString());
                       HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                       try {
                           if (connection.getResponseCode() == 200) {
                               return true;
                           } else {
                               Log.w(TAG, "Could not add the task: the answer is not 200!");
                               return false;
                           }
                       } catch (IOException e) {
                           e.printStackTrace();
                           return false;
                       } finally {
                           connection.disconnect();
                       }
                   } catch (IOException e) {
                       e.printStackTrace();
                       return false;
                   }
               }
           }.execute().get();
       } catch (InterruptedException e) {
           e.printStackTrace();
           return false;
       } catch (ExecutionException e) {
           e.printStackTrace();
           return false;
       }
   }
}
