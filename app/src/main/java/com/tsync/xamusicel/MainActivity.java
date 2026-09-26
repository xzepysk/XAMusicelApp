package com.tsync.xamusicel;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import okhttp3.*;

public class MainActivity extends AppCompatActivity {
    EditText searchBar; RecyclerView recycler; TextView txtStatus;
    List<Song> list = new ArrayList<>(); SongAdapter adapter;
    OkHttpClient client = new OkHttpClient();
    View miniPlayer; ImageView pThumb, pPlay; TextView pTitle, pArtist;
    ExoPlayer player; Song current;

    @Override protected void onCreate(Bundle b){
        super.onCreate(b); setContentView(R.layout.activity_main);
        searchBar=findViewById(R.id.searchBar); recycler=findViewById(R.id.recycler);
        txtStatus=findViewById(R.id.txtStatus); miniPlayer=findViewById(R.id.miniPlayer);
        pThumb=findViewById(R.id.pThumb); pTitle=findViewById(R.id.pTitle); pArtist=findViewById(R.id.pArtist); pPlay=findViewById(R.id.pPlay);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter=new SongAdapter(list, s->{ play(s); });
        recycler.setAdapter(adapter);
        player=new ExoPlayer.Builder(this).build();

        searchBar.setOnEditorActionListener((v,a,e)->{ search(v.getText().toString()); return true; });
        pPlay.setOnClickListener(v->{ if(player.isPlaying()){player.pause(); pPlay.setImageResource(android.R.drawable.ic_media_play);} else {player.play(); pPlay.setImageResource(android.R.drawable.ic_media_pause);} });
        search("Metallica");
    }

    void search(String term){
        if(term.isEmpty()) return;
        txtStatus.setVisibility(View.VISIBLE); txtStatus.setText("Searching...");
        try{
            String target="https://api-faa.my.id/faa/youtube?q="+URLEncoder.encode(term,"UTF-8");
            String relay="http://benben.seyori.name.ng:2064/relay?url="+URLEncoder.encode(target,"UTF-8");
            client.newCall(new Request.Builder().url(relay).build()).enqueue(new Callback(){
                public void onFailure(Call c, java.io.IOException e){ runOnUiThread(()->txtStatus.setText("Relay error"));}
                public void onResponse(Call c, Response r) throws java.io.IOException{
                    try{
                        JSONObject obj=new JSONObject(r.body().string());
                        JSONArray arr=obj.optJSONArray("result"); if(arr==null) arr=obj.optJSONArray("data"); if(arr==null) arr=new JSONArray();
                        List<Song> n=new ArrayList<>();
                        for(int i=0;i<arr.length();i++){ JSONObject o=arr.getJSONObject(i); Song s=new Song();
                            s.title=o.optString("title"); s.channel=o.optString("channel",o.optString("author",""));
                            s.thumb=o.optString("thumbnail",o.optString("thumb","")); s.url=o.optString("url");
                            s.audio=o.optString("audio",o.optString("link",o.optString("url"))); n.add(s);
                        }
                        runOnUiThread(()->{ txtStatus.setVisibility(View.GONE); list.clear(); list.addAll(n); adapter.notifyDataSetChanged(); recycler.scheduleLayoutAnimation(); if(n.isEmpty()) txtStatus.setVisibility(View.VISIBLE); });
                    }catch(Exception ex){ runOnUiThread(()->txtStatus.setText("Parse error")); }
                }
            });
        }catch(Exception e){}
    }

    void play(Song s){
        current=s; miniPlayer.setVisibility(View.VISIBLE); miniPlayer.setAlpha(0f); miniPlayer.animate().alpha(1f).setDuration(200).start();
        pTitle.setText(s.title); pArtist.setText(s.channel); Glide.with(this).load(s.thumb).into(pThumb);
        player.setMediaItem(MediaItem.fromUri(s.audio)); player.prepare(); player.play(); pPlay.setImageResource(android.R.drawable.ic_media_pause);
    }

    public static class Song{ public String title,channel,thumb,url,audio; }
}
