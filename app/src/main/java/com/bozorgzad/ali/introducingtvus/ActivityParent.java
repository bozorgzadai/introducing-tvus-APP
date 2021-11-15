package com.bozorgzad.ali.introducingtvus;

import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.LinearLayout;

/*
 * Created by Ali_Dev on 8/13/2017.
 */

// Every Activity extends from this Activity except 'ActivityAppBarNavigationDrawer' & 'ActivitySearchUni & ActivityAboutUs'
//                                                  (We explain the reason on top of each Activity)
public class ActivityParent extends AppCompatActivity {

    public static LinearLayout llHeaderProgressParent;
    public static LinearLayout llInternetUnavailableParent;
    public static boolean connectedToNetworkParent;

    @Override
    public void setContentView(@LayoutRes int layoutResID )
    {
        super.setContentView(R.layout.activity_parent);

        ViewStub viewStubRootButton = (ViewStub) findViewById(R.id.viewStubParent);
        viewStubRootButton.setLayoutResource(layoutResID);
        viewStubRootButton.inflate();

        llHeaderProgressParent = (LinearLayout) findViewById(R.id.llHeaderProgressParent);
        llInternetUnavailableParent = (LinearLayout) findViewById(R.id.llInternetUnavailableParent);
        Button btnInternetUnavailableRootButton = (Button) findViewById(R.id.btnInternetUnavailableParent);

        btnInternetUnavailableRootButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recreate();
            }
        });

        // Set Background Color For Recent Apps
        Global.setRecentTabsTaskDescription(this);

        // Check Internet Connection
        LinearLayout llNotConnectedToInternetParent = (LinearLayout) findViewById(R.id.llNotConnectedToInternetParent);
        Global.checkInternetConnection(this, llHeaderProgressParent, llNotConnectedToInternetParent);
        connectedToNetworkParent = Global.isConnectedToNetwork(this);
    }
}
