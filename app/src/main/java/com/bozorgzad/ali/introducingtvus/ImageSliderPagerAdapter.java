package com.bozorgzad.ali.introducingtvus;

import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/*
 * Created by Ali_Dev on 7/8/2017.
 */

class ImageSliderPagerAdapter extends PagerAdapter {

    private ArrayList<String> imageSliderPhoto;

    ImageSliderPagerAdapter(ArrayList<String> imageSliderPhoto) {
        this.imageSliderPhoto = imageSliderPhoto;
    }


    @Override
    public int getCount() {
        return imageSliderPhoto.size();
    }


    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }


    @Override
    public Object instantiateItem(ViewGroup viewGroup, int position) {
        View image_slider_main = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.image_slider_main, null);
        ImageView imageSlider = (ImageView) image_slider_main.findViewById(R.id.imageSlider);

        Picasso.with(imageSlider.getContext())
                .load(Global.HOST_ADDRESS + imageSliderPhoto.get(position))
                .placeholder(R.drawable.ic_loading_slider)
                .error(R.drawable.ic_no_image_slider)
                .fit()
                .into(imageSlider);

        viewGroup.addView(image_slider_main);
        return image_slider_main;
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
