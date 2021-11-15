package com.bozorgzad.ali.introducingtvus;

import android.app.Activity;
import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

/*
 * Created by Ali_Dev on 9/2/2017.
 */

class DialogNewAndEditReview extends Dialog {

    private Activity activity;

    private TextView txtSubmitNewAndEditReview;
    private EditText edtNewAndEditReviewText;
    private RatingBar rbNewAndEditReview;
    private int uniId;

    private int uniReviewId;
    private double reviewRate;
    private String reviewText;

    DialogNewAndEditReview(Activity activity, int uniId, int uniReviewId, double reviewRate, String reviewText) {
        super(activity);
        this.activity = activity;
        this.uniId = uniId;
        this.uniReviewId = uniReviewId;
        this.reviewRate = reviewRate;
        this.reviewText = reviewText;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_new_review);

        txtSubmitNewAndEditReview = (TextView) findViewById(R.id.txtSubmitNewAndEditReview);
        setViewDateAndOnClickForButtons();

        // when we want to edit a review
        rbNewAndEditReview = (RatingBar) findViewById(R.id.rbNewAndEditReview);
        edtNewAndEditReviewText = (EditText) findViewById(R.id.edtNewAndEditReviewText);
        whenWantToEditReview();
    }

    private void setViewDateAndOnClickForButtons(){
        TextView txtUniShortNameNewAndEditReview = (TextView) findViewById(R.id.txtUniShortNameNewAndEditReview);
        TextView txtUserNameNewAndEditReview = (TextView) findViewById(R.id.txtUserNameNewAndEditReview);
        txtUniShortNameNewAndEditReview.setText(ActivityUniReviews.uniReviewsShortName);
        txtUserNameNewAndEditReview.setText(Global.currentUser.userName);

        TextView txtCancelNewAndEditReview = (TextView) findViewById(R.id.txtCancelNewAndEditReview);
        txtCancelNewAndEditReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        txtSubmitNewAndEditReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSubmitNewReview();
            }
        });
    }

    private void whenWantToEditReview(){
        TextView txtEditNoticeReview = (TextView) findViewById(R.id.txtEditNoticeReview);
        TextView txtTitleNewAndEditReview = (TextView) findViewById(R.id.txtTitleNewAndEditReview);
        if(uniReviewId != 0){
            rbNewAndEditReview.setRating((float) reviewRate);
            edtNewAndEditReviewText.setText(reviewText);
            txtEditNoticeReview.setVisibility(View.VISIBLE);
            txtTitleNewAndEditReview.setText(R.string.uni_reviews_edit_review_rate_and_review_title);
            txtSubmitNewAndEditReview.setText(R.string.uni_reviews_edit_review_edit_button);
        }
    }

    private void attemptSubmitNewReview(){
        // Store values at the time of the Submit attempt.
        String newAndEditReviewText = edtNewAndEditReviewText.getText().toString();

        // Reset errors
        edtNewAndEditReviewText.setError(null);

        boolean cancel = false;
        View focusView = null;

        // Check for a valid review.
        if(rbNewAndEditReview.getRating() == 0){
            Toast.makeText(activity, R.string.uni_reviews_new_review_set_your_rating, Toast.LENGTH_LONG).show();
            focusView = rbNewAndEditReview;
            cancel = true;
        }else if (newAndEditReviewText.isEmpty() || newAndEditReviewText.length() < 5) {
            edtNewAndEditReviewText.setError(activity.getString(R.string.uni_reviews_new_review_at_least_five_character));
            focusView = edtNewAndEditReviewText;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt submit and focus the first form field with an error.
            focusView.requestFocus();
        } else{
            // Show a progress spinner, and perform the submitNewReview
            if(uniReviewId != 0){
                if(edtNewAndEditReviewText.getText().toString().equals(reviewText) && rbNewAndEditReview.getRating() == reviewRate){
                    // if nothing changed
                    dismiss();
                }else{
                    submitEditReview(newAndEditReviewText);
                }
            }else{
                submitNewReview(newAndEditReviewText);
            }
        }
    }

    private void submitNewReview(String newReviewText){
        if(!Global.isConnectedToNetwork(activity)){
            Toast.makeText(activity, activity.getString(R.string.no_internet_your_offline_please_check_network), Toast.LENGTH_LONG).show();
            return;
        }

        Global.showProgressDialog(activity.getString(R.string.progress_dialog_wait), activity);
        Uri.Builder builder = new Uri.Builder()
                .appendQueryParameter("uniId", String.valueOf(uniId))
                .appendQueryParameter("userId", String.valueOf(Global.currentUser.user_id))
                .appendQueryParameter("reviewRate", String.valueOf(rbNewAndEditReview.getRating()))
                .appendQueryParameter("reviewText", newReviewText);

        new WebHttpConnection(Global.HOST_ADDRESS + "/webservice/submitNewReview", builder){
            @Override public void onPostExecute(String result)
            {
                Global.hideProgressDialog();
                if(result != null){
                    Toast.makeText(activity, R.string.uni_reviews_new_review_your_review_submitted_successfully, Toast.LENGTH_LONG).show();
                    dismiss();
                    activity.recreate();
                }else{
                    Toast.makeText(activity, R.string.internet_unavailable_text, Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    private void submitEditReview(String editReviewText){
        if(!Global.isConnectedToNetwork(activity)){
            Toast.makeText(activity, activity.getString(R.string.no_internet_your_offline_please_check_network), Toast.LENGTH_LONG).show();
            return;
        }

        Global.showProgressDialog(activity.getString(R.string.progress_dialog_wait), activity);
        Uri.Builder builder = new Uri.Builder()
                .appendQueryParameter("uniReviewId", String.valueOf(uniReviewId))
                .appendQueryParameter("reviewRate", String.valueOf(rbNewAndEditReview.getRating()))
                .appendQueryParameter("reviewText", editReviewText);

        new WebHttpConnection(Global.HOST_ADDRESS + "/webservice/submitEditReview", builder){
            @Override public void onPostExecute(String result)
            {
                Global.hideProgressDialog();
                if(result != null){
                    Toast.makeText(activity, R.string.uni_reviews_edit_review_your_review_edited_successfully, Toast.LENGTH_LONG).show();
                    dismiss();
                    activity.recreate();
                }else{
                    Toast.makeText(activity, R.string.internet_unavailable_text, Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }
}

