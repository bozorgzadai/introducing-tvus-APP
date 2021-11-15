package com.bozorgzad.ali.introducingtvus;

import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/*
 * Created by Ali_Dev on 7/11/2017.
 */

class AdapterRvSearchUnis extends RecyclerView.Adapter<AdapterRvSearchUnis.ViewHolder>{

    static final int LIST_IMAGE_SIDE_ITEM = 0;
    static final int LIST_IMAGE_TOP_ITEM = 1;
    static final int GRID_ITEM = 2;
    static int currentViewTypeItem = LIST_IMAGE_SIDE_ITEM;

    private ArrayList<ActivitySearchUni.Uni> unis;

    AdapterRvSearchUnis(ArrayList<ActivitySearchUni.Uni> unis){
        this.unis = unis;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgSearchUni;
        TextView txtSearchUniName;
        TextView txtSearchUniState;
        LinearLayout llSearchUnis;

        ViewHolder(View itemView) {
            super(itemView);
            imgSearchUni = (ImageView)itemView.findViewById(R.id.imgSearchUni);
            txtSearchUniName = (TextView)itemView.findViewById(R.id.txtSearchUniName);
            txtSearchUniState = (TextView)itemView.findViewById(R.id.txtSearchUniState);
            llSearchUnis = (LinearLayout)itemView.findViewById(R.id.llSearchUnis);
        }
    }

    @Override
    public int getItemCount() {
        return unis.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView;

        if (viewType == LIST_IMAGE_TOP_ITEM){
            itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.rv_search_unis_image_top, viewGroup, false);
        }else if(viewType == GRID_ITEM){
            itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.rv_search_unis_image_grid, viewGroup, false);
        }else{
            itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.rv_search_unis_image_side, viewGroup, false);
        }
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        final ActivitySearchUni.Uni uni = unis.get(i);

        if(Global.LANGUAGE.equals("fa")){
            viewHolder.txtSearchUniName.setText(uni.uniFullNameFa);
            viewHolder.txtSearchUniState.setText(uni.uniStateFa);
        }else{
            viewHolder.txtSearchUniName.setText(uni.uniFullNameEn);
            viewHolder.txtSearchUniState.setText(uni.uniStateEn);
        }

        if(Build.VERSION.SDK_INT < 23){
            viewHolder.llSearchUnis.setBackgroundResource(R.drawable.ripple_search_uni);
        }

        if(uni.uniLogo.equals("null")){
            viewHolder.imgSearchUni.setImageResource(R.drawable.ic_no_image);
        }else {
            Picasso.with(viewHolder.imgSearchUni.getContext())
                    .load(Global.HOST_ADDRESS + uni.uniLogo)
                    .placeholder(R.drawable.ic_loading)
                    .error(R.drawable.ic_no_image)
                    .into(viewHolder.imgSearchUni);
        }

        if(currentViewTypeItem == GRID_ITEM && Global.LANGUAGE.equals("en")){
            int llSearchUnisMinHeightDp = 240;
            float density = viewHolder.llSearchUnis.getContext().getResources().getDisplayMetrics().density;
            int llSearchUnisMinHeight = (int)(llSearchUnisMinHeightDp * density);
            viewHolder.llSearchUnis.setMinimumHeight(llSearchUnisMinHeight);
        }

        viewHolder.llSearchUnis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ActivityUniDetails.class);
                intent.putExtra("WhichUniDetailsId", uni.uni_id);
                view.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemViewType (int position) {
        return currentViewTypeItem;
    }

    int toggleItemViewType () {
        currentViewTypeItem++;
        if(currentViewTypeItem > 2){
            currentViewTypeItem = 0;
        }
        return currentViewTypeItem;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

}