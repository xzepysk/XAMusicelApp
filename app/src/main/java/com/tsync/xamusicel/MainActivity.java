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
    ExoPlayer player;
    
    // GANTI PORT DISINI
    String RELAY = "http://benben.seyori.name.ng:2054/relay?url=";

    @Override protected void onCreate(Bundle b){
        super.onCreate(b); setContentView(R.layout.activity_main);
        searchBar=findViewById(R.id.searchBar); recycler=findViewById(R.id.recycler);
        txtStatus=findViewById(R.id.txtStatus); miniPlayer=findViewById(R.id.miniPlayer);
        pThumb=findViewById(R.id.pThumb); pTitle=findViewById(R.id.pTitle); pArtist=findViewById(R.id.pArtist); pPlay=findViewById(R.id.pPlay);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter=new SongAdapter(list, s-> play(s));
        recycler.setAdapter(adapter);
        player=new ExoPlayer.Builder(this).build();

        searchBar.setOnEditorActionListener((v,a,e)->{ search(v.getText().toString()); return true; });
        View tab=findViewById(R.id.tabSongs);
        if(tab!=null) tab.setOnClickListener(v-> search(searchBar.getText().toString().isEmpty() ? "metallica" : searchBar.getText().toString()));
        
        pPlay.setOnClickListener(v->{ if(player.isPlaying()){player.pause(); pPlay.setImageResource(android.R.drawable.ic_media_play);} else {player.play(); pPlay.setImageResource(android.R.drawable.ic_media_pause);} });
        search("metallica");
    }

    void search(String term){
        if(term==null||term.trim().isEmpty()) return;
        txtStatus.setVisibility(View.VISIBLE); txtStatus.setText("Searching...");
        try{
            String target="https://api-faa.my.id/faa/youtube?q="+URLEncoder.encode(term,"UTF-8");
            String relayUrl = RELAY + URLEncoder.encode(target,"UTF-8");
            
            Request req = new Request.Builder().url(relayUrl).build();
            client.newCall(req).enqueue(new Callback(){
                public void onFailure(Call c, java.io.IOException e){ 
                    runOnUiThread(()-> txtStatus.setText("Keep searching..."));
                    searchDirect(term); 
                }
                public void onResponse(Call c, Response r) throws java.io.IOException{
                    try{
                        String body=r.body().string();
                        if(body.contains("Error") || body.length()<50){
                            searchDirect(term);
                            return;
                        }
                        JSONObject obj=new JSONObject(body);
                        JSONArray arr=obj.optJSONArray("result");
                        if(arr==null) arr=obj.optJSONArray("data");
                        if(arr==null) arr=new JSONArray();
                        if(arr.length()==0){ searchDirect(term); return; }
                        
                        List<Song> n=new ArrayList<>();
                        for(int i=0;i<arr.length();i++){
                            JSONObject o=arr.getJSONObject(i); Song s=new Song();
                            s.title=o.optString("title"); s.channel=o.optString("channel",o.optString("author",""));
                            s.thumb=o.optString("thumbnail",o.optString("thumb",""));
                            s.url=o.optString("url"); s.audio=o.optString("audio",o.optString("link",o.optString("url")));
                            if(s.audio==null||s.audio.isEmpty()) continue; n.add(s);
                        }
                        runOnUiThread(()->{
                            txtStatus.setVisibility(View.GONE);
                            list.clear(); list.addAll(n); adapter.notifyDataSetChanged();
                            recycler.scheduleLayoutAnimation();
                        });
                    }catch(Exception ex){ searchDirect(term); }
                }
            });
        }catch(Exception e){ searchDirect(term); }
    }

    void searchDirect(String term){
        runOnUiThread(()-> txtStatus.setText("Searching..."));
        try{
            String url="https://api-faa.my.id/faa/youtube?q="+URLEncoder.encode(term,"UTF-8");
            client.newCall(new Request.Builder().url(url).build()).enqueue(new Callback(){
                public void onFailure(Call c, java.io.IOException e){ searchPiped(term); }
                public void onResponse(Call c, Response r) throws java.io.IOException{
                    try{
                        JSONObject obj=new JSONObject(r.body().string());
                        JSONArray arr=obj.optJSONArray("result"); if(arr==null) arr=obj.optJSONArray("data"); if(arr==null) arr=new JSONArray();
                        if(arr.length()==0){ searchPiped(term); return; }
                        List<Song> n=new ArrayList<>();
                        for(int i=0;i<arr.length();i++){ JSONObject o=arr.getJSONObject(i); Song s=new Song(); s.title=o.optString("title"); s.channel=o.optString("channel",o.optString("author","")); s.thumb=o.optString("thumbnail",o.optString("thumb","")); s.url=o.optString("url"); s.audio=o.optString("audio",o.optString("link",o.optString("url"))); if(s.audio.isEmpty()) continue; n.add(s); }
                        runOnUiThread(()->{ txtStatus.setVisibility(View.GONE); list.clear(); list.addAll(n); adapter.notifyDataSetChanged(); recycler.scheduleLayoutAnimation(); });
                    }catch(Exception ex){ searchPiped(term); }
                }
            });
        }catch(Exception e){ searchPiped(term); }
    }

    void searchPiped(String term){
        runOnUiThread(()-> txtStatus.setText("Backup Piped..."));
        try{
            String url="https://pipedapi.kavin.rocks/search?q="+URLEncoder.encode(term,"UTF-8");
            client.newCall(new Request.Builder().url(url).build()).enqueue(new Callback(){
                public void onFailure(Call c, java.io.IOException e){ runOnUiThread(()-> txtStatus.setText("No providers")); }
                public void onResponse(Call c, Response r) throws java.io.IOException{
                    try{
                        JSONArray arr=new JSONObject(r.body().string()).optJSONArray("items"); if(arr==null) arr=new JSONArray();
                        List<Song> n=new ArrayList<>();
                        for(int i=0;i<arr.length();i++){ JSONObject o=arr.getJSONObject(i); if(!"stream".equals(o.optString("type"))) continue; Song s=new Song(); s.title=o.optString("title"); s.channel=o.optString("uploaderName"); s.thumb=o.optString("thumbnail"); String u=o.optString("url","").replace("/watch?v=",""); s.url="https://youtube.com/watch?v="+u; s.audio=s.url; n.add(s); }
                        runOnUiThread(()->{ txtStatus.setVisibility(View.GONE); list.clear(); list.addAll(n); adapter.notifyDataSetChanged(); recycler.scheduleLayoutAnimation(); });
                    }catch(Exception ex){ runOnUiThread(()-> txtStatus.setText("Backup fail")); }
                }
            });
        }catch(Exception e){}
    }

    void play(Song s){
        miniPlayer.setVisibility(View.VISIBLE); miniPlayer.setAlpha(0f); miniPlayer.animate().alpha(1f).setDuration(200).start();
        pTitle.setText(s.title); pArtist.setText(s.channel); Glide.with(this).load(s.thumb).into(pThumb);
        String playUrl=s.audio;
        if(playUrl.contains("youtube.com")){
            try{
                String target="https://api-faa.my.id/faa/ytmp3?url="+URLEncoder.encode(s.url,"UTF-8");
                playUrl = RELAY + URLEncoder.encode(target,"UTF-8");
            }catch(Exception e){}
        }
        player.setMediaItem(MediaItem.fromUri(playUrl)); player.prepare(); player.play();
        pPlay.setImageResource(android.R.drawable.ic_media_pause);
        Toast.makeText(this,"Playing: "+s.title,Toast.LENGTH_SHORT).show();
    }
    public static class Song{ public String title,channel,thumb,url,audio; }
}
