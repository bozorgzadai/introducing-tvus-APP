package com.bozorgzad.ali.introducingtvus;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

public class ActivityUniDetails extends ActivityParent implements OnMapReadyCallback {

    private ArrayList<String> imageSliderUniDetails = new ArrayList<>();
    private int             imageSliderDelay = 6000;
    private int             imageSliderOriginalCount;
    private int tempCountUniDetailsImageSlider;
    private boolean         goForward = true;
    private ViewPager viewPagerUniDetailsSlider;
    private ImageSliderPagerAdapter imageSliderPagerAdapter;
    private ImageSliderPageIndicator imageSliderPageIndicatorUniDetails;

    private int minDescLineNumber = 4;
    private TextView txtLittleDesc;
    private TextView txtUniWebsiteData;

    private SupportMapFragment mapFragment;
    private String uniShortName;
    private Double lat = 0.0;
    private Double lng = 0.0;

    private int uni_id;
    private String uniFullName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uni_details);

        // Set Progress Bar
        llHeaderProgressParent.setVisibility(View.VISIBLE);

        // uni site name, underline and intent to the browser
        txtUniWebsiteData = (TextView) findViewById(R.id.txtUniWebsiteData);
        txtUniWebSiteDateOnClickAndUnderline();

        /// Get Id from Previous Activity AND FetchUniDetailsById & SetToolbar & LittleDescriptionMoreAndLess///
        int whichUniDetailsId = 0;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            whichUniDetailsId = extras.getInt("WhichUniDetailsId");
        }
        fetchUniDetailsById(whichUniDetailsId);

        /// ImageSlider UniDetails ///
        uniDetailsImageSlider();
        imageSliderUniDetailsRound();

        // More Info And UserReview Buttons With Set Font
        moreInfoAndUserReview();

        /// Google Map View ///
        googleMapView();
    }

    private void txtUniWebSiteDateOnClickAndUnderline(){
        txtUniWebsiteData.setPaintFlags(txtUniWebsiteData.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        txtUniWebsiteData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(txtUniWebsiteData.getText().toString()); // missing 'http://' will cause crashed
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
    }

    private void fetchUniDetailsById(int whichUniDetailsId){
        if (!connectedToNetworkParent) {
            return;
        }

        txtLittleDesc = (TextView) findViewById(R.id.txtLittleDesc);
        final ImageView imgUniLogo = (ImageView) findViewById(R.id.imgUniLogo);
        final TextView txtUniFullNameData = (TextView) findViewById(R.id.txtUniFullNameData);
        final TextView txtUniEstablishedData = (TextView) findViewById(R.id.txtUniEstablishedData);
        final TextView txtUniTypeData = (TextView) findViewById(R.id.txtUniTypeData);
        final TextView txtUniPresidentData = (TextView) findViewById(R.id.txtUniPresidentData);
        final TextView txtEducationalAssistantData = (TextView) findViewById(R.id.txtEducationalAssistantData);
        final TextView txtStudentAssistantData = (TextView) findViewById(R.id.txtStudentAssistantData);
        final TextView txtStudentNumberData = (TextView) findViewById(R.id.txtStudentNumberData);
        final TextView txtAssociateDegreeMajorsData = (TextView) findViewById(R.id.txtAssociateDegreeMajorsData);
        final TextView txtBachelorDegreeMajorsData = (TextView) findViewById(R.id.txtBachelorDegreeMajorsData);
        final TextView txtAffiliationsData = (TextView) findViewById(R.id.txtAffiliationsData);
        final TextView txtUniStateData = (TextView) findViewById(R.id.txtUniStateData);
        final TextView txtUniCityData = (TextView) findViewById(R.id.txtUniCityData);
        final TextView txtUniAddressData = (TextView) findViewById(R.id.txtUniAddressData);

        Uri.Builder builder = new Uri.Builder()
                .appendQueryParameter("uni_id", String.valueOf(whichUniDetailsId));

        new WebHttpConnection(Global.HOST_ADDRESS + "/webservice/fetchUniDetailsById" , builder)
        {
            @Override public void onPostExecute(String result)
            {
                if(result != null){
                    try {
                        JSONArray array = new JSONArray(result);
                        JSONObject object = array.getJSONObject(0);

                        if(Global.LANGUAGE.equals("fa")){
                            uniShortName = object.getString("uniShortNameFa");
                            uniFullName = object.getString("uniFullNameFa");
                            txtUniFullNameData.setText(uniFullName);
                            txtUniTypeData.setText(object.getString("uniTypeFa"));
                            txtUniPresidentData.setText(object.getString("uniPresidentFa"));
                            txtEducationalAssistantData.setText(object.getString("uniEducationalAssistantFa"));
                            txtStudentAssistantData.setText(object.getString("uniStudentAssistantFa"));
                            txtAssociateDegreeMajorsData.setText(object.getString("uniAssociateDegreeMajorsFa"));
                            txtBachelorDegreeMajorsData.setText(object.getString("uniBachelorDegreeMajorsFa"));
                            txtAffiliationsData.setText(object.getString("uniAffiliationsFa"));
                            txtLittleDesc.setText(object.getString("uniLittleDescFa"));
                            txtUniStateData.setText(object.getString("uniStateFa"));
                            txtUniCityData.setText(object.getString("uniCityFa"));
                            txtUniAddressData.setText(object.getString("uniAddressFa"));

                            txtUniEstablishedData.setGravity(Gravity.RIGHT);
                            txtStudentNumberData.setGravity(Gravity.RIGHT);
                        }else{
                            uniShortName = object.getString("uniShortNameEn");
                            uniFullName = object.getString("uniFullNameEn");
                            txtUniFullNameData.setText(uniFullName);
                            txtUniTypeData.setText(object.getString("uniTypeEn"));
                            txtUniPresidentData.setText(object.getString("uniPresidentEn"));
                            txtEducationalAssistantData.setText(object.getString("uniEducationalAssistantEn"));
                            txtStudentAssistantData.setText(object.getString("uniStudentAssistantEn"));
                            txtAssociateDegreeMajorsData.setText(object.getString("uniAssociateDegreeMajorsEn"));
                            txtBachelorDegreeMajorsData.setText(object.getString("uniBachelorDegreeMajorsEn"));
                            txtAffiliationsData.setText(object.getString("uniAffiliationsEn"));
                            txtLittleDesc.setText(object.getString("uniLittleDescEn"));
                            txtUniStateData.setText(object.getString("uniStateEn"));
                            txtUniCityData.setText(object.getString("uniCityEn"));
                            txtUniAddressData.setText(object.getString("uniAddressEn"));
                        }

                        uni_id = object.getInt("uni_id");
                        txtUniEstablishedData.setText(object.getString("uniEstablished"));
                        txtStudentNumberData.setText(object.getString("uniStudentNumber"));
                        txtUniWebsiteData.setText(object.getString("uniWebsite"));
                        Picasso.with(imgUniLogo.getContext())
                                .load(Global.HOST_ADDRESS + object.getString("uniLogo"))
                                .placeholder(R.drawable.ic_loading)
                                .error(R.drawable.ic_no_image)
                                .into(imgUniLogo);

                        // Google Map
                        String latLng = object.getString("uniLatLng");
                        if(! latLng.equals("null")){
                            String[] separatedLatLng = latLng.split(",");
                            separatedLatLng[0] = separatedLatLng[0].trim();
                            separatedLatLng[1] = separatedLatLng[1].trim();
                            lat = Double.valueOf(separatedLatLng[0]);
                            lng = Double.valueOf(separatedLatLng[1]);
                        }
                        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                        mapFragment.getMapAsync(ActivityUniDetails.this);

                        // Image Slider
                        String uniPhotos = object.getString("uniPhotos");
                        if(!uniPhotos.equals("null")){
                            String[] separatedPhotos = uniPhotos.split("\\|");
                            Collections.addAll(imageSliderUniDetails, separatedPhotos);
                        }
                        imageSliderPageIndicatorUniDetails.setIndicatorsCount(imageSliderUniDetails.size());
                        imageSliderOriginalCount = imageSliderUniDetails.size();
                        imageSliderPagerAdapter.notifyDataSetChanged();

                        /// Set Toolbar ///
                        setToolbar();

                        /// Little Description More And Less ///
                        littleDescriptionMoreAndLess();

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
                    }
                }else{
                    llInternetUnavailableParent.setVisibility(View.VISIBLE);
                }
            }
        }.execute();
    }

    private void setToolbar(){
        Toolbar toolbarUniDetails = (Toolbar) findViewById(R.id.toolbarUniDetails);
        toolbarUniDetails.setTitle(uniShortName);
        setSupportActionBar(toolbarUniDetails);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void littleDescriptionMoreAndLess(){
        final TextView txtReadMoreAndLess = (TextView) findViewById(R.id.txtReadMoreAndLess);
        final LinearLayout llDivider = (LinearLayout) findViewById(R.id.llDivider);

        if(txtLittleDesc.getLineCount() < minDescLineNumber){
            txtReadMoreAndLess.setVisibility(View.GONE);
            llDivider.setVisibility(View.GONE);
        }else{
            txtLittleDesc.setMaxLines(minDescLineNumber);
        }

        txtReadMoreAndLess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String readMore = getString(R.string.uni_detail_read_more);
                if(txtReadMoreAndLess.getText().equals(readMore)){
                    readMoreAndLessAnimation(txtLittleDesc.getLineCount());
                    txtReadMoreAndLess.setText(getString(R.string.uni_detail_read_less));
                }else{
                    readMoreAndLessAnimation(minDescLineNumber);
                    txtReadMoreAndLess.setText(getString(R.string.uni_detail_read_more));
                }
            }
        });
    }

    private void readMoreAndLessAnimation(int maxLineNumber){
        ObjectAnimator animation = ObjectAnimator.ofInt(
                txtLittleDesc,
                "maxLines",
                maxLineNumber);
        animation.setDuration(200);
        animation.start();
    }

    private void moreInfoAndUserReview(){
        LinearLayout llMoreInfo = (LinearLayout) findViewById(R.id.llMoreInfo);
        LinearLayout llUserReviews = (LinearLayout) findViewById(R.id.llUserReviews);
        TextView txtReviews = (TextView) findViewById(R.id.txtReviews);
        TextView txtMoreInfo = (TextView) findViewById(R.id.txtMoreInfo);

        Typeface font = Typeface.createFromAsset( getAssets(), "fonts/fontawesome-webfont.ttf");
        txtReviews.setTypeface(font);
        txtMoreInfo.setTypeface(font);

        llMoreInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ActivityUniMoreInfo.class);
                intent.putExtra("WhichUniMoreInfoId", uni_id);
                intent.putExtra("WhichUniMoreInfoFullName", uniFullName);
                startActivity(intent);
            }
        });

        llUserReviews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ActivityUniReviews.class);
                intent.putExtra("WhichUniReviewsId", uni_id);
                intent.putExtra("WhichUniReviewsShortName", uniShortName);
                intent.putExtra("WhichUniReviewsFullName", uniFullName);
                startActivity(intent);
            }
        });
    }

    private void googleMapView(){
        Button btnShowInMaps = (Button) findViewById(R.id.btnShowInMaps);

        btnShowInMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri = "geo:" +lat+ "," +lng+ "?z=17";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(intent);
            }
        });
    }

    private void uniDetailsImageSlider(){
        imageSliderPageIndicatorUniDetails = (ImageSliderPageIndicator) findViewById(R.id.pageIndicatorUniDetails);
        viewPagerUniDetailsSlider = (ViewPager) findViewById(R.id.viewPagerUniDetailsSlider);
        imageSliderPagerAdapter = new ImageSliderPagerAdapter(imageSliderUniDetails);
        viewPagerUniDetailsSlider.setAdapter(imageSliderPagerAdapter);

        viewPagerUniDetailsSlider.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                imageSliderPageIndicatorUniDetails.setPercent(positionOffset);
                imageSliderPageIndicatorUniDetails.setCurrentPage(position);
            }

            @Override
            public void onPageSelected(int position) {
                tempCountUniDetailsImageSlider = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void imageSliderUniDetailsRound(){
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(tempCountUniDetailsImageSlider >= imageSliderOriginalCount){
                    tempCountUniDetailsImageSlider = imageSliderOriginalCount - (imageSliderOriginalCount - 1);
                    goForward = false;
                }else if(tempCountUniDetailsImageSlider < 0){
                    tempCountUniDetailsImageSlider = 1;
                    goForward = true;
                }

                viewPagerUniDetailsSlider.setCurrentItem(tempCountUniDetailsImageSlider);
                if(goForward){
                    tempCountUniDetailsImageSlider++;
                }else{
                    tempCountUniDetailsImageSlider--;
                }

                handler.postDelayed(this, imageSliderDelay);
            }
        };
        handler.postDelayed(runnable, imageSliderDelay);
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
            onBackPressed();
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng mineLatLng = new LatLng(lat, lng);
        googleMap.addMarker(new MarkerOptions().position(mineLatLng).title(uniShortName)).showInfoWindow();
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mineLatLng,17));
        googleMap.getUiSettings().setMapToolbarEnabled(true);
    }
}