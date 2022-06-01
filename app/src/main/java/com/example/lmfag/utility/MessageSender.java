package com.example.lmfag.utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MessageSender {

    public static void sendMessage(String condition,String title,String body)  {
        URL url = null;
        try {
            url = new URL("http://www.lmfag.web.app/#/messages/"+condition+"_"+title+"_"+body);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return;
        }
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
        } finally {
            urlConnection.disconnect();
        }
    }
}
