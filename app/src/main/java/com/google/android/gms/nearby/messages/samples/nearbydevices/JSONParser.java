package com.google.android.gms.nearby.messages.samples.nearbydevices;

/**
 * Created by User on 2016/10/27.
 */
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;
import java.net.*;

public class JSONParser {

    final String TAG = "JsonParser.java";

    static InputStream is = null;
    static JSONArray jObj = null;
    static String json = "";
    public static int length =0;

    public int getJSONFromUrl(String url) {
        HttpURLConnection c = null;

        // make HTTP request
        try {

            URL u = new URL(url);
            c = (HttpURLConnection) u.openConnection();
            c.connect();
            int status = c.getResponseCode();
            switch (status) {
                case 200:
                case 201:
                    BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line+"\n");
                    }
                    br.close();
                    json = sb.toString();
            }


        } catch (Exception e) {
            Log.e(TAG, "Error converting result " + e.toString());
        }

        // try parse the string to a JSON object
        try {
            jObj = new JSONArray(json);


        } catch (JSONException e) {
            Log.e(TAG, "Error parsing data " + e.toString());
        }

        // return JSON String
        return jObj.length() ;
    }
}