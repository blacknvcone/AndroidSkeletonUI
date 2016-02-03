package danie.lineark.skeletonui1;

import android.app.Activity;

/**
 * Created by blacknvc on 2/1/2016.
 */

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import org.jsoup.nodes.Document;


import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;



public class LoginActivity extends Activity {

    private View loginFormView;
    private View progressView;
    private AutoCompleteTextView emailTextView;
    private EditText passwordTextView;


    private String CSRFTOKEN = null;
    private DefaultHttpClient client;
    private String username,password;

    //Change Auth URL Here ....
    public static final String AuthURL = "http://36.81.202.248/android/login";


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Init Client Web Socket
        client = new DefaultHttpClient();

        emailTextView = (AutoCompleteTextView) findViewById(R.id.email);
        passwordTextView = (EditText) findViewById(R.id.password);
        loginFormView = findViewById(R.id.login_form);
        progressView = findViewById(R.id.login_progress);

        Button loginButton = (Button) findViewById(R.id.email_sign_in_button);
        loginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //initLogin();
                showProgress(true);
                username = emailTextView.getText().toString();
                password = passwordTextView.getText().toString();
                new getsToken().execute(AuthURL);

            }
        });
    }


    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            loginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            progressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    class getsToken extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... urls) {
            try {

                String hidden_token = "";
                String response = "";

                //------------------>>
                HttpGet httpget = new HttpGet(urls[0]);
                HttpResponse execute = client.execute(httpget);

                // Get the response of the GET-request
                InputStream content = execute.getEntity().getContent();
                BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                String s = "";
                while((s = buffer.readLine()) != null)
                    response += s;

                content.close();
                buffer.close();

                // Get the value of the hidden input-field with the name __RequestVerificationToken
                String TOKEN;
                TOKEN = "_token";

                Document doc = Jsoup.parse(response);
                org.jsoup.nodes.Element el = doc.select("input[name*=" + TOKEN).first();
                hidden_token = el.attr("value");

                CSRFTOKEN = hidden_token;
                Log.i("CSRF LOG",""+hidden_token);
                return true;

                //------------------>>

            } catch (ParseException e1) {
                e1.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        protected void onPostExecute(Boolean result) {
            if (result == false){
                Toast.makeText(getApplicationContext(), "Gagal Mengkoneksikan Ke Server, Cek Koneksi Anda", Toast.LENGTH_LONG).show();
            } else {
                new AuthTask().execute(username, password, CSRFTOKEN);
            }
        }

    }

    class AuthTask extends AsyncTask<String, Void, Boolean> {

        int responCodeStatus = 0;
        String rsl;

        @Override
        protected Boolean doInBackground(String... arg0) {
            String result = "";
            int responseCode = 0;
            try {

                HttpPost httppost = new HttpPost(AuthURL);

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("username", arg0[0]));
                nameValuePairs.add(new BasicNameValuePair("password", arg0[1]));
                nameValuePairs.add(new BasicNameValuePair("_token", arg0[2]));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse response;


                response = client.execute(httppost);
                responseCode = response.getStatusLine().getStatusCode();

                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

                String line;
                while ((line = rd.readLine()) != null) {
                    result = line.trim();
                }
                Log.i("LOG AUTH", result);
                Log.i("LOG AUTH", "CODE STA: " + responseCode);

                rsl = result;
                responCodeStatus = responseCode;
                return true;
                //------------------>>

            } catch (ParseException e1) {
                e1.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        protected void onPostExecute(Boolean result) {

            if (responCodeStatus != 200) {
               // showProgress(false);
                Toast.makeText(getApplicationContext(), "Cek Kembali User dan Password !", Toast.LENGTH_LONG).show();
            } else {
                Intent main = new Intent(LoginActivity.this, MenuActivity.class);
                Bundle xdata = new Bundle();

                xdata.putString("username", username);
                main.putExtras(xdata);
                startActivity(main);
                onDestroy();
            }

            if (result == false) {
               // showProgress(false);
                Toast.makeText(getApplicationContext(), "Gagal Mengkoneksikan Ke Server, Cek Koneksi Anda !", Toast.LENGTH_LONG).show();
            }


        }
    }

}
