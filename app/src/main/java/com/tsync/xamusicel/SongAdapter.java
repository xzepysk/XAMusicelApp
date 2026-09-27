package com.tsync.xamusicel;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.VH> {
    List<MainActivity.Song> list;
    OnClick click;

    public interface OnClick{ void onClick(MainActivity.Song s); }
    public SongAdapter(List<MainActivity.Song> l, OnClick c){ list=l; click=c; }

    public static class VH extends RecyclerView.ViewHolder{
        ImageView thumb, btnAdd, btnShare;
        TextView title, channel;
        public VH(View v){
            super(v);
            thumb=v.findViewById(R.id.thumb);
            title=v.findViewById(R.id.title);
            channel=v.findViewById(R.id.channel);
            btnAdd=v.findViewById(R.id.btnAdd);
            btnShare=v.findViewById(R.id.btnShare);
        }
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int t){
        return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_song,p,false));
    }

    @Override public void onBindViewHolder(@NonNull VH h, int i){
        MainActivity.Song s=list.get(i);
        h.title.setText(s.title);
        h.channel.setText(s.channel);
        Glide.with(h.itemView.getContext()).load(s.thumb).placeholder(android.R.drawable.sym_def_app_icon).into(h.thumb);
        h.itemView.setOnClickListener(v -> {
            v.animate().scaleX(0.96f).scaleY(0.96f).setDuration(80).withEndAction(() -> {
                v.animate().scaleX(1f).scaleY(1f).setDuration(80).start();
                click.onClick(s);
            }).start();
        });

        h.btnAdd.setOnClickListener(v -> Toast.makeText(v.getContext(),"Added to playlist: "+s.title,Toast.LENGTH_SHORT).show());
        h.btnShare.setOnClickListener(v -> {
            android.content.Intent intent=new android.content.Intent(android.content.Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(android.content.Intent.EXTRA_TEXT,s.url);
            v.getContext().startActivity(android.content.Intent.createChooser(intent,"Share"));
        });
    }

    @Override public int getItemCount(){ return list.size(); }
}
