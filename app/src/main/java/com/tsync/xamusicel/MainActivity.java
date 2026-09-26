package com.tsync.xamusicel;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.*;
import okhttp3.*;
import java.net.URLEncoder;
import java.util.*;

public class MainActivity extends AppCompatActivity {
    EditText etSearch; RecyclerView rv; OkHttpClient client = new OkHttpClient();
    List<Song> songs = new ArrayList<>(); SongAdapter adapter; PlayerManager playerManager;
    String userIP = "unknown"; SharedPreferences playlistPref;

    @Override protected void onCreate(Bundle b){
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        etSearch=findViewById(R.id.etSearch);
        rv=findViewById(R.id.rv);
        getIP();
        playerManager = new PlayerManager(this);
        adapter = new SongAdapter(songs, this, playerManager);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);
        etSearch.setOnEditorActionListener((v,a,e)->{ search(etSearch.getText().toString()); return true; });
        search("void resonance");
    }
    void getIP(){
        new Thread(()->{
            try{
                Request req=new Request.Builder().url("https://api.ipify.org").build();
                userIP=client.newCall(req).execute().body().string();
                playlistPref=getSharedPreferences("playlist_"+userIP, MODE_PRIVATE);
            }catch(Exception e){ userIP="local"; }
        }).start();
    }
    void search(String term){
        if(term.isEmpty())return;
        new Thread(()->{
            try{
                String faa="https://api-faa.my.id/faa/youtube?q="+URLEncoder.encode(term,"UTF-8");
                String url="http://benben.seyori.name.ng:2064/relay?url="+URLEncoder.encode(faa,"UTF-8");
                Request req=new Request.Builder().url(url).build();
                Response res=client.newCall(req).execute();
                JsonObject obj=JsonParser.parseString(res.body().string()).getAsJsonObject();
                JsonArray arr=obj.has("data")?obj.getAsJsonArray("data"):obj.getAsJsonArray("result");
                List<Song> temp=new ArrayList<>();
                for(JsonElement el:arr){
                    JsonObject o=el.getAsJsonObject();
                    temp.add(new Song(o.get("title").getAsString(), o.has("author")?o.get("author").getAsString():o.get("channel").getAsString(), o.get("thumbnail").getAsString(), o.get("url").getAsString(), ""));
                }
                runOnUiThread(()->{ songs.clear(); songs.addAll(temp); adapter.notifyDataSetChanged(); });
            }catch(Exception ex){ex.printStackTrace();}
        }).start();
    }
    public void saveToPlaylist(Song s){
        if(playlistPref==null) return;
        Set<String> set=playlistPref.getStringSet("history", new HashSet<>());
        set.add(s.title+"|"+s.artist);
        playlistPref.edit().putStringSet("history", set).apply();
    }
}