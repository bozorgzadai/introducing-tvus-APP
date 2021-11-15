package com.bozorgzad.ali.introducingtvus;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

/*
 * Created by Ali_Dev on 9/10/2017.
 */

// We didn't extends from ActivityAboutUs, because we want users can open AboutUs when offline
public class ActivityAboutUs extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        // Set Toolbar
        setToolbar();

        // set Version name
        TextView txtAppVersion = (TextView) findViewById(R.id.txtAppVersion);
        txtAppVersion.setText(BuildConfig.VERSION_NAME);

        // OnClick for tvu site
        TextView txtSiteAddress = (TextView) findViewById(R.id.txtSiteAddress);
        txtSiteAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://tvu.ac.ir"); // missing 'http://' will cause crashed
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
    }

    private void setToolbar(){
        Toolbar toolbarAboutUs = (Toolbar) findViewById(R.id.toolbarAboutUs);
        toolbarAboutUs.setTitle(R.string.about_us_toolbar_title);
        setSupportActionBar(toolbarAboutUs);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
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

        return super.onOptionsItemSelected(item);
    }
}
