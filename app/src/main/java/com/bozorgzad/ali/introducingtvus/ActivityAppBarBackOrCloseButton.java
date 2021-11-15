package com.bozorgzad.ali.introducingtvus;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.support.annotation.LayoutRes;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;

/*
 * Created by Ali_Dev on 7/20/2017.
 */

public class ActivityAppBarBackOrCloseButton extends ActivityParent {

    @Override
    public void setContentView(@LayoutRes int layoutResID )
    {
        super.setContentView(R.layout.app_bar_close_button);

        ViewStub viewStubCloseButton = (ViewStub) findViewById(R.id.viewStubCloseButton);
        viewStubCloseButton.setLayoutResource(layoutResID);
        viewStubCloseButton.inflate();
    }

    public void setBackOrCloseToolbar(boolean isCloseButton, String toolbarTitle){
        Toolbar toolbarCloseButton = (Toolbar) findViewById(R.id.toolbarCloseButton);
        toolbarCloseButton.setLayoutDirection(View.LAYOUT_DIRECTION_LOCALE); // some Activity like ChangePassword have a different direction
        toolbarCloseButton.setTitle(toolbarTitle);
        setSupportActionBar(toolbarCloseButton);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            if(isCloseButton){
                actionBar.setHomeAsUpIndicator(R.drawable.ic_action_close);
            }
        }
    }

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
}
