package com.bozorgzad.ali.introducingtvus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bozorgzad.ali.introducingtvus.common.logger.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

public class ActivityMain extends ActivityAppBarNavigationDrawer {

    private ArrayList<String> imageSliderMain = new ArrayList<>();
    private int             imageSliderDelay = 6000;
    private int             imageSliderOriginalCount;
    private int             tempCountMainImageSlider;
    private boolean         goForward = true;
    private ViewPager       viewPagerMainSlider;
    private ImageSliderPagerAdapter imageSliderPagerAdapter;
    private ImageSliderPageIndicator imageSliderPageIndicatorMain;

    private ArrayList<MostViewedUni> mostViewedBoysUnis = new ArrayList<>();
    private ArrayList<MostViewedUni> mostViewedGirlsUnis = new ArrayList<>();
    private AdapterRvMostViewedUnis rvAdapterMostViewedBoysUnis;
    private AdapterRvMostViewedUnis rvAdapterMostViewedGirlsUnis;
    class MostViewedUni {
        int uni_id;
        String uniFullNameFa;
        String uniFullNameEn;
        String uniLogo;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Make sure this is before calling super.onCreate
        setTheme(R.style.AppTheme_NoActionBar);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set Progress Bar
        llHeaderProgressMain.setVisibility(View.VISIBLE);

        //SignIn user when enter app, if not signOut before exit
        if(Global.firstTimeEnterDisableSignInSignOut){
            signInUserWhenEnterApp();
        }

        /// ImageSlider Main///
        mainImageSlider();
        fetchImageSliderMain();
        imageSliderMainRound();

        /// Most Viewed Universities Horizontal RecyclerView ///
        mostViewedUniversities();
        fetchMostViewedUnis();
    }

    private void signInUserWhenEnterApp(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int userId = sharedPreferences.getInt(ActivityUserGoogleSignIn.USER_ID, -1);
        if(userId != -1){
            boolean isSignInWithGoogle = sharedPreferences.getBoolean(ActivityUserGoogleSignIn.IS_SIGN_IN_WITH_GOOGLE, false);

            if(isSignInWithGoogle){
                // We don't check network here, because google can fetch some data without internet, if we SignIn before
                ActivityUserGoogleSignIn.signInWhenEnterApp(this);
            }else{
                if(connectedToNetwork){
                    ActivityUserSignInAndRegister.userSignInByIdWhenEnterApp(userId, this);
                }
            }
        }
    }

    private void mainImageSlider(){
        imageSliderPageIndicatorMain = (ImageSliderPageIndicator) findViewById(R.id.pageIndicatorMain);
        viewPagerMainSlider = (ViewPager) findViewById(R.id.viewPagerMainSlider);
        imageSliderPagerAdapter = new ImageSliderPagerAdapter(imageSliderMain);
        viewPagerMainSlider.setAdapter(imageSliderPagerAdapter);

        viewPagerMainSlider.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                imageSliderPageIndicatorMain.setPercent(positionOffset);
                imageSliderPageIndicatorMain.setCurrentPage(position);
            }

