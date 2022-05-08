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

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.util.Util;
import com.prts.arkmusic.ui.home.HomeFragment;

public class MusicService extends Service {

    AssetManager am;
    Thread processThread;
    static final String[] musics = {"m_avg_chenandwei_intro.wav", "m_avg_chenandwei_loop.wav", "m_sys_act18d0d0_intro.wav", "m_sys_act18d0d0_loop.wav"};


    // 当前的状态，0x11代表没有播放；0x12代表正在播放；0x13代表暂停
    int status = 0x11;
    // 记录当前正在播放的音乐
    int current = 0;
    int suzy=0x11;
    static final String CTL_ACTION = "arkmusic.action.CTL_ACTION";
    static final String UPDATE_ACTION = "arkmusic.action.UPDATE_ACTION";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("MusicService", "onCreate");
        am = getAssets(); //获取附件管理器
        ExoPlayer ep=new ExoPlayer.Builder(getApplicationContext()).build();
        // 创建IntentFilter
        IntentFilter filter = new IntentFilter();
        filter.addAction(MainActivity.CTL_ACTION);

        Player.Listener listener=new Player.Listener() {
            @Override
            public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                if(reason==Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT){

                }
                else{
                current++;
                ep.setRepeatMode(Player.REPEAT_MODE_ONE);}
            }
        };

        class MyReceiver extends BroadcastReceiver {
            @Override
            public void onReceive(final Context context, Intent intent) {
                // 获取Intent中的Control状态
                int control = intent.getIntExtra("control", -1);
                Log.d("MyReceiver", "MusicService has received a broadcast and the control is " + control);
                switch (control) {
                    // 播放或暂停
                    case 1:
                        // 原来处于没有播放状态
                        if (status == 0x11) {
                            if(current==0){
                                suzy=0x12;
                            }
                            else{
                                suzy=0x11;
                            }
                            // 准备并播放音乐
                            status = 0x12;
                            ep.addMediaItem(MediaItem.fromUri("asset:///" + musics[current]));
                            ep.addMediaItem(MediaItem.fromUri("asset:///" + musics[current+1]));
                            ep.prepare();
                            ep.play();
                            ep.addListener(listener);
                        }
                        // 原来处于播放状态
                        else if (status == 0x12) {
                            // 暂停
                            ep.pause();
                            // 改变为暂停状态
                            status = 0x13;
                        }
                        // 原来处于暂停状态
                        else if (status == 0x13) {
                            // 播放
                            if (current % 2 == 0) {
                                ep.play();
                            } else if (current % 2 == 1) {
                                ep.play();
                            }
                            // 改变状态
                            status = 0x12;
                        }
                        break;
                    //停止声音
                    case 2:
                        if (status == 0x12 || status == 0x13) {
                            ep.stop();
                            ep.removeListener(listener);
                            ep.clearMediaItems();
                            ep.setRepeatMode(Player.REPEAT_MODE_OFF);
                            if (current == 0) {
                                status = 0x11;
                            } else if (current % 2 == 1) {
                                current--;
                                status = 0x11;
                            } else {
                                status = 0x11;
                            }
                        }
                        break;
                    //上一首
                    case 3:
                        // 如果原来正在播放或暂停

                            ep.stop();
                            ep.removeListener(listener);
                            ep.clearMediaItems();
                            ep.setRepeatMode(Player.REPEAT_MODE_OFF);
                            Log.d("NOW",String.valueOf(current));
                            if (current == 0) {
                                current = musics.length - 2;
                            } else if (current == 1) {
                                current = musics.length - 2;
                            } else if (current % 2 == 1) {
                                current--;
                            } else if (current % 2 == 0) {
                                current = current - 2;
                            }
                        if(current==0){
                            suzy=0x12;
                        }
                        else{
                            suzy=0x11;
                        }

                        if (status == 0x12 || status == 0x13) {
                            ep.addMediaItem(MediaItem.fromUri("asset:///" + musics[current]));
                            ep.addMediaItem(MediaItem.fromUri("asset:///" + musics[current + 1]));
                            ep.prepare();
                            ep.play();
                            ep.addListener(listener);
                            status = 0x12;
                        }
                        break;
                    //下一首
                    case 4:
                           ep.stop();
                           ep.removeListener(listener);
                           ep.clearMediaItems();
                           ep.setRepeatMode(Player.REPEAT_MODE_OFF);
                            if (current  == musics.length-1) {
                                current = 0;
                            } else if (current == musics.length - 2) {
                                current = 0;

                            }
                            else if(current%2==0){
                                current=current+2;
                            }
                            else if (current % 2 == 1) {
                                current++;
                            }
                        if(current==0){
                            suzy=0x12;
                        }
                        else{
                            suzy=0x11;
                        }
                        if (status == 0x12 || status == 0x13) {
                            ep.addMediaItem(MediaItem.fromUri("asset:///" + musics[current]));
                            ep.addMediaItem(MediaItem.fromUri("asset:///" + musics[current + 1]));
                            ep.prepare();
                            ep.play();
                            ep.addListener(listener);
                            status = 0x12;
                        }
                        break;
                    case 5:
                        ep.stop();
                        ep.removeListener(listener);
                        ep.clearMediaItems();
                        ep.setRepeatMode(Player.REPEAT_MODE_OFF);
                        current=0;
                        status=0x11;
                    case 6:
                        ep.pause();
                        status=0x13;
                }
                // 广播通知Activity更改图标、文本框
                Intent sendIntent = new Intent(HomeFragment.UPDATE_ACTION);
                sendIntent.putExtra("update", status);
                sendIntent.putExtra("current", current);
                sendIntent.putExtra("suzy",suzy);
                // 发送广播，将被Activity组件中的BroadcastReceiver接收到
                sendBroadcast(sendIntent);
            }


        }
        MyReceiver serviceReceiver;
        // 创建BroadcastReceiver
        serviceReceiver = new MyReceiver();
        registerReceiver(serviceReceiver, filter);

        // 为MediaPlayer播放完成事件绑定监听器
    }
}
