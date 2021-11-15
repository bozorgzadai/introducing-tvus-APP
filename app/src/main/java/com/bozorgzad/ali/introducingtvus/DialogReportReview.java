package com.bozorgzad.ali.introducingtvus;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

/*
 * Created by Ali_Dev on 9/3/2017.
 */

class DialogReportReview extends Dialog {

    private Context context;

    private int uniReviewId;

    DialogReportReview(Context context, int uniReviewId) {
        super(context);
        this.context = context;
        this.uniReviewId = uniReviewId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_report_review);

        TextView txtCancelReportReview = (TextView) findViewById(R.id.txtCancelReportReview);
        TextView txtSubmitReportReview = (TextView) findViewById(R.id.txtSubmitReportReview);
        txtCancelReportReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        txtSubmitReportReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptReportReview();
            }
        });
    }

    private void attemptReportReview(){
        RadioGroup rgReportReview = (RadioGroup) findViewById(R.id.rgReportReview);

        int checkedRadioButtonId = rgReportReview.getCheckedRadioButtonId();
        if (checkedRadioButtonId == -1) {
            Toast.makeText(context, R.string.uni_reviews_report_dialog_choose_one_item, Toast.LENGTH_LONG).show();
            return;
        }
        // we store the english name of action in RadioButtons Tag
        RadioButton radioButton = (RadioButton) rgReportReview.findViewById(checkedRadioButtonId);
        String selectedText = radioButton.getTag().toString();
        reportReview(selectedText);
    }

    private void reportReview(String reviewReport){
        if(!Global.isConnectedToNetwork(context)){
            Toast.makeText(context, R.string.no_internet_your_offline_please_check_network, Toast.LENGTH_LONG).show();
            return;
        }

        Global.showProgressDialog(context.getString(R.string.progress_dialog_wait), context);
        Uri.Builder builder = new Uri.Builder()
                .appendQueryParameter("uniReviewId", String.valueOf(uniReviewId))
                .appendQueryParameter("reviewReport", reviewReport);

        new WebHttpConnection(Global.HOST_ADDRESS + "/webservice/reportReview", builder){
            @Override public void onPostExecute(String result)
            {
                Global.hideProgressDialog();
                if(result != null){
                    Toast.makeText(context, R.string.uni_reviews_report_dialog_submitted_successfully, Toast.LENGTH_LONG).show();
                    dismiss();
                }else{
                    Toast.makeText(context, R.string.internet_unavailable_text, Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }
}

