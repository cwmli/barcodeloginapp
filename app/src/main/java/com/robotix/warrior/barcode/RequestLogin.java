package com.robotix.warrior.barcode;

import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by Calvin on 9/25/2015.
 */
public class RequestLogin extends AsyncTask<String, Void, String> {

    protected String doInBackground(String... data) {
        int count = data.length;
        StringBuffer str_response = new StringBuffer();
        JSONObject response = null;
        for (int i = 0; i < count; i++) {
            URL url;
            HttpURLConnection connection = null;

            try {
                String params = "identifier=" + URLEncoder.encode(LoginActivity.studentnumber, "UTF-8") +
                        "&password=" + URLEncoder.encode(LoginActivity.password, "UTF-8") +
                        "&check=" + ScannerActivity.mode + //"in" and "out"
                        "&text=" + URLEncoder.encode(data[i], "UTF-8");
                url = new URL("http://PLACEHOLDERURL.com/abc?" + params);
                connection = (HttpURLConnection) url.openConnection();

                connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");

                //Get Response
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;
                while ((line = rd.readLine()) != null) {
                    str_response.append(line);
                    str_response.append('\r');
                }
                rd.close();
                response = new JSONObject(str_response.toString());
            } catch (Exception e) {
                return e.toString();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            if (isCancelled()) break;
        }

        String res = response.optString("success");

        return response != null ? res : "Internal error.";
    }

    protected void onPostExecute(String result) {

        String msg;

        switch (result){
            case "true":
                if(ScannerActivity.mode == "in") {
                    msg = "User logged in.";
                } else {
                    msg = "User logged out.";
                }
                break;
            case "false":
                if(ScannerActivity.mode == "in") {
                    msg = "User is already logged in.";
                } else {
                    msg = "User is already logged out.";
                }
                break;
            default:
                msg = result;
                break;
        }

        ScannerActivity.toast = Toast.makeText(ScannerActivity.instance.getApplicationContext(),
                msg, Toast.LENGTH_SHORT);
        if(!ScannerActivity.toast.getView().isShown()){
            ScannerActivity.toast.show();
        }
    }
}
