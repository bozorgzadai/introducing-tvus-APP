package com.bozorgzad.ali.introducingtvus;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.MatrixCursor;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.speech.RecognizerIntent;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.support.v4.widget.CursorAdapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bozorgzad.ali.introducingtvus.common.logger.Log;
import com.bozorgzad.ali.introducingtvus.common.logger.LogFragment;
import com.bozorgzad.ali.introducingtvus.common.logger.LogWrapper;
import com.bozorgzad.ali.introducingtvus.common.logger.MessageOnlyLogFilter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/*
 * Created by Ali_Dev on 7/26/2017.
 */

// I didn't extends from ActivityParent, because the searchView DropDown cannot fit with toolbar search
//                                       (when it set in ViewStub of ActivityParent)
public class ActivitySearchUni extends AppCompatActivity {

    private LinearLayout llHeaderProgressSearchUni;
    private LinearLayout llInternetUnavailableSearchUni;
    private boolean connectedToNetworkSearchUni;
    
    public static final String SUGGEST_COLUMN_TEXT = "suggest_text";
    public static final String SUGGEST_COLUMN_TYPE = "suggest_type";
    public static final String SUGGEST_COLUMN_TYPE_RECENT = "Recent";
    public static final String SUGGEST_COLUMN_TYPE_SUGGEST = "Suggest";
    public static final String RECENT_SEARCH_TAG = "RecentSearches";
    public static final String CURRENT_VIEW_TYPE_ITEM = "CurrentViewTypeItem";
    public static SearchView searchViewSearch;
    public static AutoCompleteTextView searchEditText;

    public static ArrayList<Uni> boysUnis = new ArrayList<>();
    public static ArrayList<Uni> girlsUnis = new ArrayList<>();
    private ArrayList<Uni>  unis = new ArrayList<>();
    private AdapterRvSearchUnis rvAdapterSearchUnis;
    static class Uni {
        int uni_id;
        String uniStateFa;
        String uniStateEn;
        String uniFullNameFa;
        String uniFullNameEn;
        String uniLogo;
    }

    private class unisName{
        String uniFullNameFa;
        String uniFullNameEn;
    }
    private static ArrayList<unisName> suggestionUnisNames = new ArrayList<>();

    private MenuItem menuItem;
    private Spinner spnSearchUniState;
    private ImageView imgSearchEmptyList;
    private LinearLayout llFilterSearchUni;
    private TextView txtChangeLayout;
    private boolean isInternetAvailable = false;
    private boolean isSeeAllMostViewedUnis = false;
    private boolean isFirstTimeEnter = true;
    private int keyboardHeight = 0;
    private int checkedFilterItem = 0;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor spEditor;
    private final int SUGGESTION_VIEW_MARGIN_VERTICAL = 10;

    @Override
    protected  void onStart() {
        super.onStart();
        initializeLogging();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_uni);

        // Set Background Color For Recent Apps
        Global.setRecentTabsTaskDescription(this);

        // For Check The Internet
            llHeaderProgressSearchUni = (LinearLayout) findViewById(R.id.llHeaderProgressSearchUni);
            llInternetUnavailableSearchUni = (LinearLayout) findViewById(R.id.llInternetUnavailableSearchUni);
            Button btnInternetUnavailableRootButton = (Button) findViewById(R.id.btnInternetUnavailableSearchUni);

            llHeaderProgressSearchUni.setVisibility(View.VISIBLE);
            llHeaderProgressSearchUni.setZ(50); // the layout come over the toolbar

            btnInternetUnavailableRootButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    recreate();
                }
            });

            // Check Internet Connection
            LinearLayout llNotConnectedToInternetSearchUni = (LinearLayout) findViewById(R.id.llNotConnectedToInternetSearchUni);
            Global.checkInternetConnection(this, llHeaderProgressSearchUni, llNotConnectedToInternetSearchUni);
            connectedToNetworkSearchUni = Global.isConnectedToNetwork(this);


        // Set Toolbar
        setToolbar();

        // Get SharedPreferences And Editor
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        spEditor = sharedPreferences.edit();

        // Spinner //
        spnSearchUniState = (Spinner) findViewById(R.id.spnSearchUniState);
        spnOnItemSelected();
        fetchAllUnisNameAndUnisState();

        // Separate boys and girls OR Attach them
        llFilterSearchUni = (LinearLayout) findViewById(R.id.llFilterSearchUni);
        llFilterSearchUniOnClick();

        // Come from where, To choose the correct fragment tab
        imgSearchEmptyList = (ImageView) findViewById(R.id.imgSearchEmptyList);
        chooseWhichFragmentTabAtFirst();

        // Set font, size and scale
        txtChangeLayout = (TextView) findViewById(R.id.txtChangeLayout);
        changeSizeAndScaleForDifferentLanguage();

        // Change layout view between Grid,Side,Top
        setLayoutManagerForFirstTimeAndToggleOnChange();
    }

    // Happens only when remove the recent searches
    public static void updateAndShowDropDown(Context context, boolean isRecentEmpty){
        MatrixCursor matrixCursor = filterRecentAndSuggest(context, "");
        searchViewSearch.setSuggestionsAdapter(new AdapterCaRecentAndSuggest(context, matrixCursor, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER));
        if(!isRecentEmpty){
            searchEditText.showDropDown();
        }
    }

    public static MatrixCursor filterRecentAndSuggest(Context context, String query) {
        query = query.toLowerCase();

        // Create a table with cursor
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{ BaseColumns._ID, SUGGEST_COLUMN_TEXT , SUGGEST_COLUMN_TYPE});
        int index = 0;

        // Get the current RecentSearches
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        String recentSearches;
        if(Global.currentUser.user_id == 0){
            recentSearches = sharedPreferences.getString(RECENT_SEARCH_TAG, "");
        }else{
            recentSearches = Global.currentUser.userRecentSearches;
        }
        

        if (!recentSearches.isEmpty()){
            ArrayList<String> recentSearchesArray = new ArrayList<>(Arrays.asList(recentSearches.split("\\|")));

            for (int j=recentSearchesArray.size()-1; j>=0; j--) {
                if (recentSearchesArray.get(j).contains(query)) {
                    matrixCursor.addRow(new Object[]{index, recentSearchesArray.get(j), SUGGEST_COLUMN_TYPE_RECENT});
                    index++;
                }
            }
        }

        if(!query.equals("")){
            int rowCount = 0;

            for (unisName suggestionUniName : suggestionUnisNames) {
                if (suggestionUniName.uniFullNameEn.toLowerCase().contains(query)) {
                    matrixCursor.addRow(new Object[]{index, suggestionUniName.uniFullNameEn.toLowerCase(), SUGGEST_COLUMN_TYPE_SUGGEST});
                }else if(suggestionUniName.uniFullNameFa.toLowerCase().contains(query)){
                    matrixCursor.addRow(new Object[]{index, suggestionUniName.uniFullNameFa.toLowerCase(), SUGGEST_COLUMN_TYPE_SUGGEST});
                }else{
                    continue;
                }
                index++;
                rowCount++;
                if (rowCount > 4) {
                    break;
                }
            }
        }
        return matrixCursor;
    }

    private void setToolbar(){
        Toolbar toolbarSearchUni = (Toolbar) findViewById(R.id.toolbarSearchUni);
        toolbarSearchUni.setTitle(R.string.navigation_ic_most_viewed_unis);
        setSupportActionBar(toolbarSearchUni);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void spnOnItemSelected(){
        spnSearchUniState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                FragmentSearchUniSlidingTabsColors.llHeaderProgressFragmentContent.setVisibility(View.VISIBLE);
                if(!isFirstTimeEnter){
                    fetchSearchUnisOrMostViewedUnis();
                }
                isFirstTimeEnter = false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void llFilterSearchUniOnClick(){
        final TextView txtCurrentFilter = (TextView) findViewById(R.id.txtCurrentFilter);
        final FrameLayout flFragmentContentSearchUni = (FrameLayout) findViewById(R.id.flFragmentContentSearchUni);
        final CharSequence[] filterListNames = {getString(R.string.search_filter_list_set_separation), getString(R.string.search_filter_list_cancel_separation)};

        llFilterSearchUni.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alt_bld = new AlertDialog.Builder(v.getContext());
                alt_bld.setTitle(R.string.search_filter_title);

                alt_bld.setSingleChoiceItems(filterListNames, checkedFilterItem, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        checkedFilterItem = item;
                        dialog.dismiss();
                        if(item == 0){
                            // Happen when want to separate boys and girls
                            txtCurrentFilter.setText(R.string.search_filter_list_set_separation_small);
                            flFragmentContentSearchUni.setVisibility(View.VISIBLE);
                        }else{
                            txtCurrentFilter.setText(R.string.search_filter_list_cancel_separation);
                            flFragmentContentSearchUni.setVisibility(View.GONE);
                            searchViewSearch.clearFocus();
                        }
                    }
                });
                AlertDialog alert = alt_bld.create();
                alert.show();
            }
        });
    }

    private void chooseWhichFragmentTabAtFirst(){
        String whichSearchUniTab = getString(R.string.find_uni_tab_boys);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            whichSearchUniTab = extras.getString("WhichSearchUniTab");
            isSeeAllMostViewedUnis = extras.getBoolean("SeeAllMostViewedUnis");

            // if Come from MostViewedUnis, then fetch them
            if(isSeeAllMostViewedUnis){
                fetchSearchUnisOrMostViewedUnis();
            }
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        FragmentSearchUniSlidingTabsColors fragment = FragmentSearchUniSlidingTabsColors.newInstance(whichSearchUniTab);
        transaction.replace(R.id.flFragmentContentSearchUni, fragment);
        transaction.commit();
    }

    private void changeSizeAndScaleForDifferentLanguage(){
        TextView txtFilterImage = (TextView) findViewById(R.id.txtFilterImage);

        Typeface font = Typeface.createFromAsset( getAssets(), "fonts/fontawesome-webfont.ttf");
        txtChangeLayout.setTypeface(font);
        txtFilterImage.setTypeface(font);

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) llFilterSearchUni.getLayoutParams();
        if(Global.LANGUAGE.equals("fa")){
            txtChangeLayout.setScaleX(-1);
            params.weight = 0.6f;
        }else{
            params.weight = 0.5f;
            txtFilterImage.setScaleX(-1);
        }

        // More width for filter when on farsi language
        llFilterSearchUni.setLayoutParams(params);
    }

    private void setLayoutManagerForFirstTimeAndToggleOnChange(){
        final RecyclerView rvSearchUniByName = (RecyclerView) findViewById(R.id.rvSearchUniByName);
        LinearLayout llChangeLayout = (LinearLayout) findViewById(R.id.llChangeLayout);

        llChangeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // there is no difference to toggle currentViewTypeItem with any ADAPTER because it's Static
                int currentViewTypeItem = rvAdapterSearchUnis.toggleItemViewType();
                spEditor.putInt(CURRENT_VIEW_TYPE_ITEM, currentViewTypeItem);
                spEditor.apply();

                setLayoutManagerByCurrentViewTypeItem(rvSearchUniByName, currentViewTypeItem, true);
                rvAdapterSearchUnis.notifyDataSetChanged();
                FragmentContentSearchUni.rvAdapterGirlsUnis.notifyDataSetChanged();
                FragmentContentSearchUni.rvAdapterBoysUnis.notifyDataSetChanged();
            }
        });

        // Get the current CurrentViewTypeItem And create a adapter
        int currentViewTypeItem = sharedPreferences.getInt(CURRENT_VIEW_TYPE_ITEM, 0);
        AdapterRvSearchUnis.currentViewTypeItem = currentViewTypeItem;
        setLayoutManagerByCurrentViewTypeItem(rvSearchUniByName, currentViewTypeItem, false);
        rvAdapterSearchUnis = new AdapterRvSearchUnis(unis);
        rvSearchUniByName.setAdapter(rvAdapterSearchUnis);
    }

    private void setLayoutManagerByCurrentViewTypeItem(RecyclerView rvSearchUniByName, int currentViewTypeItem , boolean changeLayoutButNotFirstTime){
        // change image base on our current layout
        if(currentViewTypeItem == AdapterRvSearchUnis.LIST_IMAGE_SIDE_ITEM){
            txtChangeLayout.setText(R.string.icon_bars_image_top);
        }else if(currentViewTypeItem == AdapterRvSearchUnis.LIST_IMAGE_TOP_ITEM){
            txtChangeLayout.setText(R.string.icon_th_large_image_grid);
        }else{
            txtChangeLayout.setText(R.string.icon_list_ul_image_side);

            // Set Grid View
            rvSearchUniByName.setLayoutManager(new GridLayoutManager(ActivitySearchUni.this, 2));
            if(changeLayoutButNotFirstTime){
                FragmentContentSearchUni.rvPagerFindBoysUni.setLayoutManager(new GridLayoutManager(ActivitySearchUni.this, 2));
                FragmentContentSearchUni.rvPagerFindGirlsUni.setLayoutManager(new GridLayoutManager(ActivitySearchUni.this, 2));
            }
            return;
        }

        // Set Linear View
        rvSearchUniByName.setLayoutManager(new LinearLayoutManager(ActivitySearchUni.this, LinearLayoutManager.VERTICAL, false));
        if(changeLayoutButNotFirstTime){
            FragmentContentSearchUni.rvPagerFindBoysUni.setLayoutManager(new LinearLayoutManager(ActivitySearchUni.this, LinearLayoutManager.VERTICAL, false));
            FragmentContentSearchUni.rvPagerFindGirlsUni.setLayoutManager(new LinearLayoutManager(ActivitySearchUni.this, LinearLayoutManager.VERTICAL, false));
        }
    }


    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        // Voice Recognition
        // Display an hint to the user about what he should say.
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getResources().getString(R.string.speech_recognizer_prompt));

        // Given an hint to the recognizer about what the user is going to say
        // 1.LANGUAGE_MODEL_WEB_SEARCH : For short phrases
        // 2.LANGUAGE_MODEL_FREE_FORM  : If not sure about the words or phrases
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);


        String currentVoiceSearchLanguage = sharedPreferences.getString(ActivitySettings.CURRENT_VOICE_SEARCH_LANGUAGE, "");
        String voiceSearchBy;
        if(currentVoiceSearchLanguage.equals(ActivitySettings.CURRENT_SOFTWARE_LANGUAGE)){
            voiceSearchBy = Global.LANGUAGE;
        }else if(currentVoiceSearchLanguage.equals(getString(R.string.app_language_name_en))){
            voiceSearchBy = "en";
        }else{
            voiceSearchBy = "fa";
        }

        // Speech Recognizer Language
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, voiceSearchBy);

        super.startActivityForResult(intent, requestCode);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu_search; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);

        menuItem = menu.findItem(R.id.action_search_search);
        searchViewSearch = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchViewSearch.setQueryHint(getResources().getString(R.string.speech_recognizer_hint));

        // Searchable Configuration set to SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchViewSearch.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchEditText = (AutoCompleteTextView) searchViewSearch.findViewById(R.id.search_src_text);

        // SetOnQueryTextListener
        searchViewSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
            public boolean onQueryTextChange(String newText) {
                // Change recent and suggest when searchEditText changed
                MatrixCursor matrixCursor = filterRecentAndSuggest(ActivitySearchUni.this, newText);
                searchViewSearch.setSuggestionsAdapter(new AdapterCaRecentAndSuggest(ActivitySearchUni.this,matrixCursor,CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER));

                return false;
            }
        });

        // Show recent for the first time enter
        // if this method run first, we don't know we have internet or not, so we not expand the menuItem, then
        // we expand the menuItem when we detect the net on fetchFunction
        // Only one time this IF expand the menuItem, when the speed of internet is very high and the fetchFunction runs before this
        // But if the speed of internet is low, we expand the menu in fetchFunction
        if(!isSeeAllMostViewedUnis && connectedToNetworkSearchUni && isInternetAvailable){
            menuItem.expandActionView();
        }

        // When searchView is empty and press search button
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if(searchEditText.getText().toString().equals("")){
                        fetchSearchUnisOrMostViewedUnis();
                        searchViewSearch.clearFocus();
                    }
                }
                return false;
            }
        });

        // Open Keyboard when setting focus on EditText of SearchView
        searchViewSearch.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.showSoftInput(view.findFocus(), 0);
                    }
                }
            }
        });

        // Happen when Collapse or Expand the SearchView
        MenuItemCompat.setOnActionExpandListener(menuItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                if(searchViewSearch.getQuery().length() == 0){
                    finish();
                }else{
                    searchEditText.setText("");
                    item.expandActionView();
                    searchViewSearch.requestFocus();
                }
                return false;  // Return true to collapse action view
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                isSeeAllMostViewedUnis = false;
                return true;
            }
        });


        final View dropDownAnchor = searchViewSearch.findViewById(searchEditText.getDropDownAnchor());
        if (dropDownAnchor != null) {
            // Get screen width and height
            final int screenWidthPixel = this.getResources().getDisplayMetrics().widthPixels;
            final int screenHeightPixel = this.getResources().getDisplayMetrics().heightPixels;

            dropDownAnchor.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                           int oldLeft, int oldTop, int oldRight, int oldBottom) {

                    // Get the height of keyboard
                    final CoordinatorLayout clSearchUni = (CoordinatorLayout) findViewById(R.id.clSearchUni);
                    clSearchUni.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            Rect rect = new Rect();
                            ViewGroup parent = (ViewGroup) clSearchUni.getParent();
                            parent.getWindowVisibleDisplayFrame(rect);

                            int screenHeight = parent.getRootView().getHeight();
                            keyboardHeight = screenHeight - (rect.bottom - rect.top);
                        }
                    });

                    // Set dropDownView width fullscreen, margin from toolbar, set a background transparent color
                    searchEditText.setDropDownWidth(screenWidthPixel);
                    searchEditText.setDropDownVerticalOffset(SUGGESTION_VIEW_MARGIN_VERTICAL);
                    searchEditText.setDropDownBackgroundResource(R.color.search_view_suggest_bg_color);

                    // Set height fullscreen except keyboard for transparent bgColor
                    searchEditText.setDropDownHeight(screenHeightPixel - keyboardHeight - (SUGGESTION_VIEW_MARGIN_VERTICAL *2));
                }
            });
        }

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // On ActionBar Back Press
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }
    // When search button press OR Voice search happen
    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            searchEditText.setText(query);
            searchViewSearch.clearFocus();

            //use the query to search
            query = query.toLowerCase();
            fetchSearchUnisOrMostViewedUnis();

            String recentSearches;
            if(Global.currentUser.user_id == 0){
                recentSearches = sharedPreferences.getString(RECENT_SEARCH_TAG, "");

                String newRecentSearches = addQueryToRecentSearches(recentSearches, query);
                spEditor.putString(RECENT_SEARCH_TAG, newRecentSearches);
                spEditor.apply();
            }else{
                recentSearches = Global.currentUser.userRecentSearches;
                Global.currentUser.userRecentSearches = addQueryToRecentSearches(recentSearches, query);

                if(connectedToNetworkSearchUni){
                    setRecentSearchesByUserId();
                }
            }
        }
    }

    private String addQueryToRecentSearches(String recentSearches, String query){
        String newRecentSearches;

        if(recentSearches.isEmpty()){
            // If the list is empty
            newRecentSearches = query;
        }else{
            ArrayList<String> recentSearchesArray = new ArrayList<>(Arrays.asList(recentSearches.split("\\|")));

            // if the query is duplicate, remove the previous one
            boolean hasDuplicate = false;
            for(String recentSearch : recentSearchesArray){
                if(recentSearch.equals(query)){
                    recentSearchesArray.remove(query);
                    recentSearches = TextUtils.join("|", recentSearchesArray);
                    hasDuplicate = true;
                    break;
                }
            }

            // If the length more than 5, remove the first one
            if(recentSearchesArray.size() == 5 && !hasDuplicate){
                recentSearchesArray.remove(0);
                recentSearches = TextUtils.join("|", recentSearchesArray);
            }

            if(recentSearches.isEmpty()){
                // if recentSearches has one item, and we search that item again(so it's duplicate)
                newRecentSearches = query;
            }else{
                newRecentSearches = recentSearches + "|" + query;
            }
        }

        return newRecentSearches;
    }

    public static void setRecentSearchesByUserId(){
        Uri.Builder builder = new Uri.Builder()
                .appendQueryParameter("userId", String.valueOf(Global.currentUser.user_id))
                .appendQueryParameter("recentSearches", Global.currentUser.userRecentSearches);
        new WebHttpConnection(Global.HOST_ADDRESS + "/webservice/setRecentSearchesByUserId", builder).execute();
    }

    private void fetchAllUnisNameAndUnisState(){
        if(!connectedToNetworkSearchUni){
            return;
        }

        new WebHttpConnection(Global.HOST_ADDRESS + "/webservice/fetchAllUnisNameAndUnisState",null)
        {
            @Override public void onPostExecute(String result)
            {
                if(result != null){
                    try {
                        JSONObject rootObject= new JSONObject(result);
                        JSONArray arrayUnisName = rootObject.getJSONArray("unisName");
                        JSONArray arrayUnisState = rootObject.getJSONArray("unisState");

                        // UnisName
                        suggestionUnisNames.clear();
                        for (int i = 0 ; i < arrayUnisName.length() ; i++) {
                            unisName uniName = new unisName();
                            JSONObject object = arrayUnisName.getJSONObject(i);

                            uniName.uniFullNameFa = object.getString("uniFullNameFa");
                            uniName.uniFullNameEn = object.getString("uniFullNameEn");

                            suggestionUnisNames.add(uniName);
                        }

                        // UnisState
                        ArrayList<String> tempUniStateFa = new ArrayList<>();
                        ArrayList<String> tempUniStateEn = new ArrayList<>();
                        for (int i = 0; i < arrayUnisState.length() ; i++) {
                            JSONObject object = arrayUnisState.getJSONObject(i);

                            tempUniStateFa.add(object.getString("uniStateFa"));
                            tempUniStateEn.add(object.getString("uniStateEn"));
                        }

                        String[] uniStateFa = new String[tempUniStateFa.size() +1];
                        String[] uniStateEn = new String[tempUniStateEn.size() +1];

                        uniStateFa[0] = getString(R.string.search_choose_state);
                        uniStateEn[0] = getString(R.string.search_choose_state);

                        int i = 1;
                        for(String temp : tempUniStateFa){
                            uniStateFa[i] = temp;
                            i++;
                        }

                        i = 1;
                        Collections.sort(tempUniStateEn);
                        for(String temp : tempUniStateEn){
                            uniStateEn[i] = temp;
                            i++;
                        }

                        if(Global.LANGUAGE.equals("fa")){
                            ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), R.layout.spinner_find_uni, uniStateFa);
                            spnSearchUniState.setAdapter(adapter);
                        }else{
                            ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), R.layout.spinner_find_uni, uniStateEn);
                            spnSearchUniState.setAdapter(adapter);
                        }

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // Do something after 200ms
                                llHeaderProgressSearchUni.setVisibility(View.GONE);
                            }
                        }, 200);

                        // if onCreateOptionsMenu run first the menuItem is not null so we expand the menu
                        // if this method run first, so the menuItem is null and we cannot expand it, here we set a isInternetAvailable=true
                        // it allow us to expand the menu in onCreateOptionsMenu
                        isInternetAvailable = true;
                        if (menuItem != null && !isSeeAllMostViewedUnis) {
                            menuItem.expandActionView();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else{
                    llInternetUnavailableSearchUni.setVisibility(View.VISIBLE);
                }
            }
        }.execute();
    }

    private void fetchSearchUnisOrMostViewedUnis(){
        connectedToNetworkSearchUni = Global.isConnectedToNetwork(this);
        if(!connectedToNetworkSearchUni){
            Toast.makeText(this, getString(R.string.no_internet_your_offline_please_check_network), Toast.LENGTH_LONG).show();
            return;
        }

        String webServiceController;
        Uri.Builder builder;
        String spnCurrentText = "";

        if(spnSearchUniState.getSelectedItem() != null){
            spnCurrentText = spnSearchUniState.getSelectedItem().toString();
            if(spnCurrentText.equals(getString(R.string.search_choose_state))) {
                spnCurrentText = "";
            }
        }

        if(isSeeAllMostViewedUnis){
            webServiceController = "fetchMostViewedUnis";
            builder = new Uri.Builder().appendQueryParameter("uniState", spnCurrentText);
        }else{
            webServiceController = "fetchSearchUnisByNameAndState";

            String currentSearchPhrase = searchEditText.getText().toString().toLowerCase();
            if(spnCurrentText.isEmpty()){
                if(currentSearchPhrase.isEmpty()){
                    imgSearchEmptyList.setVisibility(View.VISIBLE);
                    return;
                }
            }

            builder = new Uri.Builder()
                    .appendQueryParameter("uniState", spnCurrentText)
                    .appendQueryParameter("searchPhrase", currentSearchPhrase);
        }
        imgSearchEmptyList.setVisibility(View.GONE);


        new WebHttpConnection(Global.HOST_ADDRESS + "/webservice/" + webServiceController, builder)
        {
            @Override public void onPostExecute(String result)
            {
                if(result != null){
                    try {
                        JSONArray array = new JSONArray(result);
                        boysUnis.clear();
                        girlsUnis.clear();
                        unis.clear();

                        for (int i = 0 ; i < array.length() ; i++) {
                            Uni boysUni = new Uni();
                            Uni girlsUni = new Uni();

                            JSONObject object = array.getJSONObject(i);

                            String fileType = object.getString("uniGender");
                            if(fileType.equals("boys")){
                                boysUni.uni_id = object.getInt("uni_id");
                                boysUni.uniStateFa = object.getString("uniStateFa");
                                boysUni.uniStateEn = object.getString("uniStateEn");
                                boysUni.uniFullNameFa = object.getString("uniFullNameFa");
                                boysUni.uniFullNameEn = object.getString("uniFullNameEn");
                                boysUni.uniLogo = object.getString("uniLogo");
                                boysUnis.add(boysUni);
                                unis.add(boysUni);
                            }else if(fileType.equals("girls")){
                                girlsUni.uni_id = object.getInt("uni_id");
                                girlsUni.uniStateFa = object.getString("uniStateFa");
                                girlsUni.uniStateEn = object.getString("uniStateEn");
                                girlsUni.uniFullNameFa = object.getString("uniFullNameFa");
                                girlsUni.uniFullNameEn = object.getString("uniFullNameEn");
                                girlsUni.uniLogo = object.getString("uniLogo");
                                girlsUnis.add(girlsUni);
                                unis.add(girlsUni);
                            }
                        }

                        if(FragmentContentSearchUni.rvAdapterBoysUnis != null){
                            FragmentContentSearchUni.rvAdapterBoysUnis.notifyDataSetChanged();
                        }

                        if(FragmentContentSearchUni.rvAdapterGirlsUnis != null){
                            FragmentContentSearchUni.rvAdapterGirlsUnis.notifyDataSetChanged();
                        }
                        rvAdapterSearchUnis.notifyDataSetChanged();

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // Do something after 200ms
                                FragmentSearchUniSlidingTabsColors.llHeaderProgressFragmentContent.setVisibility(View.GONE);
                            }
                        }, 200);

                        TextView noResultFound = (TextView) findViewById(R.id.noResultFound);
                        if(unis.isEmpty()){
                            noResultFound.setVisibility(View.VISIBLE);
                            if(FragmentContentSearchUni.noResultFoundBoysUni != null && FragmentContentSearchUni.noResultFoundGirlsUni != null){
                                FragmentContentSearchUni.noResultFoundBoysUni.setVisibility(View.VISIBLE);
                                FragmentContentSearchUni.noResultFoundGirlsUni.setVisibility(View.VISIBLE);
                            }
                        }else{
                            if(boysUnis.isEmpty() && FragmentContentSearchUni.noResultFoundBoysUni != null){
                                FragmentContentSearchUni.noResultFoundBoysUni.setVisibility(View.VISIBLE);
                            }else{
                                FragmentContentSearchUni.noResultFoundBoysUni.setVisibility(View.GONE);
                            }

                            if(girlsUnis.isEmpty() && FragmentContentSearchUni.noResultFoundGirlsUni != null){
                                FragmentContentSearchUni.noResultFoundGirlsUni.setVisibility(View.VISIBLE);
                            }else{
                                FragmentContentSearchUni.noResultFoundGirlsUni.setVisibility(View.GONE);
                            }
                            noResultFound.setVisibility(View.GONE);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else{
                    Toast.makeText(ActivitySearchUni.this, getString(R.string.internet_unavailable_text), Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    /** Create a chain of targets that will receive log data */
    public void initializeLogging() {
        /* Set up targets to receive log data */
        // Using Log, front-end to the logging chain, emulates android.util.log method signatures.
        // Wraps Android's native log framework

        // Wraps Android's native log framework.
        LogWrapper logWrapper = new LogWrapper();
        // Using Log, front-end to the logging chain, emulates android.util.log method signatures.
        Log.setLogNode(logWrapper);

        // Filter strips out everything except the message text.
        MessageOnlyLogFilter msgFilter = new MessageOnlyLogFilter();
        logWrapper.setNext(msgFilter);

        // On screen logging via a fragment with a TextView.
        LogFragment logFragment = (LogFragment) getSupportFragmentManager().findFragmentById(R.id.log_fragment);
        msgFilter.setNext(logFragment.getLogView());
    }
}