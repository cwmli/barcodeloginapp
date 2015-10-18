package com.robotix.warrior.barcode;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.Scanner;

public class LoginActivity extends AppCompatActivity {

    public static AppCompatActivity instance;

    private Button loginbutton;

    public static String studentnumber;
    public static String password;

    public static boolean verified;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        instance = this;
        verified = false;

        loginbutton = (Button)findViewById(R.id.login);
        loginbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkFields();
                new VerifyAdmin().execute(studentnumber, password);
            }
        });
    }

    public boolean checkFields(){
        EditText ss = (EditText)findViewById(R.id.student_num_field);
        studentnumber = ss.getText().toString();
        EditText pp = (EditText)findViewById(R.id.password_field);
        password = pp.getText().toString();

        if(studentnumber.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Both fields must be completed.", Toast.LENGTH_SHORT).show();
            return false;
        } else if(studentnumber.length() != 6){
            Toast.makeText(this, "Invalid student number.", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        return super.onOptionsItemSelected(item);
    }

    public static class VerifyAdmin extends AsyncTask<String, Void, String>{
        protected String doInBackground(String... data) {
            StringBuffer str_response = new StringBuffer();
            JSONObject response = null;

            URL url;
            HttpURLConnection connection = null;

            try {
                String params = "identifier=" + URLEncoder.encode(data[0], "UTF-8") + "&password=" + URLEncoder.encode(data[1], "UTF-8");

                url = new URL("http://PLACEHOLDERURL.com/abc.json");
                connection = (HttpURLConnection) url.openConnection();

                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                OutputStream os = connection.getOutputStream();
                BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(os));
                wr.write(params);
                wr.flush();
                wr.close();

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
            String res = response.optString("access");

            return response != null ? res : "Internal error.";
        }

        protected void onPostExecute(String result) {

            String msg;

            switch(result) {
                case "2": //admin
                    msg = "Verified.";
                    LoginActivity.verified = true;
                    Intent scanner = new Intent(instance, ScannerActivity.class);
                    instance.startActivity(scanner);
                    break;
                case "1": //regular
                    msg = "Your account does not have admin privileges";
                    break;
                case "0": //invalid
                    msg = "Unknown username and/or password";
                    break;
                default: //unknown result
                    msg = "Unable to reach server.";
                    break;
            }

            ScannerActivity.toast = Toast.makeText(instance.getApplicationContext(),
                    msg, Toast.LENGTH_SHORT);
            if(!ScannerActivity.toast.getView().isShown()){
                ScannerActivity.toast.show();
            }

        }
    }
}
