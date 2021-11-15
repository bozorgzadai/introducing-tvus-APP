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
 * Created by Ali_Dev on 7/8/2017.
 */

class AdapterRvMostViewedUnis extends RecyclerView.Adapter<AdapterRvMostViewedUnis.ViewHolder>{

    private ArrayList<ActivityMain.MostViewedUni> mostViewedUnis;

    AdapterRvMostViewedUnis(ArrayList<ActivityMain.MostViewedUni> mostViewedUnis){
        this.mostViewedUnis = mostViewedUnis;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgMostViewedUni;
        TextView txtMostViewedUni;
        LinearLayout llMostViewedUni;

        ViewHolder(View itemView) {
            super(itemView);
            imgMostViewedUni = (ImageView)itemView.findViewById(R.id.imgMostViewedUni);
            txtMostViewedUni = (TextView)itemView.findViewById(R.id.txtMostViewedUni);
            llMostViewedUni = (LinearLayout)itemView.findViewById(R.id.llMostViewedUni);
        }
    }

    @Override
    public int getItemCount() {
        return mostViewedUnis.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View recycler_view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.rv_most_viewed_uni, viewGroup, false);
        return new ViewHolder(recycler_view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        final ActivityMain.MostViewedUni mostViewedUni = mostViewedUnis.get(i);

        if(Global.LANGUAGE.equals("fa")){
            viewHolder.txtMostViewedUni.setText(mostViewedUni.uniFullNameFa);
        }else{
            viewHolder.txtMostViewedUni.setText(mostViewedUni.uniFullNameEn);
        }

        if(Build.VERSION.SDK_INT < 23){
            viewHolder.llMostViewedUni.setBackgroundResource(R.drawable.ripple_most_viewed_uni);
        }

        Picasso.with(viewHolder.imgMostViewedUni.getContext())
                .load(Global.HOST_ADDRESS + mostViewedUni.uniLogo)
                .placeholder(R.drawable.ic_loading)
                .error(R.drawable.ic_no_image)
                .into(viewHolder.imgMostViewedUni);

        viewHolder.llMostViewedUni.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ActivityUniDetails.class);
                intent.putExtra("WhichUniDetailsId", mostViewedUni.uni_id);
                view.getContext().startActivity(intent);
            }
        });
    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

}