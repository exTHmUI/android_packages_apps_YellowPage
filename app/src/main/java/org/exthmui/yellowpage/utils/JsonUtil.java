package org.exthmui.yellowpage.utils;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class JsonUtil {

    public static final String TAG = "JsonUtil";

    public static JSONObject getJsonFromURL(String url) throws IOException, JSONException {
        URL updateURL = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) updateURL.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept","*/*");
        connection.setRequestProperty("User-Agent", "YellowPage/1.0");
        connection.setConnectTimeout(2000);
        connection.connect();
        if (connection.getResponseCode() != 200) {
            InputStream inputStream = connection.getErrorStream();
            Log.d(TAG, "HTTP code = " + connection.getResponseCode() + "," + "data = " + InputStreamToString(inputStream));
            return null;
        }
        InputStream inputStream = connection.getInputStream();
        return new JSONObject(InputStreamToString(inputStream));
    }

    public static String InputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder builder = new StringBuilder();
        String line;
        while((line = reader.readLine()) != null){
            builder.append(line);
        }
        reader.close();
        inputStream.close();
        return builder.toString();
    }
}
