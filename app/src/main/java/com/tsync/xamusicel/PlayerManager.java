package com.tsync.xamusicel;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.*;

public class PlayerManager {
    ExoPlayer player; MainActivity act;
    public PlayerManager(MainActivity c){act=c; player=new ExoPlayer.Builder(c).build();}
    public void play(Song s){
        act.saveToPlaylist(s);
        View bottom=act.findViewById(R.id.bottomPlayer);
        TextView tTitle=act.findViewById(R.id.txtTitle);
        TextView tArtist=act.findViewById(R.id.txtArtist);
        ImageView art=act.findViewById(R.id.imgArt);
        ImageView btnPlay=act.findViewById(R.id.btnPlay);
        ImageView btnPrev=act.findViewById(R.id.btnPrev);
        ImageView btnNext=act.findViewById(R.id.btnNext);
        bottom.setVisibility(View.VISIBLE);
        bottom.setAlpha(0f); bottom.animate().alpha(1f).setDuration(300).start();
        tTitle.setText(s.title);
        tArtist.setText(s.artist);
        Glide.with(act).load(s.artwork).circleCrop().into(art);
        MediaItem item=MediaItem.fromUri(s.url);
        player.setMediaItem(item); player.prepare(); player.play();
        btnPlay.setImageResource(android.R.drawable.ic_media_pause);
        btnPlay.setOnClickListener(v->{
            if(player.isPlaying()){ player.pause(); btnPlay.setImageResource(android.R.drawable.ic_media_play); }
            else { player.play(); btnPlay.setImageResource(android.R.drawable.ic_media_pause); }
        });
        btnPrev.setOnClickListener(v->player.seekTo(Math.max(0,player.getCurrentPosition()-10000)));
        btnNext.setOnClickListener(v->player.seekTo(player.getCurrentPosition()+10000));
    }
}