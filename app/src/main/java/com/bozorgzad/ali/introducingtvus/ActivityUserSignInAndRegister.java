package com.bozorgzad.ali.introducingtvus;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/*
 * Created by Ali_Dev on 8/16/2017.
 */

public class ActivityUserSignInAndRegister extends ActivityAppBarBackOrCloseButton {

    private String registerOrSignIn = "";

    private EditText edtEmailSignIn;
    private EditText edtPasswordSignIn;
    private EditText edtName;
    private EditText edtPasswordRegister;
    private EditText edtEmailRegister;

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Hide keyboard when exit
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_sign_in_and_register);

        // Do it for Error in EditText (can change the direction only for EditText)
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);

        // Hide elevation under the Toolbar
        AppBarLayout ablSignInAndRegister = (AppBarLayout) findViewById(R.id.ablBackOrCloseButton);
        ablSignInAndRegister.setStateListAnimator(null);

        // detect previous Activity with extras And request focus for appropriate EditText
        edtName = (EditText) findViewById(R.id.edtName);
        edtEmailSignIn = (EditText) findViewById(R.id.edtEmailSignIn);
        detectPreviousActivityAndFocusOnEditText();

        // Set Toolbar
        setBackOrCloseToolbar(true, registerOrSignIn);

        // Show password onChange for checkBox
        CheckBox cbShowPasswordSignIn = (CheckBox) findViewById(R.id.cbShowPasswordSignIn);
        CheckBox cbShowPasswordRegister = (CheckBox) findViewById(R.id.cbShowPasswordRegister);
        cbShowPassword(cbShowPasswordSignIn, edtPasswordSignIn);
        cbShowPassword(cbShowPasswordRegister, edtPasswordRegister);

        // Editor action listener
        edtPasswordSignIn = (EditText) findViewById(R.id.edtPasswordSignIn);
        edtPasswordRegister = (EditText) findViewById(R.id.edtPasswordRegister);
        editorActionListener();

        // onClick for Buttons and forgotPassword
        edtEmailRegister = (EditText) findViewById(R.id.edtEmailRegister);
        Button btnRegister = (Button) findViewById(R.id.btnRegister);
        Button btnSignIn = (Button) findViewById(R.id.btnSignIn);
        TextView txtForgotPassword = (TextView) findViewById(R.id.txtForgotPassword);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptRegister();
            }
        });
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSignIn();
            }
        });
        txtForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptResetPassword();
            }
        });
    }

    private void cbShowPassword(CheckBox cbShowPassword, final EditText edtPassword){
        cbShowPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    edtPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }else{
                    edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            }
        });
    }

    private void editorActionListener(){
        edtPasswordSignIn.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.btnSignIn || id == EditorInfo.IME_NULL) {
                    attemptSignIn();
                    return true;
                }
                return false;
            }
        });

        edtPasswordRegister.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.btnRegister || id == EditorInfo.IME_NULL) {
                    attemptRegister();
                    return true;
                }
                return false;
            }
        });
    }

    private void detectPreviousActivityAndFocusOnEditText(){
        LinearLayout llSignIn = (LinearLayout) findViewById(R.id.llSignIn);
        LinearLayout llRegister = (LinearLayout) findViewById(R.id.llRegister);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            registerOrSignIn = extras.getString("RegisterOrSignIn");

            if (registerOrSignIn != null && registerOrSignIn.equals(getString(R.string.sign_in_register_toolbar_title))) {
                llSignIn.setVisibility(View.GONE);
                llRegister.setVisibility(View.VISIBLE);

                edtName.requestFocus();
            }else{
                llSignIn.setVisibility(View.VISIBLE);
                llRegister.setVisibility(View.GONE);

                edtEmailSignIn.requestFocus();
            }
        }
    }

    private void attemptSignIn(){
        // Reset errors.
        edtEmailSignIn.setError(null);
        edtPasswordSignIn.setError(null);

        // Store values at the time of the userSignIn attempt.
        String email = edtEmailSignIn.getText().toString();
        String password = edtPasswordSignIn.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (password.isEmpty() || password.length() < 5) {
            edtPasswordSignIn.setError(getString(R.string.sign_in_register_sing_in_invalid_password));
            focusView = edtPasswordSignIn;
            cancel = true;
        }

        // Check for a valid email address.
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmailSignIn.setError(getString(R.string.sign_in_register_sign_in_invalid_email_address));
            focusView = edtEmailSignIn;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt signIn and focus the first form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and perform the user userRegister
            userSignIn(email, password);
        }
    }

    private void attemptRegister(){
        // Reset errors.
        edtName.setError(null);
        edtEmailRegister.setError(null);
        edtPasswordRegister.setError(null);

        // Store values at the time of the userRegister attempt.
        String name = edtName.getText().toString();
        String email = edtEmailRegister.getText().toString();
        String password = edtPasswordRegister.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (password.isEmpty() || password.length() < 5) {
            edtPasswordRegister.setError(getString(R.string.sign_in_register_sing_in_invalid_password));
            focusView = edtPasswordRegister;
            cancel = true;
        }

        // Check for a valid email address.
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmailRegister.setError(getString(R.string.sign_in_register_sign_in_invalid_email_address));
            focusView = edtEmailRegister;
            cancel = true;
        }

        // Check for a valid name.
        if (name.isEmpty() || name.length() < 3) {
            edtName.setError(getString(R.string.sign_in_register_at_least_3_characters));
            focusView = edtName;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt register and focus the first form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and perform the user userRegister
            userRegister(name, email, password);
        }
    }

    private void attemptResetPassword(){
        // Reset errors.
        edtEmailSignIn.setError(null);

        // Store values at the time of the userSignIn attempt.
        String email = edtEmailSignIn.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        // Check for a valid email address.
        if (email.isEmpty()) {
            edtEmailSignIn.setError(getString(R.string.sign_in_sign_in_forget_password_enter_email));
            focusView = edtEmailSignIn;
            cancel = true;
        }else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmailSignIn.setError(getString(R.string.sign_in_register_sign_in_invalid_email_address));
            focusView = edtEmailSignIn;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt signIn and focus the first form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and perform the user userRegister
            userResetPassword(email);
        }
    }

    private void userSignIn(String email, String password){
        if(!Global.isConnectedToNetwork(ActivityUserSignInAndRegister.this)){
            Toast.makeText(ActivityUserSignInAndRegister.this, getString(R.string.no_internet_your_offline_please_check_network), Toast.LENGTH_LONG).show();
            return;
        }

        String passwordSha1 = password;
        try {
            passwordSha1 = SHA1(email.toLowerCase() + password + getPackageName());
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Global.showProgressDialog(getString(R.string.sign_in_google_progress_dialog_loading), this);
        Uri.Builder builder = new Uri.Builder()
                .appendQueryParameter("userEmail", email)
                .appendQueryParameter("userPassword", passwordSha1);

        new WebHttpConnection(Global.HOST_ADDRESS + "/webservice/userSignIn", builder){
            @Override public void onPostExecute(String result)
            {
                Global.hideProgressDialog();
                if(result != null){
                    if(result.equals("EmailNotRegistered")){
                        edtEmailSignIn.setError(getString(R.string.sign_in_sign_in_email_not_registered));
                        edtEmailSignIn.requestFocus();
                    }else if(result.equals("GoogleEmailSignIn")){
                        edtEmailSignIn.setError(getString(R.string.sign_in_register_sign_in_google_email_error));
                        edtEmailSignIn.requestFocus();
                    }else if(result.equals("PasswordIsIncorrect")){
                        edtPasswordSignIn.setError(getString(R.string.sign_in_sign_in_password_incorrect));
                        edtPasswordSignIn.requestFocus();
                    }else{
                        try {
                            JSONArray array = new JSONArray(result);
                            JSONObject object = array.getJSONObject(0);
                            Global.currentUser.user_id = object.getInt("user_id");
                            Global.currentUser.userName = object.getString("userName");
                            Global.currentUser.userEmail = object.getString("userEmail");
                            Global.currentUser.userSignInWithGoogle = object.getInt("userSignInWithGoogle");
                            Global.currentUser.userRecentSearches = object.getString("userRecentSearches");
                            Global.currentUser.isPasswordSet = 1;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        ActivityUserGoogleSignIn.changedPreferencesRelateToSignIn(ActivityUserSignInAndRegister.this, false);

                        sendBroadcast(new Intent(ActivityAppBarNavigationDrawer.TOGGLE_SIGN_IN_AND_SIGN_OUT));
                        Toast.makeText(ActivityUserSignInAndRegister.this, R.string.sign_in_sign_in_you_are_successfully_signed_in, Toast.LENGTH_SHORT).show();

                        // Back to previous Activity
                        ActivityUserGoogleSignIn.isComeFromSignInOrRegister = true;
                        finish();
                    }
                }else{
                    Toast.makeText(ActivityUserSignInAndRegister.this, getString(R.string.internet_unavailable_text), Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    private void userRegister(final String name, final String email, String password){
        if(!Global.isConnectedToNetwork(ActivityUserSignInAndRegister.this)){
            Toast.makeText(ActivityUserSignInAndRegister.this, getString(R.string.no_internet_your_offline_please_check_network), Toast.LENGTH_LONG).show();
            return;
        }

        String passwordSha1 = password;
        try {
            passwordSha1 = SHA1(email.toLowerCase() + password + getPackageName());
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Global.showProgressDialog(getString(R.string.progress_dialog_wait), this);
        Uri.Builder builder = new Uri.Builder()
                .appendQueryParameter("userName", name)
                .appendQueryParameter("userEmail", email)
                .appendQueryParameter("userPassword", passwordSha1);

        new WebHttpConnection(Global.HOST_ADDRESS + "/webservice/userRegister", builder){
            @Override public void onPostExecute(String result)
            {
                Global.hideProgressDialog();
                if(result != null){
                    if(result.equals("EmailDuplicate")){
                        edtEmailRegister.setError(getString(R.string.sign_in_register_email_duplicate_error));
                        edtEmailRegister.requestFocus();
                    }else if(result.equals("GoogleEmailDuplicate")){
                        edtEmailRegister.setError(getString(R.string.sign_in_register_sign_in_google_email_error));
                        edtEmailRegister.requestFocus();
                    }else{
                        Global.currentUser.user_id = Integer.parseInt(result);
                        Global.currentUser.userName = name;
                        Global.currentUser.userEmail = email;
                        Global.currentUser.userSignInWithGoogle = 0;
                        Global.currentUser.userRecentSearches = "";
                        Global.currentUser.isPasswordSet = 1;

                        ActivityUserGoogleSignIn.setRecentSearchesForFirstTimeRegister(ActivityUserSignInAndRegister.this);
                        ActivityUserGoogleSignIn.changedPreferencesRelateToSignIn(ActivityUserSignInAndRegister.this, false);

                        sendBroadcast(new Intent(ActivityAppBarNavigationDrawer.TOGGLE_SIGN_IN_AND_SIGN_OUT));
                        Toast.makeText(ActivityUserSignInAndRegister.this, R.string.sign_in_register_you_are_successfully_registered, Toast.LENGTH_SHORT).show();

                        // Back to previous Activity
                        ActivityUserGoogleSignIn.isComeFromSignInOrRegister = true;
                        finish();
                    }
                }else{
                    Toast.makeText(ActivityUserSignInAndRegister.this, getString(R.string.internet_unavailable_text), Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    private void userResetPassword(final String email){
        if(!Global.isConnectedToNetwork(this)){
            Toast.makeText(this, getString(R.string.no_internet_your_offline_please_check_network), Toast.LENGTH_LONG).show();
            return;
        }

        Global.showProgressDialog(getString(R.string.progress_dialog_wait), this);
        Uri.Builder builder = new Uri.Builder()
                .appendQueryParameter("userEmail", email);

        new WebHttpConnection(Global.HOST_ADDRESS + "/webservice/resetPassword", builder){
            @Override public void onPostExecute(String result)
            {
                Global.hideProgressDialog();
                if(result != null){
                    if(result.equals("EmailNotRegistered")){
                        edtEmailSignIn.setError(getString(R.string.sign_in_sign_in_email_not_registered));
                        edtEmailSignIn.requestFocus();
                    }else if(result.equals("EmailSent")){
                        showAlertDialogEmailSent(ActivityUserSignInAndRegister.this, email, false);
                    }
                }else{
                    Toast.makeText(ActivityUserSignInAndRegister.this, getString(R.string.internet_unavailable_text), Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    public static void userSignInByIdWhenEnterApp(int userId, final Activity activity){
        Uri.Builder builder = new Uri.Builder()
                .appendQueryParameter("userId", String.valueOf(userId));

        new WebHttpConnection(Global.HOST_ADDRESS + "/webservice/userSignInByIdWhenEnterApp", builder){
            @Override public void onPostExecute(String result)
            {
                if(result != null){
                    try {
                        JSONArray array = new JSONArray(result);
                        JSONObject object = array.getJSONObject(0);
                        Global.currentUser.user_id = object.getInt("user_id");
                        Global.currentUser.userName = object.getString("userName");
                        Global.currentUser.userEmail = object.getString("userEmail");
                        Global.currentUser.userSignInWithGoogle = object.getInt("userSignInWithGoogle");
                        Global.currentUser.userRecentSearches = object.getString("userRecentSearches");
                        Global.currentUser.isPasswordSet = 1;

                        activity.sendBroadcast(new Intent(ActivityAppBarNavigationDrawer.TOGGLE_SIGN_IN_AND_SIGN_OUT));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.execute();
    }

    public static void showAlertDialogEmailSent(Context context, String email, boolean isCreatePassword){
        String messagePart1 = context.getString(R.string.sign_in_sign_in_forget_password_alert_text_part1);
        String messagePart2;
        if(isCreatePassword){
            messagePart2 = context.getString(R.string.user_edit_profile_create_password_alert_text_part2);
        }else{
            messagePart2 = context.getString(R.string.sign_in_sign_in_forget_password_alert_text_part2);
        }

        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(context.getString(R.string.sign_in_sign_in_forget_password_alert_email_sent));
        alertDialog.setMessage(messagePart1 + " \"" + email + "\"\n"+ messagePart2);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, context.getString(R.string.sign_in_google_alert_button_text_ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();

        final Button alertDialogButton = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
        LinearLayout.LayoutParams alertDialogButtonLayoutParams = (LinearLayout.LayoutParams) alertDialogButton.getLayoutParams();
        alertDialogButtonLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        alertDialogButton.setLayoutParams(alertDialogButtonLayoutParams);
    }
/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu_search; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_setting_only, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // On ActionBar Back Press
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, ActivitySettings.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
*/
    private static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte b : data) {
            int halfbyte = (b >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte) : (char) ('a' + (halfbyte - 10)));
                halfbyte = b & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    public static String SHA1(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] textBytes = text.getBytes("UTF-8");
        md.update(textBytes, 0, textBytes.length);
        byte[] sha1hash = md.digest();
        return convertToHex(sha1hash);
    }
}
