package com.bozorgzad.ali.introducingtvus;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/*
 * Created by Ali_Dev on 7/11/2017.
 */

public class FragmentContentSearchUni extends Fragment {

    public static AdapterRvSearchUnis rvAdapterBoysUnis;
    public static AdapterRvSearchUnis rvAdapterGirlsUnis;
    public static RecyclerView rvPagerFindBoysUni;
    public static RecyclerView rvPagerFindGirlsUni;
    public static TextView noResultFoundBoysUni;
    public static TextView noResultFoundGirlsUni;

    private static final String KEY_TITLE = "title";

    /**
     * @return a new instance of {@link FragmentContentSearchUni}, adding the parameters into a bundle and
     * setting them as arguments.
     */

    public static FragmentContentSearchUni newInstance(CharSequence title) {
        Bundle bundle = new Bundle();
        bundle.putCharSequence(KEY_TITLE, title);

        FragmentContentSearchUni fragment = new FragmentContentSearchUni();
        fragment.setArguments(bundle);

        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pager_find_uni, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args == null) {
            return;
        }
        CharSequence tabTitle = args.getCharSequence(KEY_TITLE);
        if (tabTitle == null) {
            return;
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(view.getContext());
        int currentViewTypeItem = sharedPreferences.getInt(ActivitySearchUni.CURRENT_VIEW_TYPE_ITEM, 0);

        // we have to get "R.id.rvPagerSearchUni" for Boys & Girls because we want to change their LayoutManager in ActivitySearchUni
        if(tabTitle.equals(getString(R.string.find_uni_tab_boys))){
            rvPagerFindBoysUni = (RecyclerView) view.findViewById(R.id.rvPagerSearchUni);
            noResultFoundBoysUni = (TextView) view.findViewById(R.id.noResultFoundBoysAndGirls);
            setLayoutManagerByCurrentViewTypeItem(rvPagerFindBoysUni, currentViewTypeItem, view);

            rvAdapterBoysUnis = new AdapterRvSearchUnis(ActivitySearchUni.boysUnis);
            rvPagerFindBoysUni.setAdapter(rvAdapterBoysUnis);
        }else if(tabTitle.equals(getString(R.string.find_uni_tab_girls))){
            rvPagerFindGirlsUni = (RecyclerView) view.findViewById(R.id.rvPagerSearchUni);
            noResultFoundGirlsUni = (TextView) view.findViewById(R.id.noResultFoundBoysAndGirls);
            setLayoutManagerByCurrentViewTypeItem(rvPagerFindGirlsUni, currentViewTypeItem, view);

            rvAdapterGirlsUnis = new AdapterRvSearchUnis(ActivitySearchUni.girlsUnis);
            rvPagerFindGirlsUni.setAdapter(rvAdapterGirlsUnis);
        }
    }

    private void setLayoutManagerByCurrentViewTypeItem(RecyclerView rvPagerSearchUni, int currentViewTypeItem, View view){
        if(currentViewTypeItem == AdapterRvSearchUnis.GRID_ITEM){
            rvPagerSearchUni.setLayoutManager(new GridLayoutManager(view.getContext(), 2));
        }else{
            rvPagerSearchUni.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.VERTICAL, false));
        }
    }
}
