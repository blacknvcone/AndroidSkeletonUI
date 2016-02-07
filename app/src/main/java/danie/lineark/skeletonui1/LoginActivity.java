package danie.lineark.skeletonui1;

import android.app.Activity;

/**
 * Created by blacknvc on 2/1/2016.
 */

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import org.jsoup.nodes.Document;
import org.jsoup.Jsoup;

import com.loopj.android.http.*;
import cz.msebera.android.httpclient.Header;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import danie.lineark.skeletonui1.service.QuickstartPreferences;
import danie.lineark.skeletonui1.service.RegistrationIntentService;



public class LoginActivity extends Activity {

    private View loginFormView;
    private View progressView;
    private AutoCompleteTextView emailTextView;
    private EditText passwordTextView;
    private BroadcastReceiver mRegistrationBroadcastReceiver;

    private String CSRFTOKEN = null;
    private String username,password;

    private SharedPreferences sharedPreferences,prefs;
    private Context mContext;

    //Change Auth URL Here ....
    public static final String AuthURL = "http://lineark.esy.es/login";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "GcmActivity";
    private static final String REG_ID = "regId";


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Init Context
        mContext = this.getApplicationContext();

        emailTextView = (AutoCompleteTextView) findViewById(R.id.email);
        passwordTextView = (EditText) findViewById(R.id.password);
        loginFormView = findViewById(R.id.login_form);
        progressView = findViewById(R.id.login_progress);

        prefs = getSharedPreferences("UserDetails", Context.MODE_PRIVATE);
        String registrationId = prefs.getString(REG_ID, "");

        //When Email ID is set in Sharedpref, User will be taken to MenuActivity
        if (!TextUtils.isEmpty(registrationId)) {
            Intent i = new Intent(mContext, MenuActivity.class);
            //i.putExtra("regId", registrationId);
            startActivity(i);
            finish();
        }

        Button loginButton = (Button) findViewById(R.id.email_sign_in_button);
        loginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgress(true);
                username = emailTextView.getText().toString();
                password = passwordTextView.getText().toString();

                //If Using Login Inteface -> Call registAPI inside AuthRest Client
                //new AuthRestClient().get(username,password);
                registAPI();
            }
        });

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                Boolean sentToken = sharedPreferences.getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    showProgress(false);
                    Log.i("Log Auth", "REGISTRASI TOKEN PASSED !");

                    Toast.makeText(getApplicationContext(), "Selamat Datang", Toast.LENGTH_LONG).show();
                    Intent i = new Intent(mContext, MenuActivity.class);
                    startActivity(i);
                    finish();
                } else {
                    showProgress(false);
                    Log.i("Log Auth","REGISTRASI TOKEN Gagal !");
                }
            }
        };
    }

    public void registAPI(){

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Toast.makeText(getApplicationContext(), "Mohon Install Google Play Terlebih Dahulu", Toast.LENGTH_LONG).show();
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
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

    public class AuthRestClient{
        private  AsyncHttpClient client = new AsyncHttpClient();

        public void get(final String email2, final String password2) {
            client.setEnableRedirects(true);
            client.get(AuthURL, new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Log.i("CSRF LOG", "Failed-> " + responseString);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    // Get the value of the hidden input-field with the name __RequestVerificationToken
                    String TOKEN;
                    TOKEN = "_token";

                    Document doc = Jsoup.parse(responseString);
                    org.jsoup.nodes.Element el = doc.select("input[name*=" + TOKEN).first();
                    String hidden_token = el.attr("value");

                    CSRFTOKEN = hidden_token;
                    Log.i("CSRF LOG", "" + hidden_token);


                    RequestParams req = new RequestParams();
                        req.put("email",email2);
                        req.put("password", password2);
                        req.put("_token", CSRFTOKEN);
                    post(req);
                }
            });

        }
        public void post(RequestParams params) {

            client.post(AuthURL, params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    Log.i("LOG AUTH", "Success -> " +statusCode);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.i("LOG AUTH", "Failed ->" +statusCode);
                    Log.i("LOG AUTH","Error ->"+error);
                }
            });


        }
    }

}
