package com.tsync.xamusicel;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.media.app.NotificationCompat.MediaStyle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.support.v4.media.session.MediaSessionCompat;
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
    String RELAY = "http://benben.seyori.name.ng:2054/relay?url=";
    MediaSessionCompat mediaSession;

    @Override protected void onCreate(Bundle b){
        super.onCreate(b); setContentView(R.layout.activity_main);
        searchBar=findViewById(R.id.searchBar); recycler=findViewById(R.id.recycler);
        txtStatus=findViewById(R.id.txtStatus); miniPlayer=findViewById(R.id.miniPlayer);
        pThumb=findViewById(R.id.pThumb); pTitle=findViewById(R.id.pTitle); pArtist=findViewById(R.id.pArtist); pPlay=findViewById(R.id.pPlay);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter=new SongAdapter(list, s-> play(s));
        recycler.setAdapter(adapter);
        player=new ExoPlayer.Builder(this).build();
        if(android.os.Build.VERSION.SDK_INT>=26){
            NotificationChannel ch=new NotificationChannel("music","Music", NotificationManager.IMPORTANCE_LOW);
            ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(ch);
        }
        mediaSession=new MediaSessionCompat(this,"XamusiceL");
        mediaSession.setActive(true);

        searchBar.setOnEditorActionListener((v,a,e)->{ search(v.getText().toString()); return true; });
        View tab=findViewById(R.id.tabSongs);
        if(tab!=null) tab.setOnClickListener(v-> search(searchBar.getText().toString().isEmpty()?"system of a down":searchBar.getText().toString()));
        pPlay.setOnClickListener(v->{ if(player.isPlaying()){player.pause(); pPlay.setImageResource(android.R.drawable.ic_media_play);} else {player.play(); pPlay.setImageResource(android.R.drawable.ic_media_pause);} showNotif(list.isEmpty()?null:list.get(0)); });

        search("system of a down");
    }

    void search(String term){
        if(term==null||term.trim().isEmpty()) return;
        txtStatus.setVisibility(View.VISIBLE); txtStatus.setText("Searching...");
        try{
            String target="https://api-faa.my.id/faa/youtube?q="+URLEncoder.encode(term,"UTF-8");
            String url=RELAY+URLEncoder.encode(target,"UTF-8");
            client.newCall(new Request.Builder().url(url).build()).enqueue(new Callback(){
                public void onFailure(Call c, java.io.IOException e){ searchDirect(term); }
                public void onResponse(Call c, Response r) throws java.io.IOException{
                    try{
                        String body=r.body().string(); if(body.contains("Relay error")){searchDirect(term); return;}
                        JSONObject obj=new JSONObject(body); JSONArray arr=obj.optJSONArray("result"); if(arr==null) arr=obj.optJSONArray("data"); if(arr==null) arr=new JSONArray();
                        if(arr.length()==0){searchDirect(term); return;}
                        List<Song> n=new ArrayList<>();
                        for(int i=0;i<arr.length();i++){ JSONObject o=arr.getJSONObject(i); Song s=new Song(); s.title=o.optString("title"); s.channel=o.optString("channel",o.optString("author","")); s.thumb=o.optString("thumbnail",o.optString("thumb","")); s.url=o.optString("url"); s.audio=s.url; if(s.url.isEmpty()) continue; n.add(s); }
                        runOnUiThread(()->{ txtStatus.setVisibility(View.GONE); list.clear(); list.addAll(n); adapter.notifyDataSetChanged(); recycler.scheduleLayoutAnimation(); });
                    }catch(Exception ex){ searchDirect(term); }
                }
            });
        }catch(Exception e){ searchDirect(term); }
    }
    void searchDirect(String term){
        try{
            String url="https://api-faa.my.id/faa/youtube?q="+URLEncoder.encode(term,"UTF-8");
            client.newCall(new Request.Builder().url(url).build()).enqueue(new Callback(){
                public void onFailure(Call c, java.io.IOException e){ runOnUiThread(()-> txtStatus.setText("API down")); }
                public void onResponse(Call c, Response r) throws java.io.IOException{
                    try{
                        JSONObject obj=new JSONObject(r.body().string()); JSONArray arr=obj.optJSONArray("result"); if(arr==null) arr=obj.optJSONArray("data"); if(arr==null) arr=new JSONArray();
                        List<Song> n=new ArrayList<>(); for(int i=0;i<arr.length();i++){ JSONObject o=arr.getJSONObject(i); Song s=new Song(); s.title=o.optString("title"); s.channel=o.optString("channel",""); s.thumb=o.optString("thumbnail",""); s.url=o.optString("url"); n.add(s); }
                        runOnUiThread(()->{ txtStatus.setVisibility(View.GONE); list.clear(); list.addAll(n); adapter.notifyDataSetChanged(); });
                    }catch(Exception e){}
                }
            });
        }catch(Exception e){}
    }

    void play(Song s){
        miniPlayer.setVisibility(View.VISIBLE); miniPlayer.setAlpha(0f); miniPlayer.animate().alpha(1f).setDuration(200).start();
        pTitle.setText(s.title); pArtist.setText(s.channel); Glide.with(this).load(s.thumb).into(pThumb);
        txtStatus.setVisibility(View.VISIBLE); txtStatus.setText("Loading audio...");
        try{
            String target="https://api-faa.my.id/faa/ytmp3?url="+URLEncoder.encode(s.url,"UTF-8");
            String apiUrl=RELAY+URLEncoder.encode(target,"UTF-8");
            client.newCall(new Request.Builder().url(apiUrl).build()).enqueue(new Callback(){
                public void onFailure(Call c, java.io.IOException e){ runOnUiThread(()-> txtStatus.setText("Failed")); }
                public void onResponse(Call c, Response r) throws java.io.IOException{
                    try{
                        String body=r.body().string(); JSONObject obj=new JSONObject(body); String mp3=null;
                        if(obj.has("result")){ JSONObject res=obj.getJSONObject("result"); mp3=res.optString("download"); if(mp3.isEmpty()) mp3=res.optString("url"); }
                        if(mp3==null||mp3.isEmpty()) mp3=obj.optString("download"); if(mp3==null||mp3.isEmpty()) mp3=obj.optString("url");
                        if(mp3==null||mp3.isEmpty()){ runOnUiThread(()-> txtStatus.setText("No url")); return; }
                        String finalMp3=mp3;
                        runOnUiThread(()->{
                            txtStatus.setVisibility(View.GONE);
                            player.setMediaItem(MediaItem.fromUri(finalMp3)); player.prepare(); player.play();
                            pPlay.setImageResource(android.R.drawable.ic_media_pause);
                            showNotif(s);
                        });
                    }catch(Exception ex){ runOnUiThread(()-> txtStatus.setText(ex.getMessage())); }
                }
            });
        }catch(Exception e){}
    }

    void showNotif(Song s){
        if(s==null) return;
        new Thread(() -> {
            try{
                Bitmap bmp = Glide.with(this).asBitmap().load(s.thumb).submit(200,200).get();
                NotificationCompat.Builder nb = new NotificationCompat.Builder(this,"music")
                        .setSmallIcon(android.R.drawable.ic_media_play)
                        .setContentTitle(s.title)
                        .setContentText(s.channel)
                        .setLargeIcon(bmp)
                        .setStyle(new MediaStyle().setMediaSession(mediaSession.getSessionToken()).setShowActionsInCompactView(0,1,2))
                        .addAction(android.R.drawable.ic_media_previous,"Prev",null)
                        .addAction(player.isPlaying()?android.R.drawable.ic_media_pause:android.R.drawable.ic_media_play,"Play",null)
                        .addAction(android.R.drawable.ic_media_next,"Next",null)
                        .setOngoing(player.isPlaying())
                        .setColor(0xFF3B82F6);
                NotificationManagerCompat.from(this).notify(1,nb.build());
            }catch(Exception e){
                NotificationCompat.Builder nb = new NotificationCompat.Builder(this,"music")
                        .setSmallIcon(android.R.drawable.ic_media_play)
                        .setContentTitle(s.title)
                        .setContentText(s.channel)
                        .setStyle(new MediaStyle().setMediaSession(mediaSession.getSessionToken()))
                        .setOngoing(true).setColor(0xFF3B82F6);
                NotificationManagerCompat.from(this).notify(1,nb.build());
            }
        }).start();
    }

    public static class Song{ public String title,channel,thumb,url,audio; }
}
