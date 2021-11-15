package com.bozorgzad.ali.introducingtvus;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

/*
 * Created by Ali_Dev on 7/30/2017.
 */

class AdapterCaRecentAndSuggest extends CursorAdapter {

    private Context context;
    private int searchIconPadding;
    private int suggestIconColor;

    private TextView txtSuggest;
    private ImageView imgRecentAndSuggest;
    private LinearLayout llSearchCommit;
    private LinearLayout llRecentAndSuggest;

    AdapterCaRecentAndSuggest(Context context, Cursor c, int flags) {
        super(context, c, flags);
        this.context = context;

        int searchIconPaddingDp = 3;
        float density = context.getResources().getDisplayMetrics().density;
        searchIconPadding = (int)(searchIconPaddingDp * density);
        suggestIconColor = ContextCompat.getColor(context, R.color.search_view_suggest_icon_color);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.recent_and_suggestion, parent, false);

        txtSuggest = (TextView) view.findViewById(R.id.txtSuggest);
        imgRecentAndSuggest = (ImageView) view.findViewById(R.id.imgRecentAndSuggest);
        llSearchCommit = (LinearLayout) view.findViewById(R.id.llSearchCommit);
        llRecentAndSuggest = (LinearLayout) view.findViewById(R.id.llRecentAndSuggest);

        return view;
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        final String suggestColumnText = cursor.getString(cursor.getColumnIndex(ActivitySearchUni.SUGGEST_COLUMN_TEXT));
        final String type = cursor.getString(cursor.getColumnIndex(ActivitySearchUni.SUGGEST_COLUMN_TYPE));

        txtSuggest.setText(suggestColumnText);

        if(type.equals(ActivitySearchUni.SUGGEST_COLUMN_TYPE_RECENT)){
            imgRecentAndSuggest.setImageResource(R.drawable.ic_search_history);
            imgRecentAndSuggest.setPadding(0, 0, 0, 0);
        }else{
            imgRecentAndSuggest.setImageResource(R.drawable.ic_search);
            imgRecentAndSuggest.setColorFilter(suggestIconColor);
            imgRecentAndSuggest.setPadding(searchIconPadding, searchIconPadding, searchIconPadding, searchIconPadding);
        }

        llSearchCommit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivitySearchUni.searchViewSearch.setQuery(suggestColumnText, false);
            }
        });

        llRecentAndSuggest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivitySearchUni.searchViewSearch.setQuery(suggestColumnText, true);
            }
        });


        llRecentAndSuggest.setOnTouchListener( new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int colorFrom = ContextCompat.getColor(context, R.color.search_view_suggest_item_bg_color);
                int colorTo = ContextCompat.getColor(context, R.color.search_view_suggest_item_bg_color_hold);
                int duration = 500;

                switch(event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        ObjectAnimator.ofObject(view, "backgroundColor", new ArgbEvaluator(), colorFrom, colorTo)
                                .setDuration(duration)
                                .start();
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        ObjectAnimator.ofObject(view, "backgroundColor", new ArgbEvaluator(), colorTo, colorFrom)
                                .setDuration(duration)
                                .start();
                        break;
                }
                return false;
            }
        });

        llRecentAndSuggest.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                if(!type.equals(ActivitySearchUni.SUGGEST_COLUMN_TYPE_RECENT)){
                    return false;
                }
                askUserForRemoveItem(v.getContext(), suggestColumnText);
                return true;
            }
        });
    }

    private void askUserForRemoveItem(final Context context, final String suggestColumnText){
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(suggestColumnText);
        alertDialog.setMessage(context.getString(R.string.search_history_remove_item_message));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.search_history_remove_item_positive_button_text),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        boolean isRecentEmpty = removeRecentItem(suggestColumnText);
                        ActivitySearchUni.searchEditText.setText("");
                        ActivitySearchUni.updateAndShowDropDown(context , isRecentEmpty);
                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, context.getString(android.R.string.no),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivitySearchUni.searchEditText.showDropDown();
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

    private boolean removeRecentItem(String recentItemText){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String recentSearches;
        String newRecentSearches;
        if(Global.currentUser.user_id == 0){
            recentSearches = sharedPreferences.getString(ActivitySearchUni.RECENT_SEARCH_TAG, "");

            newRecentSearches = findAndRemove(recentSearches, recentItemText);
            editor.putString(ActivitySearchUni.RECENT_SEARCH_TAG, newRecentSearches);
            editor.apply();
        }else{
            recentSearches = Global.currentUser.userRecentSearches;

            newRecentSearches = findAndRemove(recentSearches, recentItemText);
            Global.currentUser.userRecentSearches = newRecentSearches;

            if(Global.isConnectedToNetwork(context)){
                ActivitySearchUni.setRecentSearchesByUserId();
            }
        }

        return newRecentSearches.isEmpty();
    }

    private String findAndRemove(String recentSearches, String recentItemText){
        ArrayList<String> recentSearchesArray = new ArrayList<>(Arrays.asList(recentSearches.split("\\|")));

        for(String recentSearch : recentSearchesArray){
            if(recentSearch.equals(recentItemText)){
                recentSearchesArray.remove(recentItemText);
                recentSearches = TextUtils.join("|", recentSearchesArray);
                break;
            }
        }

        return recentSearches;
    }
}