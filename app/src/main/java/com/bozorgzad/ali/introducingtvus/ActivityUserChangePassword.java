package com.bozorgzad.ali.introducingtvus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

/*
 * Created by Ali_Dev on 8/21/2017.
 */

public class ActivityUserChangePassword extends ActivityAppBarBackOrCloseButton {

    private Global.user tempCurrentUser = new Global.user();
    private String token;
    private TextView txtEmailChangePassword;
    private TextView txtUserNameChangePassword;
    private EditText edtPasswordChangePassword;
    private EditText edtPasswordAgainChangePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Make sure this is before calling super.onCreate
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);

        // Change Language for first time enter
        if(Global.firstEnter){
            ActivityAppBarNavigationDrawer.toggleLanguage(this);
            Global.firstEnter = false;
        }

        setContentView(R.layout.activity_user_change_password);

        // Do it for Error in EditText (can change the direction only for EditText)
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);

        // Show progressBarReview
        llHeaderProgressParent.setVisibility(View.VISIBLE);

        // Hide elevation under the Toolbar
        AppBarLayout ablSignInAndRegister = (AppBarLayout) findViewById(R.id.ablBackOrCloseButton);
        ablSignInAndRegister.setStateListAnimator(null);

        // Set Toolbar
        setBackOrCloseToolbar(true, getString(R.string.change_password_title));

        txtEmailChangePassword = (TextView) findViewById(R.id.txtEmailChangePassword);
        txtUserNameChangePassword = (TextView) findViewById(R.id.txtUserNameChangePassword);
        getTokenFromUrlAndSendItToServer();

        edtPasswordChangePassword = (EditText) findViewById(R.id.edtPasswordChangePassword);
        edtPasswordAgainChangePassword = (EditText) findViewById(R.id.edtPasswordAgainChangePassword);
        whenPressChangePasswordButton();
    }

    private void getTokenFromUrlAndSendItToServer(){
        Intent intent = getIntent();
        Uri data = intent.getData();

        ArrayList<String> urlArray = new ArrayList<>(Arrays.asList(data.toString().split("=")));
        token = urlArray.get(1);

        Uri.Builder builder = new Uri.Builder()
                .appendQueryParameter("token", token);

        new WebHttpConnection(Global.HOST_ADDRESS + "/webservice/checkTokenAndFetchUserInfo", builder){
            @Override public void onPostExecute(String result)
            {
                if(result != null){
                    if(result.equals("PasswordHasChangedBefore")){
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // Do something after 300ms
                                final Toast toast = Toast.makeText(ActivityUserChangePassword.this, getString(R.string.change_password_password_has_changed_before), Toast.LENGTH_LONG);
                                toast.show();
                                showToastLonger(toast, 4000);

                                Intent intent = new Intent(ActivityUserChangePassword.this, ActivityUserGoogleSignIn.class);
                                startActivity(intent);
                            }
                        }, 300);

                    }else if(result.equals("Expired")){
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // Do something after 300ms
                                final Toast toast = Toast.makeText(ActivityUserChangePassword.this, getString(R.string.change_password_link_expired), Toast.LENGTH_LONG);
                                toast.show();
                                showToastLonger(toast, 4000);

                                Intent intent = new Intent(ActivityUserChangePassword.this, ActivityUserGoogleSignIn.class);
                                startActivity(intent);
                            }
                        }, 300);

                    }else{
                        try {
                            JSONArray array = new JSONArray(result);
                            JSONObject object = array.getJSONObject(0);
                            tempCurrentUser.user_id = object.getInt("user_id");
                            tempCurrentUser.userName = object.getString("userName");
                            tempCurrentUser.userEmail = object.getString("userEmail");
                            tempCurrentUser.userSignInWithGoogle = object.getInt("userSignInWithGoogle");
                            tempCurrentUser.userRecentSearches = object.getString("userRecentSearches");
                            tempCurrentUser.isPasswordSet = 1;

                            txtUserNameChangePassword.setText(object.getString("userName"));
                            txtEmailChangePassword.setText(object.getString("userEmail"));

                            // Change Toolbar if we create a new password
                            if(object.getString("userPassword").isEmpty()){
                                Toolbar toolbarCloseButton = (Toolbar) findViewById(R.id.toolbarCloseButton);
                                toolbarCloseButton.setLayoutDirection(View.LAYOUT_DIRECTION_LOCALE);
                                toolbarCloseButton.setTitle(R.string.change_password_create_password_title);
                                setSupportActionBar(toolbarCloseButton);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        llHeaderProgressParent.setVisibility(View.GONE);
                    }
                }else{
                    llInternetUnavailableParent.setVisibility(View.VISIBLE);
                }
            }
        }.execute();
    }

    private void whenPressChangePasswordButton(){
        Button btnChangePassword = (Button) findViewById(R.id.btnChangePassword);
        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptChangePassword();
            }
        });

        edtPasswordAgainChangePassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.btnSignIn || id == EditorInfo.IME_NULL) {
                    attemptChangePassword();
                    return true;
                }
                return false;
            }
        });
    }

    private void attemptChangePassword(){
        // Reset errors.
        edtPasswordChangePassword.setError(null);
        edtPasswordAgainChangePassword.setError(null);

        // Store values at the time of the changePassword attempt.
        String password = edtPasswordChangePassword.getText().toString();
        String passwordAgain = edtPasswordAgainChangePassword.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (password.isEmpty() || password.length() < 5) {
            edtPasswordChangePassword.setError(getString(R.string.sign_in_register_sing_in_invalid_password));
            focusView = edtPasswordChangePassword;
            cancel = true;
        }else if(!passwordAgain.equals(password)){
            edtPasswordAgainChangePassword.setError(getString(R.string.change_password_password_not_match));
            focusView = edtPasswordAgainChangePassword;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt changePassword and focus the first form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and perform the user changePassword
            changePassword(password);
        }
    }

    private void changePassword(String password){
        if(!Global.isConnectedToNetwork(ActivityUserChangePassword.this)){
            Toast.makeText(ActivityUserChangePassword.this, getString(R.string.no_internet_your_offline_please_check_network), Toast.LENGTH_LONG).show();
            return;
        }

        String passwordSha1 = password;
        String email = txtEmailChangePassword.getText().toString();
        try {
            passwordSha1 = ActivityUserSignInAndRegister.SHA1(email.toLowerCase() + password + getPackageName());
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Global.showProgressDialog(getString(R.string.progress_dialog_wait), this);
        Uri.Builder builder = new Uri.Builder()
                .appendQueryParameter("token", token)
                .appendQueryParameter("userPassword", passwordSha1);

        new WebHttpConnection(Global.HOST_ADDRESS + "/webservice/changePassword", builder){
            @Override public void onPostExecute(String result)
            {
                Global.hideProgressDialog();
                if(result != null){
                    Toast.makeText(ActivityUserChangePassword.this, R.string.change_password_password_changed_successfully, Toast.LENGTH_LONG).show();

                    // Get SharedPreferences And Editor
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ActivityUserChangePassword.this);
                    int userId = sharedPreferences.getInt(ActivityUserGoogleSignIn.USER_ID, -1);
                    if(userId == -1){
                        Global.currentUser = tempCurrentUser;
                        ActivityUserGoogleSignIn.changedPreferencesRelateToSignIn(ActivityUserChangePassword.this, false);

                        // When we are in app, and want to change password
                        if(!Global.firstTimeEnterDisableSignInSignOut){
                            sendBroadcast(new Intent(ActivityAppBarNavigationDrawer.TOGGLE_SIGN_IN_AND_SIGN_OUT));
                        }
                    }else{
                        // When we are in app, and create password for the user that currently signIn
                        if(Global.currentUser.user_id == tempCurrentUser.user_id){
                            Global.currentUser.isPasswordSet = 1;
                        }
                    }

                    Intent intent = new Intent(ActivityUserChangePassword.this, ActivityMain.class);
                    startActivity(intent);
                }else{
                    Toast.makeText(ActivityUserChangePassword.this, getString(R.string.internet_unavailable_text), Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    private void showToastLonger(final Toast toast, int time){
        new CountDownTimer(time, 1000){
            public void onTick(long millisUntilFinished) {toast.show();}
            public void onFinish() {toast.show();}
        }.start();
    }

    @Override
    public boolean onSupportNavigateUp() {
        // close this activity as oppose to navigating up

        if(Global.firstTimeEnterDisableSignInSignOut){
            // When we were not in app, and press close button
            Intent intent = new Intent(ActivityUserChangePassword.this, ActivityMain.class);
            startActivity(intent);
        }else{
            finish();
        }

        return false;
    }
}

