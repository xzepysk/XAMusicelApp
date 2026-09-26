package com.tsync.xamusicel;
public class Song {
    public String title,artist,artwork,url,duration;
    public int playCount;
    public Song(String t,String a,String art,String u,String d){
        title=t;artist=a;artwork=art;url=u;duration=d;
        playCount=(int)(Math.random()*5000+1);
    }
}