package com.bozorgzad.ali.introducingtvus;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/*
 * Created by Ali_Dev on 8/24/2017.
 */

public class ActivityUserEditProfileAndSignOut extends ActivityAppBarBackOrCloseButton {

    private EditText edtUserNameUserInfo;
    private EditText edtUserEmailUserInfo;
    private TextView txtResetPassword;
    private boolean isSignInWithGoogle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_edit_profile_and_sign_out);

        // Hide elevation under the Toolbar
        AppBarLayout ablSignInAndRegister = (AppBarLayout) findViewById(R.id.ablBackOrCloseButton);
        ablSignInAndRegister.setStateListAnimator(null);

        // Set Toolbar
        setBackOrCloseToolbar(true, getString(R.string.navigation_header_edit_profile_sign_out));

        // Set userName and userEmail
        edtUserNameUserInfo = (EditText) findViewById(R.id.edtUserNameUserInfo);
        edtUserEmailUserInfo = (EditText) findViewById(R.id.edtUserEmailUserInfo);
        edtUserNameUserInfo.setText(Global.currentUser.userName);
        edtUserEmailUserInfo.setText(Global.currentUser.userEmail);

        // change EditText divider color when focus change
        changeEditTextDividerColor();

        // Set Gravity and Drawable for EditText when language change
        setGravityAndDrawableForEditText();

        // If this account has ever been signIn with Google account, disable Email
        disableEmailEditWhenSignInWithGoogle();

        // if user doesn't have a password yet, show createPassword instead of resetPassword
        txtResetPassword = (TextView) findViewById(R.id.txtResetPassword);
        if (Global.currentUser.isPasswordSet == 0) {
            txtResetPassword.setText(R.string.user_edit_profile_account_create_password);
        }

        // setOnClickListener for each account item and profileChangeButton
        whenClickOnEachItem();
    }

    private void changeEditTextDividerColor(){
        final TextView txtDividerUserName = (TextView) findViewById(R.id.txtDividerUserName);
        final TextView txtDividerUserEmail = (TextView) findViewById(R.id.txtDividerUserEmail);
        final int colorDefault = ContextCompat.getColor(this, R.color.user_edit_profile_divider_item);
        final int colorActivated = ContextCompat.getColor(this, R.color.user_edit_profile_edit_text_activated_color);

        edtUserNameUserInfo.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    txtDividerUserName.setBackgroundColor(colorActivated);
                } else {
                    txtDividerUserName.setBackgroundColor(colorDefault);
                }
            }
        });
        edtUserEmailUserInfo.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    txtDividerUserEmail.setBackgroundColor(colorActivated);
                } else {
                    txtDividerUserEmail.setBackgroundColor(colorDefault);
                }
            }
        });
    }

    private void setGravityAndDrawableForEditText(){
        if (Global.LANGUAGE.equals("fa")) {
            edtUserNameUserInfo.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            edtUserEmailUserInfo.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            edtUserNameUserInfo.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_account_circle, 0);
            edtUserEmailUserInfo.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_email, 0);
        } else {
            edtUserNameUserInfo.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            edtUserEmailUserInfo.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            edtUserNameUserInfo.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_account_circle, 0, 0, 0);
            edtUserEmailUserInfo.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_email, 0, 0, 0);
        }
    }

    private void disableEmailEditWhenSignInWithGoogle(){
        if (Global.currentUser.userSignInWithGoogle == 1) {
            edtUserEmailUserInfo.setAlpha(0.5f);
            edtUserEmailUserInfo.setFocusable(false);
            edtUserNameUserInfo.setImeOptions(EditorInfo.IME_ACTION_DONE);
        }
        edtUserEmailUserInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Global.currentUser.userSignInWithGoogle == 1) {
                    Toast.makeText(ActivityUserEditProfileAndSignOut.this, R.string.user_edit_profile_cannot_change_email_toast, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void whenClickOnEachItem(){
        txtResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userResetPassword(Global.currentUser.userEmail);
            }
        });


        TextView txtClearSearchHistory = (TextView) findViewById(R.id.txtClearSearchHistory);
        txtClearSearchHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = getString(R.string.user_edit_profile_account_clear_search_history);
                String message = getString(R.string.user_edit_profile_account_clear_search_history_message);
                String positiveButtonText = getString(R.string.user_edit_profile_account_clear_search_history_positive_button);
                String negativeButtonText = getString(android.R.string.no);
                askUser(title, message, positiveButtonText, negativeButtonText, "ClearSearchHistory");
            }
        });


        TextView txtSignOut = (TextView) findViewById(R.id.txtSignOut);
        txtSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ActivityUserEditProfileAndSignOut.this);
                isSignInWithGoogle = sharedPreferences.getBoolean(ActivityUserGoogleSignIn.IS_SIGN_IN_WITH_GOOGLE, false);

                if(isSignInWithGoogle){
                    ActivityUserGoogleSignIn.mGoogleApiClient.connect();
                }

                String title = getString(R.string.user_edit_profile_account_sign_out);
                String message = getString(R.string.user_edit_profile_account_sign_out_message);
                String positiveButtonText = getString(R.string.user_edit_profile_account_sign_out_positive_button);
                String negativeButtonText = getString(android.R.string.no);
                askUser(title, message, positiveButtonText, negativeButtonText, "SignOut");
            }
        });


        Button btnApplyProfileChange = (Button) findViewById(R.id.btnApplyProfileChange);
        btnApplyProfileChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptApplyProfileChange();
            }
        });
    }

    private void userResetPassword(final String email) {
        if (!Global.isConnectedToNetwork(this)) {
            Toast.makeText(this, getString(R.string.no_internet_your_offline_please_check_network), Toast.LENGTH_LONG).show();
            return;
        }

        Global.showProgressDialog(getString(R.string.progress_dialog_wait), this);
        Uri.Builder builder = new Uri.Builder()
                .appendQueryParameter("userEmail", email);

        new WebHttpConnection(Global.HOST_ADDRESS + "/webservice/resetPassword", builder) {
            @Override
            public void onPostExecute(String result) {
                Global.hideProgressDialog();
                if (result != null) {
                    if (result.equals("EmailSent")) {
                        if (Global.currentUser.isPasswordSet == 0) {
                            ActivityUserSignInAndRegister.showAlertDialogEmailSent(ActivityUserEditProfileAndSignOut.this, email, true);
                        } else {
                            ActivityUserSignInAndRegister.showAlertDialogEmailSent(ActivityUserEditProfileAndSignOut.this, email, false);
                        }
                    }
                } else {
                    Toast.makeText(ActivityUserEditProfileAndSignOut.this, getString(R.string.internet_unavailable_text), Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    private void clearSearchHistory() {
        if (!Global.isConnectedToNetwork(this)) {
            Toast.makeText(this, getString(R.string.no_internet_your_offline_please_check_network), Toast.LENGTH_LONG).show();
            return;
        }

        Global.showProgressDialog(getString(R.string.progress_dialog_wait), this);
        Uri.Builder builder = new Uri.Builder()
                .appendQueryParameter("userId", String.valueOf(Global.currentUser.user_id));

        new WebHttpConnection(Global.HOST_ADDRESS + "/webservice/clearSearchHistory", builder) {
            @Override
            public void onPostExecute(final String result) {
                Global.hideProgressDialog();
                if (result != null) {
                    Toast.makeText(ActivityUserEditProfileAndSignOut.this, R.string.user_edit_profile_account_clear_search_history_successfully, Toast.LENGTH_LONG).show();
                    Global.currentUser.userRecentSearches = "";
                } else {
                    Toast.makeText(ActivityUserEditProfileAndSignOut.this, R.string.internet_unavailable_text, Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    private void signOut(){
        if (!Global.isConnectedToNetwork(this)) {
            Toast.makeText(this, getString(R.string.no_internet_your_offline_please_check_network), Toast.LENGTH_LONG).show();
            return;
        }

        if(isSignInWithGoogle){
            ActivityUserGoogleSignIn.signOutWithGoogle(this);
        }else{
            ActivityUserGoogleSignIn.signOut(this);
        }
        Toast.makeText(this, getString(R.string.user_edit_profile_account_sign_out_successfully), Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, ActivityMain.class);
        startActivity(intent);
    }

    private void attemptApplyProfileChange(){
        // Store values at the time of the userSignIn attempt.
        String name = edtUserNameUserInfo.getText().toString();
        String email = edtUserEmailUserInfo.getText().toString();

        // if nothing has changed finish
        if(Global.currentUser.userName.equals(name) && Global.currentUser.userEmail.equals(email)){
            finish();
            return;
        }

        // Reset errors.
        edtUserNameUserInfo.setError(null);
        edtUserEmailUserInfo.setError(null);

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (Global.LANGUAGE.equals("fa")){
                edtUserEmailUserInfo.setError(getString(R.string.sign_in_register_sign_in_invalid_email_address), null);
            }else{
                edtUserEmailUserInfo.setError(getString(R.string.sign_in_register_sign_in_invalid_email_address));
            }
            focusView = edtUserEmailUserInfo;
            cancel = true;
        }

        // Check for a valid name.
        if (name.isEmpty() || name.length() < 3) {
            if (Global.LANGUAGE.equals("fa")){
                edtUserNameUserInfo.setError(getString(R.string.sign_in_register_at_least_3_characters), null);
            }else{
                edtUserNameUserInfo.setError(getString(R.string.sign_in_register_at_least_3_characters));
            }
            focusView = edtUserNameUserInfo;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt signIn and focus the first form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and perform the user userRegister
            applyProfileChange(name, email);
        }
    }

    private void applyProfileChange(final String name, final String email){
        if(!Global.isConnectedToNetwork(this)){
            Toast.makeText(this, getString(R.string.no_internet_your_offline_please_check_network), Toast.LENGTH_LONG).show();
            return;
        }

        Global.showProgressDialog(getString(R.string.progress_dialog_wait), this);
        Uri.Builder builder = new Uri.Builder()
                .appendQueryParameter("userId", String.valueOf(Global.currentUser.user_id))
                .appendQueryParameter("userName", name)
                .appendQueryParameter("userEmail", email);

        new WebHttpConnection(Global.HOST_ADDRESS + "/webservice/applyProfileChange", builder){
            @Override public void onPostExecute(String result)
            {
                Global.hideProgressDialog();
                if(result != null){
                    if(result.equals("EmailDuplicate")){
                        edtUserEmailUserInfo.setError(getString(R.string.user_edit_profile_email_duplicate));
                        edtUserEmailUserInfo.requestFocus();
                    }else{
                        Toast.makeText(ActivityUserEditProfileAndSignOut.this, R.string.user_edit_profile_change_successfully, Toast.LENGTH_SHORT).show();
                        Global.currentUser.userName = name;
                        Global.currentUser.userEmail = email;
                        finish();
                    }
                }else{
                    Toast.makeText(ActivityUserEditProfileAndSignOut.this, getString(R.string.internet_unavailable_text), Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    private void askUser(String title, String message, String positiveButtonText, String negativeButtonText, final String askType){
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, positiveButtonText,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(askType.equals("ClearSearchHistory")){
                            clearSearchHistory();
                        }else if(askType.equals("SignOut")){
                            signOut();
                        }else if(askType.equals("UnsavedChanges")){
                            finish();
                        }
                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, negativeButtonText,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();

        Button btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button btnNegative = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) btnPositive.getLayoutParams();
        layoutParams.weight = 10;
        btnPositive.setLayoutParams(layoutParams);
        btnNegative.setLayoutParams(layoutParams);
    }

    @Override
    public boolean onSupportNavigateUp() {
        String userName = edtUserNameUserInfo.getText().toString();
        String userEmail = edtUserEmailUserInfo.getText().toString();

        if(Global.currentUser.userName.equals(userName) && Global.currentUser.userEmail.equals(userEmail)){
            finish();
        }else{
            String title = getString(R.string.user_edit_profile_unsaved_changes_title);
            String message = getString(R.string.user_edit_profile_unsaved_changes_message);
            String positiveButtonText = getString(R.string.user_edit_profile_unsaved_changes_positive_button);
            String negativeButtonText = getString(R.string.user_edit_profile_unsaved_changes_negative_button);
            askUser(title, message, positiveButtonText, negativeButtonText, "UnsavedChanges");
        }
        return false;
    }
}