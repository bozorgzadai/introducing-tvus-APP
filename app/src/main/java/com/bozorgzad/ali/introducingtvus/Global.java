package com.bozorgzad.ali.introducingtvus;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

/*
 * Created by Ali_Dev on 7/7/2017.
 */

public class Global extends Application {

    public static String HOST_ADDRESS;

    public static final String CURRENT_LANGUAGE = "CurrentLanguage";
    public static String LANGUAGE;
    public static boolean firstEnter = true;
    public static boolean firstTimeEnterDisableSignInSignOut = true;
    public static user currentUser;

    private static ProgressDialog mProgressDialog;

    @Override
    public void onCreate() {
        super.onCreate();
        HOST_ADDRESS = getApplicationContext().getString(R.string.full_host_name);
//        HOST_ADDRESS = "http://192.168.13.199/tvu";

        currentUser = new user();

        // Get the current Language
        getTheCurrentLanguageAndSetVoiceSearch();
    }

    private void getTheCurrentLanguageAndSetVoiceSearch() {
        // Get SharedPreferences And Editor
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor spEditor = sharedPreferences.edit();

        String currentLanguage = sharedPreferences.getString(CURRENT_LANGUAGE, "");
        if (currentLanguage.isEmpty()) {
            LANGUAGE = "fa";
            spEditor.putString(CURRENT_LANGUAGE, LANGUAGE);
            spEditor.apply();
        } else {
            LANGUAGE = currentLanguage;
        }

        String currentVoiceSearchLanguage = sharedPreferences.getString(ActivitySettings.CURRENT_VOICE_SEARCH_LANGUAGE, "");
        if (currentVoiceSearchLanguage.equals("")) {
            spEditor.putString(ActivitySettings.CURRENT_VOICE_SEARCH_LANGUAGE, ActivitySettings.CURRENT_SOFTWARE_LANGUAGE);
            spEditor.apply();
        }
    }

    public static Boolean isConnectedToNetwork(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    public static void checkInternetConnection(final Activity activity, LinearLayout llHeaderProgress, LinearLayout llNotConnectedToInternet) {
        if (Global.isConnectedToNetwork(activity)) {
            return;
        }

        llHeaderProgress.setVisibility(View.VISIBLE);
        llNotConnectedToInternet.setVisibility(View.VISIBLE);

        llNotConnectedToInternet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(), R.string.no_internet_toast, Toast.LENGTH_SHORT).show();
            }
        });

        final Handler handler = new Handler();
        final int delay = 2000; //milliseconds

        handler.postDelayed(new Runnable() {
            public void run() {
                if (Global.isConnectedToNetwork(activity)) {
                    handler.removeCallbacks(this);
                    activity.recreate();
                } else {
                    handler.postDelayed(this, delay);
                }
            }
        }, 0);
    }

    public static void setRecentTabsTaskDescription(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String appName = activity.getString(R.string.app_name);
            Bitmap iconBitmap = BitmapFactory.decodeResource(activity.getResources(), R.mipmap.ic_launcher);
            int color = ContextCompat.getColor(activity, R.color.recent_apps_title_background_color);

            ActivityManager.TaskDescription taskDescription = new ActivityManager.TaskDescription(appName, iconBitmap, color);
            activity.setTaskDescription(taskDescription);
        }
    }

    public static void showProgressDialog(String message, Context context) {
        mProgressDialog = new ProgressDialog(context, R.style.Theme_AppCompat_DayNight_Dialog);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage(fromHtml("<b>" + message + "</b>"));
        mProgressDialog.show();
    }

    public static void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
            //mProgressDialog.hide();
        }
    }

    @SuppressWarnings("deprecation")
    private static Spanned fromHtml(String html) {
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
    }

    public static class user {
        int user_id;
        String userName;
        String userEmail;
        int isPasswordSet;
        int userSignInWithGoogle;
        String userRecentSearches;
    }
}