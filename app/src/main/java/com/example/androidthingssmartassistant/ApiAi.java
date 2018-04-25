package com.example.androidthingssmartassistant;


import android.os.AsyncTask;

import java.util.concurrent.ExecutionException;

import ai.api.AIDataService;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;


public class ApiAi {
    //
    //创建一个AIConfiguration的实例
    final AIConfiguration config = new AIConfiguration(Config.ACCESS_TOKEN,
            AIConfiguration.SupportedLanguages.English,   //支持的语言
            AIConfiguration.RecognitionEngine.System);   //识别引擎

    //使用配置对象创建一个AIDataService实例
    final AIDataService aiDataService = new AIDataService(config);

    //创建空的AIRequest实例,使用setQuery方法设置请求文本。
    final AIRequest aiRequest = new AIRequest();


    AIResponse makeRequest(final String query) {

        try {

            return new AsyncTask<Void, Void, AIResponse>() {

                @Override
                protected AIResponse doInBackground(Void... params) {
                    aiRequest.setQuery(query);
                    try {
                        //将请求发送到API.AI服务。
                        return aiDataService.request(aiRequest);
                    } catch (AIServiceException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
            //异常一般发生在线程中，当一个正在执行的线程被中断时就会出现这个异常
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
//执行任务异常
        catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }
}