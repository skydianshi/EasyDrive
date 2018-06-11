package com.saic.easydrive.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public class MusicService extends Service {
    private static final File MUSIC_PATH = Environment.getExternalStorageDirectory();// 找到music存放的路径。
    public List<String> musicList;// 存放找到的所有mp3的绝对路径。
    public MediaPlayer player; // 定义多媒体对象
    public int songNum; // 当前播放的歌曲在List中的下标
    public String songName; // 当前播放的歌曲名

    public MusicService() {
        musicList = new ArrayList<String>();
        player = new MediaPlayer();

        if (MUSIC_PATH.listFiles(new MusicFilter()).length > 0) {
            for (File file : MUSIC_PATH.listFiles(new MusicFilter())) {
                musicList.add(file.getAbsolutePath());
            }
        }
    }

    class MusicFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return (name.endsWith(".mp3"));//返回当前目录所有以.mp3结尾的文件
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return new MsgBinder();
    }

    public class MsgBinder extends Binder {
        public MusicService getService(){
            return MusicService.this;
        }
    }

    public void setPlayName(String dataSource) {
        File file = new File(dataSource);//假设为D:\\mm.mp3
        String name = file.getName();//name=mm.mp3
        int index = name.lastIndexOf(".");//找到最后一个.
        songName = name.substring(0, index);//截取为mm
    }

    public void startPlay(){
        try {
            player.reset(); //重置多媒体
            String dataSource = musicList.get(songNum);//得到当前播放音乐的路径
            setPlayName(dataSource);//截取歌名
            player.setDataSource(dataSource);//为多媒体对象设置播放路径
            player.prepare();//准备播放
            player.start();//开始播放
            //setOnCompletionListener 当当前多媒体对象播放完成时发生的事件
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer arg0) {
                    playNextSong();//如果当前歌曲播放完毕,自动播放下一首.
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void pausePlay(){
        if (player.isPlaying())
            player.pause();
        else{
            player.start();
        }
    }

    public void stopPlay(){
        if (player.isPlaying()) {
            player.stop();
        }
    }
    public void playPreSong(){
        songNum = songNum == 0 ? musicList.size() - 1 : songNum - 1;
        startPlay();
    }
    public void playNextSong(){
        songNum = songNum == musicList.size() - 1 ? 0 : songNum + 1;
        startPlay();
    }
}
