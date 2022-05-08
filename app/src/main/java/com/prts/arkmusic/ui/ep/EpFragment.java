package com.prts.arkmusic.ui.ep;

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
import com.prts.arkmusic.databinding.FragmentEpBinding;
import com.prts.arkmusic.ms3;

import org.xmlpull.v1.XmlPullParser;

import java.util.List;

public class EpFragment extends Fragment{
    public static final String CTL_ACTION = "arkmusic.action.CTL_ACTION";
    public static final String UPDATE_ACTION = "arkmusic.action.UPDATE_ACTION";
    private FragmentEpBinding binding;

    ImageButton play,stop,previous,next;
    ActivityReceiver activityReceiver;
    TextView name, usage;
    ImageView image,suzy;
    int status=0x11;

    String[] titleStrs = {"Spark for Dream","Lullabye","《阴云火花》PV"};
    String[] authorStrs = {"小苏茜的梦想，在罗德岛上继续闪耀","感染者终究，归于温暖的摇篮","在卡拉顿城，苏茜即将实现自己的梦想。"};
    int[] im={R.drawable.sparkfordream,R.drawable.lullabye,R.mipmap.ic_lightsparkindarkness};


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,Bundle savedInstanceState) {
        Log.d("CREATE","CREATE");

        EpViewModel homeViewModel =
                new ViewModelProvider(this).get(EpViewModel.class);



        binding = FragmentEpBinding.inflate(inflater, container, false);


        View root = binding.getRoot();


        return root;


    }
    @Override
    public void onStart(){
        super.onStart();
        getActivity().startService(new Intent(getActivity(), ms3.class));
        Log.d("START","START");
        Intent intent=new Intent(CTL_ACTION);
        play=getView().findViewById(R.id.play3);
        stop=getView().findViewById(R.id.stop3);
        previous=getView().findViewById(R.id.previous3);
        next=getView().findViewById(R.id.next3);
        name = getView().findViewById(R.id.name3);
        usage = getView().findViewById(R.id.usage3);
        image=getView().findViewById(R.id.cover3);
        suzy=getView().findViewById(R.id.suzy3);

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
                intent.putExtra("control",13);
                getActivity().sendBroadcast(intent);
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra("control",23);
                getActivity().sendBroadcast(intent);
            }
        });
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra("control",33);
                getActivity().sendBroadcast(intent);
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra("control",43);
                getActivity().sendBroadcast(intent);
            }
        });
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Intent intent=new Intent(CTL_ACTION);
        intent.putExtra("control",53);
        getActivity().sendBroadcast(intent);
        getActivity().stopService(new Intent(getActivity(),ms3.class));
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
                    Log.d("BLUE","BLUE");
                    intentt.putExtra("control",63);
                    getActivity().sendBroadcast(intentt);
                }
            } if(AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(action)){
                intentt.putExtra("control",63);
                getActivity().sendBroadcast(intentt);
            }
        }

    };
}