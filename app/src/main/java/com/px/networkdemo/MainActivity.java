package com.px.networkdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import com.px.network.NetType;
import com.px.network.Network;
import com.px.network.NetworkManager;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NetworkManager.getInstance().init(this.getApplication());
        NetworkManager.getInstance().registerObserver(this);
    }

    @Network(netType = NetType.AUTO)
    public void onNetChanged(NetType netType) {
        switch (netType) {
            case WIFI:
                Log.e(TAG,"AUTO监控：WIFI CONNECT");
                break;
            case MOBILE:
                Log.e(TAG,"AUTO监控：MOBILE CONNECT");
                break;
            case AUTO:
                Log.e(TAG,"AUTO监控：AUTO CONNECT");
                break;
            case NONE:
                Log.e(TAG,"AUTO监控：NONE CONNECT");
                break;
            default:
                break;
        }
    }

    @Network(netType = NetType.WIFI)
    public void onWifiChanged(NetType netType){
        switch (netType){
            case WIFI:
                Log.e(TAG,"wifi监控：WIFI CONNECT");
                break;
            case NONE:
                Log.e(TAG,"wifi监控：NONE CONNECT");
                break;
        }
    }

    @Network(netType = NetType.MOBILE)
    public void onMobileChanged(NetType netType){
        switch (netType){
            case MOBILE:
                Log.e(TAG,"Mobile监控：MOBILE CONNECT");
                break;
            case NONE:
                Log.e(TAG,"Mobile监控：NONE CONNECT");
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NetworkManager.getInstance().unRegisterObserver(this);
    }
}
