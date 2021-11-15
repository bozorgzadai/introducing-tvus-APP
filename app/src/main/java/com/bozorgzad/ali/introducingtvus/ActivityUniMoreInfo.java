package com.bozorgzad.ali.introducingtvus;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*
 * Created by Ali_Dev on 7/20/2017.
 */

public class ActivityUniMoreInfo extends ActivityAppBarBackOrCloseButton {

    private String whichUniMoreInfoFullName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uni_more_info);

        // Set Progress Bar
        llHeaderProgressParent.setVisibility(View.VISIBLE);

        // Set Toolbar
        setBackOrCloseToolbar(true, getString(R.string.uni_more_info_title));

        /// Get Id from Previous Activity AND FetchUniMoreInfoById ///
        int whichUniMoreInfoId = 0;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            whichUniMoreInfoId = extras.getInt("WhichUniMoreInfoId");
            whichUniMoreInfoFullName = extras.getString("WhichUniMoreInfoFullName");
        }
        fetchUniMoreInfoById(whichUniMoreInfoId);

        // When Click On Each Expandable Layout
        whenClickOnEachExpandableLayout();
    }

    private void fetchUniMoreInfoById(int whichUniMoreInfoId){
        if(!connectedToNetworkParent){
            return;
        }

        final TextView txtUniFullNameMoreInfoTitle = (TextView) findViewById(R.id.txtUniFullNameMoreInfoTitle);
        final TextView txtHistory = (TextView) findViewById(R.id.txtHistory);
        final TextView txtNames = (TextView) findViewById(R.id.txtNames);
        final TextView txtPresidents = (TextView) findViewById(R.id.txtPresidents);
        final TextView txtEducationalGroups = (TextView) findViewById(R.id.txtEducationalGroups);
        final TextView txtHonours = (TextView) findViewById(R.id.txtHonours);
        final TextView txtAdditionalExp = (TextView) findViewById(R.id.txtAdditionalExp);

        final LinearLayout llNamesFromBeginning = (LinearLayout) findViewById(R.id.llNamesFromBeginning);
        final LinearLayout llPresidentsFromBeginning = (LinearLayout) findViewById(R.id.llPresidentsFromBeginning);
        final LinearLayout llEducationalGroupsAndMajors = (LinearLayout) findViewById(R.id.llEducationalGroupsAndMajors);
        final LinearLayout llHonours = (LinearLayout) findViewById(R.id.llHonours);
        final LinearLayout llAdditionalExplanations = (LinearLayout) findViewById(R.id.llAdditionalExplanations);
        
        
        Uri.Builder builder = new Uri.Builder()
                .appendQueryParameter("uni_id", String.valueOf(whichUniMoreInfoId));

        new WebHttpConnection(Global.HOST_ADDRESS + "/webservice/fetchUniMoreInfoById" , builder)
        {
            @Override public void onPostExecute(String result)
            {
                if(result != null){
                    try {
                        JSONArray array = new JSONArray(result);
                        JSONObject object = array.getJSONObject(0);

                        txtUniFullNameMoreInfoTitle.setText(whichUniMoreInfoFullName);
                        if(Global.LANGUAGE.equals("fa")){
                            txtHistory.setText(object.getString("uniHistoryFa"));
                            txtNames.setText(setBulletForEachNewLine(object.getString("uniNamesFromTheBeginningFa")));
                            txtPresidents.setText(setBulletForEachNewLine(object.getString("uniPresidentsFromTheBeginningFa")));
                            txtEducationalGroups.setText(object.getString("uniEducationalGroupsAndMajorsFa"));
                            txtHonours.setText(setBulletForEachNewLine(object.getString("uniHonoursFa")));
                            txtAdditionalExp.setText(object.getString("uniAdditionalExplanationsFa"));
                        }else{
                            txtHistory.setText(object.getString("uniHistoryEn"));
                            txtNames.setText(setBulletForEachNewLine(object.getString("uniNamesFromTheBeginningEn")));
                            txtPresidents.setText(setBulletForEachNewLine(object.getString("uniPresidentsFromTheBeginningEn")));
                            txtEducationalGroups.setText(object.getString("uniEducationalGroupsAndMajorsEn"));
                            txtHonours.setText(setBulletForEachNewLine(object.getString("uniHonoursEn")));
                            txtAdditionalExp.setText(object.getString("uniAdditionalExplanationsEn"));
                        }

                        ifNoDataSetVisibilityToGone(txtNames, llNamesFromBeginning);
                        ifNoDataSetVisibilityToGone(txtPresidents, llPresidentsFromBeginning);
                        ifNoDataSetVisibilityToGone(txtEducationalGroups, llEducationalGroupsAndMajors);
                        ifNoDataSetVisibilityToGone(txtHonours, llHonours);
                        ifNoDataSetVisibilityToGone(txtAdditionalExp, llAdditionalExplanations);

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // Do something after 200ms
                                llHeaderProgressParent.setVisibility(View.GONE);
                            }
                        }, 200);

                    } catch (JSONException e) {
                        e.printStackTrace();

                        // for unis that not set data
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // Do something after 200ms
                                llHeaderProgressParent.setVisibility(View.GONE);
                            }
                        }, 200);
                    }
                }else{
                    llInternetUnavailableParent.setVisibility(View.VISIBLE);
                }
            }
        }.execute();
    }

    private String setBulletForEachNewLine(String text){
        text = "\u25CF " + text;
        for(int i=0; i<text.length(); i++)
        {
            if(text.charAt(i)=='\n')
            {
                text = text.substring(0, i) + "\n\u25CF " + text.substring(i+1);
            }
        }

        return text;
    }

    private void ifNoDataSetVisibilityToGone(TextView textView, LinearLayout linearLayout){
        // '2' is for bullet
        if(textView.getText().toString().length() <= 2){
            linearLayout.setVisibility(View.GONE);
        }
    }

    private void whenClickOnEachExpandableLayout(){
        Button btnExpandableHistory = (Button) findViewById(R.id.btnExpandableHistory);
        Button btnExpandableNames = (Button) findViewById(R.id.btnExpandableNames);
        Button btnExpandablePresidents = (Button) findViewById(R.id.btnExpandablePresidents);
        Button btnExpandableEducationalGroups = (Button) findViewById(R.id.btnExpandableEducationalGroups);
        Button btnExpandableHonours = (Button) findViewById(R.id.btnExpandableHonours);
        Button btnExpandableAdditionalExp = (Button) findViewById(R.id.btnExpandableAdditionalExp);

        View.OnClickListener llOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button btn = (Button) view;
                String tag = btn.getTag().toString();

                ViewGroup parent = (ViewGroup) btn.getParent();
                LinearLayout linearLayout = (LinearLayout) parent.getChildAt(1);

                if(tag.equals("true")){
                    btn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_expand, 0, 0, 0);
                    collapse(linearLayout);
                    btn.setTag("false");
                }else{
                    btn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_collapse, 0, 0, 0);
                    expand(linearLayout);
                    btn.setTag("true");
                }
            }
        };

        btnExpandableHistory.setOnClickListener(llOnClickListener);
        btnExpandableNames.setOnClickListener(llOnClickListener);
        btnExpandablePresidents.setOnClickListener(llOnClickListener);
        btnExpandableEducationalGroups.setOnClickListener(llOnClickListener);
        btnExpandableHonours.setOnClickListener(llOnClickListener);
        btnExpandableAdditionalExp.setOnClickListener(llOnClickListener);
    }

    public static void expand(final View view) {
        view.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        final int targetHeight = view.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        view.getLayoutParams().height = 1;
        view.setVisibility(View.VISIBLE);
        Animation animation = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                view.getLayoutParams().height = interpolatedTime == 1
                        ? LinearLayout.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                view.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 0.75dp/ms
        int number = (int) (targetHeight * 1.5);
        animation.setDuration((int)(number / view.getContext().getResources().getDisplayMetrics().density));
        view.startAnimation(animation);
    }

    public static void collapse(final View view) {
        final int initialHeight = view.getMeasuredHeight();

        Animation animation = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    view.setVisibility(View.GONE);
                }else{
                    view.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    view.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 0.75dp/ms
        int number = (int) (initialHeight * 1.5);
        animation.setDuration((int)(number / view.getContext().getResources().getDisplayMetrics().density));
        view.startAnimation(animation);
    }
}
