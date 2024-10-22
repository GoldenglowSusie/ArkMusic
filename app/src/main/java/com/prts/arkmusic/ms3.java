package com.prts.arkmusic;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.prts.arkmusic.ui.ep.EpFragment;
import com.prts.arkmusic.ui.home.HomeFragment;

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
    AudioManager amm;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        am = getAssets(); //获取附件管理器
        amm=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
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
        Intent sendIntent = new Intent(EpFragment.UPDATE_ACTION);
        sendIntent.putExtra("current3",current);
        sendIntent.putExtra("suzy3",suzy);
        sendBroadcast(sendIntent);

        AudioManager.OnAudioFocusChangeListener afChangeListener=new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                if(focusChange==AudioManager.AUDIOFOCUS_LOSS_TRANSIENT||focusChange==AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK){
                    ep.pause();
                    status=0x13;
                }else if(focusChange==AudioManager.AUDIOFOCUS_GAIN){
                    ep.play();
                    status=0x12;
                }else if(focusChange==AudioManager.AUDIOFOCUS_LOSS){
                    ep.stop();
                    ep.clearMediaItems();
                    status=0x11;
                }
                Intent sendIntent = new Intent(HomeFragment.UPDATE_ACTION);
                sendIntent.putExtra("update3", status);
                sendBroadcast(sendIntent);
            }
        };

        AudioAttributes playbackAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        AudioFocusRequest focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(afChangeListener)
                .build();

        class MyReceiver extends BroadcastReceiver {
            @Override
            public void onReceive(final Context context, Intent intent) {
                // 获取Intent中的Control状态
                Intent sendIntent = new Intent(EpFragment.UPDATE_ACTION);
                int control = intent.getIntExtra("control3", -1);
                switch (control) {
                    // 播放或暂停
                    case 13:
                        // 原来处于没有播放状态
                        if(egg==1){
                            amm.requestAudioFocus(focusRequest);
                            ep.prepare();
                            ep.play();
                            egg=2;
                            status=0x12;
                            sendIntent.putExtra("update3",status);
                            sendIntent.putExtra("suzy3",suzy);
                            sendBroadcast(sendIntent);
                        }
                        if (status == 0x11) {
                            amm.requestAudioFocus(focusRequest);
                            if(current==0||current==2||current==4){
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
                            sendIntent.putExtra("update3", status);
                            sendIntent.putExtra("current3", current);
                            sendIntent.putExtra("suzy3",suzy);
                            sendBroadcast(sendIntent);
                        }
                        // 原来处于播放状态
                        else if (status == 0x12) {
                            amm.abandonAudioFocusRequest(focusRequest);
                            // 暂停
                            ep.pause();
                            // 改变为暂停状态
                            status = 0x13;
                            sendIntent.putExtra("update3", status);
                            sendBroadcast(sendIntent);
                            if(egg==2){}
                            else{
                            sendIntent.putExtra("update3", status);
                            sendIntent.putExtra("current3", current);
                            sendIntent.putExtra("suzy3",suzy);
                            sendBroadcast(sendIntent);}
                        }
                        // 原来处于暂停状态
                        else if (status == 0x13) {
                            // 播放
                            amm.requestAudioFocus(focusRequest);
                            ep.play();
                            // 改变状态
                            status = 0x12;
                            sendIntent.putExtra("update3", status);
                            sendBroadcast(sendIntent);
                            if(egg==2){}
                            else{
                            sendIntent.putExtra("update3", status);
                            sendIntent.putExtra("current3", current);
                            sendIntent.putExtra("suzy3",suzy);
                            sendBroadcast(sendIntent);}
                        }

                        break;
                    //停止声音
                    case 23:
                        egg=0;
                        if (status == 0x12 || status == 0x13) {
                            amm.abandonAudioFocusRequest(focusRequest);
                            ep.stop();
                            ep.clearMediaItems();
                            status=0x11;
                        }
                        sendIntent.putExtra("update3", status);
                        sendIntent.putExtra("current3", current);
                        sendIntent.putExtra("suzy3",suzy);
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
                        if(current==0||current==2||current==4){
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
                        sendIntent.putExtra("update3", status);
                        sendIntent.putExtra("current3", current);
                        sendIntent.putExtra("suzy3",suzy);
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
                        if(current==0||current==2||current==4){
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
                        sendIntent.putExtra("update3", status);
                        sendIntent.putExtra("current3", current);
                        sendIntent.putExtra("suzy3",suzy);
                        sendBroadcast(sendIntent);
                        break;
                    case 53:
                        amm.abandonAudioFocusRequest(focusRequest);
                        ep.release();
                        current=0;
                        status=0x11;
                    case 63:
                        amm.abandonAudioFocusRequest(focusRequest);
                        ep.pause();
                        status=0x13;
                        sendIntent.putExtra("update3", status);
                        sendIntent.putExtra("current3", current);
                        sendIntent.putExtra("suzy3",suzy);
                        sendBroadcast(sendIntent);
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
        NotificationManager notificationManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent pendingIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(this, 3, new Intent(this, MainActivity.class), PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(this, 3, new Intent(this,MainActivity.class), PendingIntent.FLAG_ONE_SHOT);
        }

        //        获取Notification实例
        Notification notification=new NotificationCompat.Builder(this,"ms3")
                .setContentTitle("ArkMusic正在运行")
                .setContentText("ArkMusic正在运行于“EP与PV”。点此以跳转。")
                .setWhen(System.currentTimeMillis())
                //             .setAutoCancel(false) 点击通知栏后是否消失
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_am2))
                // .setCustomContentView(remoteView) // 设置自定义的RemoteView，需要API最低为24
                .setSmallIcon(R.mipmap.ic_am2)
                // 设置点击通知栏后跳转地址
                .setContentIntent(pendingIntent)
                .build();
//        添加渠道
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("ms3", "subscribeName", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("description");
            notificationManager.createNotificationChannel(channel);
        }
        // 设置常驻 Flag
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        //展示通知栏
        notificationManager.notify(3,notification);
        // 为MediaPlayer播放完成事件绑定监听器
    }
}
