package com.tsync.xamusicel;
import android.content.Context;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.VH>{
    List<Song> list; Context ctx; PlayerManager pm;
    public SongAdapter(List<Song> l, Context c, PlayerManager p){list=l;ctx=c;pm=p;}
    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p,int t){
        View v=LayoutInflater.from(p.getContext()).inflate(R.layout.item_song,p,false);
        return new VH(v);
    }
    @Override public void onBindViewHolder(@NonNull VH h,int i){
        Song s=list.get(i);
        h.title.setText(s.title);
        h.artist.setText(s.artist);
        h.badge.setText(String.valueOf(s.playCount));
        Glide.with(ctx).load(s.artwork).circleCrop().into(h.art);
        h.itemView.setOnClickListener(v->{
            v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(80).withEndAction(()->v.animate().scaleX(1f).scaleY(1f).setDuration(80).start()).start();
            pm.play(s);
        });
    }
    @Override public int getItemCount(){return list.size();}
    static class VH extends RecyclerView.ViewHolder{
        TextView title,artist,badge; ImageView art;
        public VH(@NonNull View v){super(v);
            title=v.findViewById(R.id.title);
            artist=v.findViewById(R.id.artist);
            art=v.findViewById(R.id.artwork);
            badge=v.findViewById(R.id.badge);
        }
    }
}