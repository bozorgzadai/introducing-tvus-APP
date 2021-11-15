package com.bozorgzad.ali.introducingtvus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.LayoutRes;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

/*
 * Created by Ali_Dev on 7/20/2017.
 */

// We didn't extends from ActivityParent, because we want users can open apps when offline
// When we do this, the user can access the menu and navigation drawer when offline(put the menu on top)
public class ActivityAppBarNavigationDrawer extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static LinearLayout llHeaderProgressMain;
    public static LinearLayout llInternetUnavailableMain;
    public static boolean connectedToNetwork;

    private LinearLayout llNavHeader;
    private TextView txtUserEmailHeader;
    public TextView txtSignInOrRegister;
    public LinearLayout llEditProfileOrSignOut;

    private TextView txtChangeLanguage;
    private BroadcastReceiver toggleSignInAndSignOutReceiver;

    public static final String TOGGLE_SIGN_IN_AND_SIGN_OUT = "ToggleSignInAndSignOut";

    @Override
    protected void onResume() {
        super.onResume();
        if(llInternetUnavailableMain.getVisibility() == View.VISIBLE){
            recreate();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Global.firstEnter){
            toggleLanguage(this);
            Global.firstEnter = false;
        }
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID)
    {
        super.setContentView(R.layout.navigation_drawer_main);

        ViewStub viewStubNavigationDrawer = (ViewStub) findViewById(R.id.viewStubNavigationDrawer);
        viewStubNavigationDrawer.setLayoutResource(layoutResID);
        viewStubNavigationDrawer.inflate();

        // Set Background Color For Recent Apps
        Global.setRecentTabsTaskDescription(this);

        // For Check The Internet
            // ProgressBar For Loading Data From Internet & If Internet Unavailable
            llHeaderProgressMain = (LinearLayout) findViewById(R.id.llHeaderProgressMain);
            llInternetUnavailableMain = (LinearLayout) findViewById(R.id.llInternetUnavailableMain);

            // Check Internet Connection
            LinearLayout llNotConnectedToInternetMain = (LinearLayout) findViewById(R.id.llNotConnectedToInternetMain);
            Global.checkInternetConnection(this, llHeaderProgressMain, llNotConnectedToInternetMain);
            connectedToNetwork = Global.isConnectedToNetwork(this);

        onCreateDrawer();
        toggleTxtChangeLanguage();
        toggleSignInAndSignOut();
        defineAndRegisterToggleSignInSignOutReceiver();
    }

    protected void onCreateDrawer() {
        /* Use these lines for child toolbar
            Toolbar toolbarNavigationDrawer = (Toolbar) findViewById(R.id.toolbarNavigationDrawer);
            DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            toolbarNavigationDrawer.setTitle("Title");
            setSupportActionBar(toolbarNavigationDrawer);

            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawerLayout, toolbarNavigationDrawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.setDrawerListener(toggle);
            toggle.syncState();*/

        Toolbar toolbarNavigationDrawer = (Toolbar) findViewById(R.id.toolbarNavigationDrawer);
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        View header = navigationView.getHeaderView(0);
        Button btnInternetUnavailableMain = (Button) findViewById(R.id.btnInternetUnavailableMain);
        llNavHeader = (LinearLayout) header.findViewById(R.id.llNavHeader);
        ImageView imgHeader = (ImageView) header.findViewById(R.id.imgHeader);
        txtSignInOrRegister = (TextView) header.findViewById(R.id.txtSignInOrRegister);
        txtUserEmailHeader = (TextView) header.findViewById(R.id.txtUserEmailHeader);
        llEditProfileOrSignOut = (LinearLayout) header.findViewById(R.id.llEditProfileOrSignOut);
        txtChangeLanguage = (TextView) header.findViewById(R.id.txtChangeLanguage);

        // Set Toolbar Title
        toolbarNavigationDrawer.setTitle(R.string.app_name);
        setSupportActionBar(toolbarNavigationDrawer);
        navigationView.setNavigationItemSelectedListener(this);

        // Toolbar open and close button
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbarNavigationDrawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        // When click on imgHeader in Navigation Drawer
        imgHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ActivityMain.class);
                startActivity(intent);
            }
        });

        if(Global.firstTimeEnterDisableSignInSignOut){
            // Disable onClick for signIn and editProfile
            txtSignInOrRegister.setAlpha(0.5f);
            llEditProfileOrSignOut.setAlpha(0.5f);
        }
        txtSignInOrRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(txtSignInOrRegister.getAlpha() == 0.5f){
                    Toast.makeText(view.getContext(), R.string.no_internet_toast, Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(view.getContext(), ActivityUserGoogleSignIn.class);
                startActivity(intent);
            }
        });

        llEditProfileOrSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(llEditProfileOrSignOut.getAlpha() == 0.5f){
                    Toast.makeText(view.getContext(), R.string.no_internet_toast, Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(view.getContext(), ActivityUserEditProfileAndSignOut.class);
                startActivity(intent);
            }
        });

        // When click on Language in Navigation Drawer
        txtChangeLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Global.LANGUAGE.equals("fa")){
                    Global.LANGUAGE = "en";
                    toggleLanguage(v.getContext());
                }else{
                    Global.LANGUAGE = "fa";
                    toggleLanguage(v.getContext());
                }
            }
        });

        btnInternetUnavailableMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recreate();
            }
        });
    }

    public static void toggleLanguage(Context context){
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        android.content.res.Configuration conf = res.getConfiguration();
        Locale local = new Locale(Global.LANGUAGE);
        conf.setLocale(local); // API 17+ only.
        Locale.setDefault(local);
        res.updateConfiguration(conf, dm);

        if(!Global.firstEnter){
            // Get SharedPreferences And Editor
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor spEditor = sharedPreferences.edit();
            spEditor.putString(Global.CURRENT_LANGUAGE, Global.LANGUAGE);
            spEditor.apply();

            Intent intent = new Intent(context, ActivityMain.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
        }
    }

    private void toggleTxtChangeLanguage(){
        if(Global.LANGUAGE.equals("fa")){
            txtChangeLanguage.setText(R.string.app_language_name_en);
        }else{
            txtChangeLanguage.setText(R.string.app_language_name_fa);
        }
    }

    private void defineAndRegisterToggleSignInSignOutReceiver(){
        // Define receiver
        toggleSignInAndSignOutReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                toggleSignInAndSignOut();
            }
        };

        // Register receiver
        registerReceiver(toggleSignInAndSignOutReceiver, new IntentFilter(TOGGLE_SIGN_IN_AND_SIGN_OUT));
    }

    private void toggleSignInAndSignOut(){
        ViewGroup.LayoutParams params = llNavHeader.getLayoutParams();

        if(Global.currentUser.userEmail == null){
            txtSignInOrRegister.setVisibility(View.VISIBLE);
            llEditProfileOrSignOut.setVisibility(View.GONE);

            params.height = (int) getResources().getDimension(R.dimen.nav_header_height_sign_out);
        }else{
            txtSignInOrRegister.setVisibility(View.GONE);
            llEditProfileOrSignOut.setVisibility(View.VISIBLE);

            params.height = (int) getResources().getDimension(R.dimen.nav_header_height_sign_in);
        }
        llNavHeader.setLayoutParams(params);
        txtUserEmailHeader.setText(Global.currentUser.userEmail);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu_search; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            if(Global.firstTimeEnterDisableSignInSignOut){
                Toast.makeText(this, R.string.no_internet_toast, Toast.LENGTH_SHORT).show();
                return true;
            }
            Intent intent = new Intent(this, ActivitySettings.class);
            startActivity(intent);

            return true;
        }else if(id == R.id.action_search_main){
            Intent intent = new Intent(this, ActivitySearchUni.class);
            this.startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.navHome) {
            Intent intent = new Intent(this, ActivityMain.class);
            startActivity(intent);
        } else if (id == R.id.navMostViewedUnis) {
            Intent intent = new Intent(this, ActivitySearchUni.class);
            intent.putExtra("WhichSearchUniTab", getString(R.string.find_uni_tab_boys));
            intent.putExtra("SeeAllMostViewedUnis", true);
            startActivity(intent);
        } else if (id == R.id.navSearch) {
            Intent intent = new Intent(this, ActivitySearchUni.class);
            this.startActivity(intent);
        } else if (id == R.id.navSettings) {
            if(Global.firstTimeEnterDisableSignInSignOut){
                Toast.makeText(this, R.string.no_internet_toast, Toast.LENGTH_SHORT).show();
                return true;
            }
            Intent intent = new Intent(this, ActivitySettings.class);
            startActivity(intent);
        } else if (id == R.id.navAboutUs) {
            Intent intent = new Intent(this, ActivityAboutUs.class);
            startActivity(intent);
        } else if (id == R.id.navShare) {
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.app_name_navigation));
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.share_app_text));
            startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_app_title)));
        } else if (id == R.id.navFeedback) {
            Intent intent = new Intent(this, ActivityFeedback.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterReceiver(toggleSignInAndSignOutReceiver);
        toggleSignInAndSignOutReceiver = null;
    }

    private void unRegisterReceiver(BroadcastReceiver broadcastReceiver){
        if (broadcastReceiver != null) {
            try {
                unregisterReceiver(broadcastReceiver);
            } catch (final Exception e){
                e.printStackTrace();
            }
        }
    }
}
