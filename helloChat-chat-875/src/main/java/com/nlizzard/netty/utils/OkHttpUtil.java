package com.nlizzard.netty.utils;

import com.nlizzard.grace.result.GraceJSONResult;
import com.nlizzard.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Slf4j
public class OkHttpUtil {

    public static GraceJSONResult get(String url) {
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .get()
                    .url(url)
                    .build();
            Response response = client.newCall(request).execute();
            String res = null;
            if (response.body() != null) {
                res = response.body().string();
            }
            return JsonUtils.jsonToPojo(res, GraceJSONResult.class);
        } catch (Exception e) {
            log.error("OkHttp get failed:", e);
        }
        return null;
    }

}
