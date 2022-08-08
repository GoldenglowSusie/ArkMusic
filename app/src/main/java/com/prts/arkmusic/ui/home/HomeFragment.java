package com.prts.arkmusic.ui.home;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.prts.arkmusic.MainActivity;
import com.prts.arkmusic.MusicService;
import com.prts.arkmusic.R;
import com.prts.arkmusic.databinding.FragmentHomeBinding;

import org.xmlpull.v1.XmlPullParser;

import java.util.List;

public class HomeFragment extends Fragment{
    public static final String CTL_ACTION = "arkmusic.action.CTL_ACTION";
    public static final String UPDATE_ACTION = "arkmusic.action.UPDATE_ACTION";
    private FragmentHomeBinding binding;

    ImageButton play,stop,previous,next;
    ActivityReceiver activityReceiver;
    TextView name, usage;
    ImageView image,suzy;
    int status=0x11;

    String[] titleStrs = {"陈与魏", "陈与魏", "遗尘漫步","遗尘漫步","多索雷斯假日","多索雷斯假日"};
    String[] authorStrs = {"主线第八章、《阴云火花》", "主线第八章、《阴云火花》", "《遗尘漫步》","《遗尘漫步》","水陈：给我玩明日方舟！","水陈：给我玩明日方舟！"};
    int[] im={R.mipmap.ic_lightsparkindarkness,R.mipmap.ic_lightsparkindarkness,R.mipmap.ic_wd,R.mipmap.ic_wd,R.drawable.doss,R.drawable.doss};


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,Bundle savedInstanceState) {
        Log.d("CREATE","CREATE");

        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);



        binding = FragmentHomeBinding.inflate(inflater, container, false);


        View root = binding.getRoot();


        return root;


    }
    @Override
    public void onStart(){
        super.onStart();
        getActivity().startService(new Intent(getActivity(), MusicService.class));
        Log.d("START","START");
        Intent intent=new Intent(CTL_ACTION);
        play=getView().findViewById(R.id.play);
        stop=getView().findViewById(R.id.stop);
        previous=getView().findViewById(R.id.previous);
        next=getView().findViewById(R.id.next);
        name = getView().findViewById(R.id.name);
        usage = getView().findViewById(R.id.usage);
        image=getView().findViewById(R.id.cover);
        suzy=getView().findViewById(R.id.suzy);

        activityReceiver = new ActivityReceiver();
        // 创建IntentFilter
        IntentFilter filter = new IntentFilter();
        // 指定BroadcastReceiver监听的Action
        filter.addAction(UPDATE_ACTION);
        // 注册BroadcastReceiver
        getActivity().registerReceiver(activityReceiver, filter);

        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        getActivity().registerReceiver(headsetPlugReceiver, intentFilter);
        // for bluetooth headset connection receiver
        IntentFilter bluetoothFilter = new IntentFilter(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
        getActivity().registerReceiver(headsetPlugReceiver, bluetoothFilter);

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 intent.putExtra("control",1);
                 getActivity().sendBroadcast(intent);
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra("control",2);
                getActivity().sendBroadcast(intent);
            }
        });
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra("control",3);
                getActivity().sendBroadcast(intent);
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra("control",4);
                getActivity().sendBroadcast(intent);
            }
        });
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Intent intent=new Intent(CTL_ACTION);
        intent.putExtra("control",5);
        getActivity().sendBroadcast(intent);
        getActivity().stopService(new Intent(getActivity(),MusicService.class));
        binding = null;
    }
    public class ActivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            int update = intent.getIntExtra("update",-1);
            int current = intent.getIntExtra("current", -1);
            int suzyy=intent.getIntExtra("suzy",-1);

            if (current >= 0){
                name.setText(titleStrs[current]);
                usage.setText(authorStrs[current]);
                image.setImageResource(im[current]);
            }
            switch (update){
                case 0x11:
                    play.setImageResource(R.drawable.ic_play);
                    status = 0x11;
                    break;
                case 0x12:
                    play.setImageResource(R.drawable.ic_pause);
                    status = 0x12;
                    break;

                case 0x13:
                    play.setImageResource(R.drawable.ic_play);
                    status = 0x13;
                    break;
            }
            switch(suzyy){
                case 0x11:
                    suzy.setImageResource(R.drawable.head);
                    break;
                case 0x12:
                    suzy.setImageResource(R.drawable.head2);
            }
        }
    }
    private BroadcastReceiver headsetPlugReceiver = new BroadcastReceiver() {
        Intent intentt=new Intent(CTL_ACTION);
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                if(BluetoothProfile.STATE_DISCONNECTED == adapter.getProfileConnectionState(BluetoothProfile.HEADSET)) {
                    //Bluetooth headset is now disconnected
                    intentt.putExtra("control",6);
                    intentt.putExtra("control2",62);
                    intentt.putExtra("control3",63);
                    intentt.putExtra("control4",64);
                    getActivity().sendBroadcast(intentt);
                }
            } if(AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(action)){
                intentt.putExtra("control",6);
                intentt.putExtra("control2",62);
                intentt.putExtra("control3",63);
                intentt.putExtra("control4",64);
                getActivity().sendBroadcast(intentt);
            }
        }

    };
}