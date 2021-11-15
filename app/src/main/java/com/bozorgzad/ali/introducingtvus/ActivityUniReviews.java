package com.bozorgzad.ali.introducingtvus;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/*
 * Created by Ali_Dev on 8/31/2017.
 */

public class ActivityUniReviews extends ActivityAppBarBackOrCloseButton {

    private static AdapterRvUniReviews rvAdapterUniReviews;
    private UniReviews currentUserReview = new UniReviews();
    private static ArrayList<UniReviews> uniReviews = new ArrayList<>();
    static class UniReviews {
        int uniReview_id;
        int user_id;
        String reviewUserName;
        String reviewDate;
        double reviewRate;
        String reviewText;
        int reviewUpVote;
        int reviewDownVote;
        int reviewEdited;
        int currentUserVoted;
    }

    private String uniReviewsFullName;
    public static double averageOfRates;
    public static int numberOfReviews;

    private static int uniId;
    public static boolean noInternetWhileLazyLoading;
    public static boolean whileChangingOrderBy;
    public static String uniReviewsShortName;
    public static String currentOrderBy;
    private static LinearLayout llReviewProgressBar;
    private static RecyclerView rvUniReviews;

    private FloatingActionButton fabWriteReview;
    private static LinearLayout llNoReview;

    private boolean isComeBackFromSignInNewAndEditReview = false;
    private static boolean callFabWriteReview = false;
    public static boolean isComeBackFromSignIn = false;
    @Override
    protected void onResume() {
        super.onResume();
        if(isComeBackFromSignInNewAndEditReview && Global.currentUser.user_id != 0){
            callFabWriteReview = true;
            recreate();
        }else if(isComeBackFromSignIn && Global.currentUser.user_id != 0){
            recreate();
        }
        isComeBackFromSignInNewAndEditReview = false;
        isComeBackFromSignIn = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uni_reviews);

        // Set Progress Bar
        llHeaderProgressParent.setVisibility(View.VISIBLE);

        // set default values
        setStaticVariablesToDefaultValue();