            @Override
            public void onPageSelected(int position) {
                tempCountMainImageSlider = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void imageSliderMainRound(){
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(tempCountMainImageSlider >= imageSliderOriginalCount){
                    tempCountMainImageSlider = imageSliderOriginalCount - (imageSliderOriginalCount - 1);
                    goForward = false;
                }else if(tempCountMainImageSlider < 0){
                    tempCountMainImageSlider = 1;
                    goForward = true;
                }

                viewPagerMainSlider.setCurrentItem(tempCountMainImageSlider);
                if(goForward){
                    tempCountMainImageSlider++;
                }else{
                    tempCountMainImageSlider--;
                }

                handler.postDelayed(this, imageSliderDelay);
            }
        };
        handler.postDelayed(runnable, imageSliderDelay);
    }

    private void mostViewedUniversities(){
        RecyclerView rvMostViewedBoysUnis = (RecyclerView) findViewById(R.id.rvMostViewedBoysUnis);
        RecyclerView rvMostViewedGirlsUnis = (RecyclerView) findViewById(R.id.rvMostViewedGirlsUnis);
        LinearLayout llSeeAllMostViewedBoysUnis = (LinearLayout) findViewById(R.id.llSeeAllMostViewedBoysUnis);
        LinearLayout llSeeAllMostViewedGirlsUnis = (LinearLayout) findViewById(R.id.llSeeAllMostViewedGirlsUnis);

        LinearLayoutManager LLMMostViewedBoysUnis = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false);
        rvMostViewedBoysUnis.setLayoutManager(LLMMostViewedBoysUnis);
        rvAdapterMostViewedBoysUnis = new AdapterRvMostViewedUnis(mostViewedBoysUnis);
        rvMostViewedBoysUnis.setAdapter(rvAdapterMostViewedBoysUnis);

        LinearLayoutManager LLMMostViewedGirlsUnis = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false);
        rvMostViewedGirlsUnis.setLayoutManager(LLMMostViewedGirlsUnis);
        rvAdapterMostViewedGirlsUnis = new AdapterRvMostViewedUnis(mostViewedGirlsUnis);
        rvMostViewedGirlsUnis.setAdapter(rvAdapterMostViewedGirlsUnis);


        llSeeAllMostViewedBoysUnis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ActivitySearchUni.class);
                intent.putExtra("WhichSearchUniTab", getString(R.string.find_uni_tab_boys));
                intent.putExtra("SeeAllMostViewedUnis", true);
                startActivity(intent);
            }
        });

        llSeeAllMostViewedGirlsUnis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ActivitySearchUni.class);
                intent.putExtra("WhichSearchUniTab", getString(R.string.find_uni_tab_girls));
                intent.putExtra("SeeAllMostViewedUnis", true);
                startActivity(intent);
            }
        });
    }

    private void fetchImageSliderMain(){
        if(!connectedToNetwork){
            return;
        }

        new WebHttpConnection(Global.HOST_ADDRESS + "/webservice/fetchImageSliderMain" , null)
        {
            @Override public void onPostExecute(String result)
            {
                if(result != null){
                    try {
                        JSONObject object = new JSONObject(result);
                        String imageSliderMainPhotos = object.getString("images");
                        String[] separatedPhotos = imageSliderMainPhotos.split("\\|");
                        imageSliderMain.clear();
                        Collections.addAll(imageSliderMain, separatedPhotos);

                        imageSliderPageIndicatorMain.setIndicatorsCount(imageSliderMain.size());
                        imageSliderOriginalCount = imageSliderMain.size();
                        imageSliderPagerAdapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else{
                    llInternetUnavailableMain.setVisibility(View.VISIBLE);
                }
            }
        }.execute();

    }

    private void fetchMostViewedUnis(){
        if(!connectedToNetwork){
            return;
        }

        new WebHttpConnection(Global.HOST_ADDRESS + "/webservice/fetchMostViewedUnis" , null)
        {
            @Override public void onPostExecute(String result)
            {
                if(result != null){
                    try {
                        JSONArray array = new JSONArray(result);
                        mostViewedBoysUnis.clear();
                        mostViewedGirlsUnis.clear();

                        for (int i = 0 ; i < array.length() ; i++) {
                            JSONObject object = array.getJSONObject(i);

                            MostViewedUni mostViewedBoysUni = new MostViewedUni();
                            MostViewedUni mostViewedGirlsUni = new MostViewedUni();

                            String fileType = object.getString("uniGender");
                            if(fileType.equals("boys")){
                                mostViewedBoysUni.uni_id = object.getInt("uni_id");
                                mostViewedBoysUni.uniFullNameFa = object.getString("uniFullNameFa");
                                mostViewedBoysUni.uniFullNameEn = object.getString("uniFullNameEn");
                                mostViewedBoysUni.uniLogo= object.getString("uniLogo");
                                mostViewedBoysUnis.add(mostViewedBoysUni);
                            }else if(fileType.equals("girls")){
                                mostViewedGirlsUni.uni_id = object.getInt("uni_id");
                                mostViewedGirlsUni.uniFullNameFa = object.getString("uniFullNameFa");
                                mostViewedGirlsUni.uniFullNameEn = object.getString("uniFullNameEn");
                                mostViewedGirlsUni.uniLogo = object.getString("uniLogo");
                                mostViewedGirlsUnis.add(mostViewedGirlsUni);
                            }
                        }

                        rvAdapterMostViewedBoysUnis.notifyDataSetChanged();
                        rvAdapterMostViewedGirlsUnis.notifyDataSetChanged();

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // Do something after 200ms

                                // Enable onClick for signIn and editProfile
                                txtSignInOrRegister.setAlpha(1);
                                llEditProfileOrSignOut.setAlpha(1);
                                Global.firstTimeEnterDisableSignInSignOut = false;
                                llHeaderProgressMain.setVisibility(View.GONE);
                            }
                        }, 200);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.execute();
    }
}
