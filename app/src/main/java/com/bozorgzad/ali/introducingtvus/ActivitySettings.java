package com.bozorgzad.ali.introducingtvus;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/*
 * Created by Ali_Dev on 7/12/2017.
 */

public class ActivitySettings extends ActivityAppBarBackOrCloseButton {

    public static final String CURRENT_VOICE_SEARCH_LANGUAGE = "CurrentVoiceSearchLanguage";
    public static final String CURRENT_SOFTWARE_LANGUAGE = "CurrentSoftwareLanguage";

    private int checkedSoftwareLanguage = 0;
    private int checkedVoiceSearchLanguage = 0;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor spEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Set Toolbar
        setBackOrCloseToolbar(false, getString(R.string.settings_toolbar_title));

        // Get SharedPreferences And Editor
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        spEditor = sharedPreferences.edit();

        // set current software language and onClick for change it
        setAndChangeSoftwareLanguage();

        // set current voice search language and onClick for change it
        setAndChangeVoiceSearchLanguage();

        // setOnClickListener for each item in System & Other Setting
        whenClickOnEachItemInSetting();
    }

    private void setAndChangeSoftwareLanguage(){
        final TextView txtCurrentSoftwareLanguage = (TextView) findViewById(R.id.txtCurrentSoftwareLanguage);
        if(Global.LANGUAGE.equals("fa")){
            txtCurrentSoftwareLanguage.setText(R.string.app_language_name_fa);
            checkedSoftwareLanguage = 1;
        }else{
            txtCurrentSoftwareLanguage.setText(R.string.app_language_name_en);
            checkedSoftwareLanguage = 0;
        }

        LinearLayout llSoftwareLanguage = (LinearLayout) findViewById(R.id.llSoftwareLanguage);
        llSoftwareLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CharSequence[] filterListNames = {getString(R.string.app_language_name_en), getString(R.string.app_language_name_fa)};

                AlertDialog.Builder alt_bld = new AlertDialog.Builder(v.getContext());
                alt_bld.setTitle(R.string.settings_language_choose_language);

                alt_bld.setSingleChoiceItems(filterListNames, checkedSoftwareLanguage, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        checkedSoftwareLanguage = item;
                        dialog.dismiss();
                        if(item == 0){
                            Global.LANGUAGE = "en";
                            ActivityAppBarNavigationDrawer.toggleLanguage(ActivitySettings.this);
                        }else{
                            Global.LANGUAGE = "fa";
                            ActivityAppBarNavigationDrawer.toggleLanguage(ActivitySettings.this);
                        }
                    }
                });
                AlertDialog alert = alt_bld.create();
                alert.show();
            }
        });
    }

    private void setAndChangeVoiceSearchLanguage(){
        final TextView txtVoiceSearchLanguage = (TextView) findViewById(R.id.txtVoiceSearchLanguage);

        String currentVoiceSearchLanguage = sharedPreferences.getString(CURRENT_VOICE_SEARCH_LANGUAGE, "");
        if(currentVoiceSearchLanguage.equals(CURRENT_SOFTWARE_LANGUAGE)){
            txtVoiceSearchLanguage.setText(R.string.settings_language_current_software_language);
            checkedVoiceSearchLanguage = 0;
        }else if(currentVoiceSearchLanguage.equals(getString(R.string.app_language_name_en))){
            txtVoiceSearchLanguage.setText(R.string.app_language_name_en);
            checkedVoiceSearchLanguage = 1;
        }else{
            txtVoiceSearchLanguage.setText(R.string.app_language_name_fa);
            checkedVoiceSearchLanguage = 2;
        }

        LinearLayout llVoiceSearchAssistant = (LinearLayout) findViewById(R.id.llVoiceSearchAssistant);
        llVoiceSearchAssistant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CharSequence[] filterListNames = {getString(R.string.settings_language_current_software_language), getString(R.string.app_language_name_en), getString(R.string.app_language_name_fa)};

                AlertDialog.Builder alt_bld = new AlertDialog.Builder(v.getContext());
                alt_bld.setTitle(R.string.settings_language_choose_voice_search_language);

                alt_bld.setSingleChoiceItems(filterListNames, checkedVoiceSearchLanguage, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        checkedVoiceSearchLanguage = item;
                        dialog.dismiss();
                        if(item == 0){
                            txtVoiceSearchLanguage.setText(R.string.settings_language_current_software_language);
                            spEditor.putString(CURRENT_VOICE_SEARCH_LANGUAGE, CURRENT_SOFTWARE_LANGUAGE);
                        }else if(item == 1){
                            txtVoiceSearchLanguage.setText(R.string.app_language_name_en);
                            spEditor.putString(CURRENT_VOICE_SEARCH_LANGUAGE, getString(R.string.app_language_name_en));
                        }else{
                            txtVoiceSearchLanguage.setText(R.string.app_language_name_fa);
                            spEditor.putString(CURRENT_VOICE_SEARCH_LANGUAGE, getString(R.string.app_language_name_fa));
                        }
                        spEditor.apply();
                    }
                });
                AlertDialog alert = alt_bld.create();
                alert.show();
            }
        });
    }

    private void whenClickOnEachItemInSetting(){
        LinearLayout llViewAppInSystemSettings = (LinearLayout) findViewById(R.id.llViewAppInSystemSettings);
        llViewAppInSystemSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        });


        LinearLayout llClearSearchHistory = (LinearLayout) findViewById(R.id.llClearSearchHistory);
        llClearSearchHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askUserForClearSearchHistorySavedOnPhone();
            }
        });


        LinearLayout llAccountManagement = (LinearLayout) findViewById(R.id.llAccountManagement);
        llAccountManagement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Global.currentUser.user_id == 0){
                    Intent intent = new Intent(v.getContext(), ActivityUserGoogleSignIn.class);
                    startActivity(intent);
                }else{
                    Intent intent = new Intent(v.getContext(), ActivityUserEditProfileAndSignOut.class);
                    startActivity(intent);
                }
            }
        });


        LinearLayout llAboutUs = (LinearLayout) findViewById(R.id.llAboutUs);
        llAboutUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivitySettings.this, ActivityAboutUs.class);
                startActivity(intent);
            }
        });
    }

    private void askUserForClearSearchHistorySavedOnPhone(){
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getString(R.string.settings_system_clear_search_history_title));
        alertDialog.setMessage(getString(R.string.settings_system_clear_search_history_message));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.settings_system_clear_search_history_positive_button),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        spEditor.putString(ActivitySearchUni.RECENT_SEARCH_TAG, "");
                        spEditor.apply();
                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(android.R.string.no),
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
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
}
