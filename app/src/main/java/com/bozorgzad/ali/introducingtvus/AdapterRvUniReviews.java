package com.bozorgzad.ali.introducingtvus;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.annotation.IdRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;

/*
 * Created by Ali_Dev on 9/1/2017.
 */

class AdapterRvUniReviews extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private final int TYPE_HEADER = 0;
    private final int TYPE_ITEM = 1;
    private final int VIEW_PROGRESS = 2;
    private ArrayList<ActivityUniReviews.UniReviews> uniReviews;
    private String uniReviewsFullName;
    private Activity activity;

    // Variables for LazyLoading
    // The minimum amount of items to have below your current scroll position before isLoading more.
    private int visibleThreshold = 2;
    private int lastVisibleItem;
    private int totalItemCount;
    private boolean isLoading;
    private boolean isMoreDataAvailable = true;
    private OnLoadMoreListener onLoadMoreListener;

    AdapterRvUniReviews(Activity activity, ArrayList<ActivityUniReviews.UniReviews> uniReviews, RecyclerView recyclerView, String uniReviewsFullName){
        this.activity = activity;
        this.uniReviews = uniReviews;
        this.uniReviewsFullName = uniReviewsFullName;

        // When LazyLoading call
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    totalItemCount = linearLayoutManager.getItemCount();
                    if(totalItemCount < 10){
                        totalItemCount = 10;
                    }
                    lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                    if (!isLoading && !ActivityUniReviews.whileChangingOrderBy && isMoreDataAvailable && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                        // End has been reached
                        // Do something
                        if (onLoadMoreListener != null) {
                            onLoadMoreListener.onLoadMore();
                        }
                        isLoading = true;
                    }
                }
            });
        }
    }

    private class VHHeader extends RecyclerView.ViewHolder {
        private TextView txtFullNameReview;
        private TextView txtUniRateAvg;
        private RatingBar rbUniRatingAvg;
        private TextView txtNumberOfUniReviews;
        private RadioGroup rgTopOrNew;

        VHHeader(View itemView) {
            super(itemView);

            txtFullNameReview = (TextView) itemView.findViewById(R.id.txtFullNameReview);
            txtUniRateAvg = (TextView) itemView.findViewById(R.id.txtUniRateAvg);
            rbUniRatingAvg = (RatingBar) itemView.findViewById(R.id.rbUniRatingAvg);
            txtNumberOfUniReviews = (TextView) itemView.findViewById(R.id.txtNumberOfUniReviews);
            txtNumberOfUniReviews = (TextView) itemView.findViewById(R.id.txtNumberOfUniReviews);
            rgTopOrNew = (RadioGroup) itemView.findViewById(R.id.rgTopOrNew);
        }
    }

    private class VHItem extends RecyclerView.ViewHolder {
        private TextView txtUserNameReview;
        private TextView txtReviewDate;
        private RatingBar rbUserReview;
        private TextView txtUserReview;
        private TextView txtUpVote;
        private TextView txtDownVote;
        private TextView txtReviewEdited;
        private LinearLayout llUpVote;
        private LinearLayout llDownVote;
        private LinearLayout llReportReview;
        private LinearLayout llDeleteReview;
        private LinearLayout llEditReview;
        private TextView txtIconThumbsUp;
        private TextView txtIconThumbsDown;

        VHItem(View itemView) {
            super(itemView);

            txtUserNameReview = (TextView) itemView.findViewById(R.id.txtUserNameReview);
            txtReviewDate = (TextView) itemView.findViewById(R.id.txtReviewDate);
            rbUserReview = (RatingBar) itemView.findViewById(R.id.rbUserReview);
            txtUserReview = (TextView) itemView.findViewById(R.id.txtUserReview);
            txtUpVote = (TextView) itemView.findViewById(R.id.txtUpVote);
            txtDownVote = (TextView) itemView.findViewById(R.id.txtDownVote);
            txtReviewEdited = (TextView) itemView.findViewById(R.id.txtReviewEdited);
            llUpVote = (LinearLayout) itemView.findViewById(R.id.llUpVote);
            llDownVote = (LinearLayout) itemView.findViewById(R.id.llDownVote);
            llReportReview = (LinearLayout) itemView.findViewById(R.id.llReportReview);
            llDeleteReview = (LinearLayout) itemView.findViewById(R.id.llDeleteReview);
            llEditReview = (LinearLayout) itemView.findViewById(R.id.llEditReview);

            // Set font for txtIcons
            TextView txtIconReview = (TextView) itemView.findViewById(R.id.txtIconReview);
            TextView txtIconDelete = (TextView) itemView.findViewById(R.id.txtIconDelete);
            TextView txtIconEditReview = (TextView) itemView.findViewById(R.id.txtIconEditReview);
            txtIconThumbsUp = (TextView) itemView.findViewById(R.id.txtIconThumbsUp);
            txtIconThumbsDown = (TextView) itemView.findViewById(R.id.txtIconThumbsDown);
            Typeface font = Typeface.createFromAsset(itemView.getContext().getAssets(), "fonts/fontawesome-webfont.ttf");
            txtIconReview.setTypeface(font);
            txtIconDelete.setTypeface(font);
            txtIconEditReview.setTypeface(font);
            txtIconThumbsUp.setTypeface(font);
            txtIconThumbsDown.setTypeface(font);

            // Scale icon for proper language
            // Set proper Gravity for txtUpVote for when don't have '+' in text
            if(Global.LANGUAGE.equals("en")){
                txtIconReview.setScaleX(-1);
                txtUpVote.setGravity(Gravity.LEFT);
            }else{
                txtIconEditReview.setScaleX(-1);
                txtUpVote.setGravity(Gravity.RIGHT);
            }
        }
    }

    private class VHProgress extends RecyclerView.ViewHolder {
        ProgressBar progressBarReview;

        VHProgress(View v) {
            super(v);
            progressBarReview = (ProgressBar) v.findViewById(R.id.progressBarReview);
        }
    }

    @Override
    public int getItemCount() {
        // plus one for header
        return uniReviews.size() + 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        RecyclerView.ViewHolder rvViewHolder;

        if (viewType == TYPE_HEADER) {
            View recycler_view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.rv_uni_review_header, viewGroup, false);
            rvViewHolder =  new VHHeader(recycler_view);
        }else if (viewType == TYPE_ITEM){
            View recycler_view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.rv_uni_review_item, viewGroup, false);
            rvViewHolder =  new VHItem(recycler_view);
        }else{
            View recycler_view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.rv_uni_review_progress, viewGroup, false);
            rvViewHolder =  new VHProgress(recycler_view);
        }

        return rvViewHolder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof VHHeader) {
            //cast holder to VHHeader and set data for header.

            // set uniName and number of review
            ((VHHeader) viewHolder).txtFullNameReview.setText(uniReviewsFullName);
            ((VHHeader) viewHolder).txtNumberOfUniReviews.setText(String.valueOf(ActivityUniReviews.numberOfReviews));

            // set AVG Number
            double average = Math.floor(ActivityUniReviews.averageOfRates * 10) / 10; // one digit after point
            ((VHHeader) viewHolder).txtUniRateAvg.setText(String.valueOf(average));
            ((VHHeader) viewHolder).rbUniRatingAvg.setRating((float) average);

            ((VHHeader) viewHolder).rgTopOrNew.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                    RadioButton radioButton = (RadioButton) group.findViewById(checkedId);
                    ActivityUniReviews.currentOrderBy = radioButton.getTag().toString();
                    ActivityUniReviews.fetchUniReviewsByUniId(radioButton.getContext(), "ChangeOrderBy");
                    ActivityUniReviews.whileChangingOrderBy = true;
                    isMoreDataAvailable = true;

                    // after noInternetWhileLazyLoading
                    if(ActivityUniReviews.noInternetWhileLazyLoading){
                        ActivityUniReviews.noInternetWhileLazyLoading = false;
                        isLoading = false;
                    }
                }
            });

        }else if (viewHolder instanceof VHItem) {
            // minus one position variable for header
            final ActivityUniReviews.UniReviews uniReview = uniReviews.get(position - 1);

            //cast holder to VHItem and set data
            // if this review is mine, show the edit and delete instead of report
            if(Global.currentUser.user_id == uniReview.user_id){
                ((VHItem) viewHolder).llEditReview.setVisibility(View.VISIBLE);
                ((VHItem) viewHolder).llDeleteReview.setVisibility(View.VISIBLE);
                ((VHItem) viewHolder).llReportReview.setVisibility(View.GONE);
            }else{
                ((VHItem) viewHolder).llEditReview.setVisibility(View.GONE);
                ((VHItem) viewHolder).llDeleteReview.setVisibility(View.GONE);
                ((VHItem) viewHolder).llReportReview.setVisibility(View.VISIBLE);
            }

            // if the review was edited, show the edited title
            if(uniReview.reviewEdited == 1){
                ((VHItem) viewHolder).txtReviewEdited.setVisibility(View.VISIBLE);
            }else{
                ((VHItem) viewHolder).txtReviewEdited.setVisibility(View.INVISIBLE);
            }

            // set appropriate date
            String[] reviewDateSplit = uniReview.reviewDate.split("-");
            int year = Integer.parseInt(reviewDateSplit[0]);
            int month = Integer.parseInt(reviewDateSplit[1]);
            int day = Integer.parseInt(reviewDateSplit[2].substring(0,2));
            String reviewDate;
            if(Global.LANGUAGE.equals("fa")){
                ConvertDate convertDate = new ConvertDate();
                reviewDate = convertDate.gregorianToJalali(day, month, year);
            }else{
                reviewDate = month+"/"+day+"/"+year;
            }

            // set values for each reviews
            ((VHItem) viewHolder).txtUserNameReview.setText(uniReview.reviewUserName);
            ((VHItem) viewHolder).txtReviewDate.setText(reviewDate);
            ((VHItem) viewHolder).rbUserReview.setRating((float) uniReview.reviewRate);
            ((VHItem) viewHolder).txtUserReview.setText(uniReview.reviewText);
            ((VHItem) viewHolder).txtDownVote.setText(String.valueOf(uniReview.reviewDownVote));
            setTxtUpVoteText(viewHolder, uniReview.reviewUpVote);

            // if current user vote this review, make the Thumbs colorFull
            if(uniReview.currentUserVoted == 0){
                setDownVoteColor(((VHItem) viewHolder).txtIconThumbsDown.getContext(), viewHolder);
                setVoteDefaultColor(((VHItem) viewHolder).txtIconThumbsUp.getContext(), viewHolder, true);
            }else if(uniReview.currentUserVoted == 1){
                setUpVoteColor(((VHItem) viewHolder).txtIconThumbsUp.getContext(), viewHolder);
                setVoteDefaultColor(((VHItem) viewHolder).txtIconThumbsDown.getContext(), viewHolder, false);
            }else{
                setVoteDefaultColor(((VHItem) viewHolder).txtIconThumbsUp.getContext(), viewHolder, true);
                setVoteDefaultColor(((VHItem) viewHolder).txtIconThumbsDown.getContext(), viewHolder, false);
            }

            // when click on voteUp
            // position variable cannot be final, so we set it in another variable
            final int uniReviewPosition = position;
            ((VHItem) viewHolder).llUpVote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(Global.currentUser.user_id == 0){
                        goToActivitySignIn(v.getContext());
                    }else{
                        setVote(v.getContext(), viewHolder, true, Global.currentUser.user_id, uniReview.uniReview_id, uniReviewPosition);
                    }
                }
            });

            // when click on voteDown
            ((VHItem) viewHolder).llDownVote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(Global.currentUser.user_id == 0){
                        goToActivitySignIn(v.getContext());
                    }else{
                        setVote(v.getContext(), viewHolder, false, Global.currentUser.user_id, uniReview.uniReview_id, uniReviewPosition);
                    }
                }
            });

            // when click on reportReview
            ((VHItem) viewHolder).llReportReview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(Global.currentUser.user_id == 0){
                        goToActivitySignIn(v.getContext());
                    }else{
                        DialogReportReview dialogReportReview = new DialogReportReview(v.getContext(), uniReview.uniReview_id);
                        dialogReportReview.show();
                    }
                }
            });

            // when click on deleteReview
            ((VHItem) viewHolder).llDeleteReview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    askUserForDeleteReview(v.getContext(), uniReview.uniReview_id);
                }
            });

            // when click on editReview
            ((VHItem) viewHolder).llEditReview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogNewAndEditReview dialogNewAndEditReview = new DialogNewAndEditReview(activity, 0, uniReview.uniReview_id, uniReview.reviewRate, uniReview.reviewText);
                    dialogNewAndEditReview.show();
                }
            });
        }else {
            // only happen when we need LazyLoading
            ((VHProgress) viewHolder).progressBarReview.setIndeterminate(true);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        }

        if(uniReviews.get(position - 1) != null){
            return TYPE_ITEM;
        }else{
            return VIEW_PROGRESS;
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    void setLoaded() {
        isLoading = false;
    }

    void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    interface OnLoadMoreListener {
        void onLoadMore();
    }

    void setMoreDataAvailable(boolean moreDataAvailable) {
        isMoreDataAvailable = moreDataAvailable;
    }



    private void setTxtUpVoteText(RecyclerView.ViewHolder viewHolder, int number){
        if(number == 0){
            ((VHItem) viewHolder).txtUpVote.setText(String.valueOf(number));
            return;
        }
        String upVoteText = "+" + String.valueOf(number);
        ((VHItem) viewHolder).txtUpVote.setText(upVoteText);
    }

    private void goToActivitySignIn(Context context){
        Intent intent = new Intent(context, ActivityUserGoogleSignIn.class);
        context.startActivity(intent);
        ActivityUniReviews.isComeBackFromSignIn = true;
    }

    private void askUserForDeleteReview(final Context context, final int uniReviewId){
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(context.getString(R.string.uni_reviews_rv_delete_question_title));
        alertDialog.setMessage(context.getString(R.string.uni_reviews_rv_delete_question_message));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.uni_reviews_rv_delete_question_positive_button),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deleteReview(context, uniReviewId);
                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, context.getString(android.R.string.no),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.setIcon(R.drawable.ic_delete_forever);
        alertDialog.show();

        Button btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button btnNegative = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) btnPositive.getLayoutParams();
        layoutParams.weight = 10;
        btnPositive.setLayoutParams(layoutParams);
        btnNegative.setLayoutParams(layoutParams);
    }

    private void deleteReview(final Context context, int uniReviewId){
        if(!Global.isConnectedToNetwork(context)){
            Toast.makeText(context, context.getString(R.string.no_internet_your_offline_please_check_network), Toast.LENGTH_LONG).show();
            return;
        }

        Uri.Builder builder = new Uri.Builder()
                .appendQueryParameter("uniReviewId", String.valueOf(uniReviewId));

        new WebHttpConnection(Global.HOST_ADDRESS + "/webservice/deleteReview", builder){
            @Override public void onPostExecute(String result)
            {
                if(result != null){
                    Toast.makeText(context, R.string.uni_reviews_rv_delete_deleted_successfully, Toast.LENGTH_LONG).show();
                    activity.recreate();
                }else{
                    Toast.makeText(context, R.string.internet_unavailable_text, Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    private void setVote(final Context context, final RecyclerView.ViewHolder viewHolder, final boolean isUpVote, int userId, int uniReviewId, final int position){
        if(!Global.isConnectedToNetwork(context)){
            Toast.makeText(context, context.getString(R.string.no_internet_your_offline_please_check_network), Toast.LENGTH_LONG).show();
            return;
        }

        String vote;
        if(isUpVote){
            vote = "1";
        }else{
            vote = "0";
        }
        Uri.Builder builder = new Uri.Builder()
                .appendQueryParameter("userId", String.valueOf(userId))
                .appendQueryParameter("uniReviewId", String.valueOf(uniReviewId))
                .appendQueryParameter("isUpVote", vote);

        new WebHttpConnection(Global.HOST_ADDRESS + "/webservice/setVote", builder){
            @Override public void onPostExecute(String result)
            {
                if(result != null){
                    int currentUpVote = Integer.parseInt(((VHItem) viewHolder).txtUpVote.getText().toString());
                    int currentDownVote = Integer.parseInt(((VHItem) viewHolder).txtDownVote.getText().toString());

                    if(result.equals("SetNewVote")){
                        if(isUpVote){
                            int newUpVote = ++currentUpVote;
                            setTxtUpVoteText(viewHolder, newUpVote);
                            setUpVoteColor(context, viewHolder);
                            uniReviews.get(position -1).reviewUpVote = newUpVote;
                            uniReviews.get(position -1).currentUserVoted = 1;
                        }else{
                            int newDownVote = --currentDownVote;
                            ((VHItem) viewHolder).txtDownVote.setText(String.valueOf(newDownVote));
                            setDownVoteColor(context, viewHolder);
                            uniReviews.get(position -1).reviewDownVote = newDownVote;
                            uniReviews.get(position -1).currentUserVoted = 0;
                        }

                    }else if(result.equals("DeleteUpVote")){
                        int newUpVote = --currentUpVote;
                        setTxtUpVoteText(viewHolder, newUpVote);
                        setVoteDefaultColor(context, viewHolder, true);
                        uniReviews.get(position -1).reviewUpVote = newUpVote;
                        uniReviews.get(position -1).currentUserVoted = -1;

                    }else if(result.equals("DeleteUpVoteInsertDownVote")){
                        int newUpVote = --currentUpVote;
                        int newDownVote = --currentDownVote;
                        setTxtUpVoteText(viewHolder, newUpVote);
                        ((VHItem) viewHolder).txtDownVote.setText(String.valueOf(newDownVote));

                        setVoteDefaultColor(context, viewHolder, true);
                        setDownVoteColor(context, viewHolder);

                        uniReviews.get(position -1).reviewUpVote = newUpVote;
                        uniReviews.get(position -1).reviewDownVote = newDownVote;
                        uniReviews.get(position -1).currentUserVoted = 0;

                    }else if(result.equals("DeleteDownVoteInsertUpVote")){
                        int newUpVote = ++currentUpVote;
                        int newDownVote = ++currentDownVote;
                        ((VHItem) viewHolder).txtDownVote.setText(String.valueOf(newDownVote));
                        setTxtUpVoteText(viewHolder, newUpVote);

                        setVoteDefaultColor(context, viewHolder, false);
                        setUpVoteColor(context, viewHolder);

                        uniReviews.get(position -1).reviewUpVote = newUpVote;
                        uniReviews.get(position -1).reviewDownVote = newDownVote;
                        uniReviews.get(position -1).currentUserVoted = 1;

                    }else if(result.equals("DeleteDownVote")){
                        int newDownVote = ++currentDownVote;
                        ((VHItem) viewHolder).txtDownVote.setText(String.valueOf(newDownVote));
                        setVoteDefaultColor(context, viewHolder, false);
                        uniReviews.get(position -1).reviewDownVote = newDownVote;
                        uniReviews.get(position -1).currentUserVoted = -1;
                    }
                }else{
                    Toast.makeText(context, R.string.internet_unavailable_text, Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    private void setUpVoteColor(Context context, RecyclerView.ViewHolder viewHolder){
        int colorUpVote = ContextCompat.getColor(context, R.color.uni_reviews_rv_up_vote_color);
        ((VHItem) viewHolder).txtUpVote.setTextColor(colorUpVote);
        ((VHItem) viewHolder).txtIconThumbsUp.setTextColor(colorUpVote);
    }
    private void setDownVoteColor(Context context, RecyclerView.ViewHolder viewHolder){
        int colorDownVote = ContextCompat.getColor(context, R.color.uni_reviews_rv_down_vote_color);
        ((VHItem) viewHolder).txtDownVote.setTextColor(colorDownVote);
        ((VHItem) viewHolder).txtIconThumbsDown.setTextColor(colorDownVote);
    }

    private void setVoteDefaultColor(Context context, RecyclerView.ViewHolder viewHolder, boolean isUpVote){
        int defaultColor = ContextCompat.getColor(context, R.color.uni_reviews_rv_vote_default_color);
        if(isUpVote){
            ((VHItem) viewHolder).txtUpVote.setTextColor(defaultColor);
            ((VHItem) viewHolder).txtIconThumbsUp.setTextColor(defaultColor);
            return;
        }
        ((VHItem) viewHolder).txtDownVote.setTextColor(defaultColor);
        ((VHItem) viewHolder).txtIconThumbsDown.setTextColor(defaultColor);
    }
}