package com.seaice.csar.seaiceprototype;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by PabloJavier on 23-Apr-16.
 */
public class HttpDelete extends AsyncTask<String, Void, String> {

    //Activity where;
    boolean termine = false;
    String response;

    @Override
    protected String doInBackground(String... parameters) {
        termine = false;
        int status = -1;
        try
        {
            String server_url = "http://seaice-jayala.rhcloud.com/delcoord";
            URL url = new URL(server_url);
            Map<String,Object> params = new LinkedHashMap<>();

            params.put("telefono", "+50230359588");
            //Datos del post
            params.put("id", parameters[0]);


            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String,Object> param : params.entrySet()) {
                if (postData.length() != 0) postData.append('&');
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            }
            byte[] postDataBytes = postData.toString().getBytes("UTF-8");

            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            conn.setDoOutput(true);
            conn.getOutputStream().write(postDataBytes);

            status = conn.getResponseCode();
            Log.v("Response POST", postData.toString());
            Log.v("Response STATUS", ""+status);

            InputStream in = conn.getInputStream();

            int len;
            byte[] buffer = new byte[4096];
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            while (-1 != (len = in.read(buffer))) {
                bos.write(buffer, 0, len);
            }

            response = new String(bos.toByteArray());


            Log.v("Response RECEIVED", response);
            termine = true;

        }
        catch(Exception e)
        {

            Log.e("HTTP STATUS", ""+status);
            Log.e("MainActivity", "Exception Dummy", e);
        }

        return response;
    }
}
