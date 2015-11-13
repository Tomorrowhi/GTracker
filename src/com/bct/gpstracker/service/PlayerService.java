package com.bct.gpstracker.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.IBinder;
import android.util.Log;

import com.bct.gpstracker.common.Constants;

/**
 * Created by HH
 * Date: 2015/8/27 0027
 * Time: 下午 6:34
 */
public class PlayerService extends Service {
    private MediaPlayer mediaPlayer;		//媒体播放器对象
    private String path;						//音乐文件路径
    private boolean isPause;					//暂停状态
    private static PlayerService player;

    public static PlayerService getPlayer(){
        return player;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(mediaPlayer==null){
            mediaPlayer =  new MediaPlayer();
        }
        if(mediaPlayer.isPlaying()) {
            stop();
        }
        player=this;
        return super.onStartCommand(intent, flags, startId);
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setCompletionListener(MediaPlayer.OnCompletionListener listener){
        mediaPlayer.setOnCompletionListener(listener);
    }

    /**
     * 播放音乐
     */
    public int play() {
        return play(0);
    }

    /**
     * 播放音乐
     * @param position
     * @return 0 正常 -1失败
     */
    public int play(int position) {
        try {
            if(mediaPlayer==null){
                mediaPlayer=new MediaPlayer();
            }
            mediaPlayer.reset();//把各项参数恢复到初始状态
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//            path = "http://abv.cn/music/光辉岁月.mp3";
            mediaPlayer.setDataSource(path);
            Log.d(Constants.TAG, "音乐地址：" + path);
//            mediaPlayer.prepare();    //进行缓冲
            mediaPlayer.prepareAsync(); //播放网络用这个
            mediaPlayer.setOnPreparedListener(new PreparedListener(position));//注册一个监听器

            return 0;
        } catch (Exception e) {
            Log.e(Constants.TAG, "播放音频失败！", e);
            return -1;
        }
    }

    /**
     * 暂停音乐
     */
    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPause = true;
        }
    }

    /**
     * 停止音乐
     */
    public void stop(){
        if(mediaPlayer != null) {
            mediaPlayer.stop();
//            try {
//                mediaPlayer.prepare(); // 在调用stop后如果需要再次通过start进行播放,需要之前调用prepare函数
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        }
    }


    @Override
    public void onDestroy() {
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer=null;
        }
        player=null;
    }
    /**
     *
     * 实现一个OnPrepareLister接口,当音乐准备好的时候开始播放
     *
     */
    private final class PreparedListener implements OnPreparedListener {
        private int positon;

        public PreparedListener(int positon) {
            this.positon = positon;
        }

        @Override
        public void onPrepared(MediaPlayer mp) {
            mediaPlayer.start();    //开始播放
            if (positon > 0) {    //如果音乐不是从头播放
                mediaPlayer.seekTo(positon);
            }
        }
    }

}

