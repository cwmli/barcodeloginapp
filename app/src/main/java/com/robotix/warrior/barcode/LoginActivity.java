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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

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

                if(verified){
                    Intent scanner = new Intent(instance, ScannerActivity.class);
                    startActivity(scanner);
                }
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
                String params = "user=" + URLEncoder.encode(data[0], "UTF-8") + "&pass=" + URLEncoder.encode(data[1], "UTF-8");

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


            String res = "";

            try {
                res = response.getString("status");
            } catch (Exception e){

            }
            return response != null ? res : "Internal error.";
        }

        protected void onPostExecute(String result) {

            String msg;

            switch(result) {
                case "OK":
                    msg = "Verified.";
                    LoginActivity.verified = true;
                    break;
                case "INSUFFICIENT":
                    msg = "Your account does not have admin privileges";
                    break;
                case "INVALID":
                    msg = "Unknown username and/or password";
                    break;
                default:
                    msg = "Unable to reach server.";
                    break;
            }

            Toast toast = Toast.makeText(instance.getApplicationContext(),
                    msg, Toast.LENGTH_SHORT);
            if(!toast.getView().isShown()){
                toast.show();
            }

        }
    }
}
