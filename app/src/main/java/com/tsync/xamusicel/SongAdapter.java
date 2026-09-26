package com.tsync.xamusicel;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.VH> {
    List<MainActivity.Song> list; OnClick click;
    public interface OnClick{ void onClick(MainActivity.Song s); }
    public SongAdapter(List<MainActivity.Song> l, OnClick c){list=l; click=c;}
    public static class VH extends RecyclerView.ViewHolder{ ImageView thumb; TextView title,channel; public VH(View v){super(v); thumb=v.findViewById(R.id.thumb); title=v.findViewById(R.id.title); channel=v.findViewById(R.id.channel);}}
    @Override public VH onCreateViewHolder(ViewGroup p,int t){return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_song,p,false));}
    @Override public void onBindViewHolder(VH h,int i){
        MainActivity.Song s=list.get(i); h.title.setText(s.title); h.channel.setText(s.channel);
        Glide.with(h.itemView).load(s.thumb).into(h.thumb);
        h.itemView.setOnClickListener(v->{ v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(80).withEndAction(()->v.animate().scaleX(1f).scaleY(1f).setDuration(80).start()).start(); click.onClick(s); });
    }
    @Override public int getItemCount(){return list.size();}
}