        /// Get Id from Previous Activity ///
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            uniId = extras.getInt("WhichUniReviewsId");
            uniReviewsShortName = extras.getString("WhichUniReviewsShortName");
            uniReviewsFullName = extras.getString("WhichUniReviewsFullName");
        }

        // fetch AVG and number of reviews for header
        llNoReview = (LinearLayout) findViewById(R.id.llNoReview);
        fetchAvgAndNumberOfReviewsAndCurrentReview();

        // fetch all of the reviews for this uni
        llReviewProgressBar = (LinearLayout) findViewById(R.id.llReviewProgressBar);
        rvUniReviews = (RecyclerView) findViewById(R.id.rvUniReviews);
        fetchUniReviewsByUniId(this, "FirstEnter");

        // Set Toolbar
        setBackOrCloseToolbar(true, getString(R.string.uni_reviews_toolbar_title));

        // fabWriteReview onClick
        fabWriteReview = (FloatingActionButton) findViewById(R.id.fabWriteReview);
        fabWriteReviewOnClick();

        // Create recyclerView and setOnLoadMore
        createRVAndSetOnLoadMore();
    }

    private void setStaticVariablesToDefaultValue(){
        rvAdapterUniReviews = null;
        uniReviews.clear();
        averageOfRates = 0;
        numberOfReviews = 0;
        noInternetWhileLazyLoading = false;
        whileChangingOrderBy = false;
        currentOrderBy = "Top";
        rvUniReviews = null;
        isComeBackFromSignIn = false;
    }

    private void fabWriteReviewOnClick(){
        fabWriteReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Global.currentUser.user_id == 0){
                    Intent intent = new Intent(ActivityUniReviews.this, ActivityUserGoogleSignIn.class);
                    startActivity(intent);
                    isComeBackFromSignInNewAndEditReview = true;
                }else{
                    DialogNewAndEditReview dialogNewAndEditReview = new DialogNewAndEditReview(ActivityUniReviews.this, uniId, currentUserReview.uniReview_id, currentUserReview.reviewRate, currentUserReview.reviewText);
                    dialogNewAndEditReview.show();
                }
            }
        });
    }

    private void createRVAndSetOnLoadMore(){
        LinearLayoutManager LLMUniReviews = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false);
        rvUniReviews.setLayoutManager(LLMUniReviews);
        rvAdapterUniReviews = new AdapterRvUniReviews(this, uniReviews, rvUniReviews, uniReviewsFullName);
        rvUniReviews.setAdapter(rvAdapterUniReviews);

        rvAdapterUniReviews.setOnLoadMoreListener(new AdapterRvUniReviews.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                fetchUniReviewsByUniId(ActivityUniReviews.this, "LazyLoading");
            }
        });
    }

    private void fetchAvgAndNumberOfReviewsAndCurrentReview(){
        if(!connectedToNetworkParent){
            return;
        }

        Uri.Builder builder = new Uri.Builder()
                .appendQueryParameter("userId", String.valueOf(Global.currentUser.user_id))
                .appendQueryParameter("uniId", String.valueOf(uniId));

        new WebHttpConnection(Global.HOST_ADDRESS + "/webservice/fetchAvgAndNumberOfReviewsAndCurrentReview", builder){
            @Override public void onPostExecute(String result)
            {
                if(result != null){
                    try {
                        JSONObject rootObject= new JSONObject(result);
                        JSONArray arrayCurrentUserReview = rootObject.getJSONArray("currentUserReview");
                        JSONArray arrayAvgAndNumberOfReviews = rootObject.getJSONArray("avgAndNumberOfReviews");

                        JSONObject objectAvgAndNumberOfReviews = arrayAvgAndNumberOfReviews.getJSONObject(0);
                        numberOfReviews = objectAvgAndNumberOfReviews.getInt("numberOfReviews");
                        averageOfRates = objectAvgAndNumberOfReviews.getDouble("average");

                        JSONObject objectCurrentUserReview = arrayCurrentUserReview.getJSONObject(0);
                        currentUserReview.uniReview_id = objectCurrentUserReview.getInt("uniReview_id");
                        currentUserReview.user_id = objectCurrentUserReview.getInt("user_id");
                        currentUserReview.reviewUserName = objectCurrentUserReview.getString("userName");
                        currentUserReview.reviewDate = objectCurrentUserReview.getString("reviewDate");
                        currentUserReview.reviewRate = objectCurrentUserReview.getDouble("reviewRate");
                        currentUserReview.reviewText = objectCurrentUserReview.getString("reviewText");
                        currentUserReview.reviewUpVote = objectCurrentUserReview.getInt("reviewUpVote");
                        currentUserReview.reviewDownVote = objectCurrentUserReview.getInt("reviewDownVote");
                        currentUserReview.reviewEdited = objectCurrentUserReview.getInt("reviewEdited");
                        currentUserReview.currentUserVoted = -1;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if(callFabWriteReview){
                        callFabWriteReview = false;
                        fabWriteReview.performClick();
                    }

                }else{
                    llInternetUnavailableParent.setVisibility(View.VISIBLE);
                }
            }
        }.execute();
    }

    public static void fetchUniReviewsByUniId(final Context context, final String enterType){
        int limitFrom = 0;
        int waitTime = 0;

        if(enterType.equals("FirstEnter")){
            if(!connectedToNetworkParent){
                return;
            }
            uniReviews.clear();
            waitTime = 500;
        }else if(enterType.equals("ChangeOrderBy")){
            llReviewProgressBar.setVisibility(View.VISIBLE);
            rvUniReviews.scrollToPosition(0);
            rvUniReviews.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });

            if(!Global.isConnectedToNetwork(context)){
                Toast.makeText(context, context.getString(R.string.no_internet_your_offline_please_check_network), Toast.LENGTH_LONG).show();
                return;
            }
            uniReviews.clear();
            waitTime = 150;
        }else if(enterType.equals("LazyLoading")){
            //add progress item
            uniReviews.add(null);
            rvAdapterUniReviews.notifyItemInserted(uniReviews.size() - 1);

            // reduce one for the progress bar
            limitFrom = uniReviews.size() - 1;

            if(!Global.isConnectedToNetwork(context)){
                Toast.makeText(context, context.getString(R.string.no_internet_your_offline_please_check_network), Toast.LENGTH_LONG).show();
                noInternetWhileLazyLoading = true;
                return;
            }
            waitTime = 100;
        }

        Uri.Builder builder = new Uri.Builder()
                .appendQueryParameter("uniId", String.valueOf(uniId))
                .appendQueryParameter("userId", String.valueOf(Global.currentUser.user_id))
                .appendQueryParameter("orderBy", currentOrderBy)
                .appendQueryParameter("limitFrom", String.valueOf(limitFrom));

        final int finalWaitTime = waitTime;
        new WebHttpConnection(Global.HOST_ADDRESS + "/webservice/fetchUniReviewsByUniId" , builder)
        {
            @Override public void onPostExecute(final String result)
            {
                if(result != null){

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Do something after 'waitTime' variable

                            try {
                                JSONObject rootObject= new JSONObject(result);
                                final JSONArray arrayAllReviews = rootObject.getJSONArray("allReviews");
                                JSONArray arrayAllReviewsUserVoted = rootObject.getJSONArray("allReviewsUserVoted");

                                if(enterType.equals("LazyLoading")){
                                    //remove progress item
                                    uniReviews.remove(uniReviews.size() - 1);
                                    rvAdapterUniReviews.notifyItemRemoved(uniReviews.size());
                                }
                                for (int i = 0 ; i < arrayAllReviews.length() ; i++) {
                                    UniReviews uniReview = new UniReviews();
                                    JSONObject object = arrayAllReviews.getJSONObject(i);

                                    uniReview.uniReview_id = object.getInt("uniReview_id");
                                    uniReview.user_id = object.getInt("user_id");
                                    uniReview.reviewUserName = object.getString("userName");
                                    uniReview.reviewDate = object.getString("reviewDate");
                                    uniReview.reviewRate = object.getDouble("reviewRate");
                                    uniReview.reviewText = object.getString("reviewText");
                                    uniReview.reviewUpVote = object.getInt("reviewUpVote");
                                    uniReview.reviewDownVote = object.getInt("reviewDownVote");
                                    uniReview.reviewEdited = object.getInt("reviewEdited");
                                    uniReview.currentUserVoted = -1;

                                    uniReviews.add(uniReview);
                                }

                                // find the reviews that user voted
                                for (int i = 0 ; i < arrayAllReviewsUserVoted.length() ; i++) {
                                    JSONObject object = arrayAllReviewsUserVoted.getJSONObject(i);

                                    for(int j=0; j < uniReviews.size(); j++){
                                        if(uniReviews.get(j).uniReview_id == object.getInt("uniReview_id")){
                                            uniReviews.get(j).currentUserVoted = object.getInt("vote");
                                        }
                                    }
                                }

                                rvAdapterUniReviews.notifyDataSetChanged();

                                if(enterType.equals("FirstEnter")){
                                    if(uniReviews.size() == 0){
                                        llNoReview.setVisibility(View.VISIBLE);
                                    }
                                    llHeaderProgressParent.setVisibility(View.GONE);
                                }else if(enterType.equals("ChangeOrderBy")){
                                    llReviewProgressBar.setVisibility(View.GONE);
                                    rvUniReviews.setOnTouchListener(new View.OnTouchListener() {
                                        @Override
                                        public boolean onTouch(View v, MotionEvent event) {
                                            return false;
                                        }
                                    });
                                    whileChangingOrderBy = false;
                                }else if(enterType.equals("LazyLoading")){
                                    // if fetch less data, set there is no more data available on the server
                                    if(arrayAllReviews.length() < 10){
                                        rvAdapterUniReviews.setMoreDataAvailable(false);
                                    }
                                    rvAdapterUniReviews.setLoaded();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }, finalWaitTime);
                }else{
                    if(! enterType.equals("FirstEnter")){
                        Toast.makeText(context, R.string.internet_unavailable_text, Toast.LENGTH_LONG).show();
                    }
                }
            }
        }.execute();
    }
}
