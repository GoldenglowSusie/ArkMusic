package com.prts.arkmusic;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.os.IBinder;
import android.util.Log;

import android.media.MediaPlayer;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.extractor.flac.FlacExtractor;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.util.Util;
import com.prts.arkmusic.ui.ep.EpFragment;

public class ms3 extends Service {

    AssetManager am;
    Thread processThread;
    static final String[] musics = {"sparkfordream.flac","lullabye.flac","spark.wav","lead_seal.flac","blade.flac","dawnseeker.flac","spect.flac"};

    // 当前的状态，0x11代表没有播放；0x12代表正在播放；0x13代表暂停
    int status = 0x11;
    // 记录当前正在播放的音乐
    int current = 0;
    int suzy=0x11;
    int egg=0;
    static final String CTL_ACTION = "arkmusic.action.CTL_ACTION";
    static final String UPDATE_ACTION = "arkmusic.action.UPDATE_ACTION";


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        am = getAssets(); //获取附件管理器
        ExoPlayer ep=new ExoPlayer.Builder(getApplicationContext()).build();
        // 创建IntentFilter
        IntentFilter filter = new IntentFilter();
        filter.addAction(MainActivity.CTL_ACTION);

        Player.Listener listener=new Player.Listener() {
            @Override
            public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                current++;
                ep.setRepeatMode(Player.REPEAT_MODE_ONE);
            }
        };

        class MyReceiver extends BroadcastReceiver {
            @Override
            public void onReceive(final Context context, Intent intent) {
                // 获取Intent中的Control状态
                Intent sendIntent = new Intent(EpFragment.UPDATE_ACTION);
                int control = intent.getIntExtra("control", -1);
                switch (control) {
                    // 播放或暂停
                    case 13:
                        // 原来处于没有播放状态
                        if(egg==1){
                            ep.prepare();
                            ep.play();
                            egg=2;
                            status=0x12;
                            sendIntent.putExtra("update",status);
                            sendIntent.putExtra("suzy",suzy);
                            sendBroadcast(sendIntent);
                        }
                        if (status == 0x11) {
                            if(current==0||current==2){
                                suzy=0x12;
                            }
                            else{
                                suzy=0x11;
                            }
                            // 准备并播放音乐
                            status = 0x12;
                            ep.addMediaItem(MediaItem.fromUri("asset:///" + musics[current]));
                            ep.setRepeatMode(Player.REPEAT_MODE_ONE);
                            ep.prepare();
                            ep.play();
                            sendIntent.putExtra("update", status);
                            sendIntent.putExtra("current", current);
                            sendIntent.putExtra("suzy",suzy);
                            sendBroadcast(sendIntent);
                        }
                        // 原来处于播放状态
                        else if (status == 0x12) {
                            // 暂停
                            ep.pause();
                            // 改变为暂停状态
                            status = 0x13;
                            sendIntent.putExtra("update", status);
                            sendBroadcast(sendIntent);
                            if(egg==2){}
                            else{
                            sendIntent.putExtra("update", status);
                            sendIntent.putExtra("current", current);
                            sendIntent.putExtra("suzy",suzy);
                            sendBroadcast(sendIntent);}
                        }
                        // 原来处于暂停状态
                        else if (status == 0x13) {
                            // 播放
                            ep.play();
                            // 改变状态
                            status = 0x12;
                            sendIntent.putExtra("update", status);
                            sendBroadcast(sendIntent);
                            if(egg==2){}
                            else{
                            sendIntent.putExtra("update", status);
                            sendIntent.putExtra("current", current);
                            sendIntent.putExtra("suzy",suzy);
                            sendBroadcast(sendIntent);}
                        }

                        break;
                    //停止声音
                    case 23:
                        egg=0;
                        if (status == 0x12 || status == 0x13) {
                            ep.stop();
                            ep.clearMediaItems();
                            status=0x11;
                        }
                        sendIntent.putExtra("update", status);
                        sendIntent.putExtra("current", current);
                        sendIntent.putExtra("suzy",suzy);
                        sendBroadcast(sendIntent);
                        break;
                    //上一首
                    case 33:
                        // 如果原来正在播放或暂停
                        egg=0;
                        ep.stop();
                        ep.clearMediaItems();
                        if(current==0){
                            current=musics.length-1;
                        }
                        else{
                            current--;
                        }
                        if(current==0||current==2){
                            suzy=0x12;
                        }
                        else{
                            suzy=0x11;
                        }

                        if (status == 0x12 || status == 0x13) {
                            ep.addMediaItem(MediaItem.fromUri("asset:///" + musics[current]));
                            ep.setRepeatMode(Player.REPEAT_MODE_ONE);
                            ep.prepare();
                            ep.play();
                            status = 0x12;
                        }
                        sendIntent.putExtra("update", status);
                        sendIntent.putExtra("current", current);
                        sendIntent.putExtra("suzy",suzy);
                        sendBroadcast(sendIntent);
                        break;
                    //下一首
                    case 43:
                        egg=0;
                        ep.stop();
                        ep.clearMediaItems();
                        if(current==musics.length-1){
                            current=0;
                        }
                        else{
                            current++;
                        }
                        if(current==0||current==2){
                            suzy=0x12;
                        }
                        else{
                            suzy=0x11;
                        }
                        if (status == 0x12 || status == 0x13) {
                            ep.addMediaItem(MediaItem.fromUri("asset:///" + musics[current]));
                            ep.setRepeatMode(Player.REPEAT_MODE_ONE);
                            ep.prepare();
                            ep.play();
                            status = 0x12;
                        }
                        sendIntent.putExtra("update", status);
                        sendIntent.putExtra("current", current);
                        sendIntent.putExtra("suzy",suzy);
                        sendBroadcast(sendIntent);
                        break;
                    case 53:
                        ep.stop();
                        ep.clearMediaItems();
                        current=0;
                        status=0x11;
                    case 63:
                        ep.pause();
                        status=0x13;
                    case 1590107:
                        if(current==0){
                            if(status==0x11){
                                ep.clearMediaItems();
                                ep.setRepeatMode(Player.REPEAT_MODE_ONE);
                                ep.addMediaItem(MediaItem.fromUri("asset:///easteregg.wav"));
                                egg=1;
                                sendIntent.putExtra("egg",1);
                                sendBroadcast(sendIntent);
                            }
                        }
                }

            }


        }
        MyReceiver serviceReceiver;
        // 创建BroadcastReceiver
        serviceReceiver = new MyReceiver();
        registerReceiver(serviceReceiver, filter);

        // 为MediaPlayer播放完成事件绑定监听器
    }
}
