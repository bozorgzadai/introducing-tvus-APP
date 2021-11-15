package com.bozorgzad.ali.introducingtvus;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bozorgzad.ali.introducingtvus.common.logger.Log;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*
 * Created by Ali_Dev on 8/14/2017.
 */

public class ActivityUserGoogleSignIn extends ActivityAppBarBackOrCloseButton implements
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private static final int RC_SIGN_IN = 9001;
    public static final String USER_ID = "UserId";
    public static final String IS_SIGN_IN_WITH_GOOGLE = "IsSignInWithGoogle";
    public static GoogleApiClient mGoogleApiClient;

    public static boolean isComeFromSignInOrRegister = false;
    @Override
    protected void onResume() {
        super.onResume();
        if(isComeFromSignInOrRegister){
            ActivityUserGoogleSignIn.isComeFromSignInOrRegister = false;
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_google_sign_in);

        // Set Toolbar
        setBackOrCloseToolbar(false, getString(R.string.navigation_header_sign_in_register));

        // Hide elevation under the Toolbar
        AppBarLayout ablNavigation = (AppBarLayout) findViewById(R.id.ablBackOrCloseButton);
        ablNavigation.setStateListAnimator(null);

        // Set font for txtIcons
        TextView txtGoogleIcon = (TextView) findViewById(R.id.txtGoogleIcon);
        TextView txtEmailIcon = (TextView) findViewById(R.id.txtEmailIcon);
        Typeface font = Typeface.createFromAsset( getAssets(), "fonts/fontawesome-webfont.ttf");
        txtGoogleIcon.setTypeface(font);
        txtEmailIcon.setTypeface(font);

        // Set onClick for each item
        LinearLayout llSignInWithGoogle = (LinearLayout) findViewById(R.id.llSignInWithGoogle);
        LinearLayout llRegisterWithEmail = (LinearLayout) findViewById(R.id.llRegisterWithEmail);
        TextView txtSignIn = (TextView) findViewById(R.id.txtSignIn);
        llSignInWithGoogle.setOnClickListener(this);
        llRegisterWithEmail.setOnClickListener(this);
        txtSignIn.setOnClickListener(this);

        // Configure Google sign-in
        configureGoogleSignIn();
    }

    public static void signInWhenEnterApp(Activity activity){
        // LIKE "configureGoogleSignIn()" WITHOUT ONE LINE
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(activity)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            GoogleSignInResult result = opr.get();
            handleSignInResult(result, activity, true);
        }
    }

    private void configureGoogleSignIn(){
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.llSignInWithGoogle:
                signIn();
                break;

            case R.id.llRegisterWithEmail:
                Intent intentRegister = new Intent(view.getContext(), ActivityUserSignInAndRegister.class);
                intentRegister.putExtra("RegisterOrSignIn", getString(R.string.sign_in_register_toolbar_title));
                startActivity(intentRegister);
                break;

            case R.id.txtSignIn:
                Intent intentSignIn = new Intent(view.getContext(), ActivityUserSignInAndRegister.class);
                intentSignIn.putExtra("RegisterOrSignIn", getString(R.string.sign_in_sign_in_toolbar_title));
                startActivity(intentSignIn);
                break;
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not be available.
        Log.d("ActivityUserGoogleSignIn", "onConnectionFailed: " + connectionResult);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result, this, false);
        }
    }

    private static void handleSignInResult(GoogleSignInResult result, Activity activity, boolean isSignInWhenEnterApp) {
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();

            if (acct != null) {
                Global.currentUser.userName = acct.getDisplayName();
                Global.currentUser.userEmail = acct.getEmail();
                Global.currentUser.userSignInWithGoogle = 1;

                activity.sendBroadcast(new Intent(ActivityAppBarNavigationDrawer.TOGGLE_SIGN_IN_AND_SIGN_OUT));
                googleSignInOrRegister(acct.getDisplayName(), acct.getEmail(), activity, isSignInWhenEnterApp);
            }
        }else{
            if(result.getStatus().getStatusCode() == 8){
                showAlertDialogGooglePlayServiceProblem(activity);
            }else{
                Toast.makeText(activity, activity.getString(R.string.internet_unavailable_text), Toast.LENGTH_LONG).show();
            }
        }
    }

    private static void showAlertDialogGooglePlayServiceProblem(Context context){
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(context.getString(R.string.sign_in_google_your_google_play_services_has_problem_title));
        alertDialog.setMessage(context.getString(R.string.sign_in_google_your_google_play_services_has_problem));
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

    private static void googleSignInOrRegister(String userName, final String userEmail, final Activity activity, final boolean isSignInWhenEnterApp){
        if(!Global.isConnectedToNetwork(activity)){
            return;
        }
        Global.showProgressDialog(activity.getString(R.string.progress_dialog_wait), activity);
        Uri.Builder builder = new Uri.Builder()
                .appendQueryParameter("userName", userName)
                .appendQueryParameter("userEmail", userEmail);

        new WebHttpConnection(Global.HOST_ADDRESS + "/webservice/googleSignInOrRegister", builder){
            @Override public void onPostExecute(String result)
            {
                Global.hideProgressDialog();
                if(result != null){
                    try {
                        JSONObject object = new JSONObject(result);
                        String type = object.getString("Type");

                        if(type.equals("SignIn")){
                            // SignIn
                            JSONArray array = object.getJSONArray("userInfo");
                            JSONObject userInfoObject = array.getJSONObject(0);

                            Global.currentUser.user_id = userInfoObject.getInt("user_id");
                            Global.currentUser.userName = userInfoObject.getString("userName");
                            Global.currentUser.userRecentSearches = userInfoObject.getString("userRecentSearches");
                            if(userInfoObject.getString("userPassword").isEmpty()){
                                Global.currentUser.isPasswordSet = 0;
                            }else{
                                Global.currentUser.isPasswordSet = 1;
                            }

                            if(!isSignInWhenEnterApp){
                                Toast.makeText(activity, activity.getString(R.string.sign_in_sign_in_you_are_successfully_signed_in), Toast.LENGTH_LONG).show();
                            }
                        }else{
                            // Register
                            Global.currentUser.user_id = object.getInt("user_id");
                            Global.currentUser.userRecentSearches = "";
                            Global.currentUser.isPasswordSet = 0;

                            setRecentSearchesForFirstTimeRegister(activity);
                            Toast.makeText(activity, activity.getString(R.string.sign_in_register_you_are_successfully_registered), Toast.LENGTH_LONG).show();
                        }
                        changedPreferencesRelateToSignIn(activity, true);

                        if(!isSignInWhenEnterApp){
                            activity.finish();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.execute();
    }

    public static void setRecentSearchesForFirstTimeRegister(Context context){
        // Get the current RecentSearches
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String recentSearches = sharedPreferences.getString(ActivitySearchUni.RECENT_SEARCH_TAG, "");
        if(recentSearches.isEmpty()){
            return;
        }
        Global.currentUser.userRecentSearches = recentSearches;
        ActivitySearchUni.setRecentSearchesByUserId();
    }

    public static void changedPreferencesRelateToSignIn(Context context, boolean isSignInWithGoogle){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor spEditor = sharedPreferences.edit();
        spEditor.putInt(USER_ID, Global.currentUser.user_id);
        spEditor.putBoolean(IS_SIGN_IN_WITH_GOOGLE, isSignInWithGoogle);
        spEditor.putString(ActivitySearchUni.RECENT_SEARCH_TAG, "");
        spEditor.apply();
    }

    private void signIn() {
        if(!Global.isConnectedToNetwork(this)){
            Toast.makeText(ActivityUserGoogleSignIn.this, getString(R.string.no_internet_your_offline_please_check_network), Toast.LENGTH_LONG).show();
            return;
        }

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public static void signOutWithGoogle(final Activity activity) {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        signOut(activity);
                    }
                });
    }

    public static void signOut(Activity activity){
        Global.currentUser.user_id = 0;
        Global.currentUser.userName = null;
        Global.currentUser.userEmail = null;
        Global.currentUser.userSignInWithGoogle = 0;
        Global.currentUser.userRecentSearches = null;
        Global.currentUser.isPasswordSet = 1;

        activity.sendBroadcast(new Intent(ActivityAppBarNavigationDrawer.TOGGLE_SIGN_IN_AND_SIGN_OUT));

        // Get SharedPreferences And Editor
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor spEditor = sharedPreferences.edit();
        spEditor.putInt(USER_ID, -1);
        spEditor.putBoolean(IS_SIGN_IN_WITH_GOOGLE, false);
        spEditor.apply();
    }
}
