package de.dlyt.yanndroid.notifer.utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class HttpRequest {

    public static void post(String url, JSONObject json) {
        new Thread(() -> {
            try {
                new OkHttpClient().newCall(
                        new Request.Builder()
                                .url(url)
                                .post(RequestBody.create(
                                        json.toString(),
                                        MediaType.parse("application/json; charset=utf-8"))
                                ).build()
                ).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static JSONObject makeBody(CharSequence label, String packageName, int id, long time, boolean ongoing, String template, boolean removed, String title, String text, String subText, String titleBig, String textBig, boolean progressIndeterminate, int progressMax, int progress) throws JSONException {
        JSONObject body = new JSONObject();

        body.put("label", label);
        body.put("package", packageName);
        body.put("id", id);
        body.put("time", time);
        body.put("ongoing", ongoing);
        body.put("template", template);
        body.put("removed", removed);
        body.put("title", title);
        body.put("text", text);
        body.put("sub_text", subText);
        body.put("title_big", titleBig);
        body.put("text_big", textBig);
        body.put("progress_indeterminate", progressIndeterminate);
        body.put("progress_max", progressMax);
        body.put("progress", progress);

        return body;
    }

}
