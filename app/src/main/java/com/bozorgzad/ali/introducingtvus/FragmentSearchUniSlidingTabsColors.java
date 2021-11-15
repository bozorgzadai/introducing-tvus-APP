/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bozorgzad.ali.introducingtvus;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter  ;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import com.bozorgzad.ali.introducingtvus.common.view.SlidingTabLayout;

/**
 * A basic sample which shows how to use {@link SlidingTabLayout}
 * to display a custom {@link ViewPager} title strip which gives continuous feedback to the user
 * when scrolling.
 */
public class FragmentSearchUniSlidingTabsColors extends Fragment {

    public static LinearLayout llHeaderProgressFragmentContent;
    private static final String KEY_WHICH_FIND_UNI_TAB = "SearchUniTab";

    public static FragmentSearchUniSlidingTabsColors newInstance(String whichSearchUniTab) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_WHICH_FIND_UNI_TAB, whichSearchUniTab);

        FragmentSearchUniSlidingTabsColors fragment = new FragmentSearchUniSlidingTabsColors();
        fragment.setArguments(bundle);

        return fragment;
    }


    /**
     * This class represents a tab to be displayed by {@link ViewPager} and it's associated
     * {@link SlidingTabLayout}.
     */
    private class SamplePagerItem {
        private final CharSequence mTitle;
        private final int mIndicatorColor;
        private final int mDividerColor;

        SamplePagerItem(CharSequence title, int indicatorColor, int dividerColor) {
            mTitle = title;
            mIndicatorColor = indicatorColor;
            mDividerColor = dividerColor;
        }



        /**
         * @return A new {@link Fragment} to be displayed by a {@link ViewPager}
         */
        Fragment createFragment() {
            return FragmentContentSearchUni.newInstance(mTitle);
        }



        /**
         * @return the title which represents this tab. In this sample this is used directly by
         * {@link android.support.v4.view.PagerAdapter#getPageTitle(int)}
         */
        CharSequence getTitle() {
            return mTitle;
        }



        /**
         * @return the color to be used for indicator on the {@link SlidingTabLayout}
         */
        int getIndicatorColor() {
            return mIndicatorColor;
        }



        /**
         * @return the color to be used for right divider on the {@link SlidingTabLayout}
         */
        int getDividerColor() {
            return mDividerColor;
        }
    }


    /**
     * List of {@link SamplePagerItem} which represent this sample's tabs.
     */
    private List<SamplePagerItem> mTabs = new ArrayList<>();



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // BEGIN_INCLUDE (populate_tabs)
        /*
         * Populate our tab list with tabs. Each item contains a title, indicator color and divider
         * color, which are used by {@link SlidingTabLayout}.
         */
        mTabs.add(new SamplePagerItem(
                getString(R.string.find_uni_tab_girls), // Title
                Color.MAGENTA, // Indicator color
                Color.GRAY // Divider color
        ));

        mTabs.add(new SamplePagerItem(
                getString(R.string.find_uni_tab_boys), // Title
                Color.BLUE, // Indicator color
                Color.GRAY // Divider color
        ));

        // END_INCLUDE (populate_tabs)
    }

    /**
     * Inflates the {@link View} which will be displayed by this {@link Fragment}, from the app's
     * resources.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_content, container, false);
    }




    // BEGIN_INCLUDE (fragment_onviewcreated)
    /**
     * This is called after the {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} has finished.
     * Here we can pick out the {@link View}s we need to configure from the content view.
     *
     * We set the {@link ViewPager}'s adapter to be an instance of
     * {@link SampleFragmentPagerAdapter  }. The {@link SlidingTabLayout} is then given the
     * {@link ViewPager} so that it can populate itself.
     *
     * @param view View created in {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        llHeaderProgressFragmentContent = (LinearLayout) view.findViewById(R.id.llHeaderProgressFragmentContent);

        /*
         * A {@link ViewPager} which will be used in conjunction with the {@link SlidingTabLayout} above.
         */
        ViewPager mViewPager;
        // BEGIN_INCLUDE (setup_viewpager)
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        mViewPager = (ViewPager) view.findViewById(R.id.fcViewpager);
        mViewPager.setAdapter(new SampleFragmentPagerAdapter  (getChildFragmentManager()));

        String SearchUniTab = getArguments().getString(KEY_WHICH_FIND_UNI_TAB);
        int tabCount = 1;
        if (SearchUniTab != null) {
            if(SearchUniTab.equals(getString(R.string.find_uni_tab_girls))){
                tabCount = 0;
            }else if(SearchUniTab.equals(getString(R.string.find_uni_tab_boys))){
                tabCount = 1;
            }
        }

        mViewPager.setCurrentItem(tabCount);
        // END_INCLUDE (setup_viewpager)


        /*
         *
         * A custom {@link ViewPager} title strip which looks much like Tabs present in Android v4.0 and
         * above, but is designed to give continuous feedback to the user when scrolling.
         */
        SlidingTabLayout mSlidingTabLayout;

        // BEGIN_INCLUDE (setup_slidingtablayout)
        // Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
        // it's PagerAdapter set.
        mSlidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setViewPager(mViewPager);


        // BEGIN_INCLUDE (tab_colorizer)
        // Set a TabColorizer to customize the indicator and divider colors. Here we just retrieve
        // the tab at the position, and return it's set color
        mSlidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {

            @Override
            public int getIndicatorColor(int position) {
                return mTabs.get(position).getIndicatorColor();
            }

            @Override
            public int getDividerColor(int position) {
                return mTabs.get(position).getDividerColor();
            }

        });
        // END_INCLUDE (tab_colorizer)
        // END_INCLUDE (setup_slidingtablayout)
    }
    // END_INCLUDE (fragment_onviewcreated)





    /**
     * The {@link FragmentPagerAdapter  } used to display pages in this sample. The individual pages
     * are instances of {@link FragmentSearchUniSlidingTabsColors} which just display three lines of text. Each page is
     * created by the relevant {@link SamplePagerItem} for the requested position.
     * <p>
     * The important section of this class is the {@link #getPageTitle(int)} method which controls
     * what is displayed in the {@link SlidingTabLayout}.
     */
    private class SampleFragmentPagerAdapter   extends FragmentPagerAdapter   {

        SampleFragmentPagerAdapter  (FragmentManager fm) {
            super(fm);
        }


        /**
         * Return the {@link Fragment} to be displayed at {@code position}.
         * <p>
         * Here we return the value returned from {SamplePagerItem#createFragment()}.
         */
        @Override
        public Fragment getItem(int position) {
            return mTabs.get(position).createFragment();
        }


        @Override
        public int getCount() {
            return mTabs.size();
        }



        // BEGIN_INCLUDE (pageradapter_getpagetitle)
        /**
         * Return the title of the item at {@code position}. This is important as what this method
         * returns is what is displayed in the {@link SlidingTabLayout}.
         * <p>
         * Here we return the value returned from {@link SamplePagerItem#getTitle()}.
         */
        @Override
        public CharSequence getPageTitle(int position) {
            return mTabs.get(position).getTitle();
        }
        // END_INCLUDE (pageradapter_getpagetitle)

    }

}