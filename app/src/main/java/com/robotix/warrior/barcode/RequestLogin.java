package com.robotix.warrior.barcode;

import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
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
                String params = "barnum=" + URLEncoder.encode(data[i], "UTF-8"); //studentnum is the name of the parameter in the web address

                //url = new URL("http://4659warriors.com"); //ADDRESS TO CONTENT DATA TO THE RUBY CONTROLLER FOR PARSING BARCODE CONTENT
                url = new URL("http://barcodelogin.herokuapp.com/loginapp?" + params);
                connection = (HttpURLConnection) url.openConnection();

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

        String res = "";

        try {
            res = response.getString("message");
        } catch (Exception e){

        }

        return response != null ? res : "Internal error.";
    }

    protected void onPostExecute(String result) {
        Toast toast = Toast.makeText(ScannerActivity.instance.getApplicationContext(),
                result, Toast.LENGTH_SHORT);
        if(!toast.getView().isShown()){
            toast.show();
        }
    }
}
